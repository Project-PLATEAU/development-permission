package developmentpermission.entity;

import java.io.Serializable;

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
 *M_開発登録簿Entityクラス
 */
@Entity
@Data
@Table(name = "m_development_document")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DevelopmentDocumentMaster implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** 開発登録簿マスタID */
	@Id
	@Column(name = "development_document_id")
	private Integer developmentDocumentId;

	/** 書類名 */
	@Column(name = "document_name")
	private String documentName;

	/** 書類種類 */
	@Column(name = "document_type")
	private String documentType;

}
