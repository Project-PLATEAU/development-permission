package developmentpermission.util.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * メールメッセージアイテム
 */
@Getter
@Setter
public class MailItem {

	/** 照合ID */
	private String id;
	/** パスワード */
	private String password;
	/** 申請者氏名 */
	private String name;
	/** 申請者メールアドレス */
	private String mailAddress;
	/** 申請ID */
	private String applicationId;
	/** 申請種類名 */
	private String applicationTypeName;
	/** 申請段階名 */
	private String applicationStepName;
	/** 版番号（書式：”n版”） */
	private String versionInformation;
	/** 申請地番 */
	private String lotNumber;
	/** 申請登録日時 */
	private String timestamp;
	/** 判定結果リスト */
	private List<MailResultItem> resultList;
	/** 回答日数 */
	private String answerDays;
	/** 申請月 */
	private String applicationMonth;
	/** 申請日 */
	private String applicationDay ;
	/** 回答対象 */
	private String answerTarget;
	/** 問い合わせ内容 */
	private String inquiryContent ;
	/** 部署名 */
	private String departmentName;
	/** 回答内容 */
	private String answerContent;
	/** コメント１ */
	private String comment1;
	/** 帳票名 */
	private String ledgerName;
	/** 統括部署管理者の受付確認コメント(メール本文) */
	private String acceptContent;
	/** 申請ファイル変更案内 */
	private String applicationFileChangedContent;
	/** 統括部署管理者の受付確認コメント */
	private String comment;

	/**
	 * コンストラクタ
	 */
	public MailItem() {
		id = "";
		password = "";
		name = "";
		mailAddress = "";
		applicationId = "";
		applicationTypeName = "";
		applicationStepName = "";
		versionInformation = "";
		lotNumber = "";
		timestamp = "";
		resultList = new ArrayList<MailResultItem>();
		answerDays = "";
		applicationMonth = "";
		applicationDay = "";
		answerTarget = "";
		inquiryContent = "";
		departmentName = "";
		answerContent = "";
		comment1 = "";
		ledgerName = "";
		acceptContent = "";
		applicationFileChangedContent = "";
		comment = "";
	}

	/**
	 * 複製
	 */
	public MailItem clone() {
		MailItem tmpItem = new MailItem();
		tmpItem.id = id;
		tmpItem.password = password;
		tmpItem.name = name;
		tmpItem.mailAddress = mailAddress;
		tmpItem.applicationId = applicationId;
		tmpItem.applicationTypeName = applicationTypeName;
		tmpItem.applicationStepName = applicationStepName;
		tmpItem.versionInformation = versionInformation;
		tmpItem.lotNumber = lotNumber;
		tmpItem.timestamp = timestamp;
		tmpItem.resultList = new ArrayList<MailResultItem>();
		if (resultList != null) {
			for (MailResultItem item : resultList) {
				MailResultItem resultItem = new MailResultItem();
				resultItem.setTarget(item.getTarget());
				resultItem.setResult(item.getResult());
				resultItem.setApplicationFileMasterName(item.getApplicationFileMasterName());
				resultItem.setDirectionDepartmentNames(item.getDirectionDepartmentNames());
				resultItem.setReviseContent(item.getReviseContent());
				tmpItem.resultList.add(resultItem);
			}
		}
		tmpItem.answerDays = answerDays;
		tmpItem.applicationMonth = applicationMonth;
		tmpItem.applicationDay = applicationDay;
		tmpItem.answerTarget = answerTarget;
		tmpItem.inquiryContent = inquiryContent;
		tmpItem.departmentName = departmentName;
		tmpItem.answerContent = answerContent;
		tmpItem.comment1 = comment1;
		tmpItem.ledgerName = ledgerName;
		tmpItem.acceptContent = acceptContent;
		tmpItem.applicationFileChangedContent = applicationFileChangedContent;
		tmpItem.comment = comment;
		return tmpItem;
	}
}
