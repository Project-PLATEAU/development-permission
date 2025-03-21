package developmentpermission.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import developmentpermission.entity.LedgerMaster;

/**
 * M_帳票Repositoryインタフェース
 */
@Transactional
@Repository
public interface LedgerMasterRepository extends JpaRepository<LedgerMaster, String> {

	/**
	 * 帳票を取得する
	 * 
	 * @param applicationStepId 申請段階ID
	 * @return LedgerMasterList
	 */
	@Query(value = "SELECT ledger_id, application_step_id, ledger_name, display_name, template_path, output_type, notification_flag, ledger_type, update_flag, notify_flag, upload_extension, information_text FROM m_ledger WHERE application_step_id = :applicationStepId AND output_type = '1' ORDER BY ledger_id ASC", nativeQuery = true)
	List<LedgerMaster> getLedgerMasterListForDisplay(@Param("applicationStepId") Integer applicationStepId);
	
	/**
	 * 帳票を取得する
	 * 
	 * @param applicationStepId 申請段階ID
	 * @return LedgerMasterList
	 */
	@Query(value = "SELECT ledger_id, application_step_id, ledger_name, display_name, template_path, output_type, notification_flag, ledger_type, update_flag, notify_flag, upload_extension, information_text FROM m_ledger WHERE application_step_id = :applicationStepId ORDER BY ledger_id ASC", nativeQuery = true)
	List<LedgerMaster> getLedgerMasterListForExport(@Param("applicationStepId") Integer applicationStepId);
	/**
	 * 帳票を取得する
	 * 
	 * @param ledgerId 帳票マスタID
	 * @return LedgerMasterList
	 */
	@Query(value = "SELECT ledger_id, application_step_id, ledger_name, display_name, template_path, output_type, notification_flag, ledger_type, update_flag, notify_flag, upload_extension, information_text FROM m_ledger WHERE ledger_id = :ledgerId ORDER BY ledger_id ASC", nativeQuery = true)
	List<LedgerMaster> getLedgerMasterByLedgerId(@Param("ledgerId") String ledgerId);

	/**
	 * 開発登録簿対象帳票マスタID一覧取得
	 * 
	 * @return List<String>
	 */
	@Query(value = "SELECT DISTINCT ledger_id FROM m_ledger WHERE ledger_type = '1' ORDER BY ledger_id", nativeQuery = true)
	List<String> getDevelopmentRegisterFileList();
}
