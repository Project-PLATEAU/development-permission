import { observer } from "mobx-react";
import PropTypes from "prop-types";
import React from "react";
import { withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import Icon, { StyledIcon } from "../../../../Styled/Icon";
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
            //回答状態(検索値)
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
                    this.setState({
                        applicantInformation: res.applicantInformationItemForm,
                        screen: res.applicationCategories,
                        status: res.status,
                        answerStatus: res.answerStatus,
                        department: res.department,
                        answerName: res.answerName,
                        selectedScreen: selectedScreen
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

        this.setState({ status: status, department: department, selectedScreen: selectedScreen, applicantInformation: applicantInformation });

    }
    
    /**
     * 検索
     */
    search() {
        this.setState({
            searchConditionShow: false,
            searcResultShow: true,
        });

        //document.getElementById("searchFrame").scrollTop = 0;
        document.getElementById("customloaderSearch").style.display = "block";
        
        const selectedScreen = this.state.selectedScreen;
        let resultScreen = new Array();
        
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

        // 区分
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

        // console.log(resultScreen);
        // console.log(resultStatus);
        // console.log(resultDepartment);
        // console.log(resultApplicantInformation);
        // console.log(resultSearchCategory);
        // console.log(resultAnswerStatus);
        // console.log(resultAnswerName);

        // 申請情報検索
        if(searchResultCategory === "0"){
            fetch(Config.config.apiUrl + "/application/search", {
                method: 'POST',
                body: JSON.stringify({
                    applicantInformationItemForm: resultApplicantInformation,
                    applicationCategories: resultScreen,
                    status: resultStatus,
                    department: resultDepartment,
                    searchCategory : resultSearchCategory,
                    answerName: resultAnswerName
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
                // console.log(res);
                if (Object.keys(res).length > 0 && !res.status) {
                    this.setState({
                        searcConditionShow: false,
                        searcResultShow: true,
                        searchValue: res,
                        searchConditionShow: false,
                        searcResultShow: true,
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
                body: JSON.stringify({
                    applicantInformationItemForm: resultApplicantInformation,
                    applicationCategories: resultScreen,
                    answerStatus: resultAnswerStatus,
                    department: resultDepartment,
                    searchCategory : resultSearchCategory,
                    answerName: resultAnswerName
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
                // console.log(res);
                if (Object.keys(res).length > 0 && !res.status) {
                    this.setState({
                        searchConditionShow: false,
                        searcResultShow: true,
                        searchValue: res
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
     * @param {object} lotNumbers 申請地情報
     */
    showLayers(lotNumbers) {
        console.log("地番情報");
        console.log(lotNumbers);
        try{
            const wmsUrl = Config.config.geoserverUrl;
            const items = this.state.terria.workbench.items;
            let layerFlg = false;
            for (const aItem of items) {
                if (aItem.uniqueId === Config.layer.lotnumberSearchLayerNameForApplicationSearchTarget) {
                    aItem.setTrait(CommonStrata.user,
                        "parameters",
                        {
                            "viewparams": Config.layer.lotnumberSearchViewParamNameForApplicationSearchTarget + Object.keys(lotNumbers)?.map(key => { return lotNumbers[key].chibanId }).filter(chibanId => { return chibanId !== null }).join("_"),
                        });
                    aItem.loadMapItems();
                    layerFlg = true;
                }
            }

            this.focusMapPlaceDriver(lotNumbers);

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
                        "viewparams": Config.layer.lotnumberSearchViewParamNameForApplicationSearchTarget + Object.keys(lotNumbers)?.map(key => { return lotNumbers[key].chibanId }).filter(chibanId => { return chibanId !== null }).join("_"),
                    });
                item.loadMapItems();
                this.state.terria.workbench.add(item);
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
        // 3dmodeにセット
        this.props.viewState.set3dMode();
        //現在のカメラ位置等を取得
        const currentSettings = getShareData(this.state.terria, this.props.viewState);
        const currentCamera = currentSettings.initSources[0].initialCamera;
        let newCamera = Object.assign(currentCamera);
        //新規の表示範囲を設定
        let currentLonDiff = Math.abs(maxLon - minLon);
        let currentLatDiff = Math.abs(maxLat - minLat);
        newCamera.north = maxLon + currentLatDiff / 2;
        newCamera.south = minLon - currentLatDiff / 2;
        newCamera.east = maxLat + currentLonDiff / 2;
        newCamera.west = minLat - currentLonDiff / 2;
        //camera.positionを緯度経度に合わせて設定
        const scene = this.props.terria.cesium.scene;
        const terrainProvider = scene.terrainProvider;
        const positions = [Cartographic.fromDegrees(lon, minLat)];
        let height = 0;
        sampleTerrainMostDetailed(terrainProvider, positions).then((updatedPositions) => {
            height = updatedPositions[0].height
            let coord_wgs84 = Cartographic.fromDegrees(lon, minLat, parseFloat(height) + parseInt((400000 * currentLatDiff )) + 200 );
            let coord_xyz = Ellipsoid.WGS84.cartographicToCartesian(coord_wgs84);
            newCamera.position = { x: coord_xyz.x, y: coord_xyz.y, z: coord_xyz.z - parseInt((300000 * currentLatDiff )) - 170 };
            //カメラの向きは統一にさせる
            newCamera.direction = { x: this.props.terria.focusCameraDirectionX, y: this.props.terria.focusCameraDirectionY, z: this.props.terria.focusCameraDirectionZ };
            newCamera.up = { x: this.props.terria.focusCameraUpX, y: this.props.terria.focusCameraUpY, z:this.props.terria.focusCameraUpZ };
            this.state.terria.currentViewer.zoomTo(newCamera, 5);
        })
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
        console.log(showApplyDetail);
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
                    {/* <button
                        className={CustomStyle.close_button}
                        onClick={e => {
                            this.close();
                        }}
                    >
                        <span>戻る</span>
                    </button>
                    <Spacing bottom={2} /> */}
                    
                    <div style={{ width: "100%"}}>
                        { this.state.searchConditionShow && (
                        <>
                        <div className={CustomStyle.clear_left}></div>
                        {/* 検索項目に区分を追加する場合 */}
                        <div className={CustomStyle.applicant_status_box} style={{marginBottom: "5px"}}>
                            <div className={CustomStyle.applicant_status_box_div}>
                                <div><Text>■区分</Text></div>
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
                            <div className={CustomStyle.applicant_status_box_div}></div>
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
                        <div className={CustomStyle.clear_left}></div>
                        <Spacing bottom={2} />
                        <div className={CustomStyle.search_button_box}>
                            {/* <div style={{ fontSize: ".7em", paddingTop: "1.8em" }}>検索結果件数：{Number(Object.keys(searchValue).length).toLocaleString()} 件</div> */}
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
                                {/* <button style={{ width:"75%", height:"36px", backgroundColor: "#ff9900", color:"#fff", margin:"0 auto", padding:"10px 20px", border: "none", borderRadius:"10px", fontWeight:"600"}} onClick={(e)=>this.hideSearchCondition()}>検索条件を隠す</button> */}
                                <button className={CustomStyle.show_search_condition} onClick={(e)=>this.hideSearchCondition()}>検索条件を隠す</button>
                            </div>
                        )}
                        { !this.state.searchConditionShow && (
                            <div style={{textAlign: "center"}}>
                                {/* <button style={{ width:"75%", height:"36px", backgroundColor: "#ff9900", color:"#fff", margin:"0 auto", padding:"10px 20px", border: "none", borderRadius:"10px", fontWeight:"600"}} onClick={(e)=>this.showSearchCondition()}>検索条件を表示する</button> */}
                                <button  className={CustomStyle.show_search_condition} onClick={(e)=>this.showSearchCondition()}>検索条件を表示する</button>
                            </div>
                        )}
                        <Spacing bottom={2} />

                        { this.state.searcResultShow && (
                        <div style={{ border: "1px solid #ccc", marginTop: "10", minHeight:"50%"}}>
                            {searchResultCategory === "0" && (
                                <div>
                                    <div style={{ padding: "5px"}}>
                                        <div style={{ fontSize: ".7em", paddingTop: "1.8em" }}>{`検索結果件数：${searchValue.length}件`}</div>
                                        <table className={CustomStyle.selection_table}>
                                            <thead>
                                                <tr className={CustomStyle.table_header}>
                                                    {Object.keys(table).map(key => (
                                                        <th style={{ width: table[key].tableWidth + "%" }}>
                                                            {table[key]?.displayColumnName}
                                                        </th>
                                                    ))}
                                                    <th style={{ width: "50px" }}></th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                {Object.keys(searchValue).map(searchKey => (
                                                    <tr className={CustomStyle.tr_button} key={searchKey} onClick={() => {
                                                        this.showLayers(searchValue[searchKey]?.lotNumbers);
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
                                    <div style={{ fontSize: ".7em", paddingTop: "1.8em" }}>{`検索結果件数：${searchValue.length}件`}</div>
                                    {/* <div style={{ padding: "5px"}} className={CustomStyle.table_scroll}> */}
                                    <div className={CustomStyle.table_scroll}>
                                        <table className={CustomStyle.selection_table}>
                                            <thead>
                                                <tr className={CustomStyle.table_header}>
                                                    <th style={{ width: "60px" }} className={CustomStyle.fixedCol}></th>
                                                    <th style={{ width: "80px" }}>ステータス</th>
                                                    <th style={{ width: "60px" }}>申請ID</th>
                                                    <th style={{ width: "200px" }}>対象</th>
                                                    <th style={{ width: "120px" }}>回答担当課</th>
                                                    <th style={{ width: "120px" }}>初回投稿日時	</th>
                                                    <th style={{ width: "120px" }}>最新投稿日時</th>
                                                    <th style={{ width: "120px" }}>最新回答者</th>
                                                    <th style={{ width: "120px" }}>最新回答日時</th>
                                                    
                                                </tr>
                                            </thead>
                                            <tbody>
                                                {searchValue && Object.keys(searchValue).map(searchKey => (
                                                    <tr className={CustomStyle.tr_button} key={searchKey} onClick={() => {
                                                        this.showLayers(searchValue[searchKey]?.lotNumbers);
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