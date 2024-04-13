package developmentpermission.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import developmentpermission.dao.AnswerDao;
import developmentpermission.dao.ApplicationDao;
import developmentpermission.entity.Answer;
import developmentpermission.entity.AnswerFile;
import developmentpermission.entity.AnswerFileHistory;
import developmentpermission.entity.AnswerFileHistoryView;
import developmentpermission.entity.AnswerHistory;
import developmentpermission.entity.ApplicantInformation;
import developmentpermission.entity.Application;
import developmentpermission.entity.ApplicationFile;
import developmentpermission.entity.Department;
import developmentpermission.entity.GovernmentUser;
import developmentpermission.entity.LotNumberAndDistrict;
import developmentpermission.entity.LotNumberSearchResultDefinition;
import developmentpermission.form.AnswerFileForm;
import developmentpermission.form.AnswerFileHistoryForm;
import developmentpermission.form.AnswerForm;
import developmentpermission.form.AnswerHistoryForm;
import developmentpermission.form.ApplyAnswerForm;
import developmentpermission.form.GovernmentUserForm;
import developmentpermission.form.LotNumberForm;
import developmentpermission.form.QuoteFileForm;
import developmentpermission.repository.AnswerFileRepository;
import developmentpermission.repository.AnswerHistoryRepository;
import developmentpermission.repository.AnswerRepository;
import developmentpermission.repository.ApplicantInformationRepository;
import developmentpermission.repository.ApplicationFileRepository;
import developmentpermission.repository.ApplicationRepository;
import developmentpermission.repository.DepartmentRepository;
import developmentpermission.repository.LotNumberSearchResultDefinitionRepository;
import developmentpermission.repository.RoadJudgeLabelRepository;
import developmentpermission.repository.jdbc.AnswerFileHistoryJdbc;
import developmentpermission.repository.jdbc.AnswerFileJdbc;
import developmentpermission.repository.jdbc.AnswerHistoryJdbc;
import developmentpermission.repository.jdbc.AnswerJdbc;
import developmentpermission.repository.jdbc.ApplicationJdbc;
import developmentpermission.repository.jdbc.QuoteFileJdbc;
import developmentpermission.util.AuthUtil;
import developmentpermission.util.ExportJudgeForm;
import developmentpermission.util.LogUtil;
import developmentpermission.util.MailMessageUtil;
import developmentpermission.util.model.MailItem;

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

	/** 回答ファイル履歴更新タイプ定義JSON */
	@Value("${app.def.answerfilehistory.updatetype}")
	private String answerFileHistoryUpdateTypeJson;

	/** 申請登録時の概況診断レポートのファイルID */
	@Value("${app.application.report.fileid}")
	protected String applicationReportFileId;

	/**
	 * 回答登録のパラメータチェック
	 * 
	 * @param answerFormList 登録パラメータ
	 * @param departmentId   部署ID
	 * @return 判定結果
	 */
	public boolean validateRegistAnswersParam(List<AnswerForm> answerFormList, String departmentId) {
		LOGGER.debug("回答登録のパラメータチェック 開始");
		try {
			if (answerFormList.size() == 0 //
					|| departmentId == null || EMPTY.equals(departmentId)) {
				// 登録データが空
				LOGGER.warn("登録パラメータが空、または部署IDがnullまたは空");
				return false;
			}

			// 基準申請ID
			Integer baseApplicationId = null;

			for (AnswerForm answerForm : answerFormList) {
				// 部署チェック
				LOGGER.trace("部署チェック 開始");
				boolean departmentFlg = false;
				AnswerDao dao = new AnswerDao(emf);
				List<Department> departmentList = dao.getDepartmentList(answerForm.getAnswerId());
				for (Department department : departmentList) {
					if (departmentId.equals(department.getDepartmentId())) {
						departmentFlg = true;
						break;
					}
				}
				if (!departmentFlg) {
					// 回答アクセス権限がない
					LOGGER.warn("回答アクセス権限がない");
					return false;
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
				}
				LOGGER.trace("回答データチェック 終了");
			}
			return true;
		} finally {
			LOGGER.debug("回答登録のパラメータチェック 終了");
		}
	}

	/**
	 * 回答登録(行政のみ)
	 * @param answerFormList 登録パラメータ
	 * @param loginId ログインID
	 * @param departmentName 部署名
	 * @param userId ユーザーID
	 * @param accessId アクセスID
	 */
	public void registAnswers(List<AnswerForm> answerFormList, String loginId, String departmentName, String userId, String accessId) {
		LOGGER.debug("回答登録 開始");
		try {
			LOGGER.trace("回答情報更新 開始");
			// 「登録」と言いつつ、Updateのみ
			Integer baseApplicationId = null;
			for (AnswerForm answerForm : answerFormList) {
				if (answerJdbc.update(answerForm) != 1) {
					LOGGER.warn("回答情報の更新件数不正");
					throw new RuntimeException("回答情報の更新に失敗");
				}
				if (baseApplicationId == null) {
					List<Answer> answerList = answerRepository.findByAnswerId(answerForm.getAnswerId());
					if (answerList.size() != 1) {
						LOGGER.error("回答情報の取得に失敗");
						throw new RuntimeException("回答情報の取得に失敗");
					}
					Answer answer = answerList.get(0);
					baseApplicationId = answer.getApplicationId();
				}
				// 回答履歴登録
				if (answerHistoryJdbc.insert(answerForm.getAnswerId(), userId, answerForm.getAnswerContent()) != 1) {
					LOGGER.warn("回答履歴の更新件数不正");
					throw new RuntimeException("回答履歴の更新に失敗");
				}
				// 回答登録ログ出力
				try {
					Object[] logData = { accessId, LogUtil.localDateTimeToString(LocalDateTime.now()), loginId, departmentName,
							baseApplicationId, answerForm.getAnswerId(),
							answerForm.getJudgementInformation().getTitle(), answerForm.getAnswerContent() };
					LogUtil.writeLogToCsv(answerRegisterLogPath, answerRegisterLogHeader, logData);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			LOGGER.trace("回答情報更新 終了");

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
			
			List<Answer> unansweredAnswer = answerRepository.findUnansweredByApplicationId(baseApplicationId);
			String status = STATE_ANSWERING;
			if (unansweredAnswer.size() == 0) {
				status = STATE_ANSWERED;
			}
			if (!STATE_NOTIFIED_REAPP.equals(applicationList.get(0).getStatus())) {
				if (applicationJdbc.updateApplicationStatus(baseApplicationId, status) != 1) {
					LOGGER.warn("申請情報の更新件数不正");
					throw new RuntimeException("申請情報の更新に失敗");
				}
				LOGGER.trace("申請ステータス更新 終了");
			} else {
				LOGGER.trace("現状の申請ステータスが要再申請のためステータス更新は実施しない");
			}
			

			if (STATE_ANSWERING.equals(status)) {
				// 回答中
				// 行政に回答更新通知送付
				LOGGER.trace("行政に回答更新通知送付 開始");
				sendUpdatedMainToGovernmentUser(baseApplicationId, false);
				LOGGER.trace("行政に回答更新通知送付 終了");
			} else {
				// 回答完了
				// 行政に全部署回答完了通知送付
				LOGGER.trace("行政に全部署回答完了通知送付 開始");
				sendUpdatedMainToGovernmentUser(baseApplicationId, true);
				LOGGER.trace("行政に全部署回答完了通知送付 終了");
			}
		} finally {
			LOGGER.debug("回答登録 終了");
		}
	}

	/**
	 * 行政に回答更新/完了通知送付
	 * 
	 * @param applicationId 申請ID
	 * @param isFinished    完了かどうか
	 */
	private void sendUpdatedMainToGovernmentUser(Integer applicationId, boolean isFinished) {
		MailItem item = new MailItem();

		List<ApplicantInformation> applicantList = applicantInformationRepository.getApplicantList(applicationId);
		if (applicantList.size() != 1) {
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

		// 地番
		ApplicationDao applicationDao = new ApplicationDao(emf);
		List<LotNumberForm> lotNumberFormList = new ArrayList<LotNumberForm>();
		List<LotNumberSearchResultDefinition> lotNumberSearchResultDefinitionList = lotNumberSearchResultDefinitionRepository
				.getLotNumberSearchResultDefinitionList();
		List<LotNumberAndDistrict> lotNumberList = applicationDao.getLotNumberList(applicationId, lonlatEpsg);
		for (LotNumberAndDistrict lotNumber : lotNumberList) {
			lotNumberFormList.add(getLotNumberFormFromEntity(lotNumber, lotNumberSearchResultDefinitionList));
		}
		ExportJudgeForm exportForm = new ExportJudgeForm();
		String addressText = exportForm.getAddressText(lotNumberFormList, lotNumberSeparators, separator);
		item.setLotNumber(addressText);

		String subject, body;
		if (!isFinished) {
			// 行政に回答更新通知送付
			if (mailSendAnswerUpdateFlg == 1) {
				subject = getMailPropValue(MailMessageUtil.KEY_UPDATE_SUBJECT, item);
				body = getMailPropValue(MailMessageUtil.KEY_UPDATE_BODY, item);
			} else {
				LOGGER.debug("行政に回答更新通知を送付しない設定になっているのでメール送信はスキップ");
				return;
			}
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
	 * 回答通知
	 * 
	 * @param applyAnswerForm 申請・回答内容確認情報フォーム
	 */
	public void notifyAnswer(ApplyAnswerForm applyAnswerForm) {
		LOGGER.debug("回答通知 開始");
		try {
			// O_申請の登録ステータスをチェック
			List<Application> applicationList = applicationRepository
					.getApplicationList(applyAnswerForm.getApplicationId());
			if (applicationList.size() != 1) {
				LOGGER.error("申請データの件数が不正");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}
			Application application = applicationList.get(0);
			String status = application.getStatus();
			if (!STATE_ANSWERING.equals(status) && !STATE_ANSWERED.equals(status) && !STATE_NOTIFIED_REAPP.equals(status)) {
				// ステータスが回答中、または回答完了、要再申請ではないので不正
				LOGGER.error("ステータスが回答中、または回答完了、要再申請でない");
				throw new ResponseStatusException(HttpStatus.CONFLICT);
			}

			LOGGER.debug("回答ファイル更新処理 開始");
			List<Answer> answerList = answerRepository.findByApplicationId(applyAnswerForm.getApplicationId());
			// 再申請要否
			boolean isReaplication = false;
			for (Answer answer : answerList) {
				LOGGER.debug("回答ID: " + answer.getAnswerId());
				if (answer.getReApplicationFlag() != null && answer.getReApplicationFlag()) {
					// 1件でも要再申請の回答があれば再申請とする。
					isReaplication = true;
				}
				// 申請IDと紐づくO_回答の回答内容(answer_content)を通知テキスト(notified_text)にCopy
				if (answerJdbc.copyNotifyText(answer) != 1) {
					LOGGER.error("回答データの更新件数が不正");
					throw new RuntimeException("回答データの更新件数が不正");
				}
				// O_回答履歴.通知フラグを更新
				try {
					answerHistoryJdbc.updateAnswerHistoryNotifyFlag(answer.getAnswerId());
				} catch(Exception e) {
					LOGGER.error("回答履歴データの更新に失敗");
					throw new RuntimeException("回答履歴データの更新に失敗");
				}
				// 申請IDと紐づくO_回答ファイルのファイルパス(file_path)を通知済みファイルパス(notified_file_path)にCopy
				List<AnswerFile> answerFileList = answerFileRepository.findByAnswerId(answer.getAnswerId());
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

					// ファイルパスは「/answer/<回答ID>/<回答ファイルID>/<timestamp>/<アップロードファイル名>」
					// 相対フォルダパス(回答ファイルIDまで)
					String folderPath = answerFolderName;
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
					} catch(Exception e) {
						LOGGER.error("回答ファイル履歴更新に失敗");
						throw new RuntimeException("回答ファイル履歴更新に失敗");
					}
				}
			}
			LOGGER.debug("回答ファイル更新処理 完了");

			// O_申請のステータスを更新
			String updateStatus = (isReaplication) ? STATE_NOTIFIED_REAPP : STATE_NOTIFIED;
			if (applicationJdbc.updateApplicationStatus(applyAnswerForm.getApplicationId(), updateStatus) != 1) {
				LOGGER.warn("申請情報の更新不正");
				throw new RuntimeException("申請情報の更新に失敗");
			}

			if (isReaplication) {
				// 事業者に再申請通知送付
				sendReapplicationFinishMailToBusinessUser(application);
			} else {
				// 事業者に回答完了通知送付
				sendFinishedMailToBusinessUser(application);
			}

		} finally {
			LOGGER.debug("回答通知 終了");
		}
	}

	/**
	 * 事業者に回答完了通知送付
	 * 
	 * @param application 申請情報
	 */
	private void sendFinishedMailToBusinessUser(Application application) {
		List<ApplicantInformation> applicantList = applicantInformationRepository
				.getApplicantList(application.getApplicationId());
		if (applicantList.size() != 1) {
			LOGGER.error("申請者データの件数が不正");
			throw new RuntimeException("申請者データの件数が不正");
		}
		ApplicantInformation applicant = applicantList.get(0);

		MailItem item = new MailItem();

		// 日時
		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(mailTimestampFormat);
		item.setTimestamp(application.getRegisterDatetime().format(dateFormat));

		// 地番
		ApplicationDao applicationDao = new ApplicationDao(emf);
		List<LotNumberForm> lotNumberFormList = new ArrayList<LotNumberForm>();
		List<LotNumberSearchResultDefinition> lotNumberSearchResultDefinitionList = lotNumberSearchResultDefinitionRepository
				.getLotNumberSearchResultDefinitionList();
		List<LotNumberAndDistrict> lotNumberList = applicationDao.getLotNumberList(application.getApplicationId(),
				lonlatEpsg);
		for (LotNumberAndDistrict lotNumber : lotNumberList) {
			lotNumberFormList.add(getLotNumberFormFromEntity(lotNumber, lotNumberSearchResultDefinitionList));
		}
		ExportJudgeForm exportForm = new ExportJudgeForm();
		String addressText = exportForm.getAddressText(lotNumberFormList, lotNumberSeparators, separator);
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
	 * 事業者に回答完了（再申請）通知送付
	 * 
	 * @param application 申請情報
	 */
	private void sendReapplicationFinishMailToBusinessUser(Application application) {
		List<ApplicantInformation> applicantList = applicantInformationRepository
				.getApplicantList(application.getApplicationId());
		if (applicantList.size() != 1) {
			LOGGER.error("申請者データの件数が不正");
			throw new RuntimeException("申請者データの件数が不正");
		}
		ApplicantInformation applicant = applicantList.get(0);

		MailItem item = new MailItem();

		// 日時
		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(mailTimestampFormat);
		item.setTimestamp(application.getRegisterDatetime().format(dateFormat));

		// 地番
		ApplicationDao applicationDao = new ApplicationDao(emf);
		List<LotNumberForm> lotNumberFormList = new ArrayList<LotNumberForm>();
		List<LotNumberSearchResultDefinition> lotNumberSearchResultDefinitionList = lotNumberSearchResultDefinitionRepository
				.getLotNumberSearchResultDefinitionList();
		List<LotNumberAndDistrict> lotNumberList = applicationDao.getLotNumberList(application.getApplicationId(),
				lonlatEpsg);
		for (LotNumberAndDistrict lotNumber : lotNumberList) {
			lotNumberFormList.add(getLotNumberFormFromEntity(lotNumber, lotNumberSearchResultDefinitionList));
		}
		ExportJudgeForm exportForm = new ExportJudgeForm();
		String addressText = exportForm.getAddressText(lotNumberFormList, lotNumberSeparators, separator);
		item.setLotNumber(addressText);

		// 事業者に回答完了（再申請）通知送付
		String subject = getMailPropValue(MailMessageUtil.KEY_REAPPLICATION_ANSWER_FINISH_SUBJECT, item);
		String body = getMailPropValue(MailMessageUtil.KEY_REAPPLICATION_ANSWER_FINISH_BODY, item);

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
//			MultipartFile uploadFile = form.getUploadFile();
			if (answerId == null //
					|| answerFileName == null || EMPTY.equals(answerFileName) //
					//|| uploadFile == null //
					|| departmentId == null || EMPTY.equals(departmentId)) {
				// パラメータ不足
				LOGGER.warn("パラメータ不足");
				return false;
			}

			// 回答ID
			if (answerRepository.findByAnswerId(answerId).size() != 1) {
				LOGGER.warn("回答データ取得件数不正");
				return false;
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
			AnswerDao dao = new AnswerDao(emf);
			List<Department> departmentList = dao.getDepartmentList(answerId);
			for (Department department : departmentList) {
				if (departmentId.equals(department.getDepartmentId())) {
					departmentFlg = true;
					break;
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
			String folderPath = answerFolderName;
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
			String folderPath = answerFolderName;
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
			// ToDo：　引用した申請ファイルを回答ファイルとして登録
			LOGGER.debug(fileRootPath + applicationFilePath);
			LOGGER.debug(absoluteFilePath);
			
			//ファイルの複製
			Path applicationFile = Paths.get(fileRootPath + applicationFilePath);
			Path answerFile = Paths.get(absoluteFilePath);
			Files.copy(applicationFile, answerFile);

			LOGGER.debug("ファイル出力 終了");
		}catch(Exception ex){
			LOGGER.debug("回答ファイル（引用）アップロードで例外発生", ex);
			throw new RuntimeException(ex);
		}finally {
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

			// 回答ID
			if (answerRepository.findByAnswerId(answerId).size() != 1) {
				LOGGER.warn("回答データの件数が不正");
				return false;
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
				// 回答情報取得
				List<Answer> answerList = answerRepository.findByAnswerId(form.getAnswerId());
				if (answerList.size() != 1) {
					LOGGER.warn("回答データ取得件数不正");
					return new ResponseEntity<Resource>(HttpStatus.SERVICE_UNAVAILABLE);
				}
				Answer answer = answerList.get(0);
				if (!answer.getNotifiedFlag()) {
					LOGGER.warn("通知フラグがfalseのファイルの取得要求(事業者)");
					return new ResponseEntity<Resource>(HttpStatus.FORBIDDEN);
				}

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
	 * @param applicationId 申請ID
	 * @param response レスポンス
	 * @return 異常であるか
	 */
	public boolean exportAnswerReportWorkBook( Integer applicationId,HttpServletResponse response) {
		LOGGER.debug("行政回答レポート帳票生成 開始");
		try {
		   
		   // 回答ID-回答内容の紐づけ情報を作成
			ApplicationDao dao = new ApplicationDao(emf);
		   final Map<Integer, String> answerNotifiedTextMap = new HashMap<Integer, String>();
		   List<Answer> answerList = dao.getAnswerList(applicationId, false);
		   for (Answer answer :answerList) {
			   answerNotifiedTextMap.put(answer.getAnswerId(), answer.getNotifiedText());
		   }
		   
		   // 申請情報を登録するときに、作成された概況診断レポートを取得する
		   List<ApplicationFile> applicationFiles=  applicationFileRepository.getApplicationFiles( applicationReportFileId, applicationId );
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
			Workbook wb =exportAnswerReportWorkBook(absoluteFilePath, answerNotifiedTextMap );

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

}
