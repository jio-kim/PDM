/**
 * 
 */
package org.sdv.core.ui.operation;

import org.sdv.core.common.ISDVOperation;

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.util.MessageBox;

/**
 * Class Name : ExecuteSDVOperation
 * Class Description :
 * 
 * @date 2013. 9. 17.
 * 
 */
public abstract class ExecuteSDVOperation extends AbstractSDVOperation implements ISDVOperation {

    public String operationId;

    public ExecuteSDVOperation(String jobName) {
        super(jobName);
    }

    @Override
    public void executeOperation() throws Exception {
        try {
            beforeExecuteOperation();
            executeRunOperation();
            afterExecuteOperation();
        } catch (Exception e) {
            e.printStackTrace();
            MessageBox.post(AIFUtility.getActiveDesktop().getShell(), e.toString(), "ERROR", MessageBox.ERROR);
        }
    }

    /**
     * @return the operationId
     */
    public final String getOperationId() {
        return operationId;
    }

    /**
     * @param operationId
     *            the operationId to set
     */
    public final void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    abstract public void beforeExecuteOperation() throws Exception;

    abstract public void executeRunOperation() throws Exception;

    abstract public void afterExecuteOperation() throws Exception;

}
