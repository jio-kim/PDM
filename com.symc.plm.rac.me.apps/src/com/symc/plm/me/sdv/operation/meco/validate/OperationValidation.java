/**
 *
 */
package com.symc.plm.me.sdv.operation.meco.validate;

import com.teamcenter.rac.util.Registry;

/**
 * Class Name : OperationValidation
 * Class Description :
 *
 *[SR150415-005][20150518] shcho, 용접점 할당 오류 검증 기능 추가 (용접공법이 아닌 곳에 용접점이 존재하는지 검증)
 *
 * @date 2013. 12. 10.
 *
 */
public abstract class OperationValidation<T, R> {

    public T target;
    public R result;

    public static Registry registry = Registry.getRegistry(OperationValidation.class);

    public final static int MSG_TYPE_OP_DUPASSIGN = 0; // 공법 중복 할당 메세지
    public final static int MSG_TYPE_OP_LINE_MATCH = 1; // 공법 라인 불일치 메세지
    public final static int MSG_TYPE_ENDITEM = 2; // END ITEM 메세지
    public final static int MSG_TYPE_TOOL = 3; // 공구 메세지
    public final static int MSG_TYPE_SUBPART = 4; // 부자재 메세지
    public final static int MSG_TYPE_EQUIPMENT = 5; // 부자재 메세지
    public final static int MSG_TYPE_ACTIVITY = 6; // ACTIVITY 메세지

    public final static int ERROR_TYPE_ENITEM_QTY_EMPTY = 10; // End Item 수량
    public final static int ERROR_TYPE_ENDITEM_SEQ_EMPTY = 11; // End Item Sequence

    public final static int ERROR_TYPE_SUBPART_OPTION_EMPTY = 12; // 부자재 옵션
    public final static int ERROR_TYPE_SUBPART_QTY_EMPTY = 13; // 부자재 수량
    public final static int ERROR_TYPE_SUBPART_DAYORNIGHT_EMPTY = 14; // 부자재 주,야간 구분
    public final static int ERROR_TYPE_TOOL_QTY_EMPTY = 15; // 공구 수량
    public final static int ERROR_TYPE_TOOL_QTY_INVALID = 16; // 공구 수량 잘못된 값
    public final static int ERROR_TYPE_TOOL_TORQUE_EMPTY = 17; // 공구 토크
    public final static int ERROR_TYPE_TOOL_NOTASSIGNED = 18; // 공구 미할당
    public final static int ERROR_TYPE_PS_NOT_EXIST = 19; // 작업 표준서 미존재
    public final static int ERROR_TYPE_ACTIVITY_FREQUENCY = 20; // 난이도 계수
    public final static int ERROR_TYPE_OP_STATION_NOT_INVALID = 21; // 공법 공정 코드
    public final static int ERROR_TYPE_OP_WORKERCODE_NOT_INVALID = 22; // 공법 작업자 코드
    public final static int ERROR_TYPE_OP_DUPLICATE_ASSIGNED = 23; // 공법 중복할당
    public final static int ERROR_TYPE_WP_NOT_EXIST = 24; // 용접 조건표 미존재
    public final static int ERROR_TYPE_WP_NOTASSIGNED = 25; // 용접점 미할당
    public final static int ERROR_TYPE_CHECK_UNLINK = 26; // 할당이 끈어짐
    public final static int ERROR_TYPE_WP_CONDITION_SHEET_UPDATE = 27; // 용접조건표 Update 필요함
    public final static int ERROR_TYPE_GUN_DUPLICATE_ASSIGNED = 28;  // 용접 공법의 Gun 중복 할당

    public final static int ERROR_TYPE_ENDITEM_SEQ_INVALID = 31;  //End Item Sequence (1~9)
    public final static int ERROR_TYPE_TOOL_SEQ_INVALID = 32;  //공구 Sequence (10~200)
    public final static int ERROR_TYPE_EQUIPMENT_SEQ_INVALID = 33;  //설비 Sequence (210~500)
    public final static int ERROR_TYPE_SUBPART_SEQ_INVALID = 34;  //부자재 Sequence (510~800)

