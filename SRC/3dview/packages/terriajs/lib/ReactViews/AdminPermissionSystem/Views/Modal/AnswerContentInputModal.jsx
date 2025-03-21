import React from "react";
import PropTypes from "prop-types";
import { observer } from "mobx-react";
import { withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import Box from "../../../../Styled/Box";
import Styles from "./scss/AnswerContentInputModal.scss"
import { RawButton } from "../../../../Styled/Button";
import Icon, { StyledIcon } from "../../../../Styled/Icon";
import Spacing from "../../../../Styled/Spacing";
import Config from "../../../../../customconfig.json";

/**
 * 行政用コンポーネント：回答テンプレート選択ダイアログ
 */

@observer
class AnswerContentInputModal extends React.Component {
    static displayName = "AnswerContentInputModal"
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
            answerContent:"",
            applyInfo:[],
            callback:props.viewState.callBackFunction
        }
        this.CallBackFunction = this.CallBackFunction.bind(this);
    }

    /**
     * 初期表示
     */
    componentDidMount() {

        // 申請情報一覧編集
        const applyAnswerForm = this.props.viewState.applyAnswerForm;
        let applyInfo = [];
        // 申請地番
        let lotNumberText = "";
        const lotNumber = applyAnswerForm.lotNumbers;
        Object.keys(lotNumber).map(key => {
            lotNumberText = lotNumberText + lotNumber[key].lot_numbers;
        });
        applyInfo.push({name:"申請地番",value:lotNumberText});

        // 申請区分(該当申請段階に対する申請区分)
        let applicationCategoryText = "";
        const checkedApplicationStepId = this.props.viewState.checkedApplicationStepId
        Object.keys(applyAnswerForm.applyAnswerDetails).map(key => {
            let applyAnswerDetailForm = applyAnswerForm.applyAnswerDetails[key];
            if(applyAnswerDetailForm.applicationStepId == checkedApplicationStepId){
                const checkedApplicationCategory = applyAnswerDetailForm.applicationCategories;
                Object.keys(checkedApplicationCategory).map(key => {
                    applicationCategoryText = applicationCategoryText + `${checkedApplicationCategory[key]?.title}：${checkedApplicationCategory[key]?.applicationCategory?.map(function (value) { return value.content }).join(",")}`;
                    applicationCategoryText = applicationCategoryText + `\n`;
                });
            }
        })
        applyInfo.push({name:"申請区分",value:applicationCategoryText});

        // 申請者氏名
        let applicantName = "";
        const applicantInformation = applyAnswerForm.applicantInformations;
        applicantName = applicantInformation[0]?.value + `\n`;
        applyInfo.push({name:"申請者氏名",value:applicantName});

        // 申請者情報
        let applicantInformationText = "";
        Object.keys(applicantInformation).map(key => {
            applicantInformationText = applicantInformationText + `${applicantInformation[key]?.name}：${applicantInformation[key]?.value}`;
            applicantInformationText = applicantInformationText + `\n`;
        });
        applyInfo.push({name:"申請者情報",value:applicantInformationText});


        // 回答内容
        let answerContent = this.props.viewState.answerTemplateTarget.answerContent;

        this.setState({applyInfo:applyInfo, answerContent:answerContent});
        this.draggable(document.getElementById('answerContentInputModalDrag'), document.getElementById('answerContentInputModal'));
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
     * コールバック関数
     * @param {*} target 回答
     * @param {*} text 回答内容
     */
    CallBackFunction(target, text) {
        this.state.callback(target, text);
    }

    /**
     * モーダルを閉じる
     * 
     */
    close(){
        const target = this.state.answer;
        let answerId = target.answerId;
        let answerContent = this.state.answerContent;
        this.CallBackFunction(target, answerContent);
        this.state.viewState.changeAnswerContentInputModalShow();
    }

    /**
     * 申請情報・回答テンプレートを回答内容に追加
     * ※カーソルの位置に追加。
     * @param {*} text 追加内容（申請情報/回答テンプレート）
     */
    addAnswerContent(text){

        // 回答内容の入力欄
        let answerTextarea = document.getElementById("answerContent");

        if (answerTextarea) {
            //カーソルの位置を基準に前後を分割して、その間に文字列を挿入
            let answerContent = answerTextarea.value.substr(0, answerTextarea.selectionStart)
                + text
                + answerTextarea.value.substr(answerTextarea.selectionStart);

            // 文字数チェック
            if(answerContent.length > Config.inputMaxLength.answerContent){
                alert(Config.inputMaxLength.answerContent + "文字以内で入力してください。");
                return;
            }
            answerTextarea.value = answerContent;
            this.setState({answerContent:answerTextarea.value});
        }
    }

    render(){
        // 回答テンプレート一覧
        const target = this.state.answer;
        const answerTemplates = target.answerTemplate;
        // 申請情報一覧
        const applyInfo = this.state.applyInfo;
        // 入力した回答内容
        let answerContent = this.state.answerContent;
        // 回答内容のテキストエリアのデフォルト内容
        const placeholderText = `テキスト入力で回答を入力できます。\nテンプレートを選択すると、カーソルが当たっている位置にテンプレートを差し込むことができます。`;
        const maxLength =  Config.inputMaxLength.answerContent;
        const inputBuffLength = 1;
        return (
            <div className={Styles.overlay}>
                <div className={Styles.modal}  id="answerContentInputModal">
                    <Box position="absolute" paddedRatio={3} topRight>
                        <RawButton onClick={() => { this.close(); }}>
                            <StyledIcon
                                styledWidth={"16px"}
                                fillColor={"#000"}
                                opacity={"0.5"}
                                glyph={Icon.GLYPHS.closeLight}
                                css={`cursor:pointer;`}
                            />
                        </RawButton>
                    </Box>
                    <nav className={Styles.custom_nuv} id="answerContentInputModalDrag">
                        回答入力
                    </nav>
                    <div className={Styles.container}>
                        <p>対象：<span style={{ fontWeight: "bold"}}>{target.judgementInformation?.title}</span></p>
                        <p>回答を入力してください。</p>
                        <p>テンプレートを選択で回答に埋め込むこともできます。</p>
                   
                        <div className={Styles.contentDiv}>
                            <div className={Styles.list_item}>
                                <textarea className={Styles.text_area}
                                    id="answerContent"
                                    type="text"
                                    maxLength={maxLength + inputBuffLength}
                                    value={answerContent}
                                    placeholder ={placeholderText}
                                    onChange={e => {
                                        if(e.target.value.length > maxLength){
                                            alert(maxLength+"文字以内で入力してください。");
                                            return;
                                        }
                                        this.setState({ answerContent: e.target.value });
                                    }}
                                />
                            </div>
                            <div className={Styles.list_item}>
                                <div className={Styles.table_scroll}>
                                    <table className={Styles.selection_table}>
                                        <thead className={Styles.table_header}>
                                            <tr key={`header-applyInfo}`}>
                                                <th style={{width: "80%"}}>申請情報</th>
                                                <th style={{width: "20%"}}></th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            {Object.keys(applyInfo).map(key => (
                                                <tr key={`body-applyInfo-${key}`}>
                                                    <td className={Styles.text_align_left}>{applyInfo[key].name}</td>
                                                    <td>
                                                        <button className={Styles.selected_button}
                                                            onClick={e => {this.addAnswerContent(applyInfo[key].value)}}
                                                        >
                                                            <span>選択</span>
                                                        </button>
                                                    </td>
                                                </tr>
                                            ))}
                                        </tbody>
                                    </table>
                                </div>
                                <Spacing bottom={2} />
                                <div className={Styles.table_scroll}>
                                    <table className={Styles.selection_table}>
                                        <thead className={Styles.table_header}>
                                            <tr>
                                                <th style={{width: "80%"}}>テンプレート</th>
                                                <th style={{width: "20%"}}></th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            {answerTemplates && Object.keys(answerTemplates).map(index => (
                                                <tr key={`body-template-${index}`}>
                                                    <td className={Styles.text_align_left}>
                                                        {answerTemplates[index]?.answerTemplateText}
                                                    </td>
                                                    <td>
                                                        <button className={Styles.selected_button}
                                                            onClick={e => {
                                                                this.addAnswerContent(answerTemplates[index]?.answerTemplateText);
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
                </div>
            </div>
        )
    }
}
export default withTranslation()(withTheme(AnswerContentInputModal));