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
 * [SR140702-044][20140702] shcho 1. ����/�������� �� Solution�� �ִ� ��� Validate �� �� �ֵ��� ���� ���� (�������� Station, Line�� �ƴ� ������ �ִ� ��� Validate �ȵǵ��� ó���Ǿ� �־���)
 * 2. ���������� �ɼǺ��� ���� �� �� �ְ� ���� �Ǹ鼭 �ΰ��� ���������� �ϳ��� GUN�� �ߺ� �Ҵ� �� �� �ֵ��� Validate ���� ����
 * 
 *  [SR140924-021][20141006] shcho, ���� ������ �ҿ䷮ üũ ��� �߰�(������ �����ϰ�)
 *  [SR150415-005][20150518] shcho, ������ �Ҵ� ���� ���� ��� �߰� (���������� �ƴ� ���� �������� �����ϴ��� ����)
 *  
 * @date 2013. 12. 10.
 * 
 */
public class ValidateManager {
    private WaitProgressBar progress = null;
    private boolean isValidOK = true;

    /**
     * BOP Ÿ��
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
     * Validation ����
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
     * ���� ���� ����
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
        // Line �� ������ ���
        if (itemType.equals(SDVTypeConstant.BOP_PROCESS_LINE_ITEM)) {
            String targetId = targetBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
            getOperationListOfLine(targetBOMLine, operationList, BOPTYPE.ASSEMBLY);
            /**
             * ���� �ߺ� �Ҵ� ����
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
     * ���� ���� ����
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
        // Line �� ������ ���
        if (itemType.equals(SDVTypeConstant.BOP_PROCESS_LINE_ITEM)) {
            String targetId = targetBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
            getOperationListOfLine(targetBOMLine, operationList, BOPTYPE.PAINT);
            /**
             * ���� �ߺ� �Ҵ� ����
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
     * ��ü ���� ����
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
        // Line �Ǵ� Station�� ������ ���
        if (itemType.equals(SDVTypeConstant.BOP_PROCESS_LINE_ITEM) || itemType.equals(SDVTypeConstant.BOP_PROCESS_STATION_ITEM)) {
            // Line �� ������ ���
            if (itemType.equals(SDVTypeConstant.BOP_PROCESS_LINE_ITEM)) {
                /**
                 * ������ ���� ���� ����
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
            // Station�� ������ ���
            else if (itemType.equals(SDVTypeConstant.BOP_PROCESS_STATION_ITEM)) {
                executeBodyStationValidation(targetBOMLine);
            }

            String targetId = targetBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
            getOperationListOfLine(targetBOMLine, operationList, BOPTYPE.BODY);

            /**
             * ���� �ߺ� �Ҵ� ����
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
        // Target�� Line, Station �̿��� ���
        // [SR140702-044][20140702] shcho ����/�������� �� Solution�� �ִ� ��� Validate �Ҽ� �ֵ��� ���� ���� (�������� Station, Line�� �ƴ� ������ �ִ� ��� Validate �ȵǵ��� ó���Ǿ� �־���)
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
     * ���� ���� ���� ����
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
         * ���� ���� ����
         */
        errorMsg = new OpInformValidation().execute(targetBOMLine);
        displayMessage(errorMsg, false);
        if (errorMsg != null)
            isValidOK = false;

        /**
         * Activity ����
         */
        errorMsg = new ActivitiyValidation().execute(targetBOMLine);
        displayMessage(errorMsg, false);
        if (errorMsg != null)
            isValidOK = false;

        /**
         * �۾�ǥ�ؼ� ���� ���� ����
         */
        errorMsg = new ProcessSheetValidation().execute(targetBOMLine);
        displayMessage(errorMsg, false);
        if (errorMsg != null)
            isValidOK = false;

