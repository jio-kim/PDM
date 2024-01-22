package com.symc.plm.me.sdv.validate;

import java.util.ArrayList;
import java.util.Map;

import org.sdv.core.common.ISDVValidator;
import org.sdv.core.common.exception.SDVException;
import org.sdv.core.common.exception.ValidateSDVException;

import com.symc.plm.me.common.SDVTypeConstant;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.util.Registry;

public class ResourceSubsidiaryAssignSDVValidator implements ISDVValidator {

    private Registry registry = Registry.getRegistry(ResourceSubsidiaryAssignSDVValidator.class);

    public ResourceSubsidiaryAssignSDVValidator() {
    }

    @Override
    @SuppressWarnings("unchecked")
    public void validate(String commandId, Map<String, Object> parameter, Object applicationCtx) throws SDVException {
        registry = Registry.getRegistry(this);

        ArrayList<InterfaceAIFComponent> resourceItemsList = (ArrayList<InterfaceAIFComponent>) parameter.get("RESOURCE");
        TCComponentBOMLine targetComponent = (TCComponentBOMLine) parameter.get("BOPTARGET");

        if (resourceItemsList != null && targetComponent != null) {
            try {
                //부자재 Validation (BOPOperation에만 할당)
                String targetType = targetComponent.getItem().getType();

                if (!targetType.equals(SDVTypeConstant.BOP_PROCESS_ASSY_OPERATION_ITEM) && !targetType.equals(SDVTypeConstant.BOP_PROCESS_BODY_OPERATION_ITEM) && !targetType.equals(SDVTypeConstant.BOP_PROCESS_PAINT_OPERATION_ITEM)) {
                    for (InterfaceAIFComponent aifComponent : resourceItemsList) {
                        TCComponent resourceComponent = (TCComponent) aifComponent;
                        String resourceItemId = resourceComponent.getProperty("item_id");
                        String resourceType = resourceComponent.getType();
                        if (resourceType.equals(SDVTypeConstant.BOP_PROCESS_SUBSIDIARY_ITEM_REV)) {
                            throw new ValidateSDVException(registry.getString("ResourceSubsidiaryAssign.Operation.MESSAGE").replace("%0", resourceItemId));
                        }
                    }
                }
            } catch (Exception ex) {
                throw new ValidateSDVException(ex.getMessage(), ex);
            }
        }
    }

}
