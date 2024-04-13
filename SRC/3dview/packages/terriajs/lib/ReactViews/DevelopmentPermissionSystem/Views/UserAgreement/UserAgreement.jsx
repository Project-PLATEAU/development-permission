import { observer } from "mobx-react";
import PropTypes from "prop-types";
import React from "react";
import { withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import AnswerLogin from "../Answer/AnswerLogin.jsx"
import Spacing from "../../../../Styled/Spacing";
import Box from "../../../../Styled/Box";
import CustomStyle from "./scss/user-agreement.scss";
import Config from "../../../../../customconfig.json";

/**
 * 利用者規約・アンケート画面
 */
@observer
class UserAgreement extends React.Component {
    static displayName = "UserAgreement";
    static propTypes = {
        terria: PropTypes.object.isRequired,
        viewState: PropTypes.object.isRequired,
        theme: PropTypes.object,
        t: PropTypes.func.isRequired
    };
    constructor(props) {
        super(props);
        this.state = {
            viewState: props.viewState,
            terria: props.terria,
            // 利用者規約
            userAgreement: {},
            // 利用目的のラベルリスト
            questionaryPurposes: [],
            // 利用目的を選択されたか
            displayFlag: false
        };
    }

    /**
     * 初期処理
     */
    componentDidMount() {
        const { t } = this.props;
        let userAgreement = this.state.userAgreement;
        //サーバから利用者規約のlabelを取得
        fetch(Config.config.apiUrl + "/label/1000")
        .then(res => res.json())
        .then(res => {
            if(res.status === 401){
                alert("認証情報が無効です。ページの再読み込みを行います。");
                window.location.reload();
                return null;
            }
            if (Object.keys(res).length > 0) {
                userAgreement = { title:  res[0]?.labels?.title, content:  res[0]?.labels?.content, consentButtonText:  res[0]?.labels?.contentButtonText, systemName:  res[0]?.labels?.systemName,purposeContent:  res[0]?.labels?.purposeContent, questionaryContent:  res[0]?.labels?.questionaryContent };
                this.setState({ userAgreement: userAgreement });
                // 利用目的リスト取得
                this.getQuestionaryPurposes();
            }else{
                alert("labelの取得に失敗しました。");
            }
        }).catch(error => {
            console.error('通信処理に失敗しました', error);
            alert('通信処理に失敗しました');
        });
    }

    /**
     *　背景のDIVを非表示にする
     */
    closeBackgroundDiv(){
        if (document.getElementById("customBackgroundDiv")) {
            document.getElementById("customBackgroundDiv").style.display = "none";
        }
    }

    /**
     * アンケートの利用目的を選択する
     * @param {*} event 
     */
    changeQuestionaryType(event){
        let value = event.target.value;
        let list = this.state.questionaryPurposes;
        let displayFlag = false;
        Object.keys(list).map(key => {

            if(list[key].value == value){
                list[key].checked = true;
                displayFlag = true;
            }else{
                list[key].checked = false;
            }

        })

        // 利用目的選択状態保存
        this.setState({questionaryPurposes:list, displayFlag:displayFlag});
    }

    /**
     *　アンケートの利用目的の選択肢を取得する
     */
    getQuestionaryPurposes(){
        fetch(Config.config.apiUrl + "/questionnaire/search")
        .then(res => res.json())
        .then(res => {
            if(res.status === 401){
                alert("認証情報が無効です。ページの再読み込みを行います。");
                window.location.reload();
                return null;
            }

            if (Object.keys(res).length > 0 && !res.status) {
               
                Object.keys(res).map(key => {
                    res[key].checked = false;
                })
                this.setState({ questionaryPurposes: res, displayFlag: false });
            } else {
                alert("アンケートの利用目的一覧取得に失敗しました");
            }
        }).catch(error => {
            console.error('通信処理に失敗しました', error);
            alert('通信処理に失敗しました');
        });
    }

    /**
     * アンケートをクリックする
     */
    doQuestion(){
        // window.open();
        let purposeList = this.state.questionaryPurposes;
        let purpose = purposeList.filter((value) => value["checked"]);

        fetch(Config.config.apiUrl + "/questionnaire/reply", {
            method: 'POST',
            body: JSON.stringify({
                value:purpose[0].value,
                text:purpose[0].text,
                checked:purpose[0].checked
            }),
            headers: new Headers({ 'Content-type': 'application/json' }),
        })
        .then(res => res.json())
        .then(res => {
            if (res.status == 200) {

                // 初期画面を開く
                this.closeBackgroundDiv();
                this.props.viewState.hideUserAgreementView();

                if(Config.QuestionaryActived.UserAgreementView == "true"){
                    // アンケート画面のURL
                    let url = Config.config.questionnaireUrlForBusiness;
                    // ターゲット名
                    let target="develop_quessionaire"
                    // アンケート画面を開く
                    window.open(url,target);
                }
            }else if(res.status === 401){
                alert("認証情報が無効です。ページの再読み込みを行います。");
                window.location.reload();
            }else{
                alert("アンケートの利用目的のログ出力処理に失敗しました");
            }
        }).catch(error => {
            console.error('通信処理に失敗しました', error);
            alert('通信処理に失敗しました');
        });
    }


    render() {
        const { t } = this.props;
        const title = this.state.userAgreement.title;
        const content = this.state.userAgreement.content;
        const consentButtonText = this.state.userAgreement.consentButtonText;
        const systemName = this.state.userAgreement.systemName;
        const purposeContent = this.state.userAgreement.purposeContent;
        const questionaryContent = this.state.userAgreement.questionaryContent;
        let textContent = purposeContent;
        if(Config.QuestionaryActived.UserAgreementView == "true"){
            textContent = textContent + '<BR>' + questionaryContent;
        }
        const questionaryPurposeList = this.state.questionaryPurposes;
        let displayFlag = this.state.displayFlag;

        return (
            <>
                <div className={CustomStyle.customBackground} id="customBackgroundDiv"></div>
                <Box
                    displayInlineBlock
                    backgroundColor={this.props.theme.textLight}
                    overflow={"hidden"}
                    css={`
                        position: fixed;
                        z-index: 999999;
                        height: 80%;
                        width: 40%;
                    `}
                    className={CustomStyle.custom_frame}
                >
                    
                    <div className={CustomStyle.custom_nuv}>
                        <div className={CustomStyle.custom_header_title}>
                            <h3 className={CustomStyle.title_text} dangerouslySetInnerHTML={{ __html: systemName }}></h3>
                        </div>
                        <div>
                            <button
                                className={CustomStyle.back_button}
                                onClick={e => {
                                    if( window.history.length >= 2 ) {
                                        this.closeBackgroundDiv();
                                        window.history.back();
                                        return false;
                                    }
                                    else {
                                        this.closeBackgroundDiv();
                                        window.close();
                                    }
                                }}
                            >
                                <span>トップに戻る</span>
                            </button>
                        </div>
                    </div>

                    <div className={CustomStyle.customScroll}>
                        <Box
                            centered
                            paddedHorizontally={6}
                            displayInlineBlock
                        >
                            {/* <p className={CustomStyle.fontSize08} dangerouslySetInnerHTML={{ __html: textContent }}></p> */}
                            <p dangerouslySetInnerHTML={{ __html: textContent }}></p>
                        </Box>
                        <div className={CustomStyle.div_area}>
                            <div className={CustomStyle.radio_button_area}>
                                {Object.keys(questionaryPurposeList).map(key => (
                                    <div >
                                        <label className={CustomStyle.radio_label}>
                                            <input
                                                className={CustomStyle.radio_input}
                                                type="radio"
                                                value={questionaryPurposeList[key].value}
                                                onChange={e => this.changeQuestionaryType(e)}
                                                checked={questionaryPurposeList[key].checked}
                                            />
                                            <span className={CustomStyle.custom_radio} />
                                            {"：" + questionaryPurposeList[key].text}
                                        </label>
                                    </div>
                                ))}
                            </div>
                        </div>
                        {/* <div className={CustomStyle.button_position}>
                            <button
                                className={CustomStyle.next_button}
                                onClick={e => { this.doQuestion() }}
                            >
                                <span>アンケート</span>
                            </button>
                        </div> */}
                        <Spacing bottom={2} />
                        {/* <div style={{display: displayFlag ? "block":"none"}}> */}
                        <div className={CustomStyle.custom_sub_title}>
                            <p className={CustomStyle.title_text} dangerouslySetInnerHTML={{ __html: title }}></p>
                        </div>
                        <Box
                            centered
                            paddedHorizontally={3}
                            displayInlineBlock
                            className={CustomStyle.scrollUserAgreement}
                        >
                            <Spacing bottom={1} />
                            <div dangerouslySetInnerHTML={{ __html: content }}></div>
                            
                        </Box>
                   
                        <div className={CustomStyle.button_position} >
                            <button
                                className={CustomStyle.next_button}
                                disabled={displayFlag? false : true}
                                onClick={e => {
                                    this.doQuestion()
                                }}
                            >
                                <span dangerouslySetInnerHTML={{ __html: consentButtonText }}></span>
                            </button>
                        </div>
                        <div className={CustomStyle.custom_sub_title}>
                            <p className={CustomStyle.title_text}>回答確認</p>
                        </div>

                        <Box fullWidth centered paddedHorizontally={6} displayInlineBlock >
                            <AnswerLogin  terria={this.props.terria} viewState={this.props.viewState} t={this.props.t} disabledFlag={!displayFlag} questionaryPurposes={questionaryPurposeList}/>
                        </Box>
                        {/* </div> */}
                    </div>
                </Box >
            </>
        );
    }
}
export default withTranslation()(withTheme(UserAgreement));