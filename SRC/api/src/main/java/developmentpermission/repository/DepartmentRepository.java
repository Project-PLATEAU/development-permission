package developmentpermission.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import developmentpermission.entity.Department;

/**
 * M_部署Repositoryインタフェース
 */
@Transactional
@Repository
public interface DepartmentRepository extends JpaRepository<Department, String> {

	/**
	 * 部署一覧取得
	 * 
	 * @return 部署一覧
	 */
	@Query(value = "SELECT department_id, department_name, answer_authority_flag, mail_address, admin_mail_address FROM m_department ORDER BY department_id ASC", nativeQuery = true)
	List<Department> getDepartmentList();
	
	/**
	 * 部署一覧取得
	 * 
	 * @param departmentId 部署ID
	 * @return 部署一覧
	 */
	@Query(value = "SELECT department_id, department_name, answer_authority_flag, mail_address, admin_mail_address FROM m_department WHERE department_id = :departmentId ORDER BY department_id ASC", nativeQuery = true)
	List<Department> getDepartmentListById(@Param("departmentId") String departmentId);
	
	/**
	 * 部署一覧取得（ID複数指定）
	 * 
	 * @param departmentIdList 部署ID一覧
	 * @return 部署一覧
	 */
	@Query(value = "SELECT department_id, department_name, answer_authority_flag, mail_address, admin_mail_address FROM m_department WHERE department_id IN (:departmentId) ORDER BY department_id ASC", nativeQuery = true)
	List<Department> getDepartmentListByIdList(@Param("departmentId") List<String> departmentIdList);
}
