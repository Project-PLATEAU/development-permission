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
 * 問合せ情報検索結果データフォーム
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
public class ChatSearchResultForm  implements Serializable {
	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;
	/** 申請ID */
	@ApiModelProperty(value = "申請ID", example = "1")
	private Integer applicationId;
	
	/** チャットID */
	@ApiModelProperty(value = "チャットID", example = "1")
	private Integer chatId;
	
	/** ステータス */
	@ApiModelProperty(value = "ステータス", example = "1")
	private Integer status;
	
	/** 回答対象 */
	@ApiModelProperty(value = "回答対象")
	private String categoryJudgementTitle;
	
	/** 回答担当課	 */
	@ApiModelProperty(value = "回答担当課")
	private String departmentName;
	
	/** 最新回答者	 */
	@ApiModelProperty(value = "最新回答者")
	private String answerUserName;
	
	/** 最新回答日時		 */
	@ApiModelProperty(value = "最新回答日時",example = "2023/04/20 16:00")
	private String answerDatetime;
	
	/** 最新投稿日時		 */
	@ApiModelProperty(value = "最新投稿日時",example = "2023/04/20 16:00")
	private String sendDatetime;
	
	/** 事業者初回投稿日時		 */
	@ApiModelProperty(value = "事業者初回投稿日時",example = "2023/04/20 16:00")
	private String establishmentFirstPostDatetime;

	/** 最新メッセージ */
	@ApiModelProperty(value = "最新メッセージ")
	private MessageForm message;
	
	/** 部署一覧 */
	@ApiModelProperty(value = "部署一覧")
	private List<DepartmentForm> departments;
	
	/** 地番一覧 */
	@ApiModelProperty(value = "地番一覧")
	private List<LotNumberForm> lotNumbers;
	
}
