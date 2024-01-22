package com.symc.plm.me.common;

/**
 * 등록하기 전 반드시 검색 필수!!!!!!!!
 * 
 * @author hybyeon
 * 
 */

public class SDVPropertyConstant {
    public static final String ITEM_REV_ID_ROOT = "000";
    public static final String ITEM_ID_PREFIX = "PTP";

    // MEProcessRevision Properties
    public static final String ME_EXT_DECESSORS = "m7_EXT_DECESSORS";
    public static final String ME_PREDECESSORS = "Mfg0predecessors";
    public static final String ME_SUCCESSORS = "Mfg0successors";

    // Object Properties
    public static final String ITEM_ITEM_ID = "item_id";
    public static final String ITEM_REVISION_ID = "item_revision_id";
    public static final String ITEM_OBJECT_NAME = "object_name";
    public static final String ITEM_OBJECT_DESC = "object_desc";
    public static final String ITEM_OBJECT_DISPLAY_TYPE = "";
    public static final String ITEM_CREATION_DATE = "creation_date";
    public static final String ITEM_LAST_MODIFY_DATE = "last_mod_date";
    public static final String ITEM_LAST_MODIFY_USER = "last_mod_user";
    public static final String ITEM_DATE_RELEASED = "date_released";
    public static final String ITEM_OWNING_USER = "owning_user";
    public static final String ITEM_OBJECT_TYPE = "object_type";
    public static final String ITEM_CHECKED_OUT_USER = "checked_out_user";

    public static final String ITEM_M7_REFERENCE_INFO = "m7_REFERENCE_INFO";
    public static final String ITEM_M7_DISCARD_DATE = "m7_DISCARD_DATE";

    public static final String ITEM_REV_MECO_NO = "m7_MECO_NO";
    public static final String ITEM_REV_RELEASE_STATUS_LIST = "release_status_list";

    // Shop & Shop Revision Master Properties
    public static final String SHOP_ENG_NAME = "m7_ENG_NAME";
    public static final String SHOP_VEHICLE_KOR_NAME = "m7_VEHICLE_KOR_NAME";
    public static final String SHOP_VEHICLE_ENG_NAME = "m7_VEHICLE_ENG_NAME";
    public static final String SHOP_REV_SHOP_CODE = "m7_SHOP";
    public static final String SHOP_REV_PROCESS_TYPE = "m7_PROCESS_TYPE";
    public static final String SHOP_REV_JPH = "m7_JPH";
    public static final String SHOP_REV_PRODUCT_CODE = "m7_PRODUCT_CODE";
    public static final String SHOP_REV_VEHICLE_CODE = "m7_VEHICLE_CODE";
    public static final String SHOP_REV_ALLOWANCE = "m7_ALLOWANCE";
    public static final String SHOP_REV_MECO_NO = "m7_MECO_NO";
    public static final String SHOP_REV_IS_ALTBOP = "m7_IS_ALTBOP";
    public static final String SHOP_REV_ALT_PREFIX = "m7_ALT_PREFIX";
    public static final String SHOP_REV_ENG_NAME = "m7_ENG_NAME";
    public static final String SHOP_REV_KOR_NAME = "m7_KOR_NAME";

    /* [CF-3537] [20230131]	[개선과제]MECO 결재 거부 후 공법 개정 불가
     *  BOP에서 사용하는 기존 MECO 검색 화면에서 반려된 MECO가 검색 안되는 문제가 있어서 수정 
     *  View 화면 호출 및 속성값을 받아오기 위해 속성 추가  
     */
    public static final String MECO_NO = "mecoNo";
    public static final String MECO_REV = "mecoRev";
    public static final String MECO_SELECT = "mecoSelect";
    
    // Line & Line Revision Master Properties
    public static final String LINE_REV_CODE = "m7_LINE";
    public static final String LINE_REV_SHOP_CODE = "m7_SHOP";
    public static final String LINE_REV_PRODUCT_CODE = "m7_PRODUCT_CODE";
    public static final String LINE_REV_VEHICLE_CODE = "m7_VEHICLE_CODE";
    public static final String LINE_REV_ENG_NAME = "m7_ENG_NAME";
    public static final String LINE_PARALLEL_LINE_NO = "m7_PARALLEL_LINE_NO";
    public static final String LINE_REV_JPH = "m7_JPH";
    public static final String LINE_REV_ALLOWANCE = "m7_ALLOWANCE";
    public static final String LINE_REV_MECO_NO = "m7_MECO_NO";
    public static final String LINE_REV_IS_ALTBOP = "m7_IS_ALTBOP";
    public static final String LINE_REV_ALT_PREFIX = "m7_ALT_PREFIX";
    public static final String LINE_REV_PROCESS_TYPE = "m7_PROCESS_TYPE";

