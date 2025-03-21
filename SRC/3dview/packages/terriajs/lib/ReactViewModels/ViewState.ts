import {
  action,
  computed,
  IReactionDisposer,
  observable,
  reaction,
  runInAction
} from "mobx";
import { Ref } from "react";
import defined from "terriajs-cesium/Source/Core/defined";
import CesiumEvent from "terriajs-cesium/Source/Core/Event";
import addedByUser from "../Core/addedByUser";
import {
  Category,
  HelpAction,
  StoryAction,
  LotSearchAction,
  ApplicationCategorySelection,
  GeneralConditionDiagnosis,
  DistrictNameSearchAction,
  KanaDistrictNameSearchAction,
  EnterApplicantInformation,
  UploadApplicationInformation,
  ConfirmApplicationDetails,
  CustomMessage,
  AnswerLogin,
  AnswerContent,
  UserAgreement,
  MapSelection,
  CharacterSelection,
  ApplicationInformationSearch,
  ApplicationDetails,
  AnswerInput,
  ApplicationList
} from "../Core/AnalyticEvents/analyticEvents";
import Result from "../Core/Result";
import triggerResize from "../Core/triggerResize";
import PickedFeatures from "../Map/PickedFeatures";
import CatalogMemberMixin, { getName } from "../ModelMixins/CatalogMemberMixin";
import GroupMixin from "../ModelMixins/GroupMixin";
import MappableMixin from "../ModelMixins/MappableMixin";
import ReferenceMixin from "../ModelMixins/ReferenceMixin";
import CommonStrata from "../Models/Definition/CommonStrata";
import { BaseModel } from "../Models/Definition/Model";
import getAncestors from "../Models/getAncestors";
import Terria from "../Models/Terria";
import { SATELLITE_HELP_PROMPT_KEY } from "../ReactViews/HelpScreens/SatelliteHelpPrompt";
import { animationDuration } from "../ReactViews/StandardUserInterface/StandardUserInterface";
import {
  defaultTourPoints,
  RelativePosition,
  TourPoint
} from "./defaultTourPoints";
import DisclaimerHandler from "./DisclaimerHandler";
import SearchState from "./SearchState";
import ViewerMode, {
  MapViewers,
  setViewerMode
} from "../Models/ViewerMode";
import ImagerySplitDirection from "terriajs-cesium/Source/Scene/ImagerySplitDirection";
import webMapServiceCatalogItem from '../Models/Catalog/Ows/WebMapServiceCatalogItem';
import Config from "../../customconfig.json";

export const DATA_CATALOG_NAME = "data-catalog";
export const USER_DATA_NAME = "my-data";

// check showWorkbenchButton delay and transforms
// export const WORKBENCH_RESIZE_ANIMATION_DURATION = 250;
export const WORKBENCH_RESIZE_ANIMATION_DURATION = 500;

interface ViewStateOptions {
  terria: Terria;
  catalogSearchProvider: any;
  locationSearchProviders: any[];
  errorHandlingProvider?: any;
}

/**
 * Root of a global view model. Presumably this should get nested as more stuff goes into it. Basically this belongs to
 * the root of the UI and then it can choose to pass either the whole thing or parts down as props to its children.
 */

export default class ViewState {
  readonly mobileViewOptions = Object.freeze({
    data: "data",
    preview: "preview",
    nowViewing: "nowViewing",
    locationSearchResults: "locationSearchResults"
  });
  readonly searchState: SearchState;
  readonly terria: Terria;
  readonly relativePosition = RelativePosition;

  @observable private _previewedItem: BaseModel | undefined;
  get previewedItem() {
    return this._previewedItem;
  }
  @observable userDataPreviewedItem: BaseModel | undefined;
  @observable explorerPanelIsVisible: boolean = false;
  @observable activeTabCategory: string = DATA_CATALOG_NAME;
  @observable activeTabIdInCategory: string | undefined = undefined;
  @observable isDraggingDroppingFile: boolean = false;
  @observable mobileView: string | null = null;
  @observable isMapFullScreen: boolean = false;
  @observable myDataIsUploadView: boolean = true;
  @observable mobileMenuVisible: boolean = false;
  @observable explorerPanelAnimating: boolean = false;
  @observable topElement: string = "FeatureInfo";
  @observable lastUploadedFiles: any[] = [];
  @observable storyBuilderShown: boolean = false;

  // Flesh out later
  @observable showHelpMenu: boolean = false;
  @observable showSatelliteGuidance: boolean = false;
  @observable showWelcomeMessage: boolean = false;
  @observable selectedHelpMenuItem: string = "";
  @observable helpPanelExpanded: boolean = false;
  @observable disclaimerSettings: any | undefined = undefined;
  @observable disclaimerVisible: boolean = false;
  @observable videoGuideVisible: string = "";

  @observable trainerBarVisible: boolean = false;
  @observable trainerBarExpanded: boolean = false;
  @observable trainerBarShowingAllSteps: boolean = false;
  @observable selectedTrainerItem: string = "";
  @observable currentTrainerItemIndex: number = 0;
  @observable currentTrainerStepIndex: number = 0;

  @observable printWindow: Window | null = null;

  //↓カスタム機能
  
  //↓↓↓↓↓画面にコンポーネント表示・非表示制御用変数↓↓↓↓↓
  // 共通：地番検索画面表示有無
  @observable showLotNumberSearch: boolean = true;
  // 共通：町丁名一覧画面表示有無
  @observable showDistrictNameSearch: boolean = false;
  // 共通：かな検索画面表示有無
  @observable showKanaDistrictNameSearch: boolean = false;
  // 共通：ファイルプレビューモーダル表示
  @observable filePreViewModalShow: boolean = false;
  // 共通：ファイルダウンロードモーダル表示
  @observable fileDownloadModalShow: boolean = false;
  // 共通：カスタムメッセージ画面表示有無 
  @observable showCustomMessage: boolean = false;

  // 事業者：地番選択結果一覧表示有無
  @observable showLotNumberSelected: boolean = false;
  // 事業者：トップ画面表示有無
  @observable showInitAndLotNumberSearchView: boolean = true;
  @observable isClickEvent: boolean = true;
  // 事業者：申請区分選択画面表示有無
  @observable showInputApplyConditionView: boolean = false;
  // 事業者：概況診断結果表示画面表示有無
  @observable showGeneralAndRoadJudgementResultView: boolean = false;
  // 事業者：申請フォーム表示有無
  @observable showApplyInformationView: boolean = false;
  // 事業者：申請者情報入力画面表示有無 
  @observable showEnterApplicantInformation: boolean = false;
  // 事業者：申請ファイルアップロード画面表示有無 
  @observable showUploadApplicationInformation: boolean = false;
  // 事業者：申請内容確認画面表示有無 
  @observable showConfirmApplicationDetails: boolean = false;
  // 事業者：回答確認画面表示有無
  @observable showConfirmAnswerInformationView: boolean = false;
  // 事業者：問合せ画面表示有無
  @observable showChatView: boolean = false;
  // 事業者：問い合わせ添付ファイルアップロードモーダル表示
  @observable inquiryFileUploadModalShow: boolean = false;
  // 事業者：sideパネル表示・非表示
  @observable showSidePanel: boolean = false;
  // 事業者：利用者規約画面表示有無 
  @observable showUserAgreement: boolean = false;

  // 行政：サイトパネルのアクティブタブ
  @observable adminTabActive: string = "mapSearch";
  // 行政：地図検索タブの問合せタブ表示有無
  @observable showInquiryList:boolean = true;
  // 行政：地図検索タブの回答タブ表示有無
  @observable showAnswerList:boolean = false;
  // 行政：申請検索タブ内のアクティブページ
  @observable applyPageActive: string = "applyList";

  // 行政：ファイルアップロードモーダル表示
  @observable fileUploadModalShow: boolean = false;
  // 行政：発行様式アップロードモーダル表示
  @observable issuanceFileUploadModalShow: boolean = false;
  // 行政：チャット宛先入力モーダル表示
  @observable inputChatAddressModalShow: boolean = false;
  // 行政: 回答テンプレート選択モーダル表示（仕様変更と伴い、回答入力モーダルを使うため、廃止）
  @observable inputAnswerTemplateModalShow: boolean = false;
  // 行政: 回答入力モーダル表示
  @observable inputAnswerContentModalShow: boolean = false;
  // 行政：申請地番表示パネル表示有無
  @observable showApplicationLotNumberPanel: boolean = false;
  // 行政：申請情報検索画面表示有無
  @observable showApplicationInformationSearch: boolean = false;
  // 行政：申請情報詳細画面表示有無
  @observable showApplicationDetails: boolean = false;
  // 行政：回答内容入力画面表示有無
  @observable showAnswerInput: boolean = false;
  // 行政：申請一覧のサブ画面表示有無
  @observable showApplicationList: boolean = false;
  // 行政：PDFビュワー表示
  @observable showPdfViewer: boolean = false;

  // 申請区分選択画面表示有無 
  @observable showApplicationCategorySelection: boolean = false;
  // 地図選択画面表示有無 
  @observable showMapSelection: boolean = false;
  // 文字選択画面表示有無 
  @observable showCharacterSelection: boolean = false;
  // 概況診断画面表示有無 
  @observable showGeneralConditionDiagnosis: boolean = false;
  // 申請ID/パスワード照合画面表示有無
  @observable showAnswerLogin: boolean = false;
  // 申請内容確認画面表示有無
  @observable showAnswerContent: boolean = false;

  /* ■ R6年度追加 ↓↓↓ */
  // 行政: 回答申請種別選択モーダル表示
  @observable selectApplicationClassModalShow: boolean = false;
  // 行政: 回答通知確認画面モーダル表示
  @observable confirmAnswerNotificationModalShow: boolean = false;
  // 事業者:概況診断レポート一覧画面
  @observable generalConditionDiagnosisrReportShow: boolean = false;

  //↑↑↑↑↑画面にコンポーネント表示・非表示制御用変数↑↑↑↑↑

  //↓↓↓↓↓各画面に利用している変数↓↓↓↓↓

  //------------共通------------

  //3dModeキー
  map3dModeKey: keyof typeof MapViewers = "3d";

  /* ■ R6年度追加 */
  // 1=事前相談,2=事前協議,3=許可判定
  @observable checkedApplicationStepId:number = 1;

  //------------共通：カスタムメッセージ表示（申請完了（SC112）、回答登録完了（SC211）、回答完了通知（SC212））------------

  // 申請完了であるかフラグ
  @observable isApplyCompleted: boolean = false;
  // 回答完了であるかフラグ
  @observable isAnswerCompleted: boolean = false;
  // 回答完了であるかフラグ
  @observable isNotifiedCompleted: boolean = false;
  //カスタムメッセージ タイトル
  customMessageTitle:string = "";
  //カスタムメッセージ 内容
  customMessageContent:string = "";

  //------------共通：地番検索（SC001）------------
  //検索対象の町丁名
  @observable searchDistrictName:any = null;
  //検索対象の町丁ID
  @observable searchDistrictId:any = null;
  //検索対象の地番
  @observable searchLotNum:any = null;
  //検索結果
  @observable searchResult:any = null;
  //地番検索結果チェックボックス選択状態
  @observable selectLotSearchResult:any[] = [];
  //地番検索パネル表示フラグ true:地番検索単体 false:申請地選択に埋め込み
  islotNumberSearchViewIndependent: boolean = true;
  //地番検索パネル位置補正値(top)
  lotNumberSearchPosDiffTop:number = 0;
  //地番検索パネル位置補正値(left)
  lotNumberSearchPosDiffLeft:number = 0;
  //町丁名一覧結果保持用
  DistrictNameSearch:any="";
  //地番検索結果一覧保持用
  SearchRsultContent:any="";
  //かな検索結果一覧保持用
  KanaDistrictNameSearch:any="";
  // 選択地番情報
  lotNumbers: object = {};
  
