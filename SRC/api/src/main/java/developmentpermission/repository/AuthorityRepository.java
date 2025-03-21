package developmentpermission.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import developmentpermission.entity.Authority;
import developmentpermission.entity.key.AuthorityKey;

/**
 * O_権限Repositoryインタフェース
 */
@Transactional
@Repository
public interface AuthorityRepository extends JpaRepository<Authority, AuthorityKey> {

	/**
	 * 権限を取得する
	 * 
	 * @param departmentId 部署ID
	 * @param applicationStepId 申請段階ID
	 * @return AuthorityList
	 */
	@Query(value = "SELECT department_id, application_step_id, answer_authority_flag, notification_authority_flag FROM m_authority WHERE department_id = :departmentId AND application_step_id = :applicationStepId", nativeQuery = true)
	List<Authority> getAuthorityList(@Param("departmentId") String departmentId, @Param("applicationStepId") Integer applicationStepId );
	
	/**
	 * 権限を取得する
	 * 
	 * @param departmentId 部署ID
	 * @param applicationStepId 申請段階ID
	 * @return AuthorityList
	 */
	@Query(value = "SELECT department_id, application_step_id, answer_authority_flag, notification_authority_flag FROM m_authority WHERE department_id = :departmentId ORDER BY application_step_id ASC", nativeQuery = true)
	List<Authority> findByDepartmentId(@Param("departmentId") String departmentId);
	
	/**
	 * 許可判定通知部署を取得する
	 * @return AuthorityList
	 */
	@Query(value = "SELECT department_id, application_step_id, answer_authority_flag, notification_authority_flag FROM m_authority WHERE application_step_id = 3 AND notification_authority_flag IN ('1','2') ORDER BY department_id ASC", nativeQuery = true)
	List<Authority> findStep3NotificationDepartment();
	
	/**
	 * 許可判定回答部署を取得する
	 * @return AuthorityList
	 */
	@Query(value = "SELECT department_id, application_step_id, answer_authority_flag, notification_authority_flag FROM m_authority WHERE application_step_id = 3 AND answer_authority_flag IN ('1','2') ORDER BY department_id ASC", nativeQuery = true)
	List<Authority> findStep3AnswerDepartment();
	
	/**
	 * 申請段階ごとの部署権限一覧取得
	 * 
	 * @param applicationStepId 申請段階ID
	 * @return
	 */
	@Query(value = "SELECT department_id, application_step_id, answer_authority_flag, notification_authority_flag FROM m_authority WHERE application_step_id = :applicationStepId ORDER BY department_id ASC", nativeQuery = true)
	List<Authority> getAuthorityListByApplicationStepId(@Param("applicationStepId") Integer applicationStepId);
}
