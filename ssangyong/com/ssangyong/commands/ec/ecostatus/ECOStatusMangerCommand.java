package com.ssangyong.commands.ec.ecostatus;

import org.eclipse.swt.widgets.Shell;

import com.ssangyong.commands.ec.ecostatus.ui.EcoStatusManagerDialog;
import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aifrcp.AIFUtility;

/**
 * 설계변경현황 관리 
 *
 */
public class ECOStatusMangerCommand extends AbstractAIFCommand {

	public ECOStatusMangerCommand() {
		Shell shell = AIFUtility.getActiveDesktop().getShell();
		EcoStatusManagerDialog dialog = new EcoStatusManagerDialog(shell);
		dialog.open();
	}

}
