import { observer } from "mobx-react";
import PropTypes, { number, object } from "prop-types";
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
import InputChatAddress from "../../../AdminPermissionSystem/Views/Chat/InputChatAddress";
import testdata from "./testdata.json";
import webMapServiceCatalogItem from '../../../../Models/Catalog/Ows/WebMapServiceCatalogItem';
import CommonStrata from "../../../../Models/Definition/CommonStrata";

/**
 * 事業者/行政：問合せ画面
 */
@observer
class InputChatMessage extends React.Component {
    static displayName = "InputChatMessage";
    static propTypes = {
        terria: PropTypes.object.isRequired,
        viewState: PropTypes.object.isRequired,
        theme: PropTypes.object,
        t: PropTypes.func.isRequired,
        // 申請種別
        activeApplicationStepId: PropTypes.string,
        // 事前相談の場合、回答IDを渡す、事前協議/許可判定の場合、担当課の部署IDを渡す
        activeChatKey: PropTypes.string,
        // 問合せしたい申請のID
        applicationId: PropTypes.string
    }
    constructor(props) {
        super(props);
        this.state = {
            //　共通引数
            viewState: props.viewState,
            //　共通引数
            terria: props.terria,
            // 共通引数：入力したメッセージ内容
            messageText:  "",
            // リサイズ引数：チャットメッセージ表示エリアの高さ
            height: 0,
            // リサイズ引数：関連情報表示エリアの位置(左)
            positionLeft:600,
            // リサイズ引数：関連情報表示エリアの幅
            width:7000,
            // リサイズ引数：関連情報表示エリアの高さ
            heightForAnswerContent:500,
            // 廃止
            // 該当回答内容に対する問合せ情報リスト
            chat: {},
            // 行政からの回答一覧
            answers: props.viewState.answerContent.answers,
            // チャットに紐づく回答ID
            currentAnswerId: props.viewState.currentAnswerId,
            // 申請ID
            applicationId: props.viewState.currentChat.applicationId,
            // 定期リフレッシュ引数
            intervalID:null,
            // 関連情報表示エリア：回答リスト
            answer:[],
            // 関連情報表示エリア：回答登録内容表示フラグ
            answerContentDisplay:false,
            // 関連情報表示エリア：回答履歴
            answerHistory: [],
            // 関連情報表示エリア：回答ファイル
            answerFile: [],
            // 関連情報表示エリア：申請ファイル
            applicationFile: [],
            // 関連情報表示エリア：表示している一覧ID
            activeListId:0,
            // 関連情報表示エリア：選択されている回答
            selectedAnswer:{},
            // メッセージ表示エリア：チャットグループ
            chatGroupInfo: [],
            // メッセージ表示エリア：申請段階ID
            activeChatGroupId:props.activeApplicationStepId,
            // メッセージ表示エリア：チャットID
            activeMessageGroupId:props.activeChatKey,
            // 行政専用：部署回答ID
            departmentAnswerId:0,
            // 行政専用：ログインユーザーID
            loginUserId:"0001",
            // 行政専用：ログインユーザー部署ID
            loginUserDepartmentId:"10001",
            // 行政専用：チャットID
            currentChatId: props.viewState.currentChat.chatId,
            // リフレッシュ中判定フラグ(true:定期更新をスキップ,false:定期更新続行)
            refreshingFlag:false
        };
        this.setAddressText = this.setAddressText.bind(this);
        this.openFile = this.openFile.bind(this);
        this.resizeEvent = () => {
            this.getWindowSize();
            this.changeTextAreaHight(false,true);
        };
    }

    /**
     * クリーンアップ処理
     */
    componentWillUnmount(){
        window.removeEventListener('resize', this.resizeEvent);
        let map = document.getElementById("terriaViewer");
        map.style.maxHeight = "100%";
        this.props.viewState.triggerResizeEvent();
    }

    /**
     * 初期処理
     */
    componentDidMount() {
        //リサイズイベント
        window.removeEventListener('resize', this.resizeEvent);
        window.addEventListener('resize', this.resizeEvent);

        //  問合せのメッセージと相関情報取得
        this.getChatRelatedInfo(false,()=>this.changeTextAreaHight(false,false));
    
        // 画面リサイズ
        this.getWindowSize(); 
        this.props.viewState.triggerResizeEvent();

        // 行政の場合、
        if(this.props.terria.authorityJudgment()){
            // 宛先を初期化する
            this.props.viewState.removeAllInputChatAddress();
            // ログインユーザー情報取得
            this.setLoginUserInfo();
        }

        // チャットボックスのリサイズ用
        //メッセージ入力欄の要素取得
        let textarea = document.getElementById('messageText');
        //メッセージ入力欄のinputイベント
        textarea.addEventListener('input', ()=>{
            this.changeTextAreaHight(false,true);
        }); 
        
        // 定期更新処理 
        let intervalID = setInterval(() => {
            //定期更新,リフレッシュ処理排他制御
            if(this.state.refreshingFlag){
                return;
            }
            if(!this.props.terria.authorityJudgment()){
                //事業者の場合
                if(this.props.viewState.showChatView
                    && !this.props.viewState.inquiryFileUploadModalShow
                    && !this.props.viewState.fileDownloadModalShow
                    && !this.props.viewState.filePreViewModalShow){
                    this.getChatRelatedInfo(true);
                }else{
                    return;
                }
            }else{
                //行政の場合、
                if(this.props.viewState.applyPageActive == "chat" 
                    && !this.props.viewState.inputChatAddressModalShow
                    && !this.props.viewState.fileDownloadModalShow
                    && !this.props.viewState.filePreViewModalShow ){
                    this.getChatRelatedInfo(true);  
                }else{
                    return;
                }
            }
        }, 30000);

        this.setState({intervalID:intervalID});
        
    }

