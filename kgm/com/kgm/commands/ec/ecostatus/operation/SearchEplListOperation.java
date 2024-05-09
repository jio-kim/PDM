package com.kgm.commands.ec.ecostatus.operation;

import java.util.ArrayList;
import java.util.HashMap;

import ca.odell.glazedlists.EventList;

import com.kgm.commands.ec.dao.CustomECODao;
import com.kgm.commands.ec.ecostatus.model.EcoOspecCateData;
import com.kgm.commands.ec.ecostatus.model.EcoStatusData;
import com.kgm.commands.ec.ecostatus.model.EplSearchData;
import com.kgm.common.WaitProgressBar;
import com.kgm.common.remote.DataSet;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aifrcp.AIFUtility;

/**
 * EPL �˻� Operation
 * 
 * @author baek
 * 
 */
public class SearchEplListOperation extends AbstractAIFOperation {
	private WaitProgressBar waitProgress;
	private EcoOspecCateData categoryData = null; // Category ����
	private EcoStatusData stdInformData = null; // ��������
	private ArrayList<EplSearchData> allEplList = new ArrayList<EplSearchData>(); // ��ü EPL ����Ʈ
	private ArrayList<EplSearchData> eplWithDeDuplList = new ArrayList<EplSearchData>(); // �ߺ����ŵ� EPL ����Ʈ
	private HashMap<String, ArrayList<EplSearchData>> allEplListSet = new HashMap<String, ArrayList<EplSearchData>>();// Category �� EPL ��ü ����Ʈ
	private HashMap<String, ArrayList<EplSearchData>> eplWithDeDuplListSet = new HashMap<String, ArrayList<EplSearchData>>();// Category �� EPL �ߺ����ŵ� ����Ʈ
	private EventList<EplSearchData> tableDataList = null; // ���̺� ����Ʈ

	/**
	 * 
	 * @param initData
	 * @param stdInformData
	 */
	public SearchEplListOperation(EcoStatusData stdInformData, EcoOspecCateData categoryData, EventList<EplSearchData> tableDataList) {
		this.stdInformData = stdInformData;
		this.categoryData = categoryData;
		this.tableDataList = tableDataList;
	}

	@Override
	public void executeOperation() throws Exception {
		try {
			waitProgress = new WaitProgressBar(AIFUtility.getActiveDesktop());
			waitProgress.start();
			waitProgress.setStatus("Searching ...");
			executeSearch();
			waitProgress.setStatus("Complete");
			waitProgress.close();
		} catch (Exception ex) {

			if (waitProgress != null) {
				waitProgress.setStatus("�� Error Message : ");
				waitProgress.setStatus(ex.toString());
				waitProgress.close("Error", false);
			}
			setAbortRequested(true);
			ex.printStackTrace();
			throw ex;
		}
	}

