package developmentpermission.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import developmentpermission.entity.CategoryJudgement;
import developmentpermission.entity.CategoryJudgementAndResult;
import developmentpermission.entity.LotNumberAndDistrict;

/**
 * M_区分判定DAO
 */
public class CategoryJudgementDao extends AbstractDao {

	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(CategoryJudgementDao.class);

	/**
	 * コンストラクタ
	 * 
	 * @param emf Entityマネージャファクトリ
	 */
	public CategoryJudgementDao(EntityManagerFactory emf) {
		super(emf);
	}

	/**
	 * 区分判定一覧取得
	 * 
	 * @return 区分判定一覧
	 */
	@SuppressWarnings("unchecked")
	public List<CategoryJudgement> getCategoryJudgementList() {
		LOGGER.debug("区分判定一覧取得 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();
			String sql = "" + //
					"SELECT " + //
					"  judgement_item_id, " + //
					"  department_id, " + //
					"  category_1, " + //
					"  category_2, " + //
					"  category_3, " + //
					"  category_4, " + //
					"  category_5, " + //
					"  category_6, " + //
					"  category_7, " + //
					"  category_8, " + //
					"  category_9, " + //
					"  category_10, " + //
					"  gis_judgement, " + //
					"  buffer, " + //
					"  display_attribute_flag, " + //
					"  judgement_layer, " + //
					"  title, " + //
					"  applicable_summary, " + //
					"  applicable_description, " + //
					"  non_applicable_display_flag, " + //
					"  non_applicable_summary, " + //
					"  non_applicable_description, " + //
					"  table_name, " + //
					"  field_name, " + //
					"  non_applicable_layer_display_flag, " + //
					"  simultaneous_display_layer, " + //
					"  simultaneous_display_layer_flag, " + //
					"  answer_require_flag, " + //
					"  default_answer, " + //
					"  answer_editable_flag, " + //
					"  answer_days " + //
					"FROM " + //
					"  m_category_judgement " + //
					"ORDER BY " + //
					"  disp_order ASC";

			return em.createNativeQuery(sql, CategoryJudgement.class).getResultList();
		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("区分判定一覧取得 終了");
		}
	}

	/**
	 * 区分判定一覧取得
	 * 
	 * @param judgementItemId 判定項目ID
	 * @return 区分判定一覧
	 */
	@SuppressWarnings("unchecked")
	public List<CategoryJudgement> getCategoryJudgementListById(String judgementItemId) {
		LOGGER.debug("区分判定一覧取得 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();
			String sql = "" + //
					"SELECT " + //
					"  judgement_item_id, " + //
					"  department_id, " + //
					"  category_1, " + //
					"  category_2, " + //
					"  category_3, " + //
					"  category_4, " + //
					"  category_5, " + //
					"  category_6, " + //
					"  category_7, " + //
					"  category_8, " + //
					"  category_9, " + //
					"  category_10, " + //
					"  gis_judgement, " + //
					"  buffer, " + //
					"  display_attribute_flag, " + //
					"  judgement_layer, " + //
					"  title, " + //
					"  applicable_summary, " + //
					"  applicable_description, " + //
					"  non_applicable_display_flag, " + //
					"  non_applicable_summary, " + //
					"  non_applicable_description, " + //
					"  table_name, " + //
					"  field_name, " + //
					"  non_applicable_layer_display_flag, " + //
					"  simultaneous_display_layer, " + //
					"  simultaneous_display_layer_flag, " + //
					"  answer_require_flag, " + //
					"  default_answer, " + //
					"  answer_editable_flag, " + //
					"  answer_days " + //
					"FROM " + //
					"  m_category_judgement " + //
					"WHERE " + //
					"  judgement_item_id = :judgementItemId " + //
					"ORDER BY " + //
					"  disp_order ASC";

			return em.createNativeQuery(sql, CategoryJudgement.class).setParameter("judgementItemId", judgementItemId)
					.getResultList();
		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("区分判定一覧取得 終了");
		}
	}
	
