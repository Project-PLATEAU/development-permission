package developmentpermission.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import developmentpermission.entity.CategoryJudgement;
import developmentpermission.form.ApplicationCategoryForm;
import developmentpermission.form.ApplicationCategorySelectionViewForm;
import developmentpermission.form.ApplyLotNumberForm;
import developmentpermission.form.GeneralConditionDiagnosisReportRequestForm;
import developmentpermission.form.GeneralConditionDiagnosisResultForm;
import developmentpermission.form.LotNumberForm;
import developmentpermission.service.JudgementService;
import developmentpermission.util.model.CellStyle;
import developmentpermission.util.model.ExportJudgeFormParam;
import developmentpermission.util.model.Picture;
import developmentpermission.util.model.TextValue;

/**
 * 概況診断結果レポート出力
 */
@Component
public class ExportJudgeForm extends AbstractExportForm {

	/** 概況図ファイル名 */
	public static final String OVERVIEW_FILE_NAME = "0.png";
	/** 区分判定のファイル拡張子 */
	public static final String JUDGEMENT_IMAGE_EXTENTION = ".png";

	/** 対象シート番号 */
	private static final int TARGET_SHEET_INDEX = 0;

	/** 区分判定リストのGIS判定値Map */
	private Map<String, String> categoryJudgementMap;

	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(ExportJudgeForm.class);

	/**
	 * コンストラクタ
	 */
	public ExportJudgeForm() {
		// 区分判定のGIS判定値を記憶
		categoryJudgementMap = new HashMap<String, String>();
	}

	/**
	 * コンストラクタ
	 * 
	 * @param categoryJudgementList 区分判定リスト
	 */
	public ExportJudgeForm(List<CategoryJudgement> categoryJudgementList) {
		// 区分判定のGIS判定値を記憶
		categoryJudgementMap = new HashMap<String, String>();
		for (CategoryJudgement categoryJudgement : categoryJudgementList) {
			categoryJudgementMap.put(categoryJudgement.getJudgementItemId(), categoryJudgement.getGisJudgement());
		}
	}

	/**
	 * 概況診断結果レポート帳票生成
	 * 
	 * @param generalConditionDiagnosisReportRequestForm 要求パラメータ
	 * @param param                                      帳票出力パラメータ
	 * @return 生成帳票
	 * @throws Exception 例外
	 */
	public Workbook createJudgeReportWorkBook(
			GeneralConditionDiagnosisReportRequestForm generalConditionDiagnosisReportRequestForm,
			ExportJudgeFormParam param) throws Exception {
		LOGGER.debug("概況診断結果レポート帳票生成 開始");
		try {
			Workbook wb = null;
			// テンプレートを読み込む
			final File templateFile = new File(param.getTemplatePath());
			try (InputStream is = new FileInputStream(templateFile)) {
				wb = new XSSFWorkbook(is);
				// wb = WorkbookFactory.create(is);
			}

			if (wb != null) {
				// 出力日
				LOGGER.trace("出力日出力 開始");
				SimpleDateFormat df = new SimpleDateFormat(param.getDateFormat());
				String nowDateText = df.format(new Date());
				writeTextValue(wb,
						new TextValue(nowDateText, TARGET_SHEET_INDEX, param.getDateRow(), param.getDateCol()));
				LOGGER.trace("出力日出力 終了");

				// キャプチャがある場合概況図出力
				if(param.getFolderPath() != null) {
					LOGGER.trace("概況図出力 開始");
					Picture overviewPic = new Picture();
					overviewPic.setType(Workbook.PICTURE_TYPE_PNG);
					overviewPic.setFilePath(param.getFolderPath() + PATH_SPLITTER + OVERVIEW_FILE_NAME);
					overviewPic.setStartCol(param.getOverviewStartCol());
					overviewPic.setEndCol(param.getOverviewEndCol() + 1);
					overviewPic.setStartRow(param.getOverviewStartRow());
					overviewPic.setEndRow(param.getOverviewEndRow() + 1);
					writePicture(wb, overviewPic);
					LOGGER.trace("概況図出力 終了");
				}else {
					LOGGER.trace("概況図出力 なし");
					int startC = param.getOverviewStartCol();
					int startR = param.getOverviewStartRow();
					//　文字列入力
					writeTextValue(wb, new TextValue("画像なし", TARGET_SHEET_INDEX, startR, startC));
				}

				// 区分出力
				LOGGER.trace("区分出力 開始");
				int startRow = param.getCategoryStartRow();
				int endRow = param.getCategoryEndRow();
				int currentRow = startRow;
				List<ApplicationCategorySelectionViewForm> applicationCategories = generalConditionDiagnosisReportRequestForm
						.getApplicationCategories();
				for (ApplicationCategorySelectionViewForm applicationCategory : applicationCategories) {
					if (applicationCategory.isEnable()) {
						writeTextValue(wb, new TextValue(applicationCategory.getTitle(), TARGET_SHEET_INDEX, currentRow,
								param.getCategoryTitleCol()));

						String contentText = EMPTY;
						List<ApplicationCategoryForm> categoryList = applicationCategory.getApplicationCategory();
						for (ApplicationCategoryForm category : categoryList) {
							// if (category.getChecked()) {
							if (contentText.length() > 0) {
								contentText += param.getSeparator();
							}
							contentText += category.getContent();
							// }
						}
						writeTextValue(wb, new TextValue(contentText, TARGET_SHEET_INDEX, currentRow,
								param.getCategoryDescriptionCol()));

						currentRow++;
						if (currentRow > endRow) {
							// これ以上は出力不可
							break;
						}
					}
				}
				LOGGER.trace("区分出力 終了");

				// 地番出力
				LOGGER.trace("地番出力 開始");
				String addressText = "";
				List<LotNumberForm> lotNumbers = generalConditionDiagnosisReportRequestForm.getLotNumbers();
				if (lotNumbers != null && lotNumbers.size() > 0) {
					// 初回申請
					addressText = getAddressText(lotNumbers, param.getLotNumberSeparators(), param.getSeparator());
				} else {
					// 2回目以降申請
					List<ApplyLotNumberForm> applyLotNumbers = generalConditionDiagnosisReportRequestForm.getApplyLotNumbers();
					if (applyLotNumbers != null && applyLotNumbers.size() > 0) {
						addressText = applyLotNumbers.get(0).getLot_numbers();
					}
				}
				

				writeTextValue(wb,
						new TextValue(addressText, TARGET_SHEET_INDEX, param.getAddressRow(), param.getAddressCol()));
				LOGGER.trace("地番出力 終了");

				// 判定結果summary出力
				currentRow = writeJudgeResultSummary(wb, generalConditionDiagnosisReportRequestForm, param);

				// 判定結果description出力
				writeJudgeResultDescription(wb, generalConditionDiagnosisReportRequestForm, param, currentRow);
			}

			return wb;
		} finally {
			LOGGER.debug("概況診断結果レポート帳票生成 終了");
		}
	}

