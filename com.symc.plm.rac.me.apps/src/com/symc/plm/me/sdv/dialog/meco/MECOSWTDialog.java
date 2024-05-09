package com.symc.plm.me.sdv.dialog.meco;

import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import com.kgm.common.dialog.SYMCAbstractDialog;
import com.symc.plm.me.sdv.operation.meco.CreateMECOOperation;
import com.symc.plm.me.sdv.view.meco.MECOSWTRenderingView;
import com.teamcenter.rac.kernel.TCComponentChangeItemRevision;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

public class MECOSWTDialog extends SYMCAbstractDialog {

	private Registry registry;
	private MECOSWTRenderingView mecoinfoPanel;
	@SuppressWarnings("unused")
    private TCComponentChangeItemRevision mecoRevision;

	public MECOSWTDialog(Shell paramShell) {
		super(paramShell);

		this.registry = Registry.getRegistry(this);
		this.createButtonBar(paramShell);
	}

	@Override
	protected void createDialogWindow(Composite paramComposite) {
		super.createDialogWindow(paramComposite);
	}

	@Override
	protected Composite createDialogPanel(ScrolledComposite parentScrolledComposite) {
		setDialogTextAndImage(registry.getString("CreateMECODialog.title"), registry.getImage("CreateMECO.ICON"));
		mecoinfoPanel = new MECOSWTRenderingView(parentScrolledComposite, true);

		return mecoinfoPanel.getComposite();
	}

	@Override
	protected boolean validationCheck() {
		return mecoinfoPanel.isSavable();
	}

	@Override
	protected boolean apply() {
		boolean isOk = true;
		StringBuffer message = new StringBuffer();
		if (mecoinfoPanel.getMecoRevision() == null) {
			try {
				// mecoinfoPanel.getParamMap();
				CreateMECOOperation mecoOperation = new CreateMECOOperation(mecoinfoPanel.getParamMap(), this, mecoinfoPanel.targetFolder, mecoinfoPanel);
				mecoinfoPanel.session.setStatus("Creating MECO...");
				mecoOperation.executeOperation();
			} catch (Exception e) {
				isOk = false;
				MessageBox.post(this.getShell(), e.getMessage(), "MECO Error", MessageBox.ERROR);
			} finally {
				mecoinfoPanel.session.setReadyStatus();
			}

			if (isOk) {
				message.append("MECO has been successfully registered.\n");
			}
		} else {
			mecoinfoPanel.save();
			message.append(mecoinfoPanel.getMecoRevision() + " has been saved successfully.\n");

			return false;
		}

		if (message.length() > 0)
			MessageBox.post(this.getShell(), message.toString(), "MECO Information", MessageBox.INFORMATION);

		return true;
	}

	public void setMECORevison(TCComponentChangeItemRevision mecoRevision) {
		this.mecoRevision = mecoRevision;
	}

	protected void okPressed() {
		if (!validationCheck()) {
			return;
		}

		if (apply()) {
			this.close();
		}
	}

}
