package developmentpermission.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import developmentpermission.entity.ApplicationFile;

/**
 * O_申請ファイルRepositoryインタフェース
 */
@Transactional
@Repository
public interface ApplicationFileRepository extends JpaRepository<ApplicationFile, Integer> {

	/**
	 * 申請ファイル一覧取得
	 * 
	 * @param judgementItemIdList 申請ファイルID
	 * @return 申請ファイル一覧
	 */
	@Query(value = "SELECT file_id, application_id, application_step_id, application_file_id, upload_file_name, file_path,version_information, extension, upload_datetime, direction_department, revise_content FROM o_application_file WHERE application_id = :applicationId AND application_file_id = :applicationFileId AND delete_flag = '0' ORDER BY file_id ASC", nativeQuery = true)
	List<ApplicationFile> getApplicationFiles(@Param("applicationFileId") String applicationFileId, @Param("applicationId") Integer applicationId);

	/**
	 * 申請ファイル一覧取得
	 * 
	 * @param fileId ファイルID
	 * @return 申請ファイル一覧
	 */
	@Query(value = "SELECT file_id, application_id, application_step_id, application_file_id, upload_file_name, file_path,version_information, extension, upload_datetime, direction_department, revise_content FROM o_application_file WHERE file_id = :fileId AND delete_flag = '0' ORDER BY file_id ASC", nativeQuery = true)
	List<ApplicationFile> getApplicationFile(@Param("fileId") Integer fileId);

	/**
	 * 申請ファイル一覧取得(版情報で降順)
	 * 
	 * @param applicationFileId 申請ファイルID
	 * @param applicationId     申請ID
	 * @param applicationStepId 申請段階ID
	 * @return 申請ファイル一覧
	 */
	@Query(value = "SELECT file_id, application_id, application_step_id, application_file_id, upload_file_name, file_path,version_information, extension, upload_datetime, direction_department, revise_content FROM o_application_file WHERE application_id = :applicationId AND application_step_id = :applicationStepId AND application_file_id = :applicationFileId AND delete_flag = '0' ORDER BY version_information desc", nativeQuery = true)
	List<ApplicationFile> getApplicationFilesSortByVer(@Param("applicationFileId") String applicationFileId, @Param("applicationId") Integer applicationId, @Param("applicationStepId") Integer applicationStepId);

	/**
	 * 指定版情報の申請ファイル一覧取得
	 * 
	 * @param applicationId      申請ID
	 * @param applicationStepId  申請段階ID
	 * @param versionInformation 版情報
	 * 
	 * @return 申請ファイル一覧
	 */
	@Query(value = "SELECT file_id, application_id, application_step_id, application_file_id, upload_file_name, file_path,version_information, extension, upload_datetime, direction_department, revise_content FROM o_application_file WHERE application_id = :applicationId AND application_step_id = :applicationStepId AND version_information = :versionInformation AND delete_flag = '0' ORDER BY file_id desc", nativeQuery = true)
	List<ApplicationFile> getApplicationFilesByVersionInformation(@Param("applicationId") Integer applicationId, @Param("applicationStepId") Integer applicationStepId, @Param("versionInformation") Integer versionInformation);

	/**
	 * 申請IDに対する全ての申請ファイル一覧取得
	 * 
	 * @param applicationId 申請ID
	 * 
	 * @return 申請ファイル一覧
	 */
	@Query(value = "SELECT file_id, application_id, application_step_id, application_file_id, upload_file_name, file_path,version_information, extension, upload_datetime, direction_department, revise_content FROM o_application_file WHERE application_id = :applicationId AND delete_flag = '0' ORDER BY file_id desc", nativeQuery = true)
	List<ApplicationFile> getApplicationFilesByApplicationId(@Param("applicationId") Integer applicationId);
	
