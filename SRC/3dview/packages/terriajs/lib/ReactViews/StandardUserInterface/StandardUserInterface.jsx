import React from "react";
import memo from "react";
import createReactClass from "create-react-class";
import { ThemeProvider, createGlobalStyle } from "styled-components";
import PropTypes from "prop-types";
import combine from "terriajs-cesium/Source/Core/combine";

import { terriaTheme } from "./StandardTheme";
import arrayContains from "../../Core/arrayContains";
import DesktopHeader from "../Desktop/DesktopHeader";
import Branding from "../SidePanel/Branding";
import DragDropFile from "../DragDropFile";
import DragDropNotification from "./../DragDropNotification";
import ExplorerWindow from "../ExplorerWindow/ExplorerWindow";
import FeatureInfoPanel from "../FeatureInfo/FeatureInfoPanel";
import FeedbackForm from "../Feedback/FeedbackForm";
import MapColumn from "./MapColumn";
import MapInteractionWindow from "../Notification/MapInteractionWindow";
import TrainerBar from "../Map/TrainerBar/TrainerBar";
import ExperimentalFeatures from "../Map/ExperimentalFeatures";
import MobileHeader from "../Mobile/MobileHeader";
import Notification from "../Notification/Notification";
import ProgressBar from "../Map/ProgressBar";
import SidePanel from "../SidePanel/SidePanel";
import processCustomElements from "./processCustomElements";
import FullScreenButton from "./../SidePanel/FullScreenButton.jsx";
import StoryPanel from "./../Story/StoryPanel.jsx";
import StoryBuilder from "./../Story/StoryBuilder.jsx";
import ApplicationCategorySelection from "../DevelopmentPermissionSystem/Views/Apply/ApplicationCategorySelection.jsx";
import MapSelection from "../DevelopmentPermissionSystem/Views/Apply/Map/MapSelection.jsx";
import CharacterSelection from "../DevelopmentPermissionSystem/Views/Apply/Map/CharacterSelection.jsx";
import GeneralConditionDiagnosis from "../DevelopmentPermissionSystem/Views/Apply/GeneralConditionDiagnosis.jsx";
import EnterApplicantInformation from "../DevelopmentPermissionSystem/Views/Apply/EnterApplicantInformation.jsx";
import UploadApplicationInformation from "../DevelopmentPermissionSystem/Views/Apply/UploadApplicationInformation.jsx";
import ConfirmApplicationDetails from "../DevelopmentPermissionSystem/Views/Apply/ConfirmApplicationDetails.jsx";
import CustomMessage from "../AdminDevelopCommon/CustomMessage.jsx";
import AnswerLogin from "../DevelopmentPermissionSystem/Views/Answer/AnswerLogin.jsx";
import AnswerContent from "../DevelopmentPermissionSystem/Views/Answer/AnswerContent.jsx";
import UserAgreement from "../DevelopmentPermissionSystem/Views/UserAgreement/UserAgreement.jsx";
import ApplicationInformationSearch from "../AdminPermissionSystem/Views/Apply/ApplicationInformationSearch.jsx";
import ApplicationDetails from "../AdminPermissionSystem/Views/Apply/ApplicationDetails.jsx";
import AnswerInput from "../AdminPermissionSystem/Views/Apply/AnswerInput.jsx";
import ApplicationList from "../AdminPermissionSystem/Views/Apply/ApplicationList.jsx";

import PrintView from "../../ReactViews/Map/Panels/SharePanel/Print/PrintView";

import withFallback from "../HOCs/withFallback";
import TourPortal from "../Tour/TourPortal";
import SatelliteHelpPrompt from "../HelpScreens/SatelliteHelpPrompt";
import WelcomeMessage from "../WelcomeMessage/WelcomeMessage";

import { Small, Medium } from "../Generic/Responsive";
import classNames from "classnames";
import "inobounce";

import { withTranslation } from "react-i18next";

