/**
 * 
 */
package com.symc.plm.me.sdv.operation.swm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sdv.core.common.IDialog;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.ui.UIManager;
import org.sdv.core.ui.operation.AbstractSDVActionOperation;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.operation.ps.SDVNewProcessCommand;
import com.symc.plm.me.utils.CustomUtil;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

/**
 * Class Name : WorkflowProcessSWMDocOperation
 * Class Description :
 * 
 * @date 2014. 1. 27.
 * 
 */
public class WorkflowProcessSWMDocOperation extends AbstractSDVActionOperation {
    private Registry registry = Registry.getRegistry(this);
    InterfaceAIFComponent[] selectedTargets;
    TCComponent selectedTarget;

    public WorkflowProcessSWMDocOperation(int actionId, String ownerId, IDataSet dataSet) {
        super(actionId, ownerId, dataSet);
    }

    public WorkflowProcessSWMDocOperation(String actionId, String ownerId, IDataSet dataSet) {
        super(actionId, ownerId, dataSet);
    }

    public WorkflowProcessSWMDocOperation(int actionId, String ownerId, Map<String, Object> parameters, IDataSet dataset) {
        super(actionId, ownerId, parameters, dataset);
    }

    public WorkflowProcessSWMDocOperation(String actionId, String ownerId, Map<String, Object> parameters, IDataSet dataset) {
        super(actionId, ownerId, parameters, dataset);
    }

    @Override
    public void startOperation(String commandId) {

    }

    @Override
    public void endOperation() {

    }

    @Override
    public void executeOperation() throws Exception {
        Map<String, Object> paramMap = getParameters();

        if (paramMap.containsKey("modifyflag")) {
            String checkinflag = (String) paramMap.get("modifyflag");
            if (checkinflag.equals("true")) {
                IDataSet dataset = getDataSet();
                IDataMap dataMap = dataset.getDataMap("registerSWMDocView");

                TCComponentItemRevision targetComp = (TCComponentItemRevision) dataMap.getValue("targetComp");
                selectedTargets = new InterfaceAIFComponent[] { targetComp };
            } else {
                List<HashMap<String, Object>> opList = null;
                IDataSet dataset = getDataSet();
                IDataMap dataMap = dataset.getDataMap("searchListSWMDocView");

                if (!dataMap.containsKey("targetOperationList")) {
                    MessageBox.post(UIManager.getCurrentDialog().getShell(), registry.getString("SelectOneTargetItem.MESSAGE"), "Warning", MessageBox.WARNING);
                    return;
                } else {
                    opList = (List<HashMap<String, Object>>) dataMap.getTableValue("targetOperationList");
                    if (opList.size() == 0) {
                        MessageBox.post(UIManager.getCurrentDialog().getShell(), registry.getString("SelectOneTargetItem.MESSAGE"), "Warning", MessageBox.WARNING);
                        return;
                    }
                }

                selectedTargets = new InterfaceAIFComponent[opList.size()];
                for (int i = 0; i < opList.size(); i++) {
                    String itemId = registry.getString((String) opList.get(i).get(SDVPropertyConstant.ITEM_ITEM_ID));
                    String revisionId = (String) opList.get(i).get(SDVPropertyConstant.ITEM_REVISION_ID);

                    TCComponentItemRevision revision = CustomUtil.findItemRevision(SDVTypeConstant.STANDARD_WORK_METHOD_ITEM_REV, itemId, revisionId);
                    selectedTargets[i] = revision.getItem().getLatestItemRevision();
                    selectedTarget = (TCComponent) (selectedTargets[i]);
                    if (!wrokflowProcessValidate()) {
                        return;
                    }
                }
            }
        }

        new SDVNewProcessCommand(AIFUtility.getActiveDesktop(), AIFUtility.getCurrentApplication(), selectedTargets, registry.getString("SWM_Workflow_Template"));
    }

    public boolean wrokflowProcessValidate() throws TCException {
        if (CustomUtil.isInProcess((TCComponent) selectedTarget)) {
            IDialog dialog = UIManager.getCurrentDialog();
            if (dialog.getShell() == null) {
                dialog = UIManager.getAvailableDialog("symc.dialog.SearchSWMDocDialog");
            }
            MessageBox.post(dialog.getShell(), registry.getString("SelectedTargetIsInAWorkflowProcess.MESSAGE"), "Warning", MessageBox.WARNING);
            return false;
        }

        if (CustomUtil.isReleased((TCComponent) selectedTarget)) {
            IDialog dialog = UIManager.getCurrentDialog();
            if (dialog.getShell() == null) {
                dialog = UIManager.getAvailableDialog("symc.dialog.SearchSWMDocDialog");
            }
            MessageBox.post(dialog.getShell(), registry.getString("SelectedTargetWasReleased.MESSAGE"), "Warning", MessageBox.WARNING);
            return false;
        }

        String checkOutUser = selectedTarget.getProperty("checked_out_user");
        if (!CustomUtil.getTCSession().getUser().toString().equals(checkOutUser) && !checkOutUser.trim().equals("")) {
            IDialog dialog = UIManager.getCurrentDialog();
            if (dialog.getShell() == null) {
                dialog = UIManager.getAvailableDialog("symc.dialog.SearchSWMDocDialog");
            }
            MessageBox.post(dialog.getShell(), registry.getString("SelectedTargetIsWorking.MESSAGE"), "Warning", MessageBox.WARNING);
            return false;
        }

        return true;
    }

}
