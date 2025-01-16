package developmentpermission.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import developmentpermission.entity.Application;
import developmentpermission.entity.Chat;
import developmentpermission.entity.Department;
import developmentpermission.entity.Message;
import developmentpermission.form.AnswerNameForm;
import developmentpermission.form.AnswerStatusForm;
import developmentpermission.form.ApplicationSearchConditionForm;
import developmentpermission.form.ApplicationStepForm;
import developmentpermission.form.DepartmentForm;

/**
 * チャットDAO
 */
@Transactional
public class ChatDao extends AbstractDao {

	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(ChatDao.class);

	/**
	 * コンストラクタ
	 * 
	 * @param emf Entityマネージャファクトリ
	 */
	public ChatDao(EntityManagerFactory emf) {
		super(emf);
	}

	/**
	 * O_メッセージ検索<BR>
	 * 担当部署IDに紐づく未回答メッセージリスト抽出
	 * 
	 * @param departmentId 担当部署ID
	 * @return O_メッセージリスト
	 */
	@SuppressWarnings("unchecked")
	public List<Message> getNoAnswerMessageList(String departmentId) {
		LOGGER.debug("O_メッセージ検索 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();

			String sql = "" + //
					"SELECT " + //
					"  m.message_id AS message_id, " + //
					"  m.chat_id AS chat_id, " + //
					"  m.message_text AS message_text, " + //
					"  m.read_flag AS read_flag, " + //
					"  m.sender_id AS sender_id, " + //
					"  m.to_department_id AS to_department_id, " + //
					"  m.message_type AS message_type, " + //
					"  m.send_datetime AS send_datetime, " + //
					"  m.answer_complete_flag AS answer_complete_flag " + //
					"FROM " + //
					"  o_message m " + //
					"  INNER JOIN ( " + //
					"    SELECT  " + //
					"      a.chat_id AS lastChatId, " + //
					"      Max(a.send_datetime) AS lastSendDatetime  " + //
					"    FROM " + //
					"      o_message AS a " + //
					"    WHERE " + //
					"      EXISTS ( " + "        SELECT " + "          b.inquiry_address_id " + // 問合せ宛先ID
					"        FROM " + "          o_inquiry_address b " + // 「O_問合せ宛先」
					"        WHERE " + "          b.message_id = a.message_id " +
					"        AND " + 
					//他部署回答済みだが未読の場合の考慮を追加
					"          ((b.answer_complete_flag = '0') OR (b.answer_complete_flag = '1' AND b.read_flag = '0')) " + // 未回答 or 他部署回答済みだが未読の場合
					"        AND " + "          b.department_id = :departmentId " + // 担当課
					"      ) " + // メッセージIDに紐づく「O_問合せ宛先」に未回答の指定部署がある
					"    GROUP BY " + //
					"      a.chat_id " + //
					"  ) AS sub " + //
					"  ON " + //
					"    m.chat_id = sub.lastChatId " + //
					"WHERE " + //
					"  m.send_datetime = sub.lastSendDatetime " + // 最新なメッセージ
					"ORDER BY  " + //
					"  m.send_datetime DESC  "; // 送信日時で降順

			Query query = em.createNativeQuery(sql, Message.class);
			query = query.setParameter("departmentId", departmentId);
			return query.getResultList();
		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("O_メッセージ検索 終了");
		}
	}

