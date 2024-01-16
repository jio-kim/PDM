package com.ssangyong.commands.ec.fncepl.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ca.odell.glazedlists.EventList;

/**
 * EPL CHECK Data
 * 
 */
public class FncEplCheckData {

	private boolean rowCheck = false;
	private String functionNo = null; // Function No
	private String prodNo = null; // Product No
	private String ecoType = null; // Vehicle ECO : V , Power Train ECO : P
	private String applyEcoNo = null; // 반영 ECO No
	private String addEcoPublish = null; // 추가 ECO 발행
	private String description = null; // 비고
	private String fncEplPuid = null; // table row PUID
	private String attachFilePuid = null; // 첨부파일 PUID
	private String baseDate = null; // 기준일자
	private String createDate = null; // 등록일자
	private String isLatestCheck = null; // 같은 Function 중 기중 Date 가 늦은 것
	private String registerId = null; // 등록자 ID
	private String attachFilePath = null; // Upload 파일 경로
	private String prodDspName = null;// Project display name
	private int regFncCnt = 0;// 등록 Function
	private int needCnt = 0;// 검토필요 Function
	private int specCnt = 0;// 사양 Function

	private Date startRegDate = null; // 등록일 시작일
	private Date endRegDate = null; // 등록일 종료일

	private boolean isAttachFileDelete = false;// 첨부파일 삭제 여부

	private FncEplCheckData rowDataObj = null; // 현재 Row 모든 정보

	public static String PROP_NAME_ROW_CHECK = "rowCheck";// 행선택
	public static String PROP_NAME_IS_LATEST_CHECK = "isLatestCheck";// Latest
	public static String PROP_NAME_FUNCTION_NO = "functionNo";// Function
	public static String PROP_NAME_BASE_DATE = "baseDate";// 기준 Date
	public static String PROP_NAME_APPLY_ECO_NO = "applyEcoNo";// 반영 ECO
	public static String PROP_NAME_ADD_ECO_PUBLISH = "addEcoPublish";// 추가 ECO 발행
	public static String PROP_NAME_DESCRIPTION = "description";// 비고
	public static String PROP_NAME_CREATE_DATE = "createDate";// 등록일
	public static String PROP_NAME_FNC_EPL_PUID = "fncEplPuid";// table row PUID
	public static String PROP_NAME_ROW_DATA_OBJ = "rowDataObj";// 현재 Row 의 모든 정보

	public static String PROP_NAME_PROD_NO = "prodNo";// PROD NO
	public static String PROP_NAME_REG_FNC_CNT = "regFncCnt";// 등록 Function
	public static String PROP_NAME_NEED_FNC_CNT = "needCnt";// 검토필요 Function
	public static String PROP_NAME_SPEC_FNC_CNT = "specCnt";// 사양 Function

	public static final String CHECK_BOX_CONFIG_LABEL = "checkBox";
	public static final String CHECK_BOX_EDITOR_CONFIG_LABEL = "checkBoxEditor";

	public static final String ALIGN_CELL_CONTENTS_CENTER_CONFIG_LABEL = "alignCellContentsLeftConfigLabel"; // 중간으로 정렬

	public static final String[] ECO_PUBLISH_LIST = new String[] { "필요", "사양정리", "불필요" };

	private EventList<FncEplCheckData> tableDataList; // 테이블 데이터 리스트

	private ArrayList<FncEplCheckData> deleteDataList = null; // 삭제되는 데이터 리스트
	
	public static final String TEMPLATE_FNC_EPL_CHECK_RPT = "ssangyong_fnc_epl_check_list_template.xlsx"; // Excel 리스트 출력 Template 파일

	/**
	 * @return the rowCheck
	 */
	public boolean isRowCheck() {
		return rowCheck;
	}

	/**
	 * @param rowCheck
	 *            the rowCheck to set
	 */
	public void setRowCheck(boolean rowCheck) {
		this.rowCheck = rowCheck;
	}

	/**
	 * @return the functionNo
	 */
	public String getFunctionNo() {
		return functionNo;
	}

	/**
	 * @param functionNo
	 *            the functionNo to set
	 */
	public void setFunctionNo(String functionNo) {
		this.functionNo = functionNo;
	}

	/**
	 * @return the prodNo
	 */
	public String getProdNo() {
		return prodNo;
	}

	/**
	 * @param prodNo
	 *            the prodNo to set
	 */
	public void setProdNo(String prodNo) {
		this.prodNo = prodNo;
	}

	/**
	 * @return the ecoType
	 */
	public String getEcoType() {
		return ecoType;
	}

	/**
	 * @param ecoType
	 *            the ecoType to set
	 */
	public void setEcoType(String ecoType) {
		this.ecoType = ecoType;
	}

	/**
	 * @return the applyEcoNo
	 */
	public String getApplyEcoNo() {
		return applyEcoNo;
	}

