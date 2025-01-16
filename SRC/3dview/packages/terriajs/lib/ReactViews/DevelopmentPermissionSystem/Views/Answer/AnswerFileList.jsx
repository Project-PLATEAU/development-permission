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

/**
 * 回答添付ファイル一覧コンポーネント
 */
@observer
class AnswerFileList extends React.Component {
    static displayName = "AnswerFileList";
    static propTypes = {
        terria: PropTypes.object.isRequired,
        viewState: PropTypes.object.isRequired,
        theme: PropTypes.object,
        t: PropTypes.func.isRequired,
        answerFiles: PropTypes.array
    }

    constructor(props) {
        super(props);
        this.state = {
            viewState: props.viewState,
            terria: props.terria,
            height: "0",
            //回答ファイル一覧
            answerFiles: props.answerFiles
        };
    }

    /**
     * 初期処理
     */
    componentDidMount() { }

    /**
     * props更新時
     */
    componentDidUpdate(prevProps) {
        if (!_isEqual(this.props.answerFiles, prevProps.answerFiles)) {
            this.setState({answerFiles: this.props.answerFiles});
        }
    }

    /**
     * ファイルダウンロード
     * @param {string} path　apiのpath
     * @param {object} file 対象ファイル情報
     * @param {string} fileNameKey 対象ファイルの取得key
     */
    output(path, file, fileNameKey) {
        //ダウンロード時に認証が必要ため、申請ID及び照合IDとパスワードをセット
        if(!this.props.terria.authorityJudgment() && file){
            file.applicationId = this.props.viewState.answerContent.applicationId;
            file.loginId = this.props.viewState.answerContent.loginId;
            file.password = this.props.viewState.answerContent.password;
        }
        // APIへのリクエスト
        fetch(Config.config.apiUrl + path, {
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
                const now = new Date();
                let anchor = document.createElement("a");
                anchor.href = window.URL.createObjectURL(blob);
                anchor.download = file[fileNameKey];
                anchor.click();
            })
            .catch(error => {
                console.error('処理に失敗しました', error);
                alert('処理に失敗しました');
            });
    }

    /**
     * 拡張子取得
     * @param {String} fileName ファイル名
     */
    getExtension(fileName){
        // 拡張子チェック
        let extension = fileName.split('.').pop();
        return extension;
    }

    render() {
        const answerFiles = this.state.answerFiles;

        return (
            <Box
                centered
                displayInlineBlock
                className={CustomStyle.custom_content}
            >
                <h2 className={CustomStyle.title}>回答添付ファイル一覧</h2>
                <Spacing bottom={1} />
                <div className={CustomStyle.scroll_container} id="AnswerFileListTable" style={{height: "18vh", minHeight:"200px"}}>    
                    <table className={CustomStyle.selection_table}>
                        <thead>
                            <tr className={CustomStyle.table_header}>
                                <th className="no-sort" style={{ width: "20%" }}>回答ファイル</th>
                                <th style={{ width: "35%" }}>対象</th>
                                <th style={{ width: "15%" }}>拡張子</th>
                                <th style={{ width: "30%" }}>ファイル名</th>
                            </tr>
                        </thead>
                        <tbody>
                            {answerFiles && Object.keys(answerFiles).map(key => (
                                <tr>
                                    <td>
                                        <button
                                            className={CustomStyle.download_button}
                                            onClick={e => {
                                                this.output("/answer/file/download", answerFiles[key], "answerFileName");
                                            }}
                                        >
                                            <span>ダウンロード</span>
                                        </button>
                                    </td>
                                    <td>
                                        {answerFiles[key].judgementInformation?answerFiles[key].judgementInformation.title:""}
                                    </td>
                                    <td>{this.getExtension(answerFiles[key]["answerFileName"])}</td>
                                    <td style={{ width: 250 + "px", overflow: "hidden", wordWrap: "break-word" }}>
                                        {answerFiles[key]["answerFileName"]}
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
export default withTranslation()(withTheme(AnswerFileList));