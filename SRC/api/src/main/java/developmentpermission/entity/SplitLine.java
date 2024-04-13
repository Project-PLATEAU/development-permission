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
 * F_区割り線Entityクラス
 */
@Entity
@Data
@Table(name = "f_split_line")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SplitLine implements Serializable{
	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;
	
	/** オブジェクトID */
	@Id
	@Column(name = "object_id")
	private Integer objectId;
	
	/** 道路部幅員 */
	@Column(name = "road_width")
	private Double roadWidth;
	
	/** 車道幅員 */
	@Column(name = "roadway_width")
	private Double roadwayWidth; 
}
