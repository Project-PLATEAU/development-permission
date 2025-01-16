package developmentpermission.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import developmentpermission.entity.Answer;

/**
 * O_回答Repositoryインタフェース
 */
@Transactional
@Repository
public interface AnswerRepository extends JpaRepository<Answer, Integer> {

	/**
	 * O_回答検索
	 * 
	 * @param answerId 回答ID
	 * @return 回答一覧
	 */
	@Query(value = "SELECT answer_id, application_id, judgement_id, judgement_result_index, department_answer_id, judgement_result, answer_content, notified_text, register_datetime, update_datetime, complete_flag, notified_flag, answer_update_flag, re_application_flag, business_reapplication_flag, application_step_id, department_id, discussion_flag, discussion_item, business_pass_status, business_pass_comment, business_answer_datetime, government_confirm_status, government_confirm_datetime, government_confirm_comment, government_confirm_notified_flag, permission_judgement_result, answer_status, answer_data_type, register_status, delete_unnotified_flag, deadline_datetime, answer_permission_flag, government_confirm_permission_flag, permission_judgement_migration_flag, version_information FROM o_answer WHERE answer_id = :answerId", nativeQuery = true)
	List<Answer> findByAnswerId(@Param("answerId") Integer answerId);

	/**
	 * O_回答検索
	 * 
	 * @param applicationId 申請ID
	 * @return 回答一覧
	 */
	@Query(value = "SELECT answer_id, application_id, judgement_id, judgement_result_index, department_answer_id, judgement_result, answer_content, notified_text, register_datetime, update_datetime, complete_flag, notified_flag, answer_update_flag, re_application_flag, business_reapplication_flag, application_step_id, department_id, discussion_flag, discussion_item, business_pass_status, business_pass_comment, business_answer_datetime, government_confirm_status, government_confirm_datetime, government_confirm_comment, government_confirm_notified_flag, permission_judgement_result, answer_status, answer_data_type, register_status, delete_unnotified_flag, deadline_datetime, answer_permission_flag, government_confirm_permission_flag, permission_judgement_migration_flag, version_information FROM o_answer WHERE application_id = :applicationId", nativeQuery = true)
	List<Answer> findByApplicationId(@Param("applicationId") Integer applicationId);

	/**
	 * O_回答検索
	 * 
	 * @param applicationId 申請ID
	 * @param applicationStepId 申請段階ID
	 * @param answerId 回答ID
	 * @return 回答一覧
	 */
	@Query(value = "SELECT answer_id, application_id, judgement_id, judgement_result_index, department_answer_id, judgement_result, answer_content, notified_text, register_datetime, update_datetime, complete_flag, notified_flag, answer_update_flag, re_application_flag, business_reapplication_flag, application_step_id, department_id, discussion_flag, discussion_item, business_pass_status, business_pass_comment, business_answer_datetime, government_confirm_status, government_confirm_datetime, government_confirm_comment, government_confirm_notified_flag, permission_judgement_result, answer_status, answer_data_type, register_status, delete_unnotified_flag, deadline_datetime, answer_permission_flag, government_confirm_permission_flag, permission_judgement_migration_flag, version_information FROM o_answer WHERE application_id = :applicationId AND application_step_id = :applicationStepId AND answer_id = :answerId AND notified_flag = '1'", nativeQuery = true)
	List<Answer> findByApplicationIdAndApplicationStepIdAndAnswerId(@Param("applicationId") Integer applicationId,@Param("applicationStepId") Integer applicationStepId,@Param("answerId") Integer answerId);
	
	/**
	 * O_回答検索
	 * 
	 * @param applicationId 申請ID
	 * @param applicationStepId 申請段階ID
	 * @param departmentAnswerId 部署回答ID
	 * @return 回答一覧
	 */
	@Query(value = "SELECT answer_id, application_id, judgement_id, judgement_result_index, department_answer_id, judgement_result, answer_content, notified_text, register_datetime, update_datetime, complete_flag, notified_flag, answer_update_flag, re_application_flag, business_reapplication_flag, application_step_id, department_id, discussion_flag, discussion_item, business_pass_status, business_pass_comment, business_answer_datetime, government_confirm_status, government_confirm_datetime, government_confirm_comment, government_confirm_notified_flag, permission_judgement_result, answer_status, answer_data_type, register_status, delete_unnotified_flag, deadline_datetime, answer_permission_flag, government_confirm_permission_flag, permission_judgement_migration_flag, version_information FROM o_answer WHERE application_id = :applicationId AND application_step_id = :applicationStepId AND department_answer_id = :departmentAnswerId AND notified_flag = '1'", nativeQuery = true)
	List<Answer> findByApplicationIdAndApplicationStepIdAndDepartmentAnswerId(@Param("applicationId") Integer applicationId,@Param("applicationStepId") Integer applicationStepId,@Param("departmentAnswerId") Integer departmentAnswerId);
	
