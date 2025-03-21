package developmentpermission.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.SequenceWriter;

import developmentpermission.form.ApplicationInformationSearchResultHeaderForm;
import developmentpermission.form.ApplicationSearchResultForm;
import developmentpermission.form.ChatSearchResultForm;
import developmentpermission.form.OutputDataForm;

/**
 * 
 * CSV出力用サービス
 *
 */
@Service
public class CsvExportService extends AbstractService {
	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(CsvExportService.class);

	/** データタイプ:申請 */
	public static final String DATA_TYPE_APPLICATION = "application";

	/** データタイプ:問合せ */
	public static final String DATA_TYPE_INQUIRY = "inquiry";

	/** ソートカラム: ステータス */
	public static final String SORT_COLUMN_STATUS = "status";

	/** ソートカラム: 申請ID */
	public static final String SORT_COLUMN_APPLICATION_ID = "applicationId";

	/** ソートカラム: 対象 */
	public static final String SORT_COLUMN_CATEGORY_JUDGEMENT_TITLE = "categoryJudgementTitle";

	/** ソートカラム: 担当課 */
	public static final String SORT_COLUMN_DEPARTMENT_NAME = "departmentName";

	/** ソートカラム: 初回投稿日時 */
	public static final String SORT_COLUMN_ESTABLISHMENT_FIRST_POST_DATETIME = "establishmentFirstPostDatetime";

	/** ソートカラム: 最新投稿日時 */
	public static final String SORT_COLUMN_SEND_DATETIME = "sendDatetime";

	/** ソートカラム: 最新回答者 */
	public static final String SORT_COLUMN_ANSWER_USER_NAME = "answerUserName";

	/** ソートカラム: 最新回答日時 */
	public static final String SORT_COLUMN_ANSWER_DATETIME = "answerDatetime";

	/** ソート順: 昇順 */
	public static final String SORT_ORDER_ASC = "asc";

	/** ソート順: 降順 */
	public static final String SORT_ORDER_DESC = "desc";

	/** 問合せ検索CSV出力 出力ラベル名:ステータス */
	@Value("${app.chat.search.csv.headName.status}")
	private String csvHeadNameStatus;

	/** 問合せ検索CSV出力 出力ラベル名:申請ID */
	@Value("${app.chat.search.csv.headName.applicationid}")
	private String csvHeadNameApplicationId;

	/** 問合せ検索CSV出力 出力ラベル名:対象 */
	@Value("${app.chat.search.csv.headName.title}")
	private String csvHeadNameTitle;

	/** 問合せ検索CSV出力 出力ラベル名:回答担当課 */
	@Value("${app.chat.search.csv.headName.department}")
	private String csvHeadNameDepartment;

	/** 問合せ検索CSV出力 出力ラベル名:初回投稿日時 */
	@Value("${app.chat.search.csv.headName.initialDate}")
	private String csvHeadNameInitialDate;

	/** 問合せ検索CSV出力 出力ラベル名:最新投稿日時 */
	@Value("${app.chat.search.csv.headName.latestDate}")
	private String csvHeadNameLatestDate;

	/** 問合せ検索CSV出力 出力ラベル名:最新回答者 */
	@Value("${app.chat.search.csv.headName.latestAnswer}")
	private String csvHeadNameLatestAnswer;

	/** 問合せ検索CSV出力 出力ラベル名:最新回答日時 */
	@Value("${app.chat.search.csv.headName.latestAnswerDate}")
	private String csvHeadNameLatestAnswerDate;

