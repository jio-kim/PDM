package com.kgm.commands.ec.ecostatus.model;

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ca.odell.glazedlists.EventList;

/**
 * ���� ������Ȳ ����Ʈ Data
 */
public class EcoStatusData {

	private String status = null; // ����
	private String stageType = null; // �з�
	private String projectId = null; // Project
	private String ospecId = null; // ����
	private String changeDesc = null; // ���泻��
	private String estApplyDate = null; // �����������
	private String receiptDate = null; // O/SPECT ������
	private String firstMailSendDate = null; // EPL�����û ��������/���Ϲ߼�����
	private String ecoCompleteReqDate = null; // ECO �Ϸ� ��û��
	private String estChangePeriod = null; // ���� ���躯�� �Ⱓ
	private String ecoLastCompleteDate = null; // ����ECOó������
	private String realChangePeriod = null; // ���� ���躯�� �Ⱓ
	private int totalReviewList = 0; // ��ü ���� ����Ʈ
	private int requiredEcoList = 0; // �ʼ� ���躯�� �ʿ丮��Ʈ
	private int inCompleteCnt = 0; // �Ⱓ�� �Ϸ�
	private int delayCompleteCnt = 0; // �����Ϸ�
	private int missCompleteCnt = 0; // �����Ϸ�
	private int inProcessCnt = 0; // �Ⱓ�� ����
	private int delayProcessCnt = 0; // ���� ����
	private int missProcess = 0; // ���� ����
	private String registerDate = null; // �����
	private String registerUserId = null; // ����� ID
	private String description = null; // ���
	private int specArrange = 0;// ������� (ȭ�鿡���� ǥ�þ���)
	private String masterPuid = null; // MASTER PUID
	private String ecoFirstCompleteDate = null; // ECO ó����¥ �� ���� ��¥
	private String gModel = null; // GMODEL

	private boolean rowCheck = false;
	private String lastFirstPeriod = null; // ECO ���� ó������ - ECO ���� ó�� ���� �Ⱓ

	// private boolean isStausWarning = false; // ���¿� ���� Color ǥ��

	private EcoStatusData rowDataObj = null; // ���� Row ����

	private Date startRegDate = null; // ����� ������
	private Date endRegDate = null; // ����� ������

	private STATUS_COLOR statusColor = STATUS_COLOR.BLACK; // ���� �÷�

	public static enum STATUS_COLOR {
		BLACK, RED, BLUE
	};

	public static String PROP_NAME_STATUS = "status";// ����
	public static String PROP_NAME_STAGE_TYPE = "stageType";// �з�
	public static String PROP_NAME_PROJECT_ID = "projectId";// Project
	public static String PROP_NAME_OSPECT_ID = "ospecId";// ����(O/Spec)
	public static String PROP_NAME_CHANGE_DESC = "changeDesc";// ���泻��
	public static String PROP_NAME_EST_APPLY_DATE = "estApplyDate";// �����������
	public static String PROP_NAME_RECEIPT_DATE = "receiptDate";// O/SPECT ������
	// public static String PROP_NAME_FIRST_MAIL_SEND_DATE = "firstMailSendDate";// ó�� ���� �߼� ��¥
	public static String PROP_NAME_COMPLETE_REQ_DATE = "ecoCompleteReqDate";// ECO �Ϸ� ��û��
	public static String PROP_NAME_EST_CHANGE_PERIOD = "estChangePeriod";// ���� ���躯�� �Ⱓ
	public static String PROP_NAME_ECO_LAST_COMPLETE_DATE = "ecoLastCompleteDate";// ����ECOó������
	public static String PROP_NAME_REAL_CHG_REIOAD = "realChangePeriod";// ���� ���躯�� �Ⱓ
	public static String PROP_NAME_TOTAL_REVIEWLIST = "totalReviewList";// ��ü ���� ����Ʈ
	public static String PROP_NAME_REQUIRED_ECO_LIST = "requiredEcoList";// �ʼ� ���躯�� �ʿ丮��Ʈ
	public static String PROP_NAME_IN_COMPLETE_CNT = "inCompleteCnt";// �Ⱓ�� �Ϸ�
	public static String PROP_NAME_DELAY_COMPLETE_CNT = "delayCompleteCnt";// �����Ϸ�
	public static String PROP_NAME_MISS_COMPLETE_CNT = "missCompleteCnt";// �����Ϸ�
	public static String PROP_NAME_IN_PROC_CNT = "inProcessCnt";// �Ⱓ�� ����
	public static String PROP_NAME_DELAY_PROC_CNT = "delayProcessCnt";// ���� ����
	public static String PROP_NAME_MISS_PROC_CNT = "missProcess";// ���� ����
	public static String PROP_NAME_REGISTER_DATE = "registerDate";// �����
	public static String PROP_NAME_DESCRIPTION = "description";// ���
	public static String PROP_NAME_SPEC_ARRANGE = "specArrange";// �������
	public static String PROP_NAME_ECO_FIRST_COMPLETE_DATE = "ecoFirstCompleteDate";// ECO ó����¥ �� ���� ��¥
	public static String PROP_NAME_ECO_LAST_FIRST_PERIOD = "lastFirstPeriod";// ECO ���� ó������ - ECO ���� ó�� ���� �Ⱓ

