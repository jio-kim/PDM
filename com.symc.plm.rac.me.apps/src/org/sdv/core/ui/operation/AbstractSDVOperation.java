/**
 * 
 */
package org.sdv.core.ui.operation;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.sdv.core.common.ISDVOperation;
import org.sdv.core.common.ISDVValidator;

/**
 * Class Name : AbstractSDVOperation
 * Class Description : 
 * @date 2013. 10. 15.
 *
 */
public abstract class AbstractSDVOperation extends Job implements ISDVOperation {

    private String operationId ;
    private List<ISDVValidator>  validators;
    /**
     * @param name
     */
    public AbstractSDVOperation(String operationId) {
        super(operationId);
        this.operationId = operationId;
    }

    /* (non-Javadoc)
     * @see org.sdv.core.common.ISDVOperation#getOperationId()
     */
    @Override
    public String getOperationId() {
        return this.operationId;
    }

    /* (non-Javadoc)
     * @see org.sdv.core.common.ISDVOperation#setOperationId(java.lang.String)
     */
    @Override
    public void setOperationId(String operationId) {
        this.operationId = operationId;
        
    }      
    
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    protected IStatus run(IProgressMonitor arg0) {
        try {
            startOperation(getName());
            executeOperation();
            endOperation();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Status.OK_STATUS;
    }

	/**
	 * @return the validators
	 */
	public List<ISDVValidator> getValidators() {
		return validators;
	}

	/**
	 * @param validators the validators to set
	 */
	public void setValidators(List<ISDVValidator> validators) {
		this.validators = validators;
	}    
    
}