    // Station & Station Revision Master Properties
    public static final String STATION_REV_CODE = "m7_STATION_CODE";

    // Operation & Operation Revision Master Properties
    // public static final String OPERATION_ENG_NAME = "m7_ENG_NAME";
    public static final String OPERATION_WORKER_CODE = "m7_WORKER_CODE";
    public static final String OPERATION_PROCESS_SEQ = "m7_PROCESS_SEQ";
    public static final String OPERATION_WORKAREA = "m7_WORKAREA";
    public static final String OPERATION_WORK_UBODY = "m7_WORK_UBODY";
    public static final String OPERATION_ITEM_UL = "m7_ITEM_LOC_UL";
    public static final String OPERATION_MAX_WORK_TIME_CHECK = "m7_MAX_WORK_TIME_CHECK";
    public static final String OPERATION_REP_VEHICLE_CHECK = "m7_REP_VHICLE_CHECK";

    public static final String OPERATION_REV_DR = "m7_DR";
    public static final String OPERATION_REV_STATION_NO = "m7_STATION_NO";
    public static final String OPERATION_REV_STATION_CODE = "m7_STATION_CODE";
    public static final String OPERATION_REV_LINE = "m7_LINE";
    public static final String OPERATION_REV_INSTALL_DRW_NO = "m7_INST_DWG_NO";
    /*
     * 수정점 : 20200110
     * [CF196] 속성 추가 요청 "m7_P_FMEA_NO", "m7_CP_NO"
     */
    public static final String OPERATION_ITEM_P_MEFA_NO = "m7_P_FMEA_NO";
    public static final String OPERATION_ITEM_CP_NO = "m7_CP_NO";

    
    public static final String OPERATION_REV_KPC = "m7_KPC";
    public static final String OPERATION_REV_KOR_NAME = "m7_KOR_NAME";
    public static final String OPERATION_REV_ENG_NAME = "m7_ENG_NAME";
    public static final String OPERATION_REV_SHOP = "m7_SHOP";
    public static final String OPERATION_REV_OPERATION_CODE = "m7_OPERATION_CODE";
    public static final String OPERATION_REV_PRODUCT_CODE = "m7_PRODUCT_CODE";
    public static final String OPERATION_REV_VEHICLE_CODE = "m7_VEHICLE_CODE";
    public static final String OPERATION_REV_BOP_VERSION = "m7_BOP_VERSION";
    public static final String OPERATION_REV_MECO_NO = "m7_MECO_NO";
    public static final String OPERATION_REV_ASSY_SYSTEM = "m7_ASSY_SYSTEM";
    public static final String OPERATION_REV_FUNCTION_CODE = "m7_FUNCTION_CODE";
    public static final String OPERATION_REV_MAX_WORK_TIME_CHECK = "m7_MAX_WORK_TIME_CHECK";
    public static final String OPERATION_REV_REP_VHICLE_CHECK = "m7_REP_VHICLE_CHECK";
    public static final String OPERATION_REV_IS_ALTBOP = "m7_IS_ALTBOP";
    public static final String OPERATION_REV_ALT_PREFIX = "m7_ALT_PREFIX";

    public static final String PAINT_OPERATION_REV_WORKER_COUNT = "m7_WORKER_COUNT";
    
    //이종화 차장님 요청
    // 특별 특성 속성 추가
//    public static final String OPERATION_REV_SPECIAL_CHARACTERISTIC = "m7_SPECIAL_CHARICTERISTIC";
    public static final String OPERATION_REV_SPECIAL_CHARACTERISTIC = "m7_SPECIAL_CHARICTER";
    
    

    // Weld Operation & Operation Revision Master Properties
    public static final String WELDOP_REV_TARGET_OP = "m7_TARGET_OP";
    public static final String WELDOP_REV_LAST_MOD_DATE = "m7_WELD_NAME_LAST_MOD_DATE";

