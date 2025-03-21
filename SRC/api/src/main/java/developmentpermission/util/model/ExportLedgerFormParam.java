package developmentpermission.util.model;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

/**
 * 32条協議書・同意通知書帳票パラメータ
 */
@Getter
@Setter
public class ExportLedgerFormParam {

	/** フォント名 */
	private String fontName;

	/** 編集開始行 */
	private int editSrartRow;

	/** ページ当たりの最大行数（ひな形以外の1ページ目） */
	private int firstPageMaxRow;
	/** ページ当たりの最大行数（ひな形以外の2ページ目以降） */
	private int pageMaxRow;

	/** 印刷範囲開始列 */
	private int printAreaStartCol;
	/** 印刷範囲終了列 */
	private int printAreaEndCol;

	/** 課名 印字開始列 */
	private int departmentNameStartCol;
	/** 課名 フォントサイズ */
	private short departmentNameFontSize;
	/** 課名 行当たり最大文字数 */
	private int departmentNameRowMaxCharacter;
	/** 課名 段落番号をつけるかフラグ */
	private Boolean departmentNameParagraph;
	/** 課名 段落番号が文字列と同一セルに印字するかフラグ */
	private Boolean departmentNameParagraphWithinText;
	/** 課名 段落番号の書式文字 */
	private String departmentNameParagraphCharacter;
	/** 課名 段落番号の採番種類（0：レコード全件で採番、1：課ごとに採番） */
	private String departmentNameParagraphType;
	/** 課名 空白行挿入フラグ */
	private String departmentNameInsertBlankLine;

	/** 関連条項名 印字開始列 */
	private int judgementTitleStartCol;
	/** 関連条項名 フォントサイズ */
	private short judgementTitleFontSize;
	/** 関連条項名 行当たり最大文字数 */
	private int judgementTitleRowMaxCharacter;
	/** 関連条項名 段落番号をつけるかフラグ */
	private Boolean judgementTitleParagraph;
	/** 関連条項名 段落番号が文字列と同一セルに印字するかフラグ */
	private Boolean judgementTitleParagraphWithinText;
	/** 関連条項名 段落番号の書式文字 */
	private String judgementTitleParagraphCharacter;
	/** 関連条項名 段落番号の採番種類（0：レコード全件で採番、1：課ごとに採番） */
	private String judgementTitleParagraphType;
	/** 関連条項名 空白行挿入フラグ */
	private String judgementTitleInsertBlankLine;

	/** 行政回答内容 印字開始列 */
	private int answerContentStartCol;
	/** 行政回答内容 フォントサイズ */
	private short answerContentFontSize;
	/** 行政回答内容 行当たり最大文字数 */
	private int answerContentRowMaxCharacter;
	/** 行政回答内容 段落番号をつけるかフラグ */
	private Boolean answerContentParagraph;
	/** 行政回答内容 段落番号が文字列と同一セルに印字するかフラグ */
	private Boolean answerContentParagraphWithinText;
	/** 行政回答内容 段落番号の書式文字 */
	private String answerContentParagraphCharacter;
	/** 行政回答内容 段落番号の採番種類（0：レコード全件で採番、1：課ごとに採番） */
	private String answerContentParagraphType;
	/** 行政回答内容 空白行挿入フラグ */
	private String answerContentInsertBlankLine;

	/** 出力内容マップ（課ID-「条項名、回答内容」リスト） */
	Map<String, List<Map<String, String>>> outputDataDetialMap;

}
