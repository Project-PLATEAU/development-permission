import React from "react";
import createReactClass from "create-react-class";
import PropTypes from "prop-types";
import "mutationobserver-shim";
import Config from "../../../customconfig.json";

import TerriaViewerWrapper from "../Map/TerriaViewerWrapper";
import DistanceLegend from "../Map/Legend/DistanceLegend";
// import FeedbackButton from "../Feedback/FeedbackButton";
import LocationBar from "../Map/Legend/LocationBar";
import MapNavigation from "../Map/Navigation/MapNavigation";
import MenuBar from "../Map/MenuBar";
import MapDataCount from "../BottomDock/MapDataCount";
// import defined from "terriajs-cesium/Source/Core/defined";
import FeatureDetection from "terriajs-cesium/Source/Core/FeatureDetection";
import BottomDock from "../BottomDock/BottomDock";
import classNames from "classnames";
import { withTranslation } from "react-i18next";
import Toast from "./Toast";
import Loader from "../Loader";
import Styles from "./map-column.scss";
import { observer } from "mobx-react";
import SlideUpFadeIn from "../Transitions/SlideUpFadeIn/SlideUpFadeIn";
import TerriaCesiumLogo from "./TerriaCesiumLogo";
import i18next from "i18next";
import { googleAnalyticsNotification } from "../Notification/googleAnalyticsNotification";
import Icon, { StyledIcon } from "../../Styled/Icon";
import { action, runInAction } from "mobx";
import { notification } from "../drag-drop-notification.scss";

const isIE = FeatureDetection.isInternetExplorer();
const chromeVersion = FeatureDetection.chromeVersion();

/**
 * Right-hand column that contains the map, controls that sit over the map and sometimes the bottom dock containing
 * the timeline and charts.
 *
 * Note that because IE9-11 is terrible the pure-CSS layout that is used in nice browsers doesn't work, so for IE only
 * we use a (usually polyfilled) MutationObserver to watch the bottom dock and resize when it changes.
 */
