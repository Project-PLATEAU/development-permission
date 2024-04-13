import React from "react";
import PropTypes from "prop-types";
import { observer } from "mobx-react";
import { withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import Box from "../../../../Styled/Box";
import Styles from "../../../DevelopmentPermissionSystem/PageViews/scss/pageStyle.scss";
import { TextSpan } from "../../../../Styled/Text";

/**
 * 行政用コンポーネント：メッセージ表示
 */

@observer
class ShowMessage extends React.Component {
    static displayName = "ShowMessage"
    static propsType = {
        t: PropTypes.func.isRequired,
        message: PropTypes.string.isRequired
    }

    constructor(props) {
        super(props);
        this.state = {
            message: props.message
        }
    }

    render(){
        const message = this.props.t(`${this.props.message}`);
        return(
            <Box padded  className={Styles.text_area}>
                <TextSpan textDark uppercase overflowHide overflowEllipsis>{message}</TextSpan>
            </Box>
        )
    }
}
export default withTranslation()(withTheme(ShowMessage));