  //------------共通：レイヤ一覧表示画面（SC002）------------
  // レイヤ画面にグループをクローズするために、非表示となる対象
  hiddenItems: object[] = [];
  // レイヤ画面に表示している対象
  displayItems: object[] = [];
  // 初期のカタログデータのバックアップ(各groupの展開状態)
  groupItemListBak: object[] = [];
  // 初期のカタログデータのバックアップ(初期の表示レイヤ)
  displayingItemListBak: object[] = [];
  // ドラッグ時に、移動先のindex
  currentDragItemIndex: number = -1;

  //------------事業者：トップ画面（SC102）------------
  // 地番検索で地番選択するかフラグ
  @observable islotNumberSearch: boolean = true;
  // 申請地情報
  @observable applicationPlace:any[] = [];
  // 申請地情報temp用
  applicationPlaceTemp:any[] = [];

  //------------事業者：申請区分選択画面（SC104）------------
  // 選択済み申請
  selectedApplicationType:object = {};
  // 選択済み申請区分
  checkedApplicationCategory:Object = [];
  // 選択済み申請区分ローカル保持用
  checkedApplicationCategoryLocalSave:Object = [];

  //------------事業者：概況診断結果表示画面（SC105）------------
  // 概況診断結果
  generalConditionDiagnosisResult:Object = {};
  // 一時フォルダ名
  folderName:string = "";
  /* ■ R6年度追加 */
  // 一時ファイル名
  fileName:string = "";
  // 申請ID
  applicationId:number = 0;

  //------------事業者：申請者情報入力画面（SC108）------------
  // 申請者情報
  applicantInformation:Object = [];

  //------------事業者：申請対象ファイルアップロード画面（SC109）------------
  // 再申請であるかフラグ
  @observable isReApply: boolean = false;
  // 再申請情報
  reApplication:Object = {};
  // 再申請ファイル
  reApplicationFile:Object = {};
  // 申請ファイル
  applicationFile:Object = {};

  //------------事業者：申請ID/パスワード認証画面（SC113）------------
  // 回答内容確認
  answerContent:Object = {};

  //------------事業者：回答内容確認画面（SC114）------------
  //選択中の回答内容の部署回答ID
  currentDepartmentAnswerId:any="";
  //選択中の回答内容の回答ID
  currentAnswerId:any="";
  
  //------------事業者：問合せファイル選択（SC117）------------
  // 問い合わせ添付ファイルリスト
  inquiryFiles: object[] = [];

  //------------行政：トップ画面（SC202）------------
  // 行政：戻るボタンの遷移先
  @observable adminBackTab: string = "";
  // 行政：戻るボタンの遷移先
  @observable adminBackPage: string = "";
  //申請ID
  @observable applicationInformationSearchForApplicationId:string = "0";
  // 選択された問い合わせのチャット情報
  currentChat:Object = {};
  // チャットの遷移元
  backToPage:Object = {};

  //------------行政：回答登録画面（SC207）------------
  // 行政: 回答テンプレート入力対象ターゲット
  answerTemplateTarget: Object = {}
  // 行政：申請回答情報
  applyAnswerForm:Object = {};
  // 子コンポネントに渡すcallback関数
  @observable callBackFunction:Function = ()=>{};

  //------------行政：申請ファイル選択ダイアログ画面（SC206）------------
  // 行政：ファイルダウンロードターゲット
  @observable fileDownloadTarget: string = "";
  // 行政：ファイルダウンロード対象ファイル
  fileDownloadFile:Object[] = [];

  //------------行政：回答ファイル選択ダイアログ画面（SC209）------------
  // 行政：ファイル引用対象ファイル(申請ファイル)
  fileQuoteFile: Object = {};

  // 行政：ファイル引用対象ファイル(登録済み回答ファイル)
  answerQuoteFile: Object = {};

  // 行政：発行様式アップロード対象
  @observable issuanceLedgerForm: object = {};

  //------------行政：宛先選択ダイアログ画面（SC214）------------
  // 行政：チャット宛先入力
  inputChatAddress:any[] = [];

  //------------行政：申請情報一覧------------
  // 行政：地番に紐づく申請リスト
  @observable applicationLotNumberList: object[] = [];

  // 申請一覧
  applicationList:Object = {};

  // 行政：ファイル編集ターゲット
  @observable fileEditTarget: string = "";

  //行政側か否か
  isUserGoverment: boolean = false;

  //申請情報詳細リフレッシュ処理
  @observable refreshConfirmApplicationDetails:Function = ()=>{};

  /* ■ R6年度追加 */
  //------------拡大/縮小MAP------------
  //サイドパネルのフルスクリーン状態
  @observable isSidePanelFullScreen: boolean = false;
  //マップ拡大状態
  @observable mapExpansionFlag: boolean = false;
  //マップのリサイズ
  @observable updateMapDimensions: Function = () => {};
  //マップ位置(下)
  @observable mapBottom: string = "";
  //マップ位置(右)
  @observable mapRight: string = "2vh";
  //マップ位置(上)
  @observable mapTop: string = "66vh";
  //マップ位置(左)
  @observable mapLeft: string = "";
  //マップ位置(幅)
  @observable mapWidth: string = "21vw";
  //マップ位置(高さ)
  @observable mapHeight: string = "23vh";
  //マップ配置ベースコンテナ要素
  @observable mapBaseContainerElement: Ref<HTMLElement> = null;
  //マップ配置ベース要素
  @observable mapBaseElement: Ref<HTMLElement> = null;

  //------------シミュレート実行関連------------
  //simulator実行中か否か
  @observable isSimulateExecution:boolean =false;
  //simulator進捗管理用
  @observable simulateProgressList:[] = [];

  //------------事業者： 再申請画面（SC115）------------
  // 申請追加情報
  applicantnAddInformation:Object = {};

  // 再申請の場合、処理中申請段階ID
  @observable reapplyApplicationStepId: number = 1;
  // 再申請の場合、処理中申請の前回申請段階ID
  @observable preReapplyApplicationStepId: number = 1;

  //↑↑↑↑↑各画面に利用している変数↑↑↑↑↑

  //選択中地番への移動処理イベント
  @observable mapFitButtonFunction:any = () => {};
  @action
  setMapFitButtonFunction(mapFitButtonFunction: any) {
    this.mapFitButtonFunction = mapFitButtonFunction;
  }

  //↑カスタム機能
  @action
  setSelectedTrainerItem(trainerItem: string) {
    this.selectedTrainerItem = trainerItem;
  }
  @action
  setTrainerBarVisible(bool: boolean) {
    this.trainerBarVisible = bool;
  }
  @action
  setTrainerBarShowingAllSteps(bool: boolean) {
    this.trainerBarShowingAllSteps = bool;
  }
  @action
  setTrainerBarExpanded(bool: boolean) {
    this.trainerBarExpanded = bool;
    // if collapsing trainer bar, also hide steps
    if (!bool) {
      this.trainerBarShowingAllSteps = bool;
    }
  }
  @action
  setCurrentTrainerItemIndex(index: number) {
    this.currentTrainerItemIndex = index;
    this.currentTrainerStepIndex = 0;
  }
  @action
  setCurrentTrainerStepIndex(index: number) {
    this.currentTrainerStepIndex = index;
  }

  /**
   * Bottom dock state & action
   */
  @observable bottomDockHeight: number = 0;
  @action
  setBottomDockHeight(height: number) {
    if (this.bottomDockHeight !== height) {
      this.bottomDockHeight = height;
    }
  }

  @observable workbenchWithOpenControls: string | undefined = undefined;

  errorProvider: any | null = null;

  // default value is null, because user has not made decision to show or
  // not show story
  // will be explicitly set to false when user 1. dismiss story
  // notification or 2. close a story
  @observable storyShown: boolean | null = null;

  @observable currentStoryId: number = 0;
  @observable featurePrompts: any[] = [];

  /**
   * we need a layering system for touring the app, but also a way for it to be
   * chopped and changed from a terriamap
   *
   * this will be slightly different to the help sequences that were done in
   * the past, but may evolve to become a "sequence" (where the UI gets
   * programatically toggled to delve deeper into the app, e.g. show the user
   * how to add data via the data catalog window)
   *
   * rough points
   * - "all guide points visible"
   * -
   *

   * draft structure(?):
   *
   * maybe each "guide" item will have
   * {
   *  ref: (react ref object)
   *  dotOffset: (which way the dot and guide should be positioned relative to the ref component)
   *  content: (component, more flexibility than a string)
   * ...?
   * }
   * and guide props?
   * {
   *  enabled: parent component to decide this based on active index
   * ...?
   * }
   *  */

  @observable tourPoints: TourPoint[] = defaultTourPoints;
  @observable showTour: boolean = false;
  @observable appRefs: Map<string, Ref<HTMLElement>> = new Map();
  @observable currentTourIndex: number = -1;
  @observable showCollapsedNavigation: boolean = false;

  get tourPointsWithValidRefs() {
    // should viewstate.ts reach into document? seems unavoidable if we want
    // this to be the true source of tourPoints.
    // update: well it turns out you can be smarter about it and actually
    // properly clean up your refs - so we'll leave that up to the UI to
    // provide valid refs
    return this.tourPoints
      .sort((a, b) => {
        return a.priority - b.priority;
      })
      .filter(
        tourPoint => (<any>this.appRefs).get(tourPoint.appRefName)?.current
      );
  }
  @action
  setTourIndex(index: number) {
    this.currentTourIndex = index;
  }
  @action
  setShowTour(bool: boolean) {
    this.showTour = bool;
    // If we're enabling the tour, make sure the trainer is collapsed
    if (bool) {
      this.setTrainerBarExpanded(false);
    }
  }
  @action
  closeTour() {
    this.currentTourIndex = -1;
    this.showTour = false;
  }
  @action
  previousTourPoint() {
    const currentIndex = this.currentTourIndex;
    if (currentIndex !== 0) {
      this.currentTourIndex = currentIndex - 1;
    }
  }
  @action
  nextTourPoint() {
    const totalTourPoints = this.tourPointsWithValidRefs.length;
    const currentIndex = this.currentTourIndex;
    if (currentIndex >= totalTourPoints - 1) {
      this.closeTour();
    } else {
      this.currentTourIndex = currentIndex + 1;
    }
  }
  @action
  closeCollapsedNavigation() {
    this.showCollapsedNavigation = false;
  }

  @action
  updateAppRef(refName: string, ref: Ref<HTMLElement>) {
    if (!this.appRefs.get(refName) || this.appRefs.get(refName) !== ref) {
      this.appRefs.set(refName, ref);
    }
  }
  @action
  deleteAppRef(refName: string) {
    this.appRefs.delete(refName);
  }

  /**
   * Gets or sets a value indicating whether the small screen (mobile) user interface should be used.
   * @type {Boolean}
   */
  @observable useSmallScreenInterface: boolean = false;

  /**
   * Gets or sets a value indicating whether the feature info panel is visible.
   * @type {Boolean}
   */
  @observable featureInfoPanelIsVisible: boolean = false;

  /**
   * Gets or sets a value indicating whether the feature info panel is collapsed.
   * When it's collapsed, only the title bar is visible.
   * @type {Boolean}
   */
  @observable featureInfoPanelIsCollapsed: boolean = false;

  /**
   * True if this is (or will be) the first time the user has added data to the map.
   * @type {Boolean}
   */
  @observable firstTimeAddingData: boolean = true;

  /**
   * Gets or sets a value indicating whether the feedback form is visible.
   * @type {Boolean}
   */
  @observable feedbackFormIsVisible: boolean = false;

  /**
   * Gets or sets a value indicating whether the catalog's model share panel
   * is currently visible.
   */
  @observable shareModelIsVisible: boolean = false;

  /**
   * The currently open tool
   */
  @observable currentTool?: Tool;

  @observable panel: React.ReactNode;

  private _pickedFeaturesSubscription: IReactionDisposer;
  private _disclaimerVisibleSubscription: IReactionDisposer;
  private _isMapFullScreenSubscription: IReactionDisposer;
  private _showStoriesSubscription: IReactionDisposer;
  private _mobileMenuSubscription: IReactionDisposer;
  private _storyPromptSubscription: IReactionDisposer;
  private _previewedItemIdSubscription: IReactionDisposer;
  private _workbenchHasTimeWMSSubscription: IReactionDisposer;
  private _storyBeforeUnloadSubscription: IReactionDisposer;
  private _disclaimerHandler: DisclaimerHandler;

