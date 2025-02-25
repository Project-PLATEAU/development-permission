import { observer } from "mobx-react";
import PropTypes, { object } from "prop-types";
import React from "react";
import { withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import Box from "../../../../Styled/Box";
import CustomStyle from "./scss/upload-applicant-Information.scss";
import Config from "../../../../../customconfig.json";
import DepartmentsSelection from "./DepartmentsSelection";

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
            //再申請ファイル一覧
            reApplicationFile: props.viewState.reApplicationFile,
            //対象一覧の高さ
            height: 0,
            //添付されたファイルのサイズの合計
            fileSizeCount: 0,
            //申請ファイルアップロードの案内文言
            explanation: {},
            //申請ファイルアップロードチェックエラー一覧
            errorItems: {},
            //部署選択モーダル表示フラグ
            departmentsSelectionViewFlag: false,
            //選択中のファイルID
            checkedApplicationFileId: null,
            //選択中の部署一覧
            checkedDepartments:null
        };
    }

    /**
     * 初期処理
     */
    componentDidMount() {
        document.getElementById("loading").style.display = "block";
        if(this.props.viewState.isReApply){
            // 再申請の場合、保持された入力情報がある場合はAPIリクエストを行わない
            if (Object.keys(this.state.reApplicationFile).length < 1) {
                let answerContent = this.props.viewState.answerContent;
                let loginId = answerContent["loginId"];
                let password = answerContent["password"];
                let generalConditionDiagnosisResult = this.props.viewState.generalConditionDiagnosisResult;
                if(this.props.viewState.reapplyApplicationStepId == 3){
                    generalConditionDiagnosisResult = this.props.viewState.reApplication.generalConditionDiagnosisResultForm;
                }
                generalConditionDiagnosisResult = Object.values(generalConditionDiagnosisResult);
                generalConditionDiagnosisResult = generalConditionDiagnosisResult.filter(Boolean);
                const applicationStepId = this.props.viewState.reapplyApplicationStepId;
                const preApplicationStepId = this.props.viewState.preReapplyApplicationStepId;
                // 再申請情報取得
                fetch(Config.config.apiUrl + "/application/reapply/applicationFiles", {
                    method: 'POST',
                    body: JSON.stringify({
                        loginId:loginId,
                        password:password,
                        applicationId: this.props.viewState.reApplication.applicationId,
                        applicationStepId: applicationStepId,
                        preApplicationStepId: preApplicationStepId,
                        generalConditionDiagnosisResultFormList: generalConditionDiagnosisResult
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
                    if (Object.keys(res).length > 0 && !res.status) {
                        // 表示している申請ファイルが、DBへ登録するか、ファイルがアップロードするかをなしで初期化
                        Object.keys(res).map(key1 => {
                            if (res[key1].uploadFileFormList) {
                                Object.keys(res[key1].uploadFileFormList).map(key2 => {
                                    if(applicationStepId == preApplicationStepId){
                                        // 再申請の時、既存のファイルをDBへ追加登録するのフラグ
                                        res[key1].uploadFileFormList[key2]["addFlag"] = 1;
                                    }else{
                                        // 再申請（次の段階へ引継）の時、既存のファイルをDBへ追加登録するのフラグ
                                        res[key1].uploadFileFormList[key2]["addFlag"] = 1;
                                    }
                                    // 再申請の時、既存のファイル実体をアップロードフラグ
                                    res[key1].uploadFileFormList[key2]["fileUploadFlag"] = 0;
                                })
                            }
                        })
                        this.setState({ applicationFile: Object.values(res) });
                    } else if (res.status) {
                        this.setState({ applicationFile: [], fileSizeCount:0 });
                        alert("申請ファイル一覧の取得に失敗しました。");
                    } else {
                        this.setState({ applicationFile: [], fileSizeCount:0 });
                        alert("該当する申請ファイルは一件もありません。");
                    }
                    this.getExplanation();
                }).catch(error => {
                    console.error('通信処理に失敗しました', error);
                    alert('通信処理に失敗しました');
                });
            }else{
                this.setState({ applicationFile: this.props.viewState.reApplicationFile });
                this.getExplanation();
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
                        } else if (res.status) {
                            this.setState({ applicationFile: [], fileSizeCount:0 });
                            alert("申請ファイル一覧の取得に失敗しました。");
                        } else {
                            this.setState({ applicationFile: [], fileSizeCount:0 });
                            alert("該当する申請ファイルは一件もありません。");
                        }
                        this.getExplanation();
                    }).catch(error => {
                        this.setState({ applicationFile: [], fileSizeCount:0 });
                        console.error('処理に失敗しました', error);
                        alert('処理に失敗しました');
                    });
            }else{
                this.getExplanation();
            }
        }
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
            let height = h - getRect.getBoundingClientRect().top - 80;
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
        let errorType = [];
        let applicationFile = this.state.applicationFile;
        let errorItems = {};
        Object.keys(applicationFile).map(key => {
            //通常の必須チェック
            if (applicationFile[key].requireFlag == "1") {
                if ((applicationFile[key].applicationFileId !== 9999 && applicationFile[key].applicationFileId !== '9999') && (!applicationFile[key] || Object.keys(applicationFile[key]["uploadFileFormList"])?.length < 1)) {
                    errorType.push(1);
                    errorItems[key] = true;
                }
            }
            //再申請の場合は担当課必須チェック
            if(this.isResubmissionOfPriorConsultation()){
                //アップロード対象があるにも関わらず担当課が選択されていない場合は必須エラー
                if(applicationFile[key].applicationFileId !== 9999 
                    && applicationFile[key].applicationFileId !== '9999' 
                        && applicationFile[key]["uploadFileFormList"] 
                            && applicationFile[key]["uploadFileFormList"]?.filter(item => item.fileUploadFlag === 1)?.length >= 1
                                && (!applicationFile[key].departmentFormList || Object.keys(applicationFile[key].departmentFormList).length < 1)){
                                    errorType.push(2);
                                    errorItems[key] = true;
                }
            }
        });
        //通常の必須チェック
        if (errorType.length > 0 && errorType[0] == 1) {
            alert("登録されていない必須ファイルがあります");
            this.setState({errorItems:errorItems});
        //再申請の場合は担当課必須チェック
        } else if (errorType.length > 0 && errorType[0] == 2) {
            alert("再アップロードした申請ファイルの指示元担当課は必須です");
            this.setState({errorItems:errorItems});
        //それ以外の任意チェック
        } else {
            if (this.isResubmissionOfPriorConsultation()) {
                Object.keys(applicationFile).map(key => {
                    //担当課が選択されているにも関わらずアップロードファイルがない場合は任意エラー
                    if(applicationFile[key].applicationFileId !== 9999 
                        && applicationFile[key].applicationFileId !== '9999' 
                            && applicationFile[key]["uploadFileFormList"] 
                                && applicationFile[key]["uploadFileFormList"]?.filter(item => item.fileUploadFlag === 1)?.length < 1
                                    && (applicationFile[key].departmentFormList && Object.keys(applicationFile[key].departmentFormList).length > 0)){
                                        errorType.push(3);
                                        errorItems[key] = true;
                    }
                });
                if (errorType.length > 0 && errorType[0] == 3) {
                    var res = confirm("対象ファイルが一つもない指示元担当課及びコメントは削除されますがよろしいですか？");
                    if(res){
                        Object.keys(errorItems).map(key => {
                            applicationFile[key].departmentFormList = [];
                            applicationFile[key].reviseContent = null;
                        })
                        errorType = [];
                        errorItems = {};
                    }else{
                        this.setState({errorItems:errorItems});
                        return;
                    }
                }
            }
            //必須チェックをスルーした場合のみ任意チェック(ハイライトが重複する為)
            Object.keys(applicationFile).map(key => {
                //任意チェック（注意文言あり）
                if (applicationFile[key].requireFlag == "2") {
                    if ((applicationFile[key].applicationFileId !== 9999 && applicationFile[key].applicationFileId !== '9999') && (!applicationFile[key] || Object.keys(applicationFile[key]["uploadFileFormList"])?.length < 1)) {
                        errorItems[key] = true;
                    }
                }
            });

            if(Object.keys(errorItems).length > 0){    
                var res = confirm(this.state.explanation.fileInfoMessage);
                if(res == true){
                    if(this.isResubmissionOfPriorConsultation()){
                        //担当部署の整形
                        Object.keys(applicationFile).map(index=>{
                            applicationFile[index].directionDepartmentId = applicationFile[index].departmentFormList?.map(department=>department.departmentId).join(",");
                            applicationFile[index].directionDepartment = applicationFile[index].departmentFormList?.map(department=>department.departmentName).join(",");
                            if(applicationFile[index]["uploadFileFormList"]){
                                Object.keys(applicationFile[index]["uploadFileFormList"]).map(index2=>{
                                    applicationFile[index]["uploadFileFormList"][index2].directionDepartmentId = applicationFile[index].directionDepartmentId;
                                    applicationFile[index]["uploadFileFormList"][index2].directionDepartment = applicationFile[index].directionDepartment;
                                    applicationFile[index]["uploadFileFormList"][index2].reviseContent = applicationFile[index].reviseContent?applicationFile[index].reviseContent:"";
                                    //ローカル保持用
                                    applicationFile[index]["uploadFileFormList"][index2].departmentFormList = applicationFile[index].departmentFormList;
                                })
                            }
                        })
                    }
                    //申請内容確認へ遷移
                    this.props.viewState.moveToConfirmApplicationDetailsView(applicationFile);
                }else{
                    this.setState({errorItems:errorItems});
                }
            }else{
                if(this.isResubmissionOfPriorConsultation()){
                    //担当部署の整形
                    Object.keys(applicationFile).map(index=>{
                        applicationFile[index].directionDepartmentId = applicationFile[index].departmentFormList?.map(department=>department.departmentId).join(",");
                        applicationFile[index].directionDepartment = applicationFile[index].departmentFormList?.map(department=>department.departmentName).join(",");
                        if(applicationFile[index]["uploadFileFormList"]){
                            Object.keys(applicationFile[index]["uploadFileFormList"]).map(index2=>{
                                applicationFile[index]["uploadFileFormList"][index2].directionDepartmentId = applicationFile[index].directionDepartmentId;
                                applicationFile[index]["uploadFileFormList"][index2].directionDepartment = applicationFile[index].directionDepartment;
                                applicationFile[index]["uploadFileFormList"][index2].reviseContent = applicationFile[index].reviseContent?applicationFile[index].reviseContent:"";
                                //ローカル保持用
                                applicationFile[index]["uploadFileFormList"][index2].departmentFormList = applicationFile[index].departmentFormList;
                            })
                        }
                    })
                }
                //申請内容確認へ遷移
                this.props.viewState.moveToConfirmApplicationDetailsView(applicationFile);
            }
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


        // 拡張子チェック
        let extension = files[0].name.split('.').pop();
        if(applicationFile[index].extension){
            let allowExts = applicationFile[index].extension.split(',')
            if(allowExts.indexOf(extension) === -1){
                alert("【拡張子】のいずれかのファイル形式のファイルをアップロードしてください。");
                return false;
            }
        }

        // ファイル名チェック
        const fileNameWithoutExtension = files[0].name.split(".").slice(0, -1).join(".");
        const regex = /[\"\'<>\&]/;
        const reg = new RegExp(regex);
        const result = reg.exec(fileNameWithoutExtension);
        if (result != null) {
            alert("ファイル名に禁止文字("+'"'+",',<,>,&"+")のいずれか含めていますので、ファイル名を修正してアップロードしてください。");
            return false;
        }

        const applicationStepId = this.props.viewState.reapplyApplicationStepId;
        const preApplicationStepId = this.props.viewState.preReapplyApplicationStepId;
        let reader = new FileReader();
        reader.readAsDataURL(files[0]);
        reader.onload = (e) => {
            let applicationFileId = applicationFile[index].applicationFileId;
            
            // 申請ファイルを差し替える または、申請ファイルの新規追加の場合、レコード追加、ファイル実体もアップロードにする
            applicationFile[index]["uploadFileFormList"] = applicationFile[index]["uploadFileFormList"].filter((uploadApplicationFile) => uploadApplicationFile.uploadFileName !== files[0].name);
            applicationFile[index]["uploadFileFormList"].push({ "fileId": "", "applicantId": "","applicationStepId":"", "applicationFileId": "", "uploadFileName": files[0].name, "filePath": files[0].name, "uploadFile": files[0], "versionInformation": 1,"extension":extension, "addFlag":1, "fileUploadFlag":1 });
            this.setState({
                applicationFile: applicationFile,
            });
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
                message = message.replace("${maxFileSize}",Config.config.maxFileSize);
                explanation = { content:  message, fileInfoMessage: res[0]?.labels?.fileInfoMessage};
                this.setState({ explanation: explanation });
                this.getWindowSize();
            }else{
                alert("labelの取得に失敗しました。");
            }
        }).catch(error => {
            console.error('通信処理に失敗しました', error);
            alert('通信処理に失敗しました');
        }).finally(()=>{ if(document.getElementById("loading")){document.getElementById("loading").style.display = "none";}});
    }

    /**
     * 事前協議の再申請かをチェックする
     */
    isResubmissionOfPriorConsultation(){
        if(this.props.viewState.isReApply
            && this.props.viewState.reapplyApplicationStepId == 2
            && this.props.viewState.reapplyApplicationStepId == this.props.viewState.preReapplyApplicationStepId
            && this.props.viewState.reApplication?.acceptVersionInformation != 0
        ){
            return true;
        }else{
            return false;
        }
    }

    render() {
        const title = "2. 申請ファイル";
        const explanation = this.state.explanation.content;
        const applicationFile = this.state.applicationFile;
        const height = this.state.height;
        const errorItems = this.state.errorItems;
        const departmentsSelectionViewFlag = this.state.departmentsSelectionViewFlag;
        const checkedDepartments = this.state.checkedDepartments;
        const maxLength =  Config.inputMaxLength.reviseContent;
        return (
            <>
                <div className={CustomStyle.div_area}>
                    <Box id="UploadApplicationInformation" css={`display:block`} style={{height: 'calc(100vh - 230px)'}}>
                        <nav className={CustomStyle.custom_nuv} id="UploadApplicationInformationDrag">
                            申請フォーム
                        </nav>
                        <div id="loading" className={CustomStyle.customloaderParent} >
                            <img className={CustomStyle.customloader} src="./images/loader.gif" />
                        </div>
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
                                                <th style={{ width: "100px" }}>対象</th>
                                                <th style={{ width: "60px" }}>拡張子</th>
                                                <th style={{ width: "100px" }}>ファイル名</th>
                                                <th className="no-sort" colSpan={2} style={{ width: "100px" }}></th>
                                                {this.isResubmissionOfPriorConsultation() && (
                                                    <th className="no-sort" style={{ width: "150px" }}>指示元担当課</th>
                                                )}
                                                {this.isResubmissionOfPriorConsultation() && (
                                                    <th className="no-sort" style={{ width: "200px" }}>修正内容</th>
                                                )}
                                            </tr>
                                        </thead>
                                        <tbody>
                                            {Object.keys(applicationFile).map(key => (
                                                (applicationFile[key].applicationFileId !== 9999 && applicationFile[key].applicationFileId !== '9999' && (
                                                <tr key={ key + `-` + applicationFile[key].applicationFileId}  className={errorItems[key] ? CustomStyle.highlight : ""}>
                                                    <td>
                                                        {applicationFile[key].requireFlag != "1" && (
                                                            "("
                                                        )}
                                                        {applicationFile[key].applicationFileName}
                                                        {applicationFile[key].requireFlag != "1" && (
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
                                                    {this.isResubmissionOfPriorConsultation() && (
                                                        <td style={{display:"flex",justifyContent:"center",alignItems:"center"}}>
                                                            <div style={{width:"55%"}}>
                                                                {
                                                                    applicationFile[key].departmentFormList?.map(item => item?.departmentName).join(', ')
                                                                }
                                                            </div>
                                                            <div>
                                                                <button
                                                                    className={`${CustomStyle.btn_baise_style} ${CustomStyle.department_btn} `}
                                                                    onClick={e => {
                                                                        this.setState({departmentsSelectionViewFlag:true,checkedApplicationFileId:applicationFile[key].applicationFileId,checkedDepartments:applicationFile[key].departmentFormList});
                                                                    }}
                                                                    disabled={!applicationFile[key]["uploadFileFormList"] || applicationFile[key]["uploadFileFormList"]?.filter(item => item.fileUploadFlag === 1)?.length < 1}
                                                                >
                                                                    <span>担当課選択</span>
                                                                </button>
                                                            </div>
                                                        </td>
                                                    )}
                                                    {this.isResubmissionOfPriorConsultation() && (
                                                    <td>
                                                        <textarea
                                                            rows={3}
                                                            maxLength={maxLength + 1}
                                                            className={`${CustomStyle.revise_content} `}
                                                            value={applicationFile[key].reviseContent}
                                                            placeholder={"修正内容を入力してください"}
                                                            onChange={e => {
                                                                if(e.target.value.length > maxLength){
                                                                    alert(maxLength+"文字以内で入力してください。");
                                                                    return;
                                                                }
                                                                const applicationFileId = applicationFile[key]?.applicationFileId;
                                                                if(applicationFileId){
                                                                    const _applicationFile = this.state.applicationFile;
                                                                    const _index = _applicationFile.findIndex(file=>file.applicationFileId == applicationFileId);
                                                                    _applicationFile[_index].reviseContent = e.target.value;
                                                                    this.setState({applicationFile:_applicationFile})
                                                                }
                                                            }}
                                                            disabled={!applicationFile[key]["uploadFileFormList"] || applicationFile[key]["uploadFileFormList"]?.filter(item => item.fileUploadFlag === 1)?.length < 1}
                                                        />
                                                    </td>
                                                    )}
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
                </div>

                {departmentsSelectionViewFlag && (
                    <DepartmentsSelection terria={this.props.terria} viewState={this.props.viewState} 
                                t={this.props.t} confirmedCallback={(departments)=>{
                                    const checkedApplicationFileId = this.state.checkedApplicationFileId;
                                    const index = applicationFile.findIndex(file=>file.applicationFileId == checkedApplicationFileId);
                                    if(index > -1){
                                        applicationFile[index].departmentFormList = departments.filter(department=>department.checked);
                                        this.setState({applicationFile:applicationFile,departmentsSelectionViewFlag:false});
                                    }
                                }}
                                closeCallback={()=>{this.setState({departmentsSelectionViewFlag:false})}}
                                checkedDepartments={checkedDepartments}/>
                )}
            </>
        );
    }
}
export default withTranslation()(withTheme(UploadApplicationInformation));