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
 * F_道路LOD2Entityクラス
 */
@Entity
@Data
@Table(name = "f_road_lod2")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RoadLod2  implements Serializable{
	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;
	
	/** オブジェクトID */
	@Id
	@Column(name = "object_id")
	private Integer objectId;
	
	/** 幅員 */
	@Column(name = "width")
	private Double width;
	
	/** 路線番号 */
	@Column(name = "line_number")
	private String lineNumber;
	
	/** 道路種別(function) */
	@Column(name = "function")
	private String roadType;
}
