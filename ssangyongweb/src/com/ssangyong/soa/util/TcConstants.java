package com.ssangyong.soa.util;

/**
 *
 * Desc :
 * @author yunjae.jung
 */
public class TcConstants {
    // ================================================
    // Configuration 레지스트리 & 설정
    // ================================================
    /** client_specific */
    public static final String REG_CLIENT_SPECIFIC = "client_specific";
    /** TcFile Export 경로 설정 */
    public static final String ENV_TC_EXPORT_DIR = "TCExportDir";
    public static final String ENV_TC_CACHE_DIR = "cache";
    public static final String ENV_FILE_PATH ="File_Path";
    public static final String ENV_FILE_NAME ="File_Name";


    // ================================================
    // ITEM TYPE
    // ================================================
    /** ITEM TYPE */
    public static final String TYPE_ITEM = "Item";  //deprecated
    /** ITEM TYPE FOR SYMC*/
    public static final String TYPE_SYMC_ITEM_VEH ="S7_Vehpart";

    // ================================================
    // ITEM REVISION TYPE
    // ================================================
    /** ITEM REVISION TYPE */
    public static final String TYPE_ITEM_REVISION = "ItemRevision"; //deprecated
    /** ITEM REVISION TYPE FOR SYMC*/
    public static final String TYPE_SYMC_ITEM_REVISION_VEH ="S7_VehpartRevision";


    // ================================================
    // MASTER TYPE 유형
    // ================================================
    /** ITEM REVISION MASTER TYPE */
    public static final String TYPE_ITEM_MASTER = "ItemMaster"; //deprecated
    /** ITEM REVISION MASTER TYPE */
    public static final String TYPE_ITEM_REVISION_MASTER = "ItemRevisionMaster"; //deprecated
    /** ITEM MASTER TYPE  FOR LGE*/
    public static final String  TYPE_LGE_ITEM_MASTER = "G2_LGEItemMaster";
    /** ITEM REVISION MASTER TYPE FOR LGE*/
    public static final String TYPE_LGE_ITEM_REVISION_MASTER = "G2_LGEItemRevisionMaster";


    // ================================================
    // FORM TYPE
    // ================================================
    /** FORM TYPE */
    public static final String TYPE_FORM = "Form";
    /** URL TYPE */
    public static final String TYPE_URL = "Web Link";

    // ================================================
    // PORTAL WEB BROWSER TYPE
    // ================================================
    /** PORTAL WEB BROWSER TYPE */
    public static final String TYPE_PORTAL_WEB_BROWSER = "PortalWebBrowser";

    // ================================================
    // DATASET TYPE
    // ================================================
    /** DATASET TYPE */
    public static final String TYPE_DATASET = "Dataset";
    /** PDF */
    public static final String TYPE_DATASET_PDF = "PDF";
    /** MS Excel */
    public static final String TYPE_DATASET_EXCEL = "MSExcel";
    /** MS PowerPoint */
    public static final String TYPE_DATASET_POWERPOINT = "MSPowerPoint";
    /** MS Word */
    public static final String TYPE_DATASET_WORD = "MSWord";
    /** MS Excel */
    public static final String TYPE_DATASET_EXCEL_2007 = "MSExcelX";
    /** MS PowerPoint */
    public static final String TYPE_DATASET_POWERPOINT_2007 = "MSPowerPointX";
    /** MS Word */
    public static final String TYPE_DATASET_WORD_2007 = "MSWordX";
    /** 텍스트 */
    public static final String TYPE_DATASET_TEXT = "Text";
    /** 이미지 */
    public static final String TYPE_DATASET_IMAGE = "Image";
    /** JT */
    public static final String TYPE_DATASET_JT = "DirectModel";
    /** UG Master*/
    public static final String TYPE_DATASET_UGMASTER = "UGMASTER";
    /** UG Part*/
    public static final String TYPE_DATASET_UGPART ="UGPART";