	/**
	 * 行政から回答レポート帳票生成
	 * @param tempFilePath テンプレートファイルのパス
	 * @param answerNotifiedTextMap  回答ID-回答内容の紐づけ情報
	 * @param param 帳票出力パラメータ
	 * @return 生成帳票
	 * @throws Exception
	 */
	public Workbook createAnswerReportWorkBook(String tempFilePath, Map<Integer, String> answerNotifiedTextMap,
			ExportJudgeFormParam param,Integer applicationStepId,
			GeneralConditionDiagnosisReportRequestForm generalConditionDiagnosisReportRequestForm) throws Exception {
		LOGGER.debug(" 行政から回答レポート帳票生成 開始");
		try {
			
			ZipSecureFile.setMinInflateRatio(0.001);
			Workbook wb = null;
		
			// テンプレートを読み込む
			final File templateFile = new File(tempFilePath);
			try (InputStream is = new FileInputStream(templateFile)) {
				wb = new XSSFWorkbook(is);
			} 
			
			if (wb != null) {
				Sheet worksheet = wb.getSheetAt(TARGET_SHEET_INDEX);
				
				org.apache.poi.ss.usermodel.CellStyle newStyle = wb.createCellStyle();
				int answerTitleMergeRow = param.getJudgeResultDescriptionAnswerTitleMergerow(); // 判定結果詳細 回答タイトル結合行数
				int answerContentCol = param.getJudgeResultDescriptionAnswerContentCol(); // 判定結果詳細 回答内容出力列
				int answerContentMergeRow = param.getJudgeResultDescriptionAnswerContentMergeRow(); // 判定結果詳細 回答内容結合行数
				String answerTitleTemplateTextStart = param.getJudgeResultDescriptionAnswerTitleTemplateText1(); // 判定結果詳細 回答タイトルテンプレート文字列1
				String answerTitleTemplateTextEnd = param.getJudgeResultDescriptionAnswerTitleTemplateText2(); // 判定結果詳細 回答タイトルテンプレート文字列2
				String answerTitleTemplateTextNot = param.getJudgeResultDescriptionAnswerTitleTemplateText3(); // 判定結果(非該当)
				// 概要が始まる行を記憶する用
				int startCount = -1;
				// 何行目サマリか判断用
				int summaryRow = 0;
				// テンプレートファイルの行数を取得
				int lastRowNum = worksheet.getLastRowNum();
				for (int rowNum = 1; rowNum < lastRowNum; rowNum++) {

					// 回答タイトルのセルを取得
					Row titleRow = worksheet.getRow(rowNum);
					if (titleRow != null) {
						Cell titleCell = titleRow.getCell(answerContentCol);
						if (titleCell != null) {
							String value = titleCell.getStringCellValue();
							if (value != null && value.startsWith(answerTitleTemplateTextNot)) {
								summaryRow += 1;
							}
							// セルの値は「回答(ID=」で始まる場合、回答内容を書き込む
							if (value != null && value.startsWith(answerTitleTemplateTextStart)) {
								if (startCount == -1) {
									startCount = rowNum - param.getJudgeResultDescriptionMergeRow() + 1;
								}
								// 回答タイトルから回答IDを取得する
								String answerIdStr = value.replace(answerTitleTemplateTextStart, "")
										.replace(answerTitleTemplateTextEnd, "");
								int answerId = Integer.parseInt(answerIdStr.trim());
								// 事前協議の場合
								if (applicationStepId.equals(APPLICATION_STEP_ID_2)) {
									// 回答内容を設定
									String answerContent = answerNotifiedTextMap.get(answerId);
									if (answerContent != null) {
										Row contentRow = worksheet.getRow(rowNum + answerTitleMergeRow);
										Cell contentCell = contentRow.getCell(answerContentCol);
										contentCell.setCellValue(answerContent);
									} else {
										// もしanswerNotifiedTextMapから値を取得できなかった場合は、セルを削除する
										for (int i = 0; i < 2; i++) {
											Row contentRow = worksheet.getRow(rowNum + answerTitleMergeRow - i);
											Cell contentCell = contentRow.getCell(answerContentCol);
											if (i == 0) {
												contentCell.setCellValue(param.getJudgeResultDescriptionAnswerDelete());
											}
											if (contentCell != null) {
												// グレーアウトを定義
												newStyle.cloneStyleFrom(contentCell.getCellStyle());
												newStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
												newStyle.setFillForegroundColor(
														IndexedColors.GREY_25_PERCENT.getIndex());
												contentCell.setCellStyle(newStyle); // セルをグレーアウト
											}
										}
										Row contentRow2 = worksheet
												.getRow(rowNum + answerTitleMergeRow - answerContentMergeRow - 1);
										Cell contentCell = contentRow2.getCell(answerContentCol);
										if (contentCell != null) {
											// グレーアウトを定義
											newStyle.cloneStyleFrom(contentCell.getCellStyle());
											newStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
											newStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
											contentCell.setCellStyle(newStyle); // セルをグレーアウト
										}
										Cell contentCell2 = contentRow2.getCell(0);
										if (contentCell2 != null) {
											// グレーアウトを定義
											newStyle.cloneStyleFrom(contentCell2.getCellStyle());
											newStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
											newStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
											contentCell2.setCellStyle(newStyle); // セルをグレーアウト
											// 該当するサマリもグレーアウト
											Row contentRow3 = worksheet
													.getRow(summaryRow + param.getJudgeResultStartRow() - 1);
											Cell contentCell3 = contentRow3.getCell(0);
											newStyle.cloneStyleFrom(contentCell3.getCellStyle());
											newStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
											newStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
											contentCell3.setCellStyle(newStyle); // セルをグレーアウト
											Cell contentCell4 = contentRow3.getCell(param.getJudgeResultSummaryCol());
											newStyle.cloneStyleFrom(contentCell4.getCellStyle());
											newStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
											newStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
											contentCell4.setCellStyle(newStyle); // セルをグレーアウト
										}
									}
									// 削除もしくは回答が新しくなったものをmapから削除(新規追加の回答のみ残るように)
									answerNotifiedTextMap.remove(answerId);
								} else {
									// 回答内容を設定
									Row contentRow = worksheet.getRow(rowNum + answerTitleMergeRow);
									Cell contentCell = contentRow.getCell(answerContentCol);
									contentCell.setCellValue(answerNotifiedTextMap.get(answerId));

									// 回答内容の結合行数をループ行数に追加
									rowNum = rowNum + answerContentMergeRow;
								}

							}
						}
					}
				}
				// 事前協議の場合
				if (applicationStepId.equals(APPLICATION_STEP_ID_2)) {
					// mapのサイズが0より大きい場合=新規追加された回答がある場合
					if (answerNotifiedTextMap.size() != 0) {
						// パラメータで貰ったAnswerJudgementMapの中から今回削除したものを除いた追加分だけを新たにセットする処理
						Map<Integer, Map<String, String>> answerJudgementMap = generalConditionDiagnosisReportRequestForm
								.getAnswerJudgementMap();
						// キーのリスト作成
						List<Integer> keys = new ArrayList<>(answerJudgementMap.keySet());
						for (Integer key : keys) {
							Map<String, String> answerJudgement = answerJudgementMap.get(key);
							String answerId = answerJudgement.get("answerId");
							if (!(answerId == null)) {
								String text = answerNotifiedTextMap.get(Integer.parseInt(answerId));
								if (text == null) {
									answerJudgementMap.remove(key);
								}
							}
						}
						// 上記で新規作成した追加分のリストをセット
						generalConditionDiagnosisReportRequestForm.setAnswerJudgementMap(answerJudgementMap);
						List<Integer> newKeys = new ArrayList<>(answerJudgementMap.keySet());
						List<GeneralConditionDiagnosisResultForm> newGeneralConditionDiagnosisResults = new ArrayList<GeneralConditionDiagnosisResultForm>();
						// GeneralConditionDiagnosisResultsも再び再作成
						List<GeneralConditionDiagnosisResultForm> generalConditionDiagnosisResults = generalConditionDiagnosisReportRequestForm
								.getGeneralConditionDiagnosisResults();
						for (Integer key : newKeys) {
							for (GeneralConditionDiagnosisResultForm generalConditionDiagnosisResult : generalConditionDiagnosisResults) {
								if (generalConditionDiagnosisResult.getJudgeResultItemId().equals(key)) {
									GeneralConditionDiagnosisResultForm generalConditionDiagnosisResultForm = new GeneralConditionDiagnosisResultForm();
									// 区分判定ID
									generalConditionDiagnosisResultForm
											.setJudgementId(generalConditionDiagnosisResult.getJudgementId());
									// 判定結果項目ID
									generalConditionDiagnosisResultForm.setJudgeResultItemId(
											generalConditionDiagnosisResult.getJudgeResultItemId());
									// 文言
									generalConditionDiagnosisResultForm
											.setDescription(generalConditionDiagnosisResult.getDescription());
									// タイトル
									generalConditionDiagnosisResultForm
											.setTitle(generalConditionDiagnosisResult.getTitle());
									// サマリ
									generalConditionDiagnosisResultForm
											.setSummary(generalConditionDiagnosisResult.getSummary());
									newGeneralConditionDiagnosisResults.add(generalConditionDiagnosisResultForm);
								}
							}
						}
						generalConditionDiagnosisReportRequestForm
								.setGeneralConditionDiagnosisResults(newGeneralConditionDiagnosisResults);
						
						setRowBreak(wb, TARGET_SHEET_INDEX, worksheet.getLastRowNum());
						param.setJudgeResultStartRow(worksheet.getLastRowNum()+1);
						// 判定結果summary出力
						int currentRow = writeJudgeResultSummary(wb, generalConditionDiagnosisReportRequestForm, param);
						// 判定結果description出力
						writeJudgeResultDescription(wb, generalConditionDiagnosisReportRequestForm, param,
								currentRow );
					}
				}
			}
			return wb;
		} finally {
			ZipSecureFile.setMinInflateRatio(0.01);
			LOGGER.debug(" 行政から回答レポート帳票生成 終了");
		}
	}

