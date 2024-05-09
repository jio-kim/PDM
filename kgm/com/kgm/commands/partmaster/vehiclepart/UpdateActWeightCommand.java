package com.kgm.commands.partmaster.vehiclepart;

import org.eclipse.swt.widgets.Shell;

import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aifrcp.AIFUtility;

public class UpdateActWeightCommand extends AbstractAIFCommand {


	/**
	 * Actual Weight Update Command
	 * 
	 * Release 후에 수정 하므로 별도 Object로 관리
	 */
	public UpdateActWeightCommand() {
		/** Dialog 호출. */
		Shell shell = AIFUtility.getActiveDesktop().getShell();
		UpdateActWeightDialog partMasterDialog = new UpdateActWeightDialog(shell);
		partMasterDialog.open();
	}
}
