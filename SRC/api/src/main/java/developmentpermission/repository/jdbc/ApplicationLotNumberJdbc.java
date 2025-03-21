package developmentpermission.repository.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import developmentpermission.form.LotNumberForm;

/**
 * 申請地番JDBC
 */
@Component
@Configuration
@ComponentScan
@EnableTransactionManagement
@Transactional
public class ApplicationLotNumberJdbc extends AbstractJdbc {

	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationLotNumberJdbc.class);


	/**
	 * F_申請地番登録
	 * @param wkt 地番図形WKT
	 * @param lotNumberText 地番名称文字列
	 * @param applicationId 申請ID
	 * @param epsg 座標系
	 * @return
	 */
	public int insert(String wkt, String lotNumberText, int applicationId, int epsg) {
		LOGGER.debug("申請地番登録 開始");
		try {
			String sql = "" + //
					"INSERT " + //
					"INTO f_application_lot_number(application_id, lot_numbers, geom) " + //
					"VALUES ( " + //
					"    ? " + //
					"    , ?" + //
					"    , ST_Multi(ST_GeomFromText(?, ?))" + //
					")";
			return jdbcTemplate.update(sql, applicationId, lotNumberText, wkt, epsg);
		} finally {
			LOGGER.debug("申請地番登録 終了");
		}
	}

	/**
	 * F_申請地番削除
	 * @param applicationId 申請ID
	 * @return
	 */
	public int delete(int applicationId) {
		LOGGER.debug("申請地番削除 開始");
		try {
			String sql = "" + //
					"DELETE FROM " + //
					"  f_application_lot_number " + //
					"WHERE " + //
					"  application_id=?";
			return jdbcTemplate.update(sql, applicationId);
		} finally {
			LOGGER.debug("申請地番削除 終了");
		}
	}
}

