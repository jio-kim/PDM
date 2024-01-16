package com.ssangyong.commands.ec.ecostatus.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.ssangyong.common.utils.PreferenceService;
import com.teamcenter.rac.kernel.TCPreferenceService;

import ca.odell.glazedlists.EventList;

/**
 * 변경리스트 Row Data
 * 
 * @author baek
 * 
 */
public class EcoChangeData implements Cloneable {

	private boolean rowCheck = false;
	private String groupSeqNo = null; // 순번
	private String registerType = null; // 등록구분
	private String category = null; // Option Category
	private String creationDate = null; // 등록일
	private String ecoPublish = null; // ECO 발행
	private String changeStatus = null; // 상태
	private String engineFlag = null; // 엔진구분
	private String functionNo = null; // Function No
	private String partName = null; // Part Name
	private String projectId = null; // Project
	private String ospecId = null; // 구분(O/Spec)
	private String changeDesc = null; // 변경내용
	private String reviewContents = null; // 변경검토내용
	private String systemNo = null; // 시스템
	private String userId = null; // 담당자 ID
	private String teamName = null; // 팀명
	private String mailStatus = null; // 메일발송
	private String ecoNo = null; // ECO NO
	private String ecoCompleteDate = null; // ECO 완료일
	private String description = null; // 비고
	private String masterPuid = null; // 기준정보 Unique Key
	private String rowChangeType = null; // ROW 변경 유형
	private String opCategoryPuid = null; // Option Category Puid
	private String changeListPuid = null; // 변경리스트 PUID
	private EcoChangeData rowDataObj = null; // 현재 Row 모든 정보(변경되면 바뀜)
	private EcoChangeData rowInitDataObj = null; // 현재 Row 초기 정보(변경되면 안바뀜)

	private String gModel = null; // GMODEL
	private String userName = null; // 담당자 명
	private String vOrEType = null; // V/E 타입
	private String registerId = null; // 등록자 ID
	private StdInformData stdInformData = null; // 기준정보 Data
	private ChangeReviewData changeReviewData = null; // 설계변경 검토 Data

	private EventList<EcoChangeData> searchEcoChangeList; // 설계변경리스트 테이블 데이터 리스트

	private ArrayList<EcoChangeData> deleteDataList = null; // 설계변경리스트에 삭제되는 데이터 리스트

	public static String PROP_NAME_ROW_CHECK = "rowCheck";// 행선택
	public static String PROP_NAME_GROUP_SEQ_NO = "groupSeqNo";// 순번
	public static String PROP_NAME_REGISTER_TYPE = "registerType";// 등록구분
	public static String PROP_NAME_CATEGORY = "category";// Option Category
	public static String PROP_NAME_CREATION_DATE = "creationDate";// 등록일
	public static String PROP_NAME_ECO_PUBLISH = "ecoPublish";// ECO 발행
	public static String PROP_NAME_CHANGE_STATUS = "changeStatus";// 상태
	public static String PROP_NAME_ENGINE_FLAG = "engineFlag";// 엔진구분
	public static String PROP_NAME_FUNCTION_NO = "functionNo";// Function No
	public static String PROP_NAME_PART_NAME = "partName";// Part Name
	public static String PROP_NAME_PROJECT_ID = "projectId";// Project
	public static String PROP_NAME_OSPECT_ID = "ospecId";// 구분(O/Spec)
	public static String PROP_NAME_CHANGE_DESC = "changeDesc";// 변경내용
	public static String PROP_NAME_REVIEW_CONTENTS = "reviewContents";// 변경검토내용
	public static String PROP_NAME_SYSTEM_NO = "systemNo";// 시스템
	public static String PROP_NAME_USER_ID = "userId";// 담당자 ID
	public static String PROP_NAME_TEAM_NAME = "teamName";// 팀명
	public static String PROP_NAME_MAIL_STATUS = "mailStatus";// 메일발송
	public static String PROP_NAME_ECO_NO = "ecoNo";// ECO NO
	public static String PROP_NAME_ECO_COMPLETE_DATE = "ecoCompleteDate";// ECO 완료일
	public static String PROP_NAME_DESCRIPTION = "description";// 비고
	public static String PROP_NAME_MASTER_PUID = "masterPuid";// 기준정보 Unique Key
	public static String PROP_NAME_ROW_DATA_OBJ = "rowDataObj";// 현재 Row 의 모든 정보
	public static String PROP_NAME_ROW_INIT_DATA_OBJ = "rowInitDataObj";// 현재 Row 의 초기 모든 정보
	public static String PROP_NAME_ROW_CHANGE_TYPE = "rowChangeType";// 현재 Row 의 변경상태
	public static String PROP_NAME_ROW_CHG_LIST_PUID = "changeListPuid";// 변경리스트 PUID

