package developmentpermission.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import developmentpermission.entity.key.ApplicationVersionInformationKey;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * O_申請版情報Entityクラス
 */
@Entity
@Data
@Table(name = "o_application_version_information")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@IdClass(value = ApplicationVersionInformationKey.class)
public class ApplicationVersionInformation implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** 申請ID */
	@Id
	@Column(name = "application_id")
	private Integer applicationId;

	/** 申請段階ID */
	@Id
	@Column(name = "application_step_id")
	private Integer applicationStepId;

	/** 版情報 */
	@Column(name = "version_information")
	private Integer versionInformation;

	/** 受付フラグ(事前協議のみ、1=受付 0=未確認 2=差戻) */
	@Column(name = "accepting_flag", columnDefinition = "char(1)")
	private String acceptingFlag;

	/** 受付版情報(事前協議のみ) */
	@Column(name = "accept_version_information")
	private Integer acceptVersionInformation;

	/** 登録日時 */
	@Column(name = "register_datetime")
	private LocalDateTime registerDatetime;

	/** 更新日時 */
	@Column(name = "update_datetime ")
	private LocalDateTime updateDatetime;
	
	/** 完了日時 */
	@Column(name = "complete_datetime ")
	private LocalDateTime completeDatetime;

	/** 登録ステータス */
	@Column(name = "register_status")
	private String registerStatus;
}
