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
 * O_回答ファイルEntityクラス
 */
@Entity
@Data
@Table(name = "m_road_judge_label")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RoadJudgeLabel implements Serializable{
	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;
	
	/** ラベルID */
	@Id
	@Column(name = "label_id")
	private Integer labelId;
	
	/** 置換識別子 */
	@Column(name = "replace_identify")
	private String replaceIdentify;
	
	/** インデックス値 */
	@Column(name = "index_value")
	private Integer indexValue;
	
	/** 最小値 */
	@Column(name = "min_value")
	private Double minValue;
	
	/** 最大値 */
	@Column(name = "max_value")
	private Double maxValue;
	
	/** 置換テキスト */
	@Column(name = "replace_text")
	private String replaceText;
	
	/** インデックス文字列 */
	@Column(name = "index_text")
	private String indexText;
}