import Styles from "./standard-user-interface.scss";
// import Variables from "../../Sass/common/variables";
import { observer } from "mobx-react";
import { action, runInAction } from "mobx";
import HelpPanel from "../Map/Panels/HelpPanel/HelpPanel";
import Tool from "../Tools/Tool";
import Disclaimer from "../Disclaimer";
import CollapsedNavigation from "../Map/Navigation/Items/OverflowNavigationItem";
import LotNumberSearch from "../DevelopmentPermissionSystem/Views/LotNumberSearch/LotNumberSearch";
import Config from "../../../customconfig.json";
import ApplicationLotNumberPanel from "../AdminPermissionSystem/Views/Apply/ApplicationLotNumberPanel";
import { getShareData } from "../Map/Panels/SharePanel/BuildShareLink";
import Cartographic from "terriajs-cesium/Source/Core/Cartographic";
import Ellipsoid from "terriajs-cesium/Source/Core/Ellipsoid";
import sampleTerrainMostDetailed from "terriajs-cesium/Source/Core/sampleTerrainMostDetailed";
import WebMapServiceCatalogItem from "../../Models/Catalog/Ows/WebMapServiceCatalogItem";
import CommonStrata from "../../Models/Definition/CommonStrata";
import MapFitButton from "../DevelopmentPermissionSystem/Views/Map/MapFitButton";
import MapFitButtonForAChatView from "../DevelopmentPermissionSystem/Views/Map/MapFitButtonForAChatView";

