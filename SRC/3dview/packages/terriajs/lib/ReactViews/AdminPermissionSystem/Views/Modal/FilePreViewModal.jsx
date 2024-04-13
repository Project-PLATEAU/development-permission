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
import URI from "urijs";

/**
 * ファイルプレビューモーダル
 */

@observer
class FilePreViewModal extends React.Component {
    static displayName = "FilePreViewModal"
    static propsType = {
        terria: PropTypes.object.isRequired,
        viewState: PropTypes.object.isRequired,
        t: PropTypes.func.isRequired,
    }

    constructor(props) {
        super(props);
        this.state = {
            viewState: props.viewState,
            terria: props.terria,
            fileList: props.viewState.fileDownloadFile,
            target: props.viewState.fileDownloadTarget,
            // コールバック関数
            callback: this.props.callback,
        }
        this.CallBackFunction = this.CallBackFunction.bind(this);
    }

    /**
     * 初期処理
     */
    componentDidMount() {
        this.draggable(document.getElementById('filePreViewModalDrag'), document.getElementById('filePreViewModal'));
    }

    /**
     * コールバック関数
     */
    CallBackFunction(path, file, fileNameKey) {
        this.state.callback(path, file, fileNameKey);
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
        this.state.viewState.changeFilePreViewModalShow();
    }

   /**
    * ファイル拡張子確認
    * @param {*} fileName 
    * @returns 
    */
    extensionCheck(fileName){
        const extArray=["PDF","PNG","JPG"];
        // 拡張子確認（PDF,PNG,JPG）
        let extension = this.getExtension(fileName).toLocaleUpperCase();;
        if(extArray.includes(extension)){
            return true;
        }else{
            return false;
        }
    }

    /**
     * 拡張子取得
     * @param {String} fileName 
     */
    getExtension(fileName){
        // 拡張子チェック
        let extension = fileName.split('.').pop();
        return extension;
    }

    /**
     * コールバック関数を呼び出す
     * （別タブでファイルを開く）
     * @param {*} filePath 
     * @returns 
     */
    openFile(file){
        this.CallBackFunction("/application/file/download", file, "uploadFileName");
    }

    render(){
        const target = this.state.target;
        const fileList = this.state.fileList;

        return (
            <div className={Styles.overlay}>
                <div className={Styles.modal}  id="filePreViewModal">
                    <Box position="absolute" paddedRatio={3} topRight>
                        <RawButton onClick={() => { this.close(); }}>
                            <StyledIcon
                                styledWidth={"16px"}
                                fillColor={"#000"}
                                opacity={"0.5"}
                                glyph={Icon.GLYPHS.closeLight}
                                css={`cursor:pointer;`}
                            />
                        </RawButton>
                    </Box>
                    <nav className={Styles.custom_nuv} id="filePreViewModalDrag">
                        ファイルプレビュー
                    </nav>
                    <div className={Styles.container}>
                        <p>ファイル：<span style={{ fontWeight: "bold"}}>{target}</span></p>
                        <p>プレビューする申請ファイルの版を選択してください。</p>
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
                                        (this.extensionCheck(fileList[index].uploadFileName) &&(
                                            <tr key={`filePreView-${fileList[index].fileId}`}>    
                                                <td>{fileList[index].versionInformation}</td>
                                                <td>{fileList[index].uploadDatetime}</td>
                                                <td style={{textAlign: "left", padding:"0 5px"}}>
                                                    {fileList[index].uploadFileName}
                                                </td>
                                                <td>
                                                    <button 
                                                        className={Styles.download_button} 
                                                        onClick={e => {
                                                            this.openFile(fileList[index]);
                                                        }}>
                                                        <span>プレビュー</span>
                                                    </button>
                                                </td>
                                            </tr>
                                        ))
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
export default withTranslation()(withTheme(FilePreViewModal));