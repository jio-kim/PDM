/**
 * 
 */
package com.symc.plm.rac.prebom.migprebom.commands;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import com.symc.plm.rac.prebom.migprebom.dialog.BWPreFunctionImpDialog;
import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aifrcp.AIFUtility;

/**
 * @author JWLEE
 * 
 */
public class ImportPreFunctionCommand extends AbstractAIFCommand {

    @Override
    protected void executeCommand() throws Exception {
        Shell shell = AIFUtility.getActiveDesktop().getShell();
        BWPreFunctionImpDialog dialog = new BWPreFunctionImpDialog(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        dialog.dialogOpen();
    }
}
