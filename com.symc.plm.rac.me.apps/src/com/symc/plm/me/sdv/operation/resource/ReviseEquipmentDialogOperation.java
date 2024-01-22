/**
 *
 */
package com.symc.plm.me.sdv.operation.resource;

import java.awt.Frame;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Shell;
import org.sdv.core.common.IDialog;
import org.sdv.core.common.IDialogOpertation;
import org.sdv.core.ui.UIManager;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.operation.AbstractTCSDVOperation;
import com.symc.plm.me.sdv.service.resource.ResourceUtilities;
import com.symc.plm.me.sdv.viewpart.resource.ResourceSearchViewPart;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentDataset;
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
public class ReviseEquipmentDialogOperation extends AbstractTCSDVOperation implements IDialogOpertation {

    private static final Logger logger = Logger.getLogger(ReviseEquipmentDialogOperation.class);
    public String dialogId;

    protected Frame parentFrame;
    private Map<String, Object> parameter;
    private boolean isValidOK;
    private Registry registry;

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.common.ISDVOperation#preExecuteSDVOperation()
     */
    @Override
    public void startOperation(String commandId) {
        registry = Registry.getRegistry(this);
        setParentFrame();
        isValidOK = false;

        // 활성화된 Viewpart 찾기
        ResourceSearchViewPart resourceSearchViewPart = ResourceUtilities.getResourceSearchViewPart();

        try {
            // Viewpart가 열려있는 경우
            if (resourceSearchViewPart != null) {
                // int bopTypeNum = 0; //Equipment
                TCComponentItemRevision targetItemRevision = ResourceUtilities.getSelectedItemRevision(resourceSearchViewPart);
                if (targetItemRevision != null) {
                    if (validateItemRevision(targetItemRevision)) {
                        // ItemRevision 속성 가져오기
                        Map<String, Object> itemRevisionMap = ResourceUtilities.getItemPropMap(targetItemRevision);
                        parameter = getParamters();
                        for (String key : itemRevisionMap.keySet()) {
                            parameter.put(key, itemRevisionMap.get(key));
                        }
                        // 첨부된 Dataset 정보 가져오기
                        HashMap<String, TCComponentDataset> datasetMap = ResourceUtilities.getDatasetMap(targetItemRevision);
                        parameter.put("Dataset", datasetMap);

                        isValidOK = true;
                    }
                }
            } else {
                throw new Exception(registry.getString("Viewpart.Check.MSG"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            MessageBox.post(AIFDesktop.getActiveDesktop().getShell(), e.toString(), "WARNING", MessageBox.WARNING);
        }
    }

    /**
     * @param targetItemRevision
     * @throws Exception
     */
    protected boolean validateItemRevision(TCComponentItemRevision targetItemRevision) throws Exception {
        String[] arrBopType = dialogId.split("[.]");
        String bopType = arrBopType[3];
        String itemType = targetItemRevision.getType();
        String strItemId = targetItemRevision.getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
        String typeKey = strItemId.substring(0, strItemId.indexOf("-"));

        // 설비 Item인 경우
        if (!itemType.equals(SDVTypeConstant.BOP_PROCESS_GENERALEQUIP_ITEM_REV) && !itemType.equals(SDVTypeConstant.BOP_PROCESS_JIGFIXTURE_ITEM_REV) && !itemType.equals(SDVTypeConstant.BOP_PROCESS_ROBOT_ITEM_REV) && !itemType.equals(SDVTypeConstant.BOP_PROCESS_GUN_ITEM_REV)) {
            throw new Exception(registry.getString("EquipmentItem.Check.MSG"));
        }

        // 조립설비검색시
        if (bopType.subSequence(0, 1).equals(registry.getString("BOP.Type.Assy"))) {
            if (!typeKey.equals(registry.getString("BOP.Type.Assy"))) {
                throw new Exception(registry.getString("Assy.Check.MSG"));
            }
        }
        // 차체설비검색시
        if (bopType.subSequence(0, 1).equals(registry.getString("BOP.Type.Body"))) {
            if (!typeKey.equals(registry.getString("BOP.Type.Body"))) {
                throw new Exception(registry.getString("Body.Check.MSG"));
            }
        }
        // 도장설비검색시
        if (bopType.subSequence(0, 1).equals(registry.getString("BOP.Type.Paint"))) {
            if (!typeKey.equals(registry.getString("BOP.Type.Paint"))) {
                throw new Exception(registry.getString("Paint.Check.MSG"));
            }
        }
        return true;
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
