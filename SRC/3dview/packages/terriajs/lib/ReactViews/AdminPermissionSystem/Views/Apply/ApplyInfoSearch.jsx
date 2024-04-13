import { observer } from "mobx-react";
import React from "react";
import PropTypes from "prop-types";
import { withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import CustomStyle from "./scss/apply-info-search.scss";
import Icon, { StyledIcon } from "../../../../Styled/Icon";
import Box from "../../../../Styled/Box";
import Spacing from "../../../../Styled/Spacing";
import Text from "../../../../Styled/Text";
import Input from "../../../../Styled/Input";
import Button, { RawButton } from "../../../../Styled/Button";

/**
 * 申請情報検索画面
 */
@observer
class ApplyInfoSearch extends React.Component {
    static propTypes = {
        terria: PropTypes.object.isRequired,
        viewState: PropTypes.object.isRequired,
        theme: PropTypes.object,
        t: PropTypes.func.isRequired
    }

    constructor(props){
        super(props);
        this.state = {
            viewState: props.viewState,
            terria: props.terria,
        }
    }

    render(){
        const applicantInformation = this.state.applicantInformation;
        const selectedScreen = this.state.selectedScreen;
        const status = this.state.status;
        const department = this.state.department;
        const table = this.state.table;
        const searchValue = this.state.searchValue;
        return (
                <Box
                    centered
                    paddedHorizontally={5}
                    paddedVertically={10}
                    displayInlineBlock
                    className={CustomStyle.custom_content}
                >
                    <Spacing bottom={1} />
                    <button
                        className={CustomStyle.close_button}
                        onClick={e => {
                            this.close();
                        }}
                    >
                        <span>戻る</span>
                    </button>
                </Box>
        );
    }

}
export default withTranslation()(withTheme(ApplyInfoSearch));