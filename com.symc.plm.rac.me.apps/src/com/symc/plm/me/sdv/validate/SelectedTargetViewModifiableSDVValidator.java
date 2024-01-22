/**
 * 
 */
package com.symc.plm.me.sdv.validate;

import java.util.Map;

import org.sdv.core.common.ISDVValidator;
import org.sdv.core.common.exception.SDVException;
import org.sdv.core.common.exception.ValidateSDVException;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.kernel.TCAccessControlService;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMViewRevision;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.util.Registry;

/**
 * Class Name : SelectedTargetViewModifiableSDVValidator
 * 
 * @date 2014. 1. 16.
 * 
 */
public class SelectedTargetViewModifiableSDVValidator implements ISDVValidator {

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.common.ISDVValidator#validate(java.lang.String, java.util.Map, java.lang.Object)
     */
    @Override
    public void validate(String commandId, Map<String, Object> parameter, Object applicationCtx) throws SDVException {
        Registry registry = Registry.getRegistry(this);
        
        try {
        	MFGLegacyApplication mfgApp = (MFGLegacyApplication) AIFUtility.getCurrentApplication();

            TCComponentBOMLine selectedBOMLine = mfgApp.getSelectedBOMLines()[0];
            TCComponentItemRevision selectedItemRevision = selectedBOMLine.getItemRevision();

            TCComponentBOMViewRevision bomViewRevision = SDVBOPUtilities.getBOMViewRevision(selectedItemRevision, "View");
            if (bomViewRevision == null)
                return;

            TCAccessControlService aclService = selectedBOMLine.getSession().getTCAccessControlService();
            boolean isWriteBOMLine = aclService.checkPrivilege(bomViewRevision, TCAccessControlService.WRITE);
            if (! isWriteBOMLine)
            {
            	String disPlayName = selectedBOMLine.toDisplayString();
                throw new ValidateSDVException(registry.getString("NoWriteAcess.MESSAGE", "You do not have write access to").concat(" ").concat(disPlayName));
            }

        } catch (Exception ex) {
            throw new ValidateSDVException(ex.getMessage(), ex);
        }

    }
}
