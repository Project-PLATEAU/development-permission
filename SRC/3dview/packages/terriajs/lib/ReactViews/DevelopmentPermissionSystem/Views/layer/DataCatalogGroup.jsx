import React from "react";
import PropTypes from "prop-types";
import { observer } from "mobx-react";
import { withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import styled from "styled-components";
import classNames from "classnames";
import getPath from "../../../../Core/getPath";
import Box from "../../../../Styled/Box";
import Text from "../../../../Styled/Text";
import Styles from "./scss/data-catalog-group.scss";
import Icon from "../../../../Styled/Icon";


const CatalogGroupButton = styled.button`
  ${props => `
    &:hover,
    &:focus {
      color: ${props.theme.textLight};
      background-color: ${props.theme.modalHighlight};
    }`}
`;

/**
 *カタログのグループ対象のレイヤ一覧に表示用コンポーネント
 */

@observer
class DataCatalogGroup extends React.Component {
    static displayName = "DataCatalogGroup"
    static propsType = {
        terria: PropTypes.object.isRequired,
        viewState: PropTypes.object.isRequired,
        group: PropTypes.object.isRequired,
        isTopLevel: PropTypes.bool
    }

    /**
     * カタロググループの展開状態取得
     * @returns 
     */
    isOpen() {
        return this.props.group.isOpen;
    }

    /**
     * URL取得
     * @returns 
     */
    getNameOrPrettyUrl() {
        // Grab a name via nameInCatalog, if it's a blank string, try and generate one from the url
        const group = this.props.group;
        const nameInCatalog = group.nameInCatalog || "";
        if (nameInCatalog !== "") {
        return nameInCatalog;
        }

        const url = group.url || "";
        // strip protocol
        return url.replace(/^https?:\/\//, "");
    }

    render(){
        let title = getPath(this.props.group, " → ");
        return (
            <>
                <div className={Styles.root}>
                    <Text fullWidth>
                        <CatalogGroupButton
                            type="button"
                            className={classNames(
                                Styles.btnCatalog,
                                { [Styles.btnCatalogTopLevel]: this.props.isTopLevel },
                            )}
                            title={title}
                        >
                            <If condition={!this.props.isTopLevel}>
                                <span className={Styles.folder}>
                                {this.isOpen() ? (
                                    <Icon glyph={Icon.GLYPHS.folderOpen} />
                                ) : (
                                    <Icon glyph={Icon.GLYPHS.folder} />
                                )}
                                </span>
                            </If>
                            <Box justifySpaceBetween>
                                <Box>{this.getNameOrPrettyUrl()}</Box>
                                <Box centered>
                                    <span className={classNames(Styles.caret)}>
                                        {this.isOpen() ? (
                                        <Icon glyph={Icon.GLYPHS.opened} />
                                        ) : (
                                        <Icon glyph={Icon.GLYPHS.closed} />
                                        )}
                                    </span>
                                </Box>
                            </Box>
                        </CatalogGroupButton>
                    </Text>
                </div>
            </>
        )
    }
}
export default withTranslation()(withTheme(DataCatalogGroup));