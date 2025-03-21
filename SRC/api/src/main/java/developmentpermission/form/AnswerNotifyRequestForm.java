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
 * 回答通知のパラメータフォーム
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
public class AnswerNotifyRequestForm implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** 申請ID */
	@ApiModelProperty(value = "申請ID", example = "1")
	private Integer applicationId;

	/** 申請段階ID */
	@ApiModelProperty(value = "申請段階ID", example = "1")
	private Integer applicationStepId;
	
	/** 更新日時 */
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.nnnnnnnnn")
	@ApiModelProperty(value = "更新日時", example = "2022-08-30T12:56:00.000")
	private LocalDateTime updateDatetime;

	/** 回答一覧 */
	@ApiModelProperty(value = "回答一覧")
	private List<AnswerForm> answers;

	/** 部署回答一覧（事前協議のみ） */
	@ApiModelProperty(value = "部署回答一覧")
	private List<DepartmentAnswerForm> departmentAnswers;
	
	/** 回答通知種類 */
	@ApiModelProperty(value = "回答通知種類", example = "1")
	private String notifyType;
	
	/** 申請受付・差戻コメント */
	@ApiModelProperty(value = "回答通知種類", example = "xxx")
	private String acceptCommentText;
}
