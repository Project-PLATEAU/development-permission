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
 * M_帳票Entityクラス
 */
@Entity
@Data
@Table(name = "m_ledger")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class LedgerMaster implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** 帳票マスタID */
	@Id
	@Column(name = "ledger_id")
	private String ledgerId;

	/** 申請段階ID */
	@Column(name = "application_step_id")
	private Integer applicationStepId;

	/** 帳票名（帳票ファイルの名称） */
	@Column(name = "ledger_name")
	private String ledgerName;

	/** 画面表示名(出力種類が１の場合のみ) */
	@Column(name = "display_name")
	private String displayName;

	/** テンプレートパス */
	@Column(name = "template_path")
	private String templatePath;

	/** 出力種類(0:常に出力、1：画面に選択されたレコードがあれば出力) */
	@Column(name = "output_type")
	private String outputType;

	/** 受領時通知要否（0:通知不要、1:通知必要） */
	@Column(name = "notification_flag", columnDefinition = "char(1)")
	private Boolean notificationFlag;

	/** 帳票種類 */
	@Column(name = "ledger_type")
	private String ledgerType;

	/** 更新フラグ */
	@Column(name = "update_flag", columnDefinition = "char(1)")
	private Boolean updateFlag;

	/** 通知フラグ */
	@Column(name = "notify_flag", columnDefinition = "char(1)")
	private Boolean notifyFlag;

	/** アップロード時拡張子 */
	@Column(name = "upload_extension")
	private String uploadExtension;

	/** 案内テキスト */
	@Column(name = "information_text")
	private String informationText;

}
