package developmentpermission.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import developmentpermission.entity.Answer;
import developmentpermission.entity.AnswerFile;
import developmentpermission.entity.AnswerFileHistoryView;
import developmentpermission.entity.AnswerHistory;
import developmentpermission.entity.ApplicationFileMaster;
import developmentpermission.entity.CategoryJudgement;
import developmentpermission.entity.CategoryJudgementAuthority;
import developmentpermission.entity.CategoryJudgementResult;
import developmentpermission.entity.Department;
import developmentpermission.entity.DepartmentAnswer;

/**
 * 回答DAO
 */
@Transactional
public class AnswerDao extends AbstractDao {

	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(AnswerDao.class);

	/**
	 * コンストラクタ
	 * 
	 * @param emf Entityマネージャファクトリ
	 */
	public AnswerDao(EntityManagerFactory emf) {
		super(emf);
	}

	/**
	 * M_部署検索
	 * 
	 * @param answerId 回答ID
	 * @return M_部署リスト
	 */
	@SuppressWarnings("unchecked")
	public List<Department> getDepartmentList(Integer answerId) {
		LOGGER.debug("M_部署検索 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();

			String sql = "" + //
					"SELECT DISTINCT " + //
					"  c.department_id AS department_id, " + //
					"  c.department_name AS department_name, " + //
					"  c.answer_authority_flag AS answer_authority_flag, " + //
					"  c.mail_address AS mail_address, " + //
					"  c.admin_mail_address AS admin_mail_address " + //
					"FROM " + //
					"  o_answer AS a " + //
					"INNER JOIN " + //
					"  m_judgement_authority AS b " + //
					"ON " + //
					"  a.judgement_id = b.judgement_item_id " + //
					"INNER JOIN " + //
					"  m_department AS c " + //
					"ON " + //
					"  b.department_id = c.department_id " + //
					"WHERE " + //
					"  a.answer_id = :answerId " + //
					"ORDER BY " + //
					"  department_id ASC";
			Query query = em.createNativeQuery(sql, Department.class);
			query = query.setParameter("answerId", answerId);
			return query.getResultList();
		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("M_部署検索 終了");
		}
	}

	/**
	 * M_区分判定_権限検索
	 * 
	 * @param chatId 回答ID
	 * @return M_区分判定_権限リスト
	 */
	@SuppressWarnings("unchecked")
	public List<CategoryJudgementAuthority> getJudgementAuthorityList(int chatId) {
		LOGGER.debug("M_区分判定_権限検索 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();

			String sql = "" + //

					" SELECT" + //
					"  t1.department_id ," + //
					"  t1.judgement_item_id " + //
					" FROM " + //
					" m_judgement_authority t1" + //
					" INNER JOIN ( " + //
					"  SELECT " + //
					"   t4.judgement_id " + //
					"  FROM" + //
					"   o_answer t4 " + //
					"  INNER JOIN (" + //
					"   SELECT" + //
					"    t2.answer_id" + //
					"   FROM" + //
					"    o_chat t2" + //
					"   WHERE" + //
					"    t2.chat_id = :chatId" + //
					"   ) t3" + //
					" ON" + //
					"  t4.answer_id = t3.answer_id" + //
					" ) t5" + //
					" ON" + //
					"  t1.judgement_item_id = t5.judgement_id";

			Query query = em.createNativeQuery(sql, CategoryJudgementAuthority.class);
			query = query.setParameter("chatId", chatId);
			return query.getResultList();
		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("M_区分判定_権限検索 終了");
		}
	}

