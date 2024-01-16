package com.ssangyong.commands.weight;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import com.ssangyong.commands.weight.EBOMWeightDialog.CustomTableModel;
import com.ssangyong.common.WaitProgressBar;
import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.remote.SYMCRemoteUtil;
import com.ssangyong.common.utils.SYMTcUtil;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentItem;

public class EBOMAllUsageListOperation extends AbstractAIFOperation{
	private HashMap<String, HashMap<String, String>> productAllChildPartsList;
	private HashMap<String, HashMap<String,String>> productAllChildPartsUsageList;
	private EBOMWeightDialog dialog;
	private HashMap<String, Vector<String>> tableDataMap;
	private int defaultHeaderCount;
	private ArrayList<StoredOptionSet> usageOptionSetList;
	private JTable targetTable;
	
	public EBOMAllUsageListOperation(EBOMWeightDialog dialog, JTable table, ArrayList<StoredOptionSet> usageOptionSetList, int defaultHeaderCount){
		this.dialog = dialog;
		this.targetTable = table;
		this.defaultHeaderCount = defaultHeaderCount;
		this.usageOptionSetList = usageOptionSetList;
	}
	
	public void executeOperation() throws Exception {
		WaitProgressBar waitBar = new WaitProgressBar(AIFUtility.getActiveDesktop().getFrame());
		try{
			waitBar.setWindowSize(400, 500);
			waitBar.start();
			waitBar.setStatus("Start... Loading BOM & Usage Data");
			loadBomUsageData(waitBar);
			waitBar.setStatus("End... Loading BOM & Usage Data");
			
			waitBar.setStatus("Start... Finding the quantity per trim.");
			tableDataMap = new HashMap<String, Vector<String>>();
			findQtyPerTrim(usageOptionSetList);
			waitBar.setStatus("End... Finding the quantity per trim.");
			
			waitBar.setStatus("Start... Calculate Total Sum Weight.");
			getSumWeight();
			waitBar.setStatus("End... Calculate Total Sum Weight.");
			
			
			waitBar.close();
		}catch(Exception e){
			waitBar.setStatus(e.getMessage());
			waitBar.setShowButton(true);
		}
	}
	
