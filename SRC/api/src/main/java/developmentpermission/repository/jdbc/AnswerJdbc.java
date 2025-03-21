package developmentpermission.repository.jdbc;

import java.time.LocalDateTime;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import developmentpermission.entity.AcceptingAnswer;
import developmentpermission.entity.Answer;
import developmentpermission.entity.AnswerHistory;
import developmentpermission.form.AnswerForm;

/**
 * 回答情報JDBC
 */
@Component
@Configuration
@ComponentScan
@EnableTransactionManagement
@Transactional
public class AnswerJdbc extends AbstractJdbc {

	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(AnswerJdbc.class);

	/**
	 * 回答登録
	 * 
	 * @param applicationId     申請ID
	 * @param judgementId       判定項目ID
	 * @param judgementResult   判定結果
	 * @param answerContent     回答文言
	 * @param notifiedText      通知文言
	 * @param completeFlag      完了フラグ
	 * @param notifiedFlag      通知済みフラグ
	 * @param reapplicationFlag 再申請フラグ
	 * @return
	 */
	public Integer insert(int applicationId, String judgementId, String judgementResult, String answerContent,
			String notifiedText, char completeFlag, char notifiedFlag, String reapplicationFlag) {
		LOGGER.debug("回答登録 開始");
		try {
			String sql = "" + //
					"INSERT INTO o_answer ( " + //
					"  answer_id, " + //
					"  application_id, " + //
					"  judgement_id, " + //
					"  judgement_result, " + //
					"  register_datetime, " + //
					"  update_datetime, " + //
					"  complete_flag, " + //
					"  notified_flag, " + //
					"  answer_content, " + //
					"  notified_text, " + //
					"  re_application_flag, " + //
					"  business_reapplication_flag " + //
					") " + //
					"VALUES ( " + //
					"  nextval('seq_answer'), " + //
					"  ?, " + // applicationId
					"  ?, " + // judgementId
					"  ?, " + // judgementResult
					"  CURRENT_TIMESTAMP, " + //
					"  CURRENT_TIMESTAMP, " + //
					"  ?, " + // completeFlag
					"  ?, " + // notifiedFlag
					"  ?, " + // answerContent
					"  ?, " + // notifiedText
					"  ?, " + // re_application_flag
					"  ?, " + // business_reapplication_flag
					"  '0' , " + // delete_unnotified_flag
					"  '0' " + // delete_flag
					")";
			jdbcTemplate.update(sql, //
					applicationId, //
					judgementId, //
					judgementResult, completeFlag, notifiedFlag, answerContent, notifiedText, reapplicationFlag,
					reapplicationFlag);
			return jdbcTemplate.queryForObject("SELECT lastval()", Integer.class);
		} finally {
			LOGGER.debug("回答登録 終了");
		}
	}

