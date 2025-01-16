package developmentpermission.service;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.transaction.Transactional;

import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.CaseFormat;

import developmentpermission.dao.ApplicationDao;
import developmentpermission.entity.AnswerName;
import developmentpermission.entity.ApplicantInformationItem;
import developmentpermission.entity.ApplicantInformationItemOption;
import developmentpermission.entity.ApplicationCategoryMaster;
import developmentpermission.entity.ApplicationCategorySelectionView;
import developmentpermission.entity.ApplicationStep;
import developmentpermission.entity.ApplicationType;
import developmentpermission.entity.ApplyLotNumber;
import developmentpermission.entity.Department;
import developmentpermission.entity.DevelopmentDocument;
import developmentpermission.entity.InquiryAddress;
import developmentpermission.entity.LedgerMaster;
import developmentpermission.entity.LotNumberAndDistrict;
import developmentpermission.entity.LotNumberSearchResultDefinition;
import developmentpermission.form.ApplicationCategoryForm;
import developmentpermission.form.ApplicationCategorySelectionViewForm;
import developmentpermission.form.ApplicationTypeForm;
import developmentpermission.form.ApplicationStepForm;
import developmentpermission.form.ApplyLotNumberForm;
import developmentpermission.form.DepartmentForm;
import developmentpermission.form.DevelopmentDocumentFileForm;
import developmentpermission.form.InquiryAddressForm;
import developmentpermission.form.ItemAnswerStatusForm;
import developmentpermission.form.JudgementTypeForm;
import developmentpermission.form.LedgerMasterForm;
import developmentpermission.form.LotNumberForm;
import developmentpermission.form.QuestionaryPurposeForm;
import developmentpermission.form.StatusForm;
import developmentpermission.form.AnswerStatusForm;
import developmentpermission.form.ApplicantInformationItemForm;
import developmentpermission.form.ApplicantInformationItemOptionForm;
import developmentpermission.form.AnswerNameForm;
import developmentpermission.repository.DepartmentRepository;
import developmentpermission.repository.GovernmentUserRepository;
import developmentpermission.repository.AnswerNameRepository;
import developmentpermission.repository.ApplicantInformationItemOptionRepository;
import developmentpermission.repository.ApplicantInformationItemRepository;
import developmentpermission.repository.ApplicationStepRepository;
import developmentpermission.repository.ApplicationTypeRepository;
import developmentpermission.util.CalendarUtil;
import developmentpermission.util.MailMessageUtil;
import developmentpermission.util.MailSendUtil;
import developmentpermission.util.model.MailItem;

/**
 * 共通Serviceクラス
 */
