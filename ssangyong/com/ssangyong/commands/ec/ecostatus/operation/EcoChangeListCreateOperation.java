package com.ssangyong.commands.ec.ecostatus.operation;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;

import ca.odell.glazedlists.EventList;

import com.ssangyong.commands.ec.dao.CustomECODao;
import com.ssangyong.commands.ec.ecostatus.model.EcoChangeData;
import com.ssangyong.commands.ec.ecostatus.model.EcoChangeData.ChangeReviewData;
import com.ssangyong.commands.ec.ecostatus.model.EcoOspecCateData;
import com.ssangyong.common.WaitProgressBar;
import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.utils.CustomUtil;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.util.MessageBox;

/**
 * 변경 관리 리스트 생성 Operation
 * 
 * @author baek
 * 
 */
public class EcoChangeListCreateOperation extends AbstractAIFOperation {
	private WaitProgressBar waitProgress;
	private EcoChangeData inputData = null;
	private LinkedHashMap<ArrayList<String>, ChangeReviewData> changeReviewHash = null; // 변경검토 리스트(KEY:분류 ,Project, O/SPEC 구분,Category,Review Contents)
	private ArrayList<EcoChangeData> addDataList = null; // 설계변경리스트에 추가되는 데이터 리스트
	private HashMap<ArrayList<String>, String> optionCategoryPuidHash = null; // Option Category PUID 정보, Key:Product No,Ospec Id, ategoryReview Contents
	private HashMap<ArrayList<String>, ArrayList<EcoOspecCateData>> categoryConditionMap = null; // Category 별 추가 검토 옵션 정보
	private String mainMasterPuid = null; // 기준정보 의 Master PUID
	private boolean isCreationSucces = false; // 생성 성공

	public EcoChangeListCreateOperation(EcoChangeData data, HashMap<ArrayList<String>, ArrayList<EcoOspecCateData>> categoryConditionMap, String mainMasterPuid) {
		this.inputData = data;
		this.categoryConditionMap = categoryConditionMap;
		this.mainMasterPuid = mainMasterPuid;
	}

	@Override
	public void executeOperation() throws Exception {

		try {

			waitProgress = new WaitProgressBar(AIFUtility.getActiveDesktop());
			waitProgress.start();
			waitProgress.setWindowSize(480, 300);
			waitProgress.setStatus("Verifying Data...");

			/**
			 * Data 검증 및 Data 분류
			 */
			String errorMsg = generateData();
			if (!errorMsg.isEmpty()) {
				waitProgress.setStatus(errorMsg.toString());
				waitProgress.close("Error", false);
				return;
			}
			waitProgress.setStatus("Saving Change List...");
			/**
			 * 정보저장
			 */
			executeSave();

			/**
			 * 저장된 Data 화면에 Load
			 */
			loadSavedData();

			waitProgress.setStatus("Complete");
			waitProgress.close();

			if (addDataList.size() == 0)
				MessageBox.post(AIFUtility.getActiveDesktop().getShell(), "Data not exist. ", "Complete", MessageBox.INFORMATION);
			else {
				/**
				 * 데이터를 다시 조회해서 Reload 함
				 */
				// reloadDataList();
				MessageBox.post(AIFUtility.getActiveDesktop().getShell(), "Data was saved successfully", "Complete", MessageBox.INFORMATION);
			}

			isCreationSucces = true;

		} catch (Exception ex) {
			if (waitProgress != null) {
				waitProgress.setStatus("＠ Error Message : ");
				waitProgress.setStatus(ex.toString());
				waitProgress.close("Error", false);
			}
			setAbortRequested(true);
			isCreationSucces = false;
			ex.printStackTrace();
		}
	}

