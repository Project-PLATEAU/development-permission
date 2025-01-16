import { observer } from "mobx-react";
import PropTypes from "prop-types";
import React from "react";
import {  withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import Box from "../../../Styled/Box";
import Styles from "./scss/pageStyle.scss";
import EnterApplicantInformation from "../Views/Apply/EnterApplicantInformation.jsx";
import UploadApplicationInformation from "../Views/Apply/UploadApplicationInformation.jsx";
import ConfirmApplicationDetails from "../Views/Apply/ConfirmApplicationDetails.jsx";
import EnterAddInformation from "../Views/Apply/EnterAddInformation.jsx";
/**
 * 事業者用画面：申請フォーム画面
 */
@observer
class ApplyInformationView extends React.Component {
    static displayName = "ApplyInformationView";
    static propTypes = {
        terria: PropTypes.object.isRequired,
        viewState: PropTypes.object.isRequired,
        applicationPlace: PropTypes.object,
        theme: PropTypes.object,
        t: PropTypes.func.isRequired
    }

    constructor(props) {
        super(props);
        this.state = {
            viewState: props.viewState,
            terria: props.terria,
        };
    }

    /**
     * 初期表示
     */
    componentDidMount(){
        let applicationPlace = Object.values(this.props.viewState.applicationPlace);
        applicationPlace = applicationPlace.filter(Boolean);
        this.props.viewState.setLotNumbers(applicationPlace);
    }

    render() {
        const t = this.props.t;
        let infoMessage = "";
        if(this.props.viewState.showEnterApplicantInformation){
            if(this.props.viewState.isReApply){

                infoMessage = t("infoMessage.tipsForInputAddInformation");
            }else{

                infoMessage = t("infoMessage.tipsForInputApplicationInformation");
            }
        }
        if(this.props.viewState.showUploadApplicationInformation) {
            infoMessage = t("infoMessage.tipsForRegistApplicationFile");
        }
        if(this.props.viewState.showConfirmApplicationDetails) {
            infoMessage = t("infoMessage.tipsForConfirmApplicationDetails");
        }
        return (
            <>
                <Box column style={{overflowY:"auto" , overflowX: "hidden"}} id="ApplyInformationView" >
                    <div className={Styles.div_area}>
                        <Box padded  className={Styles.text_area}>
                        <span dangerouslySetInnerHTML={{ __html: infoMessage }}></span>
                        </Box>
                    
                        <If condition = {this.props.viewState.showEnterApplicantInformation && !this.props.viewState.isReApply}>
                            <EnterApplicantInformation terria={this.props.terria} viewState={this.props.viewState} />
                        </If>
                        <If condition = {this.props.viewState.showUploadApplicationInformation}>
                            <UploadApplicationInformation terria={this.props.terria} viewState={this.props.viewState} />
                        </If>
                        <If condition = {this.props.viewState.showConfirmApplicationDetails}>
                            <ConfirmApplicationDetails terria={this.props.terria} viewState={this.props.viewState} />
                        </If>
                        <If condition = {this.props.viewState.showEnterApplicantInformation && this.props.viewState.isReApply}>
                            <EnterAddInformation terria={this.props.terria} viewState={this.props.viewState} />
                        </If>
                    </div>
                </Box>
            </>
        );
    };
}

export default withTranslation()(withTheme(ApplyInformationView));