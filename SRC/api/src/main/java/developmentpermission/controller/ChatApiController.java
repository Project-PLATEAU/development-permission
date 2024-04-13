package developmentpermission.controller;

import java.time.LocalDateTime;
import java.util.List;

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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import developmentpermission.entity.Answer;
import developmentpermission.form.AnswerConfirmLoginForm;
import developmentpermission.form.AnswerHistoryForm;
import developmentpermission.form.ApplicationSearchConditionForm;
import developmentpermission.form.ChatRequestForm;
import developmentpermission.form.ChatSearchResultForm;
import developmentpermission.form.InquiryFileForm;
import developmentpermission.form.MessagePostRequestForm;
import developmentpermission.form.ChatForm;
import developmentpermission.form.ChatRelatedInfoForm;
import developmentpermission.form.ResponseEntityForm;
import developmentpermission.form.ResponsibleInquiryFrom;
import developmentpermission.service.AnswerService;
import developmentpermission.service.ApplicationService;
import developmentpermission.service.ChatService;
import developmentpermission.util.AuthUtil;
import developmentpermission.util.LogUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * チャットAPIコントローラ
 */
@Api(tags = "チャット")
@RestController
@RequestMapping("/chat")
public class ChatApiController extends AbstractApiController {
	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(ChatApiController.class);

	/** チャットServiceインスタンス */
	@Autowired
	private ChatService chatService;

	/** 申請Serviceインスタンス */
	@Autowired
	private ApplicationService applicationService;

	/** 回答Serviceインスタンス */
	@Autowired
	private AnswerService answerService;

	/** チャット投稿（事業者） csvログファイルヘッダー */
	@Value("${app.csv.log.header.chat.business.message.post}")
	private String[] postBusinessChatMessageLogHeader;

	/** チャット投稿（事業者） csvログファイルパス */
	@Value("${app.csv.log.path.chat.business.message.post}")
	private String postBusinessChatMessageLogPath;

	/** チャット投稿（行政） csvログファイルヘッダー */
	@Value("${app.csv.log.header.chat.government.message.post}")
	private String[] postGovernmentChatMessageLogHeader;

	/** チャット投稿（行政） csvログファイルパス */
	@Value("${app.csv.log.path.chat.government.message.post}")
	private String postGovernmentChatMessageLogPath;

