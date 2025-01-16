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
//import InputChatMessage from "../Views/Chat/InputChatMessage.jsx";

/**
 *【R6】事前協議：協議内容一覧コンポーネント
 */
@observer
class NegotiationContentList extends React.Component {
    static displayName = "NegotiationContentList";
    static propTypes = {
        terria: PropTypes.object.isRequired,
        viewState: PropTypes.object.isRequired,
        theme: PropTypes.object,
        t: PropTypes.func.isRequired,
        departmentAnswers: PropTypes.array,
        intervalID:PropTypes.number,
        clickAnswer:PropTypes.func.isRequired,
        selectedAnswerId:PropTypes.number,
        selectedDepartmentAnswerId:PropTypes.number,
        editable:PropTypes.bool,
        callback:PropTypes.func,
        addAnswerIndex:PropTypes.number,
        deleteAnswer:PropTypes.func,
        ledgerMaster: PropTypes.array,
        applicationId: PropTypes.number,
        changeAnswerInfo:PropTypes.func
    }

    constructor(props) {
        super(props);
        this.state = {
            viewState: props.viewState,
            terria: props.terria,
            //部署別回答一覧
            departmentAnswers: props.departmentAnswers,
            //選択された回答ID/選択された
            selectedAnswerId:0,
            //選択された部署回答ID/選択された
            selectedDepartmentAnswerId:0,
            //編集可否
            editable: this.props.editable,
            // 協議対象一覧
            ledgerMaster: props.ledgerMaster,
            ledgerMasterCount: 0,
            // 回答登録のエラーレコード
            errors:{answers:[],departmentAnswers:[]},
            // 行政で追加された回答のindex(事前協議のみ)
            addAnswerIndex: props.addAnswerIndex,
            // 行政専用：ログインユーザー部署ID
            loginUserDepartmentId:"10001",
            // チャットボタンタイプ
            buttonType:1
        };
        this.clickAnswerRegBtn = this.clickAnswerRegBtn.bind(this);
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
        if (!_isEqual(this.props.departmentAnswers, prevProps.departmentAnswers)) {
            this.setState({ departmentAnswers:this.props.departmentAnswers });
        }
        if (!_isEqual(this.props.selectedAnswerId, prevProps.selectedAnswerId)) {
            this.setState({ selectedAnswerId:this.props.selectedAnswerId });
        }
        if (!_isEqual(this.props.selectedDepartmentAnswerId, prevProps.selectedDepartmentAnswerId)) {
            this.setState({ selectedDepartmentAnswerId:this.props.selectedDepartmentAnswerId });
        }
        if (!_isEqual(this.props.ledgerMaster, prevProps.ledgerMaster)) {
            this.setState({ ledgerMaster:this.props.ledgerMaster });
        }
        if (!_isEqual(this.props.errors, prevProps.errors)) {
            this.setState({ errors:this.props.errors });
        }
        if (!_isEqual(this.props.addAnswerIndex, prevProps.addAnswerIndex)) {
            this.setState({ addAnswerIndex:this.props.addAnswerIndex });
        }
    }

    /**
     * 部署別の回答一覧を作成
     */
    updateCategorizedAnswers = () => {
        const categorizedAnswers = this.categorizeAnswers(this.props.answers);
        this.setState({ answers:this.props.answers,categorizedAnswers:categorizedAnswers });
    };
    
    /**
     * 部署別の回答一覧を作成
     */
    categorizeAnswers = (items) => {
        const categories = {};
        items.forEach(item => {
            const departmentName = item["judgementInformation"]["department"]["departmentName"];
            if(!categories[departmentName]){
                categories[departmentName] = [];
            }
            categories[departmentName].push(item);
        });

        return categories;
    };

     /**
     * 回答内容一覧の行クリックイベント
     * @param {*} event イベント
     * @param {*} answerId 回答ID
     */
    clickAnswer(event, answerId, departmentAnswerId){
        if(this.validateAnswerId(answerId) || this.validateAnswerId(departmentAnswerId)){
            let selectedAnswerId = 0;
            let selectedDepartmentAnswerId = 0;
            if(this.validateAnswerId(answerId)){
                selectedAnswerId = answerId;
            }
            if(this.validateAnswerId(departmentAnswerId)){
                selectedDepartmentAnswerId = departmentAnswerId;
            }
            this.setState({selectedAnswerId: selectedAnswerId, selectedDepartmentAnswerId: selectedDepartmentAnswerId});
            this.props.clickAnswer(event, answerId, departmentAnswerId);
        }
    }

    validateAnswerId(answerId){
        let result = false;
        if(answerId!==undefined && answerId !== null && Number(answerId) > 0){
            result = true;
        }
        return result;
    }

    /**
     * 回答登録ボタン押下
     * 同意項目入力値をＤＢに登録更新を行う。
     */
    clickAnswerRegBtn(){
        let answersContentOnly = [];
        let key1;
        let key2;
        let businessPassStatus;
        let businessPassStatus_bef;
        let businessPassStatus_aft;
        let businessAnswerDatetime;

        // 実施確認
        var res = confirm("事業者合意内容を登録します。よろしいでしょうか？");
        if(res == false){
            return;
        }

        // 必須チェック
        for (key1 = 0; key1 < this.state.departmentAnswers.length; key1++) {
            for (key2 = 0; key2 < this.state.departmentAnswers[key1].answers.length; key2++) {
                // 登録済みになる場合、チェック対象外にする
                if(!this.isInputed(this.state.departmentAnswers[key1].answers[key2].businessPassStatus)){
                    businessPassStatus = document.getElementById('businessPassStatus_'+key1+'_'+key2).value; 
                    businessAnswerDatetime = document.getElementById('businessAnswerDatetime_'+key1+'_'+key2).value; 
                    if((this.isInputed(businessPassStatus) && !this.isInputed(businessAnswerDatetime))
                        ||(!this.isInputed(businessPassStatus) && this.isInputed(businessAnswerDatetime))){
                            alert("事業者合意または、日付を入力してください。");
                            return;
                    }

                    // 日付が入力された場合、妥当性チェック
                    if(this.isInputed(businessAnswerDatetime)) {
                        const date = new Date(businessAnswerDatetime); 
                        if(Number.isNaN(date.getDate())){
                            alert("日付を正しく入力してください。");
                            return;
                        }
                    }
                }
            }
        }

        // 登録データ作成
        for (key1 = 0; key1 < this.state.departmentAnswers.length; key1++) {
            for (key2 = 0; key2 < this.state.departmentAnswers[key1].answers.length; key2++) {
                // 事業者合否ステータスが（更新前） == "":未選択 && 事業者合否ステータスが（更新後） != "":未選択　である場合
                businessPassStatus_bef = this.state.departmentAnswers[key1].answers[key2].businessPassStatus ?? ""; 
                if (businessPassStatus_bef == "") {
                    businessPassStatus_aft = document.getElementById('businessPassStatus_'+key1+'_'+key2).value ?? "";
                    if (businessPassStatus_aft != ""){
                        businessAnswerDatetime = document.getElementById('businessAnswerDatetime_'+key1+'_'+key2).value; 

                        // 同意項目承認否認登録API 引数：List<AnswerForm>作成
                        answersContentOnly.push({
                            "answerId": this.state.departmentAnswers[key1].answers[key2]["answerId"],
                            "editable": this.state.departmentAnswers[key1].answers[key2]["editable"],
                            "judgementResult": this.state.departmentAnswers[key1].answers[key2]["judgementResult"],
                            "answerContent": this.state.departmentAnswers[key1].answers[key2]["answerContent"],
                            "updateDatetime": this.state.departmentAnswers[key1].answers[key2]["updateDatetime"],
                            "completeFlag": this.state.departmentAnswers[key1].answers[key2]["completeFlag"],
                            "judgementInformation": this.state.departmentAnswers[key1].answers[key2]["judgementInformation"],
                            "answerFiles": this.state.departmentAnswers[key1].answers[key2]["answerFiles"],
                            "reApplicationFlag": this.state.departmentAnswers[key1].answers[key2]["reApplicationFlag"],
                            "businessPassStatus":businessPassStatus_aft,
                            "businessAnswerDatetime":businessAnswerDatetime
                        })
                    }
                }
            }
        }

        // 更新対象有無チェック
        if(answersContentOnly.length==0){
            return;
        }

        //同意項目承認否認登録
        fetch(Config.config.apiUrl + "/answer/consent/input", {
            method: 'POST',
            body: JSON.stringify(answersContentOnly),
            headers: new Headers({ 'Content-type': 'application/json' }),
        })
        .then(res => res.json())
        .then(res => {
            if(res.status === 401){
                alert("認証情報が無効です。ページの再読み込みを行います。");
                window.location.reload();
                return null;
            }
            if (res.applicationId) {
                alert("事業者合意内容を登録しました。");
                this.props.changeAnswerInfo(res); 
            }else{
                alert('事業者合意内容登録に失敗しました');
            }
        }).catch(error => {
            console.error('通信処理に失敗しました', error);
            alert('通信処理に失敗しました');
        });
     
    }

