import { observer } from "mobx-react";
import PropTypes from "prop-types";
import React from "react";
import { withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import Box from "../../../Styled/Box";
import Styles from "./scss/pageStyle.scss";
import AnswerContent from "../Views/Answer/AnswerContent.jsx";
import CommonStrata from "../../../Models/Definition/CommonStrata";
import webMapServiceCatalogItem from '../../../Models/Catalog/Ows/WebMapServiceCatalogItem';
import Cartographic from "terriajs-cesium/Source/Core/Cartographic";
import Ellipsoid from "terriajs-cesium/Source/Core/Ellipsoid";
import { getShareData } from "../../Map/Panels/SharePanel/BuildShareLink";
import Config from "../../../../customconfig.json";
import sampleTerrainMostDetailed from "terriajs-cesium/Source/Core/sampleTerrainMostDetailed";


/**
 * 事業者用画面：回答内容確認画面
 */
@observer
class ConfirmAnswerInformationView extends React.Component {
    static displayName = "ConfirmAnswerInformationView";
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
            terria: props.terria
        };
    }

    /**
     * 初期処理
     */
    componentDidMount() {
        //サーバからデータを取得
        let applicationPlace = Object.assign({},this.props.viewState.answerContent.lotNumbers);
        applicationPlace = Object.values(applicationPlace);
        applicationPlace = applicationPlace.filter(Boolean);
        this.props.viewState.setLotNumbers(applicationPlace);
        try{
            const item = new webMapServiceCatalogItem(Config.layer.lotnumberSearchLayerNameForApplicationSearchTarget, this.state.terria);
            const wmsUrl = Config.config.geoserverUrl;
            const items = this.state.terria.workbench.items;
            for (const aItem of items) {
                if (aItem.uniqueId === Config.layer.lotnumberSearchLayerNameForApplicationSearchTarget) {
                    this.state.terria.workbench.remove(aItem);
                    aItem.loadMapItems();
                }
            }
            item.setTrait(CommonStrata.definition, "url", wmsUrl);
            item.setTrait(CommonStrata.user, "name", Config.layer.lotnumberSearchLayerDisplayNameForApplicationSearchTarget);
            item.setTrait(
                CommonStrata.user,
                "layers",
                Config.layer.lotnumberSearchLayerNameForApplicationSearchTarget);
            item.setTrait(CommonStrata.user,
                "parameters",
                {
                    "viewparams": Config.layer.lotnumberSearchViewParamNameForApplicationSearchTarget + this.props.viewState.answerContent.applicationId,                    
                });
            item.loadMapItems();
            this.state.terria.workbench.add(item);
        }catch(error){
            console.error('処理に失敗しました', error);
        }
    }


    /**
     * フォーカス処理ドライバー
     */
    focusMapPlaceDriver = () => {
        let applicationPlace = Object.assign({}, this.props.viewState.answerContent.lotNumbers);
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
     * フォーカス処理
     * @param {number} maxLon 最大経度
     * @param {number} maxLat 最大緯度
     * @param {number} minLon 最小経度
     * @param {number} minLat 最小緯度
     * @param {number} lon 経度
     * @param {number} lat 緯度
     */
    outputFocusMapPlace = (maxLon, maxLat, minLon, minLat, lon, lat) => {
        this.props.terria.focusMapPlace(maxLon, maxLat, minLon, minLat, lon, lat, this.props.viewState);
    }

    render() {
        return (
            <Box column style={{overflowY:"auto" , overflowX: "hidden"}} id="ConfirmAnswerInformationView" >
                <div className={Styles.div_area}>
                    <AnswerContent terria={this.props.terria} viewState={this.props.viewState}  focusMapPlaceDriver={this.focusMapPlaceDriver} />
                </div>
            </Box>
        );
    };
}

export default withTranslation()(withTheme(ConfirmAnswerInformationView));