	/**
	 * 回答ファイル更新履歴取得
	 * 
	 * @param applicationId 申請ID
	 * @return 回答ファイル更新履歴リスト
	 */
	@SuppressWarnings("unchecked")
	public List<AnswerFileHistoryView> getAnswerFileHistoryList(int applicationId) {
		LOGGER.debug("回答ファイル更新履歴取得 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();

			String sql = "" + //
					"SELECT " + //
					"  a.answer_file_history_id AS answer_file_history_id, " + //
					"  a.answer_file_id AS answer_file_id, " + //
					"  a.answer_id AS answer_id, " + //
					"  a.update_type AS update_type, " + //
					"  a.update_user_id AS update_user_id, " + //
					"  a.update_datetime AS update_datetime, " + //
					"  a.notify_flag AS notify_flag," + //
					"  b.answer_file_name AS answer_file_name," + //
					"  c.judgement_result AS judgement_result, " + //
					"  d.user_name AS user_name, " + //
					"  e.department_id AS department_id, " + //
					"  e.department_name AS department_name " + //
					"FROM o_answer_file_history AS a " + //
					"  LEFT OUTER JOIN o_answer_file AS b " + //
					"    ON a.answer_file_id = b.answer_file_id " + //
					"  LEFT OUTER JOIN o_answer AS c " + //
					"    ON a.answer_id = c.answer_id " + //
					"  LEFT OUTER JOIN m_government_user AS d " + //
					"    ON a.update_user_id = d.user_id " + //
					"  LEFT OUTER JOIN m_department AS e " + //
					"    ON d.department_id = e.department_id " + //
					"WHERE c.application_id = :applicationId ORDER BY a.update_datetime DESC";
			Query query = em.createNativeQuery(sql, AnswerFileHistoryView.class);
			query = query.setParameter("applicationId", applicationId);
			return query.getResultList();
		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("回答ファイル更新履歴取得 終了");
		}
	}

	/**
	 * 判定結果取得
	 * 
	 * @param judgementItemId   判定項目ID
	 * @param applicationTypeId 申請種類ID
	 * @param applicationStepId 申請段階ID
	 * @param departmentId      部署ID
	 * 
	 * @return 判定結果リスト
	 */
	@SuppressWarnings("unchecked")
	public List<CategoryJudgementResult> getJudgementResultList(String judgementItemId, int applicationTypeId,
			int applicationStepId, String departmentId) {
		LOGGER.debug("判定結果取得 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();

			String sql = "" + //
					"SELECT " + //
					"    judgement_item_id, " + //
					"    application_type_id, " + //
					"    application_step_id, " + //
					"    department_id, " + //
					"    title, " + //
					"    COALESCE(answer_days, 0) AS answer_days, " + //
					"    default_answer, " + //
					"    applicable_summary, " + //
					"    applicable_description, " + //
					"    non_applicable_display_flag, " + //
					"    non_applicable_summary, " + //
					"    non_applicable_description, " + //
					"    answer_require_flag, " + //
					"    answer_editable_flag " + //
					"FROM " + //
					"    m_judgement_result " + //
					"WHERE " + //
					"    judgement_item_id = :judgementItemId " + //
					"    AND application_type_id = :applicationTypeId " + //
					"    AND application_step_id = :applicationStepId ";
			if (applicationStepId == 2) {
				if (departmentId != null && !"".equals(departmentId)) {
					sql += "    AND department_id = :departmentId ";
				}
			} else {
				sql += "    AND department_id = '-1' ";
			}

			Query query = em.createNativeQuery(sql, CategoryJudgementResult.class);
			query = query.setParameter("judgementItemId", judgementItemId);
			query = query.setParameter("applicationTypeId", applicationTypeId);
			query = query.setParameter("applicationStepId", applicationStepId);
			if (applicationStepId == 2) {
				if (departmentId != null && !"".equals(departmentId)) {
					query = query.setParameter("departmentId", departmentId);
				}
			}
			return query.getResultList();
		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("判定結果取得 終了");
		}
	}

