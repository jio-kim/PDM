package com.kgm.commands.weight;



import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import com.kgm.commands.ospec.op.OSpec;
import com.kgm.commands.ospec.op.OpCategory;
import com.kgm.commands.ospec.op.OpValueName;
import com.kgm.commands.ospec.op.Option;
import com.kgm.commands.weight.EBOMWeightMasterListDialog.CustomTableModel;
import com.kgm.common.WaitProgressBar;
import com.kgm.common.remote.DataSet;
import com.kgm.common.remote.SYMCRemoteUtil;
import com.kgm.common.utils.CustomUtil;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.kernel.TCComponentItem;

/**
 * [SR170707-024] E-BOM Weight Report 개발 요청
 * @Copyright : Plmsoft
 * @author   : 이정건
 * @since    : 2017. 7. 10.
 * Package ID : com.kgm.commands.weight.EBOMWeightMasterListSearchOperation.java
 */
public class EBOMWeightMasterListSearchOperation extends AbstractAIFOperation {
	private TCComponentItem productItem;
	private StoredOptionSet sos;
	private HashMap<String, OpValueName> wtOption;
	private HashMap<String, OpValueName> tmOption;
	private HashMap<String, Vector<String>> tableDataMap = new HashMap<String, Vector<String>>();
	private JTable targetTable;
	private int defaultHeaderCount;
	private Vector<String> totalSumVector = new Vector<String>();
	private ScriptEngineManager manager = new ScriptEngineManager();
	private ScriptEngine engine = manager.getEngineByName("js");
	private HashMap<String, HashMap<String, String>> bomLineTrimSet = null; // BOMLine의 Trim 정보. Key: SystemRowKey , Value:
	private ArrayList<String> trim_list = null;
	private EBOMWeightMasterListDialog dialog;
	private OSpec ospec = null;
	private String selectedTrim = "";
	private ArrayList<StoredOptionSet> usageOptionSetList = null;
	
	public EBOMWeightMasterListSearchOperation(EBOMWeightMasterListDialog dialog, JTable table, TCComponentItem selectedProduct, StoredOptionSet selectedSOS, ArrayList<StoredOptionSet> usageOptionSetList, HashMap<String, OpValueName> wtOption, HashMap<String, OpValueName> tmOption, int defaultHeaderCount, WaitProgressBar waitBar) {
		this.targetTable = table;
		this.productItem = selectedProduct;
		this.sos = selectedSOS;
		this.ospec = ospec;
		this.selectedTrim = selectedTrim;
		this.wtOption = wtOption;
		this.tmOption = tmOption;
		this.defaultHeaderCount = defaultHeaderCount;
		this.dialog = dialog;
		this.usageOptionSetList = usageOptionSetList;
	}
	