	public static String PROP_NAME_MASTER_PUID = "masterPuid";// �������� Unique Key
	// public static String PROP_NAME_IS_STATUS_WARNING = "isStausWarning";// ���¿� ���� Color ǥ��
	public static String PROP_NAME_IS_STATUS_COLOR = "statusColor";// ���¿� ���� Color ǥ��

	public static String PROP_NAME_ROW_DATA_OBJ = "rowDataObj";// ���� Row �� ��� ����

	public static String PROP_NAME_ROW_CHECK = "rowCheck";// �༱��

	public static final String CELL_RED_LABLEL = "CELL_RED_LABLEL"; // Row Cell ������ ��
	public static final String CELL_BLUE_LABLEL = "CELL_BLUE_LABLEL"; // Row Cell �Ķ��� ��
	public static final String CELL_CHANGE_STATUS_LABLEL = "CELL_CHANGE_STATUS_LABLEL"; // Row Cell ��
	public static final String ALIGN_CELL_CONTENTS_CENTER_CONFIG_LABEL = "alignCellContentsLeftConfigLabel"; // �߰����� ����

	public static final String CHECK_BOX_CONFIG_LABEL = "checkBox";
	public static final String CHECK_BOX_EDITOR_CONFIG_LABEL = "checkBoxEditor";

	public static final String TEMPLATE_DS_ECO_TOTAL_LIST_RPT = "ssangyong_eco_status_total_list_template.xlsx"; // ���躯����Ȳ Export Template��
	public static final String TEMPLATE_DS_ECO_TOTAL_DESC_RPT = "ssangyong_eco_status_desc_template.xlsx"; // ���� ��Export Template��

	private EventList<EcoStatusData> searchChangeStatusList; // ���躯����Ȳ ���̺� ������ ����Ʈ

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
	 * table �Ӽ� �� ����Ʈ
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

