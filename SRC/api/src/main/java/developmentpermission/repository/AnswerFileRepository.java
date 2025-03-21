package developmentpermission.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import developmentpermission.entity.AnswerFile;

/**
 * O_回答ファイルRepositoryインタフェース
 */
@Transactional
@Repository
public interface AnswerFileRepository extends JpaRepository<AnswerFile, Integer> {

	/**
	 * O_回答ファイル検索
	 * 
	 * @param answerId 回答ID
	 * @return 回答ファイル一覧
	 */
	@Query(value = "SELECT answer_file_id, answer_id, application_id, application_step_id, department_id, answer_file_name, file_path, notified_file_path, delete_unnotified_flag FROM o_answer_file WHERE answer_id = :answerId ORDER BY answer_file_id ASC", nativeQuery = true)
	List<AnswerFile> findByAnswerId(@Param("answerId") Integer answerId);
	
	/**
	 * O_回答ファイル検索
	 * 
	 * @param answerId 回答ID
	 * @return 回答ファイル一覧
	 */
	@Query(value = "SELECT answer_file_id, answer_id, application_id, application_step_id, department_id, answer_file_name, file_path, notified_file_path, delete_unnotified_flag FROM o_answer_file WHERE answer_id = :answerId AND delete_unnotified_flag = '0' AND delete_flag = '0' ORDER BY answer_file_id ASC", nativeQuery = true)
	List<AnswerFile> findByAnswerIdWithoutDeleted(@Param("answerId") Integer answerId);

	/**
	 * O_回答ファイル検索(事業者用)
	 * 
	 * @param answerId 回答ID
	 * @return 回答ファイル一覧
	 */
	@Query(value = "SELECT answer_file_id, answer_id, application_id, application_step_id, department_id, answer_file_name, file_path, notified_file_path, delete_unnotified_flag FROM o_answer_file WHERE answer_id = :answerId AND notified_file_path IS NOT NULL AND delete_flag = '0' ORDER BY answer_file_id ASC", nativeQuery = true)
	List<AnswerFile> findByAnswerIdWithoutDeletedForBusiness(@Param("answerId") Integer answerId);
	
	/**
	 * O_回答ファイル検索(行政用)
	 * 
	 * @param answerId 回答ID
	 * @return 回答ファイル一覧
	 */
	@Query(value = "SELECT answer_file_id, answer_id, application_id, application_step_id, department_id, answer_file_name, file_path, notified_file_path, delete_unnotified_flag FROM o_answer_file WHERE answer_id = :answerId AND delete_flag = '0' ORDER BY answer_file_id ASC", nativeQuery = true)
	List<AnswerFile> findByAnswerIdWithoutDeletedForGoverment(@Param("answerId") Integer answerId);
	
	/**
	 * O_回答ファイル検索(事業者用、2事前協議用)
	 * 
	 * @param answerId 回答ID
	 * @param applicationId 申請ID
	 * @param applicationStepId 申請段階ID
	 * @param departmentId 部署ID
	 * @return 回答ファイル一覧
	 */
	@Query(value = "SELECT answer_file_id, answer_id, application_id, application_step_id, department_id, answer_file_name, file_path, notified_file_path, delete_unnotified_flag FROM o_answer_file WHERE application_id = :applicationId AND application_step_id = :applicationStepId AND department_id = :departmentId AND notified_file_path IS NOT NULL AND delete_flag = '0' ORDER BY answer_file_id ASC", nativeQuery = true)
	List<AnswerFile> findByAnswerIdWithoutDeletedForBusiness2(@Param("applicationId") Integer applicationId,@Param("applicationStepId") Integer applicationStepId,@Param("departmentId") String departmentId);
	
	/**
	 * O_回答ファイル検索(行政用、2事前協議用)
	 * 
	 * @param answerId 回答ID
	 * @param applicationId 申請ID
	 * @param applicationStepId 申請段階ID
	 * @param departmentId 部署ID
	 * @return 回答ファイル一覧
	 */
	@Query(value = "SELECT answer_file_id, answer_id, application_id, application_step_id, department_id, answer_file_name, file_path, notified_file_path, delete_unnotified_flag FROM o_answer_file WHERE application_id = :applicationId AND application_step_id = :applicationStepId AND department_id = :departmentId AND delete_flag = '0' ORDER BY answer_file_id ASC", nativeQuery = true)
	List<AnswerFile> findByAnswerIdWithoutDeletedForGoverment2(@Param("applicationId") Integer applicationId,@Param("applicationStepId") Integer applicationStepId,@Param("departmentId") String departmentId);
	
