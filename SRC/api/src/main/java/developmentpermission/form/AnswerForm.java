package developmentpermission.form;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.Column;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 回答情報フォーム
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
public class AnswerForm implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** 回答ID */
	@ApiModelProperty(value = "回答ID", example = "1")
	private Integer answerId;

	/** 部署回答ID */
	@ApiModelProperty(value = "部署回答ID", example = "1")
	private Integer departmentAnswerId;

	/** 編集可否(回答レコード) */
	@ApiModelProperty(value = "編集可否（事業者の場合常にfalse）", example = "true")
	private Boolean editable;

	/** 編集可否(回答内容欄のみ) */
	@ApiModelProperty(value = "編集可否（事業者の場合常にfalse）", example = "true")
	private Boolean answerContentEditable;

	/** 判定結果 */
	@ApiModelProperty(value = "判定結果", example = "相談必要")
	private String judgementResult;

	/** 回答内容 */
	@ApiModelProperty(value = "回答内容", example = "xxxxxxx")
	private String answerContent;

	/** 更新日時 */
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.nnnnnnnnn")
	@ApiModelProperty(value = "更新日時", example = "2022-08-30T12:56:00.000")
	private LocalDateTime updateDatetime;

	/** 完了フラグ */
	@ApiModelProperty(value = "完了フラグ", example = "true")
	private Boolean completeFlag;

	/** 通知フラグ */
	@ApiModelProperty(value = "通知フラグ", example = "true")
	private Boolean notifiedFlag;

	/** 回答変更フラグ */
	@ApiModelProperty(value = "回答変更フラグ", example = "true")
	private Boolean answerUpdateFlag;

	/** 区分判定情報 */
	@ApiModelProperty(value = "区分判定情報")
	private AnswerJudgementForm judgementInformation;

	/** 回答ファイル */
	@ApiModelProperty(value = "回答ファイル")
	private List<AnswerFileForm> answerFiles;

	/** チャット情報 */
	@ApiModelProperty(value = "チャット情報")
	private ChatForm chat;

	/** 回答テンプレート */
	@ApiModelProperty(value = "回答テンプレート")
	private List<AnswerTemplateForm> answerTemplate;

	/** 再申請フラグ */
	@ApiModelProperty(value = "再申請フラグ")
	private Boolean reApplicationFlag;

	/** 事前協議フラグ */
	@ApiModelProperty(value = "事前協議フラグ")
	private Boolean discussionFlag;

	/** 申請段階 */
	@ApiModelProperty(value = "申請段階")
	private ApplicationStepForm applicationStep;

	/** 協議対象(カンマ区切りで保持) */
	@ApiModelProperty(value = "協議対象", example = "101")
	private String discussionItem;

	/** 協議対象一覧 */
	@ApiModelProperty(value = "協議対象一覧", example = "101")
	private List<LedgerMasterForm> discussionItems;

	/** 事業者合否ステータス(0:否決、 1:合意) */
	@ApiModelProperty(value = "事業者合否ステータス", example = "0")
	private String businessPassStatus;

	/** 事業者回答登録日時(事前協議のみ) */
	@ApiModelProperty(value = "事業者回答登録日時", example = "2023/04/01")
	private String businessAnswerDatetime;

	/** 行政確定ステータス(0:合意 1:取下 2:却下) */
	@ApiModelProperty(value = "行政確定ステータス", example = "0")
	private String governmentConfirmStatus;

	/** 行政確定日時(事前協議のみ) */
	@ApiModelProperty(value = "行政確定日時", example = "2023/04/01")
	private String governmentConfirmDatetime;

	/** 行政確定コメント(事前協議のみ) */
	@ApiModelProperty(value = "行政確定コメント", example = "xxxxxxx")
	private String governmentConfirmComment;

	/** 行政確定通知フラグ(1:通知済み 0:未通知) */
	@ApiModelProperty(value = "行政確定通知フラグ", example = "0")
	private Boolean governmentConfirmNotifiedFlag;

	/** 許可判定結果(許可判定のみ、0:問題なし、1:問題あり) */
	@ApiModelProperty(value = "許可判定結果", example = "0")
	private String permissionJudgementResult;

	/** 回答ステータス(0:未回答、1：回答済み、2：承認待ち、3：否認済み、4：承認済み、5：却下、6：同意済み) */
	@ApiModelProperty(value = "回答ステータス", example = "0")
	private String answerStatus;

	/** 回答データ種類(0:登録、1:更新、2：追加、3:行政で追加、4:一律追加、5:削除済み、6：引継、7:削除済み（行政）) */
	@ApiModelProperty(value = "回答データ種類", example = "0")
	private String answerDataType;

	/** 削除未通知フラグ(1:削除済み・未通知) */
	@ApiModelProperty(value = " 削除未通知フラグ", example = "true")
	private Boolean deleteUnnotifiedFlag;

	/** 回答期限日時 */
	@ApiModelProperty(value = "回答期限日時", example = "6/11")
	private String deadlineDatetime;

	/** 回答通知選択可否(事業者へ通知) */
	@ApiModelProperty(value = "回答通知選択可否(事業者へ通知)", example = "true")
	private Boolean notificable;
	
	/** 回答通知選択可否(許可通知) */
	@ApiModelProperty(value = "回答通知選択可否(許可通知)", example = "true")
	private Boolean permissionNotificable;

	/** チェック有無 */
	@ApiModelProperty(value = "チェック有無", example = "true")
	private Boolean checked;

	/** 回答履歴 */
	@ApiModelProperty(value = "回答履歴一覧")
	private List<AnswerHistoryForm> answerHistorys;

	/** 回答通知許可フラグ(1=許可済み 0=未許可) */
	@ApiModelProperty(value = "許可判定結果", example = "0")
	private Boolean answerPermissionFlag;

	/** 行政確定通知許可フラグ(1=許可済み 0=未許可) */
	@ApiModelProperty(value = "許可判定結果", example = "0")
	private Boolean governmentConfirmPermissionFlag;

	/** 許可判定移行フラグ(1:許可判定移行時チェックしない) */
	@ApiModelProperty(value = "許可判定結果", example = "0")
	private Boolean permissionJudgementMigrationFlag;
}
