package developmentpermission.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * O_申請ファイルEntityクラス
 */
@Entity
@Data
@Table(name = "o_application_file")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ApplicationFile implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** ファイルID */
	@Id
	@Column(name = "file_id")
	private Integer fileId;

	/** 申請ID */
	@Column(name = "application_id")
	private Integer applicationId;

	/** 申請ファイルID */
	@Column(name = "application_file_id")
	private String applicationFileId;

	/** アップロードファイル名 */
	@Column(name = "upload_file_name")
	private String uploadFileName;

	/** ファイルパス */
	@Column(name = "file_path")
	private String filePath;
	
	/** 版情報 */
	@Column(name = "version_information")
	private Integer versionInformation;
	
	/** 拡張子 */
	@Column(name = "extension")
	private String extension;
	
	/** アップロード日時 */
	@Column(name = "upload_datetime")
	private LocalDateTime uploadDatetime;
	
}
