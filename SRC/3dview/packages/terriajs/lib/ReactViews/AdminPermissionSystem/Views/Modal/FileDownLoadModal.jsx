import React from "react";
import PropTypes from "prop-types";
import { observer } from "mobx-react";
import { withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import Box from "../../../../Styled/Box";
import Styles from "./scss/FileDownLoadModal.scss"
import { RawButton } from "../../../../Styled/Button";
import Icon, { StyledIcon } from "../../../../Styled/Icon";
import Config from "../../../../../customconfig.json";

/**
 * 行政用コンポーネント：ファイルダウンロードモーダル
 */

@observer
class FileDownLoadModal extends React.Component {
    static displayName = "FileDownLoadModal"
    static propsType = {
        terria: PropTypes.object.isRequired,
        viewState: PropTypes.object.isRequired,
        t: PropTypes.func.isRequired
    }

    constructor(props) {
        super(props);
        this.state = {
            viewState: props.viewState,
            terria: props.terria,
            fileList: props.viewState.fileDownloadFile,
            target: props.viewState.fileDownloadTarget
        }
    }

    /**
     * 初期処理
     */
    componentDidMount() {
        this.draggable(document.getElementById('fileDownLoadModalDrag'), document.getElementById('fileDownLoadModal'));
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
     * モーダルを閉じる
     * 
     */
    close(){
        this.state.viewState.changeFileDownloadModalShow();
    }

    /**
     * ファイルダウンロード
     * @param {*} path ファイルダウンロード用APIのパス
     * @param {*} file ダウンロード対象ファイル
     * @param {*} fileNameKey ファイル名に対するキー項目
     */
    outputFile(path, file, fileNameKey) {
        if(file){
            file.applicationId = file.applicationId;
            if(!this.props.terria.authorityJudgment()){
                file.loginId = this.props.viewState.answerContent.loginId;
                file.password = this.props.viewState.answerContent.password;
            }
        }
        // APIへのリクエスト
        fetch(Config.config.apiUrl + path, {
            method: 'POST',
            body: JSON.stringify(file),
            headers: new Headers({ 'Content-type': 'application/json' }),
        })
        .then(res => {
            // 401認証エラーの場合の処理を追加
            if (res.status === 401) {
                alert('認証情報が無効です。ページの再読み込みを行います。');
                window.location.href = "./login/";
                return null;
            }
            return res.blob();
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

    render(){
        const target = this.state.target;
        const fileList = this.state.fileList;

        return (
            <div className={Styles.overlay}>
                <div className={Styles.modal}  id="fileDownLoadModal">
                    <Box position="absolute" paddedRatio={3} topRight>
                        <RawButton onClick={() => {
                            this.close();
                        }}>
                            <StyledIcon
                                styledWidth={"16px"}
                                fillColor={"#000"}
                                opacity={"0.5"}
                                glyph={Icon.GLYPHS.closeLight}
                                css={`cursor:pointer;`}
                            />
                        </RawButton>
                    </Box>
                    <nav className={Styles.custom_nuv} id="fileDownLoadModalDrag">
                        ファイルダウンロード
                    </nav>
                    <div className={Styles.container}>
                        <p>ファイル：<span style={{ fontWeight: "bold"}}>{target}</span></p>
                        <p>ダウンロードする申請ファイルの版を選択してください。</p>
                        <div style={{height: "230px", overflowY: "auto"}}>
                            <table className={Styles.selection_table}>
                                <thead className={Styles.table_header}>
                                    <tr>
                                        <th style={{width: "10%"}}>版</th>
                                        <th style={{width: "30%"}}>アップロード日時</th>
                                        <th style={{width: "40%"}}>ファイル名</th>
                                        <th style={{width: "20%"}}></th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {fileList && Object.keys(fileList).map(index => (
                                        <tr key={`fileDownLoad-${fileList[index].fileId}`}>
                                            <td>{fileList[index].versionInformation}</td>
                                            <td>{fileList[index].uploadDatetime}</td>
                                            <td style={{textAlign: "left", padding:"0 5px"}}>
                                                {fileList[index].uploadFileName}
                                            </td>
                                            <td>
                                                <button className={Styles.download_button}
                                                    onClick={e => {
                                                        this.outputFile("/application/file/download", fileList[index],"uploadFileName");
                                                    }}>
                                                    <span>ダウンロード</span>
                                                </button>
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        )
    }
}
export default withTranslation()(withTheme(FileDownLoadModal));