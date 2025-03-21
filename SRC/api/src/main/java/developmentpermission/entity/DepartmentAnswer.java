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
 * O_部署_回答Entityクラス
 */
@Entity
@Data
@Table(name = "o_department_answer")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DepartmentAnswer implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** 部署回答ID */
	@Id
	@Column(name = "department_answer_id")
	private Integer departmentAnswerId;

	/** 申請ID */
	@Column(name = "application_id")
	private Integer applicationId;

	/** 申請段階ID */
	@Column(name = "application_step_id")
	private Integer applicationStepId;

	/** 部署ID */
	@Column(name = "department_id")
	private String departmentId;

	/** 行政確定ステータス(0:合意 1:取下 2:却下) */
	@Column(name = "government_confirm_status", columnDefinition = "char(1)")
	private String governmentConfirmStatus;

	/** 行政確定日時(事前協議のみ) */
	@Column(name = "government_confirm_datetime")
	private LocalDateTime governmentConfirmDatetime;

	/** 行政確定コメント(事前協議のみ) */
	@Column(name = "government_confirm_comment")
	private String governmentConfirmComment;

	/** 通知テキスト */
	@Column(name = "notified_text")
	private String notifiedText;

	/** 完了フラグ */
	@Column(name = "complete_flag", columnDefinition = "char(1)")
	private Boolean completeFlag;

	/** 通知フラグ */
	@Column(name = "notified_flag", columnDefinition = "char(1)")
	private Boolean notifiedFlag;

	/** 登録日時 */
	@Column(name = "register_datetime")
	private LocalDateTime registerDatetime;

	/** 更新日時 */
	@Column(name = "update_datetime")
	private LocalDateTime updateDatetime;

	/** 登録ステータス(0: 仮申請中 1: 申請済み) */
	@Column(name = "register_status", columnDefinition = "char(1)")
	private String registerStatus;

	/** 削除未通知フラグ(1:削除済み・未通知) */
	@Column(name = "delete_unnotified_flag", columnDefinition = "char(1)")
	private Boolean deleteUnnotifiedFlag;

	/** 行政確定通知許可フラグ(1=許可済み 0=未許可) */
	@Column(name = "government_confirm_permission_flag", columnDefinition = "char(1)")
	private Boolean governmentConfirmPermissionFlag;

}
