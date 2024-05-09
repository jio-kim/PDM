package com.kgm.soa.ospec;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import jxl.Cell;
import jxl.LabelCell;
import jxl.Sheet;
import jxl.Workbook;

public class OpUtil {
	
	private static final int OSPEC_AREA = 2;
	private static final int OSPEC_PASSENGER = 3;
	private static final int OSPEC_ENGINE = 4;
	private static final int OSPEC_GRADE = 5;
	private static final int OSPEC_TRIM = 6;	
	public static final String SELECT_G_MODEL = "Select a G-Model";
	public static final String SELECT_PROJECT = "Select a Project";
	
	/**
	 * Trim(Variant)
	 * 
	 * @param sheet
	 * @param endVariantIdx
	 * @return
	 */
	public static HashMap<String, OpTrim> getTrimMap(Sheet sheet, int endVariantIdx){
    	int iHavingTypeColumn = -1, iHavingPassengerColumn = -1, iHavingEngineColumn = -1
    			,iHavingGradeColumn = -1, iHavingTrimColumn = -1;
    	OpTrim opTrim = null;
    	HashMap<String, OpTrim> trimMap = new HashMap<String, OpTrim>();
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
        			//BlankCell�� �Ǵ�.
        			// ���� Column���� �׷��� �°�
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
		
		// Excel Load Start
    	Workbook workBook = Workbook.getWorkbook(file);
    	Sheet sheet = workBook.getSheet(0);
    	
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
    	HashMap<String, ArrayList<Option>> options = new HashMap<String, ArrayList<Option>>();
    	ospec.setOptions(options);
    	ospec.setOpNameList(new ArrayList<OpValueName>());
    	ospec.setCategory(new HashMap<String,HashMap<String,OpCategory>>());
    	ospec.setPackageMap(new HashMap<String, HashMap<String, HashMap<String, ArrayList<String>>>>());
    	ospec.setDriveTypeMap(new HashMap<String, HashMap<String, HashMap<String, ArrayList<String>>>>());
    	
//    	Registry customRegistry = Registry.getRegistry("com.kgm.common.bundlework.imp.imp");
    	int startVariantRowIdx = 6;//Integer.parseInt(customRegistry.getString("BWVariantOptionImpDialog.startVariantRowIdx"));//	6
    	int startOptionCodeColumnIdx = 3;//Integer.parseInt(customRegistry.getString("BWVariantOptionImpDialog.startOptionCodeColumnIdx"));//	3
    	int startOptionRowIdx = 8;//Integer.parseInt(customRegistry.getString("BWVariantOptionImpDialog.startOptionRowIdx"));//	8
    	int optionCategoryDescColumnIdx = 1;//Integer.parseInt(customRegistry.getString("BWVariantOptionImpDialog.optionCategoryDescColumnIdx"));//	1
    	int optionCodeDescColumnIdx = 2;//Integer.parseInt(customRegistry.getString("BWVariantOptionImpDialog.optionCodeDescColumnIdx"));//	2
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
    			
    			//�ɼ� �ڵ忡�� �� 3�ڸ��� �ɼ� ī�װ?�̴�.
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
    					ArrayList<Option> opList = options.get(opTrim.getTrim());
    					if( opList == null){
    						opList = new ArrayList<Option>();
    						opList.add(option);
    						options.put(opTrim.getTrim(), opList);
    					}else{
    						opList.add(option);
    					}
    					
    					HashMap<String, HashMap<String, OpCategory>> categories = ospec.getCategory();
    					HashMap<String, OpCategory> categoryMap = categories.get(opTrim.getTrim());
    					if( categoryMap == null){
    						categoryMap = new HashMap<String, OpCategory>();
    						
    						OpCategory category = new OpCategory(optionCategory, categoryDesc);
    						ArrayList<Option> opValueList = new ArrayList<Option>();
    						opValueList.add(option);
    						category.setOpValueList(opValueList);
    						
    						categoryMap.put(optionCategory, category);
    						categories.put(opTrim.getTrim(), categoryMap);
    						
    					}else{
    						OpCategory category = categoryMap.get(optionCategory);
    						if( category == null){
    							category = new OpCategory(optionCategory, categoryDesc);
    							ArrayList<Option> opValueList = new ArrayList<Option>();
        						opValueList.add(option);
        						category.setOpValueList(opValueList);
        						categoryMap.put(optionCategory, category);
    						}else{
    							category.getOpValueList().add(option);
    						}
    					}
    					
    					//Package Map ����
    					if( packageName != null && !packageName.equals("")){
	    					HashMap<String, HashMap<String, HashMap<String, ArrayList<String>>>> packageMap = ospec.getPackageMap();
	    					HashMap<String, HashMap<String, ArrayList<String>>> pkgPackageMap = packageMap.get(opTrim.getTrim());
	    					if( pkgPackageMap == null ){
	    						pkgPackageMap = new HashMap<String, HashMap<String, ArrayList<String>>>();
	    						HashMap<String, ArrayList<String>> categoryPackageMap = new HashMap<String, ArrayList<String>>();
	    						ArrayList<String> packageOpValues = new ArrayList<String>();
	    						packageOpValues.add(tmpCode);
	    						categoryPackageMap.put(optionCategory, packageOpValues);
	    						pkgPackageMap.put(packageName, categoryPackageMap);
	    						packageMap.put(opTrim.getTrim(), pkgPackageMap);
	    					}else{
	    						HashMap<String, ArrayList<String>> categoryPackageMap = pkgPackageMap.get(packageName);
	    						if( categoryPackageMap == null ){
	    							categoryPackageMap = new HashMap<String, ArrayList<String>>();
	    							ArrayList<String> packageOpValues = new ArrayList<String>();
		    						packageOpValues.add(tmpCode);
		    						categoryPackageMap.put(optionCategory, packageOpValues);
		    						pkgPackageMap.put(packageName, categoryPackageMap);
	    						}else{
	    							ArrayList<String> packageOpValues = categoryPackageMap.get(optionCategory);
	    							if( packageOpValues == null){
	    								packageOpValues = new ArrayList<String>();
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
    					
    					//Drive Type Map ����
    					if( driveType != null && !driveType.equals("")){
	    					HashMap<String, HashMap<String, HashMap<String, ArrayList<String>>>> driveTypeMap = ospec.getDriveTypeMap();
	    					HashMap<String, HashMap<String, ArrayList<String>>> trimDriveTypeMap = driveTypeMap.get(opTrim.getTrim());
	    					if( trimDriveTypeMap == null ){
	    						trimDriveTypeMap = new HashMap<String, HashMap<String, ArrayList<String>>>();
	    						HashMap<String, ArrayList<String>> categoryDirveMap = new HashMap<String, ArrayList<String>>();
	    						ArrayList<String> driveOpValues = new ArrayList<String>();
	    						driveOpValues.add(tmpCode);
	    						categoryDirveMap.put(optionCategory, driveOpValues);
	    						trimDriveTypeMap.put(packageName, categoryDirveMap);
	    						driveTypeMap.put(opTrim.getTrim(), trimDriveTypeMap);
	    					}else{
	    						HashMap<String, ArrayList<String>> categoryDriveMap = trimDriveTypeMap.get(driveType);
	    						if( categoryDriveMap == null ){
	    							categoryDriveMap = new HashMap<String, ArrayList<String>>();
	    							ArrayList<String> driveOpValues = new ArrayList<String>();
		    						driveOpValues.add(tmpCode);
		    						categoryDriveMap.put(optionCategory, driveOpValues);
		    						trimDriveTypeMap.put(packageName, categoryDriveMap);
	    						}else{
	    							ArrayList<String> driveOpValues = categoryDriveMap.get(optionCategory);
	    							if( driveOpValues == null){
	    								driveOpValues = new ArrayList<String>();
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
        }else if( optionValue.indexOf("_STD") > -1 || optionValue.indexOf("_OPT") > -1 || optionValue.equals("NONE")){
            return "TRIM";
        }else{
            return optionValue.substring(0, 3);
        }
	}
}