	/**
	 * 回答ファイル一覧取得
	 * 
	 * @param applicationId     申請ID
	 * @param applicationStepId 申請段階ID
	 * @param departmentId      部署ID
	 * @param isGoverment       行政かどうか
	 * 
	 * @return 回答ファイル一覧
	 */
	@SuppressWarnings("unchecked")
	public List<AnswerFile> getAnswerFileList(int applicationId, int applicationStepId, String departmentId,
			boolean isGoverment) {
		LOGGER.debug("回答ファイル一覧取得 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();

			String sql = "" + //
					"SELECT " + //
					"    answer_file_id, " + //
					"    answer_id, " + //
					"    application_id, " + //
					"    application_step_id, " + //
					"    department_id, " + //
					"    answer_file_name, " + //
					"    file_path, " + //
					"    notified_file_path, " + //
					"    delete_unnotified_flag " + //
					"FROM " + //
					"    o_answer_file " + //
					"WHERE " + //
					"    application_id = :applicationId " + //
					"    AND application_step_id = :applicationStepId " + //
					"    AND delete_flag = '0' ";
			if (applicationStepId == 2 && departmentId != null && !"".equals(departmentId)) {
				sql += "    AND department_id = :departmentId ";
			}

			if (isGoverment) {
				sql += "    AND delete_unnotified_flag = '0' ";
			} else {
				sql += "    AND notified_file_path IS NOT NULL ";
			}
			sql += "ORDER BY " + //
					"  answer_file_id ASC";

			Query query = em.createNativeQuery(sql, AnswerFile.class);
			query = query.setParameter("applicationId", applicationId);
			query = query.setParameter("applicationStepId", applicationStepId);
			if (applicationStepId == 2 && departmentId != null && !"".equals(departmentId)) {
				query = query.setParameter("departmentId", departmentId);
			}
			return query.getResultList();
		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("回答ファイル一覧取得 終了");
		}
	}

	/**
	 * 回答履歴一覧取得
	 * 
	 * @param applicationId     申請ID
	 * @param applicationStepId 申請段階ID
	 * @param answerId          回答ID
	 * @param departmentAnswer  部署回答ID
	 * @param isGoverment       行政かどうか
	 * 
	 * @return 回答履歴一覧
	 */
	@SuppressWarnings("unchecked")
	public List<AnswerHistory> getAnswerHistoryList(int applicationId, int applicationStepId, int answerId,
			int departmentAnswer, boolean isGoverment) {
		LOGGER.debug("回答履歴一覧取得 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();

			String sql = "" + //
					"SELECT " + //
					"    a.answer_history_id AS answer_history_id,  " + //
					"    a.answer_id AS answer_id,  " + //
					"    a.answer_user_id AS answer_user_id,  " + //
					"    a.answer_datetime AS answer_datetime,  " + //
					"    a.answer_text AS answer_text,  " + //
					"    a.notify_flag AS notify_flag,  " + //
					"    a.discussion_flag AS discussion_flag,  " + //
					"    a.discussion_item AS discussion_item,  " + //
					"    a.business_pass_status AS business_pass_status,  " + //
					"    a.business_pass_comment AS business_pass_comment,  " + //
					"    a.government_confirm_status AS government_confirm_status,  " + //
					"    a.government_confirm_datetime AS government_confirm_datetime,  " + //
					"    a.government_confirm_comment AS government_confirm_comment,  " + //
					"    a.re_application_flag AS re_application_flag,  " + //
					"    a.permission_judgement_result AS permission_judgement_result,  " + //
					"    a.answer_status AS answer_status,  " + //
					"    a.answer_data_type AS answer_data_type,  " + //
					"    a.update_datetime AS update_datetime,  " + //
					"    a.deadline_datetime AS deadline_datetime  " + //
					"FROM " + //
					"    o_answer_history AS a  " + //
					"    LEFT OUTER JOIN o_answer b  " + //
					"        ON a.answer_id = b.answer_id  " + //
					"WHERE " + //
					"    b.application_id = :applicationId  " + //
					"    AND b.application_step_id = :applicationStepId  " + //
					"    AND a.answer_user_id <> '-1'  ";// 申請登録時点で自動作成された履歴を除く
			// 回答ごとの回答履歴一覧
			if (answerId > 0) {
				sql += "    AND b.answer_id = :answerId ";
			}
			// 部署回答ごとの回答履歴一覧
			if (departmentAnswer > 0) {
				sql += "    AND b.department_answer_id = :departmentAnswer ";
			}
			// 事業者の場合、
			if (!isGoverment) {
				sql += "    AND b.delete_flag ='0'  " + // 行政で削除した回答の履歴が閲覧できない
					   "    AND b.notified_flag ='1'  " + // 回答が通知済み
					   "    AND a.notify_flag ='1'  "; // 回答履歴が通知済み
			}

			sql += "ORDER BY " + //
					"    answer_datetime DESC ";

			Query query = em.createNativeQuery(sql, AnswerHistory.class);
			query = query.setParameter("applicationId", applicationId);
			query = query.setParameter("applicationStepId", applicationStepId);
			if (answerId > 0) {

				query = query.setParameter("answerId", answerId);
			}
			if (departmentAnswer > 0) {

				query = query.setParameter("departmentAnswer", departmentAnswer);
			}
			return query.getResultList();
		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("回答履歴一覧 終了");
		}
	}