	/**
	 * 地番文字列を結合する
	 * 
	 * @param lotNumbers          地番情報リスト
	 * @param lotNumberSeparators 番地区切り文字群(正規表現)
	 * @param separator           複数項目の区切り文字
	 * @return 結合文字列
	 */
	public String getAddressText(List<LotNumberForm> lotNumbers, String lotNumberSeparators, String separator) {
		String addressText = EMPTY;

		// ソート
		Collections.sort(lotNumbers, new LotNumberComparator(lotNumberSeparators));

		String preCityName = null;
		String preDistrictName = null;
		String preLotNumberName = null;
		for (LotNumberForm lot : lotNumbers) {
			String cityName = lot.getCityName() != null ? lot.getCityName() : EMPTY;
			String districtName = lot.getDistrictName() != null ? lot.getDistrictName() : EMPTY;
			String lotNumberName = lot.getChiban() != null ? lot.getChiban() : EMPTY;

			if (cityName.equals(preCityName) && districtName.equals(preDistrictName)
					&& lotNumberName.equals(preLotNumberName)) {
				continue;
			}

			if (addressText.length() > 0) {
				addressText += separator;
			}

			if (!cityName.equals(preCityName)) {
				addressText += lot.getCityName();
			}
			if (!districtName.equals(preDistrictName)) {
				addressText += lot.getDistrictName();
			}
			if (!lotNumberName.equals(preLotNumberName)) {
				addressText += lot.getChiban();
			}
			if (lot.getFullFlag() != null && lot.getFullFlag().equals("1")) {
				addressText += "の一部";
			}

			preCityName = cityName;
			preDistrictName = districtName;
			preLotNumberName = lotNumberName;
		}

		return addressText;
	}

