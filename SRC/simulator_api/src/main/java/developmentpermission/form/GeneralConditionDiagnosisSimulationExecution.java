package developmentpermission.form;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
public class GeneralConditionDiagnosisSimulationExecution {
	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;
	
	/** 申請ID */
	@ApiModelProperty(value = "申請ID", example = "1")
	private Integer applicationId;

	/** 一時フォルダ名*/
	@ApiModelProperty(value = "一時フォルダ名", example = "XXXXXXXX")
	private String folderName;
	
	/** 一時ファイル名*/
	@ApiModelProperty(value = "ファイル名", example = "概況診断結果_YYYYMMDDHHMMSS.xlsx")
	private String fileName;

	/** 申請地番一覧 */
	@ApiModelProperty(value = "申請地番一覧")
	private List<Object> lotNumbers;

	/** 申請区分選択一覧 */
	@ApiModelProperty(value = "申請区分選択一覧")
	private List<Object> applicationCategories;

	/** 概況診断結果一覧 */
	@ApiModelProperty(value = "概況診断結果一覧")
	private List<Object> generalConditionDiagnosisResults;
}
