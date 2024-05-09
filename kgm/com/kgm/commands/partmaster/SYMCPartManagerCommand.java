package com.kgm.commands.partmaster;

import org.eclipse.swt.widgets.Shell;

import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCException;

/**
 * Part Manage Command
 * 
 */
public class SYMCPartManagerCommand extends AbstractAIFCommand
{

	public SYMCPartManagerCommand() throws TCException
	{

		Shell shell = AIFUtility.getActiveDesktop().getShell();

		/** Part Manage Dialog »£√‚. */
		SYMCPartManagerDialog parDialog = new SYMCPartManagerDialog(shell);
		parDialog.setApplyButtonVisible(false);
		parDialog.open();

	}
}
