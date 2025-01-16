package developmentpermission.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import developmentpermission.entity.key.ApplicationCategoryJudgementKey;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * M_申請区分_区分判定entity
 */

@Entity
@Table(name = "m_application_category_judgement")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
@IdClass(value = ApplicationCategoryJudgementKey.class)
public class ApplicationCategoryJudgement implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** 判定項目ID */
	@Id
	@Column(name = "judgement_item_id")
	private String judgementItemId;

	/** 画面ID */
	@Id
	@Column(name = "view_id")
	private String viewId;

	/** 申請区分ID */
	@Id
	@Column(name = "category_id")
	private String categoryId;
}
