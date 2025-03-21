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
import Config from "../../../../../customconfig.json";

/**
 *【R6】許可判定：判定内容一覧コンポーネント
 */
@observer
class AssessmentContentList extends React.Component {
    static displayName = "AssessmentContentList";
    static propTypes = {
        terria: PropTypes.object.isRequired,
        viewState: PropTypes.object.isRequired,
        theme: PropTypes.object,
        t: PropTypes.func.isRequired,
        answers: PropTypes.array,
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
            //表示用回答内容リスト
            answers: props.answers,
            //選択された回答ID
            selectedAnswerId:0,
            //編集可否
            editable: this.props.editable,
            //申請ID
            applicationId: props.applicationId,
            // 回答ファイルの件数
            answerFileCount: props.answerFileCount,
            // 行政専用：ログインユーザーID
            loginUserId:"0001",
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
        this.getChatButtonType();
    }

    /**
     * props更新時
     */
    componentDidUpdate(prevProps) {
        if (!_isEqual(this.props.answers, prevProps.answers)) {
            this.setState({ answers:this.props.answers });
            this.getChatButtonType();
        }
        if (!_isEqual(this.props.selectedAnswerId, prevProps.selectedAnswerId)) {
            this.setState({ selectedAnswerId:this.props.selectedAnswerId });
        }
        if (!_isEqual(this.props.answerFileCount, prevProps.answerFileCount)) {
            this.setState({ answerFileCount:this.props.answerFileCount });
        }
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
     * 回答内容入力
     * @param {number} 対象回答のkey
     * @param {string} 入力された値
     * @param {number} maxLength 最大文字数
     */
    inputChange(key, value, maxLength) {
        // 文字数チェック
        if(value.length > maxLength ){
            alert(maxLength+"文字以内で入力してください。");
            return;
        }
        let answers = [...this.state.answers];
        answers[key]["answerContent"] = value;
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
        //親コンポーネント側の回答一覧を更新
        this.props.callback(answers);
    }

    /**
     * 区分選択（判定結果）
     * @param {number} 対象回答のkey
     * @param {boolean} 入力された値 
     */
    judgementResultChange(key, value) {
        let answers = [...this.state.answers];
        answers[key]["permissionJudgementResult"] = value;
        //親コンポーネント側の回答一覧を更新
        this.props.callback(answers);
    }

    /**
     * 未読問い合わせがあるか判定
     * @returns 
     */
    getChatButtonType(){

        let buttonType = 1;
        let answerContent = this.props.viewState.answerContent;
        let requestBody = {};
        let getChatMessagesApi = "";
        if(this.props.terria.authorityJudgment()){
            getChatMessagesApi = "/chat/government/messages";
            requestBody = {
                applicationId: this.props.applicationId,
                applicationStepId:3,
                unreadFlag:true,
            }
        }else{
            getChatMessagesApi = "/chat/business/messages"
            requestBody = {
                loginId: answerContent?.loginId,
                password:answerContent?.password,
                applicationId:this.props.applicationId,
                applicationStepId:3,
                unreadFlag:true,
            }
        }
        fetch(Config.config.apiUrl + getChatMessagesApi, {
            method: 'POST',
            body: JSON.stringify(requestBody),
            headers: new Headers({ 'Content-type': 'application/json' }),
        })
        .then(res => {
            // 401認証エラーの場合の処理を追加
            if (res.status === 401) {
                alert('認証情報が無効です。ページの再読み込みを行います。');
                window.location.href = "./login/";
                return null;
            }
            return res.json();
        })
        .then(res => {
            if(res){
                let chat = null;
                Object.keys(res).forEach(key=>{
                    if(res[key] && res[key]["applicationStep"] && res[key]["applicationStep"]["applicationStepId"] && res[key]["applicationStep"]["applicationStepId"] == 3){
                        chat = res[key];
                    }
                })
                const isEmpty = chat == null || chat["messages"] == null || chat["messages"].length === 0;
                //行政の場合
                if(this.props.terria.authorityJudgment()){
                    if(isEmpty){
                        // 問い合わせ情報がない場合、チャットボタンを「吹き出し」アイコンで表示
                        buttonType = 3;
                    }else{
                        let unReadInquiryAddressForm = null;
                        let unReadMessage = null;
                        for(let i=0;i<chat["messages"].length;i++){
                            unReadInquiryAddressForm = chat["messages"][i].inquiryAddressForms.find(inquiryAddressForm => inquiryAddressForm.department?.departmentId == this.state.loginUserDepartmentId && !inquiryAddressForm.readFlag);
                            if(unReadInquiryAddressForm){
                                unReadMessage = chat["messages"][i];
                                break;
                            }
                        }
                        if((unReadInquiryAddressForm && unReadMessage && unReadMessage.messageType == 1) 
                            || (unReadInquiryAddressForm && unReadMessage && unReadMessage.messageType == 3)){
                            // チャットボタンを「吹き出し」アイコンで表示し、！マークをつける
                            buttonType = 2;
                        }else{
                            // 上記以外は　チャットボタンを「吹き出し」アイコンで表示
                            buttonType = 3;
                        }
                    }
                //事業者の場合
                }else{
                    const answers = this.state.answers;
                    const notifiedAnswer = answers?.find(_answer=>_answer.notifiedFlag);
                    if(!notifiedAnswer && isEmpty){
                        // 未通知かつ問い合わせ情報が無い場合表示しない
                        buttonType = 1;
                    }else if(notifiedAnswer && isEmpty){
                        // 通知済みかつ問い合わせ情報がない場合、チャットボタンを「吹き出し」アイコンで表示
                        buttonType = 3;
                    }else{
                        let size = chat["messages"].length;
                        let lastMessage = chat["messages"][size - 1];
                        if(lastMessage.messageType == 2 && !lastMessage.readFlag){
                            // チャットボタンを「吹き出し」アイコンで表示し、！マークをつける
                            buttonType = 2;
                        }else{
                            // 上記以外は　チャットボタンを「吹き出し」アイコンで表示
                            buttonType = 3;
                        }
                    }
                }
                this.setState({buttonType:buttonType});
            }
        }).catch(error => {
            console.error('通信処理に失敗しました', error);
            alert('通信処理に失敗しました');
        })
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

    render() {
        const answers = this.state.answers;
        const selectedAnswerId = this.state.selectedAnswerId;
        const editable = this.state.editable;
        const isAdmin = this.props.terria.authorityJudgment();
        const answerFileCount = this.state.answerFileCount;
        const buttonType = this.state.buttonType;
        const maxLength = Config.inputMaxLength.answerContent;
        const inputBuffLength = 1;
        return (
            <Box
                centered 
                displayInlineBlock 
                className={CustomStyle.custom_content}
            >
                <Spacing bottom={2} />
                <Box col12 right style={{paddingRight:"10px"}}>
                    {answerFileCount > 0 && !editable && (
                        <div className={CustomStyle.file_icon}>
                            <StyledIcon 
                                glyph={Icon.GLYPHS.fileUpload}
                                styledWidth={"20px"}
                                styledHeight={"20px"}
                                light
                            />
                        </div>
                    )}
                    {!editable && buttonType === 3 && (
                        <button className={`${CustomStyle.chat_button}`}
                            onClick={e => {
                                this.props.viewState.moveToChatView(
                                    this.props.applicationId,
                                    this.props.viewState.checkedApplicationStepId,
                                    null,
                                    null);
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
                    {!editable && buttonType === 2 && (
                        <button className={`${CustomStyle.chat_button}`}
                            onClick={e => {
                                this.props.viewState.moveToChatView(
                                    this.props.applicationId,
                                    this.props.viewState.checkedApplicationStepId,
                                    null,
                                    null);
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
                </Box>
                <Spacing bottom={2} />
                <div className={CustomStyle.scroll_container} style={{height: isAdmin?editable?"42vh":"47vh":"37.5vh"}} >
                    <table className={CustomStyle.selection_table}>
                        <thead>
                            <tr className={CustomStyle.table_header}>
                                <th style={{width:"35%"}}>対象</th>
                                <th style={{width:"15%"}}>判定結果</th>
                                <th style={{width:"35%"}}>回答内容</th>
                                <th style={{width:"15%"}}>再申請要否</th>
                            </tr>
                        </thead>
                        <tbody>
                            {answers && Object.keys(answers).map(key => (
                            <tr key={"tr" + key } onClick={e => {
                                    // 編集不可の場合、行をクリックすると、回答履歴を切り替える
                                    if(!editable){
                                        this.clickAnswer(e, answers[key]["answerId"])
                                    }
                                }} 
                                className={answers[key]["answerId"] == selectedAnswerId? CustomStyle.is_selected : ""}>
                                <td>
                                    {answers[key]["judgementInformation"]["title"]}
                                </td>
                                <td>
                                    {
                                        //:判定結果
                                    }
                                    {isAdmin && editable && (
                                        <>
                                            {answers[key]?.editable && (
                                                <select 
                                                    value={answers[key]?.permissionJudgementResult} 
                                                    onChange={e => {this.judgementResultChange(key, e.target.value)}}
                                                >
                                                    <option></option>
                                                    <option value={"0"}>問題なし</option>
                                                    <option value={"1"}>問題あり</option>
                                                </select>
                                            )}
                                            {!answers[key]?.editable&& (
                                                <select disabled value={answers[key]?.permissionJudgementResult} >
                                                    <option></option>
                                                    <option value={"0"}>問題なし</option>
                                                    <option value={"1"}>問題あり</option>
                                                </select>
                                            )}
                                        </>
                                    )}
                                    {!editable && (
                                        <>
                                            <>{answers[key]["permissionJudgementResult"] == "0"?"問題なし":answers[key]["permissionJudgementResult"] == "1"?"問題あり":""}</>
                                        </>
                                    )}
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
                                                        className={CustomStyle.custom_textarea}
                                                        rows="5" type="text" placeholder="" value={answers[key]?.answerContent}
                                                        maxLength={maxLength + inputBuffLength}
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
                                    {
                                        //:再申請要否
                                    }
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
                                        <>{answers[key]["reApplicationFlag"] == true?"必要":answers[key]["reApplicationFlag"] == false?"不要":""}</>
                                    )}
                                </td>
                            </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            </Box>
        );
    }
}
export default withTranslation()(withTheme(AssessmentContentList));