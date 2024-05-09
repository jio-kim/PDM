package com.kgm.commands.ec.ecostatus.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.kgm.common.utils.PreferenceService;
import com.teamcenter.rac.kernel.TCPreferenceService;

import ca.odell.glazedlists.EventList;

/**
 * ���渮��Ʈ Row Data
 * 
 * @author baek
 * 
 */
public class EcoChangeData implements Cloneable {

	private boolean rowCheck = false;
	private String groupSeqNo = null; // ����
	private String registerType = null; // ��ϱ���
	private String category = null; // Option Category
	private String creationDate = null; // �����
	private String ecoPublish = null; // ECO ����
	private String changeStatus = null; // ����
	private String engineFlag = null; // ��������
	private String functionNo = null; // Function No
	private String partName = null; // Part Name
	private String projectId = null; // Project
	private String ospecId = null; // ����(O/Spec)
	private String changeDesc = null; // ���泻��
	private String reviewContents = null; // ������䳻��
	private String systemNo = null; // �ý���
	private String userId = null; // ����� ID
	private String teamName = null; // ����
	private String mailStatus = null; // ���Ϲ߼�
	private String ecoNo = null; // ECO NO
	private String ecoCompleteDate = null; // ECO �Ϸ���
	private String description = null; // ���
	private String masterPuid = null; // �������� Unique Key
	private String rowChangeType = null; // ROW ���� ����
	private String opCategoryPuid = null; // Option Category Puid
	private String changeListPuid = null; // ���渮��Ʈ PUID
	private EcoChangeData rowDataObj = null; // ���� Row ��� ����(����Ǹ� �ٲ�)
	private EcoChangeData rowInitDataObj = null; // ���� Row �ʱ� ����(����Ǹ� �ȹٲ�)

	private String gModel = null; // GMODEL
	private String userName = null; // ����� ��
	private String vOrEType = null; // V/E Ÿ��
	private String registerId = null; // ����� ID
	private StdInformData stdInformData = null; // �������� Data
	private ChangeReviewData changeReviewData = null; // ���躯�� ���� Data

	private EventList<EcoChangeData> searchEcoChangeList; // ���躯�渮��Ʈ ���̺� ������ ����Ʈ

	private ArrayList<EcoChangeData> deleteDataList = null; // ���躯�渮��Ʈ�� �����Ǵ� ������ ����Ʈ

	public static String PROP_NAME_ROW_CHECK = "rowCheck";// �༱��
	public static String PROP_NAME_GROUP_SEQ_NO = "groupSeqNo";// ����
	public static String PROP_NAME_REGISTER_TYPE = "registerType";// ��ϱ���
	public static String PROP_NAME_CATEGORY = "category";// Option Category
	public static String PROP_NAME_CREATION_DATE = "creationDate";// �����
	public static String PROP_NAME_ECO_PUBLISH = "ecoPublish";// ECO ����
	public static String PROP_NAME_CHANGE_STATUS = "changeStatus";// ����
	public static String PROP_NAME_ENGINE_FLAG = "engineFlag";// ��������
	public static String PROP_NAME_FUNCTION_NO = "functionNo";// Function No
	public static String PROP_NAME_PART_NAME = "partName";// Part Name
	public static String PROP_NAME_PROJECT_ID = "projectId";// Project
	public static String PROP_NAME_OSPECT_ID = "ospecId";// ����(O/Spec)
	public static String PROP_NAME_CHANGE_DESC = "changeDesc";// ���泻��
	public static String PROP_NAME_REVIEW_CONTENTS = "reviewContents";// ������䳻��
	public static String PROP_NAME_SYSTEM_NO = "systemNo";// �ý���
	public static String PROP_NAME_USER_ID = "userId";// ����� ID
	public static String PROP_NAME_TEAM_NAME = "teamName";// ����
	public static String PROP_NAME_MAIL_STATUS = "mailStatus";// ���Ϲ߼�
	public static String PROP_NAME_ECO_NO = "ecoNo";// ECO NO
	public static String PROP_NAME_ECO_COMPLETE_DATE = "ecoCompleteDate";// ECO �Ϸ���
	public static String PROP_NAME_DESCRIPTION = "description";// ���
	public static String PROP_NAME_MASTER_PUID = "masterPuid";// �������� Unique Key
	public static String PROP_NAME_ROW_DATA_OBJ = "rowDataObj";// ���� Row �� ��� ����
	public static String PROP_NAME_ROW_INIT_DATA_OBJ = "rowInitDataObj";// ���� Row �� �ʱ� ��� ����
	public static String PROP_NAME_ROW_CHANGE_TYPE = "rowChangeType";// ���� Row �� �������
	public static String PROP_NAME_ROW_CHG_LIST_PUID = "changeListPuid";// ���渮��Ʈ PUID