	/**
	 * O_回答検索(削除済みの回答を除く)
	 * 
	 * @param applicationId 申請ID
	 * @param applicationStepId 申請段階ID
	 * @return 回答一覧
	 */
	@Query(value = "SELECT answer_id, application_id, judgement_id, judgement_result_index, department_answer_id, judgement_result, answer_content, notified_text, register_datetime, update_datetime, complete_flag, notified_flag, answer_update_flag, re_application_flag, business_reapplication_flag, application_step_id, department_id, discussion_flag, discussion_item, business_pass_status, business_pass_comment, business_answer_datetime, government_confirm_status, government_confirm_datetime, government_confirm_comment, government_confirm_notified_flag, permission_judgement_result, answer_status, answer_data_type, register_status, delete_unnotified_flag, deadline_datetime, answer_permission_flag, government_confirm_permission_flag, permission_judgement_migration_flag, version_information FROM o_answer WHERE application_id = :applicationId AND application_step_id = :applicationStepId AND notified_flag = '1' AND register_status = '1' AND delete_flag = '0'", nativeQuery = true)
	List<Answer> findByApplicationIdAndapplicationStepId(@Param("applicationId") Integer applicationId,@Param("applicationStepId") Integer applicationStepId);
	
	/**
	 * O_回答検索(削除済みの回答を除く)
	 * ※ソート順：部署ID、判定項目ID、判定結果インデックス、回答ID
	 * 
	 * @param applicationId 申請ID
	 * @param applicationStepId 申請段階ID
	 * @return 回答一覧
	 */
	@Query(value = "SELECT answer_id, application_id, judgement_id, judgement_result_index, department_answer_id, judgement_result, answer_content, notified_text, register_datetime, update_datetime, complete_flag, notified_flag, answer_update_flag, re_application_flag, business_reapplication_flag, application_step_id, department_id, discussion_flag, discussion_item, business_pass_status, business_pass_comment, business_answer_datetime, government_confirm_status, government_confirm_datetime, government_confirm_comment, government_confirm_notified_flag, permission_judgement_result, answer_status, answer_data_type, register_status, delete_unnotified_flag, deadline_datetime, answer_permission_flag, government_confirm_permission_flag, permission_judgement_migration_flag, version_information FROM o_answer WHERE application_id = :applicationId AND application_step_id = :applicationStepId AND notified_flag = '1' AND register_status = '1' AND delete_flag = '0' ORDER BY department_id ASC, CASE WHEN judgement_id = '' THEN '1' ELSE '0' END ASC, judgement_id ASC, judgement_result_index ASC, answer_id ASC", nativeQuery = true)
	List<Answer> getAnswerListWithSort(@Param("applicationId") Integer applicationId,@Param("applicationStepId") Integer applicationStepId);

	/**
	 * 未回答検索
	 * 
	 * @param applicationId 申請ID
	 * @return 未回答一覧
	 */
	@Query(value = "SELECT answer_id, application_id, judgement_id, judgement_result_index, department_answer_id, judgement_result, answer_content, notified_text, register_datetime, update_datetime, complete_flag, notified_flag, answer_update_flag, re_application_flag, business_reapplication_flag, application_step_id, department_id, discussion_flag, discussion_item, business_pass_status, business_pass_comment, business_answer_datetime, government_confirm_status, government_confirm_datetime, government_confirm_comment, government_confirm_notified_flag, permission_judgement_result, answer_status, answer_data_type, register_status, delete_unnotified_flag, deadline_datetime, answer_permission_flag, government_confirm_permission_flag, permission_judgement_migration_flag, version_information FROM o_answer WHERE application_id = :applicationId AND complete_flag = '0' AND register_status = '1' AND delete_flag = '0'", nativeQuery = true)
	List<Answer> findUnansweredByApplicationId(@Param("applicationId") Integer applicationId);
	
