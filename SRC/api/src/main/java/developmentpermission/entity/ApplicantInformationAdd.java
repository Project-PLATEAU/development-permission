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
 * O_申請追加情報Entityクラス
 */
@Entity
@Data
@Table(name = "o_applicant_information_add")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ApplicantInformationAdd implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** 申請追加情報ID */
	@Id
	@Column(name = "applicant_id")
	private Integer applicantId;

	/** 申請ID */
	@Column(name = "application_id")
	private Integer applicationId;

	/** 申請段階ID */
	@Column(name = "application_step_id")
	private Integer applicationStepId;

	/** 申請者情報項目ID */
	@Column(name = "applicant_information_item_id")
	private String applicantInformationItemId;

	/** 項目値 */
	@Column(name = "item_value")
	private String itemValue;

	/** 版情報 */
	@Column(name = "version_information ")
	private Integer versionInformation;
}
