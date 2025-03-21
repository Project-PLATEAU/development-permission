package developmentpermission.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import developmentpermission.entity.Calendar;
import developmentpermission.form.AnswerConfirmLoginForm;
import developmentpermission.form.ApplicantInformationItemForm;
import developmentpermission.form.ApplicationCategorySelectionViewForm;
import developmentpermission.form.ApplicationFileForm;
import developmentpermission.form.ApplicationInformationSearchResultHeaderForm;
import developmentpermission.form.ApplicationRegisterForm;
import developmentpermission.form.ApplicationRegisterResultForm;
import developmentpermission.form.ApplicationSearchConditionForm;
import developmentpermission.form.ApplicationSearchResultForm;
import developmentpermission.form.ApplicationStepForm;
import developmentpermission.form.ApplicationTypeForm;
import developmentpermission.form.ApplyAnswerForm;
import developmentpermission.form.ChatSearchResultForm;
import developmentpermission.form.DepartmentForm;
import developmentpermission.form.GeneralConditionDiagnosisResultForm;
import developmentpermission.form.ItemAnswerStatusForm;
import developmentpermission.form.OutputDataForm;
import developmentpermission.form.ReApplicationForm;
import developmentpermission.form.ReApplicationRequestForm;
import developmentpermission.form.ResponseEntityForm;
import developmentpermission.form.StatusForm;
import developmentpermission.form.AnswerStatusForm;
import developmentpermission.form.AnswerNameForm;
import developmentpermission.form.UploadApplicationFileForm;
import developmentpermission.service.AnswerService;
import developmentpermission.service.ApplicationService;
import developmentpermission.service.CategoryService;
import developmentpermission.service.ChatService;
import developmentpermission.service.CsvExportService;
import developmentpermission.util.AuthUtil;
import developmentpermission.util.CalendarUtil;
import developmentpermission.util.LogUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.web.util.UriUtils;
import java.nio.charset.StandardCharsets;

/**
 * リマインドAPIコントローラ
 */
@Api(tags = "リマインド")
@RestController
@RequestMapping("/reminder")
public class ReminderApiController extends AbstractApiController {

	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(ReminderApiController.class);
	
	/** 回答Serviceインスタンス */
	@Autowired
	private AnswerService answerService;
	
	/** 問合せServiceインスタンス */
	@Autowired
	private ChatService chatService;
	
	/** カレンダーユーティリティインスタンス */
	@Autowired
	private CalendarUtil calendarUtil;
	
	/** リマインド通知の有効対象 */
	@Value("${app.reminder.mail.enabledTypes}")
	private String reminderMailEnabledTypes;
	
	/** APIシークレットkey */
	@Value("${app.api.secretkey}")
	private String apiSecretkey;
	