    public final static int ERROR_TYPE_TOOL_TORGUE_VALUE_INVALID = 35;  //토크 값 체크
    public final static int ERROR_TYPE_ACTIVITY_CATEGOLRY_EMPTY = 36;  //Category 값이 미입력 되었을 경우 체크
    public final static int ERROR_TYPE_WP_MERESOURCE_CHECK = 37;  //용접공법에 MEResource 할당 체크
    public final static int ERROR_TYPE_OP_NOT_EXIST = 38;  // 공정에 공법 존재여부 체크
    public final static int ERROR_TYPE_ACTIVITY_WORKER_EMPTY = 39;  // Activity Category 에서 작업자 정미일 경우 작업자 코드가 비어 있는지 체크
    public final static int ERROR_TYPE_ACTIVITY_UNITTIME_EMPTY = 40; // Activity 의 UnitTime 이 들어 있는지 체크
   
    public final static int ERROR_TYPE_BOMLINE_STATION_LINE_INVALID = 41; //선택된 BOMLine이 Line 또는 Station 인지 체크
    public final static int ERROR_TYPE_TOOL_ASSIGN_INVALID = 42;  //할당된 공구 검증 (필수 할당 체크)
    public final static int ERROR_TYPE_TOOL_NOTASSIGN_INVALID = 43;  //할당된 공구 검증 (할당 금지 체크)

    public final static int MSG_VALID_START = 30;

    // [SR150415-005][20150518] shcho, 용접점 할당 오류 검증 기능 추가 (용접공법이 아닌 곳에 용접점이 존재하는지 검증)
    public final static int ERROR_TYPE_WP_ASSIGNED = 44; // 용접점 잘못 된 할당

