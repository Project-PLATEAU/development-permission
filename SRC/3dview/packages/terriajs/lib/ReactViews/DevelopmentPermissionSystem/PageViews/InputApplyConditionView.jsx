import { observer } from "mobx-react";
import PropTypes from "prop-types";
import React from "react";
import {  withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import { TextSpan } from "../../../Styled/Text";
import Box from "../../../Styled/Box";
import Styles from "./scss/pageStyle.scss";
import ApplicationCategorySelection from "../Views/Apply/ApplicationCategorySelection.jsx";
import CommonStrata from "../../../Models/Definition/CommonStrata";
import webMapServiceCatalogItem from '../../../Models/Catalog/Ows/WebMapServiceCatalogItem';
import Config from "../../../../customconfig.json";


/**
 * 事業者：申請区分選択画面
 */
@observer
class InputApplyConditionView extends React.Component {
    static displayName = "InputApplyConditionView";
    static propTypes = {
        terria: PropTypes.object.isRequired,
        viewState: PropTypes.object.isRequired,
        applicationPlace: PropTypes.object,
        theme: PropTypes.object,
        t: PropTypes.func.isRequired
    }

    constructor(props) {
        super(props);
        this.state = {
            viewState: props.viewState,
            terria: props.terria,
        };
    }

    /**
     * 初期処理
     */
    componentDidMount() {
        // 再申請か否か
        let isReApply = this.props.viewState.isReApply;
        if (!isReApply) {
            // 前画面で選択された申請地を地図上に表示する
            let applicationPlace = Object.values(this.props.viewState.applicationPlace);
            applicationPlace = applicationPlace.filter(Boolean);
            this.props.viewState.setLotNumbers(applicationPlace);
            try{
                const item = new webMapServiceCatalogItem(Config.layer.lotnumberSearchLayerNameForApplicationTarget, this.state.terria);
                const wmsUrl = Config.config.geoserverUrl;
                const items = this.state.terria.workbench.items;

                for (const aItem of items) {
                    if (aItem.uniqueId === Config.layer.lotnumberSearchLayerNameForApplicationTarget || aItem.uniqueId === Config.layer.lotnumberSearchLayerNameForBusiness) {
                        this.state.terria.workbench.remove(aItem);
                        aItem.loadMapItems();
                    }
                }
                item.setTrait(CommonStrata.definition, "url", wmsUrl);
                item.setTrait(CommonStrata.user, "name", Config.layer.lotnumberSearchLayerDisplayNameForApplicationTarget);
                item.setTrait(
                    CommonStrata.user,
                    "layers",
                    Config.layer.lotnumberSearchLayerNameForApplicationTarget);
                item.setTrait(CommonStrata.user,
                    "parameters",
                    {
                        "viewparams": Config.layer.lotnumberSearchViewParamNameForApplicationTarget + Object.keys(this.props.viewState.applicationPlace)?.map(key => { return this.props.viewState.applicationPlace[key].chibanId }).filter(chibanId => { return chibanId !== null }).join("_"),
                    });
                item.loadMapItems();
                this.state.terria.workbench.add(item);
            }catch(error){
                console.error('処理に失敗しました', error);
            }
        }
    }

    render() {
        const t = this.props.t;
        return (
                <Box column fullHeight style={{overflowY:"auto" , overflowX: "hidden"}}>
                    <div className={Styles.div_area} style={{height:"100%"}}>
                        <Box padded  className={Styles.text_area}>
                            <TextSpan textDark uppercase overflowHide overflowEllipsis>
                                {t("infoMessage.tipsForSelectApplicationCategory")} 
                            </TextSpan>
                        </Box>

                        <ApplicationCategorySelection terria={this.props.terria} viewState={this.props.viewState} />

                    </div>
                </Box>
        );
    };
}

export default withTranslation()(withTheme(InputApplyConditionView));