    /** CATDrawing */
    public static final String TYPE_DATASET_CATDRAWING ="CATDrawing";
    /** CATPart */
    public static final String TYPE_DATASET_CATPART ="CATPart";
    /** CATProduct */
    public static final String TYPE_DATASET_CATPRODUCT ="CATProduct";
    /** CATIA / Model */
    public static final String TYPE_DATASET_CATIA ="catia";

    // ================================================
    // FOLDER TYPE
    // ================================================
    /** 일반 FOLDER TYPE */
    public static final String TYPE_FOLDER = "Folder";
    /** 메일 FOLDER TYPE */
    public static final String TYPE_FOLDER_MAIL = "Mail Folder";
    /** Newstuff FOLDER TYPE */
    public static final String TYPE_FOLDER_NEWSTUFF = "Newstuff Folder";
    /** DIVISION FOLDER */
    public static final String TYPE_G2_DIVISION_FOLDER ="Division Library Folder";
    /** PUBLIC FOLDER*/
    public static final String TYPE_G2_PUBLIC_FOLDER ="Public Library Folder";
    /** SITE FOLDER*/
    public static final String TYPE_G2_SITE_FOLDER = "Site Library Folder";

    // ================================================
    // WORKFLOW TYPE
    // ================================================
    /** EPM Task Template TYPE */
    public static final String TYPE_EPM_TASK_TEMPLATE = "EPMTaskTemplate";
    /** TCM Release Process TYPE */
    public static final String TYPE_TCM_RELEASE_PROCESS = "TCM Release Process";
    /** EPM Job (TC Process) TYPE */
    public static final String TYPE_EPM_JOB = "Job";

    public static final String TYPE_EPM_DoTask ="EPMDoTask";

    // ================================================
    // VIEW TYPE
    // ================================================
    public static final String TYPE_BOM_VIEW ="BOMView Revision";

    // ================================================
    // 명명된 참조 TYPE
    // ================================================
    /** Image 파일의 명명된 참조 TYPE */
    public static final String TYPE_NR_IMAGE = "Image";
    /** PDF 파일의 명명된 참조 TYPE */
    public static final String TYPE_NR_PDF = "PDF_Reference";
    /** UGPart 의 명명된 참조 TYPE*/
    public static final String TYPE_NR_UGPART = "UGPART";
    /** UGMaster 의 명명된 참조 TYPE*/
    public static final String TYPE_NR_UGMASTER = "UGPART";
    /** EXCEL 파일의 명명된 참조 TYPE */
    public static final String TYPE_NR_EXCEL = "xls";
    /** EXCEL 파일의 명명된 참조 TYPE */
    public static final String TYPE_NR_EXCEL_2007 = "excel";
    /** WORD 파일의 명명된 참조 TYPE */
    public static final String TYPE_NR_WORD = "doc";
    /** POWERPOINT 파일의 명명된 참조 TYPE */
    public static final String TYPE_NR_POWERPOINT = "ppt";
    /** JT 파일의 명명된 참조 TYPE */
    public static final String TYPE_NR_JT = "JTPART";

    /** CATIA **/
    public static final String TYPE_NR_CATDRAWING = "catdrawing";
    public static final String TYPE_NR_CATPART = "catpart";
    public static final String TYPE_NR_CATPRODUCT = "catproduct";
    public static final String TYPE_NR_CATIA = "catia";
    // ================================================
    // 기타 TYPE
    // ================================================
    /** 사용자 타입 */
    public static final String TYPE_USER = "User";
    /** 그룹 타입 */
    public static final String TYPE_GROUP = "Group";
    /** 그룹 멤버(구성원) 타입 */
    public static final String TYPE_GROUP_MEMBER = "GroupMember";
    /** 사용자 InBox의 TaskInbox 타입 */
    public static final String TYPE_TASK_INBOX = "TaskInbox";
    /** 사용자 InBox의 Tasks to Perform 타입 */
    public static final String TYPE_TASKS_TO_PERFORM = "TasksToPerform";
    /** 사용자 InBox의 Tasks to Track 타입 */
    public static final String TYPE_TASKS_TO_TRACK = "TasksToTrack";
    /** TC_Project 타입 */
    public static final String TYPE_PROJECT = "TC_Project";
    /** ImanQuery 타입 */
    public static final String TYPE_IMAN_QUERY = "ImanQuery";

