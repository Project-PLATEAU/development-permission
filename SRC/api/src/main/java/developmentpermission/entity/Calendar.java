package developmentpermission.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * M_カレンダEntiryクラス
 */
@Entity
@Data
@Table(name = "m_calendar")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Calendar implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** カレンダ日付 */
	@Id
	@Column(name = "cal_date")
	private Date calDate;

	/** 曜日 */
	@Column(name = "week_day")
	private Integer weekDay ;

	/** 営業日フラグ */
	@Column(name = "biz_day_flag", columnDefinition = "char(1)")
	private  Boolean bizDayFlag;

	/** 備考 */
	@Column(name = "comment")
	private String comment;

}
