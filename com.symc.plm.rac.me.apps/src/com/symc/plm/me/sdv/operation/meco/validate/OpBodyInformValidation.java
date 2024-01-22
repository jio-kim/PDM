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
 * [SR140924-021][20141006] shcho, 차체 부자재 소요량 체크 기능 추가(조립과 동일하게)
 * [SR150107-038][20140403] shcho, Reference E/Item Find No. 속성 값 Sync 기능 추가
 * [NON-SR][20150605] shcho, Reference E/Item Find No. 속성 값 Sync 기능 수행시 MECO 상신 Validate 진행을 하게 되면 target의 Parent를 가져오지 못해 null 에러 남. 
 *                              이 문제가 해결 될 때까지 임시로 Sync기능 막음. (해결 방법은 조립처럼 차체의 모든 공법에도 m7_PRODUCT_CODE 속성값을 넣는 migration작업 필요) 
 *
 * @date 2013. 12. 10.
 *
 */
@SuppressWarnings("unused")
public class OpBodyInformValidation extends OperationValidation<TCComponentBOMLine, String> {

    /*
     * (non-Javadoc)
     *
     * @see com.symc.plm.me.sdv.operation.meco.validate.Validator#executeValidation()
     */
    @Override
    protected void executeValidation() throws Exception {
        result = null;
        // Validation 항목별 에러 메세지 모음
        HashMap<Integer, StringBuilder> resultsByItem = new HashMap<Integer, StringBuilder>();
        
        /**
         * MProductBOMWindow 열기 (할당된 End Item의 Source BOMWindow)
         */
//        TCComponentBOMWindow mProductBOMWindow = SDVBOPUtilities.getConnectedMProductBOMWindow(target.parent().parent().parent().getItemRevision());
        
        /**
         * Line Code 일치 체크
         */
        //validateLineCode(resultsByItem);

        TCComponentBOMLine[] childBOMLineList = SDVBOPUtilities.getUnpackChildrenBOMLine(target);
        for (TCComponentBOMLine childBOMLine : childBOMLineList) {
            boolean isExistError = false;
            String errorMsg = ""; // 에러 메세지
            childBOMLine.refresh();
            
            String itemId = childBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
            String itemName = childBOMLine.getProperty(SDVPropertyConstant.BL_OBJECT_NAME);
            String itemType = childBOMLine.getItem().getType();
            String quantity = childBOMLine.getProperty(SDVPropertyConstant.BL_QUANTITY);
            String findNo = childBOMLine.getProperty(SDVPropertyConstant.BL_SEQUENCE_NO);
            ArrayList<String> dupCheckList = new ArrayList<String>();
            int sequence = 0;

            try {
                sequence = Integer.parseInt(findNo);
            } catch (NumberFormatException ex) {
            }

            /**
             * End Item 체크
             * 1. End Item Seq No. 유무 체크 <- 유무만 체크
             * 2. [SR150107-038][20140403] shcho, Reference E/Item Find No. 속성 값 Sync 기능 추가
             */
            if (itemType.equals(SDVTypeConstant.EBOM_STD_PART) || itemType.equals(SDVTypeConstant.EBOM_VEH_PART)) {
                // Reference E/Item Find No. 속성 값 Sync
                /*TCComponentBOMLine assignSrcBOMLine = SDVBOPUtilities.getAssignSrcBomLine(mProductBOMWindow, childBOMLine);
                if (assignSrcBOMLine != null) {
                    String seqNo = assignSrcBOMLine.getProperty(SDVPropertyConstant.BL_SEQUENCE_NO);
                    childBOMLine.setProperty(SDVPropertyConstant.BL_NOTE_ENDITEM_SEQ_NO, seqNo);
                }*/

                if (dupCheckList.contains(itemId))
                    continue;
                // (체크) Sequence 입력 유무 체크
                if (sequence >= 1)
                    continue;

                errorMsg = getMessage(ERROR_TYPE_ENDITEM_SEQ_EMPTY, itemId, itemName);
                addErrorMsg(resultsByItem, MSG_TYPE_ENDITEM, errorMsg);
                dupCheckList.add(itemId);
            
                /**
                 * 부자재 Check
                 * 1. 소요량(수량) 입력 유무 체크 추가 [SR140924-021][20141006] shcho, 차체 부자재 소요량 체크기능 추가(조립과 동일하게)
                 * 2. Paint Marker 일 경우, 주간 조/야간 조 입력 여부 체크  <- 삭제
                 * 3. 공법에 Option이 있을 경우 부자재 Option이 있는지 체크  <- 삭제
                 * 4. Sequence (510 ~ 800) <- 삭제
                 */
            } else if (itemType.equals(SDVTypeConstant.BOP_PROCESS_SUBSIDIARY_ITEM)) {
//
                if (dupCheckList.contains(itemId))
                    continue;
//
//                String opOption = target.getProperty(SDVPropertyConstant.BL_VARIANT_CONDITION);
//                String subPartOption = childBOMLine.getProperty(SDVPropertyConstant.BL_VARIANT_CONDITION);
                String subQuantity = childBOMLine.getProperty(SDVPropertyConstant.SUB_SUBSIDIARY_QTY);
                // (체크)소요량 입력 유무
                if (subQuantity.isEmpty()) {
                    errorMsg = getMessage(ERROR_TYPE_SUBPART_QTY_EMPTY, itemId, itemName);
                    addErrorMsg(resultsByItem, MSG_TYPE_SUBPART, errorMsg);
                    isExistError = true;
                }

//                // (체크) Paint Marker 일 경우, 주간/야간 조 입력 여부 FIXME: Paint Marker 부자재 받고 적용함 <- 삭제
////                if (itemName.trim().toUpperCase().endsWith("PAINT MARKER") || itemName.trim().indexOf("마카") > 0) {
////                    String dayOrNight = childBOMLine.getProperty(SDVPropertyConstant.BL_NOTE_DAYORNIGHT);
////                    if (dayOrNight.isEmpty()) {
////                        errorMsg = getMessage(ERROR_TYPE_SUBPART_DAYORNIGHT_EMPTY, itemId, itemName);
////                        addErrorMsg(resultsByItem, MSG_TYPE_SUBPART, errorMsg);
////                        isExistError = true;
////                    }
////
////                }
//
//                // (체크) Option 체크 <- 삭제
////                if (subPartOption.isEmpty() && !opOption.isEmpty()) {
////                    errorMsg = getMessage(ERROR_TYPE_SUBPART_OPTION_EMPTY, itemId, itemName);
////                    addErrorMsg(resultsByItem, MSG_TYPE_SUBPART, errorMsg);
////                    isExistError = true;
////                }
//
//                // (체크) Sequence 범위 체크  <- 삭제
////                if (sequence < 510 || sequence > 800) {
////                    errorMsg = getMessage(ERROR_TYPE_SUBPART_SEQ_INVALID, itemId, itemName);
////                    addErrorMsg(resultsByItem, MSG_TYPE_SUBPART, errorMsg);
////                    isExistError = true;
////                }
//
                if (isExistError)
                    dupCheckList.add(itemId);
            }
//
//                /**
//                 * 공구 Check
//                 * 1. 수량 입력 유무   <- 삭제
//                 * 2. 수량 소수점 이하   <- 삭제
//                 * 3. Sequence (10 ~ 200)   <- 삭제
//                 */
//            } else if (itemType.equals(SDVTypeConstant.BOP_PROCESS_TOOL_ITEM)) {
//                if (dupCheckList.contains(itemId))
//                    continue;
//
//                // (체크)공구 입력 유무
//                if (quantity.isEmpty()) {
//                    errorMsg = getMessage(ERROR_TYPE_TOOL_QTY_EMPTY, itemId, itemName);
//                    addErrorMsg(resultsByItem, MSG_TYPE_TOOL, errorMsg);
//                    isExistError = true;
//                } else {
//                    // (체크)공구에 소수점 입력 유무
//                    String regEx = "[0-9]+|[0-9]+\\.[0]+";
//                    boolean isMatch = quantity.matches(regEx);
//                    if (!isMatch) {
//                        errorMsg = getMessage(ERROR_TYPE_TOOL_QTY_INVALID, itemId, itemName);
//                        addErrorMsg(resultsByItem, MSG_TYPE_TOOL, errorMsg);
//                        isExistError = true;
//                    }
//                }
//
//                // (체크) Sequence 범위 체크
//                if (sequence < 10 || sequence > 200) {
//                    errorMsg = getMessage(ERROR_TYPE_TOOL_SEQ_INVALID, itemId, itemName);
//                    addErrorMsg(resultsByItem, MSG_TYPE_TOOL, errorMsg);
//                    isExistError = true;
//                }
//
//                if (isExistError)
//                    dupCheckList.add(itemId);
//                /**
//                 * 설비 Find No(210 ~ 500)   <- 삭제
//                 */
//            } else if (itemType.equals(SDVTypeConstant.BOP_PROCESS_GENERALEQUIP_ITEM) || itemType.equals(SDVTypeConstant.BOP_PROCESS_JIGFIXTURE_ITEM)) {
//                if (dupCheckList.contains(itemId))
//                    continue;
//                // (체크) Sequence 범위 체크
//                if (sequence >= 210 && sequence <= 500)
//                    continue;
//
//                errorMsg = getMessage(ERROR_TYPE_EQUIPMENT_SEQ_INVALID, itemId, itemName);
//                addErrorMsg(resultsByItem, MSG_TYPE_EQUIPMENT, errorMsg);
//
//                dupCheckList.add(itemId);
//            }
        }

        /**
         * 에러 메세지를 만듬
         */
        makeErrorMsg(resultsByItem);
      
        
        /**
         *  MProductBOMWindow close.
         */
//        if (mProductBOMWindow != null) {
//            mProductBOMWindow.clearCache();
//        }
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

    /**
     * Line 코드가 공정코드, 작업자 코드의 앞자리와 일치하는지 체크
     *
     * @method validLineOperation
     * @date 2013. 12. 12.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void validateLineCode(HashMap<Integer, StringBuilder> resultsByItem) throws Exception {

        TCComponentBOMLine lineBOMLine = target.parent();

        String opItemId = target.getProperty(SDVPropertyConstant.BL_ITEM_ID);

        // 상위 BOM이 있을 경우에만 체크함
        if (lineBOMLine == null)
            return;
        String lineCode = lineBOMLine.getItemRevision().getProperty(SDVPropertyConstant.LINE_REV_CODE);
        // 공정 코드   <- 삭제
        String stationNo = target.getItemRevision().getProperty(SDVPropertyConstant.OPERATION_REV_STATION_NO);
        // 작업자 코드   <- 삭제
        String workerCode = target.getItem().getProperty(SDVPropertyConstant.OPERATION_WORKER_CODE);

        if (!stationNo.startsWith(lineCode)) {
            addErrorMsg(resultsByItem, MSG_TYPE_OP_LINE_MATCH, getMessage(ERROR_TYPE_OP_STATION_NOT_INVALID, opItemId));
        }
        if (!workerCode.startsWith(lineCode)) {
            addErrorMsg(resultsByItem, MSG_TYPE_OP_LINE_MATCH, getMessage(ERROR_TYPE_OP_WORKERCODE_NOT_INVALID, opItemId));
        }
    }
}