    /**
     * 問合せと関連情報取得
     */
    getChatRelatedInfo(isRefresh = false,callbackFunc = null ){
        let getChatMessagesApi = "";
        let getRelatedInfoApi = "";
        let requestBody = {};
        //定期更新,リフレッシュ処理排他制御
        this.setState({refreshingFlag:isRefresh});
        //事業者の場合
        if(!this.props.terria.authorityJudgment()){
            getChatMessagesApi = "/chat/business/messages";
            getRelatedInfoApi = "/chat/business/related";
            let answerContent = this.props.viewState.answerContent;
            let applicationId = this.props.viewState.applicationId;
            let currentChatId = this.state.currentChatId?this.state.currentChatId:null;
            let activeMessageGroupId = this.state.activeMessageGroupId;
            let applicationStepId = this.props.viewState.checkedApplicationStepId?this.props.viewState.checkedApplicationStepId:null;
            let answerId = this.props.viewState.currentAnswerId?this.props.viewState.currentAnswerId:null;
            let departmentAnswerId = this.props.viewState.currentDepartmentAnswerId?this.props.viewState.currentDepartmentAnswerId:null;
            requestBody = {
                loginId: answerContent?.loginId,
                password:answerContent?.password,
                applicationId:applicationId,
                answerId:answerId,
                applicationStepId:applicationStepId,
                departmentAnswerId:departmentAnswerId
            };
            if(currentChatId){
                requestBody["chatId"] = currentChatId;
            }
            if(activeMessageGroupId){
                requestBody["chatId"] = activeMessageGroupId;
            }
        //行政の場合
        }else{ 
            getChatMessagesApi = "/chat/government/messages";
            getRelatedInfoApi = "/chat/government/related";
            let currentChatId = this.state.currentChatId?this.state.currentChatId:null;
            let activeMessageGroupId = this.state.activeMessageGroupId;
            let applicationId = this.props.viewState.applicationId?this.props.viewState.applicationId:null;
            let applicationStepId = this.props.viewState.checkedApplicationStepId?this.props.viewState.checkedApplicationStepId:null;
            let answerId = this.props.viewState.currentAnswerId?this.props.viewState.currentAnswerId:null;
            let departmentAnswerId = this.props.viewState.currentDepartmentAnswerId?this.props.viewState.currentDepartmentAnswerId:null;
            requestBody = {
                applicationId:applicationId,
                answerId:answerId,
                applicationStepId:applicationStepId,
                departmentAnswerId:departmentAnswerId
            };
            if(currentChatId){
                requestBody["chatId"] = currentChatId;
            }
            if(activeMessageGroupId){
                requestBody["chatId"] = activeMessageGroupId;
            }
        }

        fetch(Config.config.apiUrl + getChatMessagesApi, {
            method: 'POST',
            body: JSON.stringify(requestBody),
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
            const chatMessageslist = JSON.parse(JSON.stringify(res));
            if (Object.keys(chatMessageslist).length > 0) {

                const displayChatMessageslist = this.createDisplayChatMessageslist(chatMessageslist);
        
                // チャットのグループレベルをセット
                let currentChatId = this.state.currentChatId;
                let activeChatGroupId = this.state.activeChatGroupId;
                let activeMessageGroupId = this.state.activeMessageGroupId;
                // 申請段階IDに基づく初回セット
                if(!currentChatId && !activeChatGroupId && !activeMessageGroupId){
                    const applicationStepId = this.props.viewState.checkedApplicationStepId;
                    const currentApplicationStepData = displayChatMessageslist?.find((item) => item.applicationStepId == applicationStepId );
                    currentChatId = currentApplicationStepData.chatList?.find((chat) => chat.answerId == this.props.viewState.currentAnswerId)?.chatId;
                    //事前協議
                    if(applicationStepId == 2){
                        currentChatId = currentApplicationStepData.chatList?.find((chat) => chat.departmentAnswerId == this.props.viewState.currentDepartmentAnswerId)?.chatId;
                    //許可判定
                    }else if(applicationStepId ==3){
                        currentChatId = currentApplicationStepData.chatList[0]?.chatId;
                    }
                    activeChatGroupId = applicationStepId;
                    activeMessageGroupId = currentChatId;
                    this.setState({
                        currentChatId:currentChatId,
                        activeChatGroupId: activeChatGroupId, 
                        activeMessageGroupId: activeMessageGroupId,
                    });
                // チャットIDに基づく初回セット
                }else if(currentChatId && !activeChatGroupId && !activeMessageGroupId){
                    for(let i=0;i<displayChatMessageslist.length;i++){
                        const chat = displayChatMessageslist[i].chatList.find(chat=>chat.chatId == currentChatId)
                        if(chat){
                            activeChatGroupId = displayChatMessageslist[i].applicationStepId;
                            activeMessageGroupId = chat.chatId;
                        }
                    }
                    this.setState({
                        activeChatGroupId: activeChatGroupId, 
                        activeMessageGroupId: activeMessageGroupId,
                    });
                }
                const currentApplicationStepData = displayChatMessageslist.find((item) => item.applicationStepId == activeChatGroupId );
                let currentChatData = currentApplicationStepData.chatList.find(item=>item.chatId == activeMessageGroupId);
                if(!currentChatData) currentChatData = currentApplicationStepData.chatList[0];
                const departmentAnswerId = currentChatData.departmentAnswerId;

                // 関連情報取得API呼び出し
                requestBody["applicationId"] = currentChatData.applicationId;
                requestBody["chatId"] = activeMessageGroupId;
                requestBody["departmentAnswerId"] = departmentAnswerId;
                requestBody["answerId"] = currentChatData.answerId;
                requestBody["applicationStepId"] = currentChatData.applicationStep.applicationStepId;
                fetch(Config.config.apiUrl + getRelatedInfoApi, {
                    method: 'POST',
                    body: JSON.stringify(requestBody),
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
                    if(res.applicationId == requestBody["applicationId"]){
                        // 回答DTO
                        const answer = res.answer;
                        // 回答履歴
                        let answerHistory = res.answerHistorys;
                        // 回答ファイル
                        let answerFile = res.answerFiles;
                        // 申請ファイル
                        const applicationFile = res.applicationFiles;
                        // 対象の回答情報
                        let selectedAnswer = answer.find(item=>item.answerId == currentChatData?.answerId);
                        const applicationStepId = activeChatGroupId;
                        // 事前協議
                        if(applicationStepId == 2){
                            selectedAnswer = answer.find(item=>item.departmentAnswerId == currentChatData?.departmentAnswerId);
                        // 許可判定
                        }else if(applicationStepId ==3){
                            selectedAnswer = answer[0];
                        }
                        // 事前相談の場合,対象の回答履歴・回答ファイルのみ表示対象とする
                        if(applicationStepId == 1 && selectedAnswer){
                            answerHistory = selectedAnswer.answerHistorys;
                            answerFile = selectedAnswer.answerFiles;
                        }else if(applicationStepId == 1 && !selectedAnswer){
                            answerHistory = [];
                            answerFile = [];
                        }
                        this.setState({
                            currentChatId:activeMessageGroupId,
                            applicationId:res.applicationId,
                            departmentAnswerId:departmentAnswerId,
                            chatGroupInfo: displayChatMessageslist,
                            answer: answer,
                            selectedAnswer: selectedAnswer,
                            answerHistory: answerHistory,
                            answerFile: answerFile,
                            applicationFile: applicationFile,
                            refreshingFlag:false
                        },()=>{
                            //コールバック処理の指定がある場合呼び出し
                            callbackFunc?.();
                        });
                        const lotNumbers = res.lotNumbers;
                        this.props.viewState.setLotNumbers(lotNumbers);
                        setTimeout(()=>{
                            this.props.viewState.mapFitButtonFunction();
                            // 申請地のレイヤを表示する(申請情報検索からの流用)
                            try{
                                if(this.props.terria.authorityJudgment()){
                                    const wmsUrl = Config.config.geoserverUrl;
                                    const items = this.props.terria.workbench.items;
                                    let layerFlg = false;
                                    for (const aItem of items) {

                                        // 表示可能な関係ないレイヤをクリアする
                                        // 地番検索結果（事業者）→申請地番の仕様変更（f503d321）より、行政側の地番検索機能は事業者側と同じようになる
                                        // 選択中地番
                                        // 申請中地番→対象申請以外の地番も含むため、一緒にクリアする
                                        if (aItem.uniqueId === Config.layer.lotnumberSearchLayerNameForBusiness ||
                                            aItem.uniqueId === Config.layer.lotnumberSearchLayerNameForSelected ||
                                            aItem.uniqueId === Config.layer.lotnumberSearchLayerNameForApplying ) {
                                            this.state.terria.workbench.remove(aItem);
                                            aItem.loadMapItems();
                                        }
                                        this.props.viewState.showApplicationSearchTargetLayer(true , this.state.applicationId);
                                        if (aItem.uniqueId === Config.layer.lotnumberSearchLayerNameForApplicationSearchTarget) {
                                            aItem.setTrait(CommonStrata.user,
                                                "parameters",
                                                {
                                                    "viewparams": Config.layer.lotnumberSearchViewParamNameForApplicationSearchTarget + res.applicationId,
                                                });
                                            aItem.loadMapItems();
                                            layerFlg = true;
                                        }
                                    }
                                    if(!layerFlg){
                                        const item = new webMapServiceCatalogItem(Config.layer.lotnumberSearchLayerNameForApplicationSearchTarget, this.state.terria);
                                        item.setTrait(CommonStrata.definition, "url", wmsUrl);
                                        item.setTrait(CommonStrata.user, "name", Config.layer.lotnumberSearchLayerDisplayNameForApplicationSearchTarget);
                                        item.setTrait(
                                            CommonStrata.user,
                                            "layers",
                                            Config.layer.lotnumberSearchLayerNameForApplicationSearchTarget);
                                        item.setTrait(CommonStrata.user,
                                            "parameters",
                                            {
                                                "viewparams": Config.layer.lotnumberSearchViewParamNameForApplicationSearchTarget + res.applicationId,
                                            });
                                        item.loadMapItems();
                                        this.state.terria.workbench.add(item);
                                    }
                                }
                            } catch (error) {
                                console.error('処理に失敗しました', error);
                            }
                        },2000)
                    }
                }).catch(error => {
                    console.error('通信処理に失敗しました', error);
                    alert('通信処理に失敗しました');
                });
            }else{
                alert('チャットメッセージ一覧取得処理に失敗しました');
            }
        }).catch(error => {
            console.error('通信処理に失敗しました', error);
            alert('通信処理に失敗しました');
        }).finally(()=>{
            this.changeTextAreaHight(false,isRefresh);
        })
    }


