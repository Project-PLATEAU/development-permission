package developmentpermission.repository.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import developmentpermission.form.InquiryFileForm;

/**
 * O_問合せファイルJDBC 
 */
@Component
@Configuration
@ComponentScan
@EnableTransactionManagement
@Transactional
public class InquiryFileJdbc extends AbstractJdbc {

	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(InquiryFileJdbc.class);

	/**
	 * 登録
	 * 
	 * @param form パラメータ
	 * @return 問合せファイルID
	 */
	public Integer insert(InquiryFileForm form) {
		LOGGER.debug("問合せファイル登録 開始");
		try {
			String sql = "" + //
					"INSERT INTO o_inquiry_file " + //
					"(inquiry_file_id, message_id, file_name, register_datetime ) " + //
					"VALUES (nextval('seq_inquiry_file'), ?, ?, CURRENT_TIMESTAMP )";
			jdbcTemplate.update(sql, //
					form.getMessageId(), //
					form.getFileName());
			return jdbcTemplate.queryForObject("SELECT lastval()", Integer.class);
		} finally {
			LOGGER.debug("問合せファイル登録 終了");
		}
	}

	/**
	 * ファイルパス更新
	 * 
	 * @param inquiryFileId 問合せファイルID
	 * @param filePath     ファイルパス
	 * @return 更新件数
	 */
	public int updateFilePath(int inquiryFileId, String filePath) {
		LOGGER.debug("問合せファイルパス更新 開始");
		try {
			String sql = "" + //
					"UPDATE o_inquiry_file " + //
					"SET file_path=? " + //
					"WHERE inquiry_file_id=?";
			return jdbcTemplate.update(sql, //
					filePath, //
					inquiryFileId);
		} finally {
			LOGGER.debug("問合せファイルパス更新 終了");
		}
	}
}
