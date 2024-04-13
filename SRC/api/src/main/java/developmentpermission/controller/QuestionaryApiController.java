package developmentpermission.controller;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import developmentpermission.form.QuestionaryPurposeForm;
import developmentpermission.form.ResponseEntityForm;
import developmentpermission.util.AuthUtil;
import developmentpermission.util.LogUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * アンケートAPIコントローラ
 */
@Api(tags = "アンケート")
@RestController
@RequestMapping("/questionnaire")
public class QuestionaryApiController extends AbstractApiController {

	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(QuestionaryApiController.class);

	/** アンケートの利用目的保存 csvログファイルパス */
	@Value("${app.csv.log.path.questionnaire.reply}")
	private String questionnaireReplyLogPath;
	
	/** アンケートの利用目的保存 csvログファイルヘッダー */
	@Value("${app.csv.log.header.questionnaire.reply}")
	private String[] questionnaireReplyLogHeader;
	
	/**
	 * 初期画面アンケート
	 * 
	 * @return アンケート回答フォーム
	 * @throws Exception 
	 */
	@RequestMapping(value = "/reply", method = RequestMethod.POST)
	@ApiOperation(value = "アンケート回答", notes = "アクセスIDと利用目的をログに保存する.")
	@ResponseBody
	@ApiResponses( value = { 
					@ApiResponse(code = 200, message = "アンケートの利用目的を正常にログファイルに出力する", response = ResponseEntityForm.class),
					@ApiResponse(code = 400, message = "パラメータ不正", response = ResponseEntityForm.class)})
	 @Retryable( value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000) )
	public ResponseEntityForm postreply(@ApiParam(required = true, value = "アンケート回答フォーム") @RequestBody QuestionaryPurposeForm questionnaireForm,@CookieValue(value = "token", required = false) String token) throws Exception {
		LOGGER.info("アンケートログ登録 開始");
		try {

			// アクセスID
			String accessId = AuthUtil.getAccessId(token);
			if(accessId == null || "".equals(accessId)) {
				LOGGER.warn("アクセスIDがnullまたは空");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}
			
			// 利用目的
			String text = questionnaireForm.getText();
			if(text == null || "".equals(text)) {
				LOGGER.warn("利用目的がnullまたは空");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}
			
			//アンケートログ出力
			try {
				// アクセスID + 利用目的
				Object[] logData = {accessId,text};
				LogUtil.writeLogToCsvWithRetry(questionnaireReplyLogPath, questionnaireReplyLogHeader, logData);
			} catch (Exception ex) {
				ex.printStackTrace();
				throw new Exception(ex);
			}

			ResponseEntityForm responseEntityForm = new ResponseEntityForm(HttpStatus.OK.value(),
					"利用目的をログに出力しました。.");
			return responseEntityForm;
		} finally {
			LOGGER.info("アンケートログ登録 終了");
		}
	}
	
	/**
	 *初期画面アンケートのカスタムログ登録のリトライ処理の フォールバックメソッド
	 * @param e リトライ処理の異常
	 * @return 
	 */
	@Recover
    public ResponseEntityForm recoverPostreply(Exception e) {

        LOGGER.warn("アンケート利用目的のカスタムログ書き込み処理に失敗しました。" );
        ResponseEntityForm responseEntityForm = new ResponseEntityForm(HttpStatus.OK.value(),
				"利用目的をログに出力失敗しました。.");
		return responseEntityForm;
    }
	
	/**
	 * アンケート利用目的一覧取得
	 * 
	 * @return 申請情報検索条件一覧取得
	 */
	@RequestMapping(value = "/search", method = RequestMethod.GET)
	@ApiOperation(value = "アンケート利用目的一覧取得", notes = "アンケートの利用目的一覧を取得する.")
	@ResponseBody
	@ApiResponses(value = { @ApiResponse(code = 503, message = "処理エラー", response = ResponseEntityForm.class)})
	public List<QuestionaryPurposeForm> getQuestionaryPurposes() {
		LOGGER.info("アンケート利用目的一覧取得取得 開始");
		try {
			List<QuestionaryPurposeForm> formList = new ArrayList<QuestionaryPurposeForm>();

			// ステータス一覧（申請情報）
			LOGGER.debug("利用目的一覧取得 開始");
			formList = authenticationService.getQuestionaryPurposeList();
			LOGGER.debug("利用目的一覧取得 終了");

			return formList;
		} catch (Exception ex) {
			LOGGER.error("アンケート利用目的一覧取得時に例外発生", ex);
			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		} finally {
			LOGGER.info("アンケート利用目的一覧取得取得 終了");
		}
	}
}
