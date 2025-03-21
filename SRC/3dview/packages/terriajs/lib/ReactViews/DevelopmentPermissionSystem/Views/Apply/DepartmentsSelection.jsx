import React from "react";
import PropTypes from "prop-types";
import { observer } from "mobx-react";
import { withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import Box from "../../../../Styled/Box";
import Spacing from "../../../../Styled/Spacing";
import Styles from "./scss/departments-selection.scss"
import { RawButton } from "../../../../Styled/Button";
import Icon, { StyledIcon } from "../../../../Styled/Icon";
import Config from "../../../../../customconfig.json";
import { tickStep } from "d3-array";

/**
 * 部署選択ダイアログ画面
 */

@observer
class DepartmentsSelection extends React.Component {
    static displayName = "DepartmentsSelection"
    static propsType = {
        terria: PropTypes.object.isRequired,
        viewState: PropTypes.object.isRequired,
        t: PropTypes.func.isRequired,
        checkedDepartments: PropTypes.object,
        confirmedCallback: PropTypes.func,
        closeCallback: PropTypes.func
    }

    constructor(props) {
        super(props);
        this.state = {
            viewState: props.viewState,
            terria: props.terria,
            // 部署一覧
            departments: [],
            // 選択済み部署一覧
            checkedDepartments: this.props.checkedDepartments,
            // 確定時のコールバック関数 引数：this.state.departments
            confirmedCallback: this.props.confirmedCallback,
            // クローズ時のコールバック関数 引数：なし
            closeCallback: this.props.closeCallback,
        }
    }

    /**
     * 初期表示
     */
    componentDidMount() {
        // 部署一覧取得
        fetch(Config.config.apiUrl + "/application/departments")
        .then(res => {
            return res.json();
        })
        .then(res => {
            if(res && Object.keys(res).length > 0){
                this.setState({departments:res});
                const checkedDepartments = this.state.checkedDepartments;
                if(checkedDepartments){
                    Object.keys(checkedDepartments).forEach(key => {
                        this.selectDepartment(checkedDepartments[key]);
                    });
                }
            }
        }).catch(error => {
            console.error('通信処理に失敗しました', error);
            alert('通信処理に失敗しました');
        });

        // コンポーネントドラッグ操作
        this.draggable(document.getElementById('departmentsSelectionModalDrag'), document.getElementById('departmentsSelectionModal'));
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
        this.state.closeCallback();
    }

    /**
     * 確定ボタン押下時
     * 親コンポーネントのコールバック関数呼び出し
     */
    confirmed(){
        const departments = this.state.departments;
        this.state.confirmedCallback(departments);
    }

    /**
     * 選択した部署を保存
     * @param {*} department 部署
     */
    selectDepartment(department){
        const departments = this.state.departments;
        const index = departments.findIndex(item => item.departmentId == department.departmentId);
        if(index > -1){
            departments[index].checked = !departments[index].checked;
            this.setState({departments:departments});
        }
    }

    render(){
        let departments = this.state.departments;
        return (
            <div className={Styles.overlay}>
                <div className={Styles.modal}  id="departmentsSelectionModal">
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
                    <nav className={Styles.custom_nuv} id="departmentsSelectionModalDrag">
                        担当課選択
                    </nav>
                    <div className={Styles.container}>
                        <Box paddedRatio={1}/>
                        <p>再提出の指示元担当課を選択してください。<br/>（複数選択可）</p>
                        <Box paddedRatio={1}/>
                        <div style={{height: "230px", overflowY: "auto", display:"block"}}>
                            <table className={Styles.selection_table}>
                                <thead className={Styles.table_header}>
                                    <tr>
                                    <th style={{width: "70%"}}>宛先</th>
                                    <th className="no-sort" style={{width: "30%"}}></th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {departments && Object.keys(departments).map(index => (
                                        <tr key={departments[index].departmentId}>    
                                            <td>{departments[index].departmentName}</td>
                                            <td>
                                                <input type="checkbox" className={Styles.custom_checkbox}
                                                    onChange={ evt => { this.selectDepartment(departments[index]) } }
                                                    checked={departments[index].checked}
                                                ></input>
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                        <Spacing bottom={2} />
                        <button
                            className={`${Styles.btn_baise_style} `}
                            onClick={e => {
                                this.confirmed();
                            }}
                        >
                            <span>決定</span>
                        </button>
                    </div>

                </div>
            </div>

        )
    }



}
export default withTranslation()(withTheme(DepartmentsSelection));