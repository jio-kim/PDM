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
 * [SR150415-005][20150518] shcho, 용접점 할당 오류 검증 기능 추가를 위한 Class 신규 생성
 * (용접점이 할당되지 않아야 할 곳에 할당 되었을 경우, 에러 메시지를 띄운다.)
 * 
 * Class Name : WeldPointAssignValidation2
 * Class Description :
 *
 *
 */
public class WeldPointAssignValidation2 extends OperationValidation<TCComponentBOMLine, String> {

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
            TCComponentBOPLine childBOMline = (TCComponentBOPLine) comp.getComponent();
            if (childBOMline.getItem().getType().equals(SDVTypeConstant.BOP_BODY_WELD_POINT_ITEM)) {
                result = getMessage(ERROR_TYPE_WP_ASSIGNED, itemId);
            }
        }
    }

}
