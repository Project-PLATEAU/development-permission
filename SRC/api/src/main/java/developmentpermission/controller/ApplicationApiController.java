package developmentpermission.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

import developmentpermission.form.AnswerConfirmLoginForm;
import developmentpermission.form.ApplicantInformationItemForm;
import developmentpermission.form.ApplicationCategorySelectionViewForm;
import developmentpermission.form.ApplicationFileForm;
import developmentpermission.form.ApplicationInformationSearchResultHeaderForm;
import developmentpermission.form.ApplicationRegisterForm;
import developmentpermission.form.ApplicationRegisterResultForm;
import developmentpermission.form.ApplicationSearchConditionForm;
import developmentpermission.form.ApplicationSearchResultForm;
import developmentpermission.form.ApplicationStepForm;
import developmentpermission.form.ApplicationTypeForm;
import developmentpermission.form.ApplyAnswerForm;
import developmentpermission.form.ChatSearchResultForm;
import developmentpermission.form.DepartmentForm;
import developmentpermission.form.GeneralConditionDiagnosisResultForm;
import developmentpermission.form.ItemAnswerStatusForm;
import developmentpermission.form.OutputDataForm;
import developmentpermission.form.ReApplicationForm;
import developmentpermission.form.ReApplicationRequestForm;
import developmentpermission.form.ResponseEntityForm;
import developmentpermission.form.StatusForm;
import developmentpermission.form.AnswerStatusForm;
import developmentpermission.form.AnswerNameForm;
import developmentpermission.form.UploadApplicationFileForm;
import developmentpermission.service.ApplicationService;
import developmentpermission.service.CategoryService;
import developmentpermission.service.ChatService;
import developmentpermission.service.CsvExportService;
import developmentpermission.util.AuthUtil;
import developmentpermission.util.LogUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.web.util.UriUtils;
import java.nio.charset.StandardCharsets;

/**
 * 申請APIコントローラ
 */
