package com.ssangyong.commands.vpm.report;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aifrcp.AIFUtility;

public class VPMReportCommand extends AbstractAIFCommand {
	public VPMReportCommand() {
		Shell shell = AIFUtility.getActiveDesktop().getShell();
		VPMReportDialog dialog = new VPMReportDialog(shell, SWT.NONE);
		dialog.open();
	}
}
