package com.kgm.commands.partmaster;

import org.eclipse.swt.widgets.Shell;

import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aifrcp.AIFUtility;

/**
 * Product Manage Command
 * 
 */
public class SYMCProductManagerCommand extends AbstractAIFCommand {

	public SYMCProductManagerCommand() {
		/** Product Dialog »£√‚. */
		Shell shell = AIFUtility.getActiveDesktop().getShell();
		SYMCProductManagerDialog parDialog = new SYMCProductManagerDialog(shell);
		parDialog.setApplyButtonVisible(false);
		parDialog.open();
	}
}
