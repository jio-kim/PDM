/**
 * 
 */
package com.symc.plm.me.sdv.operation.option;

import java.awt.Frame;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.symc.plm.me.sdv.dialog.option.ConditionSetDialog;
import com.symc.plm.me.sdv.operation.AbstractTCSDVOperation;
import com.symc.plm.me.utils.variant.ConditionVector;
import com.symc.plm.me.utils.variant.OptionManager;
import com.symc.plm.me.utils.variant.VariantOption;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.util.MessageBox;

/**
 * Class Name : SetOptionConditionsOperation
 * Class Description :
 * 
 * [SR150521-012][20150522] shcho, 옵션 적용시 멀티적용 되도록 기능 개선
 * 
 * @date 2013. 11. 12.
 * 
 */
public class SetOptionConditionsOperation extends AbstractTCSDVOperation {
    private static final Logger logger = Logger.getLogger(SetOptionConditionsOperation.class);

    protected Frame parentFrame;
    private OptionManager manager = null;
    public String dialogId;
    private boolean isValidOK = false;
    private TCComponentBOMLine[] selectedBOMLines = null; // 선택된 공법 BOMLine
    private TCComponentBOMLine bopTopBOMLine = null; // BOP TOP BOMLine
    private Vector<String[]> userDefineErrorList = new Vector<String[]>();

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.common.ISDVOperation#startOperation(java.lang.String)
     */
    @Override
    public void startOperation(String commandId) {
        // MPPAppication
        MFGLegacyApplication mfgApp = (MFGLegacyApplication) AIFUtility.getCurrentApplication();
        // 현재 BOM WINDOW
        TCComponentBOMWindow bomWindow = mfgApp.getBOMWindow();

        try {
            // (체크)1. BOP Load 유무
            if (bomWindow == null) {
                com.teamcenter.rac.util.MessageBox.post(AIFUtility.getActiveDesktop().getShell(), "BOP를 Load하여 주십시요.", "경고", com.teamcenter.rac.util.MessageBox.WARNING);
                isValidOK = false;
                return;
            }

            selectedBOMLines = mfgApp.getSelectedBOMLines();
 //           String selectedItemType = selectedBOMLine.getItem().getType();
//            boolean isEnableType = selectedItemType.equals(SDVTypeConstant.BOP_PROCESS_ASSY_OPERATION_ITEM) || selectedItemType.equals(SDVTypeConstant.BOP_PROCESS_BODY_OPERATION_ITEM) || selectedItemType.equals(SDVTypeConstant.BOP_PROCESS_PAINT_OPERATION_ITEM);
            // (체크)3. Process BOP 선택 유무
//            if (!isEnableType) {
//                MessageBox.post(AIFUtility.getActiveDesktop().getShell(), "공법을 선택하여 주세요", "경고", MessageBox.WARNING);
//                isValidOK = false;
//                return;
//            }

            bopTopBOMLine = selectedBOMLines[0].window().getTopBOMLine();

        } catch (Exception ex) {
            ex.printStackTrace();
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

            ArrayList<VariantOption> enableOptionSet = new ArrayList<VariantOption>();
            ConditionSetDialog dialog = null;
            try {
                manager = new OptionManager(selectedBOMLines[0], true);
                ArrayList<VariantOption> optionSet = manager.getOptionSet(bopTopBOMLine, null, null, null, false, false);
                enableOptionSet.addAll(optionSet);
                List<ConditionVector> conditions = null;
                if(selectedBOMLines.length == 1) {
                    conditions = manager.getConditionSet(selectedBOMLines[0]);                    
                }
                dialog = new ConditionSetDialog(enableOptionSet, conditions, selectedBOMLines, userDefineErrorList, manager);
                dialog.setVisible(true);
            } catch (Exception e) {
                logger.error(e.getClass().getName(), e);
                MessageBox messagebox = new MessageBox(parentFrame, e);
                messagebox.setModal(true);
                messagebox.setVisible(true);
                dialog.dispose();
            }

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
