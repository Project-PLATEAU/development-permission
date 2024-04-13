package developmentpermission.repository.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import developmentpermission.entity.AnswerFile;
import developmentpermission.form.AnswerFileForm;

/**
 * 回答ファイル履歴JDBC
 */
@Component
@Configuration
@ComponentScan
@EnableTransactionManagement
@Transactional
public class AnswerFileHistoryJdbc extends AbstractJdbc {
	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(AnswerFileHistoryJdbc.class);

	/**
	 * 回答ファイル履歴登録
	 * 
	 * @param answerFileId 回答ファイルID
	 * @param answerId     回答ID
	 * @param updateType   更新タイプ
	 * @param userId       ユーザID
	 * @return
	 */
	public Integer insert(int answerFileId, int answerId, int updateType, String userId) {
		LOGGER.debug("回答ファイル履歴登録 開始");
		try {
			String sql = "" + //
					"INSERT INTO o_answer_file_history " + //
					"(answer_file_history_id, answer_file_id, answer_id, update_type, update_user_id, update_datetime, notify_flag) "
					+ //
					"VALUES (nextval('seq_answer_file_history'), ?, ?, ?, ?, CURRENT_TIMESTAMP, '0')";
			jdbcTemplate.update(sql, //
					answerFileId, //
					answerId, //
					updateType, //
					userId //
			);
			return jdbcTemplate.queryForObject("SELECT lastval()", Integer.class);
		} finally {
			LOGGER.debug("回答ファイル履歴登録 終了");
		}
	}

	/**
	 * 回答ファイル履歴通知フラグ更新	
	 * @param answerFileId　回答ファイルID
	 * @return
	 */
	public int updateAnswerFileHistoryNotifyFlag(int answerFileId) throws Exception{
		LOGGER.debug("回答ファイル履歴通知フラグ更新 開始");
		try {
			// 最新1件が通知される内容になるので通知フラグを更新する
			String sql = "" + //
					"WITH max_answer_file_history AS " + //
					"(" + //
					"  SELECT a.answer_file_history_id " + //
					"  FROM o_answer_file_history AS a " + //
					"  INNER JOIN " + //
					"  (  " + //
					"     SELECT " + //
					"       answer_file_id, " + //
					"       MAX(update_datetime) AS max_v " + //
					"     FROM o_answer_file_history " + //
					"     GROUP BY answer_file_id " + //
					"  ) AS b" + //
					"  ON a.answer_file_id = b.answer_file_id AND a.update_datetime = b.max_v " + //
					"  WHERE a.answer_file_id = ? " + //
					")" + //
					"UPDATE o_answer_file_history SET notify_flag = '1' " + //
					"FROM max_answer_file_history " + //
					"WHERE max_answer_file_history.answer_file_history_id = o_answer_file_history.answer_file_history_id"
					+ //
					"";
			return jdbcTemplate.update(sql, //
					answerFileId);
		} finally {
			LOGGER.debug("回答ファイル履歴通知フラグ更新 終了");
		}
	}
}
