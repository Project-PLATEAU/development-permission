package developmentpermission.repository.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import developmentpermission.entity.Message;

/**
 * メッセージJDBC
 */
@Component
@Configuration
@ComponentScan
@EnableTransactionManagement
@Transactional
public class MessageJdbc extends AbstractJdbc {

	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(MessageJdbc.class);

	/**
	 * O_メッセージ登録
	 * 
	 * @param message メッセージ情報
	 * @return
	 */
	public Integer insert(Message message) {
		LOGGER.debug("O_メッセージ登録 開始");
		try {
			String sql = "" + //
					"INSERT INTO o_message ( " + //
					"  message_id, " + //
					"  chat_id, " + //
					"  message_type, " + //
					"  sender_id, " + //
					"  to_department_id, " + //
					"  message_text, " + //
					"  send_datetime, " + //
					"  read_flag, " + //
					"  answer_complete_flag " + //
					") " + //
					"VALUES ( " + //
					"  nextval('seq_message'), " + //
					"  ?, " + // chat_id
					"  ?, " + // message_type
					"  ?, " + // sender_id
					"  ?, " + // to_department_id
					"  ?, " + // message_text
					"  CURRENT_TIMESTAMP, " + // send_datetime
					"  0, " + // read_flag：未読
					"  0 " + // answer_complete_flag：未回答
					")";
			jdbcTemplate.update(sql, //
					message.getChatId(), message.getMessageType(), message.getSenderId(), message.getToDepartmentId(),
					message.getMessageText());
			return jdbcTemplate.queryForObject("SELECT lastval()", Integer.class);
		} finally {
			LOGGER.debug("O_メッセージ登録 終了");
		}
	}

	/**
	 * 既読に更新
	 * 
	 * @param messageId メッセージID
	 * @return
	 */
	public int updateReadFlag(int messageId) {
		LOGGER.debug("O_メッセージ：既読に更新 開始");
		try {
			String sql = "" + //
					"UPDATE o_message " + //
					"SET read_flag='1' " + //
					"WHERE message_id=?";
			return jdbcTemplate.update(sql, //
					messageId);
		} finally {
			LOGGER.debug("O_メッセージ：既読に更新 終了");
		}
	}
	
	/**
	 * 回答済みに更新
	 * 
	 * @param messageId メッセージID
	 * @return
	 */
	public int updateAnswerCompleteFlag(int messageId) {
		LOGGER.debug("O_メッセージ：回答済みに更新 開始");
		try {
			String sql = "" + //
					"UPDATE o_message " + //
					"SET answer_complete_flag='1' " + //
					"WHERE message_id=?";
			return jdbcTemplate.update(sql, //
					messageId);
		} finally {
			LOGGER.debug("O_メッセージ：回答済みに更新 終了");
		}
	}
}
