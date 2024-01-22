package com.symc.plm.me.sdv.service.resource;

public class ResourceIDConstants {

    /* ID Validation을 위한 자릿수 정의 배열 */
    public static final int[] ASSY_TOOL_GENERAL = { 1, 2, 3, 3 }; // 조립 일반공구
    public static final int[] ASSY_TOOL_SOCKET = { 1, 2, 3, 3, 3, 1 }; // 조립 소켓공구
    public static final int[] BODY_TOOL_GENERAL = { 1, 2, 2 }; // 차체 일반공구
    public static final int[] PAINT_TOOL_GENERAL = { 1, 3, 2 }; // 도장 일반공구
    public static final int[] PAINT_TOOL_STAY = { 1, 2, 3, 3 }; // 도장 STAY공구

    public static final int[] ASSY_EQUIP_GENERAL = { 1, 1, 2, 3 }; // 조립 일반설비
    public static final int[] ASSY_EQUIP_JIG = { 1, 2, 1, 3 }; // 조립 JIG설비
    public static final int[] BODY_EQUIP_ROBOT = { 1, 2, 3, 0 }; // 차체 로봇
    public static final int[] BODY_EQUIP_GUN = { 1, 2, 2, 0 }; // 차체 건
    //[SR140512-016][20140512] shcho, 차체 Resource ID 체계 변경 (ID 4번째 필드 5자리에서 6자리로 변경) -----------
    public static final int[] BODY_EQUIP_GENERAL = { 1, 2, 2, 6, 2 }; // 차체 일반설비,로봇부대설비,JIG설비
    //----------------------------------------------------------------------------------------------------------
    public static final int[] PAINT_EQUIP_GENERAL = { 1, 1, 2, 2, 2 }; // 도장 일반설비
}
