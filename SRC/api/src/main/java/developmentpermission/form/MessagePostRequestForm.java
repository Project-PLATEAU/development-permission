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
 * メッセージ投稿リクエストフォーム
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
public class MessagePostRequestForm implements Serializable{
	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;
	
	/** チャットID */
	@ApiModelProperty(value = "チャットID", example = "1")
	private Integer chatId;
	
	/** 回答ID */
	@ApiModelProperty(value = "回答ID", example = "1")
	private Integer answerId;
	
	/** ログインID （事業者のみ） */
	@ApiModelProperty(value = "ログインID", example = "00111223344")
	private String loginId;
	/** パスワード （事業者のみ） */
	@ApiModelProperty(value = "パスワード", example = "password")
	private String password;
	
	/** 宛先部署ID （行政のみ） */
	@ApiModelProperty(value = "宛先部署一覧")
	private  List<DepartmentForm> toDepartments;
	
	/** メッセージ */
	@ApiModelProperty(value = "メッセージ")
	private MessageForm message;
	
	/** 画面に表示されるの最大メッセージID */
	@ApiModelProperty(value = "メッセージID", example = "1")
	private Integer displayedMaxMessageId;
	
	/** 申請段階ID */
	@ApiModelProperty(value = "申請段階ID")
	private Integer applicationStepId;
	
	/** 部署回答ID */
	@ApiModelProperty(value = "部署回答ID")
	private Integer departmentAnswerId;
}
