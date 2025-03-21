package developmentpermission.form;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 回答ファイル（引用）フォーム
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
public class QuoteFileForm implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/** 回答ID */
	@ApiModelProperty(value = "回答ID", example = "1")
	private Integer answerId;
	/** 申請ID */
	@ApiModelProperty(value = "申請ID", example = "1")
	private Integer applicationId;
	
	/** 申請段階ID */
	@ApiModelProperty(value = "申請段階ID", example = "1")
	private Integer applicationStepId;
	
	/** 部署UID */
	@ApiModelProperty(value = "部署ID", example = "1001")
	private String departmentId;
	/** 回答ファイルID */
	@ApiModelProperty(value = "回答ファイルID", example = "1")
	private Integer answerFileId;
	/** 回答ファイル名 */
	@ApiModelProperty(value = "回答ファイル名", example = "●●.pdf")
	private String answerFileName;
	/** 回答ファイルパス */
	@ApiModelProperty(value = "回答ファイルパス", example = "/xxx/xxx/")
	private String filePath;
	/** 通知済みファイルパス */
	@ApiModelProperty(value = "通知済みファイルパス", example = "/xxx/xxx/")
	private String notifiedFilePath;
	/** 削除未通知フラグ */
	@ApiModelProperty(value = " 削除未通知フラグ", example = "true")
	private Boolean deleteUnnotifiedFlag;
	/** ログインID */
	@ApiModelProperty(value = "ログインID", example = "00111223344")
	private String loginId;
	/** パスワード */
	@ApiModelProperty(value = "パスワード", example = "password")
	private String password;
}
