package developmentpermission.form;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * チャットリクエストフォーム
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
public class ChatRequestForm implements Serializable {
	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** 回答 */
	@ApiModelProperty(value = "回答")
	private AnswerForm answer;
	/** ログインID */
	@ApiModelProperty(value = "ログインID", example = "00111223344")
	private String loginId;
	/** パスワード */
	@ApiModelProperty(value = "パスワード", example = "password")
	private String password;
}
