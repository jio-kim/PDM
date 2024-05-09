package com.kgm.common.bundlework.command;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import com.kgm.common.bundlework.imp.BWTechDocImpDialog;
import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aifrcp.AIFUtility;

public class TechDocImportCommand extends AbstractAIFCommand {

	public TechDocImportCommand() {
		/** Dialog »£√‚. */
		Shell shell = AIFUtility.getActiveDesktop().getShell();
		BWTechDocImpDialog partMasterDialog = new BWTechDocImpDialog(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		partMasterDialog.dialogOpen();
	}
}
