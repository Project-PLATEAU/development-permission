package developmentpermission.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.chrono.JapaneseDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.transaction.Transactional;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import developmentpermission.dao.ApplicationDao;
import developmentpermission.entity.Answer;
import developmentpermission.entity.ApplicantInformation;
import developmentpermission.entity.ApplicantInformationAdd;
import developmentpermission.entity.ApplicantInformationItem;
import developmentpermission.entity.ApplicantInformationItemOption;
import developmentpermission.entity.Application;
import developmentpermission.entity.ApplicationCategory;
import developmentpermission.entity.ApplicationCategoryMaster;
import developmentpermission.entity.ApplicationVersionInformation;
import developmentpermission.entity.ApplyLotNumber;
import developmentpermission.entity.CategoryJudgementResult;
import developmentpermission.entity.Department;
import developmentpermission.entity.Ledger;
import developmentpermission.entity.LedgerLabelMaster;
import developmentpermission.entity.LedgerMaster;
import developmentpermission.form.LedgerForm;
import developmentpermission.repository.AnswerRepository;
import developmentpermission.repository.ApplicantInformationAddRepository;
import developmentpermission.repository.ApplicantInformationItemOptionRepository;
import developmentpermission.repository.ApplicantInformationItemRepository;
import developmentpermission.repository.ApplicantInformationRepository;
import developmentpermission.repository.ApplicationCategoryMasterRepository;
import developmentpermission.repository.ApplicationCategoryRepository;
import developmentpermission.repository.ApplicationRepository;
import developmentpermission.repository.ApplicationVersionInformationRepository;
import developmentpermission.repository.DepartmentRepository;
import developmentpermission.repository.JudgementResultRepository;
import developmentpermission.repository.LedgerLabelMasterRepository;
import developmentpermission.repository.LedgerMasterRepository;
import developmentpermission.repository.LedgerRepository;
import developmentpermission.repository.jdbc.LedgerJdbc;
import developmentpermission.util.model.ExportLedgerFormParam;


/**
 * 
 * 帳票生成用サービス
 *
 */
@Service
@Transactional
public class LedgerService extends AbstractService {
	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(LedgerService.class);

	/** テーブル名: O_申請 */
	private final String TABLE_NAME_O_APPLICATION = "o_application";

	/** テーブル名: O_申請区分 */
	private final String TABLE_NAME_O_APPLICATION_CATEGORY = "o_application_category";

	/** テーブル名: O_申請版情報 */
	private final String TABLE_NAME_O_APPLICATION_VERSION_INFORMATION = "o_application_version_information";

	/** テーブル名: O_申請追加情報 */
	private final String TABLE_NAME_O_APPLICANT_INFORMATION_ADD = "o_applicant_information_add";

	/** テーブル名： O_申請者情報 */
	private final String TABLE_NAME_O_APPLICANT_INFORMATION = "o_applicant_information";

	/** テーブル名: F_申請地番 */
	private final String TABLE_NAME_F_APPLICATION_LOT_NUMBER = "f_application_lot_number";

	/** カラム: O_申請.登録日時 */
	private final String COLUMN_NAME_O_APPLICATION_REGISTER_DATETIME = "register_datetime";

	/** カラム: F_申請地番.地番一覧 */
	private final String COLUMN_NAME_F_APPLICATION_LOT_NUMBER_LOT_NUMBERS = "lot_numbers";

	/** カラム: O_申請版情報.完了日時 */
	private final String COLUMN_NAME_O_APPLICATION_VERSION_INFORMATION_COMPLETE_DATETIME = "complete_datetime";

	/** 出力データ変換オーダ: 丸め桁数 */
	private final String CONVERT_ORDER_ROUND = "round";

	/** 出力データ変換オーダ: 日付フォーマット */
	private final String CONVERT_ORDER_DATEFORMAT = "dateformat";

	/** 出力データ変換オーダ: 和暦 */
	private final String CONVERT_ORDER_JAPANESE = "japanese";

	/** 出力データ変換オーダ: 和暦 */
	private final String CONVERT_ORDER_JAPANESE_TRUE = "true";

	/** 出力データ変換オーダ: 加減算日数 */
	private final String CONVERT_ORDER_DAY = "day";

	/** 出力データ変換オーダ: 区切り文字 */
	private final String CONVERT_ORDER_SEPARATE = "separate";

	/** 出力データ変換オーダ: 区切り文字カンマ */
	private final String CONVERT_ORDER_SEPARATE_COMMA = "comma";

	/** 帳票種類: 開発登録簿に含める */
	private final String LEDGER_TYPE_INCLUDE_DEVELOPMENT_DOCUMENT = "1";

	/** M_帳票Repositoryインスタンス */
	@Autowired
	private LedgerMasterRepository ledgerMasterRepository;
	/** M_申請区分Repositoryインスタンス */
	@Autowired
	private ApplicationCategoryMasterRepository applicationCategoryMasterRepository;
	/** O_申請区分Repositoryインスタンス */
	@Autowired
	private ApplicationCategoryRepository applicationCategoryRepository;
	/** O_申請者情報Repositoryインスタンス */
	@Autowired
	private ApplicantInformationRepository applicantInformationRepository;
	/** O_申請Repositoryインスタンス */
	@Autowired
	private ApplicationRepository applicationRepository;

	/** O_申請版情報Repositoryインスタンス */
	@Autowired
	private ApplicationVersionInformationRepository applicationVersionInformationRepository;

	/** M_申請者情報項目Repositoryインスタンス */
	@Autowired
	private ApplicantInformationItemRepository applicantInformationItemRepository;

	/** M_申請情報項目選択肢Repositoryインスタンス */
	@Autowired
	private ApplicantInformationItemOptionRepository applicantInformationItemOptionRepository;

	/** O_申請追加報項目選択肢Repositoryインスタンス */
	@Autowired
	private ApplicantInformationAddRepository applicantInformationAddRepository;

	/** O_回答Repositoryインスタンス */
	@Autowired
	private AnswerRepository answerRepository;

	/** M_帳票ラベル Repositoryインスタンス */
	@Autowired
	private LedgerLabelMasterRepository ledgerLabelMasterRepository;

	/** O_帳票JDBCインスタンス */
	@Autowired
	private LedgerJdbc ledgerJdbc;

	/** O_帳票Repositoryインスタンス */
	@Autowired
	private LedgerRepository ledgerRepository;

	/** M_判定結果Repositoryインスタンス */
	@Autowired
	private JudgementResultRepository judgementResultRepository;
	
	/** M_部署Repositoryインスタンス */
	@Autowired
	private DepartmentRepository departmentRepository;
	
	/** ファイル管理rootパス */
	@Value("${app.file.rootpath}")
	protected String fileRootPath;
	/** 帳票ファイル管理フォルダパス */
	@Value("${app.file.ledger.folder}")
	private String ledgerFolderPath;

	/** 条項詳細出力必要の帳票IDリスト */
	@Value("${app.ledger.output.detail.ledgerId.list}")
	protected List<String> outputDetailLedgerIdList;
	/** 帳票プロパティ定義 */
	@Value("${app.ledger.properties}")
	protected String ledgerProperties;

	/** 開発登録簿Serviceインスタンス */
	@Autowired
	private DevelopmentRegisterService developmentRegisterService;

	/** 対象シート番号 */
	private static final int TARGET_SHEET_INDEX = 0;

