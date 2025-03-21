package developmentpermission.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import developmentpermission.util.model.MailItem;
import developmentpermission.util.model.MailResultItem;

/**
 * メール文言設定用プロパティ
 */
public class MailMessageUtil {

	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(MailMessageUtil.class);

	/** キー: 回答確認ID/パスワード通知(事業者向け) 件名 */
	public static final String KEY_BUSSINESS_ACCEPT_SUBJECT = "mail.bussiness.application.accept.subject";
	/** キー: 回答確認ID/パスワード通知(事業者向け) 本文 */
	public static final String KEY_BUSSINESS_ACCEPT_BODY = "mail.bussiness.application.accept.body";
	/** キー: No.2 申請受付通知(行政向け) 件名 */
	public static final String KEY_ACCEPT_SUBJECT = "mail.application.accept.subject";
	/** キー: No.2 申請受付通知(行政向け) 本文 */
	public static final String KEY_ACCEPT_BODY = "mail.application.accept.body";
	/** キー: No.2 申請受付通知(行政向け) 本文-統括部署管理者の受付コメント */
	public static final String KEY_ACCEPT_BODY_ACCEPT_CONTENT = "mail.application.accept.body.accept.content";
	/** キー: No.2 申請受付通知(行政向け) 本文-差し替えた申請ファイルの案内文 */
	public static final String KEY_ACCEPT_BODY_APPLICATION_FILE_CHANGED_CONTENT = "mail.application.accept.body.application.file.changed.content";
	/** キー: No.2 申請提出書類変更通知(行政向け) 件名 */
	public static final String KEY_APPLICATION_FILE_CHANGE_SUBJECT = "mail.application.file.change.subject";
	/** キー: No.2 申請提出書類変更通知(行政向け)本文 */
	public static final String KEY_APPLICATION_FILE_CHANGE_BODY = "mail.application.file.change.body";
	
	/** キー: 回答完了通知(事業者向け) 件名 */
	public static final String KEY_BUSSINESS_FINISH_SUBJECT = "mail.bussiness.answer.finish.subject";
	/** キー: 回答完了通知(事業者向け) 本文 */
	public static final String KEY_BUSSINESS_FINISH_BODY = "mail.bussiness.answer.finish.body";
	/** キー: 回答完了通知(事業者向け) 本文-コメント文言（再申請） */
	public static final String KEY_BUSSINESS_FINISH_BODY_COMMENT_REAPPLICATION = "mail.bussiness.answer.finish.body.comment.reapplication";
	/** キー: 回答完了通知(事業者向け) 本文-コメント文言（事業者合意登録） */
	public static final String KEY_BUSSINESS_FINISH_BODY_COMMENT_AGREEMENT_REGISTRATION = "mail.bussiness.answer.finish.body.comment.agreement.registration";
	/** キー: 回答完了通知(事業者向け) 本文-コメント文言（次の段階へ進む） */
	public static final String KEY_BUSSINESS_FINISH_BODY_COMMENT_NEXT_STEP = "mail.bussiness.answer.finish.body.comment.next.step";
	
	/** キー: 全部署回答完了通知(行政向け) 件名 */
	public static final String KEY_FINISH_SUBJECT = "mail.answer.finish.subject";
	/** キー: 全部署回答完了通知(行政向け) 本文 */
	public static final String KEY_FINISH_BODY = "mail.answer.finish.body";
	
	/** キー: 問合せ通知(行政(事業者から)向け) 件名 */
	public static final String KEY_INQUIRY_FROM_BUSSINESS_SUBJECT = "mail.inquiry.from.bussiness.subject";
	/** キー: 問合せ通知(行政(事業者から)向け) 本文 */
	public static final String KEY_INQUIRY_FROM_BUSSINESS_BODY = "mail.inquiry.from.bussiness.body";
	/** キー: 問合せ通知(行政(行政から)向け) 件名 */
	public static final String KEY_INQUIRY_FROM_GOVERNMENT_SUBJECT = "mail.inquiry.from.government.subject";
	/** キー: 問合せ通知(行政(行政から)向け) 本文 */
	public static final String KEY_INQUIRY_FROM_GOVERNMENT_BODY = "mail.inquiry.from.government.body";
	/** キー:問合せ回答通知(事業者向け) 件名 */
	public static final String KEY_BUSSINESS_INQUIRY_SUBJECT = "mail.bussiness.inquiry.subject";
	/** キー: 問合せ回答通知(事業者向け) 本文 */
	public static final String KEY_BUSSINESS_INQUIRY_BODY = "mail.bussiness.inquiry.body";
	/** キー: 申請受付通知(行政（回答通知担当課）向け) 件名 */
	public static final String KEY_ACCEPT_NOTIFICATION_SUBJECT = "mail.application.accept.notification.subject";
	/** キー: 申請受付通知(行政（回答通知担当課）向け) 本文 */
	public static final String KEY_ACCEPT_NOTIFICATION_BODY = "mail.application.accept.notification.body";
	
