package com.ssangyong.commands.revise;

import java.awt.Color;
import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.ssangyong.common.dialog.SYMCAWTAbstractDialog;
import com.teamcenter.rac.util.Registry;

/**
 * Revise Dialog
 */
@SuppressWarnings("serial")
public class ChangeDatasetDialog extends SYMCAWTAbstractDialog {
	
	/** Revise Info Panel */
	private ChangeDatasetPanel ChangeDatasetPanel;
	/** TC Registry */
	private Registry registry;
	
	public ChangeDatasetDialog(Frame frame) throws Exception {
		super(frame);
		this.registry = Registry.getRegistry(this);
		initUI();
	}

	private void initUI() throws Exception {
		/** Title 지정 */
		setTitle("Create dataset from previous revision");
		/** 상위 텍스트 및 아이콘 지정. 필수 적용 메소드 항목 */
		createDialogUI(registry.getString("ReviseDialog.MESSGAE_TITLE"), registry.getImageIcon("Revise_32.ICON"));
		
		ChangeDatasetPanel = new ChangeDatasetPanel(this);
		JScrollPane mainScrollPane = new JScrollPane(ChangeDatasetPanel);
		mainScrollPane.getViewport().setBackground(Color.white);
		mainScrollPane.updateUI();
		add("unbound.bind", mainScrollPane);
		showVisible(false);
		
	}

	@Override
	protected JPanel getUIPanel() {
		return ChangeDatasetPanel;
	}

	@Override
	public boolean validCheck() {
		return ChangeDatasetPanel.validCheck();
	}

	@Override
	public void invokeOperation(ActionEvent e) {
		ChangeDatasetOperation operation = new ChangeDatasetOperation(this, ChangeDatasetPanel);
		session.queueOperation(operation);
	}
	

}
