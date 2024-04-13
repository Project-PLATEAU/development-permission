import React from "react";
import PropTypes from "prop-types";
import { observer } from "mobx-react";
import { withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import ShowMessage from "../Views/Message/ShowMessage";
import AdminTab from "../Views/Tab/AdminTab";
import LayerTab from "../../DevelopmentPermissionSystem/Views/layer/LayerTab";
import Box from "../../../Styled/Box";
import Styles from "../../DevelopmentPermissionSystem/PageViews/scss/pageStyle.scss";
import CustomStyle from "./scss/custumStyle.scss";
import InquiryList from "../Views/Apply/InquiryList";
import AnswerList from "../Views/Apply/AnswerList";
import Config from "../../../../customconfig.json";

/**
 * 行政画面：レイヤ表示
 */
@observer
class AdminLayerView extends React.Component {
    static displayName = "AdminLayerView"
    static propsType = {
        terria: PropTypes.object.isRequired,
        viewState: PropTypes.object.isRequired,
        items: PropTypes.array,
        onActionButtonClicked: PropTypes.func,
        t: PropTypes.func.isRequired
    }

    constructor(props) {
        super(props);
        this.state = {
            viewState: props.viewState,
            terria: props.terria,
            height: 800,
             // 自分担当課に対する問い合わせ一覧
             inquiries: [],
             // 自分担当課に対する回答（申請）一覧 
             answers: []
        }
    }

    /**
     * 初期表示
    */
    componentDidMount() {

        // 担当課の問合せ・回答一覧取得
        this.getResponsibleInquiries();  

        // 30秒につき、問い合わせ内容をリフレッシュする
        let intervalID = setInterval(() => {
            if(this.props.viewState.adminTabActive === "layershow"){
                
                this.getResponsibleInquiries();
            
            }else{
                return;
            }
            
        }, 30000);        
    }

    /**
     * 担当課の問合せ・回答一覧取得
     */
    getResponsibleInquiries(){
        fetch(Config.config.apiUrl + "/chat/inquiries")
        .then(res => {
            // 401認証エラーの場合の処理を追加
            if (res.status === 401) {
                alert('認証情報が無効です。ページの再読み込みを行います。');
                window.location.href = "./login/";
                return null;
            }
            return res.json();
        })
        .then(res => {
            if (Object.keys(res).length > 0) {
             this.setState({inquiries: res.inquiries, answers:res.answers});
            }else{
                alert('担当課の問合せ・回答一覧取得に失敗しました');
            }
        }).catch(error => {
            console.error('通信処理に失敗しました', error);
            alert('通信処理に失敗しました');
        });
    }

    // 問合せタブをクリック
    clickInquiryListModel(){
        this.props.viewState.changeShowInquiryList(true);
        this.props.viewState.changeShowAnswerList(false);
        // console.log("問合せ");
    }

    // 回答タブをクリック
    clickAnswerListModel(){
        this.props.viewState.changeShowInquiryList(false);
        this.props.viewState.changeShowAnswerList(true);
        // console.log("回答");
    }

    render(){
        const t = this.props.t;
        let tabAreaHeight = this.state.height - 330;
        if(!this.props.viewState.showLotNumberSelected){
            tabAreaHeight = tabAreaHeight -330;
        }
        let inquiries = this.state.inquiries;
        let answers = this.state.answers;
        return (
            <>
                <div style={{width: "100%", margin: "0"}}>
                    <ShowMessage t={t} message={"infoMessage.tipsForLayerTab"} />
                    <AdminTab terria={this.props.terria} viewState={this.props.viewState} t={t}/>
                </div>
                    
                <div style={{height: "calc(100vh - 160px)" ,overflowY: "auto"}}>
                    <div className={Styles.tab_div_area}  id="tabArea" 
                        style={{height: tabAreaHeight + "px"}}
                    >
                        <LayerTab terria={this.props.terria} viewState={this.state.viewState}  tabAreaHeight = {tabAreaHeight} />
                    </div>

                    <ShowMessage t={t} message={"adminInfoMessage.tipsForApplyList"} />
                        <Box padded>
                            <button className={`${Styles.btn_baise_style} ${CustomStyle.button} ${this.props.viewState.showInquiryList? "": Styles.btn_gry}`}
                                onClick={evt => {
                                    evt.preventDefault();
                                    evt.stopPropagation();
                                    this.clickInquiryListModel();
                                }} id="">
                                <span>問合せ</span>
                                <span className={CustomStyle.badge}>{Object.keys(inquiries).length}</span>
                            </button>

                            <button className={`${Styles.btn_baise_style} ${CustomStyle.button} ${this.props.viewState.showAnswerList? "": Styles.btn_gry}`}
                                onClick={evt => {
                                    evt.preventDefault();
                                    evt.stopPropagation();
                                    this.clickAnswerListModel();
                                }} id="">
                                <span>回答</span>
                                <span className={CustomStyle.badge}>{Object.keys(answers).length}</span>
                            </button>
                        </Box>

                        <div>
                            <If condition = {this.props.viewState.showInquiryList}>
                                <div className={Styles.component_border} style={{height: "30vh", overflowY: "auto", marginBottom:"0"}}>
                                    <InquiryList terria={this.props.terria} viewState={this.props.viewState} referrer={"layershow"} inquiries={inquiries}/> 
                                </div>
                            </If>
                            <If condition = {this.props.viewState.showAnswerList}>
                                <div className={Styles.component_border} style={{height: "30vh", overflowY: "auto", marginBottom:"0"}}>
                                    <AnswerList terria={this.props.terria} viewState={this.props.viewState} referrer={"layershow"} answers={answers} /> 
                                </div>
                            </If>
                        </div>
                </div>
            </>
        );
    };
}
export default withTranslation()(withTheme(AdminLayerView));