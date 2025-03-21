package developmentpermission.repository.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import developmentpermission.entity.ApplicantInformationAdd;

/**
 * 申請追加情報JDBC
 */
@Component
@Configuration
@ComponentScan
@EnableTransactionManagement
@Transactional
public class ApplicantAddJdbc extends AbstractJdbc {

	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicantAddJdbc.class);

	/**
	 * O_申請追加情報登録
	 * 
	 * @return 申請ID
	 */
	public int insert(ApplicantInformationAdd applicantInformationAdd) {
		LOGGER.debug("O_申請版情報登録 開始");
		try {
			String sql = "" + //
					"INSERT INTO  o_applicant_information_add( " + //
					"  applicant_id, " + //
					"  application_id, " + //
					"  application_step_id, " + //
					"  applicant_information_item_id, " + //
					" item_value, " + //
					" version_information " + //
					") " + //
					"VALUES (nextval('seq_applicant_information_add'), ?, ?, ?, ?, ?)";

			return jdbcTemplate.update(sql, applicantInformationAdd.getApplicationId(),
					applicantInformationAdd.getApplicationStepId(),
					applicantInformationAdd.getApplicantInformationItemId(), applicantInformationAdd.getItemValue(),
					applicantInformationAdd.getVersionInformation());
		} finally {
			LOGGER.debug("O_申請情報登録 終了");
		}
	}
	
	/**
	 * 申請追加情報削除
	 * @param applicantId 申請追加項目ID
	 * 
	 * @return 件数
	 */
	public int deleteApplicantAddInfo(Integer applicantId) {
		LOGGER.debug("申請追加情報削除 開始");
		try {
			String sql = "" + //
					"DELETE FROM " + //
					"  o_applicant_information_add " + //
					"WHERE " + //
					"  applicant_id=?"; //
			return jdbcTemplate.update(sql, applicantId);
		} finally {
			LOGGER.debug("申請追加情報削除 終了");
		}
	}
}
