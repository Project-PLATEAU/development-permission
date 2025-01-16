package developmentpermission.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.StringUtil;
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
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import developmentpermission.dao.AnswerDao;
import developmentpermission.dao.ApplicationDao;
import developmentpermission.dao.ChatDao;
import developmentpermission.dao.GovernmentUserDao;
import developmentpermission.entity.AcceptingAnswer;
import developmentpermission.entity.Answer;
import developmentpermission.entity.AnswerFile;
import developmentpermission.entity.AnswerFileHistory;
import developmentpermission.entity.AnswerFileHistoryView;
import developmentpermission.entity.AnswerHistory;
import developmentpermission.entity.ApplicantInformation;
import developmentpermission.entity.Application;
import developmentpermission.entity.ApplicationFile;
import developmentpermission.entity.ApplicationFileMaster;
import developmentpermission.entity.ApplicationStep;
import developmentpermission.entity.ApplicationType;
import developmentpermission.entity.ApplicationVersionInformation;
import developmentpermission.entity.ApplyLotNumber;
import developmentpermission.entity.Authority;
import developmentpermission.entity.CategoryJudgement;
import developmentpermission.entity.CategoryJudgementAuthority;
import developmentpermission.entity.CategoryJudgementResult;
import developmentpermission.entity.Department;
import developmentpermission.entity.DepartmentAnswer;
import developmentpermission.entity.GovernmentUser;
import developmentpermission.entity.GovernmentUserAndAuthority;
import developmentpermission.entity.Ledger;
import developmentpermission.entity.LedgerMaster;
import developmentpermission.entity.LotNumberAndDistrict;
import developmentpermission.entity.LotNumberSearchResultDefinition;
import developmentpermission.form.AnswerFileForm;
import developmentpermission.form.AnswerFileHistoryForm;
import developmentpermission.form.AnswerForm;
import developmentpermission.form.AnswerHistoryForm;
import developmentpermission.form.AnswerNotifyRequestForm;
import developmentpermission.form.ApplyAnswerDetailForm;
import developmentpermission.form.ApplyAnswerForm;
import developmentpermission.form.ApplyLotNumberForm;
import developmentpermission.form.DepartmentAnswerForm;
import developmentpermission.form.GeneralConditionDiagnosisReportRequestForm;
import developmentpermission.form.GeneralConditionDiagnosisResultForm;
import developmentpermission.form.GovernmentUserForm;
import developmentpermission.form.LedgerForm;
import developmentpermission.form.LedgerMasterForm;
import developmentpermission.form.LotNumberForm;
import developmentpermission.form.QuoteFileForm;
import developmentpermission.form.UploadApplicationFileForm;
import developmentpermission.repository.AcceptingAnswerRepository;
import developmentpermission.repository.AnswerFileRepository;
import developmentpermission.repository.AnswerHistoryRepository;
import developmentpermission.repository.AnswerRepository;
import developmentpermission.repository.ApplicantInformationRepository;
import developmentpermission.repository.ApplicationFileRepository;
import developmentpermission.repository.ApplicationRepository;
import developmentpermission.repository.ApplicationStepRepository;
import developmentpermission.repository.ApplicationTypeRepository;
import developmentpermission.repository.ApplicationVersionInformationRepository;
import developmentpermission.repository.AuthorityRepository;
import developmentpermission.repository.DepartmentAnswerRepository;
import developmentpermission.repository.DepartmentRepository;
import developmentpermission.repository.JudgementAuthorityRepository;
import developmentpermission.repository.JudgementResultRepository;
import developmentpermission.repository.LedgerMasterRepository;
import developmentpermission.repository.LedgerRepository;
import developmentpermission.repository.LotNumberSearchResultDefinitionRepository;
import developmentpermission.repository.RoadJudgeLabelRepository;
import developmentpermission.repository.jdbc.AnswerFileHistoryJdbc;
import developmentpermission.repository.jdbc.AnswerFileJdbc;
import developmentpermission.repository.jdbc.AnswerHistoryJdbc;
import developmentpermission.repository.jdbc.AnswerJdbc;
import developmentpermission.repository.jdbc.ApplicationFileJdbc;
import developmentpermission.repository.jdbc.ApplicationJdbc;
import developmentpermission.repository.jdbc.ApplicationVersionInformationJdbc;
import developmentpermission.repository.jdbc.DepartmentAnswerJdbc;
import developmentpermission.repository.jdbc.LedgerJdbc;
import developmentpermission.repository.jdbc.QuoteFileJdbc;
import developmentpermission.util.AuthUtil;
import developmentpermission.util.ExportJudgeForm;
import developmentpermission.util.LogUtil;
import developmentpermission.util.MailMessageUtil;
import developmentpermission.util.model.MailItem;
import developmentpermission.util.model.MailResultItem;

/**
 * 回答Serviceクラス
 */
@Service
@Transactional
public class AnswerService extends AbstractJudgementService {

	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(AnswerService.class);

	/** O_回答Repositoryインスタンス */
	@Autowired
	private AnswerRepository answerRepository;
	/** O_回答ファイルRepositoryインスタンス */
	@Autowired
	private AnswerFileRepository answerFileRepository;
	/** O_申請Repositoryインスタンス */
	@Autowired
	private ApplicationRepository applicationRepository;
	/** O_申請者情報Repositoryインスタンス */
	@Autowired
	private ApplicantInformationRepository applicantInformationRepository;
	/** M_部署Repositoryインスタンス */
	@Autowired
	private DepartmentRepository departmentRepository;
	/** M_地番検索結果定義Repositoryインスタンス */
	@Autowired
	private LotNumberSearchResultDefinitionRepository lotNumberSearchResultDefinitionRepository;
	/** O_回答履歴Repositoryインスタンス */
	@Autowired
	private AnswerHistoryRepository answerHistoryRepository;

	/** O_申請ファイルRepositoryインスタンス */
	@Autowired
	private ApplicationFileRepository applicationFileRepository;

	/** M_帳票Repositoryインスタンス */
	@Autowired
	private LedgerMasterRepository ledgerMasterRepository;

	/** O_帳票Repositoryインスタンス */
	@Autowired
	private LedgerRepository ledgerRepository;

	/** O_部署回答Repositoryインスタンス */
	@Autowired
	private DepartmentAnswerRepository departmentAnswerRepository;

	/** M_権限Repositoryインスタンス */
	@Autowired
	private AuthorityRepository authorityRepository;

	/** M_区分判定_権限Repositoryインスタンス */
	@Autowired
	private JudgementAuthorityRepository judgementAuthorityRepository;

	/** O_受付回答Repositoryインスタンス */
	@Autowired
	private AcceptingAnswerRepository acceptingAnswerRepository;

	/** 回答情報JDBCインスタンス */
	@Autowired
	private AnswerJdbc answerJdbc;
	/** 申請情報JDBCインスタンス */
	@Autowired
	private ApplicationJdbc applicationJdbc;
	/** 回答ファイルJDBCインスタンス */
	@Autowired
	private AnswerFileJdbc answerFileJdbc;
	/** 回答ファイル（引用）JDBCインスタンス */
	@Autowired
	private QuoteFileJdbc quoteFileJdbc;

	/** 回答履歴JDBCインスタンス */
	@Autowired
	private AnswerHistoryJdbc answerHistoryJdbc;

	/** 回答ファイル履歴JDBCインスタンス */
	@Autowired
	private AnswerFileHistoryJdbc answerFileHistoryJdbc;

	/** 帳票JDBCインスタンス */
	@Autowired
	private LedgerJdbc ledgerJdbc;

	/** 部署回答JDBCインスタンス */
	@Autowired
	private DepartmentAnswerJdbc departmentAnswerJdbc;

	/** O_申請版情報JDBCインスタンス */
	@Autowired
	private ApplicationVersionInformationJdbc applicationVersionInformationJdbc;
	
	/** O_申請ファイルJDBCインスタンス */
	@Autowired
	private ApplicationFileJdbc applicationFileJdbc;

	/** 帳票Serviceインスタンス */
	@Autowired
	private LedgerService ledgerService;

	/** 帳票Serviceインスタンス */
	@Autowired	
	private DevelopmentRegisterService developmentRegisterService;
	
	/** 回答ファイル用フォルダのtimestampフォーマット */
	@Value("${app.file.answer.foldername.format}")
	protected String answerFolderNameFormat;

	/** 回答更新通知（行政向け）を送信するかどうか (0:送信しない、1:送信する) */
	@Value("${app.mail.send.answer.update}")
	protected Integer mailSendAnswerUpdateFlg;

	/** 回答登録 csvログファイルヘッダー */
	@Value("${app.csv.log.header.answer.register}")
	private String[] answerRegisterLogHeader;

	/** 回答登録 csvログファイルパス */
	@Value("${app.csv.log.path.answer.register}")
	private String answerRegisterLogPath;

	/** 回答登録(行政確定登録内容登録（事前協議のみ）) csvログファイルヘッダー */
	@Value("${app.csv.log.header.answer.register.government.confirm}")
	private String[] answerRegisterConfirmLogHeader;

	/** 回答登録(行政確定登録内容登録（事前協議のみ）) csvログファイルパス */
	@Value("${app.csv.log.path.answer.register.government.confirm}")
	private String answerRegisterConfirmLogPath;
	
	/** 回答登録(部署回答行政確定登録内容登録（事前協議のみ）) csvログファイルヘッダー */
	@Value("${app.csv.log.header.answer.register.government.confirm.department}")
	private String[] departmentAnswerRegisterConfirmLogHeader;

	/** 回答登録(部署回答行政確定登録内容登録（事前協議のみ）)csvログファイルパス */
	@Value("${app.csv.log.path.answer.register.government.confirm.department}")
	private String departmentAnswerRegisterConfirmLogPath;
	
	/** 同意項目承認否認登録（事業者（事前協議のみ））csvログファイルヘッダー */
	@Value("${app.csv.log.header.answer.consent.input}")
	private String[] answerConsentInoutLogHeader;

	/** 同意項目承認否認登録（事業者（事前協議のみ） csvログファイルパス */
	@Value("${app.csv.log.path.answer.consent.input}")
	private String answerConsentInoutLogPath;

	/** 回答ファイル履歴更新タイプ定義JSON */
	@Value("${app.def.answerfilehistory.updatetype}")
	private String answerFileHistoryUpdateTypeJson;

	/** 申請登録時の概況診断レポートのファイルID */
	@Value("${app.application.report.fileid}")
	protected String applicationReportFileId;
	
	/** # 回答.期日X日前 */
	@Value("${app.answer.deadlineXDaysAgo}")
	private Integer appAnswerDeadlineXDaysAgo;
	
	/** # 回答.回答予定のバッファ日数 */
	@Value("${app.answer.bufferDays}")
	private Integer appAnswerBufferDays;
	
	/** # 回答.事業者へ合意登録通知日時の日数のY日前 */
	@Value("${app.answer.bussinesStatusDays}")
	private Integer appAnswerBussinesStatusDays;
	
	/** # 回答.事業者合意登録日時のZ日前 */
	@Value("${app.answer.bussinesRegisterDays}")
	private Integer appAnswerBussinesRegisterDays;

	/** 回答通知の通知種別定義（ログ出力用）JSON */
	@Value("${app.def.answer.notify.type}")
	private String answerNotifyTypeJson;
	
	/**  回答登録の操作種別定義（ログ出力用）JSON */
	@Value("${app.def.answer.register.updatetype}")
	private String answerRegisterUpdateTypeJson;

	/** 回答通知時の回答レポート接頭句 */
	@Value("${app.answer.report.filename.header}")
	protected String answerReportFileNameHeader;

	/** 回答通知時の回答レポート接尾句(日付フォーマット) */
	@Value("${app.answer.report.filename.footer}")
	protected String answerReportFileNameFooter;
	
	/** M_判定結果Repositoryインスタンス */
	@Autowired
	private JudgementResultRepository judgementResultRepository;
	
	/** M_申請種類Repositoryインスタンス */
	@Autowired
	private ApplicationTypeRepository applicationTypeRepository;

	/** M_申請段階Repositoryインスタンス */
	@Autowired
	private ApplicationStepRepository applicationStepRepository;

	/** O_申請版情報Repositoryインスタンス */
	@Autowired
	private ApplicationVersionInformationRepository applicationVersionInformationRepository;

	/** 申請段階:1(事前相談) */
	public static final String APPLICATION_STEP_ID_1_NAME = "事前相談";

	/** 申請段階:2(事前協議) */
	public static final String APPLICATION_STEP_ID_2_NAME = "事前協議";

	/** 申請段階:3(許可判定) */
	public static final String APPLICATION_STEP_ID_3_NAME = "許可判定";
	
	/**
	 * 回答登録のパラメータチェック
	 * 
	 * @param applyAnswerDetailForm 登録パラメータ
	 * @param departmentId          部署ID
	 * @return 判定結果
	 */
	public boolean validateRegistAnswersParam(ApplyAnswerDetailForm form, String departmentId) {
		LOGGER.debug("回答登録のパラメータチェック 開始");
		try {

			// 回答リスト
			List<AnswerForm> answerFormList = form.getAnswers();
			// 部署回答リスト
			List<DepartmentAnswerForm> departmentAnswerFormList = form.getDepartmentAnswers();
			// 申請段階
			Integer applicationStepId = form.getApplicationStepId();

			if (departmentId == null || EMPTY.equals(departmentId)) {
				LOGGER.warn("部署IDがnullまたは空");
				return false;
			}

			if (form.getApplicationId() == null) {
				// 登録データが空
				LOGGER.warn("登録パラメータの申請IDが空");
				return false;
			}

			if (form.getApplicationStepId() == null) {
				// 登録データが空
				LOGGER.warn("登録パラメータの申請段階IDが空");
				return false;
			}

			if (APPLICATION_STEP_ID_2.equals(applicationStepId)) {
				if (answerFormList.size() == 0 && departmentAnswerFormList.size() == 0) {
					// 登録データが空
					LOGGER.warn("登録パラメータの回答リストが空");
					return false;
				}
			} else {
				if (answerFormList.size() == 0) {
					// 登録データが空
					LOGGER.warn("登録パラメータの回答リストが空");
					return false;
				}
			}

			// 基準申請ID
			Integer baseApplicationId = form.getApplicationId();
			Integer baseApplicationStepId = form.getApplicationStepId();

			// ログインIDの回答権限があるか判定
			List<Authority> authorityList = authorityRepository.getAuthorityList(departmentId, applicationStepId);
			if (authorityList.size() != 1) {
				LOGGER.warn("部署の権限取得件数不正");
				return false;
			}
			for (AnswerForm answerForm : answerFormList) {
				// 行政で新規追加された回答は、DBにないので、下記のチェックをスキップ
				//if (APPLICATION_STEP_ID_2.equals(applicationStepId) && answerForm.getAnswerId() == 0
				// 行政確定登録時に登録不可になるので条件に回答IDは含まない
				if (APPLICATION_STEP_ID_2.equals(applicationStepId)
						&& ANSWER_DATA_TYPE_GOVERNMENT_ADD.equals(answerForm.getAnswerDataType())) {
					continue;
				}
				// 部署チェック
				LOGGER.trace("部署チェック 開始");
				// 回答権限があるかチェック
				if (AUTH_TYPE_NONE.equals(authorityList.get(0).getAnswerAuthorityFlag())) {
					// 回答アクセス権限がない
					LOGGER.warn("回答アクセス権限がない");
					return false;
				} else {
					// 自身のみ回答可能の場合、
					if (AUTH_TYPE_SELF.equals(authorityList.get(0).getAnswerAuthorityFlag())) {

						// 行政で追加される条項は判定項目IDがないため、O_回答の担当課で判断する
						if (answerForm.getJudgementInformation().getJudgementId() == null
								|| EMPTY.equals(answerForm.getJudgementInformation().getJudgementId())) {
							List<Answer> answerList = answerRepository.findByAnswerId(answerForm.getAnswerId());

							if (answerList.size() == 0) {
								// 回答データの件数不正
								LOGGER.warn("回答データ件数不正");
								return false;
							}
							if (!departmentId.equals(answerList.get(0).getDepartmentId())) {
								// 回答アクセス権限がない
								LOGGER.warn("回答アクセス権限がない");
								return false;
							}
						} else {
							List<CategoryJudgementAuthority> judgementAuthorityList = judgementAuthorityRepository
									.getOneByKey(answerForm.getJudgementInformation().getJudgementId(), departmentId);
							if (judgementAuthorityList.size() != 1) {
								// 回答アクセス権限がない
								LOGGER.warn("回答アクセス権限がない");
								return false;
							}
						}
					}
				}

				LOGGER.trace("部署チェック 終了");

				LOGGER.trace("回答データチェック 開始");
				List<Answer> answerList = answerRepository.findByAnswerId(answerForm.getAnswerId());
				if (answerList.size() != 1) {
					// 回答データの件数不正
					LOGGER.warn("回答データ件数不正");
					return false;
				} else {
					Answer answer = answerList.get(0);
					if (baseApplicationId == null) {
						baseApplicationId = answer.getApplicationId();
					} else if (!baseApplicationId.equals(answer.getApplicationId())) {
						// パラメータの回答IDが持つ申請IDに異なるものがある
						LOGGER.warn("回答データリストの申請IDが全て一致していない");
						return false;
					}

					if (baseApplicationStepId == null) {
						baseApplicationStepId = answer.getApplicationStepId();
					} else if (!baseApplicationStepId.equals(answer.getApplicationStepId())) {
						// パラメータの回答IDが持つ申請段階IDに異なるものがある
						LOGGER.warn("回答データリストの申請段階IDが全て一致していない");
						return false;
					}
				}
				LOGGER.trace("回答データチェック 終了");
			}

			for (DepartmentAnswerForm departmentAnswerForm : departmentAnswerFormList) {

				String departmentIdOfAnswer = departmentAnswerForm.getDepartment().getDepartmentId();
				if (departmentIdOfAnswer == null || EMPTY.equals(departmentIdOfAnswer)) {
					LOGGER.debug("部署回答の部署IDがnullまたは空");
					return false;
				}

				// 部署チェック
				LOGGER.trace("部署チェック 開始");
				if (AUTH_TYPE_NONE.equals(authorityList.get(0).getAnswerAuthorityFlag())) {
					// 回答アクセス権限がない
					LOGGER.warn("回答アクセス権限がない");
					return false;
				} else {
					// 自身のみ回答可能の場合、
					if (AUTH_TYPE_SELF.equals(authorityList.get(0).getAnswerAuthorityFlag())) {
						if (!departmentIdOfAnswer.equals(departmentId)) {
							// 回答アクセス権限がない
							LOGGER.warn("回答アクセス権限がない");
							return false;
						}
					}
				}
				LOGGER.trace("部署チェック 終了");

				LOGGER.trace("部署回答データチェック 開始");
				List<DepartmentAnswer> departmentAnswerList = departmentAnswerRepository
						.findByDepartmentAnswerId(departmentAnswerForm.getDepartmentAnswerId());
				if (departmentAnswerList.size() != 1) {
					// 回答データの件数不正
					LOGGER.warn("回答データ件数不正");
					return false;
				} else {
					DepartmentAnswer departmentAnswer = departmentAnswerList.get(0);
					if (baseApplicationId == null) {
						baseApplicationId = departmentAnswer.getApplicationId();
					} else if (!baseApplicationId.equals(departmentAnswer.getApplicationId())) {
						// パラメータの回答IDが持つ申請IDに異なるものがある
						LOGGER.warn("回答データリストの申請IDが全て一致していない");
						return false;
					}
				}
				LOGGER.trace("回答データチェック 終了");
			}
			return true;
		} finally {
			LOGGER.debug("回答登録のパラメータチェック 終了");
		}
	}

	/**
	 * 同意項目承認否認登録のパラメータチェック
	 * 
	 * @param answerFormList 登録パラメータ
	 * @return 判定結果
	 */
	public boolean validateRegistConsentParam(List<AnswerForm> answerFormList) {
		LOGGER.debug("同意項目承認否認登録のパラメータチェック 開始");
		try {
			if (answerFormList.size() == 0) {
				// 登録データが空
				LOGGER.warn("登録パラメータが空");
				return false;
			}

			// 基準申請ID
			Integer baseApplicationId = null;

			for (AnswerForm answerForm : answerFormList) {
				LOGGER.trace("回答データチェック 開始");
				List<Answer> answerList = answerRepository.findByAnswerId(answerForm.getAnswerId());
				if (answerList.size() != 1) {
					// 回答データの件数不正
					LOGGER.warn("回答データ件数不正");
					return false;
				} else {
					Answer answer = answerList.get(0);
					if (baseApplicationId == null) {
						baseApplicationId = answer.getApplicationId();
					} else if (!baseApplicationId.equals(answer.getApplicationId())) {
						// パラメータの回答IDが持つ申請IDに異なるものがある
						LOGGER.warn("回答データリストの申請IDが全て一致していない");
						return false;
					}
				}
				LOGGER.trace("回答データチェック 終了");
			}
			return true;
		} finally {
			LOGGER.debug("同意項目承認否認登録のパラメータチェック 終了");
		}
	}

	/**
	 * 回答登録(行政のみ)
	 * 
	 * @param applyAnswerDetailForm 登録パラメータ
	 * @param loginId               ログインID
	 * @param departmentName        部署名
	 * @param userId                ユーザーID
	 * @param accessId              アクセスID
	 */
	public void registAnswers(ApplyAnswerDetailForm form, String loginId, String departmentName, String userId,
			String accessId) {
		LOGGER.debug("回答登録 開始");
		try {
			// 回答リスト
			List<AnswerForm> answerFormList = form.getAnswers();
			// 部署回答リスト
			List<DepartmentAnswerForm> departmentAnswerFormList = form.getDepartmentAnswers();
			// 申請段階
			Integer applicationStepId = form.getApplicationStepId();

			LOGGER.trace("回答情報更新 開始");
			// 「登録」と言いつつ、Updateのみ
			Integer baseApplicationId = form.getApplicationId();
			
			LOGGER.trace("O_申請版情報取得 開始");
			List<ApplicationVersionInformation> applicationVersionInformationList = applicationVersionInformationRepository
					.findByApplicationSteId(baseApplicationId, applicationStepId);
			if (applicationVersionInformationList.size() == 0) {
				LOGGER.warn("O_申請版情報が存在しません。");
				throw new RuntimeException();
			}
			Integer acceptVersionInformation = applicationVersionInformationList.get(0).getAcceptVersionInformation();

			// 更新ありの回答リスト（事前協議のみ）
			List<AnswerForm> answerUpdateList = new ArrayList<AnswerForm>();

			for (AnswerForm answerForm : answerFormList) {

				// ログの種別
				String logType = EMPTY;
				// 操作種別
				String updateType = EMPTY;
				// 回答版情報
				String answerVersionInformation = EMPTY;
		
				// O_回答更新要否フラグ
				boolean isUpdate = false;

				// 行政で新規追加レコードであるかフラグ
				boolean governmentAdd = false;
				if(APPLICATION_STEP_ID_2.equals(applicationStepId) && answerForm.getAnswerId() == 0
						&& ANSWER_DATA_TYPE_GOVERNMENT_ADD.equals(answerForm.getAnswerDataType())) {
					governmentAdd = true;
				}
				
				/// DBに保持している回答取得
				List<Answer> answerList = answerRepository.findByAnswerId(answerForm.getAnswerId());
				// 行政で新規追加したレコードがDBに存在しない
				if (answerList.size() < 1 && !governmentAdd) {
					LOGGER.warn("O_回答が存在しません。回答ID：" + answerForm.getAnswerId());
					throw new RuntimeException();
				}

				// 回答更新を行う

				// 事前相談
				if (APPLICATION_STEP_ID_1.equals(applicationStepId)) {
					
					// 更新前回答内容
					Answer answerOld = answerList.get(0);
					
					// 回答内容
					String answerContent = answerForm.getAnswerContent() == null ? EMPTY : answerForm.getAnswerContent();
					String answerContentOld = answerOld.getAnswerContent() == null ? EMPTY : answerOld.getAnswerContent();

					// 回答内容
					if (!answerContent.equals(answerContentOld)) {
						isUpdate = true;
					}

					// 再申請要否
					if (!compareToBoolean(answerForm.getReApplicationFlag(), answerOld.getReApplicationFlag())) {
						isUpdate = true;
					}

					// 事前協議要否
					if (!compareToBoolean(answerForm.getDiscussionFlag(), answerOld.getDiscussionFlag())) {
						isUpdate = true;
					}

					// 回答内容、再申請要否、事前協議要否のいずれか変更したら、O_回答が「完了」に更新
					if (isUpdate) {
						
						logType = "1";// 回答登録（回答内容変更)のログ
						updateType = "1";// 回答内容変更
						// 回答の版情報
						answerVersionInformation = answerOld.getVersionInformation() == null ? EMPTY
								: answerOld.getVersionInformation().toString();

						boolean completeFlag = true;
						// 回答内容、再申請要否、事前協議要否のいずれか未入力であれば、回答フラグが未完了になる
						if (answerForm.getReApplicationFlag() == null || answerForm.getDiscussionFlag() == null || EMPTY.equals(answerContent)) {
							completeFlag = false;
						}

						if (answerJdbc.updateForAnswerContent(answerForm, applicationStepId, completeFlag) != 1) {
							LOGGER.warn("回答情報の更新件数不正");
							throw new RuntimeException("回答情報の更新に失敗");
						}
					}
				}
				// 事前協議
				if (APPLICATION_STEP_ID_2.equals(applicationStepId)) {
					// ①：削除未通知フラグが「true」の場合、回答の論理削除を行う
					if (answerForm.getDeleteUnnotifiedFlag() != null && answerForm.getDeleteUnnotifiedFlag()) {

						isUpdate = true;
						if (answerJdbc.updateDeleteUnnotifiedFlag(answerForm) != 1) {
							LOGGER.warn("回答情報の更新件数不正");
							throw new RuntimeException("回答情報の更新に失敗");
						}

						logType = "1";// 回答登録（回答内容変更)のログ
						updateType = "3";// 行政回答削除
						// 回答の版情報
						answerVersionInformation = answerList.get(0).getVersionInformation() == null ? EMPTY
								: answerList.get(0).getVersionInformation().toString();

					} else {
						// ②：行政追加の場合、回答が新規登録する
						if (answerForm.getAnswerId() == 0
								&& ANSWER_DATA_TYPE_GOVERNMENT_ADD.equals(answerForm.getAnswerDataType())) {
							Answer answer = new Answer();
							answer.setApplicationId(baseApplicationId); // 申請ID
							answer.setApplicationStepId(applicationStepId);// 申請段階ID
							answer.setJudgementId(EMPTY); // 判定項目ID
							answer.setJudgementResultIndex(0);// 判定結果のインデックス
							answer.setDepartmentAnswerId(answerForm.getDepartmentAnswerId());// 部署回答ID（事前協議以外は「0」）
							String departmentId = answerForm.getJudgementInformation().getDepartments().get(0)
									.getDepartmentId();
							answer.setDepartmentId(departmentId);// 部署ID（事前協議以外は「-1」）
							answer.setJudgementResult(EMPTY);// 判定結果
							answer.setAnswerDataType(answerForm.getAnswerDataType()); // データ種類
							answer.setRegisterStatus(STATE_APPLIED); // 登録ステータス
							answer.setDiscussionItem(answerForm.getDiscussionItem());// 協議対象（事前協議のみ）
							answer.setAnswerContent(answerForm.getAnswerContent());// 回答内容
							answer.setNotifiedText(null);// 通知テキスト
							if (answerForm.getAnswerContent() == null || EMPTY.equals(answerForm.getAnswerContent())) {
								answer.setCompleteFlag(false);// 完了フラグ
								answer.setAnswerUpdateFlag(false);// 回答変更フラグ
								answer.setAnswerStatus(ANSWER_STATUS_NOTANSWERED);// 回答ステータス
							} else {
								answer.setCompleteFlag(true);// 完了フラグ
								answer.setAnswerUpdateFlag(true);// 回答変更フラグ
								answer.setAnswerStatus(ANSWER_STATUS_APPROVING);// 回答ステータス
							}
							answer.setNotifiedFlag(false);// 通知フラグ
							answer.setReApplicationFlag(null); // 再申請フラグ
							answer.setBusinessReApplicationFlag(null); // 事業者再申請フラグ
							answer.setDiscussionFlag(null);// 事前協議フラグ
							answer.setVersionInformation(acceptVersionInformation);
							// 行政追加された回答には、回答期限日時を設定しない
							answer.setDeadlineDatetime(null);
							Integer answerId = answerJdbc.insert(answer);
							answerForm.setAnswerId(answerId);

							isUpdate = true;
							logType = "1";// 回答登録（回答内容変更)のログ
							updateType = "2";// 行政回答追加登録
							// 回答の版情報
							answerVersionInformation = acceptVersionInformation.toString();

						} else {

							// 更新前回答内容
							Answer answerOld = answerList.get(0);

							// ■追加要件②：事業者へ通知済みになった後、回答内容が上書き更新可能とする。
							// 行政回答登録
							// ↓A（回答内容入力可能）
							// 統括部署管理者へ回答許可通知
							// ↓B（回答内容入力不可）
							// 事業者へ回答通知
							// ↓C（回答内容入力可能
							// 事業者が回答登録
							// ↓-（回答内容入力不可
							// ↓D（行政確定登録入力可能）
							// 行政確定登録
							// ↓-（回答内容入力不可）
							// ↓E（行政確定登録入力可能）
							// 統括部署管理者へ行政確定許可通知
							// ↓-（回答内容入力不可）
							// ↓F（行政確定登録入力不可）

							// 回答内容更新要否フラグ
							boolean isUpdateAnswerContent = false;
							// 行政確定登録内容更新要否フラグ
							boolean isUpdateGovernmentConfirm = false;

							// 事業者回答登録済みになるか
							if (answerForm.getBusinessPassStatus() == null
									|| EMPTY.equals(answerForm.getBusinessPassStatus())) {

								// 事業者未回答の場合、事業者へ通知済みになるか判断
								if (answerForm.getNotifiedFlag() == null || !answerForm.getNotifiedFlag()) {

									// 事業者へ一度も通知しない場合、統括部署管理者へ通知済みか判断
									if (answerForm.getAnswerPermissionFlag() == null
											|| !answerForm.getAnswerPermissionFlag()) {
										// 統括部署管理者へ回答許可通知しない場合、上記のパターンAとなるため、回答更新要
										isUpdateAnswerContent = true;
									} else {
										// 統括部署管理者へ回答許可通知済み、かつ、事業者へ未通知の場合、上記のパターンBとなるため、回答更新不要
										isUpdateAnswerContent = false;
									}

								} else {
									// 事業者未回答 かつ、事業者へ通知済みの場合、上記のパターンCとなるため、回答内容更新要
									isUpdateAnswerContent = true;
								}
							} else {
								// 事業者回答登録済みの場合、回答内容更新要否が常に不要になる
								// 行政確定登録内容更新要否が、統括部署管理者へ行政確定許可通知を行うか判断
								if (answerForm.getGovernmentConfirmPermissionFlag() == null
										|| !answerForm.getGovernmentConfirmPermissionFlag()) {

									// 事業者回答登録済み、かつ 統括部署管理者へ行政確定許可通知未実施の場合、上記のパターンD、Eとなるため、行政確定登録内容更新要
									isUpdateGovernmentConfirm = true;
								} else {
									// 統括部署管理者へ行政確定許可通知済みの場合、上記のパターンFとなるため、行政確定登録内容更新不要
									isUpdateGovernmentConfirm = false;
								}
							}

							// ③：回答内容更新を行う
							if (isUpdateAnswerContent) {

								// 回答内容
								String answerContent = answerForm.getAnswerContent() == null ? EMPTY : answerForm.getAnswerContent();
								String answerContentOld = answerOld.getAnswerContent() == null ? EMPTY : answerOld.getAnswerContent();

								if (!answerContent.equals(answerContentOld)) {
									isUpdate = true;
								}

								// 32協議
								String discussionItem = answerForm.getDiscussionItem() == null ? EMPTY : answerForm.getDiscussionItem();
								String discussionItemOld = answerOld.getDiscussionItem() == null ? EMPTY : answerOld.getDiscussionItem();
								if (!discussionItem.equals(discussionItemOld)) {
									isUpdate = true;
								}

								// 回答内容、協議対象のいずれか変更したら、O_回答が更新
								if (isUpdate) {
									boolean completeFlag = true;
									// 回答内容が空欄に更新すれば、完了フラグが未完了に更新
									if (EMPTY.equals(answerContent)) {
										completeFlag = false;
									}
									if (answerJdbc.updateForAnswerContent(answerForm, applicationStepId, completeFlag) != 1) {
										LOGGER.warn("回答情報の更新件数不正");
										throw new RuntimeException("回答情報の更新に失敗");
									}

									logType = "1";// 回答登録（回答内容変更)のログ
									updateType = "1";// 事前協議回答登録
									// 回答の版情報
									answerVersionInformation = answerOld.getVersionInformation() == null ? EMPTY
											: answerOld.getVersionInformation().toString();
								}
							}

							// ④：行政確認内容更新を行う
							if (isUpdateGovernmentConfirm) {

								// 行政確定登録：ステータス
								String governmentConfirmStatus = answerForm.getGovernmentConfirmStatus() == null 
										? EMPTY
										: answerForm.getGovernmentConfirmStatus();
								String governmentConfirmStatusOld = answerOld.getGovernmentConfirmStatus() == null
										? EMPTY
										: answerOld.getGovernmentConfirmStatus();

								if (!governmentConfirmStatus.equals(governmentConfirmStatusOld)) {
									isUpdate = true;
								}

								// 行政確定登録：日付
								String governmentConfirmDatetime = answerForm.getGovernmentConfirmDatetime() == null
										? EMPTY
										: answerForm.getGovernmentConfirmDatetime();
								governmentConfirmDatetime = governmentConfirmDatetime.replace("/", "-");
								String governmentConfirmDatetimeOld = EMPTY;
								if (answerOld.getGovernmentConfirmDatetime() != null) {
									DateTimeFormatter dateformatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
									String dateformated = dateformatter
											.format(answerOld.getGovernmentConfirmDatetime());
									governmentConfirmDatetimeOld = dateformated.replace("/", "-");

								}
								if (!governmentConfirmDatetime.equals(governmentConfirmDatetimeOld)) {
									isUpdate = true;
								}

								// 行政確定登録：コメント
								String governmentConfirmComment = answerForm.getGovernmentConfirmComment() == null
										? EMPTY
										: answerForm.getGovernmentConfirmComment();
								String governmentConfirmCommentOld = answerOld.getGovernmentConfirmComment() == null
										? EMPTY
										: answerOld.getGovernmentConfirmComment();
								if (!governmentConfirmComment.equals(governmentConfirmCommentOld)) {
									isUpdate = true;
								}

								// 行政確定登録：ステータス、日付、コメントのいずれか変更したら、O_回答が更新
								if (isUpdate) {

									// 行政確認ステータスが却下の場合、→ 却下が選択できなくなったため、実は到達できない
									if (GOVERNMENT_CONFIRM_STATUS_2_REJECT
											.equals(answerForm.getGovernmentConfirmStatus())) {
										answerForm.setAnswerStatus(ANSWER_STATUS_REJECTED);
									}

									// 行政確認ステータスが同意、又は、取下の場合、
									if (GOVERNMENT_CONFIRM_STATUS_0_AGREE
											.equals(answerForm.getGovernmentConfirmStatus())
											|| GOVERNMENT_CONFIRM_STATUS_1_WITHDRAW
													.equals(answerForm.getGovernmentConfirmStatus())) {
										answerForm.setAnswerStatus(ANSWER_STATUS_AGREED);
									}

									LocalDateTime dateTime = null;
									if (answerForm.getGovernmentConfirmDatetime() != null
											&& !EMPTY.equals(answerForm.getGovernmentConfirmDatetime())) {

										String str = answerForm.getGovernmentConfirmDatetime() + " 00:00";
										str = str.replace("-", "/");
										DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
										dateTime = LocalDateTime.parse(str, formatter);
									}

									if (answerJdbc.updateForGovernmentContent(answerForm, dateTime) != 1) {
										LOGGER.warn("回答情報の更新件数不正");
										throw new RuntimeException("回答情報の更新に失敗");
									}

									logType = "2";// 行政確定登録のログ
									updateType = EMPTY;// 事前協議回答登録
									// 回答の版情報
									answerVersionInformation = answerOld.getVersionInformation() == null ? EMPTY
											: answerOld.getVersionInformation().toString();
								}
							}
						}

					}

					if (isUpdate) {
						// 変更ありの回答リストに追加
						answerUpdateList.add(answerForm);
					}
				}

				// 許可判定
				if (APPLICATION_STEP_ID_3.equals(applicationStepId)) {
					
					// 更新前回答内容
					Answer answerOld = answerList.get(0);
					
					// 回答内容
					String answerContent = answerForm.getAnswerContent() == null ? EMPTY : answerForm.getAnswerContent();
					String answerContentOld = answerOld.getAnswerContent() == null ? EMPTY : answerOld.getAnswerContent();

					// 回答内容
					if (!answerContent.equals(answerContentOld)) {
						isUpdate = true;
					}

					// 再申請要否
					if (!compareToBoolean(answerForm.getReApplicationFlag(), answerOld.getReApplicationFlag())) {
						isUpdate = true;
					}

					// 許可判定結果
					String permissionJudgementResult = answerForm.getPermissionJudgementResult() == null ? EMPTY
							: answerForm.getPermissionJudgementResult();
					String permissionJudgementResultOld = answerOld.getPermissionJudgementResult() == null ? EMPTY
							: answerOld.getPermissionJudgementResult();

					if (!permissionJudgementResult.equals(permissionJudgementResultOld)) {
						isUpdate = true;
					}

					// 回答内容、再申請要否、許可判定結果のいずれか変更したら、O_回答が「完了」に更新
					if (isUpdate) {

						boolean completeFlag = true;
						// 再申請要否、許可判定結果のいずれか未入力であれば、回答フラグが未完了になる
						if (answerForm.getReApplicationFlag() == null) {
							completeFlag = false;
						}
						if (answerForm.getPermissionJudgementResult() == null
								|| EMPTY.equals(answerForm.getPermissionJudgementResult())) {
							completeFlag = false;
							answerForm.setPermissionJudgementResult(null);
						}

						// 回答内容がクリアされた場合、回答フラグが未完了になる
						if(EMPTY.equals(answerContent)) {
							completeFlag = false;
						}

						if (answerJdbc.updateForAnswerContent(answerForm, applicationStepId, completeFlag) != 1) {
							LOGGER.warn("回答情報の更新件数不正");
							throw new RuntimeException("回答情報の更新に失敗");
						}
						
						logType = "1";// 回答登録（回答内容変更)のログ
						updateType = "1";// 許可判定回答登録
						// 回答の版情報
						answerVersionInformation = answerOld.getVersionInformation() == null ? EMPTY
								: answerOld.getVersionInformation().toString();
					}
				}

				// O_回答が更新したら、回答履歴を生成して、回答登録の操作ログを出力
				if (isUpdate) {
					// 回答履歴登録
					if (answerHistoryJdbc.insert(answerForm.getAnswerId(), userId, false) != 1) {
						LOGGER.warn("回答履歴の更新件数不正");
						throw new RuntimeException("回答履歴の更新に失敗");
					}

					// 回答登録ログ出力
					outputAnswerRegistLogToCsv(answerForm, baseApplicationId, applicationStepId, accessId, loginId,
							departmentName, logType, updateType, answerVersionInformation);
				}

			}
			LOGGER.trace("回答情報更新 終了");

			// 事前協議の場合、部署回答の更新を行う
			if (APPLICATION_STEP_ID_2.equals(applicationStepId)) {
				for (DepartmentAnswerForm departmentAnswerForm : departmentAnswerFormList) {

					// O_部署回答更新要否フラグ
					boolean isUpdate = false;
					
					// 事前協議フロー修正を対応する伴い、統括部署管理者に通知したら、更新できないようになる
					if (departmentAnswerForm.getGovernmentConfirmPermissionFlag() == null
							|| !departmentAnswerForm.getGovernmentConfirmPermissionFlag()) {

						List<DepartmentAnswer> departmentAnswerList = departmentAnswerRepository
								.findByDepartmentAnswerId(departmentAnswerForm.getDepartmentAnswerId());
						if (departmentAnswerList.size() < 1) {
							LOGGER.warn("O_部署回答が存在しません。部署回答ID：" + departmentAnswerForm.getDepartmentAnswerId());
							throw new RuntimeException();
						}

						DepartmentAnswer departmentAnswerOld = departmentAnswerList.get(0);

						// 行政確定登録：ステータス
						String governmentConfirmStatus = departmentAnswerForm.getGovernmentConfirmStatus() == null
								? EMPTY
								: departmentAnswerForm.getGovernmentConfirmStatus();
						String governmentConfirmStatusOld = departmentAnswerOld.getGovernmentConfirmStatus() == null
								? EMPTY
								: departmentAnswerOld.getGovernmentConfirmStatus();

						if (!governmentConfirmStatus.equals(governmentConfirmStatusOld)) {
							isUpdate = true;
						}

						// 行政確定登録：日付
						String governmentConfirmDatetime = departmentAnswerForm.getGovernmentConfirmDatetime() == null
								? EMPTY
								: departmentAnswerForm.getGovernmentConfirmDatetime();
						governmentConfirmDatetime = governmentConfirmDatetime.replace("/", "-");
						String governmentConfirmDatetimeOld = EMPTY;
						if (departmentAnswerOld.getGovernmentConfirmDatetime() != null) {
							DateTimeFormatter dateformatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
							String dateformated = dateformatter
									.format(departmentAnswerOld.getGovernmentConfirmDatetime());
							governmentConfirmDatetimeOld = dateformated.replace("/", "-");

						}
						if (!governmentConfirmDatetime.equals(governmentConfirmDatetimeOld)) {
							isUpdate = true;
						}

						// 行政確定登録：コメント
						String governmentConfirmComment = departmentAnswerForm.getGovernmentConfirmComment() == null
								? EMPTY
								: departmentAnswerForm.getGovernmentConfirmComment();
						String governmentConfirmCommentOld = departmentAnswerOld.getGovernmentConfirmComment() == null
								? EMPTY
								: departmentAnswerOld.getGovernmentConfirmComment();
						if (!governmentConfirmComment.equals(governmentConfirmCommentOld)) {
							isUpdate = true;
						}

						// 行政確定登録：ステータス、日付、コメントのいずれか変更したら、O_部署回答が更新
						if (isUpdate) {
							// 行政確定登録：ステータス
							// 空の場合、DBにnullで登録する
							if (departmentAnswerForm.getGovernmentConfirmStatus() != null
									&& EMPTY.equals(departmentAnswerForm.getGovernmentConfirmStatus().trim())) {
								departmentAnswerForm.setGovernmentConfirmStatus(null);
							}

							LocalDateTime dateTime = null;
							if (departmentAnswerForm.getGovernmentConfirmDatetime() != null
									&& !EMPTY.equals(departmentAnswerForm.getGovernmentConfirmDatetime())) {

								String str = departmentAnswerForm.getGovernmentConfirmDatetime() + " 00:00";
								str = str.replace("-", "/");
								DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
								dateTime = LocalDateTime.parse(str, formatter);
							}

							// 行政確定登録：ステータス又は、日時が空の場合、完了フラグが未完に更新
							if (departmentAnswerForm.getGovernmentConfirmStatus() == null || dateTime == null) {
								departmentAnswerForm.setCompleteFlag(false);
							} else {
								departmentAnswerForm.setCompleteFlag(true);
							}

							if (departmentAnswerJdbc.update(departmentAnswerForm, dateTime) != 1) {
								LOGGER.warn("部署回答情報の更新件数不正");
								throw new RuntimeException("回答情報の更新に失敗");
							}

							// 部署回答登録ログ出力
							outputDepartmentAnswerLogToCsv(departmentAnswerForm, baseApplicationId, applicationStepId,
									accessId, loginId, departmentName);

						}

					}
				}
			}

			if (baseApplicationId == null) {
				LOGGER.error("申請IDの取得に失敗");
				throw new RuntimeException("申請IDの取得に失敗");
			}

			// O_回答ファイル
			// 回答ファイル登録は別APIで実施

			LOGGER.trace("申請ステータス更新 開始");
			// 回答更新の際に、申請のステータスが「通知済み（要再申請）」でなければO_申請のステータスも更新する
			List<Application> applicationList = applicationRepository.getApplicationList(baseApplicationId);
			if (applicationList.size() == 0) {
				LOGGER.warn("申請情報の取得件数不正");
				throw new RuntimeException("申請情報の取得に失敗");
			}

			// 全ての条項が回答完了かどうか
			boolean isAnswerCompleted = true;

			List<Answer> unansweredAnswer = answerRepository.findUnansweredListByApplicationStepId(baseApplicationId,
					applicationStepId);
			if (unansweredAnswer.size() > 0) {
				isAnswerCompleted = false;
			}

			// 事前相談
			if (APPLICATION_STEP_ID_1.equals(applicationStepId)) {
				String status = STATUS_CONSULTATION_ANSWERED_PREPARING;
				if (isAnswerCompleted) {
					status = STATUS_CONSULTATION_ANSWERED_REVIEWING;
				}

				if (!STATUS_CONSULTATION_REAPP.equals(applicationList.get(0).getStatus())) {
					if (applicationJdbc.updateApplicationStatus(baseApplicationId, status) != 1) {
						LOGGER.warn("申請情報の更新件数不正");
						throw new RuntimeException("申請情報の更新に失敗");
					}
					LOGGER.trace("申請ステータス更新 終了");
				} else {
					LOGGER.trace("現状の申請ステータスが事前相談：未完（要再申請）のためステータス更新は実施しない");
				}
				
			}

			// 事前協議
			if (APPLICATION_STEP_ID_2.equals(applicationStepId)) {

				String status = STATUS_DISCUSSIONS_ANSWERED_PREPARING;
				if (isAnswerCompleted) {
					status = STATUS_DISCUSSIONS_ANSWERED_REVIEWING;
				}

				if (!STATUS_DISCUSSIONS_REAPP.equals(applicationList.get(0).getStatus())
						&& !STATUS_DISCUSSIONS_IN_PROGRESS.equals(applicationList.get(0).getStatus())) {
					if (applicationJdbc.updateApplicationStatus(baseApplicationId, status) != 1) {
						LOGGER.warn("申請情報の更新件数不正");
						throw new RuntimeException("申請情報の更新に失敗");
					}
					LOGGER.trace("申請ステータス更新 終了");
				} else {
					if (STATUS_DISCUSSIONS_REAPP.equals(applicationList.get(0).getStatus())) {
						LOGGER.trace("現状の申請ステータスが事前協議：未完（要再申請）のためステータス更新は実施しない");
					}
					if (STATUS_DISCUSSIONS_IN_PROGRESS.equals(applicationList.get(0).getStatus())) {
						LOGGER.trace("現状の申請ステータスが事前協議：未完（協議進行中）のためステータス更新は実施しない");
					}
				}
			}

			// 許可判定
			if (APPLICATION_STEP_ID_3.equals(applicationStepId)) {
				String status = STATUS_PERMISSION_ANSWERED_PREPARING;
				if (isAnswerCompleted) {
					status = STATUS_PERMISSION_ANSWERED_REVIEWING;
				}

				if (!STATUS_PERMISSION_REAPP.equals(applicationList.get(0).getStatus())) {
					if (applicationJdbc.updateApplicationStatus(baseApplicationId, status) != 1) {
						LOGGER.warn("申請情報の更新件数不正");
						throw new RuntimeException("申請情報の更新に失敗");
					}
					LOGGER.trace("申請ステータス更新 終了");
				} else {
					LOGGER.trace("現状の申請ステータスが許可判定：未完（要再申請）のためステータス更新は実施しない");
				}
			}
			
			// 事前協議-部署ごと回答が
			if (APPLICATION_STEP_ID_2.equals(applicationStepId)) {
				ApplicationDao dao = new ApplicationDao(emf);

				// 判定済みの部署回答IDリスト
				List<Integer> departmentAnswerIdList = new ArrayList<Integer>();

				// 管理通知必要の部署IDリスト
				List<String> departmentIdList = new ArrayList<String>();

				// 更新ありの回答リスト
				for (AnswerForm answerForm : answerUpdateList) {

					Integer departmentAnswerId = answerForm.getDepartmentAnswerId();
					if (departmentAnswerIdList.contains(departmentAnswerId)) {
						continue;
					}

					departmentAnswerIdList.add(departmentAnswerId);

					// 回答完了フラグ
					boolean completed = true;

					// 部署ごとの回答一覧を取得(DBに登録済みの)
					List<Answer> answerList = dao.getAnswerList(baseApplicationId, true, applicationStepId,
							departmentAnswerId);
					
					// 判定済みの回答IDリスト
					List<Integer> answerIdList = new ArrayList<Integer>();
					for (Answer entity : answerList) {

						String governmentConfirmStatus = entity.getGovernmentConfirmStatus();

						for (AnswerForm ans : answerUpdateList) {
							if (entity.getAnswerId().equals(ans.getAnswerId())) {
								governmentConfirmStatus = ans.getGovernmentConfirmStatus();
							}
						}

						// 行政確定登録ステータスがないの場合、未回答とする
						if (governmentConfirmStatus == null || EMPTY.equals(governmentConfirmStatus.trim())) {
							completed = false;
						}

						answerIdList.add(entity.getAnswerId());
					}

					// 画面で新規追加する条項があるか判断
					for (AnswerForm ans : answerUpdateList) {

						if (departmentAnswerId.equals(ans.getDepartmentAnswerId())) {

							if (answerIdList.contains(ans.getAnswerId())) {
								continue;
							}

							// 行政確定登録ステータスがないの場合、未回答とする
							if (ans.getGovernmentConfirmStatus() == null
									|| EMPTY.equals(ans.getGovernmentConfirmStatus().trim())) {
								completed = false;
							}
						}
					}

					// 部署回答ごとの全ての回答が行政確定登録完了の場合、管理者にメール通知を行う
					if (completed && answerList.size() > 0) {
						if(!departmentIdList.contains(answerList.get(0).getDepartmentId())) {
							departmentIdList.add(answerList.get(0).getDepartmentId());
						}
					}
				}

				// メール通知：回答担当課の管理者に事前協議行政確定登録完了通知を送信する
				if (departmentIdList.size() > 0) {
					sendAnswerCompletedMainToGovernmentAdmin(baseApplicationId, applicationStepId, departmentIdList);
				}

			}
		} finally {
			LOGGER.debug("回答登録 終了");
		}
	}

