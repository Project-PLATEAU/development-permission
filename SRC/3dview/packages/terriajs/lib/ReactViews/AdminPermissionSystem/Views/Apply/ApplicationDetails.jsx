import { observer } from "mobx-react";
import { reaction } from "mobx";
import PropTypes from "prop-types";
import React from "react";
import { withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import Icon, { StyledIcon } from "../../../../Styled/Icon";
import Spacing from "../../../../Styled/Spacing";
import Box from "../../../../Styled/Box";
import CustomStyle from "./scss/application-details.scss";
import Common from "../../../AdminDevelopCommon/CommonFixedValue.json";
import Config from "../../../../../customconfig.json";
import ShowMessage from "../Message/ShowMessage";
import AdminTab from "../Tab/AdminTab";
import Styles from "../../../DevelopmentPermissionSystem/PageViews/scss/pageStyle.scss";
import FileDownLoadModal from "../Modal/FileDownLoadModal";
import Loader from "../../../Loader";

/**
 * 申請情報詳細画面
 */
@observer
class ApplicationDetails extends React.Component {

    static displayName = "ApplicationDetails";

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
            //申請者情報
            applicantInformation: [],
            //申請区分
            checkedApplicationCategory: [],
            //申請ファイル
            applicationFile: [],
            //回答
            answers: [],
            //申請地番
            lotNumber: [],
            //申請状態
            status: "",
            //回答通知権限
            notificable: false,
            //申請ID
            applicationId: null,
            //申請回答DTO
            ApplyAnswerForm: {},
            //ファイルダウンロードモーダル
            fileDownLoadModalShow: false,
            //回答履歴
            answerHistory: [],
            // 回答ファイル更新履歴
            answerFileHistory: []
        };
    }

    /**
     * 初期処理（サーバからデータを取得）
     */
    componentDidMount() {
        if (this.props.viewState.applicationInformationSearchForApplicationId) {
            document.getElementById("customloader_sub").style.display = "block"
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
                        this.setState({
                            applicantInformation: res.applicantInformations,
                            checkedApplicationCategory: res.applicationCategories,
                            applicationFile: res.applicationFiles,
                            answers: res.answers,
                            lotNumber: res.lotNumbers,
                            status: res.status,
                            notificable: res.notificable,
                            applicationId: res.applicationId,
                            answerHistory: res.answerHistory,
                            answerFileHistory: res.answerFileHistory,
                            ApplyAnswerForm: res
                        },
                        this.props.viewState.setLotNumbers(res.lotNumbers));
                    } else {
                        alert("申請情報詳細取得に失敗しました。再度操作をやり直してください。");
                    }
                }).catch(error => {
                    console.error('通信処理に失敗しました', error);
                    alert('通信処理に失敗しました');
                }).finally(() => document.getElementById("customloader_sub").style.display = "none");
        } else {
            alert("申請情報詳細取得に失敗しました。再度操作をやり直してください。");
            document.getElementById("customloader_sub").style.display = "none";
        }
    }

    /**
     * status更新用リフレッシュ
     */
    refresh(){
        document.getElementById("customloader_sub").style.display = "block"
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
                this.setState({
                    applicantInformation: res.applicantInformations,
                    checkedApplicationCategory: res.applicationCategories,
                    applicationFile: res.applicationFiles,
                    answers: res.answers,
                    lotNumber: res.lotNumbers,
                    status: res.status,
                    notificable: res.notificable,
                    applicationId: res.applicationId,
                    answerHistory: res.answerHistory,
                    ApplyAnswerForm: res
                });
            } else {
                alert("申請情報詳細取得に失敗しました。再度操作をやり直してください。");
            }
        }).catch(error => {
            console.error('通信処理に失敗しました', error);
            alert('通信処理に失敗しました');
        }).finally(() => document.getElementById("customloader_sub").style.display = "none");
    }

    /**
     * ファイルダウンロード
     * @param {string} apiのpath
     * @param {object} 対象ファイル情報
     */
    //output(path, file) {
    //output(target) {
    //    this.props.viewState.setFileDownloadTarget(target)
    //    this.state.viewState.changeFileDownloadModalShow();
        // // APIへのリクエスト
        // fetch(Config.config.apiUrl + path, {
        //     method: 'POST',
        //     body: JSON.stringify(file),
        //     headers: new Headers({ 'Content-type': 'application/json' }),
        // })
        //     .then((res) => res.blob())
        //     .then(blob => {
        //         const now = new Date();
        //         let anchor = document.createElement("a");
        //         anchor.href = window.URL.createObjectURL(blob);
        //         anchor.download = file.uploadFileName;
        //         anchor.click();
        //     })
        //     .catch(error => {
        //         console.error('処理に失敗しました', error);
        //         alert('処理に失敗しました');
        //     });
    //}
    /**
     * ファイルダウンロード
     * @param {*} path ファイルダウンロード用APIのパス
     * @param {*} file ダウンロード対象ファイル
     * @param {*} fileNameKey ファイル名に対するキー項目
     */
    outputFile(path, file, fileNameKey) {
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

    /**
     * ファイルダウンロード
     * @param {Object} 対象ファイル情報
     */
    output(applicationFile) {
        let applicationFileHistorys = applicationFile.applicationFileHistorys;
        let target = applicationFile.applicationFileName;
        // ダウンロード対象ファイルの版情報と異なるファイルがあれば、ファイルダウンロードモーダルを開く
        if(Object.keys(applicationFileHistorys).length == 1){
            this.outputFile("/application/file/download", applicationFileHistorys[0],"uploadFileName");
        }else{
            this.openDownFileView(applicationFileHistorys,target);
        }
    }

    /**
     * ファイルダウンロードモーダルを開く
     * @param {*} applicationFile 申請ファイル
     * @param {*} target ターゲット
     */
    openDownFileView(applicationFile,target){
        this.props.viewState.setFileDownloadTarget(applicationFile,target)
        this.state.viewState.changeFileDownloadModalShow();
    }

    /**
     * 回答通知処理
     * @returns 
     */
    nextAnswerNotificationView() {
        const notificable = this.state.notificable;
        const status = this.state.status;
        if (!notificable) {
            alert("回答通知権限がありません。");
            return false;
        }
        document.getElementById("customloader_sub").style.display = "block"
        fetch(Config.config.apiUrl + "/answer/notification", {
            method: 'POST',
            body: JSON.stringify(this.state.ApplyAnswerForm),
            headers: new Headers({ 'Content-type': 'application/json' }),
        })
            .then(res => {
                if (res.status === 200) {
                    fetch(Config.config.apiUrl + "/label/1003")
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
                                this.props.viewState.nextAnswerNotificationView();
                            } else {
                                alert("labelの取得に失敗しました。");
                            }
                        }).catch(error => {
                            console.error('通信処理に失敗しました', error);
                            alert('通信処理に失敗しました');
                        });
                    } else if (res.status === 401){
                        // 401認証エラーの場合の処理を追加
                        alert('認証情報が無効です。ページの再読み込みを行います。');
                        window.location.href = "./login/";
                    } else if (res.status === 409) {
                        alert('回答未登録のため通知できません。回答を登録または更新してから通知を行ってください。');
                    } else {
                    alert('回答通知に失敗しました');   
                }
            }).catch(error => {
                console.error('処理に失敗しました', error);
                alert('処理に失敗しました');
            }).finally(() => document.getElementById("customloader_sub").style.display = "none");;
    }

    /**
     * 戻るボタン処理
     */
    hideApplicationDetailsView(){
        this.state.viewState.changeAdminTabActive(this.state.viewState.adminBackTab);
        this.state.viewState.adminBackPage ? this.state.viewState.changeApplyPageActive(this.state.viewState.adminBackPage): this.state.viewState.changeApplyPageActive("applyList");
    }

    render() {
        const applicantInformation = this.state.applicantInformation;
        const checkedApplicationCategory = this.state.checkedApplicationCategory;
        const applicationFile = this.state.applicationFile;
        const status = this.state.status;
        const answers = this.state.answers;
        const lotNumber = this.state.lotNumber;
        const applicationId = this.state.applicationId;
        let lotNumberResult = {};
        let departmentName = [];
        // const applicationFiles = this.state.applicationFile;
        const answerHistorys = this.state.answerHistory;
        const answerFileHistory = this.state.answerFileHistory;
        const t = this.props.t;
        Object.keys(lotNumber).map(key => {
            if (!lotNumberResult[lotNumber[key].districtName]) {
                lotNumberResult[lotNumber[key].districtName] = new Array();
            }
            lotNumberResult[lotNumber[key].districtName].push(lotNumber[key].chiban);
        });
        Object.keys(answers).map(key => {
            if (departmentName.indexOf(answers[key].judgementInformation?.department?.departmentName) < 0) {
                departmentName.push(answers[key].judgementInformation?.department?.departmentName);
            }
        });
        return (
            <>
            <div style={{ position: "absolute", left: -99999 + "px" }} id="refreshConfirmApplicationDetails" onClick={evt => {
                evt.preventDefault();
                evt.stopPropagation();
                this.refresh();
            }}></div>
            <div className={Styles.div_area}>
                <ShowMessage t={t} message={"adminInfoMessage.tipsForAnswerReister"} />
                
                {/* <AdminTab terria={this.props.terria} viewState={this.props.viewState} t={t}/> */}
            </div>
            <Box
                displayInlineBlock
                backgroundColor={this.props.theme.textLight}
                fullWidth
                fullHeight
                // id="ApplicationDetails"
                // onClick={() =>
                //     this.props.viewState.setTopElement("ApplicationDetails")}
        //         css={`
        //   position: fixed;
        //   z-index: 9992;
        // `}
                // className={CustomStyle.custom_frame}
            >
                <div id="customloader_sub" className={CustomStyle.customloaderParent}>
                    <div className={CustomStyle.customloader}>Loading...</div>
                </div>
                <nav className={CustomStyle.custom_nuv}>
                    申請情報詳細
                </nav>
                <Box
                    centered
                    paddedHorizontally={5}
                    paddedVertically={10}
                    displayInlineBlock
                    className={CustomStyle.custom_content}
                >
                    <Spacing bottom={1} />
                    <button
                        className={CustomStyle.close_button}
                        onClick={e => {
                            this.hideApplicationDetailsView();
                        }}
                    >
                        <span>戻る</span>
                    </button>
                    <Spacing bottom={3} />

                    {applicationId && (
                        <div className={CustomStyle.box}>
                            <div className={CustomStyle.item} style={{ width: 30 + "%" }}>
                                <button
                                    className={CustomStyle.action_button}
                                    onClick={e => {
                                        this.props.viewState.nextAnswerInputView(this.state.ApplyAnswerForm);
                                    }}
                                >
                                    <span>回答登録</span>
                                </button>
                            </div>
                            <div className={CustomStyle.item} style={{ width: 30 + "%" }}>
                                <button
                                    className={CustomStyle.action_button}
                                    onClick={e => {
                                        this.nextAnswerNotificationView();
                                    }}
                                >
                                    <span>回答通知</span>
                                </button>
                            </div>
                        </div>
                    )}
                    <Spacing bottom={2} />
                    <div className={CustomStyle.scrollContainer}>
                        <div style={{ height: "400px", overflowY: "auto"}}>
                            <div className={CustomStyle.box}>
                                <div className={CustomStyle.item} style={{ width: 45 + "%" }}>
                                    ■回答担当課
                                </div>
                                <div className={CustomStyle.item} style={{ width: 55 + "%" }}>
                                    {departmentName.join(",")}
                                </div>
                            </div>
                            <div className={CustomStyle.box}>
                                <div className={CustomStyle.item} style={{ width: 45 + "%" }}>
                                    ■ステータス
                                </div>
                                <div className={CustomStyle.item} style={{ width: 55 + "%" }}>
                                    {status}
                                </div>
                            </div>
                            <p>■申請者情報</p>
                            {Object.keys(applicantInformation).map(key => (
                                <div className={CustomStyle.box} key={key}>
                                    <div className={CustomStyle.item} style={{ width: 45 + "%" }}>
                                        ・{applicantInformation[key]?.name}
                                    </div>
                                    <div className={CustomStyle.item} style={{ width: 55 + "%" }}>
                                        {applicantInformation[key]?.value}
                                    </div>
                                </div>
                            ))}
                            {Object.keys(checkedApplicationCategory).map(key => (
                                <div className={CustomStyle.box} key={key}>
                                    <div className={CustomStyle.item} style={{ width: 45 + "%" }}>
                                        ■{checkedApplicationCategory[key]?.title}
                                    </div>
                                    <div className={CustomStyle.item} style={{ width: 55 + "%" }}>
                                        {checkedApplicationCategory[key]?.applicationCategory?.map(function (value) { return value.content }).join(",")}
                                    </div>
                                </div>
                            ))}
                            <div className={CustomStyle.box}>
                            <div className={CustomStyle.item} style={{ width: 60 + "%" }}>
                                ■申請地番
                            </div>
                            <div className={CustomStyle.item} style={{ width: 40 + "%" }}>
                                {Object.keys(lotNumberResult).map(key => (
                                    <p>{key} {lotNumberResult[key].map(chiban => { return chiban }).join(",")}</p>
                                ))}
                            </div>
                        </div>
                        </div>
                        <Spacing bottom={3} />
                        <div>
                            <nav className={CustomStyle.custom_nuv}>申請ファイル一覧</nav>
                            <Spacing bottom={1} />
                            <div style={{ maxHeight: "400px", overflowY: "auto"}}>
                                <table className={CustomStyle.selection_table}>
                                    <thead>
                                        <tr className={CustomStyle.table_header}>
                                            <th style={{ width: 100 + "px" }}>申請ファイル</th>
                                            <th>対象</th>
                                            <th style={{ width: 50 + "px" }}>拡張子</th>
                                            <th style={{ width: 250 + "px" }}>ファイル名</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {applicationFile && Object.keys(applicationFile).map(index => (
                                            (applicationFile[index]["uploadFileFormList"] && Object.keys(applicationFile[index]["uploadFileFormList"]).length > 0 && (
                                                <tr key={`${index}-applicationFile`}>
                                                    <td>
                                                        <button
                                                            className={CustomStyle.download_button}
                                                            onClick={e => {
                                                                this.output(applicationFile[index]);
                                                            }}
                                                        >
                                                            <span>ダウンロード</span>
                                                        </button>
                                                    </td>
                                                    <td>
                                                        {applicationFile[index]?.applicationFileName}
                                                    </td>
                                                    <td>
                                                        {applicationFile[index].extension}
                                                    </td>
                                                    <td>
                                                        {applicationFile[index]["uploadFileFormList"][0]?.uploadFileName}
                                                        {Object.keys(applicationFile[index]["uploadFileFormList"]).length > 1 && ( `,...` )}
                                                    </td>
                                                </tr>
                                            ))
                                        ))}
                                    </tbody>
                                </table>
                            </div>
                        </div>
                        <Spacing bottom={3} />
                        <div>
                            {/* 
                            <nav className={CustomStyle.custom_nuv}>事業者からの問合せ添付ファイル一覧</nav>
                            <Spacing bottom={1} />
                            <div style={{ maxHeight: "300px", overflowY: "auto"}}>
                                <table className={CustomStyle.selection_table}>
                                    <thead>
                                        <tr className={CustomStyle.table_header}>
                                            <th style={{ width: 100 + "px" }}>申請ファイル</th>
                                            <th>対象</th>
                                            <th style={{ width: 50 + "px" }}>拡張子</th>
                                            <th style={{ width: 250 + "px" }}>ファイル名</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {applicationFiles && Object.keys(applicationFiles).map(index => {
                                           return (
                                                applicationFiles[index].uploadFileFormList && Object.keys(applicationFiles[index].uploadFileFormList).map(key => {
                                                    return(
                                                        <tr>
                                                            <td>
                                                                <button 
                                                                    className={Styles.download_button} 
                                                                    onClick={e => {
                                                                        this.output(applicationFiles[index].uploadFileFormList[key],applicationFiles[index].applicationFileName);
                                                                    }}>
                                                                    <span>ダウンロード</span>
                                                                </button>
                                                            </td>
                                                            <td>{applicationFiles[index].applicationFileName}</td>
                                                            <td>{applicationFiles[index].uploadFileFormList[key].extension}</td>
                                                            <td>{applicationFiles[index].uploadFileFormList[key].uploadFileName}</td>
                                                        </tr>
                                                    );
                                                })
                                           );
                                        })}
                                    </tbody>
                                </table>
                            </div>
                            */}
                            <Spacing bottom={3} />
                            <div>
                                <nav className={CustomStyle.custom_nuv}>回答履歴</nav>
                                <Spacing bottom={1} />
                                <div style={{ maxHeight: "400px", overflowY: "auto"}}>
                                    <table className={CustomStyle.selection_table}>
                                        <thead>
                                            <tr className={CustomStyle.table_header}>
                                                <th style={{ width: 20 + "%" }}>回答日時</th>
                                                <th style={{ width: 20 + "%" }}>回答者</th>
                                                <th style={{ width: 20 + "%" }}>対象</th>
                                                <th style={{ width: 30 + "%" }}>回答内容</th>
                                                <th style={{ width: 10 + "%" }}>通知</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                        {answerHistorys && Object.keys(answerHistorys).map(index => (
                                            <tr>
                                                <td>{answerHistorys[index].updateDatetime}</td>
                                                <td>
                                                    {
                                                        answerHistorys[index].answererUser.departmentName + 
                                                        "　" + 
                                                        answerHistorys[index].answererUser.userName
                                                    }
                                                
                                                </td>
                                                <td>{answerHistorys[index].judgementResult}</td>
                                                <td>{answerHistorys[index].answerContent}</td>
                                                <td>
                                                    {answerHistorys[index].notifiedFlag &&(
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
                                            </tr>
                                        ))}
                                        </tbody>
                                    </table>
                                </div>
                            </div>
                        </div>
                        <Spacing bottom={3} />
                        <div>
                            <nav className={CustomStyle.custom_nuv}>回答ファイル更新履歴</nav>
                            <Spacing bottom={1} />
                            <div style={{ maxHeight: "400px", overflowY: "auto"}}>
                                <table className={CustomStyle.selection_table}>
                                    <thead>
                                        <tr className={CustomStyle.table_header}>
                                            <th style={{ width: 20 + "%" }}>更新日時</th>
                                            <th style={{ width: 20 + "%" }}>更新者</th>
                                            <th style={{ width: 10 + "%" }}>更新タイプ</th>
                                            <th style={{ width: 20 + "%" }}>対象</th>
                                            <th style={{ width: 20 + "%" }}>ファイル名</th>
                                            <th style={{ width: 10 + "%" }}>通知</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                    {answerFileHistory && Object.keys(answerFileHistory).map(index => (
                                        <tr>
                                            <td>{answerFileHistory[index].updateDatetime}</td>
                                            <td>{answerFileHistory[index].departmentName + " " + answerFileHistory[index].updateUserName}</td>
                                            <td>{answerFileHistory[index].updateType}</td>
                                            <td>{answerFileHistory[index].judgementResult}</td>
                                            <td>{answerFileHistory[index].fileName}</td>
                                            <td>{answerFileHistory[index].notifiedFlag &&(
                                                <div className={Styles.ellipse}>
                                                    <StyledIcon 
                                                    glyph={Icon.GLYPHS.checked}
                                                    styledWidth={"20px"}
                                                    styledHeight={"20px"}
                                                    light
                                                    />
                                                </div>
                                            )}</td>
                                        </tr>
                                    ))}
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                    <Spacing bottom={3} />

                    { this.state.viewState.fileDownloadModalShow && (
                        <FileDownLoadModal terria={this.props.terria} viewState={this.props.viewState} t={t} />
                    )}

                </Box>
            </Box >
            </>
        );
    }
}
export default withTranslation()(withTheme(ApplicationDetails));