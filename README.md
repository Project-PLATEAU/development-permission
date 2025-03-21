# 開発許可申請管理システム
![image](./img/key_visual.png)

## 更新履歴
| 更新日時 | リリース | 更新内容 |
| ---- | ---- | ---- |
|2025/3/21| 3rd Release|**開発許可申請管理システム v3.0** <br> UI改善、2D表示モードを追加 <br> 事前協議・第32条協議機能を追加 <br> 開発許可申請機能を追加|
|2024/3/29 | 2nd Release|**開発許可申請管理システム v2.0** <br> 前面道路判定機能を追加<br>コミュニケーション機能を追加|
|2023/3/27 | 1st Release| **開発許可申請管理システム v1.0** <br>①地番図を用いた検索機能<br>②概況診断結果のレポート出力機能<br>③行政担当者への申請機能<br>④行政担当者の申請情報検索及び回答確認機能<br>を実装|

## 1.概要
本リポジトリでは、2024年度のProject PLATEAUで開発した「開発許可申請管理システム v3」のソースコードを公開しています。  
「開発許可申請管理システム」は、都市計画法に基づく開発許可手続（市区町村に対する「事前相談」を含む）をオンライン化し、申請の自動判定機能を提供するシステムです。  
<!-- 本システムは3D都市モデルをはじめとする空間情報をPostGISとGeoServerを組み合わせたリレーショナルデータベースで管理し、空間解析機能等のバックエンド機能を備えるウェブシステムとなっています。また、CesiumJS及びTerriaJSをフロントエンドで利用しています。

