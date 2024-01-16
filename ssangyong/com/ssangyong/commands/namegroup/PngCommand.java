package com.ssangyong.commands.namegroup;

import com.teamcenter.rac.aif.AbstractAIFCommand;

public class PngCommand extends AbstractAIFCommand {

	@Override
	protected void executeCommand() throws Exception {
		PngDlg dlg = new PngDlg();
		setRunnable(dlg);
		super.executeCommand();
	}

}
