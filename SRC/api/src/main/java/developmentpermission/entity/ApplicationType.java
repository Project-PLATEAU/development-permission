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
 * O_申請種類Entityクラス.
 *
 *
 */
@Entity
@Data
@Table(name = "m_application_type")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ApplicationType implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** 申請種類ID */
	@Id
	@Column(name = "application_type_id")
	private Integer applicationTypeId;

	/** 申請種類名称ID */
	@Column(name = "application_type_name")
	private String applicationTypeName;

	/** 申請段階（カンマ区切りで複数申請段階IDを保持） */
	@Column(name = "application_step")
	private String applicationStep;

}
