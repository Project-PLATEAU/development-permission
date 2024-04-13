import { observer } from "mobx-react";
import PropTypes from "prop-types";
import React from "react";
import {  withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import Box from "../../../Styled/Box";
import Styles from "./scss/pageStyle.scss";
import GeneralConditionDiagnosis from "../Views/Apply/GeneralConditionDiagnosis.jsx";
import Config from "../../../../customconfig.json";

/**
 * 事業者用画面：概況診断画面
 */
@observer
class GeneralAndRoadJudgementResultView extends React.Component {
    static displayName = "GeneralAndRoadJudgementResultView";
    static propTypes = {
        terria: PropTypes.object.isRequired,
        viewState: PropTypes.object.isRequired,
        applicationPlace: PropTypes.object,
        theme: PropTypes.object,
        t: PropTypes.func.isRequired
    }

    constructor(props) {
        super(props);
        this.state = {
            viewState: props.viewState,
            terria: props.terria,
            // 概況診断結果表示画面の案内文言
            viewLabel:""
        };
    }

    /**
     * 初期処理
    */
    componentDidMount(){
        let applicationPlace = Object.values(this.props.viewState.applicationPlace);
        applicationPlace = applicationPlace.filter(Boolean);
        this.props.viewState.setLotNumbers(applicationPlace);
        //概況診断結果表示画面の案内文言取得
        this.getViewLabel();
    }

    /**
     * DBから概況診断結果表示画面の案内文言取得
     */
    getViewLabel(){
        //サーバからlabelを取得
        fetch(Config.config.apiUrl + "/label/1005")
        .then(res => res.json())
        .then(res => {
            if(res.status === 401){
                alert("認証情報が無効です。ページの再読み込みを行います。");
                window.location.reload();
                return null;
            }
            if (Object.keys(res).length > 0) {
                let message = res[0]?.labels?.judgementContent;
                this.setState({ viewLabel: message });
            }else{
                alert("labelの取得に失敗しました。");
            }
        }).catch(error => {
            console.error('通信処理に失敗しました', error);
            alert('通信処理に失敗しました');
        });
    }

    render() {
        const viewLabel = this.state.viewLabel;
        return (
            <>
                <Box column >
                    <div className={Styles.div_area}>
                        <Box padded  className={Styles.text_area}>
                            <div dangerouslySetInnerHTML={{ __html: viewLabel }}></div>
                        </Box>

                        { viewLabel && ( <GeneralConditionDiagnosis terria={this.props.terria} viewState={this.props.viewState} /> )}
                    </div>
                </Box>
            </>
        );
    };
}

export default withTranslation()(withTheme(GeneralAndRoadJudgementResultView));