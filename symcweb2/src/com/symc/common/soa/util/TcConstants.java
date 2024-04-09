package com.symc.common.soa.util;

/**
 *
 * Desc :
 * @author yunjae.jung
 * [SR170703-020][LJG]Proto Tooling 컬럼 추가
 * [20180213][ljg] 시스템 코드 리비전 정보에서 bomline정보로 이동
 */
public class TcConstants {
    // ================================================
    // Configuration �덉��ㅽ듃由�& �ㅼ젙
    // ================================================
    /** client_specific */
    public static final String REG_CLIENT_SPECIFIC = "client_specific";
    /** TcFile Export 寃쎈줈 �ㅼ젙 */
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
    // MASTER TYPE �좏삎
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
    /** �띿뒪��*/
    public static final String TYPE_DATASET_TEXT = "Text";
    /** �대�吏�*/
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
    /** �쇰컲 FOLDER TYPE */
    public static final String TYPE_FOLDER = "Folder";
    /** 硫붿씪 FOLDER TYPE */
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
    // 紐낅챸��李몄“ TYPE
    // ================================================
    /** Image �뚯씪��紐낅챸��李몄“ TYPE */
    public static final String TYPE_NR_IMAGE = "Image";
    /** PDF �뚯씪��紐낅챸��李몄“ TYPE */
    public static final String TYPE_NR_PDF = "PDF_Reference";
    /** UGPart ��紐낅챸��李몄“ TYPE*/
    public static final String TYPE_NR_UGPART = "UGPART";
    /** UGMaster ��紐낅챸��李몄“ TYPE*/
    public static final String TYPE_NR_UGMASTER = "UGPART";
    /** EXCEL �뚯씪��紐낅챸��李몄“ TYPE */
    public static final String TYPE_NR_EXCEL = "xls";
    /** EXCEL �뚯씪��紐낅챸��李몄“ TYPE */
    public static final String TYPE_NR_EXCEL_2007 = "excel";
    /** WORD �뚯씪��紐낅챸��李몄“ TYPE */
    public static final String TYPE_NR_WORD = "doc";
    /** POWERPOINT �뚯씪��紐낅챸��李몄“ TYPE */
    public static final String TYPE_NR_POWERPOINT = "ppt";
    /** JT �뚯씪��紐낅챸��李몄“ TYPE */
    public static final String TYPE_NR_JT = "JTPART";

    /** CATIA **/
    public static final String TYPE_NR_CATDRAWING = "catdrawing";
    public static final String TYPE_NR_CATPART = "catpart";
    public static final String TYPE_NR_CATPRODUCT = "catproduct";
    public static final String TYPE_NR_CATIA = "catia";
    // ================================================
    // 湲고� TYPE
    // ================================================
    /** �ъ슜����엯 */
    public static final String TYPE_USER = "User";
    /** 洹몃９ ��엯 */
    public static final String TYPE_GROUP = "Group";
    /** 洹몃９ 硫ㅻ쾭(援ъ꽦�� ��엯 */
    public static final String TYPE_GROUP_MEMBER = "GroupMember";
    /** �ъ슜��InBox��TaskInbox ��엯 */
    public static final String TYPE_TASK_INBOX = "TaskInbox";
    /** �ъ슜��InBox��Tasks to Perform ��엯 */
    public static final String TYPE_TASKS_TO_PERFORM = "TasksToPerform";
    /** �ъ슜��InBox��Tasks to Track ��엯 */
    public static final String TYPE_TASKS_TO_TRACK = "TasksToTrack";
    /** TC_Project ��엯 */
    public static final String TYPE_PROJECT = "TC_Project";
    /** ImanQuery ��엯 */
    public static final String TYPE_IMAN_QUERY = "ImanQuery";

    public static final String TYPE_IMAN_FILE = "ImanFile";

    public static final String TYPE_VIEW_DEFAULT ="view";
    public static final String TYPE_VIEW_TOTALBOM = "TOTAL_BOM";
    public static final String TYPE_VIEW_EBOM = "E-BOM";




