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
 * O_問合せ宛先Entityクラス
 */
@Entity
@Data
@Table(name = "o_inquiry_address")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class InquiryAddress implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** 問合せ宛先ID */
	@Id
	@Column(name = "inquiry_address_id")
	private Integer inquiryAddressId;

	/** メッセージID */
	@Column(name = "message_id")
	private Integer messageId;

	/** 部署ID */
	@Column(name = "department_id")
	private String departmentId;

	/** 既読フラグ */
	@Column(name = "read_flag", columnDefinition = "char(1)")
	private Boolean readFlag;
	
	/**回答済みフラグ */
	@Column(name = "answer_complete_flag", columnDefinition = "char(1)")
	private Boolean answerCompleteFlag;
}
