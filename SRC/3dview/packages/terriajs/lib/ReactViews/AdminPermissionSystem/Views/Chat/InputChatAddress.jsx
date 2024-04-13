import React from "react";
import PropTypes from "prop-types";
import { observer } from "mobx-react";
import { withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import Box from "../../../../Styled/Box";
import Styles from "./scss/InputChatAddress.scss"
import { RawButton } from "../../../../Styled/Button";
import Icon, { StyledIcon } from "../../../../Styled/Icon";
import Config from "../../../../../customconfig.json";

/**
 * 行政:宛先選択ダイアログ画面
 */

@observer
class InputChatAddress extends React.Component {
    static displayName = "InputChatAddress"
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
            // 部署一覧
            department: [],
            // 選択された部署が事業者であるかフラグ
            isBusiness: true,
            selectedDepartment:props.viewState.inputChatAddress,
            // コールバック関数
            callback: this.props.callback,
            // ログインユーザー部署ID
            selfDepartmentId: props.selfDepartmentId
        }
        this.CallBackFunction = this.CallBackFunction.bind(this);
    }

    /**
     * 初期表示
     */
    componentDidMount() {
        // 部署一覧取得
        this.getDepartmentList();
        // 選択された部署が事業者であるかフラグ設定
        if(this.props.viewState.inputChatAddress.length > 0){
            const index = this.props.viewState.inputChatAddress.findIndex(obj => 
                obj.departmentId == "-1"
            );
            this.setState({isBusiness: (index != -1)? true : false});
        }else{
            // 事業者をデフォルトに選択する
            this.changeRadio(true);
        }

        // コンポーネントドラッグ操作
        this.draggable(document.getElementById('inputChatAddressModalDrag'), document.getElementById('fileDownLoadModal'));
    }

    /**
     * コールバック関数
     */
    CallBackFunction() {
        this.state.callback();
    }

    /**
     * 部署一覧取得
     */
    getDepartmentList(){
        fetch(Config.config.apiUrl + "/application/search/conditions")
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
                this.setState({department: res.department});
            } else {
                alert("部署一覧取得に失敗しました。再度操作をやり直してください。");
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
     * モーダルを閉じる
     * 
     */
    close(){
        this.CallBackFunction();
        this.state.viewState.changeInputChatAddressModalShow();
    }

    /**
     * 宛先のラジオ切り替え
     * @param {*} isBusiness 事業者かどうか
     */
    changeRadio(isBusiness){
        this.setState({isBusiness: isBusiness});
        this.props.viewState.removeAllInputChatAddress();
        if(isBusiness){
            this.selectAddress({"departmentId": "-1","departmentName":"事業者"})
        }

        this.setState({selectedDepartment:this.props.viewState.inputChatAddress});
    }

    /**
     * 選択した宛先保存
     * @param {*} address 宛先 
     */
    selectAddress(address){
        this.props.viewState.setInputChatAddress(address);
        this.setState({selectedDepartment:this.props.viewState.inputChatAddress});
    }

    /**
     * 部署の選択状態
     * @param {*} departmentObj 対象部署 
     * @returns 選択状態(選択した場合、trueで返す、以外はfalseを返す)　
     */
    isCheckSelected(departmentObj){
        const index = this.props.viewState.inputChatAddress.findIndex(obj => 
            obj.departmentId == departmentObj.departmentId
        );

        return (index != -1)? true : false;
    }

    render(){
        let department = this.state.department;
        let isBusiness = this.state.isBusiness;
        let selectedDepartment = this.state.selectedDepartment;
        let selfDepartmentId = this.state.selfDepartmentId;
        return (
            <div className={Styles.overlay}>
                <div className={Styles.modal}  id="inputChatAddressModal">
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
                    <nav className={Styles.custom_nuv} id="inputChatAddressModalDrag">
                        宛先選択
                    </nav>
                    <div className={Styles.container}>
                        <p>
                            メッセージの送信先を選択してください。<br />
                            事業者を選択すると通知されます。<br />
                            他部署宛のメッセージは事業者に通知されません。<br />
                        </p>
                        <Box paddedRatio={2}/>
                        <div className={Styles.radio_div_area}>
                            <div css={`width:50%`}>
                                <label className={Styles.radio_label}>
                                    <input
                                        className={Styles.radio_input}
                                        type="radio"
                                        value="0"
                                        onChange={e => this.changeRadio(true)}
                                        checked={isBusiness}
                                    />
                                    <span className={Styles.custom_radio}/>
                                    {`：事業者`}
                                </label>
                            </div>
                            <div css={`width:50%`}>
                                <label className={Styles.radio_label}>
                                    <input
                                        className={Styles.radio_input}
                                        type="radio"
                                        value="1"
                                        onChange={e => this.changeRadio(false)}
                                        checked={!isBusiness}
                                    />
                                    <span className={Styles.custom_radio} />
                                    {`：行政部署`}
                                </label>
                            </div>
                        </div>
                        <Box paddedRatio={2}/>
                        <div style={{height: "230px", overflowY: "auto", display:isBusiness ? "none" :"block"}}>
                            <table className={Styles.selection_table}>
                                <thead className={Styles.table_header}>
                                    <tr>
                                    <th style={{width: "70%"}}>宛先</th>
                                    <th style={{width: "30%"}}></th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {department && Object.keys(department).map(index => (
                                        (selfDepartmentId != department[index].departmentId &&(
                                        <tr key={department[index].departmentId}>    
                                            <td>{department[index].departmentName}</td>
                                            <td>
                                                <input type="checkbox" className={Styles.custom_checkbox}
                                                    onChange={ evt => { this.selectAddress(department[index]) } }
                                                    checked={this.isCheckSelected(department[index])}
                                                ></input>
                                            </td>
                                        </tr>
                                        ))
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
export default withTranslation()(withTheme(InputChatAddress));