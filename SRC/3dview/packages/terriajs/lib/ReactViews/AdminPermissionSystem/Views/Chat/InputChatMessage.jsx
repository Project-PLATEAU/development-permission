import { observer } from "mobx-react";
import PropTypes from "prop-types";
import React from "react";
import { withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import Icon, { StyledIcon } from "../../../../Styled/Icon";
import Spacing from "../../../../Styled/Spacing";
import Box from "../../../../Styled/Box";
import CustomStyle from "../../../DevelopmentPermissionSystem/Views/Chat/scss/input-chat-message.scss";
import Styles from "./scss/InputChatMessage.scss";
import FileDownLoadModal from "../Modal/FileDownLoadModal";
import InputChatAddress from "./InputChatAddress";
import Config from "../../../../../customconfig.json";
import FilePreViewModal from "../Modal/FilePreViewModal";

/**
 * 行政：問合せ画面
 */
@observer
class InputChatMessage extends React.Component {
    static displayName = "InputChatMessage";
    static propTypes = {
        terria: PropTypes.object.isRequired,
        viewState: PropTypes.object.isRequired,
        theme: PropTypes.object,
        t: PropTypes.func.isRequired,
        backPage: PropTypes.string
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
            // チャットID
            currentChatId: props.viewState.currentChat.chatId,
            // チャットメッセージ表示エリアの高さ
            height: 0,
            // 回答内容を表示するエリアの幅
            width:0,
            // 回答内容を表示するエリアの高さ
            heightForAnswerContent:0,
            intervalID:null,
            // 回答内容を表示するエリアの位置(左)
            positionLeft:600,
            // 回答内容を表示するエリアの幅
            width:700,
            // 回答内容を表示するエリアの高さ
            heightForAnswerContent:500,
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
            //ログインユーザーID
            loginUserId:"",
            //ログインユーザー部署ID
            loginUserDepartmentId:""
        };
        this.setAddressText = this.setAddressText.bind(this);
        this.openFile = this.openFile.bind(this);
    }

    /**
     * 初期処理
     */
    componentDidMount() {
       // ログインユーザー情報取得
       this.setLoginUserInfo();
       // 問合せ内容取得
        this.getMessageList(false);
        //  問合せの相関情報取得
        this.getChatRelatedInfo();
        // 画面リサイズ
        this.getWindowSize(); 
        // 宛先を初期化する
        this.props.viewState.removeAllInputChatAddress();
        // チャットボックスのリサイズ用
        //メッセージ入力欄の要素取得
        let textarea = document.getElementById('messageText');
        //メッセージ入力欄のinputイベント
        textarea.addEventListener('input', ()=>{
            this.changeTextAreaHight(false,true);
        }); 
 
        // 30秒につき、問合せ内容をリフレッシュする
        let intervalID = setInterval(() => {
            if(this.props.viewState.applyPageActive == "chat" 
                && !this.state.viewState.inputChatAddressModalShow
                && !this.state.viewState.fileDownloadModalShow
                && !this.state.viewState.filePreViewModalShow ){
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
        // 問合せしているチャットID
        let currentChatId = this.state.currentChatId;
        
        fetch(Config.config.apiUrl + "/chat/government/messages", {
            method: 'POST',
            body: JSON.stringify({
                chatId:currentChatId
            }),
            headers: new Headers({ 'Content-type': 'application/json' }),
        })
        .then(res => {
            // 401認証エラーの場合の処理を追加
            if (res.status === 401) {
                alert('認証情報が無効です。ページの再読み込みを行います。');
                window.location.href = "./login/";
                return null;
            }
            return res.json();
        })
        .then(res => {
            if (res.chatId) {
                // チャットメッセージ
                this.setState({chat:JSON.parse(JSON.stringify(res))});
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
     * 問合せの関連情報検索
     */
    getChatRelatedInfo(){
        // 問合せしているチャットID
        let currentChatId = this.state.currentChatId;
        if (currentChatId) {
            fetch(Config.config.apiUrl + "/chat/government/related/" + currentChatId)
            .then(res => {
                // 401認証エラーの場合の処理を追加
                if (res.status === 401) {
                    alert('認証情報が無効です。ページの再読み込みを行います。');
                    window.location.href = "./login/";
                    return null;
                }
                return res.json();
            })
            .then(res => {
                if (res.chatId) {
                    this.setState({
                        answer: res.answer,
                        categoryJudgementTitle: res.categoryJudgementTitle,
                        answerHistory: res.answerHistorys,
                        answerFile: res.answerFiles,
                        applicationFile: res.applicationFiles,
                        ChatRelatedInfoForm: res
                    },
                    this.props.viewState.setLotNumbers(res.lotNumbers));
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
     * 宛先を選択した後で、callback関数
     * 選択された宛先内容より、高さ再計算
     */
    setAddressText(){
        const addressList = this.state.viewState.inputChatAddress;
        let addressText= addressList?.map(address => { 
                return address.departmentName 
            }).filter(departmentName => {
                 return departmentName !== null 
            }).join(", ");
        let elem = document.getElementById('addressText');
        elem.innerHTML = "<span>" + addressText + "</span>";
        this.changeTextAreaHight(false,true);
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
        let addressTextHeight = document.getElementById('addressText').clientHeight;
        if(addressTextHeight < 40){
            addressTextHeight = 40;
        }

        let borderWidth = 2*4+2;
        inputMessageBox.style.height = addressTextHeight + sh + borderWidth + 'px';
        
        //メッセージ表示のボックス
        let chatBox = document.getElementById('ChatBox');
        chatBox.style.height = (this.state.height - (addressTextHeight + sh + borderWidth)) + 'px';

        // 定期リフレッシュの場合、スクロールを移動しない
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
        let height = h - getRect.getBoundingClientRect().top - 20;
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
            this.getWindowSize();
            this.changeTextAreaHight(false,true);
        })
    }

    /**
     * メッセージ投稿
     * @param {*} addressList 宛先リスト
     */
    sendMessage(addressList){
        //一番目の部署IDで事業者であるか判定する。
        let departmentId = addressList[0].departmentId;
        // 宛先が事業者の場合、確認メッセージを表示する
        if(departmentId === "-1"){
            var res = confirm("事業者に送信してよろしいでしょうか？");
            if(res == true){
                this.postMessageApi(addressList);
            }
        }else{
            // 宛先が行政ユーザの場合、メッセージ投稿を行う
            this.postMessageApi(addressList);
        }
    }

    /**
     * メッセージ投稿APIを呼び出す
     * @param {*} addressList 宛先リスト
     */
    postMessageApi(addressList){
        // 問合せしているチャットID
        let chatId = this.state.currentChatId;
        // 投稿内容
        let text = this.state.messageText;
        let message = {"messageText": text};
        // 表示されているメッセージ件数
        let chat = this.state.chat;
        let messageCount = Object.keys( chat["messages"]).length;
        // 最後なメッセージのメッセージID
        let displayedMaxMessageId = chat["messages"][messageCount-1]["messageId"];

        fetch(Config.config.apiUrl + "/chat/government/message/post", {
            method: 'POST',
            body: JSON.stringify({
                chatId:chatId,
                message:message,
                toDepartments:addressList,
                displayedMaxMessageId:displayedMaxMessageId
            }),
            headers: new Headers({ 'Content-type': 'application/json' }),
        })
        .then(res => {
            // 401認証エラーの場合の処理を追加
            if (res.status === 401) {
                alert('認証情報が無効です。ページの再読み込みを行います。');
                window.location.href = "./login/";
                return null;
            }
            return res.json();
        })
        .then(res => {
            // 投稿前後にメッセージ件数を比較して、投稿が成功するか判定
            if (res.messages.length > messageCount) {
                // 最新なメッセージ一覧を表示 
                this.setState({chat:JSON.parse(JSON.stringify(res)), messageText:""});
                // 選択された宛先をクリアする
                this.state.viewState.removeAllInputChatAddress();
                let elem = document.getElementById('addressText');
                elem.innerHTML = "<span></span>";
                // チャットボックスリサイズ
                this.changeTextAreaHight(true,false);
            }else{
                alert('メッセージ送信処理に失敗しました');
            }
        }).catch(error => {
            console.error('処理に失敗しました', error);
            alert('処理に失敗しました');
        });
    }

    /**
     * 遷移元画面へ戻す
     */
    back(){
        // チャット画面の定期リフレッシュ処理をクリアする
        clearInterval(this.state.intervalID);
        let backToPage = this.state.viewState.backToPage;
        this.state.viewState.changeAdminTabActive(backToPage.tab);
        this.state.viewState.changeApplyPageActive(backToPage.page? backToPage.page:"applyList");
    }

    /**
     * 申請情報詳細画面へ遷移
     * @param {*} active アクティブページ
     */
    showApplyDetail(active){
        // チャット画面の定期リフレッシュ処理をクリアする
        clearInterval(this.state.intervalID);
        let applicationId = this.state.ChatRelatedInfoForm.applicationId;
        this.state.viewState.applicationInformationSearchForApplicationId = applicationId;
        this.state.viewState.changeApplyPageActive(active);
        this.state.viewState.setAdminBackPage("applySearch", "chat");
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
            this.openDownFileView(applicationFileHistorys,target);
        }
    }

    /**
     * ファイルダウンロードモーダルを開く
     * @param {*} applicationFile 対象ファイル
     * @param {*} target ターゲット
     */
    openDownFileView(applicationFile,target){
        this.props.viewState.setFileDownloadTarget(applicationFile,target)
        this.state.viewState.changeFileDownloadModalShow();
    }
 
    /**
     * ファイルダウンロード
     * @param {*} path ファイルダウンロード用APIのパス
     * @param {*} file ダウンロード対象ファイル(UploadApplicationFileForm/InquiryFileForm)
     * @param {*} fileNameKey ファイル名に対するキー項目
     */
    outputFile(path, file, fileNameKey) {
        if(file){
            file.applicationId = this.state.ChatRelatedInfoForm.applicationId;
        }
        // APIへのリクエスト
        fetch(Config.config.apiUrl + path, {
            method: 'POST',
            body: JSON.stringify(file),
            headers: new Headers({ 'Content-type': 'application/json' }),
        })
        .then(res => {
            // 401認証エラーの場合の処理を追加
            if (res.status === 401) {
                alert('認証情報が無効です。ページの再読み込みを行います。');
                window.location.href = "./login/";
                return null;
            }
            return res.blob();
        })
        .then(blob => {
            const now = new Date();
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
     * 宛先選択ダイアログ画面を開く
     */
    inputChatAddress(){
        this.state.viewState.changeInputChatAddressModalShow();
    }

    /**
     * 自分から投稿するメッセージである判定
     * @param {*} chatMessage メッセージ
     * @returns 判断結果
     */
    isSelf(chatMessage){
        let isSelf = false;
        let loginUserId = this.state.loginUserId;

        // メッセージタイプが「1：事業者→行政」の場合、
        if(chatMessage.messageType == 1){
            return isSelf;
        }

        if (loginUserId == chatMessage.sender.userId){
            isSelf = true;
        }

        return isSelf;
    }
    
    /**
     * トークンからログインユーザーID、部署を取得する
     */
    setLoginUserInfo(){
        let userId = "";
        let departmentId = "";
        let decoded = {};
        if (document.cookie) {
            let token = "";
            const cookiesArray = document.cookie.split(';');
            cookiesArray.forEach(data => {
                data = data.split('=');
                data[0] = data[0].trim(' ');
                if(data[0] === "token"){
                    token = data[1];
                }
            });
            if(token !== ""){
                const base64Url = token.split('.')[1];
                const base64 = decodeURIComponent(atob(base64Url).split('').map(function(c) {
                    return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
                }).join(''));
                decoded = JSON.parse(base64);
                if(decoded["X-USERID"]){
                    userId = decoded["X-USERID"];
                }
                if(decoded["X-DEPARTMENT"]){
                    departmentId = decoded["X-DEPARTMENT"];
                }
            }
        }
        this.setState({loginUserId: userId,loginUserDepartmentId: departmentId});
    }

    /**
     * メッセージの送信者情報をもとに、表示内容を編集する
     * @param {*} chatMessage メッセージ
     * @returns 送信者の表示文言
     */
    getSenderDisplayText(chatMessage){

        let displayText = "";

        // メッセージタイプが「1：事業者→行政」の場合、
        if(chatMessage.messageType == 1){
            return displayText = "事業者";
        }

        // メッセージタイプが「1：事業者→行政」以外の場合、
        displayText = chatMessage.sender.departmentName + " " + chatMessage.sender.userName;
        return displayText;
    }

    /**
     * 拡張子取得
     * @param {String} fileName ファイル名
     * @returns 拡張子
     */
    getExtension(fileName){
        // 拡張子チェック
        let extension = fileName.split('.').pop();
        return extension;
    }

    /**
     * メッセージ上の宛先表示内容を編集
     * @param {*} chatMessage メッセージ
     * @returns 宛先の表示文言
     */
    getAddressDisplayText(chatMessage){
        let inquiryAddress = chatMessage.inquiryAddressForms;

        let displayText = inquiryAddress?.map(address => { 
            return address.department.departmentName
        }).filter(departmentName => {
             return departmentName !== null 
        }).join("、");

        // 事業者の場合、「事業者」で表示する
        if(displayText == "" || displayText == null ){
            if(inquiryAddress[0].department.departmentId = "-1"){
                displayText = "事業者";
            }
        }

        return displayText;
    }

    /**
     * ファイルプレビュー
     * @param {*} path ファイルダウンロード用APIのパス
     * @param {*} file ダウンロード対象ファイル
     * @param {*} fileNameKey ファイル名に対するキー項目
     */
    openFile(path, file, fileNameKey) {
        if(file){
            file.applicationId = file.applicationId;
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
     * プレビューファイルの拡張子チェック
     * @param {*} fileName ファイル名
     * @returns チェック結果
     */
    extensionCheck(fileName){
        const extArray=["PDF","PNG","JPG"];
        // 拡張子確認（PDF,PNG,JPG）
        let extension = this.getExtension(fileName).toLocaleUpperCase();
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
     * @param {Object} 対象ファイル情報
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
     * @param {*} applicationFile 申請ファイル
     * @param {*} target ターゲット
     */
    openPreviewFileView(applicationFile,target,previwFlag){
        this.setState({previwFlag})
        this.props.viewState.setFileDownloadTarget(applicationFile,target)
        this.state.viewState.changeFilePreViewModalShow();
    }

    /**
     * メッセージと宛先部署が入力したか確認
     * @param {*} text 入力したメッセージ内容 
     * @param {*} addressList 宛先リスト
     */
    isInputed(text, addressList){

        if(text){
            text = text.trim();
        }
        if(text && Object.keys(addressList).length > 0 ){
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
        let addressList = this.state.viewState.inputChatAddress;
        const t = this.props.t;

        let answer = this.state.answer;
        let categoryJudgementTitle = this.state.categoryJudgementTitle;
        let answerHistorys = this.state.answerHistory;
        let answerFiles = this.state.answerFile;
        let applicationFiles = this.state.applicationFile;
        let answerContentDisplay = this.state.answerContentDisplay;
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
                            <button
                                className={`${CustomStyle.btn_baise_style} ${Styles.details_button}`}
                                // style={{width:"20%", backgroundColor: "orange", color:"white"}}
                                onClick={e => 
                                    this.showApplyDetail("applyDetail")
                                }
                            >
                                <span>申請詳細</span>
                            </button>
                            <Spacing bottom={2} />
                            <div className={`${CustomStyle.scrollContainer} ${CustomStyle.chat_box_border}`} id="ChatBox" style={{height:height + "px"}}>
                                <table style={{width:"100%"}} >
                                    <tbody>
                                        {chatMessages && Object.keys(chatMessages).map(index => (
                                        <tr key={`Messages-${chatMessages[index].messageId}`}>
                                            <td>
                                                {/* 自分から送信場合 */}
                                                {this.isSelf(chatMessages[index]) && (
                                                    // レイアウト案①
                                                    // <div className={Styles.self_mesage}>
                                                    //     <p className={`${CustomStyle.message_tips}`}>
                                                    //         {`FROM: ${this.getDisplayName(chatMessages[index])}`}
                                                    //     </p>
                                                    //     <p className={`${CustomStyle.message_tips}`}>
                                                    //         {`TO: ${this.getDisplayNameTO(chatMessages[index])}`}
                                                    //     </p>
                                                    //     <Spacing bottom={2}/>
                                                    //     <div className={Styles.self_mesage_content}>
                                                    //         <p className={CustomStyle.preLine}>{chatMessages[index].messageText}</p>
                                                    //     </div>
                                                    //     <p className={`${CustomStyle.message_tips} ${CustomStyle.text_right}`}>
                                                    //         {chatMessages[index].sendDatetime}
                                                    //     </p>
                                                    // </div>
                                                    // レイアウト案②
                                                    <div className={Styles.self_mesage}>
                                                        <div className={Styles.mesage_address}>
                                                            <p className={`${CustomStyle.message_tips}`}>
                                                                {`FROM: ${this.getSenderDisplayText(chatMessages[index])}`}
                                                            </p>
                                                            <p className={`${CustomStyle.message_tips}`}>
                                                                {`TO: ${this.getAddressDisplayText(chatMessages[index])}`}
                                                            </p>
                                                        </div>
                                                        <p className={`${CustomStyle.message_tips} ${CustomStyle.text_right}`}>
                                                            {chatMessages[index].sendDatetime}
                                                        </p>
                                                        <div className={Styles.self_mesage_content}>
                                                            <p className={CustomStyle.preLine}>{chatMessages[index].messageText}</p>
                                                        </div>
                                                    </div>
                                                )}
                                                {/* 他ユーザーから送信場合 */}
                                                {!this.isSelf(chatMessages[index]) && (
                                                    // レイアウト案①
                                                    // <div className={Styles.other_mesage}>
                                                    //     <p className={`${CustomStyle.message_tips}`}>
                                                    //         {`FROM: ${this.getDisplayName(chatMessages[index])}`}
                                                    //     </p>
                                                    //     <p className={`${CustomStyle.message_tips}`}>
                                                    //         {`TO: ${this.getDisplayNameTO(chatMessages[index])}`}
                                                    //     </p>
                                                    //     <Spacing bottom={2}/>
                                                    //     <div className={Styles.other_mesage_content}>
                                                    //         <p className={CustomStyle.preLine}>{chatMessages[index].messageText}</p>
                                                    //         <div className={CustomStyle.download_file_area} >
                                                    //             {Object.keys(chatMessages[index].inquiryFiles).length > 0 && (
                                                    //                 <div className={CustomStyle.download_file_area}>
                                                    //                     { Object.keys(chatMessages[index].inquiryFiles).map(key => (
                                                    //                         <div style={{width:"180px",marginRight:"10px"}}>
                                                    //                             <button className={CustomStyle.download_file_button}
                                                    //                                 title={chatMessages[index].inquiryFiles[key].fileName}
                                                    //                                 onClick={e => this.outputFile("/chat/file/download", chatMessages[index].inquiryFiles[key],"fileName")}
                                                    //                             >
                                                    //                                 <label className={CustomStyle.download_file_label}>{chatMessages[index].inquiryFiles[key].fileName}</label>
                                                    //                                 <StyledIcon glyph={Icon.GLYPHS.downloadNew}
                                                    //                                     styledWidth={"15px"}
                                                    //                                     styledHeight={"15px"}
                                                    //                                     light
                                                    //                                     className={CustomStyle.download_file_icon}
                                                    //                                 />
                                                    //                             </button>
                                                    //                         </div>
                                                    //                     ))}
                                                    //                 </div>
                                                    //             )}
                                                    //         </div>
                                                    //     </div>
                                                    //     <p className={`${CustomStyle.message_tips}`}>
                                                    //         {chatMessages[index].sendDatetime}
                                                    //     </p>
                                                    // </div>
                                                    // レイアウト案②
                                                    <div className={Styles.other_mesage}>
                                                        <div className={Styles.mesage_address}>
                                                            <p className={`${CustomStyle.message_tips}`}>
                                                                {`FROM: ${this.getSenderDisplayText(chatMessages[index])}`}
                                                            </p>
                                                            <p className={`${CustomStyle.message_tips}`}>
                                                                {`TO: ${this.getAddressDisplayText(chatMessages[index])}`}
                                                            </p>
                                                        </div>
                                                        <p className={`${CustomStyle.message_tips}`}>
                                                            {chatMessages[index].sendDatetime}
                                                        </p>
                                                        <div className={Styles.other_mesage_content}>
                                                            <p className={CustomStyle.preLine}>{chatMessages[index].messageText}</p>
                                                            <div className={CustomStyle.download_file_area} >
                                                                {Object.keys(chatMessages[index].inquiryFiles).length > 0 && (
                                                                    <div className={CustomStyle.download_file_area}>
                                                                        { Object.keys(chatMessages[index].inquiryFiles).map(key => (
                                                                            <div style={{width:"180px",marginRight:"10px"}}>
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
                                                                )}
                                                            </div>
                                                        </div>
                                                    </div>
                                                )}
                                            </td>
                                        </tr>
                                        ))}
                                    </tbody>
                                </table>
                            </div>
                            <div className={`${Styles.input_message_area} ${CustomStyle.chat_box_border}`} id="inputMessageBox">
                                <table style={{width:"100%",height: "100%"}} >
                                    <tbody>
                                        <tr key={`input-address-line`}>
                                            <td style={{width:"150px",verticalAlign:"middle"}}>
                                                <button className={`${Styles.address_btn}`} onClick={e => this.inputChatAddress()}>
                                                    <span>＠</span>
                                                </button>
                                                <span style={{ marginLeft: "20px"}}>{`送信先：`}</span>
                                            </td>
                                            <td colSpan={2} style={{verticalAlign:"middle"}} >
                                                <div id="addressText"></div>
                                            </td>
                                        </tr>
                                        <tr key={`input-message-line`}>
                                            <td style={{textAlign:"center"}} colSpan={2}>
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
                                                    // disabled={!this.state.messageText || Object.keys(addressList).length == 0}
                                                    disabled={!this.isInputed(this.state.messageText , addressList)}
                                                    title="送信" onClick={evt => { this.sendMessage(addressList) } }> 
                                                        <StyledIcon glyph={Icon.GLYPHS.send}
                                                            styledWidth={"15px"}
                                                            styledHeight={"15px"}
                                                            light
                                                            // className={`${!this.state.messageText || Object.keys(addressList).length == 0 ? CustomStyle.send_btn_icon_disabled : CustomStyle.send_btn_icon}`}
                                                            className={`${!this.isInputed(this.state.messageText , addressList)? CustomStyle.send_btn_icon_disabled : CustomStyle.send_btn_icon}`}
                                                       />
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
                <div className={Styles.answerContentDiv} style={{width: descriptionDivWidth + "px", left: positionLeft + "px", height:heightForAnswerContent + "px"}}>
                    <div className={Styles.list_item1} style={{ maxHeight: heightForAnswerContent - 10 + "px", overflowY: "auto"}}>
                        <button className={`${Styles.custom_selection} ${answerContentDisplay? Styles.checked: ""}`}
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
                        <div className={Styles.answer_content_area} style={{display:answerContentDisplay? "block":"none"}}>
                            <p>対象：{answer.judgementResult}</p>
                            <p>判定結果：{categoryJudgementTitle}</p>
                        </div>
                        <Spacing bottom={1} />
                        <button className={`${Styles.custom_selection} ${activeListId == 1? Styles.checked: ""}`}
                            onClick={e => { this.setState({activeListId:1})}}
                        >
                            <span>回答履歴一覧</span>
                            <StyledIcon style={{margin:"auto 0"}} styledWidth={"12px"} fillColor={"white"} glyph={Icon.GLYPHS.right} />
                        </button>
                        <Spacing bottom={1} />
                        <button className={`${Styles.custom_selection} ${activeListId == 2? Styles.checked: ""}`}
                            onClick={e => { this.setState({activeListId:2})}}
                        >
                            <span>回答ファイル一覧</span>
                            <StyledIcon styledWidth={"12px"} fillColor={"white"} glyph={Icon.GLYPHS.right}/>
                        </button>
                        <Spacing bottom={1} />

                        <button className={`${Styles.custom_selection} ${activeListId == 3? Styles.checked: ""}`}
                            onClick={e => { this.setState({activeListId:3})}}
                        >
                            <span>申請ファイル一覧</span>
                            <StyledIcon styledWidth={"12px"} fillColor={"white"} glyph={Icon.GLYPHS.right}/>
                        </button>
                        <Spacing bottom={1} />
                    </div>
                    <div className={Styles.list_item2}>
                        { activeListId == 1 && ( 
                            <nav className={Styles.custom_nuv}>回答履歴一覧</nav> 
                        )}
                        { activeListId == 2 && ( 
                            <nav className={Styles.custom_nuv}>回答ファイル一覧</nav>  
                        )}
                        { activeListId == 3 && ( 
                            <nav className={Styles.custom_nuv}>申請ファイル一覧</nav> 
                        )}
                        <Spacing bottom={2} />
                        <div style={{ maxHeight: heightForAnswerContent- 70 + "px", overflowY: "auto"}}>
                            { activeListId == 1 && ( 
                                <table className={Styles.selection_table}>
                                    <thead>
                                        <tr className={Styles.table_header}>
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
                                                      <div className={Styles.ellipse}>
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
                                <table className={Styles.selection_table}>
                                    <thead>
                                        <tr className={Styles.table_header}>
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
                                                            className={Styles.download_button} 
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
                                <table className={Styles.selection_table}>
                                    <thead>
                                        <tr className={Styles.table_header}>
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
                                                                className={Styles.download_button} 
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

                { this.state.viewState.inputChatAddressModalShow && (
                    <InputChatAddress terria={this.props.terria} viewState={this.props.viewState} t={t}  callback={this.setAddressText} selfDepartmentId={this.state.loginUserDepartmentId}/>
                )}

                { this.state.viewState.fileDownloadModalShow && (
                    <FileDownLoadModal terria={this.props.terria} viewState={this.props.viewState} t={t} />
                )}

                { this.state.viewState.filePreViewModalShow && (
                    <FilePreViewModal terria={this.props.terria} viewState={this.props.viewState} t={this.props.t} callback={this.openFile} />
                )}
            </>
        );
    }
}
export default withTranslation()(withTheme(InputChatMessage));