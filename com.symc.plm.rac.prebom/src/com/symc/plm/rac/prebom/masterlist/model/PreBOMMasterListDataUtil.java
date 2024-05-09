package com.symc.plm.rac.prebom.masterlist.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellRangeAddress;

import com.kgm.common.remote.DataSet;
import com.kgm.common.remote.SYMCRemoteUtil;
import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.PreferenceService;
import com.kgm.common.utils.SYMTcUtil;
import com.kgm.common.utils.TcDefinition;
import com.symc.plm.rac.prebom.common.util.SDVPreBOMUtilities;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPreferenceService;

import jxl.Workbook;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

/**
 * [SR160621-031][20160707] taeku.jeong
 * �ְ� ������ ������ Pre-BOM �����͸� Ȱ���Ͽ� ������ ����� �� �ִ� ��� ����
 */
public class PreBOMMasterListDataUtil {
	
	private String exportTargetFolderPath = System.getenv("user.home")+"\\Documents";
	
	// Template�� ���ǵ� Data ��¿� ���� Sheet�� �̸�.
	private static String SheetName = "MasterList";
	
	// Header ������ �߷��Ҷ� ����� ���� ����
	private static int UssageHeaderTitleRow = 0;  // Column, Row �� Index�� 0���� ����
	private static int AraeHeaderRow = 1;  // Column, Row �� Index�� 0���� ����
	private static int PassengerHeaderRow = 2;  // Column, Row �� Index�� 0���� ����
	private static int EngineHeaderRow = 3;  // Column, Row �� Index�� 0���� ����
	private static int GradeHeaderRow = 4;  // Column, Row �� Index�� 0���� ����
	private static int TrimHeaderRow = 5;  // Column, Row �� Index�� 0���� ����
	
	// Excel Template�� ����� �׷��� �ִ� ���������� �� Row ���� Row Index
	private static int TemplateEndRowIndex = 18;
	// Ussage�� ǥ�� ���� �Ǵ� Column�� Index
	private static int UssageHeaderStartColumn = 19;  // Column, Row �� Index�� 0���� ����
	
	// Data�� Queyr�ϱ����� ������ SQLMap Service
	private static final String PREBOM_USSAGE_QUERY_SERVICE = "com.kgm.service.PreBOMUssageExportService";

	public PreBOMMasterListDataUtil(){
		
	}
	
	/**
	 * ��´���̵� Project Data ���������� ����ڰ� ������ ���ֵ��� Project List�� �������� �Լ�
	 */
	public Hashtable<String, MasterAndUssageFindKey> getProjectDataLIst(){
		
		Hashtable<String, MasterAndUssageFindKey> masterAndUssageFindKeyHash = null;
		
		SYMCRemoteUtil remoteQuery = new SYMCRemoteUtil();
		
		ArrayList<HashMap> resultList = null;
        
		DataSet ds = new DataSet();
        ds.put("blankedInput", "");

        try {
			resultList = (ArrayList<HashMap>) remoteQuery.execute(PREBOM_USSAGE_QUERY_SERVICE, "getExportTargetProjectList", ds);
		} catch (Exception e) {
			e.printStackTrace();
		}

        masterAndUssageFindKeyHash = null;
        if(resultList!=null && resultList.size()>0){
        	masterAndUssageFindKeyHash = new Hashtable<String, MasterAndUssageFindKey>();
        }
        for (int i = 0;resultList!=null && i < resultList.size(); i++) {
        	
        	HashMap rowHash  = resultList.get(i);
        	
        	// ����ڿ��� ���̴� ������ Data ���� �ð�����
        	String projectCode = (String)rowHash.get("PROJECT_CODE");
        	String eaiCreateTime = (String)rowHash.get("LATESTEAICREATEDATE");
        	
        	// �Ʒ��� ������ ������ Table���� �˻��� �Ҷ� ���� ������ Data ���� �ð��� �ȴ�.
        	String mCreateTime = (String)rowHash.get("MASTER_CREATE_TIME");
        	String oCreateTime = (String)rowHash.get("OSPEC_CREATE_TIME");
        	String uCreateTime = (String)rowHash.get("USSAGE_CREATE_TIME");
        	
        	MasterAndUssageFindKey tmpMasterAndUssageFindKey = new MasterAndUssageFindKey(projectCode, eaiCreateTime, mCreateTime,  oCreateTime, uCreateTime);
        	masterAndUssageFindKeyHash.put(projectCode, tmpMasterAndUssageFindKey);

		}
        
        return masterAndUssageFindKeyHash;
	}
	
