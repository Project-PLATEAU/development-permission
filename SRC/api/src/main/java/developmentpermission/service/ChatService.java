package developmentpermission.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import developmentpermission.dao.AnswerDao;
import developmentpermission.dao.ApplicationDao;
import developmentpermission.dao.ChatDao;
import developmentpermission.entity.Answer;
import developmentpermission.entity.ApplicantInformation;
import developmentpermission.entity.Application;
import developmentpermission.entity.ApplicationAnswerSearchResult;
import developmentpermission.entity.ApplicationStep;
import developmentpermission.entity.ApplicationType;
import developmentpermission.entity.ApplicationVersionInformation;
import developmentpermission.entity.ApplyLotNumber;
import developmentpermission.entity.Authority;
import developmentpermission.entity.CategoryJudgement;
import developmentpermission.entity.CategoryJudgementAuthority;
import developmentpermission.entity.CategoryJudgementResult;
import developmentpermission.entity.Chat;
import developmentpermission.entity.Department;
import developmentpermission.entity.DepartmentAnswer;
import developmentpermission.entity.GovernmentUser;
import developmentpermission.entity.InquiryAddress;
import developmentpermission.entity.InquiryFile;
import developmentpermission.entity.LotNumberAndDistrict;
import developmentpermission.entity.LotNumberSearchResultDefinition;
import developmentpermission.entity.Message;
import developmentpermission.form.ApplicationSearchConditionForm;
import developmentpermission.form.ApplicationStepForm;
import developmentpermission.form.ApplyAnswerSearchResultForm;
import developmentpermission.form.ApplyLotNumberForm;
import developmentpermission.form.ChatForm;
import developmentpermission.form.ChatRequestForm;
import developmentpermission.form.ChatSearchResultForm;
import developmentpermission.form.DepartmentForm;
import developmentpermission.form.GovernmentUserForm;
import developmentpermission.form.InquiryAddressForm;
import developmentpermission.form.InquiryFileForm;
import developmentpermission.form.LotNumberForm;
import developmentpermission.form.MessageForm;
import developmentpermission.form.MessagePostRequestForm;
import developmentpermission.form.ResponsibleInquiryFrom;
import developmentpermission.repository.AnswerDepartmentRepository;
import developmentpermission.repository.AnswerRepository;
import developmentpermission.repository.ApplicantInformationRepository;
import developmentpermission.repository.ApplicationRepository;
import developmentpermission.repository.ApplicationStepRepository;
import developmentpermission.repository.ApplicationTypeRepository;
import developmentpermission.repository.ApplicationVersionInformationRepository;
import developmentpermission.repository.AuthorityRepository;
import developmentpermission.repository.CategoryJudgementRepository;
import developmentpermission.repository.ChatRepository;
import developmentpermission.repository.DepartmentAnswerRepository;
import developmentpermission.repository.DepartmentRepository;
import developmentpermission.repository.GovernmentUserRepository;
import developmentpermission.repository.InquiryAddressRepository;
import developmentpermission.repository.InquiryFileRepository;
import developmentpermission.repository.JudgementAuthorityRepository;
import developmentpermission.repository.JudgementResultRepository;
import developmentpermission.repository.LotNumberSearchResultDefinitionRepository;
import developmentpermission.repository.MssageRepository;
import developmentpermission.repository.jdbc.ChatJdbc;
import developmentpermission.repository.jdbc.InquiryAddressJdbc;
import developmentpermission.repository.jdbc.InquiryFileJdbc;
import developmentpermission.repository.jdbc.MessageJdbc;
import developmentpermission.util.AuthUtil;
import developmentpermission.util.ExportJudgeForm;
import developmentpermission.util.LogUtil;
import developmentpermission.util.MailMessageUtil;
import developmentpermission.util.model.MailItem;

/**
 * チャットServiceクラス
 */
@Service
@Transactional
public class ChatService extends AbstractJudgementService {

	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(ChatService.class);

	/** O_チャットJDBC */
	@Autowired
	private ChatJdbc chattionJdbc;

	/** O_メッセージJDBC */
	@Autowired
	private MessageJdbc messageJdbc;

	/** O_問合せファイルJDBC */
	@Autowired
	private InquiryFileJdbc inquiryFileJdbc;

	/** O_問合せ宛先JDBC */
	@Autowired
	private InquiryAddressJdbc inquiryAddressJdbc;

	/** O_チャットRepositoryインタフェース */
	@Autowired
	private ChatRepository chatRepository;

	/** O_メッセージRepositoryインタフェース */
	@Autowired
	private MssageRepository mssageRepository;

	/** M_行政ユーザRepositoryインタフェース */
	@Autowired
	private GovernmentUserRepository governmentUserRepository;

	/** M_部署Repositoryインタフェース */
	@Autowired
	private DepartmentRepository departmentRepository;
	
	/** O_部署回答Repositoryインタフェース */
	@Autowired
	private DepartmentAnswerRepository departmentAnswerRepository;

	/** M_申請段階Repositoryインタフェース */
	@Autowired
	private ApplicationStepRepository applicationStepRepository;

	/** O_回答Repositoryインタフェース */
	@Autowired
	private AnswerRepository answerRepository;

	/** M_区分判定Repositoryインタフェース */
	@Autowired
	private CategoryJudgementRepository categoryJudgementRepository;

	/** M_地番検索結果定義Repositoryインタフェース */
	@Autowired
	private LotNumberSearchResultDefinitionRepository lotNumberSearchResultDefinitionRepository;

	/** O_問合せファイルRepositoryインタフェース */
	@Autowired
	private InquiryFileRepository inquiryFileRepository;

	/** O_申請Repositoryインスタンス */
	@Autowired
	private ApplicationRepository applicationRepository;
	
	/** O_申請者情報Repositoryインタフェース */
	@Autowired
	private ApplicantInformationRepository applicantInformationRepository;

	/** O_問合せ宛先Repositoryインタフェース */
	@Autowired
	private InquiryAddressRepository inquiryAddressRepository;

	/** O_部署回答Repositoryインタフェース */
	@Autowired
	private AnswerDepartmentRepository answerDepartmentRepository;

	/** M_申請区分Repositoryインタフェース */
	@Autowired
	private JudgementAuthorityRepository judgementAuthorityRepository;

	/** M_判定結果Repositoryインタフェース */
	@Autowired
	private JudgementResultRepository judgementResultRepository;

	/** 申請版情報表示用文字列 */
	@Value("${app.application.versioninformation.text}")
	protected String versionInformationText;

	/** 申請版情報置換文字列 */
	@Value("${app.application.versioninformation.replacetext}")
	protected String versionInformationReplaceText;

	/** 回答.期日X日前 */
	@Value("${app.answer.deadlineXDaysAgo}")
	private int deadlineXDaysAgo;

	/** 問合せ検索 対象列出力内容（事前協議） */
	@Value("${app.chat.search.title.step2}")
	private String step2Title;

	/** 問合せ検索 対象列出力内容（許可判定） */
	@Value("${app.chat.search.title.step3}")
	private String step3Title;
	/** 問合せ検索 対象列出力内容（事前協議）置換文字列 */
	@Value("${app.chat.search.title.step2.replaceText}")
	private String step2TitleReplaceText;

	/** M_権限Repositoryインタフェース */
	@Autowired
	private AuthorityRepository authorityRepository;
	
	/** M_申請種類Repositoryインスタンス */
	@Autowired
	private ApplicationTypeRepository applicationTypeRepository;

	/** O_申請版情報Repositoryインスタンス */
	@Autowired
	private ApplicationVersionInformationRepository applicationVersionInformationRepository;
	
	/**
	 * チャット登録
	 * 
	 * @param answerId 回答ID
	 * @return
	 */
	public int registerChat(int answerId) {
		LOGGER.debug("チャット登録 開始");
		LOGGER.trace("回答ID: " + answerId);
		try {
			// チャットID
			int chatId = chattionJdbc.insert(answerId);
			return chatId;
		} finally {
			LOGGER.debug("チャット登録 終了");
		}
	}

	/**
	 * 事業者から メッセージ登録
	 * 
	 * @param chatId      チャットID
	 * @param messageText メッセージ本文
	 * @return
	 */
	public ChatForm registerMessageForBusiness(MessagePostRequestForm form) {
		LOGGER.debug("事業者から メッセージ登録 開始");
		Integer chatId = form.getChatId();
		Integer answerId = form.getAnswerId();
		String messageText = form.getMessage().getMessageText();
		try {
			
			Integer applicationStepId = form.getApplicationStepId();
			DepartmentAnswer departmentId = new DepartmentAnswer();
			List<CategoryJudgementAuthority> departmentIdList = new ArrayList<CategoryJudgementAuthority>();
			List<Authority> authorityList = new ArrayList<Authority>();
			
			LOGGER.trace("部署ID取得 開始");
			LOGGER.trace("申請段階ID：" + applicationStepId);
			LOGGER.trace("チャットID：" + chatId);
			AnswerDao answerDao = new AnswerDao(emf);
			if (APPLICATION_STEP_ID_1.equals(applicationStepId)) {
				departmentIdList = answerDao.getJudgementAuthorityList(chatId);
			} else if (APPLICATION_STEP_ID_2.equals(applicationStepId)) {
				departmentId = answerDepartmentRepository.findByDepartmentId(chatId);
			} else if (APPLICATION_STEP_ID_3.equals(applicationStepId)) {
				authorityList = authorityRepository.findStep3AnswerDepartment();
			}
			LOGGER.trace("部署ID取得 終了");

			Message message = new Message();
			message.setChatId(chatId);
			message.setMessageText(messageText);
			// メッセージタイプ：1：事業者→行政
			message.setMessageType(MESSAGE_TYPE_BUSINESS_TO_GOVERNMENT);
			// 事業者である場合、固定「-1]を設定
			message.setSenderId("-1");

			// O_メッセージ登録
			LOGGER.trace("O_メッセージ登録 開始");
			int messageId = messageJdbc.insert(message);
			LOGGER.trace("O_メッセージ登録 終了 メッセージID：" + messageId);

			InquiryAddress inquiryAddress = new InquiryAddress();
			inquiryAddress.setMessageId(messageId);
			LOGGER.trace("O_問合せ宛先 開始");
			if (APPLICATION_STEP_ID_1.equals(applicationStepId)) {
				for (CategoryJudgementAuthority department : departmentIdList) {
					inquiryAddress.setDepartmentId(department.getDepartmentId());
					int inquiryAddressId = inquiryAddressJdbc.insert(inquiryAddress);
					LOGGER.trace("問合せ宛先ID：" + inquiryAddressId);
				}
				LOGGER.trace("O_問合せ宛先 終了");
			} else if (APPLICATION_STEP_ID_2.equals(applicationStepId)) {
				inquiryAddress.setDepartmentId(departmentId.getDepartmentId());
				int inquiryAddressId = inquiryAddressJdbc.insert(inquiryAddress);
				LOGGER.trace("O_問合せ宛先 終了  問合せ宛先ID：" + inquiryAddressId);
			} else if (APPLICATION_STEP_ID_3.equals(applicationStepId)) {
				for (Authority authority : authorityList) {
					inquiryAddress.setDepartmentId(authority.getDepartmentId());
					int inquiryAddressId = inquiryAddressJdbc.insert(inquiryAddress);
					LOGGER.trace("問合せ宛先ID：" + inquiryAddressId);
				}
				LOGGER.trace("O_問合せ宛先 終了");
			}

			// 初回投稿であるか判断
			List<Message> messageList = mssageRepository.findByChatIdForBusiness(chatId);
			boolean isFirst = true;
			for (Message m : messageList) {
				if (m.getMessageId() != messageId) {
					isFirst = false;
					break;
				}
			}

			// O_チャット更新
			LOGGER.trace("O_チャット更新 開始");
			if (chattionJdbc.updateEstablishmentPostDatetime(chatId, isFirst) != 1) {
				LOGGER.warn("O_チャット更新不正");
				throw new RuntimeException("事業者投稿日時の更新に失敗");
			}
			LOGGER.trace("O_チャット更新 終了");

			// 最新なメッセージ一覧を取得する
			ChatForm chatForm = searchChatMessage(answerId,chatId);

			// 行政に事業者からの問合せ通知を送付する
			if (APPLICATION_STEP_ID_1.equals(applicationStepId)) {
				for (CategoryJudgementAuthority department : departmentIdList) {
					sendInquiryMailFromBusinessUser(form, department.getDepartmentId(), messageId);
				}
			} else if (APPLICATION_STEP_ID_2.equals(applicationStepId)) {
				sendInquiryMailFromBusinessUser(form, departmentId.getDepartmentId(), messageId);
			} else if (APPLICATION_STEP_ID_3.equals(applicationStepId)) {
				for (Authority authority : authorityList) {
					sendInquiryMailFromBusinessUser(form, authority.getDepartmentId(), messageId);
				}
			}

			return chatForm;
		} finally {
			LOGGER.debug("事業者から メッセージ登録 終了");
		}
	}

