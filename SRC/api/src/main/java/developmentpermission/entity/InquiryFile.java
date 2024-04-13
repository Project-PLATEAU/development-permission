package developmentpermission.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

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
 * O_問合せファイルEntityクラス
 */
@Entity
@Data
@Table(name = "o_inquiry_file")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class InquiryFile implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** 問合せファイルID */
	@Id
	@Column(name = "inquiry_file_id")
	private Integer inquiryFileId;

	/** メッセージID */
	@Column(name = "message_id")
	private Integer messageId;

	/** ファイル名 */
	@Column(name = "file_name")
	private String fileName;

	/** ファイルパス */
	@Column(name = "file_path")
	private String filePath;
	
	/** 登録日時 */
	@Column(name = "register_datetime")
	private LocalDateTime registerDatetime;

}
