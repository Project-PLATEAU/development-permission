package developmentpermission.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import developmentpermission.entity.DevelopmentDocument;

/**
 * O_開発登録簿Repositoryインタフェース
 */
@Transactional
@Repository
public interface DevelopmentDocumentRepository extends JpaRepository<DevelopmentDocument, Integer> {

	/**
	 * O_開発登録簿検索
	 * 
	 * @param applicationId 申請ID
	 * @return O_開発登録簿一覧
	 */
	@Query(value = "SELECT file_id, application_id, development_document_id, file_path, register_datetime FROM o_development_document WHERE application_id = :applicationId ORDER BY file_id", nativeQuery = true)
	List<DevelopmentDocument> getDevelopmentDocumentByApplicationId(@Param("applicationId") Integer applicationId);

}
