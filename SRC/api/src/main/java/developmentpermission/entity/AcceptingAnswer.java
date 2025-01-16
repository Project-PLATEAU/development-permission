package developmentpermission.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

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
 * O_受付回答Entityクラス
 */
@Entity
@Data
@Table(name = "o_accepting_answer")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AcceptingAnswer implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** 受付回答ID */
	@Id
	@Column(name = "accepting_answer_id")
	private Integer acceptingAnswerId;

	/** 申請ID */
	@Column(name = "application_id")
	private Integer applicationId;

	/** 申請段階ID */
	@Column(name = "application_step_id")
	private Integer applicationStepId;

	/** 版情報 */
	@Column(name = "version_infomation")
	private Integer versionInfomation;

	/** 判定項目ID */
	@Column(name = "judgement_id")
	private String judgementId;

	/** 部署ID */
	@Column(name = "department_id")
	private String departmentId;

	/** 判定結果 */
	@Column(name = "judgement_result")
	private String judgementResult;

	/** 判定項目の複数行の判定結果のインデックス */
	@Column(name = "judgement_result_index")
	private Integer judgementResultIndex;

	/** 回答内容 */
	@Column(name = "answer_content")
	private String answerContent;

	/** 登録日時 */
	@Column(name = "register_datetime")
	private LocalDateTime registerDatetime;

	/** 更新日時 */
	@Column(name = "update_datetime")
	private LocalDateTime updateDatetime;

	/** データ種類(0:登録、1:更新、2：追加、3:行政で追加、4:一律追加、5:削除済み、6：引継、7:削除済み（行政）) */
	@Column(name = "answer_data_type", columnDefinition = "char(1)")
	private String answerDataType;

	/** 登録ステータス(0: 仮申請中 1: 申請済み) */
	@Column(name = "register_status", columnDefinition = "char(1)")
	private String registerStatus;

	/** 回答期限日時 */
	@Column(name = "deadline_datetime")
	private LocalDateTime deadlineDatetime;

	/** 回答ID */
	@Column(name = "answer_id")
	private Integer answerId;
}