	/**
	 * 回答IDに対する回答を取得
	 * 
	 * @param answerId 回答ID
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Answer> getAnswerList(int answerId) {
		LOGGER.debug("回答取得 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();

			String sql = "" + //
					"SELECT " + //
					"  answer_id, " + //
					"  application_id,  " + //
					"  application_step_id,  " + //
					"  judgement_id,  " + //
					"  judgement_result_index,  " + //
					"  department_answer_id,  " + //
					"  department_id, " + //
					"  judgement_result,  " + //
					"  answer_content,  " + //
					"  notified_text, " + //
					"  register_datetime,  " + //
					"  update_datetime,  " + //
					"  complete_flag,  " + //
					"  notified_flag,  " + //
					"  answer_update_flag,  " + //
					"  re_application_flag,  " + //
					"  business_reapplication_flag,  " + //
					"  discussion_flag,  " + //
					"  discussion_item,  " + //
					"  business_pass_status,  " + //
					"  business_pass_comment,  " + //
					"  business_answer_datetime,  " + //
					"  government_confirm_status,  " + //
					"  government_confirm_datetime,  " + //
					"  government_confirm_comment, " + //
					"  government_confirm_notified_flag,  " + //
					"  permission_judgement_result,  " + //
					"  answer_status, " + //
					"  answer_data_type,  " + //
					"  register_status,  " + //
					"  delete_unnotified_flag,  " + //
					"  deadline_datetime, " + //
					"  answer_permission_flag, " + //
					"  government_confirm_permission_flag, " + //
					"  permission_judgement_migration_flag, " + //
					"  version_information " + //
					"FROM  " + //
					"  o_answer " + //
					"WHERE  " + //
					"  answer_id = :answerId " + //
					"ORDER BY " + //
					"  answer_id ";

			Query query = em.createNativeQuery(sql, Answer.class);
			query = query.setParameter("answerId", answerId);
			return query.getResultList();
		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("回答取得 終了");
		}
	}

	/**
	 * 条項ごとの回答を取得
	 * 
	 * @param applicationId
	 * @param applicationStepId
	 * @param judgementId
	 * @param judgementResultIndex
	 * @param includeDeleteItem 削除済み回答含むかどうか
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Answer> getAnswerList(int applicationId, int applicationStepId, String judgementId, int judgementResultIndex, boolean includeDeleteItem ) {
		LOGGER.debug("回答一覧取得 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();

			String sql = "" + //
					"SELECT " + //
					"  answer_id, " + //
					"  application_id,  " + //
					"  application_step_id,  " + //
					"  judgement_id,  " + //
					"  judgement_result_index,  " + //
					"  department_answer_id,  " + //
					"  department_id, " + //
					"  judgement_result,  " + //
					"  answer_content,  " + //
					"  notified_text, " + //
					"  register_datetime,  " + //
					"  update_datetime,  " + //
					"  complete_flag,  " + //
					"  notified_flag,  " + //
					"  answer_update_flag,  " + //
					"  re_application_flag,  " + //
					"  business_reapplication_flag,  " + //
					"  discussion_flag,  " + //
					"  discussion_item,  " + //
					"  business_pass_status,  " + //
					"  business_pass_comment,  " + //
					"  business_answer_datetime,  " + //
					"  government_confirm_status,  " + //
					"  government_confirm_datetime,  " + //
					"  government_confirm_comment, " + //
					"  government_confirm_notified_flag,  " + //
					"  permission_judgement_result,  " + //
					"  answer_status, " + //
					"  answer_data_type,  " + //
					"  register_status,  " + //
					"  delete_unnotified_flag,  " + //
					"  deadline_datetime, " + //
					"  answer_permission_flag, " + //
					"  government_confirm_permission_flag, " + //
					"  permission_judgement_migration_flag, " + //
					"  version_information " + //
					"FROM  " + //
					"  o_answer " + //
					"WHERE  " + //
					"  application_id = :applicationId " + //
					"  AND application_step_id = :applicationStepId " + //
					"  AND judgement_id = :judgementId " + //
					"  AND judgement_result_index = :judgementResultIndex "; //
			if (!includeDeleteItem) {
				sql += "  AND delete_flag = '0' "; // 未削除の回答
			}
			sql += "" + //
					"ORDER BY " + //
					"  answer_id ";

			Query query = em.createNativeQuery(sql, Answer.class);
			query = query.setParameter("applicationId", applicationId);
			query = query.setParameter("applicationStepId", applicationStepId);
			query = query.setParameter("judgementId", judgementId);
			query = query.setParameter("judgementResultIndex", judgementResultIndex);
			return query.getResultList();
		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("回答一覧取得 終了");
		}
	}

	/**
	 * 最新の回答履歴
	 * 
	 * @param answerId 回答ID
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<AnswerHistory> getAnswerHistoryMax(int answerId) {
		LOGGER.debug("最新の回答履歴取得 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();
			String sql = "" + //
					"WITH max_answer_history AS" + //
					"( " + //
					"  SELECT a.answer_history_id " + //
					"  FROM  o_answer_history AS a " + //
					"  INNER JOIN " + //
					"   ( " + //
					"     SELECT " + //
					"       answer_id, " + //
					"       MAX(answer_datetime) AS max_v " + //
					"     FROM " + //
					"       o_answer_history " + //
					"     GROUP BY answer_id " + //
					"   ) AS b " + //
					"   ON a.answer_id = b.answer_id AND a.answer_datetime = b.max_v " + //
					"   WHERE a.answer_id = :answerId" + //
					"   AND a.answer_user_id <> '-1'" + //
					") " + //
					"SELECT " + //
					"  o.answer_history_id, " + //
					"  o.answer_id,  " + //
					"  o.answer_user_id,  " + //
					"  o.answer_datetime,  " + //
					"  o.answer_text,  " + //
					"  o.notify_flag,  " + //
					"  o.discussion_item,  " + //
					"  o.business_pass_status, " + //
					"  o.business_pass_comment,  " + //
					"  o.government_confirm_status,  " + //
					"  o.government_confirm_datetime,  " + //
					"  o.government_confirm_comment,  " + //
					"  o.permission_judgement_result,  " + //
					"  o.re_application_flag,  " + //
					"  o.discussion_flag,  " + //
					"  o.answer_status,  " + //
					"  o.answer_data_type,  " + //
					"  o.update_datetime,  " + //
					"  o.deadline_datetime  " + //
					"FROM  " + //
					"  o_answer_history AS o" + //
					"  INNER JOIN max_answer_history AS max" + //
					"  ON   " + //
					"  max.answer_history_id = o.answer_history_id "; //
			Query query = em.createNativeQuery(sql, AnswerHistory.class);
			query = query.setParameter("answerId", answerId);
			return query.getResultList();
		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("最新の回答履歴取得 終了");
		}
	}
	
	/**
	 * 判定項目IDから部署情報を取得
	 * 
	 * @param judgementId 判定項目ID
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Department> getDepartmentListByJudgementId(Integer JudgementId) {
		LOGGER.debug("部署回答部署検索 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();
			String sql = "" + //
					"SELECT " + //
					"md.department_id AS department_id " + //
					", md.department_name AS department_name " + //
					", md.answer_authority_flag AS answer_authority_flag " + //
					", md.mail_address AS mail_address " + //
					", md.admin_mail_address AS admin_mail_address " + //
					" FROM " + //
					"  m_department AS md " + //
					" LEFT OUTER JOIN  " + //
					"  m_judgement_authority AS mj " + //
					" ON  " + //
					"  md.department_id = mj.department_id " + //
					" WHERE  " + //
					" mj.judgement_item_id = CAST(:JudgementId AS VARCHAR) ";
			Query query = em.createNativeQuery(sql, Department.class);
			query = query.setParameter("JudgementId", JudgementId);
			return query.getResultList();
		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("部署回答部署検索 終了");
		}
	}
	
	/**
	 * 申請ファイルマスタIDから部署回答一覧取得
	 * 
	 * @param applicationId     申請ID
	 * @param applicationStepId 申請段階ID
	 * @param applicationFileMasterId        マスタ申請ファイルID
	 * 
	 * @return 部署回答一覧
	 */
	@SuppressWarnings("unchecked")
	public List<DepartmentAnswer> getDepartmentAnswerList(int applicationId, int applicationStepId,
			List<String> applicationFileMasterId) {
		LOGGER.debug("部署回答一覧取得 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();

			String sql = "" + //
					"SELECT DISTINCT " + //
					"    a.department_answer_id AS department_answer_id,  " + //
					"    a.application_id AS application_id,  " + //
					"    a.application_step_id AS application_step_id,  " + //
					"    a.department_id AS department_id,  " + //
					"    a.government_confirm_status AS government_confirm_status,  " + //
					"    a.government_confirm_datetime AS government_confirm_datetime,  " + //
					"    a.government_confirm_comment AS government_confirm_comment,  " + //
					"    a.notified_text AS notified_text,  " + //
					"    a.complete_flag AS complete_flag,  " + //
					"    a.notified_flag AS notified_flag,  " + //
					"    a.register_datetime AS register_datetime,  " + //
					"    a.update_datetime AS update_datetime,  " + //
					"    a.register_status AS register_status,  " + //
					"    a.delete_unnotified_flag AS delete_unnotified_flag,  " + //
					"    a.government_confirm_permission_flag AS government_confirm_permission_flag  " + //
					"FROM " + //
					"    o_department_answer AS a  " + //
					"    INNER JOIN o_answer AS b  " + //
					"        ON a.department_answer_id = b.department_answer_id " + //
					"    INNER JOIN m_application_file AS c  " + //
					"        ON b.judgement_id = c.judgement_item_id " + //
					"WHERE " + //
					"    b.application_id = :applicationId  " + //
					"    AND b.application_step_id = :applicationStepId  " + //
					"    AND a.government_confirm_permission_flag = '1'  " + // 統括部署管理者に通知ずみ
					"    AND c.application_file_id IN ( :applicationFileMasterId) "; // 統括部署管理者に通知ずみ

			sql += "ORDER BY " + //
					"    department_answer_id DESC ";

			Query query = em.createNativeQuery(sql, DepartmentAnswer.class);
			query = query.setParameter("applicationId", applicationId);
			query = query.setParameter("applicationStepId", applicationStepId);
			query = query.setParameter("applicationFileMasterId", applicationFileMasterId);
			return query.getResultList();
		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("部署回答一覧 終了");
		}
	}
	