	/**
	 * O_回答登録
	 * 
	 * @param answer
	 * @return
	 */
	public Integer insert(Answer answer) {
		LOGGER.debug("回答登録 開始");
		try {
			String sql = "" + //
					"INSERT INTO o_answer ( " + //
					"  answer_id, " + //
					"  application_id, " + //
					"  application_step_id, " + //
					"  judgement_id, " + //
					"  judgement_result_index, " + //
					"  department_answer_id, " + //
					"  department_id, " + //
					"  judgement_result, " + //
					"  answer_content, " + //
					"  notified_text, " + //
					"  register_datetime, " + //
					"  update_datetime, " + //
					"  complete_flag, " + //
					"  notified_flag, " + //
					"  answer_update_flag, " + //
					"  re_application_flag, " + //
					"  business_reapplication_flag, " + //
					"  discussion_flag, " + //
					"  discussion_item, " + //
					"  answer_status, " + //
					"  answer_data_type, " + //
					"  register_status, " + //
					"  version_information, " + //
					"  delete_unnotified_flag, " + //
					"  delete_flag, " + //
					"  answer_permission_flag, " + //
					"  government_confirm_permission_flag, " + //
					"  permission_judgement_migration_flag, " + //
					"  deadline_datetime " + //
					") " + //
					"VALUES ( " + //
					"  nextval('seq_answer'), " + //
					"  ?, " + // applicationId
					"  ?, " + // applicationStepId
					"  ?, " + // judgementId
					"  ?, " + // judgementResultIndex
					"  ?, " + // departmentAnswerId
					"  ?, " + // departmentId
					"  ?, " + // judgementResult
					"  ?, " + // answerContent
					"  ?, " + // notifiedText
					"  CURRENT_TIMESTAMP, " + //
					"  CURRENT_TIMESTAMP, " + //
					"  ?, " + // completeFlag
					"  ?, " + // notifiedFlag
					"  ?, " + // answer_update_flag
					"  ?, " + // reApplicationFlag
					"  ?, " + // businessReApplicationFlag
					"  ?, " + // discussionFlag
					"  ?, " + // discussion_item
					"  ?, " + // answerStatus
					"  ?, " + // answerDataType
					"  ?, " + // registerStatus
					"  ?, " + // delete_unnotified_flag
					"  '0', " + // delete_unnotified_flag
					"  '0', " + // delete_flag
					"  '0', " + // answer_permission_flag
					"  '0', " + // government_confirm_permission_flag
					"  '0',  " + // permission_judgement_migration_flag
					"  ?" + // deadlineDatetime
					")";
			jdbcTemplate.update(sql, //
					answer.getApplicationId(), //
					answer.getApplicationStepId(), //
					answer.getJudgementId(), //
					answer.getJudgementResultIndex(), //
					answer.getDepartmentAnswerId(), //
					answer.getDepartmentId(), //
					answer.getJudgementResult(), //
					answer.getAnswerContent(), //
					answer.getNotifiedText(), //
					convertBooleanToString(answer.getCompleteFlag()), //
					convertBooleanToString(answer.getNotifiedFlag()), //
					convertBooleanToString(answer.getAnswerUpdateFlag()), //
					convertBooleanToString(answer.getReApplicationFlag()), //
					convertBooleanToString(answer.getBusinessReApplicationFlag()), //
					convertBooleanToString(answer.getDiscussionFlag()), //
					answer.getDiscussionItem(), //
					answer.getAnswerStatus(), //
					answer.getAnswerDataType(), //
					answer.getRegisterStatus(), //
					answer.getVersionInformation(), //
					answer.getDeadlineDatetime());
			return jdbcTemplate.queryForObject("SELECT lastval()", Integer.class);
		} finally {
			LOGGER.debug("回答登録 終了");
		}
	}

	/**
	 * 回答更新
	 * 
	 * @param answerForm        回答フォーム
	 * @param applicationStepId 申請段階ID
	 * @param completeFlag      完了フラグ
	 * @return 更新件数
	 */
	public int updateForAnswerContent(AnswerForm answerForm, int applicationStepId, boolean completeFlag) {
		LOGGER.debug("回答更新 開始");
		try {
			int updatedCount = 0;
			// 事前相談
			if (applicationStepId == 1) {

				String sql = "" + //
						"UPDATE " + //
						"  o_answer " + //
						"SET " + //
						"  answer_content=?, "; //
				if (completeFlag) {
					sql += "  complete_flag='1', "; // 「1:完了」
				} else {
					sql += "  complete_flag='0', "; // 「0:未完了」
				}
				sql += "" + //
						"  re_application_flag=?, " + //
						"  discussion_flag=?, " + // 事前協議フラグ
						"  answer_update_flag='1' , " + // 回答登録時は「1：変更あり」固定
						"  update_datetime=CURRENT_TIMESTAMP, " + //
						"  answer_status='1', " + // 回答ステータス: 回答登録時は「1:回答済み」固定
						"  answer_data_type=? " + // データ種類
						"WHERE " + //
						"  answer_id=?" + //
						"  AND update_datetime=?";
				updatedCount = jdbcTemplate.update(sql, //
						answerForm.getAnswerContent(), //
						convertBooleanToString(answerForm.getReApplicationFlag()), //
						convertBooleanToString(answerForm.getDiscussionFlag()), //
						answerForm.getAnswerDataType(), answerForm.getAnswerId(), //
						answerForm.getUpdateDatetime());

			}
			// 事前協議
			if (applicationStepId == 2) {
				String sql = "" + //
						"UPDATE " + //
						"  o_answer " + //
						"SET " + //
						"  answer_content=?, "; //
				if (completeFlag) {
					sql += "  complete_flag='1', "; // 「1:完了」
				} else {
					sql += "  complete_flag='0', "; // 「0:未完了」
				}
				sql += "" + //
						"  discussion_item=?, " + // 協議対象
						"  answer_update_flag='1' , " + // 回答登録時は「1：変更あり」固定
						"  update_datetime=CURRENT_TIMESTAMP, " + //
						"  answer_status='2', " + // 回答ステータス: 回答登録時は「2:承認まち」固定
						"  answer_data_type=? " + // データ種類
						"WHERE " + //
						"  answer_id=?" + //
						"  AND update_datetime=?";
				updatedCount = jdbcTemplate.update(sql, //
						answerForm.getAnswerContent(), //
						answerForm.getDiscussionItem(), //
						answerForm.getAnswerDataType(), //
						answerForm.getAnswerId(), //
						answerForm.getUpdateDatetime());

			}
			// 許可判定
			if (applicationStepId == 3) {

				String sql = "" + //
						"UPDATE " + //
						"  o_answer " + //
						"SET " + //
						"  answer_content=?, "; //
				if (completeFlag) {
					sql += "  complete_flag='1', "; // 「1:完了」
				} else {
					sql += "  complete_flag='0', "; // 「0:未完了」
				}
				sql += "" + //
						"  re_application_flag=?, " + //
						"  permission_judgement_result=?, " + // 判定結果
						"  answer_update_flag='1' , " + // 回答登録時は「1：変更あり」固定
						"  update_datetime=CURRENT_TIMESTAMP, " + //
						"  answer_status='1', " + // 回答ステータス: 回答登録時は「1:回答済み」固定
						"  answer_data_type=? " + // データ種類
						"WHERE " + //
						"  answer_id=?" + //
						"  AND update_datetime=?";
				updatedCount = jdbcTemplate.update(sql, //
						answerForm.getAnswerContent(), //
						convertBooleanToString(answerForm.getReApplicationFlag()), //
						answerForm.getPermissionJudgementResult(), //
						answerForm.getAnswerDataType(), //
						answerForm.getAnswerId(), //
						answerForm.getUpdateDatetime());
			}
			return updatedCount;
		} finally {
			LOGGER.debug("回答更新 終了");
		}
	}

