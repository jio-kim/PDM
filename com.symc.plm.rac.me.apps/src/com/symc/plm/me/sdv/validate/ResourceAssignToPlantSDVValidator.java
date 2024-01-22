package com.symc.plm.me.sdv.validate;

import java.util.ArrayList;
import java.util.Map;

import org.sdv.core.common.ISDVValidator;
import org.sdv.core.common.exception.SDVException;
import org.sdv.core.common.exception.ValidateSDVException;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.sdv.service.resource.ResourceUtilities;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.util.Registry;

public class ResourceAssignToPlantSDVValidator implements ISDVValidator {

    private Registry registry = Registry.getRegistry(ResourceAssignToPlantSDVValidator.class);

    public ResourceAssignToPlantSDVValidator() {
    }

    @Override
    @SuppressWarnings("unchecked")
    public void validate(String commandId, Map<String, Object> parameter, Object applicationCtx) throws SDVException {
        registry = Registry.getRegistry(this);

        ArrayList<InterfaceAIFComponent> resourceItemsList = (ArrayList<InterfaceAIFComponent>) parameter.get("RESOURCE");
        TCComponentBOMLine targetComponent = (TCComponentBOMLine) parameter.get("BOPTARGET");

        if (resourceItemsList != null && targetComponent != null) {
            try {
                String prefName = "M7_AllowAssignEquipmentToAnyView";
                String preferenceValue = ResourceUtilities.getPreferenceValue(prefName);

                // Preference (M7_AllowAssignEquipmentToAnyView) 값이 false일 경우 차체 설비는 Plant에만 할당되도록 한다.
                if (preferenceValue == null || preferenceValue.equals("") || preferenceValue.equals("false")) {
                    // Target Item Type
                    boolean isMfg0BvrWorkarea = targetComponent.getType().equals("Mfg0BvrWorkarea");

                    // 할당할 자원 Type
                    for (int i = 0; i < resourceItemsList.size(); i++) {
                        InterfaceAIFComponent component = resourceItemsList.get(i);
                        TCComponentItemRevision itemRevision = (TCComponentItemRevision) component;
                        String itemId = itemRevision.getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
                        boolean isBodyItem = ResourceUtilities.getBOPType(itemId).equals("Body");
                        boolean isEquip = itemRevision.isTypeOf("M7_EquipmentRevision");
                        // boolean isTool = itemRevision.isTypeOf(SDVTypeConstant.BOP_PROCESS_TOOL_ITEM_REV);
                        // boolean isSubidiary = itemRevision.isTypeOf(SDVTypeConstant.BOP_PROCESS_SUBSIDIARY_ITEM_REV);

                        if (!isMfg0BvrWorkarea && isEquip && isBodyItem) {
                            throw new Exception(registry.getString("ResourceAssignToPlant.Body.MESSAGE"));
                        }
                    }
                }

            } catch (Exception ex) {
                throw new ValidateSDVException(ex.getMessage(), ex);
            }
        }
    }

}
