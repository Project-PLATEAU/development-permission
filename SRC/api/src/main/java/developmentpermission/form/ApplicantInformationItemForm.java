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
 * M_申請者情報項目フォーム
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
public class ApplicantInformationItemForm implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** 申請者情報項目ID */
	@ApiModelProperty(value = "申請者情報項目ID", example = "1001")
	private String id;

	/** 表示順 */
	@ApiModelProperty(value = "表示順", example = "1")
	private Integer order;

	/** 表示フラグ */
	@ApiModelProperty(value = "表示フラグ", example = "true")
	private Boolean displayFlag;

	/** 必須フラグ */
	@ApiModelProperty(value = "必須フラグ", example = "true")
	private Boolean requireFlag;
	
	/** 検索条件表示フラグ */
	@ApiModelProperty(value = "検索条件表示フラグ", example = "true")
	private Boolean searchConditionFlag;
	
	/** ラベル */
	@ApiModelProperty(value = "ラベル", example = "メールアドレス")
	private String name;

	/** 正規表現 */
	@ApiModelProperty(value = "正規表現", example = "^[a-zA-Z0-9_.+-]+@([a-zA-Z0-9][a-zA-Z0-9-]*[a-zA-Z0-9]*\\\\.)+[a-zA-Z]{2,}$")
	private String RegularExpressions;

	/** メールアドレスか否か */
	@ApiModelProperty(value = "メールアドレスか否か", example = "true")
	private Boolean mailAddress;
	
	/** 項目型 */
	@ApiModelProperty(value = "項目型", example = "1")
	private String itemType;

	/** 登録情報 */
	@ApiModelProperty(value = "登録情報", example = "dev@apply.com")
	private String value;
	
	/** 追加情報フラグ */
	@ApiModelProperty(value = "追加情報フラグ", example = "true")
	private Boolean addInformationItemFlag;

	/** 申請段階 */
	@ApiModelProperty(value = "追加情報フラグ")
	private List<ApplicationStepForm> applicationSteps;
	
	/** 連絡先登録情報 */
	@ApiModelProperty(value = "連絡先登録情報", example = "dev@apply.com")
	private String contactValue;
	
	/** 連絡先フラグ */
	@ApiModelProperty(value = "連絡先フラグ", example = "true")
	private Boolean contactAddressFlag;
	
	/** 申請者同一フラグ */
	@ApiModelProperty(value = "申請者同一フラグ", example = "true")
	private Boolean applicantSameFlag;

	/** 申請情報項目選択肢一覧 */
	@ApiModelProperty(value = "申請情報項目選択肢一覧")
    private List<ApplicantInformationItemOptionForm> itemOptions;
}
