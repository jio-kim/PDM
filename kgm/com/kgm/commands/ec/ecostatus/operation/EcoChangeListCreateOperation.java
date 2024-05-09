package com.kgm.commands.ec.ecostatus.operation;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;

import ca.odell.glazedlists.EventList;

import com.kgm.commands.ec.dao.CustomECODao;
import com.kgm.commands.ec.ecostatus.model.EcoChangeData;
import com.kgm.commands.ec.ecostatus.model.EcoOspecCateData;
import com.kgm.commands.ec.ecostatus.model.EcoChangeData.ChangeReviewData;
import com.kgm.common.WaitProgressBar;
import com.kgm.common.remote.DataSet;
import com.kgm.common.utils.CustomUtil;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.util.MessageBox;

/**
 * ���� ���� ����Ʈ ���� Operation
 * 
 * @author baek
 * 
 */
public class EcoChangeListCreateOperation extends AbstractAIFOperation {
	private WaitProgressBar waitProgress;
	private EcoChangeData inputData = null;
	private LinkedHashMap<ArrayList<String>, ChangeReviewData> changeReviewHash = null; // ������� ����Ʈ(KEY:�з� ,Project, O/SPEC ����,Category,Review Contents)
	private ArrayList<EcoChangeData> addDataList = null; // ���躯�渮��Ʈ�� �߰��Ǵ� ������ ����Ʈ
	private HashMap<ArrayList<String>, String> optionCategoryPuidHash = null; // Option Category PUID ����, Key:Product No,Ospec Id, ategoryReview Contents
	private HashMap<ArrayList<String>, ArrayList<EcoOspecCateData>> categoryConditionMap = null; // Category �� �߰� ���� �ɼ� ����
	private String mainMasterPuid = null; // �������� �� Master PUID
	private boolean isCreationSucces = false; // ���� ����

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
			 * Data ���� �� Data �з�
			 */
			String errorMsg = generateData();
			if (!errorMsg.isEmpty()) {
				waitProgress.setStatus(errorMsg.toString());
				waitProgress.close("Error", false);
				return;
			}
			waitProgress.setStatus("Saving Change List...");
			/**
			 * ��������
			 */
			executeSave();

			/**
			 * ����� Data ȭ�鿡 Load
			 */
			loadSavedData();

			waitProgress.setStatus("Complete");
			waitProgress.close();

			if (addDataList.size() == 0)
				MessageBox.post(AIFUtility.getActiveDesktop().getShell(), "Data not exist. ", "Complete", MessageBox.INFORMATION);
			else {
				/**
				 * �����͸� �ٽ� ��ȸ�ؼ� Reload ��
				 */
				// reloadDataList();
				MessageBox.post(AIFUtility.getActiveDesktop().getShell(), "Data was saved successfully", "Complete", MessageBox.INFORMATION);
			}

