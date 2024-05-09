package com.kgm.commands.partmaster.vehiclepart;

import org.eclipse.swt.widgets.Shell;

import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aifrcp.AIFUtility;

public class UpdateSESSpecNoCommand extends AbstractAIFCommand {


	/**
	 * [SR140324-030][20140619] KOG DEV SES Spec No Update Command
	 * 
	 * Release �Ŀ� ���� �ϹǷ� ���� Object�� ����
	 */
	public UpdateSESSpecNoCommand() {
		/** Dialog ȣ��. */
		Shell shell = AIFUtility.getActiveDesktop().getShell();
		UpdateSESSpecNoDialog partMasterDialog = new UpdateSESSpecNoDialog(shell);
		partMasterDialog.open();
	}
}
