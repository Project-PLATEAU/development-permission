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
 * メッセージフォーム
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
public class MessageForm  implements Serializable {
	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;
	
	/** メッセージID */
	@ApiModelProperty(value = "メッセージID", example = "1")
	private Integer messageId;
	
	/** メッセージ本文 */
	@ApiModelProperty(value = "メッセージ本文", example = "sampletext")
	private String messageText;
	
	/** 既読フラグ */
	@ApiModelProperty(value = "既読フラグ", example = "true")
	private Boolean readFlag;
	
	/** 送信ユーザ（行政による送信・閲覧時のみ付加） */
	@ApiModelProperty(value = "送信ユーザ")
	private GovernmentUserForm sender;
	
	/** 宛先部署（行政による送信時のみ付加） */
	@ApiModelProperty(value = "宛先部署")
	private List<InquiryAddressForm> inquiryAddressForms;
	
	/** メッセージタイプ */
	@ApiModelProperty(value = "メッセージタイプ", example = "1")
	private Integer messageType;
	
	/** 送信日時 */
	@ApiModelProperty(value = "送信日時", example = "2023/04/20 16:00")
	private String sendDatetime;
	
	/** 回答済みフラグ */
	@ApiModelProperty(value = "回答済みフラグ", example = "true")
	private Boolean answerCompleteFlag;
	
	/** 問い合わせ添付ファイル一式 */
	@ApiModelProperty(value = "問い合わせ添付ファイル一式")
	private List<InquiryFileForm> inquiryFiles;
	
}
