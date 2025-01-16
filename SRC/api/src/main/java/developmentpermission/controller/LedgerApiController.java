package developmentpermission.controller;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import developmentpermission.form.LabelForm;
import developmentpermission.form.LedgerForm;
import developmentpermission.form.ResponseEntityForm;
import developmentpermission.service.LedgerService;
import developmentpermission.util.AuthUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * 
 * 帳票系API Controllerクラス.
 *
 */
@Api(tags = "帳票")
@RestController
@RequestMapping("/ledger")
public class LedgerApiController extends AbstractApiController {

	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(LedgerApiController.class);

	/** 帳票Serviceインスタンス */
	@Autowired
	private LedgerService ledgerService;
	
	/**
	 * 指定した画面のラベル一覧取得
	 * 
	 * @param viewCode 画面コード
	 * @return ラベル一覧
	 */
	@RequestMapping(value = "/test", method = RequestMethod.GET)
	@ApiOperation(value = "指定した画面のラベル一覧取得", notes = "指定した画面のラベル一覧をJSON形式で取得する.")
	@ResponseBody
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "パラメータ不正", response = ResponseEntityForm.class),
			@ApiResponse(code = 403, message = "ロール不適合", response = ResponseEntityForm.class) })
	public void getLabel(
			) {
		LOGGER.info("ラベル一覧取得 開始");
		try {
			ledgerService.exportLedger(84, 3);
		} catch(Exception e) {
			
		} finally {
			LOGGER.info("ラベル一覧取得 終了");
		}
	}
	/**
	 * 帳票アップロード
	 * 
	 * @param LedgerForm O_帳票ファイルフォーム
	 * @return
	 */
	@RequestMapping(value = "/file/upload", method = RequestMethod.POST, consumes = { "multipart/form-data" })
	@ApiOperation(value = "帳票アップロード", notes = "帳票をアップロードする.")
	@ResponseBody
	@ApiResponses(value = { @ApiResponse(code = 400, message = "パラメータ不正", response = ResponseEntityForm.class),
			@ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class),
			@ApiResponse(code = 403, message = "ロール不適合", response = ResponseEntityForm.class) })
	public ResponseEntityForm uploadLedgerFile(
			/* @RequestBody // MultipartFileを含む場合、RequestBodyでは415エラーで弾かれる・・・？ */
			@ApiParam(required = true, value = "帳票フォーム[multipart/form-data]") @ModelAttribute LedgerForm ledgerForm,
			@CookieValue(value = "token", required = false) String token) {
		LOGGER.info("帳票アップロード 開始");
		try {
			String role = AuthUtil.getRole(token);
			// 行政担当者のみアップロード可能
			if (AuthUtil.ROLE_GOVERMENT.equals(role)) {
				// fileIdからアップロード対象のファイルをチェック
				// 帳票ファイルを所定の場所に格納する
				// O_帳票.ファイルパスを更新
				// ファイル出力
				
				// 帳票アップロード
				ledgerService.uploadLedgerFile(ledgerForm);

				ResponseEntityForm responseEntityForm = new ResponseEntityForm(HttpStatus.CREATED.value(),
						"Application File registration successful.");
				return responseEntityForm;
			} else {
				// 権限不正
				LOGGER.warn("ロール不適合: " + role);
				throw new ResponseStatusException(HttpStatus.FORBIDDEN);
			}

		} finally {
			LOGGER.info("帳票アップロード 終了");
		}
	}
	
	/**
	 * 帳票を事業者に通知する
	 * 
	 * @param LedgerForm O_帳票ファイルフォーム
	 * @return
	 */
	@RequestMapping(value = "/notify", method = RequestMethod.POST)
	@ApiOperation(value = "帳票通知", notes = "帳票を事業者に通知する.")
	@ResponseBody
	@ApiResponses(value = { @ApiResponse(code = 400, message = "パラメータ不正", response = ResponseEntityForm.class),
			@ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class),
			@ApiResponse(code = 403, message = "ロール不適合", response = ResponseEntityForm.class) })
	public void notifiLedgerFile(
			@ApiParam(required = true, value = "帳票フォーム") @RequestBody LedgerForm ledgerForm,
			@CookieValue(value = "token", required = false) String token) {
		LOGGER.info("帳票通知 開始");
		try {
			String role = AuthUtil.getRole(token);
			// 行政担当者のみアクセス可能
			if (AuthUtil.ROLE_GOVERMENT.equals(role)) {
				// fileIdから通知対象の帳票をチェック
				// O_帳票.通知フラグ、O_帳票.通知ファイルパスを更新
				
				// 帳票通知
				ledgerService.notifyLedgerFile(ledgerForm);
				
			} else {
				// 権限不正
				LOGGER.warn("ロール不適合: " + role);
				throw new ResponseStatusException(HttpStatus.FORBIDDEN);
			}

		} finally {
			LOGGER.info("帳票通知 終了");
		}
	}

}
