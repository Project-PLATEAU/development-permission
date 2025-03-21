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
 * 再申請取得パラメータフォーム
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
public class ReApplicationRequestForm implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** ログインID */
	@ApiModelProperty(value = "ログインID", example = "00111223344")
	private String loginId;

	/** パスワード */
	@ApiModelProperty(value = "パスワード", example = "password")
	private String password;

	/** 申請ID */
	@ApiModelProperty(value = "申請ID", example = "1")
	private Integer applicationId;

	/** 申請段階ID */
	@ApiModelProperty(value = "申請段階ID", example = "1")
	private Integer applicationStepId;

	/** 前回申請段階ID */
	@ApiModelProperty(value = "前回申請段階ID", example = "1")
	private Integer preApplicationStepId;

	/** 区分判定（概況診断結果）一覧 */
	@ApiModelProperty(value = "区分判定（概況診断結果）一覧")
	private List<GeneralConditionDiagnosisResultForm> generalConditionDiagnosisResultFormList;
}
