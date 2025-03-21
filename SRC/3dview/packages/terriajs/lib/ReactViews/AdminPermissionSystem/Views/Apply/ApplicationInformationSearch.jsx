import { observer } from "mobx-react";
import PropTypes from "prop-types";
import React from "react";
import { withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import Icon, {GLYPHS, StyledIcon } from "../../../../Styled/Icon";
import Spacing from "../../../../Styled/Spacing";
import Text from "../../../../Styled/Text";
import Input from "../../../../Styled/Input";
import Box from "../../../../Styled/Box";
import Button, { RawButton } from "../../../../Styled/Button";
import Select from "../../../../Styled/Select";
import CustomStyle from "./scss/application-information-search.scss";
import CommonStrata from "../../../../Models/Definition/CommonStrata";
import webMapServiceCatalogItem from '../../../../Models/Catalog/Ows/WebMapServiceCatalogItem';
import Config from "../../../../../customconfig.json";
import {
    getShareData
} from "../../../Map/Panels/SharePanel/BuildShareLink";
import Cartographic from "terriajs-cesium/Source/Core/Cartographic";
import Ellipsoid from "terriajs-cesium/Source/Core/Ellipsoid";
import sampleTerrainMostDetailed from "terriajs-cesium/Source/Core/sampleTerrainMostDetailed";
import Styles from "../../../DevelopmentPermissionSystem/PageViews/scss/pageStyle.scss";
import ApplicationDetails from "./ApplicationDetails";
import Common from "../../../AdminDevelopCommon/CommonFixedValue.json";
import MultiSelect from "react-select";

const DropdownIndicator = props => {
    return(
        <div className={`${CustomStyle.select_box_icon}`}>
            <StyledIcon
                // light bg needs dark icon
                fillColor={"#000"}
                styledWidth="16px"
                glyph={GLYPHS.arrowDown}
            />
        </div>
    )
}

/**
 * 申請情報検索画面
 */
@observer
class ApplicationInformationSearch extends React.Component {

    static displayName = "ApplicationInformationSearch";

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
            //検索区分（検索値）
            searchCategory: [{"value": "0", "text": "申請情報", "checked": true}, {"value": "1", "text": "問い合わせ情報", "checked": false}],
            //申請者情報(検索値)
            applicantInformation: [],
            //申請状態(検索値)
            status: [],
            //問合せの回答状態(検索値)
            answerStatus: [],
            //部署(検索値)
            department: [],
            //回答者（検索値）
            answerName: [],
            //申請区分検索対象の画面情報(検索値)
            selectedScreen: [[], [], []],
            //検索結果テーブル定義情報
            table: [],
            //申請区分画面情報
            screen: [],
            //検索条件表示
            searchConditionShow: true,
            //検索結果表示
            searcResultShow: false,
            //検索結果表示区分
            searchResultCategory: 0,
            //検索結果
            searchValue: [],
            //申請情報詳細表示
            showApplyDetail: false, 
            // 申請種類(検索値)
            applicationType: [],
            // 申請段階（検索値）
            applicationStep: [],
            //申請追加情報（検索値）(検索条件1～3にセットする値)
            applicationAddInfo: [[], [], []],
            // 各申請段階に対する申請追加情報（申請段階IDをキーとしたマップ）
            allApplicationAddInfo: {},
            //申請追加情報(検索値)の表示フラグ
            applicationAddInfoDisplay: false,
            //条文ステータス情報(検索値)
            itemAnswerStatus: [],
            //条文ステータス(検索値)の表示フラグ
            itemAnswerStatusDisplay: false,
            // ソート対象カラム
            sortColumn: "",
            //　昇順/降順
            sortType: ""
        };
    }

    /**
     * 初期処理（サーバからデータを取得）
     */
    componentDidMount() {
        // 検索結果テーブルのカラム取得
        fetch(Config.config.apiUrl + "/application/search/columns")
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
                if (Object.keys(res).length > 0 && !res.status) {
                    this.setState({
                        table: res
                    });
                } else if (res.status) {
                    alert(res.status + "エラーが発生しました");
                } else {
                    alert("申請情報検索結果表示項目一覧取得に失敗しました。再度操作をやり直してください。");
                }
            }).catch(error => {
                console.error('通信処理に失敗しました', error);
                alert('通信処理に失敗しました');
                this.props.viewState.hideApplicationInformationSearchView();
            });
        
        // 検索条件項目・選択肢取得
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
                    let selectedScreen = this.state.selectedScreen;
                    Object.keys(selectedScreen).map(key => {
                        selectedScreen[key] = JSON.parse(JSON.stringify(res.applicationCategories));
                    })
                    document.getElementById("customloaderSearch").style.display = "none";
                    let applicationType = res.applicationTypes;
                    let applicationStep = res.applicationSteps;
                    
                    // 0:1行のみの入力欄で表示、
                    // 1:複数行の入力欄で表示、
                    // 2:日付（カレンダー）、
                    // 3:数値、
                    // 4:ドロップダウン単一選択
                    // 5:ドロップダウン複数選択
                    let resApplicationAddInfoList = {};
                    let applicationAddInfoList = res.applicantAddInformationItemForm;
                    Object.keys(applicationStep).map(applicationStepIndex => {
                        let mapKey = applicationStep[applicationStepIndex].applicationStepId;
                        resApplicationAddInfoList[mapKey] = [];
                        applicationAddInfoList.forEach(applicationAddInfoListVal => {
                            applicationAddInfoListVal.applicationStep = applicationAddInfoListVal.applicationSteps;
                            Object.keys(applicationAddInfoListVal.applicationSteps).map(applicationAddInfoStepIndex => {
                                if(applicationAddInfoListVal.applicationSteps[applicationAddInfoStepIndex].applicationStepId == mapKey) {
                                    resApplicationAddInfoList[mapKey].push(JSON.parse(JSON.stringify(applicationAddInfoListVal)));
                                }
                            })
                        })
                    })
                    // 回答条文ステータス
                    let itemAnswerStatus = res.itemAnswerStatus;

                    this.setState({
                        applicantInformation: res.applicantInformationItemForm,
                        screen: res.applicationCategories,
                        status: res.status,
                        answerStatus: res.answerStatus,
                        department: res.department,
                        answerName: res.answerName,
                        selectedScreen: selectedScreen,
                        applicationType: applicationType,
                        applicationStep: applicationStep,
                        applicationAddInfoDisplay: false,
                        itemAnswerStatus: itemAnswerStatus,
                        itemAnswerStatusDisplay: false,
                        allApplicationAddInfo: resApplicationAddInfoList
                    });
                    
                } else {
                    alert("申請情報検索条件一覧取得に失敗しました。再度操作をやり直してください。");
                }
            }).catch(error => {
                console.error('通信処理に失敗しました', error);
                alert('通信処理に失敗しました');
                this.props.viewState.hideApplicationInformationSearchView();
            });

        this.draggable(document.getElementById('searchFrameDrag'), document.getElementById('searchFrame'));
    }

    /**
     * 検索区分選択時
     * @param {number} 対象index
     */
    handleSelectSearchCategory(index){
        let searchCategory = this.state.searchCategory;
        Object.keys(searchCategory).map(index => {
            searchCategory[index].checked = false;
        })
        if (index >= 0) {
            searchCategory[index].checked = true;
        }
        // ステータスの選択値を空にする
        document.getElementById("statusSelect").selectedIndex = -1;

        // 設定をStateに格納
        this.setState({ searchCategory: searchCategory})
    }

    /**
     * 申請者情報入力時
     * @param {number} 申請者情報の対象index
     * @param {string} 入力された値
     */
    inputChange(index, value) {
        let applicantInformation = this.state.applicantInformation;
        applicantInformation[index].value = value;
        this.setState({ applicantInformation: applicantInformation });
    }

    /**
     * 申請区分選択時①
     * @param {number} 対象index
     * @param {number} 対象画面key
     */
    handleSelectScreen(index, key) {
        let selectedScreen = this.state.selectedScreen;
        Object.keys(selectedScreen[index]).map(key => {
            selectedScreen[index][key].checked = false;
        })
        if (key >= 0) {
            selectedScreen[index][key].checked = true;
        }
        this.setState({ selectedScreen: selectedScreen });
    }

    /**
     * 申請区分選択時②
     * @param {number} 対象画面index
     * @param {number} 対象区分key
     */
    handleSelectApplicationCategory(index, categoryKey) {
        let selectedScreen = this.state.selectedScreen;
        Object.keys(selectedScreen[index]).map(key => {
            Object.keys(selectedScreen[index][key]["applicationCategory"]).map(categoryKey => {
                selectedScreen[index][key]["applicationCategory"][categoryKey].checked = false;
            })
            if (categoryKey >= 0) {
                if (selectedScreen[index][key].checked) {
                    selectedScreen[index][key]["applicationCategory"][categoryKey].checked = true;
                }
            }
        })
        this.setState({ selectedScreen: selectedScreen });
    }

    
    /**
     * 申請区分選択時（複数選択）
     * @param {*} index 申請区分条件のindex（条件１～３）
     * @param {*} categoryIndex 対象区分key（左側のプルダウンの選択肢のindex）
     * @param {*} selectedValue 選択中選択肢
     */
    handleMultiSelectApplicationCategory(index, categoryIndex, selectedValue){

        if(!selectedValue){
            return;
        }

        let selectedScreen = this.state.selectedScreen;
        // 前回選択したの結果をクリアする
        Object.keys(selectedScreen[index][categoryIndex].applicationCategory).map(i => {
            selectedScreen[index][categoryIndex].applicationCategory[i].checked = false;
        })
        
        // 今回の選択結果を設定
        Object.keys(selectedValue).map(key => {
            let value = selectedValue[key].value;
            selectedScreen[index][categoryIndex].applicationCategory[Number(value)].checked = true;
        })

        this.setState({ selectedScreen: selectedScreen });
    }

    /**
     * 申請追加情報項目選択時
     * @param {*} index　申請追加情報検索条件index 
     * @param {*} key 対象申請追加情報項目key
     */
    handleSelectApplicationAddInfo(index, key){
        let applicationAddInfo = this.state.applicationAddInfo;
        Object.keys(applicationAddInfo[index]).map(itemKey => {
            applicationAddInfo[index][itemKey].checked = false;
        })
        if (key >= 0) {
            applicationAddInfo[index][key].checked = true;
        }
        this.setState({ applicationAddInfo: applicationAddInfo });
    }

    /**
     * 申請追加情報項目の入力値(テキスト、数値)が変わる時
     * @param {*} index 申請追加情報検索条件index 
     * @param {*} key 対象申請追加情報項目key
     * @param {*} value 対象申請追加情報項目の入力値
     */
    inputChangeApplicationAddInfoItem(index, key, value){
        let applicationAddInfo = this.state.applicationAddInfo;
        applicationAddInfo[index][key].value = value;
        this.setState({ applicationAddInfo: applicationAddInfo });
    }

       /**
     * 申請追加情報項目の入力値(日付)が変わる時
     * @param {*} index 申請追加情報検索条件index 
     * @param {*} key 対象申請追加情報項目key
     * @param {string} date 対象申請追加情報項目の入力値
     */
    changeApplicationAddInfoItemDate(index, key, dateStr){
        let applicationAddInfo = this.state.applicationAddInfo;
        if(dateStr){
            let date = new Date(dateStr);
            let formated = date.toLocaleDateString("ja-JP",{year: "numeric", month:"2-digit", day:"2-digit"}).split("/").join("-");
            applicationAddInfo[index][key].value = formated;
        }else{
            applicationAddInfo[index][key].value = "";
        }

        this.setState({ applicationAddInfo: applicationAddInfo });
    }

    /**
     * 申請追加情報項目選択時(単一選択)
     * @param {*} index 申請追加情報検索条件index
     * @param {*} key 対象申請追加情報項目key
     * @param {*} itemKey 対象申請追加情報項目の選択肢Key
     */
    handleSelectApplicationAddInfoItem(index, key, itemKey){
        let applicationAddInfo = this.state.applicationAddInfo;
        Object.keys(applicationAddInfo[index][key].itemOptions).map(i => {
            applicationAddInfo[index][key].itemOptions[i].checked = false;
        })
        if (itemKey >= 0) {
            applicationAddInfo[index][key].itemOptions[itemKey].checked = true;
        }
        this.setState({ applicationAddInfo: applicationAddInfo });
    }

    /**
     * 選択肢リストを、multiSelectに利用可能の形に整形する 
     * @param {*} itemOptions 複数選択の追加情報の選択肢
     * @returns 
     */
    getMultiSelectOptions(itemOptions){

        let options = [];
        Object.keys(itemOptions).map(index => {
            let option = {"value": index, "label": itemOptions[index].content};
            options.push(option);
        });

        return options;
    }

    /**
     * 選択肢リストを、multiSelectに利用可能の形に整形する 
     * @param {*} itemOptions 複数選択の追加情報の選択肢
     * @returns 
     */
    getMultiSelectDefaultValue(itemOptions){

        let options = [];
        Object.keys(itemOptions).map(index => {
            if(itemOptions[index].checked){
                let option = {"value": index, "label": itemOptions[index].content};
                options.push(option);
            }
        });

        return options;
    }

    /**
     * 申請追加情報項目選択時（複数選択）
     * @param {*} index 申請追加情報検索条件index(条件１～３)
     * @param {*} itemIndex 申請追加情報項目index（左側のプルダウンの選択肢のindex）
     * @param {*} selectedValue 選択中選択肢(選択中選択肢リスト)
     */
    handleMultiSelectApplicationAddInfoItem(index, itemIndex, selectedValue){

        if(!selectedValue){
            return;
        }

        let applicationAddInfo = this.state.applicationAddInfo;
        // 前回選択したの結果をクリアする
        Object.keys(applicationAddInfo[index][itemIndex].itemOptions).map(i => {
            applicationAddInfo[index][itemIndex].itemOptions[i].checked = false;
        })
        
        // 今回の選択結果を設定
        Object.keys(selectedValue).map(key => {
            let value = selectedValue[key].value;
            applicationAddInfo[index][itemIndex].itemOptions[Number(value)].checked = true;
        })

        this.setState({ applicationAddInfo: applicationAddInfo });
    }

    /**
     * ステータス選択時
     * @param {number} 対象index
     */
    handleSelectStatus(index) {
        let searchCategory = this.state.searchCategory;
        if(searchCategory[0].checked){
            let status = this.state.status;
            Object.keys(status).map(index => {
                status[index].checked = false;
            })
            if (index >= 0) {
                status[index].checked = true;
            }
            this.setState({ status: status });
        }
        if(searchCategory[1].checked){
            let answerStatus = this.state.answerStatus;
            Object.keys(answerStatus).map(index => {
                answerStatus[index].checked = false;
            })
            if (index >= 0) {
                answerStatus[index].checked = true;
            }
            this.setState({ answerStatus: answerStatus });   
        }
        
    }

    /**
     * 部署選択時
     * @param {number} 対象index
     */
    handleSelectDepartment(index) {
        let department = this.state.department;
        Object.keys(department).map(index => {
            department[index].checked = false;
        })
        if (index >= 0) {
            department[index].checked = true;
        }
        this.setState({ department: department });
    }

    /**
     * 回答者選択時
     * @param {number} 対象index
     */
    handleSelectAnswerName(index){
        let answerName = this.state.answerName;
        Object.keys(answerName).map(index => {
            answerName[index].checked = false;
        })
        if (index >= 0) {
            answerName[index].checked = true;
        }
        this.setState({ answerName: answerName });
    }

    /**
     * 申請種類選択時
     * @param {number} 対象index
     */
    handleSelectApplicationType(index){
        let applicationType = this.state.applicationType;

        Object.keys(applicationType).map(key => {
            applicationType[key].checked = false;
        })
        if (index >= 0) {
            applicationType[index].checked = true;

            let applicationStep = this.state.applicationStep;
            let checkedApplicationStep = -1; 
            Object.keys(applicationStep).map(key => {
                if(applicationStep[key].checked){
                    checkedApplicationStep = applicationStep[key].applicationStepId; 
                }
            })

            applicationStep = applicationType[index].applicationSteps;
            Object.keys(applicationStep).map(key => {
                    applicationStep[key].checked = false; 
            })
            // 申請段階選択状態をリセット
            let itemAnswerStatusDisplay = applicationType[index].applicationSteps[0] == 2;
            const allApplicationAddInfo = this.state.allApplicationAddInfo;
            const applicationAddInfoDisplay = allApplicationAddInfo[applicationType[index].applicationSteps[0].applicationStepId].length > 0;
            let applicationAddInfo = [
                JSON.parse(JSON.stringify(allApplicationAddInfo[applicationStep[index].applicationStepId])),
                JSON.parse(JSON.stringify(allApplicationAddInfo[applicationStep[index].applicationStepId])),
                JSON.parse(JSON.stringify(allApplicationAddInfo[applicationStep[index].applicationStepId]))
            ];
            /// 申請段階の選択肢リストを選択可能値に更新
            this.setState({ applicationType: applicationType, applicationStep:applicationType[index].applicationSteps,  applicationAddInfo: applicationAddInfo,applicationAddInfoDisplay: applicationAddInfoDisplay, itemAnswerStatusDisplay: itemAnswerStatusDisplay});
        }else{
            this.setState({ applicationType: applicationType });
        }
    }

     /**
     * 申請段階選択時
     * @param {number} 対象index
     */
     handleSelectapplicationStep(index){
        let applicationStep = this.state.applicationStep;

        Object.keys(applicationStep).map(key => {
            applicationStep[key].checked = false;
        })
        if (index >= 0) {
            applicationStep[index].checked = true;


            let itemAnswerStatusDisplay = false;
            //選択された申請段階が事前協議の場合、
            if(applicationStep[index].applicationStepId == 2){
                itemAnswerStatusDisplay = true;
            }
            const allApplicationAddInfo = this.state.allApplicationAddInfo;
            // 申請段階にひもづく申請追加情報が存在する場合検索を有効にする
            const applicationAddInfoDisplay = allApplicationAddInfo[applicationStep[index].applicationStepId].length > 0;
            // 検索条件入力値リセット
            let applicationAddInfo = [
                JSON.parse(JSON.stringify(allApplicationAddInfo[applicationStep[index].applicationStepId])),
                JSON.parse(JSON.stringify(allApplicationAddInfo[applicationStep[index].applicationStepId])),
                JSON.parse(JSON.stringify(allApplicationAddInfo[applicationStep[index].applicationStepId]))
            ];
            this.setState({ applicationStep: applicationStep, applicationAddInfo: applicationAddInfo,applicationAddInfoDisplay: applicationAddInfoDisplay, itemAnswerStatusDisplay: itemAnswerStatusDisplay});
        }else{
            this.setState({ applicationStep: applicationStep, applicationAddInfoDisplay: false, itemAnswerStatusDisplay: false});
        }
    }

    /**
     * 条文ステータス選択時
     * @param {*} index 対象index
     */
    changeItemAnswerStatus(index){
        let itemAnswerStatus = this.state.itemAnswerStatus;
        itemAnswerStatus[index].checked = !itemAnswerStatus[index].checked;
        this.setState({itemAnswerStatus :itemAnswerStatus });
    }

    /**
     * 申請IDの値変更時
     * @param {*} value 入力値
     * @param {*} stateApplicationId　入力前の申請ID 
     */
    inputChangeApplicationId(value, stateApplicationId){

        // 未入力・クリアの場合、
        if(value == null || value == ""){
            this.setState({applicationId: ""});
        }else{
            // 正規表現：数字だけ
            const onlyNumberRegex = new RegExp(/^[0-9]*$/);
            if(onlyNumberRegex.exec(value) == null){
                // 数字以外入力する場合、入力値無視して、元々値のまま
                this.setState({applicationId: stateApplicationId});
            }else{
                this.setState({applicationId: value});
            }
        }
    }
    
    /**
     * クリア
     */
    clear() {
        let searchCategory = this.state.searchCategory;
        searchCategory[0].checked = true;
        searchCategory[1].checked = false;

        let status = this.state.status;
        Object.keys(status).map(index => {
            status[index].checked = false;
        })
        let answerStatus = this.state.answerStatus;
        Object.keys(answerStatus).map(index => {
            console.log(answerStatus[index]);
            answerStatus[index].checked = false;
        })
        let department = this.state.department;
        Object.keys(department).map(index => {
            department[index].checked = false;
        })
        let selectedScreen = this.state.selectedScreen;
        Object.keys(selectedScreen).map(index => {
            Object.keys(selectedScreen[index]).map(key => {
                selectedScreen[index][key].checked = false;
                Object.keys(selectedScreen[index][key]["applicationCategory"]).map(categoryKey => {
                    selectedScreen[index][key]["applicationCategory"][categoryKey].checked = false;
                })
            })
        })
        let applicantInformation = this.state.applicantInformation;
        Object.keys(applicantInformation).map(key => {
            applicantInformation[key].value = "";
        })
        let answerName = this.state.answerName;
        Object.keys(answerName).map(index => {
            answerName[index].checked = false;
        })

        let applicationType = this.state.applicationType;
        Object.keys(applicationType).map(key => {
            applicationType[key].checked = false;
        })
        let applicationStep = this.state.applicationStep;
        Object.keys(applicationStep).map(key => {
            applicationStep[key].checked = false;
        })
        let applicationAddInfo = this.state.applicationAddInfo;
        Object.keys(applicationAddInfo).map(index => {
            Object.keys(applicationAddInfo[index]).map(key => {
                applicationAddInfo[index][key].checked = false;
                applicationAddInfo[index][key].value =  "";
                Object.keys(applicationAddInfo[index][key]["itemOptions"]).map(optionKey => {
                    applicationAddInfo[index][key]["itemOptions"][optionKey].checked = false;
                });
            })
        })

        let itemAnswerStatus = this.state.itemAnswerStatus;
        Object.keys(itemAnswerStatus).map(key => {
            itemAnswerStatus[key].checked = false;
        })

        this.setState({ status: status, department: department, selectedScreen: selectedScreen, applicantInformation: applicantInformation,
            applicationType: applicationType, applicationStep: applicationStep, applicationAddInfo: applicationAddInfo, applicationAddInfoDisplay: false,
            itemAnswerStatus: itemAnswerStatus, itemAnswerStatusDisplay: false
         });

    }
    
    setSearchRequestBody(searchResultCategory, resultSearchCategory) {
        const selectedScreen = this.state.selectedScreen;
        let resultScreen = new Array();
        // 申請種別
        let resultApplicationType = new Array();
        const applicationType = this.state.applicationType;
        Object.keys(applicationType).map(index => {
            if (applicationType[index].checked) {
                resultApplicationType[resultApplicationType.length] = JSON.parse(JSON.stringify(applicationType[index]));
            }
        })
        // 申請段階
        let resultApplicationStep = new Array();
        const applicationStep = this.state.applicationStep;
        Object.keys(applicationStep).map(index => {
            if (applicationStep[index].checked) {
                resultApplicationStep[resultApplicationStep.length] = JSON.parse(JSON.stringify(applicationStep[index]));
            }
        })
        // 申請区分
        Object.keys(selectedScreen).map(index => {
            Object.keys(selectedScreen[index]).map(screenKey => {
                if (selectedScreen[index][screenKey].checked && selectedScreen[index][screenKey].screenId) {
                    const resultScreenIndex = resultScreen.length;
                    resultScreen[resultScreenIndex] = JSON.parse(JSON.stringify(selectedScreen[index][screenKey]));
                    resultScreen[resultScreenIndex]["applicationCategory"] = new Array();
                    Object.keys(selectedScreen[index][screenKey]["applicationCategory"]).map(categoryKey => {
                        if (selectedScreen[index][screenKey]["applicationCategory"][categoryKey].checked) {
                            resultScreen[resultScreenIndex]["applicationCategory"] = new Array(JSON.parse(JSON.stringify(selectedScreen[index][screenKey]["applicationCategory"][categoryKey])));
                        }
                    })
                }
            })
        })

        // ステータス
        const status = this.state.status;
        let resultStatus = new Array();
        Object.keys(status).map(index => {
            if (status[index].checked) {
                resultStatus[resultStatus.length] = JSON.parse(JSON.stringify(status[index]));
            }
        })

        // 担当課
        const department = this.state.department;
        let resultDepartment = new Array();
        Object.keys(department).map(index => {
            if (department[index].checked) {
                resultDepartment[resultDepartment.length] = JSON.parse(JSON.stringify(department[index]));
            }
        })

        // 申請者情報
        const applicantInformation = this.state.applicantInformation;
        let resultApplicantInformation = new Array();
        Object.keys(applicantInformation).map(index => {
            if (applicantInformation[index].value) {
                resultApplicantInformation[resultApplicantInformation.length] = JSON.parse(JSON.stringify(applicantInformation[index]));
            }
        })        
        // ステータス（問い合わせ情報）
        const answerStatus = this.state.answerStatus;
        let resultAnswerStatus = new Array();
        Object.keys(answerStatus).map(index => {
            if(answerStatus[index].checked){
                resultAnswerStatus[resultAnswerStatus.length] = JSON.parse(JSON.stringify(answerStatus[index]));
            }
        })

        // 回答者
        const answerName = this.state.answerName;
        let resultAnswerName = new Array();
        Object.keys(answerName).map(index => {
            if(answerName[index].checked){
                resultAnswerName[resultAnswerName.length] = JSON.parse(JSON.stringify(answerName[index]));
            }
        })

        // 申請ID
        let applicationId = null;
        if(this.state.applicationId){
            applicationId = parseInt(this.state.applicationId);
        }

        // 条文ステータス
        let resultAnswerItemStatus = new Array();
        if (this.state.itemAnswerStatusDisplay == true) {
            const itemAnswerStatus = this.state.itemAnswerStatus;
            Object.keys(itemAnswerStatus).map(index => {
                if (itemAnswerStatus[index].checked) {
                    resultAnswerItemStatus[resultAnswerItemStatus.length] = JSON.parse(JSON.stringify(itemAnswerStatus[index]));
                }
            })
        }
        // 申請追加情報
        let resultApplicationAddInfo = new Array();
        if (this.state.applicationAddInfoDisplay == true) {
            const applicationAddInfo = this.state.applicationAddInfo;
            Object.keys(applicationAddInfo).map(index => {
                Object.keys(applicationAddInfo[index]).map(condKey =>{
                    if (applicationAddInfo[index][condKey].checked) {
                        //const resultApplicationAddInfoIndex = resultApplicationAddInfo.length;
                        if (applicationAddInfo[index][condKey].itemType == "4" || applicationAddInfo[index][condKey].itemType == "5") {
                            // ドロップダウン単一選択または複数選択
                            Object.keys(applicationAddInfo[index][condKey].itemOptions).map(itemKey => {
                                if (applicationAddInfo[index][condKey].itemOptions[itemKey]?.checked) {
                                    let selectResult = JSON.parse(JSON.stringify(applicationAddInfo[index][condKey]));
                                    selectResult["value"] = applicationAddInfo[index][condKey].itemOptions[itemKey]?.id;
                                    resultApplicationAddInfo.push(selectResult);     
                                }
                            })
                        } else {
                            // それ以外の項目型
                            resultApplicationAddInfo.push(JSON.parse(JSON.stringify(applicationAddInfo[index][condKey])));
                        }
                    }
                })
            })
        }
        let res = {}
        if (searchResultCategory === "0") {
            res = {
                applicantInformationItemForm: resultApplicantInformation,
                applicationCategories: resultScreen,
                status: resultStatus,
                department: resultDepartment,
                searchCategory : resultSearchCategory,
                answerName: resultAnswerName,
                applicationTypes: resultApplicationType,
                applicationSteps: resultApplicationStep,
                itemAnswerStatus: resultAnswerItemStatus,
                applicantAddInformationItemForm: resultApplicationAddInfo,
                applicationId: applicationId
            };
        } else if (searchResultCategory === "1") {
            res = {
                applicantInformationItemForm: resultApplicantInformation,
                applicationCategories: resultScreen,
                answerStatus: resultAnswerStatus,
                department: resultDepartment,
                searchCategory : resultSearchCategory,
                answerName: resultAnswerName,
                applicationTypes: resultApplicationType,
                applicationSteps: resultApplicationStep,
                itemAnswerStatus: resultAnswerItemStatus,
                applicantAddInformationItemForm: resultApplicationAddInfo,
                applicationId: applicationId
            };
        }
        
        return res;
    }

    /**
     * 検索
     */
    search() {
        this.setState({
            searchConditionShow: false,
            searcResultShow: true,
        });

        document.getElementById("customloaderSearch").style.display = "block";
        // 検索区分
        let searchResultCategory = "";
        const searchCategory = this.state.searchCategory;
        let resultSearchCategory = new Array();
        Object.keys(searchCategory).map(index => {
            if(searchCategory[index].checked){
                resultSearchCategory[resultSearchCategory.length] = JSON.parse(JSON.stringify(searchCategory[index]));
                this.setState({ searchResultCategory: index});
                searchResultCategory= index;
            }
        })
        // リクエストボディ取得
        let requestBody = this.setSearchRequestBody(searchResultCategory, resultSearchCategory);
        // 申請情報検索
        if(searchResultCategory === "0"){
            fetch(Config.config.apiUrl + "/application/search", {
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
                // console.log(res);
                if (Object.keys(res).length > 0 && !res.status) {
                    this.setState({
                        searcConditionShow: false,
                        searcResultShow: true,
                        searchValue: res,
                        searchConditionShow: false,
                        searcResultShow: true,
                        sortColumn: "",
                        sortType: ""
                    });
                } else if (res.status) {
                    this.setState({ searchValue: [] });
                    alert(res.status + "エラーが発生しました");
                } else {
                    this.setState({ searchValue: [] });
                    alert("検索結果はありません");
                }
            }).catch(error => {
                this.setState({ searchValue: [] });
                console.error('通信処理に失敗しました', error);
                alert('通信処理に失敗しました');
            }).finally(() => document.getElementById("customloaderSearch").style.display = "none");
        }
        // 問い合わせ情報検索
        if(searchResultCategory === "1"){
            fetch(Config.config.apiUrl + "/chat/search", {
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
                if (Object.keys(res).length > 0 && !res.status) {
                    this.setState({
                        searchConditionShow: false,
                        searcResultShow: true,
                        searchValue: res,
                        sortColumn: "",
                        sortType: ""
                    });
                } else if (res.status) {
                    this.setState({ searchValue: [] });
                    alert(res.status + "エラーが発生しました");
                } else {
                    this.setState({ searchValue: [] });
                    alert("検索結果はありません");
                }
            }).catch(error => {
                this.setState({ searchValue: [] });
                console.error('通信処理に失敗しました', error);
                alert('通信処理に失敗しました');
            }).finally(() => document.getElementById("customloaderSearch").style.display = "none");
        }
    }

    /**
     * 申請詳細画面へ遷移
     * @param {number} applicationId 申請ID
     */
    details(applicationId) {
        this.props.viewState.nextApplicationDetailsView(applicationId);
    }

    /**
     * 申請地レイヤーの表示
     * @param {object} searchResult 検索結果
     */
    showLayers(searchResult) {
        try{
            const wmsUrl = Config.config.geoserverUrl;
            const items = this.state.terria.workbench.items;
            let layerFlg = false;
            if (searchResult.applicationId) {
                for (const aItem of items) {
                    if (aItem.uniqueId === Config.layer.lotnumberSearchLayerNameForApplicationSearchTarget) {
                        aItem.setTrait(CommonStrata.user,
                            "parameters",
                            {
                                "viewparams": Config.layer.lotnumberSearchViewParamNameForApplicationSearchTarget + searchResult.applicationId,
                            });
                        aItem.loadMapItems();
                        layerFlg = true;
                    }
                }
            }
            if (searchResult.lotNumbers) {
                this.focusMapPlaceDriver(searchResult.lotNumbers);
            }

            if(!layerFlg){
                if (searchResult.applicationId) {
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
                            "viewparams": Config.layer.lotnumberSearchViewParamNameForApplicationSearchTarget + searchResult.applicationId,
                        });
                    item.loadMapItems();
                    this.state.terria.workbench.add(item);
                }
            }
        } catch (error) {
            console.error('処理に失敗しました', error);
        }
    }

    /**
     * フォーカス処理ドライバー
     * @param {object} lotNumbers 申請地情報
     */
    focusMapPlaceDriver(lotNumbers) {
        let applicationPlace = Object.values(lotNumbers);
        applicationPlace = applicationPlace.filter(Boolean);
        let maxLon = 0;
        let maxLat = 0;
        let minLon = 0;
        let minLat = 0;
        Object.keys(applicationPlace).map(key => {
            const targetMaxLon = parseFloat(applicationPlace[key].maxlon);
            const targetMaxLat = parseFloat(applicationPlace[key].maxlat);
            const targetMinLon = parseFloat(applicationPlace[key].minlon);
            const targetMinLat = parseFloat(applicationPlace[key].minlat);
            if (key === 0 || key === "0") {
                maxLon = targetMaxLon;
                maxLat = targetMaxLat;
                minLon = targetMinLon;
                minLat = targetMinLat;
            } else {
                if (maxLon < targetMaxLon) {
                    maxLon = targetMaxLon;
                }
                if (maxLat < targetMaxLat) {
                    maxLat = targetMaxLat;
                }
                if (minLon > targetMinLon) {
                    minLon = targetMinLon;
                }
                if (minLat > targetMinLat) {
                    minLat = targetMinLat;
                }
            }
        })
        this.outputFocusMapPlace(maxLon, maxLat, minLon, minLat, (maxLon + minLon) / 2, (maxLat + minLat) / 2);
    }

    /**
     * フォーカス処理
     * @param {number} maxLon 最大経度
     * @param {number} maxLat 最大緯度
     * @param {number} minLon 最小経度
     * @param {number} minLat 最小緯度
     * @param {number} lon 経度
     * @param {number} lat 緯度
     */
    outputFocusMapPlace(maxLon, maxLat, minLon, minLat, lon, lat) {
        this.props.terria.focusMapPlace(maxLon, maxLat, minLon, minLat, lon, lat, this.props.viewState);
    }

    /**
     * 閉じる
     */
    close() {
        try{
            const items = this.state.terria.workbench.items;
            for (const aItem of items) {
                if (aItem.uniqueId === Config.layer.lotnumberSearchLayerNameForApplicationSearchTarget || aItem.uniqueId === Config.landMark.id) {
                    this.state.terria.workbench.remove(aItem);
                    aItem.loadMapItems();
                }
            }
        } catch (error) {
            console.error('処理に失敗しました', error);
        }
        this.props.viewState.hideApplicationInformationSearchView();
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
            content.style.top = (event.clientY + (parseInt(content.clientHeight) / 2) - 10) + 'px';
            content.style.left = event.clientX + 'px';
        }
    }

    /**
     * 申請情報詳細画面表示
     * @param {*} active アクティブページ
     */
    showApplyDetail(active){
        this.state.viewState.changeApplyPageActive(active);
        this.state.viewState.setAdminBackPage("applySearch", "applyList");
    }

    /**
     * 申請情報詳細表示設定
     * @returns 
     */
    showApplyList(){
        this.setState({ showApplyDetail: false})
    }

    /**
     * 検索条件を隠す
     */
    hideSearchCondition(){
        this.setState({ searchConditionShow: false})
    }

    /**
     * 検索条件を表示する
     */
    showSearchCondition(){
        this.setState({ searchConditionShow: true})
    }

    /**
     * 問い合わせステータスに対するラベルを取得
     * @param {Number} status ステータスコード
     */
    getAnswerStatusLabel(status){

        return Common.answerstatus[status];
     }

    /**
     * 問合せ画面へ遷移
     * @param {*} chatObj 問い合わせ情報
     */
    showChat(chatObj){
        this.state.viewState.changeAdminTabActive("applySearch");
        this.state.viewState.changeApplyPageActive("chat")
        this.state.viewState.moveToAdminChatView(chatObj,"applySearch", "applyList");
        this.state.viewState.setAdminBackPage("applySearch", "applyList");
    }

    /**
     * 一覧をCSV出力
     * @param {*} searchValue 
     */
    outPutCSVFile(searchValue){
        const searchResultCategory = this.state.searchResultCategory;
        const sortType = this.state.sortType;
        const sortColumn = this.state.sortColumn;
        const table = this.state.table;
        const searchResult = this.state.searchValue;
        if(searchResultCategory === "0"){

            var res = confirm("申請情報一覧をCSVで出力してよろしいでしょうか？");
            if(res == true){
                // 申請情報CSV出力
                let requestBody = this.setSearchRequestBody(searchResultCategory);
                fetch(Config.config.apiUrl + "/application/searchresult/output", {
                    method: 'POST',
                    body: JSON.stringify({
                        dataType: "application",
                        sortColumn: null,
                        sortType: null,
                        conditions: requestBody,
                        applicationSearchResults: searchResult
                    }),
                    headers: new Headers({ 'Content-type': 'application/json' }),
                })
                .then(response => {
                    if (!response.ok) {
                        throw new Error('ファイルの取得に失敗しました');
                    }
                    return response.blob(); // ファイルをBlobとして返す
                })
                .then(blob => {
                    //CSVファイルの出力
                    const now = new Date();
                    const filename = Config.config.applicationSearchResultFileName + "_" + now.toLocaleDateString();
                    let anchor = document.createElement("a");
                    anchor.href = window.URL.createObjectURL(blob);
                    anchor.download = filename;
                    anchor.click();
                })
                .catch(error => {
                    console.error('処理に失敗しました', error);
                });
            }
        }

        if(searchResultCategory === "1"){

            var res = confirm("問合せ情報一覧をCSVで出力してよろしいでしょうか？");
            if(res == true){
                // 問合せ情報CSV出力
                let requestBody = this.setSearchRequestBody(searchResultCategory);
                fetch(Config.config.apiUrl + "/application/searchresult/output", {
                    method: 'POST',
                    body: JSON.stringify({
                        dataType: "inquiry",
                        sortColumn: null,
                        sortType: null,
                        conditions: requestBody,
                        chatSearchResults: searchResult
                    }),
                    headers: new Headers({ 'Content-type': 'application/json' }),
                })
                .then(response => {
                    if (!response.ok) {
                        throw new Error('ファイルの取得に失敗しました');
                    }
                    return response.blob(); // ファイルをBlobとして返す
                })
                .then(blob => {
                    //CSVファイルの出力
                    const now = new Date();
                    const filename = Config.config.inquirySearchResultFileName + "_" + now.toLocaleDateString();
                    let anchor = document.createElement("a");
                    anchor.href = window.URL.createObjectURL(blob);
                    anchor.download = filename;
                    anchor.click();
                })
                .catch(error => {
                    console.error('処理に失敗しました', error);
                });
            }
        }

    }

    /**
     * 申請情報一覧のソート処理
     * @param {*} key 
     * @param {*} orderType 
     */
    sortApplicationList(key, orderType){

        const table = this.state.table;
        const searchValue = this.state.searchValue;

        let sortList =[];
        sortList= searchValue.sort((e1, e2) => {
           
            let text1 = e1.attributes[table[key].resonseKey].map(text => { return text }).filter(text => { return text !== null }).join(",");
            let text2 = e2.attributes[table[key].resonseKey].map(text => { return text }).filter(text => { return text !== null }).join(",");
            if (table[key].resonseKey == "status") {
                // ステータスの場合ステータスコードでソート
                text1 = e1.statusCode;
                text2 = e2.statusCode;
            }
            if (!isNaN(text1) && !isNaN(text2)) {
                text1 = Number(text1);
                text2 = Number(text2);
            } else if (this.isDate(text1) && this.isDate(text2)) {
                text1 = new Date(text1);
                text2 = new Date(text2);
            }
            if( orderType === "asc" ){

                if(text1 < text2){
                    return -1;
                }

                if(text1 > text2){
                    return 1;
                }

                return 0;
            }else{
                if(text1 < text2){
                    return 1;
                }

                if(text1 > text2){
                    return -1;
                }

                return 0;
            }
            
        })

        this.setState({sortColumn: key,sortType: orderType,searchValue: sortList});

    }
    /**
     * 日付型かどうかチェック
     * @param {*} value 
     * @returns 
     */
    isDate(value) {
        return !isNaN(new Date(value).getDate());
    }
    /**
     * ソートアイコンが選択されたか判断
     * @param {*} key 
     * @param {*} orderType 
     * @returns 
     */
    isSorted(key, orderType){
        const sortColumn = this.state.sortColumn;
        const sortType = this.state.sortType;

        if(sortColumn == key && sortType == orderType){
            return true;
        }else{
            return false;
        }

    }

    /**
     * 問合せ一覧をソート
     * @param {*} colum ソート対象カラム名称
     * @param {*} orderType 昇順/降順
     */
    sortChatMessageList(colum, orderType){

        const searchValue = this.state.searchValue;

        let sortList =[];
        sortList= searchValue.sort((e1, e2) => {

            let text1 = e1[colum];
            let text2 = e2[colum];
            if (!isNaN(text1) && !isNaN(text2)) {
                text1 = Number(text1);
                text2 = Number(text2);
            } else if (this.isDate(text1) && this.isDate(text2)) {
                text1 = new Date(text1);
                text2 = new Date(text2);
            }
            if( orderType === "asc" ){

                if(text1 < text2){
                    return -1;
                }

                if(text1 > text2){
                    return 1;
                }

                return 0;
            }else{
                if(text1 < text2){
                    return 1;
                }

                if(text1 > text2){
                    return -1;
                }

                return 0;
            }
            
        })

        this.setState({sortColumn: colum,sortType: orderType,searchValue: sortList});
    }


    render() {
        const searchCategory = this.state.searchCategory;
        const applicantInformation = this.state.applicantInformation;
        const selectedScreen = this.state.selectedScreen;
        const status = this.state.status;
        const answerStatus = this.state.answerStatus;
        const department = this.state.department;
        const answerName = this.state.answerName;
        const table = this.state.table;
        const searchValue = this.state.searchValue;
        const showApplyDetail = this.state.showApplyDetail;
        const searchResultCategory = this.state.searchResultCategory;

        const applicationType = this.state.applicationType;
        const applicationStep = this.state.applicationStep;
        const applicationAddInfo = this.state.applicationAddInfo;
        const applicationAddInfoDisplay = this.state.applicationAddInfoDisplay;
        const itemAnswerStatusDisplay = this.state.itemAnswerStatusDisplay;
        const itemAnswerStatus = this.state.itemAnswerStatus;
        // console.log(showApplyDetail);

        const sortColumn = this.state.sortColumn;
        const sortType = this.state.sortType;
        const applicationId = this.state.applicationId;
        return(

            <Box
                displayInlineBlock
                backgroundColor={this.props.theme.textLight}
                styledWidth={"98%"}
                styledHeight={"100%"}
                fullHeight
                style={{marginButtom: "10px"}}
            >
                { !showApplyDetail && 
                <>
                <div id="customloaderSearch" className={CustomStyle.customloaderParent}>
                    <div className={CustomStyle.customloader}>Loading...</div>
                </div>
                <nav className={CustomStyle.custom_nuv} id="searchFrameDrag">
                    申請情報検索
                </nav>
                <Box
                    centered
                    paddedHorizontally={5}
                    paddedVertically={10}
                    displayInlineBlock
                    className={CustomStyle.custom_content}
                >
                    <Spacing bottom={1} />
                    
                    <div style={{ width: "100%"}}>
                        { this.state.searchConditionShow && (
                        <>
                        <div className={CustomStyle.clear_left}></div>
                        {/* 検索項目に区分を追加する場合 */}
                        <div style={{marginBottom: "5px"}}>
                            <div><Text>■区分</Text></div>
                            <div className={CustomStyle.applicant_status_box}>
                                <div className={CustomStyle.applicant_status_box_div}>
                                    <Select
                                        light={true}
                                        dark={false}
                                        onChange={e => this.handleSelectSearchCategory(e.target.value)}
                                        style={{ color: "#000", width: "100%", minHeight: "28px"}}>
                                        {Object.keys(searchCategory).map(key => (
                                            <option key={"category" + key} value={key} selected={searchCategory[key]?.checked}>
                                                {searchCategory[key]?.text}
                                            </option>
                                        ))}
                                    </Select>
                                </div>
                                <div className={CustomStyle.applicant_status_box_div}>
                                    <div className={CustomStyle.applicant_status_box}>
                                        <div className={CustomStyle.applicant_type_box_div} style={{marginRight:"7px"}}>
                                            <Select
                                                light={true}
                                                dark={false}
                                                onChange={e => this.handleSelectApplicationType(e.target.value)}
                                                style={{ color: "#000", width: "100%", minHeight: "28px"}}>
                                                <option value={-1}></option>
                                                {Object.keys(applicationType).map(key => (
                                                    <option key={"applicationType" + key} value={key} selected={applicationType[key]?.checked}>
                                                        {applicationType[key]?.applicationTypeName}
                                                    </option>
                                                ))}
                                            </Select>
                                        </div>
                                        <div className={CustomStyle.applicant_type_box_div}>
                                            <Select
                                                light={true}
                                                dark={false}
                                                onChange={e => this.handleSelectapplicationStep(e.target.value)}
                                                style={{ color: "#000", width: "100%", minHeight: "28px"}}>
                                                <option value={-1}></option>
                                                {Object.keys(applicationStep).map(key => (
                                                    <option key={"applicationStep" + key} value={key} selected={applicationStep[key]?.checked}>
                                                        {applicationStep[key]?.applicationStepName}
                                                    </option>
                                                ))}
                                            </Select>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div style={{marginBottom: "5px"}}>
                            <div><Text>■申請者情報</Text></div>
                            {Object.keys(applicantInformation).map(key => (
                                <div className={CustomStyle.applicant_information_box}>
                                    <div className={CustomStyle.caption}>
                                        <Text>
                                            {!applicantInformation[key]?.requireFlag && (
                                                "("
                                            )}
                                            {applicantInformation[key]?.name}
                                            {!applicantInformation[key]?.requireFlag && (
                                                ")"
                                            )}</Text>
                                    </div>
                                    <div className={CustomStyle.input_text}>
                                        <Input
                                            light={true}
                                            dark={false}
                                            type="text"
                                            value={applicantInformation[key]?.value}
                                            placeholder=""
                                            id={applicantInformation[key]?.id}
                                            style={{ height: "28px", width: "100%" }}
                                            onChange={e => this.inputChange(key, e.target.value)}
                                        />
                                    </div>

                                </div>
                            ))}
                        </div>
                        <div className={CustomStyle.clear_left}></div>
                        <div className={CustomStyle.applicant_status_box} style={{marginBottom: "5px"}}>
                            <div className={CustomStyle.applicant_status_box_div}>
                                <div><Text>■ステータス</Text></div>
                                <Select
                                    id="statusSelect"
                                    light={true}
                                    dark={false}
                                    onChange={e => this.handleSelectStatus(e.target.value)}
                                    style={{ color: "#000", width: "100%", minHeight: "28px" }}>
                                    <option value={-1}></option>
                                    {this.state.searchCategory[0].checked && (
                                        Object.keys(status).map(key => (
                                            <option key={"status" + key} value={key} selected={status[key]?.checked}>
                                                {status[key]?.text}
                                            </option>
                                        ))
                                    )}
                                    {this.state.searchCategory[1].checked && (
                                        Object.keys(answerStatus).map(key => (
                                            <option key={"answerStatus" + key} value={key} selected={answerStatus[key]?.checked}>
                                                {answerStatus[key]?.text}
                                            </option>
                                        ))
                                    )}
                                </Select>
                            </div>
                            <div className={CustomStyle.applicant_status_box_div}>
                                <div><Text>■担当課</Text></div>
                                <Select
                                    light={true}
                                    dark={false}
                                    onChange={e => this.handleSelectDepartment(e.target.value)}
                                    style={{ color: "#000", width: "100%", minHeight: "28px" }}>
                                    <option value={-1}></option>
                                    {Object.keys(department).map(key => (
                                        <option key={"department" + key} value={key} selected={department[key]?.checked}>
                                            {department[key]?.departmentName}
                                        </option>
                                    ))}
                                </Select>
                            </div>
                            <div>
                                <div><Text>■回答者</Text></div>
                                <Select
                                    id="respondent"
                                    light={true}
                                    dark={false}
                                    onChange={e => this.handleSelectAnswerName(e.target.value)}
                                    style={{ color: "#000", width: "100%", minHeight: "28px" }}>
                                    <option value={-1}></option>
                                    {Object.keys(answerName).map(key => (
                                        <option key={"answerName" + key} value={key} selected={answerName[key]?.checked}>
                                            {`${answerName[key]?.departmentName}：${answerName[key]?.userName}`}
                                        </option>                                        
                                    ))}
                                </Select>
                            </div>
                            {itemAnswerStatusDisplay && (
                                <div>
                                    <div><Text>■条文ステータス</Text></div>
                                    {Object.keys(itemAnswerStatus).map(index => (
                                        <>
                                        <input type="checkbox" className={CustomStyle.custom_checkbox}
                                            onChange={ evt => { this.changeItemAnswerStatus(index)} }
                                            checked={itemAnswerStatus[index].checked}
                                        />
                                        {itemAnswerStatus[index].text}
                                        </>
                                    ))}
                                </div>
                            )}
                        </div>
                        <div className={CustomStyle.clear_left}></div>
                        <div className={CustomStyle.applicant_status_box} style={{marginBottom: "5px"}}>
                            <div className={CustomStyle.applicant_status_box_div}>
                                <div><Text>■申請ID</Text></div>
                                <Input
                                    light={true}
                                    dark={false}
                                    type="text"
                                    value={applicationId}
                                    placeholder=""
                                    id={applicationId}
                                    style={{ height: "28px", width: "100%" }}
                                    onChange={e => this.inputChangeApplicationId(e.target.value, applicationId)}
                                    />
                            </div>
                        </div>

                        <div className={CustomStyle.clear_left}></div>
                        <div><Text>■申請区分</Text></div>
                        {Object.keys(selectedScreen).map(index => (
                            // <div className={CustomStyle.box}>
                            <div className={CustomStyle.applicant_division_box}>
                                <div>
                                    <Text>条件 {Number(index) + 1}</Text>
                                </div>
                                <div style={{ display: "flex", justifyContent: "flex-start"}}>
                                    <div style={{ width: "55%", marginRight: "10px"}}>
                                        <Select
                                            light={true}
                                            dark={false}
                                            onChange={e => this.handleSelectScreen(index, e.target.value)}
                                            style={{ color: "#000", width: "100%", minHeight: "28px", marginBottom:"5px", }}>
                                            <option value={-1}></option>
                                            {Object.keys(selectedScreen[index]).map(key => (
                                                <option key={index + selectedScreen[index][key]?.screenId} value={key} selected={selectedScreen[index][key]?.checked}>
                                                    {selectedScreen[index][key]?.title}
                                                </option>
                                            ))}
                                        </Select>
                                    </div>
                                    <div style={{ width: "40%", marginRight: "10px"}}>
                                        <Select
                                            light={true}
                                            dark={false}
                                            onChange={e => this.handleSelectApplicationCategory(index, e.target.value)}
                                            style={{ color: "#000", width: "100%", minHeight: "28px", marginBottom:"5px"  }}>
                                            <option value={-1}></option>
                                            {Object.keys(selectedScreen[index]).map(key => (
                                                selectedScreen[index][key].checked && Object.keys(selectedScreen[index][key]["applicationCategory"]).map(categoryKey => (
                                                    <option key={index + selectedScreen[index][key]["applicationCategory"][categoryKey]?.id} value={categoryKey} selected={selectedScreen[index][key]["applicationCategory"][categoryKey]?.checked}>
                                                        {selectedScreen[index][key]["applicationCategory"][categoryKey]?.content}
                                                    </option>
                                                ))
                                            ))}
                                        </Select>
                                    </div>
                                </div>
                            </div>
                        ))}
                        { applicationAddInfoDisplay && (
                            <>
                                <div><Text>■申請追加情報</Text></div>
                                {Object.keys(applicationAddInfo).map(index => (
                                    <div className={CustomStyle.applicant_division_box}>
                                        <div>
                                            <Text>条件 {Number(index) + 1}</Text>
                                        </div>
                                        <div style={{ display: "flex", justifyContent: "flex-start"}}>
                                            <div style={{ width: "55%", marginRight: "10px"}}>
                                                <Select
                                                    key={index}
                                                    id={"add-cond-" + index}
                                                    light={true}
                                                    dark={false}
                                                    onChange={e => this.handleSelectApplicationAddInfo(index, e.target.value)}
                                                    style={{ color: "#000", width: "100%", minHeight: "28px", marginBottom:"5px", }}>
                                                    <option value={-1}></option>
                                                    {Object.keys(applicationAddInfo[index]).map(key => (
                                                        <option key={index + applicationAddInfo[index][key]?.id} value={key} selected={applicationAddInfo[index][key]?.checked}>
                                                            {applicationAddInfo[index][key]?.name}
                                                        </option>
                                                    ))}
                                                </Select>
                                            </div>
                                            <div style={{ width: "40%", marginRight: "10px"}}>
                                                { Object.keys(applicationAddInfo[index]).map(key => (
                                                    <>
                                                        {applicationAddInfo[index][key].checked && applicationAddInfo[index][key].itemType == "0" && (
                                                            <Input
                                                                light={true}
                                                                dark={false}
                                                                type="text"
                                                                value={applicationAddInfo[index][key]?.value}
                                                                placeholder={applicationAddInfo[index][key]?.name+ "を入力してください"}
                                                                id={index + `-` + applicationAddInfo[index][key]?.id}
                                                                style={{ height: "28px", width: "100%" }}
                                                                onChange={e => this.inputChangeApplicationAddInfoItem(index, key, e.target.value)}
                                                            />
                                                        )}

                                                        {applicationAddInfo[index][key].checked && applicationAddInfo[index][key].itemType == "1" && (
                                                            <textarea 
                                                                className={CustomStyle.input_text_area} 
                                                                type="text" 
                                                                value={applicationAddInfo[index][key]?.value}
                                                                placeholder={applicationAddInfo[index][key]?.name+ "を入力してください"}
                                                                id={index + `-` + applicationAddInfo[index][key]?.id}
                                                                style={{ height: "56px", width: "100%" }}
                                                                onChange={e => this.inputChangeApplicationAddInfoItem(index, key, e.target.value)}
                                                            />
                                                        )}

                                                        {applicationAddInfo[index][key].checked && applicationAddInfo[index][key].itemType == "2" && (
                                                            <Input
                                                                light={true}
                                                                dark={false}
                                                                type="date"
                                                                value={applicationAddInfo[index][key]?.value}
                                                                placeholder="年/月/日"
                                                                id={index + `-` + applicationAddInfo[index][key]?.id}
                                                                style={{ height: "28px", width: "100%" }}
                                                                onChange={e => this.changeApplicationAddInfoItemDate(index, key, e.target.value)}
                                                                max={"9999-12-31"} min={"2000-01-01"}
                                                            />
                                                        )}

                                                        {applicationAddInfo[index][key].checked && applicationAddInfo[index][key].itemType == "3" && (
                                                            <Input
                                                                light={true}
                                                                dark={false}
                                                                type="number"
                                                                value={applicationAddInfo[index][key]?.value}
                                                                placeholder={applicationAddInfo[index][key]?.name+ "を入力してください"}
                                                                id={index + `-` + applicationAddInfo[index][key]?.id}
                                                                style={{ height: "28px", width: "100%" }}
                                                                onChange={e => this.inputChangeApplicationAddInfoItem(index, key, e.target.value)}
                                                            />
                                                        )}

                                                        {applicationAddInfo[index][key].checked && applicationAddInfo[index][key].itemType == "4" && (
                                                           
                                                           <Select
                                                                light={true}
                                                                dark={false}
                                                                onChange={e => this.handleSelectApplicationAddInfoItem(index, key, e.target.value)}
                                                                style={{ color: "#000", width: "100%", minHeight: "28px", marginBottom:"5px"  }}>
                                                                <option value={-1}></option>
                                                                {
                                                                Object.keys(applicationAddInfo[index][key].itemOptions).map(itemKey => (
                                                                <option key={index + key + applicationAddInfo[index][key].itemOptions[itemKey]?.id} value={itemKey} selected={applicationAddInfo[index][key].itemOptions[itemKey]?.checked}>
                                                                    {applicationAddInfo[index][key].itemOptions[itemKey]?.content}
                                                                </option>))
                                                                }
                                                            </Select>
                                                        )}
                                                        {/* 複数選択 */}
                                                        {applicationAddInfo[index][key].checked && applicationAddInfo[index][key].itemType == "5" && (

                                                            <MultiSelect
                                                                className={CustomStyle.container}
                                                                classNamePrefix ="multi_select"
                                                                onChange={(value) => this.handleMultiSelectApplicationAddInfoItem(index, key, value)}
                                                                isClearable={false}
                                                                isSearchable={false}
                                                                isMulti={true}
                                                                components={{IndicatorSeparator: () => null , DropdownIndicator: DropdownIndicator}}
                                                                defaultValue={this.getMultiSelectDefaultValue(applicationAddInfo[index][key].itemOptions)}
                                                                options={this.getMultiSelectOptions(applicationAddInfo[index][key].itemOptions)}
                                                                placeholder = ""
                                                                menuPlacement={"auto"}                                                            >
                                                            </MultiSelect>
                                                        )}
                                                    </>
                                                ))} 
                                            </div>
                                        </div>
                                    </div>
                                ))}
                            </>
                        )}
                        <div className={CustomStyle.clear_left}></div>
                        <Spacing bottom={2} />
                        <div className={CustomStyle.search_button_box}>
                            <button
                                className={CustomStyle.clear_button}
                                onClick={e => {
                                    this.clear();
                                }}
                            >
                                <span>クリア</span>
                            </button>
                            <button
                                className={CustomStyle.search_button}
                                onClick={e => {
                                    this.search();
                                }}
                            >
                                <span>検索</span>
                            </button>
                        </div>
                        <div className={CustomStyle.clear_left}></div>

                        </>
                        )}
                        <Spacing bottom={2} />
                        { this.state.searchConditionShow && this.state.searcResultShow && (
                            <div style={{textAlign: "center"}}>
                                <button className={CustomStyle.show_search_condition} onClick={(e)=>this.hideSearchCondition()}>検索条件を隠す</button>
                            </div>
                        )}
                        { !this.state.searchConditionShow && (
                            <div style={{textAlign: "center"}}>
                                <button  className={CustomStyle.show_search_condition} onClick={(e)=>this.showSearchCondition()}>検索条件を表示する</button>
                            </div>
                        )}
                        <Spacing bottom={2} />

                        { this.state.searcResultShow && (
                        <div style={{ border: "1px solid #ccc", marginTop: "10", minHeight:"50%"}}>
                            {searchResultCategory === "0" && (
                                <div>
                                    <div style={{ padding: "5px"}}>
                                    <div className={CustomStyle.list_header_div}>
                                            <div style={{ fontSize: ".7em", paddingTop: "1.8em" }}>{`検索結果件数：${searchValue.length}件`}</div>
                                            <div>
                                                <button
                                                    className={CustomStyle.csv_button}
                                                    onClick={e => {
                                                        this.outPutCSVFile(searchValue);
                                                    }}
                                                >
                                                    <span>CSV出力</span>
                                                </button>
                                            </div>
                                        </div>
                                        <table className={CustomStyle.selection_table+" no-sort"}>
                                            <thead>
                                                <tr className={CustomStyle.table_header}>
                                                    {Object.keys(table).map(key => (
                                                        <th style={{ width: table[key].tableWidth + "%" }}>
                                                            <div style={{display:"flex",justifyContent: "space-between"}}>
                                                                <span>{table[key]?.displayColumnName}</span>
                                                                <div className={CustomStyle.sort_icon_position}>
                                                                    <button id={`sort_asc_${key}`} className={CustomStyle.sort_button}
                                                                        style={{display:"block"}}
                                                                        onClick={e => {this.sortApplicationList( key, "asc")}}
                                                                    >
                                                                        <StyledIcon 
                                                                            glyph={Icon.GLYPHS.opened}
                                                                            styledWidth={"12px"}
                                                                            styledHeight={"12px"}
                                                                            light
                                                                            rotation={180}
                                                                            className={`${this.isSorted(key,"asc" )? CustomStyle.sort_icon_checked :CustomStyle.sort_icon}`}
                                                                        />
                                                                    </button>
                                                                    <button id={`sort_desc_${key}`} className={CustomStyle.sort_button}
                                                                        style={{display:"block"}}
                                                                        onClick={e => {this.sortApplicationList( key, "desc")}}
                                                                    >
                                                                        <StyledIcon 
                                                                            glyph={Icon.GLYPHS.opened}
                                                                            styledWidth={"12px"}
                                                                            styledHeight={"12px"}
                                                                            light
                                                                            className={this.isSorted(key,"desc")? CustomStyle.sort_icon_checked :CustomStyle.sort_icon}
                                                                        />
                                                                    </button>
                                                                </div>
                                                            </div>
                                                        </th>
                                                    ))}
                                                    <th style={{ width: "50px" }}></th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                {Object.keys(searchValue).map(searchKey => (
                                                    <tr className={CustomStyle.tr_button} key={searchKey} onClick={() => {
                                                        this.showLayers(searchValue[searchKey]);
                                                    }}>
                                                        {Object.keys(table).map(tableKey => (
                                                            <td style={{ width: table[tableKey].tableWidth + "%" }}>
                                                                {searchValue[searchKey]?.attributes?.[table[tableKey].resonseKey].map(text => { return text }).filter(text => { return text !== null }).join(",")}
                                                            </td>
                                                        ))}
                                                        <td>
                                                            <button
                                                                className={CustomStyle.detail_button}
                                                                onClick={evt => {
                                                                    evt.preventDefault();
                                                                    evt.stopPropagation();
                                                                    this.details(searchValue[searchKey]?.applicationId);
                                                                }}
                                                            >
                                                                <span>詳細</span>
                                                            </button>
                                                        </td>
                                                    </tr>
                                                ))}                                                
                                            </tbody>
                                        </table>
                                    </div>
                                </div>
                            )}

                            {searchResultCategory === "1"  && (
                                <div style={{ border: "none", margin: "0 5px"}}>
                                    <div className={CustomStyle.list_header_div}>
                                        <div style={{ fontSize: ".7em", paddingTop: "1.8em" }}>{`検索結果件数：${searchValue.length}件`}</div>
                                        <div>
                                            <button className={CustomStyle.csv_button}
                                                onClick={e => {
                                                    this.outPutCSVFile(searchValue);
                                                }}
                                            >
                                                <span>CSV出力</span>
                                            </button>
                                        </div>
                                    </div>
                                    
                                    <div className={CustomStyle.table_scroll}>
                                        <table className={CustomStyle.selection_table + " no-sort"}>
                                            <thead>
                                                <tr className={CustomStyle.table_header}>
                                                    <th style={{ width: "60px" }} className={CustomStyle.fixedCol}></th>
                                                    <th style={{ width: "105px" }}>
                                                        <div style={{display:"flex", justifyContent:"space-between"}}>
                                                            <div className={CustomStyle.table_header_label}>
                                                                <span>ステータス</span>
                                                            </div>
                                                            <div className={CustomStyle.sort_icon_position}>
                                                                <button id="sort_asc_status" className={CustomStyle.sort_button}
                                                                    style={{display:"block"}}
                                                                    onClick={e => {this.sortChatMessageList( "status", "asc")}}
                                                                >
                                                                    <StyledIcon 
                                                                        glyph={Icon.GLYPHS.opened}
                                                                        styledWidth={"12px"}
                                                                        styledHeight={"12px"}
                                                                        light
                                                                        rotation={180}
                                                                        className={`${this.isSorted("status","asc" )? CustomStyle.sort_icon_checked :CustomStyle.sort_icon}`}
                                                                    />
                                                                </button>
                                                                <button id="sort_desc_status" className={CustomStyle.sort_button}
                                                                    style={{display:"block"}}
                                                                    onClick={e => {this.sortChatMessageList( "status", "desc")}}
                                                                >
                                                                    <StyledIcon 
                                                                        glyph={Icon.GLYPHS.opened}
                                                                        styledWidth={"12px"}
                                                                        styledHeight={"12px"}
                                                                        light
                                                                        className={this.isSorted("status","desc")? CustomStyle.sort_icon_checked :CustomStyle.sort_icon}
                                                                    />
                                                                </button>
                                                            </div>
                                                        </div>
                                                    </th>
                                                    <th style={{ width: "85px" }}>
                                                        <div style={{display:"flex", justifyContent:"space-between"}}>
                                                            <div className={CustomStyle.table_header_label}>
                                                                <span>申請ID</span>
                                                            </div>
                                                            <div className={CustomStyle.sort_icon_position}>
                                                                <button id="sort_asc_applicationId" className={CustomStyle.sort_button}
                                                                    style={{display:"block"}}
                                                                    onClick={e => {this.sortChatMessageList( "applicationId", "asc")}}
                                                                >
                                                                    <StyledIcon 
                                                                        glyph={Icon.GLYPHS.opened}
                                                                        styledWidth={"12px"}
                                                                        styledHeight={"12px"}
                                                                        light
                                                                        rotation={180}
                                                                        className={`${this.isSorted("applicationId","asc" )? CustomStyle.sort_icon_checked :CustomStyle.sort_icon}`}
                                                                    />
                                                                </button>
                                                                <button id="sort_desc_applicationId" className={CustomStyle.sort_button}
                                                                    style={{display:"block"}}
                                                                    onClick={e => {this.sortChatMessageList( "applicationId", "desc")}}
                                                                >
                                                                    <StyledIcon 
                                                                        glyph={Icon.GLYPHS.opened}
                                                                        styledWidth={"12px"}
                                                                        styledHeight={"12px"}
                                                                        light
                                                                        className={this.isSorted("applicationId","desc")? CustomStyle.sort_icon_checked :CustomStyle.sort_icon}
                                                                    />
                                                                </button>
                                                            </div>
                                                        </div>
                                                    </th>
                                                    <th style={{ width: "200px" }}>
                                                        <div style={{display:"flex", justifyContent:"space-between"}}>
                                                            <div className={CustomStyle.table_header_label}>
                                                                <span>対象</span>
                                                            </div>
                                                            <div className={CustomStyle.sort_icon_position}>
                                                                <button id="sort_asc_categoryJudgementTitle" className={CustomStyle.sort_button}
                                                                    style={{display:"block"}}
                                                                    onClick={e => {this.sortChatMessageList( "categoryJudgementTitle", "asc")}}
                                                                >
                                                                    <StyledIcon 
                                                                        glyph={Icon.GLYPHS.opened}
                                                                        styledWidth={"12px"}
                                                                        styledHeight={"12px"}
                                                                        light
                                                                        rotation={180}
                                                                        className={`${this.isSorted("categoryJudgementTitle","asc" )? CustomStyle.sort_icon_checked :CustomStyle.sort_icon}`}
                                                                    />
                                                                </button>
                                                                <button id="sort_desc_categoryJudgementTitle" className={CustomStyle.sort_button}
                                                                    style={{display:"block"}}
                                                                    onClick={e => {this.sortChatMessageList( "categoryJudgementTitle", "desc")}}
                                                                >
                                                                    <StyledIcon 
                                                                        glyph={Icon.GLYPHS.opened}
                                                                        styledWidth={"12px"}
                                                                        styledHeight={"12px"}
                                                                        light
                                                                        className={this.isSorted("categoryJudgementTitle","desc")? CustomStyle.sort_icon_checked :CustomStyle.sort_icon}
                                                                    />
                                                                </button>
                                                            </div>
                                                        </div>
                                                    </th>
                                                    <th style={{ width: "120px" }}>
                                                        <div style={{display:"flex", justifyContent:"space-between"}}>
                                                            <div className={CustomStyle.table_header_label}>
                                                                <span>回答担当課</span>
                                                            </div>
                                                            <div className={CustomStyle.sort_icon_position}>
                                                                <button id="sort_asc_departmentName" className={CustomStyle.sort_button}
                                                                    style={{display:"block"}}
                                                                    onClick={e => {this.sortChatMessageList( "departmentName", "asc")}}
                                                                >
                                                                    <StyledIcon 
                                                                        glyph={Icon.GLYPHS.opened}
                                                                        styledWidth={"12px"}
                                                                        styledHeight={"12px"}
                                                                        light
                                                                        rotation={180}
                                                                        className={`${this.isSorted("departmentName","asc" )? CustomStyle.sort_icon_checked :CustomStyle.sort_icon}`}
                                                                    />
                                                                </button>
                                                                <button id="sort_desc_departmentName" className={CustomStyle.sort_button}
                                                                    style={{display:"block"}}
                                                                    onClick={e => {this.sortChatMessageList( "departmentName", "desc")}}
                                                                >
                                                                    <StyledIcon 
                                                                        glyph={Icon.GLYPHS.opened}
                                                                        styledWidth={"12px"}
                                                                        styledHeight={"12px"}
                                                                        light
                                                                        className={this.isSorted("departmentName","desc")? CustomStyle.sort_icon_checked :CustomStyle.sort_icon}
                                                                    />
                                                                </button>
                                                            </div>
                                                        </div>
                                                    </th>
                                                    <th style={{ width: "120px" }}>
                                                        <div style={{display:"flex", justifyContent:"space-between"}}>
                                                            <div className={CustomStyle.table_header_label}>
                                                                <span>初回投稿日時</span>
                                                            </div>
                                                            <div className={CustomStyle.sort_icon_position}>
                                                                <button id="sort_asc_establishmentFirstPostDatetime" className={CustomStyle.sort_button}
                                                                    style={{display:"block"}}
                                                                    onClick={e => {this.sortChatMessageList( "establishmentFirstPostDatetime", "asc")}}
                                                                >
                                                                    <StyledIcon 
                                                                        glyph={Icon.GLYPHS.opened}
                                                                        styledWidth={"12px"}
                                                                        styledHeight={"12px"}
                                                                        light
                                                                        rotation={180}
                                                                        className={`${this.isSorted("establishmentFirstPostDatetime","asc" )? CustomStyle.sort_icon_checked :CustomStyle.sort_icon}`}
                                                                    />
                                                                </button>
                                                                <button id="sort_desc_establishmentFirstPostDatetime" className={CustomStyle.sort_button}
                                                                    style={{display:"block"}}
                                                                    onClick={e => {this.sortChatMessageList( "establishmentFirstPostDatetime", "desc")}}
                                                                >
                                                                    <StyledIcon 
                                                                        glyph={Icon.GLYPHS.opened}
                                                                        styledWidth={"12px"}
                                                                        styledHeight={"12px"}
                                                                        light
                                                                        className={this.isSorted("establishmentFirstPostDatetime","desc")? CustomStyle.sort_icon_checked :CustomStyle.sort_icon}
                                                                    />
                                                                </button>
                                                            </div>
                                                        </div>
                                                    </th>
                                                    <th style={{ width: "120px" }}>
                                                        <div style={{display:"flex", justifyContent:"space-between"}}>
                                                            <div className={CustomStyle.table_header_label}>
                                                                <span>最新投稿日時</span>
                                                            </div>
                                                            <div className={CustomStyle.sort_icon_position}>
                                                                <button id="sort_asc_sendDatetime" className={CustomStyle.sort_button}
                                                                    style={{display:"block"}}
                                                                    onClick={e => {this.sortChatMessageList( "sendDatetime", "asc")}}
                                                                >
                                                                    <StyledIcon 
                                                                        glyph={Icon.GLYPHS.opened}
                                                                        styledWidth={"12px"}
                                                                        styledHeight={"12px"}
                                                                        light
                                                                        rotation={180}
                                                                        className={`${this.isSorted("sendDatetime","asc" )? CustomStyle.sort_icon_checked :CustomStyle.sort_icon}`}
                                                                    />
                                                                </button>
                                                                <button id="sort_desc_sendDatetime" className={CustomStyle.sort_button}
                                                                    style={{display:"block"}}
                                                                    onClick={e => {this.sortChatMessageList( "sendDatetime", "desc")}}
                                                                >
                                                                    <StyledIcon 
                                                                        glyph={Icon.GLYPHS.opened}
                                                                        styledWidth={"12px"}
                                                                        styledHeight={"12px"}
                                                                        light
                                                                        className={this.isSorted("sendDatetime","desc")? CustomStyle.sort_icon_checked :CustomStyle.sort_icon}
                                                                    />
                                                                </button>
                                                            </div>
                                                        </div>
                                                    </th>
                                                    <th style={{ width: "120px" }}>
                                                        <div style={{display:"flex", justifyContent:"space-between"}}>
                                                            <div className={CustomStyle.table_header_label}>
                                                                <span>最新回答者</span>
                                                            </div>
                                                            <div className={CustomStyle.sort_icon_position}>
                                                                <button id="sort_asc_answerUserName" className={CustomStyle.sort_button}
                                                                    style={{display:"block"}}
                                                                    onClick={e => {this.sortChatMessageList( "answerUserName", "asc")}}
                                                                >
                                                                    <StyledIcon 
                                                                        glyph={Icon.GLYPHS.opened}
                                                                        styledWidth={"12px"}
                                                                        styledHeight={"12px"}
                                                                        light
                                                                        rotation={180}
                                                                        className={`${this.isSorted("answerUserName","asc" )? CustomStyle.sort_icon_checked :CustomStyle.sort_icon}`}
                                                                    />
                                                                </button>
                                                                <button id="sort_desc_answerUserName" className={CustomStyle.sort_button}
                                                                    style={{display:"block"}}
                                                                    onClick={e => {this.sortChatMessageList( "answerUserName", "desc")}}
                                                                >
                                                                    <StyledIcon 
                                                                        glyph={Icon.GLYPHS.opened}
                                                                        styledWidth={"12px"}
                                                                        styledHeight={"12px"}
                                                                        light
                                                                        className={this.isSorted("answerUserName","desc")? CustomStyle.sort_icon_checked :CustomStyle.sort_icon}
                                                                    />
                                                                </button>
                                                            </div>
                                                        </div>
                                                    </th>
                                                    <th style={{ width: "120px" }}>
                                                        <div style={{display:"flex", justifyContent:"space-between"}}>
                                                            <div className={CustomStyle.table_header_label}>
                                                                <span>最新回答日時</span>
                                                            </div> 
                                                            <div className={CustomStyle.sort_icon_position}>
                                                                <button id="sort_asc_answerDatetime" className={CustomStyle.sort_button}
                                                                    style={{display:"block"}}
                                                                    onClick={e => {this.sortChatMessageList( "answerDatetime", "asc")}}
                                                                >
                                                                    <StyledIcon 
                                                                        glyph={Icon.GLYPHS.opened}
                                                                        styledWidth={"12px"}
                                                                        styledHeight={"12px"}
                                                                        light
                                                                        rotation={180}
                                                                        className={`${this.isSorted("answerDatetime","asc" )? CustomStyle.sort_icon_checked :CustomStyle.sort_icon}`}
                                                                    />
                                                                </button>
                                                                <button id="sort_desc_answerDatetime" className={CustomStyle.sort_button}
                                                                    style={{display:"block"}}
                                                                    onClick={e => {this.sortChatMessageList( "answerDatetime", "desc")}}
                                                                >
                                                                    <StyledIcon 
                                                                        glyph={Icon.GLYPHS.opened}
                                                                        styledWidth={"12px"}
                                                                        styledHeight={"12px"}
                                                                        light
                                                                        className={this.isSorted("answerDatetime","desc")? CustomStyle.sort_icon_checked :CustomStyle.sort_icon}
                                                                    />
                                                                </button>
                                                            </div>
                                                        </div>
                                                    </th>
                                                    
                                                </tr>
                                            </thead>
                                            <tbody>
                                                {searchValue && Object.keys(searchValue).map(searchKey => (
                                                    <tr className={CustomStyle.tr_button} key={searchKey} onClick={() => {
                                                        this.showLayers(searchValue[searchKey]);
                                                    }}>
                                                        <td className={CustomStyle.fixedCol}>
                                                            <button className={CustomStyle.detail_button} 
                                                            onClick={e => { this.showChat(searchValue[searchKey]); }}>
                                                                <span>詳細</span>
                                                            </button> 
                                                        </td>
                                                        <td>{this.getAnswerStatusLabel(searchValue[searchKey].status)}</td>
                                                        <td>{searchValue[searchKey].applicationId}</td>
                                                        <td>{searchValue[searchKey].categoryJudgementTitle}</td>
                                                        <td>{searchValue[searchKey].departmentName}</td>
                                                        <td>{searchValue[searchKey].establishmentFirstPostDatetime}</td>
                                                        <td>{searchValue[searchKey].sendDatetime}</td>
                                                        <td>{searchValue[searchKey].answerUserName == "" ? "ー" : searchValue[searchKey].answerUserName}</td>
                                                        <td>{searchValue[searchKey].answerDatetime == "" ? "ー" : searchValue[searchKey].answerDatetime}</td>
                                                    </tr>
                                                ))} 
                                            </tbody>
                                        </table>
                                    </div>
                                </div>
                            )}
                        </div>
                        )}
                    </div>
                </Box>
                </>
            }

            { showApplyDetail && 
                <ApplicationDetails
                    terria={this.props.terria}
                    viewState={this.props.viewState}
                ></ApplicationDetails>
            }
            </Box >
            );
        };
    }
export default withTranslation()(withTheme(ApplicationInformationSearch));