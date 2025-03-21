import React from "react";
import { useTranslation } from "react-i18next";

import ViewState from "../../../ReactViewModels/ViewState";
import Icon from "../../../Styled/Icon";

import Styles from "../../Map/HelpButton/help-button.scss";
import Terria from "../../../Models/Terria";


interface Props {
  viewState: ViewState;
  terria: Terria;
}

export default (props: Props) => {
  const { t } = useTranslation();

  return (
    <div>
      <button
        className={Styles.helpBtn}
        onClick={evt => {
          evt.preventDefault();
          evt.stopPropagation();
          props.viewState.setGeneralConditionDiagnosisrReportShow(!props.viewState.generalConditionDiagnosisrReportShow);
        }}
      >
        {
        //<Icon glyph={Icon.GLYPHS.user} />
        }
        <span>概況診断レポート一覧</span>
      </button>
    </div>
  );
};
