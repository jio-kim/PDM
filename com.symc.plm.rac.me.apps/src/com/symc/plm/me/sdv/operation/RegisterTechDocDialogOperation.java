package com.symc.plm.me.sdv.operation;

import java.awt.Frame;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Shell;
import org.sdv.core.common.IDialog;
import org.sdv.core.common.IDialogOpertation;
import org.sdv.core.ui.UIManager;

import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

public class RegisterTechDocDialogOperation extends AbstractTCSDVOperation implements IDialogOpertation {

    private static final Logger logger = Logger.getLogger(RegisterTechDocDialogOperation.class);
    public String dialogId;
    protected Frame parentFrame;
    private boolean isValidOK;
    
    public RegisterTechDocDialogOperation() {
    }

    @SuppressWarnings("unused")
    @Override
    public void startOperation(String commandId) {
        
        setParentFrame();
        Registry registry = Registry.getRegistry(this);
        
//        TCComponentItemRevision component = (TCComponentItemRevision) AIFUtility.getCurrentApplication().getTargetComponent();
//        
//        
//        try {
//            if(component == null){
//                MessageBox.post(AIFUtility.getActiveDesktop().getShell(), registry.getString("noBopLoad.MSG"), "Warning", MessageBox.WARNING);
//                isValidOK = false;
//                return;
//            }
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }

        isValidOK = true;
    }


    @Override
    public void endOperation() {

    }

    @Override
    public void executeOperation() throws Exception {
        try {
            if (!isValidOK)
                return;
            Shell shell = AIFUtility.getActiveDesktop().getShell();
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

    protected void setParentFrame() {
        this.parentFrame = AIFDesktop.getActiveDesktop();
        
    }
}
