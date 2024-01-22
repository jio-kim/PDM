/**
 * 
 */
package com.symc.plm.me.sdv.operation;


import java.util.List;
import java.util.Map;

import org.sdv.core.common.ISDVOperation;
import org.sdv.core.common.ISDVValidator;

import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aif.InterfaceAIFOperationListener;

/**
 * Class Name : AbstractTCSDVExecuteOperation
 * Class Description :
 * 
 * @date 2013. 9. 17.
 * 
 */
public abstract class AbstractTCSDVOperation extends AbstractAIFOperation implements ISDVOperation, InterfaceAIFOperationListener {
    
    private String operationId;
    private String commandId;
    private Map<String, Object> paramters;
    private Object applicationContext;
    
    private List<ISDVValidator>  validators;
    
    public AbstractTCSDVOperation() {
        this.addOperationListener(this);
    }
    
    /**
     * @return the operationId
     */
    public String getOperationId() {
        return operationId;
    }

    /**
     * @param operationId the operationId to set
     */
    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    /**
     * @return the commandId
     */
    public String getCommandId() {
        return commandId;
    }

    /**
     * @return the paramters
     */
    public Map<String, Object> getParamters() {
        return paramters;
    }

    /**
     * @return the applicationContext
     */
    public Object getApplicationContext() {
        return applicationContext;
    }

    public void setParameter(String commandId, Map<String, Object> paramters, Object applicationContext){
        this.commandId = commandId;
        this.paramters = paramters;
        this.applicationContext = applicationContext;
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