    public R execute(T target) {
        try {

            this.target = target;
            executeValidation();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    protected abstract void executeValidation() throws Exception;

    public static String getMessage(int type, String param1) {
        return getMessage(type, param1, "", true);
    }

    public static String getMessage(int type, String param1, String param2) {
        return getMessage(type, param1, param2, true);
    }

    /**
     * 정해진 Error Message 를 가져옴
     * FIXME: Registry 변경 필요
     *
     * @method getErrMsg
     * @date 2013. 12. 11.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    public static String getMessage(int type, String param1, String param2, boolean isNewLine) {
        String errorMsg = "";
        switch (type) {
        case ERROR_TYPE_ENITEM_QTY_EMPTY:
            errorMsg = "     " + registry.getString("EndItemQtyEmty.MSG").replace("%0", param1).replace("%1", param2);
            break;
        case ERROR_TYPE_ENDITEM_SEQ_EMPTY:
            errorMsg = "     " + registry.getString("EndItemSeqEmty.MSG").replace("%0", param1).replace("%1", param2);
            break;
        case ERROR_TYPE_SUBPART_QTY_EMPTY:
            errorMsg = "     " + registry.getString("SubPartQtyEmty.MSG").replace("%0", param1).replace("%1", param2);
            break;
        case ERROR_TYPE_SUBPART_OPTION_EMPTY:
            errorMsg = "     " + registry.getString("SubPartOptionEmty.MSG").replace("%0", param1).replace("%1", param2);
            break;
        case ERROR_TYPE_SUBPART_DAYORNIGHT_EMPTY:
            errorMsg = "     " + registry.getString("SubPartDayOrNightEmty.MSG").replace("%0", param1).replace("%1", param2);
            break;
        case ERROR_TYPE_TOOL_QTY_EMPTY:
            errorMsg = "     " + registry.getString("ToolQtyEmty.MSG").replace("%0", param1).replace("%1", param2);
            break;
        case ERROR_TYPE_TOOL_QTY_INVALID:
            errorMsg = "     " + registry.getString("ToolQtyInvalid.MSG").replace("%0", param1).replace("%1", param2);
            break;
        case ERROR_TYPE_TOOL_TORQUE_EMPTY:
            errorMsg = "     " + registry.getString("TorqueEmty.MSG").replace("%0", param1).replace("%1", param2);
            break;
        case ERROR_TYPE_TOOL_NOTASSIGNED:
            errorMsg = "     " + registry.getString("TorqueNotAssigned.MSG").replace("%0", param1).replace("%1", param2);
            break;
        case ERROR_TYPE_PS_NOT_EXIST:
            errorMsg = "     " + registry.getString("PSNotExist.MSG").replace("%0", param1);
            break;
        case ERROR_TYPE_ACTIVITY_FREQUENCY:
            errorMsg = "     " + registry.getString("FrequencyOverOne.MSG").replace("%0", param1);
            break;
        case ERROR_TYPE_OP_STATION_NOT_INVALID:
            errorMsg = "     " + registry.getString("StationCodeInvalid.MSG").replace("%0", param1);
            break;
        case ERROR_TYPE_OP_WORKERCODE_NOT_INVALID:
            errorMsg = "     " + registry.getString("WorkerCodeInvalid.MSG").replace("%0", param1);
            break;
        case ERROR_TYPE_OP_DUPLICATE_ASSIGNED:
            errorMsg = "     " + registry.getString("OPDuplicated.MSG").replace("%0", param1).replace("%1", param2);
            break;
        case ERROR_TYPE_WP_NOT_EXIST:
            errorMsg = "     " + registry.getString("WPNotExist.MSG").replace("%0", param1).replace("%1", param2);
            break;
        case ERROR_TYPE_WP_NOTASSIGNED:
            errorMsg = "     " + registry.getString("WPNotAssigned.MSG").replace("%0", param1).replace("%1", param2);
            break;
        case ERROR_TYPE_CHECK_UNLINK:
            errorMsg = "     " + registry.getString("CheckUnLink.MSG").replace("%0", param1).replace("%1", param2);
            break;
        case ERROR_TYPE_WP_CONDITION_SHEET_UPDATE:
            errorMsg = "     " + registry.getString("WPConditionSheetUpdate.MSG").replace("%0", param1).replace("%1", param2);
            break;
        case ERROR_TYPE_GUN_DUPLICATE_ASSIGNED:
            errorMsg = "     " + registry.getString("GunDuplicated.MSG").replace("%0", param1).replace("%1", param2);
            break;
        case MSG_VALID_START:
            errorMsg = " " + registry.getString("StartVerify.MSG").replace("%0", param1).replace("%1", param2);
            break;
        case ERROR_TYPE_ENDITEM_SEQ_INVALID:
            errorMsg = "     " + registry.getString("EndItemSeqInvalid.MSG").replace("%0", param1).replace("%1", param2);
            break;
        case ERROR_TYPE_TOOL_SEQ_INVALID:
            errorMsg = "     " + registry.getString("ToolSeqInvalid.MSG").replace("%0", param1).replace("%1", param2);
            break;
        case ERROR_TYPE_EQUIPMENT_SEQ_INVALID:
            errorMsg = "     " + registry.getString("EquipmentSeqInvalid.MSG").replace("%0", param1).replace("%1", param2);
            break;
        case ERROR_TYPE_SUBPART_SEQ_INVALID:
            errorMsg = "     " + registry.getString("SubPartSeqInvalid.MSG").replace("%0", param1).replace("%1", param2);
            break;
        case ERROR_TYPE_TOOL_TORGUE_VALUE_INVALID:
            errorMsg = "     " + registry.getString("ToolTorgueValueInvalid.MSG").replace("%0", param1).replace("%1", param2);
            break;
        case ERROR_TYPE_ACTIVITY_CATEGOLRY_EMPTY:
            errorMsg = "     " + registry.getString("CategoryEmpty.MSG").replace("%0", param1);
            break;
        case ERROR_TYPE_WP_MERESOURCE_CHECK:
            errorMsg = "     " + registry.getString("MERsourceAssigned.MSG").replace("%0", param1).replace("%1", param2);
            break;
        case ERROR_TYPE_OP_NOT_EXIST:
            errorMsg = "     " + registry.getString("OperationNotExist.MSG").replace("%0", param1);
            break;
        case ERROR_TYPE_ACTIVITY_WORKER_EMPTY:
            errorMsg = "     " + registry.getString("WorkerCodeEmpty.MSG").replace("%0", param1);
            break;
        case ERROR_TYPE_ACTIVITY_UNITTIME_EMPTY:
            errorMsg = "     " + registry.getString("UnitTimeEmpty.MSG").replace("%0", param1);
            break;
        case ERROR_TYPE_BOMLINE_STATION_LINE_INVALID:
            errorMsg = "     " + registry.getString("NotValidSelectedType2.MSG").replace("%0", param1).replace("%1", param2);
            break;
        case ERROR_TYPE_TOOL_ASSIGN_INVALID:
            errorMsg = "     " + registry.getString("ToolAssignInvalid.MSG").replace("%0", param1);
            break;
        case ERROR_TYPE_TOOL_NOTASSIGN_INVALID:
            errorMsg = "     " + registry.getString("ToolNotAssignInvalid.MSG").replace("%0", param1);
            break;
        // [SR150415-005][20150518] shcho, 용접점 할당 오류 검증 기능 추가 (용접공법이 아닌 곳에 용접점이 존재하는지 검증)
        case ERROR_TYPE_WP_ASSIGNED:
            errorMsg = "     " + registry.getString("WPAssigned.MSG").replace("%0", param1);
            break;
        }
        errorMsg += (isNewLine ? "\r\n" : "");
        return errorMsg;
    }
}
