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
 * M_申請ファイルフォーム
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
public class ApplicationFileForm implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** 申請ファイルID */
	@ApiModelProperty(value = "申請ファイルID", example = "1001")
	private String applicationFileId;

	/** 判定項目ID */
	@ApiModelProperty(value = "判定項目ID", example = "2001")
	private String judgementItemId;

	/** 必須有無(1:必須 0:任意 2:任意(注意文言あり) */
	@ApiModelProperty(value = "必須有無", example = "1")
	private String requireFlag;

	/** 申請ファイル名 */
	@ApiModelProperty(value = "申請ファイル名", example = "給排水平面図")
	private String applicationFileName;

	/** 申請ファイル拡張子 */
	@ApiModelProperty(value = "申請ファイル拡張子", example = "pdf")
	private String extension;
	
	/** 版情報 */
	@ApiModelProperty(value = "版情報")
	private Integer versionInformation;

	/** アップロードファイル一式 */
	@ApiModelProperty(value = "アップロードファイル一式")
	private List<UploadApplicationFileForm> uploadFileFormList;
	
	/** アップロードファイル一式 */
	@ApiModelProperty(value = "アップロードファイル一式")
	private List<UploadApplicationFileForm> applicationFileHistorys;
	
	/** アップロードファイル一式(回答登録で引用可能の全ての申請段階の申請ファイル) */
	@ApiModelProperty(value = "アップロードファイル一式")
	private List<UploadApplicationFileForm> applicationFileAllHistorys;
}
