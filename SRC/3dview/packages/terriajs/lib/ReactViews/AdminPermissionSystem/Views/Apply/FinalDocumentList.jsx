import { observer } from "mobx-react";
import PropTypes from "prop-types";
import React from "react";
import { withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import Spacing from "../../../../Styled/Spacing";
import Box from "../../../../Styled/Box";
import CustomStyle from "./scss/answer-history-list.scss";
import _isEqual from 'lodash/isEqual';
import Icon, { StyledIcon } from "../../../../Styled/Icon";
import Config from "../../../../../customconfig.json";

/**
 * 【R6】最終提出書類一覧コンポーネント
 */
@observer
class developmentDocumentFileFormListList extends React.Component {
    static displayName = "developmentDocumentFileFormListList";
    static propTypes = {
        terria: PropTypes.object.isRequired,
        viewState: PropTypes.object.isRequired,
        theme: PropTypes.object,
        t: PropTypes.func.isRequired,
        applicationId: PropTypes.number
    }
    constructor(props) {
        super(props);
        this.state = {
            viewState: props.viewState,
            terria: props.terria,
            applicationId: props.applicationId,
            developmentDocumentFileFormList:[]
        };
    }

    /**
     * 初期処理
     */
    componentDidMount() {
         // APIへのリクエスト
         const applicationId = this.state.applicationId;
         if(applicationId != undefined && applicationId != null && applicationId > 0){
            const request = {};
            request.applicationId = this.state.applicationId;
            if(!this.props.terria.authorityJudgment()){
                request.loginId = this.props.viewState.answerContent.loginId;
                request.password = this.props.viewState.answerContent.password;
            }
            fetch(Config.config.apiUrl + "/developmentdocument/documents", {
                method: 'POST',
                body: JSON.stringify(request),
                headers: new Headers({ 'Content-type': 'application/json' }),
            })
            .then(res => {
                // 401認証エラーの場合の処理を追加
                if (res.status === 401) {
                    alert('認証情報が無効です。ページの再読み込みを行います。');
                    window.location.reload();
                    return null;
                }
                return res.json();
            })
            .then((res) => {
                if (res && Object.keys(res).length > 0) {
                    this.setState({developmentDocumentFileFormList:res});
                }
            })
            .catch(error => {
                console.error('処理に失敗しました', error);
                alert('処理に失敗しました');
            });
        }
    }

    /**
     * props更新時
     */
    componentDidUpdate(prevProps) {
        if (!_isEqual(this.props.applicationId, prevProps.applicationId)) {
            this.setState({applicationId:this.props.applicationId});
        }
    }

    /**
     * ダウンロード処理
     * @param {*} request 
     */
    download(request){
        //ダウンロード時に認証が必要ため、申請ID及び照合IDとパスワードをセット
        if(!this.props.terria.authorityJudgment() && request){
            request.applicationId = this.props.viewState.answerContent.applicationId;
            request.loginId = this.props.viewState.answerContent.loginId;
            request.password = this.props.viewState.answerContent.password;
        }
        // APIへのリクエスト
        let fileName = 'unkown.zip';
        fetch(Config.config.apiUrl + "/developmentdocument/file/download", {
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
                let disposition = res.headers.get('Content-Disposition');
                disposition = disposition
                    ? decodeURIComponent(disposition)
                    : 'filename=unkown.zip';
                fileName = disposition
                    ? disposition.split('filename=')[1].replace(/"/g, '')
                    : 'unkown.zip';
                return res.blob();
            }
        })
        .then(blob => {
            let anchor = document.createElement("a");
            anchor.href = window.URL.createObjectURL(blob);
            anchor.download = fileName;
            anchor.click();
        })
        .catch(error => {
            console.error('処理に失敗しました', error);
            alert('処理に失敗しました');
        });
    }

    render() {
        const developmentDocumentFileFormList = this.state.developmentDocumentFileFormList;
        return (
            <Box
                centered
                displayInlineBlock
                className={CustomStyle.custom_content}
            >
                <h2 className={CustomStyle.title}>最終提出書類一式</h2>
                <Spacing bottom={1} />
                <div className={CustomStyle.scroll_container} id="developmentDocumentFileFormListListTable" style={{height: "18vh", minHeight:"200px"}}>    
                    <table className={CustomStyle.selection_table}>
                        <thead>
                            <tr className={CustomStyle.table_header}>
                                <th style={{ width: 20 + "%" }} className="no-sort">ファイル</th>
                                <th style={{ width: 80 + "%" }}>項目</th>
                            </tr>
                        </thead>
                        <tbody>
                        {developmentDocumentFileFormList && Object.keys(developmentDocumentFileFormList).map(index => (
                            <tr key={`developmentDocumentFileFormListListTable-tr-`+index }
                            >
                                <td>
                                    <button className={CustomStyle.download_button} 
                                            onClick={e => {
                                                this.download(developmentDocumentFileFormList[index]);
                                            }}
                                        >
                                            <span>ダウンロード</span>
                                    </button>
                                </td>
                                <td>
                                    {developmentDocumentFileFormList[index].documentName}
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
export default withTranslation()(withTheme(developmentDocumentFileFormListList));