	/**
	 * 区分判定一覧取得
	 * @param applicationTypeId 申請種類ID
	 * @param applicationStepId 申請段階ID
	 * 
	 * @return 区分判定一覧
	 */
	@SuppressWarnings("unchecked")
	public List<CategoryJudgementAndResult> getCategoryJudgementList(int applicationTypeId, int applicationStepId ) {
		LOGGER.debug("区分判定一覧取得 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();
			String sql = "" + //
					"SELECT " + //
					"  a.judgement_item_id AS judgement_item_id,  " + //
					"  b.application_type_id AS application_type_id,  " + //
					"  b.application_step_id AS application_step_id,  " + //
					"  b.department_id AS department_id,  " + //
					"  a.gis_judgement AS gis_judgement,  " + //
					"  a.buffer AS buffer,  " + //
					"  a.display_attribute_flag AS display_attribute_flag,  " + //
					"  a.judgement_layer AS judgement_layer,  " + //
					"  a.table_name AS table_name,  " + //
					"  a.field_name AS field_name,  " + //
					"  a.non_applicable_layer_display_flag AS non_applicable_layer_display_flag,  " + //
					"  a.simultaneous_display_layer AS simultaneous_display_layer,  " + //
					"  a.simultaneous_display_layer_flag AS simultaneous_display_layer_flag,  " + //
					"  b.title AS title,  " + //
					"  b.applicable_summary AS applicable_summary,  " + //
					"  b.applicable_description AS applicable_description,  " + //
					"  b.non_applicable_display_flag AS non_applicable_display_flag,  " + //
					"  b.non_applicable_summary AS non_applicable_summary,  " + //
					"  b.non_applicable_description AS non_applicable_description,  " + //
					"  b.answer_require_flag AS answer_require_flag,  " + //
					"  b.default_answer AS default_answer,  " + //
					"  b.answer_days AS answer_days,  " + //
					"  b.answer_editable_flag AS answer_editable_flag  " + //
					"FROM " + //
					"  m_category_judgement AS a  " + //
					"  INNER JOIN m_judgement_result AS b  " + //
					"    ON a.judgement_item_id = b.judgement_item_id  " + //
					"WHERE " + //
					"  b.application_type_id  = :applicationTypeId   " + //
					"  AND b.application_step_id  = :applicationStepId   " + //
					"ORDER BY " + //
					"  a.disp_order,  " + //
					"  b.department_id ASC ";

			Query query = em.createNativeQuery(sql, CategoryJudgementAndResult.class);
			query = query.setParameter("applicationTypeId", applicationTypeId);
			query = query.setParameter("applicationStepId", applicationStepId);
			return query.getResultList();
		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("区分判定一覧取得 終了");
	
		}
	}
	
	/**
	 * 区分判定一覧取得
	 * @param applicationTypeId 申請種類ID
	 * @param applicationStepId 申請段階ID
	 * @param judgementItemId   判定項目ID
	 * 
	 * @return 区分判定一覧
	 */
	@SuppressWarnings("unchecked")
	public List<CategoryJudgementAndResult> getCategoryJudgementListById(int applicationTypeId, int applicationStepId, String judgementItemId ) {
		LOGGER.debug("区分判定一覧取得 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();
			String sql = "" + //
					"SELECT " + //
					"  a.judgement_item_id AS judgement_item_id,  " + //
					"  b.application_type_id AS application_type_id,  " + //
					"  b.application_step_id AS application_step_id,  " + //
					"  b.department_id AS department_id,  " + //
					"  a.gis_judgement AS gis_judgement,  " + //
					"  a.buffer AS buffer,  " + //
					"  a.display_attribute_flag AS display_attribute_flag,  " + //
					"  a.judgement_layer AS judgement_layer,  " + //
					"  a.table_name AS table_name,  " + //
					"  a.field_name AS field_name,  " + //
					"  a.non_applicable_layer_display_flag AS non_applicable_layer_display_flag,  " + //
					"  a.simultaneous_display_layer AS simultaneous_display_layer,  " + //
					"  a.simultaneous_display_layer_flag AS simultaneous_display_layer_flag,  " + //
					"  b.title AS title,  " + //
					"  b.applicable_summary AS applicable_summary,  " + //
					"  b.applicable_description AS applicable_description,  " + //
					"  b.non_applicable_display_flag AS non_applicable_display_flag,  " + //
					"  b.non_applicable_summary AS non_applicable_summary,  " + //
					"  b.non_applicable_description AS non_applicable_description,  " + //
					"  b.answer_require_flag AS answer_require_flag,  " + //
					"  b.default_answer AS default_answer,  " + //
					"  b.answer_days AS answer_days,  " + //
					"  b.answer_editable_flag AS answer_editable_flag  " + //
					"FROM " + //
					"  m_category_judgement AS a  " + //
					"  INNER JOIN m_judgement_result AS b  " + //
					"    ON a.judgement_item_id = b.judgement_item_id  " + //
					"WHERE " + //
					"  b.application_type_id  = :applicationTypeId   " + //
					"  AND b.application_step_id  = :applicationStepId   " + //
					"  AND b.judgement_item_id = :judgementItemId " + //
					"ORDER BY " + //
					"  a.disp_order,  " + //
					"  b.department_id ASC ";

			Query query = em.createNativeQuery(sql, CategoryJudgementAndResult.class);
			query = query.setParameter("applicationTypeId", applicationTypeId);
			query = query.setParameter("applicationStepId", applicationStepId);
			query = query.setParameter("judgementItemId", judgementItemId);
			return query.getResultList();
		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("区分判定一覧取得 終了");
	
		}
	}
}
