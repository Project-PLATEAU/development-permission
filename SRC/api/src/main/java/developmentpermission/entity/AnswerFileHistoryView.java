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
 * O_回答ファイル履歴ViewEntityクラス
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AnswerFileHistoryView implements Serializable{
	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;
	
	/** 回答ファイル履歴ID */
	@Id
	@Column(name = "answer_file_history_id")
	private Integer answerFileHistoryId;
	
	/** 回答ファイルID */
	@Column(name = "answer_file_id")
	private Integer answerFileId;
	
	/** 回答ID */
	@Column(name = "answer_id")
	private Integer answerId;
	
	/** 更新タイプ */
	@Column(name = "update_type")
	private Integer updateType;
	
	/** 更新者ID */
	@Column(name = "update_user_id")
	private String updateUserId;
	
	/** 更新日時 */
	@Column(name = "update_datetime")
	private LocalDateTime updateDatetime;
	
	/** 通知フラグ */
	@Column(name = "notify_flag", columnDefinition = "char(1)")
	private Boolean notifyFlag;
	
	/** 回答ファイル名 */
	@Column(name = "answer_file_name")
	private String answerFileName;
	
	/** 判定結果 */
	@Column(name = "judgement_result")
	private String judgementResult;
	
	/** ユーザ名 */
	@Column(name = "user_name")
	private String userName;
	
	/** 部署ID */
	@Column(name = "department_id")
	private String departmentId;
	
	/** 部署名 */
	@Column(name = "department_name")
	private String departmentName;
}
