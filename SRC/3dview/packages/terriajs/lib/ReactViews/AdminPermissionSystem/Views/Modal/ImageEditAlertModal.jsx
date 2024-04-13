import React from "react";
import PropTypes from "prop-types";
import { observer } from "mobx-react";
import { withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import Styles from "./scss/ImageEditAlertModal.scss"

/**
 * 行政用コンポーネント：marker.js2 注意モーダル
 */

@observer
class ImageEditAlertModal extends React.Component {
    static displayName = "ImageEditAlertModal"
    static propsType = {
        terria: PropTypes.object.isRequired,
        viewState: PropTypes.object.isRequired,
        t: PropTypes.func.isRequired,
        confirmAlert: PropTypes.func.isRequired
    }

    constructor(props){
        super(props);
        this.confirmAlert = this.confirmAlert.bind(this);
    }

    /**
     * 初期処理
     */
    componentDidMount() {
        this.draggable(document.getElementById('ImageEditAlertModalDrag'), document.getElementById('ImageEditAlertModal'));
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
     * 注意事項の確認完了
     * @returns 
     */
    confirmAlert = () => {
        this.props.confirmAlert();
    } 

    render(){
        return (
            <div className={Styles.overlay}>
                <div className={Styles.modal}  id="ImageEditAlertModal">
                    <nav className={Styles.custom_nuv} id="ImageEditAlertModalDrag">
                        注意事項
                    </nav>
                    <div className={Styles.container}>
                        <ul>
                            <li>画面の編集画面を表示します。</li>
                            <li>画面の左下に表示されるアイコン（<img src="./images/markerjs2.svg" />）をクリックすると、使用ライブラリの公式サイトに遷移します。</li>
                        </ul>
                        <p>（このメッセージは、本システムを開いている間に1回だけ表示されます。）</p>
                        <div className={Styles.confirmButton}>
                            <button 
                                onClick={() => {
                                    this.confirmAlert();
                                }}
                            >OK
                            </button>
                        </div>
                    </div>
                </div>
            </div>

        )
    }
}
export default withTranslation()(withTheme(ImageEditAlertModal))