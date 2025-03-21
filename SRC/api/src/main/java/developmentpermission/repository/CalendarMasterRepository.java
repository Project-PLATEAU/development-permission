package developmentpermission.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import developmentpermission.entity.Calendar;

/**
 * M_カレンダRepositoryインタフェース
 */
@Transactional
@Repository
public interface CalendarMasterRepository extends JpaRepository<Calendar, Date> {

	/**
	 * Ｎ営業日後日付取得
	 * 
	 * @param baseDate 基準日
	 * @param nbizDay Ｎ営業日
	 * @return Ｎ営業日後日付
	 */
	@Query(value = "SELECT MAX(t1.cal_date) as cal_date FROM ( SELECT s1.cal_date FROM m_calendar s1 WHERE s1.cal_date > to_date(:baseDate,'YYYY/MM/DD') AND s1.biz_day_flag = '1' ORDER BY s1.cal_date LIMIT :nbizDay)t1", nativeQuery = true)
	Date getNBizDay(@Param("baseDate") Date baseDate,@Param("nbizDay") Integer nbizDay);
	
	/**
	 * 指定日のカレンダー情報を取得
	 * 
	 * @param calDate YYYY-MM-DD形式
	 * @return Calendar
	 */
	@Query(value = "SELECT cal_date,week_day,biz_day_flag,comment FROM m_calendar WHERE cal_date = TO_DATE(:calDate, 'YYYY-MM-DD')", nativeQuery = true)
	Calendar getCalender(@Param("calDate") String calDate);

}