  constructor(options: ViewStateOptions) {
    const terria = options.terria;
    this.searchState = new SearchState({
      terria: terria,
      catalogSearchProvider: options.catalogSearchProvider,
      locationSearchProviders: options.locationSearchProviders
    });

    this.errorProvider = options.errorHandlingProvider
      ? options.errorHandlingProvider
      : null;
    this.terria = terria;

    // When features are picked, show the feature info panel.
    this._pickedFeaturesSubscription = reaction(
      () => this.terria.pickedFeatures,
      (pickedFeatures: PickedFeatures | undefined) => {
        if (defined(pickedFeatures)) {
          this.featureInfoPanelIsVisible = true;
          this.featureInfoPanelIsCollapsed = false;
        } else {
          this.featureInfoPanelIsVisible = false;
        }
      }
    );
    // When disclaimer is shown, ensure fullscreen
    // unsure about this behaviour because it nudges the user off center
    // of the original camera set from config once they acknowdge
    this._disclaimerVisibleSubscription = reaction(
      () => this.disclaimerVisible,
      disclaimerVisible => {
        if (disclaimerVisible) {
          this.isMapFullScreen = true;
          this.setIsSidePanelFullScreen(false);
        } else if (!disclaimerVisible && this.isMapFullScreen) {
          this.isMapFullScreen = false;
        }
      }
    );

    this._isMapFullScreenSubscription = reaction(
      () =>
        terria.userProperties.get("hideWorkbench") === "1" ||
        terria.userProperties.get("hideExplorerPanel") === "1",
      (isMapFullScreen: boolean) => {
        this.isMapFullScreen = isMapFullScreen;
        if(this.isMapFullScreen){
          this.setIsSidePanelFullScreen(false);
        }

        // if /#hideWorkbench=1 exists in url onload, show stories directly
        // any show/hide workbench will not automatically show story
        if (!defined(this.storyShown)) {
          // why only checkk config params here? because terria.stories are not
          // set at the moment, and that property will be checked in rendering
          // Here are all are checking are: is terria story enabled in this app?
          // if so we should show it when app first laod, if workbench is hiddne
          this.storyShown = terria.configParameters.storyEnabled;
        }
      }
    );

    this._showStoriesSubscription = reaction(
      () => Boolean(terria.userProperties.get("playStory")),
      (playStory: boolean) => {
        this.storyShown = terria.configParameters.storyEnabled && playStory;
      }
    );

    this._mobileMenuSubscription = reaction(
      () => this.mobileMenuVisible,
      (mobileMenuVisible: boolean) => {
        if (mobileMenuVisible) {
          this.explorerPanelIsVisible = false;
          this.switchMobileView(null);
        }
      }
    );

    this._disclaimerHandler = new DisclaimerHandler(terria, this);

    this._workbenchHasTimeWMSSubscription = reaction(
      () => this.terria.workbench.hasTimeWMS,
      (hasTimeWMS: boolean) => {
        if (
          this.terria.configParameters.showInAppGuides &&
          hasTimeWMS === true &&
          // // only show it once
          !this.terria.getLocalProperty(`${SATELLITE_HELP_PROMPT_KEY}Prompted`)
        ) {
          this.setShowSatelliteGuidance(true);
          this.toggleFeaturePrompt(SATELLITE_HELP_PROMPT_KEY, true, true);
        }
      }
    );

    this._storyPromptSubscription = reaction(
      () => this.storyShown,
      (storyShown: boolean | null) => {
        if (storyShown === false) {
          // only show it once
          if (!this.terria.getLocalProperty("storyPrompted")) {
            this.toggleFeaturePrompt("story", true, false);
          }
        }
      }
    );

    this._previewedItemIdSubscription = reaction(
      () => this.terria.previewedItemId,
      (previewedItemId: string | undefined) => {
        if (previewedItemId === undefined) {
          return;
        }

        const model = this.terria.getModelById(BaseModel, previewedItemId);
        if (model !== undefined) {
          this.viewCatalogMember(model);
        }
      }
    );

    const handleWindowClose = (e: BeforeUnloadEvent) => {
      // Cancel the event
      e.preventDefault(); // If you prevent default behavior in Mozilla Firefox prompt will always be shown
      // Chrome requires returnValue to be set
      e.returnValue = "";
    };

    this._storyBeforeUnloadSubscription = reaction(
      () => this.terria.stories.length > 0,
      hasScenes => {
        if (hasScenes) {
          window.addEventListener("beforeunload", handleWindowClose);
        } else {
          window.removeEventListener("beforeunload", handleWindowClose);
        }
      }
    );
  }

  dispose() {
    this._pickedFeaturesSubscription();
    this._disclaimerVisibleSubscription();
    this._mobileMenuSubscription();
    this._isMapFullScreenSubscription();
    this._showStoriesSubscription();
    this._storyPromptSubscription();
    this._previewedItemIdSubscription();
    this._workbenchHasTimeWMSSubscription();
    this._disclaimerHandler.dispose();
    this.searchState.dispose();
  }

  @action
  triggerResizeEvent() {
    triggerResize();
  }

  @action
  setIsMapFullScreen(
    bool: boolean,
    animationDuration = WORKBENCH_RESIZE_ANIMATION_DURATION
  ) {
    this.isMapFullScreen = bool;
    if(this.isMapFullScreen){
      this.setIsSidePanelFullScreen(false);
    }
    // Allow any animations to finish, then trigger a resize.

    // (wing): much better to do by listening for transitionend, but will leave
    // this as is until that's in place
    setTimeout(function() {
      // should we do this here in viewstate? it pulls in browser dependent things,
      // and (defensively) calls it.
      // but only way to ensure we trigger this resize, by standardising fullscreen
      // toggle through an action.
      triggerResize();
    }, animationDuration);
  }

  @action
  toggleStoryBuilder() {
    this.storyBuilderShown = !this.storyBuilderShown;
  }

  @action
  setTopElement(key: string) {
    this.topElement = key;
  }

  @action
  openAddData() {
    this.explorerPanelIsVisible = true;
    this.activeTabCategory = DATA_CATALOG_NAME;
    this.switchMobileView(this.mobileViewOptions.data);
  }

  @action
  openUserData() {
    this.explorerPanelIsVisible = true;
    this.activeTabCategory = USER_DATA_NAME;
  }

  @action
  closeCatalog() {
    this.explorerPanelIsVisible = false;
    this.switchMobileView(null);
    this.clearPreviewedItem();
  }

  @action
  searchInCatalog(query: string) {
    this.openAddData();
    this.searchState.catalogSearchText = query;
    this.searchState.searchCatalog();
  }

  @action
  clearPreviewedItem() {
    this.userDataPreviewedItem = undefined;
    this._previewedItem = undefined;
  }

  /**
   * Views a model in the catalog. If model is a
   *
   * - `Reference` - it will be dereferenced first.
   * - `CatalogMember` - `loadMetadata` will be called
   * - `Group` - its `isOpen` trait will be set according to the value of the `isOpen` parameter in the `stratum` indicated.
   *   - If after doing this the group is open, its members will be loaded with a call to `loadMembers`.
   * - `Mappable` - `loadMapItems` will be called
   *
   * Then (if no errors have occurred) it will open the catalog.
   * Note - `previewItem` is set at the start of the function, regardless of errors.
   *
   * @param item The model to view in catalog.
   * @param [isOpen=true] True if the group should be opened. False if it should be closed.
   * @param stratum The stratum in which to mark the group opened or closed.
   * @param openAddData True if data catalog window should be opened.
   */
  async viewCatalogMember(
    item: BaseModel,
    isOpen: boolean = true,
    stratum: string = CommonStrata.user,
    openAddData = true
  ): Promise<Result<void>> {
    try {
      // Get referenced target first.
      if (ReferenceMixin.isMixedInto(item)) {
        (await item.loadReference()).throwIfError();
        if (item.target) {
          return this.viewCatalogMember(item.target);
        } else {
          return Result.error(`Could not view catalog member ${getName(item)}`);
        }
      }
      const theItem: BaseModel =
        ReferenceMixin.isMixedInto(item) && item.target ? item.target : item;

      // Set preview item
      runInAction(() => (this._previewedItem = theItem));

      // Open "Add Data"
      if (openAddData) {
        if (addedByUser(theItem)) {
          runInAction(() => (this.userDataPreviewedItem = theItem));

          this.openUserData();
        } else {
          runInAction(() => {
            this.openAddData();
            if (this.terria.configParameters.tabbedCatalog) {
              const parentGroups = getAncestors(item);
              if (parentGroups.length > 0) {
                // Go to specific tab
                this.activeTabIdInCategory = parentGroups[0].uniqueId;
              }
            }
          });
        }

        // mobile switch to now vewing if not viewing a group
        if (!GroupMixin.isMixedInto(theItem)) {
          this.switchMobileView(this.mobileViewOptions.preview);
        }
      }

      if (GroupMixin.isMixedInto(theItem)) {
        theItem.setTrait(stratum, "isOpen", isOpen);
        if (theItem.isOpen) {
          (await theItem.loadMembers()).throwIfError();
        }
      } else if (MappableMixin.isMixedInto(theItem))
        (await theItem.loadMapItems()).throwIfError();
      else if (CatalogMemberMixin.isMixedInto(theItem))
        (await theItem.loadMetadata()).throwIfError();
    } catch (e) {
      return Result.error(e, `Could not view catalog member ${getName(item)}`);
    }
    return Result.none();
  }

  @action
  switchMobileView(viewName: string | null) {
    this.mobileView = viewName;
  }

  @action
  showHelpPanel() {
    this.terria.analytics?.logEvent(Category.help, HelpAction.panelOpened);
    this.showHelpMenu = true;
    this.helpPanelExpanded = false;
    this.selectedHelpMenuItem = "";
    this.setTopElement("HelpPanel");
  }

  @action
  selectHelpMenuItem(key: string) {
    this.selectedHelpMenuItem = key;
    this.helpPanelExpanded = true;
  }

  @action
  hideHelpPanel() {
    this.showHelpMenu = false;
  }

  @action
  changeSearchState(newText: string) {
    this.searchState.catalogSearchText = newText;
  }

  @action
  setDisclaimerVisible(bool: boolean) {
    this.disclaimerVisible = bool;
  }

  @action
  hideDisclaimer() {
    this.setDisclaimerVisible(false);
  }

  @action
  setShowSatelliteGuidance(showSatelliteGuidance: boolean) {
    this.showSatelliteGuidance = showSatelliteGuidance;
  }

  @action
  setShowWelcomeMessage(welcomeMessageShown: boolean) {
    this.showWelcomeMessage = welcomeMessageShown;
  }

  @action
  setVideoGuideVisible(videoName: string) {
    this.videoGuideVisible = videoName;
  }

  /**
   * Removes references of a model from viewState
   */
  @action
  removeModelReferences(model: BaseModel) {
    if (this._previewedItem === model) this._previewedItem = undefined;
    if (this.userDataPreviewedItem === model)
      this.userDataPreviewedItem = undefined;
  }

  @action
  toggleFeaturePrompt(
    feature: string,
    state: boolean,
    persistent: boolean = false
  ) {
    const featureIndexInPrompts = this.featurePrompts.indexOf(feature);
    if (
      state &&
      featureIndexInPrompts < 0 &&
      !this.terria.getLocalProperty(`${feature}Prompted`)
    ) {
      this.featurePrompts.push(feature);
    } else if (!state && featureIndexInPrompts >= 0) {
      this.featurePrompts.splice(featureIndexInPrompts, 1);
    }
    if (persistent) {
      this.terria.setLocalProperty(`${feature}Prompted`, true);
    }
  }

  viewingUserData() {
    return this.activeTabCategory === USER_DATA_NAME;
  }

  afterTerriaStarted() {
    if (this.terria.configParameters.openAddData) {
      this.openAddData();
    }
  }

  @action
  openTool(tool: Tool) {
    this.currentTool = tool;
  }

  @action
  closeTool() {
    this.currentTool = undefined;
  }

