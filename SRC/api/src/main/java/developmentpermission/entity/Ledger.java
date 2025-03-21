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
 * O_帳票Entityクラス
 */
@Entity
@Data
@Table(name = "o_ledger")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Ledger implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** ファイルID */
	@Id
	@Column(name = "file_id")
	private Integer fileId;

	/** 申請ID */
	@Column(name = "application_id")
	private Integer applicationId;

	/** 申請段階ID */
	@Column(name = "application_step_id")
	private Integer applicationStepId;

	/** 帳票マスタID */
	@Column(name = "ledger_id")
	private String ledgerId;

	/** ファイル名 */
	@Column(name = "file_name")
	private String fileName;

	/** ファイルパス */
	@Column(name = "file_path")
	private String filePath;

	/** 作成日時 */
	@Column(name = "register_datetime")
	private LocalDateTime registerDatetime;

	/** 受領日時 */
	@Column(name = "receipt_datetime")
	private LocalDateTime receiptDatetime;
	
	/** 通知フラグ */
	@Column(name = "notify_flag", columnDefinition = "char(1)")
	private Boolean notifyFlag;
	
	/** 通知ファイルパス */
	@Column(name = "notify_file_path")
	private String notifyFilePath;
	
}
