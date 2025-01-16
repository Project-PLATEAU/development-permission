package developmentpermission.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import developmentpermission.entity.key.CategoryJudgementAuthorityKey;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * M_区分判定_権限Entityクラス.
 *
 *
 */
@Entity
@Data
@Table(name = "m_judgement_authority")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@IdClass(value = CategoryJudgementAuthorityKey.class)
public class CategoryJudgementAuthority implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** 判定項目ID */
	@Id
	@Column(name = "judgement_item_id")
	private String judgementItemId;

	/** 部署ID */
	@Id
	@Column(name = "department_id")
	private String departmentId;
}
