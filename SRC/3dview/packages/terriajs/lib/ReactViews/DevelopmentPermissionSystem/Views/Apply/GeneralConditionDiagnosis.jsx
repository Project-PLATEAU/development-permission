import { observer } from "mobx-react";
import PropTypes from "prop-types";
import React from "react";
import { withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import Box from "../../../../Styled/Box";
import CustomStyle from "./scss/general-condition-diagnosis.scss";
import CommonStrata from "../../../../Models/Definition/CommonStrata";
import webMapServiceCatalogItem from '../../../../Models/Catalog/Ows/WebMapServiceCatalogItem';
import WebFeatureServiceCatalogItem from "../../../../Models/Catalog/Ows/WebFeatureServiceCatalogItem";
import Cartographic from "terriajs-cesium/Source/Core/Cartographic";
import Ellipsoid from "terriajs-cesium/Source/Core/Ellipsoid";
import {
    getShareData
} from "../../../Map/Panels/SharePanel/BuildShareLink";
import Config from "../../../../../customconfig.json";
import sampleTerrainMostDetailed from "terriajs-cesium/Source/Core/sampleTerrainMostDetailed";
import { BaseModel } from "../../../../Models/Definition/Model";
import EventHelper from "terriajs-cesium/Source/Core/EventHelper";
/**
 * 概況診断結果表示画面
 */
@observer
class GeneralConditionDiagnosis extends React.Component {
    static displayName = "GeneralConditionDiagnosis";
    static propTypes = {
        terria: PropTypes.object.isRequired,
        viewState: PropTypes.object.isRequired,
        theme: PropTypes.object,
        t: PropTypes.func.isRequired
    }

    constructor(props) {
        super(props);
        this.state = {
            viewState: props.viewState,
            terria: props.terria,
            //概況診断結果
            generalConditionDiagnosisResult: [],
            //ボタン非活性フラグ
            disabledFlg: true,
            //概況診断結果レポート出力 キャプチャ一覧
            uploadForGeneralConditionDiagnosisForm:{},
            //概況診断結果レポート出力 完了キャプチャ一覧
            uploadForGeneralConditionDiagnosisCompleteForm:{},
            // 結果一覧の高さ
            height:0,
            // 各診断結果詳細表示エリアの幅
            width:0,
            // 各診断結果詳細表示エリアの位置
            positionLeft:0,
            // 診断結果詳細を表示されるか
            displayDescriptionFlg:false,
            // 表示される詳細に対する診断結果
            currentGeneralConditionDiagnosisResult:null,
            //　概況診断結果レポート出力 キャプチャモード判別フラグ
            captureMode:false,
        };
        //概況診断結果レポート出力 処理済みキャプチャ数
        this.capturedCount = 0;
        //概況診断結果レポート出力 処理対象キャプチャ数
        this.currentCaptureCount = 0;
        ///概況診断結果レポート出力 キャプチャ対象最大数
        this.captureMaxCount = 0;
        ///概況診断結果レポート出力 layerの対象index
        this.layerIndex = -1;
        ///概況診断結果レポート出力 対象index
        this.generalConditionDiagnosisResultIndex = -1
        //概況診断結果レポート出力 スクリーンショット完了率
        this.captureMaxPercent = 90;
        //概況診断結果レポート出力 スクリーンショット最大完了率
        this.captureAllMaxPercent = 90;

    }

    UNSAFE_componentWillMount() {
        this.eventHelper = new EventHelper();
    }

    /**
     * 結果一覧の高さ再計算
     */
    getWindowSize() {
        if(this.props.viewState.showGeneralAndRoadJudgementResultView){
            let win = window;
            let d = document;
            let e = window.document.documentElement;
            let g = window.document.documentElement.getElementsByTagName('body')[0];
            let h = win.innerHeight|| e.clientHeight|| g.clientHeight;
            let w = win.innerWidth|| e.clientWidth|| g.clientWidth;
            const getRect = document.getElementById("GeneralConditionDiagnosisDragTable");
            let height = h - getRect.getBoundingClientRect().top -80;
            const sidePanel = document.getElementById("SidePanel");
            let width = w - sidePanel.clientWidth -20;
            this.setState({height: height,width: width, positionLeft: sidePanel.clientWidth});
        }
    }

    /**
     * componentWillMountイベント
     */
    componentWillMount () {
        window.addEventListener('resize', () => {
                this.getWindowSize() 
        })
    }

    /**
     * 初期処理
     */ 
    componentDidMount() {
        document.getElementById("customloaderForGeneralConditionDiagnosisDrag").style.display = "block";
        //サーバからデータを取得
        let applicationPlace = Object.values(this.props.viewState.applicationPlace);
        applicationPlace = applicationPlace.filter(Boolean);
        let checkedApplicationCategory = Object.values(this.props.viewState.checkedApplicationCategory);
        checkedApplicationCategory = checkedApplicationCategory.filter(Boolean);
        let generalConditionDiagnosisResult = this.props.viewState.generalConditionDiagnosisResult;
        try{
            const item = new webMapServiceCatalogItem(Config.layer.lotnumberSearchLayerNameForApplicationTarget, this.state.terria);
            const wmsUrl = Config.config.geoserverUrl;
            const items = this.state.terria.workbench.items;
            for (const aItem of items) {
                if (aItem.uniqueId === Config.layer.lotnumberSearchLayerNameForApplicationTarget) {
                    this.state.terria.workbench.remove(aItem);
                    aItem.loadMapItems();
                }
            }
            item.setTrait(CommonStrata.definition, "url", wmsUrl);
            item.setTrait(CommonStrata.user, "name", Config.layer.lotnumberSearchLayerDisplayNameForApplicationTarget);
            item.setTrait(
                CommonStrata.user,
                "layers",
                Config.layer.lotnumberSearchLayerNameForApplicationTarget);
            item.setTrait(CommonStrata.user,
                "parameters",
                {
                    "viewparams": Config.layer.lotnumberSearchViewParamNameForApplicationTarget + Object.keys(this.props.viewState.applicationPlace)?.map(key => { return this.props.viewState.applicationPlace[key].chibanId }).filter(chibanId => { return chibanId !== null }).join("_"),
                });
            item.loadMapItems();
            this.state.terria.workbench.add(item);
            this.focusMapPlaceDriver();
        }catch(error){
            document.getElementById("customloaderForGeneralConditionDiagnosisDrag").style.display = "none";
            console.error('処理に失敗しました', error);
        }
        if(generalConditionDiagnosisResult && Object.keys(generalConditionDiagnosisResult).length > 0){
            let disabledFlg = this.state.disabledFlg;
            Object.keys(generalConditionDiagnosisResult).map(key => {
                if (generalConditionDiagnosisResult[key].result) {
                    disabledFlg = false;
                }
            });
            this.setState({ generalConditionDiagnosisResult: generalConditionDiagnosisResult, disabledFlg: disabledFlg });
            document.getElementById("customloaderForGeneralConditionDiagnosisDrag").style.display = "none";
        }else{
            fetch(Config.config.apiUrl + "/judgement/execute", {
                method: 'POST',
                body: JSON.stringify({
                    lotNumbers: applicationPlace,
                    applicationCategories: checkedApplicationCategory
                }),
                headers: new Headers({ 'Content-type': 'application/json' }),
            })
                .then(res => res.json())
                .then(res => {
                    if(res.status === 401){
                        alert("認証情報が無効です。ページの再読み込みを行います。");
                        window.location.reload();
                        return null;
                    }
                    let disabledFlg = this.state.disabledFlg;
                    if (Object.keys(res).length > 0 && !res.status) {
                        Object.keys(res).map(key => {
                            if (res[key].result) {
                                disabledFlg = false;
                            }
                        });
                        this.setState({ generalConditionDiagnosisResult: res, disabledFlg: disabledFlg });
                        document.getElementById("customloaderForGeneralConditionDiagnosisDrag").style.display = "none";
                    } else {
                        this.setState({ generalConditionDiagnosisResult: [], disabledFlg: true });
                        alert("概況診断に失敗しました。\n再度初めからやり直してください。");
                    }
                }).catch(error => {
                    this.setState({ generalConditionDiagnosisResult: [], disabledFlg: true });
                    document.getElementById("customloaderForGeneralConditionDiagnosisDrag").style.display = "none";
                    console.error('処理に失敗しました', error);
                    alert('処理に失敗しました');
                });
        }
        this.getWindowSize();
    }

    /**
     * コンポーネント更新のイベント
     */
    componentDidUpdate() {
        //概況診断レポート出力かつ処理完了の場合は出力処理呼び出し
        const uploadForGeneralConditionDiagnosisForm = this.state.uploadForGeneralConditionDiagnosisForm;
        const uploadForGeneralConditionDiagnosisCompleteForm = this.state.uploadForGeneralConditionDiagnosisCompleteForm;
        if(this.state.captureMode && 
            this.captureMaxCount === this.capturedCount && 
                Object.keys(uploadForGeneralConditionDiagnosisCompleteForm).length >= Object.keys(uploadForGeneralConditionDiagnosisForm).length){
            let completeFlg = true;
            Object.keys(uploadForGeneralConditionDiagnosisCompleteForm).map(secondKey => {
                if (!uploadForGeneralConditionDiagnosisCompleteForm[secondKey]["status"]) {
                    completeFlg=false;
                }
            })
            if(completeFlg){
                this.setState({captureMode:false});
                this.outputFile();
            }
        }
    }

    /**
     * キャプチャを作成し、概況診断結果レポートを出力
     */
    outputPreparation() {
        if (window.confirm("現在マップで表示されている領域に申請地が全て含まれるようにしてください。このままレポートの出力を開始しますか？")) {
            let myBar = document.getElementById("myBar");
            let wholePercent = document.getElementById("wholePercent");
            document.getElementById("loadingBg").style.display = "block";
            document.getElementById("loading").style.display = "block";
            //完了率をスクリーンショット最大完了率 - スクリーンショット完了率にしてレイヤ表示切替処理に遷移
            myBar.style.width = this.captureAllMaxPercent - this.captureMaxPercent + "%";
            wholePercent.innerHTML = this.captureAllMaxPercent - this.captureMaxPercent;
            try{
                this.showCaptureLayers();
            }catch(error){
                this.errorHandler(error);
            }
        }
    }

    /**
     * キャプチャを作成し、概況診断結果レポートを出力（申請登録時）
     */
    outputPreparationForConfirmApplicationDetails() {
        if (window.confirm("申請に必要なレポートの生成を行います。現在マップで表示されている領域に申請地が全て含まれるようにしてください。このまま申請処理を開始しますか？")) {
            let myBar = document.getElementById("myBar");
            let wholePercent = document.getElementById("wholePercent");
            document.getElementById("loadingBg").style.display = "block";
            document.getElementById("loading").style.display = "block";
            //完了率をスクリーンショット最大完了率 - スクリーンショット完了率にしてレイヤ表示切替処理に遷移
            myBar.style.width = this.captureAllMaxPercent - this.captureMaxPercent + "%";
            wholePercent.innerHTML = this.captureAllMaxPercent - this.captureMaxPercent;
            try{
                this.showCaptureLayers();
            }catch(error){
                this.errorHandler(error);
            }
        }
    }

    /**
     * フォーカス処理ドライバー
     */
    focusMapPlaceDriver() {
        let applicationPlace = this.props.viewState.applicationPlace;
        applicationPlace = Object.values(applicationPlace);
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
     * 結果行選択時に固有のフォーカスを行う
     * @param {*} maxLon 最大緯度
     * @param {*} maxLat 最大経度
     * @param {*} minLon 最小緯度
     * @param {*} minLat 最小経度
     */
    resultRowFocusMapPlace(maxLon, maxLat, minLon, minLat) {
        this.outputFocusMapPlaceWithTop(maxLon, maxLat, minLon, minLat, (maxLon + minLon) / 2, (maxLat + minLat) / 2);
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
        // 3dmodeにセット
        this.props.viewState.set3dMode();
        //現在のカメラ位置等を取得
        const currentSettings = getShareData(this.state.terria, this.props.viewState);
        const currentCamera = currentSettings.initSources[0].initialCamera;
        let newCamera = Object.assign(currentCamera);
        //新規の表示範囲を設定
        let currentLonDiff = Math.abs(maxLon - minLon);
        let currentLatDiff = Math.abs(maxLat - minLat);
        newCamera.north = maxLon + currentLatDiff / 2;
        newCamera.south = minLon - currentLatDiff / 2;
        newCamera.east = maxLat + currentLonDiff / 2;
        newCamera.west = minLat - currentLonDiff / 2;
        //camera.positionを緯度経度に合わせて設定
        const scene = this.props.terria.cesium.scene;
        const terrainProvider = scene.terrainProvider;
        const positions = [Cartographic.fromDegrees(lon, minLat)];
        let height = 0;
        sampleTerrainMostDetailed(terrainProvider, positions).then((updatedPositions) => {
            height = updatedPositions[0].height
            let coord_wgs84 = Cartographic.fromDegrees(lon, minLat, parseFloat(height) + parseInt((400000 * currentLatDiff )) + 200 );
            let coord_xyz = Ellipsoid.WGS84.cartographicToCartesian(coord_wgs84);
            newCamera.position = { x: coord_xyz.x, y: coord_xyz.y, z: coord_xyz.z - parseInt((300000 * currentLatDiff )) - 170 };
            //カメラの向きは統一にさせる
            newCamera.direction = { x: this.props.terria.focusCameraDirectionX, y: this.props.terria.focusCameraDirectionY, z: this.props.terria.focusCameraDirectionZ };
            newCamera.up = { x: this.props.terria.focusCameraUpX, y: this.props.terria.focusCameraUpY, z:this.props.terria.focusCameraUpZ };
            this.state.terria.currentViewer.zoomTo(newCamera, 5);
        })
    }
    /**
     * フォーカス処理（正面表示）
     * @param {number} maxLon 最大経度
     * @param {number} maxLat 最大緯度
     * @param {number} minLon 最小経度
     * @param {number} minLat 最小緯度
     * @param {number} lon 経度
     * @param {number} lat 緯度
     */
    outputFocusMapPlaceWithTop(maxLon, maxLat, minLon, minLat, lon, lat) {
        // 3dmodeにセット
        this.props.viewState.set3dMode();
        //現在のカメラ位置等を取得
        const currentSettings = getShareData(this.state.terria, this.props.viewState);
        const currentCamera = currentSettings.initSources[0].initialCamera;
        let newCamera = Object.assign(currentCamera);
        //新規の表示範囲を設定
        let currentLonDiff = Math.abs(maxLon - minLon);
        let currentLatDiff = Math.abs(maxLat - minLat);
        newCamera.north = maxLon + currentLatDiff / 2;
        newCamera.south = minLon - currentLatDiff / 2;
        newCamera.east = maxLat + currentLonDiff / 2;
        newCamera.west = minLat - currentLonDiff / 2;
        //camera.positionを緯度経度に合わせて設定
        const scene = this.props.terria.cesium.scene;
        const terrainProvider = scene.terrainProvider;
        const positions = [Cartographic.fromDegrees(lon, lat)];
        let height = 0;
        sampleTerrainMostDetailed(terrainProvider, positions).then((updatedPositions) => {
            height = updatedPositions[0].height
            //let coord_wgs84 = Cartographic.fromDegrees(lon, minLat, parseFloat(height) + parseInt((400000 * currentLatDiff )) + 200 );
            let coord_wgs84 = Cartographic.fromDegrees(lon, lat, parseFloat(height + 300) + parseInt((100000 * (maxLon-minLon) )));
            let coord_xyz = Ellipsoid.WGS84.cartographicToCartesian(coord_wgs84);
            //newCamera.position = { x: coord_xyz.x, y: coord_xyz.y, z: coord_xyz.z - parseInt((300000 * currentLatDiff )) - 170 };
            newCamera.position = { x: coord_xyz.x, y: coord_xyz.y, z: coord_xyz.z  };
            //カメラの向きは統一にさせる
            newCamera.direction = { x: this.props.terria.focusCameraDirectionX, y: this.props.terria.focusCameraDirectionY - 1.0, z: this.props.terria.focusCameraDirectionZ };
            newCamera.up = { x: this.props.terria.focusCameraUpX, y: this.props.terria.focusCameraUpY, z:this.props.terria.focusCameraUpZ };
            this.state.terria.currentViewer.zoomTo(newCamera, 5);
        })
    }
    /**
     * レイヤ表示切替及びcapture処理（呼び出し元）
     */
    showCaptureLayers() {
        let total = 1;
        let generalConditionDiagnosisResult = this.state.generalConditionDiagnosisResult;
        generalConditionDiagnosisResult = Object.values(generalConditionDiagnosisResult);
        generalConditionDiagnosisResult = generalConditionDiagnosisResult.filter(Boolean);

        //全ての処理数を算出
        Object.keys(generalConditionDiagnosisResult).map((key) => {
            if (Object.keys(generalConditionDiagnosisResult[key].layers).length > 0) {
                total = total + 1;
            }
        });
        //処理対象数を初期化
        this.capturedCount = 0;
        this.currentCaptureCount = 1;
        this.captureMaxCount = total;
        this.layerIndex = 0;
        this.generalConditionDiagnosisResultIndex = 0;
        //概況図のcaptureを取得するために関連レイヤのリセット
        Object.keys(generalConditionDiagnosisResult).map(key => {
            const items = this.state.terria.workbench.items;
            const layers = generalConditionDiagnosisResult[key].layers;
            for (const aItem of items) {
                Object.keys(layers).map(key => {
                    if (aItem.uniqueId === layers[key].layerCode || aItem.uniqueId === '対象地点') {
                        this.state.terria.workbench.remove(aItem);
                        aItem.loadMapItems();
                    }
                });
            }
        });
        //terriaに概況診断コンポーネントをセット
        this.props.terria.setGeneralConditionDiagnosis(this);
        //概況診断のキャプチャ格納用フォルダを生成
        fetch(Config.config.apiUrl + "/judgement/image/upload/preparation")
        .then(res => res.json())
        .then(res => {
            if(res.status === 401){
                alert("認証情報が無効です。ページの再読み込みを行います。");
                window.location.reload();
                return null;
            }
            if (res.folderName) {
                const folderName = res.folderName;
                this.props.viewState.setFolderName(folderName);
                this.setState({ captureMode: true }, () => {
                    //初回キャプチャ呼び出し
                    if(parseInt(document.getElementById("ProgressBarJsx").style.width) >= 90){
                        this.initCapture(0, 1, this.props.terria);
                    }else{
                        //初回キャプチャのイベントをセット
                        this.eventHelper.add(
                            this.props.terria.tileLoadProgressEvent,
                            this.initCapture
                        );
                    }
                });
            }else{
                this.errorHandler(null);
            }
        }).catch(error => {
            this.errorHandler(error);
        });

    }

    /**
     * 初回キャプチャ処理(tileLoadProgressEvent)
     * @param {*} _remaining 
     * @param {*} _max 
     */
    initCapture(_remaining, _max, terria) {
        const rawPercentage = (1 - _remaining / _max) * 100;
        const sanitisedPercentage = Math.floor(_remaining > 0 ? rawPercentage : 100);
        const generalConditionDiagnosis = terria.generalConditionDiagnosis;
        const uploadForGeneralConditionDiagnosisForm = {};
        if(generalConditionDiagnosis.capturedCount<generalConditionDiagnosis.currentCaptureCount && sanitisedPercentage>=80){
            //キャプチャ済み処理数を更新
            generalConditionDiagnosis.capturedCount=generalConditionDiagnosis.capturedCount+1;
            //キャプチャ処理
            if(generalConditionDiagnosis.capturedCount === generalConditionDiagnosis.currentCaptureCount){
                generalConditionDiagnosis.eventHelper.removeAll();
                generalConditionDiagnosis.state.terria.currentViewer
                .captureScreenshot()
                .then(dataString => {
                    uploadForGeneralConditionDiagnosisForm[generalConditionDiagnosis.currentCaptureCount] = {"image":generalConditionDiagnosis.decodeBase64(dataString),"judgementId":null, "judgeResultItemId": -1, "currentSituationMapFlg":true};
                    generalConditionDiagnosis.updateProgressBar();
                    generalConditionDiagnosis.setState({uploadForGeneralConditionDiagnosisForm:uploadForGeneralConditionDiagnosisForm},()=>{
                        //概況診断毎のキャプチャ処理呼び出し
                        generalConditionDiagnosis.generalConditionDiagnosisResultLoop();
                        //裏側でキャプチャのアップロード
                        generalConditionDiagnosis.uploadCapture(uploadForGeneralConditionDiagnosisForm,generalConditionDiagnosis.currentCaptureCount)
                    });
                }).catch(error => {
                    generalConditionDiagnosis.eventHelper.removeAll();
                    generalConditionDiagnosis.errorHandler(error);
                });
            }
        }
    }

    /**
     * キャプチャ用の概況診断取得処理(再帰処理)
     */
    generalConditionDiagnosisResultLoop(){
        const index = this.generalConditionDiagnosisResultIndex;
        let generalConditionDiagnosisResult = this.state.generalConditionDiagnosisResult;
        generalConditionDiagnosisResult = Object.values(generalConditionDiagnosisResult);
        generalConditionDiagnosisResult = generalConditionDiagnosisResult.filter(Boolean);
        if (Object.keys(generalConditionDiagnosisResult[index].layers).length > 0) {
            //概況図のcaptureを取得するために関連レイヤのリセット
            Object.keys(generalConditionDiagnosisResult).map(innerKey => {
                const innerItems = this.state.terria.workbench.items;
                const innerLayers = generalConditionDiagnosisResult[innerKey].layers;
                for (const aItem of innerItems) {
                    Object.keys(innerLayers).map(innerLayerKey => {
                        if (aItem.uniqueId === innerLayers[innerLayerKey].layerCode) {
                            this.state.terria.workbench.remove(aItem);
                            aItem.loadMapItems();
                        }
                    });
                }
            });
            //概況診断の関連レイヤを全て表示
            this.layerIndex = 0;
            this.layerLoop();
        }else{
            if(this.captureMaxCount > this.capturedCount && this.generalConditionDiagnosisResultIndex < Object.keys(generalConditionDiagnosisResult).length-1){
                this.generalConditionDiagnosisResultIndex = this.generalConditionDiagnosisResultIndex+1;
                //再帰呼び出し
                this.generalConditionDiagnosisResultLoop();
            }
        }
    }

    /**
     * キャプチャ用のレイヤー表示(再帰処理)
     */
    layerLoop(){
        const index = this.generalConditionDiagnosisResultIndex;
        const layerIndex = this.layerIndex;
        let generalConditionDiagnosisResult = this.state.generalConditionDiagnosisResult;
        generalConditionDiagnosisResult = Object.values(generalConditionDiagnosisResult);
        generalConditionDiagnosisResult = generalConditionDiagnosisResult.filter(Boolean);
        let layers = generalConditionDiagnosisResult[index].layers;
        layers = Object.values(layers);
        layers = layers.filter(Boolean);
        // 建物モデルの表示
        this.switch3DBuildings(generalConditionDiagnosisResult[index].buildingDisplayFlag);
        // 判定レイヤと同時表示レイヤを表示
        if ((generalConditionDiagnosisResult[index].result && layers[layerIndex].layerType) || (generalConditionDiagnosisResult[index].judgementLayerDisplayFlag && layers[layerIndex].layerType) || (generalConditionDiagnosisResult[index].simultameousLayerDisplayFlag && !layers[layerIndex].layerType)) {
            // 表示位置の切替
            if (generalConditionDiagnosisResult[index].extentFlag && generalConditionDiagnosisResult[index].minlon 
                && generalConditionDiagnosisResult[index].minlat && generalConditionDiagnosisResult[index].maxlon && generalConditionDiagnosisResult[index].maxlat) {
                // 指定した範囲で表示
                this.resultRowFocusMapPlace(generalConditionDiagnosisResult[index].maxlon, generalConditionDiagnosisResult[index].maxlat, 
                    generalConditionDiagnosisResult[index].minlon, generalConditionDiagnosisResult[index].minlat);
            } else {
                // 申請地番全域表示
                this.focusMapPlaceDriver();
            }
            if (layers[layerIndex].layerQuery && layers[layerIndex].layerQuery.includes(Config.wfs.identify_text)) {
                // WFS
                const item = new WebFeatureServiceCatalogItem(layers[layerIndex].layerCode, this.state.terria);
                const wfsUrl = Config.config.geoserverWfsUrl;
                item.setTrait(CommonStrata.definition, "url", wfsUrl);
                item.setTrait(CommonStrata.user, "name", layers[layerIndex].layerName);
                item.setTrait(
                    CommonStrata.user,
                    "typeNames",
                    layers[layerIndex].layerCode);
                if (layers[layerIndex].layerQuery.includes(Config.wfs.road_width_identify_text)) {
                    // 道路判定幅員値
                    item.setTrait(CommonStrata.user,
                        "parameters",
                        {
                            "viewparams": layers[layerIndex].layerQuery.replace(Config.wfs.road_width_identify_text, "").replace(Config.wfs.identify_text, ""),
                        });
                    item.setTrait(CommonStrata.user, "czmlTemplate", Config.wfs.czmlTemplate.road_width);
                } 
                item.loadMapItems();
                this.state.terria.workbench.add(item).then(
                    r =>{
                        this.judgementLayerChange();
                    }
                );
            } else {
                const item = new webMapServiceCatalogItem(layers[layerIndex].layerCode, this.state.terria);
                const wmsUrl = Config.config.geoserverUrl;
                item.setTrait(CommonStrata.definition, "url", wmsUrl);
                item.setTrait(CommonStrata.user, "name", layers[layerIndex].layerName);
                item.setTrait(
                    CommonStrata.user,
                    "layers",
                    layers[layerIndex].layerCode);
                if (layers[layerIndex].layerQuery && layers[layerIndex].queryRequireFlag) {
                    item.setTrait(CommonStrata.user,
                        "parameters",
                        {
                            "viewparams": layers[layerIndex].layerQuery,
                        });
                } else {
                    item.setTrait(CommonStrata.user,
                        "parameters",
                        {});
                }
                item.loadMapItems();
                this.state.terria.workbench.add(item).then(
                    r =>{
                        this.judgementLayerChange();
                    }
                );
            }
            
        }else{
            this.judgementLayerChange();
        }
    }

    /**
     * layerLoopの再帰呼び出しを判定
     */
     judgementLayerChange(){
        const index = this.generalConditionDiagnosisResultIndex;
        const layerIndex = this.layerIndex;
        let generalConditionDiagnosisResult = this.state.generalConditionDiagnosisResult;
        generalConditionDiagnosisResult = Object.values(generalConditionDiagnosisResult);
        generalConditionDiagnosisResult = generalConditionDiagnosisResult.filter(Boolean);
        let layers = generalConditionDiagnosisResult[index].layers;
        layers = Object.values(layers);
        layers = layers.filter(Boolean);
        if(layerIndex === Object.keys(layers).length - 1){
            //3秒後にイベントセット(即時の場合は上手くいかない)
            setTimeout(function() {
                this.currentCaptureCount = this.currentCaptureCount+1;
                if(parseInt(document.getElementById("ProgressBarJsx").style.width) >= 90){
                    this.layerCapture(0, 1, this.props.terria);
                }else{
                    //初回以降のレイヤキャプチャのイベントをセット
                    this.eventHelper.add(
                        this.props.terria.tileLoadProgressEvent,
                        this.layerCapture
                    );
                }
            }.bind(this), 3000);
        }else{
            //概況診断のレイヤindexを更新
            this.layerIndex = this.layerIndex + 1;
            this.layerLoop();
        }
     }

     /**
     * 初回以降のレイヤキャプチャ処理(tileLoadProgressEvent)
     * @param {*} _remaining 
     * @param {*} _max 
     */
    layerCapture(_remaining, _max, terria) {
        const rawPercentage = (1 - _remaining / _max) * 100;
        const sanitisedPercentage = Math.floor(_remaining > 0 ? rawPercentage : 100);
        const generalConditionDiagnosis = terria.generalConditionDiagnosis;
        const index = generalConditionDiagnosis.generalConditionDiagnosisResultIndex;
        const uploadForGeneralConditionDiagnosisForm = generalConditionDiagnosis.state.uploadForGeneralConditionDiagnosisForm;
        let generalConditionDiagnosisResult = generalConditionDiagnosis.state.generalConditionDiagnosisResult;
        generalConditionDiagnosisResult = Object.values(generalConditionDiagnosisResult);
        generalConditionDiagnosisResult = generalConditionDiagnosisResult.filter(Boolean);
        if(generalConditionDiagnosis.capturedCount<generalConditionDiagnosis.currentCaptureCount && sanitisedPercentage>=80){
            //キャプチャ済み処理数を更新
            generalConditionDiagnosis.capturedCount=generalConditionDiagnosis.capturedCount + 1;
            //キャプチャ処理
            if( generalConditionDiagnosis.capturedCount=== generalConditionDiagnosis.currentCaptureCount){
                generalConditionDiagnosis.eventHelper.removeAll();
                generalConditionDiagnosis.state.terria.currentViewer
                .captureScreenshot()
                .then(dataString => {
                    uploadForGeneralConditionDiagnosisForm[generalConditionDiagnosis.currentCaptureCount] = { "image": generalConditionDiagnosis.decodeBase64(dataString), "judgementId": generalConditionDiagnosisResult[index].judgementId, "judgeResultItemId": generalConditionDiagnosisResult[index].judgeResultItemId, "currentSituationMapFlg": false };
                    generalConditionDiagnosis.updateProgressBar();
                    generalConditionDiagnosis.setState({uploadForGeneralConditionDiagnosisForm:uploadForGeneralConditionDiagnosisForm},()=>{
                        if(generalConditionDiagnosis.generalConditionDiagnosisResultIndex < Object.keys(generalConditionDiagnosisResult).length-1){
                            generalConditionDiagnosis.generalConditionDiagnosisResultIndex = index + 1;
                            //概況診断毎のキャプチャ処理を呼び出し
                            generalConditionDiagnosis.generalConditionDiagnosisResultLoop();
                        }
                        //裏側でキャプチャのアップロード
                        generalConditionDiagnosis.uploadCapture(uploadForGeneralConditionDiagnosisForm,generalConditionDiagnosis.currentCaptureCount)
                    });
                }).catch(error => {
                    generalConditionDiagnosis.eventHelper.removeAll();
                    generalConditionDiagnosis.errorHandler(error);
                });
            }
        }
    }

    /**
     * 概況診断キャプチャ画像のアップロード処理
     * @param {Object} 概況診断結果のキャプチャ一覧
     * @param {string} key
     */
    uploadCapture(uploadForGeneralConditionDiagnosisForm,key){
        uploadForGeneralConditionDiagnosisForm[key].folderName = this.props.viewState.folderName;
        const formData = new FormData();
        for (const name in uploadForGeneralConditionDiagnosisForm[key]) {
            formData.append(name, uploadForGeneralConditionDiagnosisForm[key][name]);
        }
        fetch(Config.config.apiUrl + "/judgement/image/upload", {
            method: 'POST',
            body: formData,
        })
        .then(res => res.json())
        .then(res => {
            if(res.status === 401){
                alert("認証情報が無効です。ページの再読み込みを行います。");
                window.location.reload();
                return null;
            }
            uploadForGeneralConditionDiagnosisForm[key]["status"] = res.status;
            this.setState({ uploadForGeneralConditionDiagnosisCompleteForm: uploadForGeneralConditionDiagnosisForm});
        }).catch(error => {
            this.errorHandler(error);
        });
    }

    /**
     * プログレスバーを更新
     */
    updateProgressBar(){
        const myBar = document.getElementById("myBar");
        const wholePercent = document.getElementById("wholePercent");
        const numberOfSheets = document.getElementById("numberOfSheets");
        if (this.captureMaxCount > this.capturedCount ) {
            let myBarWidth = myBar.style.width?parseInt(myBar.style.width):0;
            myBar.style.width = parseInt(myBarWidth + (this.captureMaxPercent / this.captureMaxCount )) + "%";
            numberOfSheets.innerHTML = this.capturedCount + "/" + this.captureMaxCount;
            wholePercent.innerHTML = parseInt(myBar.style.width);
        } else if(this.captureMaxCount === this.capturedCount){
            myBar.style.width = this.captureAllMaxPercent + "%";
            numberOfSheets.innerHTML = this.capturedCount + "/" + this.captureMaxCount;
            wholePercent.innerHTML = this.captureAllMaxPercent;
        }
    }

    /**
     * base64形式からfileObjectへ変換
     * @param {string} base64形式
     * @return {File} fileObject
     */
    decodeBase64(fileData) {
        let bin = atob(fileData.replace(/^.*,/, ''));
        let buffer = new Uint8Array(bin.length);
        for (let i = 0; i < bin.length; i++) {
            buffer[i] = bin.charCodeAt(i);
        }
        let image_file = new File([buffer.buffer], "map.png", { type: "image/png" });
        return image_file;
    }

    /**
     * 概況診断レポートをExcel形式で出力
     */
    outputFile() {
        let generalConditionDiagnosisResult = this.state.generalConditionDiagnosisResult;
        generalConditionDiagnosisResult = Object.values(generalConditionDiagnosisResult);
        generalConditionDiagnosisResult = generalConditionDiagnosisResult.filter(Boolean);
        let applicationPlace = this.props.viewState.applicationPlace;
        applicationPlace = Object.values(applicationPlace);
        applicationPlace = applicationPlace.filter(Boolean);
        let checkedApplicationCategory = this.props.viewState.checkedApplicationCategory;
        checkedApplicationCategory = Object.values(checkedApplicationCategory);
        checkedApplicationCategory = checkedApplicationCategory.filter(Boolean);
        let myBar = document.getElementById("myBar");
        let wholePercent = document.getElementById("wholePercent");
        let numberOfSheets = document.getElementById("numberOfSheets");
        let allMax = 100;
        //申請登録時のレポート生成の場合帳票出力は行わない
        if(!document.getElementById("confirmApplicationDetailsRegisterButton")){
            fetch(Config.config.apiUrl + "/judgement/report", {
                method: 'POST',
                body: JSON.stringify({
                    folderName: this.props.viewState.folderName,
                    lotNumbers: applicationPlace,
                    applicationCategories: checkedApplicationCategory,
                    generalConditionDiagnosisResults: generalConditionDiagnosisResult
                }),
                headers: new Headers({ 'Content-type': 'application/json' }),
            })
            .then((res) => {
                if(res.status === 401){
                    alert("認証情報が無効です。ページの再読み込みを行います。");
                    window.location.reload();
                    return null;
                }else{
                    return res.blob();
                }
            })
            .then(blob => {
                myBar.style.width = allMax + "%";
                wholePercent.innerHTML = parseInt(myBar.style.width);

                //概況診断レポートの出力
                const now = new Date();
                const filename = "概況診断結果" + now.toLocaleDateString();
                let anchor = document.createElement("a");
                anchor.href = window.URL.createObjectURL(blob);
                anchor.download = filename + ".xlsx";
                anchor.click();

                try{
                    Object.keys(generalConditionDiagnosisResult).map(key => {
                        const items = this.state.terria.workbench.items;
                        const layers = generalConditionDiagnosisResult[key].layers;
                        for (const aItem of items) {
                            Object.keys(layers).map(key => {
                                if (aItem.uniqueId === layers[key].layerCode) {
                                    this.state.terria.workbench.remove(aItem);
                                    aItem.loadMapItems();
                                }
                            });
                        }
                    });
                }catch(error){
                    console.error('処理に失敗しました', error);
                }

                // 帳票出力したあとで、アンケート画面を開く
                this.openQuestionaryView();
            })
            .catch(error => {
                this.errorHandler(error);
            }).finally(() => {
                //初期化
                this.setState({ captureMode: false, uploadForGeneralConditionDiagnosisCompleteForm: {},uploadForGeneralConditionDiagnosisForm: {} },()=>{
                    this.clearLayer(false);
                    this.props.terria.setGeneralConditionDiagnosis(null);
                    setTimeout(() => {
                        document.getElementById("loadingBg").style.display = "none";
                        document.getElementById("loading").style.display = "none";
                        myBar.style.width = "0%";
                        wholePercent.innerHTML = 0;
                        numberOfSheets.innerHTML = 0 + "/" + 0;
                    }, 3000)
                });
            });
        }else{
            //初期化
            this.setState({ captureMode: false, uploadForGeneralConditionDiagnosisCompleteForm: {},uploadForGeneralConditionDiagnosisForm: {} },()=>{
                this.clearLayer(false);
                this.props.terria.setGeneralConditionDiagnosis(null);
                setTimeout(() => {
                    document.getElementById("loadingBg").style.display = "none";
                    document.getElementById("loading").style.display = "none";
                    myBar.style.width = "0%";
                    wholePercent.innerHTML = 0;
                    numberOfSheets.innerHTML = 0 + "/" + 0;
                    document.getElementById("confirmApplicationDetailsRegisterButton").click();
                }, 3000)
            });
        }
    }

    /**
     * アンケート画面を開く
     */
    openQuestionaryView(){
        if(Config.QuestionaryActived.GeneralConditionDiagnosisView == "true"){
            setTimeout(() => {
                // URL
                let url = Config.config.questionnaireUrlForBusiness;
                // ターゲット名
                let target="develop_quessionaire";
                // アンケート画面を開く
                window.open(url,target);
            }, 5000)
        }
    }

    /**
     * 関連レイヤの表示切替処理
     * @param {Object} obj 対象の概況診断結果
     */
    showLayers(obj) {
        try{
            const items = this.state.terria.workbench.items;
            const layers = obj.layers;
            const displayDescriptionFlg = this.state.displayDescriptionFlg;
            const currentGeneralConditionDiagnosisResult = this.state.currentGeneralConditionDiagnosisResult;
            let generalConditionDiagnosisResult = this.state.generalConditionDiagnosisResult;
            // 一旦全ての関連レイヤをリセット
            Object.keys(generalConditionDiagnosisResult).map(key => {
                const layers = generalConditionDiagnosisResult[key].layers;
                for (const aItem of items) {
                    Object.keys(layers).map(key => {
                        if (aItem.uniqueId === layers[key].layerCode) {
                            this.state.terria.workbench.remove(aItem);
                            aItem.loadMapItems();
                        }
                    });
                }
            });

            // 該当判定結果の詳細は表示されているか判断して、表示・非表示を切り替え
            if(displayDescriptionFlg){
                //if(currentGeneralConditionDiagnosisResult.judgementId === obj.judgementId){
                if(currentGeneralConditionDiagnosisResult.judgeResultItemId === obj.judgeResultItemId){
                    this.setState({displayDescriptionFlg:false});
                    return;
                }else{
                    this.setState({currentGeneralConditionDiagnosisResult:obj});
                }
            }else{
                this.setState({displayDescriptionFlg:true,currentGeneralConditionDiagnosisResult:obj});
            }
            // 建物モデルの表示
            this.switch3DBuildings(obj.buildingDisplayFlag);
            
            // レイヤの表示
            Object.keys(layers).map(key => {
                // 判定レイヤと同時表示レイヤを表示
                if ((obj.result && layers[key].layerType) || (obj.judgementLayerDisplayFlag && layers[key].layerType) || (obj.simultameousLayerDisplayFlag && !layers[key].layerType)) {
                    if (layers[key].layerQuery && layers[key].layerQuery.includes(Config.wfs.identify_text)) {
                        // WFS
                        const item = new WebFeatureServiceCatalogItem(layers[key].layerCode, this.state.terria);
                        const wfsUrl = Config.config.geoserverWfsUrl;
                        item.setTrait(CommonStrata.definition, "url", wfsUrl);
                        item.setTrait(CommonStrata.user, "name", layers[key].layerName);
                        item.setTrait(
                            CommonStrata.user,
                            "typeNames",
                            layers[key].layerCode);
                        if (layers[key].layerQuery.includes(Config.wfs.road_width_identify_text)) {
                            // 道路判定幅員値
                            item.setTrait(CommonStrata.user,
                                "parameters",
                                {
                                    "viewparams": layers[key].layerQuery.replace(Config.wfs.road_width_identify_text, "").replace(Config.wfs.identify_text, ""),
                                });
                            item.setTrait(CommonStrata.user, "czmlTemplate", Config.wfs.czmlTemplate.road_width);
                        } 
                        item.loadMapItems();
                        this.state.terria.workbench.add(item);
                    } else {
                        // WMS
                        const item = new webMapServiceCatalogItem(layers[key].layerCode, this.state.terria);
                        const wmsUrl = Config.config.geoserverUrl;
                        item.setTrait(CommonStrata.definition, "url", wmsUrl);
                        item.setTrait(CommonStrata.user, "name", layers[key].layerName);
                        item.setTrait(
                            CommonStrata.user,
                            "layers",
                            layers[key].layerCode);
                        if (layers[key].layerQuery && layers[key].queryRequireFlag) {
                            item.setTrait(CommonStrata.user,
                                "parameters",
                                {
                                    "viewparams": layers[key].layerQuery,
                                });
                        } else {
                            item.setTrait(CommonStrata.user,
                                "parameters",
                                {});
                        }
                        item.loadMapItems();
                        this.state.terria.workbench.add(item);
                    }
                    
                }
            });
            // 表示位置の切替
            if (obj.extentFlag && obj.minlon && obj.minlat && obj.maxlon && obj.maxlat) {
                // 指定した範囲で表示
                this.resultRowFocusMapPlace(obj.maxlon, obj.maxlat, obj.minlon, obj.minlat);
            } else {
                // 申請地番全域表示
                this.focusMapPlaceDriver();
            }
        this.setState({displayDescriptionFlg:true,displayDescription:obj.description});
        }catch(error){
            console.error('処理に失敗しました', error);
        }
    }

    // 全ての関連レイヤ(地番含む)をリセット
    clearLayer(isClose) {
        try{
            const items = this.state.terria.workbench.items;
            let generalConditionDiagnosisResult = this.state.generalConditionDiagnosisResult;
            if(isClose){
                for (const aItem of items) {
                    if (aItem.uniqueId === Config.layer.lotnumberSearchLayerNameForBusiness || aItem.uniqueId === Config.layer.lotnumberSearchLayerNameForGoverment || aItem.uniqueId === Config.layer.lotnumberSearchLayerNameForApplicationTarget || aItem.uniqueId === Config.landMark.id) {
                        this.state.terria.workbench.remove(aItem);
                        aItem.loadMapItems();
                    }
                }
            }
            Object.keys(generalConditionDiagnosisResult).map(key => {
                const layers = generalConditionDiagnosisResult[key].layers;
                for (const aItem of items) {
                    Object.keys(layers).map(key => {
                        if (aItem.uniqueId === layers[key].layerCode) {
                            this.state.terria.workbench.remove(aItem);
                            aItem.loadMapItems();
                        }
                    });
                }
            });
        }catch(error){
            console.error('処理に失敗しました', error);
        }
    }

    //閉じる
    close() {
        this.clearLayer(true);
        this.props.viewState.backFromGeneralAndRoadJudgementResultView();
    }

    //次へ
    next() {
        this.clearLayer(false);
        this.props.viewState.moveToApplyInformationView(this.state.generalConditionDiagnosisResult);
    }

    /**
     * エラーハンドリング処理
     * @param {error}
     */
    errorHandler(error){
        document.getElementById("myBar").style.width = "0%";
        document.getElementById("wholePercent").innerHTML = 0;
        document.getElementById("numberOfSheets").innerHTML = 0 + "/" + 0;
        document.getElementById("loadingBg").style.display = "none";
        document.getElementById("loading").style.display = "none";
        if(error){
            console.error('処理に失敗しました', error);
        }
        this.setState({captureMode: false , uploadForGeneralConditionDiagnosisCompleteForm: {},uploadForGeneralConditionDiagnosisForm: {}  });
        this.props.terria.setGeneralConditionDiagnosis(null);
        alert("帳票生成または帳票出力時に何らかのエラーが発生しました。大変お手数ですが一度画面を閉じて初めからやり直すか、もう一度実行してください。")
    }

    /**
     * リンク文字列変換処理
     * @param {string} リンク変換前文字列
     * @return {string} リンク変換後文字列
     */
    autoLink(str) {
        let regexp_url = /((<a>h?)(ttps?:\/\/[a-zA-Z0-9.\-_@:/~?%&;=+#',()*!]+)(<\/a>))/g; // ']))/;
        let regexp_makeLink = function(all, url, h, href) {
            url = url.replace("<a>", "");
            url = url.replace("</a>", "");
            return '<a href="h' + href + '" target="_blank">' + url + '</a>';
        }
        return str.replace(regexp_url, regexp_makeLink);
    }
    /**
     * 建物モデルの表示・非表示を切り替える
     * @param {*} showFlag 
     */
    switch3DBuildings(showFlag) {
        const cesium3DTiles = this.state.terria.getModelById(BaseModel, Config.buildingModel.id);
            cesium3DTiles.traits.style.show = showFlag;
            cesium3DTiles.setTrait(
                CommonStrata.user,
                "style",
                {"show": showFlag});
            cesium3DTiles.loadMapItems();
    }
    render() {
        let generalConditionDiagnosisResult = this.state.generalConditionDiagnosisResult;
        let disabledFlg = this.state.disabledFlg;
        let height = this.state.height;
        let descriptionDivWidth = this.state.width;
        let positionLeft = this.state.positionLeft;
        const displayDescriptionFlg = this.state.displayDescriptionFlg;
        let judgeResultItemId = "";
        if(displayDescriptionFlg){
            judgeResultItemId = this.state.currentGeneralConditionDiagnosisResult.judgeResultItemId;
        }
        return (
            <>
                <div className={CustomStyle.loadingBg} id="loadingBg"></div>
                <div className={CustomStyle.loading} id="loading">
                    <p style={{ textAlign: "center" }}>処理中です。暫く画面はこのままでお待ちください。</p>
                    <p style={{ textAlign: "center" }}>※バックグラウンド動作の場合正常にキャプチャの切替が行われませんのでご注意ください。</p>
                    <p style={{ textAlign: "center" }}>画面キャプチャ取得中 <span id="numberOfSheets">0/0</span></p>
                    <div className={CustomStyle.myProgress}>
                        <div className={CustomStyle.myBar} id="myBar"></div>
                    </div>
                    <p style={{ textAlign: "center" }}>帳票作成中 <span id="wholePercent">0</span>%</p>
                    <If condition = {!document.getElementById("confirmApplicationDetailsRegisterButton") && Config.QuestionaryActived.GeneralConditionDiagnosisView == "true"}>
                        <span style={{ textAlign: "center", fontSize: "x-small" }}>※帳票出力したあとで、別タブでアンケート画面が開きますので、アンケートへの回答ご協力お願いします。</span>
                    </If>
                </div>
                <div style={{margin: 5 + "px", marginBottom :20 + "px"}}>
                    <Box id="GeneralConditionDiagnosis" css={`display:block`} >
                        <nav className={CustomStyle.custom_nuv} id="GeneralConditionDiagnosisDrag">
                            概況診断結果
                        </nav>
                        <Box paddedRatio={2}></Box>
                        <div id="GeneralConditionDiagnosisDragTable">
                            <div className={CustomStyle.table_frame} style={{height: height + "px", position: "relative"}}>
                                <div id="customloaderForGeneralConditionDiagnosisDrag" className={CustomStyle.customloaderParent}>
                                    <img className={CustomStyle.customloader} src="./images/loader.gif" />
                                </div>
                                <table className={CustomStyle.selection_table}>
                                    <thead>
                                        <tr>
                                            <th style={{ width: 245 + "px" }}>対象</th>
                                            <th style={{ width: 150 + "px" }}>判定結果</th>
                                            <th style={{ width: 50 + "px" }}>距離</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {Object.keys(generalConditionDiagnosisResult).map(key => (
                                            <tr key={key} 
                                                onClick={() => { this.showLayers(generalConditionDiagnosisResult[key]);}}
                                                className={generalConditionDiagnosisResult[key]["judgeResultItemId"] == judgeResultItemId ? CustomStyle.isActiveLine : CustomStyle.tr_button} 
                                            >
                                                <td className={CustomStyle.title}>
                                                    {generalConditionDiagnosisResult[key].title}
                                                </td>
                                                <td className={CustomStyle.result}>
                                                    {generalConditionDiagnosisResult[key].summary}
                                                </td>
                                                <td className={CustomStyle.title}>
                                                    {generalConditionDiagnosisResult[key].distance}
                                                </td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </Box >
                    <div className={CustomStyle.custom_footer}>
                        <div className={CustomStyle.box}>
                            <div className={CustomStyle.item}>
                                <button className={CustomStyle.custom_button} disabled={disabledFlg} onClick={() => {
                                    this.outputPreparation();
                                    }}>出力</button>
                                <button className={CustomStyle.custom_button} id="generalConditionOutputBtn" style={{ display: "none" }} onClick={() => {
                                    this.outputPreparationForConfirmApplicationDetails();
                                    }}>出力</button>
                            </div>
                            <div className={CustomStyle.item}>
                                <button className={CustomStyle.custom_button} disabled={disabledFlg} onClick={() => {
                                    this.next();
                                    }}>申請</button>
                            </div>
                            <div className={CustomStyle.item}>
                                <button className={CustomStyle.custom_button} onClick={() => {
                                    this.close();
                                    }}>閉じる</button>
                            </div>
                        </div>
                    </div>
                    <div className={CustomStyle.descriptionDiv}>
                        {this.state.displayDescriptionFlg && (
                        <div className={CustomStyle.description}>
                            <div className={CustomStyle.descriptionInner}>
                                <span dangerouslySetInnerHTML={{ __html: this.autoLink(this.state.currentGeneralConditionDiagnosisResult.description) }}></span>
                            </div>
                        </div>
                        )}
                    </div>
                </div>
            </>
        );
    }
}
export default withTranslation()(withTheme(GeneralConditionDiagnosis));