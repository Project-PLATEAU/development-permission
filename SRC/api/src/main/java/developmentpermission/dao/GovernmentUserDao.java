package developmentpermission.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import developmentpermission.entity.ApplicationFile;
import developmentpermission.entity.GovernmentUserAndAuthority;

/**
 * M_行政ユーザー、M_部署、M_権限DAO
 */
@Transactional
public class GovernmentUserDao extends AbstractDao {

	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(GovernmentUserDao.class);

	/**
	 * コンストラクタ
	 * 
	 * @param emf Entityマネージャファクトリ
	 */
	public GovernmentUserDao(EntityManagerFactory emf) {
		super(emf);
	}

	/**
	 * 行政ユーザーの情報取得
	 * 
	 * @param userId ユーザーID
	 * @param applicationStepId 申請段階ID
	 * 
	 * @return 行政ユーザー情報
	 */
	@SuppressWarnings("unchecked")
	public List<GovernmentUserAndAuthority> getGovernmentUserInfo(String userId, Integer applicationStepId) {
		LOGGER.debug("行政ユーザー情報取得 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();

			String sql = "" + //
					"SELECT " + //
					"    u.user_id AS user_id " + //
					"  , u.login_id AS login_id " + //
					"  , u.password AS password " + //
					"  , u.role_code AS role_code " + //
					"  , u.department_id AS department_id " + //
					"  , u.user_name AS user_name " + //
					"  , u.admin_flag AS admin_flag " + //
					"  , d.department_name AS department_name " + //
					"  , d.answer_authority_flag AS management_department_flag " + //
					"  , d.mail_address AS mail_address " + //
					"  , d.admin_mail_address AS admin_mail_address " + //
					"  , a.application_step_id AS application_step_id " + //
					"  , a.answer_authority_flag AS answer_authority_flag " + //
					"  , a.notification_authority_flag AS notification_authority_flag " + //
					"FROM " + //
					"  m_government_user AS u " + //
					"  INNER JOIN m_department AS d " + //
					"    ON u.department_id = d.department_id " + //
					"  INNER JOIN m_authority AS a " + //
					"    ON d.department_id = a.department_id " + //
					"WHERE " + //
					"  u.user_id = :userId ";//
			if (applicationStepId != null) {
				sql += "  AND a.application_step_id = :applicationStepId"; //
			} else {
				sql += "ORDER BY application_step_id ASC"; //
			}

			Query query = em.createNativeQuery(sql, GovernmentUserAndAuthority.class);
			query = query.setParameter("userId", userId);
			if (applicationStepId != null) {
				query = query.setParameter("applicationStepId", applicationStepId);
			}
			return query.getResultList();
		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("行政ユーザー情報取得 終了");
		}
	}
	
	/**
	 * 統括部署管理者の情報取得
	 * 
	 * @param applicationStepId 申請段階ID
	 * @return 統括部署管理者情報
	 */
	@SuppressWarnings("unchecked")
	public List<GovernmentUserAndAuthority> getControlDepartmentAdmin(Integer applicationStepId) {
		LOGGER.debug("統括部署管理者情報取得 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();

			String sql = "" + //
					"SELECT " + //
					"    u.user_id AS user_id " + //
					"  , u.login_id AS login_id " + //
					"  , u.password AS password " + //
					"  , u.role_code AS role_code " + //
					"  , u.department_id AS department_id " + //
					"  , u.user_name AS user_name " + //
					"  , u.admin_flag AS admin_flag " + //
					"  , d.department_name AS department_name " + //
					"  , d.answer_authority_flag AS management_department_flag " + //
					"  , d.mail_address AS mail_address " + //
					"  , d.admin_mail_address AS admin_mail_address " + //
					"  , a.application_step_id AS application_step_id " + //
					"  , a.answer_authority_flag AS answer_authority_flag " + //
					"  , a.notification_authority_flag AS notification_authority_flag " + //
					"FROM " + //
					"  m_government_user AS u " + //
					"  INNER JOIN m_department AS d " + //
					"    ON u.department_id = d.department_id " + //
					"  INNER JOIN m_authority AS a " + //
					"    ON d.department_id = a.department_id " + //
					"WHERE " + //
					"  d.answer_authority_flag = '1' " + // 統括部署
					"  AND u.admin_flag = '1' "; // 管理者
			if (applicationStepId != null) {
				sql += "  AND a.application_step_id = :applicationStepId"; // 申請段階の権限
			} else {
				sql += "ORDER BY application_step_id ASC"; //
			}

			Query query = em.createNativeQuery(sql, GovernmentUserAndAuthority.class);
			if (applicationStepId != null) {
				query = query.setParameter("applicationStepId", applicationStepId);
			}
			return query.getResultList();
		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("統括部署管理者情報取得 終了");
		}
	}
}
