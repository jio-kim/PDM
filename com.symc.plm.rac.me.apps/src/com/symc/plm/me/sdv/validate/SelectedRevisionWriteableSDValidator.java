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
import com.teamcenter.rac.kernel.TCAccessControlService;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.util.Registry;

/**
 * Class Name : SelectedRevisionWriteableSDValidator
 * Class Description : 선택된 개체의 Item Revision 이 쓰기 권한이 있는 지 체크함
 * 
 * @date 2014. 2. 4.
 * 
 */
public class SelectedRevisionWriteableSDValidator implements ISDVValidator {

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.common.ISDVValidator#validate(java.lang.String, java.util.Map, java.lang.Object)
     */
    @Override
    public void validate(String commandId, Map<String, Object> parameter, Object applicationCtx) throws SDVException {
        Registry registry = Registry.getRegistry(this);

        try {
            InterfaceAIFComponent[] selectedTargets = CustomUtil.getCurrentApplicationTargets();

            if (selectedTargets.length == 0) {
                throw new ValidateSDVException(registry.getString("Common.SelectedComponent.MESSAGE").replace("%0", "selectTarget"));
            }
            for (InterfaceAIFComponent selectedTarget : selectedTargets) {
                TCComponentItemRevision selectedItemRevision = null;
                // BOPLine일 경우 수정권한이 있는 지 체크함
                if (selectedTarget instanceof TCComponentBOMLine) {
                    TCComponentBOMLine selectedBOMLine = (TCComponentBOMLine) selectedTarget;
                    selectedItemRevision = selectedBOMLine.getItemRevision();
                } else if (selectedTarget instanceof TCComponentItem) {
                    selectedItemRevision = ((TCComponentItem) selectedTarget).getLatestItemRevision();
                } else if (selectedTarget instanceof TCComponentItemRevision) {
                    selectedItemRevision = (TCComponentItemRevision) selectedTarget;
                }

                if (selectedItemRevision == null)
                    continue;

                TCAccessControlService aclService = selectedItemRevision.getSession().getTCAccessControlService();
                boolean isWriteRevision = aclService.checkPrivilege(selectedItemRevision, TCAccessControlService.WRITE);
                String disPlayName = selectedItemRevision.toDisplayString();
                if (!isWriteRevision)
                    throw new ValidateSDVException(registry.getString("NoWriteAcess.MESSAGE", "You do not have write access to").concat(" ").concat(disPlayName));
            }

        } catch (Exception ex) {
            throw new ValidateSDVException(ex.getMessage(), ex);
        }

    }

}
