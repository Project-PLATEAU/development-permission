package developmentpermission.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * F_申請地番Entityクラス
 */
@Entity
@Data
@Table(name = "f_application_lot_number")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ApplyLotNumber {

	/** 申請 申請ID */
	@Id
	@Column(name = "application_id")
	private Integer applicationId;

	/** 地番一覧 */
	@Column(name = "lot_numbers")
	private String lotNumbers;

	/** 経度 */
	@Column(name = "lon")
	private Double lon;

	/** 緯度 */
	@Column(name = "lat")
	private Double lat;

	/** 最小経度 */
	@Column(name = "minlon")
	private Double minlon;

	/** 最小緯度 */
	@Column(name = "minlat")
	private Double minlat;
	
	/** 最大経度 */
	@Column(name = "maxlon")
	private Double maxlon;

	/** 最大緯度 */
	@Column(name = "maxlat")
	private Double maxlat;

	/** ステータス */
	@Column(name = "status")
	private String status;

}
