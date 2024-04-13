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
 * 回答テンプレートフォーム
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
public class AnswerTemplateForm  implements Serializable{
	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** 回答テンプレートID */
	@ApiModelProperty(value = "回答テンプレートID", example = "1")
	private Integer answerTemplateId;
	
	/** 表示順 */
	@ApiModelProperty(value = "表示順", example="1")
	private Integer dispOrder;
	
	/** 回答テンプレートテキスト */
	@ApiModelProperty(value = "回答テンプレートテキスト", example="xxx")
	private String answerTemplateText;
	
	/** 判定項目ID */
	@ApiModelProperty(value = "判定項目ID", example="0001")
	private String judgementItemId;
}