    // Station & Station Revision Master Properties
    public static final String STATION_ENG_NAME = "m7_ENG_NAME";
    public static final String STATION_PROCESS_TYPE = "m7_PROCESS_TYPE";
    public static final String STATION_SHOP = "m7_SHOP";
    public static final String STATION_LINE = "m7_LINE";
    public static final String STATION_STATION_CODE = "m7_STATION_CODE";
    public static final String STATION_PRODUCT_CODE = "m7_PRODUCT_CODE";
    public static final String STATION_VEHICLE_CODE = "m7_VEHICLE_CODE";
    public static final String STATION_IS_ALTBOP = "m7_IS_ALTBOP";
    public static final String STATION_ALT_PREFIX = "m7_ALT_PREFIX";
    public static final String STATION_PARALLEL_STATION_NO = "m7_PARALLEL_STATION_NO";
    public static final String STATION_MECO_NO = "m7_MECO_NO";
    public static final String STATION_BOP_VERSION = "m7_BOP_VERSION";

    // BOPLine Properties
    public static final String BL_ITEM_ID = "bl_item_item_id";
    public static final String BL_ITEM_REV_ID = "bl_rev_item_revision_id";
    public static final String BL_ITEM_REV = "bl_rev_item_revision";
    public static final String BL_OBJECT_NAME = "bl_item_object_name";
    public static final String BL_REV_OBJECT_NAME = "bl_rev_object_name";
    public static final String BL_OBJECT_TYPE = "bl_item_object_type";
    public static final String BL_ACTIVITY_LINES = "bl_me_activity_lines";
    public static final String BL_OCC_ORDER_NO = "bl_occ_order_no";
    public static final String BL_OCC_INT_ORDER_NO = "bl_occ_int_order_no";
    public static final String BL_OCC_MVL_CONDITION = "bl_occ_mvl_condition";
    public static final String BL_VARIANT_CONDITION = "bl_variant_condition";
    public static final String BL_QUANTITY = "bl_quantity";
    public static final String BL_SEQUENCE_NO = "bl_sequence_no";
    public static final String BL_UNIT_OF_MEASURES = "bl_uom";
    public static final String BL_OWNING_USER = "bl_rev_owning_user";
    public static final String BL_RELEASE_STATUS = "bl_rev_release_statuses";
    public static final String BL_DATE_RELEASED = "bl_rev_date_released";
    public static final String BL_ABS_OCC_ID = "bl_abs_occ_id";
    public static final String BL_OCCURRENCE_NAME = "bl_occurrence_name";
    public static final String BL_OCC_FND_OBJECT_ID = "bl_occ_fnd0objectId";
    public static final String BL_OCC_ASSIGNED = "bl_occ_assigned";
    public static final String BL_OCC_THREAD_ID = "bl_occ_threadId";
    public static final String BL_OCC_TYPE = "bl_occ_type";
    public static final String BL_ME_APPGROUP = "bl_me_appgroup";
    public static final String BL_ITEM_IMAN_MEVIEW = "bl_item_IMAN_MEView";
    public static final String BL_FORMATTED_TITLE = "bl_formatted_title";
    public static final String BL_OCCGRP_VISIBLE_LINES = "bl_occgrp_visible_lines";
    public static final String BL_ITEM_PUID = "bl_item_fnd0objectId";
    public static final String BL_ITEM_REV_PUID = "bl_rev_fnd0objectId";
//    public static final String BL_CONNECTED_LINES_TAGS = "bl_connected_lines_tags";

    public static final String BL_MFG0ASSIGNED_MATERIAL = "Mfg0assigned_material";
    public static final String BL_MFG0ALL_MATERIAL = "Mfg0all_material";
    public static final String BL_MFG0IMPLEMENTS = "Mfg0implements";

    // public static final String BL_SHOP_SHOP_CODE = "bl_M7_BOPShopRevision_m7_SHOP";
    // public static final String BL_SHOP_PRODUCT_CODE = "bl_M7_BOPShopRevision_m7_PRODUCT_CODE";
    // public static final String BL_SHOP_JPH = "bl_M7_BOPShopRevision_m7_JPH";
    // public static final String BL_REV_SHOP_ALLOWANCE = "bl_M7_BOPShopRevision_m7_ALLOWANCE";
    // public static final String BL_SHOP_ALT_PREFIX = "bl_M7_BOPShopRevision_m7_ALT_PREFIX";

