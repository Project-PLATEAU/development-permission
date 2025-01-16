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
 * O_回答Entityクラス
 */
@Entity
@Data
@Table(name = "o_answer")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Answer implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** 回答ID */
	@Id
	@Column(name = "answer_id")
	private Integer answerId;

	/** 申請ID */
	@Column(name = "application_id")
	private Integer applicationId;

	/** 申請段階ID */
	@Column(name = "application_step_id")
	private Integer applicationStepId;

	/** 判定項目ID */
	@Column(name = "judgement_id")
	private String judgementId;

	/** 判定項目の複数行の判定結果のインデックス */
	@Column(name = "judgement_result_index")
	private Integer judgementResultIndex;

	/** 部署回答ID */
	@Column(name = "department_answer_id")
	private Integer departmentAnswerId;

	/** 部署ID */
	@Column(name = "department_id")
	private String departmentId;

	/** 判定結果 */
	@Column(name = "judgement_result")
	private String judgementResult;

	/** 回答内容 */
	@Column(name = "answer_content")
	private String answerContent;

	/** 通知テキスト */
	@Column(name = "notified_text")
	private String notifiedText;

	/** 登録日時 */
	@Column(name = "register_datetime")
	private LocalDateTime registerDatetime;

	/** 更新日時 */
	@Column(name = "update_datetime")
	private LocalDateTime updateDatetime;

	/** 完了フラグ */
	@Column(name = "complete_flag", columnDefinition = "char(1)")
	private Boolean completeFlag;

	/** 通知フラグ */
	@Column(name = "notified_flag", columnDefinition = "char(1)")
	private Boolean notifiedFlag;

	/** 回答変更フラグ */
	@Column(name = "answer_update_flag", columnDefinition = "char(1)")
	private Boolean answerUpdateFlag;

	/** 再申請フラグ */
	@Column(name = "re_application_flag", columnDefinition = "char(1)")
	private Boolean reApplicationFlag;

	/** 事業者再申請フラグ */
	@Column(name = "business_reapplication_flag", columnDefinition = "char(1)")
	private Boolean businessReApplicationFlag;

	/** 事前協議フラグ（事前相談のみ） */
	@Column(name = "discussion_flag", columnDefinition = "char(1)")
	private Boolean discussionFlag;

	/** 協議対象(事前協議のみ、選択協議に対する帳票マスタIDがカンマ区切りで保持) */
	@Column(name = "discussion_item")
	private String discussionItem;

	/** 事業者合否ステータス(0:否決、 1:合意) */
	@Column(name = "business_pass_status")
	private String businessPassStatus;

	/** 事業者合否コメント */
	@Column(name = "business_pass_comment")
	private String businessPassComment;

	/** 行政確定ステータス(0:合意 1:取下 2:却下) */
	@Column(name = "government_confirm_status", columnDefinition = "char(1)")
	private String governmentConfirmStatus;

	/** 行政確定日時(事前協議のみ) */
	@Column(name = "government_confirm_datetime")
	private LocalDateTime governmentConfirmDatetime;

	/** 行政確定コメント(事前協議のみ) */
	@Column(name = "government_confirm_comment")
	private String governmentConfirmComment;

	/** 行政確定通知フラグ(1:通知済み 0:未通知) */
	@Column(name = "government_confirm_notified_flag", columnDefinition = "char(1)")
	private Boolean governmentConfirmNotifiedFlag;

	/** 許可判定結果(許可判定のみ、0:問題なし、1:問題あり) */
	@Column(name = "permission_judgement_result", columnDefinition = "char(1)")
	private String permissionJudgementResult;

	/** ステータス(0:未回答、1：回答済み、2：承認待ち、3：否認済み、4：承認済み、5：却下、6：同意済み) */
	@Column(name = "answer_status", columnDefinition = "char(1)")
	private String answerStatus;

	/** データ種類(0:登録、1:更新、2：追加、3:行政で追加、4:一律追加、5:削除済み、6：引継、7:削除済み（行政）) */
	@Column(name = "answer_data_type", columnDefinition = "char(1)")
	private String answerDataType;

	/** 登録ステータス(0: 仮申請中 1: 申請済み) */
	@Column(name = "register_status", columnDefinition = "char(1)")
	private String registerStatus;

	/** 削除未通知フラグ(1:削除済み・未通知) */
	@Column(name = "delete_unnotified_flag", columnDefinition = "char(1)")
	private Boolean deleteUnnotifiedFlag;

	/** 回答期限日時 */
	@Column(name = "deadline_datetime")
	private LocalDateTime deadlineDatetime;

	/** 回答通知許可フラグ(1=許可済み 0=未許可) */
	@Column(name = "answer_permission_flag", columnDefinition = "char(1)")
	private Boolean answerPermissionFlag;

	/** 行政確定通知許可フラグ(1=許可済み 0=未許可) */
	@Column(name = "government_confirm_permission_flag", columnDefinition = "char(1)")
	private Boolean governmentConfirmPermissionFlag;

	/** 許可判定移行フラグ(1:許可判定移行時チェックしない) */
	@Column(name = "permission_judgement_migration_flag", columnDefinition = "char(1)")
	private Boolean permissionJudgementMigrationFlag;

	/** 版情報 */
	@Column(name = "version_information")
	private Integer versionInformation;

	/** 事業者回答登録日時 */
	@Column(name = "business_answer_datetime")
	private LocalDateTime businessAnswerDatetime;
}
