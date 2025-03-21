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
 * M_区分判定Entityクラス.
 *
 *
 */
@Entity
@Data
@Table(name = "m_category_judgement")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CategoryJudgement implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** 判定項目ID */
	@Id
	@Column(name = "judgement_item_id")
	private String judgementItemId;

	/** GIS判定 */
	@Column(name = "gis_judgement")
	private String gisJudgement;

	/** バッファ */
	@Column(name = "buffer")
	private Double buffer;

	/** 重なり属性表示フラグ */
	@Column(name = "display_attribute_flag")
	private String displayAttributeFlag;

	/** 判定対象レイヤ */
	@Column(name = "judgement_layer")
	private String judgementLayer;
	
	/** テーブル名 */
	@Column(name = "table_name")
	private String tableName;

	/** フィールド名 */
	@Column(name = "field_name")
	private String fieldName;

	/** 判定レイヤ非該当時表示有無 */
	@Column(name = "non_applicable_layer_display_flag", columnDefinition = "char(1)")
	private Boolean nonApplicableLayerDisplayFlag;

	/** 同時表示レイヤ */
	@Column(name = "simultaneous_display_layer")
	private String simultaneousDisplayLayer;

	/** 同時表示レイヤ表示有無 */
	@Column(name = "simultaneous_display_layer_flag", columnDefinition = "char(1)")
	private Boolean simultaneousDisplayLayerFlag;
}
