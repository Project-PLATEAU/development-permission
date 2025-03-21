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
 * 担当問合せ・回答フォーム
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
public class ResponsibleInquiryFrom implements Serializable {
	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;
	
	/** 問合せ一覧 */
	@ApiModelProperty(value = "問合せ一覧")
	private List<ChatSearchResultForm> inquiries;
	
	/** 申請一覧（事前相談、未回答あり） */
	@ApiModelProperty(value = "申請一覧（事前相談、未回答あり）")
	private List<ApplyAnswerSearchResultForm> consultationApplys;
	
	/** 申請一覧（事前協議、未回答あり） */
	@ApiModelProperty(value = "申請一覧（事前協議、未回答あり）")
	private List<ApplyAnswerSearchResultForm> discussionApplys;
	
	/** 申請一覧（事前協議、未回答あり） */
	@ApiModelProperty(value = "申請一覧（事前協議、未回答あり）")
	private List<ApplyAnswerSearchResultForm> permissionApplys;
	
}
