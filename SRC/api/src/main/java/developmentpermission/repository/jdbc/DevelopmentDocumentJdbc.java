package developmentpermission.repository.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

/**
 * 開発登録簿情報JDBC
 */
@Component
@Configuration
@ComponentScan
@EnableTransactionManagement
@Transactional
public class DevelopmentDocumentJdbc extends AbstractJdbc {

	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(DevelopmentDocumentJdbc.class);

	/**
	 * O_開発登録簿登録
	 * 
	 * @param applicationId 申請ID
	 * @param developmentDocumentId 開発登録簿マスタID
	 * @param filePath ファイルパス
	 * @return 
	 */
	public Integer insert(int applicationId, int developmentDocumentId, String filePath) {
		LOGGER.debug("開発登録簿登録 開始");
		try {

			String sql = "" + //
					"INSERT INTO o_development_document( " + //
					"  file_id, " + //
					"  application_id, " + //
					"  development_document_id, " + //
					"  file_path, " + //
					"  register_datetime " + //
					") " + //
					"VALUES ( " + //
					"  nextval('seq_development_document_file'), " + //
					"  ?, " + // application_id
					"  ?, " + // development_document_id
					"  ?, " + // file_path
					"  CURRENT_TIMESTAMP  " + //
			        ")";
			jdbcTemplate.update(sql, //
					applicationId, //
					developmentDocumentId,
					filePath);
			return jdbcTemplate.queryForObject("SELECT lastval()", Integer.class);
		} finally {
			LOGGER.debug("開発登録簿登録 終了");
		}
	}
	/**
	 * O_開発登録簿削除
	 * 
	 * @param applicationId 申請ID
	 * @return 更新件数
	 */
	public int delete(int applicationId) {
		LOGGER.debug("開発登録簿削除 開始");
		try {
			String sql = "" + //
					"DELETE FROM " + //
					"  o_development_document " + //
					"WHERE " + //
					"  application_id=?"; //
			return jdbcTemplate.update(sql, //
					applicationId);
		} catch(Exception ex) {
			LOGGER.debug("開発登録簿削除 失敗");
			throw new RuntimeException();
		} finally {
			LOGGER.debug("開発登録簿削除 終了");
		}
	}
	/**
	 * O_開発登録簿削除
	 * 
	 * @param applicationId 申請ID
	 * @param developmentDocumentId 開発登録簿マスタID
	 * @return 更新件数
	 */
	public int delete(int applicationId, int developmentDocumentId) {
		LOGGER.debug("開発登録簿削除 開始");
		try {
			String sql = "" + //
					"DELETE FROM " + //
					"  o_development_document " + //
					"WHERE " + //
					"  application_id=?" + //
					"  AND development_document_id=? ";
			return jdbcTemplate.update(sql, //
					applicationId, //
					developmentDocumentId);
		} catch(Exception ex) {
			LOGGER.debug("開発登録簿削除 失敗");
			throw new RuntimeException();
		} finally {
			LOGGER.debug("開発登録簿削除 終了");
		}
	}
}
