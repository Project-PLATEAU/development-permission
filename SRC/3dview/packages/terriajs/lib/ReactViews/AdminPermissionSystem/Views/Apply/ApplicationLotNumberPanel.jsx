import { observer } from "mobx-react";
import { action, reaction, runInAction } from "mobx";
import PropTypes from "prop-types";
import React, { Component } from 'react';
import { withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import classNames from "classnames";
import CustomStyle from "./scss/ApplicationLotNumberPanel.scss";
import Styles from "../../../FeatureInfo/feature-info-panel.scss";
import Icon from "../../../../Styled/Icon";
import DragWrapper from "../../../DragWrapper";
import Config from "../../../../../customconfig.json";
import Common from "../../../AdminDevelopCommon/CommonFixedValue.json";
import Loader from "../../../Loader";
import AnimatedSpinnerIcon from "../../../../Styled/AnimatedSpinnerIcon";
import Button from "../../../../Styled/Button";

/**
 * 申請一覧画面
 */
@observer
class ApplicationLotNumberPanel extends React.Component {
    
    static displayName = "ApplicationLotNumberPanel";

    static propTypes = {
        terria: PropTypes.object.isRequired,
        viewState: PropTypes.object.isRequired
    }

    constructor(props){
        super(props);
        this.state = {
            viewState: props.viewState,
            terria: props.terria,
            isLoding: true, 
            lotNumbers: [], //地番リスト
            applicantInformation: {}
        }
    }

    /**
     * 初期処理
     */
    componentDidMount(){
        const lotNumbers = this.props.viewState.applicationLotNumberList;

        if (lotNumbers.length !== 0) {
            const applicantInformation = {};
    
            const fetches = lotNumbers.map((application, index) => {
                return fetch(Config.config.apiUrl + "/application/detail/" + application.applicationId)
                    .then(res => {
                        if (res.status === 401) {
                            // 401認証エラーの場合の処理を追加
                            alert('認証情報が無効です。ページの再読み込みを行います。');
                            window.location.href = '/login';
                            throw new Error('Unauthorized'); 
                        }
                        if (!res.ok) {
                            throw new Error("Network response was not ok");
                        }
                        return res.json();
                    })
                    .then(res => {
                        if (res.applicationId) {
                            applicantInformation[index] = {
                                applicationId: res.applicationId,
                                status: res.status,
                                applicantName: res.applicantInformations[0].value
                            };
                        }
                    });
            });
    
            Promise.all(fetches).then(() => {
                this.setState({ applicantInformation: applicantInformation });
                this.setState({ isLoding: false});
            }).catch(error => {
                console.error("There was a problem with the fetch operation:", error.message);
            });
        }
    }

    /**
     * 申請一覧画面を閉じる
     */
    @action.bound
    close(){
        this.props.viewState.showApplicationLotNumberPanel = false;
    }

    /**
     * 申請情報詳細画面へ遷移
     * @param {*} applicationId 申請ID 
     */
    @action.bound
    details(applicationId){
        this.props.viewState.nextApplicationDetailsView(applicationId);
        const htmlElement = document.getElementById("refreshConfirmApplicationDetails");
        if(htmlElement){
            htmlElement.click();
        }
    }

    render(){
        const { t } = this.props;
        const isLoding = this.state.isLoding; 
        const applicantInformation = this.state.applicantInformation;
        return (            
            <div className={CustomStyle.applicationLotNumberPanel}>
                <DragWrapper>
                    <div>
                        <div className={CustomStyle.header}>
                            <div
                                className={classNames("drag-handle", CustomStyle.btnPanelHeading)}
                            >
                                <span>申請一覧</span>
                            </div>
                            <button
                                type="button"
                                onClick={this.close}
                                className={Styles.btnCloseFeature}
                                title={t("featureInfo.btnCloseFeature")}
                            >
                                <Icon glyph={Icon.GLYPHS.close} />
                            </button>
                        </div>
                    </div>

                    {isLoding && (
                        <div className={CustomStyle.loader}>
                            <Loader />
                        </div>
                    )}

                    {!isLoding && (
                        <table>
                            <thead>
                                <tr>
                                    <th style={{width: "15%"}}>申請ID</th>
                                    <th style={{width: "20%"}}>ステータス</th>
                                    <th style={{width: "30%"}}>申請者</th>
                                    <th style={{width: "25%"}}></th>
                                </tr>
                            </thead>
                            <tbody>
                                {Object.keys(applicantInformation).map(key => (
                                    <tr key={key}>
                                        <td>{applicantInformation[key]["applicationId"]}</td>
                                        <td>{applicantInformation[key]["status"]}</td>
                                        <td>{applicantInformation[key]["applicantName"]}</td>
                                        <td>
                                            <Button
                                                className={CustomStyle.button}
                                                onClick={e => this.details(applicantInformation[key]["applicationId"])}
                                            >
                                                <span className={CustomStyle.button_name}>申請情報詳細</span>
                                            </Button>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    )}
                </DragWrapper>
            </div>
        )
    }

}
export default withTranslation()(withTheme(ApplicationLotNumberPanel));