package developmentpermission.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;
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
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import developmentpermission.dao.ApplicationDao;
import developmentpermission.dao.CategoryJudgementDao;
import developmentpermission.dao.JudgementLayerDao;
import developmentpermission.dao.LayerDao;
import developmentpermission.dao.LotNumberDao;
import developmentpermission.entity.Answer;
import developmentpermission.entity.ApplicationCategoryJudgement;
import developmentpermission.entity.ApplicationCategorySelectionView;
import developmentpermission.entity.ApplicationFile;
import developmentpermission.entity.ApplicationVersionInformation;
import developmentpermission.entity.CategoryJudgementAndResult;
import developmentpermission.entity.ColumnValue;
import developmentpermission.entity.DepartmentAnswer;
import developmentpermission.entity.Distance;
import developmentpermission.entity.Layer;
import developmentpermission.entity.LotNumber;
import developmentpermission.entity.Oid;
import developmentpermission.entity.RoadCenterLinePosition;
import developmentpermission.entity.RoadJudgeLabel;
import developmentpermission.entity.RoadLod2;
import developmentpermission.entity.SpiltLineExtent;
import developmentpermission.entity.SplitLine;
import developmentpermission.entity.SplitRoadCenterLine;
import developmentpermission.form.ApplicationCategoryForm;
import developmentpermission.form.ApplicationCategorySelectionViewForm;
import developmentpermission.form.GeneralConditionDiagnosisReportProgressForm;
import developmentpermission.form.GeneralConditionDiagnosisReportRequestForm;
import developmentpermission.form.GeneralConditionDiagnosisRequestForm;
import developmentpermission.form.GeneralConditionDiagnosisResultForm;
import developmentpermission.form.LayerForm;
import developmentpermission.form.LotNumberForm;
import developmentpermission.form.UploadApplicationFileForm;
import developmentpermission.form.UploadForGeneralConditionDiagnosisForm;
import developmentpermission.repository.AnswerRepository;
import developmentpermission.repository.ApplicationCategoryJudgementRepository;
import developmentpermission.repository.ApplicationVersionInformationRepository;
import developmentpermission.repository.CategorySelectionViewRepository;
import developmentpermission.repository.DepartmentAnswerRepository;
import developmentpermission.repository.RoadJudgeLabelRepository;
import developmentpermission.repository.jdbc.JudgementJdbc;
import developmentpermission.util.AuthUtil;
import developmentpermission.util.ExportJudgeForm;
import developmentpermission.util.model.RoadJudgeResult;

/**
 * 概況診断Serviceクラス
 */
@Service
@Transactional
public class JudgementService extends AbstractJudgementService {

	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(JudgementService.class);

	/** M_道路判定ラベルRepositoryインスタンス */
	@Autowired
	private RoadJudgeLabelRepository roadJudgeLabelRepository;

	/** M_画面インスタンス */
	@Autowired
	private CategorySelectionViewRepository categorySelectionViewRepository;

	/** M_カテゴリ判定インスタンス */
	@Autowired
	private ApplicationCategoryJudgementRepository applicationCategoryJudgementRepository;

	/** O_回答Repositoryインスタンス */
	@Autowired
	private AnswerRepository answerRepository;

	/** O_部署回答Repositoryインスタンス */
	@Autowired
	private DepartmentAnswerRepository departmentAnswerRepository;

	/** GIS判定:判定無し */
	public static final String GIS_JUDGEMENT_0 = "0";
	/** GIS判定:重なる */
	public static final String GIS_JUDGEMENT_1 = "1";
	/** GIS判定:重ならない */
	public static final String GIS_JUDGEMENT_2 = "2";
	/** GIS判定:バッファに重なる */
	public static final String GIS_JUDGEMENT_3 = "3";
	/** GIS判定:バッファに重ならない */
	public static final String GIS_JUDGEMENT_4 = "4";

	/** GIS判定：道路判定 */
	public static final String GIS_JUDGEMENT_5 = "5";

	/** 重なり属性表示: なし */
	public static final String DISPLAY_ATTRIBUTE_NONE = "0";
	/** 重なり属性表示: 連結 */
	public static final String DISPLAY_ATTRIBUTE_JOINT = "1";
	/** 重なり属性表示: 改行リピート */
	public static final String DISPLAY_ATTRIBUTE_NEWLINE = "2";
	/** 重なり属性表示: 概況診断結果行を分けて表示 */
	public static final String DISPLAY_ATTRIBUTE_NEWROW = "3";

	/** 区分判定:判定なし */
	public static final String CATEGORY_NONE = "0";
	/** 区分1 */
	public static final String CATEGORY_1 = "1";
	/** 区分2 */
	public static final String CATEGORY_2 = "2";
	/** 区分3 */
	public static final String CATEGORY_3 = "3";
	/** 区分4 */
	public static final String CATEGORY_4 = "4";
	/** 区分5 */
	public static final String CATEGORY_5 = "5";
	/** 区分6 */
	public static final String CATEGORY_6 = "6";
	/** 区分7 */
	public static final String CATEGORY_7 = "7";
	/** 区分8 */
	public static final String CATEGORY_8 = "8";
	/** 区分9 */
	public static final String CATEGORY_9 = "9";
	/** 区分10 */
	public static final String CATEGORY_10 = "0";

	/** 概況診断結果画像格納フォルダのランダム文字列に使用する文字 */
	public static final String JUDGEMENT_FOLDER_CHARACTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	/** 概況診断結果画像格納フォルダのランダム文字列長 */
	public static final int JUDGEMENT_FOLDER_LENGTH = 10;
	/** 概況診断結果画像格納フォルダの日時フォーマット */
	public static final String JUDGEMENT_FOLDER_DATE_FORMAT = "yyyyMMddHHmmssSSS";

	/** 概況表示文言置換ターゲット文字 */
	private static final String DESCRIPTION_REPLACE_TARGET_CHARACTER = "@";

	/** 概況表示文言距離置換後文字(申請地範囲内の場合) */
	@Value("${app.category.judgement.attribute.distance.applicationAreaText}")
	private String distanceApplicationAreaText;

	/** 概況表示文言距離置換ターゲット文字 */
	@Value("${app.category.judgement.attribute.distance.replaceText}")
	private String distanceReplaceText;

	/** 概況表示文言距離置換後文字 */
	@Value("${app.category.judgement.attribute.distance.replacedText}")
	private String distanceReplacedText;

	/** 距離算出時に使用するepsg */
	@Value("${app.category.judgement.distance.epsg}")
	private Integer distanceEpsg;

	/** 重なり属性表示フラグが1の場合の属性区切り文字 */
	@Value("${app.category.judgement.attribute.joint}")
	private String descriptionJointCharacter;

	/** 属性NULL時表示文字 */
	@Value("${app.category.judgement.attribute.nullValue}")
	private String attributeNullValue;

	/** 道路判定分割道路中心線取得バッファ(m) */
	@Value("${app.roadjudge.splitcenterline.buffer}")
	private Double splitCenterLineBuffer;

	/** 道路判定道路中心線取得バッファ(m) */
	@Value("${app.roadjudge.roadcenterline.buffer}")
	private Double roadCenterLineBuffer;

	/** 道路部最大幅員置き換え文字 */
	@Value("${app.roadjudge.roadMaxWidth.replaceText}")
	private String roadMaxWidthReplaceText;

	/** 車道最大幅員置き換え文字 */
	@Value("${app.roadjudge.roadwayMaxWidth.replaceText}")
	private String roadwayMaxWidthReplaceText;

	/** 道路部最小幅員置き換え文字 */
	@Value("${app.roadjudge.roadMinWidth.replaceText}")
	private String roadMinWidthReplaceText;

	/** 車道最小幅員置き換え文字 */
	@Value("${app.roadjudge.roadwayMinWidth.replaceText}")
	private String roadwayMinWidthReplaceText;

	/** 道路判定 レイヤクエリ値置き換え文字 */
	@Value("${app.roadjudge.layerQueryReplaceText.value}")
	private String layerQueryReplaceTextValue;

	/** 道路判定 道路LOD2レイヤ識別文字 */
	@Value("${app.roadjudge.identifyText.roadLod2Layer}")
	private String roadLod2LayerIdentifyText;

	/** 道路判定 区割り線レイヤ識別文字 */
	@Value("${app.roadjudge.identifyText.splitLineLayer}")
	private String splitLineLayerIdentifyText;

	/** 道路判定 最大幅員レイヤ識別文字 */
	@Value("${app.roadjudge.identifyText.maxWidthLayer}")
	private String maxWidthLayerIdentifyText;

	/** 道路判定 最小幅員レイヤ識別文字 */
	@Value("${app.roadjudge.identifyText.minWidthLayer}")
	private String minWidthLayerIdentifyText;

	/** 道路判定 隣接歩道レイヤ識別文字 */
	@Value("${app.roadjudge.identifyText.sideWalkLayer}")
	private String sideWalkLayerIdentifyText;

	/** 道路判定 幅員値レイヤ識別文字 */
	@Value("${app.roadjudge.identifyText.widthTextLayer}")
	private String widthTextLayerIdentifyText;

	/** 道路判定 隣接歩道地番ID値置き換え文字 */
	@Value("${app.roadjudge.identifyText.sideWalkLayer.lotNumber}")
	private String sideWalkLotNumberIdentifyText;

	/** 道路判定 隣接歩道隣接歩道ID値置き換え文字 */
	@Value("${app.roadjudge.identifyText.sideWalkLayer.sideWalk}")
	private String sideWalkSidewalkIdentifyText;

	/** 道路判定 幅員表示範囲識別文字 **/
	@Value("${app.roadjudge.identifyText.widthTextArea}")
	private String widthTextAreaIdentifyText;
	/** 道路判定 最大幅員表示範囲識別文字 */
	@Value("${app.roadjudge.identifyText.maxWidthTextArea}")
	private String maxWidthTextAreaIdentifyText;
	/** 道路判定 最小幅員表示範囲識別文字 */
	@Value("${app.roadjudge.identifyText.minWidthTextArea}")
	private String minWidthTextAreaIdentifyText;
	/** 道路判定 分割線取得結果表示範囲識別文字 **/
	@Value("${app.roadjudge.identifyText.splitLineResult}")
	private String splitLineResultIdentifyText;
	/** 道路判定 隣接歩道判定結果表示範囲識別文字 **/
	@Value("${app.roadjudge.identifyText.walkwayResult}")
	private String walkwayResultIdentifyText;
	/** 道路判定 幅員による段階表示範囲識別文字 **/
	@Value("${app.roadjudge.identifyText.displayByWidth}")
	private String displayByWidthIdentifyText;
	/** 道路判定 分割線取得結果 両側取得フラグ値 **/
	@Value("${app.roadjudge.splitLineResult.flag.bothSide}")
	private int bothSideFlag;
	/** 道路判定 分割線取得結果 片側取得フラグ値 **/
	@Value("${app.roadjudge.splitLineResult.flag.oneSide}")
	private int oneSideFlag;
	/** 道路判定 分割線取得結果 取得なしフラグ値 **/
	@Value("${app.roadjudge.splitLineResult.flag.noSide}")
	private int noSideFlag;
	/** 道路判定 分割線取得結果 取得エラーフラグ値 **/
	@Value("${app.roadjudge.splitLineResult.flag.error}")
	private int splitLineErrorFlag;
	/** 道路判定 隣接歩道判定結果 隣接有りフラグ値 **/
	@Value("${app.roadjudge.walkwayResult.true}")
	private int walkwayResultTrueFlag;
	/** 道路判定 隣接歩道判定結果 隣接なしフラグ値 */
	@Value("${app.roadjudge.walkwayResult.false}")
	private int walkwayResultFalseFlag;

	/** 道路判定 幅員エラー値 */
	@Value("${app.roadjudge.widthErrorCode}")
	private int widthErrorCode;

	/** 道路判定 道路種別案内表示範囲識別文字 */
	@Value("${app.roadjudge.identifyText.roadTypeResult}")
	private String roadTypeResultIdentifyText;

	/** 道路判定 道路種別該当時文言非表示対象道路種別値（カンマ区切り） */
	@Value("${app.roadjudge.roadtype.nondisplay.value}")
	private String roadtypeNondisplayValueText;

	/** 道路判定 道路種別該当時文言非表示対象テキスト識別子（カンマ区切り） */
	@Value("${app.roadjudge.roadtype.nondisplay.identifyText}")
	private String roadtypeNondisplayIdentifyTextText;

	/** 道路判定 道路判定 道路種別不明値 */
	@Value("${app.roadjudge.roadtype.unknown.value}")
	private String roadtypeUnknownValue;

	/** 申請登録時の概況診断レポート接尾句(日付フォーマット) */
	@Value("${app.application.report.filename.footer}")
	protected String applicationReportFileNameFooter;

	/** 申請登録時の概況診断レポート接頭句 */
	@Value("${app.application.report.filename.header}")
	protected String applicationReportFileNameHeader;

	/** 申請登録時の概況診断レポートのファイルID */
	@Value("${app.application.report.fileid}")
	protected String applicationReportFileId;
	/**
	 * 判定JDBCインスタンス
	 */
	@Autowired
	private JudgementJdbc judgementJdbc;

	/** int型最大値 */
	private int MAX_INT = 2147483647;

	/** 申請Serviceインスタンス */
	@Autowired
	private ApplicationService applicationService;

	/** o_申請版情報Repositoryインスタンス */
	@Autowired
	private ApplicationVersionInformationRepository applicationVersionInformationRepository;

