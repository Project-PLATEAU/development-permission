package developmentpermission.form;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 申請地番フォーム
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
public class ApplyLotNumberForm implements Serializable{

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** 申請ID */
	@ApiModelProperty(value = "申請ID", example = "1")
	private Integer applicationId;
	
	/** 地番一覧 */
	@ApiModelProperty(value = "地番一覧", example = "xx市yy12332,12333,12334")
	private String lot_numbers;

	/** 中心座標(経度) */
	@ApiModelProperty(value = "中心座標（経度）", example = "135.01")
	private Double lon;

	/** 中心座標(緯度) */
	@ApiModelProperty(value = "中心座標（緯度）", example = "35.01")
	private Double lat;

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
	
	/** ステータス */
	@ApiModelProperty(value = "ステータス(0:申請中 1:回答中（未回答課あり）2:回答完了 3:通知済み 4:通知済み（要再申請)", example = "第1版申請中")
	private String status;
}