    public static final String TYPE_IMAN_FILE = "ImanFile";

    public static final String TYPE_VIEW_DEFAULT ="view";
    public static final String TYPE_VIEW_TOTALBOM = "TOTAL_BOM";
    public static final String TYPE_VIEW_EBOM = "E-BOM";




    // ================================================
    // RELATION
    // ================================================
    /** ITEM MASTER FORM 관계 */
    public static final String RELATION_ITEM_MASTER_FORM = "IMAN_master_form";
    /** ITEM REVISION MASTER FORM 관계 */
    public static final String RELATION_ITEM_REVISION_MASTER_FORM = "IMAN_master_form_rev";
    /** 사양 관계 */
    public static final String RELATION_SPECIFICATION = "IMAN_specification";
    /** ITEM REVISION 관계 */
    public static final String RELATION_REVISION_LIST = "revision_list";
    /** 내용 관계 */
//    public static final String RELATION_CONTENTS = "contents";
    /** 참조 관계 */
    public static final String RELATION_REFERENCES = "IMAN_reference";
    /** Rendering (JT) 관계 */
    public static final String RELATION_RENDERING = "IMAM_Rendering";
    /** UGMASTER 관계 (Quick Access Binary) */
    public static final String RELATION_QUICK_ACCESS_BINARY_REFERENCES = "UG-QuickAccess-Binary";
    /** Part Family 관계 IMAN_UG_part_family_link*/
    public static final String RELATION_UG_PART_FAMILY_LINK = "IMAN_UG_part_family_link";
    /** BOM View Revision */
    public static final String RELATION_BOM_VIEW_REVISION = "BOMView Revision";

    public static final String RELATION_TC_NX_NONPS_OCCURENCE = "TC_NX_nonps_occurrence";

    /** MECO와 Publish 된 작업표준서 관계 */
    public static final String PROCESS_SHEET_KO_RELATION = "M7_PROCESS_SHEET_KO_REL";

    /** MECO와 용접공법 관계 */
    public static final String WELD_CONDITIOIN_SHEET_RELATION = "M7_WELD_CONDITIOIN_SHEET_REL";

    // ================================================
    // ATTRIBUTE - TCCOMPONENT 기본
    // ================================================
    /** PUID */
    public static final String PROP_PUID = "puid";
    /** 객체 이름 */
    public static final String PROP_OBJECT_NAME = "object_name";
    /** 객체 설명 */
    public static final String PROP_OBJECT_DESC = "object_desc";
    /** 객체 타입 */
    public static final String PROP_OBJECT_TYPE = "object_type";
    /** 소유자 */
    public static final String PROP_OWNING_USER = "owning_user"; // IMANComponentUser
    /** 소유자 그룹 */
    public static final String PROP_OWNING_GROUP = "owning_group"; // IMANComponentGroup
    /** 최종 수정자 */
    public static final String PROP_LAST_MOD_USER = "last_mod_user"; // IMANComponentUser
    /** 생성 일자 */
    public static final String PROP_CREATION_DATE = "creation_date";
    /** 최종 수정 일자 */
    public static final String PROP_LAST_MOD_DATE = "last_mod_date";
    /** 체크 아웃 */
    public static final String PROP_CHECKED_OUT = "checked_out";
    /** 체크 아웃 변경 ID */
    public static final String PROP_CHECKED_OUT_CHANGE_ID = "checked_out_change_id";
    /** 체크 아웃 사용자 */
    public static final String PROP_CHECKED_OUT_USER = "checked_out_user";
    /** 체크 아웃 일자 */
    public static final String PROP_CHECKED_OUT_DATE = "checked_out_date";
    /** 프로젝트 ID */
    public static final String PROP_PROJECT_ID = "project_id";
    /** REFERENCE_LIST */
    public static final String PROP_REF_LIST = "ref_list";
    /** REFERENCE_NAME */
    public static final String PROP_REF_NAMES = "ref_names";

