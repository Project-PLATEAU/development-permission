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
public class AnswerHistory  implements Serializable {
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
	
}
