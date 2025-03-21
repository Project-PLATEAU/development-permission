import { observer } from "mobx-react";
import PropTypes from "prop-types";
import React from "react";
import { withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import Spacing from "../../../../Styled/Spacing";
import Box from "../../../../Styled/Box";
import CustomStyle from "./scss/confirm-application-details.scss";
import CustomGeneralStyle from "./scss/general-condition-diagnosis.scss";
import Config from "../../../../../customconfig.json";
import GeneralConditionDiagnosis from "./GeneralConditionDiagnosis.jsx";

/**
 * 申請内容確認画面
 */
@observer
class ConfirmApplicationDetails extends React.Component {
    static displayName = "ConfirmApplicationDetails";
    static propTypes = {
        terria: PropTypes.object.isRequired,
        viewState: PropTypes.object.isRequired,
        theme: PropTypes.object,
        t: PropTypes.func.isRequired
    }

    constructor(props) {
        super(props);
        this.state = {
            viewState: props.viewState,
            terria: props.terria,
            height: 0
        };
    }
    
    /**
     * 初期処理
     */
    componentDidMount() {
        document.getElementById("customloader").style.display = "none";
        this.getWindowSize();
    }

    /**
     * 高さ再計算
     */
    getWindowSize() {
        if(this.props.viewState.showConfirmApplicationDetails){
            let win = window;
            let e = window.document.documentElement;
            let g = window.document.documentElement.getElementsByTagName('body')[0];
            let h = win.innerHeight|| e.clientHeight|| g.clientHeight;
            const getRect = document.getElementById("AapplicationConfirmArea");
            let height = h - getRect.getBoundingClientRect().top - 85;
            this.setState({height: height});
        }
    }

    /**
     * リサイズ
     */
    componentWillMount () {
        window.addEventListener('resize', () => {
                this.getWindowSize() 
        })
    }

    /**
     * 申請情報を新規登録する
     */
    register(){
        document.getElementById("applicationFrame").scrollTop = 0;
        document.getElementById("customloader").style.display = "block";
        let generalConditionDiagnosisResult = Object.values(this.props.viewState.generalConditionDiagnosisResult);
        generalConditionDiagnosisResult = generalConditionDiagnosisResult.filter(Boolean);
        let checkedApplicationCategory = Object.values(this.props.viewState.checkedApplicationCategory);
        checkedApplicationCategory = checkedApplicationCategory.filter(Boolean);
        let applicationPlace = Object.values(this.props.viewState.applicationPlace);
        applicationPlace = applicationPlace.filter(Boolean);
        let applicantInformation = Object.values(this.props.viewState.applicantInformation);
        applicantInformation = applicantInformation.filter(Boolean);
        let notifyCount = 0;
        let rollbackCount = 0;
        const captureRequiredJudgement = Config.captureRequiredJudgement;
        const selectedApplicationType = this.props.viewState.selectedApplicationType;
        const checkedApplicationStepId = this.props.viewState.checkedApplicationStepId;
        // 申請情報登録を行う
        const mainFunc = ()=>{
            fetch(Config.config.apiUrl + "/application/register", {
                method: 'POST',
                body: JSON.stringify({
                    generalConditionDiagnosisResultForm:generalConditionDiagnosisResult,
                    applicationCategories:checkedApplicationCategory,
                    lotNumbers: applicationPlace,
                    applicantInformationItemForm:applicantInformation,
                    folderName:this.props.viewState.folderName,
                    applicationTypeId: selectedApplicationType.applicationTypeId,
                    applicationStepId: checkedApplicationStepId

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
                    let applicationFile = this.props.viewState.applicationFile;
                    let resApplicationId = res.applicationId;
                    // 回答予定日数
                    let answerExpectDays = res.answerExpectDays;
                    let fileCount = 0;

                    //シミュレート実行
                    if(captureRequiredJudgement.find((num) => num == selectedApplicationType.applicationTypeId) != undefined){
                        //ファイル名を生成
                        const fileName = this.props.viewState.getFileName();
                        const requestBody = {
                            folderName: this.props.viewState.folderName,
                            fileName:fileName,
                            lotNumbers: applicationPlace,
                            applicationCategories: checkedApplicationCategory,
                            generalConditionDiagnosisResults: generalConditionDiagnosisResult,
                            applicationId:res.applicationId
                        };
                        //概況診断シミュレート実行API
                        fetch(Config.config.simulatorApiUrl+"/simulator/execution", {
                            method: 'POST',
                            body: JSON.stringify(requestBody),
                            headers: new Headers({ 'Content-type': 'application/json' }),
                        })
                        .then(res => res.json())
                        .then(res => {
                            if(res.status === 401){
                                alert("認証情報が無効です。ページの再読み込みを行います。");
                                window.location.reload();
                                return null;
                            }
                            if(res.status === 202){
                                //処理状況の対象にセット
                                this.props.viewState.setProgressList(requestBody);
                                //概況診断レポート一覧画面表示
                                this.props.viewState.setGeneralConditionDiagnosisrReportShow(true);
                            }else{
                                alert('帳票生成呼び出し処理に失敗しました');
                            }
                        }).catch(error => {
                            console.error('処理に失敗しました', error);
                            alert('帳票生成呼び出し処理に失敗しました');
                        });
                    }

                    if(Object.keys(applicationFile).length > 0){
                        Object.keys(applicationFile).map(key => {
                            let versionInformation = applicationFile[key]["versionInformation"];
                            if(versionInformation){
                                versionInformation = versionInformation + 1;
                            }else{
                                versionInformation = 1;
                            }
                            // 申請対象ファイルをサーバーに送信する
                            Object.keys(applicationFile[key]["uploadFileFormList"]).map(fileKey => {
                                applicationFile[key]["uploadFileFormList"][fileKey]["applicationId"] = res.applicationId;
                                applicationFile[key]["uploadFileFormList"][fileKey]["applicationStepId"] = checkedApplicationStepId;
                                applicationFile[key]["uploadFileFormList"][fileKey]["applicationFileId"] = applicationFile[key]["applicationFileId"];
                                applicationFile[key]["uploadFileFormList"][fileKey]["versionInformation"] = versionInformation;
                                const formData  = new FormData();
                                for(const name in applicationFile[key]["uploadFileFormList"][fileKey]) {
                                    //フォームデータでObjectのマッピングは非対応のため除外
                                    if (name != 'departmentFormList'){
                                        formData.append(name, applicationFile[key]["uploadFileFormList"][fileKey][name]);
                                    }
                                }
                                fileCount = fileCount + 1;
                                fetch(Config.config.apiUrl + "/application/file/upload", {
                                    method: 'POST',
                                    body: formData,
                                })
                                .then(res => res.json())
                                .then(res => {
                                    if(res.status === 401){
                                        alert("認証情報が無効です。ページの再読み込みを行います。");
                                        window.location.reload();
                                        return null;
                                    }
                                    applicationFile[key]["uploadFileFormList"][fileKey]["status"] = res.status;
                                    if(res.status !== 201){
                                        // 仮申請状態のデータを消去
                                        rollbackCount = rollbackCount + 1;
                                        this.execRollBack(1, resApplicationId, rollbackCount);
                                        return null;
                                    }
                                    let completeFlg = true;
                                    Object.keys(applicationFile).map(key => { 
                                        Object.keys(applicationFile[key]["uploadFileFormList"]).map(fileKey => {
                                            if(!applicationFile[key]["uploadFileFormList"][fileKey]["status"]){
                                                completeFlg = false;
                                            }
                                        })
                                    })
                                    if(completeFlg){
                                        notifyCount = notifyCount + 1;
                                        this.notify(resApplicationId,notifyCount,answerExpectDays, checkedApplicationStepId);               
                                    }
                                }).catch(error => {
                                    console.error('処理に失敗しました', error);     
                                    // 仮申請状態のデータの消去
                                    rollbackCount = rollbackCount + 1;
                                    this.execRollBack(1, resApplicationId, rollbackCount);
                                });
                            });
                        });
                        if(fileCount === 0){
                            notifyCount = notifyCount + 1;
                            this.notify(resApplicationId,notifyCount,answerExpectDays, checkedApplicationStepId);  
                        }
                    }else{
                        notifyCount = notifyCount + 1;
                        this.notify(resApplicationId,notifyCount,answerExpectDays, checkedApplicationStepId); 
                    }
                }else{
                    document.getElementById("customloader").style.display = "none";
                    alert("申請の登録処理に失敗しました");
                    // 自動ロールバック可能ため、「仮申請状態のデータの消去」を行わない
                }
            }).catch(error => {
                document.getElementById("customloader").style.display = "none";
                console.error('処理に失敗しました', error);
                alert('処理に失敗しました');
            });
        }
        //一時フォルダの生成有無判定後申請登録処理の呼び出し
        if(captureRequiredJudgement.find((num) => num == selectedApplicationType.applicationTypeId) != undefined){
            fetch(Config.config.apiUrl + "/judgement/image/upload/preparation")
            .then(res => res.json())
            .then(res => {
                if(res.status === 401){
                    alert("認証情報が無効です。ページの再読み込みを行います。");
                    window.location.reload();
                    return null;
                }
                if (res.folderName) {
                    this.props.viewState.setFolderName(res.folderName);
                    mainFunc();
                }else{
                    document.getElementById("customloader").style.display = "none";
                    alert('処理に失敗しました');
                }
            }).catch(error => {
                document.getElementById("customloader").style.display = "none";
                console.error('処理に失敗しました', error);
                alert('処理に失敗しました');
            });
        }else{
            this.props.viewState.setFolderName(null);
            mainFunc();
        }
    }

    /**
     * 再申請情報を登録する
     */
    reApply(){
        
        document.getElementById("applicationFrame").scrollTop = 0;
        document.getElementById("customloader").style.display = "block";
        let notifyCount = 0;
        const captureRequiredJudgement = Config.captureRequiredJudgement;
        //処理中の申請段階ID
        const applicationStepId = this.props.viewState.reapplyApplicationStepId;
        //前回申請の申請段階ID
        const preApplicationStepId = this.props.viewState.preReapplyApplicationStepId;
        // 申請種類
        const selectedApplicationType = this.props.viewState.reApplication.applicationType;
        // 再申請のタイミングで、申請段階が変わるか、申請区分が変わるか判定して、概況診断レポートが出力必要フラグを取得
        const outputReportFlag = this.isOutputReport();

        // 申請ファイル
        const applicationFile = this.props.viewState.reApplicationFile;
        let quoteFileList = [];
        let uploadFileList = [];

        let versionInformation =  this.props.viewState.reApplication.versionInformation;
        // 次の段階へ進むではない場合、版情報+1にする
        if(applicationStepId == preApplicationStepId){
            versionInformation = Number(versionInformation) + 1;
        }else{
            versionInformation = 1;
        }

        // 引継ファイル・新規追加ファイル
        Object.keys(applicationFile).map(key => {
            Object.keys(applicationFile[key]["uploadFileFormList"]).map(fileKey => {
                if (applicationFile[key].applicationFileId !== 9999 && applicationFile[key].applicationFileId !== '9999' ) {

                    // レコード追加フラグが「1」である場合、DBへ登録必要
                    if(applicationFile[key]["uploadFileFormList"][fileKey]["addFlag"] == 1){
                        applicationFile[key]["uploadFileFormList"][fileKey]["applicationId"] = this.props.viewState.reApplication.applicationId;
                        applicationFile[key]["uploadFileFormList"][fileKey]["applicationStepId"] = applicationStepId;
                        applicationFile[key]["uploadFileFormList"][fileKey]["applicationFileId"] = applicationFile[key]["applicationFileId"];
                        applicationFile[key]["uploadFileFormList"][fileKey]["versionInformation"] = versionInformation;
    
                        // ファイル実体がアップロード必要である場合、新規追加ファイルリストに追加
                        if(applicationFile[key]["uploadFileFormList"][fileKey]["fileUploadFlag"] == 1){
                            uploadFileList.push(applicationFile[key]["uploadFileFormList"][fileKey]);
                        }else{
                            quoteFileList.push(applicationFile[key]["uploadFileFormList"][fileKey]);
                        }
                    }
                }
            })
        })


        // 再申請情報登録を行う
        const mainFunc = ()=>{
            let fileCount = 0;

            if(Object.keys(uploadFileList).length > 0){
                // 申請対象ファイルをサーバーに送信する
                Object.keys(uploadFileList).map(fileKey => {
                    const formData  = new FormData();
                    for(const name in uploadFileList[fileKey]) {
                        //フォームデータでObjectのマッピングは非対応のため除外
                        if (name != 'departmentFormList'){
                            formData.append(name, uploadFileList[fileKey][name]);
                        }
                    }
                    fileCount = fileCount + 1;
                    fetch(Config.config.apiUrl + "/application/file/upload", {
                        method: 'POST',
                        body: formData,
                    })
                    .then(res => res.json())
                    .then(res => {
                        if(res.status === 401){
                            alert("認証情報が無効です。ページの再読み込みを行います。");
                            window.location.reload();
                            return null;
                        }
                        uploadFileList[fileKey]["status"] = res.status;
                        if(res.status !== 201){
                            alert('アップロードに失敗しました');
                        }
                        let completeFlg = true;
                        Object.keys(uploadFileList).map(fileKey => {
                            if(!uploadFileList[fileKey]["status"]){
                                completeFlg = false;
                            }
                        })
                        if(completeFlg){
                            notifyCount = notifyCount + 1;
                            // 再申請情報登録を行う
                            this.registerReapplyInfo(outputReportFlag, quoteFileList, notifyCount);              
                        }
                    }).catch(error => {
                        document.getElementById("customloader").style.display = "none";
                        console.error('処理に失敗しました', error);
                        alert('処理に失敗しました');
                    });
                });
            }else{
                notifyCount = notifyCount + 1;
                // 再申請情報登録を行う
                this.registerReapplyInfo(outputReportFlag, quoteFileList, notifyCount);
            }
        }

        //一時フォルダの生成有無判定後申請登録処理の呼び出し
        if(outputReportFlag && captureRequiredJudgement.find((num) => num == selectedApplicationType.applicationTypeId) != undefined){
            fetch(Config.config.apiUrl + "/judgement/image/upload/preparation")
            .then(res => res.json())
            .then(res => {
                if(res.status === 401){
                    alert("認証情報が無効です。ページの再読み込みを行います。");
                    window.location.reload();
                    return null;
                }
                if (res.folderName) {
                    this.props.viewState.setFolderName(res.folderName);
                    mainFunc();
                }else{
                    document.getElementById("customloader").style.display = "none";
                    alert('処理に失敗しました');
                }
            }).catch(error => {
                document.getElementById("customloader").style.display = "none";
                console.error('処理に失敗しました', error);
                alert('処理に失敗しました');
            });
        }else{
            this.props.viewState.setFolderName(null);
            mainFunc();
        }
    }

    /**
     * 申請ファイルをアップロード完了後、再申請情報登録をDBへ登録
     * 
     */
    registerReapplyInfo(outputReportFlag, quoteFileList, notifyCount){
        if(notifyCount !==1){
            return ;
        }
        const captureRequiredJudgement = Config.captureRequiredJudgement;

        //処理中の申請段階ID
        const applicationStepId = this.props.viewState.reapplyApplicationStepId;
        //前回申請の申請段階ID
        const preApplicationStepId = this.props.viewState.preReapplyApplicationStepId;
        // 申請種類
        const selectedApplicationType = this.props.viewState.reApplication.applicationType;
        // 申請地番
        let applicationPlace = Object.values(this.props.viewState.reApplication.lotNumbers);
        applicationPlace = applicationPlace.filter(Boolean);
        // 申請区分
        let checkedApplicationCategory = Object.values(this.props.viewState.checkedApplicationCategory);
        checkedApplicationCategory = checkedApplicationCategory.filter(Boolean);
        //許可判定の場合、概況診断結果が、保存している再申請情報から取得
        if(applicationStepId == 3){
            checkedApplicationCategory = this.props.viewState.reApplication.applicationCategories;
        }
        // 概要診断結果
        let generalConditionDiagnosisResult = Object.values(this.props.viewState.generalConditionDiagnosisResult);
        //許可判定の場合、概況診断結果が、保存している再申請情報から取得
        if(applicationStepId == 3){
            generalConditionDiagnosisResult = this.props.viewState.reApplication.generalConditionDiagnosisResultForm
        }
        generalConditionDiagnosisResult = generalConditionDiagnosisResult.filter(Boolean);
        // 事業者情報
        let applicantInformation = Object.values(this.props.viewState.reApplication.applicantInformations);
        applicantInformation = applicantInformation.filter(Boolean);
        // 申請追加情報
        let applicantnAddInformation = Object.values(this.props.viewState.applicantnAddInformation);
        applicantnAddInformation = applicantnAddInformation.filter(Boolean);

        // 申請ファイル
        let applicationFileList = [];
        let applicationFile = this.props.viewState.reApplicationFile;
        Object.keys(applicationFile).map(key => {
            applicationFileList.push({"applicationFileId":applicationFile[key]["applicationFileId"]});
        })

        fetch(Config.config.apiUrl + "/application/reapplication", {
            method: 'POST',
            body: JSON.stringify({
                applicationId: this.props.viewState.reApplication.applicationId,
                outputReportFlag: outputReportFlag,
                folderName: this.props.viewState.folderName,
                applicationTypeId: selectedApplicationType.applicationTypeId,
                applicationStepId: applicationStepId,
                preApplicationStepId: preApplicationStepId,
                versionInformation: this.props.viewState.reApplication.versionInformation,
                loginId: this.props.viewState.reApplication.loginId,
                password: this.props.viewState.reApplication.password,
                lotNumbers: applicationPlace,
                applicationCategories: checkedApplicationCategory,
                applicantInformations: applicantInformation,
                applicantAddInformations: applicantnAddInformation,
                uploadFiles: quoteFileList,
                generalConditionDiagnosisResultForm: generalConditionDiagnosisResult,
                applicationFileForm:applicationFileList
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
            if (res.answerExpectDays) {
                // 回答予定日数
                let answerExpectDays = res.answerExpectDays;
                // 再申請完了通知を行う
                this.reApplyNotify(res.applicationId, answerExpectDays, res.applicationStepId);
            }else{
                document.getElementById("customloader").style.display = "none";
                lert('再申請情報登録処理に失敗しました');
            }
        }).catch(error => {
            document.getElementById("customloader").style.display = "none";
            console.error('処理に失敗しました', error);
            alert('処理に失敗しました');
        });

        //シミュレート実行
        if(outputReportFlag && captureRequiredJudgement.find((num) => num == selectedApplicationType.applicationTypeId) != undefined){
            //ファイル名を生成
            const fileName = this.props.viewState.getFileName();
            const requestBody = {
                folderName: this.props.viewState.folderName,
                fileName:fileName,
                lotNumbers: applicationPlace,
                applicationCategories: checkedApplicationCategory,
                generalConditionDiagnosisResults: generalConditionDiagnosisResult,
                applicationId:this.props.viewState.reApplication.applicationId
            };
            //概況診断シミュレート実行API
            fetch(Config.config.simulatorApiUrl+"/simulator/execution", {
                method: 'POST',
                body: JSON.stringify(requestBody),
                headers: new Headers({ 'Content-type': 'application/json' }),
            })
            .then(res => res.json())
            .then(res => {
                if(res.status === 401){
                    alert("認証情報が無効です。ページの再読み込みを行います。");
                    window.location.reload();
                    return null;
                }
                if(res.status === 202){
                    //処理状況の対象にセット
                    this.props.viewState.setProgressList(requestBody);
                    //概況診断レポート一覧画面表示
                    this.props.viewState.setGeneralConditionDiagnosisrReportShow(true);
                }else{
                    alert('帳票生成呼び出し処理に失敗しました');
                }
            }).catch(error => {
                console.error('処理に失敗しました', error);
                alert('帳票生成呼び出し処理に失敗しました');
            });
        }
    }
    /**
     * 再申請完了通知を行う
     */
    reApplyNotify(resApplicationId, answerExpectDays, applicationStepId){
        if(resApplicationId){
            fetch(Config.config.apiUrl + "/application/reapplication/complete/notify", {
                method: 'POST',
                body: JSON.stringify({
                    loginId:this.props.viewState.reApplication.loginId,
                    password:this.props.viewState.reApplication.password,
                    applicationId:resApplicationId,
                    answerExpectDays:answerExpectDays,
                    applicationStepId:applicationStepId
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
                if (res.status == 200) {
                    // 再申請完了画面を開く
                    const loginId = this.props.viewState.reApplication.loginId;
                    const password = this.props.viewState.reApplication.password;
                    this.showApplicationCompletedView(loginId, password, answerExpectDays, applicationStepId);
                }else{
                    document.getElementById("customloader").style.display = "none";
                    alert('照合情報通知処理に失敗しました');
                }
            }).catch(error => {
                document.getElementById("customloader").style.display = "none";
                console.error('通信処理に失敗しました', error);
                alert('通信処理に失敗しました');
            });
        }
    }

    /**
     * 申請登録完了通知処理
     * @param {number} resApplicationId 申請ID
     * @param {number} notifyCount 呼び出し回数
     * @param {number} answerExpectDays 回答予定日数
     */
    notify(resApplicationId,notifyCount,answerExpectDays, applicationStepId){
        if(resApplicationId && notifyCount ===1){
            fetch(Config.config.apiUrl + "/application/notify/collation", {
                method: 'POST',
                body: JSON.stringify({
                    applicationId:resApplicationId,
                    answerExpectDays:answerExpectDays,
                    applicationStepId:applicationStepId
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
                if (res.loginId) {
                    const loginId = res.loginId;
                    const password = res.password;
                    this.showApplicationCompletedView(loginId,password,answerExpectDays,applicationStepId);
                }else{
                    // 仮申請状態のデータの消去
                    this.execRollBack(2, resApplicationId, 1);
                }
            }).catch(error => {
                console.error('通信処理に失敗しました', error);
                // 仮申請状態のデータの消去
                this.execRollBack(2, resApplicationId, 1);
            });
        }
    }

    /**
     * 完了画面を開く
     * @param {string} loginId ログインID
     * @param {string} password パスワード
     * @param {number} answerExpectDays 回答予定日数
     * @param {number} applicationStepId 申請段階ID
     */
    showApplicationCompletedView(loginId,password,answerExpectDays,applicationStepId){
        fetch(Config.config.apiUrl + "/label/1001/" + applicationStepId)
        .then(res => res.json())
        .then(res => {
            if(res.status === 401){
                alert("認証情報が無効です。ページの再読み込みを行います。");
                window.location.reload();
                return null;
            }
            if (Object.keys(res).length > 0) {
                if(Config.QuestionaryActived.ApplyCompletedView == "true"){
                    this.props.viewState.setCustomMessage(res[0]?.labels?.title,res[0]?.labels?.content + '<BR>' + res[0]?.labels?.questionaryContent)
                }else{
                    this.props.viewState.setCustomMessage(res[0]?.labels?.title,res[0]?.labels?.content)
                }
                if(this.props.viewState.folderName){
                    this.props.viewState.updateProgressListForCertification(this.props.viewState.folderName,loginId,password);
                }
                document.getElementById("customloader").style.display = "none";
                this.props.viewState.showApplicationCompletedView(loginId,password,answerExpectDays);
            }else{
                document.getElementById("customloader").style.display = "none";
                alert("labelの取得に失敗しました。");
            }
        }).catch(error => {
            document.getElementById("customloader").style.display = "none";
            console.error('通信処理に失敗しました', error);
            alert('通信処理に失敗しました');
        });
    }

    /**
     * 申請登録前帳票出力処理
     */
    generalConditionOutput(){
        if(this.props.viewState.isReApply){
            // 再申請の場合、リセット処理を行う
            document.getElementById("customloader").style.display = "block";
            let applicationId = this.props.viewState.reApplication.applicationId
            fetch(Config.config.apiUrl + "/application/reapplication/reset/" + applicationId)
            .then(res => res.json())
            .then(res => {
                if(res.status === 401){
                    alert("認証情報が無効です。ページの再読み込みを行います。");
                    window.location.reload();
                    return null;
                }
                if(res.status === 200){
                    this.reApply();
                }else{
                    document.getElementById("customloader").style.display = "none";
                    alert("再申請情報リセット処理に失敗しました。");
                }
            }).catch(error => {
                document.getElementById("customloader").style.display = "none";
                console.error('通信処理に失敗しました', error);
                alert('通信処理に失敗しました');
            });
            
        }else{
            // 新規申請の場合
            this.register();
        }
        
    }


    /**
     * 申請登録失敗時のロールバック対応
     * @param {*} type ロールバックタイミング（1：ファイルアップロード、2；照合通知）
     * @param {*} applicationId 
     */
    execRollBack(type, applicationId, rollbackCount){
        if(rollbackCount === 1){
            fetch(Config.config.apiUrl + "/application/application/rollback/" + applicationId)
                .then(res => res.json())
                .then(res => {
                    if(res.status === 401){
                        alert("認証情報が無効です。ページの再読み込みを行います。");
                        window.location.reload();
                        return null;
                    }
                    
                    if(document.getElementById("customloader")){
                        document.getElementById("customloader").style.display = "none";
                    }
                    if(type == 1 ){
                        alert("申請ファイルアップロード処理に失敗しました。");
                    }
                    if(type == 2 ){
                        alert("照合情報通知処理に失敗しました。");
                    }
                }).catch(error => {
                    document.getElementById("customloader").style.display = "none";
                    console.error('通信処理に失敗しました', error);
                    alert('通信処理に失敗しました');
                });
        }
    }


    /**
     * 再申請の場合、概要診断レポートが出力必要か判定
     */
    isOutputReport(){
        const applicationStepId = this.props.viewState.reapplyApplicationStepId;
        const preApplicationStepId = this.props.viewState.preReapplyApplicationStepId;

        // 次の段階を申請する場合、概況診断レポートが出力必要
        if(applicationStepId !== preApplicationStepId){
            return true;
        }else{
            // 許可判定⇒許可判定の再申請である場合、概況診断レポートが出力不要
            if(applicationStepId ==3){
                return false;
            }else{
                // 事前相談、または、事前協議の場合、今回選択された申請区分が前回と変更されるか判定
                let isChanged = false;
                 // 申請区分
                let newApplicationCategory = Object.values(this.props.viewState.checkedApplicationCategory);
                newApplicationCategory = newApplicationCategory.filter(Boolean);
                let oldApplicationCategory = Object.values(this.props.viewState.reApplication.applicationCategories);
                oldApplicationCategory = oldApplicationCategory.filter(Boolean);

                // 
                if(Object.keys(newApplicationCategory).length != Object.keys(oldApplicationCategory).length){
                    isChanged = true;
                }else{
                    Object.keys(newApplicationCategory).map(key1 => {
    
                        let screenId = newApplicationCategory[key1].screenId;
                        if(oldApplicationCategory.some(view => view.screenId == screenId)){

                            let oldViewIndex = oldApplicationCategory.findIndex(view => view.screenId== screenId);
                            let oldCategorys = oldApplicationCategory[oldViewIndex]?.applicationCategory;
                            if(Object.keys(newApplicationCategory[key1].applicationCategory).length != Object.keys(oldCategorys).length){
                                isChanged = true;
                            }else{
                                Object.keys(newApplicationCategory[key1].applicationCategory).map(key2 => {
                                    let categoryId = newApplicationCategory[key1].applicationCategory[key2].id;
                                    if(!oldCategorys.some(categorys => categorys.id == categoryId)){
                                        isChanged = true;
                                    }
                                })
                            }
                        }else{
                            isChanged = true;
                        }
                    })
                }

                if(!isChanged){
                    // 事前協議⇒事前協議　受付版情報が0の場合、概況診断レポート出力要
                    if(applicationStepId == 2){

                        // 受付版情報
                        const acceptVersionInformation =  this.props.viewState.reApplication.acceptVersionInformation;

                        if(acceptVersionInformation == 0 ){
                            isChanged = true;
                        }
                    }
                     
                }

                return isChanged;
            }
        }
    }

    /**
     * 申請対象レイヤのクリア処理
     */
    clearLayer() {
        try{
            const items = this.state.terria.workbench.items;
            for (const aItem of items) {
                if (aItem.uniqueId === Config.layer.lotnumberSearchLayerNameForApplicationTarget) {
                    this.state.terria.workbench.remove(aItem);
                    aItem.loadMapItems();
                }
            }
        }catch(e){
            console.error('処理に失敗しました', error);
        }
    }

    /**
     * 申請段階の名称取得
     * @param {Object} applicationType 申請種類
     * @returns 申請段階の名称
     */
    getApplicationStepName(applicationType){
        const applicationSteps = applicationType.applicationSteps;
        let activeApplicationStepId = this.props.viewState.checkedApplicationStepId;
        if(this.props.viewState.isReApply){
            activeApplicationStepId = this.props.viewState.reapplyApplicationStepId;
        }
        const res = applicationSteps.filter( applicationStep =>  applicationStep.applicationStepId == activeApplicationStepId);

        if(res.length > 0 ){
            return res[0].applicationStepName;
        }else{
            return "";
        } 
    }

    /**
     * 申請追加情報の表示テキストを取得
     * @param {*} applicantnAddInformation 
     */
    getDisplayAddInformationText(applicantnAddInformation){

        let text = "";
        let itemType = applicantnAddInformation.itemType;
        
        switch (itemType) {
            case '0': // 0:1行のみの入力欄で表示
            case '1'://1:複数行の入力欄で表示
                text = applicantnAddInformation.value;
                break;
            case '2'://2:日付（カレンダー）
                let dateText = applicantnAddInformation.value;
                if(dateText){
                    // yyyy/MM/dd に変換
                    text = dateText.replaceAll("-","/");
                }else{
                    text = "";
                }
                break;
            case '3'://3:数値
                text = applicantnAddInformation.value;
                break;
            case '4'://4:ドロップダウン単一選択
            case '5'://5:ドロップダウン複数選択
                text = applicantnAddInformation.itemOptions.filter(option => {return option.checked == true}).map(function (value) { return value.content }).join(",");
                break;
            default:
                text = applicantnAddInformation.value;
        }

        return text;
    }

    /**
     * 事前協議の再申請かをチェックする
     */
    isResubmissionOfPriorConsultation(){
        if(this.props.viewState.isReApply
            && this.props.viewState.reapplyApplicationStepId == 2
            && this.props.viewState.reapplyApplicationStepId == this.props.viewState.preReapplyApplicationStepId 
            && this.props.viewState.reApplication?.acceptVersionInformation != 0
        ){
            return true;
        }else{
            return false;
        }
    }

    render() {
        const explanation = "以下の内容で申請を行います。よろしいでしょうか？";
        let checkedApplicationCategory = this.props.viewState.checkedApplicationCategory;
        let applicantInformation = this.props.viewState.applicantInformation;
        let applicationFile = {};
        let applicantnAddInformation = {};
        if(this.props.viewState.isReApply){
            applicationFile = this.props.viewState.reApplicationFile;
            applicantnAddInformation = this.props.viewState.applicantnAddInformation;
            applicantInformation = this.props.viewState.reApplication.applicantInformations;
            const applicationStepId = this.props.viewState.reapplyApplicationStepId;
            if(applicationStepId == 3){
                checkedApplicationCategory = this.props.viewState.reApplication.applicationCategories;
            }
        }else{
            applicationFile = this.props.viewState.applicationFile;
        }
        let isReApply = this.props.viewState.isReApply;
        let applicationPlaceResult = {};
        let applicationPlace = this.props.viewState.applicationPlace;
        if(this.props.viewState.isReApply){
            // 再申請の場合
            const lotNumber = this.props.viewState.reApplication.lotNumbers;
            applicationPlace = Object.assign({},lotNumber);
        }
        if (!isReApply) {
            // 初回申請
            Object.keys(applicationPlace).map(key => {
                if(!applicationPlaceResult[applicationPlace[key].districtName]){
                    applicationPlaceResult[applicationPlace[key].districtName] = new Array();
                }
                if(applicationPlace[key].fullFlag == "1"){
                    applicationPlaceResult[applicationPlace[key].districtName].push(applicationPlace[key].chiban + "の一部");
                }else{
                applicationPlaceResult[applicationPlace[key].districtName].push(applicationPlace[key].chiban);
                }
            });
        }

        let applicationType = this.props.viewState.selectedApplicationType;
        if(this.props.viewState.isReApply){
            applicationType = this.props.viewState.reApplication.applicationType;
        }
        const height = this.state.height;
        return (
            <>
                <div className={CustomStyle.div_area}>
                    <Box id="applicationFrame" css={`display:block`}>
                        <div id="customloader" className={CustomStyle.customloaderParent}>
                            <div className={CustomStyle.customloader}>Loading...</div>
                        </div>
                    
                        <nav className={CustomStyle.custom_nuv} id="applicationFrameDrag">
                            申請確認
                        </nav>
                        <Box
                            centered
                            displayInlineBlock
                            className={CustomStyle.custom_content}
                        >
                            <p className={CustomStyle.explanation}>{explanation}</p>
                            <Spacing bottom={1} />
                            <div className={CustomStyle.scrollContainer} id="AapplicationConfirmArea" style={{height:height + "px"}}>
                                <div className={CustomStyle.box}>
                                    <div className={CustomStyle.item} style={{ width: 60 + "%" }}>
                                            ■申請種類
                                    </div>
                                    <div className={CustomStyle.item} style={{ width: 40 + "%" }}>
                                        {applicationType["applicationTypeName"]}
                                    </div>
                                </div>
                                <div className={CustomStyle.box}>
                                    <div className={CustomStyle.item} style={{ width: 60 + "%" }}>
                                            ■申請種別
                                    </div>
                                    <div className={CustomStyle.item} style={{ width: 40 + "%" }}>
                                        <>{this.getApplicationStepName(applicationType)}</>
                                    </div>
                                </div>
                                <p>■申請者情報</p>
                                {Object.keys(applicantInformation).map(key => (
                                    <div className={CustomStyle.box}>
                                        <div className={CustomStyle.item} style={{ width: 60 + "%" }}>
                                            ・{applicantInformation[key].name}
                                        </div>
                                        <div className={CustomStyle.item} style={{ width: 40 + "%" }}>
                                            {applicantInformation[key].value}
                                        </div>
                                    </div>
                                ))}
                                <p>■連絡先情報</p>
                                {Object.keys(applicantInformation).map(key => (
                                    applicantInformation[key].contactAddressFlag ? (
                                        <div className={CustomStyle.box}>
                                            <div className={CustomStyle.item} style={{ width: 60 + "%" }}>
                                                ・{applicantInformation[key].name}
                                            </div>
                                            <div className={CustomStyle.item} style={{ width: 40 + "%" }}>
                                                {applicantInformation[key].applicantSameFlag?applicantInformation[key].value:applicantInformation[key].contactValue}
                                            </div>
                                        </div>
                                    ):(null)
                                ))}
                                {applicantnAddInformation && Object.keys(applicantnAddInformation).length > 0 && (
                                    <>
                                        <p>■申請追加情報</p>
                                        {Object.keys(applicantnAddInformation).map(key => (
                                            <div className={CustomStyle.box} key={key}>
                                                <div className={CustomStyle.item} style={{ width: 60 + "%" }}>
                                                    ・{applicantnAddInformation[key]?.name}
                                                </div>
                                                <div className={CustomStyle.item} style={{ width: 40 + "%" }}>
                                                    {this.getDisplayAddInformationText(applicantnAddInformation[key])}
                                                </div>
                                            </div>
                                        ))}
                                    </>
                                )}
                                {Object.keys(checkedApplicationCategory).map(key => (
                                    <div className={CustomStyle.box}>
                                        <div className={CustomStyle.item} style={{ width: 60 + "%" }}>
                                            ■{checkedApplicationCategory[key]["title"]}
                                        </div>
                                        <div className={CustomStyle.item} style={{ width: 40 + "%" }}>
                                            {checkedApplicationCategory[key]["applicationCategory"]?.map(function (value) { return value.content }).filter(content => {return content !== null}).join(",")}
                                        </div>
                                    </div>
                                ))}
                                <div className={CustomStyle.box}>
                                    <div className={CustomStyle.item} style={{ width: 60 + "%" }}>
                                        ■申請地番
                                    </div>
                                    <div className={CustomStyle.item} style={{ width: 40 + "%" }}>
                                        {isReApply == false && Object.keys(applicationPlaceResult).map(key => (
                                            <p>{key} {applicationPlaceResult[key].map(chiban => { return chiban }).filter(chiban => {return chiban !== null}).join(",")}</p>
                                        ))}
                                        {isReApply == true &&
                                            Object.keys(applicationPlace).map(key =>(
                                                <div>{applicationPlace[key].lot_numbers}</div>
                                            ))
                                        }
                                    </div>
                                </div>
                                <Spacing bottom={3} />
                                <table className={CustomStyle.selection_table}>
                                    <thead>
                                        <tr className={CustomStyle.table_header}>
                                            <th style={{ width: "100px" }}>対象</th>
                                            <th style={{ width: "60px" }}>拡張子</th>
                                            <th style={{ width: "100px" }}>ファイル名</th>
                                            {this.isResubmissionOfPriorConsultation() && (
                                                <th className="no-sort" style={{ width: "150px" }}>指示元担当課</th>
                                            )}
                                            {this.isResubmissionOfPriorConsultation() && (
                                                <th className="no-sort" style={{ width: "200px" }}>修正内容</th>
                                            )}
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {Object.keys(applicationFile).map(key => (
                                            applicationFile[key].applicationFileId !== "9999" && (
                                            <tr>
                                                <td>
                                                    {applicationFile[key].applicationFileName}
                                                </td>
                                                <td>{applicationFile[key].extension}</td>
                                                <td>{applicationFile[key] && (
                                                    applicationFile[key]["uploadFileFormList"].map(uploadApplicationFile => { return uploadApplicationFile.uploadFileName }).filter(uploadFileName => {return uploadFileName !== null}).join(",")
                                                )}</td>
                                                {this.isResubmissionOfPriorConsultation() && (
                                                    <td style={{display:"flex",justifyContent:"center",alignItems:"center"}}>
                                                        {applicationFile[key].directionDepartment}
                                                    </td>
                                                )}
                                                {this.isResubmissionOfPriorConsultation() && (
                                                    <td>
                                                        {applicationFile[key].reviseContent}
                                                    </td>
                                                )}
                                            </tr>
                                            )
                                        ))}
                                    </tbody>
                                </table>
                            </div>
                        </Box>
                    </Box >
                </div>
                <div className={CustomStyle.div_area}>
                    <Box padded paddedHorizontally={3} paddedVertically={2} css={`display:block; text-align:center `}>
                        <button
                            className={`${CustomStyle.btn_baise_style}`}
                            style={{width:"45%"}}
                            onClick={e => {
                                this.generalConditionOutput();
                            }}
                        >
                            <span>申請</span>
                        </button>

                        <button
                            className={`${CustomStyle.btn_baise_style} ${CustomStyle.btn_gry}`}
                            style={{width:"45%"}}
                            onClick={e => {
                                this.clearLayer();
                                this.props.viewState.backToUploadApplicationInformationView();
                            }}
                        >
                            <span>戻る</span>
                        </button>
                    </Box>
                </div>
            </>
        );
    }
}
export default withTranslation()(withTheme(ConfirmApplicationDetails));