	/** キー: 申請受付通知(行政（回答通知担当課）向け) 事前協議時付加コメント */
	public static final String KEY_ACCEPT_NOTIFICATION_NEGOTIATION_ADDITIONAL_COMMENT = "mail.application.accept.notification.negotiation.additional.comment";
	
	/** キー: No.10 事前協議差戻通知(事業者向け) 件名 */
	public static final String KEY_APPLICATION_REMAND_SUBJECT = "mail.application.remand.subject";
	/** キー: No.10 事前協議差戻通知(事業者向け) 本文 */
	public static final String KEY_APPLICATION_REMAND_BODY = "mail.application.remand.body";
	
	/** キー: No.10 回答許可通知(行政（統括部署管理者）向け) 件名 */
	public static final String KEY_RESPONSE_APPROVAL_SUBJECT = "mail.response.approval.subject";
	/** キー: No.10 回答許可通知(行政（統括部署管理者）向け) 本文 */
	public static final String KEY_RESPONSE_APPROVAL_BODY = "mail.response.approval.body";

	/** キー: No.11 同意項目否認通知(行政（回答担当課）向け) 件名 */
	public static final String KEY_CONSENT_DENIAL_NOTIFICATION_SUBJECT = "mail.consent.denial.notification.subject";
	/** キー: No.11 同意項目否認通知(行政（回答担当課）向け) 本文 */
	public static final String KEY_CONSENT_DENIAL_NOTIFICATION_BODY = "mail.consent.denial.notification.body";

	/** キー: No.12 同意項目登録通知(行政（回答担当課）向け) 件名 */
	public static final String KEY_CONSENT_REGIST_NOTIFICATION_SUBJECT = "mail.consent.regist.notification.subject";
	/** キー: No.12 同意項目登録通知(行政（回答担当課）向け) 本文 */
	public static final String KEY_CONSENT_REGIST_NOTIFICATION_BODY = "mail.consent.regist.notification.body";
	/** キー: No.12 同意項目登録通知(行政（回答担当課）向け) 本文- コメント文言（事業者側で同意項目登録完了） */
	public static final String KEY_CONSENT_REGIST_NOTIFICATION_BODY_COMMENT_CONSENT_COMPLETED = "mail.consent.regist.notification.body.comment.consent.completed";

	/** キー: No.13 事前協議行政確定登録完了通知(行政（回答担当課管理者）向け) 件名 */
	public static final String KEY_NEGOTIATION_CONFIRMED_NOTIFICATION_SUBJECT = "mail.negotiation.confirmed.notification.subject";
	/** キー: No.13 事前協議行政確定登録完了通知(行政（回答担当課管理者）向け) 本文 */
	public static final String KEY_NEGOTIATION_CONFIRMED_NOTIFICATION_BODY = "mail.negotiation.confirmed.notification.body";

	/** キー: No.14 事前協議行政確定登録許可通知（行政（統括部署管理者）向け）件名 */
	public static final String KEY_NEGOTIATION_CONFIRMED_APPROVAL_NOTIFICATION_SUBJECT = "mail.negotiation.confirmed.approval.notification.subject";
	/** キー: No.14 事前協議行政確定登録許可通知（行政（統括部署管理者）向け）本文 */
	public static final String KEY_NEGOTIATION_CONFIRMED_APPROVAL_NOTIFICATION_BODY = "mail.negotiation.confirmed.approval.notification.body";

	/** キー: No.15 回答リマインド通知（行政（回答担当課）向け）件名 */
	public static final String KEY_ANSWER_REMIND_NOTIFICATION_SUBJECT = "mail.answer.remind.notification.subject";
	/** キー: No.15 回答リマインド通知（行政（回答担当課）向け）本文 */
	public static final String KEY_ANSWER_REMIND_NOTIFICATION_BODY = "mail.answer.remind.notification.body";

