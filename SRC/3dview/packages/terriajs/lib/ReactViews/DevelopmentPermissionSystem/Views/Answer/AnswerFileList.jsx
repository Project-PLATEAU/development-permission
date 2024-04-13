import { observer } from "mobx-react";
import PropTypes from "prop-types";
import React from "react";
import { withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import Spacing from "../../../../Styled/Spacing";
import Box from "../../../../Styled/Box";
import CustomStyle from "./scss/answer-file-list.scss";
import Config from "../../../../../customconfig.json";

/**
 * 回答内容確認画面の回答添付ファイル一覧コンポーネント
 */
@observer
class AnswerFileList extends React.Component {
    static displayName = "AnswerFileList";
    static propTypes = {
        terria: PropTypes.object.isRequired,
        viewState: PropTypes.object.isRequired,
        theme: PropTypes.object,
        t: PropTypes.func.isRequired,
        selectedAnswers: PropTypes.object
    }
    constructor(props) {
        super(props);
        this.state = {
            viewState: props.viewState,
            terria: props.terria,
            height: 0,
            //表示用回答内容リスト
            answers: props.answerContentList,
        };
    }

    /**
     * 初期処理
     */
    componentDidMount() {

        this.getWindowSize(); 
    }

    /**
     * リサイズのために、高さ再計算
     */
    getWindowSize() {
        let win = window;
        let e = window.document.documentElement;
        let g = window.document.documentElement.getElementsByTagName('body')[0];
        let h = win.innerHeight|| e.clientHeight|| g.clientHeight;

        const getRect = document.getElementById("AnswerFileListTable");
        let height = h - getRect.getBoundingClientRect().top;

        if(this.props.viewState.showChatView){
            let map = window.document.documentElement.getElementsByTagName('canvas')[0];
            height = map.clientHeight/2 - 80;
        }else if(this.props.viewState.showConfirmAnswerInformationView){
            const answerContent = document.getElementById("AnswerContentListTable");
            height = h - answerContent.getBoundingClientRect().top;
            height = (height-170)/2;
        }
        this.setState({height: height});
    }

    /**
     * ファイルダウンロード
     * @param {string} path　apiのpath
     * @param {object} file 対象ファイル情報
     * @param {string} fileNameKey 対象ファイルの取得key
     */
    output(path, file, fileNameKey) {
        //ダウンロード時に認証が必要ため、申請ID及び照合IDとパスワードをセット
        if(file){
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
        let answers = this.props.answerContentList;
        let height = this.state.height;
        let isChatView = this.props.viewState.showChatView;
        return (
            <>
                <Box
                    centered
                    displayInlineBlock
                    className={CustomStyle.custom_content}
                >
                    {isChatView && (
                        <h2 className={CustomStyle.title_in_chat_view}>添付ファイル一覧</h2>
                    )}
                    {!isChatView && (
                        <h2 className={CustomStyle.title}>回答添付ファイル一覧</h2>
                    )}
                    <Spacing bottom={1} />
                    {!isChatView && (
                        <span style={{margin:"5px"}}>選択中の対象：{answers[0]["judgementInformation"]["title"]}</span>
                    )}
                    <Spacing bottom={1} />
                    <div className={CustomStyle.scrollContainer} id="AnswerFileListTable" style={{height: height + "px"}}>    
                        <table className={CustomStyle.selection_table}>
                            <thead>
                                <tr className={CustomStyle.table_header}>
                                    <th style={{ width: 100 + "px" }}>回答ファイル</th>
                                    <th style={{ width: 100 + "px" }}>対象</th>
                                    <th style={{ width: 50 + "px" }}>拡張子</th>
                                    <th>ファイル名</th>
                                </tr>
                            </thead>
                            <tbody>
                                {answers && Object.keys(answers).map(index => (
                                    Object.keys(answers[index]["answerFiles"]).map(key => (
                                        <tr>
                                            <td>
                                                <button
                                                    className={CustomStyle.download_button}
                                                    onClick={e => {
                                                        this.output("/answer/file/download", answers[index]["answerFiles"][key], "answerFileName");
                                                    }}
                                                >
                                                    <span>ダウンロード</span>
                                                </button>
                                            </td>
                                            <td>
                                                {answers[index]["judgementInformation"]["title"]}
                                            </td>
                                            <td>{this.getExtension(answers[index]["answerFiles"][key]["answerFileName"])}</td>
                                            <td style={{ width: 250 + "px", overflow: "hidden", wordWrap: "break-word" }}>
                                                {answers[index]["answerFiles"][key]["answerFileName"]}
                                            </td>
                                        </tr>
                                    ))
                                ))}
                            </tbody>
                        </table>
                    </div>
                </Box>
            </>
        );
    }
}
export default withTranslation()(withTheme(AnswerFileList));