  @action setPrintWindow(window: Window | null) {
    if (this.printWindow) {
      this.printWindow.close();
    }
    this.printWindow = window;
  }

  @action
  toggleMobileMenu() {
    this.setTopElement("mobileMenu");
    this.mobileMenuVisible = !this.mobileMenuVisible;
  }

  @action
  runStories() {
    this.storyBuilderShown = false;
    this.storyShown = true;

    setTimeout(function() {
      triggerResize();
    }, animationDuration || 1);

    this.terria.currentViewer.notifyRepaintRequired();

    this.terria.analytics?.logEvent(Category.story, StoryAction.runStory);
  }

  /**
   * ↓カスタム機能
   */

  //------------共通------------
  /**
   * viewermodeを3Dに切り替える
   */
  @action
  set3dMode() {
    const mainViewer = this.terria.mainViewer;
    this.terria.terrainSplitDirection = ImagerySplitDirection.NONE;
    setViewerMode(this.map3dModeKey, mainViewer);
    this.terria.setLocalProperty("viewermode", this.map3dModeKey);
    this.terria.currentViewer.notifyRepaintRequired();
  }

  /**
   * カスタムメッセージ表示/非表示
   * @param bool 
   */
  @action 
  setShowCustomMessage(bool:boolean){
    this.showCustomMessage = bool;
  }

  /**
   * カスタムメッセージをセットする
   * @param {string} タイトル
   * @param {string} 内容
   */
  @action
  setCustomMessage(title:string,content:string) {
    this.customMessageTitle = title;
    this.customMessageContent = content;
  }

  /**
   * カスタムメッセージ画面を閉じる
   */
  @action
  hideCustomMessageView() {
    this.customMessageTitle = "";
    this.customMessageContent = "";
    this.showCustomMessage = false;
    this.isApplyCompleted = false;
    this.isAnswerCompleted = false;
    this.isNotifiedCompleted = false;
    this.isReApply = false;

    // 申請者の権限である場合
    if(!this.terria.authorityJudgment()){
      
      if(this.showApplyInformationView){
        // 申請フォーム画面を非表示にする
        this.showApplyInformationView = false;
        this.showEnterApplicantInformation = false;
        this.showUploadApplicationInformation = false;
        this.showConfirmApplicationDetails = false;
        // 申請に関する情報をクリアして、初期画面へ戻す
        this.initInputApplication();
        this.changeShowLotNumberSelectedFlg(false);
        this.changeShowInitAndLotNumberSearchViewFlg(true);
        this.islotNumberSearch = true;
        this.showGeneralAndRoadJudgementResultView = false;
        // 再申請の場合、回答内容情報をクリアする
        this.answerContent = {};
        // 情報クリア
        this.reApplication = {};
        this.reApplicationFile = {};
        this.applicantnAddInformation = {};
        /**■ R6年度 改修（再申請完了の画面を閉じる時に、申請種類、申請段階の初期化）　*/
        this.selectedApplicationType = {};
        this.checkedApplicationStepId = 1;
        // 申請対象地番レイヤをクリア
        const items = this.terria.workbench.items;
        for (const aItem of items) {
            if (aItem.uniqueId === Config.layer.lotnumberSearchLayerNameForApplicationTarget) {
                this.terria.workbench.remove(aItem);
            }
        }
      }
    }
  }

  /**
   * 共通：ファイルダウンロードターゲットのセット
   */
  @action
  setFileDownloadTarget(applicationFileHistorys: Object[] , target: string){
    this.fileDownloadTarget = target;
    this.fileDownloadFile = applicationFileHistorys;
  }

  /**
   * 共通：ファイルプレビューモーダル表示
   */
  @action
  changeFilePreViewModalShow() {
    this.filePreViewModalShow = !this.filePreViewModalShow;
  }

  /**
   * 行政：サイドパネルのアクティブタブ変更
   */
  @action
  changeAdminTabActive(active: string) {
    this.adminTabActive = active;

    // 地番検索結果の行をクリックして、表示されたピンマークをクリアする
    if(active !=  "mapSearch" ){
      const items = this.terria.workbench.items;
      for (const aItem of items) {
          if (aItem.uniqueId === '対象地点') {
              this.terria.workbench.remove(aItem);
          }
      }
    }
  }

  /**
   * 行政：申請検索タブ内のアクティブページ変更
   */
  @action
  changeApplyPageActive(active: string) {
    this.applyPageActive = active;
    this.setIsSidePanelFullScreen(active === "applyDetail" || active === "answerRegister");
  }

  //------------共通：地番検索（SC001）------------
  /**
   * かな検索結果一覧を保持する
   * @param {any} かな検索結果一覧
   */
  @action
  setKanaDistrictNameSearch(KanaDistrictNameSearch:any){
    this.KanaDistrictNameSearch = KanaDistrictNameSearch;
  }

  /**
   * かな検索画面を閉じる
   */
  @action
  hideKanaDistrictNameSearchView() {
    this.showKanaDistrictNameSearch = false;
  }

  /**
   * 地番をセットする
   * @param {string} 地番
   */
  @action
  setLotNumber(lotNumber: string) {
    this.searchLotNum = lotNumber;
  }

  /**
   * 検索結果一覧を保持する
   * @param {any} 検索結果一覧
   */
  @action
  setSearchRsultContent(SearchRsultContent:any){
    this.SearchRsultContent = SearchRsultContent;
  }

  /**
   * 町丁名一覧結果を保持する
   * @param {any} 町丁名一覧結果
   */
  @action
  setDistrictNameSearch(DistrictNameSearch:any){
    this.DistrictNameSearch = DistrictNameSearch;
  }

  /**
   * 町丁名一覧画面を表示
   */
  @action
  showDistrictNameSearchView() {
    this.terria.analytics?.logEvent(Category.DistrictNameSearch, DistrictNameSearchAction.panelOpened);
    this.showDistrictNameSearch = true;
    this.setTopElement("DistrictNameSearch");
  }

  /**
   * かな検索画面を表示
   */
  @action
  showKanaDistrictNameSearchView() {
    this.terria.analytics?.logEvent(Category.KanaDistrictNameSearch, KanaDistrictNameSearchAction.panelOpened);
    this.showKanaDistrictNameSearch = true;
    this.setTopElement("KanaDistrictNameSearch");
  }

  /**
   * 地番検索画面を閉じる
   */
  @action
  hideLotNumberSearchView() {
    this.searchDistrictName = null;
    this.searchDistrictId = null;
    this.searchLotNum = null;
    this.showLotNumberSearch = false;
    this.showDistrictNameSearch = false;
    this.showKanaDistrictNameSearch = false;
  }

  /**
   * 町丁名一覧画面を閉じる
   */
  @action
  hideDistrictNameSearchView() {
    this.showDistrictNameSearch = false;
  }

  /**
   * 選択した町丁名をセットする
   * @param {string} 町丁ID
   * @param {string} 町丁名
   */
  @action
  setDistrictName(districtId: string, districtName:string) {
    this.searchDistrictId = districtId;
    this.searchDistrictName = districtName;
  }

  /**
   * 検索ボタン押下イベント
   */
  @action
  setIsClickEvent(){
    this.isClickEvent = true
  }

  //------------共通：レイヤ一覧表示画面（SC002）------------
  /**
   * レイヤドラッグの移動先index格納
   * @param index 移動先index
   */
  @action
  setCurrentDragItemIndex(index: number){
    this.currentDragItemIndex = index;
  }

  /**
   * groupをクローズする伴い、非表示となるレイヤリスト格納
   * @param items 非表示となるレイヤリスト
   */
  @action
  setHiddenItems(items:Object[]){
    this.hiddenItems = items;
  }

  /**
   * 現在レイヤ一覧に表示されたレイヤリスト
   * @param items 表示しているレイヤリスト
   */
  @action
  setDisplayItems(items:Object[]){
    this.displayItems = items;
  }

  /**
   * ログイン後、レイヤタブ画面を初回開く場合、カタログデータをバックアップする
   * @param groupItemList グループ対象リスト
   * @param displayingItemList レイヤリスト
   */
  @action
  catalogDataBackup(groupItemList:Object[],displayingItemList:Object[]){
    this.groupItemListBak = groupItemList;
    this.displayingItemListBak = displayingItemList;
  }


  //------------事業者：利用者規約・アンケート画面（SC101）------------
  /**
   * 利用者規約画面を閉じる
   */
  @action
  hideUserAgreementView(){
    this.showUserAgreement = false;
    this.showSidePanel = true;
  }

  //------------事業者：トップ画面（SC102）------------

  /**
   * 地番の選択状態(チェックボックス)を削除
   */
  @action
  removeLosNumberSelect() {
    this.selectLotSearchResult = [];
  }

  /**
   * 地番からの選択済み申請地の削除
   * @param {any} 削除対象の地番
   * @returns 
   */
  @action
  deleteApplicationPlaceByChiban(lotObj: any) {
    const index = this.applicationPlaceTemp.findIndex(obj => 
      obj.chibanId == lotObj.chibanId
    );
    if(index >= 0){
      this.applicationPlaceTemp.splice(index,1);
      this.applicationPlace = Object.assign({},this.applicationPlaceTemp);
      return true;
    }
    return false;
  }

  /**
   * 地番の選択状態(チェックボックス)を変更
   */
  @action 
  switchLotNumberSelect(lotObj: any) {
    const index = this.selectLotSearchResult.findIndex(obj => 
      obj.chibanId == lotObj.chibanId
    );
    (index != -1)? this.selectLotSearchResult.splice(index, 1): this.selectLotSearchResult.push(lotObj);
  }

  /**
   * 選択済みの申請地を変更
   */
  @action
  changeApplicationPlace() {
    this.selectLotSearchResult.forEach(element => {
      const index = this.applicationPlaceTemp.findIndex(obj => 
        obj.chibanId == element.chibanId
      );
      if(index < 0){
        this.applicationPlaceTemp.push(element);
      }
    })
    this.applicationPlace = Object.assign({},this.applicationPlaceTemp);
    
    if(this.showInitAndLotNumberSearchView && Object.keys(this.applicationPlace).length > 0){
      this.changeShowLotNumberSelectedFlg(true);
    }
  }

  /**
   * 地番検索有効・無効切り替え（画面に地番検索のラジオを切り替える伴い）
   * @param bool 
   */
  @action
  setIslotNumberSearch(bool: boolean) {
    this.islotNumberSearch = bool;
  }

  /**
   * 地番選択結果コンポーネント表示・非表示切り替え
   * @param bool 
   */
  @action
  changeShowLotNumberSelectedFlg(bool:boolean) {
    this.showLotNumberSelected = bool;
  }

  /**
   * 事業者：申請区分選択画面へ遷移
   */
  @action
  moveToInputApplyConditionView() {
    this.selectedApplicationType = {};
    this.checkedApplicationStepId = 1;
    this.showInitAndLotNumberSearchView = false;
    this.showInputApplyConditionView = true;
  }

  //------------事業者：トップ画面（SC102）-地番選択結果コンポーネント------------
  /**
  * 選択済み申請地の削除
  * @param {any} 削除対象
  */
  @action
  setFullFlag(applicationPlace: any) {
    // 全筆フラグ更新
    const index = this.applicationPlaceTemp.findIndex(obj => 
      obj.chibanId == applicationPlace.chibanId
    );
    const flag = this.applicationPlaceTemp[index].fullFlag;
    if(flag == "1") this.applicationPlaceTemp[index].fullFlag = "0";
    if(flag == "0") this.applicationPlaceTemp[index].fullFlag = "1";
    this.applicationPlace = Object.assign({},this.applicationPlaceTemp);
  }

  /**
  * 選択済み申請地の削除
  * @param {any} 削除対象
  */
  @action
  deleteApplicationPlace(applicationPlace: any) {
    // 申請地番選択結果一覧から削除
    const index = this.applicationPlaceTemp.findIndex(obj => 
      obj.chibanId == applicationPlace.chibanId
    );
    this.applicationPlaceTemp.splice(index,1);
    this.applicationPlace = Object.assign({},this.applicationPlaceTemp);
    
    // 地番検索結果チェックボックス選択状態一覧から削除
    const checkedIndex = this.selectLotSearchResult.findIndex(obj => 
      obj.chibanId == applicationPlace.chibanId
    );
    if(checkedIndex >= 0){
      this.selectLotSearchResult.splice(checkedIndex, 1);
    }
  }

