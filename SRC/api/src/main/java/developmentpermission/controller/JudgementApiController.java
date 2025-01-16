package developmentpermission.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import developmentpermission.form.ApplicationCategoryForm;
import developmentpermission.form.ApplicationCategorySelectionLogForm;
import developmentpermission.form.ApplicationCategorySelectionViewForm;
import developmentpermission.form.ApplyLotNumberForm;
import developmentpermission.form.CategoryJudgementLogForm;
import developmentpermission.form.GeneralConditionDiagnosisReportProgressForm;
import developmentpermission.form.GeneralConditionDiagnosisReportRequestForm;
import developmentpermission.form.GeneralConditionDiagnosisRequestForm;
import developmentpermission.form.GeneralConditionDiagnosisResultForm;
import developmentpermission.form.GeneralConditionDiagnosisResultLogForm;
import developmentpermission.form.LotNumberForm;
import developmentpermission.form.ResponseEntityForm;
import developmentpermission.form.UploadForGeneralConditionDiagnosisForm;
import developmentpermission.service.JudgementService;
import developmentpermission.util.AuthUtil;
import developmentpermission.util.LogUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * 概況診断APIコントローラ
 */
@Api(tags = "概況診断")
@RestController
@RequestMapping("/judgement")
public class JudgementApiController extends AbstractApiController {

	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(JudgementApiController.class);

	/** 区分判定Serviceインスタンス */
	@Autowired
	private JudgementService judgementService;

	/** 概況診断結果ログファイルパス */
	@Value("${app.json.log.rootPath.judgeresult}")
	private String logJudgeresultRootPath;

	/** 概況診断結果レポート（出力件数） csvログファイルヘッダー */
	@Value("${app.csv.log.header.judge.report}")
	private String[] judgeReportLogHeader;

	/** 概況診断結果レポート（出力件数） csvログファイルパス */
	@Value("${app.csv.log.path.judge.report}")
	private String judgeReportLogPath;

	/**	申請段階ID */
	final private Integer APPLICATION_STEP_ID2 = 2;
	final private Integer APPLICATION_STEP_ID3 = 3;
	
