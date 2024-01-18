package com.ssangyong.soa.bop.reports;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ssangyong.soa.bop.util.BasicSoaUtil;
import com.ssangyong.soa.bop.util.LogFileUtility;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.strong.BOMLine;
import com.teamcenter.soa.client.model.strong.CfgActivityLine;
import com.teamcenter.soa.client.model.strong.ItemRevision;
import com.teamcenter.soa.client.model.strong.MEActivity;
import com.teamcenter.soa.client.model.strong.Mfg0BvrOperation;
import com.teamcenter.soa.client.model.strong.WorkspaceObject;


public class ExcelWorkBookWriter {

	String srcExcelFilePath;
	String exportTargetDir;
	
	int templateDataStartRowIndex = 0;
	int templateDataStartColumnIndex = 0;
	
	int templateReadyLastRowIndex = 10;
	int maxColIndex = 0;
	
	XSSFWorkbook workbook;
	
	int[] wrapTextColumns = null;
	
	public ExcelWorkBookWriter(String srcExcelFilePath){
		this.srcExcelFilePath = srcExcelFilePath;
		System.out.println("srcExcelFilePath = "+srcExcelFilePath);
	}
	
	public void readyFile(int templateDataStartRowIndex,
			int templateDataStartColumnIndex,
			int templateReadyLastRowIndex)  throws Exception {
		
		this.templateDataStartRowIndex = templateDataStartRowIndex;
		this.templateDataStartColumnIndex = templateDataStartColumnIndex;
		this.templateReadyLastRowIndex = templateReadyLastRowIndex;
		
		if(this.srcExcelFilePath==null || (this.srcExcelFilePath!=null && this.srcExcelFilePath.trim().length()<4)){
			throw new Exception("Must specify where the document is to be created.");
		}
		
		openWorkBook();
		
	}
	
	private XSSFSheet getWorkSheet(String workSheetName){
		
		XSSFSheet workSheet = null;
		
		if(workbook==null){
			return workSheet;
		}
		
		workSheet = workbook.getSheet(workSheetName);
		
		return workSheet;
	}
	
	private void openWorkBook() throws Exception {
		
		Exception err1 = null;
		try {
			// File 객체 가져오기.
			workbook = new XSSFWorkbook(new FileInputStream(this.srcExcelFilePath));
		} catch (Exception err) {
			workbook = null;
			throw err;
		}
		
	}
	
	public void saveWorkBook() throws Exception {

		if(workbook==null){
			return;
		}
		
		try {
		    // Excel File 저장.
		    FileOutputStream fileOut = new FileOutputStream(this.srcExcelFilePath);
		    workbook.write(fileOut);
		    fileOut.close();
		    
		    System.out.println("Close Excel File : "+this.srcExcelFilePath);
		    
		} catch (Exception err) {
			throw err;
		}
		
		openWorkBook();
	}
	
	public void closeWorkBook() throws Exception {

		if(workbook==null){
			return;
		}
		
		try {
		    // Excel File 저장.
		    FileOutputStream fileOut = new FileOutputStream(this.srcExcelFilePath);
		    workbook.write(fileOut);
		    fileOut.close();
		    
		    System.out.println("Close Excel File : "+this.srcExcelFilePath);
		    
		} catch (Exception err) {
			throw err;
		} finally {
			if (workbook != null) {
				workbook = null;
			}
		}
		
	}
	
