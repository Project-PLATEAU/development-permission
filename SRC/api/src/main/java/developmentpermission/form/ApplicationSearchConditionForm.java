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
 * 申請情報検索条件フォーム
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
public class ApplicationSearchConditionForm implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** 申請ID */
	@ApiModelProperty(value = "申請ID", example = "1")
	private Integer applicationId;

	/** 申請者情報一覧 */
	@ApiModelProperty(value = "申請者情報一覧")
	private List<ApplicantInformationItemForm> applicantInformationItemForm;

	/** 申請区分選択一覧 */
	@ApiModelProperty(value = "申請区分選択一覧")
	private List<ApplicationCategorySelectionViewForm> applicationCategories;

	/** ステータス */
	@ApiModelProperty(value = "ステータス")
	private List<StatusForm> status;
	
	/** 問い合わせステータス */
	@ApiModelProperty(value = "問い合わせステータス")
	private List<AnswerStatusForm> answerStatus;

	/** 条文ステータス */
	@ApiModelProperty(value = "条文ステータス")
	private List<ItemAnswerStatusForm> itemAnswerStatus;
	
	/** 部署 */
	@ApiModelProperty(value = "部署")
	private List<DepartmentForm> department;
	
	/** 回答者 */
	@ApiModelProperty(value = "回答者")
	private List<AnswerNameForm> answerName;
	
	/** 申請種類 */
	@ApiModelProperty(value = "申請種類")
	private List<ApplicationTypeForm> applicationTypes;
	
	/** 申請段階 */
	@ApiModelProperty(value = "申請段階")
	private List<ApplicationStepForm> applicationSteps;
	
	/** 申請追加情報一覧 */
	@ApiModelProperty(value = "申請追加情報一覧")
	private List<ApplicantInformationItemForm> applicantAddInformationItemForm;
}