	/**
	 * 回答IDから判定結果取得
	 * 
	 * @param answerId   回答ID
	 * 
	 * @return 判定結果リスト
	 */
	@SuppressWarnings("unchecked")
	public List<CategoryJudgementResult> getJudgementResultByAnswerId(int answerId) {
		LOGGER.debug("回答IDから判定結果取得 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();

			String sql = "" + //
					"SELECT " + //
					"  c.judgement_item_id " + //
					"  , c.application_type_id " + //
					"  , c.application_step_id " + //
					"  , c.department_id " + //
					"  , c.title " + //
					"  , COALESCE(c.answer_days, 0) AS answer_days " + //
					"  , c.default_answer " + //
					"  , c.applicable_summary " + //
					"  , c.applicable_description " + //
					"  , c.non_applicable_display_flag " + //
					"  , c.non_applicable_summary " + //
					"  , c.non_applicable_description " + //
					"  , c.answer_require_flag " + //
					"  , c.answer_editable_flag " + //
					"FROM " + //
					"  o_answer AS a " + //
					"  INNER JOIN o_application AS b " + //
					"    ON a.application_id = b.application_id " + //
					"  INNER JOIN m_judgement_result AS c " + //
					"    ON c.judgement_item_id = a.judgement_id " + //
					"    AND c.application_type_id = b.application_type_id " + //
					"    AND c.application_step_id = a.application_step_id " + //
					"    AND c.department_id = a.department_id " + //
					"WHERE " + //
					"  a.answer_id = :answerId " + //
					"ORDER BY " + //
					"  c.judgement_item_id ";
			Query query = em.createNativeQuery(sql, CategoryJudgementResult.class);
			query = query.setParameter("answerId", answerId);
			return query.getResultList();
		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("回答IDから判定結果取得 終了");
		}
	}
	
