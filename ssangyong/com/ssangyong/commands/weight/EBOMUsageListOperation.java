package com.ssangyong.commands.weight;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

import com.ssangyong.commands.ospec.op.OSpec;
import com.ssangyong.commands.ospec.op.OpCategory;
import com.ssangyong.commands.ospec.op.OpTrim;
import com.ssangyong.commands.ospec.op.OpValueName;
import com.ssangyong.commands.ospec.op.Option;
import com.ssangyong.common.utils.CustomUtil;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCSession;

public class EBOMUsageListOperation implements IRunnableWithProgress{
	private HashMap<String, OpValueName> wtMap;
	private HashMap<String, OpValueName> tmMap;
	private ScriptEngineManager manager = new ScriptEngineManager();
	private ScriptEngine engine = manager.getEngineByName("js");
	private TCComponentItem variantItem;
	private TCComponentItem productItem;
	private OSpec ospec;
	private StoredOptionSet sos;
	private TCSession session;
	private HashMap<String, HashMap<String, String>> productAllChildPartsList;
	private HashMap<String, HashMap<String, HashMap<String, String>>> productAllChildPartsUsageList;
	private int total_index = 1; // 전체 오퍼레이션 진행되는 횟수
	private EBOMWeightMasterListDialog dialog;
	private int FMP_SIZE = 0;

	private static int TOTAL_TIME = 1000;
	private static boolean indeterminate = true;
	
	// Team 별 중량 체크 Smode 변수 선언
	
	private Vector<String> teamWeightSmode = null;
	private HashMap<String, String> teamWeightMap = null;

	public EBOMUsageListOperation(EBOMWeightMasterListDialog dialog){
		this.variantItem = dialog.getVariantItem();
		this.productItem = dialog.getProductItem();
		this.ospec = dialog.getOspec();
		this.tmMap = dialog.getTmMap();
		this.wtMap = dialog.getWtMap();
		this.session = productItem.getSession();
		this.dialog = dialog;
		
		// Team 별 중량 체크 Smode 변수 선언
		
		this.teamWeightSmode = new Vector<String>();
		this.teamWeightMap = new HashMap<String, String>();
		
		setTeamWeightSmode(this.teamWeightSmode);
	}

	/**
	 * @param projCode
	 * @param funcCode
	 * @param fmpLine
	 * @param allPartsList
	 * @param allUsageList
	 * @param logBuff
	 * @throws Exception
	 */
	protected void getChildBOMLineWithSOS(String projCode, String funcCode, TCComponentBOMLine fmpLine,	HashMap<String, HashMap<String, String>> allPartsList, 
			HashMap<String, HashMap<String, HashMap<String, String>>> allUsageList,
			int func_idx, int fmp_idx, int line_idx, int level_idx, String functionID) throws Exception{
		if (fmpLine.getItemRevision() == null)
			return;

		
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 테스트를 위해 FMP 하위 Part의 개수를 제한 하여 빠른 실행을 위한 로직
		//AIFComponentContext[] childLinesTest = fmpLine.getChildren();
		//AIFComponentContext[] childLines = { childLinesTest[0] };
		
		
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		AIFComponentContext[] childLines = fmpLine.getChildren();

		if (childLines != null && childLines.length > 0){
			String fmpItemId = fmpLine.getItem().getStringProperty("item_id");
			getAllChildrenList(fmpItemId, childLines, projCode, funcCode, "", allPartsList, allUsageList, func_idx, fmp_idx, line_idx, level_idx, functionID);
		}
	}

