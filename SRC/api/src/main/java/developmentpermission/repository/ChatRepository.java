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
	@Query(value = "SELECT chat_id, application_id, application_step_id, department_answer_id, answer_id, government_answer_datetime, establishment_post_datetime, last_answerer_id, establishment_first_post_datetime  FROM o_chat WHERE answer_id = :answerId ORDER BY chat_id ASC", nativeQuery = true)
	List<Chat> findByAnswerId(@Param("answerId") Integer answerId);
	
	/**
	 * チャット一覧取得
	 * 
	 * @return チャット一覧
	 */
	@Query(value = "SELECT chat_id, application_id, application_step_id, department_answer_id, answer_id, government_answer_datetime, establishment_post_datetime, last_answerer_id, establishment_first_post_datetime  FROM o_chat WHERE application_id = :applicationId ORDER BY chat_id ASC", nativeQuery = true)
	List<Chat> findByApplicationId(@Param("applicationId") Integer applicationId);
	
	/**
	 * チャット一覧取得
	 * 
	 * @return チャット一覧
	 */
	@Query(value = "SELECT chat_id, application_id, application_step_id, department_answer_id, answer_id, government_answer_datetime, establishment_post_datetime, last_answerer_id, establishment_first_post_datetime  FROM o_chat WHERE chat_id = :chatId ORDER BY chat_id ASC", nativeQuery = true)
	List<Chat> findByChatId(@Param("chatId") Integer chatId);
	
	/**
	 * チャット一覧取得
	 * 
	 * @return チャット一覧
	 */
	@Query(value = "SELECT chat_id, application_id, application_step_id, department_answer_id, answer_id, government_answer_datetime, establishment_post_datetime, last_answerer_id, establishment_first_post_datetime  FROM o_chat WHERE department_answer_id = :departmentAnswerId ORDER BY chat_id ASC", nativeQuery = true)
	List<Chat> findByDepartmentAnswerId(@Param("departmentAnswerId") Integer departmentAnswerId);
	
	/**
	 * チャット一覧取得
	 * 
	 * @return チャット一覧
	 */
	@Query(value = "SELECT chat_id, application_id, application_step_id, department_answer_id, answer_id, government_answer_datetime, establishment_post_datetime, last_answerer_id, establishment_first_post_datetime  FROM o_chat WHERE application_id = :applicationId AND application_step_id = :applicationStepId ORDER BY chat_id ASC", nativeQuery = true)
	List<Chat> findByApplicationStepId(@Param("applicationId") Integer applicationId, @Param("applicationStepId") Integer applicationStepId);

	/**
	 * チャット一覧取得
	 * 
	 * @return チャット一覧
	 */
	@Query(value = "SELECT chat_id, application_id, application_step_id, department_answer_id, answer_id, government_answer_datetime, establishment_post_datetime, last_answerer_id, establishment_first_post_datetime FROM o_chat WHERE application_id = :applicationId AND answer_id = :answerId AND application_step_id = :applicationStepId ORDER BY chat_id ASC", nativeQuery = true)
	List<Chat> findByApplicationStepId1(@Param("applicationId") Integer applicationId,@Param("answerId") Integer answerId ,@Param("applicationStepId") Integer applicationStepId);
	
	/**
	 * チャット一覧取得
	 * 
	 * @return チャット一覧
	 */
	@Query(value = "SELECT chat_id, application_id, application_step_id, department_answer_id, answer_id, government_answer_datetime, establishment_post_datetime, last_answerer_id, establishment_first_post_datetime FROM o_chat WHERE application_id = :applicationId AND application_step_id = :applicationStepId AND department_answer_id = :departmentAnswerId ORDER BY chat_id ASC", nativeQuery = true)
	List<Chat> findByApplicationStepId2(@Param("applicationId") Integer applicationId,@Param("applicationStepId") Integer applicationStepId,@Param("departmentAnswerId") Integer departmentAnswerId);
	
	/**
	 * チャット一覧取得
	 * 
	 * @return チャット一覧
	 */
	@Query(value = "SELECT chat_id, application_id, application_step_id, department_answer_id, answer_id, government_answer_datetime, establishment_post_datetime, last_answerer_id, establishment_first_post_datetime FROM o_chat WHERE application_id = :applicationId AND application_step_id = :applicationStepId ORDER BY chat_id ASC", nativeQuery = true)
	List<Chat> findByApplicationStepId3(@Param("applicationId") Integer applicationId,@Param("applicationStepId") Integer applicationStepId);
	
	/**
	 * チャット一覧取得(未回答リマインドの取得)
	 * 
	 * @return チャット一覧(未回答リマインドの取得)
	 */
	@Query(value = "SELECT * FROM o_chat t1 INNER JOIN o_message t2 ON t1.chat_id = t2.chat_id WHERE t2.message_type = 1 AND t2. answer_complete_flag = '0' AND DATE(t1.establishment_post_datetime)+1 < CURRENT_DATE", nativeQuery = true)
	List<Chat> findRemindChat();
}


