package developmentpermission.dao;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import developmentpermission.entity.Answer;
import developmentpermission.entity.ApplicantInformation;
import developmentpermission.entity.Application;
import developmentpermission.entity.ApplicationAnswerSearchResult;
import developmentpermission.entity.ApplicationCategory;
import developmentpermission.entity.ApplicationCategoryMaster;
import developmentpermission.entity.ApplicationCategorySelectionView;
import developmentpermission.entity.ApplicationFile;
import developmentpermission.entity.ApplicationFileMaster;
import developmentpermission.entity.ApplicationLotNumber;
import developmentpermission.entity.ApplicationVersionInformation;
import developmentpermission.entity.ApplyLotNumber;
import developmentpermission.entity.CategoryJudgement;
import developmentpermission.entity.Department;
import developmentpermission.entity.DepartmentAnswer;
import developmentpermission.entity.LotNumberAndDistrict;
import developmentpermission.form.AnswerNameForm;
import developmentpermission.form.ApplicantInformationItemForm;
import developmentpermission.form.ApplicationCategoryForm;
import developmentpermission.form.ApplicationCategorySelectionViewForm;
import developmentpermission.form.ApplicationSearchConditionForm;
import developmentpermission.form.ApplicationStepForm;
import developmentpermission.form.ApplicationTypeForm;
import developmentpermission.form.DepartmentForm;
import developmentpermission.form.ItemAnswerStatusForm;
import developmentpermission.form.StatusForm;

/**
 * O_申請DAO
 */
