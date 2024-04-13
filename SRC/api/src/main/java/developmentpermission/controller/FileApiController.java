package developmentpermission.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import developmentpermission.form.ResponseEntityForm;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * ファイルAPIコントローラ
 */
@Api(tags = "ファイル")
@RestController
@RequestMapping("/file")
public class FileApiController extends AbstractApiController {
	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(FileApiController.class);

	@Value("${app.file.service.rootpath}")
	private String serviceFileBasePath;
	
	@Value("${app.file.answer.folder}")
	private String answerFolder;
	
	@Value("${app.file.rootpath}")
	private String applicationFolder;
	
	@Value("${app.file.manual.folder}")
	private String manualFolder;
	
	@Value("${app.file.manual.business.file}")
	private String BusinessManual;
	
	@Value("${app.file.manual.goverment.file}")
	private String GovermentManual;
	
	/**
	 * ファイルダウンロード
	 * 
	 * @return 申請者情報入力項目一覧
	 */
	@RequestMapping(value = "/view/**", method = RequestMethod.GET)
	@ApiOperation(value = "ファイル取得", notes = "ファイルを取得する.")
	@ResponseBody
	@ApiResponses(value = { @ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class),
			@ApiResponse(code = 404, message = "ファイルが存在しない場合", response = ResponseEntityForm.class),
			@ApiResponse(code = 500, message = "ファイルの取得に失敗した場合", response = ResponseEntityForm.class), })
	public HttpEntity<byte[]> getApplicantItems(HttpServletRequest request) {
		LOGGER.info("ファイル取得 開始");
		try {
			// ファイルパスを取得
			String uri = request.getRequestURI();
			final String[] targetDir = {"file", "view"};
			String fileName = extractAccessFilePath(uri, targetDir);
			LOGGER.info("取得するファイル名:" + fileName);
			// 絶対ファイルパス
			String absoluteFilePath = serviceFileBasePath + "/" + fileName;
			Path filePath = Paths.get(absoluteFilePath);
			
			if (!Files.exists(filePath)) {
				// ファイルが存在しない
				LOGGER.warn("ファイルが存在しない");
				throw new ResponseStatusException(HttpStatus.NOT_FOUND);
			}
			// リソースファイルを読み込み
			File file = new File(absoluteFilePath);
			InputStream is = new FileInputStream(file);
			// byteへ変換
			byte[] data = IOUtils.toByteArray(is);

			String mimeType = URLConnection.guessContentTypeFromStream(is);
			if (mimeType == null) {
				int point = fileName.lastIndexOf(".");
				String sp = fileName.substring(point + 1);
				if ("pdf".equals(fileName.substring(point + 1)) || "PDF".equals(fileName.substring(point + 1))) {
					mimeType = "application/pdf";
				} else if ("jpg".equals(fileName.substring(point + 1)) || "jpeg".equals(fileName.substring(point + 1))
						|| "JPG".equals(fileName.substring(point + 1))
						|| "JPEG".equals(fileName.substring(point + 1))) {
					mimeType = "image/jpeg";
				} else if ("png".equals(fileName.substring(point + 1))
						|| "PNG".equals(fileName.substring(point + 1))) {
					mimeType = "image/png";
				} else if ("tif".equals(fileName.substring(point + 1)) || "tiff".equals(fileName.substring(point + 1))
						|| "TIF".equals(fileName.substring(point + 1))
						|| "TIFF".equals(fileName.substring(point + 1))) {
					mimeType = "image/tiff";
				} else {
					mimeType = "application/octet-stream";
				}
			}
			HttpHeaders headers = new HttpHeaders();
			headers.add("Content-Type", mimeType);
			headers.setContentLength(data.length);
			LOGGER.info("ファイル取得 終了");
			return new HttpEntity<byte[]>(data, headers);
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	/**
	 * 回答ファイル参照
	 * 
	 * @return 申請者情報入力項目一覧
	 */
	@RequestMapping(value = "/viewapp/**", method = RequestMethod.GET)
	@ApiOperation(value = "回答ファイル取得", notes = "回答ファイルを取得する.")
	@ResponseBody
	@ApiResponses(value = { @ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class),
			@ApiResponse(code = 404, message = "回答ファイルが存在しない場合", response = ResponseEntityForm.class),
			@ApiResponse(code = 500, message = "回答ファイルの取得に失敗した場合", response = ResponseEntityForm.class), })
	public HttpEntity<byte[]> getApplicantAnserFile(HttpServletRequest request) {
		LOGGER.info("回答ファイル取得 開始");
		try {
			// ファイルパスを取得
			String uri = request.getRequestURI();
			final String[] targetDir = {"file", "viewapp"};
			String fileName = extractAccessFilePath(uri, targetDir);
			LOGGER.info("取得するファイル名:" + URLDecoder.decode(fileName, "UTF-8"));
			// 絶対ファイルパス
			String absoluteFilePath = applicationFolder + "/" + URLDecoder.decode(fileName, "UTF-8");
			Path filePath = Paths.get(absoluteFilePath);
			LOGGER.info("取得するパス名:" + absoluteFilePath);
			if (!Files.exists(filePath)) {
				// ファイルが存在しない
				LOGGER.warn("ファイルが存在しない");
				throw new ResponseStatusException(HttpStatus.NOT_FOUND);
			}
			// リソースファイルを読み込み
			File file = new File(absoluteFilePath);
			InputStream is = new FileInputStream(file);
			// byteへ変換
			byte[] data = IOUtils.toByteArray(is);

			String mimeType = URLConnection.guessContentTypeFromStream(is);
			if (mimeType == null) {
				int point = fileName.lastIndexOf(".");
				String sp = fileName.substring(point + 1);
				if ("pdf".equals(fileName.substring(point + 1)) || "PDF".equals(fileName.substring(point + 1))) {
					mimeType = "application/pdf";
				} else if ("jpg".equals(fileName.substring(point + 1)) || "jpeg".equals(fileName.substring(point + 1))
						|| "JPG".equals(fileName.substring(point + 1))
						|| "JPEG".equals(fileName.substring(point + 1))) {
					mimeType = "image/jpeg";
				} else if ("png".equals(fileName.substring(point + 1))
						|| "PNG".equals(fileName.substring(point + 1))) {
					mimeType = "image/png";
				} else if ("tif".equals(fileName.substring(point + 1)) || "tiff".equals(fileName.substring(point + 1))
						|| "TIF".equals(fileName.substring(point + 1))
						|| "TIFF".equals(fileName.substring(point + 1))) {
					mimeType = "image/tiff";
				} else {
					mimeType = "application/octet-stream";
				}
			}
			HttpHeaders headers = new HttpHeaders();
			headers.add("Content-Type", mimeType);
			headers.setContentLength(data.length);
			LOGGER.info("回答ファイル取得 終了");
			return new HttpEntity<byte[]>(data, headers);
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	/**
	 * ファイル変換　PDF→PNG
	 * 
	 * @return 申請者情報入力項目一覧
	 */
	@RequestMapping(value = "/convert/**", method = RequestMethod.GET)
	@ApiOperation(value = "ファイル変換", notes = "ファイルを変換する.")
	@ResponseBody
	@ApiResponses(value = { @ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class),
			@ApiResponse(code = 404, message = "ファイルが存在しない場合", response = ResponseEntityForm.class),
			@ApiResponse(code = 500, message = "ファイルの取得に失敗した場合", response = ResponseEntityForm.class), })
	public HttpEntity<byte[]> convertPdfFile(
			HttpServletRequest request, 
			@RequestParam(name = "page", required = false, defaultValue = "1") int page,
			@RequestParam(name = "version", required = false) String version
	) {
		LOGGER.info("ファイル変換 開始");
		try {
			// ファイルパス等を取得
			String uri = request.getRequestURI();
			LOGGER.info(uri);
			final String[] targetDir = {"file", "convert"};
			String filePath = extractAccessFilePath(uri, targetDir);
			filePath = URLDecoder.decode(filePath, "UTF-8");
			LOGGER.info("変換するファイル名:" + filePath);
			String dirPath = filePath.replaceAll("/[^/]+\\.[^/]+$", "");
			String absoluteFilePath = applicationFolder + "/" + filePath;

			File file = new File(absoluteFilePath);
			String fileName = file.getName();
			String fileParentPath = file.getParent();
			
			if (!Files.exists(Paths.get(absoluteFilePath))) {
				// ファイルが存在しない
				LOGGER.warn("ファイルが存在しない");
				throw new ResponseStatusException(HttpStatus.NOT_FOUND);
			}
			
			// PDFファイルの読み込み
			PDDocument document = PDDocument.load(new File(absoluteFilePath));
			PDFRenderer renderer = new PDFRenderer(document);
			
	        // 変換ファイルディレクトリ作成
			String imageDirectoryPath = fileParentPath + "/images";
			File directory = new File(imageDirectoryPath);
	        if (!directory.exists()) {
	            directory.mkdirs();
	        }
	        
	        // 変換処理
	        List<String> imageList = new ArrayList<>();
	        int processArrayNo = page - 1;
	        String convertFileName = "";
	        if(version != null) {
		        convertFileName = "/images/" + fileName.substring(0, fileName.length() - 4) + "_" + version + "_" + page + ".png";
	        } else {
	        	convertFileName = "/images/" + fileName.substring(0, fileName.length() - 4) + "_" + page + ".png";
	        }
	        BufferedImage bufferedImage = renderer.renderImage(processArrayNo, 1.5f);
	        ImageIO.write(bufferedImage, "png", new File(fileParentPath + convertFileName));
	        imageList.add(dirPath + convertFileName);
	        
			// リソースファイルを読み込み
			File convertFile = new File(fileParentPath + convertFileName);
			InputStream is = new FileInputStream(convertFile);
			// byteへ変換
			byte[] data = IOUtils.toByteArray(is);

			String mimeType = URLConnection.guessContentTypeFromStream(is);
			if (mimeType == null) {
				mimeType = "image/png";
			}
			HttpHeaders headers = new HttpHeaders();
			headers.add("Content-Type", mimeType);
			headers.setContentLength(data.length);
			String encodedFileName = "";
	        if(version != null) {
	        	encodedFileName = URLEncoder.encode(fileName.substring(0, fileName.length() - 4) + "_" + version +"_" + page + ".png", "UTF-8");
	        } else {
	        	encodedFileName = URLEncoder.encode(fileName.substring(0, fileName.length() - 4) + "_" + page + ".png", "UTF-8");
	        }
		    headers.add("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"");
		    headers.add("X-File-Path", dirPath + URLDecoder.decode(convertFileName, "UTF-8"));
			document.close();
			return new HttpEntity<byte[]>(data, headers);
	        
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	/** マニュアルファイル参照　**/
	@RequestMapping(value = "/view/manual/**", method = RequestMethod.GET)
	@ApiOperation(value = "マニュアルファイル取得", notes = "マニュアルファイルを取得する.")
	@ResponseBody
	@ApiResponses(value = {  
			@ApiResponse(code = 400, message = "マニュアルファイルの格納場所が指定しない場合", response = ResponseEntityForm.class),
			@ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class),
			@ApiResponse(code = 404, message = "マニュアルファイルが存在しない場合", response = ResponseEntityForm.class),
			@ApiResponse(code = 500, message = "マニュアルファイルの取得に失敗した場合", response = ResponseEntityForm.class), })
	public HttpEntity<byte[]> getApplicantManualFile(HttpServletRequest request) {
		LOGGER.info("マニュアルファイル取得 開始");
		try {
			// ファイルパスを取得
			String uri = request.getRequestURI();
			final String[] targetDir = {"file", "view", "manual"};
			final String manualType = extractAccessFilePath(uri, targetDir);
			String absoluteFilePath = "";
			if(manualType.equals("government")) {
				absoluteFilePath = applicationFolder + manualFolder + "/" + GovermentManual;
				LOGGER.info("取得するパス:" + absoluteFilePath);
			}
			if(manualType.equals("business")) {
				absoluteFilePath = applicationFolder + manualFolder + "/" + BusinessManual;
			}
			
			if(absoluteFilePath.equals("")){
			    LOGGER.warn("有効なパスが指定されていません");
			    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "有効なパスが指定されていません");
			}
			
			if(!absoluteFilePath.equals("")){
				Path filePath = Paths.get(absoluteFilePath);
				LOGGER.info("取得するパス名:" + absoluteFilePath);
				if (!Files.exists(filePath)) {
					// ファイルが存在しない
					LOGGER.warn("ファイルが存在しない");
					throw new ResponseStatusException(HttpStatus.NOT_FOUND);
				}
				// リソースファイルを読み込み
				File file = new File(absoluteFilePath);
				InputStream is = new FileInputStream(file);
				// byteへ変換
				byte[] data = IOUtils.toByteArray(is);
	
				String mimeType = URLConnection.guessContentTypeFromStream(is);
				if (mimeType == null) {
					mimeType = "application/pdf";
				}
				HttpHeaders headers = new HttpHeaders();
				headers.add("Content-Type", mimeType);
				headers.setContentLength(data.length);
				LOGGER.info("回答ファイル取得 終了");
				return new HttpEntity<byte[]>(data, headers);
			}
		    LOGGER.warn("予期しないエラーが発生しました");
		    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "リクエストされたマニュアルが見つかりません");
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	/**
	 * アクセスするファイルパスを抽出する
	 * @param srcPath リクエストされたファイルパス
	 * @param targetDir 抽出ターゲットディレクトリ
	 * @return targetDir以降のファイルパス
	 */
	private String extractAccessFilePath(String srcPath, String[] targetDir) {
		try {
			final String[] srcDirs = srcPath.split("/");
			// ディレクトリ名抽出開始インデックス
			int extractDirIndex = 0;
			for (int i = 0; i < srcDirs.length; i++) {
				if (srcDirs[i].equals(targetDir[0])) {
					extractDirIndex = i;
					break;
				}
			}
			boolean pathCheckResult = true;
			for (int i = 0; i < targetDir.length; i++) {
				if (!srcDirs[extractDirIndex + i].equals(targetDir[i])) {
					// リクエストパスが想定パスと一致しているかチェック
					pathCheckResult = false;
					break;
				}
			}
			if (pathCheckResult) {
				final String[] resDirs = Arrays.copyOfRange(srcDirs, extractDirIndex + targetDir.length, srcDirs.length);
				return String.join("/", resDirs);
			} else {
				// パス不正
				return null;
			}
			
		} catch (Exception e) {
			LOGGER.error("アクセス先ディレクトリの抽出に失敗");
			return null;
		}
		
	}
}