	/**
	 * 概況診断結果取得
	 * 
	 * @param generalConditionDiagnosisRequestFrom パラメータ
	 * @return 診断結果
	 */
	public List<GeneralConditionDiagnosisResultForm> executeGeneralConditionDiagnosis(
			GeneralConditionDiagnosisRequestForm generalConditionDiagnosisRequestFrom) {
		LOGGER.debug("概況診断結果取得 開始");
		try {
			JudgementLayerDao judgementLayerDao = new JudgementLayerDao(emf);
			CategoryJudgementDao categoryJudgementDao = new CategoryJudgementDao(emf);

			List<GeneralConditionDiagnosisResultForm> formList = new ArrayList<GeneralConditionDiagnosisResultForm>();

			// 選択区分集約
			Map<String, Set<String>> categoryMap = new HashMap<String, Set<String>>();
			List<ApplicationCategorySelectionView> viewList = categorySelectionViewRepository
					.getCategorySelectionViewList();

			// 各区分単位で初期化
			for (ApplicationCategorySelectionView view : viewList) {
				categoryMap.put(view.getViewId(), new HashSet<String>());
			}

			if (generalConditionDiagnosisRequestFrom.getApplicationCategories() != null) {
				for (ApplicationCategorySelectionViewForm viewForm : generalConditionDiagnosisRequestFrom
						.getApplicationCategories()) {
					if (viewForm.getApplicationCategory() != null) {
						for (ApplicationCategoryForm categoryForm : viewForm.getApplicationCategory()) {
							String tmpCategoryId = categoryForm.getId();
							String screenId = categoryForm.getScreenId();
							if (!EMPTY.equals(screenId)) {
								Set<String> categorySet = categoryMap.get(screenId);
								// checkedの確認は不要
								if (/* categoryForm.getChecked() && */tmpCategoryId != null
										&& !EMPTY.equals(tmpCategoryId) && !categorySet.contains(tmpCategoryId)) {
									// checkedのIDのみを集約, categorySetはアドレスであるため
									categorySet.add(tmpCategoryId);
								}
							}
						}
					}
				}
			}
			// 申請ID
			Integer applicationId = generalConditionDiagnosisRequestFrom.getApplicationId();
			// 初回申請の場合、地番IDリストを取得
			List<Integer> lotNumberList = new ArrayList<Integer>();
			if (generalConditionDiagnosisRequestFrom.getLotNumbers() != null && applicationId == null) {
				for (LotNumberForm lotNumberForm : generalConditionDiagnosisRequestFrom.getLotNumbers()) {
					int tmpLotNumber = lotNumberForm.getChibanId();
					if (!lotNumberList.contains(tmpLotNumber)) {
						lotNumberList.add(tmpLotNumber);
					}
				}
			}
			// 申請種類ID
			Integer applicationTypeId = generalConditionDiagnosisRequestFrom.getApplicationTypeId();
			// 申請段階ID
			Integer applicationStepId = generalConditionDiagnosisRequestFrom.getApplicationStepId();
			// 区分判定リスト取得categoryJudgementDao
			List<CategoryJudgementAndResult> categoryJudgementList = categoryJudgementDao
					.getCategoryJudgementList(applicationTypeId, applicationStepId);
			// 判定結果IDを採番
			int generalConditionDiagnosisId = judgementJdbc.generateGeneralConditionDiagnosisId();

			// 判定結果項目ID
			int judgeResultItemId = 1;
			for (CategoryJudgementAndResult categoryJudgement : categoryJudgementList) {
				LOGGER.debug("概況診断判定実行開始 区分判定ID=" + categoryJudgement.getJudgementItemId());
				// 判定結果
				boolean judgeResult = false;

				// 前回の申請で登録されるかフラグ
				boolean isRegistedJudgementItem = false;
				// 今回登録予定フラグ
				boolean isnNewJudgementItem = false;

				// 重なり属性表示フラグ
				String displayAttributeFlag = categoryJudgement.getDisplayAttributeFlag();
				// テーブル名
				String tableName = categoryJudgement.getTableName();
				// フィールド名
				String fieldName = categoryJudgement.getFieldName();
				// フィールド名配列
				String[] fieldArray = new String[0];
				if (fieldName != null && !fieldName.isEmpty()) {
					fieldArray = fieldName.split(COMMA);
				}

				// 重なり属性用属性値リスト
				List<List<String>> valuesList = new ArrayList<List<String>>();
				// レイヤリスト
				List<Layer> targetLayers = null;
				// DistanceとLayerの紐づけ用Map 距離結果を保持する
				Map<String, Distance> layerDistanceMap = new HashMap<>();
				// oidとLayerの紐づけ用Map GIS判定の結果を保持する
				Map<String, List<Oid>> layerOidMap = new HashMap<>();

				// 道路判定結果リスト
				final List<RoadJudgeResult> roadJudgeResultList = new ArrayList<RoadJudgeResult>();

				if (lotNumberList.size() > 0 || applicationId != null) {
					// 区分判定結果
					boolean categoryJudgeResult = false;
					// GIS判定結果
					boolean gisJudgeResult = true;
					// 区分判定有無
					boolean isCatagoryJudgeExists = isCategoryJudgeExists(categoryJudgement, applicationStepId);
					// GIS判定有無
					String gisJudgement = categoryJudgement.getGisJudgement();
					boolean gisJudgeExists = (gisJudgement != null && !GIS_JUDGEMENT_0.equals(gisJudgement));
					// 前回の申請で登録されるかフラグ
					isRegistedJudgementItem = isReapplicationJudgementItem(categoryJudgement,
							generalConditionDiagnosisRequestFrom);

					boolean executeCategoryJudgement = executeCategoryJudgement(categoryMap, categoryJudgement);
					// 区分判定を実施
					if (generalConditionDiagnosisRequestFrom.getApplicationId() == null) {
						// 初回申請
						if (isCatagoryJudgeExists && executeCategoryJudgement) {
							categoryJudgeResult = true;
						}
					} else {
						// 再申請の場合、条項は前回の申請にある場合も、区分判定実施対象とする
						if (isCatagoryJudgeExists && (executeCategoryJudgement || isRegistedJudgementItem)) {
							categoryJudgeResult = true;
						}
					}
					// GIS判定を実施
					if (gisJudgeExists) {
						if (GIS_JUDGEMENT_5.equals(gisJudgement)) {
							// 道路判定
							LOGGER.debug("道路判定開始 区分判定ID=" + categoryJudgement.getJudgementItemId());
							// A1: 道路LOD2レイヤに重なるか判定
							List<RoadLod2> roadLod2List = judgementLayerDao.getIntersectsRoadLod2(lotNumberList,
									applicationId, epsg, categoryJudgement.getBuffer());
							// 同一路線番号の判定結果を集約する
							roadLod2List = aggregateRoadLod2Result(roadLod2List);
							if (roadLod2List.size() > 0) {
								// 該当
								LOGGER.debug("道路判定 該当");
								gisJudgeResult = true;
								for (RoadLod2 lod2 : roadLod2List) {
									final RoadJudgeResult roadJudgeResult = new RoadJudgeResult();
									final List<SplitLine> splitLineList = new ArrayList<SplitLine>();
									try {
										// A2: 申請地番+バッファ及び道路LOD2と重なる区割り線フィーチャを取得
										final List<SplitLine> bufferIntersectSplitLines = judgementLayerDao
												.getSplitLineFromLotNumberAndRoadLod2(lotNumberList, applicationId,
														epsg, categoryJudgement.getBuffer() + (lod2.getWidth() / 2.0),
														lod2.getObjectId(), lod2.getLineNumber());

										final List<String> objectIdString = new ArrayList<String>();
										for (SplitLine bSplit : bufferIntersectSplitLines) {
											objectIdString.add(bSplit.getObjectId().toString());
										}
										LOGGER.debug("バッファ重なり区割り線取得結果 objectId=" + String.join(",", objectIdString));
										// B1-B2: 申請地番の重心位置から最近接の道路中心位置と道路中心線フィーチャを取得
										// まず、地番バッファ重複長の最も大きい道路中心線で取得
										List<RoadCenterLinePosition> roadCenterLinePosList = judgementLayerDao
												.getRoadCenterLinePositionWithBuffer(lotNumberList, applicationId,
														lod2.getObjectId(), epsg, roadCenterLineBuffer,
														lod2.getLineNumber());
										if (roadCenterLinePosList.size() != 1) {
											// バッファ重複長で取れない場合、最も近い道路中心線を取得
											LOGGER.debug("道路判定 地番バッファで道路中心線が取得できないため最も近い道路中心線を取得");
											roadCenterLinePosList = judgementLayerDao.getRoadCenterLinePosition(
													lotNumberList, applicationId, lod2.getObjectId(),
													lod2.getLineNumber());
										}

										if (roadCenterLinePosList.size() != 1) {
											throw new Exception("最近接道路中心位置取得結果不正");
										}
										// B2-B3: 最近接道路中心線位置から正負の方向に道路中心線を分割したフィーチャを取得
										final List<SplitRoadCenterLine> splitRoadCenterLineList = judgementLayerDao
												.getSplitRoadCenterLine(roadCenterLinePosList.get(0).getWkt(), epsg,
														roadCenterLinePosList.get(0).getObjectId(),
														splitCenterLineBuffer);
										if (splitRoadCenterLineList.size() == 0 || splitRoadCenterLineList.size() > 2) {
											// 分割道路中心線は通常1または2件取得される
											throw new Exception("正負両方向の道路中心線取得に失敗");
										}
										// 正負の方向で区割り線判定
										for (int i = 0; i < splitRoadCenterLineList.size(); i++) {
											// 分割道路中心線に重なる区割り線を最近接道路中心位置からの距離順で取得
											final List<SplitLine> splitLineListWithRoadCenterLine = judgementLayerDao
													.getSplitLineWithSplitRoadCenterLine(
															splitRoadCenterLineList.get(i).getWkt(),
															roadCenterLinePosList.get(0).getWkt(), epsg);

											// B3: 最近接道路中心位置から指定の方向で区割り線が1件以上取得できているか判定
											final List<SplitLine> intersectedList = new ArrayList<SplitLine>();
											for (SplitLine aSplit : splitLineListWithRoadCenterLine) {
												for (SplitLine bSplit : bufferIntersectSplitLines) {
													if (aSplit.getObjectId().equals(bSplit.getObjectId())) {
														intersectedList.add(aSplit);
													}
												}
											}
											if (intersectedList.size() > 0) {
												// 取得できている場合、比較可能な区割り線としてセット
												LOGGER.debug("地番バッファ上の区割り線取得に成功");
												splitLineList.addAll(intersectedList);
												if (i == 0) {
													roadJudgeResult.setDirection1SplitLineGetFlag(true);
												} else {
													roadJudgeResult.setDirection2SplitLineGetFlag(true);
												}
											} else {
												// B4: 取得できていない場合、最短距離で到達する区割り線を取得する
												LOGGER.debug("地番バッファ上の区割り線取得不可のため、最短距離で到達する区割り線をセット");
												if (splitLineListWithRoadCenterLine.size() > 0) {
													splitLineList.add(splitLineListWithRoadCenterLine.get(0));
												}
												if (i == 0) {
													roadJudgeResult.setDirection1SplitLineGetFlag(false);
												} else {
													roadJudgeResult.setDirection2SplitLineGetFlag(false);
												}
											}

										}
										// 道路中心位置が道路中心線の端部にある場合など、分割道路中心線が片方向しか取れていない場合、もう一方は取得失敗とする
										if (splitRoadCenterLineList.size() == 1) {
											roadJudgeResult.setDirection2SplitLineGetFlag(false);
										}
										// D: 取得した区割り線から最大・最小となる幅員値を持つ区割り線を判定
										int maxIndexValue = -1;
										int minIndexValue = -1;
										double maxRoadWidth = 0.0;
										double minRoadWidth = 10000.0;
										boolean widthErrorFlag = true;
										for (int i = 0; i < splitLineList.size(); i++) {
											if (splitLineList.get(i).getRoadWidth() == widthErrorCode
													|| splitLineList.get(i).getRoadwayWidth() == widthErrorCode) {
												// エラーコードの入っているデータは判定対象から除外し文言表示
												widthErrorFlag = false;
											} else {
												// 初期状態(0.0)時または最大値が取れた場合最大値を更新
												if (maxRoadWidth == 0.0 || splitLineList.get(i).getRoadWidth()
														.doubleValue() > maxRoadWidth) {
													maxRoadWidth = splitLineList.get(i).getRoadWidth();
													maxIndexValue = i;
												}
												// 初期状態(10000.0)時または最小値が取れた場合最小値を更新
												if (minRoadWidth == 10000.0 || splitLineList.get(i).getRoadWidth()
														.doubleValue() < minRoadWidth) {
													minRoadWidth = splitLineList.get(i).getRoadWidth();
													minIndexValue = i;
												}
											}

										}
										// D: 車道幅員、道路部幅員の最大・最小をセット
										if (maxIndexValue >= 0 && minIndexValue >= 0) {
											roadJudgeResult
													.setRoadMaxWidth(splitLineList.get(maxIndexValue).getRoadWidth());
											roadJudgeResult.setRoadWayMaxWidth(
													splitLineList.get(maxIndexValue).getRoadwayWidth());
											roadJudgeResult.setMaxWidthSplitLineObjectId(
													splitLineList.get(maxIndexValue).getObjectId());
											roadJudgeResult
													.setRoadMinWidth(splitLineList.get(minIndexValue).getRoadWidth());
											roadJudgeResult.setRoadWayMinWidth(
													splitLineList.get(minIndexValue).getRoadwayWidth());
											roadJudgeResult.setMinWidthSplitLineObjectId(
													splitLineList.get(minIndexValue).getObjectId());
											roadJudgeResult.setJudgeProcessResultFlag(true);
											// 幅員不明値有無をセット
											roadJudgeResult.setWidthErrorFlag(widthErrorFlag);
										} else {
											// 処理異常系
											// 区割り線が1件も取得できない場合に遷移
											LOGGER.debug("道路部幅員の最大・最小値の取得に失敗");
											if (!widthErrorFlag) {
												// 幅員値が全てエラー値の場合こちらに遷移
												LOGGER.debug("道路部幅員がすべてエラー値");
												roadJudgeResult.setWidthErrorFlag(false);
												roadJudgeResult.setJudgeProcessResultFlag(true);
											} else {
												// 区割り線が取得できなかった場合こちらに遷移
												LOGGER.debug("区割り線が取得されていない");
												roadJudgeResult.setJudgeProcessResultFlag(false);
												roadJudgeResult.setWidthErrorFlag(false);
											}

										}
									} catch (Exception e) {
										LOGGER.error("道路判定処理に失敗:" + e);
										roadJudgeResult.setDirection1SplitLineGetFlag(false);
										roadJudgeResult.setDirection2SplitLineGetFlag(false);
										roadJudgeResult.setJudgeProcessResultFlag(false);

										roadJudgeResult.setWidthErrorFlag(false);

									} finally {
										// オブジェクトID
										roadJudgeResult.setLod2ObjectId(lod2.getObjectId());
										// 路線番号
										roadJudgeResult.setLineNumber(lod2.getLineNumber());
										// 道路種別
										roadJudgeResult.setRoadType(lod2.getRoadType());
										// 区割り線
										roadJudgeResult.setSplitLineList(splitLineList);
										// 隣接歩道判定
										try {
											final List<RoadLod2> walkwayList = judgementLayerDao
													.getWalkWaysIntersectsRoadWay(lod2.getObjectId(),
															lod2.getLineNumber());
											if (walkwayList.size() > 0) {
												roadJudgeResult.setAdjacentWalkwayFlag(true);
												final List<Integer> walkwayObjectIdList = new ArrayList<Integer>();
												for (RoadLod2 walkway : walkwayList) {
													walkwayObjectIdList.add(walkway.getObjectId());
												}
												roadJudgeResult.setAdjacentWalkwayObjectIdList(walkwayObjectIdList);
											} else {
												roadJudgeResult.setAdjacentWalkwayFlag(false);
												roadJudgeResult
														.setAdjacentWalkwayObjectIdList(new ArrayList<Integer>());
											}
										} catch (Exception e) {
											roadJudgeResult.setAdjacentWalkwayFlag(false);
											roadJudgeResult.setAdjacentWalkwayObjectIdList(new ArrayList<Integer>());
										}
										LOGGER.debug("道路判定結果: " + //
												"道路LOD2objectId=" + roadJudgeResult.getLod2ObjectId() + //
												", 路線番号=" + roadJudgeResult.getLineNumber() + //
												", 道路種別=" + roadJudgeResult.getRoadType() + //
												", 判定処理結果=" + roadJudgeResult.getJudgeProcessResultFlag() + //
												", 道路部最大幅員=" + roadJudgeResult.getRoadMaxWidth() + //
												", 道路部最小幅員=" + roadJudgeResult.getRoadMinWidth() + //
												", 車道部最大幅員=" + roadJudgeResult.getRoadWayMaxWidth() + //
												", 車道部最小幅員=" + roadJudgeResult.getRoadWayMinWidth() + //
												", 方向1区割り線取得結果=" + roadJudgeResult.getDirection1SplitLineGetFlag() + //
												", 方向2区割り線取得結果=" + roadJudgeResult.getDirection2SplitLineGetFlag() + //
												", 隣接歩道判定結果=" + roadJudgeResult.getAdjacentWalkwayFlag() + //
												", 幅員取得結果=" + roadJudgeResult.getWidthErrorFlag() + //
												"");
										roadJudgeResultList.add(roadJudgeResult);
									}

								}
							} else {
								// 非該当
								LOGGER.debug("道路判定 非該当");
								gisJudgeResult = false;
							}
						} else {
							if (categoryJudgement.getJudgementLayer() != null) {
								// 判定対象レイヤ取得
								targetLayers = getLayers(categoryJudgement.getJudgementLayer());
								for (Layer targetLayer : targetLayers) {
									String layerTableName = targetLayer.getTableName();
									List<Oid> oidList;
									if (GIS_JUDGEMENT_1.equals(gisJudgement) || GIS_JUDGEMENT_2.equals(gisJudgement)) {
										LOGGER.debug("GIS重なり判定開始 区分判定ID=" + categoryJudgement.getJudgementItemId()
												+ " レイヤテーブル名=" + layerTableName);
										// 重なる・重ならない判定(1,2)
										oidList = judgementLayerDao.getIntersectsOid(lotNumberList, applicationId,
												layerTableName);
										layerOidMap.put(targetLayer.getLayerId(), oidList);
										if ((GIS_JUDGEMENT_1.equals(gisJudgement) && oidList.size() == 0)
												|| (GIS_JUDGEMENT_2.equals(gisJudgement) && oidList.size() > 0)) {
											gisJudgeResult = false;
											break;
										}
									} else if (GIS_JUDGEMENT_3.equals(gisJudgement)
											|| GIS_JUDGEMENT_4.equals(gisJudgement)) {
										LOGGER.debug("GISバッファ判定開始 区分判定ID=" + categoryJudgement.getJudgementItemId()
												+ " レイヤテーブル名=" + layerTableName);
										// バッファに重なる・重ならない判定(3,4)
										oidList = judgementLayerDao.getBufferIntersectsOid(lotNumberList, applicationId,
												layerTableName, epsg, categoryJudgement.getBuffer());
										layerOidMap.put(targetLayer.getLayerId(), oidList);
										if ((GIS_JUDGEMENT_3.equals(gisJudgement) && oidList.size() == 0)
												|| (GIS_JUDGEMENT_4.equals(gisJudgement) && oidList.size() > 0)) {
											gisJudgeResult = false;
											break;
										}
									} else {
										LOGGER.error("未対応のGIS判定コード: " + gisJudgement);
										throw new RuntimeException("未対応のGIS判定コード");
									}
									if (displayAttributeFlag != null
											&& !DISPLAY_ATTRIBUTE_NONE.equals(displayAttributeFlag)) {
										// 重なり属性表示フラグが設定されている
										if (oidList.size() > 0 && tableName != null && !tableName.isEmpty()
												&& fieldArray.length > 0) {
											// 該当表示文言用のデータを収集する
											for (int oidIdx = 0; oidIdx < oidList.size(); oidIdx++) {
												Oid tmpOid = oidList.get(oidIdx);
												List<String> values = new ArrayList<String>();
												for (int fieldIdx = 0; fieldIdx < fieldArray.length; fieldIdx++) {
													List<ColumnValue> columnValueList = judgementLayerDao
															.getColumnValue(tableName, fieldArray[fieldIdx],
																	tmpOid.getOid());
													if (columnValueList == null || columnValueList.size() != 1) {
														LOGGER.error("テーブル値の取得に失敗 テーブル名: " + tableName + ", カラム名: "
																+ fieldArray[fieldIdx] + ", OID: " + tmpOid.getOid());
														throw new RuntimeException("テーブル値の取得に失敗");
													}
													ColumnValue tmpValue = columnValueList.get(0);
													String tVal = (tmpValue != null) ? tmpValue.getVal()
															: attributeNullValue;
													values.add(tVal);
												}
												valuesList.add(values);
											}
										}
									}
								}
							}
						}
					}

					// 区分判定結果とGIS判定結果を結合
					if (isCatagoryJudgeExists && gisJudgeExists) {
						judgeResult = categoryJudgeResult && gisJudgeResult;
					} else if (!isCatagoryJudgeExists && gisJudgeExists) {
						judgeResult = gisJudgeResult;
					} else if (isCatagoryJudgeExists && !gisJudgeExists) {
						judgeResult = categoryJudgeResult;
					} else {
						judgeResult = false;
					}

					// 今回判定対象
					if (judgeResult) {
						if (isCatagoryJudgeExists && gisJudgeExists) {
							isnNewJudgementItem = executeCategoryJudgement && gisJudgeResult;
						} else if (!isCatagoryJudgeExists && gisJudgeExists) {
							isnNewJudgementItem = gisJudgeResult;
						} else if (isCatagoryJudgeExists && !gisJudgeExists) {
							isnNewJudgementItem = executeCategoryJudgement;
						}
					}

					// 許可判定の場合、前回存在すれば、判定対象とする
					if (APPLICATION_STEP_ID_3.equals(applicationStepId)) {
						judgeResult = isRegistedJudgementItem;
						// 一律追加条項リスト存在するするか判定
						for (String judgementId : defaultAddJudgementItemIdList) {
							if (judgementId.equals(categoryJudgement.getJudgementItemId())) {
								judgeResult = true;
							}
						}
					}
					LOGGER.debug("概況診断判定実行終了 区分判定ID=" + categoryJudgement.getJudgementItemId() + ", 区分判定有無="
							+ isCatagoryJudgeExists + ", GIS判定有無=" + gisJudgeExists + ", 区分判定結果=" + categoryJudgeResult
							+ ", GIS判定結果=" + gisJudgeResult + ", 判定結果=" + judgeResult);
				} else {
					// 地番が0件のときは判定を実施しない
					LOGGER.info("地番が0件のため、判定を実施しない");
				}

				if (judgeResult || categoryJudgement.getNonApplicableDisplayFlag()) {
					// 診断結果が「該当」または「非該当かつ 非該当時表示がtrue」のときデータを追加

					// GIS判定有無
					String gisJudgement = categoryJudgement.getGisJudgement();

					// 判定レイヤとの距離
					Double distance = 0.0;

					// 判定されていないレイヤーがある場合残りのGIS判定を行い、距離を算出
					// layerOidMapに保持されている場合保持結果を使用すること
					if (targetLayers != null) {
						for (Layer targetLayer : targetLayers) {
							String layerTableName = targetLayer.getTableName();
							List<Oid> oidList;
							if (!layerDistanceMap.containsKey(targetLayer.getLayerId())) {
								if (GIS_JUDGEMENT_1.equals(gisJudgement) || GIS_JUDGEMENT_2.equals(gisJudgement)) {
									// 重なる・重ならない判定(1,2)
									if (!layerOidMap.containsKey(targetLayer.getLayerId())) {
										LOGGER.debug("GIS重なり判定開始 区分判定ID=" + categoryJudgement.getJudgementItemId()
												+ " レイヤテーブル名=" + layerTableName);
										oidList = judgementLayerDao.getIntersectsOid(lotNumberList, applicationId,
												layerTableName);
									} else {
										oidList = layerOidMap.get(targetLayer.getLayerId());
									}
									if (oidList.size() == 0) {
										// 距離を算出
										List<Distance> distanceResult = judgementLayerDao.getDistance(lotNumberList,
												applicationId, layerTableName, distanceEpsg);
										if (distanceResult.size() > 0) {
											layerDistanceMap.put(targetLayer.getLayerId(), distanceResult.get(0));
											LOGGER.debug("距離を算出 区分判定ID=" + categoryJudgement.getJudgementItemId()
													+ " レイヤテーブル名=" + layerTableName + " 距離="
													+ distanceResult.get(0).getDistance());
										}

									}
								} else if (GIS_JUDGEMENT_3.equals(gisJudgement)
										|| GIS_JUDGEMENT_4.equals(gisJudgement)) {
									// バッファに重なる・重ならない判定(3,4)
									if (!layerOidMap.containsKey(targetLayer.getLayerId())) {
										LOGGER.debug("GISバッファ判定開始 区分判定ID=" + categoryJudgement.getJudgementItemId()
												+ " レイヤテーブル名=" + layerTableName);
										oidList = judgementLayerDao.getBufferIntersectsOid(lotNumberList, applicationId,
												layerTableName, epsg, categoryJudgement.getBuffer());
									} else {
										oidList = layerOidMap.get(targetLayer.getLayerId());
									}
									if (oidList.size() == 0) {
										// 距離を算出
										List<Distance> distanceResult = judgementLayerDao.getDistance(lotNumberList,
												applicationId, layerTableName, distanceEpsg);
										if (distanceResult.size() > 0) {
											layerDistanceMap.put(targetLayer.getLayerId(), distanceResult.get(0));
											LOGGER.debug("距離を算出 区分判定ID=" + categoryJudgement.getJudgementItemId()
													+ " レイヤテーブル名=" + layerTableName + " 距離="
													+ distanceResult.get(0).getDistance());
										}
									}
								}
							}
						}
					}
					// 全ての算出した距離を昇順でsortし該当すれば最短距離を更新
					if (layerDistanceMap.size() > 0) {
						List<Distance> layerDistanceList = new ArrayList<>(layerDistanceMap.values());
						if (layerDistanceList != null && layerDistanceList.size() > 0) {
							layerDistanceList.removeAll(Collections.singleton(null));
							layerDistanceList.sort(Comparator.comparing(Distance::getDistance));
							if (layerDistanceList.size() > 0 && layerDistanceList.get(0) != null
									&& layerDistanceList.get(0).getDistance() >= 0) {
								distance = layerDistanceList.get(0).getDistance();
							}
						}
					}

					// レイヤリスト取得（判定該当時または非該当時かつ非該当同時レイヤ表示ありの場合）
					List<LayerForm> layers = new ArrayList<LayerForm>();
					if (judgeResult || !judgeResult && categoryJudgement.getNonApplicableLayerDisplayFlag()) {
						// 判定対象レイヤ取得
						if (targetLayers != null) {
							layers.addAll(getLayerForms(targetLayers));
						} else if (categoryJudgement.getJudgementLayer() != null) {
							layers.addAll(getLayerForms(getLayers(categoryJudgement.getJudgementLayer())));
						}

						if (categoryJudgement.getSimultaneousDisplayLayer() != null) {
							// 同時表示レイヤ取得
							layers.addAll(getLayerForms(getLayers(categoryJudgement.getSimultaneousDisplayLayer())));
						}
					}

					// 該当表示文言を取得
					String applicableDescription = categoryJudgement.getApplicableDescription();
					// 非該当時は非該当表示文言（非該当表示概要）で該当表示文言（該当表示概要）を上書きする
					if (!judgeResult) {
						if (categoryJudgement.getNonApplicableDescription() != null) {
							applicableDescription = categoryJudgement.getNonApplicableDescription();
						} else {
							applicableDescription = "";
						}
						if (categoryJudgement.getNonApplicableSummary() != null) {
							categoryJudgement.setApplicableSummary(categoryJudgement.getNonApplicableSummary());
						} else {
							categoryJudgement.setApplicableSummary("");
						}
					}
					if (GIS_JUDGEMENT_5.equals(gisJudgement) && judgeResult) {
						// GIS判定が「道路判定」かつ該当の場合、取得された判定図形単位でformを付加する
						judgeResultItemId = setRoadJudgeResultToGeneralConditionDiagnosisResultForm(formList,
								roadJudgeResultList, categoryJudgement, generalConditionDiagnosisId, layers,
								judgeResultItemId, lotNumberList, applicationId, isnNewJudgementItem,
								generalConditionDiagnosisRequestFrom);
					} else {
						// 該当表示文言の距離表示箇所を置き換え
						int intDistance = 0;
						String distanceText = "";
						String distanceResultText = "";
						if (targetLayers != null && distance != null) {
							intDistance = (int) Double.parseDouble(distance.toString());
							if (intDistance > 0) {
								distanceResultText = intDistance + "m" + "";
								distanceText = distanceReplacedText + String.format("%,d", intDistance) + "m";
							} else {
								distanceResultText = distanceApplicationAreaText + "";
								distanceText = distanceReplacedText + distanceApplicationAreaText;
							}
							applicableDescription = applicableDescription.replace(distanceReplaceText, distanceText);
						}
						// 置換後文字列で上書き
						categoryJudgement.setApplicableDescription(applicableDescription);
						if (displayAttributeFlag != null && DISPLAY_ATTRIBUTE_NEWROW.equals(displayAttributeFlag)) {
							// 重なり属性表示フラグが「行を分けて表示」の場合、取得された判定図形単位でformを付加する
							judgeResultItemId = setSeparatedJudgeResultToGeneralConditionDiagnosisResultForm(formList,
									valuesList, categoryJudgement, judgeResult, generalConditionDiagnosisId, layers,
									judgeResultItemId, distanceResultText, isnNewJudgementItem,
									generalConditionDiagnosisRequestFrom);
						} else {
							if (displayAttributeFlag != null && !DISPLAY_ATTRIBUTE_NONE.equals(displayAttributeFlag)) {
								// 重なり属性表示フラグが設定されているので、該当表示文言を置換する
								if (DISPLAY_ATTRIBUTE_JOINT.equals(displayAttributeFlag)) {
									// 重なり属性表示: 連結
									applicableDescription = replaceDescriptionJoint(applicableDescription, valuesList);
								} else if (DISPLAY_ATTRIBUTE_NEWLINE.equals(displayAttributeFlag)) {
									// 重なり属性表示: 改行リピート
									applicableDescription = replaceDescriptionRepeat(applicableDescription, valuesList);
								} else {
									LOGGER.error("未対応の重なり属性フラグ: " + displayAttributeFlag);
									throw new RuntimeException("未対応の重なり属性フラグ");
								}
								// 置換後文字列で上書き
								categoryJudgement.setApplicableDescription(applicableDescription);
							}
							// 判定結果の一行だけなので、インデックスを0とする
							String dataType = getDataType(isnNewJudgementItem, generalConditionDiagnosisRequestFrom,
									categoryJudgement, 0);
							// 道路判定以外は建物表示有効
							formList.add(getGeneralConditionDiagnosisResultFormFromEntity(categoryJudgement,
									judgeResult, layers, generalConditionDiagnosisId, distanceResultText,
									judgeResultItemId, true, dataType));
							judgeResultItemId++;
						}
					}
				}
			}
			return formList;
		} finally {
			LOGGER.debug("概況診断結果取得 終了");
		}
	}