	/**
	 * O_回答ファイル検索(事業者用、3許可判定用)
	 * 
	 * @param answerId 回答ID
	 * @param applicationId 申請ID
	 * @param applicationStepId 申請段階ID
	 * @return 回答ファイル一覧
	 */
	@Query(value = "SELECT answer_file_id, answer_id, application_id, application_step_id, department_id, answer_file_name, file_path, notified_file_path, delete_unnotified_flag FROM o_answer_file WHERE application_id = :applicationId AND application_step_id = :applicationStepId AND notified_file_path IS NOT NULL AND delete_flag = '0' ORDER BY answer_file_id ASC", nativeQuery = true)
	List<AnswerFile> findByAnswerIdWithoutDeletedForBusiness3(@Param("applicationId") Integer applicationId,@Param("applicationStepId") Integer applicationStepId);
	
	/**
	 * O_回答ファイル検索(行政用、3許可判定用)
	 * 
	 * @param answerId 回答ID
	 * @param applicationId 申請ID
	 * @param applicationStepId 申請段階ID
	 * @return 回答ファイル一覧
	 */
	@Query(value = "SELECT answer_file_id, answer_id, application_id, application_step_id, department_id, answer_file_name, file_path, notified_file_path, delete_unnotified_flag FROM o_answer_file WHERE application_id = :applicationId AND application_step_id = :applicationStepId AND delete_flag = '0' ORDER BY answer_file_id ASC", nativeQuery = true)
	List<AnswerFile> findByAnswerIdWithoutDeletedForGoverment3(@Param("applicationId") Integer applicationId,@Param("applicationStepId") Integer applicationStepId);
	
	/**
	 * O_回答ファイル検索
	 * 
	 * @param answerId 回答ID
	 * @return 回答ファイル一覧
	 */
	@Query(value = "SELECT answer_file_id, answer_id, application_id, application_step_id, department_id, answer_file_name, file_path, notified_file_path, delete_unnotified_flag FROM o_answer_file WHERE answer_file_id = :answerFileId ORDER BY answer_file_id ASC", nativeQuery = true)
	List<AnswerFile> findByAnswerFileId(@Param("answerFileId") Integer answerFileId);
	
	/**
	 * O_回答ファイル検索
	 * 
	 * @param applicationId 申請ID
	 * @param applicationStepId 申請段階ID
	 * @param departmentId 部署ID
	 * 
	 * @return 回答ファイル一覧
	 */
	@Query(value = "SELECT answer_file_id, answer_id, application_id, application_step_id, department_id, answer_file_name, file_path, notified_file_path, delete_unnotified_flag FROM o_answer_file WHERE application_id = :applicationId AND application_step_id = :applicationStepId AND department_id IN (:departmentId) ORDER BY answer_file_id ASC", nativeQuery = true)
	List<AnswerFile> findByDepartmentId(@Param("applicationId") Integer applicationId, @Param("applicationStepId") Integer applicationStepId, @Param("departmentId") List<String> departmentIdList);

	/**
	 * O_回答ファイル検索
	 * 
	 * @param applicationId 申請ID
	 * @param applicationStepId 申請段階ID
	 * 
	 * @return 回答ファイル一覧
	 */
	@Query(value = "SELECT answer_file_id, answer_id, application_id, application_step_id, department_id, answer_file_name, file_path, notified_file_path, delete_unnotified_flag FROM o_answer_file WHERE application_id = :applicationId AND application_step_id = :applicationStepId ORDER BY answer_file_id ASC", nativeQuery = true)
	List<AnswerFile> findByApplicationStepId(@Param("applicationId") Integer applicationId, @Param("applicationStepId") Integer applicationStepId);
	
	/**
	 * O_回答ファイル検索（未削除のみ）
	 * 
	 * @param applicationId 申請ID
	 * @param applicationStepId 申請段階ID
	 * 
	 * @return 回答ファイル一覧
	 */
	@Query(value = "SELECT answer_file_id, answer_id, application_id, application_step_id, department_id, answer_file_name, file_path, notified_file_path, delete_unnotified_flag FROM o_answer_file WHERE application_id = :applicationId AND application_step_id = :applicationStepId AND delete_flag = '0' ORDER BY answer_file_id ASC", nativeQuery = true)
	List<AnswerFile> findByApplicationStepIdUndeleted(@Param("applicationId") Integer applicationId, @Param("applicationStepId") Integer applicationStepId);

	/**
	 * O_回答ファイル検索（未削除のみ）
	 * 
	 * @param filePath ファイルパス
	 * 
	 * @return 回答ファイル一覧
	 */
	@Query(value = "SELECT answer_file_id, answer_id, application_id, application_step_id, department_id, answer_file_name, file_path, notified_file_path, delete_unnotified_flag FROM o_answer_file WHERE file_path = :filePath AND delete_flag = '0' ORDER BY answer_file_id ASC", nativeQuery = true)
	List<AnswerFile> findByFilePath(@Param("filePath") String filePath);

}
