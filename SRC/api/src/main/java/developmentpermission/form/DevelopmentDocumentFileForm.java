package developmentpermission.form;

import java.io.Serializable;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 開発登録簿ファイルフォーム
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
public class DevelopmentDocumentFileForm implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** ファイルID */
	@ApiModelProperty(value = "ファイルID", example = "1")
	private Integer fileId;
	
	/** 開発登録簿マスタID */
	@ApiModelProperty(value = "開発登録簿マスタID", example = "1")
	private Integer developmentDocumentId;
	
	/** 申請ID */
	@ApiModelProperty(value = "申請ID", example = "1")
	private Integer applicationId;
	
	/** 書類名 */
	@ApiModelProperty(value = "書類名", example = "1")
	private String documentName;
	
	
	/** ログインID */
	@ApiModelProperty(value = "ログインID", example = "00111223344")
	private String loginId;

	/** パスワード */
	@ApiModelProperty(value = "パスワード", example = "password")
	private String password;
	
	/** アップロード日時 */
	@ApiModelProperty(value = "アップロード日時", example = "2023/04/20 16:00")
	private String uploadDatetime;
}