	/**
	 * 問合せ情報CSV出力
	 * 
	 * @param outputDataForm
	 * @param resultList
	 * @return CSVファイル
	 */
	public String exportInquiryCsv(OutputDataForm outputDataForm, List<ChatSearchResultForm> resultList) {
		try {
			// ソート:リクエスト時点でソート済みの前提
			// CSVデータ作成（ヘッダ）
			List<String> csvHeader = new ArrayList<String>();
			// ステータス
			csvHeader.add(csvHeadNameStatus);
			// 申請ID
			csvHeader.add(csvHeadNameApplicationId);
			// 対象
			csvHeader.add(csvHeadNameTitle);
			// 回答担当課
			csvHeader.add(csvHeadNameDepartment);
			// 初回投稿日時
			csvHeader.add(csvHeadNameInitialDate);
			// 最新投稿日時
			csvHeader.add(csvHeadNameLatestDate);
			// 最新回答者
			csvHeader.add(csvHeadNameLatestAnswer);
			// 最新回答日時
			csvHeader.add(csvHeadNameLatestAnswerDate);

			// CSVデータ作成（データ）
			List<String> csvData = new ArrayList<String>();
			for (ChatSearchResultForm aRecord : resultList) {
				// ステータス
				Integer statusValue = aRecord.getStatus();
				String statusText = "";
				Map<String, String> statusMap = getAnswerStatusMap();
				if (statusMap.containsKey(statusValue + "")) {
					statusText = statusMap.get(statusValue + "");
				}
				csvData.add(statusText);
				// 申請ID
				String applicationId = (aRecord.getApplicationId() != null) ? aRecord.getApplicationId() + "" : "";
				csvData.add(applicationId);
				// 対象
				String title = (aRecord.getCategoryJudgementTitle() != null) ? aRecord.getCategoryJudgementTitle() : "";
				csvData.add(title);
				// 回答担当課
				String departmentName = (aRecord.getDepartmentName() != null) ? aRecord.getDepartmentName() : "";
				csvData.add(departmentName);
				// 初回投稿日時
				String initPostDate = (aRecord.getEstablishmentFirstPostDatetime() != null)
						? aRecord.getEstablishmentFirstPostDatetime()
						: "";
				csvData.add(initPostDate);
				// 最新投稿日時
				String latestPostDate = (aRecord.getSendDatetime() != null) ? aRecord.getSendDatetime() : "";
				csvData.add(latestPostDate);
				// 最新回答者
				String answerUserName = (aRecord.getAnswerUserName() != null) ? aRecord.getAnswerUserName() : "";
				csvData.add(answerUserName);
				// 最新回答日時
				String lastAnswerDate = (aRecord.getAnswerDatetime() != null) ? aRecord.getAnswerDatetime() : "";
				csvData.add(lastAnswerDate);
			}

			// CSVテキスト化
			return generateCsvText(csvHeader, csvData);
		} catch (Exception e) {
			LOGGER.error("CSVデータ生成時に例外発生", e);
		}
		return null;
	}

	/**
	 * 申請情報CSV出力
	 * 
	 * @param outputDataForm
	 * @param resultList
	 * @param headers
	 * @return CSVファイル
	 */
	public String exportApplicationCsv(OutputDataForm outputDataForm, List<ApplicationSearchResultForm> resultList,
			List<ApplicationInformationSearchResultHeaderForm> headers) {
		try {
			// ソート: リクエスト側でソートする前提
			// データリスト整形
			List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
			for (ApplicationSearchResultForm aRecord : resultList) {
				final Map<String, Object> aMap = new HashMap<String, Object>();
				final Map<String, Object> attributes = aRecord.getAttributes();
				for (Map.Entry<String, Object> entry : attributes.entrySet()) {

					if (entry.getValue() instanceof List) {
						List<?> aList = (List<?>) entry.getValue();
						Object aData = (Object) (aList.stream().map(String::valueOf).collect(Collectors.joining(",")));
						aMap.put(entry.getKey(), aData);
					} else {
						aMap.put(entry.getKey(), entry.getValue());
					}
				}
				dataList.add(aMap);
			}
			// CSVデータ作成（ヘッダ）
			List<String> csvHeader = new ArrayList<String>();
			for (ApplicationInformationSearchResultHeaderForm aHeader : headers) {
				csvHeader.add(aHeader.getDisplayColumnName());
			}
			// CSVデータ作成（データ）
			List<String> csvData = new ArrayList<String>();
			for (Map<String, Object> aRecord : dataList) {
				for (ApplicationInformationSearchResultHeaderForm aHeader : headers) {
					final String key = aHeader.getResonseKey();
					if (aRecord.containsKey(key)) {
						final String aData = aRecord.get(key) != null ? aRecord.get(key).toString() : "";
						csvData.add(aData);
					} else {
						csvData.add("");
					}
				}
			}
			// CSVテキスト化
			return generateCsvText(csvHeader, csvData);
		} catch (Exception e) {
			LOGGER.error("CSVデータ生成時に例外発生", e);
		}
		return null;
	}

	/**
	 * CSV文字列生成
	 * 
	 * @param csvHeader ヘッダ
	 * @param csvData   データ
	 * @return
	 */
	private String generateCsvText(List<String> csvHeader, List<String> csvData) {
		String csvText = "";
		int dataColumn = csvHeader.size();
		// ヘッダ書き込み
		for (int i = 0; i < dataColumn; i++) {
			csvText += "\"" + csvHeader.get(i) + "\"" + ",";
		}
		csvText += "\n";
		// データ部書き込み
		int rowNum = 1;
		for (int i = 0; i < csvData.size(); i++) {
			csvText += "\"" + csvData.get(i) + "\"" + ",";
			if (i == (dataColumn * rowNum - 1)) {
				csvText += "\n";
				rowNum++;
			}
		}
		return csvText;
	}

}
