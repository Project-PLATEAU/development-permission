package developmentpermission.form;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * O_問合せ宛先フォーム
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
public class InquiryAddressForm implements Serializable {
	
	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;
	
	/** メッセージID */
	@ApiModelProperty(value = "メッセージID", example = "1")
	private Integer messageId;
	
	/** 問合せ宛先ID */
	@ApiModelProperty(value = "問い合わせ宛先ID", example = "1")
	private Integer inquiryAddressId;
	
	/** 部署 */
	@ApiModelProperty(value = "部署")
	private DepartmentForm department;
	
	/** 既読フラグ */
	@ApiModelProperty(value = "既読フラグ", example = "true")
	private Boolean readFlag;

	/** 回答済みフラグ */
	@ApiModelProperty(value = "回答済みフラグ", example = "true")
	private Boolean answerCompleteFlag;

}
