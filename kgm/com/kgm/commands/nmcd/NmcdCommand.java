package com.kgm.commands.nmcd;

import com.teamcenter.rac.aif.AbstractAIFCommand;

public class NmcdCommand extends AbstractAIFCommand {

	public NmcdCommand(){
		try {
			NmcdDialog dialog = new NmcdDialog();
//			dialog.setModal(true);
			dialog.pack();
			setRunnable(dialog);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}