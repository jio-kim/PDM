package com.kgm.commands.downdataset;

import java.awt.Dimension;

import javax.swing.JDialog;
import javax.swing.JPanel;

import com.kgm.common.SYMCClass;
import com.kgm.common.SYMCInterfaceInfoPanel;
import com.kgm.common.attachfile.AttachFilePanel;
import com.kgm.common.utils.CustomUtil;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.rac.util.VerticalLayout;

public class DownDataSetInfoPanel extends JPanel implements SYMCInterfaceInfoPanel {

	private static final long serialVersionUID = 1L;
	private AIFComponentContext[] targets;
	private JDialog dialog;
	private AttachFilePanel attachFilePanel;
	private TCSession session = CustomUtil.getTCSession();
	private Registry registry = Registry.getRegistry(this);

	/**
	 * 생성자.
	 * 
	 * @copyright : S-PALM
	 * @author : 권상기
	 * @since : 2013. 1. 8.
	 */
	public DownDataSetInfoPanel(JDialog dialog, AIFComponentContext[] targets) {
		super(new VerticalLayout(5, 5, 5, 5, 5));
		this.dialog = dialog;
		this.targets = targets;

		initUI();

		/** 선택 대상 List Add */
		listAdd();
	}

	/**
	 * UI Panel.
	 * 
	 * @Copyright : S-PALM
	 * @author : 권상기
	 * @since : 2013. 1. 8.
	 */
	private void initUI() {
		setOpaque(false);

		attachFilePanel = new AttachFilePanel();
		attachFilePanel.setRelationType(SYMCClass.SPECIFICATION_REL);
		attachFilePanel.setTitledBorder("Download File");
		attachFilePanel.addChoosableFileFilter(SYMCClass.DATASET_FILTER_DOC(), registry
				.getString("Doc.TEXT"));
		attachFilePanel.setQuerySaveAs(CustomUtil.getTextServerString(session, "k_find_dataset_name"), null,
				AttachFilePanel.DATASET_TYPE_ARRAY, null, null, null);
		attachFilePanel.setVisibleModifybutton(false);
		attachFilePanel.setPreferredSize(new Dimension(450, 200));
		attachFilePanel.setVisibleAddFileButton(false);

		add("unbound.bind", attachFilePanel);
	}

	/**
	 * 선택 대상 List Add.
	 * 
	 * @Copyright : S-PALM
	 * @author : 권상기
	 * @since : 2013. 1. 8.
	 */
	private void listAdd() {
		try {
			int targetSize = targets.length;
			AIFComponentContext tcComp = null;
			TCComponent comp = null;
			for (int i = 0; i < targetSize; i++) {
				tcComp = targets[i];
				comp = (TCComponent) tcComp.getComponent();
				if (comp instanceof TCComponentDataset) {
					attachFilePanel.addAttachDatasetList(tcComp);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public AttachFilePanel getAttachFilePanel() {
		return attachFilePanel;
	}

	@Override
	public boolean validCheck() {
		int size = attachFilePanel.getListModel().getSize();
		System.out.println("size : " + size);
		if(attachFilePanel.getListModel().isEmpty() || size == 0){
			MessageBox.post(dialog, "복사 할 DataSet이 List에 없습니다.", "알림", MessageBox.INFORMATION);
			return false;
		}
		return true;
	}
}