	/**
	 * 概況診断結果レポート帳票生成
	 * 
	 * @param generalConditionDiagnosisReportRequestForm リクエストパラメータ
	 * @return 生成帳票
	 */
	public boolean generateJudgeReportWorkBook(
			GeneralConditionDiagnosisReportRequestForm generalConditionDiagnosisReportRequestForm,
			HttpServletResponse response) {
		LOGGER.debug("概況診断結果レポート帳票生成 開始");
		try {
			// 概況診断レポートを生成
			Workbook wb = null;
			// 申請IDがある場合、申請フォルダに配置
			if (generalConditionDiagnosisReportRequestForm.getApplicationId() != null
					&& generalConditionDiagnosisReportRequestForm.getApplicationId().intValue() > 0) {

				final Map<Integer, Map<String, String>> generalConditionAnswerMap = new HashMap<Integer, Map<String, String>>();
				List<GeneralConditionDiagnosisResultForm> conditionList = generalConditionDiagnosisReportRequestForm
						.getGeneralConditionDiagnosisResults();
				if (conditionList.size() < 1) {
					LOGGER.error("概況診断結果が存在しない");
					throw new RuntimeException("概況診断結果が存在しません");
				}
				Integer applicationStepId = conditionList.get(0).getApplicationStepId();
				for (GeneralConditionDiagnosisResultForm condition : conditionList) {
					List<Answer> answerList = answerRepository.findAnswerByJudgementResult(
							generalConditionDiagnosisReportRequestForm.getApplicationId(), applicationStepId,
							condition.getJudgementId(), condition.getJudgementResultIndex());
					if (answerList.size() < 1) {
						LOGGER.warn("回答が存在しない 判定項目ID：" + condition.getJudgementId());
						continue;
					}
					Answer answer = answerList.get(0);
					Map<String, String> answerContentMap = new HashMap<String, String>();
					answerContentMap.put("answerId", answer.getAnswerId().toString());
					answerContentMap.put("answerContent", answer.getNotifiedText());
					generalConditionAnswerMap.put(condition.getJudgeResultItemId(), answerContentMap);
				}
				generalConditionDiagnosisReportRequestForm.setAnswerJudgementMap(generalConditionAnswerMap);
				wb = exportJudgeReportWorkBook(generalConditionDiagnosisReportRequestForm);
				if (wb == null) {
					return false;
				}
				LOGGER.trace("概況診断レポートアップロード 開始");
				Integer applicationId = generalConditionDiagnosisReportRequestForm.getApplicationId();
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
				applicationService.uploadApplicationFile(uploadForm, wb);
				LOGGER.trace("概況診断レポートアップロード 終了");
			} else {
				wb = exportJudgeReportWorkBook(generalConditionDiagnosisReportRequestForm);
				if (wb == null) {
					return false;
				}
			}
			// 一時フォルダに配置
			String absoluteFolderPath = judgementFolderPath + PATH_SPLITTER
					+ generalConditionDiagnosisReportRequestForm.getFolderName();
			Path checkPath = Paths.get(absoluteFolderPath);
			if (!Files.exists(checkPath)) {
				LOGGER.error("指定フォルダが存在しない: " + absoluteFolderPath);
				throw new RuntimeException("指定フォルダが存在しません");
			}
			exportWorkBook(wb,
					absoluteFolderPath + PATH_SPLITTER + generalConditionDiagnosisReportRequestForm.getFileName());
			return true;
		} catch (Exception ex) {
			LOGGER.error("概況診断結果レポート帳票生成で例外発生", ex);
			return false;
		} finally {
			LOGGER.debug("概況診断結果レポート帳票生成 終了");
		}
	}

