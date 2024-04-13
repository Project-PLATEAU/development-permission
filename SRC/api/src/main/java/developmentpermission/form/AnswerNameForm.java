package developmentpermission.form;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 回答者フォーム
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
public class AnswerNameForm implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** ユーザーID */
	@ApiModelProperty(value = "ユーザーID", example = "0001")
	private String userId;	
	
	/** ログインID */
	@ApiModelProperty(value = "ログインID", example = "kankyoka_test")
	private String loginId;

	/** 職員名 */
	@ApiModelProperty(value = "ユーザー名", example = "環境課テスト")
	private String userName;
	
	/** 部署UID */
	@ApiModelProperty(value = "部署ID", example = "0001")
	private String departmentId;
	
	/** 部署名 */
	@ApiModelProperty(value = "部署名", example = "2F環境課")
	private String departmentName;
	
	/** 選択状態 */
	@ApiModelProperty(value = "選択状態", example = "true")
	private Boolean checked;

}