    // ================================================
    // RELATION
    // ================================================
    /** ITEM MASTER FORM 愿�퀎 */
    public static final String RELATION_ITEM_MASTER_FORM = "IMAN_master_form";
    /** ITEM REVISION MASTER FORM 愿�퀎 */
    public static final String RELATION_ITEM_REVISION_MASTER_FORM = "IMAN_master_form_rev";
    /** �ъ뼇 愿�퀎 */
    public static final String RELATION_SPECIFICATION = "IMAN_specification";
    /** ITEM REVISION 愿�퀎 */
    public static final String RELATION_REVISION_LIST = "revision_list";
    /** �댁슜 愿�퀎 */
//    public static final String RELATION_CONTENTS = "contents";
    /** 李몄“ 愿�퀎 */
    public static final String RELATION_REFERENCES = "IMAN_reference";
    /** Rendering (JT) 愿�퀎 */
    public static final String RELATION_RENDERING = "IMAM_Rendering";
    /** UGMASTER 愿�퀎 (Quick Access Binary) */
    public static final String RELATION_QUICK_ACCESS_BINARY_REFERENCES = "UG-QuickAccess-Binary";
    /** Part Family 愿�퀎 IMAN_UG_part_family_link*/
    public static final String RELATION_UG_PART_FAMILY_LINK = "IMAN_UG_part_family_link";
    /** BOM View Revision */
    public static final String RELATION_BOM_VIEW_REVISION = "BOMView Revision";

    public static final String RELATION_TC_NX_NONPS_OCCURENCE = "TC_NX_nonps_occurrence";

    /** MECO��Publish ���묒뾽�쒖���愿�퀎 */
    public static final String PROCESS_SHEET_KO_RELATION = "M7_PROCESS_SHEET_KO_REL";

    /** MECO���⑹젒怨듬쾿 愿�퀎 */
    public static final String WELD_CONDITIOIN_SHEET_RELATION = "M7_WELD_CONDITIOIN_SHEET_REL";
    public static final String CMHAS_SOLUTION_ITEM = "CMHasSolutionItem";
    public static final String CMHAS_PROBLEM_ITEM = "CMHasProblemItem";

    // ================================================
    // ATTRIBUTE - TCCOMPONENT 湲곕낯
    // ================================================
    /** PUID */
    public static final String PROP_PUID = "puid";
    /** 媛앹껜 �대쫫 */
    public static final String PROP_OBJECT_NAME = "object_name";
    /** 媛앹껜 �ㅻ챸 */
    public static final String PROP_OBJECT_DESC = "object_desc";
    /** 媛앹껜 ��엯 */
    public static final String PROP_OBJECT_TYPE = "object_type";
    /** �뚯쑀��*/
    public static final String PROP_OWNING_USER = "owning_user"; // IMANComponentUser
    /** �뚯쑀��洹몃９ */
    public static final String PROP_OWNING_GROUP = "owning_group"; // IMANComponentGroup
    /** 理쒖쥌 �섏젙��*/
    public static final String PROP_LAST_MOD_USER = "last_mod_user"; // IMANComponentUser
    /** �앹꽦 �쇱옄 */
    public static final String PROP_CREATION_DATE = "creation_date";
    /** 理쒖쥌 �섏젙 �쇱옄 */
    public static final String PROP_LAST_MOD_DATE = "last_mod_date";
    /** 泥댄겕 �꾩썐 */
    public static final String PROP_CHECKED_OUT = "checked_out";
    /** 泥댄겕 �꾩썐 蹂�꼍 ID */
    public static final String PROP_CHECKED_OUT_CHANGE_ID = "checked_out_change_id";
    /** 泥댄겕 �꾩썐 �ъ슜��*/
    public static final String PROP_CHECKED_OUT_USER = "checked_out_user";
    /** 泥댄겕 �꾩썐 �쇱옄 */
    public static final String PROP_CHECKED_OUT_DATE = "checked_out_date";
    /** �꾨줈�앺듃 ID */
    public static final String PROP_PROJECT_ID = "project_id";
    /** REFERENCE_LIST */
    public static final String PROP_REF_LIST = "ref_list";
    /** REFERENCE_NAME */
    public static final String PROP_REF_NAMES = "ref_names";

