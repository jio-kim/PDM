package com.symc.plm.me.sdv.validate;

import java.util.ArrayList;
import java.util.Map;

import org.sdv.core.common.ISDVValidator;
import org.sdv.core.common.exception.SDVException;
import org.sdv.core.common.exception.ValidateSDVException;

import com.symc.plm.me.common.SDVTypeConstant;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.util.Registry;

public class SelectedResourceSDVValidator implements ISDVValidator {

    private Registry registry = Registry.getRegistry(SelectedResourceSDVValidator.class);

    public SelectedResourceSDVValidator() {
    }

    @Override
    @SuppressWarnings("unchecked")
    public void validate(String commandId, Map<String, Object> parameter, Object applicationCtx) throws SDVException {
        registry = Registry.getRegistry(this);

        ArrayList<InterfaceAIFComponent> resourceItemsList = (ArrayList<InterfaceAIFComponent>) parameter.get("RESOURCE");
        TCComponentBOMLine targetComponent = (TCComponentBOMLine) parameter.get("BOPTARGET");

        try {
            if (resourceItemsList != null && targetComponent != null) {
                
                // Target Item Type이 자원이면 오류 처리
                String itemType = targetComponent.getItem().getType();
                if (itemType.equals(SDVTypeConstant.BOP_PROCESS_GENERALEQUIP_ITEM) || itemType.equals(SDVTypeConstant.BOP_PROCESS_GUN_ITEM) || itemType.equals(SDVTypeConstant.BOP_PROCESS_JIGFIXTURE_ITEM) || itemType.equals(SDVTypeConstant.BOP_PROCESS_ROBOT_ITEM) || itemType.equals(SDVTypeConstant.BOP_PROCESS_SUBSIDIARY_ITEM) || itemType.equals(SDVTypeConstant.BOP_PROCESS_TOOL_ITEM)) {
                    throw new Exception(registry.getString("SelectedResource.Resource.MESSAGE"));
                }
            }
        } catch (Exception ex) {
            throw new ValidateSDVException(ex.getMessage(), ex);
        }
    }
}
