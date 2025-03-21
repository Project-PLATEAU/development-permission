package developmentpermission.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import developmentpermission.entity.DevelopmentDocumentMaster;

/**
 * M_開発登録簿Repositoryインタフェース
 */
@Transactional
@Repository
public interface DevelopmentDocumentMasterRepository extends JpaRepository<DevelopmentDocumentMaster, Integer> {

	/**
	 * M_開発登録簿取得
	 * 
	 * @param developmentDocumentId 開発登録簿マスタID
	 * @return M_開発登録簿
	 */
	@Query(value = "SELECT development_document_id, document_name, document_type FROM m_development_document WHERE development_document_id = :developmentDocumentId", nativeQuery = true)
	DevelopmentDocumentMaster getDevelopmentDocumentMaster(@Param("developmentDocumentId") Integer developmentDocumentId);

	/**
	 * M_開発登録簿取得
	 * 
	 * @return M_開発登録簿
	 */
	@Query(value = "SELECT development_document_id, document_name, document_type FROM m_development_document ORDER BY development_document_id", nativeQuery = true)
	List<DevelopmentDocumentMaster> getDevelopmentDocumentMaster();
}