	/**
	 * O_申請検索
	 * 
	 * @param chatId チャットID
	 * @return O_メッセージリスト
	 */
	@SuppressWarnings("unchecked")
	public List<Application> getApplicatioList(Integer chatId) {
		LOGGER.debug("O_申請検索 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();

			String sql = "" + //
					"SELECT " + //
					"  p.application_id AS application_id, " + //
					"  p.applicant_id AS applicant_id, " + //
					"  p.status AS status, " + //
					"  p.register_status AS register_status, " + //
					"  p.collation_text AS collation_text, " + //
					"  p.version_information AS version_information, " + //
					"  p.register_datetime AS register_datetime, " + //
					"  p.update_datetime AS update_datetime, " + //
					"  p.application_type_id AS application_type_id " + //
					"FROM " + //
					"  o_chat c " + //
					"INNER JOIN " + //
					"  o_answer a " + //
					"ON " + //
					"  c.answer_id = a.answer_id " + //
					"INNER JOIN " + //
					"  o_application p  " + //
					"ON " + //
					"  a.application_id = p.application_id " + //
					"WHERE " + //
					"  c.chat_id = :chatId  ";
			Query query = em.createNativeQuery(sql, Application.class);
			query = query.setParameter("chatId", chatId);
			return query.getResultList();
		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("O_メッセージ検索 終了");
		}
	}

