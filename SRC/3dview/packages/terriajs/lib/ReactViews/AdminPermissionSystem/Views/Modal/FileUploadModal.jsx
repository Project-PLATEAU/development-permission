import React from "react";
import PropTypes from "prop-types";
import { observer } from "mobx-react";
import { withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import Box from "../../../../Styled/Box";
import Styles from "./scss/FileUploadModal.scss"
import { RawButton } from "../../../../Styled/Button";
import Icon, { StyledIcon } from "../../../../Styled/Icon";
import Config from "../../../../../customconfig.json";
/**
 * 行政用コンポーネント：回答ファイル選択ダイアログ
 */

@observer
class FileUploadModal extends React.Component {
    static displayName = "FileUploadModal"
    static propsType = {
        terria: PropTypes.object.isRequired,
        viewState: PropTypes.object.isRequired
    }

    constructor(props) {
        super(props);
        this.state = {
            viewState: props.viewState,
            terria: props.terria,
            // 申請ファイル一覧
            applicationFiles: props.viewState.fileQuoteFile,
            // 申請ファイル版情報一覧
            applicationVersions: [],
            // 回答ファイル一覧
            answerFiles: props.viewState.answerQuoteFile,
            // 引用テーブル表示モード
            quoteUpload: false,
            // ファイルアップロードコールバック関数
            // callback: this.props.callback
            callback: props.viewState.callBackFunction
        }
        this.CallBackFunction = this.CallBackFunction.bind(this);
    }

    /**
     * 初期処理
     */
    componentDidMount() {
        this.draggable(document.getElementById('fileUploadModalDrag'), document.getElementById('fileUploadModal'));
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
     * ファイルアップロードコールバック関数
     * @param {*} file ファイル実体
     * @param {*} quote 引用する申請ファイル
     * @param {*} answerFile 引用する回答ファイル
     */
    CallBackFunction(file, quote, answerFile) {
        this.state.callback(file, quote, answerFile);
    }
    
    /**
     * ファイルアップロード
     * @param {*} e イベント
     * @returns 
     */
    handleFileUpload(e){
        const file = e.target.files[0];
        if (file.size > 10485760) {
            alert("10M以下のファイルをアップロードできます");
            return false;
        }
        // 拡張子チェック
        let extension = file.name.split('.').pop();
        if(Config.extension.answerFile){
            let allowExts = Config.extension.answerFile.split(',')
            if(allowExts.indexOf(extension) === -1){
                alert(Config.extension.answerFile + " のいずれかのファイル形式のファイルをアップロードしてください。");
                return false;
            }
        }
        // ファイル名チェック
        const fileNameWithoutExtension = file.name.split(".").slice(0, -1).join(".");
        const regex = /[\"\'<>\&]/;
        const reg = new RegExp(regex);
        const result = reg.exec(fileNameWithoutExtension);
        if (result != null) {
            alert("ファイル名に禁止文字("+'"'+",',<,>,&"+")のいずれか含めていますので、ファイル名を修正してアップロードしてください。");
            return false;
        }
        this.CallBackFunction(file, null, null);
        this.close();
    };

    /**
     * 引用可能なファイル一覧表示
     * @param {*} applicationFileHistorys 過去版含む申請ファイルリスト
     */
    quoteUpload(applicationFileHistorys){
        if (applicationFileHistorys.length > 1) {
            this.setState( {
                quoteUpload: true,
                applicationVersions: applicationFileHistorys
            });
        } else {
            this.quoteApplicationFile(applicationFileHistorys[0]);
        }
    }

    /**
     * 申請ファイル引用
     * @param {*} applicationFileHistory 過去版含む申請ファイルリスト
     */
    quoteApplicationFile(applicationFileHistory){
        this.setState( {quoteUpload: false} );
        this.CallBackFunction(null, applicationFileHistory, null);
        this.close();
    }

    /**
     * 回答ファイル引用
     * @param {*} answerFile 回答ファイル
     */
    quoteAnswerFile(answerFile){
        this.setState( {quoteUpload: false} );
        this.CallBackFunction(null, null, answerFile);
        this.close();
    }

    /**
     * モーダルを閉じる
     * 
     */
    close(){
        this.state.viewState.changeFileUploadModalShow();
    }

    render(){
        let applicationFiles = this.state.applicationFiles;
        let applicationFileHistories = this.state.applicationVersions;
        return (
            <div className={Styles.overlay}>
                <div className={Styles.modal}  id="fileUploadModal">
                    <Box position="absolute" paddedRatio={3} topRight>
                        <RawButton onClick={() => {
                            this.close();
                        }}>
                            <StyledIcon
                                styledWidth={"16px"}
                                fillColor={"#000"}
                                opacity={"0.5"}
                                glyph={Icon.GLYPHS.closeLight}
                                css={`
                                    cursor:pointer;
                                `}
                            />
                        </RawButton>
                    </Box>
                    <nav className={Styles.custom_nuv} id="fileUploadModalDrag">
                        ファイル追加
                    </nav>
                    { !this.state.quoteUpload && (
                        <div className={Styles.container}>
                            {/* PCから追加 */}
                            <div>
                                <p style={{ fontSize: "1.1em", fontWeight: "600"}}>・PCのファイルを追加</p>
                                <p>PCのファイルを参照して追加します。</p>
                                <label htmlFor="file-upload" className={Styles.file_upload_button}>
                                    ファイルを選択
                                    <input id="file-upload" type="file" onChange={(e) => this.handleFileUpload(e)} />
                                </label>
                            </div>

                            {/* 申請ファイル引用 */}
                            <div>
                                <p style={{ fontSize: "1.1em", fontWeight: "600"}}>・申請ファイルから引用</p>
                                <p>申請ファイルを引用してファイルを追加します。</p>
                                <div className={Styles.scrollContainer}>
                                    <div>
                                        <table className={Styles.selection_table}>
                                            <thead className={Styles.table_header}>
                                                <tr>
                                                    <th style={{width: "35%"}}>対象</th>
                                                    <th style={{width: "10%"}}>拡張子</th>
                                                    <th style={{width: "40%"}}>ファイル名</th>
                                                    <th style={{width: "15%"}}></th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                            {Object.keys(this.state.applicationFiles).map(index => {

                                                //申請段階を問わず、全ての申請ファイルが引用できる
                                                const applicationFileHistorys = this.state.applicationFiles[index]?.applicationFileAllHistorys;

                                                // applicationFileHistorysが存在しない or 空の場合は表示しない
                                                if (!applicationFileHistorys || Object.keys(applicationFileHistorys).length === 0) return null;

                                                // 最大の申請段階ID、versionInformationを持つデータのキーを取得
                                                const maxVersionKey = Object.keys(applicationFileHistorys).reduce((maxKey, currentKey) => {
                                                    if (!maxKey) return currentKey;
                                                    if(applicationFileHistorys[maxKey].applicationStepId > applicationFileHistorys[currentKey].applicationStepId ){
                                                        return maxKey;
                                                    }else{
                                                        if(applicationFileHistorys[maxKey].applicationStepId == applicationFileHistorys[currentKey].applicationStepId ){
                                                            return applicationFileHistorys[maxKey].versionInformation > applicationFileHistorys[currentKey].versionInformation ? maxKey : currentKey;
                                                        }else{
                                                            return currentKey;
                                                        }
                                                    }
                                                }, null);

                                                return (
                                                    <tr key={`${index}-${maxVersionKey}`}>
                                                        <td>{this.state.applicationFiles[index]?.applicationFileName}</td>
                                                        <td>{applicationFileHistorys[maxVersionKey]?.extension}</td>
                                                        <td>{applicationFileHistorys[maxVersionKey]?.uploadFileName}</td>
                                                        <td>
                                                            <button
                                                                className={Styles.upload_button}
                                                                onClick={(e) => this.quoteUpload(applicationFileHistorys)}
                                                            >引用
                                                            </button>
                                                        </td>
                                                    </tr>
                                                );
                                            })}
                                            </tbody>
                                        </table>
                                    </div>
                                </div>
                            </div>

                            {/* 既登録回答ファイル引用 */}
                            { this.state.answerFiles.length > 0 && (
                                <div>
                                    <p style={{ fontSize: "1.1em", fontWeight: "600"}}>・回答ファイルから引用</p>
                                    <p>回答ファイルを引用してファイルを追加します。</p>
                                    <div className={Styles.scrollContainer}>
                                        <div>
                                            <table className={Styles.selection_table}>
                                            <thead className={Styles.table_header}>
                                                <tr>
                                                    <th style={{width: "10%"}}>拡張子</th>
                                                    <th style={{width: "75%"}}>ファイル名</th>
                                                    <th style={{width: "15%"}}></th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                {Object.keys(this.state.answerFiles).map(index => {
                                                    return (
                                                        <tr key={index}>
                                                            <td>{this.state.answerFiles[index]?.answerFileName.split(".").pop()}</td>
                                                            <td>{this.state.answerFiles[index]?.answerFileName}</td>
                                                            <td>
                                                                <button
                                                                    className={Styles.upload_button}
                                                                    onClick={(e) => this.quoteAnswerFile(this.state.answerFiles[index])}
                                                                >引用
                                                                </button>
                                                            </td>
                                                        </tr>
                                                    );
                                                })}
                                            </tbody>
                                            </table>
                                        </div>
                                    </div>
                                </div>
                            )}
                        </div>
                    )}

                    { this.state.quoteUpload && (
                        <div className={Styles.container}>
                            <p>引用する申請ファイルの版を選択してください。</p>
                            <div className={Styles.scrollContainer}>
                                <div style={{max_height: "260px", overflowY: "auto"}}>
                                    <table className={Styles.selection_table}>
                                        <thead className={Styles.table_header}>
                                            <tr>
                                                <th style={{width: "15%"}}>申請種別</th>
                                                <th style={{width: "10%"}}>版</th>
                                                <th style={{width: "25%"}}>アップロード日時</th>
                                                <th style={{width: "35%"}}>ファイル名</th>
                                                <th style={{width: "15%"}}></th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            {Object.keys(applicationFileHistories).map(index => (
                                                <tr>    
                                                    <td>{applicationFileHistories[index].applicationStepName}</td>
                                                    <td>{applicationFileHistories[index].versionInformation}</td>
                                                    <td>{applicationFileHistories[index].uploadDatetime}</td>
                                                    <td>{applicationFileHistories[index].uploadFileName}</td>
                                                    <td>
                                                        <button
                                                            className={Styles.upload_button}
                                                            onClick={(e) => this.quoteApplicationFile(applicationFileHistories[index])}
                                                        >引用
                                                        </button>
                                                    </td>
                                                </tr>
                                            ))}
                                            
                                        </tbody>
                                    </table>
                                </div>
                            </div>
                        </div>
                    )}

                </div>
            </div>

        )
    }



}
export default withTranslation()(withTheme(FileUploadModal));