package developmentpermission.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import developmentpermission.entity.AnswerHistory;

/**
 * O_回答履歴Repositoryインタフェース
 */
@Transactional
@Repository
public interface AnswerHistoryRepository extends JpaRepository<AnswerHistory, Integer> {

	/**
	 * 回答IDから回答履歴を取得する
	 * 
	 * @param answerId 回答ID
	 * @return AnswerHistory
	 */
	@Query(value = "SELECT answer_history_id, answer_id, answer_user_id, answer_datetime, answer_text, notify_flag FROM o_answer_history WHERE answer_id = :answerId ORDER BY answer_datetime DESC", nativeQuery = true)
	List<AnswerHistory> getAnswerHistoryByAnswerId(@Param("answerId") Integer answerId);
	
	/**
	 * 回答IDから事業者向け回答履歴を取得する
	 * 
	 * @param answerId 回答ID
	 * @return AnswerHistory
	 */
	@Query(value = "SELECT answer_history_id, answer_id, answer_user_id, answer_datetime, answer_text, notify_flag FROM o_answer_history WHERE answer_id = :answerId and notify_flag = '1' ORDER BY answer_datetime DESC", nativeQuery = true)
	List<AnswerHistory> getAnswerHistoryByAnswerIdForBusiness(@Param("answerId") Integer answerId);

	/**
	 * 申請IDから回答履歴を取得する
	 * 
	 * @param applicationId 回答ID
	 * @return AnswerHistory
	 */
	@Query(value = "SELECT a.answer_history_id AS answer_history_id, a.answer_id AS answer_id, a.answer_user_id AS answer_user_id, a.answer_datetime AS answer_datetime, a.answer_text AS answer_text, a.notify_flag AS notify_flag FROM o_answer_history AS a LEFT OUTER JOIN o_answer b ON a.answer_id = b.answer_id WHERE b.application_id = :applicationId ORDER BY answer_datetime DESC", nativeQuery = true)
	List<AnswerHistory> getAnswerHistoryByApplicationId(@Param("applicationId") Integer applicationId);
}
