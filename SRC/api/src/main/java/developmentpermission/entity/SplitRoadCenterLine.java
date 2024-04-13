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
 * 道路中心線（分割）Entity
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SplitRoadCenterLine {
	/** ID */
	@Id
	@Column(name = "id")
	private Integer id;
	
	/** WKT */
	@Column(name = "wkt")
	private String wkt;
}
