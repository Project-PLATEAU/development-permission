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
 * M_申請情報項目選択肢Entityクラス
 */
@Entity
@Data
@Table(name = "m_applicant_information_item_option")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ApplicantInformationItemOption implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** 申請情報項目選択肢ID */
	@Id
	@Column(name = "applicant_information_item_option_id")
	private String applicantInformationItemOptionId;

	/** 申請者情報項目ID */
	@Column(name = "applicant_information_item_id")
	private String applicantInformationItemId;

	/** 昇順 */
	@Column(name = "display_order")
	private Integer displayOrder;

	/** 選択肢名 */
	@Column(name = "applicant_information_item_option_name")
	private String applicantInformationItemOptionName;

}
