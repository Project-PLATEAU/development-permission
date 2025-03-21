package developmentpermission.util.model;

import lombok.Getter;
import lombok.Setter;

/**
 * 概況診断結果レポート帳票パラメータ
 */
@Getter
@Setter
public class ExportJudgeFormParam {

	/** テンプレートパス */
	private String templatePath;

	/** ページ当たりの最大行数 */
	private int pageMaxRow;

	/** フォント名 */
	private String fontName;

	/** 複数項目の区切り文字 */
	private String separator;

	/** 出力日 出力行 */
	private int dateRow;
	/** 出力日 出力列 */
	private int dateCol;
	/** 出力日 フォーマット */
	private String dateFormat;

	/** 概況図 開始行 */
	private int overviewStartRow;
	/** 概況図 終了行 */
	private int overviewEndRow;
	/** 概況図 開始列 */
	private int overviewStartCol;
	/** 概況図 終了列 */
	private int overviewEndCol;

	/** 区分 開始行 */
	private int categoryStartRow;
	/** 区分 終了行 */
	private int categoryEndRow;
	/** 区分名 出力列 */
	private int categoryTitleCol;
	/** 区分詳細 出力列 */
	private int categoryDescriptionCol;

	/** 開発予定地 出力行 */
	private int addressRow;
	/** 開発予定地 出力列 */
	private int addressCol;
	/** 開発予定地 番地区切り文字群(正規表現) */
	private String lotNumberSeparators;

	/** 判定結果出力開始行 */
	private int judgeResultStartRow;
	/** 判定結果 結合行数 */
	private int judgeResultMergeRow;
	/** 判定結果タイトル 出力列 */
	private int judgeResultTitleCol;
	/** 判定結果タイトル 結合列数 */
	private int judgeResultTitleMergeCol;
	/** 判定結果タイトル 文字サイズ */
	private short judgeResultTitleFontSize;
	/** 判定結果要約 出力列 */
	private int judgeResultSummaryCol;
	/** 判定結果要約 結合列数 */
	private int judgeResultSummaryMergeCol;
	/** 判定結果要約 文字サイズ */
	private short judgeResultSummaryFontSize;

	/** 判定結果詳細 行結合数 */
	private int judgeResultDescriptionMergeRow;
	/** 判定結果詳細 タイトル出力列 */
	private int judgeResultDescriptionTitleCol;
	/** 判定結果詳細 タイトル 文字サイズ */
	private short judgeResultDescriptionTitleFontSize;
	/** 判定結果詳細 タイトル 結合列数 */
	private int judgeResultDescriptionTitleMergeCol;
	/** 判定結果詳細 詳細列 */
	private int judgeResultDescriptionCol;
	/** 判定結果詳細 詳細結合列数 */
	private int judgeResultDescriptionMergeCol;
	/** 判定結果詳細 詳細 文字サイズ */
	private short judgeResultDescriptionFontSize;
	/** 判定結果詳細 画像列 */
	private int judgeResultDescriptionImageCol;
	/** 判定結果詳細 画像結合列数 */
	private int judgeResultDescriptionImageMergeCol;

	/** 判定結果詳細 「画像なし 区分判定」ラベル */
	private String judgeResultDescriptionNoGisLabel;
	
	/** 判定結果詳細「画像なし 非該当」ラベル */
	private String judgeResultDescriptionNoApplyLabel;
	
	/** 「画像なし 区分判定」ラベル文字サイズ */
	private short judgeResultDescriptionNoGisLabelSize;

	/** 判定結果詳細 回答タイトル結合行数 **/
	private int judgeResultDescriptionAnswerTitleMergerow;
	
	/** 判定結果詳細 回答タイトル結合列数 **/
	private int judgeResultDescriptionAnswerTitleMergeCol;
	
	/** 判定結果詳細 回答タイトルテンプレート文字列1 **/
	private String judgeResultDescriptionAnswerTitleTemplateText1;
	
	/** 判定結果詳細 回答タイトルテンプレート文字列2 **/
	private String judgeResultDescriptionAnswerTitleTemplateText2;
	
	/** 判定結果詳細 回答タイトルテンプレート文字列3 **/
	private String judgeResultDescriptionAnswerTitleTemplateText3;
	
	/** 判定結果詳細 回答タイトル出力列 **/
	private int judgeResultDescriptionAnswerTitleCol;
	
	/** 判定結果詳細 回答内容結合行数 **/
	private int judgeResultDescriptionAnswerContentMergeRow;
	
	/** 判定結果詳細 回答内容結合列数**/
	private int judgeResultDescriptionAnswerContentMergeCol;
	
	/** 判定結果詳細 回答内容出力列 **/
	private int judgeResultDescriptionAnswerContentCol;
	
	/** 判定結果詳細 回答タイトル非該当時文字列 **/
	private String judgeResultDescriptionAnswerTitleNoApply;
	
	/** 判定結果詳細 回答内容非該当時文字列 **/
	private String judgeResultDescriptionAnswerContentNoApply;
	
	/** 行政による回答削除 **/
	private String judgeResultDescriptionAnswerDelete;
	
	/** 一時フォルダパス */
	private String folderPath;

}
