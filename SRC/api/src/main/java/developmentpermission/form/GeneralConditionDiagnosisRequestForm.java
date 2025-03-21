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
 * 概況診断リクエストフォーム
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
public class GeneralConditionDiagnosisRequestForm implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** 申請地番一覧 */
	@ApiModelProperty(value = "申請地番一覧")
	private List<LotNumberForm> lotNumbers;

	/** 申請地番一覧（2回目以降申請用） */
	@ApiModelProperty(value = "申請地番一覧")
	private List<ApplyLotNumberForm> applyLotNumbers;

	/** 申請区分一覧 */
	@ApiModelProperty(value = "申請区分選択一覧")
	private List<ApplicationCategorySelectionViewForm> applicationCategories;
	
	/** 申請種類ID */
	@ApiModelProperty(value = "申請種類ID")
	private Integer applicationTypeId;
	
	/** 申請段階ID */
	@ApiModelProperty(value = "申請段階ID")
	private Integer applicationStepId;
	
	/** 申請ID(再申請の場合のみ) */
	@ApiModelProperty(value = "申請ID")
	private Integer applicationId;
	
	/** 前回申請段階ID(再申請の場合のみ) */
	@ApiModelProperty(value = "前回申請段階ID")
	private Integer preApplicationStepId;
	
	/** 受付版情報(事前協議⇒事前協議の再申請の場合のみ) */
	@ApiModelProperty(value = "受付版情報")
	private Integer acceptVersionInformation;	
	
}
