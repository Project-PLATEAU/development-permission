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
import Icon, { StyledIcon } from "../../../../Styled/Icon";
import FileDownLoadModal from "../../../AdminPermissionSystem/Views/Modal/FileDownLoadModal";

/**
 * 【R6】申請添付ファイル一覧コンポーネント
 *  ※:行政の場合、申請段階ごとのすべての申請ファイルを表示
 */
@observer
class ApplicationFileList extends React.Component {
    static displayName = "ApplicationFileList";
    static propTypes = {
        terria: PropTypes.object.isRequired,
        viewState: PropTypes.object.isRequired,
        theme: PropTypes.object,
        t: PropTypes.func.isRequired,
        applicationFiles: PropTypes.array
    }
    constructor(props) {
        super(props);
        this.state = {
            viewState: props.viewState,
            terria: props.terria,
            //申請ファイルリスト
            applicationFiles: props.applicationFiles,
            departments:[]
        };
    }

    /**
     * 初期処理
     */
    componentDidMount() {
        // 部署一覧取得
        fetch(Config.config.apiUrl + "/application/departments")
        .then(res => {
            return res.json();
        })
        .then(res => {
            if(res && Object.keys(res).length > 0){
                this.setState({departments:res});
            }
        }).catch(error => {
            console.error('通信処理に失敗しました', error);
            alert('通信処理に失敗しました');
        });
    }

    /**
     * props更新時
     */
    componentDidUpdate(prevProps) {
        if (!_isEqual(this.props.applicationFiles, prevProps.applicationFiles)) {
            this.setState({applicationFiles:this.props.applicationFiles});
        }
    }

    /**
     * ファイルダウンロード
     * @param {Object} applicationFile 対象ファイル情報
     */
    output(applicationFile) {
        let applicationFileHistorys = applicationFile.applicationFileHistorys;
        // 事業者の場合、最新版の申請ファイルだけがダウンロードできるため、「uploadFileFormList」をダウンロード
        if(!this.props.terria.authorityJudgment()){
            applicationFileHistorys = applicationFile.uploadFileFormList;
        }
        let target = applicationFile.applicationFileName;

        // 対象ごとに申請ファイル（全て版を含む）が1件のみ場合、直接ダウンロードする
        if(Object.keys(applicationFileHistorys).length == 1){
            this.outputFile("/application/file/download", applicationFileHistorys[0],"uploadFileName");
        }else{
            this.openDownFileView(applicationFileHistorys,target);
        }
    }

    /**
     * ファイルダウンロードモーダルを開く
     * @param {*} applicationFile 対象ファイル
     * @param {*} target ターゲット
     */
    openDownFileView(applicationFile,target){
        this.props.viewState.setFileDownloadTarget(applicationFile,target)
        this.props.viewState.changeFileDownloadModalShow();
    }

    /**
     * ファイルダウンロード
     * @param {string} path　apiのpath
     * @param {object} file 対象ファイル情報
     * @param {string} fileNameKey 対象ファイルの取得key
     */
    outputFile(path, file, fileNameKey) {
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

    /**
     * 部署名一覧
     * @param {String} directionDepartmentId 部署名
     */
    getDirectionDepartmentNames(directionDepartmentId){
        let directionDepartmentNames = "";
        if(directionDepartmentId){
            const departments = this.state.departments;
            const directionDepartmentIdList = directionDepartmentId.split(",");
            const directionDepartmentNameList = [];
            Object.keys(directionDepartmentIdList).map(key=>{
                const index = departments.findIndex(department=>department.departmentId == directionDepartmentIdList[key]);
                if(index > -1){
                    directionDepartmentNameList.push(departments[index].departmentName);
                }
            })
            directionDepartmentNames = directionDepartmentNameList.join(",");
        }
        return directionDepartmentNames;
    }

    render() {
        const applicationFiles = this.state.applicationFiles;
        return (
            <>
                <Box
                    centered
                    displayInlineBlock
                    className={CustomStyle.custom_content}
                >
                    <h2 className={CustomStyle.title}>申請添付ファイル一覧</h2>
                    <Spacing bottom={1} />
                    <div className={CustomStyle.scroll_container} id="ApplicationFileListTable" style={{height: "18vh", minHeight:"200px"}}>    
                        <table className={CustomStyle.selection_table}>
                            <thead>
                                <tr className={CustomStyle.table_header}>
                                    <th className="no-sort" style={{ width: "20%"}}>申請ファイル</th>
                                    <th style={{ width: "35%"}}>対象</th>
                                    <th style={{ width: "15%"}}>拡張子</th>
                                    <th style={{ width: "30%"}}>ファイル名</th>
                                </tr>
                            </thead>
                            <tbody>
                                {applicationFiles && Object.keys(applicationFiles).map(index => (
                                    (applicationFiles[index]["uploadFileFormList"] && Object.keys(applicationFiles[index]["uploadFileFormList"]).length > 0 && (
                                        <tr key={`applicationFile-${index}`}>
                                            <td>
                                                <button 
                                                    className={CustomStyle.download_button} 
                                                    onClick={e => {
                                                        this.output(applicationFiles[index]);
                                                    }}>
                                                    <span>ダウンロード</span>
                                                </button>
                                            </td>
                                            <td>
                                                {applicationFiles[index].applicationFileName}
                                                {applicationFiles[index]["uploadFileFormList"] &&
                                                    applicationFiles[index]["uploadFileFormList"]
                                                        .findIndex(file=>file.directionDepartmentId != undefined && file.directionDepartmentId != null) > -1 && (
                                                        <span className={CustomStyle.info_icon}>
                                                            <StyledIcon 
                                                                glyph={Icon.GLYPHS.info}
                                                                styledWidth={"25px"}
                                                                styledHeight={"25px"}
                                                                fillColor="#fff"
                                                                light
                                                                
                                                            />
                                                            <span className={CustomStyle.info_comment}>
                                                                {Object.keys(applicationFiles[index]["uploadFileFormList"]).map(subIndex => (
                                                                    (applicationFiles[index]["uploadFileFormList"][subIndex].directionDepartmentId && (
                                                                        <span>
                                                                            <span style={{fontWeight:"bold"}}>■ファイル名</span><br/>{applicationFiles[index]["uploadFileFormList"][subIndex].uploadFileName}<br/>
                                                                            <span style={{fontWeight:"bold"}}>■指示元担当課</span><br/>{this.getDirectionDepartmentNames(applicationFiles[index]["uploadFileFormList"][subIndex].directionDepartmentId)}<br/>
                                                                            <span style={{fontWeight:"bold"}}>■修正内容</span><br/>{applicationFiles[index]["uploadFileFormList"][subIndex].reviseContent}<br/>
                                                                            <hr/>
                                                                        </span>
                                                                    ))
                                                                ))}
                                                            </span>
                                                        </span>
                                                )}
                                            </td>
                                            <td>
                                                {applicationFiles[index].extension}
                                            </td>
                                            <td>
                                                {applicationFiles[index]["uploadFileFormList"][0].uploadFileName}
                                                {Object.keys(applicationFiles[index]["uploadFileFormList"] ).length > 1 && ( `,...` )}
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
export default withTranslation()(withTheme(ApplicationFileList));