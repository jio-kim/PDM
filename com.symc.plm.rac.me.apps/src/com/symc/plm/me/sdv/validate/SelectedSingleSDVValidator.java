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
import com.teamcenter.rac.util.Registry;

/**
 * Class Name : SelectedSingleSDVValidator
 * Class Description :
 * 
 * @date 2014. 2. 4.
 * 
 */
public class SelectedSingleSDVValidator implements ISDVValidator {
    private Registry registry = Registry.getRegistry(SelectedSingleSDVValidator.class);

    @Override
    public void validate(String commandId, Map<String, Object> parameter, Object applicationCtx) throws SDVException {
        try {
            InterfaceAIFComponent[] selectedTargets = CustomUtil.getCurrentApplicationTargets();
            
            if(selectedTargets.length == 0) {
                throw new ValidateSDVException(registry.getString("Common.SelectedComponent.MESSAGE").replace("%0", "selectTarget"));
            }            
            if (selectedTargets.length > 1) {
                throw new ValidateSDVException(registry.getString("Common.SelectedOne.MESSAGE").replace("%0", "selectTarget"));
            }     
            
        } catch (Exception ex) {
            throw new ValidateSDVException(ex.getMessage(), ex);
        }
    }
}