	@Override
	public void executeOperation() throws Exception {
		try {
			/**
			HashMap<String, HashMap<String, OpCategory>> trimCategoryMap = ospec.getCategory();
			productItem.tr
			HashMap<String, OpCategory> categoryMap = trimCategoryMap.get( trimName );
			**/

			
			/**	
			Set categoryKeySet = categoryMap.keySet();
			
			for (String wtKey : wtOption.keySet()) {
				for (String tmKey : tmOption.keySet()) {
					StoredOptionSet copySOS = new StoredOptionSet(sos.getName());
					StoredOptionSet sosMandatoried = new StoredOptionSet(sos.getName());

					copySOS.addAll(sos.getOptionSet());

					ArrayList<String> wtList = new ArrayList<String>();
					wtList.add(wtOption.get(wtKey).getOption());
					copySOS.getOptionSet().put(wtOption.get(wtKey).getCategory(), wtList);
					wtList = new ArrayList<String>();
					wtList.add(tmOption.get(tmKey).getOption());
					copySOS.getOptionSet().put(tmOption.get(tmKey).getCategory(), wtList);

					sosMandatoried = setMandatory(copySOS);

					if(sosMandatoried != null){
						usageOptionSetList.add(sosMandatoried);
						usageOptionSetList.add(sosMandatoried);
					}
				}
			}
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

//			ArrayList<String> scodeListToEnableSum = EBOMWeightMasterListSearchOperation.getSCodeListToEnableAllSum();
			
			/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//			 값 바꿔치기 
//			Vector<String> rowNo = setTeamWeightAndgetRowNo();
			///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

			totalSumVector.clear();
			totalSumVector.setSize(totalSumVectorCount);
			for (Vector<String> rowVector : tableDataMap.values()) {
				//				if (rowVector.get(defaultHeaderCount - 1).equals("0.0") || rowVector.get(defaultHeaderCount - 1).equals("0"))
				//					continue;

				int endColumnIndex = usageOptionSetList.size() + defaultHeaderCount ;
				String sMode = rowVector.get(9);
				/**
				 * 합산을 할 수 있는 S/CODE 일경우 만 합산
				 */
//				if(!scodeListToEnableSum.contains(sMode))
//					continue;
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//				for (int i = defaultHeaderCount ; i < endColumnIndex; i++) {
//					int sumColIndex = i - defaultHeaderCount;
//					BigDecimal sumDecimal;
//					if (totalSumVector.get(sumColIndex) == null)
//						sumDecimal = new BigDecimal("0.0");
//					else
//						// 이부분 에러
//						sumDecimal = new BigDecimal(totalSumVector.get(sumColIndex) );
//
//					BigDecimal newValue = null;
//					int usageIndex = sumColIndex > usageOptionSetList.size() - 1 ? sumColIndex - usageOptionSetList.size() : sumColIndex;
//
//					if (usageOptionSetList.get(usageIndex) == null) {
//						newValue = rowVector.get(i) == null ? new BigDecimal("0.0") : sumDecimal.add(new BigDecimal(rowVector.get(i)));
//					} else {
//						if(i%2 != 0){
//							newValue = rowVector.get(i) == null ? new BigDecimal("0.0") : sumDecimal.add((new BigDecimal(rowVector.get(i))).multiply(new BigDecimal(rowVector.get(defaultHeaderCount - 1))));
////							newValue = rowVector.get(i) == null ? new BigDecimal("0.0") : sumDecimal.add((new BigDecimal(rowVector.get(i))).multiply(new BigDecimal(rowVector.get(defaultHeaderCount - 1))));
//						}
////						else{
////							newValue = rowVector.get(i) == null ? new BigDecimal("0.0") : sumDecimal.add((new BigDecimal(rowVector.get(i))).multiply(new BigDecimal(rowVector.get(defaultHeaderCount - 1))));	
//////							newValue = rowVector.get(i) == null ? new BigDecimal("0.0") : sumDecimal.add((new BigDecimal(rowVector.get(i))).multiply(new BigDecimal(rowVector.get(defaultHeaderCount - 2))));
////							newVal
////						}
//					}
//					
//					
////					if(i % 2 != 0) {
////						
////					}
//
//					totalSumVector.set(sumColIndex, newValue == null ? String.valueOf( new BigDecimal("0.0") ) : newValue.toString());
//				}
				
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////				
				for (int i = defaultHeaderCount ; i < endColumnIndex; i++) {
					int sumColIndex = i - defaultHeaderCount;
					BigDecimal newValue = new BigDecimal("0.0");
					
					BigDecimal sumDecimal = new BigDecimal("0.0");
					if (totalSumVector.get(sumColIndex) == null)
						sumDecimal = new BigDecimal("0.0");
					else if( !totalSumVector.get(sumColIndex).equals(""))
						// 이부분 에러
						sumDecimal = new BigDecimal(totalSumVector.get(sumColIndex) );
					
					if( i % 2 != 0 && i != (endColumnIndex - 1)) {
						newValue = rowVector.get(i) == null ? new BigDecimal("0.0") : sumDecimal.add((new BigDecimal(rowVector.get(i))).multiply(new BigDecimal(rowVector.get(defaultHeaderCount - 1))));
						totalSumVector.set(sumColIndex, newValue == null ? String.valueOf( new BigDecimal("0.0") ) : newValue.toString());
					} else if ( i == (endColumnIndex - 1) ) {
//						newValue = rowVector.get(i) == null ? new BigDecimal("0.0") : sumDecimal.add((new BigDecimal(rowVector.get(i))));
						newValue = rowVector.get(i) == null ? new BigDecimal("0.0") : sumDecimal.add((new BigDecimal(rowVector.get(i))).multiply(new BigDecimal(rowVector.get(defaultHeaderCount - 1))));
						totalSumVector.set(sumColIndex, newValue == null ? String.valueOf( new BigDecimal("0.0") ) : newValue.toString());
					}else {
						totalSumVector.set(sumColIndex, "" );
					}
				}
				
				
				
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				
			}

			Vector<Vector<String>> dataVector = new Vector<Vector<String>>();
			
			dataVector.addAll(tableDataMap.values());

			Vector<String> totalSumRow = new Vector<String>();

			totalSumRow.addAll(Arrays.asList(new String[] { "0", "", "", "", "", "", "", "","Total Sum Weight", "", "", "" }));
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

			for (int i = 0; i < targetTable.getTableHeader().getColumnModel().getColumnCount(); i++) {
				targetTable.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderers.get(i));
				targetTable.getColumnModel().getColumn(i).setPreferredWidth(columnWidths.get(i));
				targetTable.getColumnModel().getColumn(i).setCellRenderer(cellRenderers.get(i));
			}
			
