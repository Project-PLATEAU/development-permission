import React from "react";
import PropTypes from "prop-types";
import { observer } from "mobx-react";
import { withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import Box from "../../../Styled/Box";
import { TextSpan } from "../../../Styled/Text";
import Styles from "../../DevelopmentPermissionSystem/PageViews/scss/pageStyle.scss";
import DataCatalog from "../../DataCatalog/DataCatalog";
import ShowMessage from "../Views/Message/ShowMessage";
import AdminTab from "../Views/Tab/AdminTab";
import ApplicationInformationSearch from "../Views/Apply/ApplicationInformationSearch";

/**
 * 行政用画面：サイドパネル
 */
@observer
class AdminApplySearchView extends React.Component {
    static displayName = "AdminApplySearchView"
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
        this.state.viewState.changeAdminTabActive(active);
    }

    render(){
        const t = this.props.t;
        return(
            <>
                <div>
                    <ShowMessage t={t} message={"adminInfoMessage.tipsForApplySearch"} />                    
                    <AdminTab terria={this.props.terria} viewState={this.props.viewState} t={t}/>
                </div>
                
                <div className={Styles.div_area} style={{height: "calc(100vh - 160px)" ,overflowY: "auto"}}>
                    <ApplicationInformationSearch terria={this.props.terria} viewState={this.props.viewState} />
                </div>
            </>
        );
    };
}
export default withTranslation()(withTheme(AdminApplySearchView));