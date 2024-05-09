package com.kgm.commands.ec.fncepl.operation;

import java.util.ArrayList;
import java.util.HashMap;

import com.kgm.commands.ec.dao.CustomECODao;
import com.kgm.commands.ec.fncepl.model.FncEplCheckData;
import com.kgm.common.WaitProgressBar;
import com.kgm.common.remote.DataSet;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aifrcp.AIFUtility;

/**
 * Function EPL Check 리스트 조회
 * 
 * @author baek
 * 
 */
public class SearchFncEplCheckListOperation extends AbstractAIFOperation {
	private WaitProgressBar waitProgress;
	private FncEplCheckData inputData = null;

	public SearchFncEplCheckListOperation(FncEplCheckData inputData) {
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

	/**
	 * 조회
	 * 
	 * @throws Exception
	 */
	private void executeSearch() throws Exception {

		CustomECODao dao = new CustomECODao();
		DataSet ds = new DataSet();
		ds.put("PROD_NO", inputData.getProdNo());
		ds.put("LATEST_CHECKED", inputData.getIsLatestCheck() == null ? null : inputData.getIsLatestCheck());
		ds.put("FUNCTION_NO", inputData.getFunctionNo());
		ds.put("APPLY_ECO_NO", inputData.getApplyEcoNo());
		ds.put("ADD_ECO_PUBLISH", inputData.getAddEcoPublish());

		ArrayList<HashMap<String, Object>> resultList = dao.getFncEplCheckList(ds);

		for (HashMap<String, Object> rowMap : resultList) {
			int rowNumber = FncEplCheckData.objToInt(rowMap.get("RN"));
			String functionNo = FncEplCheckData.objToStr(rowMap.get("FUNCTION_NO"));
			String prodNo = FncEplCheckData.objToStr(rowMap.get("PROD_NO"));
			String baseDate = FncEplCheckData.objToStr(rowMap.get("BASE_DATE_STR"));
			String applyEcoNo = FncEplCheckData.objToStr(rowMap.get("APPLY_ECO_NO"));
			String addEcoPublish = FncEplCheckData.objToStr(rowMap.get("ADD_ECO_PUBLISH"));
			String description = FncEplCheckData.objToStr(rowMap.get("DESCRIPTION"));
			String createDate = FncEplCheckData.objToStr(rowMap.get("CREATE_DATE_STR"));
			String fncEplPuid = FncEplCheckData.objToStr(rowMap.get("PUID"));
			String attachFilePuid = FncEplCheckData.objToStr(rowMap.get("ATTACH_FILE_PUID"));
			String registerId = FncEplCheckData.objToStr(rowMap.get("REGISTER_ID"));
			String ecoType = FncEplCheckData.objToStr(rowMap.get("ECO_TYPE"));
			String prodDspName = FncEplCheckData.objToStr(rowMap.get("PROD_DP_NAME"));
			

			FncEplCheckData rowData = new FncEplCheckData();
			rowData.setFunctionNo(functionNo.concat(prodNo));
			rowData.setProdNo(prodNo);
			rowData.setEcoType(ecoType);
			rowData.setApplyEcoNo(applyEcoNo);
			rowData.setAddEcoPublish(addEcoPublish);
			rowData.setDescription(description);
			rowData.setFncEplPuid(fncEplPuid);
			rowData.setAttachFilePuid(attachFilePuid);
			rowData.setBaseDate(baseDate);
			rowData.setCreateDate(createDate);
			rowData.setRegisterId(registerId);
			rowData.setIsLatestCheck(rowNumber == 1 ? "V" : "");
			rowData.setProdDspName(prodDspName);
			rowData.setRowDataObj(rowData);
			inputData.getTableDataList().add(rowData);
		}

	}
}
