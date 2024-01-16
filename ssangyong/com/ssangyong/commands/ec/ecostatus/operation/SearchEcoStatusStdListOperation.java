package com.ssangyong.commands.ec.ecostatus.operation;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import com.ssangyong.commands.ec.dao.CustomECODao;
import com.ssangyong.commands.ec.ecostatus.model.EcoStatusData;
import com.ssangyong.commands.ec.ecostatus.model.EcoStatusData.STATUS_COLOR;
import com.ssangyong.common.WaitProgressBar;
import com.ssangyong.common.remote.DataSet;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aifrcp.AIFUtility;

/**
 * 변경현황 기준정보 리스트 조회
 * 
 * @author baek
 * 
 */
public class SearchEcoStatusStdListOperation extends AbstractAIFOperation {

	private WaitProgressBar waitProgress;
	private EcoStatusData data = null;

	public SearchEcoStatusStdListOperation(EcoStatusData data) {
		this.data = data;
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

	/**
	 * 리스트 검색
	 * 
	 * @throws Exception
	 */
	private void executeSearch() throws Exception {
		CustomECODao dao = new CustomECODao();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		Date startDate = data.getStartRegDate();
		Date endDate = data.getEndRegDate();
		String startDateStr = startDate == null ? null : df.format(startDate.getTime());
		String endDateStr = endDate == null ? null : df.format(endDate.getTime());
		DataSet ds = new DataSet();
		// 조건 검색
		ds.put("PROJECT_NO", data.getProjectId());
		ds.put("STAGE_TYPE", data.getStageType());
		ds.put("STATUS_FLAG", data.getStatus());
		ds.put("REG_START_DATE", startDateStr);
		ds.put("REG_END_DATE", endDateStr);
		ds.put("GMODEL_NO", data.getgModel());

		ArrayList<HashMap<String, Object>> changStatusList = dao.getEcoStatusStdList(ds);

		for (HashMap<String, Object> changeSatusRowMap : changStatusList) {

			EcoStatusData rowData = new EcoStatusData();

			String statusFlag = objToStr(changeSatusRowMap.get("STATUS_FLAG"));
			// boolean isStausWarning = statusFlag.endsWith("_2") ? true : false;
			STATUS_COLOR statusColor = STATUS_COLOR.BLACK;
			if (statusFlag.equals("진행중"))
				statusColor = STATUS_COLOR.BLUE;
			else if (statusFlag.equals("진행중_2"))
				statusColor = STATUS_COLOR.RED;

			String changePeriod = objToStr(changeSatusRowMap.get("CHANGE_PERIOD"));
			changePeriod = changePeriod.equals("") ? "" : changePeriod.concat("일");

			rowData.setStatus(statusFlag.split("_")[0]);
			rowData.setStageType(objToStr(changeSatusRowMap.get("STAGE_TYPE")));
			rowData.setProjectId(objToStr(changeSatusRowMap.get("PROJECT_NO")));
			rowData.setOspecId(objToStr(changeSatusRowMap.get("OSPEC_ID")));
			rowData.setChangeDesc(objToStr(changeSatusRowMap.get("CHANGE_DESC")));
			rowData.setEstApplyDate(objToStr(changeSatusRowMap.get("APPLY_DATE")));
			rowData.setReceiptDate(objToStr(changeSatusRowMap.get("OSPEC_RECEIPT_DATE")));
			rowData.setEcoCompleteReqDate(objToStr(changeSatusRowMap.get("ECO_COMPLETE_REQ_DATE")));
			rowData.setEstChangePeriod(changePeriod);
			rowData.setRegisterDate(objToStr(changeSatusRowMap.get("CREATE_DATE_STR")));
			rowData.setMasterPuid(objToStr(changeSatusRowMap.get("MASTER_PUID")));
			rowData.setRegisterUserId(objToStr(changeSatusRowMap.get("REGISTER_ID")));
			rowData.setStatusColor(statusColor);
			// rowData.setStausWarning(isStausWarning);
			rowData.setRowDataObj(rowData);
			data.getSearchChangeStatusList().add(rowData);
		}
	}

	private String objToStr(Object obj) {
		return obj != null ? (String) obj : "";
	}
}