	/**
	 * O_回答検索(事業者へ通知した、再申請要の回答)
	 * 
	 * @param applicationId 申請ID
	 * @return 回答一覧
	 */
	@Query(value = "SELECT answer_id, application_id, judgement_id, judgement_result_index, department_answer_id, judgement_result, answer_content, notified_text, register_datetime, update_datetime, complete_flag, notified_flag, answer_update_flag, re_application_flag, business_reapplication_flag, application_step_id, department_id, discussion_flag, discussion_item, business_pass_status, business_pass_comment, business_answer_datetime, government_confirm_status, government_confirm_datetime, government_confirm_comment, government_confirm_notified_flag, permission_judgement_result, answer_status, answer_data_type, register_status, delete_unnotified_flag, deadline_datetime, answer_permission_flag, government_confirm_permission_flag, permission_judgement_migration_flag, version_information FROM o_answer WHERE application_id = :applicationId AND business_reapplication_flag = '1' AND register_status = '1' AND delete_flag = '0'", nativeQuery = true)
	List<Answer> findReapplicationByApplicationId(@Param("applicationId") Integer applicationId);

	/**
	 * 未回答検索(申請段階ごとの回答)
	 * 
	 * @param applicationId     申請ID
	 * @param applicationStepId 申請段階ID
	 * 
	 * @return 未回答一覧
	 */
	@Query(value = "SELECT answer_id, application_id, judgement_id, judgement_result_index, department_answer_id, judgement_result, answer_content, notified_text, register_datetime, update_datetime, complete_flag, notified_flag, answer_update_flag, re_application_flag, business_reapplication_flag, application_step_id, department_id, discussion_flag, discussion_item, business_pass_status, business_pass_comment, business_answer_datetime, government_confirm_status, government_confirm_datetime, government_confirm_comment, government_confirm_notified_flag, permission_judgement_result, answer_status, answer_data_type, register_status, delete_unnotified_flag, deadline_datetime, answer_permission_flag, government_confirm_permission_flag, permission_judgement_migration_flag, version_information FROM o_answer WHERE application_id = :applicationId AND application_step_id = :applicationStepId AND complete_flag = '0' AND register_status = '1' AND delete_flag = '0'", nativeQuery = true)
	List<Answer> findUnansweredListByApplicationStepId(@Param("applicationId") Integer applicationId, @Param("applicationStepId") Integer applicationStepId );

	/**
	 * O_回答検索
	 * 
	 * @param applicationId 申請ID
	 * @return 回答一覧
	 */
	@Query(value = "SELECT answer_id, application_id, judgement_id, judgement_result_index, department_answer_id, judgement_result, answer_content, notified_text, register_datetime, update_datetime, complete_flag, notified_flag, answer_update_flag, re_application_flag, business_reapplication_flag, application_step_id, department_id, discussion_flag, discussion_item, business_pass_status, business_pass_comment, business_answer_datetime, government_confirm_status, government_confirm_datetime, government_confirm_comment, government_confirm_notified_flag, permission_judgement_result, answer_status, answer_data_type, register_status, delete_unnotified_flag, deadline_datetime, answer_permission_flag, government_confirm_permission_flag, permission_judgement_migration_flag, version_information FROM o_answer WHERE application_id = :applicationId AND application_step_id = :applicationStepId AND register_status = '1' AND delete_flag = '0' ORDER BY answer_id ", nativeQuery = true)
	List<Answer> findByApplicationStepId(@Param("applicationId") Integer applicationId, @Param("applicationStepId") Integer applicationStepId);

	/**
	 * O_回答検索
	 * 
	 * @param applicationId 申請ID
	 * @return 回答一覧
	 */
	@Query(value = "SELECT answer_id, application_id, judgement_id, judgement_result_index, department_answer_id, judgement_result, answer_content, notified_text, register_datetime, update_datetime, complete_flag, notified_flag, answer_update_flag, re_application_flag, business_reapplication_flag, application_step_id, department_id, discussion_flag, discussion_item, business_pass_status, business_pass_comment, business_answer_datetime, government_confirm_status, government_confirm_datetime, government_confirm_comment, government_confirm_notified_flag, permission_judgement_result, answer_status, answer_data_type, register_status, delete_unnotified_flag, deadline_datetime, answer_permission_flag, government_confirm_permission_flag, permission_judgement_migration_flag, version_information FROM o_answer WHERE application_id = :applicationId AND application_step_id = :applicationStepId AND judgement_id=:judgementId AND register_status = '1' AND delete_flag = '0' ORDER BY answer_id", nativeQuery = true)
	List<Answer> findByJudgementId(@Param("applicationId") Integer applicationId, @Param("applicationStepId") Integer applicationStepId, @Param("judgementId") String judgementId);

