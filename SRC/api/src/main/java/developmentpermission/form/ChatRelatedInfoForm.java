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
 * 問い合わせの相関情報フォーム
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
public class ChatRelatedInfoForm implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** チャットID */
	@ApiModelProperty(value = "チャットID", example = "1")
	private Integer chatId;
	
	/** 申請ID */
	@ApiModelProperty(value = "申請ID", example = "1")
	private Integer applicationId;

	/** 回答一覧 */
	@ApiModelProperty(value = "回答一覧")
	private List<AnswerForm> answer;
	
	/** 回答履歴一覧*/
	@ApiModelProperty(value = "回答履歴一覧")
	private List<AnswerHistoryForm> answerHistorys;
	
	/** 回答ファイル一覧 */
	@ApiModelProperty(value = "回答ファイル一覧")
	private List<AnswerFileForm> answerFiles;

	/** 申請ファイル一覧 */
	@ApiModelProperty(value = "申請ファイル一覧")
	private List<ApplicationFileForm> applicationFiles;
	
	
	/** 申請地番一覧 */
	@ApiModelProperty(value = "申請地番一覧")
	private List<ApplyLotNumberForm> lotNumbers;
}
