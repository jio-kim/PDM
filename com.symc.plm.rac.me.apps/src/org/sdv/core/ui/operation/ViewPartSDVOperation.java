/**
 * 
 */
package org.sdv.core.ui.operation;

import org.sdv.core.common.ISDVOperation;

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.util.MessageBox;

/**
 * Class Name : ViewPartSDVOperation
 * Class Description :
 * 
 * @date 2013. 9. 17.
 * 
 */
public abstract class ViewPartSDVOperation extends AbstractSDVOperation implements ISDVOperation {
    public String viewPartId;
    
    public ViewPartSDVOperation(String jobName) {
        super(jobName);
    }
    
    @Override
    public void executeOperation() throws Exception {
        try {
            beforeExecuteOperation();
            executeViewPartOperation();
            afterExecuteOperation();
        } catch (Exception e) {
            e.printStackTrace();
            MessageBox.post(AIFUtility.getActiveDesktop().getShell(), e.toString(), "ERROR", MessageBox.ERROR);
        }
    }

    /**
     * @return the viewPartId
     */
    public final String getViewPartId() {
        return viewPartId;
    }

    /**
     * @param viewPartId
     *            the viewPartId to set
     */
    public final void setViewPartId(String viewPartId) {
        this.viewPartId = viewPartId;
    }

    abstract public void beforeExecuteOperation() throws Exception;

    abstract public void executeViewPartOperation() throws Exception;

    abstract public void afterExecuteOperation() throws Exception;
}
