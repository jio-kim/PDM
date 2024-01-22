/**
 * 
 */
package com.symc.plm.me.sdv.operation.assembly;

import java.awt.Frame;


import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Shell;
import org.sdv.core.common.IDialog;
import org.sdv.core.common.IDialogOpertation;
import org.sdv.core.ui.UIManager;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.operation.AbstractTCSDVOperation;
import com.symc.plm.me.utils.CustomUtil;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
//import com.teamcenter.rac.cme.framework.util.MFGStructureType;
import com.teamcenter.rac.cme.framework.util.MFGStructureTypeUtil;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

/**
 * Class Name : CreateAssemblyOPDialogOperation
 * Class Description :
 * 
 * @date 2013. 11. 15.
 * 
 */
public class ModifyAssemblyOPDialogOperation extends AbstractTCSDVOperation implements IDialogOpertation {
    private static final Logger logger = Logger.getLogger(ModifyAssemblyOPDialogOperation.class);
    public String dialogId;
    protected Frame parentFrame;
    private boolean isValidOK;

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.common.ISDVOperation#startOperation(java.lang.String)
     */
    @Override
    public void startOperation(String commandId) {
        setParentFrame();
        Registry registry = Registry.getRegistry(this);
        // MPPAppication
        MFGLegacyApplication mfgApp = (MFGLegacyApplication) AIFUtility.getCurrentApplication();
        // 현재 BOM WINDOW
        TCComponentBOMWindow bomWindow = mfgApp.getBOMWindow();

        try {
            // (체크)1. BOP Load 유무
            if (bomWindow == null) {
                MessageBox.post(AIFUtility.getActiveDesktop().getShell(), registry.getString("noBopLoad.MSG"), "Warning", MessageBox.WARNING);
                isValidOK = false;
                return;
            }
            // (체크)2. Process BOP Load 유무
//            MFGStructureType mfgType = MFGStructureTypeUtil.getStructureType(bomWindow.getTopBOMLine());
//            if (mfgType != MFGStructureType.Process) {
//                MessageBox.post(AIFUtility.getActiveDesktop().getShell(), registry.getString("noBopLoad.MSG"), "Warning", MessageBox.WARNING);
//                isValidOK = false;
//                return;
//            }
            boolean isProcess = MFGStructureTypeUtil.isProcess(bomWindow.getTopBOMLine());
            if(!isProcess) {
                MessageBox.post(AIFUtility.getActiveDesktop().getShell(), registry.getString("noBopLoad.MSG"), "Warning", MessageBox.WARNING);
                isValidOK = false;
                return;
            }
            // (체크)3. Top이 Shop인지 유무
            String topItemType = bomWindow.getTopBOMLine().getItem().getType();
            boolean isEnableType = topItemType.equals(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM);
            if (!isEnableType) {
                MessageBox.post(AIFUtility.getActiveDesktop().getShell(), registry.getString("noBopLoad.MSG"), "Warning", MessageBox.WARNING);
                isValidOK = false;
                return;
            }
            // (체크)4. Line일 경우 Release 유무 체크
            TCComponentBOMLine selectedBOMLine = mfgApp.getSelectedBOMLines()[0];
            if (selectedBOMLine != null) {
                String selectedItemType = selectedBOMLine.getItem().getType();
                if (selectedItemType.equals(SDVTypeConstant.BOP_PROCESS_LINE_ITEM)) {
                    // 미할당 Line
                    String tempLineId = getTempBOPLineId(bomWindow.getTopBOMLine());
                    // 선택된것이 미할당 라인유무
                    boolean isTargetTempLine = tempLineId.equals(selectedBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID));
                    if (CustomUtil.isReleased(selectedBOMLine.getItemRevision()) && !isTargetTempLine) {
                        MessageBox.post(AIFUtility.getActiveDesktop().getShell(), registry.getString("SelectedBOPReleased.MSG"), "Warning", MessageBox.WARNING);
                        isValidOK = false;
                        return;
                    }
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            isValidOK = false;
        }

        isValidOK = true;

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

    /**
     * 미할당 Line Id를 가져옴
     * 
     * @method getTempBOPLineId
     * @date 2013. 12. 31.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    private String getTempBOPLineId(TCComponentBOMLine topBOMLine) throws Exception {
        String tempLineId = "";
        String shopId = topBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
        String[] idSplit = shopId.split("-");
        // 미할당 LINE ID
        tempLineId = idSplit[0] + "-" + idSplit[1] + "-TEMP-" + idSplit[2];
        return tempLineId;
    }

}