	/**
	 * 데이터 검증 및 데이터 조합하여 저장함
	 * 
	 * @return
	 * @throws Exception
	 */
	private String generateData() throws Exception {
		EventList<EcoChangeData> tableDataList = inputData.getSearchEcoChangeList();
		changeReviewHash = new LinkedHashMap<ArrayList<String>, ChangeReviewData>(); // 변경검토내용 Table에 저장되는 정보
		addDataList = new ArrayList<EcoChangeData>(); // 추가되는 Data 리스트
		optionCategoryPuidHash = new HashMap<ArrayList<String>, String>();
		StringBuffer errorMsgSb = new StringBuffer();
		// Function No, Part Name 이 중복되었는지 확인 함 Key:Project Id, Ospec Id, Category, 변경검토내용, Function No, Part Name, Value: 행번호
		HashMap<ArrayList<String>, EcoChangeData> dupRowCheckList = new HashMap<ArrayList<String>, EcoChangeData>();

		/**
		 * Row Data 중목 체크
		 */
		for (EcoChangeData rowData : tableDataList) {
			String projectId = rowData.getProjectId();
			String ospecId = rowData.getOspecId();
			String reviewContents = rowData.getReviewContents();
			String functionNo = rowData.getFunctionNo();
			String partName = rowData.getPartName();
			String category = rowData.getCategory() == null ? "" : rowData.getCategory();

			if (CustomUtil.isNullString(reviewContents) || CustomUtil.isNullString(functionNo) || CustomUtil.isNullString(partName)) {
				continue;
			}
			// Row 값이 중복되었는지 체크를 위해 Row 값을 저장함
			ArrayList<String> dupRowCheckValueList = new ArrayList<String>(Arrays.asList(projectId, ospecId, category, reviewContents, functionNo, partName));
			if (!dupRowCheckList.containsKey(dupRowCheckValueList))
				dupRowCheckList.put(dupRowCheckValueList, rowData);
		}

		int rowIndex = 1;
		for (EcoChangeData rowData : tableDataList) {
			String rowChangeType = rowData.getRowChangeType();
			String reviewContents = rowData.getReviewContents();
			String category = rowData.getCategory() == null ? "" : rowData.getCategory();
			/**
			 * 변경 유형이 신규일 경우 Data 분류 <BR>
			 * FIXME:ROW_CHANGE_TYPE_ALL_NEW 일 경우는 이미 중복제어가 되어서 체크를 안해도 됨
			 */
			if (!rowChangeType.equals(EcoChangeData.ROW_CHANGE_TYPE_NEW) && !rowChangeType.equals(EcoChangeData.ROW_CHANGE_TYPE_ALL_NEW)) {
				rowIndex++;
				continue;
			}

			/**
			 * Row 값 검증
			 */
			String validMsg = validateData(rowData, dupRowCheckList, rowIndex);
			errorMsgSb.append(validMsg);
			// 오류가 있으면 PASS
			if (!validMsg.isEmpty()) {
				rowIndex++;
				continue;
			}

			ArrayList<String> opCategoryKeyList = new ArrayList<String>(Arrays.asList(category, reviewContents));
			// 동일한 변경검토 내용이 존재하면
			if (optionCategoryPuidHash.containsKey(opCategoryKeyList)) {
				String optionCategoryPuid = optionCategoryPuidHash.get(opCategoryKeyList);
				rowData.setOpCategoryPuid(optionCategoryPuid);
				// 신규 변경검토이면
			} else {
				String optionCategoryPuid = getSysGuid(); // Option Category PUID를 생성해서 가져옴
				optionCategoryPuidHash.put(opCategoryKeyList, optionCategoryPuid);
				rowData.setOpCategoryPuid(optionCategoryPuid);
				ChangeReviewData changeReviewData = new ChangeReviewData(rowData.getMasterPuid(), rowData.getCategory(), reviewContents, optionCategoryPuid);
				/**
				 * 변경검토내용 테이블에 추가 될 Data 정보 저장
				 */
				changeReviewHash.put(opCategoryKeyList, changeReviewData);
			}
			/**
			 * 변경리스트 테이블에 추가될 Data 정보 저장
			 */
			addDataList.add(rowData);
			rowIndex++;
		}

		/**
		 * Category 추가 검토 조건 리스트에서 변경검토내용 테이블에 추가 될 Data 저장<BR>
		 * 이 경우는 변경 리스트가 없는 Category 일 경우 해당
		 */
		for (ArrayList<String> categoryKey : categoryConditionMap.keySet()) {
			if (changeReviewHash.containsKey(categoryKey))
				continue;

			String optionCategoryPuid = getSysGuid(); // Option Category PUID를 생성해서 가져옴
			ChangeReviewData changeReviewData = new ChangeReviewData(mainMasterPuid, categoryKey.get(0), categoryKey.get(1), optionCategoryPuid);
			changeReviewHash.put(categoryKey, changeReviewData);
		}

		return errorMsgSb.toString();

	}

