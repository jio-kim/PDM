/**
 * 
 */
package com.symc.plm.me.sdv.operation.assembly;

import org.apache.log4j.Logger;

import org.eclipse.swt.widgets.Shell;
import org.sdv.core.common.IDialog;
import org.sdv.core.ui.UIManager;

import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.operation.SimpleTCSDVDialogOperation;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
//import com.teamcenter.rac.cme.framework.util.MFGStructureType;
import com.teamcenter.rac.cme.framework.util.MFGStructureTypeUtil;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

/**
 * Class Name : SaveAsAssemblyOPDialogOperation
 * Class Description :
 * 
 * @date 2013. 11. 27.
 * 
 */
public class SaveAsAssemblyOPDialogOperation extends SimpleTCSDVDialogOperation {
    private static final Logger logger = Logger.getLogger(SaveAsAssemblyOPDialogOperation.class);
    private boolean isValidOK;

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.common.ISDVOperation#preExecuteSDVOperation()
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
            // (체크)4. 조립 공법 선택 유무
            TCComponentBOMLine selectedBOPLine = mfgApp.getSelectedBOMLines()[0];
            String selectedItemType = selectedBOPLine.getItem().getType();
            isEnableType = selectedItemType.equals(SDVTypeConstant.BOP_PROCESS_ASSY_OPERATION_ITEM);
            if (!isEnableType) {
                MessageBox.post(AIFUtility.getActiveDesktop().getShell(), registry.getString("SelectOperation.MSG"), "Warning", MessageBox.WARNING);
                isValidOK = false;
                return;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        isValidOK = true;

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.common.ISDVOperation#executeSDVOperation()
     */
    @Override
    public void executeOperation() throws Exception {
        try {
            if (!isValidOK)
                return;
            Shell shell = AIFUtility.getActiveDesktop().getShell();
            IDialog dialog = UIManager.getDialog(shell, dialogId);
            if(dialog!=null){
            	dialog.open();
            }
        } catch (Exception exception) {
            logger.error(exception.getClass().getName(), exception);
            MessageBox messagebox = new MessageBox(parentFrame, exception);
            messagebox.setModal(true);
            messagebox.setVisible(true);
        }
    }
}
