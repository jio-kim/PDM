package com.kgm.soa.util;

import java.text.SimpleDateFormat;

public class SYMCClass {
	/**************************************** 쌍용 ItemType 정보 String **************************************************************/
  
  /** 쌍용 Product Type */
  public static String S7_PRODUCTPARTTYPE = "S7_Product";
  /** 쌍용 Product Revision Type */
  public static String S7_PRODUCTPARTREVISIONTYPE =  "S7_ProductRevision";

  
  /** 쌍용 Function Type */
  public static String S7_FNCPARTTYPE = "S7_Function";
  /** 쌍용 Function Revision Type */
  public static String S7_FNCPARTREVISIONTYPE =  "S7_FunctionRevision";

  
  /** 쌍용 Function Master Type */
  public static String S7_FNCMASTPARTTYPE = "S7_FunctionMast";
  /** 쌍용 Function Master Revision Type */
  public static String S7_FNCMASTPARTREVISIONTYPE =  "S7_FunctionMastRevision";

  
  /** 쌍용 Variant Type */
  public static String S7_VARIANTPARTTYPE = "S7_Variant";
  /** 쌍용 Variant Revision Type */
  public static String S7_VARIANTPARTREVISIONTYPE =  "S7_VariantRevision";

  
  
	/** 쌍용 Vehpart Type */
	public static String S7_VEHPARTTYPE = "S7_Vehpart";
	/** 쌍용 Vehpart Revision Type */
	public static String S7_VEHPARTREVISIONTYPE =  "S7_VehpartRevision";
	
	
  /** 쌍용 Material Type */
  public static String S7_MATPARTTYPE = "S7_Material";
  /** 쌍용 Material Revision Type */
  public static String S7_MATPARTREVISIONTYPE =  "S7_MaterialRevision";

  /** 쌍용 Standard Type */
  public static String S7_STDPARTTYPE = "S7_Stdpart";
  /** 쌍용 Standard Revision Type */
  public static String S7_STDPARTREVISIONTYPE =  "S7_StdpartRevision";

  /** 쌍용 Standard Type */
  public static String S7_SOFTPARTTYPE = "S7_Software";
  /** 쌍용 Standard Revision Type */
  public static String S7_SOFTPARTREVISIONTYPE =  "S7_SoftwareRevision";
  

  /** 쌍용 TechDoc Type */
  public static String S7_TECHDOCTYPE = "S7_ENGDOC";
  /** 쌍용 TechDoc Revision Type */
  public static String S7_TECHDOCREVISIONTYPE =  "S7_ENGDOCRevision";

	
	
	/** 쌍용 Item Type */
	public static String ITEM_TYPE = "Item";
	/** 쌍용 Item Revision ID */
	public static String ITEM_REV_ID = "000";

	/**************************************** 쌍용 Query 정보 String **************************************************************/
	/** Item Search Name */
	public static String ITEMS_SEARCH = "Item...";
	
	public static String[] ITEMS_IDNAMETYPE_SEARCH_KEY = {"ItemID", "Name", "Type"};
	
	/**************************************** Value 정보 String **************************************************************/
	/** ItemID field 값 */
	public static String ITEMIDFIELD = "Item ID";
	
	/** CIS NO field 값 */
	public static String CISNOFIELD = "CIS No";
	
	/** Name field 값 */
	public static String NAMEFIELD = "Name";
	
	/** 쌍용 심플 데이트 포멧 */
	public static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	
	/** 쌍용 데이트 포멧 */
	public static SimpleDateFormat DATE_FORMAT_MM = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	
	/** 쌍용 IMAN_reference 타입 */
	public static String REFERENCE_REL = "IMAN_reference";
	
	/** 쌍용 Contents 타입 */
	public static String CONTENT_REL = "contents";
	
	/** 쌍용 제품 리비전 밑에 도면 릴레이션 */
	public static String RELATED_DWG_REL = "IMAN_specification";
	
	public static int PROBLEM = 0;
	public static int SOLUTION = 1;
	
	/** WEBJDBC URL */
	public static String SYMC_WEBJDBC_URL = "SYMC_WEBJDBC_URL";
	
