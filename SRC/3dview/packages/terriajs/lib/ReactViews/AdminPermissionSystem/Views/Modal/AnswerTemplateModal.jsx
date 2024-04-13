import React from "react";
import PropTypes from "prop-types";
import { observer } from "mobx-react";
import { withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import Box from "../../../../Styled/Box";
import Styles from "./scss/AnswerTemplateModal.scss";
import { RawButton } from "../../../../Styled/Button";
import Icon, { StyledIcon } from "../../../../Styled/Icon";
import Config from "../../../../../customconfig.json";

/**
 * 行政用コンポーネント：回答テンプレート選択モーダル
 */

@observer
class AnswerTemplateModal extends React.Component {
    static displayName = "answerTemplateModal"
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
            answer: props.viewState.answerTemplateTarget,
            callback: this.props.callback
        }
        this.CallBackFunction = this.CallBackFunction.bind(this);
    }

    componentDidMount() {
        this.draggable(document.getElementById('answerTemplateModalDrag'), document.getElementById('answerTemplateModal'));
    }

    /**
     * コンポーネントドラッグ操作
     * @param {Object} ドラッグ操作対象
     * @param {Object} ドラッグ対象
     */
    draggable(target, content) {
        target.onmousedown = function () {
            document.onmousemove = mouseMove;
        };
        document.onmouseup = function () {
            document.onmousemove = null;
        };
        function mouseMove(e) {
            var event = e ? e : window.event;
            content.style.top = event.clientY + 10 +  'px';
            content.style.left = event.clientX - (parseInt(content.clientWidth) / 2) + 'px';
        }
    }
    /**
     * 回答テンプレート選択
     */
    select(text, target) {
        let answerId = target.answerId;
        this.CallBackFunction(answerId, text);
        this.close();
    }
    CallBackFunction(answerId, text) {
        this.state.callback(answerId, text);
    }
    /**
     * モーダルを閉じる
     * 
     */
    close(){
        this.state.viewState.changeAnswerTemplateModalShow();
    }
    render(){
        const target = this.state.answer;
        const answerTemplates = target.answerTemplate;
        console.log(answerTemplates);
        return (
            <div className={Styles.overlay}>
                <div className={Styles.modal}  id="answerTemplateModal">
                    <Box position="absolute" paddedRatio={3} topRight>
                        <RawButton onClick={() => {
                            this.close();
                        }}>
                            <StyledIcon
                                styledWidth={"16px"}
                                fillColor={"#000"}
                                opacity={"0.5"}
                                glyph={Icon.GLYPHS.closeLight}
                                css={`cursor:pointer;`}
                            />
                        </RawButton>
                    </Box>
                    <nav className={Styles.custom_nuv} id="answerTemplateModalDrag">
                        回答テンプレート
                    </nav>
                    <div className={Styles.container}>
                        <p>対象：<span style={{ fontWeight: "bold"}}>{target.judgementInformation?.title}</span></p>
                        <p>回答テンプレートを選択してください。</p>
                        <div style={{height: "230px", overflowY: "auto"}}>
                            <table className={Styles.selection_table}>
                                <thead className={Styles.table_header}>
                                    <tr>
                                        <th style={{width: "80%"}}>テンプレート</th>
                                        <th style={{width: "10%"}}></th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {Object.keys(target.answerTemplate).map(index => (
                                        <tr>    
                                            <td><div>{target.answerTemplate[index]?.answerTemplateText}</div></td>
                                            <td>
                                                <button className={Styles.download_button}
                                                    onClick={e => {
                                                        this.select(target.answerTemplate[index]?.answerTemplateText, target);
                                                    }}>
                                                    <span>選択</span>
                                                </button>
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    </div>

                </div>
            </div>

        )
    }



}
export default withTranslation()(withTheme(AnswerTemplateModal));