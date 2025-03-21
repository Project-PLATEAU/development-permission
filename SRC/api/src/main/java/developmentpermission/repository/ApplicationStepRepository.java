package developmentpermission.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import developmentpermission.entity.ApplicationStep;

/**
 * M_申請段階Repositoryインタフェース
 */
@Transactional
@Repository
public interface ApplicationStepRepository extends JpaRepository<ApplicationStep, Integer> {

	/**
	 * 申請段階取得
	 * 
	 * @return 申請段階リスト
	 */
	@Query(value = "SELECT application_step_id, application_step_name FROM m_application_step", nativeQuery = true)
	List<ApplicationStep> findByApplicationStepList();

	/**
	 * 申請段階取得
	 * 
	 * @return 申請段階リスト
	 */
	@Query(value = "SELECT application_step_id, application_step_name FROM m_application_step WHERE application_step_id = :applicationStepId", nativeQuery = true)
	List<ApplicationStep> findByApplicationStepId(@Param("applicationStepId") Integer applicationStepId);

	/**
	 * 申請段階名取得
	 * 
	 * @return 申請段階名
	 */
	@Query(value = "SELECT application_step_id, application_step_name FROM m_application_step WHERE application_step_id = :applicationStepId", nativeQuery = true)
	ApplicationStep findByApplicationStep(@Param("applicationStepId") Integer applicationStepId);
}
