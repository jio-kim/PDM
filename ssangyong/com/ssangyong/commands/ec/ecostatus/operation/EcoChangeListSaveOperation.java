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
import com.ssangyong.common.WaitProgressBar;
import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.utils.CustomUtil;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.util.MessageBox;

/**
 * 설계변경리스트 관리에서 변경사항(추가/삭제/수정)을 저장(Save)하는 Operation
 * 
 * @author baek
 * 
 */
public class EcoChangeListSaveOperation extends AbstractAIFOperation {

	private WaitProgressBar waitProgress;
	private EcoChangeData data = null;
	private LinkedHashMap<ArrayList<String>, ChangeReviewData> changeReviewHash = null; // 변경검토 리스트(KEY:분류 ,Project, O/SPEC 구분,Category,Review Contents)
	private ArrayList<EcoChangeData> addDataList = null; // 설계변경리스트에 추가되는 데이터 리스트
	private ArrayList<EcoChangeData> modifyDataList = null; // 설계변경리스트에 수정되는 데이터 리스트
	private HashMap<ArrayList<String>, String> optionCategoryPuidHash = null; // Option Category PUID 정보, Key:Product No,Ospec Id, Review Contents

	public EcoChangeListSaveOperation(EcoChangeData data) {
		this.data = data;
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
			waitProgress.setStatus("Complete");
			waitProgress.close();

			if (addDataList.size() == 0 && modifyDataList.size() == 0 && data.getDeleteDataList().size() == 0)
				MessageBox.post(AIFUtility.getActiveDesktop().getShell(), "Data not changed. ", "Complete", MessageBox.INFORMATION);
			else {
				/**
				 * 데이터를 다시 조회해서 Reload 함
				 */
				reloadDataList();
				MessageBox.post(AIFUtility.getActiveDesktop().getShell(), "Data was saved successfully", "Complete", MessageBox.INFORMATION);
			}
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
	 * 데이터 검증 및 데이터 조합하여 저장함
	 * 
	 * @return
	 * @throws Exception
	 */
	private String generateData() throws Exception {
		EventList<EcoChangeData> tableDataList = data.getSearchEcoChangeList();
		changeReviewHash = new LinkedHashMap<ArrayList<String>, ChangeReviewData>(); // 변경검토내용 Table에 저장되는 정보
		addDataList = new ArrayList<EcoChangeData>(); // 추가되는 Data 리스트
		modifyDataList = new ArrayList<EcoChangeData>();// 수정되는 Data 리스트
		optionCategoryPuidHash = new HashMap<ArrayList<String>, String>();

		// Function No, Part Name 이 중복되었는지 확인 함 Key:Project Id, Ospec Id, 변경검토내용, Function No, Part Name, Value: 행번호
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
			String opCategoryPuid = rowData.getOpCategoryPuid();

			if (CustomUtil.isNullString(reviewContents) || CustomUtil.isNullString(functionNo) || CustomUtil.isNullString(partName)) {
				continue;
			}
			// Row 값이 중복되었는지 체크를 위해 Row 값을 저장함
			ArrayList<String> dupRowCheckValueList = new ArrayList<String>(Arrays.asList(projectId, ospecId, reviewContents, functionNo, partName));
			if (!dupRowCheckList.containsKey(dupRowCheckValueList))
				dupRowCheckList.put(dupRowCheckValueList, rowData);

			/**
			 * Option Category PUID 정보 저장(ECO_RPT_CHG_REVIEW 테이블에 저장되는 유일키)
			 */
			ArrayList<String> optCategoryKeyList = new ArrayList<String>();
			optCategoryKeyList.add(projectId);
			optCategoryKeyList.add(ospecId);
			optCategoryKeyList.add(reviewContents);
			if (rowData.getRowChangeType().equals(EcoChangeData.ROW_CHANGE_TYPE_NEW))
				continue;
			if (!optionCategoryPuidHash.containsKey(optCategoryKeyList))
				optionCategoryPuidHash.put(optCategoryKeyList, opCategoryPuid);

		}

		StringBuffer errorMsgSb = new StringBuffer();
		int rowIndex = 1;
		for (EcoChangeData rowData : tableDataList) {
			String rowChangeType = rowData.getRowChangeType();
			String reviewContents = rowData.getReviewContents();
			/**
			 * 변경 유형이 신규일 경우 Data 분류
			 */
			if (rowChangeType.equals(EcoChangeData.ROW_CHANGE_TYPE_NEW)) {
				String projectId = rowData.getProjectId();
				String ospecId = rowData.getOspecId();
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

				ArrayList<String> opCategoryKeyList = new ArrayList<String>(Arrays.asList(projectId, ospecId, reviewContents));
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

				/**
				 * 변경 유형이 수정일 경우 분류
				 */
			} else if (rowChangeType.equals(EcoChangeData.ROW_CHANGE_TYPE_MODIFY)) {
				/**
				 * Row 값 검증
				 */
				String errorMsg = validateData(rowData, null, rowIndex);
				errorMsgSb.append(errorMsg);
				// 오류가 있으면 PASS
				if (!errorMsg.isEmpty()) {
					rowIndex++;
					continue;
				}
				/**
				 * 변경리스트 테이블에 수정될 Data 정보 저장
				 */
				EcoChangeData rowInitData = rowData.getRowInitDataObj(); // 처음 저장된 정보
				String initEcoPublish = EcoChangeData.objToStr(rowInitData.getEcoPublish());
				String initUserId = EcoChangeData.objToStr(rowInitData.getUserId());
				String initEcoNo = EcoChangeData.objToStr(rowInitData.getEcoNo());
				String initDescription = EcoChangeData.objToStr(rowInitData.getDescription());

				String ecoPublish = EcoChangeData.objToStr(rowData.getEcoPublish());
				String userId = EcoChangeData.objToStr(rowData.getUserId());
				String ecoNo = EcoChangeData.objToStr(rowData.getEcoNo());
				String description = EcoChangeData.objToStr(rowData.getDescription());

				// 초기저장된 값과 같은지 한번 더 비교
				boolean isRowChanged = !initEcoPublish.equals(ecoPublish) || !initUserId.equals(userId) || !initEcoNo.equals(ecoNo)
						|| !initDescription.equals(description);

				if (!isRowChanged)
					continue;

				modifyDataList.add(rowData);
			}
			rowIndex++;

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
			ArrayList<String> dupRowCheckValueList = new ArrayList<String>(Arrays.asList(projectId, ospecId, reviewContents, functionNo, partName));
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
	 * Data 저장 수행
	 * 
	 * @throws Exception
	 */
	private void executeSave() throws Exception {
		/**
		 * 추가되는 Data DB Table 에 저장함
		 */
		addDataList();

		/**
		 * 수정되는 Data 관련 DB Table 에서 Update 함
		 */
		modifyDataList();

		/**
		 * 삭제되는 Data 관련 DB Table 에서 Update 함
		 */
		deleteDataList();

	}

	/**
	 * 추가되는 Data 관련 DB Table 에 저장함 <BR>
	 * 1. 변경검토 내용이 신규이면 변경검토 테이블(ECO_RPT_CHG_REVIEW)에 생성 후, 변경리스트 테이블(ECO_RPT_LIST)에 Data 생성함<BR>
	 * 2. 변경검토 내용이 이미 존재하면 변경리스트 테이블(ECO_RPT_LIST)만 Data 생성함<BR>
	 * 
	 * @throws Exception
	 */
	private void addDataList() throws Exception {

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
		/**
		 * 변경 검토 DB 테이블에 Data 생성
		 */
		createRptChgReview(reviewChangeDataList);
		/**
		 * 변경리스트 DB 테이블에 Data 생성
		 */
		createRptList(changeDataList);

	}

	/**
	 * 수정되는 Data 관련 DB Table 에서 Update 함
	 * 
	 * @throws Exception
	 */
	private void modifyDataList() throws Exception {

		if (modifyDataList.size() == 0)
			return;
		/**
		 * 변경검토 테이블에 수정할 Data 저장
		 */
		ArrayList<HashMap<String, String>> modDataList = saveModifyChangeList();

		/**
		 * 변경리스트 DB 테이블에 Data 수정
		 */
		updateRptList(modDataList);
	}

	/**
	 * 삭제되는 Data 관련 DB Table 에서 삭제함
	 * 
	 * @throws Exception
	 */
	private void deleteDataList() throws Exception {
		ArrayList<EcoChangeData> deleteDataList = data.getDeleteDataList();

		if (deleteDataList.size() == 0)
			return;

		// 삭제되는 변경 리스트 데이터 리스트
		ArrayList<HashMap<String, String>> deleteRptDataList = new ArrayList<HashMap<String, String>>();
		// 삭제된 변경 리스트의 OPTION_CATEGORY_PUID 정보 저장
		ArrayList<HashMap<String, String>> optionCategoryDataList = new ArrayList<HashMap<String, String>>();
		ArrayList<String> dupPuidCheck = new ArrayList<String>();// 중복 체크
		for (EcoChangeData deleteData : deleteDataList) {
			HashMap<String, String> dataMap = new HashMap<String, String>();
			String opCategoryPuid = deleteData.getOpCategoryPuid();
			dataMap.put("PUID", deleteData.getChangeListPuid());
			dataMap.put("OPTION_CATEGORY_PUID", opCategoryPuid);
			deleteRptDataList.add(dataMap);
			if (!dupPuidCheck.contains(opCategoryPuid)) {
				dupPuidCheck.add(opCategoryPuid);
				optionCategoryDataList.add(dataMap);
			}
		}

		/**
		 * 1. 변경 리스트 DB TABLE 데이터(ECO_RPT_LIST) 삭제
		 */
		deleteRpListWithPuid(deleteRptDataList);

		/**
		 * 2. 변경 검토 정보 삭제 (1) 삭제 수행 후 변경 검토내용이 더이상 존재 하지않을 경우 변경검토내용 테이블에서 데이터를 삭제한다.<BR>
		 * ssangwongweb deleteChgReviewWithPuid 에서 판단하여 처리함
		 */
		deleteChgReviewWithPuid(optionCategoryDataList);
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
	 * 수정할 변경 리스트 데이터를 저장함
	 * 
	 * @return
	 * @throws Exception
	 */
	private ArrayList<HashMap<String, String>> saveModifyChangeList() throws Exception {
		ArrayList<HashMap<String, String>> changeDataList = new ArrayList<HashMap<String, String>>();
		for (EcoChangeData modData : modifyDataList) {
			HashMap<String, String> dataMap = new HashMap<String, String>();

			String userInform = modData.getUserId();
			String userId = null, userName = null;
			if (userInform != null && userInform.indexOf("(") > 0) {
				userId = userInform.substring(userInform.indexOf("(") + 1, userInform.indexOf(")"));
				userName = userInform.substring(0, userInform.indexOf("("));
			}
			dataMap.put("PUID", modData.getChangeListPuid());
			dataMap.put("ECO_PUBLISH", modData.getEcoPublish());
			dataMap.put("USER_ID", userId);
			dataMap.put("USER_NAME", userName);
			dataMap.put("TEAM_NAME", modData.getTeamName());
			dataMap.put("ECO_NO", modData.getEcoNo());
			dataMap.put("DESCRIPTION", modData.getDescription());
			changeDataList.add(dataMap);
		}
		return changeDataList;
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
	 * 설계변경 현황 변경 리스트 수정
	 * 
	 * @param dataList
	 * @throws Exception
	 */
	private void updateRptList(ArrayList<HashMap<String, String>> dataList) throws Exception {
		CustomECODao dao = new CustomECODao();
		dao.updateRptList(dataList);
	}

	/**
	 * 설계변경 현황 변경 리스트 ROW 삭제
	 * 
	 * @param dataList
	 * @throws Exception
	 */
	private void deleteRpListWithPuid(ArrayList<HashMap<String, String>> dataList) throws Exception {
		CustomECODao dao = new CustomECODao();
		dao.deleteRpListWithPuid(dataList);
	}

	/**
	 * 설계변경 현황 변경 검토 Row 삭제
	 * 
	 * @param dataList
	 * @throws Exception
	 */
	private void deleteChgReviewWithPuid(ArrayList<HashMap<String, String>> dataList) throws Exception {
		CustomECODao dao = new CustomECODao();
		dao.deleteChgReviewWithPuid(dataList);
	}

	/**
	 * 데이터를 다시 조회해서 Load함
	 * 
	 * @throws Exception
	 */
	private void reloadDataList() throws Exception {
		data.getSearchEcoChangeList().clear();

		CustomECODao dao = new CustomECODao();
		// String inputCategory = data.getCategory();
		String inputProjectId = data.getProjectId();
		String inputGmodelNo = data.getgModel();
		String inputOspecId = data.getOspecId();

		String vOrEType = data.getvOrEType();
		String functionName = data.getFunctionNo();
		String userName = data.getUserName();
		String ecoPublishC = data.getEcoPublish();
		String status = data.getChangeStatus();

		if (inputProjectId != null)
			inputGmodelNo = null;

		DataSet ds = new DataSet();
		ds.put("GMODEL_NO", inputGmodelNo);
		ds.put("PROJECT_NO", inputProjectId);
		// ds.put("CATEGORY_NO", inputCategory);
		ds.put("OSPEC_ID", inputOspecId);

		ds.put("VORT_TYPE", vOrEType);
		ds.put("FUNCTION_ID", functionName);
		ds.put("USER_NAME", userName);
		ds.put("ECO_PUBLISH", ecoPublishC);
		ds.put("STATUS", status);

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
			String adminDesc = changeRowMap.get("ADMIN_DESC");
			String opCategoryPuid = changeRowMap.get("OPTION_CATEGORY_PUID");
			String changeListPuid = changeRowMap.get("PUID");
			String engineFlag = changeRowMap.get("VORT_TYPE");
			String registerId = changeRowMap.get("REGISTER_ID");

			description = description == null && adminDesc != null ? adminDesc : description;

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
			data.getSearchEcoChangeList().add(rowData);
		}
	}

}