    /**
     * 検索結果より、画面に表示する用リストに転換する
     */
    createDisplayChatMessageslist(chatMessageslist){
        let displayChatMessageslist = [];

        for(let i=0; chatMessageslist && i<chatMessageslist.length; i++){
            let currentData = chatMessageslist[i];
            let indexDisplay = displayChatMessageslist.findIndex((item) => item.applicationStepId == currentData.applicationStep.applicationStepId);
            // 申請種別IDが表示リストに存在しない場合、申請種別IDで項目追加
            if(indexDisplay < 0){
                displayChatMessageslist.push(
                    {
                        "applicationStepId":currentData.applicationStep.applicationStepId,
                        "applicationStepName":currentData.applicationStep.applicationStepName,
                        "chatList":[currentData]
                    }
                );
            }else{
                displayChatMessageslist[indexDisplay]["chatList"].push(currentData);
            }
        }

        return displayChatMessageslist;
    }

    /**
     * 入力される内容に合わせて各要素の高を調整する
     * @param {*} isSendEvent 送信クリックイベントであるフラグ
     * @param {*} isRefresh 定期リフレッシュイベントであるフラグ
     */
    changeTextAreaHight(isSendEvent,isRefresh){
        try{
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
            //メッセージ表示のボックス
            let chatBox = document.getElementById('ChatBox');
            let messageListBox = document.getElementById('messageListBox');

            if(this.props.terria.authorityJudgment()){

                let addressTextHeight = document.getElementById('addressText').clientHeight;
                if(addressTextHeight < 40){
                    addressTextHeight = 40;
                }

                let borderWidth = 2*4+2;
                inputMessageBox.style.height = addressTextHeight + sh + borderWidth + 'px';
                chatBox.style.height = (this.state.height - (addressTextHeight + sh + borderWidth) + 90) + 'px';
            }else{

                inputMessageBox.style.height = (sh + 30) + 'px';
                chatBox.style.height = (this.state.height - (sh + 30) + 70) + 'px';
            }

            // リフレッシュ以外の場合、スクロールを最後に、移動する
            if(!isRefresh){
                // スクロールは最後にする
                setTimeout(() => {
                    //chatBox.scrollTo(0,chatBox.scrollHeight)
                    //タブまでのコンテナスクロール位置
                    let activeMessageGroupId = this.state.activeMessageGroupId;
                    if(activeMessageGroupId){
                        let targetElement = document.getElementById("title2-"+activeMessageGroupId);
                        // 要素の位置を取得してスクロール
                        if (targetElement && chatBox) {
                            // 要素の位置を取得し、余白を追加してスクロール位置を設定
                            let offset = 40; // 余白の高さ（ピクセル）
                            let elementPosition = targetElement.offsetTop - chatBox.offsetTop - offset;
                            if(elementPosition < 0){
                                elementPosition = 0;
                            }
                            chatBox.scrollTo({
                                top: elementPosition,
                                behavior: "smooth"
                            });
                        }
                    }
                    //タブ内のスクロール位置は常に最下部
                    if(messageListBox){
                        messageListBox.scrollTo(0,messageListBox.scrollHeight)
                    }
                }, 300);
            }
        }catch(e){
            console.log(e.message);
        }
    }

    /**
     * 高さ再計算
     */
    getWindowSize() {
        try{
            let win = window;
            let e = window.document.documentElement;
            let g = window.document.documentElement.getElementsByTagName('body')[0];
            let h = win.innerHeight|| e.clientHeight|| g.clientHeight;
            let w = win.innerWidth|| e.clientWidth|| g.clientWidth;
            const getRect = document.getElementById("ChatBox");
            let height = h - getRect.getBoundingClientRect().top - 20;
            if(this.props.terria.authorityJudgment()){
                // 宛先と入力欄の高さを減る
                height = height - 90;
            }else{
                // 入力欄の高さを減る
                height = height - 70;
            }
            const sidePanel = document.getElementById("SidePanel");
            let width = w - sidePanel.clientWidth;
            let map = document.getElementById("terriaViewer");
            map.style.maxHeight = "50%";
            this.setState({height: height, width: width, positionLeft: sidePanel.clientWidth, heightForAnswerContent: map.clientHeight});
        }catch(e){
            console.log(e.message);
        }
    }

   
    /**
     * メッセージ投稿
     * @param {*} addressList 宛先リスト
     */
    sendMessage(addressList){
        if(this.props.terria.authorityJudgment()){

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
        }else{
            this.postMessageApi(addressList);
        }
    }

    /**
     * メッセージ投稿(事業者用)
     */
    sendMessageForBusiness(){
        this.postMessageApi(null);
    }

    /**
     * メッセージ投稿APIを呼び出す
     * @param {*} addressList 宛先リスト
     */
    postMessageApi(addressList){
        let activeChatGroupId = this.state.activeChatGroupId;
        let activeMessageGroupId = this.state.activeMessageGroupId;
        let chatGroupInfo = this.state.chatGroupInfo;

        let index = chatGroupInfo.findIndex((item) => item.applicationStepId == activeChatGroupId);
        let index2 = chatGroupInfo[index]["chatList"].findIndex((item) => item.chatId == activeMessageGroupId);

        // 回答ID
        let answerId = chatGroupInfo[index]["chatList"][index2].answerId;
        if(!answerId)answerId = 0;
        // 担当課
        let departmentId = chatGroupInfo[index]["chatList"][index2].department.departmentId;
        let departmentName = chatGroupInfo[index]["chatList"][index2].department.departmentName;
        // 最大のメッセージIDを取得
        let messageIdList = chatGroupInfo[index]["chatList"][index2].messages.map(message => message.messageId);
        let messageId = Math.max(...messageIdList);

        let message = {};
        message.messageId= messageId + 1;
        message.messageText = this.state.messageText;
        message.readFlag = false;
        const pad2 = (n) =>  { return n < 10 ? '0' + n : n };
        const date = new Date();
        const sendDatetime = date.getFullYear().toString() + "/"
        + pad2(date.getMonth() + 1) + "/"
        + pad2(date.getDate()) + " "
        + pad2(date.getHours()) + ":"
        + pad2(date.getMinutes());
        message.sendDatetime = sendDatetime; 
        message.inquiryAddressForms=[];
        if(addressList == null){
            let inquiryAddressForm = {};
            inquiryAddressForm.messageId = messageId + 1;
            inquiryAddressForm.inquiryAddressId = 1;
            inquiryAddressForm.department = {};
            inquiryAddressForm.department.departmentId = departmentId;
            inquiryAddressForm.department.departmentName = departmentName;
            message.inquiryAddressForms.push(inquiryAddressForm);
            message.messageType = 1;
        }else{
            let inquiryAddressForm = {};
            Object.keys(addressList).map(key => {
                inquiryAddressForm.messageId = messageId + 1;
                inquiryAddressForm.inquiryAddressId = Number(key) + 1;
                inquiryAddressForm.department = {};
                inquiryAddressForm.department.departmentId = addressList[key].departmentId;
                inquiryAddressForm.department.departmentName = addressList[key].departmentName;
                message.inquiryAddressForms.push(inquiryAddressForm);
            });
            if(addressList[0].departmentId == -1){
                message.messageType = 2;
            }else{
                message.messageType = 3;
            }
            message.sender = {};
            message.sender.userId = this.state.loginUserId;
            message.sender.userName = "";
            message.sender.departmentId = departmentId;
            message.sender.departmentName = departmentName;
        }
        
        //表示用に更新
        chatGroupInfo[index]["chatList"][index2].messages.push(message);
        this.setState({chatGroupInfo: chatGroupInfo, messageText:""});

        //メッセージ投稿APIの呼び出し
        let postMessageApi = "/chat/business/message/post";
        let requestBody = {
            chatId:activeMessageGroupId,
            answerId:answerId,
            message:message,
            displayedMaxMessageId:message.messageId,
            toDepartments:addressList,
            applicationStepId:chatGroupInfo[index]["chatList"][index2].applicationStep.applicationStepId,
            departmentAnswerId:chatGroupInfo[index]["chatList"][index2].departmentAnswerId
        };
        if(this.props.terria.authorityJudgment()){
            postMessageApi = "/chat/government/message/post";
        }else{
            const answerContent = this.props.viewState.answerContent;
            requestBody["loginId"] = answerContent?.loginId;
            requestBody["password"] = answerContent?.password;
        }
        fetch(Config.config.apiUrl + postMessageApi, {
            method: 'POST',
            body: JSON.stringify(requestBody),
            headers: new Headers({ 'Content-type': 'application/json' }),
        })
        .then(res => res.json())
        .then(res => {
            if(res.status === 401){
                alert("認証情報が無効です。ページの再読み込みを行います。");
                window.location.reload();
                return null;
            }

            if (res.chatId || res.length > 0) {
                this.getChatRelatedInfo(true);
                // 登録されたメッセージをクリアする
                this.setState({messageText:""});
                // 事業者の場合ファイルアップロード呼び出し
                if(!this.props.terria.authorityJudgment()){
                    this.uploadFile(this.props.viewState.inquiryFiles,res.messages[res.messages.length-1].messageId);
                }
            }else{
                alert('メッセージ送信処理に失敗しました');
            }
        }).catch(error => {
            console.error('処理に失敗しました', error);
            alert('処理に失敗しました');
        });
        if(this.props.terria.authorityJudgment()){
            // 選択された宛先をクリアする
            this.props.viewState.removeAllInputChatAddress();
            let elem = document.getElementById('addressText');
            elem.innerHTML = "<span></span>";
        }
        this.changeTextAreaHight(true,false);
    }

