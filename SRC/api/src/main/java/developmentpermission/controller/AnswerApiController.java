package developmentpermission.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import developmentpermission.entity.Answer;
import developmentpermission.form.AnswerConfirmLoginForm;
import developmentpermission.form.AnswerFileForm;
import developmentpermission.form.AnswerForm;
import developmentpermission.form.AnswerNotifyRequestForm;
import developmentpermission.form.ApplyAnswerDetailForm;
import developmentpermission.form.ApplyAnswerForm;
import developmentpermission.form.DepartmentAnswerForm;
import developmentpermission.form.LedgerForm;
import developmentpermission.form.LedgerMasterForm;
import developmentpermission.form.QuoteFileForm;
import developmentpermission.form.ResponseEntityForm;
import developmentpermission.repository.AnswerRepository;
import developmentpermission.service.AnswerService;
import developmentpermission.service.ApplicationService;
import developmentpermission.service.ChatService;
import developmentpermission.service.LedgerService;
import developmentpermission.util.AuthUtil;
import developmentpermission.util.LogUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * 回答APIコントローラ
 */
@Api(tags = "回答")
@RestController
@RequestMapping("/answer")
public class AnswerApiController extends AbstractApiController {

	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(AnswerApiController.class);

	/** 申請Serviceインスタンス */
	@Autowired
	private ApplicationService applicationService;

	/** 回答Serviceインスタンス */
	@Autowired
	private AnswerService answerService;
	
	/** 問合せServiceインスタンス */
	@Autowired
	private ChatService chatService;
	
	/** 帳票Serviceインスタンス */
	@Autowired
	private LedgerService ledgerService;

	/** 回答確認 csvログファイルヘッダー */
	@Value("${app.csv.log.header.answer.confirm}")
	private String[] answerConfirmLogHeader;

	/** 回答確認 csvログファイルパス */
	@Value("${app.csv.log.path.answer.confirm}")
	private String answerConfirmLogPath;

	/** 回答通知 csvログファイルヘッダー */
	@Value("${app.csv.log.header.answer.notification}")
	private String[] answerNotificationLogHeader;

	/** 回答通知 csvログファイルパス */
	@Value("${app.csv.log.path.answer.notification}")
	private String answerNotificationLogPath;

