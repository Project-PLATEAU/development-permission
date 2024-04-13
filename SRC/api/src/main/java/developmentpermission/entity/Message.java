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
 * O_メッセージEntityクラス.
 *
 *
 */
@Entity
@Data
@Table(name = "o_message")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Message implements Serializable {
	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;
	
	/** メッセージID*/
	@Id
	@Column(name = "message_id")
	private Integer messageId;
	
	/** チャットID */
	@Column(name = "chat_id")
	private Integer chatId;
	
	/** メッセージタイプ */
	@Column(name = "message_type")
	private Integer messageType;
	
	/** 送信者ID */
	@Column(name = "sender_id")
	private String senderId;
	
	/** 宛先部署ID */
	@Column(name = "to_department_id")
	private String toDepartmentId;
	
	/** メッセージ本文 */
	@Column(name = "message_text")
	private String messageText;
	
	/** 送信日時 */
	@Column(name = "send_datetime")
	private LocalDateTime sendDatetime;
	
	/** 既読フラグ */
	@Column(name = "read_flag", columnDefinition = "char(1)")
	private Boolean readFlag;
	
	/**回答済みフラグ */
	@Column(name = "answer_complete_flag", columnDefinition = "char(1)")
	private Boolean answerCompleteFlag;
}
