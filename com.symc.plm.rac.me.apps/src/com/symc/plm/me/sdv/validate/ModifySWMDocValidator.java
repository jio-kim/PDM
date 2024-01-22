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
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.Registry;

/**
 * Class Name : ModifySWMDocValidator
 * Class Description :
 * 
 * @date 2013. 12. 31.
 * 
 */
public class ModifySWMDocValidator implements ISDVValidator {
    private Registry registry = Registry.getRegistry(ModifySWMDocValidator.class);

    @Override
    public void validate(String commandId, Map<String, Object> parameter, Object applicationCtx) throws SDVException {
        TCSession session = CustomUtil.getTCSession();
        try {
            InterfaceAIFComponent[] selectedTargets = CustomUtil.getCurrentApplicationTargets();
            for (InterfaceAIFComponent selectedTarget : selectedTargets) {
                if (!selectedTarget.getType().equals(SDVTypeConstant.STANDARD_WORK_METHOD_ITEM_REV)) {
                    throw new ValidateSDVException(registry.getString("SelectTargetIsNotSWMDoc.MESSAGE"));
                }

                TCComponentItemRevision selectedTargetRev = (TCComponentItemRevision) selectedTarget;
                selectedTargetRev.refresh();

                String checkOutUser = selectedTarget.getProperty("checked_out_user");
                if (!session.getUser().toString().equals(checkOutUser) && !checkOutUser.trim().equals("")) {
                    throw new ValidateSDVException(registry.getString("SelectTargetIsWorking.MESSAGE"));
                }
            }
        } catch (Exception ex) {
            throw new ValidateSDVException(ex.getMessage(), ex);
        }
    }
}
