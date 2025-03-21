package developmentpermission.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import developmentpermission.entity.CategoryJudgementAuthority;
import developmentpermission.entity.CategoryJudgementResult;
import developmentpermission.entity.key.CategoryJudgementAuthorityKey;

/**
 * M_判定結果Repositoryインタフェース
 */
@Transactional
@Repository
public interface JudgementResultRepository extends JpaRepository<CategoryJudgementResult, CategoryJudgementAuthorityKey> {

	/**
	 * 判定結果リスト取得
	 * 
	 * @param 判定項目ID
	 * @return 判定結果のタイトル
	 */
	@Query(value = "SELECT judgement_item_id,application_type_id,application_step_id,department_id,title,applicable_summary,applicable_description,non_applicable_display_flag,non_applicable_summary,non_applicable_description,answer_require_flag,default_answer,answer_editable_flag,answer_days FROM m_judgement_result WHERE judgement_item_id = :judgementItemId", nativeQuery = true)
	List<CategoryJudgementResult> getJudgementTitleByJudgementId(@Param("judgementItemId") String judgementItemId);
	
	/**
	 * 判定結果リスト取得
	 * 
	 * @param 判定項目ID
	 * @param 部署ID
	 * @param 申請段階ID
	 * @return 判定結果のタイトル
	 */
	@Query(value = "SELECT judgement_item_id,application_type_id,application_step_id,department_id,title,applicable_summary,applicable_description,non_applicable_display_flag,non_applicable_summary,non_applicable_description,answer_require_flag,default_answer,answer_editable_flag,answer_days FROM m_judgement_result WHERE judgement_item_id = :judgementItemId AND application_step_id = :applicationStepId AND department_id = :departmentId", nativeQuery = true)
	List<CategoryJudgementResult> getJudgementTitleByJudgementIdAndApplicationStepId(@Param("judgementItemId") String judgementItemId,@Param("applicationStepId") Integer applicationStepId,@Param("departmentId") String departmentId);

	/**
	 * 判定結果リスト取得
	 * 
	 * @param 判定項目ID
	 * @param 部署ID
	 * @param 申請段階ID
	 * @return 判定結果のタイトル
	 */
	@Query(value = "SELECT judgement_item_id,application_type_id,application_step_id,department_id,title,applicable_summary,applicable_description,non_applicable_display_flag,non_applicable_summary,non_applicable_description,answer_require_flag,default_answer,answer_editable_flag,answer_days FROM m_judgement_result WHERE judgement_item_id = :judgementItemId AND application_type_id = :applicationTypeId AND application_step_id = :applicationStepId AND department_id = :departmentId", nativeQuery = true)
	List<CategoryJudgementResult> getJudgementResult(@Param("judgementItemId") String judgementItemId,@Param("applicationTypeId") Integer applicationTypeId,@Param("applicationStepId") Integer applicationStepId,@Param("departmentId") String departmentId);
}
