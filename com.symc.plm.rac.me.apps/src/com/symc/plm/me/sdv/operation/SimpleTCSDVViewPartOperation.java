package com.symc.plm.me.sdv.operation;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.sdv.core.common.IDialogOpertation;

import com.teamcenter.rac.aifrcp.AIFUtility;

public class SimpleTCSDVViewPartOperation extends AbstractTCSDVOperation implements IDialogOpertation{

    String viewPartId;
    
    
    public SimpleTCSDVViewPartOperation() throws PartInitException {
    }

    @Override
    public void startOperation(String commandId) {

    }

    @Override
    public void endOperation() {

    }

    @Override
    public void executeOperation() throws Exception {

        IWorkbenchWindow workbenchWindow = AIFUtility.getActiveDesktop().getDesktopWindow();
        IWorkbenchPage page = workbenchWindow.getActivePage();
        page.showView(getViewPartId());
    }

    public String getViewPartId() {
        return viewPartId;
    }

    public void setViewPartId(String viewPartId) {
        this.viewPartId = viewPartId;
    }

    
}
