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
 * 未回答の申請情報フォーム
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
public class ApplyAnswerSearchResultForm implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** 申請ID */
	@ApiModelProperty(value = "申請ID", example = "1")
	private Integer applicationId;

	/** ステータス */
	@ApiModelProperty(value = "版情報付けるステータス", example = "第1版事前相談：未回答")
	private String status;
	
	/** 期限 */
	@ApiModelProperty(value = "回答期限")
	String deadlineDate;
	
	/** 警告フラグ */
	@ApiModelProperty(value = "回答期限X日前の警告フラグ")
	Boolean warning;

	/** 地番一覧 */
	@ApiModelProperty(value = "地番一覧")
	private List<ApplyLotNumberForm> lotNumbers;
}