	private void getAllChildrenList(String lineItemId, AIFComponentContext[] children, String projCode, String funcCode, String parentCondition, HashMap<String, HashMap<String, String>> allPartsList, 
			HashMap<String, HashMap<String, HashMap<String, String>>> allUsageList,
			int func_idx, int fmp_idx, int line_idx, int level_idx, String functionId) throws Exception{
		if (children == null)
			return;

			level_idx ++;

		for (AIFComponentContext child : children){
			line_idx ++ ;
			TCComponentBOMLine childBOMLine = (TCComponentBOMLine)child.getComponent();
			if (childBOMLine.getItemRevision() == null)
				continue;

			TCComponentItemRevision childRevision = (TCComponentItemRevision) childBOMLine.getItemRevision();
			TCComponentItemRevision parentRevision = childBOMLine.parent().getItemRevision();
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			// Parent BOM Line 추가
//			TCComponentBOMLine parentBOMLine = childBOMLine.parent();
			// level 추출
			String level = (String.valueOf(  childBOMLine.getIntProperty("bl_level_starting_0") -2 ));
			String smode = childBOMLine.getStringProperty(PropertyConstant.ATTR_NAME_BL_SUPPLY_MODE);
			String childNo  = childRevision.getItem().getStringProperty(PropertyConstant.ATTR_NAME_ITEMID);
			
			///////////////////////////////////////////////////////////////////////////////////////////////////////
//			String curCondition = childBOMLine.getStringProperty(PropertyConstant.ATTR_NAME_BL_VARIANT_CONDITION);
			String curCondition = get1levelCondition( childBOMLine, level );
			
			/////////////////////////////////////////////////////////////////////////////////////////////////////////////
			HashMap<String, String> smmodeStatus = null;
			// 1레벨일 경우
			if( null != level && level.equals("1")) {
					smmodeStatus = getSCodeListToEnableOnlyOneLevelSum();
			}  else {
					
					smmodeStatus = getSCodeListToEnableAllSum();
					
					// Team별 중량 체크 로직 추가
					//getTeamWeight(childBOMLine, level, smode, funcCode);
					getTeamWeightTest(childBOMLine, level, smode, funcCode);
				}
			
			String lineUniqNo = lineItemId + "^" + childNo;	
			String thisCondition;
			
			if (parentCondition == null || parentCondition.equals("")) {                	
				thisCondition = curCondition;
			}
			else {
				thisCondition = "(" + parentCondition + ") and (" + curCondition + ")";
			}
			
			if( smmodeStatus.containsKey(smode)) {
				
				String alterPart = childBOMLine.getStringProperty("S7_ALTER_PART");
				if( null != alterPart && !alterPart.equals("") ) {
					if( alterPart.length() == 3 && "M".equals( String.valueOf(alterPart.charAt(2) ))) {
						//tempAlterPartMap.put(sKey, alterPart);
					
				//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
						String parentNo = parentRevision.getItem().getStringProperty(PropertyConstant.ATTR_NAME_ITEMID);
						
						String findNo = childBOMLine.getStringProperty(PropertyConstant.ATTR_NAME_BL_SEQUENCE_NO);
						
			
						String mapKey = funcCode + parentNo + findNo + childNo;
			
						if (! allPartsList.containsKey(mapKey)){
							allPartsList.put(mapKey, getPartValue(lineUniqNo, projCode, funcCode, childBOMLine, parentRevision, childRevision, curCondition, func_idx, fmp_idx, line_idx, level_idx, functionId));
							allUsageList.put(mapKey, getUsageValue(lineUniqNo, projCode, childBOMLine, childRevision, thisCondition, null));
						}
						else{
							allUsageList.put(mapKey, getUsageValue(lineUniqNo, projCode, childBOMLine, childRevision, thisCondition, allUsageList.get(mapKey)));
						}
			
						if (childBOMLine.getChildren() != null && childBOMLine.getChildren().length > 0){
							getAllChildrenList(lineUniqNo, childBOMLine.getChildren(), projCode, funcCode, thisCondition, allPartsList, allUsageList, func_idx, fmp_idx, line_idx, level_idx, functionId);
						}
					}
				}  else {
					
					String parentNo = parentRevision.getItem().getStringProperty(PropertyConstant.ATTR_NAME_ITEMID);
					
					String findNo = childBOMLine.getStringProperty(PropertyConstant.ATTR_NAME_BL_SEQUENCE_NO);
					
					
					String mapKey = funcCode + parentNo + findNo + childNo;
					
					if (! allPartsList.containsKey(mapKey)){
						allPartsList.put(mapKey, getPartValue(lineUniqNo, projCode, funcCode, childBOMLine, parentRevision, childRevision, curCondition, func_idx, fmp_idx, line_idx, level_idx, functionId));
						allUsageList.put(mapKey, getUsageValue(lineUniqNo, projCode, childBOMLine, childRevision, thisCondition, null));
					}
					else{
						allUsageList.put(mapKey, getUsageValue(lineUniqNo, projCode, childBOMLine, childRevision, thisCondition, allUsageList.get(mapKey)));
					}
					
					if (childBOMLine.getChildren() != null && childBOMLine.getChildren().length > 0){
						getAllChildrenList(lineUniqNo, childBOMLine.getChildren(), projCode, funcCode, thisCondition, allPartsList, allUsageList, func_idx, fmp_idx, line_idx, level_idx, functionId);
					}
				}
				
				
					//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				
			} else {
				
				if (childBOMLine.getChildren() != null && childBOMLine.getChildren().length > 0){
					getAllChildrenList(lineUniqNo, childBOMLine.getChildren(), projCode, funcCode, thisCondition, allPartsList, allUsageList, func_idx, fmp_idx, line_idx, level_idx, functionId);
				}
			}
		}
	}