	/**
	 * Project��  Ussage Header ������ �о� �´�.
	 */
	public UssageHeaderColum[] getUssageColumnHeaderData(MasterAndUssageFindKey masterAndUssageFindKey ){
		
		UssageHeaderColum[] ussageHeaderColums = null;
		
		SYMCRemoteUtil remoteQuery = new SYMCRemoteUtil();
		
		ArrayList<HashMap> resultList = null;
        
		DataSet ds = new DataSet();
        ds.put("project_code", masterAndUssageFindKey.projectCode);
        ds.put("master_eai_create", masterAndUssageFindKey.masterEAICreateTime);
        ds.put("ospec_eai_create", masterAndUssageFindKey.ospecEAICreateTime);

        try {
			resultList = (ArrayList<HashMap>) remoteQuery.execute(PREBOM_USSAGE_QUERY_SERVICE, "geProjectUssageHeaderList", ds);
		} catch (Exception e) {
			e.printStackTrace();
		}
        
        if(resultList!=null && resultList.size()>0){
        	ussageHeaderColums = new UssageHeaderColum[resultList.size()];
        }
        for (int i = 0;resultList!=null && i < resultList.size(); i++) {
        	
        	HashMap rowHash  = resultList.get(i);
        	
        	String area = (String)rowHash.get("AREA");
        	String passenger =  (String)rowHash.get("PASSENGER");
        	String engine =  (String)rowHash.get("ENGINE");
        	String grade =  (String)rowHash.get("GRADE");
        	String trim =  (String)rowHash.get("TRIM");
        	String ussageKey = (String)rowHash.get("USSAGE_KEY");

        	UssageHeaderColum aColumnData = new UssageHeaderColum(area, passenger, engine, grade, trim, ussageKey);
        	ussageHeaderColums[i] = aColumnData;
		}
        
        return ussageHeaderColums;
		
	}
	
	/**
	 * Project Data ����� ���� Excle File �� Format�� Ussage �κ��� Column���� �߰��ϰ� Header �� �غ� �Ѵ�.
	 * @param masterAndUssageFindKey
	 * @param ussageHeaderColums
	 * @return
	 */
	public String readyExcelFile(MasterAndUssageFindKey masterAndUssageFindKey, UssageHeaderColum[] ussageHeaderColums){
	
		String projectCode = masterAndUssageFindKey.projectCode;
		String makingDate = masterAndUssageFindKey.masterEAICreateTime;
		
		String newFileName = this.exportTargetFolderPath+"\\Pre-BOMMasterList_"+projectCode+"["+makingDate+"].xls";
		String newTemplateFilePath = getDownloadAndRenamedTemplateFilePath(newFileName);
		
		if(newTemplateFilePath==null || (newTemplateFilePath!=null && newTemplateFilePath.trim().length()<0)){
			return null;
		}
		
		String newFilePath =  newTemplateFilePath;
		
		//----------------------------------------------------------------
		// Excel File�� Column�� �غ� �ϴ� �κ��� ���� �Ѵ�.
		//----------------------------------------------------------------
		
		// 001. excel file�� �غ��Ѵ�.
		createNewXLSFile(newTemplateFilePath, newFilePath);
		
		// 002. �ʿ��� Column�� �߰��Ѵ�.
		int insertCount = ussageHeaderColums.length - 1;
		if(insertCount>0){
			insertUssageColumns(newFilePath, 
					(PreBOMMasterListDataUtil.UssageHeaderStartColumn), 
					insertCount);
		}
		
		// 003. Column�� �̸��� ����ϰ� Cell Style�� �����Ѵ�.
		readyHeaderColumFormat(newFilePath, ussageHeaderColums);
		
		return newFilePath;
	}
	
