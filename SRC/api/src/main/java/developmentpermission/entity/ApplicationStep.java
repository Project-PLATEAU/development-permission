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
 * O_申請段階Entityクラス.
 *
 *
 */
@Entity
@Data
@Table(name = "m_application_step")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ApplicationStep implements Serializable {
	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** 申請段階ID */
	@Id
	@Column(name = "application_step_id")
	private Integer applicationStepId;

	/** 申請段階名称 */
	@Column(name = "application_step_name")
	private String applicationStepName;

}
