import { observer } from "mobx-react";
import React from "react";
import PropTypes from "prop-types";
import { withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import Icon from "../../../../Styled/Icon";
import Box from "../../../../Styled/Box";
import Spacing from "../../../../Styled/Spacing";
import CustomStyle from "./scss/lot-number-search.scss";
import CommonStrata from "../../../../Models/Definition/CommonStrata";
import Config from "../../../../../customconfig.json";
import webMapServiceCatalogItem from '../../../../Models/Catalog/Ows/WebMapServiceCatalogItem';
import CzmlCatalogItem from '../../../../Models/Catalog/CatalogItems/CzmlCatalogItem';
import { BaseModel } from "../../../../Models/Definition/Model";

/**
 * 地番選択結果表示コンポーネント
 */
@observer
class LotNumberSelect extends React.Component {
    static displayName = "LotNumberSelect";

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
            //地番情報のテーブル定義
            table: [],
        };
    }
    
    /**
     * 初期描画
     */
    componentDidMount() {
        //サーバからデータを取得
        fetch(Config.config.apiUrl + "/lotnumber/columns")
        .then(res => res.json())
        .then(res => {
            if(res.status === 401){
                alert("認証情報が無効です。ページの再読み込みを行います。");
                window.location.reload();
                return null;
            }
            if (Object.keys(res).length > 0) {
                this.setState({table: res});
            }else{
                alert("地番情報のテーブル定義の取得に失敗しました。再度操作をやり直してください。");
            }
        }).catch(error => {
            console.error('通信処理に失敗しました', error);
            alert('通信処理に失敗しました');
        });
        
    }

    /**
     * コンポーネント更新イベント
     */
    componentDidUpdate(){
        this.updateMapLayer();
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

    /**
     * 選択状態の申請地を削除
     * @param {any} 削除対象
     */
    deleteApplicationPlace(applicationPlace){
        this.props.viewState.deleteApplicationPlace(applicationPlace);
        this.updateMapLayer();
    }

    /**
     * 選択状態の申請地を全て削除
     */
    deleteAllApplicationPlace(){
        this.props.viewState.deleteAllApplicationPlace();
        this.updateMapLayer();
    }

    render() {

        const applicationPlace = this.props.viewState.applicationPlace;       
        const table = this.state.table;

        return (
            <>
                <Box id="LotNumberSelectedFrame"
                    backgroundColor={this.props.theme.textLight}
                    css={`display:block`}
                >
                    <Box
                        column
                        paddedRatio={3}
                        topRight
                        styledPadding="5px"
                        className={CustomStyle.custom_header}
                    >
                        <Box className={CustomStyle.custom_header_content}>
                            <div style={{ height: 100 + "%" }}><nav>申請地番選択結果</nav></div>
                            <div>
                                <button className={CustomStyle.trash_all_button} 
                                    disabled={Object.keys(applicationPlace).length > 0? false: true}
                                    onClick={() => {
                                        this.deleteAllApplicationPlace();
                                    }}>
                                    <span>全て削除</span>
                                </button>
                            </div>
                        </Box>
                    </Box>
                    
                    <Box
                        centered
                        displayInlineBlock
                        className={CustomStyle.custom_content}
                    >
                        <table className={CustomStyle.result_table}>
                            <thead>
                                {Object.keys(table).length > 0 && (
                                    <tr className={CustomStyle.table_header}>
                                        {Object.keys(table).map(tableKey => (
                                            <th key={"applicationPlace-th" + tableKey} style={{ width: table[tableKey].tableWidth + "%" }}>{table[tableKey].displayColumnName}</th>
                                        ))}
                                        <th style={{ width: "10%" }}><div style={{ width: "70px" }}></div></th>
                                    </tr>
                                )}
                            </thead>
                            <tbody style={{height: 180 + "px"}}>
                                {Object.keys(applicationPlace).map(idx => (
                                    <tr key={"applicationPlace-tr" + idx} >
                                        {Object.keys(table).map(tableKey => (
                                            <>
                                                {applicationPlace[idx]?.attributes && table[tableKey]?.responseKey &&
                                                    <td key={"applicationPlace-tr-td" + idx + tableKey} style={{ width: table[tableKey]?.tableWidth + "%" }}>{applicationPlace[idx]?.attributes[table[tableKey]?.responseKey]}</td>
                                                }
                                            </>
                                        ))}
                                        <td style={{ width: "10%", textAlign:"center" }}><button className={CustomStyle.trash_button} onClick={() => {
                                            this.deleteApplicationPlace(applicationPlace[idx]);
                                        }}><Icon style={{ fill: "#fff", height: 25 + "px", display: "block", margin: "0 auto" }} glyph={Icon.GLYPHS.trashcan} />
                                        </button></td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </Box>
                    <Spacing bottom={1} />
                </Box>
            </>
        );
    }
}

export default withTranslation()(withTheme(LotNumberSelect));