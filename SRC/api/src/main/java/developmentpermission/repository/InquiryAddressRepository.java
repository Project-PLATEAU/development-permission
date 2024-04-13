package developmentpermission.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import developmentpermission.entity.InquiryAddress;

/**
 * O_問合せ宛先Repositoryインタフェース
 */
@Transactional
@Repository
public interface InquiryAddressRepository extends JpaRepository<InquiryAddress, Integer> {

	/**
	 * O_問合せ宛先検索
	 * 
	 * @param inquiryAddressId 問合せ宛先ID
	 * @return 問合せ宛先一覧
	 */
	@Query(value = "SELECT inquiry_address_id, message_id, department_id, read_flag, answer_complete_flag FROM o_inquiry_address WHERE inquiry_address_id = :inquiryAddressId ", nativeQuery = true)
	List<InquiryAddress> findByInquiryAddressId(@Param("inquiryAddressId") Integer inquiryAddressId);
	
	/**
	 * O_問合せ宛先検索
	 * 
	 * @param messageId メッセージID
	 * @return　問合せ宛先一覧
	 */
	@Query(value = "SELECT inquiry_address_id, message_id, department_id, read_flag, answer_complete_flag FROM o_inquiry_address  WHERE message_id = :messageId ORDER BY inquiry_address_id ASC", nativeQuery = true)
	List<InquiryAddress> findByMessageId(@Param("messageId") Integer messageId);
	
	/**
	 * O_問合せ宛先検索
	 * 
	 * @param messageId メッセージID
	 * @param departmentId 部署ID
	 * @return　問合せ宛先一覧
	 */
	@Query(value = "SELECT inquiry_address_id, message_id, department_id, read_flag, answer_complete_flag FROM o_inquiry_address  WHERE message_id = :messageId AND department_id =:departmentId ", nativeQuery = true)
	List<InquiryAddress> findByMessageIdAndDepartmentId(@Param("messageId") Integer messageId, @Param("departmentId") String departmentId);

}
