package developmentpermission.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import developmentpermission.form.ApplicationCategorySelectionViewForm;
import developmentpermission.form.JudgementTypeForm;
import developmentpermission.form.ResponseEntityForm;
import developmentpermission.service.CategoryService;
import developmentpermission.util.AuthUtil;
import developmentpermission.util.LogUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * 申請区分APIコントローラ
 */
@Api(tags = "申請区分")
@RestController
@RequestMapping("/category")
public class CategoryApiController extends AbstractApiController {

	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(CategoryApiController.class);

	/** 申請区分項目取得 csvログファイルヘッダー */
	@Value("${app.csv.log.header.category.views}")
	private String[] categoryViewsLogHeader;

	/** 申請区分項目取得 csvログファイルパス */
	@Value("${app.csv.log.path.category.views}")
	private String categoryViewsLogPath;

	/**
	 * 申請区分Serviceインスタンス
	 */
	@Autowired
	private CategoryService categoryService;

	/**
	 * 申請区分画面一覧取得
	 * 
	 * @return 申請区分画面一覧
	 */
	@RequestMapping(value = "/views", method = RequestMethod.GET)
	@ApiOperation(value = "申請区分画面一覧取得", notes = "申請区分画面一覧を選択肢含め取得する.")
	@ResponseBody
	@ApiResponses(value = { @ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class) })
	public List<ApplicationCategorySelectionViewForm> getCategories(@CookieValue(value = "token", required = false) String token) {
		LOGGER.info("申請区分画面一覧取得 開始");
		try {
			List<ApplicationCategorySelectionViewForm> applicationCategoryViewFormList = categoryService
					.getApplicationCategorySelectionViewList();
			
			try {
				// アクセスID
				String accessId = AuthUtil.getAccessId(token);
				// アクセスID、アクセス日時
				Object[] logData = { accessId, LogUtil.localDateTimeToString(LocalDateTime.now()) };
				LogUtil.writeLogToCsv(categoryViewsLogPath, categoryViewsLogHeader, logData);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return applicationCategoryViewFormList;
		} finally {
			LOGGER.info("申請区分画面一覧取得 終了");
		}
	}
	
	/**
	 * 概況診断タイプ一覧取得
	 * 
	 * @return 概況診断タイプ一覧取得
	 */
	@RequestMapping(value = "/judgementTypes", method = RequestMethod.GET)
	@ApiOperation(value = "概況診断タイプ一覧取得", notes = "概況診断タイプ一覧を取得する.")
	@ResponseBody
	@ApiResponses(value = { @ApiResponse(code = 503, message = "処理エラー", response = ResponseEntityForm.class)})
	public List<JudgementTypeForm> getJudgementTypes() {
		LOGGER.info("概況診断タイプ一覧取得 開始");
		try {
			List<JudgementTypeForm> formList = new ArrayList<JudgementTypeForm>();
 
			// 申請区分選択画面の概況診断タイプリスト取得
			LOGGER.debug("概況診断タイプ一覧取得 開始");
			formList = authenticationService.getJudgementTypeList();
			LOGGER.debug("概況診断タイプ一覧取得 終了");
 
			return formList;
		} catch (Exception ex) {
			LOGGER.error("概況診断タイプ一覧取得に例外発生", ex);
			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		} finally {
			LOGGER.info("概況診断タイプ一覧取得 終了");
		}
	}
}
