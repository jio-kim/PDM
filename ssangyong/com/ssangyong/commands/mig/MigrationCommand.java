package com.ssangyong.commands.mig;

import org.eclipse.swt.SWT;

import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aifrcp.AIFUtility;

public class MigrationCommand extends AbstractAIFCommand {

    @Override
    protected void executeCommand() throws Exception {       
        MigrationDialog migDialog = new MigrationDialog(AIFUtility.getActiveDesktop().getShell(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        migDialog.open();
        super.executeCommand();
    }

}