	/**
	 * 同意項目承認否認登録API(事業者のみ)
	 * 
	 * @param answerFormList 登録パラメータ
	 * @param accessId       アクセスID
	 * 
	 */
	public void registConsent(List<AnswerForm> answerFormList, String accessId) {
		LOGGER.debug(" 同意項目承認否認登録 開始");
		try {
			LOGGER.trace("回答情報更新 開始");
			// 「登録」と言いつつ、Updateのみ
			Integer baseApplicationId = null;
			Integer baseApplicationStepId = null;
			Integer cntAgree = 0;
			for (AnswerForm answerForm : answerFormList) {

				// 事業者合否ステータス=""：未選択は更新対象外
				if (BUSINESS_PASS_STATUS_NOT_SELECTED.equals(answerForm.getBusinessPassStatus())) {
					continue;
				}

				// 事業者合否ステータス="1"：合意の件数カウント
				if (BUSINESS_PASS_STATUS_1_AGREE.equals(answerForm.getBusinessPassStatus())) {
					cntAgree++;
					answerForm.setAnswerStatus(ANSWER_STATUS_APPROVED);
				}

				// 事業者回答登録日時
				LocalDateTime dateTime = null;
				if (answerForm.getBusinessAnswerDatetime() != null
						&& !EMPTY.equals(answerForm.getBusinessAnswerDatetime())) {

					LocalDateTime now = LocalDateTime.now();
					String hour = String.format("%02d", now.getHour());
					String minute = String.format("%02d", now.getMinute());
					String second = String.format("%02d", now.getSecond());

					String str = answerForm.getBusinessAnswerDatetime() + " " + hour + ":" + minute + ":" + second;
					str = str.replace("-", "/");
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
					dateTime = LocalDateTime.parse(str, formatter);
				}

				// 回答情報更新
				if (answerJdbc.updateConsent(answerForm, dateTime) != 1) {
					LOGGER.warn("回答情報の更新件数不正");
					throw new RuntimeException("回答情報の更新に失敗");
				}

				List<Answer> answerList = answerRepository.findByAnswerId(answerForm.getAnswerId());
				if (answerList.size() != 1) {
					LOGGER.error("回答情報の取得に失敗");
					throw new RuntimeException("回答情報の取得に失敗");
				}
				Answer answer = answerList.get(0);
				if (baseApplicationId == null) {
					baseApplicationId = answer.getApplicationId();
					baseApplicationStepId = answer.getApplicationStepId();
				}
				// 回答履歴登録
				// ※事業者が回答履歴を登録する際はユーザを"-1"で登録する
				if (answerHistoryJdbc.insert(answerForm.getAnswerId(), "-1", true) != 1) {
					LOGGER.warn("回答履歴の更新件数不正");
					throw new RuntimeException("回答履歴の更新に失敗");
				}

				// 同意項目承認否認登録の操作ログ出力
				outputAnswerRegistLogToCsv(answerForm, baseApplicationId, baseApplicationStepId, accessId, EMPTY, EMPTY,
						"3", EMPTY, answer.getVersionInformation().toString());
			}
			LOGGER.trace("回答情報更新 終了");

			if (baseApplicationId == null) {
				LOGGER.error("申請IDの取得に失敗");
				throw new RuntimeException("申請IDの取得に失敗");
			}

			// メール通知(行政に同意項目登録通知送付)
			LOGGER.debug("同意項目登録通知送付 開始");
			sendRegistConsentMailToGovernmentUser(baseApplicationId,baseApplicationStepId,answerFormList);
			LOGGER.debug("同意項目登録通知送付 終了");

		} finally {
			LOGGER.debug("同意項目承認否認登録 終了");
		}
	}

	/**
	 * 行政に同意項目登録通知送付
	 * 
	 * @param applicationId  申請ID
	 * @param applicationId  申請段階ID
	 * @param answerFormList 登録パラメータ
	 */
	private void sendRegistConsentMailToGovernmentUser(Integer applicationId, Integer applicationStepId,
			List<AnswerForm> answerFormList) {

		LOGGER.debug("同意項目登録通知 開始");
		// 回答DAO
		AnswerDao answerDao = new AnswerDao(emf);
		// メールメッセージアイテム
		MailItem item = new MailItem();
		// メールメッセージ対象アイテム
		List<MailResultItem> mailResultItemList = new ArrayList<MailResultItem>();

		// 対象部署ID取得
		Map<String, Department> tgtDepartmentMap = new HashMap<String, Department>();
		for (AnswerForm answerForm : answerFormList) {

			String departmentId;

			// 回答情報取得
			LOGGER.trace("回答情報取得 開始");
			List<Answer> answerList = answerRepository.findByAnswerId(answerForm.getAnswerId());
			if (answerList.size() != 1) {
				LOGGER.error("回答情報の取得に失敗");
				throw new RuntimeException("回答情報の取得に失敗");
			}
			departmentId = answerList.get(0).getDepartmentId();
			LOGGER.trace("回答情報取得 終了");

			// 部署ID確認
			LOGGER.trace("部署ID確認 開始");
			if (Objects.isNull(tgtDepartmentMap.get(departmentId))) {
				// 部署情報取得
				List<Department> departmentList = departmentRepository.getDepartmentListById(departmentId);
				if (departmentList.size() != 1) {
					LOGGER.warn("部署ID不正");
					throw new RuntimeException();
				}
				tgtDepartmentMap.put(departmentId, departmentList.get(0));
			}
			LOGGER.trace("部署ID確認 終了");
		}

		// 申請者情報
		LOGGER.trace("申請者情報取得 開始");
		List<ApplicantInformation> applicantList = applicantInformationRepository.getApplicantList(applicationId,
				CONTACT_ADDRESS_INVALID);
		if (applicantList.size() < 1) {
			LOGGER.error("申請者データの件数が不正");
			throw new RuntimeException("申請者データの件数が不正");
		}
		ApplicantInformation applicant = applicantList.get(0);
		LOGGER.trace("申請者情報取得 終了");

		switch (applicantNameItemNumber) { // 氏名はプロパティで設定されたアイテムを設定
		case 1:
			item.setName(applicant.getItem1());
			break;
		case 2:
			item.setName(applicant.getItem2());
			break;
		case 3:
			item.setName(applicant.getItem3());
			break;
		case 4:
			item.setName(applicant.getItem4());
			break;
		case 5:
			item.setName(applicant.getItem5());
			break;
		case 6:
			item.setName(applicant.getItem6());
			break;
		case 7:
			item.setName(applicant.getItem7());
			break;
		case 8:
			item.setName(applicant.getItem8());
			break;
		case 9:
			item.setName(applicant.getItem9());
			break;
		case 10:
			item.setName(applicant.getItem10());
			break;
		default:
			LOGGER.error("氏名アイテム番号指定が不正: " + applicantNameItemNumber);
			throw new RuntimeException("氏名アイテム番号指定が不正");
		}
		item.setMailAddress(applicant.getMailAddress()); // 申請者メールアドレス

		// 申請情報
		LOGGER.trace("申請情報取得 開始");
		Application application = getApplication(applicationId);
		item.setApplicationId(applicationId.toString()); // 申請ID
		item.setApplicationTypeName(getApplicationTypeName(application.getApplicationTypeId())); // 申請種類名
		item.setApplicationStepName(getApplicationStepName(applicationStepId)); // 申請段階名
		item.setVersionInformation(getVersionInformation(applicationId, applicationStepId).toString()); // 版情報
		LOGGER.trace("申請情報取得 終了");

		// 地番
		LOGGER.trace("地番情報取得 開始");
		ApplicationDao applicationDao = new ApplicationDao(emf);
		List<ApplyLotNumber> lotNumbersList = applicationDao.getApplyingLotNumberList(application.getApplicationId(),
				lonlatEpsg);
		String addressText = (lotNumbersList != null && lotNumbersList.size() > 0)
				? lotNumbersList.get(0).getLotNumbers()
				: "";
		item.setLotNumber(addressText);
		LOGGER.trace("地番情報取得 終了");

		// 繰り返し部作成
		// 対象部署IDループ
		for (String departmentId : tgtDepartmentMap.keySet()) {
			
			// メールメッセージ対象アイテムクリア
			mailResultItemList.clear();

			// 部署情報取得
			Department department = tgtDepartmentMap.get(departmentId);

			// 回答情報取得
			List<Answer> answerList = answerRepository.findByApplicationIdAndApplicationStepIdAndDepartmentId(
					applicationId, applicationStepId, departmentId);
			Integer businessPassCnt = 0;

			for (Answer answer : answerList) {

				// メールメッセージ対象アイテム
				MailResultItem mailResultItem = new MailResultItem();

				// 対象取得
				LOGGER.trace("対象取得 開始");
				List<CategoryJudgement> categoryList = answerDao.getCategoryJudgementList(answer.getAnswerId());
				if (categoryList.size() == 0) {
					LOGGER.warn("M_区分判定の値が取得できない 回答ID: " + answer.getAnswerId());
					continue;
				}
				CategoryJudgement category = categoryList.get(0);
				mailResultItem.setTarget(category.getTitle());
				LOGGER.trace("対象取得 終了");
				
				// JdbcTemplateで再取得（JdbcTemplateで更新しているため）
				LOGGER.trace("回答取得 開始");
				Map<String, Object> answerMap = answerJdbc.selectAnswer(answer.getAnswerId());
				LOGGER.trace("回答取得 終了");

				// 合否内容取得
				String businessPassStatus = (String) answerMap.get("business_pass_status");

				// 合否内容
				if (BUSINESS_PASS_STATUS_1_AGREE.equals(businessPassStatus)) {
					mailResultItem.setConsentResult(BUSINESS_PASS_STATUS_1_AGREE_NAME);
					businessPassCnt++;
				} else if (BUSINESS_PASS_STATUS_0_AGREE.equals(businessPassStatus)) {
					mailResultItem.setConsentResult(BUSINESS_PASS_STATUS_0_AGREE_NAME);
					businessPassCnt++;
				} else {
					// 未選択は対象外
					continue;
				}

				// 日付
				Timestamp businessAnswerdatetime = (Timestamp) answerMap.get("business_answer_datetime");
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
				mailResultItem.setConsentDate(dateFormat.format(businessAnswerdatetime));

				// メールメッセージ対象アイテム
				mailResultItemList.add(mailResultItem);
			}
			// メールメッセージ対象アイテム
			item.setResultList(mailResultItemList);

			// {すべての項目がそろった場合：すべての項目の事業者回答登録が完了しています。}
			if (answerList.size() == businessPassCnt) {
				String contentText = getMailPropValue(
						MailMessageUtil.KEY_CONSENT_REGIST_NOTIFICATION_BODY_COMMENT_CONSENT_COMPLETED, new MailItem());
				item.setComment1(contentText);
			} else {
				item.setComment1("");
			}

			// メール送信
			// 行政に全部署回答完了通知送付
			String subject = getMailPropValue(MailMessageUtil.KEY_CONSENT_REGIST_NOTIFICATION_SUBJECT, item);
			String body = getMailPropValue(MailMessageUtil.KEY_CONSENT_REGIST_NOTIFICATION_BODY, item);

			LOGGER.trace(department.getMailAddress());
			LOGGER.trace(subject);
			LOGGER.trace(body);

			try {
				final String[] mailAddressList = department.getMailAddress().split(",");
				for (String aMailAddress : mailAddressList) {
					LOGGER.trace("メール通知 開始");
					mailSendutil.sendMail(aMailAddress, subject, body);
					LOGGER.trace("メール通知 終了");
				}
			} catch (Exception e) {
				LOGGER.error("メール送信時にエラー発生", e);
				throw new RuntimeException(e);
			}
		}
		LOGGER.debug("同意項目登録通知 終了");
	}