	/**
	 * 回答更新（同意項目承認否認登録）
	 * 
	 * @param answerForm パラメータ
	 * @return 更新件数
	 */
	public int updateConsent(AnswerForm answerForm, LocalDateTime businessAnswerDatetime) {
		LOGGER.debug("回答更新（同意項目承認否認登録） 開始");
		try {
			String sql = "" + //
					"UPDATE " + //
					"  o_answer " + //
					"SET " + //
					"  business_pass_status=?, " + //
					"  answer_status=?, " + //
					"  business_answer_datetime=?, " + //
					"  update_datetime=CURRENT_TIMESTAMP " + //
					"WHERE " + //
					"  answer_id=?" + //
					"  AND update_datetime=?";
			return jdbcTemplate.update(sql, //
					answerForm.getBusinessPassStatus(), //
					answerForm.getAnswerStatus(), // 回答ステータス
					businessAnswerDatetime,
					answerForm.getAnswerId(), //
					answerForm.getUpdateDatetime());
		} finally {
			LOGGER.debug("回答更新（同意項目承認否認登録） 終了");
		}
	}

	/**
	 * 回答情報取得
	 * 
	 * @param answerId パラメータ
	 * @return 
	 */
	public Map<String, Object> selectAnswer(Integer answerId) {
		LOGGER.debug("回答情報検索 開始");
		try {
			String sql = "SELECT * FROM o_answer WHERE answer_id = ?";
			Map<String, Object> result = jdbcTemplate.queryForMap(sql,
					answerId);
			return result;
		} finally {
			LOGGER.debug("回答情報検索 終了");
		}
	}
	
	/**
	 * 回答更新-削除・未通知フラグ(事前協議のみ)
	 * 
	 * @param answerForm 回答フォーム
	 * @return 更新件数
	 */
	public int updateDeleteUnnotifiedFlag(AnswerForm answerForm) {
		LOGGER.debug("回答更新 開始");
		try {
			String sql = "" + //
					"UPDATE " + //
					"  o_answer " + //
					"SET " + //
					"  answer_data_type='7', " + // 回答削除時に、「7：削除済み（行政）」
					"  delete_unnotified_flag='1', " + //
					"  answer_update_flag='1' , " + // 回答登録時は「1：変更あり」固定
					"  update_datetime=CURRENT_TIMESTAMP " + //
					"WHERE " + //
					"  answer_id=?" + //
					"  AND update_datetime=?";
			return jdbcTemplate.update(sql, //
					answerForm.getAnswerId(), //
					answerForm.getUpdateDatetime());
		} finally {
			LOGGER.debug("回答更新 終了");
		}
	}

