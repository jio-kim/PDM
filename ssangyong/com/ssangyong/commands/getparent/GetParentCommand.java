package com.ssangyong.commands.getparent;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aifrcp.AIFUtility;

public class GetParentCommand extends AbstractAIFCommand {
	public GetParentCommand() {
		Shell shell = AIFUtility.getActiveDesktop().getShell();
		GetParentDialog dialog = new GetParentDialog(shell, SWT.DIALOG_TRIM | SWT.MIN);
		dialog.open();
	}
}