	/**
	 * 申請IDに対する全ての申請ファイル一覧取得
	 * 
	 * @param applicationId 申請ID
	 * 
	 * @return 申請ファイル一覧
	 */
	@Query(value = "SELECT file_id, application_id, application_step_id, application_file_id, upload_file_name, file_path, version_information, extension, upload_datetime, direction_department, revise_content FROM o_application_file WHERE application_id = :applicationId AND application_file_id = :applicationFileId AND (application_step_id <> 2 OR (application_step_id = 2 AND version_information <= :versionInformation)) AND delete_flag = '0' ORDER BY application_step_id ASC, version_information DESC ", nativeQuery = true)
	List<ApplicationFile> getAllHistoryApplicationFilesFromVersionInformation(@Param("applicationFileId") String applicationFileId, @Param("applicationId") Integer applicationId, @Param("versionInformation") Integer versionInformation);

	/**
	 * 指定版情報の申請ファイル一覧取得(申請ファイルID単位)
	 * 
	 * @param applicationFileId  申請ファイルID
	 * @param applicationId      申請ID
	 * @param applicationStepId  申請段階ID
	 * @param versionInformation 版情報
	 * 
	 * @return 申請ファイル一覧
	 */
	@Query(value = "SELECT file_id, application_id, application_step_id, application_file_id, upload_file_name, file_path,version_information, extension, upload_datetime, direction_department, revise_content FROM o_application_file WHERE application_id = :applicationId AND application_step_id = :applicationStepId AND version_information = :versionInformation AND application_file_id = :applicationFileId AND delete_flag = '0' ORDER BY file_id desc", nativeQuery = true)
	List<ApplicationFile> getApplicationFilesByVersionInformation(@Param("applicationFileId") String applicationFileId,@Param("applicationId") Integer applicationId, @Param("applicationStepId") Integer applicationStepId, @Param("versionInformation") Integer versionInformation);

	/**
	 * 指定版情報以前の申請ファイル一覧取得(申請ファイルID単位)
	 * 
	 * @param applicationFileId  申請ファイルID
	 * @param applicationId      申請ID
	 * @param applicationStepId  申請段階ID
	 * @param versionInformation 版情報
	 * 
	 * @return 申請ファイル一覧
	 */
	@Query(value = "SELECT file_id, application_id, application_step_id, application_file_id, upload_file_name, file_path,version_information, extension, upload_datetime, direction_department, revise_content FROM o_application_file WHERE application_id = :applicationId AND application_step_id = :applicationStepId AND version_information <= :versionInformation AND application_file_id = :applicationFileId AND delete_flag = '0' ORDER BY file_id desc", nativeQuery = true)
	List<ApplicationFile> getHistoryApplicationFilesFromVersionInformation(@Param("applicationFileId") String applicationFileId,@Param("applicationId") Integer applicationId, @Param("applicationStepId") Integer applicationStepId, @Param("versionInformation") Integer versionInformation);

	/**
	 * 回答レポートファイル一覧取得
	 * 
	 * @param applicationFileId 申請ファイルID
	 * @param applicationId     申請ID
	 * @param applicationStepId 申請段階ID
	 * @return 申請ファイル一覧
	 */
	@Query(value = "SELECT file_id, application_id, application_step_id, application_file_id, upload_file_name, file_path,version_information, extension, upload_datetime, direction_department, revise_content FROM o_application_file WHERE application_id = :applicationId AND application_step_id = :applicationStepId  AND application_file_id = :applicationFileId ORDER BY version_information desc, upload_datetime desc", nativeQuery = true)
	List<ApplicationFile> getAnswerRepotFileList(@Param("applicationFileId") String applicationFileId, @Param("applicationId") Integer applicationId, @Param("applicationStepId") Integer applicationStepId);

	/**
	 * 申請ファイル一覧取得
	 * 
	 * @param filePath ファイルパス
	 * 
	 * @return 申請ファイル一覧
	 */
	@Query(value = "SELECT file_id, application_id, application_step_id, application_file_id, upload_file_name, file_path,version_information, extension, upload_datetime, direction_department, revise_content FROM o_application_file WHERE file_path = :filePath AND delete_flag = '0' ORDER BY file_id desc", nativeQuery = true)
	List<ApplicationFile> findByFilePath(@Param("filePath") String filePath);

}
