import { observer } from "mobx-react";
import PropTypes from "prop-types";
import React from "react";
import { withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import Icon, { StyledIcon } from "../../Styled/Icon";
import Spacing from "../../Styled/Spacing";
import Text from "../../Styled/Text";
import Checkbox from "../../Styled/Checkbox";
import Input from "../../Styled/Input";
import Box from "../../Styled/Box";
import Button, { RawButton } from "../../Styled/Button";
import Select from "../../Styled/Select";
import CustomStyle from "./scss/application-completed.scss";
import CommonStrata from "../../Models/Definition/CommonStrata";
import webMapServiceCatalogItem from '../../Models/Catalog/Ows/WebMapServiceCatalogItem';
import { useState } from 'react';
import Config from "../../../customconfig.json";

/**
 * カスタムメッセージ表示コンポーネント
 * （申請完了画面、回答登録完了画面、回答完了通知画面）
 */
@observer
class CustomMessage extends React.Component {
    static displayName = "CustomMessage";
    static propTypes = {
        terria: PropTypes.object.isRequired,
        viewState: PropTypes.object.isRequired,
        theme: PropTypes.object,
        t: PropTypes.func.isRequired
    };
    constructor(props) {
        super(props);
        this.state = {
            viewState: props.viewState,
            terria: props.terria
        };
    }
    
    /**
     * 初期処理
     */
    componentDidMount() {
        const { isApplyCompleted, isAnswerCompleted } = this.props.viewState;
    
        // 申請完了の場合、事業者向けアンケート画面を開く
        if (isApplyCompleted) {
            if(Config.QuestionaryActived.ApplyCompletedView == "true"){
                this.openQuestionnaire(Config.config.questionnaireUrlForBusiness, "develop_quessionaire");
            }
        }
    
    }
    
    /**
     * カスタムメッセージ表示した数秒後、アンケート画面を開く
     * @param {*} url URL
     * @param {*} target ターゲット
     * @param {*} delay 遅延秒数（ミリ秒）
     */
    openQuestionnaire(url, target, delay = 3000) {
        setTimeout(() => {
            window.open(url, target);
        }, delay);
    }

    /**
     * 画面を閉じる
     */
    close(){
        document.getElementById("customBackgroundDiv").style.display = "none";
        if (this.props.viewState.isAnswerCompleted) {
            this.props.viewState.nextApplicationDetailsView(this.props.viewState.applicationInformationSearchForApplicationId);
        } 
        // 回答通知完了画面を閉じると、申請情報詳細リフレッシュ処理を行う
        if(this.props.viewState.isNotifiedCompleted){
            this.props.viewState.refreshConfirmApplicationDetails();
        }
    }

    render() {
        return (
            <>
            <div className={CustomStyle.customBackground} id="customBackgroundDiv"></div>
            <Box
                displayInlineBlock
                backgroundColor={this.props.theme.textLight}
                styledWidth={"600px"}
                overflow={"hidden"}
                css={`
                    position: fixed;
                    z-index: 99999;
                `}
                className={CustomStyle.custom_frame}
            >
                <Box position="absolute" paddedRatio={3} topRight>
                    <RawButton onClick={() => {
                        this.close();
                        this.props.viewState.hideCustomMessageView();
                    }}>
                        <StyledIcon
                            styledWidth={"16px"}
                            fillColor={this.props.theme.textLight}
                            opacity={"0.5"}
                            glyph={Icon.GLYPHS.closeLight}
                            css={`
                            cursor:pointer;
                            fill:#000000;
                          `}
                        />
                    </RawButton>
                </Box>
                <nav className={CustomStyle.custom_nuv}>&nbsp;</nav>
                <Box
                    centered
                    paddedHorizontally={5}
                    paddedVertically={10}
                    displayInlineBlock
                    className={CustomStyle.custom_content}
                >
                    <p className={CustomStyle.center}>
                        <h2 dangerouslySetInnerHTML={{ __html: this.props.viewState.customMessageTitle }}></h2>
                    </p>
                    <p className={CustomStyle.center}>
                        <span dangerouslySetInnerHTML={{ __html: this.props.viewState.customMessageContent }}></span>
                    </p>
                   
                </Box>
            </Box >
            </>
        );
    }
}
export default withTranslation()(withTheme(CustomMessage));