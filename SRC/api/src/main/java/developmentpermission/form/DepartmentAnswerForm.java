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
 * O_部署_回答フォーム
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
public class DepartmentAnswerForm implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** 部署回答ID */
	@ApiModelProperty(value = "部署回答ID", example = "1")
	private Integer departmentAnswerId;

	/** 申請ID */
	@ApiModelProperty(value = "申請ID")
	private Integer applicationId;

	/** 申請段階ID */
	@ApiModelProperty(value = "申請段階ID")
	private Integer applicationStepId;

	/** 部署 */
	@ApiModelProperty(value = "部署")
	private DepartmentForm department;

	/** 行政確定ステータス(0:合意 1:取下 2:却下) */
	@ApiModelProperty(value = "行政確定ステータス", example = "0")
	private String governmentConfirmStatus;

	/** 行政確定日時(事前協議のみ) */
	@ApiModelProperty(value = "行政確定日時", example = "2023/04/01")
	private String governmentConfirmDatetime;

	/** 行政確定コメント(事前協議のみ) */
	@ApiModelProperty(value = "行政確定コメント", example = "xxxxxxx")
	private String governmentConfirmComment;

	/** 完了フラグ */
	@ApiModelProperty(value = "完了フラグ", example = "true")
	private Boolean completeFlag;

	/** 通知フラグ */
	@ApiModelProperty(value = "通知フラグ", example = "true")
	private Boolean notifiedFlag;

	/** 更新日時 */
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.nnnnnnnnn")
	@ApiModelProperty(value = "更新日時", example = "2022-08-30T12:56:00.000")
	private LocalDateTime updateDatetime;

	/** 削除未通知フラグ(1:削除済み・未通知) */
	@ApiModelProperty(value = " 削除未通知フラグ", example = "true")
	private Boolean deleteUnnotifiedFlag;

	/** 編集可否 */
	@ApiModelProperty(value = "編集可否（事業者の場合常にfalse）", example = "true")
	private Boolean editable;

	/** 回答通知選択可否(事業者へ通知) */
	@ApiModelProperty(value = "回答通知選択可否(事業者へ通知)", example = "true")
	private Boolean notificable;
	
	/** 回答通知選択可否(許可通知) */
	@ApiModelProperty(value = "回答通知選択可否(許可通知)", example = "true")
	private Boolean permissionNotificable;

	/** チェック有無 */
	@ApiModelProperty(value = "チェック有無", example = "true")
	private Boolean checked;

	/** 回答一覧 */
	@ApiModelProperty(value = "回答一覧")
	private List<AnswerForm> answers;

	/** 回答ファイル */
	@ApiModelProperty(value = "回答ファイル")
	private List<AnswerFileForm> answerFiles;

	/** チャット情報 */
	@ApiModelProperty(value = "チャット情報")
	private ChatForm chat;

	/** 回答履歴一覧 */
	@ApiModelProperty(value = "回答履歴一覧")
	private List<AnswerHistoryForm> answerHistorys;

	/** 行政確定通知許可フラグ(1=許可済み 0=未許可) */
	@ApiModelProperty(value = "許可判定結果", example = "0")
	private Boolean governmentConfirmPermissionFlag;
	
	/** 受付回答一覧(受付確認可の場合のみ ) */
	@ApiModelProperty(value = "受付回答一覧")
	private List<AcceptingAnswerForm> acceptingAnswers;

	/** 回答内容を事業者へ通知済みの回答があるかフラグ */
	@ApiModelProperty(value = "回答内容を事業者へ通知済みの回答があるかフラグ", example = "true")
	private Boolean answerContentNotifiedFlag;
}
