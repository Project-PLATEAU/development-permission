package developmentpermission.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
import developmentpermission.entity.Chat;
import developmentpermission.form.AnswerConfirmLoginForm;
import developmentpermission.form.AnswerFileForm;
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
import developmentpermission.repository.ChatRepository;
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

	final private Integer APPLICATION_STEP_ID1 = 1;
	final private Integer APPLICATION_STEP_ID2 = 2;
	final private Integer APPLICATION_STEP_ID3 = 3;

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
			@ApiResponse(code = 503, message = "チャット新規作成にエラー発生", response = ResponseEntityForm.class) })
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
			@ApiResponse(code = 403, message = "ロール不適合", response = ResponseEntityForm.class) })
	public List<ChatForm> getBusinessChatMessages(
			@ApiParam(required = true, value = "チャットリクエストフォーム") @RequestBody ChatRequestForm chatRequestForm,
			@CookieValue(value = "token", required = false) String token) {
		LOGGER.info("事業者向けチャットメッセージ一覧取得開始");
		try {

			// 権限チェック（事業者か否か）
			// 照合ID,パスワードチェック,申請IDチェック
			validateLoginInfo(token, chatRequestForm.getLoginId(), chatRequestForm.getPassword(),
					chatRequestForm.getAnswerId(), chatRequestForm.getApplicationId(),
					chatRequestForm.getApplicationStepId(), chatRequestForm.getDepartmentAnswerId());
			// 該当の問い合わせがあるか
			int chatId = 0;
			// パラメータのチャットIDが空(null)かどうか
			if (chatRequestForm.getChatId() == null) {
				// チャットIDが空の場合の存在チェックを行う
				if (APPLICATION_STEP_ID1.equals(chatRequestForm.getApplicationStepId())) {
					// 申請段階IDが1:事前相談の場合
					chatId = chatService.searchChatMessageExistStep1(chatRequestForm.getApplicationId(),
							chatRequestForm.getAnswerId(), chatRequestForm.getApplicationStepId());
				} else if (APPLICATION_STEP_ID2.equals(chatRequestForm.getApplicationStepId())) {
					// 申請段階IDが2:事前協議の場合
					chatId = chatService.searchChatMessageExistStep2(chatRequestForm.getApplicationId(),
							chatRequestForm.getApplicationStepId(), chatRequestForm.getDepartmentAnswerId());
				} else if (APPLICATION_STEP_ID3.equals(chatRequestForm.getApplicationStepId())) {
					// 申請段階IDが3:許可判定の場合
					chatId = chatService.searchChatMessageExistStep3(chatRequestForm.getApplicationId(),
							chatRequestForm.getApplicationStepId());
				}
			} else {
				// チャットIDが空でない場合は存在チェックを行う
				chatService.searchChatMessageExist(chatRequestForm.getChatId());
				chatId = chatRequestForm.getChatId();
			}

			// チャットメッセージ一覧取得
			List<ChatForm> reurnChatFormList = chatService
					.searchChatMessageListForApplicationId(chatRequestForm.getApplicationId(), chatId, chatRequestForm.getUnreadFlag());
			return reurnChatFormList;

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
			@ApiResponse(code = 403, message = "ロール不適合", response = ResponseEntityForm.class),
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

				// アクセスID
				String accessId = AuthUtil.getAccessId(token);

				// ログ内容を編集:アクセスID、アクセス日時、申請ID、申請種類、申請段階、回答ID、問合せ部署
				Object[] logData = chatService.editLogContentList(false, accessId, null, null, chatId);

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
			@ApiResponse(code = 403, message = "ロール不適合", response = ResponseEntityForm.class) })
	public List<ChatForm> getGovernmentChatMessages(
			@ApiParam(required = true, value = "チャットフォーム") @RequestBody ChatRequestForm chatRequestForm,
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
			Integer chatId = chatRequestForm.getChatId();
			// パラメータのチャットIDが空(null)かどうか
			if (chatId == null) {
				// チャットIDが空の場合の存在チェックを行う
				if (APPLICATION_STEP_ID1.equals(chatRequestForm.getApplicationStepId())) {
					// 申請段階IDが1:事前相談の場合
					chatId = chatService.searchChatMessageExistStep1(chatRequestForm.getApplicationId(),
							chatRequestForm.getAnswerId(), chatRequestForm.getApplicationStepId());
				} else if (APPLICATION_STEP_ID2.equals(chatRequestForm.getApplicationStepId())) {
					// 申請段階IDが2:事前協議の場合
					chatId = chatService.searchChatMessageExistStep2(chatRequestForm.getApplicationId(),
							chatRequestForm.getApplicationStepId(), chatRequestForm.getDepartmentAnswerId());
				} else if (APPLICATION_STEP_ID3.equals(chatRequestForm.getApplicationStepId())) {
					// 申請段階IDが3:許可判定の場合
					chatId = chatService.searchChatMessageExistStep3(chatRequestForm.getApplicationId(),
							chatRequestForm.getApplicationStepId());
				}
			} else {
				// チャットIDが空でない場合は存在チェックを行う
				chatService.searchChatMessageExist(chatId);
			}
			String departmentId = AuthUtil.getDepartmentId(token);
			if (departmentId == null || "".equals(departmentId)) {
				// 登録データが空
				LOGGER.warn("部署IDがnullまたは空");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}

			// 回答IDに紐づく行政向けチャットメッセージ一覧を取得し、行政からメッセージを既読にする
			List<ChatForm> chatMessageSearchResult = chatService.searchChatMessageForGovernment(chatId, departmentId, chatRequestForm.getUnreadFlag());

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
			@ApiResponse(code = 503, message = "行政チャットメッセージ投稿にエラー発生", response = ResponseEntityForm.class) })
	public List<ChatForm> postGovernmentChatMessage(
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

			// メッセージ登録処理
			List<ChatForm> form = chatService.registerMessageForGovernment(messagePostRequestForm, useId, departmentId,
					departmentName);

			// チャット投稿 ログ出力
			try {

				// アクセスID
				String accessId = AuthUtil.getAccessId(token);
				// 操作ユーザ
				String loginId = AuthUtil.getLoginId(token);

				// ログ内容を編集：アクセスID、アクセス日時、操作ユーザ、操作ユーザ所属部署、申請ID、申請種類、申請段階、回答ID、問合せ部署
				Object[] logData = chatService.editLogContentList(true, accessId, loginId, departmentName, chatId);

				LogUtil.writeLogToCsv(postGovernmentChatMessageLogPath, postGovernmentChatMessageLogHeader, logData);

			} catch (Exception ex) {
				ex.printStackTrace();
			}

			// 最新なメッセージ一覧を返却する
			return form;

		} catch (RuntimeException ex) {
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
			@ApiResponse(code = 403, message = "ロール不適合", response = ResponseEntityForm.class) })
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
			LOGGER.info("担当課問合せ情報取得終了");
		}
	}

	/**
	 * 問合せの関連情報検索（行政）
	 * 
	 * @param messagePostRequestForm メッセージリクエスト
	 * @return チャットフォーム
	 */
	@RequestMapping(value = "/government/related", method = RequestMethod.POST)
	@ApiOperation(value = "行政向け問合せの関連情報検索", notes = "行政向け問合せの関連情報検索を検索する.")
	@ResponseBody
	@ApiResponses(value = { @ApiResponse(code = 400, message = "パラメータ不正", response = ResponseEntityForm.class),
			@ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class),
			@ApiResponse(code = 403, message = "ロール不適合", response = ResponseEntityForm.class) })
	public ChatRelatedInfoForm searchChatRelatedInfoForGoverment(
			@ApiParam(required = true, value = "チャットID") @RequestBody ChatRequestForm chatRequestForm,
			@CookieValue(value = "token", required = false) String token) {
		LOGGER.info("行政向け問合せの関連情報検索開始");
		try {

			ChatRelatedInfoForm form = new ChatRelatedInfoForm();

			// 権限チェック（行政か否か）
			LOGGER.info("権限チェック（行政か否か） 開始");
			String role = AuthUtil.getRole(token);
			if (!AuthUtil.ROLE_GOVERMENT.equals(role)) {
				// 行政ユーザしかアクセス不可
				LOGGER.warn("ロール不適合:" + role);
				throw new ResponseStatusException(HttpStatus.FORBIDDEN);
			}
			LOGGER.info("権限チェック（行政か否か） 終了");

			// ユーザーID
			String useId = AuthUtil.getUserId(token);
			if (useId == null || "".equals(useId)) {
				// 登録データが空
				LOGGER.warn("ユーザーIDがnullまたは空");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}

			// 必須チェック
			if (chatRequestForm.getChatId() == null) {
				// パラメータ不正
				LOGGER.warn("パラメータ不正");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}

			List<Chat> chatList = chatService.getChatMessage(chatRequestForm.getChatId());

			// 回答一覧取得
			int answerId = 0;
			if(chatList.get(0).getAnswerId() != null ) {
				answerId = chatList.get(0).getAnswerId();
			}
			List<Answer> answerList = answerService.getAnswerMessage(chatList.get(0).getApplicationId(),
					chatList.get(0).getApplicationStepId(), answerId,
					chatRequestForm.getDepartmentAnswerId(), true);

			// 問合せ関連情報検索
			form = applicationService.searchChatRelatedInfo(answerList, true, useId, chatList.get(0).getApplicationId(),
					chatList.get(0).getApplicationStepId(),chatList.get(0).getDepartmentAnswerId());

			form.setChatId(chatRequestForm.getChatId());
			form.setApplicationId(chatRequestForm.getApplicationId());
			return form;

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
	@RequestMapping(value = "/business/related", method = RequestMethod.POST)
	@ApiOperation(value = "事業者向け問合せの関連情報検索", notes = "事業者向け問合せの関連情報検索を検索する.")
	@ResponseBody
	@ApiResponses(value = { @ApiResponse(code = 400, message = "パラメータ不正", response = ResponseEntityForm.class),
			@ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class),
			@ApiResponse(code = 403, message = "ロール不適合", response = ResponseEntityForm.class) })
	public ChatRelatedInfoForm searchChatRelatedInfoForBusiness(
			@ApiParam(required = true, value = "チャットID") @RequestBody ChatRequestForm chatRequestForm,
			@CookieValue(value = "token", required = false) String token) {
		LOGGER.info("事業者向け問合せの関連情報検索開始");
		try {

			ChatRelatedInfoForm form = new ChatRelatedInfoForm();

			// 権限チェック（事業者か否か）
			LOGGER.info("権限チェック（事業者か否か） 開始");
			String role = AuthUtil.getRole(token);
			if (!AuthUtil.ROLE_BUSINESS.equals(role)) {
				// 事業者しかアクセス不可
				LOGGER.warn("ロール不適合:" + role);
				throw new ResponseStatusException(HttpStatus.FORBIDDEN);
			}
			LOGGER.info("権限チェック（事業者か否か） 終了");

			// 必須チェック
			LOGGER.info("必須チェック 開始");
			if (chatRequestForm.getLoginId() != null && !"".equals(chatRequestForm.getLoginId()) //
					&& chatRequestForm.getPassword() != null && !"".equals(chatRequestForm.getPassword())
					&& chatRequestForm.getApplicationId() != null && chatRequestForm.getApplicationStepId() != null) {
				if (APPLICATION_STEP_ID1.equals(chatRequestForm.getApplicationStepId())) {
					if (chatRequestForm.getAnswerId() == null) {
						// パラメータ不正
						LOGGER.warn("パラメータ不正");
						throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
					}
				} else if (APPLICATION_STEP_ID2.equals(chatRequestForm.getApplicationStepId())) {
					if (chatRequestForm.getDepartmentAnswerId() == null) {
						// パラメータ不正
						LOGGER.warn("パラメータ不正");
						throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
					}
				}
			} else {
				// パラメータ不正
				LOGGER.warn("パラメータ不正");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}
			LOGGER.info("必須チェック 終了");
			// チャット情報取得
			List<Chat> chatList = new ArrayList<Chat>();
			// パラメータのチャットIDが空(null)かどうか
			if (chatRequestForm.getChatId() == null) {
				chatList = chatService.getChatMessage(chatRequestForm.getApplicationId(),
						chatRequestForm.getApplicationStepId(), chatRequestForm.getAnswerId(),
						chatRequestForm.getDepartmentAnswerId());
			} else {
				// チャットIDが空でない場合はそのチャットIDで取得を行う
				chatList = chatService.getChatMessage(chatRequestForm.getChatId());
			}

			// 回答一覧取得
			List<Answer> answerList = answerService.getAnswerMessage(chatRequestForm.getApplicationId(),
					chatRequestForm.getApplicationStepId(), chatRequestForm.getAnswerId(),
					chatRequestForm.getDepartmentAnswerId(), false);

			// 問合せ関連情報検索
			form = applicationService.searchChatRelatedInfo(answerList, false, null, chatRequestForm.getApplicationId(),
					chatRequestForm.getApplicationStepId(),chatRequestForm.getDepartmentAnswerId());

			form.setChatId(chatRequestForm.getChatId());
			form.setApplicationId(chatRequestForm.getApplicationId());

			return form;

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
			@ApiResponse(code = 503, message = "問合せファイルダウンロードにエラー発生", response = ResponseEntityForm.class) })
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
							if (answerId != null && answerId != 0 && !chatService.validateLoginInfo(applicationId, answerId)) {
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

	/**
	 * 事業者側：処理前に、引数チャットを行う <br>
	 * ・権限チェック（事業者か否か） <br>
	 * ・照合ID,パスワードチェック ・申請IDチェック
	 * 
	 * @param token              トークン
	 * @param collationId        申請情報の照会ID
	 * @param password           申請情報のパスワード
	 * @param answerId           回答ID
	 * @param applicationIdParm  申請ID
	 * @param applicationStepId  申請段階ID
	 * @param departmentAnswerId 部署回答ID
	 */
	private void validateLoginInfo(String token, String collationId, String password, Integer answerId,
			Integer applicationIdParm, Integer applicationStepId, Integer departmentAnswerId) {
		try {
			LOGGER.warn("権限チェック 開始");

			// 権限チェック（事業者か否か）
			String role = AuthUtil.getRole(token);
			if (AuthUtil.ROLE_BUSINESS.equals(role)) {
				try {
					LOGGER.warn("照合ID,パスワードチェック 開始");

					if (collationId != null && !"".equals(collationId) //
							&& password != null && !"".equals(password) && applicationIdParm != null
							&& applicationStepId != null) {
						if (APPLICATION_STEP_ID1.equals(applicationStepId)) {
							if (answerId == null) {
								// パラメータ不正
								LOGGER.warn("パラメータ不正");
								throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
							}
						} else if (APPLICATION_STEP_ID2.equals(applicationStepId)) {
							if (departmentAnswerId == null) {
								// パラメータ不正
								LOGGER.warn("パラメータ不正");
								throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
							}
						}

						AnswerConfirmLoginForm answerConfirmLoginForm = new AnswerConfirmLoginForm(collationId,
								password, false);
						// 申請ID
						Integer applicationId = applicationService
								.getApplicationIdFromApplicantInfo(answerConfirmLoginForm);
						if (applicationId == null) {
							// 照合IDとパスワードによる認証失敗
							LOGGER.warn("照合IDとパスワードによる認証失敗：" + role);
							throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
						} else if (!applicationId.equals(applicationIdParm)) {
							// 申請IDとパラメータの申請IDが一緒かどうか
							LOGGER.warn("照合IDとパスワードによる認証失敗：" + role);
							throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
						} else {
							if (answerId != null && answerId != 0 && !chatService.validateLoginInfo(applicationId, answerId)) {
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
