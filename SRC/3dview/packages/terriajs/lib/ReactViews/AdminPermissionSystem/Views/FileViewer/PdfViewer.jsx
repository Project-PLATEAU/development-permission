import { observer } from "mobx-react";
import PropTypes from "prop-types";
import React from 'react';
import { withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import Config from "../../../../../customconfig.json";
import Icon, { StyledIcon } from "../../../../Styled/Icon";
import CustomStyle from "./scss/pdf-viewer.scss"
import Styles from "../../../DevelopmentPermissionSystem/PageViews/scss/pageStyle.scss";
import { RawButton } from "../../../../Styled/Button";

/**
 * PDFビューワー
 */
@observer
class PdfViewer extends React.Component {
    
    static displayName = "PdfViewer"

    static propType = {
        path: PropTypes.string.isRequired,
        name: PropTypes.string.isRequired,
        dataUrl: PropTypes.string,
        appFileVersion: PropTypes.string,
        t: PropTypes.func.isRequired,
        fileClose: PropTypes.func.isRequired,
        addFile: PropTypes.func.isRequired
    }

    constructor(props){
        super(props);
        this.state = {
            filePath: props.path,
            fileName: props.name,
            dataUrl: props.dataUrl,
            appFileVersion: props.appFileVersion
        };
    }

    componentDidUpdate(prevProps) {
        if (this.props.path !== prevProps.path || this.props.name !== prevProps.name || this.props.dataUrl !== prevProps.dataUrl) {
            this.setState({
                filePath: this.props.path,
                fileName: this.props.name,
                dataUrl: this.props.dataUrl
            });
        }
    }

    editShowPage = () => {
        var iframeElement = document.getElementById('pdfViewer');
        var iframeDocument = iframeElement.contentDocument || iframeElement.contentWindow.document;

        if (iframeDocument) {
            const currentPageNum = iframeDocument.getElementById('pageNumber');
            if (currentPageNum.value) {
                if(!this.props.dataUrl){
                    // 指定したページの画像変換
                    console.log(`申請ファイルバージョン：${this.state.appFileVersion}`);
                    let apiUrl = !this.props.appFileVersion ? Config.config.apiUrl + "/file/convert" + this.state.filePath + "?page=" + currentPageNum.value : Config.config.apiUrl + "/file/convert" + this.state.filePath + "?page=" + currentPageNum.value + "&version=" + this.state.appFileVersion;
                    fetch(apiUrl) 
                    .then(res => {
                        const contentDisposition = res.headers.get("Content-Disposition");
                        const fileName = contentDisposition ? contentDisposition.match(/filename="([^"]+)"/)[1] : "";
                        
                        return res.blob().then(blob => {
                            return {
                                blob: blob,
                                fileName: decodeURIComponent(fileName)
                            };
                        });
                    })
                    .then(data => {
                        this.props.addFile(data.blob, data.fileName);
                    });
                }else{
                    let canvasList = iframeDocument.querySelectorAll("#viewer canvas");
                    let canvas = canvasList[parseInt(currentPageNum.value, 10) - 1];
                    console.log(canvas);
                    if (canvas) {
                        canvas.toBlob((blob) => {
                            let fileName = this.props.name.split('.').slice(0, -1).join('.') + "_" + currentPageNum.value +  ".png";
                            this.props.addFile(blob, fileName);
                        }, 'image/png');
                    }
                }

            } else {
                alert("ページ番号の取得に失敗しました。");
            }
        } else {
            alert("PDFビュワーの読み込みに失敗しました。");
        }
    }

    render() {
        const t = this.props.t;
        const fileName = this.state.fileName;
        const dataUrl = this.state.dataUrl;
        const baseUrl = dataUrl ? dataUrl : Config.config.apiUrl + "/file/viewapp" + this.state.filePath;
        const viewerUrl = Config.config.pdfViewerUrl + `/web/viewer.html?file=${encodeURIComponent(baseUrl)}`;

        return (
            <div style={{ display: "flex", flexDirection: "column", height: "100%"}}>
                <div className={CustomStyle.viewerHeader}>
                    <div>
                        <span>{`表示ファイル：${fileName}`}</span>
                    </div>
                    <div>
                        <RawButton
                            onClick={this.props.fileClose}
                            title={t("featureInfo.btnCloseFeature")}
                        >
                            <StyledIcon
                                styledWidth={"16px"}
                                fillColor={"#000"}
                                opacity={"0.5"}
                                glyph={Icon.GLYPHS.closeLight}
                                css={`
                                    cursor:pointer;
                                `}
                            />
                        </RawButton>
                    </div>
                </div>
                <div style={{display: "flex", justifyContent: "center"}}>
                    <button
                        className={CustomStyle.edit_button}
                        onClick={this.editShowPage}
                    >
                        表示中のページを編集する
                    </button>
                </div>
                <div style={{ flex: "1"}}>
                    <iframe 
                        id="pdfViewer"
                        title="PDF Viewer" 
                        src={viewerUrl} 
                        width="100%" 
                        height="100%" 
                        style={{border: "none"}}
                    ></iframe>
                </div>
            </div>
        );
    }
}

export default withTranslation()(withTheme(PdfViewer));
