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
 * 용접 공법에 MERESOURCE 있는지 여부 체크
 * Class Name : WeldOperationMEResourceCheckValidation
 * Class Description :
 *
 * @date 2014. 1. 28.
 * @author jwlee
 *
 */
public class WeldOperationMEResourceCheckValidation extends OperationValidation<TCComponentBOMLine, String> {

    /*
     * (non-Javadoc)
     *
     * @see com.symc.plm.me.sdv.operation.meco.validate.WeldPointAssignValidation#executeValidation()
     */
    @Override
    protected void executeValidation() throws Exception {

        StringBuilder errorMsgBuilder = new StringBuilder();
        String errorMsg = ""; // 에러 메세지
        String itemId = target.getProperty(SDVPropertyConstant.BL_ITEM_ID);
        AIFComponentContext[] comps = target.getChildren();
        for (AIFComponentContext comp : comps) {
            TCComponentBOPLine weldOPChilden = (TCComponentBOPLine) comp.getComponent();
            if (weldOPChilden.getProperty(SDVPropertyConstant.BL_OCC_TYPE).equals(SDVTypeConstant.BOP_PROCESS_OCCURRENCE_RESOURCE)){
                errorMsg = getMessage(ERROR_TYPE_WP_MERESOURCE_CHECK, itemId, weldOPChilden.getProperty(SDVPropertyConstant.BL_ITEM_ID));
                errorMsgBuilder.append(errorMsg);
            }
        }
        if (errorMsgBuilder.length() > 0)
            result = errorMsgBuilder.toString();
    }

}
