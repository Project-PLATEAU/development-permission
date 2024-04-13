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
            let height = h - getRect.getBoundingClientRect().top - 80;
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
        // 申請情報登録を行う
        fetch(Config.config.apiUrl + "/application/register", {
            method: 'POST',
            body: JSON.stringify({
                generalConditionDiagnosisResultForm:generalConditionDiagnosisResult,
                applicationCategories:checkedApplicationCategory,
                lotNumbers: applicationPlace,
                applicantInformationItemForm:applicantInformation,
                folderName:this.props.viewState.folderName

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
                            applicationFile[key]["uploadFileFormList"][fileKey]["applicationFileId"] = applicationFile[key]["applicationFileId"];
                            applicationFile[key]["uploadFileFormList"][fileKey]["versionInformation"] = versionInformation;
                            const formData  = new FormData();
                            for(const name in applicationFile[key]["uploadFileFormList"][fileKey]) {
                                formData.append(name, applicationFile[key]["uploadFileFormList"][fileKey][name]);
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
                                    alert('アップロードに失敗しました');
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
                                    this.notify(resApplicationId,notifyCount,answerExpectDays);               
                                }
                            }).catch(error => {
                                document.getElementById("customloader").style.display = "none";
                                console.error('処理に失敗しました', error);
                                alert('処理に失敗しました');
                            });
                        });
                    });
                    if(fileCount === 0){
                        notifyCount = notifyCount + 1;
                        this.notify(resApplicationId,notifyCount,answerExpectDays);  
                    }
                }else{
                    notifyCount = notifyCount + 1;
                    this.notify(resApplicationId,notifyCount,answerExpectDays); 
                }
            }else{
                document.getElementById("customloader").style.display = "none";
                alert("申請の登録処理に失敗しました");
            }
        }).catch(error => {
            document.getElementById("customloader").style.display = "none";
            console.error('処理に失敗しました', error);
            alert('処理に失敗しました');
        });
    }

    /**
     * 再申請情報を登録する
     */
    reApply(){
        document.getElementById("applicationFrame").scrollTop = 0;
        document.getElementById("customloader").style.display = "block";

        // 再申請用の申請ファイルをアップロードする
        let reApplication = this.props.viewState.reApplication;
        let applicationId = reApplication["applicationId"];
        // 申請情報の版情報
        let versionInformation = reApplication["versionInformation"];
        let fileCount = 0;
        let reApplicationFile = this.props.viewState.reApplicationFile;
        if(Object.keys(reApplicationFile).length > 0){
            Object.keys(reApplicationFile).map(key => {
                let applicationFileId = reApplicationFile[key]["applicationFileId"];
                
                Object.keys(reApplicationFile[key]["uploadFileFormList"]).map(fileKey => {
                    reApplicationFile[key]["uploadFileFormList"][fileKey]["applicationId"] = applicationId;
                    reApplicationFile[key]["uploadFileFormList"][fileKey]["applicationFileId"] = applicationFileId;
                    reApplicationFile[key]["uploadFileFormList"][fileKey]["versionInformation"] = versionInformation + 1;
                    const formData  = new FormData();
                    for(const name in reApplicationFile[key]["uploadFileFormList"][fileKey]) {
                        formData.append(name, reApplicationFile[key]["uploadFileFormList"][fileKey][name]);
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
                        reApplicationFile[key]["uploadFileFormList"][fileKey]["status"] = res.status;
                        if(res.status !== 201){
                            alert('アップロードに失敗しました');
                        }
                        let completeFlg = true;
                        Object.keys(reApplicationFile).map(key => { 
                            Object.keys(reApplicationFile[key]["uploadFileFormList"]).map(fileKey => {
                                if(!reApplicationFile[key]["uploadFileFormList"][fileKey]["status"]){
                                    completeFlg = false;
                                }
                            })
                        })
                        if(completeFlg){
                            this.updateApplication();               
                        }
                    }).catch(error => {
                        document.getElementById("customloader").style.display = "none";
                        console.error('処理に失敗しました', error);
                        alert('処理に失敗しました');
                    });
                });
            });
            if(fileCount === 0){
                this.updateApplication();      
            }
        }
    }

    /**
     * 再申請の申請ファイルをアップロード完了した後、
     * 申請ステータス更新と完了通知を行う。
     */
    updateApplication(){
        let reApplication = this.props.viewState.reApplication;
        let reApplicationWithoutFile = reApplication["applicationFileForm"];
        let applicationId = reApplication["applicationId"];
        let loginId = reApplication["loginId"];
        let password = reApplication["password"];
        fetch(Config.config.apiUrl + "/application/reapplication", {
            method: 'POST',
            body: JSON.stringify({
                applicationId: applicationId,
                loginId: loginId,
                password: password,
                applicationFileForm: reApplicationWithoutFile
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
            // 回答予定日数
            if (res.answerExpectDays) {
                this.showApplicationCompletedView(loginId,password,res.answerExpectDays);
            }else{
                document.getElementById("customloader").style.display = "none";
                alert("再申請の登録処理に失敗しました");
            }
        }).catch(error => {
            document.getElementById("customloader").style.display = "none";
            console.error('処理に失敗しました', error);
            alert('処理に失敗しました');
        });
    }

    /**
     * 申請登録完了通知処理
     * @param {number} resApplicationId 申請ID
     * @param {number} notifyCount 呼び出し回数
     * @param {number} answerExpectDays 回答予定日数
     */
    notify(resApplicationId,notifyCount,answerExpectDays){
        if(resApplicationId && notifyCount ===1){
            fetch(Config.config.apiUrl + "/application/notify/collation", {
                method: 'POST',
                body: JSON.stringify({
                    applicationId:resApplicationId,
                    answerExpectDays:answerExpectDays
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
                    this.showApplicationCompletedView(loginId,password,answerExpectDays);
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
     * 完了画面を開く
     * @param {string} loginId ログインID
     * @param {string} password パスワード
     * @param {number} answerExpectDays 回答予定日数
     */
    showApplicationCompletedView(loginId,password,answerExpectDays){
        fetch(Config.config.apiUrl + "/label/1001")
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
            // 再申請の場合
            this.reApply();
        }else{
            // 新規申請の場合
            if(document.getElementById("generalConditionOutputBtn")){
                document.getElementById("generalConditionOutputBtn").click();
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

    render() {
        const explanation = "以下の内容で申請を行います。よろしいでしょうか？";
        const checkedApplicationCategory = this.props.viewState.checkedApplicationCategory;
        const applicantInformation = this.props.viewState.applicantInformation;
        let applicationFile = {};
        if(this.props.viewState.isReApply){
            applicationFile = this.props.viewState.reApplicationFile;
        }else{
            applicationFile = this.props.viewState.applicationFile;
        }
        
        const applicationPlace = this.props.viewState.applicationPlace;
        let applicationPlaceResult = {};
        Object.keys(applicationPlace).map(key => {
            if(!applicationPlaceResult[applicationPlace[key].districtName]){
                applicationPlaceResult[applicationPlace[key].districtName] = new Array();
            }
            applicationPlaceResult[applicationPlace[key].districtName].push(applicationPlace[key].chiban);
        });

        const height = this.state.height;
        return (
            <>
                <div className={CustomGeneralStyle.loadingBg} id="loadingBg"></div>
                <div className={CustomGeneralStyle.loading} id="loading">
                    <p style={{ textAlign: "center" }}>申請処理中です。画面はこのままでお待ちください。</p>
                    <p style={{ textAlign: "center" }}>※バックグラウンド動作の場合正常にキャプチャの切替が行われませんのでご注意ください。</p>
                    <p style={{ textAlign: "center" }}>画面キャプチャ取得中 <span id="numberOfSheets">0/0</span></p>
                    <div className={CustomGeneralStyle.myProgress}>
                        <div className={CustomGeneralStyle.myBar} id="myBar"></div>
                    </div>
                    <p style={{ textAlign: "center" }}>申請帳票作成中 <span id="wholePercent">0</span>%</p>
                </div>
                <If condition = {!this.props.viewState.isReApply}>
                    <div style={{ display: "none" }}>
                        <GeneralConditionDiagnosis terria={this.props.terria} viewState={this.props.viewState} />
                    </div>
                </If>
                <div style={{ display: "none" }} id="confirmApplicationDetailsRegisterButton" 
                    onClick={e => { this.register(); }}>
                </div>
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
                                        {Object.keys(applicationPlaceResult).map(key => (
                                            <p>{key} {applicationPlaceResult[key].map(chiban => { return chiban }).filter(chiban => {return chiban !== null}).join(",")}</p>
                                        ))}
                                    </div>
                                </div>
                                <Spacing bottom={3} />
                                <table className={CustomStyle.selection_table}>
                                    <thead>
                                        <tr className={CustomStyle.table_header}>
                                            <th>対象</th>
                                            <th style={{ width: 50 + "px"}}>拡張子</th>
                                            <th style={{ width: 280 + "px"}}>ファイル名</th>
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