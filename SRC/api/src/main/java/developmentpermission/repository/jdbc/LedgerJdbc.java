package developmentpermission.repository.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import developmentpermission.entity.Ledger;

/**
 * 帳票JDBC
 */
@Component
@Configuration
@ComponentScan
@EnableTransactionManagement
@Transactional
public class LedgerJdbc extends AbstractJdbc {

	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(LedgerJdbc.class);

	/**
	 * 受領日時更新
	 * 
	 * @param fileId ファイルID
	 * @return
	 */
	public int updateReceiptDatetime(Integer fileId) {
		LOGGER.debug(" 受領日時更新 開始");
		try {
			String sql = "" + //
					"UPDATE o_ledger " + //
					"SET " + //
					"  receipt_datetime = CURRENT_TIMESTAMP " + //
					"WHERE " + //
					"  file_id=?  ";

			return jdbcTemplate.update(sql, fileId);
		} finally {
			LOGGER.debug(" 受領日時更新 終了");
		}
	}
	/**
	 * ファイルパス更新
	 * 
	 * @param fileId ファイルID
	 * @param filePath ファイルパス
	 * @return
	 */
	public int updateFilePath(Integer fileId,String filePath) {
		LOGGER.debug(" ファイルパス更新 開始");
		try {
			String sql = "" + //
					"UPDATE o_ledger " + //
					"SET " + //
					"  file_path = ? " + //
					"WHERE " + //
					"  file_id = ?  ";

			return jdbcTemplate.update(sql, filePath, //
					fileId); //
		} finally {
			LOGGER.debug(" ファイルパス更新 終了");
		}
	}
	/**
	 * 通知済み更新
	 * 
	 * @param fileId ファイルID
	 * @param filePath ファイルパス
	 * @return
	 */
	public int updateNotify(Integer fileId) {
		LOGGER.debug(" 通知済み更新 開始");
		try {
			String sql = "" + //
					"UPDATE o_ledger " + //
					"SET " + //
					"  notify_file_path = file_path " + //
					" ,notify_flag = '1' " + //
					"WHERE " + //
					"  file_id = ?  ";

			return jdbcTemplate.update(sql, fileId); //
		} finally {
			LOGGER.debug(" 通知済み更新 終了");
		}
	}
	
	/**
	 * O_帳票登録
	 * @param ledger O_帳票Entity
	 * @return
	 */
	public Integer insert(Ledger ledger) {
		LOGGER.debug("O_帳票登録 開始");
		try {
			String sql = "" + //
					"INSERT INTO o_ledger( " + //
					" file_id " + //
					", application_id " + //
					", application_step_id " + //
					", ledger_id " + //
					", file_name " + //
					", file_path " + //
					", register_datetime " + //
					") " + //
					"VALUES ( " + //
					"    nextval('seq_ledger') " + //
					"    , ? " + //
					"    , ? " + //
					"    , ? " + //
					"    , ? " + //
					"    , ? " + //
					"    , CURRENT_TIMESTAMP " + //
					")";
			jdbcTemplate.update(sql, //
					ledger.getApplicationId(), //
					ledger.getApplicationStepId(), //
					ledger.getLedgerId(), //
					ledger.getFileName(), //
					ledger.getFilePath());
			return jdbcTemplate.queryForObject("SELECT lastval()", Integer.class);
		} finally {
			LOGGER.debug("O_帳票登録 終了");
		}
	}

}
