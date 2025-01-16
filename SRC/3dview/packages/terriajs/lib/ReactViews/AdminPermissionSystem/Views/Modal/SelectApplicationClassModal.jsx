import React from "react";
import PropTypes from "prop-types";
import { observer } from "mobx-react";
import { withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import Box from "../../../../Styled/Box";
import Styles from "./scss/SelectApplicationClassModal.scss"
import { RawButton } from "../../../../Styled/Button";
import Icon, { StyledIcon } from "../../../../Styled/Icon";
import Config from "../../../../../customconfig.json";

/**
 * 行政用コンポーネント：回答申請段階選択画面
 */

@observer
class SelectApplicationClassModal extends React.Component {
    static displayName = "SelectApplicationStepModal"
    static propsType = {
        terria: PropTypes.object.isRequired,
        viewState: PropTypes.object.isRequired
    }

    constructor(props) {
        super(props);
        this.state = {
            viewState: props.viewState,
            terria: props.terria,
            //選択された申請段階
            checkedApplicationStepId: props.viewState.checkedApplicationStepId,
            //前画面に選択された申請段階を保持
            initApplicationStepId: props.viewState.checkedApplicationStepId,
            // 該当申請に対する回答内容閲覧可能の申請段階リスト
            applicationStepList:[]
        }
    }

    /**
     * 初期処理
     */
    componentDidMount() {
        this.draggable(document.getElementById('selectApplicationStepModalDrag'), document.getElementById('selectApplicationStepModal'));
        this.getApplicationStepList();
    }

    /**
     * 回答可能な申請段階リスト取得
     */
    getApplicationStepList(){
        const applicationId = this.props.viewState.applicationInformationSearchForApplicationId;
        fetch(Config.config.apiUrl + "/application/applicationStep/" + applicationId + "/false")
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
            if (Object.keys(res).length > 0) {
                // 前画面に、選択中タブに対する申請種別がデフォルト選択されるにする
                let checkedApplicationStepId = this.state.checkedApplicationStepId;
                Object.keys(res).map( index =>{
                    
                    if(res[index].applicationStepId == checkedApplicationStepId ){
                        res[index].checked = true;
                    }
                });

                res.sort((a, b) => a.applicationStepId - b.applicationStepId);

                this.setState({applicationStepList: res});
            } else {
                alert("申請段階リスト取得に失敗しました。再度操作をやり直してください。");
            }
        }).catch(error => {
            console.error('通信処理に失敗しました', error);
            alert('通信処理に失敗しました');
        });
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
     * 申請段階の選択結果を切り替え
     * @param {*} event 
     */
    onChange(event){
        let value = event.target.value;
        let list = this.state.applicationStepList;

        Object.keys(list).map(key => {

            if(list[key].applicationStepId == value){
                list[key].checked = true;
            }else{
                list[key].checked = false;
            }

        });

        // 申請段階の選択状態保存
        this.setState({applicationStepList:list});
    }

    /**
     * 回答登録画面へ遷移
     */
    moveToAnswerInput(){

        let list = this.state.applicationStepList;

        let checkedItemIndex = list.findIndex( (item) => item.checked == true );

        if(checkedItemIndex < 0 ){

            alert("申請段階を選択してください。");
        }else{      
            this.props.viewState.nextAnswerInputView(list[checkedItemIndex].applicationStepId);
        }
    }
    

    render(){
        let applicationStepList = this.state.applicationStepList;
        return (
            <div className={Styles.overlay}>
                <div className={Styles.modal}  id="selectApplicationStepModal">
                    <Box position="absolute" paddedRatio={3} topRight>
                        <RawButton onClick={() => {
                            this.props.viewState.closeSelectApplicationClassModal(this.state.initApplicationStepId);
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
                    <nav className={Styles.custom_nuv} id="selectApplicationStepModalDrag">
                        申請種別選択
                    </nav>
                    <div className={Styles.container}>
                        <p>回答する申請種別を選択してください。</p>
                        <div className={Styles.radioDiv}>
                            {Object.keys(applicationStepList).map(key => (
                                <div>
                                    <label className={Styles.radio_label}>
                                        <input
                                            className={Styles.radio_input}
                                            type="radio"
                                            value={applicationStepList[key].applicationStepId}
                                            onChange={e => this.onChange(e)}
                                            checked={applicationStepList[key].checked}
                                        />
                                        <span className={Styles.custom_radio} />
                                        {"：" + applicationStepList[key].applicationStepName}
                                    </label>
                                </div>
                            ))}
                        </div>
                    </div>
                    <div className={Styles.button_div}>
                        <button
                            className={Styles.btn_baise_style}
                            style={{width:"30%",height:"40px"}}
                            onClick={e => {this.moveToAnswerInput()}}
                        >
                            <span>回答登録</span>
                        </button>
                    </div>
                </div>
            </div>
        )
    }
}
export default withTranslation()(withTheme(SelectApplicationClassModal));