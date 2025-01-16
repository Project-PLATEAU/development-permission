package developmentpermission.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import developmentpermission.entity.Answer;
import developmentpermission.entity.DepartmentAnswer;

/**
 * O_部署回答Repositoryインタフェース
 */
@Transactional
@Repository
public interface AnswerDepartmentRepository extends JpaRepository<DepartmentAnswer, Integer> {

	/**
	 * O_部署回答検索
	 * 
	 * @param departmentAnswerId 部署回答ID
	 * @return 部署ID
	 */
	@Query(value = "SELECT department_answer_id,application_id,application_step_id,department_id,government_confirm_status,government_confirm_datetime,government_confirm_comment,notified_text,complete_flag,notified_flag,register_datetime,update_datetime,register_status,delete_unnotified_flag,government_confirm_permission_flag FROM o_department_answer WHERE department_answer_id = :departmentAnswerId", nativeQuery = true)
	DepartmentAnswer findByAnswerId(@Param("departmentAnswerId") Integer departmentAnswerId);
	
	/**
	 * O_部署回答検索(o_chatより合致するもの)
	 * 
	 * @param chatId チャットID
	 * @return 部署ID
	 */
	@Query(value = "SELECT t1.department_answer_id,t1.application_id,t1.application_step_id,t1.department_id,t1.government_confirm_status,t1.government_confirm_datetime,t1.government_confirm_comment,t1.notified_text,t1.complete_flag,t1.notified_flag,t1.register_datetime,t1.update_datetime,t1.register_status,t1.delete_unnotified_flag,government_confirm_permission_flag FROM o_department_answer t1 INNER JOIN ( SELECT t2.department_answer_id FROM o_chat t2 WHERE t2.chat_id = :chatId ) t3 ON t1.department_answer_id = t3.department_answer_id", nativeQuery = true)
	DepartmentAnswer findByDepartmentId(@Param("chatId") Integer chatId);
}