	/**
	 * 事業者向けチャットメッセージ一覧取得
	 * 
	 * @param answerId 回答ID
	 * @param chatId チャットID
	 * @return 事業者向けチャットメッセージ一覧
	 */
	public ChatForm searchChatMessage(Integer answerId,Integer chatId) {

		LOGGER.debug("事業者向けチャットメッセージ一覧取得 開始");
		LOGGER.trace("回答ID: " + answerId);
		ChatForm form = new ChatForm();
		try {
			// 回答ID紐づくチャットを取得する
			List<Chat> chatList = chatRepository.findByAnswerId(answerId);
			// チャットが既に存在する場合
			if(chatId != null && chatId.intValue() != 0) {
				chatList = chatRepository.findByChatId(chatId);
			}
			if (chatList.size() == 0) {
				// 回答IDに紐づくO_チャットのレコードがない場合新規作成する
				chatId = chattionJdbc.insert(answerId);
			} else {
				chatId = chatList.get(0).getChatId();
			}
			// 回答ID
			form.setAnswerId(answerId);
			// チャットID
			form.setChatId(chatId);

			// チャットIDに紐づくメッセージを取得する
			List<Message> messageList = mssageRepository.findByChatIdForBusiness(chatId);

			List<MessageForm> messageFormList = new ArrayList<MessageForm>();
			for (Message message : messageList) {
				MessageForm messageForm = getMessageFormFromEntity(message);
				messageFormList.add(messageForm);
				// 行政から未読のメッセージを既読に更新する
				if (message.getMessageType() == MESSAGE_TYPE_GOVERNMENT_TO_BUSINESS && !message.getReadFlag()) {
					if (messageJdbc.updateReadFlag(message.getMessageId()) != 1) {
						LOGGER.warn("メッセージ情報の更新件数不正");
						throw new RuntimeException("メッセージ情報の更新に失敗");
					}

					// 行政から事業者へ回答する時に、宛先は一つしか選択しないので、更新条件に部署IDを入れなくてもいい
					for (InquiryAddressForm inquiryAddressForm : messageForm.getInquiryAddressForms()) {
						if (inquiryAddressJdbc.updateReadFlag(inquiryAddressForm.getInquiryAddressId()) != 1) {
							LOGGER.warn("問合せ宛先情報の更新件数不正");
							throw new RuntimeException("問合せ宛先情報の更新に失敗");
						}
					}
				}
			}
			// メッセージ一覧
			form.setMessages(messageFormList);
			return form;
		} finally {
			LOGGER.debug("事業者向けチャットメッセージ一覧取得 終了");
		}

	}

	/**
	 * 事業者向けチャットメッセージ存在確認
	 * 
	 * @param chatId チャットID
	 */
	public void searchChatMessageExist(int chatId) {

		LOGGER.debug("事業者向けチャットメッセージ件数チェック 開始");
		LOGGER.trace("チャットID： " + chatId);
		try {
			// O_チャットを取得する
			List<Chat> chatList = chatRepository.findByChatId(chatId);

			if (chatList.size() < 1) {
				LOGGER.warn("O_チャットの値が取得できない。");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}
		} finally {
			LOGGER.debug("事業者向けチャットメッセージ件数チェック 終了");
		}
	}

	/**
	 * 事業者向けチャットメッセージ存在確認(チャットID無し、申請段階が事前相談の場合)
	 * 
	 * @param applicationId     申請ID
	 * @param answerId          回答ID
	 * @param applicationStepId 申請段階ID
	 * @return chatId チャットID
	 */
	public int searchChatMessageExistStep1(int applicationId, int answerId, int applicationStepId) {

		LOGGER.debug("事業者向けチャットメッセージ件数チェック 開始");
		LOGGER.trace("申請ID： " + applicationId);
		LOGGER.trace("回答ID： " + answerId);
		LOGGER.trace("申請段階ID： " + applicationStepId);
		try {
			// O_チャットを取得する
			List<Chat> chatList = chatRepository.findByApplicationStepId1(applicationId, answerId, applicationStepId);
			int chatId = 0;
			if (chatList.size() < 1) {
				LOGGER.warn("O_チャットの値が取得できない。追加を行う。");
				if(applicationId < 1 || applicationStepId < 1 || answerId < 1) {
					LOGGER.warn("登録値が不正");
					throw new RuntimeException("チャットの作成に失敗");
				}
				chatId = chattionJdbc.insertStep1(answerId, applicationId, applicationStepId);
			} else {
				chatId = chatList.get(0).getChatId();
			}
			return chatId;
		} finally {
			LOGGER.debug("事業者向けチャットメッセージ件数チェック 終了");
		}
	}

	/**
	 * 事業者向けチャットメッセージ存在確認(チャットID無し、申請段階が事前協議の場合)
	 * 
	 * @param applicationId      申請ID
	 * @param applicationStepId  申請段階ID
	 * @param departmentAnswerId 部署回答ID
	 * @return chatId チャットID
	 */
	public int searchChatMessageExistStep2(int applicationId, int applicationStepId, int departmentAnswerId) {

		LOGGER.debug("事業者向けチャットメッセージ件数チェック 開始");
		LOGGER.trace("申請ID： " + applicationId);
		LOGGER.trace("申請段階ID： " + applicationStepId);
		LOGGER.trace("部署回答ID： " + departmentAnswerId);
		try {
			// O_チャットを取得する
			List<Chat> chatList = chatRepository.findByApplicationStepId2(applicationId, applicationStepId,
					departmentAnswerId);
			int chatId = 0;
			if (chatList.size() < 1) {
				LOGGER.warn("O_チャットの値が取得できない。追加を行う。");
				if(applicationId < 1 || applicationStepId < 1 || departmentAnswerId < 1) {
					LOGGER.warn("登録値が不正");
					throw new RuntimeException("チャットの作成に失敗");
				}
				chatId = chattionJdbc.insertStep2(applicationId, applicationStepId, departmentAnswerId);
			} else {
				chatId = chatList.get(0).getChatId();
			}
			return chatId;
		} finally {
			LOGGER.debug("事業者向けチャットメッセージ件数チェック 終了");
		}
	}

	/**
	 * 事業者向けチャットメッセージ存在確認(チャットID無し、申請段階が許可判定の場合)
	 * 
	 * @param applicationId     申請ID
	 * @param applicationStepId 申請段階ID
	 * @return chatId チャットID
	 */
	public int searchChatMessageExistStep3(int applicationId, int applicationStepId) {

		LOGGER.debug("事業者向けチャットメッセージ件数チェック 開始");
		LOGGER.trace("申請ID： " + applicationId);
		LOGGER.trace("申請段階ID： " + applicationStepId);
		try {
			// O_チャットを取得する
			List<Chat> chatList = chatRepository.findByApplicationStepId3(applicationId, applicationStepId);
			int chatId = 0;
			if (chatList.size() < 1) {
				LOGGER.warn("O_チャットの値が取得できない。追加を行う。");
				if(applicationId < 1 || applicationStepId < 1) {
					LOGGER.warn("登録値が不正");
					throw new RuntimeException("チャットの作成に失敗");
				}
				chatId = chattionJdbc.insertStep3(applicationId, applicationStepId);
			} else {
				chatId = chatList.get(0).getChatId();
			}
			return chatId;
		} finally {
			LOGGER.debug("事業者向けチャットメッセージ件数チェック 終了");
		}
	}

	/**
	 * 事業者向けチャットメッセージ取得(チャットID無し,検索時)
	 * 
	 * @param applicationId      申請ID
	 * @param applicationStepId  申請段階ID
	 * @param answerId           回答ID
	 * @param departmentAnswerId 部署回答ID
	 * @return chatId チャットID
	 */
	public List<Chat> getChatMessage(Integer applicationId, Integer applicationStepId, Integer answerId, Integer departmentAnswerId) {

		LOGGER.debug("事業者向けチャットメッセージ取得 開始");
		LOGGER.trace("申請ID： " + applicationId);
		LOGGER.trace("申請段階ID： " + applicationStepId);
		LOGGER.trace("回答ID： " + answerId);
		LOGGER.trace("部署回答ID： " + departmentAnswerId);
		try {
			List<Chat> chatList = new ArrayList<Chat>();
			if (APPLICATION_STEP_ID_1.equals(applicationStepId)) {
				chatList = chatRepository.findByApplicationStepId1(applicationId, answerId, applicationStepId);
			} else if (APPLICATION_STEP_ID_2.equals(applicationStepId)) {
				chatList = chatRepository.findByApplicationStepId2(applicationId, applicationStepId,
						departmentAnswerId);
			} else if (APPLICATION_STEP_ID_3.equals(applicationStepId)) {
				chatList = chatRepository.findByApplicationStepId3(applicationId, applicationStepId);
			}

			if (chatList.size() < 1) {
				LOGGER.warn("O_チャットの値が取得できない。");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}
			return chatList;
		} finally {
			LOGGER.debug("事業者向けチャットメッセージ取得 終了");
		}
	}

	/**
	 * 事業者向けチャットメッセージ取得(chatIdで)
	 * 
	 * @param chatId チャットID
	 */
	public List<Chat> getChatMessage(int chatId) {

		LOGGER.debug("事業者向けチャットメッセージ取得(chatId) 開始");
		LOGGER.trace("チャットID： " + chatId);
		try {
			// O_チャットを取得する
			List<Chat> chatList = chatRepository.findByChatId(chatId);

			if (chatList.size() < 1) {
				LOGGER.warn("O_チャットの値が取得できない。");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}
			return chatList;
		} finally {
			LOGGER.debug("事業者向けチャットメッセージ件数チェック 終了");
		}
	}

