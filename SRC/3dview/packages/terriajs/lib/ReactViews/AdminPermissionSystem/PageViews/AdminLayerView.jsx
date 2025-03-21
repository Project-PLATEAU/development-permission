import React from "react";
import PropTypes from "prop-types";
import { observer } from "mobx-react";
import { withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import ShowMessage from "../Views/Message/ShowMessage";
import AdminTab from "../Views/Tab/AdminTab";
import LayerTab from "../../DevelopmentPermissionSystem/Views/layer/LayerTab";
import Box from "../../../Styled/Box";
import Styles from "../../DevelopmentPermissionSystem/PageViews/scss/pageStyle.scss";
import CustomStyle from "./scss/custumStyle.scss";
import NotificationList from "../Views/Apply/NotificationList";

/**
 * 行政画面：レイヤ表示
 */
@observer
class AdminLayerView extends React.Component {
    static displayName = "AdminLayerView"
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
            height: 800
        }
    }


    render(){
        const t = this.props.t;
        let tabAreaHeight = this.state.height - 330;
        if(!this.props.viewState.showLotNumberSelected){
            tabAreaHeight = tabAreaHeight -330;
        }
        let inquiries = this.state.inquiries;
        let answers = this.state.answers;
        return (
            <>
                <div style={{width: "100%", margin: "0"}}>
                    <ShowMessage t={t} message={"infoMessage.tipsForLayerTab"} />
                    <AdminTab terria={this.props.terria} viewState={this.props.viewState} t={t}/>
                </div>
                    
                <div style={{height: "calc(100vh - 160px)" ,overflowY: "auto"}}>
                    <div className={Styles.tab_div_area}  id="tabArea" 
                        style={{height: tabAreaHeight + "px"}}
                    >
                        <LayerTab terria={this.props.terria} viewState={this.state.viewState}  tabAreaHeight = {tabAreaHeight} />
                    </div>

                    <NotificationList terria={this.props.terria} viewState={this.props.viewState} t={t} referrer={"layershow"}/>

                </div>
            </>
        );
    };
}
export default withTranslation()(withTheme(AdminLayerView));