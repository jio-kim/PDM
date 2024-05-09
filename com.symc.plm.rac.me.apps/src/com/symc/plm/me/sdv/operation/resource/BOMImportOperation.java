package com.symc.plm.me.sdv.operation.resource;

import org.sdv.core.common.IDialogOpertation;

import com.kgm.common.bundlework.command.PartBOMImportCommand;
import com.symc.plm.me.sdv.operation.AbstractTCSDVOperation;

public class BOMImportOperation extends AbstractTCSDVOperation implements IDialogOpertation {

    public BOMImportOperation() {
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
        new PartBOMImportCommand();
    }

}
