/**
 *
 */
package com.symc.plm.me.sdv.operation.meco.validate;

import java.util.ArrayList;

import com.kgm.common.WaitProgressBar;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.utils.CustomUtil;
import com.symc.plm.me.utils.SYMTcUtil;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.TCComponentBOMLine;

/**
 * 
 * Class Name : ValidatorManager
 * Class Description :
 * 
 * [SR140702-044][20140702] shcho 1. 공법/용접공법 만 Solution에 있는 경우 Validate 할 수 있도록 오류 수정 (기존에는 Station, Line이 아닌 공법만 있는 경우 Validate 안되도록 처리되어 있었음)
 * 2. 용접공법을 옵션별로 생성 할 수 있게 변경 되면서 두개의 용접공법에 하나의 GUN이 중복 할당 될 수 있도록 Validate 조건 변경
 * 
 *  [SR140924-021][20141006] shcho, 도장 부자재 소요량 체크 기능 추가(조립과 동일하게)
 *  [SR150415-005][20150518] shcho, 용접점 할당 오류 검증 기능 추가 (용접공법이 아닌 곳에 용접점이 존재하는지 검증)
 *  
 * @date 2013. 12. 10.
 * 
 */
public class ValidateManager {
    private WaitProgressBar progress = null;
    private boolean isValidOK = true;

    /**
     * BOP 타입
     * Class Name : BOPTYPE
     * Class Description :
     * 
     * @date 2013. 12. 12.
     * 
     */
    public static enum BOPTYPE {
        ASSEMBLY, BODY, PAINT
    }

    public ValidateManager(WaitProgressBar progress) {
        this.progress = progress;
    }

    /**
     * Validation 실행
     * 
     * @method executeValidation
     * @date 2013. 12. 12.
     * @param
     * @return boolean
     * @exception
     * @throws
     * @see
     */
    public boolean executeValidation(BOPTYPE bopType, TCComponentBOMLine targetBOMLine) throws Exception {
        if (bopType == BOPTYPE.ASSEMBLY)
            startAssemblyValidation(targetBOMLine);
        else if (bopType == BOPTYPE.PAINT)
            startPaintValidation(targetBOMLine);
        else if (bopType == BOPTYPE.BODY)
            startBodyValidation(targetBOMLine);

        return isValidOK;
    }

    /**
     * 조립 검증 시작
     * 
     * @method assemblyValidation
     * @date 2013. 12. 10.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void startAssemblyValidation(TCComponentBOMLine targetBOMLine) throws Exception {

        String itemType = targetBOMLine.getItem().getType();
        ArrayList<TCComponentBOMLine> operationList = new ArrayList<TCComponentBOMLine>();
        // Line 을 선택할 경우
        if (itemType.equals(SDVTypeConstant.BOP_PROCESS_LINE_ITEM)) {
            String targetId = targetBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
            getOperationListOfLine(targetBOMLine, operationList, BOPTYPE.ASSEMBLY);
            /**
             * 공법 중복 할당 유무
             */
            String errorMsg = new DuplcateOPValidation().execute(operationList);
            if (errorMsg != null) {
                displayMessage(OperationValidation.getMessage(OperationValidation.MSG_VALID_START, targetId, "Line", false), true);
                displayMessage(errorMsg, false);
                if (errorMsg != null)
                    isValidOK = false;
            }

