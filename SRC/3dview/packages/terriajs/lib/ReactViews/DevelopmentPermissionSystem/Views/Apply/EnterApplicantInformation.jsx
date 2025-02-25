import { observer } from "mobx-react";
import PropTypes from "prop-types";
import React from "react";
import { withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import Spacing from "../../../../Styled/Spacing";
import Text from "../../../../Styled/Text";
import Input from "../../../../Styled/Input";
import Box from "../../../../Styled/Box";
import CustomStyle from "./scss/enter-applicant-Information.scss";
import Config from "../../../../../customconfig.json";

/**
 * 申請者情報入力画面
 */
@observer
class EnterApplicantInformation extends React.Component {
    static displayName = "EnterApplicantInformation";

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
            //申請者情報一覧
            applicantInformation: props.viewState.applicantInformation,
            //申請者情報入力エラー一覧
            errorItems1: {},
            //連絡先情報入力エラー一覧
            errorItems2: {},
            //入力エリアの高さ
            height: 0,
            //申請者同一フラグ（デフォルトはtrue） 
            applicantSameFlag:true,
            // 申請者情報入力の案内文言
            explanation: {}
        };
    }

    /**
     * 初期処理
     */
    componentDidMount() {
        //保持された入力情報がある場合はAPIリクエストを行わない
        const applicantInformation = this.state.applicantInformation;
        if (applicantInformation.length < 1) {
            // APIへのリクエスト
            fetch(Config.config.apiUrl + "/application/applicantItems/")
            .then(res => res.json())
            .then(res => {
                if(res.status === 401){
                    alert("認証情報が無効です。ページの再読み込みを行います。");
                    window.location.reload();
                    return null;
                }
                if (Object.keys(res).length > 0 && !res.status) {
                    this.setState({applicantInformation:res});
                }else{
                    alert('申請者情報入力項目一覧取得に失敗しました');
                }
            }).catch(error => {
                console.error('通信処理に失敗しました', error);
                alert('通信処理に失敗しました');
            });
        }else{
            // 保持された入力情報の申請者同一フラグで再現
            let applicantSameFlag = this.state.applicantSameFlag;
            Object.keys(applicantInformation).map(key => {
                if(applicantInformation[key].contactAddressFlag){
                    applicantSameFlag = applicantInformation[key].applicantSameFlag ? true : false;
                }
            })
            // 新規申請以外の場合は、全てが申請者情報と同一の場合は申請者と同じと判断する
            if(this.props.viewState.isReApply){
                applicantSameFlag = true;
                Object.keys(applicantInformation).map(key => {
                    if(applicantInformation[key].contactAddressFlag){
                        if(applicantInformation[key].value != applicantInformation[key].contactValue){
                            applicantSameFlag = false;
                        }
                    }
                })
            }
            this.setState({applicantSameFlag:applicantSameFlag});
        }
        this.getWindowSize();
        //案内文言取得
        this.getExplanationLabel();
    }

    /**
     * 高さ再計算
     */
    getWindowSize() {
        if(this.props.viewState.showEnterApplicantInformation){
            let win = window;
            let e = window.document.documentElement;
            let g = window.document.documentElement.getElementsByTagName('body')[0];
            let h = win.innerHeight|| e.clientHeight|| g.clientHeight;

            const getRect = document.getElementById("ApplicantInfoInputArea");
            let height = h - getRect.getBoundingClientRect().top - 100;
            this.setState({height: height});
        }
    }
    
    /**
     * DBから申請者情報入力の案内文言を取得
     */
    getExplanationLabel(){

        let explanation = this.state.explanation;

        //サーバからlabelを取得
        fetch(Config.config.apiUrl + "/label/1007/1")
        .then(res => res.json())
        .then(res => {
            if(res.status === 401){
                alert("認証情報が無効です。ページの再読み込みを行います。");
                window.location.reload();
                return null;
            }
            if (Object.keys(res).length > 0) {
                let label2 = res[0]?.labels?.content1;
                let label4 = res[0]?.labels?.content2;
                explanation = { content1: label2, content2: label4};
                this.setState({ explanation: explanation });
            }else{
                alert("labelの取得に失敗しました。");
            }
        }).catch(error => {
            console.error('通信処理に失敗しました', error);
            alert('通信処理に失敗しました');
        });
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
     * 申請者の入力値をセット
     * @param {number} 対象のindex
     * @param {string} 入力された値
     * @param {number} 最大文字数
     */
    inputChange1(index, value, maxLength) {
        // 文字数チェック
        if(value.length > maxLength ){
            alert(maxLength+"文字以内で入力してください。");
            return;
        }

        let applicantInformation = this.state.applicantInformation;
        applicantInformation[index].value = value;
        this.setState({ applicantInformation: applicantInformation });
    }

    /**
     * 連絡先の入力値をセット
     * @param {number} 対象のindex
     * @param {string} 入力された値
     * @param {number} 最大文字数
     */
    inputChange2(index, value, maxLength) {
        // 文字数チェック
        if(value.length > maxLength ){
            alert(maxLength+"文字以内で入力してください。");
            return;
        }
        let applicantInformation = this.state.applicantInformation;
        if(applicantInformation[index].contactAddressFlag){
            applicantInformation[index].contactValue = value;
            this.setState({ applicantInformation: applicantInformation });
        }
    }

    /**
     * 申請対象ファイルアップロード画面へ遷移
     */
    next() {
        const applicantInformation = this.state.applicantInformation;
        const applicantSameFlag = this.state.applicantSameFlag;
        let errorItems1 = this.state.errorItems1;
        let errorItems2 = this.state.errorItems2;
        let reg = "";
        // 申請者 必須入力チェック & 個別の形式チェック
        Object.keys(applicantInformation).map(key => {
            if (applicantInformation[key].requireFlag) {
                if (!applicantInformation[key].value) {
                    errorItems1[applicantInformation[key].id] = "必須項目です";
                } else {
                    delete errorItems1[applicantInformation[key].id];
                    if (applicantInformation[key].regularExpressions) {
                        reg = new RegExp(applicantInformation[key].regularExpressions);
                        if (!reg.exec(applicantInformation[key].value)) {
                            errorItems1[applicantInformation[key].id] = applicantInformation[key].name + "の形式を正しく入力してください";
                        } else {
                            delete errorItems1[applicantInformation[key].id];
                        }
                    }
                }
            }
        })
        // 連絡先 必須入力チェック & 個別の形式チェック
        Object.keys(applicantInformation).map(key => {
            if (!applicantSameFlag && applicantInformation[key].contactAddressFlag && applicantInformation[key].requireFlag) {
                if (!applicantInformation[key].contactValue) {
                    errorItems2[applicantInformation[key].id] = "必須項目です";
                } else {
                    delete errorItems2[applicantInformation[key].id];
                    if (applicantInformation[key].regularExpressions) {
                        reg = new RegExp(applicantInformation[key].regularExpressions);
                        if (!reg.exec(applicantInformation[key].contactValue)) {
                            errorItems2[applicantInformation[key].id] = applicantInformation[key].name + "の形式を正しく入力してください";
                        } else {
                            delete errorItems2[applicantInformation[key].id];
                        }
                    }
                }
            }else{
                //申請者と同一の場合入力チェックはスキップ
                delete errorItems2[applicantInformation[key].id];
            }
        })
        if (Object.keys(errorItems1).length > 0) {
            this.setState({ errorItems1: errorItems1 });
        } else if (Object.keys(errorItems2).length > 0) {
            this.setState({ errorItems2: errorItems2 });
        } else {
            // 最終の申請者同一フラグをセット
            Object.keys(applicantInformation).map(key => {
                if(applicantInformation[key].contactAddressFlag){
                    applicantInformation[key].applicantSameFlag = this.state.applicantSameFlag;
                }
            })
            // ファイルアップロードへ遷移
            this.props.viewState.moveToUploadApplicationInformationView(Object.assign({}, this.state.applicantInformation));
        }
    }

    /**
     * 概況診断結果表示画面に戻る
     */
    back() {
        this.props.viewState.backToGeneralConditionDiagnosisView();
    }

    render() {
        const title1 = "1. 申請者情報";
        const explanation1 = this.state.explanation.content1;
        const title2 = "2. 連絡先";
        const explanation2 = this.state.explanation.content2;
        
        const applicantInformation = this.state.applicantInformation;
        const errorItems1 = this.state.errorItems1;
        const errorItems2 = this.state.errorItems2;
        const height = this.state.height;
        const applicantSameFlag = this.state.applicantSameFlag;
        const isReApply = this.props.viewState.isReApply;
        const maxLengthText =  Config.inputMaxLength.applicantInfo.text;
        const maxLengthTextarea =  Config.inputMaxLength.applicantInfo.textarea;
        const inputBuffLength = 1;
        return (
            <>
                <div className={CustomStyle.div_area}>
                    <Box id="EnterApplicantInformation" css={`display:block`} >
                        <nav className={CustomStyle.custom_nuv} id="EnterApplicantInformationDrag">
                            申請フォーム
                        </nav>
                        <div className={CustomStyle.scrollContainer} id="ApplicantInfoInputArea" style={{height:height + "px"}}>
                        <Box
                            centered
                            paddedHorizontally={5}
                            displayInlineBlock
                            className={CustomStyle.custom_content}
                        >
                            <h2 className={CustomStyle.title}>{title1}</h2>
                            <p className={CustomStyle.explanation}>{explanation1}</p>
                            <Spacing bottom={1} />
                            {Object.keys(applicantInformation).map(key => (
                                <div className={CustomStyle.box}>
                                    <div className={CustomStyle.caption}>
                                        <Text>
                                            {!applicantInformation[key].requireFlag && (
                                                "("
                                            )}
                                            {applicantInformation[key].name}
                                            {!applicantInformation[key].requireFlag && (
                                                ")"
                                            )}</Text>
                                    </div>
                                    <div className={CustomStyle.input_text}>
                                        {errorItems1[applicantInformation[key].id] && (
                                            <p className={CustomStyle.error}>{errorItems1[applicantInformation[key].id]}</p>
                                        )}
                                        {applicantInformation[key].itemType == "0" ? 
                                        <Input
                                            light={true}
                                            dark={false}
                                            type="text"
                                            maxLength={maxLengthText + inputBuffLength}
                                            value={applicantInformation[key].value}
                                            placeholder={applicantInformation[key].name + "を入力してください"}
                                            id={applicantInformation[key].id}
                                            onChange={e => this.inputChange1(key, e.target.value, maxLengthText)}
                                            
                                        /> : 
                                        <textarea
                                            rows={3}
                                            maxLength={maxLengthTextarea + inputBuffLength}
                                            value={applicantInformation[key].value}
                                            placeholder={applicantInformation[key].name + "を入力してください"}
                                            id={applicantInformation[key].id}
                                            onChange={e => this.inputChange1(key, e.target.value, maxLengthTextarea)}
                                        />}
                                    </div>
                                </div>
                            ))}
                        </Box>
                        {!isReApply ? (
                            <Box
                                centered
                                paddedHorizontally={5}
                                displayInlineBlock
                                className={CustomStyle.custom_content}
                            >
                                <h2 className={CustomStyle.title}>{title2}</h2>
                                <p className={CustomStyle.explanation}>{explanation2}</p>
                                <Spacing bottom={1} />
                                <input type="checkbox" className={CustomStyle.custom_checkbox_auto}
                                    onChange={ e => { 
                                        this.setState({applicantSameFlag:!applicantSameFlag,applicantInformation:applicantInformation});
                                    } }
                                    checked={applicantSameFlag}
                                /> {"申請者と同じ"} 
                                {!applicantSameFlag && Object.keys(applicantInformation).map(key => (
                                    applicantInformation[key].contactAddressFlag ? (
                                        <div className={CustomStyle.box}>
                                                <div className={CustomStyle.caption}>
                                                    <Text>
                                                        {!applicantInformation[key].requireFlag && (
                                                            "("
                                                        )}
                                                        {applicantInformation[key].name}
                                                        {!applicantInformation[key].requireFlag && (
                                                            ")"
                                                        )}</Text>
                                                </div>
                                                <div className={CustomStyle.input_text}>
                                                    {errorItems2[applicantInformation[key].id] && (
                                                        <p className={CustomStyle.error}>{errorItems2[applicantInformation[key].id]}</p>
                                                    )}
                                                    {applicantInformation[key].itemType == "0" ? 
                                                    <Input
                                                        light={true}
                                                        dark={false}
                                                        type="text"
                                                        maxLength={maxLengthText + inputBuffLength}
                                                        value={applicantInformation[key].contactValue}
                                                        placeholder={applicantInformation[key].name + "を入力してください"}
                                                        id={applicantInformation[key].id}
                                                        onChange={e => this.inputChange2(key, e.target.value, maxLengthText)}
                                                    /> : 
                                                    <textarea
                                                        rows={3}
                                                        maxLength={maxLengthTextarea + inputBuffLength}
                                                        value={applicantInformation[key].contactValue}
                                                        placeholder={applicantInformation[key].name + "を入力してください"}
                                                        id={applicantInformation[key].id}
                                                        onChange={e => this.inputChange2(key, e.target.value, maxLengthTextarea)}
                                                    />}
                                                </div>
                                            </div>
                                    ):(null)
                                ))}
                            </Box>
                        ):null}
                        </div>
                    </Box >
                </div>
                <div className={CustomStyle.div_area} >
                    {Object.keys(applicantInformation).length > 0 && (
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
export default withTranslation()(withTheme(EnterApplicantInformation));