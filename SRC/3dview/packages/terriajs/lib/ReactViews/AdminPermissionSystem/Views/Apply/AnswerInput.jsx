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
import PageStyle from "../../../DevelopmentPermissionSystem/PageViews/scss/pageStyle.scss";
import Config from "../../../../../customconfig.json";
import FileUploadModal from "../Modal/FileUploadModal";
import ImageEdit from "../ImageEdit/ImageEdit";
import PdfViewModal from "../Modal/PdfViewModal";
import Tiff from "tiff.js";
import AnswerContentList from "../../../DevelopmentPermissionSystem/Views/Answer/AnswerContentList";
import NegotiationContentList from "../../../DevelopmentPermissionSystem/Views/Answer/NegotiationContentList";
import AssessmentContentList from "../../../DevelopmentPermissionSystem/Views/Answer/AssessmentContentList"
import ApplicantInformation from "../../../DevelopmentPermissionSystem/Views/Answer/ApplicantInformation";

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
            //1=事前相談,2=事前協議,3=許可判定
            checkedApplicationStepId:props.viewState.checkedApplicationStepId,
            checkedApplicationStepName:"",
            //申請者情報
            applicantInformations: [],
            //申請区分
            checkedApplicationCategory: [],
            //申請地番
            lotNumbers: [],
            //申請状態
            status: "",
            //回答
            answers: [],
            //部署回答一覧
            departmentAnswers:[],
            //申請ファイル
            applicationFiles: [],
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
            },
            //申請種類
            checkedApplicationType: {},
            //申請追加情報
            applicantAddInformations: [],
            //協議対象一覧
            ledgerMaster:[],
            //回答ファイル（申請段階単位）
            answerFile:[],
            //回答ファイルアップロード対象
            uploadFileFormList:[],
            // エラーレコード
            errors:{answers:[],departmentAnswers:[]},
            // 行政で追加された回答のindex(事前協議のみ)
            addAnswerIndex:0

        };
        this.mapBaseElement = React.createRef();
        this.mpBaseContainerElement =React.createRef();
    }

    /**
     * 初期処理
     */
    componentDidMount() {
        if (this.props.viewState.applicationInformationSearchForApplicationId) {
            document.getElementById("customloader_main").style.display = "block";
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
                        let checkedApplicationStepId = this.state.checkedApplicationStepId;
                        let checkedApplicationStepName = this.state.checkedApplicationStepName;
                        let answers = this.state.answers;
                        let departmentAnswers = this.state.departmentAnswers;
                        let applicationFiles = this.state.applicationFiles;
                        let answerFile = this.state.answerFile;
                        let applicantAddInformations = this.state.applicantAddInformations;
                        let checkedApplicationCategory = this.state.checkedApplicationCategory;
                        let ledgerMaster = this.state.ledgerMaster;

                        Object.keys(res.applyAnswerDetails).map(key => {
                            checkedApplicationStepId = res.applyAnswerDetails[0].applicationStepId;;
                            let applyAnswerDetailForm = res.applyAnswerDetails[key];
                            if(applyAnswerDetailForm.applicationStepId == checkedApplicationStepId && ((checkedApplicationStepId == 2 && !applyAnswerDetailForm.isAcceptInfo)|| (checkedApplicationStepId != 2))){
                                //回答ファイルが新規追加できるため、uploadFileFormList項目を設置
                                // 申請段階単位（許可判定）
                                Object.keys(applyAnswerDetailForm.answerFiles).map(file => {
                                    applyAnswerDetailForm.answerFiles[file]["uploadFileForm"] = null;
                                    applyAnswerDetailForm.answerFiles[file]["deleteFlag"] = false;
                                });

                                // 部署回答単位（事前協議）
                                Object.keys(applyAnswerDetailForm.departmentAnswers).map(key => {
                                    // 新規追加用リスト
                                    applyAnswerDetailForm.departmentAnswers[key]["uploadFileFormList"] = [];
                                    Object.keys(applyAnswerDetailForm.departmentAnswers[key].answerFiles).map(file => {
                                        applyAnswerDetailForm.departmentAnswers[key].answerFiles[file]["uploadFileForm"] = null;
                                        applyAnswerDetailForm.departmentAnswers[key].answerFiles[file]["deleteFlag"] = false;
                                    })
                                });

                                // 回答単位（事前相談のみ）
                                if(checkedApplicationStepId == 1){
                                    Object.keys(applyAnswerDetailForm.answers).map(key => {
                                        applyAnswerDetailForm.answers[key]["uploadFileFormList"] = [];
                                        Object.keys(applyAnswerDetailForm.answers[key].answerFiles).map(file => {
                                            applyAnswerDetailForm.answers[key].answerFiles[file]["uploadFileForm"] = null;
                                            applyAnswerDetailForm.answers[key].answerFiles[file]["deleteFlag"] = false;
                                        })
                                    });
                                }

                                checkedApplicationStepName = applyAnswerDetailForm.applicationStepName;
                                checkedApplicationCategory = applyAnswerDetailForm.applicationCategories;
                                applicantAddInformations = applyAnswerDetailForm.applicantAddInformations;
                                applicationFiles = applyAnswerDetailForm.applicationFiles;
                                answerFile = applyAnswerDetailForm.answerFiles;
                                answers = applyAnswerDetailForm.answers;
                                departmentAnswers = applyAnswerDetailForm.departmentAnswers;
                                Object.keys(departmentAnswers).map(key1 => {
                                    Object.keys(departmentAnswers[key1].answers).map(key2 => {
                                        departmentAnswers[key1].answers[key2]["answerDataType_bak"] = departmentAnswers[key1].answers[key2].answerDataType;
                                    })
                                })
                                ledgerMaster = applyAnswerDetailForm.ledgerMasters;
                            }
                        })
                        this.setState({
                            answers: answers,
                            departmentAnswers: departmentAnswers,
                            applicationFiles: applicationFiles,
                            applicantInformations: res.applicantInformations,
                            applicantAddInformations: applicantAddInformations,
                            checkedApplicationCategory: checkedApplicationCategory,
                            answerFile: answerFile,
                            lotNumbers: res.lotNumbers,
                            status: res.status,
                            checkedApplicationStepName:checkedApplicationStepName,
                            checkedApplicationType: res.applicationType,
                            ledgerMaster: ledgerMaster
                        });
                    } else {
                        alert("回答の取得に失敗しました。再度操作をやり直してください。");
                    }
                    this.props.viewState.setLotNumbers(res.lotNumbers);
                    this.props.viewState.setMapBaseElement(this.mapBaseElement);
                    this.props.viewState.setMapBaseContainerElement(this.mpBaseContainerElement);
                    setTimeout(() => {
                        this.props.viewState.updateMapDimensions();
                        this.focusMapPlaceDriver();
                    }, 1000);
                }).catch(error => {
                    console.error('通信処理に失敗しました', error);
                    alert('通信処理に失敗しました');
                }).finally(() => document.getElementById("customloader_main").style.display = "none");
        } else {
            alert("回答の取得に失敗しました。再度操作をやり直してください。");
            document.getElementById("customloader_main").style.display = "none";
        }

    }

    /**
     * フォーカス処理ドライバー
     */
    focusMapPlaceDriver(){
        if(!this.state.lotNumbers) return;
        let applicationPlace = Object.assign({}, this.state.lotNumbers);
        applicationPlace = Object.values(applicationPlace);
        applicationPlace = applicationPlace.filter(Boolean);
        let maxLon = 0;
        let maxLat = 0;
        let minLon = 0;
        let minLat = 0;
        Object.keys(applicationPlace).map(key => {
            const targetMaxLon = parseFloat(applicationPlace[key].maxlon);
            const targetMaxLat = parseFloat(applicationPlace[key].maxlat);
            const targetMinLon = parseFloat(applicationPlace[key].minlon);
            const targetMinLat = parseFloat(applicationPlace[key].minlat);
            if (key === 0 || key === "0") {
                maxLon = targetMaxLon;
                maxLat = targetMaxLat;
                minLon = targetMinLon;
                minLat = targetMinLat;
            } else {
                if (maxLon < targetMaxLon) {
                    maxLon = targetMaxLon;
                }
                if (maxLat < targetMaxLat) {
                    maxLat = targetMaxLat;
                }
                if (minLon > targetMinLon) {
                    minLon = targetMinLon;
                }
                if (minLat > targetMinLat) {
                    minLat = targetMinLat;
                }
            }
        })
        this.outputFocusMapPlace(maxLon, maxLat, minLon, minLat, (maxLon + minLon) / 2, (maxLat + minLat) / 2);
    }

    /**
     * フォーカス処理
     * @param {number} maxLon 最大経度
     * @param {number} maxLat 最大緯度
     * @param {number} minLon 最小経度
     * @param {number} minLat 最小緯度
     * @param {number} lon 経度
     * @param {number} lat 緯度
     */
    outputFocusMapPlace(maxLon, maxLat, minLon, minLat, lon, lat){
        this.props.terria.focusMapPlace(maxLon, maxLat, minLon, minLat, lon, lat, this.props.viewState);
    }

    /**
     * ファイルアップロードコールバック関数
     * @param {*} file ファイル実体（アップロード時に使用）
     * @param quote 引用する申請ファイル情報
     * @param answerQuote 引用する回答ファイル情報
     */
    fileUploadCallback = (file, quote, answerQuote) => {
        // 申請段階
        const checkedApplicationStepId =  this.state.checkedApplicationStepId; 
        const answers =  [...this.state.answers];
        const departmentAnswers =  [...this.state.departmentAnswers];
        const key =  this.state.selectedAnswerKey;
        const answerFileKey =  this.state.updateAnswerFileKey;
        const applicationId =  this.props.viewState.applicationInformationSearchForApplicationId;
        if (key != null && checkedApplicationStepId !== 3) {
            if (file != null) {
                let reader = new FileReader();
                // アップロードされたファイルがPDFファイルの場合
                if (file.type === 'application/pdf'){
                    reader.readAsArrayBuffer(file);
                    reader.onload = (e) => {
                        let blob = new Blob([e.target.result], { type: file.type });
                        let uploadFileUrl = URL.createObjectURL(blob);
                        
                        // 事前相談
                        if(checkedApplicationStepId == 1){
                            if (!answers[key]?.answerFiles.some(answerFile => answerFile.answerFileName.includes(file.name))) {
                                // 新規追加
                                answers[key]["uploadFileFormList"] = answers[key]["uploadFileFormList"].filter((uploadAnswerFile) => uploadAnswerFile.answerFileName !== file.name);
                                answers[key]["uploadFileFormList"].push({ "answerFileId": "", "answerId": answers[key].answerId, "answerFileName": file.name, "filePath":  null, "uploadFile": file, "uploadFileUrl": uploadFileUrl, "applicationId": applicationId, "applicationStepId": checkedApplicationStepId, "departmentId": "-1"});
                            } else{
                                let answerFileIndex = answers[key]?.answerFiles.findIndex(answerFile => answerFile.answerFileName.includes(file.name));
                                let answerFile = answers[key]?.answerFiles[answerFileIndex];
                                answers[key].answerFiles[answerFileIndex].uploadFileForm = {"answerFileId": answerFile.answerFileId, "answerId": answers[key].answerId, "answerFileName": file.name, "filePath":  null, "uploadFile": file, "uploadFileUrl": uploadFileUrl, "applicationId": applicationId, "applicationStepId": checkedApplicationStepId, "departmentId": "-1"};
                            }
                            this.setState({
                                answers: answers,
                                selectedAnswer: answers[key],
                                quoteFile: false
                            });
                        }
                        // 事前協議
                        if(checkedApplicationStepId == 2){
                            if (!departmentAnswers[key]?.answerFiles.some(answerFile => answerFile.answerFileName.includes(file.name))) {
                                // 新規追加
                                departmentAnswers[key]["uploadFileFormList"] = departmentAnswers[key]["uploadFileFormList"].filter((uploadAnswerFile) => uploadAnswerFile.answerFileName !== file.name);
                                departmentAnswers[key]["uploadFileFormList"].push({ "answerFileId": "", "answerId": 0, "answerFileName": file.name, "filePath":  null, "uploadFile": file, "uploadFileUrl": uploadFileUrl, "applicationId": applicationId, "applicationStepId": checkedApplicationStepId, "departmentId": departmentAnswers[key].department.departmentId});
                            } else{
                                let answerFileIndex = departmentAnswers[key]?.answerFiles.findIndex(answerFile => answerFile.answerFileName.includes(file.name));
                                let answerFile = departmentAnswers[key]?.answerFiles[answerFileIndex];
                                departmentAnswers[key].answerFiles[answerFileIndex].uploadFileForm = {"answerFileId": answerFile.answerFileId, "answerId": 0, "answerFileName": file.name, "filePath":  null, "uploadFile": file, "uploadFileUrl": uploadFileUrl, "applicationId": applicationId, "applicationStepId": checkedApplicationStepId, "departmentId": departmentAnswers[key].department.departmentId};
                            }
                            this.setState({
                                departmentAnswers: departmentAnswers,
                                selectedAnswer: departmentAnswers[key],
                                quoteFile: false
                            });
                        }

                    }
                }
                // アップロードされたファイルが画像ファイルの場合
                if(file.type === 'image/png' || file.type === 'image/jpeg'){
                    reader.readAsDataURL(file);
                    reader.onload = (e) => {
                        let uploadFileUrl = e.target.result;
                        this.showFile(null, file.name, uploadFileUrl, null, false, true);
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

                    // 事前相談
                    if(checkedApplicationStepId == 1){
                        if (!answers[key]?.answerFiles.some(answerFile => answerFile.answerFileName.includes(file.name))) {
                            // 新規追加
                            answers[key]["uploadFileFormList"] = answers[key]["uploadFileFormList"].filter((uploadAnswerFile) => uploadAnswerFile.answerFileName !== file.name);
                            answers[key]["uploadFileFormList"].push({ "answerFileId": "", "answerId": answers[key].answerId, "answerFileName": file.name, "filePath": null, "uploadFile": file, "applicationId": applicationId, "applicationStepId": checkedApplicationStepId, "departmentId": "-1"});
                        }else{
                            // 更新
                            let answerFile = answers[key]?.answerFiles[answerFileKey];
                            answers[key].answerFiles[answerFileKey].uploadFileForm = {"answerFileId": answerFile.answerFileId, "answerId": answers[key].answerId, "answerFileName": file.name, "filePath":  null, "uploadFile": file, "applicationId": applicationId, "applicationStepId": checkedApplicationStepId, "departmentId": "-1"};
                        }
                        this.setState({
                            answers: answers,
                            selectedAnswer: answers[key],
                            quoteFile: false
                        });
                    }

                    // 事前協議
                    if(checkedApplicationStepId == 2){
                        if (!departmentAnswers[key]?.answerFiles.some(answerFile => answerFile.answerFileName.includes(file.name))) {
                            // 新規追加
                            departmentAnswers[key]["uploadFileFormList"] = departmentAnswers[key]["uploadFileFormList"].filter((uploadAnswerFile) => uploadAnswerFile.answerFileName !== file.name);
                            departmentAnswers[key]["uploadFileFormList"].push({ "answerFileId": "", "answerId": 0, "answerFileName": file.name, "filePath": null, "uploadFile": file, "applicationId": applicationId, "applicationStepId": checkedApplicationStepId, "departmentId": departmentAnswers[key].department.departmentId});
                        }else{
                            // 更新
                            let answerFile = departmentAnswers[key]?.answerFiles[answerFileKey];
                            departmentAnswers[key].answerFiles[answerFileKey].uploadFileForm = {"answerFileId": answerFile.answerFileId, "answerId": 0, "answerFileName": file.name, "filePath":  null, "uploadFile": file, "applicationId": applicationId, "applicationStepId": checkedApplicationStepId, "departmentId": departmentAnswers[key].department.departmentId};
                        }
                        this.setState({
                            departmentAnswers: departmentAnswers,
                            selectedAnswer: departmentAnswers[key],
                            quoteFile: false
                        });
                    }
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
                    }else{
                        // 事前相談
                        if(checkedApplicationStepId == 1){
                            if(!answers[key]?.answerFiles.some(answerFile => answerFile.answerFileName.includes(fileName))){
                                //新規追加
                                answers[key]["uploadFileFormList"] = answers[key]["uploadFileFormList"].filter((uploadAnswerFile) => uploadAnswerFile.answerFileName !== quote.uploadFileName);
                                answers[key]["uploadFileFormList"].push({ "answerFileId": "", "answerId": answers[key].answerId, "answerFileName": fileName, "filePath": quote.filePath, "version": quote.versionInformation, "applicationId": applicationId, "applicationStepId": checkedApplicationStepId, "departmentId": "-1"});
                            }else{
                                //更新
                                let answerFileIndex = answers[key]?.answerFiles.findIndex(answerFile => answerFile.answerFileName.includes(fileName));
                                let answerFile = answers[key]?.answerFiles[answerFileIndex];
                                answers[key].answerFiles[answerFileIndex].uploadFileForm = {"answerFileId": answerFile.answerFileId, "answerId": answers[key].answerId, "answerFileName": fileName, "filePath": quote.filePath, "version": quote.versionInformation, "applicationId": applicationId, "applicationStepId": checkedApplicationStepId, "departmentId": "-1"};
                            }
                            this.setState({
                                answers: answers,
                                selectedAnswer: answers[key],
                                quoteFile: true
                            });
                        }
                        // 事前協議
                        if(checkedApplicationStepId == 2){
                            if(!departmentAnswers[key]?.answerFiles.some(answerFile => answerFile.answerFileName.includes(fileName))){
                                //新規追加
                                departmentAnswers[key]["uploadFileFormList"] = departmentAnswers[key]["uploadFileFormList"].filter((uploadAnswerFile) => uploadAnswerFile.answerFileName !== quote.uploadFileName);
                                departmentAnswers[key]["uploadFileFormList"].push({ "answerFileId": "", "answerId": 0, "answerFileName": fileName, "filePath": quote.filePath, "version": quote.versionInformation, "applicationId": applicationId, "applicationStepId": checkedApplicationStepId, "departmentId": departmentAnswers[key].department.departmentId});
                            }else{
                                //更新
                                let answerFileIndex = departmentAnswers[key]?.answerFiles.findIndex(answerFile => answerFile.answerFileName.includes(fileName));
                                let answerFile = departmentAnswers[key]?.answerFiles[answerFileIndex];
                                departmentAnswers[key].answerFiles[answerFileIndex].uploadFileForm = {"answerFileId": answerFile.answerFileId, "answerId": 0, "answerFileName": fileName, "filePath": quote.filePath, "version": quote.versionInformation, "applicationId": applicationId, "applicationStepId": checkedApplicationStepId, "departmentId": departmentAnswers[key].department.departmentId};
                            }
                            this.setState({
                                departmentAnswers: departmentAnswers,
                                selectedAnswer: departmentAnswers[key],
                                quoteFile: true
                            });
                        }
                    }
                    
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
                    }
                }
            }
        }

        const answerFiles = this.state.answerFile;
        let uploadFileFormList = this.state.uploadFileFormList;
        // 許可判定
        if (checkedApplicationStepId == 3) {
            if (file != null) {
                let reader = new FileReader();
                // アップロードされたファイルがPDFファイルの場合
                if (file.type === 'application/pdf'){
                    reader.readAsArrayBuffer(file);
                    reader.onload = (e) => {
                        let blob = new Blob([e.target.result], { type: file.type });
                        let uploadFileUrl = URL.createObjectURL(blob);
                        
                        if (!answerFiles.some(answerFile => answerFile.answerFileName.includes(file.name))) {
                            // 新規追加
                            uploadFileFormList = uploadFileFormList.filter((uploadAnswerFile) => uploadAnswerFile.answerFileName !== file.name);
                            uploadFileFormList.push({ "answerFileId": "", "answerId": 0, "answerFileName": file.name, "filePath":  null, "uploadFile": file, "uploadFileUrl": uploadFileUrl, "applicationId": applicationId, "applicationStepId": checkedApplicationStepId, "departmentId": "-1"});
                        } else{
                            let answerFileIndex = answerFiles.findIndex(answerFile => answerFile.answerFileName.includes(file.name));
                            let answerFile = answerFiles[answerFileIndex];
                            answerFiles[answerFileIndex].uploadFileForm = {"answerFileId": answerFile.answerFileId, "answerId": 0, "answerFileName": file.name, "filePath":  null, "uploadFile": file, "uploadFileUrl": uploadFileUrl, "applicationId": applicationId, "applicationStepId": checkedApplicationStepId, "departmentId": "-1"};
                        }
                        this.setState({
                            answerFile: answerFiles,
                            uploadFileFormList: uploadFileFormList,
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
                            answerFile: answerFiles,
                            uploadFileFormList: uploadFileFormList,
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
                                answerFile: answerFiles,
                                uploadFileFormList: uploadFileFormList,
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
                    if (!answerFiles.some(answerFile => answerFile.answerFileName.includes(file.name))) {
                        // 新規追加
                        uploadFileFormList = uploadFileFormList.filter((uploadAnswerFile) => uploadAnswerFile.answerFileName !== file.name);
                        uploadFileFormList.push({ "answerFileId": "", "answerId": 0, "answerFileName": file.name, "filePath": null, "uploadFile": file, "applicationId": applicationId, "applicationStepId": checkedApplicationStepId, "departmentId": "-1"});
                    }else{
                        // 更新
                        let answerFile = answerFiles[answerFileKey];
                        answerFiles[answerFileKey].uploadFileForm = {"answerFileId": answerFile.answerFileId, "answerId": 0, "answerFileName": file.name, "filePath":  null, "uploadFile": file, "applicationId": applicationId, "applicationStepId": checkedApplicationStepId, "departmentId": "-1"};
                    }
                    this.setState({
                        answerFile: answerFiles,
                        uploadFileFormList: uploadFileFormList,
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
                            answerFile: answerFiles,
                            uploadFileFormList: uploadFileFormList,
                            quoteFile: true
                        });
                    }else{
                        if(!answerFiles.some(answerFile => answerFile.answerFileName.includes(fileName))){
                            //新規追加
                            uploadFileFormList = uploadFileFormList.filter((uploadAnswerFile) => uploadAnswerFile.answerFileName !== quote.uploadFileName);
                            uploadFileFormList.push({ "answerFileId": "", "answerId": 0, "answerFileName": fileName, "filePath": quote.filePath, "version": quote.versionInformation, "applicationId": applicationId, "applicationStepId": checkedApplicationStepId, "departmentId": "-1"});
                        }else{
                            //更新
                            let answerFileIndex = answerFiles.findIndex(answerFile => answerFile.answerFileName.includes(fileName));
                            let answerFile = answerFiles[answerFileIndex];
                            answerFiles.answerFiles[answerFileIndex].uploadFileForm = {"answerFileId": answerFile.answerFileId, "answerId": 0, "answerFileName": fileName, "filePath": quote.filePath, "version": quote.versionInformation, "applicationId": applicationId, "applicationStepId": checkedApplicationStepId, "departmentId": "-1"};
                        }
                    }
                    this.setState({
                        answerFile: answerFiles,
                        uploadFileFormList: uploadFileFormList,
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
                            answerFile: answerFiles,
                            uploadFileFormList: uploadFileFormList,
                            quoteFile: true
                        });
                    }
                    this.setState({
                        answerFile: answerFiles,
                        uploadFileFormList: uploadFileFormList,
                        quoteFile: true
                    });
                }
            }
        }
    }

    /**
     * 変数が空であるか判断
     * @param {*} text 
     * @returns 
    */
    isInputed(text){

        if(text == undefined || text == null || text == "" ){
            return false;
        }else{
            return true;
        }
    }
    /**
     *  回答登録
     */
    register() {
        
        // 申請段階
        const checkedApplicationStepId = this.state.checkedApplicationStepId; 
        const answers = this.state.answers;
        const departmentAnswers = this.state.departmentAnswers;
        let deleteFileFormList = [];
        let answersContentOnly = [];
        let departmentAnswersContentOnly = [];
        let uploadFileFormList = [];
        let deleteFileFormListCount = 0;
        let notifyCount = 0;
        let unAnswered = [];

        // 行政確定ステータス・行政確定登録日時の必須チェック
        let unInput = 0;
        let unInputErrors = {answers:[],departmentAnswers:[]}

        // 事前相談、許可判定
        if(checkedApplicationStepId == 1 || checkedApplicationStepId == 3 ){
            Object.keys(answers).map(key => {
                if (answers[key]["editable"]) {
                    if(answers[key]["answerContent"] == undefined || answers[key]["answerContent"] == null || answers[key]["answerContent"]==""){
                        if(answers[key]["answerContentEditable"] == true){
                            unAnswered.push(answers[key]["answerId"])
                        }
                    }
                    // 回答内容空欄でも、登録できます
                    answersContentOnly.push({
                        "answerId": answers[key]["answerId"],
                        "editable": answers[key]["editable"],
                        "judgementResult": answers[key]["judgementResult"],
                        "answerContent": answers[key]["answerContent"],
                        "updateDatetime": answers[key]["updateDatetime"],
                        "completeFlag": answers[key]["completeFlag"],
                        "judgementInformation": answers[key]["judgementInformation"],
                        "answerFiles": answers[key]["answerFiles"],
                        "reApplicationFlag": answers[key]["reApplicationFlag"],
                        "discussionFlag": answers[key]["discussionFlag"],
                        "applicationStep": answers[key]["applicationStep"],
                        "permissionJudgementResult": answers[key]["permissionJudgementResult"],
                        "answerStatus": answers[key]["answerStatus"],
                        "answerDataType": answers[key]["answerDataType"]
                    });
                    // 事前相談
                    if(checkedApplicationStepId == 1){
                        // 新規追加ファイル
                        Object.keys(answers[key].uploadFileFormList).map(filekey => {
                            if(answers[key].uploadFileFormList[filekey] && answers[key].uploadFileFormList[filekey].answerId){
                                uploadFileFormList.push(answers[key].uploadFileFormList[filekey]);
                            }
                        })
                        // 更新ファイル・削除ファイル
                        Object.keys(answers[key].answerFiles).map(fileKey => {
                            if (answers[key].answerFiles[fileKey].uploadFileForm != null) {
                                uploadFileFormList.push(answers[key].answerFiles[fileKey].uploadFileForm);
                            }
                            if(answers[key].answerFiles[fileKey].deleteFlag == true) {
                                deleteFileFormList.push(answers[key].answerFiles[fileKey]);
                                deleteFileFormListCount++;
                            }
                        })
                    }
                    
                }
            })
        }

        // 事前協議
        if(checkedApplicationStepId == 2 ){
            Object.keys(departmentAnswers).map(key1 => {
                let fileEditable = false;
                if (departmentAnswers[key1]["editable"]) {
                    fileEditable = true;

                    // 行政確定登録：ステータスが入力している場合、日時が入力必要；日時が入力している場合、ステータスが入力必要；
                    if((this.isInputed(departmentAnswers[key1].governmentConfirmStatus) && !this.isInputed(departmentAnswers[key1].governmentConfirmDatetime))
                        ||(!this.isInputed(departmentAnswers[key1].governmentConfirmStatus) && this.isInputed(departmentAnswers[key1].governmentConfirmDatetime))){
                            unInput ++;
                            unInputErrors.departmentAnswers.push(departmentAnswers[key1].departmentAnswerId);
                    }

                    departmentAnswersContentOnly.push({
                        "departmentAnswerId": departmentAnswers[key1].departmentAnswerId,
                        "applicationId": departmentAnswers[key1].applicationId,
                        "applicationStepId": departmentAnswers[key1].applicationStepId,
                        "department": departmentAnswers[key1].department,
                        "governmentConfirmStatus": departmentAnswers[key1].governmentConfirmStatus,
                        "governmentConfirmDatetime": departmentAnswers[key1].governmentConfirmDatetime,
                        "governmentConfirmComment": departmentAnswers[key1].governmentConfirmComment,
                        "completeFlag": departmentAnswers[key1].completeFlag,
                        "notifiedFlag": departmentAnswers[key1].notifiedFlag,
                        "updateDatetime": departmentAnswers[key1].updateDatetime,
                        "governmentConfirmPermissionFlag": departmentAnswers[key1].governmentConfirmPermissionFlag
                    });
                }
                Object.keys(departmentAnswers[key1].answers).map(key2 => {
                    let answer = departmentAnswers[key1].answers[key2];
                    if (answer.editable) {
                        fileEditable = true;
                        if(answer["answerContent"] == undefined || answer["answerContent"] == null || answer["answerContent"]==""){
                            if(answer["answerContentEditable"] == true && answer["answerDataType"] !== "7" && answer["answerPermissionFlag"] == false){
                                unAnswered.push(answer["answerId"]);
                            }
                        }

                        // 行政確定登録：ステータスが入力している場合、日時が入力必要；日時が入力している場合、ステータスが入力必要；
                        if((this.isInputed(answer["governmentConfirmStatus"]) && !this.isInputed(answer["governmentConfirmDatetime"]))
                            ||(!this.isInputed(answer["governmentConfirmStatus"]) && this.isInputed(answer["governmentConfirmDatetime"]))){
                                unInput ++;
                                unInputErrors.answers.push(answer["answerId"]);
                        }

                        // 回答内容空欄でも、登録できます    
                        //画面に、選択された協議対象に対する帳票ID
                        let discussionItemText = answer["discussionItems"]?.filter(item => {return item.checked == true})?.map(item => { return item.ledgerId }).filter(ledgerId => { return ledgerId !== null }).join(",");
                        answersContentOnly.push({
                            "answerId": answer["answerId"],
                            "departmentAnswerId":departmentAnswers[key1].departmentAnswerId,
                            "editable": answer["editable"],
                            "judgementResult": answer["judgementResult"],
                            "answerContent": answer["answerContent"],
                            "updateDatetime": answer["updateDatetime"],
                            "completeFlag": answer["completeFlag"],
                            "notifiedFlag": answer["notifiedFlag"],
                            "judgementInformation": answer["judgementInformation"],
                            "answerFiles": [],
                            "reApplicationFlag": answer["reApplicationFlag"],
                            "discussionFlag": answer["reApplicationFlag"],
                            "discussionItem": discussionItemText,
                            "businessPassStatus":answer["businessPassStatus"],
                            "businessPassComment": answer["businessPassComment"],
                            "governmentConfirmStatus": answer["governmentConfirmStatus"],
                            "governmentConfirmDatetime": answer["governmentConfirmDatetime"],
                            "governmentConfirmComment": answer["governmentConfirmComment"],
                            "governmentConfirmNotifiedFlag": answer["governmentConfirmNotifiedFlag"],
                            "deleteUnnotifiedFlag": answer["deleteUnnotifiedFlag"],
                            "permissionJudgementResult": answer["permissionJudgementResult"],
                            "answerStatus": answer["answerStatus"],
                            "answerDataType": answer["answerDataType"],
                            "answerPermissionFlag": answer["answerPermissionFlag"],
                            "governmentConfirmPermissionFlag": answer["governmentConfirmPermissionFlag"],
                            "permissionJudgementMigrationFlag": answer["permissionJudgementMigrationFlag"]
                        });
                        
                    }
                })

                // 該当部署回答中に、編集可能の回答が存在する場合、回答ファイルの更新を行う
                if(fileEditable){
                    // 新規追加ファイル
                    Object.keys(departmentAnswers[key1].uploadFileFormList).map(filekey => {
                        if(departmentAnswers[key1].uploadFileFormList[filekey]){
                            uploadFileFormList.push(departmentAnswers[key1].uploadFileFormList[filekey]);
                        }
                    })
                    // 更新ファイル・削除ファイル
                    Object.keys(departmentAnswers[key1].answerFiles).map(fileKey => {
                        if (departmentAnswers[key1].answerFiles[fileKey].uploadFileForm != null) {
                            uploadFileFormList.push(departmentAnswers[key1].answerFiles[fileKey].uploadFileForm);
                        }
                        if(departmentAnswers[key1].answerFiles[fileKey].deleteFlag == true) {
                            deleteFileFormList.push(departmentAnswers[key1].answerFiles[fileKey]);
                            deleteFileFormListCount++;
                        }
                    })
                }

            })
            
        }
        
        // 事前協議の場合、行政確定登録：ステータス、日時が一つだけ入力済みの場合、
        if(unInput > 0){
            alert("行政確定登録ステータス、または、行政確定登録日時を入力してください。");
            this.setState({ errors: unInputErrors});
            return ;
        }
        
        // 回答欄に入力しない回答があります
        if(unAnswered.length > 0){
            var res = confirm("未入力の回答があります。そのまま登録してもよろしいでしょうか？");
            if(res == false){
                return ;
            }
        }


        // 許可判定
        if(checkedApplicationStepId == 3){
            const answerFiles = this.state.answerFile; // 既存のもの
            const uploadFiles = this.state.uploadFileFormList; // 新規追加されたもの
            // 新規追加ファイル
            Object.keys(uploadFiles).map(filekey => {
                if(uploadFiles[filekey] && uploadFiles[filekey].applicationId){
                    uploadFileFormList.push(uploadFiles[filekey]);
                }
            })
            // 更新ファイル・削除ファイル
            Object.keys(answerFiles).map(fileKey => {
                if (answerFiles[fileKey].uploadFileForm != null) {
                    uploadFileFormList.push(answerFiles[fileKey].uploadFileForm);
                }
                if(answerFiles[fileKey].deleteFlag == true) {
                    deleteFileFormList.push(answerFiles[fileKey]);
                    deleteFileFormListCount++;
                }
            })
        }

        const applicationId = this.props.viewState.applicationInformationSearchForApplicationId;

        document.getElementById("customloader_main").style.display = "block";

        //回答登録
        fetch(Config.config.apiUrl + "/answer/input", {
            method: 'POST',
            body: JSON.stringify({
                applicationId: applicationId,
                applicationStepId: checkedApplicationStepId,
                answers: answersContentOnly,
                departmentAnswers: departmentAnswersContentOnly
            }),
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
                                //ファイル登録
                                this.uploadAnswerFile(uploadFileFormList,notifyCount);
                            }
                        }).catch(error => {
                            document.getElementById("customloader_main").style.display = "none";
                            console.error('処理に失敗しました', error);
                            alert('回答ファイルの削除処理に失敗しました');
                        });
                    })
                } else {
                    //ファイル登録
                    this.uploadAnswerFile(uploadFileFormList,notifyCount);
                }
            } else {
                document.getElementById("customloader_main").style.display = "none";
                alert('回答登録に失敗しました。一度画面を閉じて再度入力を行い実行してください。');
            }
        }).catch(error => {
            document.getElementById("customloader_main").style.display = "none";
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
                    if(name == "uploadFileUrl"){
                        continue;
                    }
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
                            document.getElementById("customloader_main").style.display = "none";
                            notifyCount = notifyCount + 1;
                            this.notify(notifyCount);
                        }
                    }).catch(error => {
                        document.getElementById("customloader_main").style.display = "none";
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
                            document.getElementById("customloader_main").style.display = "none";
                            notifyCount = notifyCount + 1;
                            this.notify(notifyCount);
                        }
                    }).catch(error => {
                        document.getElementById("customloader_main").style.display = "none";
                        console.error('処理に失敗しました', error);
                        alert('回答ファイルのアップロード処理に失敗しました');
                    });
                }
            })
        } else {
          document.getElementById("customloader_main").style.display = "none";
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
                    this.props.viewState.setCustomMessage(res[0]?.labels?.title, res[0]?.labels?.content);
                    this.props.viewState.nextAnswerCompleteView();
                } else {
                    alert("labelの取得に失敗しました。");
                }
            }).catch(error => {
                console.error('通信処理に失敗しました', error);
                alert('通信処理に失敗しました');
            }).finally(() => document.getElementById("customloader_main").style.display = "none");
        }
    }

    /**
     * 回答キャンセルボタン
     * @param {*} active アクティブページ
     */
    chancelAnswerInput(active){
        this.state.viewState.changeApplyPageActive(active);
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
        this.state.viewState.setCallBackFunction(this.fileUploadCallback);
        this.state.viewState.setQuoteFiles(target);
        const checkedApplicationStepId = this.state.checkedApplicationStepId;
        if(checkedApplicationStepId == 3){
            this.state.viewState.setAnswerQuoteFiles(this.state.answerFile);
        }else{
            this.state.viewState.setAnswerQuoteFiles(this.state.selectedAnswer?.answerFiles);
        }
        this.state.viewState.changeFileUploadModalShow();
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
        // 申請段階
        const checkedApplicationStepId = this.state.checkedApplicationStepId;

        // 許可判定の場合、回答行選択イベントがないので、keyが存在しない
        if (key != null || checkedApplicationStepId == 3) {
            let reader = new FileReader();
            reader.readAsDataURL(file);
            reader.onload = (e) => {
                let uploadFileUrl = e.target.result;

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
        // 申請段階
        const checkedApplicationStepId = this.state.checkedApplicationStepId;
        const key = this.state.selectedAnswerKey;
        const applicationId = this.props.viewState.applicationInformationSearchForApplicationId;

        const answers = this.state.answers;
        if(key != null && checkedApplicationStepId == 1){
            let reader = new FileReader();
            reader.readAsDataURL(file);
            reader.onload = (e) => {
                // 更新
                let uploadFileUrl = e.target.result;
                // answerFileNameに存在する場合

                if(answers[key]?.answerFiles.some(answerFile => answerFile.answerFileName.includes(fileName))){
                    let answerFileIndex = answers[key]?.answerFiles.findIndex(answerFile => answerFile.answerFileName.includes(fileName));
                    let answerFile = answers[key]?.answerFiles[answerFileIndex];
                    answers[key].answerFiles[answerFileIndex].uploadFileForm = {"answerFileId": answerFile.answerFileId, "answerId": answers[key].answerId, "answerFileName": fileName, "filePath":  null, "uploadFile": file, "uploadFileUrl": uploadFileUrl, "applicationId": applicationId, "applicationStepId": checkedApplicationStepId, "departmentId": "-1"}
                }else{
                    this.removeNewFile(fileName);
                    answers[key]["uploadFileFormList"].push({ "answerFileId": "", "answerId": answers[key].answerId, "answerFileName": fileName, "filePath": null, "uploadFile": file, "uploadFileUrl": uploadFileUrl, "applicationId": applicationId, "applicationStepId": checkedApplicationStepId, "departmentId": "-1" });
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

        const departmentAnswers = this.state.departmentAnswers;
        if(key != null && checkedApplicationStepId == 2){
            let reader = new FileReader();
            reader.readAsDataURL(file);
            reader.onload = (e) => {
                // 更新
                let uploadFileUrl = e.target.result;
                // answerFileNameに存在する場合

                if(departmentAnswers[key]?.answerFiles.some(answerFile => answerFile.answerFileName.includes(fileName))){
                    let answerFileIndex = departmentAnswers[key]?.answerFiles.findIndex(answerFile => answerFile.answerFileName.includes(fileName));
                    let answerFile = departmentAnswers[key]?.answerFiles[answerFileIndex];
                    departmentAnswers[key].answerFiles[answerFileIndex].uploadFileForm = {"answerFileId": answerFile.answerFileId, "answerId": 0, "answerFileName": fileName, "filePath":  null, "uploadFile": file, "uploadFileUrl": uploadFileUrl, "applicationId": applicationId, "applicationStepId": checkedApplicationStepId, "departmentId": departmentAnswers[key].department.departmentId}
                }else{
                    this.removeNewFile(fileName);
                    departmentAnswers[key]["uploadFileFormList"].push({ "answerFileId": "", "answerId": 0, "answerFileName": fileName, "filePath": null, "uploadFile": file, "uploadFileUrl": uploadFileUrl, "applicationId": applicationId, "applicationStepId": checkedApplicationStepId, "departmentId": departmentAnswers[key].department.departmentId });
                }
                this.setState({
                    departmentAnswers: departmentAnswers,
                    selectedAnswer: departmentAnswers[key],
                    targetFilePath: null,
                    targetFile: fileName,
                    targetFileUrl: uploadFileUrl,
                    targetAppFileVersion: null
                });
            }
        }

        let uploadFileFormList = this.state.uploadFileFormList;
        const answerFiles = this.state.answerFile;
        if(checkedApplicationStepId == 3){
            let reader = new FileReader();
            reader.readAsDataURL(file);
            reader.onload = (e) => {
                // 更新
                let uploadFileUrl = e.target.result;
                // answerFileNameに存在する場合

                if(answerFiles.some(answerFile => answerFile.answerFileName.includes(fileName))){
                    let answerFileIndex = answerFiles.findIndex(answerFile => answerFile.answerFileName.includes(fileName));
                    let answerFile = answerFiles[answerFileIndex];
                    answerFiles[answerFileIndex].uploadFileForm = {"answerFileId": answerFile.answerFileId, "answerId": 0, "answerFileName": fileName, "filePath":  null, "uploadFile": file, "uploadFileUrl": uploadFileUrl, "applicationId": applicationId, "applicationStepId": checkedApplicationStepId, "departmentId": "-1"}
                }else{
                    this.removeNewFile(fileName);
                    uploadFileFormList.push({ "answerFileId": "", "answerId": 0, "answerFileName": fileName, "filePath": null, "uploadFile": file, "uploadFileUrl": uploadFileUrl, "applicationId": applicationId, "applicationStepId": checkedApplicationStepId, "departmentId": "-1" });
                }
                this.setState({
                    answerFile: answerFiles,
                    uploadFileFormList:uploadFileFormList,
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
        // 申請段階
        const checkedApplicationStepId = this.state.checkedApplicationStepId;

        // 事前相談
        if(checkedApplicationStepId == 1){
            let selectedAnswer = this.state.selectedAnswer;
            let answers = this.state.answers;
            let selectedAnswerKey = this.state.selectedAnswerKey;
            selectedAnswer.uploadFileFormList = selectedAnswer.uploadFileFormList.filter((file) => (file.answerFileName !== fileName));
            answers[selectedAnswerKey] = selectedAnswer;
            this.setState({
                selectedAnswer: selectedAnswer,
                answers: answers
            });
        }

        // 事前協議
        if(checkedApplicationStepId == 2){
            let selectedAnswer = this.state.selectedAnswer;
            let departmentAnswers = this.state.departmentAnswers;
            let selectedAnswerKey = this.state.selectedAnswerKey;
            selectedAnswer.uploadFileFormList = selectedAnswer.uploadFileFormList.filter((file) => (file.answerFileName !== fileName));
            departmentAnswers[selectedAnswerKey] = selectedAnswer;
            this.setState({
                selectedAnswer: selectedAnswer,
                departmentAnswers: departmentAnswers
            });
        }

        // 許可判定
        if(checkedApplicationStepId == 3){
            let uploadFileFormList = this.state.uploadFileFormList;
            uploadFileFormList = uploadFileFormList.filter((file) => (file.answerFileName !== fileName));
            this.setState({uploadFileFormList: uploadFileFormList});
        }
    }

    /**
     * 既存のファイルを削除する
     * @param {*} index 回答ファイルリストのindex
     * @param {*} deleteFlag 削除フラグ
     */
    removeExistFile(index, deleteFlag) {
        const checkedApplicationStepId = this.state.checkedApplicationStepId;
        // 事前相談
        if(checkedApplicationStepId == 1){
            let selectedAnswer = this.state.selectedAnswer;
            let answers = this.state.answers;
            let selectedAnswerKey = this.state.selectedAnswerKey;

            selectedAnswer.answerFiles[index].deleteFlag = deleteFlag;
            if (deleteFlag) {
                // 更新アップロードファイルも削除する
                selectedAnswer.answerFiles[index]["uploadFileForm"] = null;
            }

            answers[selectedAnswerKey] = selectedAnswer;
            this.setState({
                selectedAnswer: selectedAnswer,
                answers: answers
            });
        }

        // 事前協議
        if(checkedApplicationStepId == 2){
            let selectedAnswer = this.state.selectedAnswer;
            let departmentAnswers = this.state.departmentAnswers;
            let selectedAnswerKey = this.state.selectedAnswerKey;

            selectedAnswer.answerFiles[index].deleteFlag = deleteFlag;
            if (deleteFlag) {
                // 更新アップロードファイルも削除する
                selectedAnswer.answerFiles[index]["uploadFileForm"] = null;
            }
            departmentAnswers[selectedAnswerKey] = selectedAnswer;
            this.setState({
                selectedAnswer: selectedAnswer,
                departmentAnswers: departmentAnswers
            });
        }

        // 許可判定
        if(checkedApplicationStepId == 3){
            let answerFile = this.state.answerFile;
            answerFile[index].deleteFlag = deleteFlag;
            if (deleteFlag) {
                // 更新アップロードファイルも削除する
                answerFile[index]["uploadFileForm"] = null;
            }
            this.setState({answerFile: answerFile});
        }
    }

    /**
     * 対象選択
     * @param {*} event 
     * @param {*} selectedAnswerId 選択対象の回答ID
     */
    handleRowClick = (event, selectedAnswerId, departmentAnswerId = 0) => {
        const checkedApplicationStepId = this.state.checkedApplicationStepId;

        if(checkedApplicationStepId == 2){
            if(departmentAnswerId!==undefined && departmentAnswerId !== null && Number(departmentAnswerId) > 0){
                const departmentAnswers = [...this.state.departmentAnswers];
                const index = departmentAnswers.findIndex(answer=>answer.departmentAnswerId === departmentAnswerId);
                const answer = index>-1?departmentAnswers[index]:null;
                this.setState({selectedAnswer:answer, selectedAnswerKey:index, selectedDepartmentAnswerId: index>-1?answer?.departmentAnswerId:0});
            }
        }

        if(checkedApplicationStepId == 1){
            if(selectedAnswerId!==undefined && selectedAnswerId !== null && Number(selectedAnswerId) > 0){
                const answers = [...this.state.answers];
                const index = answers.findIndex(answer=>answer.answerId===selectedAnswerId);
                const answer = index>-1?answers[index]:null;
                this.setState({ 
                    selectedAnswer: answer,
                    selectedAnswerKey: index
                });
            }
        }
    }

    /**
     * 回答入力のコールバック処理
     * @param {*} answers 回答一覧
     */
    answerInputCallback = (answers) => {
        const checkedApplicationStepId = this.state.checkedApplicationStepId;

        if(checkedApplicationStepId == 2){

            let addAnswerMaxIndex = 0;
            Object.keys(answers).map( key1 =>{
                Object.keys(answers[key1].answers).map( key2 =>{

                    // 行政で新規追加した条項
                    if(answers[key1].answers[key2].answerId == 0){
                        if(answers[key1].answers[key2].addAnswerIndex > addAnswerMaxIndex){
                            addAnswerMaxIndex = answers[key1].answers[key2].addAnswerIndex;
                        }
                    }
                })
            })

            this.setState({departmentAnswers: answers, addAnswerIndex: addAnswerMaxIndex});
        }else{
            this.setState({answers: answers});
        }
    }

    isTrue(value){
        if(value == null || value ==undefined || value === false){
            return false;
        }else{
            return true;
        }
    }
    /**
     * 編集可能のレコードがあるか判定
     * ※事前協議：選択中の部署に対する回答リスト
     * ※許可判定：全ての回答リスト
     * @returns 
     */
    isEditableAmswerFile(){
        let editable = false;
        const checkedApplicationStepId = this.state.checkedApplicationStepId;
        if(checkedApplicationStepId == 1){
            const answer = this.state.selectedAnswer;
            if(answer.editable !== null && answer.editable !== undefined && answer.editable === true) {
                editable = true;
            }
        }
        if(checkedApplicationStepId == 2){
            const departmentAnswer = this.state.selectedAnswer;
            // 編集権限あるか
            if(departmentAnswer.editable !== null && departmentAnswer.editable !== undefined && departmentAnswer.editable === true) {
                editable = true;
            }else{
                const answers = departmentAnswer.answers;
                if(answers){
                    Object.keys(answers).map( index =>{
                        if(answers[index].editable !== null && answers[index].editable !== undefined && answers[index].editable === true) {
                            editable = true;
                        }
                    })
                }else{
                    editable = false;
                }
            }

            //編集権限ある場合、
            if( editable == true){
                // 行政確定登録許可通知済みである場合、ファイル編集不可
                if(this.isTrue(departmentAnswer.governmentConfirmPermissionFlag)){
                    editable = false;
                }

                // 仕様：事業者合意登録済み後、ファイル編集不可
                // 部署の全て回答に、事業者合意登録していない条項がある場合、回答ファイルが編集可能
                const answers = departmentAnswer.answers;
                if(answers){

                    // 事業者合意登録しない条項があるか
                    const isExist = answers.some(ans => this.isInputed(ans.businessPassStatus) === false);
                    if(isExist === false){
                        // 事業者合意登録しない条項がない場合、事業者登録済みとして、回答ファイルが編集不可になる
                        editable = false;
                    }
                }
            }
        }

        if(checkedApplicationStepId == 3){
            const answers = this.state.answers;
            if(answers){
                Object.keys(answers).map( index =>{
                    if(answers[index].editable !== null && answers[index].editable !== undefined && answers[index].editable === true) {
                        editable = true;
                    }
                })
            }else{
                editable = false
            }
        }

        return editable;
    }

    /**
     * 編集可能な回答があるか判定
     * @param {*} checkedApplicationStepId 申請段階
     * @param {*} answers 回答一覧
     * @param {*} departmentAnswers 部署回答一覧 
     * @returns 
     */
    registerDisabled(checkedApplicationStepId, answers, departmentAnswers){

        // 編集可能な回答の件数
        let editableAnswersCount = 0;

        // 事前相談、許可判定
        if(checkedApplicationStepId == 1 || checkedApplicationStepId == 3 ){

            Object.keys(answers).map(key => {
                if (answers[key].editable) {
                    editableAnswersCount ++;
                }
            })
        }

        // 事前協議
        if(checkedApplicationStepId == 2 ){

            Object.keys(departmentAnswers).map(key1 => {
                if (departmentAnswers[key1].editable) {
                    editableAnswersCount ++;
                }

                Object.keys(departmentAnswers[key1].answers).map(key2 => {
                    let answer = departmentAnswers[key1].answers[key2];
                    if (answer.editable) {
                        editableAnswersCount ++;
                    }
                })
            })
        }

        // 編集可能な回答が0件数の場合、「回答登録」ボタンが無効にする
        if(editableAnswersCount == 0){
            return true;
        }else{
            return false;
        }

    }

    render() {
        const t = this.props.t;
        const checkedApplicationStepId = this.state.checkedApplicationStepId;
        const checkedApplicationStepName = this.state.checkedApplicationStepName;
        const applicantInformations = this.state.applicantInformations;
        const checkedApplicationCategory = this.state.checkedApplicationCategory;
        const lotNumbers = this.state.lotNumbers;
        const status = this.state.status;
        const answers = this.state.answers;
        const sortedAnswers = this.state.sortedAnswers;
        const selectedAnswer = this.state.selectedAnswer;
        const applicationFiles = this.state.applicationFiles;
        const adminInfoMessage = t("adminInfoMessage.tipsForAnswerInput");
        const applicationType = this.state.checkedApplicationType;
        const applicantAddInformations = this.state.applicantAddInformations;
        const departmentAnswers = this.state.departmentAnswers;
        const addAnswerIndex = this.state.addAnswerIndex;
        const ledgerMaster = this.state.ledgerMaster;

        // 申請段階ごとの回答ファイル
        const answerFile = this.state.answerFile;
        const uploadFileFormList = this.state.uploadFileFormList;

        let answerFileCount = 0;
        if(checkedApplicationStepId == 3){

            // 既存の回答ファイルの件数
            const count1 = Object.keys(answerFile).length;
            // 新追加の回答ファイルの件数
            const count2 = Object.keys(uploadFileFormList).length;

            answerFileCount = count1 + count2;
        }

        const errors = this.state.errors;
        const applicationId = this.props.viewState.applicationInformationSearchForApplicationId;
        return (
            <div style={{overflowY:"auto" }}>
                <Box overflow={false} overflowY={false}>
                    <Box className={PageStyle.text_area} style={{width: "65%"}}>
                        <span dangerouslySetInnerHTML={{ __html: adminInfoMessage}}></span>
                    </Box>
                    <Box right style={{width: "30%"}}>
                        <button
                            className={`${CustomStyle.btn_baise_style} ${CustomStyle.btn_gry}`}
                            style={{width:"30%",height:"40px"}}
                            onClick={e => {
                                this.props.viewState.hideAnswerInputView();
                                this.chancelAnswerInput("applyDetail");
                            }}
                        >
                            <span>回答キャンセル</span>
                        </button>
                        <button
                            className={`${CustomStyle.btn_baise_style} `}
                            style={{width:"30%",height:"40px"}}
                            disabled = {this.registerDisabled(checkedApplicationStepId, answers, departmentAnswers)}
                            onClick={e => {
                                this.register();
                            }}
                        >
                            <span>回答登録</span>
                        </button>
                    </Box>
                </Box>
                <Box>
                    <Box col12 className={CustomStyle.custom_nuv}>
                        回答入力
                    </Box>
                </Box>
                <Box>
                    <Box style={{display:"block", width: "calc(35vw - 10px)"}}>
                        <Spacing bottom={1} />
                        <p>回答を入力してください。</p>
                        <Spacing bottom={1} />
                        <p>入力中の申請種別：<span style={{fontWeight:"bold"}}>{checkedApplicationStepName}</span></p>
                        <Spacing bottom={3} />
                        <ApplicantInformation 
                            viewState={this.props.viewState} terria={this.props.terria} 
                            applicationType={applicationType}
                            applicantInformations={applicantInformations}
                            applicantAddInformations={applicantAddInformations}  
                            checkedApplicationCategory={checkedApplicationCategory} 
                            lotNumbers={lotNumbers}
                            answers={answers}
                            departmentAnswers={departmentAnswers} 
                            status={status} height={"62vh"}
                            applicationId={applicationId}
                        />
                    </Box>
                    <Box  ref={this.mpBaseContainerElement} style={{height:"75.5vh", width: "calc(65vw - 10px)"}}>
                        <Box
                            centered
                            displayInlineBlock
                            className={CustomStyle.custom_content}
                        >
                            {checkedApplicationStepId == 1&& (
                            <Box col12>
                                <AnswerContentList 
                                    t={t} 
                                    viewState={this.props.viewState} 
                                    terria={this.props.terria} 
                                    answers={answers} 
                                    selectedAnswerId={selectedAnswer?selectedAnswer.answerId:0} 
                                    editable={true} 
                                    callback={this.answerInputCallback} 
                                    clickAnswer={this.handleRowClick}
                                />
                            </Box>
                            )}
                            {checkedApplicationStepId == 2&& (
                            <Box col12>
                                <NegotiationContentList 
                                    viewState={this.props.viewState} 
                                    terria={this.props.terria} 
                                    departmentAnswers ={departmentAnswers}
                                    addAnswerIndex ={addAnswerIndex}
                                    selectedAnswerId = {selectedAnswer?selectedAnswer.answerId:0}
                                    selectedDepartmentAnswerId = {selectedAnswer?selectedAnswer.departmentAnswerId:0}
                                    editable={true} 
                                    callback={this.answerInputCallback} 
                                    clickAnswer={this.handleRowClick}
                                    ledgerMaster = {ledgerMaster}
                                    errors = {errors}
                                />
                            </Box>
                            )}
                            {checkedApplicationStepId == 3&& (
                            <Box col12>
                                <AssessmentContentList 
                                    viewState={this.props.viewState} 
                                    terria={this.props.terria} 
                                    answers ={answers}
                                    selectedAnswerId = {selectedAnswer?selectedAnswer.answerId:0}
                                    editable={true} 
                                    callback={this.answerInputCallback} 
                                    clickAnswer={this.handleRowClick}
                                    answerFileCount={0}
                                />
                            </Box>
                            )}
                            <Spacing bottom={1} />
                            <Box>
                                {/* {checkedApplicationStepId !== 2&& (     */}
                                <Box col8 displayInlineBlock padded>
                                    <nav className={CustomStyle.custom_nuv}>添付ファイル一覧</nav>
                                    <Spacing bottom={2} />
                                    {(this.state.selectedAnswer != null &&  checkedApplicationStepId == 1)&& (
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
                                    {(this.state.selectedDepartmentAnswerId != null  && checkedApplicationStepId == 2)&& (
                                        <div style={{ display: "flex", justifyContent: "start"}}>
                                            {this.isEditableAmswerFile() && (
                                                <button
                                                className={CustomStyle.download_button}
                                                style={{ margin: "0 10px"}}
                                                onClick={e => {
                                                    this.output(applicationFiles, null);
                                                }}
                                                >追加</button>
                                            )}
                                            <p style={{ margin: "auto 0", padding: "0"}}>{`選択中の部署：${this.state.selectedAnswer?.department?.departmentName}`}</p>
                                        </div>
                                    )}
                                    {(checkedApplicationStepId == 3)&& (
                                        <div style={{ display: "flex", justifyContent: "start"}}>
                                            {this.isEditableAmswerFile() && (
                                                <button
                                                className={CustomStyle.download_button}
                                                style={{ margin: "0 10px"}}
                                                onClick={e => {
                                                    this.output(applicationFiles, null);
                                                }}
                                                >追加</button>
                                            )}
                                        </div>
                                    )}
                                    <Spacing bottom={2} />
                                    <div style={{ height:"16vh", overflowY: "auto", minHeight: "200px"}}>
                                        <table className={CustomStyle.selection_table}>
                                            <thead>
                                                <tr className={CustomStyle.table_header}>
                                                    <th style={{ width: 10 + "%" }}>拡張子</th>
                                                    <th style={{ width: 40 + "%" }}>ファイル名</th>
                                                    <th className="no-sort" style={{ width: 50 + "%" }}></th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                { checkedApplicationStepId !==3 && (
                                                    <>
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
                                                                                <>{this.isEditableAmswerFile() ? "表示/編集" : "表示"}</>
                                                                            )}
                                                                            {this.state.selectedAnswer?.answerFiles[file]?.answerFileName.split(".").pop() === "pdf" && (
                                                                                <>表示</>
                                                                            )}
                                                                            </button>
                                                                        )}
                                                                        {this.state.selectedAnswer?.answerFiles[file]?.deleteFlag == false && this.isEditableAmswerFile() && (
                                                                            <button className={CustomStyle.delete_button} style={{ margin: "0 10px"}} onClick={e=>{this.removeExistFile(file,true)}}>削除</button>
                                                                        )}
                                                                        
                                                                        {this.state.selectedAnswer?.answerFiles[file]?.deleteFlag == true && this.isEditableAmswerFile() && (
                                                                            <button className={CustomStyle.delete_button} style={{ margin: "0 10px"}} onClick={e=>{this.removeExistFile(file,false)}}>元に戻す</button>
                                                                        )}
                                                                    </div>
                                                                )}
                                                            </td>
                                                        </tr>
                                                    ))}
                                                    {/** 新規追加したファイルを表示 */}
                                                    {this.state.selectedAnswer && this.isEditableAmswerFile() && Object.keys(this.state.selectedAnswer?.uploadFileFormList).map(file => (
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
                                                    </>
                                                )}
                                                { checkedApplicationStepId ==3 && (
                                                    <>
                                                    {/** 既存のファイルを表示 */}
                                                    {answerFile && Object.keys(answerFile).map(file => (
                                                        <tr>
                                                            <td>{answerFile[file]?.answerFileName.split(".").pop()}</td>
                                                            {answerFile[file]?.deleteFlag == true && (
                                                                <td><p style={{textDecorationLine: 'line-through', textDecorationStyle: 'solid'}}>{answerFile[file]?.answerFileName}</p></td>
                                                            )}
                                                            {answerFile[file]?.deleteFlag == false && (
                                                                <td>{answerFile[file]?.answerFileName}</td>
                                                            )}
                                                            
                                                            <td style={{ display: "flex", justifyContent: "start"}}>
                                                            {this.isEditableAmswerFile()  && ( 
                                                                <div style={{ display: "flex"}}>                                                        
                                                                    {answerFile[file]?.deleteFlag == false && this.state.editableExtentions.includes(answerFile[file]?.answerFileName.split(".").pop()) && (
                                                                        <button
                                                                            className={CustomStyle.edit_button}
                                                                            style={{ margin: "0 10px"}}
                                                                            onClick={e => this.showFile(
                                                                                !answerFile[file]?.uploadFileForm ? answerFile[file]?.answerFilePath : answerFile[file]?.uploadFileForm.filePath,
                                                                                answerFile[file]?.answerFileName,
                                                                                !answerFile[file]?.uploadFileForm ? null : answerFile[file]?.uploadFileForm.uploadFileUrl,
                                                                                null,
                                                                                false,
                                                                                false
                                                                            )}
                                                                        >
                                                                        {answerFile[file]?.answerFileName.split(".").pop() !== "pdf" && (
                                                                            <>表示/編集</>
                                                                        )}
                                                                        {answerFile[file]?.answerFileName.split(".").pop() === "pdf" && (
                                                                            <>表示</>
                                                                        )}
                                                                        </button>
                                                                    )}
                                                                    {answerFile[file]?.deleteFlag == false && this.isEditableAmswerFile() && (
                                                                        <button className={CustomStyle.delete_button} style={{ margin: "0 10px"}} onClick={e=>{this.removeExistFile(file,true)}}>削除</button>
                                                                    )}
                                                                    
                                                                    {answerFile[file]?.deleteFlag == true && this.isEditableAmswerFile() && (
                                                                        <button className={CustomStyle.delete_button} style={{ margin: "0 10px"}} onClick={e=>{this.removeExistFile(file,false)}}>元に戻す</button>
                                                                    )}
                                                                </div>
                                                            )}
                                                            </td>
                                                        </tr>
                                                    ))}
                                                    {/** 新規追加したファイルを表示 */}
                                                    {uploadFileFormList && Object.keys(uploadFileFormList).map(file => (
                                                        <tr>
                                                            <td>{uploadFileFormList[file]?.answerFileName.split(".").pop()}</td>
                                                            <td>{uploadFileFormList[file]?.answerFileName}</td>
                                                            <td style={{ display: "flex", justifyContent: "start"}}>
                                                            {this.state.editableExtentions.includes(uploadFileFormList[file]?.answerFileName.split(".").pop()) && 
                                                                <button className={CustomStyle.edit_button} style={{ margin: "0 10px"}} onClick={e => this.showFile(
                                                                    uploadFileFormList[file]?.filePath,
                                                                    uploadFileFormList[file]?.answerFileName,
                                                                    uploadFileFormList[file]?.uploadFileUrl,
                                                                    uploadFileFormList[file]?.version,
                                                                    false,
                                                                    false
                                                                )}>
                                                                    {uploadFileFormList[file]?.answerFileName.split(".").pop() !== "pdf" && (
                                                                        <>表示/編集</>
                                                                    )}
                                                                    {uploadFileFormList[file]?.answerFileName.split(".").pop() === "pdf" && (
                                                                        <>表示</>
                                                                    )}
                                                                </button>
                                                            }
                                                            <button className={CustomStyle.delete_button} style={{ margin: "0 10px"}} onClick={e=>{this.removeNewFile(uploadFileFormList[file]?.answerFileName)}}>削除</button>
                                                            </td>
                                                        </tr>
                                                    ))}
                                                    </>
                                                )}
                                            </tbody>
                                        </table>
                                    </div>
                                </Box>
                                <Box col4 ref={this.mapBaseElement} style={{height:checkedApplicationStepId !== 2?"29vh":"25vh"}}>
                                </Box>
                            </Box>
                        </Box>
                    </Box>
                </Box>
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
                    editable={this.isEditableAmswerFile()}
                />
            )}
        </div>
        );
        
    }
}
export default withTranslation()(withTheme(AnswerInput));