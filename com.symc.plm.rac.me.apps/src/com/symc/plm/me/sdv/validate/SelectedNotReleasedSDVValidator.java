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
 */
public class SelectedNotReleasedSDVValidator implements ISDVValidator {
    private Registry registry = Registry.getRegistry(SelectedNotReleasedSDVValidator.class);

    @Override
    public void validate(String commandId, Map<String, Object> parameter, Object applicationCtx) throws SDVException {
        try {
            InterfaceAIFComponent[] selectedTargets = CustomUtil.getCurrentApplicationTargets();

            if(selectedTargets == null || selectedTargets.length == 0) {
                throw new ValidateSDVException(registry.getString("Common.SelectedComponent.MESSAGE").replace("%0", "selectTarget"));
            }
            
            for (InterfaceAIFComponent selectedTarget : selectedTargets) {
                TCComponent selectedComponent = null;
                if (selectedTarget instanceof TCComponentBOMLine) {
                    TCComponentBOMLine selectedBOMLine = (TCComponentBOMLine) selectedTarget;
                    selectedComponent = (TCComponent) selectedBOMLine.getItemRevision();
                } else {
                    selectedComponent = (TCComponent) selectedTarget;
                }
                
                if (selectedComponent != null && !CustomUtil.isReleased(selectedComponent)) {
                    throw new ValidateSDVException(registry.getString("SelectNotReleased.MESSAGE"));
                }
            }
        } catch (Exception ex) {
            throw new ValidateSDVException(ex.getMessage(), ex);
        }
    }
}
