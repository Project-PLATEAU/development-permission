package developmentpermission.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import developmentpermission.entity.ApplicantInformationAdd;
import developmentpermission.entity.ApplicationCategory;

/**
 * 
 * O_申請区分Repositoryインタフェース
 *
 */
@Transactional
@Repository
public interface ApplicationCategoryRepository extends JpaRepository<ApplicationCategory, String>{
	/**
	 * O_申請区分検索
	 * 
	 * @param answerId 回答ID
	 * @return 回答ファイル一覧
	 */
	@Query(value = "SELECT application_id, view_id, category_id, application_step_id, version_information FROM o_application_category WHERE application_id = :applicationId AND application_step_id = :applicationStepId AND version_information = :versionInformation", nativeQuery = true)
	List<ApplicationCategory> findByVer(@Param("applicationId") Integer applicationId, @Param("applicationStepId") Integer applicationStepId, @Param("versionInformation") Integer versionInformation);

	/**
	 * O_申請区分検索
	 * 
	 * @param answerId 回答ID
	 * @return 回答ファイル一覧
	 */
	@Query(value = "SELECT application_id, view_id, category_id, application_step_id, version_information FROM o_application_category WHERE application_id = :applicationId", nativeQuery = true)
	List<ApplicationCategory> getApplicationCategoryByApplicationId(@Param("applicationId") Integer applicationId);

}
