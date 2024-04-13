import React from "react";
import PropTypes from "prop-types";
import { observer } from "mobx-react";
import { withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import Box from "../../../../Styled/Box";
import Styles from "./scss/layer-sort-setting-modal.scss"
import { RawButton } from "../../../../Styled/Button";
import Icon, { StyledIcon } from "../../../../Styled/Icon";
import Spacing from "../../../../Styled/Spacing";
import LayerLegend from "./LayerLegend";
import proxyCatalogItemUrl from "../../../../Models/Catalog/proxyCatalogItemUrl";
import URI from "urijs";

const IMAGE_URL_REGEX = /[.\/](png|jpg|jpeg|gif|svg)/i;
/**
 * レイヤ表示順設定モダール
 */

@observer
class LayerSortSettingModal extends React.Component {
    static displayName = "LayerSortSettingModal"
    static propsType = {
        terria: PropTypes.object.isRequired,
        viewState: PropTypes.object.isRequired,
        t: PropTypes.func.isRequired
    }

    constructor(props) {
        super(props);
        this.state = {
            viewState: props.viewState,
            terria: props.terria,
            // 表示しているレイヤリスト
            layerItems:[],
            wmsLayerItems:[]
        }
    }

    /**
     * 初期表示
     */
    componentDidMount() {
        let layerItems = [];
        let wmsLayerItems = [];
        const items = this.state.terria.workbench.items;
        for (const aItem of items) {
            if(aItem.type == "wms"){
                wmsLayerItems.push(aItem);
            }else{
                layerItems.push(aItem);
            }
        }

        this.setState({layerItems:layerItems, wmsLayerItems:wmsLayerItems});

        this.draggable(document.getElementById('layerSortSettingModalDrag'), document.getElementById('fileUploadModal'));
    }

    /**
     * コンポーネントドラッグ操作
     * @param {Object} ドラッグ操作対象
     * @param {Object} ドラッグ対象
     */
    draggable(target, content) {
        target.onmousedown = function () {
            document.onmousemove = mouseMove;
        };
        document.onmouseup = function () {
            document.onmousemove = null;
        };
        function mouseMove(e) {
            var event = e ? e : window.event;
            content.style.top = event.clientY + 10 +  'px';
            content.style.left = event.clientX - (parseInt(content.clientWidth) / 2) + 'px';
        }
    }

    /**
     * カタログ定義より、凡例表示用srcを編集する
     * @returns 
     */
    loadImage(catalogItem){
        let url = catalogItem.url;
        let type = catalogItem.type;
        let legends = catalogItem.legends;
        let urls = [];
        if(Object.keys(legends).length > 0){
            Object.keys(legends).map(key => {
                let str = legends[key].url
                if(str?.match(IMAGE_URL_REGEX) && type != "wms" ){
                    urls.push(str);
                }else{
                    urls.push(this.editUrl(catalogItem));
                }
            });
        }else{
            if(url?.match(IMAGE_URL_REGEX) && type != "wms" ){
                urls.push(url);
            }else{
                urls.push(this.editUrl(catalogItem));
            }
        }
        return urls;
    }
  
    /**
     * GetLegendGraphicで凡例画像を取得する用リクエストを編集する
     * @returns リクエスト
     */
    editUrl(catalogItem){
        let layer = catalogItem.layers;
        let url = catalogItem.url;

        let legendUri = URI(proxyCatalogItemUrl(
            catalogItem,url.split("?")[0]
        ));

        legendUri.setQuery("service", "WMS")
        .setQuery("version", "1.3.0")
        .setQuery("request", "GetLegendGraphic")
        .setQuery("format", "image/png")
        .setQuery("layer", layer)
        .setQuery("WIDTH", "20")
        .setQuery("HEIGHT","20");
    
        let src = this.makeAbsolute(legendUri.toString());
        return src;
    }
  
      /**
       * URL編集
       * @param {String} url 
       * @returns 
       */
      makeAbsolute(url) {
        const uri = new URI(url);
        if (
          uri.protocol() &&
          uri.protocol() !== "http" &&
          uri.protocol() !== "https"
        ) {
          return url;
        } else {
          return uri.absoluteTo(window.location.href).toString();
        }
      }

    /**
     * モーダルを閉じる
     * 
     */
    close(){
        try{
            // 一旦レイヤをすべて削除
            this.state.terria.workbench.removeAll();
            
            // 新しい順番で、(WMSレイヤ)追加します。
            let wmsLayerItems = this.state.wmsLayerItems;
            let count = Object.keys(wmsLayerItems).length;
            
            Object.keys(wmsLayerItems).map(key => {
                let index = Number(key) + 1;
                let item = wmsLayerItems[count - index];
                item.loadMapItems();
                this.state.terria.workbench.add(item); 
            });

            // 新しい順番で、(WMSレイヤ以外)追加します。
            let layerItems = this.state.layerItems;
            count = Object.keys(layerItems).length;
            
            Object.keys(layerItems).map(key => {
                let index = Number(key) + 1;
                let item = layerItems[count - index];
                item.loadMapItems();
                this.state.terria.workbench.add(item); 
            });

        }catch(e){
            console.error('処理に失敗しました', error);
        }

        this.state.viewState.changeLayerSortSettingModalShow();
    }

    /**
     * 該当レイヤが上に移動する
     * @param {*} wmsLayerItems 
     * @param {*} index 
     */
    movingUp(wmsLayerItems, index){
        let i = Number(index);
        // 要素を入れ替える
        wmsLayerItems.splice(index-1, 2, wmsLayerItems[i], wmsLayerItems[i-1]);

        this.setState({wmsLayerItems: wmsLayerItems});
    }

    /**
     * 該当レイヤが下に移動する
     * @param {*} wmsLayerItems 
     * @param {*} index 
     */
    movingDown(wmsLayerItems, index){
        let i = Number(index);
        // 要素を入れ替える
        wmsLayerItems.splice(index, 2, wmsLayerItems[i+1], wmsLayerItems[i]);

        this.setState({wmsLayerItems: wmsLayerItems});
    }

    render(){
        // 地番検索結果（事業者）レイヤを表示にする
        let layerItems = this.state.layerItems;
        let wmsLayerItems = this.state.wmsLayerItems;
        let count = Object.keys(wmsLayerItems).length;
        return (
            <div className={Styles.overlay}>
                <div className={Styles.modal}  id="layerSortSettingModal">
                    <Box position="absolute" paddedRatio={3} topRight>
                        <RawButton onClick={() => {
                            this.close();
                        }}>
                            <StyledIcon
                                styledWidth={"16px"}
                                fillColor={"#000"}
                                opacity={"0.5"}
                                glyph={Icon.GLYPHS.closeLight}
                                css={`cursor:pointer;`}
                            />
                        </RawButton>
                    </Box>
                    <nav className={Styles.custom_nuv} id="layerSortSettingModalDrag">
                        レイヤ表示順設定
                    </nav>
                    <div className={Styles.container}>
                        <p>現在表示中の各レイヤ右の矢印ボタンを操作することで、レイヤの表示順が変更できます。</p>
                        <Spacing bottom={2} />
                        <div className={Styles.scrollContainer}>
                            <table className={Styles.selection_table}>
                                <tbody>
                                    {Object.keys(layerItems).map(index => {
                                        let srcs = this.loadImage(layerItems[index]);
                                        return(
                                            <tr key={`layerItems-${index}`}>    
                                                <td style={{width: "20%"}}>
                                                    <div className={Styles.layer_legend_postion}>
                                                        { Object.keys(srcs).map(key => (
                                                            <img src={srcs[key]} className={Styles.legends}></img>
                                                        ))}
                                                    </div>
                                                </td>
                                                <td style={{width: "50%", textAlign:"left"}}>
                                                    {layerItems[index].nameInCatalog}
                                                </td>
                                                <td style={{width: "15%"}}></td>
                                                <td style={{width: "15%"}}></td>
                                            </tr>
                                        )
                                    })}
                                    {Object.keys(wmsLayerItems).map(index => {
                                        let srcs = this.loadImage(wmsLayerItems[index]);
                                        return(
                                            <tr key={`wmsLayerItems-${index}`}>    
                                                <td style={{width: "20%"}}>
                                                    <div className={Styles.layer_legend_postion}>
                                                        { Object.keys(srcs).map(key => (
                                                            <img src={srcs[key]} className={Styles.legends}></img>
                                                        ))}
                                                    </div>
                                                </td>
                                                <td style={{width: "50%", textAlign:"left"}}>
                                                    {wmsLayerItems[index].nameInCatalog}
                                                </td>
                                                <td style={{width: "15%"}}>
                                                    { index != 0  &&(
                                                        <button className={Styles.arrow_button}
                                                            onClick={e => {this.movingUp(wmsLayerItems, index);}}
                                                        >
                                                            <StyledIcon
                                                                styledWidth={"20px"}
                                                                fillColor={"#FFF"}
                                                                glyph={Icon.GLYPHS.movingUp}
                                                                css={`cursor:pointer;`}
                                                            />
                                                        </button>
                                                    )}
                                                </td>
                                                <td style={{width: "15%"}}>
                                                    { index != count-1 && (
                                                        <button className={Styles.arrow_button}
                                                            onClick={e => {this.movingDown(wmsLayerItems, index);}}
                                                        >
                                                            <StyledIcon
                                                                styledWidth={"20px"}
                                                                fillColor={"#FFF"}
                                                                glyph={Icon.GLYPHS.movingDown}
                                                                css={`cursor:pointer;`}
                                                            />
                                                        </button>
                                                    )}
                                                </td>
                                            </tr>
                                        )
                                    })}
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        )
    }



}
export default withTranslation()(withTheme(LayerSortSettingModal));