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
	
	/** 申請ID */
	@ApiModelProperty(value = "申請ID", example = "1")
	private Integer applicationId;
	
	/** 申請段階 */
	@ApiModelProperty(value = "申請段階", example = "1")
	private ApplicationStepForm applicationStep;
	
	/** 部署 */
	@ApiModelProperty(value = "部署ID", example = "1001")
	private DepartmentForm department;
	
	/** 部署回答ID */
	@ApiModelProperty(value = "部署回答ID", example = "1")
	private Integer departmentAnswerId;

	/** タイトル（事前相談：条項のタイトル、事前協議：担当部署名、許可判定：申請段階名） */
	@ApiModelProperty(value = "タイトル", example = "消火栓の設置等について")
	private String title;
	
	/** メッセージ一覧 */
	@ApiModelProperty(value = "メッセージ一覧")
	private List<MessageForm> messages;
	
	/** 選択状態 */
	@ApiModelProperty(value = "選択状態")
	private Boolean checked;
}