package com.symc.plm.me.sdv.operation.report;

import org.eclipse.swt.widgets.Shell;
import org.sdv.core.common.IDialogOpertation;

import com.symc.plm.me.sdv.dialog.report.CompareEbomVsBOPDialog;
import com.symc.plm.me.sdv.operation.AbstractTCSDVOperation;
import com.teamcenter.rac.aifrcp.AIFUtility;

public class CompareEbomVsBOPOperation extends AbstractTCSDVOperation implements IDialogOpertation {

    public CompareEbomVsBOPOperation() {

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

        CompareEbomVsBOPDialog compareEbomVsBOPDialohg = new CompareEbomVsBOPDialog(shell);
        compareEbomVsBOPDialohg.open();
    }

}