	/**
	 * 申請段階の帳票生成
	 * 
	 * @param applicationId
	 * @param applicationStepId
	 */
	public void exportLedger(int applicationId, int applicationStepId) throws Exception {

		/**
		 * 帳票マスタ一覧取得
		 */
		LOGGER.info("帳票生成開始");
		final List<LedgerMaster> masterLedgers = ledgerMasterRepository.getLedgerMasterListForExport(applicationStepId);
		if (masterLedgers.size() == 0) {
			LOGGER.info("帳票マスタ件数0件のため帳票生成はスキップ");
			return;
		}
		/**
		 * 申請関連情報取得
		 */
		// 申請
		final List<Application> applications = applicationRepository.getApplicationList(applicationId);
		// 申請版情報
		final List<ApplicationVersionInformation> applicationVersionInformations = applicationVersionInformationRepository
				.getLatestApplicationVersionInformation(applicationId);
		int versionInformation = -1;
		// 対象の申請段階の最新の版情報を取得
		for (ApplicationVersionInformation applicationVersionInformation : applicationVersionInformations) {
			if (applicationVersionInformation.getApplicationStepId().equals(applicationStepId)) {
				versionInformation = applicationVersionInformation.getVersionInformation();
				break;
			}
		}
		if (versionInformation < 0) {
			LOGGER.info("対象の申請段階の版情報が存在しないため帳票生成はスキップ");
			return;
		}
		// 申請者情報
		final List<ApplicantInformation> applicantInformations = applicantInformationRepository
				.getApplicantList(applicationId, CONTACT_ADDRESS_INVALID);
		// 連絡先
		final List<ApplicantInformation> applicantContactInformations = applicantInformationRepository
				.getApplicantList(applicationId, CONTACT_ADDRESS_VALID);
		// 申請追加情報
		final List<ApplicantInformationAdd> applicantAddInfos = applicantInformationAddRepository
				.getApplicantInformationAddByVer(applicationId, applicationStepId, versionInformation);
		// 申請追加情報マスタ
		final List<ApplicantInformationItem> applicantAddInfoMasters = applicantInformationItemRepository
				.getApplicantAddItems(applicationStepId + "");
		final Map<String, ApplicantInformationItem> applicantAddInfoMasterMap = new HashMap<String, ApplicantInformationItem>();
		for (ApplicantInformationItem aItem : applicantAddInfoMasters) {
			applicantAddInfoMasterMap.put(aItem.getApplicantInformationItemId(), aItem);
		}
		// 申請区分
		List<ApplicationCategory> applicationCategories = new ArrayList<ApplicationCategory>();
		if (applicationStepId == APPLICATION_STEP_ID_3.intValue()) {
			// 許可判定の場合、事前協議の申請区分を取得
			int versionInformationStep2 = -1;
			List<ApplicationVersionInformation> step2VersionInformation = applicationVersionInformationRepository
					.findByApplicationSteId(applicationId, APPLICATION_STEP_ID_2);
			if (step2VersionInformation.size() > 0) {
				versionInformationStep2 = step2VersionInformation.get(0).getVersionInformation();
			}
			applicationCategories = applicationCategoryRepository.findByVer(applicationId, APPLICATION_STEP_ID_2,
					versionInformationStep2);
		} else {
			applicationCategories = applicationCategoryRepository.findByVer(applicationId, applicationStepId,
					versionInformation);
		}

		// 申請区分マスタ
		final List<ApplicationCategoryMaster> applicationCategoryMaster = applicationCategoryMasterRepository.findAll();
		final Map<String, Map<String, String>> applicationCategoryMasterMap = new HashMap<String, Map<String, String>>();
		for (ApplicationCategoryMaster aMaster : applicationCategoryMaster) {
			if (applicationCategoryMasterMap.containsKey(aMaster.getViewId())) {
				applicationCategoryMasterMap.get(aMaster.getViewId()).put(aMaster.getCategoryId(),
						aMaster.getLabelName());
			} else {
				final Map<String, String> aMap = new HashMap<String, String>();
				aMap.put(aMaster.getCategoryId(), aMaster.getLabelName());
				applicationCategoryMasterMap.put(aMaster.getViewId(), aMap);
			}
		}
		// 申請地番
		ApplicationDao dao = new ApplicationDao(emf);
		List<ApplyLotNumber> lotNumbersList = dao.getApplyingLotNumberList(applicationId, lonlatEpsg);
		/**
		 * 帳票マスタごとに出力処理を実行
		 */
		for (LedgerMaster aMasterLedger : masterLedgers) {
			/**
			 * 出力要否確認
			 */
			boolean exportFlag = false;
			if ("0".equals(aMasterLedger.getOutputType())) {
				// 常に出力
				exportFlag = true;
			} else {
				// 条件付き出力
				if (applicationStepId == APPLICATION_STEP_ID_2.intValue()) {
					// 協議対象回答の存在有無をチェック
					List<Answer> answers = answerRepository.findByApplicationIdAndapplicationStepId(applicationId,
							applicationStepId);
					for (Answer aAnswer : answers) {
						if (aAnswer.getDiscussionItem() != null) {
							String[] discussionItems = aAnswer.getDiscussionItem().split(",");
							exportFlag = (Arrays.asList(discussionItems).contains(aMasterLedger.getLedgerId()));
							if (exportFlag) {
								break;
							}
						}
					}
				}
			}
			if (exportFlag) {
				LOGGER.info(String.format("帳票生成開始.帳票名=%s", aMasterLedger.getLedgerName()));
				// 帳票埋込テキスト定義取得
				List<LedgerLabelMaster> ledgerLabels = ledgerLabelMasterRepository
						.findByLedgerId(aMasterLedger.getLedgerId());
				/**
				 * 埋込テキスト情報生成
				 */
				Map<String, String> replaceMap = new HashMap<String, String>();
				for (LedgerLabelMaster aLabel : ledgerLabels) {
					String replaceText = "";
					// 一律Object型としてまずはデータを抽出する
					Object srcData = null;
					/**
					 * データ抽出
					 */
					final String tableName = aLabel.getTableName();
					final String exportColumnName = aLabel.getExportColumnName();
					final String filterColumnName = aLabel.getFilterColumnName();
					final String filterCondition = aLabel.getFilterCondition();
					final String itemId1 = aLabel.getItemId1();
					final String itemId2 = aLabel.getItemId2();

					if (tableName.equals(TABLE_NAME_O_APPLICATION_CATEGORY)) {
						// O_申請区分
						// 画面IDを項目ID1で指定、項目名称（M_申請区分.選択肢名）を出力
						final List<String> categoryNames = new ArrayList<String>();
						for (ApplicationCategory applicationCategory : applicationCategories) {
							if (applicationCategory.getViewId().equals(itemId1)) {
								final String categoryName = (applicationCategoryMasterMap
										.containsKey(applicationCategory.getViewId()))
												? applicationCategoryMasterMap.get(applicationCategory.getViewId())
														.containsKey(applicationCategory.getCategoryId())
																? applicationCategoryMasterMap
																		.get(applicationCategory.getViewId())
																		.get(applicationCategory.getCategoryId())
																: null
												: null;
								if (categoryName != null) {
									categoryNames.add(categoryName);
								}
							}
						}
						srcData = categoryNames;
					} else if (tableName.equals(TABLE_NAME_O_APPLICATION_VERSION_INFORMATION)) {
						// O_申請版情報
						// 項目ID1で申請段階IDを指定
						final Integer targetApplicationStepId = Integer.parseInt(itemId1);
						ApplicationVersionInformation targetVersionInformation = null;
						for (ApplicationVersionInformation aVersion : applicationVersionInformations) {
							if (targetApplicationStepId.equals(aVersion.getApplicationStepId())) {
								targetVersionInformation = aVersion;
								break;
							}
						}
						if (targetVersionInformation != null) {
							// 完了日時
							if (exportColumnName
									.equals(COLUMN_NAME_O_APPLICATION_VERSION_INFORMATION_COMPLETE_DATETIME)) {
								srcData = targetVersionInformation.getCompleteDatetime();
							}
						}
					} else if (tableName.equals(TABLE_NAME_O_APPLICANT_INFORMATION_ADD)) {
						// O_申請追加情報
						for (ApplicantInformationAdd applicantInformationAdd : applicantAddInfos) {
							final String itemId = applicantInformationAdd.getApplicantInformationItemId();
							if (itemId1.equals(itemId) && applicantAddInfoMasterMap.containsKey(itemId)) {
								// 申請追加マスタ情報を取得
								ApplicantInformationItem applicantInformationItem = applicantAddInfoMasterMap
										.get(itemId);
								// 項目型
								final String itemType = applicantInformationItem.getItemType();
								// 項目値
								final String itemValue = applicantInformationAdd.getItemValue();
								if (itemType.equals(APPLICANT_ITEM_TYPE_TEXT)
										|| itemType.equals(APPLICANT_ITEM_TYPE_TEXTAREA)) {
									// テキスト、テキストエリア
									srcData = itemValue;
								} else if (itemType.equals(APPLICANT_ITEM_TYPE_DATE)) {
									// 日付
									try {
										SimpleDateFormat sdf = new SimpleDateFormat(ITEM_TYPE_DATE_FORMAT);
										Date formatDate = sdf.parse(itemValue);
										srcData = LocalDateTime.ofInstant(formatDate.toInstant(),
												ZoneId.systemDefault());
									} catch (Exception e) {
										LOGGER.error("申請追加情報項目日付型変換に失敗", e);
									}

								} else if (itemType.equals(APPLICANT_ITEM_TYPE_NUMBER)) {
									// 数値: 一旦Doubleで投入
									try {
										srcData = Double.parseDouble(itemValue);
									} catch (Exception e) {
										LOGGER.error("申請追加情報項目数値型変換に失敗", e);
									}
								} else if (itemType.equals(ITEM_TYPE_SINGLE_SELECTION)) {
									// 単一選択
									List<ApplicantInformationItemOption> applicantInformationItemOptionList = applicantInformationItemOptionRepository
											.findByApplicantInformationItemId(itemId);
									for (ApplicantInformationItemOption aOption : applicantInformationItemOptionList) {
										if (aOption.getApplicantInformationItemOptionId().equals(itemValue)) {
											srcData = aOption.getApplicantInformationItemOptionName();
											break;
										}
									}
								} else if (itemType.equals(ITEM_TYPE_MULTIPLE_SELECTION)) {
									// 複数選択
									if (itemValue != null) {
										final String[] itemIds = itemValue.split(",");
										List<ApplicantInformationItemOption> applicantInformationItemOptionList = applicantInformationItemOptionRepository
												.findByApplicantInformationItemId(itemId);
										final List<String> itemNames = new ArrayList<String>();
										for (ApplicantInformationItemOption aOption : applicantInformationItemOptionList) {
											if (Arrays.asList(itemIds)
													.contains(aOption.getApplicantInformationItemOptionId())) {
												itemNames.add(aOption.getApplicantInformationItemOptionName());
											}
										}
										srcData = itemNames;
									}

								}
							}
						}
					} else if (tableName.equals(TABLE_NAME_O_APPLICANT_INFORMATION)) {
						// O_申請者情報
						ApplicantInformation aInfo = null;
						// 連絡先フラグをM_帳票ラベル.項目ID1で設定
						if (itemId1.equals(CONTACT_ADDRESS_VALID) && applicantContactInformations.size() > 0) {
							// 連絡先
							aInfo = applicantContactInformations.get(0);
						} else if (itemId1.equals(CONTACT_ADDRESS_INVALID) && applicantInformations.size() > 0) {
							// 申請者情報
							aInfo = applicantInformations.get(0);
						}
						if (aInfo != null) {
							// 項目ID2で指定された申請者情報項目値を取得
							switch (itemId2) {
							case "1001":
								srcData = aInfo.getItem1();
								break;
							case "1002":
								srcData = aInfo.getItem2();
								break;
							case "1003":
								srcData = aInfo.getItem3();
								break;
							case "1004":
								srcData = aInfo.getItem4();
								break;
							case "1005":
								srcData = aInfo.getItem5();
								break;
							case "1006":
								srcData = aInfo.getItem6();
								break;
							case "1007":
								srcData = aInfo.getItem7();
								break;
							case "1008":
								srcData = aInfo.getItem8();
								break;
							case "1009":
								srcData = aInfo.getItem9();
								break;
							case "1010":
								srcData = aInfo.getItem10();
								break;
							}
						}
					} else if (tableName.equals(TABLE_NAME_F_APPLICATION_LOT_NUMBER)) {
						// F_申請地番
						ApplyLotNumber lotNumber = null;
						if (lotNumbersList.size() > 0) {
							lotNumber = lotNumbersList.get(0);
							// 申請地番
							if (exportColumnName.equals(COLUMN_NAME_F_APPLICATION_LOT_NUMBER_LOT_NUMBERS)) {
								srcData = lotNumber.getLotNumbers();
							}
						}
					} else if (tableName.equals(TABLE_NAME_O_APPLICATION)) {
						// O_申請
						if (applications.size() > 0) {
							Application sApplication = applications.get(0);
							// 登録日時
							if (exportColumnName.equals(COLUMN_NAME_O_APPLICATION_REGISTER_DATETIME)) {
								srcData = sApplication.getRegisterDatetime();
							}
						}

					} else {
						LOGGER.info(String.format("埋込未対応のテーブルが指定された.table_name=%s", tableName));
					}

					/**
					 * データ変換
					 */
					// 変換オーダ
					final String convertOrder = aLabel.getConvertOrder();
					final Map<String, String> convertOrders = new HashMap<String, String>();
					if (convertOrder != null) {
						final String[] convertOrderSep1 = convertOrder.split(",");
						for (String aSep : convertOrderSep1) {
							final String[] convertOrderSep2 = aSep.split("=");
							if (convertOrderSep2.length > 1) {
								convertOrders.put(convertOrderSep2[0], convertOrderSep2[1]);
							}
						}
					}
					// 埋込フォーマット
					final String convertFormat = (aLabel.getConvertFormat() != null
							&& !aLabel.getConvertFormat().isEmpty()) ? aLabel.getConvertFormat() : "%s";
					// 元データを文字列に変換.変換タイプ指定がある場合変換を実施
					try {
						String baseText = "";
						if (srcData != null) {
							if (srcData instanceof String) {
								baseText = (String) srcData;
							} else if (srcData instanceof Integer) {
								baseText = ((Integer) srcData).toString();
							} else if (srcData instanceof Long) {
								baseText = ((Long) srcData).toString();
							} else if (srcData instanceof Float) {
								Double dData = ((Float) srcData).doubleValue();
								if (convertOrders.containsKey(CONVERT_ORDER_ROUND)) {
									// 桁数丸め
									baseText = roundDoubleValue(dData, convertOrders.get(CONVERT_ORDER_ROUND));
								} else {
									// 指定ない場合整数化
									baseText = roundDoubleValue(dData, "0");
								}
							} else if (srcData instanceof Double) {
								Double dData = (Double) srcData;
								if (convertOrders.containsKey(CONVERT_ORDER_ROUND)) {
									// 桁数丸め
									baseText = roundDoubleValue(dData, convertOrders.get(CONVERT_ORDER_ROUND));
								} else {
									// 指定ない場合整数化
									baseText = roundDoubleValue(dData, "0");
								}
							} else if (srcData instanceof LocalDateTime) {
								LocalDateTime srcLd = (LocalDateTime) srcData;
								if (convertOrders.containsKey(CONVERT_ORDER_DAY)) {
									// 日付加算
									try {
										int addDays = Integer.parseInt(convertOrders.get(CONVERT_ORDER_DAY));
										if (addDays > 0) {
											srcLd = srcLd.plusDays(addDays);
										} else {
											srcLd = srcLd.minusDays(Math.abs(addDays));
										}
									} catch (Exception e) {
										LOGGER.error("日付加減算処理に失敗", e);
									}
								}
								if (convertOrders.containsKey(CONVERT_ORDER_DATEFORMAT)) {
									try {
										Locale locale = new Locale("ja", "JP", "JP");
										DateTimeFormatter dFormat = DateTimeFormatter
												.ofPattern(convertOrders.get(CONVERT_ORDER_DATEFORMAT), locale);
										if (convertOrders.containsKey(CONVERT_ORDER_JAPANESE) && convertOrders
												.get(CONVERT_ORDER_JAPANESE).equals(CONVERT_ORDER_JAPANESE_TRUE)) {
											// 和暦変換
											JapaneseDate japaneseDate = JapaneseDate.from(srcLd);
											baseText = japaneseDate.format(dFormat);
										} else {
											baseText = dFormat.format(srcLd);
										}

									} catch (Exception e) {
										LOGGER.error("日付フォーマット処理に失敗", e);
										// フォーマット不正の場合デフォルトフォーマットとする
										DateTimeFormatter dateTimeFormatter = DateTimeFormatter
												.ofPattern(ITEM_TYPE_DATE_FORMAT);
										baseText = dateTimeFormatter.format(srcLd);
									}
								} else {
									// 指定ない場合デフォルトフォーマットとする
									DateTimeFormatter dateTimeFormatter = DateTimeFormatter
											.ofPattern(ITEM_TYPE_DATE_FORMAT);
									baseText = dateTimeFormatter.format(srcLd);
								}
							} else if (srcData instanceof List) {
								List<?> tmpList = (List<?>) srcData;
								if (tmpList.size() > 0) {
									if (tmpList.get(0) instanceof String) {
										List<String> dataList = (List<String>) srcData;
										if (convertOrders.containsKey(CONVERT_ORDER_SEPARATE)) {
											final String separateText = (convertOrders.get(CONVERT_ORDER_SEPARATE)
													.equals(CONVERT_ORDER_SEPARATE_COMMA)) ? ","
															: convertOrders.get(CONVERT_ORDER_SEPARATE);
											baseText = String.join(separateText, dataList);
										} else {
											baseText = String.join(",", dataList);
										}
									} else {
										// String以外のリストは想定しない
										LOGGER.info("未対応のデータ形式");
									}
								}
							} else {
								LOGGER.info("未対応のデータ形式");
							}
							// 文字列埋込を実施
							replaceText = String.format(convertFormat, baseText);
						}
					} catch (Exception e) {
						LOGGER.error("帳票埋込テキスト生成に失敗", e);
					}
					LOGGER.debug("置換識別子=" + aLabel.getReplaceIdentify() + ", 置換文字列=" + replaceText);
					replaceMap.put(aLabel.getReplaceIdentify(), replaceText);
				}
				/**
				 * 帳票作成
				 */
				final String ledgerTemplatePath = aMasterLedger.getTemplatePath();
				int dotIndex = ledgerTemplatePath.lastIndexOf(".");
				final String extension = (dotIndex == -1 || dotIndex == 0) ? ""
						: ledgerTemplatePath.substring(dotIndex + 1);
				// 保存パス
				// [root]/ledger/[申請ID]/[申請段階ID]/[帳票マスタID/[申請ID]_[帳票名][拡張子]
				String ledgerSavePath = ledgerFolderPath + "/" + applicationId + "/" + applicationStepId + "/"
						+ aMasterLedger.getLedgerId() + "/" + applicationId + "_" + aMasterLedger.getLedgerName() + "."
						+ extension;
				boolean fileExportResult = false;
				if (extension.equals("xlsx")) {
					// 条項詳細出力必要の帳票であるか判断
					if (outputDetailLedgerIdList.contains(aMasterLedger.getLedgerId())) {
						// 出力詳細内容データ編集
						ExportLedgerFormParam param = getExportLedgerParamFromProperties(aMasterLedger, applicationId,
								applicationStepId, applications.get(0).getApplicationTypeId());

						if (param == null) {
							LOGGER.error("帳票を印字する用パラメータ編集に失敗しました。");
							throw new Exception("帳票生成に失敗");
						}

						fileExportResult = exportLedgerExcel(ledgerTemplatePath, replaceMap, ledgerSavePath, param);
					} else {
						fileExportResult = exportLedgerExcel(ledgerTemplatePath, replaceMap, ledgerSavePath, null);
					}
				} else {
					// 帳票形式はxlsxのみサポート
					LOGGER.info("サポートされていない帳票形式");
				}
				/**
				 * 帳票パス保存
				 */
				if (fileExportResult) {
					final Ledger ledger = new Ledger();
					// 申請ID
					ledger.setApplicationId(applicationId);
					// 申請段階ID
					ledger.setApplicationStepId(applicationStepId);
					// 帳票マスタID
					ledger.setLedgerId(aMasterLedger.getLedgerId());
					// ファイル名
					ledger.setFileName(aMasterLedger.getLedgerName());
					// ファイルパス
					ledger.setFilePath(ledgerSavePath);
					int fileId = ledgerJdbc.insert(ledger);
				} else {
					throw new Exception("帳票生成に失敗");
				}
			}

		}
	}

