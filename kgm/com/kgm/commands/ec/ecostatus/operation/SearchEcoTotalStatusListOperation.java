package com.kgm.commands.ec.ecostatus.operation;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import com.kgm.commands.ec.dao.CustomECODao;
import com.kgm.commands.ec.ecostatus.model.EcoStatusData;
import com.kgm.commands.ec.ecostatus.model.EcoStatusData.STATUS_COLOR;
import com.kgm.common.WaitProgressBar;
import com.kgm.common.remote.DataSet;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aifrcp.AIFUtility;

/**
 * 변경현황 리스트 조회
 * 
 * @author baek
 * 
 */
public class SearchEcoTotalStatusListOperation extends AbstractAIFOperation {

	private WaitProgressBar waitProgress;
	private EcoStatusData data = null;

	public SearchEcoTotalStatusListOperation(EcoStatusData data) {
		this.data = data;
	}

	@Override
	public void executeOperation() throws Exception {
		try {
			waitProgress = new WaitProgressBar(AIFUtility.getActiveDesktop());
			waitProgress.start();
			waitProgress.setStatus("Searching Change Status List...");
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
		ds.put("DESCRIPTION", data.getDescription());
		ds.put("REG_START_DATE", startDateStr);
		ds.put("REG_END_DATE", endDateStr);
		ds.put("GMODEL_NO", data.getgModel());

		ArrayList<HashMap<String, Object>> changStatusList = dao.getEcoTotalStatusList(ds);

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

			String realChgPeriod = objToStr(changeSatusRowMap.get("REAL_CHANGE_PERIOD"));
			realChgPeriod = realChgPeriod.equals("") ? "" : realChgPeriod.concat("일");

			String lastFirstPeriod = objToStr(changeSatusRowMap.get("LAST_FIRST_PERIOD"));
			lastFirstPeriod = lastFirstPeriod.equals("") ? "" : lastFirstPeriod.concat("일");

			rowData.setStatus(statusFlag.split("_")[0]);
			rowData.setStageType(objToStr(changeSatusRowMap.get("STAGE_TYPE")));
			rowData.setProjectId(objToStr(changeSatusRowMap.get("PROJECT_NO")));
			rowData.setOspecId(objToStr(changeSatusRowMap.get("OSPEC_ID")));
			rowData.setChangeDesc(objToStr(changeSatusRowMap.get("CHANGE_DESC")));
			rowData.setEstApplyDate(objToStr(changeSatusRowMap.get("APPLY_DATE")));
			rowData.setReceiptDate(objToStr(changeSatusRowMap.get("OSPEC_RECEIPT_DATE")));
			rowData.setFirstMailSendDate(objToStr(changeSatusRowMap.get("FIRST_MAIL_SEND_DATE")));
			rowData.setEcoCompleteReqDate(objToStr(changeSatusRowMap.get("ECO_COMPLETE_REQ_DATE")));
			rowData.setEstChangePeriod(changePeriod);
			rowData.setEcoLastCompleteDate(objToStr(changeSatusRowMap.get("ECO_LAST_COMPLETE_DATE")));
			rowData.setRealChangePeriod(realChgPeriod);
			rowData.setTotalReviewList(objToInt(changeSatusRowMap.get("TOTAL_CNT")));
			rowData.setRequiredEcoList(objToInt(changeSatusRowMap.get("REQUIRED_ECO_CNT")));
			rowData.setInCompleteCnt(objToInt(changeSatusRowMap.get("IN_COMPLETE")));
			rowData.setDelayCompleteCnt(objToInt(changeSatusRowMap.get("DELAY_COMPLETE")));
			rowData.setMissCompleteCnt(objToInt(changeSatusRowMap.get("MISS_ERR_COMPLETE")));
			rowData.setInProcessCnt(objToInt(changeSatusRowMap.get("IN_PROCESSING")));
			rowData.setDelayProcessCnt(objToInt(changeSatusRowMap.get("DELAY_PROCESSING")));
			rowData.setMissProcess(objToInt(changeSatusRowMap.get("MISS_ERR_PROCESSING")));
			rowData.setRegisterDate(objToStr(changeSatusRowMap.get("CREATE_DATE_STR")));
			rowData.setDescription(objToStr(changeSatusRowMap.get("DESCRIPTION")));
			rowData.setSpecArrange(objToInt(changeSatusRowMap.get("SPEC_ARRANGE")));
			rowData.setMasterPuid(objToStr(changeSatusRowMap.get("MASTER_PUID")));
			// rowData.setStausWarning(isStausWarning);
			rowData.setStatusColor(statusColor);
			rowData.setEcoFirstCompleteDate(objToStr(changeSatusRowMap.get("ECO_FIRST_COMPLETE_DATE")));
			rowData.setLastFirstPeriod(lastFirstPeriod);
			rowData.setRowDataObj(rowData);
			data.getSearchChangeStatusList().add(rowData);
		}
	}

	private String objToStr(Object obj) {
		return obj != null ? (String) obj : "";
	}

	private int objToInt(Object obj) {
		return obj != null ? ((BigDecimal) obj).intValue() : 0;
	}
}