	/**
	 * 受付回答IDから判定結果取得
	 * 
	 * @param acceptingAnswerId 受付回答ID
	 * 
	 * @return 判定結果リスト
	 */
	@SuppressWarnings("unchecked")
	public List<CategoryJudgementResult> getJudgementResultByAcceptingAnswerId(int acceptingAnswerId) {
		LOGGER.debug("回答IDから判定結果取得 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();

			String sql = "" + //
					"SELECT " + //
					"  c.judgement_item_id " + //
					"  , c.application_type_id " + //
					"  , c.application_step_id " + //
					"  , c.department_id " + //
					"  , c.title " + //
					"  , COALESCE(c.answer_days, 0) AS answer_days " + //
					"  , c.default_answer " + //
					"  , c.applicable_summary " + //
					"  , c.applicable_description " + //
					"  , c.non_applicable_display_flag " + //
					"  , c.non_applicable_summary " + //
					"  , c.non_applicable_description " + //
					"  , c.answer_require_flag " + //
					"  , c.answer_editable_flag " + //
					"FROM " + //
					"  o_accepting_answer AS a " + //
					"  INNER JOIN o_application AS b " + //
					"    ON a.application_id = b.application_id " + //
					"  INNER JOIN m_judgement_result AS c " + //
					"    ON c.judgement_item_id = a.judgement_id " + //
					"    AND c.application_type_id = b.application_type_id " + //
					"    AND c.application_step_id = a.application_step_id " + //
					"    AND c.department_id = a.department_id " + //
					"WHERE " + //
					"  a.accepting_answer_id = :acceptingAnswerId " + //
					"ORDER BY " + //
					"  c.judgement_item_id ";
			Query query = em.createNativeQuery(sql, CategoryJudgementResult.class);
			query = query.setParameter("acceptingAnswerId", acceptingAnswerId);
			return query.getResultList();
		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("回答IDから判定結果取得 終了");
		}
	}
	