	/**
	 * 回答更新(事前協議の行政確定内容)
	 * 
	 * @param answerForm パラメータ
	 * @return 更新件数
	 */
	public int updateForGovernmentContent(AnswerForm answerForm, LocalDateTime governmentConfirmDateTime) {
		LOGGER.debug("回答更新 開始");
		try {

			String sql = "" + //
					"UPDATE " + //
					"  o_answer " + //
					"SET " + //
					"  government_confirm_status=?, " + //
					"  government_confirm_datetime=?, " + //
					"  government_confirm_comment=?, " + //
					"  answer_status=? ," + //
					"  answer_update_flag='1' , " + // 回答登録時は「1：変更あり」固定
					"  update_datetime=CURRENT_TIMESTAMP, " + //
					"  answer_data_type=? " + // データ種類
					"WHERE " + //
					"  answer_id=?" + //
					"  AND update_datetime=?";
			return jdbcTemplate.update(sql, //
					answerForm.getGovernmentConfirmStatus(), //
					governmentConfirmDateTime, //
					answerForm.getGovernmentConfirmComment(), //
					answerForm.getAnswerStatus(), //
					answerForm.getAnswerDataType(), //
					answerForm.getAnswerId(), //
					answerForm.getUpdateDatetime());
		} finally {
			LOGGER.debug("回答更新 終了");
		}
	}

	/**
	 * 回答内容を通知テキストに設定
	 * 
	 * @param answerForm パラメータ
	 * @return 更新件数
	 */
	public int copyNotifyText(Answer answer) {
		LOGGER.debug("回答通知設定 開始");
		try {
			String sql = "" + //
					"UPDATE " + //
					"  o_answer " + //
					"SET " + //
					"  notified_text=?, " + //
					"  notified_flag='1'," + // 通知フラグに1を設定
					"  business_reapplication_flag = ?, " + //
					"  answer_update_flag = '0', " + // 回答変更フラグに1を設定
					"  government_confirm_notified_flag = ?, " + //
					"  answer_permission_flag = ?, " + //回答許可通知フラグ
					"  update_datetime=CURRENT_TIMESTAMP " + //
					"WHERE " + //
					"  answer_id=?" + //
					"  AND update_datetime=?";
			return jdbcTemplate.update(sql, //
					answer.getAnswerContent(), //
					convertBooleanToString(answer.getReApplicationFlag()), // , //
					convertBooleanToString(answer.getGovernmentConfirmNotifiedFlag()), //
					convertBooleanToString(answer.getAnswerPermissionFlag()), //回答許可通知フラグ
					answer.getAnswerId(), //
					answer.getUpdateDatetime());
		} finally {
			LOGGER.debug("回答通知設定 終了");
		}
	}

	/**
	 * 事業者再申請フラグと完了フラグをリセット
	 * 
	 * @param answer パラメータ
	 * @return 更新件数
	 */
	public int resetBuinessReapplicationFlag(Answer answer, int answerDays) {
		LOGGER.debug("再申請フラグリセット 開始");
		try {
			String sql = "" + //
					"UPDATE " + //
					"  o_answer " + //
					"SET " + //
					"  business_reapplication_flag = '0', " + // 事業者再申請フラグをリセット
					"  complete_flag = '0', " + // 完了フラグをリセット
					"  answer_status = '0', " + // 回答ステータスが未回答にリセット
					"  answer_data_type = ?, " + // 回答種類が更新にリセット
					"  update_datetime=CURRENT_TIMESTAMP, " + //
					"  deadline_datetime = CURRENT_TIMESTAMP + '" + answerDays + " Day' " + //
					"WHERE " + //
					"  answer_id=?" + //
					"  AND update_datetime=?";
			return jdbcTemplate.update(sql, //
					answer.getAnswerDataType(), answer.getAnswerId(), //
					answer.getUpdateDatetime());
		} finally {
			LOGGER.debug("再申請フラグリセット 終了");
		}
	}

