package com.kgm.commands.ec.monitor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aifrcp.AIFUtility;

public class MonitoringECOCommand extends AbstractAIFCommand {
	public MonitoringECOCommand() {
		Shell shell = AIFUtility.getActiveDesktop().getShell();
		/**
		 * [SR141106-036][20141117][jclee] Monitoring ECO 최소화 기능 추가
		 */
//		MonitoringECODialog dialog = new MonitoringECODialog(shell, SWT.NONE);
		MonitoringECODialog dialog = new MonitoringECODialog(shell, SWT.DIALOG_TRIM | SWT.MIN);
		dialog.open();
	}
}