	/**
	 * 검증 메세지를 체크함
	 * 
	 * @param rowData
	 *            Row Data
	 * @param dupRowCheckList
	 *            중복값 체크를 위한 리스트
	 * @param rowIndex
	 *            Row 번호
	 * @return
	 */
	private String validateData(EcoChangeData rowData, HashMap<ArrayList<String>, EcoChangeData> dupRowCheckList, int rowIndex) {
		StringBuffer validMsgSb = new StringBuffer();
		String projectId = rowData.getProjectId();
		String ospecId = rowData.getOspecId();
		String functionNo = rowData.getFunctionNo();
		String partName = rowData.getPartName();
		String reviewContents = rowData.getReviewContents();
		String ecoPublish = rowData.getEcoPublish();
		String category = rowData.getCategory() == null ? "" : rowData.getCategory();
		int firstCharLength = 0;
		// 필수입력 항목 값 체크
		boolean isRequiredValueEmty = CustomUtil.isNullString(ecoPublish) || CustomUtil.isNullString(functionNo) || CustomUtil.isNullString(partName)
				|| CustomUtil.isNullString(reviewContents);

		if (isRequiredValueEmty) {
			validMsgSb.append(rowIndex + "행: ");
			firstCharLength = validMsgSb.length();
			if (CustomUtil.isNullString(ecoPublish))
				validMsgSb.append("ECO 발행");
			if (CustomUtil.isNullString(functionNo))
				validMsgSb.append(validMsgSb.length() == firstCharLength ? "" : ", ").append("Function No");
			if (CustomUtil.isNullString(partName))
				validMsgSb.append(validMsgSb.length() == firstCharLength ? "" : ", ").append("Part Name");
			if (CustomUtil.isNullString(reviewContents))
				validMsgSb.append(validMsgSb.length() == firstCharLength ? "" : ", ").append("변경검토내용");
			validMsgSb.append("은(는) 필수입력항목 입니다.\n");
		} else {
			ArrayList<String> dupRowCheckValueList = new ArrayList<String>(Arrays.asList(projectId, ospecId, category, reviewContents, functionNo, partName));
			boolean isDuplcatedRow = dupRowCheckList != null && dupRowCheckList.containsKey(dupRowCheckValueList);
			// 중복된 Row 값체크
			if (!isDuplcatedRow)
				return validMsgSb.toString();
			else {
				EcoChangeData checkData = dupRowCheckList.get(dupRowCheckValueList);
				// 중복된 값이 자기자신일 경우 PASS
				if (rowData.equals(checkData))
					return validMsgSb.toString();
			}
			validMsgSb.append(rowIndex + "행: ");
			firstCharLength = validMsgSb.length();
			validMsgSb.append("중복된 Data (Function No, Part Name)가 존재합니다.\n");

		}
		return validMsgSb.toString();
	}

	/**
	 * 신규 생성되는 Data 를 관련 DB Table 에 저장함 <BR>
	 * 변경검토 테이블(ECO_RPT_CHG_REVIEW)에 생성 후, 변경리스트 테이블(ECO_RPT_LIST)에 Data 생성함<BR>
	 * 추가 검토 옵션 테이블(ECO_RPT_OPT_CONDITION)에 Data 생성함
	 * 
	 * @throws Exception
	 */
	private void executeSave() throws Exception {
		if (addDataList.size() == 0)
			return;
		/**
		 * 변경검토 테이블에 생성할 Data 저장
		 */
		ArrayList<HashMap<String, String>> reviewChangeDataList = saveAddChgReviewList();
		/**
		 * 변경리스트 테이블에 생성할 Data 저장
		 */
		ArrayList<HashMap<String, String>> changeDataList = saveAddChangeList();

		ArrayList<HashMap<String, String>> optionCoditionDataList = saveOptionCondtionList();
		/**
		 * 변경 검토 DB 테이블에 Data 생성
		 */
		createRptChgReview(reviewChangeDataList);
		/**
		 * 변경리스트 DB 테이블에 Data 생성
		 */
		createRptList(changeDataList);
		/**
		 * 추가 검토 옵션 DB 테이블에 Data를 생성
		 */
		createRptOptCondition(optionCoditionDataList);
	}

