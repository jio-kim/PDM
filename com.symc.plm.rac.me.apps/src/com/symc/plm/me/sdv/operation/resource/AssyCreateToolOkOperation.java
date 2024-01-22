/**
 * 
 */
package com.symc.plm.me.sdv.operation.resource;

import java.util.Map;

import org.sdv.core.common.data.IDataSet;
import org.sdv.core.ui.operation.AbstractSDVActionOperation;

import com.symc.plm.me.sdv.service.resource.ResourceUtilities;
import com.teamcenter.rac.util.Registry;

/**
 * Class Name : SDVSampleOkOperation
 * Class Description :
 * 
 * @date 2013. 11. 13.
 * 
 */
public class AssyCreateToolOkOperation extends AbstractSDVActionOperation {

    Registry registry; 
    /**
     * @param operationId
     * @param ownerId
     * @param dataSet
     */
    public AssyCreateToolOkOperation(int operationId, String ownerId, IDataSet dataSet) {
        super(operationId, ownerId, dataSet);
        registry = Registry.getRegistry(this);
    }

    public AssyCreateToolOkOperation(String operationId, String ownerId, IDataSet dataSet) {
        super(operationId, ownerId, dataSet);
        registry = Registry.getRegistry(this);
    }

    public AssyCreateToolOkOperation(String operationId, String ownerId, Map<String, Object> parameters, IDataSet dataSet) {
        super(operationId, ownerId, parameters, dataSet);
        registry = Registry.getRegistry(this);
    }
    
    public AssyCreateToolOkOperation(int operationId, String ownerId, Map<String, Object> parameters, IDataSet dataSet) {
        super(operationId, ownerId, parameters, dataSet);
        registry = Registry.getRegistry(this);
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
            ResourceUtilities.excuteResourceCreateService(dataset);
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
