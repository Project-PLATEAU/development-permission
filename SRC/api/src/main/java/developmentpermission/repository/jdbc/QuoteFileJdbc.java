package developmentpermission.repository.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import developmentpermission.form.AnswerFileForm;
import developmentpermission.form.QuoteFileForm;

/**
 *　回答ファイル（引用）JDBC
 */
@Component
@Configuration
@ComponentScan
@EnableTransactionManagement
@Transactional
public class QuoteFileJdbc extends AbstractJdbc {
	
	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(QuoteFileJdbc.class);
	
	/**
	 * 登録
	 * 
	 * @param form パラメータ
	 * @return 回答ファイルID
	 */
	public Integer insert(QuoteFileForm form) {
		LOGGER.debug("回答ファイル（引用）登録 開始");
		try {
			String sql = "" + //
					"INSERT INTO o_answer_file " + //
					"(answer_file_id, answer_id, answer_file_name, delete_unnotified_flag, delete_flag) " + //
					"VALUES (nextval('seq_answer_file'), ?, ?, '0', '0')";
			jdbcTemplate.update(sql, //
					form.getAnswerId(), //
					form.getAnswerFileName());
			return jdbcTemplate.queryForObject("SELECT lastval()", Integer.class);
		} finally {
			LOGGER.debug("回答ファイル（引用）登録 終了");
		}
	}
}
