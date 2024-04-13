package developmentpermission.form;

import java.io.Serializable;
import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 再申請フォーム
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
public class ReApplicationForm implements Serializable {
	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;
	
	/** 申請ID */
	@ApiModelProperty(value = "申請ID")
	private Integer applicationId;
	
	/** 版情報 */
	@ApiModelProperty(value = "版情報")
	private Integer versionInformation;
	
	/** ログインID */
	@ApiModelProperty(value = "ログインID", example = "00111223344")
	private String loginId;

	/** パスワード */
	@ApiModelProperty(value = "パスワード", example = "password")
	private String password;

	/** 申請ファイル一覧 */
	@ApiModelProperty(value = "申請ファイル一覧")
	private List<ApplicationFileForm> applicationFileForm;
}
