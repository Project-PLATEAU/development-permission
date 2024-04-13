package developmentpermission.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import developmentpermission.entity.Answer;
import developmentpermission.entity.AnswerTemplate;

/**
 * M_回答テンプレートrepositoryインタフェース
 */
@Transactional
@Repository
public interface AnswerTemplateRepository extends JpaRepository<AnswerTemplate, Integer> {

	/**
	 * M_回答テンプレート
	 * @param judgementItemId 判定項目ID
	 * @return 回答テンプレート一覧
	 */
	@Query(value = "SELECT answer_template_id, disp_order, answer_template_text, judgement_item_id FROM m_answer_template WHERE judgement_item_id = :judgementItemId ORDER BY disp_order ASC", nativeQuery = true)
	List<AnswerTemplate> findAnswerTemplateByJudgementItemId(String judgementItemId);
}
