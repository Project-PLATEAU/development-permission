package developmentpermission.form;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 概況診断結果レポート進捗フォーム
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
public class GeneralConditionDiagnosisReportProgressForm implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;
	
	/** 一時フォルダ名 */
	@ApiModelProperty(value = "一時フォルダ名", example = "xxxxx")
	private String folderName;
	
	/** 生成数 */
	@ApiModelProperty(value = "生成数", example = "1")
	private Integer capturedCount;
	
	/** ファイルサイズ */
	@ApiModelProperty(value = "ファイルサイズ", example = "200MB")
	private String fileSize;
	
	/** 完了日時(ブラウザ側) */
	@ApiModelProperty(value = "完了日時", example = "2024-05-23 15:00:58")
	private String completeDateTime;
	
}
