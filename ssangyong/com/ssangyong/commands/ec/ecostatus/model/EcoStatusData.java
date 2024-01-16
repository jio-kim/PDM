package com.ssangyong.commands.ec.ecostatus.model;

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ca.odell.glazedlists.EventList;

/**
 * 설계 변경현황 리스트 Data
 */
public class EcoStatusData {

	private String status = null; // 상태
	private String stageType = null; // 분류
	private String projectId = null; // Project
	private String ospecId = null; // 구분
	private String changeDesc = null; // 변경내용
	private String estApplyDate = null; // 예상적용시점
	private String receiptDate = null; // O/SPECT 접수일
	private String firstMailSendDate = null; // EPL검토요청 업무연락/메일발송일자
	private String ecoCompleteReqDate = null; // ECO 완료 요청일
	private String estChangePeriod = null; // 예상 설계변경 기간
	private String ecoLastCompleteDate = null; // 최종ECO처리일자
	private String realChangePeriod = null; // 실제 설계변경 기간
	private int totalReviewList = 0; // 전체 검토 리스트
	private int requiredEcoList = 0; // 필수 설계변경 필요리스트
	private int inCompleteCnt = 0; // 기간내 완료
	private int delayCompleteCnt = 0; // 지연완료
	private int missCompleteCnt = 0; // 누락완료
	private int inProcessCnt = 0; // 기간내 진행
	private int delayProcessCnt = 0; // 지연 진행
	private int missProcess = 0; // 누락 진행
	private String registerDate = null; // 등록일
	private String registerUserId = null; // 등록자 ID
	private String description = null; // 비고
	private int specArrange = 0;// 사양정리 (화면에서는 표시안함)
	private String masterPuid = null; // MASTER PUID
	private String ecoFirstCompleteDate = null; // ECO 처리날짜 중 최초 날짜
	private String gModel = null; // GMODEL

	private boolean rowCheck = false;
	private String lastFirstPeriod = null; // ECO 최종 처리일자 - ECO 최초 처리 일자 기간

	// private boolean isStausWarning = false; // 상태에 따른 Color 표시

	private EcoStatusData rowDataObj = null; // 현재 Row 정보

	private Date startRegDate = null; // 등록일 시작일
	private Date endRegDate = null; // 등록일 종료일

	private STATUS_COLOR statusColor = STATUS_COLOR.BLACK; // 상태 컬러

	public static enum STATUS_COLOR {
		BLACK, RED, BLUE
	};

	public static String PROP_NAME_STATUS = "status";// 상태
	public static String PROP_NAME_STAGE_TYPE = "stageType";// 분류
	public static String PROP_NAME_PROJECT_ID = "projectId";// Project
	public static String PROP_NAME_OSPECT_ID = "ospecId";// 구분(O/Spec)
	public static String PROP_NAME_CHANGE_DESC = "changeDesc";// 변경내용
	public static String PROP_NAME_EST_APPLY_DATE = "estApplyDate";// 예상적용시점
	public static String PROP_NAME_RECEIPT_DATE = "receiptDate";// O/SPECT 접수일
	// public static String PROP_NAME_FIRST_MAIL_SEND_DATE = "firstMailSendDate";// 처음 메일 발송 날짜
	public static String PROP_NAME_COMPLETE_REQ_DATE = "ecoCompleteReqDate";// ECO 완료 요청일
	public static String PROP_NAME_EST_CHANGE_PERIOD = "estChangePeriod";// 예상 설계변경 기간
	public static String PROP_NAME_ECO_LAST_COMPLETE_DATE = "ecoLastCompleteDate";// 최종ECO처리일자
	public static String PROP_NAME_REAL_CHG_REIOAD = "realChangePeriod";// 실제 설계변경 기간
	public static String PROP_NAME_TOTAL_REVIEWLIST = "totalReviewList";// 전체 검토 리스트
	public static String PROP_NAME_REQUIRED_ECO_LIST = "requiredEcoList";// 필수 설계변경 필요리스트
	public static String PROP_NAME_IN_COMPLETE_CNT = "inCompleteCnt";// 기간내 완료
	public static String PROP_NAME_DELAY_COMPLETE_CNT = "delayCompleteCnt";// 지연완료
	public static String PROP_NAME_MISS_COMPLETE_CNT = "missCompleteCnt";// 누락완료
	public static String PROP_NAME_IN_PROC_CNT = "inProcessCnt";// 기간내 진행
	public static String PROP_NAME_DELAY_PROC_CNT = "delayProcessCnt";// 지연 진행
	public static String PROP_NAME_MISS_PROC_CNT = "missProcess";// 누락 진행
	public static String PROP_NAME_REGISTER_DATE = "registerDate";// 등록일
	public static String PROP_NAME_DESCRIPTION = "description";// 비고
	public static String PROP_NAME_SPEC_ARRANGE = "specArrange";// 사양정리
	public static String PROP_NAME_ECO_FIRST_COMPLETE_DATE = "ecoFirstCompleteDate";// ECO 처리날짜 중 최초 날짜
	public static String PROP_NAME_ECO_LAST_FIRST_PERIOD = "lastFirstPeriod";// ECO 최종 처리일자 - ECO 최초 처리 일자 기간

