import { observer } from "mobx-react";
import React from "react";
import PropTypes from "prop-types";
import { withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import Box from "../../../../Styled/Box";
import Styles from "../../../DevelopmentPermissionSystem/PageViews/scss/pageStyle.scss";
import CustomStyle  from "../../PageViews/scss/custumStyle.scss";
import ShowMessage from "../../Views/Message/ShowMessage";
import InquiryList from "../../Views/Apply/InquiryList";
import AnswerList from "../../Views/Apply/AnswerList";
import Config  from "../../../../../customconfig.json";
import webMapServiceCatalogItem from '../../../../Models/Catalog/Ows/WebMapServiceCatalogItem';
import CommonStrata from "../../../../Models/Definition/CommonStrata";

/**
 * 行政トップ画面の通知一覧
 */
@observer
class NotificationList  extends React.Component {
    static propTypes = {
        terria: PropTypes.object.isRequired,
        viewState: PropTypes.object.isRequired,
        theme: PropTypes.object,
        t: PropTypes.func.isRequired,
        referrer: PropTypes.string,
    }

    constructor(props){
        super(props);
        this.state = {
            viewState: props.viewState,
            terria: props.terria,
            // 自分担当課に対する問い合わせ一覧
            inquiries: [],
            // 自分担当課に対する回答（申請）一覧 
            answers: [],
            // 自分担当課に対する回答（申請-事前相談）一覧 
            answerConsultationList: [],
             // 自分担当課に対する回答（申請-事前協議）一覧 
            answerDiscussionsList: [],
             // 自分担当課に対する回答（申請-事前許可判定）一覧 
            answerPermissionJudgmentList: [],
            intervalID: null,
            activeListId: 0
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
            if(this.props.viewState.adminTabActive === "mapSearch" || 
                this.props.viewState.adminTabActive === "layershow"){
                
                this.getResponsibleInquiries()
            
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

                let inquiries = res.inquiries;
                let answerConsultationList = res.consultationApplys;
                let answerDiscussionsList = res.discussionApplys;
                let answerPermissionJudgmentList = res.permissionApplys;
                
                this.setState({
                    inquiries: inquiries, 
                    answerConsultationList: answerConsultationList,
                    answerDiscussionsList: answerDiscussionsList,
                    answerPermissionJudgmentList: answerPermissionJudgmentList
                });

            }else{
                alert('担当課の問合せ・回答一覧取得に失敗しました');
            }
        }).catch(error => {
            console.error('通信処理に失敗しました', error);
            alert('通信処理に失敗しました');
        });



    }

    /**
     * 一覧のアクティブタブの変更
     */
    changeListActiveTab(activeListId){

        if(activeListId == 0 ){
            this.props.viewState.changeShowInquiryList(true);
            this.props.viewState.changeShowAnswerList(false);
            console.log("問合せ");
        }else{
            this.props.viewState.changeShowInquiryList(false);
            this.props.viewState.changeShowAnswerList(true);
            console.log("回答");
        }

        this.setState({activeListId: activeListId});
    }

    /**
     * 申請地レイヤーの表示
     * @param {object} applicationId 申請ID
     * @param {object} lotNumbers 申請情報の地番
     */
    showLotNumberLayers = (applicationId, lotNumbers)  => {
        try{
            const wmsUrl = Config.config.geoserverUrl;
            const items = this.state.terria.workbench.items;
            let layerFlg = false;
            if (applicationId) {
                for (const aItem of items) {
                    if (aItem.uniqueId === Config.layer.lotnumberSearchLayerNameForApplicationSearchTarget) {
                        aItem.setTrait(CommonStrata.user,
                            "parameters",
                            {
                                "viewparams": Config.layer.lotnumberSearchViewParamNameForApplicationSearchTarget + applicationId,
                            });
                        aItem.loadMapItems();
                        layerFlg = true;
                    }
                }
            }
            if (lotNumbers) {
                this.focusMapPlaceDriver(lotNumbers);
            }

            if(!layerFlg){
                if (applicationId) {
                    const item = new webMapServiceCatalogItem(Config.layer.lotnumberSearchLayerNameForApplicationSearchTarget, this.state.terria);
                    item.setTrait(CommonStrata.definition, "url", wmsUrl);
                    item.setTrait(CommonStrata.user, "name", Config.layer.lotnumberSearchLayerDisplayNameForApplicationSearchTarget);
                    item.setTrait(
                        CommonStrata.user,
                        "layers",
                        Config.layer.lotnumberSearchLayerNameForApplicationSearchTarget);
                    item.setTrait(CommonStrata.user,
                        "parameters",
                        {
                            "viewparams": Config.layer.lotnumberSearchViewParamNameForApplicationSearchTarget + applicationId,
                        });
                    item.loadMapItems();
                    this.state.terria.workbench.add(item);
                }
            }
        } catch (error) {
            console.error('処理に失敗しました', error);
        }
    }

    /**
     * フォーカス処理ドライバー
     * @param {object} lotNumbers 申請地情報
     */
    focusMapPlaceDriver(lotNumbers) {
        let applicationPlace = Object.values(lotNumbers);
        applicationPlace = applicationPlace.filter(Boolean);
        let maxLon = 0;
        let maxLat = 0;
        let minLon = 0;
        let minLat = 0;
        Object.keys(applicationPlace).map(key => {
            const targetMaxLon = parseFloat(applicationPlace[key].maxlon);
            const targetMaxLat = parseFloat(applicationPlace[key].maxlat);
            const targetMinLon = parseFloat(applicationPlace[key].minlon);
            const targetMinLat = parseFloat(applicationPlace[key].minlat);
            if (key === 0 || key === "0") {
                maxLon = targetMaxLon;
                maxLat = targetMaxLat;
                minLon = targetMinLon;
                minLat = targetMinLat;
            } else {
                if (maxLon < targetMaxLon) {
                    maxLon = targetMaxLon;
                }
                if (maxLat < targetMaxLat) {
                    maxLat = targetMaxLat;
                }
                if (minLon > targetMinLon) {
                    minLon = targetMinLon;
                }
                if (minLat > targetMinLat) {
                    minLat = targetMinLat;
                }
            }
        })
        this.outputFocusMapPlace(maxLon, maxLat, minLon, minLat, (maxLon + minLon) / 2, (maxLat + minLat) / 2);
    }

    /**
     * フォーカス処理
     * @param {number} maxLon 最大経度
     * @param {number} maxLat 最大緯度
     * @param {number} minLon 最小経度
     * @param {number} minLat 最小緯度
     * @param {number} lon 経度
     * @param {number} lat 緯度
     */
    outputFocusMapPlace(maxLon, maxLat, minLon, minLat, lon, lat) {
        this.props.terria.focusMapPlace(maxLon, maxLat, minLon, minLat, lon, lat, this.props.viewState);
    }


    render(){
        const t = this.props.t;
        const referrer = this.props.referrer;
        let inquiries = this.state.inquiries;
        let answerConsultationList = this.state.answerConsultationList;
        let answerDiscussionsList = this.state.answerDiscussionsList;
        let answerPermissionJudgmentList = this.state.answerPermissionJudgmentList;
        let activeListId = this.state.activeListId;
        let answers = [];
        if(activeListId == 1){
            answers = answerConsultationList;
        }

        if(activeListId == 2){
            answers = answerDiscussionsList;
        }

        if(activeListId == 3){
            answers = answerPermissionJudgmentList;
        }

        if(activeListId == 0 ){
            this.props.viewState.changeShowInquiryList(true);
            this.props.viewState.changeShowAnswerList(false);
            console.log("問合せ");
        }else{
            this.props.viewState.changeShowInquiryList(false);
            this.props.viewState.changeShowAnswerList(true);
            console.log("回答");
        }

        return (
            <>
               <ShowMessage t={t} message={"adminInfoMessage.tipsForApplyList"} />
               <Box padded>
                    <button className={`${Styles.btn_baise_style} ${CustomStyle.button} ${ activeListId == 0? "": Styles.btn_gry}`}
                        onClick={evt => {
                            evt.preventDefault();
                            evt.stopPropagation();
                            this.changeListActiveTab(0);
                        }} id="">
                        <span>問い合わせ</span>
                        <span className={CustomStyle.badge}>{Object.keys(inquiries).length}</span>
                    </button>

                    <button className={`${Styles.btn_baise_style} ${CustomStyle.button} ${ activeListId == 1? "": Styles.btn_gry}`}
                        onClick={evt => {
                            evt.preventDefault();
                            evt.stopPropagation();
                            this.changeListActiveTab(1);
                        }} id="consultationTab">
                        <span>事前相談</span>
                        <span className={CustomStyle.badge}>{Object.keys(answerConsultationList).length}</span>
                    </button>
                    <button className={`${Styles.btn_baise_style} ${CustomStyle.button} ${ activeListId == 2? "": Styles.btn_gry}`}
                        onClick={evt => {
                            evt.preventDefault();
                            evt.stopPropagation();
                            this.changeListActiveTab(2);
                        }} id="discussionsTab">
                        <span>事前協議</span>
                        <span className={CustomStyle.badge}>{Object.keys(answerDiscussionsList).length}</span>
                    </button>
                    <button className={`${Styles.btn_baise_style} ${CustomStyle.button} ${ activeListId == 3? "": Styles.btn_gry}`}
                        onClick={evt => {
                            evt.preventDefault();
                            evt.stopPropagation();
                            this.changeListActiveTab(3);
                        }} id="permissionJudgmentTab">
                        <span>許可判定</span>
                        <span className={CustomStyle.badge}>{Object.keys(answerPermissionJudgmentList).length}</span>
                    </button>
                </Box>

                <div>
                    <If condition = {this.props.viewState.showInquiryList}>
                        <div className={Styles.component_border} style={{height: "30vh", overflowY: "auto", marginBottom:"0"}}>
                            <InquiryList terria={this.props.terria} viewState={this.props.viewState} referrer={referrer} inquiries={inquiries} callback={this.showLotNumberLayers} /> 
                        </div>
                    </If>
                    <If condition = {this.props.viewState.showAnswerList}>
                        <div className={Styles.component_border}  style={{height: "30vh", overflowY: "auto", marginBottom:"0"}}>
                            <AnswerList terria={this.props.terria} viewState={this.props.viewState} referrer={referrer} answers={answers} callback={this.showLotNumberLayers} /> 
                        </div>
                    </If>
                </div>
            </>
        );
    }

}

export default withTranslation()(withTheme(NotificationList));