	/**
	 * O_チャット検索
	 * 
	 * @param chatId チャットID
	 * @return O_メッセージリスト
	 */
	@SuppressWarnings("unchecked")
	public List<Chat> searchChat(ApplicationSearchConditionForm paramForm, List<Integer> applicationIdList) {
		LOGGER.debug("O_チャット検索 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();

			// 問い合わせステータス(複数不可)
			List<AnswerStatusForm> statusList = paramForm.getAnswerStatus();
			// 担当課(複数不可)
			List<DepartmentForm> departmentList = paramForm.getDepartment();
			// 回答者(複数不可)
			List<AnswerNameForm> answerUserList = paramForm.getAnswerName();
			// 申請段階ID（複数不可）
			List<ApplicationStepForm> applicationSteps = paramForm.getApplicationSteps();
			// WHERE句構築
			StringBuffer where = new StringBuffer();

			// 申請ID
			appendWhereText(where);
			where.append("oc.application_id IN ( :applicationIds ) ");

			// ステータス
			String readFlag = null;
			String answerCompleteFlag = null;
			boolean isAnswerCompleteConditionIncluded = false;
			boolean isAnswerCompleteFlagIncluded = false;
			if (statusList != null && statusList.size() > 0) {
				// ステータスは1件のみ
				AnswerStatusForm statusForm = statusList.get(0);
				String status = statusForm.getValue();
				if (status != null && !"".equals(status)) {
					// ステータス ＝ 未読
					if ("0".equals(status)) {
						readFlag = "0";
						answerCompleteFlag = "0";
						isAnswerCompleteFlagIncluded = false;
					}
					// ステータス ＝ 既読
					if ("1".equals(status)) {
						readFlag = "1";
						answerCompleteFlag = "0";
						isAnswerCompleteFlagIncluded = true;
					}
					// ステータス ＝ 回答済み
					if ("2".equals(status)) {
						readFlag = "1";
						answerCompleteFlag = "1";
						isAnswerCompleteConditionIncluded = true;
						isAnswerCompleteFlagIncluded = true;
					}
				}
			}

			// 担当課
			String departmentParam = null;
			if (departmentList != null && departmentList.size() > 0) {
				// 部署は1件のみ
				DepartmentForm departmentForm = departmentList.get(0);
				String departmentId = departmentForm.getDepartmentId();
				if (departmentId != null && !"".equals(departmentId)) {
					departmentParam = departmentId;
				}
			}

			// 回答者
			String senderIdParam = null;
			if (answerUserList != null && answerUserList.size() > 0) {
				// 回答者は1件のみ
				AnswerNameForm answerNameForm = answerUserList.get(0);
				String userId = answerNameForm.getUserId();
				if (userId != null && !"".equals(userId)) {
					appendWhereText(where);
					where.append("om.sender_id = :userId ");
					appendWhereText(where);
					where.append("om.message_type IN ( 2 ) ");
					senderIdParam = userId;

				}
			}

			// 申請段階
			Integer appicationStepParam = null;
			if (applicationSteps != null && applicationSteps.size() > 0) {
				ApplicationStepForm applicationStepForm = applicationSteps.get(0);
				Integer applicationStepId = applicationStepForm.getApplicationStepId();
				if (applicationStepId != null) {
					appendWhereText(where);
					where.append("oc.application_step_id = :applicationStepId ");
					appicationStepParam = applicationStepId;
				}
			}
			// O_チャットに問合せを行うベースクエリ
			String listChatsBaseQuery = "" + //
					"SELECT DISTINCT " + //
					"  oc.chat_id AS chat_id, " + // チャットID
					"  oc.application_id AS application_id, " + // 申請ID
					"  oc.application_step_id AS application_step_id, " + // 申請段階ID
					"  oc.department_answer_id AS department_answer_id, " + // 部署回答ID
					"  oc.answer_id AS answer_id, " + // 回答ID
					"  oc.government_answer_datetime AS government_answer_datetime, " + // 行政回答日時
					"  oc.establishment_post_datetime AS establishment_post_datetime, " + // 事業者投稿日時
					"  oc.last_answerer_id AS last_answerer_id, " + // 最終回答者ID
					"  oc.establishment_first_post_datetime AS establishment_first_post_datetime " + // 事業者初回投稿日時
					"FROM " + //
					"  o_chat AS oc ";// O_チャット
			String listChatsQuery = "";
			if (departmentParam == null) {
				// 担当課指定がない場合ベースクエリを使用
				listChatsQuery = listChatsBaseQuery;
			} else {
				// 担当課指定がある場合申請段階ごとにクエリを切替・結合

				// 事前相談: 回答IDから紐づく担当部署を対象に検索
				final String subQueryStep1 = "" + //
						"chats_step_1 AS ( " + //
						listChatsBaseQuery + //
						"   INNER JOIN o_answer AS oa " + //
						"     ON oc.answer_id = oa.answer_id " + //
						"   INNER JOIN m_category_judgement AS mcj " + //
						"     ON mcj.judgement_item_id = oa.judgement_id " + //
						"   INNER JOIN m_judgement_authority AS mja " + //
						"     ON mcj.judgement_item_id = mja.judgement_item_id " + //
						"   WHERE oc.application_step_id = 1 " + //
						"     AND mja.department_id = :departmentId " + //
						" ) ";
				// 事前協議: 部署回答IDから紐づく担当部署を対象に検索
				final String subQueryStep2 = "" + //
						"chats_step_2 AS (" + //
						listChatsBaseQuery + //
						"    INNER JOIN o_department_answer AS oda " + //
						"      ON oc.department_answer_id = oda.department_answer_id " + //
						"    WHERE oc.application_step_id = 2 " + //
						"      AND oda.department_id = :departmentId " + //
						" ) ";
				// 許可判定: 指定した部署が許可判定回答権限部署の場合全件取得
				final String subQueryStep3 = "" + //
						"chats_step_3 AS (" + //
						listChatsBaseQuery + //
						"    CROSS JOIN m_authority AS ma " + //
						"    WHERE oc.application_step_id = 3 " + //
						"    AND ma.answer_authority_flag IN ('1', '2') " + //
						"    AND ma.application_step_id = 3 " + //
						"    AND ma.department_id = :departmentId" + //
						"" + //
						" ) ";
				if (appicationStepParam == null) {
					// 申請段階の指定がない場合: 各申請段階の問い合わせ結果を結合
					listChatsQuery = "" + //
							"WITH " + //
							subQueryStep1 + //
							" , " + //
							subQueryStep2 + //
							" , " + //
							subQueryStep3 + //
							" , " + //
							"chats_union AS ( " + //
							"  SELECT * FROM chats_step_1 " + //
							"  UNION " + //
							"  SELECT * FROM chats_step_2 " + //
							"  UNION " + //
							"  SELECT * FROM chats_step_3 " + //
							") " + //
							"SELECT DISTINCT " + //
							"  oc.chat_id AS chat_id, " + // チャットID
							"  oc.application_id AS application_id, " + // 申請ID
							"  oc.application_step_id AS application_step_id, " + // 申請段階ID
							"  oc.department_answer_id AS department_answer_id, " + // 部署回答ID
							"  oc.answer_id AS answer_id, " + // 回答ID
							"  oc.government_answer_datetime AS government_answer_datetime, " + // 行政回答日時
							"  oc.establishment_post_datetime AS establishment_post_datetime, " + // 事業者投稿日時
							"  oc.last_answerer_id AS last_answerer_id, " + // 最終回答者ID
							"  oc.establishment_first_post_datetime AS establishment_first_post_datetime " + // 事業者初回投稿日時
							"  FROM chats_union AS oc ";
				} else {
					// 申請段階の指定がある場合: 対象の申請段階のみを検索
					if (appicationStepParam.equals(APPLICATION_STEP_ID_1)) {
						listChatsQuery = "" + //
								"WITH " + //
								subQueryStep1 + //
								" SELECT DISTINCT " + //
								"  oc.chat_id AS chat_id, " + // チャットID
								"  oc.application_id AS application_id, " + // 申請ID
								"  oc.application_step_id AS application_step_id, " + // 申請段階ID
								"  oc.department_answer_id AS department_answer_id, " + // 部署回答ID
								"  oc.answer_id AS answer_id, " + // 回答ID
								"  oc.government_answer_datetime AS government_answer_datetime, " + // 行政回答日時
								"  oc.establishment_post_datetime AS establishment_post_datetime, " + // 事業者投稿日時
								"  oc.last_answerer_id AS last_answerer_id, " + // 最終回答者ID
								"  oc.establishment_first_post_datetime AS establishment_first_post_datetime " + // 事業者初回投稿日時
								"FROM " + //
								"  chats_step_1 AS oc ";
					} else if (appicationStepParam.equals(APPLICATION_STEP_ID_2)) {
						listChatsQuery = "" + //
								"WITH " + //
								subQueryStep2 + //
								" SELECT DISTINCT " + //
								"  oc.chat_id AS chat_id, " + // チャットID
								"  oc.application_id AS application_id, " + // 申請ID
								"  oc.application_step_id AS application_step_id, " + // 申請段階ID
								"  oc.department_answer_id AS department_answer_id, " + // 部署回答ID
								"  oc.answer_id AS answer_id, " + // 回答ID
								"  oc.government_answer_datetime AS government_answer_datetime, " + // 行政回答日時
								"  oc.establishment_post_datetime AS establishment_post_datetime, " + // 事業者投稿日時
								"  oc.last_answerer_id AS last_answerer_id, " + // 最終回答者ID
								"  oc.establishment_first_post_datetime AS establishment_first_post_datetime " + // 事業者初回投稿日時
								"FROM " + //
								"  chats_step_2 AS oc ";
					} else if (appicationStepParam.equals(APPLICATION_STEP_ID_3)) {
						listChatsQuery = "" + //
								"WITH " + //
								subQueryStep3 + //
								" SELECT DISTINCT " + //
								"  oc.chat_id AS chat_id, " + // チャットID
								"  oc.application_id AS application_id, " + // 申請ID
								"  oc.application_step_id AS application_step_id, " + // 申請段階ID
								"  oc.department_answer_id AS department_answer_id, " + // 部署回答ID
								"  oc.answer_id AS answer_id, " + // 回答ID
								"  oc.government_answer_datetime AS government_answer_datetime, " + // 行政回答日時
								"  oc.establishment_post_datetime AS establishment_post_datetime, " + // 事業者投稿日時
								"  oc.last_answerer_id AS last_answerer_id, " + // 最終回答者ID
								"  oc.establishment_first_post_datetime AS establishment_first_post_datetime " + // 事業者初回投稿日時
								"FROM " + //
								"  chats_step_3 AS oc ";
					}

				}
			}
			String sql = "" + //
					listChatsQuery + //
					"INNER JOIN " + //
					"  o_message AS om " + // O_メッセージ
					"ON " + //
					"  om.chat_id = oc.chat_id " + //
					where + // where句
					"ORDER BY " + //
					"  oc.chat_id ASC ";
			// ステータスパラメータ設定しない場合、
			if (statusList == null || statusList.size() == 0) {
				Query query = em.createNativeQuery(sql, Chat.class);
				// 申請ID
				query = query.setParameter("applicationIds", applicationIdList);
				// 担当課
				if (departmentParam != null) {
					query = query.setParameter("departmentId", departmentParam);
				}
				// 回答者
				if (senderIdParam != null) {
					query = query.setParameter("userId", senderIdParam);
				}
				// 申請段階
				if (appicationStepParam != null) {
					query = query.setParameter("applicationStepId", appicationStepParam);
				}
				return query.getResultList();
			} else {
				// 「回答済み」でステータス検索する場合、行政担当者から投げている事業者未回答の問い合わせを条件に追加する
				// 行政→事業者への初回投稿のみの場合、行政→行政への初回投稿のみの場合は回答済みとする
				String sqlWithStatusFromGoverment = "";
				if (isAnswerCompleteConditionIncluded) {
					sqlWithStatusFromGoverment = "" + //
							" UNION " + //
							"    SELECT DISTINCT  " + //
							"    a.chat_id " + //
							" FROM o_message AS a  " + //
							"LEFT OUTER JOIN o_message AS b  " + //
							"ON a.chat_id = b.chat_id AND a.message_id <> b.message_id AND b.message_type = '1' " + //
							"WHERE (a.message_type = '2' AND b.message_id IS NULL) " + //
							"OR (a.message_type = '3' AND b.message_id IS NULL)" + //
							"";
				}
				String sqlConditionAnswerCompleteFlag = "";
				if (isAnswerCompleteFlagIncluded) {
					sqlConditionAnswerCompleteFlag = "  AND  " + //
							"    m.answer_complete_flag =:answerCompleteFlag  ";
				}
				String sqlWithStatus = "" + //
						"WITH subT1 AS ( " + // ステータス以外の検索条件
						sql + //
						"), " + //
						" subT2  AS (" + // ステータスの検索SQL
						"  SELECT DISTINCT " + //
						"    m.chat_id  " + //
						"  FROM " + //
						"    o_message AS m  " + //
						"  INNER JOIN  o_chat AS c  " + //
						"  ON " + //
						"    m.chat_id = c.chat_id " + //
						"  AND " + //
						"   m.send_datetime = c.establishment_post_datetime " + //
						"  WHERE  " + //
						"    m.message_type = 1 " + //
						"  AND  " + //
						"    m.read_flag =:readFlag  " + //
						sqlConditionAnswerCompleteFlag + //
						sqlWithStatusFromGoverment + //
						") " + //
						"   " + //
						" SELECT DISTINCT  " + //
						"   subT1.chat_id AS chat_id,  " + // チャットID
						"   subT1.application_id AS application_id, " + // 申請ID
						"   subT1.application_step_id AS application_step_id, " + // 申請段階ID
						"   subT1.department_answer_id AS department_answer_id, " + // 部署回答ID
						"   subT1.answer_id AS answer_id,  " + // 回答ID
						"   subT1.government_answer_datetime AS government_answer_datetime,  " + // 行政回答日時
						"   subT1.establishment_post_datetime AS establishment_post_datetime,  " + // 事業者投稿日時
						"   subT1.last_answerer_id AS last_answerer_id,  " + // 最終回答者ID
						"   subT1.establishment_first_post_datetime AS establishment_first_post_datetime  " + // 事業者初回投稿日時
						" FROM  " + //
						"   subT1  " + //
						" INNER JOIN  " + //
						"   subT2  " + //
						" ON  " + //
						" subT1.chat_id = subT2.chat_id ";

				Query query = em.createNativeQuery(sqlWithStatus, Chat.class);
				// 申請IDパラメータ設定
				query = query.setParameter("applicationIds", applicationIdList);
				// 担当課パラメータ設定
				if (departmentParam != null) {
					query = query.setParameter("departmentId", departmentParam);
				}
				// ステータスパラメータ設定
				query = query.setParameter("readFlag", readFlag);
				if (isAnswerCompleteFlagIncluded) {
					query = query.setParameter("answerCompleteFlag", answerCompleteFlag);
				}
				
				// 回答者パラメータ設定
				if (senderIdParam != null) {
					query = query.setParameter("userId", senderIdParam);
				}
				// 申請段階パラメータ設定
				if (appicationStepParam != null) {
					query = query.setParameter("applicationStepId", appicationStepParam);
				}
				return query.getResultList();

			}
		} finally {
			LOGGER.debug("O_チャット報検索 終了");
			if (em != null) {
				em.close();
			}
		}
	}

