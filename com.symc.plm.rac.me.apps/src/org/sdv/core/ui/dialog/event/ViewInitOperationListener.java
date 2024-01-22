/**
 * 
 */
package org.sdv.core.ui.dialog.event;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.window.IShellProvider;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.ui.operation.AbstractSDVInitOperation;

import com.teamcenter.rac.aif.InterfaceAIFOperationExecutionListener;
import com.teamcenter.rac.aif.InterfaceAIFOperationListener;
import com.teamcenter.rac.kernel.TCSession;
import common.Logger;

/**
 * Class Name : ViewInitOperationListener
 * Class Description : 
 * @date 	2013. 11. 28.
 * @author  CS.Park
 * 
 */
public class ViewInitOperationListener implements InterfaceAIFOperationListener, InterfaceAIFOperationExecutionListener{
    
    private static final Logger logger = Logger.getLogger(ViewInitOperationListener.class);
    
    private AbstractSDVInitOperation operation;
    private List<ISDVInitOperationListener> initListners;
    private String targetId;
    private IShellProvider source;
    
    public ViewInitOperationListener(IShellProvider source, String targetId, AbstractSDVInitOperation operation){
      this.source = source;
      this.targetId = targetId;
      this.operation = operation;
      initListners = new ArrayList<ISDVInitOperationListener>();
    }

    public void addInitListener(ISDVInitOperationListener initListner){
        this.initListners.add(initListner);
    }
    
    public void removeInitListener(ISDVInitOperationListener initListner){
        this.initListners.remove(initListner);
    }
    
    @Override
    public void endOperation() {
        for(ISDVInitOperationListener initListner : initListners){
            final ISDVInitOperationListener inRunnerLstnr = initListner;
            final IDataSet dataSet = operation.getData();
            final TCSession session = (TCSession)operation.getSession();
            source.getShell().getDisplay().syncExec(new Runnable(){
                @Override
                public void run() {
                    try{
                        inRunnerLstnr.willInitalize(new SDVInitEvent(source, SDVInitEvent.INIT_SUCCESS, targetId, dataSet));
                    }catch(Exception ex){
                        logger.error(ex);
                    }
                    session.setReadyStatus();
                }
            });
        }
    }

    @Override
    public void startOperation(String statusMessage){
        operation.getSession().setStatus(statusMessage);
    }

    /**
     * Description :
     * @method :
     * @date : 2013. 11. 29.
     * @author : CS.Park
     * @param :
     * @return : 
     * @see com.teamcenter.rac.aif.InterfaceAIFOperationExecutionListener#exceptionThrown(java.lang.Exception)
     */
    @Override
    public void exceptionThrown(Exception paramException){
        for(ISDVInitOperationListener initListner : initListners){
            final ISDVInitOperationListener inRunnerLstnr = initListner;
            final IDataSet dataSet = operation.getData();
            final TCSession session = (TCSession)operation.getSession();
            source.getShell().getDisplay().syncExec(new Runnable(){
                @Override
                public void run() {
                    try{
                        inRunnerLstnr.failInitalize(new SDVInitEvent(source, SDVInitEvent.INIT_FAILED, targetId, dataSet));
                    }catch(Exception ex){
                        logger.error(ex);
                    }
                    session.setReadyStatus();
                }
            });
        }

    }
    
}  