  /**
   * 選択済み申請地を全て削除
  */
  @action
  deleteAllApplicationPlace() {
    this.applicationPlaceTemp= [];
    this.applicationPlace = [];
    this.selectLotSearchResult = [];
  }

  /**
   * トップ画面の初期状態に戻る
  */
  @action
  initApplicationLotNumberSelectionView() {
    this.initInputApplication();
    this.changeShowLotNumberSelectedFlg(false);
    this.changeShowInitAndLotNumberSearchViewFlg(true);
    this.setIslotNumberSearch(true);
    this.selectLotSearchResult = [];
    this.searchResult = [];
    this.isClickEvent = false;
  }

  //------------事業者：申請区分選択画面（SC104）------------

  /**
   * 事業者：申請区分選択画面からトップ画面に戻る
   */
  @action
  backFromInputApplyConditionView() {
    this.checkedApplicationCategory = [];
    this.checkedApplicationCategoryLocalSave = [];
    this.showInitAndLotNumberSearchView = true;
    this.showInputApplyConditionView = false;
    // 戻る際に、初期画面は地図検索（地番）を選択中にする
    this.islotNumberSearch=true;
  }

  /**
   * 事業者:概況診結果表示画面へ遷移
  */
  @action
  moveToGeneralAndRoadJudgementResultView(checkedApplicationCategory:Object,checkedApplicationCategoryLocalSave:Object,selectedApplicationType:Object) {
     this.showGeneralAndRoadJudgementResultView = true;
     this.showInputApplyConditionView = false;
     this.terria.setClickMode("");
     this.checkedApplicationCategory = checkedApplicationCategory;
     this.checkedApplicationCategoryLocalSave = checkedApplicationCategoryLocalSave;     
     this.selectedApplicationType = selectedApplicationType;
  }
  
  //------------事業者：概況診断結果表示画面（SC105）------------

  /**
   * 概況診断レポートの一時フォルダ名をセットする
   * @param {string} 一時フォルダ名
   */
  @action
  setFolderName(folderName:string){
    this.folderName = folderName;
  }

  /**
   * 概況診断レポートの一時ファイル名をセットする
   * @param {string} 一時ファイル名
   */
  @action
  setFileName(fileName:string){
    this.fileName = fileName;
  }

  /**
   * 申請登録後の申請IDをセットする
   * @param {number} 申請ID
   */
  @action
  setApplicationId(applicationId:number){
    this.applicationId = applicationId;
  }

  /**
   * 事業者用：概況診断結果表示画面からトップ画面に戻る
   */
  @action
  backFromGeneralAndRoadJudgementResultView() {
    if(this.isReApply){
      // 【R6】改修：再申請の場合、概要診断結果一覧も表示可能になる
      // 申請区分選択画面へ戻る
      this.showInputApplyConditionView = true;
      this.generalConditionDiagnosisResult = {};
    }else{
      this.initInputApplication();
      this.changeShowLotNumberSelectedFlg(false);
      this.changeShowInitAndLotNumberSearchViewFlg(true);
      this.islotNumberSearch = true;
    }

    this.showGeneralAndRoadJudgementResultView = false;
    // 申請対象地番レイヤをクリア
    const items = this.terria.workbench.items;
    for (const aItem of items) {
        if (aItem.uniqueId === Config.layer.lotnumberSearchLayerNameForApplicationTarget) {
            this.terria.workbench.remove(aItem);
        }
    }
  }

  /**
   * 事業者用:申請フォーム画面へ遷移
   * @param generalConditionDiagnosisResult 概況診断結果 
   */
  @action
  moveToApplyInformationView(generalConditionDiagnosisResult:Object){
    this.generalConditionDiagnosisResult = generalConditionDiagnosisResult;
    this.showGeneralAndRoadJudgementResultView = false;
    this.showApplyInformationView = true;
    this.showEnterApplicantInformation = true;
    this.showUploadApplicationInformation = false;
    this.showConfirmApplicationDetails = false;
  }

  /**
   * 事業者用初期画面(地番検索できる)の表示・非表示を切り替え
   * @param bool 
   */
  @action
  changeShowInitAndLotNumberSearchViewFlg(bool:boolean ) {
    this.showInitAndLotNumberSearchView = bool;
  }

  /**
   * 概況診断レポート一覧画面切り替え
   * @param bool 
   */
  @action
  setGeneralConditionDiagnosisrReportShow(bool:boolean ) {
    this.generalConditionDiagnosisrReportShow = bool;
  }

  //------------事業者：申請者情報入力画面（SC108）------------
  /**
   * 申請対象ファイルアップロード画面画面に遷移
   * @param {Object} 申請者情報
   */
  @action
  moveToUploadApplicationInformationView(applicantInformation:Object){
    if(this.isReApply){
      this.applicantnAddInformation = applicantInformation;
    }else{
      this.applicantInformation = applicantInformation;
    }
    this.showEnterApplicantInformation = false;
    this.terria.analytics?.logEvent(Category.UploadApplicationInformation, UploadApplicationInformation.panelOpened);
    this.showUploadApplicationInformation = true;
    this.setTopElement("UploadApplicationInformation");
  }

  /**
   * 概況診断結果表示画面に戻る
   */
  @action
  backToGeneralConditionDiagnosisView() {
    this.showEnterApplicantInformation = false;
    this.terria.analytics?.logEvent(Category.GeneralConditionDiagnosis, GeneralConditionDiagnosis.panelOpened);
    this.showGeneralAndRoadJudgementResultView = true;
    this.applicantInformation = [];
    this.applicationFile = {};
    //■R6 再申請用申請ファイルと申請追加情報のクリア
    this.applicantnAddInformation = [];
    this.reApplicationFile = {};
    this.showApplyInformationView = false;
    this.showEnterApplicantInformation = false;
    this.showUploadApplicationInformation = false;
    this.showConfirmApplicationDetails = false;
  }

  //------------事業者：申請対象ファイルアップロード画面（SC109）------------
  /**
   * 申請内容確認画面へ遷移
   * @param {Object} 申請ファイル一覧
   */
  @action
  moveToConfirmApplicationDetailsView(applicationFile:Object){
    if(this.isReApply){
      this.reApplicationFile = applicationFile;
    }else{
      this.applicationFile = applicationFile;
    }
    this.showUploadApplicationInformation = false;
    this.terria.analytics?.logEvent(Category.ConfirmApplicationDetails, ConfirmApplicationDetails.panelOpened);
    this.showConfirmApplicationDetails = true;
    // this.setTopElement("ConfirmApplicationDetails");
  }

  /**
   * 申請者情報入力画面に戻る
   * @param {Object} 申請ファイル一覧
   */
  @action
  backToEnterApplicantInformationView(applicationFile:Object){
    if(this.isReApply){
      this.reApplicationFile = applicationFile
    }else{
      this.applicationFile = applicationFile;
    }
    this.showUploadApplicationInformation = false;
    this.terria.analytics?.logEvent(Category.EnterApplicantInformation, EnterApplicantInformation.panelOpened);
    // ■R6年度　改修 (再申請の場合でも、申請フォームを表示)
    // if(Object.keys(this.answerContent).length > 0){
    //   this.showApplyInformationView = false;
    //   this.showConfirmAnswerInformationView = true;
    //   this.isReApply = false;
    //   this.setIsSidePanelFullScreen(true);
    // }else{
      this.showEnterApplicantInformation = true;
    // }
    
    // this.setTopElement("EnterApplicantInformation");
  }

  //------------事業者：申請内容確認画面（SC110）------------
  /**
   * 申請完了画面へ遷移
   * @param {string} ログインID
   * @param {string} パスワード
   */
  @action
  showApplicationCompletedView(loginId:string,password:string,anwserDays:string){
    // this.showConfirmApplicationDetails = false;
    this.customMessageContent = this.customMessageContent.replace("${回答日数}",anwserDays)
    this.customMessageContent = "ログインID："+loginId+"<br/>パスワード："+password+"<br/>" + this.customMessageContent;
    // this.initInputApplication();
    this.terria.analytics?.logEvent(Category.CustomMessage, CustomMessage.panelOpened);
    this.showCustomMessage = true;
    this.isApplyCompleted = true;
    this.setTopElement("CustomMessage");
  }

  /**
   * 申請ファイル登録画面へ戻る
   */
  @action
  backToUploadApplicationInformationView(){
    this.showConfirmApplicationDetails = false;
    // this.terria.analytics?.logEvent(Category.UploadApplicationInformation, UploadApplicationInformation.panelOpened);
    this.showUploadApplicationInformation = true;
    // this.setTopElement("UploadApplicationInformation");
  }

  //------------事業者：申請ID/パスワード認証画面（SC113）------------
  /**
   * 事業者:回答確認画面へ遷移
   * @param answerContent 
   */
  @action
  moveToConfirmAnswerInformationView(answerContent:Object ) {
    if(this.isReApply){
      this.reApplication = {};
      this.reapplyApplicationStepId = 1;
      this.preReapplyApplicationStepId = 1;
      // 申請追加情報の入力画面を非表示
      this.showEnterApplicantInformation = false;
      //申請区分選択画面を非表示
      this.showInputApplyConditionView = false;
      this.isReApply = false;
      // 情報クリア
      this.reApplication = {};
      this.reApplicationFile = {};
      this.applicantnAddInformation = {};
      this.initInputApplication();
    }else{
      this.answerContent = answerContent;
    }
    this.showConfirmAnswerInformationView = true;
    this.setIsSidePanelFullScreen(true);
    this.changeShowInitAndLotNumberSearchViewFlg(false);
  }

  //------------事業者：回答内容確認画面（SC114） ------------
  /**
   * 再申請情報保存
   * @param {Object} 再申請情報
   * @param {Object} 再申請ファイル一覧
   */
  @action
  setReAppInformation(reApplication:Object){
    // 再申請情報
    this.reApplication = reApplication;
    //------------■R6年度　改修------------
    // // 再申請ファイル
    // this.reApplicationFile = reApplicationFile;
  }
  
  /**
   * 事業者:申請対象ファイルアップロード画面へ遷移(再申請)
   * @param applicantInformation 申請者情報
   * @param applicationFile 再申請要の回答に対する申請対象ファイル
   * @param checkedApplicationCategory 選択された申請区分
   * @param lotNumber 選択された地番
   */
  @action
  moveToApplyInformationViewForReapply(applicantInformation:Object, applicationFile:Object, checkedApplicationCategory:Object, lotNumber:any){
    this.applicantInformation = applicantInformation;
    this.applicationFile = applicationFile;
    this.checkedApplicationCategory  = checkedApplicationCategory;
    this.applicationPlace = Object.assign({},lotNumber);
    this.showConfirmAnswerInformationView = false;
    this.showApplyInformationView = true;
    this.showEnterApplicantInformation = false;
    this.showUploadApplicationInformation = true;
    this.showConfirmApplicationDetails = false;
    this.isReApply = true;
    this.setIsSidePanelFullScreen(false);
  }

  /**
   * 事業者：回答確認画面からトップ画面に戻る
  */
  @action
  backFromConfirmAnswerInformationView() {
    this.initInputApplication();
    /**■ R6年度 改修（申請種類、申請段階の初期化）　*/
    this.selectedApplicationType = {};
    this.checkedApplicationStepId = 1;
    this.changeShowLotNumberSelectedFlg(false);
    this.changeShowInitAndLotNumberSearchViewFlg(true);
    this.showConfirmAnswerInformationView = false;
    this.answerContent = {};
    this.setIsSidePanelFullScreen(false);
  }

