/**
 *
 */
package com.symc.plm.me.sdv.operation.meco.validate;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.TCComponentBOMLine;

/**
 * 공법또는 용접공법에 할당이 끈어진 대상 찾기
 *
 * Class Name : CheckUnlinkValidation
 * Class Description :
 *
 * @date 2013. 12. 19.
 * @author jwlee
 *
 */
public class CheckUnlinkValidation extends OperationValidation<TCComponentBOMLine, String> {

    /*
     * (non-Javadoc)
     *
     * @see com.symc.plm.me.sdv.operation.meco.validate.NotFoundValidation#executeValidation()
     */
    @Override
    protected void executeValidation() throws Exception {

        StringBuilder errorMsgBuilder = new StringBuilder();
        AIFComponentContext[] comps = target.getChildren();
        String targetID = target.getProperty(SDVPropertyConstant.BL_ITEM_ID);
        for (AIFComponentContext comp : comps) {
            TCComponentBOMLine Childen = (TCComponentBOMLine) comp.getComponent();
            String errorMsg = ""; // 에러 메세지
            //boolean result = Childen.hasAppearanceLinks(SDVPropertyConstant.BL_OCC_ASSIGNED);
            if (Childen.getProperty(SDVPropertyConstant.BL_OCC_ASSIGNED).equals("Not Found")){
                String itemId = Childen.getProperty(SDVPropertyConstant.BL_ITEM_ID);
                errorMsg = getMessage(ERROR_TYPE_CHECK_UNLINK, targetID + "(" + itemId + ")");
                errorMsgBuilder.append(errorMsg);
            }
        }

        if (errorMsgBuilder.length() > 0)
            result = errorMsgBuilder.toString();
    }

}