    /**
     * 共通：
     * 遷移元画面へ戻る
     */
    back(){
        // チャット画面の定期リフレッシュ処理をクリアする
        clearInterval(this.state.intervalID);

        // 地図表示のリサイズ
        window.removeEventListener('resize', this.resizeEvent);
        let map = document.getElementById("terriaViewer");
        map.style.maxHeight = "100%";
        this.props.viewState.triggerResizeEvent();

        if(this.props.terria.authorityJudgment()){
            // 行政の場合
            let backToPage = this.props.viewState.backToPage;
            this.props.viewState.changeAdminTabActive(backToPage.tab);
            this.props.viewState.changeApplyPageActive(backToPage.page? backToPage.page:"applyList");

            // 申請中地番レイヤを表示
            this.props.viewState.showApplicationAreaLayer();
            // 申請情報表示地番をクリア
            this.props.viewState.showApplicationSearchTargetLayer(false, null);

        }else{
            // 回答確認画面へ戻る
            this.props.viewState.backFromChatView();
            // 選択された問合せ添付ファイルリストをクリアする
            this.props.viewState.changeInquiryFiles([]);
        }

    }

    /**
     * 共通：ファイルダウンロード
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
     * 共通：ファイルダウンロードモーダルを開く
     * @param {*} applicationFile 対象ファイル
     * @param {*} target ターゲット
     */
    openDownFileView(applicationFile,target){
        this.props.viewState.setFileDownloadTarget(applicationFile,target)
        this.props.viewState.changeFileDownloadModalShow();
    }