	private String convertToSimpleCondition(String condition) throws Exception{
		ArrayList<String> foundOpValueList = new ArrayList<String>();

		if (condition == null || condition.equals("")) {
			return "" ;
		}

		Pattern p = Pattern.compile(" or | and |\"[a-zA-Z0-9]{4}\"|\"[a-zA-Z0-9]{5}_STD\"|\"[a-zA-Z0-9]{5}_OPT\"");
		Matcher m = p.matcher(condition);
		while (m.find()) {
			//          System.out.println(m.start() + " " + m.group());
			foundOpValueList.add(m.group().trim());
		}

		String conditionResult = null;
		for( String opValue : foundOpValueList){
			String con = opValue.replaceAll("\"", "");
			if( conditionResult == null){
				conditionResult = con;
			}else{
				conditionResult += " " + con;
			}
		}

		return conditionResult;
	}

	public String removeTrimOptionValue(String condition){

		String resultStr = "";

		if (condition == null || condition.equals("")) {
			return "" ;
		}

		String[] tmpArray = condition.split(" or ");
		for( int i = 0; tmpArray != null && i < tmpArray.length; i++){
			String str = tmpArray[i].replaceAll("[a-zA-Z0-9]{5}_STD and |[a-zA-Z0-9]{5}_OPT and ", "");
			str = str.replaceAll(" and [a-zA-Z0-9]{5}_STD| and [a-zA-Z0-9]{5}_OPT", "");
			str = str.replaceAll("[a-zA-Z0-9]{5}_STD|[a-zA-Z0-9]{5}_OPT", "").trim(); 
			if( resultStr.equals("")){
				resultStr = str;
			}else{
				resultStr += " or " + str;
			}
		}

		return resultStr;
	}

	public static HashMap<String, String> getUsageInfo(String uniqueId, String itemID, String projCode, String e0Code, String e0Desc, String e1Code, String e1Desc, OpTrim trim, String qty, String optionType) throws Exception{
		HashMap<String, String> usageMap = new HashMap<String, String>();

		usageMap.put("UNIQUE_ROW_KEY", uniqueId); // FMP ID + Child ID 조합
		usageMap.put("PART_UNIQUE_NO", itemID);
		if (projCode != null && projCode.trim().length() > 0){
			usageMap.put("PROJECT_CODE", projCode);
		}
		usageMap.put("AREA", trim.getArea());
		usageMap.put("PASSENGER", trim.getPassenger());
		usageMap.put("ENGINE", trim.getEngine());
		usageMap.put("GRADE", trim.getGrade());
		usageMap.put("TRIM", trim.getTrim());
		usageMap.put("USAGE_QTY", qty);
		usageMap.put("USAGE_TYPE", optionType);
		usageMap.put("OPT_E00", e0Code);
		usageMap.put("OPT_E00_DESC", e0Desc);
		usageMap.put("OPT_E10", e1Code);
		usageMap.put("OPT_E10_DESC", e1Desc);

		return usageMap;
	}

