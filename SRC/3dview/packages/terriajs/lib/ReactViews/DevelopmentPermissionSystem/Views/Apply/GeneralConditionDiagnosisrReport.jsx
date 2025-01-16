import { observer } from "mobx-react";
import PropTypes, { object } from "prop-types";
import React from "react";
import { withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import Box from "../../../../Styled/Box";
import Button, { RawButton } from "../../../../Styled/Button";
import CustomStyle from "./scss/general-condition-diagnosisr-report.scss";
import Config from "../../../../../customconfig.json";
import Icon, { StyledIcon } from "../../../../Styled/Icon";
import Spacing from "../../../../Styled/Spacing";
import { left } from "../../../Story/story-panel.scss";

/**
 * 概況診断レポート一覧画面
 */
@observer
class GeneralConditionDiagnosisrReport extends React.Component {
    static displayName = "GeneralConditionDiagnosisrReport";

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
            intervalID:null,
            checkRetryCounter:0,
            simulateProgressListTemp:[],
            warnMessageDisplayedFlag:false,
            processedFolderNameList:[]
        };
        //進捗状況の取得間隔：ミリ秒
        this.intervalTime=15000;
        //再試行判定間隔：(intervalTime＊20)ミリ秒
        //TODO:要調整箇所
        this.retryCheckInterval=12;
    }

    /**
     * 初期処理
     */
    componentDidMount() {
        this.props.viewState.initSetProgressList();
        let intervalID = setInterval(() => {
            let checkRetryCounter = this.state.checkRetryCounter + 1;
            this.setState({
                checkRetryCounter:checkRetryCounter
            },()=>{
                const simulateProgressList = this.props.viewState.simulateProgressList;
                if(simulateProgressList){
                    const folderNameList = [];
                    simulateProgressList.map(simulateProgressMap=>{
                        folderNameList.push({"folderName":simulateProgressMap.folderName});
                    })
                    if(folderNameList && folderNameList.length > 0){
                        fetch(Config.config.apiUrl + "/judgement/progress", {
                            method: 'POST',
                            body: JSON.stringify(folderNameList),
                            headers: new Headers({ 'Content-type': 'application/json' }),
                        })
                        .then((res) => {
                            if(res.status === 401){
                                alert("認証情報が無効です。ページの再読み込みを行います。");
                                window.location.reload();
                                return null;
                            }
                            return res;
                        }) 
                        .then(res => res.json())
                        .then(res => {
                            if(res){
                                //進捗状況の更新処理
                                for(let i=0;i<res.length;i++){
                                    let formattedDate = null;
                                    if(res[i]["fileSize"]){
                                        formattedDate = this.formatDate(new Date());
                                    }
                                    const index = simulateProgressList.findIndex(simulateProgressMap=>res[i].folderName === simulateProgressMap.folderName);
                                    if(index < 0){
                                        continue;
                                    }
                                    this.props.viewState.updateProgressList(
                                        res[i]["folderName"],res[i]["capturedCount"],res[i]["fileSize"],formattedDate,simulateProgressList[index]["retry"]);
                                };
                                //進捗状況の削除処理
                                simulateProgressList.map(simulateProgressMap=>{
                                    const index = res.findIndex(res=>res.folderName === simulateProgressMap.folderName);
                                    if(index < 0){
                                        this.props.viewState.deleteProgressList(simulateProgressMap.folderName);
                                    }
                                });
                                //一定間隔で再試行対象の概況診断レポートを判定する
                                if(checkRetryCounter == this.retryCheckInterval){
                                    const simulateProgressListTemp = this.state.simulateProgressListTemp;
                                    let errorFilenameList = [];
                                    simulateProgressList.map(simulateProgressMap1=>{
                                        const index = simulateProgressListTemp.findIndex(simulateProgressMap2=>simulateProgressMap1.folderName === simulateProgressMap2.folderName);
                                        if(index > -1 && simulateProgressListTemp[index]){
                                            //完了していないかつ、キャプチャ数に変化がない場合再試行フラグをTRUEに
                                            if(!simulateProgressMap1["completeDateTime"] && simulateProgressMap1["capturedCount"] == simulateProgressListTemp[index]["capturedCount"]){
                                                errorFilenameList.push(simulateProgressMap1["fileName"]);
                                                this.props.viewState.updateProgressList(
                                                    simulateProgressMap1["folderName"],simulateProgressMap1["capturedCount"],simulateProgressMap1["fileSize"],simulateProgressMap1["completeDateTime"],true);
                                            }
                                        }
                                    });
                                    this.setState({checkRetryCounter:0,simulateProgressListTemp:JSON.parse(JSON.stringify(simulateProgressList))});
                                    if(errorFilenameList.length > 0 && !this.state.warnMessageDisplayedFlag){
                                        this.props.viewState.setCustomMessage("概況診断レポートの進捗状況の取得に失敗","アクセス集中等の原因によりバックグラウンドでの帳票生成に失敗している可能性があります。その場合、お手数ですが、再度時間を空けて右上の「再試行」ボタンから実行してください。<br>※「再試行」ボタンが表示されていて問題なく進捗の更新が行われている場合、そのままお待ちください。");
                                        this.props.viewState.setShowCustomMessage(true);
                                        this.setState({warnMessageDisplayedFlag:"true"})
                                    }
                                }
                            }
                        }).catch(error => {
                            console.error('処理に失敗しました', error);
                        });
                    }
                }
            })
        }, this.intervalTime);
        this.setState({intervalID:intervalID,simulateProgressListTemp:JSON.parse(JSON.stringify(this.props.viewState.simulateProgressList))});
    }

    componentWillUnmount() {
        if (this.state.intervalID) {
            clearInterval(this.state.intervalID);
        }
    }

    // 日付をフォーマットする関数
    formatDate(date){
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0'); // 月は0から始まるため+1
        const day = String(date.getDate()).padStart(2, '0');
        const hours = String(date.getHours()).padStart(2, '0');
        const minutes = String(date.getMinutes()).padStart(2, '0');
        const seconds = String(date.getSeconds()).padStart(2, '0');
        return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
    }

    /**
     * 概況診断レポートのダウンロード
     * @param {*} folderName 
     * @param {*} fileName 
     * @param {*} requestBody 
     * @param {*} deleteFlag 
     */
    download(folderName,fileName,requestBody,deleteFlag){
        const processedFolderNameList = this.state.processedFolderNameList;
        //ダウンロードor削除が既に実行されている場合は何もしない
        if (processedFolderNameList.includes(folderName)) {
            return;
        }
        processedFolderNameList.push(folderName);
        this.setState({processedFolderNameList:processedFolderNameList},() => {
            let currentRequestBody = {
                folderName: folderName
            };
            //出力件数のログ用に概況診断結果及び申請段階情報をリクエストに含める
            if(!deleteFlag && requestBody && requestBody["generalConditionDiagnosisResults"] && requestBody["generalConditionDiagnosisResults"][0]){
                currentRequestBody["generalConditionDiagnosisResults"] = requestBody["generalConditionDiagnosisResults"];
                let generalConditionDiagnosisResult = requestBody["generalConditionDiagnosisResults"][0];
                if(generalConditionDiagnosisResult){
                    if(generalConditionDiagnosisResult["applicationTypeId"] != undefined && generalConditionDiagnosisResult["applicationTypeId"] != null){
                        currentRequestBody["applicationTypeId"] = generalConditionDiagnosisResult["applicationTypeId"];
                    }
                    if(generalConditionDiagnosisResult["applicationStepId"] != undefined && generalConditionDiagnosisResult["applicationStepId"] != null){
                        currentRequestBody["applicationStepId"] = generalConditionDiagnosisResult["applicationStepId"];
                    }
                }
            }
            fetch(Config.config.apiUrl + "/judgement/report", {
                method: 'POST',
                body: JSON.stringify(currentRequestBody),
                headers: new Headers({ 'Content-type': 'application/json' }),
            })
            .then((res) => {
                if(res.status === 401){
                    alert("認証情報が無効です。ページの再読み込みを行います。");
                    window.location.reload();
                    return null;
                }else{
                    return res.blob();
                }
            })
            .then(blob => {
                //概況診断レポートの出力
                if(!deleteFlag){
                    let anchor = document.createElement("a");
                    anchor.href = window.URL.createObjectURL(blob);
                    anchor.download = fileName;
                    anchor.click();
                    this.props.viewState.deleteProgressList(folderName);
                    // 帳票出力したあとで、アンケート画面を開く
                    this.openQuestionaryView();
                }else{
                    this.props.viewState.deleteProgressList(folderName);
                }
            })
            .catch(error => {
                console.error('処理に失敗しました', error);
            });
        });
    }

    /**
     * アンケート画面を開く
     */
    openQuestionaryView(){
        if(Config.QuestionaryActived.GeneralConditionDiagnosisView == "true"){
            setTimeout(() => {
                // URL
                let url = Config.config.questionnaireUrlForBusiness;
                // ターゲット名
                let target="develop_quessionaire";
                // アンケート画面を開く
                window.open(url,target);
            }, 5000)
        }
    }

    /**
     * リトライ処理
     * @param simulateProgressMap
     */
    retry(simulateProgressMap){
        //概況診断シミュレート実行API
        if(!simulateProgressMap || !simulateProgressMap["requestBody"]) return;
        //概況診断のキャプチャ格納用フォルダを生成
        fetch(Config.config.apiUrl + "/judgement/image/upload/preparation")
        .then(res => res.json())
        .then(res => {
            if(res.status === 401){
                alert("認証情報が無効です。ページの再読み込みを行います。");
                window.location.reload();
                return null;
            }
            if (res.folderName) {
                //概況診断シミュレート生成再実行
                const newSimulateProgressMap = JSON.parse(JSON.stringify(simulateProgressMap));
                newSimulateProgressMap["requestBody"]["folderName"] = res.folderName;
                fetch(Config.config.simulatorApiUrl+"/simulator/execution", {
                    method: 'POST',
                    body: JSON.stringify(newSimulateProgressMap["requestBody"]),
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
                        this.props.viewState.deleteProgressList(newSimulateProgressMap["folderName"]);
                        this.props.viewState.setProgressList(newSimulateProgressMap["requestBody"]);
                        this.setState({checkRetryCounter:0,simulateProgressListTemp:JSON.parse(JSON.stringify(this.props.viewState.simulateProgressList))});
                    }else{
                        //TODO:エラー時は再実行?
                        alert("帳票生成の再実行に失敗しました");
                    }
                }).catch(error => {
                    //TODO:エラー時は再実行?
                    console.error(error);
                    alert("帳票生成の再実行に失敗しました");
                });
            }else{
                alert("一時フォルダの生成に失敗しました");
            }
        }).catch(error => {
            console.error(error);
            alert("一時フォルダの生成に失敗しました");
        });
    }

    render() {
        //モック用にコメントアウト
        const simulateProgressList = this.props.viewState.simulateProgressList;
        return (
            <div className={CustomStyle.popup_content}>
                <div className={CustomStyle.popup_arrow}></div>
                <Box position="absolute" paddedRatio={2} topRight>
                    <RawButton onClick={() => {
                        this.props.viewState.setGeneralConditionDiagnosisrReportShow(false);
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
                {!simulateProgressList || simulateProgressList.length<1 && (
                    <Spacing bottom={3} />
                )}
                {simulateProgressList && simulateProgressList.length>0 && (
                    <Spacing bottom={5} />
                )}
                <Box displayInlineBlock style={{maxHeight:"270px",overflowY:"auto"}}>
                    {simulateProgressList && simulateProgressList.length>0 && simulateProgressList.map(simulateProgressMap => (
                    <Box displayInlineBlock style={{borderBottom:"1px solid #c6c6c6"}}>
                        <Box col12 displayInlineBlock style={{fontSize:".8em",marginTop:"13px"}}>
                            <p>{simulateProgressMap["fileName"]}</p>
                        </Box>
                        <Box>
                            {((simulateProgressMap["capturedCount"]-1)/simulateProgressMap["captureMaxCount"] < 1) && (
                                <div className={CustomStyle.ui_progress_bar}>
                                    <div className={!simulateProgressMap["completeDateTime"]&&simulateProgressMap["retry"]?CustomStyle.ui_progress_warn:CustomStyle.ui_progress} style={{width:simulateProgressMap["capturedCount"]-1<0?0:Math.round((simulateProgressMap["capturedCount"]-1)/simulateProgressMap["captureMaxCount"]*100)+"%"}}>
                                    </div>
                                    <span className={CustomStyle.ui_label}><b class="value">{simulateProgressMap["capturedCount"]-1<0?0:Math.round((simulateProgressMap["capturedCount"]-1)/simulateProgressMap["captureMaxCount"]*100)}%</b></span>
                                </div>
                            )}
                            {((simulateProgressMap["capturedCount"]-1)/simulateProgressMap["captureMaxCount"] >= 1) && (
                                <>
                                    <Box col8 style={{fontSize:".5em"}}>
                                        <p>{simulateProgressMap["fileSize"]} – {simulateProgressMap["completeDateTime"]}</p>
                                    </Box>
                                    <Box col2>
                                        <button onClick={()=>{
                                                //モック用にコメントアウト
                                                this.download(simulateProgressMap["folderName"],simulateProgressMap["fileName"],simulateProgressMap["requestBody"],false)
                                            }} 
                                            className={CustomStyle.icon} title="ダウンロード">
                                            <StyledIcon
                                                styledWidth={"24px"}
                                                fillColor={this.props.theme.textLight}
                                                opacity={"0.5"}
                                                glyph={Icon.GLYPHS.downloadNew}
                                                css={`
                                                cursor:pointer;
                                                fill:#000000;
                                                `}
                                            />
                                        </button>
                                    </Box>
                                    <Box col2>
                                        <button onClick={()=>{
                                                //モック用にコメントアウト
                                                this.download(simulateProgressMap["folderName"],simulateProgressMap["fileName"],simulateProgressMap["requestBody"],true)
                                            }} 
                                                className={CustomStyle.icon} title="削除">
                                        <StyledIcon
                                            styledWidth={"24px"}
                                            fillColor={this.props.theme.textLight}
                                            opacity={"0.5"}
                                            glyph={Icon.GLYPHS.trashcan}
                                            css={`
                                            cursor:pointer;
                                            fill:#000000;
                                            `}
                                        />
                                        </button>
                                    </Box>
                                </>
                            )}
                        </Box>
                        {!simulateProgressMap["completeDateTime"]&&simulateProgressMap["retry"]&&(
                            <div className={CustomStyle.retry_container}>
                                <button className={CustomStyle.retry_button} onClick={()=>{
                                                //モック用にコメントアウト
                                                this.retry(simulateProgressMap);
                                            }}>
                                    <span className={CustomStyle.dli_redo}></span>再試行
                                </button>
                            </div>
                        )}
                    </Box>
                    ))}
                    {!simulateProgressList || simulateProgressList.length<1 && (
                        <Box col12 style={{fontSize:".8em",marginTop:"30px"}}>
                            <p>現在生成中のレポートはありません</p>
                        </Box>
                    )}
                </Box>
            </div>
        );
    }
}
export default withTranslation()(withTheme(GeneralConditionDiagnosisrReport));