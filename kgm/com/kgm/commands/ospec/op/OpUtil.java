package com.kgm.commands.ospec.op;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import javax.swing.JComboBox;

import jxl.Cell;
import jxl.LabelCell;
import jxl.Sheet;
import jxl.Workbook;

import com.kgm.commands.ec.SYMCECConstant;
import com.kgm.common.remote.DataSet;
import com.kgm.common.remote.SYMCRemoteUtil;
import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.DatasetService;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.Registry;

public class OpUtil {
	
	private static final int OSPEC_AREA = 2;
	private static final int OSPEC_PASSENGER = 3;
	private static final int OSPEC_ENGINE = 4;
	private static final int OSPEC_GRADE = 5;
	private static final int OSPEC_TRIM = 6;	
	public static final String SELECT_G_MODEL = "Select a G-Model";
	public static final String SELECT_PROJECT = "Select a Project";
	
	/**
	 * Trim(Variant) 정보를 읽어온다.
	 * 
	 * @param sheet
	 * @param endVariantIdx
	 * @return
	 */
	public static HashMap getTrimMap(Sheet sheet, int endVariantIdx){
    	int iHavingTypeColumn = -1, iHavingPassengerColumn = -1, iHavingEngineColumn = -1
    			,iHavingGradeColumn = -1, iHavingTrimColumn = -1;
    	OpTrim opTrim = null;
    	HashMap<String, OpTrim> trimMap = new HashMap();
    	for( int column = 7; column < endVariantIdx; column++){
    		for( int row = 2; row < 7; row++){
    			
    			Cell cell = sheet.getCell(column, row);
        		if( cell instanceof LabelCell){
        			LabelCell label = (LabelCell)cell;
        			
        			switch(row){
        			case OSPEC_AREA:
        				opTrim = new OpTrim();
        				opTrim.setArea(label.getString());
        				iHavingTypeColumn = column;
        				break;
        			case OSPEC_PASSENGER:
        				opTrim.setPassenger(label.getString());
        				iHavingPassengerColumn = column;
        				break;
        			case OSPEC_ENGINE:
        				opTrim.setEngine(label.getString());
        				iHavingEngineColumn = column;
        				break;
        			case OSPEC_GRADE:
        				opTrim.setGrade(label.getString());
        				iHavingGradeColumn = column;
        				break;
        			case OSPEC_TRIM:
        				opTrim.setTrim(label.getString());
        				iHavingTrimColumn = column;
        				break;
    				default:
        					
        			}
        			
        		}else{
        			//BlankCell로 판단.
        			// 이전 Column값을 그래로 승계
        			LabelCell label = null;
        			switch(row){
        			case OSPEC_AREA:
        				opTrim = new OpTrim();
        				cell = sheet.getCell(iHavingTypeColumn, row);
        				label = (LabelCell)cell;
        				opTrim.setArea(label.getString());
        				break;
        			case OSPEC_PASSENGER:
        				cell = sheet.getCell(iHavingPassengerColumn, row);
        				label = (LabelCell)cell;
        				opTrim.setPassenger(label.getString());
        				break;
        			case OSPEC_ENGINE:
        				cell = sheet.getCell(iHavingEngineColumn, row);
        				label = (LabelCell)cell;
        				opTrim.setEngine(label.getString());
        				break;
        			case OSPEC_GRADE:
        				cell = sheet.getCell(iHavingGradeColumn, row);
        				label = (LabelCell)cell;
        				opTrim.setGrade(label.getString());
        				break;
        			case OSPEC_TRIM:
        				cell = sheet.getCell(iHavingTrimColumn, row);
        				label = (LabelCell)cell;
        				opTrim.setTrim(label.getString());
        				break;
    				default:
        					
        			}
        		}
    		}
    		
			opTrim.setColOrder(column - 7);
    		if( opTrim != null && !trimMap.containsKey(opTrim.getTrim()))
    			trimMap.put(opTrim.getTrim(), opTrim);
    	}    
    	
    	return trimMap;
	}
	