	/**
	 * 指定日のカレンダー情報を取得
	 * 
	 */
	@RequestMapping(value = "/calendar", method = RequestMethod.GET)
	@ApiOperation(value = "指定日のカレンダー情報を取得", notes = "指定日のカレンダー情報を取得する.")
	@ResponseBody
	@ApiResponses(value = { @ApiResponse(code = 200, message = "成功", response = ResponseEntityForm.class),
			@ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class),
			@ApiResponse(code = 500, message = "処理に失敗", response = ResponseEntityForm.class) })
	public Calendar getCalendar(
			@ApiParam(required = false, value = "YYYY-MM-DD形式") @RequestParam(required = false, value = "calDate") String calDate,
			@ApiParam(required = false, value = "APIシークレットキー") @RequestHeader(required = false,value = "Authorization") String authorizationHeader) {
		LOGGER.info("指定日のカレンダー情報を取得 開始");
		if (authorizationHeader == null || !apiSecretkey.equals(authorizationHeader)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
		try {	
			return calendarUtil.getCalender(calDate);
		} catch (Exception ex) {
			LOGGER.error("指定日のカレンダー情報を取得で例外発生", ex);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
		}finally {
			LOGGER.info("指定日のカレンダー情報を取得 終了");
		}
	}
	
	/**
	 * 未回答通知
	 * 
	 */
	@RequestMapping(value = "/push", method = RequestMethod.GET)
	@ApiOperation(value = "行政/事業者へのリマインド通知", notes = "回答及び問い合わせ関連の通知をする.")
	@ResponseBody
	@ApiResponses(value = { @ApiResponse(code = 200, message = "成功", response = ResponseEntityForm.class),
			@ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class),
			@ApiResponse(code = 500, message = "処理に失敗", response = ResponseEntityForm.class) })
	public void answerList(
			@ApiParam(required = false, value = "リマインド通知の有効対象（app.reminder.mail.enabledTypes準拠）") @RequestParam(required = false, value = "enabledTypes") String enabledTypes,
			@ApiParam(required = false, value = "APIシークレットキー") @RequestHeader(required = false,value = "Authorization") String authorizationHeader) {
		LOGGER.info("未回答通知リマインド 開始");
		if (authorizationHeader == null || !apiSecretkey.equals(authorizationHeader)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
		try {	
			//リマインド通知有効対象の設定パラメータを取得
			List<Integer> reminderMailEnabledTypesList;
			String reminderMailEnabledTypesTemp = reminderMailEnabledTypes;
			
			if(enabledTypes != null && !enabledTypes.isEmpty() && isValidNumber(enabledTypes)) {
				reminderMailEnabledTypesTemp = enabledTypes;
			}
			
			if (reminderMailEnabledTypesTemp == null || reminderMailEnabledTypesTemp.trim().isEmpty()) {
		        // nullまたは空の場合は空のリストを設定
				reminderMailEnabledTypesList = Collections.emptyList();
		    } else {
		    	reminderMailEnabledTypesList = Arrays.stream(reminderMailEnabledTypesTemp.split(",")).map(Integer::parseInt).collect(Collectors.toList());
		    }
			
			//回答担当課通知対象(回答期限超過＆回答期限間近の未回答一覧)>>行政
			Map<String,Map<Integer,Map<Integer,List<Integer>>>> notResponseAnswer =answerService.notResponseAnswer(1);
			//通知権限部署対象(回答期限超過＆回答期限間近の未通知一覧)>>行政
			Map<String,Map<Integer,Map<Integer,List<Integer>>>> notNotifiedAnswer =answerService.notResponseAnswer(2);
			//事業者通知対象(事前協議かつ事業者が合否内容を登録していない状態で行政からの通知に応答のない一覧)>>事業者
			Map<String,List<Integer>> notResponseAnswerStep2Business = answerService.notResponseAnswerStep2Business();
			//回答担当課通知対象(事前協議かつ事業者が合否内容を登録して行政が返事をしていない回答)>>行政
			Map<String,Map<Integer,List<Integer>>> notResponseAnswerStep2 =answerService.notResponseAnswerStep2(1);
			//通知権限部署対象(事前協議かつ事業者が合否内容を登録して行政が行政確定通知をしていない回答)>>行政
			Map<String,Map<Integer,List<Integer>>> notNotifiedAnswerStep2 =answerService.notResponseAnswerStep2(2);
			//問い合わせに回答していないもの>>行政
			Map<String,Map<Integer,List<Integer>>> remindChat = chatService.getRemindMail();
			
			//リマインド通知の有効対象に含まれない場合は該当の結果を空にする
			if(!reminderMailEnabledTypesList.contains(1)) {
				notResponseAnswer = new HashMap<>();
			}
			if(!reminderMailEnabledTypesList.contains(2)) {
				notNotifiedAnswer = new HashMap<>();
			}
			if(!reminderMailEnabledTypesList.contains(3)) {
				notResponseAnswerStep2Business = new HashMap<>();
			}
			if(!reminderMailEnabledTypesList.contains(4)) {
				notResponseAnswerStep2 = new HashMap<>();
			}
			if(!reminderMailEnabledTypesList.contains(5)) {
				notNotifiedAnswerStep2 = new HashMap<>();
			}
			if(!reminderMailEnabledTypesList.contains(6)) {
				remindChat = new HashMap<>();
			}
			
			//行政のリマインドメール内容記載処理
			answerService.sendRemindMailAnswer(notResponseAnswer, notNotifiedAnswer, notResponseAnswerStep2, notNotifiedAnswerStep2,remindChat);
			//事業者のリマインドメール内容記載処理
			answerService.sendRemindMailAnswerBusiness(notResponseAnswerStep2Business);
		} catch (Exception ex) {
			LOGGER.error("未回答通知リマインドで例外発生", ex);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
		}finally {
			LOGGER.info("未回答通知リマインド 終了");
		}
	}
	
	public static boolean isValidNumber(String input) {
        // 数値またはカンマ区切りの数値の正規表現
        String regex = "^(\\d+)(,\\d+)*$";
        return input.matches(regex);
    }
}