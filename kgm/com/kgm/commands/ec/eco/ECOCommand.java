package com.kgm.commands.ec.eco;

import org.eclipse.swt.widgets.Shell;

import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aifrcp.AIFUtility;

public class ECOCommand extends AbstractAIFCommand {
	
	public ECOCommand() {
		Shell shell = AIFUtility.getActiveDesktop().getShell();
		ECOSWTDialog dialog = new ECOSWTDialog(shell);
		dialog.open();
	}
}