	private void loadBomUsageData(WaitProgressBar waitBar) throws Exception{
		ArrayList<TCComponentItem> variantItemList = dialog.getVariantItemList();
		TCComponentItem productItem = dialog.getProductItem();
		productAllChildPartsList = new HashMap<String, HashMap<String, String>>();
		productAllChildPartsUsageList = new HashMap<String, HashMap<String, String>>();
		String proj_code = productItem.getLatestItemRevision().getStringProperty(PropertyConstant.ATTR_NAME_PROJCODE);
		SYMCRemoteUtil remoteQuery = new SYMCRemoteUtil();
		ArrayList<HashMap> resultList = null;
		DataSet ds = null;
		
		int variantSize = variantItemList.size();
		String variantID = "";
		String variantRev = "";
		String trimCode = "";
		ArrayList<String> fncList = new ArrayList<String>();
		boolean isFuncCO = false;
		String revRule = dialog.getRevRule();
		String callID = "";
		for(int i=0; i<variantSize; i++){
			ds = new DataSet();
			variantID = variantItemList.get(i).getStringProperty(PropertyConstant.ATTR_NAME_ITEMID);
			waitBar.setStatus(" "+(i+1) + "/" + variantSize + " ==> [" + variantID + "] Variant Start");
			if(revRule.equals("Latest Working")){
				variantRev = variantItemList.get(i).getLatestItemRevision().getStringProperty(PropertyConstant.ATTR_NAME_ITEMREVID);
				callID = "getAllChildren";
			} else {
				variantRev = SYMTcUtil.getLatestReleasedRevision(variantItemList.get(i)).getStringProperty(PropertyConstant.ATTR_NAME_ITEMREVID);
				callID = "getAllChildrenReleased";
			}
			ds.put("ID", variantID);
			ds.put("REV", variantRev);
			trimCode = variantID.substring(1,6);
			resultList = (ArrayList<HashMap>) remoteQuery.execute("com.ssangyong.service.MasterListService", callID, ds);
			if(resultList != null && resultList.size() > 0){
				int resultListSize = resultList.size();
				String level = "";
				int iLevel = 0;
				String parent_id = "";
				String seq_no = "";
				String child_id = "";
				String supply_mode = "";
				String vc = "";
				String alter_part = "";
				String qty;
				String funcCode = "";
				String func4Code = "";
				String fmCode = "";
//				int continueLevel = 100;
				for (int j = 0; j < resultListSize; j++) {
					qty = "1";
					HashMap rowHash  = resultList.get(j);
					level = rowHash.get("LEVEL").toString();
					iLevel = Integer.parseInt(level);
					parent_id = (String)rowHash.get("PID");
					child_id = (String)rowHash.get("CID");
					supply_mode = (String)rowHash.get("SUPPLY_MODE");
					
//					if(child_id.equals("1739970031")){
//						System.out.println("");
//					}
					
					
					if(level.equals("1")){
						funcCode = child_id;
						func4Code = child_id.substring(0, 4);
						
						//Variant별 공통 Function은 productAllChildPartsList에 한번만 포함시키고, productAllChildPartsUsageList에는 trim만 추가한다.
						if(!fncList.contains(funcCode)){
							fncList.add(funcCode);
							isFuncCO = false;
						} else {
							isFuncCO = true;
						}
						waitBar.setStatus("   >> [" + funcCode + "] Function Start");
						
					} else if(level.equals("2")){
						fmCode = child_id;
					} else {
						HashMap<String, String> smmodeStatus = null;
						// fmp하위 1레벨일 경우
						if( level.equals("3")) {
								smmodeStatus = getSCodeListToEnableOnlyOneLevelSum();
						}  else {
								smmodeStatus = getSCodeListToEnableAllSum();
						}
						
						//Level별 정의된 supply mode에 속하지 않아 제외된 노드 하위의 Level도 계속 제외시킨다.
//						if(iLevel > continueLevel){
//							continue;
//						}
						
						//Level별 정의된 supply mode에 포함되는 node만...
						if( smmodeStatus.containsKey(supply_mode)) {
							alter_part = (String)rowHash.get("ALTER_PART");
							if( null != alter_part && !alter_part.equals("") ) {
								if( alter_part.length() == 3 && "M".equals( String.valueOf(alter_part.charAt(2) ))) {
								} else {
									continue;
								}
							}
							
							if((alter_part == null || alter_part.equals("")) || (alter_part.length() == 3 && "M".equals( String.valueOf(alter_part.charAt(2))))){
								seq_no = (String)rowHash.get("SEQ");
								vc = (String)rowHash.get("VC");
								
								if(rowHash.get("QTY") != null){
									qty = rowHash.get("QTY").toString();
								}
								
								String lineUniqNo = fmCode + "^" + level + "^" + parent_id + "^" + seq_no + "^" + child_id;
								String mapKey = funcCode + parent_id + seq_no + child_id;
								
								if (! productAllChildPartsList.containsKey(mapKey)){
									if(!isFuncCO){
										productAllChildPartsList.put(mapKey,getPropValue(lineUniqNo, rowHash, parent_id, child_id, seq_no, supply_mode, ""+(iLevel-2), alter_part, funcCode, vc));
										productAllChildPartsUsageList.put(mapKey, getUsageValue1(lineUniqNo, proj_code, trimCode, qty, child_id, vc, null, isFuncCO));
									}
								} else {
									productAllChildPartsUsageList.put(mapKey, getUsageValue1(lineUniqNo, proj_code, trimCode, qty, child_id, vc, productAllChildPartsUsageList.get(mapKey), isFuncCO));
								}
//								continueLevel = 100;
							}
						} else {
//							continueLevel = iLevel;
						}
					}
				}
			}
//			waitBar.setStatus(" "+(i+1) + "/" + variantSize + " ==> [" + variantID + "] Variant End");
			
		}
	}
	
