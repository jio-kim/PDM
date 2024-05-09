package com.kgm.commands.ec.ecostatus.operation;

import java.util.ArrayList;
import java.util.HashMap;

import com.kgm.commands.ec.dao.CustomECODao;
import com.kgm.commands.ec.ecostatus.model.EcoChangeData;
import com.kgm.common.WaitProgressBar;
import com.kgm.common.remote.DataSet;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aifrcp.AIFUtility;

/**
 * 설계변경리스트 조회
 * 
 * @author baek
 * 
 */
public class SearchEcoChangeListOperation extends AbstractAIFOperation {

	private WaitProgressBar waitProgress;
	private EcoChangeData data = null;

	public SearchEcoChangeListOperation(EcoChangeData data) {
		this.data = data;
	}

	@Override
	public void executeOperation() throws Exception {

		try {
			waitProgress = new WaitProgressBar(AIFUtility.getActiveDesktop());
			waitProgress.start();
			waitProgress.setStatus("Searching Change List...");
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
			// MessageBox.post(AIFUtility.getActiveDesktop().getShell(), ex.toString(), "Error", MessageBox.ERROR);
			throw ex;
		}

	}

	/**
	 * 리스트 검색
	 * 
	 * @throws Exception
	 */
	private void executeSearch() throws Exception {
		CustomECODao dao = new CustomECODao();
		//String inputCategory = data.getCategory();
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
		//ds.put("CATEGORY_NO", inputCategory);
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