	/**
	 * 事業者向けチャットメッセージ一覧取得(申請ID)
	 * 
	 * @param applicationId 申請ID
	 * @return 事業者向けチャットメッセージ一覧
	 */
	public List<ChatForm> searchChatMessageListForApplicationId(int applicationId, int chatId, Boolean unreadFlag) {
		
		LOGGER.debug("事業者向けチャットメッセージ一覧取得 開始");
		LOGGER.trace("申請ID: " + applicationId);
		List<ChatForm> form = new ArrayList<ChatForm>();
		try {
			// 申請ID紐づくチャットを取得する
			List<Chat> chatList = chatRepository.findByApplicationId(applicationId);
			ApplicationDao applicationDao = new ApplicationDao(emf);
			for (Chat chat : chatList) {
				ChatForm chatForm = new ChatForm();
				// チャットID
				chatForm.setChatId(chat.getChatId());
				// 回答ID
				chatForm.setAnswerId(chat.getAnswerId());
				// 申請ID
				chatForm.setApplicationId(chat.getApplicationId());
				// 申請段階
				ApplicationStepForm stepForm = new ApplicationStepForm();
				// 申請段階ID
				stepForm.setApplicationStepId(chat.getApplicationStepId());
				// 申請IDを元にM_申請段階から申請段階名を取得
				stepForm.setApplicationStepName(applicationStepRepository
						.findByApplicationStep(chat.getApplicationStepId()).getApplicationStepName());
				chatForm.setApplicationStep(stepForm);
				// 担当部署・タイトル
				DepartmentForm departmentForm = new DepartmentForm();
				// チャットIDに紐づくメッセージを取得する
				List<Message> messageList = mssageRepository.findByChatIdForBusiness(chat.getChatId());
				// チャットに紐づく回答一覧を取得する
				List<Answer> answers = applicationDao.getAllAnswerListForBusiness(chat.getApplicationId(),chat.getApplicationStepId(),chat.getDepartmentAnswerId());
				if (APPLICATION_STEP_ID_1.equals(chat.getApplicationStepId())) {
					// 事前相談
					List<Answer> answer = answers.stream().filter(_answer -> _answer.getAnswerId() != null && _answer.getAnswerId().equals(chat.getAnswerId())).collect(Collectors.toList()); 
					// 回答がない or メッセージがないかつ回答未通知の場合はスキップ
					if(answer == null || answer.size() < 1 || ((messageList==null || messageList.size() < 1) && (answer.get(0).getNotifiedFlag() == null || !answer.get(0).getNotifiedFlag()))) {
						continue;
					}
					// 担当部署
					List<CategoryJudgementAuthority> judgementDepartment = judgementAuthorityRepository
							.getJudgementAuthorityList(answer.get(0).getJudgementId());
					List<Department> departmentList = departmentRepository
							.getDepartmentListById(judgementDepartment.get(0).getDepartmentId());
					// 部署UID
					departmentForm.setDepartmentId(departmentList.get(0).getDepartmentId());
					// 部署名
					departmentForm.setDepartmentName(departmentList.get(0).getDepartmentName());
					// 選択状態
					departmentForm.setChecked(false);
					// メールアドレス
					departmentForm.setMailAddress(departmentList.get(0).getMailAddress());
					// タイトル
					List<CategoryJudgementResult> judgementTitle = judgementResultRepository
							.getJudgementTitleByJudgementId(answer.get(0).getJudgementId().toString());
					chatForm.setTitle(judgementTitle.get(0).getTitle());
				} else if (APPLICATION_STEP_ID_2.equals(chat.getApplicationStepId())) {
					// 事前協議
					List<Answer> answer = answers.stream().filter(_answer -> _answer.getNotifiedFlag() != null && _answer.getNotifiedFlag()).collect(Collectors.toList()); 
					// 通知済みの回答がないかつメッセージが無い場合はスキップ
					if((answer == null || answer.size() < 1) && (messageList == null || messageList.size() < 1)) {
						continue;
					}
					// 担当部署
					List<Department> departmentList = departmentRepository.getDepartmentListById(
							answerDepartmentRepository.findByAnswerId(chat.getDepartmentAnswerId()).getDepartmentId());
					// 部署UID
					departmentForm.setDepartmentId(departmentList.get(0).getDepartmentId());
					// 部署名
					departmentForm.setDepartmentName(departmentList.get(0).getDepartmentName());
					// 選択状態
					departmentForm.setChecked(false);
					// メールアドレス
					departmentForm.setMailAddress(departmentList.get(0).getMailAddress());
					// タイトル
					chatForm.setTitle(departmentList.get(0).getDepartmentName());
					// 部署回答ID
					chatForm.setDepartmentAnswerId(chat.getDepartmentAnswerId());
				} else if (APPLICATION_STEP_ID_3.equals(chat.getApplicationStepId())) {
					// 許可判定
					List<Answer> answer = answers.stream().filter(_answer -> _answer.getNotifiedFlag() != null && _answer.getNotifiedFlag()).collect(Collectors.toList()); 
					// 通知済みの回答がないかつメッセージが無い場合はスキップ
					if((answer == null || answer.size() < 1) && (messageList == null || messageList.size() < 1)) {
						continue;
					}
					// タイトル
					chatForm.setTitle("許可判定");
				}
				chatForm.setDepartment(departmentForm);
				// メッセージ一覧
				List<MessageForm> messageFormList = new ArrayList<MessageForm>();
				for (Message message : messageList) {
					MessageForm messageForm = getMessageFormFromEntity(message);
					messageFormList.add(messageForm);
					// 行政から未読のメッセージを既読に更新する(cahtIdがパラメータであるもののみ)
					if ((unreadFlag == null || unreadFlag == false) && chatId == chat.getChatId()) {
						if (MESSAGE_TYPE_GOVERNMENT_TO_BUSINESS.equals(message.getMessageType()) && !message.getReadFlag()) {
							if (messageJdbc.updateReadFlag(message.getMessageId()) != 1) {
								LOGGER.warn("メッセージ情報の更新件数不正");
								throw new RuntimeException("メッセージ情報の更新に失敗");
							}

							// 行政から事業者へ回答する時に、宛先は一つしか選択しないので、更新条件に部署IDを入れなくてもいい
							for (InquiryAddressForm inquiryAddressForm : messageForm.getInquiryAddressForms()) {
								if (inquiryAddressJdbc.updateReadFlag(inquiryAddressForm.getInquiryAddressId()) != 1) {
									LOGGER.warn("問合せ宛先情報の更新件数不正");
									throw new RuntimeException("問合せ宛先情報の更新に失敗");
								}
							}
						}
					}
				}
				// メッセージ一覧
				chatForm.setMessages(messageFormList);
				// 選択状態
				if (chatId == chat.getChatId()) {
					chatForm.setChecked(true);
				} else {
					chatForm.setChecked(false);
				}
				form.add(chatForm);
			}
			return form;
		} finally {
			LOGGER.debug("事業者向けチャットメッセージ一覧取得 終了");
		}

	}

	/**
	 * 行政向けチャットメッセージ一覧取得
	 * 
	 * @param chatId       チャットID
	 * @param departmentId ログインしている行政ユーザーの部署ID
	 * @return 行政向けチャットメッセージ一覧
	 */
	public List<ChatForm> searchChatMessageForGovernment(int chatId, String departmentId, Boolean unreadFlag) {

		LOGGER.debug("行政向けチャットメッセージ一覧取得 開始");
		LOGGER.trace("チャットID： " + chatId);
		List<ChatForm> form = new ArrayList<ChatForm>();
		try {
			// O_チャットを取得する
			List<Chat> chatList = chatRepository.findByChatId(chatId);

			if (chatList.size() > 0) {

				Integer applicationId = chatList.get(0).getApplicationId();
				// 申請ID紐づくチャットを取得する
				List<Chat> chatLists = chatRepository.findByApplicationId(applicationId);

				// 取得したチャットリストをループしてメッセージ詳細を取得する
				for (Chat chat : chatLists) {
					ChatForm chatForm = new ChatForm();
					// チャットID
					chatForm.setChatId(chat.getChatId());
					// 回答ID
					chatForm.setAnswerId(chat.getAnswerId());
					// 申請ID
					chatForm.setApplicationId(chat.getApplicationId());
					// 申請段階
					ApplicationStepForm stepForm = new ApplicationStepForm();
					// 申請段階ID
					stepForm.setApplicationStepId(chat.getApplicationStepId());
					// 申請IDを元にM_申請段階から申請段階名を取得
					stepForm.setApplicationStepName(applicationStepRepository
							.findByApplicationStep(chat.getApplicationStepId()).getApplicationStepName());
					chatForm.setApplicationStep(stepForm);
					// 担当部署・タイトル
					DepartmentForm departmentForm = new DepartmentForm();
					if (APPLICATION_STEP_ID_1.equals(chat.getApplicationStepId())) {
						// 事前相談
						// 担当部署
						List<Answer> answer = answerRepository.findByAnswerId(chat.getAnswerId());
						List<CategoryJudgementAuthority> judgementDepartment = judgementAuthorityRepository
								.getJudgementAuthorityList(answer.get(0).getJudgementId());
						List<Department> departmentList = departmentRepository
								.getDepartmentListById(judgementDepartment.get(0).getDepartmentId());
						// 部署UID
						departmentForm.setDepartmentId(departmentList.get(0).getDepartmentId());
						// 部署名
						departmentForm.setDepartmentName(departmentList.get(0).getDepartmentName());
						// 選択状態
						departmentForm.setChecked(false);
						// メールアドレス
						departmentForm.setMailAddress(departmentList.get(0).getMailAddress());
						// タイトル
						List<CategoryJudgementResult> judgementTitle = judgementResultRepository
								.getJudgementTitleByJudgementId(answer.get(0).getJudgementId().toString());
						chatForm.setTitle(judgementTitle.get(0).getTitle());
					} else if (APPLICATION_STEP_ID_2.equals(chat.getApplicationStepId())) {
						// 事前協議
						// 担当部署
						List<Department> departmentList = departmentRepository
								.getDepartmentListById(answerDepartmentRepository
										.findByAnswerId(chat.getDepartmentAnswerId()).getDepartmentId());
						// 部署UID
						departmentForm.setDepartmentId(departmentList.get(0).getDepartmentId());
						// 部署名
						departmentForm.setDepartmentName(departmentList.get(0).getDepartmentName());
						// 選択状態
						departmentForm.setChecked(false);
						// メールアドレス
						departmentForm.setMailAddress(departmentList.get(0).getMailAddress());
						// タイトル
						chatForm.setTitle(departmentList.get(0).getDepartmentName());
						// 部署回答ID
						chatForm.setDepartmentAnswerId(chat.getDepartmentAnswerId());
					} else if (APPLICATION_STEP_ID_3.equals(chat.getApplicationStepId())) {
						// 許可判定
						// タイトル
						chatForm.setTitle("許可判定");
					}
					chatForm.setDepartment(departmentForm);
					// チャットIDに紐づくメッセージを取得する
					List<Message> messageList = mssageRepository.findByChatId(chat.getChatId());
					List<MessageForm> messageFormList = new ArrayList<MessageForm>();
					for (Message message : messageList) {
						MessageForm messageForm = getMessageFormFromEntity(message);
						messageFormList.add(messageForm);

						// 自分部署担当しているの未読の問合せ宛先ID
						Integer inquiryAddressId = null;
						// 既読になる担当部署数
						int alreadyReadCount = 0;
						for (InquiryAddressForm inquiryAddressForm : messageForm.getInquiryAddressForms()) {
							// 既読になる担当部署数カウント
							if (inquiryAddressForm.getReadFlag()) {
								alreadyReadCount++;
							}

							// 自分部署担当しているの未読の問合せ宛先ID
							if (!inquiryAddressForm.getReadFlag()
									&& departmentId.equals(inquiryAddressForm.getDepartment().getDepartmentId())) {
								inquiryAddressId = inquiryAddressForm.getInquiryAddressId();
							}
						}

						if(unreadFlag == null || unreadFlag == false) {
							// 「O_問合せ宛先」の既読フラグを既読済みに更新する
							// 既読に更新する(cahtIdがパラメータであるもののみ)
							if (chatId == chat.getChatId()) {
								if (inquiryAddressId != null) {
									if (inquiryAddressJdbc.updateReadFlag(inquiryAddressId) != 1) {
										LOGGER.warn("問合せ宛先情報の更新件数不正");
										throw new RuntimeException("問合せ宛先情報の更新に失敗");
									}
									alreadyReadCount++;
								}
							}
							// 「O_メッセージ」の既読フラグを既読に更新する
							if (!message.getReadFlag() && messageForm.getInquiryAddressForms().size() == alreadyReadCount) {
								if (messageJdbc.updateReadFlag(message.getMessageId()) != 1) {
									LOGGER.warn("メッセージ情報の更新件数不正");
									throw new RuntimeException("メッセージ情報の更新に失敗");
								}
							}
							// 許可判定の場合
							if (APPLICATION_STEP_ID_3.equals(chat.getApplicationStepId())) {
								// 許可判定
								if (MESSAGE_TYPE_BUSINESS_TO_GOVERNMENT.equals(message.getMessageType())
										&& !message.getReadFlag()) {
									if (messageJdbc.updateReadFlag(message.getMessageId()) != 1) {
										LOGGER.warn("メッセージ情報の更新件数不正");
										throw new RuntimeException("メッセージ情報の更新に失敗");
									}
								}
							}
						}
					}
					chatForm.setMessages(messageFormList);
					// 選択状態
					if (chatId == chat.getChatId()) {
						chatForm.setChecked(true);
					} else {
						chatForm.setChecked(false);
					}
					form.add(chatForm);
				}
			} else {
				LOGGER.warn("O_チャットの値が取得できない。");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}
			return form;
		} finally {
			LOGGER.debug("行政向けチャットメッセージ一覧取得 終了");
		}
	}

