package developmentpermission.repository.jdbc;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import developmentpermission.entity.DepartmentAnswer;
import developmentpermission.form.AnswerForm;
import developmentpermission.form.DepartmentAnswerForm;

/**
 * 部署回答情報JDBC
 */
@Component
@Configuration
@ComponentScan
@EnableTransactionManagement
@Transactional
public class DepartmentAnswerJdbc extends AbstractJdbc {

	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(DepartmentAnswerJdbc.class);

	/**
	 * O_部署回答登録
	 * 
	 * @param answer
	 * @return
	 */
	public Integer insert(int applicationId, String departmentId, String registerStatus) {
		LOGGER.debug("部署回答登録 開始");
		try {

			String sql = "" + //
					"INSERT INTO o_department_answer( " + //
					"  department_answer_id, " + //
					"  application_id, " + //
					"  application_step_id, " + //
					"  department_id, " + //
					"  complete_flag, " + //
					"  notified_flag, " + //
					"  register_datetime, " + //
					"  update_datetime, " + //
					"  register_status " + //
					") " + //
					"VALUES ( " + //
					"  nextval('seq_department_answer'), " + //
					"  ?, " + // applicationId
					"  2, " + // applicationStepId
					"  ?, " + // departmentId
					"  '0', " + // completeFlag
					"  '0', " + // notifiedFlag
					"  CURRENT_TIMESTAMP, " + //
					"  CURRENT_TIMESTAMP, " + //
					"  ? " + // registerStatus
					")";
			jdbcTemplate.update(sql, //
					applicationId, //
					departmentId, registerStatus);
			return jdbcTemplate.queryForObject("SELECT lastval()", Integer.class);
		} finally {
			LOGGER.debug("部署回答登録 終了");
		}
	}

	/**
	 * 部署回答更新
	 * 
	 * @param departmentAnswerForm      部署回答フォーム
	 * @param governmentConfirmDateTime 行政確定日付
	 * 
	 * @return 更新件数
	 */
	public int update(DepartmentAnswerForm departmentAnswerForm, LocalDateTime governmentConfirmDateTime) {
		LOGGER.debug("部署回答更新 開始");
		try {

			String sql = "" + //
					"UPDATE " + //
					"  o_department_answer " + //
					"SET " + //
					"  complete_flag=?, " + //
					"  government_confirm_status=?, " + //
					"  government_confirm_datetime=?, " + //
					"  government_confirm_comment=?, " + //
					"  update_datetime=CURRENT_TIMESTAMP " + //
					"WHERE " + //
					"  department_answer_id=?" + //
					"  AND update_datetime=?";
			return jdbcTemplate.update(sql, //
					departmentAnswerForm.getCompleteFlag()?"1":"0",//完了フラグ：1完了、0未完
					departmentAnswerForm.getGovernmentConfirmStatus(), //
					governmentConfirmDateTime, //
					departmentAnswerForm.getGovernmentConfirmComment(), //
					departmentAnswerForm.getDepartmentAnswerId(), //
					departmentAnswerForm.getUpdateDatetime());
		} finally {
			LOGGER.debug("部署回答更新 終了");
		}
	}

	/**
	 * 回答内容を通知テキストに設定
	 * 
	 * @param answerForm パラメータ
	 * @return 更新件数
	 */
	public int copyNotifyText(DepartmentAnswer departmentAnswer) {
		LOGGER.debug("回答通知設定 開始");
		try {
			String sql = "" + //
					"UPDATE " + //
					"  o_department_answer " + //
					"SET " + //
					"  notified_text=?, " + //
					"  notified_flag='1', " + // 通知フラグに1を設定
					"  update_datetime=CURRENT_TIMESTAMP " + //
					"WHERE " + //
					"  department_answer_id=?" + //
					"  AND update_datetime=?";
			return jdbcTemplate.update(sql, //
					departmentAnswer.getGovernmentConfirmComment(), //
					departmentAnswer.getDepartmentAnswerId(), //
					departmentAnswer.getUpdateDatetime());
		} finally {
			LOGGER.debug("回答通知設定 終了");
		}
	}

