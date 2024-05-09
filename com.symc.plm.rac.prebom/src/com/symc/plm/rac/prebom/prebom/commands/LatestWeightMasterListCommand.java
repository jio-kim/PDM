package com.symc.plm.rac.prebom.prebom.commands;

import com.kgm.common.utils.CustomUtil;
import com.symc.plm.rac.prebom.prebom.dialog.weightmasterlist.LatestWeightMasterListDialog;
import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aifrcp.AIFUtility;

public class LatestWeightMasterListCommand extends AbstractAIFCommand {

	@Override
	protected void executeCommand() throws Exception {

		AbstractAIFOperation runDialog = new AbstractAIFOperation() {
			@Override
			public void executeOperation() throws Exception {
				LatestWeightMasterListDialog weightDialog = new LatestWeightMasterListDialog(AIFUtility.getActiveDesktop().getFrame(), CustomUtil.getTCSession());
				weightDialog.setVisible(true);
			}
		};
		CustomUtil.getTCSession().queueOperation(runDialog);
	}
}