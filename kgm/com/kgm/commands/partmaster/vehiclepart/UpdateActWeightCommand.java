package com.kgm.commands.partmaster.vehiclepart;

import org.eclipse.swt.widgets.Shell;

import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aifrcp.AIFUtility;

public class UpdateActWeightCommand extends AbstractAIFCommand {


	/**
	 * Actual Weight Update Command
	 * 
	 * Release �Ŀ� ���� �ϹǷ� ���� Object�� ����
	 */
	public UpdateActWeightCommand() {
		/** Dialog ȣ��. */
		Shell shell = AIFUtility.getActiveDesktop().getShell();
		UpdateActWeightDialog partMasterDialog = new UpdateActWeightDialog(shell);
		partMasterDialog.open();
	}
}
