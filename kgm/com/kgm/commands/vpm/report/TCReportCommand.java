package com.kgm.commands.vpm.report;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aifrcp.AIFUtility;

public class TCReportCommand extends AbstractAIFCommand {
	public TCReportCommand() {
		Shell shell = AIFUtility.getActiveDesktop().getShell();
		TCReportDialog dialog = new TCReportDialog(shell, SWT.NONE);
		dialog.open();
	}
}
