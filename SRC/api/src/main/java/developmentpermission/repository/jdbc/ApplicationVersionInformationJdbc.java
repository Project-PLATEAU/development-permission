package developmentpermission.repository.jdbc;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

/**
 * 申請版情報JDBC
 */
@Component
@Configuration
@ComponentScan
@EnableTransactionManagement
@Transactional
public class ApplicationVersionInformationJdbc extends AbstractJdbc {

	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationVersionInformationJdbc.class);

	/**
	 * O_申請版情報登録(新規登録の場合、版情報が「1」で固定)
	 * 
	 * @return 申請ID
	 */
	public int insert(int applicationId, int applicationStepId, String registerStatus) {
		LOGGER.debug("O_申請版情報登録 開始");
		try {
			String sql = "" + //
					"INSERT INTO o_application_version_information " + //
					"(application_id, application_step_id, version_information, register_datetime, update_datetime, register_status, accepting_flag, accept_version_information ) " + //
					"VALUES (?, ?, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, ?";
			if (applicationStepId == 2) {
				// 事前協議の場合、受付フラグ(0:未確認)と受付版情報(0)を登録
				sql += ",'0' , '0'";
			}else {
				// 事前協議以外の場合、受付フラグ(1:受付)と受付版情報(1)を登録
				sql += ",'1' , '1'";
			}
			sql += ")";

			return jdbcTemplate.update(sql, applicationId, applicationStepId, registerStatus);
		} finally {
			LOGGER.debug("O_申請情報登録 終了");
		}
	}

	/**
	 * O_申請版情報更新(再申請の場合、版情報+1)
	 * 
	 * @param applicationId     申請ID
	 * @param applicationStepId 申請段階ID
	 * @param updateDatetime    更新日時
	 * 
	 * @return 更新件数
	 */
	public int update(int applicationId, int applicationStepId, String registerStatus, LocalDateTime updateDatetime) {
		LOGGER.debug("O_申請版情報更新 開始");
		try {
			String sql = "" + //
					"UPDATE o_application_version_information " + //
					"SET " + //
					"  version_information = COALESCE(version_information, 1) + 1," + //
					"  register_status = ? ," + //
					"  update_datetime = CURRENT_TIMESTAMP "; //
			if (applicationStepId == 2) {
				// 事前協議の場合、受付フラグ(0:未確認)に更新
				sql += "  , " + //
						"  accepting_flag = '0' "; //
			}else {
				// 事前協議以外の場合、受付フラグがそのまま更新しないで、受付版情報が版情報と同じで更新
				sql += "  , " + //
				"  accept_version_information = COALESCE(accept_version_information, 1) + 1 "; //
			}
			sql += "WHERE " + //
					"  application_id=? " + //
					"  AND application_step_id=? " + //
					"  AND update_datetime=? ";

			return jdbcTemplate.update(sql, registerStatus, applicationId, applicationStepId, updateDatetime);
		} finally {
			LOGGER.debug("O_申請情報更新 終了");
		}
	}

	/**
	 * O_申請版情報更新(再申請の場合、登録ステータスの更新)
	 * 
	 * @param applicationId     申請ID
	 * @param applicationStepId 申請段階ID
	 * @param updateDatetime    更新日時
	 * 
	 * @return 更新件数
	 */
	public int updateRegisterStatus(int applicationId, int applicationStepId, LocalDateTime updateDatetime) {
		LOGGER.debug("O_申請版情報登録 開始");
		try {
			String sql = "" + //
					"UPDATE o_application_version_information " + //
					"SET " + //
					"  register_status='1' " + //
					"WHERE " + //
					"  application_id=? " + //
					"  AND application_step_id=? " + //
					"  AND update_datetime=? ";

			return jdbcTemplate.update(sql, applicationId, applicationStepId, updateDatetime);
		} finally {
			LOGGER.debug("O_申請情報登録 終了");
		}
	}

	/**
	 * O_申請版情報削除
	 * 
	 * @param applicationId     申請ID
	 * @param applicationStepId 申請段階ID
	 * @param updateDatetime    更新日時
	 * 
	 * @return 更新件数
	 */
	public int delete(int applicationId, int applicationStepId, LocalDateTime updateDatetime) {
		LOGGER.debug("申請版情報削除 開始");
		try {
			String sql = "" + //
					"DELETE FROM " + //
					"  o_application_version_information " + //
					"WHERE " + //
					"  application_id=?" + //
					"  AND application_step_id=? " + //
					"  AND update_datetime=? ";

			return jdbcTemplate.update(sql, //
					applicationId, //
					applicationStepId, updateDatetime);
		} finally {
			LOGGER.debug("申請版情報削除 終了");
		}
	}

	/**
	 * O_申請版情報更新(仮申請の消去の場合、版情報-1)
	 * 
	 * @param applicationId     申請ID
	 * @param applicationStepId 申請段階ID
	 * @param updateDatetime    更新日時
	 * 
	 * @return 更新件数
	 */
	public int resetVersion(int applicationId, int applicationStepId, LocalDateTime updateDatetime) {
		LOGGER.debug("O_申請版情報更新 開始");
		try {
			String sql = "" + //
					"UPDATE o_application_version_information " + //
					"SET " + //
					"  version_information = COALESCE(version_information, 1) - 1," + //
					"  register_status = '1' ," + //
					"  update_datetime = CURRENT_TIMESTAMP ";
			if (applicationStepId != 2) { // 事前協議の場合、再申請登録APIを行うタイミングで、受付版情報を更新しないため、リセット時に、受付版情報のリセットをスキップ
				sql += " , " + //
						"  accept_version_information = COALESCE(accept_version_information, 1) - 1 ";
			}
			sql += "WHERE " + //
					"  application_id=? " + //
					"  AND application_step_id=? " + //
					"  AND update_datetime=? ";

			return jdbcTemplate.update(sql, applicationId, applicationStepId, updateDatetime);
		} finally {
			LOGGER.debug("O_申請情報更新 終了");
		}
	}

	/**
	 * O_申請版情報：差戻通知更新(事前協議のみ)
	 * 
	 * @param applicationId     申請ID
	 * @param applicationStepId 申請段階ID
	 * @param updateDatetime    更新日時
	 * 
	 * @return 更新件数
	 */
	public int updateForRemand(int applicationId, int applicationStepId, LocalDateTime updateDatetime) {
		LOGGER.debug("O_申請版情報の差戻通知更新 開始");
		try {
			String sql = "" + //
					"UPDATE o_application_version_information " + //
					"SET " + //
					"  accepting_flag = '2' ," + // 2：差戻
					"  update_datetime = CURRENT_TIMESTAMP " + //
					"WHERE " + //
					"  application_id=? " + //
					"  AND application_step_id=? " + //
					"  AND update_datetime=? ";

			return jdbcTemplate.update(sql, applicationId, applicationStepId, updateDatetime);
		} finally {
			LOGGER.debug("O_申請版情報の差戻通知更新 終了");
		}
	}

	/**
	 * O_申請版情報：受付通知更新(事前協議のみ)
	 * 
	 * @param applicationId     申請ID
	 * @param applicationStepId 申請段階ID
	 * @param updateDatetime    更新日時
	 * 
	 * @return 更新件数
	 */
	public int updateForAccept(int applicationId, int applicationStepId, LocalDateTime updateDatetime) {
		LOGGER.debug("O_申請版情報の受付通知更新 開始");
		try {
			String sql = "" + //
					"UPDATE o_application_version_information " + //
					"SET " + //
					"  accepting_flag = '1' ," + // 1：受付
					"  accept_version_information = version_information, " + //
					"  update_datetime = CURRENT_TIMESTAMP " + //
					"WHERE " + //
					"  application_id=? " + //
					"  AND application_step_id=? " + //
					"  AND update_datetime=? ";

			return jdbcTemplate.update(sql, applicationId, applicationStepId, updateDatetime);
		} finally {
			LOGGER.debug("O_申請情報の受付通知更新 終了");
		}
	}

	/**
	 * O_申請版情報：完了日時更新
	 * 
	 * @param applicationId     申請ID
	 * @param applicationStepId 申請段階ID
	 * 
	 * @return 更新件数
	 */
	public int updateCompleteDatetime(int applicationId, int applicationStepId) {
		LOGGER.debug("O_申請版情報の完了日時更新 開始");
		try {
			String sql = "" + //
					"UPDATE o_application_version_information " + //
					"SET " + //
					"  complete_datetime = CURRENT_TIMESTAMP ," + // 完了日時
					"  update_datetime = CURRENT_TIMESTAMP " + //　更新日時
					"WHERE " + //
					"  application_id=? " + //
					"  AND application_step_id=? ";

			return jdbcTemplate.update(sql, applicationId, applicationStepId);
		} finally {
			LOGGER.debug("O_申請情報の完了日時更新 終了");
		}
	}

}
