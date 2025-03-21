package developmentpermission.entity.key;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.Data;

/**
 * O_申請版情報Entityの複合キークラス
 */
@Embeddable
@Data
public class ApplicationVersionInformationKey implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** 申請ID */
	@Column(name = "application_id")
	private Integer applicationId;

	/** 申請段階ID */
	@Column(name = "application_step_id")
	private Integer applicationStepId;
}
