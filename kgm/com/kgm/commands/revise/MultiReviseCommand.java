package com.kgm.commands.revise;

import com.teamcenter.rac.aif.AbstractAIFCommand;

public class MultiReviseCommand extends AbstractAIFCommand {

	@Override
	protected void executeCommand() throws Exception {
		MultiReviseDlg dialog = new MultiReviseDlg();
		setRunnable(dialog);
		
		super.executeCommand();
	}
}
