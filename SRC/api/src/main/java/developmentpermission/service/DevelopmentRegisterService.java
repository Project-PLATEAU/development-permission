package developmentpermission.service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.persistence.EntityManagerFactory;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import developmentpermission.dao.ApplicationFileDao;
import developmentpermission.entity.AnswerFile;
import developmentpermission.entity.Application;
import developmentpermission.entity.ApplicationFile;
import developmentpermission.entity.DevelopmentDocument;
import developmentpermission.entity.DevelopmentDocumentMaster;
import developmentpermission.entity.Ledger;
import developmentpermission.form.DevelopmentDocumentFileForm;
import developmentpermission.repository.AnswerFileRepository;
import developmentpermission.repository.ApplicationFileMasterRepository;
import developmentpermission.repository.ApplicationFileRepository;
import developmentpermission.repository.ApplicationRepository;
import developmentpermission.repository.DevelopmentDocumentMasterRepository;
import developmentpermission.repository.DevelopmentDocumentRepository;
import developmentpermission.repository.LedgerMasterRepository;
import developmentpermission.repository.LedgerRepository;
import developmentpermission.repository.jdbc.DevelopmentDocumentJdbc;

/**
 * 開発登録簿Serviceクラス
 */
@Service
@Transactional
public class DevelopmentRegisterService extends AbstractService {

	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(DevelopmentRegisterService.class);

	/** ファイル管理rootパス */
	@Value("${app.file.rootpath}")
	protected String appFileRootpath;

	/** 開発登録簿生成ルートパス */
	@Value("${app.file.developmentRegister.rootPath}")
	protected String developmentRegisterRootPath;

	/** 開発登録簿情報JDBCインスタンス */
	@Autowired
	private DevelopmentDocumentJdbc developmentDocumentJdbc;

	/** M_開発登録簿Repositoryインタフェース */
	@Autowired
	private DevelopmentDocumentMasterRepository developmentDocumentMasterRepository;

	/** O_開発登録簿Repositoryインスタンス */
	@Autowired
	private DevelopmentDocumentRepository developmentDocumentRepository;

	/** M_申請ファイルRepositoryインスタンス */
	@Autowired
	private ApplicationFileMasterRepository applicationFileMasterRepository;

	/** O_申請Repositoryインスタンス */
	@Autowired
	private ApplicationRepository applicationRepository;

	/** O_申請ファイルRepositoryインスタンス */
	@Autowired
	private ApplicationFileRepository applicationFileRepository;

	/** O_回答ファイルRepositoryインスタンス */
	@Autowired
	private AnswerFileRepository answerFileRepository;

	/** M_帳票Repositoryインスタンス */
	@Autowired
	private LedgerMasterRepository ledgerMasterRepository;

	/** O_帳票Repositoryインスタンス */
	@Autowired
	private LedgerRepository ledgerRepository;

	/** 申請段階:1(事前相談) */
	public static final String APPLICATION_STEP_ID_1_NAME = "事前相談";

	/** 申請段階:2(事前協議) */
	public static final String APPLICATION_STEP_ID_2_NAME = "事前協議";

	/** 申請段階:3(許可判定) */
	public static final String APPLICATION_STEP_ID_3_NAME = "許可判定";

	/** 開発登録簿マスタID:1(最終提出書類) */
	public static final Integer DEVELOPMENT_DOCUMENT_ID_1 = 1;

	/** 開発登録簿マスタID:2(全提出書類) */
	public static final Integer DEVELOPMENT_DOCUMENT_ID_2 = 2;

	/** 開発登録簿マスタID:3(開発登録簿) */
	public static final Integer DEVELOPMENT_DOCUMENT_ID_3 = 3;

	/** Entityマネージャファクトリ */
	@Autowired
	protected EntityManagerFactory emf; // DAOでは@Autowiredが効かないのでここで定義...

	/**
	 * 許可判定完了チェック
	 * 
	 * @param applicationId
	 * @return
	 */
	public Boolean checkStatusPermissionCompleted(Integer applicationId) {
		LOGGER.debug("許可判定完了チェック 開始");
		Boolean result = false;
		try {
			// O_申請情報取得
			Application application = getApplication(applicationId);
			// 申請.ステータスが許可判定完了になっていること
			if (STATUS_PERMISSION_COMPLETED.equals(application.getStatus())) {
				result = true;
			}
			return result;
		} finally {
			LOGGER.debug("許可判定完了チェック 終了");
		}
	}

