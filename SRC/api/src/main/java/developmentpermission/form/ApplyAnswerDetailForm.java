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
 * 申請段階ごとの申請・回答情報フォーム
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
public class ApplyAnswerDetailForm implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** 申請ID */
	@ApiModelProperty(value = "申請ID", example = "1")
	private Integer applicationId;

	/** 申請段階ID */
	@ApiModelProperty(value = "申請段階ID", example = "1")
	private Integer applicationStepId;

	/** 申請段階名 */
	@ApiModelProperty(value = "申請段階名称", example = "事前相談")
	private String applicationStepName;

	/** 版情報 */
	@ApiModelProperty(value = "版情報", example = "1")
	private Integer versionInformation;

	/** 登録日時 */
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.nnnnnnnnn")
	@ApiModelProperty(value = "登録日時", example = "2022-08-30T12:56:00.000")
	private LocalDateTime registerDatetime;

	/** 更新日時 */
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.nnnnnnnnn")
	@ApiModelProperty(value = "更新日時", example = "2022-08-30T12:56:00.000")
	private LocalDateTime updateDatetime;

	/** 申請区分 */
	@ApiModelProperty(value = "申請区分選択一覧")
	private List<ApplicationCategorySelectionViewForm> applicationCategories;

	/** 申請追加情報一覧 */
	@ApiModelProperty(value = "申請追加情報一覧")
	private List<ApplicantInformationItemForm> applicantAddInformations;

	/** 申請ファイル一覧 */
	@ApiModelProperty(value = "申請ファイル一覧")
	private List<ApplicationFileForm> applicationFiles;

	/** 回答一覧（事前相談と許可判定） */
	@ApiModelProperty(value = "回答一覧")
	private List<AnswerForm> answers;

	/** 部署回答一覧（事前協議のみ） */
	@ApiModelProperty(value = "部署回答一覧")
	private List<DepartmentAnswerForm> departmentAnswers;

	/** 回答履歴 */
	@ApiModelProperty(value = "回答履歴一覧")
	private List<AnswerHistoryForm> answerHistorys;

	/** 回答ファイル更新履歴 */
	@ApiModelProperty(value = "回答ファイル更新履歴一覧")
	private List<AnswerFileHistoryForm> answerFileHistorys;

	/** 通知ファイル一覧 */
	@ApiModelProperty(value = "通知ファイル一覧")
	private List<LedgerForm> ledgerFiles;

	/** 回答ファイル一覧（申請段階単位のすべての回答ファイル） */
	@ApiModelProperty(value = "回答ファイル一覧")
	private List<AnswerFileForm> answerFiles;

	/** チャット情報（許可判定のみ設定） */
	@ApiModelProperty(value = "チャット情報")
	private ChatForm chat;

	/** 協議対象一覧（事前協議のみ） */
	@ApiModelProperty(value = "協議対象一覧")
	private List<LedgerMasterForm> ledgerMasters;

    // 事前協議フロー修正↓↓↓↓↓↓
	/** 受付フラグ(事前協議のみ、1=受付 0=未確認 2=差戻) */
	@ApiModelProperty(value = "受付フラグ")
	private String acceptingFlag;

	/** 受付版情報(事前協議のみ) */
	@ApiModelProperty(value = "受付版情報")
	private Integer acceptVersionInformation;

	/** 受付回答一覧（事前協議のみ） */
	@ApiModelProperty(value = "受付回答一覧")
	private List<DepartmentAnswerForm> departmentAcceptingAnswers;

	/** 受付確認情報かどうか */
	@ApiModelProperty(value = "受付確認情報かどうか", example = "true")
	private Boolean isAcceptInfo;
	
	/** 全ての回答に事業者合意登録完了しているかどうか（事前協議のみ） */
	@ApiModelProperty(value = "事業者回答完了かどうか", example = "true")
	private Boolean businessAnswerCompleted;

    // 事前協議フロー修正↑↑↑↑↑↑
}
