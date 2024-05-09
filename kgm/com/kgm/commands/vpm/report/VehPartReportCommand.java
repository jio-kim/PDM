package com.kgm.commands.vpm.report;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aifrcp.AIFUtility;

public class VehPartReportCommand extends AbstractAIFCommand {
	public VehPartReportCommand() {
		Shell shell = AIFUtility.getActiveDesktop().getShell();
		VehPartReportDialog dialog = new VehPartReportDialog(shell, SWT.NONE);
		dialog.open();
	}
}