@Transactional
public class ApplicationDao extends AbstractDao {

	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationDao.class);

	/** 項目1の申請者情報項目ID */
	public static final String ITEM_1_ID = "1001";
	/** 項目2の申請者情報項目ID */
	public static final String ITEM_2_ID = "1002";
	/** 項目3の申請者情報項目ID */
	public static final String ITEM_3_ID = "1003";
	/** 項目4の申請者情報項目ID */
	public static final String ITEM_4_ID = "1004";
	/** 項目5の申請者情報項目ID */
	public static final String ITEM_5_ID = "1005";
	/** 項目6の申請者情報項目ID */
	public static final String ITEM_6_ID = "1006";
	/** 項目7の申請者情報項目ID */
	public static final String ITEM_7_ID = "1007";
	/** 項目8の申請者情報項目ID */
	public static final String ITEM_8_ID = "1008";
	/** 項目9の申請者情報項目ID */
	public static final String ITEM_9_ID = "1009";
	/** 項目10の申請者情報項目ID */
	public static final String ITEM_10_ID = "1010";

	/** 項目タイプ: テキスト */
	public static final String ITEM_TYPE_TEXT = "0";

	/** 項目タイプ: テキストエリア */
	public static final String ITEM_TYPE_TEXT_AREA = "1";

	/** 項目タイプ：日付 */
	public static final String ITEM_TYPE_DATE = "2";

	/** 項目タイプ：数値 */
	public static final String ITEM_TYPE_NUMBER = "3";

	/** 項目タイプ：ドロップダウン単一選択 */
	public static final String ITEM_TYPE_SINGLE_SELECT = "4";

	/** 項目タイプ：ドロップダウン複数選択 */
	public static final String ITEM_TYPE_MULTI_SELECT = "5";

	/**
	 * コンストラクタ
	 * 
	 * @param emf Entityマネージャファクトリ
	 */
	public ApplicationDao(EntityManagerFactory emf) {
		super(emf);
	}

	/**
	 * 申請情報検索
	 * 
	 * @param paramForm 検索条件
	 * @return 検索結果
	 */
	@SuppressWarnings("unchecked")
	public List<Application> searchApplication(ApplicationSearchConditionForm paramForm) {
		LOGGER.debug("申請情報検索 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();

			// 申請者情報一覧(複数可)
			List<ApplicantInformationItemForm> applicantList = paramForm.getApplicantInformationItemForm();
			// 申請区分選択一覧(複数可)
			List<ApplicationCategorySelectionViewForm> categorySelectionViewList = paramForm.getApplicationCategories();
			// ステータス(複数不可)
			List<StatusForm> statusList = paramForm.getStatus();
			// 部署(複数不可)
			List<DepartmentForm> departmentList = paramForm.getDepartment();
			// 回答者（複数不可）
			List<AnswerNameForm> answerName = paramForm.getAnswerName();
			// 申請種類（複数不可）
			List<ApplicationTypeForm> applicationTypes = paramForm.getApplicationTypes();
			// 申請段階（複数不可）
			List<ApplicationStepForm> applicationSteps = paramForm.getApplicationSteps();
			// 申請追加情報（複数可）
			List<ApplicantInformationItemForm> applicantAddInformationItemList = paramForm
					.getApplicantAddInformationItemForm();
			// 条文ステータス（複数可）
			List<ItemAnswerStatusForm> itemAnswerStatus = paramForm.getItemAnswerStatus();
			// 申請ID
			Integer applicationId = paramForm.getApplicationId();
			// WHERE句構築
			StringBuffer where = new StringBuffer();
			// 申請追加情報JOIN句構築
			StringBuffer addInfoJoin = new StringBuffer();
			// 登録ステータス(申請済のデータのみを許可)
			appendWhereText(where);
			where.append("oa.register_status='1' ");

			// 申請者情報検索パラメータ
			Map<Integer, String> applicantConditionParam = new LinkedHashMap<Integer, String>();
			if (applicantList != null) {
				// 申請者情報条件はvalueの部分一致検索
				for (ApplicantInformationItemForm applicant : applicantList) {
					String applicantInfoId = applicant.getId();
					String value = applicant.getValue();
					Integer applicantNum = null;
					if (applicantInfoId != null && value != null && !"".equals(value)) {
						switch (applicantInfoId) {
						case ITEM_1_ID:
							applicantNum = 1;
							break;
						case ITEM_2_ID:
							applicantNum = 2;
							break;
						case ITEM_3_ID:
							applicantNum = 3;
							break;
						case ITEM_4_ID:
							applicantNum = 4;
							break;
						case ITEM_5_ID:
							applicantNum = 5;
							break;
						case ITEM_6_ID:
							applicantNum = 6;
							break;
						case ITEM_7_ID:
							applicantNum = 7;
							break;
						case ITEM_8_ID:
							applicantNum = 8;
							break;
						case ITEM_9_ID:
							applicantNum = 9;
							break;
						case ITEM_10_ID:
							applicantNum = 10;
							break;
						default:
							// 想定外のIDなので条件に設定しない
							LOGGER.warn("未対応の申請者情報項目ID: " + applicantInfoId);
							break;
						}
					}

					if (applicantNum != null) {
						if (!applicantConditionParam.containsKey(applicantNum)) {
							// パラメータをセットできるのは各申請者情報項目ID毎に1つのみ
							appendWhereText(where);
							where.append("oai.item_" + applicantNum + " LIKE CONCAT('%', :applicantId_" + applicantNum
									+ ", '%') ");
							applicantConditionParam.put(applicantNum, value);
						}
					}
				}
			}

			// 申請区分検索パラメータ
			Map<String, List<String>> categoryConditionParam = new LinkedHashMap<String, List<String>>();
			if (categorySelectionViewList != null) {
				// 画面ID別に申請区分IDを集約
				for (ApplicationCategorySelectionViewForm categorySelectionView : categorySelectionViewList) {
					List<ApplicationCategoryForm> categoryList = categorySelectionView.getApplicationCategory();
					if (categoryList != null) {
						for (ApplicationCategoryForm category : categoryList) {
							String categoryId = category.getId();
							String screenId = category.getScreenId();
							if (categoryId != null && !"".equals(categoryId) && screenId != null
									&& !"".equals(screenId)) {
								if (!categoryConditionParam.containsKey(screenId)) {
									categoryConditionParam.put(screenId, new ArrayList<String>());
								}

								List<String> conditionList = categoryConditionParam.get(screenId);
								if (!conditionList.contains(categoryId)) {
									conditionList.add(categoryId);
								}
							}
						}
					}
				}

				// 申請区分はscreenIdが同じもの同士についてはOR条件、違うもの同士についてはAND条件でセット
				int idx = 0;
				for (Map.Entry<String, List<String>> entry : categoryConditionParam.entrySet()) {
					if (idx == 0) {
						appendWhereText(where);
					} else {
						where.append("OR ");
					}
					where.append("(oac.view_id = :viewId_" + entry.getKey() + " AND oac.category_id IN (:categoryId_"
							+ entry.getKey() + ") ) ");
					idx++;
				}
			}

			// ステータス
			String statusParam = null;
			if (statusList != null && statusList.size() > 0) {
				// ステータスは1件のみ
				StatusForm statusForm = statusList.get(0);
				String status = statusForm.getValue();
				if (status != null && !"".equals(status)) {
					appendWhereText(where);
					where.append("oa.status = :status ");
					statusParam = status;
				}
			}

			// 部署
			String departmentParam = null;
			if (departmentList != null && departmentList.size() > 0) {
				// 部署は1件のみ
				DepartmentForm departmentForm = departmentList.get(0);
				String departmentId = departmentForm.getDepartmentId();
				if (departmentId != null && !"".equals(departmentId)) {
					appendWhereText(where);
					// 事前相談・許可判定の場合、M_区分判定_権限.部署ID を参照 事前相談の場合O_回答.部署IDを参照
					where.append("(ans.department_id = :departmentId OR mja.department_id = :departmentId )");
					departmentParam = departmentId;
				}
			}
			// 回答者
			String answerUserParam = null;
			if (answerName != null && answerName.size() > 0) {
				AnswerNameForm answerNameForm = answerName.get(0);
				String userId = answerNameForm.getUserId();
				if (userId != null && !"".equals(userId)) {
					appendWhereText(where);
					where.append("anh.answer_user_id = :userId ");
					answerUserParam = userId;
				}
			}
			// 申請種類
			Integer applicationTypeParam = null;
			if (applicationTypes != null && applicationTypes.size() > 0) {
				ApplicationTypeForm applicationTypeForm = applicationTypes.get(0);
				Integer applicationTypeId = applicationTypeForm.getApplicationTypeId();
				if (applicationTypeId != null) {
					appendWhereText(where);
					where.append("oa.application_type_id = :applicationTypeId ");
					applicationTypeParam = applicationTypeId;
				}
			}
			// 申請段階
			Integer appicationStepParam = null;
			if (applicationSteps != null && applicationSteps.size() > 0) {
				ApplicationStepForm applicationStepForm = applicationSteps.get(0);
				Integer applicationStepId = applicationStepForm.getApplicationStepId();
				if (applicationStepId != null) {
					appendWhereText(where);
					where.append("oavi.application_step_id = :applicationStepId ");
					appicationStepParam = applicationStepId;
				}
			}
			// 申請追加情報
			// 申請追加情報 検索パラメータ
			Map<String, List<String>> applicantAddInformationConditionParam = new LinkedHashMap<String, List<String>>();
			// 項目IDと 項目型のMap
			Map<String, String> itemIdTypeMap = new LinkedHashMap<String, String>();
			if (applicantAddInformationItemList != null) {
				// 申請情報項目ID毎に項目値を集約
				for (ApplicantInformationItemForm applicantAddInformationForm : applicantAddInformationItemList) {
					// 申請情報項目ID
					String applicantInformationId = applicantAddInformationForm.getId();
					// 項目型
					String itemType = applicantAddInformationForm.getItemType();
					// 項目値
					String itemValue = applicantAddInformationForm.getValue();
					if (applicantInformationId != null && !"".equals(applicantInformationId) && itemType != null
							&& !"".equals(itemType) && itemValue != null && !"".equals(itemValue)) {
						if (!applicantAddInformationConditionParam.containsKey(applicantInformationId)) {
							applicantAddInformationConditionParam.put(applicantInformationId, new ArrayList<String>());
							itemIdTypeMap.put(applicantInformationId, itemType);
						}
					}
					List<String> conditionList = applicantAddInformationConditionParam.get(applicantInformationId);
					if (!conditionList.contains(itemValue)) {
						conditionList.add(itemValue);
					}
				}
			}
			// 同じ申請情報項目IDはOR条件、異なる申請情報項目はAND条件で検索する
			// int idx = 0;
			for (Map.Entry<String, List<String>> entry : applicantAddInformationConditionParam.entrySet()) {

				String itemType = itemIdTypeMap.containsKey(entry.getKey()) ? itemIdTypeMap.get(entry.getKey()) : null;
				if (itemType != null) {
					if (itemType.equals(ITEM_TYPE_TEXT) || itemType.equals(ITEM_TYPE_TEXT_AREA)) {
						// テキスト、テキストリア LIKE検索
						for (String aValue : entry.getValue()) {
							appendWhereText(where);
							where.append("(oaia.applicant_information_item_id = :applicantInformationItemId_"
									+ entry.getKey() + "_" + aValue
									+ " AND oaia.item_value LIKE CONCAT('%', :itemValue_" + entry.getKey() + "_"
									+ aValue + ", '%')) ");
						}
					} else if (itemType.equals(ITEM_TYPE_DATE) || itemType.equals(ITEM_TYPE_NUMBER)
							|| itemType.equals(ITEM_TYPE_SINGLE_SELECT)) {
						// 日付、数値、単一選択 完全一致検索
						appendWhereText(where);
						where.append("(oaia.applicant_information_item_id = :applicantInformationItemId_"
								+ entry.getKey() + " AND oaia.item_value IN (:itemValue_" + entry.getKey() + ") ) ");
					} else if ((itemType.equals(ITEM_TYPE_MULTI_SELECT))) {
						// 複数選択 カンマ区切り文字列から一致検索（OR）
						appendWhereText(where);
						int idx = 0;
						for (String aValue : entry.getValue()) {
							if (idx == 0) {
								where.append("(");
							} else {
								where.append("OR ");
							}
							where.append("(oaia.applicant_information_item_id = :applicantInformationItemId_"
									+ entry.getKey() + "_" + aValue
									+ " AND oaia.item_value LIKE CONCAT('%', :itemValue_" + entry.getKey() + "_"
									+ aValue + ", '%')) ");
							idx++;
						}
						where.append(")");
					}

				}

			}
			// 条文ステータス OR検索
			List<String> itemAnswerStatusParam = null;
			if (itemAnswerStatus != null) {
				itemAnswerStatusParam = new ArrayList<String>();
				for (ItemAnswerStatusForm answerStatus : itemAnswerStatus) {
					String[] tmpStatusList = answerStatus.getValue() != null ? answerStatus.getValue().split(",")
							: null;
					if (tmpStatusList != null) {
						for (String aStatus : tmpStatusList) {
							itemAnswerStatusParam.add(aStatus);
						}
					}
				}
				if (itemAnswerStatusParam.size() > 0) {
					appendWhereText(where);
					where.append("ans.answer_status IN  :answerStatus ");
					appendWhereText(where);
					where.append("(ans.delete_flag <> '1' OR ans.delete_unnotified_flag <> '1')");
				}
			}
			// 申請ID
			if (applicationId != null) {
				appendWhereText(where);
				where.append("oa.application_id = :applicationId ");
			}

			String sql = "" + //
			// 最大申請段階ID（最新のステータス）
					"WITH max_application_step_id AS (" + //
					"  SELECT " + //
					"    application_id, " + //
					"    MAX(application_step_id) AS max_application_id " + //
					"  FROM " + //
					"    o_application_version_information " + //
					"  WHERE register_status = '1' OR (register_status = '0' AND version_information > 1) " +// 	仮申請データを除く
					"  GROUP BY application_id" + //
					" ) " + //
					"SELECT DISTINCT " + //
					"  oa.application_id AS application_id, " + //
					"  oa.applicant_id AS applicant_id, " + //
					"  oa.status AS status, " + //
					"  oa.register_status AS register_status, " + //
					"  oa.collation_text AS collation_text, " + //
					"  oa.register_datetime AS register_datetime, " + //
					"  oa.update_datetime AS update_datetime, " + //
					"  oa.application_type_id AS application_type_id " + //
					"FROM " + // O_申請
					"  o_application AS oa " + //
					"LEFT OUTER JOIN " + // O_申請者情報
					"  o_applicant_information AS oai " + //
					"ON " + //
					"  oa.application_id = oai.application_id " + //
					"LEFT OUTER JOIN " + //
					"  max_application_step_id AS maspi " + // 最大申請段階ID
					"ON " + //
					"  oa.application_id = maspi.application_id " + //
					"LEFT OUTER JOIN " + // O_申請版情報
					"  o_application_version_information AS oavi " + //
					"ON " + //
					"  oa.application_id = oavi.application_id " + //
					"  AND  " + //
					"  maspi.application_id = oavi.application_id" + //
					"  AND  " + //
					"  maspi.max_application_id = oavi.application_step_id " + //
					"LEFT OUTER JOIN " + // O_申請区分
					"  o_application_category AS oac " + //
					"ON " + //
					"  oavi.application_id = oac.application_id " + //
					"AND " + //
					"  oavi.application_step_id = oac.application_step_id " + //
					"AND " + //
					"  oac.version_information = (CASE WHEN oavi.register_status = '0'  THEN  oavi.version_information - 1 ELSE oavi.version_information END)" + // 仮申請データを避けるため、登録済みの版情報を利用する
					"LEFT OUTER JOIN " + // O_回答
					"  o_answer AS ans " + //
					"ON " + //
					"  oa.application_id = ans.application_id " + //
					"AND " + //
					"  oavi.application_step_id = ans.application_step_id " + //
					"LEFT OUTER JOIN " + // M_区分判定
					"  m_category_judgement AS mcj " + //
					"ON " + //
					"  ans.judgement_id = mcj.judgement_item_id " + //
					"LEFT OUTER JOIN " + // M_区分判定_権限
					"  m_judgement_authority AS mja " + //
					"ON " + //
					"  mja.judgement_item_id = mcj.judgement_item_id " + //
					"LEFT OUTER JOIN " + // O_回答履歴
					" o_answer_history AS anh " + //
					"ON " + //
					"  ans.answer_id = anh.answer_id " + //
					"LEFT OUTER JOIN " + // O_申請追加情報
					"  o_applicant_information_add AS oaia " + //
					"ON " + //
					"  oavi.application_id = oaia.application_id " + //
					"AND " + //
					"  oavi.application_step_id = oaia.application_step_id " + //
					"AND " + //
                    "  oaia.version_information = (CASE WHEN oavi.register_status = '0'  THEN  oavi.version_information - 1 ELSE oavi.version_information END)" + //仮申請データを避けるため、登録済みの版情報を利用する
					where + // where句
					"ORDER BY " + //
					"  oa.application_id ASC ";
			LOGGER.debug(sql);
			Query query = em.createNativeQuery(sql, Application.class);
			// 申請者情報パラメータ設定
			for (Map.Entry<Integer, String> entry : applicantConditionParam.entrySet()) {
				query = query.setParameter("applicantId_" + entry.getKey(), entry.getValue());
			}
			// 申請区分パラメータ設定
			for (Map.Entry<String, List<String>> entry : categoryConditionParam.entrySet()) {
				query = query.setParameter("viewId_" + entry.getKey(), entry.getKey());
				query = query.setParameter("categoryId_" + entry.getKey(), entry.getValue());
			}
			// ステータスパラメータ設定
			if (statusParam != null) {
				query = query.setParameter("status", statusParam);
			}
			// 部署パラメータ設定
			if (departmentParam != null) {
				query = query.setParameter("departmentId", departmentParam);
			}
			// 回答者パラメータ設定
			if (answerUserParam != null) {
				query = query.setParameter("userId", answerUserParam);
			}
			// 申請種類パラメータ設定
			if (applicationTypeParam != null) {
				query = query.setParameter("applicationTypeId", applicationTypeParam);
			}
			// 申請段階パラメータ設定
			if (appicationStepParam != null) {
				query = query.setParameter("applicationStepId", appicationStepParam);
			}
			// 申請追加情報パラメータ設定
			for (Map.Entry<String, List<String>> entry : applicantAddInformationConditionParam.entrySet()) {
				String itemType = itemIdTypeMap.containsKey(entry.getKey()) ? itemIdTypeMap.get(entry.getKey()) : null;
				if (itemType != null) {
					if (itemType.equals(ITEM_TYPE_TEXT) || itemType.equals(ITEM_TYPE_TEXT_AREA)) {
						// テキスト、テキストリア LIKE検索
						for (String aValue : entry.getValue()) {
							query = query.setParameter("applicantInformationItemId_" + entry.getKey() + "_" + aValue,
									entry.getKey());
							query = query.setParameter("itemValue_" + entry.getKey() + "_" + aValue, aValue);
						}
					} else if (itemType.equals(ITEM_TYPE_DATE) || itemType.equals(ITEM_TYPE_NUMBER)
							|| itemType.equals(ITEM_TYPE_SINGLE_SELECT)) {
						// 日付、数値、単一選択 完全一致検索
						query = query.setParameter("applicantInformationItemId_" + entry.getKey(), entry.getKey());
						query = query.setParameter("itemValue_" + entry.getKey(), entry.getValue());
					} else if ((itemType.equals(ITEM_TYPE_MULTI_SELECT))) {
						// 複数選択 カンマ区切り文字列から一致検索
						for (String aValue : entry.getValue()) {
							query = query.setParameter("applicantInformationItemId_" + entry.getKey() + "_" + aValue,
									entry.getKey());
							query = query.setParameter("itemValue_" + entry.getKey() + "_" + aValue, aValue);
						}
					}
				}
			}
			// 条文ステータスパラメータ設定
			if (itemAnswerStatusParam != null && itemAnswerStatusParam.size() > 0) {
				query = query.setParameter("answerStatus", itemAnswerStatusParam);
			}
			// 申請IDパラメータ設定
			if (applicationId  != null) {
				query = query.setParameter("applicationId", applicationId);
			}

			return query.getResultList();
		} finally {
			LOGGER.debug("申請情報検索 終了");
			if (em != null) {
				em.close();
			}
		}
	}

	/**
	 * O_申請区分検索
	 * 
	 * @param applicationId 申請ID
	 * @return O_申請区分リスト
	 */
	@SuppressWarnings("unchecked")
	public List<ApplicationCategory> getApplicationCategoryList(int applicationId) {
		LOGGER.debug("O_申請区分検索 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();

			String sql = "" + //
					"SELECT DISTINCT " + //
					"  application_id, " + //
					"  view_id, " + //
					"  category_id, " + //
					"  application_step_id, " + //
					"  version_information " + //
					"FROM " + //
					"  o_application_category " + //
					"WHERE " + //
					"  application_id = :applicationId " + //
					"ORDER BY " + //
					"  application_id ASC ";

			Query query = em.createNativeQuery(sql, ApplicationCategory.class);
			query = query.setParameter("applicationId", applicationId);
			return query.getResultList();
		} finally {
			LOGGER.debug("O_申請区分検索 終了");
			if (em != null) {
				em.close();
			}
		}
	}

	/**
	 * O_申請区分検索（申請段階ID、版情報指定）
	 * 
	 * @param applicationId 申請ID
	 * @return O_申請区分リスト
	 */
	@SuppressWarnings("unchecked")
	public List<ApplicationCategory> getApplicationCategoryList(int applicationId, int applicationStepId,
			int versionInformation) {
		LOGGER.debug("O_申請区分検索 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();

			String sql = "" + //
					"SELECT DISTINCT " + //
					"  application_id, " + //
					"  view_id, " + //
					"  category_id, " + //
					"  application_step_id, " + //
					"  version_information " + //
					"FROM " + //
					"  o_application_category " + //
					"WHERE " + //
					"  application_id = :applicationId " + //
					"AND  application_step_id = :applicationStepId " + //
					"AND  version_information = :versionInformation " + //
					"ORDER BY " + //
					"  application_id ASC ";

			Query query = em.createNativeQuery(sql, ApplicationCategory.class);
			query = query.setParameter("applicationId", applicationId);
			query = query.setParameter("applicationStepId", applicationStepId);
			query = query.setParameter("versionInformation", versionInformation);
			return query.getResultList();
		} finally {
			LOGGER.debug("O_申請区分検索 終了");
			if (em != null) {
				em.close();
			}
		}
	}

	/**
	 * M_申請区分検索
	 * 
	 * @param applicationId 申請ID
	 * @return M_申請区分リスト
	 */
	@SuppressWarnings("unchecked")
	public List<ApplicationCategoryMaster> getApplicationCategoryMasterList(int applicationId) {
		LOGGER.debug("M_申請区分検索 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();

			String sql = "" + //
					"SELECT DISTINCT " + //
					"  b.category_id AS category_id, " + //
					"  b.view_id AS view_id, " + //
					"  b.order AS \"order\", " + //
					"  b.label_name AS label_name " + //
					"FROM " + //
					"  o_application_category AS a " + //
					"INNER JOIN " + //
					"  m_application_category AS b " + //
					"ON " + //
					"  a.view_id = b.view_id " + //
					"  AND a.category_id = b.category_id " + //
					"WHERE " + //
					"  application_id = :applicationId " + //
					"ORDER BY " + //
					"  b.order ASC ";

			Query query = em.createNativeQuery(sql, ApplicationCategoryMaster.class);
			query = query.setParameter("applicationId", applicationId);
			return query.getResultList();
		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("M_申請区分検索 終了");
		}
	}

	/**
	 * M_申請区分検索
	 * 
	 * @param applicationId      申請ID
	 * @param applicationStepId  申請段階ID
	 * @param versionInformation 申請版情報
	 * @return M_申請区分リスト
	 */
	@SuppressWarnings("unchecked")
	public List<ApplicationCategoryMaster> getApplicationCategoryMasterList(int applicationId, int applicationStepId,
			int versionInformation) {
		LOGGER.debug("M_申請区分検索 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();

			String sql = "" + //
					"SELECT DISTINCT " + //
					"  b.category_id AS category_id, " + //
					"  b.view_id AS view_id, " + //
					"  b.order AS \"order\", " + //
					"  b.label_name AS label_name " + //
					"FROM " + //
					"  o_application_category AS a " + //
					"INNER JOIN " + //
					"  m_application_category AS b " + //
					"ON " + //
					"  a.view_id = b.view_id " + //
					"  AND a.category_id = b.category_id " + //
					"WHERE " + //
					"  a.application_id = :applicationId " + //
					"  AND a.application_step_id = :applicationStepId " + //
					"  AND a.version_information = :versionInformation " + //
					"ORDER BY " + //
					"  b.order ASC ";

			Query query = em.createNativeQuery(sql, ApplicationCategoryMaster.class);
			query = query.setParameter("applicationId", applicationId);
			query = query.setParameter("applicationStepId", applicationStepId);
			query = query.setParameter("versionInformation", versionInformation);
			return query.getResultList();
		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("M_申請区分検索 終了");
		}
	}

	/**
	 * M_申請区分検索
	 * 
	 * @param applicationId      申請ID
	 * @param viewId             画面ID
	 * @param applicationStepId  申請段階ID
	 * @param versionInformation 申請版情報
	 * @return M_申請区分リスト
	 */
	@SuppressWarnings("unchecked")
	public List<ApplicationCategoryMaster> getApplicationCategoryMasterList(int applicationId, String viewId,
			int applicationStepId, int versionInformation) {
		LOGGER.debug("M_申請区分検索 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();

			String sql = "" + //
					"SELECT DISTINCT " + //
					"  b.category_id AS category_id, " + //
					"  b.view_id AS view_id, " + //
					"  b.order AS \"order\", " + //
					"  b.label_name AS label_name " + //
					"FROM " + //
					"  o_application_category AS a " + //
					"INNER JOIN " + //
					"  m_application_category AS b " + //
					"ON " + //
					"  a.view_id = b.view_id " + //
					"  AND a.category_id = b.category_id " + //
					"WHERE " + //
					"  a.application_id = :applicationId " + //
					"  AND a.view_id = :viewId " + //
					"  AND a.application_step_id = :applicationStepId " + //
					"  AND a.version_information = :versionInformation " + //
					"ORDER BY " + //
					"  b.order ASC ";

			Query query = em.createNativeQuery(sql, ApplicationCategoryMaster.class);
			query = query.setParameter("applicationId", applicationId);
			query = query.setParameter("viewId", viewId);
			query = query.setParameter("applicationStepId", applicationStepId);
			query = query.setParameter("versionInformation", versionInformation);
			return query.getResultList();
		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("M_申請区分検索 終了");
		}
	}

	/**
	 * O_申請者情報検索
	 * 
	 * @param applicationId 申請ID
	 * @return O_申請者情報リスト
	 */
	@SuppressWarnings("unchecked")
	public List<ApplicantInformation> getApplicantInformationList(int applicationId) {
		LOGGER.debug("O_申請者情報検索 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();

			String sql = "" + //
					"SELECT DISTINCT " + //
					"  application_id, " + //
					"  applicant_id, " + //
					"  item_1, " + //
					"  item_2, " + //
					"  item_3, " + //
					"  item_4, " + //
					"  item_5, " + //
					"  item_6, " + //
					"  item_7, " + //
					"  item_8, " + //
					"  item_9, " + //
					"  item_10, " + //
					"  mail_address, " + //
					"  collation_id, " + //
					"  password, " + //
					"  contact_address_flag " + //
					"FROM " + //
					"  o_applicant_information " + //
					"WHERE " + //
					"  application_id = :applicationId " + //
					"ORDER BY " + //
					"  applicant_id ASC";

			Query query = em.createNativeQuery(sql, ApplicantInformation.class);
			query = query.setParameter("applicationId", applicationId);
			return query.getResultList();
		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("O_申請者情報検索 終了");
		}
	}

	/**
	 * O_回答検索
	 * 
	 * @param applicationId      申請ID
	 * @param isGoverment        行政かどうか
	 * @param applicationStepId  申請段階ID
	 * @param departmentAnswerId 部署回答ID
	 * @return O_回答リスト
	 */
	@SuppressWarnings("unchecked")
	public List<Answer> getAnswerList(int applicationId, boolean isGoverment, int applicationStepId,
			Integer departmentAnswerId) {
		LOGGER.debug("O_回答検索 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();

			String sql = "";
			if (isGoverment) {
				sql += "" + //
						"SELECT " + //
						"  answer_id, " + //
						"  application_id, " + //
						"  judgement_id, " + //
						"  judgement_result_index," + //
						"  judgement_result, " + //
						"  answer_content, " + //
						"  notified_text, " + //
						"  register_datetime, " + //
						"  update_datetime, " + //
						"  complete_flag, " + //
						"  notified_flag, " + //
						"  re_application_flag, " + //
						"  business_reapplication_flag, " + //
						"  application_step_id, " + //
						"  department_id, " + //
						"  discussion_flag, " + //
						"  discussion_item, " + //
						"  business_pass_status, " + //
						"  business_pass_comment, " + //
						"  business_answer_datetime, " + //
						"  government_confirm_status, " + //
						"  government_confirm_datetime, " + //
						"  government_confirm_comment, " + //
						"  government_confirm_notified_flag, " + //
						"  permission_judgement_result, " + //
						"  answer_status, " + //
						"  answer_data_type, " + //
						"  register_status, " + //
						"  delete_unnotified_flag, " + //
						"  deadline_datetime, " + //
						"  department_answer_id, " + //
						"  answer_update_flag, " + //
						"  answer_permission_flag, " + //
						"  government_confirm_permission_flag, " + //
						"  permission_judgement_migration_flag, " + //
						"  version_information " + //
						"FROM " + //
						"  o_answer " + //
						"WHERE " + //
						"  application_id = :applicationId " + //
						"  AND application_step_id = :applicationStepId " + //
						"  AND delete_flag = '0' " + // 削除済みの回答を除く
						"  AND register_status = '1' ";

				// 事前協議の場合、部署回答IDを検索条件に追記
				if (applicationStepId == 2 && departmentAnswerId > 0) {
					sql += "AND department_answer_id = :departmentAnswerId ";
				}
			}
			// 事業者の場合は通知フラグがtrueのデータのみ取得、かつ 内容が回答履歴から取得
			if (!isGoverment) {
				sql += "" + //
						"WITH max_answer_history AS ( " + //
						"    SELECT " + //
						"        a.answer_id, " + //
						"        a.answer_user_id, " + //
						"        a.answer_text, " + //
						"        a.answer_datetime, " + //
						"        a.notify_flag, " + //
						"        a.discussion_flag, " + //
						"        a.discussion_item, " + //
						"        a.business_pass_status, " + //
						"        a.business_pass_comment, " + //
						"        a.government_confirm_status, " + //
						"        a.government_confirm_datetime, " + //
						"        a.government_confirm_comment, " + //
						"        a.re_application_flag, " + //
						"        a.permission_judgement_result, " + //
						"        a.answer_status, " + //
						"        a.answer_data_type, " + //
						"        a.update_datetime, " + //
						"        a.deadline_datetime " + //
						"    FROM " + //
						"        o_answer_history AS a " + //
						"        INNER JOIN ( " + //
						"            SELECT " + //
						"                answer_id, " + //
						"                MAX(answer_datetime) AS max_v " + //
						"            FROM " + //
						"                o_answer_history " + //
						"            WHERE " + //
						"                notify_flag = '1' " + //
						"            GROUP BY " + //
						"                answer_id " + //
						"        ) AS b " + //
						"            ON a.answer_id = b.answer_id " + //
						"            AND a.answer_datetime = b.max_v " + //
						") " + //
						"SELECT " + //
						"    a.answer_id, " + //
						"    a.application_id, " + //
						"    a.judgement_id, " + //
						"    a.judgement_result, " + //
						"    a.judgement_result_index, " + //
						"    a.answer_content, " + //
						"    a.notified_text AS notified_text, " + //
						"    a.register_datetime, " + //
						"    a.update_datetime AS update_datetime, " + //
						"    a.complete_flag AS complete_flag, " + //
						"    max_ah.notify_flag AS notified_flag, " + //
						"    a.re_application_flag, " + //
						"    a.business_reapplication_flag, " + //
						"    a.application_step_id, " + //
						"    a.department_id, " + //
						"    max_ah.discussion_flag, " + //
						"    max_ah.discussion_item, " + //
						"    a.business_pass_status, " + //
						"    max_ah.business_pass_comment, " + //
						"    a.business_answer_datetime, " + //
						"    max_ah.government_confirm_status, " + //
						"    max_ah.government_confirm_datetime, " + //
						"    max_ah.government_confirm_comment, " + //
						"    a.government_confirm_notified_flag, " + //
						"    max_ah.permission_judgement_result, " + //
						"    max_ah.answer_status, " + //
						"    max_ah.answer_data_type, " + //
						"    a.register_status, " + //
						"    a.delete_unnotified_flag, " + //
						"    a.deadline_datetime, " + //
						"    a.department_answer_id," + //
						"    a.answer_update_flag, " + //
						"    a.answer_permission_flag, " + //
						"    a.government_confirm_permission_flag, " + //
						"    a.permission_judgement_migration_flag, " + //
						"    a.version_information " + //
						"FROM " + //
						"    o_answer AS a " + //
						"    INNER JOIN max_answer_history AS max_ah " + //
						"        ON a.answer_id = max_ah.answer_id " + //
						"WHERE " + //
						"    a.application_id = :applicationId " + //
						"    AND a.application_step_id = :applicationStepId " + //
						"    AND a.notified_flag = '1' " + // 通知済み
						"    AND a.delete_flag = '0' " + // 未削除
						"    AND a.register_status = '1' " + // 登録ステータス
						"    AND a.permission_judgement_migration_flag = '0' "; //許可判定移行フラグ：1チェックしないのレコードが除く

				// 事前協議の場合、部署回答IDを検索条件に追記
				if (applicationStepId == 2 && departmentAnswerId > 0) {
					sql += "AND a.department_answer_id = :departmentAnswerId ";
				}
			}

			sql += "ORDER BY " + //
					"case when judgement_id = '' then '1' else '0' end, " + // 行政で追加した条項は判定項目IDが「空」なので、最後にするため、ソートキー追加
					"judgement_id, " + //
					"judgement_result_index ASC";

			Query query = em.createNativeQuery(sql, Answer.class);
			query = query.setParameter("applicationId", applicationId);
			query = query.setParameter("applicationStepId", applicationStepId);
			if (applicationStepId == 2 && departmentAnswerId > 0) {
				query = query.setParameter("departmentAnswerId", departmentAnswerId);
			}
			return query.getResultList();
		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("O_回答検索 終了");
		}
	}
	
	/**
	 * O_回答検索
	 * 
	 * @param applicationId      申請ID
	 * @param applicationStepId  申請段階ID
	 * @param departmentAnswerId 部署回答ID
	 * @return O_回答リスト
	 */
	@SuppressWarnings("unchecked")
	public List<Answer> getAllAnswerListForBusiness(int applicationId,int applicationStepId,Integer departmentAnswerId) {
		LOGGER.debug("O_回答検索 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();

			String sql = "";
			sql += "" + //
					"WITH max_answer_history AS ( " + //
					"    SELECT " + //
					"        a.answer_id, " + //
					"        a.answer_user_id, " + //
					"        a.answer_text, " + //
					"        a.answer_datetime, " + //
					"        a.notify_flag, " + //
					"        a.discussion_flag, " + //
					"        a.discussion_item, " + //
					"        a.business_pass_status, " + //
					"        a.business_pass_comment, " + //
					"        a.government_confirm_status, " + //
					"        a.government_confirm_datetime, " + //
					"        a.government_confirm_comment, " + //
					"        a.re_application_flag, " + //
					"        a.permission_judgement_result, " + //
					"        a.answer_status, " + //
					"        a.answer_data_type, " + //
					"        a.update_datetime, " + //
					"        a.deadline_datetime " + //
					"    FROM " + //
					"        o_answer_history AS a " + //
					"        INNER JOIN ( " + //
					"            SELECT " + //
					"                answer_id, " + //
					"                MAX(answer_datetime) AS max_v " + //
					"            FROM " + //
					"                o_answer_history " + //
					"            WHERE " + //
					"                notify_flag = '1' " + //
					"            GROUP BY " + //
					"                answer_id " + //
					"        ) AS b " + //
					"            ON a.answer_id = b.answer_id " + //
					"            AND a.answer_datetime = b.max_v " + //
					") " + //
					"SELECT " + //
					"    a.answer_id, " + //
					"    a.application_id, " + //
					"    a.judgement_id, " + //
					"    a.judgement_result, " + //
					"    a.judgement_result_index, " + //
					"    a.answer_content, " + //
					"    a.notified_text AS notified_text, " + //
					"    a.register_datetime, " + //
					"    a.update_datetime AS update_datetime, " + //
					"    a.complete_flag AS complete_flag, " + //
					"    max_ah.notify_flag AS notified_flag, " + //
					"    a.re_application_flag, " + //
					"    a.business_reapplication_flag, " + //
					"    a.application_step_id, " + //
					"    a.department_id, " + //
					"    max_ah.discussion_flag, " + //
					"    max_ah.discussion_item, " + //
					"    a.business_pass_status, " + //
					"    max_ah.business_pass_comment, " + //
					"    a.business_answer_datetime, " + //
					"    max_ah.government_confirm_status, " + //
					"    max_ah.government_confirm_datetime, " + //
					"    max_ah.government_confirm_comment, " + //
					"    a.government_confirm_notified_flag, " + //
					"    max_ah.permission_judgement_result, " + //
					"    max_ah.answer_status, " + //
					"    max_ah.answer_data_type, " + //
					"    a.register_status, " + //
					"    a.delete_unnotified_flag, " + //
					"    a.deadline_datetime, " + //
					"    a.department_answer_id," + //
					"    a.answer_update_flag, " + //
					"    a.answer_permission_flag, " + //
					"    a.government_confirm_permission_flag, " + //
					"    a.permission_judgement_migration_flag, " + //
					"    a.version_information " + //
					"FROM " + //
					"    o_answer AS a " + //
					"    LEFT JOIN max_answer_history AS max_ah " + //
					"        ON a.answer_id = max_ah.answer_id " + //
					"WHERE " + //
					"    a.application_id = :applicationId " + //
					"    AND a.application_step_id = :applicationStepId " + //
					"    AND a.delete_flag = '0' " + // 未削除
					"    AND a.register_status = '1' " + // 登録ステータス
					"    AND a.permission_judgement_migration_flag = '0' "; //許可判定移行フラグ：1チェックしないのレコードが除く

			// 事前協議の場合、部署回答IDを検索条件に追記
			if (applicationStepId == 2 && departmentAnswerId > 0) {
				sql += "AND a.department_answer_id = :departmentAnswerId ";
			}

			sql += "ORDER BY " + //
					"case when judgement_id = '' then '1' else '0' end, " + // 行政で追加した条項は判定項目IDが「空」なので、最後にするため、ソートキー追加
					"judgement_id, " + //
					"judgement_result_index ASC";

			Query query = em.createNativeQuery(sql, Answer.class);
			query = query.setParameter("applicationId", applicationId);
			query = query.setParameter("applicationStepId", applicationStepId);
			if (applicationStepId == 2 && departmentAnswerId > 0) {
				query = query.setParameter("departmentAnswerId", departmentAnswerId);
			}
			return query.getResultList();
		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("O_回答検索 終了");
		}
	}

	/**
	 * M_部署検索
	 * 
	 * @param applicationId     申請ID
	 * @param applicationStepId 申請段階ID
	 * @return M_部署リスト
	 */
	@SuppressWarnings("unchecked")
	public List<Department> getDepartmentList(int applicationId, int applicationStepId) {
		LOGGER.debug("M_部署検索 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();

			String sql = "" + //
					"SELECT DISTINCT " + //
					"  c.department_id AS department_id, " + //
					"  c.department_name AS department_name, " + //
					"  c.answer_authority_flag AS answer_authority_flag, " + //
					"  c.mail_address AS mail_address, " + //
					"  c.admin_mail_address AS admin_mail_address " + //
					"FROM " + //
					"  o_answer AS a " + //
					"INNER JOIN " + //
					"  m_judgement_authority AS b " + //
					"ON " + //
					"  a.judgement_id = b.judgement_item_id " + //
					"INNER JOIN " + //
					"  m_department AS c " + //
					"ON " + //
					"  b.department_id = c.department_id " + //
					"WHERE " + //
					"  a.application_id = :applicationId " + //
					"AND " + //
					"  a.application_step_id = :applicationStepId " + //
					"ORDER BY " + //
					"  department_id ASC";
			Query query = em.createNativeQuery(sql, Department.class);
			query = query.setParameter("applicationId", applicationId);
			query = query.setParameter("applicationStepId", applicationStepId);
			return query.getResultList();
		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("M_部署検索 終了");
		}
	}

	/**
	 * M_申請区分選択画面検索
	 * 
	 * @param applicationId      申請ID
	 * @param applicationStepId  申請段階ID
	 * @param versionInformation 申請版情報
	 * @return M_申請区分選択画面リスト
	 */
	@SuppressWarnings("unchecked")
	public List<ApplicationCategorySelectionView> getApplicationCategorySelectionViewList(int applicationId,
			int applicationStepId, int versionInformation) {
		LOGGER.debug("M_申請区分選択画面検索 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();

			String sql = "" + //
					"SELECT DISTINCT " + //
					"  c.view_id AS view_id, " + //
					"  c.view_flag AS view_flag, " + //
					"  c.multiple_flag AS multiple_flag, " + //
					"  c.title AS title, " + //
					"  c.require_flag AS require_flag, " + //
					"  c.description AS description, " + //
					"  c.judgement_type AS judgement_type " + //
					"FROM " + //
					"  o_application_category AS a " + //
					"INNER JOIN " + //
					"  m_application_category AS b " + //
					"ON " + //
					"  a.category_id=b.category_id " + //
					"INNER JOIN " + //
					"  m_application_category_selection_view AS c " + //
					"ON " + //
					"  b.view_id = c.view_id " + //
					"WHERE " + //
					"  a.application_id = :applicationId " + //
					"  AND a.application_step_id = :applicationStepId " + //
					"  AND a.version_information = :versionInformation " + //
					"ORDER BY " + //
					"  c.view_id ASC";
			Query query = em.createNativeQuery(sql, ApplicationCategorySelectionView.class);
			query = query.setParameter("applicationId", applicationId);
			query = query.setParameter("applicationStepId", applicationStepId);
			query = query.setParameter("versionInformation", versionInformation);
			return query.getResultList();
		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("M_申請区分選択画面検索 終了");
		}
	}

	/**
	 * 申請地番一覧検索
	 * 
	 * @param applicationId 申請ID
	 * @return 申請地番一覧
	 */
	@SuppressWarnings("unchecked")
	public List<ApplyLotNumber> getApplyingLotNumberList(int applicationId, int epsg) {
		LOGGER.debug("申請地番一覧検索開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();
			String sql;
			sql = "" + //
					"SELECT " + //
					"  a.application_id AS application_id, " + //
					"  a.lot_numbers AS lot_numbers, " + //
					"  ST_X(ST_Centroid(ST_Envelope(ST_Transform(a.geom, " + epsg + ")))) AS lon, " + //
					"  ST_Y(ST_Centroid(ST_Envelope(ST_Transform(a.geom, " + epsg + ")))) AS lat, " + //
					"  ST_XMin(ST_Transform(a.geom, " + epsg + ")) AS minlon, " + //
					"  ST_YMin(ST_Transform(a.geom, " + epsg + ")) AS minlat, " + //
					"  ST_XMax(ST_Transform(a.geom, " + epsg + ")) AS maxlon, " + //
					"  ST_YMax(ST_Transform(a.geom, " + epsg + ")) AS maxlat, " + //
					"  b.status AS status " + //
					"FROM " + //
					"  f_application_lot_number AS a " + //
					"LEFT OUTER JOIN " + //
					"    o_application AS b " + //
					"ON " + //
					"  b.application_id = a.application_id " + //
					"WHERE " + //
					"  a.application_id = :application_id " + //
					"ORDER BY " + //
					"  a.application_id ASC";
			Query query = em.createNativeQuery(sql, ApplyLotNumber.class);
			query = query.setParameter("application_id", applicationId);
			List<ApplyLotNumber> resultList = query.getResultList();
			// 地番に紐づく申請を取得し、結果に追加
			// 単純なLEFT JOIN だと結果が上書きされる?ため再度取得する形で実装
			List<ApplyLotNumber> resList = new ArrayList<ApplyLotNumber>();
			for (ApplyLotNumber aResult : resultList) {
				em.clear();
				final String sql2 = "SELECT ROW_NUMBER() OVER(ORDER BY a.application_id ASC) seq_id, " + //
						"null AS application_id, " + //
						"null AS lot_number_id, " + //
						"'0' AS full_flag, " + //
						"a.status AS status " + //
						"FROM o_application AS a " + //
						"WHERE application_id = :application_id " + //
						"ORDER BY application_id ASC";
				Query query2 = em.createNativeQuery(sql2, ApplicationLotNumber.class);
				query2.setParameter("application_id", aResult.getApplicationId());
				List<ApplicationLotNumber> applicationList = query2.getResultList();
				if (applicationList.size() == 0) {
					resList.add(aResult);
				} else {
					for (ApplicationLotNumber aApp : applicationList) {
						ApplyLotNumber newResult = new ApplyLotNumber();
						newResult.setApplicationId(aResult.getApplicationId());
						newResult.setLotNumbers(aResult.getLotNumbers());
						newResult.setLon(aResult.getLon());
						newResult.setLat(aResult.getLat());
						newResult.setMinlon(aResult.getMinlon());
						newResult.setMinlat(aResult.getMinlat());
						newResult.setMaxlon(aResult.getMaxlon());
						newResult.setMaxlat(aResult.getMaxlat());
						// 申請ID/ステータスは個別に取得した値を格納
						newResult.setStatus(aApp.getStatus());
						resList.add(newResult);
					}
				}
			}
			return resList;
		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("申請地番一覧検索　終了");
		}
	}

	/**
	 * M_申請ファイル一覧検索
	 * 
	 * @param applicationId     申請ID
	 * @param applicationStepId 申請段階ID
	 * @return M_申請ファイル一覧
	 */
	@SuppressWarnings("unchecked")
	public List<ApplicationFileMaster> getApplicationFileMasterList(int applicationId, int applicationStepId) {
		LOGGER.debug("M_申請ファイル一覧検索 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();

			String sql = "" + //
					"SELECT DISTINCT " + //
					"  b.application_file_id, " + //
					"  '' as judgement_item_id , " + //
					"  b.require_flag, " + //
					"  b.upload_file_name, " + //
					"  b.extension " + //
					"FROM " + //
					"  o_answer AS a " + //
					"INNER JOIN " + //
					"  m_application_file AS b " + //
					"ON " + //
					"  a.judgement_id = b.judgement_item_id " + //
					"WHERE " + //
					"  a.application_id = :applicationId "; //
			if (applicationStepId != 0) {
				sql += "  AND a.application_step_id = :applicationStepId ";
			}
			sql += "  GROUP BY b.application_file_id, b.require_flag, b.upload_file_name, b.extension " + //
					"ORDER BY " + //
					"  b.application_file_id ASC";
			Query query = em.createNativeQuery(sql, ApplicationFileMaster.class);
			query = query.setParameter("applicationId", applicationId);
			if (applicationStepId != 0) {
				query = query.setParameter("applicationStepId", applicationStepId);
			}
			return query.getResultList();
		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("M_申請ファイル一覧検索 終了");
		}
	}

	/**
	 * O_申請（事前相談）
	 * 
	 * @param departmentId     担当部署ID
	 * @param deadlineXDaysAgo 回答期限日の期日X日前
	 * @return O_申請検索リスト
	 */
	@SuppressWarnings("unchecked")
	public List<ApplicationAnswerSearchResult> getConsultationApplicatioList(String departmentId,
			Integer deadlineXDaysAgo) {
		LOGGER.debug("O_申請（事前相談） 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();

			String sql = "" + //
					"SELECT " + //
					"  t1.application_id, " + //
					"  t1.status, " + //
					"  t4.version_information, " + //
					"  TO_CHAR(t2.deadline_datetime,'YYYY/MM/DD') deadline_date, " + //
					"  CASE WHEN (CAST(t2.deadline_datetime AS DATE) - current_date) > :deadlineXDaysAgo THEN FALSE " + //
					"       ELSE TRUE END warning " + //
					"FROM " + //
					"  o_application t1 " + //
					"  INNER JOIN ( " + //
					"    SELECT " + //
					"      a.application_id AS application_id " + //
					"      ,MAX(a.deadline_datetime) AS deadline_datetime " + //
					"    FROM " + //
					"      o_answer AS a  " + //
					"      INNER JOIN m_judgement_authority AS b " + //
					"        ON a.judgement_id = b.judgement_item_id " + // 判断項目ID
					"    WHERE " + //
					"          a.application_step_id = 1 " + // 申請段階ID 1:事前相談
					"      AND a.answer_status = '0' " + // O_回答.ステータス 0:未回答
					"      AND b.department_id = :departmentId " + // ログインユーザ所属部署ID
					"      AND a.register_status = '1' " + // O_回答.登録ステータス 1:登録済み
					"    GROUP BY " + //
					"      a.application_id " + //
					"  ) t2 " + //
					"  ON t1.application_id = t2.application_id " + //
					"  INNER JOIN ( " + //
					"    SELECT " + //
					"      application_id " + //
					"      ,MAX(application_step_id) AS application_step_id " + //
					"    FROM " + //
					"      o_application_version_information  " + //
					"    GROUP BY " + //
					"      application_id " + //
					"  ) t3 " + //
					"  ON t1.application_id = t3.application_id " + // 
					"  AND t3.application_step_id = 1 " + // 
					"  INNER JOIN o_application_version_information AS t4" + // 最大の申請段階に対する版情報を取得（事前相談）
					"  ON t4.application_id = t1.application_id " + //
					"  AND t4.application_step_id = t3.application_step_id " + //
					"WHERE " + //
					"  t1.register_status = '1' " + // O_申請.登録ステータス1:登録済み
					"ORDER BY " + //
					"  t1.register_datetime DESC ";
			Query query = em.createNativeQuery(sql, ApplicationAnswerSearchResult.class);
			query = query.setParameter("deadlineXDaysAgo", deadlineXDaysAgo); // 回答期限日の期日X日前
			query = query.setParameter("departmentId", departmentId); // 担当部署ID
			return query.getResultList();
		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("O_申請（事前相談）検索 終了");
		}
	}

	/**
	 * O_申請（事前協議）
	 * 
	 * @param departmentId     担当部署ID
	 * @param deadlineXDaysAgo 回答期限日の期日X日前
	 * @return O_申請検索リスト
	 */
	@SuppressWarnings("unchecked")
	public List<ApplicationAnswerSearchResult> getDiscussionApplicatioList(String departmentId,
			Integer deadlineXDaysAgo) {
		LOGGER.debug("O_申請（事前協議） 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();

			String sql = "" + //
					"SELECT " + //
					"  t1.application_id, " + //
					"  t1.status, " + //
					"  t4.version_information, " + //
					"  TO_CHAR(t2.deadline_datetime,'YYYY/MM/DD') deadline_date, " + //
					"  CASE WHEN (CAST(t2.deadline_datetime AS DATE) - current_date) > :deadlineXDaysAgo THEN FALSE " + //
					"       ELSE TRUE END warning " + //
					"FROM " + //
					"  o_application t1 " + //
					"  INNER JOIN ( " + //
					"    SELECT " + //
					"      a.application_id AS application_id " + //
					"      ,MAX(a.deadline_datetime) AS deadline_datetime " + //
					"    FROM " + //
					"      o_answer AS a  " + //
					"      INNER JOIN m_category_judgement AS b " + //
					"        ON a.judgement_id = b.judgement_item_id " + // 判断項目ID
					"    WHERE " + //
					"          a.application_step_id = 2 " + // 申請段階ID 2:事前協議
					"      AND a.answer_status IN ('0','3','4') " + // O_回答.ステータス IN (0:未回答,3:否認済み,4:承認済み)
					"      AND a.department_id = :departmentId " + // ログインユーザ所属部署ID
					"      AND a.register_status = '1' " + // O_回答.登録ステータス 1:登録済み
					"    GROUP BY " + //
					"      a.application_id " + //
					"  ) t2 " + //
					"  ON t1.application_id = t2.application_id " + //
					"  INNER JOIN ( " + //
					"    SELECT " + //
					"      application_id " + //
					"      ,MAX(application_step_id) AS application_step_id " + //
					"    FROM " + //
					"      o_application_version_information  " + //
					"    GROUP BY " + //
					"      application_id " + //
					"  ) t3 " + //
					"  ON t1.application_id = t3.application_id " + // 
					"  AND t3.application_step_id = 2 " + // 
					"  INNER JOIN o_application_version_information AS t4" + // 最大の申請段階に対する版情報を取得（事前相談）
					"  ON t4.application_id = t1.application_id " + //
					"  AND t4.application_step_id = t3.application_step_id " + //
					"WHERE " + //
					"  t1.register_status = '1' " + // O_申請.登録ステータス1:登録済み
					"ORDER BY " + //
					"  t1.register_datetime DESC ";
			Query query = em.createNativeQuery(sql, ApplicationAnswerSearchResult.class);
			query = query.setParameter("deadlineXDaysAgo", deadlineXDaysAgo); // 回答期限日の期日X日前
			query = query.setParameter("departmentId", departmentId); // 担当部署ID
			return query.getResultList();
		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("O_申請（事前協議）検索 終了");
		}
	}

	/**
	 * O_申請（許可判定）
	 * 
	 * @param departmentId     担当部署ID
	 * @param deadlineXDaysAgo 回答期限日の期日X日前
	 * @return O_申請検索リスト
	 */
	@SuppressWarnings("unchecked")
	public List<ApplicationAnswerSearchResult> getPermissionApplicatioList(String departmentId,
			Integer deadlineXDaysAgo) {
		LOGGER.debug("O_申請（許可判定） 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();

			String sql = "" + //
					"SELECT " + //
					"  t1.application_id, " + //
					"  t1.status, " + //
					"  t4.version_information, " + //
					"  TO_CHAR(t2.deadline_datetime,'YYYY/MM/DD') deadline_date, " + //
					"  CASE WHEN (CAST(t2.deadline_datetime AS DATE) - current_date) > :deadlineXDaysAgo THEN FALSE " + //
					"       ELSE TRUE END warning " + //
					"FROM " + //
					"  o_application t1 " + //
					"  INNER JOIN ( " + //
					"    SELECT " + //
					"      a.application_id AS application_id " + //
					"      ,MAX(a.deadline_datetime) AS deadline_datetime " + //
					"    FROM " + //
					"      o_answer AS a  " + //
					"      INNER JOIN m_judgement_authority AS b " + //
					"        ON a.judgement_id = b.judgement_item_id " + // 判断項目ID
					"    WHERE " + //
					"          a.application_step_id = 3 " + // 申請段階ID 3:許可判定
					"      AND a.answer_status = '0' " + // O_回答.ステータス 0:未回答
					"      AND b.department_id = :departmentId " + // ログインユーザ所属部署ID
					"      AND a.register_status = '1' " + // O_回答.登録ステータス 1:登録済み
					"    GROUP BY " + //
					"      a.application_id " + //
					"  ) t2 " + //
					"  ON t1.application_id = t2.application_id " + //
					"  INNER JOIN ( " + //
					"    SELECT " + //
					"      application_id " + //
					"      ,MAX(application_step_id) AS application_step_id " + //
					"    FROM " + //
					"      o_application_version_information  " + //
					"    GROUP BY " + //
					"      application_id " + //
					"  ) t3 " + //
					"  ON t1.application_id = t3.application_id " + // 
					"  AND t3.application_step_id = 3 " + // 
					"  INNER JOIN o_application_version_information AS t4" + //
					"  ON t4.application_id = t1.application_id " + //
					"  AND t4.application_step_id = t3.application_step_id " + //
					"WHERE " + //
					"  t1.register_status = '1' " + // O_申請.登録ステータス1:登録済み
					"ORDER BY " + //
					"  t1.register_datetime DESC ";
			Query query = em.createNativeQuery(sql, ApplicationAnswerSearchResult.class);
			query = query.setParameter("deadlineXDaysAgo", deadlineXDaysAgo); // 回答期限日の期日X日前
			query = query.setParameter("departmentId", departmentId); // 担当部署ID
			return query.getResultList();
		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("O_申請（許可判定）検索 終了");
		}
	}

	/**
	 * O_申請ファイル<br>
	 * 申請ファイルの最新版を取得する
	 * 
	 * @param applicationFileId 申請ファイルID
	 * @param applicationId     申請ID
	 * @param applicationStepId 申請段階ID
	 * 
	 * @return 申請ファイルの最新版
	 */
	@SuppressWarnings("unchecked")
	public List<ApplicationFile> getApplicatioFile(String applicationFileId, int applicationId, int applicationStepId) {
		LOGGER.debug("O_申請ファイル検索  開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();

			String sql = "" + //
					"SELECT " + //
					"  file_id, " + //
					"  application_id, " + //
					"  application_step_id, " + //
					"  application_file_id, " + //
					"  upload_file_name, " + //
					"  file_path, " + //
					"  version_information, " + //
					"  extension, " + //
					"  upload_datetime, " + //
					"  application_step_id, " + //
					"  direction_department, " + //
					"  revise_content " + //
					"FROM " + //
					"  o_application_file  AS file " + //
					"  INNER JOIN (  " + //
					"    SELECT  " + //
					"      application_id AS lastApplicationId,  " + //
					"      application_step_id AS applicationStepId,  " + //
					"      application_file_id AS lastApplicationFileId,  " + //
					"      MAX(version_information) AS lastVer  " + //
					"    FROM  " + //
					"      o_application_file  " + //
					"    WHERE  " + //
					"      application_id = :applicationId   " + //
					"    AND  " + //
					"      application_step_id = :applicationStepId  " + //
					"    AND  " + //
					"      application_file_id = :applicationFileId  " + //
					"    GROUP BY  " + //
					"      application_id,  " + //
					"      application_step_id,  " + //
					"      application_file_id  " + //
					"  ) AS lastVerFiles " + //
					"  ON " + //
					"    file.application_id = lastVerFiles.lastApplicationId  " + //
					"  AND " + //
					"    file.application_step_id = lastVerFiles.applicationStepId  " + //
					"  AND " + //
					"    file.application_file_id = lastVerFiles.lastApplicationFileId  " + //
					"  AND " + //
					"    file.delete_flag = '0'  " + //
					"WHERE " + //
					"  COALESCE(file.version_information,0) = COALESCE(lastVerFiles.lastVer,0) ";
			Query query = em.createNativeQuery(sql, ApplicationFile.class);
			query = query.setParameter("applicationId", applicationId);
			query = query.setParameter("applicationStepId", applicationStepId);
			query = query.setParameter("applicationFileId", applicationFileId);
			return query.getResultList();
		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("O_申請ファイル検索 終了");
		}
	}

	/**
	 * O_部署_回答検索
	 * 
	 * @param applicationId     申請ID
	 * @param applicationStepId 申請段階ID
	 * @return O_回答リスト
	 */
	@SuppressWarnings("unchecked")
	public List<DepartmentAnswer> getDepartmentAnswerList(int applicationId, int applicationStepId) {
		LOGGER.debug("O_部署_回答検索 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();

			String sql = "" + //
					"SELECT DISTINCT " + //
					"  department_answer_id, " + //
					"  application_id, " + //
					"  application_step_id, " + //
					"  department_id, " + //
					"  government_confirm_status, " + //
					"  government_confirm_datetime, " + //
					"  government_confirm_comment, " + //
					"  notified_text, " + //
					"  complete_flag, " + //
					"  notified_flag, " + //
					"  register_datetime, " + //
					"  update_datetime, " + //
					"  register_status, " + //
					"  delete_unnotified_flag, " + //
					"  government_confirm_permission_flag " + //
					"FROM " + //
					"  o_department_answer " + //
					"WHERE " + //
					"  application_id = :applicationId " + //
					"  AND application_step_id = :applicationStepId ";
			sql += "ORDER BY " + //
					"  department_id ASC";

			Query query = em.createNativeQuery(sql, DepartmentAnswer.class);
			query = query.setParameter("applicationId", applicationId);
			query = query.setParameter("applicationStepId", applicationStepId);
			return query.getResultList();
		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("O_部署_回答検索 終了");
		}
	}
	
	/**
	 * O_受付回答に紐づく部署検索
	 * 
	 * @param applicationId      申請ID
	 * @param applicationStepId  申請段階ID
	 * @param versionInformation 版情報
	 * @return O_回答リスト
	 */
	@SuppressWarnings("unchecked")
	public List<Department> getAcceptingAnswerDepartmentList(Integer applicationId, Integer applicationStepId,
			Integer versionInformation) {
		LOGGER.debug("O_部署_回答検索 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();

			String sql = "" + //
					"SELECT DISTINCT " + //
					"  b.department_id AS department_id, " + //
					"  b.department_name AS department_name, " + //
					"  b.answer_authority_flag AS answer_authority_flag, " + //
					"  b.mail_address AS mail_address, " + //
					"  b.admin_mail_address AS admin_mail_address " + //
					"FROM " + //
					"  o_accepting_answer AS a " + //
					"INNER JOIN " + //
					"  m_department AS b " + //
					"ON " + //
					"  b.department_id = a.department_id " + //
					"WHERE " + //
					"  a.application_id = :applicationId " + //
					"  AND a.application_step_id = :applicationStepId " + //
					"  AND a.version_infomation = :versionInformation " + //
					"ORDER BY " + //
					"  department_id ASC";

			Query query = em.createNativeQuery(sql, Department.class);
			query = query.setParameter("applicationId", applicationId);
			query = query.setParameter("applicationStepId", applicationStepId);
			query = query.setParameter("versionInformation", versionInformation);
			return query.getResultList();
		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("O_部署_回答検索 終了");
		}
	}
	
	/**
	 * M_申請ファイル一覧検索(事前協議のみ)
	 * 
	 * @param applicationId      申請ID
	 * @param applicationStepId  申請段階ID
	 * @param versionInformation 版情報
	 * 
	 * @return M_申請ファイル一覧
	 */
	@SuppressWarnings("unchecked")
	public List<ApplicationFileMaster> getApplicationFileMasterListWithVersionInformation(Integer applicationId,
			Integer applicationStepId, Integer versionInformation) {
		LOGGER.debug("M_申請ファイル一覧検索 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();

			String sql = "" + //
					"SELECT DISTINCT " + //
					"  b.application_file_id, " + //
					"  '' as judgement_item_id , " + //
					"  b.require_flag, " + //
					"  b.upload_file_name, " + //
					"  b.extension " + //
					"FROM " + //
					"  o_application_file AS a " + //
					"INNER JOIN " + //
					"  m_application_file AS b " + //
					"ON " + //
					"  a.application_file_id = b.application_file_id " + //
					"WHERE " + //
					"  a.application_id = :applicationId " + //
					"  AND a.application_step_id = :applicationStepId " + //
					"  AND a.version_information = :versionInformation " + //
					"ORDER BY " + //
					"  application_file_id ASC";

			Query query = em.createNativeQuery(sql, ApplicationFileMaster.class);
			query = query.setParameter("applicationId", applicationId);
			query = query.setParameter("applicationStepId", applicationStepId);
			query = query.setParameter("versionInformation", versionInformation);
			return query.getResultList();
		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("M_申請ファイル一覧検索 終了");
		}
	}

	/**
	 * 申請版情報取得（仮申請の場合、版情報-1で返す。）
	 * 
	 * @param applicationId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<ApplicationVersionInformation> getApplicationVersionInformation(Integer applicationId) {
		LOGGER.debug("O_申請版情報検索 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();

			String sql = "" + //
					"SELECT " + //
					"  application_id, " + //
					"  application_step_id, " + //
					"  ( " + //
					"    CASE " + //
					"      WHEN register_status = '0' " + //
					"        THEN version_information - 1 " + //
					"      ELSE version_information " + //
					"      END " + //
					"  ) AS version_information, " + //
					"  accepting_flag, " + //
					"  accept_version_information, " + //
					"  register_datetime, " + //
					"  update_datetime, " + //
					"  complete_datetime, " + //
					"  register_status " + //
					"FROM " + //
					"  o_application_version_information " + //
					"WHERE " + //
					"  application_id = :applicationId " + //
					"  AND ( " + //
					"    register_status = '1' " + //
					"    OR ( " + //
					"      register_status = '0' " + //
					"      AND version_information > 1 " + //
					"    ) " + // 受付済みの版がある申請を検索対象とする
					"  ) " + //
					"ORDER BY " + //
					"  register_datetime DESC";

			Query query = em.createNativeQuery(sql, ApplicationVersionInformation.class);
			query = query.setParameter("applicationId", applicationId);
			return query.getResultList();
		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("O_申請版情報検索 終了");
		}
	}
}
