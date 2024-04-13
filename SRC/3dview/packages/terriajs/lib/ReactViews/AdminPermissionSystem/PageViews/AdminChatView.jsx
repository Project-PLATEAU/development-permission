import { observer } from "mobx-react";
import PropTypes from "prop-types";
import React from "react";
import {  withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import Box from "../../../Styled/Box";
import Styles from "../../DevelopmentPermissionSystem/PageViews/scss/pageStyle.scss";
import InputChatMessage from "../Views/Chat/InputChatMessage";
import ShowMessage from "../Views/Message/ShowMessage";

/** 行政用画面：チャット画面 */
@observer
class AdminChatView extends React.Component {
    static displayName = "AdminChatView";
    static propTypes = {
        terria: PropTypes.object.isRequired,
        viewState: PropTypes.object.isRequired,
        applicationPlace: PropTypes.object,
        theme: PropTypes.object,
        t: PropTypes.func.isRequired,
        backPage: PropTypes.string
    }

    constructor(props) {
        super(props);
        this.state = {
            viewState: props.viewState,
            terria: props.terria,
        };
    }

    render() {
        const t = this.props.t;
        console.log(this.props.backPage);
        // let infoMessage = t("adminMessage.tipsForAnswerNotice");
        return (
            <>
                <Box column style={{overflowY:"auto" , overflowX: "hidden"}} id="ApplyInformationView" >
                    <div className={Styles.div_area}>
                        {/* <Box padded  className={Styles.text_area}>
                            <span dangerouslySetInnerHTML={{ __html: infoMessage }}></span>
                        </Box> */}
                        <ShowMessage t={t} message={"adminInfoMessage.tipsForAnswerNotice"} />

                        <InputChatMessage terria={this.props.terria} viewState={this.props.viewState} backPage={this.props.backPage}/>
                    </div>
                </Box>
            </>
        );
    };
}

export default withTranslation()(withTheme(AdminChatView));