	/**
	 * 許可判定回答担当課取得
	 * 
	 * @return M_部署リスト
	 */
	@SuppressWarnings("unchecked")
	public List<Department> getPermissionJudgementDepartmentList() {
		LOGGER.debug("M_部署検索 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();

			String sql = "" + //
					"SELECT DISTINCT " + //
					"  b.department_id AS department_id, " + //
					"  b.department_name AS department_name, " + //
					"  b.answer_authority_flag AS answer_authority_flag, " + //
					"  b.mail_address AS mail_address, " + //
					"  b.admin_mail_address AS admin_mail_address " + //
					"FROM " + //
					"  m_authority AS a " + //
					"INNER JOIN " + //
					"  m_department AS b " + //
					"ON " + //
					"  a.department_id = b.department_id " + //
					"WHERE " + //
					"  a.application_step_id = 3 " + //
					"  AND a.answer_authority_flag <> '0' " + //
					"ORDER BY " + //
					"  department_id ASC";
			Query query = em.createNativeQuery(sql, Department.class);
			return query.getResultList();
		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("M_部署検索 終了");
		}
	}
	
	/**
	 * 統括部署取得
	 * 
	 * @return M_部署リスト
	 */
	@SuppressWarnings("unchecked")
	public List<Department> getControlDepartmentList() {
		LOGGER.debug("M_部署検索 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();

			String sql = "" + //
					"SELECT DISTINCT " + //
					"  a.department_id AS department_id, " + //
					"  a.department_name AS department_name, " + //
					"  a.answer_authority_flag AS answer_authority_flag, " + //
					"  a.mail_address AS mail_address, " + //
					"  a.admin_mail_address AS admin_mail_address " + //
					"FROM " + //
					"  m_department AS a " + //
					"WHERE " + //
					"  a.answer_authority_flag = '1' " + //
					"ORDER BY " + //
					"  department_id ASC";
			Query query = em.createNativeQuery(sql, Department.class);
			return query.getResultList();
		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("M_部署検索 終了");
		}
	}
	
