import { TFunction } from "i18next";
import { debounce } from "lodash-es";
import {
  action,
  computed,
  IReactionDisposer,
  observable,
  reaction,
  runInAction
} from "mobx";
import { observer } from "mobx-react";
import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import styled, { DefaultTheme, withTheme } from "styled-components";
import isDefined from "../../../Core/isDefined";
import ViewState from "../../../ReactViewModels/ViewState";
import Box from "../../../Styled/Box";
import Icon, { GLYPHS } from "../../../Styled/Icon";
import MapNavigationModel, {
  IMapNavigationItem,
  OVERFLOW_ITEM_ID
} from "../../../ViewModels/MapNavigation/MapNavigationModel";
import withControlledVisibility from "../../HOCs/withControlledVisibility";
import MapIconButton from "../../MapIconButton/MapIconButton";
import { Control, MapNavigationItem } from "./Items/MapNavigationItem";
import { registerMapNavigations } from "./registerMapNavigations";
import ViewerMode, {
  MapViewers,
  setViewerMode
} from "../../../Models/ViewerMode";
import Button, { RawButton } from "../../../Styled/Button";
import {getShareData} from "../Panels/SharePanel/BuildShareLink";
import MapFitButton from "../../DevelopmentPermissionSystem/Views/Map/MapFitButton";
import MapFitButtonForAChatView from "../../DevelopmentPermissionSystem/Views/Map/MapFitButtonForAChatView";
import Config from "../../../../customconfig.json";
import { BaseModel } from "../../../Models/Definition/Model";
import CommonStrata from "../../../Models/Definition/CommonStrata";
import { borderRadius } from "react-select/src/theme";

const OVERFLOW_ACTION_SIZE = 42;

interface StyledMapNavigationProps {
  trainerBarVisible: boolean;
  theme: DefaultTheme;
}

/**
 * TODO: fix this so that we don't need to override pointer events like this.
 * a fix would look like breaking up the top and bottom parts, so there is
 * no element "drawn/painted" between the top and bottom parts of map
 * navigation
 */
const StyledMapNavigation = styled.div<StyledMapNavigationProps>`
  position: absolute;
  right: 5px;
  z-index: 1;
  bottom: 25px;
  @media (min-width: ${props => props.theme.sm}px) {
    top: 30px;
    bottom: 50px;
    right: 30px;
  }
  @media (max-width: ${props => props.theme.mobile}px) {
    & > div {
      flex-direction: row;
    }
  }
  pointer-events: none;

  button {
    pointer-events: auto;
  }

  ${p =>
    p.trainerBarVisible &&
    `
    top: ${Number(p.theme.trainerHeight) + Number(p.theme.mapNavigationTop)}px;
  `}
`;

const ControlWrapper = styled(Box)`
  @media (min-width: ${props => props.theme.sm}px) {
    & > :first-child {
      margin-top: 0 !important;
      padding-top: 0 !important;
    }
  }
`;

type IButtonProps = {
  isActive: boolean;
};

const SettingsButton = styled(Button)<IButtonProps>`
  background-color: ${props =>
    props.isActive ? props.theme.colorPrimary : "#ededed"};
  color: ${props => (props.isActive ? "#fff" : "#444444")};
  border: none;
  border-radius: 0;
  width:10px;
  min-height: 35px;
  padding: 0 14px;
`;

interface PropTypes extends WithTranslation {
  viewState: ViewState;
  theme: DefaultTheme;
  t: TFunction;
  navItems: any[];
}

enum Orientation {
  HORIZONTAL,
  VERTICAL
}

@observer
class MapNavigation extends React.Component<PropTypes> {
  static displayName = "MapNavigation";
  private navigationRef = React.createRef<HTMLDivElement>();
  private readonly resizeListener: () => any;
  private readonly viewState: ViewState;
  private itemSizeInBar: Map<string, number>;
  @observable private model: MapNavigationModel;
  @observable private overflows: boolean;
  private viewerModeReactionDisposer: IReactionDisposer | undefined;