    public static final String PROP_NAME ="name";

    public static final String TC_TYPE = "Type";

    public static final String PROP_DATE_RELEASED= "date_released";

    // ================================================
    // ATTRIBUTE - ITEM, ITEM REVISION
    // ================================================
    /** ITEM TYPE */
    public static final String PROP_ITEM_TYPE = "item_type";
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
    /** 痢≪젙 �⑥쐞 */
    public static final String PROP_UOM_TAG = "uom_tag";
    /** �뱀씤�щ� �곹깭 */
    public static final String PROP_RELEASE_STATUSES = "release_statuses";
    /** 理쒖쥌 �뱀씤�щ� �곹깭 */
    public static final String PROP_LAST_RELEASE_STATUS = "last_release_status";
    /** 寃곗옱 吏꾪뻾 �곹깭 */
    public static final String PROP_PROCESS_STAGE = "process_stage";
    /** 寃곗옱 吏꾪뻾 �대젰 由ъ뒪��*/
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

    public static final String PROP_S7_OSPEC_NO = "s7_OSPEC_NO";

    public static final String ATTR_NAME_UNIQUE_ID = "s7_UNIQUE_ID";

    // PreVehPart Revision 속성 추가
    public static final String PROP_S7_CCN_NO = "s7_CCN_NO";
    public static final String PROP_S7_TARGET_WEIGHT = "s7_TGT_WEIGHT";
    public static final String PROP_S7_DR = "s7_REGULATION";
    public static final String PROP_S7_BOX = "s7_RESPONSIBILITY";
    public static final String PROP_S7_CONTENTS = "s7_CONTENTS";
    public static final String PROP_S7_OLD_PART_NO = "s7_OLD_PART_NO";
    public static final String PROP_S7_CHG_TYPE_NM = "s7_CHG_TYPE_NM"; // N, M
    public static final String PROP_S7_EST_COST_MATERIAL = "s7_EST_COST_MATERIAL"; // 추정 재료비
    public static final String PROP_S7_TARGET_COST_MATERIAL = "s7_TARGET_COST_MATERIAL"; // 목표 재료비
    public static final String PROP_S7_SELECTED_COMPANY = "s7_SELECTED_COMPANY"; // 업체명
    public static final String PROP_S7_CON_DWG_PLAN = "s7_CON_DWG_PLAN"; // Concept Dwg - 계획
    public static final String PROP_S7_CON_DWG_PERFORMANCE = "s7_CON_DWG_PERFORMANCE"; // Concept Dwg 실적
    public static final String PROP_S7_CON_DWG_TYPE = "s7_CON_DWG_TYPE"; // Concept Dwg 2D/3D
    public static final String PROP_S7_DWG_DEPLOYABLE_DATE = "s7_DWG_DEPLOYABLE_DATE"; // 도면 배포 예정일
    public static final String PROP_S7_PRD_DWG_PLAN = "s7_PRD_DWG_PLAN"; // 도면 작성(양산) 계획
    public static final String PROP_S7_PRD_DWG_PERFORMANCE = "s7_PRD_DWG_PERFORMANCE"; // 도면 작성(양산) 실적
    public static final String PROP_S7_PREVEH_TYPEDREFERENCE = "s7_PreVeh_TypedReference"; //s7_PreVeh_typedReference
    
