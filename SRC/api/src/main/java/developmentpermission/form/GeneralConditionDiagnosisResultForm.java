package developmentpermission.form;

import java.io.Serializable;
import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 概況診断結果フォーム
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
public class GeneralConditionDiagnosisResultForm implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** 判定結果項目ID */
	@ApiModelProperty(value = "判定結果項目ID", example = "1")
	private Integer judgeResultItemId;
	
	/** 区分判定ID */
	@ApiModelProperty(value = "区分判定ID", example = "0001")
	private String judgementId;
	
	/** 複数行の区分判定結果のインデックス */
	@ApiModelProperty(value = "複数行の区分判定結果のインデックス", example = "1")
	private Integer judgementResultIndex;

	/** 申請種類ID */
	@ApiModelProperty(value = "申請種類ID", example = "1")
	private Integer applicationTypeId;

	/** 申請段階ID */
	@ApiModelProperty(value = "申請段階ID", example = "1")
	private Integer applicationStepId;

	/** 部署ID */
	@ApiModelProperty(value = "部署ID", example = "10001")
	private String departmentId;

	/** タイトル */
	@ApiModelProperty(value = "タイトル", example = "○○市生活環境保全条例（観光")
	private String title;

	/** 結果 */
	@ApiModelProperty(value = "結果", example = "true")
	private Boolean result;

	/** 概要 */
	@ApiModelProperty(value = "概要", example = "○生活環境保護条例（環境）：対応あり")
	private String summary;

	/** 文言 */
	@ApiModelProperty(value = "文言", example = "○生活環境保護条例（環境）：対応あり 〇〇課に相談")
	private String description;

	/** 判定レイヤ表示有無 */
	@ApiModelProperty(value = "判定レイヤ表示有無", example = "true")
	private Boolean judgementLayerDisplayFlag;

	/** 同時表示レイヤ表示有無 */
	@ApiModelProperty(value = "同時表示レイヤ表示有無", example = "true")
	private Boolean simultameousLayerDisplayFlag;

	/** 概況診断結果ID */
	@ApiModelProperty(value = "概況診断結果ID", example = "1")
	private Integer generalConditionDiagnosisResultId;
	
	/** 回答必須フラグ */
	@ApiModelProperty(value = "回答必須フラグ", example = "true")
	private Boolean answerRequireFlag;
	
	/** デフォルト回答 */
	@ApiModelProperty(value = "デフォルト回答", example="判定結果の通り")
	private String defaultAnswer;
	
	/** レイヤ一覧 */
	@ApiModelProperty(value = "レイヤ一覧")
	private List<LayerForm> layers;
	
	/** 距離 */
	@ApiModelProperty(value = "判定レイヤとの距離")
	private String distance;
	
	/** 回答日数 */
	@ApiModelProperty(value = "回答日数")
	private Integer answerDays;
	
	/** 建物表示フラグ */
	@ApiModelProperty(value = "建物表示フラグ", example = "true")
	private Boolean buildingDisplayFlag;
	
	/** エクステントフラグ */
	@ApiModelProperty(value = "エクステントフラグ")
	private Boolean extentFlag;
	
	/** 最小座標(経度) */
	@ApiModelProperty(value = "最小座標（経度）", example = "135.01")
	private Double minlon;

	/** 最小座標(緯度) */
	@ApiModelProperty(value = "最小座標（緯度）", example = "35.01")
	private Double minlat;

	/** 最大座標(経度) */
	@ApiModelProperty(value = "最大座標（経度）", example = "135.01")
	private Double maxlon;

	/** 最大座標(緯度) */
	@ApiModelProperty(value = "最大座標（緯度）", example = "35.01")
	private Double maxlat;
	
	/** データ種類 */
	@ApiModelProperty(value = "データ種類", example = "0")
	private String dataType;
}