	public static final String CHECK_BOX_CONFIG_LABEL = "checkBox";
	public static final String CHECK_BOX_EDITOR_CONFIG_LABEL = "checkBoxEditor";

	public static final String EDITABLE_CONFIG_LABEL = "editableCellConfigLabel"; // Cell 수정가능한 라벨
	public static final String COMBO_ECO_PUBLISH_CONFIG_LABEL = "ComboEcoPublishConfigLabel"; // EcoPublish Combo box 라벨
	public static final String COMBO_SYSTEM_CONFIG_LABEL = "SystemCodePublishConfigLabel"; // System Combo box 라벨

	public static final String ALIGN_CELL_CONTENTS_CENTER_CONFIG_LABEL = "alignCellContentsLeftConfigLabel"; // 중간으로 정렬

	public static final String CELL_FRG_RED_COLOR_LABLEL = "CELL_FRG_RED_COLOR_LABLEL"; // Warning Foreground Color Row Cell 라벨
	public static final String CELL_BG_GREEN_COLOR_LABLEL = "CELL_BG_GREEN_COLOR_LABLEL"; // Cell 추가 될때 사용되는 Background Color Row Cell 라벨
	public static final String CELL_BG_RED_COLOR_LABLEL = "CELL_BG_RED_COLOR_LABLEL"; // Cell 삭제 될때 사용되는 Background Color Row Cell 라벨
	public static final String CELL_CHANGE_STATUS_LABLEL = "CELL_CHANGE_STATUS_LABLEL"; // 상태 Row Cell 라벨

	public static final String CELL_EDITABLE_RULE_APPLY_LABLEL = "CELL_EDITABLE_RULE_APPLY_LABLEL"; // Cell Read/Write 설정이 적용된 Cell 라벨
	public static final String CELL_USER_RIGHT_EDITABLE_RULE_LABEL = "CELL_USER_RIGHT_EDITABLE_RULE_LABEL"; // Cell 사용자 권한 체크로 Read/Write 설정이 적용된 Cell 라벨

	public static final String TEMPLATE_DS_ECO_RPT = "ssangyong_eco_status_change_list_template.xlsx"; // 업로드 Template 명

	public static final String[] DESC_STATUS = new String[] { "ALL", "누락/오류 진행중", "지연 진행중", "기간내 진행중", "누락/오류 완료", "지연 완료", "기간내 완료", "사양정리" };

	public static final String ROW_CHANGE_TYPE_NONE = "NONE";
	public static final String ROW_CHANGE_TYPE_NEW = "NEW";
	public static final String ROW_CHANGE_TYPE_REMOVE = "REMOVE";
	public static final String ROW_CHANGE_TYPE_MODIFY = "MODIFY";
	public static final String ROW_CHANGE_TYPE_ALL_NEW = "ALL_NEW"; // 변경 관리 리스트 생성

	// 변경 관리 리스트 생성중인지 여부

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
	 * @return the groupSeqNo
	 */
	public String getGroupSeqNo() {
		return groupSeqNo;
	}

	/**
	 * @param groupSeqNo
	 *            the groupSeqNo to set
	 */
	public void setGroupSeqNo(String groupSeqNo) {
		this.groupSeqNo = groupSeqNo;
	}

	/**
	 * @return the registerType
	 */
	public String getRegisterType() {
		return registerType;
	}

	/**
	 * @param registerType
	 *            the registerType to set
	 */
	public void setRegisterType(String registerType) {
		this.registerType = registerType;
	}