			///////////////////////////////////////////////////////////////////////////////////////////////////////////
			// Team 별 중량 달라진 값의 글자색 변경
//			for( int i = 0; i < rowNo.size(); i++ ) {
//				String findRowNoKey = rowNo.get(i);  
//				int rowNoInt = 0;
//				for( int k = 0; k < targetTable.getModel().getRowCount(); k++ ) {
//					
//					String function = (String)targetTable.getModel().getValueAt(k, 4);
//					String seq      = (String)targetTable.getModel().getValueAt(k, 5);
//					String level    = (String)targetTable.getModel().getValueAt(k, 6);
//					String partNo   = (String)targetTable.getModel().getValueAt(k, 7);
//					
//					String tableRowNo = function + "#" + seq + "#" + level + "#" + partNo;
//					
//					if( findRowNoKey.equals(tableRowNo)) {
//						rowNoInt = k;
//						System.out.println("팀별 중량 달라진 row:" + rowNoInt);
//						break;
//					}
//				}
//				
//				
//				for( int j = defaultHeaderCount ; j <  targetTable.getColumnModel().getColumnCount(); j ++) {
//					if ( j % 2 == 0) {
//						TableCellRenderer cellRenderer = (TableCellRenderer) targetTable.getCellRenderer(rowNoInt, j);
////						cellRenderer.setForeground(Color.RED);
////						cellRenderer.setBackground(Color.RED);
//						String compareWeight = "";
//						String actWeight = (String)targetTable.getModel().getValueAt(rowNoInt, 11);
//						String teamWeight = (String)targetTable.getModel().getValueAt(rowNoInt, j);
//						
//						if(null != actWeight && null != teamWeight && actWeight.equals(teamWeight)) {
//							compareWeight = "EQUAL";
//						} else {
//							compareWeight = "NOT_EQUAL";
//						}
//						
//						Component rendererComponent = cellRenderer.getTableCellRendererComponent(targetTable, compareWeight, false, false, rowNoInt, j);
//						rendererComponent.setBackground(Color.red);
//						
////						rendererComponent.setForeground(Color.red);
//						
//					}
//				}
//			}
			