	/**
	 * チャット新規作成（事業者）
	 * 
	 * @param chatRequestForm チャットリクエスト
	 * @return チャットフォーム
	 */
	@RequestMapping(value = "/create", method = RequestMethod.PUT)
	@ApiOperation(value = "チャット新規作成", notes = "チャットを新規作成する.")
	@ResponseBody
	@ApiResponses(value = { @ApiResponse(code = 400, message = "パラメータ不正", response = ResponseEntityForm.class),
			@ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class),
			@ApiResponse(code = 403, message = "ロール不適合", response = ResponseEntityForm.class),
			@ApiResponse(code = 503, message = "チャット新規作成にエラー発生", response = ResponseEntityForm.class)})
	public ChatForm createChat(
			@ApiParam(required = true, value = "チャットリクエストフォーム") @RequestBody ChatRequestForm chatRequestForm,
			@CookieValue(value = "token", required = false) String token) {
		LOGGER.info("チャット新規作成 開始");
		try {

			// 権限チェック（事業者か否か）
			// 照合ID,パスワードチェック
			validateLoginInfo(token, chatRequestForm.getLoginId(), chatRequestForm.getPassword(),
					chatRequestForm.getAnswer().getAnswerId());

			// 回答IDに紐づくO_チャットのレコードを新規作成し、作成されたチャットIDをレスポンスとして返す
			Integer answerId = chatRequestForm.getAnswer().getAnswerId();
			if (answerId != null) {
				try {
					// チャットID
					int chatId = chatService.registerChat(answerId);

					ChatForm form = new ChatForm();
					// チャットID
					form.setChatId(chatId);
					// 回答ID
					form.setAnswerId(answerId);

					return form;

				} catch (RuntimeException ex) {
					LOGGER.error("チャット登録時にエラー発生", ex);
					throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
				}
			} else {
				LOGGER.warn("回答IDがnull");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}
		} finally {
			LOGGER.info("チャット新規作成 終了");
		}
	}

	/**
	 * チャットメッセージ一覧取得（事業者）
	 * 
	 * @param chatRequestForm チャットリクエスト
	 * @return チャットフォーム
	 */
	@RequestMapping(value = "/business/messages", method = RequestMethod.POST)
	@ApiOperation(value = "事業者向けチャットメッセージ一覧取得", notes = "事業者向けチャットメッセージを一覧取得する.")
	@ResponseBody
	@ApiResponses(value = { @ApiResponse(code = 400, message = "パラメータ不正", response = ResponseEntityForm.class),
			@ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class),
			@ApiResponse(code = 403, message = "ロール不適合", response = ResponseEntityForm.class)})
	public ChatForm getBusinessChatMessages(
			@ApiParam(required = true, value = "チャットリクエストフォーム") @RequestBody ChatRequestForm chatRequestForm,
			@CookieValue(value = "token", required = false) String token) {
		LOGGER.info("事業者向けチャットメッセージ一覧取得開始");
		try {

			// 権限チェック（事業者か否か）
			// 照合ID,パスワードチェック
			validateLoginInfo(token, chatRequestForm.getLoginId(), chatRequestForm.getPassword(),
					chatRequestForm.getAnswer().getAnswerId());

			// チャットメッセージ一覧取得
			Integer answerId = chatRequestForm.getAnswer().getAnswerId();
			if (answerId != null) {
				// 回答IDに紐づく事業者向けチャットメッセージ一覧を取得し、行政からメッセージを既読にする
				ChatForm chatMessageSearchResult = chatService.searchChatMessage(answerId);

				// メッセージ一覧を返す
				return chatMessageSearchResult;
			} else {
				LOGGER.warn("回答IDがnull");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}
		} finally {
			LOGGER.info("事業者向けチャットメッセージ一覧取得完了");
		}
	}

	/**
	 * チャットメッセージ投稿（事業者）
	 * 
	 * @param messagePostRequestForm メッセージリクエスト
	 * @return チャットフォーム
	 */
	@RequestMapping(value = "/business/message/post", method = RequestMethod.POST)
	@ApiOperation(value = "事業者チャットメッセージ投稿", notes = "事業者のチャットメッセージを投稿する.")
	@ResponseBody
	@ApiResponses(value = { @ApiResponse(code = 400, message = "パラメータ不正", response = ResponseEntityForm.class),
			@ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class),
			@ApiResponse(code = 403, message = "ロール不適合", response = ResponseEntityForm.class) ,
			@ApiResponse(code = 503, message = "チャットメッセージ投稿時にエラー発生", response = ResponseEntityForm.class) })
	public ChatForm postBusinessChatMessage(
			@ApiParam(required = true, value = "メッセージ投稿リクエストフォーム") @RequestBody MessagePostRequestForm messagePostRequestForm,
			@CookieValue(value = "token", required = false) String token) {
		LOGGER.info("事業者チャットメッセージ投稿開始");
		try {

			Integer chatId = messagePostRequestForm.getChatId();
			Integer answerId = messagePostRequestForm.getAnswerId();

			// 権限チェック（事業者か否か）
			// 照合ID,パスワードチェック
			validateLoginInfo(token, messagePostRequestForm.getLoginId(), messagePostRequestForm.getPassword(),
					answerId);

			// パラメータチェック
			if (chatId == null) {
				LOGGER.warn("チャットIDが空");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}
			if (answerId == null) {
				LOGGER.warn("回答IDが空");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}

			// メッセージ登録処理
			ChatForm form = chatService.registerMessageForBusiness(messagePostRequestForm);

			// チャット投稿 ログ出力
			try {

				// ログ出力のために、申請ID、回答IDを取得する
				Answer answer = chatService.getApplicationId(chatId);
				// アクセスID
				String accessId = AuthUtil.getAccessId(token);

				// アクセスID、申請ID、回答ID、アクセス日時
				Object[] logData = { accessId, answer.getApplicationId(), answerId,
						LogUtil.localDateTimeToString(LocalDateTime.now()) };
				LogUtil.writeLogToCsv(postBusinessChatMessageLogPath, postBusinessChatMessageLogHeader, logData);

			} catch (Exception ex) {
				ex.printStackTrace();
			}

			// 最新なメッセージ一覧を返却する
			return form;
		} catch (RuntimeException ex) {
			LOGGER.error("事業者チャットメッセージ投稿にエラー発生", ex);
			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		} finally {
			LOGGER.info("事業者チャットメッセージ投稿完了");
		}
	}

	/**
	 * チャットメッセージ一覧取得（行政）
	 * 
	 * @param ChatForm チャットリクエスト
	 * @return チャットフォーム
	 */
	@RequestMapping(value = "/government/messages", method = RequestMethod.POST)
	@ApiOperation(value = "行政向けチャットメッセージ一覧取得", notes = "行政向けチャットメッセージを一覧取得する.")
	@ResponseBody
	@ApiResponses(value = { @ApiResponse(code = 400, message = "パラメータ不正", response = ResponseEntityForm.class),
			@ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class),
			@ApiResponse(code = 403, message = "ロール不適合", response = ResponseEntityForm.class)})
	public ChatForm getGovernmentChatMessages(
			@ApiParam(required = true, value = "チャットフォーム") @RequestBody ChatForm chatForm,
			@CookieValue(value = "token", required = false) String token) {
		LOGGER.info("行政向けチャットメッセージ一覧取得開始");
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

			// パラメータチェック
			Integer chatId = chatForm.getChatId();
			if (chatId == null) {
				LOGGER.warn("チャットIDが空");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}
			String departmentId = AuthUtil.getDepartmentId(token);
			if (departmentId == null || "".equals(departmentId)) {
				// 登録データが空
				LOGGER.warn("部署IDがnullまたは空");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}

			// 回答IDに紐づく行政向けチャットメッセージ一覧を取得し、行政からメッセージを既読にする
			ChatForm chatMessageSearchResult = chatService.searchChatMessageForGovernment(chatId, departmentId);

			// メッセージ一覧を返す
			return chatMessageSearchResult;
		} finally {
			LOGGER.info("行政向けチャットメッセージ一覧取得完了");
		}
	}

	/**
	 * チャットメッセージ投稿（行政）
	 * 
	 * @param messagePostRequestForm メッセージリクエスト
	 * @return チャットフォーム
	 */
	@RequestMapping(value = "/government/message/post", method = RequestMethod.POST)
	@ApiOperation(value = "行政チャットメッセージ投稿", notes = "行政のチャットメッセージを投稿する.")
	@ResponseBody
	@ApiResponses(value = { @ApiResponse(code = 400, message = "パラメータ不正", response = ResponseEntityForm.class),
			@ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class),
			@ApiResponse(code = 403, message = "ロール不適合", response = ResponseEntityForm.class),
			@ApiResponse(code = 503, message = "行政チャットメッセージ投稿にエラー発生", response = ResponseEntityForm.class)})
	public ChatForm postGovernmentChatMessage(
			@ApiParam(required = true, value = "メッセージ投稿リクエストフォーム") @RequestBody MessagePostRequestForm messagePostRequestForm,
			@CookieValue(value = "token", required = false) String token) {
		LOGGER.info("行政チャットメッセージ投稿開始");
		try {

			// 権限チェック（行政か否か）
			LOGGER.trace("権限チェック（行政か否か） 開始");
			String role = AuthUtil.getRole(token);
			if (!AuthUtil.ROLE_GOVERMENT.equals(role)) {
				// 行政ユーザしかアクセス不可
				LOGGER.warn("ロール不適合:" + role);
				throw new ResponseStatusException(HttpStatus.FORBIDDEN);
			}
			LOGGER.trace("権限チェック（行政か否か） 終了");

			// ユーザーID
			String useId = AuthUtil.getUserId(token);
			if (useId == null || "".equals(useId)) {
				// 登録データが空
				LOGGER.warn("ユーザーIDがnullまたは空");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}
			// 部署ID
			String departmentId = AuthUtil.getDepartmentId(token);
			if (departmentId == null || "".equals(departmentId)) {
				// 登録データが空
				LOGGER.warn("部署IDがnullまたは空");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}
			// 部署名
			String departmentName = AuthUtil.getDepartmentName(token);
			if (departmentName == null || "".equals(departmentName)) {
				// 登録データが空
				LOGGER.warn("部署名がnullまたは空");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}

			// パラメータチェック
			Integer chatId = messagePostRequestForm.getChatId();
			if (chatId == null) {
				LOGGER.warn("チャットIDが空");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}
			Integer displayedMaxMessageId = messagePostRequestForm.getDisplayedMaxMessageId();
			if (displayedMaxMessageId == null) {
				LOGGER.warn("画面に表示されるの最大メッセージIDが空");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}

			// メッセージ登録処理
			ChatForm form = chatService.registerMessageForGovernment(messagePostRequestForm, useId, departmentId,
					departmentName);

			// チャット投稿 ログ出力
			try {

				// ログ出力のために、申請ID、回答IDを取得する
				Answer answer = chatService.getApplicationId(chatId);

				// アクセスID
				String accessId = AuthUtil.getAccessId(token);

				// ユーザーID
				String userId = AuthUtil.getUserId(token);
				// ユーザー氏名
				String userName = chatService.getUserName(userId);

				// アクセスID、申請ID、回答ID、操作ユーザ、操作ユーザ所属部署、アクセス日時
				Object[] logData = { accessId, answer.getApplicationId(), answer.getAnswerId(), userName,
						AuthUtil.getDepartmentName(token), LogUtil.localDateTimeToString(LocalDateTime.now()) };
				LogUtil.writeLogToCsv(postGovernmentChatMessageLogPath, postGovernmentChatMessageLogHeader, logData);

			} catch (Exception ex) {
				ex.printStackTrace();
			}

			// 最新なメッセージ一覧を返却する
			return form;

		}catch (RuntimeException ex) {
			LOGGER.error("既読済みに更新する処理にエラー発生", ex);
			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		} finally {
			LOGGER.info("行政チャットメッセージ投稿完了");
		}
	}

	/**
	 * 問合せ情報検索（行政）
	 * 
	 * @param messagePostRequestForm メッセージリクエスト
	 * @return チャットフォーム
	 */
	@RequestMapping(value = "/search", method = RequestMethod.POST)
	@ApiOperation(value = "問合せ情報検索", notes = "問合せ情報を検索する.")
	@ResponseBody
	@ApiResponses(value = { @ApiResponse(code = 400, message = "パラメータ不正", response = ResponseEntityForm.class),
			@ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class),
			@ApiResponse(code = 403, message = "ロール不適合", response = ResponseEntityForm.class) })
	public List<ChatSearchResultForm> searchChatMessage(
			@ApiParam(required = true, value = "申請情報検索条件フォーム") @RequestBody ApplicationSearchConditionForm applicationSearchConditionForm,
			@CookieValue(value = "token", required = false) String token) {
		LOGGER.info("問合せ情報検索開始");
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

			// 問合せ情報検索
			List<ChatSearchResultForm> resultList = chatService
					.searchMessagesForGovernment(applicationSearchConditionForm);

			return resultList;

		} finally {
			LOGGER.info("問合せ情報検索完了");
		}
	}

	/**
	 * 担当課の問合せ・回答一覧取得（行政）
	 * 
	 * @return ResponsibleInquiryFrom
	 */
	@RequestMapping(value = "/inquiries", method = RequestMethod.GET)
	@ApiOperation(value = "担当課問合せ・回答一覧取得", notes = "担当課の問合せ・回答一覧取得する.")
	@ResponseBody
	@ApiResponses(value = { @ApiResponse(code = 400, message = "パラメータ不正", response = ResponseEntityForm.class),
			@ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class),
			@ApiResponse(code = 403, message = "ロール不適合", response = ResponseEntityForm.class)})
	public ResponsibleInquiryFrom getResponsibleInquiries(
			@CookieValue(value = "token", required = false) String token) {
		LOGGER.info("担当課問合せ情報取得開始");
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

			// 部署ID
			String departmentId = AuthUtil.getDepartmentId(token);
			if (departmentId == null || "".equals(departmentId)) {
				// 登録データが空
				LOGGER.warn("部署IDがnullまたは空");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);	
			}

			// 部署に紐づく問合せと回答一覧を取得する
			// 回答：登録日時降順、問合せ：送信日時降順
			ResponsibleInquiryFrom from = chatService.searchAnswersAndInquiries(departmentId);

			// 検索結果を返す
			return from;
		} finally {
			LOGGER.info("担当課問合せ情報取得開始");
		}
	}

	/**
	 * 問合せの関連情報検索（行政）
	 * 
	 * @param messagePostRequestForm メッセージリクエスト
	 * @return チャットフォーム
	 */
	@RequestMapping(value = "/government/related/{chat_id}", method = RequestMethod.GET)
	@ApiOperation(value = "行政向け問合せの関連情報検索", notes = "行政向け問合せの関連情報検索を検索する.")
	@ResponseBody
	@ApiResponses(value = { @ApiResponse(code = 400, message = "パラメータ不正", response = ResponseEntityForm.class),
			@ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class),
			@ApiResponse(code = 403, message = "ロール不適合", response = ResponseEntityForm.class)})
	public ChatRelatedInfoForm searchChatRelatedInfoForGoverment(
			@ApiParam(required = true, value = "チャットID") @PathVariable(value = "chat_id") Integer chatId,
			@CookieValue(value = "token", required = false) String token) {
		LOGGER.info("行政向け問合せの関連情報検索開始");
		try {

			ChatRelatedInfoForm from = new ChatRelatedInfoForm();

			// 権限チェック（行政か否か）
			LOGGER.info("権限チェック（行政か否か） 開始");
			String role = AuthUtil.getRole(token);
			if (!AuthUtil.ROLE_GOVERMENT.equals(role)) {
				// 行政ユーザしかアクセス不可
				LOGGER.warn("ロール不適合:" + role);
				throw new ResponseStatusException(HttpStatus.FORBIDDEN);
			}
			LOGGER.info("権限チェック（行政か否か） 終了");

			// 部署
			String departmentId = AuthUtil.getDepartmentId(token);
			if (departmentId == null || "".equals(departmentId)) {
				LOGGER.warn("部署名がnullまたは空");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}

			// 問合せ関連情報検索
			from = applicationService.searchChatRelatedInfo(chatId, null, departmentId, true);

			// 回答履歴を取得
			List<AnswerHistoryForm> answerHistorys = answerService.getAnswerHistoryFromAnswerId(from.getAnswerId());
			from.setAnswerHistorys(answerHistorys);

			return from;

		} finally {
			LOGGER.info("行政向け問合せの関連情報検索完了");
		}
	}

	/**
	 * 問合せの関連情報検索（事業者）
	 * 
	 * @param messagePostRequestForm メッセージリクエスト
	 * @return チャットフォーム
	 */
	@RequestMapping(value = "/business/related/{answer_id}", method = RequestMethod.GET)
	@ApiOperation(value = "事業者向け問合せの関連情報検索", notes = "事業者向け問合せの関連情報検索を検索する.")
	@ResponseBody
	@ApiResponses(value = { @ApiResponse(code = 400, message = "パラメータ不正", response = ResponseEntityForm.class),
			@ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class),
			@ApiResponse(code = 403, message = "ロール不適合", response = ResponseEntityForm.class)})
	public ChatRelatedInfoForm searchChatRelatedInfoForBusiness(
			@ApiParam(required = true, value = "チャットID") @PathVariable(value = "answer_id") Integer answerId,
			@CookieValue(value = "token", required = false) String token) {
		LOGGER.info("事業者向け問合せの関連情報検索開始");
		try {

			ChatRelatedInfoForm from = new ChatRelatedInfoForm();

			// 権限チェック（事業者か否か）
			LOGGER.info("権限チェック（事業者か否か） 開始");
			String role = AuthUtil.getRole(token);
			if ( !AuthUtil.ROLE_BUSINESS.equals(role)) {
				// 事業者しかアクセス不可
				LOGGER.warn("ロール不適合:" + role);
				throw new ResponseStatusException(HttpStatus.FORBIDDEN);
			}
			LOGGER.info("権限チェック（事業者か否か） 終了");

			// 問合せ関連情報検索
			from = applicationService.searchChatRelatedInfo(null, answerId, null, false);

			// 事業者向け回答履歴を取得
			List<AnswerHistoryForm> answerHistorys = answerService.getAnswerHistoryFromAnswerIdForBusiness(from.getAnswerId());
			from.setAnswerHistorys(answerHistorys);

			return from;

		} finally {
			LOGGER.info("事業者向け問合せの関連情報検索完了");
		}
	}

	/**
	 * 問合せファイルアップロード
	 * 
	 * @param InquiryFileForm O_問合せファイルフォーム
	 * @return
	 */
	@RequestMapping(value = "/file/upload", method = RequestMethod.POST, consumes = { "multipart/form-data" })
	@ApiOperation(value = "問合せファイルアップロード", notes = "問合せファイルをアップロードする.")
	@ResponseBody
	@ApiResponses(value = { @ApiResponse(code = 400, message = "パラメータ不正", response = ResponseEntityForm.class),
			@ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class) })
	public ResponseEntityForm uploadInquiryrFile(
			@ApiParam(required = true, value = "問合せファイルフォーム[multipart/form-data]") @ModelAttribute InquiryFileForm inquiryFileForm,
			@CookieValue(value = "token", required = false) String token) {
		LOGGER.info("問合せファイルアップロード 開始");
		try {
			if (chatService.validateInquiryFileForm(inquiryFileForm)) {
				chatService.uploadInquiryFile(inquiryFileForm);
				ResponseEntityForm responseEntityForm = new ResponseEntityForm(HttpStatus.CREATED.value(),
						"Answer File registration successful.");
				return responseEntityForm;
			} else {
				// パラメータ不正
				LOGGER.warn("パラメータ不正");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}
		} finally {
			LOGGER.info("問合せファイルアップロード 終了");
		}
	}

	/**
	 * 問合せファイルダウンロード
	 * 
	 * @param InquiryFileForm O_問合せファイルフォーム
	 * @return 応答Entity
	 */
	@RequestMapping(value = "/file/download", method = RequestMethod.POST)
	@ApiOperation(value = "問合せファイルダウンロード", notes = "問合せファイルをダウンロードする.")
	@ResponseBody
	@ApiResponses(value = { @ApiResponse(code = 400, message = "パラメータ不正", response = ResponseEntityForm.class),
			@ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class),
			@ApiResponse(code = 403, message = "ロール不適合", response = ResponseEntityForm.class),
			@ApiResponse(code = 404, message = "ファイルが存在しない場合", response = ResponseEntityForm.class),
			@ApiResponse(code = 503, message = "問合せファイルダウンロードにエラー発生", response = ResponseEntityForm.class)})
	public ResponseEntity<Resource> downloadInquiryFile(
			@ApiParam(required = true, value = "問合せファイルフォーム") @RequestBody InquiryFileForm inquiryFileForm,
			@CookieValue(value = "token", required = false) String token) {
		LOGGER.info("問合せファイルダウンロード 開始");
		try {

			String role = AuthUtil.getRole(token);
			if (!AuthUtil.ROLE_BUSINESS.equals(role) && !AuthUtil.ROLE_GOVERMENT.equals(role)) {
				LOGGER.warn("不正なroleによる認証：" + role);
				throw new ResponseStatusException(HttpStatus.FORBIDDEN);
			}

			if (chatService.validateDownloadInquiryFile(inquiryFileForm)) {
				return chatService.downloadInquiryFile(inquiryFileForm);
			} else {
				// パラメータ不正
				LOGGER.warn("パラメータ不正");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}
		} finally {
			LOGGER.info("問合せファイルダウンロード 終了");
		}
	}

	/**
	 * 事業者側：処理前に、引数チャットを行う <br>
	 * ・権限チェック（事業者か否か） <br>
	 * ・照合ID,パスワードチェック
	 * 
	 * @param token       トークン
	 * @param collationId 申請情報の照会ID
	 * @param password    申請情報のパスワード
	 * @param answerId    回答ID
	 */
	private void validateLoginInfo(String token, String collationId, String password, Integer answerId) {
		try {
			LOGGER.warn("権限チェック 開始");

			// 権限チェック（事業者か否か）
			String role = AuthUtil.getRole(token);
			if (AuthUtil.ROLE_BUSINESS.equals(role)) {
				try {
					LOGGER.warn("照合ID,パスワードチェック 開始");

					if (collationId != null && !"".equals(collationId) //
							&& password != null && !"".equals(password)) {
						AnswerConfirmLoginForm answerConfirmLoginForm = new AnswerConfirmLoginForm(collationId,
								password, false);
						// 申請ID
						Integer applicationId = applicationService
								.getApplicationIdFromApplicantInfo(answerConfirmLoginForm);
						if (applicationId == null) {
							// 照合IDとパスワードによる認証失敗
							LOGGER.warn("照合IDとパスワードによる認証失敗：" + role);
							throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
						} else {
							if (!chatService.validateLoginInfo(applicationId, answerId)) {
								// 照合IDとパスワードによる認証失敗
								LOGGER.warn("照合IDとパスワードによる認証失敗：" + role);
								throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
							}
						}
					} else {
						// パラメータ不正
						LOGGER.warn("パラメータ不正");
						throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
					}
				} finally {
					LOGGER.warn("照合ID,パスワードチェック 終了");
				}
			} else {
				// 事業者ユーザしかアクセス不可
				LOGGER.warn("ロール不適合:" + role);
				throw new ResponseStatusException(HttpStatus.FORBIDDEN);
			}
		} finally {
			LOGGER.warn("権限チェック 終了");
		}
	}
}