	private HashMap<String, HashMap<String, String>> getUsageValue(String uniqueId, String prodProjCode, TCComponentBOMLine childBOMLine, TCComponentItemRevision revision, String thisCondition, HashMap<String, HashMap<String, String>> usageMap) throws Exception{
		if (usageMap == null){
			usageMap = new HashMap<String, HashMap<String, String>>();
		}
		String lineQty = childBOMLine.getStringProperty(PropertyConstant.ATTR_NAME_BL_QUANTITY);
		String itemID = revision.getItem().getStringProperty("item_id");
		String projCode = prodProjCode;

		if (lineQty == null || lineQty.trim().equals("")){
			lineQty = "1";
		}
		double dNum = Double.parseDouble(lineQty);
		int iNum = (int)dNum;
		if( dNum == iNum){
			lineQty = "" + iNum;
		}

		ArrayList<OpTrim> trimList = ospec.getTrimList();
		String simpleCondition = convertToSimpleCondition(thisCondition);

		String selectTrim = variantItem.getStringProperty("item_id").substring(1,6);
		
		for (OpTrim trim : trimList){
			if(!trim.getTrim().equals(selectTrim)){
				continue;
			}
			String sosStdName = trim.getTrim();
			// 이부분 로직이 좀 이상함 
			
			HashMap<String, HashMap<String, OpCategory>> cm = ospec.getCategory();
			HashMap<String, OpCategory> opCategory = cm.get(selectTrim);
			
			if (sos.isInclude(engine, simpleCondition)){
				if (usageMap.containsKey(sosStdName)){
					HashMap<String, String> valueMap = usageMap.get(sosStdName);

					String beforeQty = valueMap.get("USAGE_QTY").toString();
					double curQty = Double.valueOf(beforeQty) + Double.valueOf(lineQty); // unpack된 line의 경우 이미 usageMap에 포함되어 있으므로 수량을 합산한다.
					valueMap.put("USAGE_QTY", ""+(int)curQty);
				}
				else{
					String tmCode = " ";
					String tmName = " ";
					String wtCode = " ";
					String wtName = " ";

					if( sos.getOptionSet().get("E00") != null ) {
						if (!"".equals(sos.getOptionSet().get("E00").get(0)) ) {
							tmCode = sos.getOptionSet().get("E00").get(0);
							tmName = tmMap.get(tmCode).getOptionName();
						}
					}

					if( sos.getOptionSet().get("E10") != null ) {
						if(!"".equals(sos.getOptionSet().get("E10").get(0)) ) {
							wtCode = sos.getOptionSet().get("E10").get(0);
							wtName = wtMap.get(wtCode).getOptionName();
						}
					}

					HashMap<String, String> newValue = getUsageInfo(uniqueId, itemID, projCode, tmCode, tmName, wtCode, wtName, trim, lineQty, "STD");

					usageMap.put(sosStdName, newValue);
				}
			}
		}
		return usageMap;
	}