    /**
     * 部署回答が編集かどうか判定
     * @param {object} departmentAnswer 部署回答
     */
    isEditableForDepartmentAnswer(departmentAnswer){

        let isEditable = false;

        // 該当レコードの編集フラグで判断する
        if(departmentAnswer.editable == true){
           
            isEditable = true;

            //部署の全ての回答中に、未入力、または[3:却下]とした回答があれば、編集不可とする
            Object.keys(departmentAnswer.answers).map(i => {

                // 行政で削除した回答、許可判定移行フラグはチェックしない回答　を除く
                if(departmentAnswer.answers[i].answerDataType != "7" && departmentAnswer.answers[i].permissionJudgementMigrationFlag == false){
                    // 行政確定ステータスが却下または未入力の場合、部署の行政確定内容が編集不可
                    let status = departmentAnswer.answers[i].governmentConfirmStatus;
                    if(status === undefined || status === null || status === "" ||  status == "2"){
                        isEditable = false;
                    }

                    //行政確定の日付が入力していない場合、部署の行政確定内容が編集不可
                    let datetime = departmentAnswer.answers[i].governmentConfirmDatetime;
                    if(datetime === undefined || datetime === null || datetime === "" ){
                        isEditable = false;
                    }
                }
            });
        }

        if(isEditable){
            // 行政確認登録通知許可フラグがTRUEの場合、統括部署管理者に通知済みになるため、変更不可
            if(departmentAnswer.governmentConfirmPermissionFlag == true){
                isEditable = false;
            }
        }
        
        return isEditable;
    }

    /**
     * 回答レコードが削除・回復できるか判定
     * @param {object} answer 回答
     * 
     * @returns 
     */
    isDeleteable(answer, isDeleteBtn){
        let isDeleteable = false;

        let editable = answer.editable;
        let deleteUnnotifiedFlag = answer.deleteUnnotifiedFlag;
        let notifiedFlag = answer.notifiedFlag;
        // 回答が編集可能
        if(editable === true ){
            if(isDeleteBtn){
                // 該当回答が削除されていない
                if(deleteUnnotifiedFlag === undefined || deleteUnnotifiedFlag === null || deleteUnnotifiedFlag == false){
                    // 該当回答まだ事業者へ通知していない
                    if(notifiedFlag === undefined || notifiedFlag === null || notifiedFlag == false){
                        // 統括部署管理者に通知したら、削除不可になる
                        if(answer.answerPermissionFlag){
                            isDeleteable = false;
                        }else{
                            isDeleteable = true;
                        }
                    }else{
                        // 事業者へ通知済みの場合、事業者合意登録が未完了の場合、削除可能
                        if(this.isAnsweredFromBusiness(answer)){
                            isDeleteable = false;
                        }else{
                            isDeleteable = true;
                        }
                    }
                }
            }else{
                // 該当回答が削除されている、かつ、初期表示時のデータ種類が「7:行政で削除済み」ではない　の場合、条項が回復できます。
                if(deleteUnnotifiedFlag === true && answer.answerDataType_bak !== "7"){
                    isDeleteable = true; 
                }
            }
        }

        return isDeleteable;
    }

    /**
     * 回答の入力内容が編集できるか判定
     * @param {object} answer 回答
     * @param {boolean} isGovernmentConfirm　行政確定内容かどうか 
     */
    isEditable(answer, isGovernmentConfirm){
        let isEditable = false;

        let editable = answer.editable;
        let deleteUnnotifiedFlag = answer.deleteUnnotifiedFlag;
        let notifiedFlag = answer.notifiedFlag;
        let governmentConfirmNotifiedFlag = answer.governmentConfirmNotifiedFlag;
        // 回答が編集可能
        if(editable === true ){
            // 該当回答が削除されていない
            if(deleteUnnotifiedFlag === undefined || deleteUnnotifiedFlag === null || deleteUnnotifiedFlag == false){
                // 行政確定内容である場合、
                if(isGovernmentConfirm){
                    // 事業者からの合否内容を登録されたか
                    if(this.isAnsweredFromBusiness(answer)){
                        // 該当回答まだ事業者へ通知していない
                        if(governmentConfirmNotifiedFlag === undefined || governmentConfirmNotifiedFlag === null || governmentConfirmNotifiedFlag == false){

                            // 統括部署管理者に通知したら、編集不可になる
                            if(answer.governmentConfirmPermissionFlag){
                                isEditable = false;
                            }else{
                                isEditable = true;
                            }
                        }
                    }
                }else{
                    // 事業者へ一度も通知しない場合、統括部署管理者へ通知済みになると、入力不可になる
                    if(notifiedFlag === undefined || notifiedFlag === null || notifiedFlag == false){
                        // isEditable = true; 
                        // 統括部署管理者に通知したら、編集不可になる
                        if(answer.answerPermissionFlag){
                            isEditable = false;
                        }else{
                            isEditable = true;
                        }
                    }else{
                        // 事業者へ通知した後から、事業者が合意登録済みまで、回答内容が上書き更新可能とする。 
                        if(this.isAnsweredFromBusiness(answer)){
                            isEditable = false;
                        }else{
                            isEditable = true;
                        }

                    }
                }
            }
        }

        return isEditable;
    }

