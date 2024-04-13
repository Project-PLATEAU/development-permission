import { observer } from "mobx-react";
import PropTypes from "prop-types";
import React from "react";
import { withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import proxyCatalogItemUrl from "../../../../Models/Catalog/proxyCatalogItemUrl";
import URI from "urijs";
import customStyle from "./scss/layer-legend.scss";

const IMAGE_URL_REGEX = /[.\/](png|jpg|jpeg|gif|svg)/i;

/**
 * レイヤの凡例表示コンポーネント
 */
@observer
class LayerLegend extends React.Component {
    static displayName = "LayerLegend";
    static propTypes = {
        item: PropTypes.object
    }
    constructor(props) {
        super(props);
        this.state = {
            item: props.item
        };
    }
    
    /**
     * カタログ定義より、凡例表示用srcを編集する
     * @returns 
     */
    loadImage(){
      let catalogItem = this.state.item;
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
            urls.push(this.editUrl());
          }
        });
      }else{
        if(url?.match(IMAGE_URL_REGEX) && type != "wms" ){
          urls.push(url);
        }else{
          urls.push(this.editUrl());
        }
      }
      return urls;
    }

    /**
     * GetLegendGraphicで凡例画像を取得する用リクエストを編集する
     * @returns リクエスト
     */
    editUrl(){
      let catalogItem = this.state.item;
      let layer = this.state.item.layers;
      let style = this.state.item.styles;
      let url = this.state.item.url;

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

    render() {
      let srcs = this.loadImage();
      return (
        <>
          {Object.keys(srcs).map(key => (
            <a href={srcs[key]} className={customStyle.legends} target="_blank" rel="noreferrer noopener">
              <img src={srcs[key]} className={customStyle.legends}></img>
            </a>
          ))}
        </>
      );
    }
}
export default withTranslation()(withTheme(LayerLegend));