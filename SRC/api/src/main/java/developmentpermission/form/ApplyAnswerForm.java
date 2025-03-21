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
 * 申請・回答内容確認情報フォーム
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
public class ApplyAnswerForm implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** 申請ID */
	@ApiModelProperty(value = "申請ID", example = "1")
	private Integer applicationId;

	/** ステータス */
	@ApiModelProperty(value = "版情報付けるステータス", example = "第1版事前相談：未回答")
	private String status;
	
	/** ステータスコード */
	@ApiModelProperty(value = "ステータスコード", example = "101")
	private String statusCode;

	/** 申請種類 */
	@ApiModelProperty(value = "申請種類")
	private ApplicationTypeForm applicationType;
	
	/** 申請地番一覧 */
	@ApiModelProperty(value = "申請地番一覧")
	private List<ApplyLotNumberForm> lotNumbers;

	/** 申請者情報一覧 */
	@ApiModelProperty(value = "申請者情報一覧")
	private List<ApplicantInformationItemForm> applicantInformations;

	/** 申請情報詳細一覧 */
	@ApiModelProperty(value = "申請情報詳細一覧")
	private List<ApplyAnswerDetailForm> applyAnswerDetails;
	
	/** 回答権限 */
	@ApiModelProperty(value = "回答通知権限", example = "true")
	private Boolean notificable;
	
	/** 統括部署の管理者かどうか */
	@ApiModelProperty(value = "統括部署の管理者かどうか", example = "true")
	private Boolean controlDepartmentAdmin;
	
	/** 申請情報が初回受付確認中かどうか（事前協議のみ） */
	@ApiModelProperty(value = "申請情報が初回受付確認中かどうか（事前協議のみ）", example = "true")
	private Boolean firstAccepting;
}
