package developmentpermission.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import developmentpermission.entity.ApplicationType;

/**
 * M_申請種類Repositoryインタフェース
 */
@Transactional
@Repository
public interface ApplicationTypeRepository extends JpaRepository<ApplicationType, Integer> {

	/**
	 * 申請種類リスト取得
	 * 
	 * @return 申請種類リスト
	 */
	@Query(value = "SELECT application_type_id, application_type_name, application_step FROM m_application_type ORDER BY application_type_id ASC", nativeQuery = true)
	List<ApplicationType> getApplicationTypeList();

	
	/**
	 * 申請種類取得
	 * 
	 * @return 申請種類リスト
	 */
	@Query(value = "SELECT application_type_id, application_type_name, application_step FROM m_application_type WHERE application_type_id = :applicationTypeId", nativeQuery = true)
	List<ApplicationType> findByApplicationTypeId(@Param("applicationTypeId") Integer applicationTypeId);
}