  constructor(props: PropTypes) {
    super(props);
    registerMapNavigations(props.viewState);
    this.viewState = props.viewState;
    this.model = props.viewState.terria.mapNavigationModel;
    this.resizeListener = debounce(() => this.updateNavigation(), 250);
    this.itemSizeInBar = new Map<string, number>();
    this.computeSizes();
    this.overflows = runInAction(() =>
      this.model.visibleItems.some(item => item.controller.collapsed)
    );
    this.viewerModeReactionDisposer = reaction(
      () => this.viewState.terria.currentViewer,
      () => this.updateNavigation(),
      {
        equals: (a, b) => {
          return a === b;
        }
      }
    );
  }

  componentDidMount() {
    this.computeSizes();
    this.updateNavigation();
    window.addEventListener("resize", this.resizeListener, false);
    this.changeBuildingModelShow();
  }

  isTablet(){
    let ut = navigator.userAgent;
    if(ut.indexOf('iPhone') > 0 || ut.indexOf('iPod') > 0 || ut.indexOf('Android') > 0 && ut.indexOf('Mobile') > 0){
      return false; 
    }else if(ut.indexOf('iPad') > 0 || ut.indexOf('Android') > 0){
      return true;
    }else{
      return false; 
    }
  }

  /**
   * 建物モデル表示・非表示
   */
  changeBuildingModelShow(){
      try{
          const currentViewer =
          this.viewState.terria.mainViewer.viewerMode === ViewerMode.Cesium
            ? this.viewState.terria.mainViewer.viewerOptions.useTerrain
              ? "3d"
              : "3dsmooth"
            : "2d";
          const cesium3DTiles:any = this.viewState.terria.getModelById(BaseModel, Config.buildingModel.id);
          const leaflet2DModel:any = this.viewState.terria.getModelById(BaseModel, Config.buildingModelFor2d.id);
          if(currentViewer === '3d' && cesium3DTiles && leaflet2DModel){
            this.viewState.terria.workbench.remove(leaflet2DModel);
            this.viewState.terria.workbench.add(cesium3DTiles);
          }else if(currentViewer === '2d' && cesium3DTiles && leaflet2DModel){
            this.viewState.terria.workbench.remove(cesium3DTiles);
            this.viewState.terria.workbench.add(leaflet2DModel);
          }
      } catch (error) {
          console.error('処理に失敗しました', error);
      }
  }

  componentWillUnmount() {
    window.removeEventListener("resize", this.resizeListener);
    if (this.viewerModeReactionDisposer) {
      this.viewerModeReactionDisposer();
    }
  }

  @action
  selectViewer(
    viewer: keyof typeof MapViewers,
    event: any
  ) {
    const mainViewer = this.viewState.terria.mainViewer;
    event.stopPropagation();
    setViewerMode(viewer, mainViewer);
    if(this.viewState.terria.authorityJudgment()){
      this.viewState.terria.setLocalProperty("goverment.viewermode", viewer);
    }else{
      this.viewState.terria.setLocalProperty("business.viewermode", viewer);
    }
    this.changeBuildingModelShow();
    setTimeout(() => {
      this.viewState.terria.cameraReset();
    }, 2000);
  }

  @computed
  get orientation(): Orientation {
    return this.viewState.useSmallScreenInterface
      ? Orientation.HORIZONTAL
      : Orientation.VERTICAL;
  }

  @action
  private computeSizes(items?: IMapNavigationItem[]): void {
    (items ?? this.model.visibleItems).forEach(item => {
      if (this.orientation === Orientation.VERTICAL) {
        if (item.controller.height && item.controller.height > 0) {
          this.itemSizeInBar.set(item.id, item.controller.height || 42);
        }
      } else {
        if (item.controller.width && item.controller.width > 0) {
          this.itemSizeInBar.set(item.id, item.controller.width || 42);
        }
      }
    });
  }