	/**
	 * @return the category
	 */
	public String getCategory() {
		return category;
	}

	/**
	 * @param category
	 *            the category to set
	 */
	public void setCategory(String category) {
		this.category = category;
	}

	/**
	 * @return the creationDate
	 */
	public String getCreationDate() {
		return creationDate;
	}

	/**
	 * @param creationDate
	 *            the creationDate to set
	 */
	public void setCreationDate(String creationDate) {
		this.creationDate = creationDate;
	}

	/**
	 * @return the ecoPublish
	 */
	public String getEcoPublish() {
		return ecoPublish;
	}

	/**
	 * @param ecoPublish
	 *            the ecoPublish to set
	 */
	public void setEcoPublish(String ecoPublish) {
		this.ecoPublish = ecoPublish;
	}

	/**
	 * @return the changeStatus
	 */
	public String getChangeStatus() {
		return changeStatus;
	}

	/**
	 * @param changeStatus
	 *            the changeStatus to set
	 */
	public void setChangeStatus(String changeStatus) {
		this.changeStatus = changeStatus;
	}

	/**
	 * @return the engineFlag
	 */
	public String getEngineFlag() {
		return engineFlag;
	}

	/**
	 * @param engineFlag
	 *            the engineFlag to set
	 */
	public void setEngineFlag(String engineFlag) {
		this.engineFlag = engineFlag;
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
	 * @return the partName
	 */
	public String getPartName() {
		return partName;
	}

	/**
	 * @param partName
	 *            the partName to set
	 */
	public void setPartName(String partName) {
		this.partName = partName;
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
	 * @return the reviewContents
	 */
	public String getReviewContents() {
		return reviewContents;
	}

	/**
	 * @param reviewContents
	 *            the reviewContents to set
	 */
	public void setReviewContents(String reviewContents) {
		this.reviewContents = reviewContents;
	}

	/**
	 * @return the systemNo
	 */
	public String getSystemNo() {
		return systemNo;
	}

	/**
	 * @param systemNo
	 *            the systemNo to set
	 */
	public void setSystemNo(String systemNo) {
		this.systemNo = systemNo;
	}

	/**
	 * @return the userId
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 * @param userId
	 *            the userId to set
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}

	/**
	 * @return the teamName
	 */
	public String getTeamName() {
		return teamName;
	}

	/**
	 * @param teamName
	 *            the teamName to set
	 */
	public void setTeamName(String teamName) {
		this.teamName = teamName;
	}

	/**
	 * @return the mailStatus
	 */
	public String getMailStatus() {
		return mailStatus;
	}

	/**
	 * @param mailStatus
	 *            the mailStatus to set
	 */
	public void setMailStatus(String mailStatus) {
		this.mailStatus = mailStatus;
	}

	/**
	 * @return the ecoNo
	 */
	public String getEcoNo() {
		return ecoNo;
	}

	/**
	 * @param ecoNo
	 *            the ecoNo to set
	 */
	public void setEcoNo(String ecoNo) {
		this.ecoNo = ecoNo;
	}

	/**
	 * @return the ecoCompleteDate
	 */
	public String getEcoCompleteDate() {
		return ecoCompleteDate;
	}

	/**
	 * @param ecoCompleteDate
	 *            the ecoCompleteDate to set
	 */
	public void setEcoCompleteDate(String ecoCompleteDate) {
		this.ecoCompleteDate = ecoCompleteDate;
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
	 * @return the rowChangeType
	 */
	public String getRowChangeType() {
		return rowChangeType;
	}

	/**
	 * @param rowChangeType
	 *            the rowChangeType to set
	 */
	public void setRowChangeType(String rowChangeType) {
		this.rowChangeType = rowChangeType;
	}

	/**
	 * @return the rowDataObj
	 */
	public EcoChangeData getRowDataObj() {
		return rowDataObj;
	}

	/**
	 * @param rowDataObj
	 *            the rowDataObj to set
	 */
	public void setRowDataObj(EcoChangeData rowDataObj) {
		this.rowDataObj = rowDataObj;
	}

	/**
	 * @return the rowInitDataObj
	 */
	public EcoChangeData getRowInitDataObj() {
		return rowInitDataObj;
	}

	/**
	 * @param rowInitDataObj
	 *            the rowInitDataObj to set
	 */
	public void setRowInitDataObj(EcoChangeData rowInitDataObj) {
		this.rowInitDataObj = rowInitDataObj;
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
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * @param userName
	 *            the userName to set
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * @return the vOrEType
	 */
	public String getvOrEType() {
		return vOrEType;
	}

	/**
	 * @param vOrEType
	 *            the vOrEType to set
	 */
	public void setvOrEType(String vOrEType) {
		this.vOrEType = vOrEType;
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
	 * @return the stdInformData
	 */
	public StdInformData getStdInformData() {
		return stdInformData;
	}

	/**
	 * @param stdInformData
	 *            the stdInformData to set
	 */
	public void setStdInformData(StdInformData stdInformData) {
		this.stdInformData = stdInformData;
	}

	/**
	 * @return the changeReviewData
	 */
	public ChangeReviewData getChangeReviewData() {
		return changeReviewData;
	}

	/**
	 * @param changeReviewData
	 *            the changeReviewData to set
	 */
	public void setChangeReviewData(ChangeReviewData changeReviewData) {
		this.changeReviewData = changeReviewData;
	}

	/**
	 * @return the searchEcoChangeList
	 */
	public EventList<EcoChangeData> getSearchEcoChangeList() {
		return searchEcoChangeList;
	}

	/**
	 * @param searchEcoChangeList
	 *            the searchEcoChangeList to set
	 */
	public void setSearchEcoChangeList(EventList<EcoChangeData> searchEcoChangeList) {
		this.searchEcoChangeList = searchEcoChangeList;
	}

	/**
	 * @return the opCategoryPuid
	 */
	public String getOpCategoryPuid() {
		return opCategoryPuid;
	}

	/**
	 * @param opCategoryPuid
	 *            the opCategoryPuid to set
	 */
	public void setOpCategoryPuid(String opCategoryPuid) {
		this.opCategoryPuid = opCategoryPuid;
	}

	/**
	 * @return the changeListPuid
	 */
	public String getChangeListPuid() {
		return changeListPuid;
	}

	/**
	 * @param changeListPuid
	 *            the changeListPuid to set
	 */
	public void setChangeListPuid(String changeListPuid) {
		this.changeListPuid = changeListPuid;
	}

	/**
	 * @return the removeDataList
	 */
	public ArrayList<EcoChangeData> getDeleteDataList() {
		return deleteDataList;
	}

	/**
	 * @param deleteDataList
	 *            the removeDataList to set
	 */
	public void setDeleteDataList(ArrayList<EcoChangeData> deleteDataList) {
		this.deleteDataList = deleteDataList;
	}

	/**
	 * table 속성 명 리스트
	 * 
	 * @return
	 */
	public static String[] getPropertyNames() {
		return new String[] { PROP_NAME_ROW_CHECK, PROP_NAME_GROUP_SEQ_NO, PROP_NAME_PROJECT_ID, PROP_NAME_OSPECT_ID, PROP_NAME_CHANGE_DESC,
				PROP_NAME_REGISTER_TYPE, PROP_NAME_CREATION_DATE, PROP_NAME_ECO_PUBLISH, PROP_NAME_CHANGE_STATUS, PROP_NAME_ENGINE_FLAG, PROP_NAME_FUNCTION_NO,
				PROP_NAME_PART_NAME, PROP_NAME_REVIEW_CONTENTS, PROP_NAME_CATEGORY, PROP_NAME_SYSTEM_NO, PROP_NAME_USER_ID, PROP_NAME_TEAM_NAME,
				PROP_NAME_MAIL_STATUS, PROP_NAME_ECO_NO, PROP_NAME_ECO_COMPLETE_DATE, PROP_NAME_DESCRIPTION, PROP_NAME_MASTER_PUID, PROP_NAME_ROW_DATA_OBJ,
				PROP_NAME_ROW_INIT_DATA_OBJ, PROP_NAME_ROW_CHANGE_TYPE, PROP_NAME_ROW_CHG_LIST_PUID };
	}

	public static Map<String, String> getPropertyToLabelMap() {
		Map<String, String> propertyToLabelMap = new LinkedHashMap<String, String>();
		propertyToLabelMap.put(PROP_NAME_ROW_CHECK, "");
		propertyToLabelMap.put(PROP_NAME_GROUP_SEQ_NO, "순번");
		propertyToLabelMap.put(PROP_NAME_PROJECT_ID, "Project");
		propertyToLabelMap.put(PROP_NAME_OSPECT_ID, "구분(O/Spec)");
		propertyToLabelMap.put(PROP_NAME_CHANGE_DESC, "변경내용");
		propertyToLabelMap.put(PROP_NAME_REGISTER_TYPE, "등록구분");
		propertyToLabelMap.put(PROP_NAME_CREATION_DATE, "등록일");
		propertyToLabelMap.put(PROP_NAME_ECO_PUBLISH, "ECO 발행\n(*)");
		propertyToLabelMap.put(PROP_NAME_CHANGE_STATUS, "상태");
		propertyToLabelMap.put(PROP_NAME_ENGINE_FLAG, "V/E");
		propertyToLabelMap.put(PROP_NAME_FUNCTION_NO, "Function No\n(*)");
		propertyToLabelMap.put(PROP_NAME_PART_NAME, "Part Name\n(*)");
		propertyToLabelMap.put(PROP_NAME_REVIEW_CONTENTS, "변경검토내용\n(*)");
		propertyToLabelMap.put(PROP_NAME_CATEGORY, "Option\nCategory");
		propertyToLabelMap.put(PROP_NAME_SYSTEM_NO, "시스템");
		propertyToLabelMap.put(PROP_NAME_USER_ID, "담당자 ID\n(*)");
		propertyToLabelMap.put(PROP_NAME_TEAM_NAME, "팀명\n(*)");
		propertyToLabelMap.put(PROP_NAME_MAIL_STATUS, "메일발송");
		propertyToLabelMap.put(PROP_NAME_ECO_NO, "ECO NO");
		propertyToLabelMap.put(PROP_NAME_ECO_COMPLETE_DATE, "처리일시");
		propertyToLabelMap.put(PROP_NAME_DESCRIPTION, "비고");
		propertyToLabelMap.put(PROP_NAME_MASTER_PUID, "");
		propertyToLabelMap.put(PROP_NAME_ROW_DATA_OBJ, "");
		propertyToLabelMap.put(PROP_NAME_ROW_INIT_DATA_OBJ, "");
		propertyToLabelMap.put(PROP_NAME_ROW_CHANGE_TYPE, "");
		propertyToLabelMap.put(PROP_NAME_ROW_CHG_LIST_PUID, "");

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
	 * Engine 타입 가져옴
	 * 
	 * @param teamName
	 * @return
	 */
	public static String getEngineType(String teamName) {

		if (teamName == null || teamName.isEmpty())
			return "";
		// Engine 팀리스트
		// 20220603 POWERTRAIN DEVELOPMENT,EMS APPLICATIION 팀명 변경으로 수정
		// 하드코딩된 엔진팀리스트를 Preference로 변경함.
		ArrayList<String> engineTeamList = new ArrayList<String>();
		String[] engineTeams = PreferenceService.getValues(TCPreferenceService.TC_preference_site, "EngineTeamList");
		for(String engineTeam : engineTeams){
			engineTeamList.add(engineTeam);
		}
//		ArrayList<String> engineTeamList = new ArrayList<String>(Arrays.asList("DIESEL EMS APPLICATION", "GASOLINE EMS APPLICATION", "GASOLINE ENGINE DESIGN",
//				"DIESEL ENGINE DESIGN", "T/M DESIGN","EMS APPLICATIION","HYBRIDE DEVELOPMENT","POWERTRAIN DEVELOPMENT"));

		if (engineTeamList.contains(teamName))
			return "E";
		else if (teamName.equalsIgnoreCase("dba"))
			return "";
		else
			return "V";
	}

	/**
	 * 기준 정보 Data (Template Upload 시 사용)
	 * 
	 * @author baek
	 * 
	 */
	public static class StdInformData {
		private String stageType = ""; // 분류
		private String projectId = ""; // Project
		private String ospecId = ""; // 구분(O/Spec)
		private String changeDesc = ""; // 변경내용
		private String applyDate = null; // 예상적용시점
		private String receiptDate = null; // O/SPECT 접수일
		private String ecoCompleteReqDate = ""; // ECO 완료 요청일
		private String registerType = ""; // 등록구분
		private String createDate = null; // 등록일
		private String description = ""; // 비고
		private String masterPuid = ""; // master puid
		private String registerId = ""; // 등록자 ID

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
		 * @return the applyDate
		 */
		public String getApplyDate() {
			return applyDate;
		}

		/**
		 * @param applyDate
		 *            the applyDate to set
		 */
		public void setApplyDate(String applyDate) {
			this.applyDate = applyDate;
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
		 * @return the registerType
		 */
		public String getRegisterType() {
			return registerType;
		}

		/**
		 * @param registerType
		 *            the registerType to set
		 */
		public void setRegisterType(String registerType) {
			this.registerType = registerType;
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

		@Override
		public String toString() {
			return stageType + "~~" + projectId + "~~" + ospecId + "~~" + masterPuid;
		}

	}

	/**
	 * 변경검토 Data
	 * 
	 * @author baek
	 * 
	 */
	public static class ChangeReviewData {
		private StdInformData stdInformData = null; // 기준정보
		private String category = null; // Option Category
		private String reviewContents = null; // 검토결과
		private String reviewPuid = null; // Review PUID
		private String masterPuid = null; // Master PUID

		public ChangeReviewData(StdInformData stdInformData, String category, String reviewContents, String reviewPuid) {
			this.stdInformData = stdInformData;
			this.category = category;
			this.reviewContents = reviewContents;
			this.reviewPuid = reviewPuid;
		}

		public ChangeReviewData(String masterPuid, String category, String reviewContents, String reviewPuid) {
			this.masterPuid = masterPuid;
			this.category = category;
			this.reviewContents = reviewContents;
			this.reviewPuid = reviewPuid;
		}

		/**
		 * @return the stdInformData
		 */
		public StdInformData getStdInformData() {
			return stdInformData;
		}

		/**
		 * @return the category
		 */
		public String getCategory() {
			return category;
		}

		/**
		 * @return the reviewContents
		 */
		public String getReviewContents() {
			return reviewContents;
		}

		/**
		 * @return the reviewPuid
		 */
		public String getReviewPuid() {
			return reviewPuid;
		}

		/**
		 * @param reviewPuid
		 *            the reviewPuid to set
		 */
		public void setReviewPuid(String reviewPuid) {
			this.reviewPuid = reviewPuid;
		}

		/**
		 * @return the masterPuid
		 */
		public String getMasterPuid() {
			return masterPuid;
		}

		@Override
		public String toString() {
			return category + "~~" + reviewContents + "~~" + reviewPuid + "~~"
					+ (stdInformData != null ? stdInformData : (masterPuid != null ? masterPuid : ""));
		}
	}

	/**
	 * 옵션 Condition 정보리스트
	 * 
	 * @author baek
	 * 
	 */
	public static class ChangeOptCondition {
		private ChangeReviewData reviewData = null;
		private String optionCode = null;
		private String optionCondition = null;

		public ChangeOptCondition(ChangeReviewData reviewData, String optionCode, String optionCondition) {
			this.optionCode = optionCode;
			this.optionCondition = optionCondition;
		}

		/**
		 * @return the reviewData
		 */
		public ChangeReviewData getReviewData() {
			return reviewData;
		}

		/**
		 * @return the optionCode
		 */
		public String getOptionCode() {
			return optionCode;
		}

		/**
		 * @return the optionCondition
		 */
		public String getOptionCondition() {
			return optionCondition;
		}

	}

	@Override
	public Object clone() {
		Object obj = null;
		try {
			obj = super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return obj;
	}

	public static String objToStr(Object obj) {
		return obj != null ? (String) obj : "";
	}

	public static int objToInt(Object obj) {
		return obj != null ? ((BigDecimal) obj).intValue() : 0;
	}

}