	private HashMap<String, String> getPartValue(String uniqueId, String prodProjectCode, String functionCode, TCComponentBOMLine bomLine,
			TCComponentItemRevision parentRevision, TCComponentItemRevision currentRevision,
			String curCondition, int func_idx, int fmp_idx, int line_idx, int level_idx, String functionId) throws Exception {
		HashMap<String, String> propMap = new HashMap<String, String>();

		String childType = currentRevision.getStringProperty("object_type");

		propMap.put("UNIQUE_ROW_KEY", uniqueId);
		if (childType.equals("S7_StdpartRevision")){
			propMap.put("SYSTEM_CODE", "X00");
		}
		else{
			propMap.put("SYSTEM_CODE", currentRevision.getStringProperty(PropertyConstant.ATTR_NAME_BUDGETCODE));
		}
		String parentNo = parentRevision.getStringProperty(PropertyConstant.ATTR_NAME_DISPLAYPARTNO);
		if (parentNo == null || parentNo.trim().length() == 0){
			parentNo = parentRevision.getItem().getStringProperty("item_id");
		}
		propMap.put("PARENT_NO", parentNo);
		String childNo = currentRevision.getStringProperty(PropertyConstant.ATTR_NAME_DISPLAYPARTNO);
		if (childNo == null || childNo.trim().length() == 0){
			childNo = currentRevision.getItem().getStringProperty("item_id");
		}
		propMap.put("CHILD_NO", childNo);
		propMap.put("CHILD_UNIQUE_NO", currentRevision.getItem().getStringProperty("item_id"));
		propMap.put("CHILD_NAME", currentRevision.getItem().getStringProperty("object_name"));
		propMap.put("SEQ", bomLine.getStringProperty(PropertyConstant.ATTR_NAME_BL_SEQUENCE_NO));
		propMap.put("SMODE", bomLine.getStringProperty(PropertyConstant.ATTR_NAME_BL_SUPPLY_MODE));
		propMap.put("LEV_M", String.valueOf(  bomLine.getIntProperty("bl_level_starting_0") -2 ));
		// Bom Line Level 값 가져오는 코드 수정
		//propMap.put("LEVEL", String.valueOf( bomLine.getIntProperty("bl_level_starting_0")));
		// Bom Line Alter_Part 속성값 가져 오는 코드 추가
		propMap.put("ALTER_PART", bomLine.getStringProperty("S7_ALTER_PART"));
		propMap.put("FUNCTION_NO", functionId);
		String simpleCondition = convertToSimpleCondition(curCondition);
		String tCondition = removeTrimOptionValue(simpleCondition);
		propMap.put("VC", tCondition);
		propMap.put("ENG_DEPT_NM", currentRevision.getProperty(PropertyConstant.ATTR_NAME_OWNINGGROUP));
		propMap.put("ENG_RESPONSIBLITY", currentRevision.getProperty(PropertyConstant.ATTR_NAME_OWNINGUSER));
		propMap.put("EST_WEIGHT", currentRevision.getProperty(PropertyConstant.ATTR_NAME_ESTWEIGHT));
		propMap.put("ACT_WEIGHT", currentRevision.getProperty(PropertyConstant.ATTR_NAME_ACTWEIGHT));
		return propMap;
	}

	class BOMLineSearcher {
		private String projCode;
		private String functionCode;
		private TCComponentBOMLine fmpLine;
		private HashMap<String, HashMap<String, String>> allPartsList;
		private HashMap<String, HashMap<String, HashMap<String, String>>> allUsageList;
		private int func_idx = 0;
		private int fmp_idx = 0;
		private int line_idx = 0;
		private int level_idx = 0;
		private IProgressMonitor monitor;
		// 추가 Function Item ID 전역변수 추가
		private String functionID;
		// 추가 Function Item ID 파라미터 추가
		public BOMLineSearcher(String projCode, String funcCode, TCComponentBOMLine fmpLine, 
				HashMap<String, HashMap<String, String>> partsList, HashMap<String, HashMap<String, HashMap<String, String>>> usageList, 
				int func_idx, int fmp_idx, IProgressMonitor monitor, String functionID){
			this.projCode = projCode;
			this.functionCode = funcCode;
			this.fmpLine = fmpLine;
			this.allPartsList = partsList;
			this.allUsageList = usageList;
			this.func_idx = func_idx;
			this.fmp_idx = fmp_idx;
			this.monitor = monitor;
			// 추가 Function Item ID 전역변수 추가
			this.functionID = functionID;
		}

		public String call() throws Exception {
			final String item_id = fmpLine.getItem().getStringProperty("item_id");
			// 추가 Function Item ID 파라미터 추가
			getChildBOMLineWithSOS(projCode, functionCode, fmpLine, allPartsList, allUsageList, func_idx, fmp_idx, line_idx, level_idx, functionID);
			System.out.println(total_index + "/" + FMP_SIZE + " ==> [" + item_id + "] END.");
			setMessage(monitor, total_index + "/" + FMP_SIZE + " ==> [" + item_id + "] END.");
			total_index ++;
			return item_id;
		}
	}