    /* [SR없음][20150914][jclee] DVP Sample 속성 BOMLine으로 이동*/
//    public static final String PROP_S7_DVP_NEEDED_QTY = "s7_DVP_NEEDED_QTY"; // DVP SAMPLE 필요수량
//    public static final String PROP_S7_DVP_USE = "s7_DVP_USE"; // DVP SAMPLE 용도
//    public static final String PROP_S7_DVP_REQ_DEPT = "s7_DVP_REQ_DEPT"; // DVP SAMPLE 요청부서
    
    /* [SR없음][20160317][jclee] Design User, Dept 속성 BOMLine으로 이동 */
//    public static final String PROP_S7_ENG_DEPT_NM = "s7_ENG_DEPT_NM"; // 설계담당 팀
//    public static final String PROP_S7_ENG_RESPONSIBLITY = "s7_ENG_RESPONSIBLITY"; // 설계 담당 담당
    
    public static final String PROP_S7_PRT_TOOLG_INVESTMENT = "s7_PRT_TOOLG_INVESTMENT"; // 예상투자비 PRO TOOL'G
    public static final String PROP_S7_PROTO_TOOLG = "s7_PROTO_TOOLG"; // 예상투자비 PRO TOOL'G
    public static final String PROP_S7_PRD_TOOL_COST = "s7_PRD_TOOL_COST"; // 예상투자비 PRO TOOL'G
    public static final String PROP_S7_PRD_SERVICE_COST = "s7_PRD_SERVICE_COST"; // 예상투자비 PRO TOOL'G
    public static final String PROP_S7_PRD_SAMPLE_COST = "s7_PRD_SAMPLE_COST"; // 예상투자비 PRO TOOL'G
    public static final String PROP_S7_PUR_DEPT_NM = "s7_PUR_DEPT_NM"; // 예상투자비 PRO TOOL'G
    public static final String PROP_S7_PUR_RESPONSIBILITY = "s7_PUR_RESPONSIBILITY"; // 예상투자비 PRO TOOL'G
    public static final String PROP_S7_EMPLOYEE_NO = "s7_EMPLOYEE_NO"; // 예상투자비 PRO TOOL'G
    public static final String PROP_S7_ECO_NO = "s7_ECO"; // Key In ECO NO
    //[20180213][ljg] 시스템 코드 리비전 정보에서 bomline정보로 이동
    //public static final String PROP_S7_BUDGETCODE = "s7_BUDGET_CODE";
    public static final String PROP_S7_SYSTEM_CODE = "s7_SYSTEM_CODE";
    public static final String PROP_S7_COLORID = "s7_COLOR";
    public static final String PROP_S7_SELECTIVEPART = "s7_SELECTIVE_PART";
    public static final String PROP_S7_REGULATION = "s7_REGULATION";
    public static final String PROP_S7_ESTWEIGHT = "s7_EST_WEIGHT";
    public static final String PROP_S7_CALWEIGHT = "s7_CAL_WEIGHT";
    public static final String PROP_S7_ACTWEIGHT = "s7_ACT_WEIGHT";
    public static final String PROP_S7_CHANGE_DESCRIPTION = "object_desc";
    public static final String PROP_S7_PROJCODE = "s7_PROJECT_CODE";
    public static final String PROP_S7_DISPLAYPARTNO = "s7_DISPLAY_PART_NO";
    public static final String PROP_S7_PRD_PROJ_CODE = "s7_PRD_PROJECT_CODE"; // Key In Project Code
    public static final String PROP_S7_PROJECTTYPE = "s7_PROJECT_TYPE";
    public static final String PROP_S7_COSTDOWN = "s7_COST_DOWN";
    public static final String PROP_S7_ORDERINGSPEC = "s7_ORDERING_SPEC";
    public static final String PROP_S7_QUALITYIMPROVEMENT = "s7_QUALITY_IMPROVEMENT";
    public static final String PROP_S7_CORRECTIONOFEPL = "s7_CORRECTION_OF_EPL";
    public static final String PROP_S7_STYLINGUPDATE = "s7_STYLING_UPDATE";
    public static final String PROP_S7_WEIGHTCHANGE = "s7_WEIGHT_CHANGE";
    public static final String PROP_S7_MATERIALCOSTCHANGE = "s7_MATERIAL_COST_CHANGE";
    public static final String PROP_S7_THEOTHERS = "s7_THE_OTHERS";
    public static final String PROP_S7_GATENO = "s7_GATE_NO";
    /** 쌍용 Pre Vehicle Part 의 s7_PreVeh_TypedReference 에 붙는 타입 */
    public static final String S7_PREVEHTYPEDREFERENCE = "S7_PreVeh_TypedReference";
    public static final String PROP_PRE_VEH_TYPE_REF = "s7_PreVeh_TypedReference"; 


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
    /** Volumne��NodeName */
    public static final String PROP_VOLUME_NODE_NAME = "node_name";

