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

import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.operation.AbstractTCSDVOperation;
import com.symc.plm.me.sdv.service.Plant.PlantUtilities;
import com.symc.plm.me.utils.CustomUtil;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.util.MessageBox;

/**
 * Class Name : AbstractTCSDVExecuteOperation
 * Class Description :
 * 
 * @date 2013. 9. 17.
 * 
 */
public class CreatePlantDialogOperation extends AbstractTCSDVOperation implements IDialogOpertation {

    private static final Logger logger = Logger.getLogger(CreatePlantDialogOperation.class);
    public String dialogId;

    protected Frame parentFrame;
    private Map<String, Object> parameter;
    private boolean isValidOK;
    // private Registry registry;
    private String opareaMessage;

    @SuppressWarnings("unused")
    @Override
    public void startOperation(String commandId) {
        // registry = Registry.getRegistry(this);
        setParentFrame();
        isValidOK = true;
        opareaMessage = null;

        // MPPAppication
        MFGLegacyApplication mfgApp = (MFGLegacyApplication) AIFUtility.getCurrentApplication();
        // 현재 BOM WINDOW
        TCComponentBOMWindow bomWindow = mfgApp.getBOMWindow();

        try {
            // // (체크)1. BOP Load 유무 : Plant Shop 생성
            // if (bomWindow == null) {
            // dialogId = "symc.me.bop.CreateShopItemDialog";
            // return;
            // }
            //
            // // (체크)2. Plant BOP Load 유무 : Plant가 아니면 Shop 생성
            // MFGStructureType mfgType = MFGStructureTypeUtil.getStructureType(bomWindow.getTopBOMLine());
            // if (mfgType != MFGStructureType.Plant) {
            // dialogId = "symc.me.bop.CreateShopItemDialog";
            // return;
            // }

            // // (체크)3. Top이 Shop인지 유무 : Shop이 아니면 오류 처리
            // String topItemType = bomWindow.getTopBOMLine().getItem().getType();
            // boolean isEnableType = topItemType.equals(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM);
            // if (!isEnableType) {
            // MessageBox.post(AIFUtility.getActiveDesktop().getShell(), registry.getString("noBopLoad.MSG"), "Warning", MessageBox.WARNING);
            // isValidOK = false;
            // return;
            // } else {
            // }

            // (체크)3. interfaceAIFComponent 가 : Mfg0BvrWorkarea가 아니면 Shop 생성
            InterfaceAIFComponent interfaceAIFComponent = mfgApp.getTargetComponent();
            TCComponentItemRevision itemRevision = PlantUtilities.getItemRevision(interfaceAIFComponent);
            String itemRevisionType = itemRevision.getType();

            // 선택한 targetItem이
            // Shop인경우 : Line 생성 화면
            if (itemRevisionType != null && itemRevisionType.equals(SDVTypeConstant.PLANT_SHOP_ITEM_REVISION)) {
                opareaMessage = itemRevision.toString() + " is Shop Item.";
                isValidOK = false;
                return;
            } else if (itemRevisionType != null && itemRevisionType.equals(SDVTypeConstant.PLANT_LINE_ITEM_REVISION)) {
                // Line인경우 : Station 생성 화면
                dialogId = "symc.me.bop.CreateStationItemDialog";
            } else if (itemRevisionType != null && itemRevisionType.equals(SDVTypeConstant.PLANT_STATION_ITEM_REVISION)) {
                // Station인경우 : OPArea 생성 화면
                dialogId = "symc.me.bop.CreateWorkareaItemDialog";
            } else if (itemRevisionType != null && itemRevisionType.equals(SDVTypeConstant.PLANT_OPAREA_ITEM_REV)) {
                // OPArea인경우 : 알림만 띄운다.
                isValidOK = false;
                opareaMessage = "Plant WorkArea child can not be added.";
                return;
            } else {
                isValidOK = false;
            }

            // Released가 아니면 오류 처리
            if (CustomUtil.isReleased(itemRevision)) {
                opareaMessage = itemRevision.toString() + " is Released.";
                isValidOK = false;
                return;
            }

            // ItemRevision을 View에 Parameter로 보낸다.
            parameter = getParamters();
            parameter.put("targetItemRevision", itemRevision);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // }
        // isValidOK = false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.common.ISDVOperation#executeSDVOperation()
     */
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
