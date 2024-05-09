package com.kgm.commands.ec.ecostatus.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ca.odell.glazedlists.EventList;

/**
 * ���� ���� �Է� Data
 * 
 * @author baek
 * 
 */
public class EcoOspecCateData {

	private String category = null; // ���� Category
	private String reviewContents = null; // ������䳻��
	private String addReviewOption = null;// �߰� ���� �ɼ�
	private String addOrExCondition = null; // �߰�/���� ����
	private String projectNo = null; // Project No
	private String addConditions = null; // �߰� ��������
	private String excludeConditions = null; // ���� ��������
	private boolean rowCheck = false;
	private boolean categoryEditable = false; // Category Edit ���� ����

	public static String PROP_NAME_CATEGORY = "category";// ���� Category
	public static String PROP_NAME_REVIEW_CONTENTS = "reviewContents";// ������䳻��
	public static String PROP_NAME_CATEGORY_EDITABLE = "categoryEditable";// Category ���� ����

	public static String PROP_NAME_ROW_CHECK = "rowCheck";// �༱��
	public static String PROP_NAME_ADD_REVIEW_OPTION = "addReviewOption";// �߰� ���� �ɼ�
	public static String PROP_NAME_ADD_OR_EX_CONDITION = "addOrExCondition";// �߰�/���� ����

	public static final String EDITABLE_CONFIG_LABEL = "editableCellConfigLabel"; // Cell ���������� ��
	public static final String ALIGN_CELL_CONTENTS_CENTER_CONFIG_LABEL = "alignCellContentsLeftConfigLabel"; // �߰����� ����

	public static final String COMBO_ADD_OR_EX_CONFIG_LABEL = "ComboAddOrExConfigLabel"; // �߰�/���� ���� Combo box ��

	public static final String CHECK_BOX_CONFIG_LABEL = "checkBox";
	public static final String CHECK_BOX_EDITOR_CONFIG_LABEL = "checkBoxEditor";

	public static final String ADD_REVIEW_CONDTIOIN_ADD = "����";
	public static final String ADD_REVIEW_CONDTIOIN_EXCLUSION = "����";

	public static final String CELL_EDITABLE_RULE_APPLY_LABLEL = "CELL_EDITABLE_RULE_APPLY_LABLEL"; // Cell Read/Write ������ ����� Cell ��

	private EventList<EcoOspecCateData> changeCategoryList; // ���� Category ����Ʈ
	private HashMap<ArrayList<String>, ArrayList<EcoOspecCateData>> categoryConditionMap = new HashMap<ArrayList<String>, ArrayList<EcoOspecCateData>>();// Category �� �߰� ����ɼ� ����
	private HashMap<String, ArrayList<EcoOspecCateData>> addOrRemoveMap = new HashMap<String, ArrayList<EcoOspecCateData>>();// Category �� �߰� ����ɼ� ����

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
	 * @return the addReviewOption
	 */
	public String getAddReviewOption() {
		return addReviewOption;
	}

	/**
	 * @param addReviewOption
	 *            the addReviewOption to set
	 */
	public void setAddReviewOption(String addReviewOption) {
		this.addReviewOption = addReviewOption;
	}

	/**
	 * @return the addOrExCondition
	 */
	public String getAddOrExCondition() {
		return addOrExCondition;
	}

