package developmentpermission.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import developmentpermission.entity.key.AuthorityKey;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * M_権限Entiryクラス
 */
@Entity
@Data
@Table(name = "m_authority")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@IdClass(value = AuthorityKey.class)
public class Authority implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** 部署ID */
	@Id
	@Column(name = "department_id")
	private String departmentId;

	/** 申請段階ID */
	@Id
	@Column(name = "application_step_id")
	private Integer applicationStepId;

	/** 回答権限フラグ(0: 権限なし 1: 権限あり（所属部署のみ操作可）、2：権限あり（他部署も操作可）) */
	@Column(name = "answer_authority_flag", columnDefinition = "char(1)")
	private String answerAuthorityFlag;

	/** 通知権限フラグ(0: 権限なし 1: 権限あり（所属部署のみ操作可）、2：権限あり（他部署も操作可）) */
	@Column(name = "notification_authority_flag", columnDefinition = "char(1)")
	private String notificationAuthorityFlag;

}
