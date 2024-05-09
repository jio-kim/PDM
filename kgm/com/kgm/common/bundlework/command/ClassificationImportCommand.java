package com.kgm.common.bundlework.command;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import com.kgm.common.bundlework.imp.BWClassificationImpDialog;
import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aifrcp.AIFUtility;

public class ClassificationImportCommand extends AbstractAIFCommand {

	public ClassificationImportCommand() {
		/** Dialog »£√‚. */
		Shell shell = AIFUtility.getActiveDesktop().getShell();
		BWClassificationImpDialog classificationDialog = new BWClassificationImpDialog(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		classificationDialog.dialogOpen();
	}
}
