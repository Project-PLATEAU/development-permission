package developmentpermission.entity;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 道路中心位置Entity
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RoadCenterLinePosition {
	/** オブジェクトID */
	@Id
	@Column(name = "object_id")
	private Integer ObjectId;
	
	/** WKT */
	@Column(name = "wkt")
	private String wkt;
}
