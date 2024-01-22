/**
 * 
 */
package com.symc.plm.me.sdv.operation.migration;

import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.sdv.core.common.IDialogOpertation;
import org.sdv.core.common.exception.ValidateSDVException;
import org.springframework.util.StringUtils;

import com.symc.plm.me.sdv.operation.AbstractTCSDVOperation;
import com.symc.plm.me.sdv.service.migration.work.export.ui.dialog.ExportProgressDialog;
import com.symc.plm.me.sdv.validate.LineSelectedSDVValidator;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.kernel.bvr.TCComponentMfgBvrProcess;
import com.teamcenter.rac.util.MessageBox;

/**
 * Class Name : BOPExcelExportOperation
 * Class Description :
 * 
 * @date 2013. 9. 17.
 * 
 */
public class BOPExcelExportOperation extends AbstractTCSDVOperation implements IDialogOpertation {

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
            // Line Selected üũ
            LineSelectedSDVValidator lineSelectedSDVValidator = new LineSelectedSDVValidator();
            lineSelectedSDVValidator.validate(getClass().toString(), null, null);
            InterfaceAIFComponent selectedComponent = AIFUtility.getCurrentApplication().getTargetComponent();
            TCComponentMfgBvrProcess processLine = (TCComponentMfgBvrProcess) selectedComponent;
            String folderPath = filePeIfOpen();
            if(StringUtils.isEmpty(folderPath)) {                 
                throw new ValidateSDVException("Please select the folder.");
            }             
            ExportProgressDialog dpb = new ExportProgressDialog(Display.getDefault().getActiveShell(), processLine, folderPath);
            dpb.initGuage();
            dpb.open();
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
    
    public String filePeIfOpen() throws Exception {
        DirectoryDialog directoryDialog = new DirectoryDialog(Display.getDefault().getActiveShell());        
        directoryDialog.setMessage("Please select a directory and click OK");
        return directoryDialog.open();
    }

}
