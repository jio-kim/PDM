package com.kgm.common.bundlework.command;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import com.kgm.common.bundlework.imp.BWStandardPartImpDialog;
import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aifrcp.AIFUtility;

public class StandardPartImportCommand extends AbstractAIFCommand {

	public StandardPartImportCommand() {
		/** Dialog »£√‚. */
		Shell shell = AIFUtility.getActiveDesktop().getShell();
		BWStandardPartImpDialog partMasterDialog = new BWStandardPartImpDialog(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		partMasterDialog.dialogOpen();
	}
}
