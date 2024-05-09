package com.kgm.commands.ec.ecostatus.model;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ca.odell.glazedlists.EventList;

/**
 * EPL ��ȸ Data ����
 */
public class EplSearchData implements Cloneable {
	private String functionNo = null; // Function No
	private String partName = null; // Part Name
	private String options = null; // Options
	private String systemNo = null; // �ý���
	private String userName = null; // ����� ��
	private String teamName = null; // ����

	private EventList<EplSearchData> tableDataList = null; // �˻���� Table Data ����Ʈ

	public static String PROP_NAME_FUNCTION_NO = "functionNo";// Function No
	public static String PROP_NAME_PART_NAME = "partName";// Part Name
	public static String PROP_NAME_OPTIONS = "options";// Options
	public static String PROP_NAME_SYSTEM_NO = "systemNo";// �ý���
	public static String PROP_NAME_USER_NAME = "userName";// ����ڸ�
	public static String PROP_NAME_TEAM_NAME = "teamName";// ����

	public static final String TEMPLATE_EPL_LIST_RPT = "ssangyong_eco_status_epl_list_template.xlsx"; // EPL Search �˻� ��� Report Template

	public EplSearchData() {

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
	 * @return the options
	 */
	public String getOptions() {
		return options;
	}

	/**
	 * @param options
	 *            the options to set
	 */
	public void setOptions(String options) {
		this.options = options;
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
	 * @return the tableDataList
	 */
	public EventList<EplSearchData> getTableDataList() {
		return tableDataList;
	}

	/**
	 * @param tableDataList
	 *            the tableDataList to set
	 */
	public void setTableDataList(EventList<EplSearchData> tableDataList) {
		this.tableDataList = tableDataList;
	}

	/**
	 * table �Ӽ� �� ����Ʈ
	 * 
	 * @return
	 */
	public static String[] getPropertyNames() {
		return new String[] { PROP_NAME_FUNCTION_NO, PROP_NAME_PART_NAME, PROP_NAME_OPTIONS, PROP_NAME_SYSTEM_NO, PROP_NAME_USER_NAME, PROP_NAME_TEAM_NAME };
	}

	public static Map<String, String> getPropertyToLabelMap() {
		Map<String, String> propertyToLabelMap = new LinkedHashMap<String, String>();
		propertyToLabelMap.put(PROP_NAME_FUNCTION_NO, "Function No");
		propertyToLabelMap.put(PROP_NAME_PART_NAME, "Part Name");
		propertyToLabelMap.put(PROP_NAME_OPTIONS, "Option");
		propertyToLabelMap.put(PROP_NAME_SYSTEM_NO, "�ý���");
		propertyToLabelMap.put(PROP_NAME_USER_NAME, "����ڸ�");
		propertyToLabelMap.put(PROP_NAME_TEAM_NAME, "����");
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

	@Override
	public boolean equals(Object o) {
		EplSearchData compareData = (EplSearchData) o;

		// String systemNo1 = systemNo == null ? "" : systemNo;
		// String compareSystemNo = compareData.getSystemNo() == null ? "" : compareData.getSystemNo();
		// if (functionNo.equals(compareData.functionNo) && partName.equals(compareData.partName) && systemNo1.equals(compareSystemNo))
		// return true;
		if (functionNo.equals(compareData.functionNo) && partName.equals(compareData.partName))
			return true;
		return false;
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

}