    // ================================================
    // ATTRIBUTE - USER
    // ================================================
    /** �ъ슜���대쫫 */
    public static final String PROP_USER_NAME = "user_name";
    /** 湲곕낯 洹몃９ */
    public static final String PROP_DEFAULT_GROUP = "default_group";
    /** 議곗쭅紐�*/
    public static final String PROP_DEPT_NAME = "PA6";

    public static final String PROP_TEL = "PA10";

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
    // ATTRIBUTE - 寃�깋 湲곕낯
    // ================================================
    /** �깅줉���댄썑 */
    public static final String PROP_CREATED_AFTER = "CreatedAfter";
    /** �깅줉���댁쟾 */
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
    /** TcFile �대옒���대쫫 */
    public static final String CLASS_TCFILE = "ImanFile";
    /** �뚯씪 �대쫫 */
    public static final String PROP_TCFILE_NAME = "original_file_name";
    /** �뚯씪 �대쫫 */
    public static final String PROP_ORIGINAL_FILE_NAME = "original_file_name";
    /** Volume �대쫫 */
    public static final String PROP_TCFILE_VOLUME = "volume_tag";
    /** �뚯씪 �щ━ */
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
    /** Item Revision... */
    public static final String SEARCH_ITEM_REVISION = "Item Revision...";
    /** Projects... */
    public static final String SEARCH_PROJECTS = "Projects...";
    /** */
    public static final String SEARCH_DATASET = "Dataset...";

    public static final String SEARCH_SITE = "__FIND_SITE";

    public static final String SEARCH_PERSON = "__WEB_find_person";

    public static final String SEARCH_USER = "__WEB_find_user";

    /** Pre Product Revision Type */
    public static final String SEARCH_PREPRODUCTREV = "SYMC_Search_PreProductRevision_Released";
    
    /** Pre Product interface Revision Type */
    public static final String SEARCH_PREPRODUCTREV_INTERFACE = "SYMC_Search_PreProductRevision_Released_for_interface";

    // ==================================================================
    // BOM Line ATTRIBUTE
    // ==================================================================
    public static final String PROP_BL_SEQUENCE_NO = "bl_sequence_no";
    public static final String PROP_BL_SUPPLY_MODE = "S7_SUPPLY_MODE";
    public static final String PROP_BL_MODULE_CODE = "S7_MODULE_CODE";
    public static final String PROP_BL_ALTER_PART = "S7_ALTER_PART";
    public static final String PROP_BL_CHG_CD = "S7_CHG_CD"; // C, D
    public static final String PROP_BL_REQ_OPT = "S7_REQ_OPT";
    public static final String PROP_BL_LEV_M = "S7_LEV_M";
    public static final String PROP_BL_SPEC_DESC = "S7_SPECIFICATION";
    public static final String PROP_BL_QUANTITY = "bl_quantity";
    public static final String PROP_BL_SYSTEM_ROW_KEY = "S7_SYSTEM_ROW_KEY";
    public static final String PROP_BL_ITEM_ID = "bl_item_item_id";
    public static final String PROP_BL_ITEM_REVISION_ID = "bl_rev_item_revision_id";
    public static final String PROP_BL_VARIANTCONDITION = "bl_occ_mvl_condition";
    public static final String PROP_BL_OBJECT_TYPE = "bl_item_object_type";
    public static final String PROP_BL_REV_OBJECT_TYPE = "bl_rev_object_name";
    public static final String PROP_BL_VARIANT_CONDITION = "bl_variant_condition";
    public static final String PROP_BL_OCC_FND_OBJECT_ID = "bl_occ_fnd0objectId";
    public static final String PROP_BL_ABS_OCC_ID = "bl_abs_occ_id";
    public static final String PROP_BL_LEVEL = "bl_level_starting_0";

