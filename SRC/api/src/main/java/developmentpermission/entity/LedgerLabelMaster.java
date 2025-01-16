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
 * 
 * M_帳票ラベルEntityクラス
 *
 */
@Entity
@Data
@Table(name = "m_ledger_label")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class LedgerLabelMaster implements Serializable {
	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** 帳票ラベルID */
	@Id
	@Column(name = "ledger_label_id")
	private String ledgerLabelId;

	/** 帳票マスタID */
	@Column(name = "ledger_id")
	private String ledgerId;

	/** 置換識別子 */
	@Column(name = "replace_identify")
	private String replaceIdentify;

	/** テーブル名 */
	@Column(name = "table_name")
	private String tableName;

	/** 出力カラム名 */
	@Column(name = "export_column_name")
	private String exportColumnName;

	/** フィルタカラム名 */
	@Column(name = "filter_column_name")
	private String filterColumnName;

	/** フィルタ条件 */
	@Column(name = "filter_condition")
	private String filterCondition;

	/** 項目ID1 */
	@Column(name = "item_id_1")
	private String itemId1;

	/** 項目ID2 */
	@Column(name = "item_id_2")
	private String itemId2;

	/** 変換オーダ */
	@Column(name = "convert_order")
	private String convertOrder;

	/** 変換フォーマット */
	@Column(name = "convert_format")
	private String convertFormat;

}
