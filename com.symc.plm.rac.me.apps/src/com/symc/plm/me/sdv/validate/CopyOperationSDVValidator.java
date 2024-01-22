/**
 * 
 */
package com.symc.plm.me.sdv.validate;

import java.util.Map;

import org.sdv.core.common.ISDVValidator;
import org.sdv.core.common.exception.SDVException;
import org.sdv.core.common.exception.ValidateSDVException;

import com.symc.plm.me.utils.CustomUtil;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.util.Registry;

/**
 * Class Name : SelectedReleasedSDVValidator
 * Class Description :
 * 
 * @date 2013. 12. 26.
 * 
 * [SR150323-035][20150506]shcho, 차체 공법 복사 기능 개선 (Parent Release 여부 체크 Validator 추가)
 * 
 */
public class CopyOperationSDVValidator implements ISDVValidator {
    private Registry registry = Registry.getRegistry(CopyOperationSDVValidator.class);

    @Override
    public void validate(String commandId, Map<String, Object> parameter, Object applicationCtx) throws SDVException {
        try {
            InterfaceAIFComponent[] selectedTargets = CustomUtil.getCurrentApplicationTargets();

            if(selectedTargets == null || selectedTargets.length == 0) {
                throw new ValidateSDVException(registry.getString("Common.SelectedComponent.MESSAGE").replace("%0", "selectTarget"));
            }
            
            for (InterfaceAIFComponent selectedTarget : selectedTargets) {
                TCComponent targetComponent = null;
                if (selectedTarget instanceof TCComponentBOMLine) {
                    TCComponentBOMLine selectedBOMLine = (TCComponentBOMLine) selectedTarget;
                    targetComponent = (TCComponent) selectedBOMLine.parent().getItemRevision();
                } 
                
                if (targetComponent != null && CustomUtil.isReleased(targetComponent)) {
                	throw new ValidateSDVException("Parent was released.".concat(" ").concat(targetComponent.toDisplayString()));
                }
            }
        } catch (Exception ex) {
            throw new ValidateSDVException(ex.getMessage(), ex);
        }
    }
}
