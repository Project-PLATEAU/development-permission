import createReactClass from "create-react-class";
import { observer } from "mobx-react";
import PropTypes from "prop-types";
import React from "react";
import { withTranslation } from "react-i18next";
import styled, { withTheme } from "styled-components";
import Icon, { StyledIcon } from "../../Styled/Icon";
import FullScreenButton from "./FullScreenButton";
import { useRefForTerria } from "../Hooks/useRefForTerria";
import Box from "../../Styled/Box";
import Text from "../../Styled/Text";
import Button from "../../Styled/Button";
import InitAndLotNumberSearchView from "../DevelopmentPermissionSystem/PageViews/InitAndLotNumberSearchView.jsx";
import InputApplyConditionView from "../DevelopmentPermissionSystem/PageViews/InputApplyConditionView.jsx";
import GeneralAndRoadJudgementResultView from "../DevelopmentPermissionSystem/PageViews/GeneralAndRoadJudgementResultView.jsx";
import ApplyInformationView from "../DevelopmentPermissionSystem/PageViews/ApplyInformationView.jsx";
import ConfirmAnswerInformationView from "../DevelopmentPermissionSystem/PageViews/ConfirmAnswerInformationView.jsx";
import ChatView from "../DevelopmentPermissionSystem/PageViews/ChatView.jsx"
import AdminInitAndLotNumberSearchView from "../AdminPermissionSystem/PageViews/AdminInitAndLotNumberSearchView";
import AdminApplySearchView from "../AdminPermissionSystem/PageViews/AdminApplySearchView";
import ApplicationDetails from "../AdminPermissionSystem/Views/Apply/ApplicationDetails";
import AnswerInput from "../AdminPermissionSystem/Views/Apply/AnswerInput";
import AdminLayerView from "../AdminPermissionSystem/PageViews/AdminLayerView";
import AdminChatView from "../AdminPermissionSystem/PageViews/AdminChatView";

const BoxHelpfulHints = styled(Box)``;

const ResponsiveSpacing = styled(Box)`
  height: 110px;
  // Hardcoded px value, TODO: make it not hardcoded
  @media (max-height: 700px) {
    height: 3vh;
  }
`;

function EmptyWorkbench(props) {
  const t = props.t;
  const HelpfulHintsIcon = () => {
    return (
      <StyledIcon
        glyph={Icon.GLYPHS.bulb}
        styledWidth={"14px"}
        styledHeight={"14px"}
        light
        css={`
          padding: 2px 1px;
        `}
      />
    );
  };

  return (
    <Text large textLight>
      {/* Hardcoded top to 150px for now for very very small screens
          TODO: make it not hardcoded */}
      <Box
        column
        fullWidth
        justifySpaceBetween
        styledHeight={"calc(100vh - 150px)"}
      >
        <Box centered column>
          <ResponsiveSpacing />
          <Text large color={props.theme.textLightDimmed}>
            {t("emptyWorkbench.emptyArea")}
          </Text>
          <ResponsiveSpacing />
        </Box>
      </Box>
    </Text>
  );
}
EmptyWorkbench.propTypes = {
  t: PropTypes.func.isRequired,
  theme: PropTypes.object.isRequired
};

const SidePanelButton = React.forwardRef((props, ref) => {
  const { btnText, ...rest } = props;
  return (
    <Button
      primary
      ref={ref}
      renderIcon={props.children && (() => props.children)}
      textProps={{
        large: true
      }}
      {...rest}
    >
      {btnText ? btnText : ""}
    </Button>
  );
});
SidePanelButton.displayName = "SidePanelButton"; // for some reasons lint doesn't like not having this
SidePanelButton.propTypes = {
  btnText: PropTypes.string,
  children: PropTypes.node
};

const StyledSidePanelButton = styled(SidePanelButton)`
  border-radius: 4px;
`;

export const EXPLORE_MAP_DATA_NAME = "ExploreMapDataButton";
export const SIDE_PANEL_UPLOAD_BUTTON_NAME = "SidePanelUploadButton";