	public static String PROP_NAME_MASTER_PUID = "masterPuid";// 기준정보 Unique Key
	// public static String PROP_NAME_IS_STATUS_WARNING = "isStausWarning";// 상태에 따른 Color 표시
	public static String PROP_NAME_IS_STATUS_COLOR = "statusColor";// 상태에 따른 Color 표시

	public static String PROP_NAME_ROW_DATA_OBJ = "rowDataObj";// 현재 Row 의 모든 정보

	public static String PROP_NAME_ROW_CHECK = "rowCheck";// 행선택

	public static final String CELL_RED_LABLEL = "CELL_RED_LABLEL"; // Row Cell 붉은색 라벨
	public static final String CELL_BLUE_LABLEL = "CELL_BLUE_LABLEL"; // Row Cell 파란색 라벨
	public static final String CELL_CHANGE_STATUS_LABLEL = "CELL_CHANGE_STATUS_LABLEL"; // Row Cell 라벨
	public static final String ALIGN_CELL_CONTENTS_CENTER_CONFIG_LABEL = "alignCellContentsLeftConfigLabel"; // 중간으로 정렬

	public static final String CHECK_BOX_CONFIG_LABEL = "checkBox";
	public static final String CHECK_BOX_EDITOR_CONFIG_LABEL = "checkBoxEditor";

	public static final String TEMPLATE_DS_ECO_TOTAL_LIST_RPT = "ssangyong_eco_status_total_list_template.xlsx"; // 설계변경현황 Export Template명
	public static final String TEMPLATE_DS_ECO_TOTAL_DESC_RPT = "ssangyong_eco_status_desc_template.xlsx"; // 변경 상세Export Template명

	private EventList<EcoStatusData> searchChangeStatusList; // 설계변경현황 테이블 데이터 리스트

	public EcoStatusData() {

	}

	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @param status
	 *            the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * @return the stageType
	 */
	public String getStageType() {
		return stageType;
	}

	/**
	 * @param stageType
	 *            the stageType to set
	 */
	public void setStageType(String stageType) {
		this.stageType = stageType;
	}

	/**
	 * @return the projectId
	 */
	public String getProjectId() {
		return projectId;
	}

	/**
	 * @param projectId
	 *            the projectId to set
	 */
	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	/**
	 * @return the ospecId
	 */
	public String getOspecId() {
		return ospecId;
	}

	/**
	 * @param ospecId
	 *            the ospecId to set
	 */
	public void setOspecId(String ospecId) {
		this.ospecId = ospecId;
	}

	/**
	 * @return the changeDesc
	 */
	public String getChangeDesc() {
		return changeDesc;
	}

	/**
	 * @param changeDesc
	 *            the changeDesc to set
	 */
	public void setChangeDesc(String changeDesc) {
		this.changeDesc = changeDesc;
	}

	/**
	 * @return the estApplyDate
	 */
	public String getEstApplyDate() {
		return estApplyDate;
	}

