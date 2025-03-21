import React from "react";
import PropTypes from "prop-types";
import { observer } from "mobx-react";
import { withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import Box from "../../../../Styled/Box";
import CustomStyle from "./scss/ConfirmAnswerNotificationModal.scss";
import { RawButton } from "../../../../Styled/Button";
import Icon, { StyledIcon } from "../../../../Styled/Icon";
import Config from "../../../../../customconfig.json";

/**
 * 行政用コンポーネント：回答通知確認ダイアログ
 */

@observer
class ConfirmAnswerNotificationModal extends React.Component {
    static displayName = "ConfirmAnswerNotificationModal"
    static propsType = {
        terria: PropTypes.object.isRequired,
        viewState: PropTypes.object.isRequired
    }

    constructor(props) {
        super(props);
        this.state = {
            viewState: props.viewState,
            terria: props.terria,
            // 選択中申請段階（回答通知確認画面）
            checkedApplicationStepId: props.viewState.checkedApplicationStepId,
            // 選択中申請段階（申請情報詳細画面）
            initApplicationStepId: props.viewState.checkedApplicationStepId,
            // 申請段階
            applicationSteps: [], 
            //協議対象一覧
            ledgerMaster:[],
            //回答リスト(表示中リスト)
            answers:[],
            //回答リスト(表示中リスト)
            displayAnswers:[],
            //部署回答リスト(表示中リスト)
            departmentAnswers:[],
            //部署回答リスト(表示中リスト)
            displayDepartmentAnswers:[],
            //申請回答DTO
            applyAnswerForm: {},
            //全ての回答表示フラグ
            displayAllAnswer: true,
            // エラーレコード
            errors:{answers:[],departmentAnswers:[]},
            //全選択（事前相談、許可判定のみ）
            selectedAll: false,
            //受付フラグ
            acceptingFlag: "1",
            //受付版情報
            acceptVersionInformation: 0,
            // 統括部署管理者 
            controlDepartmentAdmin : false,
            // 通知種類(0:事業者に回答通知、1：事業者に差戻、2：担当課に受付通知、3：統括部署管理者に回答許可通知、4：統括部署管理者に行政確定登録許可通知)
            notifyType: "0",
            //申請受付・差戻のコメント
            acceptCommentText:"",
            // 更新日時（申請版情報の更新日時）
            updateDatetime:null
        }
    }

    /**
     * 初期処理
     */
    componentDidMount() {
        this.draggable(document.getElementById('confirmAnswerNotificationModalDrag'), document.getElementById('confirmAnswerNotificationModal'));
        document.getElementById("customloader_main").style.display = "block";

        // 申請回答情報検索検索
        fetch(Config.config.apiUrl + "/application/detail/" + this.props.viewState.applicationInformationSearchForApplicationId)
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
            if (res.applicationId) {
                // 検索結果をStateにセット
                this.setApplyAnswerInfoToState(JSON.parse(JSON.stringify(res)), null, null, true, null);
            } else {
                alert("回答の取得に失敗しました。再度操作をやり直してください。");
            }
            
        }).catch(error => {
            console.error('通信処理に失敗しました', error);
            alert('通信処理に失敗しました');
        }).finally(() => document.getElementById("customloader_main").style.display = "none");
        
    }

    /**
     * 申請回答DTOからstateにセット
     * @param {*} applyAnswerForm 申請回答DTO
     * @param {*} displayAllAnswer 未通知のみ表示制御フラグ
     * @param {*} checkedApplicationStepId 選択される申請段階ID
     * @param {*} initFlag 申請段階変わるフラグ
     * @param {*} notifyType 通知種類
     */
    setApplyAnswerInfoToState(applyAnswerForm, displayAllAnswer, checkedApplicationStepId, initFlag, notifyType){
        if(applyAnswerForm == null){
            applyAnswerForm = JSON.parse(JSON.stringify(this.state.applyAnswerForm));
        }

        let changeDisplayAllAnswer = true;
        if(displayAllAnswer == null){
            changeDisplayAllAnswer = false;
            displayAllAnswer = this.state.displayAllAnswer;
        }
        if(checkedApplicationStepId == null ){
            checkedApplicationStepId = applyAnswerForm.applyAnswerDetails[0].applicationStepId;
        }
        if(notifyType == null){
            notifyType = this.state.notifyType;
        }

        let answers = this.state.answers;
        let answersBackup = JSON.parse(JSON.stringify(this.state.answers));
        let departmentAnswers = this.state.departmentAnswers;
        let departmentAnswersBackup  = JSON.parse(JSON.stringify(this.state.departmentAnswers));
        let ledgerMaster = this.state.ledgerMaster;
        let displayAnswers = [];
        let displayDepartmentAnswers = [];
        // 統括部署管理者
        let controlDepartmentAdmin = this.state.controlDepartmentAdmin;
        let acceptingFlag = this.state.acceptingFlag;
        let acceptVersionInformation = this.state.acceptVersionInformation;
        let updateDatetime = this.state.updateDatetime;

        controlDepartmentAdmin = applyAnswerForm.controlDepartmentAdmin;
        Object.keys(applyAnswerForm.applyAnswerDetails).map(key => {
            let applyAnswerDetailForm = applyAnswerForm.applyAnswerDetails[key];
            if(applyAnswerDetailForm.applicationStepId == checkedApplicationStepId && ((checkedApplicationStepId == 2 && !applyAnswerDetailForm.isAcceptInfo)|| (checkedApplicationStepId != 2))){
                answers = JSON.parse(JSON.stringify(applyAnswerDetailForm.answers));
                departmentAnswers = JSON.parse(JSON.stringify(applyAnswerDetailForm.departmentAnswers));
                ledgerMaster = applyAnswerDetailForm.ledgerMasters;
                acceptingFlag = applyAnswerDetailForm.acceptingFlag;
                acceptVersionInformation = applyAnswerDetailForm.acceptVersionInformation
                updateDatetime = applyAnswerDetailForm.updateDatetime;
            }
        });

        if(initFlag){
            if(checkedApplicationStepId == 2 ){
                if(acceptingFlag == "0" && controlDepartmentAdmin ){
                    // ログインユーザーが統括部署管理者、かつ、この申請の受付フラグが「0：未確認」の場合、担当課に申請受付通知
                    notifyType = "2";
                }else{
                    // 4：統括部署管理者に回答許可通知
                    notifyType = "3";
                }
            }else{
                // 0：事業者に回答通知
                notifyType = "0";
            }
        }

        // 未通知のみを表示がOFFの場合、
        if(displayAllAnswer){
            Object.keys(answers).map(key => {
                answers[key]["displayFlag"] = true;
            });
            
            Object.keys(departmentAnswers).map(key1 => {
                departmentAnswers[key1]["displayFlag"] = true;
                Object.keys(departmentAnswers[key1].answers).map(key2 => {
                    departmentAnswers[key1].answers[key2]["displayFlag"] = true;
                });
            });
        }else{

            Object.keys(answers).map(key => {
                answers[key]["displayFlag"] = this.isDisplayed(answers[key], false, notifyType);
            });
            
            Object.keys(departmentAnswers).map(key1 => {
                departmentAnswers[key1]["displayFlag"] = this.isDisplayed(departmentAnswers[key1], true, notifyType);
                Object.keys(departmentAnswers[key1].answers).map(key2 => {
                    departmentAnswers[key1].answers[key2]["displayFlag"] = this.isDisplayed(departmentAnswers[key1].answers[key2], false, notifyType);
                });
            });
        }

        displayAnswers = answers.filter(answer=> answer.displayFlag === true);
        displayDepartmentAnswers = departmentAnswers.filter(answer=>answer.displayFlag === true);
        Object.keys(displayDepartmentAnswers).map(key1 => {
            let list = departmentAnswers[key1].answers.filter(answer=>answer.displayFlag === true);
            departmentAnswers[key1].answers = list;
        });

        let unSelectedCount = 0;
        // 未通知のみ表示の切り替えイベントの場合、既に選択中条項を選択中にする
        if(changeDisplayAllAnswer){

            Object.keys(displayAnswers).map(key => {

                let index = answersBackup.findIndex(ans => ans.answerId == displayAnswers[key].answerId);

                if(index > -1){
                    displayAnswers[key].checked = answersBackup[index].checked;
                }else{
                    displayAnswers[key].checked = false;
                }

                // 通知選択可能対象リストに、未選択のレコードがあれば、全選択がOFFにする
                if(displayAnswers[key].notificable == true){
                    if(displayAnswers[key].checked == false){
                        unSelectedCount ++;
                    }
                }
            })

            Object.keys(displayDepartmentAnswers).map(key1 => {

                let index1 =  -1;
                if(departmentAnswersBackup && Object.keys(departmentAnswersBackup).length > 0 ){
                    index1 = departmentAnswersBackup.findIndex(ans => ans.departmentAnswerId == displayDepartmentAnswers[key1].departmentAnswerId);
                }
                if(index1 > -1){
                    displayDepartmentAnswers[key1].checked = departmentAnswersBackup[index1].checked;
                    Object.keys(displayDepartmentAnswers[key1].answers).map(key2 => {
                        let index2 = -1;
                        if(departmentAnswersBackup[index1].answers && Object.keys(departmentAnswersBackup[index1].answers).length > 0 ){

                            index2 = departmentAnswersBackup[index1].answers.findIndex(ans => ans.answerId == displayDepartmentAnswers[key1].answers[key2].answerId);
                        }

                        if(index2 > -1){
                            displayDepartmentAnswers[key1].answers[key2].checked = departmentAnswersBackup[index1].answers[index2].checked;
                        }else{
                            displayDepartmentAnswers[key1].answers[key2].checked = false;
                        }
                    })
                }else{
                    displayDepartmentAnswers[key1].checked = false;
                    Object.keys(displayDepartmentAnswers[key1].answers).map(key2 => {
                        displayDepartmentAnswers[key1].answers[key2].checked = false;
                    })
                }

            })
        }

    if(initFlag){

        this.setState({
            applyAnswerForm: applyAnswerForm,
            answers: displayAnswers,
            departmentAnswers: displayDepartmentAnswers,
            displayAllAnswer: displayAllAnswer,
            ledgerMaster: ledgerMaster,
            checkedApplicationStepId: checkedApplicationStepId,
            controlDepartmentAdmin: controlDepartmentAdmin,
            notifyType: notifyType,
            acceptingFlag: acceptingFlag,
            acceptVersionInformation: acceptVersionInformation,
            updateDatetime: updateDatetime
        });
    }else{
        this.setState({
            answers: displayAnswers,
            departmentAnswers: displayDepartmentAnswers,
            displayAllAnswer: displayAllAnswer,
            notifyType: notifyType,
            selectedAll: unSelectedCount > 0 ? false: true
        });
    }
    }

    /**
     * 回答可能な申請段階リスト取得
     */
    getApplicationStepList(){
        const applicationId = this.props.viewState.applicationInformationSearchForApplicationId;
        fetch(Config.config.apiUrl + "/application/applicationStep/" + applicationId + "/true")
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
            if (Object.keys(res).length > 0) {
                // 前画面に、選択中タブに対する申請種別がデフォルト選択されるにする
                let checkedApplicationStepId = this.state.checkedApplicationStepId;
                Object.keys(res).map( index =>{
                    
                    if(res[index].applicationStepId == checkedApplicationStepId ){
                        res[index].checked = true;
                    }
                });
                this.setState({applicationSteps: res});
            } else {
                alert("申請段階リスト取得に失敗しました。再度操作をやり直してください。");
            }
        }).catch(error => {
            console.error('通信処理に失敗しました', error);
            alert('通信処理に失敗しました');
        });
    }
    /**
     * 画面に表示可能の協議リスト取得
     */
    getLedgerMasterList(){
        const applicationStepId = 2;
        // 協議対象一覧を取得
        fetch(Config.config.apiUrl + "/answer/ledger/" + applicationStepId)
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
            if (Object.keys(res).length > 0) {
                this.setState({ledgerMaster: res});
            } else {
                alert("協議対象一覧に失敗しました。再度操作をやり直してください。");
            }
        }).catch(error => {
            console.error('通信処理に失敗しました', error);
            alert('通信処理に失敗しました');
        });
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
     * checkbox選択値を切り替え
     * @param {*} event 
     * @param {*} key1 
     * @param {*} key2 
     */
    changeChecked(checked, key1, key2){

        let checkedApplicationStepId = this.state.checkedApplicationStepId;

        //事前相談、許可判定
        if(checkedApplicationStepId == 1 || checkedApplicationStepId == 3 ){
            let answers = this.state.answers;

            answers[key1].checked =  checked;

            let selectedAll = true;
            Object.keys(answers).map(i => {
                if(answers[i].notificable === true){
                    if(answers[i].checked == false){
                        selectedAll = false;
                    }
                }
            })
            this.setState({answers: answers, selectedAll: selectedAll});

        }
        
        // 事前協議
        if(checkedApplicationStepId == 2 ){

            let departmentAnswers = this.state.departmentAnswers;  

            if(key2 == null){
                departmentAnswers[key1].checked = checked;
                departmentAnswers[key1].answers.forEach(item => {

                    // 通知種類
                    const notifyType = this.state.notifyType;

                    // 画面に、チェックボックスが表示されるのは、選択状態より、切り替える
                    if(this.notificable(item.notificable, item.permissionNotificable, notifyType)){
                        item.checked = checked;
                    }else{
                        // 画面に、チェックボックスが表示されないのは、falseに固定
                        item.checked = false;
                    }
                });
            }else{
                departmentAnswers[key1].answers[key2].checked = checked;
    
                let index= departmentAnswers[key1].answers.findIndex((item) =>  item.checked == false);
                
                // 部署ごとに回答リスト中に、未選択のものがない場合、全選択がtrueとする
                if(index < 0){
                    departmentAnswers[key1].checked = true;
                }else{
                    departmentAnswers[key1].checked = false;
                }
            }
    
            this.setState({departmentAnswers: departmentAnswers});
        }

    }

    /**
     * 全選択の設定変更
     * @param {*} e イベント
     */
    changeSelectedAllAnswer(e){

        let checked = e.target.checked;
        let answers = this.state.answers;

        Object.keys(answers).map(key1 => {
            if(answers[key1].notificable === true){
                answers[key1].checked =  checked;
            }
        })

        this.setState({answers: answers, selectedAll: checked});
    }
    /**
     * モーダルを閉じる
     * 
     */
    close(){
        this.state.viewState.closeConfirmAnswerNotificationModal(this.state.initApplicationStepId);
    }

    /**
     * コールバック関数
     * @param {*} answerId 回答ID
     * @param {*} text 回答内容
     */
    CallBackFunction() {
        this.state.callback();
    }

    /**
     * 回答通知を行って、完了画面を開く
     */
    moveToAnswerNotificationView(){

        // 通知種類
        const notifyType = this.state.notifyType;

        // 申請段階
        let checkedApplicationStepId = this.state.checkedApplicationStepId;

        // 選択された回答リスト
        let checkedAnswers = [];
        // 選択された部署回答リスト
        let checkedDepartmentAnswers = [];

        // 選択された未回答の件数
        let unAnsewerCount = 0;
        let unAnsewerErrors = {answers:[],departmentAnswers:[]};
        // 選択された未許可の件数
        let unPermissionCount = 0;
        let unPermissionErrors = {answers:[],departmentAnswers:[]};
        let unGovernmentConfirmPermissionCount = 0;
        let unGovernmentConfirmPermissionErrors = {answers:[],departmentAnswers:[]};
        // 選択された部署全体の行政確定が未登録の件数
        let unGovernmentConfirmDeptCount = 0;
        let unGovernmentConfirmDeptErrors = {answers:[],departmentAnswers:[]};

        // 申請受付・差戻のコメント
        const acceptCommentText = this.state.acceptCommentText;
        
        // 1：事業者に差戻、2：担当課に受付通知の場合、
        if(notifyType == "1" || notifyType == "2"){

            if(!acceptCommentText){
                alert("コメントを入力してください。");
                return ;
            }
        }else{
            // 事前相談、許可判定
            if(checkedApplicationStepId == 1 || checkedApplicationStepId == 3 ){
                let answers = this.state.answers;
                answers.forEach(item => {
                    if(item.checked == true){
                        checkedAnswers.push(item);
                        if(item.completeFlag == undefined || item.completeFlag == null || item.completeFlag == false ){
                            unAnsewerCount ++;
                            unAnsewerErrors.answers.push(item.answerId);
                        }
                    }
                });
            }

            // 事前協議
            if(checkedApplicationStepId == 2 ){
                let departmentAnswers = this.state.departmentAnswers;  
                departmentAnswers.forEach( item => {
                    if(item.checked == true && this.isInputed(item.governmentConfirmDatetime)){
                        //3：統括部署管理者に回答許可通知 以外の場合、部署全体のレコードを追加
                        if(notifyType != "3"){
                            checkedDepartmentAnswers.push(item);
                        }
                        //4：統括部署管理者に行政確定登録許可通知
                        if(notifyType == "4"){
                            // 行政確定ステータスが未入力の場合、
                            if(item.governmentConfirmStatus == undefined || item.governmentConfirmStatus == null || item.governmentConfirmStatus == ""){
                                unAnsewerCount ++;
                                unAnsewerErrors.departmentAnswers.push(item.departmentAnswerId);
                            }
                        }

                        //0:事業者に回答通知
                        if(notifyType == "0"){
                            // 行政確定登録通知許可フラグが未許可の場合、
                            if(item.governmentConfirmPermissionFlag == false){
                                unGovernmentConfirmPermissionCount ++;
                                unGovernmentConfirmPermissionErrors.departmentAnswers.push(item.departmentAnswerId);
                            }
                        }

                    }

                    //4：統括部署管理者に行政確定登録許可通知
                    if(item.checked == true && notifyType == "4"){

                        //行政確定登録許可通知の場合、部署の行政確定が入力しないと、エラーとする
                        if(!this.isInputed(item.governmentConfirmDatetime) || !this.isInputed(item.governmentConfirmStatus)){
                            unGovernmentConfirmDeptCount ++;
                            unGovernmentConfirmDeptErrors.departmentAnswers.push(item.departmentAnswerId);
                        }
                    }

                    // 事業者へ済み回答
                    const answerContentNotifiedFlag = item.answerContentNotifiedFlag;
                    item.answers.forEach( ans =>{
                        if(ans.checked == true && ans.permissionJudgementMigrationFlag == false){
                            // checkedAnswers.push(ans);

                            // 行政削除した回答は未回答チェックを行わない
                            if(ans.deleteUnnotifiedFlag === undefined || ans.deleteUnnotifiedFlag === null || ans.deleteUnnotifiedFlag == false){

                                //3：統括部署管理者に回答許可通知
                                if(notifyType == "3"){
                                    if(ans.completeFlag == undefined || ans.completeFlag == null || ans.completeFlag == false){
                                        unAnsewerCount ++;
                                        unAnsewerErrors.answers.push(ans.answerId);
                                    }
                                    // 重複通知しないように、回答通知許可フラグ(0=未許可)のみを通知リストに追加
                                    if(ans.answerPermissionFlag == false){
                                        checkedAnswers.push(ans);
                                    }
                                }

                                //4：統括部署管理者に行政確定登録許可通知
                                if(notifyType == "4"){
                                    
                                    if(!this.isInputed(ans.governmentConfirmStatus) ){
                                        unGovernmentConfirmDeptCount ++;
                                        unGovernmentConfirmDeptErrors.answers.push(ans.answerId);
                                    }

                                    // 重複通知しないように、行政確定通知許可フラグ(0=未許可)のみを通知リストに追加
                                    if(ans.governmentConfirmPermissionFlag == false){
                                        checkedAnswers.push(ans);
                                    }
                                }

                                //0:事業者に回答通知
                                if(notifyType == "0"){
                                    if(ans.answerPermissionFlag== false ){
                                        // 回答通知未許可の場合, かつ、2回目の回答通知ではない場合、回答通知未許可対象とする
                                        if(answerContentNotifiedFlag == undefined || answerContentNotifiedFlag == null || answerContentNotifiedFlag == false){
                                            unPermissionCount ++;
                                            unPermissionErrors.answers.push(ans.answerId);
                                        }else{
                                            ans.answerPermissionFlag = true;
                                        }
                                    }else{
                                        if(this.isInputed(ans.businessPassStatus)){
                                            if(ans.governmentConfirmPermissionFlag== false ){
                                                // 行政確定登録通知未許可の場合
                                                unGovernmentConfirmPermissionCount ++;
                                                unGovernmentConfirmPermissionErrors.answers.push(ans.answerId);
                                            }
                                        }
                                    }

                                    // 重複通知しないようため、回答内容変更有、回答内容と行政確定登録が通知完了していない（通知フラグ(0=未通知) または　行政確定通知フラグ(0=未通知)のみ）を通知リストに追加
                                    if((ans.notifiedFlag == false || ans.governmentConfirmNotifiedFlag == false) && ans.answerUpdateFlag == true){
                                        checkedAnswers.push(ans);
                                    }
                                }
                            }else{
                                // 画面で削除したレコードは「3：統括部署管理者に回答許可通知」または「0:事業者に回答通知」の場合、通知リスト追加
                                if(notifyType == "0" || notifyType == "3"){
                                    checkedAnswers.push(ans);
                                }
                            }
                        }
                    });
                } );
            }

            // 1件数でも選択しない場合、
            if(checkedAnswers.length == 0 && checkedDepartmentAnswers.length == 0){
                alert("未通知の回答通知内容を選択してください。");
                return ;
            }
            
            // 事前協議の場合、回答通知後、変更できないため、通知する前に、未回答のレコードが選択されるかチェックを行う。
            if(unAnsewerCount > 0 ){
                alert("未回答の条項が選択されています。");
                this.setState({ errors: unAnsewerErrors});
                return ;
            }

            // 事前協議の場合、回答通知未許可であるかチェックを行う
            if(unPermissionCount > 0){
                alert("回答通知未許可の条項が選択されています。");
                this.setState({ errors: unPermissionErrors});
                return ;
            }

             // 事前協議の場合、行政確定通知未許可であるかチェックを行う
             if(unGovernmentConfirmPermissionCount > 0){
                alert("行政確定通知未許可の条項が選択されています。");
                this.setState({ errors: unGovernmentConfirmPermissionErrors});
                return ;
            }
            
            // 事前協議の場合、行政確定許可通知を行う時、行政確定登録未入力であるかチェックを行う
            if(unGovernmentConfirmDeptCount > 0){
                alert("行政確定登録通知を行うためには部署全体の行政確定登録を行ってください。");
                this.setState({ errors: unGovernmentConfirmDeptErrors});
                return ;
            }

        }

        // 回答通知APIを行う
        document.getElementById("customloader_main").style.display = "block";

        fetch(Config.config.apiUrl + "/answer/notification", {
            method: 'POST',
            body: JSON.stringify({
                applicationId: this.props.viewState.applicationInformationSearchForApplicationId,
                applicationStepId: checkedApplicationStepId,
                answers: checkedAnswers,
                departmentAnswers: checkedDepartmentAnswers,
                notifyType: notifyType,
                acceptCommentText: acceptCommentText,
                updateDatetime: this.state.updateDatetime
            }),
            headers: new Headers({ 'Content-type': 'application/json' }),
        })
        .then(res => {
            if (res.status === 200) {
                // 回答通知完了画面に表示するメッセージが申請種別より切り替えるため、ラベル取得APIの検索条件に「申請種別＝選択した申請種別」を追加
                fetch(Config.config.apiUrl + "/label/1003/" + checkedApplicationStepId)
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
                        if (Object.keys(res).length > 0) {
                            // 0:事業者に回答通知
                            if((notifyType == "0" && checkedApplicationStepId == 2 ) || checkedApplicationStepId == 1 || checkedApplicationStepId == 3 ){
                                this.props.viewState.setCustomMessage(res[0]?.labels?.title, res[0]?.labels?.content);
                            }

                            // 1：事業者に差戻
                            if(notifyType == "1" && checkedApplicationStepId == 2){
                                this.props.viewState.setCustomMessage(res[0]?.labels?.title, res[0]?.labels?.remandedContent);
                            }

                            //2：担当課に受付通知
                            if(notifyType == "2" && checkedApplicationStepId == 2){
                                this.props.viewState.setCustomMessage(res[0]?.labels?.title, res[0]?.labels?.acceptedContent);
                            }

                            // 3：統括部署管理者に回答許可通知
                            if(notifyType == "3" && checkedApplicationStepId == 2){
                                this.props.viewState.setCustomMessage(res[0]?.labels?.title, res[0]?.labels?.answerPermissionContent);
                            }

                            // 4：統括部署管理者に行政確定登録許可通知)
                            if(notifyType == "4" && checkedApplicationStepId == 2){
                                this.props.viewState.setCustomMessage(res[0]?.labels?.title, res[0]?.labels?.governmentConfirmPermissionContent);
                            }

                            // モダール閉じる
                            this.close();
                            
                            // 回答通知完了を表示する
                            this.props.viewState.nextAnswerNotificationView();
                        } else {
                            alert("labelの取得に失敗しました。");
                        }
                    }).catch(error => {
                        console.error('通信処理に失敗しました', error);
                        alert('通信処理に失敗しました');
                    });
            } else if (res.status === 401){
                // 401認証エラーの場合の処理を追加
                alert('認証情報が無効です。ページの再読み込みを行います。');
                window.location.href = "./login/";
            } else if (res.status === 409) {
                alert('回答未登録のため通知できません。回答を登録または更新してから通知を行ってください。');
            } else {
                alert('回答通知に失敗しました');   
            }
        }).catch(error => {
            console.error('処理に失敗しました', error);
            alert('処理に失敗しました');
        }).finally(() => document.getElementById("customloader_main").style.display = "none");
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
     * 申請種別の選択結果を切り替え
     * @param {*} event 
     */
    onChange(event){
        let value = event.target.value;
        let list = this.state.applicationSteps;

        Object.keys(list).map(key => {

            if(list[key].applicationStepId == value){
                list[key].checked = true;
            }else{
                list[key].checked = false;
            }

        });
        
        this.setApplyAnswerInfoToState(null, null, value, true, null);
    }

    /**
     * 未通知のみ表示の設定変更
     * @param {*} e 
     */
    changeDisplayAllAnswer(e){

        let checked = e.target.checked;
        this.setApplyAnswerInfoToState(null, !checked, null, false, null);
    }


    /**
     * 回答レコードを表示するか
     * @param {*} answer 
     * @param {*} isGroup 
     * @returns 
     */
    isDisplayed(answer, isGroup, notifyType ){
        //申請段階
        let checkedApplicationStepId = this.state.checkedApplicationStepId;
        // 事前相談、許可判定
        if(checkedApplicationStepId == 1 || checkedApplicationStepId == 3){
            if(answer.notifiedFlag == undefined || answer.notifiedFlag == null || answer.notifiedFlag == false){
                // 通知フラグが、通知済みではない場合、表示リストに追加
                return true;
            }else{
                // 通知フラグが、通知済み、かつ、回答内容が変更された場合、表示リストに追加
                if(answer.answerUpdateFlag !== undefined && answer.answerUpdateFlag !== null && answer.answerUpdateFlag == true){
                    return true;
                }
            }
        }

        //事前協議
        if(checkedApplicationStepId == 2){

            if(isGroup){
                let count = 0;
                // 部署回答にしたに、未通知の条項があるかを判定
                answer.answers.forEach(item => {
                    // 0:事業者に回答通知
                    if(notifyType == "0"){
                        if(item.notifiedFlag == undefined || item.notifiedFlag == null || item.notifiedFlag ==false){
                            count ++;
                        }else{
                            // 回答内容通知済み後、回答変更あり
                            if(item.answerUpdateFlag !== undefined && item.answerUpdateFlag !== null && item.answerUpdateFlag ==true){
                                count ++;
                            }
                        }
                    }

                    // 3：統括部署管理者に回答許可通知
                    if(notifyType == "3"){
                        if(item.answerPermissionFlag == undefined || item.answerPermissionFlag == null || item.answerPermissionFlag ==false){
                            count ++;
                        }
                    }

                    // 4：統括部署管理者に行政確定登録許可通知)
                    if(notifyType == "4"){
                        if(item.governmentConfirmPermissionFlag == undefined || item.governmentConfirmPermissionFlag == null || item.governmentConfirmPermissionFlag ==false){
                            count ++;
                        }
                    }
                    
                });

                if(count > 0){
                    return true;
                }else{
                    // 0:事業者に回答通知
                    if(notifyType == "0"){
                        if((answer.notifiedFlag == undefined || answer.notifiedFlag == null || answer.notifiedFlag ==false) && answer.governmentConfirmPermissionFlag ==true){
                            return true;
                        }else{
                            return false;
                        }
                    }

                    // 3：統括部署管理者に回答許可通知
                    if(notifyType == "3"){
                        return false;
                    }

                    // 4：統括部署管理者に行政確定登録許可通知)
                    if(notifyType == "4"){
                        if(answer.governmentConfirmPermissionFlag == undefined || answer.governmentConfirmPermissionFlag == null || answer.governmentConfirmPermissionFlag ==false){
                            return true;
                        }else{
                            return false;
                        }
                    }
                }
            }else{
                // 0:事業者に回答通知
                if(notifyType == "0"){
                    // 通知フラグが、通知済みではない場合、表示リストに追加
                    if(answer.notifiedFlag == undefined || answer.notifiedFlag == null || answer.notifiedFlag == false){
                        // 通知フラグが、通知済みではない場合、表示リストに追加
                        return true;
                    }else{
                        // 回答変更あり場合、表示リストに追加
                        if(answer.answerUpdateFlag !== undefined && answer.answerUpdateFlag !== null && answer.answerUpdateFlag ==true){
                            return true;
                        }else{
                            return false;
                        }
                    }
                }

                // 3：統括部署管理者に回答許可通知
                if(notifyType == "3"){
                    if(answer.answerPermissionFlag == undefined || answer.answerPermissionFlag == null || answer.answerPermissionFlag ==false){
                        return true;
                    }else{
                        return false;
                    }
                }

                // 4：統括部署管理者に行政確定登録許可通知)
                if(notifyType == "4"){
                    if(answer.governmentConfirmPermissionFlag == undefined || answer.governmentConfirmPermissionFlag == null || answer.governmentConfirmPermissionFlag ==false){
                        return true;
                    }else{
                        return false;
                    }
                }
            }
        }
        return false;
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

    /**
     * 通知可能の条項であるか判断
     * 通知不可の場合、チェックボックスが非表示にする
     * @param {*} notificable 事業者へ通知可能フラグ
     * @param {*} permissionNotificable 回答許可通知可能フラグ
     * @param {*} notifyType 通知種類
     * @returns 
     */
    notificable(notificable, permissionNotificable, notifyType){

        // 0:事業者に回答通知
        if(notifyType == "0"){
            return notificable;
        }

        // 3：統括部署管理者に回答許可通知
        if(notifyType == "3"){
            return permissionNotificable;
        }

        //4：統括部署管理者に行政確定登録許可通知)
        if(notifyType == "4"){
            return permissionNotificable;
        }
    }

    /**
     * 事業者へ通知を行われるか判定
     * 回答一覧に事業者へ通知可能なレコードがある場合、
     * @returns 
     */
    notifyToBusiness(){
        const departmentAnswers = this.state.departmentAnswers;
        const notifyType = "0";
        // 事業者へ通知可能の回答の件数
        let notificableAnswerCount = 0;

        Object.keys(departmentAnswers).map(key1 => {
            if(departmentAnswers[key1]["notificable"] === true){
                notificableAnswerCount ++;
            }
            Object.keys(departmentAnswers[key1].answers).map(key2 => {
                if(departmentAnswers[key1].answers[key2]["notificable"] === true){
                    notificableAnswerCount ++;
                }
            });
        });

        return notificableAnswerCount > 0 ? true : false;
    }

    render(){
        let applicationSteps = this.state.applicationSteps;
        let checkedApplicationStepId = this.state.checkedApplicationStepId;

        const displayAllAnswer = this.state.displayAllAnswer;
        const answers = this.state.answers;
        const departmentAnswers = this.state.departmentAnswers;

        const ledgerMaster = this.state.ledgerMaster;
        const ledgerMasterCount = Object.keys(ledgerMaster).length;
        const errorsAnswerIds = this.state.errors.answers;
        const errorsDepartmentAnswerIds = this.state.errors.departmentAnswers;

        const controlDepartmentAdmin = this.state.controlDepartmentAdmin;
        const notifyType = this.state.notifyType;
        const acceptingFlag = this.state.acceptingFlag;
        const acceptVersionInformation = this.state.acceptVersionInformation;
        const acceptCommentText = this.state.acceptCommentText;
        const maxLength = Config.inputMaxLength.notifyComment;
        return (
            <>
                <div className={CustomStyle.overlay}>
                    <div className={CustomStyle.modal}  id="confirmAnswerNotificationModal">
                        <Box position="absolute" paddedRatio={3} topRight>
                            <RawButton onClick={() => {
                                this.props.viewState.setCallBackFunction(Function = ()=>{});
                                this.close();
                            }}>
                                <StyledIcon
                                    styledWidth={"16px"}
                                    fillColor={"#000"}
                                    opacity={"0.5"}
                                    glyph={Icon.GLYPHS.closeLight}
                                    css={`
                                        cursor:pointer;
                                    `}
                                />
                            </RawButton>
                        </Box>
                        <nav className={CustomStyle.custom_nuv} id="confirmAnswerNotificationModalDrag">
                            回答通知確認
                        </nav>
                        <div className={CustomStyle.container}>

                            { checkedApplicationStepId == 2 && (
                                <>
                                    {/* 統括部署管理者　かつ　受付フラグ＝　未確認 */}
                                    {controlDepartmentAdmin && acceptingFlag=="0" && (
                                        <>
                                            <p>回答通知先を選択してください</p>
                                            <div className={CustomStyle.radioDiv}>
                                                <p style={{fontWeight: "bold"}}>申請受付</p>
                                                <label className={CustomStyle.radio_label}>
                                                    <input
                                                        className={CustomStyle.radio_input}
                                                        type="radio"
                                                        value={"2"}
                                                        onChange={e => {
                                                            this.setState({notifyType: "2"});
                                                        }}
                                                        checked={notifyType == "2"}
                                                    />
                                                    <span className={CustomStyle.custom_radio} />
                                                    {'申請受付通知（各担当課に通知します）'}
                                                </label>
                                                <label className={CustomStyle.radio_label}>
                                                    <input
                                                        className={CustomStyle.radio_input}
                                                        type="radio"
                                                        value={"1"}
                                                        onChange={e => {
                                                            this.setState({notifyType: "1"});
                                                        }}
                                                        checked={notifyType == "1"}
                                                    />
                                                    <span className={CustomStyle.custom_radio} />
                                                    {'申請差戻通知（事業者に差戻します）'}
                                                </label>
                                            </div>
                                        </>
                                    )}
                                            <p style={{fontWeight: "bold"}}>回答通知</p>
                                            <div className={CustomStyle.radioDiv}>
                                                <label className={CustomStyle.radio_label}>
                                                    <input
                                                        className={CustomStyle.radio_input}
                                                        type="radio"
                                                        value={"3"}
                                                        onChange={e => {
                                                            this.setState({notifyType: "3", errors: {answers:[],departmentAnswers:[]}});
                                                            this.setApplyAnswerInfoToState(null, null, null, false, "3");
                                                        }}
                                                        checked={notifyType == "3"}
                                                    />
                                                    <span className={CustomStyle.custom_radio} />
                                                    {'回答許可通知（統括部署管理者に回答許可通知します）'}
                                                </label>
                                                <label className={CustomStyle.radio_label}>
                                                    <input
                                                        className={CustomStyle.radio_input}
                                                        type="radio"
                                                        value={"4"}
                                                        onChange={e => {
                                                            this.setState({notifyType: "4", errors: {answers:[],departmentAnswers:[]}});
                                                            this.setApplyAnswerInfoToState(null, null, null, false, "4");
                                                        }}
                                                        checked={notifyType == "4"}
                                                    />
                                                    <span className={CustomStyle.custom_radio} />
                                                    {'行政確定登録許可通知（統括部署管理者に行政確定登録回答通知します）'}
                                                </label>
                                                {this.notifyToBusiness() && (  
                                                    <label className={CustomStyle.radio_label}>
                                                        <input
                                                            className={CustomStyle.radio_input}
                                                            type="radio"
                                                            value={"0"}
                                                            onChange={e => {
                                                                this.setState({notifyType: "0", errors: {answers:[],departmentAnswers:[]}});
                                                                this.setApplyAnswerInfoToState(null, null, null, false, "0");
                                                            }}
                                                            checked={notifyType == "0"}
                                                        />
                                                        <span className={CustomStyle.custom_radio} />
                                                        {'回答通知（事業者に回答通知します）'}
                                                    </label>
                                                )}
                                            </div>
                                        </>
                                    )}

                            { notifyType !== "1" && notifyType !== "2" && (
                                <>
                                    <div className={CustomStyle.label_checkbox}>
                                        <p>回答通知内容を確認してください。</p>
                                        <div className={CustomStyle.custom_checkbox_div}>
                                            <input type="checkbox" className={CustomStyle.custom_checkbox}
                                                onChange={ evt => { this.changeDisplayAllAnswer(evt)} }
                                                checked={!displayAllAnswer}
                                            />
                                            {`未通知のみ表示`}
                                        </div>
                                    </div>
                                    { checkedApplicationStepId == 1 && (
                                        <div className={CustomStyle.scroll_container} id="AnswerContentList1Table">
                                            <table className={CustomStyle.selection_table}>
                                                <thead>
                                                    <tr className={CustomStyle.table_header}>
                                                        <th style={{ width: "5%"}} className="no-sort" data-sort-method='none'>
                                                            <input type="checkbox" className={CustomStyle.custom_checkbox_auto}
                                                                onChange={ e => { this.changeSelectedAllAnswer(e) } }
                                                                checked={this.state.selectedAll}
                                                            ></input>
                                                        </th>
                                                        <th style={{ width: "15%"}}>対象</th>
                                                        <th style={{ width: "14%"}}>回答課</th>
                                                        <th style={{ width: "20%"}}>判定結果</th>
                                                        <th style={{ width: "20%"}}>回答内容</th>
                                                        <th style={{ width: "8%"}}>添付有無</th>
                                                        <th style={{ width: "8%"}}>再申請要否</th>
                                                        <th style={{ width: "10%"}}>事前協議要否</th>
                                                    </tr>
                                                </thead>
                                                <tbody>
                                                    {answers && Object.keys(answers).map(key => (
                                                        <tr key={"tr1" + key } 
                                                            style={{display: answers[key].displayFlag? "":"none"}}
                                                            className={errorsAnswerIds.some(id => id == answers[key].answerId) ? CustomStyle.highlight :""}
                                                            onClick={ e => { this.changeChecked(!answers[key].checked, key, null) }}
                                                        >
                                                            <td style={{textAlign: "center"}}>
                                                                {answers[key].notificable && (
                                                                    <input type="checkbox" className={CustomStyle.custom_checkbox_auto}
                                                                        onChange={ e => { this.changeChecked(e.target.checked, key, null) } }
                                                                        checked={answers[key].checked}
                                                                    ></input>
                                                                )}
                                                            </td>
                                                            <td>{answers[key]["judgementInformation"]["title"]}</td>
                                                            <td>{this.getDepartmentName(answers[key])} </td>
                                                            <td>{answers[key]["judgementResult"]}</td>
                                                            <td>{answers[key]["answerContent"]}</td>
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
                                                            <td>
                                                                {answers[key]["reApplicationFlag"] == true && (
                                                                    <span>{`必要`}</span>
                                                                )}
                                                                {answers[key]["reApplicationFlag"] == false && (
                                                                    <span>{`不要`}</span>
                                                                )}
                                                                <span>{``}</span>
                                                            </td>
                                                            <td>
                                                                {answers[key]["discussionFlag"] == true && (
                                                                    <span>{`必要`}</span>
                                                                )}
                                                                {answers[key]["discussionFlag"] == false && (
                                                                    <span>{`不要`}</span>
                                                                )}
                                                                <span>{``}</span>
                                                            </td>
                                                        </tr>
                                                    ))}
                                                </tbody>
                                            </table>
                                        </div>
                                    )}
                                    { checkedApplicationStepId == 2 && (
                                        <div className={CustomStyle.scroll_container_2} id="AnswerContentList2Table">
                                            <table className={CustomStyle.selection_table + " no-sort"}>
                                                <thead>
                                                    <tr className={CustomStyle.table_header}>
                                                        <th style={{width:"5%"}}></th>
                                                        <th style={{width:"25%"}}>関連条項</th>
                                                        <th style={{width:"30%"}} colSpan={ledgerMasterCount + 1}>協議対象/行政回答</th>
                                                        <th style={{width:"20%"}} colSpan={2}>事業者合意登録</th>
                                                        <th style={{width:"20%"}} colSpan={2}>行政確定登録</th>
                                                    </tr>
                                                </thead>
                                                <tbody>
                                                    {departmentAnswers && Object.keys(departmentAnswers).map(key1 => (
                                                        <>
                                                            <tr key={"tr2" + key1 }  
                                                                className={`${CustomStyle.title_row} ${errorsDepartmentAnswerIds.some(id => id == departmentAnswers[key1].departmentAnswerId) ? CustomStyle.highlight :""}`} 
                                                                style={{display: departmentAnswers[key1].displayFlag ? "":"none"}}
                                                                onClick={ e => { this.changeChecked(!departmentAnswers[key1].checked, key1, null) }}
                                                            >
                                                                <td style={{textAlign: "center"}}>
                                                                    { this.notificable(departmentAnswers[key1].notificable, departmentAnswers[key1].permissionNotificable, notifyType) && (

                                                                        <input type="checkbox" className={CustomStyle.custom_checkbox}
                                                                            onChange={ e => { this.changeChecked(e.target.checked, key1, null) } }
                                                                            checked={departmentAnswers[key1].checked}
                                                                        ></input>
                                                                    )}
                                                                </td>
                                                                {/*添付ファイルのアイコン */}
                                                                <td  colSpan={ledgerMasterCount + 4}>
                                                                    <div className={CustomStyle.flex_center}>
                                                                        {departmentAnswers[key1].department?.departmentName}
                                                                        {/* 行政確定通知許可アイコン */}
                                                                        {departmentAnswers[key1]["governmentConfirmPermissionFlag"] == true && (
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
                                                                <td style={{width:"10%", minWidth:"100px"}}>
                                                                    <>
                                                                        {departmentAnswers[key1].governmentConfirmStatus == "0" && (
                                                                            <span>{`合意`}</span>
                                                                        )}
                                                                        {departmentAnswers[key1].governmentConfirmStatus == "1" && (
                                                                            <span>{`取下`}</span>
                                                                        )}
                                                                        {departmentAnswers[key1].governmentConfirmStatus == "2" && (
                                                                            <span>{`却下`}</span>
                                                                        )}
                                                                        <span>{``}</span>
                                                                    </>
                                                                </td>
                                                                <td style={{width:"10%", minWidth:"100px"}}>{departmentAnswers[key1]["governmentConfirmDatetime"]?departmentAnswers[key1]["governmentConfirmDatetime"]:""}</td>
                                                            </tr>
                                                            {Object.keys(departmentAnswers[key1].answers).map(key2 => (
                                                                <tr 
                                                                    key={"tr2" + key1 + "-" + key2} 
                                                                    style={{display: departmentAnswers[key1].answers[key2].displayFlag? "":"none"}}
                                                                    className={`${errorsAnswerIds.some(id => id == departmentAnswers[key1].answers[key2].answerId) ? CustomStyle.highlight :""}
                                                                                ${departmentAnswers[key1].answers[key2].answerDataType == "7"? CustomStyle.deleted_line:""}
                                                                                ${departmentAnswers[key1].answers[key2].permissionJudgementMigrationFlag == true ? CustomStyle.is_notCheckItem:""}
                                                                        `}
                                                                    onClick={ e => { this.changeChecked(!departmentAnswers[key1].answers[key2].checked, key1, key2) }}
                                                                >
                                                                    <td style={{textAlign: "center"}}>
                                                                        {this.notificable(departmentAnswers[key1].answers[key2].notificable, departmentAnswers[key1].answers[key2].permissionNotificable, notifyType)  && (

                                                                            <input type="checkbox" className={CustomStyle.custom_checkbox}
                                                                                onChange={ e => { this.changeChecked(e.target.checked, key1, key2) } }
                                                                                checked={departmentAnswers[key1].answers[key2].checked}
                                                                            ></input>
                                                                        )}
                                                                    </td>
                                                                    <td>{departmentAnswers[key1].answers[key2]["judgementInformation"]["title"]}</td>
                                                                    {departmentAnswers[key1].answers[key2].discussionItems && Object.keys(departmentAnswers[key1].answers[key2].discussionItems).map(i => (
                                                                        <td style={{width:"5%", minWidth:"60px"}}>
                                                                            {departmentAnswers[key1].answers[key2].discussionItems[i].checked? departmentAnswers[key1].answers[key2].discussionItems[i].displayName:""}
                                                                        </td>
                                                                    ))}
                                                                    <td>
                                                                        <div className={CustomStyle.flex_center} style={{justifyContent:"space-between"}}>
                                                                            <span>{departmentAnswers[key1].answers[key2].answerContent}</span>
                                                                            {/* 回答通知許可アイコン */}
                                                                            {departmentAnswers[key1].answers[key2]["answerPermissionFlag"] == true && (
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
                                                                    <td style={{width:"10%", minWidth:"100px"}}>
                                                                        {departmentAnswers[key1].answers[key2].businessPassStatus == "0" && (
                                                                            <span>{`否決`}</span>
                                                                        )}
                                                                        {departmentAnswers[key1].answers[key2].businessPassStatus == "1" && (
                                                                            <span>{`合意`}</span>
                                                                        )}
                                                                        <span>{``}</span>
                                                                    </td>
                                                                    <td style={{width:"10%", minWidth:"100px"}}>{departmentAnswers[key1].answers[key2].businessAnswerDatetime}</td>
                                                                    <td>
                                                                        <div className={CustomStyle.flex_center} style={{justifyContent:"space-between"}}>
                                                                            {departmentAnswers[key1].answers[key2].governmentConfirmStatus == "0" && (
                                                                                <span>{`合意`}</span>
                                                                            )}
                                                                            {departmentAnswers[key1].answers[key2].governmentConfirmStatus == "1" && (
                                                                                <span>{`取下`}</span>
                                                                            )}
                                                                            {departmentAnswers[key1].answers[key2].governmentConfirmStatus == "2" && (
                                                                                <span>{`却下`}</span>
                                                                            )}
                                                                            <span>{``}</span>
                                                                            {departmentAnswers[key1].answers[key2]["governmentConfirmPermissionFlag"] == true && (
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
                                                                    <td>{departmentAnswers[key1].answers[key2].governmentConfirmDatetime}</td>
                                                                </tr>
                                                            ))}
                                                        </>
                                                    ))}
                                                </tbody>
                                            </table>
                                        </div>
                                    )}
                                    { checkedApplicationStepId == 3 && (
                                        <div className={CustomStyle.scroll_container} id="AnswerContentList3Table">
                                            <table className={CustomStyle.selection_table}>
                                                <thead>
                                                    <tr className={CustomStyle.table_header}>
                                                        <th style={{width:"5%"}} className="no-sort" data-sort-method='none'>
                                                            <input type="checkbox" className={CustomStyle.custom_checkbox_auto}
                                                                onChange={ e => { this.changeSelectedAllAnswer(e) } }
                                                                checked={this.state.selectedAll}
                                                            ></input>
                                                        </th>
                                                        <th style={{width:"30%"}}>対象</th>
                                                        <th style={{width:"10%"}}>判定結果</th>
                                                        <th style={{width:"35%"}}>回答内容</th>
                                                        <th style={{width:"10%"}}>再申請要否</th>
                                                    </tr>
                                                </thead>
                                                <tbody>
                                                    {answers && Object.keys(answers).map(key => (
                                                        <tr key={"tr3" + key } 
                                                            style={{display: answers[key].displayFlag? "":"none"}}
                                                            className={errorsAnswerIds.some(id => id == answers[key].answerId) ? CustomStyle.highlight :""}
                                                            onClick={ e => { this.changeChecked(!answers[key].checked, key, null) }}
                                                        >
                                                            <td style={{textAlign: "center"}}>
                                                                {answers[key].notificable && (
                                                                    <input type="checkbox" className={CustomStyle.custom_checkbox_auto}
                                                                        onChange={ e => { this.changeChecked(e.target.checked, key, null) } }
                                                                        checked={answers[key].checked}
                                                                    ></input>
                                                                )}
                                                            </td>
                                                            <td>{answers[key]["judgementInformation"]["title"]}</td>
                                                            <td>
                                                                {answers[key]?.permissionJudgementResult == "0" && (
                                                                    <span>{`問題なし`}</span>
                                                                )}
                                                                {answers[key]?.permissionJudgementResult == "1" && (
                                                                    <span>{`問題あり`}</span>
                                                                )}
                                                                {!answers[key]?.permissionJudgementResult && (
                                                                    <span>{``}</span>
                                                                )}
                                                                <span>{``}</span>
                                                            </td>
                                                            <td>{answers[key]["answerContent"]}</td>
                                                            <td>
                                                                {answers[key]["reApplicationFlag"] == true && (
                                                                    <span>{`必要`}</span>
                                                                )}
                                                                {answers[key]["reApplicationFlag"] == false && (
                                                                    <span>{`不要`}</span>
                                                                )}
                                                                <span>{``}</span>
                                                            </td>
                                                        </tr>
                                                    ))}
                                                </tbody>
                                            </table>
                                        </div>
                                    )}
                                </>
                            )}
                            { (notifyType == "1" || notifyType == "2") && (
                                <>
                                    <p>コメントを入力してください。</p>
                                    <textarea 
                                        type="text" 
                                        rows={8}
                                        maxLength={maxLength + 1}
                                        value={acceptCommentText}
                                        id="acceptCommentText"
                                        onChange={e => {
                                            // 文字数チェック
                                            if(e.target.value.length > maxLength ){
                                                alert(maxLength+"文字以内で入力してください。");
                                                return;
                                            }
                                            this.setState({acceptCommentText: e.target.value});
                                        }}
                                        className={CustomStyle.accept_comment_text_area}
                                    />
                                </>
                            )}
                        </div>
                        <div className={CustomStyle.button_div}>
                            <button
                                className={CustomStyle.btn_baise_style}
                                style={{width:"30%",height:"40px"}}
                                onClick={e => {this.moveToAnswerNotificationView()}}
                            >
                                <span>回答通知</span>
                            </button>
                        </div>
                    </div>
                </div>
            </>
        )
    }



}
export default withTranslation()(withTheme(ConfirmAnswerNotificationModal));