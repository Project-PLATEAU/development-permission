import { observer } from "mobx-react";
import PropTypes from "prop-types";
import React from "react";
import { withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import Icon, { StyledIcon } from "../../../../Styled/Icon";
import Spacing from "../../../../Styled/Spacing";
import Box from "../../../../Styled/Box";
import CustomStyle from "./scss/input-chat-message.scss";
import Config from "../../../../../customconfig.json";
import InquiryFileUploadModal from "./InquiryFileUploadModal.jsx"
import FileDownLoadModal from "../../../AdminPermissionSystem/Views/Modal/FileDownLoadModal";
import FilePreViewModal from "../../../AdminPermissionSystem/Views/Modal/FilePreViewModal";

/**
 * 事業者：問合せ画面
 */
@observer
class InputChatMessage extends React.Component {
    static displayName = "InputChatMessage";
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
            // 入力したメッセージ内容
            messageText:  "",
            // 該当回答内容に対する問合せ情報リスト
            chat: {},
            // 行政からの回答一覧
            answers: props.viewState.answerContent.answers,
            // チャットに紐づく回答ID
            currentAnswerId: props.viewState.currentAnswerId,
            // チャットメッセージ表示エリアの高さ
            height: 0,
            // 回答内容を表示するエリアの位置(左)
            positionLeft:0,
            // 回答内容を表示するエリアの幅
            width:0,
            // 回答内容を表示するエリアの高さ
            heightForAnswerContent:0,
            intervalID:null,
            //問合せの相関情報DTO
            ChatRelatedInfoForm: {},
            //回答DTO
            answer: {},
            // 回答対象
            categoryJudgementTitle:"",
            // 回答登録内容表示フラグ
            answerContentDisplay:false,
            //回答履歴
            answerHistory: [],
            //回答ファイル
            answerFile: [],
            //申請ファイル
            applicationFile: [],
            // 表示している一覧
            activeListId:0,
            previwFlag: false
        };
        this.openFile = this.openFile.bind(this);
    }

    /**
     * 初期処理
     */
    componentDidMount() {
        // 問合せ内容取得
        this.getMessageList(false);
        //  問合せの相関情報取得
        this.getChatRelatedInfo();
        // 画面リサイズ
        this.getWindowSize(); 
        
        // チャットボックスのリサイズ用
        //メッセージ入力欄の要素取得
        let textarea = document.getElementById('messageText');
        //メッセージ入力欄のinputイベント
        textarea.addEventListener('input', ()=>{
            this.changeTextAreaHight(false,true);
        }); 
        
        // 定期リフレッシュ処理 
        let intervalID = setInterval(() => {
            if(this.props.viewState.showChatView
                && !this.state.viewState.inquiryFileUploadModalShow
                && !this.state.viewState.fileDownloadModalShow
                && !this.state.viewState.filePreViewModalShow){
                this.getMessageList(true);
            }else{
                return;
            }
            
        }, 30000);

        this.setState({intervalID:intervalID});
    }

    /**
     * 問合せ内容取得
     * @param {*} isRefresh 定期リフレッシュイベントであるフラグ
     */
    getMessageList(isRefresh){
        // 問合せしている回答ID
        let currentAnswerId = this.state.currentAnswerId;
        let answers = this.state.answers;
        answers = answers.filter((value) => value["answerId"] == currentAnswerId);
        let answer = answers[0];
        let answerContent = this.props.viewState.answerContent;
        let loginId = answerContent["loginId"];
        let password = answerContent["password"];
        // チャットが存在すれば、チャットメッセージを取得して、チャット画面へ遷移する
        fetch(Config.config.apiUrl + "/chat/business/messages", {
            method: 'POST',
            body: JSON.stringify({
                answer:answer,
                loginId: loginId,
                password:password
            }),
            headers: new Headers({ 'Content-type': 'application/json' }),
        })
        .then(res => res.json())
        .then(res => {
            if(res.status === 401){
                alert("認証情報が無効です。ページの再読み込みを行います。");
                window.location.reload();
                return null;
            }
            if (res.chatId) {
                // チャットメッセージを回答IDと紐づける
                answer["chat"] = JSON.parse(JSON.stringify(res));
                this.setState({chat:answer["chat"]});
                this.changeTextAreaHight(false,isRefresh);
            }else{
                alert('チャットメッセージ一覧取得処理に失敗しました');
            }
        }).catch(error => {
            console.error('通信処理に失敗しました', error);
            alert('通信処理に失敗しました');
        });
    }

    /**
     * 問合せの関連情報取得
     */
    getChatRelatedInfo(){
        let currentAnswerId = this.state.currentAnswerId;
        if (currentAnswerId) {
            fetch(Config.config.apiUrl + "/chat/business/related/" + currentAnswerId)
            .then(res => res.json())
            .then(res => {
                if(res.status === 401){
                    alert("認証情報が無効です。ページの再読み込みを行います。");
                    window.location.reload();
                    return null;
                }
                if (res.answerId) {
                    this.setState({
                        answer: res.answer,
                        categoryJudgementTitle: res.categoryJudgementTitle,
                        answerHistory: res.answerHistorys,
                        answerFile: res.answerFiles,
                        applicationFile: res.applicationFiles,
                        ChatRelatedInfoForm: res
                    });
                } else {
                    alert("該当チャットの関連情報取得に失敗しました。再度操作をやり直してください。");
                }
            }).catch(error => {
                console.error('通信処理に失敗しました', error);
                alert('通信処理に失敗しました');
            });
        } else {
            alert("該当チャットの関連情報取得に失敗しました。再度操作をやり直してください。");
        }
    }

    /**
     * 入力される内容に合わせて各要素の高を調整する
     * @param {*} isSendEvent 送信クリックイベントであるフラグ
     * @param {*} isRefresh 定期リフレッシュイベントであるフラグ
     */
    changeTextAreaHight(isSendEvent,isRefresh){
        //メッセージ入力欄の要素取得
        let textarea = document.getElementById('messageText');
        //textareaのデフォルトの要素の高さを取得
        let ch = textarea.clientHeight;
        //textareaの高さを再設定（デフォルトの高さから計算するため）
        textarea.style.height = ch + 'px';
        //textareaの入力内容の高さを取得
        let sh = textarea.scrollHeight;
        textarea.style.overflowY = "hidden";
        if(sh>100){
            textarea.style.overflowY = "scroll";
            sh = 100;
        }else if(sh<40){
            sh = 40;
        }
        // 送信ボタンを押下する場合、初期の高さをリセット
        if(isSendEvent){
            sh = 40;
            textarea.style.overflowY = "hidden";
        }

        //メッセージ入力欄の高さに入力内容の高さを設定
        textarea.style.height = sh + 'px';

        //メッセージ入力欄のボックス
        let inputMessageBox = document.getElementById('inputMessageBox');
        inputMessageBox.style.height = (sh + 30) + 'px';
        
        //メッセージ表示のボックス
        let chatBox = document.getElementById('ChatBox');
        chatBox.style.height = (this.state.height - (sh - 40)) + 'px';

        // リフレッシュ以外の場合、スクロールを最後に、移動する
        if(!isRefresh){
            // スクロールは最後にする
            setTimeout(function(){chatBox.scrollTo(0,chatBox.scrollHeight)}, 300);
        }
    }

    /**
     * 高さ再計算
     */
    getWindowSize() {
        let win = window;
        let e = window.document.documentElement;
        let g = window.document.documentElement.getElementsByTagName('body')[0];
        let h = win.innerHeight|| e.clientHeight|| g.clientHeight;
        let w = win.innerWidth|| e.clientWidth|| g.clientWidth;
        const getRect = document.getElementById("ChatBox");
        let height = h - getRect.getBoundingClientRect().top - 20 - 70;
        const sidePanel = document.getElementById("SidePanel");
        let width = w - sidePanel.clientWidth;
        let map = window.document.documentElement.getElementsByTagName('canvas')[0];
        this.setState({height: height, width: width, positionLeft: sidePanel.clientWidth, heightForAnswerContent: map.clientHeight/2});
    }

    /**
     * リサイズ
     */
    componentWillMount () {
        window.addEventListener('resize', () => {
                this.getWindowSize() 
        })
    }

    /**
     * メッセージ投稿
     */
    sendMessage(){
        let text = this.state.messageText;
        let chat = this.state.chat;

        // チャットメッセージ内容をDBに登録する
        let answerId = this.state.currentAnswerId
        let answerContent = this.props.viewState.answerContent;
        let loginId = answerContent["loginId"];
        let password = answerContent["password"];
        let message = {"messageText": text};
        let chatId = chat["chatId"];
        let messageCount = 0;
        if(chat["messages"]){
            messageCount = chat["messages"].length;
        }
        
        fetch(Config.config.apiUrl + "/chat/business/message/post", {
            method: 'POST',
            body: JSON.stringify({
                chatId:chatId,
                answerId:answerId,
                loginId: loginId,
                password:password,
                message:message
            }),
            headers: new Headers({ 'Content-type': 'application/json' }),
        })
        .then(res => res.json())
        .then(res => {
            if(res.status === 401){
                alert("認証情報が無効です。ページの再読み込みを行います。");
                window.location.reload();
                return null;
            }
            //投稿前後のメッセージ件数を比較して、投稿に成功するか判断する
            if (res.messages.length > messageCount) {
                let messageId = res.messages[res.messages.length -1].messageId;
                // 問合せ添付ファイルがある場合、ファイルアップロードを行う
                let inquiryFiles = this.props.viewState.inquiryFiles;
                if(Object.keys(inquiryFiles).length > 0){
                    this.uploadFile(inquiryFiles,messageId);
                    // 登録されたメッセージをクリアする
                    this.setState({messageText:""});
                }else{
                    // 最新なメッセージ一覧を表示 
                    this.setState({chat:JSON.parse(JSON.stringify(res)), messageText:""});
                    this.changeTextAreaHight(true,false);
                }
            }else{
                alert('メッセージ送信処理に失敗しました');
            }
        }).catch(error => {
 
            console.error('処理に失敗しました', error);
            alert('処理に失敗しました');
        });
    }

    /**
     * 回答内容確認画面へ戻る
     */
    back(){
        let currentAnswerId = this.state.currentAnswerId;
        let answers = this.state.answers;
        let answerContent = this.props.viewState.answerContent;
        let index = answers.findIndex(obj => obj.answerId == currentAnswerId );

        answers[index]["chat"] = this.state.chat;
        answerContent.answer = answers;

        // 回答確認画面へ戻る
        this.props.viewState.backFromChatView(answerContent);

        let intervalID = this.state.intervalID;
        clearInterval(intervalID);

        // 選択された問合せ添付ファイルリストをクリアする
        this.props.viewState.changeInquiryFiles([]);
    }

    /**
     * ファイルダウンロード
     * @param {Object} applicationFile 対象ファイル情報
     */
    output(applicationFile) {
        let applicationFileHistorys = applicationFile.applicationFileHistorys;
        let target = applicationFile.applicationFileName;

        // 対象ごとに申請ファイル（全て版を含む）が1件のみ場合、直接ダウンロードする
        if(Object.keys(applicationFileHistorys).length == 1){
            this.outputFile("/application/file/download", applicationFileHistorys[0],"uploadFileName");
        }else{
            this.openDownFileView(applicationFileHistorys,target,false);
        }
    }

    /**
     * ファイルダウンロードモーダルを開く
     * @param {*} applicationFile 対象ファイル
     * @param {*} target ターゲット
     */
    openDownFileView(applicationFile,target,previwFlag){
        this.setState({previwFlag})
        this.props.viewState.setFileDownloadTarget(applicationFile,target)
        this.state.viewState.changeFileDownloadModalShow();
    }

    /**
     * ファイルダウンロード
     * @param {*} path ファイルダウンロード用APIのパス
     * @param {*} file ダウンロード対象ファイル
     * @param {*} fileNameKey ファイル名に対するキー項目
     */
    outputFile(path, file, fileNameKey) {
        if(file){
            file.applicationId = this.props.viewState.answerContent.applicationId;
            file.loginId = this.props.viewState.answerContent.loginId;
            file.password = this.props.viewState.answerContent.password;
        }
        // APIへのリクエスト
        fetch(Config.config.apiUrl + path, {
            method: 'POST',
            body: JSON.stringify(file),
            headers: new Headers({ 'Content-type': 'application/json' }),
        })
        .then((res) => {
            if(res.status === 401){
                alert("認証情報が無効です。ページの再読み込みを行います。");
                window.location.reload();
                return null; 
            }else{
                return res.blob();
            }
        })
        .then(blob => {
            let anchor = document.createElement("a");
            anchor.href = window.URL.createObjectURL(blob);
            anchor.download = file[fileNameKey];
            anchor.click();
        })
        .catch(error => {
            console.error('処理に失敗しました', error);
            alert('処理に失敗しました');
        });
    }

    /**
     * 添付ファイルアップロード
     * @param {*} inquiryFiles 添付ファイル
     * @param {*} messageId メセッジID
     */
    uploadFile(inquiryFiles,messageId){
        Object.keys(inquiryFiles).map(key => {
            // パラメータ編集
            inquiryFiles[key]["messageId"] = messageId;
            const formData  = new FormData();
            for(const name in inquiryFiles[key]) {
                formData.append(name, inquiryFiles[key][name]);
            }

            // 問合せ添付ファイルアップロードAPIを呼び出す
            fetch(Config.config.apiUrl + "/chat/file/upload", {
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
                inquiryFiles[key]["status"] = res.status;
                if(res.status !== 201){
                    alert('アップロードに失敗しました');
                }
                let completeFlg = true;
                Object.keys(inquiryFiles).map(key => { 
                    if(!inquiryFiles[key]["status"]){
                        completeFlg = false;
                    }
                })
                if(completeFlg){
                    // 最新なメッセージ一覧を表示 
                    this.getMessageList(false);
                    // 選択された問合せ添付ファイルリストをクリアする
                    this.props.viewState.changeInquiryFiles([]);           
                }
            }).catch(error => {
                console.error('問合せ添付ファイルをアップロードする処理に失敗しました', error);
                alert('問合せ添付ファイルをアップロードする処理に失敗しました');
            });
        });
    }

    /**
     * 添付ファイル選択モーダル画面を開く
     */
    openUploadFileModal(){
        this.state.viewState.changeInquiryFileUploadModalShow();
    }

    /**
     * 拡張子取得
     * @param {String} fileName ファイル名
     */
    getExtension(fileName){
        // 拡張子チェック
        let extension = fileName.split('.').pop();
        return extension;
    }

    /**
     * プレビューファイルの拡張子チェック
     * @param {*} fileName ファイル名
     * @returns 
     */
    extensionCheck(fileName){
        const extArray=["PDF","PNG","JPG"];
        // 拡張子確認（PDF,PNG,JPG）
        let extension = this.getExtension(fileName).toLocaleUpperCase();;
        if(extArray.includes(extension)){
            return true;
        }else{
            return false;
        }
    }
    /**
     * ファイルリストに、プレビュー可能のファイルが存在するかチェック
     * @param {*} fileList ファイルリスト
     * @returns チェック結果
     */
    applicationFileExtensionCheck(fileList){
        let count = 0;
        Object.keys(fileList).map( key => {
            let fileName = fileList[key].uploadFileName;
            if(this.extensionCheck(fileName)){
                count++;
            }
        });

        if(count > 0){
            return true;
        }else{
            return false;
        }
    }

    /**
     * 申請ファイルプレビュー
     * @param {Object} applicationFile 対象ファイル情報
     */
    preview(applicationFile) {
        let applicationFileHistorys = applicationFile.applicationFileHistorys;
        let target = applicationFile.applicationFileName;

        // 対象ごとに申請ファイル（全て版を含む）が1件のみ場合、直接ダウンロードする
        if(Object.keys(applicationFileHistorys).length == 1){
            this.openFile("/application/file/download", applicationFileHistorys[0],"uploadFileName");
        }else{
            this.openPreviewFileView(applicationFileHistorys,target);
        }
    }

    /**
     * ファイルプレビューモーダルを開く
     * @param {*} applicationFile 対象ファイル情報
     * @param {*} target ターゲット
     */
    openPreviewFileView(applicationFile,target,previwFlag){
        this.setState({previwFlag})
        this.props.viewState.setFileDownloadTarget(applicationFile,target)
        this.state.viewState.changeFilePreViewModalShow();
    }

    /**
     * ファイルプレビュー
     * @param {*} path ファイルダウンロード用APIのパス
     * @param {*} file ダウンロード対象ファイル
     * @param {*} fileNameKey ファイル名に対するキー項目
     */
    openFile(path, file, fileNameKey) {
        if(file){
            file.applicationId = this.props.viewState.answerContent.applicationId;
            file.loginId = this.props.viewState.answerContent.loginId;
            file.password = this.props.viewState.answerContent.password;
        }
        // APIへのリクエスト
        fetch(Config.config.apiUrl + path, {
            method: 'POST',
            body: JSON.stringify(file),
            headers: new Headers({ 'Content-type': 'application/json' }),
        })
        .then((res) => {
            if(res.status === 401){
                alert("認証情報が無効です。ページの再読み込みを行います。");
                window.location.reload();
                return null; 
            }else{
                return res.blob();
            }
        })
        .then(blob => {
            let fileUrl = window.URL.createObjectURL(blob);
            // window.open(fileUrl,'_blank'); ⇒　ファイル名は別タブに表示できない。
            let win = window.open('','_blank');

            if(win.document) { 
                let html = "";
                html = '<html>';
                html += '<head><title>' + file[fileNameKey] +'</title></head>';

                let extension = this.getExtension(file[fileNameKey])
                if(extension == "pdf" || extension == "PDF"){
                    html += '<body style="height: 100%; width: 100%; overflow: hidden; margin:0px; background-color: rgb(230, 230, 230);">';
                    html += '<embed style="position:absolute; left: 0; top: 0;" width="100%" height="100%" src="' + fileUrl + '" type="application/pdf" >';
                    html += '</body>';
                }else{
                    html += '<body style="margin: 0px; height: 100%; background-color: rgb(14, 14, 14); display: flex;">';
                    html += '<img style="display: block;-webkit-user-select: none;margin: auto;background-color: hsl(0, 0%, 90%);transition: background-color 300ms;" src="' + fileUrl + '">';
                    html += '</body>';
                }
                html += '</html>';

                win.document.write(html);
            } 
        })
        .catch(error => {
            console.error('ファイルのプレビュー表示に失敗しました。', error);
            alert('ファイルのプレビュー表示に失敗しました。');
        });
    }


    /**
     * メッセージ又は添付ファイルが入力したか確認
     * @param {*} text 入力したメッセージ内容 
     * @param {*} inquiryFiles 添付ファイルリスト
     */
    isInputed(text, inquiryFiles){

        if(text){
            text = text.trim();
        }
        if(text || Object.keys(inquiryFiles).length > 0 ){
            return true;
        }else{
            return false;
        }

    }

    render() {
        let height = this.state.height;
        let messageText = this.state.messageText;
        let chatMessages = this.state.chat["messages"];
        let descriptionDivWidth = this.state.width;
        let positionLeft = this.state.positionLeft;
        let heightForAnswerContent = this.state.heightForAnswerContent;

        // 添付ファイル
        let inquiryFiles = this.props.viewState.inquiryFiles;
        //該当チャットに関する情報
        //①チャットIDに紐づく回答DTO
        let answer = this.state.answer;
        //②回答IDに紐づく回答対象
        let categoryJudgementTitle = this.state.categoryJudgementTitle;
        //③回答IDに紐づく回答履歴一覧
        let answerHistorys = this.state.answerHistory;
        //④回答IDに紐づく回答ファイル一覧
        let answerFiles = this.state.answerFile;
        //⑤回答対象に紐づく申請ファイル一覧
        let applicationFiles = this.state.applicationFile;
        //回答登録内容を表示するかフラグ
        let answerContentDisplay = this.state.answerContentDisplay;
        //表示するしている一覧のID
        let activeListId = this.state.activeListId;
        return (
            <>
                <div className={CustomStyle.div_area1}>
                    <Box css={`display:block`} >
                        <nav className={CustomStyle.custom_nuv}>
                            問い合わせチャット
                        </nav>
                        <Box
                            centered
                            displayInlineBlock
                            className={CustomStyle.custom_content}
                        >
                            <Spacing bottom={2} />
                            <button
                                className={`${CustomStyle.btn_baise_style} ${CustomStyle.btn_gry}`}
                                style={{width:"20%"}}
                                onClick={e => { this.back(); }}
                            >
                                <span>戻る</span>
                            </button>
                            <Spacing bottom={2} />
                            <div className={`${CustomStyle.scrollContainer} ${CustomStyle.chat_box_border}`} id="ChatBox" style={{height:height + "px"}}>
                            <table style={{width:"100%"}} >
                                    <tbody>
                                        {chatMessages && Object.keys(chatMessages).map(index => (
                                        <tr key={`Messages-${chatMessages[index].messageId}`}>
                                            <td>
                                                {/* 事業者側 */}
                                                { !this.props.terria.authorityJudgment() && chatMessages[index].messageType == 1 && (
                                                    <>
                                                        <p className={`${CustomStyle.message_tips} ${CustomStyle.text_right}`}>
                                                            {chatMessages[index].sendDatetime}
                                                        </p>
                                                        <div className={CustomStyle.message_box_self}>
                                                            <p className={CustomStyle.preLine}>{chatMessages[index].messageText}</p>
                                                            <div className={CustomStyle.download_file_area}>
                                                                {chatMessages[index].inquiryFiles && Object.keys(chatMessages[index].inquiryFiles).map(key => (
                                                                    <div style = {{width:"180px", marginRight:"10px"}}>
                                                                        <button className={CustomStyle.download_file_button}
                                                                            title={chatMessages[index].inquiryFiles[key].fileName}
                                                                            onClick={e => this.outputFile("/chat/file/download", chatMessages[index].inquiryFiles[key],"fileName")}
                                                                        >
                                                                            <label className={CustomStyle.download_file_label}>{chatMessages[index].inquiryFiles[key].fileName}</label>
                                                                            <StyledIcon glyph={Icon.GLYPHS.downloadNew}
                                                                                styledWidth={"15px"}
                                                                                styledHeight={"15px"}
                                                                                light
                                                                                className={CustomStyle.download_file_icon} 
                                                                            />
                                                                        </button>
                                                                    </div>
                                                                ))}
                                                            </div>
                                                        </div>
                                                    </>
                                                )}
                                                { !this.props.terria.authorityJudgment() && chatMessages[index].messageType != 1 && (
                                                    <>
                                                        <p className={`${CustomStyle.message_tips}`}>
                                                            {chatMessages[index].sender.departmentName}
                                                            <span style={{marginRight:"15px"}}></span>
                                                            {chatMessages[index].sendDatetime}
                                                        </p>
                                                        <div className={CustomStyle.message_box}>
                                                            <p className={CustomStyle.preLine}>{chatMessages[index].messageText}</p>
                                                        </div>
                                                    </>
                                                )}
                                            </td>
                                        </tr>
                                        ))}
                                    </tbody>
                                </table>
                            </div>
                            <div className={`${CustomStyle.input_message_area} ${CustomStyle.chat_box_border}`} id="inputMessageBox">
                                <table style={{width:"100%",height: "100%"}} >
                                    <tbody>
                                        <tr>
                                            <td style={{textAlign:"center"}}>
                                                <div style={{marginLeft:"10px"}}>
                                                    <textarea  
                                                        id = "messageText"
                                                        type="text" className={CustomStyle.input_message_box} 
                                                        value={messageText}
                                                        placeholder ="メッセージを入力してください"
                                                        onChange={e => this.setState({ messageText: e.target.value })}
                                                    />
                                                </div>
                                            </td>
                                            <td style={{width:"60px",textAlign:"center"}}>
                                                <button id="sendMessage" className={CustomStyle.send_btn}
                                                    // disabled={!this.state.messageText && Object.keys(inquiryFiles).length == 0}
                                                    disabled={!this.isInputed(this.state.messageText , inquiryFiles)}
                                                    
                                                    title="送信" onClick={evt => { this.sendMessage() } }> 
                                                        <StyledIcon glyph={Icon.GLYPHS.send}
                                                            styledWidth={"15px"}
                                                            styledHeight={"15px"}
                                                            light
                                                            // className={`${this.state.messageText == "" && Object.keys(inquiryFiles).length == 0 ? CustomStyle.send_btn_icon_disabled : CustomStyle.send_btn_icon}`}
                                                            className={`${!this.isInputed(this.state.messageText , inquiryFiles)? CustomStyle.send_btn_icon_disabled : CustomStyle.send_btn_icon}`}
                                                        />
                                                </button>
                                            </td>
                                            <td style={{width:"40px",textAlign:"center"}}>
                                                <button id="uploadFile" className={CustomStyle.upload_btn}
                                                    title="添付ファイル" onClick={evt => { this.openUploadFileModal() } }> 
                                                        <StyledIcon glyph={Icon.GLYPHS.fileUpload}
                                                            styledWidth={"30px"}
                                                            styledHeight={"30px"}
                                                            light
                                                            className={CustomStyle.upload_file_icon}
                                                        />
                                                        {Object.keys(inquiryFiles).length > 0 &&( <span className={CustomStyle.badge}>{Object.keys(inquiryFiles).length}</span>)}
                                                </button>
                                            </td>
                                        </tr>
                                    </tbody>
                                </table>
                            </div>
                        </Box >
                    </Box>
                </div>
                <Box padded />
                <div className={CustomStyle.answerContentDiv} style={{width: descriptionDivWidth + "px", left: positionLeft + "px", height:heightForAnswerContent + "px"}}>
                    <div className={CustomStyle.list_item1} style={{ maxHeight: heightForAnswerContent - 10 + "px", overflowY: "auto"}}>
                        <button className={`${CustomStyle.custom_selection} ${answerContentDisplay? CustomStyle.checked: ""}`}
                            onClick={e => {
                                this.setState({answerContentDisplay:!answerContentDisplay})
                            }}
                        >
                            <span>回答登録内容</span>
                            <StyledIcon styledWidth={"12px"} fillColor={"white"}
                                glyph={answerContentDisplay?Icon.GLYPHS.up:Icon.GLYPHS.down}
                            />
                        </button>
                        <Spacing bottom={1} style={{display:answerContentDisplay? "block":"none"}}/>
                        <div className={CustomStyle.answer_content_area} style={{display:answerContentDisplay? "block":"none"}}>
                            <p>対象：{answer.judgementResult}</p>
                            <p>判定結果：{categoryJudgementTitle}</p>
                        </div>
                        <Spacing bottom={1} />
                        <button className={`${CustomStyle.custom_selection} ${activeListId == 1? CustomStyle.checked: ""}`}
                            onClick={e => { this.setState({activeListId:1})}}
                        >
                            <span>回答履歴一覧</span>
                            <StyledIcon style={{margin:"auto 0"}} styledWidth={"12px"} fillColor={"white"} glyph={Icon.GLYPHS.right} />
                        </button>
                        <Spacing bottom={1} />
                        <button className={`${CustomStyle.custom_selection} ${activeListId == 2? CustomStyle.checked: ""}`}
                            onClick={e => { this.setState({activeListId:2})}}
                        >
                            <span>回答ファイル一覧</span>
                            <StyledIcon styledWidth={"12px"} fillColor={"white"} glyph={Icon.GLYPHS.right}/>
                        </button>
                        <Spacing bottom={1} />

                        <button className={`${CustomStyle.custom_selection} ${activeListId == 3? CustomStyle.checked: ""}`}
                            onClick={e => { this.setState({activeListId:3})}}
                        >
                            <span>申請ファイル一覧</span>
                            <StyledIcon styledWidth={"12px"} fillColor={"white"} glyph={Icon.GLYPHS.right}/>
                        </button>
                        <Spacing bottom={1} />
                    </div>
                    <div className={CustomStyle.list_item2}>
                        { activeListId == 1 && ( 
                            <nav className={CustomStyle.custom_nuv}>回答履歴一覧</nav> 
                        )}
                        { activeListId == 2 && ( 
                            <nav className={CustomStyle.custom_nuv}>回答ファイル一覧</nav>  
                        )}
                        { activeListId == 3 && ( 
                            <nav className={CustomStyle.custom_nuv}>申請ファイル一覧</nav> 
                        )}
                        <Spacing bottom={2} />
                        <div style={{ maxHeight: heightForAnswerContent- 70 + "px", overflowY: "auto"}}>
                            { activeListId == 1 && ( 
                                <table className={CustomStyle.selection_table}>
                                    <thead>
                                        <tr className={CustomStyle.table_header}>
                                            <th style={{ width: "25%"}}>回答日時</th>
                                            <th style={{ width: "25%"}}>回答者</th>
                                            <th style={{ width: "35%"}}>回答内容</th>
                                            <th style={{ width: "15%"}}>回答通知</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {answerHistorys && Object.keys(answerHistorys).map(index => (
                                            <tr key={`answerHistorys-${index}`}>
                                                <td>{answerHistorys[index].updateDatetime}</td>
                                                <td>
                                                    {
                                                        answerHistorys[index].answererUser.departmentName + 
                                                        "　" + 
                                                        answerHistorys[index].answererUser.userName
                                                    }
                                                </td>
                                                <td>{answerHistorys[index].answerContent}</td>
                                                <td>
                                                    {answerHistorys[index].notifiedFlag &&(
                                                        <div className={CustomStyle.ellipse}>
                                                            <StyledIcon 
                                                                glyph={Icon.GLYPHS.checked}
                                                                styledWidth={"20px"}
                                                                styledHeight={"20px"}
                                                                light
                                                            />
                                                        </div>  
                                                    )}
                                                </td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            )}
                            { activeListId == 2 && ( 
                                <table className={CustomStyle.selection_table}>
                                    <thead>
                                        <tr className={CustomStyle.table_header}>
                                            <th style={{ width: "20%"}}>回答ファイル</th>
                                            <th style={{ width: "35%"}}>対象</th>
                                            <th style={{ width: "10%"}}>拡張子</th>
                                            <th style={{ width: "35%"}}>ファイル名</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {answerFiles && Object.keys(answerFiles).map(index => (
                                            <tr key={`answerFiles-${index}`}>
                                                <td>
                                                    <div className={CustomStyle.button_vertically}>
                                                        <button 
                                                            className={CustomStyle.download_button} 
                                                            onClick={e => {
                                                                this.outputFile("/answer/file/download", answerFiles[index], "answerFileName");
                                                            }}>
                                                            <span>ダウンロード</span>
                                                        </button>
                                                        {this.extensionCheck(answerFiles[index].answerFileName) && (
                                                            <>
                                                                <button 
                                                                    className={CustomStyle.download_button} 
                                                                    onClick={e => {
                                                                        this.openFile("/answer/file/download", answerFiles[index], "answerFileName");
                                                                    }}>
                                                                    <span>プレビュー</span>
                                                                </button>
                                                            </>
                                                        )}
                                                    </div>
                                                </td>
                                                <td>{answer.judgementResult}</td>
                                                <td>{ this.getExtension(answerFiles[index].answerFileName) }</td>
                                                <td>{answerFiles[index].answerFileName}</td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            )}
                            { activeListId == 3 && ( 
                                <table className={CustomStyle.selection_table}>
                                    <thead>
                                        <tr className={CustomStyle.table_header}>
                                            <th style={{ width: "20%"}}>申請ファイル</th>
                                            <th style={{ width: "35%"}}>対象</th>
                                            <th style={{ width: "10%"}}>拡張子</th>
                                            <th style={{ width: "35%"}}>ファイル名</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {applicationFiles && Object.keys(applicationFiles).map(index => (
                                            (applicationFiles[index]["uploadFileFormList"] && Object.keys(applicationFiles[index]["uploadFileFormList"]).length > 0 && (
                                                <tr key={`applicationFile-${index}`}>
                                                    <td>
                                                        <div className={CustomStyle.button_vertically}>
                                                            <button 
                                                                className={CustomStyle.download_button} 
                                                                onClick={e => {
                                                                    this.output(applicationFiles[index]);
                                                                }}>
                                                                <span>ダウンロード</span>
                                                            </button>
                                                            {this.applicationFileExtensionCheck(applicationFiles[index]["applicationFileHistorys"]) && (
                                                                <>
                                                                    <button 
                                                                        className={CustomStyle.download_button} 
                                                                        onClick={e => {
                                                                            this.preview(applicationFiles[index]);
                                                                        }}>
                                                                        <span>プレビュー</span>
                                                                    </button>
                                                                </>
                                                            )}
                                                        </div>
                                                    </td>
                                                    <td>{applicationFiles[index].applicationFileName}</td>
                                                    <td>{applicationFiles[index].extension}</td>
                                                    <td>
                                                        {applicationFiles[index]["uploadFileFormList"][0].uploadFileName}
                                                        {Object.keys(applicationFiles[index]["uploadFileFormList"] ).length > 1 && ( `,...` )}
                                                    </td>
                                                </tr>
                                            ))
                                        ))}
                                    </tbody>
                                </table>
                            )}
                        </div>
                    </div>
                </div>

                { this.state.viewState.fileDownloadModalShow && (
                    <FileDownLoadModal terria={this.props.terria} viewState={this.props.viewState} t={this.props.t} />
                )}
                { this.state.viewState.inquiryFileUploadModalShow && (
                    <InquiryFileUploadModal terria={this.props.terria} viewState={this.props.viewState} t={this.props.t} />
                )}
                { this.state.viewState.filePreViewModalShow && (
                    <FilePreViewModal terria={this.props.terria} viewState={this.props.viewState} t={this.props.t} callback={this.openFile} />
                )}
            </>
        );
    }
}
export default withTranslation()(withTheme(InputChatMessage));