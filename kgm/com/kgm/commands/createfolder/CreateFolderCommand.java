package com.kgm.commands.createfolder;

import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aifrcp.AIFUtility;

public class CreateFolderCommand extends AbstractAIFCommand {

	/**
	 * ������.
	 * @copyright : S-PALM
	 * @author : �ǻ��
	 * @since  : 2013. 1. 9.
	 */
	public CreateFolderCommand() {
		CreateFolderDialog dialog = new CreateFolderDialog(AIFUtility.getActiveDesktop());
		dialog.setModal(true);
		setRunnable(dialog);
	}
}
