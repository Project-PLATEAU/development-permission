package developmentpermission.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *O_開発登録簿Entityクラス
 */
@Entity
@Data
@Table(name = "o_development_document")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DevelopmentDocument implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** ファイルID */
	@Id
	@Column(name = "file_id")
	private Integer fileId;

	/** 申請ID */
	@Column(name = "application_id")
	private Integer applicationId;
	
	/** 開発登録簿マスタID */
	@Column(name = "development_document_id")
	private Integer developmentDocumentId;

	/** ファイルパス */
	@Column(name = "file_path")
	private String filePath;

	/** 作成日時 */
	@Column(name = "register_datetime")
	private LocalDateTime registerDatetime;

}
