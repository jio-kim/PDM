package com.kgm.common.bundlework.command;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import com.kgm.common.bundlework.imp.BWPartAllReleaseDialog;
import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aifrcp.AIFUtility;

public class PartAllReleaseDBCommand extends AbstractAIFCommand {

	public PartAllReleaseDBCommand() {
		/** Dialog ȣ��. */
		Shell shell = AIFUtility.getActiveDesktop().getShell();
		//BWPartImpDialog partMasterDialog = new BWPartImpDialog(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		BWPartAllReleaseDialog partMasterDialog = new BWPartAllReleaseDialog(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		partMasterDialog.dialogOpen(); 
	}  
}