  /**
   * 事業者:問合せ画面へ遷移
   * @param answerId 問合せを行う回答ID
   */
  /*
  @action
  moveToChatView(answerId: string){
    this.currentAnswerId = answerId;
    this.showConfirmAnswerInformationView = false;
    this.setIsSidePanelFullScreen(false);
    this.showChatView = true;
  }
  */
  /**
   * 問合せ画面へ遷移
   * @param applicationId 申請ID
   * @param applicationStepId 申請段階ID
   * @param departmentAnswerId 部署回答ID
   * @param answerId 回答ID
   */
    @action
    moveToChatView(applicationId:number, applicationStepId:number, departmentAnswerId:number, answerId:number){
      this.applicationId = applicationId;
      this.checkedApplicationStepId = applicationStepId;
      this.currentDepartmentAnswerId = departmentAnswerId;
      this.currentAnswerId = answerId;
      // 行政の場合
      if(this.terria.authorityJudgment()){
        this.currentChat = {};
        this.backToPage = {"tab":this.adminTabActive,"page":this.applyPageActive}
        this.applyPageActive = "chat";
        this.setIsSidePanelFullScreen(false);
      // 事業者の場合
      }else{
        this.showConfirmAnswerInformationView = false;
        this.setIsSidePanelFullScreen(false);
        this.showChatView = true;
      }
    }

  /**
   * 選択された回答内容レコードの回答IDを格納
   * @param answerId 回答ID
   */
  @action
  setCurrentAnswerId(answerId: string){
    this.currentAnswerId = answerId;
  }

  //------------■R6年度　追加------------
  /**
   * 再申請のために、申請段階IDを設定
   * @param reapplyApplicationStepId 処理中申請段階ID
   * @param preReapplyApplicationStepId 前回の申請段階ID
   */
  @action
  setReapplyApplicationStepId(reapplyApplicationStepId: number, preReapplyApplicationStepId: number){
    this.reapplyApplicationStepId = reapplyApplicationStepId;
    this.preReapplyApplicationStepId = preReapplyApplicationStepId;
  }

  /**
   * 再申請のために、申請区分選択画面へ遷移
   */
   @action
   moveToInputApplyConditionViewForReapply(){
    this.showConfirmAnswerInformationView = false;
    this.showApplyInformationView = false;
    this.showEnterApplicantInformation = false;
    this.showUploadApplicationInformation = false;
    this.showConfirmApplicationDetails = false;
    this.showInputApplyConditionView = true; 
    this.isReApply = true;
    this.setIsSidePanelFullScreen(false);
   }

  /**
   * 再申請のために、再申請画面へ遷移
   */
  @action
  moveToEnterAddInformation(){
    this.showConfirmAnswerInformationView = false;
    // 下記三つを設定した、再申請画面が表示
    //申請フォーム
    this.showApplyInformationView = true;
    //申請者情報
    this.showEnterApplicantInformation = true;
    // 再申請
    this.isReApply = true;
    this.showUploadApplicationInformation = false;
    this.showConfirmApplicationDetails = false;
    this.setIsSidePanelFullScreen(false);
  }

  //------------事業者：問合せ画面（SC116）------------
  /**
   * 事業者:問合せ画面から回答確認画面へ戻る
   * @param answer 
   */
  @action
  backFromChatView(){
    this.showConfirmAnswerInformationView = true;
    this.setIsSidePanelFullScreen(true);
    this.showChatView = false;
    this.currentAnswerId = "";
  }

  //------------事業者：問合せファイル選択ダイアログ画面（SC117）------------
  /**
   * 事業者:アップロードする問い合わせ添付ファイルを格納
   * @param inquiryFiles 問合せ添付ファイル
   */
  @action
  changeInquiryFiles(inquiryFiles: object[]) {
    this.inquiryFiles = inquiryFiles;
  }

  /**
   * 事業者:問い合わせ添付ファイルのアップロードモーダル表示・非表示
   */
  @action
  changeInquiryFileUploadModalShow() {
    this.inquiryFileUploadModalShow = !this.inquiryFileUploadModalShow;
  }

  //------------行政・事業者：申請ファイル選択ダイアログ画面（SC206）------------
  /**
   * ファイルダウンロードモーダル表示
   */
  @action
  changeFileDownloadModalShow() {
    this.fileDownloadModalShow = !this.fileDownloadModalShow;
  }

  //------------行政：トップ画面（SC202）------------
  /**
   * 行政：地図検索タブの問合せタブ表示有無
   * @param bool 
   */
  @action
  changeShowInquiryList(bool: boolean){
    this.showInquiryList = bool;
  }
  
  /**
   * 行政：地図検索タブの回答タブ表示有無
   * @param bool 
   */
  @action
  changeShowAnswerList(bool: boolean){
    this.showAnswerList = bool;
  }

  /**
   * 行政:チャット画面へ遷移のために、チャット情報を格納
   * @param chatObj 選択しているチャット情報
   * @param tab 選択中タブ（地図検索・申請情報検索・レイヤ）
   * @param page 遷移元画面
   */
  @action
  moveToAdminChatView(chatObj:Object, tab:String, page:String){
    this.currentChat = chatObj;
    this.backToPage = {"tab":tab,"page":page};
  }

  /**
   * 行政：戻るボタンの遷移先設定
   * @param tab 遷移先画面に選択中タブ（地図検索・申請情報検索・レイヤ）
   * @param page 遷移先画面
   */
  @action
  setAdminBackPage(tab : string, page: string) {
    this.adminBackTab = tab;
    this.adminBackPage = page;
  }

  //------------行政：申請情報検索------------
  /**
   * 申請情報検索画面を閉じる
   */
  @action
  hideApplicationInformationSearchView() {
    this.showApplicationInformationSearch = false;
  }

  //------------ 行政：申請情報詳細画面（SC205）------------

  /**
   * 申請情報詳細画面へ遷移
   * @param {string} 申請ID
   */
  @action
  nextApplicationDetailsView(applicationInformationSearchForApplicationId:string){
    this.applicationInformationSearchForApplicationId = applicationInformationSearchForApplicationId;
    this.terria.analytics?.logEvent(Category.ApplicationDetails, ApplicationDetails.panelOpened);
    this.changeAdminTabActive("applySearch");
    this.changeApplyPageActive("applyDetail");
    this.setAdminBackPage("applySearch", "applyList");
  }

  /**
   * 選択中の地番情報を保持する
   * @param {object} 地番情報一覧
   */
  @action
  setLotNumbers(lotNumbers:object){
    this.lotNumbers = lotNumbers;
  }
  
  //------------行政：回答登録画面（SC207） ------------

  /**
   * 回答入力画面へ遷移
   */
  @action
  nextAnswerInputView( applicationId:string, applyAnswerForm:Object, checkedApplicationStepId:number = 1){
    this.terria.analytics?.logEvent(Category.AnswerInput, AnswerInput.panelOpened);
    this.showAnswerInput = true;
    // this.setTopElement("AnswerInput");
    this.checkedApplicationStepId = checkedApplicationStepId;
    this.applyAnswerForm = applyAnswerForm;
    this.applicationInformationSearchForApplicationId = applicationId;
    // this.selectApplicationClassModalShow = false;
    this.changeApplyPageActive("answerRegister");
  }

  /**
   * 行政：引用ファイルターゲットのセット
   * @param target 
   */
  @action
  setQuoteFiles(target: Object) {
    this.fileQuoteFile = target;
  }

  /**
   * 行政：引用ファイル(登録済み回答ファイル)のセット
   * @param answerFile 
   */
  @action
  setAnswerQuoteFiles(answerFile: Object) {
    this.answerQuoteFile = answerFile;
  }

  /**
   * 行政：発行様式アップロード対象のセット
   * @param issuanceLedgerForm 
   */
  @action
  setIssuanceLedgerForm(issuanceLedgerForm: Object) {
    this.issuanceLedgerForm = issuanceLedgerForm;
  }
    
  /**
   * 行政：PDFファイルビュワーの表示
   */
  @action
  setShowPdfViewer(bool: boolean){
    this.showPdfViewer = bool;
  }

  /**
   * 回答入力画面を閉じる
   */
  @action
  hideAnswerInputView() {
    this.showAnswerInput = false;
  }

  /**
   * コールバック関数を設定
   */
  @action
  setCallBackFunction(callBackFunction:Function){
    this.callBackFunction = callBackFunction;
  }
  
  //------------回答テンプレート選択ダイアログ画面（SC208） ------------
  /**
   * 行政：回答入力モーダル表示
   */
  @action
  changeAnswerContentInputModalShow() {
    this.inputAnswerContentModalShow  = !this.inputAnswerContentModalShow;
  }

  /**
   * 行政：回答テンプレートターゲットのセット
   * @param target 
   */
  @action
  setAnswerTemplateTarget(target: Object){
    this.answerTemplateTarget = target;
  }
  //------------回答ファイル選択ダイアログ画面（SC209） ------------
  /**
   * 行政：ファイルアップロードモーダル表示
   */
  @action
  changeFileUploadModalShow() {
    this.fileUploadModalShow = !this.fileUploadModalShow;
  }

  /**
   * 行政：発行様式アップロードモーダル表示
   */
  @action
  changeIssuanceFileUploadModalShow() {
    this.issuanceFileUploadModalShow = !this.issuanceFileUploadModalShow;
  }

  //------------回答登録完了（SC211）------------

  /**
   * 回答登録完了ダイアログ画面を開く
   */
  @action
  nextAnswerCompleteView(){
    this.isAnswerCompleted = true;
    this.showAnswerInput = false;
    this.initInputApplication();
    this.terria.analytics?.logEvent(Category.CustomMessage, CustomMessage.panelOpened);
    this.showCustomMessage = true;
    this.setTopElement("CustomMessage");
  }

  //------------回答通知完了（SC212）------------
  /**
   * 回答通知完了ダイアログ画面を開く
   */
  @action
  nextAnswerNotificationView(){
    this.showApplicationDetails = false;
    this.isNotifiedCompleted = true;
    this.initInputApplication();
    this.terria.analytics?.logEvent(Category.CustomMessage, CustomMessage.panelOpened);
    this.showCustomMessage = true;
    this.setTopElement("CustomMessage");
  }

  //------------行政：宛先選択ダイアログ画面（SC214）------------
  /**
   *  行政：チャット宛先入力モーダル表示
   */
  @action
  changeInputChatAddressModalShow() {
    this.inputChatAddressModalShow = !this.inputChatAddressModalShow;
  }

  /**
   * 行政：チャット宛先クリア
   */
  @action
  removeAllInputChatAddress() {
    this.inputChatAddress = [];
  }

  /**
   * 行政：チャット宛先入力
   */
  @action
  setInputChatAddress(address: any) {
    const index = this.inputChatAddress.findIndex(obj => 
      obj.departmentId == address.departmentId
    );
    (index != -1)? this.inputChatAddress.splice(index, 1): this.inputChatAddress.push(address);
  }

  /**■■ R6年度 新規追加画面　*/
  //------------行政：回答申請種別選択ダイアログ画面（SC215）------------
  
  /**
   * 回答申請種別選択モダールを開く
   */
  @action
  showSelectApplicationClassModal( applicationId:string, applyAnswerForm:Object, checkedApplicationStepId:number = 1){
    this.checkedApplicationStepId = checkedApplicationStepId;
    this.applyAnswerForm = applyAnswerForm;
    this.applicationInformationSearchForApplicationId = applicationId;
    this.selectApplicationClassModalShow = true;
  }

  /**
   * 回答申請種別選択モダールを閉じる
   */
  @action
  closeSelectApplicationClassModal(checkedApplicationStepId:number = 1){
    this.checkedApplicationStepId = checkedApplicationStepId;
    this.selectApplicationClassModalShow = false;
  }

  //------------行政：回答通知確認ダイアログ画面（SC216）------------

  /**
   * 回答通知確認モダールを開く
   */
  @action
  showConfirmAnswerNotificationModal(applyAnswerForm:Object, checkedApplicationStepId:number = 1){
    this.applyAnswerForm = applyAnswerForm;
    this.checkedApplicationStepId = checkedApplicationStepId;
    this.confirmAnswerNotificationModalShow = true;
  }

  /**
   * 回答通知確認モダールを閉じる
   */
    @action
    closeConfirmAnswerNotificationModal(checkedApplicationStepId:number = 1){
      this.checkedApplicationStepId = checkedApplicationStepId;
      this.confirmAnswerNotificationModalShow = false;
    }