	/**
	 * O_回答検索(事業者へ通知した、再申請要の回答)
	 * 
	 * @param applicationId 申請ID
	 * @return 回答一覧
	 */
	@Query(value = "SELECT answer_id, application_id, judgement_id, judgement_result_index, department_answer_id, judgement_result, answer_content, notified_text, register_datetime, update_datetime, complete_flag, notified_flag, answer_update_flag, re_application_flag, business_reapplication_flag, application_step_id, department_id, discussion_flag, discussion_item, business_pass_status, business_pass_comment, business_answer_datetime, government_confirm_status, government_confirm_datetime, government_confirm_comment, government_confirm_notified_flag, permission_judgement_result, answer_status, answer_data_type, register_status, delete_unnotified_flag, deadline_datetime, answer_permission_flag, government_confirm_permission_flag, permission_judgement_migration_flag, version_information FROM o_answer WHERE application_id = :applicationId AND application_step_id = :applicationStepId AND business_reapplication_flag = '1' AND register_status = '1' AND delete_flag = '0' ORDER BY answer_id", nativeQuery = true)
	List<Answer> findReapplicationByApplicationStepId(@Param("applicationId") Integer applicationId, @Param("applicationStepId") Integer applicationStepId);

	/**
	 * O_回答検索
	 * 
	 * @param applicationId 申請ID
	 * @return 回答一覧
	 */
	@Query(value = "SELECT answer_id, application_id, judgement_id, judgement_result_index, department_answer_id, judgement_result, answer_content, notified_text, register_datetime, update_datetime, complete_flag, notified_flag, answer_update_flag, re_application_flag, business_reapplication_flag, application_step_id, department_id, discussion_flag, discussion_item, business_pass_status, business_pass_comment, business_answer_datetime, government_confirm_status, government_confirm_datetime, government_confirm_comment, government_confirm_notified_flag, permission_judgement_result, answer_status, answer_data_type, register_status, delete_unnotified_flag, deadline_datetime, answer_permission_flag, government_confirm_permission_flag, permission_judgement_migration_flag, version_information FROM o_answer WHERE application_id = :applicationId AND application_step_id = :applicationStepId AND delete_flag = '0' ORDER BY answer_id ", nativeQuery = true)
	List<Answer> getAnswerListWithUnRegisted(@Param("applicationId") Integer applicationId, @Param("applicationStepId") Integer applicationStepId);

	/**
	 * O_回答検索
	 * 
	 * @param applicationId 申請ID
	 * @return 回答一覧
	 */
	@Query(value = "SELECT answer_id, application_id, judgement_id, judgement_result_index, department_answer_id, judgement_result, answer_content, notified_text, register_datetime, update_datetime, complete_flag, notified_flag, answer_update_flag, re_application_flag, business_reapplication_flag, application_step_id, department_id, discussion_flag, discussion_item, business_pass_status, business_pass_comment, business_answer_datetime, government_confirm_status, government_confirm_datetime, government_confirm_comment, government_confirm_notified_flag, permission_judgement_result, answer_status, answer_data_type, register_status, delete_unnotified_flag, deadline_datetime, answer_permission_flag, government_confirm_permission_flag, permission_judgement_migration_flag, version_information FROM o_answer WHERE application_id = :applicationId AND application_step_id = :applicationStepId AND answer_data_type = '3' AND register_status = '1' AND delete_flag = '0' ORDER BY answer_id ", nativeQuery = true)
	List<Answer> getGovernmentAddAnswerList(@Param("applicationId") Integer applicationId, @Param("applicationStepId") Integer applicationStepId);

