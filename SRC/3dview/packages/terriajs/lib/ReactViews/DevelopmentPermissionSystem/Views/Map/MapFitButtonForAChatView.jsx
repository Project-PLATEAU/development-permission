import React from "react";
import PropTypes from "prop-types";
import { observer } from "mobx-react";
import { withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import { getShareData } from "../../../Map/Panels/SharePanel/BuildShareLink";
import Cartographic from "terriajs-cesium/Source/Core/Cartographic";
import Ellipsoid from "terriajs-cesium/Source/Core/Ellipsoid";
import sampleTerrainMostDetailed from "terriajs-cesium/Source/Core/sampleTerrainMostDetailed";
import Icon, { StyledIcon } from "../../../../Styled/Icon";
import { RawButton } from "../../../../Styled/Button";
import CustomStyle from "./scss/map-fit-button.scss";

/**
 * 選択中地番への移動ボタン
 */
@observer
class MapFitButtonForAChatView extends React.Component {
    static displayName = "MapFitButtonForAChatView"
    static propsType = {
        terria: PropTypes.object.isRequired,
        viewState: PropTypes.object.isRequired,
    }

    constructor(props){
        super(props);
        this.state = {
            viewState: props.viewState,
            terria: props.terria
        }
    }

    componentDidMount() {
        this.props.viewState.setMapFitButtonFunction(()=>{this.fitMap();});
    }

    fitMap(){
        let lotNumbers = this.props.viewState.lotNumbers;
        console.log(lotNumbers);
        if(lotNumbers.length > 0){
            this.focusMapPlaceDriver(lotNumbers);
        }else{
            alert("地番情報の取得に失敗しました。");
        }
    }

    /**
     * フォーカス処理ドライバー
     * @param {object} 申請地情報
     */
    focusMapPlaceDriver(lotNumbers) {
        let applicationPlace = Object.values(lotNumbers);
        applicationPlace = applicationPlace.filter(Boolean);
        console.log(applicationPlace);
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
        return (
            <div className={CustomStyle.map_fit_button}>
                <RawButton
                    onClick={e => this.fitMap()}
                    title="対象の申請地番に移動"
                    >
                    <StyledIcon 
                        styledWidth={"23px"}
                        fillColor={"#FFF"}
                        glyph={Icon.GLYPHS.location2}
                        css={`
                            cursor: pointer;
                            position:absolute;
                            top: 62%;
                            left: 50%;
                            transform: translate(-50%, -50%);
                        `}
                    />
                </RawButton>
            </div>
        )
    }
}
export default withTranslation()(withTheme(MapFitButtonForAChatView));