    // public static final String BL_LINE_LINE_CODE = "bl_M7_BOPLineRevision_m7_LINE";
    // public static final String BL_LINE_SHOP_CODE = "bl_M7_BOPLineRevision_m7_SHOP";
    // public static final String BL_LINE_PRODUCT_CODE = "bl_M7_BOPLineRevision_m7_PRODUCT_CODE";
    // public static final String BL_LINE_JPH = "bl_M7_BOPLineRevision_m7_JPH";
    // public static final String BL_REV_LINE_ALLOWANCE = "bl_M7_BOPLineRevision_m7_ALLOWANCE";
    // public static final String BL_LINE_IS_ALTBOP = "bl_M7_BOPStationRevision_m7_IS_ALTBOP";
    // public static final String BL_LINE_ALT_PREFIX = "bl_M7_BOPStationRevision_m7_ALT_PREFIX";

    // public static final String BL_STATION_ENG_NAME = "bl_M7_BOPStationRevision_m7_ENG_NAME";
    // public static final String BL_STATION_LINE_CODE = "bl_M7_BOPStationRevision_m7_LINE";
    // public static final String BL_STATION_STATION_CODE = "bl_M7_BOPStationRevision_m7_STATION_CODE";
    // public static final String BL_STATION_PRODUCT_CODE = "bl_M7_BOPStationRevision_m7_PRODUCT_CODE";
    // public static final String BL_STATION_VEHICLE_CODE = "bl_M7_BOPStationRevision_m7_VEHICLE_CODE";
    // public static final String BL_REV_STATION_CODE = "bl_M7_BOPStationRevision_m7_STATION_CODE";
    // public static final String BL_STATION_SHOP = "bl_M7_BOPStationRevision_m7_SHOP";
    // public static final String BL_STATION_IS_ALTBOP = "bl_M7_BOPStationRevision_m7_IS_ALTBOP";
    // public static final String BL_STATION_ALT_PREFIX = "bl_M7_BOPStationRevision_m7_ALT_PREFIX";

    // public static final String BL_OPERATION_WORKER_CODE = "bl_M7_BOPOperation_m7_WORKER_CODE";
    // public static final String BL_OPERATION_WORKER_COUNT = "bl_M7_BOPPaintOpRevision_m7_WORKER_COUNT";
    // public static final String BL_PRODUCT_CODE = "bl_M7_BOPOperationRevision_m7_PRODUCT_CODE";
    // public static final String BL_OPERATION_OP_CODE = "bl_M7_BOPOperationRevision_m7_OPERATION_CODE";
    // public static final String BL_OPERATION_KOR_NAME = "bl_M7_BOPOperationRevision_m7_KOR_NAME";
    // public static final String BL_OPERATION_ENG_NAME = "bl_M7_BOPOperationRevision_m7_ENG_NAME";
    // public static final String BL_OPERATION_DR = "bl_M7_BOPOperationRevision_m7_DR";
    // public static final String BL_OPERATION_KPC = "bl_M7_BOPOperationRevision_m7_KPC";
    // public static final String BL_OPERATION_INST_DWG_NO = "bl_M7_BOPOperationRevision_m7_INST_DWG_NO";
    // public static final String BL_OPERATION_TARGET_OP = "bl_M7_BOPWeldOPRevision_m7_TARGET_OP";
    // public static final String BL_ASSYOP_ASSY_SYSTEM = "bl_M7_BOPAssyOpRevision_m7_ASSY_SYSTEM";
    // public static final String BL_ASSYOP_STATION_NO = "bl_M7_BOPAssyOpRevision_m7_STATION_NO";
    // public static final String BL_WORK_UBODY = "bl_M7_BOPAssyOp_m7_WORK_UBODY";

