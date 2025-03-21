package developmentpermission.form;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 受付回答情報フォーム
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
public class AcceptingAnswerForm implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** 受付回答ID */
	@ApiModelProperty(value = "受付回答ID", example = "1")
	private Integer acceptingAnswerId;

	/** 申請段階 */
	@ApiModelProperty(value = "申請段階")
	private ApplicationStepForm applicationStep;

	/** 版情報 */
	@Column(name = "version_infomation")
	private Integer versionInfomation;

	/** 区分判定情報 */
	@ApiModelProperty(value = "区分判定情報")
	private AnswerJudgementForm judgementInformation;

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

	/** 回答データ種類(0:登録、1:更新、2：追加、3:行政で追加、4:一律追加、5:削除済み、6：引継、7:削除済み（行政）) */
	@ApiModelProperty(value = "回答データ種類", example = "0")
	private String answerDataType;

	/** 回答期限日時 */
	@ApiModelProperty(value = "回答期限日時", example = "6/11")
	private String deadlineDatetime;

	/** 回答ID */
	@ApiModelProperty(value = "回答ID", example = "1")
	private Integer answerId;

	/** 回答履歴 */
	@ApiModelProperty(value = "回答履歴一覧")
	private List<AnswerHistoryForm> answerHistorys;
}