	/**
	 * 생성할 변경검토 리스트 데이터를 저장함
	 * 
	 * @return
	 * @throws Exception
	 */
	private ArrayList<HashMap<String, String>> saveAddChgReviewList() throws Exception {
		ArrayList<HashMap<String, String>> reviewChangeDataList = new ArrayList<HashMap<String, String>>();
		for (ArrayList<String> reviewKey : changeReviewHash.keySet()) {
			ChangeReviewData reviewData = changeReviewHash.get(reviewKey);
			HashMap<String, String> dataMap = new HashMap<String, String>();
			dataMap.put("OPTION_CATEGORY_PUID", reviewData.getReviewPuid());
			dataMap.put("MASTER_PUID", reviewData.getMasterPuid());
			dataMap.put("OPTION_CATEGORY", reviewData.getCategory());
			dataMap.put("REVIEW_CONTENTS", reviewData.getReviewContents());
			reviewChangeDataList.add(dataMap);
		}
		return reviewChangeDataList;
	}

	/**
	 * 생성할 변경 리스트 데이터를 저장함
	 * 
	 * @return
	 * @throws Exception
	 */
	private ArrayList<HashMap<String, String>> saveAddChangeList() throws Exception {
		ArrayList<HashMap<String, String>> changeDataList = new ArrayList<HashMap<String, String>>();
		Date toDate = new Date();
		SimpleDateFormat updateDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String updateDate = updateDateFormat.format(toDate);

		for (EcoChangeData newData : addDataList) {
			HashMap<String, String> dataMap = new HashMap<String, String>();

			String userInform = newData.getUserId();
			String userId = null, userName = null;
			if (userInform != null && userInform.indexOf("(") > 0) {
				userId = userInform.substring(userInform.indexOf("(") + 1, userInform.indexOf(")"));
				userName = userInform.substring(0, userInform.indexOf("("));
			}
			dataMap.put("MASTER_PUID", newData.getMasterPuid());
			dataMap.put("OPTION_CATEGORY_PUID", newData.getOpCategoryPuid());
			dataMap.put("ECO_PUBLISH", newData.getEcoPublish());
			dataMap.put("FUNCTION_ID", newData.getFunctionNo());
			dataMap.put("PART_NAME", newData.getPartName());
			dataMap.put("SYSTEM", newData.getSystemNo());
			dataMap.put("USER_ID", userId);
			dataMap.put("USER_NAME", userName);
			dataMap.put("TEAM_NAME", newData.getTeamName());
			dataMap.put("ECO_NO", newData.getEcoNo());
			dataMap.put("DESCRIPTION", newData.getDescription());
			dataMap.put("UPDATE_DATE", updateDate);
			changeDataList.add(dataMap);
		}
		return changeDataList;
	}

	/**
	 * 추가 검토 옵션 정보를 저장함
	 */
	private ArrayList<HashMap<String, String>> saveOptionCondtionList() {
		ArrayList<HashMap<String, String>> optionCondtionList = new ArrayList<HashMap<String, String>>();
		for (ArrayList<String> categoryKey : categoryConditionMap.keySet()) {
			ArrayList<EcoOspecCateData> condtionList = categoryConditionMap.get(categoryKey);
			if (condtionList == null)
				continue;
			// 추가 검토 조건 정보가 있는 경우만 저장
			ChangeReviewData reviewData = changeReviewHash.get(categoryKey);
			if (reviewData == null)
				continue;

			for (EcoOspecCateData optionData : condtionList) {
				String optionCondtion = optionData.getAddOrExCondition();
				String optionCondFlage = null;
				if (EcoOspecCateData.ADD_REVIEW_CONDTIOIN_ADD.equals(optionCondtion))
					optionCondFlage = "Y";
				else if (EcoOspecCateData.ADD_REVIEW_CONDTIOIN_EXCLUSION.equals(optionCondtion))
					optionCondFlage = "N";
				HashMap<String, String> dataMap = new HashMap<String, String>();
				dataMap.put("OPTION_CATEGORY_PUID", reviewData.getReviewPuid());
				dataMap.put("MASTER_PUID", reviewData.getMasterPuid());
				dataMap.put("OPTION_CODE", optionData.getAddReviewOption());
				dataMap.put("OPTION_CONDITION", optionCondFlage);
				optionCondtionList.add(dataMap);
			}
		}
		return optionCondtionList;
	}

	/**
	 * 변경검토 DB Table 에 생성
	 * 
	 * @param dataList
	 * @throws Exception
	 */
	private void createRptChgReview(ArrayList<HashMap<String, String>> dataList) throws Exception {
		CustomECODao dao = new CustomECODao();
		dao.insertRptChgReview(dataList);
	}