    /**
     * 回答には、事業者からの合否内容を登録されるか判定
     * @param {object} answer 
     * @returns 
     */
    isAnsweredFromBusiness(answer){
        let isAnswered = false;
        let businessPassStatus = answer.businessPassStatus;
        if(businessPassStatus !== undefined && businessPassStatus !== null ){
            if(businessPassStatus == "0" || businessPassStatus == "1"){
                isAnswered = true;
            }
        }

        return isAnswered;
    }

    /**
     * 回答内容入力
     * @param {number} key1 対象回答に対する部署回答key
     * @param {number} key2 対象回答に対する回答key
     * @param {string} value 入力された値
     * @param {number} maxLength 最大文字数
     */
    inputChange(key1, key2, value, maxLength ) {
        // 文字数チェック
        if(value.length > maxLength ){
            alert(maxLength+"文字以内で入力してください。");
            return;
        }
        let departmentAnswers = [...this.state.departmentAnswers];
        departmentAnswers[key1].answers[key2]["answerContent"] = value;
        // 該当レコードが5:削除済み、6：引継の場合、回答を編集すると、更新にリセット
        if(departmentAnswers[key1].answers[key2].answerDataType == "5" || departmentAnswers[key1].answers[key2].answerDataType == "6"){
            departmentAnswers[key1].answers[key2].answerDataType = "1"
        }
        //親コンポーネント側の回答一覧を更新
        this.props.callback(departmentAnswers);
    }

    /**
     * 協議対象選択（32協議など）
     * @param {number} key1 対象回答に対する部署回答key
     * @param {number} key2 対象回答に対する回答key
     * @param {number} index 協議対象のindex
     * @param {boolean} value 入力された値 
     */
    checkedDiscussionItemChange(key1, key2, index, value) {
        let departmentAnswers = [...this.state.departmentAnswers];
        departmentAnswers[key1].answers[key2].discussionItems[index].checked = value;
        // 該当レコードが5:削除済み、6：引継の場合、回答を編集すると、更新にリセット
        if(departmentAnswers[key1].answers[key2].answerDataType == "5" || departmentAnswers[key1].answers[key2].answerDataType == "6"){
            departmentAnswers[key1].answers[key2].answerDataType = "1"
        }
        //親コンポーネント側の回答一覧を更新
        this.props.callback(departmentAnswers);
    }

    /**
     * 区分選択（行政確定ステータス）
     * @param {number} key1 対象回答に対する部署回答key
     * @param {number} key2 対象回答に対する回答key
     * @param {string} value 入力された値
     */
    governmentConfirmStatusChange(key1, key2, value) {
        let departmentAnswers = [...this.state.departmentAnswers];
        if(key2){
            departmentAnswers[key1].answers[key2]["governmentConfirmStatus"] = value;
            // 該当レコードが5:削除済み、6：引継の場合、回答を編集すると、更新にリセット
            if(departmentAnswers[key1].answers[key2].answerDataType == "5" || departmentAnswers[key1].answers[key2].answerDataType == "6"){
                departmentAnswers[key1].answers[key2].answerDataType = "1"
            }
        }else{
            departmentAnswers[key1]["governmentConfirmStatus"] = value;
            // 行政確定ステータスが空の場合、行政確定登録日時も空に更新
            if(!value){
                departmentAnswers[key1]["governmentConfirmDatetime"] = value;
            }
        }
        //親コンポーネント側の回答一覧を更新
        this.props.callback(departmentAnswers);
    }

    /**
     * 内容入力（行政確定日付）
     * @param {number} key1 対象回答に対する部署回答key
     * @param {number} key2 対象回答に対する回答key
     * @param {string} value 入力された値
     */
    inputGovernmentConfirmDatetimeChange(key1, key2, value) {
        let departmentAnswers = [...this.state.departmentAnswers];
        if(key2){
            departmentAnswers[key1].answers[key2]["governmentConfirmDatetime"] = value;
            // 該当レコードが5:削除済み、6：引継の場合、回答を編集すると、更新にリセット
            if(departmentAnswers[key1].answers[key2].answerDataType == "5" || departmentAnswers[key1].answers[key2].answerDataType == "6"){
                departmentAnswers[key1].answers[key2].answerDataType = "1"
            }
        }else{
            departmentAnswers[key1]["governmentConfirmDatetime"] = value;
        }
        //親コンポーネント側の回答一覧を更新
        this.props.callback(departmentAnswers);
    }

    /**
     * 内容入力（行政確定コメント）
     * @param {number} key1 対象回答に対する部署回答key
     * @param {number} key2 対象回答に対する回答key
     * @param {string} value 入力された値
     * @param {number} maxLength 最大文字数
     */
    inputGovernmentConfirmCommentChange(key1, key2, value, maxLength) {
        // 文字数チェック
        if(value.length > maxLength ){
            alert(maxLength+"文字以内で入力してください。");
            return;
        }
        let departmentAnswers = [...this.state.departmentAnswers];
        if(key2){
            departmentAnswers[key1].answers[key2]["governmentConfirmComment"] = value;
            // 該当レコードが5:削除済み、6：引継の場合、回答を編集すると、更新にリセット
            if(departmentAnswers[key1].answers[key2].answerDataType == "5" || departmentAnswers[key1].answers[key2].answerDataType == "6"){
                departmentAnswers[key1].answers[key2].answerDataType = "1"
            }
        }else{
            departmentAnswers[key1]["governmentConfirmComment"] = value;
        }
        //親コンポーネント側の回答一覧を更新
        this.props.callback(departmentAnswers);
    }

    /**
     * 行政で回答追加
     * @param {*} event 
     * @param {number} key 対象回答に対する部署回答key
     */
    addAnswer(event, key){
        let departmentAnswers = [...this.state.departmentAnswers];
        const ledgerMaster = this.state.ledgerMaster;
        let index = this.state.addAnswerIndex;
        departmentAnswers[key].answers.push({
            answerId:0,
            departmentAnswerId: departmentAnswers[key].departmentAnswerId,
            editable: true,
            answerContentEditable: true,
            judgementResult:"",
            answerContent:"",
            completeFlag:false,
            notifiedFlag:false,
            judgementInformation:{title:Config.config.governmentAddAnswerTitle,departments:[departmentAnswers[key].department]},
            answerFiles:[],
            chat:{},
            answerTemplate:{},
            applicationStep: this.props.viewState.checkedApplicationStepId,
            discussionItem:"",
            discussionItems: JSON.parse(JSON.stringify(ledgerMaster)),
            businessPassStatus:"",
            businessPassComment:"",
            governmentConfirmStatus:"",
            governmentConfirmDatetime:"",
            governmentConfirmComment:"",
            governmentConfirmNotifiedFlag:false,
            answerStatus:"0",
            answerDataType:"3",
            answerHistorys:[],
            answerPermissionFlag: false,
            governmentConfirmPermissionFlag: false,
            permissionJudgementMigrationFlag: false,
            addAnswerIndex: index+1
        });

        //親コンポーネント側の回答一覧を更新
        this.props.callback(departmentAnswers);
    }

