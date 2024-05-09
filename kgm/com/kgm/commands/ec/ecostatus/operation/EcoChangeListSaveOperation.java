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
import com.kgm.commands.ec.ecostatus.model.EcoChangeData.ChangeReviewData;
import com.kgm.common.WaitProgressBar;
import com.kgm.common.remote.DataSet;
import com.kgm.common.utils.CustomUtil;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.util.MessageBox;

/**
 * ���躯�渮��Ʈ �������� �������(�߰�/����/����)�� ����(Save)�ϴ� Operation
 * 
 * @author baek
 * 
 */
public class EcoChangeListSaveOperation extends AbstractAIFOperation {

	private WaitProgressBar waitProgress;
	private EcoChangeData data = null;
	private LinkedHashMap<ArrayList<String>, ChangeReviewData> changeReviewHash = null; // ������� ����Ʈ(KEY:�з� ,Project, O/SPEC ����,Category,Review Contents)
	private ArrayList<EcoChangeData> addDataList = null; // ���躯�渮��Ʈ�� �߰��Ǵ� ������ ����Ʈ
	private ArrayList<EcoChangeData> modifyDataList = null; // ���躯�渮��Ʈ�� �����Ǵ� ������ ����Ʈ
	private HashMap<ArrayList<String>, String> optionCategoryPuidHash = null; // Option Category PUID ����, Key:Product No,Ospec Id, Review Contents

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
			waitProgress.setStatus("Complete");
			waitProgress.close();

