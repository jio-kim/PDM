/**
 *
 */
package com.symc.plm.me.sdv.operation.meco.validate;

import java.util.Date;
import java.util.Vector;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVStringUtiles;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.utils.CustomUtil;
import com.symc.plm.me.utils.TcDefinition;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItemRevision;

/**
 * 용접 조건표 존재 여부 체크 / 용접조건표 수정 필요 여부 체크
 * Class Name : WeldConditionSheetValidation
 * Class Description :
 *
 * @date 2013. 12. 18.
 * @author jwlee
 *
 */
public class WeldConditionSheetValidation extends OperationValidation<TCComponentBOMLine, String> {

    /*
     * (non-Javadoc)
     *
     * @see com.symc.plm.me.sdv.operation.meco.validate.WeldConditionSheetValidation#executeValidation()
     */
    @Override
    protected void executeValidation() throws Exception {

        TCComponent[] comps = target.getItemRevision().getRelatedComponents(SDVTypeConstant.WELD_CONDITION_SHEET_RELATION);
        String itemId = target.getProperty(SDVPropertyConstant.BL_ITEM_ID);
        if (comps.length == 0){
            result = getMessage(ERROR_TYPE_WP_NOT_EXIST, itemId);
            return;
        }

        if (checkWeldSheetData(target.getItemRevision()))
            result = getMessage(ERROR_TYPE_WP_CONDITION_SHEET_UPDATE, itemId);

    }

    /**
     * BOMView 와 용접조건표 데이터셋에 최종 수정일을 비교하여 BOMView 수정일과 같거나 더크면
     * true 를 반환 한다
     * 
     * [SR140704-002][20140703] shcho, 용접조건표 Update 여부 체크시 시간오차 수정 (BOMView수정일이 5초이내 범위만큼 늦게 저장되는것 허용)
     *
     * @method checkWeldSheetData
     * @date 2013. 12. 19.
     * @param
     * @return boolean
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings("unused")
    private boolean checkWeldSheetData(TCComponentItemRevision selectedTarget) throws Exception
    {
        // 선택한 WeldOP 에 BOMView Revision 타입을 가져온다
        TCComponent[] bomViewTypes = selectedTarget.getReferenceListProperty(SDVTypeConstant.BOMLINE_RELATION);
        // 선택한 WeldOP 에 용접공법 Dataset 을 가져온다
        Vector<TCComponentDataset> datasets = new Vector<TCComponentDataset>();
        datasets = CustomUtil.getDatasets(selectedTarget, SDVTypeConstant.WELD_CONDITION_SHEET_RELATION, TcDefinition.DATASET_TYPE_EXCELX);

        for (TCComponent bomViewType : bomViewTypes)
        {
            if (bomViewType.getType().equals(SDVTypeConstant.BOMLINE_ITEM_REVISION))
            {
                Date bomViewLastDate = bomViewType.getDateProperty(SDVPropertyConstant.ITEM_LAST_MODIFY_DATE);
                Date dataSetLastDate = datasets.get(0).getDateProperty(SDVPropertyConstant.ITEM_LAST_MODIFY_DATE);
                Long compare = (long) bomViewLastDate.compareTo(dataSetLastDate);

                String bomViewStringDate = SDVStringUtiles.dateToString(bomViewLastDate, "yyyyMMddHHmmss");
                String dataSetStringDate = SDVStringUtiles.dateToString(dataSetLastDate, "yyyyMMddHHmmss");
                Long bomViewLongDate = Long.parseLong(bomViewStringDate);
                Long dataSetLongDate = Long.parseLong(dataSetStringDate);
                Long compareLongResult = dataSetLongDate - bomViewLongDate;
                if (compareLongResult < Long.parseLong("-5")){
                    return true;
                }
            }
        }
        return false;
    }

}
