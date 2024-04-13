package developmentpermission.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import developmentpermission.entity.AnswerName;

/**
 * 回答者一覧Repositoryインタフェース
 */
@Transactional
@Repository
public interface AnswerNameRepository extends JpaRepository<AnswerName, String>{
	/**
	 * 回答一覧者取得
	 * 
	 * @return 回答者一覧
	 */
	@Query(value = "SELECT user_id, login_id, user_name, m_government_user.department_id , department_name FROM m_government_user LEFT JOIN m_department ON m_government_user.department_id = m_department.department_id ORDER BY m_government_user.department_id, user_id ASC", nativeQuery=true)
	List<AnswerName> getAnswerNameList();	
}