    /**
     * 行政で回答削除
     * @param {*} event 
     * @param {number} key1 対象回答に対する部署回答key
     * @param {number} key2 対象回答に対する回答key
     */
    deleteAnswer(event, key1, key2){
        let departmentAnswers = [...this.state.departmentAnswers];

        let answerId = departmentAnswers[key1].answers[key2].answerId;

        // 行政で申請追加して、まだDBへ登録されていない場合、直接削除
        if(answerId == 0){
            departmentAnswers[key1].answers.splice(key2,1);
        }else{
            departmentAnswers[key1].answers[key2].deleteUnnotifiedFlag = true;
            departmentAnswers[key1].answers[key2].answerDataType = "7";
        }
        
        //親コンポーネント側の回答一覧を更新
        this.props.callback(departmentAnswers);

    }

    /**
     * 行政で回答削除した条項の回復
     * @param {*} event 
     * @param {number} key1 対象回答に対する部署回答key
     * @param {number} key2 対象回答に対する回答key
     */
    deleteAnswerRevert(event, key1, key2){
        let departmentAnswers = [...this.state.departmentAnswers];

        let answerDataType = departmentAnswers[key1].answers[key2].answerDataType_bak;

        // 削除未通知フラグをfalseに戻す
        departmentAnswers[key1].answers[key2].deleteUnnotifiedFlag = false;
        // 回答データ種類を初期表示の値に戻す
        departmentAnswers[key1].answers[key2].answerDataType = answerDataType;

        //親コンポーネント側の回答一覧を更新
        this.props.callback(departmentAnswers);

    }

    /**
     * 回答追加可能判定
     * 追加不可の場合、「回答追加」ボタンが非表示にする
     */
    haveEditableAmswer(departmentAnswer){
        let editable = false;
        if(departmentAnswer.editable !== null && departmentAnswer.editable !== undefined && departmentAnswer.editable === true) {
            editable = true;
        }else{
            Object.keys(departmentAnswer.answers).map( index =>{
                if(departmentAnswer.answers[index].editable !== null && departmentAnswer.answers[index].editable !== undefined && departmentAnswer.answers[index].editable === true) {
                    editable = true;
                }
            })
        }

        // 編集可能な回答があれば、回答確定になると、回答追加不可になる
        let unConfirmed = false;
        if(editable){
            if(!this.isInputedDepartmentAnswer(departmentAnswer)){
                unConfirmed = true;
            }
            Object.keys(departmentAnswer.answers).map( index =>{
                // 削除予定の回答はチェック対象リストから除く
                if(departmentAnswer.answers[index]["answerDataType"] !== "7"){
                    if(!this.isInputed(departmentAnswer.answers[index].governmentConfirmDatetime) && !this.isInputed(departmentAnswer.answers[index].governmentConfirmStatus) ) {
                        unConfirmed = true;
                    }
                }
            })
        }
        return editable && unConfirmed ? true : false;
    }

    /**
     * 変数が空であるか判断
     * @param {*} text 
     * @returns 
     */
    isInputed(text){

        if(text == undefined || text == null || text == "" ){
            return false;
        }else{
            return true;
        }
    }

    /**
     * bool型の項目がtrueになるか判断
     * @param {*} value 
     * @returns 
     */
    isTrue(value){
        if(value == null || value ==undefined || value === false){
            return false;
        }else{
            return true;
        }
    }

    /**
     * 事前協議の回答登録が全て完了しているかチェック
     */
    isAllAnswerCompleted() {

        let key1;
        let key2;

        //　チェック処理
        for (key1 = 0; key1 < this.state.departmentAnswers.length; key1++) {
            if(this.state.departmentAnswers[key1].applicationId == this.props.applicationId){
                for (key2 = 0; key2 < this.state.departmentAnswers[key1].answers.length; key2++) {
                    // 同意項目入力値をstateへ保存
                    if((this.state.departmentAnswers[key1].answers[key2].businessPassStatus ?? "") == "" &&
                        this.state.departmentAnswers[key1].answers[key2].applicationStep.applicationStepId == 2) {
                            // 事業者側で編集可能であれば、事業者合意内容が未登録の場合、回答完了しないとする
                            if(this.isTrue(this.state.departmentAnswers[key1].answers[key2].editable)){
                                // 回答登録が完了していない
                                return false;
                            }
                    } 
                }
            }
        }
        // 回答登録が完了してる（全項目）
        return true;
    }

