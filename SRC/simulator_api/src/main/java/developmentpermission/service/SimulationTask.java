package developmentpermission.service;
import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.ObjectMapper;

import developmentpermission.form.GeneralConditionDiagnosisSimulationExecution;
import developmentpermission.form.ResponseEntityForm;

public class SimulationTask implements Callable<ResponseEntityForm> {
	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(SimulationTask.class);
    private String chromeDriverPath;
    private String chromeDriverLogPath;
    private Integer simulationTaskMaxTimeOut;
    private String judgementDownloadFolder;
    private String plateauUrl;
    private GeneralConditionDiagnosisSimulationExecution generalConditionDiagnosisSimulationExecution;
    
    /**
     * コンストラクタ
     * @param chromeDriverPath　ChromeのDriverファイルのパス
     * @param chromeDriverLogPath ChromeのDriverログのパス
     * @param simulationTaskMaxTimeOut シミュレート実行最大待ち時間
     * @param judgementDownloadFolder 概況診断レポートのダウンロードフォルダ
     * @param plateauUrl　PLATEAU VIEWのURL
     * @param GeneralConditionDiagnosisSimulationExecution　概況診断結果DTO
     */
    public SimulationTask(String chromeDriverPath,String chromeDriverLogPath,Integer simulationTaskMaxTimeOut,String judgementDownloadFolder,String plateauUrl,GeneralConditionDiagnosisSimulationExecution generalConditionDiagnosisSimulationExecution) {
        this.chromeDriverPath = chromeDriverPath;
        this.chromeDriverLogPath = chromeDriverLogPath;
        this.simulationTaskMaxTimeOut = simulationTaskMaxTimeOut;
        this.judgementDownloadFolder = judgementDownloadFolder;
        this.plateauUrl = plateauUrl;
    	this.generalConditionDiagnosisSimulationExecution = generalConditionDiagnosisSimulationExecution;
    }
    
    @Override
    public ResponseEntityForm call() throws Exception {
        // ChromeDriverのパスを設定
        System.setProperty("webdriver.chrome.driver", chromeDriverPath);

        // ダウンロードディレクトリのパスを設定
        String downloadDirectory = judgementDownloadFolder + generalConditionDiagnosisSimulationExecution.getFolderName();
        File folder = new File(downloadDirectory);
        if (!folder.exists()) {
            folder.mkdir();
        }
        ChromeDriverService service = new ChromeDriverService.Builder()
                .usingDriverExecutable(new File(chromeDriverPath))
                .usingAnyFreePort()
                .withVerbose(false)
                .withLogFile(new File(chromeDriverLogPath))
                .build();

        // WebDriverのオプションを設定
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-gpu");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-infobars");
        options.addArguments("--disable-extensions");
        options.addArguments("--headless");
        options.addArguments("--disable-popup-blocking");
        options.addArguments("--remote-allow-origins=*");
        HashMap<String, Object> prefs = new HashMap<>();
        prefs.put("download.default_directory", downloadDirectory);
        options.setExperimentalOption("prefs", prefs);

        // WebDriverのインスタンスを作成
        WebDriver driver = new ChromeDriver(service,options);
        // 幅: 1920px, 高さ: 1080px
        Dimension dimension = new Dimension(1920, 1080);
        driver.manage().window().setSize(dimension);
        // ページ読み込みのタイムアウト
        driver.manage().timeouts().pageLoadTimeout(Duration.ofMinutes(5));
        driver.manage().timeouts().setScriptTimeout(Duration.ofMinutes(5));
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String applicationPlace = objectMapper.writeValueAsString(generalConditionDiagnosisSimulationExecution.getLotNumbers());
            String applicationCategory = objectMapper.writeValueAsString(generalConditionDiagnosisSimulationExecution.getApplicationCategories());
            String generalConditionDiagnosisResults = objectMapper.writeValueAsString(generalConditionDiagnosisSimulationExecution.getGeneralConditionDiagnosisResults());
            String tempFolderName = objectMapper.writeValueAsString(generalConditionDiagnosisSimulationExecution.getFolderName());
            String tempFileName = objectMapper.writeValueAsString(generalConditionDiagnosisSimulationExecution.getFileName());
            Integer applicationId = generalConditionDiagnosisSimulationExecution.getApplicationId();
            String hash = "#applicationPlace=" + URLEncoder.encode(applicationPlace,StandardCharsets.UTF_8)
									+ "&applicationCategory=" + URLEncoder.encode(applicationCategory,StandardCharsets.UTF_8) 
												+ "&generalConditionDiagnosisResults=" + URLEncoder.encode(generalConditionDiagnosisResults,StandardCharsets.UTF_8)
															+ "&folderName=" + URLEncoder.encode(tempFolderName,StandardCharsets.UTF_8)
																	+ "&fileName=" + URLEncoder.encode(tempFileName,StandardCharsets.UTF_8);
            if(applicationId != null && applicationId.intValue() > 0) {
            	hash += "&applicationId=" + applicationId.intValue();
            }
            // ページにアクセス
            driver.get(plateauUrl+hash);
            
            LOGGER.info("window.location.hash:"+hash);
            
            // 待機条件
            // 最大xxx秒待機,10秒毎にポーリング
            Wait<WebDriver> wait = new FluentWait<>(driver)
                    .withTimeout(Duration.ofSeconds(simulationTaskMaxTimeOut))
                    .pollingEvery(Duration.ofSeconds(10))
                    .ignoring(Exception.class);
            // 帳票生成が完了されることを待機
            // 完了後に生成される要素が出現するまで待機
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("completionIndicator")));
            
            return new ResponseEntityForm(HttpStatus.OK.value(), "Successful simulation execution.");
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            // ブラウザを閉じる
            driver.quit();
            service.stop();
        }
    }
}