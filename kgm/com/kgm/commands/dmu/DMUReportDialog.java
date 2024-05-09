package com.kgm.commands.dmu;

import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import com.kgm.common.dialog.SYMCAbstractDialog;
import com.teamcenter.rac.util.Registry;

public class DMUReportDialog extends SYMCAbstractDialog {
	private Registry registry;
	private DMUReportInfoPanel infoPanel;

	/**
	 * 积己磊
	 */
	public DMUReportDialog(Shell parent) {
		super(parent);
		this.registry = Registry.getRegistry(this);
	}

	@Override
	protected void createDialogWindow(Composite paramComposite) {
		super.createDialogWindow(paramComposite);
	}
	
	@Override
	protected Composite createDialogPanel(ScrolledComposite parentScrolledComposite) {
		setDialogTextAndImage(registry.getString("DMUReportDialog.TITLE"), null);
		infoPanel = new DMUReportInfoPanel(parentScrolledComposite, true);
		return infoPanel.getComposite();
	}
	
	
	/**
	 * 利侩 傈 骇府单捞记
	 */
	@Override
	protected boolean validationCheck() {
		if(infoPanel.checkTextComponent()) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 历厘
	 */
	@Override
	protected boolean apply() {
		infoPanel.create();
		return true;
	}
}