	/**
	 * O_回答検索(判定項目の判定結果で回答レコードを検索)
	 * 
	 * @param applicationId 申請ID
	 * @param applicationStepId 申請段階ID
	 * @param judgementId 判定項目ID
	 * @param judgementResultIndex 判定結果のインデックス
	 * @return 回答一覧
	 */
	@Query(value = "SELECT answer_id, application_id, judgement_id, judgement_result_index, department_answer_id, judgement_result, answer_content, notified_text, register_datetime, update_datetime, complete_flag, notified_flag, answer_update_flag, re_application_flag, business_reapplication_flag, application_step_id, department_id, discussion_flag, discussion_item, business_pass_status, business_pass_comment, business_answer_datetime, government_confirm_status, government_confirm_datetime, government_confirm_comment, government_confirm_notified_flag, permission_judgement_result, answer_status, answer_data_type, register_status, delete_unnotified_flag, deadline_datetime, answer_permission_flag, government_confirm_permission_flag, permission_judgement_migration_flag, version_information FROM o_answer WHERE application_id = :applicationId AND application_step_id = :applicationStepId AND judgement_id=:judgementId AND judgement_result_index=:judgementResultIndex AND register_status = '1' AND delete_flag = '0' ORDER BY answer_id", nativeQuery = true)
	List<Answer> findAnswerByJudgementResult(@Param("applicationId") Integer applicationId, @Param("applicationStepId") Integer applicationStepId, @Param("judgementId") String judgementId, @Param("judgementResultIndex") Integer judgementResultIndex);
	
	/**
	 * 未回答検索(もう少しで期日が来るもの)
	 * 
	 * @param deadlineDatetime システム設定日時
	 * @return 未回答一覧
	 */
	@Query(value = "SELECT answer_id, application_id, judgement_id, judgement_result_index, department_answer_id, judgement_result, answer_content, notified_text, register_datetime, update_datetime, complete_flag, notified_flag, answer_update_flag, re_application_flag, business_reapplication_flag, application_step_id, department_id, discussion_flag, discussion_item, business_pass_status, business_pass_comment, business_answer_datetime, government_confirm_status, government_confirm_datetime, government_confirm_comment, government_confirm_notified_flag, permission_judgement_result, answer_status, answer_data_type, register_status, delete_unnotified_flag, deadline_datetime, answer_permission_flag, government_confirm_permission_flag, permission_judgement_migration_flag, version_information FROM o_answer WHERE DATE(deadline_datetime) <= (CURRENT_DATE + (CAST(:appAnswerDeadlineXDaysAgo || ' days' AS INTERVAL))) AND DATE(deadline_datetime) >= CURRENT_DATE AND complete_flag = '0' AND delete_flag = '0'", nativeQuery = true)
	List<Answer> findNotResponseAnswerExpireList(@Param("appAnswerDeadlineXDaysAgo") Integer appAnswerDeadlineXDaysAgo);

	/**
	 * 未回答検索(期限が過ぎているもの)
	 * 
	 * @return 未回答一覧
	 */
	@Query(value = "SELECT answer_id, application_id, judgement_id, judgement_result_index, department_answer_id, judgement_result, answer_content, notified_text, register_datetime, update_datetime, complete_flag, notified_flag, answer_update_flag, re_application_flag, business_reapplication_flag, application_step_id, department_id, discussion_flag, discussion_item, business_pass_status, business_pass_comment, business_answer_datetime, government_confirm_status, government_confirm_datetime, government_confirm_comment, government_confirm_notified_flag, permission_judgement_result, answer_status, answer_data_type, register_status, delete_unnotified_flag, deadline_datetime, answer_permission_flag, government_confirm_permission_flag, permission_judgement_migration_flag, version_information FROM o_answer WHERE DATE(deadline_datetime) < CURRENT_DATE AND complete_flag = '0' AND delete_flag = '0'", nativeQuery = true)
	List<Answer> findNotResponseAnswerList();
	
	/**
	 * 未通知検索(もう少しで期日が来るもの)
	 * 
	 * @param appappBufferDays システムバッファ日数 
	 * @param deadlineDatetime システム設定日時
	 * @return 未通知一覧
	 */
	@Query(value = "SELECT answer_id, application_id, judgement_id, judgement_result_index, department_answer_id, judgement_result, answer_content, notified_text, register_datetime, update_datetime, complete_flag, notified_flag, answer_update_flag, re_application_flag, business_reapplication_flag, application_step_id, department_id, discussion_flag, discussion_item, business_pass_status, business_pass_comment, business_answer_datetime, government_confirm_status, government_confirm_datetime, government_confirm_comment, government_confirm_notified_flag, permission_judgement_result, answer_status, answer_data_type, register_status, delete_unnotified_flag, deadline_datetime, answer_permission_flag, government_confirm_permission_flag, permission_judgement_migration_flag, version_information FROM o_answer WHERE DATE((deadline_datetime + (CAST(:appAnswerBufferDays || ' days' AS INTERVAL)))) <= (CURRENT_DATE + (CAST(:appAnswerDeadlineXDaysAgo || ' days' AS INTERVAL))) AND DATE(deadline_datetime) >= CURRENT_DATE AND complete_flag = '1' AND notified_flag ='0' AND delete_flag = '0'", nativeQuery = true)
	List<Answer> findNotNotifiedAnswerExpireList(@Param("appAnswerBufferDays") Integer appAnswerBufferDays,@Param("appAnswerDeadlineXDaysAgo") Integer appAnswerDeadlineXDaysAgo);

