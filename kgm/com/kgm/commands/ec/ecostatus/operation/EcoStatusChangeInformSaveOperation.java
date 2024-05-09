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
 * �������� �Է� ���� Operation
 * 
 * @author baek
 * 
 */
public class EcoStatusChangeInformSaveOperation extends AbstractAIFOperation {
	private WaitProgressBar waitProgress;
	private EcoOspecCateData inputData = null; // �Էµ�����
	private EcoStatusData stdInformData = null; // ��������
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
			 * EPL ���� ��� ���� ��ȸ
			 */
			LinkedHashMap<EcoOspecCateData, ArrayList<ChangeInform>> changeInformMap = executeEplSearch();

			/**
			 * ������� ����Ʈ�� ����� Load ��
			 */
			loadChangeTargetListOnUI(changeInformMap);

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
		}
	}

	/**
	 * EPL �˻�
	 * 
	 * @throws Exception
	 */
	private LinkedHashMap<EcoOspecCateData, ArrayList<ChangeInform>> executeEplSearch() throws Exception {
		// Project�� �ش��ϴ� EPL JOB PUID�� ������
		String eplJobPuid = getEPLJobPuid(stdInformData.getProjectId());
		String addConditions = inputData.getAddConditions();
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
		DataSet ds = new DataSet();
		ds.put("EPL_JOB_PUID", eplJobPuid);
		ds.put("TARGET_OPTIONS", targetOptionList);
		// ds.put("EXCLUDE_OPTIONS", inputData.getExcludeConditions());
		ArrayList<HashMap<String, String>> changeTargetList = getChangeTargetEPLList(ds);

		EventList<EcoOspecCateData> changeCategoryList = inputData.getChangeCategoryList();
		LinkedHashMap<EcoOspecCateData, ArrayList<ChangeInform>> changeInformMap = new LinkedHashMap<EcoOspecCateData, ArrayList<ChangeInform>>();

		/**
		 * Category ���� ����
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
	 * EPL �˻��� ������� Category ���� ���� ����Ʈ�� Row Data�� ������
	 * 
	 * @param changeInformMap
	 *            Categroy ���� ���� ����Ʈ Data ����
	 * @param changeTargetList
	 *            EPL �˻� ���
	 * @param cateData
	 *            Category ����
	 * @param firstSeq
	 *            Group Sequence �� ù��° �ڸ� Sequence
	 * @return
	 */
	private int makeRowData(LinkedHashMap<EcoOspecCateData, ArrayList<ChangeInform>> changeInformMap, ArrayList<HashMap<String, String>> changeTargetList,
			EcoOspecCateData cateData, int firstSeq) {
		boolean isExistCategoryValue = false; // Category �� �ش��ϴ� Option Value�� �����ϴ��� ����
		String reviewChange = cateData.getReviewContents();
		// Category �� �߰� ���� �ɼ� Set
		HashMap<String, ArrayList<EcoOspecCateData>> addOrRemoveSet = inputData.getAddOrRemoveMap();
		// ���䳻���� ������ Pass
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

			// �ش� Category ���ǿ� �´� Option ���� ����
			boolean isCategoryOptionMatched = false;

			// �߰�, ����ɼ��� ������
			if (addOrRemoveSet.get(checkOptionKey) == null) {
				// Option �� Category �� ���ϴ��� üũ. �ش� Category ���ǿ� �´� �ɼ���
				if (options.indexOf(checkOptionKey) > -1)
					isCategoryOptionMatched = true;
			} else {
				ArrayList<EcoOspecCateData> addOrRemoveList = addOrRemoveSet.get(checkOptionKey);
				// Condition ���ǿ� �ϳ��� ���� �ʴٸ� true, �ƴϸ� false
				boolean isConditonNotMatched = false;
				// Option �� Category �� ������ ������ ���ǿ� ��������
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

			// Category �� ���� �ɼ��� ������ ������
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
			// ó�� �����
			if (changeInformList == null) {
				changeInformList = new ArrayList<ChangeInform>();
				groupSeqNo = (firstSeq + 1) + "-1";
				changeInform.setGroupSeqNo(groupSeqNo);
				changeInformList.add(changeInform);
				changeInformMap.put(cateData, changeInformList);
				isExistCategoryValue = true;
			} else {
				// �ߺ����� �ʴ� �����͸� ����( ������䳻��, CATEGORY, Function No, Part Name)
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
	 * ������� ����Ʈ�� ����� Load ��
	 */
	private void loadChangeTargetListOnUI(final LinkedHashMap<EcoOspecCateData, ArrayList<ChangeInform>> changeInformMap) throws Exception {
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				// ���躯����� ����Ʈ ����
				mainDialog.getTabFolder().setSelection(2);

				EcoChangeListMgrComposite changeListMgrComposite = mainDialog.getChangeListMgrComposite();
				changeListMgrComposite.setAllChangeMode(true); // ������� ���� ���� ����
				changeListMgrComposite.setCategoryAddCondList(inputData.getCategoryConditionMap()); // Category �� �߰� ���� �ɼ� ���� ����
				changeListMgrComposite.loadInitData();
				// ������� ���̺� Data ����Ʈ
				EventList<EcoChangeData> tableDatalist = changeListMgrComposite.getTableDataList();
				tableDatalist.clear();

				Date toDate = new Date();
				SimpleDateFormat updateDateFormat = new SimpleDateFormat("yyyy-MM-dd");
				String updateDate = updateDateFormat.format(toDate);

				for (EcoOspecCateData cateData : changeInformMap.keySet()) {
					// ī�װ� ���� �ش� ����Data�� �����´�.
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

						// Engine Type ������
						String engineFlag = EcoChangeData.getEngineType(teamName);

						EcoChangeData rowData = new EcoChangeData();
						rowData.setGroupSeqNo(groupSeqNo);
						rowData.setRegisterType("�ý���");
						rowData.setCategory(category);
						rowData.setCreationDate(updateDate);
						rowData.setChangeStatus("�ۼ���");
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
						EcoChangeData rowInitDataObj = (EcoChangeData) rowData.clone();// �ʱ� ������
						rowData.setRowInitDataObj(rowInitDataObj);
						tableDatalist.add(rowData);
					}
				}

			}
		});
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
	 * ���躯����Ȳ ��� ��� �������� ����Ʈ
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
