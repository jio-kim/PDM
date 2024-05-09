package com.kgm.commands.createfolder;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.JPanel;

import com.kgm.common.dialog.SYMCAWTAbstractDialog;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.util.Registry;

public class CreateFolderDialog extends SYMCAWTAbstractDialog {

	private static final long serialVersionUID = 1L;
	private CreateFolderInfoPanel infoPanel;
	private Registry registry = Registry.getRegistry(this);
	private TCComponent selectComp;

	/**
	 * 생성자.
	 * @copyright : S-PALM
	 * @author : 권상기
	 * @since  : 2013. 1. 10.
	 * @param frame
	 */
	public CreateFolderDialog(Frame frame) {
		super(frame, false);
		
		initUI();
	}

	/**
	 * UI Panel.
	 * @Copyright : S-PALM
	 * @author : 권상기
	 * @since  : 2013. 1. 10.
	 */
	private void initUI() {
		setTitle(registry.getString("CreateFolderDialog.TITLE"));
		createDialogUI(registry.getString("CreateFolderDialog.MESSAGE_TITLE"), registry.getImageIcon("CreateFolderHeader.ICON"));
		
		infoPanel = new CreateFolderInfoPanel(this);
		
		add("unbound.bind", infoPanel);
	}

	@Override
	protected JPanel getUIPanel() {
		return infoPanel;
	}

	@Override
	public void invokeOperation(ActionEvent e) {
		CreateFolderOperation operation = new CreateFolderOperation(this);
		session.queueOperation(operation);
	}

	@Override
	public boolean validCheck() {
		return infoPanel.validCheck();
	}

	public CreateFolderInfoPanel getInfoPanel() {
		return infoPanel;
	}
	
	public void setSelectComp(TCComponent comp){
		this.selectComp = comp;
	}

	public TCComponent getSelectComp() {
		return selectComp;
	}
}
