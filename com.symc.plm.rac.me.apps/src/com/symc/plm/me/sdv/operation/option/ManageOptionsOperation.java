/**
 * 
 */
package com.symc.plm.me.sdv.operation.option;

import java.awt.Dimension;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.dialog.option.OptionSetDialog;
import com.symc.plm.me.sdv.operation.AbstractTCSDVOperation;
import com.symc.plm.me.utils.variant.OptionManager;
import com.symc.plm.me.utils.variant.VariantOption;
import com.symc.plm.me.utils.variant.VariantValue;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.util.MessageBox;

/**
 * Class Name : ManageOptionsOperation
 * Class Description :
 * 
 * @date 2013. 11. 12.
 * 
 */
public class ManageOptionsOperation extends AbstractTCSDVOperation {

    private static final Logger logger = Logger.getLogger(ManageOptionsOperation.class);
    protected Frame parentFrame;
    public String dialogId;

    private boolean isValidOK = false;
    private TCComponentBOMLine selectedBOMLine = null; // 선택된 공법 BOMLine
    private TCComponentBOMLine bopTopBOMLine = null; // BOP TOP BOMLine
    private Vector<String[]> userDefineErrorList = new Vector<String[]>();
    private OptionManager manager = null;

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

            selectedBOMLine = mfgApp.getSelectedBOMLines()[0];
            String selectedItemType = selectedBOMLine.getItem().getType();
            boolean isEnableType = selectedItemType.equals(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM);
            // (체크)3. Process BOP 선택 유무
            if (!isEnableType) {
                MessageBox.post(AIFUtility.getActiveDesktop().getShell(), "Shop 을 선택하여 주십시요", "경고", MessageBox.WARNING);
                isValidOK = false;
                return;
            }

            bopTopBOMLine = selectedBOMLine.window().getTopBOMLine();
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

            Vector<String[]> moduleConstratintsList = new Vector<String[]>();
            ArrayList<VariantOption> selectedLineOptionSet = new ArrayList<VariantOption>();
            ArrayList<VariantOption> optionSet = null;
            OptionSetDialog dialog = null;
            try {
                ArrayList<VariantOption> globalOptionSet = new ArrayList<VariantOption>();
                manager = new OptionManager(selectedBOMLine, true);
                globalOptionSet = manager.getCorpOptionSet();

                optionSet = manager.getOptionSet(bopTopBOMLine, userDefineErrorList, moduleConstratintsList, true);
                selectedLineOptionSet.addAll(optionSet);

                Vector<VariantValue> valueList = new Vector<VariantValue>();
                for (int i = 0; i < optionSet.size(); i++) {
                    VariantOption option = optionSet.get(i);
                    List<VariantValue> list = option.getValues();
                    for (int j = 0; j < list.size(); j++) {
                        list.get(j).setUsing(false);
                        if (!valueList.contains(list.get(j)))
                            valueList.add(list.get(j));
                    }
                }
                dialog = new OptionSetDialog(globalOptionSet, globalOptionSet, selectedLineOptionSet, bopTopBOMLine, userDefineErrorList, moduleConstratintsList, manager);
                dialog.setSize(new Dimension(1040, 768));
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

}
