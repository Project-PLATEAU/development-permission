import { observer } from "mobx-react";
import PropTypes from "prop-types";
import React from "react";
import { withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import Spacing from "../../../../Styled/Spacing";
import Box from "../../../../Styled/Box";
import CustomStyle from "./scss/answer-history-list.scss";
import _isEqual from 'lodash/isEqual';
import Icon, { StyledIcon } from "../../../../Styled/Icon";

/**
 * 【R6】回答履歴一覧コンポーネント
 */
@observer
class AnswerHistoryList extends React.Component {
    static displayName = "AnswerHistoryList";
    static propTypes = {
        terria: PropTypes.object.isRequired,
        viewState: PropTypes.object.isRequired,
        theme: PropTypes.object,
        t: PropTypes.func.isRequired,
        answerHistory: PropTypes.array,
        applicationStepId: PropTypes.number
    }
    constructor(props) {
        super(props);
        this.state = {
            viewState: props.viewState,
            terria: props.terria,
            //回答履歴
            answerHistory: props.answerHistory,
            //申請段階ID
            applicationStepId: props.applicationStepId
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
        if (!_isEqual(this.props.answerHistory, prevProps.answerHistory)) {
            this.setState({answerHistory:this.props.answerHistory});
        }
        if (!_isEqual(this.props.applicationStepId, prevProps.applicationStepId)) {
            this.setState({applicationStepId:this.props.applicationStepId});
        }
    }

    render() {
        const answerHistory = this.state.answerHistory;
        const applicationStepId = this.state.applicationStepId;
        return (
            <Box
                centered
                displayInlineBlock
                className={CustomStyle.custom_content}
            >
                <h2 className={CustomStyle.title}>回答履歴一覧</h2>
                <Spacing bottom={1} />
                <div className={CustomStyle.scroll_container} id="AnswerHistoryListTable" style={{height: "18vh", minHeight:"200px"}}>    
                    <table className={CustomStyle.selection_table}>
                        <thead>
                            {applicationStepId != 2 && (
                                <tr className={CustomStyle.table_header}>
                                    <th style={{ width: 20 + "%" }}>回答日時</th>
                                    <th style={{ width: 20 + "%" }}>回答者</th>
                                    <th style={{ width: 20 + "%" }}>対象</th>
                                    <th style={{ width: 30 + "%" }}>回答内容</th>
                                    <th style={{ width: 10 + "%" }}>通知</th>
                                </tr>
                            )}
                            {applicationStepId == 2 && (
                                <tr className={CustomStyle.table_header}>
                                    <th style={{ width: 20 + "%" }}>回答日時</th>
                                    <th style={{ width: 15 + "%" }}>回答者</th>
                                    <th style={{ width: 15 + "%" }}>対象</th>
                                    <th style={{ width: 20 + "%" }}>回答内容</th>
                                    <th style={{ width: 20 + "%" }} colSpan="2">行政確定登録</th>
                                    <th style={{ width: 10 + "%" }}>通知</th>
                                </tr>
                             )}
                        </thead>
                        <tbody>
                        {answerHistory && Object.keys(answerHistory).map(index => (
                            <tr key={`AnswerHistoryListTable-tr-`+index }
                                className={answerHistory[index].answerDataType == "7" ? CustomStyle.deleted_line :""} 
                            >
                                <td>{answerHistory[index].updateDatetime}</td>
                                <td>
                                    {
                                        answerHistory[index].answererUser.departmentName + 
                                        "　" + 
                                        answerHistory[index].answererUser.userName
                                    }
                                
                                </td>
                                <td>{answerHistory[index].title}</td>
                                <td>{answerHistory[index].answerContent}</td>
                                {applicationStepId == 2 && (
                                    <td>
                                        <div className={CustomStyle.flex_center} style={{justifyContent:"space-between"}}>
                                            <div className={CustomStyle.flex_center} >
                                                {answerHistory[index].governmentConfirmStatus == "0" && (
                                                    <span>{`合意`}</span>
                                                )}
                                                {answerHistory[index].governmentConfirmStatus == "1" && (
                                                    <span>{`取下`}</span>
                                                )}
                                                {answerHistory[index].governmentConfirmStatus == "2" && (
                                                    <span>{`却下`}</span>
                                                )}
                                                <span style={{marginRight: "10px"}}></span>
                                                <span className={CustomStyle.info_icon} style={{display:answerHistory[index]["governmentConfirmComment"]? "":"none"}}>
                                                    <StyledIcon 
                                                        glyph={Icon.GLYPHS.info}
                                                        styledWidth={"20px"}
                                                        styledHeight={"20px"}
                                                        light
                                                    />
                                                    <span className={CustomStyle.info_comment}>{answerHistory[index]["governmentConfirmComment"]?answerHistory[index]["governmentConfirmComment"]:""}</span>
                                                </span>
                                            </div>
                                            <div></div>
                                        </div>
                                    </td>
                                )}
                                {applicationStepId == 2 && (
                                    <td>{answerHistory[index].governmentConfirmDatetime}</td>
                                )}
                                <td>
                                    {answerHistory[index].notifiedFlag &&(
                                        <div className={CustomStyle.ellipse}>
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
            </Box>
        );
    }
}
export default withTranslation()(withTheme(AnswerHistoryList));