	/**
	 * 未通知検索(期限が過ぎているもの)
	 * 
	 * @return 未通知一覧
	 */
	@Query(value = "SELECT answer_id, application_id, judgement_id, judgement_result_index, department_answer_id, judgement_result, answer_content, notified_text, register_datetime, update_datetime, complete_flag, notified_flag, answer_update_flag, re_application_flag, business_reapplication_flag, application_step_id, department_id, discussion_flag, discussion_item, business_pass_status, business_pass_comment, business_answer_datetime, government_confirm_status, government_confirm_datetime, government_confirm_comment, government_confirm_notified_flag, permission_judgement_result, answer_status, answer_data_type, register_status, delete_unnotified_flag, deadline_datetime, answer_permission_flag, government_confirm_permission_flag, permission_judgement_migration_flag, version_information FROM o_answer WHERE DATE(deadline_datetime) < CURRENT_DATE AND complete_flag = '1' AND notified_flag ='0' AND delete_flag = '0'", nativeQuery = true)
	List<Answer> findNotNotifiedAnswerList();
	
	/**
	 * 未回答検索(もう少しで期日が来るもの・事前協議)
	 * 
	 * @param appAnswerBussinesRegisterDays 事業者合意登録日時のZ日前
	 * @return 未回答一覧
	 */
	@Query(value = "SELECT answer_id, application_id, judgement_id, judgement_result_index, department_answer_id, judgement_result, answer_content, notified_text, register_datetime, update_datetime, complete_flag, notified_flag, answer_update_flag, re_application_flag, business_reapplication_flag, application_step_id, department_id, discussion_flag, discussion_item, business_pass_status, business_pass_comment, business_answer_datetime, government_confirm_status, government_confirm_datetime, government_confirm_comment, government_confirm_notified_flag, permission_judgement_result, answer_status, answer_data_type, register_status, delete_unnotified_flag, deadline_datetime, answer_permission_flag, government_confirm_permission_flag, permission_judgement_migration_flag, version_information FROM o_answer WHERE complete_flag = '1' AND notified_flag ='1'AND application_step_id =2 AND business_pass_status IS NOT NULL AND government_confirm_status IS NULL AND DATE(update_datetime) >= (CURRENT_DATE - (CAST(:appAnswerBussinesRegisterDays || ' days' AS INTERVAL))) AND delete_flag = '0'", nativeQuery = true)
	List<Answer> findNotResponseAnswerExpireListStep2(@Param("appAnswerBussinesRegisterDays") Integer appAnswerBussinesRegisterDays);

	/**
	 * 未回答検索(期限が過ぎているもの・事前協議)
	 * 
	 * @param appAnswerBussinesRegisterDays 事業者合意登録日時のZ日前
	 * @return 未回答一覧
	 */
	@Query(value = "SELECT answer_id, application_id, judgement_id, judgement_result_index, department_answer_id, judgement_result, answer_content, notified_text, register_datetime, update_datetime, complete_flag, notified_flag, answer_update_flag, re_application_flag, business_reapplication_flag, application_step_id, department_id, discussion_flag, discussion_item, business_pass_status, business_pass_comment, business_answer_datetime, government_confirm_status, government_confirm_datetime, government_confirm_comment, government_confirm_notified_flag, permission_judgement_result, answer_status, answer_data_type, register_status, delete_unnotified_flag, deadline_datetime, answer_permission_flag, government_confirm_permission_flag, permission_judgement_migration_flag, version_information FROM o_answer WHERE complete_flag = '1' AND notified_flag ='1'AND application_step_id =2 AND business_pass_status IS NOT NULL AND government_confirm_status IS NULL AND DATE((update_datetime + (CAST(:appAnswerBussinesRegisterDays || ' days' AS INTERVAL)))) < CURRENT_DATE AND delete_flag = '0'", nativeQuery = true)
	List<Answer> findNotResponseAnswerListStep2(@Param("appAnswerBussinesRegisterDays") Integer appAnswerBussinesRegisterDays);
	
