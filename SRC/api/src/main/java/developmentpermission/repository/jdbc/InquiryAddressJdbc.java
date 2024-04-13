package developmentpermission.repository.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import developmentpermission.entity.InquiryAddress;

/**
 * O_問合せ宛先JDBC
 */
@Component
@Configuration
@ComponentScan
@EnableTransactionManagement
@Transactional
public class InquiryAddressJdbc extends AbstractJdbc {

	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(InquiryAddressJdbc.class);

	/**
	 * O_問合せ宛先登録
	 * 
	 * @param inquiryAddress 問合せ宛先情報
	 * @return
	 */
	public Integer insert(InquiryAddress inquiryAddress) {
		LOGGER.debug("O_問合せ宛先登録 開始");
		try {
			String sql = "" + //
					"INSERT INTO o_inquiry_address ( " + //
					"  inquiry_address_id, " + //
					"  message_id, " + //
					"  department_id, " + //
					"  read_flag, " + //
					"  answer_complete_flag " + //
					") " + //
					"VALUES ( " + //
					"  nextval('seq_inquiry_address'), " + //
					"  ?, " + // message_id
					"  ?, " + // department_id
					"  0, " + // read_flag：未読
					"  0 " + // answer_complete_flag：未回答
					")";
			jdbcTemplate.update(sql, //
					inquiryAddress.getMessageId(),
					inquiryAddress.getDepartmentId());
			return jdbcTemplate.queryForObject("SELECT lastval()", Integer.class);
		} finally {
			LOGGER.debug("O_問合せ宛先登録 終了");
		}
	}

	/**
	 * 既読に更新
	 * 
	 * @param inquiryAddressId  問合せ宛先ID
	 * @return
	 */
	public int updateReadFlag(int inquiryAddressId) {
		LOGGER.debug("O_問合せ宛先：既読に更新 開始");
		try {
			String sql = "" + //
					"UPDATE o_inquiry_address " + //
					"SET read_flag='1' " + //
					"WHERE inquiry_address_id=?";
			return jdbcTemplate.update(sql, //
					inquiryAddressId);
		} finally {
			LOGGER.debug("O_問合せ宛先：既読に更新 終了");
		}
	}
	
	/**
	 * 回答済みに更新
	 * 
	 * @param inquiryAddressId  問合せ宛先ID
	 * @return
	 */
	public int updateAnswerCompleteFlag(int inquiryAddressId) {
		LOGGER.debug("O_問合せ宛先：回答済みに更新 開始");
		try {
			String sql = "" + //
					"UPDATE o_inquiry_address " + //
					"SET answer_complete_flag='1' " + //
					"WHERE inquiry_address_id=?";
			return jdbcTemplate.update(sql, //
					inquiryAddressId);
		} finally {
			LOGGER.debug("O_問合せ宛先：回答済みに更新 終了");
		}
	}
}
