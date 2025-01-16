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
 * O_申請ファイルファイルフォーム
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
public class UploadApplicationFileForm implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** ファイルID */
	@ApiModelProperty(value = "ファイルID", example = "1")
	private Integer fileId;

	/** 申請ID */
	@ApiModelProperty(value = "申請ID", example = "1")
	private Integer applicationId;
	
	/** 申請段階ID */
	@ApiModelProperty(value = "申請段階ID", example = "1")
	private Integer applicationStepId;
	
	/** 申請段階ID */
	@ApiModelProperty(value = "申請段階名称", example = "事前相談")
	private String applicationStepName;

	/** 申請ファイルID */
	@ApiModelProperty(value = "申請ファイルID", example = "1001")
	private String applicationFileId;

	/** アップロードファイル名 */
	@ApiModelProperty(value = "アップロードファイル名", example = "xxx.pdf")
	private String uploadFileName;

	/** ファイルパス */
	@ApiModelProperty(value = "ファイルパス", example = "xxx/xxxx/")
	private String filePath;
	
	/** 版情報 */
	@ApiModelProperty(value = "版情報", example = "1")
	private Integer versionInformation;
	
	/** 拡張子 */
	@ApiModelProperty(value = "拡張子", example = "pdf")
	private String extension;
	
	/** アップロードファイル */
	@ApiModelProperty(value = "アップロードファイル")
	private MultipartFile uploadFile;
	
	/** ログインID */
	@ApiModelProperty(value = "ログインID", example = "00111223344")
	private String loginId;

	/** パスワード */
	@ApiModelProperty(value = "パスワード", example = "password")
	private String password;
	
	/** アップロード日時 */
	@ApiModelProperty(value = "アップロード日時", example = "2023/04/20 16:00")
	private String uploadDatetime;
	
	/** 指示元部署一覧(カンマ区切り部署名) */
	@ApiModelProperty(value = "test1,test2")
	private String directionDepartment;
	
	/** 指示元部署一覧(カンマ区切り部署ID) */
	@ApiModelProperty(value = "0001,0002")
	private String directionDepartmentId;
	
	/** 修正内容 */
	@ApiModelProperty(value = "修正内容", example = "xxxxxxxxxxx")
	private String reviseContent;
}