const MapColumn = observer(
  createReactClass({
    displayName: "MapColumn",

    propTypes: {
      terria: PropTypes.object.isRequired,
      viewState: PropTypes.object.isRequired,
      customFeedbacks: PropTypes.array.isRequired,
      allBaseMaps: PropTypes.array.isRequired,
      animationDuration: PropTypes.number.isRequired,
      customElements: PropTypes.object.isRequired,
      t: PropTypes.func.isRequired
    },

    getInitialState() {
      return {innerWidth:0,innerHeight:0};
    },

    /* eslint-disable-next-line camelcase */
    UNSAFE_componentWillMount() {
      if (isIE) {
        this.observer = new MutationObserver(this.resizeMapCell);
        window.addEventListener("resize", this.resizeMapCell, false);
      }
    },

    addBottomDock(bottomDock) {
      if (isIE) {
        this.observer.observe(bottomDock, {
          childList: true,
          subtree: true
        });
      }
    },

    newMapCell(mapCell) {
      if (isIE) {
        this.mapCell = mapCell;

        this.resizeMapCell();
      }
    },

    resizeMapCell() {
      if (this.mapCell) {
        this.setState({
          height: this.mapCell.offsetHeight
        });
      }
    },

    componentDidMount() {
      this.updateDimensions();
      this.props.viewState.setUpdateMapDimensions(()=>{this.updateDimensions(false);});
      window.addEventListener('resize', this.updateDimensions,false);
    },

    componentWillUnmount() {
      window.removeEventListener('resize', this.updateDimensionss,false);
      if (isIE) {
        window.removeEventListener("resize", this.resizeMapCell, false);
        this.observer.disconnect();
      }
    },

    showGoogleAnalyticsExplanation(e) {
      e.preventDefault();
      this.props.terria.notificationState.addNotificationToQueue({
        title: "Google Analytics の利用について",
        message: googleAnalyticsNotification
      });
    },

    showTerrainDataAttributes(e) {
      e.preventDefault();
      this.props.terria.notificationState.addNotificationToQueue({
        title: "地形データ",
        message:
          "基盤地図標高モデルから作成<br>（測量法に基づく国土地理院長承認（使用） R 3JHs 778）"
      });
    },

    showNotification(e, title, message) {
      e.preventDefault();
      this.props.terria.notificationState.addNotificationToQueue({
        title: title,
        message: message
      });
    },

    updateDimensions: function(resizeEvent=true) {
      try{
        if (resizeEvent && this.state.innerWidth === window.innerWidth && this.state.innerHeight === window.innerHeight) return;
        this.setState({innerWidth:window.innerWidth,innerHeight:window.innerHeight});
        if (this.props.viewState.isSidePanelFullScreen) {
          const mapBaseContainerElement = this.props.viewState.mapBaseContainerElement;
          const mapBaseElement = this.props.viewState.mapBaseElement;
          if(mapBaseContainerElement && mapBaseElement){
            if(this.props.viewState.mapExpansionFlag){
              const rect = mapBaseContainerElement.current.getBoundingClientRect();
              const width = rect.width;
              const height = rect.height;
              const top = rect.top;
              const left = rect.left;
              runInAction(() => {
                this.props.viewState.setMapBottom("");
                this.props.viewState.setMapRight("");
                this.props.viewState.setMapTop(top + "px");
                this.props.viewState.setMapLeft(left + "px");
                this.props.viewState.setMapWidth((width-10) + "px");
                this.props.viewState.setMapHeight((height-10) + "px");
              });
            }else{
              const rect = mapBaseElement.current.getBoundingClientRect();
              const width = rect.width;
              const height = rect.height;
              const top = rect.top;
              const left = rect.left;
              runInAction(() => {
                this.props.viewState.setMapBottom("");
                this.props.viewState.setMapRight("");
                this.props.viewState.setMapTop(top + "px");
                this.props.viewState.setMapLeft(left + "px");
                this.props.viewState.setMapWidth(width + "px");
                this.props.viewState.setMapHeight(height + "px");
              });
            }
            this.props.viewState.triggerResizeEvent();
          }
        }
      }catch(e){
        console.error(e);
        //エラーの場合はデフォ位置に配置
        runInAction(() => {
          this.props.viewState.setMapExpansionFlag(false);
          this.props.viewState.setMapBottom("");
          this.props.viewState.setMapRight("2vh");
          this.props.viewState.setMapTop("72vh");
          this.props.viewState.setMapLeft("");
          this.props.viewState.setMapWidth("21vw");
          this.props.viewState.setMapHeight("23vh");
        });
        this.props.viewState.triggerResizeEvent();
      }
    },
    
    render() {
      const { customElements } = this.props;
      // const { t } = this.props;
      // TODO: remove? see: https://bugs.chromium.org/p/chromium/issues/detail?id=1001663
      const isAboveChrome75 =
        chromeVersion && chromeVersion[0] && Number(chromeVersion[0]) > 75;
      const mapCellClass = classNames(Styles.mapCell, {
        [Styles.mapCellChrome]: isAboveChrome75
      });
      const isSidePanelFullScreen = this.props.viewState.isSidePanelFullScreen;
      const mapExpansionFlag = this.props.viewState.mapExpansionFlag;
      const isMapPartsShow = (!isSidePanelFullScreen) || (isSidePanelFullScreen && mapExpansionFlag);
      const notifications = Config.notifications;
      return (
        <div
          ref={(el) => (this.wrapperRef = el)} 
          className={classNames(Styles.mapInner, {
            [Styles.mapInnerChrome]: isAboveChrome75
          })}
        >
          <div className={Styles.mapRow}>
            <div
              className={classNames(mapCellClass, Styles.mapCellMap)}
              ref={this.newMapCell}
            >
              <If condition={!this.props.viewState.hideMapUi && isMapPartsShow}>
                <div
                  css={`
                    ${(this.props.viewState.explorerPanelIsVisible && 
                      (this.props.viewState.adminTabActive !== "layershow" && !this.props.viewState.showInitAndLotNumberSearchView) )&&
                      "opacity: 0.3;"}
                  `}
                >
                  {/* <MenuBar
                    terria={this.props.terria}
                    viewState={this.props.viewState}
                    allBaseMaps={this.props.allBaseMaps}
                    menuItems={customElements.menu}
                    menuLeftItems={customElements.menuLeft}
                    animationDuration={this.props.animationDuration}
                    elementConfig={this.props.terria.elements.get("menu-bar")}
                  /> */}
                  <MapNavigation
                    terria={this.props.terria}
                    viewState={this.props.viewState}
                    navItems={customElements.nav}
                    elementConfig={this.props.terria.elements.get(
                      "map-navigation"
                    )}
                  />
                </div>
              </If>
              <div
                id="terriaViewer"
                className={Styles.mapWrapper}
                style={{
                  height: this.state.height || (isIE ? "100vh" : "100%")
                }}
              >
                <TerriaViewerWrapper
                  terria={this.props.terria}
                  viewState={this.props.viewState}
                />
                <If condition={isSidePanelFullScreen}>
                  <button onClick={() => {
                    this.props.viewState.setMapExpansionFlag(!mapExpansionFlag);
                    this.updateDimensions(false);
                    }} css={`
                      z-index: 99999999999;
                      position: absolute;
                      background: none;
                      border: none;
                      width: 30px;
                      height: 30px;
                      ${!mapExpansionFlag &&
                       `
                      top: 15px;
                      left: 5px;
                      transform: rotate(-90deg);
                      `}
                      ${mapExpansionFlag &&
                        `
                       top: 5px;
                       left: 15px;
                       transform: rotate(90deg);
                       `}
                  `}>
                    <StyledIcon styledWidth={"12px"} fillColor={"white"} glyph={Icon.GLYPHS.externalLink} css={`
                      width: 30px;
                      height: 30px;
                  `}/>
                  </button>
                </If>
              </div>
              <If condition={!this.props.viewState.hideMapUi && isMapPartsShow}>
                <If condition={!isSidePanelFullScreen}>
                  <MapDataCount
                    terria={this.props.terria}
                    viewState={this.props.viewState}
                    elementConfig={this.props.terria.elements.get(
                      "map-data-count"
                    )}
                  />
                </If>
                <SlideUpFadeIn isVisible={this.props.viewState.isMapZooming}>
                  <Toast>
                    <Loader
                      message={this.props.t("toast.mapIsZooming")}
                      textProps={{
                        style: {
                          padding: "0 5px"
                        }
                      }}
                    />
                  </Toast>
                </SlideUpFadeIn>
                <If condition={!isSidePanelFullScreen}>
                  <div className={Styles.mapBottomBar}>
                    <a href="#" onClick={this.showGoogleAnalyticsExplanation}>
                      Google Analyticsの利用について
                    </a>
                    <a href="#" onClick={this.showTerrainDataAttributes}>
                      地形データ
                    </a>
                    {Object.keys(notifications).map(key => (
                      <a href="#" onClick={e => {this.showNotification(e, notifications[key].title, notifications[key].message)}}>
                        {notifications[key].tag}
                      </a>
                    ))}
                  </div>
                  <div className={Styles.locationDistance}>
                    <LocationBar
                      terria={this.props.terria}
                      mouseCoords={this.props.terria.currentViewer.mouseCoords}
                    />
                    <DistanceLegend terria={this.props.terria} />
                  </div>
                  <TerriaCesiumLogo
                    css={`
                      position: absolute;
                      right: 30px;
                      bottom: 35px;
                      @media (max-width: 770px) {
                        right: unset;
                        left: 30px;
                      }
                    `}
                  />
                </If>
              </If>
              {/* TODO: re-implement/support custom feedbacks */}
              {/* <If
                condition={
                  !this.props.customFeedbacks.length &&
                  this.props.terria.configParameters.feedbackUrl &&
                  !this.props.viewState.hideMapUi
                }
              >
                <div
                  className={classNames(Styles.feedbackButtonWrapper, {
                    [Styles.withTimeSeriesControls]: defined(
                      this.props.terria.timelineStack.top
                    )
                  })}
                >
                  <FeedbackButton
                    viewState={this.props.viewState}
                    btnText={t("feedback.feedbackBtnText")}
                  />
                </div>
              </If> */}

              <If
                condition={
                  this.props.customFeedbacks.length &&
                  this.props.terria.configParameters.feedbackUrl &&
                  !this.props.viewState.hideMapUi
                }
              >
                <For
                  each="feedbackItem"
                  of={this.props.customFeedbacks}
                  index="i"
                >
                  <div key={i}>{feedbackItem}</div>
                </For>
              </If>
            </div>
            <If condition={this.props.terria.configParameters.printDisclaimer}>
              <div className={classNames(Styles.mapCell, "print")}>
                <a
                  className={Styles.printDisclaimer}
                  href={this.props.terria.configParameters.printDisclaimer.url}
                >
                  {this.props.terria.configParameters.printDisclaimer.text}
                </a>
              </div>
            </If>
          </div>
          <If condition={!this.props.viewState.hideMapUi}>
            <div className={Styles.mapRow}>
              <div className={mapCellClass}>
                <BottomDock
                  terria={this.props.terria}
                  viewState={this.props.viewState}
                  domElementRef={this.addBottomDock}
                />
              </div>
            </div>
          </If>
        </div>
      );
    }
  })
);

export default withTranslation()(MapColumn);
