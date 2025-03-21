package developmentpermission.repository.jdbc;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import developmentpermission.entity.AcceptingAnswer;
import developmentpermission.entity.Answer;
import developmentpermission.entity.AnswerHistory;
import developmentpermission.form.AnswerForm;

/**
 * 受付回答情報JDBC
 */
@Component
@Configuration
@ComponentScan
@EnableTransactionManagement
@Transactional
public class AcceptingAnswerJdbc extends AbstractJdbc {

	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(AcceptingAnswerJdbc.class);

	/**
	 * 受付回答登録
	 * 
	 * @param acceptingnswer 受付回答
	 * @return
	 */
	public Integer insert(AcceptingAnswer acceptingnswer) {
		LOGGER.debug("受付回答登録 開始");
		try {
			String sql = "" + //
					"INSERT INTO o_accepting_answer ( " + //
					"  accepting_answer_id, " + //
					"  application_id, " + //
					"  application_step_id, " + //
					"  version_infomation, " + //
					"  judgement_id, " + //
					"  department_id, " + //
					"  judgement_result, " + //
					"  judgement_result_index, " + //
					"  answer_content, " + //
					"  register_datetime, " + //
					"  update_datetime, " + //
					"  answer_data_type, " + //
					"  register_status, " + //
					"  deadline_datetime, " + //
					"  answer_id " + //
					") " + //
					"VALUES ( " + //
					"  nextval('seq_accepting_answer'), " + //
					"  ?, " + // applicationId
					"  ?, " + // applicationStepId
					"  ?, " + // versionInfomation
					"  ?, " + // judgementId
					"  ?, " + // departmentId
					"  ?, " + // judgementResult
					"  ?, " + // judgementResultIndex
					"  ?, " + // answerContent
					"  CURRENT_TIMESTAMP, " + //
					"  CURRENT_TIMESTAMP, " + //
					"  ?, " + // answerDataType
					"  '0', " + // registerStatus
					"  ?, " + // deadlineDatetime
					"  ? " + // answerId
					")";
			jdbcTemplate.update(sql, //
					acceptingnswer.getApplicationId(), //
					acceptingnswer.getApplicationStepId(), //
					acceptingnswer.getVersionInfomation(), //
					acceptingnswer.getJudgementId(), //
					acceptingnswer.getDepartmentId(), //
					acceptingnswer.getJudgementResult(), //
					acceptingnswer.getJudgementResultIndex(), //
					acceptingnswer.getAnswerContent(), //
					acceptingnswer.getAnswerDataType(), //
					acceptingnswer.getDeadlineDatetime(), //
					acceptingnswer.getAnswerId());
			return jdbcTemplate.queryForObject("SELECT lastval()", Integer.class);
		} finally {
			LOGGER.debug("受付回答登録 終了");
		}
	}

	/**
	 * 受付回答更新（登録ステータス）
	 * 
	 * @param acceptingAnswer パラメータ
	 * @return 更新件数
	 */
	public int updateRegisterStatus(AcceptingAnswer acceptingAnswer) {
		LOGGER.debug("受付回答更新 開始");
		try {
			String sql = "" + //
					"UPDATE  " + //
					"  o_accepting_answer " + //
					"SET " + //
					"  register_status = '1' " + // 登録ステータス
					"WHERE " + //
					"  accepting_answer_id=?" + //
					"  AND update_datetime=?";
			return jdbcTemplate.update(sql, //
					acceptingAnswer.getAcceptingAnswerId(), //
					acceptingAnswer.getUpdateDatetime());
		} finally {
			LOGGER.debug("受付回答更新 終了");
		}
	}

	/**
	 * 受付回答削除（物理削除）
	 * 
	 * @param acceptingAnswer パラメータ
	 * @return 更新件数
	 */
	public int deleteAcceptingAnswer(AcceptingAnswer acceptingAnswer) {
		LOGGER.debug("受付回答削除 開始");
		try {
			// 再申請エラーデータ消去のため、物理削除
			String sql = "" + //
					"DELETE FROM " + //
					"  o_accepting_answer " + //
					"WHERE " + //
					"  accepting_answer_id=?" + //
					"  AND update_datetime=?";
			return jdbcTemplate.update(sql, //
					acceptingAnswer.getAcceptingAnswerId(), //
					acceptingAnswer.getUpdateDatetime());
		} finally {
			LOGGER.debug("受付回答削除 終了");
		}
	}

}