	/**
	 * 判定結果summary出力
	 * 
	 * @param wb                                         ワークブック
	 * @param generalConditionDiagnosisReportRequestForm リクエストパラメータ
	 * @param param                                      帳票出力設定
	 * @return 最終行
	 * @throws Exception 例外
	 */
	private int writeJudgeResultSummary(Workbook wb,
			GeneralConditionDiagnosisReportRequestForm generalConditionDiagnosisReportRequestForm,
			ExportJudgeFormParam param) throws Exception {
		LOGGER.debug("判定結果サマリ出力 開始");
		try {
			List<GeneralConditionDiagnosisResultForm> generalConditionDiagnosisResults = generalConditionDiagnosisReportRequestForm
					.getGeneralConditionDiagnosisResults();

			// 出力開始行の取得
			int currentRow = param.getJudgeResultStartRow(); // シート全体の現在位置行
			int currentPage = 0;

			Sheet sheet = wb.getSheetAt(TARGET_SHEET_INDEX);
			int pageRow = currentRow; // ページ内の現在位置行

			boolean rowBreakFlg = false; // 改ページ実施フラグ

			int mergeRow = param.getJudgeResultMergeRow(); // 1項目あたりの結合行数
			int titleCol = param.getJudgeResultTitleCol(); // タイトル出力列
			int titleMergeCol = param.getJudgeResultTitleMergeCol(); // タイトル結合列数
			int summaryCol = param.getJudgeResultSummaryCol(); // タイトル出力列
			int summaryMergeCol = param.getJudgeResultSummaryMergeCol(); // タイトル結合列数
			Font titleFont = wb.createFont(); // タイトル用フォント
			titleFont.setFontName(param.getFontName());
			titleFont.setFontHeightInPoints(param.getJudgeResultTitleFontSize());
			Font summaryFont = wb.createFont(); // サマリ用フォント
			summaryFont.setFontName(param.getFontName());
			summaryFont.setFontHeightInPoints(param.getJudgeResultSummaryFontSize());
			int resultCount = 1;
			int sumResult = 1;

			for (GeneralConditionDiagnosisResultForm generalConditionDiagnosisResult : generalConditionDiagnosisResults) {
				LOGGER.trace("セルスタイル設定 開始: " + currentRow);
				// title列の結合処理
				sheet.addMergedRegion(new CellRangeAddress( //
						currentRow, //
						currentRow + mergeRow - 1, //
						titleCol, //
						titleCol + titleMergeCol - 1));
				// summary列の結合処理
				sheet.addMergedRegion(new CellRangeAddress( //
						currentRow, //
						currentRow + mergeRow - 1, //
						summaryCol, //
						summaryCol + summaryMergeCol - 1));

				// 罫線
				for (int row = currentRow; row < currentRow + mergeRow; row++) {
					// 偶数個目の判定だけ行載せるすべてにスタイルセット
					LOGGER.debug(String.valueOf(sumResult) + "   " + String.valueOf(generalConditionDiagnosisResults.size()));
					if(resultCount%2 == 1 && (pageRow + mergeRow * 2) != param.getPageMaxRow() && sumResult < generalConditionDiagnosisResults.size()) {
						// 右端
						CellStyle tmpCellStyle = new CellStyle(TARGET_SHEET_INDEX, row, titleCol);
						tmpCellStyle.setFont(titleFont);
						setStyle(wb, tmpCellStyle);
						// 真ん中
						tmpCellStyle.setCol(summaryCol);
						tmpCellStyle.setFont(summaryFont);
						setStyle(wb, tmpCellStyle);
						// 左端
						tmpCellStyle.setCol((summaryCol + summaryMergeCol - 1));
						tmpCellStyle.setFont(summaryFont);
						setStyle(wb, tmpCellStyle);
					}else {
						// title
						for (int col = titleCol; col < titleCol + titleMergeCol; col++) {
							CellStyle tmpCellStyle = new CellStyle(TARGET_SHEET_INDEX, row, col);
							tmpCellStyle.setFont(titleFont);
							setStyle(wb, tmpCellStyle);
						}
	
						// summary
						for (int col = summaryCol; col < summaryCol + summaryMergeCol; col++) {
							CellStyle tmpCellStyle = new CellStyle(TARGET_SHEET_INDEX, row, col);
							tmpCellStyle.setFont(summaryFont);
							setStyle(wb, tmpCellStyle);
						}
					}
				}
				LOGGER.trace("セルスタイル設定 終了: " + currentRow);

				// title出力
				LOGGER.trace("タイトル出力 開始: " + currentRow);
				writeTextValue(wb, new TextValue(generalConditionDiagnosisResult.getTitle(), TARGET_SHEET_INDEX,
						currentRow, titleCol));
				LOGGER.trace("タイトル出力 終了: " + currentRow);

				// summary出力
				LOGGER.trace("サマリ出力 開始: " + currentRow);
				writeTextValue(wb, new TextValue(generalConditionDiagnosisResult.getSummary(), TARGET_SHEET_INDEX,
						currentRow, summaryCol));
				LOGGER.trace("サマリ出力 終了: " + currentRow);

				rowBreakFlg = false;

				currentRow += mergeRow;
				pageRow += mergeRow;

				resultCount++;
				sumResult++;
				if (pageRow - param.getPageMaxRow() >= 1) {
					pageRow -= currentRow;
				}
				if ((pageRow + mergeRow) >= param.getPageMaxRow()) {
					LOGGER.trace("改ページ 開始: " + currentPage);
					// 最大値を超えるので、ページが変わる

					// 現在ページをリセット
					pageRow = 0;

					// 改ページ設定
					setRowBreak(wb, TARGET_SHEET_INDEX, currentRow - 1);
					rowBreakFlg = true;
					resultCount = 0;

					LOGGER.trace("改ページ 終了: " + currentPage);
					currentPage++;
				}
			}
			if (!rowBreakFlg) {
				LOGGER.trace("改ページ(最終) 開始: " + currentPage);

				// 改ページ設定
				setRowBreak(wb, TARGET_SHEET_INDEX, currentRow - 1);
				LOGGER.trace("改ページ(最終) 終了: " + currentPage);
			}

			return currentRow;
		} finally {
			LOGGER.debug("判定結果サマリ出力 開始");
		}
	}

