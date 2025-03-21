import { observer } from "mobx-react";
import PropTypes, { object } from "prop-types";
import React from "react";
import { withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import CustomStyle from "./scss/layer-tab.scss";
import DataCatalogItem from "./DataCatalogItem";
import DataCatalogGroup from "./DataCatalogGroup";
import GroupMixin from "../../../../ModelMixins/GroupMixin";
import legendStyle from "./scss/layer-legend.scss";
import proxyCatalogItemUrl from "../../../../Models/Catalog/proxyCatalogItemUrl";
import URI from "urijs";
import Config from "../../../../../customconfig.json";

/**
 * レイヤリストの凡例取得する時に、取得先判断用配列
 * レイヤ凡例のURLは配列のいずれかのファイルであれば、GeoServerから取得ではない
 */
const IMAGE_URL_REGEX = /[.\/](png|jpg|jpeg|gif|svg)/i;

/**
 * レイヤ一覧表示画面コンポーネント
 */
@observer
class LayerTab extends React.Component {
    static displayName = "LayerTab";
    static propTypes = {
        terria: PropTypes.object.isRequired,
        viewState: PropTypes.object.isRequired
    }
    constructor(props) {
        super(props);
        this.state = {
            viewState: props.viewState,
            terria: props.terria,
            tabAreaHeight: props.tabAreaHeight,
            // 画面に表示用レイヤリスト
            layerItemList:[],
            // ドラッグ対象
            dragItemIndex:"",
            // 差し込む線付ける対象
            styleElement:""
        };
        // チェックボックスでレイヤを新規追加する場合、レイヤ並び替えの関数
        this.addLayerSort = this.addLayerSort.bind(this);
    }

    /**
     *　初期処理
     */
    componentDidMount() {

        let layerArray = this.props.viewState.displayItems;
        let memberModels = this.props.terria.catalog.group.memberModels;

        // 初期表示状態を保存
        this.catalogDataBackup(memberModels);
        // 画面表示用リストの形に転換する
        if(Object.keys(layerArray).length < 1){
            layerArray = this.createLoopList(memberModels,"0",0);
        }
        this.setState({layerItemList:layerArray});

        // レイヤ並び替え
        this.sortLayer(layerArray);
        // 表示しているリスト格納
        this.props.viewState.setDisplayItems(layerArray);
    }

    /**
     * ログイン後、レイヤタブ画面を初回開く場合、カタログデータをバックアップする
     * バックアップ内容は下記です。
     * ・groupの展開状態
     * ・カタログファイルに定義されたworkbenchに表示するレイヤリスト
     * @param {*} memberModels 
     * @returns 
     */
    catalogDataBackup(memberModels){

        // 初期のカタログデータをバックアップされたら、以下の処理を行いません。
        let backup = this.props.viewState.groupItemListBak;
        if(Object.keys(backup).length > 0){
            return null;
        }

        // groupの展開状態のリストを作成する
        let groupItemList = this.getChildGroupList(memberModels);
        // 初期表示ときに、表示されるレイヤを取得する
        let displayingItemList = this.state.terria.workbench.items;
        let items = [];
        Object.keys(displayingItemList).map( i => {
            // カスタムレイヤであれば、バックアップリストから除きます
            let isCustomLayer = this.isCustomLayer(displayingItemList[i]);
            if(!isCustomLayer){
                items.push(displayingItemList[i]);
            }
        });
        
        // viewStateでバックアップ内容を保存する
        this.props.viewState.catalogDataBackup(groupItemList,items);

    }

    /**
     * カタログデータから、groupの対象のみを取得して、バックアップ用リストの形に変換する
     * @param {*} memberModels 
     * @returns group対象のバックアップリスト
     */
    getChildGroupList(memberModels){
        let childItemList = [];
        Object.keys(memberModels).map( i => {
            if(memberModels[i] !== this.props.terria.catalog.userAddedDataGroup){
                if(GroupMixin.isMixedInto(memberModels[i])){
                    childItemList.push({data: memberModels[i], isOpen:memberModels[i].isOpen});
                    let childens = this.getChildGroupList(memberModels[i].memberModels);
                    Object.keys(childens).map( j => {
                        childItemList.push(childens[j]);
                    });
                }
            }
        });
        return childItemList;
    }

    /**
     * カスタムレイヤであるか判断
     * @param {*} displayingItem 判断対象
     * @returns 判断結果
     */
    isCustomLayer(displayingItem){
        // カスタムレイヤ
        let customLayerList = Config.layer;
        let customLayerNameList = Object.keys(customLayerList).map(function (key) {return customLayerList[key]});
       
        // カスタムレイヤであるか判断
        return customLayerNameList.includes(displayingItem.uniqueId);
    }

    /**
     * レイヤ一覧の表示用リスト作成
     * @param {*} memberModels レイヤ対象リスト
     * @param {*} pid 親ID
     * @param {*} level レベル
     * @returns レイヤ一覧の表示用リスト
     */
    createLoopList(memberModels,pid,level){
        let layerArray = [];
        Object.keys(memberModels).map( index => {
            if(memberModels[index] !== this.props.terria.catalog.userAddedDataGroup){
                // 表示用リストに追加（ID、親ID、レベル、レイヤのID、レイヤ対象）
                layerArray.push({id:pid+`-`+index, pid:pid, level: level, uniqueId:memberModels[index].uniqueId, data:memberModels[index]});
            
                // レイヤ対象がグループ、かつ、展開状態が展開される場合、子対象を表示用リストに追加する
                if(GroupMixin.isMixedInto(memberModels[index]) && memberModels[index].isOpen){
                    let children = this.createLoopList(memberModels[index].memberModels, pid+`-`+index, level+1);
                    Object.keys(children).map( i => {
                        layerArray.push(children[i]);
                    }); 
                }
            }
        });

        return layerArray;
    }

    /**
     * カタログ定義より、凡例表示用srcを編集する
     * @returns 
     */
    loadImage(catalogItem){

        // 対象がgroupの場合、処理中止する
        if(GroupMixin.isMixedInto(catalogItem)){
            return null;
        }
        let url = catalogItem.url;
        let type = catalogItem.type;
        let legends = catalogItem.legends;
        let urls = [];
        if(Object.keys(legends).length > 0){
            Object.keys(legends).map(key => {
                let str = legends[key].url
                if(str?.match(IMAGE_URL_REGEX)){
                    urls.push(str);
                }else{
                    urls.push(this.editUrl(catalogItem));
                }
            });
        }else{
            if(url?.match(IMAGE_URL_REGEX)){
                urls.push(url);
            }else{
                urls.push(this.editUrl(catalogItem));
            }
        }
        return urls;
    }
  
    /**
     * GetLegendGraphicで凡例画像を取得する用リクエストを編集する
     * @returns リクエスト
     */
    editUrl(catalogItem){
        let layer = catalogItem.layers;
        let url = catalogItem.url;

        let legendUri = URI(proxyCatalogItemUrl(
            catalogItem,url.split("?")[0]
        ));

        legendUri.setQuery("service", "WMS")
        .setQuery("version", "1.3.0")
        .setQuery("request", "GetLegendGraphic")
        .setQuery("format", "image/png")
        .setQuery("layer", layer)
        .setQuery("WIDTH", "20")
        .setQuery("HEIGHT","20");
    
        let src = this.makeAbsolute(legendUri.toString());
        return src;
    }
  
    /**
     * URL編集
     * @param {String} url 
     * @returns 
     */
    makeAbsolute(url) {
        const uri = new URI(url);
        if (
            uri.protocol() &&
            uri.protocol() !== "http" &&
            uri.protocol() !== "https"
        ) {
            return url;
        } else {
            return uri.absoluteTo(window.location.href).toString();
        }
    }

    /**
     * ドラッグ可能対象判定
     * @param {*} listItem 
     * @returns 
     */
    isDraggable(listItem){
        let count = 0;
        let NotDraggableItem = Config.NotDraggableItem;
        // カスタムに定義されたドラッグ禁止対象と同じ、または、禁止対象の子対象である場合、ドラッグ不可にする
        Object.keys(NotDraggableItem).map(i => {
            if(listItem.uniqueId.startsWith(NotDraggableItem[i])){
                count ++;
            }
        });
        return count > 0 ? false : true;
    }

    /**
     * カタログレイヤリストの表示順をリセット
     */
    resetLayerList(){

        // 前回設定したレイヤリストをクリアする
        this.props.viewState.setHiddenItems([]);
        this.props.viewState.setDisplayItems([]);
        
        // groupの展開状態を初期化する
        let groupItemList =  this.props.viewState.groupItemListBak;
        Object.keys(groupItemList).map(i => {
            this.props.viewState.viewCatalogMember(groupItemList[i].data, groupItemList[i].isOpen);
        });

        // workbenchに表示されるレイヤ対象を初期化する
        let displayingItemList =  this.props.viewState.displayingItemListBak;
        const items = this.props.terria.workbench.items;
        for (const aItem of items) {
            let isCustomLayer = this.isCustomLayer(aItem);
            if (!isCustomLayer) {
                this.props.terria.workbench.remove(aItem);
                aItem.loadMapItems();
            }
        }

        Object.keys(displayingItemList).map(j => {
            this.props.terria.workbench.add(displayingItemList[j]);    
        });

        // 初期化されるカタログデータより、レイヤ一覧表示用リストを作成する
        let memberModels = this.props.terria.catalog.group.memberModels;
        let layerArray = this.createLoopList(memberModels,"0",0);
        this.setState({layerItemList:layerArray});
        
        // レイヤ並び替え
        this.sortLayer(layerArray);
        // 表示しているリスト格納
        this.props.viewState.setDisplayItems(layerArray);
    }

    /**
     * 一覧に、レコードのクリックイベント
     * @param {*} list 表示用レイヤ一覧
     * @param {*} index クリックされた要素に対するindex
     */
    onClickli(list,index){
        // groupの場合、groupの展開・折畳を切り替える
        if( GroupMixin.isMixedInto(list[index].data)){
            this.onClickGroup(list,index);
        }
    }

    /**
     * groupの展開・折畳の切り替え
     * @param {*} list 表示用レイヤ一覧
     * @param {*} index group要素は表示用レイヤ一覧にのindex
     */
    onClickGroup(list,index){

        // group展開状態
        let isOpen = list[index].data.isOpen;
        this.props.viewState.viewCatalogMember(list[index].data,!isOpen);
        let memberModels = list[index].data.memberModels;

        // カタログリストから画面表示用一覧リストの形に転換する
        let childlist = this.createLoopList(memberModels,list[index].id,list[index].level + 1);

        if(!isOpen){
            // グループは初回展開ではない場合、前回ソートした子要素を表示リストに追加
            let hiddenItems = this.props.viewState.hiddenItems;
            let hiddenItemsOfGroup = hiddenItems.filter((item) => item.uniqueId.startsWith(list[index].data.uniqueId + `/`));
            if (Object.keys(hiddenItemsOfGroup).length>0){
                childlist = hiddenItemsOfGroup;
            }

            // 折り畳んでいるグループのID
            let isClosedGroupId = [];

            Object.keys(childlist).map(i => {

                // 子要素がグループの場合、表示状態が折畳の場合、該当子要素の子要素が存在しても、表示リストに追加しないため、折り畳んでいるグループのIDを表示リストに追加
                if(GroupMixin.isMixedInto(childlist[i].data) && childlist[i].data.isOpen == false){
                    isClosedGroupId.push(childlist[i].id);
                }

                // 子要素が折り畳んでいるグループに所属するであれば、表示リストに追加しない
                if(isClosedGroupId.includes(childlist[i].pid)){
                    // 表示リストに追加しない
                }else{

                    // 表示リストに追加して、非表示リストから、削除
                    list.splice(Number(index)+1+Number(i),0,childlist[i]);
                    let addItemIndex = hiddenItems.findIndex((item) => item.id == childlist[i].id);
                    if(addItemIndex > -1){
                        hiddenItems.splice(addItemIndex,1);
                    }
                }
            });
            this.props.viewState.setHiddenItems(hiddenItems);
        }else{
            let deleteCount  = Object.keys(childlist).length;
            let removeItems = list.splice(Number(index)+1,deleteCount);
            this.setHiddenItems(removeItems);
            
        }
        this.setState({layerItemList:list});
        // 表示しているリスト格納
        this.props.viewState.setDisplayItems(list);
    }
    
    /**
     * 折り畳むため、非表示になるリストを非表示リストに追加
     * @param {*} removeItems 非表示になるリスト
     */
    setHiddenItems(removeItems){

        // 表示一覧から削除された対象の親ID
        let pid = removeItems[0].pid;
        // この前に、格納された非表示となるリスト
        let hiddenItems = this.props.viewState.hiddenItems;

        // 親レコードがあるか判断
        let indexOfParent = hiddenItems.findIndex((item) => item.id == pid);
        // 親がある場合、今回非表示となるリストを親の後に追加する
        if(indexOfParent > -1){
            Object.keys(removeItems).map(i => {
                hiddenItems.splice(Number(indexOfParent) + Number(i), 0, removeItems[i]);
            });
        }else{
            Object.keys(removeItems).map(i => {
                hiddenItems.push(removeItems[i]);
            });
        }

        // 今回追加した対象にgroupがあるか判断
        Object.keys(removeItems).map(i => {
            // group の場合、非表示リストに子レコードがあるか
            if(GroupMixin.isMixedInto(removeItems[i].data) && !removeItems[i].data.isOpen ){

                let indexOfChild = hiddenItems.findIndex((item) => item.pid == removeItems[i].id);
                if(indexOfChild > -1){
                    let children = hiddenItems.filter((item) => item.pid == removeItems[i].id);
                    let childCount = Object.keys(children).length;
                    let indexOfParent1 = hiddenItems.findIndex((item) => item.id == removeItems[i].id);

                    // 子レコードを親レコードのあとに移動する
                    const removedChild = hiddenItems.splice(indexOfChild, childCount);
                    Object.keys(removedChild).map(j => {
                        if(indexOfChild < indexOfParent1){
                            hiddenItems.splice(indexOfParent1 - childCount + 1 + Number(j) , 0, removedChild[j]);
                        }else{
                            hiddenItems.splice(indexOfParent1 + 1 + Number(i) , 0,  removedChild[j]);
                        }
                    });
                }
            }
        });

        // 表示一覧から削除したものを非表示リストに格納する
        this.props.viewState.setHiddenItems(hiddenItems);
    }

    /**
     * ドラッグ・ドロップの開始イベント
     * @param {*} event イベント
     * @param {*} list 表示用レイヤ一覧
     * @param {*} index ドラッグ要素のindex
     */
    onDragStart(event,list,index){

        // HTMLElementが存在すればドラッグ画像を設定
        const elm = document.getElementById(`layerItemList-${index}`);
        if (elm) {
            const rect = elm.getBoundingClientRect();
            const posX = event.clientX - rect.left;
            const posY = event.clientY - rect.top;
            event.dataTransfer.setDragImage(elm, posX, posY);
        }
            
        // ドラッグドロップのカーソル設定
        if(this.isDraggable(list[index])){
            event.dataTransfer.dropEffect = "move";
            event.dataTransfer.effectAllowed = "move";
        }else{
            event.dataTransfer.dropEffect = "none";
            event.dataTransfer.effectAllowed = "none";
        }
        this.setState({dragItemIndex:index});
    }

    /**
     * 差し込み先の要素に入る時のイベント
     * @param {*} event イベント
     * @param {*} list 表示用レイヤ一覧
     * @param {*} index 差し込み先要素のindex
     * @returns 処理中止
     */
    onDragOver(event,list,index){

        // 差し込み先対象index保存
        this.props.viewState.setCurrentDragItemIndex(-1);

        // ドロップできるように既定の動作を停止
        event.preventDefault();

        // 差し替え先のHTML要素
        let elm = document.getElementById(`layerItemList-${index}`);
        let targetIndex = Number(index);
        if (!elm || targetIndex < 0) {
            return;
        }
        
        //　ドラッグ対象
        let dragItemIndex = Number(this.state.dragItemIndex);

        // ドラッグ不可であるか判断する
        if(!this.isDraggable(list[dragItemIndex])){
            return;
        }

        // カーソル位置に応じてtargetIndexを更新する
        // 要素高さ半分
        const rect = elm.getBoundingClientRect();
        const heigth = rect.height / 2;
        // 要素中に、マウスカーソルの位置
        const posY = event.clientY - rect.top;

        let moveToIndex = targetIndex;
        let styledIndex = targetIndex;

        if(posY < heigth ){
            if(targetIndex > dragItemIndex){
                moveToIndex = targetIndex - 1;
            }

            // 差し替え先にスタイルを追加
            elm.style.borderTop = '2px solid blue';
            elm.style.borderBottom = '';
        }else{
            if(targetIndex <  dragItemIndex){
                moveToIndex = targetIndex + 1;
            }

            // 差し替え先にスタイルを追加
            elm.style.borderTop = '';
            elm.style.borderBottom = '2px solid blue';
        }
        
        // 上　⇒　下　ようにドラッグの場合、差し込み先がgroupであるか判断する
        if(targetIndex >=  dragItemIndex){
            let result = this.moveToAfter(list, moveToIndex, dragItemIndex, targetIndex);
            if(result.styleElementIndex != targetIndex){
                styledIndex = result.styleElementIndex; 
                elm.style.borderTop = '';
                elm.style.borderBottom = '';
            }
            if(result.moveToIndex != ""){
                moveToIndex = result.moveToIndex;
            }
        }

        // 差し込み先対象index保存
        this.props.viewState.setCurrentDragItemIndex(moveToIndex);
        
        // スタイル対象を格納前に、前回スタイルを追加した対象をリセットする
        let elmId = this.state.styleElement;
        let styleElementId = `layerItemList-` + styledIndex ;
        if(elmId != styleElementId){
            this.styleClear();
        }
        // 差し込み箇所の表示スタイルを付ける要素のID保存
        this.setState({targetIndex:moveToIndex,styleElement:styleElementId});
 
        // 移動先はドラッグ不可対象であるか判断して、カーソル変換
        if(this.isDraggable(list[moveToIndex])){
            if(list[moveToIndex].pid == list[dragItemIndex].pid){
                event.dataTransfer.dropEffect = "move";
                event.dataTransfer.effectAllowed = "move";
            }else{
                event.dataTransfer.dropEffect = "none";
                event.dataTransfer.effectAllowed = "none";
                elm.style.borderTop = '';
                elm.style.borderBottom = '';
            }
        }else{
            event.dataTransfer.dropEffect = "none";
            event.dataTransfer.effectAllowed = "none";
            elm.style.borderTop = '';
            elm.style.borderBottom = '';
        }
    }

    /**
     * 差し込み先の下に移動する時、差し替え先がgroupであるか判断する。
     * 判断結果より、スタイルを付ける要素のindex、差し込み先要素のindexを設定する
     * @param {*} list 表示用レイヤ一覧
     * @param {*} itemIndex 差し替え先のindex
     * @param {*} activeIndex ドラッグ対象のindex
     * @param {*} targetIndex カーソルがある要素のuni
     * @returns　スタイルを付ける要素のindex、差し込み先要素のindex
     */
    moveToAfter(list, itemIndex, activeIndex, targetIndex){
        let styleElementIndex = targetIndex;
        let moveToIndex = "";
        if(itemIndex < 0) return  {styleElementIndex:styleElementIndex, moveToIndex:moveToIndex};

        // 差し替え先が展開しているgroupの場合、ガイド線を子要素の最後に表示するにする
        if(GroupMixin.isMixedInto(list[itemIndex].data) && list[itemIndex].data.isOpen){
            if(list[itemIndex].pid === list[activeIndex].pid ){

                let groupItemId = list[itemIndex].id;
                let lastChildIndex = list.findLastIndex((e) => e.pid.startsWith(groupItemId) );
                let elmLast = document.getElementById(`layerItemList-${lastChildIndex}`);
                elmLast.style.borderBottom = '2px solid blue';
                elmLast.style.borderTop = '';
                styleElementIndex = lastChildIndex;
            }
        }else{
            if(Number(list[itemIndex].level) - 1 == Number(list[activeIndex].level)){

                // group内に移動する場合、移動せず
                let parentGroupIndex = list.findIndex(e => e.id == list[itemIndex].pid);
                // 差し込み先
                if(list[parentGroupIndex].pid === list[activeIndex].pid && list[parentGroupIndex].id != list[activeIndex].id){
                   let lastChildIndex = list.findLastIndex(e => e.pid.startsWith(list[parentGroupIndex].id) );
                    // group内の最後の要素の下に移動
                    if(lastChildIndex == itemIndex){
                        let elmLast = document.getElementById(`layerItemList-${lastChildIndex}`);
                        elmLast.style.borderBottom = '2px solid blue';
                        elmLast.style.borderTop = '';

                        styleElementIndex = lastChildIndex;
                        moveToIndex = parentGroupIndex;

                    }
                }
            }
        }
        // スタイルを付ける要素のindex、最新の差し替え先indexを設定して返却する。
        return {styleElementIndex:styleElementIndex, moveToIndex:moveToIndex};
    }

    /**
     * 差し込み先の要素から離す時実施イベント
     * @param {*} event イベント
     * @param {*} list 表示用レイヤ一覧
     * @param {*} index 差し込み先要素のindex
     * @returns 
     */
    onDragLeave(event,list,index){

        // 差し込み先のHTML要素
        let elm = document.getElementById(`layerItemList-${index}`);
        if (!elm || index < 0) return;

        // 要素のスタイルリセット
        elm.style.borderTop = '';
        elm.style.borderBottom = '';
        this.styleClear();
    }

    /**
     * スタイルを付ける要素のスタイルクリア
     * @returns 
     */
    styleClear(){
        let elmId = this.state.styleElement;
        let elm = document.getElementById(elmId);
        if (!elm) return;
        elm.style.borderTop = '';
        elm.style.borderBottom = '';
        this.setState({styleElement:""});
    }


    /**
     * ドラッグ終了時のカーソルはドラッグ区域内であるか判断
     * @param {*} clientX ドラッグ区域の位置X
     * @param {*} clientY ドラッグ区域の位置Y
     * @returns 
     */
    inDragArea(clientX, clientY){

        // ドラッグ可能のエリア
        let elm = document.getElementById(`dragArea`);
        if (!elm) return false;

        const rect = elm.getBoundingClientRect();
        if(rect.bottom >= clientY && rect.top <= clientY &&  rect.left <= clientX && rect.right >= clientX){
            return true;
        }else{
            return false;
        }
    }

    /**
     * ドラッグドロップ終了イベント
     * @param {*}event イベント
     * @param {*} list レイヤリスト
     * @param {*} index 移動先対象のindex
     * @returns 
    */
    onDragEnd(event,list, index){
        
        // 差し替え先の提出スタイルをクリア
        this.styleClear();

        // ドラッグ区域外にドラッグ場合、移動できない。
        if(!this.inDragArea(event.clientX, event.clientY)){
            return null;
        }

        // 移動先対象のindex
        let targetIndex = this.props.viewState.currentDragItemIndex;

        // 移動対象と移動先対象のindexが「0」以下の場合、移動できない。
        if(index < 0 || targetIndex < 0){
            return null;
        }

        // 移動先のindexと同じではない場合、移動できない。
        if(index == targetIndex){
            return null;
        }

        // 移動先対象
        let itemTo = list[targetIndex];
        // 移動対象
        let activeitem = list[index];

        // 移動先の親と同じではない場合、移動できない。
        if(activeitem.pid != itemTo.pid){
            return null;
        }

        // 移動先はドラッグ不可の対象の場合、移動できない。
        if(!this.isDraggable(itemTo)){
            return null;
        }

        // 移動対象はドラッグ不可の対象の場合、移動できない。
        if(!this.isDraggable(activeitem)){
            return null;
        }

        // レイヤ一覧リスト並び替え
        let result = this.reorde(list, index, targetIndex);

        // レイヤ一覧画面の表示リストを更新する
        this.setState({layerItemList: result});

        // 地図上のレイヤの表示順を更新する
        this.sortLayer(result);
        // 表示しているリスト格納
        this.props.viewState.setDisplayItems(result);

    }

    /**
     * ドラッグ操作に従って、レイヤ一覧リストを並び替える
     * @param {*} list レイヤ一覧リスト
     * @param {*} startIndex 移動対象のindex
     * @param {*} endIndex 移動先のindex
     * @returns 
     */
    reorde(list, startIndex, endIndex){
        const result = Array.from(list);
        // 移動先対象
        let itemTo = result[endIndex];
        // 移動対象
        let itemFrom = result[startIndex];

        // 移動対象と移動先はgroup以外の場合
        if(!GroupMixin.isMixedInto(itemFrom.data) && !GroupMixin.isMixedInto(itemTo.data)){
            const [removed] = result.splice(startIndex, 1);
            result.splice(endIndex, 0, removed);
        }

        // 移動対象がgroup、移動先がgroup以外の場合
        if(GroupMixin.isMixedInto(itemFrom.data) && !GroupMixin.isMixedInto(itemTo.data)){

            // 移動対象の子対象を取得して、一緒に移動する
            let childlist = result.filter( (child) => child.id.startsWith(itemFrom.id));
            let childCount = Object.keys(childlist).length;

            const removed = result.splice(startIndex, childCount);
            Object.keys(removed).map(i => {
                if(startIndex < endIndex){
                    result.splice(endIndex - childCount + 1 + Number(i), 0, removed[i]);
                }else{
                    result.splice(endIndex + Number(i), 0, removed[i]);
                }
            });
        }

        // 移動対象がgroup以外、移動先がgroupの場合
        if(!GroupMixin.isMixedInto(itemFrom.data) && GroupMixin.isMixedInto(itemTo.data)){

            // 移動先の子対象を取得して、一緒に移動する
            let childlist = result.filter( (child) => child.id.startsWith(itemTo.id));
            let childCount = Object.keys(childlist).length;

            const removed = result.splice(startIndex, 1);
            if(startIndex < endIndex){
                result.splice(endIndex + (childCount - 1), 0, removed[0]);
            }else{
                result.splice(endIndex, 0, removed[0]);
            }

        }

         // 移動対象と移動先がgroupの場合
        if(GroupMixin.isMixedInto(itemFrom.data) && GroupMixin.isMixedInto(itemTo.data)){
            // 移動先の子対象取得
            let itemFromChildlist = result.filter( (child) => child.id.startsWith(itemFrom.id));
            let itemFromChildCount = Object.keys(itemFromChildlist).length;

            // 移動先の子対象取得
            let itemToChildlist = result.filter( (child) => child.id.startsWith(itemTo.id));
            let itemToChildCount = Object.keys(itemToChildlist).length;

            const removed = result.splice(startIndex, itemFromChildCount);
            Object.keys(removed).map(i => {
                if(startIndex < endIndex){
                    result.splice(endIndex - itemFromChildCount + itemToChildCount + Number(i) , 0, removed[i]);
                }else{
                    result.splice(endIndex + Number(i) , 0, removed[i]);
                }
            });
        }

        return result;

    }
   
    /**
     * チェックボックスでレイヤを追加した後、地図画面上のレイヤ表示順を更新する。
     */
    addLayerSort(){
        // 現在画面に表示しているレイヤリスト
        let list = this.props.viewState.displayItems;
        // 並び替えを行う
        this.sortLayer(list);
    }

    /**
     * 地図画面上のレイヤ表示順更新
     * ・3Dレイヤが並び替え対象外
     * ・カスタムレイヤは一番上に表示
     * ・上記以外の2Dレイヤはレイヤ一覧の表示順に合わせて更新する
     * @param {*} items 表示しているレイヤリスト
     */
    sortLayer(items){
        try{
            // workbenchに表示しているレイヤリスト
            const oldLayerList = this.state.terria.workbench.items;
            // 一旦レイヤをすべて削除
            this.props.terria.workbench.removeAll();

            // 並び替え用レイヤリスト作成
            let catalogLayerItems = this.creatCatalogListForSort(items);

            // 新しい順番で、(WMSレイヤ)追加します。
            let count = Object.keys(catalogLayerItems).length;
            Object.keys(catalogLayerItems).map(key => {
                let index = Number(key) + 1;
                let item = catalogLayerItems[count - index];
                // workbenchに表示しているレイヤであるか判断
                if(oldLayerList.indexOf(item) >= 0){
                    if(item.type === "wms"){
                        item.loadMapItems();
                        this.state.terria.workbench.add(item); 
                    } 
                }    
            });

            // wms以外レイヤリスト
            let layerItems = []; 
            //　wms以外の表示中レイヤ追加
            Object.keys(oldLayerList).map(oldLayerKey => {
                if(oldLayerList[oldLayerKey].type === "wms"){
                    // カタログ以外のカスタムレイヤ（wms）を追加
                    if(!this.props.terria.workbench.contains(oldLayerList[oldLayerKey])){
                        oldLayerList[oldLayerKey].loadMapItems();
                        this.state.terria.workbench.add(oldLayerList[oldLayerKey]); 
                    }
                }else{
                    layerItems.push(oldLayerList[oldLayerKey]);
                }
            });

            // カスタムレイヤと3Dレイヤのリスト追加します。
            count = Object.keys(layerItems).length;
            Object.keys(layerItems).map(key => {
                let index = Number(key) + 1;
                let item = layerItems[count - index];
                item.loadMapItems();
                this.state.terria.workbench.add(item); 
            });

        }catch(error){
            console.error('処理に失敗しました', error);
        }
    }

    /**
     * 並び替えを行うために、レイヤリストを作成する
     * @param {*} items 表示しているレイヤリスト
     */
    creatCatalogListForSort(items){

        let sortItemList = [];
        // 表示しているレイヤ一覧をループ
        Object.keys(items).map(i => {

            // groupである場合
            if(GroupMixin.isMixedInto(items[i].data)){
                // 展開していない場合
                if(!items[i].data.isOpen){
                    // 非表示一覧に該当グループの子要素がある場合、ソート用リストに追加する
                    let hiddenItems = this.props.viewState.hiddenItems;
                    let hiddenItemsOfGroup = hiddenItems.filter((item) => item.uniqueId.startsWith(items[i].uniqueId));
                    if(Object.keys(hiddenItemsOfGroup).length > 0){
                        Object.keys(hiddenItemsOfGroup).map(i => {
                            sortItemList.push(hiddenItemsOfGroup[i].data);
                        });
                    }else{
                        sortItemList.push(items[i].data);
                    }
                }
            }else{
                sortItemList.push(items[i].data);
            }
        });
        
        let result = [];
        Object.keys(sortItemList).map(j => {

            if(GroupMixin.isMixedInto(sortItemList[j])){
                // 該当グループが展開された場合、group対象を削除する。子レコードだけを残す。
                if(!sortItemList[j].isOpen){
                    // 子レコードを追加する
                    let children =this.getChild(sortItemList[j].memberModels);
                    Object.keys(children).map( k => {
                        result.push(children[k]);
                    });
                }               
            }else{
                result.push(sortItemList[j]);
            }
        });

        return result;      
    }

    /**
     * group中に、レイヤ対象リストを取得する
     * @param {*} memberModels 
     * @returns グループ下に、子レイヤリスト
     */
    getChild(memberModels){
        let layerArray = [];
        Object.keys(memberModels).map( index => {
            if(memberModels[index] !== this.props.terria.catalog.userAddedDataGroup){            
                if(GroupMixin.isMixedInto(memberModels[index])){
                    let children = this.getChild(memberModels[index].memberModels);
                    Object.keys(children).map( i => {
                        layerArray.push(children[i]);
                    }); 
                }else{
                    layerArray.push(memberModels[index]);
                }
            }
        });
        return layerArray;
    }

    render() {
        let tabAreaHeight = this.state.tabAreaHeight;
        if(tabAreaHeight < 400){
            tabAreaHeight  = 400;
        }

        let list = this.state.layerItemList;
        return (
            <>
                <div className={CustomStyle.init_button_area}>
                    <button className={CustomStyle.init_button}
                        onClick={e => { this.resetLayerList()}}
                    >
                        <span>初期表示に戻す</span>
                    </button>
                </div>
                <div className={CustomStyle.layer_item_list_area} style={{height: (tabAreaHeight-60) + "px"}}>
                    <ul id={`dragArea`}>
                        {Object.keys(list).map(index => {
                            let srcs = this.loadImage(list[index].data);
                            return(
                                <li id={`layerItemList-${index}`}  draggable={true} key={index.toString()}
                                    onDragStart={evt => {this.onDragStart(evt,list,index)}}
                                    onDragOver={evt => {this.onDragOver(evt,list,index)}}
                                    onDragEnd={evt => {this.onDragEnd(evt,list,index)}}
                                    onDragLeave={evt => {this.onDragLeave(evt,list,index)}}
                                    onClick={evt => {this.onClickli(list,index)}} 
                                    className={ this.isDraggable(list[index]) ? "": CustomStyle.NotDraggable}
                                >
                                    <div style={{paddingLeft: (10*Number(list[index].level)) + "px"}}>
                                        {GroupMixin.isMixedInto(list[index].data) && (
                                            <DataCatalogGroup
                                                group={list[index].data}
                                                viewState={this.props.viewState}
                                                terria={this.props.terria}
                                                isTopLevel={ list[index].level==0?true:false }
                                            />
                                        )}
                                        {!GroupMixin.isMixedInto(list[index].data) && (
                                            <div className={`${list[index].level > 1 ? CustomStyle.dataCatalogItemChild : CustomStyle.dataCatalogItem}`}>
                                                { Object.keys(srcs).map(key => (
                                                    <img key = {key.toString()} src={srcs[key]} className={legendStyle.legends}></img>
                                                ))}
                                                <DataCatalogItem
                                                    item={list[index].data}
                                                    viewState={this.props.viewState}
                                                    terria={this.props.terria}
                                                    sortFunc ={this.addLayerSort}
                                                />
                                            </div>
                                        )}
                                    </div>
                                </li>
                            )
                        })}
                    </ul>
                </div>
            </>
        );
    }
}
export default withTranslation()(withTheme(LayerTab));