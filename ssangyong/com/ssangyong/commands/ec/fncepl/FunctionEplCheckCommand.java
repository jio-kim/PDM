package com.ssangyong.commands.ec.fncepl;

import org.eclipse.swt.widgets.Shell;

import com.ssangyong.commands.ec.fncepl.ui.FncEplCheckManagerDialog;
import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aifrcp.AIFUtility;

/**
 * Function EPL Check
 * @author baek
 *
 */
public class FunctionEplCheckCommand extends AbstractAIFCommand {

	public FunctionEplCheckCommand() {
		Shell shell = AIFUtility.getActiveDesktop().getShell();
		FncEplCheckManagerDialog dialog = new FncEplCheckManagerDialog(shell);
		dialog.open();
	}

}