	/**
	 * 概況診断結果レポート帳票出力
	 * 
	 * @param generalConditionDiagnosisReportRequestForm リクエストパラメータ
	 * @return 生成帳票
	 * @throws Exception 例外
	 */
	public boolean exportJudgeReportWorkBook(
			GeneralConditionDiagnosisReportRequestForm generalConditionDiagnosisReportRequestForm,
			HttpServletResponse response) {
		LOGGER.debug("概況診断結果レポート帳票出力 開始");
		try {
			if (generalConditionDiagnosisReportRequestForm.getFolderName() == null
					|| generalConditionDiagnosisReportRequestForm.getFolderName().equals("")) {
				Workbook wb = exportJudgeReportWorkBook(generalConditionDiagnosisReportRequestForm);

				if (wb != null) {
					try (OutputStream os = response.getOutputStream()) {
						// ファイルサイズ測定
						int fileSize = -1;
						try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
							wb.write(byteArrayOutputStream);
							fileSize = byteArrayOutputStream.size();
						}

						// 帳票ダウンロード出力
						LOGGER.debug(judgeReportFileName);
						LOGGER.debug(fileSize + "");
						response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
						response.setHeader("Content-Disposition", "attachment; filename=" + judgeReportFileName);
						response.setContentLength(fileSize);
						wb.write(os);
						os.flush();
					}
				} else {
					return false;
				}
			} else {
				// 絶対ファイルパス
				String absoluteFolderPath = judgementFolderPath + PATH_SPLITTER
						+ generalConditionDiagnosisReportRequestForm.getFolderName();
				Path checkPath = Paths.get(absoluteFolderPath);
				if (!Files.exists(checkPath)) {
					LOGGER.error("指定フォルダが存在しない: " + absoluteFolderPath);
					throw new RuntimeException("指定フォルダが存在しません");
				}
				List<String> xlsxFileNames = findXlsxFileNames(absoluteFolderPath);
				if (xlsxFileNames == null || xlsxFileNames.size() < 1) {
					LOGGER.error("概況診断レポートが存在しない: " + absoluteFolderPath);
					throw new RuntimeException("概況診断レポートが存在しません");
				}
				String filePath = absoluteFolderPath + PATH_SPLITTER + xlsxFileNames.get(0);
				Resource resource = new PathResource(filePath);
				response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
				response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
						"attachment; filename=\"" + resource.getFilename() + "\"");
				// ファイルの内容をバイト配列として取得
				byte[] fileBytes = Files.readAllBytes(resource.getFile().toPath());
				// レスポンスの出力ストリームにバイト配列を書き込む
				response.getOutputStream().write(fileBytes);
				response.getOutputStream().flush();
			}

			// 一時フォルダーの削除処理
			if (generalConditionDiagnosisReportRequestForm.getFolderName() != null
					&& !generalConditionDiagnosisReportRequestForm.getFolderName().equals("")) {
				deleteTmpFolder(generalConditionDiagnosisReportRequestForm);
			}

