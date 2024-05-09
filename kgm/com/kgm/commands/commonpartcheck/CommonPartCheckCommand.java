package com.kgm.commands.commonpartcheck;

import com.teamcenter.rac.aif.AbstractAIFCommand;

public class CommonPartCheckCommand extends AbstractAIFCommand {

	public CommonPartCheckCommand(){
		try {
			CommonPartCheckDialog dialog = new CommonPartCheckDialog();
//			dialog.setModal(true);
			dialog.pack();
			setRunnable(dialog);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}