	/**
	 * 回答削除(論理削除)
	 * 
	 * @param answer パラメータ
	 * @return 更新件数
	 */
	public int updateDeleteFlag(Answer answer) {
		LOGGER.debug("回答削除 開始");
		try {
			// 履歴で参照するため、論理削除する
			String sql = "" + //
					"UPDATE " + //
					"  o_answer " + //
					"SET " + //
					"  delete_flag = '1' , " + // 削除フラグを「1：削除済み」に更新
					"  answer_update_flag='0' " + // 回答通知時は「0：変更なし」固定
					"WHERE " + //
					"  answer_id=?" + //
					"  AND update_datetime=?";
			return jdbcTemplate.update(sql, //
					answer.getAnswerId(), //
					answer.getUpdateDatetime());
		} finally {
			LOGGER.debug("回答削除 終了");
		}
	}

	/**
	 * 回答削除（物理削除）
	 * 
	 * @param answer パラメータ
	 * @return 更新件数
	 */
	public int deleteAnswer(Answer answer) {
		LOGGER.debug("回答削除 開始");
		try {
			// 再申請エラーデータ消去のため、物理削除
			String sql = "" + //
					"DELETE FROM " + //
					"  o_answer " + //
					"WHERE " + //
					"  answer_id=?" + //
					"  AND update_datetime=?";
			return jdbcTemplate.update(sql, //
					answer.getAnswerId(), //
					answer.getUpdateDatetime());
		} finally {
			LOGGER.debug("回答削除 終了");
		}
	}

	/**
	 * 回答更新（事前協議の再申請）
	 * 
	 * @param answer パラメータ
	 * @return 更新件数
	 */
	public int resetBuinesscontent(Answer answer, int answerDays) {
		LOGGER.debug("回答更新 開始");
		try {
			String sql = "" + //
					"UPDATE  " + //
					"  o_answer " + //
					"SET " + //
					"  business_pass_status = '', " + // 事業者合否ステータスをリセット
					"  business_pass_comment = '', " + // 事業者合否コメントをリセット
					"  government_confirm_notified_flag = '0', " + // 行政確定通知フラグを未通知にリセット
					"  answer_status= '2', " + // 回答ステータスを承認待ちにリセット
					"  answer_data_type = ?, " + // 回答種類が更新にリセット
					"  answer_update_flag = '1', " + // 回答内容変更がありになる
					"  update_datetime=CURRENT_TIMESTAMP , " + //
					"  deadline_datetime = CURRENT_TIMESTAMP + '" + answerDays + " Day' " + //
					"WHERE " + //
					"  answer_id=?" + //
					"  AND update_datetime=?";
			return jdbcTemplate.update(sql, //
					answer.getAnswerDataType(), //
					answer.getAnswerId(), //
					answer.getUpdateDatetime());
		} finally {
			LOGGER.debug("回答更新 終了");
		}
	}

	/**
	 * 回答更新（データ種類）
	 * 
	 * @param answer パラメータ
	 * @return 更新件数
	 */
	public int resetDataType(Answer answer, String dataType) {
		LOGGER.debug("回答更新 開始");
		try {
			String sql = "" + //
					"UPDATE  " + //
					"  o_answer " + //
					"SET " + //
					"  answer_data_type = ? " + // 回答種類
					"WHERE " + //
					"  answer_id=?" + //
					"  AND update_datetime=?";
			return jdbcTemplate.update(sql, //
					dataType, answer.getAnswerId(), answer.getUpdateDatetime());
		} finally {
			LOGGER.debug("回答更新 終了");
		}
	}

	/**
	 * 回答更新（登録ステータス）
	 * 
	 * @param answer パラメータ
	 * @return 更新件数
	 */
	public int updateRegisterStatus(Answer answer) {
		LOGGER.debug("回答更新 開始");
		try {
			String sql = "" + //
					"UPDATE  " + //
					"  o_answer " + //
					"SET " + //
					"  register_status = '1' " + // 登録ステータス
					"WHERE " + //
					"  answer_id=?" + //
					"  AND update_datetime=?";
			return jdbcTemplate.update(sql, //
					answer.getAnswerId(), answer.getUpdateDatetime());
		} finally {
			LOGGER.debug("回答更新 終了");
		}
	}

