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
 * 再申請フォーム
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
public class ReApplicationForm implements Serializable {
	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;
	
	/** 申請ID */
	@ApiModelProperty(value = "申請ID")
	private Integer applicationId;
	
	/** 概況診断レポート出力要否 */
	@ApiModelProperty(value = "概況診断レポート出力要否", example = "true")
	private Boolean outputReportFlag;
	
	/** 一時フォルダ名 */
	@ApiModelProperty(value = " 一時フォルダ名", example = "xxxxx")
	private String folderName;
	
	/** 申請種類ID */
	@ApiModelProperty(value = "申請種類ID")
	private Integer applicationTypeId;
	
	/** 申請種類 */
	@ApiModelProperty(value = "申請種類")
	private ApplicationTypeForm applicationType;
	
	/** 申請段階ID */
	@ApiModelProperty(value = "申請段階ID")
	private Integer applicationStepId;
	
	/** 前回申請段階ID */
	@ApiModelProperty(value = "前回申請段階ID", example = "1")
	private Integer preApplicationStepId;

	/** 版情報 */
	@ApiModelProperty(value = "版情報")
	private Integer versionInformation;
	
	/** 受付版情報 */
	@ApiModelProperty(value = "受付版情報")
	private Integer acceptVersionInformation;
	
	/** ログインID */
	@ApiModelProperty(value = "ログインID", example = "00111223344")
	private String loginId;

	/** パスワード */
	@ApiModelProperty(value = "パスワード", example = "password")
	private String password;

	/** 申請地番一覧 */
	@ApiModelProperty(value = "申請地番一覧")
	private List<ApplyLotNumberForm> lotNumbers;
	
	/** 申請区分選択一覧 */
	@ApiModelProperty(value = "申請区分選択一覧")
	private List<ApplicationCategorySelectionViewForm> applicationCategories;
	
	/** 申請者情報一覧 */
	@ApiModelProperty(value = "申請者情報一覧")
	private List<ApplicantInformationItemForm> applicantInformations;
	
	/** 申請追加情報一覧 */
	@ApiModelProperty(value = "申請追加情報一覧")
	private List<ApplicantInformationItemForm> applicantAddInformations;
	
	/** 申請ファイル一覧 */
	@ApiModelProperty(value = "申請ファイル一覧")
	private List<ApplicationFileForm> applicationFileForm;
	
	/** 区分判定（概況診断結果）一覧 */
	@ApiModelProperty(value = "区分判定（概況診断結果）一覧")
	private List<GeneralConditionDiagnosisResultForm> generalConditionDiagnosisResultForm;

	/** 申請ファイル一覧 */
	@ApiModelProperty(value = "申請ファイル一覧")
	private List<UploadApplicationFileForm> uploadFiles;
}