			isCreationSucces = true;

		} catch (Exception ex) {
			if (waitProgress != null) {
				waitProgress.setStatus("�� Error Message : ");
				waitProgress.setStatus(ex.toString());
				waitProgress.close("Error", false);
			}
			setAbortRequested(true);
			isCreationSucces = false;
			ex.printStackTrace();
		}
	}

	/**
	 * ������ ���� �� ������ �����Ͽ� ������
	 * 
	 * @return
	 * @throws Exception
	 */
	private String generateData() throws Exception {
		EventList<EcoChangeData> tableDataList = inputData.getSearchEcoChangeList();
		changeReviewHash = new LinkedHashMap<ArrayList<String>, ChangeReviewData>(); // ������䳻�� Table�� ����Ǵ� ����
		addDataList = new ArrayList<EcoChangeData>(); // �߰��Ǵ� Data ����Ʈ
		optionCategoryPuidHash = new HashMap<ArrayList<String>, String>();
		StringBuffer errorMsgSb = new StringBuffer();
		// Function No, Part Name �� �ߺ��Ǿ����� Ȯ�� �� Key:Project Id, Ospec Id, Category, ������䳻��, Function No, Part Name, Value: ���ȣ
		HashMap<ArrayList<String>, EcoChangeData> dupRowCheckList = new HashMap<ArrayList<String>, EcoChangeData>();

		/**
		 * Row Data �߸� üũ
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
			// Row ���� �ߺ��Ǿ����� üũ�� ���� Row ���� ������
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
			 * ���� ������ �ű��� ��� Data �з� <BR>
			 * FIXME:ROW_CHANGE_TYPE_ALL_NEW �� ���� �̹� �ߺ���� �Ǿ üũ�� ���ص� ��
			 */
			if (!rowChangeType.equals(EcoChangeData.ROW_CHANGE_TYPE_NEW) && !rowChangeType.equals(EcoChangeData.ROW_CHANGE_TYPE_ALL_NEW)) {
				rowIndex++;
				continue;
			}

			/**
			 * Row �� ����
			 */
			String validMsg = validateData(rowData, dupRowCheckList, rowIndex);
			errorMsgSb.append(validMsg);
			// ������ ������ PASS
			if (!validMsg.isEmpty()) {
				rowIndex++;
				continue;
			}

			ArrayList<String> opCategoryKeyList = new ArrayList<String>(Arrays.asList(category, reviewContents));
			// ������ ������� ������ �����ϸ�
			if (optionCategoryPuidHash.containsKey(opCategoryKeyList)) {
				String optionCategoryPuid = optionCategoryPuidHash.get(opCategoryKeyList);
				rowData.setOpCategoryPuid(optionCategoryPuid);
				// �ű� ��������̸�
			} else {
				String optionCategoryPuid = getSysGuid(); // Option Category PUID�� �����ؼ� ������
				optionCategoryPuidHash.put(opCategoryKeyList, optionCategoryPuid);
				rowData.setOpCategoryPuid(optionCategoryPuid);
				ChangeReviewData changeReviewData = new ChangeReviewData(rowData.getMasterPuid(), rowData.getCategory(), reviewContents, optionCategoryPuid);
				/**
				 * ������䳻�� ���̺� �߰� �� Data ���� ����
				 */
				changeReviewHash.put(opCategoryKeyList, changeReviewData);
			}
			/**
			 * ���渮��Ʈ ���̺� �߰��� Data ���� ����
			 */
			addDataList.add(rowData);
			rowIndex++;
		}

		/**
		 * Category �߰� ���� ���� ����Ʈ���� ������䳻�� ���̺� �߰� �� Data ����<BR>
		 * �� ���� ���� ����Ʈ�� ���� Category �� ��� �ش�
		 */
		for (ArrayList<String> categoryKey : categoryConditionMap.keySet()) {
			if (changeReviewHash.containsKey(categoryKey))
				continue;

			String optionCategoryPuid = getSysGuid(); // Option Category PUID�� �����ؼ� ������
			ChangeReviewData changeReviewData = new ChangeReviewData(mainMasterPuid, categoryKey.get(0), categoryKey.get(1), optionCategoryPuid);
			changeReviewHash.put(categoryKey, changeReviewData);
		}

		return errorMsgSb.toString();

	}

	/**
	 * ���� �޼����� üũ��
	 * 
	 * @param rowData
	 *            Row Data
	 * @param dupRowCheckList
	 *            �ߺ��� üũ�� ���� ����Ʈ
	 * @param rowIndex
	 *            Row ��ȣ
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
		// �ʼ��Է� �׸� �� üũ
		boolean isRequiredValueEmty = CustomUtil.isNullString(ecoPublish) || CustomUtil.isNullString(functionNo) || CustomUtil.isNullString(partName)
				|| CustomUtil.isNullString(reviewContents);

		if (isRequiredValueEmty) {
			validMsgSb.append(rowIndex + "��: ");
			firstCharLength = validMsgSb.length();
			if (CustomUtil.isNullString(ecoPublish))
				validMsgSb.append("ECO ����");
			if (CustomUtil.isNullString(functionNo))
				validMsgSb.append(validMsgSb.length() == firstCharLength ? "" : ", ").append("Function No");
			if (CustomUtil.isNullString(partName))
				validMsgSb.append(validMsgSb.length() == firstCharLength ? "" : ", ").append("Part Name");
			if (CustomUtil.isNullString(reviewContents))
				validMsgSb.append(validMsgSb.length() == firstCharLength ? "" : ", ").append("������䳻��");
			validMsgSb.append("��(��) �ʼ��Է��׸� �Դϴ�.\n");
		} else {
			ArrayList<String> dupRowCheckValueList = new ArrayList<String>(Arrays.asList(projectId, ospecId, category, reviewContents, functionNo, partName));
			boolean isDuplcatedRow = dupRowCheckList != null && dupRowCheckList.containsKey(dupRowCheckValueList);
			// �ߺ��� Row ��üũ
			if (!isDuplcatedRow)
				return validMsgSb.toString();
			else {
				EcoChangeData checkData = dupRowCheckList.get(dupRowCheckValueList);
				// �ߺ��� ���� �ڱ��ڽ��� ��� PASS
				if (rowData.equals(checkData))
					return validMsgSb.toString();
			}
			validMsgSb.append(rowIndex + "��: ");
			firstCharLength = validMsgSb.length();
			validMsgSb.append("�ߺ��� Data (Function No, Part Name)�� �����մϴ�.\n");

		}
		return validMsgSb.toString();
	}

	/**
	 * �ű� �����Ǵ� Data �� ���� DB Table �� ������ <BR>
	 * ������� ���̺�(ECO_RPT_CHG_REVIEW)�� ���� ��, ���渮��Ʈ ���̺�(ECO_RPT_LIST)�� Data ������<BR>
	 * �߰� ���� �ɼ� ���̺�(ECO_RPT_OPT_CONDITION)�� Data ������
	 * 
	 * @throws Exception
	 */
	private void executeSave() throws Exception {
		if (addDataList.size() == 0)
			return;
		/**
		 * ������� ���̺� ������ Data ����
		 */
		ArrayList<HashMap<String, String>> reviewChangeDataList = saveAddChgReviewList();
		/**
		 * ���渮��Ʈ ���̺� ������ Data ����
		 */
		ArrayList<HashMap<String, String>> changeDataList = saveAddChangeList();

		ArrayList<HashMap<String, String>> optionCoditionDataList = saveOptionCondtionList();
		/**
		 * ���� ���� DB ���̺� Data ����
		 */
		createRptChgReview(reviewChangeDataList);
		/**
		 * ���渮��Ʈ DB ���̺� Data ����
		 */
		createRptList(changeDataList);
		/**
		 * �߰� ���� �ɼ� DB ���̺� Data�� ����
		 */
		createRptOptCondition(optionCoditionDataList);
	}

	/**
	 * ������ ������� ����Ʈ �����͸� ������
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
	 * ������ ���� ����Ʈ �����͸� ������
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
	 * �߰� ���� �ɼ� ������ ������
	 */
	private ArrayList<HashMap<String, String>> saveOptionCondtionList() {
		ArrayList<HashMap<String, String>> optionCondtionList = new ArrayList<HashMap<String, String>>();
		for (ArrayList<String> categoryKey : categoryConditionMap.keySet()) {
			ArrayList<EcoOspecCateData> condtionList = categoryConditionMap.get(categoryKey);
			if (condtionList == null)
				continue;
			// �߰� ���� ���� ������ �ִ� ��츸 ����
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
	 * ������� DB Table �� ����
	 * 
	 * @param dataList
	 * @throws Exception
	 */
	private void createRptChgReview(ArrayList<HashMap<String, String>> dataList) throws Exception {
		CustomECODao dao = new CustomECODao();
		dao.insertRptChgReview(dataList);
	}

	/**
	 * ���� ����Ʈ DB Table �� ����
	 * 
	 * @param dataList
	 * @throws Exception
	 */
	private void createRptList(ArrayList<HashMap<String, String>> dataList) throws Exception {
		CustomECODao dao = new CustomECODao();
		dao.insertRptList(dataList);
	}

	/**
	 * �߰� ���� �ɼ� DB Table �� ����
	 * 
	 * @param dataList
	 * @throws Exception
	 */
	private void createRptOptCondition(ArrayList<HashMap<String, String>> dataList) throws Exception {
		CustomECODao dao = new CustomECODao();
		dao.insertRptOptCondition(dataList);
	}

	/**
	 * Oracle Sys Guid�� ������
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
	 * ����� Data ȭ�鿡 Load
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
			EcoChangeData rowInitDataObj = (EcoChangeData) rowData.clone();// �ʱ� ������
			rowData.setRowInitDataObj(rowInitDataObj);
			// ���̺� Row �߰�
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
