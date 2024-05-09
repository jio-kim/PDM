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

import com.kgm.common.remote.DataSet;
import com.kgm.common.remote.SYMCRemoteUtil;
import com.kgm.commands.ospec.op.OpValueName;
import com.kgm.common.WaitProgressBar;
import com.symc.plm.rac.prebom.masterlist.model.StoredOptionSet;
import com.symc.plm.rac.prebom.prebom.dialog.weightmasterlist.LatestWeightMasterListDialog.CustomTableModel;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.kernel.TCComponentItem;

/***********************************************************************************************************************************************
 * [�����̷�]
 * 
 * 1. [2017.07.18][CHC][�۴뿵�����û] : E100 ���� ���� �߰� ���� ���� : E100�� ��� TRANSMISSION�� ����. WHEEL OPTION�� ������ ���Ը� Ȯ���ؾ� ��.
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
	private HashMap<String, String> targetDataSet = null; // Project ��� ����
	private HashMap<String, HashMap<String, Object>> bomLineTrimSet = null; // BOMLine�� Trim ����. Key: SystemRowKey , Value:
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
					// [��û�� : �۴뿵 ����]
					// E100 PROJECT�� ��� ������ �̹Ƿ� TRANSMISSTION�� ����
					// tmOption.size() <= 0 �� ���� �������� �Ǵ��ϰ� Wheel Option�� ������
					// Option Set�� �����Ѵ�.
					if(tmOption.size() <= 0) {
						
						StoredOptionSet copySOS = new StoredOptionSet(sos.getName());
						StoredOptionSet sosMandatoried = new StoredOptionSet(sos.getName());

						copySOS.addAll(sos.getOptionSet());
						
						ArrayList<String> wtList = new ArrayList<>();
						wtList.add(wtOption.get(wtKey).getOption());
						copySOS.getOptionSet().put(wtOption.get(wtKey).getCategory(), wtList);
						
						sosMandatoried = setMandatory(copySOS);

						usageOptionSetList.add(sosMandatoried);
					
					// ���� �ҽ�	
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
// 20170716 �����ҽ�					
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

			// 1. BOMLine�� ���´�. �� LatestReleased �� �����ǵ��� �Ѵ�.
			setMsg("create BOM for " + targetDate.toString() + ".");
			try {
				setMsg("Working for total weight and load display.");
				/**
				 * FIXME:
				 */
				/**
				 * BOMLine �� Trim ������ ������
				 */
				getBOMLineTrimList();
				/**
				 * BOM ������ Load ��
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
	                 * �ջ��� �� �� �ִ� S/CODE �ϰ�� �� �ջ�
	                 */
					//[20190604][CSH]�ջ��� �� �ִ� S/Code�� ���������� �����Ͽ����Ƿ� ���⼭�� Skip
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
			// P8�� üũ���� �ʴ´�.
//			if ("P8".equals(smode)){
			// [20190604][CSH]�ջ꿡 ���Ե��� �ʴ� ��Ʈ�� ����Ʈ���� �ȳ����� �ش޶��.
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
					boolean isExistBOMLineTrim = false; // BOM Trim �� ���� ����
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
					 * Variant Condition�� ���� ���
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
	 * [NoSR][20160315][jclee] �۴뿵å�� ��û SOS�� Pre OSpec Mandatory ����
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
		ArrayList<HashMap<String, String>> alMandatories = (ArrayList<HashMap<String, String>>) remote.execute("com.kgm.service.PreOSpecService",
				"selectPreOSpecMandatory", ds);

		// Applying Mandatories
		for (int inx = 0; inx < alMandatories.size(); inx++) {
			HashMap<String, String> hmMandatory = alMandatories.get(inx);
			String sOptionCategory = hmMandatory.get("OPTION_CATEGORY");
			String sOptionValue = hmMandatory.get("OPTION_VALUE");
			String sRemarkType = hmMandatory.get("REMARK_TYPE");
			String sRemark = hmMandatory.get("REMARK");

			if (sRemarkType.equals("Available IF") && sosMandatoried.isInclude(engine, sRemark)) {
				// �ش� Category�� Option Value�� Mandatory Option���� ��ü
				sosMandatoried.replaceOptionValue(sOptionCategory, sOptionValue);
			} else if (sRemarkType.equals("NOT Available IF") && sosMandatoried.isInclude(engine, sRemark)) {
				// �ش� Category�� ��翡�� ����
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
	 * �ֽ� Total Weight Master List ��� ������ ������
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
		List<HashMap<String, Object>> result = (List<HashMap<String, Object>>) remote.execute("com.kgm.service.MasterListService",
				"getWeightMasterDataList", ds);
		return result;
	}

	/**
	 * FIXME: OPT Ÿ�Ե� �����;� �ϴ��� Ȯ��
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
		List<HashMap<String, Object>> result = (List<HashMap<String, Object>>) remote.execute("com.kgm.service.MasterListService", "getBOMLineTrimList",
				ds);
		for (HashMap<String, Object> data : result) {
			String systemRowKey = (String) data.get("SYSTEM_ROW_KEY");
			bomLineTrimSet.put(systemRowKey, data);
		}
	}

	/**
	 * FMP, SEQ �� Sort ��
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