	/**
	 * 변경 리스트 DB Table 에 생성
	 * 
	 * @param dataList
	 * @throws Exception
	 */
	private void createRptList(ArrayList<HashMap<String, String>> dataList) throws Exception {
		CustomECODao dao = new CustomECODao();
		dao.insertRptList(dataList);
	}

	/**
	 * 추가 검토 옵션 DB Table 에 생성
	 * 
	 * @param dataList
	 * @throws Exception
	 */
	private void createRptOptCondition(ArrayList<HashMap<String, String>> dataList) throws Exception {
		CustomECODao dao = new CustomECODao();
		dao.insertRptOptCondition(dataList);
	}

	/**
	 * Oracle Sys Guid를 가져옴
	 * 
	 * @return
	 * @throws Exception
	 */
	private String getSysGuid() throws Exception {
		CustomECODao dao = new CustomECODao();
		String sysGuid = dao.getSysGuid();
		return sysGuid;
	}

	/**
	 * 저장된 Data 화면에 Load
	 * 
	 * @throws Exception
	 */
	private void loadSavedData() throws Exception {
		inputData.getSearchEcoChangeList().clear();

		CustomECODao dao = new CustomECODao();

		DataSet ds = new DataSet();
		ArrayList<String> masterPuidList = new ArrayList<String>();
		masterPuidList.add(mainMasterPuid);
		ds.put("MASTER_PUID", masterPuidList);

		ArrayList<HashMap<String, String>> changList = dao.getEcoStatusChangeList(ds);

		for (HashMap<String, String> changeRowMap : changList) {
			String groupSeqNo = changeRowMap.get("GROUP_SEQ");
			String registerType = changeRowMap.get("REGISTER_TYPE");
			String optCategory = changeRowMap.get("OPTION_CATEGORY");
			String creationDate = changeRowMap.get("CREATE_DATE");
			String ecoPublish = changeRowMap.get("ECO_PUBLISH");
			String changeStatus = changeRowMap.get("STATUS");
			String functionNo = changeRowMap.get("FUNCTION_ID");
			String partName = changeRowMap.get("PART_NAME");
			String projectId = changeRowMap.get("PROJECT_NO");
			String ospecId = changeRowMap.get("OSPEC_ID");
			String changeDesc = changeRowMap.get("CHANGE_DESC");
			String reviewContents = changeRowMap.get("REVIEW_CONTENTS");
			String systemNo = changeRowMap.get("SYSTEM");
			String userId = changeRowMap.get("USER_NAME");
			String teamName = changeRowMap.get("TEAM_NAME");
			String mailStatus = changeRowMap.get("MAIL_STATUS");
			String ecoNo = changeRowMap.get("ECO_NO");
			String ecoCompleteDate = changeRowMap.get("ECO_COMPLETE_DATE");
			String description = changeRowMap.get("DESCRIPTION");
			String masterPuid = changeRowMap.get("MASTER_PUID");
			String opCategoryPuid = changeRowMap.get("OPTION_CATEGORY_PUID");
			String changeListPuid = changeRowMap.get("PUID");
			String engineFlag = changeRowMap.get("VORT_TYPE");
			String registerId = changeRowMap.get("REGISTER_ID");
			
			EcoChangeData rowData = new EcoChangeData();
			rowData.setGroupSeqNo(groupSeqNo);
			rowData.setRegisterType(registerType);
			rowData.setCategory(optCategory);
			rowData.setCreationDate(creationDate);
			rowData.setEcoPublish(ecoPublish);
			rowData.setChangeStatus(changeStatus);
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
			rowData.setMailStatus(mailStatus);
			rowData.setEcoNo(ecoNo);
			rowData.setEcoCompleteDate(ecoCompleteDate);
			rowData.setDescription(description);
			rowData.setMasterPuid(masterPuid);
			rowData.setOpCategoryPuid(opCategoryPuid);
			rowData.setRowChangeType(EcoChangeData.ROW_CHANGE_TYPE_NONE);
			rowData.setRowDataObj(rowData);
			rowData.setChangeListPuid(changeListPuid);
			rowData.setRegisterId(registerId);
			EcoChangeData rowInitDataObj = (EcoChangeData) rowData.clone();// 초기 데이터
			rowData.setRowInitDataObj(rowInitDataObj);
			// 테이블에 Row 추가
			inputData.getSearchEcoChangeList().add(rowData);
		}
	}

	/**
	 * @return the isCreationSucces
	 */
	public boolean isCreationSucces() {
		return isCreationSucces;
	}


}
