package com.ssangyong.commands.optiondefine;

import org.eclipse.swt.SWT;

import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aifrcp.AIFUtility;

public class VariantOptionCombinationCommand extends AbstractAIFCommand {

    @Override
    protected void executeCommand() throws Exception {       
        VariantOptionCombinationDialog importDialog = new VariantOptionCombinationDialog(AIFUtility.getActiveDesktop().getShell(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        importDialog.open();
        super.executeCommand();
    }

}
