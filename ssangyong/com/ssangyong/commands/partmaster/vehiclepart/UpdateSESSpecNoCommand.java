package com.ssangyong.commands.partmaster.vehiclepart;

import org.eclipse.swt.widgets.Shell;

import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aifrcp.AIFUtility;

public class UpdateSESSpecNoCommand extends AbstractAIFCommand {


	/**
	 * [SR140324-030][20140619] KOG DEV SES Spec No Update Command
	 * 
	 * Release 후에 수정 하므로 별도 Object로 관리
	 */
	public UpdateSESSpecNoCommand() {
		/** Dialog 호출. */
		Shell shell = AIFUtility.getActiveDesktop().getShell();
		UpdateSESSpecNoDialog partMasterDialog = new UpdateSESSpecNoDialog(shell);
		partMasterDialog.open();
	}
}
