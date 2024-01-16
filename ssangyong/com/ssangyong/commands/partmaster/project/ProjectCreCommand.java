package com.ssangyong.commands.partmaster.project;

import java.util.HashMap;

import org.eclipse.swt.widgets.Shell;

import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aifrcp.AIFUtility;

/**
 * Project Creation Command
 */
public class ProjectCreCommand extends AbstractAIFCommand {

	public ProjectCreCommand() {
		/** Dialog »£√‚. */
		Shell shell = AIFUtility.getActiveDesktop().getShell();
		ProjectCreDialog parDialog = new ProjectCreDialog(shell, new HashMap<String,Object>());
		parDialog.setApplyButtonVisible(false);
		parDialog.open();
	}
}
