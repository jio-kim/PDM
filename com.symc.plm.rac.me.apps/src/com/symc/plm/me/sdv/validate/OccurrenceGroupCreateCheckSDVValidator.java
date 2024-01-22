/**
 *
 */
package com.symc.plm.me.sdv.validate;

import java.util.Map;

import org.sdv.core.common.ISDVValidator;
import org.sdv.core.common.exception.SDVException;
import org.sdv.core.common.exception.ValidateSDVException;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.utils.CustomUtil;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.util.Registry;

/**
 * Class Name : StationSelectedSDVValidator
 * Class Description :
 *
 * @date 2013. 12. 18.
 *
 */
public class OccurrenceGroupCreateCheckSDVValidator implements ISDVValidator {
    private Registry registry = Registry.getRegistry(OccurrenceGroupCreateCheckSDVValidator.class);

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
        try {
            InterfaceAIFComponent[] selectedTargets = CustomUtil.getCurrentApplicationTargets();
            TCComponentBOPLine bopLine = null;
            TCComponentBOPLine bopTopLine = null;
            for (InterfaceAIFComponent selectedTarget : selectedTargets)
            {
                bopLine = (TCComponentBOPLine)selectedTarget;
                bopTopLine = (TCComponentBOPLine) bopLine.window().getTopBOMLine();
            }
            if (bopTopLine.getReferenceListProperty(SDVPropertyConstant.BL_MFG0ASSIGNED_MATERIAL) == null || bopTopLine.getReferenceListProperty(SDVPropertyConstant.BL_MFG0ASSIGNED_MATERIAL).equals(""))
                throw new ValidateSDVException(registry.getString("OccurrenceGroupCreateCheck.NotCreate.MESSAGE"));
        } catch (ValidateSDVException ve) {
            throw ve;
        } catch (Exception e) {
            throw new ValidateSDVException(e.getMessage(), e);
        }

    }

}