    // public static final String BL_EQUIP_ENG_NAME = "bl_M7_Equipment_m7_ENG_NAME";
    // public static final String BL_EQUIP_RESOURCE_CATEGORY = "bl_M7_EquipmentRevision_m7_RESOURCE_CATEGORY";
    // public static final String BL_EQUIP_MAIN_CLASS = "bl_M7_EquipmentRevision_m7_MAIN_CLASS";
    // public static final String BL_EQUIP_SPEC_KOR = "bl_M7_EquipmentRevision_m7_SPEC_KOR";
    // public static final String BL_EQUIP_SPEC_ENG = "bl_M7_EquipmentRevision_m7_SPEC_ENG";
    // public static final String BL_EQUIP_CAPACITY = "bl_M7_EquipmentRevision_m7_CAPACITY";
    // public static final String BL_EQUIP_PURPOSE_KOR = "bl_M7_EquipmentRevision_m7_PURPOSE_KOR";
    // public static final String BL_EQUIP_PURPOSE_ENG = "bl_M7_EquipmentRevision_m7_PURPOSE_ENG";
    // public static final String BL_EQUIP_INSTALL_YEAR = "bl_M7_EquipmentRevision_m7_INSTALL_YEAR";
    // public static final String BL_EQUIP_MAKER = "bl_M7_EquipmentRevision_m7_MAKER";

    // 노트
    public static final String BL_NOTE_TORQUE = "M7_TORQUE";
    public static final String BL_NOTE_TORQUE_VALUE = "M7_TORQUE_VALUE";
    public static final String BL_NOTE_DAYORNIGHT = "M7_DAYORNIGHT";
    public static final String BL_NOTE_SUBSIDIARY_QTY = "M7_SUBSIDIARY_QTY";
    public static final String BL_NOTE_ENDITEM_SEQ_NO = "M7_ENDITEM_SEQ_NO";

    // Activity Properties
    public static final String ACTIVITY_ENG_NAME = "m7_ENG_NAME";
    public static final String ACTIVITY_ROOT_ACTIVITY = "root_activity";
    public static final String ACTIVITY_SYSTEM_CODE = "time_system_code";
    public static final String ACTIVITY_SYSTEM_CATEGORY = "time_system_category";
    public static final String ACTIVITY_TIME_SYSTEM_UNIT_TIME = "time_system_unit_time";
    public static final String ACTIVITY_TIME_SYSTEM_FREQUENCY = "time_system_frequency";
    public static final String ACTIVITY_WORK_TIME = "time";
    public static final String ACTIVITY_SUB_CATEGORY = "m7_WORK_OVERLAP_TYPE";
    public static final String ACTIVITY_WORKER = "m7_WORKERS";
    public static final String ACTIVITY_TOOL_LIST = "tool_list";
    public static final String ACTIVITY_CONTROL_POINT = "m7_CONTROL_POINT";
    public static final String ACTIVITY_CONTROL_BASIS = "m7_CONTROL_BASIS";
    public static final String ACTIVITY_WORK_OVERLAP_TYPE = "m7_WORK_OVERLAP_TYPE";
    public static final String ACTIVITY_OBJECT_NAME = "object_name";
    public static final String ACTIVITY_MECO_NO = "m7_MECO_NO";
    public static final String ACTIVITY_DATE_RELEASED = "date_released";

    // Property Type
    public static final Integer TYPE_STRING = 0x01;
    public static final Integer TYPE_INTEGER = 0x02;
    public static final Integer TYPE_DOUBLE = 0x03;
    public static final Integer TYPE_BOOLEAN = 0x04;
    public static final Integer TYPE_REFERENCE = 0x05;
    public static final Integer TYPE_DATE = 0x06;

    // 공법 Revision Properties
    public static final String BOPOP_REV_M7_ASSY_SYSTEM = "m7_ASSY_SYSTEM";

    public static final String M7_TORQUE_VALUE = "M7_TORQUE_VALUE";

    public static final String M7_ITEM_LOC_UL = "m7_ITEM_LOC_UL";

    // 공구 Properties
    // public static final String BL_TOOL_ENG_NAME = "bl_M7_Tool_m7_ENG_NAME";
    // public static final String BL_TOOL_SPEC_CODE = "bl_M7_ToolRevision_m7_SPEC_CODE";
    // public static final String BL_TOOL_SPEC_KOR = "bl_M7_ToolRevision_m7_SPEC_KOR";
    // public static final String BL_TOOL_SPEC_ENG = "bl_M7_ToolRevision_m7_SPEC_ENG";