	/**
	 * O_メッセージ検索
	 * 
	 * @param chatId           チャットID
	 * @param messageType      メッセージタイプ
	 * @param departmentId     担当部署ID
	 * @param sendMailInterval 送信間隔（分）
	 * @param messageId        メッセージID
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Message> getMessageListForSendMail(Integer chatId, Integer messageType, String departmentId,
			int sendMailInterval, Integer messageId) {
		LOGGER.debug("O_メッセージ検索 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();

			String sql = "" + //
					"SELECT " + //
					"  m.message_id AS message_id, " + //
					"  m.chat_id AS chat_id, " + //
					"  m.message_text AS message_text, " + //
					"  m.read_flag AS read_flag, " + //
					"  m.sender_id AS sender_id, " + //
					"  m.to_department_id AS to_department_id, " + //
					"  m.message_type AS message_type, " + //
					"  m.send_datetime AS send_datetime, " + //
					"  m.answer_complete_flag AS answer_complete_flag " + //
					"FROM " + //
					"  o_message m " + //
					"WHERE " + //
					" m.message_type =:messageType " + //
					"AND " + //
					" m.message_id <> :messageId " + // 今回投稿以外のメッセージ
					"AND " + //
					"  EXISTS ( " + //
					"    SELECT " + //
					"      a.inquiry_address_id " + //
					"    FROM " + //
					"      o_inquiry_address a " + //
					"    WHERE " + //
					"      a.message_id = m.message_id " + //
					"    AND " + //
					"      a.department_id = :departmentId " + //
					"  ) " + //
					"AND " + //
					"  m.chat_id =:chatId " + // チャットID
					"AND " + //
					"  m.send_datetime > now() + cast(' -" + sendMailInterval + " minutes' as INTERVAL) " + // メール通知間隔(分)の前から、指定部署に投稿有無
					"ORDER BY " + "  m.send_datetime DESC ";

			Query query = em.createNativeQuery(sql, Message.class);
			query = query.setParameter("messageType", messageType);
			query = query.setParameter("messageId", messageId);
			query = query.setParameter("departmentId", departmentId);
			query = query.setParameter("chatId", chatId);
			return query.getResultList();
		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("O_メッセージ検索 終了");
		}
	}

	/**
	 * 部署回答IDから部署情報を取得
	 * 
	 * @param departmentAnswerId 部署回答ID
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Department> getDepartmentListByDepartmentAnswerId(Integer departmentAnswerId) {
		LOGGER.debug("部署回答部署検索 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();
			String sql = "" + //
					"SELECT " + //
					"md.department_id AS department_id " + //
					", md.department_name AS department_name " + //
					", md.answer_authority_flag AS answer_authority_flag " + //
					", md.mail_address AS mail_address " + //
					", md.admin_mail_address AS admin_mail_address " + //
					" FROM " + //
					"  m_department AS md " + //
					" LEFT OUTER JOIN  " + //
					"  o_department_answer AS od " + //
					" ON  " + //
					"  md.department_id = od.department_id " + //
					" WHERE  " + //
					" od.department_answer_id = :departmentAnswerId ";
			Query query = em.createNativeQuery(sql, Department.class);
			query = query.setParameter("departmentAnswerId", departmentAnswerId);
			return query.getResultList();
		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("部署回答部署検索 終了");
		}
	}
}
