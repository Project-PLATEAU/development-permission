package developmentpermission.form;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
/**
 * 
 * 条文ステータスフォーム
 *
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
public class ItemAnswerStatusForm implements Serializable{
	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** ステータス値 */
	@ApiModelProperty(value = "ステータス値", example = "1")
	private String value;

	/** ステータスラベル */
	@ApiModelProperty(value = "ステータスラベル", example = "承認済み")
	private String text;

	/** 選択状態 */
	@ApiModelProperty(value = "選択状態", example = "true")
	private Boolean checked;
}
