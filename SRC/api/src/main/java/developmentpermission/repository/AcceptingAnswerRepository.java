package developmentpermission.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import developmentpermission.entity.AcceptingAnswer;

/**
 * O_受付回答Repositoryインタフェース
 */
@Transactional
@Repository
public interface AcceptingAnswerRepository extends JpaRepository<AcceptingAnswer, Integer> {

	/**
	 * O_受付回答検索（部署ごと）
	 * 
	 * @param applicationId     申請ID
	 * @param applicationStepId 申請段階ID
	 * @param versionInfomation 版情報D
	 * @param departmentId      部署ID
	 * 
	 * @return 回答一覧
	 */
	@Query(value = "SELECT accepting_answer_id, application_id, application_step_id, version_infomation, judgement_id, department_id, judgement_result, judgement_result_index, answer_content, register_datetime, update_datetime, answer_data_type, register_status, deadline_datetime, answer_id FROM o_accepting_answer WHERE application_id = :applicationId AND application_step_id = :applicationStepId AND version_infomation = :versionInfomation AND department_id = :departmentId AND register_status = '1' ORDER BY case when judgement_id = '' then '1' else '0' end, judgement_id, judgement_result_index ASC", nativeQuery = true)
	List<AcceptingAnswer> findByDepartmentId(@Param("applicationId") Integer applicationId,@Param("applicationStepId") Integer applicationStepId,@Param("versionInfomation") Integer versionInfomation,@Param("departmentId") String departmentId);

	/**
	 * O_受付回答検索（指定版情報の受付回答）
	 * 
	 * @param applicationId     申請ID
	 * @param applicationStepId 申請段階ID
	 * @param versionInfomation 版情報D
	 * 
	 * @return 回答一覧
	 */
	@Query(value = "SELECT accepting_answer_id, application_id, application_step_id, version_infomation, judgement_id, department_id, judgement_result, judgement_result_index, answer_content, register_datetime, update_datetime, answer_data_type, register_status, deadline_datetime, answer_id FROM o_accepting_answer WHERE application_id = :applicationId AND application_step_id = :applicationStepId AND version_infomation = :versionInfomation AND register_status = '1' ORDER BY department_id, judgement_id, judgement_result_index ASC", nativeQuery = true)
	List<AcceptingAnswer> findByVersionInfomation(@Param("applicationId") Integer applicationId,@Param("applicationStepId") Integer applicationStepId,@Param("versionInfomation") Integer versionInfomation);

	/**
	 * O_受付回答検索（指定版情報の受付回答(登録ステータスが仮申請のを含む)）
	 * 
	 * @param applicationId     申請ID
	 * @param applicationStepId 申請段階ID
	 * @param versionInfomation 版情報D
	 * 
	 * @return 回答一覧
	 */
	@Query(value = "SELECT accepting_answer_id, application_id, application_step_id, version_infomation, judgement_id, department_id, judgement_result, judgement_result_index, answer_content, register_datetime, update_datetime, answer_data_type, register_status, deadline_datetime, answer_id FROM o_accepting_answer WHERE application_id = :applicationId AND application_step_id = :applicationStepId AND version_infomation = :versionInfomation ORDER BY department_id, judgement_id, judgement_result_index ASC", nativeQuery = true)
	List<AcceptingAnswer> getAcceptingAnswerListWithUnRegisted(@Param("applicationId") Integer applicationId,@Param("applicationStepId") Integer applicationStepId,@Param("versionInfomation") Integer versionInfomation);

	/**
	 * O_受付回答検索（指定版情報の受付回答(登録ステータスが仮申請のを含む)）
	 * 
	 * @param applicationId     申請ID
	 * @param applicationStepId 申請段階ID
	 * @param versionInfomation 版情報D
	 * 
	 * @return 回答一覧
	 */
	@Query(value = "SELECT accepting_answer_id, application_id, application_step_id, version_infomation, judgement_id, department_id, judgement_result, judgement_result_index, answer_content, register_datetime, update_datetime, answer_data_type, register_status, deadline_datetime, answer_id FROM o_accepting_answer WHERE application_id = :applicationId AND application_step_id = :applicationStepId AND version_infomation = :versionInfomation ORDER BY department_id, judgement_id, judgement_result_index ASC", nativeQuery = true)
	List<AcceptingAnswer> findByJudgementIdAndDepartmentId(@Param("applicationId") Integer applicationId,@Param("applicationStepId") Integer applicationStepId,@Param("versionInfomation") Integer versionInfomation);
}
