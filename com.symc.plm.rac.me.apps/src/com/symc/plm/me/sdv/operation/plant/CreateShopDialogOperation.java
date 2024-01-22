/**
 *
 */
package com.symc.plm.me.sdv.operation.plant;

import java.awt.Frame;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Shell;
import org.sdv.core.common.IDialog;
import org.sdv.core.common.IDialogOpertation;
import org.sdv.core.common.exception.SDVException;
import org.sdv.core.ui.UIManager;

import com.symc.plm.me.sdv.operation.AbstractTCSDVOperation;
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
public class CreateShopDialogOperation extends AbstractTCSDVOperation implements IDialogOpertation {

    private static final Logger logger = Logger.getLogger(CreateShopDialogOperation.class);
    public String dialogId;

    protected Frame parentFrame;
    private Map<String, Object> parameter;
    private boolean isValidOK;
    private String opareaMessage;

    @Override
    public void startOperation(String commandId) {
        setParentFrame();
        isValidOK = true;
        opareaMessage = null;
    }

    @Override
    public void executeOperation() throws Exception {
        try {
            if (!isValidOK) {
                throw new SDVException((opareaMessage == null) ? "Failed to initialize the Dialog" : opareaMessage);
            }

            Shell shell = AIFUtility.getActiveDesktop().getShell();
            IDialog dialog = UIManager.getDialog(shell, dialogId);
            dialog.setParameters(parameter);
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

    @Override
    public void endOperation() {

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