	public static OSpec getOSpec(File file) throws Exception{

		//엑셀의 
		// Excel Load Start
		// [20240130] 추가 설명
		// 엑셀 파일 형태가 'Excel 97-2003 통합 문서'타입으로 저장된 파일만 읽어 올수 있음
		// 그냥 엑셀 파일을 읽어올 경우 'Unable to recognize OLE stream' 에러 발생
    	Workbook workBook = Workbook.getWorkbook(file);
    	Sheet sheet = workBook.getSheet(0);
    	
    	//all 을 찾은 후 그 다음 셀부터 시작해야함. 즉 7idx부터 시작하여 Eff-IN idx전까지 Variant를 읽어 간다.
    	int startVariantIdx = 7;
    	int endVariantIdx = getEndVariantIdx(sheet);
    	HashMap<String, OpTrim> trimMap = OpUtil.getTrimMap(sheet, endVariantIdx);
    	
    	Cell titleCell = sheet.getCell(0, 0);
    	String title = titleCell.getContents();
    	String gModel = null;
    	if( title != null ){
    		int idx = title.indexOf("Gmodel:");
    		if( idx > -1){
    			gModel = title.substring(idx + 7, idx + 8);
    		}
    	}
    	
    	Cell osiCell = sheet.getCell(0, 4);
    	String tmpStr = osiCell.getContents();
    	String osiNo = tmpStr.substring(tmpStr.indexOf(":") + 1).trim();
    	
    	Cell dateCell = sheet.getCell(2, 4);
    	String releasedDate = (dateCell.getContents()).replace("Released", "").trim();
    	
    	OSpec ospec = new OSpec(gModel, osiNo, releasedDate.replaceAll("-", ""));
    	ospec.setTrims(trimMap);
    	HashMap<String, ArrayList<Option>> options = new HashMap();
    	ospec.setOptions(options);
    	ospec.setOpNameList(new ArrayList<OpValueName>());
    	ospec.setCategory(new HashMap());
    	ospec.setPackageMap(new HashMap<String, HashMap<String, HashMap<String, ArrayList<String>>>>());
    	ospec.setDriveTypeMap(new HashMap<String, HashMap<String, HashMap<String, ArrayList<String>>>>());
    	
    	//imp.properties에서 값을 가져온다.
    	Registry customRegistry = Registry.getRegistry("com.kgm.common.bundlework.imp.imp");
    	int startVariantRowIdx = Integer.parseInt(customRegistry.getString("BWVariantOptionImpDialog.startVariantRowIdx"));//	6
    	int startOptionCodeColumnIdx = Integer.parseInt(customRegistry.getString("BWVariantOptionImpDialog.startOptionCodeColumnIdx"));//	3
    	int startOptionRowIdx = Integer.parseInt(customRegistry.getString("BWVariantOptionImpDialog.startOptionRowIdx"));//	8
    	int optionCategoryDescColumnIdx = Integer.parseInt(customRegistry.getString("BWVariantOptionImpDialog.optionCategoryDescColumnIdx"));//	1
    	int optionCodeDescColumnIdx = Integer.parseInt(customRegistry.getString("BWVariantOptionImpDialog.optionCodeDescColumnIdx"));//	2
    	int packageIdx = 4;
    	int driveTypeIdx = 5;
    	int allIdx = 6;
    	int effInIdx = endVariantIdx;
    	int remarkIdx = endVariantIdx + 1;
    	
    	int col_order = 0;
    	int currentOptionRowIdx = startOptionRowIdx;
    	int endOptionCodeIdx = sheet.getRows() ;
    	for( int column = startVariantIdx; column < endVariantIdx; column++, col_order++){
    		
    		LabelCell variantCell = (LabelCell)sheet.getCell(column, startVariantRowIdx);
    		OpTrim opTrim = trimMap.get(variantCell.getString());
    		if( opTrim == null){
    			throw new Exception("not found the Trim(" + variantCell.getString() + ")");
    		}
    		Cell codeCell = null, mergedCodeCell = null;
    		String categoryDesc = "", codeDesc = "";
    		String preCategory = "";
    		int row_order = 0;
    		for( int row = currentOptionRowIdx; row < endOptionCodeIdx; row++){
    			
    			codeCell = sheet.getCell(startOptionCodeColumnIdx,row);
    			if( !(codeCell instanceof LabelCell) && (row - 1) >= currentOptionRowIdx){
    				codeCell = mergedCodeCell;
    			}
    			
    			if( !(codeCell instanceof LabelCell) ){
    				continue;
    			}
    			
    			mergedCodeCell = codeCell;
    			Cell categoryDescCell = sheet.getCell(optionCategoryDescColumnIdx, row);
    			if( categoryDescCell instanceof LabelCell){
    				categoryDesc = categoryDescCell.getContents();
    			}
    			Cell codeDescCell = sheet.getCell(optionCodeDescColumnIdx, row);
    			if( codeDescCell instanceof LabelCell){
    				codeDesc = codeDescCell.getContents();
    			}else{
    				if( !categoryDesc.equals(preCategory)){
    					codeDesc = categoryDesc;
    				}
    			}
    			
    			preCategory = categoryDesc;
    			
    			String tmpCode = codeCell.getContents();
    			
    			//옵션 코드에서 앞 3자리는 옵션 카테고리이다.
    			if( tmpCode.length() < 3 ) continue;
    			String optionCategory = OpUtil.getCategory(tmpCode);
    			
    			String packageName = sheet.getCell(packageIdx, row).getContents();
    			String driveType = sheet.getCell(driveTypeIdx, row).getContents();
    			String all = sheet.getCell(allIdx, row).getContents();
    			String effIn = sheet.getCell(effInIdx, row).getContents(); 
    			String remark = sheet.getCell(remarkIdx, row).getContents();
    			Cell cell = sheet.getCell(column, row);
    			if( cell instanceof LabelCell){
    				String sCellText = ((LabelCell) cell).getString();
    				if (sCellText != null)
    					sCellText = sCellText.trim();
    				if( !"".equals(sCellText) && !"-".equals(sCellText))
    				{
    					
    					Option option = new Option(optionCategory, categoryDesc, tmpCode, codeDesc
    							, packageName, driveType, all, ((LabelCell) cell).getString(), effIn, remark, col_order, row_order);
    					option.setOpTrim(opTrim);
    					ArrayList opList = options.get(opTrim.getTrim());
    					if( opList == null){
    						opList = new ArrayList();
    						opList.add(option);
    						options.put(opTrim.getTrim(), opList);
    					}else{
    						opList.add(option);
    					}
    					
    					HashMap<String, HashMap<String, OpCategory>> categories = ospec.getCategory();
    					HashMap<String, OpCategory> categoryMap = categories.get(opTrim.getTrim());
    					if( categoryMap == null){
    						categoryMap = new HashMap();
    						
    						OpCategory category = new OpCategory(optionCategory, categoryDesc);
    						ArrayList<Option> opValueList = new ArrayList();
    						opValueList.add(option);
    						category.setOpValueList(opValueList);
    						
    						categoryMap.put(optionCategory, category);
    						categories.put(opTrim.getTrim(), categoryMap);
    						
    					}else{
    						OpCategory category = categoryMap.get(optionCategory);
    						if( category == null){
    							category = new OpCategory(optionCategory, categoryDesc);
    							ArrayList<Option> opValueList = new ArrayList();
        						opValueList.add(option);
        						category.setOpValueList(opValueList);
        						categoryMap.put(optionCategory, category);
    						}else{
    							category.getOpValueList().add(option);
    						}
    					}
    					
    					//Package Map 셋팅
    					if( packageName != null && !packageName.equals("")){
	    					HashMap<String, HashMap<String, HashMap<String, ArrayList<String>>>> packageMap = ospec.getPackageMap();
	    					HashMap<String, HashMap<String, ArrayList<String>>> pkgPackageMap = packageMap.get(opTrim.getTrim());
	    					if( pkgPackageMap == null ){
	    						pkgPackageMap = new HashMap();
	    						HashMap<String, ArrayList<String>> categoryPackageMap = new HashMap();
	    						ArrayList<String> packageOpValues = new ArrayList();
	    						packageOpValues.add(tmpCode);
	    						categoryPackageMap.put(optionCategory, packageOpValues);
	    						pkgPackageMap.put(packageName, categoryPackageMap);
	    						packageMap.put(opTrim.getTrim(), pkgPackageMap);
	    					}else{
	    						HashMap<String, ArrayList<String>> categoryPackageMap = pkgPackageMap.get(packageName);
	    						if( categoryPackageMap == null ){
	    							categoryPackageMap = new HashMap();
	    							ArrayList<String> packageOpValues = new ArrayList();
		    						packageOpValues.add(tmpCode);
		    						categoryPackageMap.put(optionCategory, packageOpValues);
		    						pkgPackageMap.put(packageName, categoryPackageMap);
	    						}else{
	    							ArrayList<String> packageOpValues = categoryPackageMap.get(optionCategory);
	    							if( packageOpValues == null){
	    								packageOpValues = new ArrayList();
	    								packageOpValues.add(tmpCode);
			    						categoryPackageMap.put(optionCategory, packageOpValues);
	    							}else{
	    								if( !packageOpValues.contains(tmpCode) ){
	    									packageOpValues.add(tmpCode);
	    								}
	    							}
	    						}
	    					}
    					}
    					
    					//Drive Type Map 셋팅
    					if( driveType != null && !driveType.equals("")){
	    					HashMap<String, HashMap<String, HashMap<String, ArrayList<String>>>> driveTypeMap = ospec.getDriveTypeMap();
	    					HashMap<String, HashMap<String, ArrayList<String>>> trimDriveTypeMap = driveTypeMap.get(opTrim.getTrim());
	    					if( trimDriveTypeMap == null ){
	    						trimDriveTypeMap = new HashMap();
	    						HashMap<String, ArrayList<String>> categoryDirveMap = new HashMap();
	    						ArrayList<String> driveOpValues = new ArrayList();
	    						driveOpValues.add(tmpCode);
	    						categoryDirveMap.put(optionCategory, driveOpValues);
	    						trimDriveTypeMap.put(packageName, categoryDirveMap);
	    						driveTypeMap.put(opTrim.getTrim(), trimDriveTypeMap);
	    					}else{
	    						HashMap<String, ArrayList<String>> categoryDriveMap = trimDriveTypeMap.get(driveType);
	    						if( categoryDriveMap == null ){
	    							categoryDriveMap = new HashMap();
	    							ArrayList<String> driveOpValues = new ArrayList();
		    						driveOpValues.add(tmpCode);
		    						categoryDriveMap.put(optionCategory, driveOpValues);
		    						trimDriveTypeMap.put(packageName, categoryDriveMap);
	    						}else{
	    							ArrayList<String> driveOpValues = categoryDriveMap.get(optionCategory);
	    							if( driveOpValues == null){
	    								driveOpValues = new ArrayList();
	    								driveOpValues.add(tmpCode);
	    								categoryDriveMap.put(optionCategory, driveOpValues);
	    							}else{
	    								if( !driveOpValues.contains(tmpCode) ){
	    									driveOpValues.add(tmpCode);
	    								}
	    							}
	    						}
	    					}
    					}    					
    					
						OpValueName ovn = new OpValueName(optionCategory, categoryDesc, tmpCode, codeDesc);
						if (!ospec.getOpNameList().contains(ovn)) {
							ospec.getOpNameList().add(ovn);
						}    					
    				}
    			}
    			row_order++;
    		}
    		
    	}
    	
    	return ospec;
		
	}
	
