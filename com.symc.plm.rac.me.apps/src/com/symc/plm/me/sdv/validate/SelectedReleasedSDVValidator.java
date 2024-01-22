/**
 * 
 */
package com.symc.plm.me.sdv.validate;

import java.util.Map;

import org.sdv.core.common.ISDVValidator;
import org.sdv.core.common.exception.SDVException;
import org.sdv.core.common.exception.ValidateSDVException;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.utils.CustomUtil;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.kernel.TCAccessControlService;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMViewRevision;
import com.teamcenter.rac.util.Registry;

/**
 * Class Name : SelectedReleasedSDVValidator
 * Class Description :
 * 
 * @date 2013. 12. 26.
 * 
 */
public class SelectedReleasedSDVValidator implements ISDVValidator {
    private Registry registry = Registry.getRegistry(SelectedReleasedSDVValidator.class);

    @Override
    public void validate(String commandId, Map<String, Object> parameter, Object applicationCtx) throws SDVException {
        try {
            InterfaceAIFComponent[] selectedTargets = CustomUtil.getCurrentApplicationTargets();
            
            if(selectedTargets == null || selectedTargets.length == 0) {
                throw new ValidateSDVException(registry.getString("Common.SelectedComponent.MESSAGE").replace("%0", "selectTarget"));
            }
            
            for (InterfaceAIFComponent selectedTarget : selectedTargets) {
                // BOPLine일 경우 수정권한이 있는 지 체크함
                if (selectedTarget instanceof TCComponentBOMLine) {
                    TCComponentBOMLine selectedBOMLine = (TCComponentBOMLine) selectedTarget;
                    TCComponentBOMViewRevision bomViewRevision = SDVBOPUtilities.getBOMViewRevision(selectedBOMLine.getItemRevision(), "View");
                    if (bomViewRevision == null)
                        return;

                    TCAccessControlService aclService = selectedBOMLine.getSession().getTCAccessControlService();
                    boolean isWriteBOMLine = aclService.checkPrivilege(bomViewRevision, TCAccessControlService.WRITE);
                    String disPlayName = selectedBOMLine.toDisplayString();
                    if (!isWriteBOMLine)
                        throw new ValidateSDVException(registry.getString("NoWriteAcess.MESSAGE", "You do not have write access to").concat(" ").concat(disPlayName));

                } else {
                    if (CustomUtil.isInProcess((TCComponent) selectedTarget))
                        throw new ValidateSDVException(registry.getString("SelectReleasedInProgress.MESSAGE"));

                    if (CustomUtil.isReleased((TCComponent) selectedTarget))
                        throw new ValidateSDVException(registry.getString("SelectReleased.MESSAGE"));
                }
            }
        } catch (Exception ex) {
            throw new ValidateSDVException(ex.getMessage(), ex);
        }
    }
}