		propertyToLabelMap.put(PROP_NAME_STATUS, "����");
		propertyToLabelMap.put(PROP_NAME_STAGE_TYPE, "�з�");
		propertyToLabelMap.put(PROP_NAME_PROJECT_ID, "Project");
		propertyToLabelMap.put(PROP_NAME_OSPECT_ID, "����");
		propertyToLabelMap.put(PROP_NAME_CHANGE_DESC, "���泻��");
		propertyToLabelMap.put(PROP_NAME_EST_APPLY_DATE, "���躯��\n�����������");
		propertyToLabelMap.put(PROP_NAME_RECEIPT_DATE, "O/SPEC\n������");
		// propertyToLabelMap.put(PROP_NAME_FIRST_MAIL_SEND_DATE, "���躯��\n�����û����\n�߼���");
		propertyToLabelMap.put(PROP_NAME_COMPLETE_REQ_DATE, "ECO\n�Ϸ� ��û��");
		propertyToLabelMap.put(PROP_NAME_EST_CHANGE_PERIOD, "����\n���躯��\n�Ⱓ");
		propertyToLabelMap.put(PROP_NAME_ECO_LAST_COMPLETE_DATE, "����ECO\nó������");
		propertyToLabelMap.put(PROP_NAME_REAL_CHG_REIOAD, "���躯��\n�Ⱓ");
		propertyToLabelMap.put(PROP_NAME_TOTAL_REVIEWLIST, "��ü ����\n����Ʈ");
		propertyToLabelMap.put(PROP_NAME_REQUIRED_ECO_LIST, "�ʼ� ���躯��\n�ʿ丮��Ʈ");
		propertyToLabelMap.put(PROP_NAME_IN_COMPLETE_CNT, "�Ⱓ�� �Ϸ�");
		propertyToLabelMap.put(PROP_NAME_DELAY_COMPLETE_CNT, "���� �Ϸ�");
		propertyToLabelMap.put(PROP_NAME_MISS_COMPLETE_CNT, "���� �Ϸ�");
		propertyToLabelMap.put(PROP_NAME_IN_PROC_CNT, "�Ⱓ�� ����");
		propertyToLabelMap.put(PROP_NAME_DELAY_PROC_CNT, "���� ����");
		propertyToLabelMap.put(PROP_NAME_MISS_PROC_CNT, "���� ����");
		propertyToLabelMap.put(PROP_NAME_DESCRIPTION, "���");
		propertyToLabelMap.put(PROP_NAME_REGISTER_DATE, "�����");
		propertyToLabelMap.put(PROP_NAME_SPEC_ARRANGE, "");
		propertyToLabelMap.put(PROP_NAME_MASTER_PUID, "");
		propertyToLabelMap.put(PROP_NAME_IS_STATUS_COLOR, "");
		propertyToLabelMap.put(PROP_NAME_ECO_FIRST_COMPLETE_DATE, "");
		propertyToLabelMap.put(PROP_NAME_ECO_LAST_FIRST_PERIOD, "");
		propertyToLabelMap.put(PROP_NAME_ROW_DATA_OBJ, "");

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
	 * �������� table �Ӽ� �� ����Ʈ
	 * 
	 * @return
	 */
	public static String[] getStdPropertyNames() {
		return new String[] { PROP_NAME_ROW_CHECK, PROP_NAME_STATUS, PROP_NAME_STAGE_TYPE, PROP_NAME_PROJECT_ID, PROP_NAME_OSPECT_ID, PROP_NAME_CHANGE_DESC,
				PROP_NAME_EST_APPLY_DATE, PROP_NAME_RECEIPT_DATE, PROP_NAME_COMPLETE_REQ_DATE, PROP_NAME_EST_CHANGE_PERIOD, PROP_NAME_REGISTER_DATE,
				PROP_NAME_MASTER_PUID, PROP_NAME_IS_STATUS_COLOR, PROP_NAME_ROW_DATA_OBJ };
	}

	/**
	 * �������� Property Name ����
	 * 
	 * @return
	 */
	public static Map<String, String> getStdPropertyToLabelMap() {
		Map<String, String> propertyToLabelMap = new LinkedHashMap<String, String>();
		propertyToLabelMap.put(PROP_NAME_ROW_CHECK, "����");
		propertyToLabelMap.put(PROP_NAME_STATUS, "����");
		propertyToLabelMap.put(PROP_NAME_STAGE_TYPE, "�з�");
		propertyToLabelMap.put(PROP_NAME_PROJECT_ID, "Project");
		propertyToLabelMap.put(PROP_NAME_OSPECT_ID, "����(O/Spec)");
		propertyToLabelMap.put(PROP_NAME_CHANGE_DESC, "���泻��");
		propertyToLabelMap.put(PROP_NAME_EST_APPLY_DATE, "���躯��\n�����������");
		propertyToLabelMap.put(PROP_NAME_RECEIPT_DATE, "O/SPEC\n������");
		propertyToLabelMap.put(PROP_NAME_COMPLETE_REQ_DATE, "ECO\n�Ϸ� ��û��");
		propertyToLabelMap.put(PROP_NAME_EST_CHANGE_PERIOD, "���� ���躯��\n�Ⱓ");
		propertyToLabelMap.put(PROP_NAME_REGISTER_DATE, "�����");
		propertyToLabelMap.put(PROP_NAME_MASTER_PUID, "");
		propertyToLabelMap.put(PROP_NAME_IS_STATUS_COLOR, "");
		propertyToLabelMap.put(PROP_NAME_ROW_DATA_OBJ, "");

		return propertyToLabelMap;
	}

	/**
	 * �������� ���̺� Property ���� ������
	 * 
	 * @return
	 */
	public static List<String> getStdPropertyNamesAsList() {
		return Arrays.asList(getStdPropertyNames());
	}

	/**
	 * �������� �Ӽ��� Column Index
	 * 
	 * @param propertyName
	 * @return
	 */
	public static int getStdColumnIndexOfProperty(String propertyName) {
		return getStdPropertyNamesAsList().indexOf(propertyName);
	}

}