	public void writeRow(String workSheetName,
			int dataRowNo,
			Object[] rowData) {
		
		if(workbook==null){
			return;
		}

		int rowIndex = templateDataStartRowIndex + dataRowNo;
		XSSFSheet workSheet = getWorkSheet(workSheetName);
		
		if(workSheet==null){
			return;
		}
		
		XSSFRow currentRow = workSheet.getRow(rowIndex);
		if(currentRow==null){
			currentRow = workSheet.createRow(rowIndex);
		}
		
		if(rowData==null || (rowData!=null && rowData.length<1)){
			return;
		}
		
		for (int i = 0; rowData!=null && i < rowData.length; i++) {

			String strValue = null;
			if(rowData[i]!=null){
				strValue = (String)rowData[i];
			}else{
				strValue = "";
			}
			
			// Cell에 데이타 입력.
			XSSFCell cell = null;
			cell = currentRow.getCell(i);
			if(cell==null){
				cell = currentRow.createCell(i);
			}

			cell.setCellValue(new XSSFRichTextString(strValue));
			// BOP Report 생성 과정에서 스타일이 깨지는 현상 발생하여 수정
//			if(this.templateReadyLastRowIndex<rowIndex){
//				XSSFCellStyle templateCellStyle = getCellStyle(workSheet, i);
//				XSSFCellStyle cellStyle2 = (XSSFCellStyle)templateCellStyle.clone();
//				cell.setCellStyle(cellStyle2);
//			}

			if(i>maxColIndex){
				maxColIndex = i;
			}
		}
		
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////////
	/*
	 수정점 : 20200330
	 * 수정내용 : MPP -> Assembly BOP -> Reports ->  Process Master List(On Server)
	 * 			  "작업정보"를 추가 하기위해 writeRow 메서드 추가 및 setCellStyleOfHeaderInfo 메서드 추가 
	 */
	
	public void writeRow(String workSheetName,
			int dataRowNo,
			Object[] rowData, String selectedValue, BOMLine operationBOMLine, BasicSoaUtil basicSoaUtil, LogFileUtility logFileUtility ) {
		
		if(workbook==null){
			return;
		}

		int rowIndex = templateDataStartRowIndex + dataRowNo;
		XSSFSheet workSheet = getWorkSheet(workSheetName);
		
		if(workSheet==null){
			return;
		}
		
		XSSFRow currentRow = workSheet.getRow(rowIndex);
		if(currentRow==null){
			currentRow = workSheet.createRow(rowIndex);
		}
		
		if(rowData==null || (rowData!=null && rowData.length<1)){
			return;
		}
		
		for (int i = 0; rowData!=null && i < rowData.length; i++) {

			String strValue = null;
			if(rowData[i]!=null){
				strValue = (String)rowData[i];
			}else{
				strValue = "";
			}
			
			// Cell에 데이타 입력.
			XSSFCell cell = null;
			cell = currentRow.getCell(i);
			if(cell==null){
				cell = currentRow.createCell(i);
			}

			cell.setCellValue(new XSSFRichTextString(strValue));
			// BOP Report 생성 과정에서 스타일이 깨지는 현상 발생하여 수정
//			if(this.templateReadyLastRowIndex<rowIndex){
//				XSSFCellStyle templateCellStyle = getCellStyle(workSheet, i);
//				XSSFCellStyle cellStyle2 = (XSSFCellStyle)templateCellStyle.clone();
//				cell.setCellStyle(cellStyle2);
//			}

			if(i>maxColIndex){
				maxColIndex = i;
			}
		}
		
		
////////////////////////////////////////////////////////////////////////////////////
	/*
	* 수정점 : 
	*/
		if ( selectedValue.equals("1") ) {
		
				List<HashMap<String, Object>> workInfoList = new ArrayList<HashMap<String, Object>>();
				
				Mfg0BvrOperation mfg0BvrOperation = null;
				if(operationBOMLine!=null && operationBOMLine instanceof Mfg0BvrOperation){
					mfg0BvrOperation = (Mfg0BvrOperation)operationBOMLine;	
				}
				
				// Root Activity 를 찾아온다.
				ModelObject activityLineModelObject = null;
				try {
					mfg0BvrOperation = (Mfg0BvrOperation)basicSoaUtil.readProperties(
							mfg0BvrOperation, 
							new String[]{"bl_me_activity_lines"});
					activityLineModelObject = mfg0BvrOperation.get_bl_me_activity_lines();
				} catch (Exception e) {
					logFileUtility.writeExceptionTrace(e);
				}
				
				MEActivity rootMEActivity = null;
				if(activityLineModelObject!=null && activityLineModelObject instanceof CfgActivityLine){
					CfgActivityLine root = (CfgActivityLine)activityLineModelObject;
					try {
						root = (CfgActivityLine)basicSoaUtil.readProperties( root, new String[]{ "al_object" });
						//"me_cl_source", "me_cl_wso" Properties도 동일한 MEActivity Object를 Return 한다.
						rootMEActivity = (MEActivity)root.get_al_object();
					} catch (Exception e) {
						logFileUtility.writeExceptionTrace(e);
					}
				}
				
				if(rootMEActivity!=null){
					WorkspaceObject[] contentsObjects = null;
					try {
						rootMEActivity = (MEActivity)basicSoaUtil.readProperties(rootMEActivity, new String[]{ "contents" });
						contentsObjects = rootMEActivity.get_contents();
					} catch (Exception e) {
						logFileUtility.writeExceptionTrace(e);
					}
					for (int i = 0; contentsObjects!=null && i < contentsObjects.length; i++) {
						HashMap<String, Object> workInfoMap = new HashMap<String, Object>();
						if(contentsObjects[i]!=null && contentsObjects[i] instanceof MEActivity){
							MEActivity tempActivity = (MEActivity)contentsObjects[i];
							// Collections Sort에 필요한 Attribute를 미리 Load 한다.
							try {
								tempActivity = (MEActivity)basicSoaUtil.readProperties( tempActivity, new String[]{ "time_system_code", "object_name", "time_system_unit_time" });
//								
								workInfoMap.put("workCode", tempActivity.getPropertyDisplayableValue("time_system_code"));
								workInfoMap.put("workInfo", tempActivity.getPropertyDisplayableValue("object_name"));
								workInfoMap.put("time", tempActivity.getPropertyDisplayableValue("time_system_unit_time"));
								
							} catch (Exception e) {
								logFileUtility.writeExceptionTrace(e);
							}
						}
						workInfoList.add(workInfoMap);
					}
				
				String workCode = "";
				String workInfo = "";
				String time = "";
				
				for (int i = 0; i < workInfoList.size(); i++) {
				if (i != 0) {
					workCode += "\n";
					time += "\n";
					workInfo += "\n";
				}
				
				workCode += (String)workInfoList.get(i).get("workCode");
				time 	 += (String)workInfoList.get(i).get("time");
				workInfo += (String)workInfoList.get(i).get("workInfo");
				}
				
				int cellNum = currentRow.getLastCellNum();
				// 작업 코드
				currentRow.createCell(cellNum).setCellValue(workCode);
				Cell cell = currentRow.getCell(cellNum);
				CellStyle cellStyle = cell.getCellStyle();
				cellStyle.setWrapText(true);
				cell.setCellStyle(cellStyle);
				
				// 작업 시간
				currentRow.createCell(cellNum + 1).setCellValue(time);
				Cell cell1 = currentRow.getCell(cellNum + 1);
				CellStyle cellStyle1 = cell1.getCellStyle();
				cellStyle.setWrapText(true);
				cell.setCellStyle(cellStyle1);
				
				// 작업 이름
				currentRow.createCell(cellNum + 2).setCellValue(workInfo);
				Cell cell2 = currentRow.getCell(cellNum + 2);
				CellStyle cellStyle2 = cell2.getCellStyle();
				cellStyle.setWrapText(true);
				cell.setCellStyle(cellStyle2);
			}
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////////
	
	private XSSFCellStyle getCellStyle(XSSFSheet workSheet, int columnIndex){
		
		XSSFRow currentRow = workSheet.getRow(templateReadyLastRowIndex);
		
		XSSFCell currentCell = currentRow.getCell(columnIndex);
		XSSFCellStyle cellStyle = currentCell.getCellStyle();
		
		boolean isWrap = isWrapStyleColumn(columnIndex);
		if(isWrap==true){
			cellStyle.setWrapText(true);
		}

		return cellStyle;
	}
	
	public void writeRow(String workSheetName,
			int rowIndex, int columnIndex,
			String rowData) {
		
		if(workbook==null){
			return;
		}
		
		XSSFSheet workSheet = getWorkSheet(workSheetName);
		
		if(workSheet==null){
			return;
		}
		
		XSSFRow currentRow = workSheet.getRow(rowIndex);
		if(currentRow==null){
			currentRow = workSheet.createRow(rowIndex);
		}
		// Cell에 데이타 입력.
		XSSFCell cell = currentRow.getCell(columnIndex);
		if(cell==null){
			cell = currentRow.createCell(columnIndex);
		}
		cell.setCellValue(new XSSFRichTextString(rowData));
		
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	/*
	 * 수정점 : 20200330
	 * 수정내용 : MPP -> Assembly BOP -> Reports ->  Process Master List(On Server)
	 * 			  "작업정보"를 추가 하기위해 writeRow 메서드 추가 및 setCellStyleOfHeaderInfo 메서드 추가 
	 * 
	 */
	public int writeRow( String workSheetName ) {
		
		if(workbook==null){
			return 0;
		}
		
		XSSFSheet workSheet = getWorkSheet(workSheetName);
		
		if(workSheet==null){
			return 0;
		}
		
		Row row = workSheet.getRow(4);
        int startIndex =  row.getLastCellNum();
        int lastIndex = startIndex + 2;
        
        int id_width = workSheet.getColumnWidth(5);
        int name_width = workSheet.getColumnWidth(3);
        int job_time = workSheet.getColumnWidth(25);

        // Column Name 설정
        String subject = "작업 정보";
        String[] columnName = {  "작업 코드", "작업시간", "작업 내용" };

        // Column Width 설정
        int[] columnWidthArray = new int[] { id_width, job_time, name_width };
        
        for (int i = startIndex; i <= lastIndex; i++) {
        	workSheet.autoSizeColumn(i);
        	workSheet.setColumnWidth(i, columnWidthArray[i - startIndex]);
        }
        
        

        setCellStyleOfHeaderInfo(workSheet, startIndex, lastIndex, subject, columnName);
        workSheet.addMergedRegion(new CellRangeAddress(4, 4, startIndex, lastIndex));
		
        return lastIndex;
	}
	
	private void setCellStyleOfHeaderInfo(Sheet currentSheet, int startIndex, int lastIndex, String subject, String[] columnName) {
        XSSFCellStyle cellStyle;
        Row row;
        Cell cell;

        // Excel 1, 2, 3행 파란색 배경 채우기
        for (int i = 0; i < 3; i++) {
            row = currentSheet.getRow(i);
            cellStyle = (XSSFCellStyle) row.getCell(0).getCellStyle();
            for (int j = startIndex; j <= lastIndex; j++) {
                cell = row.createCell(j);
                cell.setCellStyle(cellStyle);
            }
        }

        // Excel 5행 column 제목 넣기
        row = currentSheet.getRow(4);
        cell = row.getCell(0);
        cellStyle = (XSSFCellStyle) cell.getCellStyle();

        for (int i = startIndex; i <= lastIndex; i++) {
            cell = row.createCell(i);
            cell.setCellStyle(cellStyle);
        }
        cell = row.getCell(startIndex);
        cell.setCellValue(subject);

        // Excel 6행 column명 넣기
        row = currentSheet.getRow(5);
        cell = row.getCell(0);
        cellStyle = (XSSFCellStyle) cell.getCellStyle();

        int index = 0;
        for (int j = startIndex; j <= lastIndex; j++) {
            cell = row.createCell(j);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(columnName[index]);
            index++;
        }
    }
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	public void dataCellMerge(String workSheetName, int startDataIndex, int startDataColumnIndex,
			int endDataIndex, int endDataColumnIndex, String itemSubTotalValueStr) throws Exception {
		
		if(workbook==null){
			return;
		}
		
		XSSFSheet workSheet = getWorkSheet(workSheetName);
		
		if(workSheet==null){
			return;
		}
		
		int firstRow = startDataIndex;
		int lastRow = endDataIndex;
		int firstCol = startDataColumnIndex;
		int lastCol = endDataColumnIndex;
		
		XSSFCell  cell = workSheet.getRow(firstRow).getCell(firstCol);
		if(cell==null){
			cell = workSheet.getRow(firstRow).createCell(firstCol);
		}
		
		System.out.println("itemSubTotalValueStr = "+itemSubTotalValueStr);
		
		if(cell != null && itemSubTotalValueStr!=null){
			cell.setCellValue(itemSubTotalValueStr);
		}else{
			cell.setCellValue(new String(""));
		}
		workSheet.addMergedRegion(new CellRangeAddress(firstRow, lastRow, firstCol, lastCol));
	}
	
	
	public int[] getWrapTextColumns() {
		return wrapTextColumns;
	}

	public void setWrapTextColumns(int[] wrapTextColumns) {
		this.wrapTextColumns = wrapTextColumns;
	}
	
	private boolean isWrapStyleColumn(int columnIndex){
		boolean isWrap = false;
		
		for (int i = 0; wrapTextColumns!=null && i < wrapTextColumns.length; i++) {
			int tmpColumnIndex = wrapTextColumns[i];
			if(tmpColumnIndex==columnIndex){
				isWrap = true;
				break;
			}
		}
		
		return isWrap;
	}

	
	public void updateSortedDataIndex(String workSheetName, int targetColumnIndex, int sortAreasStartRowIndex){
		// Row에 Data 써 넣을때 보다 시작점의 Row Index의 위치가 하나 앞당겨지는 현상이 있음.
		sortAreasStartRowIndex = sortAreasStartRowIndex+1;
		
		int dataIndex = 1;
		XSSFSheet sheet = getWorkSheet(workSheetName);
		for (int i = sortAreasStartRowIndex; i <= sheet.getLastRowNum(); i++) {
			int rowindex = i;
			XSSFRow tempRow = sheet.getRow(rowindex);
			if(tempRow!=null){
				XSSFCell currentRowCell = tempRow.getCell(targetColumnIndex);
				currentRowCell.setCellValue(""+dataIndex);
			}
			dataIndex++;
		}
	}
	
	public void rowSortingForMakeSubTotalData(String workSheetName, int keyColumnIndex, int sortAreasStartRowIndex, boolean isAscendingOrder) {
		
		// Row에 Data 써 넣을때 보다 시작점의 Row Index의 위치가 하나 앞당겨지는 현상이 있음.
		sortAreasStartRowIndex = sortAreasStartRowIndex+1;
		
		XSSFSheet sheet = getWorkSheet(workSheetName);
		
		int lastRowIndex = sheet.getLastRowNum();
		int size = lastRowIndex-sortAreasStartRowIndex + 1;
		
		for(int i=size-1; i>0; i--) {
			
			System.out.printf("\n버블 정렬 %d 단계 : ", size-i);
			
			for(int j=0; j<i; j++) {
				
				int currentRowindex = sortAreasStartRowIndex+j;
				int nextRowIndex = sortAreasStartRowIndex + j+1;

				boolean isSwapTarget = isSwapTarget(sheet, keyColumnIndex, currentRowindex, nextRowIndex, isAscendingOrder);
				if(isSwapTarget==true) {
					 swap(sheet, currentRowindex, nextRowIndex);
				}

			}      
			
		}
		
		System.out.println();
	}
	
	private  void swap(XSSFSheet sheet, int srcRowindex, int targetRowIndex) {
		
		int columnCount = maxColIndex+1;
		
		Object[] tempValueObjects = new Object[columnCount];
		int[] tempCellType = new int[columnCount];
		
		XSSFRow srcRow = sheet.getRow(srcRowindex);
		if(srcRow==null){
			srcRow = sheet.createRow(srcRowindex);
		}

		for (int i = 0; i < columnCount; i++) {
			XSSFCell srcRowCell = srcRow.getCell(i);
			if(srcRowCell!=null){
				int cellType = srcRowCell.getCellType();
				tempCellType[i] = cellType;
				if(cellType == XSSFCell.CELL_TYPE_BOOLEAN){
					tempValueObjects[i] = new Boolean(srcRowCell.getBooleanCellValue());
				}else if(cellType == XSSFCell.CELL_TYPE_NUMERIC){
					tempValueObjects[i] = new Double(srcRowCell.getNumericCellValue());
				}else if(cellType == XSSFCell.CELL_TYPE_STRING){
					tempValueObjects[i] = new String(srcRowCell.getStringCellValue());
				}else if(cellType == XSSFCell.CELL_TYPE_FORMULA){
					tempValueObjects[i] = new String(srcRowCell.getCellFormula());
				}else{
					tempValueObjects[i] = new String("");
				}
			}else{
				srcRowCell = srcRow.createCell(i);
				tempValueObjects[i] = null;
			}
		}
		
		XSSFRow targetRow = sheet.getRow(targetRowIndex);
		if(targetRow==null){
			targetRow = sheet.createRow(targetRowIndex);
		}

		for (int i = 0; i < columnCount; i++) {
			
			XSSFCell srcRowCell = srcRow.getCell(i);
			XSSFCell targetRowCell = targetRow.getCell(i);
			
			boolean isMadeCell = false;
			if(targetRowCell==null){
				targetRowCell = targetRow.createCell(i);
				isMadeCell = true;
			}
			
			Object currentTargetCellValueObject = null;
			if(isMadeCell==false){
				int cellType = targetRowCell.getCellType();
				
				if(cellType == XSSFCell.CELL_TYPE_BOOLEAN){
					srcRowCell.setCellValue(targetRowCell.getBooleanCellValue());
					if(tempValueObjects[i]!=null){
						targetRowCell.setCellValue(((Boolean)tempValueObjects[i]).booleanValue());
					}else{
						targetRowCell.setCellValue((String)"");
					}
				}else if(cellType == XSSFCell.CELL_TYPE_NUMERIC){
					srcRowCell.setCellValue(targetRowCell.getNumericCellValue());
					if(tempValueObjects[i]!=null){
						targetRowCell.setCellValue(((Double)tempValueObjects[i]).doubleValue());
					}else{
						targetRowCell.setCellValue((String)"");
					}
				}else if(cellType == XSSFCell.CELL_TYPE_STRING){
					srcRowCell.setCellValue(targetRowCell.getStringCellValue());
					if(tempValueObjects[i]!=null){
						targetRowCell.setCellValue((String)tempValueObjects[i]);
					}else{
						targetRowCell.setCellValue((String)"");
					}
				}else if(cellType == XSSFCell.CELL_TYPE_FORMULA){
					srcRowCell.setCellValue(targetRowCell.getCellFormula());
					if(tempValueObjects[i]!=null){
						targetRowCell.setCellValue((String)tempValueObjects[i]);
					}else{
						targetRowCell.setCellValue((String)"");
					}
				}else{
					srcRowCell.setCellValue(new String(""));
					if(tempValueObjects[i]!=null){
						targetRowCell.setCellValue((String)tempValueObjects[i]);
					}else{
						targetRowCell.setCellValue((String)"");
					}
				}
			}else{
				int cellType = targetRowCell.getCellType();
				
				if(cellType == XSSFCell.CELL_TYPE_BOOLEAN){
					if(tempValueObjects[i]!=null){
						targetRowCell.setCellValue(((Boolean)tempValueObjects[i]).booleanValue());
					}else{
						targetRowCell.setCellValue((String)"");
					}
				}else if(cellType == XSSFCell.CELL_TYPE_NUMERIC){
					if(tempValueObjects[i]!=null){
						targetRowCell.setCellValue(((Double)tempValueObjects[i]).doubleValue());
					}else{
						targetRowCell.setCellValue((String)"");
					}
				}else if(cellType == XSSFCell.CELL_TYPE_STRING){
					if(tempValueObjects[i]!=null){
						targetRowCell.setCellValue((String)tempValueObjects[i]);
					}else{
						targetRowCell.setCellValue((String)"");
					}
				}else if(cellType == XSSFCell.CELL_TYPE_FORMULA){
					if(tempValueObjects[i]!=null){
						targetRowCell.setCellValue((String)tempValueObjects[i]);
					}else{
						targetRowCell.setCellValue((String)"");
					}
				}else{
					if(tempValueObjects[i]!=null){
						targetRowCell.setCellValue((String)tempValueObjects[i]);
					}else{
						targetRowCell.setCellValue((String)"");
					}
				}
			}
		}
		
	}

	private  boolean isSwapTarget(XSSFSheet sheet, int keyColumnIndex, int currentRowindex, int nextRowIndex, boolean isAscendingOrder) {

		XSSFRow currentRow = sheet.getRow(currentRowindex);

		String currentCellValue = null;
		if(currentRow!=null){
			XSSFCell currentRowCell = currentRow.getCell(keyColumnIndex);
			if(currentRowCell!=null){
				currentCellValue = currentRowCell.getStringCellValue();
			}
		}

		String nextCellValue = null;
		XSSFRow nextRow = sheet.getRow(nextRowIndex);
		if(nextRow!=null){
			XSSFCell nextRowCell = nextRow.getCell(keyColumnIndex);
			if(nextRowCell!=null){
				nextCellValue = nextRowCell.getStringCellValue();
			}
		}

		int compareResult = 0; 
		if(currentCellValue!=null && nextCellValue!=null){
			compareResult = currentCellValue.compareToIgnoreCase(nextCellValue);
		}else if(currentCellValue==null && nextCellValue!=null){
			compareResult = 1; 
		}else if(currentCellValue!=null && nextCellValue==null){
			compareResult = -1;
		}else{
			compareResult = 0; 
		}
		
		boolean isSwaptarget = false;
		
		if(isAscendingOrder){
			if(compareResult>0){
				 isSwaptarget = true;
			}			
		}else{
			if(compareResult<0){
				 isSwaptarget = true;
			}
		}
		
		return isSwaptarget;
	}
	
	public void subsidiaryReportSubTotalExpressionUpdate(
			String workSheetName, int partIdColumnIndex, int quantityColumnIndex, int firstDataRowIndex) throws Exception {
		
		
		String subTotalTargetItemId = null;
		int subTotalStartRowIndex = 0;
		int subTotalEndRowIndex = 0;
		int subTotalIndexCount = 0;
		double subTotalValue = 0.0;
		
		
		XSSFSheet sheet = getWorkSheet(workSheetName);
		int lastRowIndex = sheet.getLastRowNum();
		
		DecimalFormat decimalFormat = new DecimalFormat("0.##");
		
		int currentRowindex = 0;
		for (int i = firstDataRowIndex; i <= lastRowIndex; i++) {
			
			currentRowindex = i;
			
			XSSFRow currentRow = sheet.getRow(currentRowindex);
			
			String partIdValueString = null;
			String partQunatityValueString = null;
			
			if(currentRow!=null){
				XSSFCell partIdColumnCell = currentRow.getCell(partIdColumnIndex);
				if(partIdColumnCell != null){
					partIdValueString = partIdColumnCell.getStringCellValue();
				}
				
				XSSFCell quantityColumnCell = currentRow.getCell(quantityColumnIndex);
				if(quantityColumnCell != null){
					partQunatityValueString = quantityColumnCell.getStringCellValue();
				}
			}
			
			double partQunatityValue = getDoubleFromString(partQunatityValueString);

			// SubTotalData를 생성한다.
			if(subTotalTargetItemId==null || (subTotalTargetItemId!=null && subTotalTargetItemId.trim().equalsIgnoreCase(partIdValueString.trim())==false) ){
				// SubTotal 대상이 변경되었음.
				
				if(subTotalTargetItemId!=null && subTotalTargetItemId.trim().length()>0){
					// Excel Column을 합치고 값을 기록 한다.
					int rowMergeStartIndex = subTotalStartRowIndex;
					int rowMergeEndIndex = subTotalEndRowIndex;
					int rowMergeCount = subTotalIndexCount;
					double itemSubTotalValue = subTotalValue;
					writeSubTotalValue(workSheetName, rowMergeStartIndex, rowMergeEndIndex, itemSubTotalValue);
				}
				
				// 값을 초기화한다.
				subTotalTargetItemId = partIdValueString;
				subTotalStartRowIndex = currentRowindex;
				subTotalEndRowIndex = currentRowindex;
				subTotalIndexCount = 1;
				subTotalValue = partQunatityValue;
			}else{
				// SubTotal 대상이 계속됨
				subTotalEndRowIndex = currentRowindex;
				subTotalIndexCount++;
				subTotalValue = subTotalValue + partQunatityValue;
			}
		}
		
		if(currentRowindex>0){
			if(subTotalTargetItemId!=null && subTotalTargetItemId.trim().length()>0){
				// Excel Column을 합치고 값을 기록 한다.
				int rowMergeStartIndex = subTotalStartRowIndex;
				int rowMergeEndIndex = subTotalEndRowIndex;
				int rowMergeCount = subTotalIndexCount;
				double itemSubTotalValue = subTotalValue;
				writeSubTotalValue(workSheetName, rowMergeStartIndex, rowMergeEndIndex, itemSubTotalValue);
			}
		}
		
	}
	
	private void writeSubTotalValue(String workSheetName, int rowSpanStartIndex, int rowSpanEndIndex, double itemSubTotalValue) throws Exception {
		
		DecimalFormat fmt = new DecimalFormat("0.###");
		String itemSubTotalValueStr = fmt.format(itemSubTotalValue);
		int startColumnIndex = 7;
		int endColumnIndex = 7;
		dataCellMerge(workSheetName, rowSpanStartIndex, startColumnIndex, rowSpanEndIndex, endColumnIndex, itemSubTotalValueStr);
	}
	
	
    private double getDoubleFromString(String inputString)
    {
        double doubleValue = 0;
        StringBuilder sb = new StringBuilder();
        String regEx = "(?!=\\d\\.\\d\\.)([\\d.]+)";
        Pattern pattern = Pattern.compile(regEx);

        Matcher match = pattern.matcher(inputString);
        while (match.find()) {
            sb.append(match.group());
        }        
        try {
            doubleValue = Double.parseDouble(sb.toString());
        } catch (NumberFormatException ex) {
            return 0.0; 
        }
        return doubleValue; 
    }
}