	/**
	 * 開発登録簿生成
	 * 
	 * @param applicationId 申請ID
	 */
	public void exportDevelopmentRegister(Integer applicationId) {
		LOGGER.debug("開発登録簿生成 開始");
		try {
			/**
			 * 以下、仕様で開発登録簿ファイルを出力する
			 * ■出力フォルダ
			 * 　開発登録簿ルート
			 * 　└申請ID
			 * 　　├最終版
			 * 　　│└【Ｆ①】[申請ファイルID]_[カテゴリ名]_[版番号]_[アップロード日].[拡張子]
			 * 　　├全ファイル
			 * 　　│├[カテゴリ名]
			 * 　　││　└【Ｆ②】[申請ファイルID]_[カテゴリ名]_[申請段階]_[版番号]_[アップロード日].[拡張子]
			 * 　　│└行政回答
			 * 　　│　　└[回答ID]_[申請段階]_[アップロード日].[拡張子]
			 * 　　└開発登録簿
			 * 　　　├【Ｆ③】調書
			 * 　　　└【Ｆ④】土地利用計画図
			 * ■出力ファイル
			 * 　　【Ｆ①】[申請ファイルID]_[カテゴリ名]_[版番号]_[アップロード日].[拡張子]
			 * 　　・[申請ファイルID]：O_申請ファイル.ファイルID
			 * 　　・[カテゴリ名]　　：M_申請ファイル.アップロードファイル名
			 * 　　・[版番号]　　　　：O_申請ファイル.版情報
			 * 　　・[アップロード日]：O_申請ファイル.アップロード日時
			 * 　　・[拡張子]　　　　：O_申請ファイル.拡張子
			 * 　　・[申請段階]　　　：O_申請ファイル.申請段階ID
			 * 　　
			 * 　　【Ｆ②】[回答ID]_[申請段階]_[アップロード日].[拡張子]
			 * 　　・[回答ID]　　　　：O_回答ファイル.回答ID
			 * 　　・[申請段階]　　　：O_回答ファイル.申請段階IDを元に事前相談、事前協議、許可判定に文字列変換する
			 * 　　・[アップロード日]：O_回答ファイル.ファイルパスからアップロード日を取得
			 * 　　・[拡張子]　　　　：O_回答ファイル.ファイルパスから取得
			 * 　　
			 * 　　【Ｆ③】調書　　　　　：M_帳票.帳票種類=1のファイルを出力する
			 * 　　【Ｆ④】土地利用計画図：M_申請ファイル.申請ファイル種別=1のファイルを出力する
			 */
			// -----------------------------------------------------------------
			// 開発登録簿出力ルートフォルダ存在確認
			// -----------------------------------------------------------------
			if (Objects.isNull(developmentRegisterRootPath)) {
				LOGGER.trace("開発登録簿出力フォルダパス: " + developmentRegisterRootPath);
				throw new RuntimeException("開発登録簿出力ルートフォルダ未設定");
			}
			// 開発登録簿出力ルートフォルダ存在確認
			Path outputRootPath = Paths.get(developmentRegisterRootPath);
			if (!Files.exists(outputRootPath)) {
				LOGGER.trace("開発登録簿出力ルートフォルダ: " + developmentRegisterRootPath);
				throw new RuntimeException("開発登録簿出力ルートフォルダ存在なし");
			}
			// -----------------------------------------------------------------
			// 申請IDフォルダ作成
			// -----------------------------------------------------------------
			LOGGER.trace("申請IDフォルダ作成 開始");
			Path applicationIdPath = Paths.get(developmentRegisterRootPath, applicationId.toString());
			if (!Files.exists(applicationIdPath)) {
				Files.createDirectories(applicationIdPath);
			}
			LOGGER.trace("申請IDフォルダ作成 終了");

			// -----------------------------------------------------------------
			// O_開発登録簿削除
			// -----------------------------------------------------------------
			developmentDocumentJdbc.delete(applicationId);

			// -----------------------------------------------------------------
			// 開発登録簿ルートフォルダ/[申請ID]/最終版
			// -----------------------------------------------------------------
			exportFinalVersionFile(applicationId);

			// -----------------------------------------------------------------
			// 開発登録簿ルートフォルダ/[申請ID]/全ファイル
			// -----------------------------------------------------------------
			exportAllFile(applicationId);

			// -----------------------------------------------------------------
			// 開発登録簿ルートフォルダ/[申請ID]/開発登録簿
			// 調書アップロード時に生成
			// -----------------------------------------------------------------
			//exportDevelopmentRegisterFile(applicationId);

		} catch (Exception ex) {
			LOGGER.error("開発登録簿生成で例外発生", ex);
			throw new RuntimeException(ex);
		} finally {
			LOGGER.debug("開発登録簿生成 終了");
		}
	}

	
	/**
	 * 開発登録簿生成（最終版）
	 * 
	 * @param applicationId 申請ID
	 */
	private void exportFinalVersionFile(Integer applicationId) {
		LOGGER.debug("開発登録簿生成（最終版） 開始");
		try {
			Map<String, String> categoryNameMap = new HashMap<String, String>();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuuMMdd");
			ApplicationFileDao applicationFileDao = new ApplicationFileDao(emf);

			// M_開発登録簿取得
			DevelopmentDocumentMaster developmentDocumentMaster = developmentDocumentMasterRepository
					.getDevelopmentDocumentMaster(DEVELOPMENT_DOCUMENT_ID_1);

			// =================================================================
			// 最終版フォルダ作成
			// =================================================================
			/* ■出力フォルダ
			 * 　開発登録簿ルート
			 * 　└申請ID
			 * 　　├最終版
			 */
			LOGGER.trace("最終版フォルダ作成　開始");
			Path finalVersionPath = Paths.get(developmentRegisterRootPath, 
					applicationId.toString(), developmentDocumentMaster.getDocumentName());
			if (Files.exists(finalVersionPath)) {
				// フォルダ削除
				deleteDirectory(finalVersionPath.toFile());
			}
			// フォルダ作成
			Files.createDirectories(finalVersionPath);
			LOGGER.trace("最終版フォルダ作成　終了");

			// O_開発登録簿登録
			developmentDocumentJdbc.insert(applicationId, developmentDocumentMaster.getDevelopmentDocumentId(),
					finalVersionPath.toString());

			// =================================================================
			// 最終版ファイルコピー
			// =================================================================
			// O_申請ファイルリスト取得
			List<ApplicationFile> applicationFileList = applicationFileDao
					.getFinalVersionApplicationFile(applicationId);
			for (ApplicationFile applicationFile : applicationFileList) {
				/*
				 * ■出力フォルダ
				 * 　開発登録簿ルート
				 * 　└申請ID
				 * 　　├最終版
				 * 　　│└[申請ファイルID]_[カテゴリ名]_[版番号]_[アップロード日].[拡張子]
				 */
				// カテゴリ名取得
				String categoryName = categoryNameMap.get(applicationFile.getApplicationFileId());
				if (Objects.isNull(categoryName)) {
					categoryName = applicationFileMasterRepository
							.getCategoryName(applicationFile.getApplicationFileId());
					if (Objects.isNull(categoryName)) {
						categoryName = "カテゴリなし";
					}
					categoryNameMap.put(applicationFile.getApplicationFileId(), categoryName);
				}

				// アップロード日
				String upladDate = applicationFile.getUploadDatetime().format(formatter);

				// 入力ファイル
				Path srcPath = Paths.get(appFileRootpath + "/" + applicationFile.getFilePath());

				// 出力ファイル
				// [申請ファイルID]_[カテゴリ名]_[版番号]_[アップロード日].[拡張子]
				String dstFile = finalVersionPath.toString()
						+ "/" + applicationFile.getApplicationFileId() // 申請ファイルID
						+ "_" + categoryName // カテゴリ名
						+ "_" + getVersionInformationDispString(
								applicationFile.getVersionInformation()) // 版番号
						+ "_" + upladDate // アップロード日
						+ "." + applicationFile.getExtension(); // 拡張子
				Path dstPath = Paths.get(dstFile);

				LOGGER.trace("開発登録簿ファイルコピー　開始");
				Files.copy(srcPath, dstPath, StandardCopyOption.REPLACE_EXISTING);
				LOGGER.trace("開発登録簿ファイルコピー　終了");

			}

		} catch (Exception ex) {
			LOGGER.error("開発登録簿生成（最終版）で例外発生", ex);
			throw new RuntimeException(ex);
		} finally {
			LOGGER.debug("開発登録簿生成（最終版） 終了");
		}
	}

