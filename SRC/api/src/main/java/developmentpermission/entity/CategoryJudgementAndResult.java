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
 * M_区分判定、M_判定結果Entityクラス
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CategoryJudgementAndResult implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** 判定項目ID */
	@Id
	@Column(name = "judgement_item_id")
	private String judgementItemId;

	/** 申請種類ID */
	@Id
	@Column(name = "application_type_id")
	private Integer applicationTypeId;

	/** 申請段階ID */
	@Id
	@Column(name = "application_step_id")
	private Integer applicationStepId;

	/** 部署ID(事前協議以外ではnull) */
	@Id
	@Column(name = "department_id")
	private String departmentId;

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

	/** タイトル */
	@Column(name = "title")
	private String title;

	/** 該当表示概要 */
	@Column(name = "applicable_summary")
	private String applicableSummary;

	/** 該当表示文言 */
	@Column(name = "applicable_description")
	private String applicableDescription;

	/** 非該当表示有無 */
	@Column(name = "non_applicable_display_flag", columnDefinition = "char(1)")
	private Boolean nonApplicableDisplayFlag;

	/** 非該当表示概要 */
	@Column(name = "non_applicable_summary")
	private String nonApplicableSummary;

	/** 非該当表示文言 */
	@Column(name = "non_applicable_description")
	private String nonApplicableDescription;

	/** 回答必須フラグ */
	@Column(name = "answer_require_flag", columnDefinition = "char(1)")
	private Boolean answerRequireFlag;

	/** デフォルト回答 */
	@Column(name = "default_answer")
	private String defaultAnswer;

	/** 編集可能フラグ */
	@Column(name = "answer_editable_flag", columnDefinition = "char(1)")
	private Boolean answerEditableFlag;

	/** 回答日数 */
	@Column(name = "answer_days")
	private Integer answerDays;
}
