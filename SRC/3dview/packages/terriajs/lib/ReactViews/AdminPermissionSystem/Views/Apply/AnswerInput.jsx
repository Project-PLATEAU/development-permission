import { observer } from "mobx-react";
import PropTypes from "prop-types";
import React, { Component } from 'react';
import { withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import Icon, { GLYPHS, StyledIcon } from "../../../../Styled/Icon";
import Spacing from "../../../../Styled/Spacing";
import Box from "../../../../Styled/Box";
import Button, { RawButton } from "../../../../Styled/Button";
import CustomStyle from "./scss/answer-input.scss";
import Config from "../../../../../customconfig.json";
import ShowMessage from "../Message/ShowMessage";
import AdminTab from "../Tab/AdminTab";
import Styles from "../../../DevelopmentPermissionSystem/PageViews/scss/pageStyle.scss";
import FileDownLoadModal from "../Modal/FileDownLoadModal";
import FileUploadModal from "../Modal/FileUploadModal";
import * as markerjs2 from 'markerjs2';
import ImageEdit from "../ImageEdit/ImageEdit";
import AnswerTemplateModal from "../Modal/AnswerTemplateModal";
import AnswerContentInputModal from "../Modal/AnswerContentInputModal";
import PdfViewer from "../FileViewer/PdfViewer";
import PdfViewModal from "../Modal/PdfViewModal";
import Tiff from "tiff.js";

/**
 * 回答登録画面
 */
@observer
class AnswerInput extends React.Component {

    static displayName = "AnswerInput";

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
            //回答
            answers: [],
            //回答ファイル
            applicationFiles: [],
            //回答ファイルアップロード対象
            uploadFileFormList: [],
            //回答ファイル削除対象
            deleteFileFormList: {},
            //ファイル表示
            fileOpen: false,
            //選択中の回答
            selectedAnswer: null,
            // 選択中の回答キー
            selectedAnswerKey: null,
            //編集対象ファイル
            targetFile: null,
            //編集対象ファイルパス
            targetFilePath: null,
            //編集対象ファイルURL
            targetFileUrl: null,
            //編集対象申請ファイルバージョン
            targetAppFileVersion: null,
            // 編集可能拡張子
            editableExtentions: ["pdf", "jpg", "jpeg", "png", "tiff", "tif"],
            // 更新対象の回答ファイルキー
            updateAnswerFileKey: null,
            // ファイル追加か否か
            addFile: false,
            // 引用ファイルか否か
            quoteFile: false,
            // PDFファイル編集可否
            pdfEditable: false,
            // PDF→PNG変換フラグ
            pdfConvertFlag: false,
            // 引用ファイル
            quoteFileData: {
                filePath: null,
                fileName: null,
                fileVersion: null
            }
        };
        this.inputFromTemplate = this.inputFromTemplate.bind(this);
        this.fileUploadCallback = this.fileUploadCallback.bind(this);
    }

    /**
     * 初期処理
     */
    componentDidMount() {
        if (this.props.viewState.applicationInformationSearchForApplicationId) {
            fetch(Config.config.apiUrl + "/application/detail/" + this.props.viewState.applicationInformationSearchForApplicationId)
                .then(res => {
                    // 401認証エラーの場合の処理を追加
                    if (res.status === 401) {
                        alert('認証情報が無効です。ページの再読み込みを行います。');
                        window.location.href = "./login/";
                        return null;
                    }
                    return res.json();
                })
                .then(res => {
                    if (res.applicationId) {
                        Object.keys(res.answers).map(key => {
                            res.answers[key]["uploadFileFormList"] = [];
                            Object.keys(res.answers[key].answerFiles).map(file => {
                                res.answers[key].answerFiles[file]["uploadFileForm"] = null;
                                res.answers[key].answerFiles[file]["deleteFlag"] = false;
                            })
                        })
                        this.setState({
                            answers: res.answers,
                            applicationFiles: res.applicationFiles
                        });
                    } else {
                        alert("回答の取得に失敗しました。再度操作をやり直してください。");
                    }
                }).catch(error => {
                    console.error('通信処理に失敗しました', error);
                    alert('通信処理に失敗しました');
                }).finally(() => document.getElementById("customloader_sub_").style.display = "none");
        } else {
            alert("回答の取得に失敗しました。再度操作をやり直してください。");
            document.getElementById("customloader_sub_").style.display = "none";
        }
    }
    /**
     * ファイルアップロードコールバック関数
     * @param {*} file ファイル実体（アップロード時に使用）
     * @param quote 引用する申請ファイル情報
     * @param answerQuote 引用する回答ファイル情報
     */
    fileUploadCallback(file, quote, answerQuote) {
        const answers = this.state.answers;
        const key = this.state.selectedAnswerKey;
        const answerFileKey = this.state.updateAnswerFileKey;
        console.log(key)
        if (key != null) {
            if (file != null) {
                let reader = new FileReader();
                // アップロードされたファイルがPDFファイルの場合
                if (file.type === 'application/pdf'){
                    reader.readAsArrayBuffer(file);
                    reader.onload = (e) => {
                        let blob = new Blob([e.target.result], { type: file.type });
                        let uploadFileUrl = URL.createObjectURL(blob);
                        if (!answers[key]?.answerFiles.some(answerFile => answerFile.answerFileName.includes(file.name))) {
                            // 新規追加
                            answers[key]["uploadFileFormList"] = answers[key]["uploadFileFormList"].filter((uploadAnswerFile) => uploadAnswerFile.answerFileName !== file.name);
                            answers[key]["uploadFileFormList"].push({ "answerFileId": "", "answerId": answers[key].answerId, "answerFileName": file.name, "filePath":  null, "uploadFile": file, "uploadFileUrl": uploadFileUrl});
                        } else{
                            let answerFileIndex = answers[key]?.answerFiles.findIndex(answerFile => answerFile.answerFileName.includes(file.name));
                            let answerFile = answers[key]?.answerFiles[answerFileIndex];
                            answers[key].answerFiles[answerFileIndex].uploadFileForm = {"answerFileId": answerFile.answerFileId, "answerId": answers[key].answerId, "answerFileName": file.name, "filePath":  null, "uploadFile": file, "uploadFileUrl": uploadFileUrl};
                        }
                        this.setState({
                            answers: answers,
                            selectedAnswer: answers[key],
                            quoteFile: false
                        });
                    }
                }
                // アップロードされたファイルが画像ファイルの場合
                if(file.type === 'image/png' || file.type === 'image/jpeg'){
                    reader.readAsDataURL(file);
                    reader.onload = (e) => {
                        let uploadFileUrl = e.target.result;
                        this.showFile(null, file.name, uploadFileUrl, null, false, true);
                        this.setState({
                            answers: answers,
                            selectedAnswer: answers[key],
                            quoteFile: false
                        });
                    }
                }
                // アップロードされたファイルがTIFFファイルの場合、pngに転換処理を行う
                if(file.type === 'image/tiff'){
                    const reader = new FileReader();
                    reader.onload = (event) => {
                      const tiff = new Tiff({ buffer: event.target.result });
                      const canvas = tiff.toCanvas();
                
                      if (canvas) {
                        canvas.toBlob((blob) => {
                            // BlobからオブジェクトURLを作成
                            const uploadFileUrl = URL.createObjectURL(blob);
                            const pngFileName = file.name.replace(/\.tiff?$/, '.png');
                            this.showFile(null, pngFileName, uploadFileUrl, null, false, true);
                            this.setState({
                                answers: answers,
                                selectedAnswer: answers[key],
                                quoteFile: false
                            });            
                        }, 'image/png', 0.9);          
                      } else {
                        alert("ファイルの読み込みに失敗しました");
                      }
                    };
                
                    reader.onerror = () => {
                      alert("ファイルの読み込みに失敗しました");
                    };
                
                    reader.readAsArrayBuffer(file);
                }
                // 上記以外の場合、
                if(file.type !== 'application/pdf' && !file.type.startsWith('image/')){
                    if (!answers[key]?.answerFiles.some(answerFile => answerFile.answerFileName.includes(file.name))) {
                        // 新規追加
                        answers[key]["uploadFileFormList"] = answers[key]["uploadFileFormList"].filter((uploadAnswerFile) => uploadAnswerFile.answerFileName !== file.name);
                        answers[key]["uploadFileFormList"].push({ "answerFileId": "", "answerId": answers[key].answerId, "answerFileName": file.name, "filePath": null, "uploadFile": file});
                    }else{
                        // 更新
                        let answerFile = answers[key]?.answerFiles[answerFileKey];
                        answers[key].answerFiles[answerFileKey].uploadFileForm = {"answerFileId": answerFile.answerFileId, "answerId": answers[key].answerId, "answerFileName": file.name, "filePath":  null, "uploadFile": file};
                    }
                    this.setState({
                        answers: answers,
                        selectedAnswer: answers[key],
                        quoteFile: false
                    });
                }
            }else {
                // 引用
                if (quote != null) {
                    this.setState({ 
                        quoteFileData: {
                            filePath: quote.filePath,
                            fileName: quote.uploadFileName,
                            fileVersion: quote.versionInformation
                        }
                    });
                    let fileName = quote.extension === "tiff" || quote.extension === "tif" ? quote.uploadFileName.replace(/\.tiff?$/, '.png') : quote.uploadFileName;
                    if(this.state.editableExtentions.includes(quote.extension)){
                        this.showFile(quote.filePath, quote.uploadFileName, null, quote.versionInformation, true, true);                
                        this.setState({
                            answers: answers,
                            selectedAnswer: answers[key],
                            quoteFile: true
                        });
                    }else{
                        if(!answers[key]?.answerFiles.some(answerFile => answerFile.answerFileName.includes(fileName))){
                            //新規追加
                            answers[key]["uploadFileFormList"] = answers[key]["uploadFileFormList"].filter((uploadAnswerFile) => uploadAnswerFile.answerFileName !== quote.uploadFileName);
                            answers[key]["uploadFileFormList"].push({ "answerFileId": "", "answerId": answers[key].answerId, "answerFileName": fileName, "filePath": quote.filePath, "version": quote.versionInformation});
                        }else{
                            //更新
                            let answerFileIndex = answers[key]?.answerFiles.findIndex(answerFile => answerFile.answerFileName.includes(fileName));
                            let answerFile = answers[key]?.answerFiles[answerFileIndex];
                            answers[key].answerFiles[answerFileIndex].uploadFileForm = {"answerFileId": answerFile.answerFileId, "answerId": answers[key].answerId, "answerFileName": fileName, "filePath": quote.filePath, "version": quote.versionInformation};
                        }
                    }
                    this.setState({
                        answers: answers,
                        selectedAnswer: answers[key],
                        quoteFile: true
                    });
                }
                if (answerQuote != null){
                    this.setState({ 
                        quoteFileData: {
                            filePath: answerQuote.answerFilePath,
                            fileName: answerQuote.answerFileName,
                            fileVersion: null
                        }
                    });
                    let extension = answerQuote.answerFileName.split(".").pop();
                    if(this.state.editableExtentions.includes(extension)){
                        this.showFile(answerQuote.answerFilePath, answerQuote.answerFileName, null, null, true, true);                
                        this.setState({
                            answers: answers,
                            selectedAnswer: answers[key],
                            quoteFile: true
                        });
                    }
                    this.setState({
                        answers: answers,
                        selectedAnswer: answers[key],
                        quoteFile: true
                    });
                }
            }
        }
    }

    /**
     * 更新対象ファイル全削除
     * @param {number} 対象回答のkey
     */
    targetFilesAllDelete(key) {
        let answers = this.state.answers;
        let deleteFileFormList = this.state.deleteFileFormList;
        answers[key]["uploadFileFormList"] = [];
        deleteFileFormList[key] = [];
        this.setState({
            answers: answers,
            deleteFileFormList: deleteFileFormList
        });
    }

    /**
     * 回答内容入力
     * @param {number} 対象回答のkey
     * @param {string} 入力された値
     */
    inputChange(key, value) {
        let answers = this.state.answers;
        answers[key]["answerContent"] = value;
        this.setState({
            answers: answers,
        });
    }
    /**
     * 回答内容を回答テンプレートで上書きする
     * @param {*} answerId 回答ID
     * @param {*} text テキスト
     */
    inputFromTemplate(answerId, text) {
        let targetDocument = document.getElementById("answer_input_" + answerId);
        if (targetDocument) {
            // text = targetDocument.value + text;
            targetDocument.value = text;
            targetDocument.defaultValue = text;
        }
        let answers = this.state.answers;
        for(let i = 0; i< answers.length; i++) {
            if (answers[i]["answerId"] == answerId) {
                answers[i]["answerContent"] = text;
                break;
            }
        }
        this.setState({
            answers: answers,
        });
    }
    /**
     * 区分選択
     * @param {number} 対象回答のkey
     * @param {boolean} 入力された値 
     */
    reapplicationChange(key, value) {
        let answers = this.state.answers;
        answers[key]["reApplicationFlag"] = value;
        this.setState({
            answers: answers
        })
    }

    /**
     *  回答登録
     */
    register() {
        //document.getElementById("answerFrame").scrollTop = 0;
        document.getElementById("customloader_sub_").style.display = "block";
        const answers = this.state.answers;
        let deleteFileFormList = [];
        let answersContentOnly = [];
        let uploadFileFormList = [];
        let deleteFileFormListCount = 0;
        let notifyCount = 0;
        if (Object.keys(deleteFileFormList).length > 0) {
            Object.keys(deleteFileFormList).map(key => {
                Object.keys(deleteFileFormList[key]).map(fileKey => {
                    if(deleteFileFormList[key][fileKey] && deleteFileFormList[key][fileKey].answerId){
                        deleteFileFormListCount = deleteFileFormListCount + 1;
                    }
                })
            })
        }
        Object.keys(answers).map(key => {
            if (answers[key]["editable"]) {
                answersContentOnly.push({
                    "answerId": answers[key]["answerId"],
                    "editable": answers[key]["editable"],
                    "judgementResult": answers[key]["judgementResult"],
                    "answerContent": answers[key]["answerContent"],
                    "updateDatetime": answers[key]["updateDatetime"],
                    "CompleteFlag": answers[key]["CompleteFlag"],
                    "judgementInformation": answers[key]["judgementInformation"],
                    "answerFiles": answers[key]["answerFiles"],
                    "reApplicationFlag": answers[key]["reApplicationFlag"]
                })
                // 新規追加ファイル
                Object.keys(answers[key]["uploadFileFormList"]).map(filekey => {
                    if(answers[key]["uploadFileFormList"][filekey] && answers[key]["uploadFileFormList"][filekey].answerId){
                        uploadFileFormList.push(answers[key]["uploadFileFormList"][filekey]);
                    }
                })
                // 更新ファイル・削除ファイル
                Object.keys(answers[key].answerFiles).map(fileKey => {
                    if (answers[key].answerFiles[fileKey]["uploadFileForm"] != null) {
                        uploadFileFormList.push(answers[key].answerFiles[fileKey]["uploadFileForm"]);
                    }
                    if(answers[key].answerFiles[fileKey]["deleteFlag"] == true) {
                        deleteFileFormList.push(answers[key].answerFiles[fileKey]);
                        deleteFileFormListCount++;
                    }
                }
                    )
            }
        })
        fetch(Config.config.apiUrl + "/answer/input", {
            method: 'POST',
            body: JSON.stringify(answersContentOnly),
            headers: new Headers({ 'Content-type': 'application/json' }),
        })
            .then(res => {
                // 401認証エラーの場合の処理を追加
                if (res.status === 401) {
                    alert('認証情報が無効です。ページの再読み込みを行います。');
                    window.location.href = "./login/";
                    return null;
                }
                return res.json();
            })
            .then(res => {
                if (res.status === 201) {
                    //ファイル削除
                    if (deleteFileFormListCount > 0) {
                        Object.keys(deleteFileFormList).map(key => {
                            fetch(Config.config.apiUrl + "/answer/file/delete", {
                                method: 'POST',
                                body: JSON.stringify(deleteFileFormList[key]),
                                headers: new Headers({ 'Content-type': 'application/json' }),
                            })
                                .then(res => {
                                    // 401認証エラーの場合の処理を追加
                                    if (res.status === 401) {
                                        alert('認証情報が無効です。ページの再読み込みを行います。');
                                        window.location.href = "./login/";
                                        return null;
                                    }
                                    return res.json();
                                })                    
                                .then(res => {
                                    deleteFileFormList[key]["status"] = res.status;
                                    let completeFlg = true;
                                    Object.keys(deleteFileFormList).map(innerKey => {

                                        if (!deleteFileFormList[innerKey].status) {
                                            completeFlg = false;
                                        }
                                    })
                                    if (completeFlg) {
                                        this.uploadAnswerFile(uploadFileFormList,notifyCount);
                                    }
                                }).catch(error => {
                                    document.getElementById("customloader_sub_").style.display = "none";
                                    console.error('処理に失敗しました', error);
                                    alert('回答ファイルの削除処理に失敗しました');
                                });
                        })
                    } else {
                        this.uploadAnswerFile(uploadFileFormList,notifyCount);
                    }
                    document.getElementById("customloader_sub_").style.display = "none";
                } else {
                    document.getElementById("customloader_sub_").style.display = "none";
                    alert('回答登録に失敗しました。一度画面を閉じて再度入力を行い実行してください。');
                }
            }).catch(error => {
                document.getElementById("customloader_sub_").style.display = "none";
                console.error('処理に失敗しました', error);
                alert('処理に失敗しました');
            });
    }

    /**
     * 回答ファイル登録
     * @param {object} アップロード対象の回答ファイル一覧
     * @param {number} 通知処理呼び出し回数
     */
    uploadAnswerFile(uploadFileFormList,notifyCount){
        //ファイルアップロード
        if (Object.keys(uploadFileFormList).length > 0) {
            Object.keys(uploadFileFormList).map(key => {
                const formData = new FormData();
                const formDataQuote = new FormData();
                for (const name in uploadFileFormList[key]) {
                    if(uploadFileFormList[key]["uploadFile"] != null){
                        formData.append(name, uploadFileFormList[key][name]);
                    }else{
                        formDataQuote.append(name, uploadFileFormList[key][name])
                    }
                }
                if(formData.entries().next().value !== undefined){
                    fetch(Config.config.apiUrl + "/answer/file/upload", {
                        method: 'POST',
                        body: formData,
                    })
                    .then(res => {
                        // 401認証エラーの場合の処理を追加
                        if (res.status === 401) {
                            alert('認証情報が無効です。ページの再読み込みを行います。');
                            window.location.href = "./login/";
                            return null;
                        }
                        return res.json();
                    })            
                    .then(res => {
                        uploadFileFormList[key]["status"] = res.status;
                        if (res.status !== 201) {
                            alert('回答ファイルのアップロードに失敗しました');
                        }
                        let completeFlg = true;
                        Object.keys(uploadFileFormList).map(key => {
                            if (!uploadFileFormList[key]["status"]) {
                                completeFlg = false;
                            }
                        })
                        if (completeFlg) {
                            notifyCount = notifyCount + 1;
                            this.notify(notifyCount);
                        }
                    }).catch(error => {
                        document.getElementById("customloader_sub_").style.display = "none";
                        console.error('処理に失敗しました', error);
                        alert('回答ファイルのアップロード処理に失敗しました');
                    });
                }

                if(formDataQuote.entries().next().value !== undefined){
                    fetch(Config.config.apiUrl + "/answer/quote/upload", {
                        method: 'POST',
                        body: formDataQuote,
                    })
                    .then(res => {
                        // 401認証エラーの場合の処理を追加
                        if (res.status === 401) {
                            alert('認証情報が無効です。ページの再読み込みを行います。');
                            window.location.href = "./login/";
                            return null;
                        }
                        return res.json();
                    })            
                    .then(res => {
                        uploadFileFormList[key]["status"] = res.status;
                        if (res.status !== 201) {
                            alert('回答ファイルのアップロードに失敗しました');
                        }
                        let completeFlg = true;
                        Object.keys(uploadFileFormList).map(key => {
                            if (!uploadFileFormList[key]["status"]) {
                                completeFlg = false;
                            }
                        })
                        if (completeFlg) {
                            notifyCount = notifyCount + 1;
                            this.notify(notifyCount);
                        }
                    }).catch(error => {
                        document.getElementById("customloader_sub_").style.display = "none";
                        console.error('処理に失敗しました', error);
                        alert('回答ファイルのアップロード処理に失敗しました');
                    });
                }
            })
        } else {
          notifyCount = notifyCount + 1;
          this.notify(notifyCount);  
        }
    }

    /**
     *通知処理
     * @param {number} 通知処理呼び出し回数
     */
    notify(notifyCount){
        if(notifyCount === 1){
            fetch(Config.config.apiUrl + "/label/1002")
            .then(res => {
                // 401認証エラーの場合の処理を追加
                if (res.status === 401) {
                    alert('認証情報が無効です。ページの再読み込みを行います。');
                    window.location.href = "./login/";
                    return null;
                }
                return res.json();
            })    
            .then(res => {
                if (Object.keys(res).length > 0) {
                    this.props.viewState.setCustomMessage(res[0]?.labels?.title, res[0]?.labels?.content)
                    document.getElementById("customloader_sub_").style.display = "none";
                    if(document.getElementById("refreshConfirmApplicationDetails")){
                        document.getElementById("refreshConfirmApplicationDetails").click();
                    }
                    this.props.viewState.nextAnswerCompleteView();
                } else {
                    document.getElementById("customloader_sub_").style.display = "none";
                    alert("labelの取得に失敗しました。");
                }
            }).catch(error => {
                document.getElementById("customloader_sub_").style.display = "none";
                console.error('通信処理に失敗しました', error);
                alert('通信処理に失敗しました');
            });
        }
    }

    /**
     *削除ファイル一覧表示
     * @param {string} entityのid名
     */
    showDeleteAnswerFiles(idName) {
        if (document.getElementById(idName)) {
            document.getElementById(idName).style.display = "block";
        }
    }

    /**
     *削除ファイル一覧非表示
     * @param {string} entityのid名
     */
    closeDeleteAnswerFiles(idName) {
        if (document.getElementById(idName)) {
            document.getElementById(idName).style.display = "none";
        }
    }

    /**
     *ファイルの追加・解除
     * @param {number} 対象回答のkey
     * @param {object} 対象回答ファイル情報
     */
    toggleAnswerFile(key, answerFile) {
        let deleteFileFormList = this.state.deleteFileFormList;
        if (!deleteFileFormList[key]) {
            deleteFileFormList[key] = [];
        }
        const index = deleteFileFormList[key].findIndex(obj =>
            obj.answerFileId == answerFile.answerFileId
        );
        if (index < 0) {
            deleteFileFormList[key].push(answerFile);
        } else {
            deleteFileFormList[key].splice(index, 1);
        }
        this.setState({ deleteFileFormList: deleteFileFormList });
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
            content.style.top = (event.clientY + (parseInt(content.clientHeight) / 2) - 10) + 'px';
            content.style.left = event.clientX + 'px';
        }
    }

    /**
     * ファイル追加
     * @param {*} target 申請ファイル一覧 
     * @param {*} index 回答ファイルインデックス番号（新規の場合null）
     */
    output(target, index) {
        this.setState({
            updateAnswerFileKey: index
        });
        this.state.viewState.setQuoteFiles(target);
        this.state.viewState.changeFileUploadModalShow();
    }

    /**
     * 回答キャンセルボタン
     * @param {*} active アクティブページ
     */
    chancelAnswerInput(active){
        this.state.viewState.changeApplyPageActive(active);
    }

    /**
     * ファイル取得
     * @param {*} path ファイルパス
     * @param {*} name ファイル名
     * @param {*} objectUrl ファイルデータのURL
     * @param {*} version 版情報
     * @param {*} pdfEditable PDFファイル編集可否
     * @param {*} addFile ファイル追加か否か
     */
    showFile(path, name, objectUrl, version, pdfEditable, addFile){
        this.setState({ 
            targetFilePath: path,
            targetFile: name,
            targetFileUrl: objectUrl,
            targetAppFileVersion: version,
            addFile: addFile
        });
        if(path, name){
            this.setState({ pdfEditable: pdfEditable}, this.fileOpen)
        }
    }

    /**
     * ファイル表示
     */
    fileOpen = () =>{
        this.setState({ fileOpen: true });
        this.props.viewState.setShowPdfViewer(true);
    }

    /**
     * ファイル閉じる
     * @param {*} addFile ファイル追加か否か
     */  
    fileClose = (addFile) =>{
        this.setState({ fileOpen: false});
        this.props.viewState.setShowPdfViewer(false);

        let addFileModalFlag = addFile !== null ? addFile : this.state.addFile;
        if(addFileModalFlag){
            this.output(this.state.applicationFiles, null)
        }
        
        if(this.state.pdfConvertFlag){
            // PDFビューワの再表示
            if(this.state.quoteFileData){
                let quote = this.state.quoteFileData;
                console.log(quote);
                this.showFile(quote.filePath, quote.fileName, null, quote.fileVersion, true, true);                
            }

            // PDF変換フラグの初期化
            this.setState({pdfConvertFlag: false});
        }
    }

    /**
     * PDF→画像変換のコールバック関数
     * @param {*} file ファイル実体
     * @param {*} fileName ファイル名
     */
    addFile = (file, fileName) => {
        const answers = this.state.answers;
        const key = this.state.selectedAnswerKey;
        const answerFileKey = this.state.updateAnswerFileKey;
        if (key != null) {
            let reader = new FileReader();
            reader.readAsDataURL(file);
            reader.onload = (e) => {
                let uploadFileUrl = e.target.result;

                // answers[key]["uploadFileFormList"] = answers[key]["uploadFileFormList"].filter((uploadAnswerFile) => uploadAnswerFile.answerFileName !== fileName);
                // answers[key]["uploadFileFormList"].push({ "answerFileId": "", "answerId": answers[key].answerId, "answerFileName": fileName, "filePath": null, "uploadFile": file, "uploadFileUrl": uploadFileUrl });
                // this.setState({
                //     answers: answers,
                //     selectedAnswer: answers[key]
                // });

                // PDFファイル削除
                this.removeNewFile(this.state.targetFile);

                // PDF変換フラグ設定
                this.setState({ pdfConvertFlag: true}, console.log(this.state.pdfConvertFlag));

                // 画像変換後に画像ビュワーを表示する
                this.showFile(null, fileName, uploadFileUrl, null, false, false);
            }
        }
    }

    /**
     * 編集した画像を保存する
     * @param {*} file ファイル実体
     * @param {*} fileName ファイル名
     */
    fileSave = (file, fileName) => {
        const answers = this.state.answers;
        const key = this.state.selectedAnswerKey;
        if(key != null){
            let reader = new FileReader();
            reader.readAsDataURL(file);
            reader.onload = (e) => {
                // 更新
                let uploadFileUrl = e.target.result;
                // answerFileNameに存在する場合
                if(answers[key]?.answerFiles.some(answerFile => answerFile.answerFileName.includes(fileName))){
                    let answerFileIndex = answers[key]?.answerFiles.findIndex(answerFile => answerFile.answerFileName.includes(fileName));
                    let answerFile = answers[key]?.answerFiles[answerFileIndex];
                    answers[key].answerFiles[answerFileIndex].uploadFileForm = {"answerFileId": answerFile.answerFileId, "answerId": answers[key].answerId, "answerFileName": fileName, "filePath":  null, "uploadFile": file, "uploadFileUrl": uploadFileUrl}
                }else{
                    // answerFileNameに存在しない場合
                    //let answerFileIndex = answers[key]["uploadFileFormList"].findIndex(uploadFile => uploadFile.answerFileName.includes(fileName));
                    this.removeNewFile(fileName);
                    answers[key]["uploadFileFormList"].push({ "answerFileId": "", "answerId": answers[key].answerId, "answerFileName": fileName, "filePath": null, "uploadFile": file, "uploadFileUrl": uploadFileUrl });
                }
                this.setState({
                    answers: answers,
                    selectedAnswer: answers[key],
                    targetFilePath: null,
                    targetFile: fileName,
                    targetFileUrl: uploadFileUrl,
                    targetAppFileVersion: null
                });
            }
        }
    }

    /**
     * 新規追加したファイルを削除する
     * @param {*} fileName ファイル名
     */
    removeNewFile(fileName) {
        let selectedAnswer = this.state.selectedAnswer;
        let answers = this.state.answers;
        let selectedIndex = this.state.selectedAnswerKey;
        selectedAnswer.uploadFileFormList = selectedAnswer.uploadFileFormList.filter((file) => (file.answerFileName !== fileName));
        answers[selectedIndex] = selectedAnswer;
        this.setState({
            selectedAnswer: selectedAnswer,
            answers: answers
        });
    }

    /**
     * 既存のファイルを削除する
     * @param {*} index 回答ファイルリストのindex
     * @param {*} deleteFlag 削除フラグ
     */
    removeExistFile(index, deleteFlag) {
        let selectedAnswer = this.state.selectedAnswer;
        let answers = this.state.answers;
        let selectedIndex = this.state.selectedAnswerKey;
        selectedAnswer.answerFiles[index].deleteFlag = !deleteFlag;
        if (!deleteFlag) {
            // 更新アップロードファイルも削除する
            selectedAnswer.answerFiles[index]["uploadFileForm"] = null;
        }
        answers[selectedIndex] = selectedAnswer;
        this.setState({
            selectedAnswer: selectedAnswer,
            answers: answers
        });
        
    }

    /**
     * 対象選択
     * @param {*} answer 回答対象
     * @param {*} key ループのキー
     */
    handleRowClick(answer, key){
        this.setState({ 
            selectedAnswer: answer,
            selectedAnswerKey: key
        });
    }

    /**
     * 回答入力モダール表示
     * @param {*} answer 回答対象
     */
    showTemplate(answer) {
        this.props.viewState.changeAnswerContentInputModalShow();
        this.props.viewState.setAnswerTemplateTarget(answer);
    }

    render() {
        const answers = this.state.answers;
        const applicationFiles = this.state.applicationFiles;
        const deleteFileFormList = this.state.deleteFileFormList;
        const t = this.props.t;
        return (
            <>
            <div className={Styles.div_area}>
                <ShowMessage t={t} message={"adminInfoMessage.tipsForAnswerReister"} />
                
                {/* <AdminTab terria={this.props.terria} viewState={this.props.viewState} t={t}/> */}
            </div>
            <Box
                displayInlineBlock
                backgroundColor={this.props.theme.textLight}
                fullWidth
                fullHeight
                // onClick={() =>
                //     this.props.viewState.setTopElement("AnswerInput")}
        //         css={`
        //   position: fixed;
        //   z-index: 9992;
        // `}
                // className={CustomStyle.custom_frame}
            >
                <div id="customloader_sub_" className={CustomStyle.customloaderParent}>
                    <div className={CustomStyle.customloader}>Loading...</div>
                </div>
                <nav className={CustomStyle.custom_nuv}>
                    回答入力
                </nav>
                <Box
                    centered
                    paddedHorizontally={5}
                    paddedVertically={10}
                    displayInlineBlock
                    className={CustomStyle.custom_content}
                >
                    <Spacing bottom={1} />
                    <p className={CustomStyle.explanation}>回答を入力してください。</p>
                    <Spacing bottom={3} />
                    <div className={CustomStyle.scrollContainer}>
                        <div style={{ maxHeight: "400px", overflowY: "auto"}}>
                            <table className={CustomStyle.selection_table}>
                                <thead>
                                    <tr className={CustomStyle.table_header}>
                                        <th style={{ width: 20 + "%" }}>対象</th>
                                        <th style={{ width: 10 + "%" }}>担当課</th>
                                        <th style={{ width: 20 + "%" }}>判定結果</th>
                                        <th style={{ width: 30 + "%" }}>回答内容</th>
                                        <th style={{ width: 10 + "%" }}>添付</th>
                                        <th style={{ width: 10 + "%" }}>区分</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {Object.keys(answers).map(key => (
                                        <tr onClick={e=> this.handleRowClick(answers[key], key)}>
                                            <td>{answers[key]?.judgementInformation?.title}</td>
                                            <td>{answers[key]?.judgementInformation?.department?.departmentName}</td>
                                            <td>{answers[key]?.judgementResult}</td>
                                            <td>
                                                {answers[key]?.editable && (
                                                    <div>
                                                        {answers[key]?.answerTemplate.length > 0 && (
                                                            <button
                                                            className={CustomStyle.template_button}
                                                            style={{ margin: "0 10px"}}
                                                            onClick={e => {
                                                            this.showTemplate(answers[key]);
                                                            }}
                                                        >回答入力</button>
                                                        )}
                                                        <textarea 
                                                        id={"answer_input_" + answers[key]?.answerId}
                                                        style={{
                                                        background: "rgba(0,0,0,0.15)",
                                                        border: "none",
                                                        borderRadius: 2 + "px",
                                                        width: 97 + "%",
                                                        resize: "none",
                                                        border: "2px solid black"
                                                        }} rows="5" type="text" placeholder="" value={answers[key]?.answerContent}
                                                        autoComplete="off"
                                                        onChange={e => this.inputChange(key, e.target.value)}
                                                        ></textarea>
                                                    </div>
                                                )}
                                                {!answers[key]?.editable && (
                                                    answers[key]?.answerContent
                                                )}
                                            </td>
                                            <td>
                                                {(answers[key]?.answerFiles?.length > 0 || answers[key]?.uploadFileFormList.length > 0)
                                                && (
                                                    <div className={Styles.ellipse}>
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
                                                {answers[key]?.editable && (
                                                    <select value={answers[key]?.reApplicationFlag}
                                                    onChange={e => this.reapplicationChange(key, e.target.value)}>
                                                        <option></option>
                                                        <option value={true}>要再申請</option>
                                                        <option value={false}>再申請不要</option>
                                                    </select>
                                                )}
                                                {!answers[key]?.editable&& (
                                                    <select disabled value={answers[key]?.reApplicationFlag}>
                                                        <option></option>
                                                        <option value={true}>要再申請</option>
                                                        <option value={false}>再申請不要</option>
                                                    </select>
                                                )}
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                        <Spacing bottom={3} />
                        <nav className={CustomStyle.custom_nuv}>添付ファイル一覧</nav>
                        <Spacing bottom={2} />
                        {this.state.selectedAnswer != null && (
                            <div style={{ display: "flex", justifyContent: "start"}}>
                            {this.state.selectedAnswer?.editable && (
                                <button
                                className={CustomStyle.download_button}
                                style={{ margin: "0 10px"}}
                                onClick={e => {
                                    this.output(applicationFiles, null);
                                }}
                                >追加</button>
                            )}

                            <p style={{ margin: "auto 0", padding: "0"}}>{`選択中の対象：${this.state.selectedAnswer?.judgementInformation?.title}`}</p>
                        </div>
                        )}
                        <Spacing bottom={2} />
                        <div style={{ maxHeight: "300px", overflowY: "auto"}}>
                            <table className={CustomStyle.selection_table}>
                                <thead>
                                    <tr className={CustomStyle.table_header}>
                                        <th style={{ width: 10 + "%" }}>拡張子</th>
                                        <th style={{ width: 40 + "%" }}>ファイル名</th>
                                        <th style={{ width: 50 + "%" }}></th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {/** 既存のファイルを表示 */}
                                    {this.state.selectedAnswer && Object.keys(this.state.selectedAnswer?.answerFiles).map(file => (
                                        <tr>
                                            <td>{this.state.selectedAnswer?.answerFiles[file]?.answerFileName.split(".").pop()}</td>
                                            {this.state.selectedAnswer?.answerFiles[file]?.deleteFlag == true && (
                                                <td><p style={{textDecorationLine: 'line-through', textDecorationStyle: 'solid'}}>{this.state.selectedAnswer?.answerFiles[file]?.answerFileName}</p></td>
                                            )}
                                            {this.state.selectedAnswer?.answerFiles[file]?.deleteFlag == false && (
                                                <td>{this.state.selectedAnswer?.answerFiles[file]?.answerFileName}</td>
                                                )}
                                            
                                            <td style={{ display: "flex", justifyContent: "start"}}>
                                                {this.state.selectedAnswer?.editable && (
                                                    <div style={{ display: "flex"}}>                                                        
                                                        {this.state.selectedAnswer?.answerFiles[file]?.deleteFlag == false && this.state.editableExtentions.includes(this.state.selectedAnswer?.answerFiles[file]?.answerFileName.split(".").pop()) && (
                                                            <button
                                                                className={CustomStyle.edit_button}
                                                                style={{ margin: "0 10px"}}
                                                                onClick={e => this.showFile(
                                                                    !this.state.selectedAnswer?.answerFiles[file]?.uploadFileForm ? this.state.selectedAnswer?.answerFiles[file]?.answerFilePath : this.state.selectedAnswer?.answerFiles[file]?.uploadFileForm.filePath,
                                                                    this.state.selectedAnswer?.answerFiles[file]?.answerFileName,
                                                                    !this.state.selectedAnswer?.answerFiles[file]?.uploadFileForm ? null : this.state.selectedAnswer?.answerFiles[file]?.uploadFileForm.uploadFileUrl,
                                                                    null,
                                                                    false,
                                                                    false
                                                                )}
                                                            >
                                                            {this.state.selectedAnswer?.answerFiles[file]?.answerFileName.split(".").pop() !== "pdf" && (
                                                                <>表示/編集</>
                                                            )}
                                                            {this.state.selectedAnswer?.answerFiles[file]?.answerFileName.split(".").pop() === "pdf" && (
                                                                <>表示</>
                                                            )}
                                                            </button>
                                                        )}
                                                        {this.state.selectedAnswer?.answerFiles[file]?.deleteFlag == false && (
                                                            <button className={CustomStyle.delete_button} style={{ margin: "0 10px"}} onClick={e=>{this.removeExistFile(file, false)}}>削除</button>
                                                        )}
                                                        
                                                        {this.state.selectedAnswer?.answerFiles[file]?.deleteFlag == true && (
                                                            <button className={CustomStyle.delete_button} style={{ margin: "0 10px"}} onClick={e=>{this.removeExistFile(file, true)}}>元に戻す</button>
                                                        )}
                                                    </div>
                                                )}
                                            </td>
                                        </tr>
                                            ))}
                                    {/** 新規追加したファイルを表示 */}
                                    {this.state.selectedAnswer?.editable && this.state.selectedAnswer && Object.keys(this.state.selectedAnswer?.uploadFileFormList).map(file => (
                                        <tr>
                                            <td>{this.state.selectedAnswer?.uploadFileFormList[file]?.answerFileName.split(".").pop()}</td>
                                            <td>{this.state.selectedAnswer?.uploadFileFormList[file]?.answerFileName}</td>
                                            <td style={{ display: "flex", justifyContent: "start"}}>
                                            {this.state.editableExtentions.includes(this.state.selectedAnswer?.uploadFileFormList[file]?.answerFileName.split(".").pop()) && 
                                                <button className={CustomStyle.edit_button} style={{ margin: "0 10px"}} onClick={e => this.showFile(
                                                    this.state.selectedAnswer?.uploadFileFormList[file]?.filePath,
                                                    this.state.selectedAnswer?.uploadFileFormList[file]?.answerFileName,
                                                    this.state.selectedAnswer?.uploadFileFormList[file]?.uploadFileUrl,
                                                    this.state.selectedAnswer?.uploadFileFormList[file]?.version,
                                                    false,
                                                    false
                                                )}>
                                                    {this.state.selectedAnswer?.uploadFileFormList[file]?.answerFileName.split(".").pop() !== "pdf" && (
                                                        <>表示/編集</>
                                                    )}
                                                    {this.state.selectedAnswer?.uploadFileFormList[file]?.answerFileName.split(".").pop() === "pdf" && (
                                                        <>表示</>
                                                    )}
                                                </button>
                                            }
                                            <button className={CustomStyle.delete_button} style={{ margin: "0 10px"}} onClick={e=>{this.removeNewFile(this.state.selectedAnswer?.uploadFileFormList[file]?.answerFileName)}}>削除</button>
                                            </td>
                                        </tr>
                                    ))}

                                </tbody>
                            </table>
                        </div>                        
                    </div>
                    <Spacing bottom={3} />
                    <div className={CustomStyle.box}>
                        <div className={CustomStyle.item} style={{ width: 48 + "%" }}>
                            <button
                                className={CustomStyle.back_button}
                                onClick={e => {
                                    this.props.viewState.hideAnswerInputView();
                                    this.chancelAnswerInput("applyDetail");
                                }}
                            >
                                <span>回答キャンセル</span>
                            </button>
                        </div>
                        <div className={CustomStyle.item} style={{ width: 48 + "%" }}>
                            <button
                                className={CustomStyle.next_button}
                                onClick={e => {
                                    this.register();
                                }}
                            >
                                <span>回答登録</span>
                            </button>
                        </div>
                    </div>
                    <Spacing bottom={3} />
                    { this.state.viewState.fileUploadModalShow && (
                        <FileUploadModal terria={this.props.terria} viewState={this.props.viewState} t={t} answerFiles={this.state.selectedAnswer?.answerFiles} callback={this.fileUploadCallback}/>
                    )}
                </Box>
            </Box >
            {/* { this.state.viewState.inputAnswerTemplateModalShow && (
                <AnswerTemplateModal terria={this.props.terria} viewState={this.props.viewState} t={t} callback={this.inputFromTemplate}/>
            )} */}
            { this.state.viewState.inputAnswerContentModalShow && (
                <AnswerContentInputModal terria={this.props.terria} viewState={this.props.viewState} t={t} callback={this.inputFromTemplate} />
            )}
            { this.state.fileOpen && this.state.targetFile && this.state.targetFile.split(".").pop() === "pdf" && (
                <PdfViewModal
                    path={this.state.targetFilePath}
                    name={this.state.targetFile}
                    url={this.state.targetFileUrl}
                    appFileVersion={this.state.targetAppFileVersion}
                    fileClose={this.fileClose}
                    addFile={this.addFile}
                    fileDelete={this.removeNewFile}
                    t={t}  
                    pdfEditable={this.state.pdfEditable}
                    addFileFlag={this.state.addFile}
                />
            )}
            { this.state.fileOpen && this.state.targetFile
                && (this.state.targetFile.split(".").pop() === "png" 
                || this.state.targetFile.split(".").pop() === "jpg"
                || this.state.targetFile.split(".").pop() === "jpeg" 
                || this.state.targetFile.split(".").pop() === "tiff" 
                || this.state.targetFile.split(".").pop() === "tif") && (
                <ImageEdit
                    path={this.state.targetFilePath}
                    name={this.state.targetFile}
                    objectUrl={this.state.targetFileUrl}
                    fileClose={this.fileClose}
                    fileSave={this.fileSave}
                    addFile={this.state.addFile}
                />
            )}
        </>
        );
        
    }
}
export default withTranslation()(withTheme(AnswerInput));