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
 * 区割り線・道路LOD2エクステントEntityクラス.
 *
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SpiltLineExtent implements Serializable{
	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;
	
	/** ID */
	@Id
	@Column(name = "id")
	private Integer id;
	
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
	
}
