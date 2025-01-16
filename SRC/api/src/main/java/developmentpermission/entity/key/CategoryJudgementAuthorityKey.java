package developmentpermission.entity.key;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.Data;

/**
 * M_区分判定_権限Entityの複合キークラス
 */
@Embeddable
@Data
public class CategoryJudgementAuthorityKey implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** 判定項目ID */
	@Column(name = "judgement_item_id")
	private String judgementItemId;

	/** 部署ID */
	@Column(name = "department_id")
	private String departmentId;
}
