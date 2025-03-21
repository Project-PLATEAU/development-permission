
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
 * M_申請種類フォーム
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
public class ApplicationTypeForm implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** 申請種類ID */
	@ApiModelProperty(value = "申請種類ID", example = "1")
	private Integer applicationTypeId;

	/** 申請種類名 */
	@ApiModelProperty(value = "申請種類名", example = "開発許可")
	private String applicationTypeName;

	/** 申請段階 */
	@ApiModelProperty(value = "申請段階一覧")
	private List<ApplicationStepForm> applicationSteps;
	
	/** 選択状態 */
	@ApiModelProperty(value = "選択状態", example = "true")
	private Boolean checked;

	/** 申請可能 */
	@ApiModelProperty(value = "申請可能", example = "true")
	private Boolean applicable;
}