	/**
	 * @param applyEcoNo
	 *            the applyEcoNo to set
	 */
	public void setApplyEcoNo(String applyEcoNo) {
		this.applyEcoNo = applyEcoNo;
	}

	/**
	 * @return the addEcoPublish
	 */
	public String getAddEcoPublish() {
		return addEcoPublish;
	}

	/**
	 * @param addEcoPublish
	 *            the addEcoPublish to set
	 */
	public void setAddEcoPublish(String addEcoPublish) {
		this.addEcoPublish = addEcoPublish;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the fncEplPuid
	 */
	public String getFncEplPuid() {
		return fncEplPuid;
	}

	/**
	 * @param fncEplPuid
	 *            the fncEplPuid to set
	 */
	public void setFncEplPuid(String fncEplPuid) {
		this.fncEplPuid = fncEplPuid;
	}

	/**
	 * @return the attachFilePuid
	 */
	public String getAttachFilePuid() {
		return attachFilePuid;
	}

	/**
	 * @param attachFilePuid
	 *            the attachFilePuid to set
	 */
	public void setAttachFilePuid(String attachFilePuid) {
		this.attachFilePuid = attachFilePuid;
	}

	/**
	 * @return the baseDate
	 */
	public String getBaseDate() {
		return baseDate;
	}

	/**
	 * @param baseDate
	 *            the baseDate to set
	 */
	public void setBaseDate(String baseDate) {
		this.baseDate = baseDate;
	}

	/**
	 * @return the createDate
	 */
	public String getCreateDate() {
		return createDate;
	}

	/**
	 * @param createDate
	 *            the createDate to set
	 */
	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}

	/**
	 * @return the isLatestCheck
	 */
	public String getIsLatestCheck() {
		return isLatestCheck;
	}

	/**
	 * @param isLatestCheck
	 *            the isLatestCheck to set
	 */
	public void setIsLatestCheck(String isLatestCheck) {
		this.isLatestCheck = isLatestCheck;
	}

	/**
	 * @return the registerId
	 */
	public String getRegisterId() {
		return registerId;
	}

	/**
	 * @param registerId
	 *            the registerId to set
	 */
	public void setRegisterId(String registerId) {
		this.registerId = registerId;
	}

	/**
	 * @return the attachFilePath
	 */
	public String getAttachFilePath() {
		return attachFilePath;
	}

	/**
	 * @return the rowDataObj
	 */
	public FncEplCheckData getRowDataObj() {
		return rowDataObj;
	}

	/**
	 * @param rowDataObj
	 *            the rowDataObj to set
	 */
	public void setRowDataObj(FncEplCheckData rowDataObj) {
		this.rowDataObj = rowDataObj;
	}

	/**
	 * @param attachFilePath
	 *            the attachFilePath to set
	 */
	public void setAttachFilePath(String attachFilePath) {
		this.attachFilePath = attachFilePath;
	}

	/**
	 * @return the prodDspName
	 */
	public String getProdDspName() {
		return prodDspName;
	}

	/**
	 * @param prodDspName
	 *            the prodDspName to set
	 */
	public void setProdDspName(String prodDspName) {
		this.prodDspName = prodDspName;
	}

	/**
	 * @return the regFncCnt
	 */
	public int getRegFncCnt() {
		return regFncCnt;
	}

	/**
	 * @param regFncCnt
	 *            the regFncCnt to set
	 */
	public void setRegFncCnt(int regFncCnt) {
		this.regFncCnt = regFncCnt;
	}

	/**
	 * @return the needCnt
	 */
	public int getNeedCnt() {
		return needCnt;
	}

	/**
	 * @param needCnt
	 *            the needCnt to set
	 */
	public void setNeedCnt(int needCnt) {
		this.needCnt = needCnt;
	}

	/**
	 * @return the specCnt
	 */
	public int getSpecCnt() {
		return specCnt;
	}

	/**
	 * @param specCnt
	 *            the specCnt to set
	 */
	public void setSpecCnt(int specCnt) {
		this.specCnt = specCnt;
	}

	/**
	 * @return the startRegDate
	 */
	public Date getStartRegDate() {
		return startRegDate;
	}

	/**
	 * @param startRegDate
	 *            the startRegDate to set
	 */
	public void setStartRegDate(Date startRegDate) {
		this.startRegDate = startRegDate;
	}

	/**
	 * @return the endRegDate
	 */
	public Date getEndRegDate() {
		return endRegDate;
	}

	/**
	 * @param endRegDate
	 *            the endRegDate to set
	 */
	public void setEndRegDate(Date endRegDate) {
		this.endRegDate = endRegDate;
	}

	/**
	 * @return the isAttachFileDelete
	 */
	public boolean isAttachFileDelete() {
		return isAttachFileDelete;
	}

