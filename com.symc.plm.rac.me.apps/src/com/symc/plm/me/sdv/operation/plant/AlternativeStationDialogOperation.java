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

import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.operation.AbstractTCSDVOperation;
import com.symc.plm.me.sdv.service.Plant.PlantUtilities;
import com.symc.plm.me.utils.CustomUtil;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

/**
 * Class Name : AbstractTCSDVExecuteOperation
 * Class Description :
 * 
 * @date 2013. 9. 17.
 * 
 */
public class AlternativeStationDialogOperation extends AbstractTCSDVOperation implements IDialogOpertation {

    private static final Logger logger = Logger.getLogger(AlternativeStationDialogOperation.class);
    public String dialogId;

    protected Frame parentFrame;
    private Map<String, Object> parameter;
    private boolean isValidOK;
    private Registry registry;
    private String opareaMessage;

    @Override
    public void startOperation(String commandId) {
        registry = Registry.getRegistry(this);
        setParentFrame();
        isValidOK = true;
        opareaMessage = null;

        // MPPAppication
        MFGLegacyApplication mfgApp = (MFGLegacyApplication) AIFUtility.getCurrentApplication();
        // 현재 BOM WINDOW
        TCComponentBOMWindow bomWindow = mfgApp.getBOMWindow();

        try {
            //Top이 Shop인지 유무 : Shop이 아니면 오류 처리
            String topItemType = bomWindow.getTopBOMLine().getItem().getType();
            boolean isEnableType = topItemType.equals(SDVTypeConstant.PLANT_SHOP_ITEM);
            if (!isEnableType) {
                MessageBox.post(AIFUtility.getActiveDesktop().getShell(), registry.getString("noBoMLoad.MSG"), "Warning", MessageBox.WARNING);
                isValidOK = false;
                return;
            }

            InterfaceAIFComponent interfaceAIFComponent = mfgApp.getTargetComponent();
            TCComponentItemRevision itemRevision = PlantUtilities.getItemRevision(interfaceAIFComponent);
            String itemRevisionType = itemRevision.getType();

            // 선택한 targetItem이
            if (itemRevisionType != null) {
                // Station이 아니면 오류 처리
                if (!itemRevisionType.equals(SDVTypeConstant.PLANT_STATION_ITEM_REVISION)) {
                    opareaMessage = "Please selecte Plant Station.";
                    isValidOK = false;
                    return;
                }
            } else {
                isValidOK = false;
            }

            //상부 Line이 Released면 오류 처리
            TCComponentBOMLine selectedBOMLine = mfgApp.getSelectedBOMLines()[0];
            TCComponentBOMLine parentBOMLine = selectedBOMLine.parent();
            if(CustomUtil.isReleased(parentBOMLine.getItemRevision())) {
                opareaMessage = itemRevision.toString() + " is Released.";
                isValidOK = false;
                return;
            }
            
            //Apply Alternative to Production시
            if(dialogId.equals("symc.me.bop.ApplyAlternativeStationDialog")) {
                //Production은 Production에 반영하지 못한다.
                if(!selectedBOMLine.getItemRevision().getLogicalProperty(SDVPropertyConstant.PLANT_REV_IS_ALTBOP)) {
                    opareaMessage = itemRevision.toString() + " is not Alternative Plant.";
                    isValidOK = false;
                }
            }
            // Copy Alternative from Production시
            else if(dialogId.equals("symc.me.bop.CopyAlternativeStationDialog")) {
                // ALT는 ALT로 복사 할 수 있다.
            }
            
            
            // ItemRevision을 View에 Parameter로 보낸다.
            parameter = getParamters();
            parameter.put("tcComponentBOMLine", selectedBOMLine);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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
