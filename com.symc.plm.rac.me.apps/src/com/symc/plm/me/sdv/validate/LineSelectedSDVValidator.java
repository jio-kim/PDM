/**
 * 
 */
package com.symc.plm.me.sdv.validate;

import java.util.Map;

import org.sdv.core.common.ISDVValidator;
import org.sdv.core.common.exception.SDVException;
import org.sdv.core.common.exception.ValidateSDVException;

import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.utils.CustomUtil;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.util.Registry;

/**
 * Class Name : LineSelectedSDVValidator
 * Class Description :
 * 
 * @date 2013. 12. 18.
 * 
 */
public class LineSelectedSDVValidator implements ISDVValidator {
    private Registry registry = Registry.getRegistry(LineSelectedSDVValidator.class);

    /**
     * Description :
     * 
     * @method :
     * @date : 2013. 12. 18.
     * @param :
     * @return :
     * @see org.sdv.core.common.ISDVValidator#validate(java.lang.String, java.util.Map)
     */
    @Override
    public void validate(String commandId, Map<String, Object> parameter, Object applicationCtx) throws SDVException {
        if (!(AIFUtility.getCurrentApplication() instanceof MFGLegacyApplication)) {
            // MPPApplication Check
            throw new ValidateSDVException(registry.getString("Common.SelectedMPPApp"));
        }
        InterfaceAIFComponent[] selectedTargets = CustomUtil.getCurrentApplicationTargets();
        if(selectedTargets.length == 0) {
            throw new ValidateSDVException(registry.getString("Common.SelectedComponent.MESSAGE").replace("%0", "LINE"));
        }
        if (selectedTargets.length > 1) {
            throw new ValidateSDVException(registry.getString("Common.SelectedOne.MESSAGE").replace("%0", "LINE"));
        }
        try {
            if ((!(selectedTargets[0] instanceof TCComponentBOMLine)) || (!((TCComponentBOMLine) selectedTargets[0]).getItem().getType().equals(SDVTypeConstant.BOP_PROCESS_LINE_ITEM))) {
                throw new ValidateSDVException(registry.getString("Common.SelectedComponent.MESSAGE").replace("%0", "LINE"));
            }
        } catch (ValidateSDVException ve) {
            throw ve;
        } catch (Exception e) {
            throw new ValidateSDVException(e.getMessage(), e);
        }
    }

}
