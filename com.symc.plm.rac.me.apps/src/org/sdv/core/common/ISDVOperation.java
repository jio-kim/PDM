/**
 * 
 */
package org.sdv.core.common;

import java.util.List;
import java.util.Map;

/**
 * Class Name : ISDVOperation
 * Class Description :
 * 
 * @date 2013. 9. 24.
 * 
 */
public interface ISDVOperation {
    public void executeOperation() throws Exception;

    public void startOperation(String commandId);

    public void endOperation();
    
    public void setParameter(String commandId, Map<String, Object> paramters, Object applicationContext);
    
    public String getOperationId();
    
    public void setOperationId(String operationId);
    
	public List<ISDVValidator> getValidators();
}
