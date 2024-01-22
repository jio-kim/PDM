package org.sdv.core.ui.handler;

import java.awt.Frame;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.sdv.core.common.ISDVHandler;
import org.sdv.core.common.exception.SDVException;

import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.util.MessageBox;

/**
 * 
 * Class Name : AbstractSDVCommonHandler
 * Class Description :
 * 
 * @date 2013. 9. 24.
 * 
 */
public abstract class AbstractSDVHandler extends AbstractHandler implements ISDVHandler {
    
    private static final Logger logger = Logger.getLogger(AbstractSDVHandler.class);

    public static final String DEFAULT_PARAMETER_COMMANDID = "COMMAND_ID";
    
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        try {
            beforeExecuteCommand(event);
            validateCommand(event);
            executeCommand(event);
            afterExecuteCommand(event);
        } catch (Throwable e) {
            showMessage(e);
        }
        return null;
    }

    public void validateCommand(ExecutionEvent event) throws SDVException {
//        String commandId = event.getCommand().getId();
//        Map<String, Object>   parameters = event.getParameters();
//        Object applicationContext = event.getApplicationContext();
        
    }
    
    protected void showMessage(Throwable throwable) {
        
        Frame parentFrame = AIFDesktop.getActiveDesktop();
        logger.error(throwable.getClass().getName(), throwable);
        MessageBox messagebox = new MessageBox(parentFrame, throwable);
        messagebox.setModal(true);
        messagebox.setVisible(true);
    }
    
    /**
     * 
     * @method getParameters 
     * @date 2013. 11. 19.
     * @param
     * @return Map
     * @exception
     * @throws
     * @see
     */
    protected Map<String, Object> getParameters(ExecutionEvent event) {
        String commandId = event.getCommand().getId();
        Map<String, Object> clonedParameters = new HashMap<String, Object>();
        
        @SuppressWarnings("rawtypes")
        Map eventParameters = event.getParameters();
        if(eventParameters != null){
            for(Object key : eventParameters.keySet()){
                clonedParameters.put(key.toString(), eventParameters.get(key));
            }
        }
        clonedParameters.put(DEFAULT_PARAMETER_COMMANDID, commandId);
        
        return clonedParameters;
    }    

    abstract public void beforeExecuteCommand(ExecutionEvent event) throws SDVException;

    abstract public void executeCommand(ExecutionEvent event) throws SDVException;

    abstract public void afterExecuteCommand(ExecutionEvent event) throws SDVException;
}