    public static final String PROP_NAME ="name";

    public static final String TC_TYPE = "Type";


    // ================================================
    // ATTRIBUTE - ITEM, ITEM REVISION
    // ================================================
    /** ITEM ID */
    public static final String PROP_ITEM_ID = "item_id";
    /** ITEM REVISION ID */
    public static final String PROP_ITEM_REVISION_ID = "item_revision_id";
    /** ITEM NAME */
    public static final String PROP_ITEM_NAME ="item_name";
    /** ITEM DESCRIPTION */
    public static final String PROP_ITEM_DESC = "item_desc";
    /** ITEM CURRENT_REVISION_ID */
    public static final String PROP_CURRENT_REVISION_ID = "current_revision_id";
    /** 측정 단위 */
    public static final String PROP_UOM_TAG = "uom_tag";
    /** 승인여부 상태 */
    public static final String PROP_RELEASE_STATUSES = "release_statuses";
    /** 최종 승인여부 상태 */
    public static final String PROP_LAST_RELEASE_STATUS = "last_release_status";
    /** 결재 진행 상태 */
    public static final String PROP_PROCESS_STAGE = "process_stage";
    /** 결재 진행 이력 리스트 */
    // [20240404][UPGRADE] TC12.2 이후 process_stage_list 는 Root Task 만 표시하도록 되어 있어 fnd0StartedWorkflowTasks 로 교체 
    public static final String PROP_PROCESS_STAGE_LIST = "process_stage_list";
    public static final String PROP_STARTED_WORKFLOW_TASKS = "fnd0StartedWorkflowTasks";

    public static final String PROP_ROOT_TASK ="root_task";

    public static final String PROP_PROCESS_ATTACHEMENTS = "attachments";

    public static final String PROP_PROCESS_PARENT ="parent_process";
    /** Sequence ID */
    public static final String PROP_SEQUENCE_ID = "sequence_id";
    /** Structure Level*/
    public static final String PROP_BOM_LEVEL ="level";
    /** Structure Revision*/
    public static final String PROP_STRUCTURE_REVISIONS ="structure_revisions";

    public static final String PROP_RELEASE_STATUS_LIST ="release_status_list";

    public static final String PROP_ITEMS_TAG ="items_tag";

    public static final String PROP_ITEM_MASTER_TAG = "item_master_tag";

    public static final String PROP_G2_REVRELEASESTATUS = "g2_RevReleaseStatus";

    public static final String PROP_G2_DRAWINGNO = "g2_DrawingNo";

    public static final String PROP_G2_LGE_MODEL_NAME = "g2_LGE_MODEL_NAME";

    public static final String PROP_G2_LGE_PROJECT_CODE = "g2_ProjectCode";

    public static final String PROP_ITEM_IMAN_MASTER_FORM  ="IMAN_master_form";


    // ================================================
    // ATTRIBUTE - BOM VIEW
    // ================================================
    public static final String PROP_BOMVIEW_TYPE_NAME ="viewTypeName";
    // ================================================
    // ATTRIBUTE - FOLDER
    // ================================================

    /** FOLDER CONTENTS*/
    public static final String PROP_FOLDER_CONTENTS ="contents";
    /** FOLDER CURRENT NAME*/
    public static final String PROP_FOLDER_CURRENT_NAME ="current_name";
    /** FOLDER OBJECT DESC */
    public static final String PROP_FOLDER_OBJECT_DESC ="object_desc";
    /** FOLDER OBJECT NAME */
    public static final String PROP_FOLDER_OBJECT_NAME ="object_name";

    // ================================================
    // ATTRIBUTE - VOLUME
    // ================================================
    /** Volumne의 NodeName */
    public static final String PROP_VOLUME_NODE_NAME = "node_name";

    // ================================================
    // ATTRIBUTE - USER
    // ================================================
    /** 사용자 이름 */
    public static final String PROP_USER_NAME = "user_name";
    /** 기본 그룹 */
    public static final String PROP_DEFAULT_GROUP = "default_group";
    /** 조직명 */
    public static final String PROP_DEPT_NAME = "PA6";

