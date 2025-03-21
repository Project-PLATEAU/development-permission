import { observer } from "mobx-react";
import PropTypes from "prop-types";
import React from "react";
import { withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import Spacing from "../../../../Styled/Spacing";
import Box from "../../../../Styled/Box";
import CustomStyle from "./scss/content-list.scss";
import Icon, { StyledIcon } from "../../../../Styled/Icon";
import _isEqual from 'lodash/isEqual';
import AnswerContentInputModal from "../../../AdminPermissionSystem/Views/Modal/AnswerContentInputModal";
import Config from "../../../../../customconfig.json";

/**
 * 回答内容一覧コンポーネント
 */
@observer
class AnswerContentList extends React.Component {
    static displayName = "AnswerContentList";
    static propTypes = {
        terria: PropTypes.object.isRequired,
        viewState: PropTypes.object.isRequired,
        theme: PropTypes.object,
        t: PropTypes.func.isRequired,
        answers: PropTypes.array.isRequired,
        intervalID:PropTypes.number,
        clickAnswer:PropTypes.func.isRequired,
        selectedAnswerId:PropTypes.number,
        editable:PropTypes.bool,
        callback:PropTypes.func,
        applicationId: PropTypes.number
    }

    constructor(props) {
        super(props);
        this.state = {
            viewState: props.viewState,
            terria: props.terria,
            //回答一覧
            answers: props.answers,
            //選択された回答ID
            selectedAnswerId:props.selectedAnswerId,
            //編集可否
            editable: this.props.editable,
            //申請ID
            applicationId: props.applicationId,
            // 行政で追加された回答のindex(事前協議のみ)
            addAnswerIndex: props.addAnswerIndex,
            // 行政専用：ログインユーザー部署ID
            loginUserDepartmentId:"10001",
            // チャットボタンタイプ
            buttonType:1
        };
    }

    /**
     * 初期処理
     */
    componentDidMount() {
        let userId = "";
        let departmentId = "";
        let decoded = {};
        if (document.cookie) {
            let token = "";
            const cookiesArray = document.cookie.split(';');
            cookiesArray.forEach(data => {
                data = data.split('=');
                data[0] = data[0].trim(' ');
                if(data[0] === "token"){
                    token = data[1];
                }
            });
            if(token !== ""){
                const base64Url = token.split('.')[1];
                const base64 = decodeURIComponent(atob(base64Url).split('').map(function(c) {
                    return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
                }).join(''));
                decoded = JSON.parse(base64);
                if(decoded["X-USERID"]){
                    userId = decoded["X-USERID"];
                }
                if(decoded["X-DEPARTMENT"]){
                    departmentId = decoded["X-DEPARTMENT"];
                }
            }
        }
        this.setState({loginUserId: userId,loginUserDepartmentId: departmentId});
    }

    /**
     * props更新時
     */
    componentDidUpdate(prevProps) {
        if (!_isEqual(this.props.answers, prevProps.answers)) {
            this.setState({ answers:this.props.answers });
        }
        if (!_isEqual(this.props.selectedAnswerId, prevProps.selectedAnswerId)) {
            this.setState({ selectedAnswerId:this.props.selectedAnswerId });
        }
    }

    /**
     * 行政から未読問い合わせがあるか判定
     * @param {*} answer 回答情報
     * @returns 
     */
    getButtonType(answer){
        let buttonType = 1;
        const isEmpty = answer["chat"] == null || answer["chat"]["messages"] == null || answer["chat"]["messages"].length === 0;
        if(this.props.terria.authorityJudgment()){
            if(isEmpty){
                // 問い合わせ情報がない場合、チャットボタンを「吹き出し」アイコンで表示
                buttonType = 3;
            }else{
                let unReadInquiryAddressForm = null;
                let unReadMessage = null;
                for(let i=0;i<answer["chat"]["messages"].length;i++){
                    unReadInquiryAddressForm = answer["chat"]["messages"][i].inquiryAddressForms.find(inquiryAddressForm => inquiryAddressForm.department?.departmentId == this.state.loginUserDepartmentId && !inquiryAddressForm.readFlag);
                    if(unReadInquiryAddressForm){
                        unReadMessage = answer["chat"]["messages"][i];
                        break;
                    }
                }
                if((unReadInquiryAddressForm && unReadMessage && unReadMessage.messageType == 1) 
                    || (unReadInquiryAddressForm && unReadMessage && unReadMessage.messageType == 3)  ){
                    // チャットボタンを「吹き出し」アイコンで表示し、！マークをつける
                    buttonType = 2;
                }else{
                    // 上記以外は　チャットボタンを「吹き出し」アイコンで表示
                    buttonType = 3;
                }
            }
        }else{
            if(!answer["notifiedFlag"] && isEmpty){
                // 未通知かつ問い合わせ情報が無い場合表示しない
                buttonType = 1;
            }else if(answer["notifiedFlag"] && isEmpty){
                // 通知済みかつ問い合わせ情報がない場合、チャットボタンを「吹き出し」アイコンで表示
                buttonType = 3;
            }else{
                let size = answer["chat"]["messages"].length;
                let lastMessage = answer["chat"]["messages"][size - 1];
                if(lastMessage.messageType == 2 && !lastMessage.readFlag ){
                    // チャットボタンを「吹き出し」アイコンで表示し、！マークをつける
                    buttonType = 2;
                }else{
                    // 上記以外は　チャットボタンを「吹き出し」アイコンで表示
                    buttonType = 3;
                }
            }
        }
        return buttonType;
    }

     /**
      * 問合せ画面へ遷移
      * @param {*} e イベント
      * @param {*} answer 回答情報
      */
     moveToChatView(e, answer){
        if(this.props.intervalID){
            clearInterval(this.props.intervalID);
        }
        this.props.viewState.moveToChatView(this.props.applicationId,answer.applicationStep.applicationStepId,0,answer.answerId); 
    }

     /**
     * 回答内容一覧の行クリックイベント
     * @param {*} event イベント
     * @param {*} answerId 回答ID
     */
     clickAnswer(event, answerId){
        if(answerId!==undefined && answerId !== null && Number(answerId) > 0){
            this.setState({selectedAnswerId:answerId});
            this.props.clickAnswer(event, answerId, 0);
        }
    }

    /**
     * 回答入力モダール表示
     * @param {*} answer 回答対象
     */
    showTemplate(answer) {
        this.props.viewState.setAnswerTemplateTarget(answer);
        this.props.viewState.setCallBackFunction(this.inputFromTemplate);
        this.props.viewState.changeAnswerContentInputModalShow();
    }

    /**
     * 回答内容入力
     * @param {number} 対象回答のkey
     * @param {string} 入力された値
     * @param {number} 最大文字数
     */
    inputChange(key, value, maxLength) {
        // 文字数チェック
        if(value.length > maxLength ){
            alert(maxLength+"文字以内で入力してください。");
            return;
        }

        let answers = [...this.state.answers];
        answers[key]["answerContent"] = value;
        // 該当レコードが5:削除済みの場合、回答を編集すると、更新にリセット
        if(answers[key].answerDataType == "5"){
            answers[key].answerDataType = "1"
        }
        //親コンポーネント側の回答一覧を更新
        this.props.callback(answers);
    }

    /**
     * 区分選択（再申請要否）
     * @param {number} 対象回答のkey
     * @param {boolean} 入力された値 
     */
    reapplicationChange(key, value) {
        let answers = [...this.state.answers];
        answers[key]["reApplicationFlag"] = value;
        // 該当レコードが5:削除済みの場合、回答を編集すると、更新にリセット
        if(answers[key].answerDataType == "5"){
            answers[key].answerDataType = "1"
        }
        //親コンポーネント側の回答一覧を更新
        this.props.callback(answers);
    }

    /**
     * 区分選択（事前協議要否）
     * @param {number} 対象回答のkey
     * @param {boolean} 入力された値 
     */
    discussionFlagChange(key, value) {
        let answers = [...this.state.answers];
        answers[key]["discussionFlag"] = value;
        // 該当レコードが5:削除済みの場合、回答を編集すると、更新にリセット
        if(answers[key].answerDataType == "5"){
            answers[key].answerDataType = "1"
        }
        //親コンポーネント側の回答一覧を更新
        this.props.callback(answers);
    }
    
    /**
     * 回答内容を回答テンプレートで上書きする
     * @param {*} answer 回答
     * @param {*} text テキスト
     */
    inputFromTemplate = (answer, text) => {
        let answers = [...this.state.answers];
        const answerId = answer.answerId;
        for(let i = 0; i< answers.length; i++) {
            if (answers[i]["answerId"] == answerId) {
                answers[i]["answerContent"] = text;
                // 該当レコードが5:削除済みの場合、回答を編集すると、更新にリセット
                if(answers[i].answerDataType == "5"){
                    answers[i].answerDataType = "1"
                }
                break;
            }
        }
        //親コンポーネント側の回答一覧を更新
        this.props.callback(answers);
    }

    /**
     * 該当回答の担当部署名を取得
     * 複数の場合、カンマ区切りで表示
     * @param {*} answer 回答
     */
    getDepartmentName(answer){
        let departmentNames = [];
        let departments = answer.judgementInformation?.departments;
        Object.keys(departments).map(key2 => {
            if (departmentNames.indexOf(departments[key2].departmentName) < 0) {
                departmentNames.push(departments[key2].departmentName);
            }
        });

        return departmentNames?.join(",");
    }

    render() {
        const t = this.props.t;
        const answers = this.props.answers;
        const isChatView = this.props.viewState.showChatView;    
        const selectedAnswerId = this.state.selectedAnswerId;
        const editable = this.state.editable;
        const isAdmin = this.props.terria.authorityJudgment();
        const maxLength =  Config.inputMaxLength.answerContent;
        const inputBuffLength = 1;
        return (
            <Box
                centered
                displayInlineBlock
                className={CustomStyle.custom_content}
            >
                <Spacing bottom={3} />
                <div className={CustomStyle.scroll_container} id="AnswerContentListTable" style={{height: isAdmin?editable?"42vh":"47vh":"44vh"}} >
                    <table className={CustomStyle.selection_table}>
                        <thead>
                            <tr className={CustomStyle.table_header}>
                                <th style={{ width: "16%"}}>対象</th>
                                <th style={{ width: "12%"}}>回答課</th>
                                <th style={{ width: "16%"}}>判定結果</th>
                                <th style={{ width: "18%"}}>回答内容</th>
                                <th data-sort-column-key="attachment" style={{ width: "8%"}}>添付<br/>有無</th>
                                <th style={{ width: "8%"}}>再申請<br/>要否</th>
                                <th style={{ width: "10%"}}>事前協議<br/>要否</th>
                                {!editable && (
                                    <th className="no-sort" style={{ width: "8%"}}>問い合わせ</th>
                                )}
                            </tr>
                        </thead>
                        <tbody>
                            {answers && Object.keys(answers).map(key => (
                                <tr key={"tr" + key } onClick={e => {
                                        this.clickAnswer(e, answers[key]["answerId"])
                                    }} 
                                    className={`${answers[key]["answerId"] == selectedAnswerId && !isChatView? CustomStyle.is_selected : isAdmin && editable && answers[key]["answerDataType"] == "5" ? CustomStyle.is_deleted : ""}
                                                ${isAdmin && editable && answers[key]["answerDataType"] == "2" ? CustomStyle.is_added:""}`}
                                >
                                    <td>
                                        {answers[key]["judgementInformation"]["title"]}
                                    </td>
                                    <td>{answers[key]["judgementInformation"]["departments"]?.map(department => { return department.departmentName }).filter(departmentName => { return departmentName !== null }).join(",")}</td>
                                    <td>
                                        {answers[key]["judgementResult"]}
                                    </td>
                                    <td>
                                        {isAdmin && editable && (
                                            <>
                                                {answers[key]?.editable && answers[key]?.answerContentEditable && (
                                                    <div>
                                                            <button
                                                                className={CustomStyle.template_button}
                                                                style={{ margin: "0 10px"}}
                                                                onClick={e => {
                                                                this.showTemplate(answers[key]);
                                                                }}
                                                            >回答入力</button>
                                                        <textarea 
                                                            className={CustomStyle.custom_textarea}  maxLength={maxLength + inputBuffLength}
                                                            rows="5" type="text" placeholder="" value={answers[key]?.answerContent}
                                                            autoComplete="off"
                                                            onChange={e => this.inputChange(key, e.target.value, maxLength)}
                                                            ></textarea>
                                                    </div>
                                                )}
                                                {(!answers[key]?.editable || !answers[key]?.answerContentEditable) && (
                                                    answers[key]?.answerContent
                                                )}
                                            </>
                                        )}
                                        {!editable && (
                                            answers[key]["answerContent"]
                                        )}
                                    </td>
                                    <td>
                                        <div className={CustomStyle.flex_center} style={{justifyContent:"center"}}>
                                            {answers[key]["answerFiles"].length > 0 && (
                                                <div className={CustomStyle.file_icon}>
                                                    <StyledIcon 
                                                        glyph={Icon.GLYPHS.fileUpload}
                                                        styledWidth={"20px"}
                                                        styledHeight={"20px"}
                                                        light
                                                    />
                                                </div>
                                            )}
                                        </div>
                                    </td>
                                    <td style={{display:"none"}} data-sort-column-key="attachment">{answers[key]["answerFiles"].length}</td>
                                    <td>
                                        {isAdmin && editable && (
                                            <>
                                                {answers[key]?.editable && (
                                                    <select value={answers[key]?.reApplicationFlag}
                                                        onChange={e => this.reapplicationChange(key, e.target.value)}>
                                                        <option></option>
                                                        <option value={true}>要再申請</option>
                                                        <option value={false}>再申請不要</option>
                                                    </select>
                                                )}
                                                {!answers[key]?.editable&& (
                                                    <select disabled value={answers[key]?.reApplicationFlag}>
                                                        <option></option>
                                                        <option value={true}>要再申請</option>
                                                        <option value={false}>再申請不要</option>
                                                    </select>
                                                )}
                                            </>
                                        )}
                                        {!editable && (
                                            <>{answers[key]["reApplicationFlag"]==true?"必要":answers[key]["reApplicationFlag"]==false?"不要":""}</>
                                        )}
                                    </td>
                                    <td>
                                        {isAdmin && editable && (
                                            <>
                                                {answers[key]?.editable && (
                                                    <select value={answers[key]?.discussionFlag}
                                                    onChange={e => this.discussionFlagChange(key, e.target.value)}>
                                                        <option></option>
                                                        <option value={true}>要事前協議</option>
                                                        <option value={false}>事前協議不要</option>
                                                    </select>
                                                )}
                                                {!answers[key]?.editable && (
                                                    <select disabled value={answers[key]?.discussionFlag}>
                                                        <option></option>
                                                        <option value={true}>要事前協議</option>
                                                        <option value={false}>事前協議不要</option>
                                                    </select>
                                                )}
                                            </>
                                        )}
                                        {!editable && (
                                            <>{answers[key]["discussionFlag"]==true?"必要":answers[key]["discussionFlag"]==false?"不要":""}</>
                                        )}
                                    </td>
                                    {!editable && (
                                        <td>
                                            <div className={CustomStyle.table_button_box}>
                                                <div className={CustomStyle.item}>
                                                    {this.getButtonType(answers[key]) === 3 && (
                                                        <button className={`${CustomStyle.chat_button}`}
                                                            onClick={e => {
                                                                this.moveToChatView(e, answers[key])
                                                            }}
                                                        >
                                                            <StyledIcon 
                                                                glyph={Icon.GLYPHS.chat}
                                                                styledWidth={"30px"}
                                                                styledHeight={"30px"}
                                                                light
                                                            />
                                                        </button>
                                                    )}
                                                    {this.getButtonType(answers[key]) === 2 && (
                                                        <button className={`${CustomStyle.chat_button}`}
                                                            onClick={e => {
                                                                this.moveToChatView(e, answers[key])
                                                            }}
                                                        >
                                                            <StyledIcon 
                                                                glyph={Icon.GLYPHS.chat}
                                                                styledWidth={"30px"}
                                                                styledHeight={"30px"}
                                                                light
                                                            />
                                                            <span className={CustomStyle.badge}>!</span>
                                                        </button>
                                                    )}
                                                </div>
                                            </div>
                                        </td>
                                    )}
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            </Box>
        );
    }
}
export default withTranslation()(withTheme(AnswerContentList));