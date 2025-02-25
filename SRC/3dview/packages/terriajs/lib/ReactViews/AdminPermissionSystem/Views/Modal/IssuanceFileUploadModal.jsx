import React from "react";
import PropTypes from "prop-types";
import { observer } from "mobx-react";
import { withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import Box from "../../../../Styled/Box";
import Styles from "./scss/IssuanceFileUploadModal.scss"
import { RawButton } from "../../../../Styled/Button";
import Icon, { StyledIcon } from "../../../../Styled/Icon";
import Config from "../../../../../customconfig.json";

/**
 * 行政用コンポーネント：発行様式ファイル選択ダイアログ
 */

@observer
class IssuanceFileUploadModal extends React.Component {
    static displayName = "IssuanceFileUploadModal"
    static propsType = {
        terria: PropTypes.object.isRequired,
        viewState: PropTypes.object.isRequired
    }

    constructor(props) {
        super(props);
        this.state = {
            viewState: props.viewState,
            terria: props.terria,
        }
    }

    /**
     * 初期処理
     */
    componentDidMount() {
        this.draggable(document.getElementById('issuanceFileUploadModalDrag'), 
                                        document.getElementById('issuanceFileUploadModal'));
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
     * @param {*} e イベント
     * @returns 
     */
    handleFileUpload(e){
        let file = null;
        const issuanceLedgerForm = {...this.props.viewState.issuanceLedgerForm};
        //チェック処理
        if(issuanceLedgerForm){
            file = e.target.files[0];
            if(!file){
                alert("ファイルの取得に失敗しました。アップロードをやり直してください。"); 
                return false;
            }
            if (file.size > 10485760) {
                alert("10M以下のファイルをアップロードできます");
                return false;
            }
        }else{
            alert("アップロード対象の帳票の取得に失敗しました。再度画面を閉じてやり直してください。");
            return false;
        }
        // 拡張子チェック
        let extension = file.name.split('.').pop();
        if(issuanceLedgerForm.extension){
            let allowExts = issuanceLedgerForm.extension.split(',')
            if(allowExts.indexOf(extension) === -1){
                alert(issuanceLedgerForm.extension + " のいずれかのファイル形式のファイルをアップロードしてください。");
                return false;
            }
        }

        // ファイル名チェック
        const fileNameWithoutExtension = file.name.split(".").slice(0, -1).join(".");
        const regex = /[\"\'<>\&]/;
        const reg = new RegExp(regex);
        const result = reg.exec(fileNameWithoutExtension);
        if (result != null) {
            alert("ファイル名に禁止文字("+'"'+",',<,>,&"+")のいずれか含めていますので、ファイル名を修正してアップロードしてください。");
            return false;
        }

        issuanceLedgerForm.uploadFile = file;
        issuanceLedgerForm.uploadFileName = file.name;

        if(!issuanceLedgerForm.notifiable){
            issuanceLedgerForm.notifiable = false;
        }
        if(!issuanceLedgerForm.uploadable){
            issuanceLedgerForm.uploadable = false;
        }

        this.props.viewState.setIssuanceLedgerForm(issuanceLedgerForm);
    };

    /**
     * アップロード処理
     */
    upload(){
        try{
            document.getElementById("customloader_main").style.display = "block";
            const issuanceLedgerForm = this.props.viewState.issuanceLedgerForm;
            if(issuanceLedgerForm["notifyFlag"]){
                if(!window.confirm("このファイルは既に事業者へ通知済みです。本当に置き換えますか？\nアップロード後は再度、事業者への通知が必要となります。")){
                    this.props.viewState.refreshConfirmApplicationDetails();
                    document.getElementById("customloader_main").style.display = "none";
                    return;
                }
            }
            const formData  = new FormData();
            for(const name in issuanceLedgerForm) {
                if(name == "notifyFlag" && !issuanceLedgerForm[name]){
                    issuanceLedgerForm[name] = false;
                }
                formData.append(name, issuanceLedgerForm[name]);
            }
            fetch(Config.config.apiUrl + "/ledger/file/upload", {
                method: 'POST',
                body: formData,
            })
            .then(res => res.json())
            .then(res => {
                if(res.status === 401){
                    alert("認証情報が無効です。ページの再読み込みを行います。");
                    window.location.reload();
                    return null;
                }
                if(res.status !== 201){
                    alert('帳票の更新に失敗しました。再度やり直してください。');
                }else{
                    alert('帳票の更新に成功しました');
                }
                
                this.props.viewState.refreshConfirmApplicationDetails();
                document.getElementById("customloader_main").style.display = "none";
            })
        }catch(error){
            document.getElementById("customloader_main").style.display = "none";
            console.error('処理に失敗しました', error);
            alert('帳票の更新に失敗しました。再度やり直してください。');
        }
    }

    /**
     * モーダルを閉じる
     * 
     */
    close(){
        this.state.viewState.changeIssuanceFileUploadModalShow();
    }

    render(){
        const issuanceLedgerForm = this.props.viewState.issuanceLedgerForm;
        return (
            <div className={Styles.overlay}>
                <div className={Styles.modal}  id="issuanceFileUploadModal" style={{width:"40vw"}}>
                    <Box position="absolute" paddedRatio={3} topRight>
                        <RawButton onClick={() => {
                            this.close();
                        }}>
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
                    </Box>
                    <nav className={Styles.custom_nuv} id="issuanceFileUploadModalDrag">
                        帳票更新・通知
                    </nav>
                    <Box className={Styles.container} displayInlineBlock>
                        <Box displayInlineBlock>
                            <p style={{ fontSize: "1.1em", fontWeight: "600"}}>帳票を更新</p>
                            <p>{issuanceLedgerForm.informationText}</p>
                            <label htmlFor="file-upload" className={Styles.file_upload_button}>
                                ファイルを選択
                                <input id="file-upload" type="file" onChange={(e) => this.handleFileUpload(e)} />
                            </label>
                            {issuanceLedgerForm && issuanceLedgerForm.uploadFile && (
                                <span style={{marginLeft:"20px"}}>{issuanceLedgerForm.uploadFileName}</span>
                            )}
                        </Box>
                        <Box right style={{marginBottom:0}}>
                            <button className={`${Styles.file_upload_button} ${!issuanceLedgerForm || !issuanceLedgerForm.uploadFile ? Styles.disabled_button : ""}`} 
                                    style={{padding:"10px 20px"}}
                                    onClick={e => {
                                        this.upload();
                                    }}
                                    disabled= {!issuanceLedgerForm || !issuanceLedgerForm.uploadFile}
                                >
                                <span>登録</span>
                            </button>
                        </Box>
                    </Box>
                </div>
            </div>
        )
    }
}
export default withTranslation()(withTheme(IssuanceFileUploadModal));