	/**
	 * 開発登録簿生成（全ファイル）
	 * 
	 * @param applicationId 申請ID
	 */
	private void exportAllFile(Integer applicationId) {
		LOGGER.debug("開発登録簿生成（全ファイル） 開始");
		try {
			Map<String, String> categoryNameMap = new HashMap<String, String>();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuuMMdd");

			// M_開発登録簿取得
			DevelopmentDocumentMaster developmentDocumentMaster = developmentDocumentMasterRepository
					.getDevelopmentDocumentMaster(DEVELOPMENT_DOCUMENT_ID_2);

			//=================================================================
			// 全ファイルフォルダ作成
			//=================================================================
			/* ■出力フォルダ
			 * 　開発登録簿ルート
			 * 　└申請ID
			 * 　　├全ファイル
			 */
			LOGGER.trace("全ファイルフォルダ作成　開始");
			Path allFilePath = Paths.get(developmentRegisterRootPath, 
					applicationId.toString(), developmentDocumentMaster.getDocumentName());
			if (Files.exists(allFilePath)) {
				// フォルダ削除
				deleteDirectory(allFilePath.toFile());
			}
			// フォルダ作成
			Files.createDirectories(allFilePath);
			LOGGER.trace("全ファイルフォルダ作成　終了");

			// O_開発登録簿登録
			developmentDocumentJdbc.insert(applicationId, developmentDocumentMaster.getDevelopmentDocumentId(),
					allFilePath.toString());

			// =================================================================
			// 全ファイルコピー
			// =================================================================
			// O_申請ファイルリスト取得
			List<ApplicationFile> applicationFileList = applicationFileRepository
					.getApplicationFilesByApplicationId(applicationId);
			for (ApplicationFile applicationFile : applicationFileList) {
				// =================================================================
				// カテゴリ名フォルダ作成
				// =================================================================
			/* ■出力フォルダ
				 * 　開発登録簿ルート
				 * 　└申請ID
				 * 　　├全ファイル
				 * 　　│├[カテゴリ名]
				 * 　　││　└[申請ファイルID]_[カテゴリ名]_[申請段階]_[版番号]_[アップロード日].[拡張子]
				 */
				// カテゴリ名取得
				String categoryName = categoryNameMap.get(applicationFile.getApplicationFileId());
				if (Objects.isNull(categoryName)) {
					categoryName = applicationFileMasterRepository
							.getCategoryName(applicationFile.getApplicationFileId());
					if (Objects.isNull(categoryName)) {
						categoryName = "カテゴリなし";
					}
					categoryNameMap.put(applicationFile.getApplicationFileId(), categoryName);
				}
				LOGGER.trace("カテゴリ名フォルダ作成　開始");
				Path categoryPath = Paths.get(allFilePath.toString(), categoryName);
				if (!Files.exists(categoryPath)) {
					Files.createDirectories(categoryPath);
				}
				LOGGER.trace("カテゴリ名フォルダ作成　終了");
				// =================================================================
				// カテゴリ名ファイルコピー
				// =================================================================
				// アップロード日
				String upladDate = applicationFile.getUploadDatetime().format(formatter);

				// 入力ファイル
				Path srcPath = Paths.get(appFileRootpath + "/" + applicationFile.getFilePath());

				// 出力ファイル
				// [申請ファイルID]_[カテゴリ名]_[申請段階]_[版番号]_[アップロード日].[拡張子]
				String dstFile = categoryPath.toString()
						+ "/" + applicationFile.getApplicationFileId() // 申請ファイルID
						+ "_" + categoryName // カテゴリ名
						+ "_" + getApplicationStepName(applicationFile.getApplicationStepId()) // 申請段階
						+ "_" + getVersionInformationDispString(
								applicationFile.getVersionInformation()) // 版番号
						+ "_" + upladDate // アップロード日
						+ "." + applicationFile.getExtension(); // 拡張子
				Path dstPath = Paths.get(dstFile);

				LOGGER.trace("開発登録簿ファイルコピー　開始");
				Files.copy(srcPath, dstPath, StandardCopyOption.REPLACE_EXISTING);
				LOGGER.trace("開発登録簿ファイルコピー　終了");

			}

			// =================================================================
			// 行政回答フォルダ作成
			// =================================================================
			/* ■出力フォルダ
			 * 　開発登録簿ルート
			 * 　└申請ID
			 * 　　├全ファイル
			 * 　　│└行政回答
			 */
			LOGGER.trace("行政回答フォルダ作成　開始");
			Path governmentAnswerPath = Paths.get(allFilePath.toString(), "行政回答");
			if (!Files.exists(governmentAnswerPath)) {
				Files.createDirectories(governmentAnswerPath);
			}
			LOGGER.trace("行政回答フォルダ作成　終了");
			//=================================================================
			// 行政回答ファイルコピー
			//=================================================================
			/* ■出力フォルダ
			 * 　開発登録簿ルート
			 * 　└申請ID
			 * 　　├全ファイル
			 * 　　│└行政回答
			 * 　　│　　└[回答ID]_[申請段階]_[アップロード日].[拡張子]
			 */
			// O_回答ファイルリスト取得
			List<AnswerFile> answerFileList = new ArrayList<AnswerFile>();
			answerFileList.addAll(answerFileRepository.findByApplicationStepIdUndeleted(applicationId, 1));
			answerFileList.addAll(answerFileRepository.findByApplicationStepIdUndeleted(applicationId, 2));
			answerFileList.addAll(answerFileRepository.findByApplicationStepIdUndeleted(applicationId, 3));
			for (AnswerFile answerFile : answerFileList) {

				String filePath = answerFile.getFilePath();
				List<String> uploadDateSplitAry = Arrays.asList(filePath.split("/"));
				// アップロード日
				String uploadDateTime = uploadDateSplitAry.get(uploadDateSplitAry.size() - 2);
				String uploadDate = uploadDateTime.substring(0, 8);
				// 拡張子
				String fileName = uploadDateSplitAry.get(uploadDateSplitAry.size() - 1);
				String extension = fileName.substring(fileName.lastIndexOf(".") + 1);

				// 入力ファイル
				Path srcPath = Paths.get(appFileRootpath + "/" + answerFile.getFilePath());

				// 出力ファイル
				// [回答ID]_[申請段階]_[アップロード日].[拡張子]
				String dstFile = governmentAnswerPath.toString() 
						+ "/" + answerFile.getAnswerId() // 回答ID
						+ "_" + getApplicationStepName(answerFile.getApplicationStepId()) // 申請段階
						+ "_" + uploadDate // アップロード日
						+ "." + extension; // 拡張子
				Path dstPath = Paths.get(dstFile);

				LOGGER.trace("行政回答ファイルコピー　開始");
				Files.copy(srcPath, dstPath, StandardCopyOption.REPLACE_EXISTING);
				LOGGER.trace("行政回答ファイルコピー　終了");

			}

		} catch (Exception ex) {
			LOGGER.error("開発登録簿生成（全ファイル）で例外発生", ex);
			throw new RuntimeException(ex);
		} finally {
			LOGGER.debug("開発登録簿生成（全ファイル） 終了");
		}
	}