const SidePanel = observer(
  createReactClass({
    displayName: "SidePanel",

    propTypes: {
      terria: PropTypes.object.isRequired,
      viewState: PropTypes.object.isRequired,
      refForExploreMapData: PropTypes.object.isRequired,
      refForUploadData: PropTypes.object.isRequired,
      t: PropTypes.func.isRequired,
      theme: PropTypes.object.isRequired
    },

    render() {
      const { t, theme } = this.props;
      const addData = t("addData.addDataBtnText");
      const uploadText = t("models.catalog.upload");
      return (
        
        <Box column fullHeight>
          {/* ×ボタンがなくなる */}
          {/* <FullScreenButton
              terria={this.props.terria}
              viewState={this.props.viewState}
              minified={true}
              animationDuration={250}
              btnText={t("addData.btnHide")}
            /> */}
          <If condition={!this.props.terria.authorityJudgment()}>
            {/* 事業者用：初期画面 */}
            <If condition={this.props.viewState.showInitAndLotNumberSearchView}>
              <InitAndLotNumberSearchView terria={this.props.terria} viewState={this.props.viewState} t={t}  />
            </If>
            {/* 事業者用：申請条件入力画面 */}
            <If condition = {this.props.viewState.showInputApplyConditionView}>
              <InputApplyConditionView terria={this.props.terria} viewState={this.props.viewState} t={t} />
            </If>
            {/* 事業者用：概況診断・道路判定結果表示画面 */}
            <If condition = {this.props.viewState.showGeneralAndRoadJudgementResultView}>
              <GeneralAndRoadJudgementResultView terria={this.props.terria} viewState={this.props.viewState} t={t} />
            </If>
            {/* 事業者用：申請フォーム画面 */}
            <If condition = {this.props.viewState.showApplyInformationView}>
              <ApplyInformationView terria={this.props.terria} viewState={this.props.viewState} t={t} />
            </If>
            {/* 事業者用：回答確認画面 */}
            <If condition = {this.props.viewState.showConfirmAnswerInformationView}>
              <ConfirmAnswerInformationView terria={this.props.terria} viewState={this.props.viewState} t={t} />
            </If>
            {/* 事業者用：チャット画面 */}
            <If condition = {this.props.viewState.showChatView}>
              <ChatView terria={this.props.terria} viewState={this.props.viewState} t={t} />
            </If>
          </If>
          
          <If condition={this.props.terria.authorityJudgment()}>
            {/* 行政用：初期画面 */}
            <If condition={this.props.viewState.adminTabActive === "mapSearch"}>
              <AdminInitAndLotNumberSearchView terria={this.props.terria} viewState={this.props.viewState} t={t}  />
            </If>
            {/* 行政用：申請情報検索 */}
            <If condition={this.props.viewState.adminTabActive === "applySearch" && this.props.viewState.applyPageActive === "applyList"}>
              <AdminApplySearchView terria={this.props.terria} viewState={this.props.viewState} t={t}  />
            </If>
            {/* 行政用：申請情報詳細 */}
              <If condition={this.props.viewState.adminTabActive === "applySearch" && this.props.viewState.applyPageActive === "applyDetail"}>
              <ApplicationDetails terria={this.props.terria} viewState={this.props.viewState} t={t}  />
            </If>
            {/* 行政用：回答登録 */}
            <If condition={this.props.viewState.adminTabActive === "applySearch" && this.props.viewState.applyPageActive === "answerRegister"}>
              <AnswerInput terria={this.props.terria} viewState={this.props.viewState} t={t}/>
            </If>
            {/* 行政用：チャット画面 */}
            <If condition = {this.props.viewState.adminTabActive === "applySearch" && this.props.viewState.applyPageActive === "chat"}>
              <AdminChatView terria={this.props.terria} viewState={this.props.viewState} t={t} />
            </If>
            {/* 行政用：レイヤツリー */}
            <If condition={this.props.viewState.adminTabActive === "layershow"}>
              <AdminLayerView terria={this.props.terria} viewState={this.props.viewState} t={t}/>
            </If>
          </If>
        </Box>
      );
    }
  })
);

// Used to create two refs for <SidePanel /> to consume, rather than
// using the withTerriaRef() HOC twice, designed for a single ref
const SidePanelWithRefs = props => {
  const refForExploreMapData = useRefForTerria(
    EXPLORE_MAP_DATA_NAME,
    props.viewState
  );
  const refForUploadData = useRefForTerria(
    SIDE_PANEL_UPLOAD_BUTTON_NAME,
    props.viewState
  );
  return (
    <SidePanel
      {...props}
      refForExploreMapData={refForExploreMapData}
      refForUploadData={refForUploadData}
    />
  );
};
SidePanelWithRefs.propTypes = {
  viewState: PropTypes.object.isRequired
};

module.exports = withTranslation()(withTheme(SidePanelWithRefs));