	/** キー: No.16 事前協議回答リマインド通知（事業者）件名 */
	public static final String KEY_NEGOTIATION_ANSWER_REMIND_NOTIFICATION_SUBJECT = "mail.negotiation.answer.remind.notification.subject";
	/** キー: No.16 事前協議回答リマインド通知（事業者）本文 */
	public static final String KEY_NEGOTIATION_ANSWER_REMIND_NOTIFICATION_BODY = "mail.negotiation.answer.remind.notification.body";

	/** キー: No.17 回答通知リマインド通知（行政（回答通知課）事前相談：回答通知担当、事前協議：①統括部署管理者、②各部署管理者、許可判定：許可判定通知担当課管理者）件名 */
	public static final String KEY_ANSWER_NOTIFICATION_REMIND_NOTIFICATION_SUBJECT = "mail.answer.notification.remind.notification.subject";
	/** キー: No.17 回答通知リマインド通知（行政（回答通知課）事前相談：回答通知担当、事前協議：①統括部署管理者、②各部署管理者、許可判定：許可判定通知担当課管理者）本文 */
	public static final String KEY_ANSWER_NOTIFICATION_REMIND_NOTIFICATION_BODY = "mail.answer.notification.remind.notification.body";

	/** キー: No.18 行政確定登録リマインド通知（行政　事前協議：①統括部署管理者、②各部署管理者、③各部署担当者）件名 */
	public static final String KEY_NEGOTIATION_CONFIRM_REMIND_NOTIFICATION_SUBJECT = "mail.negotiation.confirm.remind.notification.subject";
	/** キー: No.18 行政確定登録リマインド通知（行政　事前協議：①統括部署管理者、②各部署管理者、③各部署担当者）本文 */
	public static final String KEY_NEGOTIATION_CONFIRM_REMIND_NOTIFICATION_BODY = "mail.negotiation.confirm.remind.notification.body";

	/** キー: No.19 問合せリマインド通知（行政　回答担当課）件名 */
	public static final String KEY_INQUIRY_REMIND_NOTIFICATION_SUBJECT = "mail.inquiry.remind.notification.subject";
	/** キー: No.19 問合せリマインド通知（行政　回答担当課）本文 */
	public static final String KEY_INQUIRY_REMIND_NOTIFICATION_BODY = "mail.inquiry.remind.notification.body";

	/** キー: No.20 帳票受領通知（行政（許可判定回答権限部署管理者））件名 */
	public static final String KEY_LEDGER_RECEIPT_NOTIFICATION_SUBJECT = "mail.report.receipt.notification.subject";
	/** キー: No.20 帳票受領通知（行政（許可判定回答権限部署管理者））本文 */
	public static final String KEY_LEDGER_RECEIPT_NOTIFICATION_BODY = "mail.report.receipt.notification.body";
	
	/** キー: No.21 全リマインド通知(行政)件名 */
	public static final String KEY_ANSWER_ALLREMIND_SUBJECT = "mail.answer.all.remind.subject";
	/** キー: No.21 全リマインド通知(行政) 本文(申請段階) */
	public static final String KEY_ANSWER_ALL_REMIND_NOTIFICATION_STEP_BODY = "mail.answer.all.remind.notification.step.body";
	/** キー: No.21 全リマインド通知(行政) 本文(回答期限) */
	public static final String KEY_ANSWER_ALL_REMIND_DEADLINE_BODY = "mail.answer.all.remind.deadline.body";
	/** キー: No.21 全リマインド通知(行政) 本文(回答通知) */
	public static final String KEY_ANSWER_ALL_REMIND_NOTIFICATION_BODY = "mail.answer.all.remind.notification.body";
	/** キー: No.21 全リマインド通知(行政) 本文(行政確定登録未登録通知) */
	public static final String KEY_ANSWER_ALL_REMIND_NOTIFICATION_REGISTERED_BODY = "mail.answer.all.remind.notification.registered.body";
	/** キー: No.21 全リマインド通知(行政) 本文(問い合わせ未回答) */
	public static final String KEY_ANSWER_ALL_REMIND_CHAT_REGISTERED_BODY = "mail.answer.all.remind.chat.registered.body";
	/** キー: No.21 全リマインド通知(行政) 本文(x日前以内) */
	public static final String KEY_ANSWER_ALL_REMIND_NOTIFICATION_STEP_XDAYSBEFOREDUEDATE_BODY = "mail.answer.all.remind.notification.step.xDaysBeforeDueDate.body";
	/** キー: No.21 全リマインド通知(行政) 本文(期日超過) */
	public static final String KEY_ANSWER_ALL_REMIND_NOTIFICATION_STEP_OVERDUE_BODY = "mail.answer.all.remind.notification.step.overdue.body";
	/** キー: No.21 全リマインド通知(行政) 本文(期日超過・問い合わせ) */
	public static final String KEY_ANSWER_ALL_REMIND_CHAT_BODY = "mail.answer.all.remind.chat.body";
	/** キー: No.21 全リマインド通知(行政) 本文(末尾・リマインド) */
	public static final String KEY_ANSWER_ALL_REMIND_END_BODY = "mail.answer.all.remind.end.body";
	
