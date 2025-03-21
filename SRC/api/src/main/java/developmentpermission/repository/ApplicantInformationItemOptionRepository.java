package developmentpermission.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import developmentpermission.entity.ApplicantInformationItemOption;

/**
 * M_申請情報項目選択肢Repositoryインタフェース
 */
@Transactional
@Repository
public interface ApplicantInformationItemOptionRepository extends JpaRepository<ApplicantInformationItemOption, String> {

	/**
	 * 申請種類取得
	 * 
	 * @return 申請種類リスト
	 */
	@Query(value = "SELECT applicant_information_item_option_id, applicant_information_item_id, display_order, applicant_information_item_option_name FROM m_applicant_information_item_option WHERE applicant_information_item_id = :applicantInformationItemId ORDER BY display_order ASC", nativeQuery = true)
	List<ApplicantInformationItemOption> findByApplicantInformationItemId(@Param("applicantInformationItemId") String applicantInformationItemId);
}
