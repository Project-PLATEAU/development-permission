package developmentpermission.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import developmentpermission.entity.DepartmentAnswer;

/**
 * O_部署回答Repositoryインタフェース
 */
@Transactional
@Repository
public interface DepartmentAnswerRepository extends JpaRepository<DepartmentAnswer, Integer> {

	/**
	 * O_部署回答検索
	 * 
	 * @param departmentAnswerId 部署回答ID
	 * @return 部署回答一覧
	 */
	@Query(value = "SELECT department_answer_id, application_id, application_step_id, department_id, government_confirm_status, government_confirm_datetime, government_confirm_comment, notified_text, complete_flag, notified_flag, register_datetime, update_datetime, register_status, delete_unnotified_flag, government_confirm_permission_flag FROM o_department_answer WHERE department_answer_id = :departmentAnswerId ORDER BY department_answer_id", nativeQuery = true)
	List<DepartmentAnswer> findByDepartmentAnswerId(@Param("departmentAnswerId") Integer departmentAnswerId);
	
	/**
	 * O_部署回答検索
	 * 
	 * @param applicationId 申請ID
	 * @return 部署回答一覧
	 */
	@Query(value = "SELECT department_answer_id, application_id, application_step_id, department_id, government_confirm_status, government_confirm_datetime, government_confirm_comment, notified_text, complete_flag, notified_flag, register_datetime, update_datetime, register_status, delete_unnotified_flag, government_confirm_permission_flag FROM o_department_answer WHERE application_id = :applicationId AND application_step_id = '2' ORDER BY department_id", nativeQuery = true)
	List<DepartmentAnswer> findByApplicationId(@Param("applicationId") Integer applicationId);
	
	/**
	 * 未回答の部署回答検索
	 * 
	 * @param applicationId 申請ID
	 * @return 部署回答一覧
	 */
	@Query(value = "SELECT department_answer_id, application_id, application_step_id, department_id, government_confirm_status, government_confirm_datetime, government_confirm_comment, notified_text, complete_flag, notified_flag, register_datetime, update_datetime, register_status, delete_unnotified_flag, government_confirm_permission_flag FROM o_department_answer WHERE application_id = :applicationId AND application_step_id = '2' AND complete_flag = '0' ORDER BY department_answer_id", nativeQuery = true)
	List<DepartmentAnswer> findUnansweredList(@Param("applicationId") Integer applicationId);
	
	/**
	 * O_部署回答検索
	 * 
	 * @param applicationId 申請ID
	 * @return 部署回答一覧
	 */
	@Query(value = "SELECT department_answer_id, application_id, application_step_id, department_id, government_confirm_status, government_confirm_datetime, government_confirm_comment, notified_text, complete_flag, notified_flag, register_datetime, update_datetime, register_status, delete_unnotified_flag, government_confirm_permission_flag FROM o_department_answer WHERE application_id = :applicationId AND application_step_id = '2' AND department_id = :departmentId ORDER BY department_answer_id", nativeQuery = true)
	List<DepartmentAnswer> getDepartmentAnswer(@Param("applicationId") Integer applicationId, @Param("departmentId") String departmentId);
}
