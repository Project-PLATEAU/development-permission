import { observer } from "mobx-react";
import PropTypes from "prop-types";
import React, { Component } from 'react';
import { withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import Config from "../../../../../customconfig.json";
import * as markerjs2 from 'markerjs2';
import Tiff from "tiff.js";
import ImageEditAlertModal from "../Modal/ImageEditAlertModal";

/**
 * 行政：画像編集（marker.js 2）
 */
@observer
class ImageEdit extends React.Component {
    static displayName = "ImageEdit";

    static propType = {
        terria: PropTypes.object.isRequired,
        viewState: PropTypes.object.isRequired,
        t: PropTypes.func.isRequired,
        path: PropTypes.string.isRequired,
        name: PropTypes.string.isRequired,
        objectUrl: PropTypes.string,
        editable: PropTypes.bool
    }

    constructor(props){
        super(props);
        const marlerjsAleartChecked = sessionStorage.getItem('marlerjsAleartChecked');
        this.state = {
            filePath: props.path,
            fileName: props.name,
            objectUrl: props.objectUrl,
            src: props.objectUrl || '',
            addFile: props.addFile,
            markerjsAlertModal: true,
            markerjsShow: false,
            editable: props.editable
        };
        this.imgRef = React.createRef();
    }

    /**
     * 初期処理
     */
    componentDidMount() {

      this.setState({markerjsAlertModal:true});

      if (this.state.src) {
      } else if (this.state.filePath) {
          this.getFileUrl(this.state.filePath).then(res => {
              this.setState({ src: res }); 
            });
      }
    }  

    /**
     * コンポーネント変更イベント
     * @param {*} prevProps 
     */
    componentDidUpdate(prevProps) 
    {
      if (this.props.path !== prevProps.path || this.props.name !== prevProps.name || this.props.objectUrl !== prevProps.objectUrl || this.props.editable !== prevProps.editable) {
        this.setState({ fileName: this.props.name, editable: this.props.editable})
        if (this.props.objectUrl) {
          this.setState({ src: this.props.objectUrl }) 
          this.showMarkerArea();
        }else{
          this.getFileUrl(this.props.path).then(res => {
            this.setState({ src: res }, this.showMarkerArea());
          });
        }
      }
    }

    /**
     * ファイルURL取得
     * @param {*} path パス
     * @returns 
     */
    getFileUrl(path) {
      return new Promise((resolve, reject) => {
          if (path) {
              fetch(Config.config.apiUrl + "/file/viewapp" + path)
                  .then(res => {
                      if (res.status === 401) {
                          alert('認証情報が無効です。ページの再読み込みを行います。');
                          window.location.href = "./login/";
                          return null;
                      }
                      return res.blob();
                  })
                  .then(blob => {
                      const ext = this.state.fileName.split('.').pop().toLowerCase();
                      if (ext === "tif" || ext === "tiff") {
                          // TIFF処理
                          const reader = new FileReader();
                          reader.onload = (event) => {
                              const buffer = event.target.result;
                              const tiff = new Tiff({ buffer: buffer });
                              const canvas = tiff.toCanvas();
                              if (canvas) {
                                  const dataUrl = canvas.toDataURL("image/png");
                                  resolve(dataUrl);
                              } else {
                                  reject(new Error("Failed to convert TIFF to PNG"));
                              }
                          };
                          reader.onerror = (error) => {
                              reject(error);
                          };
                          reader.readAsArrayBuffer(blob);
                      } else {
                          // 他の画像形式の処理
                          const reader = new FileReader();
                          reader.onload = (event) => {
                              const dataUrl = event.target.result;
                              resolve(dataUrl);
                          };
                          reader.onerror = (error) => {
                              reject(error);
                          };
                          reader.readAsDataURL(blob);
                      }
                  }).catch(error => {
                      reject(error);
                  })
          } else {
              reject(new Error("Path is not defined"));
          }
      });
  }
    
  /**
   * Markerjsで画像編集エリア表示
   */
    showMarkerArea() {
      if (this.imgRef.current !== null) {
        // create a marker.js MarkerArea
        const markerArea = new markerjs2.MarkerArea(this.imgRef.current);

        // ボタンの追加
        markerArea.uiStyleSettings.redoButtonVisible = true; // 戻るボタン
        markerArea.uiStyleSettings.zoomButtonVisible = true; // 拡大ボタン
        markerArea.uiStyleSettings.zoomOutButtonVisible = true; // 縮小ボタン

        // attach an event handler to assign annotated image back to our image element
        markerArea.addEventListener('render', event => {
            if (this.imgRef.current) {
              this.imgRef.current.src = event.dataUrl;
              this.fileSave();
              this.setState({ addFile: false}, this.props.fileClose(this.state.addFile));
            }
        });

        markerArea.addEventListener('close', event => {
          this.props.fileClose(this.state.addFile);
        })

        // launch marker.js
        markerArea.renderAtNaturalSize = true;
        markerArea.renderImageType = 'image/png';
        markerArea.renderImageQuality = 1.0;
        markerArea.settings.displayMode = 'popup';
        markerArea.show();

        // show tooltip
        const elements = document.querySelectorAll( "[class*='_toolbar_button']");

        if(this.state.editable == false){
          // 回答ファイルが編集不可の場合、ツールボタンが非表示にする
          Array.from(elements).forEach(element => {
            const dataAction = element.getAttribute("data-action");

            if(dataAction !== "close"){
              element.style.display = 'none';
            }
          });

        }else{
          Array.from(elements).forEach(element => {
            const dataAction = element.getAttribute("data-action");
            const typeName = element.getAttribute("data-type-name");
            if(typeName) {
              let typeNameJp;
              // 変換
              switch(typeName){
                case "FrameMarker":
                  typeNameJp = "正方形/長方形";
                  break;
                case "FreehandMarker":
                  typeNameJp = "フリーハンド";
                  break
                case "ArrowMarker":
                  typeNameJp = "矢印";
                  break;
                case "TextMarker":
                  typeNameJp = "テキスト";
                  break;
                case "EllipseMarker":
                  typeNameJp = "円/楕円";
                  break;
                case "HighlightMarker":
                  typeNameJp = "強調";
                  break;
                case "CalloutMarker":
                  typeNameJp = "吹き出し";
                  break;
                default:
                  typeNameJp = typeName;
              }  
              element.setAttribute("data-tippy-content", typeNameJp);
            }
            else if(dataAction){
              let dataActionJp;
              switch(dataAction){
                case "select":
                  dataActionJp = "選択";
                  break;
                case "delete":
                  dataActionJp = "削除";
                  break;
                case "undo":
                  dataActionJp = "戻る";
                  break;
                case "render":
                  dataActionJp = "完了";
                  break;
                case "close":
                  dataActionJp = "閉じる";
                  break;
                default:
                  dataActionJp = dataAction;
              }
              element.setAttribute("data-tippy-content", dataActionJp);
            }
          });
        }
        tippy(elements);

        markerArea.addEventListener('markercreating', event => {
          const elements = document.querySelectorAll( "[class*='_toolbox_button']");
          
          Array.from(elements).forEach(element => {
            const title = element.getAttribute("title");
            if(this.state.editable == false){
              // 回答ファイルが編集不可の場合、ツールボタンが非表示にする
              element.style.display = 'none';
            }else{

              if(title){
                let titleJp;
                switch(title){
                  case "Line color":
                    titleJp = "線色";
                    break;
                  case "Line width":
                    titleJp = "線幅";
                    break;
                  case "Line style":
                    titleJp = "線スタイル";
                    break;
                  case "Color":
                    titleJp = "色";
                    break;
                  case "Arrow type":
                    titleJp = "矢印タイプ";
                    break;
                  case "Font":
                    titleJp = "フォント";
                    break;
                  case "Fill color":
                    titleJp = "塗色";
                    break;
                  case "Opacity":
                    titleJp = "透過度";
                    break;
                  case "Text color":
                    titleJp = "文字色";
                    break;
                  default:
                    titleJp = title;
                }
                element.setAttribute("data-tippy-content", titleJp);
              }
            }
          });
          tippy(elements);
        });
        
      }
    }

    /**
     * ツールバーのボタン押下イベント
     * @param {*} buttonType ボタンタイプ
     * @param {*} value 値
     */
    ToolbarButtonClickHandler = (buttonType, value) => {
      console.log(`${buttonType}：${value}`)      
    }

    /**
     * 編集後画像保存
     */
    fileSave = () => {
      // 編集後データの取得
      const editedImage = document.getElementById("markerJs").src.split(',')[1];
      
      // base64→binary
      const binaryString = window.atob(editedImage);
      
      // binary→blob
      const byteArray = new Uint8Array(binaryString.length);
        for (let i = 0; i < binaryString.length; i++) {
        byteArray[i] = binaryString.charCodeAt(i);
      }
      const blob = new Blob([byteArray], { type: 'application/octet-stream' });
      const fileName = this.props.name.split(".").pop() === "tiff" || this.props.name.split(".").pop() === "tif" ? this.props.name.replace(/\.tiff?$/, '.png') : this.props.name;

      this.props.fileSave(blob, fileName);
    }

    /**
     * 画像ファイル読み込み
     */
    handleImageLoaded = () => {
      this.showMarkerArea();
    }

    /**
     * 画像ファイルをクリック
     */
    handleClick = () => {
      this.setState({markerjsShow:true});
    }

    /**
     * 注意事項モーダルを閉じる
     * @returns 
     */
    modalClose = () => {
      this.setState({markerjsAlertModal: false});
    }

    /**
     * 注意事項確認の完了
     * @returns 
     */
    confirmAlert = () => {
      sessionStorage.setItem('markerjsAleartChecked', 'true');
      this.setState({markerjsShow: true}, this.showMarkerArea());
    }

    render(){
      const { src } = this.state;
      const markerjsAlertChecked = sessionStorage.getItem('markerjsAleartChecked') == 'true';
      return (
        <>
          {
            !markerjsAlertChecked ? (
              <ImageEditAlertModal confirmAlert={this.confirmAlert}/>
            ) : (
              <img id="markerJs" src={src} ref={this.imgRef} onLoad={this.handleImageLoaded}/>
            )
          }
        </>
      );
    }
}
export default withTranslation()(withTheme(ImageEdit));