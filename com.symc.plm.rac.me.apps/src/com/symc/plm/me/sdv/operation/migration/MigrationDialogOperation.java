/**
 * 
 */
package com.symc.plm.me.sdv.operation.migration;

import org.sdv.core.common.IDialogOpertation;

import com.symc.plm.me.sdv.dialog.migration.MigrationDialog;
import com.symc.plm.me.sdv.operation.AbstractTCSDVOperation;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.util.MessageBox;

/**
 * Class Name : AbstractTCSDVExecuteOperation
 * Class Description :
 * 
 * @date 2013. 9. 17.
 * 
 */
public class MigrationDialogOperation extends AbstractTCSDVOperation implements IDialogOpertation {

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.common.ISDVOperation#startOperation(java.lang.String)
     */
    @Override
    public void startOperation(String commandId) {

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.common.ISDVOperation#endOperation()
     */
    @Override
    public void endOperation() {

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.teamcenter.rac.aif.AbstractAIFOperation#executeOperation()
     */
    @Override
    public void executeOperation() throws Exception {
        try {
            MigrationDialog migDialog = new MigrationDialog(AIFUtility.getActiveDesktop().getShell());
            migDialog.open();
        } catch (Exception exception) {
            MessageBox messagebox = new MessageBox(AIFDesktop.getActiveDesktop(), exception);
            messagebox.setModal(true);
            messagebox.setVisible(true);
        }

    }

}