    /**
     * 行政から未読問い合わせがあるか判定
     * @param {*} answer 回答情報
     * @returns 
     */
    getChatButtonType(answer){
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
                    || (unReadInquiryAddressForm && unReadMessage && unReadMessage.messageType == 3)){
                    // チャットボタンを「吹き出し」アイコンで表示し、！マークをつける
                    buttonType = 2;
                }else{
                    // 上記以外は　チャットボタンを「吹き出し」アイコンで表示
                    buttonType = 3;
                }
            }
        }else{
            const notifiedAnswer = answer.answers.find(_answer=>_answer.notifiedFlag);
            if(!notifiedAnswer && isEmpty){
                // 未通知かつ問い合わせ情報が無い場合表示しない
                buttonType = 1;
            }else if(notifiedAnswer && isEmpty){
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
     * 日付項目フォーマット
     * @param {*} date 
     * @returns 
     */
    formatToInputdate(date){
        if(date === undefined || date === null){
            return "";
        }else{
            return date.replaceAll('/', '-');
        }
    }

    /**
     * 日付項目フォーマット
     * @param {*} date 
     * @returns 
     */
    formatToDispalyDate(date){
        if(date === undefined || date === null){
            return "";
        }else{
            return date.replaceAll('-', '/');
        }
    }

    /**
     * 部署全体の行政確定登録入力済みか
     * @param {*} departmentAnswer 
     * @returns 
     */
    isInputedDepartmentAnswer(departmentAnswer){

        // 行政確定登録：ステータス、日時のいずれか入力したら、配下の回答が入力不可にする
        if(this.isInputed(departmentAnswer.governmentConfirmStatus) || this.isInputed(departmentAnswer.governmentConfirmDatetime)){
            return true;
        }else{
            return false;
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
     * 回答内容を回答テンプレートで上書きする
     * @param {*} answer 回答
     * @param {*} text テキスト
    */
    inputFromTemplate = (answer, text) => {
        let departmentAnswers = [...this.state.departmentAnswers];
        const answerId = answer.answerId;
        const departmentAnswerId = answer.departmentAnswerId;

        for(let i = 0; i< departmentAnswers.length; i++) {

            if(departmentAnswers[i]["departmentAnswerId"] == departmentAnswerId){

                for(let j = 0; j< departmentAnswers[i].answers.length; j++) {
    
                    if (departmentAnswers[i].answers[j]["answerId"] == answerId) {

                        // 行政で追加する場合、
                        if(departmentAnswers[i].answers[j]["answerId"] == 0){

                            const addAnswerIndex = answer.addAnswerIndex;
                            if(departmentAnswers[i].answers[j]["addAnswerIndex"] == addAnswerIndex){
                                departmentAnswers[i].answers[j]["answerContent"] = text;
                                break;
                            }
                        }else{
                            departmentAnswers[i].answers[j]["answerContent"] = text;
                            // 該当レコードが5:削除済みの場合、回答を編集すると、更新にリセット
                            if(departmentAnswers[i].answers[j].answerDataType == "5"){
                                departmentAnswers[i].answers[j].answerDataType = "1"
                            }
                            break;
                        }
                    }
                }
            }
        }
        //親コンポーネント側の回答一覧を更新
        this.props.callback(departmentAnswers);
    }

    render() {

        const departmentAnswers = this.state.departmentAnswers;
        const selectedAnswerId = this.state.selectedAnswerId;
        const selectedDepartmentAnswerId = this.state.selectedDepartmentAnswerId;
        const editable = this.state.editable;
        const isAdmin = this.props.terria.authorityJudgment();
        const ledgerMaster = this.state.ledgerMaster;
        const ledgerMasterCount = Object.keys(ledgerMaster).length;

        const errorsAnswerIds = this.state.errors.answers;
        const errorsDepartmentAnswerIds = this.state.errors.departmentAnswers;
        const maxLength = Config.inputMaxLength.answerContent;
        const inputBuffLength = 1;
        return (
            <Box
                centered 
                displayInlineBlock 
                className={CustomStyle.custom_content}
            >
                <Spacing bottom={2} />
                {!isAdmin&& (
                    <Box col12 right>
                        <button
                            className={`${CustomStyle.btn_baise_style} `}
                            style={{width:"30%",height:"40px",maxWidth:"200px"}}
                            onClick={this.clickAnswerRegBtn}
                            disabled={this.isAllAnswerCompleted()}
                        >
                            <span>回答登録</span>
                        </button>
                    </Box>
                )}
                <Spacing bottom={2} />
                <Box col12>
                    <div className={CustomStyle.scroll_container} style={{height: isAdmin&&!editable?"47vh":"37.5vh", overflowX: "auto"}} >
                        <table className={CustomStyle.selection_table+" no-sort"}>
                            <thead className={ CustomStyle.fixHeader} >
                                {
                                    //編集不可 or 事業者の場合
                                }
                                {(!editable || !isAdmin) && (
                                    <tr className={CustomStyle.table_header}>
                                        <th style={{width:"25%"}}>関連条項</th>
                                        <th style={{width:"35%"}} colSpan={ledgerMasterCount + 1}>協議対象/行政回答</th>
                                        <th style={{width:"20%"}} colSpan="2">事業者合意登録</th>
                                        <th style={{width:"20%"}} colSpan="2">行政確定登録</th>
                                    </tr>
                                )}
                                {
                                    //編集可能 and 行政の場合
                                }
                                {(editable && isAdmin) && (
                                    <>
                                        <tr className={CustomStyle.table_header}>
                                            <th style={{width:"28%"}} colSpan="2">関連条項</th>
                                            <th style={{width:"25%"}} colSpan={ledgerMasterCount + 1}>協議対象/行政回答</th>
                                            <th style={{width:"15%"}} colSpan="2">事業者合意登録</th>
                                            <th style={{width:"32%"}} colSpan="3">行政確定登録</th>
                                        </tr>
                                        <tr className={CustomStyle.table_header} style={{fontSize:".8em"}}>
                                            <th style={{width:"8%", minWidth:"100px"}}></th>
                                            <th style={{width:"20%", minWidth:"230px"}}></th>
                                            {ledgerMaster && Object.keys(ledgerMaster).map(i => (
                                                <th style={{width:"5%", minWidth:"60px"}}>{ledgerMaster[i].displayName}</th>
                                            ))}
                                            <th style={{width:"20%", minWidth:"230px"}}></th>
                                            <th style={{width:"7%", minWidth:"80px"}}>ステータス</th>
                                            <th style={{width:"8%", minWidth:"100px"}}>日付</th>
                                            <th style={{width:"7%", minWidth:"80px"}}>ステータス</th>
                                            <th style={{width:"8%", minWidth:"100px"}}>日付</th>
                                            <th style={{width:"17%", minWidth:"200px"}}>コメント</th>
                                        </tr>
                                    </>
                                )}
                            </thead>
                            <tbody>
                                {departmentAnswers && Object.keys(departmentAnswers).map(key1 => (
                                    <>
                                        {
                                           //編集不可 or 事業者の場合
                                        }
                                        {(!editable || !isAdmin) && (
                                            <tr id={"tr" + key1} 
                                                onClick={e => {this.clickAnswer(e, 0, departmentAnswers[key1]["departmentAnswerId"])}} 
                                                className={`${CustomStyle.title_row} ${departmentAnswers[key1]["departmentAnswerId"] == selectedDepartmentAnswerId && (selectedAnswerId == 0 || selectedAnswerId == undefined)? CustomStyle.is_selected : ""}`}
                                            >
                                                <td colSpan={ledgerMasterCount + 4}>
                                                    <div className={CustomStyle.flex_center}>
                                                        {this.getChatButtonType(departmentAnswers[key1]) === 3 && (
                                                            <button className={`${CustomStyle.chat_button}`}
                                                                onClick={e => {
                                                                    this.props.viewState.moveToChatView(
                                                                        this.props.applicationId,
                                                                        this.props.viewState.checkedApplicationStepId,
                                                                        departmentAnswers[key1].departmentAnswerId,
                                                                        0);
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
                                                        {this.getChatButtonType(departmentAnswers[key1]) === 2 && (
                                                            <button className={`${CustomStyle.chat_button}`}
                                                                onClick={e => {
                                                                    this.props.viewState.moveToChatView(
                                                                        this.props.applicationId,
                                                                        this.props.viewState.checkedApplicationStepId,
                                                                        departmentAnswers[key1].departmentAnswerId,
                                                                        0);
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

                                                        {departmentAnswers[key1].department?.departmentName}
                                                        {/* 行政確定通知許可アイコン */}
                                                        {isAdmin && departmentAnswers[key1]["governmentConfirmPermissionFlag"] == true && (
                                                            <div className={CustomStyle.ellipse}>
                                                                <StyledIcon 
                                                                    glyph={Icon.GLYPHS.checked}
                                                                    styledWidth={"20px"}
                                                                    styledHeight={"20px"}
                                                                    light
                                                                />
                                                            </div>
                                                        )}
                                                        {/* 回答ファイル有無アイコン */}
                                                        {departmentAnswers[key1]["answerFiles"].length > 0 && (
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
                                                <td>
                                                    {
                                                        <>
                                                        <div className={CustomStyle.flex_center} style={{justifyContent:"space-between"}}>
                                                            <div className={CustomStyle.flex_center} >
                                                                {departmentAnswers[key1].governmentConfirmStatus == "0" && (
                                                                    <span>{`合意`}</span>
                                                                )}
                                                                {departmentAnswers[key1].governmentConfirmStatus == "1" && (
                                                                    <span>{`取下`}</span>
                                                                )}
                                                                {departmentAnswers[key1].governmentConfirmStatus == "2" && (
                                                                    <span>{`却下`}</span>
                                                                )}
                                                                <span style={{marginRight: "10px"}}></span>
                                                                <span className={CustomStyle.info_icon} style={{display:departmentAnswers[key1]["governmentConfirmComment"]? "":"none"}}>
                                                                    <StyledIcon 
                                                                        glyph={Icon.GLYPHS.info}
                                                                        styledWidth={"20px"}
                                                                        styledHeight={"20px"}
                                                                        light
                                                                    />
                                                                    <span className={CustomStyle.info_comment}>{departmentAnswers[key1]["governmentConfirmComment"]?departmentAnswers[key1]["governmentConfirmComment"]:""}</span>
                                                                </span>
                                                            </div>
                                                            <div></div>
                                                        </div>
                                                        </>
                                                    }
                                                </td>
                                                <td>{departmentAnswers[key1]["governmentConfirmDatetime"]?departmentAnswers[key1]["governmentConfirmDatetime"]:""}</td>
                                            </tr>
                                        )}
                                        {
                                           //編集可 and 行政の場合
                                        }
                                        {(editable && isAdmin) && (
                                            <tr id={"tr" + key1}
                                                onClick={e => {this.clickAnswer(e, 0, departmentAnswers[key1]["departmentAnswerId"])}} 
                                                className={`${CustomStyle.title_row} 
                                                            ${errorsDepartmentAnswerIds.some(id => id == departmentAnswers[key1].departmentAnswerId) ? CustomStyle.error_line :""} 
                                                            ${departmentAnswers[key1]["departmentAnswerId"] == selectedDepartmentAnswerId? CustomStyle.is_selected : ""}`}
                                            >
                                                <td>
                                                    {this.haveEditableAmswer(departmentAnswers[key1]) && (
                                                        <button
                                                            className={`${CustomStyle.btn_baise_style} `}
                                                            style={{margin:"5px 10px"}}
                                                            onClick={e => {this.addAnswer(e, key1)}}
                                                        >
                                                            <span>回答追加</span>
                                                        </button>
                                                    )}
                                                </td>
                                                <td colSpan={ledgerMasterCount + 4}>
                                                    <div className={CustomStyle.flex_center}>
                                                        {departmentAnswers[key1].department?.departmentName}
                                                        {departmentAnswers[key1]["answerFiles"].length > 0 && (
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
                                                <td>
                                                    {this.isEditableForDepartmentAnswer(departmentAnswers[key1]) &&(
                                                        <select 
                                                            value={departmentAnswers[key1]?.governmentConfirmStatus}
                                                            onChange={e => this.governmentConfirmStatusChange(key1, null, e.target.value)}
                                                        >
                                                            <option></option>
                                                            <option value={"0"}>合意</option>
                                                        </select>
                                                    )}
                                                    {!this.isEditableForDepartmentAnswer(departmentAnswers[key1]) &&(
                                                       <>
                                                            {departmentAnswers[key1].governmentConfirmStatus == "0" && (
                                                                <span>{`合意`}</span>
                                                            )}
                                                            <span>{``}</span>
                                                        </>
                                                    )}
                                                </td>
                                                <td>
                                                    {this.isEditableForDepartmentAnswer(departmentAnswers[key1]) && (

                                                        <input type="date" max={"9999-12-31"} min={"2000-01-01"}
                                                            value={this.formatToInputdate(departmentAnswers[key1]["governmentConfirmDatetime"])}
                                                            style={{maxWidth:"110px"}}
                                                            onChange={(event) => { this.inputGovernmentConfirmDatetimeChange(key1, null, event.target.value) }}
                                                        />
                                                    )}
                                                    {!this.isEditableForDepartmentAnswer(departmentAnswers[key1]) && (
                                                        departmentAnswers[key1]["governmentConfirmDatetime"]?departmentAnswers[key1]["governmentConfirmDatetime"]:""
                                                    )}
                                                </td>
                                                <td>
                                                    {this.isEditableForDepartmentAnswer(departmentAnswers[key1]) && (
                                                        <textarea 
                                                            value={departmentAnswers[key1]["governmentConfirmComment"]?departmentAnswers[key1]["governmentConfirmComment"]:""}
                                                            style={{width:"auto"}}
                                                            className={CustomStyle.custom_textarea}
                                                            rows="5" type="text" placeholder="" 
                                                            maxLength={maxLength + inputBuffLength}
                                                            autoComplete="off"
                                                            onChange={(event) => { this.inputGovernmentConfirmCommentChange(key1, null, event.target.value, maxLength) }}
                                                        >
                                                        </textarea>
                                                    )}
                                                    {!this.isEditableForDepartmentAnswer(departmentAnswers[key1]) && (
                                                        departmentAnswers[key1]["governmentConfirmComment"]?departmentAnswers[key1]["governmentConfirmComment"]:""  
                                                    )}
                                                </td>
                                            </tr>
                                        )}
                                        {Object.keys(departmentAnswers[key1].answers).map(key2 => (
                                        <>
                                        {
                                            //編集不可 or 事業者の場合
                                        }
                                        {(!editable || !isAdmin) && (
                                            <tr key={"tr" + key1 + "" + key2 } onClick={e => {this.clickAnswer(e, departmentAnswers[key1].answers[key2]["answerId"], departmentAnswers[key1]["departmentAnswerId"])}} 
                                                className={`${departmentAnswers[key1].answers[key2]["answerId"] == selectedAnswerId? CustomStyle.is_selected : ""} ${departmentAnswers[key1].answers[key2]["permissionJudgementMigrationFlag"] == true? CustomStyle.is_notCheckItem:""}`}
                                            >
                                                <td style={{ minWidth:"230px"}}>
                                                    {departmentAnswers[key1].answers[key2]["judgementInformation"]["title"]}
                                                </td>

                                                {
                                                //32協議対象
                                                }
                                                {departmentAnswers[key1].answers[key2].discussionItems && Object.keys(departmentAnswers[key1].answers[key2].discussionItems).map(i => (
                                                    <td style={{minWidth:"60px"}}>{departmentAnswers[key1].answers[key2].discussionItems[i].checked? departmentAnswers[key1].answers[key2].discussionItems[i].displayName:""}</td>
                                                ))}
                                                <td style={{ minWidth:"230px"}}>
                                                    {
                                                    //行政回答
                                                    }
                                                    <div className={CustomStyle.flex_center} style={{justifyContent:"space-between"}}>
                                                        <span>{departmentAnswers[key1].answers[key2].answerContent}</span>
                                                        {/* 回答通知許可アイコン */}
                                                        {isAdmin && departmentAnswers[key1].answers[key2]["answerPermissionFlag"] == true && (
                                                            <div className={CustomStyle.ellipse_small}>
                                                                <StyledIcon 
                                                                    glyph={Icon.GLYPHS.checked}
                                                                    styledWidth={"15px"}
                                                                    styledHeight={"15px"}
                                                                    light
                                                                />
                                                            </div>
                                                        )}
                                                    </div>
                                                </td>
                                                <td  style={{ minWidth:"80px"}}>
                                                    {
                                                    //事業者合否内容
                                                    }
                                                    {(editable && !this.isInputed(departmentAnswers[key1].answers[key2].businessPassStatus)) && (
                                                        <div className={CustomStyle.flex_center} style={{justifyContent:"space-between"}}>
                                                            <div className={CustomStyle.flex_center} >
                                                                <select id={'businessPassStatus_'+key1+'_'+key2} 
                                                                    defaultValue={departmentAnswers[key1].answers[key2].businessPassStatus ?? ""}
                                                                    disabled={!departmentAnswers[key1].answers[key2].editable}
                                                                >
                                                                    <option value={""}></option>
                                                                    {/* <option value={"0"}>否決</option> */}
                                                                    <option value={"1"}>合意</option>
                                                                </select>
                                                                {/* 事業者回答登録する前に、行政回答内容が上書き更新した場合、ポップアップで案内文言を表示 */}
                                                                { departmentAnswers[key1].answers[key2].notifiedFlag == true && (
                                                                    <>
                                                                        <span style={{marginRight: "10px"}}></span>
                                                                        <span className={CustomStyle.info_icon} style={{display:departmentAnswers[key1].answers[key2].editable? "none":""}}>
                                                                            <StyledIcon 
                                                                                glyph={Icon.GLYPHS.info}
                                                                                styledWidth={"20px"}
                                                                                styledHeight={"20px"}
                                                                                light
                                                                            />
                                                                            <span className={CustomStyle.info_comment}>{Config.config.answerContentUpdatingInfoText}</span>
                                                                        </span>
                                                                    </>
                                                                )}
                                                            </div>
                                                        </div>
                                                    )}
                                                    {(!editable || this.isInputed(departmentAnswers[key1].answers[key2].businessPassStatus) )&& (
                                                        <>
                                                            {(departmentAnswers[key1].answers[key2].businessPassStatus ?? "") == "" && (
                                                                <span>{``}</span>
                                                            )}
                                                            {departmentAnswers[key1].answers[key2].businessPassStatus == "1" && (
                                                                <span>{`合意`}</span>
                                                            )}
                                                            <span>{``}</span>
                                                       </>
                                                    )}
                                                </td>
                                                <td style={{ minWidth:"100px"}}>
                                                    {
                                                    //事業者回答登録日付
                                                    }
                                                    {(editable && !this.isInputed(departmentAnswers[key1].answers[key2].businessPassStatus)) && (
                                                        <input type="date" style={{maxWidth:"110px"}} max={"9999-12-31"} min={"2000-01-01"}
                                                            id={'businessAnswerDatetime_'+key1+'_'+key2}
                                                            value={departmentAnswers[key1].answers[key2].businessAnswerDatetime}
                                                            disabled={!departmentAnswers[key1].answers[key2].editable}
                                                        />
                                                    )}
                                                    {(!editable || this.isInputed(departmentAnswers[key1].answers[key2].businessPassStatus))&& (
                                                        <span>{this.formatToDispalyDate(departmentAnswers[key1].answers[key2]["businessAnswerDatetime"])}</span>
                                                    )}
                                                </td>
                                                <td style={{ minWidth:"130px"}}>
                                                    {
                                                     //：行政確定登録ステータス
                                                    }
                                                    <div className={CustomStyle.flex_center} style={{justifyContent:"space-between"}}>
                                                        <div className={CustomStyle.flex_center} >
                                                            {departmentAnswers[key1].answers[key2].governmentConfirmStatus == "0" && (
                                                                <span>{`合意`}</span>
                                                            )}
                                                            <span style={{marginRight: "10px"}}></span>
                                                            <span className={CustomStyle.info_icon} style={{display:departmentAnswers[key1].answers[key2]["governmentConfirmComment"]? "":"none"}}>
                                                                <StyledIcon 
                                                                    glyph={Icon.GLYPHS.info}
                                                                    styledWidth={"20px"}
                                                                    styledHeight={"20px"}
                                                                    light
                                                                />
                                                                <span className={CustomStyle.info_comment}>{departmentAnswers[key1].answers[key2]["governmentConfirmComment"]?departmentAnswers[key1].answers[key2]["governmentConfirmComment"]:""}</span>
                                                            </span>
                                                        </div>
                                                        <div>
                                                            {isAdmin && departmentAnswers[key1].answers[key2]["governmentConfirmPermissionFlag"] == true && (
                                                                <div className={CustomStyle.ellipse_small}>
                                                                    <StyledIcon 
                                                                        glyph={Icon.GLYPHS.checked}
                                                                        styledWidth={"15px"}
                                                                        styledHeight={"15px"}
                                                                        light
                                                                    />
                                                                </div>
                                                            )}
                                                        </div>
                                                    </div>
                                                </td>
                                                <td style={{ minWidth:"100px"}}>
                                                    {
                                                    //：行政確定登録日時
                                                    }
                                                    <span>{departmentAnswers[key1].answers[key2].governmentConfirmDatetime}</span>
                                                </td>
                                            </tr>
                                        )}
                                        {
                                            //編集可 and 行政の場合
                                        }
                                        {(editable && isAdmin) && (
                                            <tr id={"tr" + key1 + "" + key2 } 
                                                className={`${errorsAnswerIds.some(id => id == departmentAnswers[key1].answers[key2].answerId) ? CustomStyle.error_line :""}
                                                            ${departmentAnswers[key1].answers[key2]["answerDataType"] == "5" || departmentAnswers[key1].answers[key2]["answerDataType"] == "6" || departmentAnswers[key1].answers[key2]["answerDataType"] == "7"? CustomStyle.is_deleted : ""}
                                                            ${departmentAnswers[key1].answers[key2]["permissionJudgementMigrationFlag"] == true ? CustomStyle.is_deleted :departmentAnswers[key1].answers[key2]["answerDataType"] == "2" || departmentAnswers[key1].answers[key2]["answerDataType"] == "3" ? CustomStyle.is_added :""}    
                                                    `}
                                            >
                                                <td>
                                                    {/* 削除 */}
                                                    {this.isDeleteable(departmentAnswers[key1].answers[key2], true)  && (
                                                        <button
                                                            className={CustomStyle.delete_button}
                                                            style={{ margin: "5px 10px"}}
                                                            onClick={e => {this.deleteAnswer(e, key1, key2)}}
                                                        >削除</button>
                                                    )}
                                                    {/* 回復 */}
                                                    {this.isDeleteable(departmentAnswers[key1].answers[key2], false) && (
                                                        <button
                                                            className={CustomStyle.delete_button}
                                                            style={{ margin: "5px 10px"}}
                                                            onClick={e => {this.deleteAnswerRevert(e, key1, key2)}}
                                                        >元に戻す</button>
                                                    )}
                                                </td>
                                                <td>
                                                    {departmentAnswers[key1].answers[key2]["judgementInformation"]["title"]}
                                                </td>
                                                {
                                                //：32協議対象
                                                }
                                                {departmentAnswers[key1].answers[key2].discussionItems && Object.keys(departmentAnswers[key1].answers[key2].discussionItems).map(i => (
                                                    <td style={{minWidth:"60px"}}>
                                                        {this.isEditable(departmentAnswers[key1].answers[key2], false) && (
                                                            <label className={`${CustomStyle.control} ${CustomStyle.controlCheckbox}`}>
                                                                <input type="checkbox" 
                                                                    onChange={ evt => { this.checkedDiscussionItemChange(key1, key2, i, evt.target.checked) } }
                                                                    checked={departmentAnswers[key1].answers[key2].discussionItems[i].checked}
                                                                />
                                                                <div className={CustomStyle.controlIndicator}></div>
                                                            </label>
                                                        )}
                                                        {(!this.isEditable(departmentAnswers[key1].answers[key2], false)) && (
                                                            departmentAnswers[key1].answers[key2].discussionItems[i].checked? departmentAnswers[key1].answers[key2].discussionItems[i].displayName:""
                                                        )}
                                                    </td>
                                                ))}
                                                <td>
                                                    {
                                                    //：行政回答
                                                    }
                                                    {this.isEditable(departmentAnswers[key1].answers[key2], false) && departmentAnswers[key1].answers[key2].answerContentEditable && (
                                                       <div>
                                                            <button
                                                                className={CustomStyle.template_button}
                                                                style={{ margin: "0 10px"}}
                                                                onClick={e => {
                                                                this.showTemplate(departmentAnswers[key1].answers[key2]);
                                                                }}
                                                            >回答入力</button>
                                                            <textarea 
                                                                className={CustomStyle.custom_textarea}
                                                                rows="5" type="text" placeholder="" 
                                                                maxLength={maxLength + inputBuffLength}
                                                                autoComplete="off"
                                                                value={departmentAnswers[key1].answers[key2]?.answerContent}
                                                                onChange={(event) => { this.inputChange(key1, key2, event.target.value, maxLength) }}
                                                            ></textarea>
                                                        </div>
                                                    )}
                                                    {(!this.isEditable(departmentAnswers[key1].answers[key2], false) || !departmentAnswers[key1].answers[key2].answerContentEditable) && (
                                                        departmentAnswers[key1].answers[key2]?.answerContent
                                                    )}
                                                </td>
                                                <td style={{minWidth:"80px"}}>
                                                    {
                                                    //：事業者合否ステータス
                                                    }
                                                    <>
                                                        {departmentAnswers[key1].answers[key2].businessPassStatus == "1" && (
                                                            <span>{`合意`}</span>
                                                        )}
                                                        <span>{``}</span>
                                                    </>
                                                </td>
                                                <td style={{minWidth:"100px"}}>
                                                    {
                                                    // ：事業者合意日付
                                                    }
                                                    <span>{this.formatToDispalyDate(departmentAnswers[key1].answers[key2].businessAnswerDatetime)}</span>
                                                </td>
                                                <td>
                                                    {
                                                    // 行政確定ステータス(
                                                    // ・行政確定通知許可フラグがTRUEの場合、入力不可
                                                    // ・部署全体の行政確定登録が入力済みであれば、配下の回答の行政確定登録が入力不可
                                                    }
                                                    {this.isEditable(departmentAnswers[key1].answers[key2], true) && (
                                                     <>
                                                            {departmentAnswers[key1].answers[key2]?.businessPassStatus == "1" && (
                                                                <select 
                                                                    value={departmentAnswers[key1].answers[key2].governmentConfirmStatus}
                                                                    disabled={this.isInputedDepartmentAnswer(departmentAnswers[key1])}
                                                                    onChange={e => this.governmentConfirmStatusChange(key1, key2, e.target.value)}
                                                                >
                                                                    <option></option>
                                                                    <option value={"0"}>合意</option>
                                                                </select>
                                                            )}
                                                            {!departmentAnswers[key1].answers[key2]?.businessPassStatus && (
                                                                ""
                                                            )}
                                                        </>
                                                    )}
                                                    {(!this.isEditable(departmentAnswers[key1].answers[key2], true))&& (
                                                        <>
                                                            {departmentAnswers[key1].answers[key2]?.governmentConfirmStatus == "0" && (
                                                                <span>{`合意`}</span>
                                                            )}
                                                            {departmentAnswers[key1].answers[key2]?.governmentConfirmStatus == "1" && (
                                                                <span>{`取下`}</span>
                                                            )}
                                                            {departmentAnswers[key1].answers[key2]?.governmentConfirmStatus == "2" && (
                                                                <span>{`却下`}</span>
                                                            )}
                                                            <span>{``}</span>
                                                        </>
                                                    )}
                                                </td>
                                                <td>
                                                    {
                                                    //：行政確定登録日時
                                                    }
                                                    {this.isEditable(departmentAnswers[key1].answers[key2], true) && (
                                                        <input type="date" style={{maxWidth:"110px"}} max={"9999-12-31"} min={"2000-01-01"}
                                                            value={this.formatToInputdate(departmentAnswers[key1].answers[key2]["governmentConfirmDatetime"])} 
                                                            disabled={this.isInputedDepartmentAnswer(departmentAnswers[key1])}
                                                            onChange={(event) => {
                                                                this.inputGovernmentConfirmDatetimeChange(key1, key2, event.target.value);
                                                            }} 
                                                        />
                                                    )}
                                                    {(!this.isEditable(departmentAnswers[key1].answers[key2], true) ) && (
                                                       <span>{this.formatToDispalyDate(departmentAnswers[key1].answers[key2]["governmentConfirmDatetime"])}</span>
                                                    )}
                                                </td>
                                                <td>
                                                    {
                                                    //：行政コメント
                                                    }
                                                    {this.isEditable(departmentAnswers[key1].answers[key2], true) && (
                                                        <textarea 
                                                            style={{width:"auto"}}
                                                            className={CustomStyle.custom_textarea}
                                                            rows="5" type="text" placeholder="" 
                                                            maxLength={maxLength + inputBuffLength}
                                                            autoComplete="off"
                                                            value={departmentAnswers[key1].answers[key2]["governmentConfirmComment"]?departmentAnswers[key1].answers[key2]["governmentConfirmComment"]:""}
                                                            disabled={this.isInputedDepartmentAnswer(departmentAnswers[key1])}
                                                            onChange={(event) => {
                                                                this.inputGovernmentConfirmCommentChange(key1, key2, event.target.value, maxLength);
                                                            }}
                                                        >
                                                        </textarea>
                                                    )}
                                                    {!this.isEditable(departmentAnswers[key1].answers[key2], true) && (
                                                        departmentAnswers[key1].answers[key2]["governmentConfirmComment"]?departmentAnswers[key1].answers[key2]["governmentConfirmComment"]:""
                                                    )}
                                                </td>
                                            </tr>
                                        )}
                                        </>
                                        ))}
                                    </>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </Box>
            </Box>
        );
    }
}
export default withTranslation()(withTheme(NegotiationContentList));