/**
 *
 */
package com.symc.plm.me.sdv.validate;

import java.util.List;
import java.util.Map;

import org.sdv.core.common.ISDVValidator;
import org.sdv.core.common.exception.SDVException;
import org.sdv.core.common.exception.ValidateSDVException;

import com.symc.plm.me.utils.CustomUtil;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.util.Registry;
import com.google.common.base.Joiner;

/**
 * Class Name : LineSelectedSDVValidator Class Description :
 *
 * @date 2013. 12. 18.
 *
 */
public class SelectedTypesSDVValidator implements ISDVValidator {

    private static final Registry registry = Registry.getRegistry(SelectedTypesSDVValidator.class);

    private List<String> types;

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

        // Type 유형을 지정하지 않았다면 검증처리를 하지 않는다.
        if (types == null || types.size() == 0) {
            return;
        }

        String typeList = Joiner.on(", ").join(types);

        if (!(AIFUtility.getCurrentApplication() instanceof MFGLegacyApplication)) {
            // MPPApplication Check
            throw new ValidateSDVException(registry.getString("Common.SelectedMPPApp"));
        }

        InterfaceAIFComponent[] selectedTargets = CustomUtil.getCurrentApplicationTargets();
System.out.println("SelectedTypesSDVValidator.validate() - selected count[" + (selectedTargets != null ? selectedTargets.length : 0) + "]");

        if (selectedTargets.length == 0) {
            throw new ValidateSDVException(registry.getString("Common.SelectedComponent.MESSAGE").replace("%0", typeList));
        }
        if (selectedTargets.length > 1) {
            throw new ValidateSDVException(registry.getString("Common.SelectedOne.MESSAGE").replace("%0", typeList));
        }

        try {
            if (selectedTargets[0] instanceof TCComponentBOMLine) {
                TCComponentItem selectedItem = ((TCComponentBOMLine) selectedTargets[0]).getItem();
                for (String type : types) {
                    if (selectedItem.getType().equals(type)) {
                        return;
                    }
System.out.println("SelectedTypesSDVValidator.validate() - check type[" + type + "] item type [" + selectedItem.getType() + "]");
                }
            }
            throw new ValidateSDVException(registry.getString("Common.SelectedComponent.MESSAGE").replace("%0", typeList));
        } catch (ValidateSDVException ve) {
            throw ve;
        } catch (Exception e) {
            throw new ValidateSDVException(e.getMessage(), e);
        }
    }

    /**
     * @return the types
     */
    public List<String> getTypes() {
        return types;
    }

    /**
     * @param types
     *            the types to set
     */
    public void setTypes(List<String> types) {
        this.types = types;
    }

}
