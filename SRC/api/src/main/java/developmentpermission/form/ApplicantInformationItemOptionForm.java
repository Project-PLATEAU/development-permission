package developmentpermission.form;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 申請者情報項目選択肢フォーム
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
public class ApplicantInformationItemOptionForm implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** 申請情報項目選択肢ID */
	@ApiModelProperty(value = "申請情報項目選択肢ID", example = "10001")
	private String id;

	/** 申請者情報項目ID */
	@ApiModelProperty(value = "申請者情報項目ID ", example = "1001")
	private String itemId;
	
	/** 昇順 */
	@ApiModelProperty(value = "昇順", example = "1")
	private Integer displayOrder;
	
	/** 選択肢名 */
	@ApiModelProperty(value = "選択肢名", example = "区分１")
	private String content;

	/** チェック有無 */
	@ApiModelProperty(value = "チェック有無", example = "true")
	private Boolean checked;

}
