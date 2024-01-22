package com.ssangyong.commands.ifpe;

import com.teamcenter.rac.aif.AbstractAIFCommand;

public class TcToPeInterfaceCommand extends AbstractAIFCommand {

	@Override
	protected void executeCommand() throws Exception {
		
		TcToPeInterfaceDialog dialog = new TcToPeInterfaceDialog();
		setRunnable(dialog);
//		dialog.setVisible(true);
		super.executeCommand();
	}
	
}
