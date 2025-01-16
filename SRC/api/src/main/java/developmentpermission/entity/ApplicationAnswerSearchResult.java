package developmentpermission.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

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
 * O_申請Entityクラス
 */
@Entity
@Data
@Table(name = "o_application")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ApplicationAnswerSearchResult implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** 申請ID */
	@Id
	@Column(name = "application_id")
	private Integer applicationId;

	/** ステータス */
	@Column(name = "status")
	private String status;

	// TODO DB定義変更で廃止となるため、削除予定
	/** 版情報 */
	@Column(name = "version_information")
	private Integer versionInformation;

	/** 期限 */
	@Column(name = "deadline_date")
	private String deadlineDate;
	
	/** 警告フラグ */
	@Column(name = "warning")
	private Boolean warning;
}
