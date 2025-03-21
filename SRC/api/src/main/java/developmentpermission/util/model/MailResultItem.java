package developmentpermission.util.model;

import lombok.Getter;
import lombok.Setter;

/**
 * メールメッセージ対象アイテム
 *
 */
@Getter
@Setter
public class MailResultItem {

	/** 対象 */
	private String target;
	/** 判定結果 */
	private String result;
	/** 合意内容 */
	private String consentResult;
	/** 合意日付 */
	private String consentDate;
	/** コメント */
	private String comment;
	/** 申請ファイル名 */
	private String applicationFileMasterName;
	/** 指示元担当課 */
	private String directionDepartmentNames;
	/** 修正内容 */
	private String reviseContent;

	/**
	 * コンストラクタ
	 */
	public MailResultItem() {
		target = "";
		result = "";
		consentResult = "";
		consentDate = "";
		comment = "";
		applicationFileMasterName = "";
		directionDepartmentNames = "";
		reviseContent = "";
	}
}
