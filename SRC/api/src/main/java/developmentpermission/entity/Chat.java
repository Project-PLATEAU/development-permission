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
 * O_チャットEntityクラス.
 *
 *
 */
@Entity
@Data
@Table(name = "o_chat")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Chat  implements Serializable {
	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;
	
	/** チャットID */
	@Id
	@Column(name = "chat_id")
	private Integer chatId;
	
	/** 回答ID */
	@Column(name = "answer_id")
	private Integer answerId;
	
	/** 行政回答日時 */
	@Column(name = "government_answer_datetime")
	private LocalDateTime governmentAnswerDatetime;
	
	/** 事業者投稿日時 */
	@Column(name = "establishment_post_datetime")
	private LocalDateTime establishmentPostDatetime;
	
	/** 最終回答者ID */
	@Column(name = "last_answerer_id")
	private String lastAnswererId;
	
	
	/**事業者初回投稿日時 */
	@Column(name = "establishment_first_post_datetime")
	private LocalDateTime establishmentFirstPostDatetime;
}