	public void setExportTargetFolderPath(String folderPath){
		exportTargetFolderPath = folderPath;
		File dir = new File(exportTargetFolderPath);
		if(dir!=null && dir.isDirectory() && dir.exists()==false){
			dir.mkdir();
		}
	}

	/**
	 * Mast Data�� �о� �־��� File�� ����ϴ� �κ�
	 * @param masterAndUssageFindKey
	 * @param ussageHeaderColums
	 * @param excelFilePath
	 */
	public void printProjectMasterData(MasterAndUssageFindKey masterAndUssageFindKey, 
			UssageHeaderColum[] ussageHeaderColums, String excelFilePath){
		
		SYMCRemoteUtil remoteQuery = new SYMCRemoteUtil();
		
		ArrayList<HashMap> resultList = null;
	    
		DataSet ds = new DataSet();
	    ds.put("project_code", masterAndUssageFindKey.projectCode);
	    ds.put("master_eai_create", masterAndUssageFindKey.masterEAICreateTime);
	
	    try {
			resultList = (ArrayList<HashMap>) remoteQuery.execute(PREBOM_USSAGE_QUERY_SERVICE, "geProjectMasterDataList", ds);
		} catch (Exception e) {
			e.printStackTrace();
		}
	    
		HSSFWorkbook excelWorkbook = null;
		try {
			excelWorkbook = new HSSFWorkbook(new FileInputStream(excelFilePath));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		HSSFSheet sheet = excelWorkbook.getSheet(PreBOMMasterListDataUtil.SheetName);
	    
	    int mastData2ndStartColumnIndex = PreBOMMasterListDataUtil.UssageHeaderStartColumn + ussageHeaderColums.length;
	    int dataStartRowIndex = PreBOMMasterListDataUtil.TrimHeaderRow + 1;
	
	    boolean isExistRow = true;
	    
	  //[CSH][20180514] COST ������ ���� �ִ� �׷� or ����ڸ� ���̵��� ���� ��û
  		String[] costViewableGroup = PreferenceService.getValues(TCPreferenceService.TC_preference_site, "SYMC_Cost_Viewable_Group");
  		String loginGroup = SDVPreBOMUtilities.getTCSession().getCurrentGroup().toString();
  		String loginUserID = "";
  		try {
			loginUserID = SDVPreBOMUtilities.getTCSession().getUser().getUserId();
		} catch (TCException e) {
			e.printStackTrace();
		}
  		boolean isCostViewable = false;
  		if(costViewableGroup != null){
  			for(int i=0; i<costViewableGroup.length; i++){
  				if(loginUserID.equals(costViewableGroup[i])){
  					isCostViewable = true;
  					break;
  				}
  			}
  		}
	  		
	    for (int i = 0;resultList!=null && i < resultList.size(); i++) {
	    	
	    	int writeRowIndex = dataStartRowIndex + i;
	    	
	    	System.out.println("writeRowIndex = "+writeRowIndex);

	    	// Master Data Row�� �ش��ϴ� Ussage Data�� ������ Header�� ������ ���� Data�� List�� ��´�. 
	    	HashMap rowHash  = resultList.get(i);
	    	ArrayList<String> columnDataList = getRowUssageDataStringArrayList( 
	    			masterAndUssageFindKey,
	    			ussageHeaderColums,
	    			rowHash,
	    			isCostViewable);
	    	
	    	if(columnDataList==null || (columnDataList!=null && columnDataList.size()<1)){
	    		continue;
	    	}
	    	
	    	if(PreBOMMasterListDataUtil.TemplateEndRowIndex==writeRowIndex){
	    		isExistRow = false;	
	    	}
	
	    	// Excel ���Ͽ� Row�� �����Ѵ�.
	    	HSSFRow  currentRow = null;
	    	if(isExistRow==true){
	    		currentRow = sheet.getRow(writeRowIndex);
	    		if(currentRow==null){
	    			currentRow = sheet.createRow(writeRowIndex);	
	    		}
	    	}else{
	    		sheet.shiftRows(writeRowIndex, sheet.getLastRowNum(), 1);
	    		currentRow = sheet.createRow(writeRowIndex);
	    	}
	    	
	    	// �̹� Cell Style�� ���ǵǾ� �ִ� ���� Ȱ���ϱ����� ���� Row�� ã�� Row Style�� �ٿ� �ִ´�.
	    	HSSFRow beforRow = null;
	    	if(isExistRow==false){
	    		beforRow = sheet.getRow((writeRowIndex-1));
	    	}
	    	
	    	if(beforRow!=null){
	    		if(beforRow.getRowStyle()!=null){
	    			currentRow.setRowStyle(beforRow.getRowStyle());
	    		}
	    	}
	    	
	    	// ������ Row�� Cell ������ Column�� ���� ����ϰ� Cell Style�� �����Ѵ�.
	    	for (int columnIndex = 0; columnIndex < columnDataList.size(); columnIndex++) {
	
	    		HSSFCell currentCell = null;
	    		if(isExistRow==false){
	    			currentCell = currentRow.createCell(columnIndex);
	    		}else{
	    			currentCell = currentRow.getCell(columnIndex);
	    			if(currentCell==null){
	    				currentCell = currentRow.createCell(columnIndex);
	    			}
	    		}
	    		
	    		String tempCellValueString = columnDataList.get(columnIndex);
	    		if(tempCellValueString!=null && tempCellValueString.trim().length()>0){
	    			tempCellValueString = tempCellValueString.trim();
	    		}else{
	    			tempCellValueString = ""; 
	    		}
	    		currentCell.setCellValue(tempCellValueString);
	    		
	    		// ���� ������ Row�� ��� ���� Row�� ������ Column�� Cell Style�� �����Ѵ�.
	            if(isExistRow==false){
	            	currentCell.setCellStyle( beforRow.getCell(columnIndex).getCellStyle() );
	            }
			}
	
		}
	    
	    // Excel ������ �����Ѵ�.
		try {
		    FileOutputStream fileOut = new FileOutputStream(excelFilePath);
		    excelWorkbook.write(fileOut);
		    fileOut.close();
		    
		} catch (Exception err) {
			;
		} finally {
			if (excelWorkbook != null) {
				excelWorkbook = null;
			}
		}
	
	}

	/**
	 * Master Data�� Ussage ������ �о� Hash Table�� ��� Return �ϴ� �Լ�
	 * Master Data�� �� Row���� ȣ���
	 * �̺κ��� ���߿� �ѹ��� Query�ϰ� �����ϸ� �� ���� P/G�� �ɰ� ����.
	 * @param masterAndUssageFindKey
	 * @param systemRowKey
	 * @return
	 */
	private Hashtable<String, UssageDataType> geProjectUssageDataList(MasterAndUssageFindKey masterAndUssageFindKey, String systemRowKey){
		
		SYMCRemoteUtil remoteQuery = new SYMCRemoteUtil();
		
		ArrayList<HashMap> resultList = null;
        
		DataSet ds = new DataSet();
        ds.put("project_code", masterAndUssageFindKey.projectCode);
        ds.put("ussage_create_time", masterAndUssageFindKey.ussageEAICreateTime);
        ds.put("systemRowKey", systemRowKey);

        try {
			resultList = (ArrayList<HashMap>) remoteQuery.execute(PREBOM_USSAGE_QUERY_SERVICE, "geProjectUssageDataList", ds);
		} catch (Exception e) {
			e.printStackTrace();
		}
        
        Hashtable<String, UssageDataType> resultDataHash = null;
        if(resultList!=null && resultList.size()>0){
        	resultDataHash = new Hashtable<String, UssageDataType>();
        }

        for (int i = 0;resultList!=null && i < resultList.size(); i++) {
        	
        	HashMap rowHash  = resultList.get(i);
        	
        	String findedRowKey = (String)rowHash.get("SYSTEM_ROW_KEY");
        	String area = (String)rowHash.get("AREA");
        	String passenger = (String)rowHash.get("PASSENGER");
        	String engine = (String)rowHash.get("ENGINE");
        	String grade = (String)rowHash.get("GRADE");
        	String trim = (String)rowHash.get("TRIM");
        	BigDecimal usageQtyObj = (BigDecimal)rowHash.get("USAGE_QTY");
        	int usageQty = 0;
        	if(usageQtyObj!=null){
        		usageQty = usageQtyObj.intValue();
        	}
        	String usageType = (String)rowHash.get("USAGE_TYPE");
        	String qValue = (String)rowHash.get("Q_VALUE");
        	String ussageKey = (String)rowHash.get("USSAGE_KEY");       	
        	
        	UssageDataType tempUssageDataType = new UssageDataType(area, passenger, engine, grade, trim, ussageKey, 
        			usageQty, usageType, qValue);
        	if(tempUssageDataType!=null){
        		resultDataHash.put(ussageKey, tempUssageDataType);
        	}
		}
        
        return resultDataHash;
	}
	
	/**
	 * Template�� �̿��� ���� Data�� ������ File�� ����� �Լ�
	 * @param templateFilePath
	 * @param newFilePath
	 */
	private void createNewXLSFile(String templateFilePath, String newFilePath){
		
		HSSFWorkbook excelWorkbook = null;
		try {
			excelWorkbook = new HSSFWorkbook(new FileInputStream(templateFilePath));
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
		    // Excel File ����.
		    FileOutputStream fileOut = new FileOutputStream(newFilePath);
		    excelWorkbook.write(fileOut);
		    fileOut.close();
		    
		} catch (Exception err) {
			;
		} finally {
			if (excelWorkbook != null) {
				excelWorkbook = null;
			}
		}
		
	}

	/**
	 * JXL�� �̿��� Column�� �߰��ϴ� ����� �����Ѵ�.
	 * Ussage�� ��� Column�� ���������� �߰��Ǵµ� �̰��� ���� Column�� �߰��ϴ±���� �ʿ�����.
	 * PIO���� Column�� Shift �ϰ� Add�ϴ� ����� �����Ƿ� Cell Merge�� ���� ������ ������ ���ľ� ��
	 * POI�� �̿��ؼ� ���� �Ϸ��� ������ ���������� ������ �־ JXL API�� �̿�����.
	 * @param filePath
	 * @param insertIndex
	 * @param insertColumnCount
	 */
	private void insertUssageColumns(String filePath, int insertIndex, int insertColumnCount) {
		
		insertIndex = insertIndex + 1;
		
		try {   
			
			Workbook protectWorkbook = Workbook.getWorkbook(new File(filePath)); 
			WritableWorkbook aWritableWorkbook = Workbook.createWorkbook(new File(filePath), protectWorkbook);
			
			 WritableSheet worksheet=aWritableWorkbook.getSheet("MasterList");
			
			if(worksheet!=null){
				for (int i = 0; i < insertColumnCount; i++) {
					worksheet.insertColumn(insertIndex);	
				}
			}
			aWritableWorkbook.write();
			aWritableWorkbook.close();
		}
		catch (Exception i){
			i.printStackTrace();
		}
	}

	private void readyHeaderColumFormat(String filePath, UssageHeaderColum[] ussageHeaderColums){
		
		
		HSSFWorkbook excelWorkbook = null;
		try {
			excelWorkbook = new HSSFWorkbook(new FileInputStream(filePath));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		HSSFSheet sheet = excelWorkbook.getSheet(PreBOMMasterListDataUtil.SheetName);
		int lastRowIndex = sheet.getLastRowNum();
	
		int headerRowIndex = PreBOMMasterListDataUtil.UssageHeaderTitleRow;
		int areaRowIndex = PreBOMMasterListDataUtil.AraeHeaderRow = 1;
		int passengerRowIndex = PreBOMMasterListDataUtil.PassengerHeaderRow = 2;
		int engineRowIndex = PreBOMMasterListDataUtil.EngineHeaderRow = 3;
		int gradeRowIndex = PreBOMMasterListDataUtil.GradeHeaderRow = 4;
		int trimRowIndex = PreBOMMasterListDataUtil.TrimHeaderRow = 5;
	
		int startColumnIndex = PreBOMMasterListDataUtil.UssageHeaderStartColumn;
		
		for (int i = 0; i <= lastRowIndex; i++) {
		
			int currentRowIndex = i;
			HSSFRow currentRow = sheet.getRow(currentRowIndex);
			
			HSSFCell startcell = currentRow.getCell(startColumnIndex);
			
			for (int j = 0; j < ussageHeaderColums.length; j++) {
				
				int columnDataIndex = j;
				
				int currentColumnIndex = startColumnIndex + columnDataIndex;
				
				HSSFCell cell = currentRow.getCell(currentColumnIndex);
				if(cell==null){
					cell = currentRow.createCell(currentColumnIndex);
				}
				
				String cellValue = null;
				if(currentRowIndex==headerRowIndex){
					cellValue = "Ussage";
				}else if(currentRowIndex==areaRowIndex){
					cellValue = ussageHeaderColums[columnDataIndex].area;
				}else if(currentRowIndex==passengerRowIndex){
					cellValue = ussageHeaderColums[columnDataIndex].passenger;
				}else if(currentRowIndex==engineRowIndex){
					cellValue = ussageHeaderColums[columnDataIndex].engine;
				}else if(currentRowIndex==gradeRowIndex){
					cellValue = ussageHeaderColums[columnDataIndex].grade;
				}else if(currentRowIndex==trimRowIndex){
					cellValue = ussageHeaderColums[columnDataIndex].trim;
				}else if(currentRowIndex>trimRowIndex){
					cellValue = "";
				}
				
				cell.setCellValue(cellValue);
				if(currentColumnIndex>startColumnIndex){
					HSSFCellStyle  cellStyle = startcell.getCellStyle();
					if(cellStyle!=null){
						cell.setCellStyle(cellStyle);
					}
				}
			}
			
		}
		
		// Ussage Header Row Merge
		int firstRow = UssageHeaderTitleRow;
		int lastRow = UssageHeaderTitleRow; 
		int firstCol = startColumnIndex;
		int lastCo = startColumnIndex + (ussageHeaderColums.length - 1);
		
		CellRangeAddress aCellRangeAddress = new CellRangeAddress(firstRow, lastRow, firstCol, lastCo);
		sheet.addMergedRegion(aCellRangeAddress);
		
		int rowIndex = 0;
		
		int mergeStartIndex = 0;
		int mergeEndIndex = 0;
	
		String beforCellValue = null;
		String compareTargetKeyStr = null;
		
		for (int k = areaRowIndex; k <= gradeRowIndex; k++) {
			
			rowIndex = k;
			
			int[] makeIndex = null;
			if(rowIndex==areaRowIndex){
				makeIndex = new int[]{0};
			}else if(rowIndex==passengerRowIndex){
				makeIndex = new int[]{0, 1};
			}else if(rowIndex==engineRowIndex){
				makeIndex = new int[]{0, 1, 2};
			}else if(rowIndex==gradeRowIndex){
				makeIndex = new int[]{0, 1, 2, 3};
			}
	
			int ussageHeaderSeqIndex = 0;
			
			beforCellValue = null;
			mergeStartIndex = startColumnIndex;
			int endIndex = startColumnIndex +  ussageHeaderColums.length;
			
			for (int i = startColumnIndex; i < endIndex; i++) {
				
				int currentColIndex = i;
				
				String currentColumnKeyStr = ussageHeaderColums[ussageHeaderSeqIndex].ussageKey;
				String[] spKey = currentColumnKeyStr.trim().split(":");
				
				compareTargetKeyStr = null;
				for (int j = 0; j <makeIndex.length; j++) {
					if(j==0){
						compareTargetKeyStr = spKey[ makeIndex[j] ];
					}else{
						compareTargetKeyStr = compareTargetKeyStr+":"+spKey[ makeIndex[j] ];
					}
				}
				
				if(beforCellValue!=null && 
						compareTargetKeyStr!=null && 
						beforCellValue.equalsIgnoreCase(compareTargetKeyStr)==false){
					
					if(mergeEndIndex>mergeStartIndex){
						// Merge
						CellRangeAddress cellRangeAddress = new CellRangeAddress(rowIndex, rowIndex, mergeStartIndex, mergeEndIndex);
						sheet.addMergedRegion(cellRangeAddress);
					}
					
					mergeStartIndex = currentColIndex;
				}
				
				beforCellValue = compareTargetKeyStr;
				mergeEndIndex = currentColIndex;
				
				ussageHeaderSeqIndex++;
			}
			
			if(mergeEndIndex>mergeStartIndex){
				CellRangeAddress cellRangeAddress = new CellRangeAddress(rowIndex, rowIndex, mergeStartIndex, mergeEndIndex);
				sheet.addMergedRegion(cellRangeAddress);
			}
			
		}
	
		try {
		    // Excel File ����.
		    FileOutputStream fileOut = new FileOutputStream(filePath);
		    excelWorkbook.write(fileOut);
		    fileOut.close();
		    
		} catch (Exception err) {
			;
		} finally {
			if (excelWorkbook != null) {
				excelWorkbook = null;
			}
		}
		
	}

	private ArrayList<String> getRowUssageDataStringArrayList( 
			MasterAndUssageFindKey masterAndUssageFindKey,
			UssageHeaderColum[] ussageHeaderColums,
			HashMap rowHash, boolean  isCostViewable){
		
		String listId = (String)rowHash.get("LIST_ID");
		String systemRowKey = (String)rowHash.get("SYSTEM_ROW_KEY");
		
		DecimalFormat df = new DecimalFormat("00");
		//[SR180903-026]vehPart mapping ���� �����ֱ� ���� ColumnCount ����.
		//20201021 seho EJS Column �߰��� �� column ���� 1 ������.
		//[CF-1706] WEIGHT MANAGEMENT Į�� �߰��� �� column ���� 1 ������(54->55). by ������(20201223)		
		int targetColumnCount = 55;//�÷� �߰�/���� �� ���� �ʿ�.
		String[] valueStringList = new String[targetColumnCount];
		for (int i = 0; i < targetColumnCount; i++) {
			String columnName = "MAST_COL"+df.format((long)(i+1));
			String valueString = (String)rowHash.get(columnName);
			if(valueString==null || (valueString!=null && valueString.trim().length()<1)){
				valueString = "";
			}else{
				valueString = valueString.trim();
			}
			//[CSH][20180514] Cost ������ ���� �ִ� �׷� or ����ڸ� ���̵��� ���� ��û
			//[CF-1706] WEIGHT MANAGEMENT Į�� �߰��� MAST_COL��ȣ ����(PreBOMUssageExportMapper ����) by ������(20201223)	
			if(!isCostViewable && (columnName.equals("MAST_COL30") || columnName.equals("MAST_COL31") || columnName.equals("MAST_COL48")|| columnName.equals("MAST_COL49")|| columnName.equals("MAST_COL50")|| columnName.equals("MAST_COL51")|| columnName.equals("MAST_COL52"))){
				valueString = "";
			}
			
			valueStringList[i] = valueString;
		}
		
		// Ussage Data�� ����Ѵ�.
		Hashtable<String, UssageDataType> ussageDataHash = null;
		if(systemRowKey!=null && systemRowKey.trim().length()>0){
			ussageDataHash = geProjectUssageDataList(masterAndUssageFindKey, systemRowKey);
		}
		
		int columnNo = 0;
		ArrayList<String> columnDataList = new ArrayList<String>();
		for (int i = 0; i < this.UssageHeaderStartColumn; i++) {
			columnDataList.add(valueStringList[columnNo]);
			columnNo++;
		}
		
		if(ussageDataHash!=null && ussageDataHash.size()>0){
			for (int i = 0; i < ussageHeaderColums.length; i++) {
				String qValue = null;
				String columnKey = ussageHeaderColums[i].ussageKey;
				UssageDataType ussageDataType = ussageDataHash.get(columnKey);
				if(ussageDataType!=null){
					qValue = ussageDataType.qValue;	
				}
				if(qValue==null || (qValue!=null && qValue.trim().length()<1)){
					qValue = "";
				}
				
	    		columnDataList.add(qValue);
	    		columnNo++;
			}
		}else{
			for (int i = 0; i < ussageHeaderColums.length; i++) {
				String qValue = "";
	    		columnDataList.add(qValue);
	    		columnNo++;
			}
		}
		
		for (int i = this.UssageHeaderStartColumn; i < targetColumnCount; i++) {
			columnDataList.add(valueStringList[i]);
		}
		
		return columnDataList;
	}

	/**
	 * �� �Լ��� ���� ������ ���ؼ��� Teamcenter�� ����� Template�� �̿��� Local�� Download �ϴ� �����
	 * �����ؾ� �Ѵ�.
	 * @return
	 */
	private String getDownloadAndRenamedTemplateFilePath(String fileName){
		
		String templateFilePath = null;
		File templateFile = null;
		try {
			templateFile = getDownloadedTemplateFile(fileName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(templateFile!=null && templateFile.exists() && templateFile.isFile()){
			templateFilePath = templateFile.getPath();	
		}
		
		return templateFilePath;
	}

	/**
	 * Template Dataset�� ã�� Dowlonad �Ѵ�.
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	private File getDownloadedTemplateFile(String fileName) throws Exception{
		
		File loFile =null;
		Vector<TCComponentDataset> datasets = null;
		TCComponentItem documentItem = CustomUtil.findItem("Document", "PRE_DOCTEMP_21");
		if(documentItem!=null){
			// Released �Ȱ͸� ã�ƿ��Ƿ� Template�� Rlease �ؾ��Ѵ�.
			TCComponentItemRevision documentItemRevision = SYMTcUtil.getLatestReleasedRevision(documentItem);
			if(documentItemRevision!=null){
				datasets = CustomUtil.getDatasets(documentItemRevision, "TC_Attaches", TcDefinition.DATASET_TYPE_EXCEL);
			}
		}
		
		for (int i = 0;datasets!=null && datasets!=null && i < datasets.size(); i++) {
			TCComponentDataset tcComponentDataset = datasets.get(i);
			File[] localfile = exportDatasetFileToLocalFolder(tcComponentDataset, this.exportTargetFolderPath);
			for (int j = 0;localfile!=null && j < localfile.length; j++) {
				File tempFile = localfile[j];
				if(tempFile!=null && tempFile.exists()){
					
					// Local�� �̹� ������ ������ �ִ°�� �����Ѵ�.
					File existFile = new File(fileName);
					if(existFile!=null && existFile.exists()){
						existFile.delete();
					}
					
					boolean success = tempFile.renameTo(new File(fileName));
					if(success==true){
						loFile = new File(fileName);
						break;
					}else{
						
					}
				}
			}
			if(loFile!=null && loFile.exists()){
				break;
			}
		}
		
		return loFile;
	}
	
	/**
	 * Dataset�� ���Ե� File�� Local�� Download �Ѵ�.
	 * @param dataset
	 * @param export_dir
	 * @return
	 * @throws Exception
	 */
    private File[] exportDatasetFileToLocalFolder(TCComponentDataset dataset, String export_dir) throws Exception {

        File folder = new File(export_dir);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        
        File[] file = null;
        TCComponentTcFile[] imanFile = dataset.getTcFiles();
        for (int i = 0;imanFile!=null && i < imanFile.length; i++) {
        	file = dataset.getFiles(CustomUtil.getNamedRefType(dataset, imanFile[i]), export_dir);
        	for (int j = 0;file!=null && j < file.length; j++) {
				File tempFile = file[j];
				if(tempFile!=null && tempFile.exists()==true && tempFile.getName().trim().toUpperCase().endsWith("XLS")){
					break;
				}
			}
		}
        
        return file;
    }
}
