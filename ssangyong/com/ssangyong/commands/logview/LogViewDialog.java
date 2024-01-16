package com.ssangyong.commands.logview;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.JPanel;

import com.ssangyong.common.dialog.SYMCAWTAbstractDialog;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.util.Registry;

public class LogViewDialog extends SYMCAWTAbstractDialog {

	private static final long serialVersionUID = 1L;
	private AIFComponentContext comp;
	private LogViewInfoPanel infoPanel;
	private Registry registry = Registry.getRegistry(this);

	/** 
	 * 생성자.
	 * @copyright : S-PALM
	 * @author : 권상기
	 * @since  : 2013. 1. 8.
	 * @param frame
	 * @param comp 
	 */
	public LogViewDialog(Frame frame, AIFComponentContext comp) {
		super(frame, false);
		
		this.comp = comp;
		
		initUI();
		
		okButton.setVisible(false);
		applyButton.setVisible(false);
	}

	/**
	 * UI Panel.
	 * @Copyright : S-PALM
	 * @author : 권상기
	 * @since  : 2013. 1. 8.
	 */
	private void initUI() {
		setTitle(registry.getString("LogViewDialog.TITLE"));
		createDialogUI(registry.getString("LogViewDialog.MESSAGE_TITLE"), registry.getImageIcon("LogViewDialogHeader.ICON"));
		
		infoPanel = new LogViewInfoPanel(this, comp);
		
		add("unbound.bind", infoPanel);
	}

	@Override
	protected JPanel getUIPanel() {
		return infoPanel;
	}

	@Override
	public void invokeOperation(ActionEvent e) {
	}

	@Override
	public boolean validCheck() {
		return infoPanel.validCheck();
	}

	public LogViewInfoPanel getInfoPanel() {
		return infoPanel;
	}
}