            for (TCComponentBOMLine operationBOMLine : operationList)
                executeAssemblyValidation(operationBOMLine);

        } else if (itemType.equals(SDVTypeConstant.BOP_PROCESS_ASSY_OPERATION_ITEM)) {
            executeAssemblyValidation(targetBOMLine);
        }
    }

    /**
     * 도장 검증 시작
     * 
     * @method startPaintValidation
     * @date 2013. 12. 12.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void startPaintValidation(TCComponentBOMLine targetBOMLine) throws Exception {

        String itemType = targetBOMLine.getItem().getType();
        ArrayList<TCComponentBOMLine> operationList = new ArrayList<TCComponentBOMLine>();
        // Line 을 선택할 경우
        if (itemType.equals(SDVTypeConstant.BOP_PROCESS_LINE_ITEM)) {
            String targetId = targetBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
            getOperationListOfLine(targetBOMLine, operationList, BOPTYPE.PAINT);
            /**
             * 공법 중복 할당 유무
             */
            String errorMsg = new DuplcateOPValidation().execute(operationList);
            if (errorMsg != null && errorMsg.length() > 0) {
                displayMessage(OperationValidation.getMessage(OperationValidation.MSG_VALID_START, targetId, "Line", false), true);
                displayMessage(errorMsg, false);
                if (errorMsg != null)
                    isValidOK = false;
            }
            for (TCComponentBOMLine operationBOMLine : operationList)
                executePaintValidation(operationBOMLine);

        } else if (itemType.equals(SDVTypeConstant.BOP_PROCESS_PAINT_OPERATION_ITEM)) {
            executePaintValidation(targetBOMLine);
        }
    }

    /**
     * 차체 검증 시작
     * 
     * @method startPaintValidation
     * @date 2013. 12. 12.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void startBodyValidation(TCComponentBOMLine targetBOMLine) throws Exception {

        String itemType = targetBOMLine.getItem().getType();
        ArrayList<TCComponentBOMLine> operationList = new ArrayList<TCComponentBOMLine>();
        // Line 또는 Station을 선택할 경우
        if (itemType.equals(SDVTypeConstant.BOP_PROCESS_LINE_ITEM) || itemType.equals(SDVTypeConstant.BOP_PROCESS_STATION_ITEM)) {
            // Line 을 선택할 경우
            if (itemType.equals(SDVTypeConstant.BOP_PROCESS_LINE_ITEM)) {
                /**
                 * 용접점 존재 여부 검증
                 */
                String errorMsg = null;
                errorMsg = new WeldPointAssignValidation2().execute(targetBOMLine);
                displayMessage(errorMsg, false);
                if (errorMsg != null)
                    isValidOK = false;
                
                for (AIFComponentContext targetBOMLineChild : targetBOMLine.getChildren()) {
                    TCComponentBOMLine lineChildLine = (TCComponentBOMLine) targetBOMLineChild.getComponent();
                    if (lineChildLine.getItem().getType().equals(SDVTypeConstant.BOP_PROCESS_STATION_ITEM)) {
                        executeBodyStationValidation(lineChildLine);
                    }
                }
            }
            // Station을 선택한 경우
            else if (itemType.equals(SDVTypeConstant.BOP_PROCESS_STATION_ITEM)) {
                executeBodyStationValidation(targetBOMLine);
            }

            String targetId = targetBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
            getOperationListOfLine(targetBOMLine, operationList, BOPTYPE.BODY);

            /**
             * 공법 중복 할당 유무
             */
            String errorMsg = new DuplcateOPValidation().execute(operationList);
            if (errorMsg != null && errorMsg.length() > 0) {
                displayMessage(OperationValidation.getMessage(OperationValidation.MSG_VALID_START, targetId, "Line", false), true);
                displayMessage(errorMsg, false);
                if (errorMsg != null)
                    isValidOK = false;
            }

            for (TCComponentBOMLine operationBOMLine : operationList) {
                if (operationBOMLine.getItem().getType().equals(SDVTypeConstant.BOP_PROCESS_BODY_OPERATION_ITEM))
                    executeBodyValidation(operationBOMLine);
                else if (operationBOMLine.getItem().getType().equals(SDVTypeConstant.BOP_PROCESS_BODY_WELD_OPERATION_ITEM))
                    executeWeldValidation(operationBOMLine);
            }

        }
        // Target이 Line, Station 이외의 경우
        // [SR140702-044][20140702] shcho 공법/용접공법 만 Solution에 있는 경우 Validate 할수 있도록 오류 수정 (기존에는 Station, Line이 아닌 공법만 있는 경우 Validate 안되도록 처리되어 있었음)
        else if (itemType.equals(SDVTypeConstant.BOP_PROCESS_BODY_OPERATION_ITEM)) {
            executeBodyValidation(targetBOMLine);
        } else if (itemType.equals(SDVTypeConstant.BOP_PROCESS_BODY_WELD_OPERATION_ITEM)) {
            executeWeldValidation(targetBOMLine);
        }
        // else {
        // String targetId = targetBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
        // String itemName = targetBOMLine.getProperty(SDVPropertyConstant.BL_OBJECT_NAME);
        // String errorMsg = OperationValidation.getMessage(OperationValidation.ERROR_TYPE_OP_DUPLICATE_ASSIGNED, targetId, itemName);
        //
        // displayMessage(OperationValidation.getMessage(OperationValidation.MSG_VALID_START, targetId, "BOMLine", false), true);
        // displayMessage(errorMsg, false);
        // if (errorMsg != null)
        // isValidOK = false;
        // }
    }

    /**
     * 조립 공법 검증 실행
     * 
     * @method executeAssemblyValidation
     * @date 2013. 12. 11.
     * @param
     * @return boolean
     * @exception
     * @throws
     * @see
     */
    private boolean executeAssemblyValidation(TCComponentBOMLine targetBOMLine) throws Exception {
        String errorMsg = null;
        boolean isReleased = CustomUtil.isReleased(targetBOMLine.getItemRevision());
        if (isReleased) {
            return false;
        }
        String targetId = targetBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
        displayMessage(OperationValidation.getMessage(OperationValidation.MSG_VALID_START, targetId, "Operation", false), true);
        /**
         * 공법 정보 검증
         */
        errorMsg = new OpInformValidation().execute(targetBOMLine);
        displayMessage(errorMsg, false);
        if (errorMsg != null)
            isValidOK = false;

        /**
         * Activity 겁증
         */
        errorMsg = new ActivitiyValidation().execute(targetBOMLine);
        displayMessage(errorMsg, false);
        if (errorMsg != null)
            isValidOK = false;

        /**
         * 작업표준서 존재 유무 검증
         */
        errorMsg = new ProcessSheetValidation().execute(targetBOMLine);
        displayMessage(errorMsg, false);
        if (errorMsg != null)
            isValidOK = false;

        return isValidOK;
    }

    /**
     * 차체 공정 검증 실행
     * 
     * [SR150415-005][20150518] shcho, 용접점 할당 오류 검증 기능 추가 (용접공법이 아닌 곳에 용접점이 존재하는지 검증)
     * 
     * @method executeBodyValidation
     * @date 2013. 12. 18.
     * @param
     * @return boolean
     * @exception
     * @throws
     * @see
     */
    private boolean executeBodyStationValidation(TCComponentBOMLine targetBOMLine) throws Exception {
        String errorMsg = null;

        String targetId = targetBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
        displayMessage(OperationValidation.getMessage(OperationValidation.MSG_VALID_START, targetId, "Station", false), true);
        /**
         * 공정하위 공법 존재 여부 검증
         */
        errorMsg = new OperationDoesNotExistCheckValidation().execute(targetBOMLine);
        displayMessage(errorMsg, false);
        if (errorMsg != null)
            isValidOK = false;
        
        /**
         * 공정하위 용접점 존재 여부 검증
         */
        errorMsg = new WeldPointAssignValidation2().execute(targetBOMLine);
        displayMessage(errorMsg, false);
        if (errorMsg != null)
            isValidOK = false;

        return isValidOK;
    }

    /**
     * 차체 공법 검증 실행
     * 
     * [SR150415-005][20150518] shcho, 용접점 할당 오류 검증 기능 추가 (용접공법이 아닌 곳에 용접점이 존재하는지 검증)
     * 
     * @method executeBodyValidation
     * @date 2013. 12. 18.
     * @param
     * @return boolean
     * @exception
     * @throws
     * @see
     */
    private boolean executeBodyValidation(TCComponentBOMLine targetBOMLine) throws Exception {
        String errorMsg = null;

        String targetId = targetBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
        displayMessage(OperationValidation.getMessage(OperationValidation.MSG_VALID_START, targetId, "Operation", false), true);
        /**
         * 공법 정보 검증
         */
        errorMsg = new OpBodyInformValidation().execute(targetBOMLine);
        displayMessage(errorMsg, false);
        if (errorMsg != null)
            isValidOK = false;

        /**
         * Activity 겁증
         */
        errorMsg = new ActivityBodyValidation().execute(targetBOMLine);
        displayMessage(errorMsg, false);
        if (errorMsg != null)
            isValidOK = false;

        /**
         * 작업표준서 존재 유무 검증
         */
        errorMsg = new ProcessSheetValidation().execute(targetBOMLine);
        displayMessage(errorMsg, false);
        if (errorMsg != null)
            isValidOK = false;

        /**
         * 할당이 끈어진 항목 찾기
         */
        errorMsg = new CheckUnlinkValidation().execute(targetBOMLine);
        displayMessage(errorMsg, false);
        if (errorMsg != null)
            isValidOK = false;
        
        /**
         * 용접점 존재 여부 검증
         */
        errorMsg = new WeldPointAssignValidation2().execute(targetBOMLine);
        displayMessage(errorMsg, false);
        if (errorMsg != null)
            isValidOK = false;
        
        return isValidOK;
    }

    /**
     * 
     * 
     * @method executeWeldValidation
     * @date 2013. 12. 18.
     * @param
     * @return boolean
     * @exception
     * @throws
     * @see
     */
    private boolean executeWeldValidation(TCComponentBOMLine targetBOMLine) throws Exception {
        String errorMsg = null;

        String targetId = targetBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
        displayMessage(OperationValidation.getMessage(OperationValidation.MSG_VALID_START, targetId, "Operation", false), true);
        /**
         * 용접조건표 존재 유무 검증 / 용접조건표 수정 필요 유무
         */
        errorMsg = new WeldConditionSheetValidation().execute(targetBOMLine);
        displayMessage(errorMsg, false);
        if (errorMsg != null)
            isValidOK = false;

        /**
         * 용접공법에 용접점 존재 유무
         */
        errorMsg = new WeldPointAssignValidation().execute(targetBOMLine);
        displayMessage(errorMsg, false);
        if (errorMsg != null)
            isValidOK = false;

        /**
         * 할당이 끈어진 항목 찾기
         */
        errorMsg = new CheckUnlinkValidation().execute(targetBOMLine);
        displayMessage(errorMsg, false);
        if (errorMsg != null)
            isValidOK = false;

        /**
         * 용접공법에 Gun 중복할당
         * [SR140702-044][20140702] shcho 용접공법을 옵션별로 생성 할 수 있게 변경 되면서 두개의 용접공법에 하나의 GUN이 중복 할당 될 수 있도록 Validate 조건 변경
         */
        // errorMsg = new PlantDuplicationCheckValidation().execute(targetBOMLine);
        // displayMessage(errorMsg, false);
        // if (errorMsg != null)
        // isValidOK = false;

        /**
         * 용접공법에 MEResource 할당 여부
         * 
         */
        errorMsg = new WeldOperationMEResourceCheckValidation().execute(targetBOMLine);
        displayMessage(errorMsg, false);
        if (errorMsg != null)
            isValidOK = false;

        return isValidOK;
    }

    /**
     * 도장 공법 검증 실행
     * 
     * @method executePaintValidation
     * @date 2013. 12. 11.
     * @param
     * @return boolean
     * @exception
     * @throws
     * @see
     */
    private boolean executePaintValidation(TCComponentBOMLine targetBOMLine) throws Exception {
        String errorMsg = null;
        boolean isReleased = CustomUtil.isReleased(targetBOMLine.getItemRevision());
        if (isReleased) {
            return false;
        }
        String targetId = targetBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
        displayMessage(OperationValidation.getMessage(OperationValidation.MSG_VALID_START, targetId, "Operation", false), true);

        /**
         * 공법 정보 검증
         * [SR140924-021][20141006] shcho, 도장 부자재 소요량 체크 기능 추가(조립과 동일하게)
         */
        errorMsg = new OpPaintInformValidation().execute(targetBOMLine);
        displayMessage(errorMsg, false);
        if (errorMsg != null)
            isValidOK = false;

      
        /**
         * Activity 겁증
         */
        errorMsg = new ActivityCommonValidation().execute(targetBOMLine);
        displayMessage(errorMsg, false);
        if (errorMsg != null)
            isValidOK = false;

        /**
         * 작업표준서 존재 유무 검증
         */
        errorMsg = new ProcessSheetValidation().execute(targetBOMLine);
        displayMessage(errorMsg, false);
        if (errorMsg != null)
            isValidOK = false;

        return isValidOK;
    }

    /**
     * 메세지를 Display 시킴
     * 
     * @method displayMessage
     * @date 2013. 12. 11.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void displayMessage(String msg, boolean nextLine) {

        if (msg == null)
            return;

        progress.setStatus(msg, nextLine);
    }

    /**
     * Line의 하위 공법을 가져옴
     * 
     * @method getOperationListOfLine
     * @date 2013. 12. 12.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void getOperationListOfLine(TCComponentBOMLine parentBOMLine, ArrayList<TCComponentBOMLine> opList, BOPTYPE bopType) throws Exception {

        String itemType = parentBOMLine.getItem().getType();
        AIFComponentContext[] contexts = parentBOMLine.getChildren();
        for (AIFComponentContext context : contexts) {
            TCComponentBOMLine childBOMLine = (TCComponentBOMLine) context.getComponent();
            String childItemType = childBOMLine.getItem().getType();
            if (itemType.equals(SDVTypeConstant.BOP_PROCESS_LINE_ITEM)) {
                if (bopType == BOPTYPE.ASSEMBLY) {
                    if (!childItemType.equals(SDVTypeConstant.BOP_PROCESS_ASSY_OPERATION_ITEM))
                        continue;
                    if (!SYMTcUtil.isBOMWritable(childBOMLine))
                        continue;
                    opList.add(childBOMLine);
                } else if (bopType == BOPTYPE.BODY || bopType == BOPTYPE.PAINT)
                    getOperationListOfLine(childBOMLine, opList, bopType);
            } else if (itemType.equals(SDVTypeConstant.BOP_PROCESS_STATION_ITEM)) {
                if (!childItemType.equals(SDVTypeConstant.BOP_PROCESS_BODY_OPERATION_ITEM) && !childItemType.equals(SDVTypeConstant.BOP_PROCESS_PAINT_OPERATION_ITEM) && !childItemType.equals(SDVTypeConstant.BOP_PROCESS_BODY_WELD_OPERATION_ITEM))
                    continue;
                if (!SYMTcUtil.isBOMWritable(childBOMLine))
                    continue;
                opList.add(childBOMLine);
            }
        }
    }
}
