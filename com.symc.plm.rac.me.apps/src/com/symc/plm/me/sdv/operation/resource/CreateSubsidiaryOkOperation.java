/**
 * 
 */
package com.symc.plm.me.sdv.operation.resource;

import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Shell;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.common.data.RawDataMap;
import org.sdv.core.common.exception.ValidateSDVException;
import org.sdv.core.ui.UIManager;
import org.sdv.core.ui.operation.AbstractSDVActionOperation;

import com.symc.plm.me.sdv.service.resource.service.create.ResourceCreateService;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.util.MessageBox;

/**
 * Class Name : SDVSampleOkOperation
 * Class Description :
 * 
 * @date 2013. 11. 13.
 * 
 */
public class CreateSubsidiaryOkOperation extends AbstractSDVActionOperation {

    /**
     * @param operationId
     * @param ownerId
     * @param dataSet
     */
    public CreateSubsidiaryOkOperation(int operationId, String ownerId, IDataSet dataSet) {
        super(operationId, ownerId, dataSet);
    }

    public CreateSubsidiaryOkOperation(String operationId, String ownerId, IDataSet dataSet) {
        super(operationId, ownerId, dataSet);
    }

    public CreateSubsidiaryOkOperation(int operationId, String ownerId, Map<String, Object> parameters, IDataSet dataSet) {
        super(operationId, ownerId, parameters, dataSet);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.common.ISDVOperation#executeOperation()
     */
    @Override
    public void executeOperation() throws Exception {
        try {
            IDataSet dataset = getDataSet();
            if (dataset.containsMap("CreateSubsidiaryViewPane")) {
                RawDataMap datamap = null;
                datamap = (RawDataMap) dataset.getDataMap("CreateSubsidiaryViewPane");
                if (datamap != null) {
                	final TCComponentItem  newItem = ResourceCreateService.createResourceItem(datamap);
                	if(newItem != null){
                		this.setExecuteResult(SUCCESS);
                		Dialog currentDialog = (Dialog)UIManager.getCurrentDialog();
                		final Shell dialogShell = currentDialog.getShell();
                		dialogShell.getDisplay().syncExec(new Runnable(){
							@Override
							public void run() {
		                		MessageBox.post(dialogShell, "Created Subsidiary Part : " + newItem.toDisplayString() , "Success Created", MessageBox.INFORMATION);
							}
                		});
                	}
                }
            }
        } catch (ValidateSDVException ve) {
            this.setExecuteResult(FAIL);
            this.setExecuteError(ve);
            this.setErrorMessage(ve.getMessage());
        } catch (Exception e) {
            this.setExecuteResult(FAIL);
            this.setExecuteError(e);
            this.setErrorMessage("오류가 발생했습니다.");
        }
    }

    @Override
    public void startOperation(String commandId) {

    }

    @Override
    public void endOperation() {

    }
}
