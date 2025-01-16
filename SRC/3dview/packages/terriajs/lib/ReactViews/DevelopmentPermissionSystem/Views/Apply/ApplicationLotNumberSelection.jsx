import { observer } from "mobx-react";
import PropTypes from "prop-types";
import React from "react";
import { withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import Box from "../../../../Styled/Box";
import CustomStyle from "./scss/application-lotNumber-selection.scss";
import LotNumberSearch from "../LotNumberSearch/LotNumberSearch.jsx";;
import LotNumberSelect from "../LotNumberSearch/LotNumberSelect.jsx";
import webMapServiceCatalogItem from '../../../../Models/Catalog/Ows/WebMapServiceCatalogItem';
import Config from "../../../../../customconfig.json";
import CommonStrata from "../../../../Models/Definition/CommonStrata";
import CzmlCatalogItem from '../../../../Models/Catalog/CatalogItems/CzmlCatalogItem';
import { BaseModel } from "../../../../Models/Definition/Model";

/**
 * トップ画面の申請地番選択コンポーネント
 */
@observer
class ApplicationLotNumberSelection extends React.Component {
    static displayName = "ApplicationLotNumberSelection";

    static propTypes = {
        terria: PropTypes.object.isRequired,
        viewState: PropTypes.object.isRequired
    }

    constructor(props) {
        super(props);
        this.state = {
            viewState: props.viewState,
            terria: props.terria,
            // 当前選択されたラジオ
            currentLotCheckModel:0,
            // 地番選択エリアの高さ
            height:"auto",
            // 相談開始ボタンラベル
            viewLabel:""
        };
    }

    /**
     * 初期処理
     */
    componentDidMount() {
        var div = document.getElementById("tooltips");
        if(this.props.viewState.islotNumberSearch){
            this.setState({currentLotCheckModel:"0"});
            div.style.display = "none";
            this.clickLotSearchModel();
        }else{
            this.setState({currentLotCheckModel:"1"});
            div.style.display = "block";
            this.clickMapSelectionModel();
        }
        this.getWindowSize("");
        // ラベル取得
        this.getViewLabel();
    }

    /**
     * DBからラベル取得
     */
    getViewLabel(){
        //サーバからlabelを取得
        fetch(Config.config.apiUrl + "/label/2000")
        .then(res => res.json())
        .then(res => {
            if(res.status === 401){
                alert("認証情報が無効です。ページの再読み込みを行います。");
                window.location.reload();
                return null;
            }
            if (Object.keys(res).length > 0) {
                let message = res[0]?.labels?.startConsult;
                this.setState({ viewLabel: message });
            }else{
                alert("labelの取得に失敗しました。");
            }
        }).catch(error => {
            console.error('通信処理に失敗しました', error);
            alert('通信処理に失敗しました');
        });
    }

    /**
     * 申請地番選択エリアの高さ再計算
     */
    getWindowSize(currentLotCheckModel) {
        if(this.props.viewState.showInitAndLotNumberSearchView){
            let win = window;
            let e = window.document.documentElement;
            let g = window.document.documentElement.getElementsByTagName('body')[0];
            let h = win.innerHeight|| e.clientHeight|| g.clientHeight;
    
            const getRect = document.getElementById("LotNumberSelectionArea");
            let height = h - getRect.getBoundingClientRect().top - 75;
            if(currentLotCheckModel == "1"){
                height = height - 20;
            }
            if(currentLotCheckModel == "0"){
                height = height + 20;
            }
            this.setState({height: height+"px"});
        }
    }

    /**
     * ツールチップ表示
     */
    tooltips() {
        var div = document.getElementById("tooltips");
        div.style.display = "block";
    }

    /**
     * ツールチップ非表示
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
     * 選択された地番を申請中地番レイヤに更新
     */
    updateMapLayer(){
        try {
            // chino:選択中地番 レイヤ
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

    /**
     *　地図上で、自由図形で地番選択したら、選択された地番の情報を取得する
     * @returns 選択された地番リスト
     */
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
            .then(res => res.json())
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
                }else if(res.status === 401){
                    alert("認証情報が無効です。ページの再読み込みを行います。");
                    window.location.reload();
                }
            }).catch(error => {
                console.error('処理に失敗しました', error);
            });
        }
    }

    /**
     * 「地図選択」ラジオを選択するイベント
     */
    clickMapSelectionModel(){
        // 地番検索無効になる
        this.props.viewState.setIslotNumberSearch(false);
        // 地番選択結果一覧を表示にする
        this.props.viewState.changeShowLotNumberSelectedFlg(true);
        // ツールチップを表示する
        this.tooltips();
        // クリックモード設定
        this.props.terria.setClickMode("1");;

        try {
            //筆界layer（全地番）を表示
            const item = new webMapServiceCatalogItem(Config.layer.lotnumberSearchLayerNameForAll, this.state.terria);
            const wmsUrl = Config.config.geoserverUrl;
            const items = this.state.terria.workbench.items;
            for (const aItem of items) {
                if (aItem.uniqueId === Config.layer.lotnumberSearchLayerNameForAll || aItem.uniqueId === Config.landMark.id) {
                    this.state.terria.workbench.remove(aItem);
                }
                // 地番検索結果（事業者）レイヤを非表示になる
                if(aItem.uniqueId === Config.layer.lotnumberSearchLayerNameForBusiness){
                    aItem.setTrait(CommonStrata.user,"opacity", 0);
                    aItem.loadMapItems();
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
            this.changeCesium3DTilesShow(false);
        } catch (error) {
            console.error('処理に失敗しました', error);
        }

    }

    /**
     * 建物モデル表示・非表示
     * @param {*} isShow 表示かどうか
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
            const leaflet2DModel = this.state.terria.getModelById(BaseModel, Config.buildingModelFor2d.id);
            leaflet2DModel.setTrait(
                CommonStrata.user,
                "show",
                isShow);
            leaflet2DModel.loadMapItems();
        } catch (error) {
            console.error('処理に失敗しました', error);
        }
    }

    /**
     * 「地図検索（地番）」ラジオを選択するイベント
     */
    clickLotSearchModel(){
        // 申請地番が未選択場合、
        if(Object.keys(this.props.viewState.applicationPlace).length < 1){
            // 地番選択結果一覧を非表示にする
            this.props.viewState.changeShowLotNumberSelectedFlg(false);
        }
        this.props.viewState.setIslotNumberSearch(true);

        this.clearTooltips();

        // 地番検索結果（事業者）レイヤを表示にする
        const items = this.state.terria.workbench.items;
        for (const aItem of items) {
            if(aItem.uniqueId === Config.layer.lotnumberSearchLayerNameForBusiness){
                aItem.setTrait(CommonStrata.user,"opacity", 1);
                aItem.loadMapItems();
            }
        }

        //3D建物モデルを表示
        this.changeCesium3DTilesShow(true);

    }

    /**
     * 申請区分選択画面へ遷移
     * @returns 
     */
    moveToApplicationView(){

        // 申請地0件チェック
        if (Object.keys(this.props.viewState.applicationPlace).length < 1) {
            alert("申請地は一つ以上必要です");
            return false;
        }

        //3D建物モデルを表示
        this.changeCesium3DTilesShow(true);
        this.clearTooltips();
        this.clearlayer();
        this.clearSamplePoint();
        this.props.viewState.moveToInputApplyConditionView();
    }

    /**
     * 指定したレイヤをクリアする
     * 指定しない場合、下記を全てクリアする
     *   選択中地番、全地番、地番検索結果（事業者）、ランドマーク、全筆かからない筆
     * @param {string} layerName レイヤ名
     */
    clearlayer(layerName = ""){
        const items = this.state.terria.workbench.items;
        for (const aItem of items) {
            if(layerName == "" ){
                if (aItem.uniqueId === Config.layer.lotnumberSearchLayerNameForSelected 
                    || aItem.uniqueId === Config.layer.lotnumberSearchLayerNameForAll 
                    || aItem.uniqueId === Config.layer.lotnumberSearchLayerNameForBusiness
                    || aItem.uniqueId === Config.landMark.id
                    || aItem.uniqueId === Config.layer.lotnumberSearchLayerNameForSelectedFullFlag) {
                    this.state.terria.workbench.remove(aItem);
                    aItem.loadMapItems();
                }
            }else{
                if (aItem.uniqueId === layerName) {
                    this.state.terria.workbench.remove(aItem);
                    aItem.loadMapItems();
                } 
            }
            
        }
    }

    /**
     * 前画面へ戻る
     */
    back(){
        this.clearlayer();
        this.clearTooltips();
        this.clearSamplePoint();
        this.props.viewState.initApplicationLotNumberSelectionView();
    }

    /**
     * 地図検索（地番）と地図選択を切り替え
     * @param {*} event イベント
     */
    changeLotCheckModel(event){
        let currentLotCheckModel = event.target.value;
        this.setState({currentLotCheckModel:currentLotCheckModel});
        if(currentLotCheckModel == "0"){
            if(!this.props.terria.authorityJudgment()){
                this.props.terria.setClickMode("");
            }
            this.clickLotSearchModel();
        }else{
            if(!this.props.terria.authorityJudgment()){
                this.props.terria.setClickMode("1");
            }
            this.clickMapSelectionModel();
            this.clearSamplePoint();
        }
        // リサイズ
        this.getWindowSize(currentLotCheckModel);
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

    render() {
        let height = this.state.height;
        if(!this.props.viewState.showLotNumberSelected){
            height = "auto";
        }
        const viewLabel = this.state.viewLabel ? this.state.viewLabel: "事前相談";
        return (
            <>
                <div>
                    <div className={CustomStyle.div_area}>
                        <div className={CustomStyle.applyType_div_area}>
                            <div css={`width:40%`}>
                                <label className={CustomStyle.radio_label}>
                                    <input
                                        id="lotSearchBtn"
                                        className={CustomStyle.radio_input}
                                        type="radio"
                                        value="0"
                                        onChange={e => this.changeLotCheckModel(e)}
                                        checked={this.props.viewState.islotNumberSearch}
                                    />
                                    <span className={CustomStyle.custom_radio}/>
                                    {"：地図検索（地番）"}
                                </label>
                            </div>
                            <div css={`width:40%`}>
                                <label className={CustomStyle.radio_label}>
                                    <input
                                        id="mapBtn"
                                        className={CustomStyle.radio_input}
                                        type="radio"
                                        value="1"
                                        onChange={e => this.changeLotCheckModel(e)}
                                        checked={!this.props.viewState.islotNumberSearch}
                                    />
                                    <span className={CustomStyle.custom_radio} />
                                    {"：地図選択"}
                                </label>
                            </div>
                        </div>
                    </div>
                    <button style={{display:"none"}} className={CustomStyle.btn_baise_style} onClick={() => {
                            this.drawingMapSelection();
                        }} id="freeBtn">自由形選択取得</button>
                </div>

                <Box id="LotNumberSelectionArea" overflowY={"auto"} styledHeight={height}  css={`display:block; overflow-x:hidden `} >
                    <If condition = {this.props.viewState.showLotNumberSearch}>
                        <div className={CustomStyle.component_border} style={{height:"400px"}}>
                            <div id="customVisible" className={CustomStyle.customloaderParent} 
                                style={{ visibility: this.props.viewState.islotNumberSearch ? "hidden" : "visible" }}>
                                
                            </div>
                            <LotNumberSearch terria={this.props.terria} viewState={this.state.viewState}/> 
                        </div>
                    </If>
                    <If condition = {this.props.viewState.showLotNumberSelected}>
                        <div className={CustomStyle.component_border}>
                            <LotNumberSelect terria={this.props.terria} viewState={this.props.viewState} />
                        </div>
                    </If>
                </Box>
                <If condition = {this.props.viewState.showLotNumberSelected}>
                    <div className={CustomStyle.div_area} >
                        <Box padded paddedHorizontally={3} paddedVertically={2} css={`display:block; text-align:center`} >
                            <button className={`${CustomStyle.btn_baise_style} ${CustomStyle.btn_apply}`}
                                disabled={Object.keys(this.props.viewState.applicationPlace).length < 1}
                                style={{width:"45%"}}
                                onClick={evt => {
                                    evt.preventDefault();
                                    evt.stopPropagation();
                                    this.moveToApplicationView();
                                }}>
                                <span>{viewLabel}</span>
                            </button>
                            
                            <button className={`${CustomStyle.btn_gry} ${CustomStyle.btn_baise_style} `}
                                style={{width:"45%"}}
                                onClick={evt => {
                                    evt.preventDefault();
                                    evt.stopPropagation();
                                    this.back();
                                }}>
                                <span>戻る</span>
                            </button>
                        </Box>
                    </div>
                </If>
            </>
        );
    }
}
export default withTranslation()(withTheme(ApplicationLotNumberSelection));