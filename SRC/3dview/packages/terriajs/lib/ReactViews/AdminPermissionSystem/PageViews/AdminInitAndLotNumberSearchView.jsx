import React from "react";
import PropTypes from "prop-types";
import { observer } from "mobx-react";
import { withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import Box from "../../../Styled/Box";
import { TextSpan } from "../../../Styled/Text";
import Styles from "../../DevelopmentPermissionSystem/PageViews/scss/pageStyle.scss";
import CustomStyle from "./scss/custumStyle.scss";
import DataCatalog from "../../DataCatalog/DataCatalog";
import AdminTab from "../Views/Tab/AdminTab";
import ShowMessage from "../Views/Message/ShowMessage";
import LotNumberSearch from "../../DevelopmentPermissionSystem/Views/LotNumberSearch/LotNumberSearch";
import InquiryList from "../Views/Apply/InquiryList";
import AnswerList from "../Views/Apply/AnswerList";
import Config from "../../../../customconfig.json";

/**
 * 行政用画面：地図検索
 */
@observer
class AdminInitAndLotNumberSearchView extends React.Component {
    static displayName = "AdminInitAndLotNumberSearchView"
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
            // 自分担当課に対する問い合わせ一覧
            inquiries: [],
            // 自分担当課に対する回答（申請）一覧 
            answers: [],
            intervalID: null
        }
    }

    //ツールチップ
    tooltips() {
        document.getElementsByTagName("canvas")[0].onmousemove = mouseMove;
        document.getElementsByTagName("canvas")[0].onmouseout = mouseOut;
        function mouseMove(e) {
            var event = e ? e : window.event;
            var div = document.getElementById("tooltips");
            div.style.left = event.clientX+10 + "px";
            div.style.top = event.clientY-70 + "px";
            div.style.display = "block";
        }
        function mouseOut(e) {
            var event = e ? e : window.event;
            var div = document.getElementById("tooltips");
            div.style.display = "none";
        }
    }

        //ツールチップクリア
        clearTooltips(){
        document.getElementsByTagName("canvas")[0].onmousemove = null;
        document.getElementsByTagName("canvas")[0].onmouseout = null;
        this.props.terria.userDrawing?.endDrawing();
        this.props.terria.initActiveShapeLonLat();
        this.props.terria.initActiveShapePoints();
    }

    //地図選択の取得処理
    clickMapSelection(){
            // 初期画面以外または、地図検索ではない場合、処理中止
            if(this.props.viewState.showLotNumberSearch || !this.props.viewState.showInitAndLotNumberSearchView){
            return false;
        }
        if(this.state.terria.clickMode === "1" && !this.state.terria.authorityJudgment()){
            fetch(Config.config.apiUrl + "/lotnumber/getFromLonlat/establishment", {
                method: 'POST',
                body: JSON.stringify({
                    latiude: this.props.terria.lat,
                    longitude:this.props.terria.lon
                }),
                headers: new Headers({ 'Content-type': 'application/json' }),
            })
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
                if(Object.keys(res).length > 0 && !res.status){
                    //選択申請地対象を初期化
                    this.props.viewState.removeLosNumberSelect();

                    Object.keys(res).map((key) => {
                        if(!this.props.viewState.deleteApplicationPlaceByChiban(res[key]))
                            this.props.viewState.switchLotNumberSelect(res[key]); 
                    })
                    this.props.viewState.changeApplicationPlace();
                    this.updateMapLayer();
                }else{
                    alert('地点の取得に失敗しました');  
                }
            }).catch(error => {
                console.error('処理に失敗しました', error);
            });
        }
    }

    //選択状態の申請地layerを更新
    updateMapLayer(){
        try {
            const item = new webMapServiceCatalogItem(Config.layer.lotnumberSearchLayerNameForSelected, this.state.terria);
            const wmsUrl = Config.config.geoserverUrl;
            const items = this.state.terria.workbench.items;
            let initFlg = true;
            for (const aItem of items) {
                if (aItem.uniqueId === Config.layer.lotnumberSearchLayerNameForSelected && Object.keys(this.props.viewState.applicationPlace).length > 0) {
                    aItem.setTrait(CommonStrata.user,
                    "parameters",
                    {
                        "viewparams": Config.layer.lotnumberSearchViewParamNameForSelected + Object.keys(this.props.viewState.applicationPlace)?.map(key => { return this.props.viewState.applicationPlace[key].chibanId }).filter(chibanId => {return chibanId !== null}).join("_"),
                    });
                    aItem.loadMapItems();
                    initFlg = false;
                }else if(aItem.uniqueId === Config.layer.lotnumberSearchLayerNameForSelected){
                    this.state.terria.workbench.remove(aItem);
                }
            }
            if(initFlg && Object.keys(this.props.viewState.applicationPlace).length > 0){
                item.setTrait(CommonStrata.definition, "url", wmsUrl);
                item.setTrait(CommonStrata.user, "name", Config.layer.lotnumberSearchLayerDisplayNameForSelected);
                item.setTrait(
                    CommonStrata.user,
                    "layers",
                    Config.layer.lotnumberSearchLayerNameForSelected);
                item.setTrait(CommonStrata.user,
                    "parameters",
                    {
                        "viewparams": Config.layer.lotnumberSearchViewParamNameForSelected + Object.keys(this.props.viewState.applicationPlace)?.map(key => { return this.props.viewState.applicationPlace[key].chibanId }).filter(chibanId => {return chibanId !== null}).join("_"),
                    });
                item.loadMapItems();
                this.state.terria.workbench.add(item);
            }
        } catch (error) {
            console.error('処理に失敗しました', error);
        }
    }

    //自由選択の取得処理
    drawingMapSelection(){
        // 初期画面以外または、地図検索ではない場合、処理中止
        if(this.props.viewState.islotNumberSearch || !this.props.viewState.showInitAndLotNumberSearchView){
            return false;
        }
        this.state.terria.setActiveShapeLonLat(this.state.terria.activeShapeLonLat[0]);
        if(this.state.terria.activeShapeLonLat.length < 3){
            return false;
        }
        if(this.state.terria.clickMode === "1" && !this.state.terria.authorityJudgment()){
            fetch(Config.config.apiUrl + "/lotnumber/getFromFigure/establishment", {
                method: 'POST',
                body: JSON.stringify({
                    coodinates: this.state.terria.activeShapeLonLat
                }),
                headers: new Headers({ 'Content-type': 'application/json' }),
            })
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
                if(Object.keys(res).length > 0 && !res.status){
                    //選択申請地対象を初期化
                    this.props.viewState.removeLosNumberSelect();

                    Object.keys(res).map((key) => {
                        this.props.viewState.switchLotNumberSelect(res[key]); 
                    })
                    this.props.viewState.changeApplicationPlace();
                    this.updateMapLayer();
                    this.props.terria.userDrawing?.endDrawing();
                    this.props.terria.initActiveShapeLonLat();
                    this.props.terria.initActiveShapePoints();
                }else if(res.status === 406){
                    alert('選択範囲には500件以上含まれるため取得できません。\n選択範囲を限定して操作をやり直してください。');
                }
            }).catch(error => {
                console.error('処理に失敗しました', error);
            });
        }
    }

    // 地図選択をクリック
    clickMapSelectionModel(){
        // 地番検索無効になる
        this.props.viewState.setIslotNumberSearch(false);
        // 地番選択結果一覧を表示にする
        this.props.viewState.changeShowLotNumberSelectedFlg(true);
        // ツールチップを表示する
        this.tooltips();
        // マップモード設定
        this.props.viewState.hideCharacterSelectionView();

        try {
            //筆界layerを表示
            const item = new webMapServiceCatalogItem(Config.layer.lotnumberSearchLayerNameForAll, this.state.terria);
            const wmsUrl = Config.config.geoserverUrl;
            const items = this.state.terria.workbench.items;
            for (const aItem of items) {
                if (aItem.uniqueId === Config.layer.lotnumberSearchLayerNameForAll || aItem.uniqueId === Config.landMark.id) {
                    this.state.terria.workbench.remove(aItem);
                }
            }
            item.setTrait(CommonStrata.definition, "url", wmsUrl);
            item.setTrait(CommonStrata.user, "name", Config.layer.lotnumberSearchLayerDisplayNameForAll);
            item.setTrait(
                CommonStrata.user,
                "layers",
                Config.layer.lotnumberSearchLayerNameForAll);
            item.setTrait(CommonStrata.user,
                "parameters",
                {});
            item.loadMapItems();
            this.state.terria.workbench.add(item);

            //landmark layerを表示
            const landmarkUrl = Config.config.landmarkUrl;
            const catalogItem = new CzmlCatalogItem(Config.landMark.id, this.state.terria)
            catalogItem.setTrait(CommonStrata.definition, "url", landmarkUrl);
            catalogItem.setTrait(CommonStrata.user, "name", Config.landMark.displayName);
            catalogItem.loadMapItems();
            this.state.terria.workbench.add(catalogItem);

            //3D建物モデルを非表示
            const cesium3DTiles = this.state.terria.getModelById(BaseModel, Config.buildingModel.id);
            cesium3DTiles.traits.style.show = false;
            cesium3DTiles.setTrait(
                CommonStrata.user,
                "style",
                {"show": false});
            cesium3DTiles.loadMapItems();
        } catch (error) {
            console.error('処理に失敗しました', error);
        }

    }


    // 地図検索選択をクリック
    clickLotSearchModel(){
        this.props.viewState.setshowLotNumberSearch(true);
        this.props.viewState.setshowAppInfoSearch(false);
        this.props.viewState.setshowLotLayerSelected(false);

        try{
            //3D建物モデルを表示
            const cesium3DTiles = this.state.terria.getModelById(BaseModel, Config.buildingModel.id);
            cesium3DTiles.traits.style.show = false;
            cesium3DTiles.setTrait(
                CommonStrata.user,
                "style",
                {"show": true});
            cesium3DTiles.loadMapItems();
        }catch(error){
            console.error('処理に失敗しました', error);
        }

        this.clearTooltips();
        
    }

    /**
     * 初期表示
     */
    componentDidMount() {

        // 担当課の問合せ・回答一覧取得
        this.getResponsibleInquiries();  

        // 30秒につき、問い合わせ内容をリフレッシュする
        let intervalID = setInterval(() => {
            if(this.props.viewState.adminTabActive === "mapSearch"){
                
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
             this.setState({inquiries: res.inquiries, answers:res.answers});
            }else{
                alert('担当課の問合せ・回答一覧取得に失敗しました');
            }
        }).catch(error => {
            console.error('通信処理に失敗しました', error);
            alert('通信処理に失敗しました');
        });
    }

    //申請条件入力画面へ遷移
    moveToApplicationView(){
        if (Object.keys(this.props.viewState.applicationPlace).length < 1) {
            alert("申請地は一つ以上必要です");
            return false;
        }
        const items = this.state.terria.workbench.items;
        for (const aItem of items) {
            if (aItem.uniqueId === Config.layer.lotnumberSearchLayerNameForSelected || aItem.uniqueId === Config.layer.lotnumberSearchLayerNameForAll) {
                this.state.terria.workbench.remove(aItem);
                aItem.loadMapItems();
            }
        }
        try{
            //3D建物モデルを表示
            const cesium3DTiles = this.state.terria.getModelById(BaseModel, Config.buildingModel.id);
            cesium3DTiles.traits.style.show = false;
            cesium3DTiles.setTrait(
                CommonStrata.user,
                "style",
                {"show": true});
            cesium3DTiles.loadMapItems();
        }catch(error){
            console.error('処理に失敗しました', error);
        }

        this.clearTooltips();
        this.props.viewState.changeShowInitAndLotNumberSearchViewFlg(false);
        this.props.viewState.changeShowInputApplyConditionViewFlg(true);
    }

    // アクティブタブの変更
    changeActiveTab(active){
        this.state.viewState.changeAdminTabActive(active);
    }

    // 問合せタブをクリック
    clickInquiryListModel(){
        this.props.viewState.changeShowInquiryList(true);
        this.props.viewState.changeShowAnswerList(false);
        console.log("問合せ");
    }

    // 回答タブをクリック
    clickAnswerListModel(){
        this.props.viewState.changeShowInquiryList(false);
        this.props.viewState.changeShowAnswerList(true);
        console.log("回答");
    }

    render(){
        const t = this.props.t;
        let inquiries = this.state.inquiries;
        let answers = this.state.answers;
        return(
            <>
            <div className={Styles.tooltips} id="tooltips">
                左クリック = クリック地点の地番を取得<br/>
                Alt + 左クリック = 自由形選択開始<br/>
                Alt + ドラッグ = 選択範囲描画<br/>
                Alt + ドロップ= 選択範囲の地番取得<br/>
                ※選択範囲に完全に含む地番を取得します
            </div>

            <Box column>

                <div style={{ position: "absolute", left: -99999 + "px" }} id="clickMapSelection" onClick={evt => {
                    this.clickMapSelection();
                }}></div>
                
                <div>
                    <ShowMessage t={t} message={"infoMessage.tipsForLotNumberSearch"} />
                    <AdminTab terria={this.props.terria} viewState={this.props.viewState} t={t}/>
                </div>

                <div style={{height: "calc(100vh - 160px)" ,overflowY: "auto"}}>

                    <div>
                        <div className={Styles.component_border}>
                            <LotNumberSearch terria={this.props.terria} viewState={this.props.viewState} /> 
                        </div>
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
                                <InquiryList terria={this.props.terria} viewState={this.props.viewState} referrer={"mapSearch"} inquiries={inquiries}/> 
                            </div>
                        </If>
                        <If condition = {this.props.viewState.showAnswerList}>
                            <div className={Styles.component_border}  style={{height: "30vh", overflowY: "auto", marginBottom:"0"}}>
                                <AnswerList terria={this.props.terria} viewState={this.props.viewState} referrer={"mapSearch"} answers={answers}/> 
                            </div>
                        </If>
                    </div>
                </div>

            </Box>
            </>
        );
    };
}
export default withTranslation()(withTheme(AdminInitAndLotNumberSearchView));