	public static final String CHECK_BOX_CONFIG_LABEL = "checkBox";
	public static final String CHECK_BOX_EDITOR_CONFIG_LABEL = "checkBoxEditor";

	public static final String EDITABLE_CONFIG_LABEL = "editableCellConfigLabel"; // Cell ���������� ��
	public static final String COMBO_ECO_PUBLISH_CONFIG_LABEL = "ComboEcoPublishConfigLabel"; // EcoPublish Combo box ��
	public static final String COMBO_SYSTEM_CONFIG_LABEL = "SystemCodePublishConfigLabel"; // System Combo box ��

	public static final String ALIGN_CELL_CONTENTS_CENTER_CONFIG_LABEL = "alignCellContentsLeftConfigLabel"; // �߰����� ����

	public static final String CELL_FRG_RED_COLOR_LABLEL = "CELL_FRG_RED_COLOR_LABLEL"; // Warning Foreground Color Row Cell ��
	public static final String CELL_BG_GREEN_COLOR_LABLEL = "CELL_BG_GREEN_COLOR_LABLEL"; // Cell �߰� �ɶ� ���Ǵ� Background Color Row Cell ��
	public static final String CELL_BG_RED_COLOR_LABLEL = "CELL_BG_RED_COLOR_LABLEL"; // Cell ���� �ɶ� ���Ǵ� Background Color Row Cell ��
	public static final String CELL_CHANGE_STATUS_LABLEL = "CELL_CHANGE_STATUS_LABLEL"; // ���� Row Cell ��

	public static final String CELL_EDITABLE_RULE_APPLY_LABLEL = "CELL_EDITABLE_RULE_APPLY_LABLEL"; // Cell Read/Write ������ ����� Cell ��
	public static final String CELL_USER_RIGHT_EDITABLE_RULE_LABEL = "CELL_USER_RIGHT_EDITABLE_RULE_LABEL"; // Cell ����� ���� üũ�� Read/Write ������ ����� Cell ��

	public static final String TEMPLATE_DS_ECO_RPT = "ssangyong_eco_status_change_list_template.xlsx"; // ���ε� Template ��

	public static final String[] DESC_STATUS = new String[] { "ALL", "����/���� ������", "���� ������", "�Ⱓ�� ������", "����/���� �Ϸ�", "���� �Ϸ�", "�Ⱓ�� �Ϸ�", "�������" };

	public static final String ROW_CHANGE_TYPE_NONE = "NONE";
	public static final String ROW_CHANGE_TYPE_NEW = "NEW";
	public static final String ROW_CHANGE_TYPE_REMOVE = "REMOVE";
	public static final String ROW_CHANGE_TYPE_MODIFY = "MODIFY";
	public static final String ROW_CHANGE_TYPE_ALL_NEW = "ALL_NEW"; // ���� ���� ����Ʈ ����

