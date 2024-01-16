package com.ssangyong.commands.ec.ecostatus.operation;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import ca.odell.glazedlists.EventList;

import com.ssangyong.commands.ec.dao.CustomECODao;
import com.ssangyong.commands.ec.ecostatus.model.EcoChangeData.StdInformData;
import com.ssangyong.commands.ec.ecostatus.model.EcoStatusData;
import com.ssangyong.common.utils.CustomUtil;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;

public class EcoStdInformCreateOperation extends AbstractAIFOperation {
	private StdInformData inputData = null;
	private TCSession tcSession = null;
	private EventList<EcoStatusData> tableDataList = null;

	public EcoStdInformCreateOperation(StdInformData inputData, EventList<EcoStatusData> tableDataList) {
		this.inputData = inputData;
		this.tableDataList = tableDataList;
		this.tcSession = CustomUtil.getTCSession();
	}

	@Override
	public void executeOperation() throws Exception {

		try {
			ArrayList<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();
			HashMap<String, String> dataMap = new HashMap<String, String>();
			Date toDate = new Date();
			SimpleDateFormat updateDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String createDate = updateDateFormat.format(toDate);
			
			String masterPuid = getSysGuid();
			String userId = tcSession.getUser().getUserId();
			inputData.setRegisterId(userId);
			inputData.setMasterPuid(masterPuid);
			dataMap.put("MASTER_PUID", masterPuid);
			dataMap.put("PROJECT_NO", inputData.getProjectId());
			dataMap.put("OSPEC_ID", inputData.getOspecId());
			dataMap.put("STAGE_TYPE", inputData.getStageType());
			dataMap.put("CHANGE_DESC", inputData.getChangeDesc());
			dataMap.put("APPLY_DATE", "".equals(inputData.getApplyDate()) ? null : inputData.getApplyDate());
			dataMap.put("OSPEC_RECEIPT_DATE", "".equals(inputData.getReceiptDate()) ? null : inputData.getReceiptDate());
			dataMap.put("ECO_COMPLETE_REQ_DATE", "".equals(inputData.getEcoCompleteReqDate()) ? null : inputData.getEcoCompleteReqDate());
			dataMap.put("REGISTER_TYPE", inputData.getRegisterType());
			dataMap.put("REGISTER_ID", userId);
			dataMap.put("CREATE_DATE", createDate);
			dataList.add(dataMap);

			/**
			 * DB Table에 저장
			 */
			createRptStdInfo(dataList);
			/**
			 * 화면에 Data Load
			 */
			loadUIData();

			MessageBox.post(AIFUtility.getActiveDesktop().getShell(), "Data was created Successfully", "Information", MessageBox.INFORMATION);
		} catch (Exception ex) {
			MessageBox.post(AIFUtility.getActiveDesktop().getShell(), ex.toString(), "Error", MessageBox.ERROR);
		}
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
	 * 기준정보 DB Table 에 생성
	 * 
	 * @param dataList
	 * @throws Exception
	 */
	private void createRptStdInfo(ArrayList<HashMap<String, String>> dataList) throws Exception {
		CustomECODao dao = new CustomECODao();
		dao.insertRptStdInfo(dataList);
	}

	/**
	 * 화면에 Data Load 함
	 * 
	 * @throws Exception
	 */
	private void loadUIData() throws Exception {
		SimpleDateFormat uiDateFormat = new SimpleDateFormat("yyyy-MM-dd");

		String applyDateStr = inputData.getApplyDate();
		String receiptDateStr = inputData.getReceiptDate();
		String ecoCompleteReqDateStr = inputData.getEcoCompleteReqDate();

		String changePeriod = null;
		Date receiptDate = null, ecoCompleteReqDate = null;

		if (receiptDateStr != null)
			receiptDate = uiDateFormat.parse(receiptDateStr);

		if (ecoCompleteReqDateStr != null)
			ecoCompleteReqDate = uiDateFormat.parse(ecoCompleteReqDateStr);

		if (receiptDate != null && ecoCompleteReqDate != null) {
			long differTime = ecoCompleteReqDate.getTime() - receiptDate.getTime();
			long changePeriodLong = (ecoCompleteReqDate.getTime() - receiptDate.getTime()) / (24 * 60 * 60 * 1000);
			changePeriodLong = differTime >= 0 ? changePeriodLong + 1 : changePeriodLong - 1;
			changePeriod = Long.toString(changePeriodLong).concat("일");
		}

		Date toDay = new Date();
		String createDate = uiDateFormat.format(toDay);

		EcoStatusData rowData = new EcoStatusData();
		rowData.setStatus("작성중");
		rowData.setStageType(inputData.getStageType());
		rowData.setProjectId(inputData.getProjectId());
		rowData.setOspecId(inputData.getOspecId());
		rowData.setChangeDesc(inputData.getChangeDesc());
		rowData.setEstApplyDate(applyDateStr);
		rowData.setReceiptDate(receiptDateStr);
		rowData.setEcoCompleteReqDate(ecoCompleteReqDateStr);
		rowData.setEstChangePeriod(changePeriod);
		rowData.setRegisterDate(createDate);
		rowData.setMasterPuid(inputData.getMasterPuid());
		//rowData.setStausWarning(false);
		rowData.setRowDataObj(rowData);

		tableDataList.add(0, rowData);
	}
}