	/**
	 * 事業者再申請フラグと完了フラグをリセット
	 * 
	 * @param answer パラメータ
	 * @return 更新件数
	 */
	public int resetReapplicationAnswerContent(AnswerHistory answerHistory, boolean completeFlag,
			LocalDateTime updateDatetime) {
		LOGGER.debug("再申請フラグリセット 開始");
		try {
			String sql = "" + //
					"UPDATE " + //
					"  o_answer " + //
					"SET " + //
					"  business_reapplication_flag = ?, " + // 事業者再申請フラグをリセット
					"  complete_flag = ?, " + // 完了フラグをリセット
					"  answer_status = ?, " + // 回答ステータスが未回答にリセット
					"  answer_data_type = ?, " + // 回答種類が更新にリセット
					"  update_datetime= ?, " + //
					"  deadline_datetime = ? " + //
					"WHERE " + //
					"  answer_id=?" + //
					"  AND update_datetime=?";
			return jdbcTemplate.update(sql, //
					convertBooleanToString(answerHistory.getReApplicationFlag()), convertBooleanToString(completeFlag),
					answerHistory.getAnswerStatus(), //
					answerHistory.getAnswerDataType(), answerHistory.getUpdateDatetime(),
					answerHistory.getDeadlineDatetime(), answerHistory.getAnswerId(), updateDatetime);
		} finally {
			LOGGER.debug("再申請フラグリセット 終了");
		}
	}

	/**
	 * O_受付回答からO_回答にコピー登録（事前協議のみ）
	 * 
	 * @param acceptingAnswer    O_受付回答
	 * @param departmentAnswerId 部署ID
	 * 
	 * @return 更新件数
	 */
	public int insertCopyAcceptingAnswer(AcceptingAnswer acceptingAnswer, Integer departmentAnswerId) {
		LOGGER.debug("O_受付回答からO_回答にコピー登録 開始");
		try {
			String sql = "" + //
					"INSERT " + //
					"INTO o_answer( " + //
					"  answer_id " + //
					"  , application_id " + //
					"  , application_step_id " + //
					"  , judgement_id " + //
					"  , department_answer_id " + //
					"  , department_id " + //
					"  , judgement_result " + //
					"  , answer_content " + //
					"  , register_datetime " + //
					"  , update_datetime " + //
					"  , complete_flag " + //
					"  , notified_flag " + //
					"  , answer_update_flag " + //
					"  , discussion_item " + //
					"  , government_confirm_notified_flag " + //
					"  , answer_status " + //
					"  , answer_data_type " + //
					"  , register_status " + //
					"  , delete_unnotified_flag " + //
					"  , deadline_datetime " + //
					"  , judgement_result_index " + //
					"  , delete_flag " + //
					"  , answer_permission_flag " + //
					"  , government_confirm_permission_flag " + //
					"  , permission_judgement_migration_flag " + //
					"  , version_information " + //
					") " + //
					"SELECT " + //
					"  nextval('seq_answer') AS answer_id " + //
					"  , a.application_id AS application_id " + //
					"  , a.application_step_id AS application_step_id " + //
					"  , a.judgement_id AS judgement_id " + //
					"  , " + departmentAnswerId + " AS department_answer_id " + //
					"  , a.department_id AS department_id " + //
					"  , a.judgement_result AS judgement_result " + //
					"  , a.answer_content AS answer_content " + //
					"  , CURRENT_TIMESTAMP AS register_datetime " + //
					"  , CURRENT_TIMESTAMP AS update_datetime " + //
					"  , ( " + //
					"    CASE coalesce(a.answer_content, '') " + //
					"      WHEN '' THEN '0' " + // 回答内容が空の場合、0：未完了で登録
					"      ELSE '1' " + // 回答内容が空以外の場合、1：完了で登録
					"      END" + //
					"  ) AS complete_flag" + // 完了フラ
					"  , '0' AS notified_flag" + // 回答内容通知フラグ0：未通知
					"  , ( " + //
					"    CASE coalesce(a.answer_content, '') " + //
					"      WHEN '' THEN '0' " + // 回答内容が空の場合、0：回答変更なしで登録
					"      ELSE '1' " + // 回答内容が空以外の場合、1：回答変更ありで登録
					"      END" + //
					"  ) AS answer_update_flag" + // 回答変更フラグ
					"  , '' AS discussion_item" + //
					"  , '0' AS government_confirm_notified_flag" + // 行政確定登録通知フラグ：0：未通知
					"  , ( " + //
					"    CASE coalesce(a.answer_content, '') " + //
					"      WHEN '' THEN '0' " + // 回答内容が空の場合、0：未回答で登録
					"      ELSE '2' " + // 回答内容が空以外の場合、2：承認待ちで登録
					"      END" + //
					"  ) AS answer_status" + // 回答ステータス
					"  , a.answer_data_type AS answer_data_type" + //
					"  , '1' AS register_status" + // 登録ステータス
					"  , '0' AS delete_unnotified_flag" + //
					"  , a.deadline_datetime AS deadline_datetime" + //
					"  , a.judgement_result_index AS judgement_result_index" + //
					"  , '0' AS delete_flag" + //
					"  , '0' AS answer_permission_flag" + // 回答通知許可フラグ0：未許可
					"  , '0' AS government_confirm_permission_flag" + // 行政確定登録通知許可フラグ0：未許可
					"  , '0' AS permission_judgement_migration_flag " + //
					"  , a.version_infomation as version_information " + //
					"FROM" + //
					"  o_accepting_answer AS a " + //
					"WHERE" + //
					"  a.accepting_answer_id = ? ";

			jdbcTemplate.update(sql, //
					acceptingAnswer.getAcceptingAnswerId());
			return jdbcTemplate.queryForObject("SELECT lastval()", Integer.class);

		} finally {
			LOGGER.debug("O_受付回答からO_回答にコピー登録 終了");
		}
	}

