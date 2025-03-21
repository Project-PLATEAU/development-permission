package developmentpermission.form;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * M_帳票表示用フォーム
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
public class LedgerMasterForm implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** ID */
	@ApiModelProperty(value = "帳票マスタID")
	private String ledgerId;

	/** 申請段階ID */
	@ApiModelProperty(value = "申請段階ID")
	private Integer applicationStepId;

	/** 帳票名（帳票ファイルの名称） */
	@ApiModelProperty(value = "帳票名（帳票ファイルの名称）", example = "協議書")
	private String ledgerName;
	
	/** 画面表示ラベル */
	@ApiModelProperty(value = "画面表示ラベル", example = "32協議")
	private String displayName;
	
	/** テンプレートパス */
	@ApiModelProperty(value = "テンプレートパス", example = "/xxx/xxx")
	private String templatePath;
	
	/** 出力種類(0:常に出力、1：画面に選択されたレコードがあれば出力) */
	@ApiModelProperty(value = "出力種類", example = "1")
	private String outputType;
	
	/** 受領時通知要否 */
	@ApiModelProperty(value = "受領時通知要否")
	private Boolean notificationFlag;
	
	/** チェック有無 */
	@ApiModelProperty(value = "チェック有無", example = "true")
	private Boolean checked;
}
