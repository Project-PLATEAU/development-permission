import { observer } from "mobx-react";
import PropTypes from "prop-types";
import React from 'react';
import { withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import Config from "../../../../../customconfig.json";
import Icon, { StyledIcon } from "../../../../Styled/Icon";
import CustomStyle from "./scss/pdf-view-modal.scss"
import { RawButton } from "../../../../Styled/Button";

/**
 * PDF表示モーダル
 */
@observer
class PdfViewModal extends React.Component {

    static display = "PdfViewModal"

    static propsType = {
        t: PropTypes.func.isRequired,
        name: PropTypes.string.isRequired,
        path: PropTypes.string.isRequired,
        url: PropTypes.string,
        appFileVersion: PropTypes.string,
        pdfEditable: PropTypes.bool
    }

    constructor(props){
        super(props);
        this.state = {
            fileName: props.name,
            filePath: props.path,
            fileUrl: props.url,
            version: props.appFileVersion
        };
    }

    /**
     * コンポーネント更新イベント
     * @param {*} prevProps 
     */
    componentDidUpdate(prevProps) {
        if (this.props.path !== prevProps.path || this.props.name !== prevProps.name || this.props.url !== prevProps.url) {
            this.setState({
                filePath: this.props.path,
                fileName: this.props.name,
                fileUrl: this.props.url
            });
        }
    }

    /**
     * 指定したPDFページを画像に変化して編集
     */
    editShowPage = () => {
        var iframeElement = document.getElementById('pdfViewer');
        var iframeDocument = iframeElement.contentDocument || iframeElement.contentWindow.document;

        if (iframeDocument) {
            const currentPageNum = iframeDocument.getElementById('pageNumber');
            if (currentPageNum.value) {
                if(!this.props.url){
                    // 指定したページの画像変換
                    let apiUrl = !this.props.appFileVersion ? Config.config.apiUrl + "/file/convert" + this.state.filePath + "?page=" + currentPageNum.value : Config.config.apiUrl + "/file/convert" + this.state.filePath + "?page=" + currentPageNum.value + "&version=" + this.state.version;
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
                        // PDFファイルの削除（新規追加時のみ）
                        this.props.fileDelete(this.props.name);
                    });
                }else{
                    let pageList = iframeDocument.querySelectorAll("#viewer .page");
                    let pageData = pageList[parseInt(currentPageNum.value, 10) - 1]
                    let canvas = pageData.getElementsByTagName("canvas");
                    if (canvas[0]) {
                        canvas[0].toBlob((blob) => {
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

    /**
     * モーダル閉じる
     */
    modalClose = () => {
        this.props.fileClose(this.props.addFileFlag);
    }

    render(){
        const t = this.props.t;
        const fileUrl = this.state.fileUrl;
        const baseUrl = fileUrl ? fileUrl : Config.config.apiUrl + "/file/viewapp" + this.state.filePath;
        const viewerUrl = Config.config.pdfViewerUrl + `/web/viewer.html?file=${encodeURIComponent(baseUrl)}#pagemode=thumbs`;
        return (
            <div className={CustomStyle.overlay}>
                <div className={CustomStyle.tool_button}>
                    <RawButton
                        onClick={this.modalClose}
                        title={t("general.back")}
                        className={CustomStyle.button}
                        >
                        <StyledIcon
                            styledWidth={"16px"}
                            fillColor={"#FFF"}
                            glyph={Icon.GLYPHS.arrowLeft}
                            css={`
                                cursor:pointer;
                            `}
                        />
                    </RawButton>
                </div>
                <div className={CustomStyle.modal}>
                    <div className={CustomStyle.edit_button}>
                        { this.props.pdfEditable && (
                            <button
                                onClick={this.editShowPage}
                            >
                            表示ページを編集
                            </button>
                        )}
                    </div>
                    <div className={CustomStyle.pdfViewer}>
                        <iframe 
                            id="pdfViewer"
                            title="PDF Viewer" 
                            src={viewerUrl} 
                            width="100%" 
                            height="90%" 
                            style={{border: "none"}}
                        ></iframe>
                    </div>
                </div>
            </div>
        )
    }
}

export default withTranslation()(withTheme(PdfViewModal));