  //-----------画面戻る時に、入力された情報をクリアする------------
  @action
  initInputApplication(){
    this.applicantInformation = [];
    this.applicationFile = {};
    this.checkedApplicationCategory = [];
    this.checkedApplicationCategoryLocalSave = [];
    this.applicationPlace = [];
    this.applicationPlaceTemp = [];
    this.searchDistrictName = null;
    this.searchDistrictId = null;
    this.searchLotNum = null;
    this.generalConditionDiagnosisResult = {};
    // this.isReApply = false;
    if(!this.terria.authorityJudgment()){
      this.terria.setClickMode("");
    }
    this.setLotNumber("");
  }

  //-----------行政：申請一覧画面（地図上に、地番クリック）------------
  /**
   * 行政：申請地番表示パネルを表示
   * @param list 
   */
  @action
  showApplicationLotNumberPanelView(list: object[]){
    this.applicationLotNumberList = list;
    this.showApplicationLotNumberPanel = true;
  }

  /**
   * 行政：申請地番表示パネルを閉じる
   */
  @action
  closeApplicationLotNumberPanelView(){
    this.showApplicationLotNumberPanel = false;
  }

  /**
   * 行政：回答テンプレート選択モーダル表示
   *　※仕様変更と伴い、回答入力モーダルを使うため、廃止します
  */
  @action
  changeAnswerTemplateModalShow() {
    this.inputAnswerTemplateModalShow = !this.inputAnswerTemplateModalShow;
  }

  /**
   * ↓↓↓既存関数
   */

  //------------UI013地番検索------------
  /**
   * 地番検索画面を表示
   */
  @action
  showLotNumberSearchView() {
    this.terria.analytics?.logEvent(Category.LotNumberSearch, LotSearchAction.panelOpened);
    this.showLotNumberSearch = true;
    //this.islotNumberSearchViewIndependent = false;
    this.lotNumberSearchPosDiffTop = 0;
    this.lotNumberSearchPosDiffLeft = 0;
    this.selectLotSearchResult = [];
    this.islotNumberSearchViewIndependent = true;
    this.setTopElement("LotNumberSearch");
  }

  /**
   * 行政：地番に紐づく申請一覧を格納
   */
  @action
  setApplicationLotNumberList(list: object[]){
    this.applicationLotNumberList = list;
  }

  /**
   * 行政：ファイル編集ターゲットの設定
   */
  @action
  setFileEditTarget(target: string){
    this.fileEditTarget = target;
  }

  /**
   * 地番選択結果画面を表示
   */
  @action
  changeLotNumberSearchTypeFlg() {
    // this.terria.analytics?.logEvent(Category.LotNumberSearch, LotSearchAction.panelOpened);
    this.showLotNumberSelected = true;
  }

  /**
   * 町丁名をセットする
   * @param {string} 町丁名
   */
  @action
  setSearchDistrictName(districtName:string) {
    this.searchDistrictName = districtName;
  }

  //------------共通------------
  /**
   * 申請地レイヤーを表示する
   */
  @action
  showApplicationAreaLayer(){
    this.terria.setClickMode("2");
    if(this.terria.authorityJudgment()){
      const item = new webMapServiceCatalogItem(Config.layer.lotnumberSearchLayerNameForApplying, this.terria);
      const wmsUrl = Config.config.geoserverUrl;
      const items = this.terria.workbench.items;
      for (const aItem of items) {
          if (aItem.uniqueId === Config.layer.lotnumberSearchLayerNameForApplying) {
              this.terria.workbench.remove(aItem);
          }
      }
      item.setTrait(CommonStrata.definition, "url", wmsUrl);
      item.setTrait(CommonStrata.user, "name", Config.layer.lotnumberSearchLayerDisplayNameForApplying);
      item.setTrait(
          CommonStrata.user,
          "layers",
          Config.layer.lotnumberSearchLayerNameForApplying);
      item.setTrait(CommonStrata.user,
          "parameters",
          {});
      item.loadMapItems();
      this.terria.workbench.add(item); 

    }
  }

  /**
   * 申請情報表示地番レイヤーを表示・非表示する
   */
  @action
  showApplicationSearchTargetLayer( isShow : boolean, applicationId:number){
    if(this.terria.authorityJudgment()){
      const wmsUrl = Config.config.geoserverUrl;
      const items = this.terria.workbench.items;
      for (const aItem of items) {
          if (aItem.uniqueId === Config.layer.lotnumberSearchLayerNameForApplicationSearchTarget) {
              this.terria.workbench.remove(aItem);
          }
      }
      if(isShow){
        const item = new webMapServiceCatalogItem(Config.layer.lotnumberSearchLayerNameForApplicationSearchTarget, this.terria);
        item.setTrait(CommonStrata.definition, "url", wmsUrl);
        item.setTrait(CommonStrata.user, "name", Config.layer.lotnumberSearchLayerDisplayNameForApplicationSearchTarget);
        item.setTrait(
            CommonStrata.user,
            "layers",
            Config.layer.lotnumberSearchLayerNameForApplicationSearchTarget);
        item.setTrait(CommonStrata.user,
            "parameters",
            {
              "viewparams": Config.layer.lotnumberSearchViewParamNameForApplicationSearchTarget + applicationId,
            });
            item.loadMapItems();
            this.terria.workbench.add(item); 
      }
    }
  }

  //------------UI001利用者規約------------
  /**
   * 利用者規約画面を表示
   */
  @action
  showUserAgreementView(){
    if(!this.terria.authorityJudgment()){
      this.terria.analytics?.logEvent(Category.UserAgreement, UserAgreement.panelOpened);
      this.showUserAgreement = true;
      this.setTopElement("UserAgreement");
    }
  }

  //------------UI003申請区分選択------------

  /**
   * 申請区分選択画面を表示
   */
  @action
  showApplicationCategorySelectionView() {
    this.terria.analytics?.logEvent(Category.ApplicationCategorySelection, ApplicationCategorySelection.panelOpened);
    this.showApplicationCategorySelection = true;
    // this.setTopElement("ApplicationCategorySelection");
  }

  /**
   * 申請区分選択画面を閉じる
   */
  @action
  hideApplicationCategorySelectionView() {
    this.initInputApplication();
    this.showApplicationCategorySelection = false;
    this.showLotNumberSearch = true;
    this.showInitAndLotNumberSearchView = true;
  }

  /**
   * 申請区分選択画面へ戻る
   */
  @action
  backApplicationCategorySelectionView() {
    this.showCharacterSelection = false;
    this.showMapSelection = false;
    this.terria.setClickMode("");
    this.terria.analytics?.logEvent(Category.ApplicationCategorySelection, ApplicationCategorySelection.panelOpened);
    this.showApplicationCategorySelection = true;
    this.setTopElement("ApplicationCategorySelection");
  }

  //------------UI004 申請地 文字選択------------

  /**
   * 申請地選択画面へ遷移(初期状態は文字選択画面)
   * @param {Object} 選択済みの申請区分
   * @param {Object} 選択済みの申請区分ローカル保持用
   */
  @action
  nextCharacterSelectionView(checkedApplicationCategory:Object,checkedApplicationCategoryLocalSave:Object){
    this.lotNumberSearchPosDiffTop = -10;
    this.lotNumberSearchPosDiffLeft = 13;
    this.islotNumberSearchViewIndependent = false;
    this.showApplicationCategorySelection = false;
    this.checkedApplicationCategory = checkedApplicationCategory;
    this.checkedApplicationCategoryLocalSave = checkedApplicationCategoryLocalSave;
    this.terria.analytics?.logEvent(Category.CharacterSelection, CharacterSelection.panelOpened);
    this.showCharacterSelectionView();
    this.showMapSelectionView();
  }

  /**
   * 文字選択画面を表示
   */
  @action
  showCharacterSelectionView(){
    this.terria.analytics?.logEvent(Category.CharacterSelection, CharacterSelection.panelOpened);
    this.selectLotSearchResult = [];
    this.terria.setClickMode("");
    this.showCharacterSelection = true;
    this.searchDistrictId = null;
    this.searchDistrictName = null;
    this.setTopElement("CharacterSelection");
  }

  /**
   * 文字選択画面を閉じる
   */
  @action
  hideCharacterSelectionView() {
    this.selectLotSearchResult = [];
    this.terria.setClickMode("1");
    this.showCharacterSelection = false;
    this.searchDistrictId = null;
    this.searchDistrictName = null;
  }

  //------------UI004 申請地 地図選択------------

  /**
   * 地図選択画面を表示
   */
  @action
  showMapSelectionView(){
    this.terria.analytics?.logEvent(Category.MapSelection, MapSelection.panelOpened);
    this.showMapSelection = true;
    this.setTopElement("MapSelection");
  }

  /**
   * 地図選択画面を閉じる
   */
  @action
  hideMapSelectionView() {
    this.showMapSelection = false;
    if(!this.terria.authorityJudgment()){
      this.terria.setClickMode("");
    }
  }

  //------------UI005概況診断------------

  /**
   * 概況診断画面に遷移
   */
  @action
  nextGeneralConditionDiagnosisView() {
    this.showCharacterSelection = false;
    this.showMapSelection = false;
    this.terria.setClickMode("");
    this.terria.analytics?.logEvent(Category.GeneralConditionDiagnosis, GeneralConditionDiagnosis.panelOpened);
    this.showGeneralConditionDiagnosis = true;
    this.setTopElement("GeneralConditionDiagnosis");
  }

  /**
   * 概況診断画面を閉じる
   */
  @action
  hideGeneralConditionDiagnosisView() {
    this.initInputApplication();
    this.showGeneralConditionDiagnosis = false;
  }

  //------------UI007申請者情報------------

  /**
   * 申請者情報画面に遷移
   * @param {Object} 概況診断結果
   */
  @action
  nextEnterApplicantInformationView(generalConditionDiagnosisResult:Object){
    this.generalConditionDiagnosisResult = generalConditionDiagnosisResult;
    this.showGeneralConditionDiagnosis = false;
    this.terria.analytics?.logEvent(Category.EnterApplicantInformation, EnterApplicantInformation.panelOpened);
    this.showEnterApplicantInformation = true;
    // this.setTopElement("EnterApplicantInformation");
  }

  /**
   * 申請者情報画面を閉じる
   */
  @action
  hideEnterApplicantInformationView() {
    this.initInputApplication();
    this.showEnterApplicantInformation = false;
  }
  
  //------------UI008 申請ファイル登録------------

  /**
   * 申請ファイル登録画面を閉じる
   */
  @action
  hideUploadApplicationInformationView() {
    this.initInputApplication();
    this.showUploadApplicationInformation = false;
  }

  //------------UI009申請内容確認------------

  /**
   * 申請内容確認画面を閉じる
   */
  @action
  hideConfirmApplicationDetailsView() {
    this.initInputApplication();
    this.showConfirmApplicationDetails = false;
  }

  //------------UI011申請ID/パスワード照合------------

  /**
   * 申請ID/パスワード照合画面を表示
   */
  @action
  showAnswerLoginView(){
    this.terria.analytics?.logEvent(Category.AnswerLogin, AnswerLogin.panelOpened);
    this.showAnswerLogin = true;
    this.setTopElement("AnswerLogin");
  }

  /**
   * 申請ID/パスワード照合画面を閉じる
   */
  @action
  hideAnswerLoginView() {
    this.showAnswerLogin = false;
  }

  //------------UI012申請内容確認------------

  /**
   * 申請内容確認画面を表示
   * @param {Object} 申請の回答内容
   */
  @action
  showAnswerContentView(answerContent:Object){
    this.answerContent = answerContent;
    this.showAnswerLogin = false;
    this.terria.analytics?.logEvent(Category.AnswerContent, AnswerContent.panelOpened);
    this.showAnswerContent = true;
    this.setTopElement("AnswerContent");
  }

  /**
   * 申請内容確認画面を閉じる
   */
  @action
  hideAnswerContentView() {
    this.answerContent = {};
    this.showAnswerContent = false;
  }

