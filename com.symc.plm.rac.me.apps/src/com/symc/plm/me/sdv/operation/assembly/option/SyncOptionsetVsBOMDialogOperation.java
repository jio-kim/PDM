/**
 * 
 */
package com.symc.plm.me.sdv.operation.assembly.option;

import java.awt.Frame;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Shell;
import org.sdv.core.common.IDialog;
import org.sdv.core.common.IDialogOpertation;
import org.sdv.core.ui.UIManager;

import com.symc.plm.me.sdv.operation.AbstractTCSDVOperation;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

/**
 * Class Name : SyncOptionsetVsBOMDialog
 * Class Description :
 * 
 * @date 2013. 11. 25.
 * 
 */
public class SyncOptionsetVsBOMDialogOperation extends AbstractTCSDVOperation implements IDialogOpertation {
    private static final Logger logger = Logger.getLogger(SyncOptionsetVsBOMDialogOperation.class);
    public String dialogId;
    protected Frame parentFrame;
    private boolean isValidOK = true;
    private Registry registry = null;

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.common.ISDVOperation#startOperation(java.lang.String)
     */
    @Override
    public void startOperation(String commandId) {
        setParentFrame();
        try {
            registry = Registry.getRegistry(this);
            // MPPAppication
            MFGLegacyApplication mfgApp = (MFGLegacyApplication) AIFUtility.getCurrentApplication();
            // 현재 BOM WINDOW
            TCComponentBOMWindow bomWindow = mfgApp.getBOMWindow();

            // (체크)1. BOP Load 유무
            if (bomWindow == null) {
                com.teamcenter.rac.util.MessageBox.post(AIFUtility.getActiveDesktop().getShell(), registry.getString("noBopLoad.MSG"), "Warning", com.teamcenter.rac.util.MessageBox.WARNING);
                isValidOK = false;
                return;
            }

//            // (체크)2. Process BOP Load 유무
//            MFGStructureType mfgType = MFGStructureTypeUtil.getStructureType(bomWindow.getTopBOMLine());
//            if (mfgType != MFGStructureType.Process) {
//                MessageBox.post(AIFUtility.getActiveDesktop().getShell(), registry.getString("noBopLoad.MSG"), "Warning", MessageBox.WARNING);
//                isValidOK = false;
//                return;
//            }
//            // (체크)3. Top이 Shop인지 유무
//            String topItemType = bomWindow.getTopBOMLine().getItem().getType();
//            boolean isEnableType = topItemType.equals(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM);
//            if (!isEnableType) {
//                MessageBox.post(AIFUtility.getActiveDesktop().getShell(), registry.getString("noBopLoad.MSG"), "Warning", MessageBox.WARNING);
//                isValidOK = false;
//                return;
//            }

            isValidOK = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.common.ISDVOperation#endOperation()
     */
    @Override
    public void endOperation() {

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.teamcenter.rac.aif.AbstractAIFOperation#executeOperation()
     */
    @Override
    public void executeOperation() throws Exception {
        try {
            if (!isValidOK)
                return;
            Shell shell = AIFUtility.getActiveDesktop().getShell();
            IDialog dialog = UIManager.getDialog(shell, dialogId);
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