	/**
	 * 回答内容を通知テキストに設定
	 * 
	 * @param answerForm パラメータ
	 * @return 更新件数
	 */
	public int resetCompleteFlag(DepartmentAnswer departmentAnswer) {
		LOGGER.debug("回答通知設定 開始");
		try {
			String sql = "" + //
					"UPDATE " + //
					"  o_department_answer " + //
					"SET " + //
					"  complete_flag = '0', " + // 完了フラグを0にリセット
					"  notified_flag = '0', " + // 通知フラグを0にリセット、未通知になると、事業者は行政確定内容が閲覧できない
					"  update_datetime=CURRENT_TIMESTAMP " + //
					"WHERE " + //
					"  department_answer_id=?" + //
					"  AND update_datetime=?";
			return jdbcTemplate.update(sql, //
					departmentAnswer.getDepartmentAnswerId(), //
					departmentAnswer.getUpdateDatetime());
		} finally {
			LOGGER.debug("回答通知設定 終了");
		}
	}

	/**
	 * 登録ステータス更新
	 * 
	 * @param answerForm パラメータ
	 * @return 更新件数
	 */
	public int updateRegisterStatus(DepartmentAnswer departmentAnswer) {
		LOGGER.debug("登録ステータス更新 開始");
		try {
			String sql = "" + //
					"UPDATE " + //
					"  o_department_answer " + //
					"SET " + //
					"  register_status = '1', " + //
					"  update_datetime=CURRENT_TIMESTAMP " + //
					"WHERE " + //
					"  department_answer_id=?" + //
					"  AND update_datetime=?";
			return jdbcTemplate.update(sql, //
					departmentAnswer.getDepartmentAnswerId(), //
					departmentAnswer.getUpdateDatetime());
		} finally {
			LOGGER.debug("登録ステータス更新 終了");
		}
	}

	/**
	 * 回答削除
	 * 
	 * @param departmentAnswer パラメータ
	 * @return 更新件数
	 */
	public int deleteDepartmentAnswer(DepartmentAnswer departmentAnswer) {
		LOGGER.debug("回答削除 開始");
		try {
			String sql = "" + //
					"DELETE FROM " + //
					"  o_department_answer " + //
					"WHERE " + //
					"  department_answer_id=?" + //
					"  AND update_datetime=?";
			return jdbcTemplate.update(sql, //
					departmentAnswer.getDepartmentAnswerId(), //
					departmentAnswer.getUpdateDatetime());
		} finally {
			LOGGER.debug("回答削除 終了");
		}
	}

	/**
	 * 部署回答の行政確定通知許可フラグ更新（事前協議のみ）
	 * 
	 * @param answer パラメータ
	 * @return 更新件数
	 */
	public int updateGovernmentConfirmPermissionFlag(DepartmentAnswerForm departmentAnswerForm) {
		LOGGER.debug("行政確定登録通知許可フラグ更新 開始");
		try {
			String sql = "" + //
					"UPDATE  " + //
					"  o_department_answer " + //
					"SET " + //
					"  government_confirm_permission_flag = '1', " + // 1:許可済み
					"  update_datetime= CURRENT_TIMESTAMP " + //
					"WHERE " + //
					"  department_answer_id=?" + //
					"  AND update_datetime=?";
			return jdbcTemplate.update(sql, //
					departmentAnswerForm.getDepartmentAnswerId(), //
					departmentAnswerForm.getUpdateDatetime());
		} finally {
			LOGGER.debug("行政確定登録通知許可フラグ更新 終了");
		}
	}
	
	/**
	 * 部署回答の行政確定登録内容クリア（事前協議のみ）
	 * 
	 * @param answer パラメータ
	 * @return 更新件数
	 */
	public int clearGovernmentConfirmInfo(DepartmentAnswer departmentAnswer) {
		LOGGER.debug("行政確定登録内容クリア更新 開始");
		try {
			String sql = "" + //
					"UPDATE  " + //
					"  o_department_answer " + //
					"SET " + //
					"  government_confirm_status = NULL , " + //
					"  government_confirm_datetime = NULL , " + //
					"  government_confirm_comment = NULL , " + //
					"  complete_flag = '0', " + // 0:未完了
					"  notified_flag = '0', " + // 0:未通知
					"  government_confirm_permission_flag = '0', " + // 0:未許可
					"  update_datetime= CURRENT_TIMESTAMP " + //
					"WHERE " + //
					"  department_answer_id=?" + //
					"  AND update_datetime=?";
			return jdbcTemplate.update(sql, //
					departmentAnswer.getDepartmentAnswerId(), //
					departmentAnswer.getUpdateDatetime());
		} finally {
			LOGGER.debug("行政確定登録内容クリア更新 終了");
		}
	}
}
