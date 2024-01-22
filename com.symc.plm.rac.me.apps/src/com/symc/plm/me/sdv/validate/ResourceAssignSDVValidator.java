package com.symc.plm.me.sdv.validate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import org.sdv.core.common.ISDVValidator;
import org.sdv.core.common.exception.SDVException;
import org.sdv.core.common.exception.ValidateSDVException;
import org.sdv.core.util.SDVSpringContextUtil;

import com.symc.plm.me.sdv.service.resource.ResourceUtilities;
import com.symc.plm.me.sdv.viewpart.resource.ResourceSearchViewPart;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.util.Registry;

public class ResourceAssignSDVValidator implements ISDVValidator {

    private Registry registry = Registry.getRegistry(ResourceAssignSDVValidator.class);
    private ArrayList<InterfaceAIFComponent> resourceItemsList = null;
    private TCComponentBOMLine targetComponent;

    public ResourceAssignSDVValidator() {
    }

    @Override
    public void validate(String commandId, Map<String, Object> parameter, Object applicationCtx) throws SDVException {
        registry = Registry.getRegistry(this);

        // ResourceViewPart
        ResourceSearchViewPart resourceSearchViewPart = ResourceUtilities.getResourceSearchViewPart();
        if (resourceSearchViewPart == null) {
            throw new ValidateSDVException(registry.getString("ResourceAssign.Viewpart.MESSAGE"));
        }

        // ResourceViewPart의 선택된 자원
        InterfaceAIFComponent[] targetResourceItems = resourceSearchViewPart.getCurrentTable().getSelectedItems();
        resourceItemsList = new ArrayList<InterfaceAIFComponent>(Arrays.asList(targetResourceItems));

        // MPPAppication BOP의 선택된 Target
        // AbstractAIFUIApplication application = AIFUtility.getCurrentApplication();
        // targetComponent = (TCComponentBOMLine) application.getTargetComponent();
        // InterfaceAIFComponent ainterfaceaifcomponent[] = AifrcpPlugin.getSelectionMediatorService().getTargetComponents();
        // InterfaceAIFComponent ainterfaceaifcomponent[] = AIFUtility.getCurrentApplication().getTargetComponents();
        AIFComponentContext[] targetContexts = AIFUtility.getCurrentApplication().getTargetContexts();

        // 자원 선택 여부, target BOP 선택 여부 체크 Validation
        if (resourceItemsList == null || resourceItemsList.size() <= 0) {
            throw new ValidateSDVException(registry.getString("ResourceAssign.Resource.MESSAGE"));
        }
        if (targetContexts == null || targetContexts.length <= 0) {
            throw new ValidateSDVException(registry.getString("ResourceAssign.BOMLine.MESSAGE"));
        }

        // 자원과 target BOP가 선택되어 있으면 Validation 수행
        targetComponent = (TCComponentBOMLine) targetContexts[0].getComponent();
        parameter.put("RESOURCE", resourceItemsList);
        parameter.put("BOPTARGET", targetComponent);

        // 할당 대상 Validation 1 (자원에 자원을 할당하지 않는다.)
        SelectedResourceSDVValidator selectedResourceSDVValidator = (SelectedResourceSDVValidator) SDVSpringContextUtil.getBean("com.symc.plm.me.sdv.validate.SelectedResourceSDVValidator");
        selectedResourceSDVValidator.validate(commandId, parameter, applicationCtx);

        // 할당 대상 Validation 2 (차체 설비는 Plant에만 할당되도록 한다.)
        ResourceAssignToPlantSDVValidator resourceAssignToPlantSDVValidator = (ResourceAssignToPlantSDVValidator) SDVSpringContextUtil.getBean("com.symc.plm.me.sdv.validate.ResourceAssignToPlantSDVValidator");
        resourceAssignToPlantSDVValidator.validate(commandId, parameter, applicationCtx);

        // 동일 자원(ID) 중복 할당 불가 Validation
        ResourceDuplicatedAssignSDVValidator resourceDuplicatedAssignSDVValidator = (ResourceDuplicatedAssignSDVValidator) SDVSpringContextUtil.getBean("com.symc.plm.me.sdv.validate.ResourceDuplicatedAssignSDVValidator");
        resourceDuplicatedAssignSDVValidator.validate(commandId, parameter, applicationCtx);

        // 공구 Validation (BOPOperation에만 할당)
        ResourceToolAssignSDVValidator resourceToolAssignSDVValidator = (ResourceToolAssignSDVValidator) SDVSpringContextUtil.getBean("com.symc.plm.me.sdv.validate.ResourceToolAssignSDVValidator");
        resourceToolAssignSDVValidator.validate(commandId, parameter, applicationCtx);

        // 부자재 Validation (BOPOperation에만 할당)
        ResourceSubsidiaryAssignSDVValidator resourceSubsidiaryAssignSDVValidator = (ResourceSubsidiaryAssignSDVValidator) SDVSpringContextUtil.getBean("com.symc.plm.me.sdv.validate.ResourceSubsidiaryAssignSDVValidator");
        resourceSubsidiaryAssignSDVValidator.validate(commandId, parameter, applicationCtx);

        // 차체 Robot Validation (Plant WorkArea에만 할당, Robot은 Plant WorkArea에 하나만 존재)
        ResourceRobotAssignSDVValidator resourceRobotAssignSDVValidator = (ResourceRobotAssignSDVValidator) SDVSpringContextUtil.getBean("com.symc.plm.me.sdv.validate.ResourceRobotAssignSDVValidator");
        resourceRobotAssignSDVValidator.validate(commandId, parameter, applicationCtx);

        // 차체 Gun Validation (Plant WorkArea에만 할당)
        ResourceGunAssignSDVValidator resourceGunAssignSDVValidator = (ResourceGunAssignSDVValidator) SDVSpringContextUtil.getBean("com.symc.plm.me.sdv.validate.ResourceGunAssignSDVValidator");
        resourceGunAssignSDVValidator.validate(commandId, parameter, applicationCtx);

        // 차체 JIG Validation (Plant Station에만 차체 Jig 할당)
        ResourceJigAssignSDVValidator resourceJigAssignSDVValidator = (ResourceJigAssignSDVValidator) SDVSpringContextUtil.getBean("com.symc.plm.me.sdv.validate.ResourceJigAssignSDVValidator");
        resourceJigAssignSDVValidator.validate(commandId, parameter, applicationCtx);
    }

}
