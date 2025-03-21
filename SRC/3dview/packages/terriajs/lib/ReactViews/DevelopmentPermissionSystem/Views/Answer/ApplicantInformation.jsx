import { observer } from "mobx-react";
import PropTypes from "prop-types";
import React from "react";
import { withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import Spacing from "../../../../Styled/Spacing";
import Box from "../../../../Styled/Box";
import CustomStyle from "./scss/answer-content.scss";
import Icon, { StyledIcon } from "../../../../Styled/Icon";
import _isEqual from 'lodash/isEqual';

/**
 *【R6】申請者情報
 */
@observer
class ApplicantInformation extends React.Component {
    static displayName = "ApplicantInformation";
    static propTypes = {
        terria: PropTypes.object.isRequired,
        viewState: PropTypes.object.isRequired,
        theme: PropTypes.object,
        t: PropTypes.func.isRequired,
        applicationType: PropTypes.object.isRequired,
        answers: PropTypes.array,
        departmentAnswers: PropTypes.array,
        checkedApplicationCategory: PropTypes.array.isRequired,
        status:PropTypes.string.isRequired,
        lotNumbers: PropTypes.array.isRequired,
        applicationId: PropTypes.number.isRequired
    }

    constructor(props) {
        super(props);
        const departmentName = [];
        if(props.answers){
            const checkedApplicationStepId = props.viewState.checkedApplicationStepId;
            if(checkedApplicationStepId === 2){
                const answers = props.departmentAnswers;
                Object.keys(answers).map(key => {
                    if (departmentName.indexOf(answers[key].department?.departmentName) < 0) {
                        departmentName.push(answers[key]?.department?.departmentName);
                    }
                });
            }else{
                const answers = props.answers;
                Object.keys(answers).map(key => {
                    let departments = answers[key].judgementInformation?.departments;
                    Object.keys(departments).map(key2 => {
                        if (departmentName.indexOf(departments[key2].departmentName) < 0) {
                            departmentName.push(departments[key2].departmentName);
                        }
                    });
                });
            }
        }
        this.state = {
            viewState: props.viewState,
            terria: props.terria,
            // 申請種類
            applicationType: props.applicationType,
            //回答一覧
            answers: props.answers,
            //部署回答一覧
            departmentAnswers: props.departmentAnswers,
            //選択申請区分
            checkedApplicationCategory:props.checkedApplicationCategory,
            //申請者情報
            applicantInformations:props.applicantInformations,
            //申請追加情報
            applicantAddInformations:props.applicantAddInformations,
            //申請状況
            status:props.status,
            //表示用申請地番
            lotNumberResult:props.lotNumbers,
            //表示用部署名
            departmentName:departmentName,
            //高さ(任意)
            height:props.height,
            // 申請ID
            applicationId: props.applicationId
        };
    }

    /**
     * 初期処理
     */
    componentDidMount() {
    }

    /**
     * props更新時
     */
    componentDidUpdate(prevProps) {
        if (!_isEqual(this.props.answers, prevProps.answers) || !_isEqual(this.props.departmentAnswers, prevProps.departmentAnswers)) {
            const departmentName = [];
            const checkedApplicationStepId = this.props.viewState.checkedApplicationStepId;
            if(checkedApplicationStepId === 2){
                if(this.props.departmentAnswers){
                    const answers = this.props.departmentAnswers;
                    Object.keys(answers).map(key => {
                        if (departmentName.indexOf(answers[key].department?.departmentName) < 0) {
                            departmentName.push(answers[key]?.department?.departmentName);
                        }
                    });
                }
                this.setState({ answers:this.props.answers,departmentName:departmentName });
            }else{
                if(this.props.answers){
                    const answers = this.props.answers;
                    Object.keys(answers).map(key => {
                        let departments = answers[key].judgementInformation?.departments;
                        Object.keys(departments).map(key2 => {
                            if (departmentName.indexOf(departments[key2].departmentName) < 0) {
                                departmentName.push(departments[key2].departmentName);
                            }
                        });
                    });
                }
            }
            this.setState({ answers:this.props.answers,departmentName:departmentName });
        }
       
        if (!_isEqual(this.props.applicantInformations, prevProps.applicantInformations)) {
            this.setState({ applicantInformations:this.props.applicantInformations });
        }
        if (!_isEqual(this.props.applicantAddInformations, prevProps.applicantAddInformations)) {
            this.setState({ applicantAddInformations:this.props.applicantAddInformations });
        }
        if (!_isEqual(this.props.lotNumbers, prevProps.lotNumbers)) {
            this.setState({ lotNumberResult:this.props.lotNumbers });
        }
        if (!_isEqual(this.props.checkedApplicationCategory, prevProps.checkedApplicationCategory)) {
            this.setState({ checkedApplicationCategory:this.props.checkedApplicationCategory });
        }
        if (!_isEqual(this.props.status, prevProps.status)) {
            this.setState({ status:this.props.status });
        }
        if (!_isEqual(this.props.applicationType, prevProps.applicationType)) {
            this.setState({ applicationType:this.props.applicationType });
        }
        if (!_isEqual(this.props.applicationId, prevProps.applicationId)) {
            this.setState({ applicationId:this.props.applicationId });
        }
    }

    /**
     * 申請追加情報の表示テキストを取得
     * @param {*} applicantnAddInformation 
     */
    getDisplayAddInformationText(applicantnAddInformation){

        let text = "";
        let itemType = applicantnAddInformation.itemType;
        
        switch (itemType) {
            case '0': // 0:1行のみの入力欄で表示
            case '1'://1:複数行の入力欄で表示
                text = applicantnAddInformation.value;
                break;
            case '2'://2:日付（カレンダー）
                let dateText = applicantnAddInformation.value;
                if(dateText){
                    // yyyy/MM/dd に変換
                    text = dateText.replaceAll("-","/");
                }else{
                    text = "";
                }
                break;
            case '3'://3:数値
                text = applicantnAddInformation.value;
                break;
            case '4'://4:ドロップダウン単一選択
            case '5'://5:ドロップダウン複数選択
                text = applicantnAddInformation.itemOptions.filter(option => {return option.checked == true}).map(function (value) { return value.content }).join(",");
                break;
            default:
                text = applicantnAddInformation.value;
        }

        return text;
    }

    render() {
        const applicationType = this.state.applicationType;
        const applicantInformations = this.state.applicantInformations;
        const applicantAddInformations = this.state.applicantAddInformations;
        const checkedApplicationCategory = this.state.checkedApplicationCategory;
        const lotNumberResult = this.state.lotNumberResult;
        const departmentName = this.state.departmentName;
        const status = this.state.status;
        const height = this.state.height;
        const isAdmin = this.props.terria.authorityJudgment();
        const applicationId = this.state.applicationId;
        return (
            <Box className={CustomStyle.scroll_container} css={`
                ${height &&
                `height: ${height}!important;`}
                overflow-y:scroll!important;
              `}>
                <Box className={CustomStyle.box}>
                    <Box className={CustomStyle.item} style={{ width: 45 + "%" }}>
                        ■申請ID
                    </Box>
                    <Box className={CustomStyle.item} style={{ width: 55 + "%" }}>
                        {applicationId}
                    </Box>
                </Box>
                <Box className={CustomStyle.box}>
                    <Box className={CustomStyle.item} style={{ width: 45 + "%" }}>
                        ■申請種類
                    </Box>
                    <Box className={CustomStyle.item} style={{ width: 55 + "%" }}>
                        {applicationType.applicationTypeName}
                    </Box>
                </Box>
                <Box className={CustomStyle.box}>
                    <Box className={CustomStyle.item} style={{ width: 45 + "%" }}>
                        ■回答担当課
                    </Box>
                    <Box className={CustomStyle.item} style={{ width: 55 + "%" }}>
                        {departmentName?.join(",")}
                    </Box>
                </Box>
                <Box className={CustomStyle.box}>
                    <Box className={CustomStyle.item} style={{ width: 45 + "%" }}>
                        ■ステータス
                    </Box>
                    <Box className={CustomStyle.item} style={{ width: 55 + "%" }}>
                        {status}
                    </Box>
                </Box>
                <Box className={CustomStyle.box}>
                    <Box className={CustomStyle.item}>
                        ■申請者情報
                    </Box>
                </Box>
                {applicantInformations && Object.keys(applicantInformations).map(key => (
                    <Box className={CustomStyle.box} key={key}>
                        <Box className={CustomStyle.item} style={{ width: 45 + "%" }}>
                            ・{applicantInformations[key]?.name}
                        </Box>
                        <Box className={CustomStyle.item} style={{ width: 55 + "%" }}>
                            {applicantInformations[key]?.value}
                        </Box>
                    </Box>
                ))}
                <Box className={CustomStyle.box}>
                    <Box className={CustomStyle.item}>
                        ■連絡先情報
                    </Box>
                </Box>
                {applicantInformations && Object.keys(applicantInformations).map(key => (
                    applicantInformations[key].contactAddressFlag ?(
                        <Box className={CustomStyle.box} key={key}>
                            <Box className={CustomStyle.item} style={{ width: 45 + "%" }}>
                                ・{applicantInformations[key]?.name}
                            </Box>
                            <Box className={CustomStyle.item} style={{ width: 55 + "%" }}>
                                {applicantInformations[key]?.contactValue}
                            </Box>
                        </Box>
                    ):(null)
                ))}
                {applicantAddInformations && Object.keys(applicantAddInformations).length > 0 && (
                    <>
                        <Box className={CustomStyle.box}>
                            <Box className={CustomStyle.item}>
                                ■申請追加情報
                            </Box>
                        </Box>
                        {Object.keys(applicantAddInformations).map(key => (
                            <Box className={CustomStyle.box} key={key}>
                                <Box className={CustomStyle.item} style={{ width: 45 + "%" }}>
                                    ・{applicantAddInformations[key]?.name}
                                </Box>
                                <Box className={CustomStyle.item} style={{ width: 55 + "%" }}>
                                   {this.getDisplayAddInformationText(applicantAddInformations[key])}
                                </Box>
                            </Box>
                        ))}
                    </>
                )}
                {checkedApplicationCategory && Object.keys(checkedApplicationCategory).map(key => (
                    <Box className={CustomStyle.box} key={key}>
                        <Box className={CustomStyle.item} style={{ width: 45 + "%" }}>
                            ■{checkedApplicationCategory[key]?.title}
                        </Box>
                        <Box className={CustomStyle.item} style={{ width: 55 + "%" }}>
                            {checkedApplicationCategory[key]?.applicationCategory?.map(function (value) { return value.content }).join(",")}
                        </Box>
                    </Box>
                ))}
                <Box className={CustomStyle.box}>
                    <Box className={CustomStyle.item} style={{ width: 45 + "%" }}>
                        ■申請地番
                    </Box>
                    <Box className={CustomStyle.item} style={{ width: 55 + "%" }}>
                        <div style={{display: "flex", flexDirection: "column"}}>
                            {lotNumberResult && Object.keys(lotNumberResult).map(key => (
                                <div>{lotNumberResult[key].lot_numbers}</div>
                            ))}
                        </div>
                    </Box>
                </Box>
            </Box>
        );
    }
}
export default withTranslation()(withTheme(ApplicantInformation));