package com.kgm.common.bundlework.command;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import com.kgm.common.bundlework.imp.BWBomTypeDbImpDialog;
import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aifrcp.AIFUtility;

public class PartBOMImportDbCommand extends AbstractAIFCommand {

	public PartBOMImportDbCommand() {
		/** Dialog »£√‚. */
		Shell shell = AIFUtility.getActiveDesktop().getShell();
		BWBomTypeDbImpDialog partMasterDialog = new BWBomTypeDbImpDialog(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		partMasterDialog.dialogOpen();
	}
}
