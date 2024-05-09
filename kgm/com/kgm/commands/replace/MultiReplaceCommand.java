package com.kgm.commands.replace;

import com.teamcenter.rac.aif.AbstractAIFCommand;

public class MultiReplaceCommand extends AbstractAIFCommand {

	@Override
	protected void executeCommand() throws Exception {
		MultiReplaceDlg dialog = new MultiReplaceDlg();
		setRunnable(dialog);
		
		super.executeCommand();
	}

}
