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
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.time.common.ActivityUtils;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentCfgActivityLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentMEActivity;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCPreferenceService.TCPreferenceLocation;
import com.teamcenter.rac.kernel.TCSession;

/**
 * Class Name : OperationValidator
 * Class Description :
 * [SR141008-012,SR141008-025][20141013] shcho, Activity 작업코드 별 공법에 할당된 공구 검증시, 대상이 되는 공구 및 설비 조건 추가
 *  1) TT1, TT2, PT3 인 경우에 체크 할 공구목록 추가 및 설비 체크 로직 추가
 *  2) TT1, TT2, PT3, CP1 이 아닌 경우에 체크 할 공구목록 추가 및 설비 체크 로직 추가
 *  3) TC인 경우에 체크 할 공구 목록 추가
 *  4) 비교대상 작업코드중 ACTIVITY_CODE_TYPE3에 PT1,PT2,CP1 추가
 * 
 * [SR141014-037][20141014] shcho,  Activity 작업코드 별 공법에 할당된 공구 검증 기능 수정
 *  1) 작업코드가 TC가 아닌 경우의 처리 로직을 Validation에서 삭제 처리
 * 
 * [SR141016-010][20141016] shcho, Activity 작업코드 별 공법에 할당된 공구 검증 기능 수정
 *  1) 할당된 공구가 없는 경우에 체크하는 로직 삭제
 *  2)  TT1, TT2, PT3인 경우 체크 로직 누락된 항목 추가
 *  3) 비교대상 작업코드중 ACTIVITY_CODE_TYPE3에서 TT4삭제
 * 
 * [SR141022-012][20141022] shcho, Activity 작업코드 별 공법에 할당된 공구 검증 기능 수정
 *  1) ACTIVITY_CODE_TYPE1에서 DA1,RV1,RV2 추가
 *  2) ACTIVITY_CODE_TYPE3에서 DA1,RV1,RV2,AJ1,AJ2 추가
 *  3) ACTIVITY_CODE_TYPE4에서 DA1,RV1,RV2,AJ1,AJ2 추가
 *  4) ACTIVITY_CODE_TYPE1인 경우 '할당되는 설비가 A-C-CH or A-E-21로 시작되어야 한다' 로 변경
 * 
 * 
 * [SR150107-038][20140403] shcho, Reference E/Item Find No. 속성 값 Sync 기능 추가
 * 
 * 
 * @date 2013. 12. 10.
 * 
 */
public class OpInformValidation extends OperationValidation<TCComponentBOMLine, String> {

    public static String ACTIVITY_CODE_TYPE1 = "TT1,TT2,PT3,DA1,RV1,RV2";
    public static String ACTIVITY_CODE_TYPE2 = "TC";
    public static String ACTIVITY_CODE_TYPE3 = "TT1,TT2,PT1,PT2,PT3,TC,CP1,DA1,RV1,RV2,AJ1,AJ2";
    public static String ACTIVITY_CODE_TYPE4 = "TT1,TT2,PT3,CP1,DA1,RV1,RV2,AJ1,AJ2";

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
        TCComponentBOMWindow mProductBOMWindow = SDVBOPUtilities.getConnectedMProductBOMWindow(target.getItemRevision());
        
        /**
         * Line Code 일치 체크
         */
        validateLineCode(resultsByItem);

