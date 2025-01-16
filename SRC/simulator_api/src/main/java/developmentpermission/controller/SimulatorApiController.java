package developmentpermission.controller;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import developmentpermission.form.GeneralConditionDiagnosisSimulationExecution;
import developmentpermission.form.ResponseEntityForm;
import developmentpermission.service.SimulationTask;
import developmentpermission.util.AuthUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * シミュレート実行APIコントローラ
 */
@Api(tags = "シミュレート実行")
@RestController
@RequestMapping("/simulator")
public class SimulatorApiController extends AbstractApiController {

	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(SimulatorApiController.class);
	@Value("${webdriver.chrome.driver}")
	private String chromeDriverPath;
	@Value("${webdriver.chrome.driver.log}")
	private String chromeDriverLogPath;
	@Value("${app.simulation.task.timeout.max.seconds}")
	private Integer simulationTaskMaxTimeOut;
	@Value("${app.file.judgement.download.folder}")
	private String judgementDownloadFolder;
	@Value("${app.plateau.url}")
	private String plateauUrl;
	@Value("${app.simulation.task.limit}")
	private Integer simulationTaskLimit;
	private ExecutorService executorService = Executors.newFixedThreadPool(10);
	
	@PostConstruct
    private void initializeExecutorService() {
        executorService = Executors.newFixedThreadPool(simulationTaskLimit);
    }

	/**
	 * 概況診断シミュレート実行
	 * 
	 * @param GeneralConditionDiagnosisSimulationExecution 概況診断シミュレート実行DTO
	 * @return ResponseEntityForm 処理結果
	 */
	@RequestMapping(value = "/execution", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "概況診断シミュレート実行", notes = "概況診断のシミュレート実行を行う.")
	@ResponseBody
	@ApiResponses(value = { @ApiResponse(code = 400, message = "パラメータ不正", response = ResponseEntityForm.class),
			@ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class),
			@ApiResponse(code = 403, message = "ロール不適合", response = ResponseEntityForm.class) })
	public ResponseEntityForm simulateExecution(
			@ApiParam(required = true, value = "概況診断シミュレート実行DTO")@RequestBody GeneralConditionDiagnosisSimulationExecution generalConditionDiagnosisSimulationExecution,
			@CookieValue(value = "token", required = false) String token) {
		LOGGER.info("概況診断シミュレート実行 開始");
		try {
			String role = AuthUtil.getRole(token);
			if (AuthUtil.ROLE_BUSINESS.equals(role) || AuthUtil.ROLE_GOVERMENT.equals(role)) {
				if (generalConditionDiagnosisSimulationExecution != null && generalConditionDiagnosisSimulationExecution.getFolderName() != null &&
						generalConditionDiagnosisSimulationExecution.getGeneralConditionDiagnosisResults() != null && generalConditionDiagnosisSimulationExecution.getLotNumbers() != null) {
					SimulationTask task = new SimulationTask(chromeDriverPath,chromeDriverLogPath,simulationTaskMaxTimeOut,judgementDownloadFolder,plateauUrl,generalConditionDiagnosisSimulationExecution);
			        // タスクを非同期で実行
					executorService.submit(task);
			        // 非同期実行なので、すぐに応答を返す
			        return new ResponseEntityForm(HttpStatus.ACCEPTED.value(), "Simulation task started.");
				} else {
					// パラメータ不正
					LOGGER.warn("パラメータ不正");
					throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
				}
			} else {
				LOGGER.warn("ロール不適合: " + role);
				throw new ResponseStatusException(HttpStatus.FORBIDDEN);
			}
		} catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		} finally {
			LOGGER.info("概況診断シミュレート実行 終了");
		}
	}
}