	/** キー: No.22 全リマインド通知(事業者)件名 */
	public static final String KEY_ANSWER_ALLREMIND_BUSINESS_SUBJECT = "mail.answer.all.remind.business.subject";
	/** キー: No.22 全リマインド通知(事業者)本文(タイトル) */
	public static final String KEY_ANSWER_ALLREMIND_BUSINESS_TITLE_BODY = "mail.answer.all.remind.business.title.body";
	/** キー: No.22 全リマインド通知(事業者)本文 */
	public static final String KEY_ANSWER_ALLREMIND_BUSINESS_BODY = "mail.answer.all.remind.business.body";
	/** キー: No.22 全リマインド通知(事業者)末尾 */
	public static final String KEY_ANSWER_ALLREMIND_BUSINESS_END_BODY = "mail.answer.all.remind.business.end.body";

	/** キー: No.23 申請受付通知(行政（回答通知担当課）向け) （照合ID・パスワードを含む）件名 */
	public static final String KEY_ACCEPT_NOTIFICATION_LOGININFO_SUBJECT = "mail.application.accept.notification.login.info.subject";
	/** キー: No.23 申請受付通知(行政（回答通知担当課）向け)（照合ID・パスワードを含む） 本文 */
	public static final String KEY_ACCEPT_NOTIFICATION_LOGININFO_BODY = "mail.application.accept.notification.login.info.body";
	
	/** キー: 照合ID */
	public static final String KEY_COLLATION_ID = "${照合ID}";
	/** キー: パスワード */
	public static final String KEY_PASSWORD = "${パスワード}";
	/** キー: 申請者氏名 */
	public static final String KEY_NAME = "${申請者氏名}";
	/** キー: メールアドレス */
	public static final String KEY_MAIL_ADDRESS = "${申請者メールアドレス}";
	/** キー: 申請ID */
	public static final String KEY_APPLICATION_ID = "${申請ID}";
	/** キー: 申請種類 */
	public static final String KEY_APPLICATION_TYPE_NAME = "${申請種類}";
	/** キー: 申請段階 */
	public static final String KEY_APPLICATION_STEP_NAME = "${申請段階}";
	/** キー: 版番号 */
	public static final String KEY_VERSION_INFORMATION = "${版番号}";
	/** キー: 申請地番 */
	public static final String KEY_LOT_NUMBER = "${申請地番}";
	/** キー: 対象 */
	public static final String KEY_TARGET = "${対象}";
	/** キー: 判定結果 */
	public static final String KEY_RESULT = "${判定結果}";
	/** キー: 申請登録日時 */
	public static final String KEY_TIMESTAMP = "${申請登録日時}";
	/** キー: 回答日数 */
	public static final String KEY_ANSWER_DAYS = "${回答日数}";
	/** キー: 申請月 */
	public static final String KEY_APPLICATION_MONTH = "${申請月}";
	/** キー: 申請日 */
	public static final String KEY_APPLICATION_DAY = "${申請日}";
	/** キー: 回答対象 */
	public static final String KEY_ANSWER_TARGET = "${回答対象}";
	/** キー: 問い合わせ内容 */
	public static final String KEY_INQUIRY_CONTENT = "${問い合わせ内容}";
	/** キー: 部署名 */
	public static final String KEY_DEPARTMENT_NAME = "${部署名}";
	/** キー: 回答内容 */
	public static final String KEY_ANSWER_CONTENT  = "${回答内容}";
	/** キー: コメント１ */
	public static final String KEY_COMMENT_1  = "${コメント１}";
	/** キー: 帳票名 */
	public static final String KEY_LEDGER_NAME  = "${帳票名}";
	/** キー: 合意内容 */
	public static final String KEY_CONSENT_RESULT  = "${合意内容}";
	/** キー: 合否内容 */
	public static final String KEY_CONSENT_DATE  = "${合意日付}";
	/** キー: コメント */
	public static final String KEY_COMMENT  = "${コメント}";
	/** キー: 繰り返し開始 */
	public static final String KEY_REPEAT_START = "$rep[";
	/** キー: 繰り返し終了 */
	public static final String KEY_REPEAT_END = "]$rep";
	/** キー: 統括部署管理者の受付確認コメント */
	public static final String KEY_ACCEPT_CONTENT = "${統括部署管理者の受付確認コメント}";
	/** キー: 差し替えた申請ファイルの案内文 */
	public static final String KEY_APPLICATION_FILE_CHANGED_CONTENT = "${申請ファイル変更案内}";
	/** キー: 差し替えた申請ファイルの案内文 */
	public static final String KEY_APPLICATION_FILE_NAME = "${申請ファイル名}";
	/** キー: 差し替えた申請ファイルの案内文 */
	public static final String KEY_DIRECTION_DEPARTMENT_NAME = "${指示元担当課}";
	/** キー: 差し替えた申請ファイルの案内文 */
	public static final String KEY_REVISE_CONTENT = "${修正内容}";

