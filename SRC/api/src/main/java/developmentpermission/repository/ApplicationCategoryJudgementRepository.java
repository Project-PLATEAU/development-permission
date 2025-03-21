package developmentpermission.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import developmentpermission.entity.ApplicationCategoryJudgement;
import developmentpermission.entity.key.ApplicationCategoryJudgementKey;

/**
 * M_申請区分_区分判定Repositoryインタフェース
 */
@Repository
public interface ApplicationCategoryJudgementRepository extends JpaRepository<ApplicationCategoryJudgement, ApplicationCategoryJudgementKey> {
	
	/**
	 * 情報取得
	 * 
	 * @param viewId 画面ID
	 * @return　リスト
	 */
	@Query(value = "SELECT judgement_item_id, view_id, category_id FROM public.m_application_category_judgement WHERE view_id = :viewId", nativeQuery=true)
	List<ApplicationCategoryJudgement> getAppCategoryJudgeListforView(@Param("viewId") String viewId);
	/**
	 * 情報取得
	 * 
	 * @param itemId 判定項目ID
	 * @return　リスト
	 */
	@Query(value = "SELECT judgement_item_id, view_id, category_id FROM public.m_application_category_judgement WHERE judgement_item_id = :itemId", nativeQuery=true)
	List<ApplicationCategoryJudgement> getAppCategoryJudgeListforItemId(@Param("itemId") String itemId);
}

