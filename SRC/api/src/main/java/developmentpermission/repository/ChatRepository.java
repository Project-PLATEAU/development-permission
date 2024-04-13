package developmentpermission.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import developmentpermission.entity.Chat;

/**
 * O_チャットRepositoryインタフェース
 */
@Transactional
@Repository
public interface ChatRepository extends JpaRepository<Chat, Integer> {

	/**
	 * チャット一覧取得
	 * 
	 * @return チャット一覧
	 */
	@Query(value = "SELECT chat_id, answer_id, government_answer_datetime, establishment_post_datetime, last_answerer_id, establishment_first_post_datetime  FROM o_chat WHERE answer_id = :answerId ORDER BY chat_id ASC", nativeQuery = true)
	List<Chat> findByAnswerId(@Param("answerId") Integer answerId);
	
	/**
	 * チャット一覧取得
	 * 
	 * @return チャット一覧
	 */
	@Query(value = "SELECT chat_id, answer_id, government_answer_datetime, establishment_post_datetime, last_answerer_id, establishment_first_post_datetime  FROM o_chat WHERE chat_id = :chatId ORDER BY chat_id ASC", nativeQuery = true)
	List<Chat> findByChatId(@Param("chatId") Integer chatId);
}