	/** プロパティ */
	private static Properties PROP = null;

	/**
	 * コンストラクタ
	 * 
	 * @param resourcePath プロパティファイルのパス
	 * @throws Exception 例外
	 */
	public MailMessageUtil(String resourcePath) throws Exception {
		if (PROP == null) {
			// プロパティ読み込み
			loadProperties(resourcePath);
		}
	}

	/**
	 * プロパティの値を取得する
	 * 
	 * @param key キー
	 * @return 値
	 */
	public String getValue(String key) {
		return PROP.getProperty(key);
	}

	/**
	 * 整形済みの値を取得する
	 * 
	 * @param key キー
	 * @return 値
	 */
	public String getFormattedValue(String key, MailItem mailItem) {
		String text = getValue(key);
		text = replaceAll(text, KEY_COLLATION_ID, mailItem.getId()); // 照合ID
		text = replaceAll(text, KEY_PASSWORD, mailItem.getPassword()); // パスワード
		text = replaceAll(text, KEY_NAME, mailItem.getName()); // 申請者氏名
		text = replaceAll(text, KEY_MAIL_ADDRESS, mailItem.getMailAddress()); // 申請者メールアドレス
		text = replaceAll(text, KEY_APPLICATION_ID, mailItem.getApplicationId()); // 申請ID
		text = replaceAll(text, KEY_APPLICATION_TYPE_NAME, mailItem.getApplicationTypeName()); // 申請種類名
		text = replaceAll(text, KEY_APPLICATION_STEP_NAME, mailItem.getApplicationStepName()); // 申請段階名
		text = replaceAll(text, KEY_VERSION_INFORMATION, mailItem.getVersionInformation()); // 版番号
		text = replaceAll(text, KEY_LOT_NUMBER, mailItem.getLotNumber()); // 申請地番
		text = replaceAll(text, KEY_TIMESTAMP, mailItem.getTimestamp()); // 申請登録日時
		text = replaceAll(text, KEY_ANSWER_DAYS, mailItem.getAnswerDays()); // 回答日数
		text = replaceAll(text, KEY_APPLICATION_MONTH, mailItem.getApplicationMonth()); // 申請月
		text = replaceAll(text, KEY_APPLICATION_DAY, mailItem.getApplicationDay()); // 申請日
		text = replaceAll(text, KEY_ANSWER_TARGET, mailItem.getAnswerTarget()); // 回答対象
		text = replaceAll(text, KEY_INQUIRY_CONTENT, mailItem.getInquiryContent()); // 問い合わせ内容
		text = replaceAll(text, KEY_DEPARTMENT_NAME, mailItem.getDepartmentName()); // 部署名
		text = replaceAll(text, KEY_ANSWER_CONTENT, mailItem.getAnswerContent()); // 回答内容
		text = replaceAll(text, KEY_COMMENT_1, mailItem.getComment1()); // コメント１
		text = replaceAll(text, KEY_LEDGER_NAME, mailItem.getLedgerName()); // 帳票名
		text = replaceAll(text, KEY_ACCEPT_CONTENT, mailItem.getAcceptContent()); // 統括部署管理者の受付確認コメント(メール本文)
		text = replaceAll(text, KEY_APPLICATION_FILE_CHANGED_CONTENT, mailItem.getApplicationFileChangedContent()); // 差し替えた申請ファイルの案内文
		text = replaceAll(text, KEY_COMMENT, mailItem.getComment()); // 統括部署管理者の受付確認コメント
		text = getFormattedRepeatValue(text, mailItem.getResultList());
		return text;
	}

