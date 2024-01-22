/**
 * 
 */
package com.symc.plm.me.sdv.validate;

import java.util.Map;

import org.sdv.core.common.ISDVValidator;
import org.sdv.core.common.exception.SDVException;
import org.sdv.core.common.exception.ValidateSDVException;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVTypeConstant;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.kernel.TCAccessControlService;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMViewRevision;
import com.teamcenter.rac.util.Registry;

/**
 * Class Name : MoveAssyOPCheckSDVValidator
 * Class Description : 조립 공법 이동 시 Validation 체크
 * 
 * @date 2014. 2. 18.
 * 
 */
public class MoveAssyOPCheckSDVValidator implements ISDVValidator {

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
            TCComponentBOMLine topLine = mfgApp.getBOMWindow().getTopBOMLine();
            TCComponentBOMLine[] selectedBOMLines = mfgApp.getSelectedBOMLines();
            if (selectedBOMLines.length == 0) {
                throw new ValidateSDVException(registry.getString("Common.SelectedComponent.MESSAGE").replace("%0", "selectTarget"));
            }
            TCAccessControlService aclService = topLine.getSession().getTCAccessControlService();

            for (TCComponentBOMLine selectedBOMLine : selectedBOMLines) {

                TCComponentBOMLine lineBOPLine = selectedBOMLine.parent();
                TCComponentBOMViewRevision lineBomViewRevision = SDVBOPUtilities.getBOMViewRevision(lineBOPLine.getItemRevision(), "view");
                // Line BOMView 수정권한 여부
                boolean isWriteLine = aclService.checkPrivilege(lineBomViewRevision, TCAccessControlService.WRITE);
                // 공법 Revision 수정권한 여부
                boolean isWriteOPRevision = aclService.checkPrivilege(selectedBOMLine.getItemRevision(), TCAccessControlService.WRITE);

                String lineDisplayName = lineBOPLine.getItemRevision().toDisplayString();
                String opDisplayName = selectedBOMLine.getItemRevision().toDisplayString();
                
                // 공법 선택 여부 체크
                if (!selectedBOMLine.getItem().getType().equals(SDVTypeConstant.BOP_PROCESS_ASSY_OPERATION_ITEM))
                    throw new ValidateSDVException(registry.getString("SelectAssyOP.MESSAGE", "Please Select Assembly Operation.").concat(" ").concat(opDisplayName));                
                // Line BOMView 수정권한 여부 체크
                if (!isWriteLine)
                    throw new ValidateSDVException(registry.getString("NoWriteAcess.MESSAGE", "You do not have write access to").concat(" ").concat(lineDisplayName));
                // 공법 Revision 수정권한 여부 체크
                if (!isWriteOPRevision)
                    throw new ValidateSDVException(registry.getString("NoWriteAcess.MESSAGE", "You do not have write access to").concat(" ").concat(opDisplayName));               
            }

        } catch (Exception ex) {
            throw new ValidateSDVException(ex.getMessage(), ex);
        }
    }

}
