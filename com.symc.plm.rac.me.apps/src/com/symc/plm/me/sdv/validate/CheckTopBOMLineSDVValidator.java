/**
 * 
 */
package com.symc.plm.me.sdv.validate;

import java.util.Map;

import org.sdv.core.common.ISDVValidator;
import org.sdv.core.common.exception.SDVException;
import org.sdv.core.common.exception.ValidateSDVException;

import com.symc.plm.me.common.SDVTypeConstant;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.util.Registry;

/**
 * Class Name : CheckTopBOMLineSDVValidator
 * Class Description :
 * 
 * @date 2014. 3. 4.
 * 
 */
public class CheckTopBOMLineSDVValidator implements ISDVValidator {
    private Registry registry = Registry.getRegistry(CheckTopBOMLineSDVValidator.class);

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

        // MPPAppication
        MFGLegacyApplication mfgApp = (MFGLegacyApplication) AIFUtility.getCurrentApplication();
        
        // ÇöÀç BOM WINDOW
        TCComponentBOMWindow bomWindow = mfgApp.getBOMWindow();
        if (bomWindow == null) {
            throw new ValidateSDVException("No BOP Structure Loaded.");
        }
        
        try {
            String topItemType = bomWindow.getTopBOMLine().getItem().getType();
            boolean isEnableType = topItemType.equals(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM);
            if (!isEnableType) {
                throw new ValidateSDVException("Shop BOP Structure not Loaded.");
            }
        } catch (ValidateSDVException ve) {
            throw ve;
        } catch (Exception e) {
            throw new ValidateSDVException(e.getMessage(), e);
        }
    }

}
