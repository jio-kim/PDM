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

import jxl.Workbook;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellRangeAddress;

import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.remote.SYMCRemoteUtil;
import com.ssangyong.common.utils.CustomUtil;
import com.ssangyong.common.utils.PreferenceService;
import com.ssangyong.common.utils.SYMTcUtil;
import com.ssangyong.common.utils.TcDefinition;
import com.symc.plm.rac.prebom.common.util.SDVPreBOMUtilities;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPreferenceService;

/**
 * [SR160621-031][20160707] taeku.jeong
 * 주간 단위로 생성된 Pre-BOM 데이터를 활용하여 엑셀로 출력할 수 있는 기능 개발
 */
public class PreBOMMasterListDataUtil {
	
	private String exportTargetFolderPath = System.getenv("user.home")+"\\Documents";
	
	// Template에 정의된 Data 출력에 사용될 Sheet의 이름.
	private static String SheetName = "MasterList";
	
	// Header 정보를 추력할때 사용할 변수 정의
	private static int UssageHeaderTitleRow = 0;  // Column, Row 의 Index는 0부터 시작
	private static int AraeHeaderRow = 1;  // Column, Row 의 Index는 0부터 시작
	private static int PassengerHeaderRow = 2;  // Column, Row 의 Index는 0부터 시작
	private static int EngineHeaderRow = 3;  // Column, Row 의 Index는 0부터 시작
	private static int GradeHeaderRow = 4;  // Column, Row 의 Index는 0부터 시작
	private static int TrimHeaderRow = 5;  // Column, Row 의 Index는 0부터 시작
	
	// Excel Template에 양식이 그려져 있는 마지막보다 한 Row 위의 Row Index
	private static int TemplateEndRowIndex = 18;
	// Ussage가 표기 시작 되는 Column의 Index
	private static int UssageHeaderStartColumn = 19;  // Column, Row 의 Index는 0부터 시작
	
	// Data를 Queyr하기위해 정위된 SQLMap Service
	private static final String PREBOM_USSAGE_QUERY_SERVICE = "com.ssangyong.service.PreBOMUssageExportService";

	public PreBOMMasterListDataUtil(){
		
	}
	
