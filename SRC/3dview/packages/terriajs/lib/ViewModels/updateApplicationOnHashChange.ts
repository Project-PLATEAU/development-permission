"use strict";

import Terria from "../Models/Terria";
import * as querystring from 'querystring';
import {runInAction,toJS} from "mobx";

/**
 * Updates the  {@link Terria} when the window's 'hashchange' event is raised.  This allows new init files and
 * "start=" URLs to be loaded just by changing the hash portion of the URL in the browser's address bar.
 *
 * @param  {Terria} terria The Terria instance to update.
 * @param {Window} window The browser's window DOM object.
 */
export default function(terria: Terria,viewState:any, window: Window) {
  const eventFunction = () => {
    setTimeout(() => {
      try {
        viewState.triggerResizeEvent();
        const jsonResult = processHashFragment(window.location.hash);
        if(jsonResult.applicationPlace && jsonResult.applicationCategory && jsonResult.generalConditionDiagnosisResult && jsonResult.folderName && jsonResult.fileName){
          runInAction(() => {
            viewState.setApplicationPlace(toJS(jsonResult.applicationPlace));
            viewState.setCheckedApplicationCategory(toJS(jsonResult.applicationCategory));
            viewState.setGeneralConditionDiagnosisResult(toJS(jsonResult.generalConditionDiagnosisResult));
            viewState.setFolderName(jsonResult.folderName);
            viewState.setFileName(jsonResult.fileName);
            viewState.setApplicationId(jsonResult.applicationId);
          });
          viewState.moveToGeneralAndRoadJudgementResultViewForSimulateExecution();
        }
      } catch (e) {
        console.error(e);
      }
    }, 10000);
  };
  window.addEventListener("hashchange",eventFunction,false);
  eventFunction();
}

// ハッシュフラグメントを解析する関数
function getHashParams(hash: string): Record<string, string> {
  // ハッシュフラグメントの先頭の`#`を削除
  const hashWithoutHash = hash.startsWith('#') ? hash.slice(1) : hash;
  // `querystring.parse` を使用してハッシュフラグメントを解析
  return querystring.parse(hashWithoutHash) as Record<string, string>;
}

// ハッシュフラグメントを処理してJSONを生成する関数
function processHashFragment(hashFragment: string): any {

  // ハッシュフラグメントのパラメータを取得
  const params = getHashParams(hashFragment);

  // URLデコードされたJSON文字列の取得と解析
  const applicationCategoryJson = decodeURIComponent(params['applicationCategory'] || '');
  const applicationPlaceJson = decodeURIComponent(params['applicationPlace'] || '');
  const generalConditionDiagnosisResultJson = decodeURIComponent(params['generalConditionDiagnosisResults'] || '');
  const folderNameJson = decodeURIComponent(params['folderName'] || '');
  const fileNameJson = decodeURIComponent(params['fileName'] || '');

  let applicationCategory;
  let applicationPlace;
  let generalConditionDiagnosisResult;
  let folderName;
  let fileName;
  let applicationId = 0;

  try {
      applicationCategory = JSON.parse(applicationCategoryJson);
  } catch (e) {
      //console.error('Error parsing applicationCategory JSON:', e);
      applicationCategory = null;
  }

  try {
      applicationPlace = JSON.parse(applicationPlaceJson);
  } catch (e) {
      //console.error('Error parsing applicationPlace JSON:', e);
      applicationPlace = null;
  }

  try {
      generalConditionDiagnosisResult = JSON.parse(generalConditionDiagnosisResultJson);
  } catch (e) {
      //console.error('Error parsing applicationPlace JSON:', e);
      generalConditionDiagnosisResult = null;
  }

  try {
      folderName = JSON.parse(folderNameJson);
  } catch (e) {
      //console.error('Error parsing applicationPlace JSON:', e);
      folderName = null;
  }

  try {
      fileName = JSON.parse(fileNameJson);
  } catch (e) {
      //console.error('Error parsing applicationPlace JSON:', e);
      fileName = null;
  }

  try {
    if(params['applicationId'] != null && Number(params['applicationId']) > 0){
      applicationId = Number(params['applicationId']);
    }
  } catch (e) {
      //console.error('Error parsing applicationPlace JSON:', e);
      applicationId = 0;
  }

  // JSONに変換
  const json = {
    applicationCategory:applicationCategory,
    applicationPlace:applicationPlace,
    generalConditionDiagnosisResult:generalConditionDiagnosisResult,
    folderName:folderName,
    fileName:fileName,
    applicationId:applicationId
  };

  return json;
}