@Api(tags = "申請")
@RestController
@RequestMapping("/application")
public class ApplicationApiController extends AbstractApiController {

	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationApiController.class);

	/** 申請Serviceインスタンス */
	@Autowired
	private ApplicationService applicationService;

	/** 申請区分Serviceインスタンス */
	@Autowired
	private CategoryService categoryService;

	/** チャットServiceインスタンス */
	@Autowired
	private ChatService chatService;
	/** CSV出力Serviceインスタンス */
	@Autowired
	private CsvExportService csvExportService;

	/** 申請登録 csvログファイルヘッダー */
	@Value("${app.csv.log.header.application.register}")
	private String[] applicationRegisterLogHeader;

	/** 申請登録 csvログファイルパス */
	@Value("${app.csv.log.path.application.register}")
	private String applicationRegisterLogPath;

	/** 再申請登録 csvログファイルヘッダー */
	@Value("${app.csv.log.header.application.reapplication}")
	private String[] reapplicationLogHeader;

	/** 再申請登録 csvログファイルパス */
	@Value("${app.csv.log.path.application.reapplication}")
	private String reapplicationLogPath;
	
	/** 除外選択部署 */
	@Value("${app.exclude.select.departments}")
	private String excludeDepartments;

	/**
	 * 申請者情報入力項目一覧取得(事業者)
	 * 
	 * @return 申請者情報入力項目一覧
	 */
	@RequestMapping(value = "/applicantItems", method = RequestMethod.GET)
	@ApiOperation(value = "申請者情報入力項目一覧取得", notes = "申請者情報入力項目一覧を取得する.")
	@ResponseBody
	@ApiResponses(value = { @ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class) })
	public List<ApplicantInformationItemForm> getApplicantItems() {
		LOGGER.info("申請者情報入力項目一覧取得 開始");
		try {
			List<ApplicantInformationItemForm> applicantInformationItemFormList = applicationService
					.getApplicantItems();
			return applicantInformationItemFormList;
		} finally {
			LOGGER.info("申請者情報入力項目一覧取得 終了");
		}
	}

	/**
	 * 申請ファイル一覧取得(事業者)
	 * 
	 * @param generalConditionDiagnosisResultForm 検索条件
	 * @return 申請ファイル一覧
	 */
	@RequestMapping(value = "/applicationFiles", method = RequestMethod.POST)
	@ApiOperation(value = "申請ファイル一覧取得", notes = "アップロード対象の申請ファイル一覧を取得する.")
	@ResponseBody
	@ApiResponses(value = { @ApiResponse(code = 400, message = "パラメータ不正", response = ResponseEntityForm.class),
			@ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class) })
	public List<ApplicationFileForm> getApplicationFiles(
			@ApiParam(required = true, value = "概況診断結果フォーム一覧") @RequestBody List<GeneralConditionDiagnosisResultForm> generalConditionDiagnosisResultFormList) {
		LOGGER.info("申請ファイル一覧取得 開始");
		try {
			if (generalConditionDiagnosisResultFormList != null && generalConditionDiagnosisResultFormList.size() > 0) {
				List<ApplicationFileForm> applicationFileFormList = applicationService
						.getApplicationFiles(generalConditionDiagnosisResultFormList);
				return applicationFileFormList;
			} else {
				LOGGER.warn("パラメータが空またはnull");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}
		} finally {
			LOGGER.info("申請ファイル一覧取得 終了");
		}
	}

	/**
	 * 再申請情報取得(事業者)
	 * 
	 * @param generalConditionDiagnosisResultForm 検索条件
	 * @return 再申請情報
	 */
	@RequestMapping(value = "/reappInformation", method = RequestMethod.POST)
	@ApiOperation(value = "再申請情報取得", notes = "再申請情報取得を取得する.")
	@ResponseBody
	@ApiResponses(value = { @ApiResponse(code = 400, message = "パラメータ不正", response = ResponseEntityForm.class),
			@ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class),
			@ApiResponse(code = 403, message = "申請情報取得不能", response = ResponseEntityForm.class) })
	public ReApplicationForm getReApplicationFiles(
			@ApiParam(required = true, value = "申請者ログインパラメータフォーム") @RequestBody ReApplicationRequestForm reApplicationRequestForm) {
		LOGGER.info("再申請情報取得 開始");
		ReApplicationForm form = new ReApplicationForm();
		try {
			// パラメータチェック
			String id = reApplicationRequestForm.getLoginId();
			String password = reApplicationRequestForm.getPassword();
			Integer parmApplicationId = reApplicationRequestForm.getApplicationId();

			if (reApplicationRequestForm.getApplicationId() == null
					|| reApplicationRequestForm.getApplicationStepId() == null
					|| reApplicationRequestForm.getPreApplicationStepId() == null) {
				LOGGER.warn("申請IDまたは処理中申請段階ID、前回の申請段階IDが設定されていないl");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}

			if (id != null && !"".equals(id) && password != null && !"".equals(password)) {
				AnswerConfirmLoginForm answerConfirmLoginForm = new AnswerConfirmLoginForm();
				answerConfirmLoginForm.setLoginId(id);
				answerConfirmLoginForm.setPassword(password);
				// 申請ID
				Integer applicationId = applicationService.getApplicationIdFromApplicantInfo(answerConfirmLoginForm);
				if (applicationId != null && applicationId.equals(parmApplicationId)) {
					// 再申請情報取得
					form = applicationService.getReApplicationInfo(reApplicationRequestForm);
					form.setApplicationId(applicationId);
					form.setLoginId(id);
					form.setPassword(password);
				} else {
					LOGGER.warn("申請情報取得不能");
					throw new ResponseStatusException(HttpStatus.FORBIDDEN);
				}
			} else {
				LOGGER.warn("IDまたはパスワードが空またはnull");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}

			return form;
		} finally {
			LOGGER.info("再申請情報取得 終了");
		}
	}

	/**
	 * 再申請用申請ファイル一覧取得(事業者)
	 * 
	 * @param generalConditionDiagnosisResultForm 検索条件
	 * @return 申請ファイル一覧
	 */
	@RequestMapping(value = "/reapply/applicationFiles", method = RequestMethod.POST)
	@ApiOperation(value = "申請ファイル一覧取得", notes = "アップロード対象の申請ファイル一覧を取得する.")
	@ResponseBody
	@ApiResponses(value = { @ApiResponse(code = 400, message = "パラメータ不正", response = ResponseEntityForm.class),
			@ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class) })
	public List<ApplicationFileForm> getApplicationFiles(
			@ApiParam(required = true, value = "概況診断結果フォーム一覧") @RequestBody ReApplicationRequestForm reApplicationRequestForm) {
		LOGGER.info("申請ファイル一覧取得 開始");
		try {

			// パラメータチェック
			String id = reApplicationRequestForm.getLoginId();
			String password = reApplicationRequestForm.getPassword();
			Integer parmApplicationId = reApplicationRequestForm.getApplicationId();

			if (id != null && !"".equals(id) && password != null && !"".equals(password) && parmApplicationId != null) {
				AnswerConfirmLoginForm answerConfirmLoginForm = new AnswerConfirmLoginForm();
				answerConfirmLoginForm.setLoginId(id);
				answerConfirmLoginForm.setPassword(password);
				// 申請ID
				Integer applicationId = applicationService.getApplicationIdFromApplicantInfo(answerConfirmLoginForm);
				if (applicationId != null && applicationId.equals(parmApplicationId)) {
					// 再申請用申請ファイル一覧取得
					List<ApplicationFileForm> applicationFileFormList = applicationService
							.getReapplicationFiles(reApplicationRequestForm);
					return applicationFileFormList;
				} else {
					LOGGER.warn("申請情報取得不能");
					throw new ResponseStatusException(HttpStatus.FORBIDDEN);
				}
			} else {
				LOGGER.warn("IDまたはパスワード、申請IDが空またはnull");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}
		} finally {
			LOGGER.info("申請ファイル一覧取得 終了");
		}
	}

	/**
	 * 【再申請】申請仮登録データの消去
	 * 
	 * @param applicationId　申請ID
	 * @return
	 */
	@RequestMapping(value = "/reapplication/reset/{application_id}", method = RequestMethod.GET)
	@ApiOperation(value = "【再申請】申請仮登録データの消去", notes = "【再申請】仮申請状態のデータを消去する.")
	@ResponseBody
	@ApiResponses(value = { @ApiResponse(code = 400, message = "パラメータ不正", response = ResponseEntityForm.class),
			@ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class),
			@ApiResponse(code = 403, message = "ロール不適合", response = ResponseEntityForm.class),
			@ApiResponse(code = 409, message = "ステータス不正", response = ResponseEntityForm.class),
			@ApiResponse(code = 503, message = "処理エラー", response = ResponseEntityForm.class) })
	public ResponseEntityForm rollbackReapplication(
			@ApiParam(required = true, value = "申請ID") @PathVariable(value = "application_id") Integer applicationId,
			@CookieValue(value = "token", required = false) String token) {
		LOGGER.info("【再申請】申請仮登録データの消去 開始");
		try {

			String role = AuthUtil.getRole(token);
			if (AuthUtil.ROLE_BUSINESS.equals(role) || AuthUtil.ROLE_GOVERMENT.equals(role)) {
				if (applicationId != null) {
					if (applicationService.resetApplicationInfo(applicationId)) {

						ResponseEntityForm responseEntityForm = new ResponseEntityForm(HttpStatus.OK.value(),
								"Application information reset successful.");
						return responseEntityForm;
					} else {
						LOGGER.warn("申請情報取得不能");
						throw new ResponseStatusException(HttpStatus.FORBIDDEN);
					}
				} else {
					// パラメータ不正
					LOGGER.warn("パラメータ不正");
					throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
				}
			} else {
				LOGGER.warn("ロール不適合: " + role);
				throw new ResponseStatusException(HttpStatus.FORBIDDEN);
			}
		} finally {
			LOGGER.info("【再申請】申請仮登録データの消去 終了");
		}
	}

	/**
	 * 再申請登録(事業者)
	 * 
	 * @param applicationRegisterForm 登録情報
	 */
	@ResponseStatus(code = HttpStatus.CREATED)
	@RequestMapping(value = "/reapplication", method = RequestMethod.POST)
	@ApiOperation(value = "申請登録", notes = "申請を登録する.")
	@ResponseBody
	@ApiResponses(value = { @ApiResponse(code = 400, message = "パラメータ不正", response = ResponseEntityForm.class),
			@ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class),
			@ApiResponse(code = 409, message = "登録に失敗した場合", response = ResponseEntityForm.class),
			@ApiResponse(code = 503, message = "処理エラー", response = ResponseEntityForm.class) })
	public ApplicationRegisterResultForm reApplicationRegister(
			@ApiParam(required = true, value = "再申請情報フォーム") @RequestBody ReApplicationForm reApplicationForm,
			@CookieValue(value = "token", required = false) String token) {
		LOGGER.info("再申請登録 開始");
		try {

			// アクセスID
			String accessId = AuthUtil.getAccessId(token);
			if (accessId == null || "".equals(accessId)) {
				LOGGER.warn("アクセスIDがnullまたは空");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}

			// パラメータチェック
			if (applicationService.validateReApplicationParam(reApplicationForm)) {
				// 再申請登録処理
				int answerExpectDays = applicationService.updateApplication(reApplicationForm);

				ApplicationRegisterResultForm form = new ApplicationRegisterResultForm();
				form.setApplicationId(reApplicationForm.getApplicationId());
				form.setApplicationStepId(reApplicationForm.getApplicationStepId());
				form.setAnswerExpectDays(answerExpectDays);

				// 再申請登録 ログ出力
				try {

					// 申請種類
					String applicationTypeName = applicationService
							.getApplicationTypeName(reApplicationForm.getApplicationTypeId());
					// 申請種類
					String applicationStepName = applicationService
							.getApplicationStepName(reApplicationForm.getApplicationStepId());
					// 版情報
					Integer versionInformation = reApplicationForm.getVersionInformation() + 1;

					//ログデータ：アクセスID、アクセス日時、申請ID、申請種類、申請段階、版情報
					Object[] logData = { accessId, LogUtil.localDateTimeToString(LocalDateTime.now()),
							reApplicationForm.getApplicationId(), applicationTypeName, applicationStepName,
							versionInformation.toString() };
					LogUtil.writeLogToCsv(reapplicationLogPath, reapplicationLogHeader, logData);

				} catch (Exception ex) {
					ex.printStackTrace();
				}
				return form;
			} else {
				// パラメータ不正
				LOGGER.warn("パラメータ不正");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}
		} finally {
			LOGGER.info("再申請登録 終了");
		}
	}

	/**
	 * 再申請完了通知
	 * 
	 * @param applicationRegisterResultForm
	 * @return
	 */
	@RequestMapping(value = "/reapplication/complete/notify", method = RequestMethod.POST)
	@ApiOperation(value = "再申請完了通知", notes = "再申請登録完了の旨を申請者に通知する.")
	@ResponseBody
	@ApiResponses(value = { @ApiResponse(code = 400, message = "パラメータ不正", response = ResponseEntityForm.class),
			@ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class),
			@ApiResponse(code = 409, message = "ステータス不正", response = ResponseEntityForm.class),
			@ApiResponse(code = 503, message = "処理エラー", response = ResponseEntityForm.class) })
	public ResponseEntityForm completeNotify(
			@ApiParam(required = true, value = "申請登録結果フォーム") @RequestBody ApplicationRegisterResultForm applicationRegisterResultForm) {
		LOGGER.info("再申請完了通知 開始");
		try {
			if (applicationRegisterResultForm != null //
					&& applicationRegisterResultForm.getApplicationId() != null
					&& applicationRegisterResultForm.getApplicationStepId() != null) {
				applicationService.notifyReapplyComplete(applicationRegisterResultForm);
				ResponseEntityForm responseEntityForm = new ResponseEntityForm(HttpStatus.OK.value(),
						"Application registration successful.");
				return responseEntityForm;
			} else {
				LOGGER.warn("パラメータ不正");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}
		} finally {
			LOGGER.info("再申請完了通知 終了");
		}
	}

	/**
	 * 申請登録(事業者)
	 * 
	 * @param applicationRegisterForm 登録情報
	 */
	@ResponseStatus(code = HttpStatus.CREATED)
	@RequestMapping(value = "/register", method = RequestMethod.POST)
	@ApiOperation(value = "申請登録", notes = "申請を登録する.")
	@ResponseBody
	@ApiResponses(value = { @ApiResponse(code = 400, message = "パラメータ不正", response = ResponseEntityForm.class),
			@ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class),
			@ApiResponse(code = 409, message = "登録に失敗した場合", response = ResponseEntityForm.class),
			@ApiResponse(code = 503, message = "処理エラー", response = ResponseEntityForm.class) })
	public ApplicationRegisterResultForm registerApplication(
			@ApiParam(required = true, value = "申請登録リクエストフォーム") @RequestBody ApplicationRegisterForm applicationRegisterForm,
			@CookieValue(value = "token", required = false) String token) {
		LOGGER.info("申請登録 開始");
		try {
			// パラメータチェック
			if (applicationService.validateRegisterApplicationParam(applicationRegisterForm)) {
				try {
					// 登録処理
					int applicationId = applicationService.registerApplication(applicationRegisterForm);

					// 申請判定項目のリスト作成
					List<String> judgementItemIdList = new ArrayList<String>();
					for (GeneralConditionDiagnosisResultForm generalConditionDiagnosisResultForm : applicationRegisterForm
							.getGeneralConditionDiagnosisResultForm()) {
						String judgementId = generalConditionDiagnosisResultForm.getJudgementId();
						if (!judgementItemIdList.contains(judgementId)) {
							judgementItemIdList.add(judgementId);
						}
					}

					// 回答予定日数算出
					int answerExpectDays = applicationService.getnswerExpectDays(judgementItemIdList,
							applicationRegisterForm.getApplicationTypeId(),
							applicationRegisterForm.getApplicationStepId());

					ApplicationRegisterResultForm form = new ApplicationRegisterResultForm();
					form.setApplicationId(applicationId);
					form.setApplicationStepId(applicationRegisterForm.getApplicationStepId());
					form.setAnswerExpectDays(answerExpectDays);
					// 申請登録 ログ出力
					try {
						// アクセスID
						String accessId = AuthUtil.getAccessId(token);

						// 申請種類
						String applicationTypeName = applicationService
								.getApplicationTypeName(applicationRegisterForm.getApplicationTypeId());
						// 申請種類
						String applicationStepName = applicationService
								.getApplicationStepName(applicationRegisterForm.getApplicationStepId());

						Object[] logData = { accessId, LogUtil.localDateTimeToString(LocalDateTime.now()),
								applicationRegisterForm.getGeneralConditionDiagnosisResultForm().get(0)
										.getGeneralConditionDiagnosisResultId(),
								applicationId, applicationTypeName, applicationStepName };
						LogUtil.writeLogToCsv(applicationRegisterLogPath, applicationRegisterLogHeader, logData);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					return form;
				} catch (RuntimeException ex) {
					LOGGER.error("申請登録時にエラー発生", ex);
					throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
				}
			} else {
				// パラメータ不正
				LOGGER.warn("パラメータ不正");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}
		} finally {
			LOGGER.info("申請登録 終了");
		}
	}

	/**
	 * 【申請】申請仮登録データの消去
	 * 
	 * @param applicationId 申請ID
	 * @return
	 */
	@RequestMapping(value = "/application/rollback/{application_id}", method = RequestMethod.GET)
	@ApiOperation(value = "【申請】申請仮登録データの消去", notes = "【申請】仮申請状態のデータを消去する.")
	@ResponseBody
	@ApiResponses(value = { @ApiResponse(code = 400, message = "パラメータ不正", response = ResponseEntityForm.class),
			@ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class),
			@ApiResponse(code = 403, message = "ロール不適合", response = ResponseEntityForm.class),
			@ApiResponse(code = 409, message = "ステータス不正", response = ResponseEntityForm.class),
			@ApiResponse(code = 503, message = "処理エラー", response = ResponseEntityForm.class) })
	public ResponseEntityForm rollbackApplication(
			@ApiParam(required = true, value = "申請ID") @PathVariable(value = "application_id") Integer applicationId,
			@CookieValue(value = "token", required = false) String token) {
		LOGGER.info("【申請】申請仮登録データの消去 開始");
		try {

			String role = AuthUtil.getRole(token);
			if (AuthUtil.ROLE_BUSINESS.equals(role) || AuthUtil.ROLE_GOVERMENT.equals(role)) {
				if (applicationId != null) {
					if (applicationService.deleteProvisionalApplication(applicationId)) {

						ResponseEntityForm responseEntityForm = new ResponseEntityForm(HttpStatus.OK.value(),
								"Application information reset successful.");
						return responseEntityForm;
					} else {
						LOGGER.warn("申請情報取得不能");
						throw new ResponseStatusException(HttpStatus.FORBIDDEN);
					}
				} else {
					// パラメータ不正
					LOGGER.warn("パラメータ不正");
					throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
				}
			} else {
				LOGGER.warn("ロール不適合: " + role);
				throw new ResponseStatusException(HttpStatus.FORBIDDEN);
			}
		} finally {
			LOGGER.info("【申請】申請仮登録データの消去 終了");
		}
	}

	/**
	 * 申請情報検索条件一覧取得(行政)
	 * 
	 * @return 申請情報検索条件一覧取得
	 */
	@RequestMapping(value = "/search/conditions", method = RequestMethod.GET)
	@ApiOperation(value = "申請情報検索条件一覧取得(行政)", notes = "申請情報検索条件一覧を取得する.")
	@ResponseBody
	@ApiResponses(value = { @ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class),
			@ApiResponse(code = 503, message = "処理エラー", response = ResponseEntityForm.class) })
	public ApplicationSearchConditionForm getApplicationSearchConditions() {
		LOGGER.info("申請情報検索条件一覧取得 開始");
		try {
			ApplicationSearchConditionForm applicationSearchConditionForm = new ApplicationSearchConditionForm();

			// 申請者情報一覧
			LOGGER.debug("申請者情報一覧取得 開始");
			List<ApplicantInformationItemForm> applicantInformationItemFormList = applicationService
					.getApplicantItems();
			applicationSearchConditionForm.setApplicantInformationItemForm(applicantInformationItemFormList);
			LOGGER.debug("申請者情報一覧取得 終了");

			// 申請区分選択一覧
			LOGGER.debug("申請区分選択一覧取得 開始");
			List<ApplicationCategorySelectionViewForm> applicationCategories = categoryService
					.getApplicationCategorySelectionViewList();
			applicationSearchConditionForm.setApplicationCategories(applicationCategories);
			LOGGER.debug("申請区分選択一覧取得 終了");

			// ステータス一覧（申請情報）
			LOGGER.debug("ステータス一覧（申請情報）取得 開始");
			List<StatusForm> statusList = applicationService.getStatusList();
			applicationSearchConditionForm.setStatus(statusList);
			LOGGER.debug("ステータス一覧（申請情報）取得 終了");

			// ステータス一覧（問い合わせ情報）
			LOGGER.debug("ステータス一覧（問い合わせ情報）取得 開始");
			List<AnswerStatusForm> answerStatusList = applicationService.getAnswerStatusList();
			applicationSearchConditionForm.setAnswerStatus(answerStatusList);
			LOGGER.debug("ステータス一覧（問い合わせ情報）取得 終了");

			// ステータス一覧（条文回答）
			LOGGER.debug("ステータス一覧（条文回答）取得 開始");
			List<ItemAnswerStatusForm> itemAnswerStatusList = applicationService.getItemAnswerStatusList();
			applicationSearchConditionForm.setItemAnswerStatus(itemAnswerStatusList);
			LOGGER.debug("ステータス一覧（条文回答）取得 終了");
			// 部署一覧
			LOGGER.debug("部署一覧取得 開始");
			List<DepartmentForm> departmentList = applicationService.getDepartmentList();
			applicationSearchConditionForm.setDepartment(departmentList);
			LOGGER.debug("部署一覧取得 終了");

			// 回答者一覧
			LOGGER.debug("回答者一覧取得 開始");
			List<AnswerNameForm> answerNameList = applicationService.getAnswerNameList();
			applicationSearchConditionForm.setAnswerName(answerNameList);
			LOGGER.debug("回答者一覧取得 終了");

			// 申請種類
			LOGGER.debug("申請種類取得 開始");
			List<ApplicationTypeForm> applicationTypeList = applicationService.getApplicationTypeList();
			applicationSearchConditionForm.setApplicationTypes(applicationTypeList);
			LOGGER.debug("申請種類取得 終了");

			// 申請段階
			LOGGER.debug("申請段階取得 開始");
			List<ApplicationStepForm> applicationStepList = applicationService.getApplicationStepList();
			applicationSearchConditionForm.setApplicationSteps(applicationStepList);
			LOGGER.debug("申請段階取得 終了");

			// 申請追加情報一覧
			LOGGER.debug("申請追加情報取得 開始");
			List<ApplicantInformationItemForm> applicantInformationItemList = applicationService
					.getApplicantInformationItemList();
			applicationSearchConditionForm.setApplicantAddInformationItemForm(applicantInformationItemList);
			LOGGER.debug("申請追加情報取得 終了");

			return applicationSearchConditionForm;
		} catch (Exception ex) {
			LOGGER.error("申請情報検索条件一覧取得時に例外発生", ex);
			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		} finally {
			LOGGER.info("申請情報検索条件一覧取得 終了");
		}
	}

	/**
	 * 申請情報検索結果表示項目一覧取得(行政)
	 * 
	 * @return 申請情報検索結果表示項目一覧取得
	 */
	@RequestMapping(value = "/search/columns", method = RequestMethod.GET)
	@ApiOperation(value = "申請情報検索結果表示項目一覧取得(行政)", notes = "申請情報検索結果表示項目一覧を取得する.")
	@ResponseBody
	@ApiResponses(value = { @ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class) })
	public List<ApplicationInformationSearchResultHeaderForm> getApplicationInformationSearchResultHeader() {
		LOGGER.info("申請情報検索結果表示項目一覧取得 開始");
		try {
			List<ApplicationInformationSearchResultHeaderForm> applicationInformationSearchResultFormList = applicationService
					.getApplicationInformationSearchResultHeader();
			return applicationInformationSearchResultFormList;
		} finally {
			LOGGER.info("申請情報検索結果表示項目一覧取得 終了");
		}
	}

	/**
	 * 申請情報検索(行政)
	 * 
	 * @param applicationSearchConditionForm 検索条件
	 * @return 検索結果
	 */
	@RequestMapping(value = "/search", method = RequestMethod.POST)
	@ApiOperation(value = "申請情報検索(行政)", notes = "申請情報を検索する.")
	@ResponseBody
	@ApiResponses(value = { @ApiResponse(code = 400, message = "パラメータ不正", response = ResponseEntityForm.class),
			@ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class) })
	public List<ApplicationSearchResultForm> searchApplicationInformation(
			@ApiParam(required = true, value = "申請情報検索条件フォーム") @RequestBody ApplicationSearchConditionForm applicationSearchConditionForm) {
		LOGGER.info("申請情報検索 開始");
		try {
			List<StatusForm> status = applicationSearchConditionForm.getStatus();
			List<DepartmentForm> department = applicationSearchConditionForm.getDepartment();
			List<AnswerNameForm> answerName = applicationSearchConditionForm.getAnswerName();
			if ((status != null && status.size() > 1) || (department != null && department.size() > 1)
					|| (answerName != null && answerName.size() > 1)) {
				// ステータス、部署、回答者は複数の場合エラーとする
				LOGGER.warn("ステータス、部署、回答者情報がnullまたは複数設定されている");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}
			List<ApplicationSearchResultForm> applicationSearchResultFormList = applicationService
					.searchApplicationInformation(applicationSearchConditionForm);
			return applicationSearchResultFormList;
		} finally {
			LOGGER.info("申請情報検索 終了");
		}
	}

	/**
	 * 申請情報詳細取得(行政)
	 * 
	 * @param Integer 申請ID
	 * @return ApplyAnswerForm 申請情報詳細
	 */
	@RequestMapping(value = "/detail/{application_id}", method = RequestMethod.GET)
	@ApiOperation(value = "申請情報詳細取得(行政)", notes = "申請情報詳細を取得する.")
	@ResponseBody
	@ApiResponses(value = { @ApiResponse(code = 400, message = "パラメータ不正", response = ResponseEntityForm.class),
			@ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class) })
	public ApplyAnswerForm getApplicationDetail(
			@ApiParam(required = true, value = "申請ID") @PathVariable(value = "application_id") Integer applicationId,
			@CookieValue(value = "token", required = false) String token) {
		LOGGER.info("申請情報詳細取得 開始");
		try {
			// ユーザーID
			String userId = AuthUtil.getUserId(token);
			if (userId != null && !"".equals(userId)) {
				ApplyAnswerForm applyAnswerForm = applicationService.getApplicationDetail(applicationId, userId,
						true);
				return applyAnswerForm;
			} else {
				LOGGER.warn("ユーザーIDがnullまたは空");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}
		} finally {
			LOGGER.info("申請情報詳細取得 終了");
		}
	}

	/**
	 * 申請ファイルアップロード
	 * 
	 * @param uploadApplicationFileForm O_申請ファイルファイルフォーム
	 * @return
	 */
	@RequestMapping(value = "/file/upload", method = RequestMethod.POST, consumes = { "multipart/form-data" })
	@ApiOperation(value = "申請ファイルアップロード", notes = "申請ファイルをアップロードする.")
	@ResponseBody
	@ApiResponses(value = { @ApiResponse(code = 400, message = "パラメータ不正", response = ResponseEntityForm.class),
			@ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class),
			@ApiResponse(code = 403, message = "ロール不適合", response = ResponseEntityForm.class) })
	public ResponseEntityForm uploadApplicationFile(
			/* @RequestBody // MultipartFileを含む場合、RequestBodyでは415エラーで弾かれる・・・？ */
			@ApiParam(required = true, value = "申請ファイルフォーム[multipart/form-data]") @ModelAttribute UploadApplicationFileForm uploadApplicationFileForm,
			@CookieValue(value = "token", required = false) String token) {
		LOGGER.info("申請ファイルアップロード 開始");
		try {
			String role = AuthUtil.getRole(token);
			if (AuthUtil.ROLE_BUSINESS.equals(role) || AuthUtil.ROLE_GOVERMENT.equals(role)) {
				if (applicationService.validateUploadApplicationFile(uploadApplicationFileForm)) {
					applicationService.uploadApplicationFile(uploadApplicationFileForm, null);
					ResponseEntityForm responseEntityForm = new ResponseEntityForm(HttpStatus.CREATED.value(),
							"Application File registration successful.");
					return responseEntityForm;
				} else {
					// パラメータ不正
					LOGGER.warn("パラメータ不正");
					throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
				}
			} else {
				// 事業者ユーザしかアクセス不可
				LOGGER.warn("ロール不適合: " + role);
				throw new ResponseStatusException(HttpStatus.FORBIDDEN);
			}
		} finally {
			LOGGER.info("申請ファイルアップロード 終了");
		}
	}

	/**
	 * 申請ファイルダウンロード
	 * 
	 * @param uploadApplicationFileForm O_申請ファイルファイルフォーム
	 * @return 応答Entity
	 */
	@RequestMapping(value = "/file/download", method = RequestMethod.POST)
	@ApiOperation(value = "申請ファイルダウンロード", notes = "申請ファイルをダウンロードする.")
	@ResponseBody
	@ApiResponses(value = { @ApiResponse(code = 400, message = "パラメータ不正", response = ResponseEntityForm.class),
			@ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class),
			@ApiResponse(code = 403, message = "ロール不適合", response = ResponseEntityForm.class),
			@ApiResponse(code = 404, message = "ファイルが存在しない場合", response = ResponseEntityForm.class) })
	public ResponseEntity<Resource> downloadApplicationFile(
			@ApiParam(required = true, value = "申請ファイルフォーム") @RequestBody UploadApplicationFileForm uploadApplicationFileForm,
			@CookieValue(value = "token", required = false) String token) {
		LOGGER.info("申請ファイルダウンロード 開始");
		try {

			String role = AuthUtil.getRole(token);
			if (AuthUtil.ROLE_BUSINESS.equals(role)) {
				String id = uploadApplicationFileForm.getLoginId();
				String password = uploadApplicationFileForm.getPassword();
				if (id != null && !"".equals(id) //
						&& password != null && !"".equals(password)) {
					AnswerConfirmLoginForm answerConfirmLoginForm = new AnswerConfirmLoginForm(id, password, false);
					// 申請ID
					Integer applicationId = applicationService
							.getApplicationIdFromApplicantInfo(answerConfirmLoginForm);
					if (applicationId == null || !applicationId.equals(uploadApplicationFileForm.getApplicationId())) {
						// 照合IDとパスワードによる認証失敗
						LOGGER.warn("申請ファイルダウンロードでの照合IDとパスワードによる認証失敗：" + role);
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

			if (applicationService.validateDownloadApplicationFile(uploadApplicationFileForm)) {
				return applicationService.downloadApplicationFile(uploadApplicationFileForm);
			} else {
				// パラメータ不正
				LOGGER.warn("パラメータ不正");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}
		} finally {
			LOGGER.info("申請ファイルダウンロード 終了");
		}
	}

	/**
	 * 照合情報を申請者に通知する
	 * 
	 * @param applicationRegisterResultForm
	 * @return
	 */
	@RequestMapping(value = "/notify/collation", method = RequestMethod.POST)
	@ApiOperation(value = "照合情報通知", notes = "照合ID/パスワードを発行し申請者に通知する.")
	@ResponseBody
	@ApiResponses(value = { @ApiResponse(code = 400, message = "パラメータ不正", response = ResponseEntityForm.class),
			@ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class),
			@ApiResponse(code = 409, message = "ステータス不正", response = ResponseEntityForm.class),
			@ApiResponse(code = 503, message = "処理エラー", response = ResponseEntityForm.class) })
	public AnswerConfirmLoginForm notifyCollationInformation(
			@ApiParam(required = true, value = "申請登録結果フォーム") @RequestBody ApplicationRegisterResultForm applicationRegisterResultForm) {
		LOGGER.info("照合情報通知 開始");
		try {
			AnswerConfirmLoginForm form = null;
			if (applicationRegisterResultForm != null //
					&& applicationRegisterResultForm.getApplicationId() != null) {
				form = applicationService.notifyCollationInformation(applicationRegisterResultForm);
				if (form == null) {
					LOGGER.warn("パラメータ不正");
					throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
				}
			} else {
				LOGGER.warn("パラメータ不正");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}
			return form;
		} finally {
			LOGGER.info("照合情報通知 終了");
		}
	}

	/**
	 * 申請種類一覧取得
	 * 
	 * @return 申請種類一覧取得
	 */
	@RequestMapping(value = "/applicationType", method = RequestMethod.GET)
	@ApiOperation(value = "申請種類一覧取得", notes = "申請種類一覧を取得する.")
	@ResponseBody
	@ApiResponses(value = { @ApiResponse(code = 503, message = "処理エラー", response = ResponseEntityForm.class) })
	public List<ApplicationTypeForm> getApplicationTypes() {
		LOGGER.info("申請種類一覧取得 開始");
		try {
			List<ApplicationTypeForm> formList = new ArrayList<ApplicationTypeForm>();

			// 申請区分選択画面の申請種類リスト取得
			LOGGER.debug("申請種類一覧取得 開始");
			formList = applicationService.getApplicationTypeList();
			LOGGER.debug("申請種類一覧取得 終了");

			return formList;
		} catch (Exception ex) {
			LOGGER.error("申請種類一覧取得に例外発生", ex);
			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		} finally {
			LOGGER.info("申請種類一覧取得 終了");
		}
	}

	/**
	 * CSVファイル出力
	 * 
	 * @param applicationRegisterResultForm
	 * @return CSVファイル出力
	 */
	@RequestMapping(value = "/searchresult/output", method = RequestMethod.POST)
	@ApiOperation(value = "CSVファイル出力", notes = "CSVファイルを出力する")
	@ResponseBody
	@ApiResponses(value = { @ApiResponse(code = 400, message = "パラメータ不正", response = ResponseEntityForm.class),
			@ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class),
			@ApiResponse(code = 409, message = "ステータス不正", response = ResponseEntityForm.class),
			@ApiResponse(code = 503, message = "処理エラー", response = ResponseEntityForm.class) })
	public ResponseEntity<byte[]> exportCsv(
			@ApiParam(required = true, value = "申請登録結果フォーム") @RequestBody OutputDataForm outputDataForm,
			@CookieValue(value = "token", required = false) String token, HttpServletResponse response) {
		LOGGER.info("CSVファイル出力 開始");
		try {
			// 権限チェック（行政か否か）
			LOGGER.info("権限チェック（行政か否か） 開始");
			String role = AuthUtil.getRole(token);
			if (!AuthUtil.ROLE_GOVERMENT.equals(role)) {
				// 行政ユーザしかアクセス不可
				LOGGER.warn("ロール不適合:" + role);
				throw new ResponseStatusException(HttpStatus.FORBIDDEN);
			}
			LOGGER.info("権限チェック（行政か否か） 終了");
			ApplicationSearchConditionForm applicationSearchConditionForm = outputDataForm.getConditions();
			// パラメータチェック
			List<StatusForm> status = applicationSearchConditionForm.getStatus();
			List<DepartmentForm> department = applicationSearchConditionForm.getDepartment();
			List<AnswerNameForm> answerName = applicationSearchConditionForm.getAnswerName();
			if ((status != null && status.size() > 1) || (department != null && department.size() > 1)
					|| (answerName != null && answerName.size() > 1)) {
				// ステータス、部署、回答者は複数の場合エラーとする
				LOGGER.warn("ステータス、部署、回答者情報がnullまたは複数設定されている");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}
			String csvText = "";
			String fileName = "";
			if (outputDataForm.getDataType().equals(CsvExportService.DATA_TYPE_APPLICATION)) {
				LOGGER.info("CSVファイル出力対象: 申請情報");
				// 申請情報出力項目取得
				List<ApplicationInformationSearchResultHeaderForm> applicationInformationSearchResultFormList = applicationService
						.getApplicationInformationSearchResultHeader();
				// 申請情報: リクエストで連携されたソート済み情報を使用
				List<ApplicationSearchResultForm> applicationSearchResultFormList = outputDataForm.getApplicationSearchResults();
				// CSVテキスト生成
				csvText = csvExportService.exportApplicationCsv(outputDataForm, applicationSearchResultFormList,
						applicationInformationSearchResultFormList);
				// ファイル名はDL時フロントエンドで指定.設定もフロントエンドで実施.
				fileName = "申請情報検索結果.csv";
			} else if (outputDataForm.getDataType().equals(CsvExportService.DATA_TYPE_INQUIRY)) {
				LOGGER.info("CSVファイル出力対象: 問合せ情報");
				// 問合せ情報: リクエストで連携されたソート済み情報を使用
				List<ChatSearchResultForm> resultList = outputDataForm.getChatSearchResults();
				// CSVテキスト生成
				csvText = csvExportService.exportInquiryCsv(outputDataForm, resultList);
				// ファイル名はDL時フロントエンドで指定.設定もフロントエンドで実施.
				fileName = "問い合わせ情報検索結果.csv";
			} else {
				LOGGER.warn("パラメータ不正");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}
			// CSV出力
			try {
				HttpHeaders headers = new HttpHeaders();
				final String CONTENT_DISPOSITION_FORMAT = "attachment; filename=\"%s\"; filename*=UTF-8''%s";
				String headerValue = String.format(CONTENT_DISPOSITION_FORMAT, fileName,
						UriUtils.encode(fileName, StandardCharsets.UTF_8.name()));
				headers.add(HttpHeaders.CONTENT_DISPOSITION, headerValue);
				headers.add(HttpHeaders.CONTENT_TYPE, "text/csv");

				byte[] data = csvText.getBytes("MS932");
				headers.setContentLength(data.length);
				return new ResponseEntity<byte[]>(data, headers, HttpStatus.OK);
			} catch (Exception e) {
				LOGGER.warn("CSV出力でエラー発生");
				throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
			}
		} finally {
			LOGGER.info("CSVファイル出力 終了");
		}
	}

	/**
	 * 回答・通知可能の申請段階一覧取得
	 * 
	 * @return 申請段階一覧取得
	 */
	@RequestMapping(value = "/applicationStep/{application_id}/{isNotify}", method = RequestMethod.GET)
	@ApiOperation(value = "申請段階一覧取得", notes = "申請段階一覧を取得する.")
	@ResponseBody
	@ApiResponses(value = { @ApiResponse(code = 400, message = "パラメータ不正", response = ResponseEntityForm.class),
			@ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class),
			@ApiResponse(code = 403, message = "ロール不適合", response = ResponseEntityForm.class),
			@ApiResponse(code = 503, message = "処理エラー", response = ResponseEntityForm.class) })
	public List<ApplicationStepForm> getApplicationStepList(
			@ApiParam(required = true, value = "申請ID") @PathVariable(value = "application_id") Integer applicationId,
			@ApiParam(required = true, value = "回答通知用フラグ") @PathVariable(value = "isNotify") boolean isNotify,
			@CookieValue(value = "token", required = false) String token) {
		LOGGER.info("回答・通知可能の申請段階一覧取得 開始");
		try {

			List<ApplicationStepForm> formList = new ArrayList<ApplicationStepForm>();

			String role = AuthUtil.getRole(token);
			String departmentId = AuthUtil.getDepartmentId(token);
			if (AuthUtil.ROLE_GOVERMENT.equals(role)) {

				// パラメータチェック
				if (applicationId != null && departmentId != null && !"".equals(departmentId)) {

					// 申請区分選択画面の申請種類リスト取得
					LOGGER.debug("申請段階一覧取得 開始");
					formList = applicationService.getApplicationStepList(applicationId, isNotify, departmentId);
					LOGGER.debug("申請段階一覧取得 終了");

				} else {
					LOGGER.error("パラメータ不正");
					throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
				}
			} else if (!AuthUtil.ROLE_BUSINESS.equals(role)) {
				LOGGER.warn("不正なroleによる認証：" + role);
				throw new ResponseStatusException(HttpStatus.FORBIDDEN);
			}
			return formList;

		} catch (Exception ex) {
			LOGGER.error("回答・通知可能の申請段階一覧取得に例外発生", ex);
			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		} finally {
			LOGGER.info("回答・通知可能の申請段階一覧取得 終了");
		}
	}
	
	/**
	 * 部署一覧取得
	 * 
	 * @return 申請情報検索条件一覧取得
	 */
	@RequestMapping(value = "/departments", method = RequestMethod.GET)
	@ApiOperation(value = "部署一覧取得", notes = "部署一覧を取得する.")
	@ResponseBody
	@ApiResponses(value = { @ApiResponse(code = 503, message = "処理エラー", response = ResponseEntityForm.class) })
	public List<DepartmentForm> getDepartments() {
		LOGGER.info("部署一覧取得 開始");
		try {
			// 部署一覧
			LOGGER.debug("部署一覧取得 開始");
			List<DepartmentForm> departmentList = applicationService.getDepartmentList();
			if(excludeDepartments != null && departmentList != null) {
				String[] excludeDepartmentList = excludeDepartments.split(",");
				Iterator<DepartmentForm> iterator = departmentList.iterator();
		        while (iterator.hasNext()) {
		        	DepartmentForm departmentForm = iterator.next();
		        	boolean contains = Arrays.asList(excludeDepartmentList).contains(departmentForm.getDepartmentId());
		            if (contains) {
		                iterator.remove();
		            }
		        }
			}
			LOGGER.debug("部署一覧取得 終了");

			return departmentList;
		} catch (Exception ex) {
			LOGGER.error("部署一覧取得時に例外発生", ex);
			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		} finally {
			LOGGER.info("部署一覧取得 終了");
		}
	}
}