        return isValidOK;
    }

    /**
     * ��ü ���� ���� ����
     * 
     * [SR150415-005][20150518] shcho, ������ �Ҵ� ���� ���� ��� �߰� (���������� �ƴ� ���� �������� �����ϴ��� ����)
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
         * �������� ���� ���� ���� ����
         */
        errorMsg = new OperationDoesNotExistCheckValidation().execute(targetBOMLine);
        displayMessage(errorMsg, false);
        if (errorMsg != null)
            isValidOK = false;
        
        /**
         * �������� ������ ���� ���� ����
         */
        errorMsg = new WeldPointAssignValidation2().execute(targetBOMLine);
        displayMessage(errorMsg, false);
        if (errorMsg != null)
            isValidOK = false;

        return isValidOK;
    }

    /**
     * ��ü ���� ���� ����
     * 
     * [SR150415-005][20150518] shcho, ������ �Ҵ� ���� ���� ��� �߰� (���������� �ƴ� ���� �������� �����ϴ��� ����)
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
         * ���� ���� ����
         */
        errorMsg = new OpBodyInformValidation().execute(targetBOMLine);
        displayMessage(errorMsg, false);
        if (errorMsg != null)
            isValidOK = false;

        /**
         * Activity ����
         */
        errorMsg = new ActivityBodyValidation().execute(targetBOMLine);
        displayMessage(errorMsg, false);
        if (errorMsg != null)
            isValidOK = false;

        /**
         * �۾�ǥ�ؼ� ���� ���� ����
         */
        errorMsg = new ProcessSheetValidation().execute(targetBOMLine);
        displayMessage(errorMsg, false);
        if (errorMsg != null)
            isValidOK = false;

        /**
         * �Ҵ��� ������ �׸� ã��
         */
        errorMsg = new CheckUnlinkValidation().execute(targetBOMLine);
        displayMessage(errorMsg, false);
        if (errorMsg != null)
            isValidOK = false;
        
        /**
         * ������ ���� ���� ����
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
         * ��������ǥ ���� ���� ���� / ��������ǥ ���� �ʿ� ����
         */
        errorMsg = new WeldConditionSheetValidation().execute(targetBOMLine);
        displayMessage(errorMsg, false);
        if (errorMsg != null)
            isValidOK = false;

        /**
         * ���������� ������ ���� ����
         */
        errorMsg = new WeldPointAssignValidation().execute(targetBOMLine);
        displayMessage(errorMsg, false);
        if (errorMsg != null)
            isValidOK = false;

        /**
         * �Ҵ��� ������ �׸� ã��
         */
        errorMsg = new CheckUnlinkValidation().execute(targetBOMLine);
        displayMessage(errorMsg, false);
        if (errorMsg != null)
            isValidOK = false;

        /**
         * ���������� Gun �ߺ��Ҵ�
         * [SR140702-044][20140702] shcho ���������� �ɼǺ��� ���� �� �� �ְ� ���� �Ǹ鼭 �ΰ��� ���������� �ϳ��� GUN�� �ߺ� �Ҵ� �� �� �ֵ��� Validate ���� ����
         */
        // errorMsg = new PlantDuplicationCheckValidation().execute(targetBOMLine);
        // displayMessage(errorMsg, false);
        // if (errorMsg != null)
        // isValidOK = false;

        /**
         * ���������� MEResource �Ҵ� ����
         * 
         */
        errorMsg = new WeldOperationMEResourceCheckValidation().execute(targetBOMLine);
        displayMessage(errorMsg, false);
        if (errorMsg != null)
            isValidOK = false;

        return isValidOK;
    }

    /**
     * ���� ���� ���� ����
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
         * ���� ���� ����
         * [SR140924-021][20141006] shcho, ���� ������ �ҿ䷮ üũ ��� �߰�(������ �����ϰ�)
         */
        errorMsg = new OpPaintInformValidation().execute(targetBOMLine);
        displayMessage(errorMsg, false);
        if (errorMsg != null)
            isValidOK = false;

      
        /**
         * Activity ����
         */
        errorMsg = new ActivityCommonValidation().execute(targetBOMLine);
        displayMessage(errorMsg, false);
        if (errorMsg != null)
            isValidOK = false;

        /**
         * �۾�ǥ�ؼ� ���� ���� ����
         */
        errorMsg = new ProcessSheetValidation().execute(targetBOMLine);
        displayMessage(errorMsg, false);
        if (errorMsg != null)
            isValidOK = false;

        return isValidOK;
    }

    /**
     * �޼����� Display ��Ŵ
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
     * Line�� ���� ������ ������
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
