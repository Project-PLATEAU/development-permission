import { observer } from "mobx-react";
import React from "react";
import PropTypes from "prop-types";
import { withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import CustomStyle from "./scss/inquiry-status-list.scss";
import Button, { RawButton } from "../../../../Styled/Button";
import Common from "../../../AdminDevelopCommon/CommonFixedValue.json";

/**
 * 問い合わせ状況一覧
 */
@observer
class InquiryStatusList extends React.Component {
    static propTypes = {
        terria: PropTypes.object.isRequired,
        viewState: PropTypes.object.isRequired,
        theme: PropTypes.object,
        t: PropTypes.func.isRequired,
        referrer: PropTypes.string,
        inquiries: PropTypes.array.isRequired,
        callback:PropTypes.func
    }

    constructor(props){
        super(props);
        this.state = {
            viewState: props.viewState,
            terria: props.terria
        }
    }

    /**
     * 問合せ画面へ遷移
     * @param {*} chatObj 問い合わせ情報
     */
    showChat( chatObj ){
        // console.log(`参照元：${this.props.referrer}`)
        this.state.viewState.changeAdminTabActive("applySearch");
        this.state.viewState.changeApplyPageActive("chat");
        this.state.viewState.moveToAdminChatView(chatObj,this.props.referrer, "");
        this.state.viewState.setAdminBackPage(this.props.referrer, "");
    }

    /**
     * 一覧の行をクリックする
     * @param {*} resultData 
     */
    showLotNumberLayers(event, resultData){
        const applicationId = resultData.applicationId;
        const LotNumbers = resultData.lotNumbers;

        if(applicationId!==undefined && applicationId !== null && Number(applicationId) > 0){
            if(LotNumbers){
                this.props.callback(applicationId, LotNumbers);
            }
        }
    }

    /**
     * ステータスに対するラベルを取得
     * @param {Number} status ステータスコード
     */
    getStatusLabel(status){

       return Common.answerstatus[status];
    }

    render(){
        let inquiries = this.props.inquiries;
        return (
            <>
                <table className={CustomStyle.inquiry_list_table}>
                    <thead>
                        <tr>
                            <th style={{width: "40%"}}>ステータス</th>
                            <th style={{width: "40%"}}>申請ID</th>
                            <th className="no-sort" style={{width: "20%"}}></th>
                        </tr>
                    </thead>
                    <tbody>
                        {inquiries && Object.keys(inquiries).map(key => ( 
                            <tr key={`inquiries-${key}`}  onClick={event => {this.showLotNumberLayers(event, inquiries[key])}}>
                                <td>{this.getStatusLabel(inquiries[key]["status"])}</td>
                                <td>{inquiries[key]["applicationId"]}</td>
                                <td><Button primary fullWidth onClick={e=>{this.showChat(inquiries[key])}}>詳細</Button></td>
                            </tr>
                        ))}
                    </tbody>
                </table>

            </>
        );
    }

}
export default withTranslation()(withTheme(InquiryStatusList));