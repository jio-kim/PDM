package com.symc.plm.rac.prebom.masterlist.commands;

import com.symc.plm.rac.prebom.masterlist.dialog.AllPreBOMUssageExportDialog;
import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aifrcp.AIFUtility;

public class AllPreBOMUssageExportCommand extends AbstractAIFCommand {

	private AllPreBOMUssageExportDialog allPreBOMUssageExportDialog;
	
	@Override
	protected void executeCommand() throws Exception {

		targetSelectionDialogOpen();
	}
	
	private void targetSelectionDialogOpen() throws Exception{
		allPreBOMUssageExportDialog = new AllPreBOMUssageExportDialog(AIFUtility.getActiveDesktop().getShell());
		allPreBOMUssageExportDialog.open();
	}

}