			return true;
		} catch (Exception ex) {
			LOGGER.error("概況診断結果レポート帳票出力で例外発生", ex);
			return false;
		} finally {
			LOGGER.debug("概況診断結果レポート帳票出力 終了");
		}
	}

	/**
	 * 概況診断結果レポート進捗状況取得
	 * 
	 * @param generalConditionDiagnosisReportRequestFormList リクエストパラメータ
	 * @return generalConditionDiagnosisReportProgressFormList
	 */
	public List<GeneralConditionDiagnosisReportProgressForm> getGeneralConditionDiagnosisReportProgress(
			List<GeneralConditionDiagnosisReportRequestForm> generalConditionDiagnosisReportRequestFormList) {
		LOGGER.debug("概況診断結果レポート進捗状況取得 開始");
		List<GeneralConditionDiagnosisReportProgressForm> generalConditionDiagnosisReportProgressFormList = new ArrayList<GeneralConditionDiagnosisReportProgressForm>();
		try {
			for (GeneralConditionDiagnosisReportRequestForm generalConditionDiagnosisReportRequestForm : generalConditionDiagnosisReportRequestFormList) {
				GeneralConditionDiagnosisReportProgressForm generalConditionDiagnosisReportProgressForm = new GeneralConditionDiagnosisReportProgressForm();
				// 絶対ファイルパス
				String absoluteFolderPath = judgementFolderPath + PATH_SPLITTER
						+ generalConditionDiagnosisReportRequestForm.getFolderName();
				Path folderPath = Paths.get(absoluteFolderPath);
				if (Files.exists(folderPath)) {
					int counts = countFoldersAndFiles(new File(absoluteFolderPath));
					generalConditionDiagnosisReportProgressForm
							.setFolderName(generalConditionDiagnosisReportRequestForm.getFolderName());
					generalConditionDiagnosisReportProgressForm.setCapturedCount(counts);
					List<File> xlsxFiles = findXlsxFiles(absoluteFolderPath);
					if (xlsxFiles != null && xlsxFiles.size() > 0) {
						File xlsxFile = xlsxFiles.get(0);
						if (xlsxFile != null) {
							// ファイルサイズを取得する（バイト単位）
							long fileSizeBytes = xlsxFile.length();
							// バイトをメガバイトに変換する
							double fileSizeMB = (double) fileSizeBytes / (1024 * 1024);
							// 小数点1位まで表示するためのフォーマットを設定する
							DecimalFormat df = new DecimalFormat("#");
							String formattedFileSizeMB = df.format(fileSizeMB) + "MB";
							generalConditionDiagnosisReportProgressForm.setFileSize(formattedFileSizeMB);
						}
					}
					generalConditionDiagnosisReportProgressFormList.add(generalConditionDiagnosisReportProgressForm);
				}
			}
			return generalConditionDiagnosisReportProgressFormList;
		} catch (Exception ex) {
			LOGGER.error("概況診断結果レポート進捗状況取得で例外発生", ex);
			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		} finally {
			LOGGER.debug("概況診断結果レポート進捗状況取得 終了");
		}
	}

	/**
	 * 一時フォルダ生成(同時アクセスで奇跡的にフォルダ名が被る可能性があるためsynchronizedとしておく)
	 * 
	 * @return 概況診断画像アップロードフォーム
	 */
	public synchronized UploadForGeneralConditionDiagnosisForm getFolderName() {
		LOGGER.debug("一時フォルダ生成 開始");
		try {
			UploadForGeneralConditionDiagnosisForm form = new UploadForGeneralConditionDiagnosisForm();

			boolean createFlg = false;
			while (!createFlg) {
				// 一時フォルダ名は「ランダム文字列_日時文字列」とする
				String randomText = AuthUtil.generatePassword(JUDGEMENT_FOLDER_CHARACTERS, JUDGEMENT_FOLDER_LENGTH);
				SimpleDateFormat df = new SimpleDateFormat(JUDGEMENT_FOLDER_DATE_FORMAT);
				String timeText = df.format(new Date());
				String folderName = randomText + "_" + timeText;

				// 一時フォルダ生成
				String absoluteFolderPath = judgementFolderPath;
				absoluteFolderPath += PATH_SPLITTER + folderName;

				Path directoryPath = Paths.get(absoluteFolderPath);
				if (!Files.exists(directoryPath)) {
					// フォルダがないので生成
					LOGGER.debug("フォルダ生成: " + directoryPath);
					Files.createDirectories(directoryPath);
					form.setFolderName(folderName);
					createFlg = true;
				} else {
					LOGGER.info("フォルダが存在するため生成を再試行: " + directoryPath);
				}
			}

			return form;
		} catch (Exception ex) {
			// RuntimeExceptionで投げないとロールバックされない
			LOGGER.error("一時フォルダ生成で例外発生", ex);
			throw new RuntimeException(ex);
		} finally {
			LOGGER.debug("一時フォルダ生成 終了");
		}
	}

	/**
	 * 概況診断画像アップロード
	 * 
	 * @param uploadForGeneralConditionDiagnosisForm 画像情報
	 */
	public void uploadImageFile(UploadForGeneralConditionDiagnosisForm uploadForGeneralConditionDiagnosisForm) {
		LOGGER.debug("概況診断画像アップロード 開始");
		try {
			String folderName = uploadForGeneralConditionDiagnosisForm.getFolderName();
			boolean currentSituationMapFlg = uploadForGeneralConditionDiagnosisForm.getCurrentSituationMapFlg();
			String judgeResultItemId = (uploadForGeneralConditionDiagnosisForm.getJudgeResultItemId() != null)
					? uploadForGeneralConditionDiagnosisForm.getJudgeResultItemId().toString()
					: null;

			String absoluteFolderPath = judgementFolderPath;
			absoluteFolderPath += PATH_SPLITTER + folderName;

			Path directoryPath = Paths.get(absoluteFolderPath);
			if (!Files.exists(directoryPath)) {
				LOGGER.error("指定フォルダが存在しない: " + absoluteFolderPath);
				throw new RuntimeException("指定フォルダが存在しません");
			}

			if (!currentSituationMapFlg) {
				if (judgeResultItemId == null) {
					throw new RuntimeException("判定項目IDがnull");
				}
				absoluteFolderPath += PATH_SPLITTER + judgeResultItemId;
			}
			directoryPath = Paths.get(absoluteFolderPath);
			if (!Files.exists(directoryPath)) {
				// フォルダがないので生成
				LOGGER.debug("フォルダ生成: " + directoryPath);
				Files.createDirectories(directoryPath);
			}

			String absoluteFilePath = absoluteFolderPath + PATH_SPLITTER;
			if (currentSituationMapFlg) {
				absoluteFilePath += ExportJudgeForm.OVERVIEW_FILE_NAME;
			} else {
				absoluteFilePath += judgeResultItemId + ExportJudgeForm.JUDGEMENT_IMAGE_EXTENTION;
			}
			LOGGER.trace("ファイル出力 開始");
			exportFile(uploadForGeneralConditionDiagnosisForm.getImage(), absoluteFilePath);
			LOGGER.trace("ファイル出力 終了");

		} catch (Exception ex) {
			// RuntimeExceptionで投げないとロールバックされない
			LOGGER.error("概況診断画像アップロードで例外発生", ex);
			throw new RuntimeException(ex);
		} finally {
			LOGGER.debug("概況診断画像アップロード 終了");
		}
	}

	/**
	 * 区分判定処理
	 * 
	 * @param categoryMap       申請区分
	 * @param categoryJudgement 区分判定リスト
	 * @return 判定結果
	 */
	private boolean executeCategoryJudgement(Map<String, Set<String>> categoryMap,
			CategoryJudgementAndResult categoryJudgement) {

		String itemId = categoryJudgement.getJudgementItemId();
		List<ApplicationCategoryJudgement> judgelist = applicationCategoryJudgementRepository
				.getAppCategoryJudgeListforItemId(itemId);
		if (judgelist == null)
			return false;
		boolean res = false;
		// 画面ID単位で集約
		final Map<String, List<String>> judgeListMap = new HashMap<String, List<String>>();
		for (ApplicationCategoryJudgement judge : judgelist) {
			if (judgeListMap.containsKey(judge.getViewId())) {
				judgeListMap.get(judge.getViewId()).add(judge.getCategoryId());
			} else {
				final List<String> categoryList = new ArrayList<String>();
				categoryList.add(judge.getCategoryId());
				judgeListMap.put(judge.getViewId(), categoryList);
			}
		}
		final List<Boolean> resultList = new ArrayList<Boolean>();
		for (String keycontent : categoryMap.keySet()) {
			// 区分判定チェックを行う画面IDか否かを判定
			if (judgeListMap.containsKey(keycontent)) {
				boolean aResult = false;
				final List<String> judgeCategoryList = judgeListMap.get(keycontent);
				for (String aCategoryId : judgeCategoryList) {
					// 区分判定リストに含まれるか否かを判定（OR判定）
					if (categoryMap.get(keycontent).contains(aCategoryId)) {
						aResult = true;
						break;
					}
				}
				// 判定結果リストに追加
				resultList.add(aResult);
			}
		}
		// 判定結果をANDで集約
		if (resultList.size() > 0) {
			boolean finalRes = true;
			for (Boolean aRes : resultList) {
				if (!aRes) {
					finalRes = false;
					break;
				}
			}
			return finalRes;
		} else {
			return false;
		}
	}

	/**
	 * 区分判定の有無を返す.
	 * 
	 * @param categoryJudgement 区分判定
	 * @return
	 */
	private boolean isCategoryJudgeExists(CategoryJudgementAndResult categoryJudgement, Integer applicationStepId) {

		String itemId = categoryJudgement.getJudgementItemId();
		List<ApplicationCategoryJudgement> judgelist = applicationCategoryJudgementRepository
				.getAppCategoryJudgeListforItemId(itemId);

		// 許可判定の場合、申請区分がないため、 M_申請区分_区分判定に存在するかチェックをおこなわない
		if (APPLICATION_STEP_ID_3.equals(applicationStepId)) {
			return true;
		}
		if (judgelist == null)
			return false;
		for (ApplicationCategoryJudgement judge : judgelist) {
			if (!judge.getCategoryId().equals(CATEGORY_NONE)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 再申請判断対象であるか判定
	 * 
	 * @param categoryJudgement
	 * @param generalConditionDiagnosisRequestFrom
	 * @param judgementResultIndex                 判定結果のインデックス
	 * 
	 * @return
	 */
	private boolean isReapplicationJudgementItem(CategoryJudgementAndResult categoryJudgement,
			GeneralConditionDiagnosisRequestForm generalConditionDiagnosisRequestFrom) {

		// 前回の申請段階IDが空である場合、初回の申請とする
		if (generalConditionDiagnosisRequestFrom.getPreApplicationStepId() == null) {
			return true;
		} else {

			String itemId = categoryJudgement.getJudgementItemId();
			String departmentId = categoryJudgement.getDepartmentId();
			Integer applicationId = generalConditionDiagnosisRequestFrom.getApplicationId();
			Integer applicationStepId = generalConditionDiagnosisRequestFrom.getApplicationStepId();
			Integer preApplicationStepId = generalConditionDiagnosisRequestFrom.getPreApplicationStepId();
			Integer acceptVersionInformation = generalConditionDiagnosisRequestFrom.getAcceptVersionInformation();

			// 事前協議⇒事前協議の場合、受付された版情報が0の場合、事前相談の条項を参照
			if (APPLICATION_STEP_ID_2.equals(preApplicationStepId) && APPLICATION_STEP_ID_2.equals(applicationStepId)
					&& acceptVersionInformation.equals(0)) {
				preApplicationStepId = APPLICATION_STEP_ID_1;
			}

			List<Answer> answerList = answerRepository.findByJudgementId(applicationId, preApplicationStepId, itemId);

			// 該当条項は前回の申請に存在するか判断
			if (answerList == null || answerList.size() == 0) {

				// 事前協議 ⇒ 許可判定
				if (APPLICATION_STEP_ID_2.equals(preApplicationStepId)
						&& APPLICATION_STEP_ID_3.equals(applicationStepId)) {
					// 一律追加条項リスト存在するするか判定
					for (String judgementItemId : defaultAddJudgementItemIdList) {
						if (itemId.equals(judgementItemId)) {
							return true;
						}
					}
					return false;
				} else {
					return false;
				}
			} else {
				// 段階変わらないの再申請
				if (preApplicationStepId.equals(applicationStepId)) {
					// 事前相談⇒事前相談
					if (APPLICATION_STEP_ID_1.equals(applicationStepId)) {

						boolean isExecution = false;

						for (Answer answer : answerList) {
							// 未回答の回答がある場合、該当判定項目は判断実施対象とする
							if (answer.getBusinessReApplicationFlag() == null) {
								isExecution = true;
							} else {
								// 再申請要の場合、判断実施対象とする
								if (answer.getBusinessReApplicationFlag()) {
									isExecution = true;
								}
							}
						}

						return isExecution;

					}

					// 事前協議⇒事前協議
					if (APPLICATION_STEP_ID_2.equals(applicationStepId)) {
						for (Answer answer : answerList) {
							if (departmentId.equals(answer.getDepartmentId())) {
								return true;
							}
						}
					}

					// 許可判定⇒許可判定
					if (APPLICATION_STEP_ID_3.equals(applicationStepId)) {
						return true;
					}
				} else {

					// 事前相談⇒事前協議
					if (APPLICATION_STEP_ID_2.equals(applicationStepId)) {

						boolean isExecution = false;

						for (Answer answer : answerList) {
							// 要事前協議の回答がある場合、該当判定項目は判断実施対象とする
							if (answer.getDiscussionFlag() != null && answer.getDiscussionFlag()) {
								isExecution = true;
							}
						}

						return isExecution;
					}

					// 事前協議⇒許可判定
					if (APPLICATION_STEP_ID_3.equals(applicationStepId)) {
						List<DepartmentAnswer> departmentAnswer = departmentAnswerRepository
								.findByDepartmentAnswerId(answerList.get(0).getDepartmentAnswerId());

						// 事前協議の中に、この条項に対する部署確定登録ステータスが取下であるか判定
						if (departmentAnswer.size() > 0) {
							// 部署の行政確定が取下である場合、該当部署の全ての条項が許可判定に引継しない
							if (GOVERNMENT_CONFIRM_STATUS_1_WITHDRAW
									.equals(departmentAnswer.get(0).getGovernmentConfirmStatus())) {
								return false;
							}
						}

						boolean isExecution = false;
						for (Answer answer : answerList) {
							// 許可判定移行フラグ（0：許可判定移行時チェック）かつ、行政確定ステータス（0:合意）の条項が判定実施対象にする
							if ((answer.getPermissionJudgementMigrationFlag() == null
									|| !answer.getPermissionJudgementMigrationFlag())
									&& (GOVERNMENT_CONFIRM_STATUS_0_AGREE
											.equals(answer.getGovernmentConfirmStatus()))) {
								isExecution = true;
							}
						}

						return isExecution;

					} else {
						return true;
					}

				}
			}
		}

		return true;
	}

	/**
	 * 条項を比較して、データ種類を設定
	 * 
	 * @param isnNewJudgementItem                  今回判定対象
	 * @param generalConditionDiagnosisRequestFrom 概況診断実施のリクエスト
	 * @param categoryJudgement                    M_判定結果
	 * @param judgementResultIndex                 判定結果のインデックス
	 * @return
	 */
	private String getDataType(boolean isnNewJudgementItem,
			GeneralConditionDiagnosisRequestForm generalConditionDiagnosisRequestFrom,
			CategoryJudgementAndResult categoryJudgement, Integer judgementResultIndex) {

		String dataType = EMPTY;
		// 「非該当かつ 非該当時表示がtrue」の判定項目は今回判定対象とする
		if (categoryJudgement.getNonApplicableDisplayFlag() || isnNewJudgementItem) {
			isnNewJudgementItem = true;
		}

		// 初回申請
		if (generalConditionDiagnosisRequestFrom.getApplicationId() == null) {
			dataType = ANSWER_DATA_TYPE_INSERT;
		} else {

			Integer applicationId = generalConditionDiagnosisRequestFrom.getApplicationId();
			Integer applicationStepId = generalConditionDiagnosisRequestFrom.getApplicationStepId();
			Integer preApplicationStepId = generalConditionDiagnosisRequestFrom.getPreApplicationStepId();
			Integer acceptVersionInformation = generalConditionDiagnosisRequestFrom.getAcceptVersionInformation();

			// 事前協議⇒事前協議の場合、受付された版情報が0の場合、事前相談の条項を参照
			if (APPLICATION_STEP_ID_2.equals(preApplicationStepId) && APPLICATION_STEP_ID_2.equals(applicationStepId)
					&& acceptVersionInformation.equals(0)) {
				preApplicationStepId = APPLICATION_STEP_ID_1;
			}

			String judgementItemId = categoryJudgement.getJudgementItemId();
			String departmentId = categoryJudgement.getDepartmentId();

			boolean isExist = false;
			List<Answer> answerList = answerRepository.findAnswerByJudgementResult(applicationId, preApplicationStepId,
					judgementItemId, judgementResultIndex);
			Answer answer = null;
			if (answerList.size() > 0) {

				isExist = true;
				answer = answerList.get(0);
				// 事前協議の場合、判定項目は部署より、複数行がある可能
				if (APPLICATION_STEP_ID_2.equals(preApplicationStepId)
						&& APPLICATION_STEP_ID_2.equals(applicationStepId)) {
					for (Answer ans : answerList) {
						if (departmentId.equals(ans.getDepartmentId())) {
							isExist = true;
							answer = ans;
							break;
						}
					}
				} else {
					isExist = true;
					answer = answerList.get(0);
				}

			}
			if (preApplicationStepId.equals(applicationStepId)) {
				// 事前相談⇒事前相談
				if (APPLICATION_STEP_ID_1.equals(applicationStepId)) {

					// 前回申請時にある
					if (isExist) {
						// 再申請時にある
						if (isnNewJudgementItem) {
							// 前回が再申請不要の場合、「削除済み」とする
							if (answer.getBusinessReApplicationFlag() != null
									&& !answer.getBusinessReApplicationFlag()) {
								dataType = ANSWER_DATA_TYPE_DELETE;
							} else {
								dataType = ANSWER_DATA_TYPE_UPDATE;
							}
						} else {
							// 前回が未回答、要再申請の場合、「削除済み」とする
							if (answer.getBusinessReApplicationFlag() == null) {
								dataType = ANSWER_DATA_TYPE_DELETE;
							} else {
								if (answer.getBusinessReApplicationFlag()) {
									dataType = ANSWER_DATA_TYPE_DELETE;
								} else {

								}
							}
						}
					} else {
						// 前回申請時になし、再申請時にある
						dataType = ANSWER_DATA_TYPE_ADD;
					}
				}
				// 事前協議⇒事前協議
				if (APPLICATION_STEP_ID_2.equals(applicationStepId)) {
					// 前回申請時にある
					if (isExist) {
						// 再申請時にある
						if (isnNewJudgementItem) {
							if (ANSWER_DATA_TYPE_GOVERNMENT_DELETE.equals(answer.getAnswerDataType())) {
								dataType = ANSWER_DATA_TYPE_GOVERNMENT_DELETE;
							} else {
								dataType = ANSWER_DATA_TYPE_UPDATE;
							}
						} else {
							if (ANSWER_DATA_TYPE_GOVERNMENT_DELETE.equals(answer.getAnswerDataType())) {
								dataType = ANSWER_DATA_TYPE_GOVERNMENT_DELETE;
							} else {
								dataType = ANSWER_DATA_TYPE_DELETE;
							}
						}
					} else {
						// 前回申請時になし、再申請時にある
						dataType = ANSWER_DATA_TYPE_ADD;
					}
				}
				// 許可判定⇒許可判定：申請区分が変わらないため、概況診断実施不要

			} else {
				// 事前相談⇒事前協議
				if (APPLICATION_STEP_ID_2.equals(applicationStepId)) {
					// 前回申請時にある
					if (isExist) {
						// 再申請時にある
						if (isnNewJudgementItem) {
							// 前回が事前協議要の場合、
							if (answer.getDiscussionFlag() != null && answer.getDiscussionFlag()) {
								dataType = ANSWER_DATA_TYPE_UPDATE;
							} else {
								dataType = ANSWER_DATA_TYPE_DELETE;
							}
						} else {
							// 前回が要事前協議の場合、「引継」とする
							if (answer.getDiscussionFlag() != null && answer.getDiscussionFlag()) {
								dataType = ANSWER_DATA_TYPE_TAKEOVER;
							}
						}
					} else {
						// 前回申請時になし、再申請時にある
						dataType = ANSWER_DATA_TYPE_ADD;
					}
				}
				// 事前協議⇒許可判定
				if (APPLICATION_STEP_ID_3.equals(applicationStepId)) {
					// 申請区分選択がないため、再申請時の申請区分の判断がいらない
					// 前回申請時にある
					if (isExist) {
						dataType = ANSWER_DATA_TYPE_UPDATE;
					} else {
						// 一律追加条項リスト存在するするか判定
						for (String judgementId : defaultAddJudgementItemIdList) {
							if (judgementId.equals(categoryJudgement.getJudgementItemId())) {
								dataType = ANSWER_DATA_TYPE_UNIFORM;
							}
						}
					}
				}
			}

		}
		return dataType;
	}

	/**
	 * 申請区分リストに指定のコードが含まれるか判定, 旧版により不使用
	 * 
	 * @param categoryList 申請区分リスト
	 * @param codes        区分コード(カンマ区切りのコード文字列)
	 * @return 判定結果
	 */
	private boolean isContainsCategoryCodes(Set<String> categorySet, String codes) {
		if (codes != null && !EMPTY.equals(codes)) {
			// 申請区分リストにコードが含まれるか判定
			String[] codeArray = codes.split(COMMA);
			for (String code : codeArray) {
				String tmpCode = code.trim();
				if (!EMPTY.equals(tmpCode)) {
					if (categorySet.contains(tmpCode)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * レイヤリスト取得
	 * 
	 * @param layerIds レイヤID(カンマ区切りの文字列)
	 * @return レイヤリスト
	 */
	private List<Layer> getLayers(String layerIds) {
		LayerDao layerDao = new LayerDao(emf);
		if (layerIds != null) {
			// レイヤID集約
			List<String> layerIdList = new ArrayList<String>();
			String[] layerIdArray = layerIds.split(COMMA);
			for (String layerId : layerIdArray) {
				if (!EMPTY.equals(layerId)) {
					if (!layerIdList.contains(layerId)) {
						layerIdList.add(layerId);
					}
				}
			}

			if (layerIdList.size() > 0) {
				// レイヤ情報取得
				List<Layer> layerList = layerDao.getLayers(layerIdList);

				// layerRepositoryで取得を何度か繰り返すと、何故か処理の先でUPDATE文が実行されてエラーになるので、DAOで実行する
				// List<Layer> layerList = layerRepository.getLayers(layerIdList);

				return layerList;
			}
		}
		return new ArrayList<Layer>();
	}

	/**
	 * レイヤフォームリスト取得
	 * 
	 * @param layerList レイヤリスト
	 * @return レイヤフォームリスト
	 */
	private List<LayerForm> getLayerForms(List<Layer> layerList) {
		List<LayerForm> layerFormList = new ArrayList<LayerForm>();
		if (layerList != null) {
			for (Layer layer : layerList) {
				layerFormList.add(getLayerFormFromEntity(layer));
			}
		}
		return layerFormList;
	}

	/**
	 * M_区分判定EntityをM_区分判定フォームに詰めなおす
	 * 
	 * @param entity M_区分判定、M_判定結果Entity
	 * @return M_区分判定フォーム
	 */
	private GeneralConditionDiagnosisResultForm getGeneralConditionDiagnosisResultFormFromEntity(
			CategoryJudgementAndResult entity, boolean judgeResult, List<LayerForm> layers,
			int generalConditionDiagnosisId, String distanceText, int judgeResultItemId, boolean buildingDisplayFlag,
			String dataType) {
		GeneralConditionDiagnosisResultForm form = new GeneralConditionDiagnosisResultForm();
		form.setJudgementId(entity.getJudgementItemId());
		form.setTitle(entity.getTitle());
		form.setResult(judgeResult);
		form.setSummary(entity.getApplicableSummary());
		form.setDescription(entity.getApplicableDescription());
		form.setJudgementLayerDisplayFlag(entity.getNonApplicableLayerDisplayFlag());
		form.setSimultameousLayerDisplayFlag(entity.getSimultaneousDisplayLayerFlag());
		form.setLayers(layers);
		form.setAnswerRequireFlag(entity.getAnswerRequireFlag());
		form.setDefaultAnswer(entity.getDefaultAnswer());
		form.setGeneralConditionDiagnosisResultId(generalConditionDiagnosisId);
		form.setBuildingDisplayFlag(buildingDisplayFlag);
		if (distanceText == null || distanceText.equals("")) {
			distanceText = "-";
		}
		form.setDistance(distanceText);
		form.setJudgeResultItemId(judgeResultItemId);
		form.setAnswerDays(entity.getAnswerDays());
		// エクステントはセットしない
		form.setExtentFlag(false);
		// 【R6】申請種類、申請段階、部署（事前協議のみ）ごとに、結果の文言を分けるため、下記を追加
		form.setApplicationTypeId(entity.getApplicationTypeId());
		form.setApplicationStepId(entity.getApplicationStepId());
		form.setDepartmentId(entity.getDepartmentId());
		form.setDataType(dataType);
		form.setJudgementResultIndex(0);

		return form;
	}

	/**
	 * 道路判定結果ラベルを取得し文言を置換する（インデックス整数を使用）
	 * 
	 * @param text     テキスト
	 * @param identify 識別子
	 * @param index    インデックス（整数）
	 * @return 置換された文言
	 */
	private String setReplaceTextFromRoadJudgeResult(String text, String identify, int index) {
		final List<RoadJudgeLabel> label = roadJudgeLabelRepository.findRoadJudgeLabelFromIndexValue(identify, index);
		if (label.size() == 1) {
			return text.replace(identify, label.get(0).getReplaceText());
		} else {
			return text.replace(identify, "");
		}
	}

	/**
	 * 道路判定結果ラベルを取得し文言を置換する（インデックス文字列を使用）
	 * 
	 * @param text       テキスト
	 * @param identify   識別子
	 * @param index      インデックス（文字列）
	 * @param errorIndex indexで置換文言が取得できなかった場合に代替使用するインデックス（文字列）
	 * @return 置換された文言
	 */
	private String setReplaceTextFromRoadJudgeResult(String text, String identify, String index, String errorIndex) {
		List<RoadJudgeLabel> label = roadJudgeLabelRepository.findRoadJudgeLabelFromIndexText(identify, index);
		if (label.size() == 1) {
			return text.replace(identify, label.get(0).getReplaceText());
		} else {
			label = roadJudgeLabelRepository.findRoadJudgeLabelFromIndexText(identify, errorIndex);
			if (label.size() == 1) {
				return text.replace(identify, label.get(0).getReplaceText());
			}
			return text.replace(identify, "");
		}
	}

	/**
	 * 道路判定結果リストをM_区分判定フォームにセットする
	 * 
	 * @param formList                    概況診断結果フォームリスト
	 * @param roadJudgeResultList         道路判定結果リスト
	 * @param generalConditionDiagnosisId 概況診断結果ID
	 * @Param layers レイヤ
	 * @Param judgeResultItemId 判定結果項目ID
	 * @param entity                               M_区分判定、M_判定結果Entity
	 * @param lotNumberList                        申請地番リスト
	 * @param applicationId                        申請ID
	 * @param isnNewJudgementItem                  再申請で新規追加された区分であるか
	 * @param generalConditionDiagnosisRequestFrom 概要診断実施リクエスト
	 * 
	 * @return 判定結果項目ID更新値
	 */
	private int setRoadJudgeResultToGeneralConditionDiagnosisResultForm(
			List<GeneralConditionDiagnosisResultForm> formList, List<RoadJudgeResult> roadJudgeResultList,
			CategoryJudgementAndResult entity, int generalConditionDiagnosisId, List<LayerForm> layers,
			int judgeResultItemId, List<Integer> lotNumberList, Integer applicationId, boolean isnNewJudgementItem,
			GeneralConditionDiagnosisRequestForm generalConditionDiagnosisRequestFrom) {
		JudgementLayerDao judgementLayerDao = new JudgementLayerDao(emf);
		for (int i = 0; i < roadJudgeResultList.size(); i++) {
			// 区割り線レイヤ表示フラグ
			boolean splitLineLayerDisplayFlag = true;
			// 隣接歩道レイヤ表示フラグ
			boolean sidewalkLayerDisplayFlag = true;
			// 幅員値レイヤ表示フラグ
			boolean widthLabelLayerDisplayFlag = true;
			RoadJudgeResult roadJudgeResult = roadJudgeResultList.get(i);
			// 路線番号が同じ道路LOD2レイヤを保持するリスト
			final List<Integer> lineNumberOidList = new ArrayList<Integer>();
			GeneralConditionDiagnosisResultForm form = new GeneralConditionDiagnosisResultForm();
			form.setJudgementId(entity.getJudgementItemId());
			form.setTitle(entity.getTitle());
			form.setResult(true);
			form.setSummary(entity.getApplicableSummary());
			// 文言を置きかえ
			String description = entity.getApplicableDescription();
			// 道路種別案内文言を置き換え
			final String roadType = (roadJudgeResult.getRoadType() != null) ? roadJudgeResult.getRoadType()
					: roadtypeUnknownValue;
			description = setReplaceTextFromRoadJudgeResult(description, roadTypeResultIdentifyText, roadType,
					roadtypeUnknownValue);
			// 道路種別が設定値の場合、非表示設定の案内文言を置き換え、レイヤを非表示とする
			final String[] roadtypeNondisplayValues = roadtypeNondisplayValueText.split(",");
			final String[] roadtypeNondisplayIdentifyTexts = roadtypeNondisplayIdentifyTextText.split(",");
			if (Arrays.asList(roadtypeNondisplayValues).contains(roadType)) {
				for (int j = 0; j < roadtypeNondisplayIdentifyTexts.length; j++) {
					description = description.replace(roadtypeNondisplayIdentifyTexts[j], "");
					if (roadtypeNondisplayIdentifyTexts[j].equals(splitLineResultIdentifyText)
							|| roadtypeNondisplayIdentifyTexts[j].equals(maxWidthTextAreaIdentifyText)
							|| roadtypeNondisplayIdentifyTexts[j].equals(minWidthTextAreaIdentifyText)) {
						// 区割り線取得結果・最大最小幅員文言表示が非表示設定の場合、区割り線レイヤ・幅員値レイヤも非表示とする
						splitLineLayerDisplayFlag = false;
						widthLabelLayerDisplayFlag = false;
					} else if (roadtypeNondisplayIdentifyTexts[j].equals(walkwayResultIdentifyText)) {
						// 隣接歩道判定結果文言表示が非表示設定の場合、隣接歩道レイヤも非表示とする。
						sidewalkLayerDisplayFlag = false;
					}
				}
			}
			// 幅員をセット
			if (roadJudgeResult.getJudgeProcessResultFlag()) {

				if (roadJudgeResult.getWidthErrorFlag()) {
					// 幅員が正常取得できた場合
					description = setReplaceTextFromRoadJudgeResult(description, widthTextAreaIdentifyText, 0);
				} else {
					// 区割り線に幅員エラーコードが含まれていた場合
					description = setReplaceTextFromRoadJudgeResult(description, widthTextAreaIdentifyText,
							widthErrorCode);
				}
				;
				description = setReplaceTextFromRoadJudgeResult(description, maxWidthTextAreaIdentifyText, 0);
				description = setReplaceTextFromRoadJudgeResult(description, minWidthTextAreaIdentifyText, 0);
				description = description.replace(roadMaxWidthReplaceText,
						(roadJudgeResult.getRoadMaxWidth() != null) ? roadJudgeResult.getRoadMaxWidth().toString()
								: "不明")
						.replace(roadwayMaxWidthReplaceText,
								(roadJudgeResult.getRoadWayMaxWidth() != null)
										? roadJudgeResult.getRoadWayMaxWidth().toString()
										: "不明")
						.replace(roadMinWidthReplaceText,
								(roadJudgeResult.getRoadMinWidth() != null)
										? roadJudgeResult.getRoadMinWidth().toString()
										: "不明")
						.replace(roadwayMinWidthReplaceText,
								(roadJudgeResult.getRoadWayMinWidth() != null)
										? roadJudgeResult.getRoadWayMinWidth().toString()
										: "不明");
			} else {
				// 幅員が取得できなかった場合
				description = description.replace(widthTextAreaIdentifyText, "")
						.replace(maxWidthTextAreaIdentifyText, "").replace(minWidthTextAreaIdentifyText, "");
			}
			// 区割り線取得結果をセット
			int splitLineResultValue = noSideFlag;
			if (roadJudgeResult.getJudgeProcessResultFlag()) {
				if (roadJudgeResult.getDirection1SplitLineGetFlag()
						&& roadJudgeResult.getDirection2SplitLineGetFlag()) {
					// 両側取得
					splitLineResultValue = bothSideFlag;
				} else if (roadJudgeResult.getDirection1SplitLineGetFlag()
						&& !roadJudgeResult.getDirection2SplitLineGetFlag()) {
					// 片側取得
					splitLineResultValue = oneSideFlag;
				} else if (!roadJudgeResult.getDirection1SplitLineGetFlag()
						&& roadJudgeResult.getDirection2SplitLineGetFlag()) {
					// 片側取得
					splitLineResultValue = oneSideFlag;
				} else {
					// 取得なし
					splitLineResultValue = noSideFlag;
				}
			} else {
				// 道路中心線が取得できないなど、区割り線取得が正常終了していない
				splitLineResultValue = splitLineErrorFlag;
			}
			description = setReplaceTextFromRoadJudgeResult(description, splitLineResultIdentifyText,
					splitLineResultValue);

			// 隣接歩道有無判定結果をセット
			int walkwayResultFlag = walkwayResultFalseFlag;
			if (roadJudgeResult.getAdjacentWalkwayFlag() != null && roadJudgeResult.getAdjacentWalkwayFlag()) {
				// 隣接歩道あり
				walkwayResultFlag = walkwayResultTrueFlag;
			}
			description = setReplaceTextFromRoadJudgeResult(description, walkwayResultIdentifyText, walkwayResultFlag);

			// 幅員による段階表示結果をセット
			if (roadJudgeResult.getRoadMinWidth() != null) {
				final List<RoadJudgeLabel> widthThresholdLabel = roadJudgeLabelRepository
						.findRoadJudgeLabelFromThresholds(displayByWidthIdentifyText,
								roadJudgeResult.getRoadMinWidth());
				if (widthThresholdLabel.size() == 1) {
					description = description.replace(displayByWidthIdentifyText,
							widthThresholdLabel.get(0).getReplaceText());
				} else {
					description = description.replace(displayByWidthIdentifyText, "");
				}
			} else {
				description = description.replace(displayByWidthIdentifyText, "");
			}

			form.setDescription(description);
			form.setJudgementLayerDisplayFlag(entity.getNonApplicableLayerDisplayFlag());
			form.setSimultameousLayerDisplayFlag(entity.getSimultaneousDisplayLayerFlag());

			boolean extentFlag = false;
			Double minlon = null;
			Double minlat = null;
			Double maxlon = null;
			Double maxlat = null;
			// レイヤセット処理
			final List<LayerForm> addLayers = new ArrayList<LayerForm>();
			for (int j = 0; j < layers.size(); j++) {
				final LayerForm addLayer = new LayerForm();
				addLayer.setLayerId(layers.get(j).getLayerId());
				addLayer.setLayerType(layers.get(j).getLayerType());
				addLayer.setLayerName(layers.get(j).getLayerName());
				addLayer.setLayerCode(layers.get(j).getLayerCode());
				addLayer.setQueryRequireFlag(layers.get(j).getQueryRequireFlag());
				final String layerQuery = layers.get(j).getLayerQuery();
				boolean addLayerFlag = false;
				if (layerQuery != null) {
					if (layerQuery.contains(roadLod2LayerIdentifyText)) {
						// 道路LOD2レイヤクエリにオブジェクトIDをセット
						if (roadJudgeResult.getLineNumber() != null) {
							// 路線番号が同じ道路LOD2フィーチャを取得
							final List<RoadLod2> lineNumberList = judgementLayerDao
									.getRoadLod2WithLineNumber(roadJudgeResult.getLineNumber());
							for (RoadLod2 lod2 : lineNumberList) {
								lineNumberOidList.add(lod2.getObjectId());
							}
							final String objectIdText = setSeparateStringFromListInteger(lineNumberOidList, "_");
							addLayer.setLayerQuery(layerQuery.replace(roadLod2LayerIdentifyText, "")
									.replace(layerQueryReplaceTextValue, objectIdText));
							addLayerFlag = true;
						} else {
							if (roadJudgeResult.getLod2ObjectId() != null) {
								final String objectId = roadJudgeResult.getLod2ObjectId().toString();
								addLayer.setLayerQuery(layerQuery.replace(roadLod2LayerIdentifyText, "")
										.replace(layerQueryReplaceTextValue, objectId));
								addLayerFlag = true;
							}
						}

					} else if (layerQuery.contains(splitLineLayerIdentifyText)) {
						// 区割り線レイヤクエリに区割り線のオブジェクトID一覧をセット
						if (splitLineLayerDisplayFlag && roadJudgeResult.getSplitLineList() != null
								&& roadJudgeResult.getSplitLineList().size() > 0) {
							// 最大幅員オブジェクトID 取得できていない場合-1
							final String maxWidthObjectId = (roadJudgeResult.getMaxWidthSplitLineObjectId() != null)
									? roadJudgeResult.getMaxWidthSplitLineObjectId().toString()
									: "-1";
							// 最小幅員オブジェクトID 取得できていない場合-1
							final String minWidthObjectId = (roadJudgeResult.getMinWidthSplitLineObjectId() != null)
									? roadJudgeResult.getMinWidthSplitLineObjectId().toString()
									: "-1";
							final List<String> objectIdList = new ArrayList<String>();
							for (int k = 0; k < roadJudgeResult.getSplitLineList().size(); k++) {
								final String tmpObjectId = roadJudgeResult.getSplitLineList().get(k).getObjectId()
										.toString();
								if (!tmpObjectId.equals(minWidthObjectId) && !tmpObjectId.equals(maxWidthObjectId)) {
									// 最大・最小幅員のオブジェクトID以外をセット
									objectIdList.add(tmpObjectId);
								}

							}
							final String oIdQuery = (objectIdList.size() > 0) ? String.join("_", objectIdList)
									: MAX_INT + "";
							addLayer.setLayerQuery(layerQuery.replace(splitLineLayerIdentifyText, "")
									.replace(layerQueryReplaceTextValue, oIdQuery)
									.replace(maxWidthLayerIdentifyText, maxWidthObjectId)
									.replace(minWidthLayerIdentifyText, minWidthObjectId));
							addLayerFlag = true;
						}
					} else if (layerQuery.contains(sideWalkLayerIdentifyText)) {
						// 隣接歩道が取得されている場合、隣接歩道レイヤに隣接歩道オブジェクトIDと地番IDをセット
						if (sidewalkLayerDisplayFlag && roadJudgeResult.getAdjacentWalkwayFlag() != null
								&& roadJudgeResult.getAdjacentWalkwayFlag()) {
							final String walkwayText = setSeparateStringFromListInteger(
									roadJudgeResult.getAdjacentWalkwayObjectIdList(), "_");
							String lotNumberText = "";
							if (applicationId == null) {
								lotNumberText = setSeparateStringFromListInteger(lotNumberList, "_");
							} else {
								// 再申請時は重なるF_地番図形を取得してセット
								LotNumberDao dao = new LotNumberDao(emf);
								final List<LotNumber> srcLotNumberList = dao
										.getLotNumberMasterFromApplicationId(applicationId, epsg);
								final List<Integer> srcLotNumberIdList = new ArrayList<Integer>();
								for (LotNumber aLotNumber : srcLotNumberList) {
									srcLotNumberIdList.add(aLotNumber.getChibanId());
								}
								lotNumberText = setSeparateStringFromListInteger(srcLotNumberIdList, "_");
							}
							addLayer.setLayerQuery(layerQuery.replace(sideWalkLayerIdentifyText, "")
									.replace(sideWalkSidewalkIdentifyText, walkwayText)
									.replace(sideWalkLotNumberIdentifyText, lotNumberText));
							addLayerFlag = true;
						}
					} else if (layerQuery.contains(widthTextLayerIdentifyText)) {
						// 幅員値レイヤクエリに区割り線のオブジェクトID一覧をセット
						final List<String> objectIdList = new ArrayList<String>();
						if (widthLabelLayerDisplayFlag && roadJudgeResult.getSplitLineList() != null
								&& roadJudgeResult.getSplitLineList().size() > 0) {
							for (int k = 0; k < roadJudgeResult.getSplitLineList().size(); k++) {
								objectIdList.add(roadJudgeResult.getSplitLineList().get(k).getObjectId().toString());
							}
							final String oIdQuery = String.join("_", objectIdList);
							addLayer.setLayerQuery(layerQuery.replace(layerQueryReplaceTextValue, oIdQuery));
							addLayerFlag = true;
						}
					} else {
						addLayer.setLayerQuery(layerQuery);
						addLayerFlag = true;
					}
				} else {
					addLayerFlag = true;
				}
				if (addLayerFlag) {
					addLayers.add(addLayer);
				}

			}
			form.setLayers(addLayers);
			form.setAnswerRequireFlag(entity.getAnswerRequireFlag());
			form.setDefaultAnswer(entity.getDefaultAnswer());
			form.setGeneralConditionDiagnosisResultId(generalConditionDiagnosisId);
			// 道路判定では距離判定は行わない
			String distanceText = "-";
			form.setDistance(distanceText);
			form.setJudgeResultItemId(judgeResultItemId);
			// 道路判定時は建物表示を無効にする
			form.setBuildingDisplayFlag(false);
			// エクステントをセットする
			// 表示範囲のエクステントを取得
			// 区割り線が取れている場合は区割り線の範囲、取れていない場合は道路LOD2の範囲
			final List<SpiltLineExtent> extent = (roadJudgeResult.getSplitLineList() != null
					&& roadJudgeResult.getSplitLineList().size() > 0)
							? judgementLayerDao.getSplitLineExtent(lonlatEpsg, roadJudgeResult.getSplitLineList())
							: (lineNumberOidList.size() > 0)
									? judgementLayerDao.getRoadLOD2Extent(lonlatEpsg, lineNumberOidList)
									: judgementLayerDao.getRoadLOD2Extent(lonlatEpsg,
											roadJudgeResult.getLod2ObjectId());
			if (extent.size() > 0) {
				extentFlag = true;
				minlon = extent.get(0).getMinlon();
				minlat = extent.get(0).getMinlat();
				maxlon = extent.get(0).getMaxlon();
				maxlat = extent.get(0).getMaxlat();
			}
			form.setExtentFlag(extentFlag);
			form.setMinlon(minlon);
			form.setMinlat(minlat);
			form.setMaxlon(maxlon);
			form.setMaxlat(maxlat);
			// 【R6】申請種類、申請段階、部署（事前協議のみ）ごとに、結果の文言を分けるため、下記を追加
			form.setApplicationTypeId(entity.getApplicationTypeId());
			form.setApplicationStepId(entity.getApplicationStepId());
			form.setDepartmentId(entity.getDepartmentId());
			String dataType = getDataType(isnNewJudgementItem, generalConditionDiagnosisRequestFrom, entity, i);
			form.setDataType(dataType);
			form.setJudgementResultIndex(i);

			judgeResultItemId++;
			formList.add(form);
		}
		return judgeResultItemId;

	}

	/**
	 * リストを指定文字列で区切った文字列に変換する
	 * 
	 * @param list         リスト
	 * @param separateText 区切り文字列
	 * @return
	 */
	private String setSeparateStringFromListInteger(List<Integer> list, String separateText) {
		try {
			StringBuilder strbul = new StringBuilder();
			Iterator<Integer> iter = list.iterator();
			while (iter.hasNext()) {
				strbul.append(iter.next());
				if (iter.hasNext()) {
					strbul.append(separateText);
				}
			}
			return strbul.toString();
		} catch (Exception e) {
			return "";
		}

	}

	/**
	 * 道路LOD2レイヤ取得結果を路線番号で集約する
	 * 
	 * @param roadLod2List 道路LOD2取得結果
	 * @return List<RoadLod2> 道路LOD2集約リスト
	 */
	private List<RoadLod2> aggregateRoadLod2Result(List<RoadLod2> roadLod2List) {
		final List<RoadLod2> resList = new ArrayList<RoadLod2>();
		final Map<String, RoadLod2> roadLod2Map = new HashMap<String, RoadLod2>();
		for (int i = 0; i < roadLod2List.size(); i++) {
			final RoadLod2 roadLod2 = roadLod2List.get(i);
			if (roadLod2.getLineNumber() == null) {
				// 路線番号がNullの場合そのまま結果としてセットする
				resList.add(roadLod2);
			} else {
				// 路線番号がNullでない場合、最大の幅員値を持つ道路LOD2フィーチャに集約する
				if (roadLod2Map.containsKey(roadLod2.getLineNumber())) {
					if (roadLod2Map.get(roadLod2.getLineNumber()).getWidth() < roadLod2.getWidth()) {
						roadLod2Map.put(roadLod2.getLineNumber(), roadLod2);
					}
				} else {
					roadLod2Map.put(roadLod2.getLineNumber(), roadLod2);
				}
			}
		}
		// 路線番号で集約した結果を追加する
		for (String key : roadLod2Map.keySet()) {
			resList.add(roadLod2Map.get(key));
		}

		return resList;
	}

	/**
	 * 概況診断結果複数行表示フォームをセット
	 * 
	 * @param formList                             概況診断結果フォーム
	 * @param valuesList                           重なり属性値リスト
	 * @param entity                               M_区分判定、M_判定結果Entity
	 * @param generalConditionDiagnosisId          概況診断結果ID
	 * @param layers                               レイヤフォーム
	 * @param judgeResultItemId                    判定結果項目ID
	 * @param distanceResultText                   距離判定結果
	 * @param isnNewJudgementItem                  再申請で新規追加された区分であるか
	 * @param generalConditionDiagnosisRequestFrom 概要診断実施リクエスト
	 * 
	 * @return
	 */
	private int setSeparatedJudgeResultToGeneralConditionDiagnosisResultForm(
			List<GeneralConditionDiagnosisResultForm> formList, List<List<String>> valuesList,
			CategoryJudgementAndResult entity, boolean judgeResult, int generalConditionDiagnosisId,
			List<LayerForm> layers, int judgeResultItemId, String distanceResultText, boolean isnNewJudgementItem,
			GeneralConditionDiagnosisRequestForm generalConditionDiagnosisRequestFrom) {
		// 置換対象要素数を取得
		int rowCount = 0;
		if (valuesList.size() > 0) {
			List<String> values = valuesList.get(0);
			rowCount = values.size();
		}

		int judgementResultIndex = 0;
		for (List<String> aValues : valuesList) {
			GeneralConditionDiagnosisResultForm form = new GeneralConditionDiagnosisResultForm();
			form.setJudgementId(entity.getJudgementItemId());
			form.setTitle(entity.getTitle());
			form.setResult(judgeResult);
			form.setSummary(entity.getApplicableSummary());
			// 文言を置換
			String description = entity.getApplicableDescription();
			for (int c = 0; c < rowCount; c++) {
				description = description.replace(DESCRIPTION_REPLACE_TARGET_CHARACTER + (c + 1), aValues.get(c));
			}
			form.setDescription(description);
			form.setJudgementLayerDisplayFlag(entity.getNonApplicableLayerDisplayFlag());
			form.setSimultameousLayerDisplayFlag(entity.getSimultaneousDisplayLayerFlag());
			// レイヤをセット
			final List<LayerForm> addLayers = new ArrayList<LayerForm>();
			for (int j = 0; j < layers.size(); j++) {
				final LayerForm addLayer = new LayerForm();
				addLayer.setLayerId(layers.get(j).getLayerId());
				addLayer.setLayerType(layers.get(j).getLayerType());
				addLayer.setLayerName(layers.get(j).getLayerName());
				addLayer.setLayerCode(layers.get(j).getLayerCode());
				addLayer.setQueryRequireFlag(layers.get(j).getQueryRequireFlag());
				String layerQuery = layers.get(j).getLayerQuery();
				// レイヤクエリをセット
				for (int c = 0; c < rowCount; c++) {
					layerQuery = layerQuery.replace(DESCRIPTION_REPLACE_TARGET_CHARACTER + (c + 1), aValues.get(c));
				}
				addLayer.setLayerQuery(layerQuery);
				addLayers.add(addLayer);
			}
			form.setLayers(addLayers);
			form.setAnswerRequireFlag(entity.getAnswerRequireFlag());
			form.setDefaultAnswer(entity.getDefaultAnswer());
			form.setGeneralConditionDiagnosisResultId(generalConditionDiagnosisId);
			if (distanceResultText == null || distanceResultText.equals("")) {
				distanceResultText = "-";
			}
			form.setDistance(distanceResultText);
			form.setJudgeResultItemId(judgeResultItemId);
			// 道路判定以外では建物表示有効
			form.setBuildingDisplayFlag(true);
			// エクステントはセットしない
			form.setExtentFlag(false);
			// 【R6】申請種類、申請段階、部署（事前協議のみ）ごとに、結果の文言を分けるため、下記を追加
			form.setApplicationTypeId(entity.getApplicationTypeId());
			form.setApplicationStepId(entity.getApplicationStepId());
			form.setDepartmentId(entity.getDepartmentId());

			String dataType = getDataType(isnNewJudgementItem, generalConditionDiagnosisRequestFrom, entity,
					judgementResultIndex);
			form.setDataType(dataType);
			form.setJudgementResultIndex(judgementResultIndex);

			judgeResultItemId++;
			judgementResultIndex++;

			formList.add(form);
		}
		return judgeResultItemId;
	}

	/**
	 * M_レイヤEntityをM_レイヤフォームに詰めなおす
	 * 
	 * @param entity M_レイヤEntity
	 * @return M_レイヤフォーム
	 */
	private LayerForm getLayerFormFromEntity(Layer entity) {
		LayerForm form = new LayerForm();
		form.setLayerId(entity.getLayerId());
		form.setLayerType(entity.getLayerType());
		form.setLayerName(entity.getLayerName());
		form.setLayerCode(entity.getLayerCode());
		form.setLayerQuery(entity.getLayerQuery());
		form.setQueryRequireFlag(entity.getQueryRequireFlag());
		return form;
	}

	/**
	 * 概況表示文言の置換(連結)
	 * 
	 * @param baseText   変換前文字列
	 * @param valuesList 属性値リスト
	 * @return 置換後文字列
	 */
	private String replaceDescriptionJoint(String baseText, List<List<String>> valuesList) {
		String description = baseText;

		int count = 0;
		if (valuesList.size() > 0) {
			List<String> values = valuesList.get(0);
			count = values.size();
		}

		Set<String> addSet = new HashSet<String>();
		for (int c = 0; c < count; c++) {
			String jointText = "";
			for (int listIdx = 0; listIdx < valuesList.size(); listIdx++) {
				List<String> values = valuesList.get(listIdx);
				String tmpText = values.get(c);

				// 重複定義は除く
				if (!addSet.contains(tmpText)) {
					if (jointText.length() > 0) {
						jointText += descriptionJointCharacter;
					}
					jointText += tmpText;
					addSet.add(tmpText);
				}
			}

			description = description.replace(DESCRIPTION_REPLACE_TARGET_CHARACTER + (c + 1), jointText);
		}

		return description;
	}

	/**
	 * 概況表示文言の置換(改行リピート)
	 * 
	 * @param baseText   変換前文字列
	 * @param valuesList 属性値リスト
	 * @return 置換後文字列
	 */
	private String replaceDescriptionRepeat(String baseText, List<List<String>> valuesList) {
		String description = "";

		// 改行コードで分解
		List<String> workList = new ArrayList<String>();
		String[] textArray = baseText.split(CR + LF);
		for (int i = 0; i < textArray.length; i++) {
			String[] textArray2 = textArray[i].split(CR);
			for (int j = 0; j < textArray2.length; j++) {
				String[] textArray3 = textArray2[j].split(LF);
				for (int k = 0; k < textArray3.length; k++) {
					workList.add(textArray3[k]);
				}
			}
		}

		for (String tmpText : workList) {
			if (tmpText.contains(DESCRIPTION_REPLACE_TARGET_CHARACTER)) {
				// @を含むので置換が必要
				String repeatBaseText = tmpText;

				Set<String> addSet = new HashSet<String>();
				for (int i = 0; i < valuesList.size(); i++) {
					String repeatText = repeatBaseText;
					List<String> values = valuesList.get(i);
					for (int j = 0; j < values.size(); j++) {
						repeatText = repeatText.replace(DESCRIPTION_REPLACE_TARGET_CHARACTER + (j + 1), values.get(j));
					}

					// 重複定義は除く
					if (!addSet.contains(repeatText)) {
						if (description.length() != 0) {
							description += CR + LF;
						}
						description += repeatText;
						addSet.add(repeatText);
					}
				}
			} else {
				// 置換の必要がないのでそのまま追加
				if (description.length() != 0) {
					description += CR + LF;
				}
				description += tmpText;
			}
		}
		return description;
	}

	/**
	 * フォルダ内にあるエクセルファイル名の一覧を取得
	 * 
	 * @param folderPath
	 * @return xlsxFileNames ファイル名一覧
	 */
	private List<String> findXlsxFileNames(String folderPath) {
		List<String> xlsxFileNames = new ArrayList<>();
		File folder = new File(folderPath);
		File[] files = folder.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.isFile() && file.getName().toLowerCase().endsWith(".xlsx")) {
					xlsxFileNames.add(file.getName());
				}
			}
		}
		return xlsxFileNames;
	}

	/**
	 * フォルダ内にあるエクセルファイル一覧を取得
	 * 
	 * @param folderPath
	 * @return xlsxFiles ファイル一覧
	 */
	private List<File> findXlsxFiles(String folderPath) {
		List<File> xlsxFiles = new ArrayList<>();
		File folder = new File(folderPath);
		File[] files = folder.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.isFile() && file.getName().toLowerCase().endsWith(".xlsx")) {
					xlsxFiles.add(file);
				}
			}
		}
		return xlsxFiles;
	}

	/**
	 * フォルダとファイル数をカウントする
	 * 
	 * @param directory
	 * @return 正常：counts=フォルダ及びファイル数の合計 , 異常：counts=-1
	 */
	private int countFoldersAndFiles(File directory) {
		try {
			int counts = 0;
			File[] files = directory.listFiles();

			if (files != null) {
				for (File file : files) {
					if (file.isDirectory()) {
						counts++;
					} else if (file.isFile()) {
						if (file.getName().toLowerCase().endsWith(".xlsx")
								|| file.getName().toLowerCase().endsWith(".png")) {
							counts++;
						}
					}
				}
			}
			return counts;
		} catch (Exception e) {
			return -1;
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
	 * 申請種類IDから申請種類名を取得する
	 * 
	 * @param applicationTypeId 申請種別ID
	 * @return 申請種別名
	 */
	public String getApplicationTypeName(Integer applicationTypeId) {

		String applicationTypeName = applicationService.getApplicationTypeName(applicationTypeId);

		if (applicationTypeName == null) {
			applicationTypeName = EMPTY;
		}

		return applicationTypeName;
	}
	
	/**
	 * 申請段階IDから申請段階名を取得する
	 * 
	 * @param applicationStepId 申請段階ID
	 * @return 申請段階名
	 */
	public String getApplicationStepName(Integer applicationStepId) {

		String applicationStepName = applicationService.getApplicationStepName(applicationStepId);

		if (applicationStepName == null) {
			applicationStepName = EMPTY;
		}
		return applicationStepName;
	}
}