	/**
	 * 小数丸め処理
	 * 
	 * @param value        丸め対象値
	 * @param convertOrder 桁数
	 * @return 丸め文字列
	 */
	private String roundDoubleValue(double value, String convertOrder) {
		try {
			int precision = Integer.parseInt(convertOrder);
			NumberFormat numberFormat = NumberFormat.getInstance();
			numberFormat.setMaximumFractionDigits(precision);
			numberFormat.setRoundingMode(RoundingMode.DOWN);
			return numberFormat.format(value);
		} catch (Exception e) {
			LOGGER.error("数値丸め処理に失敗", e);
			return value + "";
		}
	}

	/**
	 * Excel帳票生成
	 * 
	 * @param templatePath          テンプレートパス
	 * @param replaceMap            置換文字列
	 * @param savePath              帳票保存パス
	 * @param exportLedgerFormParam 再編集用パラメータ
	 * @return 生成結果
	 */
	private boolean exportLedgerExcel(String templatePath, Map<String, String> replaceMap, String savePath,
			ExportLedgerFormParam exportLedgerFormParam) {
		boolean result = false;
		LOGGER.debug("Excel帳票生成 開始");
		try {
			Workbook wb = null;
			// テンプレート読み込み
			final File templateFile = new File(fileRootPath + "/" + templatePath);
			try (InputStream is = new FileInputStream(templateFile)) {
				wb = new XSSFWorkbook(is);
				// 最初のシートを開く
				final Sheet sheet = wb.getSheetAt(0);
				for (int i = sheet.getFirstRowNum(); i < sheet.getLastRowNum(); i++) {

					Row row = sheet.getRow(i);
					if (row == null) {
						continue;
					}

					for (int j = 0; j < row.getLastCellNum(); j++) {
						Cell cell = row.getCell(j);
						if (cell == null) {
							continue;
						}
						if (cell.getCellType() == CellType.STRING) {
							String cellValue = cell.getStringCellValue();
							// 文字列置換を実行
							for (Map.Entry<String, String> entry : replaceMap.entrySet()) {
								if (cellValue.contains(entry.getKey())) {
									cellValue = cellValue.replace(entry.getKey(), entry.getValue());
									LOGGER.debug(entry.getKey());
									LOGGER.debug(entry.getValue());
									LOGGER.debug(cellValue);
								}
							}
							cell.setCellValue(cellValue);
						}
					}
				}
				
				//　帳票再編集
				if (exportLedgerFormParam != null) {
					outputDiscussionItemDetail(wb, exportLedgerFormParam);
				}

			}
			if (wb != null) {
				// 保存
				File file = new File(fileRootPath + "/" + savePath);
				File directory = file.getParentFile();
				if (!directory.exists()) {
					directory.mkdirs();
				}
				exportWorkBook(wb, fileRootPath + "/" + savePath);
				// 全て正常完了
				result = true;
			}

		} catch (Exception e) {
			LOGGER.error("Excel帳票生成に失敗", e);
		} finally {
			LOGGER.debug("Excel帳票生成 終了");
		}
		return result;
	}