  /**
   * 回答内容を保存
   * @param {Object} 申請の回答内容
   */
  @action
  setAnswerContent(answerContent:Object){
    this.answerContent = answerContent;
  }

  /**
   * 申請段階IDを保存
   * @param {checkedApplicationStepId} 申請段階ID
   */
  @action
  setCheckedApplicationStepId(checkedApplicationStepId:number){
    this.checkedApplicationStepId = checkedApplicationStepId;
  }

  //------------UI104申請情報検索------------

  /**
   * 申請情報検索画面を表示
   */
  @action
  showApplicationInformationSearchView(){
    this.terria.analytics?.logEvent(Category.ApplicationInformationSearch, ApplicationInformationSearch.panelOpened);
    this.showApplicationInformationSearch = true;
    this.setTopElement("ApplicationInformationSearch");
  }

  //------------UI106申請情報詳細------------
  /**
   * 申請情報詳細画面を閉じる
   */
  @action
  hideApplicationDetailsView() {
    this.showApplicationDetails = false;
  }

  //------------サブ画面申請一覧------------

  /**
   * 申請一覧のサブ画面を表示
   * @param {Object} 申請一覧
   */
  @action
  showApplicationListView(applicationList:Object){
    this.applicationList = applicationList;
    this.terria.analytics?.logEvent(Category.ApplicationList, ApplicationList.panelOpened);
    this.showApplicationList = true;
    this.setTopElement("ApplicationList");
  }

  /**
   * 申請一覧のサブ画面を閉じる
   */
  @action
  hideApplicationListView() {
    this.showApplicationList = false;
  }

  /**
   * 申請情報詳細リフレッシュ処理セット
   * @param refreshConfirmApplicationDetails 
   */
  @action
  setRefreshConfirmApplicationDetails(refreshConfirmApplicationDetails:Function){
    this.refreshConfirmApplicationDetails = refreshConfirmApplicationDetails;
  }

  //------------拡大/縮小MAP------------
  /**
   * サイドパネルフルスクリーン
   * @param isSidePanelFullScreen 
   */
  @action
  setIsSidePanelFullScreen(isSidePanelFullScreen:boolean){
    this.isSidePanelFullScreen = isSidePanelFullScreen;
    if(!isSidePanelFullScreen){
      runInAction(() => {
        this.setMapExpansionFlag(false);
        this.setMapBottom("");
        this.setMapRight("2vh");
        this.setMapTop("68vh");
        this.setMapLeft("");
        this.setMapWidth("21vw");
        this.setMapHeight("23vh");
      });
    }
    this.triggerResizeEvent();
  }

  /**
   * Map 拡大状態
   * @param mapExpansionFlag 
   */
  @action
  setMapExpansionFlag(mapExpansionFlag:boolean){
    this.mapExpansionFlag = mapExpansionFlag;
  }

  /**
   * Map リサイズ関数
   * @param updateMapDimensions 
   */
  @action
  setUpdateMapDimensions(updateMapDimensions:Function){
    this.updateMapDimensions = updateMapDimensions;
  }

  /**
   * Map コンテナ位置(下)
   * @param mapBottom 
   */
  @action
  setMapBottom(mapBottom:string){
    this.mapBottom = mapBottom;
  }

  /**
   * Map コンテナ位置(右)
   * @param mapBottom 
   */
  @action
  setMapRight(mapRight:string){
    this.mapRight = mapRight;
  }

  /**
   * Map コンテナ位置(上)
   * @param mapTop
   */
  @action
  setMapTop(mapTop:string){
    this.mapTop = mapTop;
  }

  /**
   * Map コンテナ位置(左)
   * @param mapLeft 
   */
  @action
  setMapLeft(mapLeft:string){
    this.mapLeft = mapLeft;
  }
  
  /**
   * Map コンテナ幅
   * @param mapBottom 
   */
  @action
  setMapWidth(mapWidth:string){
    this.mapWidth = mapWidth;
  }

  /**
   * Map コンテナ高さ
   * @param mapBottom 
   */
  @action
  setMapHeight(mapHeight:string){
    this.mapHeight = mapHeight;
  }

  /**
   * Map 基準コンテナ要素
   * @param mapBaseContainerElement 
   */
  @action
  setMapBaseContainerElement(mapBaseContainerElement:Ref<HTMLElement>){
    this.mapBaseContainerElement = mapBaseContainerElement;
  }

  /**
   * Map 基準要素
   * @param mapBaseElement
   */
  @action
  setMapBaseElement(mapBaseElement:Ref<HTMLElement>){
    this.mapBaseElement = mapBaseElement;
  }

  //------------シミュレート実行関連------------
  /**
   * 申請地番のセット
   * @param applicationPlace
   */
  @action
  setApplicationPlace(applicationPlace:any){
    this.applicationPlace = Object.assign({},applicationPlace);
  }

  /**
   * 選択中申請区分のセット
   * @param checkedApplicationCategory
   */
  @action
  setCheckedApplicationCategory(checkedApplicationCategory:[]){
    this.checkedApplicationCategory = checkedApplicationCategory;
  }

  /**
   * 概況診断結果のセット
   * @param generalConditionDiagnosisResult
   */
  setGeneralConditionDiagnosisResult(generalConditionDiagnosisResult:Object){
    this.generalConditionDiagnosisResult = generalConditionDiagnosisResult;
  }

  /**
   * 概況診断自動実行用遷移処理
   */
  @action
  moveToGeneralAndRoadJudgementResultViewForSimulateExecution() {
      this.showSidePanel = true;
      this.showGeneralAndRoadJudgementResultView = true;
      this.showInitAndLotNumberSearchView = false;
      this.showInputApplyConditionView = false;
      this.showUserAgreement = false;
      this.isSimulateExecution = true;
      this.terria.setClickMode(""); 
  }
  
  /**
   *  simulator進捗管理 初期化
   */
  @action
  initSetProgressList(){
    const progressListStr = window.localStorage.getItem('simulateProgressList');
    let progressListTemp:any = [];
    if(progressListStr){
        progressListTemp = JSON.parse(progressListStr) || [];
    }
    this.simulateProgressList = progressListTemp;
  }

  /**
   * simulator進捗管理 セット
   * @param requestBody シミュレータAPIリクエストボディ
   */
  @action
  setProgressList(requestBody:any){
    const folderName = requestBody.folderName;
    const fileName = requestBody.fileName;
    const generalConditionDiagnosisResult = requestBody.generalConditionDiagnosisResults;
    let captureMaxCount = 1;
    //全ての処理数を算出
    Object.keys(generalConditionDiagnosisResult).map((key) => {
        if (generalConditionDiagnosisResult[key] && Object.keys(generalConditionDiagnosisResult[key].layers).length > 0) {
          captureMaxCount = captureMaxCount + 1;
        }
    });
    const progressListStr = window.localStorage.getItem('simulateProgressList');
    let progressListTemp:any = [];
    if(progressListStr){
      progressListTemp = JSON.parse(progressListStr) || [];
    }
    const index = progressListTemp.findIndex((progressMap:any)=>progressMap.folderName === folderName);
    if(index < 0){
      const newProgressMap:any = {};
      newProgressMap["folderName"] = folderName;
      newProgressMap["fileName"] = fileName;
      newProgressMap["captureMaxCount"] = captureMaxCount;
      newProgressMap["capturedCount"] = 0;
      newProgressMap["requestBody"] = requestBody;
      newProgressMap["retry"] = false;
      progressListTemp.push(newProgressMap);
    }else{
      progressListTemp[index]["folderName"] = folderName;
      progressListTemp[index]["fileName"] = fileName;
      progressListTemp[index]["captureMaxCount"] = captureMaxCount;
      progressListTemp[index]["capturedCount"] = 0;
      progressListTemp[index]["requestBody"] = requestBody;
    }
    window.localStorage.setItem('simulateProgressList', JSON.stringify(progressListTemp));
    this.simulateProgressList = progressListTemp;
  }

  /**
   * simulator進捗管理 更新(認証情報)
   * @param folderName 一時フォルダ名
   * @param loginId ログインID
   * @param password パスワード
   */
  @action
  updateProgressListForCertification(folderName:string,loginId:string,password:string){
    const progressListStr = window.localStorage.getItem('simulateProgressList');
    let progressListTemp:any = [];
    if(progressListStr){
      progressListTemp = JSON.parse(progressListStr) || [];
    }
    const index = progressListTemp.findIndex((progressMap:any)=>progressMap.folderName === folderName);
    if(index >= 0 && progressListTemp[index]["requestBody"] && progressListTemp[index]["requestBody"]["applicationId"]){
      progressListTemp[index]["requestBody"]["loginId"] = loginId;
      progressListTemp[index]["requestBody"]["password"] = password;
    }
    window.localStorage.setItem('simulateProgressList', JSON.stringify(progressListTemp));
    this.simulateProgressList = progressListTemp;
  }
  

  /**
   * simulator進捗管理 更新
   * @param folderName 一時フォルダ名
   * @param capturedCount 処理済みキャプチャ数
   * @param fileSize ファイルサイズ(任意)
   * @param completeDateTime 完了日時(任意)
   * @param retry 再試行判定フラグ
   */
  @action
  updateProgressList(folderName:string,capturedCount:number,fileSize:string,completeDateTime:string,retry:boolean=false){
    const progressListStr = window.localStorage.getItem('simulateProgressList');
    let progressListTemp:any = [];
    if(progressListStr){
      progressListTemp = JSON.parse(progressListStr) || [];
    }
    const index = progressListTemp.findIndex((progressMap:any)=>progressMap.folderName === folderName);
    if(index >= 0){
      progressListTemp[index]["capturedCount"] = capturedCount;
      if(fileSize){
        progressListTemp[index]["fileSize"] = fileSize;
      }
      if(completeDateTime && !progressListTemp[index]["completeDateTime"]){
        progressListTemp[index]["completeDateTime"] = completeDateTime;
      }
      progressListTemp[index]["retry"] = retry;
    }
    window.localStorage.setItem('simulateProgressList', JSON.stringify(progressListTemp));
    this.simulateProgressList = progressListTemp;
  }

  /**
   * simulator進捗管理 削除
   * @param folderName 
   * @param total 
   */
  @action
  deleteProgressList(folderName:string){
    const progressListStr = window.localStorage.getItem('simulateProgressList');
    if(progressListStr){
      const progressListTemp = JSON.parse(progressListStr) || [];
      const index = progressListTemp.findIndex((progressMap:any)=>progressMap.folderName === folderName);
      if(index >= 0){
        progressListTemp.splice(index, 1);
        window.localStorage.setItem('simulateProgressList', JSON.stringify(progressListTemp));
        this.simulateProgressList = progressListTemp;
      }
    }
  }

  /**
   * simulator実行の概況診断レポートファイル名を生成
   * @returns ファイル名
   */
  getFileName(){
    const date = new Date();
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0'); // 月は0から始まるため+1
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    const seconds = String(date.getSeconds()).padStart(2, '0');
    const formattedDate = `${year}${month}${day}${hours}${minutes}${seconds}`;
    return "概況診断結果_" + formattedDate + ".xlsx";
  }

  /**
   * ↑↑↑既存関数
   */

  /**
   * ↑カスタム機能
   */
  @computed
  get breadcrumbsShown() {
    return (
      this.previewedItem !== undefined ||
      this.userDataPreviewedItem !== undefined
    );
  }

  @computed
  get isToolOpen() {
    return this.currentTool !== undefined;
  }

  @computed
  get hideMapUi() {
    return (
      this.terria.notificationState.currentNotification !== undefined &&
      this.terria.notificationState.currentNotification!.hideUi
    );
  }

  get isMapZooming() {
    return this.terria.currentViewer.isMapZooming;
  }

  /**
   * Returns true if the user is currently interacting with the map - like
   * picking a point or drawing a shape.
   */
  @computed
  get isMapInteractionActive() {
    return this.terria.mapInteractionModeStack.length > 0;
  }
}

interface Tool {
  toolName: string;
  getToolComponent: () => React.ComponentType | Promise<React.ComponentType>;

  showCloseButton: boolean;
  params?: any;
}
