import { runInAction } from "mobx";
import { observer } from "mobx-react";
import React from "react";
import { useTranslation } from "react-i18next";
import { DataSourceAction } from "../../../../Core/AnalyticEvents/analyticEvents";
import getPath from "../../../../Core/getPath";
import CatalogMemberMixin from "../../../../ModelMixins/CatalogMemberMixin";
import Terria from "../../../../Models/Terria";
import ViewState from "../../../../ReactViewModels/ViewState";
import CatalogItem, { ButtonState } from "./CatalogItem";
import toggleItemOnMapFromCatalog, { Op as ToggleOnMapOp } from "../../../DataCatalog/toggleItemOnMapFromCatalog";

interface Props {
  item: CatalogMemberMixin.Instance;
  viewState: ViewState;
  terria: Terria;
  sortFunc: (event: React.MouseEvent<HTMLButtonElement>) => void;
}

export default observer(function DataCatalogItem({
  item,
  viewState,
  terria,
  sortFunc
}: Props) {
  const { t } = useTranslation();
  const STATE_TO_TITLE = {
    [ButtonState.Loading]: t("catalogItem.loading"),
    [ButtonState.Remove]: t("catalogItem.removeFromMap"),
    [ButtonState.Add]: t("catalogItem.add")
  };

  const toggleEnable = async (event: React.MouseEvent<HTMLButtonElement>) => {
    const keepCatalogOpen = event.shiftKey || event.ctrlKey;
    await toggleItemOnMapFromCatalog(viewState, item, keepCatalogOpen, {
      [ToggleOnMapOp.Add]: DataSourceAction.addFromCatalogue,
      [ToggleOnMapOp.Remove]: DataSourceAction.removeFromCatalogue
    });
  };

  const onBtnClicked = (event: React.MouseEvent<HTMLButtonElement>) => {
    runInAction(() => {
        toggleEnable(event);
        sortFunc(event);
    });
  };

  let btnState: ButtonState;
  if (item.isLoading) {
    btnState = ButtonState.Loading;
  } else if (item.terria.workbench.contains(item)) {
    btnState = ButtonState.Remove;
  } else {
    btnState = ButtonState.Add;
  }

  return (
    <CatalogItem
      text={item.nameInCatalog!}
      title={getPath(item, " -> ")}
      btnState={btnState}
      onBtnClick={onBtnClicked}
      titleOverrides={STATE_TO_TITLE}
      item ={item}
    />
  );
});
