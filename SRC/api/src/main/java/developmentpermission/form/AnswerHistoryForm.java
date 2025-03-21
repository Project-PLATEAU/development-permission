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
	
	/** 申請段階 */
	@ApiModelProperty(value = "申請段階")
	private ApplicationStepForm applicationStep;
	
	/** 判定結果 */
	@ApiModelProperty(value = "判定結果", example = "相談必要")
	private String judgementResult;

	/** 区分判定タイトル */
	@ApiModelProperty(value = "区分判定タイトル ", example = "XXXについて、")
	private String title;
	
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
	
	/** 再申請フラグ */
	@ApiModelProperty(value ="再申請フラグ")
	private Boolean reApplicationFlag;
	
	/** 事前協議フラグ*/
	@ApiModelProperty(value ="事前協議フラグ")
	private Boolean discussionFlag;
	
	/** 協議対象(カンマ区切りで保持) */
	@ApiModelProperty(value = "協議対象", example = "101")
	private String discussionItem;
	
	/** 協議対象一覧 */
	@ApiModelProperty(value = "協議対象一覧", example = "101")
	private List<LedgerMasterForm> discussionItems;
	
	/** 事業者合否ステータス(0:否決、 1:合意) */
	@ApiModelProperty(value = "事業者合否ステータス", example = "0")
	private String businessPassStatus;

	/** 事業者合否コメント */
	@ApiModelProperty(value = "事業者合否コメント", example = "xxxxxxx")
	private String businessPassComment;

	/** 行政確定ステータス(0:合意 1:取下 2:却下) */
	@ApiModelProperty(value = "行政確定ステータス", example = "0")
	private String governmentConfirmStatus;

	/** 行政確定日時(事前協議のみ) */
	@ApiModelProperty(value = "行政確定日時", example = "2022-08-30T12:56:00.000")
	private String governmentConfirmDatetime;

	/** 行政確定コメント(事前協議のみ) */
	@ApiModelProperty(value = "行政確定コメント", example = "xxxxxxx")
	private String governmentConfirmComment;
	
	/** 回答ステータス(0:未回答、1：回答済み、2：承認待ち、3：否認済み、4：承認済み、5：却下、6：同意済み) */
	@ApiModelProperty(value = "回答ステータス", example = "0")
	private String answerStatus;

	/** 回答データ種類(0:登録、1:更新、2：追加、3:行政で追加、4:一律追加、5:削除済み、6：引継、7:削除済み（行政）) */
	@ApiModelProperty(value = "回答データ種類", example = "0")
	private String answerDataType;
}
