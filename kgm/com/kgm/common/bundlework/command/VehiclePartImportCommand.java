package com.kgm.common.bundlework.command;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import com.kgm.common.bundlework.imp.BWVehiclePartImpDialog;
import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aifrcp.AIFUtility;

public class VehiclePartImportCommand extends AbstractAIFCommand {

	public VehiclePartImportCommand() {
		/** Dialog »£√‚. */
		Shell shell = AIFUtility.getActiveDesktop().getShell();
		BWVehiclePartImpDialog partMasterDialog = new BWVehiclePartImpDialog(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		partMasterDialog.dialogOpen();
	}
}