    public static final String PROP_USER_ID = "user_id";

    // ================================================
    // ATTRIBUTE - Site
    // ================================================
    public static final String PROP_SITE_CONNECT_STRING = "connect_string";

    public static final String PROP_SITE_DBMS = "dbms";

    public static final String PROP_SITE_GEOGRAPHY = "geography";

    public static final String PROP_SITE_GMS_URL = "gms_url";

    public static final String PROP_SITE_HTTP_SITE_NODE = "http_site_node";

    public static final String PROP_SITE_IMC_NODE_NAME = "imc_node_name";

    public static final String PROP_SITE_IMC_ODS_SITE = "imc_ods_site";

    public static final String PROP_SITE_NAME = "name";

    public static final String PROP_SITE_PASSWORD ="password";

    public static final String PROP_SITE_SITE_ID ="site_id";

    public static final String PROP_SITE_USER_ID ="user_id";

    // ================================================
    // ATTRIBUTE - 검색 기본
    // ================================================
    /** 등록일 이후 */
    public static final String PROP_CREATED_AFTER = "CreatedAfter";
    /** 등록일 이전 */
    public static final String PROP_CREATED_BEFORE = "CreatedBefore";

    // ================================================
    // ATTRIBUTE - TCComponentDataset
    // ================================================
    /** Dataset Version */
    public static final String PROP_DATASET_VERSION = "revision_number";
    /** Dataset Lastest Version */
    public static final String PROP_DATASET_LASTEST_VERSION = "highest_rev_prop";

    public static final String PROP_DATASET_TYPE ="dataset_type";

    public static final String PROP_DATASET_TYPE_NAME ="datasettype_name";

    public static final String PROP_DATASET_REV = "rev";

    public static final String PROP_DATASET_ID = "id_prop";

    // ================================================
    // ATTRIBUTE - TCComponentTcFile
    // ================================================
    /** TcFile 클래스 이름 */
    public static final String CLASS_TCFILE = "ImanFile";
    /** 파일 이름 */
    public static final String PROP_TCFILE_NAME = "original_file_name";
    /** 파일 이름 */
    public static final String PROP_ORIGINAL_FILE_NAME = "original_file_name";
    /** Volume 이름 */
    public static final String PROP_TCFILE_VOLUME = "volume_tag";
    /** 파일 크리 */
    public static final String PROP_TCFILE_SIZE = "file_size";

    public static final String PROP_TCFILE_BYTESIZE = "byte_size";

    // ================================================
    // ATTRIBUTE - URL
    // ================================================
    /** URL */
    public static final String PROP_URL = "url";

    //===============================================
    // TC Saved Query Name
    //===============================================
    /** General */
    public static final String SEARCH_GENERAL = "General...";
    /** Item... */
    public static final String SEARCH_ITEM = "Item...";
    /** Projects... */
    public static final String SEARCH_PROJECTS = "Projects...";
    /** */
    public static final String SEARCH_DATASET = "Dataset...";

    public static final String SEARCH_SITE = "__FIND_SITE";

    public static final String SEARCH_PERSON = "__WEB_find_person";

    public static final String SEARCH_USER = "__WEB_find_user";


    // ===================================================================
    // SEARCH ATTRIBUTE
    // ===================================================================
    /** Dataset의 속성 */
    public static final String SEARCH_PROP_DATASET_TYPE = "DatasetType";

    // ===================================================================
    // USER
    // ===================================================================
    public static final String USER_INFODBA = "infodba";

    // ===================================================================
    // GROUP
    // ===================================================================
    public static final String GROUP_DBA = "dba";
    public static final String PROP_GROUP_DISPLAY_NAME = "display_name";



    public static final String REVISION_ID_DEFAULT = "0";

    //===============================================
    // SUFFIX (UGMASTER)
    //===============================================
    public static final String SUFFIX_UGMASTER_ID = "-3D";
    public static final String SUFFIX_UGPART_ID = "-2D";
    public static final String SUFFIX_PDF_ID ="-2V";

