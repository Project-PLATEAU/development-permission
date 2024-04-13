import React from "react";
import PropTypes from "prop-types";
import { observer } from "mobx-react";
import { withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import Box from "../../../../Styled/Box";
import { TextSpan } from "../../../../Styled/Text";
import Styles from "../../../DevelopmentPermissionSystem/PageViews/scss/pageStyle.scss";
import DataCatalog from "../../../DataCatalog/DataCatalog";
import DataCatalogTab from "../../../ExplorerWindow/Tabs/DataCatalogTab";


/**
 * 行政用画面：サイドパネル
 */
@observer
class AdminSidePanelView extends React.Component {
    static displayName = "AdminSidePanelView"
    static propsType = {
        terria: PropTypes.object.isRequired,
        viewState: PropTypes.object.isRequired,
        items: PropTypes.array,
        onActionButtonClicked: PropTypes.func,
        t: PropTypes.func.isRequired
    }

    constructor(props) {
        super(props);
        this.state = {
            viewState: props.viewState,
            terria: props.terria,

        }
    }

    /**
     * アクティブタブの変更
     * @returns 
     */
    changeActiveTab(active){
        console.log(active);
        this.state.viewState.setAdminTabActive(active);
    }

    render(){
        const t = this.props.t;
        return(
            <>
            <Box column>
                <div className={Styles.div_area}>
                    <Box padded  className={Styles.text_area}>
                        <TextSpan textDark uppercase overflowHide overflowEllipsis>
                            {this.state.viewState.adminTabActive === "mapSearch" ? t("infoMessage.tipsForMapSearch") : ""}
                            {this.state.viewState.adminTabActive === "applySearch" && this.state.viewState.applyPageActive === "applyList" ? t("infoMessage.tipsForMapSearch") : ""}
                        </TextSpan>
                    </Box>

                    <Box padded>
                        <button className={`${Styles.btn_baise_style} ${this.state.viewState.adminTabActive === "mapSearch" ? "": Styles.btn_gry}`}
                            onClick={evt => {
                                evt.preventDefault();
                                evt.stopPropagation();
                                this.changeActiveTab("mapSearch");
                            }} id="lotSearchBtn">
                            <span>地図検索</span>
                        </button>

                        <button className={`${Styles.btn_baise_style} ${this.state.viewState.adminTabActive === "applySearch" ? "": Styles.btn_gry}`}
                            onClick={evt => {
                                evt.preventDefault();
                                evt.stopPropagation();
                                this.changeActiveTab("applySearch");
                            }}>
                            <span>申請情報検索</span>
                        </button>

                        <button className={`${Styles.btn_baise_style} ${this.state.viewState.adminTabActive === "layerTree" ? "": Styles.btn_gry}`}
                            onClick={evt => {
                                evt.preventDefault();
                                evt.stopPropagation();
                                this.changeActiveTab("layerTree");
                            }}>
                            <span>レイヤ</span>
                        </button>
                    </Box>
                    <Box padded>
                        <If condition = {this.props.viewState.adminTabActive === "mapSearch"}>
                        </If>
                        <If condition = {this.props.viewState.adminTabActive === "applySearch"}>
                        </If>
                        <If condition = {this.props.viewState.adminTabActive === "layerTree"}>
                            <DataCatalog
                                terria={this.props.terria}
                                viewState={this.props.viewState}
                                items={this.props.terria.catalog.group.memberModels}
                            />
                        </If>
                    </Box>
                </div>

            </Box>
            </>
        );
    };
}
export default withTranslation()(withTheme(AdminSidePanelView));