        TCComponentBOMLine[] childBOMLineList = SDVBOPUtilities.getUnpackChildrenBOMLine(target);
        ArrayList<String> dupCheckList = new ArrayList<String>(); // 중복 체크 List
        ArrayList<TCComponentBOMLine> toolCheckList = new ArrayList<TCComponentBOMLine>(); // 할당된 공구 체크 List
        ArrayList<TCComponentBOMLine> equipCheckList = new ArrayList<TCComponentBOMLine>(); // 할당된 설비 체크 List
        for (TCComponentBOMLine childBOMLine : childBOMLineList) {
            boolean isExistError = false;
            String errorMsg = ""; // 에러 메세지
            childBOMLine.refresh();
            
            String itemId = childBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
            String itemName = childBOMLine.getProperty(SDVPropertyConstant.BL_OBJECT_NAME);
            String itemType = childBOMLine.getItem().getType();
            String quantity = childBOMLine.getProperty(SDVPropertyConstant.BL_QUANTITY);
            String findNo = childBOMLine.getProperty(SDVPropertyConstant.BL_SEQUENCE_NO);
            int sequence = 0;

            try {
                sequence = Integer.parseInt(findNo);

            } catch (NumberFormatException ex) {
            }

            sequence = findNo.startsWith("0") ? 0 : sequence;

            /**
             * End Item Seq No. 유무 체크( 1 ~ 9 )
             */
            if (itemType.equals(SDVTypeConstant.EBOM_STD_PART) || itemType.equals(SDVTypeConstant.EBOM_VEH_PART)) {
                // Reference E/Item Find No. 속성 값 Sync
                TCComponentBOMLine assignSrcBOMLine = SDVBOPUtilities.getAssignSrcBomLine(mProductBOMWindow, childBOMLine);
                if (assignSrcBOMLine != null) {
                    String seqNo = assignSrcBOMLine.getProperty(SDVPropertyConstant.BL_SEQUENCE_NO);
                    childBOMLine.setProperty(SDVPropertyConstant.BL_NOTE_ENDITEM_SEQ_NO, seqNo);
                }
                
                if (dupCheckList.contains(itemId))
                    continue;
                // (체크) Sequence 범위 체크
                if (sequence >= 1 && sequence <= 9)
                    continue;

                errorMsg = getMessage(ERROR_TYPE_ENDITEM_SEQ_INVALID, itemId, itemName);
                addErrorMsg(resultsByItem, MSG_TYPE_ENDITEM, errorMsg);
                dupCheckList.add(itemId);
                /**
                 * 부자재 Check
                 * 1. 소요량(수량) 입력 유무
                 * 2. Paint Marker 일 경우, 주간 조/야간 조 입력 여부 체크
                 * 3. 공법에 Option이 있을 경우 부자재 Option이 있는지 체크
                 * 4. Sequence (510 ~ 700)
                 */
            } else if (itemType.equals(SDVTypeConstant.BOP_PROCESS_SUBSIDIARY_ITEM)) {

                if (dupCheckList.contains(itemId))
                    continue;

                String opOption = target.getProperty(SDVPropertyConstant.BL_VARIANT_CONDITION);
                String subPartOption = childBOMLine.getProperty(SDVPropertyConstant.BL_VARIANT_CONDITION);
                String subQuantity = childBOMLine.getProperty(SDVPropertyConstant.SUB_SUBSIDIARY_QTY);
                // (체크)소요량 입력 유무
                if (subQuantity.isEmpty()) {
                    errorMsg = getMessage(ERROR_TYPE_SUBPART_QTY_EMPTY, itemId, itemName);
                    addErrorMsg(resultsByItem, MSG_TYPE_SUBPART, errorMsg);
                    isExistError = true;
                }

                // (체크) Paint Marker 일 경우, 주간/야간 조 입력 여부 FIXME: Paint Marker 부자재 받고 적용함
                if (itemName.trim().toUpperCase().endsWith("PAINT MARKER") || itemName.trim().indexOf("마카") > 0) {
                    String dayOrNight = childBOMLine.getProperty(SDVPropertyConstant.BL_NOTE_DAYORNIGHT);
                    if (dayOrNight.isEmpty()) {
                        errorMsg = getMessage(ERROR_TYPE_SUBPART_DAYORNIGHT_EMPTY, itemId, itemName);
                        addErrorMsg(resultsByItem, MSG_TYPE_SUBPART, errorMsg);
                        isExistError = true;
                    }

                }

                // (체크) Option 체크
                if (subPartOption.isEmpty() && !opOption.isEmpty()) {
                    errorMsg = getMessage(ERROR_TYPE_SUBPART_OPTION_EMPTY, itemId, itemName);
                    addErrorMsg(resultsByItem, MSG_TYPE_SUBPART, errorMsg);
                    isExistError = true;
                }

                // (체크) Sequence 범위 체크
                if (sequence < 510 || sequence > 700) {
                    errorMsg = getMessage(ERROR_TYPE_SUBPART_SEQ_INVALID, itemId, itemName);
                    addErrorMsg(resultsByItem, MSG_TYPE_SUBPART, errorMsg);
                    isExistError = true;
                }

                if (isExistError)
                    dupCheckList.add(itemId);

                /**
                 * 공구 Check
                 * 1. 수량 입력 유무
                 * 2. 수량 소수점 이하
                 * 3. Sequence (10 ~ 200)
                 * 4. 토크값이 잘못 입력됨
                 * 5. Activity 작업코드 별 공법에 할당된 공구 검증
                 */
            } else if (itemType.equals(SDVTypeConstant.BOP_PROCESS_TOOL_ITEM)) {
                if (dupCheckList.contains(itemId))
                    continue;

                String torgueType = childBOMLine.getProperty(SDVPropertyConstant.BL_NOTE_TORQUE);
                String torqueValue = childBOMLine.getProperty(SDVPropertyConstant.BL_NOTE_TORQUE_VALUE);

                // (체크1)공구 수량 입력 유무
                if (quantity.isEmpty()) {
                    errorMsg = getMessage(ERROR_TYPE_TOOL_QTY_EMPTY, itemId, itemName);
                    addErrorMsg(resultsByItem, MSG_TYPE_TOOL, errorMsg);
                    isExistError = true;
                } else {
                    // (체크2)공구에 소수점 입력 유무
                    String regEx = "[0-9]+|[0-9]+\\.[0]+";
                    boolean isMatch = quantity.matches(regEx);
                    if (!isMatch) {
                        errorMsg = getMessage(ERROR_TYPE_TOOL_QTY_INVALID, itemId, itemName);
                        addErrorMsg(resultsByItem, MSG_TYPE_TOOL, errorMsg);
                        isExistError = true;
                    }
                }

                // (체크3) Sequence 범위(10 ~ 200) 체크
                if (sequence < 10 || sequence > 200) {
                    errorMsg = getMessage(ERROR_TYPE_TOOL_SEQ_INVALID, itemId, itemName);
                    addErrorMsg(resultsByItem, MSG_TYPE_TOOL, errorMsg);
                    isExistError = true;
                }

                // (체크4) 토크 타입이 "미정" 이 아니고, 값이 있을 경우
                if (!torgueType.equals("") && !torgueType.equals(registry.getString("NotYetValue.NAME", "미정"))) {
                    // 토크 값에 "~" 또는 "MAX" 값이 있는 지 체크
                    if (torqueValue.indexOf("~") < 0 && torqueValue.toUpperCase().indexOf("MAX") < 0 && torqueValue.toUpperCase().indexOf("MIN") < 0) {
                        errorMsg = getMessage(ERROR_TYPE_TOOL_TORGUE_VALUE_INVALID, itemId, itemName);
                        addErrorMsg(resultsByItem, MSG_TYPE_TOOL, errorMsg);
                        isExistError = true;
                    }
                }

                // (체크5) Activity 작업코드 별 공법에 할당된 공구 검증 (toolCheckList에 Tool만 담아서 하단에서 별도 처리)
                toolCheckList.add(childBOMLine);

                if (isExistError)
                    dupCheckList.add(itemId);

                /**
                 * 설비 Find No(210 ~ 500)
                 */
            } else if (itemType.equals(SDVTypeConstant.BOP_PROCESS_GENERALEQUIP_ITEM) || itemType.equals(SDVTypeConstant.BOP_PROCESS_JIGFIXTURE_ITEM)) {
                if (dupCheckList.contains(itemId))
                    continue;

                // 'Activity 작업코드 별 공법에 할당된 공구 검증' 시 사용할 설비 리스트
                equipCheckList.add(childBOMLine);
                
                // (체크) Sequence 범위 체크
                if (sequence >= 210 && sequence <= 500)
                    continue;

                errorMsg = getMessage(ERROR_TYPE_EQUIPMENT_SEQ_INVALID, itemId, itemName);
                addErrorMsg(resultsByItem, MSG_TYPE_EQUIPMENT, errorMsg);

                dupCheckList.add(itemId);
            }
        }

