package developmentpermission.repository.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

/**
 * 回答履歴情報JDBC
 */
@Component
@Configuration
@ComponentScan
@EnableTransactionManagement
@Transactional
public class AnswerHistoryJdbc extends AbstractJdbc {
	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(AnswerJdbc.class);

	/**
	 * 回答履歴登録
	 * 
	 * @param answerId      回答ID
	 * @param answerUserId  回答者ID
	 * @param answerContent 回答内容
	 * @return
	 */
	public Integer insert(int answerId, String answerUserId, boolean notifiedFlag) {
		LOGGER.debug("回答履歴登録 開始");
		try {
			String sql = "" + //
					"INSERT INTO o_answer_history( " + //
					"  answer_history_id,  " + //
					"  answer_id,  " + //
					"  answer_user_id,  " + //
					"  answer_datetime,  " + //
					"  answer_text,  " + //
					"  notify_flag,  " + //
					"  discussion_item,  " + //
					"  business_pass_status,  " + //
					"  business_pass_comment,  " + //
					"  government_confirm_status,  " + //
					"  government_confirm_datetime,  " + //
					"  government_confirm_comment,  " + //
					"  permission_judgement_result,  " + //
					"  re_application_flag,  " + //
					"  discussion_flag,  " + //
					"  answer_status,  " + //
					"  answer_data_type,  " + //
					"  update_datetime,  " + //
					"  deadline_datetime " + //
					")  " + //
					"SELECT " + //
					"  nextval('seq_answer_history') AS answer_history_id,  " + //
					"  oa.answer_id AS answer_id,  "; //
			sql += "  '" + answerUserId + "' AS answer_user_id,  "; //
			sql += "  CURRENT_TIMESTAMP AS answer_datetime,  " + //
					"  oa.answer_content AS answer_text,  "; //

			if ("-1".equals(answerUserId) && notifiedFlag) {
				sql += "  '1' AS notify_flag,  "; // 自動回答済の場合、履歴の通知フラグが「1」固定
			} else {
				sql += "  '0' AS notify_flag,  "; //
			}
			sql += "  oa.discussion_item AS discussion_item,  " + //
					"  oa.business_pass_status AS business_pass_status,  " + //
					"  oa.business_pass_comment AS business_pass_comment,  " + //
					"  oa.government_confirm_status AS government_confirm_status,  " + //
					"  oa.government_confirm_datetime AS government_confirm_datetime,  " + //
					"  oa.government_confirm_comment AS government_confirm_comment,  " + //
					"  oa.permission_judgement_result AS permission_judgement_result,  " + //
					"  oa.re_application_flag AS re_application_flag,  " + //
					"  oa.discussion_flag AS discussion_flag,  " + //
					"  oa.answer_status AS answer_status,  " + //
					"  oa.answer_data_type AS answer_data_type,  " + //
					"  oa.update_datetime AS update_datetime,  " + //
					"  oa.deadline_datetime AS deadline_datetime  " + //
					"FROM " + //
					"  o_answer AS oa  " + //
					"WHERE " + //
					"  oa.answer_id = " + answerId;
			return jdbcTemplate.update(sql);
		} finally {
			LOGGER.debug("回答履歴登録 終了");
		}
	}

	/**
	 * 回答履歴登録
	 * 
	 * @param answerId      回答ID
	 * @param answerUserId  回答者ID
	 * @param answerContent 回答内容
	 * @return
	 */
	public Integer insert(int answerId, String answerUserId, String answerContent) {
		LOGGER.debug("回答履歴登録 開始");
		try {
			String sql = "" + //
					"INSERT INTO o_answer_history( " + //
					"         answer_history_id, " + //
					"         answer_id, " + //
					"	      answer_user_id, " + //
					"    	  answer_datetime, " + //
					"         answer_text, " + //
					"         notify_flag " + //
					"         ) " + //
					"         VALUES " + //
					"         ( " + //
					"         nextval('seq_answer_history'), " + //
					"         ?, " + //
					"         ?, " + //
					"         CURRENT_TIMESTAMP, " + //
					"         ?," + //
					"         '0' " + //
					"         )";
			return jdbcTemplate.update(sql, //
					answerId, //
					answerUserId, //
					answerContent);
		} finally {
			LOGGER.debug("回答履歴登録 終了");
		}
	}

	/**
	 * 回答履歴通知フラグを更新する
	 * 
	 * @param answerId 回答ID
	 * @return
	 */
	public int updateAnswerHistoryNotifyFlag(int answerId) throws Exception {
		LOGGER.debug("回答履歴通知フラグ更新 開始");
		try {
			// 最新1件が通知される内容になるので通知フラグを更新する
			String sql = "" + //
					"WITH max_answer_history AS" + //
					"( " + //
					"  SELECT a.answer_history_id " + //
					"  FROM  o_answer_history AS a " + //
					"  INNER JOIN " + //
					"   ( " + //
					"     SELECT " + //
					"       answer_id, " + //
					"       MAX(answer_datetime) AS max_v " + //
					"     FROM " + //
					"       o_answer_history " + //
					"     GROUP BY answer_id " + //
					"   ) AS b " + //
					"   ON a.answer_id = b.answer_id AND a.answer_datetime = b.max_v " + //
					"   WHERE a.answer_id = ?" + //
					") " + //
					"UPDATE o_answer_history SET notify_flag = '1' " + //
					"FROM max_answer_history " + //
					"WHERE max_answer_history.answer_history_id = o_answer_history.answer_history_id " + //
					"";
			return jdbcTemplate.update(sql, //
					answerId);
		} finally {
			LOGGER.debug("回答履歴通知フラグ更新 終了");
		}
	}

}