@Service
@Transactional
public abstract class AbstractService {

	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractService.class);

	/** 空文字 */
	public static final String EMPTY = "";
	/** コンマ */
	public static final String COMMA = ",";
	/** パス区切り文字 */
	public static final String PATH_SPLITTER = "/";
	/** 改行文字(LF) */
	public static final String LF = "\n";
	/** 改行文字(CR) */
	public static final String CR = "\r";

	/** 回答ステータス: 申請中 */
	public static final String STATE_APPLYING = "0";
	/** 回答ステータス: 回答中 */
	public static final String STATE_ANSWERING = "1";
	/** 回答ステータス: 回答完了 */
	public static final String STATE_ANSWERED = "2";
	/** 回答ステータス: 通知済み */
	public static final String STATE_NOTIFIED = "3";
	/** 回答ステータス： 通知済み（要再申請） */
	public static final String STATE_NOTIFIED_REAPP = "4";

	/** 登録ステータス: 仮申請中 */
	public static final String STATE_PROVISIONAL = "0";
	/** 登録ステータス: 申請済 */
	public static final String STATE_APPLIED = "1";

	/** メッセージタイプ：1：事業者→行政 */
	public static final Integer MESSAGE_TYPE_BUSINESS_TO_GOVERNMENT = 1;
	/** メッセージタイプ：2：行政→事業者 */
	public static final Integer MESSAGE_TYPE_GOVERNMENT_TO_BUSINESS = 2;
	/** メッセージタイプ：3：行政→行政 */
	public static final Integer MESSAGE_TYPE_GOVERNMENT_TO_GOVERNMENT = 3;

	/** 回答ファイル更新タイプ: 1: 追加 */
	public static final Integer ANSWER_FILE_HISTORY_ADD = 1;
	/** 回答ファイル更新タイプ: 2: 更新 */
	public static final Integer ANSWER_FILE_HISTORY_UPDATE = 2;
	/** 回答ファイル更新タイプ: 3: 削除 */
	public static final Integer ANSWER_FILE_HISTORY_DELETE = 3;

	/** 権限の種類: 権限なし */
	public static final String AUTH_TYPE_NONE = "0";
	/** 権限の種類: 自身部署のみ操作可 */
	public static final String AUTH_TYPE_SELF = "1";
	/** 権限の種類: 他の部署も操作可 */
	public static final String AUTH_TYPE_ALL = "2";

	/** 申請段階: 1（：事前相談） */
	public static final Integer APPLICATION_STEP_ID_1 = 1;
	/** 申請段階: 2（：事前協議） */
	public static final Integer APPLICATION_STEP_ID_2 = 2;
	/** 申請段階: 3（：許可判定） */
	public static final Integer APPLICATION_STEP_ID_3 = 3;

	/** 申請ステータス: 事前相談：未回答 */
	public static final String STATUS_CONSULTATION_NOTANSWERED = "101";
	/** 申請ステータス: 事前相談：未完（回答準備中） */
	public static final String STATUS_CONSULTATION_ANSWERED_PREPARING = "102";
	/** 申請ステータス: 事前相談：未完（回答精査中） */
	public static final String STATUS_CONSULTATION_ANSWERED_REVIEWING = "103";
	/** 申請ステータス: 事前相談：未完（要再申請） */
	public static final String STATUS_CONSULTATION_REAPP = "104";
	/** 申請ステータス: 事前相談：完了 */
	public static final String STATUS_CONSULTATION_COMPLETED = "105";
	/** 申請ステータス: 事前協議：未回答 */
	public static final String STATUS_DISCUSSIONS_NOTANSWERED = "201";
	/** 申請ステータス: 事前協議：未完（回答準備中） */
	public static final String STATUS_DISCUSSIONS_ANSWERED_PREPARING = "202";
	/** 申請ステータス: 事前協議：未完（回答精査中） */
	public static final String STATUS_DISCUSSIONS_ANSWERED_REVIEWING = "203";
	/** 申請ステータス: 事前協議：未完（協議進行中） */
	public static final String STATUS_DISCUSSIONS_IN_PROGRESS = "204";
	/** 申請ステータス: 事前協議：未完（要再申請） */
	public static final String STATUS_DISCUSSIONS_REAPP = "205";
	/** 申請ステータス: 事前協議：完了 */
	public static final String STATUS_DISCUSSIONS_COMPLETED = "206";
	/** 申請ステータス: 許可判定：未回答 */
	public static final String STATUS_PERMISSION_NOTANSWERED = "301";
	/** 申請ステータス: 許可判定：未完（回答準備中） */
	public static final String STATUS_PERMISSION_ANSWERED_PREPARING = "302";
	/** 申請ステータス: 許可判定：未完（回答精査中） */
	public static final String STATUS_PERMISSION_ANSWERED_REVIEWING = "303";
	/** 申請ステータス: 許可判定：未完（要再申請） */
	public static final String STATUS_PERMISSION_REAPP = "304";
	/** 申請ステータス: 許可判定：完了 */
	public static final String STATUS_PERMISSION_COMPLETED = "305";

	/** 回答ステータス: 未回答 */
	public static final String ANSWER_STATUS_NOTANSWERED = "0";
	/** 回答ステータス: 回答済み */
	public static final String ANSWER_STATUS_ANSWERED = "1";
	/** 回答ステータス: 承認待ち */
	public static final String ANSWER_STATUS_APPROVING = "2";
	/** 回答ステータス: 否認済み */
	public static final String ANSWER_STATUS_DENIED = "3";
	/** 回答ステータス: 承認済み */
	public static final String ANSWER_STATUS_APPROVED = "4";
	/** 回答ステータス: 却下 */
	public static final String ANSWER_STATUS_REJECTED = "5";
	/** 回答ステータス: 同意済み */
	public static final String ANSWER_STATUS_AGREED = "6";

	/** 回答データ種類:登録 */
	public static final String ANSWER_DATA_TYPE_INSERT = "0";
	/** 回答データ種類: 更新 */
	public static final String ANSWER_DATA_TYPE_UPDATE = "1";
	/** 回答データ種類: 追加 */
	public static final String ANSWER_DATA_TYPE_ADD = "2";
	/** 回答データ種類: 行政追加 */
	public static final String ANSWER_DATA_TYPE_GOVERNMENT_ADD = "3";
	/** 回答データ種類: 一律追加 */
	public static final String ANSWER_DATA_TYPE_UNIFORM = "4";
	/** 回答データ種類: 削除済み */
	public static final String ANSWER_DATA_TYPE_DELETE = "5";
	/** 回答データ種類: 引継 */
	public static final String ANSWER_DATA_TYPE_TAKEOVER = "6";
	/** 回答データ種類: 削除済み（行政） */
	public static final String ANSWER_DATA_TYPE_GOVERNMENT_DELETE = "7";

	/** 申請者項目データ型: テキスト */
	public static final String APPLICANT_ITEM_TYPE_TEXT = "0";
	
	/** 申請者項目データ型: テキストエリア */
	public static final String APPLICANT_ITEM_TYPE_TEXTAREA = "1";
	
	/** 申請者項目データ型: 日付 */
	public static final String APPLICANT_ITEM_TYPE_DATE = "2";
	
	/** 申請者項目データ型: 数値 */
	public static final String APPLICANT_ITEM_TYPE_NUMBER = "3";
	
	/** 申請者項目データ型: ドロップダウン(単一選択) */
	public static final String ITEM_TYPE_SINGLE_SELECTION = "4";

	/** 申請者項目データ型: ドロップダウン（複数選択） */
	public static final String ITEM_TYPE_MULTIPLE_SELECTION = "5";

	/** 申請者項目データ型: 日付：フォーマット */
	public static final String ITEM_TYPE_DATE_FORMAT = "yyyy-MM-dd";

	/** 事業者合否ステータス　"":未選択 */
	public static final String BUSINESS_PASS_STATUS_NOT_SELECTED = "";
	/** 事業者合否ステータス 1:合意 */
	public static final String BUSINESS_PASS_STATUS_1_AGREE = "1";
	/** 事業者合否ステータス0:否決 */
	public static final String BUSINESS_PASS_STATUS_0_AGREE = "0";

	/** 事業者合否ステータス名　"":未選択 */
	public static final String BUSINESS_PASS_STATUS_NOT_SELECTED_NAME = "未選択";
	/** 事業者合否ステータス名 1:合意 */
	public static final String BUSINESS_PASS_STATUS_1_AGREE_NAME = "合意";
	/** 事業者合否ステータス名　0:否決 */
	public static final String BUSINESS_PASS_STATUS_0_AGREE_NAME = "否決";
	
	/** 申請者情報 連絡先フラグ 無効 */
	public static final String CONTACT_ADDRESS_INVALID = "0";

	/** 申請者情報 連絡先フラグ 有効 */
	public static final String CONTACT_ADDRESS_VALID = "1";

	/** 事前協議の受付フラグ 0=未確認 */
	public static final String ACCEPTING_FLAG_0_UNCONFIRMED = "0";
	/** 事前協議の受付フラグ 1=受付 */
	public static final String ACCEPTING_FLAG_1_ACCEPTED = "1";
	/** 事前協議の受付フラグ 2=差戻 */
	public static final String ACCEPTING_FLAG_2_REMANDED = "2";
	
	/** 回答通知種類 0:事業者に回答通知 */
	public static final String NOTIFY_TYPE_0_ANSWERED = "0";
	/** 回答通知種類 1：事業者に差戻通知 */
	public static final String NOTIFY_TYPE_1_REMANDED = "1";
	/** 回答通知種類 2：担当課に受付通知 */
	public static final String NOTIFY_TYPE_2_ACCEPTED = "2";
	/** 回答通知種類 3：統括部署管理者に回答許可通知 */
	public static final String NOTIFY_TYPE_3_ANSWER_PERMISSION = "3";
	/** 回答通知種類 4：統括部署管理者に行政確定登録許可通知 */
	public static final String NOTIFY_TYPE_4_GOVERNMENT_CONFIRM_PERMISSION = "4";
	
	/** 行政確定登録ステータス 0:合意 */
	public static final String GOVERNMENT_CONFIRM_STATUS_0_AGREE = "0";
	/** 行政確定登録ステータス 1:取下 */
	public static final String GOVERNMENT_CONFIRM_STATUS_1_WITHDRAW = "1";
	/** 行政確定登録ステータス 2:却下 */
	public static final String GOVERNMENT_CONFIRM_STATUS_2_REJECT= "2";

	/** 行政確定登録ステータス名 0:合意 */
	public static final String GOVERNMENT_CONFIRM_STATUS_0_AGREE_NAME = "合意";
	/** 行政確定登録ステータス名 1:取下 */
	public static final String GOVERNMENT_CONFIRM_STATUS_1_WITHDRAW_NAME = "取下";
	/** 行政確定登録ステータス名 2:却下 */
	public static final String GOVERNMENT_CONFIRM_STATUS_2_REJECT_NAME = "却下";
	
	/** EPSG(初期値:4612) */
	@Value("${app.epsg:4612}")
	protected int epsg; // 変数をstaticにすると@Valueで値が設定されなくなるので注意
	/** 地理座標系用EPSG(初期値:4612) */
	@Value("${app.lonlat.epsg:4612}")
	protected int lonlatEpsg;

	/** 市町村名 */
	@Value("${app.city.name}")
	protected String cityName;

	/** ステータス定義JSON */
	@Value("${app.def.status}")
	protected String statusJSON;

	/** ステータス定義JSON */
	@Value("${app.def.answerstatus}")
	protected String answerStatusJSON;
	
	/** 条文ステータス定義JSON */
	@Value("${app.def.itemanswerstatus}")
	protected String itemAnswerStatusJSON;
	
	/** アンケートの利用目的定義JSON */
	@Value("${app.def.questionarypurpose}")
	protected String questionarypurposeJSON;

	/** 概況診断タイプ定義JSON */
	@Value("${app.def.judgementType}")
	protected String judgementTypeJSON;

	/** 概況診断タイプリストのデフォルト選択値 */
	@Value("${app.judgementtype.default.value}")
	protected int judgementTypeValue;

	/** 範囲選択時地番取得上限 */
	@Value("${app.lotnumber.getfigure.limit}")
	protected int figureLotNumberLimit;

	/** テーブル種別: 地番 */
	@Value("${app.lotnumber.result.type.lotnumber:1}")
	protected String typeLotNumber;
	/** テーブル種別: 大字 */
	@Value("${app.lotnumber.result.type.district:0}")
	protected String typeDistrict;

	/** ファイル管理rootパス */
	@Value("${app.file.rootpath}")
	protected String fileRootPath;
	/** 申請ファイル管理フォルダパス */
	@Value("${app.file.application.folder}")
	protected String applicationFolderName;
	/** 回答ファイル管理フォルダパス */
	@Value("${app.file.answer.folder}")
	protected String answerFolderName;
	/** 概況診断画像管理フォルダパス */
	@Value("${app.file.judgement.folder}")
	protected String judgementFolderPath;

	/** メール通知系定義プロパティパス */
	@Value("${app.mail.properties.path}")
	protected String mailPropertiesPath;
	/** メールの申請登録日時フォーマット */
	@Value("${app.mail.accept.timestamp.format}")
	protected String mailTimestampFormat;

	/** O_申請者情報の氏名を格納したアイテム番号 */
	@Value("${app.applicant.name.item.number:1}")
	protected int applicantNameItemNumber;

	/** 問い合わせメール通知間隔(分) */
	@Value("${app.mail.send.interval}")
	protected int sendMailInterval;

	/** 問い合わせファイル管理フォルダパス */
	@Value("${app.file.inquiry.folder}")
	protected String inquiryFolderName;

	/** 行政で追加された回答の関連条項の表示文言 */
	@Value("${app.application.goverment.add.answer.title}")
	protected String govermentAddAnswerTitle;

	/** 許可判定の申請登録時に、一律追加の条項の判定項目ID */
	@Value("${app.application.permission.default.add.judgementItemId}")
	protected List<String> defaultAddJudgementItemIdList;

	/** 回答通知時の回答レポートのファイルID */
	@Value("${app.answer.report.fileid}")
	protected String answerReportFileId;

	/** 申請不可能な申請種類（カンマ区切り） */
	@Value("${app.applicationtype.inapplicable}")
	protected String inapplicableApplicationType;

	/** メール送信ユーティリティ */
	@Autowired
	MailSendUtil mailSendutil;

	/** メールメッセージ定義ユーティリティ */
	private MailMessageUtil mailUtil = null;

	/** カレンダーユーティリティ */
	@Autowired
	CalendarUtil calendarUtil;
	
	/** Entityマネージャファクトリ */
	@Autowired
	protected EntityManagerFactory emf; // DAOでは@Autowiredが効かないのでここで定義...

	/** M_部署Repositoryインスタンス */
	@Autowired
	protected DepartmentRepository departmentRepository;

	/** M_行政ユーザRepositoryインスタンス */
	@Autowired
	protected GovernmentUserRepository governmentUserRepository;

	/** 回答者一覧Repositoryインスタンス */
	@Autowired
	protected AnswerNameRepository answerNameRepository;

	/** M_申請区分Repositoryインスタンス */
	@Autowired
	private ApplicationStepRepository applicationStepRepository;

	/** M_申請種類Repositoryインスタンス */
	@Autowired
	private ApplicationTypeRepository applicationTypeRepository;
	
	/** M_申請者情報項目Repositoryインスタンス */
	@Autowired
	private ApplicantInformationItemRepository applicantInformationItemRepository;
	
	
	/** 申請情報項目選択肢Repositoryインスタンス */
	@Autowired
	private ApplicantInformationItemOptionRepository applicantInformationItemOptionRepository;
	
	/**
	 * ステータスフォームリストを取得
	 * 
	 * @return ステータスフォームリスト
	 * @throws Exception 例外
	 */
	public List<StatusForm> getStatusList() throws Exception {
		LOGGER.trace("ステータスフォームリスト取得 開始");
		try {
			List<StatusForm> formList = new ArrayList<StatusForm>();
			Map<String, String> statusMap = getStatusMap();
			for (Map.Entry<String, String> entry : statusMap.entrySet()) {
				StatusForm form = new StatusForm();
				form.setValue(entry.getKey());
				form.setText(entry.getValue());
				form.setChecked(false);
				formList.add(form);
			}
			return formList;
		} finally {
			LOGGER.trace("ステータスフォームリスト取得 終了");
		}
	}

	/**
	 * 問い合わせステータスフォームリストを取得
	 * 
	 * @return 問い合わせステータスフォームリスト
	 * @throws Exception 例外
	 */
	public List<AnswerStatusForm> getAnswerStatusList() throws Exception {
		LOGGER.trace("問い合わせステータスフォームリスト取得 開始");
		try {
			List<AnswerStatusForm> formList = new ArrayList<AnswerStatusForm>();
			Map<String, String> statusMap = getAnswerStatusMap();
			for (Map.Entry<String, String> entry : statusMap.entrySet()) {
				AnswerStatusForm form = new AnswerStatusForm();
				form.setValue(entry.getKey());
				form.setText(entry.getValue());
				form.setChecked(false);
				formList.add(form);
			}
			return formList;
		} finally {
			LOGGER.trace("ステータスフォームリスト取得 終了");
		}
	}
	/**
	 * 条文回答ステータスフォームリストを取得
	 * 
	 * @return 条文回答ステータスフォームリスト
	 * @throws Exception 例外
	 */
	public List<ItemAnswerStatusForm> getItemAnswerStatusList() throws Exception {
		LOGGER.trace("条文回答ステータスフォームリスト取得 開始");
		try {
			List<ItemAnswerStatusForm> formList = new ArrayList<ItemAnswerStatusForm>();
			Map<String, String> statusMap = getItemAnswerStatusMap();
			Map<String, List<String>> collectMap = new HashMap<String, List<String>>();
			for (Map.Entry<String, String> entry : statusMap.entrySet()) {
				if (collectMap.containsKey(entry.getValue())) {
					collectMap.get(entry.getValue()).add(entry.getKey());
				} else {
					List<String> aList = new ArrayList<String>();
					aList.add(entry.getKey());
					collectMap.put(entry.getValue(), aList);
				}
			}
			// 同じラベルとなるキーをカンマ区切り文字列で集約
			for (Map.Entry<String, List<String>> entry: collectMap.entrySet()) {
				ItemAnswerStatusForm form = new ItemAnswerStatusForm();
				form.setValue(String.join(",",entry.getValue()));
				form.setText(entry.getKey());
				form.setChecked(false);
				formList.add(form);
			}
			
			return formList;
		} finally {
			LOGGER.trace("ステータスフォームリスト取得 終了");
		}
	}
	/**
	 * アンケート利用目的フォームリストを取得
	 * 
	 * @return アンケート利用目的フォームリスト
	 * @throws Exception 例外
	 */
	public List<QuestionaryPurposeForm> getQuestionaryPurposeList() throws Exception {
		LOGGER.trace(" アンケート利用目的フォームリスト取得 開始");
		try {
			List<QuestionaryPurposeForm> formList = new ArrayList<QuestionaryPurposeForm>();
			Map<String, String> purposeMap = getQuestionaryPurposeMap();
			for (Map.Entry<String, String> entry : purposeMap.entrySet()) {
				QuestionaryPurposeForm form = new QuestionaryPurposeForm();
				form.setValue(entry.getKey());
				form.setText(entry.getValue());
				form.setChecked(false);
				formList.add(form);
			}
			return formList;
		} finally {
			LOGGER.trace(" アンケート利用目的フォームリスト取得 終了");
		}
	}

	/**
	 * 概況診断タイプフォームリストを取得
	 * 
	 * @return 概況診断タイプフォームリスト
	 * @throws Exception 例外
	 */
	public List<JudgementTypeForm> getJudgementTypeList() throws Exception {
		LOGGER.trace(" 概況診断タイプフォームリスト取得 開始");
		try {
			List<JudgementTypeForm> formList = new ArrayList<JudgementTypeForm>();
			Map<String, String> purposeMap = getJudgementTypeMap();
			for (Map.Entry<String, String> entry : purposeMap.entrySet()) {
				JudgementTypeForm form = new JudgementTypeForm();
				form.setValue(entry.getKey());
				form.setText(entry.getValue());
				form.setChecked(String.valueOf(judgementTypeValue).equals(entry.getKey()));
				formList.add(form);
			}
			return formList;
		} finally {
			LOGGER.trace(" 概況診断タイプフォームリスト取得 終了");
		}
	}

	/**
	 * M_部署一覧を取得
	 * 
	 * @return M_部署一覧
	 */
	public List<DepartmentForm> getDepartmentList() {
		LOGGER.trace("M_部署一覧取得 開始");
		try {
			List<DepartmentForm> formList = new ArrayList<DepartmentForm>();
			List<Department> departmentList = departmentRepository.getDepartmentList();
			for (Department department : departmentList) {
				formList.add(getDepartmentFormFromEntity(department));
			}
			return formList;
		} finally {
			LOGGER.trace("M_部署一覧取得 終了");
		}
	}

	/**
	 * 回答者一覧を取得
	 * 
	 * @return 回答者一覧
	 */
	public List<AnswerNameForm> getAnswerNameList() {
		LOGGER.trace("回答者一覧取得 開始");
		try {
			List<AnswerNameForm> formList = new ArrayList<AnswerNameForm>();
			List<AnswerName> answerNameList = answerNameRepository.getAnswerNameList();
			for (AnswerName answerName : answerNameList) {
				formList.add(getAnswerNameFormFromEntity(answerName));
			}
			return formList;
		} finally {
			LOGGER.trace("回答者一覧取得 終了");
		}
	}
	
	/**
	 * 申請種類一覧を取得
	 * 
	 * @return 申請種類一覧
	 */
	public List<ApplicationTypeForm> getApplicationTypeList() {
		LOGGER.trace("申請種類一覧取得 開始");
		try {
			List<ApplicationTypeForm> formList = new ArrayList<ApplicationTypeForm>();
			List<ApplicationType> applicationTypeList = applicationTypeRepository.getApplicationTypeList();
			for (ApplicationType applicationType : applicationTypeList) {
				formList.add(getApplicationTypeFormEntity(applicationType));
			}
			return formList;
		} finally {
			LOGGER.trace("申請種類一覧取得 終了");
		}
	}
	
	/**
	 * 申請段階一覧を取得
	 * 
	 * @return 申請段階一覧
	 */
	public List<ApplicationStepForm> getApplicationStepList() {
		LOGGER.trace("申請段階一覧取得 開始");
		try {
			List<ApplicationStepForm> formList = new ArrayList<ApplicationStepForm>();
			List<ApplicationStep> applicationStepList = applicationStepRepository.findByApplicationStepList();
			for (ApplicationStep applicationStep : applicationStepList) {
				formList.add(getApplicationStepFormEntity(applicationStep));
			}
			return formList;
		} finally {
			LOGGER.trace("申請種類一覧取得 終了");
		}
	}
	
	/**
	 * "申請追加情報一覧を取得
	 * 
	 * @return 申請追加情報一覧
	 */
	public List<ApplicantInformationItemForm> getApplicantInformationItemList() {
		LOGGER.trace("申請追加情報一覧取得 開始");
		try {
			List<ApplicantInformationItemForm> formList = new ArrayList<ApplicantInformationItemForm>();
			List<ApplicantInformationItem> applicantInformationItemFormList = applicantInformationItemRepository.getApplicantItemsAddFlagOn();
			// 申請段階情報を取得
			List<ApplicationStepForm> applicationStepList = getApplicationStepList();
			for (ApplicantInformationItem applicantInformationItemForm : applicantInformationItemFormList) {
				formList.add(getApplicantInformationItemFormEntityForSearhCondition(applicantInformationItemForm, applicationStepList));
			}
			return formList;
		} finally {
			LOGGER.trace("申請追加情報一覧取得 終了");
		}
	}
	
	/**
	 * "申請情報項目選択肢一覧を取得
	 * 
	 * @return 申請情報項目選択肢一覧
	 */
	public List<ApplicantInformationItemOptionForm> getApplicantInformationItemOptionList(String applicantInformationItemId) {
		LOGGER.trace("申請情報項目選択肢一覧取得 開始");
		try {
			List<ApplicantInformationItemOptionForm> formList = new ArrayList<ApplicantInformationItemOptionForm>();
			List<ApplicantInformationItemOption> applicantInformationItemOptionList = applicantInformationItemOptionRepository.findByApplicantInformationItemId(applicantInformationItemId);
			for (ApplicantInformationItemOption applicantInformationItemOption : applicantInformationItemOptionList) {
				formList.add(getApplicantInformationItemOptionFormEntity(applicantInformationItemOption));
			}
			return formList;
		} finally {
			LOGGER.trace("申請情報項目選択肢一覧取得 終了");
		}
	}

	/**
	 * ステータス定義を取得
	 * 
	 * @return ステータス定義
	 * @throws Exception 例外
	 */
	protected Map<String, String> getStatusMap() throws Exception {
		LOGGER.trace("ステータス定義取得 開始");
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			Map<String, String> map = objectMapper.readValue(statusJSON,
					new TypeReference<LinkedHashMap<String, String>>() {
					});
			return map;
		} finally {
			LOGGER.trace("ステータス定義取得 終了");
		}
	}

	/**
	 * 問い合わせステータス定義を取得
	 * 
	 * @return ステータス定義
	 * @throws Exception 例外
	 */
	protected Map<String, String> getAnswerStatusMap() throws Exception {
		LOGGER.trace("問い合わせステータス定義取得 開始");
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			Map<String, String> map = objectMapper.readValue(answerStatusJSON,
					new TypeReference<LinkedHashMap<String, String>>() {
					});
			return map;
		} finally {
			LOGGER.trace("問い合わせステータス定義取得 終了");
		}
	}
	/**
	 * 条文回答ステータス定義を取得
	 * 
	 * @return ステータス定義
	 * @throws Exception 例外
	 */
	protected Map<String, String> getItemAnswerStatusMap() throws Exception {
		LOGGER.trace("条文回答ステータス定義取得 開始");
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			Map<String, String> map = objectMapper.readValue(itemAnswerStatusJSON,
					new TypeReference<LinkedHashMap<String, String>>() {
					});
			return map;
		} finally {
			LOGGER.trace("条文回答ステータス定義取得 終了");
		}
	}
	/**
	 * アンケートの利用目的定義を取得
	 * 
	 * @return 利用目的定義
	 * @throws Exception 例外
	 */
	protected Map<String, String> getQuestionaryPurposeMap() throws Exception {
		LOGGER.trace("アンケートの利用目的定義取得 開始");
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			Map<String, String> map = objectMapper.readValue(questionarypurposeJSON,
					new TypeReference<LinkedHashMap<String, String>>() {
					});
			return map;
		} finally {
			LOGGER.trace("アンケートの利用目的定義取得 終了");
		}
	}

	/**
	 * 概況診断タイプ定義を取得
	 * 
	 * @return 概況診断タイプ定義
	 * @throws Exception 例外
	 */
	protected Map<String, String> getJudgementTypeMap() throws Exception {
		LOGGER.trace("概況診断タイプ定義取得 開始");
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			Map<String, String> map = objectMapper.readValue(judgementTypeJSON,
					new TypeReference<LinkedHashMap<String, String>>() {
					});
			return map;
		} finally {
			LOGGER.trace("概況診断タイプ定義取得 終了");
		}
	}

	/**
	 * リストの要素をコンマで決号した文字列にして返す
	 * 
	 * @param list リスト
	 * @return 結合文字列
	 */
	protected String joinList(List<Object> list) {
		StringBuffer sb = new StringBuffer();
		for (Object obj : list) {
			if (obj != null) {
				if (sb.length() > 0) {
					sb.append(COMMA);
				}
				sb.append(obj.toString());
			}
		}
		return sb.toString();
	}

	/**
	 * F_部署EntityをF_部署フォームに詰めなおす
	 * 
	 * @param entity M_部署Entity
	 * @return M_部署フォーム
	 */
	protected DepartmentForm getDepartmentFormFromEntity(Department entity) {
		DepartmentForm form = new DepartmentForm();
		form.setDepartmentId(entity.getDepartmentId());
		form.setDepartmentName(entity.getDepartmentName());
		form.setChecked(false);
		form.setMailAddress(entity.getMailAddress());
		form.setAdminMailAddress(entity.getAdminMailAddress());
		return form;
	}

	/**
	 * 
	 * @param entity 回答者Entity
	 * @return 回答者フォーム
	 */
	protected AnswerNameForm getAnswerNameFormFromEntity(AnswerName entity) {
		AnswerNameForm form = new AnswerNameForm();
		form.setUserId(entity.getUserId());
		form.setLoginId(entity.getLoginId());
		form.setUserName(entity.getUserName());
		form.setDepartmentId(entity.getDepartmentId());
		form.setDepartmentName(entity.getDepartmentName());
		form.setChecked(false);
		return form;
	}
	
	/**
	 * 
	 * @param entity 申請種類Entity
	 * @return 申請種類フォーム
	 */
	protected ApplicationTypeForm getApplicationTypeFormEntity(ApplicationType entity) {
		ApplicationTypeForm form = new ApplicationTypeForm();
		form.setApplicationTypeId(entity.getApplicationTypeId());
		form.setApplicationTypeName(entity.getApplicationTypeName());
		form.setApplicationSteps(getApplicationStepList());
		return form;
	}
	
	/**
	 * 
	 * @param entity 申請段階Entity
	 * @return 申請段階フォーム
	 */
	protected ApplicationStepForm getApplicationStepFormEntity(ApplicationStep entity) {
		ApplicationStepForm form = new ApplicationStepForm();
		form.setApplicationStepId(entity.getApplicationStepId());
		form.setApplicationStepName(entity.getApplicationStepName());
		return form;
	}
	
	/**
	 * 
	 * @param entity 申請追加情報Entity
	 * @return 申請追加情報フォーム
	 */
	protected ApplicantInformationItemForm getApplicantInformationItemFormEntity(ApplicantInformationItem entity) {
		ApplicantInformationItemForm form = new ApplicantInformationItemForm();
		form.setId(entity.getApplicantInformationItemId());
		form.setOrder(entity.getDisplayOrder());
		form.setDisplayFlag(entity.getDisplayFlag());
		form.setRequireFlag(entity.getRequireFlag());
		form.setSearchConditionFlag(entity.getSearchConditionFlag());
		form.setName(entity.getItemName());
		form.setRegularExpressions(entity.getRegex());
		form.setMailAddress(entity.getMailAddress());
		form.setItemType(entity.getItemType());
		form.setAddInformationItemFlag(entity.getAddInformationItemFlag());
		form.setApplicationSteps(getApplicationStepList());
		form.setItemOptions(getApplicantInformationItemOptionList(entity.getApplicantInformationItemId()));
		return form;
	}
	
	/**
	 * 
	 * @param entity 申請情報項目選択肢Entity
	 * @return 申請情報項目選択肢フォーム
	 */
	protected ApplicantInformationItemOptionForm getApplicantInformationItemOptionFormEntity(ApplicantInformationItemOption entity) {
		ApplicantInformationItemOptionForm form = new ApplicantInformationItemOptionForm();
		form.setId(entity.getApplicantInformationItemOptionId());
		form.setItemId(entity.getApplicantInformationItemId());
		form.setDisplayOrder(entity.getDisplayOrder());
		form.setContent(entity.getApplicantInformationItemOptionName());
		form.setChecked(false);
		return form;
	}

	/**
	 * M_申請区分選択画面EntityをM_申請区分選択画面フォームに詰めなおす
	 * 
	 * @param entity M_申請区分選択画面Entity
	 * @return M_申請区分選択画面フォーム
	 */
	protected ApplicationCategorySelectionViewForm getSelectionViewFormFromEntity(
			ApplicationCategorySelectionView entity) {
		ApplicationCategorySelectionViewForm form = new ApplicationCategorySelectionViewForm();
		form.setEnable(entity.getViewFlag());
		form.setExplanation(entity.getDescription());
		form.setMultiple(entity.getMultipleFlag());
		form.setRequire(entity.getRequireFlag());
		form.setScreenId(entity.getViewId());
		form.setTitle(entity.getTitle());
		form.setJudgementType(entity.getJudgementType());
		return form;
	}

	/**
	 * M_申請区分選択画面EntityをM_申請区分選択画面フォームに詰めなおす
	 * 
	 * @param entity             M_申請区分選択画面Entity
	 * @param applicationId      申請ID
	 * @param applicationStepId  申請段階ID
	 * @param versionInformation 申請版情報
	 * @return M_申請区分選択画面フォーム
	 */
	protected ApplicationCategorySelectionViewForm getSelectionViewFormFromEntity(
			ApplicationCategorySelectionView entity, int applicationId, int applicationStepId, int versionInformation) {
		ApplicationCategorySelectionViewForm form = new ApplicationCategorySelectionViewForm();
		form.setEnable(entity.getViewFlag());
		form.setExplanation(entity.getTitle());
		form.setMultiple(entity.getMultipleFlag());
		form.setRequire(entity.getRequireFlag());
		form.setScreenId(entity.getViewId());
		form.setTitle(entity.getTitle());

		List<ApplicationCategoryForm> categoryFormList = new ArrayList<ApplicationCategoryForm>();

		ApplicationDao dao = new ApplicationDao(emf);
		List<ApplicationCategoryMaster> categoryList = dao.getApplicationCategoryMasterList(applicationId,
				entity.getViewId(), applicationStepId, versionInformation);
		for (ApplicationCategoryMaster category : categoryList) {
			categoryFormList.add(getApplicationCategoryFormFromEntity(category));
		}
		form.setApplicationCategory(categoryFormList);

		return form;
	}

	/**
	 * M_申請区分EntityをM_申請区分フォームに詰めなおす
	 * 
	 * @param entity M_申請区分Entity
	 * @return M_申請区分フォーム
	 */
	protected ApplicationCategoryForm getApplicationCategoryFormFromEntity(ApplicationCategoryMaster entity) {
		ApplicationCategoryForm form = new ApplicationCategoryForm();
		form.setId(entity.getCategoryId());
		form.setScreenId(entity.getViewId());
		form.setOrder(entity.getOrder());
		form.setContent(entity.getLabelName());
		return form;
	}

	/**
	 * F_地番EntityをF_地番フォームに詰めなおす
	 * 
	 * @param entity F_地番Entity
	 * @return F_地番フォーム
	 */
	protected ApplyLotNumberForm getApplyingLotNumberFormFromEntity(ApplyLotNumber entity) {
		ApplyLotNumberForm form = new ApplyLotNumberForm();
		form.setApplicationId(entity.getApplicationId());
		form.setLot_numbers(entity.getLotNumbers());
		form.setLon(entity.getLon());
		form.setLat(entity.getLat());
		form.setMaxlon(entity.getMaxlon());
		form.setMaxlat(entity.getMaxlat());
		form.setMinlon(entity.getMinlon());
		form.setMinlat(entity.getMinlat());
		form.setStatus(entity.getStatus());
		return form;
	}
	
	/**
	 * F_地番EntityをF_地番フォームに詰めなおす
	 * 
	 * @param entity F_地番Entity
	 * @return F_地番フォーム
	 */
	protected LotNumberForm getLotNumberFormFromEntity(LotNumberAndDistrict entity,
			List<LotNumberSearchResultDefinition> lotNumberSearchResultDefinitionList) {
		LotNumberForm form = new LotNumberForm();
		form.setChibanId(entity.getChibanId());
		form.setChiban(entity.getChiban());
		form.setCityName(cityName);
		form.setDistrictId(entity.getDistrictId());
		form.setDistrictName(entity.getOoazaDistrictName());
		form.setDistrictKana(entity.getOoazaDistrictKana());
		form.setLon(entity.getLon());
		form.setLat(entity.getLat());
		form.setMinlon(entity.getMinlon());
		form.setMinlat(entity.getMinlat());
		form.setMaxlon(entity.getMaxlon());
		form.setMaxlat(entity.getMaxlat());
		form.setStatus(entity.getStatus());
		if (entity.getStatus() != null) {
			try {
				Map<String, String> statusMap = getStatusMap();
				form.setStatusText(statusMap.get(entity.getStatus()));
			} catch (Exception e) {
				form.setStatusText("");
			}

		} else {
			form.setStatusText("");
		}
		form.setApplicationId(entity.getApplicationId());

		// attributesの設定
		Map<String, Object> attributes = new LinkedHashMap<String, Object>();
		if (lotNumberSearchResultDefinitionList != null) {
			for (LotNumberSearchResultDefinition lotNumberSearchResultDefinition : lotNumberSearchResultDefinitionList) {
				String tableType = lotNumberSearchResultDefinition.getTableType();
				String columnName = lotNumberSearchResultDefinition.getTableColumnName();
				String responseKey = lotNumberSearchResultDefinition.getResponseKey();
				if (columnName != null) {
					// 小文字に揃えて比較する
					columnName = columnName.toLowerCase();
					// snake_case から CamelCase への変換
					columnName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, columnName);
					// 先頭文字は常に小文字にする
					if (columnName != null) {
						columnName = columnName.substring(0, 1).toLowerCase() + columnName.substring(1);
					}
				}

				if (tableType != null && responseKey != null && columnName != null) {
					if (typeLotNumber.equals(tableType)) {
						for (int i = 1; i <= 5; i++) {
							String targetColumnName = "resultColumn" + i;
							if (columnName.equals(targetColumnName)) {
								Field field;
								try {
									field = LotNumberAndDistrict.class.getDeclaredField(targetColumnName);
									field.setAccessible(true);
									attributes.put(responseKey, field.get(entity));
									break;
								} catch (Exception e) {
									LOGGER.error("LotNumberAndDistrictのリフレクションで例外発生", e);
								}
							}
						}
					} else if (typeDistrict.equals(tableType)) {
						for (int i = 1; i <= 5; i++) {
							String targetColumnName = "ooazaResultColumn" + i;
							String comparisonColumnName = "resultColumn" + i;
							if (columnName.equals(comparisonColumnName)) {
								Field field;
								try {
									field = LotNumberAndDistrict.class.getDeclaredField(targetColumnName);
									field.setAccessible(true);
									attributes.put(responseKey, field.get(entity));
									break;
								} catch (Exception e) {
									LOGGER.error("LotNumberAndDistrictのリフレクションで例外発生", e);
								}
							}
						}

					}
				}
			}
		}
		form.setAttributes(attributes);
		form.setFullFlag(entity.getFullFlag());

		return form;
	}

	/**
	 * O_問合せ宛先EntityをO_問合せ宛先フォームに詰めなおす
	 * 
	 * @param entity O_問合せ宛先Entity
	 * @return O_問合せ宛先フォーム
	 */
	protected InquiryAddressForm getInquiryAddressFormFromEntity(InquiryAddress entity) {
		InquiryAddressForm form = new InquiryAddressForm();
		form.setInquiryAddressId(entity.getInquiryAddressId());
		form.setMessageId(entity.getMessageId());
		form.setReadFlag(entity.getReadFlag());
		form.setAnswerCompleteFlag(entity.getAnswerCompleteFlag());

		List<Department> departmentList = departmentRepository.getDepartmentListById(entity.getDepartmentId());
		DepartmentForm departmentForm = new DepartmentForm();
		if (departmentList.size() > 0) {
			departmentForm = getDepartmentFormFromEntity(departmentList.get(0));
		}
		departmentForm.setDepartmentId(entity.getDepartmentId());
		form.setDepartment(departmentForm);

		return form;
	}

	/**
	 * M_申請種類EntityをM_申請種類フォームに詰めなおす
	 * 
	 * @param entity M_申請種類Entity
	 * 
	 * @return M_申請種類フォーム
	 */
	protected ApplicationTypeForm getApplicationTypeFormFromEntity(ApplicationType entity) {
		ApplicationTypeForm form = new ApplicationTypeForm();
		form.setApplicationTypeId(entity.getApplicationTypeId());
		form.setApplicationTypeName(entity.getApplicationTypeName());
		form.setChecked(String.valueOf(judgementTypeValue).equals(entity.getApplicationTypeId().toString()));

		List<ApplicationStepForm> applicationStepFormList = new ArrayList<ApplicationStepForm>();
		String applicationStepStr = entity.getApplicationStep();
		if (applicationStepStr != null && !applicationStepStr.isEmpty()) {
			String[] applicationStepIdList = applicationStepStr.split(COMMA);
			for (String applicationStepId : applicationStepIdList) {
				LOGGER.trace("申請段階取得 開始");
				List<ApplicationStep> results = applicationStepRepository
						.findByApplicationStepId(Integer.valueOf(applicationStepId));
				ApplicationStepForm applicationStepForm = new ApplicationStepForm();
				if (results.size() > 0) {
					applicationStepForm = geApplicationStepFormFromEntity(results.get(0));
					applicationStepFormList.add(applicationStepForm);
				}

				LOGGER.trace("申請段階取得 開始");
			}
		}

		// 申請種類に対する申請段階リスト
		form.setApplicationSteps(applicationStepFormList);

		// 申請可能
		final String[] inapplicableApplicationTypes = inapplicableApplicationType.split(",");
		form.setApplicable(!Arrays.asList(inapplicableApplicationTypes).contains(entity.getApplicationTypeId().toString()));
		
		return form;
	}

	/**
	 * M_申請段階EntityをM_申請段階フォームに詰めなおす
	 * 
	 * @param entity M_申請段階Entity
	 * @return M_申請段階フォーム
	 */
	protected ApplicationStepForm geApplicationStepFormFromEntity(ApplicationStep entity) {
		ApplicationStepForm form = new ApplicationStepForm();
		form.setApplicationStepId(entity.getApplicationStepId());
		form.setApplicationStepName(entity.getApplicationStepName());
		form.setChecked(false);
		return form;
	}

	/**
	 * M_帳票EntityをM_帳票フォームに詰めなおす
	 * 
	 * @param entity
	 * @param value
	 * @return
	 */
	protected LedgerMasterForm geledgerMasterFormFromEntity(LedgerMaster entity, String value) {
		LedgerMasterForm form = new LedgerMasterForm();
		form.setLedgerId(entity.getLedgerId());
		form.setLedgerName(entity.getLedgerName());
		form.setApplicationStepId(entity.getApplicationStepId());
		form.setDisplayName(entity.getDisplayName());
		form.setTemplatePath(entity.getTemplatePath());
		form.setOutputType(entity.getOutputType());
		form.setNotificationFlag(entity.getNotificationFlag());
		if (value != null && !EMPTY.equals(value)) {
			String[] ledgerIdList = value.split(COMMA);
			form.setChecked(Arrays.asList(ledgerIdList).contains(entity.getLedgerId()));
		} else {
			form.setChecked(false);
		}

		return form;
	}
	
	/**
	 * O_開発登録簿Entityを開発登録簿ファイルフォームに詰めなおす
	 * 
	 * @param entity O_開発登録簿Entity
	 * @return 開発登録簿ファイルフォーム
	 */
	protected DevelopmentDocumentFileForm getDevelopmentDocumentFileFormEntity(DevelopmentDocument entity,Map<Integer, String> documentNameMap) {
		DevelopmentDocumentFileForm form = new DevelopmentDocumentFileForm();
		
		/** ファイルID */
		form.setFileId(entity.getFileId());
				
		/** 開発登録簿マスタID */
		form.setDevelopmentDocumentId(entity.getDevelopmentDocumentId());
		
		/** 申請ID */
		form.setApplicationId(entity.getApplicationId());
		
		/** 書類名 */
		form.setDocumentName(documentNameMap.get(entity.getDevelopmentDocumentId()));
		
		/** ログインID */
		form.setLoginId("");

		/** パスワード */
		form.setPassword("");
		
		/** アップロード日時 */
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
		String registerDatetime = dateTimeFormatter.format(entity.getRegisterDatetime());
		form.setUploadDatetime(registerDatetime);
		
		return form;
	}
	
	/**
	 * 検索条件向け申請追加情報取得
	 * @param entity 申請追加情報Entity
	 * @return 申請追加情報フォーム
	 */
	private ApplicantInformationItemForm getApplicantInformationItemFormEntityForSearhCondition(ApplicantInformationItem entity, List<ApplicationStepForm> applicationStepList) {
		ApplicantInformationItemForm form = new ApplicantInformationItemForm();
		form.setId(entity.getApplicantInformationItemId());
		form.setOrder(entity.getDisplayOrder());
		form.setDisplayFlag(entity.getDisplayFlag());
		form.setRequireFlag(entity.getRequireFlag());
		form.setSearchConditionFlag(entity.getSearchConditionFlag());
		form.setName(entity.getItemName());
		form.setRegularExpressions(entity.getRegex());
		form.setMailAddress(entity.getMailAddress());
		form.setItemType(entity.getItemType());
		form.setAddInformationItemFlag(entity.getAddInformationItemFlag());
		final String[] applicationStepString = entity.getApplicationStep().split(",");
		final List<ApplicationStepForm> applicationStepForm = new ArrayList<ApplicationStepForm>();
		// 申請情報と紐づく申請段階を取得
		for (String aStep: applicationStepString) {
			for (ApplicationStepForm aForm :applicationStepList) {
				if ((aForm.getApplicationStepId() + "").equals(aStep)) {
					aForm.setChecked(false);
					applicationStepForm.add(aForm);
				}
			}
		}
		form.setApplicationSteps(applicationStepForm);
		form.setItemOptions(getApplicantInformationItemOptionList(entity.getApplicantInformationItemId()));
		return form;
	}
	/**
	 * ファイルのContentTypeを取得
	 * 
	 * @param path ファイルパス
	 * @return ContentType
	 * @throws IOException 例外
	 */
	protected MediaType getContentType(Path path) throws IOException {
		try {
			return MediaType.parseMediaType(Files.probeContentType(path));
		} catch (IOException e) {
			return MediaType.APPLICATION_OCTET_STREAM;
		}
	}

	/**
	 * ファイル出力
	 * 
	 * @param fileData     ファイルデータ
	 * @param filePathText 出力ファイルパス
	 * @throws IOException 例外
	 */
	protected void exportFile(MultipartFile fileData, String filePathText) throws IOException {
		Path filePath = Paths.get(filePathText);
		byte[] bytes = fileData.getBytes();
		try (OutputStream stream = Files.newOutputStream(filePath)) {
			stream.write(bytes);
		}
	}

	/**
	 * EXCELファイル出力
	 * 
	 * @param workBook     ファイルデータ
	 * @param filePathText 出力ファイルパス
	 * @throws IOException 例外
	 */
	protected void exportWorkBook(Workbook workBook, String filePathText) throws IOException {
		Path filePath = Paths.get(filePathText);
		try (OutputStream stream = Files.newOutputStream(filePath)) {
			workBook.write(stream);
		}
	}

	/**
	 * メールメッセージ定義を取得
	 * 
	 * @param key キー
	 * @return メッセージ
	 */
	protected String getMailPropValue(String key, MailItem mailItem) {
		if (mailUtil == null) {
			try {
				mailUtil = new MailMessageUtil(mailPropertiesPath);
			} catch (Exception e) {
				LOGGER.error("メールメッセージ定義初期化エラー", e);
				throw new RuntimeException("メールメッセージ定義初期化エラー");
			}
		}
		return mailUtil.getFormattedValue(key, mailItem);
	}
	
	/**
	 * システム日付からN営業日後の日付を取得
	 * 
	 * @param key キー
	 * @return メッセージ
	 */
	protected LocalDateTime getDeadlineDatetime(Integer answerDays) {

		LocalDateTime deadlineDatetime = null;

		if (answerDays != null) {

			try {

				// N営業日後の日付を取得
				Date date = calendarUtil.calcDeadlineDatetime(new Date(), answerDays);
				if (date != null) {
					// LocalDateTimeデータ型に変換
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
					String str = date.toString().replace("-", "/")+ " 00:00";
					deadlineDatetime = LocalDateTime.parse(str, formatter);
				}
			} catch (Exception e) {
				LOGGER.error("N営業日後の日付取得で例外発生", e);
			}
		}
		return deadlineDatetime;
	}
}