	/**
	 * 判定結果description出力
	 * 
	 * @param wb                                         ワークブック
	 * @param generalConditionDiagnosisReportRequestForm リクエストパラメータ
	 * @param param                                      帳票出力設定
	 * @param startRow                                   出力開始行
	 * @return
	 * @throws Exception
	 */
	private int writeJudgeResultDescription(Workbook wb,
			GeneralConditionDiagnosisReportRequestForm generalConditionDiagnosisReportRequestForm,
			ExportJudgeFormParam param, int startRow) throws Exception {
		LOGGER.debug("判定結果詳細出力 開始");
		try {
			List<GeneralConditionDiagnosisResultForm> generalConditionDiagnosisResults = generalConditionDiagnosisReportRequestForm
					.getGeneralConditionDiagnosisResults();
			// 回答IDと回答内容-概況診断結果IDの紐づけ対応 ： 申請登録時の帳票作成時のみ使用。
			Map<Integer, Map<String, String>> answerJudgementMap = generalConditionDiagnosisReportRequestForm
					.getAnswerJudgementMap();
			Font titleFont = wb.createFont(); // タイトル用フォント
			titleFont.setFontName(param.getFontName());
			titleFont.setFontHeightInPoints(param.getJudgeResultDescriptionTitleFontSize());
			Font descriptionFont = wb.createFont(); // 詳細用フォント
			descriptionFont.setFontName(param.getFontName());
			descriptionFont.setFontHeightInPoints(param.getJudgeResultDescriptionFontSize());
			Font labelFont = wb.createFont(); // 画像なし用フォント
			labelFont.setFontName(param.getFontName());
			labelFont.setFontHeightInPoints(param.getJudgeResultDescriptionNoGisLabelSize());

			int tmpCurrentRow = startRow;
			int mergeRowCount = param.getJudgeResultDescriptionMergeRow();
			int rowCountPerPage = (int) Math.floor((double) param.getPageMaxRow() / (double) mergeRowCount);
			
			int titleCol = param.getJudgeResultDescriptionTitleCol(); // 判定結果詳細 タイトル出力列
			int titleMergeCol = param.getJudgeResultDescriptionTitleMergeCol(); // 判定結果詳細 タイトル結合列数
			int descriptionCol = param.getJudgeResultDescriptionCol(); // 判定結果詳細 詳細列
			int descriptionMergeCol = param.getJudgeResultDescriptionMergeCol(); // 判定結果詳細 詳細結合列数
			int imageCol = param.getJudgeResultDescriptionImageCol(); // 判定結果詳細 画像列
			int imageMergeCol = param.getJudgeResultDescriptionImageMergeCol(); // 判定結果詳細 画像結合列数

			int answerTitleCol = param.getJudgeResultDescriptionAnswerTitleCol(); // 判定結果詳細 回答タイトル出力列
			int answerTitleMergeCol = param.getJudgeResultDescriptionAnswerTitleMergeCol(); // 判定結果詳細 回答タイトル結合列数
			int answerTitleMergeRow = param.getJudgeResultDescriptionAnswerTitleMergerow(); // 判定結果詳細 回答タイトル結合行数
			int answerContentCol = param.getJudgeResultDescriptionAnswerContentCol(); // 判定結果詳細 回答内容出力列
			int answerContentMergeCol = param.getJudgeResultDescriptionAnswerContentMergeCol(); // 判定結果詳細 回答内容結合列数
			int answerContentMergeRow = param.getJudgeResultDescriptionAnswerContentMergeRow(); // 判定結果詳細 回答内容結合行数

			if (answerJudgementMap != null) {
				rowCountPerPage =  (int) Math.floor((double) param.getPageMaxRow() / (double)( mergeRowCount + answerTitleMergeRow +answerContentMergeRow));
			}
			
			Sheet sheet = wb.getSheetAt(TARGET_SHEET_INDEX);
			int tmpCount = 0;
			for (GeneralConditionDiagnosisResultForm generalConditionDiagnosisResult : generalConditionDiagnosisResults) {
				LOGGER.trace("各セル結合処理 開始: " + tmpCurrentRow);
				// description用の結合処理 title
				if (answerJudgementMap != null) {
					sheet.addMergedRegion(new CellRangeAddress(tmpCurrentRow,
							tmpCurrentRow + mergeRowCount + answerTitleMergeRow + answerContentMergeRow - 1, titleCol,
							titleCol + titleMergeCol - 1));
				} else {
					sheet.addMergedRegion(new CellRangeAddress(tmpCurrentRow, tmpCurrentRow + mergeRowCount - 1,
							titleCol, titleCol + titleMergeCol - 1));
				}
				//枠なし
				if(param.getFolderPath() != null) {
					// description用の結合処理 description
					sheet.addMergedRegion(new CellRangeAddress(tmpCurrentRow, tmpCurrentRow + mergeRowCount - 1,
							descriptionCol, descriptionCol + descriptionMergeCol - 1));
					// description用の結合処理 image
					sheet.addMergedRegion(new CellRangeAddress(tmpCurrentRow, tmpCurrentRow + mergeRowCount - 1, imageCol,
							imageCol + imageMergeCol - 1));
				}else {
					// description用の結合処理 imageがないのでdescriptionとimageのセルを結合
					sheet.addMergedRegion(new CellRangeAddress(tmpCurrentRow, tmpCurrentRow + mergeRowCount - 1,
							descriptionCol, imageCol + imageMergeCol - 1));
				}
				
				if (answerJudgementMap != null) {
					// description用の結合処理 回答タイトル
					sheet.addMergedRegion(new CellRangeAddress(tmpCurrentRow + mergeRowCount,
							tmpCurrentRow + mergeRowCount + answerTitleMergeRow - 1, answerTitleCol,
							answerTitleCol + answerTitleMergeCol - 1));
					// description用の結合処理 回答内容
					sheet.addMergedRegion(new CellRangeAddress(tmpCurrentRow + mergeRowCount + answerTitleMergeRow,
							tmpCurrentRow + mergeRowCount + answerTitleMergeRow + answerContentMergeRow - 1,
							answerContentCol, answerContentCol + answerContentMergeCol - 1));
				}

				LOGGER.trace("各セル結合処理 終了: " + tmpCurrentRow);

				// description用のセルスタイル処理
				LOGGER.trace("各セルスタイル設定処理 開始: " + tmpCurrentRow);
				for (int row = tmpCurrentRow; row < tmpCurrentRow + mergeRowCount; row++) {
					if((tmpCount == 0 && row == tmpCurrentRow) || row == (tmpCurrentRow + mergeRowCount - 1)) {
						for (int col = titleCol; col < descriptionCol; col++) {
							CellStyle tmpCellStyle = new CellStyle(TARGET_SHEET_INDEX, row, col);
							tmpCellStyle.setFont(titleFont);
							setStyle(wb, tmpCellStyle);
						}
						//枠なし
						if(param.getFolderPath() != null) {
							for (int col = descriptionCol; col < imageCol; col++) {
								CellStyle tmpCellStyle = new CellStyle(TARGET_SHEET_INDEX, row, col);
								tmpCellStyle.setFont(descriptionFont);
								setStyle(wb, tmpCellStyle);
							}
							for (int col = imageCol; col < imageCol + imageMergeCol; col++) {
								setStyle(wb, new CellStyle(TARGET_SHEET_INDEX, row, col));
							}
						}else {
							for (int col = descriptionCol; col < imageCol + imageMergeCol; col++) {
								CellStyle tmpCellStyle = new CellStyle(TARGET_SHEET_INDEX, row, col);
								tmpCellStyle.setFont(descriptionFont);
								setStyle(wb, tmpCellStyle);
							}
						}
					}else {
						CellStyle tmpCellStyle = new CellStyle(TARGET_SHEET_INDEX, row, titleCol);
						// 左端
						tmpCellStyle.setFont(titleFont);
						setStyle(wb, tmpCellStyle);
						// 真ん中
						tmpCellStyle.setCol(descriptionCol);
						tmpCellStyle.setFont(descriptionFont);
						setStyle(wb, tmpCellStyle);
						if(param.getFolderPath() != null) {
							tmpCellStyle.setCol(imageCol);
							setStyle(wb, tmpCellStyle);
						}
						// 右端
						tmpCellStyle.setCol((imageCol + imageMergeCol - 1));
						tmpCellStyle.setFont(descriptionFont);
						setStyle(wb, tmpCellStyle);
					}
				}
				if (answerJudgementMap != null) {
					for (int row = tmpCurrentRow + mergeRowCount; row < tmpCurrentRow + mergeRowCount
							+ answerTitleMergeRow; row++) {
						if(row == (tmpCurrentRow + mergeRowCount + answerTitleMergeRow - 1)) {
							for (int col = titleCol; col < descriptionCol; col++) {
								CellStyle tmpCellStyle = new CellStyle(TARGET_SHEET_INDEX, row, col);
								tmpCellStyle.setFont(titleFont);
								setStyle(wb, tmpCellStyle);
							}
							for (int col = answerTitleCol; col < answerTitleCol + answerTitleMergeCol; col++) {
								CellStyle tmpCellStyle = new CellStyle(TARGET_SHEET_INDEX, row, col);
								tmpCellStyle.setFont(titleFont);
								setStyle(wb, tmpCellStyle);
							}
						}else {
							CellStyle tmpCellStyle = new CellStyle(TARGET_SHEET_INDEX, row, titleCol);
							tmpCellStyle.setFont(titleFont);
							setStyle(wb, tmpCellStyle);
							tmpCellStyle.setCol(descriptionCol);
							setStyle(wb, tmpCellStyle);
							tmpCellStyle.setCol(answerTitleCol + answerTitleMergeCol - 1);
							setStyle(wb, tmpCellStyle);
						}
					}
					for (int row = tmpCurrentRow + mergeRowCount + answerTitleMergeRow; row < tmpCurrentRow
							+ mergeRowCount + answerTitleMergeRow + answerContentMergeRow; row++) {
						if(row == (tmpCurrentRow + mergeRowCount + answerTitleMergeRow + answerContentMergeRow - 1)) {
							for (int col = titleCol; col < descriptionCol; col++) {
								CellStyle tmpCellStyle = new CellStyle(TARGET_SHEET_INDEX, row, col);
								tmpCellStyle.setFont(titleFont);
								setStyle(wb, tmpCellStyle);
							}
							for (int col = answerContentCol; col < answerContentCol + answerContentMergeCol; col++) {
								CellStyle tmpCellStyle = new CellStyle(TARGET_SHEET_INDEX, row, col);
								tmpCellStyle.setFont(descriptionFont);
								setStyle(wb, tmpCellStyle);
							}
						}else {
							CellStyle tmpCellStyle = new CellStyle(TARGET_SHEET_INDEX, row, titleCol);
							tmpCellStyle.setFont(descriptionFont);
							setStyle(wb, tmpCellStyle);
							tmpCellStyle.setCol(descriptionCol);
							setStyle(wb, tmpCellStyle);
							tmpCellStyle.setCol(answerContentCol + answerContentMergeCol -1);
							setStyle(wb, tmpCellStyle);
						}
					}
				}
				LOGGER.trace("各セルスタイル設定処理 終了: " + tmpCurrentRow);

				// title出力
				LOGGER.trace("タイトル出力 開始: " + tmpCurrentRow);
				writeTextValue(wb, new TextValue(generalConditionDiagnosisResult.getTitle(), TARGET_SHEET_INDEX,
						tmpCurrentRow, titleCol));
				LOGGER.trace("タイトル出力 終了: " + tmpCurrentRow);

				// description出力
				LOGGER.trace("詳細出力 開始: " + tmpCurrentRow);
				writeTextValue(wb, new TextValue(generalConditionDiagnosisResult.getDescription(), TARGET_SHEET_INDEX,
						tmpCurrentRow, descriptionCol));
				LOGGER.trace("詳細出力 終了: " + tmpCurrentRow);

				boolean isGisJudge = false;
				if (categoryJudgementMap.containsKey(generalConditionDiagnosisResult.getJudgementId())) {
					String tmpGisJudgeType = categoryJudgementMap.get(generalConditionDiagnosisResult.getJudgementId());
					if (!JudgementService.GIS_JUDGEMENT_0.equals(tmpGisJudgeType)) {
						// GIS判定
						isGisJudge = true;
					}
				}

				if(param.getFolderPath() != null) {
					if (isGisJudge) {
						// 画像出力
						LOGGER.trace("画像出力 開始: " + tmpCurrentRow);
						Picture descriptionPic = new Picture();
						String imagePath = param.getFolderPath();
						imagePath += PATH_SPLITTER + generalConditionDiagnosisResult.getJudgeResultItemId();
						imagePath += PATH_SPLITTER + generalConditionDiagnosisResult.getJudgeResultItemId()
								+ JUDGEMENT_IMAGE_EXTENTION;
						Path p = Paths.get(imagePath);
						if (Files.exists(p)) {
							descriptionPic.setFilePath(imagePath);
							descriptionPic.setType(Workbook.PICTURE_TYPE_PNG);
							descriptionPic.setStartCol(imageCol);
							descriptionPic.setEndCol(imageCol + imageMergeCol);
							descriptionPic.setStartRow(tmpCurrentRow);
							descriptionPic.setEndRow(tmpCurrentRow + mergeRowCount);
							writePicture(wb, descriptionPic);
							LOGGER.trace("画像出力 終了: " + tmpCurrentRow);
						} else {
							// 非該当かつ非該当時レイヤ表示無効の場合ここに遷移
							// 「画像なし 非該当」文字列出力
							LOGGER.trace("「画像なし 非該当」文字列出力 開始: " + tmpCurrentRow);
							writeTextValue(wb, new TextValue(param.getJudgeResultDescriptionNoApplyLabel(),
									TARGET_SHEET_INDEX, tmpCurrentRow, imageCol));
							LOGGER.trace("「画像なし 非該当」文字列出力 終了: " + tmpCurrentRow);
	
							// セルスタイル更新
							LOGGER.trace("セルスタイル更新 開始: " + tmpCurrentRow);
							CellStyle tmpCellStyle = new CellStyle(TARGET_SHEET_INDEX, tmpCurrentRow, imageCol);
							// 中央揃えに変更
							tmpCellStyle.setHalign(HorizontalAlignment.CENTER);
							tmpCellStyle.setValign(VerticalAlignment.CENTER);
							tmpCellStyle.setFont(labelFont);
							setStyle(wb, tmpCellStyle);
							LOGGER.trace("セルスタイル更新 終了: " + tmpCurrentRow);
						}
	
					} else {
						// 「画像なし 区分判定」文字列出力
						LOGGER.trace("「画像なし 区分判定」文字列出力 開始: " + tmpCurrentRow);
						writeTextValue(wb, new TextValue(param.getJudgeResultDescriptionNoGisLabel(), TARGET_SHEET_INDEX,
								tmpCurrentRow, imageCol));
						LOGGER.trace("「画像なし 区分判定」文字列出力 終了: " + tmpCurrentRow);
	
						// セルスタイル更新
						LOGGER.trace("セルスタイル更新 開始: " + tmpCurrentRow);
						CellStyle tmpCellStyle = new CellStyle(TARGET_SHEET_INDEX, tmpCurrentRow, imageCol);
						// 中央揃えに変更
						tmpCellStyle.setHalign(HorizontalAlignment.CENTER);
						tmpCellStyle.setValign(VerticalAlignment.CENTER);
						tmpCellStyle.setFont(labelFont);
						setStyle(wb, tmpCellStyle);
						LOGGER.trace("セルスタイル更新 終了: " + tmpCurrentRow);
					}
				}
				// 回答出力箇所生成
				if (answerJudgementMap != null) {
					// 回答タイトル
					LOGGER.trace("回答タイトル出力 開始: " + tmpCurrentRow);
					String answerId = (answerJudgementMap
							.containsKey(generalConditionDiagnosisResult.getJudgeResultItemId()))
									? answerJudgementMap.get(generalConditionDiagnosisResult.getJudgeResultItemId()).get("answerId")
									: null;
					String answerTitleText = (answerId != null)
							? param.getJudgeResultDescriptionAnswerTitleTemplateText1() + answerId
									+ param.getJudgeResultDescriptionAnswerTitleTemplateText2()
							: param.getJudgeResultDescriptionAnswerTitleNoApply();
					writeTextValue(wb, new TextValue(answerTitleText, TARGET_SHEET_INDEX, tmpCurrentRow + mergeRowCount,
							answerTitleCol));
					LOGGER.trace("回答タイトル出力 終了: " + tmpCurrentRow);
					LOGGER.trace("回答内容出力 開始: " + tmpCurrentRow);
					// 回答任意の区分判定には初期回答をセット
					String answerContent = (answerJudgementMap
							.containsKey(generalConditionDiagnosisResult.getJudgeResultItemId()))? 
							(answerJudgementMap.get(generalConditionDiagnosisResult.getJudgeResultItemId()).get("answerContent") != null)?answerJudgementMap.get(generalConditionDiagnosisResult.getJudgeResultItemId()).get("answerContent") 
							: "":"";
					if (answerId == null) {
						answerContent = param.getJudgeResultDescriptionAnswerContentNoApply();
					}

					writeTextValue(wb, new TextValue(answerContent, TARGET_SHEET_INDEX,
							tmpCurrentRow + mergeRowCount + answerTitleMergeRow, answerContentCol));
					LOGGER.trace("回答内容出力 終了: " + tmpCurrentRow);
				}
				tmpCount++;
				tmpCurrentRow += (answerJudgementMap != null)
						? mergeRowCount + answerTitleMergeRow + answerContentMergeRow
						: mergeRowCount;
				if (tmpCount >= rowCountPerPage) {
					// 改ページ設定
					LOGGER.trace("改ページ 開始");
					setRowBreak(wb, TARGET_SHEET_INDEX, tmpCurrentRow - 1);
					tmpCount = 0;
					LOGGER.trace("改ページ 終了");
				}
			}

			return tmpCurrentRow;
		} finally {
			LOGGER.debug("判定結果詳細出力 終了");
		}
	}

