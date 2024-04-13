import PropTypes from "prop-types";
import React from "react";
import { useTranslation } from "react-i18next";
import styled from "styled-components";
import defaultValue from "terriajs-cesium/Source/Core/defaultValue";
import Box from "../../../../Styled/Box";
import { RawButton } from "../../../../Styled/Button";
import Icon from "../../../../Styled/Icon";
import Text from "../../../../Styled/Text";
import CatalogMemberMixin from "../../../../ModelMixins/CatalogMemberMixin";

/**
 * ボタン状態配列
 */
export enum ButtonState {
  Loading,
  Remove,
  Add
}

const STATE_TO_ICONS: Record<ButtonState, React.ReactElement> = {
  [ButtonState.Loading]: <Icon glyph={Icon.GLYPHS.loader} />,
  [ButtonState.Remove]: <Icon glyph={Icon.GLYPHS.checkboxOn} />,
  [ButtonState.Add]: <Icon glyph={Icon.GLYPHS.checkboxOff} />
};

interface Props {
  title: string;
  text: string;
  btnState: ButtonState;
  onBtnClick: (event: React.MouseEvent<HTMLButtonElement>) => void;
  titleOverrides?: Partial<Record<ButtonState, string>>;
  item: CatalogMemberMixin.Instance;
}

/** Dumb catalog item */
function CatalogItem(props: Props) {
  const { t } = useTranslation();
  const STATE_TO_TITLE = {
    [ButtonState.Loading]: t("catalogItem.loading"),
    [ButtonState.Remove]: t("catalogItem.remove"),
    [ButtonState.Add]: t("catalogItem.add")
  };
  const stateToTitle: Partial<Record<ButtonState, string>> = defaultValue(
    props.titleOverrides,
    STATE_TO_TITLE
  );
  return (
    <Root>
      <Text fullWidth breakWord>
        <ItemTitleButton
          type="button"
          title={props.title}
        >
          {props.text}
        </ItemTitleButton>
      </Text>
      <Box>
        <ActionButton
          type="button"
          onClick={props.onBtnClick}
          title={stateToTitle[props.btnState] || ""}
        >
          {STATE_TO_ICONS[props.btnState]}
        </ActionButton>
      </Box>
    </Root>
  );
}

const Root = styled.div`
  display: flex;
  width: 100%;
`;

const ItemTitleButton = styled(RawButton)`
  text-align: left;
  word-break: normal;
  overflow-wrap: anywhere;
  padding: 8px;
  width: 100%;
  cursor: grab;

  &:focus,
  &:hover {
    color: ${p => p.theme.modalHighlight};
  }
  

  @media (max-width: ${p => p.theme.sm}px) {
    font-size: 0.9rem;
    padding-top: 10px;
    padding-bottom: 10px;
    border-bottom: 1px solid ${p => p.theme.greyLighter};
  }
`;

const ActionButton = styled(RawButton)`
  svg {
    height: 20px;
    width: 20px;
    margin: 5px;
    fill: ${p => p.theme.colorPrimary};
  }

  &:hover,
  &:focus {
    svg {
      fill: ${p => p.theme.modalHighlight};
    }
  }
`;

CatalogItem.propTypes = {
  text: PropTypes.string,
  title: PropTypes.string,
  onBtnClick: PropTypes.func,
  btnState: PropTypes.oneOf([
    ButtonState.Loading,
    ButtonState.Add,
    ButtonState.Remove
  ]),
  titleOverrides: PropTypes.object,
};

export default CatalogItem;
