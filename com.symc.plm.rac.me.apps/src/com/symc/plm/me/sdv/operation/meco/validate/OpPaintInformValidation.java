/**
 * 
 */
package com.symc.plm.me.sdv.operation.meco.validate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;

/**
 * Class Name : OperationValidator
 * Class Description :
 * 
 * [SR140924-021][20141006] shcho, 도장 부자재 소요량 체크 기능 추가(조립과 동일하게)
 * [SR150107-038][20140403] shcho, Reference E/Item Find No. 속성 값 Sync 기능 추가
 * 
 * @date 2014. 10. 06.
 * 
 */
public class OpPaintInformValidation extends OperationValidation<TCComponentBOMLine, String> {

    /*
     * (non-Javadoc)
     * 
     * @see com.symc.plm.me.sdv.operation.meco.validate.Validator#executeValidation()
     */
    @SuppressWarnings("unused")
    @Override
    protected void executeValidation() throws Exception {
        result = null;
        // Validation 항목별 에러 메세지 모음
        HashMap<Integer, StringBuilder> resultsByItem = new HashMap<Integer, StringBuilder>();
        
        /**
         * MProductBOMWindow 열기 (할당된 End Item의 Source BOMWindow)
         */
        TCComponentBOMWindow mProductBOMWindow = SDVBOPUtilities.getConnectedMProductBOMWindow(target.getItemRevision());
        
        TCComponentBOMLine[] childBOMLineList = SDVBOPUtilities.getUnpackChildrenBOMLine(target);
        ArrayList<String> dupCheckList = new ArrayList<String>(); // 중복 체크 List
        for (TCComponentBOMLine childBOMLine : childBOMLineList) {
            boolean isExistError = false;
            String errorMsg = ""; // 에러 메세지
            childBOMLine.refresh();
            
            String itemId = childBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
            String itemName = childBOMLine.getProperty(SDVPropertyConstant.BL_OBJECT_NAME);
            String itemType = childBOMLine.getItem().getType();
            String quantity = childBOMLine.getProperty(SDVPropertyConstant.BL_QUANTITY);
            String findNo = childBOMLine.getProperty(SDVPropertyConstant.BL_SEQUENCE_NO);
            // int sequence = 0;
            //
            // try {
            // sequence = Integer.parseInt(findNo);
            //
            // } catch (NumberFormatException ex) {
            // }
            //
            // sequence = findNo.startsWith("0") ? 0 : sequence;

            /**
             * 부자재 Check
             * 1. 소요량(수량) 입력 유무
             */
            if (itemType.equals(SDVTypeConstant.BOP_PROCESS_SUBSIDIARY_ITEM)) {

                if (dupCheckList.contains(itemId))
                    continue;

                // String opOption = target.getProperty(SDVPropertyConstant.BL_VARIANT_CONDITION);
                // String subPartOption = childBOMLine.getProperty(SDVPropertyConstant.BL_VARIANT_CONDITION);
                String subQuantity = childBOMLine.getProperty(SDVPropertyConstant.SUB_SUBSIDIARY_QTY);
                // (체크)소요량 입력 유무
                if (subQuantity.isEmpty()) {
                    errorMsg = getMessage(ERROR_TYPE_SUBPART_QTY_EMPTY, itemId, itemName);
                    addErrorMsg(resultsByItem, MSG_TYPE_SUBPART, errorMsg);
                    isExistError = true;
                }

                // // (체크) Paint Marker 일 경우, 주간/야간 조 입력 여부 FIXME: Paint Marker 부자재 받고 적용함
                // if (itemName.trim().toUpperCase().endsWith("PAINT MARKER") || itemName.trim().indexOf("마카") > 0) {
                // String dayOrNight = childBOMLine.getProperty(SDVPropertyConstant.BL_NOTE_DAYORNIGHT);
                // if (dayOrNight.isEmpty()) {
                // errorMsg = getMessage(ERROR_TYPE_SUBPART_DAYORNIGHT_EMPTY, itemId, itemName);
                // addErrorMsg(resultsByItem, MSG_TYPE_SUBPART, errorMsg);
                // isExistError = true;
                // }
                // }
                //
                // // (체크) Option 체크
                // if (subPartOption.isEmpty() && !opOption.isEmpty()) {
                // errorMsg = getMessage(ERROR_TYPE_SUBPART_OPTION_EMPTY, itemId, itemName);
                // addErrorMsg(resultsByItem, MSG_TYPE_SUBPART, errorMsg);
                // isExistError = true;
                // }
                //
                // // (체크) Sequence 범위 체크
                // if (sequence < 510 || sequence > 700) {
                // errorMsg = getMessage(ERROR_TYPE_SUBPART_SEQ_INVALID, itemId, itemName);
                // addErrorMsg(resultsByItem, MSG_TYPE_SUBPART, errorMsg);
                // isExistError = true;
                // }

                if (isExistError)
                    dupCheckList.add(itemId);
                
                /**
                 * End Item 체크
                 * 1. [SR150107-038][20140403] shcho, Reference E/Item Find No. 속성 값 Sync 기능 추가
                 */
            } else if (itemType.equals(SDVTypeConstant.EBOM_STD_PART) || itemType.equals(SDVTypeConstant.EBOM_VEH_PART)) {
                // Reference E/Item Find No. 속성 값 Sync
                TCComponentBOMLine assignSrcBOMLine = SDVBOPUtilities.getAssignSrcBomLine(mProductBOMWindow, childBOMLine);
                if (assignSrcBOMLine != null) {
                    String seqNo = assignSrcBOMLine.getProperty(SDVPropertyConstant.BL_SEQUENCE_NO);
                    childBOMLine.setProperty(SDVPropertyConstant.BL_NOTE_ENDITEM_SEQ_NO, seqNo);
                }
            }
        }

        /**
         * 에러 메세지를 만듬
         */
        makeErrorMsg(resultsByItem);
      
        
        /**
         *  MProductBOMWindow close.
         */
        if (mProductBOMWindow != null) {
            mProductBOMWindow.clearCache();
        }
    }

    /**
     * 에러 항목별로 Error 리스트를 추가한다.
     * 
     * @method addErroMsg
     * @date 2013. 12. 11.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void addErrorMsg(HashMap<Integer, StringBuilder> allMap, int key, String value) {
        StringBuilder sb = new StringBuilder();
        if (!allMap.containsKey(key)) {
            sb.append(value);
            allMap.put(key, sb);
        } else {
            sb = allMap.get(key);
            sb.append(value);
        }
    }

    /**
     * 항목별 Error 리스트를 하나의 에러 메세지로 만든다
     * 
     * @method makeErrorMsg
     * @date 2013. 12. 11.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void makeErrorMsg(HashMap<Integer, StringBuilder> resultsByItem) throws Exception {
        SortedSet<Integer> keys = new TreeSet<Integer>(resultsByItem.keySet());
        StringBuilder allMsg = new StringBuilder();

        for (int key : keys) {
            StringBuilder sb = resultsByItem.get(key);
            allMsg.append(sb.toString());
        }
        if (allMsg.length() > 0)
            result = allMsg.toString();
    }


}