    // 결재자 출력(signoff)
    public static final String WORKFLOW_SIGNOFF = "signoff";

    // 부자재
    // public static final String BL_SUB_ENG_NAME = "bl_M7_Subsidiary_m7_ENG_NAME";
    // public static final String BL_SUB_MAIN_CLASS = "bl_M7_SubsidiaryRevision_m7_MAIN_CLASS";
    // public static final String BL_SUB_SPEC_KOR = "bl_M7_SubsidiaryRevision_m7_SPEC_KOR";
    // public static final String BL_SUB_SPEC_ENG = "bl_M7_SubsidiaryRevision_m7_SPEC_ENG";
    // public static final String BL_SUB_BUY_UNIT = "bl_M7_SubsidiaryRevision_m7_BUY_UNIT";
    // public static final String BL_SUB_MAKER = "bl_M7_SubsidiaryRevision_m7_MAKER";
    // public static final String BL_SUB_UNIT_AMOUNT = "bl_M7_SubsidiaryRevision_m7_UNIT_AMOUNT";
    // public static final String BL_SUB_OLDPART = "bl_M7_SubsidiaryRevision_m7_OLDPART";
    // public static final String BL_SUB_SUBSIDIARY_GROUP = "bl_M7_SubsidiaryRevision_m7_SUBSIDIARY_GROUP";
    public static final String SUB_SUBSIDIARY_QTY = "M7_SUBSIDIARY_QTY";

    // MECO
    // public static final String BL_MECO_EFFECT_DATE = "bl_M7_MECORevision_m7_EFFECT_DATE";
    // public static final String BL_OPERATION_MECO_NO = "bl_M7_BOPOperationRevision_m7_MECO_NO";
    public static final String MECO_REV_PROJECT_CODE = "m7_PROJECT";
    public static final String MECO_TYPE = "m7_MECO_TYPE";
    public static final String MECO_ORG_CODE = "m7_ORG_CODE";
    public static final String MECO_ORG_CODE_PC = "PC";
    public static final String MECO_ORG_CODE_PB = "PB";
    public static final String MECO_ORG_CODE_PP = "PP";
    public static final String MECO_ORG_CODE_PA = "PA";
    public static final String MECO_PROJECT = "m7_PROJECT";
    public static final String MECO_EFFECT_DATE = "m7_EFFECT_DATE";
    public static final String MECO_EFFECT_EVENT = "m7_EFFECT_EVENT";
    public static final String MECO_CHANGE_REASON = "m7_CHANGE_REASON";
    public static final String MECO_WORKFLOW_TYPE = "m7_WORKFLOW_TYPE";
    public static final String MECO_TYPED_REFERENCE = "m7_MECO_TypedReference";
    public static final String EFFECTIVITY_ID = "effectivity_id";
    public static final String EFFECTIVITY_STATUS = "date_open_ended_status";
    public static final String EFFECTIVITY_EVENT_PREFIX = "MECO_";
    public static final String EFFECTIVITY_DATES = "effectivity_dates";

    // End Item Properties
    public static final String S7_SUPPLY_MODE = "S7_SUPPLY_MODE";
    public static final String S7_MODULE_CODE = "S7_MODULE_CODE";
    public static final String S7_EST_WEIGT = "s7_EST_WEIGHT";
    public static final String S7_CAL_WEIGT = "s7_CAL_WEIGHT";
    public static final String S7_ACT_WEIGT = "s7_ACT_WEIGHT";
    public static final String S7_THICKNESS = "s7_THICKNESS";
    public static final String S7_MATERIAL = "s7_MATERIAL";
    public static final String S7_MATURITY = "s7_MATURITY";
    public static final String S7_ECO_NO = "s7_ECO_NO";
    public static final String S7_REFERENCE = "s7_REFERENCE";
    public static final String S7_COLOR = "s7_COLOR";
    public static final String S7_COLOR_ID = "s7_COLOR_ID";
    public static final String BL_S7_DISPLAY_NO = "bl_rev_s7_DISPLAY_PART_NO";
    public static final String S7_POSITION_DESC = "S7_POSITION_DESC";
    public static final String S7_SHOWN_NO = "s7_SHOWN_PART_NO";
    public static final String S7_PART_TYPE = "s7_PART_TYPE";
    public static final String S7_SES_CODE = "s7_SES_CODE";
    public static final String S7_PROJECT_CODE = "s7_PROJECT_CODE";
    public static final String S7_VEHICLE_NO = "s7_VEHICLE_NO";
    public static final String S7_KOR_NAME = "s7_KOR_NAME";