	/**
	 * 概況診断実行
	 * 
	 * @param generalConditionDiagnosisRequestForm パラメータ
	 * @return 診断結果
	 */
	@RequestMapping(value = "/execute", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "概況診断実行", notes = "概況診断を実行し結果を返す")
	@ResponseBody
	@ApiResponses(value = { @ApiResponse(code = 400, message = "パラメータ不正", response = ResponseEntityForm.class),
			@ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class),
			@ApiResponse(code = 503, message = "判定処理に失敗した場合", response = ResponseEntityForm.class) })
	public List<GeneralConditionDiagnosisResultForm> executeGeneralConditionDiagnosis(
			@ApiParam(required = true, value = "概況診断結果リクエストフォーム")@RequestBody GeneralConditionDiagnosisRequestForm generalConditionDiagnosisRequestForm, @CookieValue(value = "token", required = false) String token) {
		LOGGER.info("概況診断実行 開始");
		try {
			// パラメータチェック
			if (isValidParam(generalConditionDiagnosisRequestForm)) {
				try {
					List<GeneralConditionDiagnosisResultForm> generalConditionDiagnosisResultFormList = judgementService
							.executeGeneralConditionDiagnosis(generalConditionDiagnosisRequestForm);
					// 概況診断結果ログ出力
					if (generalConditionDiagnosisResultFormList.size() > 0) {
						int generalConditionDiagnosisId = generalConditionDiagnosisResultFormList.get(0)
								.getGeneralConditionDiagnosisResultId();
						try {
							// アクセスID
							String accessId = AuthUtil.getAccessId(token);
							final GeneralConditionDiagnosisResultLogForm logForm = generateLogForm(
									generalConditionDiagnosisRequestForm, generalConditionDiagnosisResultFormList, accessId);

							LogUtil.writeGeneralConditionDiagnosisRequestFormLogToJson(
									logJudgeresultRootPath + generalConditionDiagnosisId + ".json", logForm);
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					return generalConditionDiagnosisResultFormList;
				} catch (Exception ex) {
					LOGGER.error("概況診断処理で例外発生", ex);
					throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
				}
			} else {
				LOGGER.warn("パラメータ不正");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}
		} finally {
			LOGGER.info("概況診断実行 終了");
		}
	}
	
	/**
	 * 概況診断結果レポート生成（シミュレータ実行用）
	 * 
	 * @param generalConditionDiagnosisReportRequestForm パラメータ
	 * @param response                                   HttpServletResponse
	 */
	@RequestMapping(value = "/generate", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "概況診断結果レポート生成", notes = "概況診断結果レポートを生成する.")
	@ResponseBody
	@ApiResponses(value = { @ApiResponse(code = 400, message = "パラメータ不正", response = ResponseEntityForm.class),
			@ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class),
			@ApiResponse(code = 503, message = "帳票生成に失敗した場合", response = ResponseEntityForm.class) })
	public ResponseEntityForm generateGeneralConditionDiagnosisReport(
			@ApiParam(required = true, value = "概況診断結果レポート出力リクエストフォーム")@RequestBody GeneralConditionDiagnosisReportRequestForm generalConditionDiagnosisReportRequestForm,
			@CookieValue(value = "token", required = false) String token, HttpServletResponse response) {
		LOGGER.info("概況診断結果レポート生成 開始");
		try {
			// パラメータチェック
			if (isValidParam(generalConditionDiagnosisReportRequestForm) 
					&& generalConditionDiagnosisReportRequestForm.getFolderName() != null && !generalConditionDiagnosisReportRequestForm.getFolderName().equals("") 
						&& generalConditionDiagnosisReportRequestForm.getFileName() != null && !generalConditionDiagnosisReportRequestForm.getFileName().equals("")) {
				if (!judgementService.generateJudgeReportWorkBook(generalConditionDiagnosisReportRequestForm, response)) {
					LOGGER.warn("帳票生成失敗");
					throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
				}else {
					ResponseEntityForm responseEntityForm = new ResponseEntityForm(HttpStatus.CREATED.value(),
							"Application File registration successful.");
					return responseEntityForm;
				}
			} else {
				LOGGER.warn("パラメータ不正");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}
		} finally {
			LOGGER.info("概況診断結果レポート生成 終了");
		}
	}
	
	/**
	 * 概況診断結果レポート進捗状況取得（シミュレータ実行用）
	 * 
	 * @param generalConditionDiagnosisReportRequestFormList パラメータ
	 * @param response                                   HttpServletResponse
	 */
	@RequestMapping(value = "/progress", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "概況診断結果レポート進捗状況取得", notes = "概況診断結果レポートの進捗状況を取得する.")
	@ResponseBody
	@ApiResponses(value = { @ApiResponse(code = 400, message = "パラメータ不正", response = ResponseEntityForm.class),
			@ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class),
			@ApiResponse(code = 503, message = "進捗状況取得に失敗した場合", response = ResponseEntityForm.class) })
	public List<GeneralConditionDiagnosisReportProgressForm> getGeneralConditionDiagnosisReportProgress(
			@ApiParam(required = true, value = "概況診断結果レポート出力リクエストフォーム")@RequestBody List<GeneralConditionDiagnosisReportRequestForm> generalConditionDiagnosisReportRequestFormList,
			@CookieValue(value = "token", required = false) String token, HttpServletResponse response) {
		LOGGER.info("概況診断結果レポート進捗状況取得 開始");
		try {
			// パラメータチェック　一時フォルダのみ
			if (generalConditionDiagnosisReportRequestFormList != null) {
				return judgementService.getGeneralConditionDiagnosisReportProgress(generalConditionDiagnosisReportRequestFormList);
			} else {
				LOGGER.warn("パラメータ不正");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}
		} finally {
			LOGGER.info("概況診断結果レポート進捗状況取得 終了");
		}
	}

	/**
	 * 概況診断結果レポート出力
	 * 
	 * @param generalConditionDiagnosisReportRequestForm パラメータ
	 * @param response                                   HttpServletResponse
	 */
	@RequestMapping(value = "/report", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "概況診断結果レポート出力", notes = "概況診断結果レポートを出力する.")
	@ResponseBody
	@ApiResponses(value = { @ApiResponse(code = 400, message = "パラメータ不正", response = ResponseEntityForm.class),
			@ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class),
			@ApiResponse(code = 503, message = "帳票作成に失敗した場合", response = ResponseEntityForm.class) })
	public void exportGeneralConditionDiagnosisReport(
			@ApiParam(required = true, value = "概況診断結果レポート出力リクエストフォーム")@RequestBody GeneralConditionDiagnosisReportRequestForm generalConditionDiagnosisReportRequestForm,
			@CookieValue(value = "token", required = false) String token, HttpServletResponse response) {
		LOGGER.info("概況診断結果レポート出力 開始");
		try {
			// パラメータチェック
			if (generalConditionDiagnosisReportRequestForm!=null) {
				if (!judgementService.exportJudgeReportWorkBook(generalConditionDiagnosisReportRequestForm, response)) {
					LOGGER.warn("帳票生成失敗");
					throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
				}
			} else {
				LOGGER.warn("パラメータ不正");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}
		} finally {
			// 概況診断結果レポート（出力件数） ログ出力
			try {
				// バックグラウンドで帳票生成の場合、ログ出力をスキップ
				if (generalConditionDiagnosisReportRequestForm.getGeneralConditionDiagnosisResults() != null) {
					// アクセスID
					String accessId = AuthUtil.getAccessId(token);

					// 申請種類
					String applicationTypeName = "";
					if (generalConditionDiagnosisReportRequestForm.getApplicationTypeId() != null) {

						applicationTypeName = judgementService.getApplicationTypeName(
								generalConditionDiagnosisReportRequestForm.getApplicationTypeId());
					}

					// 申請段階
					String applicationStepName = "";
					if (generalConditionDiagnosisReportRequestForm.getApplicationStepId() != null) {

						applicationStepName = judgementService.getApplicationStepName(
								generalConditionDiagnosisReportRequestForm.getApplicationStepId());
					}

					Object[] logData = { LogUtil.localDateTimeToString(LocalDateTime.now()), accessId,
							generalConditionDiagnosisReportRequestForm.getGeneralConditionDiagnosisResults().get(0)
									.getGeneralConditionDiagnosisResultId(),
							applicationTypeName, applicationStepName };
					LogUtil.writeLogToCsv(judgeReportLogPath, judgeReportLogHeader, logData);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			LOGGER.info("概況診断結果レポート出力 終了");
		}
	}

	/**
	 * 概況診断 画像アップロード 一時フォルダー生成・取得
	 * 
	 * @return UploadForGeneralConditionDiagnosisForm 概況診断画像アップロードDTO（folderName）
	 */
	@ResponseStatus(code = HttpStatus.CREATED)
	@RequestMapping(value = "/image/upload/preparation", method = RequestMethod.GET)
	@ApiOperation(value = "概況診断 画像アップロード 一時フォルダー生成・取得", notes = "生成された一時フォルダーを取得する.")
	@ResponseBody
	@ApiResponses(value = { 
			@ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class),
			@ApiResponse(code = 403, message = "ロール不適合", response = ResponseEntityForm.class)})
	public UploadForGeneralConditionDiagnosisForm getFolderName(
			@CookieValue(value = "token", required = false) String token) {
		LOGGER.info("概況診断 画像アップロード 一時フォルダー生成・取得 開始");
		try {
			String role = AuthUtil.getRole(token);
			if (AuthUtil.ROLE_BUSINESS.equals(role) || AuthUtil.ROLE_GOVERMENT.equals(role)) {
				UploadForGeneralConditionDiagnosisForm uploadForGeneralConditionDiagnosisForm = judgementService
						.getFolderName();
				return uploadForGeneralConditionDiagnosisForm;
			} else {
				// 事業者ユーザしかアクセス不可
				LOGGER.warn("ロール不適合: " + role);
				throw new ResponseStatusException(HttpStatus.FORBIDDEN);
			}
		} finally {
			LOGGER.info("概況診断 画像アップロード 一時フォルダー生成・取得 終了");
		}
	}

	/**
	 * 概況診断画像アップロード
	 * 
	 * @param UploadForGeneralConditionDiagnosisForm 概況診断画像アップロードDTO(folderName,judgementId,image)
	 * @return ResponseEntityForm 処理結果
	 */
	@ResponseStatus(code = HttpStatus.CREATED)
	@RequestMapping(value = "/image/upload", method = RequestMethod.POST, consumes = { "multipart/form-data" })
	@ApiOperation(value = "概況診断画像アップロード", notes = "概況診断画像を一時フォルダーにアップロードする.")
	@ResponseBody
	@ApiResponses(value = { @ApiResponse(code = 400, message = "パラメータ不正", response = ResponseEntityForm.class),
			@ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class),
			@ApiResponse(code = 403, message = "ロール不適合", response = ResponseEntityForm.class) })
	public ResponseEntityForm uploadImageFile(
			@ApiParam(required = true, value = "概況診断画像アップロードフォーム[multipart/form-data]")@ModelAttribute UploadForGeneralConditionDiagnosisForm uploadForGeneralConditionDiagnosisForm,
			@CookieValue(value = "token", required = false) String token) {
		LOGGER.info("概況診断画像アップロード 開始");
		try {
			String role = AuthUtil.getRole(token);
			if (AuthUtil.ROLE_BUSINESS.equals(role) || AuthUtil.ROLE_GOVERMENT.equals(role)) {
				if (uploadForGeneralConditionDiagnosisForm.getImage() != null
						&& !uploadForGeneralConditionDiagnosisForm.getImage().isEmpty()
						&& uploadForGeneralConditionDiagnosisForm.getFolderName() != null
						&& !uploadForGeneralConditionDiagnosisForm.getFolderName().isEmpty()
						&& uploadForGeneralConditionDiagnosisForm.getCurrentSituationMapFlg() != null) {
					judgementService.uploadImageFile(uploadForGeneralConditionDiagnosisForm);
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
			LOGGER.info("概況診断画像アップロード 終了");
		}
	}

	/**
	 * パラメータ確認
	 * 
	 * @param generalConditionDiagnosisRequestForm リクエストフォーム
	 * @return 判定結果
	 */
	private boolean isValidParam(
			GeneralConditionDiagnosisReportRequestForm generalConditionDiagnosisReportRequestForm) {
		List<ApplicationCategorySelectionViewForm> applicationCategories = generalConditionDiagnosisReportRequestForm
				.getApplicationCategories();
		List<GeneralConditionDiagnosisResultForm> generalConditionDiagnosisResults = generalConditionDiagnosisReportRequestForm
				.getGeneralConditionDiagnosisResults();
		List<LotNumberForm> lotNumbers = generalConditionDiagnosisReportRequestForm.getLotNumbers();
		List<ApplyLotNumberForm> applyLotNumbers = generalConditionDiagnosisReportRequestForm.getApplyLotNumbers();
		if (applicationCategories == null || applicationCategories.size() == 0
				|| generalConditionDiagnosisResults == null || generalConditionDiagnosisResults.size() == 0
				|| ((lotNumbers == null || lotNumbers.size() == 0) && (applyLotNumbers == null || applyLotNumbers.size() == 0))) {
			LOGGER.warn("申請区分選択一覧または概況診断結果一覧または申請地番一覧がnullまたは空");
			return false;
		}
		return true;
	}

	/**
	 * パラメータ確認
	 * 
	 * @param generalConditionDiagnosisRequestForm リクエストフォーム
	 * @return 判定結果
	 */
	private boolean isValidParam(GeneralConditionDiagnosisRequestForm generalConditionDiagnosisRequestForm) {
		// 許可判定の場合、申請区分がないため、必須チェックを行わなない
		if (generalConditionDiagnosisRequestForm.getApplicationStepId() != 3) {
			List<ApplicationCategorySelectionViewForm> applicationCategories = generalConditionDiagnosisRequestForm
					.getApplicationCategories();
			if (applicationCategories == null || applicationCategories.size() == 0) {
				LOGGER.warn("申請区分一覧がnullまたは空");
				return false;
			}
		}

		// 初回申請の場合、申請地番が必須項目です
		if (generalConditionDiagnosisRequestForm.getApplicationId() == null) {
			List<LotNumberForm> lotNumbers = generalConditionDiagnosisRequestForm.getLotNumbers();
			if (lotNumbers == null || lotNumbers.size() == 0) {
				LOGGER.warn("申請地番一覧がnullまたは空");
				return false;
			}
		}

		if (generalConditionDiagnosisRequestForm.getApplicationTypeId() == null
				|| generalConditionDiagnosisRequestForm.getApplicationStepId() == null) {
			LOGGER.warn("申請種類IDまたは申請段階IDがnull");
			return false;
		}

		if ((APPLICATION_STEP_ID2.equals(generalConditionDiagnosisRequestForm.getApplicationStepId())
				|| APPLICATION_STEP_ID3.equals(generalConditionDiagnosisRequestForm.getApplicationStepId()))
				&& generalConditionDiagnosisRequestForm.getApplicationId() == null) {
			LOGGER.warn("申請段階が事前協議または、許可判定の場合、申請IDがnull");
			return false;
		}

		return true;
	}

	/**
	 * 概況診断結果ログフォームに整形
	 * 
	 * @param generalConditionDiagnosisRequestForm
	 * @param generalConditionDiagnosisResultFormList
	  * @param accessId
	 * @return
	 */
	private GeneralConditionDiagnosisResultLogForm generateLogForm(
			GeneralConditionDiagnosisRequestForm generalConditionDiagnosisRequestForm,
			List<GeneralConditionDiagnosisResultForm> generalConditionDiagnosisResultFormList, 
			String accessId) {
		GeneralConditionDiagnosisResultLogForm logForm = new GeneralConditionDiagnosisResultLogForm();
		try {
			// 概況診断結果ID
			logForm.set概況診断結果ID(generalConditionDiagnosisResultFormList.get(0).getGeneralConditionDiagnosisResultId());
			// 日時
			final String executeDateTime = LogUtil.localDateTimeToString(LocalDateTime.now());
			logForm.setアクセス日時(executeDateTime);
			// アクセスID
			logForm.setアクセスID(accessId);

			// 申請種類
			if (generalConditionDiagnosisRequestForm.getApplicationTypeId() == null) {
				logForm.set申請種類("");
			} else {

				String applicationTypeName = judgementService
						.getApplicationTypeName(generalConditionDiagnosisRequestForm.getApplicationTypeId());
				logForm.set申請種類(applicationTypeName);
			}

			// 申請段階
			if (generalConditionDiagnosisRequestForm.getApplicationStepId() == null) {
				logForm.set申請段階("");
			} else {

				String applicationStepName = judgementService
						.getApplicationStepName(generalConditionDiagnosisRequestForm.getApplicationStepId());
				logForm.set申請段階(applicationStepName);
			}

			// 地番
			List<String> lotNumbers = new ArrayList<String>();
			if (generalConditionDiagnosisRequestForm.getApplicationId() == null) {
				for (LotNumberForm aLotNumber : generalConditionDiagnosisRequestForm.getLotNumbers()) {

					lotNumbers.add(aLotNumber.getDistrictName() + aLotNumber.getChiban());
				}
			} else {
				for (ApplyLotNumberForm aLotNumber : generalConditionDiagnosisRequestForm.getApplyLotNumbers()) {

					lotNumbers.add(aLotNumber.getLot_numbers());
				}
			}
			logForm.set申請地番一覧(lotNumbers);
			// 申請区分
			List<ApplicationCategorySelectionLogForm> applicationCategorySelectionLogFormList = new ArrayList<ApplicationCategorySelectionLogForm>();
			for (ApplicationCategorySelectionViewForm viewForm : generalConditionDiagnosisRequestForm
					.getApplicationCategories()) {
				ApplicationCategorySelectionLogForm aApplicationCategorySelectionLogForm = new ApplicationCategorySelectionLogForm();
				aApplicationCategorySelectionLogForm.set画面ID(viewForm.getScreenId());
				aApplicationCategorySelectionLogForm.set申請区分選択項目(viewForm.getTitle());
				List<String> categoryList = new ArrayList<String>();
				for (ApplicationCategoryForm applicationCategory : viewForm.getApplicationCategory()) {
					categoryList.add(applicationCategory.getContent());
				}
				aApplicationCategorySelectionLogForm.set申請区分(categoryList);
				applicationCategorySelectionLogFormList.add(aApplicationCategorySelectionLogForm);
			}
			logForm.set申請区分選択一覧(applicationCategorySelectionLogFormList);
			// 概況診断結果
			List<CategoryJudgementLogForm> pushFormList = new ArrayList<CategoryJudgementLogForm>();
			for (GeneralConditionDiagnosisResultForm aGeneralConditionDiagnosisResultForm : generalConditionDiagnosisResultFormList) {
				CategoryJudgementLogForm pushForm = new CategoryJudgementLogForm();
				pushForm.set区分判定ID(aGeneralConditionDiagnosisResultForm.getJudgementId());
				pushForm.setタイトル(aGeneralConditionDiagnosisResultForm.getTitle());
				pushForm.set概要(aGeneralConditionDiagnosisResultForm.getSummary());
				pushForm.set文言(aGeneralConditionDiagnosisResultForm.getDescription());
				pushFormList.add(pushForm);
			}
			logForm.set概況診断結果一覧(pushFormList);
		} catch (Exception e) {
			LOGGER.error("概況診断結果ログフォーム生成に失敗", e);
		}
		return logForm;
	}
}