	/**
	 * @param estApplyDate
	 *            the estApplyDate to set
	 */
	public void setEstApplyDate(String estApplyDate) {
		this.estApplyDate = estApplyDate;
	}

	/**
	 * @return the receiptDate
	 */
	public String getReceiptDate() {
		return receiptDate;
	}

	/**
	 * @param receiptDate
	 *            the receiptDate to set
	 */
	public void setReceiptDate(String receiptDate) {
		this.receiptDate = receiptDate;
	}

	/**
	 * @return the firstMailSendDate
	 */
	public String getFirstMailSendDate() {
		return firstMailSendDate;
	}

	/**
	 * @param firstMailSendDate
	 *            the firstMailSendDate to set
	 */
	public void setFirstMailSendDate(String firstMailSendDate) {
		this.firstMailSendDate = firstMailSendDate;
	}

	/**
	 * @return the ecoCompleteReqDate
	 */
	public String getEcoCompleteReqDate() {
		return ecoCompleteReqDate;
	}

	/**
	 * @param ecoCompleteReqDate
	 *            the ecoCompleteReqDate to set
	 */
	public void setEcoCompleteReqDate(String ecoCompleteReqDate) {
		this.ecoCompleteReqDate = ecoCompleteReqDate;
	}

	/**
	 * @return the estChangePeriod
	 */
	public String getEstChangePeriod() {
		return estChangePeriod;
	}

	/**
	 * @param estChangePeriod
	 *            the estChangePeriod to set
	 */
	public void setEstChangePeriod(String estChangePeriod) {
		this.estChangePeriod = estChangePeriod;
	}

	/**
	 * @return the ecoLastCompleteDate
	 */
	public String getEcoLastCompleteDate() {
		return ecoLastCompleteDate;
	}

	/**
	 * @param ecoLastCompleteDate
	 *            the ecoLastCompleteDate to set
	 */
	public void setEcoLastCompleteDate(String ecoLastCompleteDate) {
		this.ecoLastCompleteDate = ecoLastCompleteDate;
	}

	/**
	 * @return the realChangePeriod
	 */
	public String getRealChangePeriod() {
		return realChangePeriod;
	}

	/**
	 * @param realChangePeriod
	 *            the realChangePeriod to set
	 */
	public void setRealChangePeriod(String realChangePeriod) {
		this.realChangePeriod = realChangePeriod;
	}

	/**
	 * @return the totalReviewList
	 */
	public int getTotalReviewList() {
		return totalReviewList;
	}

	/**
	 * @param totalReviewList
	 *            the totalReviewList to set
	 */
	public void setTotalReviewList(int totalReviewList) {
		this.totalReviewList = totalReviewList;
	}

	/**
	 * @return the requiredEcoList
	 */
	public int getRequiredEcoList() {
		return requiredEcoList;
	}

	/**
	 * @param requiredEcoList
	 *            the requiredEcoList to set
	 */
	public void setRequiredEcoList(int requiredEcoList) {
		this.requiredEcoList = requiredEcoList;
	}

	/**
	 * @return the inCompleteCnt
	 */
	public int getInCompleteCnt() {
		return inCompleteCnt;
	}

	/**
	 * @param inCompleteCnt
	 *            the inCompleteCnt to set
	 */
	public void setInCompleteCnt(int inCompleteCnt) {
		this.inCompleteCnt = inCompleteCnt;
	}

	/**
	 * @return the delayCompleteCnt
	 */
	public int getDelayCompleteCnt() {
		return delayCompleteCnt;
	}

	/**
	 * @param delayCompleteCnt
	 *            the delayCompleteCnt to set
	 */
	public void setDelayCompleteCnt(int delayCompleteCnt) {
		this.delayCompleteCnt = delayCompleteCnt;
	}

	/**
	 * @return the missCompleteCnt
	 */
	public int getMissCompleteCnt() {
		return missCompleteCnt;
	}

	/**
	 * @param missCompleteCnt
	 *            the missCompleteCnt to set
	 */
	public void setMissCompleteCnt(int missCompleteCnt) {
		this.missCompleteCnt = missCompleteCnt;
	}

