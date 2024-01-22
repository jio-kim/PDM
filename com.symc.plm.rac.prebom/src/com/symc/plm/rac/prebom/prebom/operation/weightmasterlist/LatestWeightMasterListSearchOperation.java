package com.symc.plm.rac.prebom.prebom.operation.weightmasterlist;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import com.ssangyong.commands.ospec.op.OpValueName;
import com.ssangyong.common.WaitProgressBar;
import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.remote.SYMCRemoteUtil;
import com.symc.plm.rac.prebom.masterlist.model.StoredOptionSet;
import com.symc.plm.rac.prebom.prebom.dialog.weightmasterlist.LatestWeightMasterListDialog.CustomTableModel;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.kernel.TCComponentItem;

/***********************************************************************************************************************************************
 * [변경이력]
 * 
 * 1. [2017.07.18][CHC][송대영차장요청] : E100 차량 무게 추가 과련 수정 : E100의 경우 TRANSMISSION이 없음. WHEEL OPTION만 가지고 무게를 확인해야 함.
 * 
 * 
 * *********************************************************************************************************************************************/

public class LatestWeightMasterListSearchOperation extends AbstractAIFOperation {
	private TCComponentItem productItem;
	private ArrayList<StoredOptionSet> selectedSOS;
	private HashMap<String, OpValueName> wtOption;
	private HashMap<String, OpValueName> tmOption;
	private HashMap<String, Vector<String>> tableDataMap = new HashMap<String, Vector<String>>();
	private WaitProgressBar waitBar;
	private JTable targetTable;
	private int defaultHeaderCount;
	private Vector<String> totalSumVector = new Vector<String>();
	private ScriptEngineManager manager = new ScriptEngineManager();
	private ScriptEngine engine = manager.getEngineByName("js");
	private HashMap<String, String> targetDataSet = null; // Project 대상 정보
	private HashMap<String, HashMap<String, Object>> bomLineTrimSet = null; // BOMLine의 Trim 정보. Key: SystemRowKey , Value:
	private int fmp_inx = 5;
	private int seq_inx = 6;
	private int smode_inx = 10;

	public LatestWeightMasterListSearchOperation(JTable table, TCComponentItem selectedProduct, ArrayList<StoredOptionSet> selectedSOS,
			HashMap<String, OpValueName> wtOption, HashMap<String, OpValueName> tmOption, HashMap<String, String> targetDataSet, int defaultHeaderCount,
			WaitProgressBar waitBar) {
		this.targetTable = table;
		this.productItem = selectedProduct;
		this.selectedSOS = selectedSOS;
		this.wtOption = wtOption;
		this.tmOption = tmOption;
		this.defaultHeaderCount = defaultHeaderCount;
		this.waitBar = waitBar;
		this.targetDataSet = targetDataSet;
	}

