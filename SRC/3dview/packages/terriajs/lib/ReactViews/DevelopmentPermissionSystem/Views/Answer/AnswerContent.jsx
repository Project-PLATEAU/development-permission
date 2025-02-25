import { observer } from "mobx-react";
import PropTypes from "prop-types";
import React from "react";
import { withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import Box from "../../../../Styled/Box";
import CustomStyle from "./scss/answer-content.scss";
import PageStyle from "../../PageViews/scss/pageStyle.scss";
import AnswerContentList from "./AnswerContentList";
import NegotiationContentList from "./NegotiationContentList";
import AssessmentContentList from "./AssessmentContentList"
import Config from "../../../../../customconfig.json";
import AnswerFileList from "./AnswerFileList";
import ApplicationFileList from "./ApplicationFileList";
import NotificationFileList from "./NotificationFileList";
import ApplicantInformation from "./ApplicantInformation"
import Spacing from "../../../../Styled/Spacing";
import Icon, { StyledIcon } from "../../../../Styled/Icon";
import FinalDocumentList from "../../../AdminPermissionSystem/Views/Apply/FinalDocumentList";

/**
 * 回答内容確認画面の申請・回答確認コンポーネント
 */
@observer
class AnswerContent extends React.Component {
    static displayName = "AnswerContent";
    static propTypes = {
        terria: PropTypes.object.isRequired,
        viewState: PropTypes.object.isRequired,
        theme: PropTypes.object,
        t: PropTypes.func.isRequired,
        focusMapPlaceDriver:PropTypes.func
    }
    constructor(props) {
        super(props);
        this.state = {
            viewState: props.viewState,
            terria: props.terria,
            //申請者情報
            applicantInformations: props.viewState.answerContent.applicantInformations,
            //申請区分
            checkedApplicationCategory: [],
            //申請ファイル
            applicationFiles: [],
            //回答ファイル
            answerFile: [],
            //申請地番
            lotNumbers: props.viewState.answerContent.lotNumbers,
            //申請状態
            status: props.viewState.answerContent.status,
            statusCode: props.viewState.answerContent.statusCode,
            //行政からの回答一覧
            answers: [],
            //部署回答一覧
            departmentAnswers:[],
            //申請ID
            applicationId: props.viewState.answerContent.applicationId,
            //申請回答DTO
            ApplyAnswerForm: {},
            //回答履歴
            answerHistory: [],
            //1=回答ファイル一覧,2=申請ファイル一覧,3=通知ファイル一覧
            activeFileListType:1,
            //1=事前相談,2=事前協議,3=許可判定
            checkedApplicationStepId: props.viewState.checkedApplicationStepId,
            //選択された回答
            selectedAnswer: null,
            //選択された回答に紐づく回答ファイル
            selectedAnswerFiles:[],
            //選択された回答履歴
            selectedAnswerHistory: [],
            //申請種類
            checkedApplicationType: {},
            //申請追加情報
            applicantAddInformations: [],
            //通知ファイル一覧
            notificationFiles:[],
            //協議対象一覧
            ledgerMaster:[],
            intervalID:null,
            // 受付フラグ：1=受付 0=未確認 2=差戻
            acceptingFlag: null,
            // 全ての回答に事業者が回答完了しているかフラグ
            businessAnswerCompleted: false,
            // 事前協議の申請（受付版＝0）が確認中であるフラグ
            firstAccepting: false
            
        };
        this.mapBaseElement = React.createRef();
        this.mpBaseContainerElement =React.createRef();
    }

    /**
     * 初期処理
     */
    componentDidMount(){
        // 申請回答確認情報取得
        this.searchAnswerInfo(true);
        this.getLedgerMasterList();
        this.props.viewState.setRefreshConfirmApplicationDetails(this.searchAnswerInfo);
        let intervalID = setInterval(() => {
            if(this.props.viewState.showConfirmAnswerInformationView){
                this.searchAnswerInfo(false);
            }else{
             return;
            }
             
         }, 30000);
         this.setState({intervalID:intervalID});
    }

    /**
     * intervalのクリア処理
     */
    componentWillUnmount() {
        let intervalID = this.state.intervalID;
        if(intervalID){
            clearInterval(intervalID);
        }
      }

     /**
     * 再申請情報取得
     */
     executeForReApply(){

        // 申請回答情報を取得する
        let answerContent = this.props.viewState.answerContent;
        let id = answerContent["loginId"];
        let password = answerContent["password"];
        // 処理中の申請段階ID
        let reapplyApplicationStepId = this.props.viewState.reapplyApplicationStepId;
        //　前回の申請段階ID
        let preReapplyApplicationStepId = this.props.viewState.preReapplyApplicationStepId;

        document.getElementById("customloader_main").style.display = "block";
        fetch(Config.config.apiUrl + "/application/reappInformation", {
            method: 'POST',
            body: JSON.stringify({
                loginId:id,
                password: password,
                applicationId: this.state.applicationId ,
                applicationStepId: reapplyApplicationStepId,
                preApplicationStepId:preReapplyApplicationStepId
            }),
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
                res.loginId = id;
                res.password = password;
                
                // 事前相談、事前協議の場合、
                if(reapplyApplicationStepId == 1 || reapplyApplicationStepId == 2){
                    // 再申請用の情報を保存
                    this.props.viewState.setReAppInformation(JSON.parse(JSON.stringify(res)));
                    // 申請区分選択画面へ遷移
                    this.props.viewState.moveToInputApplyConditionViewForReapply();
                }
                
                // 許可判定の場合、
                if(reapplyApplicationStepId == 3){
                    if(document.getElementById("customloader_main")){
                        if(document.getElementById("customloader_main").style.display == "none"){
                            document.getElementById("customloader_main").style.display = "block";
                        }
                    }
                    fetch(Config.config.apiUrl + "/judgement/execute", {
                        method: 'POST',
                        body: JSON.stringify({
                            applyLotNumbers: res.lotNumbers,
                            applicationId: this.state.applicationId ,
                            applicationTypeId: res.applicationTypeId,
                            applicationStepId: reapplyApplicationStepId,
                            preApplicationStepId: preReapplyApplicationStepId,
                            applicationCategories: res.applicationCategories
                        }),
                        headers: new Headers({ 'Content-type': 'application/json' }),
                    })
                    .then(judgementRes => judgementRes.json())
                    .then(judgementRes => {
                        if(judgementRes.status === 401){
                            alert("認証情報が無効です。ページの再読み込みを行います。");
                            window.location.reload();
                            return null;
                        }
                        if (Object.keys(judgementRes).length > 0 && !judgementRes.status) {

                            res.generalConditionDiagnosisResultForm = judgementRes;
                            // 再申請用の情報を保存
                            this.props.viewState.setReAppInformation(JSON.parse(JSON.stringify(res)));
                            // 申請追加情報画面へ遷移
                            this.props.viewState.moveToEnterAddInformation();
                       
                        } else {
                            this.setState({ generalConditionDiagnosisResult: [], disabledFlg: true });
                            alert("再申請情報に概要診断結果取得に失敗しました。");
                        }
                    }).catch(error => {
                        console.error('処理に失敗しました', error);
                        alert('処理に失敗しました');
                    }).finally(() => document.getElementById("customloader_main").style.display == "none");
                }
            }else{
                alert('再申請情報取得処理に失敗しました');
            }
        }).catch(error => {
            console.error('通信処理に失敗しました', error);
            alert('通信処理に失敗しました');
        }).finally(() => document.getElementById("customloader_main").style.display = "none");
    }

    /**
     * 申請回答確認情報取得
     * @param {boolean} initFlag
     */
    searchAnswerInfo = (initFlag=false) => {
        let answerContent = this.props.viewState.answerContent;
        let id = answerContent["loginId"];
        let password = answerContent["password"];

        fetch(Config.config.apiUrl + "/answer/confirm/answer", {
            method: 'POST',
            body: JSON.stringify({
                loginId:id,
                password:password,
                outputLogFlag:false
            }),
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
                res.loginId = id;
                res.password = password;
                this.props.viewState.setAnswerContent(JSON.parse(JSON.stringify(res)));
                const checkedApplicationStepId = this.state.checkedApplicationStepId;
                this.setApplyAnswerInfoToState(res, checkedApplicationStepId, this.state.selectedAnswer, initFlag);
                
            }else{
                alert('申請・回答内容確認情報取得処理に失敗しました');
            }
        }).catch(error => {
            console.error('通信処理に失敗しました', error);
            alert('通信処理に失敗しました');
        });
    }

    /**
     * 申請・回答内容確認情報フォーム変更処理
     * @param {*} res 
     */
    changeAnswerInfo = (res) => {
        let id = this.props.viewState.answerContent["loginId"];
        let password = this.props.viewState.answerContent["password"];
        const answerContent = JSON.parse(JSON.stringify(res));
        answerContent["loginId"] = id;
        answerContent["password"] = password;
        this.props.viewState.setAnswerContent(answerContent);
        const checkedApplicationStepId = this.state.checkedApplicationStepId;
        this.setApplyAnswerInfoToState(res, checkedApplicationStepId, this.state.selectedAnswer, false);
    }

    /**
     * 検索結果をStateにセット
     * @param {*} checkedApplicationStepId 選択中の申請段階
     * @param {*} initFlag 初期フラグ
     * @param {*} res 申請回答情報の検索結果
    */
    setApplyAnswerInfoToState(res, checkedApplicationStepId, selectedAnswer= null, initFlag = false){
        let answer = null;

        let checkedApplicationCategory = this.state.checkedApplicationCategory;
        let applicantAddInformations = this.state.applicantAddInformations;
        let applicationFiles = this.state.applicationFiles;
        let answers = this.state.answers;
        let departmentAnswers = this.state.departmentAnswers;
        let answerHistory = this.state.answerHistory;
        let answerFile = this.state.answerFile;
        let selectedAnswerHistory = this.state.selectedAnswerHistory;
        let selectedAnswerFiles = this.state.selectedAnswerFiles;
        let notificationFiles = this.state.notificationFiles;
        let activeFileListType = this.state.activeFileListType;
        let acceptingFlag = this.state.acceptingFlag;
        let businessAnswerCompleted = this.state.businessAnswerCompleted;

        Object.keys(res.applyAnswerDetails).map(key => {
            let applyAnswerDetailForm = res.applyAnswerDetails[key];
            // 事前協議
            if(applyAnswerDetailForm.applicationStepId == 2){
                acceptingFlag = applyAnswerDetailForm.acceptingFlag;
                businessAnswerCompleted = applyAnswerDetailForm.businessAnswerCompleted;
            }

            if(applyAnswerDetailForm.applicationStepId == checkedApplicationStepId){
                checkedApplicationCategory = applyAnswerDetailForm.applicationCategories;
                applicantAddInformations = applyAnswerDetailForm.applicantAddInformations;
                applicationFiles = applyAnswerDetailForm.applicationFiles;
                answers = applyAnswerDetailForm.answers;
                departmentAnswers = applyAnswerDetailForm.departmentAnswers;
                answerHistory = applyAnswerDetailForm.answerHistorys;
                answerFile = applyAnswerDetailForm.answerFiles;
                notificationFiles = applyAnswerDetailForm.ledgerFiles;

                // 回答ファイル一覧 
                if(activeFileListType = 1){
                    
                    // 事前相談
                    if(checkedApplicationStepId == 1){

                        if(selectedAnswer){
                            // 回答一覧の行が選択された場合、該当回答に対する回答ファイル一覧を表示
                            answer = answers.find(answer=>answer.answerId === selectedAnswer?.answerId);
                            selectedAnswer = answer;
                            selectedAnswerFiles = answer?.answerFiles?answer.answerFiles:[];

                        }else{
                            // 回答一覧の行が選択されない場合、該当申請段階に対するすべての回答ファイル一覧を表示
                            selectedAnswerFiles = applyAnswerDetailForm?.answerFiles?applyAnswerDetailForm.answerFiles:[];
                        }
                    }

                    // 事前協議
                    if(checkedApplicationStepId == 2){

                        if(selectedAnswer){
                            // 回答一覧の部署ごとの行が選択された場合、該当回答に対する回答ファイル一覧を表示
                            answer = departmentAnswers.find(answer=>answer.departmentAnswerId === selectedAnswer?.departmentAnswerId);
                            selectedAnswer = answer;
                            selectedAnswerFiles = answer?.answerFiles?answer.answerFiles:[];

                        }else{
                            // 回答一覧の部署ごとの行が選択されない場合、該当申請段階に対するすべての回答ファイル一覧を表示
                            selectedAnswerFiles = applyAnswerDetailForm?.answerFiles?applyAnswerDetailForm.answerFiles:[];
                        }

                    }
                    // 許可判定
                    if(checkedApplicationStepId == 3){
                        // 申請段階に対するすべての回答ファイル一覧を表示
                        selectedAnswerFiles = applyAnswerDetailForm?.answerFiles?applyAnswerDetailForm.answerFiles:[];
                    }
                }

                // 回答履歴一覧 
                if(activeFileListType = 4){
                    
                    // 事前相談
                    if(checkedApplicationStepId == 1){

                        if(selectedAnswer){
                            // 回答一覧の行が選択された場合、該当回答に対する回答履歴一覧を表示
                            answer = answers.find(answer=>answer.answerId === selectedAnswer?.answerId);
                            selectedAnswer = answer;
                            selectedAnswerHistory = answer?.answerHistorys?answer.answerHistorys:[];

                        }else{
                            // 回答一覧の行が選択されない場合、該当申請段階に対するすべての回答履歴一覧を表示
                            selectedAnswerHistory = applyAnswerDetailForm?.answerHistorys?applyAnswerDetailForm.answerHistorys:[];
                        }
                    }

                    // 事前協議
                    if(checkedApplicationStepId == 2){

                        if(selectedAnswer){
                            if(selectedAnswer?.answerId){
                                // 回答一覧の行が選択された場合、該当回答に対する回答ファイル一覧を表示
                                answer = answers.find(answer=>answer.answerId === selectedAnswer?.answerId);
                                selectedAnswer = answer;
                                selectedAnswerHistory = answer?.answerHistorys?answer.answerHistorys:[];
                            }else{
                                // 回答一覧の部署ごとの行が選択されない場合、該当申請段階に対するすべての回答履歴一覧を表示
                                selectedAnswerHistory = applyAnswerDetailForm?.answerHistorys?applyAnswerDetailForm.answerHistorys:[];
                            }

                        }else{
                            // 回答一覧の部署ごとの行が選択されない場合、該当申請段階に対するすべての回答ファイル一覧を表示
                            selectedAnswerHistory = applyAnswerDetailForm?.answerHistorys?applyAnswerDetailForm.answerHistorys:[];
                        }

                    }
                    // 許可判定
                    if(checkedApplicationStepId == 3){
                        if(selectedAnswer){
                            // 回答一覧の行が選択された場合、該当回答に対する回答履歴一覧を表示
                            answer = answers.find(answer=>answer.answerId === selectedAnswer?.answerId);
                            selectedAnswer = answer;
                            selectedAnswerHistory = answer?.answerHistorys?answer.answerHistorys:[];

                        }else{
                            // 回答一覧の行が選択されない場合、該当申請段階に対するすべての回答履歴一覧を表示
                            selectedAnswerHistory = applyAnswerDetailForm?.answerHistorys?applyAnswerDetailForm.answerHistorys:[];
                        }
                    }
                }
            }
        })
        
        // viewStateに切り替えた申請段階を保存
        this.props.viewState.setCheckedApplicationStepId(checkedApplicationStepId);

        this.setState({
            applicationId: res.applicationId,
            status: res.status,
            statusCode: res.statusCode,
            checkedApplicationType: res.applicationType,
            lotNumbers: res.lotNumbers,
            applicantInformations: res.applicantInformations,
            notificable: res.notificable,
            ApplyAnswerForm: res,
            checkedApplicationCategory: checkedApplicationCategory,
            applicantAddInformations: applicantAddInformations,
            applicationFiles: applicationFiles,
            answers: answers,
            departmentAnswers: departmentAnswers,
            answerHistory: answerHistory,
            answerFile: answerFile,
            notificationFiles: notificationFiles,
            selectedAnswerHistory: selectedAnswerHistory,
            selectedAnswer: selectedAnswer,
            selectedAnswerFiles: selectedAnswerFiles,
            acceptingFlag: acceptingFlag,
            businessAnswerCompleted: businessAnswerCompleted,
            firstAccepting: res.firstAccepting
        },()=>{
            if(initFlag){
                this.props.viewState.setMapBaseElement(this.mapBaseElement);
                this.props.viewState.setMapBaseContainerElement(this.mpBaseContainerElement);
                setTimeout(() => {
                    this.props.viewState.updateMapDimensions();
                    this.props.focusMapPlaceDriver();
                }, 1000);
            }
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
     * トップ画面へ戻す
     */
    back(){
        const items = this.state.terria.workbench.items;
        for (const aItem of items) {
            if (aItem.uniqueId === Config.layer.lotnumberSearchLayerNameForApplicationSearchTarget) {
                this.state.terria.workbench.remove(aItem);
                aItem.loadMapItems();
            }
        }
        this.props.viewState.backFromConfirmAnswerInformationView(); 
        let intervalID = this.state.intervalID;
        clearInterval(intervalID);
    }

    /**
     * 回答レポート出力
     */
    export(){
        let id = this.props.viewState.answerContent.loginId;
        let password = this.props.viewState.answerContent.password;
        let applicationId = this.state.applicationId;
        fetch(Config.config.apiUrl + "/answer/report/" + applicationId + "/" + checkedApplicationStepId,{
            method: 'POST',
            body: JSON.stringify({
                loginId: id,
                password: password
            }),
            headers: new Headers({ 'Content-type': 'application/json' }),
        })
        .then((res) => {
            if(res.status === 401){
                alert("認証情報が無効です。ページの再読み込みを行います。");
                window.location.reload();
            }else{
                return res.blob();
            }
        })
        .then(blob => {
            const now = new Date();
            const filename = Config.config.answerReportName + "_" + now.toLocaleDateString();
            let anchor = document.createElement("a");
            anchor.href = window.URL.createObjectURL(blob);
            anchor.download = filename + ".xlsx";
            anchor.click();
        }).catch(error => {
            console.error('回答レポート出力に失敗しました。', error);
            alert('回答レポート出力に失敗しました。');
        });
    }

    /**
     * 回答内容一覧の行クリックイベント
     * @param {*} event イベント
     * @param {*} answerId 回答ID
     * @param {*} departmentAnswerId 部署回答ID
     */
         clickAnswer = (event, answerId, departmentAnswerId = 0) => {
            const checkedApplicationStepId = this.state.checkedApplicationStepId;
    
            if(checkedApplicationStepId == 2){
                if(departmentAnswerId!==undefined && departmentAnswerId !== null && Number(departmentAnswerId) > 0){
                    const departmentAnswers = [...this.state.departmentAnswers];
                    const selectedDepartmentAnswers = departmentAnswers.find(answer=>answer.departmentAnswerId === departmentAnswerId);
                    if(answerId!==undefined && answerId !== null && Number(answerId) > 0){
                        const selectedAnswer = selectedDepartmentAnswers.answers.find(answer=>answer.answerId === answerId);
                        const selectedAnswerFiles = selectedDepartmentAnswers?.answerFiles?selectedDepartmentAnswers.answerFiles:[];
                        const selectedAnswerHistory = selectedAnswer.answerHistorys?selectedAnswer.answerHistorys:[];
                        this.setState({selectedAnswer:selectedAnswer,selectedAnswerFiles:selectedAnswerFiles,selectedAnswerHistory:selectedAnswerHistory});
                    }else{
    
                        const selectedAnswerFiles = selectedDepartmentAnswers?.answerFiles?selectedDepartmentAnswers.answerFiles:[];
                        const selectedAnswerHistory = selectedDepartmentAnswers.answerHistorys?selectedDepartmentAnswers.answerHistorys:[];
                        this.setState({selectedAnswer:selectedDepartmentAnswers,selectedAnswerFiles:selectedAnswerFiles,selectedAnswerHistory:selectedAnswerHistory});
                    }
                }else{
                    // 選択中回答がない場合、申請段階ごとの回答ファイルと、回答履歴を表示
                    this.setState({selectedAnswer:null, selectedAnswerFiles:  [...this.state.answerFile], selectedAnswerHistory: [...(this.state.answerHistory ?? [])]});
                }
            }
            if(checkedApplicationStepId == 1){
                if(answerId!==undefined && answerId !== null && Number(answerId) > 0){
                    const answers = [...this.state.answers];
                    const selectedAnswer = answers.find(answer=>answer.answerId === answerId);
                    const selectedAnswerFiles = selectedAnswer?.answerFiles?selectedAnswer.answerFiles:[];
                    const selectedAnswerHistory = selectedAnswer?.answerHistorys?selectedAnswer.answerHistorys:[];
                    this.setState({selectedAnswer:selectedAnswer,selectedAnswerFiles:selectedAnswerFiles,selectedAnswerHistory:selectedAnswerHistory});
                }else{
                    this.setState({selectedAnswer:null, selectedAnswerFiles:  [...this.state.answerFile], selectedAnswerHistory: [...(this.state.answerHistory ?? [])]});
                }
            }
    
            if(checkedApplicationStepId == 3){
                if(answerId!==undefined && answerId !== null && Number(answerId) > 0){
                    const answers = [...this.state.answers];
                    const selectedAnswer = answers.find(answer=>answer.answerId === answerId);
                    const selectedAnswerFiles =   [...this.state.answerFile];
                    const selectedAnswerHistory = selectedAnswer?.answerHistorys?selectedAnswer.answerHistorys:[];
                    this.setState({selectedAnswer:selectedAnswer,selectedAnswerFiles:selectedAnswerFiles,selectedAnswerHistory:selectedAnswerHistory});
                }else{
                    this.setState({selectedAnswer:null, selectedAnswerFiles:  [...this.state.answerFile], selectedAnswerHistory: [...(this.state.answerHistory ?? [])]});
                }
            }
        }
    
    /**
     * 回答入力のコールバック処理
     * @param {*} answers 回答一覧
     */
    answerInputCallback = (answers) => {
        let sortedAnswers = [];
        if(answers){
            sortedAnswers =  answers.sort((a, b) => a.judgementInformation.department.departmentId
                                                        .localeCompare(b.judgementInformation.department.departmentId));
        }
        this.setState({answers: answers,sortedAnswers: sortedAnswers});
    }

    /**
     * 再申請可能を判定
     * @returns 
     */
    canReapply(){
        // 再申請可否
        let canReapply = false;
        // 申請ステータスコード
        const statusCode = this.state.statusCode;

        switch(statusCode){
            // 104:事前相談：未完（要再申請）
            case "104":
                canReapply = true;
                this.props.viewState.setReapplyApplicationStepId(1,1);
                break;
            // 205:事前協議：未完（要再申請）
            case "205":
                canReapply = true;
                this.props.viewState.setReapplyApplicationStepId(2,2);
                break;
            // 304:許可判定：未完（要再申請）
            case "304":
                canReapply = true;
                this.props.viewState.setReapplyApplicationStepId(3,3);
                break;
            // 105:事前相談：完了
            case "105":
            // 206:事前協議：完了
            case "206":
            // 305:許可判定：完了
            case "305":
                let preApplicationStepId = 1;
                if(statusCode == "206"){
                    preApplicationStepId = 2;
                }
                if(statusCode == "305"){
                    preApplicationStepId = 3;
                }
                const checkedApplicationType = this.state.checkedApplicationType;
                if(checkedApplicationType.applicationSteps && Object.keys(checkedApplicationType.applicationSteps).length > 1){
                    let index = checkedApplicationType.applicationSteps.findIndex(step => step.applicationStepId == preApplicationStepId);

                    // 当前の申請段階IDは、申請種類より、次の段階がある場合、次の段階再申請できる
                    if(index > -1 && index < Object.keys(checkedApplicationType.applicationSteps).length - 1){
                        // 次の申請段階がある場合、
                        canReapply = true;
                        this.props.viewState.setReapplyApplicationStepId(checkedApplicationType.applicationSteps[index+1].applicationStepId, preApplicationStepId);
                    }
                }
                break;
            // 201:事前協議：未回答
            case "201":
            // 202:事前協議：未完（回答準備中）
            case "202":
            // 203:事前協議：未完（回答精査中）
            case "203":
            // 204:事前協議：未完（協議進行中）
            case "204":
                // 受付・差戻したから、すべての回答の事業者合意登録が完了するまで、再申請が行われる
                // 受付フラグ
                const acceptingFlag = this.state.acceptingFlag;
                // すべての回答の事業者合意登録が完了するかフラグ
                const businessAnswerCompleted = this.state.businessAnswerCompleted;
                // 事前協議の申請が確認中、かつ、受付版情報が0であるかフラグ
                const firstAccepting = this.state.firstAccepting;

                if(firstAccepting == true){
                    canReapply = false;
                }else{
                    if(acceptingFlag !== "0" && (businessAnswerCompleted != null && businessAnswerCompleted != undefined && businessAnswerCompleted == false)){
                        canReapply = true;
                        this.props.viewState.setReapplyApplicationStepId(2,2);
                    }else{
                        canReapply = false;
                    }
                }

                break;
            default:
                canReapply = false;
        }

        return canReapply;
    }

    /**
     * 申請段階に対する申請情報が存在するか判定
     * @param {Number} applicationStepId 申請段階ID
     * @returns 
     */
        isExist(applicationStepId){

            const applyAnswerDetailForms = this.state.ApplyAnswerForm.applyAnswerDetails;
    
            let isExist = false;
            if(applyAnswerDetailForms){
                let index = applyAnswerDetailForms.findIndex(detail =>detail.applicationStepId === applicationStepId);
                if(index > -1){
                    isExist = true;
                }
            }
            return isExist;
        }
    
    render() {
        const t = this.props.t;
        const infoMessage = t("infoMessage.tipsForConfirmAnswer");
        const answers = this.state.answers;
        const sortedAnswers = this.state.sortedAnswers;
        const status = this.state.status;
        const statusCode = this.state.statusCode;
        const applicantInformations = this.state.applicantInformations;
        const checkedApplicationCategory = this.state.checkedApplicationCategory;
        const applicationFiles = this.state.applicationFiles;
        const lotNumbers = this.state.lotNumbers;
        const activeFileListType = this.state.activeFileListType;
        const checkedApplicationStepId = this.state.checkedApplicationStepId;
        const selectedAnswer = this.state.selectedAnswer;
        const selectedAnswerFiles = this.state.selectedAnswerFiles;
        const applicationType = this.state.checkedApplicationType;
        const applicantAddInformations = this.state.applicantAddInformations;
        const departmentAnswers = this.state.departmentAnswers;
        const ledgerMaster = this.state.ledgerMaster;
        const notificationFiles = this.state.notificationFiles;
        const applicationId = this.state.applicationId;
        let notifiedAnswerCount = 0
        if(checkedApplicationStepId == 2){
            if(departmentAnswers){
                notifiedAnswerCount = Object.keys(departmentAnswers).length;
            }
        }else{
            if(answers){
                notifiedAnswerCount = Object.keys(answers).length;
            }
        }

        let answerFileCount = 0;
        if(checkedApplicationStepId == 3){

            // 回答ファイルの件数
            const count1 = Object.keys(selectedAnswerFiles).length;
            answerFileCount = count1;
        }

        return (
            <>
                <Box overflow={false} overflowY={false}>
                    <Box col8 className={PageStyle.text_area}>
                        <span dangerouslySetInnerHTML={{ __html: infoMessage }}></span>
                    </Box>
                    <Box col4 right>
                        <button
                            className={`${CustomStyle.btn_baise_style} ${CustomStyle.btn_gry}`}
                            style={{width:"30%",height:"40px"}}
                            onClick={e => {
                                this.back();
                            }}
                        >
                            <span>戻る</span>
                        </button>
                        <button
                            className={`${CustomStyle.btn_baise_style} `}
                            disabled={this.canReapply() ? false : true}
                            style={{width:"30%",height:"40px"}}
                            onClick={e => {
                                this.executeForReApply();
                            }}
                        >
                            <span>再申請</span>
                        </button>
                    </Box>
                </Box>
                <Box>
                    <Box col12 className={CustomStyle.custom_nuv}>
                        回答内容確認
                    </Box>
                </Box>
                <Box>
                    <Box col4 style={{display:"block"}}>
                        <Spacing bottom={3} />
                        <Box col12 centered>
                            <button
                                className={`${CustomStyle.btn_baise_style} ${checkedApplicationStepId !== 1? CustomStyle.checked_button: ""}`}
                                style={{width:"30%",height:"40px"}}
                                onClick={e => {
                                    this.clickAnswer(null,0,0);
                                    this.setApplyAnswerInfoToState(this.state.ApplyAnswerForm, 1, null, false);
                                    if(activeFileListType == 4){
                                        this.setState({activeFileListType:1});
                                    }
                                    this.setState({checkedApplicationStepId:1});
                                    setTimeout(() => {
                                        this.props.viewState.updateMapDimensions();
                                    }, 2000);
                                }}
                            >
                                <span>事前相談</span>
                            </button>
                            <button
                                className={`${CustomStyle.btn_baise_style} ${!this.isExist(2) ? CustomStyle.disabled_button : checkedApplicationStepId !== 2? CustomStyle.checked_button:""}`}
                                style={{width:"30%",height:"40px"}}
                                disabled = {!this.isExist(2)}
                                onClick={e => {
                                    this.clickAnswer(null,0,0);
                                    this.setApplyAnswerInfoToState(this.state.ApplyAnswerForm, 2, null,  false);
                                    if(activeFileListType == 4){
                                        this.setState({activeFileListType:1});
                                    }
                                    this.setState({checkedApplicationStepId:2});
                                    setTimeout(() => {
                                        this.props.viewState.updateMapDimensions();
                                    }, 2000);
                                }}
                            >
                                <span>事前協議</span>
                            </button>
                            <button
                                className={`${CustomStyle.btn_baise_style} ${!this.isExist(3) ? CustomStyle.disabled_button : checkedApplicationStepId !== 3? CustomStyle.checked_button:""}`}
                                style={{width:"30%",height:"40px"}}
                                disabled = {!this.isExist(3)}
                                onClick={e => {
                                    this.clickAnswer(null,0,0);
                                    this.setApplyAnswerInfoToState(this.state.ApplyAnswerForm, 3, null, false);
                                    this.setState({checkedApplicationStepId:3});
                                    setTimeout(() => {
                                        this.props.viewState.updateMapDimensions();
                                    }, 2000);
                                }}
                            >
                                <span>許可判定</span>
                            </button>
                        </Box>
                        <ApplicantInformation 
                            viewState={this.props.viewState} 
                            terria={this.props.terria} 
                            applicationType={applicationType}
                            applicantInformations={applicantInformations} 
                            applicantAddInformations={applicantAddInformations} 
                            checkedApplicationCategory={checkedApplicationCategory} 
                            lotNumbers={lotNumbers}
                            status={status}
                            answers={answers}
                            departmentAnswers={departmentAnswers} 
                            applicationId={applicationId}
                         />
                        <Spacing bottom={3} />
                        <button className={`${CustomStyle.custom_selection} ${activeFileListType == 1? CustomStyle.checked_file_button: ""}`}
                            onClick={e => { this.setState({activeFileListType:1})}}
                        >
                            <span>回答ファイル一覧</span>
                            <StyledIcon styledWidth={"12px"} fillColor={"white"} glyph={Icon.GLYPHS.right}/>
                        </button>
                        <Spacing bottom={1} />
                        <button className={`${CustomStyle.custom_selection} ${activeFileListType == 2? CustomStyle.checked_file_button: ""}`}
                            onClick={e => { this.setState({activeFileListType:2})}}
                        >
                            <span>申請ファイル一覧</span>
                            <StyledIcon styledWidth={"12px"} fillColor={"white"} glyph={Icon.GLYPHS.right}/>
                        </button>
                        <Spacing bottom={1} />
                        <button className={`${CustomStyle.custom_selection} ${activeFileListType == 3? CustomStyle.checked_file_button: ""}`}
                            onClick={e => { this.setState({activeFileListType:3})}}
                        >
                            <span>発行様式</span>
                            <StyledIcon styledWidth={"12px"} fillColor={"white"} glyph={Icon.GLYPHS.right}/>
                        </button>
                    </Box>
                    <Box col8 ref={this.mpBaseContainerElement} style={{height:"70vh"}}>
                        <Box
                            centered
                            displayInlineBlock
                            className={CustomStyle.custom_content}
                        >
                            {checkedApplicationStepId == 1&& (
                            <Box col12>
                                <AnswerContentList 
                                    viewState={this.props.viewState} 
                                    terria={this.props.terria} 
                                    answers ={answers}
                                    intervalID = {this.state.intervalID}
                                    clickAnswer = {this.clickAnswer}
                                    selectedAnswerId = {selectedAnswer?selectedAnswer.answerId?selectedAnswer.answerId:selectedAnswer.departmentAnswerId:0}
                                    applicationId = {applicationId}
                                />
                            </Box>
                            )}
                            {checkedApplicationStepId == 2&& (
                            <Box col12>
                                <NegotiationContentList 
                                    viewState={this.props.viewState} 
                                    terria={this.props.terria} 
                                    departmentAnswers ={departmentAnswers}
                                    intervalID = {this.state.intervalID}
                                    clickAnswer = {this.clickAnswer}
                                    selectedAnswerId = {selectedAnswer?selectedAnswer.answerId:0}
                                    selectedDepartmentAnswerId = {selectedAnswer?selectedAnswer.departmentAnswerId:0}
                                    editable = {true}
                                    callback = {this.answerInputCallback}
                                    ledgerMaster = {ledgerMaster}
                                    applicationId = {applicationId}
                                    changeAnswerInfo = {this.changeAnswerInfo}
                                />
                            </Box>
                            )}
                            {checkedApplicationStepId == 3&& (
                            <Box col12>
                                <AssessmentContentList 
                                    viewState={this.props.viewState} 
                                    terria={this.props.terria} 
                                    answers ={answers}
                                    intervalID = {this.state.intervalID}
                                    clickAnswer = {this.clickAnswer}
                                    selectedAnswerId = {selectedAnswer?selectedAnswer.answerId:0}
                                    applicationId = {applicationId}
                                    answerFileCount={answerFileCount}
                                />
                            </Box>
                            )}
                            <Spacing bottom={1} />
                            <Box>
                                {activeFileListType == 1&& (
                                    <Box col8>
                                        <AnswerFileList 
                                            viewState={this.props.viewState} 
                                            terria={this.props.terria} 
                                            answerFiles = {selectedAnswerFiles}
                                        />
                                    </Box>
                                )}
                                {activeFileListType == 2&& (
                                    <Box col8>
                                        <ApplicationFileList 
                                            viewState={this.props.viewState} 
                                            terria={this.props.terria} 
                                            applicationFiles = {applicationFiles}
                                        />
                                    </Box>
                                )}
                                {activeFileListType == 3&& (
                                    <Box col8>
                                        <NotificationFileList 
                                            viewState={this.props.viewState} 
                                            terria={this.props.terria} 
                                            notifiedAnswerCount = {notifiedAnswerCount}
                                            notificationFiles = {notificationFiles}
                                            applicationStepId = {checkedApplicationStepId}
                                        />
                                    </Box>
                                )}
                                <Box col4 ref={this.mapBaseElement} style={{height:"23vh"}}>
                                </Box>
                            </Box>
                        </Box>
                    </Box>
                </Box>
            </>
        );
    }
}
export default withTranslation()(withTheme(AnswerContent));