	/**
	 * @return the inProcessCnt
	 */
	public int getInProcessCnt() {
		return inProcessCnt;
	}

	/**
	 * @param inProcessCnt
	 *            the inProcessCnt to set
	 */
	public void setInProcessCnt(int inProcessCnt) {
		this.inProcessCnt = inProcessCnt;
	}

	/**
	 * @return the delayProcessCnt
	 */
	public int getDelayProcessCnt() {
		return delayProcessCnt;
	}

	/**
	 * @param delayProcessCnt
	 *            the delayProcessCnt to set
	 */
	public void setDelayProcessCnt(int delayProcessCnt) {
		this.delayProcessCnt = delayProcessCnt;
	}

	/**
	 * @return the missProcess
	 */
	public int getMissProcess() {
		return missProcess;
	}

	/**
	 * @param missProcess
	 *            the missProcess to set
	 */
	public void setMissProcess(int missProcess) {
		this.missProcess = missProcess;
	}

	/**
	 * @return the registerDate
	 */
	public String getRegisterDate() {
		return registerDate;
	}

	/**
	 * @param registerDate
	 *            the registerDate to set
	 */
	public void setRegisterDate(String registerDate) {
		this.registerDate = registerDate;
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
	 * @return the specArrange
	 */
	public int getSpecArrange() {
		return specArrange;
	}

	/**
	 * @param specArrange
	 *            the specArrange to set
	 */
	public void setSpecArrange(int specArrange) {
		this.specArrange = specArrange;
	}

	/**
	 * @return the searchChangeStatusList
	 */
	public EventList<EcoStatusData> getSearchChangeStatusList() {
		return searchChangeStatusList;
	}

	/**
	 * @return the masterPuid
	 */
	public String getMasterPuid() {
		return masterPuid;
	}

	/**
	 * @param masterPuid
	 *            the masterPuid to set
	 */
	public void setMasterPuid(String masterPuid) {
		this.masterPuid = masterPuid;
	}

	/**
	 * @return the ecoFirstCompleteDate
	 */
	public String getEcoFirstCompleteDate() {
		return ecoFirstCompleteDate;
	}

	/**
	 * @param ecoFirstCompleteDate
	 *            the ecoFirstCompleteDate to set
	 */
	public void setEcoFirstCompleteDate(String ecoFirstCompleteDate) {
		this.ecoFirstCompleteDate = ecoFirstCompleteDate;
	}

	/**
	 * @return the lastFirstPeriod
	 */
	public String getLastFirstPeriod() {
		return lastFirstPeriod;
	}

	/**
	 * @param lastFirstPeriod
	 *            the lastFirstPeriod to set
	 */
	public void setLastFirstPeriod(String lastFirstPeriod) {
		this.lastFirstPeriod = lastFirstPeriod;
	}

	// /**
	// * @return the isStausWarning
	// */
	// public boolean isStausWarning() {
	// return isStausWarning;
	// }
	//
	// /**
	// * @param isStausWarning
	// * the isStausWarning to set
	// */
	// public void setStausWarning(boolean isStausWarning) {
	// this.isStausWarning = isStausWarning;
	// }

	/**
	 * @return the rowDataObj
	 */
	public EcoStatusData getRowDataObj() {
		return rowDataObj;
	}

	/**
	 * @param rowDataObj
	 *            the rowDataObj to set
	 */
	public void setRowDataObj(EcoStatusData rowDataObj) {
		this.rowDataObj = rowDataObj;
	}

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
	 * @return the gModel
	 */
	public String getgModel() {
		return gModel;
	}

	/**
	 * @param gModel
	 *            the gModel to set
	 */
	public void setgModel(String gModel) {
		this.gModel = gModel;
	}

	/**
	 * @return the statusColor
	 */
	public STATUS_COLOR getStatusColor() {
		return statusColor;
	}

	/**
	 * @param statusColor
	 *            the statusColor to set
	 */
	public void setStatusColor(STATUS_COLOR statusColor) {
		this.statusColor = statusColor;
	}

	/**
	 * @param searchChangeStatusList
	 *            the searchChangeStatusList to set
	 */
	public void setSearchChangeStatusList(EventList<EcoStatusData> searchChangeStatusList) {
		this.searchChangeStatusList = searchChangeStatusList;
	}

	/**
	 * @return the registerUserId
	 */
	public String getRegisterUserId() {
		return registerUserId;
	}

	/**
	 * @param registerUserId
	 *            the registerUserId to set
	 */
	public void setRegisterUserId(String registerUserId) {
		this.registerUserId = registerUserId;
	}

	/**
	 * table 속성 명 리스트
	 * 
	 * @return
	 */
	public static String[] getPropertyNames() {
		return new String[] { PROP_NAME_STATUS, PROP_NAME_STAGE_TYPE, PROP_NAME_PROJECT_ID, PROP_NAME_OSPECT_ID, PROP_NAME_CHANGE_DESC,
				PROP_NAME_EST_APPLY_DATE, PROP_NAME_RECEIPT_DATE, PROP_NAME_COMPLETE_REQ_DATE, PROP_NAME_EST_CHANGE_PERIOD, PROP_NAME_ECO_LAST_COMPLETE_DATE,
				PROP_NAME_REAL_CHG_REIOAD, PROP_NAME_TOTAL_REVIEWLIST, PROP_NAME_REQUIRED_ECO_LIST, PROP_NAME_IN_COMPLETE_CNT, PROP_NAME_DELAY_COMPLETE_CNT,
				PROP_NAME_MISS_COMPLETE_CNT, PROP_NAME_IN_PROC_CNT, PROP_NAME_DELAY_PROC_CNT, PROP_NAME_MISS_PROC_CNT, PROP_NAME_DESCRIPTION,
				PROP_NAME_REGISTER_DATE, PROP_NAME_SPEC_ARRANGE, PROP_NAME_MASTER_PUID, PROP_NAME_IS_STATUS_COLOR, PROP_NAME_ECO_FIRST_COMPLETE_DATE,
				PROP_NAME_ECO_LAST_FIRST_PERIOD, PROP_NAME_ROW_DATA_OBJ };
	}

	public static Map<String, String> getPropertyToLabelMap() {
		Map<String, String> propertyToLabelMap = new LinkedHashMap<String, String>();

		propertyToLabelMap.put(PROP_NAME_STATUS, "상태");
		propertyToLabelMap.put(PROP_NAME_STAGE_TYPE, "분류");
		propertyToLabelMap.put(PROP_NAME_PROJECT_ID, "Project");
		propertyToLabelMap.put(PROP_NAME_OSPECT_ID, "구분");
		propertyToLabelMap.put(PROP_NAME_CHANGE_DESC, "변경내용");
		propertyToLabelMap.put(PROP_NAME_EST_APPLY_DATE, "설계변경\n차량적용시점");
		propertyToLabelMap.put(PROP_NAME_RECEIPT_DATE, "O/SPEC\n접수일");
		// propertyToLabelMap.put(PROP_NAME_FIRST_MAIL_SEND_DATE, "설계변경\n검토요청업연\n발송일");
		propertyToLabelMap.put(PROP_NAME_COMPLETE_REQ_DATE, "ECO\n완료 요청일");
		propertyToLabelMap.put(PROP_NAME_EST_CHANGE_PERIOD, "예상\n설계변경\n기간");
		propertyToLabelMap.put(PROP_NAME_ECO_LAST_COMPLETE_DATE, "최종ECO\n처리일자");
		propertyToLabelMap.put(PROP_NAME_REAL_CHG_REIOAD, "설계변경\n기간");
		propertyToLabelMap.put(PROP_NAME_TOTAL_REVIEWLIST, "전체 검토\n리스트");
		propertyToLabelMap.put(PROP_NAME_REQUIRED_ECO_LIST, "필수 설계변경\n필요리스트");
		propertyToLabelMap.put(PROP_NAME_IN_COMPLETE_CNT, "기간내 완료");
		propertyToLabelMap.put(PROP_NAME_DELAY_COMPLETE_CNT, "지연 완료");
		propertyToLabelMap.put(PROP_NAME_MISS_COMPLETE_CNT, "누락 완료");
		propertyToLabelMap.put(PROP_NAME_IN_PROC_CNT, "기간내 진행");
		propertyToLabelMap.put(PROP_NAME_DELAY_PROC_CNT, "지연 진행");
		propertyToLabelMap.put(PROP_NAME_MISS_PROC_CNT, "누락 진행");
		propertyToLabelMap.put(PROP_NAME_DESCRIPTION, "비고");
		propertyToLabelMap.put(PROP_NAME_REGISTER_DATE, "등록일");
		propertyToLabelMap.put(PROP_NAME_SPEC_ARRANGE, "");
		propertyToLabelMap.put(PROP_NAME_MASTER_PUID, "");
		propertyToLabelMap.put(PROP_NAME_IS_STATUS_COLOR, "");
		propertyToLabelMap.put(PROP_NAME_ECO_FIRST_COMPLETE_DATE, "");
		propertyToLabelMap.put(PROP_NAME_ECO_LAST_FIRST_PERIOD, "");
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
	 * 기준정보 table 속성 명 리스트
	 * 
	 * @return
	 */
	public static String[] getStdPropertyNames() {
		return new String[] { PROP_NAME_ROW_CHECK, PROP_NAME_STATUS, PROP_NAME_STAGE_TYPE, PROP_NAME_PROJECT_ID, PROP_NAME_OSPECT_ID, PROP_NAME_CHANGE_DESC,
				PROP_NAME_EST_APPLY_DATE, PROP_NAME_RECEIPT_DATE, PROP_NAME_COMPLETE_REQ_DATE, PROP_NAME_EST_CHANGE_PERIOD, PROP_NAME_REGISTER_DATE,
				PROP_NAME_MASTER_PUID, PROP_NAME_IS_STATUS_COLOR, PROP_NAME_ROW_DATA_OBJ };
	}

	/**
	 * 기준정보 Property Name 정보
	 * 
	 * @return
	 */
	public static Map<String, String> getStdPropertyToLabelMap() {
		Map<String, String> propertyToLabelMap = new LinkedHashMap<String, String>();
		propertyToLabelMap.put(PROP_NAME_ROW_CHECK, "선택");
		propertyToLabelMap.put(PROP_NAME_STATUS, "상태");
		propertyToLabelMap.put(PROP_NAME_STAGE_TYPE, "분류");
		propertyToLabelMap.put(PROP_NAME_PROJECT_ID, "Project");
		propertyToLabelMap.put(PROP_NAME_OSPECT_ID, "구분(O/Spec)");
		propertyToLabelMap.put(PROP_NAME_CHANGE_DESC, "변경내용");
		propertyToLabelMap.put(PROP_NAME_EST_APPLY_DATE, "설계변경\n차량적용시점");
		propertyToLabelMap.put(PROP_NAME_RECEIPT_DATE, "O/SPEC\n접수일");
		propertyToLabelMap.put(PROP_NAME_COMPLETE_REQ_DATE, "ECO\n완료 요청일");
		propertyToLabelMap.put(PROP_NAME_EST_CHANGE_PERIOD, "예상 설계변경\n기간");
		propertyToLabelMap.put(PROP_NAME_REGISTER_DATE, "등록일");
		propertyToLabelMap.put(PROP_NAME_MASTER_PUID, "");
		propertyToLabelMap.put(PROP_NAME_IS_STATUS_COLOR, "");
		propertyToLabelMap.put(PROP_NAME_ROW_DATA_OBJ, "");

		return propertyToLabelMap;
	}

	/**
	 * 기준정보 테이블 Property 명을 가져옴
	 * 
	 * @return
	 */
	public static List<String> getStdPropertyNamesAsList() {
		return Arrays.asList(getStdPropertyNames());
	}

	/**
	 * 기준정보 속성의 Column Index
	 * 
	 * @param propertyName
	 * @return
	 */
	public static int getStdColumnIndexOfProperty(String propertyName) {
		return getStdPropertyNamesAsList().indexOf(propertyName);
	}

}
