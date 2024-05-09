package com.kgm.commands.migration;

import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aifrcp.AIFUtility;

public class MigrationCommand extends AbstractAIFCommand {

	public MigrationCommand() {
		MigrationDialog dialog = new MigrationDialog(this, AIFUtility.getActiveDesktop());
		dialog.setModal(true);
		setRunnable(dialog);
	}
}
