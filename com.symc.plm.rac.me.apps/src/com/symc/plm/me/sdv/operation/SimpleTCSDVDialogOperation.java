/**
 *
 */
package com.symc.plm.me.sdv.operation;

import java.awt.Frame;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.sdv.core.common.IDialog;
import org.sdv.core.common.IDialogOpertation;
import org.sdv.core.ui.UIManager;

import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.util.MessageBox;

/**
 * Class Name : AbstractTCSDVExecuteOperation
 * Class Description :
 *
 * @date 2013. 9. 17.
 *
 */
public class SimpleTCSDVDialogOperation extends AbstractTCSDVOperation implements IDialogOpertation {

    private static final Logger logger = Logger.getLogger(SimpleTCSDVDialogOperation.class);
    public String dialogId;

    protected Frame parentFrame;

    /*
     * (non-Javadoc)
     *
     * @see org.sdv.core.common.ISDVOperation#preExecuteSDVOperation()
     */
    @Override
    public void startOperation(String commandId) {
        setParentFrame();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.sdv.core.common.ISDVOperation#executeSDVOperation()
     */
    @Override
    public void executeOperation() throws Exception {
        try {
            //Shell shell = AIFUtility.getActiveDesktop().getShell();
            
        	Shell shell = null;
        	IWorkbenchWindow iworkbenchwindow = AIFDesktop.getActiveDesktop().getDesktopWindow();
            if(iworkbenchwindow != null)
            {
                shell = iworkbenchwindow.getShell();
            }
            
            IDialog dialog = UIManager.getDialog(shell, dialogId);
            dialog.setParameters(getParamters());
            dialog.open();
        } catch (Exception exception) {
            logger.error(exception.getClass().getName(), exception);
            MessageBox messagebox = new MessageBox(parentFrame, exception);
            messagebox.setModal(true);
            messagebox.setVisible(true);
        }
    }

    protected void setParentFrame() {
        this.parentFrame = AIFDesktop.getActiveDesktop();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.sdv.core.common.ISDVOperation#afterExecuteSDVOperation()
     */
    @Override
    public void endOperation() {
        // IDialog dialog = UIManager.getActiveDialog(dialogId);
    }

    /**
     * @return the dialogId
     */
    public String getDialogId() {
        return dialogId;
    }

    /**
     * @param dialogId
     *            the dialogId to set
     */
    public void setDialogId(String dialogId) {
        this.dialogId = dialogId;
    }

}