	/**
	 * 출력대상이될 Project Data 생성정보를 사용자가 선택할 수있도록 Project List를 가져오는 함수
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
        	
        	// 사용자에게 보이는 마지막 Data 생성 시간정보
        	String projectCode = (String)rowHash.get("PROJECT_CODE");
        	String eaiCreateTime = (String)rowHash.get("LATESTEAICREATEDATE");
        	
        	// 아래의 값들은 각각의 Table에서 검색을 할때 사용될 마지막 Data 생성 시간이 된다.
        	String mCreateTime = (String)rowHash.get("MASTER_CREATE_TIME");
        	String oCreateTime = (String)rowHash.get("OSPEC_CREATE_TIME");
        	String uCreateTime = (String)rowHash.get("USSAGE_CREATE_TIME");
        	
        	MasterAndUssageFindKey tmpMasterAndUssageFindKey = new MasterAndUssageFindKey(projectCode, eaiCreateTime, mCreateTime,  oCreateTime, uCreateTime);
        	masterAndUssageFindKeyHash.put(projectCode, tmpMasterAndUssageFindKey);

		}
        
        return masterAndUssageFindKeyHash;
	}
	
	/**
	 * Project의  Ussage Header 정보를 읽어 온다.
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
	 * Project Data 출력을 위한 Excle File 의 Format중 Ussage 부분의 Column들을 추가하고 Header 를 준비 한다.
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
		// Excel File의 Column을 준비 하는 부분을 구현 한다.
		//----------------------------------------------------------------
		
		// 001. excel file을 준비한다.
		createNewXLSFile(newTemplateFilePath, newFilePath);
		
		// 002. 필요한 Column을 추가한다.
		int insertCount = ussageHeaderColums.length - 1;
		if(insertCount>0){
			insertUssageColumns(newFilePath, 
					(PreBOMMasterListDataUtil.UssageHeaderStartColumn), 
					insertCount);
		}
		
		// 003. Column의 이름을 기록하고 Cell Style을 정의한다.
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
	 * Mast Data를 읽어 주어진 File에 출력하는 부분
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
	    
	  //[CSH][20180514] COST 정보는 권한 있는 그룹 or 사용자만 보이도록 현업 요청
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

	    	// Master Data Row에 해당하는 Ussage Data를 포함해 Header의 순서에 맞춰 Data를 List에 담는다. 
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
	
	    	// Excel 파일에 Row를 생성한다.
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
	    	
	    	// 이미 Cell Style이 정의되어 있는 것을 활용하기위해 이전 Row를 찾고 Row Style을 붙여 넣는다.
	    	HSSFRow beforRow = null;
	    	if(isExistRow==false){
	    		beforRow = sheet.getRow((writeRowIndex-1));
	    	}
	    	
	    	if(beforRow!=null){
	    		if(beforRow.getRowStyle()!=null){
	    			currentRow.setRowStyle(beforRow.getRowStyle());
	    		}
	    	}
	    	
	    	// 생성된 Row에 Cell 단위로 Column의 값을 기록하고 Cell Style을 적용한다.
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
	    		
	    		// 새로 생성된 Row의 경우 이전 Row의 동일한 Column의 Cell Style을 복제한다.
	            if(isExistRow==false){
	            	currentCell.setCellStyle( beforRow.getCell(columnIndex).getCellStyle() );
	            }
			}
	
		}
	    
	    // Excel 파일을 저장한다.
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
	 * Master Data의 Ussage 정보를 읽어 Hash Table에 담아 Return 하는 함수
	 * Master Data의 매 Row마다 호출됨
	 * 이부분을 나중에 한번에 Query하게 변경하면 좀 나은 P/G이 될것 같음.
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
	 * Template을 이용해 실제 Data를 저장할 File을 만드는 함수
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
		    // Excel File 저장.
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
	 * JXL을 이용해 Column을 추가하는 기능을 수행한다.
	 * Ussage의 경우 Column이 가변적으로 추가되는데 이것을 위해 Column을 추가하는기능이 필요했음.
	 * PIO에는 Column을 Shift 하고 Add하는 기능이 없으므로 Cell Merge를 위해 복잡한 과정을 거쳐야 함
	 * POI를 이용해서 진행 하려고 했으나 복잡해지는 경향이 있어서 JXL API를 이용했음.
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
		    // Excel File 저장.
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
		//[SR180903-026]vehPart mapping 정보 보여주기 위해 ColumnCount 증가.
		//20201021 seho EJS Column 추가로 총 column 개수 1 증가함.
		//[CF-1706] WEIGHT MANAGEMENT 칼럼 추가로 총 column 개수 1 증가함(54->55). by 전성용(20201223)		
		int targetColumnCount = 55;//컬럼 추가/삭제 시 변경 필요.
		String[] valueStringList = new String[targetColumnCount];
		for (int i = 0; i < targetColumnCount; i++) {
			String columnName = "MAST_COL"+df.format((long)(i+1));
			String valueString = (String)rowHash.get(columnName);
			if(valueString==null || (valueString!=null && valueString.trim().length()<1)){
				valueString = "";
			}else{
				valueString = valueString.trim();
			}
			//[CSH][20180514] Cost 정보는 권한 있는 그룹 or 사용자만 보이도록 현업 요청
			//[CF-1706] WEIGHT MANAGEMENT 칼럼 추가로 MAST_COL번호 변경(PreBOMUssageExportMapper 참조) by 전성용(20201223)	
			if(!isCostViewable && (columnName.equals("MAST_COL30") || columnName.equals("MAST_COL31") || columnName.equals("MAST_COL48")|| columnName.equals("MAST_COL49")|| columnName.equals("MAST_COL50")|| columnName.equals("MAST_COL51")|| columnName.equals("MAST_COL52"))){
				valueString = "";
			}
			
			valueStringList[i] = valueString;
		}
		
		// Ussage Data를 출력한다.
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
	 * 이 함수는 실제 구동을 위해서는 Teamcenter에 저장된 Template를 이용해 Local에 Download 하는 기능을
	 * 수행해야 한다.
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
	 * Template Dataset을 찾고 Dowlonad 한다.
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	private File getDownloadedTemplateFile(String fileName) throws Exception{
		
		File loFile =null;
		Vector<TCComponentDataset> datasets = null;
		TCComponentItem documentItem = CustomUtil.findItem("Document", "PRE_DOCTEMP_21");
		if(documentItem!=null){
			// Released 된것만 찾아오므로 Template을 Rlease 해야한다.
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
					
					// Local에 이미 동일한 파일이 있는경우 삭제한다.
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
	 * Dataset에 포함된 File을 Local로 Download 한다.
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
