import { observer } from "mobx-react";
import React, { useMemo } from "react";
import PropTypes from "prop-types";
import { withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import Icon, { StyledIcon } from "../../../../Styled/Icon";
import Box from "../../../../Styled/Box";
import Spacing from "../../../../Styled/Spacing";
import Text from "../../../../Styled/Text";
import Input from "../../../../Styled/Input";
import Button, { RawButton } from "../../../../Styled/Button";
import CustomStyle from "./scss/lot-number-search.scss";
import ScreenButton from "../../../Map/HelpButton/help-button.scss";
import {
    getShareData
} from "../../../Map/Panels/SharePanel/BuildShareLink";
import Common from "../../../AdminDevelopCommon/CommonFixedValue.json";
import Cartographic from "terriajs-cesium/Source/Core/Cartographic";
import Ellipsoid from "terriajs-cesium/Source/Core/Ellipsoid";
import GeoJsonCatalogItem from "../../../../Models/Catalog/CatalogItems/GeoJsonCatalogItem";
import CommonStrata from "../../../../Models/Definition/CommonStrata";
import Config from "../../../../../customconfig.json";
import webMapServiceCatalogItem from '../../../../Models/Catalog/Ows/WebMapServiceCatalogItem';
import CustomStyleDistrict from "./scss/district-name-search.scss";
import CustomStyleKana from "./scss/kana-district-name-search.scss";
import sampleTerrainMostDetailed from "terriajs-cesium/Source/Core/sampleTerrainMostDetailed";
import L from 'leaflet';
/**
 * 地番検索画面
 */
@observer
class LotNumberSearch extends React.Component {
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
            //地番
            lotNumber: "",
            //かな文字
            kanaText: "",
            //検索結果一覧テーブル定義情報
            table: [],
            //検索結果
            searchResult: [],
            //ボタン非活性フラグ
            disabled: false,
            //町丁名一覧
            districtList: [],
            //かなfilter済みの町丁名一覧
            filteredList: [],
        };
        // カナ検索コンポーネント（保持）
        this.props.viewState.setKanaDistrictNameSearch((props) => {
            const kanaText = props.kanaText;
            const filteredList = props.filteredList;
            const keyBoardList = [
                ["", "あ", "か", "さ", "た", "な", "は", "ま", "や", "ら", "わ", ""],
                ["", "い", "き", "し", "ち", "に", "ひ", "み", "", "り", "", ""],
                ["", "う", "く", "す", "つ", "ぬ", "ふ", "む", "ゆ", "る", "を", ""],
                ["", "え", "け", "せ", "て", "ね", "へ", "め", "", "れ", "", ""],
                ["", "お", "こ", "そ", "と", "の", "ほ", "も", "よ", "ろ", "ん", ""]
            ];
            const boxCssText = `
                    position: absolute;
                    z-index: 9989;
                    top: 51%;
                    left:50%;
                    `;
            return (
                <Box
                    displayInlineBlock
                    backgroundColor={this.props.theme.textLight}
                    styledWidth={"580px"}
                    styledHeight={"400px"}
                    fullHeight
                    overflow={"auto"}
                    id="KanaDistrictNameSearchFrame"
                    css={boxCssText}
                    className={CustomStyleKana.custom_frame}>
                    <Box position="absolute" paddedVertically={2} paddedHorizontally={3} topRight>
                        <RawButton onClick={() => {
                            this.props.viewState.hideKanaDistrictNameSearchView();
                        }}>
                            <StyledIcon
                                styledWidth={"16px"}
                                fillColor={this.props.theme.textLight}
                                opacity={"0.5"}
                                glyph={Icon.GLYPHS.closeLight}
                                css={`
                            cursor:pointer;
                          `}
                            />
                        </RawButton>
                    </Box>
                    <nav className={CustomStyleKana.custom_nuv}>
                        かな検索
                    </nav>
                    <Box
                        paddedHorizontally={5}
                        // paddedVertically={10}
                        centered
                        displayInlineBlock
                        className={CustomStyleKana.custom_content}
                    >
                        <div className={CustomStyleKana.box_input_area}>
                            <Input
                                light={true}
                                dark={false}
                                white={true}
                                type="text"
                                value={kanaText}
                                placeholder=""
                                id="kanaText"
                                style={{ width: 100 + "%" }}
                                autocomplete="off"
                                readOnly
                            />
                            <button
                                className={CustomStyleKana.clear_button}
                                onClick={evt => {
                                    evt.preventDefault();
                                    evt.stopPropagation();
                                    this.setState({ kanaText: "" });
                                    this.setState({ filteredList: [] });
                                }}
                                id="clearBtn"
                                style={{ width: 30 + "%", lineHeight: 2 + "em", }}
                            >
                                <span>クリア</span>
                            </button>
                        </div>
                        <div className={CustomStyleKana.kana_keyboard_area}>
                            {
                                Object.keys(keyBoardList).map(idx => (
                                    <div
                                        className={CustomStyleKana.kana_row} key={"keyBoardList"+idx}
                                    >
                                        {Object.keys(keyBoardList[idx]).map(idx2 => (
                                            <div
                                                className={keyBoardList[idx][idx2] === "" ? CustomStyleKana.kana_cell_empty : CustomStyleKana.kana_cell}
                                                onClick={
                                                    evt => {
                                                        evt.preventDefault();
                                                        evt.stopPropagation();
                                                        if (keyBoardList[idx][idx2] != "") {
                                                            this.inputKanaText(keyBoardList[idx][idx2]);
                                                        }
                                                    }
                                                }
                                                key={"kana"+idx2}
                                            >{keyBoardList[idx][idx2]}
                                            </div>
                                        ))}
                                    </div>
                                ))
                            }
                        </div>
                        <div className={CustomStyleKana.district_name_area}>
                            <table
                                className={CustomStyleKana.district_list_table}
                            >
                                <tbody>
                                    {Object.keys(filteredList).map(idx => (
                                        <tr className={CustomStyleKana.district_list_table_row} key={"filteredList"+idx}>
                                            <td
                                                onClick={evt => {
                                                    evt.preventDefault();
                                                    evt.stopPropagation();
                                                    this.selectDistrictName(filteredList[idx].id, filteredList[idx].name);
                                                }
                                                }
                                            >{filteredList[idx].name}</td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                        <div className={CustomStyleKana.select_btn_area}>
                        </div>
                    </Box>
                </Box>
            );
        })

        // 町丁名検索コンポーネント　memo化（不要な再レンダリングを防止）
        if (this.props.viewState.DistrictNameSearch === "") {
            this.props.viewState.setDistrictNameSearch(React.memo(props => {
                console.log("React.memo DistrictNameSearch");
                const districtList = props.districtList;
                return (<Box
                    displayInlineBlock
                    backgroundColor={this.props.theme.textLight}
                    styledWidth={"300px"}
                    styledHeight={"300px"}
                    fullHeight
                    overflow={"auto"}
                    id="DistrictNameSearchFrame"
                    css={`
                        position: absolute;
                        z-index: 9988;
                        top: 54%;
                        left: 45%;
                        `}
                    className={CustomStyleDistrict.custom_frame}>
                    <div
                        className={CustomStyleDistrict.content_col}
                    ><Text>町名・町丁名</Text></div>
                    <div
                        className={CustomStyleDistrict.content_col}
                    >
                        <div style={{ textAlign: "right" }}>
                            <RawButton onClick={() => {
                                this.props.viewState.hideKanaDistrictNameSearchView();
                                this.props.viewState.hideDistrictNameSearchView();
                            }}>
                                <StyledIcon
                                    styledWidth={"16px"}
                                    fillColor={this.props.theme.textLight}
                                    opacity={"0.5"}
                                    glyph={Icon.GLYPHS.closeLight}
                                    css={`
                    cursor:pointer;
                    float: right;
                  `}
                                />
                            </RawButton>
                        </div>
                        <div className={CustomStyleDistrict.district_list_table_wrapper}>
                            <table
                                className={CustomStyleDistrict.district_list_table}
                            >
                                <tbody>
                                    {Object.keys(districtList).map(idx => (
                                        <tr className={CustomStyleDistrict.district_list_table_row} key={"districtList"+idx}>
                                            <td
                                                onClick={evt => {
                                                    evt.preventDefault();
                                                    evt.stopPropagation();
                                                    this.selectDistrictName(districtList[idx].id, districtList[idx].name);
                                                }
                                                }
                                            >{districtList[idx].name}</td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                        <div>
                            <button
                                className={CustomStyleDistrict.custom_button}
                                onClick={evt => {
                                    this.props.viewState.showKanaDistrictNameSearchView();
                                }}
                            >かな検索</button>
                        </div></div>
                </Box>);
            }, (prevProps, nextProps) => {
                return prevProps?.districtList[0]?.id === nextProps.districtList[0]?.id
            }))
        }

        // 検索結果一覧 memo化（不要な再レンダリングを防止）
        if (this.props.viewState.SearchRsultContent === "") {
            this.props.viewState.setSearchRsultContent(React.memo(props => {
                console.log("React.memo SearchRsultContent");
                const searchResult = props.searchResult;
                const table = props.table;
                const isViewIndependent = props.isViewIndependent;
                let flag = true;
                if(!this.props.terria.authorityJudgment()){
                    flag = props.isClickEvent;
                }
                if(flag){
                this.props.viewState.setIsClickEvent();
                return (<>
                    {Object.keys(searchResult).map(idx => {
                        let flg = this.checkSelected(searchResult[idx]);
                        return (<tr key={searchResult[idx].chibanId} onClick={
                                        evt => {
                                            if(evt.target.type !== 'checkbox'){
                                            this.focusMapPlace(searchResult[idx]?.maxlon,searchResult[idx]?.maxlat,searchResult[idx]?.minlon,searchResult[idx]?.minlat,searchResult[idx]?.lon, searchResult[idx]?.lat, false);
                                            }
                                        }
                                    }
                                >
                            {/* {!isViewIndependent && <td style={{ width: "8%" }}> */}
                            {<td style={{ width: "7%" }}>
                                <input type="checkbox"
                                    onClick={
                                        evt => {
                                            this.switchSelectLotNumber(searchResult[idx]);
                                            this.changeApplicationPlace(searchResult[idx]?.maxlon,searchResult[idx]?.maxlat,searchResult[idx]?.minlon,searchResult[idx]?.minlat,searchResult[idx]?.lon, searchResult[idx]?.lat);
                                        }
                                    }
                                    style={{ display: "block", margin: "0 auto", width:"40px" }}
                                    checked={flg}
                                ></input>
                            </td>}
                            {Object.keys(table).map(tableKey => (
                                <>
                                    {searchResult[idx]?.attributes && table[tableKey]?.responseKey &&
                                        <td key={"table" + tableKey} style={{ width: table[tableKey]?.tableWidth + "%" }}>{searchResult[idx]?.attributes[table[tableKey]?.responseKey]}</td>
                                    }
                                </>
                            ))}
                        </tr>)
                    })}
                </>);
                }else{
                    return (<></>);
                }
            }, (prevProps, nextProps) => {
                return (prevProps?.searchResult[0]?.chibanId === nextProps?.searchResult[0]?.chibanId
                        && prevProps?.selectLotCount === nextProps?.selectLotCount
                        && prevProps?.table.length === nextProps?.table.length )
            }))
        }
    }

    /**
     * 初期描画
     */
    componentDidMount() {
        this.props.viewState.triggerResizeEvent();
        document.getElementById("customloader").style.display = "none"
        // テーブル定義取得
        fetch(Config.config.apiUrl + "/lotnumber/columns")
            .then(res => res.json())
            .then(res => {
                if(res.status === 401){
                    alert("認証情報が無効です。ページの再読み込みを行います。");
                    if(this.props.terria.authorityJudgment()){
                        window.location.href = "./login/";
                    }else{
                        window.location.reload();
                    }
                    return null;
                }
                if (Object.keys(res).length > 0 && !res.status) {
                    this.setState({ table: res });
                } else {
                    alert("地番情報のテーブル定義の取得に失敗しました。再度操作をやり直してください。");
                }
            }).catch(error => {
                console.error('通信処理に失敗しました', error);
                alert('通信処理に失敗しました');
            });
        //町丁目一覧取得
        fetch(Config.config.apiUrl + "/lotnumber/districts")
            .then(res => res.json())
            .then(res => {
                if(res.status === 401){
                    alert("認証情報が無効です。ページの再読み込みを行います。");
                    if(this.props.terria.authorityJudgment()){
                        window.location.href = "./login/";
                    }else{
                        window.location.reload();
                    }
                    return null;
                }
                if (Object.keys(res).length > 0) {
                    this.setState({ districtList: res });
                } else {
                    alert("町丁名一覧の取得に失敗しました。再度操作をやり直してください。");
                }
            }).catch(error => {
                console.error('通信処理に失敗しました', error);
                alert('通信処理に失敗しました');
            });

        if(this.props.viewState.showInitAndLotNumberSearchView){
            let searchDistrictId = this.props.viewState.searchDistrictId;
            let searchLotNum = this.props.viewState.searchLotNum;
            if ((!searchDistrictId || searchDistrictId === "") && (!searchLotNum || searchLotNum === "")) {
            }else{
                // this.setState({ lotNumber: searchLotNum });
                this.searchLotNumber();
            }
        }

    }

    /*
    * 地番検索
    */
    searchLotNumber() {
        this.setState({ searchResult: [], disabled: true });
        document.getElementById("customloader").style.display = "block";
        let path = "/lotnumber/search/estabrishment";
        if (this.props.terria.authorityJudgment()) {
            path = "/lotnumber/search/estabrishment";
        }
        if ((!this.props.viewState.searchDistrictId || this.props.viewState.searchDistrictId === "") &&
            (!this.props.viewState.searchLotNum || this.props.viewState.searchLotNum === "")) {
            document.getElementById("customloader").style.display = "none";
            alert("町丁名または地番は必須入力です。");
            this.setState({ disabled: false });
            return false;
        }
        //地番検索を実施
        fetch(Config.config.apiUrl + path, {
            method: 'POST',
            body: JSON.stringify({
                districtId: this.props.viewState.searchDistrictId,
                chiban: this.props.viewState.searchLotNum
            }),
            headers: new Headers({ 'Content-type': 'application/json' }),
        })
            .then(res => res.json())
            .then(res => {
                if(res.status === 401){
                    alert("認証情報が無効です。ページの再読み込みを行います。");
                    if(this.props.terria.authorityJudgment()){
                        window.location.href = "./login/";
                    }else{
                        window.location.reload();
                    }
                    return null;
                }
                if (Object.keys(res).length > 0 && !res.status) {
                    if (!this.props.viewState.showMapSelection) {
                        let layrerName = "";
                        let displayName = "";
                        let paramName = "";
                        let layerFlg = false;
                        layrerName = Config.layer.lotnumberSearchLayerNameForBusiness;
                        displayName = Config.layer.lotnumberSearchLayerDisplayNameForBusiness;
                        paramName = Config.layer.lotnumberSearchViewParamNameForBusiness;
                        const wmsUrl = Config.config.geoserverUrl;
                        const items = this.state.terria.workbench.items;
                        try {
                            for (const aItem of items) {
                                if (aItem.uniqueId === layrerName) {
                                    aItem.setTrait(CommonStrata.user,
                                        "parameters",
                                        {
                                            "viewparams": paramName + res?.map(obj => { return obj.chibanId }).filter(chibanId => { return chibanId !== null }).join("_"),
                                        });
                                    aItem.loadMapItems();
                                    layerFlg = true;
                                }
                            }
                            if(!layerFlg){
                                const item = new webMapServiceCatalogItem(layrerName, this.state.terria);
                                item.setTrait(CommonStrata.definition, "url", wmsUrl);
                                item.setTrait(CommonStrata.user, "name", displayName);
                                item.setTrait(CommonStrata.user, "allowFeaturePicking", true);
                                item.setTrait(
                                    CommonStrata.user,
                                    "layers",
                                    layrerName);
                                item.setTrait(CommonStrata.user,
                                    "parameters",
                                    {
                                        "viewparams": paramName + res?.map(obj => { return obj.chibanId }).filter(chibanId => { return chibanId !== null }).join("_"),
                                    });
                                item.loadMapItems();
                                this.state.terria.workbench.add(item);
                            }
                        } catch (error) {
                            console.error('処理に失敗しました', error);
                        }
                    }
                    if(!this.props.terria.authorityJudgment()){
                        this.props.viewState.setIsClickEvent();
                        this.props.viewState.changeShowLotNumberSelectedFlg(true);
                    }
                    this.setState({ searchResult: res });
                } else if (res.status) {
                    this.setState({ searchResult: [] });
                    alert(res.status + "エラーが発生しました");
                } else {
                    this.setState({ searchResult: [] });
                    alert("検索結果はありません");
                }
            }).catch(error => {
                this.setState({ searchResult: [] });
                console.error('処理に失敗しました', error);
                alert('処理に失敗しました');
            }).finally(() => {
                document.getElementById("customloader").style.display = "none";
                this.setState({ disabled: false });
                if (Object.keys(this.state.searchResult).length > 5000) {
                    alert("該当件数が5,000件を超えました。\n最初の5,000件までを表示します。");
                }
            });;
    }

    /**
     * フォーカス処理
     * @param {number} maxLon 最大経度
     * @param {number} maxLat 最大緯度
     * @param {number} minLon 最小経度
     * @param {number} minLat 最小緯度
     * @param {number} lon 経度
     * @param {number} lat 緯度
     * @param {boolean} isSelected 選択中地番であるか
     */
     focusMapPlace(maxLon, maxLat, minLon, minLat, lon, lat, isSelected) {
        this.props.terria.focusMapPlace(maxLon, maxLat, minLon, minLat, lon, lat, this.props.viewState);
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
     * 地番の選択状態を切り替える
     * @param {Object} searchResultObj 対象地番
     */
    switchSelectLotNumber(searchResultObj) {
        this.props.viewState.switchLotNumberSelect(searchResultObj);
    }

    /**
     * 町丁名選択結果を親コンポーネントに返す
     * @param {string} districtId 町丁ID
     * @param {string} districtName 町丁名
     */
    selectDistrictName(districtId, districtName) {
        this.props.viewState.setDistrictName(districtId, districtName);
        this.props.viewState.hideKanaDistrictNameSearchView();
        this.props.viewState.hideDistrictNameSearchView();

    }

     /**
     * かなキーボード入力
     * @param {string} txt キーボードで選択したかな
     */
    inputKanaText(txt) {
        const currentText = this.state.kanaText;
        const newText = currentText.concat(txt)
        this.setState({ kanaText: newText });
        this.filterByInput(newText);
    }

    /**
     * リストをかな入力でフィルタする
     * @param {string} inputTxt 入力したかな町丁名
     */
    filterByInput(inputTxt) {
        const districtList = this.state.districtList;
        if (inputTxt !== "") {
            const filteredList = districtList.filter((aDist) => aDist.kana.startsWith(inputTxt));
            this.setState({ filteredList: filteredList });
        } else {
            this.setState({ filteredList: [] });
        }
    }

    /**
     * close処理
     */
    close() {
        this.clearSamplePoint();
        const items = this.state.terria.workbench.items;
        for (const aItem of items) {
            if (aItem.uniqueId === Config.layer.lotnumberSearchLayerNameForBusiness || aItem.uniqueId === Config.layer.lotnumberSearchLayerNameForGoverment) {
                this.state.terria.workbench.remove(aItem);
                aItem.loadMapItems();
            }
        }
        this.props.viewState.hideLotNumberSearchView();
    }

    /**
     * clear処理
     */
    clear() {
        this.props.viewState.setDistrictName(null, null);
        document.getElementById("districtName").value = "";
        this.setState({ lotNumber: "" });
        this.props.viewState.setLotNumber(null);
    }

    /**
     * 地番選択
     * @param {*} maxLon 最大経度
     * @param {*} maxLat 最大緯度
     * @param {*} minLon 最小経度
     * @param {*} minLat 最小緯度
     * @param {*} lon 経度
     * @param {*} lat 緯度
     */
    changeApplicationPlace(maxLon, maxLat, minLon, minLat, lon, lat){
        
        if(!this.props.terria.authorityJudgment()){
            // 事業者側：選択済み申請地の変更処理
            this.props.viewState.changeApplicationPlace();
        }else{
            // 行政側、選択された地番をハイライトで表示する
            try{
                const item = new webMapServiceCatalogItem(Config.layer.lotnumberSearchLayerNameForSelected, this.state.terria);
                const wmsUrl = Config.config.geoserverUrl;
                const items = this.state.terria.workbench.items;
                for (const aItem of items) {
                    if (aItem.uniqueId === Config.layer.lotnumberSearchLayerNameForSelected) {
                        this.state.terria.workbench.remove(aItem);
                        aItem.loadMapItems();
                    }
                }
                let selectLotSearchResult = this.props.viewState.selectLotSearchResult;
                selectLotSearchResult = selectLotSearchResult.filter(Boolean);
                if(selectLotSearchResult.length > 0){
                    item.setTrait(CommonStrata.definition, "url", wmsUrl);
                    item.setTrait(CommonStrata.user, "name", Config.layer.lotnumberSearchLayerDisplayNameForSelected);
                    item.setTrait(
                        CommonStrata.user,
                        "layers",
                        Config.layer.lotnumberSearchLayerNameForSelected);
                    item.setTrait(CommonStrata.user,
                        "parameters",
                        {
                            "viewparams": Config.layer.lotnumberSearchViewParamNameForSelected + Object.keys(selectLotSearchResult)?.map(key => { return this.props.viewState.selectLotSearchResult[key].chibanId }).filter(chibanId => {return chibanId !== null}).join("_"),
                        });
                    item.loadMapItems();
                    this.state.terria.workbench.add(item); 
                }
            }catch(e){
                console.error('処理に失敗しました', error);
            }
        }
        this.focusMapPlace(maxLon, maxLat, minLon, minLat, lon, lat, true);
    }

    /**
     * 地番が選択中であるか判断
     * @param {*} lotObj 地番Obj
     * @returns 
     */
    checkSelected(lotObj){
        const index = this.props.viewState.selectLotSearchResult.findIndex(obj => 
            obj.chibanId == lotObj.chibanId
        );

        return (index != -1)? true : false;
    }

    render() {
        const districtName = this.props.viewState.searchDistrictName;
        const topDiff = this.props.viewState.lotNumberSearchPosDiffTop;
        const leftDiff = this.props.viewState.lotNumberSearchPosDiffLeft;
        const isViewIndependent = this.props.viewState.islotNumberSearchViewIndependent;
        const lotNumber = this.props.viewState.searchLotNum
        let searchResult = this.state.searchResult;
        const table = this.state.table;
        const disabled = this.state.disabled;
        const SearchRsultContent = this.props.viewState.SearchRsultContent;
        const DistrictNameSearch = this.props.viewState.DistrictNameSearch;
        const districtList = this.state.districtList;
        const kanaText = this.state.kanaText;
        const filteredList = this.state.filteredList;
        const KanaDistrictNameSearch = this.props.viewState.KanaDistrictNameSearch;
        const boxCssText = `
            position: fixed;
            z-index: 9987;
            top: ` + (60 + topDiff) + `%;
            left: ` + (30 + leftDiff) + `%;
            `;
        //検索結果数 一度に表示できる最大件数は5000件を限度とする
        let searchResultCount = 0;
        if (Object.keys(searchResult).length > 5000) {
            searchResultCount = 5000;
        } else {
            searchResultCount = Object.keys(searchResult).length;
        }

        let isClickEvent = this.props.viewState.isClickEvent;
        if(!isClickEvent){
            searchResult = this.props.viewState.searchResult;
        }
        // 地番検索結果のチェックボックスの選択状態は申請地番選択結果と一致にするように、
        // 検索結果のメモ更新制御用変数を作成する
        let selectLotCount = this.props.viewState.selectLotSearchResult.length;
        return (
            <>
                <Box
                    // レイアウト変更のために
                    displayInlineBlock
                    backgroundColor={this.props.theme.textLight}
                    styledHeight={"380px"}
                    fullHeight
                    fullWidth
                    id="LotNumberSearchFrame"
                >
                    {this.props.viewState.showDistrictNameSearch && (
                        <DistrictNameSearch districtList={districtList} />
                    )}
                    {this.props.viewState.showKanaDistrictNameSearch && (
                        <KanaDistrictNameSearch kanaText={kanaText} filteredList={filteredList} />
                    )}
                    <Box
                        column
                        paddedRatio={3}
                        topRight
                        styledHeight="110px"
                        styledPadding="5px"
                        className={CustomStyle.custom_header}
                        id="LotNumberSearchFrameDrag"
                    >
                        <Box styledHeight={"45%"} styledWidth={"100%"}>
                            <div style={{ width: 20 + "%", height: 100 + "%" }}><nav>地番検索</nav></div>
                            <div style={{ width: 10 + "%", height: 100 + "%", textAlign: "right", verticalAlign: "middle" }}>
                                <Text style={{ lineHeight: 2 + "em" }}>町丁名</Text></div>
                            <div style={{ width: 45 + "%", padding: 5 + "px", height: 100 + "%" }}>
                                <Input
                                    light={true}
                                    dark={false}
                                    white={true}
                                    type="text"
                                    value={districtName ?? ''}
                                    placeholder="一覧から町丁名を選択"
                                    id="districtName"
                                    style={{ width: 100 + "%" }}
                                    onFocus={evt => {
                                        evt.preventDefault();
                                        evt.stopPropagation();
                                        this.props.viewState.showDistrictNameSearchView();
                                    }}
                                    autoComplete="off"
                                    readOnly
                                /></div>
                            <div style={{ width: 20 + "%", padding: 5 + "px", height: 100 + "%" }} id="lotNumberClearBtn">
                                <button
                                    className={CustomStyle.clear_button_in_header}
                                    onClick={evt => {
                                        evt.preventDefault();
                                        evt.stopPropagation();
                                        this.clear();
                                    }}
                                    disabled={disabled}
                                >
                                    <span>クリア</span>
                                </button>
                            </div>
                            <div style={{ width: 5 + "%", padding: 5 + "px", height: 100 + "%", textAlign: "right" }}>
                            </div>
                        </Box>
                        <Box styledHeight={"45%"} styledWidth={"100%"}>
                            <div style={{ width: 20 + "%", height: 100 + "%" }}>&nbsp;</div>
                            <div style={{ width: 10 + "%", height: 100 + "%", textAlign: "right", verticalAlign: "middle" }}>
                                <Text style={{ lineHeight: 2 + "em" }}>地番</Text>
                            </div>
                            <div style={{ width: 45 + "%", padding: 5 + "px", height: 100 + "%" }}>
                                <Input
                                    light={true}
                                    dark={false}
                                    white={true}
                                    type="text"
                                    value={lotNumber ?? ''}
                                    placeholder="地番を入力(例:1-1-1)"
                                    id="lotNumber"
                                    style={{ width: 100 + "%" }}
                                    onChange={e => this.props.viewState.setLotNumber( e.target.value)}
                                    
                                />
                            </div>
                            <div style={{ width: 27 + "%", padding: 5 + "px", height: 100 + "%" }} id="searchBtn">
                                <button
                                    className={CustomStyle.button_in_header}
                                    onClick={evt => {
                                        evt.preventDefault();
                                        evt.stopPropagation();
                                        this.props.viewState.removeLosNumberSelect();
                                        this.searchLotNumber();
                                    }}
                                    disabled={disabled}
                                >
                                    <span>検索</span>
                                </button>
                            </div>
                        </Box>
                        <div style={{ position: "relative", top: -10 + "px", fontSize: ".7em", width: "150px" }}>検索結果件数：{Number(searchResultCount).toLocaleString()} 件</div>
                    </Box>
                    <Box
                        column
                        centered
                        displayInlineBlock
                        className={CustomStyle.custom_content}
                    >
                        <div id="customloader" className={CustomStyle.customloaderParent}>
                            <img className={CustomStyle.customloader} src="./images/loader.gif" />
                        </div>
                        <table className={CustomStyle.result_table}>
                            <thead>
                                {Object.keys(table).length > 0 && (
                                    <tr className="add-sort">
                                        <th className="no-sort" style={{ width: "8%" }}></th>
                                        {Object.keys(table).map(tableKey => (
                                            <th key={"tableKey"+tableKey} style={{ width: table[tableKey].tableWidth + "%" }}>{table[tableKey].displayColumnName}</th>
                                        ))}
                                    </tr>
                                )}
                            </thead>
                            <tbody>
                                {SearchRsultContent &&
                                    <SearchRsultContent table={table} searchResult={searchResult} isClickEvent={isClickEvent} selectLotCount={selectLotCount}/>
                                }
                            </tbody>
                        </table>
                    </Box>
                    <Spacing bottom={1} />
                </Box>
            </>
        );
    }
}
export default withTranslation()(withTheme(LotNumberSearch));