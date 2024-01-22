package com.symc.plm.me.sdv.operation.resource;

import org.eclipse.swt.SWT;
import org.sdv.core.common.IDialogOpertation;

import com.symc.plm.me.sdv.dialog.resource.ResourceImportDialog;
import com.symc.plm.me.sdv.operation.AbstractTCSDVOperation;
import com.teamcenter.rac.aifrcp.AIFUtility;

public class ResourceImportOperation extends AbstractTCSDVOperation implements IDialogOpertation {

    public ResourceImportOperation() {
    }

    /* (non-Javadoc)
     * @see org.sdv.core.common.ISDVOperation#startOperation(java.lang.String)
     */
    @Override
    public void startOperation(String commandId) {
        
    }

    /* (non-Javadoc)
     * @see org.sdv.core.common.ISDVOperation#endOperation()
     */
    @Override
    public void endOperation() {
        
    }

    /* (non-Javadoc)
     * @see com.teamcenter.rac.aif.AbstractAIFOperation#executeOperation()
     */
    @Override
    public void executeOperation() throws Exception {
        ResourceImportDialog resourcesDialog = new ResourceImportDialog(AIFUtility.getActiveDesktop().getShell(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        resourcesDialog.dialogOpen();                
    }
}