	/**
	 * 回答の許可判定移行フラグ更新（事前協議のみ）
	 * 
	 * @param answer パラメータ
	 * @return 更新件数
	 */
	public int updatePermissionJudgementMigrationFlag(Integer answerId) {
		LOGGER.debug("許可判定移行フラグ更新 開始");
		try {
			String sql = "" + //
					"UPDATE  " + //
					"  o_answer " + //
					"SET " + //
					"  permission_judgement_migration_flag = '1', " + // 1:許可判定移行時チェックしない
					"  update_datetime= CURRENT_TIMESTAMP " + //
					"WHERE " + //
					"  answer_id=?";
			return jdbcTemplate.update(sql, answerId);
		} finally {
			LOGGER.debug("許可判定移行フラグ更新 終了");
		}
	}

	/**
	 * 回答の回答通知許可フラグ更新（事前協議のみ）
	 * 
	 * @param answer パラメータ
	 * @return 更新件数
	 */
	public int updateAnswerPermissionFlag(AnswerForm answerForm) {
		LOGGER.debug("回答通知許可フラグ更新 開始");
		try {
			String sql = "" + //
					"UPDATE  " + //
					"  o_answer " + //
					"SET " + //
					"  answer_permission_flag = '1', " + // 1:許可済み
					"  update_datetime= CURRENT_TIMESTAMP " + //
					"WHERE " + //
					"  answer_id=?" + //
					"  AND update_datetime=?";
			return jdbcTemplate.update(sql, //
					answerForm.getAnswerId(), //
					answerForm.getUpdateDatetime());
		} finally {
			LOGGER.debug("回答通知許可フラグ更新 終了");
		}
	}

	/**
	 * 回答の行政確定通知許可フラグ更新（事前協議のみ）
	 * 
	 * @param answer パラメータ
	 * @return 更新件数
	 */
	public int updateGovernmentConfirmPermissionFlag(AnswerForm answerForm) {
		LOGGER.debug("行政確定通知許可フラグ更新 開始");
		try {
			String sql = "" + //
					"UPDATE  " + //
					"  o_answer " + //
					"SET " + //
					"  government_confirm_permission_flag = '1', " + // 1:許可済み
					"  update_datetime= CURRENT_TIMESTAMP " + //
					"WHERE " + //
					"  answer_id=?" + //
					"  AND update_datetime=?";
			return jdbcTemplate.update(sql, //
					answerForm.getAnswerId(), //
					answerForm.getUpdateDatetime());
		} finally {
			LOGGER.debug("行政確定通知許可フラグ更新 終了");
		}
	}

	/**
	 * Booleanの値を文字列に転換
	 * 
	 * @param value
	 * @return
	 */
	private String convertBooleanToString(Boolean value) {

		String flag = null;

		if (value != null) {

			flag = (value) ? "1" : "0";
		}

		return flag;
	}
}