	/** O_回答Repositoryインスタンス */
	@Autowired
	private AnswerRepository answerRepository;

	
	/**
	 * 申請・回答内容確認情報取得(事業者)
	 * 
	 * @param answerConfirmLoginForm ログインID,パスワード
	 * @return ApplyAnswerForm 申請情報詳細
	 */
	@RequestMapping(value = "/confirm/answer", method = RequestMethod.POST)
	@ApiOperation(value = "申請・回答内容確認情報取得", notes = "申請情報詳細を取得する.")
	@ResponseBody
	@ApiResponses(value = { @ApiResponse(code = 400, message = "パラメータ不正", response = ResponseEntityForm.class),
			@ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class),
			@ApiResponse(code = 403, message = "申請情報取得不能", response = ResponseEntityForm.class) })
	public ApplyAnswerForm getApplicationConfirm(
			@ApiParam(required = true, value = "申請者ログインパラメータフォーム") @RequestBody AnswerConfirmLoginForm answerConfirmLoginForm,
			@CookieValue(value = "token", required = false) String token) {
		LOGGER.info("申請・回答内容確認情報取得(事業者) 開始");
		try {
			String id = answerConfirmLoginForm.getLoginId();
			String password = answerConfirmLoginForm.getPassword();

			if (id != null && !"".equals(id) //
					|| password != null && !"".equals(password)) {
				// 申請ID
				Integer applicationId = applicationService.getApplicationIdFromApplicantInfo(answerConfirmLoginForm);
				if (applicationId != null) {
					ApplyAnswerForm applyAnswerForm = applicationService.getApplicationDetail(applicationId, null,
							false);
					
					// 回答確認ログ出力
					if(answerConfirmLoginForm.getOutputLogFlag() != null && answerConfirmLoginForm.getOutputLogFlag()) {
						try {
							// アクセスID
							String accessId = AuthUtil.getAccessId(token);

							// ログデータ編集
							Object[] logData = answerService.editApplicationConfirmLogData(accessId, applicationId);

							LogUtil.writeLogToCsv(answerConfirmLogPath, answerConfirmLogHeader, logData);
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}

					return applyAnswerForm;
				} else {
					LOGGER.warn("申請情報取得不能");
					throw new ResponseStatusException(HttpStatus.FORBIDDEN);
				}
			} else {
				LOGGER.warn("IDまたはパスワードが空またはnull");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}
		} finally {
			LOGGER.info("申請・回答内容確認情報取得(事業者) 終了");
		}
	}

	/**
	 * 回答登録(行政のみ)
	 * 
	 * @param List<AnswerForm> 回答一覧
	 * @return List<AnswerForm> 回答一覧
	 */
	@ResponseStatus(code = HttpStatus.CREATED)
	@RequestMapping(value = "/input", method = RequestMethod.POST)
	@ApiOperation(value = "回答登録(行政)", notes = "回答を登録する")
	@ResponseBody
	@ApiResponses(value = { @ApiResponse(code = 400, message = "パラメータ不正", response = ResponseEntityForm.class),
			@ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class) })
	public ResponseEntityForm registAnswers(
			@ApiParam(required = true, value = "回答情報フォーム一覧") @RequestBody ApplyAnswerDetailForm applyAnswerDetailForm,
			@CookieValue(value = "token", required = false) String token) {
		LOGGER.info("回答登録 開始");
		try {
			String departmentId = AuthUtil.getDepartmentId(token);
			String loginId = AuthUtil.getLoginId(token);
			String userId = AuthUtil.getUserId(token);
			String departmentName = AuthUtil.getDepartmentName(token);
			String accessId = AuthUtil.getAccessId(token);

			if (answerService.validateRegistAnswersParam(applyAnswerDetailForm, departmentId)) {
				answerService.registAnswers(applyAnswerDetailForm, loginId, departmentName, userId, accessId);
			} else {
				// パラメータ不正
				LOGGER.warn("パラメータ不正");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}
			return new ResponseEntityForm(HttpStatus.CREATED.value(), "Application registration successful.");
		} finally {
			LOGGER.info("回答登録 終了");
		}
	}

    /**
     * 同意項目承認否認登録API(事業者のみ)
     * 
     * @param List<AnswerForm> 回答一覧
     * @return ApplyAnswerForm 申請情報詳細
     */
    @RequestMapping(value = "/consent/input", method = RequestMethod.POST)
    @ApiOperation(value = "同意項目承認否認登録API(事業者のみ)", notes = "承認/否認を登録する")
    @ResponseBody
    @ApiResponses(value = { @ApiResponse(code = 400, message = "パラメータ不正", response = ResponseEntityForm.class),
            @ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class) })
    public ApplyAnswerForm registConsent(
            @ApiParam(required = true, value = "回答情報フォーム一覧") @RequestBody List<AnswerForm> answerFormList,
            @CookieValue(value = "token", required = false) String token) {
        LOGGER.info("同意項目承認否認登録 開始");
        try {
            // バリデーションチェック
            if (answerService.validateRegistConsentParam(answerFormList)) {
            	// 同意項目承認否認登録
				answerService.registConsent(answerFormList, AuthUtil.getAccessId(token));
            } else {
                // パラメータ不正
                LOGGER.warn("パラメータ不正");
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            }
            
            // applicationId取得
            List<Answer> answerList = answerRepository.findByAnswerId(answerFormList.get(0).getAnswerId());
            if (answerList.size() != 1) {
                LOGGER.error("回答情報の取得に失敗");
                throw new RuntimeException("回答情報の取得に失敗");
            }
            Answer answer = answerList.get(0);
            Integer applicationId = answer.getApplicationId();
            
            // ApplyAnswerForm取得
			ApplyAnswerForm applyAnswerForm = applicationService.getApplicationDetail(applicationId, null,
					false);
            /*戻り値の作成*/
            return applyAnswerForm;
            
        } finally {
            LOGGER.info("同意項目承認否認登録 終了");
        }
    }
	
    /**
	 * 回答通知
	 * 
	 * @param ApplyAnswerForm 申請情報詳細
	 */
	@RequestMapping(value = "/notification", method = RequestMethod.POST)
	@ApiOperation(value = "回答通知(行政)", notes = "申請者に回答を通知する.")
	@ResponseBody
	@ApiResponses(value = { @ApiResponse(code = 400, message = "パラメータ不正", response = ResponseEntityForm.class),
			@ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class),
			@ApiResponse(code = 403, message = "権限不正", response = ResponseEntityForm.class),
			@ApiResponse(code = 409, message = "申請ステータスが回答通知不可の状態", response = ResponseEntityForm.class) })
	public void answerNotification(
			@ApiParam(required = true, value = "申請・回答内容確認情報フォーム") @RequestBody AnswerNotifyRequestForm answerNotifyRequestForm,
			@CookieValue(value = "token", required = false) String token) {
		LOGGER.info("回答通知 開始");
		try {
			// 回答通知権限チェック
			String departmentId = AuthUtil.getDepartmentId(token);
			String userId = AuthUtil.getUserId(token);
			String loginId = AuthUtil.getLoginId(token);
			String departmentName = AuthUtil.getDepartmentName(token);
			String accessId = AuthUtil.getAccessId(token);

			if (departmentId == null || "".equals(departmentId) || userId == null || "".equals(userId)) {
				LOGGER.warn("パラメータ不正");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}

			// 申請ID
			if (answerNotifyRequestForm.getApplicationId() == null) {
				LOGGER.warn("パラメータ不正");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}
			
			// 申請段階ID
			if (answerNotifyRequestForm.getApplicationStepId() == null) {
				LOGGER.warn("パラメータ不正");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}

			if (!answerService.checkNotifyAuthority(userId, answerNotifyRequestForm.getApplicationStepId(),
					answerNotifyRequestForm.getNotifyType(), answerNotifyRequestForm.getAnswers())) {
				// 権限不正
				LOGGER.warn("権限不正");
				throw new ResponseStatusException(HttpStatus.FORBIDDEN);
			}

			// 回答通知・ステータス更新
			answerService.notify(answerNotifyRequestForm);

			// 回答通知ログ出力
			try {
				Object[] logData = answerService.editAnswerNotificationLogData(answerNotifyRequestForm, accessId,
						loginId, departmentName);
				LogUtil.writeLogToCsv(answerNotificationLogPath, answerNotificationLogHeader, logData);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} finally {
			LOGGER.info("回答登録 終了");
		}
	}

	/**
	 * 回答ファイルアップロード
	 * 
	 * @param AnswerFileForm O_回答ファイルファイルフォーム
	 * @return
	 */
	@RequestMapping(value = "/file/upload", method = RequestMethod.POST, consumes = { "multipart/form-data" })
	@ApiOperation(value = "回答ファイルアップロード(行政)", notes = "回答ファイルをアップロードする.")
	@ResponseBody
	@ApiResponses(value = { @ApiResponse(code = 400, message = "パラメータ不正", response = ResponseEntityForm.class),
			@ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class) })
	public ResponseEntityForm uploadAnswerFile(
			@ApiParam(required = true, value = "回答ファイルフォーム[multipart/form-data]") @ModelAttribute AnswerFileForm answerFileForm,
			@CookieValue(value = "token", required = false) String token) {
		LOGGER.info("回答ファイルアップロード 開始");
		try {
			String departmentId = AuthUtil.getDepartmentId(token);
			if (answerService.validateUploadAnswerFile(answerFileForm, departmentId)) {
				final String userId = AuthUtil.getUserId(token);
				answerService.uploadAnswerFile(answerFileForm, userId);
				ResponseEntityForm responseEntityForm = new ResponseEntityForm(HttpStatus.CREATED.value(),
						"Answer File registration successful.");
				return responseEntityForm;
			} else {
				// パラメータ不正
				LOGGER.warn("パラメータ不正");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}
		} finally {
			LOGGER.info("回答ファイルアップロード 終了");
		}
	}

	/**
	 * 回答ファイル（引用）アップロード
	 * 
	 * @param AnswerFileForm O_回答ファイルファイルフォーム
	 * @return
	 */
	@RequestMapping(value = "/quote/upload", method = RequestMethod.POST, consumes = { "multipart/form-data" })
	@ApiOperation(value = "回答ファイル（引用）アップロード(行政)", notes = "引用ファイルを回答ファイルとしてアップロードする.")
	@ResponseBody
	@ApiResponses(value = { @ApiResponse(code = 400, message = "パラメータ不正", response = ResponseEntityForm.class),
			@ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class) })
	public ResponseEntityForm uploadQuoteFile(
			@ApiParam(required = true, value = "回答ファイル（引用）フォーム[multipart/form-data]") @ModelAttribute QuoteFileForm quoteFileForm,
			@CookieValue(value = "token", required = false) String token) {
		LOGGER.info("回答ファイル（引用）アップロード 開始");
		try {
			String departmentId = AuthUtil.getDepartmentId(token);
			// バリエーションチェック
			if (answerService.validateuploadQuoteFile(quoteFileForm, departmentId)) {
				final String userId = AuthUtil.getUserId(token);
				answerService.uploadQuoteFile(quoteFileForm, userId);
				ResponseEntityForm responseEntityForm = new ResponseEntityForm(HttpStatus.CREATED.value(),
						"Answer File registration successful.");
				return responseEntityForm;
			} else {
				// パラメータ不正
				LOGGER.warn("パラメータ不正");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}
		} finally {
			LOGGER.info("回答ファイル（引用）アップロード 終了");
		}
	}

	/**
	 * 回答ファイルダウンロード
	 * 
	 * @param AnswerFileForm O_回答ファイルファイルフォーム
	 * @return 応答Entity
	 */
	@RequestMapping(value = "/file/download", method = RequestMethod.POST)
	@ApiOperation(value = "回答ファイルダウンロード", notes = "回答ファイルをダウンロードする.")
	@ResponseBody
	@ApiResponses(value = { @ApiResponse(code = 400, message = "パラメータ不正", response = ResponseEntityForm.class),
			@ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class),
			@ApiResponse(code = 403, message = "ロール不適合", response = ResponseEntityForm.class),
			@ApiResponse(code = 404, message = "ファイルが存在しない場合", response = ResponseEntityForm.class) })
	public ResponseEntity<Resource> downloadAnswerFile(
			@ApiParam(required = true, value = "回答ファイルフォーム") @RequestBody AnswerFileForm answerFileForm,
			@CookieValue(value = "token", required = false) String token) {
		LOGGER.info("回答ファイルダウンロード 開始");
		try {

			String role = AuthUtil.getRole(token);
			if (AuthUtil.ROLE_BUSINESS.equals(role)) {
				String id = answerFileForm.getLoginId();
				String password = answerFileForm.getPassword();
				if (id != null && !"".equals(id) //
						&& password != null && !"".equals(password)) {
					AnswerConfirmLoginForm answerConfirmLoginForm = new AnswerConfirmLoginForm(id, password, false );
					// 申請ID
					Integer applicationId = applicationService
							.getApplicationIdFromApplicantInfo(answerConfirmLoginForm);
					if (applicationId == null || !applicationId.equals(answerFileForm.getApplicationId())) {
						// 照合IDとパスワードによる認証失敗
						LOGGER.warn("回答ファイルダウンロードでの照合IDとパスワードによる認証失敗：" + role);
						throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
					}

				} else {
					// パラメータ不正
					LOGGER.warn("パラメータ不正");
					throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
				}
			} else if (!AuthUtil.ROLE_GOVERMENT.equals(role)) {
				LOGGER.warn("不正なroleによる認証：" + role);
				throw new ResponseStatusException(HttpStatus.FORBIDDEN);
			}

			if (answerService.validateDownloadAnswerFile(answerFileForm)) {
				return answerService.downloadAnswerFile(answerFileForm, role);
			} else {
				// パラメータ不正
				LOGGER.warn("パラメータ不正");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}
		} finally {
			LOGGER.info("回答ファイルダウンロード 終了");
		}
	}

	/**
	 * 回答ファイル論理削除
	 * 
	 * @param AnswerFileForm O_回答ファイルファイルフォーム
	 * @return 応答Entity
	 */
	@RequestMapping(value = "/file/delete", method = RequestMethod.POST)
	@ApiOperation(value = "回答ファイル論理削除(行政)", notes = "回答ファイルを論理削除する.")
	@ResponseBody
	@ApiResponses(value = { @ApiResponse(code = 400, message = "パラメータ不正", response = ResponseEntityForm.class),
			@ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class),
			@ApiResponse(code = 404, message = "ファイルが存在しない場合", response = ResponseEntityForm.class) })
	public ResponseEntityForm deleteAnswerFile(
			@ApiParam(required = true, value = "回答ファイルフォーム") @RequestBody AnswerFileForm answerFileForm,
			@CookieValue(value = "token", required = false) String token) {
		LOGGER.info("回答ファイル論理削除 開始");
		try {
			final String userId = AuthUtil.getUserId(token);
			answerService.deleteAnswerFile(answerFileForm, userId);
			ResponseEntityForm responseEntityForm = new ResponseEntityForm(HttpStatus.OK.value(),
					"Answer File Delete successful.");
			return responseEntityForm;
		} finally {
			LOGGER.info("回答ファイル論理削除 終了");
		}
	}

	/**
	 * 回答レポート出力
	 * 
	 * @param applicationId 申請ID
	 * @return 応答Entity
	 */
	@RequestMapping(value = "/report/{application_id}/{application_step_id}", method = RequestMethod.POST)
	@ApiOperation(value = "回答レポート出力(事業者)", notes = "回答レポートを出力する.")
	@ResponseBody
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "パラメータ不正", response = ResponseEntityForm.class),
			@ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class),
			@ApiResponse(code = 503, message = "帳票作成に失敗した場合", response = ResponseEntityForm.class) })
	public void exportAnswerReport(
			@ApiParam(required = true, value = "申請ID") @PathVariable(value = "application_id") Integer applicationId , 
			@ApiParam(required = true, value = "申請段階ID") @PathVariable(value = "application_step_id") Integer applicationStepId,
			@ApiParam(required = true, value = "申請者ログインパラメータフォーム") @RequestBody AnswerConfirmLoginForm answerConfirmLoginForm,
			@CookieValue(value = "token", required = false) String token, HttpServletResponse response) {
	
		LOGGER.info("回答レポート出力 開始");
		try {
			String id = answerConfirmLoginForm.getLoginId();
			String password = answerConfirmLoginForm.getPassword();
			
            // パラメータチェック
			if (applicationId != null && applicationStepId != null && id != null && password != null) {
				// 申請ID
				Integer baseApplicationId = applicationService
						.getApplicationIdFromApplicantInfo(answerConfirmLoginForm);
				if (baseApplicationId == null || !baseApplicationId.equals(applicationId)) {
					// 照合IDとパスワードによる認証失敗
					LOGGER.warn("回答レポート出力での照合IDとパスワードによる認証失敗");
					throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
				}

				if (!answerService.exportAnswerReportWorkBook(applicationId, applicationStepId, response)) {
					LOGGER.warn("帳票生成失敗");
					throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
				}
			} else {
				LOGGER.warn("パラメータ不正");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}
		} finally {
			LOGGER.info("回答レポート出力 終了");
		}
	}

	/**
	 * 協議対象一覧取得
	 * 
	 * @param applicationStepId 申請段階ID
	 * @return 応答Entity
	 */
	@RequestMapping(value = "/ledger/{applicationStep_id}", method = RequestMethod.GET)
	@ApiOperation(value = "協議対象一覧取得(行政)", notes = "協議対象一覧を取得する.")
	@ResponseBody
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class),
			@ApiResponse(code = 403, message = "ロール不適合", response = ResponseEntityForm.class),
			@ApiResponse(code = 503, message = "処理エラー", response = ResponseEntityForm.class) })
	public List<LedgerMasterForm> getLedgerList(
			@ApiParam(required = true, value = "申請段階ID") @PathVariable(value = "applicationStep_id") Integer applicationStepId,
			@CookieValue(value = "token", required = false) String token, HttpServletResponse response) {
		LOGGER.info("協議対象一覧取得(行政) 開始");
		try {
			// 権限チェック（ロールチェック）
			LOGGER.info("権限チェック（ロールチェック） 開始");
			String role = AuthUtil.getRole(token);
			if (!AuthUtil.ROLE_GOVERMENT.equals(role) && !AuthUtil.ROLE_BUSINESS.equals(role)) {
				LOGGER.warn("ロール不適合:" + role);
				throw new ResponseStatusException(HttpStatus.FORBIDDEN);
			}
			LOGGER.info("権限チェック（ロールチェック） 終了");
			try {
				List<LedgerMasterForm> formList = new ArrayList<LedgerMasterForm>();
				// 画面に入力必要な協議対象リスト取得
				LOGGER.debug("協議対象一覧取得 開始");
				formList = applicationService.getLedgerList(applicationStepId);
				LOGGER.debug("協議対象一覧取得 終了");
				return formList;
			} catch (Exception ex) {
				LOGGER.error("協議対象一覧取得に例外発生", ex);
				throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
			}
		} finally {
			LOGGER.info("協議対象一覧取得(行政) 終了");
		}
	}

	/**
	 * 帳票ファイルダウンロード
	 * 
	 * @param AnswerFileForm O_帳票ファイルファイルフォーム
	 * @return 応答Entity
	 */
	@RequestMapping(value = "/ledger/file/download", method = RequestMethod.POST)
	@ApiOperation(value = "帳票ファイルダウンロード", notes = "帳票ファイルをダウンロードする.")
	@ResponseBody
	@ApiResponses(value = { @ApiResponse(code = 400, message = "パラメータ不正", response = ResponseEntityForm.class),
			@ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class),
			@ApiResponse(code = 403, message = "ロール不適合", response = ResponseEntityForm.class),
			@ApiResponse(code = 404, message = "ファイルが存在しない場合", response = ResponseEntityForm.class) })
	public ResponseEntity<Resource> downloadAnswerFile(
			@ApiParam(required = true, value = "帳票ファイルフォーム") @RequestBody LedgerForm ledgerForm,
			@CookieValue(value = "token", required = false) String token) {
		LOGGER.info("帳票ファイルダウンロード 開始");
		try {

			String role = AuthUtil.getRole(token);
			if (AuthUtil.ROLE_BUSINESS.equals(role)) {
				String id = ledgerForm.getLoginId();
				String password = ledgerForm.getPassword();
				if (id != null && !"".equals(id) //
						&& password != null && !"".equals(password)) {
					AnswerConfirmLoginForm answerConfirmLoginForm = new AnswerConfirmLoginForm(id, password, false);
					// 申請ID
					Integer applicationId = applicationService
							.getApplicationIdFromApplicantInfo(answerConfirmLoginForm);
					if (applicationId == null || !applicationId.equals(ledgerForm.getApplicationId())) {
						// 照合IDとパスワードによる認証失敗
						LOGGER.warn("帳票ファイルダウンロードでの照合IDとパスワードによる認証失敗：" + role);
						throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
					}

				} else {
					// パラメータ不正
					LOGGER.warn("パラメータ不正");
					throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
				}
			} else if (!AuthUtil.ROLE_GOVERMENT.equals(role)) {
				LOGGER.warn("不正なroleによる認証：" + role);
				throw new ResponseStatusException(HttpStatus.FORBIDDEN);
			}

			if (answerService.validateDownloadLedgerFile(ledgerForm)) {
				return answerService.downloadLedgeFile(ledgerForm, role);
			} else {
				// パラメータ不正
				LOGGER.warn("パラメータ不正");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}
		} finally {
			LOGGER.info("帳票ファイルダウンロード 終了");
		}
	}
}
