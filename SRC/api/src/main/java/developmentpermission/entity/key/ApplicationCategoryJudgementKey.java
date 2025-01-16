package developmentpermission.entity.key;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Id;

import lombok.Data;

/**
 * M_申請区分_区分判定Entityの複合キークラス
 */
@Embeddable
@Data

public class ApplicationCategoryJudgementKey implements Serializable {
	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** 申請区分ID */
	@Column(name = "judgement_item_id")
	private String judgementItemId;

	/** 画面ID */
	@Column(name = "view_id")
	private String viewId;

	/** 昇順 */
	@Column(name = "category_id")
	private String categoryId;

}
