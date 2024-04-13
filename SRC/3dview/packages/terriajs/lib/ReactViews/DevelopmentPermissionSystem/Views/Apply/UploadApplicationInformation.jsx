import { observer } from "mobx-react";
import PropTypes, { object } from "prop-types";
import React from "react";
import { withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import Box from "../../../../Styled/Box";
import CustomStyle from "./scss/upload-applicant-Information.scss";
import Config from "../../../../../customconfig.json";

/**
 * 申請ファイルアップロード画面
 */
@observer
class UploadApplicationInformation extends React.Component {
    static displayName = "UploadApplicationInformation";

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
            //申請ファイル一覧
            applicationFile: props.viewState.applicationFile,
            // 再申請ファイル一覧
            reApplicationFile: props.viewState.reApplicationFile,
            // 対象一覧の高さ
            height: 0,
            // 添付されたファイルのサイズの合計
            fileSizeCount: 0,
            // 申請ファイルアップロードの案内文言
            explanation: {}
        };
    }

    /**
     * 初期処理
     */
    componentDidMount() {
        if(this.props.viewState.isReApply){
            // 再申請の場合、保持された入力情報がある場合はAPIリクエストを行わない
            if (Object.keys(this.state.reApplicationFile).length < 1) {
                let answerContent = this.props.viewState.answerContent;
                let loginId = answerContent["loginId"];
                let password = answerContent["password"];
                // 再申請情報取得
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
                            this.props.viewState.setReAppInformation(JSON.parse(JSON.stringify(res)),JSON.parse(JSON.stringify(res.applicationFileForm)));
                            this.setState({ applicationFile: Object.values(res.applicationFileForm) });
                            this.countFileSize(Object.values(res.applicationFileForm));
                        } else {
                            this.setState({ applicationFile: [],fileSizeCount:0 });
                            alert("該当する申請ファイルは一件もありません。");
                        }
                    }else{
                        alert('再申請情報取得処理に失敗しました');
                    }
                }).catch(error => {
                    console.error('通信処理に失敗しました', error);
                    alert('通信処理に失敗しました');
                });
            }else{
                this.setState({ applicationFile: this.props.viewState.reApplicationFile });
                this.countFileSize(this.props.viewState.reApplicationFile);
            }
        }else{
            //保持された入力情報がある場合はAPIリクエストを行わない
            if (Object.keys(this.state.applicationFile).length < 1) {
                // APIへのリクエスト
                let generalConditionDiagnosisResult = Object.values(this.props.viewState.generalConditionDiagnosisResult);
                generalConditionDiagnosisResult = generalConditionDiagnosisResult.filter(Boolean);
                fetch(Config.config.apiUrl + "/application/applicationFiles", {
                    method: 'POST',
                    body: JSON.stringify(generalConditionDiagnosisResult),
                    headers: new Headers({ 'Content-type': 'application/json' }),
                })
                    .then(res => res.json())
                    .then(res => {
                        if(res.status === 401){
                            alert("認証情報が無効です。ページの再読み込みを行います。");
                            window.location.reload();
                            return null;
                        }
                        if (Object.keys(res).length > 0 && !res.status) {
                            Object.keys(res).map(key => {
                                if (!res[key].uploadFileFormList) {
                                    res[key].uploadFileFormList = [];
                                }
                            })
                            this.setState({ applicationFile: Object.values(res) });
                            this.countFileSize(Object.values(res));
                        } else if (res.status) {
                            this.setState({ applicationFile: [], fileSizeCount:0 });
                            alert("申請ファイル一覧の取得に失敗しました。");
                        } else {
                            this.setState({ applicationFile: [], fileSizeCount:0 });
                            alert("該当する申請ファイルは一件もありません。");
                        }
                    }).catch(error => {
                        this.setState({ applicationFile: [], fileSizeCount:0 });
                        console.error('処理に失敗しました', error);
                        alert('処理に失敗しました');
                    });
            }else{
                this.countFileSize(this.props.viewState.applicationFile);
            }
        }
        this.getExplanation();
        this.getWindowSize();
    }

    /**
     * 高さ再計算
     */
    getWindowSize() {
        if(this.props.viewState.showUploadApplicationInformation){
            let win = window;
            let e = window.document.documentElement;
            let g = window.document.documentElement.getElementsByTagName('body')[0];
            let h = win.innerHeight|| e.clientHeight|| g.clientHeight;
            const getRect = document.getElementById("UploadFileArea");
            let height = h - getRect.getBoundingClientRect().top - 140;
            this.setState({height: height});
        }
    }

    /**
     * リサイズ
     */
    componentWillMount () {
        window.addEventListener('resize', () => {
                this.getWindowSize() 
        })
    }

    /**
     * 申請内容確認画面へ遷移
     */
    next() {
        let permission = true;
        let applicationFile = this.state.applicationFile;
        Object.keys(applicationFile).map(key => {
            if (applicationFile[key].requireFlag) {
                if ((applicationFile[key].applicationFileId !== 9999 && applicationFile[key].applicationFileId !== '9999') && (!applicationFile[key] || Object.keys(applicationFile[key]["uploadFileFormList"])?.length < 1)) {
                    permission = false;
                }
            }
        });
        if (!permission) {
            alert("登録されていない必須ファイルがあります");
        } else {
            // 申請内容確認へ遷移
            this.props.viewState.moveToConfirmApplicationDetailsView(this.state.applicationFile);
        }
    }

    /**
     * 申請者情報入力画面に戻る
     */
    back() {
        //　入力情報を保持して申請者情報に戻る
        this.props.viewState.backToEnterApplicantInformationView(this.state.applicationFile);
    }

    /**
     * アップロード処理
     * @param {event} event イベント
     * @param {number} 対象申請ファイルのindex
     */
    fileUpload(event, index) {
        const applicationFile = this.state.applicationFile;
        let files = event.target.files;
        let fileSizeCount = this.state.fileSizeCount;
        // 1ファイルあたり容量上限チェック
        let maxFileSize = Config.config.maxFileSize;
        let maxFileSizeOfByte = maxFileSize*1024*1024;
        if (files[0].size > maxFileSizeOfByte) {
            alert(maxFileSize + "M以下のファイルをアップロードしてください。");
            return false;
        };

        //　アップロード1回あたり容量上限チェック
        fileSizeCount = fileSizeCount + files[0].size;
        let maxRequestFileSize = Config.config.maxRequestFileSize;
        let maxRequestFileSizeOfByte = maxRequestFileSize*1024*1024;
        if (fileSizeCount > maxRequestFileSizeOfByte) {
            alert("アップロードされたファイルのサイズ合計を" + maxRequestFileSize + "Mを超えています。");
            return false;
        };

        // 拡張子チェック
        let extension = files[0].name.split('.').pop();
        if(applicationFile[index].extension){
            let allowExts = applicationFile[index].extension.split(',')
            if(allowExts.indexOf(extension) === -1){
                alert("【拡張子】のいずれかのファイル形式のファイルをアップロードしてください。");
                return false;
            }
        }

        let reader = new FileReader();
        reader.readAsDataURL(files[0]);
        reader.onload = (e) => {
            let applicationFileId = applicationFile[index].applicationFileId;
            applicationFile[index]["uploadFileFormList"] = applicationFile[index]["uploadFileFormList"].filter((uploadApplicationFile) => uploadApplicationFile.uploadFileName !== files[0].name);
            applicationFile[index]["uploadFileFormList"].push({ "fileId": "", "applicantId": "", "applicationFileId": "", "uploadFileName": files[0].name, "filePath": files[0].name, "uploadFile": files[0], "versionInformation": 1,"extension":extension });
            this.setState({
                applicationFile: applicationFile,
            });
            this.countFileSize(applicationFile);
            document.getElementById("upload" + applicationFileId).value = "";
        }
    }

    /**
     * 添付されたファイルのサイズを合計する
     * @param {*} applicationFile 申請ファイル
     */
    countFileSize(applicationFile){
        let fileSize = 0;
        Object.keys(applicationFile).map(index => {
            Object.keys( applicationFile[index]["uploadFileFormList"]).map(key => {
                fileSize = fileSize + applicationFile[index]["uploadFileFormList"][key]["uploadFile"].size;
            });
        });

        this.setState({fileSizeCount:fileSize});
    }

    /**
     * ファイル削除
     * @param {event} event イベント
     * @param {number} 対象申請ファイルのindex
     */
    fileDelete(event, index) {
        let applicationFile = this.state.applicationFile;
        applicationFile[index]["uploadFileFormList"] = [];
        this.setState({
            applicationFile: applicationFile,
        });

        this.countFileSize(applicationFile);
    }

    /**
     * DBから申請対象ファイルアップロード処理の説明文言取得
     */
    getExplanation(){

        let explanation = this.state.explanation;

        //サーバからlabelを取得
        fetch(Config.config.apiUrl + "/label/1006")
        .then(res => res.json())
        .then(res => {
            if(res.status === 401){
                alert("認証情報が無効です。ページの再読み込みを行います。");
                window.location.reload();
                return null;
            }
            if (Object.keys(res).length > 0) {
                let message = res[0]?.labels?.content;
                message = message.replace("${maxFileSize}",Config.config.maxFileSize).replace("${maxRequestFileSize}",Config.config.maxRequestFileSize)
                explanation = { content:  message};
                this.setState({ explanation: explanation });
            }else{
                alert("labelの取得に失敗しました。");
            }
        }).catch(error => {
            console.error('通信処理に失敗しました', error);
            alert('通信処理に失敗しました');
        });
    }

    render() {
        const title = "2. 申請ファイル";
        const explanation = this.state.explanation.content;
        const applicationFile = this.state.applicationFile;
        const height = this.state.height;
        
        return (
            <>
                <div className={CustomStyle.div_area}>
                    <Box id="UploadApplicationInformation" css={`display:block`} >
                        <nav className={CustomStyle.custom_nuv} id="UploadApplicationInformationDrag">
                            申請フォーム
                        </nav>
                        <Box
                            centered
                            paddedHorizontally={3}
                            displayInlineBlock
                            className={CustomStyle.custom_content}
                        >
                            <h2 className={CustomStyle.title}>{title}</h2>
                            <p className={CustomStyle.explanation} dangerouslySetInnerHTML={{ __html: explanation }}></p>
                            <div className={CustomStyle.scrollContainer }  id="UploadFileArea" style={{height:height + "px"}}>
                                {Object.keys(applicationFile).length > 0 && (
                                    <table className={CustomStyle.selection_table}>
                                        <thead>
                                            <tr className={CustomStyle.table_header}>
                                                <th style={{ width: 170 + "px" }}>対象</th>
                                                <th style={{ width: 50 + "px" }}>拡張子</th>
                                                <th style={{ width: 170 + "px" }}>ファイル名</th>
                                                <th colSpan={2} ></th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            {Object.keys(applicationFile).map(key => (
                                                (applicationFile[key].applicationFileId !== 9999 && applicationFile[key].applicationFileId !== '9999' && (
                                                <tr key={applicationFile[key].applicationFileId}>
                                                    <td>
                                                        {!applicationFile[key].requireFlag && (
                                                            "("
                                                        )}
                                                        {applicationFile[key].applicationFileName}
                                                        {!applicationFile[key].requireFlag && (
                                                            ")"
                                                        )}
                                                    </td>
                                                    <td>{applicationFile[key].extension}</td>
                                                    <td>{applicationFile[key] && (
                                                        applicationFile[key]["uploadFileFormList"]?.map(uploadApplicationFile => { return uploadApplicationFile.uploadFileName }).filter(uploadFileName => { return uploadFileName !== null }).join(",")
                                                    )}</td>
                                                    <td>
                                                        <div className={CustomStyle.table_button_box}>
                                                            {Object.keys(applicationFile[key]["uploadFileFormList"]).length >= 1 && (
                                                                <label className={CustomStyle.add_button_label} tabIndex="0">
                                                                    <input type="file" className={CustomStyle.upload_button} onChange={e => { this.fileUpload(e, key) }} accept=".pdf, image/*" id={"upload" + applicationFile[key].applicationFileId} />
                                                                    追加
                                                                </label>
                                                            )}
                                                            {Object.keys(applicationFile[key]["uploadFileFormList"]).length < 1 && (
                                                                <label className={CustomStyle.upload_button_label} tabIndex="0">
                                                                    <input type="file" className={CustomStyle.upload_button} onChange={e => { this.fileUpload(e, key) }} accept=".pdf, image/*" id={"upload" + applicationFile[key].applicationFileId} />
                                                                    登録
                                                                </label>
                                                            )}
                                                        </div>
                                                    </td>
                                                    <td>
                                                        <div className={CustomStyle.table_button_box}>
                                                            <button
                                                                className={CustomStyle.upload_delete_button}
                                                                onClick={e => {
                                                                    this.fileDelete(e, key)
                                                                }}
                                                            >
                                                                <span>削除</span>
                                                            </button>
                                                        </div>
                                                    </td>
                                                </tr>
                                                ))
                                            ))}
                                        </tbody>
                                    </table>
                                )}
                            </div>
                        </Box>
                    </Box >
                </div>

                <div className={CustomStyle.div_area} >
                    {Object.keys(applicationFile).length > 0 && (
                    <Box padded paddedHorizontally={3} paddedVertically={2} css={`display:block; text-align:center `} >
                        <button
                            className={`${CustomStyle.btn_baise_style} `}
                            style={{width:"45%"}}
                            onClick={e => {
                                this.next();
                            }}
                        >
                            <span>次へ</span>
                        </button>
                    
                        <button
                            className={`${CustomStyle.btn_baise_style} ${CustomStyle.btn_gry}`}
                            style={{width:"45%"}}
                            onClick={e => {
                                this.back();
                            }}
                        >
                            <span>戻る</span>
                        </button>
                    </Box>
                    )}
                </div>
            </>
        );
    }
}
export default withTranslation()(withTheme(UploadApplicationInformation));