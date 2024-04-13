import React from "react";
import PropTypes from "prop-types";
import { observer } from "mobx-react";
import { withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import Box from "../../../../Styled/Box";
import Styles from "./scss/inquiry-file-upload-modal.scss"
import { RawButton } from "../../../../Styled/Button";
import Icon, { StyledIcon } from "../../../../Styled/Icon";
import Spacing from "../../../../Styled/Spacing";

/**
 * 問合せコンポーネント：ファイルアップロードモーダル
 */

@observer
class InquiryFileUploadModal extends React.Component {
    static displayName = "InquiryFileUploadModal"
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
            // 問合せファイル一覧
            inquiryFiles: props.viewState.inquiryFiles
        }
    }

    /**
     * 初期処理
     */
    componentDidMount() {
        this.draggable(document.getElementById('inquiryFileUploadModalDrag'), document.getElementById('fileUploadModal'));
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
     * ファイルアップロード
     */
    fileUpload(event){
        const file = event.target.files[0];
        let inquiryFiles = this.state.inquiryFiles; 

        if (file.size > 10485760) {
            alert("10M以下のファイルをアップロードできます");
            return false;
        }

        let isRepeat = false;
        Object.keys(inquiryFiles).map(index => {
            if(inquiryFiles[index]["fileName"] == file.name){
                isRepeat = true;
            }
        });

        if(isRepeat){
            alert("既に同一ファイル名のファイルが添付されています。別ファイルを選択してください。");
            return false;
        }

        let reader = new FileReader();
        reader.readAsDataURL(file);
        reader.onload = (e) => {
            inquiryFiles.push({ "inquiryFileId": "", "messageId": "", "fileName": file.name, "filePath": file.name, "uploadFile": file});
            this.setState({ inquiryFiles: inquiryFiles, });
            this.props.viewState.changeInquiryFiles(inquiryFiles);
            document.getElementById("file-upload").value = "";
        }
    };

    /**
     * ファイル削除
     * @param {event} event イベント
     * @param {number} 対象申請ファイルのindex
     */
    fileDelete(event, index) {
        let inquiryFiles = this.state.inquiryFiles;
        inquiryFiles.splice(index,1);
        this.setState({
            inquiryFiles: inquiryFiles,
        });

        this.props.viewState.changeInquiryFiles(inquiryFiles);
    }

    /**
     * モーダルを閉じる
     * 
     */
    close(){
        this.state.viewState.changeInquiryFileUploadModalShow();
    }

    render(){
        let inquiryFiles = this.state.inquiryFiles;
        return (
            <div className={Styles.overlay}>
                <div className={Styles.modal}  id="fileUploadModal">
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
                    <nav className={Styles.custom_nuv} id="inquiryFileUploadModalDrag">
                        ファイル追加
                    </nav>
                    <div className={Styles.container}>
                        <p>添付ファイルを選択してください。</p>
                        <Spacing bottom={2} />
                        <label htmlFor="file-upload" className={Styles.file_upload_button}>
                            ファイルを選択
                            <input id="file-upload" type="file" onChange={(e) => this.fileUpload(e)} />
                        </label>
                        <Spacing bottom={2} />
                        <div className={Styles.scrollContainer}>
                            <table className={Styles.selection_table}>
                                <thead className={Styles.table_header}>
                                    <tr>
                                        <th style={{width: "10%"}}>No.</th>
                                        <th style={{width: "60%"}}>ファイル名</th>
                                        <th style={{width: "30%"}}></th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {Object.keys(inquiryFiles).map(index => (
                                        <tr key={`inquiryFiles-${index}`}>    
                                            <td>{ Number(index) + 1}</td>
                                            <td>{inquiryFiles[index]?.fileName}</td>
                                            <td>
                                                <button className={Styles.file_upload_button}
                                                    onClick={e => {
                                                        this.fileDelete(e,index)
                                                    }}
                                                >
                                                    <span>削除</span>
                                                </button>
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        )
    }



}
export default withTranslation()(withTheme(InquiryFileUploadModal));