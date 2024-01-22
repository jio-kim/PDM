/**
 *
 */
package com.symc.plm.me.sdv.handler;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.sdv.core.common.IDialogOpertation;
import org.sdv.core.common.ISDVOperation;
import org.sdv.core.common.exception.SDVException;
import org.sdv.core.ui.OperationBeanFactory;
import org.sdv.core.ui.handler.SimpleSDVHandler;

import com.teamcenter.rac.aifrcp.AIFUtility;

/**
 * Class Name : SimpleTCSDVHandler
 * Class Description :
 * @date 2013. 9. 30.
 *
 */
public class UpdateWeldConditionSheetHandler extends SimpleSDVHandler {

    /* (non-Javadoc)
     * @see org.sdv.core.ui.handler.AbstractSDVHandler#beforeExecuteCommand()
     */
    @Override
    public void beforeExecuteCommand(ExecutionEvent event) throws SDVException {

    }

    /* (non-Javadoc)
     * @see org.sdv.core.ui.handler.SimpleSDVHandler#execute()
     */
    @Override
    public void executeCommand(ExecutionEvent event) throws SDVException {
        String commandId = event.getCommand().getId();
        if(StringUtils.isEmpty(commandId)) {
            throw new SDVException("Command ID is null");
        }
        ISDVOperation operation = OperationBeanFactory.getOperator(commandId);
        operation.setParameter(commandId, getParameters(event), event.getApplicationContext());
        try {
            if(operation instanceof IDialogOpertation) {
                operation.startOperation(commandId);
                operation.executeOperation();
                operation.endOperation();
            } else {
                AIFUtility.getDefaultSession().queueOperation((Job)operation);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new SDVException(e.getMessage(), e);
        }
    }

    /* (non-Javadoc)
     * @see org.sdv.core.ui.handler.AbstractSDVHandler#afterExecuteCommand()
     */
    @Override
    public void afterExecuteCommand(ExecutionEvent event) throws SDVException {

    }

}