	/**
	 * 帳票ファイルアップロード
	 * 
	 * @param form 帳票ファイルフォーム
	 */
	public void uploadLedgerFile(LedgerForm form) {
		LOGGER.debug("帳票ファイルアップロード 開始");
		try {

			// fileIdからアップロード対象のファイルをチェック
			// 帳票ファイルを所定の場所に格納する
			// O_帳票.ファイルパスを更新
			// ファイル出力

			// パラメータチェック
			Integer fileId = form.getFileId();
			if (fileId == null) {
				LOGGER.warn("パラメタ不正（fileId=null）");
				throw new RuntimeException("パラメタ不正（fileId=null）");
			}

			// O_帳票検索
			List<Ledger> ledgerList = ledgerRepository.findByFileId(fileId);
			if (ledgerList.size() == 0) {
				LOGGER.warn("パラメタ不正(fileId存在なし）");
				throw new RuntimeException("パラメタ不正(fileId存在なし）");
			}
			Ledger leager = ledgerList.get(0);

			// アップロード可否チェック
			if (!form.getUploadable()) {
				LOGGER.warn("アップロード可否エラー");
				throw new RuntimeException("アップロード可否エラー");
			}

			// 旧フォルダパス
			String oldFolderPath = Paths.get(leager.getFilePath()).getParent().toString();
			// 日時
			final String dateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss-SSS")
					.format(LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()));
			// 新ファイル名（ファイル名と拡張子を分割）
			String splitUploadFileName[] = splitFilename(form.getUploadFileName());
			// 新ファイルパス
			String newFilePath = oldFolderPath + PATH_SPLITTER + splitUploadFileName[0] + "_" + dateTime + "."
					+ splitUploadFileName[1];
			// 新フルパス
			Path newFullPath = Paths.get(fileRootPath + PATH_SPLITTER + newFilePath);

			// ファイル出力
			LOGGER.trace("ファイル出力 開始");
			exportFile(form.getUploadFile(), newFullPath.toString());
			LOGGER.trace("ファイル出力 終了");

			// ファイルパス更新
			LOGGER.trace("ファイルパス更新 開始");
			newFilePath = newFilePath.replace("\\", "/");
			ledgerJdbc.updateFilePath(fileId, newFilePath);
			LOGGER.trace("ファイルパス更新 終了");
			// 帳票種類チェック
			final List<LedgerMaster> ledgerMasterList = ledgerMasterRepository
					.getLedgerMasterByLedgerId(form.getLedgerId());
			if (ledgerMasterList != null && ledgerMasterList.size() > 0) {
				final String ledgerType = ledgerMasterList.get(0).getLedgerType();
				if (ledgerType != null && LEDGER_TYPE_INCLUDE_DEVELOPMENT_DOCUMENT.equals(ledgerType)) {
					// 開発登録簿生成
					developmentRegisterService.exportDevelopmentRegisterFile(form.getApplicationId());
				}
			}

		} catch (Exception ex) {
			// RuntimeExceptionで投げないとロールバックされない
			LOGGER.error("帳票ファイルアップロード 失敗", ex);
			throw new RuntimeException(ex);
		} finally {
			LOGGER.debug("帳票ファイルアップロード 終了");
		}
	}

	/**
	 * ファイル名から拡張子とそれ以外を分割
	 * 
	 * @param filename
	 * @return String[] ※[0]:ファイル名（拡張子除く）,[1]:拡張子
	 */
	private String[] splitFilename(String filename) {
		int dotIndex = filename.lastIndexOf('.');
		if (dotIndex == -1) {
			return new String[] { filename, "" };
		} else {
			String name = filename.substring(0, dotIndex);
			String extension = filename.substring(dotIndex + 1);
			return new String[] { name, extension };
		}
	}

	/**
	 * 帳票通知
	 * 
	 * @param form 帳票ファイルフォーム
	 */
	public void notifyLedgerFile(LedgerForm form) {
		LOGGER.debug("帳票通知 開始");
		try {

			// fileIdから通知対象の帳票をチェック
			// O_帳票.通知フラグ、O_帳票.通知ファイルパスを更新

			// パラメータチェック
			Integer fileId = form.getFileId();
			if (fileId == null) {
				LOGGER.warn("パラメタ不正（fileId=null）");
				throw new RuntimeException("パラメタ不正（fileId=null）");
			}

			// O_帳票検索
			List<Ledger> ledgerList = ledgerRepository.findByFileId(fileId);
			if (ledgerList.size() == 0) {
				LOGGER.warn("パラメタ不正(fileId存在なし）");
				throw new RuntimeException("パラメタ不正(fileId存在なし）");
			}
			Ledger leager = ledgerList.get(0);

			// 通知可否チェック
			if (!form.getNotifiable()) {
				LOGGER.warn("通知可否エラー");
				throw new RuntimeException("通知可否エラー");
			}

			// ファイルパス
			String filePath = fileRootPath + PATH_SPLITTER + leager.getFilePath();
			// フォルダパス
			String folderPath = Paths.get(filePath).getParent().toString();
			// ファイル名
			String fname = Paths.get(filePath).getFileName().toString();
			// ファイルオブジェクト
			File folderObj = new File(folderPath);

			// フォルダが存在するか確認
			if (folderObj.exists() && folderObj.isDirectory()) {
				// フォルダ内のファイルを取得
				File[] files = folderObj.listFiles();
				// ファイルが存在する場合、削除を実行
				if (files != null) {
					for (File file : files) {
						if (!file.getName().equals(fname)) {
							if (file.isFile()) {
								file.delete();
							}
						}
					}
				}
			}
			// O_帳票.通知フラグ、O_帳票.通知ファイルパスを更新
			LOGGER.trace("O_帳票更新 開始");
			ledgerJdbc.updateNotify(fileId);
			LOGGER.trace("O_帳票更新 終了");

		} catch (Exception ex) {
			// RuntimeExceptionで投げないとロールバックされない
			LOGGER.error("帳票通知 失敗", ex);
			throw new RuntimeException(ex);
		} finally {
			LOGGER.debug("帳票通知 終了");
		}
	}
	
	/**
	 * プロパティ定義から帳票出力用パラメータフォームに編集
	 * 
	 * @param aMasterLedger     M_帳票
	 * @param applicationId     申請ID
	 * @param applicationStepId 申請段階ID
	 * @throws Exception
	 */
	private ExportLedgerFormParam getExportLedgerParamFromProperties(LedgerMaster aMasterLedger, int applicationId,
			int applicationStepId, int applicationTypeId) throws Exception {
		ExportLedgerFormParam param = new ExportLedgerFormParam();

		// 処理対象帳票ID
		String ledgerId = aMasterLedger.getLedgerId();

		// 帳票のプロパティ定義を取得する
		LOGGER.trace("帳票の設定をパラメータフォームに設定する　開始。　帳票ID：" + ledgerId);
		if (ledgerProperties != null) {
			try {
				ObjectMapper objectMapper = new ObjectMapper();
				Map<String, Map<String, String>> map = objectMapper.readValue(ledgerProperties,
						new TypeReference<LinkedHashMap<String, Map<String, String>>>() {
						});

				if (map.containsKey(ledgerId)) {
					Map<String, String> ledgerPropertyMap = map.get(ledgerId);

					// ■フォント名
					if (ledgerPropertyMap.containsKey("fontName")) {
						param.setFontName(ledgerPropertyMap.get("fontName"));
					} else {
						// フォント名がない場合、テンプレートのデフォルトフォントで印字
						param.setFontName(EMPTY);
					}

					// ■編集開始行
					if(ledgerPropertyMap.containsKey("editSrartRow")) {
						param.setEditSrartRow(Integer.valueOf(ledgerPropertyMap.get("editSrartRow")));
					}else {
						// フ編集開始行がない場合、テンプレートのシート最終行から編集開始
						param.setEditSrartRow(-1);
					}
					
					// ■ページ当たりの最大行数（ひな形以外の1ページ目）
					if (ledgerPropertyMap.containsKey("firstPageMaxRow")) {
						param.setFirstPageMaxRow(Integer.valueOf(ledgerPropertyMap.get("firstPageMaxRow")));
					} else {
						LOGGER.debug("帳票の「firstPageMaxRow」が設定されていません。");
						return null;
					}
					// ■ページ当たりの最大行数（ひな形以外の2ページ目以降）
					if (ledgerPropertyMap.containsKey("pageMaxRow")) {
						param.setPageMaxRow(Integer.valueOf(ledgerPropertyMap.get("pageMaxRow")));
					} else {
						LOGGER.debug("帳票の「pageMaxRow」が設定されていません。");
						return null;
					}

					// ■印刷範囲開始列
					if (ledgerPropertyMap.containsKey("printAreaStartCol")) {
						param.setPrintAreaStartCol(Integer.valueOf(ledgerPropertyMap.get("printAreaStartCol")));
					} else {
						// 印刷範囲がない場合、テンプレート印刷範囲を変更しない
						param.setPrintAreaStartCol(-1);
					}
					// ■印刷範囲終了列
					if (ledgerPropertyMap.containsKey("printAreaEndCol")) {
						param.setPrintAreaEndCol(Integer.valueOf(ledgerPropertyMap.get("printAreaEndCol")));
					} else {
						// 印刷範囲がない場合、テンプレート印刷範囲を変更しない
						param.setPrintAreaEndCol(-1);
					}

					// ■課名 印字開始列
					if (ledgerPropertyMap.containsKey("departmentNameStartCol")) {
						param.setDepartmentNameStartCol(
								Integer.valueOf(ledgerPropertyMap.get("departmentNameStartCol")));
					} else {
						// 課名印字開始列がない場合、帳票に印字しない
						param.setDepartmentNameStartCol(-1);
					}
					// ■課名 フォントサイズ
					if (ledgerPropertyMap.containsKey("departmentNameFontSize")) {
						param.setDepartmentNameFontSize(
								Short.parseShort(ledgerPropertyMap.get("departmentNameFontSize")));
					} else {
						// 課名フォントサイズがない場合、テンプレートのデフォルトフォントサイズで印字
						param.setDepartmentNameFontSize((short) -1);
					}
					// ■課名 行当たり最大文字数
					if (ledgerPropertyMap.containsKey("departmentNameRowMaxCharacter")) {
						param.setDepartmentNameRowMaxCharacter(
								Integer.valueOf(ledgerPropertyMap.get("departmentNameRowMaxCharacter")));
					} else {
						// 関連条項名の行当たり最大文字数がない場合、改行しないで印字
						param.setDepartmentNameRowMaxCharacter(-1);
					}
					// ■課名 段落番号をつけるかフラグ
					if (ledgerPropertyMap.containsKey("departmentNameParagraph")) {
						param.setDepartmentNameParagraph(
								Boolean.valueOf(ledgerPropertyMap.get("departmentNameParagraph")));
					} else {
						// 課名に段落番号をつけるかフラグがない場合、段落番号がないで印字
						param.setDepartmentNameParagraph(false);
					}
					// ■課名 段落番号が文字列と同一セルに印字するかフラグ
					if (ledgerPropertyMap.containsKey("departmentNameParagraphWithinText")) {
						param.setDepartmentNameParagraphWithinText(
								Boolean.valueOf(ledgerPropertyMap.get("departmentNameParagraphWithinText")));
					} else {
						// 課名に段落番号が文字列と同一セルに印字するかフラグがない場合、文字列と同一セルに印字する
						param.setDepartmentNameParagraphWithinText(true);
					}
					// ■課名 段落番号の書式文字
					if (ledgerPropertyMap.containsKey("departmentNameParagraphCharacter")) {
						param.setDepartmentNameParagraphCharacter(
								ledgerPropertyMap.get("departmentNameParagraphCharacter"));
					} else {
						param.setDepartmentNameParagraphCharacter(EMPTY);
					}
					// ■課名 段落番号の採番種類（0：レコード全件で採番、1：課ごとに採番）
					if (ledgerPropertyMap.containsKey("departmentNameParagraphType")) {
						param.setDepartmentNameParagraphType(ledgerPropertyMap.get("departmentNameParagraphType"));
					} else {
						param.setDepartmentNameParagraphType(EMPTY);
					}
					// ■課名 課ごとに空白行挿入フラグ
					if (ledgerPropertyMap.containsKey("departmentNameInsertBlankLine")) {
						param.setDepartmentNameInsertBlankLine(ledgerPropertyMap.get("departmentNameInsertBlankLine"));
					} else {
						param.setDepartmentNameInsertBlankLine(EMPTY);
					}

					// ■関連条項名 印字開始列
					if (ledgerPropertyMap.containsKey("judgementTitleStartCol")) {
						param.setJudgementTitleStartCol(
								Integer.valueOf(ledgerPropertyMap.get("judgementTitleStartCol")));
					} else {
						// 関連条項名印字開始列がない場合、帳票に印字しない
						param.setJudgementTitleStartCol(-1);
					}
					// ■関連条項名 フォントサイズ
					if (ledgerPropertyMap.containsKey("judgementTitleFontSize")) {
						param.setJudgementTitleFontSize(
								Short.parseShort(ledgerPropertyMap.get("judgementTitleFontSize")));
					} else {
						// 関連条項名フォントサイズがない場合、テンプレートのデフォルトフォントサイズで印字
						param.setJudgementTitleFontSize((short) -1);
					}
					// ■関連条項名 行当たり最大文字数
					if (ledgerPropertyMap.containsKey("judgementTitleRowMaxCharacter")) {
						param.setJudgementTitleRowMaxCharacter(
								Integer.valueOf(ledgerPropertyMap.get("judgementTitleRowMaxCharacter")));
					} else {
						// 関連条項名の行当たり最大文字数がない場合、改行しないで印字
						param.setJudgementTitleRowMaxCharacter(-1);
					}
					// ■関連条項名 段落番号をつけるかフラグ
					if (ledgerPropertyMap.containsKey("judgementTitleParagraph")) {
						param.setJudgementTitleParagraph(
								Boolean.valueOf(ledgerPropertyMap.get("judgementTitleParagraph")));
					} else {
						// 関連条項名に段落番号をつけるかフラグがない場合、段落番号がないで印字
						param.setJudgementTitleParagraph(false);
					}
					// ■関連条項名 段落番号が文字列と同一セルに印字するかフラグ
					if (ledgerPropertyMap.containsKey("judgementTitleParagraphWithinText")) {
						param.setJudgementTitleParagraphWithinText(
								Boolean.valueOf(ledgerPropertyMap.get("judgementTitleParagraphWithinText")));
					} else {
						// 段落番号が文字列と同一セルに印字するかフラグがない場合、文字列と同一セルに印字する
						param.setJudgementTitleParagraphWithinText(true);
					}
					// ■関連条項名 段落番号の書式文字
					if (ledgerPropertyMap.containsKey("judgementTitleParagraphCharacter")) {
						param.setJudgementTitleParagraphCharacter(
								ledgerPropertyMap.get("judgementTitleParagraphCharacter"));
					} else {
						param.setJudgementTitleParagraphCharacter(EMPTY);
					}
					// ■関連条項名 段落番号の採番種類（0：レコード全件で採番、1：課ごとに採番）
					if (ledgerPropertyMap.containsKey("judgementTitleParagraphType")) {
						param.setJudgementTitleParagraphType(ledgerPropertyMap.get("judgementTitleParagraphType"));
					} else {
						param.setJudgementTitleParagraphType(EMPTY);
					}
					// ■関連条項名 関連条項ごとに空白行挿入フラグ
					if (ledgerPropertyMap.containsKey("judgementTitleInsertBlankLine")) {
						param.setJudgementTitleInsertBlankLine(ledgerPropertyMap.get("judgementTitleInsertBlankLine"));
					} else {
						param.setJudgementTitleInsertBlankLine(EMPTY);
					}

					// ■行政回答内容 印字開始列
					if (ledgerPropertyMap.containsKey("answerContentStartCol")) {
						param.setAnswerContentStartCol(Integer.valueOf(ledgerPropertyMap.get("answerContentStartCol")));
					} else {
						// 行政回答内容印字開始列がない場合、帳票に印字しない
						param.setAnswerContentStartCol(-1);
					}
					// ■行政回答内容 フォントサイズ
					if (ledgerPropertyMap.containsKey("answerContentFontSize")) {
						param.setAnswerContentFontSize(
								Short.parseShort(ledgerPropertyMap.get("answerContentFontSize")));
					} else {
						// 行政回答内容フォントサイズがない場合、テンプレートのデフォルトフォントサイズで印字
						param.setAnswerContentFontSize((short) -1);
					}
					// ■行政回答内容 行当たり最大文字数
					if (ledgerPropertyMap.containsKey("answerContentRowMaxCharacter")) {
						param.setAnswerContentRowMaxCharacter(
								Integer.valueOf(ledgerPropertyMap.get("answerContentRowMaxCharacter")));
					} else {
						// 行政回答内容の行当たり最大文字数がない場合、改行しないで印字
						param.setAnswerContentRowMaxCharacter(-1);
					}
					// ■行政回答内容 段落番号をつけるかフラグ
					if (ledgerPropertyMap.containsKey("answerContentParagraph")) {
						param.setAnswerContentParagraph(
								Boolean.valueOf(ledgerPropertyMap.get("answerContentParagraph")));
					} else {
						// 行政回答内容に段落番号をつけるかフラグがない場合、段落番号がないで印字
						param.setAnswerContentParagraph(false);
					}
					// ■行政回答内容 段落番号が文字列と同一セルに印字するかフラグ
					if (ledgerPropertyMap.containsKey("answerContentParagraphWithinText")) {
						param.setAnswerContentParagraphWithinText(
								Boolean.valueOf(ledgerPropertyMap.get("answerContentParagraphWithinText")));
					} else {
						// 段落番号が文字列と同一セルに印字するかフラグがない場合、文字列と同一セルに印字する
						param.setAnswerContentParagraphWithinText(true);
					}
					// ■行政回答内容 段落番号の書式文字
					if (ledgerPropertyMap.containsKey("answerContentParagraphCharacter")) {
						param.setAnswerContentParagraphCharacter(
								ledgerPropertyMap.get("answerContentParagraphCharacter"));
					} else {
						param.setAnswerContentParagraphCharacter(EMPTY);
					}
					// ■行政回答内容 段落番号の採番種類（0：レコード全件で採番、1：課ごとに採番）
					if (ledgerPropertyMap.containsKey("answerContentParagraphType")) {
						param.setAnswerContentParagraphType(ledgerPropertyMap.get("answerContentParagraphType"));
					} else {
						param.setAnswerContentParagraphType(EMPTY);
					}
					// ■行政回答内容 行政回答内容ごとに空白行挿入フラグ
					if (ledgerPropertyMap.containsKey("answerContentInsertBlankLine")) {
						param.setAnswerContentInsertBlankLine(ledgerPropertyMap.get("answerContentInsertBlankLine"));
					} else {
						param.setAnswerContentInsertBlankLine(EMPTY);
					}
				} else {
					// 処理対象帳票の設定定義がない
					LOGGER.trace("帳票の設定定義が定義されていません。　帳票ID：" + ledgerId);
					return null;
				}
			} finally {
				LOGGER.trace("帳票設定定義をマップに変換しました。");
			}
		} else {
			LOGGER.trace("帳票設定定義取得に失敗しました。");
			return null;
		}
		LOGGER.trace("帳票の設定をパラメータフォームに設定する　終了。　帳票ID：" + ledgerId);

		LOGGER.trace("帳票詳細のパラメータ編集　開始");
		// 回答内容を取得
		List<Answer> answers = answerRepository.getAnswerListWithSort(applicationId, applicationStepId);

		// 課ID-「条項名、回答内容」リストのマップ
		final Map<String, List<Map<String, String>>> outputDetialMap = new LinkedHashMap<>();
		for (Answer aAnswer : answers) {

			boolean exportFlag = false;
			// 出力種類(0:常に出力、1：画面に選択されたレコードがあれば出力)より、この条項が出力対象であるか判断
			if ("0".equals(aMasterLedger.getOutputType())) {
				// 常に出力
				exportFlag = true;
			} else {
				// 選択している協議ID
				if (aAnswer.getDiscussionItem() != null) {
					String[] discussionItems = aAnswer.getDiscussionItem().split(COMMA);
					boolean isContains = (Arrays.asList(discussionItems).contains(aMasterLedger.getLedgerId()));
					if (isContains) {
						exportFlag = true;
					}
				}
			}

			// 出力対象条項である場合、パラメータに編集する
			if (exportFlag) {
				// 部署ID
				String departmentId = aAnswer.getDepartmentId();

				if (!outputDetialMap.containsKey(departmentId)) {
					List<Map<String, String>> listMap = new ArrayList<>();
					outputDetialMap.put(departmentId, listMap);
				}
				Map<String, String> judgementTitleAnswerContentMap = new HashMap<String, String>();
				// 行政回答
				judgementTitleAnswerContentMap.put("ANSWERCONTENT", aAnswer.getAnswerContent());
				// 条項名
				String title = EMPTY;
				if (aAnswer.getJudgementId() != null && !EMPTY.equals(aAnswer.getJudgementId())) {
					List<CategoryJudgementResult> categoryJudgementResult = judgementResultRepository
							.getJudgementResult(aAnswer.getJudgementId(), applicationTypeId, applicationStepId,
									departmentId);
					if (categoryJudgementResult.size() == 1) {
						title = categoryJudgementResult.get(0).getTitle();
					}
				} else {
					title = govermentAddAnswerTitle;
				}
				judgementTitleAnswerContentMap.put("TITLE", title);
				outputDetialMap.get(departmentId).add(judgementTitleAnswerContentMap);
			}
		}
		param.setOutputDataDetialMap(outputDetialMap);

		LOGGER.trace("帳票詳細のパラメータ編集　開始");

		return param;
	}

	/**
	 * 帳票再編集
	 * 
	 * @param wb    ワークブック
	 * @param param 帳票設定パラメータ
	 * @throws Exception
	 */
	public void outputDiscussionItemDetail(Workbook wb, ExportLedgerFormParam param) throws Exception {
		// 最初のシートを開く
		final Sheet sheet = wb.getSheetAt(TARGET_SHEET_INDEX);
		// テンプレートの最終行
		int startRowNum = sheet.getLastRowNum();
		// 編集開始行数
		int currentRowNum = startRowNum + 1;
		if (param.getEditSrartRow() > -1) {
			currentRowNum = param.getEditSrartRow();
		}
		// 処理中ページ数（編集部分から）
		int currentPageNum = 1;
		// 課のインデックス
		int departmentIndex = 0;
		// 全ての条項のインデックス
		int indexOfAll = 0;
		// ページごとの編集済み行数
		int countLines = 0;

		// 出力詳細データ
		Map<String, List<Map<String, String>>> outputDataDetialMap = param.getOutputDataDetialMap();

		// 担当課ごとマップを繰り返して詳細部分を印字する
		for (String departmentId : outputDataDetialMap.keySet()) {
			departmentIndex++;

			/**
			 * 課名
			 */
			// 課名取得
			List<Department> department = departmentRepository.getDepartmentListById(departmentId);
			if (department.size() == 0) {
				LOGGER.warn("部署情報の取得件数不正。　部署ID：" + departmentId);
				throw new RuntimeException("部署情報取得に失敗しました。");
			}

			// 関連条項と回答内容のマップリスト
			List<Map<String, String>> titleAnswerContentMapList = outputDataDetialMap.get(departmentId);

			// 課名編集
			List<Map<String, String>> departmentNameLineText = getOutputLines(department.get(0).getDepartmentName(),
					"0", param, departmentIndex, departmentIndex);
			// 課名出力
			for (Map<String, String> map : departmentNameLineText) {
				LOGGER.debug("課名出力 開始 行：" + currentRowNum);
				// 改ページ設定
				if (isSetRowBreak(param, currentPageNum, countLines)) {
					// 改ページ設定
					sheet.setRowBreak(currentRowNum);
					// 編集中ページ番号更新
					currentPageNum++;
					// ページごとの編集済み行数をリセット
					countLines = 0;
				}
				// セル値設定
				writeTextValue(wb, sheet, currentRowNum, "0", param, map);
				// ページごとの編集済み行数をカウンター
				countLines++;
				LOGGER.debug("課名出力 終了 行：" + currentRowNum);
				currentRowNum++;
			}

			// 課ごとの番号
			int indexOfGroup = 0;
			for (Map<String, String> titleAnswerContentMap : titleAnswerContentMapList) {
				indexOfAll++;
				indexOfGroup++;

				/**
				 * 関連条項名
				 */
				String title = titleAnswerContentMap.get("TITLE");
				// 関連条項名編集
				List<Map<String, String>> titleStrList = getOutputLines(title, "1", param, indexOfAll, indexOfGroup);

				// ■関連条項名出力
				for (Map<String, String> map : titleStrList) {
					LOGGER.debug("関連条項名出力 開始: " + currentRowNum);
					// 改ページ設定
					if (isSetRowBreak(param, currentPageNum, countLines)) {
						// 改ページ設定
						sheet.setRowBreak(currentRowNum);
						// 編集中ページ番号更新
						currentPageNum++;
						// ページごとの編集済み行数をリセット
						countLines = 0;
					}
					// セル値設定
					writeTextValue(wb, sheet, currentRowNum, "1", param, map);
					// ページごとの編集済み行数をカウンター
					countLines++;
					LOGGER.debug("関連条項名出力 終了: " + currentRowNum);
					currentRowNum++;
				}

				/**
				 * 行政回答内容
				 */
				// ■行政回答内容
				String answerContent = titleAnswerContentMap.get("ANSWERCONTENT");
				// ■行政回答内容編集
				List<Map<String, String>> answerContentStrList = getOutputLines(answerContent, "2", param, indexOfAll,
						indexOfGroup);
				// ■行政回答内容出力
				for (Map<String, String> map : answerContentStrList) {
					LOGGER.debug("行政回答内容出力 開始: " + currentRowNum);
					// 改ページ設定
					if (isSetRowBreak(param, currentPageNum, countLines)) {
						// 改ページ設定
						sheet.setRowBreak(currentRowNum);
						// 編集中ページ番号更新
						currentPageNum++;
						// ページごとの編集済み行数をリセット
						countLines = 0;
					}
					// セル値設定
					writeTextValue(wb, sheet, currentRowNum, "2", param, map);
					// ページごとの編集済み行数をカウンター
					countLines++;
					LOGGER.debug("行政回答内容出力 終了: " + currentRowNum);
					currentRowNum++;
				}
			}
		}

		// 印刷範囲開始列
		int printAreaStartCol = param.getPrintAreaStartCol();
		// 印刷範囲終了列
		int printAreaEndCol = param.getPrintAreaEndCol();
		// 印字範囲がプロパティに設定している場合、
		if (printAreaStartCol >= 0 && printAreaEndCol >= 0) {
			wb.setPrintArea(TARGET_SHEET_INDEX, printAreaStartCol, printAreaEndCol, 0, currentRowNum);
		}
	}

	/**
	 * 文字列を帳票の設定より、行ごとのリストに変換
	 * 
	 * @param text                文字列
	 * @param paramType           変更対象文字列の種類（0:課名、1:関連条項名、2:行政回答内容）
	 * @param param               帳票設定パラメータ
	 * @param paragraphNum        段階番号
	 * @param paragraphNumOfGroup 段階番号（課ごと）
	 * @return
	 */
	private List<Map<String, String>> getOutputLines(String text, String paramType, ExportLedgerFormParam param, int paragraphNum,
			int paragraphNumOfGroup) {

		List<Map<String, String>> lineStrList = new ArrayList<>();

		// 段落番号をつけるかフラグ
		boolean paragraph = false;
		// 段落番号が文字列と同一セルに印字するかフラグ
		boolean paragraphWithinText = true;
		// 段落番号の書式文字
		String paragraphCharacter = EMPTY;
		// 段落番号の採番種類
		String ParagraphType = EMPTY;
		// 行当たり最大文字数
		int rowMaxCharacter = -1;
		// 空白行挿入フラグ
		String insertBlankLine = EMPTY;
		// 印字開始列
		int startCol = -1;

		switch (paramType) {
		case "0":
			// 編集文字列が課名の場合、以下ように設定
			paragraph = param.getDepartmentNameParagraph();
			paragraphWithinText = param.getDepartmentNameParagraphWithinText();
			paragraphCharacter = param.getDepartmentNameParagraphCharacter();
			ParagraphType = param.getDepartmentNameParagraphType();
			rowMaxCharacter = param.getDepartmentNameRowMaxCharacter();
			insertBlankLine = param.getDepartmentNameInsertBlankLine();
			startCol = param.getDepartmentNameStartCol();
			break;
		case "1":
			// 編集文字列が関連条項名の場合、以下ように設定
			paragraph = param.getJudgementTitleParagraph();
			paragraphWithinText = param.getJudgementTitleParagraphWithinText();
			paragraphCharacter = param.getJudgementTitleParagraphCharacter();
			ParagraphType = param.getJudgementTitleParagraphType();
			rowMaxCharacter = param.getJudgementTitleRowMaxCharacter();
			insertBlankLine = param.getJudgementTitleInsertBlankLine();
			startCol = param.getJudgementTitleStartCol();
			break;
		case "2":
			// 編集文字列が行政回答内容の場合、以下ように設定
			paragraph = param.getAnswerContentParagraph();
			paragraphWithinText = param.getAnswerContentParagraphWithinText();
			paragraphCharacter = param.getAnswerContentParagraphCharacter();
			ParagraphType = param.getAnswerContentParagraphType();
			rowMaxCharacter = param.getAnswerContentRowMaxCharacter();
			insertBlankLine = param.getAnswerContentInsertBlankLine();
			startCol = param.getAnswerContentStartCol();
			break;
		}

		// 印字開始列<０の場合、該当内容を出力しないため、空リストを戻り値として、中止する
		if (startCol < 0) {
			return lineStrList;
		}
		StringBuilder str = new StringBuilder();

		// 段落番号
		String paragraphNoStr = EMPTY;
		// 段落番号をつける
		if (paragraph) {
			if ("0".equals(ParagraphType)) {
				paragraphNoStr = Integer.valueOf(paragraphNum).toString();
			} else {
				paragraphNoStr = Integer.valueOf(paragraphNumOfGroup).toString();
			}
			// 段階番号は文字列と同じセルに印字する場合、
			if (paragraphWithinText) {
				// 段落番号
				str.append(paragraphNoStr);
				// 段落番号の書式文字
				str.append(paragraphCharacter);
				paragraphNoStr = EMPTY;
			}else {
				paragraphNoStr = paragraphNoStr + paragraphCharacter;
			}
		}
		// 文字列
		str.append(text);

		// 文字列を行の最大文字数で分割する
		List<String> strList = splitText(str.toString(), rowMaxCharacter);

		// 空白を挿入
		if ("before".equals(insertBlankLine)) {
			strList.add(0, EMPTY);
		} else if ("after".equals(insertBlankLine)) {
			strList.add(EMPTY);
		}

		for (int i = 0; i < strList.size(); i++) {
			Map<String, String> map = new HashMap<>();

			if (i == 0) {
				map.put("NO", paragraphNoStr);
			} else {
				map.put("NO", EMPTY);
			}

			map.put("TEXT", strList.get(i));

			lineStrList.add(map);
		}

		return lineStrList;
	}

	/**
	 * 文字列分割
	 * 
	 * @param text         文字列
	 * @param maxCharacter 分割用文字数
	 * @return
	 */
	public List<String> splitText(String text, int maxCharacter) {
		List<String> strList = new ArrayList<String>();
		// 最大文字数 ＞0 の場合、改行文字で分割して、最大文字数より、再度分割する
		if (maxCharacter > 0) {
			// 改行で分割
			String[] strList1 = text.split("[" + LF + CR + "]");
			// 分割した各子文字列は分割文字数で再度分割する
			for (int i = 0; i < strList1.length; i++) {
				String str = strList1[i];
				Matcher m = Pattern.compile("[\\s\\S]{1," + maxCharacter + "}").matcher(str);
				while (m.find()) {
					strList.add(m.group());
				}
			}
		} else {
			// 最大文字数 <= 0 の場合、改行しないで、一行とする

			// 改行文字を 空白に置換
			String str = text.replace(LF, EMPTY).replace(CR, EMPTY);
			strList.add(str);
		}

		return strList;
	}

	/**
	 * セルに値を設定
	 * 
	 * @param wb        ワークブック
	 * @param sheet     ワークシート
	 * @param rowNum    編集行番号
	 * @param paramType 編集内容の種類（0:課名、1:関連条項名、2:行政回答内容）
	 * @param param     帳票設定パラメータ
	 * @param text      値
	 * @throws Exception
	 */
	public void writeTextValue(Workbook wb, Sheet sheet, int rowNum, String paramType, ExportLedgerFormParam param,
			Map<String, String> valueMap) {

		// フォント名
		String fontName = param.getFontName();
		// フォントサイズ
		short fontSize = -1;
		// 印字開始列
		int startCol = -1;
		// 段落番号が文字列と同一セルに印字するかフラグ
		boolean paragraphWithinText = true;
		

		switch (paramType) {
		case "0":
			// 編集文字列が課名の場合、以下ように設定
			fontSize = param.getDepartmentNameFontSize();
			startCol = param.getDepartmentNameStartCol();
			paragraphWithinText = param.getDepartmentNameParagraphWithinText();
			break;
		case "1":
			// 編集文字列が関連条項名の場合、以下ように設定
			fontSize = param.getJudgementTitleFontSize();
			startCol = param.getJudgementTitleStartCol();
			paragraphWithinText = param.getJudgementTitleParagraphWithinText();
			break;
		case "2":
			// 編集文字列が行政回答内容の場合、以下ように設定
			fontSize = param.getAnswerContentFontSize();
			startCol = param.getAnswerContentStartCol();
			paragraphWithinText = param.getAnswerContentParagraphWithinText();
			break;
		}

		// 行作成
		Row row1 = sheet.getRow(rowNum);
		if (row1 == null) {
			row1 = sheet.createRow(rowNum);
		}
		// セル作成(テキスト)
		Cell cell = row1.getCell(startCol);
		if (cell == null) {
			cell = row1.createCell(startCol);
		}

		// 段階番号
		String no = valueMap.get("NO");
		// テキスト
		String text = valueMap.get("TEXT");
		
		// 値設定
		cell.setCellValue(text);
		
		//段階番号がテキストと同一セルに印字することではない場合、
		if(!paragraphWithinText) {
			// 印字セル作成(段階番号)
			Cell cell2 = row1.getCell(startCol-1);
			if (cell2 == null) {
				cell2 = row1.createCell(startCol-1);
			}
			// 値設定
			cell2.setCellValue(no+"");
		}

		// フォント名 または、フォントサイズを設定している場合、編集中セルに、スタイルをつける
		if (!EMPTY.equals(fontName) || Short.compare(fontSize, (short) 0) > 0) {

			// フォントオブジェクト生成
			Font font = wb.createFont();
			// フォント
			if (!EMPTY.equals(fontName)) {
				font.setFontName(fontName);
			}
			// フォントサイズ
			if (Short.compare(fontSize, (short) 0) > 0) {
				font.setFontHeightInPoints(fontSize);
			}

			// セルに生成したフォントを設定する
			CellStyle cs = wb.createCellStyle();
			cs.setFont(font);
			cell.setCellStyle(cs);
			//段階番号がテキストと同一セルに印字することではない場合、
			if(!paragraphWithinText) {
				Cell cell2 = row1.getCell(startCol-1);
				cell2.setCellStyle(cs);
			}
		}
	}

	/**
	 * 改ページ設定要否
	 * 
	 * @param param          帳票設定パラメータ
	 * @param currentPageNum 処理中ページ数（編集部分から）
	 * @param lines          編集済み行数
	 */
	private boolean isSetRowBreak(ExportLedgerFormParam param, int currentPageNum, int lines) {

		// ページ当たりの最大行数（ひな形以外の1ページ目）
		int firstPageMaxRow = param.getFirstPageMaxRow();
		// ページ当たりの最大行数（ひな形以外の2ページ目以降）
		int pageMaxRow = param.getFirstPageMaxRow();

		boolean isRowBreak = false;
		if (currentPageNum > 1) {
			if (lines >= pageMaxRow) {
				isRowBreak = true;
			}
		} else {
			if (lines >= firstPageMaxRow) {
				isRowBreak = true;
			}
		}

		return isRowBreak;
	}
}
