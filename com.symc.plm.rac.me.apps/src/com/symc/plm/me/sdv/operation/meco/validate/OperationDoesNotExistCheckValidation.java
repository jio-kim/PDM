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
 * 공정에 공법 생성 여부 체크
 * Class Name : OperationDoesNotExistCheckValidation
 * Class Description :
 *
 * @date 2014. 1. 29.
 * @author jwlee
 *
 */
public class OperationDoesNotExistCheckValidation extends OperationValidation<TCComponentBOMLine, String> {

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
            TCComponentBOPLine stationChilden = (TCComponentBOPLine) comp.getComponent();
            if (stationChilden.getItem().getType().equals(SDVTypeConstant.BOP_PROCESS_BODY_OPERATION_ITEM))
                return;
        }

        result = getMessage(ERROR_TYPE_OP_NOT_EXIST, itemId);
    }

}