	// ���� ���� ����Ʈ ���������� ����

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
	 * table �Ӽ� �� ����Ʈ
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
		propertyToLabelMap.put(PROP_NAME_GROUP_SEQ_NO, "����");
		propertyToLabelMap.put(PROP_NAME_PROJECT_ID, "Project");
		propertyToLabelMap.put(PROP_NAME_OSPECT_ID, "����(O/Spec)");
		propertyToLabelMap.put(PROP_NAME_CHANGE_DESC, "���泻��");
		propertyToLabelMap.put(PROP_NAME_REGISTER_TYPE, "��ϱ���");
		propertyToLabelMap.put(PROP_NAME_CREATION_DATE, "�����");
		propertyToLabelMap.put(PROP_NAME_ECO_PUBLISH, "ECO ����\n(*)");
		propertyToLabelMap.put(PROP_NAME_CHANGE_STATUS, "����");
		propertyToLabelMap.put(PROP_NAME_ENGINE_FLAG, "V/E");
		propertyToLabelMap.put(PROP_NAME_FUNCTION_NO, "Function No\n(*)");
		propertyToLabelMap.put(PROP_NAME_PART_NAME, "Part Name\n(*)");
		propertyToLabelMap.put(PROP_NAME_REVIEW_CONTENTS, "������䳻��\n(*)");
		propertyToLabelMap.put(PROP_NAME_CATEGORY, "Option\nCategory");
		propertyToLabelMap.put(PROP_NAME_SYSTEM_NO, "�ý���");
		propertyToLabelMap.put(PROP_NAME_USER_ID, "����� ID\n(*)");
		propertyToLabelMap.put(PROP_NAME_TEAM_NAME, "����\n(*)");
		propertyToLabelMap.put(PROP_NAME_MAIL_STATUS, "���Ϲ߼�");
		propertyToLabelMap.put(PROP_NAME_ECO_NO, "ECO NO");
		propertyToLabelMap.put(PROP_NAME_ECO_COMPLETE_DATE, "ó���Ͻ�");
		propertyToLabelMap.put(PROP_NAME_DESCRIPTION, "���");
		propertyToLabelMap.put(PROP_NAME_MASTER_PUID, "");
		propertyToLabelMap.put(PROP_NAME_ROW_DATA_OBJ, "");
		propertyToLabelMap.put(PROP_NAME_ROW_INIT_DATA_OBJ, "");
		propertyToLabelMap.put(PROP_NAME_ROW_CHANGE_TYPE, "");
		propertyToLabelMap.put(PROP_NAME_ROW_CHG_LIST_PUID, "");

		return propertyToLabelMap;
	}

	/**
	 * ���̺� Property ���� ������
	 * 
	 * @return
	 */
	public static List<String> getPropertyNamesAsList() {
		return Arrays.asList(getPropertyNames());
	}

	/**
	 * �Ӽ��� Column Index
	 * 
	 * @param propertyName
	 * @return
	 */
	public static int getColumnIndexOfProperty(String propertyName) {
		return getPropertyNamesAsList().indexOf(propertyName);
	}

	/**
	 * Engine Ÿ�� ������
	 * 
	 * @param teamName
	 * @return
	 */
	public static String getEngineType(String teamName) {

		if (teamName == null || teamName.isEmpty())
			return "";
		// Engine ������Ʈ
		// 20220603 POWERTRAIN DEVELOPMENT,EMS APPLICATIION ���� �������� ����
		// �ϵ��ڵ��� ����������Ʈ�� Preference�� ������.
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
	 * ���� ���� Data (Template Upload �� ���)
	 * 
	 * @author baek
	 * 
	 */
	public static class StdInformData {
		private String stageType = ""; // �з�
		private String projectId = ""; // Project
		private String ospecId = ""; // ����(O/Spec)
		private String changeDesc = ""; // ���泻��
		private String applyDate = null; // �����������
		private String receiptDate = null; // O/SPECT ������
		private String ecoCompleteReqDate = ""; // ECO �Ϸ� ��û��
		private String registerType = ""; // ��ϱ���
		private String createDate = null; // �����
		private String description = ""; // ���
		private String masterPuid = ""; // master puid
		private String registerId = ""; // ����� ID

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
	 * ������� Data
	 * 
	 * @author baek
	 * 
	 */
	public static class ChangeReviewData {
		private StdInformData stdInformData = null; // ��������
		private String category = null; // Option Category
		private String reviewContents = null; // ������
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
	 * �ɼ� Condition ��������Ʈ
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
