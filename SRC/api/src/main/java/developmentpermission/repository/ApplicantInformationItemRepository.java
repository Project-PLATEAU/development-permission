package developmentpermission.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import developmentpermission.entity.ApplicantInformationItem;

/**
 * M_申請者情報項目Repositoryインタフェース
 */
@Transactional
@Repository
public interface ApplicantInformationItemRepository extends JpaRepository<ApplicantInformationItem, String> {

	/**
	 * 申請者情報入力項目一覧取得
	 * 
	 * @return 申請者情報入力項目一覧
	 */
	@Query(value = "SELECT applicant_information_item_id, display_order, display_flag, require_flag, item_name, regex, mail_address, search_condition_flag, item_type, application_step, add_information_item_flag, contact_address_flag  FROM m_applicant_information_item WHERE add_information_item_flag = '0' AND display_flag = '1' ORDER BY display_order ASC", nativeQuery = true)
	List<ApplicantInformationItem> getApplicantItems();

	/**
	 * 申請追加情報入力項目一覧取得
	 * 
	 * @return 申請追加情報入力項目一覧
	 */
	@Query(value = "SELECT applicant_information_item_id, display_order, display_flag, require_flag, item_name, regex, mail_address, search_condition_flag, item_type, application_step, add_information_item_flag, contact_address_flag FROM m_applicant_information_item WHERE add_information_item_flag = '1' AND display_flag = '1' AND application_step LIKE CONCAT('%', :applicationStepId , '%') ORDER BY display_order ASC ", nativeQuery = true)
	List<ApplicantInformationItem> getApplicantAddItems(@Param("applicationStepId") String applicationStepId);

	/**
	 * 申請者情報入力項目一覧取得(追加項目あり)
	 * 
	 * @return 申請者情報入力項目一覧
	 */
	@Query(value = "SELECT applicant_information_item_id, display_order, display_flag, require_flag, item_name, regex, mail_address, search_condition_flag, item_type, application_step, add_information_item_flag, contact_address_flag  FROM m_applicant_information_item WHERE add_information_item_flag = '1' ORDER BY display_order ASC", nativeQuery = true)
	List<ApplicantInformationItem> getApplicantItemsAddFlagOn();
}
