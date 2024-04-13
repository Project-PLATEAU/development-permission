package developmentpermission.repository.jdbc;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

/**
 * チャットJDBC
 */
@Component
@Configuration
@ComponentScan
@EnableTransactionManagement
@Transactional
public class ChatJdbc extends AbstractJdbc {

	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(ChatJdbc.class);

	/**
	 * チャット登録
	 * 
	 * @param answerId 回答ID
	 * @return
	 */
	public Integer insert(int answerId) {
		LOGGER.debug("チャット登録 開始");
		try {
			String sql = "" + //
					"INSERT INTO o_chat ( " + //
					"  chat_id, " + //
					"  answer_id " + //
					") " + //
					"VALUES ( " + //
					"  nextval('seq_chat'), " + //
					"  ? " + // answer_id
					")";
			jdbcTemplate.update(sql, answerId);
			return jdbcTemplate.queryForObject("SELECT lastval()", Integer.class);
		} finally {
			LOGGER.debug("チャット登録 終了");
		}
	}

	/**
	 * チャット更新
	 * 
	 * @param chatId チャットID
	 * @return
	 */
	public int updateEstablishmentPostDatetime(Integer chatId, boolean isFirst) {
		LOGGER.debug("事業者投稿日時更新 開始");
		try {
			String sql = "" + //
					"UPDATE o_chat " + //
					"SET " + //
					"  establishment_post_datetime = CURRENT_TIMESTAMP ";//
			if (isFirst) {
				sql = sql + //
						"  , " + //
						"  establishment_first_post_datetime = CURRENT_TIMESTAMP ";//
			}
			sql = sql + //
					"WHERE " + //
					"  chat_id=?  ";

			return jdbcTemplate.update(sql, chatId);
		} finally {
			LOGGER.debug("事業者投稿日時更新 終了");
		}
	}

	/**
	 * チャット更新
	 * 
	 * @param chatId チャットID
	 * @return
	 */
	public int updateGovernmentAnswerDatetime(Integer chatId, String userId) {
		LOGGER.debug("行政回答日時更新 開始");
		try {
			String sql = "" + //
					"UPDATE o_chat " + //
					"SET " + //
					"  government_answer_datetime = CURRENT_TIMESTAMP , " + //
					"  last_answerer_id = ? " + //
					"WHERE " + //
					"  chat_id=?  ";

			return jdbcTemplate.update(sql, userId, chatId);
		} finally {
			LOGGER.debug("行政回答日時更新 終了");
		}
	}
}
