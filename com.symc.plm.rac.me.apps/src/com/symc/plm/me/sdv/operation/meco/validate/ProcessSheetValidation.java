/**
 * 
 */
package com.symc.plm.me.sdv.operation.meco.validate;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;

/**
 * 작업표준서 존재 여부 체크
 * Class Name : ProcessSheetValidation
 * Class Description :
 * 
 * @date 2013. 12. 11.
 * 
 */
public class ProcessSheetValidation extends OperationValidation<TCComponentBOMLine, String> {

    /*
     * (non-Javadoc)
     * 
     * @see com.symc.plm.me.sdv.operation.meco.validate.OperationValidation#executeValidation()
     */
    @Override
    protected void executeValidation() throws Exception {

        TCComponent[] comps = target.getItemRevision().getRelatedComponents(SDVTypeConstant.PROCESS_SHEET_KO_RELATION);
        String itemId = target.getProperty(SDVPropertyConstant.BL_ITEM_ID);
        if (comps.length != 0)
            return;

        result = getMessage(ERROR_TYPE_PS_NOT_EXIST, itemId);
    }

}