    /**
     * 共通：ファイルダウンロード
     * @param {*} path ファイルダウンロード用APIのパス
     * @param {*} file ダウンロード対象ファイル
     * @param {*} fileNameKey ファイル名に対するキー項目
     */
    outputFile(path, file, fileNameKey) {
        if(file){
            file.applicationId = this.state.applicationId;
            if(!this.props.terria.authorityJudgment()){
                file.loginId = this.props.viewState.answerContent.loginId;
                file.password = this.props.viewState.answerContent.password;
            }
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
     * 共通：
     * 拡張子取得
     * @param {String} fileName ファイル名
     */
    getExtension(fileName){
        // 拡張子チェック
        let extension = fileName.split('.').pop();
        return extension;
    }

    /**
     * 共通：
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
     * 共通：
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
     * 共通：
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
     * 共通：
     * ファイルプレビューモーダルを開く
     * @param {*} applicationFile 対象ファイル情報
     * @param {*} target ターゲット
     */
    openPreviewFileView(applicationFile,target){
        this.props.viewState.setFileDownloadTarget(applicationFile,target)
        this.props.viewState.changeFilePreViewModalShow();
    }

    /**
     * 共通：
     * ファイルプレビュー
     * @param {*} path ファイルダウンロード用APIのパス
     * @param {*} file ダウンロード対象ファイル
     * @param {*} fileNameKey ファイル名に対するキー項目
     */
    openFile(path, file, fileNameKey) {
        if(file){
            file.applicationId = this.state.applicationId;
            if(!this.props.terria.authorityJudgment()){
                file.loginId = this.props.viewState.answerContent.loginId;
                file.password = this.props.viewState.answerContent.password;
            }
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
     * 共通：
     * メッセージ又は添付ファイルが入力したか確認
     * @param {*} text 入力したメッセージ内容 
     * @param {*} inquiryFiles 添付ファイルリスト
     * @param {*} addressList 宛先リスト
     */
    isInputed(text, inquiryFiles, addressList){

        if(text){
            text = text.trim();
        }

        let activeMessageGroupId = this.state.activeMessageGroupId;
        if(!activeMessageGroupId){
            return false;
        }

        if(this.props.terria.authorityJudgment()){
            if(text && Object.keys(addressList).length > 0 ){
                return true;
            }else{
                return false;
            }
        }else{
            if(text || Object.keys(inquiryFiles).length > 0 ){
                return true;
            }else{
                return false;
            }
        }

    }

    /**
     * 共通：
     * 選択中タイトルであるか判断
     * @param {*} chatForm 
     * @returns 判断結果
     */
    isCheckedTile(chatForm){
        let activeMessageGroupId = this.state.activeMessageGroupId;
        return chatForm.chatId == activeMessageGroupId;
    }

    /**
     * 共通：
     * 申請種別を切り替えると、選択中の申請種別を変更し、
     * 選択中の子タイトルと関連情報部分の選択中一覧もクリアする
     * @param {*} activeChatGroupId 
     */
    changeActiveChatGroupId(applicationStepId){
        let activeChatGroupId = this.state.activeChatGroupId;
        const activeListId = this.state.activeListId;
        if(applicationStepId == 1 && activeChatGroupId != 1 && activeListId == 4){
            this.setState({activeListId:0});
        }
        if(applicationStepId != activeChatGroupId){
            // 申請種別切り替え時に、メッセージグループのキーと関連情報をクリアして、関連情報の表示制御変数を初期化する
            this.setState({
                activeChatGroupId: applicationStepId,
                activeMessageGroupId: "",
                answer: [],
                selectedAnswer:{},
                answerHistory: [],
                answerFile: [],
                applicationFile: []
            });
        }else{
            this.setState({
                activeChatGroupId: 0,
                activeMessageGroupId: 0,
                answer: [],
                selectedAnswer:{},
                answerHistory: [],
                answerFile: [],
                applicationFile: []
            });
        }
    }

    /**
     * 共通：
     * 申請種別したの子タイトルを切り替えると、選択中キー値を更新
     * @param {*} chatForm 
     */
    changeActiveMessageGroupId(chatForm){
        let activeChatGroupId = this.state.activeChatGroupId;
        let activeMessageGroupId = this.state.activeMessageGroupId;
        let newActiveMessageGroupId = 0;
        // チャットIDをメッセージグループのキーとする
        newActiveMessageGroupId = chatForm.chatId;
        if(activeMessageGroupId != newActiveMessageGroupId){
            // メッセージグループのキーが変わるときに、関連情報の表示制御変数を初期化する
            this.setState({activeChatGroupId: chatForm.applicationStep.applicationStepId,activeMessageGroupId: newActiveMessageGroupId},()=>{
                this.getChatRelatedInfo(true,()=>this.changeTextAreaHight(false,false));
            });
        }else{
            //許可判定タブが再度押下された場合は非活性に落とす
            let applicationStepId = chatForm.applicationStep.applicationStepId;
            if(applicationStepId == 3){
                applicationStepId = 0;
            }
            this.setState({
                activeChatGroupId: applicationStepId,
                activeMessageGroupId: 0,
                answer: [],
                selectedAnswer:{},
                answerHistory: [],
                answerFile: [],
                applicationFile: []
            });
        }

    }

    /**
     * 最後のメッセージグループのスクロール高さの計算
     * @param {*} chatFrom 
     * @returns 
     */
    calculationMessagelistHight(chatFrom){
        let chatGroupInfo = this.state.chatGroupInfo;
        let chatGroupSize =  Object.keys(chatGroupInfo).length;
        let lastChatGroupInfo = chatGroupInfo[chatGroupSize - 1];
        let lastChatGroupMessageGroupSize =  Object.keys(lastChatGroupInfo.chatList).length;
        let lastChatList = lastChatGroupInfo.chatList[lastChatGroupMessageGroupSize - 1];
        //　展開したいメッセージリストは最後である場合、
        if(lastChatGroupInfo.applicationStepId == chatFrom.applicationStepId && lastChatList.chatId == chatFrom.chatId){
            let chatBoxHeight = this.state.height;
            let height = chatBoxHeight - (chatGroupSize + lastChatGroupMessageGroupSize) * (45 + 4);
            if(height < 300){
                height = 300;
            }
           return height + "px";
        }else{
            return "300px";
        }
    }
    
    /**
     * 事業者専用：
     * 添付ファイルアップロード
     * @param {*} inquiryFiles 添付ファイル
     * @param {*} messageId メセッジID
     */
    uploadFile(inquiryFiles,messageId){
        Object.keys(inquiryFiles).map(key => {
            // パラメータ編集
            inquiryFiles[key]["applicationId"] = this.state.applicationId;
            if(!this.props.terria.authorityJudgment()){
                inquiryFiles[key]["loginId"] = this.props.viewState.answerContent.loginId;
                inquiryFiles[key]["password"] = this.props.viewState.answerContent.password;
            }
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
                    this.getChatRelatedInfo(true);
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
     * 事業者専用：
     * 添付ファイル選択モーダル画面を開く
     */
    openUploadFileModal(){
        this.props.viewState.changeInquiryFileUploadModalShow();
    }

    /**
     * 行政専用：
     * 宛先を選択した後で、callback関数
     * 選択された宛先内容より、高さ再計算
     */
    setAddressText(){
        const addressList = this.props.viewState.inputChatAddress;
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
     * 行政専用：
     * 申請情報詳細画面へ遷移
     * @param {*} active アクティブページ
     */
    showApplyDetail(active){
        // チャット画面の定期リフレッシュ処理をクリアする
        clearInterval(this.state.intervalID);
        let applicationId = this.state.applicationId;
        this.props.viewState.applicationInformationSearchForApplicationId = applicationId;
        this.props.viewState.changeApplyPageActive(active);
        this.props.viewState.setAdminBackPage("applySearch", "chat");
        window.removeEventListener('resize', this.resizeEvent);
        let map = document.getElementById("terriaViewer");
        map.style.maxHeight = "100%";
        this.props.viewState.triggerResizeEvent();
    }

    /**
     * 行政専用：
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
     * 行政専用：
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
     * 行政専用：
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
     * 行政専用：
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
            if(inquiryAddress && inquiryAddress[0]?.department?.departmentId == "-1"){
                displayText = "事業者";
            }
        }

        return displayText;
    }

    render() {
        const t = this.props.t;
        let height = this.state.height;
        let descriptionDivWidth = this.state.width;
        let positionLeft = this.state.positionLeft;
        let heightForAnswerContent = this.state.heightForAnswerContent;

        //添付ファイル
        let inquiryFiles = this.props.viewState.inquiryFiles;
        //申請種別より、チャットIDに対する回答一覧
        let answer = this.state.answer;
        //回答IDに紐づく回答対象
        let selectedAnswer = this.state.selectedAnswer;
        //回答IDに紐づく回答履歴一覧
        let answerHistorys = this.state.answerHistory;
        //回答IDに紐づく回答ファイル一覧
        let answerFiles = this.state.answerFile;
        //回答対象に紐づく申請ファイル一覧
        let applicationFiles = this.state.applicationFile;
        //回答登録内容を表示するかフラグ
        let answerContentDisplay = this.state.answerContentDisplay;
        //入力したメッセージ内容
        let messageText = this.state.messageText;
        //チャットグループ
        let chatGroupInfo = this.state.chatGroupInfo;
        //表示するチャットグループID 申請段階ID
        let activeChatGroupId = this.state.activeChatGroupId;
        //表示するチャットメッセージグループID チャットID
        let activeMessageGroupId = this.state.activeMessageGroupId;
        //表示する一覧ID
        let activeListId = this.state.activeListId;
        //宛先
        let addressList = this.props.viewState.inputChatAddress;
        // メッセージの最大文字数
        const maxLength = Config.inputMaxLength.chatMsg;
        const inputBuffLength = 1;
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
                            {this.props.terria.authorityJudgment() && this.props.viewState.backToPage?.page !== "applyDetail" && (
                                <button className={`${CustomStyle.btn_baise_style} ${CustomStyle.details_button}`}
                                    onClick={e => 
                                        this.showApplyDetail("applyDetail")
                                    }
                                >
                                    <span>申請詳細</span>
                                </button>
                            )}
                            <Spacing bottom={2} />
                            <div className={`${CustomStyle.scrollContainer} ${CustomStyle.chat_box_border}`} id="ChatBox" style={{height:height + "px"}}>
                                <table style={{width:"100%"}} >
                                    <tbody>
                                        {/* 申請種別ごとのタイトル */}
                                        {chatGroupInfo && Object.keys(chatGroupInfo).map(indexOfApplicationStepId => (
                                            <>
                                            <tr key={`title1-${chatGroupInfo[indexOfApplicationStepId].applicationStepId}`}>
                                                <td>
                                                    <button className={`${CustomStyle.chat_title_1} ${chatGroupInfo[indexOfApplicationStepId].applicationStepId == activeChatGroupId? CustomStyle.checked: CustomStyle.notChecked}`}
                                                        onClick={e => {
                                                            if(chatGroupInfo[indexOfApplicationStepId].applicationStepId !=3){
                                                                this.changeActiveChatGroupId(chatGroupInfo[indexOfApplicationStepId].applicationStepId);
                                                            }else{
                                                                this.changeActiveMessageGroupId(chatGroupInfo[indexOfApplicationStepId].chatList[0]);
                                                            }
                                                        }}
                                                    >
                                                        <span>{chatGroupInfo[indexOfApplicationStepId].applicationStepName}</span>
                                                        <StyledIcon styledWidth={"12px"} fillColor={"white"}
                                                            glyph={chatGroupInfo[indexOfApplicationStepId].applicationStepId == activeChatGroupId?Icon.GLYPHS.up:Icon.GLYPHS.down}
                                                        />
                                                    </button>
                                                </td>
                                            </tr>
                                            {/* 条項　又は、担当課　ごとのタイトル */}
                                            {(chatGroupInfo[indexOfApplicationStepId].applicationStepId == activeChatGroupId) && (
                                               <>
                                                    {chatGroupInfo[indexOfApplicationStepId].chatList && Object.keys(chatGroupInfo[indexOfApplicationStepId].chatList).map(indexOfChatId => (
                                                        <>
                                                        {(chatGroupInfo[indexOfApplicationStepId].applicationStepId != 3) && (
                                                            <tr key={`title2-${chatGroupInfo[indexOfApplicationStepId].chatList[indexOfChatId].chatId}`}>
                                                                <td>
                                                                    <button className={`${CustomStyle.chat_title_2} ${this.isCheckedTile(chatGroupInfo[indexOfApplicationStepId].chatList[indexOfChatId])? CustomStyle.checked: CustomStyle.notChecked}`}
                                                                        onClick={e => {
                                                                            this.changeActiveMessageGroupId(chatGroupInfo[indexOfApplicationStepId].chatList[indexOfChatId]);
                                                                        }}
                                                                    >
                                                                        <span>{activeChatGroupId!==2?chatGroupInfo[indexOfApplicationStepId].chatList[indexOfChatId].title:chatGroupInfo[indexOfApplicationStepId].chatList[indexOfChatId].department?.departmentName}</span>
                                                                        <StyledIcon styledWidth={"12px"} fillColor={"white"}
                                                                            glyph={this.isCheckedTile(chatGroupInfo[indexOfApplicationStepId].chatList[indexOfChatId])?Icon.GLYPHS.up:Icon.GLYPHS.down}
                                                                        />
                                                                    </button>
                                                                </td>
                                                            </tr>
                                                         )}
                                                         {(this.isCheckedTile(chatGroupInfo[indexOfApplicationStepId].chatList[indexOfChatId])) && (
                                                            <>
                                                            <tr key={`title2-${chatGroupInfo[indexOfApplicationStepId].chatList[indexOfChatId].chatId}`} id={`title2-${chatGroupInfo[indexOfApplicationStepId].chatList[indexOfChatId].chatId}`}>
                                                            <td>
                                                                <div className={`${CustomStyle.scrollContainer} ${CustomStyle.message_list_style}`} id="messageListBox" style={{height: this.calculationMessagelistHight(chatGroupInfo[indexOfApplicationStepId]["chatList"][indexOfChatId]) }}>
                                                                    <table style={{width:"100%"}} >
                                                                        <tbody>
                                                                            {/* メッセージ表示 */}
                                                                            {Object.keys(chatGroupInfo[indexOfApplicationStepId].chatList[indexOfChatId].messages).map(indexOfMessageId => (
                                                                                <tr key={`message-${indexOfApplicationStepId}-${indexOfChatId}-${indexOfMessageId}`}>
                                                                                    <td>
                                                                                        {/* 事業者側 */}
                                                                                        { !this.props.terria.authorityJudgment() && chatGroupInfo[indexOfApplicationStepId].chatList[indexOfChatId].messages[indexOfMessageId].messageType == 1 && (
                                                                                            <>
                                                                                                <p className={`${CustomStyle.message_tips} ${CustomStyle.text_right}`}>
                                                                                                    {chatGroupInfo[indexOfApplicationStepId].chatList[indexOfChatId].messages[indexOfMessageId].sendDatetime}
                                                                                                </p>
                                                                                                <div className={CustomStyle.message_box_self}>
                                                                                                    <p className={CustomStyle.preLine}>{chatGroupInfo[indexOfApplicationStepId].chatList[indexOfChatId].messages[indexOfMessageId].messageText}</p>
                                                                                                    <div className={CustomStyle.download_file_area}>
                                                                                                        {chatGroupInfo[indexOfApplicationStepId].chatList[indexOfChatId].messages[indexOfMessageId].inquiryFiles && Object.keys(chatGroupInfo[indexOfApplicationStepId].chatList[indexOfChatId].messages[indexOfMessageId].inquiryFiles).map(key => (
                                                                                                            <div style = {{width:"180px", marginRight:"10px"}}>
                                                                                                                <button className={CustomStyle.download_file_button}
                                                                                                                    title={chatGroupInfo[indexOfApplicationStepId].chatList[indexOfChatId].messages[indexOfMessageId].inquiryFiles[key].fileName}
                                                                                                                    onClick={e => this.outputFile("/chat/file/download", chatGroupInfo[indexOfApplicationStepId].chatList[indexOfChatId].messages[indexOfMessageId].inquiryFiles[key],"fileName")}
                                                                                                                >
                                                                                                                    <label className={CustomStyle.download_file_label}>{chatGroupInfo[indexOfApplicationStepId].chatList[indexOfChatId].messages[indexOfMessageId].inquiryFiles[key].fileName}</label>
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
                                                                                        { !this.props.terria.authorityJudgment() && chatGroupInfo[indexOfApplicationStepId].chatList[indexOfChatId].messages[indexOfMessageId].messageType != 1 && (
                                                                                            <>
                                                                                                <p className={`${CustomStyle.message_tips}`}>
                                                                                                    {chatGroupInfo[indexOfApplicationStepId].chatList[indexOfChatId].messages[indexOfMessageId].sender.departmentName}
                                                                                                    <span style={{marginRight:"15px"}}></span>
                                                                                                    {chatGroupInfo[indexOfApplicationStepId].chatList[indexOfChatId].messages[indexOfMessageId].sendDatetime}
                                                                                                </p>
                                                                                                <div className={CustomStyle.message_box}>
                                                                                                    <p className={CustomStyle.preLine}>{chatGroupInfo[indexOfApplicationStepId].chatList[indexOfChatId].messages[indexOfMessageId].messageText}</p>
                                                                                                </div>
                                                                                            </>
                                                                                        )}
                                                                                        {/* 行政：自分から送信場合 */}
                                                                                        { this.props.terria.authorityJudgment() && this.isSelf(chatGroupInfo[indexOfApplicationStepId].chatList[indexOfChatId].messages[indexOfMessageId]) &&(
                                                                                            <>
                                                                                                <div className={CustomStyle.self_mesage}>
                                                                                                    <div className={CustomStyle.mesage_address}>
                                                                                                        <p className={`${CustomStyle.message_tips}`}>
                                                                                                            {`FROM: ${this.getSenderDisplayText(chatGroupInfo[indexOfApplicationStepId].chatList[indexOfChatId].messages[indexOfMessageId])}`}
                                                                                                        </p>
                                                                                                        <p className={`${CustomStyle.message_tips}`}>
                                                                                                            {`TO: ${this.getAddressDisplayText(chatGroupInfo[indexOfApplicationStepId].chatList[indexOfChatId].messages[indexOfMessageId])}`}
                                                                                                        </p>
                                                                                                    </div>
                                                                                                    <p className={`${CustomStyle.message_tips} ${CustomStyle.text_right}`}>
                                                                                                        {chatGroupInfo[indexOfApplicationStepId].chatList[indexOfChatId].messages[indexOfMessageId].sendDatetime}
                                                                                                    </p>
                                                                                                    <div className={CustomStyle.self_mesage_content}>
                                                                                                        <p className={CustomStyle.preLine}>{chatGroupInfo[indexOfApplicationStepId].chatList[indexOfChatId].messages[indexOfMessageId].messageText}</p>
                                                                                                    </div>
                                                                                                </div>
                                                                                            </>
                                                                                        )}
                                                                                        {/* 行政：他のユーザーから送信場合 */}
                                                                                        { this.props.terria.authorityJudgment() && !this.isSelf(chatGroupInfo[indexOfApplicationStepId].chatList[indexOfChatId].messages[indexOfMessageId]) &&(
                                                                                            <>
                                                                                                <div className={CustomStyle.other_mesage}>
                                                                                                    <div className={CustomStyle.mesage_address}>
                                                                                                        <p className={`${CustomStyle.message_tips}`}>
                                                                                                            {`FROM: ${this.getSenderDisplayText(chatGroupInfo[indexOfApplicationStepId].chatList[indexOfChatId].messages[indexOfMessageId])}`}
                                                                                                        </p>
                                                                                                        <p className={`${CustomStyle.message_tips}`}>
                                                                                                            {`TO: ${this.getAddressDisplayText(chatGroupInfo[indexOfApplicationStepId].chatList[indexOfChatId].messages[indexOfMessageId])}`}
                                                                                                        </p>
                                                                                                    </div>
                                                                                                    <p className={`${CustomStyle.message_tips}`}>
                                                                                                        {chatGroupInfo[indexOfApplicationStepId].chatList[indexOfChatId].messages[indexOfMessageId].sendDatetime}
                                                                                                    </p>
                                                                                                    <div className={CustomStyle.other_mesage_content}>
                                                                                                        <p className={CustomStyle.preLine}>{chatGroupInfo[indexOfApplicationStepId].chatList[indexOfChatId].messages[indexOfMessageId].messageText}</p>
                                                                                                        <div className={CustomStyle.download_file_area} >
                                                                                                            {Object.keys(chatGroupInfo[indexOfApplicationStepId].chatList[indexOfChatId].messages[indexOfMessageId].inquiryFiles).length > 0 && (
                                                                                                                <div className={CustomStyle.download_file_area}>
                                                                                                                    { Object.keys(chatGroupInfo[indexOfApplicationStepId].chatList[indexOfChatId].messages[indexOfMessageId].inquiryFiles).map(key => (
                                                                                                                        <div style={{width:"180px",marginRight:"10px"}}>
                                                                                                                            <button className={CustomStyle.download_file_button}
                                                                                                                                title={chatGroupInfo[indexOfApplicationStepId].chatList[indexOfChatId].messages[indexOfMessageId].inquiryFiles[key].fileName}
                                                                                                                                onClick={e => this.outputFile("/chat/file/download", chatGroupInfo[indexOfApplicationStepId].chatList[indexOfChatId].messages[indexOfMessageId].inquiryFiles[key],"fileName")}
                                                                                                                            >
                                                                                                                                <label className={CustomStyle.download_file_label}>{chatGroupInfo[indexOfApplicationStepId].chatList[indexOfChatId].messages[indexOfMessageId].inquiryFiles[key].fileName}</label>
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
                                                                                            </>
                                                                                        )}
                                                                                    </td>
                                                                                </tr>
                                                                            ))}
                                                                            <tr>
                                                                                <td>
                                                                                    <Spacing bottom={6} />
                                                                                </td>
                                                                            </tr>
                                                                        </tbody>
                                                                    </table>
                                                                </div>
                                                            </td>
                                                            </tr>
                                                            </>
                                                        )}
                                                        </>
                                                    ))}
                                                </>
                                            )}
                                          </>
                                        ))}
                                    </tbody>
                                </table>
                            </div>
                            <div className={`${CustomStyle.input_message_area} ${CustomStyle.chat_box_border}`} id="inputMessageBox">
                                { this.props.terria.authorityJudgment() && (
                                    
                                    <table style={{width:"100%",height: "100%"}} >
                                        <tbody>
                                            <tr key={`input-address-line`}>
                                                <td style={{width:"150px",verticalAlign:"middle"}}>
                                                    <button className={`${CustomStyle.address_btn}`} onClick={e => this.props.viewState.changeInputChatAddressModalShow()}>
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
                                                            maxLength={maxLength + inputBuffLength }
                                                            value={messageText}
                                                            placeholder ="メッセージを入力してください"
                                                            onChange={e => {

                                                                if(e.target.value.length > maxLength){
                                                                    alert(maxLength + "文字以内で入力してください。");
                                                                    return;
                                                                }
                                                                this.setState({ messageText: e.target.value});
                                                            }}
                                                        />
                                                    </div>
                                                </td>
                                                <td style={{width:"60px",textAlign:"center"}}>
                                                    <button id="sendMessage" className={CustomStyle.send_btn}
                                                        disabled={!this.isInputed(this.state.messageText, null, addressList)}
                                                        title="送信" onClick={evt => { this.sendMessage(addressList) } }> 
                                                            <StyledIcon glyph={Icon.GLYPHS.send}
                                                                styledWidth={"15px"}
                                                                styledHeight={"15px"}
                                                                light
                                                                className={`${!this.isInputed(this.state.messageText, null, addressList)? CustomStyle.send_btn_icon_disabled : CustomStyle.send_btn_icon}`}
                                                        />
                                                    </button>
                                                </td>
                                            </tr>
                                        </tbody>
                                    </table>
                                )}
                                { !this.props.terria.authorityJudgment() && (
                                    <>
                                        <table style={{width:"100%",height: "100%"}} >
                                            <tbody>
                                                <tr>
                                                    <td style={{textAlign:"center"}}>
                                                        <div style={{marginLeft:"10px"}}>
                                                            <textarea  
                                                                id = "messageText"
                                                                type="text" className={CustomStyle.input_message_box} 
                                                                maxLength={maxLength + inputBuffLength }
                                                                value={messageText}
                                                                placeholder ="メッセージを入力してください"
                                                                onChange={e => {

                                                                    if(e.target.value.length > maxLength){
                                                                        alert(maxLength + "文字以内で入力してください。");
                                                                        return;
                                                                    }
                                                                    this.setState({ messageText: e.target.value});
                                                                }}
                                                            />
                                                        </div>
                                                    </td>
                                                    <td style={{width:"60px",textAlign:"center"}}>
                                                        <button id="sendMessage" className={CustomStyle.send_btn}
                                                            disabled={!this.isInputed(this.state.messageText, inquiryFiles, null)}
                                                            
                                                            title="送信" onClick={evt => { this.sendMessageForBusiness() } }> 
                                                                <StyledIcon glyph={Icon.GLYPHS.send}
                                                                    styledWidth={"15px"}
                                                                    styledHeight={"15px"}
                                                                    light
                                                                    className={`${!this.isInputed(this.state.messageText, inquiryFiles, null)? CustomStyle.send_btn_icon_disabled : CustomStyle.send_btn_icon}`}
                                                                />
                                                        </button>
                                                    </td>
                                                    <td style={{width:"40px",textAlign:"center"}}>
                                                        <button id="uploadFile" className={CustomStyle.upload_btn}
                                                            disabled={ activeMessageGroupId? false : true}
                                                            title="添付ファイル" onClick={evt => { this.openUploadFileModal() } }> 
                                                                <StyledIcon glyph={Icon.GLYPHS.fileUpload}
                                                                    styledWidth={"30px"}
                                                                    styledHeight={"30px"}
                                                                    light
                                                                    className={ activeMessageGroupId ? CustomStyle.upload_file_icon : CustomStyle.upload_file_icon_disabled }
                                                                />
                                                                {Object.keys(inquiryFiles).length > 0 &&( <span className={CustomStyle.badge}>{Object.keys(inquiryFiles).length}</span>)}
                                                        </button>
                                                    </td>
                                                </tr>
                                            </tbody>
                                        </table>
                                    </>
                                )}
                            </div>
                        </Box >
                    </Box>
                </div>
                <Box padded />
                <div className={CustomStyle.answerContentDiv} style={{width: descriptionDivWidth + "px", left: positionLeft + "px", height:heightForAnswerContent + "px"}}>
                    {activeMessageGroupId ? (
                    <>
                    <div className={CustomStyle.list_item1} style={{ maxHeight: heightForAnswerContent - 10 + "px", overflowY: "auto"}}>
                        {selectedAnswer && activeChatGroupId == 1 && (
                            <>
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
                                    <>
                                        <p>対象：{ selectedAnswer?.judgementInformation?.title}</p>
                                        <p>判定結果：{selectedAnswer?.judgementResult}</p>
                                    </>
                                    
                                </div>
                            </>
                        )}

                        {selectedAnswer && activeChatGroupId != 1 && (
                            <button className={`${CustomStyle.custom_selection} ${activeListId == 4? CustomStyle.checked: CustomStyle.notChecked}`}
                                onClick={e => { this.setState({activeListId:4})}}
                            >
                                <span>回答登録内容一覧</span>
                                <StyledIcon style={{margin:"auto 0"}} styledWidth={"12px"} fillColor={"white"} glyph={Icon.GLYPHS.right} />
                            </button>
                        )}
                        
                        <Spacing bottom={1} />
                        {answerHistorys && (
                            <button className={`${CustomStyle.custom_selection} ${activeListId == 1? CustomStyle.checked: CustomStyle.notChecked}`}
                                onClick={e => { this.setState({activeListId:1})}}
                            >
                                <span>回答履歴一覧</span>
                                <StyledIcon style={{margin:"auto 0"}} styledWidth={"12px"} fillColor={"white"} glyph={Icon.GLYPHS.right} />
                            </button>
                        )}
                        <Spacing bottom={1} />
                        {answerFiles && (
                            <button className={`${CustomStyle.custom_selection} ${activeListId == 2? CustomStyle.checked: CustomStyle.notChecked}`}
                                onClick={e => { this.setState({activeListId:2})}}
                            >
                                <span>回答ファイル一覧</span>
                                <StyledIcon styledWidth={"12px"} fillColor={"white"} glyph={Icon.GLYPHS.right}/>
                            </button>
                        )}
                        <Spacing bottom={1} />
                        {applicationFiles && (
                            <button className={`${CustomStyle.custom_selection} ${activeListId == 3? CustomStyle.checked: CustomStyle.notChecked}`}
                                onClick={e => { this.setState({activeListId:3})}}
                            >
                                <span>申請ファイル一覧</span>
                                <StyledIcon styledWidth={"12px"} fillColor={"white"} glyph={Icon.GLYPHS.right}/>
                            </button>
                        )}
                        <Spacing bottom={1} />
                    </div>
                    <div className={CustomStyle.list_item2}>
                        { answerHistorys && activeListId == 1 && ( 
                            <nav className={CustomStyle.custom_nuv}>回答履歴一覧</nav> 
                        )}
                        { answerFiles && activeListId == 2 && ( 
                            <nav className={CustomStyle.custom_nuv}>回答ファイル一覧</nav>  
                        )}
                        { applicationFiles && activeListId == 3 && ( 
                            <nav className={CustomStyle.custom_nuv}>申請ファイル一覧</nav> 
                        )}
                        { selectedAnswer && activeListId == 4 && ( 
                            <nav className={CustomStyle.custom_nuv}>回答登録内容一覧</nav> 
                        )}
                        <Spacing bottom={2} />
                        <div style={{ maxHeight: heightForAnswerContent- 70 + "px", overflowY: "auto"}}>
                            { answerHistorys && activeListId == 1 && ( 
                                <table className={CustomStyle.selection_table}>
                                    <thead>
                                        {activeChatGroupId != 2 && (
                                            <tr className={CustomStyle.table_header}>
                                                <th style={{ width: "200px"}}>対象</th>
                                                <th style={{ width: "120px"}}>回答日時</th>
                                                <th style={{ width: "150px"}}>回答者</th>
                                                <th style={{ width: "250px"}}>回答内容</th>
                                                <th style={{ width: "100px"}}>回答通知</th>
                                            </tr>
                                        )}
                                        {activeChatGroupId == 2 && (
                                            <tr className={CustomStyle.table_header}>
                                                <th style={{ width: "200px"}}>対象</th>
                                                <th style={{ width: "120px"}}>回答日時</th>
                                                <th style={{ width: "150px"}}>回答者</th>
                                                <th style={{ width: "250px"}}>回答内容</th>
                                                <th style={{ width: "150px"}} colSpan="2">行政確定登録</th>
                                                <th style={{ width: "100px"}}>回答通知</th>
                                            </tr>
                                        )}
                                    </thead>
                                    <tbody>
                                        {answerHistorys && Object.keys(answerHistorys).map(index => (
                                            <tr key={`answerHistorys-${index}`} className={answerHistorys[index].answerDataType == "7" ? CustomStyle.deleted_line :""} >
                                                <td>{answerHistorys[index].title}</td>
                                                <td>{answerHistorys[index].updateDatetime}</td>
                                                <td>
                                                    {answerHistorys[index].answererUser && (
                                                        answerHistorys[index].answererUser?.departmentName + 
                                                        "　" + 
                                                        answerHistorys[index].answererUser?.userName
                                                        )
                                                    }
                                                </td>
                                                <td>{answerHistorys[index].answerContent}</td>
                                                {activeChatGroupId == 2 && (
                                                    <td>
                                                        <div className={CustomStyle.flex_center} style={{justifyContent:"space-between"}}>
                                                            <div className={CustomStyle.flex_center} >
                                                                {answerHistorys[index].governmentConfirmStatus == "0" && (
                                                                    <span>{`合意`}</span>
                                                                )}
                                                                {answerHistorys[index].governmentConfirmStatus == "1" && (
                                                                    <span>{`取下`}</span>
                                                                )}
                                                                {answerHistorys[index].governmentConfirmStatus == "2" && (
                                                                    <span>{`却下`}</span>
                                                                )}
                                                                <span style={{marginRight: "10px"}}></span>
                                                                <span className={CustomStyle.info_icon} style={{display:answerHistorys[index]["governmentConfirmComment"]? "":"none"}}>
                                                                    <StyledIcon 
                                                                        glyph={Icon.GLYPHS.info}
                                                                        styledWidth={"20px"}
                                                                        styledHeight={"20px"}
                                                                        light
                                                                    />
                                                                    <span className={CustomStyle.info_comment}>{answerHistorys[index]["governmentConfirmComment"]?answerHistorys[index]["governmentConfirmComment"]:""}</span>
                                                                </span>
                                                            </div>
                                                            <div></div>
                                                        </div>
                                                    </td>
                                                )}
                                                {activeChatGroupId == 2 && (
                                                    <td>{answerHistorys[index].governmentConfirmDatetime}</td>
                                                )}
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
                            { answerFiles && activeListId == 2 && ( 
                                <table className={CustomStyle.selection_table}>
                                    <thead>
                                        <tr className={CustomStyle.table_header}>
                                            <th className="no-sort" style={{ width: "22%"}}>回答ファイル</th>
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
                                                <td>{answerFiles[index].judgementInformation?answerFiles[index].judgementInformation.title:""}</td>
                                                <td>{ this.getExtension(answerFiles[index].answerFileName) }</td>
                                                <td>{answerFiles[index].answerFileName}</td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            )}
                            { applicationFiles && activeListId == 3 && ( 
                                <table className={CustomStyle.selection_table}>
                                    <thead>
                                        <tr className={CustomStyle.table_header}>
                                            <th className="no-sort" style={{ width: "22%"}}>申請ファイル</th>
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
                            { selectedAnswer && activeListId == 4 && ( 
                                <table className={CustomStyle.selection_table}>
                                    <thead>
                                        <tr className={CustomStyle.table_header}>
                                            <th style={{width:"150px"}}>対象</th>
                                            <th style={{width:"150px"}}>判定結果</th>
                                            {selectedAnswer.discussionItems && Object.keys(selectedAnswer.discussionItems).map(i => (
                                                <th style={{ width:"100px"}}>{selectedAnswer.discussionItems[i].displayName}</th>
                                            ))}
                                            <th style={{ width:"200px"}}>回答内容</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {answer && Object.keys(answer).map(index => (
                                            <tr key={`answerContentList-${index}`}>
                                                <td>{answer[index].judgementInformation.title}</td>
                                                <td>{answer[index].judgementResult}</td>
                                                {selectedAnswer.discussionItems && Object.keys(selectedAnswer.discussionItems).map(i => (
                                                    <td style={{minWidth:"20px"}}>{
                                                        selectedAnswer.discussionItem?selectedAnswer.discussionItem.split(",").findIndex(item=>item == selectedAnswer.discussionItems[i].ledgerId)>-1?selectedAnswer.discussionItems[i].displayName:"":""
                                                    }</td>
                                                ))}
                                                <td>{answer[index].answerContent}</td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            )}
                        </div>
                    </div>
                    </>
                    ):null}
                </div>
                { this.props.viewState.fileDownloadModalShow && (
                    <FileDownLoadModal terria={this.props.terria} viewState={this.props.viewState} t={this.props.t} />
                )}
                { this.props.viewState.inquiryFileUploadModalShow && (
                    <InquiryFileUploadModal terria={this.props.terria} viewState={this.props.viewState} t={this.props.t} />
                )}
                { this.props.viewState.filePreViewModalShow && (
                    <FilePreViewModal terria={this.props.terria} viewState={this.props.viewState} t={this.props.t} callback={this.openFile} />
                )}
                { this.props.viewState.inputChatAddressModalShow && (
                    <InputChatAddress terria={this.props.terria} viewState={this.props.viewState} t={t}  callback={this.setAddressText} selfDepartmentId={this.state.loginUserDepartmentId}/>
                )}
            </>
        );
    }
}
export default withTranslation()(withTheme(InputChatMessage));