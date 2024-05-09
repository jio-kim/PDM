package com.kgm.commands.newdataset;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.JPanel;

import com.kgm.common.SYMCClass;
import com.kgm.common.attachfile.AttachFilePanel;
import com.kgm.common.dialog.SYMCAWTAbstractDialog;
import com.kgm.common.utils.CustomUtil;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.rac.util.VerticalLayout;

public class NewDatasetDialog extends SYMCAWTAbstractDialog {

	private static final long serialVersionUID = 1L;

	public Registry registry = Registry.getRegistry(this);

	/** 파일 첨부 패널 */
	private AttachFilePanel attachFilePanel;

	/**
	 * 생성자.
	 * 
	 * @copyright : S-PALM
	 * @author : 권상기
	 * @since : 2012. 8. 10.
	 * @param activeDesktop
	 */
	public NewDatasetDialog(Frame frame) {
		super(frame, false);
		initUI();
	}

	private void initUI() {
		String dTitle = registry.getString("NewDataset.Dialog.Title");
//		setTitle("데이터셋 등록");
//		createDialogUI("데이터셋 등록", registry.getImageIcon("NewFile.ICON"));
		setTitle(dTitle);
		createDialogUI(dTitle, registry.getImageIcon("NewFile.ICON"));

		JPanel rightPanel = new JPanel(new VerticalLayout(5, 5, 5, 5, 5));
		rightPanel.setOpaque(false);

		attachFilePanel = new AttachFilePanel();
		attachFilePanel.setRelationType(SYMCClass.SPECIFICATION_REL);
//		attachFilePanel.setTitledBorder("참고 자료");
		attachFilePanel.setTitledBorder(registry.getString("NewDataset.Dialog.TitleBolder"));
		attachFilePanel.addChoosableFileFilter(SYMCClass.DATASET_FILTER_DOC(), registry
				.getString("Doc.TEXT"));
		attachFilePanel.setQuerySaveAs(CustomUtil.getTextServerString(session, "k_find_dataset_name"), null,
				AttachFilePanel.DATASET_TYPE_ARRAY, null, null, null);
		attachFilePanel.setVisibleModifybutton(false);
		attachFilePanel.setSaveAsButtonbutton(false);
		attachFilePanel.setPreferredSize(new Dimension(450, 220));

		rightPanel.add("unbound.bind", attachFilePanel);

		add("unbound.bind", rightPanel);
	}

	@Override
	protected JPanel getUIPanel() {
		return null;
	}

	@Override
	public void invokeOperation(ActionEvent e) {
		try {
			NewDatasetOperation operation = new NewDatasetOperation(this);
			session.queueOperation(operation);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	@Override
	public boolean validCheck() {
		return true;
	}

	/**
	 * @Copyright : S-PALM
	 * @author : 권상기
	 * @since : 2012. 8. 10.
	 * @return :registry을 리턴합니다.
	 */
	public Registry getRegistry() {
		return registry;
	}

	/**
	 * @Copyright : S-PALM
	 * @author : 권상기
	 * @since : 2012. 8. 10.
	 * @return :attachFilePanel을 리턴합니다.
	 */
	public AttachFilePanel getAttachFilePanel() {
		return attachFilePanel;
	}

}
