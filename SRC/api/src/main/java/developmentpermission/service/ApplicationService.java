package developmentpermission.service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.transaction.Transactional;

import org.apache.poi.ss.usermodel.Workbook;
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

import developmentpermission.dao.AnswerDao;
import developmentpermission.dao.ApplicationDao;
import developmentpermission.dao.LotNumberDao;
import developmentpermission.dao.GovernmentUserDao;
import developmentpermission.entity.AcceptingAnswer;
import developmentpermission.entity.Answer;
import developmentpermission.entity.AnswerFile;
import developmentpermission.entity.AnswerHistory;
import developmentpermission.entity.AnswerTemplate;
import developmentpermission.entity.ApplicantInformation;
import developmentpermission.entity.ApplicantInformationAdd;
import developmentpermission.entity.ApplicantInformationItem;
import developmentpermission.entity.ApplicantInformationItemOption;
import developmentpermission.entity.Application;
import developmentpermission.entity.ApplicationCategory;
import developmentpermission.entity.ApplicationCategoryMaster;
import developmentpermission.entity.ApplicationCategorySelectionView;
import developmentpermission.entity.ApplicationFile;
import developmentpermission.entity.ApplicationFileMaster;
import developmentpermission.entity.ApplicationSearchResult;
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
import developmentpermission.entity.GovernmentUserAndAuthority;
import developmentpermission.entity.InquiryAddress;
import developmentpermission.entity.Ledger;
import developmentpermission.entity.LedgerMaster;
import developmentpermission.entity.LotNumber;
import developmentpermission.entity.LotNumberAndDistrict;
import developmentpermission.entity.LotNumberSearchResultDefinition;
import developmentpermission.entity.Message;
import developmentpermission.form.AcceptingAnswerForm;
import developmentpermission.form.AnswerConfirmLoginForm;
import developmentpermission.form.AnswerFileForm;
import developmentpermission.form.AnswerFileHistoryForm;
import developmentpermission.form.AnswerForm;
import developmentpermission.form.AnswerHistoryForm;
import developmentpermission.form.AnswerJudgementForm;
import developmentpermission.form.AnswerTemplateForm;
import developmentpermission.form.ApplicantInformationItemForm;
import developmentpermission.form.ApplicantInformationItemOptionForm;
import developmentpermission.form.ApplicationCategoryForm;
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
import developmentpermission.form.ApplyAnswerDetailForm;
import developmentpermission.form.ApplyLotNumberForm;
import developmentpermission.form.ChatForm;
import developmentpermission.form.ChatRelatedInfoForm;
import developmentpermission.form.DepartmentForm;
import developmentpermission.form.DepartmentAnswerForm;
import developmentpermission.form.GeneralConditionDiagnosisReportRequestForm;
import developmentpermission.form.GeneralConditionDiagnosisResultForm;
import developmentpermission.form.GovernmentUserForm;
import developmentpermission.form.InquiryAddressForm;
import developmentpermission.form.LedgerForm;
import developmentpermission.form.LedgerMasterForm;
import developmentpermission.form.LotNumberForm;
import developmentpermission.form.MessageForm;
import developmentpermission.form.ReApplicationForm;
import developmentpermission.form.ReApplicationRequestForm;
import developmentpermission.form.UploadApplicationFileForm;
import developmentpermission.repository.AcceptingAnswerRepository;
import developmentpermission.repository.AnswerFileRepository;
import developmentpermission.repository.AnswerHistoryRepository;
import developmentpermission.repository.AnswerRepository;
import developmentpermission.repository.AnswerTemplateRepository;
import developmentpermission.repository.ApplicantInformationAddRepository;
import developmentpermission.repository.ApplicantInformationItemOptionRepository;
import developmentpermission.repository.ApplicantInformationItemRepository;
import developmentpermission.repository.ApplicantInformationRepository;
import developmentpermission.repository.ApplicationCategoryMasterRepository;
import developmentpermission.repository.ApplicationCategoryRepository;
import developmentpermission.repository.ApplicationFileMasterRepository;
import developmentpermission.repository.ApplicationFileRepository;
import developmentpermission.repository.ApplicationRepository;
import developmentpermission.repository.ApplicationSearchResultRepository;
import developmentpermission.repository.ApplicationStepRepository;
import developmentpermission.repository.ApplicationTypeRepository;
import developmentpermission.repository.ApplicationVersionInformationRepository;
import developmentpermission.repository.AuthorityRepository;
import developmentpermission.repository.ChatRepository;
import developmentpermission.repository.DepartmentAnswerRepository;
import developmentpermission.repository.InquiryAddressRepository;
import developmentpermission.repository.JudgementAuthorityRepository;
import developmentpermission.repository.JudgementResultRepository;
import developmentpermission.repository.LedgerMasterRepository;
import developmentpermission.repository.LedgerRepository;
import developmentpermission.repository.LotNumberRepository;
import developmentpermission.repository.LotNumberSearchResultDefinitionRepository;
import developmentpermission.repository.MssageRepository;
import developmentpermission.repository.jdbc.AcceptingAnswerJdbc;
import developmentpermission.repository.jdbc.AnswerHistoryJdbc;
import developmentpermission.repository.jdbc.AnswerJdbc;
import developmentpermission.repository.jdbc.ApplicantAddJdbc;
import developmentpermission.repository.jdbc.ApplicantJdbc;
import developmentpermission.repository.jdbc.ApplicationCategoryJdbc;
import developmentpermission.repository.jdbc.ApplicationFileJdbc;
import developmentpermission.repository.jdbc.ApplicationJdbc;
import developmentpermission.repository.jdbc.ApplicationLotNumberJdbc;
import developmentpermission.repository.jdbc.ApplicationVersionInformationJdbc;
import developmentpermission.repository.jdbc.DepartmentAnswerJdbc;
import developmentpermission.util.AuthUtil;
import developmentpermission.util.ExportJudgeForm;
import developmentpermission.util.MailMessageUtil;
import developmentpermission.util.model.MailItem;
import developmentpermission.util.model.MailResultItem;

/**
 * 申請Serviceクラス
 */
