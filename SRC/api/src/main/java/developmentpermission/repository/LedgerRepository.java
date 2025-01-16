package developmentpermission.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import developmentpermission.entity.Ledger;

/**
 * O_帳票Repositoryクラス
 */
public interface LedgerRepository extends JpaRepository<Ledger, Integer> {

	/**
	 * 帳票一覧取得
	 * 
	 * @return 帳票一覧
	 */
	@Query(value = "SELECT file_id, application_id, application_step_id, ledger_id, file_name, file_path, register_datetime, receipt_datetime, notify_file_path, notify_flag FROM o_ledger WHERE application_id = :applicationId AND application_step_id = :applicationStepId ORDER BY file_id ASC", nativeQuery = true)
	List<Ledger> getLedgerList(@Param("applicationId") Integer applicationId, @Param("applicationStepId") Integer applicationStepId);

	/**
	 * 帳票一覧取得
	 * @param 申請ID
	 * @param 申請段階ID
	 * @param 通知フラグ
	 * @return 帳票一覧
	 */
	@Query(value = "SELECT file_id, application_id, application_step_id, ledger_id, file_name, file_path, register_datetime, receipt_datetime, notify_file_path, notify_flag FROM o_ledger WHERE application_id = :applicationId AND application_step_id = :applicationStepId AND notify_flag = :notifyFlag ORDER BY file_id ASC", nativeQuery = true)
	List<Ledger> getLedgerList(@Param("applicationId") Integer applicationId, @Param("applicationStepId") Integer applicationStepId, @Param("notifyFlag") String notifyFlag);

	/**
	 * 帳票一覧取得
	 * 
	 * @return 帳票一覧
	 */
	@Query(value = "SELECT file_id, application_id, application_step_id, ledger_id, file_name, file_path, register_datetime, receipt_datetime, notify_file_path, notify_flag FROM o_ledger WHERE file_id = :fileId ", nativeQuery = true)
	List<Ledger> findByFileId(@Param("fileId") Integer fileId);

	/**
	 * 帳票一覧取得
	 * @param 申請ID
	 * @param 帳票マスタID
	 * @return 帳票一覧
	 */
	@Query(value = "SELECT file_id, application_id, application_step_id, ledger_id, file_name, file_path, register_datetime, receipt_datetime, notify_file_path, notify_flag FROM o_ledger WHERE application_id = :applicationId AND ledger_id = :ledgerId ORDER BY file_id ASC", nativeQuery = true)
	List<Ledger> getLedgerListByApplicationIdLedgerId(@Param("applicationId") Integer applicationId, @Param("ledgerId") String ledgerId);

}
