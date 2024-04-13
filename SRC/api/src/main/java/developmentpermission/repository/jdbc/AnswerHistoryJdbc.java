package developmentpermission.repository.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import developmentpermission.entity.Answer;
import developmentpermission.form.AnswerForm;

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