	private TCComponentItemRevision[] getOspecRevision(String gModel, String project, String version, String dateStr) throws Exception{
		TCSession session = CustomUtil.getTCSession();
		HashMap<String, String> param = new HashMap();
		if( gModel != null ){
			param.put("ID", gModel+"*");
		}
		
		if( project != null ){
			param.put("Project", project);
		}
		
		if( version != null){
			param.put("Revision", version);
		}
		
		if( dateStr != null){
			param.put("SYMC_Search_OspecSet_Revision", dateStr);
		}
		
		ArrayList names = new ArrayList();
		ArrayList values = new ArrayList();
		Set<String> keys = param.keySet();
		for( String name : keys){
			String value = param.get(name);
			names.add(name);
			values.add(value);
		}
		TCComponentItemRevision[] coms = (TCComponentItemRevision[])CustomUtil.queryComponent("SYMC_Search_OspecSet_Revision", (String[])names.toArray(new String[names.size()]), (String[])values.toArray( new String[values.size()]));
		
		return coms;
	}	
	
	public static int getEndVariantIdx(Sheet sheet){
    	int endVariantIdx = -1;
    	for( int column = 7; column < sheet.getColumns(); column++){
    		Cell cell = sheet.getCell(column, 2);
    		if( cell instanceof LabelCell){
    			LabelCell label = (LabelCell)cell;
    			if("Eff-IN".equals(label.getString().trim())){
    				endVariantIdx = column ;
    			}
    		}
    	}		
    	
    	return endVariantIdx;
	}	
	