    // MProduct vehicle code
    public static final String S7_VEHICLE_CODE = "s7_VEHICLE_CODE";

    // 기술문서
    public static final String M7_DOC_TYPE = "m7_DOC_TYPE";
    public static final String M7_TECH_DOC_TYPE = "m7_TECH_DOC_TYPE";
    public static final String IP_CLASSIFICATION = "ip_classification";

    // Equipment & EquipmentRevision Master Properties
    public static final String EQUIP_ENG_NAME = "m7_ENG_NAME";
    public static final String EQUIP_SHOP_CODE = "m7_SHOP";
    public static final String EQUIP_RESOURCE_CATEGORY = "m7_RESOURCE_CATEGORY";
    public static final String EQUIP_MAIN_CLASS = "m7_MAIN_CLASS";
    public static final String EQUIP_SUB_CLASS = "m7_SUB_CLASS";
    public static final String EQUIP_SPEC_KOR = "m7_SPEC_KOR";
    public static final String EQUIP_SPEC_ENG = "m7_SPEC_ENG";
    public static final String EQUIP_CAPACITY = "m7_CAPACITY";
    public static final String EQUIP_MAKER = "m7_MAKER";
    public static final String EQUIP_NATION = "m7_NATION";
    public static final String EQUIP_INSTALL_YEAR = "m7_INSTALL_YEAR";
    public static final String EQUIP_PURPOSE_KOR = "m7_PURPOSE_KOR";
    public static final String EQUIP_PURPOSE_ENG = "m7_PURPOSE_ENG";
    public static final String EQUIP_REV_DESC = "m7_REV_DESC";

    public static final String EQUIP_VEHICLE_CODE = "m7_VEHICLE_CODE";
    public static final String EQUIP_STATION_CODE = "m7_STATION_CODE";
    public static final String EQUIP_POSITION_CODE = "m7_POSITION_CODE";
    public static final String EQUIP_LINE_CODE = "m7_LINE";

    public static final String EQUIP_AXIS = "m7_AXIS";
    public static final String EQUIP_SERVO = "m7_SERVO";
    public static final String EQUIP_ROBOT_TYPE = "m7_ROBOT_TYPE";
    public static final String EQUIP_MAKER_NO = "m7_MAKER_NO";

    // Tool & ToolRevision Master Properties
    public static final String TOOL_ENG_NAME = "m7_ENG_NAME";
    public static final String TOOL_RESOURCE_CATEGORY = "m7_RESOURCE_CATEGORY";
    public static final String TOOL_MAIN_CLASS = "m7_MAIN_CLASS";
    public static final String TOOL_SUB_CLASS = "m7_SUB_CLASS";
    public static final String TOOL_PURPOSE = "m7_PURPOSE_KOR";
    public static final String TOOL_SPEC_CODE = "m7_SPEC_CODE";
    public static final String TOOL_SPEC_KOR = "m7_SPEC_KOR";
    public static final String TOOL_SPEC_ENG = "m7_SPEC_ENG";
    public static final String TOOL_TORQUE_VALUE = "m7_TORQUE_VALUE";
    public static final String TOOL_UNIT_USAGE = "m7_UNIT_USAGE";
    public static final String TOOL_MATERIAL = "m7_MATERIAL";
    public static final String TOOL_MAKER = "m7_MAKER";
    public static final String TOOL_MAKER_AF_CODE = "m7_MAKER_AF_CODE";
    public static final String TOOL_TOOL_SHAPE = "m7_TOOL_SHAPE";
    public static final String TOOL_TOOL_LENGTH = "m7_TOOL_LENGTH";
    public static final String TOOL_TOOL_SIZE = "m7_TOOL_SIZE";
    public static final String TOOL_TOOL_MAGNET = "m7_TOOL_MAGNET";
    public static final String TOOL_VEHICLE_CODE = "m7_VEHICLE_CODE";
    public static final String TOOL_STAY_TYPE = "m7_STAY_TYPE";
    public static final String TOOL_STAY_AREA = "m7_STAY_AREA";