    /* [SR없음][20150914][jclee] DVP Sample 속성 BOMLine으로 이동*/
    public static final String PROP_S7_BL_DVP_NEEDED_QTY = "S7_DVP_NEEDED_QTY"; // DVP SAMPLE 필요수량
    public static final String PROP_S7_BL_DVP_USE = "S7_DVP_USE"; // DVP SAMPLE 용도
    public static final String PROP_S7_BL_DVP_REQ_DEPT = "S7_DVP_REQ_DEPT"; // DVP SAMPLE 요청부서
    
    /* [SR없음][20160317][jclee] Design User, Dept 속성 BOMLine으로 이동 */
    public static final String PROP_S7_BL_ENG_DEPT_NM = "S7_ENG_DEPT_NM"; // 설계담당 팀
    public static final String PROP_S7_BL_ENG_RESPONSIBLITY = "S7_ENG_RESPONSIBLITY"; // 설계 담당 담당
    
    //[SR170706-008][LJG] Proto Tooling 컬럼 추가
    public static final String PROP_BL_PROTO_TOOLING = "S7_PROTO_TOOLING"; // Proto Tooling 여부
    
    //[20180213][ljg] 시스템 코드 리비전 정보에서 bomline정보로 이동
    public static final String PROP_BL_BUDGET_CODE = "S7_BUDGET_CODE"; // system code
    
    // 운영에 BMIDE 미 적용 으로 인해 임시 주석 처리하고 S7_ALTER_PART 사용함
    public static final String PROP_BL_PRE_ALTER_PART = "S7_PRE_ALTER_PART";

    // 20201021 EJS Column 추가.
    public static final String PROP_BL_EJS = "S7_EJS";
    
    // 20210104 WEIGHT_MANAGEMENT Column 추가 by 전성용.
    public static final String PROP_BL_WEIGHT_MANAGEMENT = "S7_Weight_Management";
    
    // ===================================================================
    // SEARCH ATTRIBUTE
    // ===================================================================
    /** Dataset���띿꽦 */
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
    public static final String MECO_IS_COMPLETED = "m7_IS_COMPLETED";
    public static final String MECO_NO = "m7_MECO_NO";
    public static final String MECO_EFFECTIVITY_DATE ="m7_EFFECTIVITY_DATE";
    public static final String MECO_TYPED_REFERENCE = "m7_MECO_TypedReference";
    public static final String MECO_PROJECT = "m7_PROJECT";
    public static final String MECO_MATURITY = "s7_MATURITY";
    public static final String MECO_EFFECT_DATE = "m7_EFFECT_DATE";
    public static final String MECO_EFFECT_EVENT = "m7_EFFECT_EVENT";
    public static final String MECO_CHANGE_REASON = "m7_CHANGE_REASON";

    //===============================================
    // ATTRIBUTE - Task
    //===============================================
    public static final String PROP_PROCESS_CHILD_TASKS = "child_tasks";
    public static final String PROP_PROCESS_VALID_SIGNOFFS = "valid_signoffs";

    //===============================================
    // ATTRIBUTE - GroupMember
    //===============================================
    public static final String PROP_USER = "user";
    
    // [SR141119-021][20150119] ymjang, 영문 작업표준서 결재란 공백 오류 수정 의뢰
    public static final String PROP_OS_USER = "os_username";
}
