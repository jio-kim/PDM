package com.kgm.commands.namegroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;

import com.kgm.commands.namegroup.model.PngWeeklyReportData;
import com.kgm.common.WaitProgressBar;
import com.kgm.common.remote.DataSet;
import com.kgm.common.remote.SYMCRemoteUtil;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aif.InterfaceAIFOperationListener;
import com.teamcenter.rac.util.MessageBox;

/**
 * 주간 오류 리포트 검색 Operation
 * 
 * @author baek
 * 
 */
public class PngWeeklySearchOperation extends AbstractAIFOperation implements InterfaceAIFOperationListener {

	private WaitProgressBar progressBar = null;
	private PngWeeklyReportData data = null;
	private PngDlg parentDialog = null;

	public PngWeeklySearchOperation(PngWeeklyReportData data, PngDlg parentDialog) {
		this.data = data;
		this.parentDialog = parentDialog;
		this.addOperationListener(this);
	}

	@Override
	public void executeOperation() throws Exception {
		try {
			progressBar = new WaitProgressBar(parentDialog);
			progressBar.start();
			progressBar.setStatus("Searching Data....");

			/**
			 * 에러 결과 리스트를 조회함
			 */
			ArrayList<HashMap<String, Object>> resultList = retrieveResultList();

			/**
			 * 에러 결과를 화면 UI Table Load함
			 */
			loadData(resultList);

		} catch (Exception ex) {
			setAbortRequested(true);
			MessageBox.post(parentDialog, ex.toString(), "ERROR", MessageBox.ERROR);
			if (progressBar != null)
				progressBar.close();
		}
	}

	/**
	 * 에러 리스트를 조회함
	 * 
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private ArrayList<HashMap<String, Object>> retrieveResultList() throws Exception {
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		DataSet ds = new DataSet();

		ds.put("PRODUCT_NO", data.getProductNo());
		ds.put("GROUP_ID", data.getGroupId());
		ds.put("SPEC_NO", data.getSpecNo());
		ds.put("CREATION_DATE_FROM", data.getFromDate());
		ds.put("CREATION_DATE_TO", data.getEndDate());

		ArrayList<HashMap<String, Object>> resultList = (ArrayList<HashMap<String, Object>>) remote.execute("com.kgm.service.PartNameGroupService", "getPngWeeklyErrorReport", ds);
		return resultList;
	}

	/**
	 * Data를 화면 UI 테이블에 Load 함
	 * 
	 * @param resultList
	 */
	private void loadData(ArrayList<HashMap<String, Object>> resultList) throws Exception {
		DefaultTableModel tableModel = (DefaultTableModel) data.getResultTable().getModel();
		for (HashMap<String, Object> valueMap : resultList) {
			//old
//			String reason = valueMap.get("REASON") == null ? null : ((String) valueMap.get("REASON")).replace("&lt;", "<").replace("&gt;", ">");
//
//			Vector<Object> rowData = new Vector<Object>();
//			rowData.add(valueMap.get("PRODUCT_NO"));
//			rowData.add(valueMap.get("GROUP_NO"));
//			rowData.add(valueMap.get("GROUP_NAME"));
//			String spec_no = valueMap.get("SPEC_NO").toString();
//			System.out.println(spec_no);
//			rowData.add(spec_no);
//			rowData.add(reason);
//			rowData.add(valueMap.get("CREATION_DATE"));
//			rowData.add(getModelYear(spec_no));
//
//			tableModel.addRow(rowData);
			
			//[CSH][SR181025-028]ModelYear 찾는 로직 변경 (쿼리에서...)
			String reason = valueMap.get("REASON") == null ? null : ((String) valueMap.get("REASON")).replace("&lt;", "<").replace("&gt;", ">");

			Vector<Object> rowData = new Vector<Object>();
			rowData.add(valueMap.get("PRODUCT_NO"));
			rowData.add(valueMap.get("GROUP_NO"));
			rowData.add(valueMap.get("GROUP_NAME"));
			String spec_no = valueMap.get("SPEC_NO").toString();
//			System.out.println(spec_no);
			rowData.add(spec_no);
			rowData.add(reason);
			rowData.add(valueMap.get("CREATION_DATE"));
			rowData.add(valueMap.get("MY"));

			tableModel.addRow(rowData);
		}
	}
	
	/**
	 * 해당 스펙의 옵션 중 M/Y 옵션을 가져옴
	 * @Copyright : Plmsoft
	 * @author : 이정건
	 * @since  : 2017. 6. 14.
	 * @return
	 * @throws Exception
	 */
	private String getModelYear(String spec_no) throws Exception {
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		DataSet ds = new DataSet();
		ds.put("SPEC_NO", spec_no);

		ArrayList<String> resultList = (ArrayList<String>) remote.execute("com.kgm.service.PartNameGroupService", "getModelYear", ds);
		if(resultList == null || resultList.size() <= 0){
			return "M/Y not exist";
		}
		return resultList.get(0);
	}

	@Override
	public void endOperation() {
		if (progressBar != null)
			progressBar.close();
	}

	@Override
	public void startOperation(String arg0) {
		// TODO Auto-generated method stub

	}

}
