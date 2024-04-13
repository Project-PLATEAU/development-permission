package developmentpermission.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import developmentpermission.entity.InquiryFile;

/**
 * O_問合せファイルRepositoryインタフェース
 */
@Transactional
@Repository
public interface InquiryFileRepository extends JpaRepository<InquiryFile, Integer> {

	/**
	 * O_問合せファイル検索
	 * 
	 * @param inquiryFileId 問合せファイルID
	 * @return 問合せファイル一覧
	 */
	@Query(value = "SELECT inquiry_file_id, message_id, file_name, file_path, register_datetime FROM o_inquiry_file WHERE inquiry_file_id = :inquiryFileId ", nativeQuery = true)
	List<InquiryFile> findByInquiryFileId(@Param("inquiryFileId") Integer inquiryFileId);
	
	/**
	 * O_問合せファイル検索
	 * 
	 * @param messageId メッセージID
	 * @return 問合せファイル一覧
	 */
	@Query(value = "SELECT inquiry_file_id, message_id, file_name, file_path, register_datetime FROM o_inquiry_file  WHERE message_id = :messageId ORDER BY inquiry_file_id ASC", nativeQuery = true)
	List<InquiryFile> findByMessageId(@Param("messageId") Integer messageId);

}
