package developmentpermission.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * M_行政ユーザ&M_権限&M_部署Entiryクラス
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GovernmentUserAndAuthority implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** ユーザID */
	@Id
	@Column(name = "user_id")
	private String userId;

	/** ログインID */
	@Column(name = "login_id")
	private String loginId;

	/** パスワード */
	@Column(name = "password")
	private String password;

	/** ロールコード */
	@Column(name = "role_code")
	private String roleCode;

	/** 部署ID */
	@Column(name = "department_id")
	private String departmentId;

	/** 氏名 */
	@Column(name = "user_name")
	private String userName;

	/** 管理者フラグ(0:一般ユーザ、1:管理者) */
	@Column(name = "admin_flag", columnDefinition = "char(1)")
	private Boolean adminFlag;

	/** M_部署.部署名 */
	@Column(name = "department_name")
	private String departmentName;

	/** M_部署.回答権限フラグ(0:その他、1=統括部署) */
	@Column(name = "management_department_flag", columnDefinition = "char(1)")
	private Boolean managementDepartmentFlag;

	/** M_部署.メールアドレス */
	@Column(name = "mail_address")
	private String mailAddress;

	/** M_部署.管理者メールアドレス */
	@Column(name = "admin_mail_address")
	private String adminMailAddress;

	/** M_権限.申請段階ID */
	@Id
	@Column(name = "application_step_id")
	private Integer applicationStepId;

	/** M_権限.回答権限フラグ(0: 権限なし 1: 権限あり（所属部署のみ操作可）、2：権限あり（他部署も操作可）) */
	@Column(name = "answer_authority_flag", columnDefinition = "char(1)")
	private String answerAuthorityFlag;

	/** M_権限.通知権限フラグ(0: 権限なし 1: 権限あり（所属部署のみ操作可）、2：権限あり（他部署も操作可）) */
	@Column(name = "notification_authority_flag", columnDefinition = "char(1)")
	private String notificationAuthorityFlag;

}
