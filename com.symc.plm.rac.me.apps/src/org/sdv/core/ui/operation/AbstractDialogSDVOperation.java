/**
 * 
 */
package org.sdv.core.ui.operation;

import java.util.Map;

/**
 * Class Name : DialogSDVOperation
 * Class Description :
 * 
 * @date 2013. 9. 17.
 * 
 */
public abstract class AbstractDialogSDVOperation extends AbstractSDVOperation {
    public String dialogId;
    
    private Map<String, Object> paramters;
    private Object applicationContext;

    public AbstractDialogSDVOperation(String jobName) {
        super(jobName);
        setDialogId(jobName);
    }


    /* (non-Javadoc)
     * @see org.sdv.core.common.ISDVOperation#setParameter(java.lang.String, java.util.Map, java.lang.Object)
     */
    @Override
    public void setParameter(String commandId, Map<String, Object> paramters, Object applicationContext) {
        this.paramters = paramters;
        this.applicationContext = applicationContext;
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


    /**
     * @return the dialogId
     */
    public final String getDialogId() {
        return dialogId;
    }

    /**
     * @param dialogId
     *            the dialogId to set
     */
    public final void setDialogId(String dialogId) {
        this.dialogId = dialogId;
        setName(dialogId);
    }    
}
