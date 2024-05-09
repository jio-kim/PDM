package com.kgm.commands.ec.fncepl.operation;

import java.util.ArrayList;
import java.util.HashMap;

import com.kgm.commands.ec.dao.CustomECODao;
import com.kgm.commands.ec.fncepl.model.FncEplCheckData;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.util.MessageBox;

/**
 * Function EPL Check 리스트 저장
 * @author baek
 *
 */
public class SaveFncEplCheckListOperation extends AbstractAIFOperation {
	private FncEplCheckData inputData = null;

	public SaveFncEplCheckListOperation(FncEplCheckData inputData) {
		this.inputData = inputData;
	}

	@Override
	public void executeOperation() throws Exception {
		try {
			executeSave();
			MessageBox.post(AIFUtility.getActiveDesktop().getShell(), "Data was saved successfully", "Information", MessageBox.INFORMATION);
		} catch (Exception ex) {
			MessageBox.post(AIFUtility.getActiveDesktop().getShell(), ex.toString(), "Error", MessageBox.ERROR);
		}

	}

	/**
	 * 저장
	 */
	private void executeSave() throws Exception {

		/**
		 * 삭제되는 Data 관련 DB Table 에서 Update 함
		 */
		deleteDataList();
	}

	private void deleteDataList() throws Exception {
		ArrayList<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();
		ArrayList<FncEplCheckData> deleteDataList = inputData.getDeleteDataList();
		for (FncEplCheckData data : deleteDataList) {
			HashMap<String, String> dataMap = new HashMap<String, String>();
			dataMap.put("PUID", data.getFncEplPuid());
			dataList.add(dataMap);
		}
		deleteFncEpl(dataList);
	}

	/**
	 * Function EPL Check 리스트 삭제
	 * 
	 * @param dataList
	 * @throws Exception
	 */
	private void deleteFncEpl(ArrayList<HashMap<String, String>> dataList) throws Exception {
		CustomECODao dao = new CustomECODao();
		dao.deleteFncEpl(dataList);
	}
}
