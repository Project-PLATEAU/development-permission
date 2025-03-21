package developmentpermission.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import developmentpermission.entity.CategoryJudgement;

/**
 * M_区分判定Repositoryインタフェース
 */
@Transactional
@Repository
public interface CategoryJudgementRepository extends JpaRepository<CategoryJudgement, String> {

	/**
	 * 区分判定一覧取得
	 * 
	 * @return 区分判定一覧
	 */
	@Query(value = "SELECT judgement_item_id, gis_judgement, buffer, display_attribute_flag, judgement_layer, table_name, field_name, non_applicable_layer_display_flag, simultaneous_display_layer, simultaneous_display_layer_flag FROM m_category_judgement ORDER BY judgement_item_id ASC", nativeQuery = true)
	List<CategoryJudgement> getCategoryJudgementList();

	/**
	 * 区分判定一覧取得
	 * 
	 * @param judgementItemId 判定項目ID
	 * @return 区分判定一覧
	 */
	@Query(value = "SELECT judgement_item_id, gis_judgement, buffer, display_attribute_flag, judgement_layer, table_name, field_name, non_applicable_layer_display_flag, simultaneous_display_layer, simultaneous_display_layer_flag FROM m_category_judgement WHERE judgement_item_id = :judgementItemId ORDER BY judgement_item_id ASC", nativeQuery = true)
	List<CategoryJudgement> getCategoryJudgementListById(@Param("judgementItemId") String judgementItemId);
}