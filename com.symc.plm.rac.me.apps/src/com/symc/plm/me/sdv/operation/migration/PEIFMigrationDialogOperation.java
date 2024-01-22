/**
 * 
 */
package com.symc.plm.me.sdv.operation.migration;

import org.sdv.core.common.IDialogOpertation;
import org.sdv.core.common.exception.ValidateSDVException;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.sdv.dialog.migration.PEIFMigrationDialog;
import com.symc.plm.me.sdv.operation.AbstractTCSDVOperation;
import com.symc.plm.me.sdv.validate.LineSelectedSDVValidator;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.kernel.bvr.TCComponentMfgBvrProcess;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.internal.rac.structuremanagement._2011_06.VariantManagement.ModularOption;

/**
 * Class Name : PEIFMigrationDialogOperation
 * Class Description :
 * 
 * @date 2013. 9. 17.
 * 
 */
public class PEIFMigrationDialogOperation extends AbstractTCSDVOperation implements IDialogOpertation {

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
            // Line Selected 체크
            LineSelectedSDVValidator lineSelectedSDVValidator = new LineSelectedSDVValidator();
            lineSelectedSDVValidator.validate(getClass().toString(), null, null);
            InterfaceAIFComponent selectedComponent = AIFUtility.getCurrentApplication().getTargetComponent();
            TCComponentMfgBvrProcess processLine = (TCComponentMfgBvrProcess) selectedComponent;
            ModularOption[] modularOptions = SDVBOPUtilities.getModularOptions(processLine.window().getTopBOMLine());
            if(modularOptions == null || modularOptions.length == 0) {
                throw new Exception("SHOP에 Option이 정의되지 않았습니다.");                
            }
            PEIFMigrationDialog migDialog = new PEIFMigrationDialog(AIFUtility.getActiveDesktop().getShell(), processLine);
            migDialog.open();
        } catch (Exception exception) {
            int status = 0;
            if (exception instanceof ValidateSDVException) {
                status = MessageBox.INFORMATION;
            } else {
                status = MessageBox.ERROR;
            }
            MessageBox.post(AIFDesktop.getActiveDesktop().getShell(), exception.getMessage(), "PE I/F", status);
        }
    }

}
