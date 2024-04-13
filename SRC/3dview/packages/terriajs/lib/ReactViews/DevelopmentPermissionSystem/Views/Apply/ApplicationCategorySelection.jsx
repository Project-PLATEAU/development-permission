import { observer } from "mobx-react";
import PropTypes from "prop-types";
import React from "react";
import { withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import Spacing from "../../../../Styled/Spacing";
import Box from "../../../../Styled/Box";
import Select from "../../../../Styled/Select";
import CustomStyle from "./scss/application-category-selection.scss";
import Config from "../../../../../customconfig.json";
import Styles from "../../PageViews/scss/pageStyle.scss";

/**
 * 申請区分選択画面の申請区分選択コンポーネント
 */
@observer
class ApplicationCategorySelection extends React.Component {
    static displayName = "ApplicationCategorySelection";
    static propTypes = {
        terria: PropTypes.object.isRequired,
        viewState: PropTypes.object.isRequired,
        theme: PropTypes.object,
        t: PropTypes.func.isRequired
    };
    constructor(props) {
        super(props);
        this.state = {
            viewState: props.viewState,
            terria: props.terria,
            //画面
            screen: [{}],
            //画面(概況診断タイプを問わず、全て画面)
            screenAll: [{}],
            //申請区分
            applicationCategory: [[{}]],
            //選択済み申請区分
            checkedApplicationCategory: [[{}]],
            //概況診断実施ボタンの有効フラグ
            disabledFlg: false,
            //申請区分選択入力エリアの高さ
            height:0,
            //選択中概況診断タイプ
            currentJudgementType:"",
            // 概況診断タイプ選択肢
            judgementTypeList: [],
            // 概況診断タイプのデフォルト選択肢
            defaultJudgementType:""
        };
    }

    /**
     * 初期処理
     */
    componentDidMount() {
        document.getElementById("loading").style.display = "block";

        let screen = this.state.screen;
        let screenAll = this.state.screenAll;
        let applicationCategory = this.state.applicationCategory;
        let checkedApplicationCategory = this.state.checkedApplicationCategory;
        // APIへのリクエスト・レスポンス結果取得処理
        fetch(Config.config.apiUrl + "/category/views/")
        .then(res => res.json())
        .then(res => {
            if(res.status === 401){
                alert("認証情報が無効です。ページの再読み込みを行います。");
                window.location.reload();
                return null;
            }
            if (Object.keys(res).length > 0  && !res.status) {
                screenAll = res;
                this.setState({ screenAll:screenAll});

            }else{
                alert("区分選択画面の項目一覧の取得に失敗しました。再度操作をやり直してください。");
                this.props.viewState.backFromInputApplyConditionView(); 
            }
            this.getJudgementTypes(screenAll);
        }).catch(error => {
            console.error('通信処理に失敗しました', error);
            alert('通信処理に失敗しました');
        });
    }

    /**
     * 申請区分の初期値設定
     * @param {*} screen 選択中概況診断タイプに対する画面
     * @param {*} applicationCategory 申請区分
     * @param {*} checkedApplicationCategory 選択中申請区分
     */
    initScreenData(screen,applicationCategory,checkedApplicationCategory){
        Object.keys(screen).map(key => {
            applicationCategory[key] = screen[key].applicationCategory;
            // チェック状態初期化
            for (let i = 0; i < applicationCategory[key].length; i++) {
                applicationCategory[key][i].checked = false;
            }
        });
        //　申請区分に選択中対象がある場合、再度選択済みで設定する
        //　選択しない場合状態の初期化を行う
        Object.keys(screen).map(key => {
            if (this.props.viewState.checkedApplicationCategoryLocalSave[key]) {
                checkedApplicationCategory[key] = this.props.viewState.checkedApplicationCategoryLocalSave[key];
                
                Object.keys(checkedApplicationCategory[key]).map(categoryKey => {
                    // select boxの場合初期化
                    if (!screen[key].multiple && checkedApplicationCategory[key][categoryKey].checked) {
                        let index = applicationCategory[key].findIndex((v) => v.id === checkedApplicationCategory[key][categoryKey].id);
                        if(index >= 0){
                            applicationCategory[key][index].checked = true;
                        }
                    }
                    // check boxの場合初期化
                    if(screen[key].multiple){
                        let index = applicationCategory[key].findIndex((v) => v.id === checkedApplicationCategory[key][categoryKey].id);
                        applicationCategory[key][index].checked = true;
                    }
                });

            } else {
                checkedApplicationCategory[key] = [{}];
                // selectboxの場合は先頭値を選択済みに追加しておく
                if (!screen[key].multiple && applicationCategory[key][0]) {
                    applicationCategory[key][0].checked = true;
                    checkedApplicationCategory[key][0] = Object.assign({}, applicationCategory[key][0]);
                }
            }
        });
        this.setState({ screen: screen, applicationCategory: applicationCategory, checkedApplicationCategory: checkedApplicationCategory })
    }

    /**
     * 概況診断タイプの選択肢取得
     * @param {*} screenAll 全て申請区分選択画面
     */
    getJudgementTypes(screenAll){
        let judgementTypeList = this.state.judgementTypeList;
        let defaultJudgementType = this.state.defaultJudgementType;
        let screen = this.state.screen;
        let applicationCategory = this.state.applicationCategory;
        let checkedApplicationCategory = this.state.checkedApplicationCategory;
        //APIへのリクエスト・レスポンス結果取得処理
        fetch(Config.config.apiUrl + "/category/judgementTypes")
        .then(res => res.json())
        .then(res => {
            if(res.status === 401){
                alert("認証情報が無効です。ページの再読み込みを行います。");
                window.location.reload();
                return null;
            }
            if (Object.keys(res).length > 0) {
                judgementTypeList = res;
                Object.keys(res).map(key => {
                    // check boxの場合初期化
                    if(res[key].checked){
                        defaultJudgementType = res[key].value;
                    }
                });
 
                screen = this.getScreenList(screenAll, defaultJudgementType, defaultJudgementType);
                this.setState({ judgementTypeList: res, defaultJudgementType: defaultJudgementType, currentJudgementType: defaultJudgementType});
                // 画面初期表示内容設定
                this.initScreenData(screen,applicationCategory,checkedApplicationCategory);
            }else{
                alert("概況診断タイプの選択肢取得に失敗しました。");
            }
        }).catch(error => {
            console.error('通信処理に失敗しました', error);
            alert('通信処理に失敗しました');
        }).finally(() => {
            setTimeout(() => {
                this.getWindowSize();
                document.getElementById("loading").style.display = "none";
            }, 3000)
        });
    }


    /**
     * 申請区分選択入力エリアの高さ再計算
     */
    getWindowSize() {
        if(this.props.viewState.showInputApplyConditionView){
            let win = window;
            let e = window.document.documentElement;
            let g = window.document.documentElement.getElementsByTagName('body')[0];
            let h = win.innerHeight|| e.clientHeight|| g.clientHeight;
    
            const getRect = document.getElementById("ApplicationCategorySelectionArea");
            let height = h - getRect.getBoundingClientRect().top - 75;
            this.setState({height: height});
        }
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
     * 項目一覧のcheckbox処理
     * @param {*} index インデックス
     * @param {*} event イベント
     */
    handleCheckBoxAdd( index, event) {
        // const index = this.state.index;
        let applicationCategory = this.state.applicationCategory;
        let key = event.target.value;
        applicationCategory[index][key].checked = event.target.checked;
        // 選択済み申請区分更新
        let checkedApplicationCategory = this.state.checkedApplicationCategory;
        if(event.target.checked){
            checkedApplicationCategory[index][key] = Object.assign({}, applicationCategory[index][key])
        }else{
            checkedApplicationCategory[index].splice(key,1);
        }

        this.setState({ checkedApplicationCategory: checkedApplicationCategory, applicationCategory: applicationCategory });
    }

    /**
     * selectbox処理
     * @param {*} index インデックス
     * @param {*} event イベント
     */
    handleSelectBoxAdd(index,event) {
        let applicationCategory = this.state.applicationCategory;
        let checkedApplicationCategory = this.state.checkedApplicationCategory;
        //　一旦全てのcheckedをfalseに設定
        Object.keys(applicationCategory[index]).map(key => {
            if (applicationCategory[index][key].checked) {
                applicationCategory[index][key].checked = false;
            }
            if (checkedApplicationCategory[index][key]) {
                delete checkedApplicationCategory[index][key];
            }
        });
        applicationCategory[index][event.target.value].checked = true;
        checkedApplicationCategory[index][event.target.value] = Object.assign({}, applicationCategory[index][event.target.value]);
        this.setState({ applicationCategory: applicationCategory, checkedApplicationCategory: checkedApplicationCategory });
    }

    /**
     * 「概況診断実施」ボタンを制御するために、すべて必須入力の申請区分が選択されたか判断
     * @returns 判定結果
     */
    checkSelectedAll(){
        
        let checkedCount = 0;
        const screen = this.state.screen;
        const checkedApplicationCategory = this.state.checkedApplicationCategory;

        // 画面に表示している申請区分リストをループして、下記の申請区分をカウンターする
        // ・必須入力の申請区分、選択されたのレコード
        // ・非必須入力の申請区分
        Object.keys(screen).map(index => {
            let permission = false;
            if (screen[index].require) {
                Object.keys(checkedApplicationCategory[index]).map(key => {
                    if (checkedApplicationCategory[index][key]?.id) {
                        permission = true;
                    }
                });
            } else {
                permission = true;
            }
            if(permission){
                checkedCount = checkedCount + 1;
            }
        });

        // カウンター結果が画面に表示している申請区分の件数と同じ場合、falseを返す、以外の場合、trueを返却する
        return screen.length == checkedCount ? false : true;
    }

    /**
     * 概況診断実施ボタン押下を押下すると、概況診断結果表示画面へ遷移する
     */
    moveToGeneralAndRoadJudgementResultView() {
        const screen = this.state.screen;
        const checkedApplicationCategory = this.state.checkedApplicationCategory;
 
        let checkedApplicationCategoryCopy = Object.assign({}, checkedApplicationCategory);
        Object.keys(checkedApplicationCategoryCopy).map(key => {
            checkedApplicationCategoryCopy[key] = checkedApplicationCategoryCopy[key].filter(function (s) { return Object.keys(s).length > 0 });
        });
        let checkedApplicationCategoryResult = {};
        Object.keys(screen).map(key => {
            checkedApplicationCategoryResult[key] = JSON.parse(JSON.stringify(screen[key]));
            checkedApplicationCategoryResult[key]["applicationCategory"] = checkedApplicationCategoryCopy[key];
        });
        // 選択済みの項目を保持して画面遷移
        this.props.viewState.moveToGeneralAndRoadJudgementResultView(checkedApplicationCategoryResult,checkedApplicationCategory);
    }

    /**
     * トップ画面に戻る
     */
    back() {
        try{
            const items = this.state.terria.workbench.items;
            for (const aItem of items) {
                if (aItem.uniqueId === Config.layer.lotnumberSearchLayerNameForApplicationTarget) {
                    this.state.terria.workbench.remove(aItem);
                    aItem.loadMapItems();
                }
            }
        }catch(error){
            console.error('処理に失敗しました', error);
        }
        this.props.viewState.backFromInputApplyConditionView();
    }

    /**
     * 選択中概況診断タイプを切り替える
     * @param {*} event イベント
     */
    changeJudgementType(event){
        let currentJudgementType = event.target.value;
        let defaultJudgementType = this.state.defaultJudgementType; 
        this.setState({currentJudgementType:currentJudgementType});
        let screenAll = this.state.screenAll;
 
        // 選択された概況診断タイプに対する申請区分選択画面リストを抽出
        let screen = this.getScreenList(screenAll, defaultJudgementType, currentJudgementType);
        //画面に表示できるの申請区分選択画面の入力値をクリアする
        this.initScreenData(screen,[[{}]],[[{}]]);

        // 切り替え時に、スクロールの一番上に戻る
        let screenArea = document.getElementById('ApplicationCategorySelectionArea');
        screenArea.scrollTo(0,0);
    }
 
    /**
     * 選択された概況診断タイプに対する申請区分選択画面リストを抽出
     * @param {*} screenAll 全て申請区分選択画面
     * @param {*} defaultJudgementType デフォルト選択中の概況診断タイプ
     * @param {*} currentJudgementType 選択された概況診断タイプ
     * @returns 
     */
    getScreenList(screenAll, defaultJudgementType, currentJudgementType){
        let screenList = [];
        Object.keys(screenAll).map( key => {
            let judgementTypeStr = screenAll[key]["judgementType"];
            // DBから取得したM_申請区分選択画面のレコードの概況診断タイプが空の場合、
            //デフォルト概況診断タイプを選択すれば、該当申請区分選択画面が表示できます
            if(judgementTypeStr === null || judgementTypeStr === ""){
                if(currentJudgementType == defaultJudgementType){
                    screenList.push(screenAll[key]);
                }
            }else{
               let judgementTypeAry = judgementTypeStr.split(",");
               let index= judgementTypeAry.findIndex((type) =>  type == currentJudgementType);
               if(index >-1){
                screenList.push(screenAll[key]);
               }
            }
        });
 
        return screenList;
    }

    render() {
        const screen = this.state.screen;
        const applicationCategory = this.state.applicationCategory;
        // 選択済み申請区分、画面表示用に詰め替え
        let checkedApplicationCategoryCopy = Object.assign({}, this.state.checkedApplicationCategory);
        Object.keys(checkedApplicationCategoryCopy).map(key => {
            checkedApplicationCategoryCopy[key] = checkedApplicationCategoryCopy[key].filter(function (s) { return Object.keys(s).length > 0 });
        });

        const heigth = this.state.height;
        const judgementTypeList = this.state.judgementTypeList;
        return (
            <>
            <div className={`${CustomStyle.div_area} ${CustomStyle.fullHeight}`} >
                <Box fullHeight css={`display:block`} id="ApplicationCategorySelection" >
                    <nav className={CustomStyle.custom_nuv} id="ApplicationCategorySelectionDrag">
                        申請区分選択
                    </nav>
                    <div id="loading" className={CustomStyle.customloaderParent} >
                        <img className={CustomStyle.customloader} src="./images/loader.gif" />
                    </div>
                    <div className={CustomStyle.div_area}>
                        <div className={CustomStyle.applyType_div_area}>
                            {/* <div css={`width:40%`}>
                                <label className={CustomStyle.radio_label}>
                                    <input
                                        className={CustomStyle.radio_input}
                                        type="radio"
                                        value="0"
                                        onChange={e => this.changeJudgementType(e)}
                                        checked={this.state.currentJudgementType == 0}
                                    />
                                    <span className={CustomStyle.custom_radio}/>
                                    {"：" + judgementTypeText_0}
                                </label>
                            </div>
                            <div css={`width:40%`}>
                                <label className={CustomStyle.radio_label}>
                                    <input
                                        className={CustomStyle.radio_input}
                                        type="radio"
                                        value="1"
                                        onChange={e => this.changeJudgementType(e)}
                                        checked={this.state.currentJudgementType == 1}
                                    />
                                    <span className={CustomStyle.custom_radio} />
                                    {"：" + judgementTypeText_1}
                                </label>
                            </div> */}
                            {Object.keys(judgementTypeList).map(key => (
                                <label className={CustomStyle.radio_label}>
                                    <input
                                        name="judgementType"
                                        className={CustomStyle.radio_input}
                                        type="radio"
                                        value={judgementTypeList[key].value}
                                        onChange={e => this.changeJudgementType(e)}
                                        checked={this.state.currentJudgementType == judgementTypeList[key].value}
                                    />
                                    <span className={CustomStyle.custom_radio} />
                                    {"：" + judgementTypeList[key].text}
                                    {Object.keys(judgementTypeList).length > (Number(key)+1) && (
                                        <span className={CustomStyle.spaceWith}></span>
                                    )}
                                </label>
                            ))}

                        </div>
                    </div>

                    <Box id="ApplicationCategorySelectionArea" overflowY={"auto"} styledMinHeight={"300px"} styledHeight={heigth + "px"}  css={`display:block; overflow-x:hidden `} >
                    {Object.keys(screen).map(index => (
                        <div key={index}>
                        {screen[index].screenId && (
                            <Box
                                centered
                                paddedHorizontally={3}
                                paddedVertically={5}
                                displayInlineBlock
                                className={CustomStyle.custom_content}
                            >
                                <h2 className={CustomStyle.title}>{(parseInt(index) + 1) + ". " + screen[index].title}</h2>
                                <div className={screen[index].multiple ? CustomStyle.scrollContainer : ""}>
                                {screen[index].multiple && (
                                    <>
                                    <p className={CustomStyle.table_explanation}><span dangerouslySetInnerHTML={{ __html: screen[index].explanation }}></span></p>
                                    <div className={CustomStyle.box}>
                                        <div className={CustomStyle.itemOption}>
                                            <h3 className={CustomStyle.sub_title}>項目一覧</h3>
                                            <table className={CustomStyle.selection_table}>
                                                <tbody>
                                                    {Object.keys(applicationCategory[index]).map(key => (
                                                        <tr key={key}>
                                                            <td className={CustomStyle.checkbox}>
                                                                {applicationCategory[index][key]?.id && (
                                                                    <input type="checkbox" value={key} onChange={e => this.handleCheckBoxAdd(index, e)} checked={applicationCategory[index][key]?.checked} />
                                                                )}
                                                            </td>
                                                            <td className={CustomStyle.content}>
                                                                {applicationCategory[index][key]?.id && (
                                                                    applicationCategory[index][key]?.content
                                                                )}
                                                            </td>
                                                        </tr>
                                                    ))}
                                                </tbody>
                                            </table>
                                        </div>
                                    </div>
                                    </>
                                )}
                                {!screen[index].multiple && (
                                    <>
                                    <p className={CustomStyle.explanation}><span dangerouslySetInnerHTML={{ __html: screen[index].explanation }}></span></p>
                                    <div className={CustomStyle.box}>
                                        <div className={CustomStyle.select_box}>
                                            <Select
                                                light={true}
                                                dark={false}
                                                onChange={e => this.handleSelectBoxAdd(index, e)}
                                                style={{ color: "#000" }}>
                                                {Object.keys(applicationCategory[index]).map(key => (
                                                    <option key={applicationCategory[index][key]?.content} value={key} selected={applicationCategory[index][key]?.checked}>
                                                        {applicationCategory[index][key]?.content}
                                                    </option>
                                                ))}
                                            </Select>
                                        </div>
                                    </div>
                                    </>
                                )}
                                </div>
                                <Spacing bottom={3} />
                                
                            </Box >
                        )} 
                        </div>
                    ))}
                    </Box>
                </Box >
            </div>
            <div className={CustomStyle.div_area} >
                <Box padded paddedHorizontally={3} paddedVertically={2} css={`display:block; text-align:center`} >
                    <button className={`${CustomStyle.btn_baise_style} ${CustomStyle.btn_judgement}`}
                        disabled={this.checkSelectedAll()}
                        style={{width:"45%"}}
                        onClick={evt => {
                            evt.preventDefault();
                            evt.stopPropagation();
                            this.moveToGeneralAndRoadJudgementResultView();
                        }}>
                        <span>概況診断実施</span>
                    </button>
                    
                    <button className={`${CustomStyle.btn_gry} ${CustomStyle.btn_baise_style} `}
                        style={{width:"45%"}}
                        onClick={evt => {
                            evt.preventDefault();
                            evt.stopPropagation();
                            this.back();
                        }}>
                        <span>戻る</span>
                    </button>
                </Box>
            </div>
            {/* <Box paddedRatio={2}></Box> */}
            </>
        );
    }
}

export default withTranslation()(withTheme(ApplicationCategorySelection));