	/**
	 * @param isAttachFileDelete
	 *            the isAttachFileDelete to set
	 */
	public void setAttachFileDelete(boolean isAttachFileDelete) {
		this.isAttachFileDelete = isAttachFileDelete;
	}

	/**
	 * @return the deleteDataList
	 */
	public ArrayList<FncEplCheckData> getDeleteDataList() {
		return deleteDataList;
	}

	/**
	 * @param deleteDataList
	 *            the deleteDataList to set
	 */
	public void setDeleteDataList(ArrayList<FncEplCheckData> deleteDataList) {
		this.deleteDataList = deleteDataList;
	}

	/**
	 * @return the tableDataList
	 */
	public EventList<FncEplCheckData> getTableDataList() {
		return tableDataList;
	}

	/**
	 * @param tableDataList
	 *            the tableDataList to set
	 */
	public void setTableDataList(EventList<FncEplCheckData> tableDataList) {
		this.tableDataList = tableDataList;
	}

	/**
	 * table 속성 명 리스트
	 * 
	 * @return
	 */
	public static String[] getPropertyNames() {
		return new String[] { PROP_NAME_ROW_CHECK, PROP_NAME_IS_LATEST_CHECK, PROP_NAME_FUNCTION_NO, PROP_NAME_BASE_DATE, PROP_NAME_APPLY_ECO_NO,
				PROP_NAME_ADD_ECO_PUBLISH, PROP_NAME_DESCRIPTION, PROP_NAME_CREATE_DATE, PROP_NAME_FNC_EPL_PUID, PROP_NAME_ROW_DATA_OBJ };
	}

	public static Map<String, String> getPropertyToLabelMap() {
		Map<String, String> propertyToLabelMap = new LinkedHashMap<String, String>();
		propertyToLabelMap.put(PROP_NAME_ROW_CHECK, "");
		propertyToLabelMap.put(PROP_NAME_IS_LATEST_CHECK, "Latest");
		propertyToLabelMap.put(PROP_NAME_FUNCTION_NO, "Function");
		propertyToLabelMap.put(PROP_NAME_BASE_DATE, "기준 Date");
		propertyToLabelMap.put(PROP_NAME_APPLY_ECO_NO, "반영 ECO");
		propertyToLabelMap.put(PROP_NAME_ADD_ECO_PUBLISH, "추가ECO 발행");
		propertyToLabelMap.put(PROP_NAME_DESCRIPTION, "비고");
		propertyToLabelMap.put(PROP_NAME_CREATE_DATE, "등록일");
		propertyToLabelMap.put(PROP_NAME_FNC_EPL_PUID, "");
		propertyToLabelMap.put(PROP_NAME_ROW_DATA_OBJ, "");
		return propertyToLabelMap;
	}

	/**
	 * 테이블 Property 명을 가져옴
	 * 
	 * @return
	 */
	public static List<String> getPropertyNamesAsList() {
		return Arrays.asList(getPropertyNames());
	}

	/**
	 * 속성의 Column Index
	 * 
	 * @param propertyName
	 * @return
	 */
	public static int getColumnIndexOfProperty(String propertyName) {
		return getPropertyNamesAsList().indexOf(propertyName);
	}

	/**
	 * table 속성 명 리스트
	 * 
	 * @return
	 */
	public static String[] getStsPropertyNames() {
		return new String[] { PROP_NAME_PROD_NO, PROP_NAME_REG_FNC_CNT, PROP_NAME_NEED_FNC_CNT, PROP_NAME_SPEC_FNC_CNT };
	}

	public static Map<String, String> getStsPropertyToLabelMap() {
		Map<String, String> propertyToLabelMap = new LinkedHashMap<String, String>();
		propertyToLabelMap.put(PROP_NAME_PROD_NO, "Veh/Prod No.");
		propertyToLabelMap.put(PROP_NAME_REG_FNC_CNT, "등록\nFunction");
		propertyToLabelMap.put(PROP_NAME_NEED_FNC_CNT, "검토필요\nFunction");
		propertyToLabelMap.put(PROP_NAME_SPEC_FNC_CNT, "사양정리\nFunction");
		return propertyToLabelMap;
	}

	/**
	 * 테이블 Property 명을 가져옴
	 * 
	 * @return
	 */
	public static List<String> getStsPropertyNamesAsList() {
		return Arrays.asList(getStsPropertyNames());
	}

	/**
	 * 속성의 Column Index
	 * 
	 * @param propertyName
	 * @return
	 */
	public static int getStsColumnIndexOfProperty(String propertyName) {
		return getStsPropertyNamesAsList().indexOf(propertyName);
	}

	public static String objToStr(Object obj) {
		return obj != null ? (String) obj : "";
	}

	public static int objToInt(Object obj) {
		return obj != null ? ((BigDecimal) obj).intValue() : 0;
	}
}
