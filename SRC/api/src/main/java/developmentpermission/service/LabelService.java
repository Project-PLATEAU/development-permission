package developmentpermission.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import developmentpermission.entity.Label;
import developmentpermission.form.LabelForm;
import developmentpermission.repository.LabelRepository;

/**
 * ラベルServiceクラス
 */
@Service
@Transactional
public class LabelService extends AbstractService {

	/** ラベルRepositoryインスタンス */
	@Autowired
	private LabelRepository labelRepository;

	/**
	 * ラベル一覧取得
	 * 
	 * @param viewCode  画面コード
	 * @param labelType 種別
	 * @return ラベル一覧
	 */
	public LabelForm getLabelByViewCode(String viewCode, String labelType) {
		LabelForm form = new LabelForm();

		if (viewCode != null && !EMPTY.equals(viewCode) && labelType != null && !EMPTY.equals(labelType)) {
			List<Label> labelList = labelRepository.findByViewCode(viewCode, labelType);

			Map<String, Object> labels = new HashMap<String, Object>();
			for (Label label : labelList) {
				labels.put(label.getLabelKey(), label.getLabelText());
			}
			form.setLabels(labels);
		}

		return form;
	}

	/**
	 * ラベル一覧取得
	 * 
	 * @param viewCode  画面コード
	 * @param applicationStepId 申請段階ID
	 * @param labelType 種別
	 * @return ラベル一覧
	 */
	public LabelForm getLabelByApplicationStepId(String viewCode, String applicationStepId, String labelType) {
		LabelForm form = new LabelForm();

		if (isNotEmpty(viewCode) && isNotEmpty(applicationStepId) && isNotEmpty(labelType )) {
			List<Label> labelList = labelRepository.getLabelForApplicationStepId(viewCode, applicationStepId, labelType);

			Map<String, Object> labels = new HashMap<String, Object>();
			for (Label label : labelList) {
				labels.put(label.getLabelKey(), label.getLabelText());
			}
			form.setLabels(labels);
		}

		return form;
	}
	
	/**
	 * 空ではないかチェック
	 * @param str
	 * @return　結果
	 */
	private boolean isNotEmpty(String str) {
		if (str != null && !EMPTY.equals(str)) {
			return true;
		}
		return false;
	}
}
