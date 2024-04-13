import { observer } from "mobx-react";
import PropTypes from "prop-types";
import React from "react";
import { withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import Box from "../../../../Styled/Box";
import CustomStyle from "./scss/answer-content.scss";
import AnswerContentList from "./AnswerContentList";
import Config from "../../../../../customconfig.json";

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
        t: PropTypes.func.isRequired
    }
    constructor(props) {
        super(props);
        this.state = {
            viewState: props.viewState,
            terria: props.terria,
            //申請者情報
            applicantInformation: props.viewState.answerContent.applicantInformations,
            //申請区分
            checkedApplicationCategory: props.viewState.answerContent.applicationCategories,
            //申請ファイル
            applicationFile: props.viewState.answerContent.applicationFiles,
            //申請地番
            lotNumber: props.viewState.answerContent.lotNumbers,
            //申請状態
            status: props.viewState.answerContent.status,
            statusCode: props.viewState.answerContent.statusCode,
            //行政からの回答一覧
            answers: props.viewState.answerContent.answers,
            //申請ID
            applicationId: props.viewState.answerContent.applicationId,
            intervalID:null
        };
    }

    /**
     * 初期処理
     */
    componentDidMount(){
        // 申請回答確認情報取得
        this.searchAnswerInfo();
        let intervalID = setInterval(() => {
            if(this.props.viewState.showConfirmAnswerInformationView){
                this.searchAnswerInfo();
            }else{
             return;
            }
             
         }, 30000);
         this.setState({intervalID:intervalID});
    }

    /**
     * 再申請ため、申請ファイルアップロード画面に、遷移する
     */
    moveToApplyInformationView(){
    
        // 申請者情報
        const applicantInformation = this.state.applicantInformation;
        // 申請ファイル情報
        const applicationFile = this.state.applicationFile;
        // 申請ファイル情報
        const checkedApplicationCategory = this.state.checkedApplicationCategory;
        // 申請地番情報
        const lotNumber = this.state.lotNumber;
        
        // 再申請情報取得
        let answerContent = this.props.viewState.answerContent;
        let loginId = answerContent["loginId"];
        let password = answerContent["password"];
        fetch(Config.config.apiUrl + "/application/reappInformation", {
            method: 'POST',
            body: JSON.stringify({
                loginId:loginId,
                password:password,
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
                if (Object.keys(res.applicationFileForm).length > 0) {
                    Object.keys(res.applicationFileForm).map(key => {
                        if (!res.applicationFileForm[key].uploadFileFormList) {
                            res.applicationFileForm[key].uploadFileFormList = [];
                        }
                    })
                    // 再申請情報保存
                    this.props.viewState.setReAppInformation(JSON.parse(JSON.stringify(res)),JSON.parse(JSON.stringify(res.applicationFileForm)));
                    // 申請フォームの申請対象ファイルアップロード画面へ遷移して、再申請を行う
                    this.props.viewState.moveToApplyInformationViewForReapply(applicantInformation,applicationFile,checkedApplicationCategory,lotNumber);
                    let intervalID = this.state.intervalID;
                    clearInterval(intervalID);
                } else {
                    alert("要再申請の申請ファイルは一件もありません。");
                }
            }else{
                alert('再申請情報取得処理に失敗しました');
            }
        }).catch(error => {
            console.error('通信処理に失敗しました', error);
            alert('通信処理に失敗しました');
        });
    }

    /**
     * 申請回答確認情報取得
     */
    searchAnswerInfo(){
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
                this.state.viewState.answerContent = JSON.parse(JSON.stringify(res));
                
                this.setState({
                    //申請者情報
                    applicantInformation: res.applicantInformations,
                    //申請区分
                    checkedApplicationCategory: res.applicationCategories,
                    //申請ファイル
                    applicationFile: res.applicationFiles,
                    //申請地番
                    lotNumber: res.lotNumbers,
                    //申請状態
                    status: res.status,
                    statusCode: res.statusCode,
                    //行政からの回答一覧
                    answers: res.answers,
                    // 申請ID
                    applicationId: res.applicationId
                })

            }else{
                alert('申請・回答内容確認情報取得処理に失敗しました');
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
            if (aItem.uniqueId === Config.layer.lotnumberSearchLayerNameForApplicationTarget) {
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
        let applicationId = this.state.applicationId;
        fetch(Config.config.apiUrl + "/answer/report/" + applicationId)
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

    render() {
        let answers = this.state.answers;
        let statusCode = this.state.statusCode;
        return (
            <>
                <nav className={CustomStyle.custom_nuv} id="AnswerContentDrag">
                    回答内容確認
                </nav>
                <div>
                    <Box padded paddedHorizontally={3} paddedVertically={2} css={`display:block; text-align:center `} >
                        <button
                            className={`${CustomStyle.btn_baise_style} `}
                            style={{width:"30%"}}
                            onClick={e => {
                                this.export();
                            }}
                        >
                            <span>回答レポート出力</span>
                        </button>
                        <button
                            className={`${CustomStyle.btn_baise_style} `}
                            disabled={statusCode == "4" ? false : true}
                            style={{width:"30%"}}
                            onClick={e => {
                                this.moveToApplyInformationView();
                            }}
                        >
                            <span>再申請</span>
                        </button>
                        
                        <button
                            className={`${CustomStyle.btn_baise_style} ${CustomStyle.btn_gry}`}
                            style={{width:"30%"}}
                            onClick={e => {
                                this.back();
                            }}
                        >
                            <span>戻る</span>
                        </button>
                    </Box>
                </div>
                <div className={CustomStyle.scrollContainer}>
                    <div className={CustomStyle.div_area} >
                        <Box id="AnswerContent" css={`display:block`} >
                            
                            <Box
                                centered
                                displayInlineBlock
                                className={CustomStyle.custom_content}
                            >
                                <div>
                                    <Box padded/>
                                    <AnswerContentList 
                                        viewState={this.props.viewState} 
                                        terria={this.props.terria} 
                                        answerContentList ={answers}
                                        intervalID = {this.state.intervalID}
                                    />
                                </div>
                                {/* <div>
                                    <Box paddedRatio={3}/>
                                    <AnswerFileList 
                                        viewState={this.props.viewState} 
                                        terria={this.props.terria} 
                                        answerContentList ={this.props.viewState.answerContent.answers}
                                    />
                                </div> */}
                            </Box>
                        </Box >
                    </div>  
                </div>
            </>
        );
    }
}
export default withTranslation()(withTheme(AnswerContent));