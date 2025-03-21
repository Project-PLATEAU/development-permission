package developmentpermission.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import developmentpermission.entity.ApplicationVersionInformation;
import developmentpermission.entity.key.ApplicationVersionInformationKey;

/**
 * O_申請版情報Repositoryインタフェース
 */
@Transactional
@Repository
public interface ApplicationVersionInformationRepository extends JpaRepository<ApplicationVersionInformation, ApplicationVersionInformationKey> {

	/**
	 * 申請版情報取得(仮申請の申請版情報である場合、版情報-1)
	 * 
	 * @return 申請段階リスト
	 */
	@Query(value = "SELECT application_id, application_step_id, (CASE WHEN register_status = '0' THEN version_information - 1 ELSE version_information END) AS version_information, accepting_flag, accept_version_information, register_datetime, update_datetime, complete_datetime, register_status FROM o_application_version_information WHERE application_id = :applicationId AND (register_status = '1' OR (register_status = '0' AND version_information > 1)) ORDER BY register_datetime DESC ", nativeQuery = true)
	List<ApplicationVersionInformation> findByApplicationId(@Param("applicationId") Integer applicationId);
	
	/**
	 * 最大申請段階の申請版情報取得
	 * @param applicationId 申請ID
	 * @return 申請段階リスト
	 */
	@Query(value = "WITH max_application_step_id AS (SELECT application_id, MAX(application_step_id) AS max_application_id FROM o_application_version_information WHERE  (register_status = '1' OR (register_status = '0' AND version_information > 1)) GROUP BY application_id) SELECT vi.application_id, vi.application_step_id, (CASE WHEN register_status = '0' THEN vi.version_information - 1 ELSE vi.version_information END) AS version_information, accepting_flag, accept_version_information, register_datetime, update_datetime, complete_datetime, register_status FROM o_application_version_information AS vi LEFT OUTER JOIN max_application_step_id AS max ON vi.application_id = max.application_id AND vi.application_step_id = max.max_application_id WHERE vi.application_id = :applicationId AND (vi.register_status = '1' OR (vi.register_status = '0' AND vi.version_information > 1)) AND vi.application_step_id = max.max_application_id ORDER BY register_datetime DESC", nativeQuery = true)
	List<ApplicationVersionInformation> getLatestApplicationVersionInformation(@Param("applicationId") Integer applicationId);
	
	/**
	 * 申請版情報取得
	 * 
	 * @return 申請段階リスト
	 */
	@Query(value = "SELECT application_id, application_step_id, version_information, accepting_flag, accept_version_information, register_datetime, update_datetime, complete_datetime, register_status FROM o_application_version_information WHERE application_id = :applicationId AND application_step_id = :applicationStepId ", nativeQuery = true)
	List<ApplicationVersionInformation> findByApplicationSteId(@Param("applicationId") Integer applicationId, @Param("applicationStepId") Integer applicationStepId);
	
	/**
	 * 申請版情報取得
	 * 
	 * @return 申請段階リスト
	 */
	@Query(value = "SELECT application_id, application_step_id, version_information, accepting_flag, accept_version_information, register_datetime, update_datetime, complete_datetime, register_status FROM o_application_version_information WHERE application_id = :applicationId AND application_step_id = :applicationStepId AND version_information = :versionInformation ", nativeQuery = true)
	List<ApplicationVersionInformation> findOneByVersionInformation(@Param("applicationId") Integer applicationId, @Param("applicationStepId") Integer applicationStepId, @Param("versionInformation") Integer versionInformation );

	/**
	 * 申請版情報取得
	 * 
	 * @return 申請段階リスト
	 */
	@Query(value = "SELECT application_id, application_step_id, version_information, accepting_flag, accept_version_information, register_datetime, update_datetime, complete_datetime, register_status FROM o_application_version_information WHERE application_id = :applicationId ORDER BY register_datetime DESC ", nativeQuery = true)
	List<ApplicationVersionInformation> getApplicationWithUnRegisted(@Param("applicationId") Integer applicationId);

}
