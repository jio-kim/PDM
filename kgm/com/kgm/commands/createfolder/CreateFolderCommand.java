package com.kgm.commands.createfolder;

import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aifrcp.AIFUtility;

public class CreateFolderCommand extends AbstractAIFCommand {

	/**
	 * 생성자.
	 * @copyright : S-PALM
	 * @author : 권상기
	 * @since  : 2013. 1. 9.
	 */
	public CreateFolderCommand() {
		CreateFolderDialog dialog = new CreateFolderDialog(AIFUtility.getActiveDesktop());
		dialog.setModal(true);
		setRunnable(dialog);
	}
}
