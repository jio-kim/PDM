package com.ssangyong.commands.ec.history;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aifrcp.AIFUtility;

public class EPLCorrectionHistoryCommand extends AbstractAIFCommand {
	public EPLCorrectionHistoryCommand() {
		Shell shell = AIFUtility.getActiveDesktop().getShell();
		EPLCorrectionHistoryDialog dialog = new EPLCorrectionHistoryDialog(shell);
		dialog.open();
	}
}