	/**
	 * 未通知検索(もう少しで期日が来るもの・事前協議)
	 * 
	 * @param appAnswerBussinesRegisterDays 事業者合意登録日時のZ日前
	 * @param appappBufferDays システムバッファ日数 
	 * @param deadlineDatetime システム設定日時
	 * @return 未通知一覧
	 */
	@Query(value = "SELECT answer_id, application_id, judgement_id, judgement_result_index, department_answer_id, judgement_result, answer_content, notified_text, register_datetime, update_datetime, complete_flag, notified_flag, answer_update_flag, re_application_flag, business_reapplication_flag, application_step_id, department_id, discussion_flag, discussion_item, business_pass_status, business_pass_comment, business_answer_datetime, government_confirm_status, government_confirm_datetime, government_confirm_comment, government_confirm_notified_flag, permission_judgement_result, answer_status, answer_data_type, register_status, delete_unnotified_flag, deadline_datetime, answer_permission_flag, government_confirm_permission_flag, permission_judgement_migration_flag, version_information FROM o_answer WHERE complete_flag = '1' AND notified_flag ='1' AND application_step_id =2 AND business_pass_status IS NOT NULL AND government_confirm_status IS NULL AND DATE((update_datetime + (CAST(:appAnswerBussinesRegisterDays || ' days' AS INTERVAL)))) >= CURRENT_DATE AND (government_confirm_notified_flag IS NULL OR government_confirm_notified_flag = '0') AND (CURRENT_DATE +(CAST(:appAnswerDeadlineXDaysAgo || ' days' AS INTERVAL))) >= DATE((deadline_datetime + (CAST(:appAnswerBufferDays || ' days' AS INTERVAL)))) AND DATE(deadline_datetime) <= CURRENT_DATE AND delete_flag = '0'", nativeQuery = true)
	List<Answer> findNotNotifiedAnswerExpireListStep2(@Param("appAnswerBussinesRegisterDays") Integer appAnswerBussinesRegisterDays,@Param("appAnswerBufferDays") Integer appAnswerBufferDays,@Param("appAnswerDeadlineXDaysAgo") Integer appAnswerDeadlineXDaysAgo);

	/**
	 * 未通知検索(期限が過ぎているもの・事前協議)
	 * 
	 * @return 未通知一覧
	 */
	@Query(value = "SELECT answer_id, application_id, judgement_id, judgement_result_index, department_answer_id, judgement_result, answer_content, notified_text, register_datetime, update_datetime, complete_flag, notified_flag, answer_update_flag, re_application_flag, business_reapplication_flag, application_step_id, department_id, discussion_flag, discussion_item, business_pass_status, business_pass_comment, business_answer_datetime, government_confirm_status, government_confirm_datetime, government_confirm_comment, government_confirm_notified_flag, permission_judgement_result, answer_status, answer_data_type, register_status, delete_unnotified_flag, deadline_datetime, answer_permission_flag, government_confirm_permission_flag, permission_judgement_migration_flag, version_information FROM o_answer WHERE complete_flag = '1' AND notified_flag ='1' AND application_step_id =2 AND business_pass_status IS NOT NULL AND government_confirm_status IS NULL AND DATE((update_datetime + (CAST(:appAnswerBussinesRegisterDays || ' days' AS INTERVAL)))) >= CURRENT_DATE AND (government_confirm_notified_flag IS NULL OR government_confirm_notified_flag = '0') AND CURRENT_DATE < DATE(deadline_datetime) AND delete_flag = '0'", nativeQuery = true)
	List<Answer> findNotNotifiedAnswerListStep2(@Param("appAnswerBussinesRegisterDays") Integer appAnswerBussinesRegisterDays);
	