	/**
	 * 開発登録簿生成（開発登録簿）
	 * 
	 * @param applicationId 申請ID
	 */
	public void exportDevelopmentRegisterFile(Integer applicationId) {
		LOGGER.debug("開発登録簿生成（開発登録簿） 開始");
		try {
			// M_開発登録簿取得
			DevelopmentDocumentMaster developmentDocumentMaster = 
					developmentDocumentMasterRepository.getDevelopmentDocumentMaster(DEVELOPMENT_DOCUMENT_ID_3);
			// -----------------------------------------------------------------
			// 開発登録簿出力ルートフォルダ存在確認
			// -----------------------------------------------------------------
			if (Objects.isNull(developmentRegisterRootPath)) {
				LOGGER.warn("開発登録簿出力フォルダパス: " + developmentRegisterRootPath);
				throw new RuntimeException("開発登録簿出力ルートフォルダ未設定");
			}
			// 開発登録簿出力ルートフォルダ存在確認
			Path outputRootPath = Paths.get(developmentRegisterRootPath);
			if (!Files.exists(outputRootPath)) {
				LOGGER.warn("開発登録簿出力ルートフォルダ: " + developmentRegisterRootPath);
				throw new RuntimeException("開発登録簿出力ルートフォルダ存在なし");
			}
			//=================================================================
			// 開発登録簿
			//=================================================================
			/* ■出力フォルダ
			 * 　開発登録簿ルート
			 * 　└申請ID
			 * 　　└開発登録簿
			 */
			// -----------------------------------------------------------------
			// 申請IDフォルダ作成
			// -----------------------------------------------------------------
			LOGGER.trace("申請IDフォルダ作成 開始");
			Path applicationIdPath = Paths.get(developmentRegisterRootPath, applicationId.toString());
			if (!Files.exists(applicationIdPath)) {
				Files.createDirectories(applicationIdPath);
			}
			LOGGER.trace("申請IDフォルダ作成 終了");
			// -----------------------------------------------------------------
			// 開発登録簿フォルダ作成
			// -----------------------------------------------------------------
			LOGGER.trace("開発登録簿フォルダ作成　開始");
			Path developmentRegisterPath = Paths.get(developmentRegisterRootPath, 
					applicationId.toString(), developmentDocumentMaster.getDocumentName());
			if (Files.exists(developmentRegisterPath)) {
				// フォルダ削除
				deleteDirectory(developmentRegisterPath.toFile());
			}
			// フォルダ作成
			Files.createDirectories(developmentRegisterPath);
			LOGGER.trace("開発登録簿フォルダ作成　終了");

			// -----------------------------------------------------------------
			// O_開発登録簿削除
			// -----------------------------------------------------------------
			developmentDocumentJdbc.delete(applicationId,developmentDocumentMaster.getDevelopmentDocumentId());
			
			// -----------------------------------------------------------------
			// O_開発登録簿登録
			// -----------------------------------------------------------------
			developmentDocumentJdbc.insert(applicationId, developmentDocumentMaster.getDevelopmentDocumentId(),
					developmentRegisterPath.toString());

			//=================================================================
			// 調書ファイルコピー
			//=================================================================
			/* ■出力フォルダ
			 * 　開発登録簿ルート
			 * 　└申請ID
			 * 　　└開発登録簿
			 * 　　　├【Ｆ③】調書
			 * ■調書ファイル取得手順
			 * ①調書対象帳票マスタID取得。
			 * 　(*)M_帳票.帳票種類=1（開発登録簿に含める）
			 * ②O_帳票から申請ID、調書対象帳票マスタIDに一致するファイルが調書ファイルとなる。
			 */
			// 調書（開発登録簿対象帳票マスタID取得）
			List<String> ledgerIdList = ledgerMasterRepository.getDevelopmentRegisterFileList();
			for (String ledgerId : ledgerIdList) {

				// O_帳票リスト取得
				List<Ledger> ledgerList = ledgerRepository.getLedgerListByApplicationIdLedgerId(applicationId,ledgerId);
				for (Ledger ledger : ledgerList) {

					// 入力ファイル
					Path srcPath = Paths.get(appFileRootpath + "/" + ledger.getFilePath());
					// 出力ファイル
					final String extension = ledger.getFilePath().substring(ledger.getFilePath().lastIndexOf("."));
					String dstFile = developmentRegisterPath.toString() + "/" + ledger.getFileName() + extension;
					Path dstPath = Paths.get(dstFile);

					LOGGER.trace("調書ファイルコピー　開始");
					Files.copy(srcPath, dstPath, StandardCopyOption.REPLACE_EXISTING);
					LOGGER.trace("調書ファイルコピー　終了");

				}
			}
			// =================================================================
			// 土地利用計画図ファイルコピー
			// =================================================================
			/* ■出力フォルダ
			 * 　開発登録簿ルート
			 * 　└申請ID
			 * 　　└開発登録簿
			 * 　　　└【Ｆ④】土地利用計画図
			 * ■土地利用計画図ファイル取得手順
			 * ①土地利用計画図対象申請ファイルID取得。
			 * 　(*)M_申請ファイル.申請ファイル種別=1（開発登録簿に含める）
			 * ②O_申請ファイルから申請ID、土地利用計画図対象申請ファイルIDに一致するファイルが土地利用計画図ファイルとなる。
			 */
			// 土地利用計画図（開発登録簿対象申請ファイルID取得）
			List<String> applicationFileIdList = applicationFileMasterRepository.getDevelopmentRegisterFileList();
			for (String applicationFileId : applicationFileIdList) {

				// O_申請ファイルリスト取得
				List<ApplicationFile> applicationFileList = applicationFileRepository
						.getApplicationFiles(applicationFileId, applicationId);
				for (ApplicationFile applicationFile : applicationFileList) {
					// カテゴリ名取得
					String categoryName = applicationFileMasterRepository
							.getCategoryName(applicationFile.getApplicationFileId());
					// 入力ファイル
					Path srcPath = Paths.get(appFileRootpath + "/" + applicationFile.getFilePath());
					// 出力ファイル
					String dstFile = developmentRegisterPath.toString() + "/" + categoryName + "." + applicationFile.getExtension();
					Path dstPath = Paths.get(dstFile);

					LOGGER.trace("土地利用計画図ファイルコピー　開始");
					Files.copy(srcPath, dstPath, StandardCopyOption.REPLACE_EXISTING);
					LOGGER.trace("土地利用計画図ファイルコピー　終了");

				}
			}
		} catch (Exception ex) {
			LOGGER.error("開発登録簿生成（開発登録簿）で例外発生", ex);
			throw new RuntimeException(ex);
		} finally {
			LOGGER.debug("開発登録簿生成（開発登録簿） 終了");
		}
	}

