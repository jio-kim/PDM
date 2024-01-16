package com.ssangyong.commands.ec.fncepl.operation;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import ca.odell.glazedlists.EventList;

import com.ssangyong.commands.ec.dao.CustomECODao;
import com.ssangyong.commands.ec.fncepl.model.FncEplCheckData;
import com.ssangyong.common.utils.CustomUtil;
import com.ssangyong.common.utils.DatasetService;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;

/**
 * Function EPL Check Operation
 * 
 * @author baek
 * 
 */
public class FncEplCheckCreateOperation extends AbstractAIFOperation {

	private FncEplCheckData inputData = null;
	private EventList<FncEplCheckData> tableDataList = null;
	private TCSession tcSession = null;

	public FncEplCheckCreateOperation(FncEplCheckData inputData, EventList<FncEplCheckData> tableDataList) {
		this.inputData = inputData;
		this.tableDataList = tableDataList;
		this.tcSession = CustomUtil.getTCSession();
	}

	@Override
	public void executeOperation() throws Exception {
		try {

			ArrayList<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();
			HashMap<String, String> dataMap = new HashMap<String, String>();

			TCComponentDataset attachDataSetComp = createDataset();
			String attachFilePuid = attachDataSetComp != null ? attachDataSetComp.getUid() : null;
			String rowPuid = getSysGuid();

			String prodName = inputData.getProdNo();

			String prodNo = prodName.substring(0, prodName.indexOf(" ("));

			String prodDispName = prodName.substring(prodName.indexOf(" (") + 2, prodName.length() - 1);

			dataMap.put("PUID", rowPuid);
			dataMap.put("FUNCTION_NO", inputData.getFunctionNo());
			dataMap.put("PROD_NO", prodNo);
			dataMap.put("PROD_NAME", prodDispName);
			dataMap.put("ECO_TYPE", inputData.getEcoType());
			dataMap.put("APPLY_ECO_NO", inputData.getApplyEcoNo());
			dataMap.put("ADD_ECO_PUBLISH", inputData.getAddEcoPublish());
			dataMap.put("DESCRIPTION", inputData.getDescription());
			dataMap.put("ATTACH_FILE_PUID", attachFilePuid);
			dataMap.put("REGISTER_ID", inputData.getRegisterId());
			dataMap.put("BASE_DATE", inputData.getBaseDate());
			dataList.add(dataMap);
			/**
			 * Function EPL Check Data 생성
			 */
			createFncEplCheck(dataList);
			/**
			 * 화면에 Row 추가
			 */

			SimpleDateFormat uiDateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Date toDay = new Date();
			String createDate = uiDateFormat.format(toDay);

			FncEplCheckData rowData = new FncEplCheckData();
			rowData.setFunctionNo(inputData.getFunctionNo().concat(prodNo));
			rowData.setProdNo(prodNo);
			rowData.setEcoType(inputData.getEcoType());
			rowData.setApplyEcoNo(inputData.getApplyEcoNo());
			rowData.setAddEcoPublish(inputData.getAddEcoPublish());
			rowData.setDescription(inputData.getDescription());
			rowData.setFncEplPuid(rowPuid);
			rowData.setAttachFilePuid(attachFilePuid);
			rowData.setBaseDate(inputData.getBaseDate());
			rowData.setCreateDate(createDate);
			rowData.setRegisterId(tcSession.getUser().getUserId());
			rowData.setIsLatestCheck("V");
			rowData.setProdDspName(prodDispName);
			rowData.setRowDataObj(rowData);

			tableDataList.add(0, rowData);

			MessageBox.post(AIFUtility.getActiveDesktop().getShell(), "Data was created successfully", "Information", MessageBox.INFORMATION);
		} catch (Exception ex) {
			MessageBox.post(AIFUtility.getActiveDesktop().getShell(), ex.toString(), "Error", MessageBox.ERROR);
		}
	}

	private TCComponentDataset createDataset() throws Exception {
		String attachFilePath = inputData.getAttachFilePath();
		if (attachFilePath == null)
			return null;
		DatasetService.createService(CustomUtil.getTCSession());
		TCComponentDataset dataSet = DatasetService.createDataset(attachFilePath);
		return dataSet;
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
	 * Function EPL DB Table 에 정보를 생성
	 * 
	 * @param dataList
	 * @throws Exception
	 */
	private void createFncEplCheck(ArrayList<HashMap<String, String>> dataList) throws Exception {
		CustomECODao dao = new CustomECODao();
		dao.insertFncEplCheck(dataList);
	}

}