	/**
	 * 未回答検索(期限が過ぎているもの・事前協議・事業者側)
	 * 
	 * @param appAnswerBussinesStatusDays 事業者へ合意登録通知日時のY日前
	 * @return 未回答一覧
	 */
	@Query(value = "SELECT answer_id, application_id, judgement_id, judgement_result_index, department_answer_id, judgement_result, answer_content, notified_text, register_datetime, update_datetime, complete_flag, notified_flag, answer_update_flag, re_application_flag, business_reapplication_flag, application_step_id, department_id, discussion_flag, discussion_item, business_pass_status, business_pass_comment, business_answer_datetime, government_confirm_status, government_confirm_datetime, government_confirm_comment, government_confirm_notified_flag, permission_judgement_result, answer_status, answer_data_type, register_status, delete_unnotified_flag, deadline_datetime, answer_permission_flag, government_confirm_permission_flag, permission_judgement_migration_flag, version_information FROM o_answer WHERE complete_flag = '1' AND notified_flag ='1' AND application_step_id =2 AND business_pass_status IS NULL AND DATE(update_datetime + (CAST(:appAnswerBussinesStatusDays || ' days' AS INTERVAL))) < CURRENT_DATE AND delete_flag = '0'", nativeQuery = true)
	List<Answer> findNotResponseAnswerListStep2Bussines(@Param("appAnswerBussinesStatusDays") Integer appAnswerBussinesStatusDays);

	/**
	 * O_回答検索
	 * 
	 * @param applicationId 申請ID
	 * @param applicationStepId 申請段階ID
	 * @param departmentId 部署ID
	 * @return 回答一覧
	 */
	@Query(value = "SELECT answer_id, application_id, judgement_id, judgement_result_index, department_answer_id, judgement_result, answer_content, notified_text, register_datetime, update_datetime, complete_flag, notified_flag, answer_update_flag, re_application_flag, business_reapplication_flag, application_step_id, department_id, discussion_flag, discussion_item, business_pass_status, business_pass_comment, business_answer_datetime, government_confirm_status, government_confirm_datetime, government_confirm_comment, government_confirm_notified_flag, permission_judgement_result, answer_status, answer_data_type, register_status, delete_unnotified_flag, deadline_datetime, answer_permission_flag, government_confirm_permission_flag, permission_judgement_migration_flag, version_information FROM o_answer WHERE application_id = :applicationId AND application_step_id = :applicationStepId AND department_id = :departmentId AND delete_flag = '0' ORDER BY answer_id", nativeQuery = true)
	List<Answer> findByApplicationIdAndApplicationStepIdAndDepartmentId(@Param("applicationId") Integer applicationId,@Param("applicationStepId") Integer applicationStepId,@Param("departmentId") String departmentId);

	/**
	 * 部署ID一覧検索
	 * 
	 * @param applicationId 申請ID
	 * @param applicationStepId 申請段階ID
	 * @return 部署ID一覧
	 */
	@Query(value = "SELECT DISTINCT department_id FROM o_answer WHERE application_id = :applicationId AND application_step_id = :applicationStepId AND delete_flag = '0' AND department_id!='-1' ORDER BY department_id ", nativeQuery = true)
	List<String> getDepartmentIdList(@Param("applicationId") Integer applicationId, @Param("applicationStepId") Integer applicationStepId);

	/**
	 * O_回答検索(事業者へ通知済み　かつ　事業者合意未登録の回答を検索)
	 * 
	 * @param departmentAnswerId 部署回答ID
	 * @return 回答一覧
	 */
	@Query(value = "SELECT answer_id, application_id, judgement_id, judgement_result_index, department_answer_id, judgement_result, answer_content, notified_text, register_datetime, update_datetime, complete_flag, notified_flag, answer_update_flag, re_application_flag, business_reapplication_flag, application_step_id, department_id, discussion_flag, discussion_item, business_pass_status, business_pass_comment, business_answer_datetime, government_confirm_status, government_confirm_datetime, government_confirm_comment, government_confirm_notified_flag, permission_judgement_result, answer_status, answer_data_type, register_status, delete_unnotified_flag, deadline_datetime, answer_permission_flag, government_confirm_permission_flag, permission_judgement_migration_flag, version_information FROM o_answer WHERE department_answer_id = :departmentAnswerId AND application_step_id = 2 AND notified_flag = '1' AND business_pass_status IS NULL AND register_status = '1' AND delete_flag = '0' ORDER BY answer_id", nativeQuery = true)
	List<Answer> getNotifiedAnswerListByDepartmentAnswerId(@Param("departmentAnswerId") Integer departmentAnswerId);
	
}
