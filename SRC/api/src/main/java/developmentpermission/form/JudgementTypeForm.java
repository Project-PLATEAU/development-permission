package developmentpermission.form;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 概況診断タイプフォーム
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
public class JudgementTypeForm implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** 概況診断タイプ値 */
	@ApiModelProperty(value = "概況診断タイプ", example = "1")
	private String value;

	/** 概況診断タイプラベル */
	@ApiModelProperty(value = "概況診断タイプのラベル", example = "開発許可関連")
	private String text;

	/** 選択状態 */
	@ApiModelProperty(value = "選択状態", example = "true")
	private Boolean checked;
}
