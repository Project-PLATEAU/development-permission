package developmentpermission.form;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 概況診断結果レポート出力リクエストフォーム
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
public class GeneralConditionDiagnosisReportRequestForm implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;
	
	/** 申請ID */
	@ApiModelProperty(value = "申請ID", example = "1")
	private Integer applicationId;
	
	/** ログインID */
	@ApiModelProperty(value = "ログインID", example = "00111223344")
	private String loginId;

	/** パスワード */
	@ApiModelProperty(value = "パスワード", example = "password")
	private String password;
	
	/** 一時フォルダ名*/
	@ApiModelProperty(value = "一時フォルダ名", example = "XXXXXXXX")
	private String folderName;
	
	/** 一時ファイル名*/
	@ApiModelProperty(value = "ファイル名", example = "概況診断結果_YYYYMMDDHHMMSS.xlsx")
	private String fileName;

	/** 申請地番一覧（初回申請用） */
	@ApiModelProperty(value = "申請地番一覧")
	private List<LotNumberForm> lotNumbers;
	
	/** 申請地番一覧（2回目以降申請用） */
	@ApiModelProperty(value = "申請地番一覧")
	private List<ApplyLotNumberForm> applyLotNumbers;
	/** 申請区分選択一覧 */
	@ApiModelProperty(value = "申請区分選択一覧")
	private List<ApplicationCategorySelectionViewForm> applicationCategories;

	/** 概況診断結果一覧 */
	@ApiModelProperty(value = "概況診断結果一覧")
	private List<GeneralConditionDiagnosisResultForm> generalConditionDiagnosisResults;
	
	/** 回答IDと回答内容-概況診断結果ID紐づけ情報 */
	@ApiModelProperty(value = "回答IDと回答内容-概況診断結果ID紐づけ情報")
	private Map<Integer, Map<String, String>> answerJudgementMap;
	
	/** 申請種類ID */
	@ApiModelProperty(value = "申請種類ID")
	private Integer applicationTypeId;
	
	/** 申請段階ID */
	@ApiModelProperty(value = "申請段階ID")
	private Integer applicationStepId;
}