	/**
	 * 部署ごとのO_回答又は、O_受付回答指定版にある判定項目に対するM_申請ファイル一覧検索 
	 * ※事前協議⇒事前協議のみ
	 * 
	 * @param applicationId               申請ID
	 * @param applicationStepId           申請段階ID
	 * @param versionInformation          版情報
	 * @param departmentId                部署ID
	 * @param applicationFileMasterIdList 申請ファイルマスタIDリスト
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<ApplicationFileMaster> getApplicationFileMasterList(int applicationId, int applicationStepId,
			int versionInformation, String departmentId, List<String> applicationFileMasterIdList) {
		LOGGER.debug("M_申請ファイル一覧検索 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();

			String sql = "" + //
					"SELECT DISTINCT " + //
					"  a.application_file_id AS application_file_id " + //
					"  , '' AS judgement_item_id " + //
					"  , a.require_flag AS require_flag " + //
					"  , a.upload_file_name AS upload_file_name " + //
					"  , a.extension AS extension " + //
					"  , a.application_file_type AS application_file_type " + //
					"FROM " + //
					"  m_application_file AS a " + //
					"  INNER JOIN o_answer AS b " + //
					"    ON a.judgement_item_id = b.judgement_id " + //
					"WHERE " + //
					"  a.application_file_id IN ( :applicationFileMasterId )  " + //
					"  AND b.application_id = :applicationId  " + //
					"  AND b.application_step_id = :applicationStepId " + //
					"  AND b.department_id = :departmentId " + //
					"UNION " + //
					"SELECT DISTINCT " + //
					"  a.application_file_id AS application_file_id " + //
					"  , '' AS judgement_item_id " + //
					"  , a.require_flag AS require_flag " + //
					"  , a.upload_file_name AS upload_file_name " + //
					"  , a.extension AS extension " + //
					"  , a.application_file_type AS application_file_type " + //
					"FROM " + //
					"  m_application_file AS a " + //
					"  INNER JOIN o_accepting_answer AS b " + //
					"    ON a.judgement_item_id = b.judgement_id " + //
					"WHERE " + //
					"  a.application_file_id IN ( :applicationFileMasterId ) " + //
					"  AND b.application_id = :applicationId " + //
					"  AND b.application_step_id = :applicationStepId " + //
					"  AND b.department_id = :departmentId " + //
					"  AND b.version_infomation = :versionInformation " + //
					"ORDER BY " + //
					"  application_file_id ASC";
			Query query = em.createNativeQuery(sql, ApplicationFileMaster.class);
			query = query.setParameter("applicationId", applicationId);
			query = query.setParameter("applicationStepId", applicationStepId);
			query = query.setParameter("versionInformation", versionInformation);
			query = query.setParameter("departmentId", departmentId);
			query = query.setParameter("applicationFileMasterId", applicationFileMasterIdList);
			return query.getResultList();
		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("M_申請ファイル一覧検索 終了");
		}
	}
}
