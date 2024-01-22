/**
 * 
 */
package com.symc.plm.me.sdv.operation.sdvsample;

import java.util.Map;

import org.sdv.core.common.data.IDataSet;
import org.sdv.core.ui.operation.AbstractSDVActionOperation;

import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.util.MessageBox;

/**
 * Class Name : SDVSampleOkOperation
 * Class Description : 
 * @date 2013. 11. 13.
 *
 */
public class SDVSampleOkOperation extends AbstractSDVActionOperation {

    /**
     * @param operationId
     * @param ownerId
     * @param dataSet
     */
    public SDVSampleOkOperation(int operationId, String ownerId, IDataSet dataSet) {
        super(operationId, ownerId, dataSet);
    }
    
    public SDVSampleOkOperation(String operationId, String ownerId, IDataSet dataSet) {
        super(operationId, ownerId, dataSet);
    }
    
    public SDVSampleOkOperation(String operationId, String ownerId,  Map<String, Object> parameters, IDataSet dataSet) {
        super(operationId, ownerId, parameters, dataSet);
    }

    public SDVSampleOkOperation(int buttonId, String ownerId,  Map<String, Object> parameters, IDataSet dataSet) {
        super(buttonId, ownerId, parameters, dataSet);
    }

    /* (non-Javadoc)
     * @see org.sdv.core.common.ISDVOperation#executeOperation()
     */
    @Override
    public void executeOperation() throws Exception {
        System.out.println("[OWNER:" + getOwnerId() + " ] OperatinId=" + getOperationId() + ",  SDVSampleOkOperation executeOperation() method is called");
        AIFDesktop.getActiveDesktop().getShell().getDisplay().asyncExec(new Runnable(){
            @Override
            public void run() {
                MessageBox.post("[OWNER:" + getOwnerId() + " ] OperatinId=" + getOperationId() + "\nSDVSampleOkOperation executeOperation() method is called", getOperationId(), MessageBox.INFORMATION);
            }
            
        });
    }

    /* (non-Javadoc)
     * @see org.sdv.core.common.ISDVOperation#startOperation(java.lang.String)
     */
    @Override
    public void startOperation(String commandId) {
        System.out.println( "[OWNER:" + getOwnerId() + " ] OperatinId=" + getOperationId() + ",  SDVSampleOkOperation startOperation() method is called");
    }

    /* (non-Javadoc)
     * @see org.sdv.core.common.ISDVOperation#endOperation()
     */
    @Override
    public void endOperation() {
        System.out.println( "[OWNER:" + getOwnerId() + " ] OperatinId=" + getOperationId() + ",  SDVSampleOkOperation endOperation() method is called");
    }

}
