import React from "react";
import PropTypes from "prop-types";
import { observer } from "mobx-react";
import { withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import Box from "../../../../Styled/Box";
import Styles from "./scss/AdminTab.scss";

/**
 * 行政用コンポーネント：サイドパネルタブ
 */

@observer
class AdminTab extends React.Component {
    static displayName = "AdminTab"
    static propsType = {
        terria: PropTypes.object.isRequired,
        viewState: PropTypes.object.isRequired,
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
        this.state.viewState.changeAdminTabActive(active);
    }

    render(){
        const t = this.props.t;
        return(
            <>
            <div className={Styles.tab}>
                <button className={`${Styles.btn_baise_style} ${this.state.viewState.adminTabActive === "mapSearch" ? "": Styles.btn_gry}`}
                    onClick={evt => {
                        evt.preventDefault();
                        evt.stopPropagation();
                        this.changeActiveTab("mapSearch");
                    }} id="lotSearchBtn">
                    <span style={{fontWeight: "bold"}}>地図検索</span>
                </button>

                <button className={`${Styles.btn_baise_style} ${this.state.viewState.adminTabActive === "applySearch" ? "": Styles.btn_gry}`}
                    style={{ width: "30%"}}
                    onClick={evt => {
                        evt.preventDefault();
                        evt.stopPropagation();
                        this.changeActiveTab("applySearch");
                    }}>
                    <span>申請情報検索</span>
                </button>

                <button className={`${Styles.btn_baise_style} ${this.state.viewState.adminTabActive === "layershow" ? "": Styles.btn_gry}`}
                    style={{ width: "30%"}}
                    onClick={evt => {
                        evt.preventDefault();
                        evt.stopPropagation();
                        this.changeActiveTab("layershow");
                    }}>
                    <span>レイヤ</span>
                </button>              
            </div>

            </>

        )
    }
}
export default withTranslation()(withTheme(AdminTab));