	/**
	 * 行政から メッセージ登録
	 * 
	 * @param form           メッセージ情報
	 * @param useId          ログインしている行政ユーザーのユーザーID
	 * @param departmentId   ログインしている行政ユーザーの部署ID
	 * @param departmentName ログインしている行政ユーザーの部署名
	 * @return 最新なチャットメッセージ
	 */
	public List<ChatForm> registerMessageForGovernment(MessagePostRequestForm form, String useId, String departmentId,
			String departmentName) {
		LOGGER.debug("行政から メッセージ登録 開始");
		try {
			Message message = new Message();
			message.setChatId(form.getChatId());
			message.setMessageText(form.getMessage().getMessageText());
			List<DepartmentForm> toDepartmentList = form.getToDepartments();
			// メッセージタイプ
			// 事業者へ投稿する場合、一つ宛先しか選択できないため、一番目でメッセージタイプを判定する
			String toDepartmentId = toDepartmentList.get(0).getDepartmentId();
			if ("-1".equals(toDepartmentId)) {
				// 宛先部署が事業者（-1）場合、「2：行政→事業者」を設定
				message.setMessageType(MESSAGE_TYPE_GOVERNMENT_TO_BUSINESS);
				message.setToDepartmentId("-1");
			} else {
				// 宛先部署がある場合、「3：行政→行政」を設定
				message.setMessageType(MESSAGE_TYPE_GOVERNMENT_TO_GOVERNMENT);
			}
			// 送信者ID
			message.setSenderId(useId);

			// O_メッセージ登録
			LOGGER.trace("O_メッセージ登録 開始");
			int messageId = messageJdbc.insert(message);
			LOGGER.trace("O_メッセージ登録 終了 メッセージID：" + messageId);

			List<String> toDepartmentIdList = new ArrayList<String>();
			for (DepartmentForm department : toDepartmentList) {
				InquiryAddress inquiryAddress = new InquiryAddress();
				inquiryAddress.setMessageId(messageId);
				inquiryAddress.setDepartmentId(department.getDepartmentId());

				LOGGER.trace("O_問合せ宛先 開始");
				int inquiryAddressId = inquiryAddressJdbc.insert(inquiryAddress);
				LOGGER.trace("O_問合せ宛先 終了  問合せ宛先ID：" + inquiryAddressId);
				toDepartmentIdList.add(department.getDepartmentId());
			}

			// O_メッセージ更新
			LOGGER.trace("O_メッセージ更新 開始");
			List<Message> messageList = mssageRepository.findByChatId(form.getChatId());
			for (Message messageObj : messageList) {
				// 画面に表示されたメッセージだけを回答済みに更新する
				int displayedMaxMessageId = 1;
				if (form.getDisplayedMaxMessageId() != null) {
					displayedMaxMessageId = form.getDisplayedMaxMessageId();
				}

				// 投稿以前のメッセージの回答済みフラグを更新
				if (displayedMaxMessageId >= messageObj.getMessageId()) {

					MessageForm messageForm = getMessageFormFromEntity(messageObj);

					// 事業者に回答する投稿メッセージの場合、送信者が事業者のメッセージを回答済みにする
					if (MESSAGE_TYPE_GOVERNMENT_TO_BUSINESS.equals(message.getMessageType())) {

						// 自分の部署担当かつ、送信者IDの部署IDが投稿メッセージの宛先部署IDの未回答メッセージを回答済みに更新する
						if (!messageObj.getAnswerCompleteFlag() && "-1".equals(messageObj.getSenderId())) {

							if (messageJdbc.updateAnswerCompleteFlag(messageObj.getMessageId()) != 1) {
								LOGGER.warn("メッセージ情報の更新件数不正");
								throw new RuntimeException("メッセージ情報の更新に失敗");
							}

							// 事業者から投稿する時に、宛先は一つしか選できないので、更新条件に部署IDを入れなくてもいい
							for (InquiryAddressForm inquiryAddressForm : messageForm.getInquiryAddressForms()) {
								if (inquiryAddressJdbc
										.updateAnswerCompleteFlag(inquiryAddressForm.getInquiryAddressId()) != 1) {
									LOGGER.warn("問合せ宛先情報の更新件数不正");
									throw new RuntimeException("問合せ宛先情報の更新に失敗");
								}
							}
						}
					} else {
						// 行政に回答する投稿メッセージの場合、
						// 送信者IDの部署ID取得
						List<GovernmentUser> governmentUserList = governmentUserRepository
								.findByUserId(messageObj.getSenderId());
						if (governmentUserList.size() > 0) {
							String senderDepartmentId = governmentUserList.get(0).getDepartmentId();

							// メッセージの送信者の部署は今回投稿の宛先リストに存在する場合、回答済みフラグ更新を行う
							if (toDepartmentIdList.contains(senderDepartmentId)) {

								// 自分部署担当しているの未回答の問合せ宛先ID
								Integer inquiryAddressId = null;
								// 回答済みになる担当部署数
								int answerCompleteCount = 0;
								for (InquiryAddressForm inquiryAddressForm : messageForm.getInquiryAddressForms()) {
									// 回答済みになる担当部署数カウント
									if (inquiryAddressForm.getAnswerCompleteFlag()) {
										answerCompleteCount++;
									}

									// 自分部署担当しているの未回答の問合せ宛先ID
									if (!inquiryAddressForm.getAnswerCompleteFlag() && departmentId
											.equals(inquiryAddressForm.getDepartment().getDepartmentId())) {
										inquiryAddressId = inquiryAddressForm.getInquiryAddressId();
									}
								}

								// 「O_問合せ宛先」の回答済みフラグを回答済みに更新する
								if (inquiryAddressId != null) {
									if (inquiryAddressJdbc.updateAnswerCompleteFlag(inquiryAddressId) != 1) {
										LOGGER.warn("問合せ宛先情報の更新件数不正");
										throw new RuntimeException("問合せ宛先情報の更新に失敗");
									}
									answerCompleteCount++;
								}

								// 回答済みになる担当部署数= 担当部署数 かつ、 未回答 の場合、メッセージの回答済みフラグを回答済みに更新する
								if (!messageObj.getAnswerCompleteFlag()
										&& messageForm.getInquiryAddressForms().size() == answerCompleteCount) {
									if (messageJdbc.updateAnswerCompleteFlag(messageObj.getMessageId()) != 1) {
										LOGGER.warn("メッセージ情報の更新件数不正");
										throw new RuntimeException("メッセージ情報の更新に失敗");
									}
								}
							}
						}
					}
				}
			}
			LOGGER.trace("O_メッセージ更新 終了");

			// メッセージタイプが「2：行政→事業者」の場合、行政回答日時を更新する
			if (MESSAGE_TYPE_GOVERNMENT_TO_BUSINESS.equals(message.getMessageType())) {
				// O_チャット更新
				LOGGER.trace("O_チャット更新 開始");
				if (chattionJdbc.updateGovernmentAnswerDatetime(form.getChatId(), useId) != 1) {
					LOGGER.warn("O_チャット更新不正");
					throw new RuntimeException("行政回答日時の更新に失敗");
				}
				LOGGER.trace("O_チャット更新 終了");
			}

			// 行政向け 最新なメッセージ一覧を取得する
			List<ChatForm> chatForm = searchChatMessageForGovernment(form.getChatId(), departmentId, false);

			// メール通信
			sendInquiryMailFromGovernmentUser(form, departmentName, message.getMessageType(), messageId);

			return chatForm;
		} finally {
			LOGGER.debug("行政から メッセージ登録 終了");
		}
	}

	/**
	 * 問合せ情報検索（行政）
	 * 
	 * @param form    メッセージ情報
	 * @param loginId ログインしている行政ユーザーのID
	 * @return
	 */
	public List<ChatSearchResultForm> searchMessagesForGovernment(
			ApplicationSearchConditionForm applicationSearchConditionForm) {
		LOGGER.debug("問合せ情報検索 開始");

		try {
			List<ChatSearchResultForm> formList = new ArrayList<ChatSearchResultForm>();

			ApplicationDao applicationDao = new ApplicationDao(emf);
			AnswerDao answerDao = new AnswerDao(emf);
			ChatDao chatDao = new ChatDao(emf);

			DateTimeFormatter datetimeformatter = DateTimeFormatter.ofPattern("yyyy/MM/dd/ HH:mm");

			// O_申請検索
			LOGGER.trace("O_申請検索 開始");
			// ステータスと担当課、回答者、申請段階は申請情報取得と異なるため、一旦除く
			ApplicationSearchConditionForm condition = new ApplicationSearchConditionForm();
			// 申請者情報
			condition.setApplicantInformationItemForm(applicationSearchConditionForm.getApplicantInformationItemForm());
			// 申請区分
			condition.setApplicationCategories(applicationSearchConditionForm.getApplicationCategories());
			// 申請種類
			condition.setApplicationTypes(applicationSearchConditionForm.getApplicationTypes());

			// 申請追加情報
			condition.setApplicantAddInformationItemForm(
					applicationSearchConditionForm.getApplicantAddInformationItemForm());
			// 条文ステータス
			condition.setItemAnswerStatus(applicationSearchConditionForm.getItemAnswerStatus());
			// 申請ID
			condition.setApplicationId(applicationSearchConditionForm.getApplicationId());

			List<Application> applicationList = applicationDao.searchApplication(condition);
			LOGGER.trace("O_申請検索 終了");

			// 申請IDのリストを作成する
			List<Integer> applicationIdList = new ArrayList<Integer>();
			for (Application application : applicationList) {
				applicationIdList.add(application.getApplicationId());
			}
			LOGGER.trace("O_チャット検索 開始");
			List<Chat> chats = chatDao.searchChat(applicationSearchConditionForm, applicationIdList);
			LOGGER.trace("O_チャット検索 終了");

			LOGGER.debug("検索結果編集 開始");
			for (Chat chat : chats) {
				ChatSearchResultForm form = new ChatSearchResultForm();
				// 申請ID取得
				form.setApplicationId(chat.getApplicationId());
				form.setChatId(chat.getChatId());

				// ステータス
				List<Message> inquiryMessages = mssageRepository.findByChatIdAndMessageType(chat.getChatId(),
						MESSAGE_TYPE_BUSINESS_TO_GOVERNMENT);
				if (inquiryMessages.size() > 0) {
					Message message = inquiryMessages.get(0);
					form.setStatus(getStatus(message.getReadFlag(), message.getAnswerCompleteFlag()));
				} else {
					// 事業者→行政のメッセージが取得できない場合、行政から問い合わせを投げて事業者未回答の状態とみなす
					form.setStatus(2);
				}

				// 回答対象、担当課: 申請段階により取得先を切替
				final Integer applcationStepId = chat.getApplicationStepId();

				String deparmentName = "";
				String title = "";
				if (applcationStepId.equals(APPLICATION_STEP_ID_1)) {
					// 事前相談: 回答IDより回答対象、担当課を取得
					List<CategoryJudgementResult> categoryJudgements = answerDao.getJudgementResultByAnswerId(chat.getAnswerId());

					if (categoryJudgements.size() == 0) {
						LOGGER.warn("M_区分判定の値が取得できない 回答ID: " + chat.getAnswerId());
					} else {
						title = categoryJudgements.get(0).getTitle();
						List<CategoryJudgementAuthority> authorityList = judgementAuthorityRepository
								.getJudgementAuthorityList(categoryJudgements.get(0).getJudgementItemId());
						if (categoryJudgements.size() == 0) {
							LOGGER.warn("M_権限の値が取得できない 回答ID: " + chat.getAnswerId());
						} else {
							final List<String> departmentIdList = new ArrayList<String>();
							for (CategoryJudgementAuthority authority : authorityList) {
								departmentIdList.add(authority.getDepartmentId());
							}
							List<Department> toDepartmentList = departmentRepository
									.getDepartmentListByIdList(departmentIdList);
							final List<String> departmentNameList = new ArrayList<String>();
							for (Department aDepartment : toDepartmentList) {
								departmentNameList.add(aDepartment.getDepartmentName());
							}
							deparmentName = String.join(",", departmentNameList);
						}

					}
				} else if (applcationStepId.equals(APPLICATION_STEP_ID_2)) {
					// 事前協議: 回答部署IDより担当課を取得、問合せ対象は一律で設定
					final Integer departmentAnswerId = chat.getDepartmentAnswerId();
					List<Department> toDepartmentList = chatDao
							.getDepartmentListByDepartmentAnswerId(departmentAnswerId);
					if (toDepartmentList.size() > 0) {
						deparmentName = toDepartmentList.get(0).getDepartmentName();
					}
					title = step2Title.replace(step2TitleReplaceText, deparmentName);
				} else if (applcationStepId.equals(APPLICATION_STEP_ID_3)) {
					// 許可判定: 許可判定回答権限より担当課を取得、問合せ対象は一律で設定
					final List<Authority> authorityList = authorityRepository.findStep3AnswerDepartment();
					final List<String> departmentIdList = new ArrayList<String>();
					for (Authority authority : authorityList) {
						departmentIdList.add(authority.getDepartmentId());
					}
					List<Department> toDepartmentList = departmentRepository
							.getDepartmentListByIdList(departmentIdList);
					final List<String> departmentNameList = new ArrayList<String>();
					for (Department aDepartment : toDepartmentList) {
						departmentNameList.add(aDepartment.getDepartmentName());
					}
					deparmentName = String.join(",", departmentNameList);
					title = step3Title;
				}
				// 担当課
				form.setDepartmentName(deparmentName);
				// 対象
				form.setCategoryJudgementTitle(title);

				List<Message> messages = mssageRepository.findByChatId(chat.getChatId());
				// 最新投稿日時
				if(messages != null && messages.size() > 0) {
					form.setSendDatetime(datetimeformatter.format(messages.get(messages.size()-1).getSendDatetime()));
				} else {
					form.setSendDatetime("");
				}
				
				// 初回投稿日時
				if(messages != null && messages.size() > 0) {
					form.setEstablishmentFirstPostDatetime(datetimeformatter.format(messages.get(0).getSendDatetime()));
				} else {
					form.setEstablishmentFirstPostDatetime("");
				}

				// 回答済みの場合、回答者と回答日時を設定する
				if (chat.getLastAnswererId() != null && !"".equals(chat.getLastAnswererId())) {

					// 最新回答者
					List<GovernmentUser> governmentUserList = governmentUserRepository
							.findByUserId(chat.getLastAnswererId());
					if (governmentUserList.size() > 0) {
						form.setAnswerUserName(governmentUserList.get(0).getUserName());
					}else {
						form.setAnswerUserName("");
					}
					// 最新回答日時
					form.setAnswerDatetime(datetimeformatter.format(chat.getGovernmentAnswerDatetime()));
				} else {
					// 最新回答者
					form.setAnswerUserName("");
					// 最新回答日時
					form.setAnswerDatetime("");
				}

				// 地番
				List<ApplyLotNumberForm> lotNumberFormList = new ArrayList<ApplyLotNumberForm>();
				List<ApplyLotNumber> lotNumbersList = applicationDao.getApplyingLotNumberList(chat.getApplicationId(), lonlatEpsg);
				for (ApplyLotNumber lotNumbers : lotNumbersList) {
					lotNumberFormList.add(getApplyingLotNumberFormFromEntity(lotNumbers));
				}
				form.setLotNumbers(lotNumberFormList);

				formList.add(form);
				LOGGER.debug("検索結果編集  終了");
			}
			return formList;
		} finally {
			LOGGER.debug("問合せ情報検索 終了");
		}
	}

