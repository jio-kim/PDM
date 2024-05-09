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
 * EPL 검색 Operation
 * 
 * @author baek
 * 
 */
public class SearchEplListOperation extends AbstractAIFOperation {
	private WaitProgressBar waitProgress;
	private EcoOspecCateData categoryData = null; // Category 정보
	private EcoStatusData stdInformData = null; // 기준정보
	private ArrayList<EplSearchData> allEplList = new ArrayList<EplSearchData>(); // 전체 EPL 리스트
	private ArrayList<EplSearchData> eplWithDeDuplList = new ArrayList<EplSearchData>(); // 중복제거된 EPL 리스트
	private HashMap<String, ArrayList<EplSearchData>> allEplListSet = new HashMap<String, ArrayList<EplSearchData>>();// Category 별 EPL 전체 리스트
	private HashMap<String, ArrayList<EplSearchData>> eplWithDeDuplListSet = new HashMap<String, ArrayList<EplSearchData>>();// Category 별 EPL 중복제거된 리스트
	private EventList<EplSearchData> tableDataList = null; // 테이블 리스트

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
				waitProgress.setStatus("＠ Error Message : ");
				waitProgress.setStatus(ex.toString());
				waitProgress.close("Error", false);
			}
			setAbortRequested(true);
			ex.printStackTrace();
			throw ex;
		}
	}

	private void executeSearch() throws Exception {
		// 변경 대상 Category 리스트
		EventList<EcoOspecCateData> changeCategoryList = categoryData.getChangeCategoryList();
		ArrayList<String> categoryList = new ArrayList<String>();
		for (EcoOspecCateData cateData : changeCategoryList) {
			String category = cateData.getCategory();
			if (category == null)
				continue;
			categoryList.add(category);
		}
		// Category 별 추가 검토 옵션 Set
		HashMap<String, ArrayList<EcoOspecCateData>> addOrRemoveSet = categoryData.getAddOrRemoveMap();
		// Project 에 해당하는 EPL JOB PUID를 가져옴
		String eplJobPuid = getEPLJobPuid(stdInformData.getProjectId());
		DataSet ds = new DataSet();
		ds.put("EPL_JOB_PUID", eplJobPuid);
		String addConditions = categoryData.getAddConditions();

		int splitInterval = 500; // Text 자르는 간격 REGEXP_LIKE는 512 자리까지 가능
		ArrayList<String> targetOptionList = new ArrayList<String>();// 검색되는 옵션리스트
		// 500 보다 길이가 클경우
		if (addConditions.length() > splitInterval) {
			int share = addConditions.length() / splitInterval; // 간격으로 나눈 몫
			int preIndex = 0;

			for (int i = 1; i <= share + 1; i++) {
				String splitText = null; // 간격으로 나눈 옵션
				int lastIndex = 0;
				if (i <= share) {
					splitText = addConditions.substring(preIndex, i * splitInterval);
					lastIndex = splitText.lastIndexOf("|");
				} else {
					// 마지막일 경우
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
			eplDeDupSearchData.setOptions(null); // 중복제거된 값은 Option 이 Null

			boolean isExistOptionMatched = false; // Category 의 조건에 맞는 Option 값이 하나라도 존재하는 지 유무 체크
			for (String category : categoryList) {
				// 해당 Category 조건에 맞는 Option 인지 유무
				boolean isCategoryOptionMatched = false;
				// 추가, 검토옵션이 없으면
				if (addOrRemoveSet.get(category) == null) {
					// Option 이 Category 에 속하는지 체크. 해당 Category 조건에 맞는 옵션임
					if (options.indexOf(category) > -1)
						isCategoryOptionMatched = true;
				} else {
					ArrayList<EcoOspecCateData> addOrRemoveList = addOrRemoveSet.get(category);
					// Condition 조건에 하나라도 맞지 않다면 true, 아니면 false
					boolean isConditonNotMatched = false;
					// Option 이 Category 에 속하지 않으면 조건에 맞지않음
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
							// '포함' 옵션 값이 존재하고, 포함조건에 해당하는 값이 없으면
							if (EcoOspecCateData.ADD_REVIEW_CONDTIOIN_ADD.equals(addOrExCondition)) {
								if (options.indexOf(addReviewOption) == -1) {
									isConditonNotMatched = true;
									break;
								}
							} else {
								// '제외' 옵션 값이 존재하지 하는지체크. 제외조건에 해당하는 값이 있으면
								if (options.indexOf(addReviewOption) > -1) {
									isConditonNotMatched = true;
									break;
								}
							}
						}
					}
					// 추가옵션 조건이 맞으면
					if (!isConditonNotMatched)
						isCategoryOptionMatched = true;
				}
				if (!isCategoryOptionMatched)
					continue;
				/**
				 * Category 별로 전체리스트를 저장함
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
				 * Category 별로 중복제거된 리스트 저장
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
				 * Category 하나라도 옵션조건에 맞으면 true
				 */
				isExistOptionMatched = true;
			}

			// 조건에 맞는 옵션이 존재하면, (추가 옵션이 없거나, 추가옵션의 조건에 맞으면)
			if (isExistOptionMatched) {
				/**
				 * 화면에 전체 EPL Data 리스트를 Load 함
				 */
				tableDataList.add(eplSearchData);
				/**
				 * 전체 EPL Data 리스트를 저장함
				 */
				allEplList.add(eplSearchData);

				/**
				 * 중복 제거된 리스트 저장
				 */
				if (!eplWithDeDuplList.contains(eplDeDupSearchData))
					eplWithDeDuplList.add(eplDeDupSearchData);

			}
		}
	}

	/**
	 * Project 에 해당하는 EPL JOB PUID를 가져옴
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
	 * EPL Full 리스트 검색
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
