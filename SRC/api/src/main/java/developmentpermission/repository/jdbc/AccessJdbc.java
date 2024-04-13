package developmentpermission.repository.jdbc;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * アクセスJDBCクラス
 *
 */
@Component
@Configuration
@ComponentScan
@EnableTransactionManagement
@Transactional
public class AccessJdbc extends AbstractJdbc {

	/**
	 * アクセスID採番
	 * 
	 * @return seq アクセスID
	 */
	public String getAccessId() {
		int seq = this.jdbcTemplate.queryForObject("SELECT nextval('seq_access_id')", Integer.class);
		return String.valueOf(seq);
	}
}
