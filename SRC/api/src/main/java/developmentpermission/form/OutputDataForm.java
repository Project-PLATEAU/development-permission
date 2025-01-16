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
 * CSVデータ出力フォーム
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
public class OutputDataForm implements Serializable {
	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;
	
	/** データタイプ */
	@ApiModelProperty(value = "データタイプ application 申請 inquiry 問合せ")
	private String dataType;
	
	/** ソートカラム */
	@ApiModelProperty(value = "ソートカラム")
	private String sortColumn;
	
	/** ソート方向 */
	@ApiModelProperty(value = "ソート方向 asc 昇順 desc 降順")
	private String sortType;
	
	/** 検索条件 */
	@ApiModelProperty(value = "検索条件")
	private ApplicationSearchConditionForm conditions;
	
	/** 申請情報検索結果 */
	@ApiModelProperty(value = "申請情報検索結果")
	private List<ApplicationSearchResultForm> applicationSearchResults;
	
	/** 問合せ情報検索結果 */
	@ApiModelProperty(value = "問合せ情報検索結果")
	private List<ChatSearchResultForm> chatSearchResults;
}