	@Override
	public void executeOperation() throws Exception {
		try {
			SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String latestCreateDate = targetDataSet.get("LATEST_M_CREATE_TIME");
			Date targetDate = sd.parse(latestCreateDate);

			ArrayList<StoredOptionSet> usageOptionSetList = new ArrayList<StoredOptionSet>();
			for (StoredOptionSet sos : selectedSOS) {
				for (String wtKey : wtOption.keySet()) {
					
					// [20170716][CHC]
					// [요청자 : 송대영 차장]
					// E100 PROJECT의 경우 전기차 이므로 TRANSMISSTION이 없다
					// tmOption.size() <= 0 인 경우는 전기차로 판단하고 Wheel Option만 가지고
					// Option Set을 구성한다.
					if(tmOption.size() <= 0) {
						
						StoredOptionSet copySOS = new StoredOptionSet(sos.getName());
						StoredOptionSet sosMandatoried = new StoredOptionSet(sos.getName());

						copySOS.addAll(sos.getOptionSet());
						
						ArrayList<String> wtList = new ArrayList<>();
						wtList.add(wtOption.get(wtKey).getOption());
						copySOS.getOptionSet().put(wtOption.get(wtKey).getCategory(), wtList);
						
						sosMandatoried = setMandatory(copySOS);

						usageOptionSetList.add(sosMandatoried);
					
					// 기존 소스	
					} else {
						for (String tmKey : tmOption.keySet()) {
							StoredOptionSet copySOS = new StoredOptionSet(sos.getName());
							StoredOptionSet sosMandatoried = new StoredOptionSet(sos.getName());

							copySOS.addAll(sos.getOptionSet());

							ArrayList<String> wtList = new ArrayList<>();
							wtList.add(wtOption.get(wtKey).getOption());
							copySOS.getOptionSet().put(wtOption.get(wtKey).getCategory(), wtList);
							wtList = new ArrayList<>();
							wtList.add(tmOption.get(tmKey).getOption());
							copySOS.getOptionSet().put(tmOption.get(tmKey).getCategory(), wtList);

							// [NoSR][20160321][jclee] Applying Pre OSpec Mandatory
							sosMandatoried = setMandatory(copySOS);

							usageOptionSetList.add(sosMandatoried);
						}	
					}
// 20170716 원본소스					
//					for (String tmKey : tmOption.keySet()) {
//						StoredOptionSet copySOS = new StoredOptionSet(sos.getName());
//						StoredOptionSet sosMandatoried = new StoredOptionSet(sos.getName());
//
//						copySOS.addAll(sos.getOptionSet());
//
//						ArrayList<String> wtList = new ArrayList<>();
//						wtList.add(wtOption.get(wtKey).getOption());
//						copySOS.getOptionSet().put(wtOption.get(wtKey).getCategory(), wtList);
//						wtList = new ArrayList<>();
//						wtList.add(tmOption.get(tmKey).getOption());
//						copySOS.getOptionSet().put(tmOption.get(tmKey).getCategory(), wtList);
//
//						// [NoSR][20160321][jclee] Applying Pre OSpec Mandatory
//						sosMandatoried = setMandatory(copySOS);
//
//						usageOptionSetList.add(sosMandatoried);
//					}
					
					
				}

				usageOptionSetList.add(null);
			}

			// 1. BOMLine을 얻어온다. 및 LatestReleased 로 설정되도록 한다.
			setMsg("create BOM for " + targetDate.toString() + ".");
			try {
				setMsg("Working for total weight and load display.");
				/**
				 * FIXME:
				 */
				/**
				 * BOMLine 의 Trim 정보를 가져옴
				 */
				getBOMLineTrimList();
				/**
				 * BOM 정보를 Load 함
				 */
				loadBOMDataList(usageOptionSetList);

				int totalSumVectorCount = usageOptionSetList.size();
				
//				ArrayList<String> scodeListToEnableSum = WeightMasterListSearchOperation.getSCodeListToEnableSum();

				totalSumVector.clear();
				totalSumVector.setSize(totalSumVectorCount);
				for (Vector<String> rowVector : tableDataMap.values()) {
					if (rowVector.get(defaultHeaderCount - 1).equals("0.0"))
						continue;

					int endColumnIndex = usageOptionSetList.size() + defaultHeaderCount;
//					String sMode = rowVector.get(smode_inx);
	                /**
	                 * 합산을 할 수 있는 S/CODE 일경우 만 합산
	                 */
					//[20190604][CSH]합산할 수 있는 S/Code만 보여지도록 개선하였으므로 여기서는 Skip
//	                if(!scodeListToEnableSum.contains(sMode))
//	                	continue;
	                
					for (int i = defaultHeaderCount; i < endColumnIndex; i++) {
						int sumColIndex = i - defaultHeaderCount;
						BigDecimal sumDecimal;
						if (totalSumVector.get(sumColIndex) == null)
							sumDecimal = new BigDecimal("0.0");
						else
							sumDecimal = new BigDecimal(totalSumVector.get(sumColIndex));

						BigDecimal newValue;
						int usageIndex = sumColIndex > usageOptionSetList.size() - 1 ? sumColIndex - usageOptionSetList.size() : sumColIndex;
						if (usageOptionSetList.get(usageIndex) == null) {
							newValue = rowVector.get(i) == null ? new BigDecimal("0.0") : sumDecimal.add(new BigDecimal(rowVector.get(i)));
						} else {
							newValue = rowVector.get(i) == null ? new BigDecimal("0.0") : sumDecimal.add((new BigDecimal(rowVector.get(i))).multiply(new BigDecimal(rowVector.get(defaultHeaderCount - 1))));
						}

						totalSumVector.set(sumColIndex, newValue.toString());
					}
				}

				Vector<Vector<String>> dataVector = new Vector<Vector<String>>();
				dataVector.addAll(tableDataMap.values());

				Vector<String> totalSumRow = new Vector<String>();

				totalSumRow.addAll(Arrays.asList(new String[] { "0", "", "", "", "", "", "", "", "", "Total Sum Weight", "", "", "", "" }));
				totalSumRow.addAll(totalSumVector);

				if (dataVector.size() > 0)
					dataVector.insertElementAt(totalSumRow, 0);

				Collections.sort(dataVector, new TableDataComparator());

				Vector<Object> headerIdentifier = ((CustomTableModel) targetTable.getModel()).getIdentifier();

				ArrayList<TableCellRenderer> cellRenderers = new ArrayList<TableCellRenderer>();
				ArrayList<Integer> columnWidths = new ArrayList<Integer>();
				ArrayList<TableCellRenderer> headerRenderers = new ArrayList<TableCellRenderer>();
				for (int i = 0; i < targetTable.getColumnModel().getColumnCount(); i++) {
					cellRenderers.add(targetTable.getColumnModel().getColumn(i).getCellRenderer());
					columnWidths.add(targetTable.getColumnModel().getColumn(i).getWidth());
					headerRenderers.add(targetTable.getColumnModel().getColumn(i).getHeaderRenderer());
				}

				((CustomTableModel) targetTable.getModel()).setDataVector(dataVector, headerIdentifier);

				for (int i = 0; i < targetTable.getColumnModel().getColumnCount(); i++) {
					targetTable.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderers.get(i));
					targetTable.getColumnModel().getColumn(i).setPreferredWidth(columnWidths.get(i));
					targetTable.getColumnModel().getColumn(i).setCellRenderer(cellRenderers.get(i));
				}
				targetTable.repaint();

			} catch (Exception ex) {
				throw ex;
			}
		} catch (Exception ex) {
			storeOperationResult(ex.getMessage());
			throw ex;
		}

	}

	private void loadBOMDataList(ArrayList<StoredOptionSet> sosList) throws Exception {
		List<HashMap<String, Object>> masterDataList = getWeightMasterDataList();
		String smode = "";
		Vector<String> rowVector;
		String deptName = "";
		String userName = "";
		String sysCode = "";
		String fmp = "";
		String seqNo = "";
		BigDecimal levMValue;
		String itemId = "";
		String partNo = "";
		String partName = "";
		String curVCondition = "";
		BigDecimal actWeightBig;
		String actWeight = "";
		String estWeight = "";
		String parentID = "";
		String projectCode = "";
		String nmcd = "";
		String sysName = "";
		String partWeight = "0";
		String levM = "";
		String systemRowKey = "";
		String sKey = "";
		ArrayList<String> scodeListToEnableSum = WeightMasterListSearchOperation.getSCodeListToEnableSum();
		
		for (HashMap<String, Object> masterData : masterDataList) {
			smode = (String) masterData.get("SMODE");
			// P8은 체크하지 않는다.
//			if ("P8".equals(smode)){
			// [20190604][CSH]합산에 포함되지 않는 파트는 리스트에도 안나오게 해달라네.
			if(!scodeListToEnableSum.contains(smode)){
				continue;
			}
						
			rowVector = new Vector<String>();
			deptName = (String) masterData.get("ENG_DEPT_NM");
			userName = (String) masterData.get("USER_NAME");
			sysCode = (String) masterData.get("SYS_CODE");
			fmp = (String) masterData.get("PARENT_NO");
			seqNo = (String) masterData.get("SEQ");
			levMValue = (BigDecimal) masterData.get("LEV_M");
			itemId = (String) masterData.get("CHILD_UNIQUE_NO");
			partNo = (String) masterData.get("CHILD_NO");
			partName = (String) masterData.get("CHILD_NAME");
			curVCondition = (String) masterData.get("VC");
			actWeightBig = (BigDecimal) masterData.get("ACT_WEIGHT");
			actWeight = actWeightBig == null ? null : actWeightBig.toString();
			estWeight = (String) masterData.get("EST_WEIGHT");
			parentID = (String) masterData.get("PARENT_NO");
			projectCode = (String) masterData.get("PROJECT");
			nmcd = (String) masterData.get("CHG_TYPE_ENGCONCEPT");
			sysName = (String) masterData.get("SYSTEM_NAME");
			partWeight = "0";
			levM = Integer.toString(levMValue.intValue());
			systemRowKey = (String) masterData.get("SYSTEM_ROW_KEY");

//			double partWeight = 0;
			sKey = itemId + "#" + fmp + "#" + parentID + "#" + seqNo;
			
			if (actWeight != null && actWeight.trim().length() > 0 && !actWeight.equals("0")) {
				partWeight = actWeight;
//				partWeight = Double.valueOf(actWeight);
			} else if (estWeight != null && estWeight.trim().length() > 0) {
				partWeight = estWeight;
//				partWeight = Double.valueOf(estWeight);
			}

			rowVector.add((tableDataMap.keySet().size() + 1) + "");
			rowVector.add(deptName);
			rowVector.add(userName);
			rowVector.add(sysCode == null ? "X00" : sysCode);
			rowVector.add(sysName);
			rowVector.add(fmp);
			rowVector.add(seqNo);
			rowVector.add(levM);
			rowVector.add(partNo);
			rowVector.add(partName);
			rowVector.add(smode);
			rowVector.add(projectCode);
			rowVector.add(nmcd);
			rowVector.add(partWeight);
//			rowVector.add(partWeight + "");

			int usageIndex = defaultHeaderCount;
			boolean bIsVCInclude = false;
			double doubleWeight = Double.valueOf(partWeight);
			for (StoredOptionSet curSOS : sosList) {
				if (curSOS == null) {
					try {
						if (tableDataMap.containsKey(sKey)) {
							try {
								rowVector = tableDataMap.get(sKey);
								doubleWeight = Double.valueOf(rowVector.get(defaultHeaderCount - 1));
//								partWeight = Double.valueOf(rowVector.get(defaultHeaderCount - 1));
							} catch (Exception ex) {
								throw ex;
							}
						}

						BigDecimal newValue;
						try {
							newValue = (rowVector.get(usageIndex - 1) == null) ? new BigDecimal("0.0") : new BigDecimal(rowVector.get(usageIndex - 1)).multiply(BigDecimal.valueOf(doubleWeight));
//							newValue = (rowVector.get(usageIndex - 1) == null) ? new BigDecimal("0.0") : new BigDecimal(rowVector.get(usageIndex - 1)).multiply(BigDecimal.valueOf(partWeight));
						} catch (Exception ex) {
							throw ex;
						}
						try {
							if (rowVector.size() - 1 < usageIndex)
								rowVector.add(newValue.toString());
							else
								rowVector.set(usageIndex, newValue.toString());
						} catch (Exception ex) {
							throw ex;
						}
					} catch (Exception ex) {
						throw ex;
					}
				} else {

					String ssoName = curSOS.getName().replace("_STD", "");
					boolean isExistBOMLineTrim = false; // BOM Trim 이 존재 유무
					String quantity = "1";
					if (bomLineTrimSet.containsKey(systemRowKey)) {
						HashMap<String, Object> trimData = bomLineTrimSet.get(systemRowKey);
						String trimList = (String) trimData.get("TRIM_LIST");
						BigDecimal usageQty = (BigDecimal) trimData.get("USAGE_QTY");
						quantity = Integer.toString(usageQty.intValue());
						if (trimList.indexOf(ssoName) > -1)
							isExistBOMLineTrim = true;
					}

					boolean isInclude = false;
					/**
					 * Variant Condition이 없는 경우
					 */
					if (curVCondition == null) {
						if (isExistBOMLineTrim) {
							bIsVCInclude = true;
							rowVector.add(quantity);
						} else
							rowVector.add("0");
					} else {
						isInclude = curSOS.isInclude(engine, curVCondition);
						if (isInclude) {
							bIsVCInclude = true;
							if (tableDataMap.containsKey(sKey)) {
								rowVector = tableDataMap.get(sKey);
								if (rowVector.size() > usageIndex) {
									rowVector.set(usageIndex, (Integer.valueOf(rowVector.get(usageIndex)) + 1) + "");
								} else {
									rowVector.add(quantity);
								}
							} else {
								rowVector.add(quantity);
							}
						} else {
							rowVector.add("0");
						}
					}
				}
				usageIndex++;
			}

			if (!bIsVCInclude)
				continue;
			if (tableDataMap.containsKey(sKey))
				continue;
			tableDataMap.put(sKey, rowVector);

		}
	}

	/**
	 * [NoSR][20160315][jclee] 송대영책임 요청 SOS에 Pre OSpec Mandatory 적용
	 * 
	 * @param copySOS
	 * @return
	 */
	private StoredOptionSet setMandatory(StoredOptionSet sos) throws Exception {
		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByName("js");

		StoredOptionSet sosMandatoried = new StoredOptionSet(sos.getName());
		sosMandatoried.addAll(sos.getOptionSet());

		// Select OSpec, Trim Mandatory Option
		String sOSpecNo = "";
		String sTrim = "";

		sOSpecNo = productItem.getLatestItemRevision().getProperty("s7_OSPEC_NO");
		sTrim = sos.getName().substring(0, sos.getName().indexOf('_'));

		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		DataSet ds = new DataSet();
		ds.setString("OSPEC_NO", sOSpecNo);
		ds.setString("TRIM", sTrim);

		@SuppressWarnings("unchecked")
		ArrayList<HashMap<String, String>> alMandatories = (ArrayList<HashMap<String, String>>) remote.execute("com.ssangyong.service.PreOSpecService",
				"selectPreOSpecMandatory", ds);

		// Applying Mandatories
		for (int inx = 0; inx < alMandatories.size(); inx++) {
			HashMap<String, String> hmMandatory = alMandatories.get(inx);
			String sOptionCategory = hmMandatory.get("OPTION_CATEGORY");
			String sOptionValue = hmMandatory.get("OPTION_VALUE");
			String sRemarkType = hmMandatory.get("REMARK_TYPE");
			String sRemark = hmMandatory.get("REMARK");

			if (sRemarkType.equals("Available IF") && sosMandatoried.isInclude(engine, sRemark)) {
				// 해당 Category의 Option Value를 Mandatory Option으로 교체
				sosMandatoried.replaceOptionValue(sOptionCategory, sOptionValue);
			} else if (sRemarkType.equals("NOT Available IF") && sosMandatoried.isInclude(engine, sRemark)) {
				// 해당 Category를 사양에서 제외
				sosMandatoried.removeOptionCategory(sOptionCategory);
			}
		}

		return sosMandatoried;
	}

	private synchronized void setMsg(String msg) {
		if (waitBar != null) {
			waitBar.setStatus(msg);
		}
	}

	/**
	 * 최신 Total Weight Master List 대상 정보를 가져옴
	 * 
	 * @param prodRevision
	 * @throws Exception
	 */
	private List<HashMap<String, Object>> getWeightMasterDataList() throws Exception {
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		DataSet ds = new DataSet();
		ds.put("PROJECT_NO", targetDataSet.get("PROJECT_NO"));
		ds.put("EAI_CREATE_TIME", targetDataSet.get("MASTER_CREATE_TIME"));

		@SuppressWarnings("unchecked")
		List<HashMap<String, Object>> result = (List<HashMap<String, Object>>) remote.execute("com.ssangyong.service.MasterListService",
				"getWeightMasterDataList", ds);
		return result;
	}

	/**
	 * FIXME: OPT 타입도 가져와야 하는지 확인
	 * 
	 * @return
	 * @throws Exception
	 */
	private void getBOMLineTrimList() throws Exception {
		bomLineTrimSet = new HashMap<String, HashMap<String, Object>>();
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		DataSet ds = new DataSet();
		ds.put("PROJECT_NO", targetDataSet.get("PROJECT_NO"));
		ds.put("EAI_CREATE_TIME", targetDataSet.get("MASTER_CREATE_TIME"));

		@SuppressWarnings("unchecked")
		List<HashMap<String, Object>> result = (List<HashMap<String, Object>>) remote.execute("com.ssangyong.service.MasterListService", "getBOMLineTrimList",
				ds);
		for (HashMap<String, Object> data : result) {
			String systemRowKey = (String) data.get("SYSTEM_ROW_KEY");
			bomLineTrimSet.put(systemRowKey, data);
		}
	}

	/**
	 * FMP, SEQ 로 Sort 함
	 */
	private class TableDataComparator implements Comparator<Vector<String>> {

		@Override
		public int compare(Vector<String> paramT1, Vector<String> paramT2) {

			String fmpNo1 = paramT1.get(fmp_inx);
			String seq1 = paramT1.get(seq_inx);

			String fmpNo2 = paramT2.get(fmp_inx);
			String seq2 = paramT2.get(seq_inx);

			int firstCompare = fmpNo1.compareTo(fmpNo2);
			if (firstCompare != 0)
				return firstCompare;
			else
				return seq1.compareTo(seq2);
		}

	}

}
