package developmentpermission.controller;
/**
 * 最終提出書類APIコントローラ
 */

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import developmentpermission.form.AnswerConfirmLoginForm;
import developmentpermission.form.DevelopmentDocumentFileForm;
import developmentpermission.form.ResponseEntityForm;
import developmentpermission.service.ApplicationService;
import developmentpermission.service.DevelopmentRegisterService;
import developmentpermission.util.AuthUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(tags = "最終提出書類")
@RestController
@RequestMapping("/developmentdocument")
public class DevelopmentDocumentApiController extends AbstractApiController {
	
	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(DevelopmentDocumentApiController.class);

	/** 申請Serviceインスタンス */
	@Autowired
	private ApplicationService applicationService;
	
	/** 開発管理簿Serviceインスタンス */
	@Autowired
	private DevelopmentRegisterService developmentRegisterService;
	
	/**
	 * 最終提出書類ダウンロード
	 * 
	 * @param developmentDocumentFileForm O_開発登録簿フォーム
	 * @return 応答Entity zip形式
	 */
	@RequestMapping(value = "/file/download", method = RequestMethod.POST)
	@ApiOperation(value = "最終提出書類ダウンロード", notes = "最終提出書類をダウンロードする.")
	@ResponseBody
	@ApiResponses(value = { @ApiResponse(code = 400, message = "パラメータ不正", response = ResponseEntityForm.class),
			@ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class),
			@ApiResponse(code = 403, message = "ロール不適合", response = ResponseEntityForm.class),
			@ApiResponse(code = 404, message = "ファイルが存在しない場合", response = ResponseEntityForm.class) })
	public ResponseEntity<Resource> downloadDevelopmentDocument(
			@ApiParam(required = true, value = "申請ファイルフォーム") @RequestBody DevelopmentDocumentFileForm developmentDocumentFileForm,
			@CookieValue(value = "token", required = false) String token) {
		LOGGER.info("最終提出書類ダウンロード 開始");
		try {
			String role = AuthUtil.getRole(token);

			if (AuthUtil.ROLE_BUSINESS.equals(role)) {
				String id = developmentDocumentFileForm.getLoginId();
				String password = developmentDocumentFileForm.getPassword();
				if (id != null && !"".equals(id) //
						&& password != null && !"".equals(password)) {
					AnswerConfirmLoginForm answerConfirmLoginForm = new AnswerConfirmLoginForm(id, password, false);
					// 申請ID
					Integer applicationId = applicationService
							.getApplicationIdFromApplicantInfo(answerConfirmLoginForm);
					if (applicationId == null || !applicationId.equals(developmentDocumentFileForm.getApplicationId())) {
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
			
			// 申請IDのステータスが許可判定完了の場合のみ処理を実施
			if(!developmentRegisterService.checkStatusPermissionCompleted(
					developmentDocumentFileForm.getApplicationId())) {
				LOGGER.warn("許可判定未完了");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}			
			// 申請ID,開発登録簿マスタIDでO_開発登録簿からファイルパスを取得し、以下のファイルをzip化してダウンロードする
			// ファイルパスは開発登録簿のルートディレクトリ以下が入る想定
			String zipFilePath = developmentRegisterService.createDevelopmentRegisterZipFile(
					developmentDocumentFileForm.getApplicationId(),
					developmentDocumentFileForm.getDevelopmentDocumentId());
			if(Objects.isNull(zipFilePath)) {
				LOGGER.error("最終提出書類ZIPファイル 失敗");
				throw new RuntimeException("最終提出書類ZIPファイル 失敗");	
			}
			// 最終提出書類ダウンロード（ZIPファイル）
			File zipFile = new File(zipFilePath);
			InputStreamResource resource = new InputStreamResource(new FileInputStream(zipFile));
			String encodedFilename = URLEncoder.encode(zipFile.getName(), StandardCharsets.UTF_8.toString()).replace("+", "%20");
	        return ResponseEntity.ok()
	                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFilename + "\"")
	                .contentType(MediaType.APPLICATION_OCTET_STREAM)
	                .contentLength(zipFile.length())
	                .body(resource);
	        
		} catch (Exception e) {
			LOGGER.error("最終提出書類ダウンロード 失敗", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
		} finally {
			LOGGER.info("最終提出書類ダウンロード 終了");
		}
	}
	
	/**
	 * 最終提出書類一覧取得
	 * 
	 * @param developmentDocumentFileForm O_開発登録簿フォーム
	 * @return developmentDocumentFileForm 最終提出書類一覧
	 */
	@RequestMapping(value = "/documents", method = RequestMethod.POST)
	@ApiOperation(value = "最終提出書類一覧取得", notes = "最終提出書類一覧を取得する.")
	@ResponseBody
	@ApiResponses(value = { @ApiResponse(code = 400, message = "パラメータ不正", response = ResponseEntityForm.class),
			@ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class),
			@ApiResponse(code = 403, message = "ロール不適合", response = ResponseEntityForm.class),
			@ApiResponse(code = 404, message = "ファイルが存在しない場合", response = ResponseEntityForm.class) })
	public List<DevelopmentDocumentFileForm> getDevelopmentDocumentList(
			@ApiParam(required = true, value = "申請ファイルフォーム") @RequestBody DevelopmentDocumentFileForm developmentDocumentFileForm,
			@CookieValue(value = "token", required = false) String token) {
		LOGGER.info("最終提出書類一覧取得 開始");
		try {
			String role = AuthUtil.getRole(token);
			if (AuthUtil.ROLE_BUSINESS.equals(role)) {
				// IDパスワードチェック（申請ファイルダウンロードと同様）
			} else if (!AuthUtil.ROLE_GOVERMENT.equals(role)) {
				LOGGER.warn("不正なroleによる認証：" + role);
				throw new ResponseStatusException(HttpStatus.FORBIDDEN);
			}			
			// 申請IDのステータスが許可判定完了の場合のみ処理を実施
			if(!developmentRegisterService.checkStatusPermissionCompleted(
					developmentDocumentFileForm.getApplicationId())) {
				LOGGER.warn("許可判定未完了");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}
			// 申請IDに紐づく最終提出書類一覧をO_開発登録簿から取得
			if (Objects.nonNull(developmentDocumentFileForm.getApplicationId())) {
				List<DevelopmentDocumentFileForm> developmentDocumentFileFormList = developmentRegisterService
						.getDevelopmentDocumentFileFormList(
								developmentDocumentFileForm.getApplicationId());
				return developmentDocumentFileFormList;
			} else {
				LOGGER.warn("パラメータが空またはnull");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}

		} finally {
			LOGGER.info("最終提出書類一覧取得 終了");
		}
	}
}
