import { observer } from "mobx-react";
import PropTypes from "prop-types";
import React from "react";
import {  withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import Box from "../../../Styled/Box";
import Styles from "./scss/pageStyle.scss";
import InputChatMessage from "../Views/Chat/InputChatMessage.jsx";
/**
 * 事業者用画面：問合せ画面　
 */
@observer
class ChatView extends React.Component {
    static displayName = "ChatView";
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
     * 初期処理
    */
    componentDidMount(){
        let applicationPlace = Object.assign({},this.props.viewState.answerContent.lotNumbers);
        applicationPlace = Object.values(applicationPlace);
        applicationPlace = applicationPlace.filter(Boolean);
        this.props.viewState.setLotNumbers(applicationPlace);
    }

    render() {
        const t = this.props.t;
        let infoMessage = t("infoMessage.tipsForInputMessage");
        return (
            <>
                <Box fullHeight column style={{overflowY:"auto" , overflowX: "hidden"}} id="ChatView" >
                    <div className={Styles.div_area} style={{height:"100%"}}>
                        <Box padded  className={Styles.text_area}>
                            <span dangerouslySetInnerHTML={{ __html: infoMessage }}></span>
                        </Box>
                    
                        <InputChatMessage terria={this.props.terria} viewState={this.props.viewState} />
                    </div>
                </Box>
            </>
        );
    };
}

export default withTranslation()(withTheme(ChatView));