  /**
   * Check if we need to collapse navigation items and determine which one need to be collapsed.
   */
  @action
  private updateNavigation(): void {
    if (!this.navigationRef.current) {
      // navigation bar has not been rendered yet so there is nothing to update.
      return;
    }
    if (this.computeSizes.length !== this.model.visibleItems.length) {
      this.computeSizes();
    }
    let itemsToShow = this.model.visibleItems.filter(item =>
      filterViewerAndScreenSize(item, this.viewState)
    );
    // items we have to show in the navigation bar
    let pinnedItems = this.model.pinnedItems.filter(item =>
      filterViewerAndScreenSize(item, this.viewState)
    );
    // items that are possible to be collapsed
    let possibleToCollapse = itemsToShow
      .filter(
        item => !pinnedItems.some(pinnedItem => pinnedItem.id === item.id)
      )
      .reverse();

    // Ensure we are not showing more composites than we have height for
    let overflows = false;
    let maxVisible = itemsToShow.length;
    let size = 0;
    if (this.overflows) {
      size += OVERFLOW_ACTION_SIZE;
    }
    const limit =
      this.orientation === Orientation.VERTICAL
        ? this.navigationRef.current.clientHeight
        : this.navigationRef.current.parentElement?.parentElement
        ? this.navigationRef.current.parentElement?.parentElement?.clientWidth -
          100
        : this.navigationRef.current.clientWidth;

    for (let i = 0; i < itemsToShow.length; i++) {
      size += this.itemSizeInBar.get(itemsToShow[i].id) || 0;
      if (size <= limit) {
        maxVisible = i + 1;
      }
    }

    if (pinnedItems.length > maxVisible) {
      possibleToCollapse.forEach(item => {
        this.model.setCollapsed(item.id, true);
      });
      //there is nothing else we can do, we have to show the rest of items as it is.
      return;
    }
    overflows = itemsToShow.length > maxVisible;
    const itemsToCollapseId: string[] = [];
    const activeCollapsible: string[] = [];
    if (overflows) {
      if (!this.overflows) {
        // overflow is not currently visible so add its height here
        size += OVERFLOW_ACTION_SIZE;
        this.overflows = true;
      }
      maxVisible = maxVisible - pinnedItems.length;
      // first try to collapse inactive items and then active ones if needed
      for (let i = 0; i < possibleToCollapse.length; i++) {
        const item = possibleToCollapse[i];
        if (item.controller.active) {
          activeCollapsible.push(item.id);
          continue;
        }
        itemsToCollapseId.push(item.id);
        size -= this.itemSizeInBar.get(item.id) || 0;
        if (size <= limit) {
          break;
        }
      }
      if (size > limit) {
        for (let i = 0; i < activeCollapsible.length; i++) {
          const itemId = activeCollapsible[i];
          itemsToCollapseId.push(itemId);
          size -= this.itemSizeInBar.get(itemId) || 0;
          if (size <= limit) {
            break;
          }
        }
      }
    } else {
      this.overflows = false;
    }

    this.model.visibleItems.forEach(item => {
      if (itemsToCollapseId.includes(item.id)) {
        this.model.setCollapsed(item.id, true);
      } else {
        this.model.setCollapsed(item.id, false);
      }
    });
  }