	public static ArrayList<String> getGModelList() throws Exception{
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		DataSet ds = new DataSet();
		try {
			ds.put("NO-PARAM", null);
			ArrayList<String> gModelList = (ArrayList<String>)remote.execute("com.kgm.service.OSpecService", "getGModel", ds);
			return gModelList;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw e;
		}
	}
	
	public static void refreshGmodel(JComboBox cbGmodel) throws Exception{
		
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		DataSet ds = new DataSet();
		try {
			ds.put("NO-PARAM", null);
			for( int i = cbGmodel.getModel().getSize() - 1; i >= 0; i--){
				cbGmodel.removeItemAt(i);
			}
			cbGmodel.addItem(SELECT_G_MODEL);
			ArrayList<String> gModelList = (ArrayList<String>)remote.execute("com.kgm.service.OSpecService", "getGModel", ds);
			for( int i = 0; gModelList!=null && i < gModelList.size(); i++){
				cbGmodel.addItem(gModelList.get(i));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw e;
		}
		
	}	
	
	public static ArrayList<String> getProjectList(String gModel) throws Exception{
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		DataSet ds = new DataSet();
		if( gModel != null && !gModel.equals("")){
			ds.put("G_MODEL", gModel);
		}
		
		try {
			ArrayList<String> gModelList = (ArrayList<String>)remote.execute("com.kgm.service.OSpecService", "getProject", ds);
			return gModelList;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw e;
		}
	}
	
	public static void refreshProject(JComboBox cbGmodel, JComboBox cbProject) throws Exception{
		
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		DataSet ds = new DataSet();
		if( cbGmodel.getSelectedIndex() > -1){
			String gModel = (String)cbGmodel.getSelectedItem();
			if( gModel != null && !gModel.equals(SELECT_G_MODEL)){
				ds.put("G_MODEL", gModel);
			}
		}
		
		try {
			for( int i = cbProject.getModel().getSize() - 1; i >= 0; i--){
				cbProject.removeItemAt(i);
			}
			cbProject.addItem(SELECT_PROJECT);
			if( ds.isEmpty()){
				ds.put("NO-PARAM", null);
			}
			ArrayList<String> gModelList = (ArrayList<String>)remote.execute("com.kgm.service.OSpecService", "getProject", ds);
			for( int i = 0; gModelList!=null && i < gModelList.size(); i++){
				cbProject.addItem(gModelList.get(i));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw e;
		}
		
	}		
	
	public static String getCategory(String optionValue){
		
		if( optionValue == null || optionValue.length() < 4){
			return null;
		}
		
		if( optionValue.equals("3C61") || optionValue.equals("3WCC")){
			return "301";
		}else if( optionValue.equals("3F02") || optionValue.equals("3W02")){
			return "302";
		}else if( optionValue.equals("3D00") || optionValue.equals("3WDD")){
			return "303";
		}else if( optionValue.equals("3B16") || optionValue.equals("3W16")){
			return "304";
		}else if( optionValue.equals("3A17") || optionValue.equals("3W17")){
			return "305";
		}else if( optionValue.equals("3A51") || optionValue.equals("3W51")){
			return "321";
		}else if( optionValue.equals("3E35") || optionValue.equals("3W35")){
			return "342";
		}else if( optionValue.equals("3D01") || optionValue.equals("3W01")){
			return "344";
		}else if( optionValue.equals("3A46") || optionValue.equals("3W46")){
			return "345";
		}else if( optionValue.equals("3D25") || optionValue.equals("3W25")){
			return "346";
//		if( optionValue.equals("3C61") || optionValue.equals("3WCC")|| optionValue.equals("3C61") || optionValue.equals("3W02")
//				|| optionValue.equals("3D00") || optionValue.equals("3WDD")|| optionValue.equals("3B16") || optionValue.equals("3W16")
//				|| optionValue.equals("3A17") || optionValue.equals("3W17")|| optionValue.equals("3A51") || optionValue.equals("3W51")
//				|| optionValue.equals("3E35") || optionValue.equals("3W35")|| optionValue.equals("3A46") || optionValue.equals("3W46")
//				|| optionValue.equals("3D01") || optionValue.equals("3W01")){
//			return "301";	
		}else{
			return optionValue.substring(0,	3);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static void resetColOrder(OSpec oldOspec, OSpec newOspec) {
		ArrayList<OpTrim> oldTrims = (ArrayList<OpTrim>) oldOspec.getTrimList();
		ArrayList<OpTrim> newTrims = (ArrayList<OpTrim>) newOspec.getTrimList();
		
		// Col Order가 입력되어있는 Trim을 통합함.
		ArrayList<OpTrim> trims = new ArrayList();
		
		for (int inx = 0; inx < oldTrims.size(); inx++) {
			OpTrim oldTrim = oldTrims.get(inx);
			String sTrim = oldTrim.getTrim();
			String sArea = oldTrim.getArea();
			String sPass = oldTrim.getPassenger();
			String sEngine = oldTrim.getEngine();
			String sGrade = oldTrim.getGrade();
			
			if (sTrim == null || sArea == null || sPass == null || sEngine == null || sGrade == null) {
				continue;
			}
			
			boolean isContain = false;
			for (int jnx = 0; jnx < trims.size(); jnx++) {
				OpTrim trim = trims.get(jnx);
				String sTrim2 = trim.getTrim();
				
				if (sTrim.equals(sTrim2)) {
					isContain = true;
				}
			}
			
			if (!isContain) {
				trims.add(oldTrim);
			}
		}
		
		for (int inx = 0; inx < newTrims.size(); inx++) {
			OpTrim newTrim = newTrims.get(inx);
			String sTrim = newTrim.getTrim();
			String sArea = newTrim.getArea();
			String sPass = newTrim.getPassenger();
			String sEngine = newTrim.getEngine();
			String sGrade = newTrim.getGrade();
			
			if (sTrim == null || sArea == null || sPass == null || sEngine == null || sGrade == null) {
				continue;
			}
			
			boolean isContain = false;
			for (int jnx = 0; jnx < trims.size(); jnx++) {
				OpTrim trim = trims.get(jnx);
				String sTrim2 = trim.getTrim();
				
				if (sTrim.equals(sTrim2)) {
					isContain = true;
				}
			}
			
			if (!isContain) {
				trims.add(newTrim);
			}
		}
		
		Collections.sort(trims);
		
		// 실제 OSpec 내에 입력되어있는 OpTrim들의 Col Order를 trims에 입력된 순으로 변경
		// Old OSpec
		HashMap<String, OpTrim> hmOldTrims = oldOspec.getTrims();
		// New OSpec
		HashMap<String, OpTrim> hmNewTrims = newOspec.getTrims();
		
		for (int inx = 0; inx < trims.size(); inx++) {
			OpTrim opTrim = trims.get(inx);
			
			String sTrim = opTrim.getTrim();
			OpTrim otOldTarget = hmOldTrims.get(sTrim);
			OpTrim otNewTarget = hmNewTrims.get(sTrim);
			
			otOldTarget.setColOrder(inx);
			otNewTarget.setColOrder(inx);
		}
	}

	/*
	 * 프로젝트 코드로 Ospec 정보를 바로 가져오도록 함.
	 */
	public static OSpec getOspec(String projectCode) throws Exception
	{
		OSpec ospec = null;

		TCComponentItemRevision ospecRev = CustomUtil.findLatestItemRevision("S7_OspecSet", "OSI-"+projectCode);
		String ospecStr = ospecRev.getProperty("item_id") + "-" + ospecRev.getProperty("item_revision_id");
		AIFComponentContext[] context = ospecRev.getChildren(SYMCECConstant.ITEM_DATASET_REL);
		for( int i = 0; context != null && i < context.length; i++){
			TCComponentDataset ds = (TCComponentDataset)context[i].getComponent();
			if( ospecStr.equals(ds.getProperty("object_name"))){
				File[] files = DatasetService.getFiles(ds);
				ospec = OpUtil.getOSpec(files[0]);
				break;
			};
		}
		return ospec;
	}
}
