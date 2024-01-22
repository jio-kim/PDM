/**
 * 
 */
package com.symc.plm.me.sdv.operation.resource;

import java.util.Map;

import org.sdv.core.common.data.IDataSet;
import org.sdv.core.ui.operation.AbstractSDVActionOperation;

import com.symc.plm.me.sdv.service.resource.ResourceUtilities;

/**
 * Class Name : SDVSampleOkOperation
 * Class Description :
 * 
 * @date 2013. 11. 13.
 * 
 */
public class BodyCreateToolOkOperation extends AbstractSDVActionOperation {
    /**
     * @param operationId
     * @param ownerId
     * @param dataSet
     */
    public BodyCreateToolOkOperation(int operationId, String ownerId, IDataSet dataSet) {
        super(operationId, ownerId, dataSet);
    }

    public BodyCreateToolOkOperation(String operationId, String ownerId, IDataSet dataSet) {
        super(operationId, ownerId, dataSet);
    }

    public BodyCreateToolOkOperation(String operationId, String ownerId, Map<String, Object> parameters, IDataSet dataSet) {
        super(operationId, ownerId, parameters, dataSet);
    }
    
    public BodyCreateToolOkOperation(int operationId, String ownerId, Map<String, Object> parameters, IDataSet dataSet) {
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