@Service
@Transactional
public class ApplicationService extends AbstractJudgementService {

	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationService.class);

	/** 照合IDの文字数 */
	@Value("${app.applicant.id.length:20}")
	protected int applicantIdLength;
	/** パスワード発行時の文字数 */
	@Value("${app.password.length:10}")
	protected int passwordLength;
	/** パスワードに使用する文字種 */
	@Value("${app.password.character}")
	protected String passwordCharacter;

	/** 参照タイプ: 申請区分 */
	@Value("${app.application.result.type.category:0}")
	protected String typeCategory;
	/** 参照タイプ: 申請者情報 */
	@Value("${app.application.result.type.applicant:1}")
	protected String typeApplicant;
	/** 参照タイプ: その他 */
	@Value("${app.application.result.type.other:2}")
	protected String typeOther;

	/** テーブル名: O_申請区分 */
	@Value("${app.application.table.application.category:o_application_category}")
	protected String tableNameApplicationCategory;
	/** テーブル名: M_申請区分 */
	@Value("${app.application.table.application.category.master:m_application_category}")
	protected String tableNameApplicationCategoryMaster;
	/** テーブル名: O_申請者情報 */
	@Value("${app.application.table.applicant.information:o_applicant_information}")
	protected String tableNameApplicantInformation;
	/** テーブル名: O_回答 */
	@Value("${app.application.table.answer:o_answer}")
	protected String tableNameAnswer;
	/** テーブル名: M_区分設定 */
	@Value("${app.application.table.category.judgement:m_category_judgement}")
	protected String tableNameCategoryJudgement;
	/** テーブル名: M_部署 */
	@Value("${app.application.table.department:m_department}")
	protected String tableNameDepartment;
	/** テーブル名： O_申請 */
	@Value("${app.application.table.application}")
	protected String tableNameApplication;
	/** カラム名: O_申請区分 申請ID */
	@Value("${app.application.category.column.applicationid}")
	protected String columnNameApplicationId;
	/** カラム名: O_申請区分 画面ID */
	@Value("${app.application.category.column.viewid}")
	protected String columnNameApplicationViewId;
	/** カラム名: O_申請区分 申請区分ID */
	@Value("${app.application.category.column.categoryid}")
	protected String columnNameApplicationCategoryId;

	/** カラム名: M_申請区分 申請区分ID */
	@Value("${app.application.category.master.column.categoryid}")
	protected String columnNameApplicationCategoryMasterId;
	/** カラム名: M_申請区分 画面ID */
	@Value("${app.application.category.master.column.viewid}")
	protected String columnNameApplicationCategoryMasterViewId;
	/** カラム名: M_申請区分 昇順 */
	@Value("${app.application.category.master.column.order}")
	protected String columnNameApplicationCategoryMasterOrder;
	/** カラム名: M_申請区分 選択肢名 */
	@Value("${app.application.category.master.column.labelname}")
	protected String columnNameApplicationCategoryMasterLabelName;

	/** カラム名: O_申請者情報 申請ID */
	@Value("${app.applicant.information.column.applicationid}")
	protected String columnNameApplicantInformationApplicationId;
	/** カラム名: O_申請者情報 申請者情報ID */
	@Value("${app.applicant.information.column.applicantid}")
	protected String columnNameApplicantInformationApplicantId;
	/** カラム名: O_申請者情報 項目1 */
	@Value("${app.applicant.information.column.item1}")
	protected String columnNameApplicantInformationItem1;
	/** カラム名: O_申請者情報 項目2 */
	@Value("${app.applicant.information.column.item2}")
	protected String columnNameApplicantInformationItem2;
	/** カラム名: O_申請者情報 項目3 */
	@Value("${app.applicant.information.column.item3}")
	protected String columnNameApplicantInformationItem3;
	/** カラム名: O_申請者情報 項目4 */
	@Value("${app.applicant.information.column.item4}")
	protected String columnNameApplicantInformationItem4;
	/** カラム名: O_申請者情報 項目5 */
	@Value("${app.applicant.information.column.item5}")
	protected String columnNameApplicantInformationItem5;
	/** カラム名: O_申請者情報 項目6 */
	@Value("${app.applicant.information.column.item6}")
	protected String columnNameApplicantInformationItem6;
	/** カラム名: O_申請者情報 項目7 */
	@Value("${app.applicant.information.column.item7}")
	protected String columnNameApplicantInformationItem7;
	/** カラム名: O_申請者情報 項目8 */
	@Value("${app.applicant.information.column.item8}")
	protected String columnNameApplicantInformationItem8;
	/** カラム名: O_申請者情報 項目9 */
	@Value("${app.applicant.information.column.item9}")
	protected String columnNameApplicantInformationItem9;
	/** カラム名: O_申請者情報 項目10 */
	@Value("${app.applicant.information.column.item10}")
	protected String columnNameApplicantInformationItem10;
	/** カラム名: O_申請者情報 メールアドレス */
	@Value("${app.applicant.information.column.mailaddress}")
	protected String columnNameApplicantInformationMailAddress;
	/** カラム名: O_申請者情報 照合ID */
	@Value("${app.applicant.information.column.collationid}")
	protected String columnNameApplicantInformationCollationId;
	/** カラム名: O_申請者情報 パスワード */
	@Value("${app.applicant.information.column.password}")
	protected String columnNameApplicantInformationPassword;

	/** カラム名: O_回答 回答ID */
	@Value("${app.answer.column.answerid}")
	protected String columnNameAnswerId;
	/** カラム名: O_回答 申請ID */
	@Value("${app.answer.column.applicationid}")
	protected String columnNameAnswerApplicationId;
	/** カラム名: O_回答 判定項目ID */
	@Value("${app.answer.column.judgementid}")
	protected String columnNameAnswerJudgementId;
	/** カラム名: O_回答 判定結果 */
	@Value("${app.answer.column.judgementresult}")
	protected String columnNameAnswerJudgementResult;
	/** カラム名: O_回答 回答内容 */
	@Value("${app.answer.column.answercontent}")
	protected String columnNameAnswerContent;
	/** カラム名: O_回答 回答内容 */
	@Value("${app.answer.column.notifiedtext}")
	protected String columnNameAnswerNotifiedText;
	/** カラム名: O_回答 登録日時 */
	@Value("${app.answer.column.registerdatetime}")
	protected String columnNameAnswerRegisterDatetime;
	/** カラム名: O_回答 更新日時 */
	@Value("${app.answer.column.updatedatetime}")
	protected String columnNameAnswerUpdateDatetime;
	/** カラム名: O_回答 完了フラグ */
	@Value("${app.answer.column.completeflag}")
	protected String columnNameAnswerCompleteFlag;
	/** カラム名: O_回答 通知フラグ */
	@Value("${app.answer.column.notifiedflag}")
	protected String columnNameAnswerNotifiedFlag;

	/** カラム名: M_区分判定 判定項目ID */
	@Value("${app.category.judgement.column.judgementitemid}")
	protected String columnNameCategoryJudgementId;
	/** カラム名: M_区分判定 担当部署ID */
	@Value("${app.category.judgement.column.departmentid}")
	protected String columnNameCategoryJudgementDepartmentId;
	/** カラム名: M_区分判定 区分1 */
	@Value("${app.category.judgement.column.category1}")
	protected String columnNameCategoryJudgementCategory1;
	/** カラム名: M_区分判定 区分2 */
	@Value("${app.category.judgement.column.category2}")
	protected String columnNameCategoryJudgementCategory2;
	/** カラム名: M_区分判定 区分3 */
	@Value("${app.category.judgement.column.category3}")
	protected String columnNameCategoryJudgementCategory3;
	/** カラム名: M_区分判定 区分4 */
	@Value("${app.category.judgement.column.category4}")
	protected String columnNameCategoryJudgementCategory4;
	/** カラム名: M_区分判定 区分5 */
	@Value("${app.category.judgement.column.category5}")
	protected String columnNameCategoryJudgementCategory5;
	/** カラム名: M_区分判定 区分6 */
	@Value("${app.category.judgement.column.category6}")
	protected String columnNameCategoryJudgementCategory6;
	/** カラム名: M_区分判定 区分7 */
	@Value("${app.category.judgement.column.category7}")
	protected String columnNameCategoryJudgementCategory7;
	/** カラム名: M_区分判定 区分8 */
	@Value("${app.category.judgement.column.category8}")
	protected String columnNameCategoryJudgementCategory8;
	/** カラム名: M_区分判定 区分9 */
	@Value("${app.category.judgement.column.category9}")
	protected String columnNameCategoryJudgementCategory9;
	/** カラム名: M_区分判定 区分10 */
	@Value("${app.category.judgement.column.category10}")
	protected String columnNameCategoryJudgementCategory10;
	/** カラム名: M_区分判定 GIS判定 */
	@Value("${app.category.judgement.column.gisjudgement}")
	protected String columnNameCategoryJudgementGisJudgement;
	/** カラム名: M_区分判定 バッファ */
	@Value("${app.category.judgement.column.buffer}")
	protected String columnNameCategoryJudgementBuffer;
	/** カラム名: M_区分判定 判定対象レイヤ */
	@Value("${app.category.judgement.column.judgementlayer}")
	protected String columnNameCategoryJudgementLayer;
	/** カラム名: M_区分判定 タイトル */
	@Value("${app.category.judgement.column.title}")
	protected String columnNameCategoryJudgementTitle;
	/** カラム名: M_区分判定 該当表示概要 */
	@Value("${app.category.judgement.column.applicablesummary}")
	protected String columnNameCategoryJudgementApplicableSummary;
	/** カラム名: M_区分判定 該当表示文言 */
	@Value("${app.category.judgement.column.applicabledescription}")
	protected String columnNameCategoryJudgementApplicableDescription;
	/** カラム名: M_区分判定 非該当表示有無 */
	@Value("${app.category.judgement.column.nonapplicabledisplayflag}")
	protected String columnNameCategoryJudgementNonApplicableDisplayFlag;
	/** カラム名: M_区分判定 非該当表示概要 */
	@Value("${app.category.judgement.column.nonapplicablesummary}")
	protected String columnNameCategoryJudgementNonApplicableSummary;
	/** カラム名: M_区分判定 非該当表示文言 */
	@Value("${app.category.judgement.column.nonapplicabledescription}")
	protected String columnNameCategoryJudgementNonApplicableDescription;
	/** カラム名: M_区分判定 テーブル名 */
	@Value("${app.category.judgement.column.tablename}")
	protected String columnNameCategoryJudgementTableName;
	/** カラム名: M_区分判定 フィールド名 */
	@Value("${app.category.judgement.column.fieldname}")
	protected String columnNameCategoryJudgementFieldName;
	/** カラム名: M_区分判定 判定レイヤ非該当時表示有無 */
	@Value("${app.category.judgement.column.nonapplicablelayerdisplayflag}")
	protected String columnNameCategoryJudgementNonApplicableLayerDisplayFlag;
	/** カラム名: M_区分判定 同時表示レイヤ */
	@Value("${app.category.judgement.column.simultaneousdisplaylayer}")
	protected String columnNameCategoryJudgementSimultaneousDisplayLayer;
	/** カラム名: M_区分判定 同時表示レイヤ表示有無 */
	@Value("${app.category.judgement.column.simultaneousdisplaylayerflag}")
	protected String columnNameCategoryJudgementSimultaneousDisplayLayerFlag;

	/** カラム名: M_部署 部署ID */
	@Value("${app.department.column.departmentid}")
	protected String columnNameDepartmentId;
	/** カラム名: M_部署 部署名 */
	@Value("${app.department.column.departmentname}")
	protected String columnNameDepartmentName;
	/** カラム名: M_部署 回答権限フラグ */
	@Value("${app.department.column.answerauthorityflag}")
	protected String columnNameDepartmentAnswerAuthorityFlag;
	/** カラム名: M_部署 メールアドレス */
	@Value("${app.department.column.mailaddress}")
	protected String columnNameDepartmentMailAddress;

	/** カラム名：O_申請 ステータス */
	@Value("${app.application.column.status}")
	protected String columnNameStatus;
	/** カラム名: O_申請 申請ID */
	@Value("${app.application.column.applicationid}")
	protected String columnNameOaApplicationId;
	/** 申請登録時の概況診断レポート接頭句 */
	@Value("${app.application.report.filename.header}")
	protected String applicationReportFileNameHeader;
	/** 申請登録時の概況診断レポート接尾句(日付フォーマット) */
	@Value("${app.application.report.filename.footer}")
	protected String applicationReportFileNameFooter;
	/** 申請登録時の概況診断レポートのファイルID */
	@Value("${app.application.report.fileid}")
	protected String applicationReportFileId;

	/** 申請登録時の回答予定のバッファ日数 */
	@Value("${app.application.answer.buffer.days}")
	protected int answerBufferDays;

	/** 事前協議の申請登録時の統括部署向けのバッファ日数 */
	@Value("${app.application.step2.control.department.buffer.days}")
	protected int controlDepartmentBufferDays;

	/** 事前協議の統括部署の受付確認日数 */
	@Value("${app.application.step2.control.department.accepting.confirm.days}")
	protected int acceptingConfirmDays;

	/** 申請版情報表示用文字列 */
	@Value("${app.application.versioninformation.text}")
	protected String versionInformationText;
	/** 申請版情報置換文字列 */
	@Value("${app.application.versioninformation.replacetext}")
	protected String versionInformationReplaceText;

	/** 申請登録時再申請不要で登録する区分判定概要文字列 */
	@Value("${app.application.default.reapplication.false}")
	protected List<String> reapplicationFalseTextList;

	/** 申請登録時再申請必要で登録する区分判定概要文字列（カンマ区切り） */
	@Value("${app.application.default.reapplication.true}")
	protected List<String> reapplicationTrueTextList;

	/** 申請登録時事前協議不要で登録する区分判定概要文字列 */
	@Value("${app.application.default.discussion.false}")
	protected List<String> discussionFalseTextList;

	/** 申請登録時事前協議必要で登録する区分判定概要文字列（カンマ区切り） */
	@Value("${app.application.default.discussion.true}")
	protected List<String> discussionTrueTextList;

	/** 申請回答情報の回答ファイル一覧 対象列出力内容（事前協議） */
	@Value("${app.application.answer.file.item.step2}")
	private String step2AnswerFileTitle;

	/** 申請回答情報の回答ファイル一覧 対象列出力内容（許可判定） */
	@Value("${app.application.answer.file.item.step3}")
	private String step3AnswerFileTitle;

	/** 申請回答情報の回答ファイル一覧 対象列出力内容（事前協議）置換文字列 */
	@Value("${app.application.answer.file.item.step2.replaceText}")
	private String step2AnswerFileTitleReplaceText;

	/** 回答レポートの表示名 */
	@Value("${app.answer.report.master.filename}")
	protected String answerReportMasterFilename;

	/** M_申請者情報項目Repositoryインスタンス */
	@Autowired
	private ApplicantInformationItemRepository applicantInformationItemRepository;
	/** M_申請ファイルRepositoryインスタンス */
	@Autowired
	private ApplicationFileMasterRepository applicationFileMasterRepository;
	/** O_申請ファイルRepositoryインスタンス */
	@Autowired
	private ApplicationFileRepository applicationFileRepository;
	/** M_申請情報検索結果Repositoryインスタンス */
	@Autowired
	private ApplicationSearchResultRepository applicationSearchResultRepository;
	/** O_申請Repositoryインスタンス */
	@Autowired
	private ApplicationRepository applicationRepository;
	/** O_回答ファイルRepositoryインスタンス */
	@Autowired
	private AnswerFileRepository answerFileRepository;
	/** M_地番検索結果定義Repositoryインスタンス */
	@Autowired
	private LotNumberSearchResultDefinitionRepository lotNumberSearchResultDefinitionRepository;
	/** F_地番Repositoryインスタンス */
	@Autowired
	private LotNumberRepository lotNumberRepository;
	/** M_申請区分Repositoryインスタンス */
	@Autowired
	private ApplicationCategoryMasterRepository applicationCategoryMasterRepository;
	/** O_申請者情報Repositoryインスタンス */
	@Autowired
	private ApplicantInformationRepository applicantInformationRepository;
	/** O_回答Repositoryインタフェース */
	@Autowired
	private AnswerRepository answerRepository;
	/** O_チャットRepositoryインスタンス */
	@Autowired
	private ChatRepository chatRepository;
	/** O_メッセージRepositoryインタフェース */
	@Autowired
	private MssageRepository mssageRepository;

	/** M_回答テンプレートRepositoryインスタンス */
	@Autowired
	private AnswerTemplateRepository answerTemplateRepository;

	/** O_問合せ宛先Repositoryインタフェース */
	@Autowired
	private InquiryAddressRepository inquiryAddressRepository;

	/** M_申請種類Repositoryインスタンス */
	@Autowired
	private ApplicationTypeRepository applicationTypeRepository;

	/** M_申請段階Repositoryインスタンス */
	@Autowired
	private ApplicationStepRepository applicationStepRepository;

	/** O_申請版情報Repositoryインスタンス */
	@Autowired
	private ApplicationVersionInformationRepository applicationVersionInformationRepository;

	/** M_申請情報項目選択肢Repositoryインスタンス */
	@Autowired
	private ApplicantInformationItemOptionRepository applicantInformationItemOptionRepository;

	/** O_申請追加報項目選択肢Repositoryインスタンス */
	@Autowired
	private ApplicantInformationAddRepository applicantInformationAddRepository;

	/** M_区分判定_権限Repositoryインスタンス */
	@Autowired
	private JudgementAuthorityRepository judgementAuthorityRepository;

	/** M_権限Repositoryインスタンス */
	@Autowired
	private AuthorityRepository authorityRepository;

	/** M_帳票Repositoryインスタンス */
	@Autowired
	private LedgerMasterRepository ledgerMasterRepository;

	/** O_帳票Repositoryインスタンス */
	@Autowired
	private LedgerRepository ledgerRepository;

	/** O_部署回答Repositoryインスタンス */
	@Autowired
	private DepartmentAnswerRepository departmentAnswerRepository;

	/** O_申請区分Repositoryインスタンス */
	@Autowired
	private ApplicationCategoryRepository applicationCategoryRepository;

	/** O_回答履歴Repositoryインスタンス */
	@Autowired
	private AnswerHistoryRepository answerHistoryRepository;

	/** M_判定結果Repositoryインスタンス */
	@Autowired
	private JudgementResultRepository judgementResultRepository;

	/** O_受付回答Repositoryインスタンス */
	@Autowired
	private AcceptingAnswerRepository acceptingAnswerRepository;

	/** O_申請JDBCインスタンス */
	@Autowired
	private ApplicationJdbc applicationJdbc;
	/** O_申請者情報JDBCインスタンス */
	@Autowired
	private ApplicantJdbc applicantJdbc;
	/** 申請区分JDBCインスタンス */
	@Autowired
	private ApplicationCategoryJdbc applicationCategoryJdbc;
	/** 申請地番JDBCインスタンス */
	@Autowired
	private ApplicationLotNumberJdbc applicationLotNumberJdbc;
	/** 回答情報JDBCインスタンス */
	@Autowired
	private AnswerJdbc answerJdbc;
	/** 申請ファイルJDBCインスタンス */
	@Autowired
	private ApplicationFileJdbc applicationFileJdbc;

	/** O_申請版情報JDBCインスタンス */
	@Autowired
	private ApplicationVersionInformationJdbc applicationVersionInformationJdbc;

	/** O_回答履歴JDBCインスタンス */
	@Autowired
	private AnswerHistoryJdbc answerHistoryJdbc;

	/** O_申請追加情報JDBCインスタンス */
	@Autowired
	private ApplicantAddJdbc applicantAddJdbc;

	/** O_部署回答JDBCインスタンス */
	@Autowired
	private DepartmentAnswerJdbc departmentAnswerJdbc;

	/** O_受付署回答JDBCインスタンス */
	@Autowired
	private AcceptingAnswerJdbc acceptingAnswerJdbc;

	/** 回答Serviceインスタンス */
	@Autowired
	private AnswerService answerService;

	/** O_帳票.通知フラグ 1:事業者に通知済 */
	public static final String LEDGER_NOTIFY_FLAG_1 = "1";

	/**
	 * 申請者情報入力項目一覧取得
	 * 
	 * @return 申請者情報入力項目一覧
	 */
	public List<ApplicantInformationItemForm> getApplicantItems() {
		LOGGER.debug("申請者情報入力項目一覧取得 開始");
		try {
			List<ApplicantInformationItem> itemList = applicantInformationItemRepository.getApplicantItems();

			List<ApplicantInformationItemForm> formList = new ArrayList<ApplicantInformationItemForm>();

			for (ApplicantInformationItem item : itemList) {
				formList.add(getApplicantInformationItemFormFromEntity(item, EMPTY, EMPTY));
			}
			return formList;
		} finally {
			LOGGER.debug("申請者情報入力項目一覧取得 終了");
		}
	}

	/**
	 * 申請ファイル一覧取得
	 * 
	 * @param generalConditionDiagnosisResultFormList 検索条件
	 * @return 申請ファイル一覧
	 */
	public List<ApplicationFileForm> getApplicationFiles(
			List<GeneralConditionDiagnosisResultForm> generalConditionDiagnosisResultFormList) {
		LOGGER.debug("申請ファイル一覧取得 開始");
		try {
			List<ApplicationFileForm> formList = new ArrayList<ApplicationFileForm>();

			List<String> judgementItemIdList = new ArrayList<String>();
			for (GeneralConditionDiagnosisResultForm generalConditionDiagnosisResultForm : generalConditionDiagnosisResultFormList) {
				String judgementId = generalConditionDiagnosisResultForm.getJudgementId();
				if (!judgementItemIdList.contains(judgementId) && generalConditionDiagnosisResultForm.getResult()) {
					LOGGER.trace("検索対象判定項目ID追加: " + judgementId);
					judgementItemIdList.add(judgementId);
				}
			}

			if (judgementItemIdList.size() > 0) {
				LOGGER.trace("申請ファイル一覧取得 開始");
				List<ApplicationFileMaster> applicationFileList = applicationFileMasterRepository
						.getApplicationFiles(judgementItemIdList);
				for (ApplicationFileMaster applicationFile : applicationFileList) {
					formList.add(getApplicationFileFormFromEntity(applicationFile));
				}
				LOGGER.trace("申請ファイル一覧取得 終了");
			}
			return formList;
		} finally {
			LOGGER.debug("申請ファイル一覧取得 終了");
		}
	}

	/**
	 * 申請情報検索結果表示項目一覧取得
	 * 
	 * @return 申請情報検索結果表示項目一覧
	 */
	public List<ApplicationInformationSearchResultHeaderForm> getApplicationInformationSearchResultHeader() {
		LOGGER.debug("申請情報検索結果表示項目一覧取得 開始");
		try {
			List<ApplicationInformationSearchResultHeaderForm> formList = new ArrayList<ApplicationInformationSearchResultHeaderForm>();

			List<ApplicationSearchResult> applicationSearchResultList = applicationSearchResultRepository
					.getApplicationSearchResultList();
			for (ApplicationSearchResult applicationSearchResult : applicationSearchResultList) {
				formList.add(getApplicationInformationSearchResultHeaderFormFromEntity(applicationSearchResult));
			}
			return formList;
		} finally {
			LOGGER.debug("申請情報検索結果表示項目一覧取得 終了");
		}
	}

	/**
	 * 申請情報検索
	 * 
	 * @param applicationSearchConditionForm 検索条件
	 * @return 申請情報検索結果
	 */
	public List<ApplicationSearchResultForm> searchApplicationInformation(
			ApplicationSearchConditionForm applicationSearchConditionForm) {
		LOGGER.debug("申請情報検索 開始");
		try {
			List<ApplicationSearchResultForm> formList = new ArrayList<ApplicationSearchResultForm>();
			ApplicationDao dao = new ApplicationDao(emf);

			// O_申請検索
			LOGGER.trace("O_申請検索 開始");
			List<Application> applicationList = dao.searchApplication(applicationSearchConditionForm);
			LOGGER.trace("O_申請検索 終了");

			// M_申請情報検索結果情報取得
			LOGGER.trace("M_申請情報検索結果情報取得 開始");
			List<ApplicationSearchResult> applicationSearchResultList = applicationSearchResultRepository
					.getApplicationSearchResultList();
			LOGGER.trace("M_申請情報検索結果情報取得 終了");

			// 申請情報ごとに各データを収集して格納
			for (Application application : applicationList) {
				formList.add(getApplicationSearchResultFormFromEntity(dao, application, applicationSearchResultList));
			}
			return formList;
		} finally {
			LOGGER.debug("申請情報検索 終了");
		}
	}

	/**
	 * 申請情報詳細取得【R6】
	 * 
	 * @param applicationId 申請ID
	 * @param userId        ユーザーID
	 * @param isGoverment   行政かどうか
	 * @return 申請情報詳細
	 */
	public ApplyAnswerForm getApplicationDetail(Integer applicationId, String userId, boolean isGoverment) {
		LOGGER.debug("申請情報詳細取得 開始");
		LOGGER.trace("申請ID: " + applicationId);
		LOGGER.trace("ユーザーID: " + userId);
		LOGGER.trace("行政かどうか: " + isGoverment);
		try {

			// 仮登録情報をリセットする
			resetApplicationInfo(applicationId);

			ApplyAnswerForm form = new ApplyAnswerForm();

			Application application = null;
			// O_申請検索
			LOGGER.trace("O_申請検索 開始");
			List<Application> applicationList = applicationRepository.getApplicationList(applicationId);
			if (applicationList.size() == 0) {
				LOGGER.warn("申請情報の件数が0");
				return null;
			} else {
				application = applicationList.get(0);
				form.setApplicationId(applicationId);
				form.setStatusCode(application.getStatus());

			}
			LOGGER.trace("O_申請検索 終了");

			ApplicationDao dao = new ApplicationDao(emf);

			// M_申請種類検索
			LOGGER.trace("M_申請種類検索 開始");
			List<ApplicationType> applicationTypeList = applicationTypeRepository
					.findByApplicationTypeId(application.getApplicationTypeId());
			if (applicationTypeList.size() == 0) {
				LOGGER.warn("申請情報の申請種類が存在しません。");
				return null;
			} else {
				ApplicationTypeForm applicationTypeForm = getApplicationTypeFormFromEntity(applicationTypeList.get(0));
				form.setApplicationType(applicationTypeForm);
			}
			LOGGER.trace("M_申請種類検索 終了");

			// O_申請版情報検索
			LOGGER.trace("O_申請版情報検索 開始");
			List<ApplicationVersionInformation> applicationVersionInformationList = dao
					.getApplicationVersionInformation(applicationId);
			if (applicationVersionInformationList.size() == 0) {
				LOGGER.warn("申請情報の版情報の件数が0");
				return null;
			} else {
				ApplicationVersionInformation activeInfo = applicationVersionInformationList.get(0);
				try {
					final String statusText = (activeInfo.getVersionInformation() != null)
							? versionInformationText.replace(versionInformationReplaceText,
									activeInfo.getVersionInformation().toString())
									+ getStatusMap().get(application.getStatus())
							: getStatusMap().get(application.getStatus());
					form.setStatus(statusText);
				} catch (Exception e) {
					form.setStatus("");
				}
			}
			LOGGER.trace("O_申請版情報検索 終了");

			// 申請地番一覧
			LOGGER.trace("申請地番一覧取得 開始");
			List<ApplyLotNumberForm> formList = new ArrayList<ApplyLotNumberForm>();
			List<ApplyLotNumber> lotNumbersList = dao.getApplyingLotNumberList(applicationId, lonlatEpsg);
			for (ApplyLotNumber lotNumbers : lotNumbersList) {
				formList.add(getApplyingLotNumberFormFromEntity(lotNumbers));
			}
			form.setLotNumbers(formList);
			LOGGER.trace("申請地番一覧取得 終了");

			// 申請者情報一覧
			LOGGER.trace("申請者情報一覧取得 開始");
			List<ApplicantInformationItemForm> applicantFormList = new ArrayList<ApplicantInformationItemForm>();
			List<ApplicantInformation> applicantList = dao.getApplicantInformationList(applicationId);
			if (applicantList.size() > 0) {
				// 申請者は申請者IDに対して1件
				ApplicantInformation applicant = applicantList.get(0);
				// 連絡先情報
				ApplicantInformation contactApplicant = null;
				if (applicantList.size() > 1) {
					contactApplicant = applicantList.get(1);
				}
				// 申請者情報項目一覧
				List<ApplicantInformationItem> applicantInformationItemList = applicantInformationItemRepository
						.getApplicantItems();
				for (ApplicantInformationItem applicantInformationItem : applicantInformationItemList) {
					String value = null;
					String contactValue = null;

					switch (applicantInformationItem.getApplicantInformationItemId()) {
					case ApplicationDao.ITEM_1_ID:
						value = applicant.getItem1();
						if (contactApplicant != null) {
							contactValue = contactApplicant.getItem1();
						}
						break;
					case ApplicationDao.ITEM_2_ID:
						value = applicant.getItem2();
						if (contactApplicant != null) {
							contactValue = contactApplicant.getItem2();
						}
						break;
					case ApplicationDao.ITEM_3_ID:
						value = applicant.getItem3();
						if (contactApplicant != null) {
							contactValue = contactApplicant.getItem3();
						}
						break;
					case ApplicationDao.ITEM_4_ID:
						value = applicant.getItem4();
						if (contactApplicant != null) {
							contactValue = contactApplicant.getItem4();
						}
						break;
					case ApplicationDao.ITEM_5_ID:
						value = applicant.getItem5();
						if (contactApplicant != null) {
							contactValue = contactApplicant.getItem5();
						}
						break;
					case ApplicationDao.ITEM_6_ID:
						value = applicant.getItem6();
						if (contactApplicant != null) {
							contactValue = contactApplicant.getItem6();
						}
						break;
					case ApplicationDao.ITEM_7_ID:
						value = applicant.getItem7();
						if (contactApplicant != null) {
							contactValue = contactApplicant.getItem7();
						}
						break;
					case ApplicationDao.ITEM_8_ID:
						value = applicant.getItem8();
						if (contactApplicant != null) {
							contactValue = contactApplicant.getItem8();
						}
						break;
					case ApplicationDao.ITEM_9_ID:
						value = applicant.getItem9();
						if (contactApplicant != null) {
							contactValue = contactApplicant.getItem9();
						}
						break;
					case ApplicationDao.ITEM_10_ID:
						value = applicant.getItem10();
						if (contactApplicant != null) {
							contactValue = contactApplicant.getItem10();
						}
						break;
					default:
						LOGGER.warn("未知の項目ID: " + applicantInformationItem.getApplicantInformationItemId());
						break;
					}
					applicantFormList.add(
							getApplicantInformationItemFormFromEntity(applicantInformationItem, value, contactValue));
				}
			}
			form.setApplicantInformations(applicantFormList);
			LOGGER.trace("申請者情報一覧取得 終了");

			LOGGER.trace("申請段階ごとの申請詳細情報取得 開始");
			List<ApplyAnswerDetailForm> applyAnswerDetailFormList = new ArrayList<ApplyAnswerDetailForm>();

			// 申請種類ID
			Integer applicationTypeId = form.getApplicationType().getApplicationTypeId();

			// 統括部署管理者
			boolean administers = false;
			List<GovernmentUserAndAuthority> governmentUserAndAuthorityList = new ArrayList<GovernmentUserAndAuthority>();
			if (isGoverment) {
				GovernmentUserDao governmentUserDao = new GovernmentUserDao(emf);
				governmentUserAndAuthorityList = governmentUserDao.getGovernmentUserInfo(userId, null);
				if (governmentUserAndAuthorityList.size() < 0) {
					LOGGER.warn("ユーザー情報がない");
					return null;
				}
				if (governmentUserAndAuthorityList.get(0).getAdminFlag()
						&& governmentUserAndAuthorityList.get(0).getManagementDepartmentFlag()) {
					administers = true;
				}
			}

			// 申請情報が初回受付確認中かどうか（事前協議のみ）
			boolean firstAccepting = false;
			// 申請段階ごとの申請詳細情報を取得
			for (ApplicationVersionInformation applicationVersionInformation : applicationVersionInformationList) {

				// 申請段階ID
				Integer applicationStepId = applicationVersionInformation.getApplicationStepId();

				// 事前協議の場合、
				if (APPLICATION_STEP_ID_2.equals(applicationStepId)) {
					// 受け付けた申請がないの場合（受付版情報＝0）、統括部署管理者以外の場合、事前協議情報の取得をスキップ
					if (!administers && applicationVersionInformation.getAcceptVersionInformation().compareTo(0) == 0) {
						// 事業者の場合、再申請ボタンを制御するため、初回受付確認中フラグを設定
						if (!isGoverment && ACCEPTING_FLAG_0_UNCONFIRMED
								.equals(applicationVersionInformation.getAcceptingFlag())) {
							firstAccepting = true;
						}
						continue;
					}
				}

				if (isGoverment) {

					for (GovernmentUserAndAuthority governmentUserAndAuthority : governmentUserAndAuthorityList) {
						if (governmentUserAndAuthority.getApplicationStepId().equals(applicationStepId)) {
							// 回答詳細情報
							applyAnswerDetailFormList.add(editapplyAnswerDetailForm(applicationVersionInformationList,
									applicationVersionInformation, false, isGoverment, applicationTypeId,
									governmentUserAndAuthority));
							// 受付フラグ
							String acceptingFlag = applicationVersionInformation.getAcceptingFlag();

							// 事前協議、申請版情報の受付フラグが0：未確認、ログインユーザーが、統括部署管理者の場合、受付確認情報をセット
							if (APPLICATION_STEP_ID_2.equals(applicationStepId) && administers
									&& ACCEPTING_FLAG_0_UNCONFIRMED.equals(acceptingFlag)) {
								applyAnswerDetailFormList.add(editapplyAnswerDetailForm(
										applicationVersionInformationList, applicationVersionInformation, true,
										isGoverment, applicationTypeId, governmentUserAndAuthority));
							}
						}
					}
				} else {
					// 回答詳細情報
					applyAnswerDetailFormList.add(editapplyAnswerDetailForm(applicationVersionInformationList,
							applicationVersionInformation, false, isGoverment, applicationTypeId, null));
				}

			}
			form.setFirstAccepting(firstAccepting);
			form.setApplyAnswerDetails(applyAnswerDetailFormList);
			LOGGER.trace("申請段階ごとの申請詳細情報取得 終了");

			// 回答通知可能(画面に「回答通知ボタン押下」の制御用：通知不可の場合、メッセージ表示)
			boolean notificable = false;

			if (isGoverment) {
				// 処理中の申請段階
				Integer activeApplicationStepId = applicationVersionInformationList.get(0).getApplicationStepId();

				for (GovernmentUserAndAuthority user : governmentUserAndAuthorityList) {
					if (user.getApplicationStepId().equals(activeApplicationStepId)) {

						// 事前相談 ⇒ 通知権限持っている場合、通知可能とする
						if (APPLICATION_STEP_ID_1.equals(activeApplicationStepId)) {
							if (!AUTH_TYPE_NONE.equals(user.getNotificationAuthorityFlag())) {
								notificable = true;
							}
						}
						// 事前協議 ⇒ （通知権限持っている、かつ、管理者）または 統括部署管理者
						if (APPLICATION_STEP_ID_2.equals(activeApplicationStepId)) {
							if (!AUTH_TYPE_NONE.equals(user.getNotificationAuthorityFlag()) && user.getAdminFlag()) {
								notificable = true;
							}

							if (administers) {
								notificable = true;
							}
						}
						// 許可判定 ⇒ 回答権限持っている、かつ、管理者の場合、通知可能とする
						if (APPLICATION_STEP_ID_3.equals(activeApplicationStepId)) {
							if (!AUTH_TYPE_NONE.equals(user.getAnswerAuthorityFlag()) && user.getAdminFlag()) {
								notificable = true;
							}
						}
					}
				}
			}
			form.setNotificable(notificable);
			form.setControlDepartmentAdmin(administers);

			return form;
		} finally {
			LOGGER.debug("申請情報詳細取得 終了");
		}
	}

	/**
	 * 申請段階ごとの申請情報詳細フォームの編集
	 * 
	 * @param applicationVersionInformationList 申請版情報リスト
	 * @param applicationVersionInformation     編集中申請段階の申請版情報
	 * @param isAcceptInfo                      申請受付情報の編集かどうか（事前協議以外の場合、常にfalse）
	 * @param isGoverment                       行政かどうか
	 * @param applicationTypeId                 申請種類
	 * @param governmentUserAndAuthority        ログインユーザーの部署・権限情報（行政の場合のみ、事業者の場合、null）
	 * @return
	 */
	private ApplyAnswerDetailForm editapplyAnswerDetailForm(
			List<ApplicationVersionInformation> applicationVersionInformationList,
			ApplicationVersionInformation applicationVersionInformation, boolean isAcceptInfo, boolean isGoverment,
			Integer applicationTypeId, GovernmentUserAndAuthority governmentUserAndAuthority) {
		ApplyAnswerDetailForm applyAnswerDetailForm = new ApplyAnswerDetailForm();

		ApplicationDao applicationDao = new ApplicationDao(emf);
		AnswerDao answerDao = new AnswerDao(emf);

		// 申請ID
		Integer applicationId = applicationVersionInformation.getApplicationId();
		// 申請段階ID
		Integer applicationStepId = applicationVersionInformation.getApplicationStepId();
		// 該当申請段階の最新な版情報
		Integer versionInformation = applicationVersionInformation.getVersionInformation();
		// 該当申請段階の受付版情報（事前協議以外の場合、版情報と受付版情報が同じ）
		Integer acceptVersionInformation = applicationVersionInformation.getAcceptVersionInformation();

		// 事前協議、回答詳細用情報の場合、
		if (APPLICATION_STEP_ID_2.equals(applicationStepId) && !isAcceptInfo) {
			// 受付フラグが受付以外の場合、受付版情報に対する申請を取得する
			if (!ACCEPTING_FLAG_1_ACCEPTED.equals(applicationVersionInformation.getAcceptingFlag())) {
				versionInformation = acceptVersionInformation;
			}
		}

		// 申請ID
		applyAnswerDetailForm.setApplicationId(applicationId);
		// 申請段階
		LOGGER.trace("O_申請段階取得 開始");
		applyAnswerDetailForm.setApplicationStepId(applicationStepId);
		List<ApplicationStep> aplicationStepList = applicationStepRepository.findByApplicationStepId(applicationStepId);
		if (aplicationStepList.size() == 0) {
			LOGGER.warn("申請情報の申請段階が存在しません。");
			return null;
		}
		applyAnswerDetailForm.setApplicationStepName(aplicationStepList.get(0).getApplicationStepName());
		LOGGER.trace("O_申請段階取得 終了");

		// 版情報
		applyAnswerDetailForm.setVersionInformation(applicationVersionInformation.getVersionInformation());
		// 登録日時
		applyAnswerDetailForm.setRegisterDatetime(applicationVersionInformation.getRegisterDatetime());
		// 更新日時
		applyAnswerDetailForm.setUpdateDatetime(applicationVersionInformation.getUpdateDatetime());

		// 受付フラグ
		applyAnswerDetailForm.setAcceptingFlag(applicationVersionInformation.getAcceptingFlag());
		// 受付版情報
		applyAnswerDetailForm.setAcceptVersionInformation(acceptVersionInformation);
		// 受付確認情報かどうか
		applyAnswerDetailForm.setIsAcceptInfo(isAcceptInfo);

		// 申請区分選択一覧
		LOGGER.trace("申請区分選択一覧取得 開始");
		Integer paramApplicationStepId = applicationStepId;
		Integer paramVersionInformation = versionInformation;
		// 許可判定の場合、事前協議の申請区分を表示するため、事前協議の最終版の申請区分を検索する
		if (APPLICATION_STEP_ID_3.equals(applicationStepId)) {
			paramApplicationStepId = APPLICATION_STEP_ID_2;
			for (ApplicationVersionInformation applyVer : applicationVersionInformationList) {
				if (APPLICATION_STEP_ID_2.equals(applyVer.getApplicationStepId())) {
					paramVersionInformation = applyVer.getVersionInformation();
				}
			}

		}
		List<ApplicationCategorySelectionViewForm> viewFormList = new ArrayList<ApplicationCategorySelectionViewForm>();
		List<ApplicationCategorySelectionView> applicationCategorySelectionViewList = applicationDao
				.getApplicationCategorySelectionViewList(applicationId, paramApplicationStepId,
						paramVersionInformation);
		for (ApplicationCategorySelectionView applicationCategorySelectionView : applicationCategorySelectionViewList) {
			ApplicationCategorySelectionViewForm viewForm = getSelectionViewFormFromEntity(
					applicationCategorySelectionView, applicationId, paramApplicationStepId, paramVersionInformation);
			viewFormList.add(viewForm);
		}
		applyAnswerDetailForm.setApplicationCategories(viewFormList);
		LOGGER.trace("申請区分選択一覧取得 終了");

		// 申請追加情報一覧
		LOGGER.trace("O_申請追加情報一覧取得 開始");
		List<ApplicantInformationItemForm> applicantAddFormList = new ArrayList<ApplicantInformationItemForm>();
		List<ApplicantInformationItem> applicantAddInformationItemList = applicantInformationItemRepository
				.getApplicantAddItems(String.valueOf(applicationStepId));
		for (ApplicantInformationItem applicantInformationItem : applicantAddInformationItemList) {
			List<ApplicantInformationAdd> applicantInformationAddList = applicantInformationAddRepository
					.getApplicantInformationAdd(applicationId, applicationStepId,
							applicantInformationItem.getApplicantInformationItemId(), versionInformation);
			String value = EMPTY;
			if (applicantInformationAddList.size() > 0) {
				value = applicantInformationAddList.get(0).getItemValue();
			}
			applicantAddFormList.add(getApplicantInformationItemFormFromEntity(applicantInformationItem, value, EMPTY));
		}

		applyAnswerDetailForm.setApplicantAddInformations(applicantAddFormList);
		LOGGER.trace("O_申請追加情報一覧取得 終了");

		// 申請ファイル一覧
		LOGGER.trace("申請ファイル一覧取得 開始");
		List<ApplicationFileForm> applicationFileMasterFormList = new ArrayList<ApplicationFileForm>();
		List<ApplicationFileMaster> applicationFileMasterList = new ArrayList<ApplicationFileMaster>();
		// 受付確認情報の場合
		if (isAcceptInfo) {
			// 受付確認の版情報に対する申請ファイル一覧
			applicationFileMasterList = applicationDao.getApplicationFileMasterListWithVersionInformation(applicationId,
					applicationStepId, applicationVersionInformation.getVersionInformation());
		} else {
			applicationFileMasterList = applicationDao.getApplicationFileMasterList(applicationId, applicationStepId);
		}
		for (ApplicationFileMaster applicationFileMaster : applicationFileMasterList) {
			ApplicationFileForm fileForm = getApplicationFileFormFromEntity(applicationFileMaster);

			// アップロードファイル一式
			List<UploadApplicationFileForm> applicationFileFormList = new ArrayList<UploadApplicationFileForm>();
			// 版情報に対する申請ファイルを取得する

			List<ApplicationFile> applicationFileList = applicationFileRepository
					.getApplicationFilesByVersionInformation(applicationFileMaster.getApplicationFileId(),
							applicationId, applicationStepId, versionInformation);

			for (ApplicationFile applicationFile : applicationFileList) {
				UploadApplicationFileForm uploadApplicationFileForm = getUploadApplicationFileFormFromEntity(
						applicationFile);
				applicationFileFormList.add(uploadApplicationFileForm);
			}

			List<UploadApplicationFileForm> applicationFileWithVerList = new ArrayList<UploadApplicationFileForm>();
			List<UploadApplicationFileForm> allHistoryApplicationFiles = new ArrayList<UploadApplicationFileForm>();
			if (isGoverment) {
				// 行政の場合、全ての版の申請ファイルを取得する
				// 全て版の申請ファイル一覧
				LOGGER.trace("申請ファイル一覧取得 開始");

				List<ApplicationFile> applicationFileHistorys = applicationFileRepository
						.getHistoryApplicationFilesFromVersionInformation(applicationFileMaster.getApplicationFileId(),
								applicationId, applicationStepId, versionInformation);
				for (ApplicationFile file : applicationFileHistorys) {
					applicationFileWithVerList.add(getUploadApplicationFileFormFromEntity(file));
				}
				LOGGER.trace("申請ファイル一覧取得 終了");

				// 回答登録画面に、全ての申請段階の申請ファイルが引用できるため、申請段階を問わず、全ての版の申請ファイルを取得する
				LOGGER.trace("申請ファイル一覧取得 開始");
				List<ApplicationFile> applicationFileAllHistorys = applicationFileRepository
						.getAllHistoryApplicationFilesFromVersionInformation(
								applicationFileMaster.getApplicationFileId(), applicationId, versionInformation);
				for (ApplicationFile file : applicationFileAllHistorys) {
					allHistoryApplicationFiles.add(getUploadApplicationFileFormFromEntity(file));
				}
				LOGGER.trace("申請ファイル一覧取得 終了");

			}

			fileForm.setUploadFileFormList(applicationFileFormList);
			fileForm.setApplicationFileHistorys(applicationFileWithVerList);
			fileForm.setApplicationFileAllHistorys(allHistoryApplicationFiles);

			applicationFileMasterFormList.add(fileForm);
		}

		// 行政の場合、回答通知時点で作成した回答レポート一覧取得
		if (isGoverment) {

			// O_申請ファイル中に、回答レポートの一覧を取得
			List<ApplicationFile> applicationFileList = applicationFileRepository
					.getAnswerRepotFileList(answerReportFileId, applicationId, applicationStepId);

			// 回答レポートが存在する場合、申請ファイル一覧に追加
			if (applicationFileList.size() > 0) {
				// 画面に表示するため、回答レポートの表示名と拡張子を設定する
				ApplicationFileForm applicationFileMasterform = new ApplicationFileForm();
				applicationFileMasterform.setApplicationFileName(answerReportMasterFilename);
				applicationFileMasterform.setExtension(applicationFileList.get(0).getExtension());
				// アップロードファイル一式(最終版)
				List<UploadApplicationFileForm> applicationFileFormList = new ArrayList<UploadApplicationFileForm>();
				UploadApplicationFileForm uploadApplicationFileForm = getUploadApplicationFileFormFromEntity(
						applicationFileList.get(0));
				applicationFileFormList.add(uploadApplicationFileForm);

				// 全て版の申請ファイル一覧
				List<UploadApplicationFileForm> applicationFileWithVerList = new ArrayList<UploadApplicationFileForm>();
				for (ApplicationFile file : applicationFileList) {
					applicationFileWithVerList.add(getUploadApplicationFileFormFromEntity(file));
				}

				applicationFileMasterform.setUploadFileFormList(applicationFileFormList);
				applicationFileMasterform.setApplicationFileHistorys(applicationFileWithVerList);

				applicationFileMasterFormList.add(applicationFileMasterform);
			}
		}

		applyAnswerDetailForm.setApplicationFiles(applicationFileMasterFormList);
		LOGGER.trace("申請ファイル一覧取得 終了");

		// 受付確認情報・回答詳細の一覧設定
		// 回答一覧（事前相談、許可判定のみ）
		List<AnswerForm> answerFormList = new ArrayList<AnswerForm>();
		// 部署回答一覧（事前協議のみ）
		List<DepartmentAnswerForm> departmentAnswerFormList = new ArrayList<DepartmentAnswerForm>();
		// 部署受付回答一覧（事前協議のみ）
		List<DepartmentAnswerForm> departmentAcceptingAnswerList = new ArrayList<DepartmentAnswerForm>();
		// 既存回答ありの受付回答の部署IDリスト
		List<String> departmentIdList = new ArrayList<String>();
		// 既存回答ありの受付回答の回答IDリスト
		List<Integer> answerIdList = new ArrayList<Integer>();
		if (isAcceptInfo) {

			List<Department> departmentList = applicationDao.getAcceptingAnswerDepartmentList(applicationId,
					applicationStepId, versionInformation);

			// 申請段階
			ApplicationStepForm applicationStepForm = new ApplicationStepForm();
			List<ApplicationStep> applicationStepList = applicationStepRepository
					.findByApplicationStepId(applicationStepId);
			if (applicationStepList.size() > 0) {
				applicationStepForm = geApplicationStepFormFromEntity(applicationStepList.get(0));
			}

			for (Department entity : departmentList) {

				DepartmentAnswerForm form = new DepartmentAnswerForm();

				// 部署回答ID(受付回答なので、部署回答がない。画面に、区別できるため、インデックスで設定)
				form.setDepartmentAnswerId(departmentList.indexOf(entity) + 1);
				// 申請ID
				form.setApplicationId(applicationId);
				// 申請段階ID
				form.setApplicationStepId(applicationStepId);
				// 部署
				DepartmentForm departmentForm = getDepartmentFormFromEntity(entity);
				form.setDepartment(departmentForm);
				// 回答通知選択可否(回答通知画面に回答一覧が非表示)
				form.setNotificable(false);
				// 編集可否
				form.setEditable(false);
				// チェック有無
				form.setChecked(false);

				// 受付回答一覧
				List<Integer> answerIdDepartmentList = new ArrayList<Integer>();
				List<AcceptingAnswer> acceptingAnswerList = acceptingAnswerRepository.findByDepartmentId(applicationId,
						applicationStepId, versionInformation, entity.getDepartmentId());
				List<AcceptingAnswerForm> acceptingAnswerFormList = new ArrayList<AcceptingAnswerForm>();
				for (AcceptingAnswer acceptingAnswer : acceptingAnswerList) {
					AcceptingAnswerForm acceptingAnswerForm = getAcceptingAnswerFormFromEntity(acceptingAnswer,
							departmentForm, applicationTypeId);
					acceptingAnswerForm.setApplicationStep(applicationStepForm);

					acceptingAnswerFormList.add(acceptingAnswerForm);

					if (acceptingAnswer.getAnswerId() != null && acceptingAnswer.getAnswerId() > 0) {
						answerIdList.add(acceptingAnswer.getAnswerId());
						answerIdDepartmentList.add(acceptingAnswer.getAnswerId());
					}
				}
				form.setAcceptingAnswers(acceptingAnswerFormList);
				// 回答一覧
				form.setAnswers(new ArrayList<AnswerForm>());

				// 回答ファイル一覧
				LOGGER.trace("回答ファイル一覧取得 開始");
				List<AnswerFileForm> answerFileFormList = new ArrayList<AnswerFileForm>();
				form.setAnswerFiles(answerFileFormList);
				LOGGER.trace("回答ファイル一覧取得 終了");

				// チャット情報
				form.setChat(new ChatForm());

				// 回答履歴一覧
				form.setAnswerHistorys(new ArrayList<AnswerHistoryForm>());

				// 行政確定通知許可フラグ
				form.setGovernmentConfirmPermissionFlag(false);

				departmentAcceptingAnswerList.add(form);
			}

		} else {

			LOGGER.trace("回答一覧取得 開始");
			LOGGER.trace("申請段階：" + aplicationStepList.get(0).getApplicationStepName());
			// 申請種類
			if (APPLICATION_STEP_ID_1.equals(applicationStepId) || APPLICATION_STEP_ID_3.equals(applicationStepId)) {

				List<Answer> answerList = new ArrayList<>();
				if (isGoverment) {
					answerList = applicationDao.getAnswerList(applicationId, isGoverment, applicationStepId, 0);
				} else {
					answerList = applicationDao.getAllAnswerListForBusiness(applicationId, applicationStepId, 0);
				}
				for (Answer answer : answerList) {
					answerFormList.add(getAnswerFormFromEntity(answer, governmentUserAndAuthority, isGoverment,
							applicationTypeId, false));
				}
			}
			LOGGER.trace("回答一覧取得 終了");

			if (APPLICATION_STEP_ID_2.equals(applicationStepId)) {
				List<DepartmentAnswer> departmentAnswerList = applicationDao.getDepartmentAnswerList(applicationId,
						applicationStepId);
				for (DepartmentAnswer departmentAnswer : departmentAnswerList) {
					List<Answer> answerList = new ArrayList<>();
					if (isGoverment) {
						answerList = applicationDao.getAnswerList(applicationId, isGoverment, applicationStepId,
								departmentAnswer.getDepartmentAnswerId());
					} else {
						answerList = applicationDao.getAllAnswerListForBusiness(applicationId, applicationStepId,
								departmentAnswer.getDepartmentAnswerId());
					}

					if (isGoverment) {
						// 行政の場合、回答がなくても、部署回答が表示できる
						departmentAnswerFormList.add(getDepartmentAnswerFormFromEntity(departmentAnswer,
								governmentUserAndAuthority, isGoverment, applicationTypeId, answerList));
					} else {
						// 未通知の場合も表示するため以下条件なしでの追加
						departmentAnswerFormList.add(getDepartmentAnswerFormFromEntity(departmentAnswer,
								governmentUserAndAuthority, isGoverment, applicationTypeId, answerList));
					}

				}
			}
		}
		applyAnswerDetailForm.setAnswers(answerFormList);
		applyAnswerDetailForm.setDepartmentAnswers(departmentAnswerFormList);
		applyAnswerDetailForm.setDepartmentAcceptingAnswers(departmentAcceptingAnswerList);

		// 協議対象一覧
		List<LedgerMasterForm> ledgerMasters = new ArrayList<LedgerMasterForm>();
		if (APPLICATION_STEP_ID_2.equals(applicationStepId)) {
			ledgerMasters = getLedgerList(applicationStepId);
		}
		applyAnswerDetailForm.setLedgerMasters(ledgerMasters);

		// 行政
		if (isGoverment) {

			// 回答履歴一覧(申請段階ごと)
			if (isAcceptInfo) {
				// 回答履歴一覧
				applyAnswerDetailForm.setAnswerHistorys(new ArrayList<AnswerHistoryForm>());
			} else {
				final List<AnswerHistoryForm> answerHistoryForm = answerService.getAnswerHistory(applicationId,
						applicationTypeId, applicationStepId, isGoverment, 0, 0);
				applyAnswerDetailForm.setAnswerHistorys(answerHistoryForm);
			}
		}

		// 通知ファイル一覧
		// 事業者向けの場合、通知フラグ=trueのもののみ取得
		List<Ledger> ledgerList;
		if (isGoverment) {
			// 行政向けの場合
			ledgerList = ledgerRepository.getLedgerList(applicationId, applicationStepId);
		} else {
			// 事業者向けの場合（通知フラグ=trueのもののみ取得）
			ledgerList = ledgerRepository.getLedgerList(applicationId, applicationStepId, LEDGER_NOTIFY_FLAG_1);
		}
		List<LedgerForm> LedgerFormList = new ArrayList<LedgerForm>();
		for (Ledger ledger : ledgerList) {
			// Formセット
			LedgerFormList.add(getLedgerFormFromEntity(isGoverment, ledger));
		}
		applyAnswerDetailForm.setLedgerFiles(LedgerFormList);

		// 回答ファイル一覧(申請段階ごと)
		LOGGER.trace("回答ファイル一覧取得 開始");

		List<AnswerFile> answerFileList = answerDao.getAnswerFileList(applicationId, applicationStepId, null,
				isGoverment);

		List<AnswerFileForm> answerFileFormList = new ArrayList<AnswerFileForm>();
		for (AnswerFile answerFile : answerFileList) {
			// 受付確認情報の場合、受付内容一覧に、既存の回答データ紐づけるレコードがない場合、回答ファイルが非表示
			if (isAcceptInfo) {
				if (!departmentIdList.contains(answerFile.getDepartmentId())) {
					continue;
				}
			}
			AnswerFileForm answerFileForm = getAnswerFileFormFromEntity(answerFile);

			// 回答ファイル一覧の対象列に表示する文言取得
			String title = EMPTY;
			String judgementId = EMPTY;

			// 事前相談の場合
			if (APPLICATION_STEP_ID_1.equals(applicationStepId)) {
				List<Answer> answerList = answerRepository.findByAnswerId(answerFile.getAnswerId());
				if (answerList.size() > 0) {
					Answer answer = answerList.get(0);
					if (answer.getJudgementId() != null && !"".equals(answer.getJudgementId())) {
						List<CategoryJudgementResult> categoryJudgementResult = judgementResultRepository
								.getJudgementResult(answer.getJudgementId(), applicationTypeId, applicationStepId,
										answer.getDepartmentId());
						if (categoryJudgementResult.size() > 0) {
							judgementId = answer.getJudgementId();
							title = categoryJudgementResult.get(0).getTitle();
						}
					}
				}
			}

			// 事前協議の場合、「事前協議回答ファイル（xxx課）」で固定表示
			if (APPLICATION_STEP_ID_2.equals(applicationStepId)) {
				if (!EMPTY.equals(answerFile.getDepartmentId()) && answerFile.getDepartmentId() != null) {

					List<Department> departmentList = departmentRepository
							.getDepartmentListById(answerFile.getDepartmentId());

					if (departmentList.size() > 0) {
						String deparmentName = departmentList.get(0).getDepartmentName();
						title = step2AnswerFileTitle.replace(step2AnswerFileTitleReplaceText, deparmentName);
					}
				}
			}

			// 許可判定の場合、「許可判定回答ファイル」で固定表示
			if (APPLICATION_STEP_ID_3.equals(applicationStepId)) {
				title = step3AnswerFileTitle;
			}

			AnswerJudgementForm answerJudgementForm = new AnswerJudgementForm();
			answerJudgementForm.setJudgementId(judgementId);
			answerJudgementForm.setTitle(title);
			answerFileForm.setJudgementInformation(answerJudgementForm);
			answerFileFormList.add(answerFileForm);
		}
		applyAnswerDetailForm.setAnswerFiles(answerFileFormList);
		LOGGER.trace("回答ファイル一覧取得 終了");

		// チャット情報(許可判定のみ)
		if (APPLICATION_STEP_ID_3.equals(applicationStepId)) {
			// O_チャットを取得する
			List<Chat> chatList = chatRepository.findByApplicationStepId(applicationId, applicationStepId);
			ChatForm chatForm = new ChatForm();
			if (chatList.size() > 0) {
				Chat chat = chatList.get(0);
				chatForm.setAnswerId(chat.getAnswerId());
				chatForm.setApplicationId(chat.getApplicationId());
				ApplicationStepForm applicationStep = new ApplicationStepForm();
				applicationStep.setApplicationStepId(chat.getApplicationStepId());
				chatForm.setApplicationStep(applicationStep);
				// chatForm.setApplicationStepId(chat.getApplicationStepId());
				chatForm.setTitle("");
				chatForm.setChatId(chat.getChatId());
				// チャットIDに紐づくメッセージを取得する
				List<Message> messageList = new ArrayList<Message>();
				if (isGoverment) {
					// 行政
					messageList = mssageRepository.findByChatId(chat.getChatId());
				} else {
					// 事業者
					messageList = mssageRepository.findByChatIdForBusiness(chat.getChatId());
				}
				List<MessageForm> messageFormList = new ArrayList<MessageForm>();
				for (Message message : messageList) {
					MessageForm messageForm = getMessageFormFromEntity(message);
					messageFormList.add(messageForm);
				}
				chatForm.setMessages(messageFormList);
			}

			applyAnswerDetailForm.setChat(chatForm);
			LOGGER.trace("チャット情報取得 終了");
		}

		// 事業者が全ての回答が回答完了しているかフラグ設定
		if (APPLICATION_STEP_ID_2.equals(applicationStepId)) {

			applyAnswerDetailForm.setBusinessAnswerCompleted(true);
			List<Answer> answerEntityList = answerRepository.findByApplicationStepId(applicationId, applicationStepId);
			for (Answer entity : answerEntityList) {

				String businessPassStatus = entity.getBusinessPassStatus();
				if (businessPassStatus == null || EMPTY.equals(businessPassStatus.trim())) {
					applyAnswerDetailForm.setBusinessAnswerCompleted(false);
					break;
				}
			}

		} else {
			applyAnswerDetailForm.setBusinessAnswerCompleted(null);
		}

		return applyAnswerDetailForm;
	}

	/**
	 * 申請登録パラメータ検査
	 * 
	 * @param applicationRegisterForm パラメータ
	 * @return 判定結果
	 */
	public boolean validateRegisterApplicationParam(ApplicationRegisterForm applicationRegisterForm) {
		LOGGER.debug("申請登録パラメータ検査 開始");
		try {

			if (applicationRegisterForm.getApplicationTypeId() == null) {
				LOGGER.warn("申請種類が設定されていない: " + applicationRegisterForm.getApplicationTypeId());
				return false;
			}

			if (applicationRegisterForm.getApplicationStepId() == null) {
				LOGGER.warn("申請段階が設定されていない: " + applicationRegisterForm.getApplicationStepId());
				return false;
			}

			// 申請者情報
			List<ApplicantInformationItemForm> applicantInfomationList = applicationRegisterForm
					.getApplicantInformationItemForm();
			int mailAddressCount = 0;
			for (ApplicantInformationItemForm applicantInfomation : applicantInfomationList) {
				if (applicantInfomation.getMailAddress()) {
					mailAddressCount++;
				}
			}
			if (mailAddressCount != 1) {
				// メールアドレスは必ず1件のみ
				LOGGER.warn("メールアドレスの件数が不正");
				return false;
			}

			// O_申請区分
			LOGGER.trace("O_申請区分検査 開始");
			List<ApplicationCategorySelectionViewForm> categoryList = applicationRegisterForm
					.getApplicationCategories();
			for (ApplicationCategorySelectionViewForm category : categoryList) {
				List<ApplicationCategoryForm> applicationCategoryList = category.getApplicationCategory();
				for (ApplicationCategoryForm applicationCategory : applicationCategoryList) {
					List<ApplicationCategoryMaster> applicationCategoryMasterList = applicationCategoryMasterRepository
							.findByCategoryId(applicationCategory.getId(), applicationCategory.getScreenId());
					if (applicationCategoryMasterList.size() == 0) {
						// 申請区分ID、画面IDに適応した情報がない
						LOGGER.warn("申請区分ID、画面IDに適応した情報がない");
						return false;
					}
				}
			}
			LOGGER.trace("O_申請区分検査 終了");

			// O_申請地番
			LOGGER.trace("O_申請地番検査 開始");
			List<LotNumberForm> lotNumbers = applicationRegisterForm.getLotNumbers();
			for (LotNumberForm lotNumber : lotNumbers) {
				List<LotNumber> lotNumberList = lotNumberRepository.findByChibanId(lotNumber.getChibanId());
				if (lotNumberList.size() == 0) {
					// 地番IDに適応した地番情報が無い
					LOGGER.warn("地番IDに適応した地番情報が無い");
					return false;
				}
			}
			LOGGER.trace("O_申請地番検査 終了");

			// 区分判定ID
			LOGGER.trace("区分判定検査 開始");
			List<GeneralConditionDiagnosisResultForm> conditionList = applicationRegisterForm
					.getGeneralConditionDiagnosisResultForm();
			for (GeneralConditionDiagnosisResultForm condition : conditionList) {
				List<CategoryJudgement> categoryJudgementList = categoryJudgementRepository
						.getCategoryJudgementListById(condition.getJudgementId());
				if (categoryJudgementList.size() == 0) {
					// 区分判定IDに適応した区分判定情報が無い
					LOGGER.warn("区分判定IDに適応した区分判定情報が無い");
					return false;
				}
			}
			LOGGER.trace("区分判定検査 終了");

			return true;
		} finally {
			LOGGER.debug("申請登録パラメータ検査 終了");
		}
	}

	/**
	 * 申請登録
	 * 
	 * @param applicationRegisterForm パラメータ
	 * @throws Exception
	 */
	public int registerApplication(ApplicationRegisterForm applicationRegisterForm) throws RuntimeException {
		LOGGER.debug("申請登録 開始");
		try {

			// 申請種類ID
			Integer applicationTypeId = applicationRegisterForm.getApplicationTypeId();
			// 申請段階ID
			Integer applicationStepId = applicationRegisterForm.getApplicationStepId();
			// 申請版情報（初回登録なので、1で固定）
			int versionInformation = 1;

			// O_申請
			LOGGER.trace("O_申請登録 開始");
			// 申請ID
			int applicationId = applicationJdbc.insert(applicationTypeId);
			LOGGER.trace("O_申請登録 終了 新規申請ID: " + applicationId);

			// O_申請版情報
			LOGGER.trace("O_申請版情報登録 開始");
			// 申請ID
			applicationVersionInformationJdbc.insert(applicationId, applicationStepId, STATE_APPLIED);
			LOGGER.trace("O_申請版情報登録 終了");

			// O_申請者情報
			LOGGER.trace("O_申請者情報登録 開始");
			ApplicantInformation applicantInfo = buildApplicantInformation(
					applicationRegisterForm.getApplicantInformationItemForm(), false);
			applicantInfo.setApplicationId(applicationId);

			// 申請者情報ID
			int applicantId = applicantJdbc.insert(applicantInfo, CONTACT_ADDRESS_INVALID);
			LOGGER.trace("O_申請者情報登録 終了 新規申請者情報ID: " + applicantId);

			// O_申請者情報(連絡先)
			ApplicantInformation contactApplicantInfo = buildApplicantInformation(
					applicationRegisterForm.getApplicantInformationItemForm(), true);
			contactApplicantInfo.setApplicationId(applicationId);

			// 申請者情報ID(連絡先)
			int contactApplicantId = applicantJdbc.insert(contactApplicantInfo, CONTACT_ADDRESS_VALID);
			LOGGER.trace("O_申請者情報登録 終了 新規申請者情報ID(連絡先): " + contactApplicantId);

			// O_申請の申請者ID更新処理
			LOGGER.trace("O_申請の申請者ID更新 開始");
			if (applicationJdbc.updateApplicantId(applicationId, applicantId) != 1) {
				LOGGER.warn("O_申請の更新件数不正");
				throw new RuntimeException("申請情報への申請者情報ID設定に失敗");
			}
			LOGGER.trace("O_申請の申請者ID更新 終了");

			// O_申請区分登録
			LOGGER.trace("O_申請区分登録 開始");
			List<ApplicationCategorySelectionViewForm> categoryList = applicationRegisterForm
					.getApplicationCategories();
			for (ApplicationCategorySelectionViewForm category : categoryList) {
				List<ApplicationCategoryForm> applicationCategoryList = category.getApplicationCategory();
				for (ApplicationCategoryForm applicationCategory : applicationCategoryList) {
					applicationCategoryJdbc.insert(applicationCategory, applicationId, applicationStepId,
							versionInformation);
				}
			}
			LOGGER.trace("O_申請区分登録 終了");

			// O_申請ファイル
			// 申請ファイル登録は別APIで実施

			// F_申請地番登録
			LOGGER.trace("F_申請地番登録 開始");
			List<LotNumberForm> lotNumbers = applicationRegisterForm.getLotNumbers();
			// 地番ディゾルブ図形取得
			LotNumberDao lotnumberDao = new LotNumberDao(emf);
			final List<Integer> lotNumberIds = new ArrayList<Integer>();
			for (LotNumberForm aForm : lotNumbers) {
				lotNumberIds.add(aForm.getChibanId());
			}
			final String dissolvedLotNumberWkt = lotnumberDao.getDissolvedLotNumberWkt(lotNumberIds);
			if (dissolvedLotNumberWkt == null) {
				LOGGER.warn("登録用地番図形の取得に失敗");
				throw new RuntimeException("登録用地番図形の取得に失敗");
			}
			LOGGER.trace(dissolvedLotNumberWkt);
			// 地番テキスト取得
			ExportJudgeForm exportForm = new ExportJudgeForm();
			final String lotNumberText = exportForm.getAddressText(lotNumbers, lotNumberSeparators, separator);
			// F_申請地番登録
			applicationLotNumberJdbc.insert(dissolvedLotNumberWkt, lotNumberText, applicationId, epsg);
			LOGGER.trace("F_申請地番登録 終了");

			// O_回答登録
			LOGGER.trace("O_回答登録 開始");
			List<GeneralConditionDiagnosisResultForm> conditionList = applicationRegisterForm
					.getGeneralConditionDiagnosisResultForm();
			final Map<Integer, Map<String, String>> generalConditionAnswerMap = new HashMap<Integer, Map<String, String>>();

			for (GeneralConditionDiagnosisResultForm condition : conditionList) {
				if (condition.getResult()) {
					Answer answer = new Answer();
					answer.setApplicationId(applicationId); // 申請ID
					answer.setApplicationStepId(applicationStepId);// 申請段階ID
					answer.setJudgementId(condition.getJudgementId()); // 判定項目ID
					answer.setJudgementResultIndex(condition.getJudgementResultIndex());// 判定項目ID
					answer.setDepartmentAnswerId(0);// 部署回答ID（事前協議以外は「0」）
					answer.setDepartmentId("-1");// 部署ID（事前協議以外は「-1」）
					answer.setJudgementResult(condition.getSummary());// 判定結果
					answer.setAnswerDataType(ANSWER_DATA_TYPE_INSERT); // データ種類
					answer.setRegisterStatus(STATE_APPLIED); // 登録ステータス
					answer.setDiscussionItem(EMPTY);// 協議対象（事前協議のみ）
					answer.setVersionInformation(versionInformation);
					// 回答日数取得
					int answerDays = 0;
					if (condition.getAnswerDays() != null) {
						answerDays = condition.getAnswerDays();
					} else {
						LOGGER.trace("M_判定結果一覧取得 開始");
						AnswerDao dao = new AnswerDao(emf);
						List<CategoryJudgementResult> categoryJudgementResultList = dao.getJudgementResultList(
								condition.getJudgementId(), applicationTypeId, applicationStepId, null);
						LOGGER.trace("M_判定結果一覧取得 終了");
						if (categoryJudgementResultList.size() > 0) {
							answerDays = categoryJudgementResultList.get(0).getAnswerDays();
						}
					}
					if (condition.getAnswerRequireFlag()) {
						// 回答必須：初期回答なし

						answer.setAnswerContent(null);// 回答内容
						answer.setNotifiedText(null);// 通知テキスト
						answer.setCompleteFlag(false);// 完了フラグ
						answer.setNotifiedFlag(false);// 通知フラグ
						answer.setAnswerUpdateFlag(false);// 回答変更フラグ
						answer.setReApplicationFlag(null); // 再申請フラグ
						answer.setBusinessReApplicationFlag(null); // 事業者再申請フラグ
						answer.setDiscussionFlag(null);// 事前協議フラグ
						answer.setAnswerStatus(ANSWER_STATUS_NOTANSWERED);// 回答ステータス

					} else {
						// 回答任意：初期回答あり
						boolean reapplicationFlag = false;
						for (String reapplicationFalseText : reapplicationFalseTextList) {
							if (condition.getSummary().contains(reapplicationFalseText)) {
								// 再申請不要
								reapplicationFlag = false;
								break;
							}
						}
						for (String reapplicationTrueText : reapplicationTrueTextList) {
							if (condition.getSummary().contains(reapplicationTrueText)) {
								// 要再申請
								reapplicationFlag = true;
								break;
							}
						}
						boolean discussionFlag = false;
						for (String discussionFalseText : discussionFalseTextList) {
							if (condition.getSummary().contains(discussionFalseText)) {
								// 事前協議不要
								discussionFlag = false;
								break;
							}
						}
						for (String discussionTrueText : discussionTrueTextList) {
							if (condition.getSummary().contains(discussionTrueText)) {
								// 要事前協議
								discussionFlag = true;
								break;
							}
						}
						answer.setAnswerContent(condition.getDefaultAnswer());// 回答内容
						answer.setNotifiedText(condition.getDefaultAnswer());// 通知テキスト
						answer.setCompleteFlag(true);// 完了フラグ
						answer.setNotifiedFlag(true);// 通知フラグ
						answer.setAnswerUpdateFlag(false);// 回答変更フラグ
						answer.setReApplicationFlag(reapplicationFlag); // 再申請フラグ
						answer.setBusinessReApplicationFlag(reapplicationFlag); // 事業者再申請フラグ
						answer.setDiscussionFlag(discussionFlag);// 事前協議フラグ
						answer.setAnswerStatus(ANSWER_STATUS_ANSWERED);// 回答ステータス

					}

					// 回答期限日時
					answer.setDeadlineDatetime(getDeadlineDatetime(answerDays));
					Integer answerId = answerJdbc.insert(answer);
					Map<String, String> answerContentMap = new HashMap<String, String>();
					answerContentMap.put("answerId", answerId.toString());
					answerContentMap.put("answerContent", answer.getAnswerContent());
					generalConditionAnswerMap.put(condition.getJudgeResultItemId(), answerContentMap);

					// 自動回答済みの場合、事業者側で見えるため、回答履歴を作成する
					// 仮登録のリセット処理のために、登録の内容を回答履歴を作成する
					// 回答履歴登録
					if (answerHistoryJdbc.insert(answerId, "-1", answer.getNotifiedFlag()) != 1) {
						LOGGER.warn("回答履歴の更新件数不正");
						throw new RuntimeException("回答履歴の更新に失敗");
					}
				}

			}
			LOGGER.trace("O_回答登録 終了");

			// 概況診断レポート生成
			if (applicationRegisterForm.getFolderName() == null || applicationRegisterForm.getFolderName().equals("")) {
				LOGGER.trace("概況診断レポート生成 開始");
				GeneralConditionDiagnosisReportRequestForm reportForm = new GeneralConditionDiagnosisReportRequestForm();
				reportForm.setFolderName(applicationRegisterForm.getFolderName());
				reportForm.setLotNumbers(applicationRegisterForm.getLotNumbers());
				reportForm.setApplicationCategories(applicationRegisterForm.getApplicationCategories());
				reportForm.setGeneralConditionDiagnosisResults(
						applicationRegisterForm.getGeneralConditionDiagnosisResultForm());
				reportForm.setAnswerJudgementMap(generalConditionAnswerMap);
				Workbook wb = null;
				try {
					wb = exportJudgeReportWorkBook(reportForm);
					if (wb == null) {
						throw new RuntimeException("概況診断レポート生成に失敗");
					}
				} catch (Exception ex) {
					LOGGER.error("概況診断レポート生成でエラー発生", ex);
					throw new RuntimeException(ex);
				}
				LOGGER.trace("概況診断レポート生成 終了");

				// 概況診断レポートをアップロードし、O_申請ファイルに登録する
				LOGGER.trace("概況診断レポートアップロード 開始");
				// ファイル名は「概況診断結果_<申請ID>_yyyy_mm_dd.xlsx」
				SimpleDateFormat sdf = new SimpleDateFormat(applicationReportFileNameFooter);
				String fileName = applicationReportFileNameHeader + applicationId + sdf.format(new Date()) + ".xlsx";
				UploadApplicationFileForm uploadForm = new UploadApplicationFileForm();
				uploadForm.setApplicationId(applicationId);
				uploadForm.setApplicationStepId(applicationStepId);
				uploadForm.setApplicationFileId(applicationReportFileId);
				uploadForm.setUploadFileName(fileName);
				// 版情報
				uploadForm.setVersionInformation(
						getApplicatioFileMaxVersion(applicationId, applicationReportFileId, applicationStepId) + 1);
				// 拡張子
				uploadForm.setExtension("xlsx");
				uploadApplicationFile(uploadForm, wb);
				LOGGER.trace("概況診断レポートアップロード 終了");

			}

			return applicationId;
		} finally {
			LOGGER.debug("申請登録 終了");
		}
	}

	/**
	 * 申請ファイルアップロードパラメータチェック
	 * 
	 * @param uploadApplicationFileForm パラメータ
	 * @return 判定結果
	 */
	public boolean validateUploadApplicationFile(UploadApplicationFileForm uploadApplicationFileForm) {
		LOGGER.debug("申請ファイルアップロードパラメータチェック 開始");
		try {
			Integer applicationId = uploadApplicationFileForm.getApplicationId();
			String applicationFileId = uploadApplicationFileForm.getApplicationFileId();
			String fileName = uploadApplicationFileForm.getUploadFileName();
			MultipartFile uploadFile = uploadApplicationFileForm.getUploadFile();
			Integer versionInformation = uploadApplicationFileForm.getVersionInformation();
			Integer applicationStepId = uploadApplicationFileForm.getApplicationStepId();
			if (applicationId == null //
					|| applicationFileId == null || EMPTY.equals(applicationFileId) //
					|| fileName == null || EMPTY.equals(fileName) //
					|| uploadFile == null //
					|| versionInformation == null //
					|| applicationStepId == null //
			) {
				// パラメータが空
				LOGGER.warn("パラメータにnullまたは空が含まれる");
				return false;
			}

			// 申請ID
			if (applicationRepository.getApplicationList(applicationId).size() != 1) {
				// 申請IDが不正
				LOGGER.warn("申請IDで得られる申請データ件数が不正");
				return false;
			}

			// 申請ファイルID
			if (applicationFileMasterRepository.getApplicationFile(applicationFileId).size() == 0) {
				// 申請ファイルIDが不正
				LOGGER.warn("申請ファイルIDで得られる申請ファイルデータ件数が不正");
				return false;
			}
			return true;
		} finally {
			LOGGER.debug("申請ファイルアップロードパラメータチェック 終了");
		}
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
			// ※ファイルパスの構造を変わると、ファイル自体削除へ影響がある

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

	/**
	 * ファイルダウンロードパラメータチェック
	 * 
	 * @param form パラメータ
	 * @return 判定結果
	 */
	public boolean validateDownloadApplicationFile(UploadApplicationFileForm form) {
		LOGGER.debug("申請ファイルダウンロードパラメータチェック 開始");
		try {
			Integer fileId = form.getFileId();
			Integer applicationId = form.getApplicationId();
			String applicationFileId = form.getApplicationFileId();
			if (fileId == null //
					|| applicationId == null//
					|| applicationFileId == null || EMPTY.equals(applicationFileId)) {
				LOGGER.warn("パラメータにnullまたは空が含まれる");
				return false;
			}

			// ファイルID
			if (applicationFileRepository.getApplicationFile(fileId).size() != 1) {
				// ファイルIDが不正
				LOGGER.warn("ファイルIDで取得される申請ファイルデータ件数が不正");
				return false;
			}

			// 申請ID
			if (applicationRepository.getApplicationList(applicationId).size() != 1) {
				// 申請IDが不正
				LOGGER.warn("申請IDで取得される申請データ件数が不正");
				return false;
			}

			// 申請ファイルID(回答レポートの申請ファイルIDがapplication.propertitesに定義するため、M_申請ファイルに存在するかチェックをスキップ)
			if (!answerReportFileId.equals(applicationFileId)) {
				if (applicationFileMasterRepository.getApplicationFile(applicationFileId).size() == 0) {
					// 申請ファイルIDが不正
					LOGGER.warn("申請ファイルIDで取得される申請ファイルデータがない");
					return false;
				}
			}

			return true;
		} finally {
			LOGGER.debug("申請ファイルダウンロードパラメータチェック 終了");
		}
	}

	/**
	 * 申請ファイルダウンロード
	 * 
	 * @param form パラメータ
	 * @return 応答Entity
	 */
	public ResponseEntity<Resource> downloadApplicationFile(UploadApplicationFileForm form) {
		LOGGER.debug("申請ファイルダウンロード 開始");
		try {
			LOGGER.trace("申請ファイルデータ取得 開始");
			List<ApplicationFile> applicationFileList = applicationFileRepository.getApplicationFile(form.getFileId());
			ApplicationFile applicationFile = applicationFileList.get(0);
			LOGGER.trace("申請ファイルデータ取得 終了");

			// 絶対ファイルパス
			String absoluteFilePath = fileRootPath + applicationFile.getFilePath();
			Path filePath = Paths.get(absoluteFilePath);
			if (!Files.exists(filePath)) {
				// ファイルが存在しない
				LOGGER.warn("ファイルが存在しない");
				return new ResponseEntity<Resource>(HttpStatus.NOT_FOUND);
			}

			Resource resource = new PathResource(filePath);
			return ResponseEntity.ok().contentType(getContentType(filePath))
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
					.body(resource);
		} catch (Exception ex) {
			LOGGER.error("申請ファイルダウンロードで例外発生", ex);
			return new ResponseEntity<Resource>(HttpStatus.SERVICE_UNAVAILABLE);
		} finally {
			LOGGER.debug("申請ファイルダウンロード 終了");
		}
	}

	/**
	 * 照合ID、パスワードから申請者情報を取得し、申請IDを取得する
	 * 
	 * @param form パラメータ
	 * @return 申請ID
	 */
	public Integer getApplicationIdFromApplicantInfo(AnswerConfirmLoginForm form) {
		LOGGER.debug("申請者情報の申請ID取得 開始");
		try {
			String id = form.getLoginId();
			String password = form.getPassword();
			if (id != null && !EMPTY.equals(id) //
					&& password != null && !EMPTY.equals(password)) {
				String hash = AuthUtil.createHash(password);
				List<ApplicantInformation> applicantList = applicantInformationRepository.getApplicantList(id, hash);
				if (applicantList.size() == 1) {
					ApplicantInformation applicant = applicantList.get(0);
					return applicant.getApplicationId();
				} else {
					LOGGER.warn("申請者情報の取得件数が不正");
				}
			} else {
				LOGGER.warn("パラメータにnullまたは空が含まれる");
			}
			return null;
		} finally {
			LOGGER.debug("申請者情報の申請ID取得 終了");
		}
	}

	/**
	 * 照合情報取得
	 * 
	 * @param applicationRegisterResultForm O_申請登録結果フォーム
	 * @return 照合情報
	 */
	public AnswerConfirmLoginForm notifyCollationInformation(
			ApplicationRegisterResultForm applicationRegisterResultForm) {
		// O_申請の登録ステータスをチェック
		List<Application> applicationList = applicationRepository
				.getApplicationList(applicationRegisterResultForm.getApplicationId());
		if (applicationList.size() != 1) {
			LOGGER.error("申請データの件数が不正");
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		}

		Application application = applicationList.get(0);
		String registerStatus = application.getRegisterStatus();
		if (STATE_APPLIED.equals(registerStatus)) {
			// 申請済なので不正
			LOGGER.error("対象データは登録ステータスが申請済");
			throw new ResponseStatusException(HttpStatus.CONFLICT);
		}

		List<ApplicantInformation> applicantList = applicantInformationRepository
				.getApplicantList(application.getApplicationId(), CONTACT_ADDRESS_INVALID);
		if (applicantList.size() < 1) {
			LOGGER.error("申請者データの件数が不正");
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		}

		ApplicantInformation applicant = applicantList.get(0);
		String id = applicant.getCollationId();
		String password = applicant.getPassword();
		if ((id != null && !EMPTY.equals(id)) //
				|| (password != null && !EMPTY.equals(password))) {
			LOGGER.error("申請者データはIDまたはパスワードが設定済");
			throw new ResponseStatusException(HttpStatus.CONFLICT);
		}

		AnswerConfirmLoginForm form = new AnswerConfirmLoginForm();

		// ID、パスワードを設定
		// IDは申請者情報IDを使用する
		id = String.format("%0" + applicantIdLength + "d", applicant.getApplicantId());
		password = AuthUtil.generatePassword(passwordCharacter, passwordLength);
		form.setLoginId(id);
		form.setPassword(password);

		if (applicantJdbc.updateId(applicant.getApplicantId(), id, AuthUtil.createHash(password)) != 1) {
			LOGGER.error("申請者情報の更新件数が不正");
			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}

		// 申請情報の登録ステータスを更新
		if (applicationJdbc.updateRegisterStatus(application.getApplicationId(), STATE_APPLIED) != 1) {
			LOGGER.error("申請情報の更新件数が不正");
			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}

		// 事業者に回答確認ID/パスワード通知送付
		String mailAddress = applicant.getMailAddress();
		List<ApplicantInformation> contactApplicantList = applicantInformationRepository
				.getApplicantList(application.getApplicationId(), CONTACT_ADDRESS_VALID);
		if (contactApplicantList.size() > 0) {
			ApplicantInformation contactApplicant = contactApplicantList.get(0);
			mailAddress = contactApplicant.getMailAddress();
		}
		sendNoticeMailToBusinessUser(form.getLoginId(), form.getPassword(), mailAddress, application,
				applicationRegisterResultForm);

		// 行政に申請受付通知送付
		sendNoticeMailToGovernmentUser(application, applicant, applicationRegisterResultForm.getAnswerExpectDays(),
				applicationRegisterResultForm.getApplicationStepId());

		// 回答通知担当課に受付通知（照合ID・パスワードを含む）送付
		sendNoticeMailToGovernmentNotificationUser(id, password, application.getApplicationId(),
				applicationRegisterResultForm.getApplicationStepId());

		return form;
	}

	/**
	 * 事業者に回答確認ID/パスワード通知送付
	 * 
	 * @param loginId                       ログインID
	 * @param password                      パスワード
	 * @param mailAddress                   宛先メールアドレス
	 * @param application                   O_申請
	 * @param applicationRegisterResultForm O_申請登録結果フォーム
	 */
	private void sendNoticeMailToBusinessUser(String loginId, String password, String mailAddress,
			Application application, ApplicationRegisterResultForm applicationRegisterResultForm) {
		MailItem businessItem = new MailItem();
		businessItem.setId(loginId); // 回答確認ID
		businessItem.setPassword(password); // パスワード
		businessItem.setApplicationId(application.getApplicationId().toString()); // 申請ID
		businessItem.setApplicationTypeName(getApplicationTypeName(application.getApplicationTypeId())); // 申請種類名
		businessItem
				.setApplicationStepName(getApplicationStepName(applicationRegisterResultForm.getApplicationStepId())); // 申請段階名
		businessItem.setVersionInformation(getVersionInformation(application.getApplicationId(),
				applicationRegisterResultForm.getApplicationStepId()).toString()); // 版情報
		Calendar now = Calendar.getInstance();
		businessItem.setApplicationMonth(Integer.valueOf(now.get(Calendar.MONTH) + 1).toString());// 申請月
		businessItem.setApplicationDay(Integer.valueOf(now.get(Calendar.DAY_OF_MONTH)).toString());// 申請日
		businessItem.setAnswerDays(applicationRegisterResultForm.getAnswerExpectDays().toString());// 回答日数

		String subject = getMailPropValue(MailMessageUtil.KEY_BUSSINESS_ACCEPT_SUBJECT, businessItem);
		String body = getMailPropValue(MailMessageUtil.KEY_BUSSINESS_ACCEPT_BODY, businessItem);

		// 事業者に回答確認ID/パスワード通知送付
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

	/**
	 * 行政に受付通知送付
	 * 
	 * @param application       申請情報
	 * @param applicant         申請者情報
	 * @param answerExpectDays  回答予定日数
	 * @param applicationStepId 申請段階ID
	 */
	private void sendNoticeMailToGovernmentUser(Application application, ApplicantInformation applicant,
			Integer answerExpectDays, Integer applicationStepId) {
		MailItem baseItem = new MailItem();
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
		baseItem.setApplicationId(application.getApplicationId().toString()); // 申請ID
		baseItem.setApplicationTypeName(getApplicationTypeName(application.getApplicationTypeId())); // 申請種類名
		baseItem.setApplicationStepName(getApplicationStepName(applicationStepId)); // 申請段階名
		baseItem.setVersionInformation(
				getVersionInformation(application.getApplicationId(), applicationStepId).toString()); // 版情報

		// 地番
		ApplicationDao applicationDao = new ApplicationDao(emf);
		List<ApplyLotNumber> lotNumbersList = applicationDao.getApplyingLotNumberList(application.getApplicationId(),
				lonlatEpsg);
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
		List<Answer> answerList = applicationDao.getAnswerList(application.getApplicationId(), true, applicationStepId,
				0);

		// 部署別の対象・判定結果リスト
		Map<String, List<MailResultItem>> resultMap = new HashMap<String, List<MailResultItem>>();
		Map<String, Integer> answerDaysMap = new HashMap<String, Integer>();

		for (Answer answer : answerList) {
			Integer answerId = answer.getAnswerId();
			List<CategoryJudgementResult> categoryList = answerDao.getJudgementResultByAnswerId(answerId);
			if (categoryList.size() == 0) {
				LOGGER.warn("M_判定結果の値が取得できない 回答ID: " + answerId);
				continue;
			}
			CategoryJudgementResult category = categoryList.get(0);

			Integer answerDays = 0;
			if (category.getAnswerDays() != null) {
				answerDays = category.getAnswerDays();
			}

			MailResultItem item = new MailResultItem();
			item.setTarget(category.getTitle()); // 対象
			item.setResult(answer.getJudgementResult()); // 判定結果

			// 回答に紐づく部署
			List<Department> answerDepartmentList = answerDao.getDepartmentList(answerId);
			for (Department answerDepartment : answerDepartmentList) {
				String answerDepartmentId = answerDepartment.getDepartmentId();
				if (!resultMap.containsKey(answerDepartmentId)) {
					// 初期化
					resultMap.put(answerDepartmentId, new ArrayList<MailResultItem>());
				}
				// 部署ごとに集約
				resultMap.get(answerDepartmentId).add(item);

				if (!answerDaysMap.containsKey(answerDepartmentId)) {
					// 初期化
					answerDaysMap.put(answerDepartmentId, answerDays);
				} else {
					if (answerDaysMap.get(answerDepartmentId) < answerDays) {
						answerDaysMap.replace(answerDepartmentId, answerDays);
					}
				}
			}
		}

		// 行政回答担当部署（送信先）取得
		// 申請段階に紐づく全部署
		List<Department> departmentList = applicationDao.getDepartmentList(application.getApplicationId(),
				applicationStepId);

		// 行政に申請受付通知送付
		for (Department department : departmentList) {
			if (!resultMap.containsKey(department.getDepartmentId())) {
				// 対象・判定結果がないのでスキップ
				continue;
			}
			MailItem governmentItem = baseItem.clone();
			// 部署ごとに対象・判定結果は異なるのでここで設定
			governmentItem.setResultList(resultMap.get(department.getDepartmentId()));
			governmentItem.setAnswerDays(answerDaysMap.get(department.getDepartmentId()).toString());// 回答日数

			String subject = getMailPropValue(MailMessageUtil.KEY_ACCEPT_SUBJECT, governmentItem);
			String body = getMailPropValue(MailMessageUtil.KEY_ACCEPT_BODY, governmentItem);

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

			// 回答権限ありの担当課に申請受付通知メールを送付する
			// ⇒事前相談の場合、回答通知担当課は通知権限持てる部署です。
			Authority authority = getAuthority(department.getDepartmentId(), applicationStepId);
			if (AUTH_TYPE_SELF.equals(authority.getNotificationAuthorityFlag())
					|| AUTH_TYPE_ALL.equals(authority.getNotificationAuthorityFlag())) {

				// 本メソッドが、申請登録APIから呼び出すため、申請段階が事前相談に固定するため、送信先が通知権限持てる担当課です。
				// 宛先
				String mailAddress = department.getMailAddress();

				MailItem mailItem = baseItem.clone();
				mailItem.setResultList(resultMap.get(department.getDepartmentId()));
				mailItem.setAnswerDays(answerExpectDays.toString());// 回答日数
				mailItem.setComment1(EMPTY);// コメント。事前相談の場合、コメントがない。

				subject = getMailPropValue(MailMessageUtil.KEY_ACCEPT_NOTIFICATION_SUBJECT, mailItem);
				body = getMailPropValue(MailMessageUtil.KEY_ACCEPT_NOTIFICATION_BODY, mailItem);
				LOGGER.trace(mailAddress);
				LOGGER.trace(subject);
				LOGGER.trace(body);
				try {
					final String[] mailAddressList = mailAddress.split(",");
					for (String aMailAddress : mailAddressList) {
						mailSendutil.sendMail(aMailAddress, subject, body);
					}
				} catch (Exception e) {
					LOGGER.error("メール送信時にエラー発生", e);
					throw new RuntimeException(e);
				}
			}
		}

		// 担当している回答条項がない部署が、他部署操作可能の通知権限が持っている場合、回答通知できるため、申請受付通知を送付する
		List<Authority> authorityList = authorityRepository.getAuthorityListByApplicationStepId(applicationStepId);
		for (Authority departmentAuthority : authorityList) {

			// 他部署に通知できる場合、
			if (AUTH_TYPE_ALL.equals(departmentAuthority.getNotificationAuthorityFlag())) {

				// 通知済みであるか判断
				boolean isNotified = false;
				for (Department department : departmentList) {
					if (departmentAuthority.getDepartmentId().equals(department.getDepartmentId())) {
						isNotified = true;
						break;
					}
				}

				// 通知済みの場合、スキップ
				if (isNotified) {
					continue;
				}

				List<Department> entityList = departmentRepository
						.getDepartmentListById(departmentAuthority.getDepartmentId());
				if (entityList.size() == 0) {
					LOGGER.warn("M_部署情報が存在しません。部署ID：" + departmentAuthority.getDepartmentId());
					throw new RuntimeException();
				}

				String mailAddress = entityList.get(0).getMailAddress();
				MailItem mailItem = baseItem.clone();
				mailItem.setAnswerDays(answerExpectDays.toString());// 回答日数

				String subject = getMailPropValue(MailMessageUtil.KEY_ACCEPT_NOTIFICATION_SUBJECT, mailItem);
				String body = getMailPropValue(MailMessageUtil.KEY_ACCEPT_NOTIFICATION_BODY, mailItem);

				LOGGER.trace(mailAddress);
				LOGGER.trace(subject);
				LOGGER.trace(body);

				try {
					final String[] mailAddressList = mailAddress.split(",");
					for (String aMailAddress : mailAddressList) {
						mailSendutil.sendMail(aMailAddress, subject, body);
					}
				} catch (Exception e) {
					LOGGER.error("メール送信時にエラー発生", e);
					throw new RuntimeException(e);
				}
			}
		}
	}

	/**
	 * O_申請Entityから申請情報検索結果データフォームを生成
	 * 
	 * @param dao                                 O_申請DAO
	 * @param application                         O_申請Entity
	 * @param applicationSearchResultList         M_申請情報検索結果情報
	 * @param lotNumberSearchResultDefinitionList 地番検索結果定義一覧
	 * @return 申請情報検索結果データフォーム
	 */
	private ApplicationSearchResultForm getApplicationSearchResultFormFromEntity(ApplicationDao dao,
			Application application, List<ApplicationSearchResult> applicationSearchResultList) {
		LOGGER.debug("申請情報検索結果データフォームを生成 開始");
		try {
			ApplicationSearchResultForm form = new ApplicationSearchResultForm();

			// 申請ID
			int applicationId = application.getApplicationId();
			// 申請種類ID
			int applicationTypeId = application.getApplicationTypeId();

			form.setApplicationId(applicationId);
			// 申請ステータス
			form.setStatusCode(application.getStatus());
			// 申請IDの最大申請段階ID、版情報
			Integer maxApplicationStepId = null;
			Integer maxApplicationVersionInformation = null;
			List<ApplicationVersionInformation> latestApplicationVersion = applicationVersionInformationRepository
					.getLatestApplicationVersionInformation(applicationId);
			if (latestApplicationVersion.size() > 0) {
				maxApplicationStepId = latestApplicationVersion.get(0).getApplicationStepId();
				maxApplicationVersionInformation = latestApplicationVersion.get(0).getVersionInformation();
			}
			// 地番
			List<ApplyLotNumberForm> formList = new ArrayList<ApplyLotNumberForm>();
			List<ApplyLotNumber> lotNumbersList = dao.getApplyingLotNumberList(applicationId, lonlatEpsg);
			for (ApplyLotNumber lotNumbers : lotNumbersList) {
				formList.add(getApplyingLotNumberFormFromEntity(lotNumbers));
			}
			form.setLotNumbers(formList);

			Map<String, Object> attributes = new LinkedHashMap<String, Object>();

			// O_申請区分
			LOGGER.trace("O_申請区分取得 開始");
			int targetApplicationStepId = maxApplicationStepId;
			int targetApplicationVersionInformation = maxApplicationVersionInformation;
			// 許可判定の場合事前協議最終版の情報を取得
			if (maxApplicationStepId.equals(APPLICATION_STEP_ID_3)) {
				targetApplicationStepId = APPLICATION_STEP_ID_2;
				int versionInformationStep2 = -1;
				List<ApplicationVersionInformation> step2VersionInformation = applicationVersionInformationRepository
						.findByApplicationSteId(applicationId, APPLICATION_STEP_ID_2);
				if (step2VersionInformation.size() > 0) {
					versionInformationStep2 = step2VersionInformation.get(0).getVersionInformation();
				}
				targetApplicationVersionInformation = versionInformationStep2;
			}
			List<ApplicationCategory> categoryList = dao.getApplicationCategoryList(applicationId,
					targetApplicationStepId, targetApplicationVersionInformation);
			LOGGER.trace("O_申請区分取得 終了");

			// M_申請区分
			LOGGER.trace("M_申請区分取得 開始");
			List<ApplicationCategoryMaster> categoryMasterList = dao.getApplicationCategoryMasterList(applicationId,
					targetApplicationStepId, targetApplicationVersionInformation);
			LOGGER.trace("M_申請区分取得 終了");

			// O_申請者情報
			LOGGER.trace("O_申請者情報取得 開始");
			List<ApplicantInformation> applicantList = dao.getApplicantInformationList(applicationId);
			LOGGER.trace("O_申請者情報取得 終了");

			// O_回答
			LOGGER.trace("O_回答取得 開始");
			List<Answer> answerList = new ArrayList<Answer>();
			if (maxApplicationStepId.equals(APPLICATION_STEP_ID_1)
					|| maxApplicationStepId.equals(APPLICATION_STEP_ID_3)) {
				// 事前相談、許可判定
				answerList = dao.getAnswerList(applicationId, true, maxApplicationStepId, 0);
			} else if (maxApplicationStepId.equals(APPLICATION_STEP_ID_2)) {
				// 事前協議
				// 部署回答一覧取得
				List<DepartmentAnswer> departmentAnswerList = dao.getDepartmentAnswerList(applicationId,
						maxApplicationStepId);
				// 部署回答に紐づく回答一覧取得
				for (DepartmentAnswer departmentAnswer : departmentAnswerList) {
					List<Answer> tmpAnswerList = dao.getAnswerList(applicationId, true, maxApplicationStepId,
							departmentAnswer.getDepartmentAnswerId());
					answerList.addAll(tmpAnswerList);
				}
			}
			LOGGER.trace("O_回答取得 終了");

			// M_部署
			LOGGER.trace("M_部署取得 開始");
			List<Department> departmentList = dao.getDepartmentList(applicationId, maxApplicationStepId);
			LOGGER.trace("M_部署取得 終了");

			// TODO 申請追加情報の取得（申請段階により項目異なるので表示側含めて考慮必要）

			// attributeの設定
			for (ApplicationSearchResult applicationSearchResult : applicationSearchResultList) {
				String responseKey = applicationSearchResult.getResponseKey();
				String refType = applicationSearchResult.getReferenceType();
				String tableName = applicationSearchResult.getTableName();
				String columnName = applicationSearchResult.getTableColumnName();

				List<Object> valueList = new ArrayList<Object>();

				if (typeCategory.equals(refType)) {
					// 申請区分
					if (tableNameApplicationCategory.equals(tableName)) {
						// O_申請区分
						for (ApplicationCategory category : categoryList) {
							if (columnNameApplicationId.equals(columnName)) {
								// 申請ID
								valueList.add(category.getApplicationId());
							} else if (columnNameApplicationViewId.equals(columnName)) {
								// 画面ID
								valueList.add(category.getViewId());
							} else if (columnNameApplicationCategoryId.equals(columnName)) {
								// 申請区分ID
								valueList.add(category.getCategoryId());
							} else {
								LOGGER.warn("非対応のカラム名: " + columnName);
							}
						}
					} else if (tableNameApplicationCategoryMaster.equals(tableName)) {
						// M_申請区分
						for (ApplicationCategoryMaster categoryMaster : categoryMasterList) {
							// 指定したViewIdと等しいレコードを追加する
							if (categoryMaster.getViewId().equals(columnName)) {
								valueList.add(categoryMaster.getLabelName());
							}
						}
					}
				} else if (typeApplicant.equals(refType)) {
					// 申請者情報
					if (tableNameApplicantInformation.equals(tableName)) {
						// 「カラム名,連絡先フラグ」のフォーマットでcolumnNameにセットされる想定
						final String[] columnInfo = columnName.split(",");
						final String extractColumnName = columnInfo[0];
						// 連絡先フラグ
						final String contractAddressFlag = (columnInfo.length > 1) ? columnInfo[1] : "0";
						// O_申請者情報
						for (ApplicantInformation applicant : applicantList) {
							final String applicantContactAddressFlag = (applicant.getContactAddressFlag()) ? "1" : "0";
							if (applicantContactAddressFlag.equals(contractAddressFlag)) {
								if (columnNameApplicantInformationApplicationId.equals(extractColumnName)) {
									// 申請ID
									valueList.add(applicant.getApplicationId());
								} else if (columnNameApplicantInformationApplicantId.equals(extractColumnName)) {
									// 申請者情報ID
									valueList.add(applicant.getApplicantId());
								} else if (columnNameApplicantInformationItem1.equals(extractColumnName)) {
									// 項目1
									valueList.add(applicant.getItem1());
								} else if (columnNameApplicantInformationItem2.equals(extractColumnName)) {
									// 項目2
									valueList.add(applicant.getItem2());
								} else if (columnNameApplicantInformationItem3.equals(extractColumnName)) {
									// 項目3
									valueList.add(applicant.getItem3());
								} else if (columnNameApplicantInformationItem4.equals(extractColumnName)) {
									// 項目4
									valueList.add(applicant.getItem4());
								} else if (columnNameApplicantInformationItem5.equals(extractColumnName)) {
									// 項目5
									valueList.add(applicant.getItem5());
								} else if (columnNameApplicantInformationItem6.equals(extractColumnName)) {
									// 項目6
									valueList.add(applicant.getItem6());
								} else if (columnNameApplicantInformationItem7.equals(extractColumnName)) {
									// 項目7
									valueList.add(applicant.getItem7());
								} else if (columnNameApplicantInformationItem8.equals(extractColumnName)) {
									// 項目8
									valueList.add(applicant.getItem8());
								} else if (columnNameApplicantInformationItem9.equals(extractColumnName)) {
									// 項目9
									valueList.add(applicant.getItem9());
								} else if (columnNameApplicantInformationItem10.equals(extractColumnName)) {
									// 項目10
									valueList.add(applicant.getItem10());
								} else if (columnNameApplicantInformationMailAddress.equals(extractColumnName)) {
									// メールアドレス
									valueList.add(applicant.getMailAddress());
								} else if (columnNameApplicantInformationCollationId.equals(extractColumnName)) {
									// 照合ID
									valueList.add(applicant.getCollationId());
								} else if (columnNameApplicantInformationPassword.equals(extractColumnName)) {
									// パスワード
									valueList.add(applicant.getPassword());
								} else {
									LOGGER.warn("非対応のカラム名: " + columnName);
								}
							}
						}
					}
				} else if (typeOther.equals(refType)) {
					// その他
					if (tableNameAnswer.equals(tableName)) {
						// O_回答
						for (Answer answer : answerList) {
							if (columnNameAnswerId.equals(columnName)) {
								// 回答ID
								valueList.add(answer.getAnswerId());
							} else if (columnNameAnswerApplicationId.equals(columnName)) {
								// 申請ID
								valueList.add(answer.getApplicationId());
							} else if (columnNameAnswerJudgementId.equals(columnName)) {
								// 判定項目ID
								valueList.add(answer.getJudgementId());
							} else if (columnNameAnswerJudgementResult.equals(columnName)) {
								// 判定結果
								valueList.add(answer.getJudgementResult());
							} else if (columnNameAnswerContent.equals(columnName)) {
								// 回答内容
								valueList.add(answer.getAnswerContent());
							} else if (columnNameAnswerNotifiedText.equals(columnName)) {
								// 通知テキスト
								valueList.add(answer.getNotifiedText());
							} else if (columnNameAnswerRegisterDatetime.equals(columnName)) {
								// 登録日時
								valueList.add(answer.getRegisterDatetime());
							} else if (columnNameAnswerUpdateDatetime.equals(columnName)) {
								// 更新日時
								valueList.add(answer.getUpdateDatetime());
							} else if (columnNameAnswerCompleteFlag.equals(columnName)) {
								// 完了フラグ
								valueList.add(answer.getCompleteFlag());
							} else if (columnNameAnswerNotifiedFlag.equals(columnName)) {
								// 通知フラグ
								valueList.add(answer.getNotifiedFlag());
							} else {
								LOGGER.warn("非対応のカラム名: " + columnName);
							}
						}
					} else if (tableNameDepartment.equals(tableName)) {
						// M_部署
						for (Department department : departmentList) {
							if (columnNameDepartmentId.equals(columnName)) {
								// 部署ID
								valueList.add(department.getDepartmentId());
							} else if (columnNameDepartmentName.equals(columnName)) {
								// 部署名
								valueList.add(department.getDepartmentName());
							} else if (columnNameDepartmentAnswerAuthorityFlag.equals(columnName)) {
								// 回答権限フラグ
								valueList.add(department.getAnswerAuthorityFlag());
							} else if (columnNameDepartmentMailAddress.equals(columnName)) {
								// メールアドレス
								valueList.add(department.getMailAddress());
							} else {
								LOGGER.warn("非対応のカラム名: " + columnName);
							}
						}
					} else if (tableNameApplication.equals(tableName)) {
						// O_申請
						if (columnNameStatus.equals(columnName)) {
							// ステータス
							try {
								final String statusText = (maxApplicationVersionInformation != null)
										? versionInformationText.replace(versionInformationReplaceText,
												maxApplicationVersionInformation.toString())
												+ getStatusMap().get(application.getStatus())
										: getStatusMap().get(application.getStatus());
								valueList.add(statusText);
							} catch (Exception e) {
								valueList.add("");
							}
						}
						// 申請ID
						if (columnNameOaApplicationId.equals(columnName)) {
							valueList.add(application.getApplicationId() + "");
						}
					} else {
						LOGGER.warn("非対応のテーブル名: " + tableName);
					}
				} else {
					LOGGER.warn("非対応の参照タイプ: " + refType);
				}

				// 要素をカンマ結合して文字列に変換後に設定 -> リストのまま格納する
				if (!attributes.containsKey(responseKey)) {
					attributes.put(responseKey, valueList);
				}
			}

			form.setAttributes(attributes);

			return form;
		} finally {
			LOGGER.debug("申請情報検索結果データフォームを生成 終了");
		}
	}

	/**
	 * M_申請者情報項目EntityをM_申請者情報項目フォームに詰めなおす
	 * 
	 * @param entity M_申請者情報項目Entity
	 * @param value  登録情報値
	 * @return M_申請者情報項目フォーム
	 */
	private ApplicantInformationItemForm getApplicantInformationItemFormFromEntity(ApplicantInformationItem entity,
			String value, String contactValue) {
		ApplicantInformationItemForm form = new ApplicantInformationItemForm();
		form.setDisplayFlag(entity.getDisplayFlag());
		form.setId(entity.getApplicantInformationItemId());
		form.setMailAddress(entity.getMailAddress());
		form.setName(entity.getItemName());
		form.setOrder(entity.getDisplayOrder());
		form.setRegularExpressions(entity.getRegex());
		form.setRequireFlag(entity.getRequireFlag());
		form.setSearchConditionFlag(entity.getSearchConditionFlag());
		form.setItemType(entity.getItemType());
		form.setValue(value);
		form.setContactValue(contactValue);
		form.setAddInformationItemFlag(entity.getAddInformationItemFlag());
		form.setContactAddressFlag(entity.getContactAddressFlag());

		List<ApplicationStepForm> applicationStepFormList = new ArrayList<ApplicationStepForm>();
		if (entity.getApplicationStep() != null && !EMPTY.equals(entity.getApplicationStep())) {
			String[] applicationStepIdList = entity.getApplicationStep().split(COMMA);
			for (String applicationStepId : applicationStepIdList) {
				LOGGER.trace("申請段階取得 開始");
				List<ApplicationStep> results = applicationStepRepository
						.findByApplicationStepId(Integer.valueOf(applicationStepId));
				if (results.size() > 0) {
					applicationStepFormList.add(geApplicationStepFormFromEntity(results.get(0)));
				}
				LOGGER.trace("申請段階取得 開始");
			}
		}
		form.setApplicationSteps(applicationStepFormList);

		List<ApplicantInformationItemOptionForm> applicantInformationItemOptionFormList = new ArrayList<ApplicantInformationItemOptionForm>();
		// 項目型がドロップダウンの場合、選択肢取得
		if (ITEM_TYPE_SINGLE_SELECTION.equals(entity.getItemType())
				|| ITEM_TYPE_MULTIPLE_SELECTION.equals(entity.getItemType())) {

			List<ApplicantInformationItemOption> applicantInformationItemOptionList = applicantInformationItemOptionRepository
					.findByApplicantInformationItemId(entity.getApplicantInformationItemId());

			for (ApplicantInformationItemOption applicantInformationItemOption : applicantInformationItemOptionList) {
				applicantInformationItemOptionFormList
						.add(getApplicantInformationItemOptionFormFromEntity(applicantInformationItemOption, value));
			}
		}

		form.setItemOptions(applicantInformationItemOptionFormList);

		return form;
	}

	/**
	 * M_申請者情報項目選択肢Entityを申請者情報項目選択肢フォームに詰めなおす
	 * 
	 * @param entity
	 * @param value
	 * @return
	 */
	private ApplicantInformationItemOptionForm getApplicantInformationItemOptionFormFromEntity(
			ApplicantInformationItemOption entity, String value) {
		ApplicantInformationItemOptionForm form = new ApplicantInformationItemOptionForm();
		form.setId(entity.getApplicantInformationItemOptionId());
		form.setItemId(entity.getApplicantInformationItemId());
		form.setDisplayOrder(entity.getDisplayOrder());
		form.setContent(entity.getApplicantInformationItemOptionName());

		if (value == null || EMPTY.equals(value)) {
			form.setChecked(false);
		} else {
			String[] valueList = value.split(COMMA);
			boolean result = Arrays.asList(valueList).contains(entity.getApplicantInformationItemOptionId());
			form.setChecked(result);
		}

		return form;
	}

	/**
	 * M_申請ファイルEntityをM_申請ファイルフォームに詰めなおす
	 * 
	 * @param entity M_申請ファイルEntity
	 * @return M_申請ファイルフォーム
	 */
	private ApplicationFileForm getApplicationFileFormFromEntity(ApplicationFileMaster entity) {
		ApplicationFileForm form = new ApplicationFileForm();
		form.setApplicationFileId(entity.getApplicationFileId());
		form.setApplicationFileName(entity.getUploadFileName());
		form.setJudgementItemId(entity.getJudgementItemId());
		form.setRequireFlag(entity.getRequireFlag());
		form.setExtension(entity.getExtension());
		return form;
	}

	/**
	 * O_申請ファイルEntityをO申請ファイルフォームに詰めなおす
	 * 
	 * @param entity O_申請ファイルEntity
	 * @return O_申請ファイルフォーム
	 */
	private UploadApplicationFileForm getUploadApplicationFileFormFromEntity(ApplicationFile entity) {
		UploadApplicationFileForm form = new UploadApplicationFileForm();
		form.setFileId(entity.getFileId());
		form.setApplicationId(entity.getApplicationId());
		form.setApplicationStepId(entity.getApplicationStepId());
		form.setApplicationFileId(entity.getApplicationFileId());
		form.setUploadFileName(entity.getUploadFileName());
		form.setFilePath(entity.getFilePath());
		form.setVersionInformation(entity.getVersionInformation());
		form.setExtension(entity.getExtension());
		DateTimeFormatter datetimeformatter = DateTimeFormatter.ofPattern("yyyy/MM/dd/ HH:mm");
		String datetimeformated = (entity.getUploadDatetime() != null)
				? datetimeformatter.format(entity.getUploadDatetime())
				: "";
		form.setUploadDatetime(datetimeformated);
		form.setDirectionDepartmentId(entity.getDirectionDepartment());
		form.setReviseContent(entity.getReviseContent());

		List<ApplicationStep> aplicationStepList = applicationStepRepository
				.findByApplicationStepId(entity.getApplicationStepId());
		if (aplicationStepList.size() == 0) {
			LOGGER.warn("申請情報の申請段階が存在しません。");
			return null;
		}
		form.setApplicationStepName(aplicationStepList.get(0).getApplicationStepName());

		return form;
	}

	/**
	 * M_申請情報検索結果EntityをM_申請情報検索結果フォームに詰めなおす
	 * 
	 * @param entity M_申請情報検索結果Entity
	 * @return M_申請情報検索結果フォーム
	 */
	private ApplicationInformationSearchResultHeaderForm getApplicationInformationSearchResultHeaderFormFromEntity(
			ApplicationSearchResult entity) {
		ApplicationInformationSearchResultHeaderForm form = new ApplicationInformationSearchResultHeaderForm();
		form.setDisplayColumnName(entity.getDisplayColumnName());
		form.setReferenceType(entity.getReferenceType());
		form.setResonseKey(entity.getResponseKey());
		form.setTableWidth(entity.getTableWidth());
		form.setDisplayOrder(entity.getDisplayOrder());
		return form;
	}

	/**
	 * O_回答ファイルEntityを回答情報フォームに詰めなおす
	 * 
	 * @param entity             O_回答ファイルEntity
	 * @param departmentId       ログインユーザーの部署・権限情報
	 * @param isGoverment        行政かどうか
	 * @param applicationTypeId  申請種類ID
	 * @param notificableByAdmin 部署ごとに、事業者へ通知済み かつ
	 *                           事業者合意未登録の回答があるかどうか（事前協議の回答通知選択可能判断用）
	 * 
	 * @return 回答情報フォーム
	 */
	private AnswerForm getAnswerFormFromEntity(Answer entity, GovernmentUserAndAuthority governmentUserInfo,
			boolean isGoverment, Integer applicationTypeId, boolean notificableByAdmin) {
		LOGGER.debug("回答情報フォーム生成 開始");
		try {

			// 申請段階ID
			Integer applicationStepId = entity.getApplicationStepId();

			AnswerForm form = new AnswerForm();
			form.setAnswerId(entity.getAnswerId());
			form.setDepartmentAnswerId(entity.getDepartmentAnswerId());
			form.setJudgementResult(entity.getJudgementResult());
			if (isGoverment) {
				// 行政
				form.setAnswerContent(entity.getAnswerContent());
			} else {
				// 事業者
				form.setAnswerContent(entity.getNotifiedText());
			}
			form.setUpdateDatetime(entity.getUpdateDatetime());
			form.setCompleteFlag(entity.getCompleteFlag());
			form.setNotifiedFlag(entity.getNotifiedFlag());
			form.setAnswerUpdateFlag(entity.getAnswerUpdateFlag());

			// 区分判定一覧

			AnswerJudgementForm judgeForm = new AnswerJudgementForm();
			// 判定結果
			CategoryJudgementResult judgementResult = new CategoryJudgementResult();
			AnswerDao dao = new AnswerDao(emf);

			if (ANSWER_DATA_TYPE_GOVERNMENT_ADD.equals(entity.getAnswerDataType())
					|| EMPTY.equals(entity.getJudgementId())) {
				// 行政で追加された条項は、関連する判定区分がないので、区分判定IDがない
				judgeForm.setJudgementId(entity.getJudgementId());
				judgeForm.setTitle(govermentAddAnswerTitle);
			} else {

				LOGGER.trace("M_判定結果一覧取得 開始");
				List<CategoryJudgementResult> categoryJudgementResultList = dao.getJudgementResultList(
						entity.getJudgementId(), applicationTypeId, applicationStepId, entity.getDepartmentId());
				LOGGER.trace("M_判定結果一覧取得 終了");
				if (categoryJudgementResultList.size() > 0) {
					judgementResult = categoryJudgementResultList.get(0);
					judgeForm.setJudgementId(entity.getJudgementId());
					judgeForm.setTitle(judgementResult.getTitle());
				}
			}

			// 部署リスト
			LOGGER.trace("部署リスト取得 開始");
			List<DepartmentForm> departmentFormList = new ArrayList<DepartmentForm>();
			// 事前協議の場合、担当部署がO_回答に保持
			if (APPLICATION_STEP_ID_2.equals(entity.getApplicationStepId())) {
				List<Department> departmentList = departmentRepository.getDepartmentListById(entity.getDepartmentId());
				if (departmentList.size() > 0) {
					departmentFormList.add(getDepartmentFormFromEntity(departmentList.get(0)));
				}
			} else {

				LOGGER.trace("M_判定結果一覧取得 開始");
				List<CategoryJudgementAuthority> judgementAuthorityList = judgementAuthorityRepository
						.getJudgementAuthorityList(entity.getJudgementId());
				LOGGER.trace("M_判定結果一覧取得 終了");

				for (CategoryJudgementAuthority judgementAuthority : judgementAuthorityList) {
					List<Department> result = departmentRepository
							.getDepartmentListById(judgementAuthority.getDepartmentId());
					if (result.size() > 0) {
						departmentFormList.add(getDepartmentFormFromEntity(result.get(0)));
					}
				}

			}
			judgeForm.setDepartments(departmentFormList);
			LOGGER.trace("部署リスト取得 終了");
			form.setJudgementInformation(judgeForm);

			String businessPassStatus = null;
			if (entity.getBusinessPassStatus() != null) {
				businessPassStatus = entity.getBusinessPassStatus().trim();
			}

			// 編集可否
			if (!isGoverment) {
				form.setEditable(false);
				// 事前協議の場合、
				if (APPLICATION_STEP_ID_2.equals(applicationStepId)) {

					// 通知フラグ
					boolean notifiedFlag = false;
					if (entity.getNotifiedFlag() == null) {
						// 事業者向けの場合、回答履歴がない場合（自動回答の条項）、回答から、通知フラグを再取得
						List<Answer> answerList = answerRepository.findByAnswerId(entity.getAnswerId());
						if (answerList.size() == 0) {
							notifiedFlag = false;
						} else {
							notifiedFlag = answerList.get(0).getNotifiedFlag() == null ? false
									: answerList.get(0).getNotifiedFlag();
						}
					} else {
						notifiedFlag = entity.getNotifiedFlag();
					}
					
					// 事業者へ通知済み後、事業者回答未登録の場合、
					if ((businessPassStatus == null || EMPTY.equals(businessPassStatus)) && notifiedFlag) {
						// 回答内容を上書き更新して、事業者へ再通知しない場合、事業者合意内容が入力不可になる
						if (entity.getAnswerUpdateFlag() != null && entity.getAnswerUpdateFlag()) {
							// 編集不可とする
							form.setEditable(false);
						} else {
							form.setEditable(true);
						}
					}
				}
				form.setNotificable(false);
				form.setAnswerContentEditable(false);
				form.setPermissionNotificable(false);
			} else {

				// 判定項目の編集可能フラグ
				form.setAnswerContentEditable(judgementResult.getAnswerEditableFlag());

				// 部署ID
				String departmentId = governmentUserInfo.getDepartmentId();
				// 回答権限
				String answerAuthority = governmentUserInfo.getAnswerAuthorityFlag();
				// 通知権限
				String notifyAuthority = governmentUserInfo.getNotificationAuthorityFlag();
				// 管理者フラグ
				boolean adminFlag = governmentUserInfo.getAdminFlag();
				// 統括部署フラグ
				boolean managementDepartmentFlag = governmentUserInfo.getManagementDepartmentFlag();

				// 編集可否
				boolean editable = false;
				// 行政で追加の回答は自身の部署のみが回答可能
				if (ANSWER_DATA_TYPE_GOVERNMENT_ADD.equals(entity.getAnswerDataType())) {
					if ((departmentId.equals(entity.getDepartmentId()) && AUTH_TYPE_SELF.equals(answerAuthority))
							|| AUTH_TYPE_ALL.equals(answerAuthority)) {
						editable = true;
						form.setAnswerContentEditable(true);
					}
				} else {

					// 権限ないの場合、編集不可
					if (AUTH_TYPE_NONE.equals(answerAuthority)) {
						editable = false;
					}
					// 自身部署のみ操作可の権限である場合、回答の担当部署リストにログインユーザの部署を含む場合、編集可
					if (AUTH_TYPE_SELF.equals(answerAuthority)
							&& isContainDepartmentId(judgeForm.getDepartments(), departmentId)) {
						editable = true;
					}
					// 他の部署も操作可の権限であれば、担当部署ではなくても、編集可
					if (AUTH_TYPE_ALL.equals(answerAuthority)) {
						editable = true;
					}

					// 行政で削除したものは編集不可
					if (ANSWER_DATA_TYPE_GOVERNMENT_DELETE.equals(entity.getAnswerDataType())) {
						editable = false;
					}
				}

				// 事前協議の場合、事業者へ通知済みになると、編集不可とする
				if (APPLICATION_STEP_ID_2.equals(applicationStepId)) {
					// 回答内容が事業者へ通知済みを前提とする
					if (entity.getNotifiedFlag() != null && entity.getNotifiedFlag()) {

						// 行政確定内容も事業者へ通知済み場合、編集不可とする
						if (entity.getGovernmentConfirmNotifiedFlag() != null
								&& entity.getGovernmentConfirmNotifiedFlag()) {
							editable = false;
						}
					}
				}

				// 許可判定の場合、回答担当者が「M_区分判定_権限」の部署ではなく、許可判定回答権限課（M_権限.回答権限フラグ＝1、2）だけが回答可能
				if (APPLICATION_STEP_ID_3.equals(applicationStepId)) {
					if (AUTH_TYPE_NONE.equals(answerAuthority)) {
						editable = false;
					} else {
						editable = true;
					}
				}

				Integer applicationId = entity.getApplicationId();
				List<Application> application = applicationRepository.getApplicationList(applicationId);
				String status = application.get(0).getStatus();

				// 申請ステータスより、回答できる申請段階を判断
				if (APPLICATION_STEP_ID_1.equals(applicationStepId)) {

					if (!STATUS_CONSULTATION_NOTANSWERED.equals(status)
							&& !STATUS_CONSULTATION_ANSWERED_PREPARING.equals(status)
							&& !STATUS_CONSULTATION_ANSWERED_REVIEWING.equals(status)
							&& !STATUS_CONSULTATION_REAPP.equals(status)
					) {

						// 処理中の申請段階が事前相談のステータス以外と完了の場合、編集不可とする
						editable = false;
					}

				}
				if (APPLICATION_STEP_ID_2.equals(applicationStepId)) {

					if (!STATUS_DISCUSSIONS_NOTANSWERED.equals(status)
							&& !STATUS_DISCUSSIONS_ANSWERED_PREPARING.equals(status)
							&& !STATUS_DISCUSSIONS_ANSWERED_REVIEWING.equals(status)
							&& !STATUS_DISCUSSIONS_IN_PROGRESS.equals(status)
							&& !STATUS_DISCUSSIONS_REAPP.equals(status)
					) {

						// 処理中の申請段階が事前協議のステータス以外と完了の場合、編集不可とする
						editable = false;
					}
				}
				if (APPLICATION_STEP_ID_3.equals(applicationStepId)) {

					if (!STATUS_PERMISSION_NOTANSWERED.equals(status)
							&& !STATUS_PERMISSION_ANSWERED_PREPARING.equals(status)
							&& !STATUS_PERMISSION_ANSWERED_REVIEWING.equals(status)
							&& !STATUS_PERMISSION_REAPP.equals(status)
					) {

						// 処理中の申請段階が許可判定のステータス以外と完了の場合、編集不可とする
						editable = false;
					}
				}

				form.setEditable(editable);

				// 通知可否(事業者へ通知)
				boolean notificable = false;
				// 通知可否(許可通知)
				boolean permissionNotificable = false;

				// 事前相談
				if (APPLICATION_STEP_ID_1.equals(applicationStepId)) {
					// 権限ないの場合、通知不可
					if (AUTH_TYPE_NONE.equals(notifyAuthority)) {
						notificable = false;
					}

					// 自身部署のみ操作可の権限である場合、回答の担当部署がログインユーザの部署の場合、通知可
					if (AUTH_TYPE_SELF.equals(notifyAuthority)
							&& isContainDepartmentId(judgeForm.getDepartments(), departmentId)) {
						notificable = true;
					}

					// 他の部署も操作可の権限であれば、担当部署ではなくても、通知可
					if (AUTH_TYPE_ALL.equals(notifyAuthority)) {
						notificable = true;
					}
				}

				// 事前協議
				if (APPLICATION_STEP_ID_2.equals(applicationStepId)) {

					// 権限ないの場合、通知不可
					if (AUTH_TYPE_NONE.equals(notifyAuthority)) {
						notificable = false;
						permissionNotificable = false;
					} else {
						// 通知権限ありの管理者が統括部署管理者に回答許可判定通知可能
						if (adminFlag) {
							// 自身部署のみ操作可の権限である場合、自分の部署が担当している条項だけ
							if (AUTH_TYPE_SELF.equals(notifyAuthority)
									&& departmentId.equals(entity.getDepartmentId())) {
								permissionNotificable = true;
							}

							// 他の部署も操作可の権限であれば、担当部署ではなくても、通知可
							if (AUTH_TYPE_ALL.equals(notifyAuthority)) {
								permissionNotificable = true;
							}
						}

						// ■仕様説明（事前協議のみ）
						// ■ 2回目以降の回答通知（回答内容）が担当課管理者だけが実行できる。
						// ■ １回目の回答通知（回答内容）と回答通知（行政確定登録）は統括部署管理者だけが実行できる

						// 2回目の回答通知であるか判断(事業者へ通知済み、かつ、事業者合意未登録の回答があるかどうか)
						if (notificableByAdmin) {
							// 2回目の回答通知の場合、担当課の管理者だけが通知可能
							if (adminFlag && departmentId.equals(entity.getDepartmentId())) {
								notificable = true;
							}
						} else {
							// 事業者へ1回目の回答通知と行政確定登録の回答通知を行う場合、統括部署管理者だけが通知可能
							if (adminFlag && managementDepartmentFlag) {
								notificable = true;
							}
						}
					}
				}

				// 許可判定
				if (APPLICATION_STEP_ID_3.equals(applicationStepId)) {
					// 通知担当者が許可判定回答権限課（M_権限.回答権限フラグ＝1、2）の管理者だけ
					if (!AUTH_TYPE_NONE.equals(answerAuthority) && adminFlag) {
						notificable = true;
					}
				}

				form.setNotificable(notificable);
				form.setPermissionNotificable(permissionNotificable);
			}

			// 再申請フラグ
			if (!isGoverment) {
				form.setReApplicationFlag(entity.getBusinessReApplicationFlag());
			} else {
				form.setReApplicationFlag(entity.getReApplicationFlag());
			}

			// 事前相談の場合、回答ごとの回答ファイル、問合せ情報を取得する
			if (APPLICATION_STEP_ID_1.equals(applicationStepId)) {

				// 回答ファイル一覧
				LOGGER.trace("回答ファイル一覧取得 開始");
				List<AnswerFile> answerFileList;
				if (isGoverment) {
					// 行政
					answerFileList = answerFileRepository.findByAnswerIdWithoutDeleted(entity.getAnswerId());
				} else {
					// 事業者
					answerFileList = answerFileRepository.findByAnswerIdWithoutDeletedForBusiness(entity.getAnswerId());
				}
				List<AnswerFileForm> answerFormList = new ArrayList<AnswerFileForm>();
				for (AnswerFile answer : answerFileList) {
					AnswerFileForm answerFileForm = getAnswerFileFormFromEntity(answer);
					answerFileForm.setJudgementInformation(judgeForm);
					answerFormList.add(answerFileForm);
				}
				form.setAnswerFiles(answerFormList);
				LOGGER.trace("回答ファイル一覧取得 終了");

				// チャット情報
				LOGGER.trace("チャット情報取得 開始");
				// O_チャットを取得する
				List<Chat> chatList = chatRepository.findByAnswerId(entity.getAnswerId());
				ChatForm chatForm = new ChatForm();
				if (chatList.size() > 0) {
					Chat chat = chatList.get(0);
					chatForm.setAnswerId(chat.getAnswerId());
					chatForm.setApplicationId(chat.getApplicationId());
					ApplicationStepForm applicationStep = new ApplicationStepForm();
					applicationStep.setApplicationStepId(chat.getApplicationStepId());
					chatForm.setApplicationStep(applicationStep);
					// chatForm.setApplicationStepId(chat.getApplicationStepId());
					chatForm.setTitle(judgeForm.getTitle());
					chatForm.setChatId(chat.getChatId());
					// チャットIDに紐づくメッセージを取得する
					List<Message> messageList = new ArrayList<Message>();
					if (isGoverment) {
						// 行政
						messageList = mssageRepository.findByChatId(chat.getChatId());
					} else {
						// 事業者
						messageList = mssageRepository.findByChatIdForBusiness(chat.getChatId());
					}
					List<MessageForm> messageFormList = new ArrayList<MessageForm>();
					for (Message message : messageList) {
						MessageForm messageForm = getMessageFormFromEntity(message);
						messageFormList.add(messageForm);
					}
					chatForm.setMessages(messageFormList);
				}

				form.setChat(chatForm);
				LOGGER.trace("チャット情報取得 終了");
			}

			// 回答テンプレート(事前相談の場合)
			if (APPLICATION_STEP_ID_1.equals(applicationStepId)) {

				List<AnswerTemplate> answerTemplate = answerTemplateRepository
						.findAnswerTemplateByJudgementItemId(entity.getJudgementId());
				final List<AnswerTemplateForm> answerTemplateForm = new ArrayList<AnswerTemplateForm>();
				for (AnswerTemplate aTemplate : answerTemplate) {
					final AnswerTemplateForm aTemplateForm = new AnswerTemplateForm();
					aTemplateForm.setAnswerTemplateId(aTemplate.getAnswerTemplateId());
					aTemplateForm.setDispOrder(aTemplate.getDispOrder());
					aTemplateForm.setAnswerTemplateText(aTemplate.getAnswerTemplateText());
					aTemplateForm.setJudgementItemId(aTemplate.getJudgementItemId());
					answerTemplateForm.add(aTemplateForm);
				}
				form.setAnswerTemplate(answerTemplateForm);
			}

			// 事前協議フラグ
			form.setDiscussionFlag(entity.getDiscussionFlag());

			// 申請段階
			LOGGER.trace("申請段階情報取得 開始");
			List<ApplicationStep> applicationStepList = applicationStepRepository
					.findByApplicationStepId(applicationStepId);
			if (applicationStepList.size() > 0) {
				form.setApplicationStep(geApplicationStepFormFromEntity(applicationStepList.get(0)));
			}
			LOGGER.trace("申請段階情報取得 終了");

			// 協議対象
			form.setDiscussionItem(entity.getDiscussionItem());
			// 協議対象マップ 一覧
			if (APPLICATION_STEP_ID_2.equals(applicationStepId)) {
				LOGGER.trace("M_帳票一覧取得 開始");
				List<LedgerMaster> ledgerMasterList = ledgerMasterRepository
						.getLedgerMasterListForDisplay(applicationStepId);
				List<LedgerMasterForm> ledgerMasterFormList = new ArrayList<LedgerMasterForm>();
				for (LedgerMaster ledgerMaster : ledgerMasterList) {
					ledgerMasterFormList.add(geledgerMasterFormFromEntity(ledgerMaster, entity.getDiscussionItem()));
				}
				form.setDiscussionItems(ledgerMasterFormList);
				LOGGER.trace("M_帳票一覧取得 終了");
			}

			// 事業者合否ステータス
			form.setBusinessPassStatus(businessPassStatus);
			// 事業者回答登録日時
			if (entity.getBusinessAnswerDatetime() != null) {
				DateTimeFormatter datetimeformatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
				String timeText = datetimeformatter.format(entity.getBusinessAnswerDatetime());
				form.setBusinessAnswerDatetime(timeText);
			}

			// 行政確定ステータス
			String governmentConfirmStatus = null;
			if (entity.getGovernmentConfirmStatus() != null) {
				governmentConfirmStatus = entity.getGovernmentConfirmStatus().trim();
			}
			form.setGovernmentConfirmStatus(governmentConfirmStatus);
			// 行政確定日時
			if (entity.getGovernmentConfirmDatetime() != null) {
				DateTimeFormatter datetimeformatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
				String timeText = datetimeformatter.format(entity.getGovernmentConfirmDatetime());
				form.setGovernmentConfirmDatetime(timeText);
			}
			// 行政確定コメント
			form.setGovernmentConfirmComment(entity.getGovernmentConfirmComment());
			// 行政確定通知フラグ
			if (entity.getGovernmentConfirmNotifiedFlag() == null) {
				form.setGovernmentConfirmNotifiedFlag(false);
			} else {
				form.setGovernmentConfirmNotifiedFlag(entity.getGovernmentConfirmNotifiedFlag());
			}
			// 行政確定再申請後、行政確定内容はクリアしないため、
			// 事業者確定内容が未入力する場合、事業者側で、行政確定内容が閲覧できないために、リセットする
			// BusinessPassStatusがDBにchartで保存して、再申請の場合、クリアされて、””になります。
			if (!isGoverment && (businessPassStatus == null || EMPTY.equals(businessPassStatus))) {
				// 行政確定ステータス
				form.setGovernmentConfirmStatus(EMPTY);
				// 行政確定日時
				form.setGovernmentConfirmDatetime(EMPTY);
				// 行政確定コメント
				form.setGovernmentConfirmComment(EMPTY);
			}
			// 許可判定結果
			form.setPermissionJudgementResult(entity.getPermissionJudgementResult());
			// ステータス
			form.setAnswerStatus(entity.getAnswerStatus());
			// データ種類
			form.setAnswerDataType(entity.getAnswerDataType());
			// 削除未通知フラグ
			form.setDeleteUnnotifiedFlag(entity.getDeleteUnnotifiedFlag());
			// 回答期限日時
			if (entity.getDeadlineDatetime() != null) {
				int month = entity.getDeadlineDatetime().getMonthValue();
				int day = entity.getDeadlineDatetime().getDayOfMonth();
				String dateText = String.valueOf(month) + "/" + String.valueOf(day);
				form.setDeadlineDatetime(dateText);
			}
			// チェック有無
			form.setChecked(false);

			// 回答履歴一覧
			final List<AnswerHistoryForm> answerHistoryForm = answerService.getAnswerHistory(entity.getApplicationId(),
					applicationTypeId, applicationStepId, isGoverment, entity.getAnswerId(), 0);
			form.setAnswerHistorys(answerHistoryForm);

			// 回答通知許可フラグ
			if (entity.getAnswerPermissionFlag() == null) {
				form.setAnswerPermissionFlag(false);
			} else {
				form.setAnswerPermissionFlag(entity.getAnswerPermissionFlag());
			}
			// 行政確定通知許可フラグ
			if (entity.getGovernmentConfirmPermissionFlag() == null) {
				form.setGovernmentConfirmPermissionFlag(false);
			} else {
				form.setGovernmentConfirmPermissionFlag(entity.getGovernmentConfirmPermissionFlag());
			}
			// 許可判定移行フラグ
			if (entity.getPermissionJudgementMigrationFlag() == null) {
				form.setPermissionJudgementMigrationFlag(false);
			} else {
				form.setPermissionJudgementMigrationFlag(entity.getPermissionJudgementMigrationFlag());
			}

			return form;
		} finally {
			LOGGER.debug("回答情報フォーム生成 終了");
		}
	}

	/**
	 * O_回答ファイルEntityをO_回答ファイルフォームに詰めなおす
	 * 
	 * @param entity O_回答ファイルEntity
	 * @return O_回答ファイルフォーム
	 */
	private AnswerFileForm getAnswerFileFormFromEntity(AnswerFile entity) {
		AnswerFileForm form = new AnswerFileForm();
		form.setAnswerId(entity.getAnswerId());
		form.setAnswerFileId(entity.getAnswerFileId());
		form.setAnswerFileName(entity.getAnswerFileName());
		form.setAnswerFilePath(entity.getFilePath());
		form.setNotifiedFilePath(entity.getNotifiedFilePath());
		form.setDeleteUnnotifiedFlag(entity.getDeleteUnnotifiedFlag());
		form.setApplicationId(entity.getApplicationId());
		form.setApplicationStepId(entity.getApplicationStepId());
		form.setDepartmentId(entity.getDepartmentId());
		return form;
	}

	/**
	 * 申請者情報を構築
	 * 
	 * @param applicantInformationList 申請者情報項目リスト
	 * @param isContact                連絡先の場合→true それ以外→false
	 * @return 申請者情報
	 */
	private ApplicantInformation buildApplicantInformation(List<ApplicantInformationItemForm> applicantInformationList,
			boolean isContact) {
		ApplicantInformation infomation = new ApplicantInformation();

		Set<String> workSet = new HashSet<String>();
		String mailAddress = null;
		for (ApplicantInformationItemForm applicantInformation : applicantInformationList) {
			if (workSet.contains(applicantInformation.getId())) {
				continue;
			}

			String value = applicantInformation.getValue();
			if (isContact && (applicantInformation.getApplicantSameFlag() == null
					|| Boolean.FALSE.equals(applicantInformation.getApplicantSameFlag()))) {
				value = applicantInformation.getContactValue();
			}
			// 必須項目にも関わらずnullの場合は元の値を流用
			if (isContact && Boolean.TRUE.equals(applicantInformation.getRequireFlag())
					&& (value == null || "".equals(value))) {
				value = applicantInformation.getValue();
			}

			switch (applicantInformation.getId()) {
			case ApplicationDao.ITEM_1_ID:
				infomation.setItem1(value);
				break;
			case ApplicationDao.ITEM_2_ID:
				infomation.setItem2(value);
				break;
			case ApplicationDao.ITEM_3_ID:
				infomation.setItem3(value);
				break;
			case ApplicationDao.ITEM_4_ID:
				infomation.setItem4(value);
				break;
			case ApplicationDao.ITEM_5_ID:
				infomation.setItem5(value);
				break;
			case ApplicationDao.ITEM_6_ID:
				infomation.setItem6(value);
				break;
			case ApplicationDao.ITEM_7_ID:
				infomation.setItem7(value);
				break;
			case ApplicationDao.ITEM_8_ID:
				infomation.setItem8(value);
				break;
			case ApplicationDao.ITEM_9_ID:
				infomation.setItem9(value);
				break;
			case ApplicationDao.ITEM_10_ID:
				infomation.setItem10(value);
				break;
			default:
				LOGGER.warn("未対応の申請者情報項目ID: " + applicantInformation.getId());
				break;
			}

			workSet.add(applicantInformation.getId());

			if (applicantInformation.getMailAddress()) {
				mailAddress = value;
			}
		}
		infomation.setMailAddress(mailAddress);
		return infomation;
	}

	/**
	 * 再申請用申請情報を取得する
	 * 
	 * @param applicationId 申請ID
	 * @return 再申請フォーム
	 */
	public ReApplicationForm getReApplicationInfo(ReApplicationRequestForm reApplicationRequestForm) {
		LOGGER.debug("再申請情報取得 開始");
		try {
			ReApplicationForm form = new ReApplicationForm();

			// 申請ID
			Integer applicationId = reApplicationRequestForm.getApplicationId();
			// 申請段階ID
			Integer applicationStepId = reApplicationRequestForm.getApplicationStepId();
			// 前回の申請段階ID
			Integer preApplicationStepId = reApplicationRequestForm.getPreApplicationStepId();

			LOGGER.trace("O_申請検索 開始");
			List<Application> applications = applicationRepository.getApplicationList(applicationId);
			if (applications.size() != 1) {
				LOGGER.error("申請データの件数が不正");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}
			LOGGER.trace("O_申請検索 終了");

			form.setApplicationId(applicationId);
			// 申請種類ID
			form.setApplicationTypeId(applications.get(0).getApplicationTypeId());
			List<ApplicationType> applicationTypeList = applicationTypeRepository
					.findByApplicationTypeId(applications.get(0).getApplicationTypeId());
			if (applicationTypeList.size() > 0) {
				form.setApplicationType(getApplicationTypeFormFromEntity(applicationTypeList.get(0)));
			}
			// 申請段階ID
			form.setApplicationStepId(applicationStepId);

			LOGGER.trace("O_申請版情報検索 開始");
			List<ApplicationVersionInformation> applicationVersionInformationList = applicationVersionInformationRepository
					.findByApplicationId(applicationId);
			if (applicationVersionInformationList.size() == 0) {
				LOGGER.warn("申請IDに対する申請が存在しません。");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}
			LOGGER.trace("O_申請版情報検索 終了");

			// 申請段階が変わらない場合、申請段階に対する版情報
			if (applicationStepId == preApplicationStepId) {
				form.setVersionInformation(applicationVersionInformationList.get(0).getVersionInformation());
				form.setAcceptVersionInformation(
						applicationVersionInformationList.get(0).getAcceptVersionInformation());
			} else {
				form.setVersionInformation(0);
				form.setAcceptVersionInformation(0);
			}
			// 前回の申請段階ID
			form.setPreApplicationStepId(preApplicationStepId);

			ApplicationDao dao = new ApplicationDao(emf);

			// 申請地番一覧
			LOGGER.trace("申請地番一覧取得 開始");
			List<LotNumberForm> lotNumberFormList = new ArrayList<LotNumberForm>();
			List<ApplyLotNumberForm> formList = new ArrayList<ApplyLotNumberForm>();
			List<ApplyLotNumber> lotNumbersList = dao.getApplyingLotNumberList(applicationId, lonlatEpsg);
			for (ApplyLotNumber lotNumbers : lotNumbersList) {
				formList.add(getApplyingLotNumberFormFromEntity(lotNumbers));
			}
			form.setLotNumbers(formList);

			LOGGER.trace("申請地番一覧取得 終了");

			// 申請区分選択一覧
			LOGGER.trace("申請区分選択一覧取得 開始");
			Integer paramApplicationStepId = applicationStepId;
			int paramVersionInformation = applicationVersionInformationList.get(0).getVersionInformation();
			// 次の段階への再申請である場合、前回の申請段階IDに対する申請区分選択一覧を取得
			if (applicationStepId != preApplicationStepId) {
				paramApplicationStepId = preApplicationStepId;
			} else {
				// 事前協議⇒事前協議の場合、
				if (APPLICATION_STEP_ID_2.equals(applicationStepId)) {
					// 受付版情報が0の場合、事前相談の情報を前回とする
					if (applicationVersionInformationList.get(0).getAcceptVersionInformation().equals(0)) {
						paramApplicationStepId = APPLICATION_STEP_ID_1;
						for (ApplicationVersionInformation applyVer : applicationVersionInformationList) {
							if (APPLICATION_STEP_ID_1.equals(applyVer.getApplicationStepId())) {
								paramVersionInformation = applyVer.getVersionInformation();
							}
						}
					} else {
						paramApplicationStepId = APPLICATION_STEP_ID_2;
						paramVersionInformation = applicationVersionInformationList.get(0)
								.getAcceptVersionInformation();
					}
				}
			}

			// 許可判定の場合、事前協議の申請区分を表示するため、事前協議の申請区分を検索する
			if (APPLICATION_STEP_ID_3.equals(applicationStepId)) {
				paramApplicationStepId = APPLICATION_STEP_ID_2;
				for (ApplicationVersionInformation applyVer : applicationVersionInformationList) {
					if (APPLICATION_STEP_ID_2.equals(applyVer.getApplicationStepId())) {
						paramVersionInformation = applyVer.getVersionInformation();
					}
				}

			}
			List<ApplicationCategorySelectionViewForm> viewFormList = new ArrayList<ApplicationCategorySelectionViewForm>();
			List<ApplicationCategorySelectionView> applicationCategorySelectionViewList = dao
					.getApplicationCategorySelectionViewList(applicationId, paramApplicationStepId,
							paramVersionInformation);
			for (ApplicationCategorySelectionView applicationCategorySelectionView : applicationCategorySelectionViewList) {
				ApplicationCategorySelectionViewForm viewForm = getSelectionViewFormFromEntity(
						applicationCategorySelectionView, applicationId, paramApplicationStepId,
						paramVersionInformation);
				viewFormList.add(viewForm);
			}
			form.setApplicationCategories(viewFormList);
			LOGGER.trace("申請区分選択一覧取得 終了");

			// 申請者情報一覧
			LOGGER.trace("申請者情報一覧取得 開始");
			List<ApplicantInformationItemForm> applicantFormList = new ArrayList<ApplicantInformationItemForm>();
			List<ApplicantInformation> applicantList = dao.getApplicantInformationList(applicationId);
			if (applicantList.size() > 0) {
				// 申請者は申請者IDに対して1件
				ApplicantInformation applicant = applicantList.get(0);

				// 連絡先情報
				ApplicantInformation contactApplicant = null;
				if (applicantList.size() > 1) {
					contactApplicant = applicantList.get(1);
				}

				// 申請者情報項目一覧
				List<ApplicantInformationItem> applicantInformationItemList = applicantInformationItemRepository
						.getApplicantItems();
				for (ApplicantInformationItem applicantInformationItem : applicantInformationItemList) {
					String value = null;
					String contactValue = null;

					switch (applicantInformationItem.getApplicantInformationItemId()) {
					case ApplicationDao.ITEM_1_ID:
						value = applicant.getItem1();
						if (contactApplicant != null) {
							contactValue = contactApplicant.getItem1();
						}
						break;
					case ApplicationDao.ITEM_2_ID:
						value = applicant.getItem2();
						if (contactApplicant != null) {
							contactValue = contactApplicant.getItem2();
						}
						break;
					case ApplicationDao.ITEM_3_ID:
						value = applicant.getItem3();
						if (contactApplicant != null) {
							contactValue = contactApplicant.getItem3();
						}
						break;
					case ApplicationDao.ITEM_4_ID:
						value = applicant.getItem4();
						if (contactApplicant != null) {
							contactValue = contactApplicant.getItem4();
						}
						break;
					case ApplicationDao.ITEM_5_ID:
						value = applicant.getItem5();
						if (contactApplicant != null) {
							contactValue = contactApplicant.getItem5();
						}
						break;
					case ApplicationDao.ITEM_6_ID:
						value = applicant.getItem6();
						if (contactApplicant != null) {
							contactValue = contactApplicant.getItem6();
						}
						break;
					case ApplicationDao.ITEM_7_ID:
						value = applicant.getItem7();
						if (contactApplicant != null) {
							contactValue = contactApplicant.getItem7();
						}
						break;
					case ApplicationDao.ITEM_8_ID:
						value = applicant.getItem8();
						if (contactApplicant != null) {
							contactValue = contactApplicant.getItem8();
						}
						break;
					case ApplicationDao.ITEM_9_ID:
						value = applicant.getItem9();
						if (contactApplicant != null) {
							contactValue = contactApplicant.getItem9();
						}
						break;
					case ApplicationDao.ITEM_10_ID:
						value = applicant.getItem10();
						if (contactApplicant != null) {
							contactValue = contactApplicant.getItem10();
						}
						break;
					default:
						LOGGER.warn("未知の項目ID: " + applicantInformationItem.getApplicantInformationItemId());
						break;
					}
					applicantFormList.add(
							getApplicantInformationItemFormFromEntity(applicantInformationItem, value, contactValue));
				}
			}
			form.setApplicantInformations(applicantFormList);
			LOGGER.trace("申請者情報一覧取得 終了");

			// 申請追加情報一覧
			LOGGER.trace("O_申請追加情報一覧取得 開始");
			List<ApplicantInformationItemForm> applicantAddFormList = new ArrayList<ApplicantInformationItemForm>();
			// 申請段階IDに対する申請追加情報一覧を取得
			List<ApplicantInformationItem> applicantAddInformationItemList = applicantInformationItemRepository
					.getApplicantAddItems(String.valueOf(applicationStepId));

			paramApplicationStepId = applicationStepId;
			paramVersionInformation = applicationVersionInformationList.get(0).getVersionInformation();
			// 次の段階への再申請である場合、前回の申請段階IDに対する申請区分選択一覧を取得
			if (applicationStepId != preApplicationStepId) {
				paramApplicationStepId = preApplicationStepId;
			} else {
				// 事前協議⇒事前協議の場合、
				if (APPLICATION_STEP_ID_2.equals(applicationStepId)) {
					// 受付版情報が0の場合、事前相談の情報を前回とする
					if (applicationVersionInformationList.get(0).getAcceptVersionInformation().equals(0)) {
						paramApplicationStepId = APPLICATION_STEP_ID_1;
						for (ApplicationVersionInformation applyVer : applicationVersionInformationList) {
							if (APPLICATION_STEP_ID_1.equals(applyVer.getApplicationStepId())) {
								paramVersionInformation = applyVer.getVersionInformation();
							}
						}
					} else {
						paramApplicationStepId = APPLICATION_STEP_ID_2;
						paramVersionInformation = applicationVersionInformationList.get(0)
								.getAcceptVersionInformation();
					}
				}
			}

			for (ApplicantInformationItem applicantInformationItem : applicantAddInformationItemList) {
				// 前回申請で登録した申請追加情報を取得して、前回がない（申請段階が変わる場合のみが発生可能）場合、空でセット
				List<ApplicantInformationAdd> applicantInformationAddList = applicantInformationAddRepository
						.getApplicantInformationAdd(applicationId, paramApplicationStepId,
								applicantInformationItem.getApplicantInformationItemId(), paramVersionInformation);
				String value = EMPTY;
				if (applicantInformationAddList.size() > 0) {
					value = applicantInformationAddList.get(0).getItemValue();
				}
				applicantAddFormList
						.add(getApplicantInformationItemFormFromEntity(applicantInformationItem, value, EMPTY));
			}

			form.setApplicantAddInformations(applicantAddFormList);
			LOGGER.trace("O_申請追加情報一覧取得 終了");

			return form;
		} finally

		{
			LOGGER.debug("再申請情報取得 終了");
		}
	}

	/**
	 * 再申請用申請ファイル一覧取得
	 * 
	 * @param form 検索条件
	 * @return 再申請用申請ファイル一覧
	 */
	public List<ApplicationFileForm> getReapplicationFiles(ReApplicationRequestForm form) {
		LOGGER.debug("申請ファイル一覧取得 開始");
		try {
			List<ApplicationFileForm> formList = new ArrayList<ApplicationFileForm>();

			List<GeneralConditionDiagnosisResultForm> generalConditionDiagnosisResultFormList = form
					.getGeneralConditionDiagnosisResultFormList();

			List<String> judgementItemIdList = new ArrayList<String>();

			Integer applicationId = form.getApplicationId();
			Integer applicationStepId = form.getApplicationStepId();
			Integer preApplicationStepId = form.getPreApplicationStepId();

			// 前回申請の版番号
			List<ApplicationVersionInformation> applicationVersionInformation = applicationVersionInformationRepository
					.findByApplicationSteId(applicationId, preApplicationStepId);

			Integer paramApplicationStepId = preApplicationStepId;
			Integer paramVersionInformation = applicationVersionInformation.get(0).getVersionInformation();

			// 事前協議 ⇒ 事前協議の場合、
			if (APPLICATION_STEP_ID_2.equals(applicationStepId) && APPLICATION_STEP_ID_2.equals(preApplicationStepId)) {

				// 受付版情報が0の場合、事前相談の最終版に対する申請ファイルを取得
				// 受付版情報が0以外の場合、事前協議の受付版情報に対する申請ファイルを取得
				if (applicationVersionInformation.get(0).getAcceptVersionInformation().equals(0)) {
					paramApplicationStepId = APPLICATION_STEP_ID_1;
					applicationVersionInformation = applicationVersionInformationRepository
							.findByApplicationSteId(applicationId, APPLICATION_STEP_ID_1);
					paramVersionInformation = applicationVersionInformation.get(0).getVersionInformation();

				} else {
					paramApplicationStepId = APPLICATION_STEP_ID_2;
					paramVersionInformation = applicationVersionInformation.get(0).getAcceptVersionInformation();
				}
			}

			// 許可判定⇒許可判定の場合、要再申請の回答に対する申請ファイルのみを検索
			if (APPLICATION_STEP_ID_3.equals(applicationStepId) && APPLICATION_STEP_ID_3.equals(preApplicationStepId)) {

				List<Answer> answerList = answerRepository.findReapplicationByApplicationStepId(applicationId,
						applicationStepId);
				for (Answer answer : answerList) {
					String judgementId = answer.getJudgementId();
					if (!judgementItemIdList.contains(judgementId)) {
						LOGGER.trace("検索対象判定項目ID追加: " + judgementId);
						judgementItemIdList.add(judgementId);
					}
				}

			} else {
				// 上記以外の場合、概要診断結果一覧により、申請ファイルを取得する
				for (GeneralConditionDiagnosisResultForm generalConditionDiagnosisResultForm : generalConditionDiagnosisResultFormList) {
					String judgementId = generalConditionDiagnosisResultForm.getJudgementId();
					if (!judgementItemIdList.contains(judgementId) && generalConditionDiagnosisResultForm.getResult()) {
						LOGGER.trace("検索対象判定項目ID追加: " + judgementId);
						judgementItemIdList.add(judgementId);
					}
				}
			}

			if (judgementItemIdList.size() > 0) {
				LOGGER.trace("申請ファイル一覧取得 開始");

				ApplicationDao dao = new ApplicationDao(emf);
				List<ApplicationFileMaster> applicationFileMasterList = applicationFileMasterRepository
						.getApplicationFiles(judgementItemIdList);
				for (ApplicationFileMaster applicationMasterFile : applicationFileMasterList) {

					// M_申請ファイルから申請ファイルフォームへ転換
					ApplicationFileForm applicationFileForm = getApplicationFileFormFromEntity(applicationMasterFile);
					// 前回でアップロードした申請ファイル（最新版）を画面に表示する

					List<ApplicationFile> applicationFileList = applicationFileRepository
							.getApplicationFilesByVersionInformation(applicationMasterFile.getApplicationFileId(),
									applicationId, paramApplicationStepId, paramVersionInformation);

					List<UploadApplicationFileForm> fileList = new ArrayList<UploadApplicationFileForm>();

					for (ApplicationFile applicationFile : applicationFileList) {
						fileList.add(getUploadApplicationFileFormFromEntity(applicationFile));
					}
					applicationFileForm.setUploadFileFormList(fileList);
					formList.add(applicationFileForm);
				}
				LOGGER.trace("申請ファイル一覧取得 終了");
			}
			return formList;
		} finally {
			LOGGER.debug("申請ファイル一覧取得 終了");
		}
	}

	/**
	 * 申請ファイルIDごとに、申請ファイルの最新版情報取得
	 * 
	 * @param applicationId     申請ID
	 * @param applicationFileId 申請ファイルID
	 * @param applicationStepId 申請段階ID
	 * 
	 * @return 版情報
	 */
	public int getApplicatioFileMaxVersion(int applicationId, String applicationFileId, int applicationStepId) {
		LOGGER.trace("申請ファイルの最新版情報取得 開始");
		int versionInformation = 0;
		ApplicationDao dao = new ApplicationDao(emf);
		List<ApplicationFile> applicationFileList = dao.getApplicatioFile(applicationFileId, applicationId,
				applicationStepId);
		if (applicationFileList.size() > 0) {
			versionInformation = applicationFileList.get(0).getVersionInformation();
		}
		LOGGER.trace("申請ファイルの最新版情報取得 終了");
		return versionInformation;

	}

	/**
	 * 再申請登録パラメータ検査
	 * 
	 * @param reApplicationForm パラメータ
	 * @return 判定結果
	 */
	public boolean validateReApplicationParam(ReApplicationForm reApplicationForm) {
		LOGGER.debug("再申請登録パラメータ検査 開始");
		try {

			if (reApplicationForm.getApplicationId() == null) {
				LOGGER.warn("申請IDが設定されていない: " + reApplicationForm.getApplicationId());
				return false;
			}

			if (reApplicationForm.getApplicationTypeId() == null) {
				LOGGER.warn("申請種類IDが設定されていない: " + reApplicationForm.getApplicationTypeId());
				return false;
			}

			if (reApplicationForm.getApplicationStepId() == null) {
				LOGGER.warn("申請段階IDが設定されていない: " + reApplicationForm.getApplicationStepId());
				return false;
			}

			if (reApplicationForm.getPreApplicationStepId() == null) {
				LOGGER.warn("前回の申請の申請段階IDが設定されていない: " + reApplicationForm.getPreApplicationStepId());
				return false;
			}

			if (reApplicationForm.getOutputReportFlag() == null) {
				LOGGER.warn("概況診断レポート出力要否が設定されていない: " + reApplicationForm.getOutputReportFlag());
				return false;
			}

			if (reApplicationForm.getVersionInformation() == null) {
				LOGGER.warn("版情報が設定されていない: " + reApplicationForm.getVersionInformation());
				return false;
			}

			String id = reApplicationForm.getLoginId();
			String password = reApplicationForm.getPassword();
			if (id == null || "".equals(id) || password == null || "".equals(password)) {
				LOGGER.warn("ログインIDまたはパスワードが設定されていない:");
				return false;
			}
			AnswerConfirmLoginForm answerConfirmLoginForm = new AnswerConfirmLoginForm(id, password, false);
			Integer applicationId = getApplicationIdFromApplicantInfo(answerConfirmLoginForm);

			if (!reApplicationForm.getApplicationId().equals(applicationId)) {
				LOGGER.warn("ログインIDとパスワードに対する申請IDがパラメータの申請IDと一致していない: " + reApplicationForm.getApplicationId());
				return false;
			}
			Integer applicationStepId = reApplicationForm.getApplicationStepId();
			// 事前相談と事前協議の場合、申請区分のチェックを行う
			if (APPLICATION_STEP_ID_1.equals(applicationStepId) || APPLICATION_STEP_ID_2.equals(applicationStepId)) {

				// O_申請区分
				LOGGER.trace("O_申請区分検査 開始");
				List<ApplicationCategorySelectionViewForm> categoryList = reApplicationForm.getApplicationCategories();
				for (ApplicationCategorySelectionViewForm category : categoryList) {
					List<ApplicationCategoryForm> applicationCategoryList = category.getApplicationCategory();
					for (ApplicationCategoryForm applicationCategory : applicationCategoryList) {
						List<ApplicationCategoryMaster> applicationCategoryMasterList = applicationCategoryMasterRepository
								.findByCategoryId(applicationCategory.getId(), applicationCategory.getScreenId());
						if (applicationCategoryMasterList.size() == 0) {
							// 申請区分ID、画面IDに適応した情報がない
							LOGGER.warn("申請区分ID、画面IDに適応した情報がない");
							return false;
						}
					}
				}
				LOGGER.trace("O_申請区分検査 終了");
			}

			// O_申請版情報
			if (reApplicationForm.getApplicationStepId().equals(reApplicationForm.getPreApplicationStepId())) {
				List<ApplicationVersionInformation> list = applicationVersionInformationRepository
						.findOneByVersionInformation(reApplicationForm.getApplicationId(),
								reApplicationForm.getApplicationStepId(), reApplicationForm.getVersionInformation());

				if (list.size() == 0) {
					LOGGER.warn("申請ID、申請段階ID、版情報に適応した情報がない");
					return false;
				}
			} else {
				if (!reApplicationForm.getVersionInformation().equals(0)) {
					LOGGER.warn("版情報が0ではない");
					return false;
				}
			}

			// 区分判定ID
			LOGGER.trace("区分判定検査 開始");
			List<GeneralConditionDiagnosisResultForm> conditionList = reApplicationForm
					.getGeneralConditionDiagnosisResultForm();
			for (GeneralConditionDiagnosisResultForm condition : conditionList) {
				List<CategoryJudgement> categoryJudgementList = categoryJudgementRepository
						.getCategoryJudgementListById(condition.getJudgementId());
				if (categoryJudgementList.size() == 0) {
					if (APPLICATION_STEP_ID_3.equals(applicationStepId)
							&& defaultAddJudgementItemIdList.contains(condition.getJudgementId())) {
						// 一律追加の条項なので、問題ない

					} else {
						// 区分判定IDに適応した区分判定情報が無い
						LOGGER.warn("区分判定IDに適応した区分判定情報が無い");
						return false;
					}
				}
			}
			LOGGER.trace("区分判定検査 終了");

			return true;
		} finally {
			LOGGER.debug("再申請登録パラメータ検査 終了");
		}
	}

	/**
	 * 再申請申請情報を登録
	 * 
	 * @param reApplicationForm
	 * @return
	 */
	public int updateApplication(ReApplicationForm reApplicationForm) {

		// 申請ID
		Integer applicationId = reApplicationForm.getApplicationId();
		// 申請種類ID
		Integer applicationTypeId = reApplicationForm.getApplicationTypeId();
		// 処理中の申請段階ID
		Integer applicationStepId = reApplicationForm.getApplicationStepId();
		// 前回の申請段階ID
		Integer preApplicationStepId = reApplicationForm.getPreApplicationStepId();
		// 版情報
		Integer versionInformation = reApplicationForm.getVersionInformation() + 1;

		// O_申請版情報テーブル
		// 同じ申請段階の再申請または、次の段階の再申請を判断して、再申請処理を行う
		if (applicationStepId.equals(preApplicationStepId)) {

			List<ApplicationVersionInformation> applicationVersionInformation = applicationVersionInformationRepository
					.findByApplicationSteId(applicationId, applicationStepId);

			if (applicationVersionInformation.size() != 1) {
				LOGGER.error("O_申請版情報のデータ件数が不正");
				throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
			}

			LOGGER.trace("O_申請版情報の版情報更新 開始");
			// 申請情報の版情報を更新
			if (applicationVersionInformationJdbc.update(applicationId, applicationStepId, STATE_PROVISIONAL,
					applicationVersionInformation.get(0).getUpdateDatetime()) != 1) {
				LOGGER.error("O_申請版情報の更新件数が不正");
				throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
			}
			LOGGER.trace("O_申請版情報の版情報更新 終了");

		} else {
			LOGGER.trace("O_申請版情報登録 開始");
			// 申請ID
			applicationVersionInformationJdbc.insert(applicationId, applicationStepId, STATE_PROVISIONAL);
			LOGGER.trace("O_申請版情報登録 終了");
		}

		// O_申請区分テーブル
		// 事前相談または、事前協議の場合、申請区分の登録を行う
		if (APPLICATION_STEP_ID_1.equals(applicationStepId) || APPLICATION_STEP_ID_2.equals(applicationStepId)) {

			LOGGER.trace("O_申請区分登録 開始");
			LOGGER.trace("O_申請区分の版情報：" + versionInformation);
			List<ApplicationCategorySelectionViewForm> categoryList = reApplicationForm.getApplicationCategories();
			for (ApplicationCategorySelectionViewForm category : categoryList) {
				List<ApplicationCategoryForm> applicationCategoryList = category.getApplicationCategory();
				for (ApplicationCategoryForm applicationCategory : applicationCategoryList) {
					applicationCategoryJdbc.insert(applicationCategory, applicationId, applicationStepId,
							versionInformation);
				}
			}
			LOGGER.trace("O_申請区分登録 終了");

		}

		// O_申請追加情報テーブル
		LOGGER.trace("O_申請追加情報登録 開始");
		LOGGER.trace("O_申請追加情報の版情報：" + versionInformation);
		List<ApplicantInformationItemForm> itemList = reApplicationForm.getApplicantAddInformations();
		for (ApplicantInformationItemForm item : itemList) {

			String value = EMPTY;
			// 申請追加情報が、プルダウンの場合、申請情報項目選択肢一覧から値を取得
			if (ITEM_TYPE_SINGLE_SELECTION.equals(item.getItemType())
					|| ITEM_TYPE_MULTIPLE_SELECTION.equals(item.getItemType())) {
				if (item.getItemOptions() == null || item.getItemOptions().size() == 0) {
					value = EMPTY;
				} else {
					for (ApplicantInformationItemOptionForm option : item.getItemOptions()) {
						if (option.getChecked()) {
							if (!EMPTY.equals(value)) {
								value += COMMA;
							}
							value += option.getId();
						}
					}
				}
			} else {
				value = item.getValue();
			}

			ApplicantInformationAdd applicantInformationAdd = new ApplicantInformationAdd();
			applicantInformationAdd.setApplicationId(applicationId);
			applicantInformationAdd.setApplicationStepId(applicationStepId);
			applicantInformationAdd.setApplicantInformationItemId(item.getId());
			applicantInformationAdd.setItemValue(value);
			applicantInformationAdd.setVersionInformation(versionInformation);
			applicantAddJdbc.insert(applicantInformationAdd);
		}
		LOGGER.trace("O_申請追加情報登録 終了");

		// O_申請ファイルテーブル
		// 引継の申請の登録（ファイル実体アップロードなし）
		LOGGER.trace("O_申請ファイル登録（ファイル実体アップロードなし） 開始");
		// 画面に表示する条項の更新
		List<UploadApplicationFileForm> uploadFileList = reApplicationForm.getUploadFiles();
		for (UploadApplicationFileForm uploadApplicationFileForm : uploadFileList) {
			applicationFileJdbc.insertWithPath(uploadApplicationFileForm);
		}

		// 次の段階へ進む場合、なくなった条項に対する申請ファイルも引継
		if (!applicationStepId.equals(preApplicationStepId)) {
			// 画面に表示している申請ファイル一覧
			List<ApplicationFileForm> applicationFileFormList = reApplicationForm.getApplicationFileForm();
			ApplicationDao dao = new ApplicationDao(emf);
			List<ApplicationFileMaster> applicationFileMasterList = dao.getApplicationFileMasterList(applicationId,
					preApplicationStepId);
			for (ApplicationFileMaster applicationFileMaster : applicationFileMasterList) {

				String applicationFileId = applicationFileMaster.getApplicationFileId();

				// 概況診断レポートをスキップ
				if (applicationReportFileId.equals(applicationFileId)) {
					continue;
				}
				// 画面に表示するフラグ
				boolean isDisplayed = false;
				for (ApplicationFileForm applicationFileForm : applicationFileFormList) {
					if (applicationFileId.equals(applicationFileForm.getApplicationFileId())) {
						isDisplayed = true;
						break;
					}
				}
				// 画面に表示する場合、なくなった条項ではない、次へ進む
				if (isDisplayed) {
					continue;
				} else {
					// なくなった条項であれば、最新版の申請ファイルを引継
					List<ApplicationFile> applicationFileList = dao.getApplicatioFile(
							applicationFileMaster.getApplicationFileId(), applicationId, preApplicationStepId);

					for (ApplicationFile applicationFile : applicationFileList) {
						UploadApplicationFileForm uploadApplicationFileForm = new UploadApplicationFileForm();
						uploadApplicationFileForm.setApplicationId(applicationId);
						uploadApplicationFileForm.setApplicationStepId(applicationStepId);
						uploadApplicationFileForm.setApplicationFileId(applicationFile.getApplicationFileId());
						uploadApplicationFileForm.setUploadFileName(applicationFile.getUploadFileName());
						uploadApplicationFileForm.setFilePath(applicationFile.getFilePath());
						uploadApplicationFileForm.setExtension(applicationFile.getExtension());
						uploadApplicationFileForm.setVersionInformation(1);

						applicationFileJdbc.insertWithPath(uploadApplicationFileForm);
					}
				}
			}
		}
		LOGGER.trace("O_申請ファイル登録（ファイル実体アップロードなし） 終了");

		// O_回答テーブル
		// 判定結果と回答ID、回答内容を紐づけるマップ
		final Map<Integer, Map<String, String>> generalConditionAnswerMap = new HashMap<Integer, Map<String, String>>();
		// 再申請処理中に、更新・登録を行った回答の回答ID
		List<Integer> answerIdList = new ArrayList<Integer>();

		AnswerDao dao = new AnswerDao(emf);

		// 申請区分より、実行した概要診断結果一覧
		List<GeneralConditionDiagnosisResultForm> conditionList = reApplicationForm
				.getGeneralConditionDiagnosisResultForm();

		// 判定項目IDリスト（回答予定日算出用）
		List<String> judgementItemIdList = new ArrayList<String>();

		// 概況診断結果をループして、O_回答へ登録・更新
		for (GeneralConditionDiagnosisResultForm condition : conditionList) {

			if (condition.getResult()) {

				boolean isInsert = false;
				// 更新用回答エンティティ
				Answer updateAnswer = null;
				// 同じ申請段階で再申請を行う場合、該当条項がDBに登録ずみであるかよ、登録・更新を判断する
				if (applicationStepId.equals(preApplicationStepId)) {

					// O_回答に既に存在する回答リスト（事前協議の場合、削除済みのものも含む）を取得
					boolean includeDeleteItem = false;
					if (APPLICATION_STEP_ID_2.equals(applicationStepId)) {
						includeDeleteItem = true;
					}
					List<Answer> answerList = dao.getAnswerList(applicationId, applicationStepId,
							condition.getJudgementId(), condition.getJudgementResultIndex(), includeDeleteItem);
					if (answerList.size() == 0) {
						isInsert = true;
					} else {
						// 事前協議の場合、判断結果の部署IDに対す回答が存在する7か判断
						if (APPLICATION_STEP_ID_2.equals(applicationStepId)) {
							boolean isExist = false;
							for (Answer ans : answerList) {

								// 事前協議の場合、同じ判定項目には、複数回答担当課がある可能ため、部署IDが一致するか判定する
								if (ans.getDepartmentId().equals(condition.getDepartmentId())) {
									isExist = true;
									updateAnswer = ans;
								}
							}
							isInsert = !isExist;
						} else {
							updateAnswer = answerList.get(0);
						}

					}
				} else {
					isInsert = true;
				}

				// 回答日数取得
				int answerDays = 0;
				if (condition.getAnswerDays() != null) {
					answerDays = condition.getAnswerDays();
				} else {
					LOGGER.trace("M_判定結果一覧取得 開始");
					List<CategoryJudgementResult> categoryJudgementResultList = dao.getJudgementResultList(
							condition.getJudgementId(), applicationTypeId, applicationStepId,
							condition.getDepartmentId());
					LOGGER.trace("M_判定結果一覧取得 終了");
					if (categoryJudgementResultList.size() > 0) {
						answerDays = categoryJudgementResultList.get(0).getAnswerDays();
					}
				}

				if (APPLICATION_STEP_ID_2.equals(applicationStepId)) {
					// 受付回答に追加するか判定
					boolean insertAcceptingAnswer = false;

					// 既存回答が存在しない、（又は、既存回答が却下になる場合、→ 仕様変更のために、削除）受付回答の登録を行う
					if (isInsert || insertAcceptingAnswer) {

						// O_受付回答に登録
						AcceptingAnswer acceptingAnswer = new AcceptingAnswer();
						acceptingAnswer.setApplicationId(applicationId); // 申請ID
						acceptingAnswer.setApplicationStepId(applicationStepId);// 申請段階ID
						acceptingAnswer.setVersionInfomation(versionInformation);// 版情報
						acceptingAnswer.setJudgementId(condition.getJudgementId()); // 判定項目ID

						acceptingAnswer.setDepartmentId(condition.getDepartmentId());// 部署ID
						acceptingAnswer.setJudgementResult(condition.getSummary());// 判定結果
						acceptingAnswer.setJudgementResultIndex(condition.getJudgementResultIndex()); // 判定結果のインデックス
						acceptingAnswer.setAnswerContent(condition.getDefaultAnswer());// 回答内容
						acceptingAnswer.setAnswerDataType(condition.getDataType());// データ種類
						acceptingAnswer.setRegisterStatus(STATE_PROVISIONAL); // 登録ステータス
						if (updateAnswer != null) {
							acceptingAnswer.setAnswerId(updateAnswer.getAnswerId()); // 既存回答ID
						} else {
							acceptingAnswer.setAnswerId(null); // 既存回答ID
						}

						// 回答期限日時(システム日付 + 回答日数 + 統括部署の受付確認日数)
						acceptingAnswer.setDeadlineDatetime(getDeadlineDatetime(answerDays + acceptingConfirmDays));
						Integer answerId = acceptingAnswerJdbc.insert(acceptingAnswer);
						// 概況診断結果レポートを出力するために、
						Map<String, String> answerContentMap = new HashMap<String, String>();
						answerContentMap.put("answerId", answerId.toString());
						answerContentMap.put("answerContent", condition.getDefaultAnswer());
						generalConditionAnswerMap.put(condition.getJudgeResultItemId(), answerContentMap);

						judgementItemIdList.add(condition.getJudgementId());
					} else {
						// 概況診断結果レポートを出力するために、
						Map<String, String> answerContentMap = new HashMap<String, String>();
						answerContentMap.put("answerId", updateAnswer.getAnswerId().toString());
						answerContentMap.put("answerContent", updateAnswer.getAnswerContent());
						generalConditionAnswerMap.put(condition.getJudgeResultItemId(), answerContentMap);
					}

				} else {

					// O_回答へ新規登録
					if (isInsert) {
						Answer answer = new Answer();
						answer.setApplicationId(applicationId); // 申請ID
						answer.setApplicationStepId(applicationStepId);// 申請段階ID
						answer.setJudgementId(condition.getJudgementId()); // 判定項目ID
						answer.setJudgementResultIndex(condition.getJudgementResultIndex()); // 判定結果のインデックス
						answer.setDepartmentAnswerId(0);// 部署回答ID（事前協議以外は「0」）
						answer.setDepartmentId("-1");// 部署ID（事前協議以外は「-1」）
						answer.setJudgementResult(condition.getSummary());// 判定結果
						answer.setAnswerDataType(condition.getDataType());// データ種類
						answer.setRegisterStatus(STATE_PROVISIONAL); // 登録ステータス
						answer.setDiscussionItem(EMPTY);// 協議対象（事前協議のみ）
						answer.setVersionInformation(versionInformation);
						if (condition.getAnswerRequireFlag()) {
							// 回答必須：初期回答なし
							answer.setAnswerContent(null);// 回答内容
							answer.setNotifiedText(null);// 通知テキスト
							answer.setCompleteFlag(false);// 完了フラグ
							answer.setNotifiedFlag(false);// 通知フラグ
							answer.setAnswerUpdateFlag(false);// 回答変更フラグ
							answer.setReApplicationFlag(null); // 再申請フラグ
							answer.setBusinessReApplicationFlag(null); // 事業者再申請フラグ
							answer.setDiscussionFlag(null);// 事前協議フラグ
							answer.setAnswerStatus(ANSWER_STATUS_NOTANSWERED);// 回答ステータス
						} else {
							// 回答任意：初期回答あり
							boolean reapplicationFlag = false;
							for (String reapplicationFalseText : reapplicationFalseTextList) {
								if (condition.getSummary().contains(reapplicationFalseText)) {
									// 再申請不要
									reapplicationFlag = false;
									break;
								}
							}
							for (String reapplicationTrueText : reapplicationTrueTextList) {
								if (condition.getSummary().contains(reapplicationTrueText)) {
									// 要再申請
									reapplicationFlag = true;
									break;
								}
							}
							boolean discussionFlag = false;
							for (String discussionFalseText : discussionFalseTextList) {
								if (condition.getSummary().contains(discussionFalseText)) {
									// 事前協議不要
									discussionFlag = false;
									break;
								}
							}
							for (String discussionTrueText : discussionTrueTextList) {
								if (condition.getSummary().contains(discussionTrueText)) {
									// 要事前協議
									discussionFlag = true;
									break;
								}
							}
							answer.setAnswerContent(condition.getDefaultAnswer());// 回答内容
							answer.setNotifiedText(condition.getDefaultAnswer());// 通知テキスト
							answer.setCompleteFlag(true);// 完了フラグ
							if (APPLICATION_STEP_ID_1.equals(applicationStepId)) {
								answer.setNotifiedFlag(true);// 通知フラグ
								answer.setAnswerUpdateFlag(false);// 回答変更フラグ
							} else {
								answer.setNotifiedFlag(false);// 通知フラグ
								answer.setAnswerUpdateFlag(true);// 回答変更フラグ
							}
							if (!APPLICATION_STEP_ID_2.equals(applicationStepId)) {
								answer.setReApplicationFlag(reapplicationFlag); // 再申請フラグ
								answer.setBusinessReApplicationFlag(reapplicationFlag); // 事業者再申請フラグ
							} else {
								answer.setReApplicationFlag(null); // 再申請フラグ
								answer.setBusinessReApplicationFlag(null); // 事業者再申請フラグ

							}
							if (APPLICATION_STEP_ID_1.equals(applicationStepId)) {
								answer.setDiscussionFlag(discussionFlag);// 事前協議フラグ
							} else {
								answer.setDiscussionFlag(null); // 事前協議フラグ
							}
							answer.setAnswerStatus(ANSWER_STATUS_ANSWERED);// 回答ステータス

						}

						// 回答期限日時
						answer.setDeadlineDatetime(getDeadlineDatetime(answerDays));
						Integer answerId = answerJdbc.insert(answer);

						Map<String, String> answerContentMap = new HashMap<String, String>();
						answerContentMap.put("answerId", answerId.toString());
						answerContentMap.put("answerContent", answer.getAnswerContent());
						generalConditionAnswerMap.put(condition.getJudgeResultItemId(), answerContentMap);

						// リセット処理のために、インサート時のデータも履歴作成を行う。「回答ユーザが「-1」に固定」
						// 回答履歴登録
						if (answerHistoryJdbc.insert(answerId, "-1", answer.getNotifiedFlag()) != 1) {
							LOGGER.warn("回答履歴の更新件数不正");
							throw new RuntimeException("回答履歴の更新に失敗");
						}
						answerIdList.add(answerId);
					} else {

						if (!EMPTY.equals(condition.getDataType())) {
							updateAnswer.setAnswerDataType(condition.getDataType());
						}
						boolean isUpdated = false;
						// 事前相談、許可判定の場合
						if (APPLICATION_STEP_ID_1.equals(applicationStepId)
								|| APPLICATION_STEP_ID_3.equals(applicationStepId)) {

							// 通知済みの再申請要の回答は事業者再申請フラグと完了フラグをリセット
							if (updateAnswer.getBusinessReApplicationFlag() != null
									&& updateAnswer.getBusinessReApplicationFlag()) {
								isUpdated = true;
								if (answerJdbc.resetBuinessReapplicationFlag(updateAnswer, answerDays) != 1) {
									LOGGER.error("O_回答の更新件数が不正");
									throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
								}

							}
						}

						// 未通知の回答には、データ種類が削除済みになる場合、データ種類を更新 画面にグレー表示できるため、データ種類を更新
						if (!isUpdated) {
							// 削除済みになる回答はデータ種類を更新
							if (ANSWER_DATA_TYPE_DELETE.equals(condition.getDataType())) {
								isUpdated = true;
								// 回答
								if (answerJdbc.resetDataType(updateAnswer, ANSWER_DATA_TYPE_DELETE) != 1) {
									LOGGER.error("O_回答の更新件数が不正");
									throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
								}
							}
						}
						// 回答更新された場合、事業者側で見えるため、回答履歴を作成する
						if (isUpdated) {
							// 回答履歴登録
							if (answerHistoryJdbc.insert(updateAnswer.getAnswerId(), "-1", true) != 1) {
								LOGGER.warn("回答履歴の作成件数不正");
								throw new RuntimeException("回答履歴の作成に失敗");
							}

							answerIdList.add(updateAnswer.getAnswerId());
						}
						// 概況診断結果レポートを出力するために、
						Map<String, String> answerContentMap = new HashMap<String, String>();
						answerContentMap.put("answerId", updateAnswer.getAnswerId().toString());
						answerContentMap.put("answerContent", updateAnswer.getAnswerContent());
						generalConditionAnswerMap.put(condition.getJudgeResultItemId(), answerContentMap);
					}
				}

			}

		}

		// 事前相談の再申請
		// 前回の申請で登録済み、再申請時に、表示しない回答は画面にグレー表示できるため、データ種類を「削除済み」に更新
		if (APPLICATION_STEP_ID_1.equals(applicationStepId)) {
			List<Answer> answerList = answerRepository.findByApplicationStepId(applicationId, applicationStepId);
			for (Answer answer : answerList) {
				Integer answerId = answer.getAnswerId();
				if (answerIdList.contains(answerId)) {
					continue;
				} else {
					// 通知済みの再申請不要の条項は概要申請診断結果に表示しないため、回答のデータ種類を「削除済み」に更新
					if (answer.getBusinessReApplicationFlag() != null && !answer.getBusinessReApplicationFlag()) {
						if (answerJdbc.resetDataType(answer, ANSWER_DATA_TYPE_DELETE) != 1) {
							LOGGER.error("O_回答の更新件数が不正");
							throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
						}
					}
				}

			}
		}

		List<Answer> governmentAddAnswerList = answerRepository.getGovernmentAddAnswerList(applicationId,
				preApplicationStepId);
		// 事前協議 ⇒ 許可判定の場合、行政追加の条項は、判定項目IDがないので、概況診断結果になし
		if (APPLICATION_STEP_ID_3.equals(applicationStepId) && APPLICATION_STEP_ID_2.equals(preApplicationStepId)) {
			for (Answer addAnswer : governmentAddAnswerList) {
				// 許可判定移行フラグが移行フラグチェックなし、又は、行政確定ステータスが取下の場合、引継不要
				if (addAnswer.getPermissionJudgementMigrationFlag()
						|| GOVERNMENT_CONFIRM_STATUS_1_WITHDRAW.equals(addAnswer.getGovernmentConfirmStatus())) {
					continue;
				}
				Answer answer = new Answer();
				answer.setApplicationId(applicationId); // 申請ID
				answer.setApplicationStepId(applicationStepId);// 申請段階ID
				answer.setJudgementId(EMPTY); // 判定項目ID
				answer.setJudgementResultIndex(0); // 判定結果のインデックス
				answer.setDepartmentAnswerId(0);// 部署回答ID（事前協議以外は「0」）
				answer.setDepartmentId("-1");// 部署ID（事前協議以外は「-1」）
				answer.setJudgementResult(EMPTY);// 判定結果
				answer.setAnswerDataType(ANSWER_DATA_TYPE_GOVERNMENT_ADD);// データ種類
				answer.setRegisterStatus(STATE_PROVISIONAL); // 登録ステータス
				answer.setDiscussionItem(EMPTY);// 協議対象（事前協議のみ）
				answer.setAnswerContent(null);// 回答内容
				answer.setNotifiedText(null);// 通知テキスト
				answer.setCompleteFlag(false);// 完了フラグ
				answer.setNotifiedFlag(false);// 通知フラグ
				answer.setAnswerUpdateFlag(false);// 回答変更フラグ
				answer.setReApplicationFlag(null); // 再申請フラグ
				answer.setBusinessReApplicationFlag(null); // 事業者再申請フラグ
				answer.setDiscussionFlag(null);// 事前協議フラグ
				answer.setAnswerStatus(ANSWER_STATUS_NOTANSWERED);// 回答ステータス
				answer.setVersionInformation(1);
				// 回答期限日時(行政追加なので、回答期限日時が設定しない)
				answer.setDeadlineDatetime(null);
				Integer answerId = answerJdbc.insert(answer);
			}
		}

		// 事前協議 ⇒ 事前協議の場合、行政追加の条項は、判定項目IDがないので、概況診断結果になし
		if (APPLICATION_STEP_ID_2.equals(applicationStepId) && APPLICATION_STEP_ID_2.equals(preApplicationStepId)) {

			for (Answer addAnswer : governmentAddAnswerList) {

				// 却下、かつ、許可判定移行フラグがfalse 場合、この条項が受付回答に登録
				if (GOVERNMENT_CONFIRM_STATUS_2_REJECT.equals(addAnswer.getGovernmentConfirmStatus())
						&& !addAnswer.getPermissionJudgementMigrationFlag()) {

					// O_受付回答に登録
					AcceptingAnswer acceptingAnswer = new AcceptingAnswer();
					acceptingAnswer.setApplicationId(applicationId); // 申請ID
					acceptingAnswer.setApplicationStepId(applicationStepId);// 申請段階ID
					acceptingAnswer.setVersionInfomation(versionInformation);// 版情報;
					acceptingAnswer.setJudgementId(EMPTY); // 判定項目ID

					acceptingAnswer.setDepartmentId(addAnswer.getDepartmentId());// 部署ID
					acceptingAnswer.setJudgementResult(addAnswer.getJudgementResult());// 判定結果
					acceptingAnswer.setJudgementResultIndex(addAnswer.getJudgementResultIndex()); // 判定結果のインデックス
					acceptingAnswer.setAnswerContent(EMPTY);// 回答内容
					acceptingAnswer.setAnswerDataType(addAnswer.getAnswerDataType());// データ種類
					acceptingAnswer.setRegisterStatus(STATE_PROVISIONAL); // 登録ステータス
					acceptingAnswer.setAnswerId(addAnswer.getAnswerId()); // 既存回答ID
					// 回答期限日時(行政追加なので、回答期限日時が設定しない)
					acceptingAnswer.setDeadlineDatetime(null);

					Integer acceptingAnswerId = acceptingAnswerJdbc.insert(acceptingAnswer);
				}
			}
		}

		// 概要診断レポート出力
		// 概況診断レポート出力要否がTRUE、かつ、画像の一時格納場所がないの場合、画像ないの概況診断レポートを出力
		if (reApplicationForm.getOutputReportFlag()) {
			if (reApplicationForm.getFolderName() == null || reApplicationForm.getFolderName().equals("")) {

				LOGGER.trace("概況診断レポート生成 開始");
				GeneralConditionDiagnosisReportRequestForm reportForm = new GeneralConditionDiagnosisReportRequestForm();
				reportForm.setFolderName(reApplicationForm.getFolderName());
				reportForm.setApplyLotNumbers(reApplicationForm.getLotNumbers());
				reportForm.setApplicationCategories(reApplicationForm.getApplicationCategories());
				reportForm.setGeneralConditionDiagnosisResults(
						reApplicationForm.getGeneralConditionDiagnosisResultForm());
				reportForm.setAnswerJudgementMap(generalConditionAnswerMap);
				Workbook wb = null;
				try {
					wb = exportJudgeReportWorkBook(reportForm);
					if (wb == null) {
						throw new RuntimeException("概況診断レポート生成に失敗");
					}
				} catch (Exception ex) {
					LOGGER.error("概況診断レポート生成でエラー発生", ex);
					throw new RuntimeException(ex);
				}
				LOGGER.trace("概況診断レポート生成 終了");

				// 概況診断レポートをアップロードし、O_申請ファイルに登録する
				LOGGER.trace("概況診断レポートアップロード 開始");
				// ファイル名は「概況診断結果_<申請ID>_yyyy_mm_dd.xlsx」
				SimpleDateFormat sdf = new SimpleDateFormat(applicationReportFileNameFooter);
				String fileName = applicationReportFileNameHeader + applicationId + sdf.format(new Date()) + ".xlsx";
				UploadApplicationFileForm uploadForm = new UploadApplicationFileForm();
				uploadForm.setApplicationId(applicationId);
				uploadForm.setApplicationStepId(applicationStepId);
				uploadForm.setApplicationFileId(applicationReportFileId);
				uploadForm.setUploadFileName(fileName);
				// 版情報
				uploadForm.setVersionInformation(versionInformation);
				// 拡張子
				uploadForm.setExtension("xlsx");
				uploadApplicationFile(uploadForm, wb);
				LOGGER.trace("概況診断レポートアップロード 終了");
			}
		}

		// 判定項目のリスト作成
		if (!APPLICATION_STEP_ID_2.equals(applicationStepId)) {
			for (GeneralConditionDiagnosisResultForm generalConditionDiagnosisResultForm : reApplicationForm
					.getGeneralConditionDiagnosisResultForm()) {
				String judgementId = generalConditionDiagnosisResultForm.getJudgementId();
				if (!judgementItemIdList.contains(judgementId)) {
					judgementItemIdList.add(judgementId);
				}
			}
		}

		int answerExpectDays = getnswerExpectDays(judgementItemIdList, applicationTypeId, applicationStepId);

		return answerExpectDays;
	}

	/**
	 * 再申請完了通知
	 * 
	 * @param applicationRegisterResultForm 申請登録結果フォーム
	 * 
	 */
	public void notifyReapplyComplete(ApplicationRegisterResultForm applicationRegisterResultForm) {
		// 申請ID
		Integer applicationId = applicationRegisterResultForm.getApplicationId();
		// 処理中の申請段階ID
		Integer applicationStepId = applicationRegisterResultForm.getApplicationStepId();
		// 回答予定日数
		Integer answerExpectDays = applicationRegisterResultForm.getAnswerExpectDays();

		// O_申請版情報の登録ステータスをチェック
		List<ApplicationVersionInformation> applicationVersionInformationList = applicationVersionInformationRepository
				.findByApplicationSteId(applicationId, applicationStepId);

		if (applicationVersionInformationList.size() != 1) {
			LOGGER.error("申請データの件数が不正");
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		}

		ApplicationVersionInformation application = applicationVersionInformationList.get(0);
		String registerStatus = application.getRegisterStatus();
		if (STATE_APPLIED.equals(registerStatus)) {
			// 申請済なので不正
			LOGGER.error("対象データは登録ステータスが申請済");
			throw new ResponseStatusException(HttpStatus.CONFLICT);
		}

		// O_申請版情報の登録ステータスを「１：登録済み」に更新
		applicationVersionInformationJdbc.updateRegisterStatus(applicationId, applicationStepId,
				application.getUpdateDatetime());

		// O_回答の登録ステータスを「１：登録済み」に更新
		List<Answer> answerList = answerRepository.getAnswerListWithUnRegisted(applicationId, applicationStepId);
		// 事前協議の場合、 O_受付回答の登録ステータスを「１：登録済み」に更新
		List<AcceptingAnswer> acceptingAnswerList = acceptingAnswerRepository.getAcceptingAnswerListWithUnRegisted(
				applicationId, applicationStepId, application.getVersionInformation());

		if (APPLICATION_STEP_ID_2.equals(applicationStepId)) {

			for (AcceptingAnswer acceptingAnswer : acceptingAnswerList) {
				// 登録ステータス
				if (STATE_PROVISIONAL.equals(acceptingAnswer.getRegisterStatus())) {
					if (acceptingAnswerJdbc.updateRegisterStatus(acceptingAnswer) != 1) {
						LOGGER.error("O_受付回答の更新件数が不正");
						throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
					}
				}
			}

		} else {

			for (Answer answer : answerList) {
				// 登録ステータス
				if (STATE_PROVISIONAL.equals(answer.getRegisterStatus())) {
					if (answerJdbc.updateRegisterStatus(answer) != 1) {
						LOGGER.error("O_回答の更新件数が不正");
						throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
					}
				}
			}
		}

		// O_申請のステータスを更新
		LOGGER.trace("O_申請のステータス判定 開始");

		// 事前相談
		String status = STATUS_CONSULTATION_NOTANSWERED;

		// 事前協議
		if (APPLICATION_STEP_ID_2.equals(applicationStepId)) {
			status = STATUS_DISCUSSIONS_IN_PROGRESS;
		}

		// 許可判定
		if (APPLICATION_STEP_ID_3.equals(applicationStepId)) {
			status = STATUS_PERMISSION_NOTANSWERED;
		}

		// 申請情報のステータスを更新
		if (applicationJdbc.updateApplicationStatus(applicationId, status) != 1) {
			LOGGER.error("O_申請の更新件数が不正");
			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
		LOGGER.trace("O_申請のステータス判定 終了");

		// 行政に再申請受付通知を送付する
		sendReapplicationMailToGovernmentUser(applicationId, applicationStepId, answerExpectDays, answerList);

		// 事業者に再申請受付通知を送付する
		sendReapplicationMailToBusinessUser(applicationRegisterResultForm, answerExpectDays);

	}

	/**
	 * 回答日数算出処理
	 * 
	 * @param applicationRegisterForm パラメータ
	 * @return 回答予定日数
	 */
	public int getnswerExpectDays(List<String> judgementItemIdList, Integer applicationTypeId,
			Integer applicationStepId) {
		LOGGER.debug(" 回答予定日数算出 開始");
		try {
			AnswerDao dao = new AnswerDao(emf);

			// 回答予定日数
			int answerExpectDays = 0;

			for (String judgementItemId : judgementItemIdList) {
				List<CategoryJudgementResult> categoryJudgementResultList = dao.getJudgementResultList(judgementItemId,
						applicationTypeId, applicationStepId, null);

				int answerDays = 0;
				if (categoryJudgementResultList.size() > 0) {
					answerDays = categoryJudgementResultList.get(0).getAnswerDays();
				}

				if (answerDays > answerExpectDays) {
					answerExpectDays = answerDays;
				}
			}

			// 最大の回答日数 + バッファ日数
			answerExpectDays = answerExpectDays + answerBufferDays;

			// 事前協議の場合、統括部署のバックアップを回答予定日日数に加算
			if (APPLICATION_STEP_ID_2.equals(applicationStepId)) {
				answerExpectDays = answerExpectDays + controlDepartmentBufferDays;
			}

			return answerExpectDays;
		} finally {
			LOGGER.debug("回答予定日数算出 終了");
		}
	}

	/**
	 * 行政に再申請受付通知送付
	 * 
	 * @param applicationId     申請ID
	 * @param applicationStepId 申請段階ID
	 * @param answerExpectDays  回答予定日数
	 * @param answerList        回答リスト
	 * 
	 */
	private void sendReapplicationMailToGovernmentUser(Integer applicationId, Integer applicationStepId,
			Integer answerExpectDays, List<Answer> answerList) {
		MailItem baseItem = new MailItem();

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
		Integer versionInformation = getVersionInformation(application.getApplicationId(), applicationStepId);
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

		// 回答予定日数（許可判定のみ）
		Integer step3AnswerDays = 0;
		// 対象・判定結果リスト（許可判定のみ）
		List<MailResultItem> step3ResultList = new ArrayList<MailResultItem>();
		// 処理済み判定項目IDリスト（同一判定項目が複数行結果が生成する場合、重複な対象・判定結果をリストに追加するのを避ける用）
		List<String> judgementIdList = new ArrayList<String>();

		// 要再申請の回答に対する全部署
		List<Department> departmentList = new ArrayList<Department>();

		// 部署ごとの処理済み判定項目IDリスト（同一判定項目が複数行結果が生成する場合、重複な対象・判定結果をリストに追加するのを避ける用）
		Map<String, List<String>> departmentJudgementIdMap = new HashMap<String, List<String>>();

		// 事前相談、許可判定の場合、
		if (APPLICATION_STEP_ID_1.equals(applicationStepId) || APPLICATION_STEP_ID_3.equals(applicationStepId)) {
			for (Answer answer : answerList) {
				Integer answerId = answer.getAnswerId();
				// 事前協議で追加する条項は同意になる場合、許可判定に移行されるため、判定項目IDが空である可能
				MailResultItem item = new MailResultItem();
				Integer answerDays = 0;
				if (answer.getJudgementId() == null || EMPTY.equals(answer.getJudgementId())) {
					item.setTarget(govermentAddAnswerTitle); // 対象
					item.setResult(EMPTY); // 判定結果
				} else {
					List<CategoryJudgementResult> categoryList = answerDao.getJudgementResultByAnswerId(answerId);
					if (categoryList.size() == 0) {
						LOGGER.warn("M_判定結果の値が取得できない 回答ID: " + answerId);
						continue;
					}
					CategoryJudgementResult category = categoryList.get(0);

					if (category.getAnswerDays() != null) {
						answerDays = category.getAnswerDays();
					}

					item.setTarget(category.getTitle()); // 対象
					item.setResult(answer.getJudgementResult()); // 判定結果
				}

				// 事前相談の場合、担当課ごとの対象・判定結果リストを作成
				if (APPLICATION_STEP_ID_1.equals(applicationStepId)) {

					// 回答に紐づく部署
					List<Department> answerDepartmentList = answerDao.getDepartmentList(answerId);
					for (Department answerDepartment : answerDepartmentList) {
						String answerDepartmentId = answerDepartment.getDepartmentId();
						if (!resultMap.containsKey(answerDepartmentId)) {
							// 初期化
							resultMap.put(answerDepartmentId, new ArrayList<MailResultItem>());
						}

						// 部署ごとの処理済み判定項目IDリストに存在しない場合、対象・判定結果を部署ごとに集約
						if (!departmentJudgementIdMap.containsKey(answerDepartmentId)) {
							// 初期化：部署の処理済み判定項目リスト
							departmentJudgementIdMap.put(answerDepartmentId, new ArrayList<String>());
							resultMap.get(answerDepartmentId).add(item);
						} else {
							List<String> departmentJudgementIdList = departmentJudgementIdMap.get(answerDepartmentId);
							if (!departmentJudgementIdList.contains(answer.getJudgementId())) {
								resultMap.get(answerDepartmentId).add(item);
							}
						}

						if (!answerDaysMap.containsKey(answerDepartmentId)) {
							// 初期化
							answerDaysMap.put(answerDepartmentId, answerDays);
						} else {
							if (answerDaysMap.get(answerDepartmentId) < answerDays) {
								answerDaysMap.replace(answerDepartmentId, answerDays);
							}
						}
						if (!departmentList.contains(answerDepartment)) {
							departmentList.add(answerDepartment);
						}
					}
				}
				// 許可判定の場合、担当課は、判定項目の部署に関係ないため、全ての判定項目を基に、対象・判定結果リストを作成
				if (APPLICATION_STEP_ID_3.equals(applicationStepId)) {
					if (versionInformation.compareTo(1) == 0) {
						// 最大回答予定日数
						if (step3AnswerDays.compareTo(answerDays) < 0) {
							step3AnswerDays = answerDays;
						}

						// 対象・判定結果リスト
						if (!judgementIdList.contains(answer.getJudgementId())) {
							step3ResultList.add(item);
							judgementIdList.add(answer.getJudgementId());
						}
					} else {
						// 許可判定の場合、要再申請の申請ファイルだけが再申請できるため、再申請要の対象のみをリストを作成
						if (answer.getReApplicationFlag() != null && answer.getReApplicationFlag()) {
							// 最大回答予定日数
							if (step3AnswerDays.compareTo(answerDays) < 0) {
								step3AnswerDays = answerDays;
							}

							// 対象・判定結果リスト
							if (!judgementIdList.contains(answer.getJudgementId())) {
								step3ResultList.add(item);
								judgementIdList.add(answer.getJudgementId());
							}
						}
					}

				}
			}
		}

		// 行政側の各担当課に再申請受付通知送付
		// 事前相談
		if (APPLICATION_STEP_ID_1.equals(applicationStepId)) {
			for (Department department : departmentList) {
				if (!resultMap.containsKey(department.getDepartmentId())) {
					// 対象・判定結果がないのでスキップ
					continue;
				}
				MailItem governmentItem = baseItem.clone();
				// 部署ごとに対象・判定結果は異なるのでここで設定
				governmentItem.setResultList(resultMap.get(department.getDepartmentId()));
				governmentItem.setAnswerDays(answerDaysMap.get(department.getDepartmentId()).toString());// 回答日数

				// 統括部署管理者の受付確認コメント
				governmentItem.setAcceptContent(EMPTY);
				// 申請ファイル変更案内
				governmentItem.setApplicationFileChangedContent(EMPTY);

				String subject = getMailPropValue(MailMessageUtil.KEY_ACCEPT_SUBJECT, governmentItem);
				String body = getMailPropValue(MailMessageUtil.KEY_ACCEPT_BODY, governmentItem);

				LOGGER.trace(department.getMailAddress());
				LOGGER.trace(subject);
				LOGGER.trace(body);

				try {
					final String[] mailAddressList = department.getMailAddress().split(COMMA);
					for (String aMailAddress : mailAddressList) {
						mailSendutil.sendMail(aMailAddress, subject, body);
					}
				} catch (Exception e) {
					LOGGER.error("メール送信時にエラー発生", e);
					throw new RuntimeException(e);
				}
			}
		}

		// 事前協議の場合、統括部署管理者が申請受付待っているため、担当課に受付通知を送信しない

		// 許可判定
		// 許可判定回答権限課取得
		List<Department> permissionJudgementDepartmentList = answerDao.getPermissionJudgementDepartmentList();

		if (APPLICATION_STEP_ID_3.equals(applicationStepId)) {

			MailItem governmentItem = baseItem.clone();
			// 部署ごとに対象・判定結果は異なるのでここで設定
			governmentItem.setResultList(step3ResultList);
			governmentItem.setAnswerDays(answerExpectDays.toString());// 回答日数

			// 統括部署管理者の受付確認コメント
			governmentItem.setAcceptContent(EMPTY);
			// 申請ファイル変更案内
			governmentItem.setApplicationFileChangedContent(EMPTY);

			String subject = getMailPropValue(MailMessageUtil.KEY_ACCEPT_SUBJECT, governmentItem);
			String body = getMailPropValue(MailMessageUtil.KEY_ACCEPT_BODY, governmentItem);

			for (Department department : permissionJudgementDepartmentList) {

				LOGGER.trace(department.getMailAddress());
				LOGGER.trace(subject);
				LOGGER.trace(body);
				try {
					final String[] mailAddressList = department.getMailAddress().split(COMMA);
					for (String aMailAddress : mailAddressList) {
						mailSendutil.sendMail(aMailAddress, subject, body);
					}
				} catch (Exception e) {
					LOGGER.error("メール送信時にエラー発生", e);
					throw new RuntimeException(e);
				}
			}
		}
		// 行政側の回答通知権限部署へ申請受付通知メールを常に送付する

		// 事前相談⇒回答通知担当課：担当課
		if (APPLICATION_STEP_ID_1.equals(applicationStepId)) {

			List<Department> allDepartment = applicationDao.getDepartmentList(applicationId, applicationStepId);
			for (Department department : allDepartment) {
				// 申請受付通知（行政（回答通知担当課））（※再申請受付通知と申請受付通知を統合）
				// 回答権限ありの担当課に申請受付通知メールを送付する
				Authority authority = getAuthority(department.getDepartmentId(), applicationStepId);
				if (AUTH_TYPE_SELF.equals(authority.getNotificationAuthorityFlag())
						|| AUTH_TYPE_ALL.equals(authority.getNotificationAuthorityFlag())) {

					// 宛先⇒事前協議の場合、回答通知権限を持っている部署に申請受付通知メールを送付する
					String mailAddress = department.getMailAddress();
					MailItem mailItem = baseItem.clone();
					mailItem.setAnswerDays(answerExpectDays.toString());// 回答日数

					String subject = getMailPropValue(MailMessageUtil.KEY_ACCEPT_NOTIFICATION_SUBJECT, mailItem);
					String body = getMailPropValue(MailMessageUtil.KEY_ACCEPT_NOTIFICATION_BODY, mailItem);

					LOGGER.trace(mailAddress);
					LOGGER.trace(subject);
					LOGGER.trace(body);

					try {
						final String[] mailAddressList = mailAddress.split(COMMA);
						for (String aMailAddress : mailAddressList) {
							mailSendutil.sendMail(aMailAddress, subject, body);
						}
					} catch (Exception e) {
						LOGGER.error("メール送信時にエラー発生", e);
						throw new RuntimeException(e);
					}
				}
			}

			// 担当している回答条項がない部署が、他部署操作可能の通知権限が持っている場合、回答通知できるため、申請受付通知を送付する
			List<Authority> authorityList = authorityRepository.getAuthorityListByApplicationStepId(applicationStepId);
			for (Authority departmentAuthority : authorityList) {

				// 他部署に通知できる場合、
				if (AUTH_TYPE_ALL.equals(departmentAuthority.getNotificationAuthorityFlag())) {

					// 通知済みであるか判断
					boolean isNotified = false;
					for (Department department : allDepartment) {
						if (departmentAuthority.getDepartmentId().equals(department.getDepartmentId())) {
							isNotified = true;
							break;
						}
					}

					// 通知済みの場合、スキップ
					if (isNotified) {
						continue;
					}

					List<Department> entityList = departmentRepository
							.getDepartmentListById(departmentAuthority.getDepartmentId());
					if (entityList.size() == 0) {
						LOGGER.warn("M_部署情報が存在しません。部署ID：" + departmentAuthority.getDepartmentId());
						throw new RuntimeException();
					}

					String mailAddress = entityList.get(0).getMailAddress();
					MailItem mailItem = baseItem.clone();
					mailItem.setAnswerDays(answerExpectDays.toString());// 回答日数

					String subject = getMailPropValue(MailMessageUtil.KEY_ACCEPT_NOTIFICATION_SUBJECT, mailItem);
					String body = getMailPropValue(MailMessageUtil.KEY_ACCEPT_NOTIFICATION_BODY, mailItem);

					LOGGER.trace(mailAddress);
					LOGGER.trace(subject);
					LOGGER.trace(body);

					try {
						final String[] mailAddressList = mailAddress.split(COMMA);
						for (String aMailAddress : mailAddressList) {
							mailSendutil.sendMail(aMailAddress, subject, body);
						}
					} catch (Exception e) {
						LOGGER.error("メール送信時にエラー発生", e);
						throw new RuntimeException(e);
					}
				}
			}
		}

		// 事前協議⇒回答通知担当課：統括部署管理者
		if (APPLICATION_STEP_ID_2.equals(applicationStepId)) {

			MailItem mailItem = baseItem.clone();
			mailItem.setAnswerDays(answerExpectDays.toString());// 回答日数
			mailItem.setComment1(
					getMailPropValue(MailMessageUtil.KEY_ACCEPT_NOTIFICATION_NEGOTIATION_ADDITIONAL_COMMENT, mailItem));

			String subject = getMailPropValue(MailMessageUtil.KEY_ACCEPT_NOTIFICATION_SUBJECT, mailItem);
			String body = getMailPropValue(MailMessageUtil.KEY_ACCEPT_NOTIFICATION_BODY, mailItem);

			// 統括部署取得
			List<Department> controlDepartmentList = answerDao.getControlDepartmentList();
			for (Department department : controlDepartmentList) {
				// 宛先:管理者のメールアドレス
				String mailAddress = department.getAdminMailAddress();

				LOGGER.trace(mailAddress);
				LOGGER.trace(subject);
				LOGGER.trace(body);

				try {
					final String[] mailAddressList = mailAddress.split(COMMA);
					for (String aMailAddress : mailAddressList) {
						mailSendutil.sendMail(aMailAddress, subject, body);
					}
				} catch (Exception e) {
					LOGGER.error("メール送信時にエラー発生", e);
					throw new RuntimeException(e);
				}
			}

		}

		// 許可判定⇒回答通知担当課：許可判定回答権限課の管理者
		if (APPLICATION_STEP_ID_3.equals(applicationStepId)) {

			MailItem mailItem = baseItem.clone();
			mailItem.setAnswerDays(answerExpectDays.toString());// 回答日数
			mailItem.setComment1(EMPTY); // コメント

			String subject = getMailPropValue(MailMessageUtil.KEY_ACCEPT_NOTIFICATION_SUBJECT, mailItem);
			String body = getMailPropValue(MailMessageUtil.KEY_ACCEPT_NOTIFICATION_BODY, mailItem);

			for (Department department : permissionJudgementDepartmentList) {
				// 宛先
				String mailAddress = department.getAdminMailAddress();

				LOGGER.trace(mailAddress);
				LOGGER.trace(subject);
				LOGGER.trace(body);

				try {
					final String[] mailAddressList = mailAddress.split(COMMA);
					for (String aMailAddress : mailAddressList) {
						mailSendutil.sendMail(aMailAddress, subject, body);
					}
				} catch (Exception e) {
					LOGGER.error("メール送信時にエラー発生", e);
					throw new RuntimeException(e);
				}
			}
		}
	}

	/**
	 * 事業者に再申請受付通知送付
	 * 
	 * @param applicationId     申請ID
	 * @param applicationStepId 申請段階ID
	 * @param answerExpectDays  回答予定日数
	 */
	private void sendReapplicationMailToBusinessUser(ApplicationRegisterResultForm applicationRegisterResultForm,
			Integer answerExpectDays) {
		MailItem businessItem = new MailItem();

		// O_申請情報の取得
		Application application = getApplication(applicationRegisterResultForm.getApplicationId());
		businessItem.setId(applicationRegisterResultForm.getLoginId()); // ログインID
		businessItem.setPassword(applicationRegisterResultForm.getPassword()); // パスワード
		businessItem.setApplicationId(applicationRegisterResultForm.getApplicationId().toString()); // 申請ID
		businessItem.setApplicationTypeName(getApplicationTypeName(application.getApplicationTypeId())); // 申請種類名
		businessItem
				.setApplicationStepName(getApplicationStepName(applicationRegisterResultForm.getApplicationStepId())); // 申請段階名
		businessItem.setVersionInformation(getVersionInformation(applicationRegisterResultForm.getApplicationId(),
				applicationRegisterResultForm.getApplicationStepId()).toString()); // 版情報

		Calendar now = Calendar.getInstance();
		// 申請月
		businessItem.setApplicationMonth(Integer.valueOf(now.get(Calendar.MONTH) + 1).toString());
		// 申請日
		businessItem.setApplicationDay(Integer.valueOf(now.get(Calendar.DAY_OF_MONTH)).toString());
		// 回答日数
		businessItem.setAnswerDays(answerExpectDays.toString());

		List<ApplicantInformation> applicantList = applicantInformationRepository
				.getApplicantList(applicationRegisterResultForm.getApplicationId(), CONTACT_ADDRESS_INVALID);
		if (applicantList.size() < 1) {
			LOGGER.error("申請者データの件数が不正");
			throw new RuntimeException("申請者データの件数が不正");
		}
		// メールアドレス
		String mailAddress = applicantList.get(0).getMailAddress();

		List<ApplicantInformation> contactApplicantList = applicantInformationRepository
				.getApplicantList(application.getApplicationId(), CONTACT_ADDRESS_VALID);
		if (contactApplicantList.size() > 0) {
			ApplicantInformation contactApplicant = contactApplicantList.get(0);
			mailAddress = contactApplicant.getMailAddress();
		}

		// 申請受付通知（事業者）
		String subject = getMailPropValue(MailMessageUtil.KEY_BUSSINESS_ACCEPT_SUBJECT, businessItem);
		String body = getMailPropValue(MailMessageUtil.KEY_BUSSINESS_ACCEPT_BODY, businessItem);

		// 事業者に再申請受付通知送付
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

			DateTimeFormatter datetimeformatter = DateTimeFormatter.ofPattern("yyyy/MM/dd/ HH:mm");
			String datetimeformated = datetimeformatter.format(message.getSendDatetime());
			form.setSendDatetime(datetimeformated);
			String userId = message.getSenderId();

			LOGGER.trace("メッセージの送信ユーザ情報取得 開始");
			LOGGER.trace("ユーザID: " + userId);
			List<GovernmentUser> governmentUserList = governmentUserRepository.findByUserId(userId);
			GovernmentUserForm governmentUserForm = new GovernmentUserForm();
			if (governmentUserList.size() > 0) {
				GovernmentUser governmentUser = governmentUserList.get(0);
				governmentUserForm.setUserName(governmentUser.getUserName());
				governmentUserForm.setRoleCode(governmentUser.getRoleCode());
				governmentUserForm.setLoginId(governmentUser.getLoginId());
				governmentUserForm.setDepartmentId(governmentUser.getDepartmentId());
				LOGGER.trace("メッセージの送信ユーザの部署情報取得 開始");
				LOGGER.trace("部署ID: " + governmentUser.getDepartmentId());
				List<Department> departmentList = departmentRepository
						.getDepartmentListById(governmentUser.getDepartmentId());
				if (departmentList.size() > 0) {
					governmentUserForm.setDepartmentName(departmentList.get(0).getDepartmentName());
				}
				LOGGER.trace("メッセージの送信ユーザの部署情報取得 終了");
			}
			governmentUserForm.setUserId(userId);
			form.setSender(governmentUserForm);
			LOGGER.trace("メッセージの送信ユーザ情報取得 終了");

			LOGGER.trace("メッセージの宛先部署情報取得 開始");
			List<InquiryAddressForm> inquiryAddressFormList = new ArrayList<InquiryAddressForm>();
			List<InquiryAddress> inquiryAddressList = inquiryAddressRepository.findByMessageId(message.getMessageId());
			for (InquiryAddress inquiryAddress : inquiryAddressList) {
				inquiryAddressFormList.add(getInquiryAddressFormFromEntity(inquiryAddress));
			}
			form.setInquiryAddressForms(inquiryAddressFormList);
			LOGGER.trace("メッセージの宛先部署情報取得 終了");

			return form;
		} finally {
			LOGGER.debug("メッセージフォームを生成 終了");
		}
	}

	/**
	 * チャットIDに紐づく相関情報を取得する
	 * 
	 * @param answerList         回答一覧
	 * @param isGoverment        行政かどうか
	 * @param userId             ユーザーID
	 * @param applicationId      申請ID
	 * @param applicationStepId  申請段階ID
	 * @param departmentAnswerId 部署回答ID
	 * @return
	 */
	public ChatRelatedInfoForm searchChatRelatedInfo(List<Answer> answerList, boolean isGoverment, String userId,
			Integer applicationId, Integer applicationStepId, Integer departmentAnswerId) {
		ChatRelatedInfoForm form = new ChatRelatedInfoForm();

		// 申請情報取得
		List<Application> application = applicationRepository.getApplicationList(applicationId);
		if (application.size() != 1) {
			LOGGER.error("申請データの件数が不正");
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		}
		final Integer applicationTypeId = application.get(0).getApplicationTypeId();

		List<AnswerForm> answerForm = new ArrayList<AnswerForm>();
		if (isGoverment) {
			GovernmentUserDao governmentUserDao = new GovernmentUserDao(emf);
			List<GovernmentUserAndAuthority> governmentUserAndAuthorityList = governmentUserDao
					.getGovernmentUserInfo(userId, applicationStepId);
			if (governmentUserAndAuthorityList.size() < 0) {
				LOGGER.warn("ユーザー情報がない");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}

			for (Answer answer : answerList) {
				answerForm.add(getAnswerFormFromEntity(answer, governmentUserAndAuthorityList.get(0), isGoverment,
						applicationTypeId, false));
			}
		} else {
			for (Answer answer : answerList) {
				answerForm.add(getAnswerFormFromEntity(answer, null, isGoverment, applicationTypeId, false));
			}
		}
		form.setAnswer(answerForm);

		// 回答ファイル一覧
		LOGGER.trace("回答ファイル一覧取得 開始");
		List<AnswerFileForm> answerFileFormList = new ArrayList<AnswerFileForm>();
		if (APPLICATION_STEP_ID_1.equals(applicationStepId)) {
			for (Answer answer : answerList) {
				List<AnswerFile> answerFileList = new ArrayList<AnswerFile>();
				if (APPLICATION_STEP_ID_1.equals(answer.getApplicationStepId())) {
					// 申請段階が1:事前相談の場合
					if (isGoverment) {
						answerFileList = answerFileRepository
								.findByAnswerIdWithoutDeletedForGoverment(answer.getAnswerId());
					} else {
						answerFileList = answerFileRepository
								.findByAnswerIdWithoutDeletedForBusiness(answer.getAnswerId());
					}
				}

				for (AnswerFile answerFile : answerFileList) {

					AnswerFileForm answerFileForm = getAnswerFileFormFromEntity(answerFile);

					// 回答ファイル一覧の対象列に表示する文言取得
					String title = EMPTY;
					String judgementId = EMPTY;

					// 事前相談の場合
					if (answer.getJudgementId() != null && !"".equals(answer.getJudgementId())) {
						List<CategoryJudgementResult> categoryJudgementResult = judgementResultRepository
								.getJudgementResult(answer.getJudgementId(), applicationTypeId, applicationStepId,
										answer.getDepartmentId());
						if (categoryJudgementResult.size() > 0) {
							judgementId = answer.getJudgementId();
							title = categoryJudgementResult.get(0).getTitle();
						}
					}

					AnswerJudgementForm answerJudgementForm = new AnswerJudgementForm();
					answerJudgementForm.setJudgementId(judgementId);
					answerJudgementForm.setTitle(title);
					answerFileForm.setJudgementInformation(answerJudgementForm);

					answerFileFormList.add(answerFileForm);
				}
			}
		} else {
			List<AnswerFile> answerFileList = new ArrayList<AnswerFile>();
			if (APPLICATION_STEP_ID_2.equals(applicationStepId)) {
				if (departmentAnswerId != null) {
					List<DepartmentAnswer> departmentAnswerList = departmentAnswerRepository
							.findByDepartmentAnswerId(departmentAnswerId);
					if (departmentAnswerList != null && departmentAnswerList.size() > 0) {
						// 申請段階が2:事前協議の場合
						if (isGoverment) {
							answerFileList = answerFileRepository.findByAnswerIdWithoutDeletedForGoverment2(
									applicationId, applicationStepId, departmentAnswerList.get(0).getDepartmentId());
						} else {
							answerFileList = answerFileRepository.findByAnswerIdWithoutDeletedForBusiness2(
									applicationId, applicationStepId, departmentAnswerList.get(0).getDepartmentId());
						}
					}
				}
			} else if (APPLICATION_STEP_ID_3.equals(applicationStepId)) {
				// 申請段階が3:許可判定の場合
				if (isGoverment) {
					answerFileList = answerFileRepository.findByAnswerIdWithoutDeletedForGoverment3(applicationId,
							applicationStepId);
				} else {
					answerFileList = answerFileRepository.findByAnswerIdWithoutDeletedForBusiness3(applicationId,
							applicationStepId);
				}
			}
			for (AnswerFile answerFile : answerFileList) {

				AnswerFileForm answerFileForm = getAnswerFileFormFromEntity(answerFile);

				// 回答ファイル一覧の対象列に表示する文言取得
				String title = EMPTY;
				String judgementId = EMPTY;

				// 事前協議の場合、「事前協議回答ファイル（xxx課）」で固定表示
				if (APPLICATION_STEP_ID_2.equals(applicationStepId)) {
					if (!EMPTY.equals(answerFile.getDepartmentId()) && answerFile.getDepartmentId() != null) {

						List<Department> departmentList = departmentRepository
								.getDepartmentListById(answerFile.getDepartmentId());

						if (departmentList.size() > 0) {
							String deparmentName = departmentList.get(0).getDepartmentName();
							title = step2AnswerFileTitle.replace(step2AnswerFileTitleReplaceText, deparmentName);
						}
					}
				}

				// 許可判定の場合、「許可判定回答ファイル」で固定表示
				if (APPLICATION_STEP_ID_3.equals(applicationStepId)) {
					title = step3AnswerFileTitle;
				}

				AnswerJudgementForm answerJudgementForm = new AnswerJudgementForm();
				answerJudgementForm.setJudgementId(judgementId);
				answerJudgementForm.setTitle(title);
				answerFileForm.setJudgementInformation(answerJudgementForm);

				answerFileFormList.add(answerFileForm);
			}
		}
		form.setAnswerFiles(answerFileFormList);
		LOGGER.trace("回答ファイル一覧取得 終了");

		// 申請ファイル一覧
		LOGGER.trace("申請ファイル一覧取得 開始");
		List<ApplicationFileForm> applicationFileMasterFormList = new ArrayList<ApplicationFileForm>();
		ApplicationDao dao = new ApplicationDao(emf);
		List<String> judgementItemIdList = new ArrayList<String>();
		for (Answer answer : answerList) {
			judgementItemIdList.add(answer.getJudgementId());
		}
		List<ApplicationFileMaster> applicationFileMasterList = applicationFileMasterRepository
				.getApplicationFiles(judgementItemIdList);

		for (ApplicationFileMaster applicationFileMaster : applicationFileMasterList) {
			ApplicationFileForm fileForm = getApplicationFileFormFromEntity(applicationFileMaster);

			// アップロードファイル一式
			List<UploadApplicationFileForm> applicationFileFormList = new ArrayList<UploadApplicationFileForm>();
			// 最新な版情報に対する申請ファイルを取得する
			List<ApplicationFile> applicationFileList = dao
					.getApplicatioFile(applicationFileMaster.getApplicationFileId(), applicationId, applicationStepId);

			for (ApplicationFile applicationFile : applicationFileList) {

				UploadApplicationFileForm uploadApplicationFileForm = getUploadApplicationFileFormFromEntity(
						applicationFile);
				applicationFileFormList.add(uploadApplicationFileForm);
			}
			fileForm.setUploadFileFormList(applicationFileFormList);

			// 全て版の申請ファイル一覧
			LOGGER.trace("申請ファイル一覧取得 開始");
			List<ApplicationFile> applicationFileHistorys = applicationFileRepository.getApplicationFilesSortByVer(
					applicationFileMaster.getApplicationFileId(), applicationId, applicationStepId);
			List<UploadApplicationFileForm> applicationFileWithVerList = new ArrayList<UploadApplicationFileForm>();
			for (ApplicationFile file : applicationFileHistorys) {
				applicationFileWithVerList.add(getUploadApplicationFileFormFromEntity(file));
			}
			LOGGER.trace("申請ファイル一覧取得 終了");
			fileForm.setApplicationFileHistorys(applicationFileWithVerList);

			applicationFileMasterFormList.add(fileForm);
		}
		form.setApplicationFiles(applicationFileMasterFormList);
		LOGGER.trace("申請ファイル一覧取得 終了");

		// 地番
		List<ApplyLotNumberForm> formList = new ArrayList<ApplyLotNumberForm>();
		List<ApplyLotNumber> lotNumbersList = dao.getApplyingLotNumberList(applicationId, lonlatEpsg);
		for (ApplyLotNumber lotNumbers : lotNumbersList) {
			formList.add(getApplyingLotNumberFormFromEntity(lotNumbers));
		}
		form.setLotNumbers(formList);

		// 回答履歴一覧
		LOGGER.trace("回答履歴一覧取得 開始");

		List<AnswerHistoryForm> answerHistoryForm = new ArrayList<AnswerHistoryForm>();
		// 事前相談の場合、回答単位の履歴取得
		if (APPLICATION_STEP_ID_1.equals(applicationStepId)) {
			if (answerList != null && answerList.size() > 0) {
				answerHistoryForm = answerService.getAnswerHistory(applicationId, applicationTypeId, applicationStepId,
						isGoverment, answerList.get(0).getAnswerId(), 0);
			}
		}
		// 事前協議の場合、部署回答単位の履歴取得
		if (APPLICATION_STEP_ID_2.equals(applicationStepId)) {
			if (answerList != null && answerList.size() > 0) {
				answerHistoryForm = answerService.getAnswerHistory(applicationId, applicationTypeId, applicationStepId,
						isGoverment, 0, answerList.get(0).getDepartmentAnswerId());
			}

		}
		// 許可判定の場合、申請段階単位の履歴取得
		if (APPLICATION_STEP_ID_3.equals(applicationStepId)) {
			answerHistoryForm = answerService.getAnswerHistory(applicationId, applicationTypeId, applicationStepId,
					isGoverment, 0, 0);

		}

		form.setAnswerHistorys(answerHistoryForm);
		LOGGER.trace("回答履歴一覧取得 終了");

		return form;
	}

	/**
	 * 申請種類一覧を取得
	 * 
	 * @return
	 */
	public List<ApplicationTypeForm> getApplicationTypeList() {
		LOGGER.debug("申請種類一覧取得 開始");
		try {
			List<ApplicationTypeForm> formList = new ArrayList<ApplicationTypeForm>();

			LOGGER.trace("申請種類リスト取得 開始");
			List<ApplicationType> applicationTypeList = applicationTypeRepository.getApplicationTypeList();
			LOGGER.trace("申請種類リスト取得 終了");

			for (ApplicationType applicationType : applicationTypeList) {
				ApplicationTypeForm form = getApplicationTypeFormFromEntity(applicationType);
				formList.add(form);
			}

			return formList;
		} finally {
			LOGGER.debug("申請種類一覧取得 終了");
		}
	}

	/**
	 * 協議対象一覧を取得
	 * 
	 * @return
	 */
	public List<LedgerMasterForm> getLedgerList(int applicationStepId) {
		LOGGER.debug("協議対象一覧取得 開始");
		try {
			List<LedgerMasterForm> formList = new ArrayList<LedgerMasterForm>();

			LOGGER.trace("協議対象リスト取得 開始");
			List<LedgerMaster> ledgerMasterList = ledgerMasterRepository
					.getLedgerMasterListForDisplay(applicationStepId);
			LOGGER.trace("協議対象リスト取得 終了");

			for (LedgerMaster ledgerMaster : ledgerMasterList) {
				formList.add(geledgerMasterFormFromEntity(ledgerMaster, null));
			}

			return formList;
		} finally {
			LOGGER.debug("協議対象一覧取得 終了");
		}
	}

	/**
	 * 申請段階一覧を取得
	 * 
	 * @param applicationId 申請ID
	 * @param isNotify      回答通知用フラグ
	 * @param departmentId  ログインユーザーの部署ID
	 * 
	 * @return
	 */
	public List<ApplicationStepForm> getApplicationStepList(int applicationId, boolean isNotify, String departmentId) {
		LOGGER.debug("申請段階一覧 開始");
		try {
			List<ApplicationStepForm> formList = new ArrayList<ApplicationStepForm>();

			LOGGER.trace("申請段階リスト取得 開始");
			LOGGER.trace("申請ID：" + applicationId);

			List<ApplicationVersionInformation> applicationVersionInformationList = applicationVersionInformationRepository
					.findByApplicationId(applicationId);

			if (applicationVersionInformationList.size() == 0) {
				LOGGER.warn("申請IDに対する申請が存在しません。");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}

			for (ApplicationVersionInformation applicationVersionInformation : applicationVersionInformationList) {
				// 回答通知の場合、部署の通知権限を持っているかチェック
				if (isNotify) {

					List<Authority> authoritList = authorityRepository.getAuthorityList(departmentId,
							applicationVersionInformation.getApplicationStepId());
					String notifyAuthority = AUTH_TYPE_NONE;
					if (authoritList.size() > 0) {
						notifyAuthority = authoritList.get(0).getNotificationAuthorityFlag();
					}
					// 部署は該当する申請段階の通知権限が持っていない、申請段階の追加をスキップ
					if (AUTH_TYPE_NONE.equals(notifyAuthority)) {
						continue;
					}
				} else {
					// 回答登録の場合、事前協議の受付版情報が0であるかチェック
					if (APPLICATION_STEP_ID_2.equals(applicationVersionInformation.getApplicationStepId())
							&& applicationVersionInformation.getAcceptVersionInformation().intValue() == 0) {
						continue;
					}
				}

				List<ApplicationStep> applicationStepList = applicationStepRepository
						.findByApplicationStepId(applicationVersionInformation.getApplicationStepId());

				for (ApplicationStep applicationStep : applicationStepList) {
					formList.add(geApplicationStepFormFromEntity(applicationStep));
				}

			}

			LOGGER.trace("申請段階リスト取得 終了");

			return formList;
		} finally {
			LOGGER.debug("閲覧可能の申請段階一覧 終了");
		}
	}

	/**
	 * 【再申請】仮申請状態のデータを消去する<br>
	 * 申請情報（事前相談・事前協議・許可申請）を申請・再申請する際に、<br>
	 * 申請IDを発行したから、申請完了まで、異常が発生する場合、<br>
	 * 仮申請状態のデータ（申請・申請区分・申請ファイル・申請者情報・回答）を削除する
	 * 
	 * @param applicationId 申請ID
	 * @return 実施結果
	 */
	public boolean resetApplicationInfo(Integer applicationId) {
		LOGGER.debug("申請情報リセット 開始");
		try {
			// 仮申請の申請の存在フラグ
			boolean isExist = false;

			// 登録済みの申請段階ID-版情報のマップト
			Map<Integer, Integer> applicationStepMap = new HashMap<Integer, Integer>();

			// O_申請版情報より、リセットを行うか判断
			List<ApplicationVersionInformation> applicationVersionInformationList = applicationVersionInformationRepository
					.getApplicationWithUnRegisted(applicationId);

			if (applicationVersionInformationList.size() == 0) {
				LOGGER.warn("申請情報取得不能");
				return false;
			}

			ApplicationVersionInformation applicationVersionInformation = null;
			for (ApplicationVersionInformation applicat : applicationVersionInformationList) {
				if (STATE_PROVISIONAL.equals(applicat.getRegisterStatus())) {
					isExist = true;
					applicationVersionInformation = applicat;
				} else {
					applicationStepMap.put(applicat.getApplicationStepId(), applicat.getVersionInformation());
				}
			}

			if (!isExist) {

				// フロント側での実施流れが、申請ファイルアップロード→申請情報登録→完了通知ようなですので、申請版情報がない場合、申請ファイルがあるか判断必要

				// 仮登録された申請ファイルがあるか判定
				// O_申請ファイルのリセット（版情報に対する申請ファイル削除、ファイル自体も削除⇒引継可能なので、ファイル自体削除を行わない）
				List<ApplicationFile> applicationFileList = applicationFileRepository
						.getApplicationFilesByApplicationId(applicationId);
				for (ApplicationFile applicationFile : applicationFileList) {

					// 仮申請が生成するタイミングで、回答レポートが作成しないた、削除処理をスキップ
					if (answerReportFileId.equals(applicationFile.getApplicationFileId())) {
						continue;
					}

					// 申請ファイルの申請段階IDがO_申請版情報に存在するか判定、存在しなければ、仮登録された申請ファイルとして、削除する。
					if (!applicationStepMap.containsKey(applicationFile.getApplicationStepId())) {
						isExist = true;
						// 申請ファイルのレコードと実体を削除
						deleteApplicationFile(applicationFile);
					} else {
						Integer versionInformation = applicationStepMap.get(applicationFile.getApplicationStepId());
						// 申請ファイルの版番号 ＞ 申請版情報の版情報の場合、申請ファイルが削除
						if (versionInformation.compareTo(applicationFile.getVersionInformation()) < 0) {
							isExist = true;
							// 申請ファイルのレコードと実体を削除
							deleteApplicationFile(applicationFile);
						}
					}
				}

				if (!isExist) {

					LOGGER.trace("仮登録された情報がないため、リセット処理が実施不要");
					return true;
				}

			} else {

				Integer applicationStepId = applicationVersionInformation.getApplicationStepId();
				Integer versionInformation = applicationVersionInformation.getVersionInformation();

				// O_申請ファイルのリセット（版情報に対する申請ファイル削除、ファイル自体も削除⇒引継可能なので、ファイル自体削除を行わない）
				List<ApplicationFile> applicationFileList = applicationFileRepository
						.getApplicationFilesByVersionInformation(applicationId, applicationStepId, versionInformation);
				for (ApplicationFile applicationFile : applicationFileList) {

					// 仮申請が生成するタイミングで、回答レポートが作成しないた、削除処理をスキップ
					if (answerReportFileId.equals(applicationFile.getApplicationFileId())) {
						continue;
					}

					// 申請ファイルのレコードと実体を削除
					deleteApplicationFile(applicationFile);
				}

				// O_申請追加情報のリセット
				List<ApplicantInformationAdd> applicantInformationAddList = applicantInformationAddRepository
						.getApplicantInformationAddByVer(applicationId, applicationStepId, versionInformation);
				for (ApplicantInformationAdd applicantInformationAdd : applicantInformationAddList) {

					if (applicantAddJdbc.deleteApplicantAddInfo(applicantInformationAdd.getApplicantId()) != 1) {
						LOGGER.error("申請追加情報データの削除件数が不正");
						throw new RuntimeException("申請追加情報データの削除件数が不正");
					}
				}

				// 許可判定の場合、下記を更新しないため、リセットしない
				if (!APPLICATION_STEP_ID_3.equals(applicationStepId)) {
					// O_申請区分のリセット
					List<ApplicationCategory> applicationCategoryList = applicationCategoryRepository
							.findByVer(applicationId, applicationStepId, versionInformation);
					for (ApplicationCategory applicationCategory : applicationCategoryList) {

						if (applicationCategoryJdbc.deleteApplicationCategory(applicationCategory) != 1) {
							LOGGER.error("申請区分データの削除件数が不正");
							throw new RuntimeException("申請区分データの削除件数が不正");
						}
					}
				}

				// 事前協議の場合、O_受付回答のリセット
				if (APPLICATION_STEP_ID_2.equals(applicationStepId)) {
					// O_受付回答のリセット
					List<AcceptingAnswer> acceptingAnswerList = acceptingAnswerRepository
							.getAcceptingAnswerListWithUnRegisted(applicationId, applicationStepId, versionInformation);
					for (AcceptingAnswer acceptingAnswer : acceptingAnswerList) {
						// 仮申請のレコードが物理削除
						if (STATE_PROVISIONAL.equals(acceptingAnswer.getRegisterStatus())) {
							if (acceptingAnswerJdbc.deleteAcceptingAnswer(acceptingAnswer) != 1) {
								LOGGER.error("受付回答データの削除件数が不正");
								throw new RuntimeException("受付回答データの削除件数が不正");
							}
						}
					}
				} else {
					// O_回答のリセット
					List<Answer> answerList = answerRepository.getAnswerListWithUnRegisted(applicationId,
							applicationStepId);
					for (Answer answer : answerList) {
						// 仮申請のレコードが物理削除
						if (STATE_PROVISIONAL.equals(answer.getRegisterStatus())) {
							if (answerJdbc.deleteAnswer(answer) != 1) {
								LOGGER.error("回答データの削除件数が不正");
								throw new RuntimeException("回答データの削除件数が不正");
							}
						} else {

							AnswerDao dao = new AnswerDao(emf);
							AnswerHistory answerHistory = null;
							boolean completeFlag = false;
							// 行政操作より、作成された最新の回答履歴を取得する
							List<AnswerHistory> answerHistoryList = dao.getAnswerHistoryMax(answer.getAnswerId());

							if (answerHistoryList.size() > 0) {
								answerHistory = answerHistoryList.get(0);
								completeFlag = true;
							} else {
								// 行政操作がない場合、事業者は申請、再申請時に、作成された回答履歴を取得する
								answerHistoryList = answerHistoryRepository
										.getAnswerHistoryByBusiness(answer.getAnswerId());
								if (answerHistoryList.size() < 2) {
									// 回答インサート時に、作成された回答履歴だけなので、再申請で更新されないとする
								} else {
									answerHistory = answerHistoryList.get(1);
									// 自動回答したの場合、回答完了とする
									if (answerHistory.getAnswerText() != null
											|| !EMPTY.equals(answerHistory.getAnswerText())) {
										completeFlag = true;
									}
								}
							}

							// 事前相談、許可判定の場合
							if (APPLICATION_STEP_ID_1.equals(applicationStepId)
									|| APPLICATION_STEP_ID_3.equals(applicationStepId)) {

								if (answerHistory != null) {
									if (answerJdbc.resetReapplicationAnswerContent(answerHistory, completeFlag,
											answer.getUpdateDatetime()) != 1) {
										LOGGER.error("O_回答の更新件数が不正");
										throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
									}
								}
							}
						}
					}
				}

				// O_申請版情報のリセット
				if (versionInformation.intValue() == 1) {
					if (applicationVersionInformationJdbc.delete(applicationId, applicationStepId,
							applicationVersionInformation.getUpdateDatetime()) != 1) {
						LOGGER.error("O_申請版情報の更新件数が不正");
						throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
					}
				} else {
					if (applicationVersionInformationJdbc.resetVersion(applicationId, applicationStepId,
							applicationVersionInformation.getUpdateDatetime()) != 1) {
						LOGGER.error("O_申請版情報の更新件数が不正");
						throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
					}
				}
			}
			return true;
		} finally {
			LOGGER.debug("申請情報リセット 終了");
		}
	}

	/**
	 * O_部署_回答Entityから部署回答フォームを生成
	 * 
	 * @param entity             O_部署_回答
	 * @param governmentUserInfo ユーザー情報
	 * @param isGoverment        行政かどうか
	 * @param applicationTypeId  申請種類
	 * @param answers            回答一覧
	 * @return 部署回答フォーム
	 */
	private DepartmentAnswerForm getDepartmentAnswerFormFromEntity(DepartmentAnswer entity,
			GovernmentUserAndAuthority governmentUserInfo, boolean isGoverment, Integer applicationTypeId,
			List<Answer> answerList) {

		Integer applicationStepId = entity.getApplicationStepId();

		DepartmentAnswerForm form = new DepartmentAnswerForm();
		// 部署回答ID
		form.setDepartmentAnswerId(entity.getDepartmentAnswerId());
		// 申請ID
		form.setApplicationId(entity.getApplicationId());
		// 申請段階ID
		form.setApplicationStepId(applicationStepId);
		// 部署
		List<Department> departmentList = departmentRepository.getDepartmentListById(entity.getDepartmentId());
		if (departmentList.size() > 0) {
			form.setDepartment(getDepartmentFormFromEntity(departmentList.get(0)));
		}

		// 行政確定ステータス
		String governmentConfirmStatus = null;
		if (entity.getGovernmentConfirmStatus() != null) {
			governmentConfirmStatus = entity.getGovernmentConfirmStatus().trim();
		}
		form.setGovernmentConfirmStatus(governmentConfirmStatus);
		// 行政確定日時
		if (entity.getGovernmentConfirmDatetime() != null) {
			DateTimeFormatter datetimeformatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
			String timeText = datetimeformatter.format(entity.getGovernmentConfirmDatetime());
			form.setGovernmentConfirmDatetime(timeText);
		}
		// 行政確定コメント
		form.setGovernmentConfirmComment(entity.getGovernmentConfirmComment());
		// 事業者には、未通知の場合、行政確定内容が閲覧できない
		if (!isGoverment && !entity.getNotifiedFlag()) {
			// 行政確定ステータス
			form.setGovernmentConfirmStatus(EMPTY);
			// 行政確定日時
			form.setGovernmentConfirmDatetime(EMPTY);
			// 行政確定コメント
			form.setGovernmentConfirmComment(EMPTY);
		}
		// 完了フラグ
		form.setCompleteFlag(entity.getCompleteFlag());
		// 通知フラグ
		form.setNotifiedFlag(entity.getNotifiedFlag());
		// 更新日時
		form.setUpdateDatetime(entity.getUpdateDatetime());
		// 削除未通知フラグ
		form.setDeleteUnnotifiedFlag(entity.getDeleteUnnotifiedFlag());
		if (!isGoverment) {
			// 回答通知選択可否
			form.setNotificable(false);
			// 編集可否
			form.setEditable(false);
		} else {

			// 部署ID
			String departmentId = governmentUserInfo.getDepartmentId();
			// 回答権限
			String answerAuthority = governmentUserInfo.getAnswerAuthorityFlag();
			// 通知権限
			String notifyAuthority = governmentUserInfo.getNotificationAuthorityFlag();
			// 管理者フラグ
			boolean adminFlag = governmentUserInfo.getAdminFlag();
			// 統括部署フラグ
			boolean managementDepartmentFlag = governmentUserInfo.getManagementDepartmentFlag();

			// 編集可否
			boolean editable = false;

			// 権限ないの場合、編集不可
			if (AUTH_TYPE_NONE.equals(answerAuthority)) {
				editable = false;
			}
			// 自身部署のみ操作可の権限である場合、回答の担当部署がログインユーザの部署の場合、編集権限あり
			if (AUTH_TYPE_SELF.equals(answerAuthority) && departmentId.equals(entity.getDepartmentId())) {
				editable = true;
			}
			// 他の部署も操作可の権限であれば、担当部署ではなくても、編集権限あり
			if (AUTH_TYPE_ALL.equals(answerAuthority)) {
				editable = true;
			}

			List<Application> application = applicationRepository.getApplicationList(entity.getApplicationId());
			String status = application.get(0).getStatus();
			if (!STATUS_DISCUSSIONS_NOTANSWERED.equals(status) && !STATUS_DISCUSSIONS_ANSWERED_PREPARING.equals(status)
					&& !STATUS_DISCUSSIONS_ANSWERED_REVIEWING.equals(status)
					&& !STATUS_DISCUSSIONS_IN_PROGRESS.equals(status) && !STATUS_DISCUSSIONS_REAPP.equals(status)
			) {

				// 処理中の申請段階が事前協議のステータス以外と完了の場合、編集不可とする
				editable = false;
			}

			// 管理者以外の場合、編集不可にする
			if (!adminFlag) {
				editable = false;
			}

			form.setEditable(editable);

			// 通知可否(事業者へ通知)
			boolean notificable = false;
			// 通知可否(許可通知)
			boolean permissionNotificable = false;

			// 権限ないの場合、通知不可
			if (AUTH_TYPE_NONE.equals(notifyAuthority)) {
				notificable = false;
				permissionNotificable = false;
			} else {
				// 通知権限ありの管理者が統括部署管理者に回答許可判定通知可能
				if (adminFlag) {
					// 自身部署のみ操作可の権限である場合、自分の部署が担当している条項だけ
					if (AUTH_TYPE_SELF.equals(notifyAuthority) && departmentId.equals(entity.getDepartmentId())) {
						permissionNotificable = true;
					}

					// 他の部署も操作可の権限であれば、担当部署ではなくても、通知可
					if (AUTH_TYPE_ALL.equals(notifyAuthority)) {
						permissionNotificable = true;
					}
				}

				// 部署全体回答中に、2回目の回答通知(通知済みかつ事業者合意未登録)を行う回答がある場合、自担当課の管理者だけが通知可能
				if (notificableByAdmin(answerList)) {
					if (adminFlag && departmentId.equals(entity.getDepartmentId())) {
						notificable = true;
					}
				} else {
					// 統括部署管理者だけが事業者へ通知可能。
					if (adminFlag && managementDepartmentFlag) {
						notificable = true;
					}
				}
			}

			form.setNotificable(notificable);
			form.setPermissionNotificable(permissionNotificable);
		}
		// チェック有無
		form.setChecked(false);

		// 回答一覧
		List<AnswerForm> answerFormList = new ArrayList<AnswerForm>();
		// 部署ごとに、事業者へ通知済み かつ 事業者合意未登録の回答があるかどうか
		boolean notificableByAdmin = notificableByAdmin(answerList);
		for (Answer answer : answerList) {
			answerFormList.add(getAnswerFormFromEntity(answer, governmentUserInfo, isGoverment, applicationTypeId,
					notificableByAdmin));
		}
		form.setAnswers(answerFormList);
		// 回答内容を事業者へ通知済みの回答があるかフラグ
		form.setAnswerContentNotifiedFlag(notificableByAdmin);

		// 回答ファイル一覧
		LOGGER.trace("回答ファイル一覧取得 開始");
		AnswerDao dao = new AnswerDao(emf);
		List<AnswerFile> answerFileList = dao.getAnswerFileList(entity.getApplicationId(), applicationStepId,
				entity.getDepartmentId(), isGoverment);

		List<AnswerFileForm> answerFileFormList = new ArrayList<AnswerFileForm>();
		for (AnswerFile answerFile : answerFileList) {
			AnswerFileForm answerFileForm = getAnswerFileFormFromEntity(answerFile);
			// 事前協議の場合、「事前協議回答ファイル（xxx課）」で固定表示
			String title = EMPTY;
			if (!EMPTY.equals(answerFile.getDepartmentId()) && answerFile.getDepartmentId() != null) {

				List<Department> department = departmentRepository.getDepartmentListById(answerFile.getDepartmentId());

				if (department.size() > 0) {
					String deparmentName = department.get(0).getDepartmentName();
					title = step2AnswerFileTitle.replace(step2AnswerFileTitleReplaceText, deparmentName);
				}
			}

			AnswerJudgementForm answerJudgementForm = new AnswerJudgementForm();
			answerJudgementForm.setJudgementId(EMPTY);
			answerJudgementForm.setTitle(title);
			answerFileForm.setJudgementInformation(answerJudgementForm);
			answerFileFormList.add(answerFileForm);
		}
		form.setAnswerFiles(answerFileFormList);
		LOGGER.trace("回答ファイル一覧取得 終了");

		// チャット情報
		LOGGER.trace("チャット情報取得 開始");
		// O_チャットを取得する
		List<Chat> chatList = chatRepository.findByDepartmentAnswerId(entity.getDepartmentAnswerId());
		ChatForm chatForm = new ChatForm();
		if (chatList.size() > 0) {
			Chat chat = chatList.get(0);
			chatForm.setAnswerId(chat.getAnswerId());
			chatForm.setApplicationId(chat.getApplicationId());
			ApplicationStepForm applicationStep = new ApplicationStepForm();
			applicationStep.setApplicationStepId(chat.getApplicationStepId());
			chatForm.setApplicationStep(applicationStep);
			// chatForm.setApplicationStepId(chat.getApplicationStepId());
			chatForm.setTitle(departmentList.get(0).getDepartmentName());
			chatForm.setDepartment(form.getDepartment());
			chatForm.setChatId(chat.getChatId());
			// チャットIDに紐づくメッセージを取得する
			List<Message> messageList = new ArrayList<Message>();
			if (isGoverment) {
				// 行政
				messageList = mssageRepository.findByChatId(chat.getChatId());
			} else {
				// 事業者
				messageList = mssageRepository.findByChatIdForBusiness(chat.getChatId());
			}
			List<MessageForm> messageFormList = new ArrayList<MessageForm>();
			for (Message message : messageList) {
				MessageForm messageForm = getMessageFormFromEntity(message);
				messageFormList.add(messageForm);
			}
			chatForm.setMessages(messageFormList);
		}
		form.setChat(chatForm);
		LOGGER.trace("チャット情報取得 終了");

		// 回答履歴一覧
		final List<AnswerHistoryForm> answerHistoryForm = answerService.getAnswerHistory(entity.getApplicationId(),
				applicationTypeId, applicationStepId, isGoverment, 0, entity.getDepartmentAnswerId());
		form.setAnswerHistorys(answerHistoryForm);

		// 行政確定通知許可フラグ
		if (entity.getGovernmentConfirmPermissionFlag() == null) {
			form.setGovernmentConfirmPermissionFlag(false);
		} else {
			form.setGovernmentConfirmPermissionFlag(entity.getGovernmentConfirmPermissionFlag());
		}

		return form;
	}

	/**
	 * O_帳票Entityから帳票フォームを生成
	 * 
	 * @param entity O_帳票
	 * @return 帳票フォーム
	 */
	private LedgerForm getLedgerFormFromEntity(boolean isGoverment, Ledger entity) {
		// アップロード可否
		// 事業者の場合: 一律false
		// 行政担当者の場合:
		// M_帳票.更新フラグ=trueの場合 -> 更新可能
		// 上記以外 -> 更新不可
		// 通知可否
		// 事業者の場合: 一律false
		// 行政担当者の場合:
		// M_帳票.通知フラグ=false -> 通知不可
		// M_帳票.通知フラグ=true
		// O_帳票.ファイルパス と O_帳票.通知ファイルパスが同じ -> 通知済みのため通知不可
		// O_帳票.ファイルパス と O_帳票.通知ファイルパスが異なる -> 未通知のため通知可能
		// M_帳票.拡張子をセット
		// M_帳票.案内テキストをセット
		LedgerForm form = new LedgerForm();
		form.setFileId(entity.getFileId());
		form.setApplicationId(entity.getApplicationId());
		form.setApplicationStepId(entity.getApplicationStepId());
		form.setLedgerId(entity.getLedgerId());
		List<LedgerMaster> LedgerMasterList = ledgerMasterRepository.getLedgerMasterByLedgerId(entity.getLedgerId());
		if (LedgerMasterList.size() > 0) {
			form.setLedgerName(LedgerMasterList.get(0).getLedgerName());
		}
		form.setUploadFileName(entity.getFileName());
		form.setFilePath(entity.getFilePath());
		DateTimeFormatter datetimeformatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
		String registerDatetime = datetimeformatter.format(entity.getRegisterDatetime());
		form.setRegisterDatetime(registerDatetime);
		if (entity.getReceiptDatetime() != null) {
			String receiptDatetime = datetimeformatter.format(entity.getReceiptDatetime());
			form.setReceiptDatetime(receiptDatetime);
		}

		// アップロード可否
		if (!isGoverment) {
			// 事業者の場合: 一律false
			form.setUploadable(false);
		} else {
			// 行政担当者の場合:
			// M_帳票.更新フラグ=trueの場合 -> 更新可能
			// 上記以外 -> 更新不可
			form.setUploadable(false);
			if (LedgerMasterList.size() > 0) {
				if (Objects.nonNull(LedgerMasterList.get(0).getUpdateFlag())
						&& LedgerMasterList.get(0).getUpdateFlag()) {
					form.setUploadable(true);
				}
			}
		}

		// 通知可否
		if (!isGoverment) {
			// 事業者の場合: 一律false
			form.setNotifiable(false);
		} else {
			// 行政担当者の場合:
			// M_帳票.通知フラグ=false -> 通知不可
			form.setNotifiable(false);
			if (LedgerMasterList.size() > 0) {
				if (Objects.nonNull(LedgerMasterList.get(0).getNotifyFlag())
						&& LedgerMasterList.get(0).getNotifyFlag()) {
					// M_帳票.通知フラグ=true
					// O_帳票.ファイルパス と O_帳票.通知ファイルパスが同じ -> 通知済みのため通知不可
					// O_帳票.ファイルパス と O_帳票.通知ファイルパスが異なる -> 未通知のため通知可能
					if ((entity.getFilePath() + "").equals(entity.getNotifyFilePath())) {
						form.setNotifiable(false);
					} else {
						form.setNotifiable(true);
					}
				}
			}
			// M_帳票.拡張子をセット
			// M_帳票.案内テキストをセット
			if (LedgerMasterList.size() > 0) {
				form.setExtension(LedgerMasterList.get(0).getUploadExtension());
				form.setInformationText(LedgerMasterList.get(0).getInformationText());
			}
		}

		// 通知済みフラグ
		form.setNotifyFlag(entity.getNotifyFlag());
		// 通知済みファイルパス
		form.setNotifyFilePath(entity.getNotifyFilePath());

		if (LedgerMasterList.size() > 0) {
			LOGGER.debug("M_帳票.帳票マスタID:" + LedgerMasterList.get(0).getLedgerId());
			LOGGER.debug("M_帳票.帳票名:" + LedgerMasterList.get(0).getLedgerName());
			LOGGER.debug("M_帳票.更新フラグ：" + (LedgerMasterList.get(0).getUpdateFlag() ? "true" : "false"));
			LOGGER.debug("M_帳票.通知フラグ：" + (LedgerMasterList.get(0).getNotifyFlag() ? "true" : "false"));
			LOGGER.debug("O_帳票.ファイルパス：" + entity.getFilePath());
			LOGGER.debug("O_帳票.通知ファイルパス：" + entity.getNotifyFilePath());
			LOGGER.debug("帳票フォーム");
			LOGGER.debug("帳票フォーム.アップデート可否：" + (form.getUploadable() ? "true" : "false"));
			LOGGER.debug("帳票フォーム.通知可否：" + (form.getNotifiable() ? "true" : "false"));
			LOGGER.debug("帳票フォーム.拡張子：" + form.getExtension());
			LOGGER.debug("帳票フォーム.案内テキスト：" + form.getInformationText());
		} else {
			LOGGER.debug("M_帳票.帳票マスタID該当なし");
		}

		return form;
	}

	/**
	 * 回答担当部署リストに該当する部署を含むかどうか
	 * 
	 * @param departmentList 回答担当部署リスト
	 * @param departmentId   部署ID
	 * @return
	 */
	private boolean isContainDepartmentId(List<DepartmentForm> departmentList, String departmentId) {

		boolean isContain = false;

		for (DepartmentForm departmentForm : departmentList) {

			if (departmentForm.getDepartmentId().equals(departmentId)) {
				isContain = true;
			}
		}

		return isContain;
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
		List<Application> applicationList = applicationRepository.getApplicationList(applicationId);
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
	public String getApplicationTypeName(Integer applicationTypeId) {

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
	public String getApplicationStepName(Integer applicationStepId) {

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
	 * @param applicationId     申請ID
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
	 * @param departmentId      部署ID
	 * @param applicationStepId 申請段階ID
	 * @return 版情報
	 */
	private Authority getAuthority(String departmentId, Integer applicationStepId) {

		Authority authority;

		LOGGER.trace("M_権限報取得 開始");
		List<Authority> AuthorityList = authorityRepository.getAuthorityList(departmentId, applicationStepId);
		if (AuthorityList.size() == 0) {
			LOGGER.warn("M_権限報取得が存在しません。");
			throw new RuntimeException();
		}
		authority = AuthorityList.get(0);
		LOGGER.trace("M_権限報取得取得 終了");

		return authority;
	}

	/**
	 * O_受付回答ファイルEntityを受付回答情報フォームに詰めなおす
	 * 
	 * @param entity            O_受付回答ファイルEntity
	 * @param departmentForm    部署フォーム
	 * @param applicationTypeId 申請種類ID
	 * 
	 * @return 受付回答情報フォーム
	 */
	private AcceptingAnswerForm getAcceptingAnswerFormFromEntity(AcceptingAnswer entity, DepartmentForm departmentForm,
			Integer applicationTypeId) {
		LOGGER.debug("受付回答情報フォーム生成 開始");
		try {

			AcceptingAnswerForm form = new AcceptingAnswerForm();

			// 申請段階ID
			Integer applicationStepId = entity.getApplicationStepId();

			// 受付回答ID
			form.setAcceptingAnswerId(entity.getAcceptingAnswerId());
			// 版情報
			form.setVersionInfomation(entity.getVersionInfomation());

			// 区分判定情報
			AnswerJudgementForm judgeForm = new AnswerJudgementForm();
			// 判定結果
			CategoryJudgementResult judgementResult = new CategoryJudgementResult();
			AnswerDao dao = new AnswerDao(emf);

			if (ANSWER_DATA_TYPE_GOVERNMENT_ADD.equals(entity.getAnswerDataType())) {
				// 行政で追加された条項は、関連する判定区分がないので、区分判定IDがない
				judgeForm.setJudgementId(entity.getJudgementId());
				judgeForm.setTitle(govermentAddAnswerTitle);
			} else {

				LOGGER.trace("M_判定結果一覧取得 開始");
				List<CategoryJudgementResult> categoryJudgementResultList = dao.getJudgementResultList(
						entity.getJudgementId(), applicationTypeId, applicationStepId, entity.getDepartmentId());
				LOGGER.trace("M_判定結果一覧取得 終了");
				if (categoryJudgementResultList.size() > 0) {
					judgementResult = categoryJudgementResultList.get(0);
					judgeForm.setJudgementId(entity.getJudgementId());
					judgeForm.setTitle(judgementResult.getTitle());
				}
			}

			List<DepartmentForm> departmentFormList = new ArrayList<DepartmentForm>();
			departmentFormList.add(departmentForm);
			judgeForm.setDepartments(departmentFormList);
			form.setJudgementInformation(judgeForm);

			// 判定結果
			form.setJudgementResult(entity.getJudgementResult());
			// 回答内容
			form.setAnswerContent(entity.getAnswerContent());
			// 回答期限日時
			if (entity.getDeadlineDatetime() != null) {
				int month = entity.getDeadlineDatetime().getMonthValue();
				int day = entity.getDeadlineDatetime().getDayOfMonth();
				String dateText = String.valueOf(month) + "/" + String.valueOf(day);
				form.setDeadlineDatetime(dateText);
			}

			// 回答履歴一覧
			if (entity.getAnswerId() != null && entity.getAnswerId() > 0) {

				List<AnswerHistoryForm> answerHistoryForm = answerService.getAnswerHistory(entity.getApplicationId(),
						applicationTypeId, applicationStepId, true, entity.getAnswerId(), 0);
				form.setAnswerHistorys(answerHistoryForm);
			} else {
				form.setAnswerHistorys(new ArrayList<AnswerHistoryForm>());
			}

			// 更新日時 */
			form.setUpdateDatetime(entity.getUpdateDatetime());

			/// データ種類
			form.setAnswerDataType(entity.getAnswerDataType());

			// 回答ID
			form.setAnswerId(entity.getAnswerId());

			return form;
		} finally {
			LOGGER.debug("受付回答情報フォーム生成 終了");
		}
	}

	/**
	 * 回答通知担当課に受付通知（照合ID・パスワードを含む）送付
	 * 
	 * @param loginId           ログインID
	 * @param password          パスワード
	 * @param applicationId     申請ID
	 * @param applicationStepId 申請段階ID
	 */
	private void sendNoticeMailToGovernmentNotificationUser(String loginId, String password, Integer applicationId,
			Integer applicationStepId) {
		MailItem mailItem = new MailItem();

		// 申請IDから申請者情報取得
		List<ApplicantInformation> applicantList = applicantInformationRepository.getApplicantList(applicationId,
				CONTACT_ADDRESS_INVALID);

		if (applicantList.size() != 1) {
			LOGGER.error("申請者データの件数が不正");
			throw new RuntimeException("申請者データの件数が不正");
		}
		ApplicantInformation applicant = applicantList.get(0);

		switch (applicantNameItemNumber) { // 氏名はプロパティで設定されたアイテムを設定
		case 1:
			mailItem.setName(applicant.getItem1());
			break;
		case 2:
			mailItem.setName(applicant.getItem2());
			break;
		case 3:
			mailItem.setName(applicant.getItem3());
			break;
		case 4:
			mailItem.setName(applicant.getItem4());
			break;
		case 5:
			mailItem.setName(applicant.getItem5());
			break;
		case 6:
			mailItem.setName(applicant.getItem6());
			break;
		case 7:
			mailItem.setName(applicant.getItem7());
			break;
		case 8:
			mailItem.setName(applicant.getItem8());
			break;
		case 9:
			mailItem.setName(applicant.getItem9());
			break;
		case 10:
			mailItem.setName(applicant.getItem10());
			break;
		default:
			LOGGER.error("氏名アイテム番号指定が不正: " + applicantNameItemNumber);
			throw new RuntimeException("氏名アイテム番号指定が不正");
		}
		mailItem.setMailAddress(applicant.getMailAddress()); // メールアドレス
		mailItem.setApplicationId(applicationId.toString());// 申請ID
		mailItem.setId(loginId); // 回答確認ID
		mailItem.setPassword(password); // パスワード
		Application application = getApplication(applicationId);
		mailItem.setApplicationTypeName(getApplicationTypeName(application.getApplicationTypeId())); // 申請種類名
		mailItem.setApplicationStepName(getApplicationStepName(applicationStepId)); // 申請段階名
		mailItem.setVersionInformation(getVersionInformation(applicationId, applicationStepId).toString()); // 版情報

		// 地番
		ApplicationDao applicationDao = new ApplicationDao(emf);
		List<ApplyLotNumber> lotNumbersList = applicationDao.getApplyingLotNumberList(applicationId, lonlatEpsg);
		String addressText = (lotNumbersList != null && lotNumbersList.size() > 0)
				? lotNumbersList.get(0).getLotNumbers()
				: "";
		mailItem.setLotNumber(addressText);
				
		// ■送信先（統括部署の管理者）取得
		AnswerDao answerDao = new AnswerDao(emf);

		// 統括部署取得
		List<Department> controlDepartmentList = answerDao.getControlDepartmentList();

		// ■メール本文編集
		String subject = getMailPropValue(MailMessageUtil.KEY_ACCEPT_NOTIFICATION_LOGININFO_SUBJECT, mailItem);
		String body = getMailPropValue(MailMessageUtil.KEY_ACCEPT_NOTIFICATION_LOGININFO_BODY, mailItem);

		// 送付済みの部署リスト
		List<String> departmentIdList = new ArrayList<String>();

		for (Department department : controlDepartmentList) {
			// 通知済みの部署をスキップ
			if (!departmentIdList.contains(department.getDepartmentId())) {

				departmentIdList.add(department.getDepartmentId());

				// ■メールアドレス
				String mailAddress = EMPTY;

				mailAddress = department.getAdminMailAddress();

				LOGGER.trace(mailAddress);
				LOGGER.trace(subject);
				LOGGER.trace(body);

				try {
					final String[] mailAddressList = mailAddress.split(COMMA);
					for (String aMailAddress : mailAddressList) {
						mailSendutil.sendMail(aMailAddress, subject, body);
					}
				} catch (Exception e) {
					LOGGER.error("メール送信時にエラー発生", e);
					throw new RuntimeException(e);
				}
			}
		}
	}

	/**
	 * 事業者へ1回目の回答通知を行ったから、事業者回答登録までの回答があるか判断
	 * 
	 * @param answerList
	 * @return
	 */
	private boolean notificableByAdmin(List<Answer> answerList) {
		boolean notificable = false;

		// 事業者へ通知済み、かつ事業者未回答の回答件数
		int count = 0;
		for (Answer answer : answerList) {

			if (answer.getNotifiedFlag() == null || answer.getNotifiedFlag()) {
				if (answer.getBusinessPassStatus() == null || EMPTY.equals(answer.getBusinessPassStatus().trim())) {
					count++;
				}
			}
		}

		if (count > 0) {
			notificable = true;
		}
		return notificable;
	}

	/**
	 * 申請ファイル削除
	 * 
	 * @param applicationFile O_申請ファイル
	 */
	private void deleteApplicationFile(ApplicationFile applicationFile) {
		LOGGER.debug("申請ファイル削除処理　開始　ファイルID: " + applicationFile.getFileId());

		// ファイル自体の削除フラグ
		boolean isDelete = false;

		if (applicationFile.getFilePath() != null) {
			// ファイルパスからファイルIDを抽出
			// ファイルパスは「/application/<申請ID>/<申請ファイルID>/<ファイルID>/<申請段階ID>/<版情報>/<アップロードファイル名>」
			String[] filePaths = applicationFile.getFilePath().split(PATH_SPLITTER);
			String fileId = filePaths[4];

			// ファイルパス中のファイルIDがレコードのファイルIDと同じの場合、追加登録のファイルとして、実体が削除可能
			// 異なる場合、前版、または、前段階から引継のファイルとして、ファイル削除不可
			if (fileId != null && fileId.equals(applicationFile.getFileId().toString())) {
				isDelete = true;
			}
		}

		// 削除したい申請ファイル
		if (isDelete) {
			File tmpFile = new File(fileRootPath + applicationFile.getFilePath());
			LOGGER.trace("申請ファイル実体削除開始: " + tmpFile.getAbsolutePath());
			if (tmpFile.exists()) {
				if (!FileSystemUtils.deleteRecursively(tmpFile)) {
					LOGGER.error("申請ファイル実体削除に失敗: " + tmpFile.getAbsolutePath());
					throw new RuntimeException("申請ファイル実体の削除に失敗");
				}
			} else {
				LOGGER.warn("削除する申請ファイルが存在しない");
			}
			LOGGER.trace("申請ファイル実体削除 完了");
		} else {
			LOGGER.trace("申請ファイルが前版、または、前段階から引継ぎなので、ファイル実体削除をスキップ");
		}

		// O_申請ファイル
		LOGGER.trace("申請ファイルDB削除開始 ファイルID: " + applicationFile.getFileId());
		if (applicationFileJdbc.deleteApplicationFile(applicationFile.getFileId()) != 1) {
			LOGGER.error("申請ファイルデータの削除件数が不正");
			throw new RuntimeException("申請ファイルデータの削除件数が不正");
		}
		LOGGER.trace("申請ファイルDB削除 完了");
	}

	/**
	 * 【申請】仮申請状態のデータを消去する<br>
	 * 申請情報（事前相談）を初回申請する際に、<br>
	 * 申請IDを発行したから、申請完了まで、異常が発生する場合、<br>
	 * 仮申請状態のデータ（申請・申請区分・申請ファイル・申請者情報・回答）を削除する
	 * 
	 * @param applicationId 申請ID
	 * @return 実施結果
	 */
	public boolean deleteProvisionalApplication(Integer applicationId) {
		LOGGER.debug("申請情報ロールバック処理 開始");
		try {
			//O_申請より、仮申請であるか判断
			List<Application> applicationList = applicationRepository.getApplicationList(applicationId);
			if (applicationList.size() == 0) {
				LOGGER.warn("申請情報取得不能");
				return false;
			}
			
			Application application = applicationList.get(0);		
			
			//　申請済みの場合、ロールバック処理が実施不要
			if (STATE_APPLIED.equals(application.getRegisterStatus())) {
				LOGGER.trace("申請【申請ID："+ applicationId +"】が申請済みの状態になるため、ロールバック処理が実施不要");
				return true;
			}
			
			//申請IDに紐づくレコードがいかの各テーブルから削除
			
			//　O_回答
			List<Answer> answerList = answerRepository.findByApplicationId(applicationId);
			for (Answer answer : answerList) {
				// レコードが物理削除
				if (answerJdbc.deleteAnswer(answer) != 1) {
					LOGGER.error("回答データの削除件数が不正");
					throw new RuntimeException("回答データの削除件数が不正");
				}
			}

			// O_申請ファイル
			List<ApplicationFile> applicationFileList = applicationFileRepository
					.getApplicationFilesByApplicationId(applicationId);
			for (ApplicationFile applicationFile : applicationFileList) {
				// 申請ファイルのレコードと実体を削除
				deleteApplicationFile(applicationFile);
			}
			
			//　F_申請地番
			ApplicationDao dao = new ApplicationDao(emf);
			List<ApplyLotNumber> lotNumbersList = dao.getApplyingLotNumberList(applicationId, lonlatEpsg);
			if(lotNumbersList.size() == 0) {
				// 申請地番がDBに登録されていないため、削除処理をスキップ
			}else {
				// レコードが物理削除
				if (applicationLotNumberJdbc.delete(applicationId) != 1) {
					LOGGER.error("申請地番データの削除件数が不正");
					throw new RuntimeException("申請地番データの削除件数が不正");
				}
			}
			
			//　O_申請区分
			List<ApplicationCategory> applicationCategoryList = applicationCategoryRepository
					.getApplicationCategoryByApplicationId(applicationId);
			for (ApplicationCategory applicationCategory : applicationCategoryList) {

				if (applicationCategoryJdbc.deleteApplicationCategory(applicationCategory) != 1) {
					LOGGER.error("申請区分データの削除件数が不正");
					throw new RuntimeException("申請区分データの削除件数が不正");
				}
			}
			
			//　O_申請者情報
			List<ApplicantInformation> applicantInformationList = applicantInformationRepository.getApplicantListByApplicationId(applicationId);
			for (ApplicantInformation applicantInformation : applicantInformationList) {

				if (applicantJdbc.delete(applicantInformation.getApplicantId()) != 1) {
					LOGGER.error("申請者情報データの削除件数が不正");
					throw new RuntimeException("申請者情報データの削除件数が不正");
				}
			}
			
			//　O_申請版情報
			List<ApplicationVersionInformation> applicationVersionInformationList = applicationVersionInformationRepository.getApplicationWithUnRegisted(applicationId);
			for (ApplicationVersionInformation applicat : applicationVersionInformationList) {
				if (applicationVersionInformationJdbc.delete(applicationId, applicat.getApplicationStepId(), applicat.getUpdateDatetime()) != 1) {
					LOGGER.error("申請版情報の削除件数が不正");
					throw new RuntimeException("申請版情報データの削除件数が不正");
				}
			}
			// O_申請
			if (applicationJdbc.delete(applicationId) != 1) {
				LOGGER.error("申請データの削除件数が不正");
				throw new RuntimeException("申請データの削除件数が不正");
			}

			return true;
		} finally {
			LOGGER.debug("申請情報ロールバック処理 終了");
		}
	}
}
