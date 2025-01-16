import { observer } from "mobx-react";
import { reaction } from "mobx";
import PropTypes, { object } from "prop-types";
import React from "react";
import { withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import Icon, { StyledIcon } from "../../../../Styled/Icon";
import Spacing from "../../../../Styled/Spacing";
import Box from "../../../../Styled/Box";
import CustomStyle from "./scss/application-details.scss";
import Common from "../../../AdminDevelopCommon/CommonFixedValue.json";
import Config from "../../../../../customconfig.json";
import ShowMessage from "../Message/ShowMessage";
import AdminTab from "../Tab/AdminTab";
import PageStyle from "../../../DevelopmentPermissionSystem/PageViews/scss/pageStyle.scss";
import FileDownLoadModal from "../Modal/FileDownLoadModal";
import Loader from "../../../Loader";
import AnswerContentList from "../../../DevelopmentPermissionSystem/Views/Answer/AnswerContentList";
import NegotiationContentList from "../../../DevelopmentPermissionSystem/Views/Answer/NegotiationContentList";
import AssessmentContentList from "../../../DevelopmentPermissionSystem/Views/Answer/AssessmentContentList"
import AnswerFileList from "../../../DevelopmentPermissionSystem/Views/Answer/AnswerFileList";
import ApplicationFileList from "../../../DevelopmentPermissionSystem/Views/Answer/ApplicationFileList";
import NotificationFileList from "../../../DevelopmentPermissionSystem/Views/Answer/NotificationFileList";
import AnswerHistoryList from "./AnswerHistoryList";
import FinalDocumentList from "./FinalDocumentList";
import ApplicantInformation from "../../../DevelopmentPermissionSystem/Views/Answer/ApplicantInformation";
import webMapServiceCatalogItem from '../../../../Models/Catalog/Ows/WebMapServiceCatalogItem';
import CommonStrata from "../../../../Models/Definition/CommonStrata";
import AcceptingAnswerContentList from "../../../DevelopmentPermissionSystem/Views/Answer/AcceptingAnswerContentList";

/**
 * 申請情報詳細画面
 */
@observer
class ApplicationDetails extends React.Component {

    static displayName = "ApplicationDetails";

    static propTypes = {
        terria: PropTypes.object.isRequired,
        viewState: PropTypes.object.isRequired,
        theme: PropTypes.object,
        t: PropTypes.func.isRequired
    }

    constructor(props) {
        super(props);
        this.state = {
            viewState: props.viewState,
            terria: props.terria,
            //申請者情報
            applicantInformations: [],
            //申請区分
            checkedApplicationCategory: [],
            //申請ファイル
            applicationFiles: [],
            //回答
            answers: [],
            //部署回答一覧
            departmentAnswers:[],
            //申請地番
            lotNumbers: [],
            //申請状態
            status: "",
            //申請状態コード
            statusCode: 0,
            //回答通知権限
            notificable: false,
            //申請ID
            applicationId: null,
            //申請回答DTO
            ApplyAnswerForm: {},
            //回答履歴
            answerHistory: [],
            //回答ファイル
            answerFile: [],
            // 回答ファイル更新履歴
            answerFileHistory: [],
            //1=回答ファイル一覧,2=申請ファイル一覧,3=通知ファイル一覧,4=回答履歴一覧
            activeFileListType:1,
            //1=事前相談,2=事前協議,3=許可判定
            checkedApplicationStepId:props.viewState.checkedApplicationStepId,
            //選択された回答
            selectedAnswer: null,
            //選択された回答に紐づく回答ファイル
            selectedAnswerFiles:[],
            //選択された回答履歴
            selectedAnswerHistory: [],
            //申請種類
            checkedApplicationType: {},
            //申請追加情報
            applicantAddInformations: [],
            //通知ファイル一覧
            notificationFiles:[],
            //協議対象一覧
            ledgerMaster:[],
            isNotifiedCompleted: props.viewState.isNotifiedCompleted,
            //受付フラグ
            acceptingFlag: "1",
            // 統括部署管理者 
            controlDepartmentAdmin : false,
            // 受付回答一覧(事前協議のみ) 
            departmentAcceptingAnswers:[],
            // 回答一覧種類(事前協議のみ: 0：受付確認・1：回答詳細) 
            checkedAnswerListType:1,
        };
        this.mapBaseElement = React.createRef();
        this.mpBaseContainerElement =React.createRef();
    }

    /**
     * 初期処理（サーバからデータを取得）
     */
    componentDidMount() {
        this.getApplicationDetails(true);
        this.props.viewState.setRefreshConfirmApplicationDetails(this.getApplicationDetails);
    }

    /**
     * 申請情報詳細の取得
     */
    getApplicationDetails = (initFlag=false) => {
        document.getElementById("customloader_main").style.display = "block";
        fetch(Config.config.apiUrl + "/application/detail/" + this.props.viewState.applicationInformationSearchForApplicationId)
        .then(res => {
            // 401認証エラーの場合の処理を追加
            if (res.status === 401) {
                alert('認証情報が無効です。ページの再読み込みを行います。');
                window.location.href = "./login/";
                return null;
            }
            return res.json();
        })
        .then(res => {
            if (res.applicationId) {
                const checkedApplicationStepId = this.state.checkedApplicationStepId;
                const checkedAnswerListType = this.state.checkedAnswerListType;
                this.setApplyAnswerInfoToState(res, checkedApplicationStepId, this.state.selectedAnswer, initFlag, checkedAnswerListType);
            } else {
                alert("申請情報詳細取得に失敗しました。再度操作をやり直してください。");
            }
        }).catch(error => {
            console.error('通信処理に失敗しました', error);
            alert('通信処理に失敗しました');
        }).finally(() => document.getElementById("customloader_main").style.display = "none");
    }

    /**
     * 検索結果をStateにセット
     * @param {*} checkedApplicationStepId 選択中の申請段階
     * @param {*} initFlag 初期フラグ
     * @param {*} res 申請回答情報の検索結果
    */
    setApplyAnswerInfoToState(res, checkedApplicationStepId, selectedAnswer= null, initFlag = false, checkedAnswerListType){
        let answer = null;

        let checkedApplicationCategory = this.state.checkedApplicationCategory;
        let applicantAddInformations = this.state.applicantAddInformations;
        let applicationFiles = this.state.applicationFiles;
        let answers = this.state.answers;
        let departmentAnswers = this.state.departmentAnswers;
        let answerHistory = this.state.answerHistory;
        let answerFile = this.state.answerFile;
        let selectedAnswerHistory = this.state.selectedAnswerHistory;
        let selectedAnswerFiles = this.state.selectedAnswerFiles;
        let notificationFiles = this.state.notificationFiles;
        let activeFileListType = this.state.activeFileListType;
        let ledgerMaster = this.state.ledgerMaster;
        let departmentAcceptingAnswers = this.state.departmentAcceptingAnswers;
        let acceptingFlag = this.state.acceptingFlag;

        if(checkedAnswerListType == null){
            checkedAnswerListType = 1;
        }
        let controlDepartmentAdmin = res.controlDepartmentAdmin;
        let applyAnswerDetailForm = null;
        Object.keys(res.applyAnswerDetails).map(key => {
            let detailForm = res.applyAnswerDetails[key];
            if(detailForm.applicationStepId == checkedApplicationStepId){
                // 事前協議
                if(checkedApplicationStepId ==2){
                    // 事前協議の受付フラグが0：未確認の場合、選択中「受付確認・回答詳細」タブが「回答詳細」にする
                    if(detailForm.acceptingFlag != 0){
                        checkedAnswerListType = 1;
                    }
                    // 統括部署管理者、受付確認の場合、受付確認情報
                    if(controlDepartmentAdmin && checkedAnswerListType == 0){
                        if(detailForm.isAcceptInfo){
                            applyAnswerDetailForm = res.applyAnswerDetails[key];
                        }
                    }else{
                        // 回答確認情報
                        if(!detailForm.isAcceptInfo){
                            applyAnswerDetailForm = res.applyAnswerDetails[key];
                        }
                    }

                    if(Object.keys(ledgerMaster).length < 1){
                        ledgerMaster = detailForm.ledgerMasters;
                    }
                }else{
                    applyAnswerDetailForm = res.applyAnswerDetails[key];
                }
            }
        });

        checkedApplicationCategory = applyAnswerDetailForm.applicationCategories;
        applicantAddInformations = applyAnswerDetailForm.applicantAddInformations;
        applicationFiles = applyAnswerDetailForm.applicationFiles;
        answers = applyAnswerDetailForm.answers;
        departmentAnswers = applyAnswerDetailForm.departmentAnswers;
        answerHistory = applyAnswerDetailForm.answerHistorys;
        answerFile = applyAnswerDetailForm.answerFiles;
        notificationFiles = applyAnswerDetailForm.ledgerFiles;
        departmentAcceptingAnswers = applyAnswerDetailForm.departmentAcceptingAnswers;
        acceptingFlag = applyAnswerDetailForm.acceptingFlag;

        // 回答ファイル一覧 
        if(activeFileListType = 1){
            
            // 事前相談
            if(checkedApplicationStepId == 1){

                if(selectedAnswer){
                    // 回答一覧の行が選択された場合、該当回答に対する回答ファイル一覧を表示
                    answer = answers.find(answer=>answer.answerId === selectedAnswer?.answerId);
                    selectedAnswer = answer;
                    selectedAnswerFiles = answer?.answerFiles?answer.answerFiles:[];

                }else{
                    // 回答一覧の行が選択されない場合、該当申請段階に対するすべての回答ファイル一覧を表示
                    selectedAnswerFiles = applyAnswerDetailForm?.answerFiles?applyAnswerDetailForm.answerFiles:[];
                }
            }

            // 事前協議
            if(checkedApplicationStepId == 2){

                if(selectedAnswer){
                    // 回答一覧の部署ごとの行が選択された場合、該当回答に対する回答ファイル一覧を表示
                    answer = departmentAnswers.find(answer=>answer.departmentAnswerId === selectedAnswer?.departmentAnswerId);
                    selectedAnswer = answer;
                    selectedAnswerFiles = answer?.answerFiles?answer.answerFiles:[];
                }else{
                    // 回答一覧の部署ごとの行が選択されない場合、該当申請段階に対するすべての回答ファイル一覧を表示
                    selectedAnswerFiles = applyAnswerDetailForm?.answerFiles?applyAnswerDetailForm.answerFiles:[];
                }

            }
            // 許可判定
            if(checkedApplicationStepId == 3){
                // 申請段階に対するすべての回答ファイル一覧を表示
                selectedAnswerFiles = applyAnswerDetailForm?.answerFiles?applyAnswerDetailForm.answerFiles:[];
            }
        }

        // 回答履歴一覧 
        if(activeFileListType = 4){
            
            // 事前相談
            if(checkedApplicationStepId == 1){

                if(selectedAnswer){
                    // 回答一覧の行が選択された場合、該当回答に対する回答履歴一覧を表示
                    answer = answers.find(answer=>answer.answerId === selectedAnswer?.answerId);
                    selectedAnswer = answer;
                    selectedAnswerHistory = answer?.answerHistorys?answer.answerHistorys:[];

                }else{
                    // 回答一覧の行が選択されない場合、該当申請段階に対するすべての回答履歴一覧を表示
                    selectedAnswerHistory = applyAnswerDetailForm?.answerHistorys?applyAnswerDetailForm.answerHistorys:[];
                }
            }

            // 事前協議
            if(checkedApplicationStepId == 2){

                if(selectedAnswer){
                    if(selectedAnswer?.answerId){
                        // 回答一覧の行が選択された場合、該当回答に対する回答履歴一覧を表示
                        answer = answers.find(answer=>answer.answerId === selectedAnswer?.answerId);
                        selectedAnswer = answer;
                        selectedAnswerHistory = answer?.answerHistorys?answer.answerHistorys:[];
                    }else{
                        // 回答一覧の部署ごとの行が選択されない場合、該当申請段階に対するすべての回答履歴一覧を表示
                        selectedAnswerHistory = applyAnswerDetailForm?.answerHistorys?applyAnswerDetailForm.answerHistorys:[];
                    }

                }else{
                    // 回答一覧の部署ごとの行が選択されない場合、該当申請段階に対するすべての回答履歴一覧を表示
                    selectedAnswerHistory = applyAnswerDetailForm?.answerHistorys?applyAnswerDetailForm.answerHistorys:[];
                }

            }
            // 許可判定
            if(checkedApplicationStepId == 3){
                if(selectedAnswer){
                    // 回答一覧の行が選択された場合、該当回答に対する回答履歴一覧を表示
                    answer = answers.find(answer=>answer.answerId === selectedAnswer?.answerId);
                    selectedAnswer = answer;
                    selectedAnswerHistory = answer?.answerHistorys?answer.answerHistorys:[];

                }else{
                    // 回答一覧の行が選択されない場合、該当申請段階に対するすべての回答履歴一覧を表示
                    selectedAnswerHistory = applyAnswerDetailForm?.answerHistorys?applyAnswerDetailForm.answerHistorys:[];
                }
            }
        }
        
        // viewStateに切り替えた申請段階を保存
        this.props.viewState.setCheckedApplicationStepId(checkedApplicationStepId);
        this.setState({
            applicationId: res.applicationId,
            status: res.status,
            statusCode: res.statusCode,
            checkedApplicationType: res.applicationType,
            lotNumbers: res.lotNumbers,
            applicantInformations: res.applicantInformations,
            notificable: res.notificable,
            ApplyAnswerForm: res,
            checkedApplicationCategory: checkedApplicationCategory,
            applicantAddInformations: applicantAddInformations,
            applicationFiles: applicationFiles,
            answers: answers,
            departmentAnswers: departmentAnswers,
            answerHistory: answerHistory,
            answerFile: answerFile,
            notificationFiles: notificationFiles,
            selectedAnswerHistory: selectedAnswerHistory,
            selectedAnswer: selectedAnswer,
            selectedAnswerFiles: selectedAnswerFiles,
            ledgerMaster: ledgerMaster,
            controlDepartmentAdmin: controlDepartmentAdmin,
            departmentAcceptingAnswers: departmentAcceptingAnswers,
            acceptingFlag:acceptingFlag
        },()=>{
            if(initFlag){
                this.props.viewState.setLotNumbers(res.lotNumbers);
                this.props.viewState.setMapBaseElement(this.mapBaseElement);
                this.props.viewState.setMapBaseContainerElement(this.mpBaseContainerElement);
                setTimeout(() => {
                    this.props.viewState.updateMapDimensions();
                    this.focusMapPlaceDriver();
                }, 1000);
            }else{
                this.props.viewState.updateMapDimensions();
            }
        });
    }

    /**
     * 画面に表示可能の協議リスト取得
     */
    getLedgerMasterList(){
        const applicationStepId = 2;
        // 協議対象一覧を取得
        fetch(Config.config.apiUrl + "/answer/ledger/" + applicationStepId)
        .then(res => {
            // 401認証エラーの場合の処理を追加
            if (res.status === 401) {
                alert('認証情報が無効です。ページの再読み込みを行います。');
                window.location.href = "./login/";
                return null;
            }
            return res.json();
        })
        .then(res => {
            if (Object.keys(res).length > 0) {
                this.setState({ledgerMaster: res});
            } else {
                alert("協議対象一覧に失敗しました。再度操作をやり直してください。");
            }
        }).catch(error => {
            console.error('通信処理に失敗しました', error);
            alert('通信処理に失敗しました');
        });
    }

    /**
     * フォーカス処理ドライバー
     */
    focusMapPlaceDriver(){
        if(!this.state.lotNumbers) return;
        let applicationPlace = Object.assign({}, this.state.lotNumbers);
        applicationPlace = Object.values(applicationPlace);
        applicationPlace = applicationPlace.filter(Boolean);
        let maxLon = 0;
        let maxLat = 0;
        let minLon = 0;
        let minLat = 0;
        Object.keys(applicationPlace).map(key => {
            const targetMaxLon = parseFloat(applicationPlace[key].maxlon);
            const targetMaxLat = parseFloat(applicationPlace[key].maxlat);
            const targetMinLon = parseFloat(applicationPlace[key].minlon);
            const targetMinLat = parseFloat(applicationPlace[key].minlat);
            if (key === 0 || key === "0") {
                maxLon = targetMaxLon;
                maxLat = targetMaxLat;
                minLon = targetMinLon;
                minLat = targetMinLat;
            } else {
                if (maxLon < targetMaxLon) {
                    maxLon = targetMaxLon;
                }
                if (maxLat < targetMaxLat) {
                    maxLat = targetMaxLat;
                }
                if (minLon > targetMinLon) {
                    minLon = targetMinLon;
                }
                if (minLat > targetMinLat) {
                    minLat = targetMinLat;
                }
            }
        })
        this.outputFocusMapPlace(maxLon, maxLat, minLon, minLat, (maxLon + minLon) / 2, (maxLat + minLat) / 2);
        // 申請地のレイヤを表示する(申請情報検索からの流用)
        try{
            const wmsUrl = Config.config.geoserverUrl;
            const items = this.state.terria.workbench.items;
            let layerFlg = false;
            for (const aItem of items) {
                if (aItem.uniqueId === Config.layer.lotnumberSearchLayerNameForApplicationSearchTarget) {
                    aItem.setTrait(CommonStrata.user,
                        "parameters",
                        {
                            "viewparams": Config.layer.lotnumberSearchViewParamNameForApplicationSearchTarget + this.state.applicationId,
                        });
                    aItem.loadMapItems();
                    layerFlg = true;
                }
            }
            if(!layerFlg){
                const item = new webMapServiceCatalogItem(Config.layer.lotnumberSearchLayerNameForApplicationSearchTarget, this.state.terria);
                item.setTrait(CommonStrata.definition, "url", wmsUrl);
                item.setTrait(CommonStrata.user, "name", Config.layer.lotnumberSearchLayerDisplayNameForApplicationSearchTarget);
                item.setTrait(
                    CommonStrata.user,
                    "layers",
                    Config.layer.lotnumberSearchLayerNameForApplicationSearchTarget);
                item.setTrait(CommonStrata.user,
                    "parameters",
                    {
                        "viewparams": Config.layer.lotnumberSearchViewParamNameForApplicationSearchTarget + this.state.applicationId,
                    });
                item.loadMapItems();
                this.state.terria.workbench.add(item);
            }
        } catch (error) {
            console.error('処理に失敗しました', error);
        }
    }

    /**
     * フォーカス処理
     * @param {number} maxLon 最大経度
     * @param {number} maxLat 最大緯度
     * @param {number} minLon 最小経度
     * @param {number} minLat 最小緯度
     * @param {number} lon 経度
     * @param {number} lat 緯度
     */
    outputFocusMapPlace(maxLon, maxLat, minLon, minLat, lon, lat){
        this.props.terria.focusMapPlace(maxLon, maxLat, minLon, minLat, lon, lat, this.props.viewState);
    }

    /**
     * 回答通知処理
     * @returns 
     */
    nextAnswerNotificationView() {
        const notificable = this.state.notificable;
        if (!notificable) {
            alert("回答通知権限がありません。");
            return false;
        }

        // SC216_回答通知確認画面を開く
        this.props.viewState.showConfirmAnswerNotificationModal(this.state.ApplyAnswerForm, this.state.checkedApplicationStepId);
    }

    /**
     * 戻るボタン処理
     */
    hideApplicationDetailsView(){
        // 選択中の申請段階をクリアする
        this.props.viewState.checkedApplicationStepId = 1;
        if(this.state.viewState.adminBackPage !== "chat"){
            this.state.viewState.changeAdminTabActive(this.state.viewState.adminBackTab);
            this.state.viewState.adminBackPage ? this.state.viewState.changeApplyPageActive(this.state.viewState.adminBackPage): this.state.viewState.changeApplyPageActive("applyList");
        }else{
            let backToPage = this.props.viewState.backToPage;
            if(backToPage?.tab && backToPage.tab == "mapSearch"){
                this.state.viewState.changeAdminTabActive(backToPage.tab);
                this.state.viewState.changeApplyPageActive("applyList");
            }else{
                this.state.viewState.changeAdminTabActive("applySearch");
                this.state.viewState.changeApplyPageActive("applyList");
            }
        }
        this.props.viewState.setIsSidePanelFullScreen(false);
    }

     /**
     * 回答内容一覧の行クリックイベント
     * @param {*} event イベント
     * @param {*} answerId 回答ID
     * @param {*} departmentAnswerId 部署回答ID
     */
    clickAnswer = (event, answerId, departmentAnswerId = 0) => {
        const checkedApplicationStepId = this.state.checkedApplicationStepId;

        if(checkedApplicationStepId == 2){
            if(departmentAnswerId!==undefined && departmentAnswerId !== null && Number(departmentAnswerId) > 0){
                const departmentAnswers = [...this.state.departmentAnswers];
                const selectedDepartmentAnswers = departmentAnswers.find(answer=>answer.departmentAnswerId === departmentAnswerId);
                if(answerId!==undefined && answerId !== null && Number(answerId) > 0){
                    const selectedAnswer = selectedDepartmentAnswers.answers.find(answer=>answer.answerId === answerId);
                    const selectedAnswerFiles = selectedDepartmentAnswers?.answerFiles?selectedDepartmentAnswers.answerFiles:[];
                    const selectedAnswerHistory = selectedAnswer.answerHistorys?selectedAnswer.answerHistorys:[];
                    this.setState({selectedAnswer:selectedAnswer,selectedAnswerFiles:selectedAnswerFiles,selectedAnswerHistory:selectedAnswerHistory});
                }else{

                    const selectedAnswerFiles = selectedDepartmentAnswers?.answerFiles?selectedDepartmentAnswers.answerFiles:[];
                    const selectedAnswerHistory = selectedDepartmentAnswers.answerHistorys?selectedDepartmentAnswers.answerHistorys:[];
                    this.setState({selectedAnswer:selectedDepartmentAnswers,selectedAnswerFiles:selectedAnswerFiles,selectedAnswerHistory:selectedAnswerHistory});
                }
            }else{
                // 選択中回答がない場合、申請段階ごとの回答ファイルと、回答履歴を表示
                this.setState({selectedAnswer:null, selectedAnswerFiles:  [...this.state.answerFile], selectedAnswerHistory: [...this.state.answerHistory]});
            }
        }
        if(checkedApplicationStepId == 1){
            if(answerId!==undefined && answerId !== null && Number(answerId) > 0){
                const answers = [...this.state.answers];
                const selectedAnswer = answers.find(answer=>answer.answerId === answerId);
                const selectedAnswerFiles = selectedAnswer?.answerFiles?selectedAnswer.answerFiles:[];
                const selectedAnswerHistory = selectedAnswer?.answerHistorys?selectedAnswer.answerHistorys:[];
                this.setState({selectedAnswer:selectedAnswer,selectedAnswerFiles:selectedAnswerFiles,selectedAnswerHistory:selectedAnswerHistory});
            }else{
                this.setState({selectedAnswer:null, selectedAnswerFiles:  [...this.state.answerFile], selectedAnswerHistory: [...this.state.answerHistory]});
            }
        }

        if(checkedApplicationStepId == 3){
            if(answerId!==undefined && answerId !== null && Number(answerId) > 0){
                const answers = [...this.state.answers];
                const selectedAnswer = answers.find(answer=>answer.answerId === answerId);
                const selectedAnswerFiles =   [...this.state.answerFile];
                const selectedAnswerHistory = selectedAnswer?.answerHistorys?selectedAnswer.answerHistorys:[];
                this.setState({selectedAnswer:selectedAnswer,selectedAnswerFiles:selectedAnswerFiles,selectedAnswerHistory:selectedAnswerHistory});
            }else{
                this.setState({selectedAnswer:null, selectedAnswerFiles:  [...this.state.answerFile], selectedAnswerHistory: [...this.state.answerHistory]});
            }
        }
    }

    /**
     * 申請段階に対する申請情報が存在するか判定
     * @param {Number} applicationStepId 申請段階ID
     * @returns 
     */
    isExist(applicationStepId){
        const applyAnswerDetailForms = this.state.ApplyAnswerForm.applyAnswerDetails;

        let isExist = false;
        if(applyAnswerDetailForms){
            let index = applyAnswerDetailForms.findIndex(detail =>detail.applicationStepId === applicationStepId);
            if(index > -1){
                isExist = true;
            }
        }
        return isExist;
    }


    render() {
        const t = this.props.t;
        const adminInfoMessage = t("adminInfoMessage.tipsForAnswerReister");
        const applicantInformations = this.state.applicantInformations;
        const checkedApplicationCategory = this.state.checkedApplicationCategory;
        const applicationFiles = this.state.applicationFiles;
        const status = this.state.status;
        const statusCode = this.state.statusCode;
        const answers = this.state.answers;
        const lotNumbers = this.state.lotNumbers;
        const applicationId = this.state.applicationId;
        const answerHistory = this.state.answerHistory;
        const answerFileHistory = this.state.answerFileHistory;
        const activeFileListType = this.state.activeFileListType;
        const checkedApplicationStepId = this.state.checkedApplicationStepId;
        const selectedAnswer = this.state.selectedAnswer;
        const selectedAnswerFiles = this.state.selectedAnswerFiles;
        const selectedAnswerHistory = this.state.selectedAnswerHistory;
        const isAdmin = this.props.terria.authorityJudgment();
        const applicationType = this.state.checkedApplicationType;
        const applicantAddInformations = this.state.applicantAddInformations;
        const departmentAnswers = this.state.departmentAnswers;
        const ledgerMaster = this.state.ledgerMaster;
        const notificationFiles = this.state.notificationFiles;

        const acceptingFlag = this.state.acceptingFlag;
        const controlDepartmentAdmin = this.state.controlDepartmentAdmin;
        const departmentAcceptingAnswers = this.state.departmentAcceptingAnswers;
        const checkedAnswerListType = this.state.checkedAnswerListType;

        let answerFileCount = 0;
        if(checkedApplicationStepId == 3){

            // 回答ファイルの件数
            const count1 = Object.keys(selectedAnswerFiles).length;
            answerFileCount = count1;
        }

        return (
        <div style={{overflowY:"auto" }}>
            <Box overflow={false} overflowY={false}>
                <Box col8 className={PageStyle.text_area}>
                    <span dangerouslySetInnerHTML={{ __html: adminInfoMessage }}></span>
                </Box>
                <Box col4 right>
                    <button
                        className={`${CustomStyle.btn_baise_style} ${CustomStyle.btn_gry}`}
                        style={{width:"30%",height:"40px"}}
                        onClick={e => {
                            this.hideApplicationDetailsView();
                        }}
                    >
                        <span>戻る</span>
                    </button>
                    <button
                        className={`${CustomStyle.btn_baise_style} `}
                        style={{width:"30%",height:"40px"}}
                        onClick={e => {

                            let applyAnswerForm = this.state.ApplyAnswerForm;
                            let activeApplicationStepId = applyAnswerForm.applyAnswerDetails[0].applicationStepId;
                            this.props.viewState.nextAnswerInputView(applicationId, applyAnswerForm, activeApplicationStepId );
                        }}
                    >
                        <span>回答登録</span>
                    </button>
                    <button
                        className={`${CustomStyle.btn_baise_style} `}
                        style={{width:"30%",height:"40px"}}
                        onClick={e => {
                            this.nextAnswerNotificationView();
                        }}
                    >
                        <span>回答通知</span>
                    </button>
                </Box>
            </Box>
            <Box>
                <Box col12 className={CustomStyle.custom_nuv}>
                    申請情報詳細
                </Box>
            </Box>
            <Box>
                <Box col4 style={{display:"block"}}>
                    <Spacing bottom={3} />
                    <Box col12 centered>
                        <button
                            className={`${CustomStyle.btn_baise_style} ${checkedApplicationStepId !== 1? CustomStyle.checked_button: ""}`}
                            style={{width:"30%",height:"40px"}}
                            onClick={e => {
                                this.clickAnswer(null,0,0);
                                this.setApplyAnswerInfoToState(this.state.ApplyAnswerForm, 1, null, false, null);
                                if(activeFileListType == 5){
                                    this.setState({activeFileListType:1});
                                }
                                this.setState({checkedApplicationStepId:1});
                                setTimeout(() => {
                                    this.props.viewState.updateMapDimensions();
                                }, 2000);
                            }}
                        >
                            <span>事前相談</span>
                        </button>
                        <button
                            className={`${CustomStyle.btn_baise_style} ${!this.isExist(2) ? CustomStyle.disabled_button : checkedApplicationStepId !== 2? CustomStyle.checked_button:""}`}
                            style={{width:"30%",height:"40px"}}
                            disabled = {!this.isExist(2)}
                            onClick={e => {
                                this.clickAnswer(null,0,0);
                                this.setApplyAnswerInfoToState(this.state.ApplyAnswerForm, 2, null, false, null);
                                if(activeFileListType == 5){
                                    this.setState({activeFileListType:1});
                                }
                                this.setState({checkedApplicationStepId:2});
                                setTimeout(() => {
                                    this.props.viewState.updateMapDimensions();
                                }, 2000);
                            }}
                        >
                            <span>事前協議</span>
                        </button>
                        <button
                            className={`${CustomStyle.btn_baise_style} ${!this.isExist(3) ? CustomStyle.disabled_button : checkedApplicationStepId !== 3? CustomStyle.checked_button:""}`}
                            style={{width:"30%",height:"40px"}}
                            disabled = {!this.isExist(3)}
                            onClick={e => {
                                this.clickAnswer(null,0,0);
                                this.setApplyAnswerInfoToState(this.state.ApplyAnswerForm, 3, null, false, null);
                                this.setState({checkedApplicationStepId:3});
                                setTimeout(() => {
                                    this.props.viewState.updateMapDimensions();
                                }, 2000);
                            }}
                        >
                            <span>許可判定</span>
                        </button>
                    </Box>
                    <ApplicantInformation 
                        viewState={this.props.viewState} 
                        terria={this.props.terria}
                        applicationType={applicationType}
                        applicantInformations={applicantInformations} 
                        applicantAddInformations={applicantAddInformations} 
                        checkedApplicationCategory={checkedApplicationCategory} 
                        lotNumbers={lotNumbers} 
                        answers={answers}
                        departmentAnswers={checkedAnswerListType == 0? departmentAcceptingAnswers : departmentAnswers} 
                        status={status}
                        applicationId={applicationId}
                    />
                    <Spacing bottom={3} />
                    <button className={`${CustomStyle.custom_selection} ${activeFileListType == 1? CustomStyle.checked_file_button: ""}`}
                        onClick={e => { this.setState({activeFileListType:1})}}
                    >
                        <span>回答ファイル一覧</span>
                        <StyledIcon styledWidth={"12px"} fillColor={"white"} glyph={Icon.GLYPHS.right}/>
                    </button>
                    <Spacing bottom={1} />
                    <button className={`${CustomStyle.custom_selection} ${activeFileListType == 2? CustomStyle.checked_file_button: ""}`}
                        onClick={e => { this.setState({activeFileListType:2})}}
                    >
                        <span>申請ファイル一覧</span>
                        <StyledIcon styledWidth={"12px"} fillColor={"white"} glyph={Icon.GLYPHS.right}/>
                    </button>
                    <Spacing bottom={1} />
                    <button className={`${CustomStyle.custom_selection} ${activeFileListType == 3? CustomStyle.checked_file_button: ""}`}
                        onClick={e => { this.setState({activeFileListType:3})}}
                    >
                        <span>発行様式</span>
                        <StyledIcon styledWidth={"12px"} fillColor={"white"} glyph={Icon.GLYPHS.right}/>
                    </button>
                    <Spacing bottom={1} />
                    <button className={`${CustomStyle.custom_selection} ${activeFileListType == 4? CustomStyle.checked_file_button: ""}`}
                        onClick={e => { this.setState({activeFileListType:4})}}
                    >
                        <span>回答履歴一覧</span>
                        <StyledIcon styledWidth={"12px"} fillColor={"white"} glyph={Icon.GLYPHS.right}/>
                    </button>
                    {checkedApplicationStepId == 3 && statusCode == 305 && 
                    (
                        <>
                            <Spacing bottom={1} />
                            <button className={`${CustomStyle.custom_selection} ${activeFileListType == 5? CustomStyle.checked_file_button: ""}`}
                                onClick={e => { this.setState({activeFileListType:5})}}
                            >
                                <span>最終提出書類一式</span>
                                <StyledIcon styledWidth={"12px"} fillColor={"white"} glyph={Icon.GLYPHS.right}/>
                            </button> 
                        </>
                    )}
                </Box>
                <Box col8 ref={this.mpBaseContainerElement} style={{height:"75.5vh"}}>
                    <Box
                        centered
                        displayInlineBlock
                        className={CustomStyle.custom_content}
                    >
                        {checkedApplicationStepId == 1 && (
                        <Box col12>
                            <AnswerContentList 
                                viewState={this.props.viewState} 
                                terria={this.props.terria} 
                                answers ={answers}
                                intervalID = {this.state.intervalID}
                                clickAnswer = {this.clickAnswer}
                                selectedAnswerId = {selectedAnswer?selectedAnswer.answerId?selectedAnswer.answerId:selectedAnswer.departmentAnswerId:0}
                                applicationId = {applicationId}
                            />
                        </Box>
                        )}
                        {checkedApplicationStepId == 2 && (
                        <Box col12 displayInlineBlock>
                            {/* 統括部署管理者の場合、受付確認・回答詳細のタブを表示する */}
                            {controlDepartmentAdmin == true && (
                                <>
                                <Spacing bottom={2} />
                                <Box col12>
                                    {/* 受付フラグが「0：未確認」の場合、受付確認が活性 */}
                                    <button
                                        className={`${CustomStyle.btn_baise_style} ${checkedAnswerListType !== 0? CustomStyle.checked_button:""} ${acceptingFlag !== "0"? CustomStyle.disabled_button :""}`}
                                        style={{width:"20%",height:"40px"}}
                                        disabled = {acceptingFlag !== "0" ? true : false}
                                        onClick={e => {
                                            this.clickAnswer(null,0,0);
                                            this.setApplyAnswerInfoToState(this.state.ApplyAnswerForm, 2, null, false, 0);
                                            this.setState({checkedAnswerListType:0});
                                        }}
                                    >
                                        <span>受付確認</span>
                                    </button>
                                    <button
                                        className={`${CustomStyle.btn_baise_style} ${checkedAnswerListType !== 1? CustomStyle.checked_button:"" }`}
                                        style={{width:"20%",height:"40px"}}
                                        onClick={e => {
                                            this.clickAnswer(null,0,0);
                                            this.setApplyAnswerInfoToState(this.state.ApplyAnswerForm, 2, null, false, 1);
                                            this.setState({checkedAnswerListType:1});
                                        }}
                                    >
                                        <span>回答詳細</span>
                                    </button>
                                </Box>
                                <Spacing bottom={2} />
                                </>
                            )}
                            {/* 受付確認-回答一覧 */}
                            {checkedAnswerListType == 0 && (
                                <Box col12>
                                    <AcceptingAnswerContentList 
                                        viewState={this.props.viewState} 
                                        terria={this.props.terria} 
                                        departmentAcceptingAnswers ={departmentAcceptingAnswers}
                                        intervalID = {this.state.intervalID}
                                        clickAnswer = {this.clickAnswer}
                                        selectedAnswerId = {selectedAnswer?selectedAnswer.acceptingAnswerId:0}
                                        selectedDepartmentAnswerId = {selectedAnswer?selectedAnswer.departmentAnswerId:0}
                                        ledgerMaster = {ledgerMaster}
                                        applicationId = {applicationId}
                                    />
                                </Box>
                            )}
                             {/* 回答詳細-回答一覧 */}
                            {checkedAnswerListType == 1 && (
                                 <Box col12>
                                    <NegotiationContentList 
                                        viewState={this.props.viewState} 
                                        terria={this.props.terria} 
                                        departmentAnswers ={departmentAnswers}
                                        intervalID = {this.state.intervalID}
                                        clickAnswer = {this.clickAnswer}
                                        selectedAnswerId = {selectedAnswer?selectedAnswer.answerId:0}
                                        selectedDepartmentAnswerId = {selectedAnswer?selectedAnswer.departmentAnswerId:0}
                                        ledgerMaster = {ledgerMaster}
                                        applicationId = {applicationId}
                                    />
                                </Box>
                            )}
                        </Box>
                        )}
                        {checkedApplicationStepId == 3 && (
                        <Box col12>
                            <AssessmentContentList 
                                viewState={this.props.viewState} 
                                terria={this.props.terria} 
                                answers ={answers}
                                intervalID = {this.state.intervalID}
                                clickAnswer = {this.clickAnswer}
                                selectedAnswerId = {selectedAnswer?selectedAnswer.answerId:0}
                                applicationId = {applicationId}
                                answerFileCount={answerFileCount}
                           />
                        </Box>
                        )}
                        <Spacing bottom={1} />
                        <Box>
                            {activeFileListType == 1&& (
                                <Box col8>
                                    <AnswerFileList 
                                        viewState={this.props.viewState} 
                                        terria={this.props.terria} 
                                        answerFiles = {selectedAnswerFiles}
                                    />
                                </Box>
                            )}
                            {activeFileListType == 2&& (
                                <Box col8>
                                    <ApplicationFileList 
                                        viewState={this.props.viewState} 
                                        terria={this.props.terria} 
                                        applicationFiles = {applicationFiles}
                                    />
                                </Box>
                            )}
                            {activeFileListType == 3&& (
                                <Box col8>
                                    <NotificationFileList 
                                        viewState={this.props.viewState} 
                                        terria={this.props.terria} 
                                        notificationFiles = {notificationFiles}
                                    />
                                </Box>
                            )}
                            {activeFileListType == 4&& (
                                <Box col8>
                                    <AnswerHistoryList 
                                        viewState={this.props.viewState} 
                                        terria={this.props.terria} 
                                        answerHistory = {selectedAnswerHistory}
                                        applicationStepId={checkedApplicationStepId}
                                    />
                                </Box>
                            )}
                            {checkedApplicationStepId == 3 && statusCode == 305 && activeFileListType == 5&& (
                                <Box col8>
                                    <FinalDocumentList 
                                        viewState={this.props.viewState} 
                                        terria={this.props.terria} 
                                        applicationId = {applicationId}
                                    />
                                </Box>
                            )}
                            <Box col4 ref={this.mapBaseElement} style={{height:"23vh"}}>
                            </Box>
                        </Box>
                    </Box>
                </Box>
            </Box>
        </div>
        );
    }
}
export default withTranslation()(withTheme(ApplicationDetails));