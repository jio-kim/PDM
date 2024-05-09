package com.kgm.commands.standardpart;


import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.AbstractAIFCommand;

public class StandardPartImportCommand extends AbstractAIFCommand {

	public StandardPartImportCommand() {
		/** Dialog »£√‚. */
		StandardPartImportDialog dialog = new StandardPartImportDialog(AIFDesktop.getActiveDesktop().getShell());
		dialog.open();
	}
}