	/**
	 * 部署に紐づく問合せと回答情報検索（行政）
	 * 
	 * @param departmentId 部署ID
	 * @return 検索結果
	 */
	public ResponsibleInquiryFrom searchAnswersAndInquiries(String departmentId) {
		LOGGER.debug("担当課の問合せ・回答一覧検索 開始");
		ResponsibleInquiryFrom form = new ResponsibleInquiryFrom();
		try {

			LOGGER.trace("担当課の回答一覧検索 開始");
			ApplicationDao applicationDao = new ApplicationDao(emf);
			// O_申請（事前相談）取得
			List<ApplicationAnswerSearchResult> consultationApplicationList = applicationDao
					.getConsultationApplicatioList(departmentId, deadlineXDaysAgo);
			List<ApplyAnswerSearchResultForm> consultationApplyAnswerFormList = setApplyAnswerFormList(
					consultationApplicationList);
			form.setConsultationApplys(consultationApplyAnswerFormList);
			// O_申請（事前協議）取得
			List<ApplicationAnswerSearchResult> discussionApplicationList = applicationDao
					.getDiscussionApplicatioList(departmentId, deadlineXDaysAgo);
			List<ApplyAnswerSearchResultForm> discussionApplyAnswerFormList = setApplyAnswerFormList(
					discussionApplicationList);
			form.setDiscussionApplys(discussionApplyAnswerFormList);
			// O_申請（許可判定）取得
			List<ApplicationAnswerSearchResult> permissionApplicationList = applicationDao
					.getPermissionApplicatioList(departmentId, deadlineXDaysAgo);
			List<ApplyAnswerSearchResultForm> permissionApplyAnswerFormList = setApplyAnswerFormList(
					permissionApplicationList);
			form.setPermissionApplys(permissionApplyAnswerFormList);
			LOGGER.trace("担当課の回答一覧検索 終了");

			LOGGER.trace("担当課の問合せ一覧検索 開始");
			ChatDao chatDao = new ChatDao(emf);
			// 自分担当している未回答メッセージが存在するチャットに、該当チャットに事業者から最終投稿されなメッセージを取得する
			List<Message> messageList = chatDao.getNoAnswerMessageList(departmentId);
			List<ChatSearchResultForm> chatSearchResultFormList = new ArrayList<ChatSearchResultForm>();
			for (Message message : messageList) {
				ChatSearchResultForm chatSearchResultForm = new ChatSearchResultForm();
				LOGGER.trace("申請情報検索 開始");
				List<Chat> chatList = chatRepository.findByChatId(message.getChatId());

				if (chatList.size() > 0) {
					// 申請ID
					chatSearchResultForm.setApplicationId(chatList.get(0).getApplicationId());
				} else {
					LOGGER.warn("O_申請の値が取得できない チャットID: " + message.getChatId());
					continue;
				}
				LOGGER.trace("申請情報検索 終了");

				// ステータス
				List<InquiryAddress> inquiryAddress = inquiryAddressRepository
						.findByMessageIdAndDepartmentId(message.getMessageId(), departmentId);
				chatSearchResultForm.setStatus(
						getStatus(inquiryAddress.get(0).getReadFlag(), inquiryAddress.get(0).getAnswerCompleteFlag()));
				// チャットID
				chatSearchResultForm.setChatId(message.getChatId());
				// 最新メッセージ(当課の未回答メッセージリストの最新なメッセージを設定)
				chatSearchResultForm.setMessage(getMessageFormFromEntity(message));
				// 部署一覧
				LOGGER.trace("部署情報検索 開始");
				List<Department> departmentList = departmentRepository.getDepartmentListById(departmentId);
				List<DepartmentForm> departments = new ArrayList<DepartmentForm>();
				for (Department department : departmentList) {
					departments.add(getDepartmentFormFromEntity(department));
				}
				chatSearchResultForm.setDepartments(departments);
				// 申請地番一覧
				List<ApplyLotNumberForm> formList = new ArrayList<ApplyLotNumberForm>();
				List<ApplyLotNumber> lotNumbersList = applicationDao.getApplyingLotNumberList(chatList.get(0).getApplicationId(), lonlatEpsg);
				for (ApplyLotNumber lotNumbers : lotNumbersList) {
					formList.add(getApplyingLotNumberFormFromEntity(lotNumbers));
				}
				chatSearchResultForm.setLotNumbers(formList);			
				chatSearchResultFormList.add(chatSearchResultForm);
				LOGGER.trace("部署情報検索 終了");
			}

			form.setInquiries(chatSearchResultFormList);
			LOGGER.trace("担当課の問合せ一覧検索 終了");
			return form;
		} finally {
			LOGGER.debug("担当課の問合せ・回答一覧検索 終了");
		}
	}

	/**
	 * 申請一覧フォーム設定（事前相談、事前協議、許可申請）
	 * 
	 * @param applicationList
	 * @return 申請一覧フォームリスト
	 */
	List<ApplyAnswerSearchResultForm> setApplyAnswerFormList(List<ApplicationAnswerSearchResult> applicationList) {

		List<ApplyAnswerSearchResultForm> applyAnswerFormList = new ArrayList<ApplyAnswerSearchResultForm>();
		ApplicationDao dao = new ApplicationDao(emf);
		for (ApplicationAnswerSearchResult application : applicationList) {
			ApplyAnswerSearchResultForm applyAnswerForm = new ApplyAnswerSearchResultForm();
			// 申請ID
			applyAnswerForm.setApplicationId(application.getApplicationId());
			// ステータス
			try {
				final String statusText = (application.getVersionInformation() != null)
						? versionInformationText.replace(versionInformationReplaceText,
								application.getVersionInformation().toString())
								+ getStatusMap().get(application.getStatus())
						: getStatusMap().get(application.getStatus());
				applyAnswerForm.setStatus(statusText);
			} catch (Exception e) {
				applyAnswerForm.setStatus("");
			}
			// 期限
			applyAnswerForm.setDeadlineDate(application.getDeadlineDate());

			// 警告フラグ
			applyAnswerForm.setWarning(application.getWarning());

			// 申請地番一覧
			List<ApplyLotNumberForm> formList = new ArrayList<ApplyLotNumberForm>();
			List<ApplyLotNumber> lotNumbersList = dao.getApplyingLotNumberList(application.getApplicationId(), lonlatEpsg);
			for (ApplyLotNumber lotNumbers : lotNumbersList) {
				formList.add(getApplyingLotNumberFormFromEntity(lotNumbers));
			}
			applyAnswerForm.setLotNumbers(formList);
			
			applyAnswerFormList.add(applyAnswerForm);
		}

		return applyAnswerFormList;
	}

	/**
	 * 照合ID,パスワードチェック
	 */
	public boolean validateLoginInfo(Integer applicationId, Integer answerId) {
		boolean result = true;

		List<Answer> anwserList = answerRepository.findByAnswerId(answerId);
		if (anwserList.size() == 1) {
			Answer answer = anwserList.get(0);
			if (applicationId.equals(answer.getApplicationId())) {
				result = true;
			} else {
				result = false;
			}
		} else {
			LOGGER.warn("回答情報の取得件数が不正");
			result = false;
		}
		return result;
	}

	/**
	 * チャットIDに紐づけ回答情報を取得する
	 * 
	 * @param chatId チャットID
	 * @return
	 */
	public Answer getApplicationId(Integer chatId) {
		LOGGER.debug("申請ID取得 開始");
		try {
			LOGGER.debug("チャットIDに紐づけ回答ID取得 開始");
			LOGGER.debug("チャットID：" + chatId);
			List<Chat> chatList = chatRepository.findByChatId(chatId);
			LOGGER.debug("チャットIDに紐づけ回答ID取得 終了");

			Answer answer = new Answer();
			LOGGER.debug("回答ID：" + chatList.get(0).getAnswerId());
			List<Answer> answerList = answerRepository.findByAnswerId(chatList.get(0).getAnswerId());
			if (answerList.size() > 0) {
				answer = answerList.get(0);
			}

			return answer;
		} finally {
			LOGGER.debug("申請ID取得 終了");
		}
	}

	/**
	 * ユーザー名取得
	 * 
	 * @param userId ユーザーID
	 * @return ユーザー名
	 */
	public String getUserName(String userId) {
		LOGGER.debug("ユーザー名取得 開始");
		try {
			String userName = "";
			LOGGER.debug("M_行政ユーザ取得 開始");
			List<GovernmentUser> governmentUserList = governmentUserRepository.findByUserId(userId);
			if (governmentUserList.size() > 0) {
				userName = governmentUserList.get(0).getUserName();
			}
			LOGGER.debug("M_行政ユーザ取得 終了");

			return userName;
		} finally {
			LOGGER.debug("ユーザー名取得 終了");
		}
	}