	/** FTP INFO [0=URL,1=PORT,2=USER,3=PASS,4=ROOT] 구분자는 콤마(,) */
	public static String SYMC_FTP_INFO = "SYMC_FTP_INFO";
	
	/** 쌍용 INFODBA 연결 키 값. */
	public static String INFODBA = "infodba";
	/** 쌍용 IF_USER 연결 키 값. */
	public static String IF_USER = "if_user";
	
	/**************************************** ECR 정보 LOV **************************************************************/
	/** 쌍용 ECR 검토 결과 LOV */
	public static String ECR_REVIEW_RESULT_LOV = "PK_ECR_REVIEW_RESULT.LOV";
	
	/**************************************** 변경 대상 **************************************************************/
	/** 풍강 표준양식을 add하는 relation 타입 */
	public static String STANDARD_DOC_REL = "PK4_standard_doc_rel";
	/** 풍강 설비 Dummy Item 타입 */
	public static String Equipment_Dummy_TYPE = "PK4_dummy";
	/** 풍강 제품 검색 쿼리 */
	public static String QryProductSearch = "PK_PRODUCT_SEARCH";
	/** 풍강 원자재 검색 쿼리 */
	public static String QryRawMaterialsSearch = "PK_RAWMATERIALS_SEARCH";
	/** 풍강 공정 Item 타입 */
	public static String Processing_TYPE = "PK4_Processing";
	/** 풍강 설비 ETC 검색 쿼리 */
	public static String QryEquipmentESearch = "PK_EQUIPMENTE_SEARCH";
	/** 풍강 부자재 검색 쿼리 */
	public static String QrySubMaterialsSearch = "PK_SUBMATERIALS_SEARCH";
	/** 풍강 설비 Former 검색 쿼리 */
	public static String QryEquipmentFSearch = "PK_EQUIPMENTF_SEARCH";
	/** 풍강 설비 Tapping 검색 쿼리 */
	public static String QryEquipmentTSearch = "PK_EQUIPMENTT_SEARCH";
	/** 풍강 공구 Former 검색 쿼리 */
	public static String QryMoldFormerSearch = "PK_MOLD_FORMER_SEARCH";
	/** 풍강 공구 Tapping 검색 쿼리 */
	public static String QryMoldTappingSearch = "PK_MOLD_TAPPING_SEARCH";
	/** 풍강 공구 기타 검색 쿼리 */
	public static String QryMoldEtcSearch = "PK_MOLD_ETC_SEARCH";
	/** 풍강 공구 TAP 검색 쿼리 */
	public static String QryMoldTapSearch = "PK_MOLD_TAP_SEARCH";
	
	/** 쌍용 Temp 경로. */
	public static String TEMPDIRECTORY = "C:/Siemens/ssangyong/temp";
	/** 쌍용 유저 검색 쿼리 */
	public static String QryUserSearch = "PK_USER_SEARCH";
	/** 쌍용 Item... 검색 쿼리 */
	public static String QryItemSearch = "Item...";
	/** 쌍용 합의문서결재 타입 이름 */
	public static String CONSENSUS_DOC_WORKFLOW = "합의문서결재";
	/** 쌍용 IMAN_specification 타입 */
	public static String SPECIFICATION_REL = "IMAN_specification";
	
	/**************************************** SPAlM DATASET TYPE **************************************************************/
	/** 첨부 되는 데이터셋 타입 정의 */
	public static String SYMC_DATASET_TYPE = "SYMC_DATASET_TYPE_LIST";
	/** 생성 되는 폴더의 타입 정의 */
	public static String SYMC_FOLDER_TYPE = "SYMC_FOLDER_TYPE_CHECK";
	/** 생성 되는 폴더의 Role 정보 정의 */
	public static String SYMC_FOLDER_ROLE = "SYMC_FOLDER_ROLE_CHECK";
	/** 생성 되는 폴더의 Type 정의 */
	public static String SYMC_CREATEFOLDERTYPE = "SYMC_CREATE_FOLDER_TYPE";	
}
