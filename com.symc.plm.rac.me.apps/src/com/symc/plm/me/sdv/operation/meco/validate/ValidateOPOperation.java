/**
 *
 */
package com.symc.plm.me.sdv.operation.meco.validate;

import com.kgm.common.WaitProgressBar;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.operation.AbstractTCSDVOperation;
import com.symc.plm.me.sdv.operation.meco.validate.ValidateManager.BOPTYPE;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
//import com.teamcenter.rac.cme.framework.util.MFGStructureType;
import com.teamcenter.rac.cme.framework.util.MFGStructureTypeUtil;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

/**
 * Class Name : ValidateOPOperation
 * Class Description :
 * 
 * @date 2013. 12. 10.
 * 
 */
public class ValidateOPOperation extends AbstractTCSDVOperation {

    private WaitProgressBar progress = null;
    private TCComponentBOMLine targetBOMLine = null;
    private boolean isValidOK = false;
    private Registry registry = null;
    private BOPTYPE bopType;

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.common.ISDVOperation#startOperation(java.lang.String)
     */
    @Override
    public void startOperation(String commandId) {

        registry = Registry.getRegistry(this);

        try {
            MFGLegacyApplication mfgApp = (MFGLegacyApplication) AIFUtility.getCurrentApplication();

            TCComponentBOMWindow bomWindow = mfgApp.getBOMWindow();

            if (bomWindow == null)
                return;
            targetBOMLine = mfgApp.getSelectedBOMLines()[0];
            TCComponentItemRevision targetItemRevision = targetBOMLine.getItemRevision();
            String itemType = targetBOMLine.getItem().getType();

            // (üũ)2. Process BOP Load ����
//            MFGStructureType mfgType = MFGStructureTypeUtil.getStructureType(bomWindow.getTopBOMLine());
//            if (mfgType != MFGStructureType.Process) {
//                MessageBox.post(AIFUtility.getActiveDesktop().getShell(), registry.getString("NotBopLoad.MSG"), "Warning", MessageBox.WARNING);
//                isValidOK = false;
//                return;
//            }
            boolean isProcess = MFGStructureTypeUtil.isProcess(bomWindow.getTopBOMLine());
            if(!isProcess) {
                MessageBox.post(AIFUtility.getActiveDesktop().getShell(), registry.getString("NotBopLoad.MSG"), "Warning", MessageBox.WARNING);
                isValidOK = false;
                return;
            }
            // (üũ)3. Top�� Shop���� ����
            String topItemType = bomWindow.getTopBOMLine().getItem().getType();
            boolean isEnableType = topItemType.equals(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM);
            if (!isEnableType) {
                MessageBox.post(AIFUtility.getActiveDesktop().getShell(), registry.getString("NotBopLoad.MSG"), "Warning", MessageBox.WARNING);
                isValidOK = false;
                return;
            }

            // Process Type(����,����,��ü ����)�� ������
            if (itemType.equals(SDVTypeConstant.BOP_PROCESS_LINE_ITEM) || itemType.equals(SDVTypeConstant.BOP_PROCESS_STATION_ITEM)) {
                String processType = targetItemRevision.getProperty(SDVPropertyConstant.LINE_REV_PROCESS_TYPE);
                if (processType.isEmpty()) {
                    MessageBox.post(AIFUtility.getActiveDesktop().getShell(), registry.getString("NotExistProcessType.MSG"), "Warning", MessageBox.WARNING);
                    isValidOK = false;
                    return;
                }
                bopType = getBopType(processType);
            } else
                bopType = getBopType(itemType);

            // (üũ)4. ���õ� BOP�� LINE, STATION Ȥ�� ���� ���� (����/���� : LINE Ȥ�� ����, ��ü : LINE Ȥ�� STATION) 
            boolean isValidSelectedType = false;
            String errorMSG = "Select Line, Please.";
            if (bopType.equals(BOPTYPE.ASSEMBLY) || bopType.equals(BOPTYPE.PAINT)) {
                isValidSelectedType = itemType.equals(SDVTypeConstant.BOP_PROCESS_LINE_ITEM) || itemType.equals(SDVTypeConstant.BOP_PROCESS_ASSY_OPERATION_ITEM) || itemType.equals(SDVTypeConstant.BOP_PROCESS_PAINT_OPERATION_ITEM);
                errorMSG = registry.getString("NotValidSelectedType.MSG");
            } else if (bopType.equals(BOPTYPE.BODY)) {
                isValidSelectedType = itemType.equals(SDVTypeConstant.BOP_PROCESS_LINE_ITEM) || itemType.equals(SDVTypeConstant.BOP_PROCESS_STATION_ITEM);
                errorMSG = registry.getString("NotValidSelectedType2.MSG").replace("SELECTED BOMLINE,%0,%1, ", "");
            }
            if (!isValidSelectedType) {
                MessageBox.post(AIFUtility.getActiveDesktop().getShell(), errorMSG, "Warning", MessageBox.WARNING);
                isValidOK = false;
                return;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            setAbortRequested(true);
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

            if (isValidOK == false)
                return;
            /**
             * Line ����, Ÿ�� üũ, ���� �� üũ��
             */

            progress = new WaitProgressBar(AIFUtility.getActiveDesktop().getFrame());
            progress.setWindowSize(700, 500);
            progress.start();

            progress.setShowButton(true);
            // progress.setAlwaysOnTop(true);

            ValidateManager vldMgr = new ValidateManager(progress);
            boolean isValid = vldMgr.executeValidation(bopType, targetBOMLine);

            if (isValid)
                progress.close("Validation Success", false, false);
            else
                progress.close("Check Validation List!", true, false);

        } catch (Exception ex) {
            progress.setStatus("Error was Happened");
            progress.setStatus(ex.toString());
            progress.close("Error", true, false);
            setAbortRequested(true);

        }
    }

    /**
     * BOP TYPE�� ������
     * 
     * @method getBopType
     * @date 2013. 12. 13.
     * @param
     * @return BOPTYPE
     * @exception
     * @throws
     * @see
     */
    private BOPTYPE getBopType(String type) {
        BOPTYPE bopType;
        if (type.equals(SDVTypeConstant.BOP_PROCESS_PAINT_OPERATION_ITEM) || type.startsWith("P"))
            bopType = BOPTYPE.PAINT;
        else if (type.equals(SDVTypeConstant.BOP_PROCESS_BODY_OPERATION_ITEM) || type.startsWith("B"))
            bopType = BOPTYPE.BODY;
        else
            bopType = BOPTYPE.ASSEMBLY;

        return bopType;
    }

}
