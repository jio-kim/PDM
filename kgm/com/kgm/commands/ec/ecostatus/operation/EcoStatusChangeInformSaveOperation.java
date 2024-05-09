package com.kgm.commands.ec.ecostatus.operation;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.eclipse.swt.widgets.Display;

import ca.odell.glazedlists.EventList;

import com.kgm.commands.ec.dao.CustomECODao;
import com.kgm.commands.ec.ecostatus.model.EcoChangeData;
import com.kgm.commands.ec.ecostatus.model.EcoOspecCateData;
import com.kgm.commands.ec.ecostatus.model.EcoStatusData;
import com.kgm.commands.ec.ecostatus.model.EcoOspecCateData.ChangeInform;
import com.kgm.commands.ec.ecostatus.ui.EcoChangeListMgrComposite;
import com.kgm.commands.ec.ecostatus.ui.EcoStatusManagerDialog;
import com.kgm.common.WaitProgressBar;
import com.kgm.common.remote.DataSet;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aifrcp.AIFUtility;

/**
 * 변경정보 입력 저장 Operation
 * 
 * @author baek
 * 
 */
public class EcoStatusChangeInformSaveOperation extends AbstractAIFOperation {
	private WaitProgressBar waitProgress;
	private EcoOspecCateData inputData = null; // 입력데이터
	private EcoStatusData stdInformData = null; // 기준정보
	private EcoStatusManagerDialog mainDialog = null;

	/**
	 * 
	 * @param targetCategorytList
	 * @param addConditions
	 * @param excludeConditions
	 */
	public EcoStatusChangeInformSaveOperation(EcoOspecCateData inputData, EcoStatusData stdInformData, EcoStatusManagerDialog mainDialog) {
		this.inputData = inputData;
		this.stdInformData = stdInformData;
		this.mainDialog = mainDialog;
	}

	@Override
	public void executeOperation() throws Exception {
		try {
			waitProgress = new WaitProgressBar(AIFUtility.getActiveDesktop());
			waitProgress.start();
			waitProgress.setWindowSize(480, 300);
			waitProgress.setStatus("Search EPL Change Data...");

			/**
			 * EPL 변경 대상 정보 조회
			 */
			LinkedHashMap<EcoOspecCateData, ArrayList<ChangeInform>> changeInformMap = executeEplSearch();

			/**
			 * 변경관리 리스트에 결과를 Load 함
			 */
			loadChangeTargetListOnUI(changeInformMap);

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
		}
	}

	/**
	 * EPL 검색
	 * 
	 * @throws Exception
	 */
	private LinkedHashMap<EcoOspecCateData, ArrayList<ChangeInform>> executeEplSearch() throws Exception {
		// Project에 해당하는 EPL JOB PUID를 가져옴
		String eplJobPuid = getEPLJobPuid(stdInformData.getProjectId());
		String addConditions = inputData.getAddConditions();
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
		DataSet ds = new DataSet();
		ds.put("EPL_JOB_PUID", eplJobPuid);
		ds.put("TARGET_OPTIONS", targetOptionList);
		// ds.put("EXCLUDE_OPTIONS", inputData.getExcludeConditions());
		ArrayList<HashMap<String, String>> changeTargetList = getChangeTargetEPLList(ds);

		EventList<EcoOspecCateData> changeCategoryList = inputData.getChangeCategoryList();
		LinkedHashMap<EcoOspecCateData, ArrayList<ChangeInform>> changeInformMap = new LinkedHashMap<EcoOspecCateData, ArrayList<ChangeInform>>();

		/**
		 * Category 변경 정보
		 */
		int firstSeq = 0;
		for (int i = 0; i < changeCategoryList.size(); i++) {
			EcoOspecCateData cateData = changeCategoryList.get(i);
			String category = cateData.getCategory();
			if (category == null)
				continue;
			firstSeq = makeRowData(changeInformMap, changeTargetList, cateData, firstSeq);
		}

		return changeInformMap;
	}