	/**
	 * 申請段階名取得
	 * 
	 * @param applicationStepId
	 * @return 申請段階名
	 */
	private String getApplicationStepName(Integer applicationStepId) {
		if (APPLICATION_STEP_ID_1.equals(applicationStepId)) {
			return APPLICATION_STEP_ID_1_NAME;
		} else if (APPLICATION_STEP_ID_2.equals(applicationStepId)) {
			return APPLICATION_STEP_ID_2_NAME;
		} else if (APPLICATION_STEP_ID_3.equals(applicationStepId)) {
			return APPLICATION_STEP_ID_3_NAME;
		}
		return "";
	}

	/**
	 * 版番号表示文字列取得
	 * 
	 * @param versionInformation
	 * @return 版番号表示文字列
	 */
	private String getVersionInformationDispString(Integer versionInformation) {
		return versionInformation + "版";
	}

	/**
	 * 開発登録簿ZIPファイル作成
	 * 
	 * @param applicationId 申請ID
	 * @param developmentDocumentId 開発登録簿マスタID
	 * @return　開発登録簿ZIPファイルパス
	 */
	public String createDevelopmentRegisterZipFile(Integer applicationId,Integer developmentDocumentId) {
		
		LOGGER.debug("開発登録簿ZIPファイル作成 開始");
		    	
		try {
			
			// 申請IDのステータスが許可判定完了の場合のみ処理を実施
			// 申請ID,開発登録簿マスタIDでO_開発登録簿からファイルパスを取得し、以下のファイルをzip化してダウンロードする
			// ファイルパスは開発登録簿のルートディレクトリ以下が入る想定
			
			// O_申請情報取得
			Application application = getApplication(applicationId);
			
			// 申請.ステータスが許可判定完了になっていること
			if(!STATUS_PERMISSION_COMPLETED.equals(application.getStatus())) {
				LOGGER.warn("許可判定完了ていない");
				return null;
			}
			
			// M_開発登録簿取得
			DevelopmentDocumentMaster developmentDocumentMaster = 
					developmentDocumentMasterRepository.getDevelopmentDocumentMaster(developmentDocumentId);
			
			// 環境確認
			if (Objects.isNull(developmentRegisterRootPath)) {
				LOGGER.warn("開発登録簿出力フォルダパス: " + developmentRegisterRootPath);
				throw new RuntimeException("開発登録簿出力ルートフォルダ未設定");				
			}
			// 開発登録簿出力ルートフォルダ存在確認
			Path outputRootPath = Paths.get(developmentRegisterRootPath);
			if (!Files.exists(outputRootPath)) {
				LOGGER.warn("開発登録簿出力ルートフォルダ: " + developmentRegisterRootPath);
				throw new RuntimeException("開発登録簿出力ルートフォルダ存在なし");
			}
			// 開発登録簿ルートフォルダ/[申請ID]フォルダ存在確認
			Path applicationIdPath = Paths.get(developmentRegisterRootPath,applicationId.toString());
			if (!Files.exists(applicationIdPath)) {
				LOGGER.warn("開発登録簿申請フォルダ存在なし");
				throw new RuntimeException("開発登録簿申請フォルダ存在なし");
			}
			// 開発登録簿別フォルダ
			Path developmentDocumentPath = Paths.get(applicationIdPath.toString(),developmentDocumentMaster.getDocumentName());
			if (!Files.exists(developmentDocumentPath)) {
				LOGGER.warn("開発登録簿フォルダ存在なし");
				throw new RuntimeException("開発登録簿フォルダ存在なし");
			}
			// tempフォルダ作成			
			Path tempPath = Paths.get(developmentRegisterRootPath,"temp");
			if (!Files.exists(tempPath)) {
				Files.createDirectories(tempPath);
			}
			// 日時
			final String dateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss-SSS")
					.format(LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()));

			// ZIPファイル作成処理
			String zipFileName = tempPath + "\\" +applicationId+"_"+developmentDocumentMaster.getDocumentName() +"_"+dateTime+".zip";
			ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFileName)));
			
			// 圧縮元ファイル
			File srcDir = new File(developmentDocumentPath.toString());
			
			// 圧縮
			File[] featurelist = srcDir.listFiles();
			for(File feature : featurelist)createZipFile(zos, "", feature);
			zos.close();
			
			// ZIPファイル名
			return zipFileName;
			
		}catch(Exception e) {
			throw new RuntimeException();
		} finally {
			LOGGER.debug("開発登録簿ZIPファイル作成 終了");
		}
	}

	/**
	 * ZIPファイル作成
	 * @param zipoutputstream, 親ディレクトリ名称, 対象ファイル
	 * @return -
	 */
    private void createZipFile(ZipOutputStream zos, String parentDirName, File file) throws IOException {
    	byte[] buf = new byte[1024];
    	int len;
		if(parentDirName == null) {
			parentDirName = "";
		}
    	try {
	    	if(file.isDirectory()) {
	    		parentDirName = parentDirName + file.getName() + "/";
	    		zos.putNextEntry(new ZipEntry(parentDirName));
	    		File[] childFilelist = file.listFiles();
	    		for(File childFile : childFilelist) {
	    			createZipFile(zos, parentDirName, childFile);
	    		}
	    	}else if(file.isFile()) {
	    		zos.putNextEntry(new ZipEntry(parentDirName + file.getName()));
	    		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
	    		while((len = bis.read(buf, 0, buf.length)) != -1) {
	    			zos.write(buf, 0, len);
	    		}
	    		bis.close();
	    	}
    	}finally {
    		zos.closeEntry();
    	}
    }
	
	/**
	 * 最終提出書類一覧取得
	 * 
	 * @param applicationId 申請ID
	 * @return 最終提出書類一覧
	 */
	public List<DevelopmentDocumentFileForm> getDevelopmentDocumentFileFormList(Integer applicationId) {
		LOGGER.debug("最終提出書類一覧取得 開始");
		try {
			// 開発登録簿ファイルフォームList
			List<DevelopmentDocumentFileForm> developmentDocumentFileFormList = new ArrayList<DevelopmentDocumentFileForm>();

			// M_開発登録簿一覧取得
			List<DevelopmentDocumentMaster> developmentDocumentMasterList = developmentDocumentMasterRepository
					.getDevelopmentDocumentMaster();
			// O_開発登録簿（最終提出書類）一覧検索
			List<DevelopmentDocument> developmentDocumentList = developmentDocumentRepository
					.getDevelopmentDocumentByApplicationId(applicationId);
			// 書類名MAP作成
			Map<Integer, String> documentNameMap = new HashMap<Integer, String>();
			for (DevelopmentDocumentMaster developmentDocumentMaster : developmentDocumentMasterList) {
				documentNameMap.put(developmentDocumentMaster.getDevelopmentDocumentId(),
						developmentDocumentMaster.getDocumentName());
			}
			// 開発登録簿ファイルフォームList作成
			for (DevelopmentDocument developmentDocument : developmentDocumentList) {
				// O_開発登録簿⇒開発登録簿ファイルフォーム 変換
				developmentDocumentFileFormList
						.add(getDevelopmentDocumentFileFormEntity(developmentDocument, documentNameMap));
			}
			return developmentDocumentFileFormList;

		} catch (Exception ex) {
			LOGGER.error("最終提出書類一覧取得 失敗");
			throw new RuntimeException(ex);
		} finally {
			LOGGER.debug("最終提出書類一覧取得 終了");
		}
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
	 * ディレクトリ削除
	 * 
	 * @param directory
	 */
	private void deleteDirectory(File directory) {
		LOGGER.debug("ディレクトリ削除 開始");
		try {
			if (directory.isDirectory()) {
				// ディレクトリ内のすべてのファイルとサブディレクトリを取得
				File[] files = directory.listFiles();
				if (files != null) {
					for (File file : files) {
						// 再帰的にファイルとサブディレクトリを削除
						deleteDirectory(file);
					}
				}
			}
			// ディレクトリまたはファイルを削除
			directory.delete();
		} catch (Exception ex) {
			LOGGER.error("ディレクトリ削除 失敗", ex);
			throw new RuntimeException(ex);
		} finally {
			LOGGER.debug("ディレクトリ削除 終了");
		}
	}
}
