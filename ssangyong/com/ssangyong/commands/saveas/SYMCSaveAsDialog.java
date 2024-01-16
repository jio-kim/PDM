package com.ssangyong.commands.saveas;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.JPanel;

import com.ssangyong.common.dialog.SYMCAWTAbstractDialog;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.util.Registry;

public class SYMCSaveAsDialog extends SYMCAWTAbstractDialog {

	private static final long serialVersionUID = 1L;
	private Registry registry = Registry.getRegistry(this);
	private SYMCSaveAsInfoPanel infoPanel;
	private TCComponent target;

	public SYMCSaveAsDialog(Frame frame, TCComponent target) {
		super(frame, false);
		this.target = target;
		
		initUI();
	}

	/**
	 * UI Panel 생성.
	 * 
	 * @Copyright : S-PALM
	 * @author : 권상기
	 * @since  : 2012. 12. 18.
	 */
	private void initUI() {
		setTitle(registry.getString("SSANGYONGSaveAsDialog.TITLE"));
		createDialogUI(registry.getString("SSANGYONGSaveAsDialog.MESSAGE_TITLE"), registry.getImageIcon("SSANGYONGSaveAsDialogHeader.ICON"));
		
		infoPanel = new SYMCSaveAsInfoPanel(this, target);
		
		add("unbound.bind", infoPanel);
	}

	@Override
	protected JPanel getUIPanel() {
		return infoPanel;
	}

	@Override
	public void invokeOperation(ActionEvent e) {
		SYMCSaveAsOperation operation = new SYMCSaveAsOperation(this);
		session.queueOperation(operation);
	}

	@Override
	public boolean validCheck() {
		return infoPanel.validCheck();
	}

	public SYMCSaveAsInfoPanel getInfoPanel() {
		return infoPanel;
	}

	public TCComponent getTarget() {
		return target;
	}
}
