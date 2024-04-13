package developmentpermission.form;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
/**
 * 回答履歴フォーム
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
public class AnswerHistoryForm  implements Serializable {
	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;
	
	/** 回答履歴ID */
	@ApiModelProperty(value = "回答履歴ID", example = "1")
	private Integer answerHistoryId;
	
	/** 回答ID */
	@ApiModelProperty(value = "回答ID", example = "1")
	private Integer answerId;
	
	/** 判定結果 */
	@ApiModelProperty(value = "判定結果", example = "相談必要")
	private String judgementResult;

	/** 回答内容 */
	@ApiModelProperty(value = "回答内容", example = "回答内容です")
	private String answerContent;
	
	/** 回答者ユーザ */
	@ApiModelProperty(value = "回答者ユーザ")
	private GovernmentUserForm answererUser;
	
	/** 通知フラグ */
	@ApiModelProperty(value = "通知フラグ", example = "true")
	private Boolean notifiedFlag;
	
	/** 更新日時 */
	@ApiModelProperty(value = "更新日時", example = "2023/04/20 16:00")
	private String updateDatetime;
	
}
