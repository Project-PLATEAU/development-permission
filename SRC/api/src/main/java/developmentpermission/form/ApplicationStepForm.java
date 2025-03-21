package developmentpermission.form;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 
 * M_申請段階フォーム
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
public class ApplicationStepForm implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** 申請段階ID */
	@ApiModelProperty(value = "申請段階ID", example = "1")
	private Integer applicationStepId;

	/** 申請段階名 */
	@ApiModelProperty(value = "申請段階名称", example = "事前相談")
	private String applicationStepName;

	/** 選択状態 */
	@ApiModelProperty(value = "選択状態", example = "true")
	private Boolean checked;
}