	/**
	 * 問合せファイルアップロードパラメータチェック
	 * 
	 * @param InquiryFileForm パラメータ
	 * @return 判定結果
	 */
	public boolean validateInquiryFileForm(InquiryFileForm inquiryFileForm) {
		LOGGER.debug("問合せファイルアップロードパラメータチェック 開始");
		try {
			Integer messageId = inquiryFileForm.getMessageId();
			String fileName = inquiryFileForm.getFileName();
			MultipartFile uploadFile = inquiryFileForm.getUploadFile();
			if (messageId == null //
					|| fileName == null //
					|| EMPTY.equals(fileName) //
					|| uploadFile == null //
			) {
				// パラメータが空
				LOGGER.warn("パラメータにnullまたは空が含まれる");
				return false;
			}

			// メッセージID
			if (mssageRepository.findByMessageId(messageId).size() != 1) {
				// メッセージIDが不正
				LOGGER.warn("メッセージIDで得られるメッセージデータ件数が不正");
				return false;
			}

			// 問合せファイルID
			Integer inquiryFileId = inquiryFileForm.getInquiryFileId();
			if (inquiryFileId != null) {
				if (inquiryFileRepository.findByInquiryFileId(inquiryFileId).size() != 1) {
					LOGGER.warn("問合せファイルの件数が不正");
					return false;
				}
			}

			return true;
		} finally {
			LOGGER.debug("問合せファイルアップロードパラメータチェック 終了");
		}
	}

	/**
	 * 問合せファイルアップロード
	 * 
	 * @param form パラメータ
	 */
	public void uploadInquiryFile(InquiryFileForm form) {
		LOGGER.debug("問合せファイルアップロード 開始");
		try {
			// ファイルパスは「/chat/<チャットID>/<メッセージID>/<問合せファイルID>/<アップロードファイル名>」

			// O_問合せファイル登録
			LOGGER.trace("O_問合せファイル登録 開始");
			Integer inquiryFileId = inquiryFileJdbc.insert(form);
			form.setInquiryFileId(inquiryFileId);
			LOGGER.trace("O_問合せファイル登録 終了");

			// O_メッセージ取得
			LOGGER.trace("O_メッセージ取得 開始");
			List<Message> messages = mssageRepository.findByMessageId(form.getMessageId());
			Integer chatId = messages.get(0).getChatId();
			LOGGER.trace("O_メッセージ取得 終了");

			// 相対フォルダパス
			String folderPath = inquiryFolderName;
			folderPath += PATH_SPLITTER + chatId;
			folderPath += PATH_SPLITTER + form.getMessageId();
			folderPath += PATH_SPLITTER + form.getInquiryFileId();

			// 絶対フォルダパス
			String absoluteFolderPath = fileRootPath + folderPath;
			Path directoryPath = Paths.get(absoluteFolderPath);
			if (!Files.exists(directoryPath)) {
				// フォルダがないので生成
				LOGGER.debug("フォルダ生成: " + directoryPath);
				Files.createDirectories(directoryPath);
			}

			// 相対ファイルパス
			String filePath = folderPath + PATH_SPLITTER + form.getFileName();
			// 絶対ファイルパス
			String absoluteFilePath = absoluteFolderPath + PATH_SPLITTER + form.getFileName();

			// ファイルパスはrootを除いた相対パスを設定
			form.setFilePath(filePath);

			// ファイルパス更新
			LOGGER.trace("ファイルパス更新 開始");
			if (inquiryFileJdbc.updateFilePath(inquiryFileId, filePath) != 1) {
				LOGGER.warn("ファイルパス更新件数が不正");
				throw new RuntimeException("ファイルパス更新件数が不正");
			}
			LOGGER.trace("ファイルパス更新 終了");

			// ファイル出力
			LOGGER.trace("ファイル出力 開始");
			exportFile(form.getUploadFile(), absoluteFilePath);
			LOGGER.trace("ファイル出力 終了");
		} catch (Exception ex) {
			// RuntimeExceptionで投げないとロールバックされない
			LOGGER.error("問合せファイルアップロードで例外発生", ex);
			throw new RuntimeException(ex);
		} finally {
			LOGGER.debug("問合せファイルアップロード 終了");
		}
	}

	/**
	 * 問合せファイルダウンロードパラメータ確認
	 * 
	 * @param form パラメータ
	 * @return 確認結果
	 */
	public boolean validateDownloadInquiryFile(InquiryFileForm form) {
		LOGGER.debug("問合せファイルダウンロードパラメータ確認 開始");
		try {
			Integer messageId = form.getMessageId();
			Integer inquiryFileId = form.getInquiryFileId();

			// メッセージID
			if (mssageRepository.findByMessageId(messageId).size() != 1) {
				LOGGER.warn("メセッジデータの件数が不正");
				return false;
			}

			// 問合せファイルID
			if (inquiryFileRepository.findByInquiryFileId(inquiryFileId).size() != 1) {
				LOGGER.warn("問合せファイルの件数が不正");
				return false;
			}
			return true;
		} finally {
			LOGGER.debug("問合せファイルダウンロードパラメータ確認 終了");
		}
	}

	/**
	 * 問合せファイルダウンロード
	 * 
	 * @param form パラメータ
	 * @return 応答Entity
	 */
	public ResponseEntity<Resource> downloadInquiryFile(InquiryFileForm form) {
		LOGGER.debug("問合せファイルダウンロード 開始");
		try {
			LOGGER.trace("問合せファイルデータ取得 開始: " + form.getInquiryFileId());
			List<InquiryFile> answerFileList = inquiryFileRepository.findByInquiryFileId(form.getInquiryFileId());
			InquiryFile inquiryFile = answerFileList.get(0);
			LOGGER.trace("問合せファイルデータ取得 終了:" + form.getInquiryFileId());

			// 絶対ファイルパス
			String absoluteFilePath = fileRootPath + inquiryFile.getFilePath();
			Path filePath = Paths.get(absoluteFilePath);

			if (!Files.exists(filePath)) {
				// ファイルが存在しない
				LOGGER.warn("ファイルが存在しない: " + filePath);
				return new ResponseEntity<Resource>(HttpStatus.NOT_FOUND);
			}

			Resource resource = new PathResource(filePath);
			return ResponseEntity.ok().contentType(getContentType(filePath))
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
					.body(resource);
		} catch (Exception ex) {
			LOGGER.error("問合せファイルダウンロードで例外発生", ex);
			return new ResponseEntity<Resource>(HttpStatus.SERVICE_UNAVAILABLE);
		} finally {
			LOGGER.debug("問合せファイルダウンロード 終了");
		}
	}

	/**
	 * O_メッセージEntityからメッセージデータフォームを生成
	 * 
	 * @param message O_メッセージEntity
	 * @return メッセージデータフォーム
	 */
	protected MessageForm getMessageFormFromEntity(Message message) {
		LOGGER.debug("メッセージフォームを生成 開始");
		try {
			MessageForm form = new MessageForm();
			form.setMessageId(message.getMessageId());
			form.setMessageText(message.getMessageText());
			form.setMessageType(message.getMessageType());
			form.setReadFlag(message.getReadFlag());
			form.setAnswerCompleteFlag(message.getAnswerCompleteFlag());

			DateTimeFormatter datetimeformatter = DateTimeFormatter.ofPattern("yyyy/MM/dd/ HH:mm");
			String datetimeformated = datetimeformatter.format(message.getSendDatetime());
			form.setSendDatetime(datetimeformated);
			String userId = message.getSenderId();

			LOGGER.trace("メッセージの送信ユーザ情報取得 開始");
			LOGGER.trace("ユーザID: " + userId);
			List<GovernmentUser> governmentUserList = governmentUserRepository.findByUserId(userId);
			if (governmentUserList.size() > 0) {
				form.setSender(getGovernmentUserFormFromEntity(governmentUserList.get(0)));
			}
			LOGGER.trace("メッセージの送信ユーザ情報取得 終了");

			LOGGER.trace("メッセージの宛先部署情報取得 開始");
			List<InquiryAddressForm> inquiryAddressFormList = new ArrayList<InquiryAddressForm>();
			List<InquiryAddress> inquiryAddressList = inquiryAddressRepository.findByMessageId(message.getMessageId());
			for (InquiryAddress inquiryAddress : inquiryAddressList) {
				inquiryAddressFormList.add(getInquiryAddressFormFromEntity(inquiryAddress));
			}
			form.setInquiryAddressForms(inquiryAddressFormList);
			LOGGER.trace("メッセージの宛先部署情報取得 終了");

			LOGGER.trace("メッセージの問合せ添付ファイル情報取得 開始");
			List<InquiryFile> inquiryFileList = inquiryFileRepository.findByMessageId(message.getMessageId());
			List<InquiryFileForm> inquiryFileFormList = new ArrayList<InquiryFileForm>();
			for (InquiryFile inquiryFile : inquiryFileList) {
				inquiryFileFormList.add(getInquiryFileFormFromEntity(inquiryFile));
			}
			form.setInquiryFiles(inquiryFileFormList);
			LOGGER.trace("メッセージの問合せ添付ファイル情報取得 終了");

			return form;
		} finally {
			LOGGER.debug("メッセージフォームを生成 終了");
		}
	}

	/**
	 * 既読フラグと回答済みフラグより、問合せステータスを判定する
	 * 
	 * @param readFlag           既読フラグ
	 * @param answerCompleteFlag 回答済みフラグ
	 * @return 問合せステータス
	 */
	protected Integer getStatus(Boolean readFlag, Boolean answerCompleteFlag) {
		Integer status = 0;
		if (!readFlag) {
			// 既読フラグ=false ⇒ 0：未読
			status = 0;
		} else {
			if (answerCompleteFlag) {
				// 既読フラグ=true かつ、 回答済みフラグ=true ⇒ ２：回答済み
				status = 2;
			} else {
				// 既読フラグ=true かつ、 回答済みフラグ=false ⇒ １：既読
				status = 1;
			}
		}
		return status;
	}

	/**
	 * M_行政ユーザEntityをM_行政ユーザフォームに詰めなおす
	 * 
	 * @param entity M_部署Entity
	 * @return M_部署フォーム
	 */
	protected GovernmentUserForm getGovernmentUserFormFromEntity(GovernmentUser entity) {
		GovernmentUserForm form = new GovernmentUserForm();
		form.setUserId(entity.getUserId());
		form.setUserName(entity.getUserName());
		form.setLoginId(entity.getLoginId());
		form.setRoleCode(entity.getRoleCode());
		form.setDepartmentId(entity.getDepartmentId());
		List<Department> departmentList = departmentRepository.getDepartmentListById(entity.getDepartmentId());
		if (departmentList.size() > 0) {
			form.setDepartmentName(departmentList.get(0).getDepartmentName());
		}
		return form;
	}

