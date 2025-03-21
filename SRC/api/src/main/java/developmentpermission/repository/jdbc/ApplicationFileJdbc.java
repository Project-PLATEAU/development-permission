package developmentpermission.repository.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import developmentpermission.entity.Answer;
import developmentpermission.entity.ApplicationFile;
import developmentpermission.form.UploadApplicationFileForm;

/**
 * 申請ファイルJDBC
 */
@Component
@Configuration
@ComponentScan
@EnableTransactionManagement
@Transactional
public class ApplicationFileJdbc extends AbstractJdbc {

	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationFileJdbc.class);

	/**
	 * 登録
	 * 
	 * @param form パラメータ
	 * @return ファイルID
	 */
	public Integer insert(UploadApplicationFileForm form) {
		LOGGER.debug("申請ファイル登録 開始");
		try {
			String sql = "" + //
					"INSERT INTO o_application_file " + //
					"(file_id, application_id, application_file_id, upload_file_name, version_information, extension,  upload_datetime, application_step_id) "
					+ //
					"VALUES (nextval('seq_file'), ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?)";
			jdbcTemplate.update(sql, //
					form.getApplicationId(), //
					form.getApplicationFileId(), //
					form.getUploadFileName(), form.getVersionInformation(), form.getExtension(),
					form.getApplicationStepId());
			return jdbcTemplate.queryForObject("SELECT lastval()", Integer.class);
		} finally {
			LOGGER.debug("申請ファイル登録 終了");
		}
	}

	/**
	 * ファイルパス更新
	 * 
	 * @param fileId   ファイルID
	 * @param filePath ファイルパス
	 * @return 更新件数
	 */
	public int updateFilePath(int fileId, String filePath) {
		LOGGER.debug("申請ファイルパス更新 開始");
		try {
			String sql = "" + //
					"UPDATE o_application_file " + //
					"SET file_path=? " + //
					"WHERE file_id=?";
			return jdbcTemplate.update(sql, //
					filePath, //
					fileId);
		} finally {
			LOGGER.debug("申請ファイルパス更新 終了");
		}
	}
	
	/**
	 * 修正内容更新
	 * 
	 * @param fileId   ファイルID
	 * @param directionDepartment 指示元部署
	 * @param reviseContent 修正内容
	 * @return 更新件数
	 */
	public int updateReviseContent(int fileId, String directionDepartment, String reviseContent) {
		LOGGER.debug("申請ファイル修正内容更新 開始");
		try {
			String sql = "" + //
					"UPDATE o_application_file " + //
					"SET direction_department=?, revise_content=? " + //
					"WHERE file_id=?";
			return jdbcTemplate.update(sql, //
					directionDepartment, //
					reviseContent, //
					fileId);
		} finally {
			LOGGER.debug("申請ファイル修正内容更新 終了");
		}
	}

	/**
	 * 登録
	 * 
	 * @param form パラメータ
	 * @return ファイルID
	 */
	public Integer insertWithPath(UploadApplicationFileForm form) {
		LOGGER.debug("申請ファイル登録 開始");
		try {
			String sql = "" + //
					"INSERT INTO o_application_file( " + //
					"  file_id, " + //
					"  application_id, " + //
					"  application_step_id, " + //
					"  application_file_id, " + //
					"  upload_file_name, " + //
					"  file_path, " + //
					"  extension," + //
					"  version_information, " + //
					"  upload_datetime " + //
					") " + //
					"VALUES ( " + //
					"  nextval('seq_file'), " + //
					"  ?, " + //
					"  ?," + //
					"  ?, " + //
					"  ?," + //
					"  ?, " + //
					"  ?, " + //
					"  ?, " + //
					"  CURRENT_TIMESTAMP " + //
					")";//

			jdbcTemplate.update(sql, //
					form.getApplicationId(), //
					form.getApplicationStepId(), form.getApplicationFileId(), //
					form.getUploadFileName(), form.getFilePath(), form.getExtension(), form.getVersionInformation());
			return jdbcTemplate.queryForObject("SELECT lastval()", Integer.class);
		} finally {
			LOGGER.debug("申請ファイル登録 終了");
		}
	}
	
	/**
	 * 申請ファイル削除
	 * 
	 * @param fileId ファイルID
	 * @return 
	 */
	public int deleteApplicationFile(Integer fileId) {
		LOGGER.debug("申請ファイル削除 開始");
		try {
			String sql = "" + //
					"DELETE FROM " + //
					"  o_application_file " + //
					"WHERE " + //
					"  file_id=?"; //
			return jdbcTemplate.update(sql, fileId);
		} finally {
			LOGGER.debug("申請ファイル削除 終了");
		}
	}
	
	/**
	 * 申請ファイル削除(論理削除)
	 * 
	 * @param fileId ファイルID
	 * @return 
	 */
	public int updateDeleteFlag(Integer fileId) {
		LOGGER.debug("申請ファイル論理削除 開始");
		try {
			String sql = "" + //
					"UPDATE o_application_file " + //
					"SET delete_flag= '1' " + //
					"WHERE " + //
					"  file_id=?"; //
			return jdbcTemplate.update(sql, fileId);
		} finally {
			LOGGER.debug("申請ファイル論理削除 終了");
		}
	}
}
