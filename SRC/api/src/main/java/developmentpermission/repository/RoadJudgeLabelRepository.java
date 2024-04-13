package developmentpermission.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import developmentpermission.entity.RoadJudgeLabel;

/**
 * 道路判定ラベルRepositoryインタフェース
 */
@Transactional
@Repository
public interface RoadJudgeLabelRepository extends JpaRepository<RoadJudgeLabel, String> {

	/**
	 * 識別子とインデックス値からラベルを取得
	 * 
	 * @param idenify    識別子
	 * @param indexValue インデックス値
	 * @return
	 */
	@Query(value = "SELECT label_id, replace_identify, index_value, min_value, max_value, replace_text, index_text FROM m_road_judge_label WHERE replace_identify = :identify AND index_value=:indexValue", nativeQuery = true)
	List<RoadJudgeLabel> findRoadJudgeLabelFromIndexValue(@Param("identify") String idenify,
			@Param("indexValue") int indexValue);

	/**
	 * 識別子と値から閾値内のラベルを取得
	 * 
	 * @param idenify    識別子
	 * @param checkValue 値
	 * @return
	 */
	@Query(value = "SELECT label_id, replace_identify, index_value, min_value, max_value, replace_text, index_text FROM m_road_judge_label WHERE replace_identify = :identify AND (min_value <= :checkValue AND max_value > :checkValue) OR (min_value <= :checkValue AND max_value IS NULL) OR (min_value IS NULL AND  max_value > :checkValue)", nativeQuery = true)
	List<RoadJudgeLabel> findRoadJudgeLabelFromThresholds(@Param("identify") String idenify,
			@Param("checkValue") double checkValue);
	
	/**
	 * 識別子とインデックス文字列からラベルを取得
	 * 
	 * @param idenify    識別子
	 * @param indexValue インデックス値
	 * @return
	 */
	@Query(value = "SELECT label_id, replace_identify, index_value, min_value, max_value, replace_text, index_text FROM m_road_judge_label WHERE replace_identify = :identify AND index_text=:indexText", nativeQuery = true)
	List<RoadJudgeLabel> findRoadJudgeLabelFromIndexText(@Param("identify") String idenify,
			@Param("indexText") String indexText);
}