	/**
	 * 行政に事業者からの問合せ通知送付
	 * 
	 * @param form           メッセージ投稿リクエストフォーム
	 * @param toDepartmentId 宛先部署ID
	 * @param messageId      今回投稿されたメッセージID
	 */
	private void sendInquiryMailFromBusinessUser(MessagePostRequestForm form, String toDepartmentId,
			Integer messageId) {
		Integer chatId = form.getChatId();
		Integer answerId = form.getAnswerId();

		LOGGER.debug("O_メッセージ検索 開始");
		ChatDao chatDao = new ChatDao(emf);
		List<Message> messageList = chatDao.getMessageListForSendMail(chatId, MESSAGE_TYPE_BUSINESS_TO_GOVERNMENT,
				toDepartmentId, sendMailInterval, messageId);
		LOGGER.debug("O_メッセージ検索 終了");

		// 送信間隔の間に、同じ宛先部署への問合せがない場合、メール通知を行う。
		if (messageList.size() == 0) {
			MailItem baseItem = new MailItem();
			LOGGER.debug(" O_申請者情報検索 開始");
			String id = form.getLoginId();
			String password = form.getPassword();
			String hash = AuthUtil.createHash(password);
			List<ApplicantInformation> applicantInformationList = applicantInformationRepository.getApplicantList(id,
					hash);
			if (applicantInformationList.size() != 1) {
				LOGGER.error("申請者データの件数が不正");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}
			ApplicantInformation applicantInformation = applicantInformationList.get(0);
			LOGGER.debug(" O_申請者情報検索 終了");
			
			// O_申請情報検索
			Application application = getApplication(applicantInformation.getApplicationId());
			baseItem.setApplicationId(application.getApplicationId().toString()); // 申請ID
			baseItem.setApplicationTypeName(getApplicationTypeName(application.getApplicationTypeId())); // 申請種類名
			baseItem.setApplicationStepName(getApplicationStepName(form.getApplicationStepId())); // 申請段階名
			
			LOGGER.debug("事業者から問合せ通知（行政向け）用内容編集 開始");
			// 申請者氏名
			LOGGER.debug("申請者氏名設定 開始");
			baseItem.setName(getApplicantName(applicantInformation));
			LOGGER.debug("申請者氏名設定 終了");

			// 申請者メールアドレス
			LOGGER.debug("申請者メールアドレス設定 開始");
			baseItem.setMailAddress(applicantInformation.getMailAddress());
			LOGGER.debug("申請者メールアドレス設定 終了");

			// 申請地番
			LOGGER.debug("申請地番設定 開始");
			baseItem.setLotNumber(getLotNumbers(applicantInformation.getApplicationId()));
			LOGGER.debug("申請地番設定 終了");

			// 回答対象
			List<Chat> chatList = chatRepository.findByChatId(chatId);
			if(chatList.get(0).getApplicationStepId().intValue() == 1) {
				LOGGER.debug("回答対象設定 開始");
				baseItem.setAnswerTarget(getAnswerTarget(answerId));
				LOGGER.debug("回答対象設定 終了");
			}else if(chatList.get(0).getApplicationStepId().intValue() == 2 && chatList.get(0).getDepartmentAnswerId() != null) {
				LOGGER.debug("回答対象設定 開始");
				List<DepartmentAnswer> departmentAnswerList = departmentAnswerRepository.findByDepartmentAnswerId(chatList.get(0).getDepartmentAnswerId());
				if(departmentAnswerList != null && departmentAnswerList.get(0) != null && departmentAnswerList.get(0).getDepartmentId() != null) {
					List<Department> departmentList = departmentRepository.getDepartmentListById(departmentAnswerList.get(0).getDepartmentId());
					if(departmentList != null && departmentList.get(0) != null && departmentList.get(0).getDepartmentName() != null) {
						baseItem.setAnswerTarget(departmentList.get(0).getDepartmentName());
					}
				}
				LOGGER.debug("回答対象設定 終了");
			}else if(chatList.get(0).getApplicationStepId().intValue() == 3){
				LOGGER.debug("回答対象設定 開始");
				baseItem.setAnswerTarget(step3Title);
				LOGGER.debug("回答対象設定 終了");
			}

			// 問合せ内容
			LOGGER.debug("問合せ内容設定 開始");
			baseItem.setInquiryContent(form.getMessage().getMessageText());
			LOGGER.debug("問合せ内容設定 終了");

			LOGGER.debug("事業者から問合せ通知（行政向け）用内容編集 終了");

			// 宛先部署びメールアドレス
			List<Department> toDepartmentList = departmentRepository.getDepartmentListById(toDepartmentId);
			if (toDepartmentList.size() != 1) {
				LOGGER.error("部署データの件数が不正");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}
			String address = toDepartmentList.get(0).getMailAddress();

			String subject = getMailPropValue(MailMessageUtil.KEY_INQUIRY_FROM_BUSSINESS_SUBJECT, baseItem);
			String body = getMailPropValue(MailMessageUtil.KEY_INQUIRY_FROM_BUSSINESS_BODY, baseItem);

			LOGGER.trace(address);
			LOGGER.trace(subject);
			LOGGER.trace(body);

			try {
				final String[] mailAddressList = address.split(",");
				for (String aMailAddress : mailAddressList) {
					mailSendutil.sendMail(aMailAddress, subject, body);
				}
			} catch (Exception e) {
				LOGGER.error("メール送信時にエラー発生", e);
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * 行政に行政からの問合せ通知/事業者に問合せ回答通知 を送付する
	 * 
	 * @param form           メッセージ投稿リクエストフォーム
	 * @param departmentName 送信者部署名
	 * @param messageType    メッセージタイプ
	 */
	private void sendInquiryMailFromGovernmentUser(MessagePostRequestForm form, String departmentName,
			Integer messageType, Integer messageId) {
		Integer chatId = form.getChatId();
		Integer answerId = form.getAnswerId();
		List<DepartmentForm> toDepartments = form.getToDepartments();

		// 選択された宛先リストをループして、通知メール送信を行う
		for (DepartmentForm department : toDepartments) {

			String toDepartmentId = department.getDepartmentId();

			LOGGER.debug("O_メッセージ検索 開始");
			ChatDao chatDao = new ChatDao(emf);
			List<Message> messageList = chatDao.getMessageListForSendMail(chatId, messageType, toDepartmentId,
					sendMailInterval, messageId);
			LOGGER.debug("O_メッセージ検索 終了");

			// 送信間隔の間に、同じ宛先部署への問合せがない場合、メール通知を行う。
			if (messageList.size() == 0) {
				MailItem baseItem = new MailItem();

				// 回答がない場合、チャットIDで、回答IDを取得する
				List<Chat> chatList = chatRepository.findByChatId(chatId);
				if (answerId == null) {
					if (chatList.size() != 1) {
						LOGGER.error("チャットデータの件数が不正");
						throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
					}
					answerId = chatList.get(0).getAnswerId();
				}
				
				if (chatList.size() != 1) {
					LOGGER.error("チャットデータの件数が不正");
					throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
				}
				Integer applicationId = chatList.get(0).getApplicationId();

				// O_申請情報検索
				Application application = getApplication(applicationId);
				baseItem.setApplicationId(application.getApplicationId().toString()); // 申請ID
				baseItem.setApplicationTypeName(getApplicationTypeName(application.getApplicationTypeId())); // 申請種類名
				baseItem.setApplicationStepName(getApplicationStepName(form.getApplicationStepId())); // 申請段階名
				
				LOGGER.debug(" O_申請者情報検索 開始");
				List<ApplicantInformation> applicantInformationList = applicantInformationRepository
						.getApplicantList(applicationId,CONTACT_ADDRESS_INVALID);
				if (applicantInformationList.size() < 1) {
					LOGGER.error("申請者データの件数が不正");
					throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
				}
				ApplicantInformation applicantInformation = applicantInformationList.get(0);
				LOGGER.debug(" O_申請者情報検索 終了");

				LOGGER.debug("行政から問合せ通知用内容編集 開始");

				// 行政向け問合せ通知情報編集
				if (MESSAGE_TYPE_GOVERNMENT_TO_GOVERNMENT.equals(messageType)) {

					// 部署名
					LOGGER.debug("部署名設定 開始");
					baseItem.setDepartmentName(departmentName);
					LOGGER.debug("部署名設定 終了");

					// 申請者氏名
					LOGGER.debug("申請者氏名設定 開始");
					baseItem.setName(getApplicantName(applicantInformation));
					LOGGER.debug("申請者氏名設定 終了");

					// 申請者メールアドレス
					LOGGER.debug("申請者メールアドレス設定 開始");
					baseItem.setMailAddress(applicantInformation.getMailAddress());
					LOGGER.debug("申請者メールアドレス設定 終了");
				}
				// 申請地番
				LOGGER.debug("申請地番設定 開始");
				baseItem.setLotNumber(getLotNumbers(applicationId));
				LOGGER.debug("申請地番設定 終了");

				// 回答対象
				if(chatList.get(0).getApplicationStepId() == 1) {
					LOGGER.debug("回答対象設定 開始");
					baseItem.setAnswerTarget(getAnswerTarget(answerId));
					LOGGER.debug("回答対象設定 終了");
				}else if(chatList.get(0).getApplicationStepId().intValue() == 2 && chatList.get(0).getDepartmentAnswerId() != null) {
					LOGGER.debug("回答対象設定 開始");
					List<DepartmentAnswer> departmentAnswerList = departmentAnswerRepository.findByDepartmentAnswerId(chatList.get(0).getDepartmentAnswerId());
					if(departmentAnswerList != null && departmentAnswerList.get(0) != null && departmentAnswerList.get(0).getDepartmentId() != null) {
						List<Department> departmentList = departmentRepository.getDepartmentListById(departmentAnswerList.get(0).getDepartmentId());
						if(departmentList != null && departmentList.get(0) != null && departmentList.get(0).getDepartmentName() != null) {
							baseItem.setAnswerTarget(departmentList.get(0).getDepartmentName());
						}
					}
					LOGGER.debug("回答対象設定 終了");
				}else if(chatList.get(0).getApplicationStepId().intValue() == 3){
					LOGGER.debug("回答対象設定 開始");
					baseItem.setAnswerTarget(step3Title);
					LOGGER.debug("回答対象設定 終了");
				}

				// 問合せ内容
				LOGGER.debug("問合せ内容設定 開始");
				baseItem.setInquiryContent(form.getMessage().getMessageText());
				LOGGER.debug("問合せ内容設定 終了");

				// 回答内容
				LOGGER.debug("回答内容設定 開始");
				baseItem.setAnswerContent(form.getMessage().getMessageText());
				LOGGER.debug("回答内容設定 終了");

				LOGGER.debug("行政から問合せ通知用内容編集 終了");

				// 行政に行政からの問合せ通知を送付する
				if (MESSAGE_TYPE_GOVERNMENT_TO_GOVERNMENT.equals(messageType)) {
					// 宛先部署びメールアドレス
					List<Department> toDepartmentList = departmentRepository.getDepartmentListById(toDepartmentId);
					if (toDepartmentList.size() != 1) {
						LOGGER.error("部署データの件数が不正");
						throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
					}
					String address = toDepartmentList.get(0).getMailAddress();

					String subject = getMailPropValue(MailMessageUtil.KEY_INQUIRY_FROM_GOVERNMENT_SUBJECT, baseItem);
					String body = getMailPropValue(MailMessageUtil.KEY_INQUIRY_FROM_GOVERNMENT_BODY, baseItem);

					LOGGER.trace(address);
					LOGGER.trace(subject);
					LOGGER.trace(body);

					try {
						final String[] mailAddressList = address.split(",");
						for (String aMailAddress : mailAddressList) {
							mailSendutil.sendMail(aMailAddress, subject, body);
						}
					} catch (Exception e) {
						LOGGER.error("メール送信時にエラー発生", e);
						throw new RuntimeException(e);
					}
				}

				// 事業者に問合せ回答通知を送付する
				if (MESSAGE_TYPE_GOVERNMENT_TO_BUSINESS.equals(messageType)) {
					String mailAddress = applicantInformation.getMailAddress();
					
					List<ApplicantInformation> contactApplicantList = applicantInformationRepository
							.getApplicantList(applicationId,CONTACT_ADDRESS_VALID);
					if (contactApplicantList.size() > 0) {
						ApplicantInformation contactApplicant = contactApplicantList.get(0);
						mailAddress = contactApplicant.getMailAddress();
					}

					String subject = getMailPropValue(MailMessageUtil.KEY_BUSSINESS_INQUIRY_SUBJECT, baseItem);
					String body = getMailPropValue(MailMessageUtil.KEY_BUSSINESS_INQUIRY_BODY, baseItem);

					LOGGER.trace(mailAddress);
					LOGGER.trace(subject);
					LOGGER.trace(body);

					try {
						mailSendutil.sendMail(mailAddress, subject, body);
					} catch (Exception e) {
						LOGGER.error("メール送信時にエラー発生", e);
						throw new RuntimeException(e);
					}
				}
			}
		}

	}

	/**
	 * 申請者氏名取得
	 * 
	 * @param applicant 申請者情報
	 * @return 申請者氏名
	 */
	private String getApplicantName(ApplicantInformation applicant) {
		String name = "";
		switch (applicantNameItemNumber) { // 氏名はプロパティで設定されたアイテムを設定
		case 1:
			name = applicant.getItem1();
			break;
		case 2:
			name = applicant.getItem2();
			break;
		case 3:
			name = applicant.getItem3();
			break;
		case 4:
			name = applicant.getItem4();
			break;
		case 5:
			name = applicant.getItem5();
			break;
		case 6:
			name = applicant.getItem6();
			break;
		case 7:
			name = applicant.getItem7();
			break;
		case 8:
			name = applicant.getItem8();
			break;
		case 9:
			name = applicant.getItem9();
			break;
		case 10:
			name = applicant.getItem10();
			break;
		default:
			LOGGER.error("氏名アイテム番号指定が不正: " + applicantNameItemNumber);
			throw new RuntimeException("氏名アイテム番号指定が不正");
		}

		return name;
	}

	/**
	 * 申請地番一覧取得
	 * 
	 * @param applicationId 申請ID
	 * @return
	 */
	private String getLotNumbers(int applicationId) {
		LOGGER.debug("申請地番一覧取得 開始");
		ApplicationDao applicationDao = new ApplicationDao(emf);
		List<ApplyLotNumber> lotNumbersList = applicationDao.getApplyingLotNumberList(applicationId, lonlatEpsg);
		String addressText = (lotNumbersList != null && lotNumbersList.size() > 0) ? lotNumbersList.get(0).getLotNumbers(): "";
		LOGGER.debug("申請地番一覧取得 終了");
		return addressText;
	}

	/**
	 * 回答対象取得
	 * 
	 * @param answerId 回答ID
	 * @return
	 */
	private String getAnswerTarget(int answerId) {
		LOGGER.debug(" 回答対象取得 開始");

		String answerTarget = "";
		AnswerDao answerDao = new AnswerDao(emf);
		List<CategoryJudgementResult> categoryJudgements = answerDao.getJudgementResultByAnswerId(answerId);
		if (categoryJudgements.size() != 1) {
			LOGGER.error("区分判定データの件数が不正");
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		}
		answerTarget = categoryJudgements.get(0).getTitle();

		LOGGER.debug(" 回答対象取得 終了");

		return answerTarget;
	}

	/**
	 * O_問合せファイルEntityをO_問合せファイルフォームに詰めなおす
	 * 
	 * @param entity O_問合せファイルEntity
	 * @return O_問合せファイルフォーム
	 */
	private InquiryFileForm getInquiryFileFormFromEntity(InquiryFile entity) {
		InquiryFileForm form = new InquiryFileForm();
		form.setMessageId(entity.getMessageId());
		form.setInquiryFileId(entity.getInquiryFileId());
		form.setFileName(entity.getFileName());
		form.setFilePath(entity.getFilePath());
		DateTimeFormatter datetimeformatter = DateTimeFormatter.ofPattern("yyyy/MM/dd/ HH:mm");
		String datetimeformated = (entity.getRegisterDatetime() != null)
				? datetimeformatter.format(entity.getRegisterDatetime())
				: "";
		form.setRegisterDatetime(datetimeformated);

		return form;
	}
	
	/**
	 * 申請IDからO_申請を取得する
	 * 
	 * @param applicationId 申請ID
	 * @return O_申請
	 */
	private Application getApplication(Integer applicationId) {
	
		Application application;
		
		LOGGER.trace("O_申請検索 開始");
		List<Application> applicationList = applicationRepository
				.getApplicationList(applicationId);
		if (applicationList.size() == 0) {
			LOGGER.warn("O_申請情報が存在しません。");
			throw new RuntimeException();
		}
		application = applicationList.get(0);
		LOGGER.trace("O_申請検索 終了");
		
		return application;
	}
	/**
	 * 申請種類IDから申請種類名を取得する
	 * 
	 * @param applicationTypeId 申請種別ID
	 * @return 申請種別名
	 */
	private String getApplicationTypeName(Integer applicationTypeId) {
	
		ApplicationType applicationType;
		
		LOGGER.trace("M_申請種類検索 開始");
		List<ApplicationType> applicationTypeList = applicationTypeRepository
				.findByApplicationTypeId(applicationTypeId);
		if (applicationTypeList.size() == 0) {
			LOGGER.warn("M_申請種類情報が存在しません。");
			throw new RuntimeException();
		}
		applicationType = applicationTypeList.get(0);
		LOGGER.trace("M_申請種類検索 終了");
		
		return applicationType.getApplicationTypeName();
	}
	/**
	 * 申請段階IDから申請段階名を取得する
	 * 
	 * @param applicationStepId 申請段階ID
	 * @return 申請段階名
	 */
	private String getApplicationStepName(Integer applicationStepId) {
	
		ApplicationStep applicationStep;
		
		LOGGER.trace("M_申請段階検索 開始");
		List<ApplicationStep> applicationStepList = applicationStepRepository
				.findByApplicationStepId(applicationStepId);
		if (applicationStepList.size() == 0) {
			LOGGER.warn("M_申請段階情報が存在しません。");
			throw new RuntimeException();
		}
		applicationStep = applicationStepList.get(0);
		LOGGER.trace("M_申請段階検索 終了");
		
		return applicationStep.getApplicationStepName();
	}
	
	/**
	 * 
	 * 問い合わせリマインドの検索を行う
	 */
	public Map<String, Map<Integer, List<Integer>>> getRemindMail() {
		Map<String, Map<Integer, List<Integer>>> chatMap = new HashMap<>();
		List<Chat> chats = chatRepository.findRemindChat();
		for (Chat chat : chats) {
			// 申請段階IDを取得
			Integer stepId = chat.getApplicationStepId();
			// 許可判定の場合
			if (stepId.equals(APPLICATION_STEP_ID_3)) {
				// 許可判定,M_権限より、部署ID
				List<Authority> departmentIdList = authorityRepository.findStep3AnswerDepartment();
				// 部署のリストを回す
				for (Authority departmentIds : departmentIdList) {
					// 部署ID
					String departmentId = departmentIds.getDepartmentId();
					// 返却するmapに部署IDが含まれていなければ追加する
					if (!(chatMap.containsKey(departmentId))) {
						List<Integer> applicationIdnew = new ArrayList<>();
						applicationIdnew.add(chat.getApplicationId());
						Map<Integer, List<Integer>> stepApplicationIdNew = new HashMap<>();
						stepApplicationIdNew.put(APPLICATION_STEP_ID_3, applicationIdnew);
						chatMap.put(departmentId, stepApplicationIdNew);
					} else {
						// 返却するmapに部署IDが含まれている場合は取り出す
						Map<Integer, List<Integer>> stepApplicationId = chatMap.get(departmentId);
						// 申請段階が3のものを取り出す
						List<Integer> applicationId = stepApplicationId.get(APPLICATION_STEP_ID_3);
						// 申請段階3のものが無ければ追加する
						if (applicationId == null) {
							List<Integer> applicationIdnew = new ArrayList<>();
							applicationIdnew.add(chat.getApplicationId());
							stepApplicationId.put(APPLICATION_STEP_ID_3, applicationIdnew);
							chatMap.put(departmentId, stepApplicationId);
						} else {
							// 申請段階3のものがあった場合は取り出したものにaddしてputし直す
							if (!applicationId.contains(chat.getApplicationId())) {
								applicationId.add(chat.getApplicationId());
								stepApplicationId.put(APPLICATION_STEP_ID_3, applicationId);
								chatMap.put(departmentId, stepApplicationId);
							}
						}
					}
				}
			} else {
				// 許可判定以外の場合
				// o_メッセージからメッセージを取得する
				List<Message> messages = mssageRepository.findRemindMessageByChatId(chat.getChatId());
				// メッセージのリストを回して該当する部署を取得する
				for (Message message : messages) {
					// 部署リストを取得
					List<InquiryAddress> address = inquiryAddressRepository
							.findByMessageIdAndFlag0(message.getMessageId());
					// 部署リストを回してmapに追加していく
					for (InquiryAddress addres : address) {
						// 部署ID
						String departmentId = addres.getDepartmentId();
						// 返却するmapに部署IDが含まれていなければ追加する
						if (!(chatMap.containsKey(departmentId))) {
							List<Integer> applicationIdnew = new ArrayList<>();
							applicationIdnew.add(chat.getApplicationId());
							Map<Integer, List<Integer>> stepApplicationIdNew = new HashMap<>();
							stepApplicationIdNew.put(stepId, applicationIdnew);
							chatMap.put(departmentId, stepApplicationIdNew);
						} else {
							// 返却するmapに部署IDが含まれている場合は取り出す
							Map<Integer, List<Integer>> stepApplicationId = chatMap.get(departmentId);
							// 申請段階で取り出す
							List<Integer> applicationId = stepApplicationId.get(stepId);
							// その申請段階のものが無ければ追加する
							if (applicationId == null) {
								List<Integer> applicationIdnew = new ArrayList<>();
								applicationIdnew.add(chat.getApplicationId());
								stepApplicationId.put(stepId, applicationIdnew);
								chatMap.put(departmentId, stepApplicationId);
							} else {
								// 申請段階のものがあった場合は取り出したものにaddしてputし直す
								if (!applicationId.contains(chat.getApplicationId())) {
									applicationId.add(chat.getApplicationId());
									stepApplicationId.put(stepId, applicationId);
									chatMap.put(departmentId, stepApplicationId);
								}
							}
						}
					}
				}
			}
		}
		return chatMap;
	}
	
	/**
	 * チャット投稿のログデータ編集
	 * 
	 * @param isGoverment    行政かどうか
	 * @param accessId       アクセスID
	 * @param loginId        ログインID
	 * @param departmentName 部署名
	 * @param chatId         チャットID
	 * 
	 * @return チャット投稿のログデータ
	 */
	public Object[] editLogContentList(boolean isGoverment, String accessId, String loginId, String departmentName,
			Integer chatId) {

		// チャット情報取得
		List<Chat> chatList = chatRepository.findByChatId(chatId);
		if (chatList.size() == 0) {
			LOGGER.warn("O_チャットのデータが存在しません。　チャットID：" + chatId);
			throw new RuntimeException();
		}
		Chat chatEntity = chatList.get(0);

		// 申請ID
		Integer applicationId = chatEntity.getApplicationId();
		// O_申請を取得
		Application application = getApplication(applicationId);

		// 申請種類
		String applicationTypeName = getApplicationTypeName(application.getApplicationTypeId());
		// 申請段階
		Integer applicationStepId = chatEntity.getApplicationStepId();
		String applicationStepName = getApplicationStepName(applicationStepId);

		// 回答ID
		String answerIdStr = EMPTY;
		// 問合せ部署
		String inquiryDepartmentName = EMPTY;

		// 事前相談：回答ID＝O_回答ID、問合せ部署＝空

		if (APPLICATION_STEP_ID_1.equals(applicationStepId)) {
			// 回答ID
			answerIdStr = chatEntity.getAnswerId().toString();
			// 問合せ部署
			inquiryDepartmentName = EMPTY;
		}

		// 事前協議：回答ID＝空、問合せ部署＝O_部署回答に対する部署名

		if (APPLICATION_STEP_ID_2.equals(applicationStepId)) {
			// 回答ID
			answerIdStr = EMPTY;

			// 問合せ部署
			Integer departmentAnswerId = chatEntity.getDepartmentAnswerId();
			DepartmentAnswer departmentAnswer = answerDepartmentRepository.findByAnswerId(departmentAnswerId);

			List<Department> departmentList = departmentRepository
					.getDepartmentListById(departmentAnswer.getDepartmentId());

			if (departmentList.size() == 0) {

				inquiryDepartmentName = EMPTY;
			} else {
				inquiryDepartmentName = departmentList.get(0).getDepartmentName();
			}

		}
		// 許可判定：回答ID＝空、問合せ部署＝空

		if (APPLICATION_STEP_ID_3.equals(applicationStepId)) {
			// 回答ID
			answerIdStr = EMPTY;
			// 問合せ部署
			inquiryDepartmentName = EMPTY;
		}

		if (isGoverment) {
			// チャット投稿（行政）
			Object[] logData = { accessId, LogUtil.localDateTimeToString(LocalDateTime.now()), loginId, departmentName,
					applicationId, applicationTypeName, applicationStepName, answerIdStr, inquiryDepartmentName };

			return logData;
		} else {
			// チャット投稿（事業者）
			Object[] logData = { accessId, LogUtil.localDateTimeToString(LocalDateTime.now()), applicationId,
					applicationTypeName, applicationStepName, answerIdStr, inquiryDepartmentName };

			return logData;
		}
	}
}
