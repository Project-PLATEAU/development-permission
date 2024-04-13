import { observer } from "mobx-react";
import PropTypes from "prop-types";
import React from "react";
import { withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import Text from "../../../../Styled/Text";
import Input from "../../../../Styled/Input";
import Box from "../../../../Styled/Box";
import CustomStyle from "./scss/answer-login.scss";
import Config from "../../../../../customconfig.json";
import Icon, { StyledIcon } from "../../../../Styled/Icon";
import Spacing from "../../../../Styled/Spacing";

/**
 * 申請ID/パスワード認証画面
 */
@observer
class AnswerLogin extends React.Component {
    static displayName = "AnswerLogin";
    static propTypes = {
        terria: PropTypes.object.isRequired,
        viewState: PropTypes.object.isRequired,
        theme: PropTypes.object,
        t: PropTypes.func.isRequired,
        disabledFlag: PropTypes.bool,
        questionaryPurposes: PropTypes.array
    }

    constructor(props) {
        super(props);
        this.state = {
            viewState: props.viewState,
            terria: props.terria,
            //ID
            id: "",
            //パスワード
            password: "",
            //入力エラー
            errorItems: {},
            // パスワード見えるフラグ
            pushHide:true
        };
    }

    /**
     * 申請ID/パスワード認証を行う
     */
    confirm(){
        const id = this.state.id;
        const password = this.state.password;
        let errorItems = this.state.errorItems;
        // 必須項目のチェック
        if(!id){
            errorItems["id"] = "必須項目です";
        }else{
            delete errorItems["id"];
        }
        if(!password){
            errorItems["password"] = "必須項目です";
        }else{
            delete errorItems["password"];
        }
        // エラーがある場合は処理を中断　エラーが無ければAPIへログイン情報を送信しログイン可否を決定する
        if (Object.keys(errorItems).length > 0) {
            this.setState({ errorItems: errorItems });
        } else {
            fetch(Config.config.apiUrl + "/answer/confirm/answer", {
                method: 'POST',
                body: JSON.stringify({
                    loginId:id,
                    password:password,
                    outputLogFlag:true
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
                    res.loginId = id;
                    res.password = password;
                    this.state.viewState.moveToConfirmAnswerInformationView(JSON.parse(JSON.stringify(res)));
                    if(this.props.viewState.showUserAgreement){
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
                   }
                }else{
                    alert('ログイン処理に失敗しました');
                }
            }).catch(error => {
                console.error('通信処理に失敗しました', error);
                alert('通信処理に失敗しました');
            });
        }
    }

    /**
     * 読み込み中のdivを非表示とする
     */
    closeBackgroundDiv(){
        document.getElementById("customBackgroundDiv").style.display = "none";
    }

    /**
     * 選択した利用目的をアクセスログに記録する
     */
    doQuestion(){
        let purposeList = this.props.questionaryPurposes;
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
            if(res.status === 401){
                alert("認証情報が無効です。ページの再読み込みを行います。");
                window.location.reload();
                return null;
            }
            if (res.status === 200) {
                // ログインを行う
                this.confirm();
            }else{
                alert("アンケートの利用目的のログ出力処理に失敗しました");
            }
        }).catch(error => {
            console.error('通信処理に失敗しました', error);
            alert('通信処理に失敗しました');
        });
    }

    render() {
        const explanation = "ID/パスワードを入力してください";
        const id = this.state.id;
        const password = this.state.password;
        const errorItems = this.state.errorItems;
        const t = this.props.t;
        let isUserAgreementView = this.props.viewState.showUserAgreement;
        let pushHide = this.state.pushHide;
        const disabledFlag = this.props.disabledFlag;
        return (
            <>
                <Box id="AnswerLogin" style={{display: isUserAgreementView ? "none" : "block" }} >

                    <Box padded  className={CustomStyle.text_area}>
                        <Text textDark uppercase overflowHide overflowEllipsis>
                            {/* 案内文言：申請内容に対する回答確認・再申請はID・パスワードを入力してください。 */}
                            {t("infoMessage.tipsForInputAnswerLoginInfo")}    
                        </Text>
                    </Box>

                    <div className={CustomStyle.area_background} >
                        <Box paddedRatio={2} css={`display:block`}>
                            <div className={CustomStyle.box}>
                                <div className={CustomStyle.input_text}>
                                    {errorItems["id"] && (
                                        <p className={CustomStyle.error}>{errorItems["id"]}</p>
                                    )}
                                    <Input
                                        type="text"
                                        white={true}
                                        value={id}
                                        placeholder="ID"
                                        id="id"
                                        fieldBorder={"#d3d3d3"}
                                        border
                                        onChange={e => this.setState({ id: e.target.value })}
                                    />
                                </div>
                            </div>
                            <div className={CustomStyle.box}>
                                <div className={CustomStyle.input_text}>
                                    {errorItems["password"] && (
                                        <p className={CustomStyle.error}>{errorItems["password"]}</p>
                                    )}
                                    <Input
                                        dark={false}
                                        type={pushHide ? "password" : "text" }
                                        white={true}
                                        value={password}
                                        placeholder="Pass"
                                        id="password"
                                        fieldBorder={"#d3d3d3"}
                                        border
                                        onChange={e => this.setState({ password: e.target.value })}
                                    />
                                    <div className={CustomStyle.eye_icon_position} onClick={e => this.setState({ pushHide: !pushHide})}>
                                        <StyledIcon
                                            styledWidth={"20px"}
                                            fillColor={this.props.theme.textLight}
                                            opacity={"0.5"}
                                            glyph={ pushHide ? Icon.GLYPHS.eye2 : Icon.GLYPHS.eye2Closed }
                                            
                                        />
                                    </div>
                                </div>
                            </div>
                        </Box>
                        <Spacing bottom={4}/>
                        <Box padded>
                            <button className={`${CustomStyle.btn_baise_style} ${CustomStyle.btn_answer}`}    
                                onClick={evt => {
                                    this.confirm();
                                }}>
                                <span>回答内容を確認する</span>
                            </button>
                        </Box>
                    </div>
                </Box >
                <Box id="AnswerLogin" fullWidth style={{display: isUserAgreementView ? "block" : "none" }} >
                    <div style={{height:"150px"}}>
                        <p className={CustomStyle.explanation}>{explanation}</p>
                        <div className={CustomStyle.inputBox}>
                            <div className={CustomStyle.caption}>
                                <Text>ID</Text>
                            </div>
                            <div className={CustomStyle.input_text}>
                                {errorItems["id"] && (
                                    <p className={CustomStyle.error}>{errorItems["id"]}</p>
                                )}
                                <Input
                                    white={true}
                                    type="text"
                                    value={id}
                                    placeholder=""
                                    fieldBorder={"#D3d3d3"}
                                    border
                                    id="id"
                                    onChange={e => this.setState({ id: e.target.value })}
                                />
                            </div>
                        </div>
                        <div className={CustomStyle.inputBox}>
                            <div className={CustomStyle.caption}>
                                <Text>パスワード</Text>
                            </div>
                            <div className={CustomStyle.input_text}>
                                {errorItems["password"] && (
                                    <p className={CustomStyle.error}>{errorItems["password"]}</p>
                                )}
                                <Input
                                    white={true}
                                    type={pushHide ? "password" : "text" }
                                    value={password}
                                    placeholder=""
                                    id="password"
                                    fieldBorder={"#D3d3d3"}
                                    border
                                    onChange={e => this.setState({ password: e.target.value })}
                                />
                                <div className={CustomStyle.eye_icon_position} onClick={e => this.setState({ pushHide: !pushHide})}>
                                    <StyledIcon
                                        styledWidth={"20px"}
                                        fillColor={this.props.theme.textLight}
                                        opacity={"0.5"}
                                        glyph={ pushHide ? Icon.GLYPHS.eye2 : Icon.GLYPHS.eye2Closed }
                                        
                                    />
                                </div>
                            </div>
                        </div>
                    </div>
                    {/* <Box paddedRatio={3} /> */}
                    <div className={CustomStyle.button_position}>
                        <button
                            className={CustomStyle.next_button}
                            disabled = { disabledFlag }
                            onClick={e => {
                                this.doQuestion();
                            }}
                        >
                            <span dangerouslySetInnerHTML={{ __html: "回答内容を確認する" }}></span>
                        </button>
                    </div>
                </Box>
            </>
        );
    }
}

export default withTranslation()(withTheme(AnswerLogin));