本システムの使い方は下記の操作マニュアルを参照ください。  
[開発許可申請管理システム　操作マニュアル](https://project-plateau.github.io/development-permission/manual/user_manual.html)

本システムの構築方法については下記の環境構築手順書を参照ください。  
[環境構築手順書](https://project-plateau.github.io/development-permission/manual/environment.html) -->

## 2．「開発許可申請管理システム」について
「開発許可申請管理システム」では、開発許可制度上の関連資料の収集や、関係者との協議を支援し、審査側の行政と申請側の民間の、双方の事務負担を軽減することを目的としています。本システムは、事前相談の結果を継承し、公共施設管理者との事前協議・同意（都市計画法第32条）、開発許可申請と許可（都市計画法第29条）に対する申請・コミュニケーション・様式作成などの機能を実装しています。  
本システムは、空間情報を三次元表示可能なCesiumJS及びTerriaJSをフロントエンドで利用するとともに、PostGIS（空間情報を管理するOSSのデータベース拡張機能）と GeoServer（空間情報を共有するOSSのGISサーバ）を組み合わせ、空間解析機能及びリレーショナルデータベースをバックエンドで統合したウェブシステムです。
本システムの詳細については、[技術検証レポート](https://www.mlit.go.jp/plateau/file/libraries/doc/plateau_tech_doc_0106_ver01.pdf)を参照してください。

## 3.利用手順
本システムの構築手順及び利用手順については[利用チュートリアル](https://project-plateau.github.io/development-permission/manual/environment.html)を参照してください。

## 4.システム概要 
#### ①地番図を用いた検索機能
- 対象の地番位置（筆界）を検索します。
- 検索結果筆界をクリックすると、対象の位置に地図表示箇所を移動します。

#### ②概況診断結果のレポート出力機能 
- 概況把握・診断の表示結果を帳票様式でExcel出力します。
- 対象範囲周辺の地図画像を帳票に引用します。

#### ③行政担当者への申請機能  
- 申請が完了した旨、申請者及び行政担当者へメールで通知します。
- 概況把握・診断結果のExcel帳票データを申請時IDに関連付けします。

#### ④行政担当者の申請情報検索及び回答確認機能
- 事業者申請時の入力情報、ステータスを検索条件とし、申請情報を検索します。
- 申請内容に対する行政担当者からの回答内容を確認します。

#### ⑤前面道路判定機能
- 「申請範囲選択で選択した申請範囲」に隣接する道路を判定します。

#### ⑥コミュニケーション機能
- 申請・回答内容についてチャット形式で事業者と行政担当者がコミュニケーションをとることができます。

#### ⑦事前協議・第32条協議機能
- 開発行為の事前協議、そして都市計画法第32条（公共施設の管理者の同意など）として、事前相談の内容を引き継いて申請することができます。

#### ⑧開発許可申請機能
- 都市計画法第29条（開発行為の許可）として、事前協議結果を引き継いで事業者から申請することができます。

## 5.利用技術

|種別|名称|バージョン|内容|
| ---- | ---- | ---- | ---- |
|ライブラリ|[TerriaJS](https://terria.io/)|8.1.22|UIの提供及びUIを介してCesiumJSの描画機能を制御するためのライブラリ|
||[CesiumJS](https://cesium.com/platform/cesiumjs/)|1.81|3Dビューワ上にデータを描画するためのライブラリ|
||[Apache POI](https://poi.apache.org/)|4.1.2|帳票出力にて、Excel出力を行うライブラリ|
||[React.js](https://ja.react.dev/)|16.3.2|JavaScriptのフレームワーク内で機能するUIを構築するためのライブラリ|
||[marker.js](https://markerjs.com/demos/all-defaults/)|2.29.4|画像データへの図形や文字情報の書き込みをブラウザ上で行うライブラリ|
||[tiff.js](https://github.com/seikichi/tiff.js)|1.0.0|Tiffファイルをブラウザで閲覧・編集可能なPNG形式に変換するライブラリ|
||[PDF.js](https://mozilla.github.io/pdf.js/)|3.10.111|PDFファイルをプレビューするライブラリ|
||[PDFBox](https://pdfbox.apache.org/)|2.0.28|PDF文章を扱うライブラリで、PDFファイルの画像ファイル変換に利用|
||[Leaflet](https://leafletjs.com/)|1.4.3|2Dビューワ上にデータを描画するためのライブラリ|
||[Selenium WebDriver](https://www.selenium.dev/ja/)|4.15.0|仮想ブラウザでの操作をシミュレートするためのライブラリ|
|ミドルウェア|[Apache HTTP Server](https://httpd.apache.org/)|2.4|Webアプリで配信を行うためのWebサーバ|
||[Apache Tomcat](https://tomcat.apache.org/)|9.0.65|GeoServer、カスタムアプリを実行するためのJava Servletコンテナ|
||[GeoServer](https://geoserver.org/)|2.21.5|各種データをWMS及びWFSなどで配信するためのGISサーバ|
||[PostgreSQL](https://www.postgresql.org/)|14.3|各種配信するデータを格納するデータベース|
||[PostGIS](https://postgis.net/)|3.1|PostgreSQLで位置情報を扱うことを可能とする拡張機能|
||[Java](https://openjdk.java.net/)|1.8 , 17|GeoServer、カスタムアプリを稼働させるためのプラットフォーム. GeoServer,申請APIは1.8を使用. シミュレータAPIは17を使用.|
|ソフトウェア|[FME Form](https://fme.safe.com/platform/)|任意|CityGML形式のデータをアプリケーションで利用可能な形式に変換するソフトウェア|
||[QGIS](https://www.qgis.org/ja/site/)|任意|各種GISデータをアプリケーションで利用可能な形式に変換するオープンソースGISソフトウェア|
|ランタイム環境|[Node.js](https://nodejs.org/en)|16.16.0|3Dビューワの実行環境|
|フレームワーク|[Spring Boot](https://spring.io/projects/spring-boot/)|2.7.0|Javaで利用可能なWebアプリのフレームワーク|

## 6.動作環境

|項目|最小動作環境|推奨動作環境|
| ---- | ---- | ---- |
|OS|Microsoft Windows 10 または 11|同左|
|CPU|Intel Core i3以上|Intel Core i5以上|
|メモリ|4GB以上|8GB以上|
|ディスプレイ解像度|1024×768以上|同左|
|ネットワーク|10Mbps以上|高速な回線を推奨|

## 7.本リポジトリのフォルダ構成

|フォルダ名|詳細|
| ---- | ---- |
|[/Settings/environment_settings/](./Settings/environment_settings/)|環境設定ファイル一式|
|[/SampleData/](./SampleData/)|サンプルデータ一式|
|[/SRC/3dview](./SRC/3dview/)|申請画面（3DViewer）のソースコード一式|
|[/SRC/api](./SRC/api/)|申請API（Springboot）のソースコード一式|
|[/SRC/simulator_api](./SRC/simulator_api/)|シミュレータAPI（Springboot）のソースコード一式|
|[/SRC/pdfjs/](./SRC/pdfjs/)|PDFビューワ（PDF.js）スタイルシート|

```Text
/
│
├─SampleData
│    ├─f_district
│    ├─f_lot_number
│    ├─judgement_layers
│    │   ├─agricultural_land
│    │   ├─buried_cultural_property
│    │   ├─city_planning
│    │   ├─flood_assumption
│    │   └─use_districts
│    ├─landmark
│    └─road_layers
│        ├─f_road_center_line
│        ├─f_road_lod2
│        └─f_split_line
├─Settings
│  └─environment_settings
│       └─road_judge
│
└─SRC
    ├─3dview
    ├─api
    ├─simulator_api
    └─pdfjs
```

## 8.ライセンス
* ソースコードおよび関連ドキュメントの著作権は国土交通省に帰属します。
* 本ドキュメントは[Project PLATEAUのサイトポリシー](https://www.mlit.go.jp/plateau/site-policy/)（CCBY4.0および公共データ利用規約第1.0版）に従い提供されています。

## 9.注意事項

* 本レポジトリは参考資料として提供しているものです。動作保証は行っておりません。
* 予告なく変更・削除する可能性があります。
* 本レポジトリの利用により生じた損失及び損害等について、国土交通省はいかなる責任も負わないものとします。

## 10.参考資料
* 開発許可のDXv3 技術検証レポート：https://www.mlit.go.jp/plateau/file/libraries/doc/plateau_tech_doc_0106_ver01.pdf
* 開発許可のDXv2 技術検証レポート：https://www.mlit.go.jp/plateau/file/libraries/doc/plateau_tech_doc_0076_ver01.pdf
* 開発許可のDXv1 技術検証レポート：https://www.mlit.go.jp/plateau/file/libraries/doc/plateau_tech_doc_0024_ver01.pdf
* PLATEAU Webサイト Use caseページ「開発許可のDX v2.0」: https://www.mlit.go.jp/plateau/use-case/uc23-06/
* Project-PLATEAU PLATEAU-VIEW:https://github.com/Project-PLATEAU/PLATEAU-VIEW
* Project-PLATEAU terriajs:https://github.com/Project-PLATEAU/terriajs
* GeoServer:https://geoserver.org/
