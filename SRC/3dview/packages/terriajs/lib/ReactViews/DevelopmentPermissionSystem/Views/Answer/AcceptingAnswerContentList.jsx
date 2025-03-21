import { observer } from "mobx-react";
import PropTypes from "prop-types";
import React from "react";
import { withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import Spacing from "../../../../Styled/Spacing";
import Box from "../../../../Styled/Box";
import CustomStyle from "./scss/content-list.scss";
import Icon, { StyledIcon } from "../../../../Styled/Icon";
import _isEqual from 'lodash/isEqual';
import Config from "../../../../../customconfig.json";

/**
 *【R6】事前協議：受付確認の受付回答一覧
 */
@observer
class AcceptingAnswerContentList extends React.Component {
    static displayName = "AcceptingAnswerContentList";
    static propTypes = {
        terria: PropTypes.object.isRequired,
        viewState: PropTypes.object.isRequired,
        theme: PropTypes.object,
        t: PropTypes.func.isRequired,
        departmentAcceptingAnswers: PropTypes.array,
        intervalID:PropTypes.number,
        clickAnswer:PropTypes.func.isRequired,
        selectedAnswerId:PropTypes.number,
        selectedDepartmentAnswerId:PropTypes.number,
        // editable:PropTypes.bool,
        callback:PropTypes.func,
        // addAnswer:PropTypes.func,
        // deleteAnswer:PropTypes.func,
        ledgerMaster: PropTypes.array,
        applicationId: PropTypes.number
        // changeAnswerInfo:PropTypes.func
    }

    constructor(props) {
        super(props);
        this.state = {
            viewState: props.viewState,
            terria: props.terria,
            //受付回答一覧
            departmentAcceptingAnswers: props.departmentAcceptingAnswers,
            //選択された受付回答ID
            selectedAnswerId:0,
            //選択された部署回答ID
            selectedDepartmentAnswerId:0,
            // 協議対象一覧
            ledgerMaster: props.ledgerMaster,
            ledgerMasterCount: 0
        };
    }

    /**
     * 初期処理
     */
    componentDidMount() {
        // this.updateCategorizedAnswers();
    }

    /**
     * props更新時
     */
    componentDidUpdate(prevProps) {
        if (!_isEqual(this.props.departmentAcceptingAnswers, prevProps.departmentAcceptingAnswers)) {
            this.setState({ departmentAcceptingAnswers:this.props.departmentAcceptingAnswers });
        }
        if (!_isEqual(this.props.selectedAnswerId, prevProps.selectedAnswerId)) {
            this.setState({ selectedAnswerId:this.props.selectedAnswerId });
        }
        if (!_isEqual(this.props.selectedDepartmentAnswerId, prevProps.selectedDepartmentAnswerId)) {
            this.setState({ selectedDepartmentAnswerId:this.props.selectedDepartmentAnswerId });
        }
        if (!_isEqual(this.props.ledgerMaster, prevProps.ledgerMaster)) {
            this.setState({ ledgerMaster:this.props.ledgerMaster });
        }
    }

     /**
     * 回答内容一覧の行クリックイベント
     * @param {*} event イベント
     * @param {*} answerId 回答ID
     */
    clickAnswer(event, answerId, departmentAnswerId){
        if(this.validateAnswerId(answerId) || this.validateAnswerId(departmentAnswerId)){
            let selectedAnswerId = 0;
            let selectedDepartmentAnswerId = 0;
            if(this.validateAnswerId(answerId)){
                selectedAnswerId = answerId;
            }
            if(this.validateAnswerId(departmentAnswerId)){
                selectedDepartmentAnswerId = departmentAnswerId;
            }
            this.setState({selectedAnswerId: selectedAnswerId, selectedDepartmentAnswerId: selectedDepartmentAnswerId});
            this.props.clickAnswer(event, answerId, departmentAnswerId);
        }
    }

    validateAnswerId(answerId){
        let result = false;
        if(answerId!==undefined && answerId !== null && Number(answerId) > 0){
            result = true;
        }
        return result;
    }

    /**
     * 変数が空であるか判断
     * @param {*} text 
     * @returns 
     */
    isInputed(text){

        if(text == undefined || text == null || text == "" ){
            return false;
        }else{
            return true;
        }
    }

    /**
     * 日付項目フォーマット
     * @param {*} date 
     * @returns 
     */
    formatToInputdate(date){
        if(date === undefined || date === null){
            return "";
        }else{
            return date.replaceAll('/', '-');
        }
    }

    render() {

        const departmentAcceptingAnswers = this.state.departmentAcceptingAnswers;
        const selectedAnswerId = this.state.selectedAnswerId;
        const selectedDepartmentAnswerId = this.state.selectedDepartmentAnswerId;
        const ledgerMaster = this.state.ledgerMaster;
        const ledgerMasterCount = Object.keys(ledgerMaster).length;
        const isAdmin = this.props.terria.authorityJudgment();
        return (
            <Box
                centered 
                displayInlineBlock 
                className={CustomStyle.custom_content}
            >
                <Spacing bottom={2} />
                <Spacing bottom={2} />
                <Box col12>
                    <div className={CustomStyle.scroll_container} style={{height: isAdmin?"47vh":"37.5vh", overflowX: "auto"}} >
                        <table className={CustomStyle.selection_table+" no-sort"}>
                            <thead className={ CustomStyle.fixHeader} >
                                <tr className={CustomStyle.table_header}>
                                    <th style={{width:"30%"}}>関連条項</th>
                                    <th style={{width:"30%"}} colSpan={ledgerMasterCount + 1}>協議対象/行政回答</th>
                                    <th style={{width:"20%"}} colSpan="2">事業者合意登録</th>
                                    <th style={{width:"20%"}} colSpan="2">行政確定登録</th>
                                </tr>
                            </thead>
                            <tbody>
                                {departmentAcceptingAnswers && Object.keys(departmentAcceptingAnswers).map(key1 => (
                                    <>
                                            <tr id={"tr" + key1} 
                                                className={CustomStyle.title_row}
                                            >
                                                <td colSpan={ledgerMasterCount + 4}>
                                                    <div className={CustomStyle.flex_center}>
                                                        {departmentAcceptingAnswers[key1].department?.departmentName}
                                                        {departmentAcceptingAnswers[key1]["answerFiles"].length > 0 && (
                                                            <div className={CustomStyle.info_icon} style={{width:"35px"}}>
                                                                <StyledIcon 
                                                                    glyph={Icon.GLYPHS.fileUpload}
                                                                    styledWidth={"20px"}
                                                                    styledHeight={"20px"}
                                                                    light
                                                                />
                                                            </div>
                                                        )}
                                                    </div>
                                                </td>
                                                <td><span>{``}</span></td>
                                                <td><span>{``}</span></td>
                                            </tr>
                                        {departmentAcceptingAnswers[key1].acceptingAnswers && Object.keys(departmentAcceptingAnswers[key1].acceptingAnswers).map(key2 => (
                                            <tr key={"tr" + key1 + "" + key2 }
                                            >
                                                {/* 関連条項 */}
                                                <td>
                                                    {departmentAcceptingAnswers[key1].acceptingAnswers[key2]["judgementInformation"]["title"]}
                                                </td>

                                                {/* 32協議対象 */}
                                                {ledgerMaster && Object.keys(ledgerMaster).map(i => (
                                                    <td style={{width:"5%"}}>
                                                        <span>{``}</span>
                                                    </td>
                                                ))}

                                                {/* 行政回答内容 */}
                                                <td>
                                                    {departmentAcceptingAnswers[key1].acceptingAnswers[key2].answerContent}
                                                </td>

                                                {/* 事業者合意登録-ステータス */}
                                                <td style={{width:"10%"}}>
                                                    <span>{``}</span>
                                                </td>

                                                {/* 事業者合意登録-日付 */}
                                                <td style={{width:"10%"}}>
                                                    <span>{``}</span>
                                                </td>

                                                {/* 行政確定登録ステータス */}
                                                <td style={{width:"10%"}}>
                                                    <span>{``}</span>
                                                </td>

                                                {/* 行政確定登録日時 */}
                                                <td style={{width:"10%"}}>
                                                    <span>{``}</span>
                                                </td>
                                            </tr>
                                        ))}
                                    </>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </Box>
            </Box>
        );
    }
}
export default withTranslation()(withTheme(AcceptingAnswerContentList));