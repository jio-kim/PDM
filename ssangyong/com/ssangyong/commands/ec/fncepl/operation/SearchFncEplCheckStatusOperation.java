package com.ssangyong.commands.ec.fncepl.operation;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import com.ssangyong.commands.ec.dao.CustomECODao;
import com.ssangyong.commands.ec.fncepl.model.FncEplCheckData;
import com.ssangyong.common.WaitProgressBar;
import com.ssangyong.common.remote.DataSet;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aifrcp.AIFUtility;

public class SearchFncEplCheckStatusOperation extends AbstractAIFOperation {
	private WaitProgressBar waitProgress;
	private FncEplCheckData inputData = null;

	public SearchFncEplCheckStatusOperation(FncEplCheckData inputData) {
		this.inputData = inputData;
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
		CustomECODao dao = new CustomECODao();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		Date startDate = inputData.getStartRegDate();
		Date endDate = inputData.getEndRegDate();
		String startDateStr = startDate == null ? null : df.format(startDate.getTime());
		String endDateStr = endDate == null ? null : df.format(endDate.getTime());
		DataSet ds = new DataSet();
		// 조건 검색
		ds.put("PROD_NO", inputData.getProdNo());
		ds.put("REG_START_DATE", startDateStr);
		ds.put("REG_END_DATE", endDateStr);

		ArrayList<HashMap<String, Object>> statusList = dao.getFncEplCheckStatusList(ds);
		for (HashMap<String, Object> rowMap : statusList) {
			FncEplCheckData rowData = new FncEplCheckData();
			rowData.setProdNo(FncEplCheckData.objToStr(rowMap.get("PROD_NAME")));
			rowData.setRegFncCnt(FncEplCheckData.objToInt(rowMap.get("REG_CNT")));
			rowData.setNeedCnt(FncEplCheckData.objToInt(rowMap.get("NEED_CNT")));
			rowData.setSpecCnt(FncEplCheckData.objToInt(rowMap.get("SPEC_CNT")));
			inputData.getTableDataList().add(rowData);
		}

	}

}