	/**
	 * 整形済みの文字列を取得する
	 * 
	 * @param text       文字列
	 * @param resultList 判定結果リスト
	 * @return 整形済み文字列
	 */
	private String getFormattedRepeatValue(String text, List<MailResultItem> resultList) {
		String tmpText = text;
		boolean repeatFlg = true;
		while (repeatFlg) {
			int repeatStartIndex = tmpText.indexOf(KEY_REPEAT_START);
			int repeatEndIndex = tmpText.indexOf(KEY_REPEAT_END);
			if (repeatStartIndex < 0 && repeatEndIndex < 0) {
				repeatFlg = false;
				continue;
			}
			if (repeatStartIndex > repeatEndIndex) {
				// 定義ミス
				LOGGER.error("繰り返し開始定義より前に繰り返し終了定義がある");
				throw new RuntimeException("繰り返し開始定義より前に繰り返し終了定義がある");
			}

			String repeatBeforeText = tmpText.substring(0, repeatStartIndex);
			String repeatText = tmpText.substring(repeatStartIndex + KEY_REPEAT_START.length(), repeatEndIndex);
			String repeatAfterText = tmpText.substring(repeatEndIndex + KEY_REPEAT_END.length());

			for (int i = 0; i < resultList.size(); i++) {
				MailResultItem resultItem = resultList.get(i);
				String tmpRepeatText = repeatText;
				tmpRepeatText = replaceAll(tmpRepeatText, KEY_TARGET, resultItem.getTarget()); // 対象
				tmpRepeatText = replaceAll(tmpRepeatText, KEY_RESULT, resultItem.getResult()); // 判定結果
				tmpRepeatText = replaceAll(tmpRepeatText, KEY_CONSENT_RESULT, resultItem.getConsentResult()); // 合意内容
				tmpRepeatText = replaceAll(tmpRepeatText, KEY_CONSENT_DATE, resultItem.getConsentDate()); // 合意日付
				tmpRepeatText = replaceAll(tmpRepeatText, KEY_COMMENT, resultItem.getComment()); // コメント
				tmpRepeatText = replaceAll(tmpRepeatText, KEY_APPLICATION_FILE_NAME, resultItem.getApplicationFileMasterName()); // 申請ファイル名
				tmpRepeatText = replaceAll(tmpRepeatText, KEY_DIRECTION_DEPARTMENT_NAME, resultItem.getDirectionDepartmentNames()); // 指示元担当課
				tmpRepeatText = replaceAll(tmpRepeatText, KEY_REVISE_CONTENT, resultItem.getReviseContent()); // 修正内容
				repeatBeforeText += tmpRepeatText;
			}
			repeatBeforeText += repeatAfterText;
			tmpText = repeatBeforeText;
		}
		return tmpText;
	}

	/**
	 * 指定キーの文字を全て置換する
	 * 
	 * @param text         文字列
	 * @param replaceKey   キー
	 * @param replaceValue 置換文字列
	 * @return 置換後文字列
	 */
	private String replaceAll(String text, String replaceKey, String replaceValue) {
		String tmpText = text;
		boolean differenceFlg = true;
		while (differenceFlg) {
			String afterText = tmpText.replace(replaceKey, replaceValue);
			if (tmpText.equals(afterText)) {
				// 変化がない = 全て置き換えした
				differenceFlg = false;
			}
			tmpText = afterText;
		}
		return tmpText;
	}

	/**
	 * プロパティ読み込み
	 * 
	 * @param resourcePath リソースパス
	 * @throws IOException 例外
	 */
	private void loadProperties(String resourcePath) throws Exception {
		try (InputStream is = new FileInputStream(resourcePath);
				InputStreamReader isr = new InputStreamReader(is, "UTF-8");
				BufferedReader reader = new BufferedReader(isr)) {
			PROP = new Properties();
			PROP.load(reader);
		}
	}
}
