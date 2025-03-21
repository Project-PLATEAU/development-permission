package developmentpermission.entity.key;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.Data;

/**
 * M_権限Entityの複合キークラス
 */
@Embeddable
@Data
public class AuthorityKey implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** 部署ID */
	@Column(name = "department_id")
	private String departmentId;

	/** 申請段階ID */
	@Column(name = "application_step_id")
	private Integer applicationStepId;
}
