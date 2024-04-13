package developmentpermission.form;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
/**
 * 回答ファイル履歴フォーム
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
public class AnswerFileHistoryForm implements Serializable{
	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;
	
	/** 更新日時 */
	@ApiModelProperty(value = "更新日時", example = "2023/04/01 10:00")
	private String updateDatetime;
	
	/** 更新ユーザID */
	@ApiModelProperty(value = "更新ユーザID")
	private String updateUserId;
	
	/** 更新ユーザ名 */
	@ApiModelProperty(value = "更新ユーザ名")
	private String updateUserName;
	
	/** 判定結果 */
	@ApiModelProperty(value = "判定結果", example = "相談必要")
	private String judgementResult;
	
	/** ファイル名 */
	@ApiModelProperty(value = "ファイル名", example = "xxx.pdf")
	private String fileName;
	
	/** 更新タイプ */
	@ApiModelProperty(value = "更新タイプ", example = "追加")
	private String updateType;
	
	/** 通知フラグ */
	@ApiModelProperty(value = "通知フラグ", example = "true")
	private Boolean notifiedFlag;
	
	/** 部署ID */
	@ApiModelProperty(value = "部署ID", example = "0001")
	private String departmentId;
	/** 部署名 */
	@ApiModelProperty(value = "部署名", example = "○○課")
	private String departmentName;
}
