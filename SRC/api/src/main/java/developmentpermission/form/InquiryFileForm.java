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
 * O_問合せファイルフォーム
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
public class InquiryFileForm implements Serializable {
	
	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;
	
	/** メッセージID */
	@ApiModelProperty(value = "メッセージID", example = "1")
	private Integer messageId;
	
	/** 問合せファイルID*/
	@ApiModelProperty(value = "問合せファイルID", example = "1")
	private Integer inquiryFileId;
	
	/** ファイル名 */
	@ApiModelProperty(value = "ファイル名", example = "●●.pdf")
	private String fileName;
	
	/** ファイルパス */
	@ApiModelProperty(value = "ファイルパス", example = "/xxx/xxx/")
	private String filePath;
	
	/** 登録日時 */
	@ApiModelProperty(value = "登録日時", example = "2023/04/20 16:00")
	private String registerDatetime;
	
	/** アップロードファイル */
	@ApiModelProperty(value = "アップロードファイル")
	private MultipartFile uploadFile;

}
