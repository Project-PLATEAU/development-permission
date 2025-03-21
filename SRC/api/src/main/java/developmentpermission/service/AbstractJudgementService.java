package developmentpermission.service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.FileSystemUtils;

import developmentpermission.entity.Answer;
import developmentpermission.entity.CategoryJudgement;
import developmentpermission.entity.CategoryJudgementResult;
import developmentpermission.form.GeneralConditionDiagnosisReportRequestForm;
import developmentpermission.form.GeneralConditionDiagnosisResultForm;
import developmentpermission.repository.AnswerRepository;
import developmentpermission.repository.CategoryJudgementRepository;
import developmentpermission.repository.JudgementResultRepository;
import developmentpermission.util.ExportJudgeForm;
import developmentpermission.util.model.ExportJudgeFormParam;

/**
 * 概況診断結果レポート帳票出力設定記述用
 */
public abstract class AbstractJudgementService extends AbstractService {

	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractJudgementService.class);

	/** 出力帳票名 */
	@Value("${app.judge.report.name}")
	protected String judgeReportFileName;

	/** 出力帳票名（回答レポート） */
	@Value("${app.answer.report.name}")
	protected String answerReportFileName;
	
	/** テンプレートパス */
	@Value("${app.judge.report.path}")
	protected String templatePath;

	/** ページ当たりの最大行数 */
	@Value("${app.judge.report.page.maxrow}")
	protected int pageMaxRow;

	/** フォント名 */
	@Value("${app.judge.report.font.name}")
	protected String fontName;

	/** 複数項目の区切り文字 */
	@Value("${app.judge.report.separator}")
	protected String separator;

	/** 出力日 出力行 */
	@Value("${app.judge.report.date.row}")
	protected int dateRow;
	/** 出力日 出力列 */
	@Value("${app.judge.report.date.col}")
	protected int dateCol;
	/** 出力日 フォーマット */
	@Value("${app.judge.report.date.format:yyyy/MM/dd}")
	protected String dateFormat;

	/** 概況図 開始行 */
	@Value("${app.judge.report.overview.startrow}")
	protected int overviewStartRow;
	/** 概況図 終了行 */
	@Value("${app.judge.report.overview.endrow}")
	protected int overviewEndRow;
	/** 概況図 開始列 */
	@Value("${app.judge.report.overview.startcol}")
	protected int overviewStartCol;
	/** 概況図 終了列 */
	@Value("${app.judge.report.overview.endcol}")
	protected int overviewEndCol;

	/** 区分 開始行 */
	@Value("${app.judge.report.category.startrow}")
	protected int categoryStartRow;
	/** 区分 終了行 */
	@Value("${app.judge.report.category.endrow}")
	protected int categoryEndRow;
	/** 区分名 出力列 */
	@Value("${app.judge.report.title.col}")
	protected int categoryTitleCol;
	/** 区分詳細 出力列 */
	@Value("${app.judge.report.description.col}")
	protected int categoryDescriptionCol;

	/** 開発予定地 出力行 */
	@Value("${app.judge.report.address.row}")
	protected int addressRow;
	/** 開発予定地 出力列 */
	@Value("${app.judge.report.address.col}")
	protected int addressCol;
	/** 開発予定地 番地区切り文字群(正規表現) */
	@Value("${app.judge.report.lotnumber.separators}")
	protected String lotNumberSeparators;

	/** 判定結果出力開始行 */
	@Value("${app.judge.report.judgeresult.startrow}")
	protected int judgeResultStartRow;
	/** 判定結果 結合行数 */
	@Value("${app.judge.report.judgeresult.mergerow}")
	protected int judgeResultMergeRow;
	/** 判定結果タイトル 出力列 */
	@Value("${app.judge.report.judgeresult.title.col}")
	protected int judgeResultTitleCol;
	/** 判定結果タイトル 結合列数 */
	@Value("${app.judge.report.judgeresult.title.mergecol}")
	protected int judgeResultTitleMergeCol;
	/** 判定結果タイトル 文字サイズ */
	@Value("${app.judge.report.judgeresult.title.font.size}")
	protected short judgeResultTitleFontSize;
	/** 判定結果要約 出力列 */
	@Value("${app.judge.report.judgeresult.summary.col}")
	protected int judgeResultSummaryCol;
	/** 判定結果要約 結合列数 */
	@Value("${app.judge.report.judgeresult.summary.mergecol}")
	protected int judgeResultSummaryMergeCol;
	/** 判定結果要約 文字サイズ */
	@Value("${app.judge.report.judgeresult.summary.font.size}")
	protected short judgeResultSummaryFontSize;

	/** 判定結果詳細 行結合数 */
	@Value("${app.judge.report.judgeresult.description.mergerow}")
	protected int judgeResultDescriptionMergeRow;
	/** 判定結果詳細 タイトル出力列 */
	@Value("${app.judge.report.judgeresult.description.title.col}")
	protected int judgeResultDescriptionTitleCol;
	/** 判定結果詳細 タイトル 文字サイズ */
	@Value("${app.judge.report.judgeresult.description.title.font.size}")
	protected short judgeResultDescriptionTitleFontSize;
	/** 判定結果詳細 タイトル 結合列数 */
	@Value("${app.judge.report.judgeresult.description.title.mergecol}")
	protected int judgeResultDescriptionTitleMergeCol;
	/** 判定結果詳細 詳細列 */
	@Value("${app.judge.report.judgeresult.description.col}")
	protected int judgeResultDescriptionCol;
	/** 判定結果詳細 詳細結合列数 */
	@Value("${app.judge.report.judgeresult.description.mergecol}")
	protected int judgeResultDescriptionMergeCol;
	/** 判定結果詳細 詳細 文字サイズ */
	@Value("${app.judge.report.judgeresult.description.font.size}")
	protected short judgeResultDescriptionFontSize;
	/** 判定結果詳細 画像列 */
	@Value("${app.judge.report.judgeresult.description.image.col}")
	protected int judgeResultDescriptionImageCol;
	/** 判定結果詳細 画像結合列数 */
	@Value("${app.judge.report.judgeresult.description.image.mergecol}")
	protected int judgeResultDescriptionImageMergeCol;

	/** 判定結果詳細 「画像なし 区分判定」ラベル */
	@Value("${app.judge.report.judgeresult.description.nogis.label}")
	protected String judgeResultDescriptionNoGisLabel;
	/** 「画像なし 区分判定」ラベル文字サイズ */
	@Value("${app.judge.report.judgeresult.description.nogis.label.size}")
	protected short judgeResultDescriptionNoGisLabelSize;

	/** 判定結果詳細 回答タイトル結合行数 **/
	@Value("${app.judge.report.judgeresult.description.answer.title.megerow}")
	protected int judgeResultDescriptionAnswerTitleMergerow;
	
	/** 判定結果詳細 回答タイトル結合列数 **/
	@Value("${app.judge.report.judgeresult.description.answer.title.mergecol}")
	protected int judgeResultDescriptionAnswerTitleMergeCol;
	
	/** 判定結果詳細 回答タイトルテンプレート文字列1 **/
	@Value("${app.judge.report.judgeresult.description.answer.title.template.text1}")
	protected String judgeResultDescriptionAnswerTitleTemplateText1;
	
	/** 判定結果詳細 回答タイトルテンプレート文字列2 **/
	@Value("${app.judge.report.judgeresult.description.answer.title.template.text2}")
	protected String judgeResultDescriptionAnswerTitleTemplateText2;
	
	/** 判定結果詳細 回答タイトルテンプレート文字列3 **/
	@Value("${app.judge.report.judgeresult.description.answer.title.template.text3}")
	protected String judgeResultDescriptionAnswerTitleTemplateText3;
	
	/** 判定結果詳細 回答タイトル出力列 **/
	@Value("${app.judge.report.judgeresult.description.answer.title.col}")
	protected int judgeResultDescriptionAnswerTitleCol;
	
	/** 判定結果詳細 回答内容結合行数 **/
	@Value("${app.judge.report.judgeresult.description.answer.content.mergerow}")
	protected int judgeResultDescriptionAnswerContentMergeRow;
	
	/** 判定結果詳細 回答内容結合列数**/
	@Value("${app.judge.report.judgeresult.description.answer.content.mergecol}")
	protected int judgeResultDescriptionAnswerContentMergeCol;
	
	/** 判定結果詳細 回答内容出力列 **/
	@Value("${app.judge.report.judgeresult.description.answer.content.col}")
	protected int judgeResultDescriptionAnswerContentCol;
	
	/** 「画像なし 非該当」ラベル */
	@Value("${app.judge.report.judgeresult.description.noapply.label}")
	protected String judgeResultDescriptionNoApplyLabel;
	
	/** 判定結果詳細 回答タイトル非該当時文字列 */
	@Value("${app.judge.report.judgeresult.description.answer.title.noapply}")
	protected String judgeResultDescriptionAnswerTitleNoApply;
	
	/** 判定結果詳細 回答内容非該当時文字列 */
	@Value("${app.judge.report.judgeresult.description.answer.content.noapply}")
	protected String judgeResultDescriptionAnswerContentNoApply;
	
	/** 判定結果詳細行政による回答削除 **/
	@Value("${app.judge.report.judgeresult.description.answer.delete}")
	protected String judgeResultDescriptionAnswerDelete;
	
	/** M_区分判定Repositoryインスタンス */
	@Autowired
	protected CategoryJudgementRepository categoryJudgementRepository;
	
	/**
	 * 帳票出力設定を取得
	 * 
	 * @return 帳票出力設定
	 */
	protected ExportJudgeFormParam getExportParam() {
		ExportJudgeFormParam param = new ExportJudgeFormParam();
		param.setTemplatePath(templatePath);
		param.setPageMaxRow(pageMaxRow);
		param.setFontName(fontName);
		param.setSeparator(separator);
		param.setDateRow(dateRow);
		param.setDateCol(dateCol);
		param.setDateFormat(dateFormat);
		param.setOverviewStartRow(overviewStartRow);
		param.setOverviewEndRow(overviewEndRow);
		param.setOverviewStartCol(overviewStartCol);
		param.setOverviewEndCol(overviewEndCol);
		param.setCategoryStartRow(categoryStartRow);
		param.setCategoryEndRow(categoryEndRow);
		param.setCategoryTitleCol(categoryTitleCol);
		param.setCategoryDescriptionCol(categoryDescriptionCol);
		param.setAddressRow(addressRow);
		param.setAddressCol(addressCol);
		param.setLotNumberSeparators(lotNumberSeparators);
		param.setJudgeResultStartRow(judgeResultStartRow);
		param.setJudgeResultMergeRow(judgeResultMergeRow);
		param.setJudgeResultTitleCol(judgeResultTitleCol);
		param.setJudgeResultTitleMergeCol(judgeResultTitleMergeCol);
		param.setJudgeResultTitleFontSize(judgeResultTitleFontSize);
		param.setJudgeResultSummaryCol(judgeResultSummaryCol);
		param.setJudgeResultSummaryMergeCol(judgeResultSummaryMergeCol);
		param.setJudgeResultSummaryFontSize(judgeResultSummaryFontSize);
		param.setJudgeResultDescriptionMergeRow(judgeResultDescriptionMergeRow);
		param.setJudgeResultDescriptionTitleCol(judgeResultDescriptionTitleCol);
		param.setJudgeResultDescriptionTitleMergeCol(judgeResultDescriptionTitleMergeCol);
		param.setJudgeResultDescriptionTitleFontSize(judgeResultDescriptionTitleFontSize);
		param.setJudgeResultDescriptionCol(judgeResultDescriptionCol);
		param.setJudgeResultDescriptionMergeCol(judgeResultDescriptionMergeCol);
		param.setJudgeResultDescriptionFontSize(judgeResultDescriptionFontSize);
		param.setJudgeResultDescriptionImageCol(judgeResultDescriptionImageCol);
		param.setJudgeResultDescriptionImageMergeCol(judgeResultDescriptionImageMergeCol);
		param.setJudgeResultDescriptionNoGisLabel(judgeResultDescriptionNoGisLabel);
		param.setJudgeResultDescriptionNoGisLabelSize(judgeResultDescriptionNoGisLabelSize);
		param.setJudgeResultDescriptionAnswerTitleMergerow(judgeResultDescriptionAnswerTitleMergerow);
		param.setJudgeResultDescriptionAnswerTitleMergeCol(judgeResultDescriptionAnswerTitleMergeCol);
		param.setJudgeResultDescriptionAnswerTitleTemplateText1(judgeResultDescriptionAnswerTitleTemplateText1);
		param.setJudgeResultDescriptionAnswerTitleTemplateText2(judgeResultDescriptionAnswerTitleTemplateText2);
		param.setJudgeResultDescriptionAnswerTitleTemplateText3(judgeResultDescriptionAnswerTitleTemplateText3);
		param.setJudgeResultDescriptionAnswerTitleCol(judgeResultDescriptionAnswerTitleCol);
		param.setJudgeResultDescriptionAnswerContentMergeRow(judgeResultDescriptionAnswerContentMergeRow);
		param.setJudgeResultDescriptionAnswerContentMergeCol(judgeResultDescriptionAnswerContentMergeCol);
		param.setJudgeResultDescriptionAnswerContentCol(judgeResultDescriptionAnswerContentCol);
		param.setJudgeResultDescriptionAnswerTitleNoApply(judgeResultDescriptionAnswerTitleNoApply);
		param.setJudgeResultDescriptionAnswerContentNoApply(judgeResultDescriptionAnswerContentNoApply);
		param.setJudgeResultDescriptionNoApplyLabel(judgeResultDescriptionNoApplyLabel);
		param.setJudgeResultDescriptionAnswerDelete(judgeResultDescriptionAnswerDelete);
		return param;
	}

	/**
	 * 帳票生成
	 * 
	 * @param form リクエストパラメータ
	 * @return 生成帳票
	 * @throws Exception 例外
	 */
	protected Workbook exportJudgeReportWorkBook(GeneralConditionDiagnosisReportRequestForm form) throws Exception {
		// 区分判定リスト取得
		List<CategoryJudgement> categoryJudgementList = categoryJudgementRepository.getCategoryJudgementList();
		// 帳票生成
		ExportJudgeForm exportForm = new ExportJudgeForm(categoryJudgementList);
		ExportJudgeFormParam formParam = getExportParam();
		
		if(form.getFolderName() != null && !form.getFolderName().equals("")) {
			formParam.setFolderPath(judgementFolderPath + PATH_SPLITTER + form.getFolderName());
			Path directoryPath = Paths.get(formParam.getFolderPath());
			if (!Files.exists(directoryPath)) {
				LOGGER.error("指定フォルダが存在しない: " + directoryPath);
				return null;
			}
		}else {
			formParam.setFolderPath(null);
		}
		return exportForm.createJudgeReportWorkBook(form, formParam);
	}

	/**
	 * 行政回答レポート帳票生成
	 * 
	 * @param tempFilePath テンプレートファイルのパス
	 * @param answerNotifiedTextMap  回答ID-回答内容の紐づけ情報
	 * @return 生成帳票
	 * @throws Exception 例外
	 */
	protected Workbook exportAnswerReportWorkBook(String tempFilePath, Map<Integer, String> answerNotifiedTextMap,Integer applicationStepId,GeneralConditionDiagnosisReportRequestForm generalConditionDiagnosisReportRequestForm) throws Exception {
		// 帳票生成
		ExportJudgeForm exportForm = new ExportJudgeForm();
		// 帳票作成用プロパティパラメータを設定する
		ExportJudgeFormParam formParam = getExportParam();
		return exportForm.createAnswerReportWorkBook(tempFilePath, answerNotifiedTextMap, formParam,applicationStepId,generalConditionDiagnosisReportRequestForm);
	}
	
	/**
	 * 一時フォルダ削除
	 * 
	 * @param form パラメータ
	 */
	protected void deleteTmpFolder(GeneralConditionDiagnosisReportRequestForm form) {
		String tmpFolderPath = judgementFolderPath + PATH_SPLITTER + form.getFolderName();
		if (!FileSystemUtils.deleteRecursively(new File(tmpFolderPath))) {
			LOGGER.error("一時フォルダの削除に失敗: " + tmpFolderPath);
			throw new RuntimeException("一時フォルダの削除に失敗");
		}
	}
}