	private void getSumWeight(){
		int totalSumVectorCount = usageOptionSetList.size();
		Vector<String> totalSumVector = new Vector<String>();
		totalSumVector.setSize(totalSumVectorCount);
		for (Vector<String> rowVector : tableDataMap.values()) {
			int endColumnIndex = usageOptionSetList.size() + defaultHeaderCount ;
			for (int i = defaultHeaderCount ; i < endColumnIndex; i++) {
				int sumColIndex = i - defaultHeaderCount;
				BigDecimal newValue = new BigDecimal("0.0");
				
				BigDecimal sumDecimal = new BigDecimal("0.0");
				if (totalSumVector.get(sumColIndex) == null)
					sumDecimal = new BigDecimal("0.0");
				else if( !totalSumVector.get(sumColIndex).equals(""))
					sumDecimal = new BigDecimal(totalSumVector.get(sumColIndex) );
				
				if( i % 2 != 0 ) {
					newValue = rowVector.get(i) == null ? new BigDecimal("0.0") : sumDecimal.add(new BigDecimal(rowVector.get(i)));
					totalSumVector.set(sumColIndex, newValue == null ? String.valueOf( new BigDecimal("0.0") ) : newValue.toString());
				} else {
					totalSumVector.set(sumColIndex, "" );
				}
			}
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
		
		targetTable.repaint();
	}
	
	private void findQtyPerTrim(ArrayList<StoredOptionSet> sosList) throws Exception {
		List<HashMap<String, String>> masterDataList = new ArrayList<HashMap<String, String>>();
		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByName("js");
		HashMap<String, HashMap<String, String>> allPartList = dialog.getProductAllChildPartsList();
		HashMap<String, String> masterData = null;
		Vector<String> rowVector = null;
		
		for (String mapKey : allPartList.keySet()){
			masterData = allPartList.get(mapKey);
			rowVector = new Vector<String>();
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
//			String estWeight = (String) masterData.get("EST_WEIGHT");
			String parentID = (String) masterData.get("PARENT_NO");
			String uom = (String) masterData.get("UOM");

			String uniqueRowKey = (String) masterData.get("UNIQUE_ROW_KEY");

			String actWeight = actWeightBig == null ? "0" : actWeightBig.toString();

			String sKey = itemId + "#" + fmp + "#" + parentID + "#" + seqNo;
			double estDoubleWeight = 0;
			double actDoubleWeight = 0;

			if (actWeight != null && actWeight.trim().length() > 0 && !actWeight.equals("0")) {
				actDoubleWeight = Double.valueOf(actWeight);
			} 
			
//			if(itemId.equals("9501410001")){
//				System.out.println("");
//			}
			
			rowVector.add((tableDataMap.keySet().size() + 1) + "");
			rowVector.add(deptName);
			rowVector.add(userName);
			rowVector.add(sysCode == null ? "X00" : sysCode);
			rowVector.add(funtionItemId);
			rowVector.add(seqNo);
			rowVector.add(levMValue);
			rowVector.add(partNo);
			rowVector.add(partName);
			rowVector.add(smode);
			rowVector.add(curVCondition);
			rowVector.add(actWeight);

			int usageIndex = defaultHeaderCount;
			boolean bIsVCInclude = false;
			BigDecimal actWeightNewValue = null;
			
//			if(funtionItemId.equals("F010D16F15")){
//				System.out.println("");
//			}
//			
//			if(funtionItemId.equals("F240HA2019")){
//				System.out.println("");
//			}
//			
//			if(partNo.equals("175 010 00 30")){
//				System.out.println("");
//			}
			
			for (StoredOptionSet curSOS : sosList) {
				if (curSOS == null) {
					if (tableDataMap.containsKey(sKey)) {
						rowVector = tableDataMap.get(sKey);
					}
				} else {
						// rowVector 객체에 ActWeight 값을 입력
					if( usageIndex % 2 != 0) {
						//UOM이 EA이면 수량*중량, 나머지는 중량 값만...
						if(uom.equals("EA")){
							actWeightNewValue = (rowVector.get(usageIndex-1) == null) ? new BigDecimal("0.0") : new BigDecimal(rowVector.get(usageIndex-1)).multiply(BigDecimal.valueOf(actDoubleWeight));
						} else {
							actWeightNewValue = (rowVector.get(usageIndex-1) == null) ? new BigDecimal("0.0") : BigDecimal.valueOf(actDoubleWeight);
						}
						rowVector.add(actWeightNewValue.toString());
						
					} else {
						// 부품 개수를 추출하는 로직
						String ssoName = curSOS.getName().replace("_BASE", "").substring(0, 5);
						boolean isExistBOMLineTrim = false; // BOM Trim 이 존재 유무
						String quantity = "1";
						
						//usageList에서 해당트림에 포함되는지 찾는 로직 구현
						HashMap<String, String> usageData =  dialog.getProductAllChildPartsUsageList().get(mapKey);
						if (null != usageData && usageData.size() > 0) {
							String trim = usageData.get("TRIM");
							quantity = usageData.get("USAGE_QTY");
							if(trim.contains(ssoName)){
								isExistBOMLineTrim = true;
							}
						}
						
	
						boolean isInclude = false;
						if(isExistBOMLineTrim){
							if (curVCondition == null) { //Variant Condition이 없는 경우
								bIsVCInclude = true;
								rowVector.add(quantity);
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

	private HashMap<String, String> getPropValue(String lineUniqNo, HashMap rowHash, String parent_id, String child_id, String seq_no, String supply_mode, String level, String alter_part, String funcCode, String vc) throws Exception{
		String child_dis_id = (String)rowHash.get("CDISID");
		String child_name = (String)rowHash.get("CNAME");
		String system_cd = (String)rowHash.get("SYSTEM_CD");
		String dept = (String)rowHash.get("DEPT");
		String res = (String)rowHash.get("RES");
		String act_weight = "";
		String uom = (String)rowHash.get("UOM");
		if(rowHash.get("ACT_WEIGHT") != null){
			act_weight = (String)rowHash.get("ACT_WEIGHT").toString();
		}
		
		HashMap<String, String> propMap = new HashMap<String, String>();
		propMap.put("UNIQUE_ROW_KEY", lineUniqNo);
		propMap.put("SYSTEM_CODE", system_cd);
		propMap.put("PARENT_NO", parent_id);
		propMap.put("CHILD_NO", child_dis_id);
		propMap.put("CHILD_UNIQUE_NO", child_id);
		propMap.put("CHILD_NAME", child_name);
		propMap.put("SEQ", seq_no);
		propMap.put("SMODE", supply_mode);
		propMap.put("LEV_M", "" + level);
		propMap.put("ALTER_PART", alter_part);
		propMap.put("FUNCTION_NO", funcCode);
		propMap.put("VC", vc);
		propMap.put("ENG_DEPT_NM", dept);
		propMap.put("ENG_RESPONSIBLITY", res);
		propMap.put("ACT_WEIGHT", act_weight);
		propMap.put("UOM", uom);
		
		return propMap;
	}

	public static HashMap<String, String> getUsageInfo1(String uniqueId, String itemID, String projCode, String selectTrim, String qty) throws Exception{
		HashMap<String, String> usageMap = new HashMap<String, String>();

		usageMap.put("UNIQUE_ROW_KEY", uniqueId); // FMP ID + Child ID 조합
		usageMap.put("PART_UNIQUE_NO", itemID);
		if (projCode != null && projCode.trim().length() > 0){
			usageMap.put("PROJECT_CODE", projCode);
		}
		usageMap.put("TRIM", selectTrim);
		usageMap.put("USAGE_QTY", qty);

		return usageMap;
	}
	
	private HashMap<String, String> getUsageValue1(String uniqueId, String projCode, String selectTrim, String lineQty, String itemID, String simpleCondition, HashMap<String, String> usageMap, boolean isFuncCO) throws Exception{
		if (lineQty == null || lineQty.trim().equals("") || lineQty.trim().equals("-1")){
			lineQty = "1";
		}
		double dNum = Double.parseDouble(lineQty);
		int iNum = (int)dNum;
		if( dNum == iNum){
			lineQty = "" + iNum;
		}
		
		if (usageMap == null){
			usageMap = getUsageInfo1(uniqueId, itemID, projCode, selectTrim, lineQty);
		} else {
			if(!isFuncCO){
				//중복 function이 아닌경우 usageMap이 존재하면 수량을 추가 시킨다.
				String beforeQty = usageMap.get("USAGE_QTY").toString();
				double curQty = Double.valueOf(beforeQty) + Double.valueOf(lineQty);
				usageMap.put("USAGE_QTY", ""+(int)curQty);
			} else {
				//중복 function의 경우 Trim만 추가 시킨다.
				String beforeTrim = usageMap.get("TRIM").toString();
				if(!beforeTrim.contains(selectTrim)){
					String afterTrim = beforeTrim + ":" + selectTrim;
					usageMap.put("TRIM", afterTrim);
				}
				
			}
		}

		return usageMap;
	}

	public HashMap<String, HashMap<String, String>> getProductAllChildPartsList(){
		return productAllChildPartsList;
	}

	public HashMap<String, HashMap<String, String>> getProductAllChildPartsUsageList(){
		return productAllChildPartsUsageList;
	}

	//BOM 레벨에 상관없이 모든 레벨에서 합산되는 Supply Mode
		public HashMap<String, String> getSCodeListToEnableAllSum(){
			 HashMap<String, String> allLevelSm = new HashMap<String, String>();
			 allLevelSm.put("C1", "C1");
			 allLevelSm.put("C7", "C7");
			 allLevelSm.put("CD", "CD");
			 allLevelSm.put("P1", "P1");
			 allLevelSm.put("P7", "P7");
			 allLevelSm.put("PD", "PD");
			return allLevelSm;
		}

		//BOM 1레벨 Level 만 합산 되는 Supply Mode
		public HashMap<String, String>  getSCodeListToEnableOnlyOneLevelSum(){
			 HashMap<String, String> oneLevelSm = new HashMap<String, String>();
			 oneLevelSm.put("P7CP8", "P7CP8");
			 oneLevelSm.put("P7MP8", "P7MP8");
			 oneLevelSm.put("P7UP8", "P7UP8");
			 oneLevelSm.put("P7YP8", "P7YP8");
			 oneLevelSm.put("P7ZP8", "P7ZP8");
			 oneLevelSm.put("PDYP8", "PDYP8");
			 oneLevelSm.put("C1", "C1");
			 oneLevelSm.put("C7", "C7");
			 oneLevelSm.put("CD", "CD");
			 oneLevelSm.put("P1", "P1");
			 oneLevelSm.put("P7", "P7");
			 oneLevelSm.put("PD", "PD");
			return oneLevelSm;
		}
		
}