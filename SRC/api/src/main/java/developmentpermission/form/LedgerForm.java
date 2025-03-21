package developmentpermission.form;

import java.io.Serializable;

import org.springframework.web.multipart.MultipartFile;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 帳票ファイルフォーム
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
public class LedgerForm implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** ファイルID */
	@ApiModelProperty(value = "ファイルID", example = "1")
	private Integer fileId;

	/** 申請段階ID */
	@ApiModelProperty(value = "申請段階ID", example = "1")
	private Integer applicationStepId;

	/** 帳票マスタID */
	@ApiModelProperty(value = "帳票マスタID", example = "1001")
	private String ledgerId;

	/** 帳票名 */
	@ApiModelProperty(value = "帳票名", example = "同意書")
	private String ledgerName;
	
	/** 申請ID */
	@ApiModelProperty(value = "申請ID", example = "1")
	private Integer applicationId;

	/** アップロードファイル名 */
	@ApiModelProperty(value = "アップロードファイル名", example = "xxx.pdf")
	private String uploadFileName;

	/** ファイルパス */
	@ApiModelProperty(value = "ファイルパス", example = "xxx/xxxx/")
	private String filePath;
	
	/** 通知済みファイルパス */
	@ApiModelProperty(value = "通知済みファイルパス", example = "xxx/xxxx/")
	private String notifyFilePath;

	/** ログインID */
	@ApiModelProperty(value = "ログインID", example = "00111223344")
	private String loginId;

	/** パスワード */
	@ApiModelProperty(value = "パスワード", example = "password")
	private String password;

	/** 作成日時 */
	@ApiModelProperty(value = "作成日時", example = "2023/04/20 16:00")
	private String registerDatetime;

	/** 受領日時 */
	@ApiModelProperty(value = "受領日時", example = "2023/04/20 16:00")
	private String receiptDatetime;
	
	/** 拡張子（カンマ区切りテキスト） */
	@ApiModelProperty(value = "拡張子", example = "pdf")
	private String extension;
	
	/** 通知可否 */
	@ApiModelProperty(value = "通知可否", example = "true")
	private Boolean notifiable;
	
	/** 通知済みフラグ */
	@ApiModelProperty(value = "通知済み", example = "true")
	private Boolean notifyFlag;
	
	/** アップロード可否 */
	@ApiModelProperty(value = "アップロード可否", example = "true")
	private Boolean uploadable;

	/** アップロードファイル */
	@ApiModelProperty(value = "アップロードファイル")
	private MultipartFile uploadFile;
	
	/** 案内テキスト */
	@ApiModelProperty(value = "案内テキスト", example = "xxxxx")
	private String informationText;
	
}