	/**
	 * @param addOrExCondition
	 *            the addOrExCondition to set
	 */
	public void setAddOrExCondition(String addOrExCondition) {
		this.addOrExCondition = addOrExCondition;
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
	 * @return the addConditions
	 */
	public String getAddConditions() {
		return addConditions;
	}

	/**
	 * @param addConditions
	 *            the addConditions to set
	 */
	public void setAddConditions(String addConditions) {
		this.addConditions = addConditions;
	}

	/**
	 * @return the excludeConditions
	 */
	public String getExcludeConditions() {
		return excludeConditions;
	}

	/**
	 * @param excludeConditions
	 *            the excludeConditions to set
	 */
	public void setExcludeConditions(String excludeConditions) {
		this.excludeConditions = excludeConditions;
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
	 * @return the changeCategoryList
	 */
	public EventList<EcoOspecCateData> getChangeCategoryList() {
		return changeCategoryList;
	}

	/**
	 * @return the categoryEditable
	 */
	public boolean isCategoryEditable() {
		return categoryEditable;
	}

	/**
	 * @param categoryEditable
	 *            the categoryEditable to set
	 */
	public void setCategoryEditable(boolean categoryEditable) {
		this.categoryEditable = categoryEditable;
	}

	/**
	 * @param changeCategoryList
	 *            the changeCategoryList to set
	 */
	public void setChangeCategoryList(EventList<EcoOspecCateData> changeCategoryList) {
		this.changeCategoryList = changeCategoryList;
	}

	/**
	 * @return the categoryConditionMap
	 */
	public HashMap<ArrayList<String>, ArrayList<EcoOspecCateData>> getCategoryConditionMap() {
		return categoryConditionMap;
	}

	/**
	 * @param categoryConditionMap
	 *            the categoryConditionMap to set
	 */
	public void setCategoryConditionMap(HashMap<ArrayList<String>, ArrayList<EcoOspecCateData>> categoryConditionMap) {
		this.categoryConditionMap = categoryConditionMap;
	}

	/**
	 * @return the addOrRemoveMap
	 */
	public HashMap<String, ArrayList<EcoOspecCateData>> getAddOrRemoveMap() {
		return addOrRemoveMap;
	}

	/**
	 * @param addOrRemoveMap
	 *            the addOrRemoveMap to set
	 */
	public void setAddOrRemoveMap(HashMap<String, ArrayList<EcoOspecCateData>> addOrRemoveMap) {
		this.addOrRemoveMap = addOrRemoveMap;
	}

	public static String[] getPropertyNames() {
		return new String[] { PROP_NAME_ROW_CHECK, PROP_NAME_CATEGORY, PROP_NAME_REVIEW_CONTENTS, PROP_NAME_CATEGORY_EDITABLE };
	}

	public static Map<String, String> getPropertyToLabelMap() {
		Map<String, String> propertyToLabelMap = new LinkedHashMap<String, String>();
		propertyToLabelMap.put(PROP_NAME_ROW_CHECK, "");
		propertyToLabelMap.put(PROP_NAME_CATEGORY, "���� Category");
		propertyToLabelMap.put(PROP_NAME_REVIEW_CONTENTS, "������� ����");
		propertyToLabelMap.put(PROP_NAME_CATEGORY_EDITABLE, "");
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

	public static String[] getOptPropertyNames() {
		return new String[] { PROP_NAME_ROW_CHECK, PROP_NAME_ADD_REVIEW_OPTION, PROP_NAME_ADD_OR_EX_CONDITION };
	}

	public static Map<String, String> getOptPropertyToLabelMap() {
		Map<String, String> propertyToLabelMap = new LinkedHashMap<String, String>();
		propertyToLabelMap.put(PROP_NAME_ROW_CHECK, "");
		propertyToLabelMap.put(PROP_NAME_ADD_REVIEW_OPTION, "�߰� ���� �ɼ�");
		propertyToLabelMap.put(PROP_NAME_ADD_OR_EX_CONDITION, "�߰�/���� ����");
		return propertyToLabelMap;
	}

	/**
	 * ���̺� Property ���� ������
	 * 
	 * @return
	 */
	public static List<String> getOptPropertyNamesAsList() {
		return Arrays.asList(getOptPropertyNames());
	}

	/**
	 * �Ӽ��� Column Index
	 * 
	 * @param propertyName
	 * @return
	 */
	public static int getOptColumnIndexOfProperty(String propertyName) {
		return getOptPropertyNamesAsList().indexOf(propertyName);
	}

	public static class ChangeInform {
		private String groupSeqNo = null; // ����
		private String category = null; // Option Category
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

		public ChangeInform() {

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

		@Override
		public boolean equals(Object o) {
			ChangeInform compareData = (ChangeInform) o;
			String compareReviewContents = compareData.reviewContents == null ? "" : compareData.reviewContents;
			String tartgetReviewContents = reviewContents == null ? "" : reviewContents;
			String compareCategory = compareData.category == null ? "" : compareData.category;
			String tartgetCategory = category == null ? "" : category;

			if (compareReviewContents.equals(tartgetReviewContents) && compareCategory.equals(tartgetCategory) && compareData.functionNo.equals(functionNo)
					&& compareData.partName.equals(partName))
				return true;
			return false;
		}
	}

}
