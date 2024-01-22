/**
 * 
 */
package com.symc.plm.me.sdv.validate;

import java.util.Map;

import org.sdv.core.common.ISDVValidator;
import org.sdv.core.common.exception.SDVException;
import org.sdv.core.common.exception.ValidateSDVException;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.kernel.TCAccessControlService;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMViewRevision;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.util.Registry;

/**
 * Class Name : SelectedAssyBOPReleasedSDVValidator
 * Class Description : 조립 공법일 경우 Line을 선택할 경우 Line에 대한 수정 권한이 있는 체크를 한다.(단, 미할당 라인일 경우는 제외함.자동Revise됨)
 * 
 * @date 2014. 1. 16.
 * 
 */
public class SelectedAssyLineModifiableSDVValidator implements ISDVValidator {

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.common.ISDVValidator#validate(java.lang.String, java.util.Map, java.lang.Object)
     */
    @Override
    public void validate(String commandId, Map<String, Object> parameter, Object applicationCtx) throws SDVException {
        Registry registry = Registry.getRegistry(this);
        MFGLegacyApplication mfgApp = (MFGLegacyApplication) AIFUtility.getCurrentApplication();

        try {

            TCComponentBOMLine selectedBOMLine = mfgApp.getSelectedBOMLines()[0];
            TCComponentItemRevision selectedItemRevision = selectedBOMLine.getItemRevision();
            if (!selectedItemRevision.getType().equals(SDVTypeConstant.BOP_PROCESS_LINE_ITEM_REV))
                return;
            String itemId = selectedBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
            // Line을 선택하였을 경우, TEMP Line인 경우 제외
            if (itemId.toUpperCase().indexOf("TEMP") > 0)
                return;

            TCComponentBOMViewRevision bomViewRevision = SDVBOPUtilities.getBOMViewRevision(selectedItemRevision, "view");
            if (bomViewRevision == null)
                return;

            TCAccessControlService aclService = selectedBOMLine.getSession().getTCAccessControlService();
            boolean isWriteBOMLine = aclService.checkPrivilege(bomViewRevision, TCAccessControlService.WRITE);
            String disPlayName = selectedBOMLine.toDisplayString();
            if (!isWriteBOMLine)
                throw new ValidateSDVException(registry.getString("NoWriteAcess.MESSAGE", "You do not have write access to").concat(" ").concat(disPlayName));

        } catch (Exception ex) {
            throw new ValidateSDVException(ex.getMessage(), ex);
        }

    }
}
