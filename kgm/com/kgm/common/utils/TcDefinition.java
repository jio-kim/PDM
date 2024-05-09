package com.kgm.common.utils;

import java.util.ArrayList;
import java.util.List;


public class TcDefinition {
	
	//Option Item Type
	public static final String CORPORATE_ITEM_TYPE = "S7_CorpOption";
	public static final String PRODUCT_ITEM_TYPE = "S7_Product";
	public static final String VARIANT_ITEM_TYPE = "S7_Variant";
	public static final String FUNCTION_ITEM_TYPE = "S7_Function";
	public static final String FUNCTION_MASTER_ITEM_TYPE = "S7_FunctionMast";
	
	//Pre BOM 관련
	public static final String PRE_PRODUCT_ITEM_TYPE = "S7_PreProduct";
	public static final String PRE_FUNCTION_ITEM_TYPE = "S7_PreFunction";
	public static final String PRE_FUNCTION_MASTER_ITEM_TYPE = "S7_PreFuncMaster";
	
    public static final String RELEASESTATUS_NAME = "Released";
    
    public static final String TC_SPECIFICATION_RELATION = "IMAN_specification";
    public static final String TC_REFERENCE_RELATION = "IMAN_reference";
    
    public static final String REVISION_TYPE_STCPART = "STCPartRevision";
    public static final String INITIAL_REVISION_ID_STCPART = "1a.01";
    public static final String INITIAL_STD_REVISION_ID_STCPART = "-1a.01";
    public static final String REVISION_TYPE_STCNPI = "STCNPIRevision";
    public static final String REVISION_TYPE_STCMRO = "STCMRORevision";
    
    public static final String DATASET_TYPE_ACAD = "ACADDWG";
    public static final String DATASET_TYPE_EXCEL = "MSExcel";
    public static final String DATASET_TYPE_EXCELX = "MSExcelX";
    public static final String DATASET_TYPE_EDIF = "EDAGenSchem";
    public static final String DATASET_TYPE_ORCAD = "OrCAD";
    public static final String DATASET_TYPE_CGM = "DrawingSheet";
    // deprecate start
    public static final String DATASET_TYPE_PROPRT = "ProPrt";
    public static final String DATASET_TYPE_PROASM = "ProAsm";
    public static final String DATASET_TYPE_PRODRW = "ProDrw";
    // deprecate end
    public static final String DATASET_TYPE_STCPROPRT = "stc_part";
    public static final String DATASET_TYPE_STCPROASM = "stc_assy";
    public static final String DATASET_TYPE_STCPROFLT = "mmns_flat_harness";
    public static final String DATASET_TYPE_STCPROBLK = "stc_bulk";
    public static final String DATASET_TYPE_STCPROSHM = "stc_sheetmetal";
    public static final String DATASET_TYPE_ORCADVIWEING = "SCHFATF";
    public static final String DATASET_TYPE_STCJUNGUM = "STCJungUm";
    
    public static final List<String> SWDATASETTYPES = new ArrayList<String>();
    static{
    	SWDATASETTYPES.add("STCSWExeSet");
    	SWDATASETTYPES.add("STCSWSrcSet");
    	SWDATASETTYPES.add("STCSWOutExeSet");
    	SWDATASETTYPES.add("STCSWOutSrcSet");
    }
    
    public static final ArrayList<String> CAT_DOWN_FILTER_TYPE = new ArrayList<String>();
    static{
        // 3D Dataset
        CAT_DOWN_FILTER_TYPE.add("CATPart");
        CAT_DOWN_FILTER_TYPE.add("CATDrawing");
        CAT_DOWN_FILTER_TYPE.add("CATProduct");
        CAT_DOWN_FILTER_TYPE.add("catia");
        // Zip Dataset
        CAT_DOWN_FILTER_TYPE.add("Zip");
    }
    
	public static int STATUS_WORKING = 0;
	public static int STATUS_IN_PROCESS = 1;
	public static int STATUS_HAS_STATUS = 2;
	public static int STATUS_IN_PROCESS_AND_HAS_STATUS = 3;
	
	
	public static String APPLINE_SW_DELIVERY = "납품SW 입고";
	public static String APPLINE_SW_REPOS = "SW 입고";
	public static String APPLINE_SW_NREPOS = "NSW 입고";
	
	public static final String TEMPLATE_SWREQUESTREPOSITORY = "swRequestRepository.template"; // 저장소 생성요청 통보
	public static final String TEMPLATE_SWCREATEREPOSITORY = "swCreateRepository.template"; // 저장소생성 완료 통보
	public static final String TEMPLATE_SWSTORENOREPOS = "swStoreNoRepos.template"; //저장소 미관리 입고
	public static final String TEMPLATE_SWSTOREREPOS = "storeRepos.template"; // 저장소 관리 입고
	public static final String TEMPLATE_SWBASELINE = "swBaseLine.template"; // 베이스 라인 입고
	public static final String TEMPLATE_REQDIST = "swReqDist.template"; // (SW)표준배포서
	
	public static String getWorkFlowTemplateDir(){
	  //  Map<String, String> map = System.getenv();
        String dir = System.getenv("TPR");
        dir = dir + "\\STC\\richtexteditor\\";
        return dir;
	}
}
