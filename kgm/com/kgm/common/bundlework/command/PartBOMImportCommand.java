package com.kgm.common.bundlework.command;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import com.kgm.common.bundlework.imp.BWPartBOMImpDialog;
import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aifrcp.AIFUtility;

public class PartBOMImportCommand extends AbstractAIFCommand {

	public PartBOMImportCommand() {
		/** Dialog »£√‚. */
		Shell shell = AIFUtility.getActiveDesktop().getShell();
		BWPartBOMImpDialog partMasterDialog = new BWPartBOMImpDialog(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		partMasterDialog.dialogOpen();
	}
}