	public HashMap<String, HashMap<String, String>> getProductAllChildPartsList(){
		return productAllChildPartsList;
	}

	public HashMap<String, HashMap<String, HashMap<String, String>>> getProductAllChildPartsUsageList(){
		return productAllChildPartsUsageList;
	}

	@Override
	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		try {
			FMP_SIZE = 0;
			System.out.println("Variant Item ID : ==>" + variantItem.getStringProperty("item_id"));
			ArrayList<String> functionList = dialog.getChildren(variantItem.getStringProperty("item_id")); //variant 하위의 모든 Function 리스트
			System.out.println("Function Size : ==>" + functionList.size());
			for(int i=0; i<functionList.size(); i++){
				ArrayList<String> fmpList = dialog.getChildren(functionList.get(i)); //function 하위의 모든 FMP 리스트
				int index = 0;
				for(int j=0; j<fmpList.size(); j++){
					if(dialog.getChildren(fmpList.get(j)).size() > 0){
						index ++;
					}
				}
				FMP_SIZE = FMP_SIZE + index;
			}

			System.out.println("FMP SIZE : " + FMP_SIZE);

			total_index = 1;
			productAllChildPartsList = new HashMap<String, HashMap<String, String>>();
			productAllChildPartsUsageList = new HashMap<String, HashMap<String, HashMap<String, String>>>();

			TCComponentBOMLine variantBOMLine = CustomUtil.getBomline(variantItem.getLatestItemRevision(), session);

			String proj_code = productItem.getLatestItemRevision().getStringProperty(PropertyConstant.ATTR_NAME_PROJCODE);

			AIFComponentContext[] funcLinesContext = variantBOMLine.getChildren();
			int func_idx = 0;
			// OSpec 에서 Option 값이 "S" 인 것들의 Option들을 걸러낸다.
			sos = getStoredOptionSets(ospec);

			for (AIFComponentContext funcLineContext : funcLinesContext){
				TCComponentBOMLine funcLine = (TCComponentBOMLine) funcLineContext.getComponent();
				if (funcLine.getItemRevision() != null){
					func_idx++;
					TCComponentItem funcItem = funcLine.getItem();
					// 추가 Function Item ID 속성 추가
					String functionId = funcItem.getStringProperty("item_id");
					
					//if( ! "F240HA2019".equals(functionId) ) continue;
					
					String func_code = functionId.substring(0, 4);
					AIFComponentContext[] fmpLinesContext = funcLine.getChildren();
					int fmp_idx = 0;
					for (AIFComponentContext fmpLineContext : fmpLinesContext){
						fmp_idx ++;
						TCComponentBOMLine fmpLine = (TCComponentBOMLine) fmpLineContext.getComponent();

						if (fmpLine.getItemRevision() != null && fmpLine.getChildrenCount() > 0){
							// 추가 Function Item ID 파라미터 추가
							BOMLineSearcher bomLoader = new BOMLineSearcher(proj_code, func_code, fmpLine, productAllChildPartsList, productAllChildPartsUsageList, func_idx, fmp_idx, monitor, functionId);
							bomLoader.call();
						}
					}
					
				}
			}
		} catch (Exception e) {
			try {
				throw e;
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}finally{
			monitor.done();
		}
	}

	private StoredOptionSet getStoredOptionSets(OSpec ospec) throws Exception
	{
		try
		{
			StoredOptionSet stdSos = null;
			ArrayList<OpTrim> trimList = ospec.getTrimList();
			HashMap<String, ArrayList<Option>> trimOptionMap = ospec.getOptions();
			String selectTrim = variantItem.getStringProperty("item_id").substring(1,6);
			
			for (OpTrim opTrim : trimList)
			{
				if(!opTrim.getTrim().equals(selectTrim)){
					continue;
				}
				ArrayList<Option> options = trimOptionMap.get(opTrim.getTrim());
				String stdName = opTrim.getTrim() + "_BASE";
				stdSos = new StoredOptionSet(stdName);
				stdSos.add("TRIM", stdName);

				for(Option option : options){
					if( option.getValue().equalsIgnoreCase("S")){
						stdSos.add(option.getOp(), option.getOpValue());
					}
				}
			}

			return stdSos;
		}
		catch (Exception ex)
		{
			throw ex;
		}
	}


