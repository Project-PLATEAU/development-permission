package developmentpermission.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import developmentpermission.entity.CategoryJudgementAuthority;
import developmentpermission.entity.key.CategoryJudgementAuthorityKey;

/**
 * M_区分判定_権限Repositoryインタフェース
 */
@Transactional
@Repository
public interface JudgementAuthorityRepository extends JpaRepository<CategoryJudgementAuthority, CategoryJudgementAuthorityKey> {

	/**
	 * 申請区分リスト取得
	 * 
	 * @param judgementItemId 判定項目ID
	 * @return 申請区分リスト
	 */
	@Query(value = "SELECT judgement_item_id, department_id FROM m_judgement_authority WHERE judgement_item_id = :judgementItemId ORDER BY department_id ASC", nativeQuery = true)
	List<CategoryJudgementAuthority> getJudgementAuthorityList(@Param("judgementItemId") String judgementItemId);

	/**
	 * 区分判定権限リスト取得
	 * 
	 * @param judgementItemId 判定項目ID
	 * @param departmentId    部署ID
	 * @return 申請区分リスト
	 */
	@Query(value = "SELECT judgement_item_id, department_id FROM m_judgement_authority WHERE judgement_item_id = :judgementItemId AND department_id = :departmentId ", nativeQuery = true)
	List<CategoryJudgementAuthority> getOneByKey(@Param("judgementItemId") String judgementItemId, @Param("departmentId") String departmentId);
}
