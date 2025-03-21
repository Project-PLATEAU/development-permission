package developmentpermission.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import developmentpermission.entity.ApplicantInformationAdd;

/**
 * O_申請追加情報Repositoryインタフェース
 */
@Transactional
@Repository
public interface ApplicantInformationAddRepository extends JpaRepository<ApplicantInformationAdd, Integer> {

	/**
	 * 申請追加情報取得
	 * @param applicationId 申請ID
	 * @param applicationStepId 申請段階ID
	 * @param applicantInformationItemId 申請項目ID
	 * @param versionInformation 版情報
	 * @return
	 */
	@Query(value = "SELECT applicant_id, application_id, application_step_id, applicant_information_item_id, item_value, version_information FROM o_applicant_information_add WHERE application_id = :applicationId AND application_step_id = :applicationStepId AND applicant_information_item_id = :applicantInformationItemId AND version_information = :versionInformation ", nativeQuery = true)
	List<ApplicantInformationAdd> getApplicantInformationAdd(@Param("applicationId") Integer applicationId, @Param("applicationStepId") Integer applicationStepId, @Param("applicantInformationItemId") String applicantInformationItemId, @Param("versionInformation") Integer versionInformation);


	/**
	 * 申請追加情報取得
	 * @param applicationId 申請ID
	 * @param applicationStepId 申請段階ID
	 * @param versionInformation 版情報
	 * @return
	 */
	@Query(value = "SELECT applicant_id, application_id, application_step_id, applicant_information_item_id, item_value, version_information FROM o_applicant_information_add WHERE application_id = :applicationId AND application_step_id = :applicationStepId AND version_information = :versionInformation ", nativeQuery = true)
	List<ApplicantInformationAdd> getApplicantInformationAddByVer(@Param("applicationId") Integer applicationId, @Param("applicationStepId") Integer applicationStepId, @Param("versionInformation") Integer versionInformation);

}