	private void setMessage(IProgressMonitor monitor, String strMessage) {
		session.setStatus(strMessage);
		monitor.beginTask(strMessage, indeterminate ? IProgressMonitor.UNKNOWN : TOTAL_TIME);
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
		
		
		public String get1levelCondition (TCComponentBOMLine  bomLine, String level) throws Exception {
			String condition = "";
			
			TCComponentBOMLine tempBomLine = bomLine;
			
			while( true ) {
				TCComponentBOMLine directBomLine = tempBomLine.parent();
				int bomLevel = (directBomLine.getIntProperty("bl_level_starting_0") - 2) ;
				
				if( bomLevel > 1 ) {
						tempBomLine = directBomLine;
						continue;
					}  else {
						condition = bomLine.getStringProperty(PropertyConstant.ATTR_NAME_BL_VARIANT_CONDITION);
						break;
					}
				} 

			return condition;
		}
		
		
		
		
		public void getTeamWeight (TCComponentBOMLine bomLine, String level, String smode, String funcCode) throws Exception 	{
			
			// 2Level 이하 이면서 ( P1YP8, P7UP8, P7YP8, PDYP8 ) 인경우  하위 로직 실행
//			int treehit = Integer.valueOf(level);
			if( this.teamWeightSmode.contains(smode) ) {
				

				HashMap<String, TCComponentBOMLine> tempMap = new HashMap<String, TCComponentBOMLine>();
				tempMap.put(level, bomLine);
				
				TCComponentBOMLine tempBomLine = bomLine;
			
			while( true ) {
				TCComponentBOMLine directBomLine = tempBomLine.parent();
				int bomLevel = (directBomLine.getIntProperty("bl_level_starting_0") - 2) ;
				
				if( bomLevel > 1 ) {
					String bomSmode = directBomLine.getStringProperty(PropertyConstant.ATTR_NAME_BL_SUPPLY_MODE);
					
					if( !this.teamWeightSmode.contains(bomSmode)) {
						tempBomLine = directBomLine;
//						treehit = tempBomLine.getIntProperty("bl_level_starting_0") - 2;
						continue;
					} else {
						tempMap.put(String.valueOf(bomLevel), directBomLine);
						tempBomLine = directBomLine;
					}
					
				}else {
					String bomSmode = directBomLine.getStringProperty(PropertyConstant.ATTR_NAME_BL_SUPPLY_MODE);
					if( !getSCodeListToEnableAllSum().containsKey(bomSmode)) {
						//tempBomLine = directBomLine;
//						treehit = tempBomLine.getIntProperty("bl_level_starting_0") - 2;
						break;
					} else {
						tempMap.put(String.valueOf(bomLevel), directBomLine);
						break;
					}
					
					
				}
			}
			
			
			TCComponentBOMLine rootBomLine = tempMap.get("1");
			if( rootBomLine != null ) {
				
				String rootSystemCode = "";
				
				
				TCComponentItemRevision rootRevision = (TCComponentItemRevision) rootBomLine.getItemRevision();
				String rootType = rootRevision.getStringProperty("object_type");
				
				if (rootType.equals("S7_StdpartRevision")){
					rootSystemCode = "X00";
				}
				else{
					rootSystemCode = rootBomLine.getStringProperty(PropertyConstant.ATTR_NAME_BUDGETCODE);
					
					rootSystemCode = rootSystemCode == null ? "X00" : rootSystemCode;
					
				}
				
				
				
				String rootActWeight = rootBomLine.getProperty(PropertyConstant.ATTR_NAME_ACTWEIGHT);
				
				String rootdNo  = rootBomLine.getItem().getStringProperty(PropertyConstant.ATTR_NAME_ITEMID);
				String rootParentNo = rootBomLine.parent().getItemRevision().getStringProperty(PropertyConstant.ATTR_NAME_DISPLAYPARTNO);
				if (rootParentNo == null || rootParentNo.trim().length() == 0){
					rootParentNo = rootBomLine.parent().getItemRevision().getItem().getStringProperty("item_id");
				}
				
				String rootFindNo = rootBomLine.getStringProperty(PropertyConstant.ATTR_NAME_BL_SEQUENCE_NO);
				
				String rootKey = rootdNo + "#" + rootParentNo + "#" + rootParentNo + "#" + rootFindNo;
				
				double calculationActWeight = (rootActWeight == null || rootActWeight.equals("") ? new Double("0.0") : new Double( rootActWeight ));
				for( String mapKey : tempMap.keySet()) {
					if( !mapKey.equals("1") ) {
						TCComponentBOMLine childBomLine = tempMap.get(mapKey);
						
						
						String childSystemCode = "";
						
						
						TCComponentItemRevision childRevision = (TCComponentItemRevision) childBomLine.getItemRevision();
						String childType = childRevision.getStringProperty("object_type");
						
						if (childType.equals("S7_StdpartRevision")){
							childSystemCode = "X00";
						}
						else{
							childSystemCode = childBomLine.getStringProperty(PropertyConstant.ATTR_NAME_BUDGETCODE);
							childSystemCode = childSystemCode == null ? "X00" : childSystemCode;
						}
						
						
						
						if( !rootSystemCode.equals( childSystemCode ) ) {
							String childWeight = childBomLine.getStringProperty(PropertyConstant.ATTR_NAME_BUDGETCODE);
							
							calculationActWeight = (calculationActWeight) - (childWeight == null || childWeight.equals("")? new Double("0.0") : new Double( rootActWeight ));
							this.teamWeightMap.put( rootKey, String.valueOf(calculationActWeight) );
							
							
							String childNo  = childBomLine.getItem().getStringProperty(PropertyConstant.ATTR_NAME_ITEMID);
							String parentNo = childBomLine.parent().getItemRevision().getStringProperty(PropertyConstant.ATTR_NAME_DISPLAYPARTNO);
							if (parentNo == null || parentNo.trim().length() == 0){
								parentNo = childBomLine.parent().getItemRevision().getItem().getStringProperty("item_id");
							}
							
							String findNo = childBomLine.getStringProperty(PropertyConstant.ATTR_NAME_BL_SEQUENCE_NO);
							
							String childKey = childNo + "#" + parentNo + "#" + parentNo + "#" + findNo;
							this.teamWeightMap.put( childKey, childWeight );
						}
					}
				}
				
				}
			}
			
		}
	
	
	public void setTeamWeightSmode( Vector<String> teamWeightSmode) {
		// 2Level 이하 이면서 ( P1YP8, P7UP8, P7YP8, PDYP8 ) 인경우  하위 로직 실행
		teamWeightSmode.add("P1YPB");
		teamWeightSmode.add("P7UP8");
		teamWeightSmode.add("P7YP8");
		teamWeightSmode.add("PDYP8");
		
	}
	
	
	public HashMap<String, String> getTeamWeightMap() {
		return this.teamWeightMap;
	}
	
	
	////////////////////////////////////////////Test//////////////////////////////////////////////////////////////
	// Team별 중량 변화 로직 검증을 위한 테스트 메서드
	public void getTeamWeightTest (TCComponentBOMLine bomLine, String level, String smode, String funcCode) throws Exception 	{
		
		this.teamWeightMap.put( "5730037000#M620HA2019A#M620HA2019A#100100", "33.333");
		this.teamWeightMap.put( "5211137000#52110 37000#52110 37000#000010" , "33.333");
		this.teamWeightMap.put( "5215037000#52110 37000#52110 37000#000020" , "33.333");
		this.teamWeightMap.put( "5255037000#52110 37000#52110 37000#000030" , "33.333");
		this.teamWeightMap.put( "5211837000#52110 37000#52110 37000#000050" , "33.333");
		this.teamWeightMap.put( "5211837000#52110 37000#52110 37000#000040" , "33.333");
		
	}
	
	
}