	private void executeSearch() throws Exception {
		// ���� ��� Category ����Ʈ
		EventList<EcoOspecCateData> changeCategoryList = categoryData.getChangeCategoryList();
		ArrayList<String> categoryList = new ArrayList<String>();
		for (EcoOspecCateData cateData : changeCategoryList) {
			String category = cateData.getCategory();
			if (category == null)
				continue;
			categoryList.add(category);
		}
		// Category �� �߰� ���� �ɼ� Set
		HashMap<String, ArrayList<EcoOspecCateData>> addOrRemoveSet = categoryData.getAddOrRemoveMap();
		// Project �� �ش��ϴ� EPL JOB PUID�� ������
		String eplJobPuid = getEPLJobPuid(stdInformData.getProjectId());
		DataSet ds = new DataSet();
		ds.put("EPL_JOB_PUID", eplJobPuid);
		String addConditions = categoryData.getAddConditions();

		int splitInterval = 500; // Text �ڸ��� ���� REGEXP_LIKE�� 512 �ڸ����� ����
		ArrayList<String> targetOptionList = new ArrayList<String>();// �˻��Ǵ� �ɼǸ���Ʈ
		// 500 ���� ���̰� Ŭ���
		if (addConditions.length() > splitInterval) {
			int share = addConditions.length() / splitInterval; // �������� ���� ��
			int preIndex = 0;

			for (int i = 1; i <= share + 1; i++) {
				String splitText = null; // �������� ���� �ɼ�
				int lastIndex = 0;
				if (i <= share) {
					splitText = addConditions.substring(preIndex, i * splitInterval);
					lastIndex = splitText.lastIndexOf("|");
				} else {
					// �������� ���
					splitText = addConditions.substring(preIndex, addConditions.length());
					lastIndex = splitText.length();
				}

				String addOption = splitText.substring(0, lastIndex); //
				targetOptionList.add(addOption);
				preIndex = preIndex + lastIndex + 1;
			}
		} else {
			targetOptionList.add(addConditions);
		}

		ds.put("TARGET_OPTIONS", targetOptionList);
		// ds.put("EXCLUDE_OPTIONS", categoryData.getExcludeConditions());
		ArrayList<HashMap<String, String>> changeEplList = getAllChangeTargetEPLList(ds);

		for (HashMap<String, String> rowMap : changeEplList) {
			String functionNo = rowMap.get("FUNC_NO");
			String partName = rowMap.get("PART_NAME");
			String options = rowMap.get("OPTIONS").replace("@", "\n");
			String systemNo = rowMap.get("U_SYSTEM_CODE");
			String userName = rowMap.get("U_OWNER");
			String teamName = rowMap.get("U_TEAM");

			EplSearchData eplSearchData = new EplSearchData();
			eplSearchData.setFunctionNo(functionNo);
			eplSearchData.setPartName(partName);
			eplSearchData.setOptions(options);
			eplSearchData.setSystemNo(systemNo);
			eplSearchData.setUserName(userName);
			eplSearchData.setTeamName(teamName);

			EplSearchData eplDeDupSearchData = (EplSearchData) eplSearchData.clone();
			eplDeDupSearchData.setOptions(null); // �ߺ����ŵ� ���� Option �� Null

			boolean isExistOptionMatched = false; // Category �� ���ǿ� �´� Option ���� �ϳ��� �����ϴ� �� ���� üũ
			for (String category : categoryList) {
				// �ش� Category ���ǿ� �´� Option ���� ����
				boolean isCategoryOptionMatched = false;
				// �߰�, ����ɼ��� ������
				if (addOrRemoveSet.get(category) == null) {
					// Option �� Category �� ���ϴ��� üũ. �ش� Category ���ǿ� �´� �ɼ���
					if (options.indexOf(category) > -1)
						isCategoryOptionMatched = true;
				} else {
					ArrayList<EcoOspecCateData> addOrRemoveList = addOrRemoveSet.get(category);
					// Condition ���ǿ� �ϳ��� ���� �ʴٸ� true, �ƴϸ� false
					boolean isConditonNotMatched = false;
					// Option �� Category �� ������ ������ ���ǿ� ��������
					if (options.indexOf(category) == -1)
						isConditonNotMatched = true;
					else {
						for (EcoOspecCateData optionData : addOrRemoveList) {
							String addReviewOption = optionData.getAddReviewOption();
							String addOrExCondition = optionData.getAddOrExCondition();
							if (addReviewOption == null)
								continue;
							if (addOrExCondition == null)
								continue;
							// '����' �ɼ� ���� �����ϰ�, �������ǿ� �ش��ϴ� ���� ������
							if (EcoOspecCateData.ADD_REVIEW_CONDTIOIN_ADD.equals(addOrExCondition)) {
								if (options.indexOf(addReviewOption) == -1) {
									isConditonNotMatched = true;
									break;
								}
							} else {
								// '����' �ɼ� ���� �������� �ϴ���üũ. �������ǿ� �ش��ϴ� ���� ������
								if (options.indexOf(addReviewOption) > -1) {
									isConditonNotMatched = true;
									break;
								}
							}
						}
					}
					// �߰��ɼ� ������ ������
					if (!isConditonNotMatched)
						isCategoryOptionMatched = true;
				}
				if (!isCategoryOptionMatched)
					continue;
				/**
				 * Category ���� ��ü����Ʈ�� ������
				 */
				ArrayList<EplSearchData> eplList = null;
				if (!allEplListSet.containsKey(category)) {
					eplList = new ArrayList<EplSearchData>();
				} else {
					eplList = allEplListSet.get(category);
				}
				eplList.add(eplSearchData);
				allEplListSet.put(category, eplList);

				/**
				 * Category ���� �ߺ����ŵ� ����Ʈ ����
				 */
				ArrayList<EplSearchData> eplDeDupList = null;
				if (!eplWithDeDuplListSet.containsKey(category)) {
					eplDeDupList = new ArrayList<EplSearchData>();
					eplDeDupList.add(eplDeDupSearchData);
				} else {
					eplDeDupList = eplWithDeDuplListSet.get(category);
					if (!eplDeDupList.contains(eplDeDupSearchData))
						eplDeDupList.add(eplDeDupSearchData);
				}
				eplWithDeDuplListSet.put(category, eplDeDupList);

				/**
				 * Category �ϳ��� �ɼ����ǿ� ������ true
				 */
				isExistOptionMatched = true;
			}

			// ���ǿ� �´� �ɼ��� �����ϸ�, (�߰� �ɼ��� ���ų�, �߰��ɼ��� ���ǿ� ������)
			if (isExistOptionMatched) {
				/**
				 * ȭ�鿡 ��ü EPL Data ����Ʈ�� Load ��
				 */
				tableDataList.add(eplSearchData);
				/**
				 * ��ü EPL Data ����Ʈ�� ������
				 */
				allEplList.add(eplSearchData);

				/**
				 * �ߺ� ���ŵ� ����Ʈ ����
				 */
				if (!eplWithDeDuplList.contains(eplDeDupSearchData))
					eplWithDeDuplList.add(eplDeDupSearchData);

			}
		}
	}

	/**
	 * Project �� �ش��ϴ� EPL JOB PUID�� ������
	 * 
	 * @param projectNo
	 * @return
	 * @throws Exception
	 */
	private String getEPLJobPuid(String projectNo) throws Exception {
		CustomECODao dao = new CustomECODao();
		String sysGuid = dao.getEPLJobPuid(projectNo);
		return sysGuid;
	}

	/**
	 * EPL Full ����Ʈ �˻�
	 * 
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public ArrayList<HashMap<String, String>> getAllChangeTargetEPLList(DataSet ds) throws Exception {
		CustomECODao dao = new CustomECODao();
		ArrayList<HashMap<String, String>> resultList = dao.getAllChangeTargetEPLList(ds);
		return resultList;
	}

	/**
	 * @return the allEplList
	 */
	public ArrayList<EplSearchData> getAllEplList() {
		return allEplList;
	}

	/**
	 * @return the allEplWithDeDuplList
	 */
	public ArrayList<EplSearchData> getEplWithDeDuplList() {
		return eplWithDeDuplList;
	}

	/**
	 * @return the allEplListSet
	 */
	public HashMap<String, ArrayList<EplSearchData>> getAllEplListSet() {
		return allEplListSet;
	}

	/**
	 * @return the allEplWithDeDuplListSet
	 */
	public HashMap<String, ArrayList<EplSearchData>> getEplWithDeDuplListSet() {
		return eplWithDeDuplListSet;
	}
}