	/**
	 * 地番コンパレータクラス
	 */
	private class LotNumberComparator implements Comparator<LotNumberForm> {

		/** 数値化失敗時の数値 */
		private static final int ERROR_VALUE = Integer.MIN_VALUE;

		/** 区切り文字 */
		String splitter;

		/**
		 * コンストラクタ
		 * 
		 * @param splitter 地番区切り文字(正規表現)
		 */
		public LotNumberComparator(String splitter) {
			this.splitter = splitter;
		}

		/**
		 * 比較
		 */
		@Override
		public int compare(LotNumberForm f1, LotNumberForm f2) {
			String cityName1 = f1.getCityName() != null ? f1.getCityName() : EMPTY;
			String districtName1 = f1.getDistrictName() != null ? f1.getDistrictName() : EMPTY;
			String districtKana1 = f1.getDistrictKana() != null ? f1.getDistrictKana() : EMPTY;
			String lotNumberName1 = f1.getChiban() != null ? f1.getChiban() : EMPTY;

			String cityName2 = f2.getCityName() != null ? f2.getCityName() : EMPTY;
			String districtName2 = f2.getDistrictName() != null ? f2.getDistrictName() : EMPTY;
			String districtKana2 = f2.getDistrictKana() != null ? f2.getDistrictKana() : EMPTY;
			String lotNumberName2 = f2.getChiban() != null ? f2.getChiban() : EMPTY;

			int cityCompareResult = cityName1.compareTo(cityName2);
			int districtCompareResult = districtName1.compareTo(districtName2);
			int districtKanaCompareResult = districtKana1.compareTo(districtKana2);
			int lotnumberCompareResult = lotNumberComparator(lotNumberName1, lotNumberName2);
			if (cityCompareResult != 0) {
				return cityCompareResult;
			} else if (districtKanaCompareResult != 0) {
				return districtKanaCompareResult;
			} else if (districtCompareResult != 0) {
				return districtCompareResult;
			} else {
				return lotnumberCompareResult;
			}
		}