	/**
	 * EPL 검색된 결과에서 Category 별로 변경 리스트를 Row Data를 구성함
	 * 
	 * @param changeInformMap
	 *            Categroy 별로 변경 리스트 Data 정보
	 * @param changeTargetList
	 *            EPL 검색 결과
	 * @param cateData
	 *            Category 정보
	 * @param firstSeq
	 *            Group Sequence 의 첫번째 자리 Sequence
	 * @return
	 */
	private int makeRowData(LinkedHashMap<EcoOspecCateData, ArrayList<ChangeInform>> changeInformMap, ArrayList<HashMap<String, String>> changeTargetList,
			EcoOspecCateData cateData, int firstSeq) {
		boolean isExistCategoryValue = false; // Category 에 해당하는 Option Value가 존재하는지 여부
		String reviewChange = cateData.getReviewContents();
		// Category 별 추가 검토 옵션 Set
		HashMap<String, ArrayList<EcoOspecCateData>> addOrRemoveSet = inputData.getAddOrRemoveMap();
		// 검토내용이 없으면 Pass
		if (reviewChange == null) {
			return firstSeq;
		}

		for (HashMap<String, String> rowMap : changeTargetList) {
			String functionNo = rowMap.get("FUNC_NO");
			String partName = rowMap.get("PART_NAME");
			String options = rowMap.get("OPTIONS");
			String systemNo = rowMap.get("U_SYSTEM_CODE");
			String userId = rowMap.get("U_OWNER_ID");
			String userName = rowMap.get("U_OWNER");
			String teamName = rowMap.get("U_TEAM");
			String userInform = userName + "(" + userId + ")";
			String checkOptionKey = cateData.getCategory();

			// 해당 Category 조건에 맞는 Option 인지 유무
			boolean isCategoryOptionMatched = false;

			// 추가, 검토옵션이 없으면
			if (addOrRemoveSet.get(checkOptionKey) == null) {
				// Option 이 Category 에 속하는지 체크. 해당 Category 조건에 맞는 옵션임
				if (options.indexOf(checkOptionKey) > -1)
					isCategoryOptionMatched = true;
			} else {
				ArrayList<EcoOspecCateData> addOrRemoveList = addOrRemoveSet.get(checkOptionKey);
				// Condition 조건에 하나라도 맞지 않다면 true, 아니면 false
				boolean isConditonNotMatched = false;
				// Option 이 Category 에 속하지 않으면 조건에 맞지않음
				if (options.indexOf(checkOptionKey) == -1)
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

			// Category 에 속한 옵션을 가지고 있으면
			ChangeInform changeInform = new ChangeInform();
			changeInform.setReviewContents(cateData.getReviewContents());
			changeInform.setCategory(cateData.getCategory());
			changeInform.setFunctionNo(functionNo);
			changeInform.setPartName(partName);
			changeInform.setProjectId(stdInformData.getProjectId());
			changeInform.setOspecId(stdInformData.getOspecId());
			changeInform.setChangeDesc(stdInformData.getChangeDesc());
			changeInform.setSystemNo(systemNo);
			changeInform.setUserId(userInform);
			changeInform.setTeamName(teamName);

			ArrayList<ChangeInform> changeInformList = changeInformMap.get(cateData);
			String groupSeqNo = null;
			// 처음 저장시
			if (changeInformList == null) {
				changeInformList = new ArrayList<ChangeInform>();
				groupSeqNo = (firstSeq + 1) + "-1";
				changeInform.setGroupSeqNo(groupSeqNo);
				changeInformList.add(changeInform);
				changeInformMap.put(cateData, changeInformList);
				isExistCategoryValue = true;
			} else {
				// 중복되지 않는 데이터만 저장( 변경검토내용, CATEGORY, Function No, Part Name)
				if (changeInformList.contains(changeInform))
					continue;
				groupSeqNo = (firstSeq + 1) + "-" + (changeInformList.size() + 1);
				changeInform.setGroupSeqNo(groupSeqNo);
				changeInformList.add(changeInform);
			}
		}
		int nextGroupFirstSeq = isExistCategoryValue ? firstSeq + 1 : firstSeq;
		return nextGroupFirstSeq;
	}

	/**
	 * 변경관리 리스트에 결과를 Load 함
	 */
	private void loadChangeTargetListOnUI(final LinkedHashMap<EcoOspecCateData, ArrayList<ChangeInform>> changeInformMap) throws Exception {
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				// 설계변경관리 리스트 선택
				mainDialog.getTabFolder().setSelection(2);

				EcoChangeListMgrComposite changeListMgrComposite = mainDialog.getChangeListMgrComposite();
				changeListMgrComposite.setAllChangeMode(true); // 변경관리 생성 모드로 설정
				changeListMgrComposite.setCategoryAddCondList(inputData.getCategoryConditionMap()); // Category 별 추가 검토 옵션 정보 저장
				changeListMgrComposite.loadInitData();
				// 변경관리 테이블 Data 리스트
				EventList<EcoChangeData> tableDatalist = changeListMgrComposite.getTableDataList();
				tableDatalist.clear();

				Date toDate = new Date();
				SimpleDateFormat updateDateFormat = new SimpleDateFormat("yyyy-MM-dd");
				String updateDate = updateDateFormat.format(toDate);

				for (EcoOspecCateData cateData : changeInformMap.keySet()) {
					// 카테고리 별로 해당 변경Data를 가져온다.
					ArrayList<ChangeInform> changeInformList = changeInformMap.get(cateData);
					for (ChangeInform changeInform : changeInformList) {

						String groupSeqNo = changeInform.getGroupSeqNo();
						String reviewContents = changeInform.getReviewContents();
						String category = changeInform.getCategory();
						String functionNo = changeInform.getFunctionNo();
						String partName = changeInform.getPartName();
						String projectId = changeInform.getProjectId();
						String ospecId = changeInform.getOspecId();
						String changeDesc = changeInform.getChangeDesc();
						String systemNo = changeInform.getSystemNo();
						String userId = changeInform.getUserId();
						String teamName = changeInform.getTeamName();

						// Engine Type 가져옴
						String engineFlag = EcoChangeData.getEngineType(teamName);

						EcoChangeData rowData = new EcoChangeData();
						rowData.setGroupSeqNo(groupSeqNo);
						rowData.setRegisterType("시스템");
						rowData.setCategory(category);
						rowData.setCreationDate(updateDate);
						rowData.setChangeStatus("작성중");
						rowData.setEngineFlag(engineFlag);
						rowData.setFunctionNo(functionNo);
						rowData.setPartName(partName);
						rowData.setProjectId(projectId);
						rowData.setOspecId(ospecId);
						rowData.setChangeDesc(changeDesc);
						rowData.setReviewContents(reviewContents);
						rowData.setSystemNo(systemNo);
						rowData.setUserId(userId);
						rowData.setTeamName(teamName);
						rowData.setMasterPuid(stdInformData.getMasterPuid());
						rowData.setRowChangeType(EcoChangeData.ROW_CHANGE_TYPE_ALL_NEW);
						rowData.setRowDataObj(rowData);
						EcoChangeData rowInitDataObj = (EcoChangeData) rowData.clone();// 초기 데이터
						rowData.setRowInitDataObj(rowInitDataObj);
						tableDatalist.add(rowData);
					}
				}

			}
		});
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
	 * 설계변경현황 등록 대상 변경정보 리스트
	 * 
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public ArrayList<HashMap<String, String>> getChangeTargetEPLList(DataSet ds) throws Exception {
		CustomECODao dao = new CustomECODao();
		ArrayList<HashMap<String, String>> resultList = dao.getChangeTargetEPLList(ds);
		return resultList;
	}

}
