package com.symc.plm.me.sdv.validate;

import java.util.ArrayList;
import java.util.Map;

import org.sdv.core.common.ISDVValidator;
import org.sdv.core.common.exception.SDVException;
import org.sdv.core.common.exception.ValidateSDVException;

import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.Registry;

public class ResourceDuplicatedAssignSDVValidator implements ISDVValidator {

    private Registry registry = Registry.getRegistry(ResourceDuplicatedAssignSDVValidator.class);

    public ResourceDuplicatedAssignSDVValidator() {
    }

    @Override
    @SuppressWarnings("unchecked")
    public void validate(String commandId, Map<String, Object> parameter, Object applicationCtx) throws SDVException {
        registry = Registry.getRegistry(this);
        
        ArrayList<InterfaceAIFComponent> resourceItemsList = (ArrayList<InterfaceAIFComponent>)parameter.get("RESOURCE");
        TCComponentBOMLine targetComponent = (TCComponentBOMLine)parameter.get("BOPTARGET");

        if (resourceItemsList != null  && targetComponent != null ) {
            try {      
                for (InterfaceAIFComponent aifComponent : resourceItemsList) {
                    TCComponent resourceComponent = (TCComponent) aifComponent;
                    String resourceItemId = resourceComponent.getProperty("item_id");
                    if (compareResourceItemId(resourceItemId, targetComponent)) {
                        throw new ValidateSDVException(registry.getString("ResourceDuplicated.Duplicate.MESSAGE").replace("%0", resourceItemId));
                    }
                }               
            } catch (Exception ex) {
                throw new ValidateSDVException(ex.getMessage(), ex);
            }
        }
    }

    
    /**
     * 할당할 자원이, 대상에 이미 할당 되어있는지 ID 비교 하는 함수
     * 
     * @param resourceItemId
     * @throws TCException
     */
    public boolean compareResourceItemId(String resourceItemId, TCComponentBOMLine targetComponent) throws TCException {
        boolean isDuplicate = false;
        AIFComponentContext[] aifComponentContexts = targetComponent.getChildren();

        for (AIFComponentContext aifComponentContext : aifComponentContexts) {
            TCComponentBOMLine childComponent = (TCComponentBOMLine) aifComponentContext.getComponent();
            String childItemId = childComponent.getItem().getProperty("item_id");

            if (childItemId.equals(resourceItemId)) {
                isDuplicate = true;
                break;
            }
        }

        return isDuplicate;
    }

}
