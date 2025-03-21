import { observer } from "mobx-react";
import PropTypes from "prop-types";
import React from "react";
import { withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import Spacing from "../../../../Styled/Spacing";
import Text from "../../../../Styled/Text";
import Input from "../../../../Styled/Input";
import Box from "../../../../Styled/Box";
import Select from "../../../../Styled/Select";
import CustomStyle from "./scss/enter-add-Information.scss";
import MultiSelect from "react-select";
import { GLYPHS, StyledIcon } from "../../../../Styled/Icon";
import Config from "../../../../../customconfig.json";

/**
 * 複数プルダウンの入力ボックスのCSS
 * @param {*} props 
 * @returns 
 */
const DropdownIndicator = props => {
    return(
        <div className={`${CustomStyle.select_box_icon}`}>
            <StyledIcon
                // light bg needs dark icon
                fillColor={"#000"}
                styledWidth="16px"
                glyph={GLYPHS.arrowDown}
            />
        </div>
    )
}

/**
 * R6: SC115 再申請画面
 * ※申請者情報入力を行う
 */
@observer
class EnterAddInformation extends React.Component {
    static displayName = "EnterAddInformation";

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
            //申請追加情報一覧
            applicantAddInformations: props.viewState.applicantnAddInformation,
            //申請追加情報入力エラー一覧
            errorItems: {},
            // 入力エリアの高さ
            height: 0
        };
    }

    /**
     * 初期処理
     */
    componentDidMount() {
        // 保持された入力情報がない場合は再申請情報の申請追加情報をセット
        let applicantAddInformations = this.state.applicantAddInformations;
        if (Object.keys(applicantAddInformations).length < 1) {
            applicantAddInformations = this.props.viewState.reApplication.applicantAddInformations;
            this.setState({applicantAddInformations: applicantAddInformations});

            // 申請済みの申請地番
            const lotNumber = this.props.viewState.reApplication.lotNumbers;
            this.props.viewState.applicationPlace = Object.assign({},lotNumber);
        }

        this.getWindowSize();
    }

    /**
     * 高さ再計算
     */
    getWindowSize() {
        if(this.props.viewState.showEnterApplicantInformation && this.props.viewState.isReApply){
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
     * 入力値をセット（文字列）
     * @param {number} 対象のindex
     * @param {string} 入力された値
     * @param {number} 最大文字数
     */
    inputChange(index, value, maxLength) {
        // 文字数チェック
        if(value.length > maxLength ){
            alert(maxLength+"文字以内で入力してください。");
            return;
        }

        let applicantAddInformations = this.state.applicantAddInformations;
        applicantAddInformations[index].value = value;
        this.setState({ applicantAddInformations: applicantAddInformations });
    }

    /**
     * 入力値をセット（数値）
     * @param {number} 対象のindex
     * @param {string} 入力された値
     */
    inputChangeNumber(index, value) {
        let applicantAddInformations = this.state.applicantAddInformations;
        applicantAddInformations[index].value = value;
        this.setState({ applicantAddInformations: applicantAddInformations });
    }

     /**
     * 入力値をセット(日付)
     * @param {number} 対象のindex
     * @param {string} 入力された値
     */
    inputChangeDate(index, value){
        let applicantAddInformations = this.state.applicantAddInformations;
        if(value){
            let date = new Date(value);
            let formated = date.toLocaleDateString("ja-JP",{year: "numeric", month:"2-digit", day:"2-digit"}).split("/").join("-");
            applicantAddInformations[index].value = formated;
        }else{
            applicantAddInformations[index].value = "";
        }

        this.setState({ applicantAddInformations: applicantAddInformations });
    }

    /**
     * 選択肢変更（単一選択）
     * @param {number} 対象のindex
     * @param {number} 選択中選択肢のインデックス
     */
    selectedChange(index, key){
        let applicantAddInformations = this.state.applicantAddInformations;
        Object.keys(applicantAddInformations[index].itemOptions).map(i => {
            applicantAddInformations[index].itemOptions[i].checked = false;
        })
        if (key >= 0) {
            applicantAddInformations[index].itemOptions[key].checked = true;
        }
        this.setState({ applicantAddInformations: applicantAddInformations });
    }

    /**
     * 選択肢リストを、multiSelectに利用可能の形に整形する 
     * @param {*} itemOptions 複数選択の追加情報の選択肢
     * @returns 
     */
    getOptions(itemOptions){

        let options = [];
        Object.keys(itemOptions).map(index => {
            let option = {"value": index, "label": itemOptions[index].content};
            options.push(option);
        });

        return options;
    }

    /**
     * 選択肢リストを、multiSelectに利用可能の形に整形する 
     * @param {*} itemOptions 複数選択の追加情報の選択肢
     * @returns 
     */
    getDefaultValue(itemOptions){

        let options = [];
        Object.keys(itemOptions).map(index => {
            if(itemOptions[index].checked){
                let option = {"value": index, "label": itemOptions[index].content};
                options.push(option);
            }
        });

        return options;
    }

    /**
     * 選択肢変更（複数選択）
     * @param {number} 対象のindex
     * @param {number} 選択中選択肢
     */
    multiSelectedChange(index, selectedValue){

        let applicantAddInformations = this.state.applicantAddInformations;
        // 前回選択したの結果をクリアする
        Object.keys(applicantAddInformations[index].itemOptions).map(i => {
            applicantAddInformations[index].itemOptions[i].checked = false;
        })

        if(selectedValue){
            // 今回の選択結果を設定
            Object.keys(selectedValue).map(key => {
                let value = selectedValue[key].value;
                applicantAddInformations[index].itemOptions[Number(value)].checked = true;
            })
        }

        this.setState({ applicantAddInformations: applicantAddInformations });
    }

    /**
     * 申請対象ファイルアップロード画面へ遷移
     */
    next() {
        const applicantAddInformations = this.state.applicantAddInformations;
        let errorItems = this.state.errorItems;
        let reg = "";
        // 必須入力チェック & 個別の形式チェック
        Object.keys(applicantAddInformations).map(key => {
            if (applicantAddInformations[key].requireFlag) {
                if (this.isBlank(applicantAddInformations[key])) {
                    errorItems[applicantAddInformations[key].id] = "必須項目です";
                } else {
                    delete errorItems[applicantAddInformations[key].id];
                    if (applicantAddInformations[key].regularExpressions) {
                        // プルダウン項目以外の場合、正規表現チェックを行う
                        if(applicantAddInformations[key].itemType !== "4" && applicantAddInformations[key].itemType !== "5" ){
                            reg = new RegExp(applicantAddInformations[key].regularExpressions);
                            if (!reg.exec(applicantAddInformations[key].value)) {
                                errorItems[applicantAddInformations[key].id] = applicantAddInformations[key].name + "の形式を正しく入力してください";
                            } else {
                                delete errorItems[applicantAddInformations[key].id];
                            }
                        }
                    }
                }
            }
        })
        if (Object.keys(errorItems).length > 0) {
            this.setState({ errorItems: errorItems });
        } else {
            // ファイルアップロードへ遷移
            this.props.viewState.moveToUploadApplicationInformationView(Object.assign({}, this.state.applicantAddInformations));
        }
    }
    
    /**
     * 申請追加項目に入力したか判定
     * @param {*} applicantAddInformation 申請追加項目情報
     * @returns 
     */
    isBlank(applicantAddInformation){
        let text = null;
        let itemType = applicantAddInformation.itemType;
        switch (itemType) {
            case '0': // 0:1行のみの入力欄で表示
            case '1'://1:複数行の入力欄で表示
            case '2'://2:日付（カレンダー）
            case '3'://3:数値
                text = applicantAddInformation.value;
                break;
            case '4'://4:ドロップダウン単一選択
            case '5'://5:ドロップダウン複数選択
                text = applicantAddInformation.itemOptions.filter(option => {return option.checked == true}).map(function (value) { return value.itemId }).join(",");
                break;
            default:
                text = applicantAddInformation.value;
        }

        if(text == null || text == ""){
            return true;
        }else{
            return false;
        }
        
    }
    /**
     * 概況診断結果表示画面に戻る
     */
    back() {
        if(this.props.viewState.isReApply){
            
            if(this.props.viewState.reapplyApplicationStepId == 3){
                this.state.viewState.moveToConfirmAnswerInformationView(null);
            }else{
                this.props.viewState.backToGeneralConditionDiagnosisView();
            }
        }else{
            this.props.viewState.backToGeneralConditionDiagnosisView();
        }
    }
    

    render() {
        const title = "1. 申請追加情報";
        let explanation = "下記に入力してください";
        const applicantAddInformations = this.state.applicantAddInformations;
        if(applicantAddInformations && Object.keys(applicantAddInformations).length < 1){
            explanation = "申請追加情報項目が設定されていないため、「次へ」を選択してください";
        }
        const errorItems = this.state.errorItems;
        const height = this.state.height;
        const maxLengthText =  Config.inputMaxLength.applicantInfo.text;
        const maxLengthTextarea =  Config.inputMaxLength.applicantInfo.textarea;
        const inputBuffLength = 1;
        return (
            <>
                <div className={CustomStyle.div_area}>
                    <Box id="EnterAddInformation" css={`display:block`} >
                        <nav className={CustomStyle.custom_nuv} id="AddApplicantInformationDrag">
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
                            <div className={CustomStyle.scrollContainer} id="ApplicantInfoInputArea" > 
                                {Object.keys(applicantAddInformations).map(key => (
                                    <div className={CustomStyle.box}>
                                        <div className={CustomStyle.caption}>
                                            <Text>
                                                {!applicantAddInformations[key].requireFlag && (
                                                    "("
                                                )}
                                                {applicantAddInformations[key].name}
                                                {!applicantAddInformations[key].requireFlag && (
                                                    ")"
                                                )}</Text>
                                        </div>
                                        <div className={CustomStyle.input_text}>
                                            {errorItems[applicantAddInformations[key].id] && (
                                                <p className={CustomStyle.error}>{errorItems[applicantAddInformations[key].id]}</p>
                                            )}

                                            {/* 0:一行のみ入力欄 */}
                                            {applicantAddInformations[key].itemType == "0" && (
                                                <Input
                                                    light={true}
                                                    dark={false}
                                                    type="text"
                                                    maxLength={maxLengthText + inputBuffLength }
                                                    value={applicantAddInformations[key]?.value}
                                                    placeholder={applicantAddInformations[key].name + "を入力してください"}
                                                    id={applicantAddInformations[key].id}
                                                    onChange={e => this.inputChange(key, e.target.value, maxLengthText)}
                                                />
                                            )}
                                            {/* 1:複数行入力欄 */}
                                            {applicantAddInformations[key].itemType == "1" && (
                                                <textarea 
                                                    className={CustomStyle.input_text_area} 
                                                    type="text" 
                                                    rows={3}
                                                    maxLength={maxLengthTextarea + inputBuffLength }
                                                    value={applicantAddInformations[key]?.value}
                                                    placeholder={applicantAddInformations[key].name + "を入力してください"}
                                                    id={applicantAddInformations[key].id}
                                                    onChange={e => this.inputChange(key, e.target.value, maxLengthTextarea)}
                                                />
                                            )}
                                            {/* 2：日付入力欄 */}
                                            {applicantAddInformations[key].itemType == "2" && (
                                                <Input
                                                    light={true}
                                                    dark={false}
                                                    type="date"
                                                    value={applicantAddInformations[key]?.value}
                                                    placeholder="年/月/日"
                                                    id={applicantAddInformations[key].id}
                                                    onChange={e => this.inputChangeDate(key, e.target.value)}
                                                    max={"9999-12-31"} min={"2000-01-01"}
                                                />
                                            )}
                                            {/* 3：数値入力欄 */}
                                            {applicantAddInformations[key].itemType == "3" && (
                                                <Input
                                                    light={true}
                                                    dark={false}
                                                    type="number"
                                                    value={applicantAddInformations[key]?.value}
                                                    placeholder={applicantAddInformations[key].name + "を入力してください"}
                                                    id={applicantAddInformations[key].id}
                                                    onChange={e => this.inputChangeNumber(key, e.target.value)}
                                                />
                                            )}
                                            {/* 4:単一選択欄 */}
                                            {applicantAddInformations[key].itemType == "4" && (
                                                <Select
                                                    light={true}
                                                    dark={false}
                                                    onChange={e => this.selectedChange(key, e.target.value)}
                                                    placeholder={applicantAddInformations[key].name + "を選択してください"}
                                                >
                                                    <option value={-1}></option>
                                                    {Object.keys(applicantAddInformations[key].itemOptions).map(itemKey => (
                                                        <option key={key + applicantAddInformations[key].itemOptions[itemKey]?.id} value={itemKey} selected={applicantAddInformations[key].itemOptions[itemKey]?.checked}>
                                                            {applicantAddInformations[key].itemOptions[itemKey]?.content}
                                                        </option>
                                                    ))}
                                                </Select>
                                            )}
                                            {/* 5：複数選択欄 */}
                                            {applicantAddInformations[key].itemType == "5" && (

                                                <MultiSelect
                                                    className={CustomStyle.container}
                                                    classNamePrefix ="multi_select"
                                                    onChange={(value) => this.multiSelectedChange(key, value)}
                                                    isClearable={false}
                                                    isSearchable={false}
                                                    isMulti={true}
                                                    components={{IndicatorSeparator: () => null , DropdownIndicator: DropdownIndicator}}
                                                    defaultValue={this.getDefaultValue(applicantAddInformations[key].itemOptions)}
                                                    options={this.getOptions(applicantAddInformations[key].itemOptions)}
                                                    menuPlacement={"auto"}
                                                >
                                                    
                                                </MultiSelect>
                                            )}
                                        </div>
                                    </div>
                                ))}
                                <Spacing bottom={2} />
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
                    {/* )} */}
                </div>
            </>
        );
    }
}
export default withTranslation()(withTheme(EnterAddInformation));