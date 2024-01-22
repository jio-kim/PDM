/**
 *
 */
package com.symc.plm.me.sdv.operation.meco.validate;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOPLine;

/**
 * 용접 공법에 용접점 존재 여부 체크
 * Class Name : WeldPointAssignValidation
 * Class Description :
 *
 * @date 2013. 12. 18.
 * @author jwlee
 *
 */
public class WeldPointAssignValidation extends OperationValidation<TCComponentBOMLine, String> {

    /*
     * (non-Javadoc)
     *
     * @see com.symc.plm.me.sdv.operation.meco.validate.WeldPointAssignValidation#executeValidation()
     */
    @Override
    protected void executeValidation() throws Exception {

        String itemId = target.getProperty(SDVPropertyConstant.BL_ITEM_ID);
        AIFComponentContext[] comps = target.getChildren();
        for (AIFComponentContext comp : comps) {
            TCComponentBOPLine weldOPChilden = (TCComponentBOPLine) comp.getComponent();
            if (weldOPChilden.getItem().getType().equals(SDVTypeConstant.BOP_BODY_WELD_POINT_ITEM))
                return;
        }

        result = getMessage(ERROR_TYPE_WP_NOTASSIGNED, itemId);
    }

}
