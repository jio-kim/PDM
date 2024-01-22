/**
 * 
 */
package org.sdv.core.ui.operation;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.sdv.core.common.IDialog;
import org.sdv.core.ui.UIManager;

/**
 * Class Name : SimpleDialogOperation
 * Class Description :
 * 
 * @date 2013. 10. 22.
 * 
 */
public class SimpleDialogOperation extends AbstractDialogSDVOperation {
    protected IDialog dialog;

    /**
     * @param jobName
     */
    public SimpleDialogOperation() {
        this("");
    }
    
    /**
     * @param jobName
     */
    public SimpleDialogOperation(String jobName) {
        super(jobName);
    }



    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.common.ISDVOperation#preExecuteSDVOperation()
     */
    @Override
    public void endOperation(){

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.common.ISDVOperation#executeSDVOperation()
     */
    @Override
    public void executeOperation() throws Exception {
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                try {
                    Shell shell = Display.getCurrent().getActiveShell();
                    dialog = UIManager.getDialog(shell, dialogId);
                    dialog.open();
                } catch (Exception e) {                    
                    e.printStackTrace();
                }
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.common.ISDVOperation#afterExecuteSDVOperation()
     */
    @Override
    public void startOperation(String jobName){
        

    }

}