    // Subsidiary & SubsidiaryRevision Master Properties
    public static final String SUBSIDIARY_ENG_NAME = "m7_ENG_NAME";
    public static final String SUBSIDIARY_MATERIAL_TYPE = "m7_MATERIAL_TYPE";
    public static final String SUBSIDIARY_SUBSIDIARY_GROUP = "m7_SUBSIDIARY_GROUP";
    public static final String SUBSIDIARY_PARTQUAL = "m7_PARTQUAL";
    public static final String SUBSIDIARY_SPEC_KOR = "m7_SPEC_KOR";
    public static final String SUBSIDIARY_SPEC_ENG = "m7_SPEC_ENG";
    public static final String SUBSIDIARY_OLDPART = "m7_OLDPART";
    public static final String SUBSIDIARY_UNIT_AMOUNT = "m7_UNIT_AMOUNT";
    public static final String SUBSIDIARY_BUY_UNIT = "m7_BUY_UNIT";
    public static final String SUBSIDIARY_REMARK = "m7_REMARK";
    public static final String SUBSIDIARY_MAKER = "m7_MAKER";

    // WeldPoint SpotMasterForm Properties
    public static final String SPOT_MASTER_FORM_NUMBER_OF_SHEETS = "Number_of_sheets_welded";

    // WeldPoint Note Properties
    public static final String BL_WELD_NOTE_LINE = "M7_LINE";
    public static final String BL_WELD_NOTE_PRESSURIZATION = "M7_PRESSURIZATION";
    public static final String BL_WELD_NOTE_ETC = "M7_ETC";

    // WeldPoint BOMLine Properties
//    public static final String BL_CONNECTED_LINES = "bl_connected_lines";
    public static final String BL_CONNECTED_PARTS = "M7_CONNECTED_PARTS";
    public static final String BOM_STRUCTURE_REVISIONS = "structure_revisions";
    public static final String WELD_NUMBER_OF_SHEETS = "Mfg0number_of_sheets_welded";

    // Workflow
    public static final String EPM_TASK_TYPE = "EPMTask";
    public static final String EPM_REVIEW_TASK_TYPE = "EPMReviewTask";
    public static final String EPM_SELECT_SIGNOFF_TASK_TYPE = "EPMSelectSignoffTask";
    public static final String EPM_PERFORM_SIGNOFF_TASK_TYPE = "EPMPerformSignoffTask";
    public static final String EPM_DO_TASK_TYPE = "EPMDoTask";
    public static final String EPM_ACKNOWLEDGE_TASK_TYPE = "EPMAcknowledgeTask";
    public static final String EPM_CONDITION_TASK_TYPE = "EPMConditionTask";
    public static final String EPM_NOTIFY_TASK_TYPE = "EPMNotifyTask";
    public static final String EPM_ROUTE_TASK_TYPE = "EPMRouteTask";
    public static final String WF_TASK_REFERENCES = "References";

    public static final String PROP_RELEASE_STATUSES = "release_statuses";
    public static final String PROP_LAST_RELEASE_STATUS = "last_release_status";
    public static final String PROP_PROCESS_STAGE = "process_stage";
    public static final String PROP_PROCESS_STAGE_LIST = "process_stage_list";
    public static final String PROP_ROOT_TASK = "root_task";
    public static final String PROP_PROCESS_ATTACHEMENTS = "attachments";
    public static final String PROP_PROCESS_PARENT = "parent_process";

    // Std.WorkMethod Properties
    public static final String SWM_CATEGORY = "m7_CATEGORY";
    public static final String SWM_VEHICLE_CODE = "m7_VEHICLE_CODE";
    public static final String SWM_SHOP_CODE = "m7_SHOP_CODE";
    public static final String SWM_GROUP = "m7_GROUP";

    // Process Sheet Properties
    public static final String PS_REV_LAST_PUB_USER = "m7_LAST_PUBLISH_USER";
    public static final String PS_REV_LAST_PUB_DATE = "m7_LAST_PUBLISH_DATE";

    // Plant Revision Properties
    public static final String PLANT_REV_IS_ALTBOP = "m7_IS_ALTPLANT";
    public static final String PLANT_REV_ALT_PREFIX = "m7_ALT_PREFIX";

}
