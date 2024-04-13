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
            errorItems: {},
            // 入力エリアの高さ
            height: 0
        };
    }

    /**
     * 初期処理
     */
    componentDidMount() {
        //保持された入力情報がある場合はAPIリクエストを行わない
        if (this.state.applicantInformation.length < 1) {
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
        }
        this.getWindowSize();
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
     * リサイズ
     */
    componentWillMount () {
        window.addEventListener('resize', () => {
                this.getWindowSize() 
        })
    }

    /**
     * 入力値をセット
     * @param {number} 対象のindex
     * @param {string} 入力された値
     */
    inputChange(index, value) {
        let applicantInformation = this.state.applicantInformation;
        applicantInformation[index].value = value;
        this.setState({ applicantInformation: applicantInformation });
    }

    /**
     * 申請対象ファイルアップロード画面へ遷移
     */
    next() {
        const applicantInformation = this.state.applicantInformation;
        let errorItems = this.state.errorItems;
        let reg = "";
        // 必須入力チェック & 個別の形式チェック
        Object.keys(applicantInformation).map(key => {
            if (applicantInformation[key].requireFlag) {
                if (!applicantInformation[key].value) {
                    errorItems[applicantInformation[key].id] = "必須項目です";
                } else {
                    delete errorItems[applicantInformation[key].id];
                    if (applicantInformation[key].regularExpressions) {
                        reg = new RegExp(applicantInformation[key].regularExpressions);
                        if (!reg.exec(applicantInformation[key].value)) {
                            errorItems[applicantInformation[key].id] = applicantInformation[key].name + "の形式を正しく入力してください";
                        } else {
                            delete errorItems[applicantInformation[key].id];
                        }
                    }
                }
            }
        })
        if (Object.keys(errorItems).length > 0) {
            this.setState({ errorItems: errorItems });
        } else {
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
        const title = "1. 申請者情報";
        const explanation = "下記に入力してください";
        const applicantInformation = this.state.applicantInformation;
        const errorItems = this.state.errorItems;
        const height = this.state.height;

        return (
            <>
                <div className={CustomStyle.div_area}>
                    <Box id="EnterApplicantInformation" css={`display:block`} >
                        <nav className={CustomStyle.custom_nuv} id="EnterApplicantInformationDrag">
                            申請フォーム
                        </nav>
                        <Box
                            centered
                            paddedHorizontally={5}
                            displayInlineBlock
                            className={CustomStyle.custom_content}
                        >
                            <h2 className={CustomStyle.title}>{title}</h2>
                            <p className={CustomStyle.explanation}>{explanation}</p>
                            <Spacing bottom={1} />
                            {/* style={{height:height + "px"}} */}
                            <div className={CustomStyle.scrollContainer} id="ApplicantInfoInputArea" > 
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
                                        {errorItems[applicantInformation[key].id] && (
                                            <p className={CustomStyle.error}>{errorItems[applicantInformation[key].id]}</p>
                                        )}
                                        <Input
                                            light={true}
                                            dark={false}
                                            type="text"
                                            value={applicantInformation[key].value}
                                            placeholder={applicantInformation[key].name + "を入力してください"}
                                            id={applicantInformation[key].id}
                                            onChange={e => this.inputChange(key, e.target.value)}
                                        />
                                    </div>
                                </div>
                            ))}
                            </div>
                            
                        </Box>
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