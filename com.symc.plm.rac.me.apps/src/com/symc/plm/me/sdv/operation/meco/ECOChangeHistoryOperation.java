package com.symc.plm.me.sdv.operation.meco;

import org.eclipse.swt.widgets.Shell;
import org.sdv.core.common.IDialogOpertation;

import com.symc.plm.me.sdv.dialog.meco.ECOChangeHistoryDialog;
import com.symc.plm.me.sdv.operation.AbstractTCSDVOperation;
import com.teamcenter.rac.aifrcp.AIFUtility;

public class ECOChangeHistoryOperation extends AbstractTCSDVOperation implements IDialogOpertation {

    public ECOChangeHistoryOperation() {

    }

    @Override
    public void startOperation(String commandId) {

    }

    @Override
    public void endOperation() {

    }

    @Override
    public void executeOperation() throws Exception {
        Shell shell = AIFUtility.getActiveDesktop().getShell();

        ECOChangeHistoryDialog ecoChangeDialog = new ECOChangeHistoryDialog(shell);
        ecoChangeDialog.open();
    }

}