			if (addDataList.size() == 0 && modifyDataList.size() == 0 && data.getDeleteDataList().size() == 0)
				MessageBox.post(AIFUtility.getActiveDesktop().getShell(), "Data not changed. ", "Complete", MessageBox.INFORMATION);
			else {
				/**
				 * �����͸� �ٽ� ��ȸ�ؼ� Reload ��
				 */
				reloadDataList();
				MessageBox.post(AIFUtility.getActiveDesktop().getShell(), "Data was saved successfully", "Complete", MessageBox.INFORMATION);
			}
		} catch (Exception ex) {
			if (waitProgress != null) {
				waitProgress.setStatus("�� Error Message : ");
				waitProgress.setStatus(ex.toString());
				waitProgress.close("Error", false);
			}
			setAbortRequested(true);
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
		EventList<EcoChangeData> tableDataList = data.getSearchEcoChangeList();
		changeReviewHash = new LinkedHashMap<ArrayList<String>, ChangeReviewData>(); // ������䳻�� Table�� ����Ǵ� ����
		addDataList = new ArrayList<EcoChangeData>(); // �߰��Ǵ� Data ����Ʈ
		modifyDataList = new ArrayList<EcoChangeData>();// �����Ǵ� Data ����Ʈ
		optionCategoryPuidHash = new HashMap<ArrayList<String>, String>();

		// Function No, Part Name �� �ߺ��Ǿ����� Ȯ�� �� Key:Project Id, Ospec Id, ������䳻��, Function No, Part Name, Value: ���ȣ
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
			String opCategoryPuid = rowData.getOpCategoryPuid();

			if (CustomUtil.isNullString(reviewContents) || CustomUtil.isNullString(functionNo) || CustomUtil.isNullString(partName)) {
				continue;
			}
			// Row ���� �ߺ��Ǿ����� üũ�� ���� Row ���� ������
			ArrayList<String> dupRowCheckValueList = new ArrayList<String>(Arrays.asList(projectId, ospecId, reviewContents, functionNo, partName));
			if (!dupRowCheckList.containsKey(dupRowCheckValueList))
				dupRowCheckList.put(dupRowCheckValueList, rowData);

			/**
			 * Option Category PUID ���� ����(ECO_RPT_CHG_REVIEW ���̺� ����Ǵ� ����Ű)
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
			 * ���� ������ �ű��� ��� Data �з�
			 */
			if (rowChangeType.equals(EcoChangeData.ROW_CHANGE_TYPE_NEW)) {
				String projectId = rowData.getProjectId();
				String ospecId = rowData.getOspecId();
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

				ArrayList<String> opCategoryKeyList = new ArrayList<String>(Arrays.asList(projectId, ospecId, reviewContents));
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

				/**
				 * ���� ������ ������ ��� �з�
				 */
			} else if (rowChangeType.equals(EcoChangeData.ROW_CHANGE_TYPE_MODIFY)) {
				/**
				 * Row �� ����
				 */
				String errorMsg = validateData(rowData, null, rowIndex);
				errorMsgSb.append(errorMsg);
				// ������ ������ PASS
				if (!errorMsg.isEmpty()) {
					rowIndex++;
					continue;
				}
				/**
				 * ���渮��Ʈ ���̺� ������ Data ���� ����
				 */
				EcoChangeData rowInitData = rowData.getRowInitDataObj(); // ó�� ����� ����
				String initEcoPublish = EcoChangeData.objToStr(rowInitData.getEcoPublish());
				String initUserId = EcoChangeData.objToStr(rowInitData.getUserId());
				String initEcoNo = EcoChangeData.objToStr(rowInitData.getEcoNo());
				String initDescription = EcoChangeData.objToStr(rowInitData.getDescription());

				String ecoPublish = EcoChangeData.objToStr(rowData.getEcoPublish());
				String userId = EcoChangeData.objToStr(rowData.getUserId());
				String ecoNo = EcoChangeData.objToStr(rowData.getEcoNo());
				String description = EcoChangeData.objToStr(rowData.getDescription());

				// �ʱ������ ���� ������ �ѹ� �� ��
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
			ArrayList<String> dupRowCheckValueList = new ArrayList<String>(Arrays.asList(projectId, ospecId, reviewContents, functionNo, partName));
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
	 * Data ���� ����
	 * 
	 * @throws Exception
	 */
	private void executeSave() throws Exception {
		/**
		 * �߰��Ǵ� Data DB Table �� ������
		 */
		addDataList();

		/**
		 * �����Ǵ� Data ���� DB Table ���� Update ��
		 */
		modifyDataList();

		/**
		 * �����Ǵ� Data ���� DB Table ���� Update ��
		 */
		deleteDataList();

	}

	/**
	 * �߰��Ǵ� Data ���� DB Table �� ������ <BR>
	 * 1. ������� ������ �ű��̸� ������� ���̺�(ECO_RPT_CHG_REVIEW)�� ���� ��, ���渮��Ʈ ���̺�(ECO_RPT_LIST)�� Data ������<BR>
	 * 2. ������� ������ �̹� �����ϸ� ���渮��Ʈ ���̺�(ECO_RPT_LIST)�� Data ������<BR>
	 * 
	 * @throws Exception
	 */
	private void addDataList() throws Exception {

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
		/**
		 * ���� ���� DB ���̺� Data ����
		 */
		createRptChgReview(reviewChangeDataList);
		/**
		 * ���渮��Ʈ DB ���̺� Data ����
		 */
		createRptList(changeDataList);

	}

	/**
	 * �����Ǵ� Data ���� DB Table ���� Update ��
	 * 
	 * @throws Exception
	 */
	private void modifyDataList() throws Exception {

		if (modifyDataList.size() == 0)
			return;
		/**
		 * ������� ���̺� ������ Data ����
		 */
		ArrayList<HashMap<String, String>> modDataList = saveModifyChangeList();

		/**
		 * ���渮��Ʈ DB ���̺� Data ����
		 */
		updateRptList(modDataList);
	}

	/**
	 * �����Ǵ� Data ���� DB Table ���� ������
	 * 
	 * @throws Exception
	 */
	private void deleteDataList() throws Exception {
		ArrayList<EcoChangeData> deleteDataList = data.getDeleteDataList();

		if (deleteDataList.size() == 0)
			return;

		// �����Ǵ� ���� ����Ʈ ������ ����Ʈ
		ArrayList<HashMap<String, String>> deleteRptDataList = new ArrayList<HashMap<String, String>>();
		// ������ ���� ����Ʈ�� OPTION_CATEGORY_PUID ���� ����
		ArrayList<HashMap<String, String>> optionCategoryDataList = new ArrayList<HashMap<String, String>>();
		ArrayList<String> dupPuidCheck = new ArrayList<String>();// �ߺ� üũ
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
		 * 1. ���� ����Ʈ DB TABLE ������(ECO_RPT_LIST) ����
		 */
		deleteRpListWithPuid(deleteRptDataList);

		/**
		 * 2. ���� ���� ���� ���� (1) ���� ���� �� ���� ���䳻���� ���̻� ���� �������� ��� ������䳻�� ���̺��� �����͸� �����Ѵ�.<BR>
		 * ssangwongweb deleteChgReviewWithPuid ���� �Ǵ��Ͽ� ó����
		 */
		deleteChgReviewWithPuid(optionCategoryDataList);
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
	 * ������ ���� ����Ʈ �����͸� ������
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
	 * ���躯�� ��Ȳ ���� ����Ʈ ����
	 * 
	 * @param dataList
	 * @throws Exception
	 */
	private void updateRptList(ArrayList<HashMap<String, String>> dataList) throws Exception {
		CustomECODao dao = new CustomECODao();
		dao.updateRptList(dataList);
	}

	/**
	 * ���躯�� ��Ȳ ���� ����Ʈ ROW ����
	 * 
	 * @param dataList
	 * @throws Exception
	 */
	private void deleteRpListWithPuid(ArrayList<HashMap<String, String>> dataList) throws Exception {
		CustomECODao dao = new CustomECODao();
		dao.deleteRpListWithPuid(dataList);
	}

	/**
	 * ���躯�� ��Ȳ ���� ���� Row ����
	 * 
	 * @param dataList
	 * @throws Exception
	 */
	private void deleteChgReviewWithPuid(ArrayList<HashMap<String, String>> dataList) throws Exception {
		CustomECODao dao = new CustomECODao();
		dao.deleteChgReviewWithPuid(dataList);
	}

	/**
	 * �����͸� �ٽ� ��ȸ�ؼ� Load��
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
			EcoChangeData rowInitDataObj = (EcoChangeData) rowData.clone();// �ʱ� ������
			rowData.setRowInitDataObj(rowInitDataObj);

			// ���̺� Row �߰�
			data.getSearchEcoChangeList().add(rowData);
		}
	}

}