  render() {
    const { viewState, t } = this.props;
    const terria = viewState.terria;
    const currentViewer =
      terria.mainViewer.viewerMode === ViewerMode.Cesium
        ? terria.mainViewer.viewerOptions.useTerrain
          ? "3d"
          : "3dsmooth"
        : "2d";
    let items = terria.mapNavigationModel.visibleItems.filter(
      item =>
        !item.controller.collapsed &&
        filterViewerAndScreenSize(item, this.viewState)
    );
    let bottomItems: IMapNavigationItem[] | undefined;
    if (!this.overflows && this.orientation !== Orientation.HORIZONTAL) {
      bottomItems = items.filter(item => item.location === "BOTTOM");
      items = items.filter(item => item.location === "TOP");
    }

    items = items.filter(item => ["measure-tool"].indexOf(item.id) < 0);

    let isTablet = this.isTablet();
    return (
      <StyledMapNavigation trainerBarVisible={viewState.trainerBarVisible}>
        <Box
          centered
          column
          justifySpaceBetween
          fullHeight
          alignItemsFlexEnd
          ref={this.navigationRef}
        >
          <ControlWrapper
            column={this.orientation === Orientation.VERTICAL}
            css={`
              ${this.orientation === Orientation.HORIZONTAL &&
                `margin-bottom: 5px;
                flex-wrap: wrap;`}
            `}
          >
            {items.map(item => {
              // Do not expand in place for horizontal orientation
              // as it results in buttons overlapping and hiding neighboring buttons.
              if (
                item.id === "split-tool" &&
                terria.configParameters.disableSplitter
              ) {
                return null;
              }
              // 歩行者モードがなくなる
              if ( item.id === "pedestrian-mode" ) {
                return null;
              }
              // 自位置はタブレットモードのみで表示
              if(item.id === "my-location" && !isTablet ){
                return null;
              }
              return (
                <MapNavigationItem
                  expandInPlace={this.orientation !== Orientation.HORIZONTAL}
                  key={item.id}
                  item={item}
                  terria={terria}
                />
              );
            })}
            {this.overflows && (
              <Control key={OVERFLOW_ITEM_ID}>
                <MapIconButton
                  expandInPlace
                  iconElement={() => <Icon glyph={GLYPHS.moreItems} />}
                  title={t("mapNavigation.additionalToolsTitle")}
                  onClick={() =>
                    runInAction(() => {
                      viewState.showCollapsedNavigation = true;
                    })
                  }
                >
                  {t("mapNavigation.additionalTools")}
                </MapIconButton>
              </Control>
            )}
            <Control style={{width:"56px",display:"block"}}>
            {Object.entries(MapViewers).map(([key, viewerMode]) => {
              switch (key) {
                case '3d':
                  return <SettingsButton
                            key={key}
                            isActive={String(key) === String(currentViewer)}
                            onClick={(event: any) => this.selectViewer(key as any, event)}
                            title={"3D"}
                            style={{float:"left",borderRadius:"5px 0 0 5px"}}
                          >
                            3D
                          </SettingsButton>;
                case '2d':
                  return <SettingsButton
                            key={key}
                            isActive={String(key) === String(currentViewer)}
                            onClick={(event: any) => this.selectViewer(key as any, event)}
                            title={"2D"}
                            style={{float:"right",borderRadius:"0 5px 5px 0"}}
                          >
                            2D
                          </SettingsButton>;
                // 他のケースに対する処理
                default:
                  return <></>;
              }
            })}
            </Control>
            {(viewState.adminTabActive === "applySearch" && 
              (viewState.applyPageActive === "applyDetail" 
              || viewState.applyPageActive === "answerRegister") &&
              !viewState.showPdfViewer) && (
                <Control>
                  <MapFitButton terria={terria} viewState={viewState} />
                </Control>
            )}
            {(!terria.authorityJudgment() && 
              (viewState.showInputApplyConditionView
                 || viewState.showGeneralAndRoadJudgementResultView
                 || viewState.showApplyInformationView
                 || viewState.showConfirmAnswerInformationView)) && (
                <Control>
                  <MapFitButton terria={terria} viewState={viewState} />
                </Control>
            )}
            {((viewState.adminTabActive === "applySearch" && viewState.applyPageActive === "chat") 
            || (!terria.authorityJudgment() && viewState.showChatView)) && (
              <Control>
                <MapFitButtonForAChatView terria={terria} viewState={viewState} />
              </Control>
            )}
          </ControlWrapper>
          <ControlWrapper column={this.orientation === Orientation.VERTICAL}>
            {bottomItems?.map(item => (
              <MapNavigationItem key={item.id} item={item} terria={terria} />
            ))}
          </ControlWrapper>
        </Box>
      </StyledMapNavigation>
    );
  }
}

export default withTranslation()(
  withTheme(withControlledVisibility(MapNavigation))
);

export function filterViewerAndScreenSize(
  item: IMapNavigationItem,
  viewState: ViewState
) {
  const currentViewer = viewState.terria.mainViewer.viewerMode;
  if (viewState.useSmallScreenInterface) {
    return (
      (!isDefined(item.controller.viewerMode) ||
        item.controller.viewerMode === currentViewer) &&
      (!isDefined(item.screenSize) || item.screenSize === "small")
    );
  } else {
    return (
      (!isDefined(item.controller.viewerMode) ||
        item.controller.viewerMode === currentViewer) &&
      (!isDefined(item.screenSize) || item.screenSize === "medium")
    );
  }
}