		/**
		 * 地番比較処理
		 * 
		 * @param l1 地番1
		 * @param l2 地番2
		 * @return 判定結果
		 */
		private int lotNumberComparator(String l1, String l2) {
			// 「XX-YY」のような形式を区切り単位で比較する
			String[] l1Array = l1.split(splitter);
			String[] l2Array = l2.split(splitter);

			int roopCount = Math.max(l1Array.length, l2Array.length);

			for (int i = 0; i < roopCount; i++) {
				String l1Text;
				if (l1Array.length - 1 < i) {
					l1Text = "-1";
				} else {
					l1Text = l1Array[i];
					if (EMPTY.equals(l1Text)) {
						// 空文字は-1として計算
						l1Text = "-1"; //
					}
				}
				String l2Text;
				if (l2Array.length - 1 < i) {
					l2Text = "-1";
				} else {
					l2Text = l2Array[i];
					if (EMPTY.equals(l2Text)) {
						// 空文字は0として計算
						l2Text = "-1"; //
					}
				}

				int l1Int = tryParseInt(l1Text);
				int l2Int = tryParseInt(l2Text);
				if (l1Int != ERROR_VALUE && l2Int != ERROR_VALUE) {
					// 両方数値化可能
					int compareResult = l1Int - l2Int;
					if (compareResult != 0) {
						return compareResult;
					}
				} else {
					// 文字列比較
					int compareResult = l1Text.compareTo(l2Text);
					if (compareResult != 0) {
						return compareResult;
					}
				}
			}

			// 文字列比較
			return l1.compareTo(l2);
		}

		/**
		 * 文字列の数値変換を試みる
		 */
		private int tryParseInt(String txt) {
			try {
				return Integer.parseInt(txt);
			} catch (Exception ex) {
				return ERROR_VALUE;
			}
		}

	}
}
