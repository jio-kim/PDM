package com.kgm.commands.ec.ecostatus.model;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ca.odell.glazedlists.EventList;

/**
 * OSPEC ���� Data ����
 * 
 * @author baek
 * 
 */
public class OspecSelectData {

	private String ospecId = null;
	private String releaseDate = null;
	private String gModel = null;
	private String projectNo = null;
	private boolean rowCheck = false;

	private EventList<OspecSelectData> tableDataList; // ���̺� ������ ����Ʈ

	public static String PROP_NAME_ROW_CHECK = "rowCheck";// �༱��
	public static String PROP_NAME_OSPEC_ID = "ospecId";// ����
	public static String PROP_NAME_RELEASE = "releaseDate";// �з�

	public static final String CHECK_BOX_CONFIG_LABEL = "checkBox";
	public static final String CHECK_BOX_EDITOR_CONFIG_LABEL = "checkBoxEditor";
	public static final String ALIGN_CELL_CONTENTS_CENTER_CONFIG_LABEL = "alignCellContentsLeftConfigLabel"; // �߰����� ����

	public OspecSelectData() {
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
	 * @return the releaseDate
	 */
	public String getReleaseDate() {
		return releaseDate;
	}

	/**
	 * @param releaseDate
	 *            the releaseDate to set
	 */
	public void setReleaseDate(String releaseDate) {
		this.releaseDate = releaseDate;
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
	 * @return the projectNo
	 */
	public String getProjectNo() {
		return projectNo;
	}

	/**
	 * @param projectNo
	 *            the projectNo to set
	 */
	public void setProjectNo(String projectNo) {
		this.projectNo = projectNo;
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
	 * @return the tableDataList
	 */
	public EventList<OspecSelectData> getTableDataList() {
		return tableDataList;
	}

	/**
	 * @param tableDataList
	 *            the tableDataList to set
	 */
	public void setTableDataList(EventList<OspecSelectData> tableDataList) {
		this.tableDataList = tableDataList;
	}

	/**
	 * table �Ӽ� �� ����Ʈ
	 * 
	 * @return
	 */
	public static String[] getPropertyNames() {
		return new String[] { PROP_NAME_ROW_CHECK, PROP_NAME_OSPEC_ID, PROP_NAME_RELEASE };
	}

	public static Map<String, String> getPropertyToLabelMap() {
		Map<String, String> propertyToLabelMap = new LinkedHashMap<String, String>();
		propertyToLabelMap.put(PROP_NAME_ROW_CHECK, "");
		propertyToLabelMap.put(PROP_NAME_OSPEC_ID, "Ospec ID");
		propertyToLabelMap.put(PROP_NAME_RELEASE, "Release Date");

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

}
