import React from "react";
import { useTranslation } from "react-i18next";

import ViewState from "../../../ReactViewModels/ViewState";
import Icon from "../../../Styled/Icon";
import Text from "../../../Styled/Text";
import Prompt from "../../Generic/Prompt";

import Styles from "./help-button.scss";
import { runInAction } from "mobx";

import Config from "../../../../customconfig.json"

interface Props {
  viewState: ViewState;
  terria: any;
}

export default (props: Props) => {
  const { t } = useTranslation();

  const openPdf = () => {
    if(props.terria.authorityJudgment()){
      fetch(Config.config.apiUrl + "/file/view/manual/government")
        .then(res => res.blob())
        .then(blob => {
          const objectURL = URL.createObjectURL(blob);
          window.open(objectURL, '_blank');
        })
        .catch(error => {
          alert("行政用マニュアルの表示に失敗しました。")
          console.log(error)
        });
    }else{
      fetch(Config.config.apiUrl + "/file/view/manual/business")
        .then(res => res.blob())
        .then(blob => {
          const objectURL = URL.createObjectURL(blob);
          window.open(objectURL, '_blank');
        })
        .catch(error => {
          alert("事業者用マニュアルの表示に失敗しました。")
          console.log(error)
        });
    }
  }

  return (
    <div>
      <button
        className={Styles.helpBtn}
        onClick={evt => {
          evt.preventDefault();
          evt.stopPropagation();
          // props.viewState.showHelpPanel();
          openPdf();
        }}
      >
        <Icon glyph={Icon.GLYPHS.helpThick} />
        <span>{t("helpPanel.btnText")}</span>
      </button>
      <Prompt
        content={
          <div>
            <Text bold extraLarge textLight>
              {t("helpPanel.promptMessage")}
            </Text>
          </div>
        }
        displayDelay={500}
        dismissText={t("helpPanel.dismissText")}
        dismissAction={() => {
          runInAction(() =>
            props.viewState.toggleFeaturePrompt("help", false, true)
          );
        }}
        caretTopOffset={-8}
        caretLeftOffset={130}
        caretSize={15}
        promptWidth={273}
        promptTopOffset={50}
        promptLeftOffset={-100}
        isVisible={props.viewState.featurePrompts.indexOf("help") >= 0}
      />
    </div>
  );
};
