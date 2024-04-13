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
import developmentpermission.entity.Message;
import developmentpermission.form.AnswerNameForm;
import developmentpermission.form.AnswerStatusForm;
import developmentpermission.form.ApplicationSearchConditionForm;
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
			"      EXISTS ( " +
			"        SELECT " +
			"          b.inquiry_address_id " + //問合せ宛先ID
			"        FROM " +
			"          o_inquiry_address b " + //「O_問合せ宛先」
			"        WHERE " +
			"          b.message_id = a.message_id " +
			"        AND " +
			"          b.answer_complete_flag = '0' " +//未回答
			"        AND " +
			"          b.department_id = :departmentId " +//担当課
			"      ) " + //メッセージIDに紐づく「O_問合せ宛先」に未回答の指定部署がある
			"    GROUP BY " + //
			"      a.chat_id " + //
			"  ) AS sub " + //
			"  ON " + //
			"    m.chat_id = sub.lastChatId " + //
			"WHERE " + //
			"  m.send_datetime = sub.lastSendDatetime " + // 最新なメッセージ
			"ORDER BY  " + //
			"  m.send_datetime DESC  " ; // 送信日時で降順
			
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
					"  p.update_datetime AS update_datetime " + //
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

			// ステータス(複数不可)
			List<AnswerStatusForm> statusList = paramForm.getAnswerStatus();
			// 担当課(複数不可)
			List<DepartmentForm> departmentList = paramForm.getDepartment();
			// 回答者(複数不可)
			List<AnswerNameForm> answerUserList = paramForm.getAnswerName();
			// WHERE句構築
			StringBuffer where = new StringBuffer();

			// 申請ID
			appendWhereText(where);
			where.append("oa.application_id IN ( :applicationIds ) ");

			// ステータス
			String readFlag = null;
			String answerCompleteFlag = null;
			if (statusList != null && statusList.size() > 0) {
				// ステータスは1件のみ
				AnswerStatusForm statusForm = statusList.get(0);
				String status = statusForm.getValue();
				if (status != null && !"".equals(status)) {
					// ステータス ＝ 未読
					if ("0".equals(status)) {
						readFlag = "0";
						answerCompleteFlag = "0";
					}
					// ステータス ＝ 既読
					if ("1".equals(status)) {
						readFlag = "1";
						answerCompleteFlag = "0";
					}
					// ステータス ＝ 回答済み
					if ("2".equals(status)) {
						readFlag = "1";
						answerCompleteFlag = "1";
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
					appendWhereText(where);
					where.append("mcj.department_id = :departmentId ");
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

			String sql = "" + //
					"SELECT DISTINCT " + //
					"  oc.chat_id AS chat_id, " + // チャットID
					"  oc.answer_id AS answer_id, " + // 回答ID
					"  oc.government_answer_datetime AS government_answer_datetime, " + // 行政回答日時
					"  oc.establishment_post_datetime AS establishment_post_datetime, " + // 事業者投稿日時
					"  oc.last_answerer_id AS last_answerer_id, " + // 最終回答者ID
					"  oc.establishment_first_post_datetime AS establishment_first_post_datetime " + // 事業者初回投稿日時
					"FROM " + //
					"  o_chat AS oc " + // O_チャット
					"INNER JOIN " + //
					"  o_answer AS oa " + // O_回答
					"ON " + //
					"  oc.answer_id = oa.answer_id " + //
					"INNER JOIN " + //
					"  o_message AS om " + // O_メッセージ
					"ON " + //
					"  om.chat_id = oc.chat_id " + //
					"INNER JOIN " + //
					"  m_category_judgement AS mcj " + // M_区分判定
					"ON " + //
					"  oa.judgement_id = mcj.judgement_item_id " + //
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
				return query.getResultList();
			} else {

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
						"  AND  " + //
						"    m.answer_complete_flag =:answerCompleteFlag  " + //
						") " + //
						"   " + //
						" SELECT DISTINCT  " + //
						"   subT1.chat_id AS chat_id,  " + //
						"   subT1.answer_id AS answer_id,  " + //
						"   subT1.government_answer_datetime AS government_answer_datetime,  " + //
						"   subT1.establishment_post_datetime AS establishment_post_datetime,  " + //
						"   subT1.last_answerer_id AS last_answerer_id,  " + //
						"   subT1.establishment_first_post_datetime AS establishment_first_post_datetime  " + //
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
				query = query.setParameter("answerCompleteFlag", answerCompleteFlag);
				
				// 回答者パラメータ設定
				if (senderIdParam != null) {
					query = query.setParameter("userId", senderIdParam);
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
	 * @param chatId チャットID
	 * @param messageType メッセージタイプ
	 * @param departmentId 担当部署ID
	 * @param sendMailInterval 送信間隔（分）
	 * @param  messageId メッセージID
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Message> getMessageListForSendMail(Integer chatId, Integer messageType, String departmentId, int sendMailInterval, Integer messageId) {
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
					"  m.chat_id =:chatId " +   // チャットID
					"AND " + //
					"  m.send_datetime > now() + cast(' -" + 
					sendMailInterval +
					" minutes' as INTERVAL) " +   // メール通知間隔(分)の前から、指定部署に投稿有無
					"ORDER BY " +
					"  m.send_datetime DESC ";
			
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
}