			//////////////////////////////////////////////////////////////////////////////////////////////////////////
			targetTable.repaint();
			System.out.println("END E-BOM 중량 계산 추출작업");

		} catch (Exception ex) {
			storeOperationResult(ex.getMessage());
			throw ex;
		}
	}

	private void loadBOMDataList(ArrayList<StoredOptionSet> sosList) throws Exception {
		List<HashMap<String, String>> masterDataList = new ArrayList<HashMap<String, String>>();

		for (String mapKey : dialog.getProductAllChildPartsList().keySet()){
			masterDataList.add(dialog.getProductAllChildPartsList().get(mapKey));
		}
		
		for (HashMap<String, String> masterData : masterDataList) {
			Vector<String> rowVector = new Vector<String>();
			String deptName = (String) masterData.get("ENG_DEPT_NM");
			String userName = (String) masterData.get("ENG_RESPONSIBLITY");
			String sysCode = (String) masterData.get("SYSTEM_CODE");
			String fmp = (String) masterData.get("PARENT_NO");
			String seqNo = (String) masterData.get("SEQ");
			String levMValue = (String)masterData.get("LEV_M");
			// ALTER_PART 속성값 가져오는 코드 추가
			String alterPart = (String)masterData.get("ALTER_PART");
			String funtionItemId = (String)masterData.get("FUNCTION_NO");
			String itemId = (String) masterData.get("CHILD_UNIQUE_NO");
			String partNo = (String) masterData.get("CHILD_NO");
			String partName = (String) masterData.get("CHILD_NAME");
			String smode = (String) masterData.get("SMODE");
			String curVCondition = (String) masterData.get("VC");
			String actWeightBig = (String)masterData.get("ACT_WEIGHT");
			String estWeight = (String) masterData.get("EST_WEIGHT");
			String parentID = (String) masterData.get("PARENT_NO");

			String uniqueRowKey = (String) masterData.get("UNIQUE_ROW_KEY");

			String actWeight = actWeightBig == null ? "0" : actWeightBig.toString();

			String sKey = itemId + "#" + fmp + "#" + parentID + "#" + seqNo;
			double estDoubleWeight = 0;
			double actDoubleWeight = 0;
			// P8은 체크하지 않는다.
			if ("P8".equals(smode))
				continue;

			if (actWeight != null && actWeight.trim().length() > 0 && !actWeight.equals("0")) {
				actDoubleWeight = Double.valueOf(actWeight);
			} 
			if (estWeight != null && estWeight.trim().length() > 0 && !estWeight.equals("0")) {
				estDoubleWeight = Double.valueOf(estWeight);
			}
			
			rowVector.add((tableDataMap.keySet().size() + 1) + "");
			rowVector.add(deptName);
			rowVector.add(userName);
			rowVector.add(sysCode == null ? "X00" : sysCode);
			// fmp -> function item id 로 변경
			rowVector.add(funtionItemId);
			rowVector.add(seqNo);
			rowVector.add(levMValue);
			rowVector.add(partNo);
			rowVector.add(partName);
			rowVector.add(smode);
			// Variant Condition 추가
			rowVector.add(curVCondition);
			//rowVector.add(estDoubleWeight + "");
			rowVector.add(actDoubleWeight + "" );

			int usageIndex = defaultHeaderCount;
			boolean bIsVCInclude = false;
			for (StoredOptionSet curSOS : sosList) {
				if (curSOS == null) {
					
					if (tableDataMap.containsKey(sKey)) {
						rowVector = tableDataMap.get(sKey);
					}

					BigDecimal estWeightNewValue = null;
					BigDecimal actWeightNewValue = null;
				/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
//					if(rowVector.size() - 1 < usageIndex) { // if(usageIndex <= 21) 일 경우
//						estWeightNewValue = (rowVector.get(usageIndex - 4) == null) ? new BigDecimal("0.0") : new BigDecimal(rowVector.get(usageIndex - 4)).multiply(BigDecimal.valueOf(estDoubleWeight));
//						actWeightNewValue = (rowVector.get(usageIndex - 3) == null) ? new BigDecimal("0.0") : new BigDecimal(rowVector.get(usageIndex - 3)).multiply(BigDecimal.valueOf(actDoubleWeight));
//
//						rowVector.add(estWeightNewValue.toString());
//						rowVector.add(actWeightNewValue.toString());
//					}
//					else{ // if(usageIndex > 20) 일 경우
//						estWeightNewValue = (rowVector.get(usageIndex - 5) == null) ? new BigDecimal("0.0") : new BigDecimal(rowVector.get(usageIndex - 5)).multiply(BigDecimal.valueOf(estDoubleWeight));
//						actWeightNewValue = (rowVector.get(usageIndex - 4) == null) ? new BigDecimal("0.0") : new BigDecimal(rowVector.get(usageIndex - 4)).multiply(BigDecimal.valueOf(actDoubleWeight));
//
//						if(usageIndex == targetTable.getColumnModel().getColumnCount() -2){
//							rowVector.set(usageIndex, estWeightNewValue.toString());
//						}
//						else if(usageIndex == targetTable.getColumnModel().getColumnCount() -1){
//							rowVector.set(usageIndex, actWeightNewValue.toString());
//						}
//					}
					
					if( rowVector.size() - 1 < usageIndex ) { //
						rowVector.add(rowVector.get(usageIndex - 4));
						actWeightNewValue = (rowVector.get(usageIndex - 3) == null) ? new BigDecimal("0.0") : new BigDecimal(rowVector.get(usageIndex - 3)).multiply(BigDecimal.valueOf(actDoubleWeight));
						rowVector.add(actWeightNewValue.toString());
						
					} 
				 //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
					
					
					
					
				} 
				///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				
				else {
						// rowVector 객체에 ActWeight 값을 입력
					if( usageIndex % 2 == 0) {
						rowVector.add(actDoubleWeight + "");
						
					} else {
						
						// 부품 개수를 추출하는 로직
				///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
						String ssoName = curSOS.getName().replace("_BASE", "").substring(0, 5);
						boolean isExistBOMLineTrim = false; // BOM Trim 이 존재 유무
						String quantity = "1";
						
						
						if (bomLineTrimSet.containsKey(uniqueRowKey)) {
							HashMap<String, String> trimData = bomLineTrimSet.get(uniqueRowKey);
							quantity = trimData.get("USAGE_QTY");
							if (trim_list.contains(ssoName)){
								isExistBOMLineTrim = true;
							}
						}
	
						boolean isInclude = false;
						if( "M240HA2019A^2070037100".equals(uniqueRowKey )){
							System.out.println( uniqueRowKey + " " + curVCondition + " " );
							System.out.println( curSOS.isInclude( curVCondition) );
							ArrayList<String> tmpList =  curSOS.getValueList();
							for (Iterator iterator = tmpList.iterator(); iterator.hasNext();) {
								
								String string = (String) iterator.next();
								System.out.println(string);
								
							}
							
						}
						
						if (curVCondition == null) { //Variant Condition이 없는 경우
							if (isExistBOMLineTrim) {
								bIsVCInclude = true;
								rowVector.add(quantity);
							} else{
								rowVector.add("0");
							}
						} else {
							isInclude = curSOS.isInclude(engine, curVCondition);
							//isInclude = curSOS.isInclude(curVCondition);
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
					}
				///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				usageIndex++;
			}

			if (!bIsVCInclude)
				continue;
			if (tableDataMap.containsKey(sKey))
				continue;
			tableDataMap.put(sKey, rowVector);
		}
	}

	
	// 수정중//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	private StoredOptionSet setMandatory(StoredOptionSet sos) throws Exception {
		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByName("js");

		StoredOptionSet sosMandatoried = new StoredOptionSet(sos.getName());
		sosMandatoried.addAll(sos.getOptionSet());

		// Select OSpec, Trim Mandatory Option
		String sOSpecNo = "";
		String sTrim = "";

		//sOSpecNo = "OSI-" + productItem.getLatestItemRevision().getProperty("s7_PROJECT_CODE") + "";
		TCComponentItem ospecItem = CustomUtil.findItem("S7_OspecSet", "OSI-" + productItem.getLatestItemRevision().getProperty("s7_PROJECT_CODE"));
		//sOSpecNo = ospecItem.getProperty("item_id")+"-" + ospecItem.getLatestItemRevision().getProperty("item_revision_id");
		//sOSpecNo = "OSI-C300-004";
		if(sos.getName().indexOf('_') >= 0){
			sTrim = sos.getName().substring(0, sos.getName().indexOf('_'));
		}
		else{
			return null;
		}
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		DataSet ds = new DataSet();
		ds.setString("OSPEC_NO", sOSpecNo);
		ds.setString("TRIM", sTrim);

		@SuppressWarnings("unchecked")
		ArrayList<HashMap<String, String>> alMandatories = (ArrayList<HashMap<String, String>>) remote.execute("com.kgm.service.PreOSpecService",	"selectPreOSpecMandatory", ds);

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

	/**
	 * @return
	 * @throws Exception
	 */
	private void getBOMLineTrimList() throws Exception {
		bomLineTrimSet = new HashMap<String, HashMap<String, String>>();
		trim_list = new ArrayList<String>();
		for (String mapKey : dialog.getProductAllChildPartsList().keySet()){
			HashMap<String, HashMap<String,String>> usageList =  dialog.getProductAllChildPartsUsageList().get(mapKey);

			if (null != usageList && usageList.size() > 0) {
				for (String sosKey : usageList.keySet()){
					HashMap<String, String> usageInfo = usageList.get(sosKey);
					String uniqueRowKey = usageInfo.get("UNIQUE_ROW_KEY");
					bomLineTrimSet.put(uniqueRowKey, usageInfo);
					if(!trim_list.contains(usageInfo.get("TRIM"))){
						trim_list.add(usageInfo.get("TRIM"));
					}
				}
			}
		}
	}

	/**
	 * FMP, SEQ 로 Sort 함
	 */
	public static class TableDataComparator implements Comparator<Vector<String>> {

		@Override
		public int compare(Vector<String> paramT1, Vector<String> paramT2) {

			String fmpNo1 = paramT1.get(4);
			String seq1 = paramT1.get(5);

			String fmpNo2 = paramT2.get(4);
			String seq2 = paramT2.get(5);

			int firstCompare = fmpNo1.compareTo(fmpNo2);
			if (firstCompare != 0)
				return firstCompare;
			else
				return seq1.compareTo(seq2);

		}

	}
	
	
	public Vector<String> setTeamWeightAndgetRowNo() {
		Vector<String> rowNo = new Vector<String>();
		HashMap<String, String> teamWeightMap = dialog.getTeamWeightMap();
	
		if( null != teamWeightMap && teamWeightMap.size() > 0 ) {
			for(String mapKey : teamWeightMap.keySet() ) {
				Vector<String> rowVector = tableDataMap.get(mapKey);
				if( null != rowVector) {
					for ( int i = defaultHeaderCount; i< targetTable.getColumnModel().getColumnCount(); i++ )
						if( i % 2 == 0) {
							rowVector.set( i, teamWeightMap.get(mapKey) );
						}
				}
				// No. 속성으로는 값을 찾을 수 없어서 FMP + PartNo + SEQ 의 키값을 별도로 만듬
				String function = rowVector.get(4);
				String seq = rowVector.get(5);
				String level = rowVector.get(6);
				String partNo = rowVector.get(7);
				String findRowKey = function + "#" + seq + "#" + level + "#" + partNo;
				rowNo.add(findRowKey);	
			}
		}
		
		return rowNo;
		
	}
	

//	//BOM 레벨에 상관없이 모든 레벨에서 합산되는 Supply Mode
//	public HashMap<String, String> getSCodeListToEnableAllSum(){
//		 HashMap<String, String> allLevelSm = new HashMap<String, String>();
//		 allLevelSm.put("C1", "C1");
//		 allLevelSm.put("C7", "C7");
//		 allLevelSm.put("CD", "CD");
//		 allLevelSm.put("P1", "P1");
//		 allLevelSm.put("P7", "P7");
//		 allLevelSm.put("PD", "PD");
//		return allLevelSm;
//	}
//
//	//BOM 1레벨 Level 만 합산 되는 Supply Mode
//	public HashMap<String, String>  getSCodeListToEnableOnlyOneLevelSum(){
//		 HashMap<String, String> oneLevelSm = new HashMap<String, String>();
//		 oneLevelSm.put("P7CP8", "P7CP8");
//		 oneLevelSm.put("P7MP8", "P7MP8");
//		 oneLevelSm.put("P7UP8", "P7UP8");
//		 oneLevelSm.put("P7YP8", "P7YP8");
//		 oneLevelSm.put("P7ZP8", "P7ZP8");
//		 oneLevelSm.put("PDYP8", "PDYP8");
//		 oneLevelSm.put("C1", "C1");
//		 oneLevelSm.put("C7", "C7");
//		 oneLevelSm.put("CD", "CD");
//		 oneLevelSm.put("P1", "P1");
//		 oneLevelSm.put("P7", "P7");
//		 oneLevelSm.put("PD", "PD");
//		return oneLevelSm;
//	}
//	
//	
//	public ArrayList<HashMap<String, String>> getLevelSumPartList( List<HashMap<String, String>> masterDataList )  {
//		
//			ArrayList<HashMap<String, String>> levelSumPart  = new ArrayList<HashMap<String, String>>();
//			
//			for (HashMap<String, String> masterData : masterDataList) {
//				
//				String fmp = (String) masterData.get("PARENT_NO");
//				String seqNo = (String) masterData.get("SEQ");
//				// ALTER_PART 속성값 가져오는 코드 추가
//				String level = (String)masterData.get("LEV_M");
//				String alterPart = (String)masterData.get("ALTER_PART");
//				String itemId = (String) masterData.get("CHILD_UNIQUE_NO");
//				String partNo = (String) masterData.get("CHILD_NO");
//				String smode = (String) masterData.get("SMODE");
//				String parentID = (String) masterData.get("PARENT_NO");
//				
//				String sKey = itemId + "#" + fmp + "#" + parentID + "#" + seqNo ;
//				
////				int level_int = Integer.parseInt(level) - 2;
//				
//				
//				HashMap<String, String> smmodeStatus = null;
//				// 1레벨일 경우
//				if( null != level && level.equals("1")) {
//						smmodeStatus = getSCodeListToEnableOnlyOneLevelSum();
//				}  else {
//		
//						smmodeStatus = getSCodeListToEnableAllSum();
//					}
//					
//				if( smmodeStatus.containsKey(smode)) {
//					levelSumPart.add(masterData);
//				}
//				
//			}
//			return levelSumPart;
//		}
//	
//	public HashMap<String, String> getAlterPartMap( ArrayList<HashMap<String, String>> masterDataList )  {
//		
//		HashMap<String, String> tempAlterPartMap  = new HashMap<String, String>();
//		
//		for (HashMap<String, String> masterData : masterDataList) {
//
//			String fmp = (String) masterData.get("PARENT_NO");
//			String seqNo = (String) masterData.get("SEQ");
//			// ALTER_PART 속성값 가져오는 코드 추가
//			//String level = (String)masterData.get("LEVEL");
//			String alterPart = (String)masterData.get("ALTER_PART");
//			String itemId = (String) masterData.get("CHILD_UNIQUE_NO");
//			String partNo = (String) masterData.get("CHILD_NO");
//			String smode = (String) masterData.get("SMODE");
//			String parentID = (String) masterData.get("PARENT_NO");
//
//			String sKey = itemId + "#" + fmp + "#" + parentID + "#" + seqNo ;
//			
//			if( null != alterPart && !alterPart.equals("") ) {
//				if( !(alterPart.length() == 3 && "M".equals( String.valueOf(alterPart.charAt(2) )))) {
//					tempAlterPartMap.put(sKey, alterPart);
//				}
//			}
//
//		}
//
//		return tempAlterPartMap;
//	}
//	
//	
//	
//	public String getParentForLevel(ArrayList<HashMap<String, String>> masterDataList, String partNo1, String level)  {
//		
//		for (HashMap<String, String> masterData : masterDataList) {
//
//			String parentID = (String) masterData.get("PARENT_NO");
//			String seqNo = (String) masterData.get("SEQ");
//			// ALTER_PART 속성값 가져오는 코드 추가
//			String level = (String)masterData.get("LEV_M");
//			String itemId = (String) masterData.get("CHILD_UNIQUE_NO");
//			String alterPart = (String)masterData.get("ALTER_PART");
//			String partNo = (String) masterData.get("CHILD_NO");
//			String smode = (String) masterData.get("SMODE");
//
//			String sKey = itemId + "#" + fmp + "#" + parentID + "#" + seqNo + "level" ;
//			
//			if( null != alterPart && !alterPart.equals("") ) {
//				if( !(alterPart.length() == 3 && "M".equals( String.valueOf(alterPart.charAt(2) )))) {
//					tempAlterPartMap.put(sKey, alterPart);
//				}
//			}
//
//		}
//		
//		return "";
//	}

}