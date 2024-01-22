/**
 * 
 */
package org.sdv.core.ui.handler;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.sdv.core.common.IDialogOpertation;
import org.sdv.core.common.ISDVOperation;
import org.sdv.core.common.ISDVValidator;
import org.sdv.core.common.exception.SDVException;
import org.sdv.core.ui.OperationBeanFactory;

import common.Logger;

/**
 * Class Name : DefaultHandler
 * Class Description : 
 * @date 2013. 9. 30.
 *
 */
public class SimpleSDVHandler extends AbstractSDVHandler {

	private static final Logger logger = Logger.getLogger(SimpleSDVHandler.class);
	
    /* (non-Javadoc)
     * @see org.sdv.core.ui.handler.AbstractSDVHandler#beforeExecuteCommand()
     */
    @Override
    public void beforeExecuteCommand(ExecutionEvent event) throws SDVException {
        

    }
    
    @Override
    public void validateCommand(ExecutionEvent event) throws SDVException{
    	String commandId = event.getCommand().getId();
        
        try{
        	if(StringUtils.isEmpty(commandId)){
        		 throw new SDVException("Command ID is empty.");
            }
        	ISDVOperation operation = OperationBeanFactory.getOperator(commandId);
        	if(operation == null){
        		throw new SDVException("Command[" + commandId + "] is not supported yet.");
        	}
        	
        	List<ISDVValidator> validators = operation.getValidators();
        	if(validators != null){
        		for(ISDVValidator validator : validators){
        			validator.validate(commandId, getParameters(event), event.getApplicationContext());
        		}
        	}
        	
        }catch(SDVException sex){
        	throw sex;
        }catch(Throwable th){
        	logger.error(th);
        	throw new SDVException(th.getMessage());
        }
    }

    /* (non-Javadoc)
     * @see org.sdv.core.ui.handler.AbstractSDVHandler#execute()
     */
    @Override
    public void executeCommand(ExecutionEvent event) throws SDVException {
        String commandId = event.getCommand().getId();
        if(StringUtils.isEmpty(commandId)) {
            throw new SDVException("Command ID is null");
        }
        ISDVOperation operation = OperationBeanFactory.getOperator(commandId);
        operation.setParameter(commandId, getParameters(event), event.getApplicationContext());
        if(operation instanceof IDialogOpertation) {
            operation.startOperation(commandId);
            try {
                operation.executeOperation();
            } catch (Exception e) {
                e.printStackTrace();
                throw new SDVException(e.getMessage(), e);
            }
            operation.endOperation();
        } else {
            Job job = (Job)operation;
            job.setPriority(Job.SHORT);
            job.schedule();
            
        }
    }



    /* (non-Javadoc)
     * @see org.sdv.core.ui.handler.AbstractSDVHandler#afterExecuteCommand()
     */
    @Override
    public void afterExecuteCommand(ExecutionEvent event) throws SDVException {
        

    }

}