        /**
         * 공구 Check(체크5) Activity 작업코드 별 공법에 할당된 공구 검증
         */
         toolCheckWithActivityCode(resultsByItem, toolCheckList, equipCheckList);

        
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
     * [SR141008-012,SR141008-025][20141013] shcho, Activity 작업코드 별 공법에 할당된 공구 검증시, 대상이 되는 공구 및 설비 조건 추가
     * [SR141014-037][20141014] shcho,  Activity 작업코드 별 공법에 할당된 공구 검증 기능 수정
     * [SR141016-010][20141016] shcho, Activity 작업코드 별 공법에 할당된 공구 검증 기능 수정
     * [없음][20141017] shcho, Activity 작업코드가 TC인 경우 "설비가 A-E-21-010, A-E-21-030, A-E-21-040, A-E-21-051 이어야 한다." 기능 추가 
     * [SR141022-012][20141022] shcho, Activity 작업코드 별 공법에 할당된 공구 검증 기능 수정
     *  1) ACTIVITY_CODE_TYPE1에서 DA1,RV1,RV2 추가
     *  2) ACTIVITY_CODE_TYPE3에서 DA1,RV1,RV2,AJ1,AJ2 추가
     *  3) ACTIVITY_CODE_TYPE4에서 DA1,RV1,RV2,AJ1,AJ2 추가
     *  4) ACTIVITY_CODE_TYPE1인 경우 '할당되는 설비가 A-C-CH or A-E-21로 시작되어야 한다' 로 변경
     * 
     * 
     * 
     * 1. 작업코드가 TT1,TT2,PT3,DA1,RV1,RV2 인 경우 특정 체결공구 또는 소켓공구 또는 설비 할당 필수.
     * 2. 작업코드가 TT1,TT2,PT3,CP1,DA1,RV1,RV2,AJ1,AJ2 가 아닌 경우 특정공구 또는 설비가 할당되어 있으면 안됨.
     * 3. 작업코드가 TC인 경우 특정 토크렌치 또는 소켓 할당 필수.
     * 4. 작업코드가 TC가 아닌 경우 체크 로직 삭제
     * 5. 작업코드가 TT1,TT2,PT1,PT2,PT3,TC,CP1,DA1,RV1,RV2,AJ1,AJ2 이 아닌 경우 모든 소켓 공구가 할당되어 있으면 안됨.
     * 
     * @param resultsByItem
     * @param toolCheckList
     * @param equipCheckList 
     * @throws Exception
     * @throws TCException
     */
    public void toolCheckWithActivityCode(HashMap<Integer, StringBuilder> resultsByItem, ArrayList<TCComponentBOMLine> toolCheckList, ArrayList<TCComponentBOMLine> equipCheckList) throws Exception, TCException {
        //체크 대상이 될 공구 목록을 TC Preference로 부터 가져오기
        String errorMsg ="";
        String preferenceName = "SYMC_LIST_OF_TOOLS_FOR_VALIDATION";
        TCPreferenceService prefService = ((TCSession) AIFUtility.getDefaultSession()).getPreferenceService();
//        String[] arrToolCheckKeyList = prefService.getStringArray(TCPreferenceService.TC_preference_site, preferenceName);
        String[] arrToolCheckKeyList = prefService.getStringValuesAtLocation(preferenceName, TCPreferenceLocation.OVERLAY_LOCATION);
        if(arrToolCheckKeyList == null || arrToolCheckKeyList.length <= 0) {
            throw new Exception("Please Check Preference SYMC_LIST_OF_TOOLS_FOR_VALIDATION");
        }
        
        String[] COMBINATION_TOOL_INDEPENDENT = getToolCheckKey(arrToolCheckKeyList, "COMBINATION_TOOL_INDEPENDENT");
        String[] COMBINATION_TOOL_WITH_SOCKET = getToolCheckKey(arrToolCheckKeyList, "COMBINATION_TOOL_WITH_SOCKET");
        String[] SOCKET_TOOL_WITH_COMBINATION = getToolCheckKey(arrToolCheckKeyList, "SOCKET_TOOL_WITH_COMBINATION");
        String[] TORQUEWRENCH_TOOL_INDEPENDENT = getToolCheckKey(arrToolCheckKeyList, "TORQUEWRENCH_TOOL_INDEPENDENT");
        String[] TORQUEWRENCH_TOOL_WITH_SOCKET = getToolCheckKey(arrToolCheckKeyList, "TORQUEWRENCH_TOOL_WITH_SOCKET");
        String[] SOCKET_TOOL_WITH_TORQUEWRENCH = getToolCheckKey(arrToolCheckKeyList, "SOCKET_TOOL_WITH_TORQUEWRENCH");
        
        //Activity 목록 가져오기
        AIFComponentContext[] activityParentComp = target.getRelated(SDVPropertyConstant.BL_ACTIVITY_LINES);
        TCComponentCfgActivityLine rootActLine = null;
        if(activityParentComp!=null && activityParentComp.length>0){
        	rootActLine = (TCComponentCfgActivityLine) activityParentComp[0].getComponent();
        }
        TCComponentMEActivity rootActivity = null;
        if(rootActLine!=null){
        	rootActivity = (TCComponentMEActivity) rootActLine.getUnderlyingComponent();
        }
        TCComponent[] childComps = null;
        if(rootActivity!=null){
        	childComps = ActivityUtils.getSortedActivityChildren(rootActivity);
        }
        
        if(childComps==null){
        	return;
        }
        
            
        // (체크1) 작업코드가 TT1,TT2,PT3,DA1,RV1,RV2 인 경우
        if(checkExistActivity(childComps, ACTIVITY_CODE_TYPE1.split(",")))
        {
         // [체결공구가 (A-IG or A-IS or A-IC or A-CG or A-CS or A-CC or A-OG or A-OS or A-OC or A-NG or A-NC or A-SG or A-SS or A-SC or E-SG or E-SS or E-SC or E-IG or E-IS or E-IC or E-CG or E-CS or E-CC or E-OG or E-OS or E-OC or E-NG or E-NS or E-NC or E-NH) AND 소켓은 (H-IS or H-SB or H-SS or H-SP)]
            // 또는 [체결공구가 A-RO or A-RC or A-NS or A-NH or A-RS or A-RM or A-PG or H-SO or H-SC or H-SM] 이어야 한다. 
            // 또는 [토크렌치가 H-TS AND 소켓이 H-IS or H-SS or H-SP] or  토크렌치가 H-TO or 토크렌치가 H-TC] 이어야 한다.
            // 또는 [설비가  A-C-CH or A-E-21] 로 시작되어야 한다. 
            boolean toolCheckResult = ((checkExistTool(toolCheckList, COMBINATION_TOOL_WITH_SOCKET) && checkExistTool(toolCheckList, SOCKET_TOOL_WITH_COMBINATION))
                                                   || (checkExistTool(toolCheckList, COMBINATION_TOOL_INDEPENDENT) || checkExistTool(toolCheckList, new String[] {"H-SO", "H-SC", "H-SM"}))
                                                   || (checkExistTool(toolCheckList, TORQUEWRENCH_TOOL_WITH_SOCKET) && checkExistTool(toolCheckList, SOCKET_TOOL_WITH_TORQUEWRENCH))
                                                   || checkExistTool(toolCheckList, TORQUEWRENCH_TOOL_INDEPENDENT));
            boolean equipCheckResult = checkExistEquip(equipCheckList, new String[] {"A-C-CH", "A-E-21"});  
            if(!toolCheckResult && !equipCheckResult) 
            {
                errorMsg = getMessage(ERROR_TYPE_TOOL_ASSIGN_INVALID, ACTIVITY_CODE_TYPE1.toString());
                addErrorMsg(resultsByItem, MSG_TYPE_TOOL, errorMsg);
            }
        } 
        // (체크2) 작업코드가 TT1,TT2,PT3,CP1,DA1,RV1,RV2,AJ1,AJ2 이 아닌 경우
        else if(!checkExistActivity(childComps, ACTIVITY_CODE_TYPE4.split(",")))
        {
            // [체결공구가 (A-IG or A-IS or A-IC or A-CG or A-CS or A-CC or A-OG or A-OS or A-OC or A-NG or A-NC or A-SG or A-SS or A-SC or E-SG or E-SS or E-SC or E-IG or E-IS or E-IC or E-CG or E-CS or E-CC or E-OG or E-OS or E-OC or E-NG or E-NS or E-NC or E-NH) AND 소켓은 (H-IS or H-SB or H-SS or H-SP)]
            // 또는  [체결공구가 A-RO or A-RC or A-NS or A-NH or A-RS or A-RM or A-PG] 가 할당 되어서는 안된다.
           // 또는  [설비가 A-C-CH ] 로 시작되는 것이 할당되어서는 안된다.
            boolean toolCheckResult = ((checkExistTool(toolCheckList, COMBINATION_TOOL_WITH_SOCKET) && checkExistTool(toolCheckList, SOCKET_TOOL_WITH_COMBINATION))
                                                   || checkExistTool(toolCheckList, COMBINATION_TOOL_INDEPENDENT));
            boolean equipCheckResult = checkExistEquip(equipCheckList, new String[] {"A-C-CH"});
            if(toolCheckResult || equipCheckResult) 
            {
                errorMsg = getMessage(ERROR_TYPE_TOOL_NOTASSIGN_INVALID, ACTIVITY_CODE_TYPE4.toString());
                addErrorMsg(resultsByItem, MSG_TYPE_TOOL, errorMsg);
            }
        }
        
        // (체크3) 작업코드가 TC인 경우
        if(checkExistActivity(childComps, ACTIVITY_CODE_TYPE2.split(",")))
        {
            // [토크렌치가 H-TS AND 소켓이 H-IS or H-SS or H-SP] or  토크렌치가 H-TO or 토크렌치가 H-TC] 이어야 한다.
            // 또는  [설비가 A-E-21-010, A-E-21-030, A-E-21-040, A-E-21-051 ] 이어야 한다. 
            boolean toolCheckResult = ((checkExistTool(toolCheckList, TORQUEWRENCH_TOOL_WITH_SOCKET) && checkExistTool(toolCheckList, SOCKET_TOOL_WITH_TORQUEWRENCH))
                                                   || checkExistTool(toolCheckList, TORQUEWRENCH_TOOL_INDEPENDENT));
            boolean equipCheckResult = checkExistEquip(equipCheckList, new String[] {"A-E-21-010", "A-E-21-030", "A-E-21-040", "A-E-21-051"});
            if(!toolCheckResult && !equipCheckResult) 
            {
                errorMsg = getMessage(ERROR_TYPE_TOOL_ASSIGN_INVALID, ACTIVITY_CODE_TYPE2.toString());
                addErrorMsg(resultsByItem, MSG_TYPE_TOOL, errorMsg);
            }
        }
        // (체크4) 작업코드가 TC가 아닌 경우 => [SR141014-037][20141014] shcho,  작업코드가 TC가 아닌 경우의 처리 로직을 Validation에서 삭제 처리 
        else 
        {
            // [토크렌치가 H-TS AND 소켓이 H-IS or H-SS or H-SP] or 토크렌치가 H-TO or 토크렌치가 H-TC]가 할당 되어서는 안된다.
            //boolean toolCheckResult = ((checkExistTool(toolCheckList, TORQUEWRENCH_TOOL_WITH_SOCKET) && checkExistTool(toolCheckList, SOCKET_TOOL_WITH_TORQUEWRENCH)) || checkExistTool(toolCheckList, TORQUEWRENCH_TOOL_INDEPENDENT));
            //if(toolCheckResult) 
            //{
            //    errorMsg = getMessage(ERROR_TYPE_TOOL_NOTASSIGN_INVALID, ACTIVITY_CODE_TYPE2.toString());
            //    addErrorMsg(resultsByItem, MSG_TYPE_TOOL, errorMsg);
            //}
        }
        
        // (체크5) 작업코드가 TT1,TT2,PT1,PT2,PT3,TC,CP1,DA1,RV1,RV2,AJ1,AJ2 이 아닌 경우
        if(!checkExistActivity(childComps, ACTIVITY_CODE_TYPE3.split(",")))
        {
            // 모든 소켓 공구가 할당 되어서는 안된다.
            boolean toolCheckResult = checkSocketTool(toolCheckList);
            if(toolCheckResult) 
            {
                errorMsg = getMessage(ERROR_TYPE_TOOL_NOTASSIGN_INVALID, ACTIVITY_CODE_TYPE3.toString());
                addErrorMsg(resultsByItem, MSG_TYPE_TOOL, errorMsg);
            }
        }
        
    }


    /**
     * Preference로 부터 가져온 ToolCkeckKeyList에서 대상 Category 에 해당하는 ToolCheckKey만을 뽑아서 리턴하는 함수  
     * @param arrToolCkeckKeyList, ToolCategoryName
     * @return String[]
     */
    public String[] getToolCheckKey(String[] arrToolCkeckKeyList, String ToolCategoryName) throws Exception{
        for (String toolCkeckKeyRow : arrToolCkeckKeyList) {
            if(toolCkeckKeyRow.startsWith(ToolCategoryName)) {
                String toolCkeckKeys = toolCkeckKeyRow.split(":")[1];
                return toolCkeckKeys.split(",");
            }
        }
        
        return null;
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
        // 공정 코드
        String stationNo = target.getItemRevision().getProperty(SDVPropertyConstant.OPERATION_REV_STATION_NO);
        // 작업자 코드
        String workerCode = target.getItem().getProperty(SDVPropertyConstant.OPERATION_WORKER_CODE);

        if (!stationNo.startsWith(lineCode)) {
            addErrorMsg(resultsByItem, MSG_TYPE_OP_LINE_MATCH, getMessage(ERROR_TYPE_OP_STATION_NOT_INVALID, opItemId));
        }
        if (!workerCode.startsWith(lineCode)) {
            addErrorMsg(resultsByItem, MSG_TYPE_OP_LINE_MATCH, getMessage(ERROR_TYPE_OP_WORKERCODE_NOT_INVALID, opItemId));
        }
    }

    /**
     * 공구의 ID 앞 두자리(속성 MainClass, SubClass의 조합) 값 체크 함수
     * (배열 arrToolCodes의 코드값에 해당하는 공구가 toolList 있는지 체크하는 함수)
     * 
     * @param toolList, arrToolCodes
     * @return boolean
     * @throws TCException 
     */    
    private boolean checkExistTool(ArrayList<TCComponentBOMLine> toolList, String[] arrToolCodes) throws TCException {
        for (TCComponentBOMLine toolBOMLine : toolList) {
            TCComponentItemRevision toolItemRevision = toolBOMLine.getItemRevision();
            String propValue1 = toolItemRevision.getProperty(SDVPropertyConstant.TOOL_MAIN_CLASS);
            String propValue2 = toolItemRevision.getProperty(SDVPropertyConstant.TOOL_SUB_CLASS);
            
            for (String toolKeyCode : arrToolCodes) {
                if (toolKeyCode.equals(propValue1 + "-" + propValue2)) {
                    return true;
                }                
            }
        }
        return false;
    }
    
    
    /**
     * 설비의 ID 앞 3자리 값 체크 함수
     * 
     * @param toolList, arrToolCodes
     * @return boolean
     * @throws TCException 
     */    
    private boolean checkExistEquip(ArrayList<TCComponentBOMLine> equipList, String[] arrEquipCodes) throws TCException {
        for (TCComponentBOMLine equipBOMLine : equipList) {
            TCComponentItemRevision equipItemRevision = equipBOMLine.getItemRevision();
            String equipId = equipItemRevision.getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
            
            for (String equipKeyCode : arrEquipCodes) {
                if (equipId.startsWith(equipKeyCode)) {
                    return true;
                }                
            }
        }
        return false;
    }
    
    /**
     * 소켓 공구 체크 함수 (toolList에 소켓공구가 있는지 체크)
     * 
     * @param toolList
     * @return boolean
     * @throws TCException 
     */    
    private boolean checkSocketTool(ArrayList<TCComponentBOMLine> toolList) throws TCException {
        for (TCComponentBOMLine toolBOMLine : toolList) {
            TCComponentItemRevision toolItemRevision = toolBOMLine.getItemRevision();
            String toolCategory = toolItemRevision.getProperty(SDVPropertyConstant.TOOL_RESOURCE_CATEGORY);          
            
            if (toolCategory.equals("SOC")) {
                return true;
            }                
        }
        return false;
    }

    /**
     * Activity의 SystemCode(작업코드) 체크 함수
     * (배열 arrActivityCodes의 작업코드값에 해당하는 Activity가 arrActivityTCComps에 있는지 체크하는 함수)
     * 
     * @param arrActivityTCComps, arrActivityCodes
     * @return boolean
     */
    private boolean checkExistActivity(TCComponent[] arrActivityTCComps, String[] arrActivityCodes) throws TCException {
        for (TCComponent childActivityTCComp : arrActivityTCComps) {
            TCComponentMEActivity childActivity = (TCComponentMEActivity) childActivityTCComp;
            childActivity.refresh();
            
            String systemCode = childActivity.getProperty(SDVPropertyConstant.ACTIVITY_SYSTEM_CODE);
            
            for(String activitySysCode : arrActivityCodes) {
                if(systemCode.startsWith(activitySysCode.trim())) {
                    return true;
                }
            }
        }
        return false;
    }
}
