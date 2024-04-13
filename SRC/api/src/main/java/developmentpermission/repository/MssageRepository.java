package developmentpermission.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import developmentpermission.entity.Message;

/**
 * O_メッセージRepositoryインタフェース
 */
@Transactional
@Repository
public interface MssageRepository extends JpaRepository<Message, Integer> {
	
	/**
	 * メッセージ一覧取得（事業者）
	 * 抽出対象：1: 事業者→行政 2:行政→事業者 のメッセージのみ
	 * 
	 * @return メッセージ一覧
	 */
	@Query(value = "SELECT message_id, chat_id, message_type, sender_id, to_department_id, message_text, send_datetime, read_flag, answer_complete_flag FROM o_message WHERE chat_id = :chatId  AND message_type in ('1', '2') ORDER BY send_datetime ASC", nativeQuery = true)
	List<Message> findByChatIdForBusiness(@Param("chatId") Integer chatId);
	
	/**
	 * メッセージ一覧取得
	 * 
	 * @return メッセージ一覧
	 */
	@Query(value = "SELECT message_id, chat_id, message_type, sender_id, to_department_id, message_text, send_datetime, read_flag, answer_complete_flag FROM o_message WHERE chat_id = :chatId ORDER BY send_datetime ASC", nativeQuery = true)
	List<Message> findByChatId(@Param("chatId") Integer chatId);
	
	/**
	 * メッセージ一覧取得
	 * 
	 * @return メッセージ一覧
	 */
	@Query(value = "SELECT message_id, chat_id, message_type, sender_id, to_department_id, message_text, send_datetime, read_flag, answer_complete_flag FROM o_message WHERE chat_id = :chatId AND message_type = :messageType ORDER BY send_datetime DESC", nativeQuery = true)
	List<Message> findByChatIdAndMessageType(@Param("chatId") Integer chatId, @Param("messageType") Integer messageType);
	
	
	/**
	 * メッセージ一覧取得
	 * 
	 * @return メッセージ一覧
	 */
	@Query(value = "SELECT message_id, chat_id, message_type, sender_id, to_department_id, message_text, send_datetime, read_flag, answer_complete_flag FROM o_message WHERE message_id = :messageId ", nativeQuery = true)
	List<Message> findByMessageId(@Param("messageId") Integer messageId);
}