	/**
	 * 行政に回答更新/完了通知送付
	 * 
	 * @param applyAnswerDetailForm 登録パラメータ
	 * @param isFinished    完了かどうか
	 */
	private void sendUpdatedMainToGovernmentUser(ApplyAnswerDetailForm applyAnswerDetailForm, boolean isFinished) {
		MailItem item = new MailItem();

		List<ApplicantInformation> applicantList = applicantInformationRepository.getApplicantList(applyAnswerDetailForm.getApplicationId(),CONTACT_ADDRESS_INVALID);
		if (applicantList.size() < 1) {
			LOGGER.error("申請者データの件数が不正");
			throw new RuntimeException("申請者データの件数が不正");
		}
		ApplicantInformation applicant = applicantList.get(0);
		switch (applicantNameItemNumber) { // 氏名はプロパティで設定されたアイテムを設定
		case 1:
			item.setName(applicant.getItem1());
			break;
		case 2:
			item.setName(applicant.getItem2());
			break;
		case 3:
			item.setName(applicant.getItem3());
			break;
		case 4:
			item.setName(applicant.getItem4());
			break;
		case 5:
			item.setName(applicant.getItem5());
			break;
		case 6:
			item.setName(applicant.getItem6());
			break;
		case 7:
			item.setName(applicant.getItem7());
			break;
		case 8:
			item.setName(applicant.getItem8());
			break;
		case 9:
			item.setName(applicant.getItem9());
			break;
		case 10:
			item.setName(applicant.getItem10());
			break;
		default:
			LOGGER.error("氏名アイテム番号指定が不正: " + applicantNameItemNumber);
			throw new RuntimeException("氏名アイテム番号指定が不正");
		}
		item.setMailAddress(applicant.getMailAddress()); // メールアドレス

		// O_申請取得
		Application application = getApplication(applyAnswerDetailForm.getApplicationId());
		item.setApplicationId(applyAnswerDetailForm.getApplicationId().toString()); // 申請ID
		item.setApplicationTypeName(
				getApplicationTypeName(application.getApplicationTypeId())); // 申請種類名
		item.setApplicationStepName(
				getApplicationStepName(applyAnswerDetailForm.getApplicationStepId())); // 申請段階名
		item.setVersionInformation(
				getVersionInformation(applyAnswerDetailForm.getApplicationId(),
						applyAnswerDetailForm.getApplicationStepId()).toString()); // 版情報

		// 地番
		ApplicationDao applicationDao = new ApplicationDao(emf);
		List<ApplyLotNumber> lotNumbersList = applicationDao.getApplyingLotNumberList(applyAnswerDetailForm.getApplicationId(), lonlatEpsg);
		String addressText = (lotNumbersList != null && lotNumbersList.size() > 0) ? lotNumbersList.get(0).getLotNumbers(): "";
		item.setLotNumber(addressText);

		String subject, body;
		if (!isFinished) {
			return;
		} else {
			// 行政に全部署回答完了通知送付
			subject = getMailPropValue(MailMessageUtil.KEY_FINISH_SUBJECT, item);
			body = getMailPropValue(MailMessageUtil.KEY_FINISH_BODY, item);
		}

		// 部署リスト
		List<Department> departmentList = departmentRepository.getDepartmentList();
		for (Department department : departmentList) {
			if (!department.getAnswerAuthorityFlag()) {
				// 回答権限がない
				continue;
			}
			LOGGER.trace(department.getMailAddress());
			LOGGER.trace(subject);
			LOGGER.trace(body);

			try {
				final String[] mailAddressList = department.getMailAddress().split(",");
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
	 * 回答権限チェック
	 * 
	 * @param departmentId 部署ID
	 * @return 判定結果
	 */
	public boolean checkAnswerAuthority(String departmentId) {
		LOGGER.debug("回答権限チェック 開始");
		try {
			List<Department> departmentList = departmentRepository.getDepartmentListById(departmentId);
			if (departmentList.size() != 1) {
				LOGGER.warn("部署取得件数不正");
				return false;
			}
			return departmentList.get(0).getAnswerAuthorityFlag();
		} finally {
			LOGGER.debug("回答権限チェック 終了");
		}
	}

	/**
	 * 回答通知権限チェック
	 * 
	 * @param departmentId      ユーザーID
	 * @param applicationStepId 申請段階ID
	 * @param notifyType        回答通知種類
	 * @param answers           回答一覧
	 * @return 判定結果
	 */
	public boolean checkNotifyAuthority(String userId, Integer applicationStepId, String notifyType, List<AnswerForm> answers) {
		LOGGER.debug("回答通知権限チェック 開始");
		try {

			GovernmentUserDao governmentUserDao = new GovernmentUserDao(emf);
			List<GovernmentUserAndAuthority> governmentUserAndAuthorityList = governmentUserDao
					.getGovernmentUserInfo(userId, applicationStepId);
			if (governmentUserAndAuthorityList.size() < 0) {
				LOGGER.warn("ユーザー情報がない");
				return false;
			}

			GovernmentUserAndAuthority userInfo = governmentUserAndAuthorityList.get(0);

			// 事前協議の場合、
			if (APPLICATION_STEP_ID_1.equals(applicationStepId)) {

				// 通知権限があるかチェック
				if (AUTH_TYPE_NONE.equals(userInfo.getNotificationAuthorityFlag())) {
					return false;
				}
			}

			// 事前協議の場合、
			if (APPLICATION_STEP_ID_2.equals(applicationStepId)) {

				// 0:事業者に回答通知
				if (NOTIFY_TYPE_0_ANSWERED.equals(notifyType)) {

					// ■事業者へ通知の仕様
					// 回答通知確認画面に選択した条項だけではない、部署ごとのすべての回答は、2回目回答通知（回答内容）の回答があるか判断
					// ありの場合、2回目の回答通知として、ログインユーザーが通知の条項の担当課管理者ではない場合、アクセス権限なしになる
					// なしの場合、1回目の回答通知として、ログインユーザーが統括部署管理者ではない場合、アクセス権限ないになる

					// 画面選択している回答リストから、部署リストを抽出する
					final List<Integer> departmentAnswerIdList = new ArrayList<Integer>();
					for (AnswerForm answerForm : answers) {
						if (!departmentAnswerIdList.contains(answerForm.getDepartmentAnswerId())) {
							departmentAnswerIdList.add(answerForm.getDepartmentAnswerId());
						}
					}

					// 部署単体で、判断する
					for (Integer departmentAnswerId : departmentAnswerIdList) {
						// 2回目の回答通知ができる回答（通知済みかつ事業者合意未登録）を取得
						List<Answer> answerList = answerRepository
								.getNotifiedAnswerListByDepartmentAnswerId(departmentAnswerId);

						// 取得できた場合、ログインユーザーが担当課管理者以外の場合、エラーにする
						// 取得できなかった場合、ログインユーザーが統括部署管理者以外の場合エラーにする
						if (answerList.size() > 0) {

							// 部署回答IDから、部署IDを取得
							List<DepartmentAnswer> departmentAnswerList = departmentAnswerRepository
									.findByDepartmentAnswerId(departmentAnswerId);
							if (departmentAnswerList.size() < 0) {
								LOGGER.warn("O_部署回答が存在しない。部署回答ID：" + departmentAnswerId);
								return false;
							}
							String departmentId = departmentAnswerList.get(0).getDepartmentId();

							// ログインユーザーが担当課管理者以外の場合、アクセス権限エラーとする
							if (!userInfo.getDepartmentId().equals(departmentId) || !userInfo.getAdminFlag()) {
								return false;
							}
						} else {
							// ログインユーザーが統括部署管理者以外の場合、アクセス権限エラーとする
							if (!userInfo.getManagementDepartmentFlag() || !userInfo.getAdminFlag()) {
								return false;
							}
						}
					}
				}

				// 1：事業者に差戻通知 、又は、 2：担当課に受付通知の場合、
				if (NOTIFY_TYPE_1_REMANDED.equals(notifyType) || NOTIFY_TYPE_2_ACCEPTED.equals(notifyType)) {

					// 統括部署の管理者以外の場合、回答通知不可
					if (!userInfo.getManagementDepartmentFlag() || !userInfo.getAdminFlag()) {
						return false;
					}
				}

				// 3：統括部署管理者に回答許可通知 又は 4：統括部署管理者に行政確定登録許可通知の場合、
				if (NOTIFY_TYPE_3_ANSWER_PERMISSION.equals(notifyType)
						|| NOTIFY_TYPE_4_GOVERNMENT_CONFIRM_PERMISSION.equals(notifyType)) {

					// 管理者以外 又は 回答通知権限持っていないの場合、回答通知不可
					if (!userInfo.getAdminFlag() || AUTH_TYPE_NONE.equals(userInfo.getNotificationAuthorityFlag())) {
						return false;
					}
				}

			}

			// 許可判定の場合、
			if (APPLICATION_STEP_ID_3.equals(applicationStepId)) {

				// 管理者以外 又は 回答通知権限持っていないの場合、回答通知不可
				if (!userInfo.getAdminFlag() || AUTH_TYPE_NONE.equals(userInfo.getNotificationAuthorityFlag())) {
					return false;
				}
			}

			return true;
		} finally {
			LOGGER.debug("回答通知権限チェック 終了");
		}
	}

	/**
	 * 回答通知
	 * 
	 * @param answerNotifyRequestForm 回答通知リクエストフォーム
	 */
	public void notifyAnswer(AnswerNotifyRequestForm answerNotifyRequestForm) {
		LOGGER.debug("回答通知 開始");
		try {
			// 申請ID
			int applicationId = answerNotifyRequestForm.getApplicationId();
			// 申請段階ID
			Integer applicationStepId = answerNotifyRequestForm.getApplicationStepId();

			// O_申請の登録ステータスをチェック
			List<Application> applicationList = applicationRepository.getApplicationList(applicationId);
			if (applicationList.size() != 1) {
				LOGGER.error("申請データの件数が不正");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}
			Application application = applicationList.get(0);
			String status = application.getStatus();
			// 事前相談
			if (APPLICATION_STEP_ID_1.equals(applicationStepId)) {

				if (!STATUS_CONSULTATION_ANSWERED_PREPARING.equals(status)
						&& !STATUS_CONSULTATION_ANSWERED_REVIEWING.equals(status)
						&& !STATUS_CONSULTATION_REAPP.equals(status)) {
					// ステータスが未完（回答準備中）、または未完（回答精査中）、未完（要再申請）ではないので不正
					LOGGER.error("ステータスが事前相談：未完（回答準備中）、または事前相談：未完（回答精査中）、事前相談：未完（要再申請）でない");
					throw new ResponseStatusException(HttpStatus.CONFLICT);
				}
			}
			// 事前協議
			if (APPLICATION_STEP_ID_2.equals(applicationStepId)) {
				if (!STATUS_DISCUSSIONS_ANSWERED_PREPARING.equals(status)
						&& !STATUS_DISCUSSIONS_ANSWERED_REVIEWING.equals(status)
						&& !STATUS_DISCUSSIONS_IN_PROGRESS.equals(status) && !STATUS_DISCUSSIONS_REAPP.equals(status)) {
					// ステータスが未完（回答準備中）、または未完（回答精査中）、未完（協議進行中）、未完（要再申請）ではないので不正
					LOGGER.error("ステータスが事前協議：未完（回答準備中）、または事前協議：未完（回答精査中）、事前協議：未完（協議進行中）、事前協議：未完（要再申請）でない");
					throw new ResponseStatusException(HttpStatus.CONFLICT);
				}
			}

			// 許可判定
			if (APPLICATION_STEP_ID_3.equals(applicationStepId)) {
				if (!STATUS_PERMISSION_ANSWERED_PREPARING.equals(status)
						&& !STATUS_PERMISSION_ANSWERED_REVIEWING.equals(status)
						&& !STATUS_PERMISSION_REAPP.equals(status)) {
					// ステータスが未完（回答準備中）、または未完（回答精査中）、未完（要再申請）ではないので不正
					LOGGER.error("ステータスが許可判定：未完（回答準備中）、または許可判定：未完（回答精査中）、許可判定：未完（要再申請）でない");
					throw new ResponseStatusException(HttpStatus.CONFLICT);
				}
			}

			LOGGER.debug("回答通知更新処理 開始");
			// 部署IDリスト
			List<String> departmentIdList = new ArrayList<String>();
			// 回答ID-回答内容マップ（回答レポート作成用）
			final Map<Integer, String> answerNotifiedTextMap = new HashMap<Integer, String>();
			AnswerDao dao = new AnswerDao(emf);

			// 画面に選択される回答リスト
			List<AnswerForm> answerFormList = answerNotifyRequestForm.getAnswers();
			for (AnswerForm answerForm : answerFormList) {
				LOGGER.debug("回答ID: " + answerForm.getAnswerId());
				// 何かで検索を行う時に、「値はchart(1)に長すぎ」とエラーが発生するため、DAOで検索を行うようにする
				List<Answer> answerList = dao.getAnswerList(answerForm.getAnswerId());
				Answer answer = answerList.get(0);
				// 削除未通知フラグの場合、該当レコードを論理削除
				if (APPLICATION_STEP_ID_2.equals(applicationStepId) && answer.getDeleteUnnotifiedFlag() != null
						&& answer.getDeleteUnnotifiedFlag()) {

					if (answerJdbc.updateDeleteFlag(answer) != 1) {
						LOGGER.error("回答データの更新件数が不正");
						throw new RuntimeException("回答データの更新件数が不正");
					}
				} else {

					// 回答ステータスが却下、同意済みになる場合、行政確定が登録済みとして、行政確定通知フラグが1に更新
					if (ANSWER_STATUS_REJECTED.equals(answer.getAnswerStatus())
							|| ANSWER_STATUS_AGREED.equals(answer.getAnswerStatus())) {
						answer.setGovernmentConfirmNotifiedFlag(true);
					}

					// 事前協議：2回目以降の回答通知を行う場合、回答許可未通知でも、事業者へ通知できるため、回答通知許可フラグを一緒に更新
					if(APPLICATION_STEP_ID_2.equals(applicationStepId)) {
						if(answerForm.getAnswerPermissionFlag() != null && answerForm.getAnswerPermissionFlag()) {
							answer.setAnswerPermissionFlag(true);
						}
					}
					// 申請IDと紐づくO_回答の回答内容(answer_content)を通知テキスト(notified_text)にCopy
					if (answerJdbc.copyNotifyText(answer) != 1) {
						LOGGER.error("回答データの更新件数が不正");
						throw new RuntimeException("回答データの更新件数が不正");
					}
				}
				// O_回答履歴.通知フラグを更新
				try {
					answerHistoryJdbc.updateAnswerHistoryNotifyFlag(answer.getAnswerId());
				} catch (Exception e) {
					LOGGER.error("回答履歴データの更新に失敗");
					throw new RuntimeException("回答履歴データの更新に失敗");
				}

				// 事前相談の場合、回答ごとの回答ファイルの通知更新を行う
				if (APPLICATION_STEP_ID_1.equals(applicationStepId)) {
					List<AnswerFile> answerFileList = answerFileRepository.findByAnswerId(answer.getAnswerId());
					notifyAnswerFile(answerFileList);
				}
				// 事前協議
				if (APPLICATION_STEP_ID_2.equals(applicationStepId)) {
					if (!departmentIdList.contains(answer.getDepartmentId())) {
						departmentIdList.add(answer.getDepartmentId());
					}
				}
				
				// 通知テキストマップに追加
				answerNotifiedTextMap.put(answer.getAnswerId(), answer.getAnswerContent());
			}

			// 事前協議
			// 画面に選択される部署回答リスト
			List<DepartmentAnswerForm> departmentAnswerFormList = answerNotifyRequestForm.getDepartmentAnswers();
			if (APPLICATION_STEP_ID_2.equals(applicationStepId)) {
				for (DepartmentAnswerForm departmentAnswerForm : departmentAnswerFormList) {
					LOGGER.debug("部署回答ID: " + departmentAnswerForm.getDepartmentAnswerId());

					List<DepartmentAnswer> departmentAnswerList = departmentAnswerRepository
							.findByDepartmentAnswerId(departmentAnswerForm.getDepartmentAnswerId());
					DepartmentAnswer departmentAnswer = departmentAnswerList.get(0);

					// 申請IDと紐づくO_部署回答の行政確定コメントを通知テキスト(notified_text)にCopy
					if (departmentAnswerJdbc.copyNotifyText(departmentAnswer) != 1) {
						LOGGER.error("部署回答データの更新件数が不正");
						throw new RuntimeException("部署回答データの更新件数が不正");
					}

					if (!departmentIdList.contains(departmentAnswer.getDepartmentId())) {
						departmentIdList.add(departmentAnswer.getDepartmentId());
					}
				}

				// 回答ごとの回答ファイルの通知更新を行う
				List<AnswerFile> answerFileList = answerFileRepository.findByDepartmentId(applicationId,
						applicationStepId, departmentIdList);
				notifyAnswerFile(answerFileList);
			}

			// 許可判定
			if (APPLICATION_STEP_ID_3.equals(applicationStepId)) {
				// 回答ごとの回答ファイルの通知更新を行う
				List<AnswerFile> answerFileList = answerFileRepository.findByApplicationStepId(applicationId,
						applicationStepId);
				notifyAnswerFile(answerFileList);
			}

			LOGGER.debug("回答通知更新処理処理 完了");

			LOGGER.debug("申請のステータス判定処理 開始");
			// 申請ステータス
			String updateStatus = EMPTY;
			// 再申請要否
			boolean isReaplication = false;
			// 未通知回答有無
			boolean isExistNotNotified = false;

			// 通知更新後、回答リストを取得
			List<Answer> answerList = answerRepository.findByApplicationStepId(applicationId, applicationStepId);
			for (Answer answer : answerList) {

				// 許可判定移行フラグ(1:許可判定移行時チェックしない)
				boolean permissionJudgementMigrationFlag = answer.getPermissionJudgementMigrationFlag() == null ? false : answer.getPermissionJudgementMigrationFlag();

				// 事前協議の場合、許可判定移行フラグ＝true　の回答は申請ステータス判定対象外とする
				if(APPLICATION_STEP_ID_2.equals(applicationStepId) && permissionJudgementMigrationFlag) {
					continue;
				}

				// 通知済みの再申請フラグ
				boolean businessReApplicationFlag = answer.getBusinessReApplicationFlag() == null ? false
						: answer.getBusinessReApplicationFlag();
				// 通知済みフラグ
				boolean notifiedFlag = answer.getNotifiedFlag() == null ? false : answer.getNotifiedFlag();
				// 回答変更フラグ
				boolean answerUpdateFlag = answer.getAnswerUpdateFlag() == null ? false : answer.getAnswerUpdateFlag();
				// 完了フラグ
				boolean completeFlag = answer.getCompleteFlag() == null ? false : answer.getCompleteFlag();

				// 行政確定通知フラグ
				boolean governmentConfirmNotifiedFlag = answer.getGovernmentConfirmNotifiedFlag() == null ? false
						: answer.getGovernmentConfirmNotifiedFlag();
				// 行政確定ステータス
				String governmentConfirmStatus = answer.getGovernmentConfirmStatus();

				// 画面に選択される回答リストに存在する場合、画面の入力値を使う
				for (AnswerForm answerForm : answerFormList) {
					if (answer.getAnswerId().equals(answerForm.getAnswerId())) {
						businessReApplicationFlag = answerForm.getReApplicationFlag() == null ? false
								: answerForm.getReApplicationFlag();
						notifiedFlag = true;
						answerUpdateFlag = false;
						completeFlag = true; // フロント側で、未回答条項であるかチェックを行なったため、API側で回答完了とする
						governmentConfirmStatus = answerForm.getGovernmentConfirmStatus();
						// 回答内容部分が通知済みであれば、行政確定登録通知済みとする
						if (answer.getNotifiedFlag() != null && answer.getNotifiedFlag()) {
							governmentConfirmNotifiedFlag = true;
						}
					}
				}

				// 事前相談、許可判定
				if (APPLICATION_STEP_ID_1.equals(applicationStepId) || APPLICATION_STEP_ID_3.equals(applicationStepId)) {
					// 通知済みの回答中に、1件でも要再申請の回答があれば再申請とする
					if (businessReApplicationFlag) {
						isReaplication = true;
					}

					// 通知フラグが未通知のレコードが1件でもあれば、回答が未完了とする
					if (!notifiedFlag) {
						isExistNotNotified = true;
					} else {
						// 通知済み後、回答変更がある回答があれば、回答が未完了とする
						if (answerUpdateFlag) {
							isExistNotNotified = true;
						}

						// 通知済み後、再申請を行う伴い、完了フラグがリセットされたため、再申請後回答登録か判定
						if(!completeFlag) {
							isExistNotNotified = true;
						}
					}
				}

				// 事前協議
				if (APPLICATION_STEP_ID_2.equals(applicationStepId)) {
					// 通知済みの回答中に、1件でも却下した回答があれば再申請とする→却下がなくなたため、通常は到達できない
					if (governmentConfirmNotifiedFlag && governmentConfirmStatus != null
							&& GOVERNMENT_CONFIRM_STATUS_2_REJECT.equals(governmentConfirmStatus)) {
						isReaplication = true;
					}

					// 通知フラグが未通知のレコードが1件でもあれば、回答が未完了とする
					if (!notifiedFlag) {
						isExistNotNotified = true;
					} else {
						// 回答通知済み後、行政確定まだ未通知の回答があれば、回答が未完了とする
						if (!governmentConfirmNotifiedFlag) {
							isExistNotNotified = true;
						}
					}
				}
				
				// 画面に選択していない通知済み回答内容を追加
				if (!answerNotifiedTextMap.containsKey(answer.getAnswerId())) {
					answerNotifiedTextMap.put(answer.getAnswerId(), answer.getNotifiedText());
				}
			}

			// 事前協議
			if (APPLICATION_STEP_ID_2.equals(applicationStepId)) {

				// 通知更新後、部署回答リストを取得
				List<DepartmentAnswer> departmentAnswerList = departmentAnswerRepository
						.findByApplicationId(applicationId);
				for (DepartmentAnswer departmentAnswer : departmentAnswerList) {
					// 通知済みフラグ
					boolean notifiedFlag = departmentAnswer.getNotifiedFlag() == null ? false
							: departmentAnswer.getNotifiedFlag();
					// 行政確定ステータス
					String governmentConfirmStatus = departmentAnswer.getGovernmentConfirmStatus();

					// 画面に選択される回答リストに存在する場合、画面の入力値を使う
					for (DepartmentAnswerForm departmentAnswerForm : departmentAnswerFormList) {
						if (departmentAnswer.getDepartmentAnswerId()
								.equals(departmentAnswerForm.getDepartmentAnswerId())) {
							notifiedFlag = true;
							governmentConfirmStatus = departmentAnswerForm.getGovernmentConfirmStatus();
						}
					}

					// 通知済みの部署回答中に、1件でも却下した回答があれば再申請とする
					if (notifiedFlag && governmentConfirmStatus != null && "2".equals(governmentConfirmStatus)) {
						isReaplication = true;
					}

					// 通知フラグが未通知のレコードが1件でもあれば、回答が未完了とする
					if (!notifiedFlag) {
						isExistNotNotified = true;
					}
				}
			}

			//処理中の申請段階が完了かどうか
			boolean isComplete = false;

			// 未通知回答有無
			if (!isExistNotNotified) {
				// 通知済みの回答中に、要再申請の回答があるか
				if (isReaplication) {
					// 全ての回答が通知済み、かつ、要再申請があるの場合、未完（要再申請）とする
					// 事前相談
					if (APPLICATION_STEP_ID_1.equals(applicationStepId)) {
						updateStatus = STATUS_CONSULTATION_REAPP;
					}
					// 事前協議
					if (APPLICATION_STEP_ID_2.equals(applicationStepId)) {
						updateStatus = STATUS_DISCUSSIONS_REAPP;
					}
					// 許可判定
					if (APPLICATION_STEP_ID_3.equals(applicationStepId)) {
						updateStatus = STATUS_PERMISSION_REAPP;
					}
				} else {
					// 全ての回答が通知済み、かつ、要再申請がないの場合、完了とする
					// 事前相談
					if (APPLICATION_STEP_ID_1.equals(applicationStepId)) {
						updateStatus = STATUS_CONSULTATION_COMPLETED;
						isComplete = true;
					}
					// 事前協議
					if (APPLICATION_STEP_ID_2.equals(applicationStepId)) {
						updateStatus = STATUS_DISCUSSIONS_COMPLETED;
						isComplete = true;
					}
					// 許可判定
					if (APPLICATION_STEP_ID_3.equals(applicationStepId)) {
						updateStatus = STATUS_PERMISSION_COMPLETED;
						isComplete = true;
					}
				}

			} else {
				// 通知済みの回答中に、要再申請の回答があるか
				if (isReaplication) {
					// 一部回答のみが通知済み、かつ、要再申請があるの場合、未完（要再申請）とする
					// 事前相談
					if (APPLICATION_STEP_ID_1.equals(applicationStepId)) {
						updateStatus = STATUS_CONSULTATION_REAPP;
					}
					// 事前協議
					if (APPLICATION_STEP_ID_2.equals(applicationStepId)) {
						updateStatus = STATUS_DISCUSSIONS_REAPP;
					}
					// 許可判定
					if (APPLICATION_STEP_ID_3.equals(applicationStepId)) {
						updateStatus = STATUS_PERMISSION_REAPP;
					}
				} else {
					// 一部回答のみが通知済み、かつ、要再申請がないの場合、未完（回答精査中）、未完（協議進行中）とする
					// 事前相談
					if (APPLICATION_STEP_ID_1.equals(applicationStepId)) {
						updateStatus = STATUS_CONSULTATION_ANSWERED_REVIEWING;
					}
					// 事前協議
					if (APPLICATION_STEP_ID_2.equals(applicationStepId)) {
						updateStatus = STATUS_DISCUSSIONS_IN_PROGRESS;
					}
					// 許可判定
					if (APPLICATION_STEP_ID_3.equals(applicationStepId)) {
						updateStatus = STATUS_PERMISSION_ANSWERED_REVIEWING;
					}
				}
			}
			LOGGER.debug("申請のステータス判定処理 完了");

			LOGGER.debug("申請のステータス更新処理 開始");

			if (!EMPTY.equals(updateStatus)) {

				// O_申請のステータスを更新
				if (applicationJdbc.updateApplicationStatus(applicationId, updateStatus) != 1) {
					LOGGER.warn("申請情報の更新不正");
					throw new RuntimeException("申請情報の更新に失敗");
				}
			}
			LOGGER.debug("申請のステータス更新処理 完了");

			if (isComplete) {
				LOGGER.debug("申請版情報の完了日時の更新処理 開始");
				// O_申請版情報の完了日時を更新
				if (applicationVersionInformationJdbc.updateCompleteDatetime(applicationId, applicationStepId) != 1) {
					LOGGER.warn("完了日時の更新不正");
					throw new RuntimeException("完了日時の更新に失敗");
				}
				LOGGER.debug("申請版情報の完了日時の更新処理 完了");
			}
			
			// 回答レポート生成
			if (APPLICATION_STEP_ID_1.equals(applicationStepId)) {
				createAnswerReport(applicationId, applicationStepId, answerNotifiedTextMap);
			}
			
			// 事前協議完了時、許可判定完了時帳票作成
			if ((APPLICATION_STEP_ID_2.equals(applicationStepId) && updateStatus.equals(STATUS_DISCUSSIONS_COMPLETED))
					|| (APPLICATION_STEP_ID_3.equals(applicationStepId)
							&& updateStatus.equals(STATUS_PERMISSION_COMPLETED))) {
				try {
					ledgerService.exportLedger(applicationId, applicationStepId);
				} catch (Exception e) {
					LOGGER.error("帳票生成に失敗", e);
				}
			}
			
			// 開発登録簿生成
			if(APPLICATION_STEP_ID_3.equals(applicationStepId)
					&& updateStatus.equals(STATUS_PERMISSION_COMPLETED)) {
				try {
					developmentRegisterService.exportDevelopmentRegister(applicationId);
				} catch (Exception e) {
					LOGGER.error("開発登録簿生成に失敗", e);
				}
			}
			
			if (isReaplication) {
				// 事業者に再申請通知送付
				sendReapplicationFinishMailToBusinessUser(application,applicationStepId,answerNotifyRequestForm,updateStatus);
			} else {
				// 事業者に回答完了通知送付
				sendFinishedMailToBusinessUser(application,applicationStepId,answerNotifyRequestForm,updateStatus);
			}

		} finally {
			LOGGER.debug("回答通知 終了");
		}
	}

	/**
	 * 回答通知を行うと伴い、回答ファイルを更新
	 * 
	 * @param answerFileList
	 */
	private void notifyAnswerFile(List<AnswerFile> answerFileList) {
		// 申請IDと紐づくO_回答ファイルのファイルパス(file_path)を通知済みファイルパス(notified_file_path)にCopy
		for (AnswerFile answerFile : answerFileList) {
			LOGGER.debug("回答ファイルID: " + answerFile.getAnswerFileId());
			LOGGER.debug("削除未通知フラグ: " + answerFile.getDeleteUnnotifiedFlag());

			if (answerFileJdbc.copyNotifyPath(answerFile) != 1) {
				LOGGER.error("回答ファイルデータの更新件数が不正");
				throw new RuntimeException("回答ファイルデータの更新件数が不正");
			}

			// 申請IDと紐づくO_回答ファイルのファイルパス(file_path)に記載してあるファイル以外の、回答ファイルIDと紐づくファイルの実体を削除する
			// 削除しないファイルパス
			String baseFilePath = answerFile.getFilePath();
			LOGGER.debug("回答ファイルベースパス: " + baseFilePath);

			File baseFile = new File(fileRootPath + baseFilePath);
			File baseFolder = baseFile.getParentFile(); // timestampフォルダ
			String baseTimestampFolderName = baseFolder.getName();

			// ファイルパスは
			// 「/answer/<申請ID>/<申請段階ID>/<回答ID>/<回答ファイルID>/<timestamp>/<アップロードファイル名>」
			// 相対フォルダパス(回答ファイルIDまで)
			String folderPath = answerFolderName;
			folderPath += PATH_SPLITTER + answerFile.getApplicationId();
			folderPath += PATH_SPLITTER + answerFile.getApplicationStepId();
			folderPath += PATH_SPLITTER + answerFile.getAnswerId();
			folderPath += PATH_SPLITTER + answerFile.getAnswerFileId();

			// 絶対フォルダパス(回答ファイルIDまで)
			String absoluteFolderPath = fileRootPath + folderPath;
			File absoluteFolder = new File(absoluteFolderPath);

			// 指定フォルダ内のフォルダリストを取得(timestampリスト)
			File[] timestampFileArray = absoluteFolder.listFiles();
			for (File timestamp : timestampFileArray) {
				LOGGER.debug("ファイルのtimestamp: " + timestamp.getName());
				if (timestamp.isFile()) {
					// ここにファイルがあるはずがない
					LOGGER.debug("ここにファイルがあるはずがないので削除");
					if (!timestamp.delete()) {
						LOGGER.error("ファイルの削除に失敗: " + timestamp.getAbsolutePath());
						throw new RuntimeException("ファイルの削除に失敗");
					}
				} else {
					LOGGER.debug("ファイルではない場合");
					if (!timestamp.getName().equals(baseTimestampFolderName)) { // timestampが異なる
						// 削除するパスなので、フォルダごと削除
						String tmpAbsoluteFolderPath = absoluteFolderPath + PATH_SPLITTER + timestamp.getName();
						LOGGER.debug("ベースのtimestampと等しくない場合なのでフォルダごと削除: " + tmpAbsoluteFolderPath);
						File tmpFile = new File(tmpAbsoluteFolderPath);
						if (tmpFile.exists()) {
							if (!FileSystemUtils.deleteRecursively(tmpFile)) {
								LOGGER.error("フォルダの削除に失敗: " + tmpAbsoluteFolderPath);
								throw new RuntimeException("フォルダの削除に失敗");
							}
						} else {
							LOGGER.warn("削除するディレクトリが存在しない");
						}
					}
				}
			}
			LOGGER.debug("更新ファイル削除 完了");

			if (answerFile.getDeleteUnnotifiedFlag()) {
				// 申請IDと紐づくO_回答ファイルの削除未通知フラグ（delete_unnotidied_flag）がtrueの回答ファイルIDと紐づくファイルの実体をすべて削除する
				File tmpFile = new File(fileRootPath + answerFile.getFilePath());
				LOGGER.debug("回答ファイル削除開始: " + tmpFile.getAbsolutePath());
				if (tmpFile.exists()) {
					if (!FileSystemUtils.deleteRecursively(tmpFile)) {
						LOGGER.error("フォルダの削除に失敗: " + tmpFile.getAbsolutePath());
						throw new RuntimeException("フォルダの削除に失敗");
					}
				} else {
					LOGGER.warn("削除する回答ファイルが存在しない");
				}
				LOGGER.debug("回答ファイル削除 完了");

				// 申請IDと紐づくO_回答ファイルの削除未通知フラグ（delete_unnotidied_flag）がtrueのレコードを削除する
				LOGGER.debug("回答ファイルDB削除開始 回答ファイルID: " + answerFile.getAnswerFileId());
				if (answerFileJdbc.delete(answerFile) != 1) {
					LOGGER.error("回答ファイルデータの削除件数が不正");
					throw new RuntimeException("回答ファイルデータの削除件数が不正");
				}
				LOGGER.debug("回答ファイルDB削除 完了");
			}
			// O_回答ファイル履歴.通知フラグ更新
			try {
				answerFileHistoryJdbc.updateAnswerFileHistoryNotifyFlag(answerFile.getAnswerFileId());
			} catch (Exception e) {
				LOGGER.error("回答ファイル履歴更新に失敗");
				throw new RuntimeException("回答ファイル履歴更新に失敗");
			}
		}
	}

	/**
	 * 事業者に回答完了通知送付
	 * 
	 * @param application           申請ID
	 * @param applicationStepId     申請段階ID
	 * @param applyAnswerDetailForm 回答通知リクエストフォーム
	 * @param updateStatus          申請のステータス
	 */
	private void sendFinishedMailToBusinessUser(Application application,Integer applicationStepId,AnswerNotifyRequestForm answerNotifyRequestForm,String updateStatus) {
		List<ApplicantInformation> applicantList = applicantInformationRepository
				.getApplicantList(application.getApplicationId(),CONTACT_ADDRESS_VALID);
		if (applicantList.size() < 1) {
			LOGGER.error("申請者データの件数が不正");
			throw new RuntimeException("申請者データの件数が不正");
		}
		ApplicantInformation applicant = applicantList.get(0);

		MailItem item = new MailItem();

		item.setApplicationId(application.getApplicationId().toString()); // 申請ID
		item.setApplicationTypeName(getApplicationTypeName(application.getApplicationTypeId())); // 申請種類名
		item.setApplicationStepName(getApplicationStepName(applicationStepId)); // 申請段階名
		item.setVersionInformation(getVersionInformation(application.getApplicationId(),applicationStepId).toString()); // 版情報
		
		// コメント１
		if(checkReApplicationRequired(applicationStepId,answerNotifyRequestForm,updateStatus)) {
			// ※再申請必要な場合:
			// お手数をおかけしますが、以下よりご確認の上再申請をお願いいたします。
			// ※実は、到達できない。該当パターンは「sendReapplicationFinishMailToBusinessUser」で行う
			String contentText = getMailPropValue(MailMessageUtil.KEY_BUSSINESS_FINISH_BODY_COMMENT_REAPPLICATION,
					new MailItem());
			item.setComment1(contentText);
		}
		else if(checkAnswerRegistrationRequired(applicationStepId,answerNotifyRequestForm,updateStatus)) {
			// ※事前協議で合否項目の入力事項がある場合：
			// 項目への合意を入力して回答登録をお願いします。
			String contentText = getMailPropValue(
					MailMessageUtil.KEY_BUSSINESS_FINISH_BODY_COMMENT_AGREEMENT_REGISTRATION, new MailItem());
			item.setComment1(contentText);
		}
		else if(checkProceedToNextStep(applicationStepId,answerNotifyRequestForm,updateStatus)) {
			// ※{次の申請段階（事前相談→事前協議、事前協議→許可判定）に進める場合:再申請より次段階の申請へ進んでください。}
			String contentText = getMailPropValue(MailMessageUtil.KEY_BUSSINESS_FINISH_BODY_COMMENT_NEXT_STEP,
					new MailItem());
			item.setComment1(contentText);
		} else {
			// ※上記以外の場合
			item.setComment1("");
		}
		
		// 日時
		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(mailTimestampFormat);
		item.setTimestamp(application.getRegisterDatetime().format(dateFormat));

		// 地番
		ApplicationDao applicationDao = new ApplicationDao(emf);
		List<ApplyLotNumber> lotNumbersList = applicationDao.getApplyingLotNumberList(application.getApplicationId(), lonlatEpsg);
		String addressText = (lotNumbersList != null && lotNumbersList.size() > 0) ? lotNumbersList.get(0).getLotNumbers(): "";
		item.setLotNumber(addressText);

		// 事業者に回答完了通知送付
		String subject = getMailPropValue(MailMessageUtil.KEY_BUSSINESS_FINISH_SUBJECT, item);
		String body = getMailPropValue(MailMessageUtil.KEY_BUSSINESS_FINISH_BODY, item);

		LOGGER.trace(applicant.getMailAddress());
		LOGGER.trace(subject);
		LOGGER.trace(body);

		try {
			mailSendutil.sendMail(applicant.getMailAddress(), subject, body);
		} catch (Exception e) {
			LOGGER.error("メール送信時にエラー発生", e);
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * 再申請要否チェック
	 * @param applicationStepId
	 * @param answerNotifyRequestForm
	 * @param updateStatus
	 * @return
	 */
	private Boolean checkReApplicationRequired(Integer applicationStepId,AnswerNotifyRequestForm answerNotifyRequestForm,String updateStatus) {
		
		Boolean result = false;
		
		//---------------------------------------------------------------------
		// ■再申請が必要かどうか
		//---------------------------------------------------------------------
		// (1)申請段階が事前相談(applicationStepId = 1)の場合
		// updateStatus == STATUS_CONSULTATION_REAPP
		// かつ選択済み回答リスト（applyAnswerDetailForm.getAnswers()）
		// の中に再申請フラグ=1のレコードが1件以上ある場合
		if(APPLICATION_STEP_ID_1.equals(applicationStepId)) {
			if(STATUS_CONSULTATION_REAPP.equals(updateStatus)){	
				for(AnswerForm answerForm:answerNotifyRequestForm.getAnswers()) {
					if(answerForm.getReApplicationFlag()) {
						result = true;
						break;
					}
				}
			}
		}
		// (2)申請段階が事前協議(applicationStepId = 2)の場合
		// updateStatus == STATUS_DISCUSSIONS_REAPP　
		// かつ選択済み回答リスト（applyAnswerDetailForm.getAnswers()）
		// の中に行政確定ステータス（government_confirm_status）=2（却下）のレコードが1件以上ある場合
		else if(APPLICATION_STEP_ID_2.equals(applicationStepId)) {
			if(STATUS_DISCUSSIONS_REAPP.equals(updateStatus)){
				for(AnswerForm answerForm:answerNotifyRequestForm.getAnswers()) {
					if("2".equals(answerForm.getGovernmentConfirmStatus())) {
						result = true;
						break;
					}
				}
			}
		}
		// (3)申請段階が許可判定(applicationStepId = 3)の場合
		// updateStatus == STATUS_PERMISSION_REAPP
		// かつ選択済み回答リスト（applyAnswerDetailForm.getAnswers()）
		// の中に再申請フラグ=1のレコードが1件以上ある場合
		else if(APPLICATION_STEP_ID_3.equals(applicationStepId)) {
			if(STATUS_PERMISSION_REAPP.equals(updateStatus)){
				for(AnswerForm answerForm:answerNotifyRequestForm.getAnswers()) {
					if(answerForm.getReApplicationFlag()) {
						result = true;
						break;
					}
				}
			}
		}
		
		return result;
	}

	/**
	 * 回答ステータスが承認待ち有無チェック
	 * @param applicationStepId
	 * @param answerNotifyRequestForm
	 * @param updateStatus
	 * @return
	 */
	private Boolean checkAnswerRegistrationRequired(Integer applicationStepId,AnswerNotifyRequestForm answerNotifyRequestForm,String updateStatus) {
		
		Boolean result = false;
		
		// updateStatus = STATUS_DISCUSSIONS_IN_PROGRESS かつ　
		// 選択した回答リスト（applyAnswerDetailForm.getAnswers()）中に、
		// 回答ステータスが承認待ち（ANSWER_STATUS_APPROVING）のレコードがあるか
		if(STATUS_DISCUSSIONS_IN_PROGRESS.equals(updateStatus)){
			for(AnswerForm answerForm:answerNotifyRequestForm.getAnswers()) {
				if(ANSWER_STATUS_APPROVING.equals(answerForm.getAnswerStatus())) {
					result = true;
					break;
				}
			}
		}
		
		return result;
	}

	/**
	 * 次段階に進めるかチェック
	 * @param applicationStepId
	 * @param answerNotifyRequestForm
	 * @param updateStatus
	 * @return
	 */
	private Boolean checkProceedToNextStep(Integer applicationStepId,AnswerNotifyRequestForm answerNotifyRequestForm,String updateStatus) {
		
		Boolean result = false;
		
		// ・申請IDから申請種類ID(ApplicationRepository.getApplicationList)を取得
		Application application = getApplication(answerNotifyRequestForm.getApplicationId());
		Integer applicationTypeId = application.getApplicationTypeId();
		
		// ・申請種類IDから申請種類を取得（ApplicationTypeRepository.findByApplicationTypeId）し、取得した申請段階一覧（カンマ区切り）の中（仮にstepIdListとします）の値をチェック
		ApplicationType applicationType = getApplicationType(applicationTypeId);
		
		// (1)申請段階が事前相談(applicationStepId = 1)の場合
		// updateStatus == STATUS_CONSULTATION_COMPLETED
		// かつ stepIdListに2,3が含まれる
		// ※ 1->3に進むことは現状ないですが要件として相談されているので条件に含めてください。
		if(APPLICATION_STEP_ID_1.equals(applicationStepId)) {
			if(STATUS_CONSULTATION_COMPLETED.equals(updateStatus)){
				String stepList[] = applicationType.getApplicationStep().split(",");
				for(int i=0; i<stepList.length; i++) {
					if(APPLICATION_STEP_ID_2.toString().equals(stepList[i])
							|| APPLICATION_STEP_ID_3.toString().equals(stepList[i])) {
						result = true;
						break;
					}
				}
			}
		}
		
		//(2)申請段階が事前協議(applicationStepId = 2)の場合
		//updateStatus == STATUS_DISCUSSIONS_COMPLETED
		//かつ stepIdListに3が含まれる
		if(APPLICATION_STEP_ID_2.equals(applicationStepId)) {
			if(STATUS_DISCUSSIONS_COMPLETED.equals(updateStatus)){
				String stepList[] = applicationType.getApplicationStep().split(",");
				for(int i=0; i<stepList.length; i++) {
					if(APPLICATION_STEP_ID_3.toString().equals(stepList[i])) {
						result = true;
						break;
					}
				}
			}
		}
		
		// (3)申請段階が許可判定(applicationStepId = 3)の場合
		// updateStatus == STATUS_PERMISSION_COMPLETED
		// →次の申請段階はないので案内不要
		if(APPLICATION_STEP_ID_3.equals(applicationStepId)) {
			if(STATUS_PERMISSION_COMPLETED.equals(updateStatus)){
				;
			}
		}
		
		return result;
	}
	
	/**
	 * 事業者に回答完了（再申請）通知送付
	 * 
	 * @param application             申請ID
	 * @param applicationStepId       申請段階ID
	 * @param answerNotifyRequestForm 回答通知リクエストフォーム
	 * @param updateStatus            申請のステータス
	 */
	private void sendReapplicationFinishMailToBusinessUser(Application application,Integer applicationStepId,AnswerNotifyRequestForm answerNotifyRequestForm,String updateStatus) {
		List<ApplicantInformation> applicantList = applicantInformationRepository
				.getApplicantList(application.getApplicationId(),CONTACT_ADDRESS_VALID);
		if (applicantList.size() < 1) {
			LOGGER.error("申請者データの件数が不正");
			throw new RuntimeException("申請者データの件数が不正");
		}
		ApplicantInformation applicant = applicantList.get(0);

		MailItem item = new MailItem();

		item.setApplicationId(application.getApplicationId().toString()); // 申請ID
		item.setApplicationTypeName(getApplicationTypeName(application.getApplicationTypeId())); // 申請種類名
		item.setApplicationStepName(getApplicationStepName(applicationStepId)); // 申請段階名
		item.setVersionInformation(getVersionInformation(application.getApplicationId(),applicationStepId).toString()); // 版情報
		
		// コメント１
		if(checkReApplicationRequired(applicationStepId,answerNotifyRequestForm,updateStatus)) {
			// ※再申請必要な場合:
			// お手数をおかけしますが、以下よりご確認の上再申請をお願いいたします。
			String contentText = getMailPropValue(MailMessageUtil.KEY_BUSSINESS_FINISH_BODY_COMMENT_REAPPLICATION,
					new MailItem());
			item.setComment1(contentText);
		}
		else if(checkAnswerRegistrationRequired(applicationStepId,answerNotifyRequestForm,updateStatus)) {
			// ※事前協議で合否項目の入力事項がある場合：
			// 項目への合意を入力して回答登録をお願いします。
			// ※実は、到達できない。該当パターンは、「sendFinishedMailToBusinessUser」で行う
			String contentText = getMailPropValue(
					MailMessageUtil.KEY_BUSSINESS_FINISH_BODY_COMMENT_AGREEMENT_REGISTRATION, new MailItem());
			item.setComment1(contentText);
		}
		else if(checkProceedToNextStep(applicationStepId,answerNotifyRequestForm,updateStatus)) {
			// ※{次の申請段階（事前相談→事前協議、事前協議→許可判定）に進める場合:再申請より次段階の申請へ進んでください。}
			String contentText = getMailPropValue(MailMessageUtil.KEY_BUSSINESS_FINISH_BODY_COMMENT_NEXT_STEP,
					new MailItem());
			item.setComment1(contentText);
		} else {
			// ※上記以外の場合
			item.setComment1("");
		}
		
		// 日時
		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(mailTimestampFormat);
		item.setTimestamp(application.getRegisterDatetime().format(dateFormat));

		// 地番
		ApplicationDao applicationDao = new ApplicationDao(emf);
		List<ApplyLotNumber> lotNumbersList = applicationDao.getApplyingLotNumberList(application.getApplicationId(), lonlatEpsg);
		String addressText = (lotNumbersList != null && lotNumbersList.size() > 0) ? lotNumbersList.get(0).getLotNumbers(): "";
		item.setLotNumber(addressText);

		// 回答完了（再申請）通知（事業者）
		// 統合 String subject = getMailPropValue(MailMessageUtil.KEY_REAPPLICATION_ANSWER_FINISH_SUBJECT, item);
		// 統合 String body = getMailPropValue(MailMessageUtil.KEY_REAPPLICATION_ANSWER_FINISH_BODY, item);
		// 回答完了通知（事業者）
		String subject = getMailPropValue(MailMessageUtil.KEY_BUSSINESS_FINISH_SUBJECT, item);
		String body = getMailPropValue(MailMessageUtil.KEY_BUSSINESS_FINISH_BODY, item);

		LOGGER.trace(applicant.getMailAddress());
		LOGGER.trace(subject);
		LOGGER.trace(body);

		try {
			mailSendutil.sendMail(applicant.getMailAddress(), subject, body);
		} catch (Exception e) {
			LOGGER.error("メール送信時にエラー発生", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * 回答ファイルアップロードパラメータ確認
	 * 
	 * @param form         パラメータ
	 * @param departmentId 部署ID
	 * @return 確認結果
	 */
	public boolean validateUploadAnswerFile(AnswerFileForm form, String departmentId) {
		LOGGER.debug("回答ファイルアップロードパラメータ確認 開始");
		try {
			Integer answerId = form.getAnswerId();
			String answerFileName = form.getAnswerFileName();
			Integer applicationId = form.getApplicationId();
			Integer applicationStepId = form.getApplicationStepId();
			String answerdDpartmentId = form.getDepartmentId();

			if (
			answerFileName == null || EMPTY.equals(answerFileName) //
					|| departmentId == null || EMPTY.equals(departmentId) || applicationStepId == null
					|| applicationId == null) {
				// パラメータ不足
				LOGGER.warn("パラメータ不足");
				return false;
			}

			// 回答データ存在チェック
			if (APPLICATION_STEP_ID_1.equals(applicationStepId)) {
				if (answerId == null) {
					// パラメータ不足
					LOGGER.warn("パラメータ不足：回答ID");
					return false;
				}
				// 回答ID
				if (answerRepository.findByAnswerId(answerId).size() != 1) {
					LOGGER.warn("回答データ取得件数不正");
					return false;
				}
			}

			if (APPLICATION_STEP_ID_2.equals(applicationStepId)) {

				if (answerdDpartmentId == null || EMPTY.equals(answerdDpartmentId)) {
					// パラメータ不足
					LOGGER.warn("パラメータ不足:回答ファイルの部署ID");
					return false;
				}

				// 部署回答の件数チェック
				if (departmentAnswerRepository.getDepartmentAnswer(applicationId, answerdDpartmentId).size() != 1) {
					LOGGER.warn("回答データ取得件数不正");
					return false;
				}
			}

			if (APPLICATION_STEP_ID_3.equals(applicationStepId)) {

				// 回答の件数チェック
				if (answerRepository.findByApplicationStepId(applicationId, applicationStepId).size() < 1) {
					LOGGER.warn("回答データ取得件数不正");
					return false;
				}
			}

			// 回答ファイルID
			Integer answerFileId = form.getAnswerFileId();
			if (answerFileId != null) {
				if (answerFileRepository.findByAnswerFileId(answerFileId).size() != 1) {
					LOGGER.warn("回答ファイルの件数が不正");
					return false;
				}
			}

			// 部署チェック
			LOGGER.trace("部署チェック 開始");
			boolean departmentFlg = false;
			List<Authority> authorityList = authorityRepository.getAuthorityList(departmentId, applicationStepId);
			if (authorityList.size() > 0) {
				String answerAuthorityFlag = authorityList.get(0).getAnswerAuthorityFlag();

				// 他部署も操作可能場合、アクセス権限ある
				if (AUTH_TYPE_ALL.equals(answerAuthorityFlag)) {
					departmentFlg = true;
				}

				// 自身の部署のも操作可能場合、アップロード対象の部署がログインユーザの部署と同じか判断
				if (AUTH_TYPE_SELF.equals(answerAuthorityFlag)) {
					if (APPLICATION_STEP_ID_1.equals(applicationStepId)) {
						AnswerDao dao = new AnswerDao(emf);
						List<Department> departmentList = dao.getDepartmentList(answerId);
						for (Department department : departmentList) {
							if (departmentId.equals(department.getDepartmentId())) {
								departmentFlg = true;
								break;
							}
						}
					}

					if (APPLICATION_STEP_ID_2.equals(applicationStepId)) {

						if (answerdDpartmentId.equals(departmentId)) {
							departmentFlg = true;
						}
					}
					if (APPLICATION_STEP_ID_3.equals(applicationStepId)) {
						departmentFlg = true;
					}
				}
			}

			if (!departmentFlg) {
				// 回答アクセス権限がない
				LOGGER.warn("回答アクセス権限がない");
				return false;
			}
			LOGGER.trace("部署チェック 終了");

			return true;
		} finally {
			LOGGER.debug("回答ファイルアップロードパラメータ確認 終了");
		}
	}

	/**
	 * 回答ファイルアップロード
	 * 
	 * @param form   パラメータ
	 * @param userId 更新ユーザID
	 */
	public void uploadAnswerFile(AnswerFileForm form, String userId) {
		LOGGER.debug("回答ファイルアップロード 開始");
		try {
			// ファイルパスは「/answer/<回答ID>/<回答ファイルID>/<timestamp>/<アップロードファイル名>」
			SimpleDateFormat sdf = new SimpleDateFormat(answerFolderNameFormat);
			String nowTime = sdf.format(new Date());

			Integer answerFileId = form.getAnswerFileId();
			boolean insertFlag = false;
			if (answerFileId == null) {
				// ■ 新規登録
				// O_回答ファイル登録
				LOGGER.trace("O_回答ファイル登録 開始");
				insertFlag = true;
				answerFileId = answerFileJdbc.insert(form);
				// O_回答ファイル履歴登録
				answerFileHistoryJdbc.insert(answerFileId, form.getAnswerId(), ANSWER_FILE_HISTORY_ADD, userId);
				form.setAnswerFileId(answerFileId);
				LOGGER.trace("O_回答ファイル登録 終了");
			}

			// 相対フォルダパス
			// 「/answer/<申請ID>/<申請段階ID>/<回答ID>/<回答ファイルID>/<timestamp>/<アップロードファイル名>」
			// 相対フォルダパス(回答ファイルIDまで)
			String folderPath = answerFolderName;
			folderPath += PATH_SPLITTER + form.getApplicationId();
			folderPath += PATH_SPLITTER + form.getApplicationStepId();
			folderPath += PATH_SPLITTER + form.getAnswerId();
			folderPath += PATH_SPLITTER + form.getAnswerFileId();
			folderPath += PATH_SPLITTER + nowTime;

			// 絶対フォルダパス
			String absoluteFolderPath = fileRootPath + folderPath;
			Path directoryPath = Paths.get(absoluteFolderPath);
			if (!Files.exists(directoryPath)) {
				// フォルダがないので生成
				LOGGER.debug("フォルダ生成: " + directoryPath);
				Files.createDirectories(directoryPath);
			}

			// 相対ファイルパス
			String filePath = folderPath + PATH_SPLITTER + form.getAnswerFileName();
			// 絶対ファイルパス
			String absoluteFilePath = absoluteFolderPath + PATH_SPLITTER + form.getAnswerFileName();

			// ファイルパスはrootを除いた相対パスを設定
			form.setAnswerFilePath(filePath);

			// O_回答ファイル更新
			LOGGER.trace("O_回答ファイル更新 開始");
			if (answerFileJdbc.updateFilePath(answerFileId, filePath) != 1) {
				LOGGER.warn("ファイルパス更新件数が不正");
				throw new RuntimeException("ファイルパス更新件数が不正");
			}
			// ファイル更新時はO_回答ファイル履歴登録
			if (!insertFlag) {
				answerFileHistoryJdbc.insert(answerFileId, form.getAnswerId(), ANSWER_FILE_HISTORY_UPDATE, userId);
			}
			LOGGER.trace("O_回答ファイル更新 終了");

			// ファイル出力
			LOGGER.trace("ファイル出力 開始");
			exportFile(form.getUploadFile(), absoluteFilePath);

			LOGGER.trace("ファイル出力 終了");
		} catch (Exception ex) {
			// RuntimeExceptionで投げないとロールバックされない
			LOGGER.error("回答ファイルアップロードで例外発生", ex);
			throw new RuntimeException(ex);
		} finally {
			LOGGER.debug("回答ファイルアップロード 終了");
		}
	}

	/**
	 * 回答ファイル（引用）アップロード
	 * 
	 * @param form   パラメータ
	 * @param userId 更新ユーザID
	 */
	public void uploadQuoteFile(QuoteFileForm form, String userId) {
		LOGGER.debug("回答ファイル（引用）アップロード　開始");
		try {
			// ファイルパスは「/answer/<回答ID>/<回答ファイルID>/<timestamp>/<アップロードファイル名>」
			SimpleDateFormat sdf = new SimpleDateFormat(answerFolderNameFormat);
			String nowTime = sdf.format(new Date());

			Integer answerFileId = form.getAnswerFileId();
			String applicationFilePath = form.getFilePath();
			boolean insertFlag = false;
			if (answerFileId == null) {
				// ■ 新規登録
				// O_回答ファイル登録
				LOGGER.trace("O_回答ファイル登録 開始");
				insertFlag = true;
				answerFileId = quoteFileJdbc.insert(form);
				// O_回答ファイル履歴登録
				answerFileHistoryJdbc.insert(answerFileId, form.getAnswerId(), ANSWER_FILE_HISTORY_ADD, userId);
				form.setAnswerFileId(answerFileId);
				LOGGER.trace("O_回答ファイル登録 終了");
			}

			// 相対フォルダパス
			// 「/answer/<申請ID>/<申請段階ID>/<回答ID>/<回答ファイルID>/<timestamp>/<アップロードファイル名>」
			// 相対フォルダパス(回答ファイルIDまで)
			String folderPath = answerFolderName;
			folderPath += PATH_SPLITTER + form.getApplicationId();
			folderPath += PATH_SPLITTER + form.getApplicationStepId();
			folderPath += PATH_SPLITTER + form.getAnswerId();
			folderPath += PATH_SPLITTER + form.getAnswerFileId();
			folderPath += PATH_SPLITTER + nowTime;

			// 絶対フォルダパス
			String absoluteFolderPath = fileRootPath + folderPath;
			Path directoryPath = Paths.get(absoluteFolderPath);
			if (!Files.exists(directoryPath)) {
				// フォルダがないので生成
				LOGGER.debug("フォルダ生成: " + directoryPath);
				Files.createDirectories(directoryPath);
			}

			// 相対ファイルパス
			String filePath = folderPath + PATH_SPLITTER + form.getAnswerFileName();
			// 絶対ファイルパス
			String absoluteFilePath = absoluteFolderPath + PATH_SPLITTER + form.getAnswerFileName();

			// ファイルパスはrootを除いた相対パスを設定
			form.setFilePath(filePath);

			// O_回答ファイル更新
			LOGGER.trace("O_回答ファイル更新 開始");
			if (answerFileJdbc.updateFilePath(answerFileId, filePath) != 1) {
				LOGGER.warn("ファイルパス更新件数が不正");
				throw new RuntimeException("ファイルパス更新件数が不正");
			}
			// ファイル更新時はO_回答ファイル履歴登録
			if (!insertFlag) {
				answerFileHistoryJdbc.insert(answerFileId, form.getAnswerId(), ANSWER_FILE_HISTORY_UPDATE, userId);
			}
			LOGGER.trace("O_回答ファイル更新 終了");

			// ファイル複製
			LOGGER.debug("ファイル出力 開始");
			// ToDo： 引用した申請ファイルを回答ファイルとして登録
			LOGGER.debug(fileRootPath + applicationFilePath);
			LOGGER.debug(absoluteFilePath);

			// ファイルの複製
			Path applicationFile = Paths.get(fileRootPath + applicationFilePath);
			Path answerFile = Paths.get(absoluteFilePath);
			Files.copy(applicationFile, answerFile);

			LOGGER.debug("ファイル出力 終了");
		} catch (Exception ex) {
			LOGGER.debug("回答ファイル（引用）アップロードで例外発生", ex);
			throw new RuntimeException(ex);
		} finally {
			LOGGER.debug("回答ファイル（引用）アップロード　終了");
		}
	}

	/**
	 * 回答ファイルダウンロードパラメータ確認
	 * 
	 * @param form パラメータ
	 * @return 確認結果
	 */
	public boolean validateDownloadAnswerFile(AnswerFileForm form) {
		LOGGER.debug("回答ファイルダウンロードパラメータ確認 開始");
		try {
			Integer answerId = form.getAnswerId();
			Integer answerFileId = form.getAnswerFileId();
			Integer applicationId = form.getApplicationId();
			Integer applicationStepId = form.getApplicationStepId();
			String answerdDpartmentId = form.getDepartmentId();

			// 回答データ存在チェック
			if (APPLICATION_STEP_ID_1.equals(applicationStepId)) {
				if (answerId == null) {
					// パラメータ不足
					LOGGER.warn("パラメータ不足：回答ID");
					return false;
				}
				// 回答ID
				if (answerRepository.findByAnswerId(answerId).size() != 1) {
					LOGGER.warn("回答データ取得件数不正");
					return false;
				}
			}

			if (APPLICATION_STEP_ID_2.equals(applicationStepId)) {

				if (answerdDpartmentId == null || EMPTY.equals(answerdDpartmentId)) {
					// パラメータ不足
					LOGGER.warn("パラメータ不足:回答ファイルの部署ID");
					return false;
				}

				// 部署回答の件数チェック
				if (departmentAnswerRepository.getDepartmentAnswer(applicationId, answerdDpartmentId).size() != 1) {
					LOGGER.warn("回答データ取得件数不正");
					return false;
				}
			}

			if (APPLICATION_STEP_ID_3.equals(applicationStepId)) {

				// 回答の件数チェック
				if (answerRepository.findByApplicationStepId(applicationId, applicationStepId).size() < 1) {
					LOGGER.warn("回答データ取得件数不正");
					return false;
				}
			}

			// 回答ファイルID
			if (answerFileRepository.findByAnswerFileId(answerFileId).size() != 1) {
				LOGGER.warn("回答ファイルの件数が不正");
				return false;
			}
			return true;
		} finally {
			LOGGER.debug("回答ファイルダウンロードパラメータ確認 終了");
		}
	}

	/**
	 * 回答ファイルダウンロード
	 * 
	 * @param form パラメータ
	 * @return 応答Entity
	 */
	public ResponseEntity<Resource> downloadAnswerFile(AnswerFileForm form, String role) {
		LOGGER.debug("回答ファイルダウンロード 開始");
		try {
			LOGGER.trace("回答ファイルデータ取得 開始: " + form.getAnswerFileId());
			List<AnswerFile> answerFileList = answerFileRepository.findByAnswerFileId(form.getAnswerFileId());
			AnswerFile answerFile = answerFileList.get(0);
			LOGGER.trace("回答ファイルデータ取得 終了:" + form.getAnswerFileId());

			Path filePath = null;

			if (AuthUtil.ROLE_GOVERMENT.equals(role)) {
				// ■ 行政
				if (answerFile.getDeleteUnnotifiedFlag()) {
					LOGGER.warn("削除済みファイルの取得要求(行政)");
					return new ResponseEntity<Resource>(HttpStatus.NOT_FOUND);
				}

				// 絶対ファイルパス
				String absoluteFilePath = fileRootPath + answerFile.getFilePath();
				filePath = Paths.get(absoluteFilePath);
			} else if (AuthUtil.ROLE_BUSINESS.equals(role)) {
				// ■ 事業者

				// 絶対ファイルパス
				String absoluteFilePath = fileRootPath + answerFile.getNotifiedFilePath();
				filePath = Paths.get(absoluteFilePath);
			} else {
				LOGGER.error("未知のロール: " + role);
				return new ResponseEntity<Resource>(HttpStatus.SERVICE_UNAVAILABLE);
			}

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
			LOGGER.error("回答ファイルダウンロードで例外発生", ex);
			return new ResponseEntity<Resource>(HttpStatus.SERVICE_UNAVAILABLE);
		} finally {
			LOGGER.debug("回答ファイルダウンロード 終了");
		}
	}

	/**
	 * 回答ファイル削除
	 * 
	 * @param answerFileForm パラメータ
	 * @return 削除結果
	 */
	public void deleteAnswerFile(AnswerFileForm form, String userId) {
		LOGGER.debug("回答ファイル削除 開始: " + form.getAnswerFileId());
		try {
			// O_回答ファイル更新
			LOGGER.trace("O_回答ファイル削除 開始");
			if (answerFileJdbc.setDeleteFlag(form.getAnswerFileId()) != 1) {
				LOGGER.warn("ファイル削除件数が不正");
				throw new RuntimeException("ファイル削除件数が不正");
			}
			// O_回答ファイル履歴登録
			answerFileHistoryJdbc.insert(form.getAnswerFileId(), form.getAnswerId(), ANSWER_FILE_HISTORY_DELETE,
					userId);
			LOGGER.trace("O_回答ファイル削除 終了");
		} finally {
			LOGGER.debug("回答ファイル削除 終了: " + form.getAnswerFileId());
		}
	}

	/**
	 * 回答IDから回答履歴を取得する
	 * 
	 * @param answerId 回答ID
	 * @return 回答履歴Form
	 */
	public List<AnswerHistoryForm> getAnswerHistoryFromAnswerId(int answerId) {
		LOGGER.debug("回答IDから回答履歴取得 開始. 回答ID=" + answerId);
		try {
			List<AnswerHistory> entity = answerHistoryRepository.getAnswerHistoryByAnswerId(answerId);
			return setAnswerHistoryFormFromEntity(entity);
		} finally {
			LOGGER.debug("回答IDから回答履歴取得 終了");
		}
	}

	/**
	 * 回答IDから事業者向け回答履歴を取得する
	 * 
	 * @param answerId 回答ID
	 * @return 回答履歴Form
	 */
	public List<AnswerHistoryForm> getAnswerHistoryFromAnswerIdForBusiness(int answerId) {
		LOGGER.debug("回答IDから事業者向け回答履歴取得 開始. 回答ID=" + answerId);
		try {
			List<AnswerHistory> entity = answerHistoryRepository.getAnswerHistoryByAnswerIdForBusiness(answerId);
			return setAnswerHistoryFormFromEntity(entity);
		} finally {
			LOGGER.debug("回答IDから事業者向け回答履歴取得 終了");
		}
	}

	/**
	 * 申請IDから回答履歴を取得する
	 * 
	 * @param applicationId 申請ID
	 * @return 回答履歴Form
	 */
	public List<AnswerHistoryForm> getAnswerHistoryFromApplicationId(int applicationId) {
		LOGGER.debug("申請IDから回答履歴取得 開始. 申請ID=" + applicationId);
		try {
			List<AnswerHistory> entity = answerHistoryRepository.getAnswerHistoryByApplicationId(applicationId);
			return setAnswerHistoryFormFromEntity(entity);
		} finally {
			LOGGER.debug("申請IDから回答履歴取得 終了");
		}
	}

	/**
	 * 回答履歴を取得する
	 * 
	 * @param applicationId     申請ID(必須)
	 * @param applicationTypeId 申請種類ID(必須)
	 * @param applicationStepId 申請段階ID(必須)
	 * @param isGoverment       行政かどうか(必須)
	 * @param answerId          回答ID(任意、検索条件としない場合、 「0」で渡す)
	 * @param DepartmentAnswer  部署回答ID(任意、検索条件としない場合、 「0」で渡す)
	 * @return 回答履歴Form
	 */
	public List<AnswerHistoryForm> getAnswerHistory(int applicationId, int applicationTypeId, int applicationStepId,
			boolean isGoverment, int answerId, int departmentAnswer) {
		LOGGER.debug("回答履歴取得 開始");
		try {
			AnswerDao answerDao = new AnswerDao(emf);
			List<AnswerHistory> entity = answerDao.getAnswerHistoryList(applicationId, applicationStepId, answerId,
					departmentAnswer, isGoverment);

			return setAnswerHistoryFormFromEntity(entity, applicationTypeId, applicationStepId);
		} finally {
			LOGGER.debug("回答履歴取得 終了");
		}
	}

	/**
	 * 回答ファイル更新履歴を取得する
	 * 
	 * @param form 申請詳細フォーム
	 * @return
	 */
	public List<AnswerFileHistoryForm> getAnswerFileHisoryForm(ApplyAnswerForm form) {
		LOGGER.debug("回答ファイル更新履歴取得 開始");
		try {
			List<AnswerFileHistoryForm> answerFileHistoryFormList = new ArrayList<AnswerFileHistoryForm>();
			AnswerDao answerDao = new AnswerDao(emf);
			List<AnswerFileHistoryView> answerFileHistoryList = answerDao
					.getAnswerFileHistoryList(form.getApplicationId());
			DateTimeFormatter datetimeformatter = DateTimeFormatter.ofPattern("yyyy/MM/dd/ HH:mm");
			final Map<String, String> answerFileHistoryUpdateTypeMap = getAnswerFileHistoryUpdateTypeMap();
			for (AnswerFileHistoryView answerFileHistory : answerFileHistoryList) {
				final AnswerFileHistoryForm answerFileHistoryForm = new AnswerFileHistoryForm();
				answerFileHistoryForm.setNotifiedFlag(answerFileHistory.getNotifyFlag());
				String datetimeformated = datetimeformatter.format(answerFileHistory.getUpdateDatetime());
				answerFileHistoryForm.setUpdateDatetime(datetimeformated);
				if (answerFileHistoryUpdateTypeMap != null) {
					answerFileHistoryForm
							.setUpdateType(answerFileHistoryUpdateTypeMap.get(answerFileHistory.getUpdateType() + ""));
				} else {
					answerFileHistoryForm.setUpdateType("");
				}

				answerFileHistoryForm.setJudgementResult(answerFileHistory.getJudgementResult());
				answerFileHistoryForm.setUpdateUserId(answerFileHistory.getUpdateUserId());
				answerFileHistoryForm.setUpdateUserName(answerFileHistory.getUserName());
				answerFileHistoryForm.setFileName(answerFileHistory.getAnswerFileName());
				answerFileHistoryForm.setDepartmentId(answerFileHistory.getDepartmentId());
				answerFileHistoryForm.setDepartmentName(answerFileHistory.getDepartmentName());
				answerFileHistoryFormList.add(answerFileHistoryForm);
			}
			return answerFileHistoryFormList;
		} finally {
			LOGGER.debug("回答ファイル更新履歴取得 終了");
		}
	}

	/**
	 * 回答ファイル履歴更新タイプ定義を取得
	 * 
	 * @return ステータス定義
	 * @throws Exception 例外
	 */
	protected Map<String, String> getAnswerFileHistoryUpdateTypeMap() {
		LOGGER.trace("回答ファイル履歴更新タイプ定義取得 開始");
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			Map<String, String> map = objectMapper.readValue(answerFileHistoryUpdateTypeJson,
					new TypeReference<LinkedHashMap<String, String>>() {
					});
			return map;
		} catch (Exception e) {
			LOGGER.error("回答ファイル履歴更新タイプ定義取得に失敗", e);
			return null;
		} finally {
			LOGGER.trace("回答ファイル履歴更新タイプ定義取得 終了");
		}
	}

	/**
	 * 回答履歴Entityから回答履歴Form を生成する
	 * 
	 * @param entity 回答履歴Entity
	 * @return 回答履歴Form
	 */
	private List<AnswerHistoryForm> setAnswerHistoryFormFromEntity(List<AnswerHistory> entity) {
		LOGGER.debug("回答履歴関連情報セット 開始");
		final List<AnswerHistoryForm> answerHistoryFormList = new ArrayList<AnswerHistoryForm>();
		try {
			DateTimeFormatter datetimeformatter = DateTimeFormatter.ofPattern("yyyy/MM/dd/ HH:mm");
			for (AnswerHistory aEntity : entity) {
				final AnswerHistoryForm aForm = new AnswerHistoryForm();
				aForm.setAnswerHistoryId(aEntity.getAnswerHistoryId());
				aForm.setAnswerId(aEntity.getAnswerId());
				aForm.setNotifiedFlag(aEntity.getNotifyFlag());
				aForm.setAnswerContent(aEntity.getAnswerText());

				String datetimeformated = datetimeformatter.format(aEntity.getAnswerDatetime());
				aForm.setUpdateDatetime(datetimeformated);

				LOGGER.trace("回答送信ユーザ情報取得 開始");
				String userId = aEntity.getAnswerUserId();
				LOGGER.trace("ユーザID: " + userId);
				List<GovernmentUser> governmentUserList = governmentUserRepository.findByUserId(userId);
				if (governmentUserList.size() > 0) {
					aForm.setAnswererUser(getGovernmentUserFormFromEntity(governmentUserList.get(0)));
				}
				LOGGER.trace("回答送信ユーザ情報取得 終了");
				LOGGER.trace("回答情報取得 開始");
				List<Answer> answerList = answerRepository.findByAnswerId(aEntity.getAnswerId());
				if (answerList.size() > 0) {
					aForm.setJudgementResult(answerList.get(0).getJudgementResult());
				}
				LOGGER.trace("回答情報取得 開始");

				answerHistoryFormList.add(aForm);
			}
			return answerHistoryFormList;
		} finally {
			LOGGER.debug("回答履歴関連情報セット 終了");
		}

	}

	/**
	 * 回答履歴Entityから回答履歴Form を生成する
	 * 
	 * @param entity 回答履歴Entity
	 * @return 回答履歴Form
	 */
	private List<AnswerHistoryForm> setAnswerHistoryFormFromEntity(List<AnswerHistory> entity, int applicationTypeId,
			int applicationStepId) {
		LOGGER.debug("回答履歴関連情報セット 開始");
		final List<AnswerHistoryForm> answerHistoryFormList = new ArrayList<AnswerHistoryForm>();
		try {
			DateTimeFormatter datetimeformatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
			DateTimeFormatter dateformatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
			for (AnswerHistory aEntity : entity) {
				final AnswerHistoryForm aForm = new AnswerHistoryForm();
				aForm.setAnswerHistoryId(aEntity.getAnswerHistoryId());
				aForm.setAnswerId(aEntity.getAnswerId());
				aForm.setNotifiedFlag(aEntity.getNotifyFlag());
				aForm.setAnswerContent(aEntity.getAnswerText());

				String datetimeformated = datetimeformatter.format(aEntity.getAnswerDatetime());
				aForm.setUpdateDatetime(datetimeformated);

				LOGGER.trace("回答送信ユーザ情報取得 開始");
				String userId = aEntity.getAnswerUserId();
				LOGGER.trace("ユーザID: " + userId);
				List<GovernmentUser> governmentUserList = governmentUserRepository.findByUserId(userId);
				if (governmentUserList.size() > 0) {
					aForm.setAnswererUser(getGovernmentUserFormFromEntity(governmentUserList.get(0)));
				}
				LOGGER.trace("回答送信ユーザ情報取得 終了");
				LOGGER.trace("回答情報取得 開始");
				List<Answer> answerList = answerRepository.findByAnswerId(aEntity.getAnswerId());
				if (answerList.size() > 0) {
					Answer answer = answerList.get(0);
					aForm.setJudgementResult(answer.getJudgementResult());
					if (ANSWER_DATA_TYPE_GOVERNMENT_ADD.equals(answer.getAnswerDataType())) {
						aForm.setTitle(govermentAddAnswerTitle);
					} else {
						AnswerDao dao = new AnswerDao(emf);
						List<CategoryJudgementResult> categoryJudgementResultList = dao.getJudgementResultList(
								answer.getJudgementId(), applicationTypeId, applicationStepId,
								answer.getDepartmentId());
						if (categoryJudgementResultList.size() > 0) {
							aForm.setTitle(categoryJudgementResultList.get(0).getTitle());
						}
					}

				}
				LOGGER.trace("回答情報取得 開始");
				aForm.setReApplicationFlag(aEntity.getReApplicationFlag());
				aForm.setDiscussionFlag(aEntity.getDiscussionFlag());
				aForm.setDiscussionItem(aEntity.getDiscussionItem());
				if (APPLICATION_STEP_ID_2.equals(applicationStepId)) {
					LOGGER.trace("M_帳票一覧取得 開始");
					List<LedgerMaster> ledgerMasterList = ledgerMasterRepository
							.getLedgerMasterListForDisplay(applicationStepId);
					List<LedgerMasterForm> ledgerMasterFormList = new ArrayList<LedgerMasterForm>();
					for (LedgerMaster ledgerMaster : ledgerMasterList) {
						ledgerMasterFormList
								.add(geledgerMasterFormFromEntity(ledgerMaster, aEntity.getDiscussionItem()));
					}
					aForm.setDiscussionItems(ledgerMasterFormList);
					LOGGER.trace("M_帳票一覧取得 終了");
				}

				aForm.setBusinessPassStatus(aEntity.getBusinessPassStatus());
				aForm.setBusinessPassComment(aEntity.getBusinessPassComment());
				aForm.setGovernmentConfirmStatus(aEntity.getGovernmentConfirmStatus());
				if (aEntity.getGovernmentConfirmDatetime() != null) {
					String dateformated = dateformatter.format(aEntity.getGovernmentConfirmDatetime());
					aForm.setGovernmentConfirmDatetime(dateformated);
				}
				aForm.setGovernmentConfirmComment(aEntity.getGovernmentConfirmComment());
				aForm.setAnswerStatus(aEntity.getAnswerStatus());
				aForm.setAnswerDataType(aEntity.getAnswerDataType());

				answerHistoryFormList.add(aForm);
			}
			return answerHistoryFormList;
		} finally {
			LOGGER.debug("回答履歴関連情報セット 終了");
		}

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
	 * 行政から回答一覧をもとに、レポート帳票を生成する
	 * 
	 * @param applicationId 申請ID
	 * @param applicationStepId 申請段階ID
	 * @param response      レスポンス
	 * @return 異常であるか
	 */
	public boolean exportAnswerReportWorkBook(Integer applicationId, Integer applicationStepId,HttpServletResponse response) {
		LOGGER.debug("行政回答レポート帳票生成 開始");
		try {

			// 回答ID-回答内容の紐づけ情報を作成
			ApplicationDao dao = new ApplicationDao(emf);
			final Map<Integer, String> answerNotifiedTextMap = new HashMap<Integer, String>();
			//パラメータの申請ID、申請段階IDよりO_回答を取得
			List<Answer> answerList = answerRepository.findByApplicationIdAndapplicationStepId(applicationId, applicationStepId);
			for (Answer answer : answerList) {
				answerNotifiedTextMap.put(answer.getAnswerId(), answer.getNotifiedText());
			}
			
			GeneralConditionDiagnosisReportRequestForm generalConditionDiagnosisReportRequestForm = new GeneralConditionDiagnosisReportRequestForm();
			//申請段階が事前協議の場合
			if(applicationStepId.equals(APPLICATION_STEP_ID_2)) {
				generalConditionDiagnosisReportRequestForm = addAnswer(answerNotifiedTextMap,applicationStepId);		
			}
			// 申請情報を登録するときに、作成された概況診断レポートを取得する
			List<ApplicationFile> applicationFiles = applicationFileRepository
					.getApplicationFilesSortByVer(applicationReportFileId, applicationId,applicationStepId);
			ApplicationFile applicationFile = applicationFiles.get(0);

			// 絶対ファイルパス
			String absoluteFilePath = fileRootPath + applicationFile.getFilePath();
			Path tempFilePath = Paths.get(absoluteFilePath);
			if (!Files.exists(tempFilePath)) {
				// ファイルが存在しない
				LOGGER.warn("ファイルが存在しない");
				return false;
			}

			// 回答レポートを作成
			Workbook wb = exportAnswerReportWorkBook(absoluteFilePath, answerNotifiedTextMap,applicationStepId,generalConditionDiagnosisReportRequestForm);

			if (wb != null) {
				try (OutputStream os = response.getOutputStream()) {
					// ファイルサイズ測定
					int fileSize = -1;
					try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
						wb.write(byteArrayOutputStream);
						fileSize = byteArrayOutputStream.size();
					}

					// 帳票ダウンロード出力
					response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
					response.setHeader("Content-Disposition", "attachment; filename=" + answerReportFileName);
					response.setContentLength(fileSize);
					wb.write(os);
					os.flush();
				}
			} else {
				return false;
			}

			return true;
		} catch (Exception ex) {
			LOGGER.error("行政回答レポート帳票生成で例外発生", ex);
			return false;
		} finally {
			LOGGER.debug("行政回答レポート帳票生成 終了");
		}
	}

	/**
	 * 帳票ファイルダウンロードパラメータ確認
	 * 
	 * @param form パラメータ
	 * @return 確認結果
	 */
	public boolean validateDownloadLedgerFile(LedgerForm form) {
		LOGGER.debug("帳票ファイルダウンロードパラメータ確認 開始");
		try {
			Integer fileId = form.getFileId();
			String ledgerId = form.getLedgerId();
			// ファイルID
			if (ledgerRepository.findByFileId(fileId).size() != 1) {
				LOGGER.warn("帳票ファイルの件数が不正");
				return false;
			}

			// 帳票マスタID
			if (ledgerMasterRepository.getLedgerMasterByLedgerId(ledgerId).size() != 1) {
				LOGGER.warn("帳票ファイルの件数が不正");
				return false;
			}
			return true;
		} finally {
			LOGGER.debug("回答ファイルダウンロードパラメータ確認 終了");
		}
	}

	/**
	 * 帳票ファイルダウンロード
	 * 
	 * @param form パラメータ
	 * @return 応答Entity
	 */
	public ResponseEntity<Resource> downloadLedgeFile(LedgerForm form, String role) {
		LOGGER.debug("帳票ファイルダウンロード 開始");
		try {
			Path filePath = null;
			// 絶対ファイルパス
			String absoluteFilePath = fileRootPath + form.getFilePath();
			if (AuthUtil.ROLE_BUSINESS.equals(role)) {
				absoluteFilePath = fileRootPath + form.getNotifyFilePath();
			}
			filePath = Paths.get(absoluteFilePath);

			if (!Files.exists(filePath)) {
				// ファイルが存在しない
				LOGGER.warn("ファイルが存在しない: " + filePath);
				return new ResponseEntity<Resource>(HttpStatus.NOT_FOUND);
			}

			// 該当帳票が通知必須の場合、受領日時を更新
			List<LedgerMaster> ledgerMasterList = ledgerMasterRepository.getLedgerMasterByLedgerId(form.getLedgerId());
			if (ledgerMasterList.get(0).getNotificationFlag() != null
					&& ledgerMasterList.get(0).getNotificationFlag()) {

				// 事業者ダウンロードの場合、受領日時を更新
				if (AuthUtil.ROLE_BUSINESS.equals(role)) {
					// 初めてダウンロードする場合、更新を行う
					if (form.getReceiptDatetime() == null || EMPTY.equals(form.getReceiptDatetime())) {
						if (ledgerJdbc.updateReceiptDatetime(form.getFileId()) != 1) {
							LOGGER.warn("O_帳票の更新件数不正");
							throw new RuntimeException("帳票受領日時の更新に失敗");
						}

						//行政管理者へデータ取得が完了した旨をメール通知する
						sendLedgerReceiptNotificationMailToGovernmentUser(form);
					}
				}
			}
			Resource resource = new PathResource(filePath);
			return ResponseEntity.ok().contentType(getContentType(filePath))
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
					.body(resource);
		} catch (Exception ex) {
			LOGGER.error("帳票ファイルダウンロードで例外発生", ex);
			return new ResponseEntity<Resource>(HttpStatus.SERVICE_UNAVAILABLE);
		} finally {
			LOGGER.debug("帳票ファイルダウンロード 終了");
		}
	}

	/**
	 * 行政に帳票受領通知送付
	 * 
	 * @param ledgerForm 帳票ファイルフォーム
	 */
	private void sendLedgerReceiptNotificationMailToGovernmentUser(LedgerForm ledgerForm) {

		LOGGER.debug("帳票受領通知 開始");

		Integer applicationId = ledgerForm.getApplicationId();
		Integer applicationStepId = ledgerForm.getApplicationStepId();
		
		// メールメッセージアイテム
		MailItem item = new MailItem();

		// 帳票名
		item.setLedgerName(ledgerForm.getLedgerName());

		// 申請者情報
		LOGGER.trace("申請者情報取得 開始");
		List<ApplicantInformation> applicantList = applicantInformationRepository.getApplicantList(applicationId,
				CONTACT_ADDRESS_INVALID);
		if (applicantList.size() < 1) {
			LOGGER.error("申請者データの件数が不正");
			throw new RuntimeException("申請者データの件数が不正");
		}
		ApplicantInformation applicant = applicantList.get(0);
		LOGGER.trace("申請者情報取得 終了");

		switch (applicantNameItemNumber) { // 氏名はプロパティで設定されたアイテムを設定
		case 1:
			item.setName(applicant.getItem1());
			break;
		case 2:
			item.setName(applicant.getItem2());
			break;
		case 3:
			item.setName(applicant.getItem3());
			break;
		case 4:
			item.setName(applicant.getItem4());
			break;
		case 5:
			item.setName(applicant.getItem5());
			break;
		case 6:
			item.setName(applicant.getItem6());
			break;
		case 7:
			item.setName(applicant.getItem7());
			break;
		case 8:
			item.setName(applicant.getItem8());
			break;
		case 9:
			item.setName(applicant.getItem9());
			break;
		case 10:
			item.setName(applicant.getItem10());
			break;
		default:
			LOGGER.error("氏名アイテム番号指定が不正: " + applicantNameItemNumber);
			throw new RuntimeException("氏名アイテム番号指定が不正");
		}
		item.setMailAddress(applicant.getMailAddress()); // 申請者メールアドレス
		
		// 地番
		LOGGER.trace("地番情報取得 開始");
		ApplicationDao applicationDao = new ApplicationDao(emf);
		List<ApplyLotNumber> lotNumbersList = applicationDao.getApplyingLotNumberList(applicationId,
				lonlatEpsg);
		String addressText = (lotNumbersList != null && lotNumbersList.size() > 0)
				? lotNumbersList.get(0).getLotNumbers()
				: "";
		item.setLotNumber(addressText);
		LOGGER.trace("地番情報取得 終了");
		
		// メール送信
		// 行政に帳票受領通知送付
		String subject = getMailPropValue(MailMessageUtil.KEY_LEDGER_RECEIPT_NOTIFICATION_SUBJECT, item);
		String body = getMailPropValue(MailMessageUtil.KEY_LEDGER_RECEIPT_NOTIFICATION_BODY, item);

		// 回答担当部署IDを取得する
		List<String> departmentIdList = new ArrayList<String>();
		if(APPLICATION_STEP_ID_2.equals(applicationStepId)) {
			// 事前協議の場合
			// O_回答から回答担当部署一覧を取得
			departmentIdList = answerRepository.getDepartmentIdList(applicationId,applicationStepId);
		}else if(APPLICATION_STEP_ID_3.equals(applicationStepId)) {
			// 許可判定の場合
			// M_権限から回答担当部署一覧を取得する
			List<Authority> authorityList = authorityRepository.findStep3AnswerDepartment();
			for(Authority authority : authorityList) {
				departmentIdList.add(authority.getDepartmentId());
			}			
		}
		
		// 回答担当部署の管理者メールアドレスをメール送付
		for(String departmentId : departmentIdList) {
			// 回答担当部署情報取得
			List<Department> departmentList = departmentRepository.getDepartmentListById(departmentId);
			if (departmentList.size() != 1) {
				LOGGER.error("部署情報の件数が不正");
				throw new RuntimeException("部署情報の件数が不正");
			}
			Department department = departmentList.get(0);
			
			LOGGER.trace(department.getMailAddress());
			LOGGER.trace(subject);
			LOGGER.trace(body);
			
			try {
				final String[] mailAddressList = department.getMailAddress().split(",");
				for (String aMailAddress : mailAddressList) {
					LOGGER.trace("メール通知 開始");
					mailSendutil.sendMail(aMailAddress, subject, body);
					LOGGER.trace("メール通知 終了");
				}
			} catch (Exception e) {
				LOGGER.error("メール送信時にエラー発生", e);
				throw new RuntimeException(e);
			}	
		}
		
		LOGGER.debug("帳票受領通知 終了");
	}
	
	/**
	 * 回答一覧取得
	 * 
	 * @param applicationId      申請ID
	 * @param applicationStepId  申請段階ID
	 * @param answerId           回答ID
	 * @param departmentAnswerId 部署回答ID
	 * @param isGoverment        行政かどうか
	 */
	public List<Answer> getAnswerMessage(Integer applicationId, Integer applicationStepId, Integer answerId, Integer departmentAnswerId,
			boolean isGoverment) {

		if (isGoverment) {
			LOGGER.debug("行政向け回答一覧取得 開始");
		} else {
			LOGGER.debug("事業者向け回答一覧取得 開始");
		}
		LOGGER.trace("申請ID： " + applicationId);
		LOGGER.trace("申請段階ID： " + applicationStepId);
		LOGGER.trace("回答ID： " + answerId);
		LOGGER.trace("部署回答ID： " + departmentAnswerId);
		try {
			ApplicationDao applicationDao = new ApplicationDao(emf);
			List<Answer> answerList = new ArrayList<>();
			if (isGoverment) {
				answerList = applicationDao.getAnswerList(applicationId, isGoverment, applicationStepId, departmentAnswerId);
			}else {
				answerList = applicationDao.getAllAnswerListForBusiness(applicationId, applicationStepId, departmentAnswerId);
			}
			return answerList;
		} finally {
			LOGGER.debug("事業者向け回答一覧取得 終了");
		}
	}

	
	/**
	 * 通知用未回答一覧取得、部署通知&回答済み未通知一覧取得
	 * 
	 */
	public Map<String,Map<Integer,Map<Integer,List<Integer>>>> notResponseAnswer(Integer notResponse) {

		LOGGER.trace("通知用未回答一覧取得開始");
		ChatDao chatDao = new ChatDao(emf);
		AnswerDao answerDao = new AnswerDao(emf);
		try {
			List<Answer> answerList = new ArrayList<>();
			List<Answer> answerListExpire = new ArrayList<>();
			if(notResponse.equals(1)) {
				//現在日時より未回答のリマインドを送る回答を取得
				//回答期日が超過しているものの回答リスト
				answerList = answerRepository.findNotResponseAnswerList();
				//もう少しで回答期日が来るもの(期間はプロパティファイルから取得)
				answerListExpire = answerRepository.findNotResponseAnswerExpireList(appAnswerDeadlineXDaysAgo);
			}else if(notResponse.equals(2)) {
				//現在日時より未通知のリマインドを送る回答を取得
				//期日が超過しているものの回答リスト
				answerList = answerRepository.findNotNotifiedAnswerList();
				//もう少しで期日が来るもの(期間はプロパティファイルから取得)
				answerListExpire = answerRepository.findNotNotifiedAnswerExpireList(appAnswerBufferDays,appAnswerDeadlineXDaysAgo);
			}
			Map<String,List<Integer>> departmentAndAnswerIdMap = new HashMap<>();
			for(Answer answer:answerList) {
				//申請段階IDを取得
				Integer stepId = answer.getApplicationStepId();
				List<Department> departmentIdList = new ArrayList<Department>();
				if (stepId.equals(APPLICATION_STEP_ID_1)||stepId.equals(APPLICATION_STEP_ID_3)) {
					// 事前相談,許可判定: 判定項目IDでM_区分判定_権限より、M_部署の部署ID
					departmentIdList = answerDao.getDepartmentListByJudgementId(Integer.parseInt(answer.getJudgementId()));
				}else if (stepId.equals(APPLICATION_STEP_ID_2)) {
					//部署回答ID でO_部署回答とJOINし、そちらの部署IDを使用
					departmentIdList = chatDao.getDepartmentListByDepartmentAnswerId(answer.getDepartmentAnswerId());
				}
				//取得した部署のリストで<部署,回答IDリスト>のマップを作成する
				for(Department department : departmentIdList) {
					//マップに部署が含まれていなければ追加
					if(!(departmentAndAnswerIdMap.containsKey(department.getDepartmentId()))) {
						List<Integer> answerId = new ArrayList<>();
						answerId.add(answer.getAnswerId());
						departmentAndAnswerIdMap.put(department.getDepartmentId(), answerId);
					}else {
						//マップに既にキー(部署ID)があれば、値を取得し回答IDを追加し戻す
						List<Integer> answerId = departmentAndAnswerIdMap.get(department.getDepartmentId());
						answerId.add(answer.getAnswerId());
						departmentAndAnswerIdMap.put(department.getDepartmentId(), answerId);
					}
				}
			}
			Map<String,List<Integer>> departmentAndAnswerIdMapExpire = new HashMap<>();
			for(Answer answer:answerListExpire) {
				//申請段階IDを取得
				Integer stepId = answer.getApplicationStepId();
				List<Department> departmentIdList = new ArrayList<Department>();
				if (stepId.equals(APPLICATION_STEP_ID_1)||stepId.equals(APPLICATION_STEP_ID_3)) {
					// 事前相談,許可判定: 判定項目IDでM_区分判定_権限より、M_部署の部署ID
					departmentIdList = answerDao.getDepartmentListByJudgementId(Integer.parseInt(answer.getJudgementId()));
				}else if (stepId.equals(APPLICATION_STEP_ID_2)) {
					//部署回答ID でO_部署回答とJOINし、そちらの部署IDを使用
					departmentIdList = chatDao.getDepartmentListByDepartmentAnswerId(answer.getDepartmentAnswerId());
				}
				//取得した部署のリストで<部署,回答IDリスト>のマップを作成する
				for(Department department : departmentIdList) {
					//マップに部署が含まれていなければ追加
					if(!(departmentAndAnswerIdMapExpire.containsKey(department.getDepartmentId()))) {
						List<Integer> answerId = new ArrayList<>();
						answerId.add(answer.getAnswerId());
						departmentAndAnswerIdMapExpire.put(department.getDepartmentId(), answerId);
					}else {
						//マップに既にキー(部署ID)があれば、値を取得し回答IDを追加し戻す
						List<Integer> answerId = departmentAndAnswerIdMapExpire.get(department.getDepartmentId());
						answerId.add(answer.getAnswerId());
						departmentAndAnswerIdMapExpire.put(department.getDepartmentId(), answerId);
					}
				}
			}
			//<部署ID,<申請段階(1 2 3),<期日(1:期日がもう少し 2:期日超過),<申請ID,件数>>>>
			Map<String,Map<Integer,Map<Integer,List<Integer>>>> responseNotificationMap = new HashMap<>();
			//部署と回答IDが入っているmapのキーリスト作成(期日がもう少し)
			List<String> keysExpire = new ArrayList<String>(departmentAndAnswerIdMapExpire.keySet());
			for(String key:keysExpire) {
				//申請段階それぞれのmapを作成
				Map<Integer,List<Integer>> countAplicationIdExpire1 = new HashMap<>();
				Map<Integer,List<Integer>> countAplicationIdExpire2 = new HashMap<>();
				Map<Integer,List<Integer>> countAplicationIdExpire3 = new HashMap<>();
				Map<Integer,Map<Integer,List<Integer>>> countAplicationIdExpireStep = new HashMap<>();
				
				List<Integer> answerIdList = departmentAndAnswerIdMapExpire.get(key);
				//各申請段階における申請IDと件数を入れるマップを作成
				List<Integer> countAplicationIdStepIdOne = new ArrayList<Integer>();
				List<Integer> countAplicationIdStepIdTwo = new ArrayList<Integer>();
				List<Integer> countAplicationIdStepIdThree = new ArrayList<Integer>();
				//回答IDのリストを回し申請IDを取得
				for(Integer id : answerIdList) {
					for(Answer answerExpire:answerListExpire) {
						if(answerExpire.getAnswerId() != null && answerExpire.getAnswerId().equals(id)) {
							//申請段階でどのListに入れるか分ける
							if(APPLICATION_STEP_ID_1.equals(answerExpire.getApplicationStepId())) {
								if(!(countAplicationIdStepIdOne.contains(answerExpire.getApplicationId()))) {
									//申請段階1のListに申請IDがキーとして入っていなければ追加
									countAplicationIdStepIdOne.add(answerExpire.getApplicationId());
								}
							}else if(APPLICATION_STEP_ID_2.equals(answerExpire.getApplicationStepId())) {
								if(!(countAplicationIdStepIdTwo.contains(answerExpire.getApplicationId()))) {
									//申請段階2のListに申請IDがキーとして入っていなければ追加
									countAplicationIdStepIdTwo.add(answerExpire.getApplicationId());
								}
							}else if(APPLICATION_STEP_ID_3.equals(answerExpire.getApplicationStepId())) {
								if(!(countAplicationIdStepIdThree.contains(answerExpire.getApplicationId()))) {
									//申請段階3のListに申請IDがキーとして入っていなければ追加
									countAplicationIdStepIdThree.add(answerExpire.getApplicationId());
								}
							}
						}
					}
				}
				//それぞれをmapに入れていく
				countAplicationIdExpire1.put(1,countAplicationIdStepIdOne);
				countAplicationIdExpireStep.put(1, countAplicationIdExpire1);
				countAplicationIdExpire2.put(1,countAplicationIdStepIdTwo);
				countAplicationIdExpireStep.put(2, countAplicationIdExpire2);
				countAplicationIdExpire3.put(1,countAplicationIdStepIdThree);
				countAplicationIdExpireStep.put(3, countAplicationIdExpire3);
				responseNotificationMap.put(key, countAplicationIdExpireStep);
			}
			
			//部署と回答IDが入っているmapのキーリスト作成(期日が超過)
			List<String> keys = new ArrayList<String>(departmentAndAnswerIdMap.keySet());
			for(String key:keys) {
				//申請段階それぞれのmapを作成
				Map<Integer,List<Integer>> countAplicationId1 = new HashMap<>();
				Map<Integer,List<Integer>> countAplicationId2 = new HashMap<>();
				Map<Integer,List<Integer>> countAplicationId3 = new HashMap<>();
				Map<Integer,Map<Integer,List<Integer>>> countAplicationIdStep = new HashMap<>();
				
				List<Integer> answerIdList = departmentAndAnswerIdMap.get(key);
				//各申請段階における申請IDと件数を入れるマップを作成
				List<Integer> countAplicationIdStepIdOne = new ArrayList<Integer>();
				List<Integer> countAplicationIdStepIdTwo = new ArrayList<Integer>();
				List<Integer> countAplicationIdStepIdThree = new ArrayList<Integer>();
				for(Integer id : answerIdList) {
					for(Answer answer:answerList) {
						if(answer.getAnswerId() != null && answer.getAnswerId().equals(id)) {
							if(APPLICATION_STEP_ID_1.equals(answer.getApplicationStepId())) {
								if(!(countAplicationIdStepIdOne.contains(answer.getApplicationId()))) {
									countAplicationIdStepIdOne.add(answer.getApplicationId());
								}
							}else if(APPLICATION_STEP_ID_2.equals(answer.getApplicationStepId())) {
								if(!(countAplicationIdStepIdTwo.contains(answer.getApplicationId()))) {
									countAplicationIdStepIdTwo.add(answer.getApplicationId());
								}
							}else if(APPLICATION_STEP_ID_3.equals(answer.getApplicationStepId())) {
								if(!(countAplicationIdStepIdThree.contains(answer.getApplicationId()))) {
									countAplicationIdStepIdThree.add(answer.getApplicationId());
								}
							}
						}
					}
				}
				//期日がもう少しでmapに部署IDがあるかで場合分け
				if(!(responseNotificationMap.containsKey(key))) {
					countAplicationId1.put(2,countAplicationIdStepIdOne);
					countAplicationIdStep.put(1, countAplicationId1);
					countAplicationId2.put(2,countAplicationIdStepIdTwo);
					countAplicationIdStep.put(2, countAplicationId2);
					countAplicationId3.put(2,countAplicationIdStepIdThree);
					countAplicationIdStep.put(3, countAplicationId3);
					responseNotificationMap.put(key, countAplicationIdStep);
				}else {
					Map<Integer,Map<Integer,List<Integer>>> mapGet = responseNotificationMap.get(key);
					Map<Integer,List<Integer>> stepIdget1 = mapGet.get(1);
					stepIdget1.put(2,countAplicationIdStepIdOne);
					Map<Integer,List<Integer>> stepIdget2 = mapGet.get(2);
					stepIdget2.put(2,countAplicationIdStepIdTwo);
					Map<Integer,List<Integer>> stepIdget3 = mapGet.get(3);
					stepIdget3.put(2,countAplicationIdStepIdThree);
					mapGet.put(1,stepIdget1);
					mapGet.put(2,stepIdget2);
					mapGet.put(3,stepIdget3);
					responseNotificationMap.put(key, mapGet);
				}
			}
			return responseNotificationMap;
			
		} finally {
			LOGGER.debug("通知用未回答一覧取得 終了");
		}
	}
	
	/**
	 * 通知用未回答一覧取得、部署通知&回答済み未通知一覧取得(事前協議)
	 * 
	 */
	public Map<String,Map<Integer,List<Integer>>> notResponseAnswerStep2(Integer notResponse) {

		LOGGER.trace("通知用未回答一覧取得(事前協議)開始");
		ChatDao chatDao = new ChatDao(emf);
		try {
			List<Answer> answerList = new ArrayList<>();
			List<Answer> answerListExpire = new ArrayList<>();
			if(notResponse.equals(1)) {
				//現在日時より未回答のリマインドを送る回答を取得
				//回答期日が超過しているものの回答リスト(期間はプロパティファイルから取得)
				//TODO 事業者回答登録日時未反映(リマインド)
				answerList = answerRepository.findNotResponseAnswerListStep2(appAnswerBussinesRegisterDays);
				//もう少しで回答期日が来るもの(期間はプロパティファイルから取得)
				//TODO 事業者回答登録日時未反映(リマインド)
				answerListExpire = answerRepository.findNotResponseAnswerExpireListStep2(appAnswerBussinesRegisterDays);
			}else if(notResponse.equals(2)) {
				//現在日時より未通知のリマインドを送る回答を取得
				//回答期日が超過しているものの回答リスト(期間はプロパティファイルから取得)
				//TODO 事業者回答登録日時未反映(リマインド)
				answerList = answerRepository.findNotNotifiedAnswerExpireListStep2(appAnswerBussinesRegisterDays,appAnswerBufferDays,appAnswerDeadlineXDaysAgo);
				//もう少しで回答期日が来るもの(期間はプロパティファイルから取得)
				//TODO 事業者回答登録日時未反映(リマインド)
				answerListExpire = answerRepository.findNotNotifiedAnswerListStep2(appAnswerBussinesRegisterDays);
			}
			Map<String,List<Integer>> departmentAndAnswerIdMap = new HashMap<>();
			for(Answer answer:answerList) {
				List<Department> departmentIdList = new ArrayList<Department>();
				//部署回答ID でO_部署回答とJOINし、そちらの部署IDを使用
				departmentIdList = chatDao.getDepartmentListByDepartmentAnswerId(answer.getDepartmentAnswerId());
				//取得した部署のリストで<部署,回答IDリスト>のマップを作成する
				for(Department department : departmentIdList) {
					//マップに部署が含まれていなければ追加
					if(!(departmentAndAnswerIdMap.containsKey(department.getDepartmentId()))) {
						List<Integer> answerId = new ArrayList<>();
						answerId.add(answer.getAnswerId());
						departmentAndAnswerIdMap.put(department.getDepartmentId(), answerId);
					}else {
						//マップに既にキー(部署ID)があれば、値を取得し回答IDを追加し戻す
						List<Integer> answerId = departmentAndAnswerIdMap.get(department.getDepartmentId());
						answerId.add(answer.getAnswerId());
						departmentAndAnswerIdMap.put(department.getDepartmentId(), answerId);
					}
				}
			}
			Map<String,List<Integer>> departmentAndAnswerIdMapExpire = new HashMap<>();
			for(Answer answer:answerListExpire) {
				//申請段階IDを取得
				List<Department> departmentIdList = new ArrayList<Department>();
				//部署回答ID でO_部署回答とJOINし、そちらの部署IDを使用
				departmentIdList = chatDao.getDepartmentListByDepartmentAnswerId(answer.getDepartmentAnswerId());
				//取得した部署のリストで<部署,回答IDリスト>のマップを作成する
				for(Department department : departmentIdList) {
					//マップに部署が含まれていなければ追加
					if(!(departmentAndAnswerIdMapExpire.containsKey(department.getDepartmentId()))) {
						List<Integer> answerId = new ArrayList<>();
						answerId.add(answer.getAnswerId());
						departmentAndAnswerIdMapExpire.put(department.getDepartmentId(), answerId);
					}else {
						//マップに既にキー(部署ID)があれば、値を取得し回答IDを追加し戻す
						List<Integer> answerId = departmentAndAnswerIdMapExpire.get(department.getDepartmentId());
						answerId.add(answer.getAnswerId());
						departmentAndAnswerIdMapExpire.put(department.getDepartmentId(), answerId);
					}
				}
			}
			//<部署ID,<期日(1:期日がもう少し 2:期日超過),<申請ID,件数>>>
			Map<String,Map<Integer,List<Integer>>> responseNotificationMap = new HashMap<>();
			Map<Integer,List<Integer>> countAplicationIdExpire2 = new HashMap<>();
			
			//部署と回答IDが入っているmapのキーリスト作成(期日がもう少し)
			List<String> keysExpire = new ArrayList<String>(departmentAndAnswerIdMapExpire.keySet());
			for(String key:keysExpire) {
				List<Integer> answerIdList = departmentAndAnswerIdMapExpire.get(key);
				//申請段階における申請IDと件数を入れるマップを作成
				List<Integer> countAplicationIdStepIdTwo = new ArrayList<Integer>();
				//回答IDのリストを回し申請IDを取得
				for(Integer id : answerIdList) {
					for(Answer answerExpire:answerListExpire) {
						if(answerExpire.getAnswerId() != null && answerExpire.getAnswerId().equals(id)) {
								if(!(countAplicationIdStepIdTwo.contains(answerExpire.getApplicationId()))) {
									//申請段階2のmapに申請IDがキーとして入っていなければ追加
									countAplicationIdStepIdTwo.add(answerExpire.getApplicationId());
								}
							}
						}
					}
				countAplicationIdExpire2.put(1,countAplicationIdStepIdTwo);
				responseNotificationMap.put(key, countAplicationIdExpire2);
			}
			//申請段階それぞれのmapを作成
			Map<Integer,List<Integer>> countAplicationId2 = new HashMap<>();
			
			//部署と回答IDが入っているmapのキーリスト作成(期日が超過)
			List<String> keys = new ArrayList<String>(departmentAndAnswerIdMap.keySet());
			for(String key:keys) {
				List<Integer> answerIdList = departmentAndAnswerIdMap.get(key);
				//各申請段階における申請IDと件数を入れるマップを作成
				List<Integer> countAplicationIdStepIdTwo = new ArrayList<Integer>();
				for(Integer id : answerIdList) {
					for(Answer answer:answerList) {
						if(answer.getAnswerId() != null && answer.getAnswerId().equals(id)) {
								if(!(countAplicationIdStepIdTwo.contains(answer.getApplicationId()))) {
									countAplicationIdStepIdTwo.add(answer.getApplicationId());
								}
						}
					}
				}
				//期日がもう少しでmapに部署IDがあるかで場合分け
				if(!(responseNotificationMap.containsKey(key))) {
					countAplicationId2.put(2,countAplicationIdStepIdTwo);
					responseNotificationMap.put(key, countAplicationId2);
				}else {
					Map<Integer,List<Integer>> mapGet = responseNotificationMap.get(key);
					mapGet.put(2,countAplicationIdStepIdTwo);
					responseNotificationMap.put(key, mapGet);
				}
			}
			return responseNotificationMap;
			
		} finally {
			LOGGER.debug("通知用未回答一覧取得(事前協議) 終了");
		}
	}
	
	/**
	 * 通知用未回答一覧取得、未通知一覧取得(事前協議・事業者側)
	 * 
	 */
	public Map<String,List<Integer>> notResponseAnswerStep2Business() {
		//リマインドする回答を取得
		List<Answer> answerList = answerRepository.findNotResponseAnswerListStep2Bussines(appAnswerBussinesStatusDays);
		List<Integer> aplicationIdList = new ArrayList<Integer>();
		//申請IDのリストを作成する(重複無し)
		for(Answer answer:answerList) {
			if(!aplicationIdList.contains(answer.getApplicationId())) {
				aplicationIdList.add(answer.getApplicationId());
			}
		}
		//レスポンス用のmapの作成(メールアドレス・申請IDリスト)
		Map<String,List<Integer>> notResponseAnswerStep2Business = new HashMap<>();
		for(Integer aplicationId: aplicationIdList) {
			List<ApplicantInformation> applicantList = applicantInformationRepository
					.getApplicantList(aplicationId, CONTACT_ADDRESS_VALID);
			if (applicantList.size() < 1) {
				LOGGER.info("連絡先データの件数が不正の為スキップ 申請ID:"+aplicationId);
				continue;
			}
			// メールアドレス
			String mailAddress = applicantList.get(0).getMailAddress();
			if(notResponseAnswerStep2Business.containsKey(mailAddress)) {
				List<Integer> aplicationIdListKey = notResponseAnswerStep2Business.get(mailAddress);
				if(!aplicationIdListKey.contains(aplicationId)) {
					aplicationIdListKey.add(aplicationId);
				}
			}else {
				List<Integer> aplicationIdListKey = new ArrayList<Integer>();
				aplicationIdListKey.add(aplicationId);
				notResponseAnswerStep2Business.put(mailAddress, aplicationIdListKey);
			}
			
		}
		return notResponseAnswerStep2Business;
	}
	
	
	/**
	 * 未回答のリマインド文章作成
	 * 
	 * @param notResponseAnswer 未回答一覧のmap
	 * @param key               部署ID
	 * @param bodyall           本文
	 */
	public String notResponseAnswerMail(Map<String, Map<Integer, Map<Integer, List<Integer>>>> notResponseAnswer,
			String key, String bodyall) {

		// メールに記載する内容を入れる要素
		MailItem baseItem = new MailItem();
		// 通知内容を取得
		Map<Integer, Map<Integer, List<Integer>>> notResponseAnswerMap = notResponseAnswer.get(key);
		// リマインドのタイトルを記載するためのフラグ
		boolean notResponseAnswerFirst = true;
		// 1事前相談 2事前協議 3許可判定
		for (int i = 1; i < 4; i++) {
			Map<Integer, List<Integer>> notResponseAnswerMapStep = notResponseAnswerMap.get(i);
			// 期日まで少しのものを取得
			List<Integer> notResponseAnswerMapStepXdays = notResponseAnswerMapStep.get(1);
			// 期日超過を取得
			List<Integer> notResponseAnswerMapStepOver = notResponseAnswerMapStep.get(2);
			String answerAuthorityFlag = "0";
			// それぞれに申請IDがあるかどうか
			if ((notResponseAnswerMapStepXdays != null && !notResponseAnswerMapStepXdays.isEmpty())
					|| (notResponseAnswerMapStepOver != null && !notResponseAnswerMapStepOver.isEmpty())) {
				// 当該部署&申請段階の権限を取得
				List<Authority> authority = authorityRepository.getAuthorityList(key, i);
				// listの中身が無い場合は権限が無いと判断しスキップ
				if (authority.size() == 0) {
					continue;
				}
				// 回答の権限を取得(0: 権限なし 1: 権限あり（所属部署のみ操作可）、2：権限あり（他部署も操作可）)
				answerAuthorityFlag = authority.get(0).getAnswerAuthorityFlag();
				if (answerAuthorityFlag == null || answerAuthorityFlag.equals("0")) {
					continue;
				}
				// 初回時にリマインドのタイトル記載
				if (notResponseAnswerFirst) {
					bodyall += getMailPropValue(MailMessageUtil.KEY_ANSWER_ALL_REMIND_DEADLINE_BODY, baseItem);
					notResponseAnswerFirst = false;
				}
				// 申請段階を記載する
				if (i == 1) {
					baseItem.setApplicationStepName(APPLICATION_STEP_ID_1_NAME);
				} else if (i == 2) {
					baseItem.setApplicationStepName(APPLICATION_STEP_ID_2_NAME);
				} else if (i == 3) {
					baseItem.setApplicationStepName(APPLICATION_STEP_ID_3_NAME);
				}
				bodyall += getMailPropValue(MailMessageUtil.KEY_ANSWER_ALL_REMIND_NOTIFICATION_STEP_BODY, baseItem);
				// 権限が2の場合は全ての部署に対する未回答を通知
				if (answerAuthorityFlag.equals("2")) {
					bodyall = notResponseAnswerAll(notResponseAnswer, key, bodyall, i,1);
				}
			}
			// 期日まで少しのものがあれば記載
			if (!answerAuthorityFlag.equals("2") && notResponseAnswerMapStepXdays != null && !notResponseAnswerMapStepXdays.isEmpty()) {
				List<MailResultItem> mailResultItems = new ArrayList<MailResultItem>();
				MailResultItem mailResultItem = new MailResultItem();
				mailResultItem.setTarget(
						notResponseAnswerMapStepXdays.stream().map(Object::toString).collect(Collectors.joining(",")));
				mailResultItem.setResult((String.valueOf(notResponseAnswerMapStepXdays.size())));
				mailResultItems.add(mailResultItem);
				baseItem.setResultList(mailResultItems);
				// X日前のものかプロパティファイルから取得し記載
				baseItem.setAnswerDays(appAnswerDeadlineXDaysAgo.toString());
				bodyall += getMailPropValue(
						MailMessageUtil.KEY_ANSWER_ALL_REMIND_NOTIFICATION_STEP_XDAYSBEFOREDUEDATE_BODY, baseItem);
			}
			// 期日超過があれば記載
			if (!answerAuthorityFlag.equals("2") && notResponseAnswerMapStepOver != null && !notResponseAnswerMapStepOver.isEmpty()) {
				List<MailResultItem> mailResultItems = new ArrayList<MailResultItem>();
				MailResultItem mailResultItem = new MailResultItem();
				mailResultItem.setTarget(
						notResponseAnswerMapStepOver.stream().map(Object::toString).collect(Collectors.joining(",")));
				mailResultItem.setResult((String.valueOf(notResponseAnswerMapStepOver.size())));
				mailResultItems.add(mailResultItem);
				baseItem.setResultList(mailResultItems);
				bodyall += getMailPropValue(MailMessageUtil.KEY_ANSWER_ALL_REMIND_NOTIFICATION_STEP_OVERDUE_BODY,
						baseItem);
			}
		}
		return bodyall;
	}
	
	/**
	 * 未通知のリマインド文章作成
	 * 
	 * @param notNotifiedAnswer    未通知一覧のmap
	 * @param tmpNotNotifiedAnswer 未通知一覧のmap(削除前)
	 * @param key                  部署ID
	 * @param bodyall              本文
	 */
	public String notNotifiedAnswerMail(Map<String, Map<Integer, Map<Integer, List<Integer>>>> notNotifiedAnswer,
			Map<String, Map<Integer, Map<Integer, List<Integer>>>> tmpNotNotifiedAnswer, String key, String bodyall) {
		// メールに記載する内容を入れる要素
		MailItem baseItem = new MailItem();
		// 通知内容を取得
		Map<Integer, Map<Integer, List<Integer>>> notNotifiedAnswerMap = notNotifiedAnswer.get(key);
		// リマインドのタイトルを記載するためのフラグ
		boolean notNotifiedAnswerFirst = true;
		// 1事前相談 2事前協議 3許可判定
		for (int i = 1; i < 4; i++) {
			Map<Integer, List<Integer>> notNotifiedAnswerMapStep = notNotifiedAnswerMap.get(i);
			// 期日まで少しのものを取得
			List<Integer> notNotifiedAnswerMapStepXdays = notNotifiedAnswerMapStep.get(1);
			// 期日超過を取得
			List<Integer> notNotifiedAnswerMapStepOver = notNotifiedAnswerMapStep.get(2);
			// それぞれに申請IDがあるかどうか
			String answerAuthorityFlag = "0";
			if ((notNotifiedAnswerMapStepXdays != null && !notNotifiedAnswerMapStepXdays.isEmpty())
					|| (notNotifiedAnswerMapStepOver != null && !notNotifiedAnswerMapStepOver.isEmpty())) {
				// 当該部署&申請段階の権限を取得
				List<Authority> authority = authorityRepository.getAuthorityList(key, i);
				// listの中身が無い場合は権限が無いと判断しスキップ
				if (authority.size() == 0) {
					continue;
				}
				// 通知の権限を取得(0: 権限なし 1: 権限あり（所属部署のみ操作可）、2：権限あり（他部署も操作可）)
				answerAuthorityFlag = authority.get(0).getNotificationAuthorityFlag();
				if (answerAuthorityFlag == null || answerAuthorityFlag.equals("0")) {
					continue;
				}
				// 初回時にリマインドのタイトル記載
				if (notNotifiedAnswerFirst) {
					bodyall += getMailPropValue(MailMessageUtil.KEY_ANSWER_ALL_REMIND_NOTIFICATION_BODY, baseItem);
					notNotifiedAnswerFirst = false;
				}
				// 申請段階を記載する
				if (i == 1) {
					baseItem.setApplicationStepName(APPLICATION_STEP_ID_1_NAME);
				} else if (i == 2) {
					baseItem.setApplicationStepName(APPLICATION_STEP_ID_2_NAME);
				} else if (i == 3) {
					baseItem.setApplicationStepName(APPLICATION_STEP_ID_3_NAME);
				}
				bodyall += getMailPropValue(MailMessageUtil.KEY_ANSWER_ALL_REMIND_NOTIFICATION_STEP_BODY, baseItem);

				// 権限が2の場合は全ての部署に対する未通知を通知
				if (answerAuthorityFlag.equals("2")) {
					bodyall = notResponseAnswerAll(tmpNotNotifiedAnswer, key, bodyall, i,2);
				}
			}
			// 期日まで少しのものがあれば記載
			if (!answerAuthorityFlag.equals("2") && notNotifiedAnswerMapStepXdays != null && !notNotifiedAnswerMapStepXdays.isEmpty()) {
				List<MailResultItem> mailResultItems = new ArrayList<MailResultItem>();
				MailResultItem mailResultItem = new MailResultItem();
				mailResultItem.setTarget(
						notNotifiedAnswerMapStepXdays.stream().map(Object::toString).collect(Collectors.joining(",")));
				mailResultItem.setResult((String.valueOf(notNotifiedAnswerMapStepXdays.size())));
				mailResultItems.add(mailResultItem);
				baseItem.setResultList(mailResultItems);
				// バッファ日数をプロパティファイルから取得し記載
				baseItem.setAnswerDays(appAnswerBufferDays.toString());
				bodyall += getMailPropValue(
						MailMessageUtil.KEY_ANSWER_ALL_REMIND_NOTIFICATION_STEP_XDAYSBEFOREDUEDATE_BODY, baseItem);
			}
			// 期日超過があれば記載
			if (!answerAuthorityFlag.equals("2") && notNotifiedAnswerMapStepOver != null && !notNotifiedAnswerMapStepOver.isEmpty()) {
				List<MailResultItem> mailResultItems = new ArrayList<MailResultItem>();
				MailResultItem mailResultItem = new MailResultItem();
				mailResultItem.setTarget(
						notNotifiedAnswerMapStepOver.stream().map(Object::toString).collect(Collectors.joining(",")));
				mailResultItem.setResult((String.valueOf(notNotifiedAnswerMapStepOver.size())));
				mailResultItems.add(mailResultItem);
				baseItem.setResultList(mailResultItems);
				bodyall += getMailPropValue(MailMessageUtil.KEY_ANSWER_ALL_REMIND_NOTIFICATION_STEP_OVERDUE_BODY,
						baseItem);
			}
		}
		return bodyall;
	}
	
	/**
	 * 未回答(事前協議)のリマインド文章作成
	 * 
	 * @param notResponseAnswerStep2    未回答(事前協議)一覧のmap
	 * @param tmpNotResponseAnswerStep2 未回答(事前協議)一覧のmap(削除前)
	 * @param key                       部署ID
	 * @param bodyall                   本文
	 */
	public String notResponseAnswerStep2Mail(Map<String, Map<Integer, List<Integer>>> notResponseAnswerStep2,
			Map<String, Map<Integer, List<Integer>>> tmpNotResponseAnswerStep2, String key, String bodyall) {
		// メールに記載する内容を入れる要素
		MailItem baseItem = new MailItem();
		// 通知内容を取得
		Map<Integer, List<Integer>> notResponseAnswerStep2Map = notResponseAnswerStep2.get(key);
		// リマインドのタイトルを記載するためのフラグ
		boolean notResponseAnswerStep2First = true;
		// 期日まで少しのものを取得
		List<Integer> notResponseAnswerStep2Xdays = notResponseAnswerStep2Map.get(1);
		// 期日超過を取得
		List<Integer> notResponseAnswerStep2Over = notResponseAnswerStep2Map.get(2);
		String answerAuthorityFlag = "0";
		// それぞれに申請IDがあるかどうか
		if ((notResponseAnswerStep2Xdays != null && !notResponseAnswerStep2Xdays.isEmpty())
				|| (notResponseAnswerStep2Over != null && !notResponseAnswerStep2Over.isEmpty())) {
			// 当該部署&申請段階の権限を取得
			List<Authority> authority = authorityRepository.getAuthorityList(key, APPLICATION_STEP_ID_2);
			// listの中身が無い場合は権限が無いと判断しスキップ
			if (authority.size() == 0) {
				return bodyall;
			}
			// 回答の権限を取得(0: 権限なし 1: 権限あり（所属部署のみ操作可）、2：権限あり（他部署も操作可）)
			answerAuthorityFlag = authority.get(0).getAnswerAuthorityFlag();
			if (answerAuthorityFlag == null || answerAuthorityFlag.equals("0")) {
				return bodyall;
			}
			// 初回時にリマインドのタイトル記載
			if (notResponseAnswerStep2First) {
				bodyall += getMailPropValue(MailMessageUtil.KEY_ANSWER_ALL_REMIND_NOTIFICATION_REGISTERED_BODY,
						baseItem);
				notResponseAnswerStep2First = false;
			}
			// 権限が2の場合は全ての部署に対する未回答を通知
			if (answerAuthorityFlag.equals("2")) {
				bodyall = notResponseAnswerAllStep2(tmpNotResponseAnswerStep2, key, bodyall,1);
			}
		}
		// 期日まで少しのものがあれば記載
		if (!answerAuthorityFlag.equals("2") && notResponseAnswerStep2Xdays != null && !notResponseAnswerStep2Xdays.isEmpty()) {
			List<MailResultItem> mailResultItems = new ArrayList<MailResultItem>();
			MailResultItem mailResultItem = new MailResultItem();
			mailResultItem.setTarget(
					notResponseAnswerStep2Xdays.stream().map(Object::toString).collect(Collectors.joining(",")));
			mailResultItem.setResult((String.valueOf(notResponseAnswerStep2Xdays.size())));
			mailResultItems.add(mailResultItem);
			baseItem.setResultList(mailResultItems);
			// Z日数をプロパティファイルから取得し記載
			baseItem.setAnswerDays(appAnswerBussinesRegisterDays.toString());
			bodyall += getMailPropValue(MailMessageUtil.KEY_ANSWER_ALL_REMIND_NOTIFICATION_STEP_XDAYSBEFOREDUEDATE_BODY,
					baseItem);
		}
		// 期日超過があれば記載
		if (!answerAuthorityFlag.equals("2") && notResponseAnswerStep2Over != null && !notResponseAnswerStep2Over.isEmpty()) {
			List<MailResultItem> mailResultItems = new ArrayList<MailResultItem>();
			MailResultItem mailResultItem = new MailResultItem();
			mailResultItem.setTarget(
					notResponseAnswerStep2Over.stream().map(Object::toString).collect(Collectors.joining(",")));
			mailResultItem.setResult((String.valueOf(notResponseAnswerStep2Over.size())));
			mailResultItems.add(mailResultItem);
			baseItem.setResultList(mailResultItems);
			bodyall += getMailPropValue(MailMessageUtil.KEY_ANSWER_ALL_REMIND_NOTIFICATION_STEP_OVERDUE_BODY, baseItem);
		}
		return bodyall;
	}
	
	/**
	 * 未通知(事前協議)のリマインド文章作成
	 * 
	 * @param notNotifiedAnswerStep2    未通知(事前協議)一覧のmap
	 * @param tmpNotNotifiedAnswerStep2 未通知(事前協議)一覧のmap(削除前)
	 * @param key                       部署ID
	 * @param bodyall                   本文
	 */
	public String notNotifiedAnswerStep2Mail(Map<String, Map<Integer, List<Integer>>> notNotifiedAnswerStep2,
			Map<String, Map<Integer, List<Integer>>> tmpNotNotifiedAnswerStep2, String key, String bodyall) {
		/////////////////////////////////////////////////////////////////////////////////////////
		// TODO 文面未確定のため中盤を修正してください
		/////////////////////////////////////////////////////////////////////////////////////////
		// メールに記載する内容を入れる要素
		MailItem baseItem = new MailItem();
		// 通知内容を取得
		Map<Integer, List<Integer>> notNotifiedAnswerStep2Map = notNotifiedAnswerStep2.get(key);
		// リマインドのタイトルを記載するためのフラグ
		boolean notResponseAnswerStep2First = true;
		// 期日まで少しのものを取得
		List<Integer> notNotifiedAnswerStep2Xdays = notNotifiedAnswerStep2Map.get(1);
		// 期日超過を取得
		List<Integer> notNotifiedAnswerStep2Over = notNotifiedAnswerStep2Map.get(2);
		String answerAuthorityFlag = "0";
		// それぞれに申請IDがあるかどうか
		if ((notNotifiedAnswerStep2Xdays != null && !notNotifiedAnswerStep2Xdays.isEmpty())
				|| (notNotifiedAnswerStep2Over != null && !notNotifiedAnswerStep2Over.isEmpty())) {
			// 当該部署&申請段階の権限を取得
			List<Authority> authority = authorityRepository.getAuthorityList(key, APPLICATION_STEP_ID_2);
			// listの中身が無い場合は権限が無いと判断しスキップ
			if (authority.size() == 0) {
				return bodyall;
			}
			// 回答の権限を取得(0: 権限なし 1: 権限あり（所属部署のみ操作可）、2：権限あり（他部署も操作可）)
			answerAuthorityFlag = authority.get(0).getNotificationAuthorityFlag();
			if (answerAuthorityFlag == null || answerAuthorityFlag.equals("0")) {
				return bodyall;
			}
			// 初回時にリマインドのタイトル記載
			if (notResponseAnswerStep2First) {
				// TODO 文面未確定のため保留、決まり次第MailMessageUtilに記載の上キーの変更とメールプロパティファイルの変更をお願いします。
				bodyall += getMailPropValue(MailMessageUtil.KEY_ANSWER_ALL_REMIND_NOTIFICATION_REGISTERED_BODY,
						baseItem);
				notResponseAnswerStep2First = false;
			}
			// 権限が2の場合は全ての部署に対する未回答を通知
			if (answerAuthorityFlag.equals("2")) {
				bodyall = notResponseAnswerAllStep2(tmpNotNotifiedAnswerStep2, key, bodyall,2);
			}
		}
		// 期日まで少しのものがあれば記載
		if (!answerAuthorityFlag.equals("2") && notNotifiedAnswerStep2Xdays != null && !notNotifiedAnswerStep2Xdays.isEmpty()) {
			List<MailResultItem> mailResultItems = new ArrayList<MailResultItem>();
			MailResultItem mailResultItem = new MailResultItem();
			mailResultItem.setTarget(
					notNotifiedAnswerStep2Xdays.stream().map(Object::toString).collect(Collectors.joining(",")));
			mailResultItem.setResult((String.valueOf(notNotifiedAnswerStep2Xdays.size())));
			mailResultItems.add(mailResultItem);
			baseItem.setResultList(mailResultItems);
			// バッファ日数をプロパティファイルから取得し記載
			baseItem.setAnswerDays(appAnswerBufferDays.toString());
			bodyall += getMailPropValue(MailMessageUtil.KEY_ANSWER_ALL_REMIND_NOTIFICATION_STEP_XDAYSBEFOREDUEDATE_BODY,
					baseItem);
		}
		// 期日超過があれば記載
		if (!answerAuthorityFlag.equals("2") && notNotifiedAnswerStep2Over != null && !notNotifiedAnswerStep2Over.isEmpty()) {
			List<MailResultItem> mailResultItems = new ArrayList<MailResultItem>();
			MailResultItem mailResultItem = new MailResultItem();
			mailResultItem.setTarget(
					notNotifiedAnswerStep2Over.stream().map(Object::toString).collect(Collectors.joining(",")));
			mailResultItem.setResult((String.valueOf(notNotifiedAnswerStep2Over.size())));
			mailResultItems.add(mailResultItem);
			baseItem.setResultList(mailResultItems);
			bodyall += getMailPropValue(MailMessageUtil.KEY_ANSWER_ALL_REMIND_NOTIFICATION_STEP_OVERDUE_BODY, baseItem);
		}
		return bodyall;
	}
	
	/**
	 * 問い合わせのリマインド文章作成
	 * 
	 * @param remindChat    未通知(事前協議)一覧のmap
	 * @param tmpNotNotifiedAnswerStep2 未通知(事前協議)一覧のmap(削除前)
	 * @param key                       部署ID
	 * @param bodyall                   本文
	 */
	public String remindChatMail(Map<String,Map<Integer,List<Integer>>> remindChat,String key,String bodyall) {
		// メールに記載する内容を入れる要素
				MailItem baseItem = new MailItem();
				// 通知内容を取得
				Map<Integer, List<Integer>> remindChatMap = remindChat.get(key);
				// リマインドのタイトルを記載するためのフラグ
				boolean remindChatMapFirst = true;
				// 1事前相談 2事前協議 3許可判定
				for (int i = 1; i < 4; i++) {
					List<Integer> remindChatMapStep = remindChatMap.get(i);
					// それぞれに申請IDがあるかどうか
					if ((remindChatMapStep != null && !remindChatMapStep.isEmpty())) {
						// 初回時にリマインドのタイトル記載
						if (remindChatMapFirst) {
							bodyall += getMailPropValue(MailMessageUtil.KEY_ANSWER_ALL_REMIND_CHAT_REGISTERED_BODY, baseItem);
							remindChatMapFirst = false;
						}
						// 申請段階を記載する
						if (i == 1) {
							baseItem.setApplicationStepName(APPLICATION_STEP_ID_1_NAME);
						} else if (i == 2) {
							baseItem.setApplicationStepName(APPLICATION_STEP_ID_2_NAME);
						} else if (i == 3) {
							baseItem.setApplicationStepName(APPLICATION_STEP_ID_3_NAME);
						}
						bodyall += getMailPropValue(MailMessageUtil.KEY_ANSWER_ALL_REMIND_NOTIFICATION_STEP_BODY, baseItem);
						List<MailResultItem> mailResultItems = new ArrayList<MailResultItem>();
						MailResultItem mailResultItem = new MailResultItem();
						mailResultItem.setTarget(
								remindChatMapStep.stream().map(Object::toString).collect(Collectors.joining(",")));
						mailResultItem.setResult((String.valueOf(remindChatMapStep.size())));
						mailResultItems.add(mailResultItem);
						baseItem.setResultList(mailResultItems);
						bodyall += getMailPropValue(MailMessageUtil.KEY_ANSWER_ALL_REMIND_CHAT_BODY,
								baseItem);
					}
				}
		return bodyall;
	}
	
	/**
	 * 回答リマインド通知の内容をメールテンプレートより置換、送信(事業者)
	 * 
	 */
	//TODO DBの取得先が不明のため記載して下さい。(リマインド)
	public void sendRemindMailAnswerBusiness(Map<String,List<Integer>> notResponseAnswerStep2Business) {
	if(notResponseAnswerStep2Business.size() != 0){
		//キーリスト(メルアドリスト)を取得
		List<String> keys = new ArrayList<String>(notResponseAnswerStep2Business.keySet());
		ApplicationDao applicationDao = new ApplicationDao(emf);
		//メルアドを回す
		for (String key : keys) {
			String bodyall = "";
			//申請IDのリスト取得
			List<Integer> applicationIdList =notResponseAnswerStep2Business.get(key);
			//各メルアドにおけるメール記載内容をリストにしておく(初期化)
			MailItem baseItem = new MailItem();
			//版情報
			List<String> versionInformations = new ArrayList<String>();
			//申請登録日時
			List<String> timestamps = new ArrayList<String>();
			//申請地番
			List<String> lotNumbers = new ArrayList<String>();
			//申請IDを回す
			for(Integer applicationId :applicationIdList) {
				//版情報を取得(申請IDと申請段階ID(2:事前協議固定))
				List<ApplicationVersionInformation> versionInformation = applicationVersionInformationRepository.findByApplicationSteId(applicationId,APPLICATION_STEP_ID_2);
				if(versionInformation == null || versionInformation.size() == 0) {
					continue;
				}
				//版情報を一旦保持
				String tmpVersionInformation = versionInformation.get(0).getVersionInformation().toString();
				
				//申請地番を取得(申請ID)
				List<ApplyLotNumber> lotNumbersList = applicationDao.getApplyingLotNumberList(applicationId, lonlatEpsg);
				if(lotNumbersList == null || lotNumbersList.size() == 0) {
					continue;
				}
				//申請地番を一旦保持
				String tmplotNumber = lotNumbersList.stream().map(lot -> lot != null ? lot.getLotNumbers() : "").collect(Collectors.joining(","));
				
				//申請登録日時を取得(申請ID)
				List<Application> applicationList = applicationRepository.getApplicationList(applicationId);
				if(applicationList == null || applicationList.size() == 0 || applicationList.get(0).getRegisterDatetime() == null) {
					continue;
				}
				DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(mailTimestampFormat);
				//申請登録日時を一旦保持
				String tmptimestamp = applicationList.get(0).getRegisterDatetime().format(dateFormat);
				
				if(	tmpVersionInformation == null || tmplotNumber == null || tmptimestamp == null 
						|| tmpVersionInformation.isEmpty()||tmplotNumber.isEmpty()||tmptimestamp.isEmpty()) {
					continue;
				}
				versionInformations.add(tmpVersionInformation);
				timestamps.add(tmptimestamp);
				lotNumbers.add(tmplotNumber);
			}
			
			//申請IDを回す
			for(int i = 0; i < versionInformations.size(); i++) {
				//最初にタイトルを記載
				if(i == 0) {
					bodyall += getMailPropValue(MailMessageUtil.KEY_ANSWER_ALLREMIND_BUSINESS_TITLE_BODY, baseItem);
				}
				baseItem.setVersionInformation(versionInformations.get(0));
				baseItem.setTimestamp(timestamps.get(0));
				baseItem.setLotNumber(lotNumbers.get(0));
				bodyall += getMailPropValue(MailMessageUtil.KEY_ANSWER_ALLREMIND_BUSINESS_BODY, baseItem);
			}
			if(versionInformations.size() != 0) {
				//メールの末尾に設定
				bodyall+= getMailPropValue(MailMessageUtil.KEY_ANSWER_ALLREMIND_BUSINESS_END_BODY, baseItem);
				//アドレスの設定
				String address =key; 
				// メールの件名設定
				String subject = getMailPropValue(MailMessageUtil.KEY_ANSWER_ALLREMIND_SUBJECT, baseItem);
				
				LOGGER.trace(address);
				LOGGER.trace(subject);
				LOGGER.trace(bodyall);

				try {
					final String[] mailAddressList = address.split(",");
					for (String aMailAddress : mailAddressList) {
						mailSendutil.sendMail(aMailAddress, subject, bodyall);
					}
				} catch (Exception e) {
					LOGGER.error("メール送信時にエラー発生", e);
					throw new RuntimeException(e);
				}
			}
		}
	}
	}
	

	/**
	 * 回答リマインド通知の内容をメールテンプレートより置換、送信
	 * 
	 * 4つのmapから部署で内容を見つける 1,回答未登録のmap 2,回答未通知のmap 3,事前協議未回答のmap 4,事前協議行政確定未登録→文面未確定
	 * 1-->1から部署を取り出し、記載内容を置換 2に1で取り出した部署があるか確認してあれば2の記載内容を置換 2にある該当部署を削除
	 * 3に1で取り出した部署があるか確認してあれば3の記載内容を置換 3にある該当部署を削除 4に1で取り出した部署があるか確認してあれば4の記載内容を置換
	 * 4にある該当部署を削除 
	 * 2-->2から部署を取り出し、記載内容を置換 3に2で取り出した部署があるか確認してあれば3の記載内容を置換
	 * 3にある該当部署を削除 4に2で取り出した部署があるか確認してあれば4の記載内容を置換 4にある該当部署を削除
	 * 3-->3から部署を取り出し、記載内容を置換 4に3で取り出した部署があるか確認してあれば4の記載内容を置換 4にある該当部署を削除
	 * 4-->4から部署を取り出し、記載内容を置換
	 */
	public void sendRemindMailAnswer(Map<String, Map<Integer, Map<Integer, List<Integer>>>> notResponseAnswer,
			Map<String, Map<Integer, Map<Integer, List<Integer>>>> notNotifiedAnswer,
			Map<String, Map<Integer, List<Integer>>> notResponseAnswerStep2,
			Map<String, Map<Integer, List<Integer>>> notNotifiedAnswerStep2,
			Map<String,Map<Integer,List<Integer>>> remindChat) {
		Map<String, Map<Integer, Map<Integer, List<Integer>>>> tmpNotNotifiedAnswer = new HashMap<>(notNotifiedAnswer);
		Map<String, Map<Integer, List<Integer>>> tmpNotResponseAnswerStep2 = new HashMap<>(notResponseAnswerStep2);
		Map<String, Map<Integer, List<Integer>>> tmpNotNotifiedAnswerStep2 = new HashMap<>(notNotifiedAnswerStep2);
		List<String> keys = new ArrayList<String>();
		MailItem baseItem = new MailItem();
		String bodyall = "";
		String address = "";
		if (notResponseAnswer.size() != 0) {
			keys = new ArrayList<String>(notResponseAnswer.keySet());
			for (String key : keys) {

				bodyall = "";
				// 宛先部署びメールアドレス
				List<Department> toDepartmentList = departmentRepository.getDepartmentListById(key);
				if (toDepartmentList.size() != 1) {
					LOGGER.error("部署データの件数が不正");
					throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
				}
				// メールアドレスの取得
				address = toDepartmentList.get(0).getMailAddress();
				// メール記載(未回答リマインド)
				bodyall = notResponseAnswerMail(notResponseAnswer, key, bodyall);

				// もしキー(部署ID)が回答通知リマインドにもある場合(該当部署に通知する内容が他にもある)
				if (notNotifiedAnswer.containsKey(key)) {
					// メール記載
					bodyall = notNotifiedAnswerMail(notNotifiedAnswer, tmpNotNotifiedAnswer, key, bodyall);
					// 回答通知で見つけた部署を削除(重複でのメール送信を防ぐため)
					notNotifiedAnswer.remove(key);
				}

				// もしキー(部署ID)が事前協議の未回答リマインドにもある場合(該当部署に通知する内容が他にもある)
				if (notResponseAnswerStep2.containsKey(key)) {
					// メール記載
					bodyall = notResponseAnswerStep2Mail(notResponseAnswerStep2, tmpNotResponseAnswerStep2, key,
							bodyall);
					// 回答通知で見つけた部署を削除(重複でのメール送信を防ぐため)
					notResponseAnswerStep2.remove(key);
				}

				// もしキー(部署ID)が事前協議の行政確定登録未通知リマインドにもある場合(該当部署に通知する内容が他にもある)
				if (notNotifiedAnswerStep2.containsKey(key)) {
					// メール記載
					bodyall = notNotifiedAnswerStep2Mail(notNotifiedAnswerStep2, tmpNotNotifiedAnswerStep2, key,
							bodyall);
					// 回答通知で見つけた部署を削除(重複でのメール送信を防ぐため)
					notNotifiedAnswerStep2.remove(key);
				}
				
				// もしキー(部署ID)が問い合わせにもある場合(該当部署に通知する内容が他にもある)
				if (remindChat.containsKey(key)) {
					// メール記載
					bodyall = remindChatMail(remindChat,key,
							bodyall);
					// 回答通知で見つけた部署を削除(重複でのメール送信を防ぐため)
					remindChat.remove(key);
				}
				if("".equals(bodyall)) {
					continue;
				}
				//メールの末尾を本文に追加
				String bodyEnd = getMailPropValue(MailMessageUtil.KEY_ANSWER_ALL_REMIND_END_BODY, baseItem);
				bodyall += bodyEnd;
				// メールの件名設定
				String subject = getMailPropValue(MailMessageUtil.KEY_ANSWER_ALLREMIND_SUBJECT, baseItem);
				LOGGER.trace(address);
				LOGGER.trace(subject);
				LOGGER.trace(bodyall);

				try {
					final String[] mailAddressList = address.split(",");
					for (String aMailAddress : mailAddressList) {
						mailSendutil.sendMail(aMailAddress, subject, bodyall);
					}
				} catch (Exception e) {
					LOGGER.error("メール送信時にエラー発生", e);
					throw new RuntimeException(e);
				}
			}
		}
		// 上記処理で見つからなかった部署のみ残っている
		if (notNotifiedAnswer.size() != 0) {
			keys = new ArrayList<String>(notNotifiedAnswer.keySet());
			for (String key : keys) {
				// 1つの部署に対するbodyを全て記載する要素
				bodyall = "";
				// 宛先部署びメールアドレス
				List<Department> toDepartmentList = departmentRepository.getDepartmentListById(key);
				if (toDepartmentList.size() != 1) {
					LOGGER.error("部署データの件数が不正");
					throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
				}
				// メールアドレスの取得
				address = toDepartmentList.get(0).getMailAddress();
				// メール記載
				bodyall = notNotifiedAnswerMail(notNotifiedAnswer, tmpNotNotifiedAnswer, key, bodyall);

				// もしキー(部署ID)が事前協議の未回答リマインドにもある場合(該当部署に通知する内容が他にもある)
				if (notResponseAnswerStep2.containsKey(key)) {
					// メール記載
					bodyall = notResponseAnswerStep2Mail(notResponseAnswerStep2, tmpNotResponseAnswerStep2, key,
							bodyall);
					// 回答通知で見つけた部署を削除(重複でのメール送信を防ぐため)
					notResponseAnswerStep2.remove(key);
				}

				// もしキー(部署ID)が事前協議の行政確定登録未通知リマインドにもある場合(該当部署に通知する内容が他にもある)
				if (notNotifiedAnswerStep2.containsKey(key)) {
					// メール記載
					bodyall = notNotifiedAnswerStep2Mail(notNotifiedAnswerStep2, tmpNotNotifiedAnswerStep2, key,
							bodyall);
					// 回答通知で見つけた部署を削除(重複でのメール送信を防ぐため)
					notNotifiedAnswerStep2.remove(key);
				}
				
				// もしキー(部署ID)が問い合わせにもある場合(該当部署に通知する内容が他にもある)
				if (remindChat.containsKey(key)) {
					// メール記載
					bodyall = remindChatMail(remindChat,key,
							bodyall);
					// 回答通知で見つけた部署を削除(重複でのメール送信を防ぐため)
					remindChat.remove(key);
				}
				if("".equals(bodyall)) {
					continue;
				}
				//メールの末尾を本文に追加
				String bodyEnd = getMailPropValue(MailMessageUtil.KEY_ANSWER_ALL_REMIND_END_BODY, baseItem);
				bodyall += bodyEnd;
				// メールの件名設定
				String subject = getMailPropValue(MailMessageUtil.KEY_ANSWER_ALLREMIND_SUBJECT, baseItem);
				LOGGER.trace(address);
				LOGGER.trace(subject);
				LOGGER.trace(bodyall);

				try {
					final String[] mailAddressList = address.split(",");
					for (String aMailAddress : mailAddressList) {
						mailSendutil.sendMail(aMailAddress, subject, bodyall);
					}
				} catch (Exception e) {
					LOGGER.error("メール送信時にエラー発生", e);
					throw new RuntimeException(e);
				}
			}
		}
		// 上記処理で見つからなかった部署のみ残っている
		if (notResponseAnswerStep2.size() != 0) {
			keys = new ArrayList<String>(notResponseAnswerStep2.keySet());
			for (String key : keys) {
				// 1つの部署に対するbodyを全て記載する要素
				bodyall = "";
				// 宛先部署びメールアドレス
				List<Department> toDepartmentList = departmentRepository.getDepartmentListById(key);
				if (toDepartmentList.size() != 1) {
					LOGGER.error("部署データの件数が不正");
					throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
				}
				// メールアドレスの取得
				address = toDepartmentList.get(0).getMailAddress();
				// メール記載
				bodyall = notResponseAnswerStep2Mail(notResponseAnswerStep2, tmpNotResponseAnswerStep2, key, bodyall);
				// もしキー(部署ID)が事前協議の行政確定登録未通知リマインドにもある場合(該当部署に通知する内容が他にもある)
				if (notNotifiedAnswerStep2.containsKey(key)) {
					// メール記載
					bodyall = notNotifiedAnswerStep2Mail(notNotifiedAnswerStep2, tmpNotNotifiedAnswerStep2, key,
							bodyall);
					// 回答通知で見つけた部署を削除(重複でのメール送信を防ぐため)
					notNotifiedAnswerStep2.remove(key);
				}
				
				// もしキー(部署ID)が問い合わせにもある場合(該当部署に通知する内容が他にもある)
				if (remindChat.containsKey(key)) {
					// メール記載
					bodyall = remindChatMail(remindChat,key,
							bodyall);
					// 回答通知で見つけた部署を削除(重複でのメール送信を防ぐため)
					remindChat.remove(key);
				}
				if("".equals(bodyall)) {
					continue;
				}
				//メールの末尾を本文に追加
				String bodyEnd = getMailPropValue(MailMessageUtil.KEY_ANSWER_ALL_REMIND_END_BODY, baseItem);
				bodyall += bodyEnd;
				// メールの件名設定
				String subject = getMailPropValue(MailMessageUtil.KEY_ANSWER_ALLREMIND_SUBJECT, baseItem);
				LOGGER.trace(address);
				LOGGER.trace(subject);
				LOGGER.trace(bodyall);

				try {
					final String[] mailAddressList = address.split(",");
					for (String aMailAddress : mailAddressList) {
						mailSendutil.sendMail(aMailAddress, subject, bodyall);
					}
				} catch (Exception e) {
					LOGGER.error("メール送信時にエラー発生", e);
					throw new RuntimeException(e);
				}
			}
		}
		// 上記処理で見つからなかった部署のみ残っている
		/////////////////////////////////////////////////////////////////////////////////////////
		// TODO 文面未確定のため中盤を修正してください
		/////////////////////////////////////////////////////////////////////////////////////////
		if (notNotifiedAnswerStep2.size() != 0) {
			keys = new ArrayList<String>(notNotifiedAnswerStep2.keySet());
			for (String key : keys) {
				// 1つの部署に対するbodyを全て記載する要素
				bodyall = "";
				// 宛先部署びメールアドレス
				List<Department> toDepartmentList = departmentRepository.getDepartmentListById(key);
				if (toDepartmentList.size() != 1) {
					LOGGER.error("部署データの件数が不正");
					throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
				}
				// メールアドレスの取得
				address = toDepartmentList.get(0).getMailAddress();
				// メール記載
				bodyall = notNotifiedAnswerStep2Mail(notNotifiedAnswerStep2, tmpNotNotifiedAnswerStep2, key, bodyall);
				/////////////////////////////////////////////////////////////////////////////////////////
				/////////////////////////////////////////////////////////////////////////////////////////
				
				// もしキー(部署ID)が問い合わせにもある場合(該当部署に通知する内容が他にもある)
				if (remindChat.containsKey(key)) {
					// メール記載
					bodyall = remindChatMail(remindChat,key,
							bodyall);
					// 回答通知で見つけた部署を削除(重複でのメール送信を防ぐため)
					remindChat.remove(key);
				}
				if("".equals(bodyall)) {
					continue;
				}
				//メールの末尾を本文に追加
				String bodyEnd = getMailPropValue(MailMessageUtil.KEY_ANSWER_ALL_REMIND_END_BODY, baseItem);
				bodyall += bodyEnd;
				// メールの件名設定
				String subject = getMailPropValue(MailMessageUtil.KEY_ANSWER_ALLREMIND_SUBJECT, baseItem);
				LOGGER.trace(address);
				LOGGER.trace(subject);
				LOGGER.trace(bodyall);

				try {
					final String[] mailAddressList = address.split(",");
					for (String aMailAddress : mailAddressList) {
						mailSendutil.sendMail(aMailAddress, subject, bodyall);
					}
				} catch (Exception e) {
					LOGGER.error("メール送信時にエラー発生", e);
					throw new RuntimeException(e);
				}
			}
		}
		if(remindChat.size() != 0) {
			keys = new ArrayList<String>(remindChat.keySet());
			for (String key : keys) {
				// 1つの部署に対するbodyを全て記載する要素
				bodyall = "";
				// 宛先部署びメールアドレス
				List<Department> toDepartmentList = departmentRepository.getDepartmentListById(key);
				if (toDepartmentList.size() != 1) {
					LOGGER.error("部署データの件数が不正");
					throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
				}
				// メールアドレスの取得
				address = toDepartmentList.get(0).getMailAddress();
				// メール記載
				bodyall = remindChatMail(remindChat,key,bodyall);
				if("".equals(bodyall)) {
					continue;
				}
				//メールの末尾を本文に追加
				String bodyEnd = getMailPropValue(MailMessageUtil.KEY_ANSWER_ALL_REMIND_END_BODY, baseItem);
				bodyall += bodyEnd;
				// メールの件名設定
				String subject = getMailPropValue(MailMessageUtil.KEY_ANSWER_ALLREMIND_SUBJECT, baseItem);
				
				LOGGER.trace(address);
				LOGGER.trace(subject);
				LOGGER.trace(bodyall);

				try {
					final String[] mailAddressList = address.split(",");
					for (String aMailAddress : mailAddressList) {
						mailSendutil.sendMail(aMailAddress, subject, bodyall);
					}
				} catch (Exception e) {
					LOGGER.error("メール送信時にエラー発生", e);
					throw new RuntimeException(e);
				}
			}
		}
	}
	
	/**
	 * 回答権限が2のものに対して全ての未回答・未通知を記載する
	 * 
	 * @param tmp          未回答・未通知のmap
	 * @param departmentId 権限が2だった部署ID
	 * @param bodyall      メールに記載するbody
	 * @param stepId       権限が2だった申請ID
	 * @param answerORNotified 未回答なのか、未通知なのか(1:未回答2:未通知)
	 */
	public String notResponseAnswerAll(Map<String, Map<Integer, Map<Integer, List<Integer>>>> tmp, String departmentId,
			String bodyall, Integer stepId,Integer answerORNotified) {
		List<String> keys = new ArrayList<String>(tmp.keySet());
		String bodyallXdays = "";
		String bodyallOver = "";
		Map<Integer, List<Integer>> stepXdaysApplicationIdResultMap = new HashMap<>();
		Map<Integer, List<Integer>> stepOverApplicationIdResultMap = new HashMap<>();
		for (String key : keys) {
			Map<Integer, Map<Integer, List<Integer>>> tmpMap = tmp.get(key);
			// 1事前相談 2事前協議 3許可判定
			for (int i = 1; i < 4; i++) {
				// 権限が2の申請段階のみ
				if (stepId != null && stepId.equals(i)) {
					Map<Integer, List<Integer>> tmpMapStep = tmpMap.get(i);
					// 期日まで少しのものを取得
					List<Integer> tmpMapStepXdays = tmpMapStep.get(1);
					// 期日超過を取得
					List<Integer> tmpMapStepOver = tmpMapStep.get(2);
					// 期日まで少しのものがあれば記載
					if (tmpMapStepXdays != null && !tmpMapStepXdays.isEmpty()) {
						if(!stepXdaysApplicationIdResultMap.containsKey(i)) {
							stepXdaysApplicationIdResultMap.put(i, new ArrayList<>(tmpMapStepXdays));
						}else {
							List<Integer> newTmpMapStepXdays = stepXdaysApplicationIdResultMap.get(i);
							if(newTmpMapStepXdays == null) {
								newTmpMapStepXdays = new ArrayList<>();
							}
							newTmpMapStepXdays.addAll(new ArrayList<>(tmpMapStepXdays));
							stepXdaysApplicationIdResultMap.put(i, newTmpMapStepXdays);
						}
					}
					// 期日超過があれば記載
					if (tmpMapStepOver != null && !tmpMapStepOver.isEmpty()) {
						if(!stepOverApplicationIdResultMap.containsKey(i)) {
							stepOverApplicationIdResultMap.put(i, new ArrayList<>(tmpMapStepOver));
						}else {
							List<Integer> newTmpMapStepOver = stepOverApplicationIdResultMap.get(i);
							if(newTmpMapStepOver == null) {
								newTmpMapStepOver = new ArrayList<>();
							}
							newTmpMapStepOver.addAll(new ArrayList<>(tmpMapStepOver));
							stepOverApplicationIdResultMap.put(i, newTmpMapStepOver);
						}
					}
				}
			}
		}
		if(stepXdaysApplicationIdResultMap != null && stepXdaysApplicationIdResultMap.size() > 0) {
			List<Integer> resultKeys = new ArrayList<Integer>(stepXdaysApplicationIdResultMap.keySet());
			for(Integer key : resultKeys) {
				if(stepXdaysApplicationIdResultMap.get(key) == null || stepXdaysApplicationIdResultMap.get(key).size() < 1) {
					continue;
				}
				Collections.sort(stepXdaysApplicationIdResultMap.get(key)); 
				MailItem baseItem = new MailItem();
				List<MailResultItem> mailResultItems = new ArrayList<MailResultItem>();
				MailResultItem mailResultItem = new MailResultItem();
				mailResultItem.setTarget(stepXdaysApplicationIdResultMap.get(key).stream().distinct().map(String::valueOf).collect(Collectors.joining(",")));
				mailResultItem.setResult((String.valueOf(stepXdaysApplicationIdResultMap.get(key).stream().distinct().collect(Collectors.toList()).size())));
				mailResultItems.add(mailResultItem);
				baseItem.setResultList(mailResultItems);
				if(answerORNotified != null && answerORNotified.equals(1)) {
					// X日前のものかプロパティファイルから取得し記載
					baseItem.setAnswerDays(appAnswerDeadlineXDaysAgo.toString());
				}else {
					// バッファ日数をプロパティファイルから取得し記載
					baseItem.setAnswerDays(appAnswerBufferDays.toString());
				}
				bodyallXdays += getMailPropValue(
						MailMessageUtil.KEY_ANSWER_ALL_REMIND_NOTIFICATION_STEP_XDAYSBEFOREDUEDATE_BODY, baseItem);
			}
		}
		if(stepOverApplicationIdResultMap != null && stepOverApplicationIdResultMap.size() > 0) {
			List<Integer> resultKeys = new ArrayList<Integer>(stepOverApplicationIdResultMap.keySet());
			for(Integer key : resultKeys) {
				if(stepOverApplicationIdResultMap.get(key) == null || stepOverApplicationIdResultMap.get(key).size() < 1) {
					continue;
				}
				Collections.sort(stepOverApplicationIdResultMap.get(key));
				MailItem baseItem = new MailItem();
				List<MailResultItem> mailResultItems = new ArrayList<MailResultItem>();
				MailResultItem mailResultItem = new MailResultItem();
				mailResultItem.setTarget(stepOverApplicationIdResultMap.get(key).stream().distinct().map(String::valueOf).collect(Collectors.joining(",")));
				mailResultItem.setResult((String.valueOf(stepOverApplicationIdResultMap.get(key).stream().distinct().collect(Collectors.toList()).size())));
				mailResultItems.add(mailResultItem);
				baseItem.setResultList(mailResultItems);
				bodyallOver += getMailPropValue(
						MailMessageUtil.KEY_ANSWER_ALL_REMIND_NOTIFICATION_STEP_OVERDUE_BODY, baseItem);
			}
		}
		bodyall += bodyallXdays + bodyallOver;
		return bodyall;
	}

	/**
	 * 回答権限が2のものに対して全ての未回答・未通知を記載する(事前協議専用)
	 * 
	 * @param tmp          未回答・未通知のmap
	 * @param departmentId 権限が2だった部署ID
	 * @param bodyall      メールに記載するbody
	 * @param stepId       権限が2だった申請ID
	 * @param answerORNotified 未回答なのか、未通知なのか(1:未回答2:未通知)
	 */
	public String notResponseAnswerAllStep2(Map<String, Map<Integer, List<Integer>>> tmp, String departmentId,
			String bodyall,Integer answerORNotified) {
		List<String> keys = new ArrayList<String>(tmp.keySet());
		String bodyallXdays = "";
		String bodyallOver = "";
		Map<Integer, List<Integer>> stepXdaysApplicationIdResultMap = new HashMap<>();
		Map<Integer, List<Integer>> stepOverApplicationIdResultMap = new HashMap<>();
		for (String key : keys) {
			Map<Integer, List<Integer>> tmpMap = tmp.get(key);
			// 期日まで少しのものを取得
			List<Integer> tmpMapStepXdays = tmpMap.get(1);
			// 期日超過を取得
			List<Integer> tmpMapStepOver = tmpMap.get(2);
			// 期日まで少しのものがあれば記載
			if (tmpMapStepXdays != null && !tmpMapStepXdays.isEmpty()) {
				if(!stepXdaysApplicationIdResultMap.containsKey(1)) {
					stepXdaysApplicationIdResultMap.put(1, new ArrayList<>(tmpMapStepXdays));
				}else {
					List<Integer> newTmpMapStepXdays = stepXdaysApplicationIdResultMap.get(1);
					if(newTmpMapStepXdays == null) {
						newTmpMapStepXdays = new ArrayList<>();
					}
					newTmpMapStepXdays.addAll(new ArrayList<>(tmpMapStepXdays));
					stepXdaysApplicationIdResultMap.put(1, newTmpMapStepXdays);
				}
			}
			// 期日超過があれば記載
			if (tmpMapStepOver != null && !tmpMapStepOver.isEmpty()) {
				if(!stepOverApplicationIdResultMap.containsKey(2)) {
					stepOverApplicationIdResultMap.put(2, new ArrayList<>(tmpMapStepOver));
				}else {
					List<Integer> newTmpMapStepOver = stepOverApplicationIdResultMap.get(2);
					if(newTmpMapStepOver == null) {
						newTmpMapStepOver = new ArrayList<>();
					}
					newTmpMapStepOver.addAll(new ArrayList<>(tmpMapStepOver));
					stepOverApplicationIdResultMap.put(2, newTmpMapStepOver);
				}
			}
		}
		if(stepXdaysApplicationIdResultMap != null && stepXdaysApplicationIdResultMap.size() > 0) {
			List<Integer> resultKeys = new ArrayList<Integer>(stepXdaysApplicationIdResultMap.keySet());
			for(Integer key : resultKeys) {
				if(stepXdaysApplicationIdResultMap.get(key) == null || stepXdaysApplicationIdResultMap.get(key).size() < 1) {
					continue;
				}
				Collections.sort(stepXdaysApplicationIdResultMap.get(key)); 
				MailItem baseItem = new MailItem();
				List<MailResultItem> mailResultItems = new ArrayList<MailResultItem>();
				MailResultItem mailResultItem = new MailResultItem();
				mailResultItem.setTarget(stepXdaysApplicationIdResultMap.get(key).stream().distinct().map(String::valueOf).collect(Collectors.joining(",")));
				mailResultItem.setResult((String.valueOf(stepXdaysApplicationIdResultMap.get(key).stream().distinct().collect(Collectors.toList()).size())));
				mailResultItems.add(mailResultItem);
				baseItem.setResultList(mailResultItems);
				if(answerORNotified != null && answerORNotified.equals(1)) {
					// Z日数をプロパティファイルから取得し記載
					baseItem.setAnswerDays(appAnswerBussinesRegisterDays.toString());
				}else {
					// バッファ日数をプロパティファイルから取得し記載
					baseItem.setAnswerDays(appAnswerBufferDays.toString());
				}
				bodyallXdays += getMailPropValue(
						MailMessageUtil.KEY_ANSWER_ALL_REMIND_NOTIFICATION_STEP_XDAYSBEFOREDUEDATE_BODY, baseItem);
			}
		}
		if(stepOverApplicationIdResultMap != null && stepOverApplicationIdResultMap.size() > 0) {
			List<Integer> resultKeys = new ArrayList<Integer>(stepOverApplicationIdResultMap.keySet());
			for(Integer key : resultKeys) {
				if(stepOverApplicationIdResultMap.get(key) == null || stepOverApplicationIdResultMap.get(key).size() < 1) {
					continue;
				}
				Collections.sort(stepOverApplicationIdResultMap.get(key));
				MailItem baseItem = new MailItem();
				List<MailResultItem> mailResultItems = new ArrayList<MailResultItem>();
				MailResultItem mailResultItem = new MailResultItem();
				mailResultItem.setTarget(stepOverApplicationIdResultMap.get(key).stream().distinct().map(String::valueOf).collect(Collectors.joining(",")));
				mailResultItem.setResult((String.valueOf(stepOverApplicationIdResultMap.get(key).stream().distinct().collect(Collectors.toList()).size())));
				mailResultItems.add(mailResultItem);
				baseItem.setResultList(mailResultItems);
				bodyallOver += getMailPropValue(
						MailMessageUtil.KEY_ANSWER_ALL_REMIND_NOTIFICATION_STEP_OVERDUE_BODY, baseItem);
			}
		}
		bodyall += bodyallXdays + bodyallOver;
		return bodyall;
	}
	
	/**
	 * 回答レポートの増加分を特定(事前協議の場合のみ)
	 * 
	 * @param form パラメータ
	 */
	public GeneralConditionDiagnosisReportRequestForm addAnswer(Map<Integer, String> answerNotifiedTextMap,Integer applicationStepId) {
		//回答IDで取得された判定をexcelに出力するためのオブジェクトを作成
		List<GeneralConditionDiagnosisResultForm> generalConditionDiagnosisResultFormList = new ArrayList<GeneralConditionDiagnosisResultForm>();
		List<Integer> keys = new ArrayList<>(answerNotifiedTextMap.keySet());
		Map<Integer, Map<String, String>> answerJudgementMap = new HashMap<>();
		//回答IDのリスト(key)で判定を取得していく
		int countId = 0;
		for(Integer key:keys) {
			GeneralConditionDiagnosisResultForm generalConditionDiagnosisResultForm = new GeneralConditionDiagnosisResultForm();
			Map<String, String> answerJudgementMap2 = new HashMap<>();
			//回答ＩＤからo_回答取得
			List<Answer> answer = answerRepository.findByAnswerId(key);
			//判定項目ＩＤ
			String judgementId = answer.get(0).getJudgementId();
			//部署ID
			String departmentId = answer.get(0).getDepartmentId();
			//判定項目ＩＤ、部署ＩＤ、申請段階IDでM_判定結果を取得
			List<CategoryJudgementResult> categoryJudgementResultList = judgementResultRepository.getJudgementTitleByJudgementIdAndApplicationStepId(judgementId,applicationStepId,departmentId);
			if(!categoryJudgementResultList.isEmpty()) {
				//文言
				generalConditionDiagnosisResultForm.setDescription(categoryJudgementResultList.get(0).getApplicableDescription());
				//タイトル
				generalConditionDiagnosisResultForm.setTitle(categoryJudgementResultList.get(0).getTitle());
				//概要
				generalConditionDiagnosisResultForm.setSummary(categoryJudgementResultList.get(0).getApplicableSummary());
				//判定項目ID
				generalConditionDiagnosisResultForm.setJudgementId(judgementId);
				//判定結果項目ID
				generalConditionDiagnosisResultForm.setJudgeResultItemId(countId);
				//answerIdとanswerContentのmapを作成
				answerJudgementMap2.put("answerId", key.toString());
				if(answer.get(0).getAnswerContent()==null) {
					if(categoryJudgementResultList.get(0).getDefaultAnswer()==null) {
						answerJudgementMap2.put("answerContent", "");
					}else {
						answerJudgementMap2.put("answerContent", categoryJudgementResultList.get(0).getDefaultAnswer());
					}
				}else {
					answerJudgementMap2.put("answerContent", answer.get(0).getAnswerContent());
				}
				//セット
				generalConditionDiagnosisResultFormList.add(generalConditionDiagnosisResultForm);
				answerJudgementMap.put(countId, answerJudgementMap2);
				countId++;
			}
		}
		//セット
		GeneralConditionDiagnosisReportRequestForm generalConditionDiagnosisReportRequestForm = new GeneralConditionDiagnosisReportRequestForm();
		generalConditionDiagnosisReportRequestForm.setGeneralConditionDiagnosisResults(generalConditionDiagnosisResultFormList);
		generalConditionDiagnosisReportRequestForm.setAnswerJudgementMap(answerJudgementMap);
	return generalConditionDiagnosisReportRequestForm;
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
	 * 申請種類IDから申請種類を取得する
	 * 
	 * @param applicationTypeId 申請種別ID
	 * @return 申請種別
	 */
	private ApplicationType getApplicationType(Integer applicationTypeId) {
	
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
		
		return applicationType;
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
	 * 申請IDと申請段階IDから版情報を取得する
	 * 
	 * @param applicationId 申請ID
	 * @param applicationStepId 申請段階ID
	 * @return 版情報
	 */
	private Integer getVersionInformation(Integer applicationId, Integer applicationStepId) {
	
		ApplicationVersionInformation applicationVersionInformation;
		
		LOGGER.trace("O_申請版情報取得 開始");
		List<ApplicationVersionInformation> applicationVersionInformationList = applicationVersionInformationRepository
				.findByApplicationSteId(applicationId, applicationStepId);
		if (applicationVersionInformationList.size() == 0) {
			LOGGER.warn("O_申請版情報が存在しません。");
			throw new RuntimeException();
		}
		applicationVersionInformation = applicationVersionInformationList.get(0);
		LOGGER.trace("O_申請版情報取得 終了");
		
		return applicationVersionInformation.getVersionInformation();
	}
	
	/**
	 * 部署IDと申請段階IDからM_権限を取得する
	 * 
	 * @param departmentId 部署ID
	 * @param applicationStepId 申請段階ID
	 * @return 版情報
	 */
	private Authority getAuthority(String departmentId, Integer applicationStepId) {
	
		Authority authority;
		
		LOGGER.trace("M_権限報取得 開始");
		List<Authority> AuthorityList = authorityRepository
				.getAuthorityList(departmentId, applicationStepId);
		if (AuthorityList.size() == 0) {
			LOGGER.warn("M_権限報取得が存在しません。");
			throw new RuntimeException();
		}
		authority = AuthorityList.get(0);
		LOGGER.trace("M_権限報取得取得 終了");
		
		return authority;
	}
	
	/**
	 *  回答通知
	 * @param answerNotifyRequestForm 回答通知リクエストフォーム
	 */
	public void notify(AnswerNotifyRequestForm answerNotifyRequestForm) {
		// 回答通知種類
		String notifyType = answerNotifyRequestForm.getNotifyType();
		// 申請段階ID
		Integer applicationStepId = answerNotifyRequestForm.getApplicationStepId();

		// 申請段階、通知種類より、各種回答通知を行う
		if (APPLICATION_STEP_ID_2.equals(applicationStepId)) {
			// 事業者に回答通知を行う
			if (NOTIFY_TYPE_0_ANSWERED.equals(notifyType)) {
				notifyAnswer(answerNotifyRequestForm);
			}

			// 事業者に差戻通知を行う
			if (NOTIFY_TYPE_1_REMANDED.equals(notifyType)) {
				notifyAnswerRemand(answerNotifyRequestForm);
			}

			// 担当課に受付通知を行う
			if (NOTIFY_TYPE_2_ACCEPTED.equals(notifyType)) {
				notifyAnswerAccept(answerNotifyRequestForm);
			}

			// 統括部署管理者に回答許可通知 
			if (NOTIFY_TYPE_3_ANSWER_PERMISSION.equals(notifyType)) {
				notifyAnswerPermission(answerNotifyRequestForm);
			}

			// 統括部署管理者に行政確定登録許可通知
			if (NOTIFY_TYPE_4_GOVERNMENT_CONFIRM_PERMISSION.equals(notifyType)) {
				notifyAnswerGovernmentConfirmPermission(answerNotifyRequestForm);
			}

		} else {
			// 事業者に回答通知を行う
			notifyAnswer(answerNotifyRequestForm);
		}

	}

	/**
	 * 事業者に申請差戻を通知する
	 * 
	 * @param answerNotifyRequestForm 回答通知リクエストフォーム
	 */
	private void notifyAnswerRemand(AnswerNotifyRequestForm answerNotifyRequestForm) {

		// 申請ID
		Integer applicationId = answerNotifyRequestForm.getApplicationId();
		// 申請段階ID
		Integer applicationStepId = answerNotifyRequestForm.getApplicationStepId();

		List<ApplicationVersionInformation> applicationVersionInformationList = applicationVersionInformationRepository
				.findByApplicationSteId(applicationId, applicationStepId);

		if (applicationVersionInformationList.size() < 1) {
			LOGGER.warn("パラメータ不正");
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		}

		// O_申請：申請ステータスが「事前協議：未完（要再申請）」に更新
		if (applicationJdbc.updateApplicationStatus(applicationId, STATUS_DISCUSSIONS_REAPP) != 1) {
			LOGGER.warn("申請ステータスの更新件数不正");
			throw new RuntimeException("申請ステータスの更新に失敗");
		}

		// O_申請版情報：受付フラグを更新
		if (applicationVersionInformationJdbc.updateForRemand(applicationId, applicationStepId,
				applicationVersionInformationList.get(0).getUpdateDatetime()) != 1) {
			LOGGER.warn("申請ステータスの更新件数不正");
			throw new RuntimeException("申請ステータスの更新に失敗");
		}

		// O_申請ファイルが削除に更新
		List<ApplicationFile> applicationFileList = applicationFileRepository.getApplicationFilesByVersionInformation(
				applicationId, applicationStepId, applicationVersionInformationList.get(0).getVersionInformation());

		for (ApplicationFile file : applicationFileList) {

			// O_申請版情報：受付フラグを更新
			if (applicationFileJdbc.updateDeleteFlag(file.getFileId()) != 1) {
				LOGGER.warn("申請ファイルの論理削除件数不正");
				throw new RuntimeException("申請ファイルの論理削除に失敗");
			}
		}

		// 申請受付・差戻コメント
		String acceptCommentText = answerNotifyRequestForm.getAcceptCommentText();
		// 事業者に差戻通知を送信する
		Integer versionInformation = applicationVersionInformationList.get(0).getVersionInformation();
		sendRemandMailToBusinessUser(applicationId, applicationStepId, versionInformation, acceptCommentText);
	}

	/**
	 * 担当課に受付通知を通知する
	 * 
	 * @param answerNotifyRequestForm 回答通知リクエストフォーム
	 */
	private void notifyAnswerAccept(AnswerNotifyRequestForm answerNotifyRequestForm) {

		// 申請ID
		Integer applicationId = answerNotifyRequestForm.getApplicationId();
		// 申請段階ID
		Integer applicationStepId = answerNotifyRequestForm.getApplicationStepId();

		List<ApplicationVersionInformation> applicationVersionInformationList = applicationVersionInformationRepository
				.findByApplicationSteId(applicationId, applicationStepId);

		if (applicationVersionInformationList.size() == 0) {
			LOGGER.warn("申請版情報が存在しません。申請ID：" + applicationId + "、申請段階ID：" + applicationStepId);
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		}
		// 版情報
		Integer versionInformation = applicationVersionInformationList.get(0).getVersionInformation();

		// 部署ID-部署回答IDの紐づくマップ作成
		final Map<String, Integer> departmentIdAnswerIdMap = new HashMap<String, Integer>();
		List<DepartmentAnswer> departmentAnswerList = departmentAnswerRepository.findByApplicationId(applicationId);
		for (DepartmentAnswer departmentAnswer : departmentAnswerList) {
			departmentIdAnswerIdMap.put(departmentAnswer.getDepartmentId(), departmentAnswer.getDepartmentAnswerId());
		}

		// ０_受付回答
		LOGGER.trace("O_回答登録 開始");
		List<AcceptingAnswer> acceptingAnswerList = acceptingAnswerRepository.findByVersionInfomation(applicationId,
				applicationStepId, versionInformation);
		List<Integer> departmentAnswerIdList = new ArrayList<Integer>();
		for (AcceptingAnswer acceptingAnswer : acceptingAnswerList) {

			// 部署IDに対するO_部署回答が存在しない場合、登録する
			if (!departmentIdAnswerIdMap.containsKey(acceptingAnswer.getDepartmentId())) {

				// O_部署回答を登録
				Integer departmentAnswerId = departmentAnswerJdbc.insert(applicationId,
						acceptingAnswer.getDepartmentId(), STATE_APPLIED);

				departmentIdAnswerIdMap.put(acceptingAnswer.getDepartmentId(), departmentAnswerId);
			}

			// O_受付回答からO_回答にコピー登録
			answerJdbc.insertCopyAcceptingAnswer(acceptingAnswer,
					departmentIdAnswerIdMap.get(acceptingAnswer.getDepartmentId()));

			departmentAnswerIdList.add(departmentIdAnswerIdMap.get(acceptingAnswer.getDepartmentId()));
		}
		LOGGER.trace("O_回答登録 完了");

		// 申請受付中の版番号に対する差し替えた申請ファイル紐づく部署回答IDを取得
		List<Integer> list = getApplicationFileChangedDepartmentAnswer(applicationId, applicationStepId,
				versionInformation);

		for (Integer id : list) {
			if (!departmentAnswerIdList.contains(id)) {
				departmentAnswerIdList.add(id);
			}
		}

		// ０_部署回答
		LOGGER.trace("O_部署回答クリア 開始");
		for (DepartmentAnswer departmentAnswer : departmentAnswerList) {
			// 統括部署管理者に行政確定登録許可通知済みのみを更新
			if (departmentAnswer.getGovernmentConfirmPermissionFlag() != null
					&& departmentAnswer.getGovernmentConfirmPermissionFlag()) {
				// 部署配下に回答が変更あり、又は、回答に対する申請ファイルが差し替えありの場合、部署全体の行政確定登録内容をクリア
				if (departmentAnswerIdList.contains(departmentAnswer.getDepartmentAnswerId())) {

					// O_部署回答：行政確定登録内容クリア
					if (departmentAnswerJdbc.clearGovernmentConfirmInfo(departmentAnswer) != 1) {
						LOGGER.warn("行政確定登録内容クリアの更新件数不正");
						throw new RuntimeException("行政確定登録内容クリアに失敗");
					}
				}
			}

		}
		LOGGER.trace("O_部署回答クリア 完了");

		// O_申請版情報：受付フラグ、受付版情報を更新
		if (applicationVersionInformationJdbc.updateForAccept(applicationId, applicationStepId,
				answerNotifyRequestForm.getUpdateDatetime()) != 1) {
			LOGGER.warn("申請ステータスの更新件数不正");
			throw new RuntimeException("申請ステータスの更新に失敗");
		}

		// 申請受付・差戻コメント
		String acceptCommentText = answerNotifyRequestForm.getAcceptCommentText();

		// 各担当課に受付通知を送信する、申請ファイル更新あり通知
		sendAcceptMailToGovernmentUser(applicationId, applicationStepId, applicationVersionInformationList.get(0), acceptCommentText);

	}

	/**
	 * 統括部署管理者に回答許可を通知する
	 * 
	 * @param answerNotifyRequestForm 回答通知リクエストフォーム
	 */
	private void notifyAnswerPermission(AnswerNotifyRequestForm answerNotifyRequestForm) {

		// 画面に選択される回答リスト
		List<AnswerForm> answerFormList = answerNotifyRequestForm.getAnswers();
		for (AnswerForm answerForm : answerFormList) {

			if (answerJdbc.updateAnswerPermissionFlag(answerForm) != 1) {
				LOGGER.error("回答通知許可フラグの更新件数が不正");
				throw new RuntimeException("回答通知許可フラグの更新件数が不正");
			}
		}

		// 統括部署管理者に回答許可を送信する
		sendPermissionMailToAdministers(answerNotifyRequestForm.getApplicationId(),
				answerNotifyRequestForm.getApplicationStepId(), answerNotifyRequestForm.getAnswers(), null, false);

	}

	/**
	 * 統括部署管理者に行政確定登録許可通知を通知する
	 * 
	 * @param answerNotifyRequestForm 回答通知リクエストフォーム
	 */
	private void notifyAnswerGovernmentConfirmPermission(AnswerNotifyRequestForm answerNotifyRequestForm) {

		// 画面に選択される回答リスト
		List<AnswerForm> answerFormList = answerNotifyRequestForm.getAnswers();
		for (AnswerForm answerForm : answerFormList) {

			if (answerJdbc.updateGovernmentConfirmPermissionFlag(answerForm) != 1) {
				LOGGER.error("行政確定登録通知許可フラグの更新件数が不正");
				throw new RuntimeException("行政確定登録通知許可フラグの更新件数が不正");
			}
		}

		// 画面に選択される部署回答リスト
		List<DepartmentAnswerForm> departmentAnswerFormList = answerNotifyRequestForm.getDepartmentAnswers();
		for (DepartmentAnswerForm departmentAnswerForm : departmentAnswerFormList) {

			if (departmentAnswerJdbc.updateGovernmentConfirmPermissionFlag(departmentAnswerForm) != 1) {
				LOGGER.error("部署回答データの更新件数が不正");
				throw new RuntimeException("部署回答データの更新件数が不正");
			}
		}

		// 統括部署管理者に行政確定許可通知を送信する
		sendPermissionMailToAdministers(answerNotifyRequestForm.getApplicationId(),
				answerNotifyRequestForm.getApplicationStepId(), answerNotifyRequestForm.getAnswers(),
				answerNotifyRequestForm.getDepartmentAnswers(), true);

	}
	
	/**
	 * 受付中の申請ファイルには、ファイル実体変更ありの申請ファイルに対する回答担当部署取得
	 * 
	 * @param applicationId      申請ID
	 * @param applicationStepId  申請段階ID
	 * @param versionInformation 版番号
	 * @return 部署回答リスト
	 */
	private List<Integer> getApplicationFileChangedDepartmentAnswer(Integer applicationId, Integer applicationStepId,
			Integer versionInformation) {

		List<Integer> departmentAnswerIdList = new ArrayList<Integer>();
		List<String> applicationFileMasterIdList = new ArrayList<String>();

		// 受付中の版番号に対する申請ファイルを取得
		List<ApplicationFile> applicationFileList = applicationFileRepository
				.getApplicationFilesByVersionInformation(applicationId, applicationStepId, versionInformation);

		// ファイル実体変更ありんの申請ファイルマスタIDを抽出
		for (ApplicationFile applicationFile : applicationFileList) {

			// 申請ファイルの指示元担当課があれば、申請ファイル差し戻されたとする
			if (applicationFile.getDirectionDepartment() != null
					&& !EMPTY.equals(applicationFile.getDirectionDepartment())
					&& !applicationReportFileId.equals(applicationFile.getApplicationFileId())) {
				if (!applicationFileMasterIdList.contains(applicationFile.getApplicationFileId())) {
					applicationFileMasterIdList.add(applicationFile.getApplicationFileId());
				}
			}
		}

		// 申請ファイルマスタIDに対する担当部署取得して、部署回答IDを抽出
		if (applicationFileMasterIdList.size() > 0) {

			AnswerDao dao = new AnswerDao(emf);

			List<DepartmentAnswer> departmentAnswerList = dao.getDepartmentAnswerList(applicationId, applicationStepId,
					applicationFileMasterIdList);

			for (DepartmentAnswer departmentAnswer : departmentAnswerList) {
				if (!departmentAnswerIdList.contains(departmentAnswer.getDepartmentAnswerId())) {
					departmentAnswerIdList.add(departmentAnswer.getDepartmentAnswerId());
				}
			}
		}

		return departmentAnswerIdList;

	}

	/**
	 * 統括部署管理者に回答許可通知・行政確定登録許可通知を送信する
	 * 
	 * @param applicationId            申請ID
	 * @param applicationStepId        申請段階ID
	 * @param answerFormList           選択した通知回答リスト
	 * @param departmentAnswerFormList 選択した通知部署回答リスト
	 * @param isGovernmentConfirm      行政確定登録許可通知かどうか
	 * 
	 */
	private void sendPermissionMailToAdministers(Integer applicationId, Integer applicationStepId,
			List<AnswerForm> answerFormList, List<DepartmentAnswerForm> departmentAnswerFormList,
			boolean isGovernmentConfirm) {

		MailItem baseItem = new MailItem();

		// 申請者情報取得
		List<ApplicantInformation> applicantList = applicantInformationRepository.getApplicantList(applicationId,
				CONTACT_ADDRESS_INVALID);
		if (applicantList.size() < 1) {
			LOGGER.error("申請者データの件数が不正");
			throw new RuntimeException("申請者データの件数が不正");
		}
		ApplicantInformation applicant = applicantList.get(0);
		switch (applicantNameItemNumber) { // 氏名はプロパティで設定されたアイテムを設定
		case 1:
			baseItem.setName(applicant.getItem1());
			break;
		case 2:
			baseItem.setName(applicant.getItem2());
			break;
		case 3:
			baseItem.setName(applicant.getItem3());
			break;
		case 4:
			baseItem.setName(applicant.getItem4());
			break;
		case 5:
			baseItem.setName(applicant.getItem5());
			break;
		case 6:
			baseItem.setName(applicant.getItem6());
			break;
		case 7:
			baseItem.setName(applicant.getItem7());
			break;
		case 8:
			baseItem.setName(applicant.getItem8());
			break;
		case 9:
			baseItem.setName(applicant.getItem9());
			break;
		case 10:
			baseItem.setName(applicant.getItem10());
			break;
		default:
			LOGGER.error("氏名アイテム番号指定が不正: " + applicantNameItemNumber);
			throw new RuntimeException("氏名アイテム番号指定が不正");
		}
		baseItem.setMailAddress(applicant.getMailAddress()); // メールアドレス

		Application application = getApplication(applicationId);

		// 申請の情報
		baseItem.setApplicationId(application.getApplicationId().toString()); // 申請ID
		baseItem.setApplicationTypeName(getApplicationTypeName(application.getApplicationTypeId())); // 申請種類名
		baseItem.setApplicationStepName(getApplicationStepName(applicationStepId)); // 申請段階名

		// 版情報-統括部署管理者に回答許可通知の場合、受付版情報を使う
		List<ApplicationVersionInformation> applicationVersionInformationList = applicationVersionInformationRepository
				.findByApplicationSteId(applicationId, applicationStepId);
		if (applicationVersionInformationList.size() == 0) {
			LOGGER.warn("O_申請版情報が存在しません。");
			throw new RuntimeException();
		}
		ApplicationVersionInformation applicationVersionInformation = applicationVersionInformationList.get(0);

		baseItem.setVersionInformation(applicationVersionInformation.getAcceptVersionInformation().toString());

		// 地番
		ApplicationDao applicationDao = new ApplicationDao(emf);
		List<ApplyLotNumber> lotNumbersList = applicationDao.getApplyingLotNumberList(applicationId, lonlatEpsg);
		String addressText = (lotNumbersList != null && lotNumbersList.size() > 0)
				? lotNumbersList.get(0).getLotNumbers()
				: "";
		baseItem.setLotNumber(addressText);

		// 宛先：統括部署管理者のメールアドレス取得
		AnswerDao answerDao = new AnswerDao(emf);
		List<Department> governmentUserAndAuthorityList = answerDao.getControlDepartmentList();
		
		// 部署の集約（部署ID-部署名）
		Map<String, String> departmentMap = new HashMap<String, String>();
		// 部署別の対象・判定結果リスト（部署ID-対象・判定結果）
		Map<String, List<MailResultItem>> resultMap = new HashMap<String, List<MailResultItem>>();
		for (AnswerForm answerForm : answerFormList) {

			List<Answer> answerList = answerRepository.findByAnswerId(answerForm.getAnswerId());
			if (answerList.size() == 0) {
				LOGGER.warn("O_回答が存在しません。回答ID：" + answerForm.getAnswerId());
				throw new RuntimeException();
			}
			Answer answer = answerList.get(0);

			if (!departmentMap.containsKey(answer.getDepartmentId())) {

				// 部署取得
				List<Department> departmentList = departmentRepository.getDepartmentListById(answer.getDepartmentId());
				if (departmentList.size() == 0) {
					LOGGER.warn("M_部署が存在しません。部署ID：" + answer.getDepartmentId());
					throw new RuntimeException();
				}
				Department department = departmentList.get(0);

				departmentMap.put(answer.getDepartmentId(), department.getDepartmentName());
			}

			// 回答許可通知の場合、選択している回答対象リストを部署ごとに集約
			if (!isGovernmentConfirm) {

				if (!resultMap.containsKey(answer.getDepartmentId())) {
					// 初期化
					resultMap.put(answer.getDepartmentId(), new ArrayList<MailResultItem>());
				}

				// 行政で追加した条項は、判定項目IDがないため、判定項目IDがあるの条項はタイトルを取得する
				if (answer.getJudgementId() != null && !EMPTY.equals(answer.getJudgementId())) {
					List<CategoryJudgementResult> categoryJudgementResultList = answerDao.getJudgementResultByAnswerId(answer.getAnswerId());
					if (categoryJudgementResultList.size() == 0) {
						LOGGER.warn("M_判定結果の値が取得できない 回答ID: " + answer.getAnswerId());
						continue;
					}

					CategoryJudgementResult category = categoryJudgementResultList.get(0);

					MailResultItem item = new MailResultItem();
					item.setTarget(category.getTitle()); // 対象
					item.setResult(answer.getJudgementResult()); // 判定結果

					// 部署ごとに集約
					resultMap.get(answer.getDepartmentId()).add(item);
				}
			}
		}

		// 行政確定登録許可通知 の場合
		if (isGovernmentConfirm) {
			for (DepartmentAnswerForm departmentAnswerForm : departmentAnswerFormList) {

				List<DepartmentAnswer> departmentAnswerList = departmentAnswerRepository
						.findByDepartmentAnswerId(departmentAnswerForm.getDepartmentAnswerId());
				if (departmentAnswerList.size() == 0) {
					LOGGER.warn("O_部署回答が存在しません。部署回答ID：" + departmentAnswerForm.getDepartmentAnswerId());
					throw new RuntimeException();
				}
				DepartmentAnswer departmentAnswer = departmentAnswerList.get(0);

				if (!departmentMap.containsKey(departmentAnswer.getDepartmentId())) {

					// 部署取得
					List<Department> departmentList = departmentRepository
							.getDepartmentListById(departmentAnswer.getDepartmentId());
					if (departmentList.size() == 0) {
						LOGGER.warn("M_部署が存在しません。部署ID：" + departmentAnswer.getDepartmentId());
						throw new RuntimeException();
					}
					Department department = departmentList.get(0);

					departmentMap.put(departmentAnswer.getDepartmentId(), department.getDepartmentName());
				}
			}

		}

		// 部署ごとに、メール内容を編集して、統括部署管理者に送信する
		for (String key : departmentMap.keySet()) {

			String subject = EMPTY;
			String body = EMPTY;

			MailItem governmentItem = baseItem.clone();
			governmentItem.setDepartmentName(departmentMap.get(key));

			if (isGovernmentConfirm) {
				// 事前協議：行政確定登録許可通知（行政（統括部署管理者）向け）
				subject = getMailPropValue(MailMessageUtil.KEY_NEGOTIATION_CONFIRMED_APPROVAL_NOTIFICATION_SUBJECT,
						governmentItem);
				body = getMailPropValue(MailMessageUtil.KEY_NEGOTIATION_CONFIRMED_APPROVAL_NOTIFICATION_BODY,
						governmentItem);

			} else {
				governmentItem.setResultList(resultMap.get(key));

				// 事前協議： 回答許可通知(行政（統括部署管理者）向け)
				subject = getMailPropValue(MailMessageUtil.KEY_RESPONSE_APPROVAL_SUBJECT, governmentItem);
				body = getMailPropValue(MailMessageUtil.KEY_RESPONSE_APPROVAL_BODY, governmentItem);
			}

			try {
				for (Department userInfo : governmentUserAndAuthorityList) {
					LOGGER.trace(userInfo.getAdminMailAddress());
					LOGGER.trace(subject);
					LOGGER.trace(body);

					final String[] mailAddressList = userInfo.getAdminMailAddress().split(",");
					for (String aMailAddress : mailAddressList) {
						mailSendutil.sendMail(aMailAddress, subject, body);
					}
				}
			} catch (Exception e) {
				LOGGER.error("メール送信時にエラー発生", e);
				throw new RuntimeException(e);
			}

		}

	}

	/**
	 * 回答担当課管理者：事前協議行政確定登録完了通知
	 * 
	 * @param applicationId     申請ID
	 * @param applicationStepId 申請段階ID
	 * @param departmentIdList  部署IDリスト
	 */
	private void sendAnswerCompletedMainToGovernmentAdmin(Integer applicationId, Integer applicationStepId,
			List<String> departmentIdList) {

		MailItem baseItem = new MailItem();

		// 申請者情報取得
		List<ApplicantInformation> applicantList = applicantInformationRepository.getApplicantList(applicationId,
				CONTACT_ADDRESS_INVALID);
		if (applicantList.size() < 1) {
			LOGGER.error("申請者データの件数が不正");
			throw new RuntimeException("申請者データの件数が不正");
		}
		ApplicantInformation applicant = applicantList.get(0);
		switch (applicantNameItemNumber) { // 氏名はプロパティで設定されたアイテムを設定
		case 1:
			baseItem.setName(applicant.getItem1());
			break;
		case 2:
			baseItem.setName(applicant.getItem2());
			break;
		case 3:
			baseItem.setName(applicant.getItem3());
			break;
		case 4:
			baseItem.setName(applicant.getItem4());
			break;
		case 5:
			baseItem.setName(applicant.getItem5());
			break;
		case 6:
			baseItem.setName(applicant.getItem6());
			break;
		case 7:
			baseItem.setName(applicant.getItem7());
			break;
		case 8:
			baseItem.setName(applicant.getItem8());
			break;
		case 9:
			baseItem.setName(applicant.getItem9());
			break;
		case 10:
			baseItem.setName(applicant.getItem10());
			break;
		default:
			LOGGER.error("氏名アイテム番号指定が不正: " + applicantNameItemNumber);
			throw new RuntimeException("氏名アイテム番号指定が不正");
		}
		baseItem.setMailAddress(applicant.getMailAddress()); // メールアドレス

		// 版情報-統括部署管理者に回答許可通知の場合、受付版情報を使う
		List<ApplicationVersionInformation> applicationVersionInformationList = applicationVersionInformationRepository
				.findByApplicationSteId(applicationId, applicationStepId);
		if (applicationVersionInformationList.size() == 0) {
			LOGGER.warn("O_申請版情報が存在しません。");
			throw new RuntimeException();
		}
		ApplicationVersionInformation applicationVersionInformation = applicationVersionInformationList.get(0);

		baseItem.setVersionInformation(applicationVersionInformation.getAcceptVersionInformation().toString());

		// 地番
		ApplicationDao applicationDao = new ApplicationDao(emf);
		List<ApplyLotNumber> lotNumbersList = applicationDao.getApplyingLotNumberList(applicationId, lonlatEpsg);
		String addressText = (lotNumbersList != null && lotNumbersList.size() > 0)
				? lotNumbersList.get(0).getLotNumbers()
				: "";
		baseItem.setLotNumber(addressText);

		String subject = getMailPropValue(MailMessageUtil.KEY_NEGOTIATION_CONFIRMED_NOTIFICATION_SUBJECT, baseItem);
		String body = getMailPropValue(MailMessageUtil.KEY_NEGOTIATION_CONFIRMED_NOTIFICATION_BODY, baseItem);

		for (String departmentId : departmentIdList) {
			List<Department> departmentList = departmentRepository.getDepartmentListById(departmentId);
			if (departmentList.size() == 0) {
				LOGGER.warn("M_部署が存在しません。部署ID：" + departmentId);
				throw new RuntimeException();
			}

			try {
				LOGGER.trace(departmentList.get(0).getAdminMailAddress());
				LOGGER.trace(subject);
				LOGGER.trace(body);

				final String[] mailAddressList = departmentList.get(0).getAdminMailAddress().split(",");
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
	 * 担当課に申請受付通知（事前協議の場合）
	 * 
	 * @param applicationId                 申請ID
	 * @param applicationStepId             申請段階ID
	 * @param applicationVersionInformation 申請版情報
	 * @param comment                       申請受付コメント
	 * 
	 */
	private void sendAcceptMailToGovernmentUser(Integer applicationId, Integer applicationStepId,
			ApplicationVersionInformation applicationVersionInformation, String comment) {
		MailItem baseItem = new MailItem();

		// 申請者情報取得
		List<ApplicantInformation> applicantList = applicantInformationRepository.getApplicantList(applicationId,
				CONTACT_ADDRESS_INVALID);
		if (applicantList.size() < 1) {
			LOGGER.error("申請者データの件数が不正");
			throw new RuntimeException("申請者データの件数が不正");
		}
		ApplicantInformation applicant = applicantList.get(0);
		switch (applicantNameItemNumber) { // 氏名はプロパティで設定されたアイテムを設定
		case 1:
			baseItem.setName(applicant.getItem1());
			break;
		case 2:
			baseItem.setName(applicant.getItem2());
			break;
		case 3:
			baseItem.setName(applicant.getItem3());
			break;
		case 4:
			baseItem.setName(applicant.getItem4());
			break;
		case 5:
			baseItem.setName(applicant.getItem5());
			break;
		case 6:
			baseItem.setName(applicant.getItem6());
			break;
		case 7:
			baseItem.setName(applicant.getItem7());
			break;
		case 8:
			baseItem.setName(applicant.getItem8());
			break;
		case 9:
			baseItem.setName(applicant.getItem9());
			break;
		case 10:
			baseItem.setName(applicant.getItem10());
			break;
		default:
			LOGGER.error("氏名アイテム番号指定が不正: " + applicantNameItemNumber);
			throw new RuntimeException("氏名アイテム番号指定が不正");
		}
		baseItem.setMailAddress(applicant.getMailAddress()); // メールアドレス

		// 申請情報取得
		Application application = getApplication(applicationId);
		baseItem.setMailAddress(applicant.getMailAddress()); // メールアドレス
		baseItem.setApplicationId(application.getApplicationId().toString()); // 申請ID
		baseItem.setApplicationTypeName(getApplicationTypeName(application.getApplicationTypeId())); // 申請種類名
		baseItem.setApplicationStepName(getApplicationStepName(applicationStepId)); // 申請段階名
		Integer versionInformation = applicationVersionInformation.getVersionInformation();
		baseItem.setVersionInformation(versionInformation.toString()); // 版情報

		// 地番
		ApplicationDao applicationDao = new ApplicationDao(emf);
		List<ApplyLotNumber> lotNumbersList = applicationDao.getApplyingLotNumberList(applicationId, lonlatEpsg);
		String addressText = (lotNumbersList != null && lotNumbersList.size() > 0)
				? lotNumbersList.get(0).getLotNumbers()
				: "";
		baseItem.setLotNumber(addressText);

		Calendar now = Calendar.getInstance();
		// 申請月
		baseItem.setApplicationMonth(Integer.valueOf(now.get(Calendar.MONTH) + 1).toString());
		// 申請日
		baseItem.setApplicationDay(Integer.valueOf(now.get(Calendar.DAY_OF_MONTH)).toString());

		// 対象・判定結果
		AnswerDao answerDao = new AnswerDao(emf);

		// 部署別の対象・判定結果リスト
		Map<String, List<MailResultItem>> resultMap = new HashMap<String, List<MailResultItem>>();
		Map<String, Integer> answerDaysMap = new HashMap<String, Integer>();
		// 部署別の申請ファイル変更内容リスト
		Map<String, List<MailResultItem>> applicationFileMap = new HashMap<String, List<MailResultItem>>();

		// 受付の通知部署IDリスト
		List<String> acceptDepartmentIdList = new ArrayList<String>();
		// 申請ファイル変更の通知部署IDリスト
		List<String> fileChangedDepartmentIdList = new ArrayList<String>();

		// 部署ごとの処理済み判定項目IDリスト（同一判定項目が複数行結果が生成する場合、重複な対象・判定結果をリストに追加するのを避ける用）
		Map<String, List<String>> departmentJudgementIdMap = new HashMap<String, List<String>>();

		// 受付したO_受付回答一覧
		List<AcceptingAnswer> acceptingAnswerList = acceptingAnswerRepository.findByVersionInfomation(applicationId,
				applicationStepId, versionInformation);

		// 申請受付実施前の受付版情報
		Integer acceptVersionInfomation = applicationVersionInformation.getAcceptVersionInformation();

		for (AcceptingAnswer acceptingAnswer : acceptingAnswerList) {

			// 対象・判定結果
			MailResultItem resultItem = new MailResultItem();
			Integer answerDays = 0;
			if (acceptingAnswer.getJudgementId() == null || EMPTY.equals(acceptingAnswer.getJudgementId())) {
				resultItem.setTarget(govermentAddAnswerTitle); // 対象
				resultItem.setResult(EMPTY); // 判定結果
			} else {
				Integer acceptingAnswerId = acceptingAnswer.getAcceptingAnswerId();
				List<CategoryJudgementResult> categoryList = answerDao
						.getJudgementResultByAcceptingAnswerId(acceptingAnswerId);
				if (categoryList.size() == 0) {
					LOGGER.warn("M_判定結果の値が取得できない 受付回答ID: " + acceptingAnswerId);
					continue;
				}

				CategoryJudgementResult category = categoryList.get(0);

				if (category.getAnswerDays() != null) {
					answerDays = category.getAnswerDays();
				}

				resultItem.setTarget(category.getTitle()); // 対象
				resultItem.setResult(acceptingAnswer.getJudgementResult()); // 判定結果
			}

			// 回答に紐づく部署
			String departmentId = acceptingAnswer.getDepartmentId();
			if (!resultMap.containsKey(departmentId)) {
				// 初期化
				resultMap.put(departmentId, new ArrayList<MailResultItem>());
			}
			// 部署ごとの処理済み判定項目IDリストに存在しない場合、対象・判定結果を部署ごとに集約
			if (!departmentJudgementIdMap.containsKey(departmentId)) {
				// 初期化：部署の処理済み判定項目リスト
				departmentJudgementIdMap.put(departmentId, new ArrayList<String>());
				resultMap.get(departmentId).add(resultItem);
			} else {
				List<String> judgementIdList = departmentJudgementIdMap.get(departmentId);
				if (!judgementIdList.contains(acceptingAnswer.getJudgementId())) {
					resultMap.get(departmentId).add(resultItem);
				}
			}

			// 処理済み判定項目IDを追加
			departmentJudgementIdMap.get(departmentId).add(acceptingAnswer.getJudgementId());

			if (!answerDaysMap.containsKey(departmentId)) {
				// 初期化
				answerDaysMap.put(departmentId, answerDays);
			} else {
				if (answerDays.compareTo(answerDaysMap.get(departmentId)) > 0) {
					answerDaysMap.replace(departmentId, answerDays);
				}
			}

			// 受付通知行政担当課
			if (!acceptDepartmentIdList.contains(departmentId)) {
				acceptDepartmentIdList.add(departmentId);
			}

		}

		// 受付版情報が0以上の場合、事前協議⇒事前協議として、申請ファイルの案内文を編集
		if (acceptVersionInfomation.compareTo(0) > 0) {

			// 受付中の版番号に対する申請ファイルを取得
			List<ApplicationFile> applicationFileList = applicationFileRepository
					.getApplicationFilesByVersionInformation(applicationId, applicationStepId, versionInformation);

			// 申請ファイルマスタIDリスト
			List<String> applicationFileMasterIdList = new ArrayList<String>();
			// 申請ファイルマスタID-申請ファイル修正内容リスト
			Map<String, Map<String, String>> applicationFileMasterMap = new HashMap<String, Map<String, String>>();

			// 申請ファイルが差し替えた申請ファイルマスタIDリスト
			for (ApplicationFile applicationFile : applicationFileList) {
				if (applicationFile.getDirectionDepartment() != null
						&& !EMPTY.equals(applicationFile.getDirectionDepartment())
						&& !applicationReportFileId.equals(applicationFile.getApplicationFileId())) {
					if (!applicationFileMasterMap.containsKey(applicationFile.getApplicationFileId())) {
						Map<String, String> fileContentMap = new HashMap<String, String>();
						fileContentMap.put("directionDepartment",
								getDirectionDepartmentNameString(applicationFile.getDirectionDepartment()));
						fileContentMap.put("reviseContent", applicationFile.getReviseContent());
						applicationFileMasterMap.put(applicationFile.getApplicationFileId(), fileContentMap);
						applicationFileMasterIdList.add(applicationFile.getApplicationFileId());
					}
				}
			}

			// O_回答から回答担当部署一覧を取得
			List<String> allDepartmentIdList = answerRepository.getDepartmentIdList(applicationId, applicationStepId);

			for (String departmentId : acceptDepartmentIdList) {
				if (!allDepartmentIdList.contains(departmentId)) {
					allDepartmentIdList.add(departmentId);
				}
			}

			// 部署ごとの差し替えた申請ファイルリスト生成
			for (String departmentId : allDepartmentIdList) {

				List<ApplicationFileMaster> applicationFileMasterList = answerDao.getApplicationFileMasterList(
						applicationId, applicationStepId, versionInformation, departmentId,
						applicationFileMasterIdList);

				for (ApplicationFileMaster file : applicationFileMasterList) {

					// O_申請ファイルを基に、作成した申請ファイルマスタID-申請ファイルリストに存在しない場合、スキップ
					if (!applicationFileMasterMap.containsKey(file.getApplicationFileId())) {
						continue;
					}

					// 申請ファイル変更内容
					MailResultItem applicationFileItem = new MailResultItem();
					applicationFileItem.setApplicationFileMasterName(file.getUploadFileName());

					Map<String, String> fileContentMap = applicationFileMasterMap.get(file.getApplicationFileId());

					// 指示元担当課
					String directionDepartment = EMPTY;
					if (fileContentMap.get("directionDepartment") != null) {
						directionDepartment = fileContentMap.get("directionDepartment");
					}

					// 修正内容
					String reviseContent = EMPTY;
					if (fileContentMap.get("reviseContent") != null) {
						reviseContent = fileContentMap.get("reviseContent");
					}

					applicationFileItem.setDirectionDepartmentNames(directionDepartment);
					applicationFileItem.setReviseContent(reviseContent);

					if (!applicationFileMap.containsKey(departmentId)) {
						// 初期化
						applicationFileMap.put(departmentId, new ArrayList<MailResultItem>());
					}
					applicationFileMap.get(departmentId).add(applicationFileItem);

				}

				// 差し替えたファイルがあり、受付条項がないの部署であるか判断
				if (applicationFileMap.containsKey(departmentId) && applicationFileMap.get(departmentId).size() > 0) {
					if (!acceptDepartmentIdList.contains(departmentId)) {
						fileChangedDepartmentIdList.add(departmentId);
					}
				}
			}
		}

		// 行政側の各担当課に申請受付通知送付
		for (String departmentId : acceptDepartmentIdList) {
			if (!resultMap.containsKey(departmentId)) {
				// 対象・判定結果がないのでスキップ
				continue;
			}

			MailItem governmentItem = baseItem.clone();
			// 部署ごとに対象・判定結果は異なるのでここで設定
			governmentItem.setResultList(resultMap.get(departmentId));
			governmentItem.setAnswerDays(answerDaysMap.get(departmentId).toString());// 回答日数

			// 統括部署管理者の受付確認コメント
			MailItem acceptCommentItem = new MailItem();
			acceptCommentItem.setComment(comment);
			String acceptContentText = getMailPropValue(MailMessageUtil.KEY_ACCEPT_BODY_ACCEPT_CONTENT,
					acceptCommentItem);
			governmentItem.setAcceptContent(acceptContentText);

			// 申請ファイル変更案内
			if (applicationFileMap.containsKey(departmentId)) {
				MailItem applicationFileItem = new MailItem();
				applicationFileItem.setResultList(applicationFileMap.get(departmentId));
				String applicationFileChangedContentText = getMailPropValue(
						MailMessageUtil.KEY_ACCEPT_BODY_APPLICATION_FILE_CHANGED_CONTENT, applicationFileItem);
				governmentItem.setApplicationFileChangedContent(applicationFileChangedContentText);
			} else {
				governmentItem.setApplicationFileChangedContent(EMPTY);
			}

			String subject = getMailPropValue(MailMessageUtil.KEY_ACCEPT_SUBJECT, governmentItem);
			String body = getMailPropValue(MailMessageUtil.KEY_ACCEPT_BODY, governmentItem);

			List<Department> departmentList = departmentRepository.getDepartmentListById(departmentId);
			if (departmentList.size() == 0) {
				LOGGER.warn("M_部署情報が存在しません。部署ID：" + departmentId);
				throw new RuntimeException();
			}

			LOGGER.trace(departmentList.get(0).getMailAddress());
			LOGGER.trace(subject);
			LOGGER.trace(body);

			try {
				final String[] mailAddressList = departmentList.get(0).getMailAddress().split(",");
				for (String aMailAddress : mailAddressList) {
					mailSendutil.sendMail(aMailAddress, subject, body);
				}
			} catch (Exception e) {
				LOGGER.error("メール送信時にエラー発生", e);
				throw new RuntimeException(e);
			}
		}
		// 行政側の各担当課に申請提出書類変更通知送付
		for (String departmentId : fileChangedDepartmentIdList) {
			if (!applicationFileMap.containsKey(departmentId)) {
				// 申請ファイル変更案内がないのでスキップ
				continue;
			}

			MailItem governmentItem = baseItem.clone();

			// 申請ファイル変更案内
			governmentItem.setResultList(applicationFileMap.get(departmentId));

			String subject = getMailPropValue(MailMessageUtil.KEY_APPLICATION_FILE_CHANGE_SUBJECT, governmentItem);
			String body = getMailPropValue(MailMessageUtil.KEY_APPLICATION_FILE_CHANGE_BODY, governmentItem);

			List<Department> departmentList = departmentRepository.getDepartmentListById(departmentId);
			if (departmentList.size() == 0) {
				LOGGER.warn("M_部署情報が存在しません。部署ID：" + departmentId);
				throw new RuntimeException();
			}

			LOGGER.trace(departmentList.get(0).getMailAddress());
			LOGGER.trace(subject);
			LOGGER.trace(body);

			try {
				final String[] mailAddressList = departmentList.get(0).getMailAddress().split(",");
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
	 * 事業者に事前協議差戻通知
	 * 
	 * @param applicationId      申請ID
	 * @param applicationStepId  申請段階ID
	 * @param versionInformation 版情報
	 * @param acceptCommentText  申請受付・差戻コメント
	 * 
	 */
	private void sendRemandMailToBusinessUser(Integer applicationId, Integer applicationStepId,
			Integer versionInformation, String acceptCommentText) {
		MailItem item = new MailItem();

		// 申請者連絡先取得
		List<ApplicantInformation> applicantList = applicantInformationRepository.getApplicantList(applicationId,
				CONTACT_ADDRESS_VALID);
		if (applicantList.size() < 1) {
			LOGGER.error("申請者データの件数が不正");
			throw new RuntimeException("申請者データの件数が不正");
		}
		ApplicantInformation applicant = applicantList.get(0);

		// 申請情報取得
		Application application = getApplication(applicationId);
		item.setApplicationId(applicationId.toString()); // 申請ID
		item.setApplicationTypeName(getApplicationTypeName(application.getApplicationTypeId())); // 申請種類名
		item.setApplicationStepName(getApplicationStepName(applicationStepId)); // 申請段階名
		item.setVersionInformation(versionInformation.toString()); // 版情報

		item.setComment(acceptCommentText);// コメント;

		// 事業者に事前協議差戻通知送付
		String subject = getMailPropValue(MailMessageUtil.KEY_APPLICATION_REMAND_SUBJECT, item);
		String body = getMailPropValue(MailMessageUtil.KEY_APPLICATION_REMAND_BODY, item);
		LOGGER.trace(applicant.getMailAddress());
		LOGGER.trace(subject);
		LOGGER.trace(body);

		try {
			mailSendutil.sendMail(applicant.getMailAddress(), subject, body);
		} catch (Exception e) {
			LOGGER.error("メール送信時にエラー発生", e);
			throw new RuntimeException(e);
		}

	}

	/**
	 * 複数の部署ID（カンマ区切り）から部署名取得
	 * @param departmentIds
	 * @return
	 */
	private String getDirectionDepartmentNameString(String departmentIds) {

		String departmentNameText = EMPTY;

		if (departmentIds != null) {

			String[] departmentIdList = departmentIds.split(COMMA);
			for (String departmentId : departmentIdList) {

				List<Department> departmentList = departmentRepository.getDepartmentListById(departmentId);
				if (departmentList.size() == 0) {
					LOGGER.warn("M_部署情報が存在しません。部署ID：" + departmentId);
					throw new RuntimeException();
				}
				String departmentName = departmentList.get(0).getDepartmentName();

				if (EMPTY.equals(departmentNameText)) {
					departmentNameText += departmentName;
				} else {
					departmentNameText += COMMA + departmentName;
				}
			}
		}

		return departmentNameText;
	}

	/**
	 * boolean が同じか判断
	 * 
	 * @param value
	 * @return
	 */
	private boolean compareToBoolean(Boolean value1, Boolean value2) {

		boolean flag = true;

		if (value1 == null) {

			if (value2 == null) {
				flag = true;
			} else {
				flag = false;
			}
		} else {
			if (value2 == null) {
				flag = false;
			} else {
				if ((value1 && value2) || (!value1 && !value2)) {
					flag = true;
				} else {
					flag = false;
				}
			}
		}

		return flag;
	}


	/**
	 * 回答通知のログデータ編集
	 * 
	 * @param answerNotifyRequestForm 回答通知リクエストフォーム
	 * @param accessId                アクセスID
	 * @param loginId                 操作ユーザー
	 * @param departmentName          操作ユーザー所属部署名
	 * @return ログデータ
	 */
	public Object[] editAnswerNotificationLogData(AnswerNotifyRequestForm answerNotifyRequestForm, String accessId,
			String loginId, String departmentName) {

		// 回答通知種類
		String notifyType = answerNotifyRequestForm.getNotifyType();
		// 申請ID
		Integer applicationId = answerNotifyRequestForm.getApplicationId();
		// 申請段階ID
		Integer applicationStepId = answerNotifyRequestForm.getApplicationStepId();

		// 申請情報
		Application application = getApplication(applicationId);
		// 申請種類名
		String applicationTypeName = getApplicationTypeName(application.getApplicationTypeId());
		// 申請段階名
		String applicationStepName = getApplicationStepName(applicationStepId);

		// O_申請版情報
		List<ApplicationVersionInformation> applicationVersionInformationList = applicationVersionInformationRepository
				.findByApplicationSteId(applicationId, applicationStepId);
		if (applicationVersionInformationList.size() == 0) {
			LOGGER.warn("O_申請版情報が存在しません。");
			throw new RuntimeException();
		}
		// 版情報
		Integer versionInformation = applicationVersionInformationList.get(0).getVersionInformation();
		// 受付版情報
		Integer acceptVersionInformation = applicationVersionInformationList.get(0).getAcceptVersionInformation();

		// 版情報(ログデータ)
		String versionInformationStr = EMPTY;
		// 通知種別(ログデータ)
		String notifyTypeStr = EMPTY;

		// 申請段階、通知種類より、各種操作ログを出力
		if (APPLICATION_STEP_ID_2.equals(applicationStepId)) {
			// ■事業者へ回答通知のログを出力
			if (NOTIFY_TYPE_0_ANSWERED.equals(notifyType)) {
				versionInformationStr = acceptVersionInformation.toString();
				notifyTypeStr = getAnswerNotifyTypeLabel(NOTIFY_TYPE_0_ANSWERED);
			}

			// ■事業者へ差戻通知のログを出力
			if (NOTIFY_TYPE_1_REMANDED.equals(notifyType)) {
				versionInformationStr = versionInformation.toString();
				notifyTypeStr = getAnswerNotifyTypeLabel(NOTIFY_TYPE_1_REMANDED);
			}

			// ■各担当課に受付通知のログを出力
			if (NOTIFY_TYPE_2_ACCEPTED.equals(notifyType)) {
				versionInformationStr = versionInformation.toString();
				notifyTypeStr = getAnswerNotifyTypeLabel(NOTIFY_TYPE_2_ACCEPTED);
			}

			// ■統括部署管理者に回答許可通知のログを出力
			if (NOTIFY_TYPE_3_ANSWER_PERMISSION.equals(notifyType)) {
				versionInformationStr = acceptVersionInformation.toString();
				notifyTypeStr = getAnswerNotifyTypeLabel(NOTIFY_TYPE_3_ANSWER_PERMISSION);
			}

			// ■統括部署管理者に行政確定登録許可通知のログを出力
			if (NOTIFY_TYPE_4_GOVERNMENT_CONFIRM_PERMISSION.equals(notifyType)) {
				versionInformationStr = acceptVersionInformation.toString();
				notifyTypeStr = getAnswerNotifyTypeLabel(NOTIFY_TYPE_4_GOVERNMENT_CONFIRM_PERMISSION);
			}

		} else {
			// ■事業者へ回答通知のログを出力
			versionInformationStr = acceptVersionInformation.toString();
			notifyTypeStr = getAnswerNotifyTypeLabel(NOTIFY_TYPE_0_ANSWERED);
		}

		// ログデータ
		Object[] logData = { accessId, LogUtil.localDateTimeToString(LocalDateTime.now()), loginId, departmentName,
				applicationId, applicationTypeName, applicationStepName, versionInformationStr, notifyTypeStr };

		return logData;

	}

	/**
	 * 回答登録の操作ログを出力
	 * 
	 * @param answerForm               回答フォーム
	 * @param applicationId            申請ID
	 * @param applicationStepId        申請段階ID
	 * @param accessId                 アクセスID
	 * @param loginId                  操作ユーザー
	 * @param departmentName           操作ユーザー所属部署名
	 * @param logType                  ログ種別<br>
	 *                                 ※1:回答登録（回答内容変更)のログ<br>
	 *                                 ※2:行政確定登録のログ<br>
	 *                                 ※3:同意項目承認否認登録（事業者）のログ<br>
	 * 
	 * @param updatetype               操作種別（ログ種別が回答登録のみ）<br>
	 *                                 ※1:事前相談回答登録、事前協議回答登録、許可判定回答登録<br>
	 *                                 ※2:行政回答追加登録<br>
	 *                                 ※3:行政回答削除<br>
	 * 
	 * @param answerVersionInformation O_回答の版情報
	 * 
	 * 
	 */
	public void outputAnswerRegistLogToCsv(AnswerForm answerForm, Integer applicationId, Integer applicationStepId,
			String accessId, String loginId, String departmentName, String logType, String updatetype,
			String answerVersionInformation) {

		// 申請情報
		Application application = getApplication(applicationId);
		// 申請種類名
		String applicationTypeName = getApplicationTypeName(application.getApplicationTypeId());
		// 申請段階名
		String applicationStepName = getApplicationStepName(applicationStepId);

		// O_申請版情報
		List<ApplicationVersionInformation> applicationVersionInformationList = applicationVersionInformationRepository
				.findByApplicationSteId(applicationId, applicationStepId);
		if (applicationVersionInformationList.size() == 0) {
			LOGGER.warn("O_申請版情報が存在しません。");
			throw new RuntimeException();
		}
		// 版情報
		Integer versionInformation = applicationVersionInformationList.get(0).getVersionInformation();
		// タイトル
		String title = answerForm.getJudgementInformation().getTitle();

		// 版情報
		String versionInformationStr = versionInformation.toString();
		// 事前協議の場合、O_回答の版情報を使ています。
		if (APPLICATION_STEP_ID_2.equals(applicationStepId)) {
			versionInformationStr = answerVersionInformation;
		}

		try {
			// ■回答登録（回答内容変更)のログを出力
			if ("1".equals(logType)) {

				// 操作種別のラベル
				String updateTypeLabel = getAnswerRegisterUpdateTypeLabel(updatetype);

				// 操作種別が2：行政回答追加の場合、タイトルが固定文言で設定
				if ("2".equals(updatetype)) {
					title = govermentAddAnswerTitle;
				}

				Object[] logData = { accessId, LogUtil.localDateTimeToString(LocalDateTime.now()), loginId,
						departmentName, applicationId, applicationTypeName, applicationStepName, versionInformationStr,
						updateTypeLabel, answerForm.getAnswerId(), title, answerForm.getAnswerContent() };
				LogUtil.writeLogToCsv(answerRegisterLogPath, answerRegisterLogHeader, logData);
			}

			// ■回答登録（行政確定登録内容登録（事前協議のみ）)のログを出力
			if ("2".equals(logType)) {

				// 行政確定登録：ステータス
				String governmentConfirmStatus = EMPTY;
				if (answerForm.getGovernmentConfirmStatus() == null) {
					governmentConfirmStatus = EMPTY;
				} else {
					if (GOVERNMENT_CONFIRM_STATUS_0_AGREE.equals(answerForm.getGovernmentConfirmStatus())) {
						governmentConfirmStatus = GOVERNMENT_CONFIRM_STATUS_0_AGREE_NAME;
					}
					if (GOVERNMENT_CONFIRM_STATUS_1_WITHDRAW.equals(answerForm.getGovernmentConfirmStatus())) {
						governmentConfirmStatus = GOVERNMENT_CONFIRM_STATUS_1_WITHDRAW_NAME;
					}
					if (GOVERNMENT_CONFIRM_STATUS_2_REJECT.equals(answerForm.getGovernmentConfirmStatus())) {
						governmentConfirmStatus = GOVERNMENT_CONFIRM_STATUS_2_REJECT_NAME;
					}
				}

				// 行政確定登録：日付
				String governmentConfirmDatetime = EMPTY;
				if (answerForm.getGovernmentConfirmDatetime() != null) {
					governmentConfirmDatetime = answerForm.getGovernmentConfirmDatetime().replace("-", "/");
				}

				// 行政確定登録：コメント
				String governmentConfirmComment = EMPTY;
				if (answerForm.getGovernmentConfirmComment() != null) {
					governmentConfirmComment = answerForm.getGovernmentConfirmComment();
				}

				Object[] logData = { accessId, LogUtil.localDateTimeToString(LocalDateTime.now()), loginId,
						departmentName, applicationId, applicationTypeName, applicationStepName, versionInformationStr,
						answerForm.getAnswerId(), title, governmentConfirmStatus, governmentConfirmDatetime,
						governmentConfirmComment };
				LogUtil.writeLogToCsv(answerRegisterConfirmLogPath, answerRegisterConfirmLogHeader, logData);
			}

			// ■同意項目承認否認登録（事業者（事前協議のみ）のログを出力
			if ("3".equals(logType)) {
				// 事業者合否ステータス
				String businessPassStatus = EMPTY;
				if (answerForm.getBusinessPassStatus() == null) {
					businessPassStatus = EMPTY;
				} else {
					if (BUSINESS_PASS_STATUS_1_AGREE.equals(answerForm.getBusinessPassStatus())) {
						businessPassStatus = BUSINESS_PASS_STATUS_1_AGREE_NAME;
					}
				}

				// 事業者回答登録日時
				String businessAnswerDatetime = EMPTY;
				if (answerForm.getBusinessAnswerDatetime() != null) {
					businessAnswerDatetime = answerForm.getBusinessAnswerDatetime().replace("-", "/");
				}

				Object[] logData = { accessId, LogUtil.localDateTimeToString(LocalDateTime.now()), applicationId,
						applicationTypeName, applicationStepName, versionInformationStr, answerForm.getAnswerId(),
						title, businessPassStatus, businessAnswerDatetime };
				LogUtil.writeLogToCsv(answerConsentInoutLogPath, answerConsentInoutLogHeader, logData);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * 回答登録(部署全体の行政確定登録内容登録（事前協議のみ）)ログ出力
	 * 
	 * @param departmentAnswer  部署回答フォーム
	 * @param applicationId     申請ID
	 * @param applicationStepId 申請段階ID
	 * @param accessId          アクセスID
	 * @param loginId           操作ユーザー
	 * @param departmentName    操作ユーザー所属部署
	 */
	public void outputDepartmentAnswerLogToCsv(DepartmentAnswerForm departmentAnswer, Integer applicationId,
			Integer applicationStepId, String accessId, String loginId, String departmentName) {

		// 申請情報
		Application application = getApplication(applicationId);
		// 申請種類名
		String applicationTypeName = getApplicationTypeName(application.getApplicationTypeId());
		// 申請段階名
		String applicationStepName = getApplicationStepName(applicationStepId);

		// O_申請版情報
		List<ApplicationVersionInformation> applicationVersionInformationList = applicationVersionInformationRepository
				.findByApplicationSteId(applicationId, applicationStepId);
		if (applicationVersionInformationList.size() == 0) {
			LOGGER.warn("O_申請版情報が存在しません。");
			throw new RuntimeException();
		}
		// 受付版情報
		Integer acceptVersionInformation = applicationVersionInformationList.get(0).getAcceptVersionInformation();
		// 版情報
		String versionInformationStr = acceptVersionInformation.toString();

		// 回答対象部署
		String answerDepartmentName = departmentAnswer.getDepartment().getDepartmentName();

		try {

			// ■回答登録（行政確定登録内容登録（事前協議のみ）)のログを出力

			// 行政確定登録：ステータス
			String governmentConfirmStatus = EMPTY;
			if (departmentAnswer.getGovernmentConfirmStatus() == null) {
				governmentConfirmStatus = EMPTY;
			} else {
				if (GOVERNMENT_CONFIRM_STATUS_0_AGREE.equals(departmentAnswer.getGovernmentConfirmStatus())) {
					governmentConfirmStatus = GOVERNMENT_CONFIRM_STATUS_0_AGREE_NAME;
				}
				if (GOVERNMENT_CONFIRM_STATUS_1_WITHDRAW.equals(departmentAnswer.getGovernmentConfirmStatus())) {
					governmentConfirmStatus = GOVERNMENT_CONFIRM_STATUS_1_WITHDRAW_NAME;
				}
				if (GOVERNMENT_CONFIRM_STATUS_2_REJECT.equals(departmentAnswer.getGovernmentConfirmStatus())) {
					governmentConfirmStatus = GOVERNMENT_CONFIRM_STATUS_2_REJECT_NAME;
				}
			}

			// 行政確定登録：日付
			String governmentConfirmDatetime = EMPTY;
			if (departmentAnswer.getGovernmentConfirmDatetime() != null) {
				governmentConfirmDatetime = departmentAnswer.getGovernmentConfirmDatetime().replace("-", "/");
			}

			// 行政確定登録：コメント
			String governmentConfirmComment = EMPTY;
			if (departmentAnswer.getGovernmentConfirmComment() != null) {
				governmentConfirmComment = departmentAnswer.getGovernmentConfirmComment();
			}

			// ログデータ
			Object[] logData = { accessId, LogUtil.localDateTimeToString(LocalDateTime.now()), loginId, departmentName,
					applicationId, applicationTypeName, applicationStepName, versionInformationStr,
					departmentAnswer.getDepartmentAnswerId(), answerDepartmentName, governmentConfirmStatus,
					governmentConfirmDatetime, governmentConfirmComment };

			// CSVファイルに出力
			LogUtil.writeLogToCsv(departmentAnswerRegisterConfirmLogPath, departmentAnswerRegisterConfirmLogHeader,
					logData);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * 回答内容確認（事業者）のログデータ編集
	 * 
	 * @param accessId      アクセスID
	 * @param applicationId 申請ID
	 * @return
	 */
	public Object[] editApplicationConfirmLogData(String accessId, Integer applicationId) {

		// 申請情報
		Application application = getApplication(applicationId);
		// 申請種類名
		String applicationTypeName = getApplicationTypeName(application.getApplicationTypeId());

		// O_申請版情報
		List<ApplicationVersionInformation> applicationVersionInformationList = applicationVersionInformationRepository
				.findByApplicationId(applicationId);
		if (applicationVersionInformationList.size() == 0) {
			LOGGER.warn("O_申請版情報が存在しません。");
			throw new RuntimeException();
		}
		ApplicationVersionInformation applicationVersionInformation = applicationVersionInformationList.get(0);

		// 申請段階
		Integer applicationStepId = applicationVersionInformation.getApplicationStepId();

		// 版情報
		Integer versionInformation = applicationVersionInformation.getVersionInformation();

		// 事前協議
		if (APPLICATION_STEP_ID_2.equals(applicationVersionInformation.getApplicationStepId())) {

			// 受付版情報が0場合、
			if (applicationVersionInformation.getAcceptVersionInformation().compareTo(0) == 0) {
				for (ApplicationVersionInformation entity : applicationVersionInformationList) {
					if (APPLICATION_STEP_ID_1.equals(entity.getApplicationStepId())) {
						applicationStepId = entity.getApplicationStepId();
						versionInformation = entity.getVersionInformation();
					}
				}
			} else {
				applicationStepId = applicationVersionInformation.getApplicationStepId();
				versionInformation = applicationVersionInformation.getAcceptVersionInformation();
			}
		}

		// 申請段階名
		String applicationStepName = getApplicationStepName(applicationStepId);

		// ログデータ：アクセスID、アクセス日時、申請ID、申請種類、申請段階、版情報

		Object[] logData = { accessId, LogUtil.localDateTimeToString(LocalDateTime.now()), applicationId,
				applicationTypeName, applicationStepName, versionInformation.toString() };

		return logData;
	}

	/**
	 * 回答通知の通知種別定義（ログ出力用）から指定ラベルを取得
	 * 
	 * @return 通知種別定義の指定ラベル
	 * @throws Exception 例外
	 */
	protected String getAnswerNotifyTypeLabel(String notifyType) {
		LOGGER.trace("回答通知の通知種別定義取得 開始");
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			Map<String, String> map = objectMapper.readValue(answerNotifyTypeJson,
					new TypeReference<LinkedHashMap<String, String>>() {
					});

			if (map.containsKey(notifyType)) {
				return map.get(notifyType);
			} else {
				return EMPTY;
			}
		} catch (Exception e) {
			LOGGER.error("回答通知の通知種別定義取得に失敗", e);
			return EMPTY;
		} finally {
			LOGGER.trace("回答通知の通知種別定義取得 終了");
		}
	}

	/**
	 * 回答登録の操作種別定義から指定ラベルを取得
	 * 
	 * @return 操作種別定義
	 * @throws Exception 例外
	 */
	protected String getAnswerRegisterUpdateTypeLabel(String updateType) {
		LOGGER.trace("回答登録の操作種別定義取得 開始");
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			Map<String, String> map = objectMapper.readValue(answerRegisterUpdateTypeJson,
					new TypeReference<LinkedHashMap<String, String>>() {
					});
			if (map.containsKey(updateType)) {
				return map.get(updateType);
			} else {
				return EMPTY;
			}
		} catch (Exception e) {
			LOGGER.error("回答登録の操作種別定義取得に失敗", e);
			return null;
		} finally {
			LOGGER.trace("回答登録の操作種別定義取得 終了");
		}
	}

	/**
	 * 回答通知時に、行政向け回答レポート作成(事前相談のみ)
	 * 
	 * @param applicationId         申請ID
	 * @param applicationStepId     申請段階ID
	 * @param answerNotifiedTextMap 回答ID-通知テキストマップ
	 */
	private void createAnswerReport(Integer applicationId, Integer applicationStepId,
			Map<Integer, String> answerNotifiedTextMap) {

		LOGGER.trace("回答レポート生成の準備データ取得 開始");
		// ■回答レポートのテンプレート取得
		// 事前相談申請を登録するときに、作成された概況診断レポート一覧を取得する
		List<ApplicationFile> applicationFiles = applicationFileRepository
				.getApplicationFilesSortByVer(applicationReportFileId, applicationId, applicationStepId);

		// 最終版の概況診断レポート
		ApplicationFile applicationFile = applicationFiles.get(0);
		// 絶対ファイルパス
		String absoluteFilePath = fileRootPath + applicationFile.getFilePath();
		Path tempFilePath = Paths.get(absoluteFilePath);
		if (!Files.exists(tempFilePath)) {
			// ファイルが存在しない
			LOGGER.warn("ファイルが存在しない");
			throw new RuntimeException("概況診断レポート取得に失敗　申請ID：" + applicationId);
		}

		// ■版情報取得
		Integer versionInformation = 0;
		LOGGER.trace("O_申請版情報取得 開始");
		List<ApplicationVersionInformation> applicationVersionInformationList = applicationVersionInformationRepository
				.findByApplicationSteId(applicationId, applicationStepId);

		if (applicationVersionInformationList.size() == 0) {
			LOGGER.warn("O_申請版情報が存在しません。");
			throw new RuntimeException();
		}

		ApplicationVersionInformation applicationVersionInformation = applicationVersionInformationList.get(0);
		if (STATE_PROVISIONAL.equals(applicationVersionInformation.getRegisterStatus())) {
			versionInformation = applicationVersionInformation.getVersionInformation() - 1;
		} else {
			versionInformation = applicationVersionInformation.getVersionInformation();
		}

		LOGGER.trace("回答レポート生成の準備データ取得 終了");

		LOGGER.trace("回答レポートファイル生成 開始");
		Workbook wb = null;
		try {
			wb = exportAnswerReportWorkBook(absoluteFilePath, answerNotifiedTextMap, applicationStepId,
					new GeneralConditionDiagnosisReportRequestForm());
			if (wb == null) {
				throw new RuntimeException("回答レポートファイル生成に失敗");
			}
		} catch (Exception ex) {
			LOGGER.error("回答レポートファイル生成でエラー発生", ex);
			throw new RuntimeException(ex);
		}

		LOGGER.trace("回答レポートファイル生成 終了");

		LOGGER.trace("回答レポートアップロード 開始");
		// ファイル名は「回答レポート_<申請ID>_yyyy_MM_dd.xlsx」
		SimpleDateFormat sdf = new SimpleDateFormat(answerReportFileNameFooter);
		String fileName = answerReportFileNameHeader + applicationId + sdf.format(new Date()) + ".xlsx";
		UploadApplicationFileForm uploadForm = new UploadApplicationFileForm();
		uploadForm.setApplicationId(applicationId);
		uploadForm.setApplicationStepId(applicationStepId);
		uploadForm.setApplicationFileId(answerReportFileId);
		uploadForm.setUploadFileName(fileName);
		// 版情報
		uploadForm.setVersionInformation(versionInformation);
		// 拡張子
		uploadForm.setExtension("xlsx");
		uploadApplicationFile(uploadForm, wb);
		LOGGER.trace("回答レポートアップロード 終了");
	}
	
	/**
	 * 申請ファイルアップロード
	 * 
	 * @param form パラメータ
	 */
	public void uploadApplicationFile(UploadApplicationFileForm form, Workbook wb) {
		LOGGER.debug("申請ファイルアップロード 開始");
		try {
			// ファイルパスは「/application/<申請ID>/<申請ファイルID>/<ファイルID>/<申請段階ID>/<版情報>/<アップロードファイル名>」

			// O_申請ファイル登録
			LOGGER.trace("O_申請ファイル登録 開始");
			Integer fileId = applicationFileJdbc.insert(form);
			form.setFileId(fileId);
			LOGGER.trace("O_申請ファイル登録 終了");

			// 相対フォルダパス
			String folderPath = applicationFolderName;
			folderPath += PATH_SPLITTER + form.getApplicationId();
			folderPath += PATH_SPLITTER + form.getApplicationFileId();
			folderPath += PATH_SPLITTER + form.getFileId();
			folderPath += PATH_SPLITTER + form.getApplicationStepId();
			folderPath += PATH_SPLITTER + form.getVersionInformation();

			// 絶対フォルダパス
			String absoluteFolderPath = fileRootPath + folderPath;
			Path directoryPath = Paths.get(absoluteFolderPath);
			if (!Files.exists(directoryPath)) {
				// フォルダがないので生成
				LOGGER.debug("フォルダ生成: " + directoryPath);
				Files.createDirectories(directoryPath);
			}

			// 相対ファイルパス
			String filePath = folderPath + PATH_SPLITTER + form.getUploadFileName();
			// 絶対ファイルパス
			String absoluteFilePath = absoluteFolderPath + PATH_SPLITTER + form.getUploadFileName();

			// ファイルパスはrootを除いた相対パスを設定
			form.setFilePath(filePath);

			// ファイルパス更新
			LOGGER.trace("ファイルパス更新 開始");
			if (applicationFileJdbc.updateFilePath(fileId, filePath) != 1) {
				LOGGER.warn("ファイルパス更新件数が不正");
				throw new RuntimeException("ファイルパス更新件数が不正");
			}
			LOGGER.trace("ファイルパス更新 終了");

			// 修正内容更新
			LOGGER.trace("修正内容更新 開始");
			// 担当課名としたい場合はform.getDirectionDepartmentId()→form.getDirectionDepartment()
			if (applicationFileJdbc.updateReviseContent(fileId, form.getDirectionDepartmentId(),
					form.getReviseContent()) != 1) {
				LOGGER.warn("修正内容更新件数が不正");
				throw new RuntimeException("修正内容更新件数が不正");
			}
			LOGGER.trace("修正内容更新 終了");

			// ファイル出力
			if (form.getUploadFile() != null) {
				LOGGER.trace("ファイル出力 開始");
				exportFile(form.getUploadFile(), absoluteFilePath);
				LOGGER.trace("ファイル出力 終了");
			} else if (wb != null) {
				LOGGER.trace("EXCELファイル出力 開始");
				exportWorkBook(wb, absoluteFilePath);
				LOGGER.trace("EXCELファイル出力 終了");
			} else {
				throw new RuntimeException("ファイル情報が設定されていない");
			}
		} catch (Exception ex) {
			// RuntimeExceptionで投げないとロールバックされない
			LOGGER.error("申請ファイルアップロードで例外発生", ex);
			throw new RuntimeException(ex);
		} finally {
			LOGGER.debug("申請ファイルアップロード 終了");
		}
	}
}
