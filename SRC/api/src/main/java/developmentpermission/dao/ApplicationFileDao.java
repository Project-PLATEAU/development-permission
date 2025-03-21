package developmentpermission.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import developmentpermission.entity.ApplicationFile;

/**
 * O_申請ファイルDAO
 */
@Transactional
public class ApplicationFileDao extends AbstractDao {

	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationFileDao.class);

	/**
	 * コンストラクタ
	 * 
	 * @param emf Entityマネージャファクトリ
	 */
	public ApplicationFileDao(EntityManagerFactory emf) {
		super(emf);
	}

	/**
	 * 開発登録簿最終版申請ファイル一覧取得
	 * 
	 * @param layerIdList レイヤIDリスト
	 * @return 開発登録簿最終版申請ファイル一覧
	 */
	@SuppressWarnings("unchecked")
	public List<ApplicationFile> getFinalVersionApplicationFile(Integer applicationId) {
		LOGGER.debug("開発登録簿最終版申請ファイル一覧 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();
			
			String sql = "" + //
					"SELECT " + //
					"   T1.file_id " + //
					"  ,T1.application_id " + //
					"  ,T1.application_step_id " + //
					"  ,T1.application_file_id " + //
					"  ,T1.upload_file_name " + //
					"  ,T1.file_path " + //
					"  ,T1.version_information " + //
					"  ,T1.extension " + //
					"  ,T1.upload_datetime  " + //
					"  ,T1.direction_department  " + //
					"  ,T1.revise_content  " + //
					"FROM " + //
					"  o_application_file T1 " + //
					"LEFT JOIN ( " + //
					"  SELECT " + //
					"     S1.application_file_id " + //
					"    ,MAX(S1.version_information) version_information " + //
					"  FROM " + //
					"    o_application_file S1 " + //
					"  WHERE " + //
					"        S1.application_id = :applicationId " + //
					"    AND S1.application_step_id = 3 " + //
					"    AND S1.delete_flag = '0' " + //
					"  GROUP BY " + //
					"    S1.application_file_id " + //
					") T2 ON  T2.application_file_id = T1.application_file_id " + //
					"     AND T2.version_information = T1.version_information " + //
					"WHERE " + //
					"  T1.application_id = :applicationId " + //
					"  AND T1.application_step_id = 3 " + //
					"  AND T1.delete_flag = '0' " + // 
					"ORDER BY " + //
					"  T1.file_id ";
			Query query = em.createNativeQuery(sql, ApplicationFile.class);
			query = query.setParameter("applicationId", applicationId);
			return query.getResultList();
		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("開発登録簿最終版申請ファイル一覧 終了");
		}
	}
}
