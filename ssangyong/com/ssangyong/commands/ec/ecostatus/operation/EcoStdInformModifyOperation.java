package com.ssangyong.commands.ec.ecostatus.operation;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.ssangyong.commands.ec.dao.CustomECODao;
import com.ssangyong.commands.ec.ecostatus.model.EcoChangeData.StdInformData;
import com.ssangyong.commands.ec.ecostatus.model.EcoStatusData;
import com.ssangyong.common.remote.DataSet;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.util.MessageBox;

/**
 * 설계변경 현황 기준정보 수정
 * 
 * @author baek
 * 
 */
public class EcoStdInformModifyOperation extends AbstractAIFOperation {
	private StdInformData inputData = null;
	private EcoStatusData selectedRowData = null;

	public EcoStdInformModifyOperation(StdInformData inputData, EcoStatusData selectedRowData) {
		this.inputData = inputData;
		this.selectedRowData = selectedRowData;
	}

	@Override
	public void executeOperation() throws Exception {

		try {
			/**
			 * 기준정보 DB Table Update
			 */
			updateRptStdInfo();

			/**
			 * 선택된 Row Data 업데이트
			 */
			updateUIRowData();
			MessageBox.post(AIFUtility.getActiveDesktop().getShell(), "Data was saved Successfully", "Information", MessageBox.INFORMATION);
		} catch (Exception ex) {
			MessageBox.post(AIFUtility.getActiveDesktop().getShell(), ex.toString(), "Error", MessageBox.ERROR);
		}
	}

	/**
	 * 기준정보 DB Table 에 수정
	 * 
	 * @param dataList
	 * @throws Exception
	 */
	private void updateRptStdInfo() throws Exception {
		DataSet ds = new DataSet();
		String masterPuid = selectedRowData.getMasterPuid();
		ds.put("MASTER_PUID", masterPuid);
		ds.put("PROJECT_NO", inputData.getProjectId());
		ds.put("OSPEC_ID", inputData.getOspecId());
		ds.put("STAGE_TYPE", inputData.getStageType());
		ds.put("CHANGE_DESC", inputData.getChangeDesc());
		ds.put("APPLY_DATE", "".equals(inputData.getApplyDate()) ? null : inputData.getApplyDate());
		ds.put("OSPEC_RECEIPT_DATE", "".equals(inputData.getReceiptDate()) ? null : inputData.getReceiptDate());
		ds.put("ECO_COMPLETE_REQ_DATE", "".equals(inputData.getEcoCompleteReqDate()) ? null : inputData.getEcoCompleteReqDate());

		CustomECODao dao = new CustomECODao();
		dao.updateRptStdInfo(ds);
	}

	/**
	 * 선택된 Row Data Update
	 * 
	 * @throws Exception
	 */
	private void updateUIRowData() throws Exception {
		SimpleDateFormat uiDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String changePeriod = null;
		Date receiptDate = null, ecoCompleteReqDate = null;
		String receiptDateStr = inputData.getReceiptDate();
		String ecoCompleteReqDateStr = inputData.getEcoCompleteReqDate();
		if (receiptDateStr != null)
			receiptDate = uiDateFormat.parse(receiptDateStr);

		if (ecoCompleteReqDateStr != null)
			ecoCompleteReqDate = uiDateFormat.parse(ecoCompleteReqDateStr);

		if (receiptDate != null && ecoCompleteReqDate != null) {
			// long changePeriodLong = Math.abs((ecoCompleteReqDate.getTime() - receiptDate.getTime()) / (1000 * 60 * 60 * 24)) + 1;
			long differTime = ecoCompleteReqDate.getTime() - receiptDate.getTime();
			long changePeriodLong = (ecoCompleteReqDate.getTime() - receiptDate.getTime()) / (24 * 60 * 60 * 1000);
			changePeriodLong = differTime >= 0 ? changePeriodLong + 1 : changePeriodLong - 1;
			changePeriod = Long.toString(changePeriodLong).concat("일");
		}
		selectedRowData.setProjectId(inputData.getProjectId());
		selectedRowData.setOspecId(inputData.getOspecId());
		selectedRowData.setStageType(inputData.getStageType());
		selectedRowData.setChangeDesc(inputData.getChangeDesc());
		selectedRowData.setEstApplyDate(inputData.getApplyDate());
		selectedRowData.setReceiptDate(inputData.getReceiptDate());
		selectedRowData.setEcoCompleteReqDate(inputData.getEcoCompleteReqDate());
		selectedRowData.setEstChangePeriod(changePeriod);
	}

}
