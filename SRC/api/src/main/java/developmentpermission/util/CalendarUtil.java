package developmentpermission.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import developmentpermission.entity.Calendar;
import developmentpermission.repository.CalendarMasterRepository;

/**
 * カレンダユーティリティクラス
 */
@Component
public class CalendarUtil {

	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(CalendarUtil.class);

	/** M_カレンダリポジトリインスタンス */
	@Autowired
	private CalendarMasterRepository calendarMasterRepository;
	
	/**
	 * 回答期限日時算出
	 * @param applicationAcceptDate 申請受付日
	 * @param answerDays 回答日数
	 * @return 回答期限日時
	 */
	public Date calcDeadlineDatetime(Date applicationAcceptDate,Integer answerDays) {
		LOGGER.debug("回答期限日時算出 開始");
		try {
			Date resultDate = calendarMasterRepository.getNBizDay(applicationAcceptDate,answerDays);		
			return resultDate;
		} finally {
			LOGGER.debug("回答期限日時算出 終了");
		}
	}
	
	/**
	 * カレンダー情報取得
	 * @param calDate YYYY-MM-DD形式
	 * @return Calendar
	 */
	public Calendar getCalender(String calDate) {
		LOGGER.debug("カレンダー情報取得 開始");
		try {
			return calendarMasterRepository.getCalender(calDate);
		} finally {
			LOGGER.debug("カレンダー情報取得 終了");
		}
	}
	
}
