package developmentpermission.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import developmentpermission.entity.ApplicationFileMaster;
import developmentpermission.entity.key.ApplicationFileKey;

/**
 * M_申請ファイルRepositoryインタフェース
 */
@Transactional
@Repository
public interface ApplicationFileMasterRepository extends JpaRepository<ApplicationFileMaster, ApplicationFileKey> {

	/**
	 * 申請ファイル一覧取得
	 * 
	 * @param judgementItemIdList 判定項目IDリスト
	 * @return 申請ファイル一覧
	 */
	@Query(value = "SELECT application_file_id, require_flag, upload_file_name,'' as judgement_item_id, extension  FROM m_application_file WHERE judgement_item_id IN (:judgementItemId) GROUP BY application_file_id, require_flag, upload_file_name, extension ORDER BY application_file_id", nativeQuery = true)
	List<ApplicationFileMaster> getApplicationFiles(@Param("judgementItemId") List<String> judgementItemIdList);

	/**
	 * 申請ファイル一覧取得
	 * 
	 * @param applicationFileId 申請ファイルID
	 * @return 申請ファイル一覧
	 */
	@Query(value = "SELECT application_file_id, judgement_item_id, require_flag, upload_file_name, extension FROM m_application_file WHERE application_file_id = :applicationFileId ORDER BY application_file_id", nativeQuery = true)
	List<ApplicationFileMaster> getApplicationFile(@Param("applicationFileId") String applicationFileId);

	/**
	 * カテゴリ名取得
	 * 
	 * @param applicationFileId 申請ファイルID
	 * @return カテゴリ名
	 */
	@Query(value = "SELECT DISTINCT upload_file_name AS category_name FROM m_application_file WHERE application_file_id = :applicationFileId LIMIT 1", nativeQuery = true)
	String getCategoryName(@Param("applicationFileId") String applicationFileId);

	/**
	 * 開発登録簿対象申請ファイルID一覧取得
	 * 
	 * @return List<String>
	 */
	@Query(value = "SELECT DISTINCT application_file_id FROM m_application_file WHERE application_file_type = '1' ORDER BY application_file_id", nativeQuery = true)
	List<String> getDevelopmentRegisterFileList();

}
