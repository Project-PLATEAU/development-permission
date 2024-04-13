import { observer } from "mobx-react";
import PropTypes from "prop-types";
import React from "react";
import { withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import Spacing from "../../../../Styled/Spacing";
import Box from "../../../../Styled/Box";
import CustomStyle from "./scss/answer-content-list.scss";
import Icon, { StyledIcon } from "../../../../Styled/Icon";
import AnswerFileList from "./AnswerFileList.jsx";

/**
 * 回答内容確認画面の回答内容一覧コンポーネント
 */
@observer
class AnswerContentList extends React.Component {
    static displayName = "AnswerContentList";
    static propTypes = {
        terria: PropTypes.object.isRequired,
        viewState: PropTypes.object.isRequired,
        theme: PropTypes.object,
        t: PropTypes.func.isRequired,
        answerContentList: PropTypes.object
    }
    constructor(props) {
        super(props);
        this.state = {
            viewState: props.viewState,
            terria: props.terria,
            height: 0,
            //表示用回答内容リスト
            answers: props.answerContentList,
            //クリックされた回答内容レコードの回答ID
            activeKey: props.viewState.currentAnswerId
        };
    }

    /**
     * 初期処理
     */
    componentDidMount() {

        this.getWindowSize(); 
    }

    /**
     * リサイズするため、高さ再計算
     */
    getWindowSize() {
        let win = window;
        let e = window.document.documentElement;
        let g = window.document.documentElement.getElementsByTagName('body')[0];
        let h = win.innerHeight|| e.clientHeight|| g.clientHeight;

        const getRect = document.getElementById("AnswerContentListTable");
        let height = h - getRect.getBoundingClientRect().top;

        if(this.props.viewState.showChatView){
            let map = window.document.documentElement.getElementsByTagName('canvas')[0];
            height = map.clientHeight/2 - 80;
        }else if(this.props.viewState.showConfirmAnswerInformationView){
            height = (height-170)/2;
        }

        this.setState({height: height});
    }

    /**
     * 行政から未読問い合わせがあるか判定
     * @param {*} answer 回答情報
     * @returns 
     */
    getButtonType(answer){

        let buttonType = 1;
        if(answer["chat"] == null 
            || answer["chat"]["messages"] == null 
            || answer["chat"]["messages"].length === 0){
            // 問い合わせ情報がない場合、チャットボタンを「吹き出し」アイコンで表示
            buttonType = 1;
        }else{
            let size = answer["chat"]["messages"].length;
            let lastMessage = answer["chat"]["messages"][size - 1];
            if(lastMessage.messageType == 2 && lastMessage.readFlag == 0  ){
                // 最新な問い合わせ情報のメッセージタイプが「２：行政→事業者」であれば、かつ、該当メッセージが未読の場合、
                // チャットボタンを「吹き出し」アイコンで表示し、！マークをつける
                buttonType = 2;
            }else{
                // 上記以外は　チャットボタンを「吹き出し」アイコンで表示
                buttonType = 3;
            }
        }
        return buttonType;
    }

     /**
      * 問合せ画面へ遷移
      * @param {*} e イベント
      * @param {*} answer 回答情報
      */
     moveToChatView(e, answer){
        if(this.props.intervalID){
            clearInterval(this.props.intervalID);
        }

        this.props.viewState.moveToChatView(answer.answerId); 
    }

    /**
     * 回答内容一覧の行クリックイベント
     * @param {*} event イベント
     * @param {*} answerId 回答ID
     */
    clickAnswer(event, answerId){
        let isChatView = this.props.viewState.showChatView;
        if(!isChatView){
            this.props.viewState.setCurrentAnswerId(answerId);
            this.setState({activeKey:answerId});
        }
    }

    render() {
        let answers = this.props.answerContentList;
        let height = this.state.height;
        let isChatView = this.props.viewState.showChatView;
        let activeKey = this.state.activeKey;
        let selectedAnswers =answers.filter((value,index) => index == 0);
        if(activeKey){
            selectedAnswers = answers.filter((value) => value["answerId"] == activeKey);
        }else{
            activeKey = selectedAnswers[0]["answerId"];
        }
        
        return (
            <>
                <Box
                    centered
                    displayInlineBlock
                    className={CustomStyle.custom_content}
                >
                    <h2 className={isChatView?CustomStyle.title_in_chat_view:CustomStyle.title}>回答内容一覧</h2>
                    <Spacing bottom={1} />
                    <div className={CustomStyle.scrollContainer} id="AnswerContentListTable" style={{height: height + "px"}} >
                        <table className={CustomStyle.selection_table}>
                            <thead>
                                <tr className={CustomStyle.table_header}>
                                    <th style={{ width: isChatView? 200 + "px" : 100 +"px" }}>対象</th>
                                    <th style={{ minWidth:"100px"}}>判定結果</th>
                                    <th>回答内容</th>
                                    {!isChatView && (<th>回答添付</th>)}
                                    {!isChatView && (<th>問い合わせ</th>)}
                                </tr>
                            </thead>
                            <tbody>
                                {answers && Object.keys(answers).map(key => (
                                    <tr id={"tr" + key } onClick={e => {this.clickAnswer(e, answers[key]["answerId"])}} 
                                        className={answers[key]["answerId"] == activeKey && !isChatView? CustomStyle.isActive : ""}>
                                        <td>
                                            {answers[key]["judgementInformation"]["title"]}
                                        </td>
                                        <td>
                                            {answers[key]["judgementResult"]}
                                        </td>
                                        <td style={{ width: 150 + "px", overflow: "hidden", wordWrap: "break-word", whiteSpace: "pre-line" }}>
                                            {answers[key]["answerContent"]}
                                        </td>
                                        {!isChatView && (
                                            <td style={{ width: 60 + "px"}}>
                                                {answers[key]["answerFiles"].length > 0 && (
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
                                        )}
                                        {!isChatView && (
                                            <td style={{ width: 100 + "px"}}>
                                                <div className={CustomStyle.table_button_box}>
                                                    <div className={CustomStyle.item}>
                                                        {this.getButtonType(answers[key]) !== 2 && (
                                                            <button className={`${CustomStyle.chat_button}`}
                                                                onClick={e => {
                                                                    this.moveToChatView(e, answers[key])
                                                                }}
                                                            >
                                                                <StyledIcon 
                                                                    glyph={Icon.GLYPHS.chat}
                                                                    styledWidth={"30px"}
                                                                    styledHeight={"30px"}
                                                                    light
                                                                />
                                                            </button>
                                                        )}
                                                        {this.getButtonType(answers[key]) === 2 && (
                                                            <button className={`${CustomStyle.chat_button}`}
                                                                onClick={e => {
                                                                    this.moveToChatView(e, answers[key])
                                                                }}
                                                            >
                                                                <StyledIcon 
                                                                    glyph={Icon.GLYPHS.chat}
                                                                    styledWidth={"30px"}
                                                                    styledHeight={"30px"}
                                                                    light
                                                                />
                                                                <span className={CustomStyle.badge}>!</span>

                                                            </button>
                                                        )}
                                                    </div>
                                                </div>
                                            </td>
                                        )}
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </Box>
                {this.props.viewState.showConfirmAnswerInformationView && (
                    <>
                        <Box paddedRatio={3}/>
                        <AnswerFileList 
                            viewState={this.props.viewState} 
                            terria={this.props.terria} 
                            answerContentList ={selectedAnswers}
                        />
                    </>
                ) }
                
            </>
        );
    }
}
export default withTranslation()(withTheme(AnswerContentList));