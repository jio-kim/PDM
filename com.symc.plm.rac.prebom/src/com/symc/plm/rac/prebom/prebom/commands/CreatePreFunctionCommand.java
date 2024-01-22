/**
 * 
 */
package com.symc.plm.rac.prebom.prebom.commands;

import java.util.HashMap;

import com.symc.plm.rac.prebom.prebom.dialog.prefunction.PreFunctionCreateDialog;
import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aifrcp.AIFUtility;

/**
 * @author jinil
 *
 */
public class CreatePreFunctionCommand extends AbstractAIFCommand {
    @Override
    protected void executeCommand() throws Exception {
//        InterfaceAIFComponent targetComponent = AIFUtility.getCurrentApplication().getTargetComponent();
        HashMap<String, Object> targetMap = new HashMap<String, Object>();
//
//        if (targetComponent != null && targetComponent instanceof TCComponentBOMLine && ((TCComponentBOMLine) targetComponent).getItem().getType().equals(TypeConstant.S7_PREPRODUCTTYPE))
//        {
//            TCComponentItemRevision targetRevision = ((TCComponentBOMLine) targetComponent).getItemRevision();
//
//            targetMap.put(PropertyConstant.ATTR_NAME_PROJCODE, targetRevision.getProperty(PropertyConstant.ATTR_NAME_PROJCODE));
//            targetMap.put(PropertyConstant.ATTR_NAME_GMODELCODE, targetRevision.getProperty(PropertyConstant.ATTR_NAME_GMODELCODE));
//        }

        PreFunctionCreateDialog dialog = new PreFunctionCreateDialog(AIFUtility.getActiveDesktop().getShell(), targetMap);
        dialog.open();
    }
}
