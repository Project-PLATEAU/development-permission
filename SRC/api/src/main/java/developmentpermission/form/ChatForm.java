package developmentpermission.form;
import java.io.Serializable;
import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
/**
 * チャットフォーム
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
public class ChatForm implements Serializable {
	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;
	
	/** チャットID */
	@ApiModelProperty(value = "チャットID", example = "1")
	private Integer chatId;
	
	/** 回答ID */
	@ApiModelProperty(value = "回答ID", example = "1")
	private Integer answerId;
	
	/** メッセージ一覧 */
	@ApiModelProperty(value = "メッセージ一覧")
	private List<MessageForm> messages;
}
