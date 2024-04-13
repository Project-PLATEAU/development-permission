import { observer } from "mobx-react";
import React from "react";
import PropTypes from "prop-types";
import { withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import CustomStyle from "./scss/inquiry-status-list.scss";
import Button, { RawButton } from "../../../../Styled/Button";
import Common from "../../../AdminDevelopCommon/CommonFixedValue.json";

/**
 * 回答申請情報一覧コンポーネント
 */
@observer
class AnswerStatusList extends React.Component {
    static propTypes = {
        terria: PropTypes.object.isRequired,
        viewState: PropTypes.object.isRequired,
        theme: PropTypes.object,
        t: PropTypes.func.isRequired,
        referrer: PropTypes.string,
        answers: PropTypes.array.isRequired
    }

    constructor(props){
        super(props);
        this.state = {
            viewState: props.viewState,
            terria: props.terria
        }
    }

    /**
     * 申請情報詳細へ遷移
     * @param {*} applicationId 申請ID
     */
    showApplyDdetail(applicationId){
        this.state.viewState.applicationInformationSearchForApplicationId = applicationId;
        this.state.viewState.changeAdminTabActive("applySearch");
        this.state.viewState.changeApplyPageActive("applyDetail")
        this.state.viewState.setAdminBackPage(this.props.referrer, "");
    }

    render(){
        let answers = this.props.answers;
        return (
            <div>
                <table className={CustomStyle.inquiry_list_table}>
                    <thead>
                        <tr>
                            <th style={{width: "40%"}}>ステータス</th>
                            <th style={{width: "40%"}}>申請ID</th>
                            <th style={{width: "20%"}}></th>
                        </tr>
                    </thead>
                    <tbody>
                        {answers && Object.keys(answers).map(key => ( 
                            <tr key={`answers-${key}`}>
                                <td>{answers[key]["status"]}</td>
                                <td>{answers[key]["applicationId"]}</td>
                                <td><Button primary fullWidth onClick={e=>{this.showApplyDdetail(answers[key]["applicationId"])}}>詳細</Button></td>
                            </tr>
                        ))}
                    </tbody>
                </table>

            </div>
        );
    }

}
export default withTranslation()(withTheme(AnswerStatusList));