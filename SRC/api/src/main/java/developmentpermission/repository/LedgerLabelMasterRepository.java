package developmentpermission.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import developmentpermission.entity.Answer;
import developmentpermission.entity.LedgerLabelMaster;

/**
 * 
 * M_帳票ラベルリポジトリインスタンス
 *
 */
@Transactional
@Repository
public interface LedgerLabelMasterRepository extends JpaRepository<LedgerLabelMaster, Integer>{
	
	/**
	 * 帳票IDから帳票ラベル取得
	 * @param ledgerId 帳票ID
	 * @return 帳票ラベル一覧
	 */
	@Query(value = "SELECT ledger_label_id, ledger_id, replace_identify, table_name, export_column_name, filter_column_name, filter_condition, item_id_1, item_id_2, convert_order, convert_format FROM public.m_ledger_label WHERE ledger_id = :ledgerId ORDER BY ledger_label_id", nativeQuery = true)
	List<LedgerLabelMaster> findByLedgerId(@Param("ledgerId") String ledgerId);
}
