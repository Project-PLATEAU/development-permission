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
 * O_回答履歴Entityクラス
 */
@Entity
@Data
@Table(name = "o_answer_history")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AnswerHistory implements Serializable {
	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** 回答履歴ID */
	@Id
	@Column(name = "answer_history_id")
	private Integer answerHistoryId;

	/** 回答ID */
	@Column(name = "answer_id")
	private Integer answerId;

	/** 回答者ID */
	@Column(name = "answer_user_id")
	private String answerUserId;

	/** 回答日時 */
	@Column(name = "answer_datetime")
	private LocalDateTime answerDatetime;

	/** 回答文言 */
	@Column(name = "answer_text")
	private String answerText;

	/** 通知フラグ */
	@Column(name = "notify_flag", columnDefinition = "char(1)")
	private Boolean notifyFlag;

	/** 再申請フラグ */
	@Column(name = "re_application_flag", columnDefinition = "char(1)")
	private Boolean reApplicationFlag;

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

	/** 許可判定結果(許可判定のみ、0:問題なし、1:問題あり) */
	@Column(name = "permission_judgement_result", columnDefinition = "char(1)")
	private String permissionJudgementResult;

	/** ステータス(0:未回答、1：回答済み、2：承認待ち、3：否認済み、4：承認済み、5：却下、6：同意済み) */
	@Column(name = "answer_status", columnDefinition = "char(1)")
	private String answerStatus;

	/** データ種類(0:登録、1:更新、2：追加、3:行政で追加、4:一律追加、5:削除済み、6：引継、7:削除済み（行政）) */
	@Column(name = "answer_data_type", columnDefinition = "char(1)")
	private String answerDataType;

	/** 更新日時 */
	@Column(name = "update_datetime")
	private LocalDateTime updateDatetime;
	
	/** 回答期限日時 */
	@Column(name = "deadline_datetime")
	private LocalDateTime deadlineDatetime;
}
