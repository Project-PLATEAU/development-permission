import { observer } from "mobx-react";
import PropTypes from "prop-types";
import React from "react";
import { withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import Spacing from "../../../../Styled/Spacing";
import Box from "../../../../Styled/Box";
import CustomStyle from "./scss/file-list.scss";
import Config from "../../../../../customconfig.json";
import _isEqual from 'lodash/isEqual';
import Icon, { StyledIcon } from "../../../../Styled/Icon";

/**
 * 【R6】通知ファイル一覧コンポーネント
 */
@observer
class NotificationFileList extends React.Component {
    static displayName = "NotificationFileList";
    static propTypes = {
        terria: PropTypes.object.isRequired,
        viewState: PropTypes.object.isRequired,
        theme: PropTypes.object,
        t: PropTypes.func.isRequired,
        notificationFiles: PropTypes.array,
        notifiedAnswerCount: PropTypes.number,
        applicationStepId : PropTypes.number.isRequired,
    }
    constructor(props) {
        super(props);
        this.state = {
            viewState: props.viewState,
            terria: props.terria,
            //通知ファイルリスト
            notificationFiles: props.notificationFiles,
            //通知済み回答の件数
            notifiedAnswerCount: props.notifiedAnswerCount ? props.notifiedAnswerCount : 0,
            applicationStepId: props.applicationStepId,
        };
    }

    /**
     * 初期処理
     */
    componentDidMount() {}

    /**
     * props更新時
     */
    componentDidUpdate(prevProps) {
        if (!_isEqual(this.props.notificationFiles, prevProps.notificationFiles)) {
            this.setState({notificationFiles:this.props.notificationFiles});
        }
        if (!_isEqual(this.props.notifiedAnswerCount, prevProps.notifiedAnswerCount)) {
            this.setState({notifiedAnswerCount:this.props.notifiedAnswerCount});
        }
        if (!_isEqual(this.props.applicationStepId, prevProps.applicationStepId)) {
            this.setState({applicationStepId:this.props.applicationStepId});
        }
    }


    /**
     * 回答レポート出力
     */
    outputAnswerReport(){
        let applicationId = this.props.viewState.answerContent.applicationId;
        let applicationStepId =  this.props.applicationStepId;
        fetch(Config.config.apiUrl + "/answer/report/" + applicationId + "/" + applicationStepId)
        .then((res) => {
            if(res.status === 401){
                alert("認証情報が無効です。ページの再読み込みを行います。");
                window.location.reload();
            }else{
                if(res.status === 200){
                    return res.blob();
                }
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
     * ファイルダウンロード
     * @param {object} file 対象ファイル情報
     */
    output(file) {
        //ダウンロード時に認証が必要ため、申請ID及び照合IDとパスワードをセット
        if(!this.props.terria.authorityJudgment() && file){
            file.applicationId = this.props.viewState.answerContent.applicationId;
            file.loginId = this.props.viewState.answerContent.loginId;
            file.password = this.props.viewState.answerContent.password;
        }
        // APIへのリクエスト
        fetch(Config.config.apiUrl + "/answer/ledger/file/download", {
            method: 'POST',
            body: JSON.stringify(file),
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
            let anchor = document.createElement("a");
            anchor.href = window.URL.createObjectURL(blob);
            anchor.download = file.uploadFileName;
            anchor.click();
        })
        .catch(error => {
            console.error('処理に失敗しました', error);
            alert('処理に失敗しました');
        });
    }

    /**
     * 発行様式の通知処理
     * @param {*} request 
     */
    notification(request){
        // APIへのリクエスト
        fetch(Config.config.apiUrl + "/ledger/notify", {
            method: 'POST',
            body: JSON.stringify(request),
            headers: new Headers({ 'Content-type': 'application/json' }),
        })
        .then((res) => {
            if(res.status === 401){
                alert("認証情報が無効です。ページの再読み込みを行います。");
                window.location.reload();
                return null;
            }else{
                this.props.viewState.setCustomMessage("発行様式の通知","事業者へ"+request.ledgerName+"の通知が完了しました");
                this.props.viewState.setShowCustomMessage(true);
                this.props.viewState.refreshConfirmApplicationDetails();
            }
        })
        .catch(error => {
            console.error('処理に失敗しました', error);
            alert('処理に失敗しました');
        });
    }

    render() {
        const notificationFiles = this.state.notificationFiles;
        const isAdmin = this.props.terria.authorityJudgment();
        const notifiedAnswerCount = this.state.notifiedAnswerCount;
        const applicationStepId = this.state.applicationStepId;
        return (
            <Box
                centered
                displayInlineBlock
                className={CustomStyle.custom_content}
            >
                <h2 className={CustomStyle.title}>{"発行様式"}</h2>
                <Spacing bottom={1} />
                <div className={CustomStyle.scroll_container} id="NotificationFileListTable" style={{height: "18vh", minHeight:"200px"}}>    
                    <table className={CustomStyle.selection_table}>
                        <thead>
                            <tr className={CustomStyle.table_header}>
                                <th className="no-sort" style={{ width: "15%"}}>ファイル</th>
                                <th style={{ width: "25%"}}>発行様式</th>
                                <th className="no-sort" style={{ width: "10%"}}>通知</th>
                                <th className="no-sort" style={{ width: "15%"}}></th>
                                <th className="no-sort" style={{ width: "20%"}}>受領日時</th>
                                <th className="no-sort" style={{ width: "15%"}}></th>
                            </tr>
                        </thead>
                        <tbody>
                            {
                                //事業者の場合、回答レポートが出力可能
                                // ■2024/09/03:事前相談以外の場合表示されないようにするため、「pplicationStepId == 1」を追加
                            }
                            {!isAdmin && notifiedAnswerCount && notifiedAnswerCount > 0 && applicationStepId == 1 ? (
                                <tr>
                                    <td>
                                        <button className={CustomStyle.download_button} 
                                            onClick={e => {
                                                this.outputAnswerReport();
                                            }}
                                        >
                                            <span>ダウンロード</span>
                                        </button>
                                    </td>
                                    <td>回答レポート</td>
                                    <td>
                                        <div className={CustomStyle.ellipse}>
                                            <StyledIcon 
                                                glyph={Icon.GLYPHS.checked}
                                                styledWidth={"20px"}
                                                styledHeight={"20px"}
                                                light
                                            />
                                        </div>
                                    </td>
                                    <td></td>
                                    <td></td>
                                    <td></td>
                                </tr>
                            ):null}
                            {
                                //:通知ファイル
                            }
                            {notificationFiles ? Object.keys(notificationFiles).map(index => (

                                <tr>
                                    <td>
                                        <button className={CustomStyle.download_button} 
                                            onClick={e => {
                                                this.output(notificationFiles[index]);
                                            }}
                                        >
                                            <span>ダウンロード</span>
                                        </button>
                                    </td>
                                    <td>{notificationFiles[index].uploadFileName}</td>
                                    {/** 通知済みフラグのチェック処理追加 */}
                                    <td>
                                        {notificationFiles[index].notifyFlag && (
                                        <div className={CustomStyle.ellipse}>
                                            <StyledIcon 
                                                glyph={Icon.GLYPHS.checked}
                                                styledWidth={"20px"}
                                                styledHeight={"20px"}
                                                light
                                            />
                                        </div>
                                        )}
                                    </td>
                                    <td>
                                        {notificationFiles[index].uploadable && isAdmin && (
                                            <button className={CustomStyle.download_button} 
                                                onClick={e => {
                                                    this.props.viewState.setIssuanceLedgerForm(JSON.parse(JSON.stringify(notificationFiles[index])));
                                                    this.props.viewState.changeIssuanceFileUploadModalShow();
                                                }}
                                            >
                                                <span>アップロード</span>
                                            </button>
                                        )}
                                    </td>
                                    <td>{notificationFiles[index].receiptDatetime}</td>
                                    <td>
                                        {notificationFiles[index].notifiable &&  isAdmin && (
                                            <button className={CustomStyle.download_button} 
                                                onClick={e => {
                                                    this.notification(notificationFiles[index]);
                                                }}
                                            >
                                                <span>事業者へ通知</span>
                                            </button>
                                        )}
                                    </td>
                                </tr>
                            )):null}
                        </tbody>
                    </table>
                </div>
            </Box>
        );
    }
}
export default withTranslation()(withTheme(NotificationFileList));