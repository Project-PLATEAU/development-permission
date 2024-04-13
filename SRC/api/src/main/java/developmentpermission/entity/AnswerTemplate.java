package developmentpermission.entity;


import java.io.Serializable;
import java.time.LocalDateTime;

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
 * M_回答テンプレートEntityクラス
 */
@Entity
@Data
@Table(name = "m_answer_template")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AnswerTemplate implements Serializable{
	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;
	
	/** 回答テンプレートID */
	@Id
	@Column(name = "answer_template_id")
	private Integer answerTemplateId;
	
	/** 表示順 */
	@Column(name = "disp_order")
	private Integer dispOrder;
	
	/** 回答テンプレートテキスト */
	@Column(name = "answer_template_text")
	private String answerTemplateText;
	
	/** 判定項目ID */
	@Column(name = "judgement_item_id")
	private String judgementItemId;
}
