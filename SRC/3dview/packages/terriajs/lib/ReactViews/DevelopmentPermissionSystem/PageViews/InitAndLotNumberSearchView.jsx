import { observer } from "mobx-react";
import PropTypes from "prop-types";
import React from "react";
import { withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import { TextSpan } from "../../../Styled/Text";
import Box from "../../../Styled/Box";
import Styles from "./scss/pageStyle.scss";
import Config from "../../../../customconfig.json";
import AnswerLogin from "../Views/Answer/AnswerLogin.jsx";
import ApplicationLotNumberSelection from "../Views/Apply/ApplicationLotNumberSelection.jsx"
import LayerTab from "../Views/layer/LayerTab.jsx";
import webMapServiceCatalogItem from '../../../Models/Catalog/Ows/WebMapServiceCatalogItem';
import CommonStrata from "../../../Models/Definition/CommonStrata";
import { BaseModel } from "../../../Models/Definition/Model";

/**
 * 事業者：トップ画面
 */
@observer
class InitAndLotNumberSearchView extends React.Component {
    static displayName = "InitAndLotNumberSearchView";
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
            activeTab: "lotSearchTab",
            height: 800,
            // ツールチップ表示エリアの幅
            width:0,
            // ツールチップ表示エリアの位置
            positionLeft:0,
        };
    }
    
    /**
     * 初期処理
     */
    componentDidMount() {
        this.getWindowSize();
    }
    
    /**
     * リサイズのために、高さ計算
     */
    getWindowSize() {
        if(this.props.viewState.showInitAndLotNumberSearchView){
            let win = window;
            let e = window.document.documentElement;
            let g = window.document.documentElement.getElementsByTagName('body')[0];
            let h = win.innerHeight|| e.clientHeight|| g.clientHeight;
            let w = win.innerWidth|| e.clientWidth|| g.clientWidth;
            const getRect = document.getElementById("SearchLotNumberArea");
            if(getRect){
                let height = h - getRect.getBoundingClientRect().top;
                const sidePanel = document.getElementById("SidePanel");
                let width = w - sidePanel.clientWidth -30;
                this.setState({height: height, width: width, positionLeft: sidePanel.clientWidth});
            }
        }
    }

    /**
     * リサイズ
     */
    componentWillMount () {
        window.addEventListener('resize', () => {
                this.getWindowSize() 
        })
    }

    /**
     * ツールチップ表示とする
     */
    tooltips() {
        var div = document.getElementById("tooltips");
        div.style.display = "block";
    }

    /**
     * ツールチップ非表示とする
     */
    clearTooltips(){
        var div = document.getElementById("tooltips");
        div.style.display = "none";
        // 全地番レイヤ（黒枠）をクリアする
        this.clearlayer(Config.layer.lotnumberSearchLayerNameForAll);
        // ランドマークをクリアする
        this.clearlayer(Config.landMark.id);
    }

    /**
     * 指定したレイヤをクリアする
     * @param {string} layerName レイヤ
     */
    clearlayer(layerName){
        const items = this.state.terria.workbench.items;
        for (const aItem of items) {
            if (aItem.uniqueId === layerName) {
                this.state.terria.workbench.remove(aItem);
                aItem.loadMapItems();
            } 
        }
    }

    /**
     * 地図選択モードで、地図上で地番をクリックしたら、地番取得を行う
     * @returns 選択した申請地番
     */
    clickMapSelection(){
        // 初期画面以外または、地図検索ではない場合、処理中止
        if(this.props.viewState.islotNumberSearch || !this.props.viewState.showInitAndLotNumberSearchView){
            return false;
        }
        // 事業者（ユーザー権限＝事業者、clickモード＝1：申請地選択（事業者））の場合
        if(this.state.terria.clickMode === "1" && !this.state.terria.authorityJudgment()){
            fetch(Config.config.apiUrl + "/lotnumber/getFromLonlat/establishment", {
                method: 'POST',
                body: JSON.stringify({
                    latiude: this.props.terria.lat,
                    longitude:this.props.terria.lon
                }),
                headers: new Headers({ 'Content-type': 'application/json' }),
            })
            .then(res => res.json())
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

    /**
     * 検索・レイヤタブを切り替え
     * @param {*} activeTab アクティブタブ
     */
    clickTab(activeTab){
        this.setState({activeTab:activeTab});

        if(activeTab == "layerTab"){
            this.clearTooltips();
            this.clearSamplePoint();
            //3D建物モデルを表示
            this.changeCesium3DTilesShow(true);
        }else{
            if(!this.props.viewState.islotNumberSearch){
                this.tooltips();
            }else{
                //3D建物モデルを表示
        this.changeCesium3DTilesShow(true);
            }
        }

        this.getWindowSize();
    }

    /**
     * 3D建物モデル表示・非表示
     * @param {*} isShow 表示するかどうか
     */
    changeCesium3DTilesShow(isShow){
        try{
            const cesium3DTiles = this.state.terria.getModelById(BaseModel, Config.buildingModel.id);
            cesium3DTiles.traits.style.show = isShow;
            cesium3DTiles.setTrait(
                CommonStrata.user,
                "style",
                {"show": isShow});
            cesium3DTiles.loadMapItems();
        } catch (error) {
            console.error('処理に失敗しました', error);
        }
    }

    /**
     * ピンマークの削除
     */
    clearSamplePoint() {
        const items = this.state.terria.workbench.items;
        for (const aItem of items) {
            if (aItem.uniqueId === '対象地点') {
                this.state.terria.workbench.remove(aItem);
                aItem.loadMapItems();
            }
        }
    }
    
    /**
     * 選択された地番を申請中地番レイヤに更新
     */
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

    render() {
        const t = this.props.t;
        const activeTab = this.state.activeTab;
        let tabAreaHeight = this.state.height;
        if(!this.props.viewState.showLotNumberSelected){
            tabAreaHeight = tabAreaHeight -330;
        }
        let infoMessage = "";
        if(activeTab == "lotSearchTab"){
            if(this.props.viewState.islotNumberSearch){
                infoMessage = t("infoMessage.tipsForLotNumberSearch")
            }else{
                infoMessage = t("infoMessage.tipsForSelectLotNumberAtMap")
            }
        }
        if(activeTab == "layerTab"){
            infoMessage = t("infoMessage.tipsForLayerTab")
        }
       
        return (
            <>
                <Box column fullHeight>
                    <div style={{ position: "absolute", left: -99999 + "px" }} id="clickMapSelection" onClick={evt => {
                        this.clickMapSelection();
                    }}></div>
                    <div>
                        <Box padded  className={Styles.text_area}>
                            <TextSpan textDark uppercase overflowHide overflowEllipsis>
                                {infoMessage} 
                            </TextSpan>
                        </Box>
                        <div className={Styles.tab_box}>
                            <button className={`${Styles.tab_btn} ${activeTab == "lotSearchTab" ? "" : Styles.tab_btn_gry}`}
                                onClick={evt => {
                                    evt.preventDefault();
                                    evt.stopPropagation();
                                    this.clickTab("lotSearchTab");
                                }} id="lotSearchTab">
                                <span>検索</span>
                            </button>
                            <button className={`${Styles.tab_btn} ${activeTab == "lotSearchTab" ? Styles.tab_btn_gry: ""}`}
                                onClick={evt => {
                                    evt.preventDefault();
                                    evt.stopPropagation();
                                    this.clickTab("layerTab");
                                }} id="layerTab">
                                <span>レイヤ</span>
                            </button>
                        </div>
                    </div>
                    <div className={Styles.scrollContainer} >
                        <div className={Styles.div_area} id="SearchLotNumberArea" style={{height: "auto"} } >
                            {/* 検索TAB */}
                            <If condition = {activeTab == "lotSearchTab"}>
                                <div id="tabArea" style={{height: "auto"}}
                                    // style={{height: this.props.viewState.showLotNumberSelected ? "auto" : tabAreaHeight + "px"}}
                                >
                                    <ApplicationLotNumberSelection terria={this.props.terria} viewState={this.state.viewState}/>
                                </div>
                            </If>
                            {/* レイヤTAB */}
                            <If condition = {activeTab == "layerTab"}>
                                {/* <div id="tabArea" style={{height: tabAreaHeight-10 + "px"}}> */}
                                <div id="tabArea" style={{height: "auto"}}>
                                    <LayerTab terria={this.props.terria} viewState={this.state.viewState}  tabAreaHeight = {tabAreaHeight} />
                                </div>
                            </If>
                        </div>

                        {/* 回答確認ログインエリア */}
                        <If condition = {!this.props.viewState.showLotNumberSelected}>
                            <div className={Styles.div_area} style={{marginBottom:10 + "px"}}>
                                <AnswerLogin terria={this.props.terria} viewState={this.props.viewState} t={t}></AnswerLogin>
                            
                                {/* <Box paddedRatio={2}></Box> */}
                            </div>
                        </If>
                    </div>
                </Box>
                <div id="tooltips" className={Styles.descriptionDiv}>
                    <div className={Styles.description}>
                        <div className={Styles.descriptionInner}>
                            <p>
                                左クリック = クリック地点の地番を取得<br/>
                                Alt + 左クリック = 自由形選択開始<br/>
                                Alt + ドラッグ = 選択範囲描画<br/>
                                Alt + ドロップ= 選択範囲の地番取得<br/>
                                ※選択範囲に完全に含む地番を取得します
                            </p>
                        </div>
                    </div>
                </div>
            </>
        );
    };
}

export default withTranslation()(withTheme(InitAndLotNumberSearchView));