    //===============================================
    // Application/Tool
    //===============================================
    public static final String TOOL_NX_UGMASTER = "UGII V10-BASE";

    //===============================================
    // Returen Message Attribute
    //===============================================
    public static final String TC_RETURN_MESSAGE = "ReturnMsg";
    public static final String TC_RETURN_OK = "OK";
    public static final String TC_RETURN_FAIL    = "FAIL";
    public static final String TC_RETURN_FAIL_REASON = "FAIL_REASON";
    public static final String TC_RETURN_STOP_ROW = "STOP_ROW";
    //===============================================
    // Process
    //===============================================
    /** Defined value for empty process */
    public static final String TC_PROCESS_IN_WORK   = "In Work";
    public static final String TC_PROCESS_INPROCESS = "In Process";
    public static final String TC_PROCESS_RELEASED  = "Released";


    public static final String LG_DEFAULT_WORKFLOW ="GPDM_WORKFLOW";
    public static final String LG_QUICK_RELEASE_TITLE = "Quick-Release";
    public static final String LG_GENERAL_RELEASE_TITLE ="General-Release";

    //===============================================
    // PUBLIC FOLDER PUID
    //===============================================


    //===============================================
    // CWPLM DB DATA SOURCE NAME
    //===============================================
    public static final String CWPLMDB ="CWPLMDB";


    //===============================================
    // TC Preference Definitions
    //===============================================
    public static final String TC_PREF_SCOPE_SITE = "site";
    public static final String TC_PREF_CONTEXT_NAME = "contextName";
    public static final String TC_PREF_CONTEXT_VALUE = "value";

    public static final String TC_PREF_CATG_MT_FILE_CACHE = "Maintenance.File Caching";
    public static final String TC_PREF_NAME_FMS_BOOTSTRP_URL = "Fms_BootStrap_Urls";
    public static final String TC_PREF_CATG_GENERAL = "General";
    public static final String TC_PREF_NAME_TELNET_CONNECTION_INFORMATION = "LG_Telnet_Connection_Info";
    public static final String TC_SESSION = "tcSession";

    //===============================================
    // TC Approvla history on ItemRevision
    //===============================================
    public static final String TC_LGE_SUBMIT_STEP_0 ="g2_LGE_SUBMIT_STEP_0";
    public static final String TC_LGE_SUBMIT_STEP_0_DATE ="g2_LGE_SUBMIT_STEP_0_DATE";
    public static final String TC_LGE_SUBMIT_STEP_1 ="g2_LGE_SUBMIT_STEP_1";
    public static final String TC_LGE_SUBMIT_STEP_1_DATE ="g2_LGE_SUBMIT_STEP_1_DATE";
    public static final String TC_LGE_SUBMIT_STEP_2 ="g2_LGE_SUBMIT_STEP_2";
    public static final String TC_LGE_SUBMIT_STEP_2_DATE ="g2_LGE_SUBMIT_STEP_2_DATE";
    public static final String TC_LGE_SUBMIT_STEP_3 ="g2_LGE_SUBMIT_STEP_3";
    public static final String TC_LGE_SUBMIT_STEP_3_DATE ="g2_LGE_SUBMIT_STEP_3_DATE";
    public static final String TC_LGE_SUBMIT_STEP_4 ="g2_LGE_SUBMIT_STEP_4";
    public static final String TC_LGE_SUBMIT_STEP_4_DATE ="g2_LGE_SUBMIT_STEP_4_DATE";

    //===============================================
    // ATTRIBUTE - MECO Revision
    //===============================================
    public static final String MECO_TYPE = "m7_MECO_TYPE";

    //===============================================
    // ATTRIBUTE - Task
    //===============================================
    public static final String PROP_PROCESS_CHILD_TASKS = "child_tasks";
    public static final String PROP_PROCESS_VALID_SIGNOFFS = "valid_signoffs";

    //===============================================
    // ATTRIBUTE - GroupMember
    //===============================================
    public static final String PROP_USER = "user";
}
