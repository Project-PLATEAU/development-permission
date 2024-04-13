package developmentpermission.form;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * アンケート利用目的フォーム
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
public class QuestionaryPurposeForm implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;
	
	/** 利用目的値 */
	@ApiModelProperty(value = "利用目的値", example = "1")
	private String value;

	/** 利用目的ラベル */
	@ApiModelProperty(value = "利用目的ラベル", example = "開発許可申請")
	private String text;

	/** 選択状態 */
	@ApiModelProperty(value = "選択状態", example = "true")
	private Boolean checked;
}