export const showStoryPrompt = (viewState, terria) => {
  terria.configParameters.showFeaturePrompts &&
    terria.configParameters.storyEnabled &&
    terria.stories.length === 0 &&
    viewState.toggleFeaturePrompt("story", true);
};
const GlobalTerriaStyles = createGlobalStyle`
  // Theme-ify sass classes until they are removed

  // We override the primary, secondary, map and share buttons here as they
  // are imported everywhere and used in various ways - until we remove sass
  // this is the quickest way to tackle them for now
  .tjs-_buttons__btn--map {
    ${p => p.theme.addTerriaMapBtnStyles(p)}
  }

  .tjs-_buttons__btn-primary {
    ${p => p.theme.addTerriaPrimaryBtnStyles(p)}
  }

  .tjs-_buttons__btn--secondary,
  .tjs-_buttons__btn--close-modal {
    ${p => p.theme.addTerriaSecondaryBtnStyles(p)}
  }

  .tjs-_buttons__btn--tertiary {
    ${p => p.theme.addTerriaTertiaryBtnStyles(p)}
  }

  .tjs-_buttons__btn-small:hover,
  .tjs-_buttons__btn-small:focus {
    color: ${p => p.theme.colorPrimary};
  }

  .tjs-share-panel__catalog-share-inner {
    background: ${p => p.theme.greyLightest};
  }

  .tjs-share-panel__btn--catalogShare {
    color: ${p => p.theme.colorPrimary};
    background:transparent;
    svg {
      fill: ${p => p.theme.colorPrimary};
    }
  }
  .tjs-dropdown__btn--dropdown {
    color: ${p => p.theme.textDark};
    background: ${p => p.theme.textLight};
    &:hover,
    &:focus {
      color: ${p => p.theme.textDark};
      background: ${p => p.theme.textLight};
      border: 1px solid ${p => p.theme.colorPrimary};
    }
    svg {
      fill: ${p => p.theme.textDark};
    }
  }
  .tjs-dropdown__btn--option.tjs-dropdown__is-selected {
    color: ${p => p.theme.colorPrimary};
  }


  ${props =>
    props.experimentalFeatures &&
    `
    body {
      *:focus {
        outline: 3px solid #C390F9;
      }
    }
  `}
`;
export const animationDuration = 250;
/** blah */
const StandardUserInterface = observer(
  createReactClass({
    displayName: "StandardUserInterface",

    propTypes: {
      /**
       * Terria instance
       */
      terria: PropTypes.object.isRequired,
      /**
       * All the base maps.
       */
      allBaseMaps: PropTypes.array,
      themeOverrides: PropTypes.object,
      viewState: PropTypes.object.isRequired,
      minimumLargeScreenWidth: PropTypes.number,
      version: PropTypes.string,
      children: PropTypes.oneOfType([
        PropTypes.arrayOf(PropTypes.element),
        PropTypes.element
      ]),
      t: PropTypes.func.isRequired
    },

    getDefaultProps() {
      return { minimumLargeScreenWidth: 768 };
    },

    /* eslint-disable-next-line camelcase */
    UNSAFE_componentWillMount() {
      const { t } = this.props;
      const that = this;
      // only need to know on initial load
      this.dragOverListener = e => {
        if (
          !e.dataTransfer.types ||
          !arrayContains(e.dataTransfer.types, "Files")
        ) {
          return;
        }
        e.preventDefault();
        e.stopPropagation();
        e.dataTransfer.dropEffect = "copy";
        that.acceptDragDropFile();
      };

      this.resizeListener = () => {
        runInAction(() => {
          this.props.viewState.useSmallScreenInterface = this.shouldUseMobileInterface();
        });
      };

      window.addEventListener("resize", this.resizeListener, false);

      this.resizeListener();

      if (
        this.props.terria.configParameters.storyEnabled &&
        this.props.terria.stories &&
        this.props.terria.stories.length &&
        !this.props.viewState.storyShown
      ) {
        this.props.viewState.terria.notificationState.addNotificationToQueue({
          title: t("sui.notifications.title"),
          message: t("sui.notifications.message"),
          confirmText: t("sui.notifications.confirmText"),
          denyText: t("sui.notifications.denyText"),
          confirmAction: action(() => {
            this.props.viewState.storyShown = true;
          }),
          denyAction: action(() => {
            this.props.viewState.storyShown = false;
          }),
          type: "story",
          width: 300
        });
      }
    },

    componentDidMount() {
      this._wrapper.addEventListener("dragover", this.dragOverListener, false);
      showStoryPrompt(this.props.viewState, this.props.terria);
    },

    componentWillUnmount() {
      window.removeEventListener("resize", this.resizeListener, false);
      document.removeEventListener("dragover", this.dragOverListener, false);
    },

    acceptDragDropFile() {
      runInAction(() => {
        this.props.viewState.isDraggingDroppingFile = true;
      });
      // if explorer window is already open, we open my data tab
      if (this.props.viewState.explorerPanelIsVisible) {
        this.props.viewState.openUserData();
      }
    },

    shouldUseMobileInterface() {
      return document.body.clientWidth < this.props.minimumLargeScreenWidth;
    },

    //地図選択から申請の取得
    getApplication(){
      if(this.props.terria.clickMode === "2" && this.props.terria.authorityJudgment()){
          console.log(this.props.terria.lon + " " + this.props.terria.lat);
          fetch(Config.config.apiUrl + "/lotnumber/getFromLonlat/goverment", {
              method: 'POST',
              body: JSON.stringify({
                  latiude: this.props.terria.lat,
                  longitude:this.props.terria.lon
              }),
              headers: new Headers({ 'Content-type': 'application/json' }),
          })
          .then(res => res.json())
          .then(res => {
              this.showLayers(res);
              const applocationId = res[0]?.applicationId;
              if(Object.keys(res).length === 1 && applocationId){
                this.props.viewState.closeApplicationLotNumberPanelView();
                this.props.viewState.nextApplicationDetailsView(applocationId);
                const htmlElement = document.getElementById("refreshConfirmApplicationDetails");
                if(htmlElement){
                    htmlElement.click();
                }
              }else if(Object.keys(res).length > 1){
                this.props.viewState.showApplicationLotNumberPanelView(res);
              }
          }).catch(error => {
              console.error('処理に失敗しました', error);
              alert('処理に失敗しました');
          });
      }
    },

    /**
     * 申請地レイヤーの表示
     * @param {object} 申請地情報
     */
    showLayers(lotNumbers) {
      try{
          const wmsUrl = Config.config.geoserverUrl;
          const items = this.props.terria.workbench.items;
          let layerFlg = false;
          for (const aItem of items) {
              if (aItem.uniqueId === Config.layer.lotnumberSearchLayerNameForApplicationSearchTarget) {
                  aItem.setTrait(CommonStrata.user,
                      "parameters",
                      {
                          "viewparams": Config.layer.lotnumberSearchViewParamNameForApplicationSearchTarget + Object.keys(lotNumbers)?.map(key => { return lotNumbers[key].chibanId }).filter(chibanId => { return chibanId !== null }).join("_"),
                      });
                  aItem.loadMapItems();
                  layerFlg = true;
              }
          }

          this.focusMapPlaceDriver(lotNumbers);

          if(!layerFlg){
              const item = new WebMapServiceCatalogItem(Config.layer.lotnumberSearchLayerNameForApplicationSearchTarget, this.props.terria);
              item.setTrait(CommonStrata.definition, "url", wmsUrl);
              item.setTrait(CommonStrata.user, "name", Config.layer.lotnumberSearchLayerDisplayNameForApplicationSearchTarget);
              item.setTrait(
                  CommonStrata.user,
                  "layers",
                  Config.layer.lotnumberSearchLayerNameForApplicationSearchTarget);
              item.setTrait(CommonStrata.user,
                  "parameters",
                  {
                      "viewparams": Config.layer.lotnumberSearchViewParamNameForApplicationSearchTarget + Object.keys(lotNumbers)?.map(key => { return lotNumbers[key].chibanId }).filter(chibanId => { return chibanId !== null }).join("_"),
                  });
              item.loadMapItems();
              this.props.terria.workbench.add(item);
          }
      } catch (error) {
          console.error('処理に失敗しました', error);
      }
    },

        /**
     * フォーカス処理ドライバー
     * @param {object} 申請地情報
     */
        focusMapPlaceDriver(lotNumbers) {
          let applicationPlace = Object.values(lotNumbers);
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
      },
  
      /**
       * フォーカス処理
       * @param {number} maxLon 最大経度
       * @param {number} maxLat 最大緯度
       * @param {number} minLon 最小経度
       * @param {number} minLat 最小緯度
       * @param {number} lon 経度
       * @param {number} lat 緯度
       */
      outputFocusMapPlace(maxLon, maxLat, minLon, minLat, lon, lat) {
          // 3dmodeにセット
          this.props.viewState.set3dMode();
          //現在のカメラ位置等を取得
          const currentSettings = getShareData(this.props.terria, this.props.viewState);
          const currentCamera = currentSettings.initSources[0].initialCamera;
          let newCamera = Object.assign(currentCamera);
          //新規の表示範囲を設定
          let currentLonDiff = Math.abs(maxLon - minLon);
          let currentLatDiff = Math.abs(maxLat - minLat);
          newCamera.north = maxLon + currentLatDiff / 2;
          newCamera.south = minLon - currentLatDiff / 2;
          newCamera.east = maxLat + currentLonDiff / 2;
          newCamera.west = minLat - currentLonDiff / 2;
          //camera.positionを緯度経度に合わせて設定
          const scene = this.props.terria.cesium.scene;
          const terrainProvider = scene.terrainProvider;
          const positions = [Cartographic.fromDegrees(lon, minLat)];
          let height = 0;
          sampleTerrainMostDetailed(terrainProvider, positions).then((updatedPositions) => {
              height = updatedPositions[0].height
              let coord_wgs84 = Cartographic.fromDegrees(lon, minLat, parseFloat(height) + parseInt((400000 * currentLatDiff )) + 200 );
              let coord_xyz = Ellipsoid.WGS84.cartographicToCartesian(coord_wgs84);
              newCamera.position = { x: coord_xyz.x, y: coord_xyz.y, z: coord_xyz.z - parseInt((300000 * currentLatDiff )) - 170 };
              //カメラの向きは統一にさせる
              newCamera.direction = { x: this.props.terria.focusCameraDirectionX, y: this.props.terria.focusCameraDirectionY, z: this.props.terria.focusCameraDirectionZ };
              newCamera.up = { x: this.props.terria.focusCameraUpX, y: this.props.terria.focusCameraUpY, z:this.props.terria.focusCameraUpZ };
              this.state.terria.currentViewer.zoomTo(newCamera, 5);
          })
    },

    render() {
      const { t } = this.props;
      // Merge theme in order of highest priority: themeOverrides props -> theme config parameter -> default terriaTheme
      const mergedTheme = combine(
        this.props.themeOverrides,
        combine(this.props.terria.configParameters.theme, terriaTheme, true),
        true
      );
      const theme = mergedTheme;

      const customElements = processCustomElements(
        this.props.viewState.useSmallScreenInterface,
        this.props.children
      );

      const terria = this.props.terria;
      const allBaseMaps = this.props.allBaseMaps;

      const showStoryBuilder =
        this.props.viewState.storyBuilderShown &&
        !this.shouldUseMobileInterface();
      const showStoryPanel =
        this.props.terria.configParameters.storyEnabled &&
        this.props.terria.stories.length > 0 &&
        this.props.viewState.storyShown &&
        !this.props.viewState.explorerPanelIsVisible &&
        !this.props.viewState.storyBuilderShown;
      return (
        <ThemeProvider theme={mergedTheme}>
          <GlobalTerriaStyles
            experimentalFeatures={
              this.props.terria.configParameters.experimentalFeatures
            }
          />
          <TourPortal terria={terria} viewState={this.props.viewState} />
          <CollapsedNavigation
            terria={terria}
            viewState={this.props.viewState}
          />
          <SatelliteHelpPrompt
            terria={terria}
            viewState={this.props.viewState}
          />
          <div className={Styles.storyWrapper}>
            {/* <If condition={!this.props.viewState.disclaimerVisible}>
              <WelcomeMessage viewState={this.props.viewState} />
            </If> */}
            <div
              className={classNames(Styles.uiRoot, {
                [Styles.withStoryBuilder]: showStoryBuilder
              })}
              css={`
                ${this.props.viewState.disclaimerVisible &&
                `filter: blur(10px);`}
              `}
              ref={w => (this._wrapper = w)}
            >
              <div className={Styles.ui}>
                <Medium>
                  <DesktopHeader
                    terria={this.props.terria}
                    version={this.props.version}
                    viewState={this.props.viewState}
                    allBaseMaps={this.props.allBaseMaps}
                    customElements={customElements}
                    animationDuration={animationDuration}
                  />
                </Medium>
                <div className={Styles.uiInner}>
                  <If condition={!this.props.viewState.hideMapUi}>
                    <Small>
                      <MobileHeader
                        terria={terria}
                        menuItems={customElements.menu}
                        menuLeftItems={customElements.menuLeft}
                        viewState={this.props.viewState}
                        version={this.props.version}
                        allBaseMaps={allBaseMaps}
                      />
                    </Small>
                    <Medium>
                      <If condition={ this.props.viewState.showSidePanel || this.props.terria.authorityJudgment()}>
                      <div id="SidePanel"
                        className={classNames(
                          Styles.sidePanel,
                          this.props.viewState.topElement === "SidePanel"
                            ? "top-element"
                            : "",
                          {
                            [Styles.sidePanelHide]: this.props.viewState
                              .isMapFullScreen
                          }
                        )}
                        tabIndex={0}
                        onClick={action(() => {
                          this.props.viewState.topElement = "SidePanel";
                        })}
                        // TODO: debounce/batch
                        onTransitionEnd={() =>
                          this.props.viewState.triggerResizeEvent()
                        }
                      >
                        <SidePanel
                          terria={terria}
                          viewState={this.props.viewState}
                        />
                      </div>
                      </If>
                    </Medium>
                  </If>
                  <Medium>
                    <div
                      className={classNames(Styles.showWorkbenchButton, {
                        [Styles.showWorkbenchButtonTrainerBarVisible]: this
                          .props.viewState.trainerBarVisible,
                        [Styles.showWorkbenchButtonisVisible]: this.props
                          .viewState.isMapFullScreen,
                        [Styles.showWorkbenchButtonisNotVisible]: !this.props
                          .viewState.isMapFullScreen
                      })}
                    >
                      <FullScreenButton
                        terria={this.props.terria}
                        viewState={this.props.viewState}
                        minified={false}
                        btnText={t("sui.showWorkbench")}
                        animationDuration={animationDuration}
                        elementConfig={this.props.terria.elements.get(
                          "show-workbench"
                        )}
                      />
                    </div>
                  </Medium>

                  <section className={Styles.map}>
                    <ProgressBar terria={terria} />
                    <MapColumn
                      terria={terria}
                      viewState={this.props.viewState}
                      customFeedbacks={customElements.feedback}
                      customElements={customElements}
                      allBaseMaps={allBaseMaps}
                      animationDuration={animationDuration}
                    />
                    <main>
                      {/* <ExplorerWindow
                        terria={terria}
                        viewState={this.props.viewState}
                      /> */}
                      <If
                        condition={
                          this.props.terria.configParameters
                            .experimentalFeatures &&
                          !this.props.viewState.hideMapUi
                        }
                      >
                        <ExperimentalFeatures
                          terria={terria}
                          viewState={this.props.viewState}
                          experimentalItems={customElements.experimentalMenu}
                        />
                      </If>
                    </main>
                  </section>
                  {this.props.terria.configParameters.storyEnabled &&
                    showStoryBuilder && (
                      <StoryBuilder
                        isVisible={showStoryBuilder}
                        terria={terria}
                        viewState={this.props.viewState}
                        animationDuration={animationDuration}
                      />
                    )}
                </div>
              </div>

              <If condition={!this.props.viewState.hideMapUi}>
                <Medium>
                  <TrainerBar
                    terria={terria}
                    viewState={this.props.viewState}
                  />
                </Medium>
              </If>

              <Medium>
                {/* I think this does what the previous boolean condition does, but without the console error */}
                <If condition={this.props.viewState.isToolOpen}>
                  <Tool
                    {...this.props.viewState.currentTool}
                    viewState={this.props.viewState}
                    t={t}
                  />
                </If>
              </Medium>

              <If condition={this.props.viewState.panel}>
                {this.props.viewState.panel}
              </If>

              <Notification viewState={this.props.viewState} />
              <MapInteractionWindow
                terria={terria}
                viewState={this.props.viewState}
              />

              <If
                condition={
                  !customElements.feedback.length &&
                  this.props.terria.configParameters.feedbackUrl &&
                  !this.props.viewState.hideMapUi &&
                  this.props.viewState.feedbackFormIsVisible
                }
              >
                <FeedbackForm viewState={this.props.viewState} />
              </If>

              {/* マップクリックして属性情報表示処理 */}
              {/* <div
                className={classNames(
                  Styles.featureInfo,
                  this.props.viewState.topElement === "FeatureInfo"
                    ? "top-element"
                    : "",
                  {
                    [Styles.featureInfoFullScreen]: this.props.viewState
                      .isMapFullScreen
                  }
                )}
                tabIndex={0}
                onClick={action(() => {
                  this.props.viewState.topElement = "FeatureInfo";
                })}
              >
                <FeatureInfoPanel
                  terria={terria}
                  viewState={this.props.viewState}
                />
              </div> */}
              <DragDropFile
                terria={this.props.terria}
                viewState={this.props.viewState}
              />
              <DragDropNotification viewState={this.props.viewState} />
              {showStoryPanel && (
                <StoryPanel terria={terria} viewState={this.props.viewState} />
              )}
            </div>
            {this.props.viewState.showHelpMenu &&
              this.props.viewState.topElement === "HelpPanel" && (
                <HelpPanel terria={terria} viewState={this.props.viewState} />
              )}
            <div style={{ position: "absolute", left: -99999 + "px" }} id="getApplication" onClick={evt => {
                evt.preventDefault();
                evt.stopPropagation();
                this.getApplication();
            }}></div>
            {/* {this.props.viewState.showLotNumberSearch &&(
              <LotNumberSearch terria={terria} viewState={this.props.viewState} />
            )} */}
            {/* {this.props.viewState.showGeneralConditionDiagnosis && !this.props.terria.authorityJudgment() &&(
              <GeneralConditionDiagnosis terria={terria} viewState={this.props.viewState} />
            )} */}

            {/* {this.props.viewState.showApplicationCategorySelection && !this.props.terria.authorityJudgment() &&(
              <ApplicationCategorySelection terria={terria} viewState={this.props.viewState} />
            )} */}
            {this.props.viewState.showCharacterSelection && !this.props.terria.authorityJudgment() &&(
              <CharacterSelection terria={terria} viewState={this.props.viewState} />
            )}
            {this.props.viewState.showMapSelection && !this.props.terria.authorityJudgment() &&(
              <MapSelection terria={terria} viewState={this.props.viewState} />
            )}
            {/* {this.props.viewState.showEnterApplicantInformation && !this.props.terria.authorityJudgment() &&(
              <EnterApplicantInformation terria={terria} viewState={this.props.viewState} />
            )}
            {this.props.viewState.showUploadApplicationInformation && !this.props.terria.authorityJudgment() &&(
              <UploadApplicationInformation terria={terria} viewState={this.props.viewState} />
            )}
            {this.props.viewState.showConfirmApplicationDetails && !this.props.terria.authorityJudgment() &&(
              <ConfirmApplicationDetails terria={terria} viewState={this.props.viewState} />
            )}*/}
            {this.props.viewState.showCustomMessage &&(
              <CustomMessage terria={terria} viewState={this.props.viewState} />
            )}
            {/* {this.props.viewState.showAnswerLogin && !this.props.terria.authorityJudgment() &&(
              <AnswerLogin terria={terria} viewState={this.props.viewState} />
            )} */}
            {/* {this.props.viewState.showAnswerContent && !this.props.terria.authorityJudgment() &&(
              <AnswerContent terria={terria} viewState={this.props.viewState} />
            )} */}
            {this.props.viewState.showUserAgreement && !this.props.terria.authorityJudgment() &&(
              <UserAgreement terria={terria} viewState={this.props.viewState} />
            )}

            {this.props.viewState.showApplicationInformationSearch && this.props.terria.authorityJudgment() &&(
              <ApplicationInformationSearch terria={terria} viewState={this.props.viewState} />
            )}

            {this.props.viewState.showApplicationDetails && this.props.terria.authorityJudgment() &&(
              <ApplicationDetails terria={terria} viewState={this.props.viewState} />
            )}

            {this.props.viewState.showAnswerInput && this.props.terria.authorityJudgment() &&(
              <AnswerInput terria={terria} viewState={this.props.viewState} />
            )}

            {this.props.viewState.showApplicationList && this.props.terria.authorityJudgment() &&(
              <ApplicationList terria={terria} viewState={this.props.viewState} />
            )}

            {this.props.viewState.showApplicationLotNumberPanel && this.props.terria.authorityJudgment() &&(
              <ApplicationLotNumberPanel terria={terria} viewState={this.props.viewState} />  
            )}

            {(this.props.viewState.adminTabActive === "applySearch" && 
              (this.props.viewState.applyPageActive === "applyDetail" 
              || this.props.viewState.applyPageActive === "answerRegister") &&
              !this.props.viewState.showPdfViewer) && (
              <MapFitButton terria={terria} viewState={this.props.viewState} />
            )}
            {(!this.props.terria.authorityJudgment() && 
              (this.props.viewState.showInputApplyConditionView
                 || this.props.viewState.showGeneralAndRoadJudgementResultView
                 || this.props.viewState.showApplyInformationView
                 || this.props.viewState.showConfirmAnswerInformationView)) && (
              <MapFitButton terria={terria} viewState={this.props.viewState} />
            )}
            {((this.props.viewState.adminTabActive === "applySearch" && this.props.viewState.applyPageActive === "chat") 
            || (!this.props.terria.authorityJudgment() && this.props.viewState.showChatView)) && (
              <MapFitButtonForAChatView terria={terria} viewState={this.props.viewState} />
            )}

            <Disclaimer viewState={this.props.viewState} />
          </div>
          {this.props.viewState.printWindow && (
            <PrintView
              window={this.props.viewState.printWindow}
              terria={terria}
              viewState={this.props.viewState}
              closeCallback={() => this.props.viewState.setPrintWindow(null)}
            />
          )}
        </ThemeProvider>
      );
    }
  })
);

export const StandardUserInterfaceWithoutTranslation = StandardUserInterface;

export default withFallback(withTranslation()(StandardUserInterface));
