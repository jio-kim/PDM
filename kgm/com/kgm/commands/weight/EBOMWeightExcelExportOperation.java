package com.kgm.commands.weight;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.kgm.commands.ospec.op.OSpec;
import com.kgm.common.WaitProgressBar;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aifrcp.AIFUtility;

/**
 * [SR180528-064] E-BOM Weight Report 개발 요청
 * @Copyright : Plmsoft
 * @author   : 조석훈
 * @since    : 2018. 6. 04.
 * Package ID : com.kgm.commands.weight.EBOMWeightExcelExportOperation.java
 */
public class EBOMWeightExcelExportOperation extends AbstractAIFOperation
{
    private File exportToFile;
    private Vector<Object> columnHeaders;
    private Vector<Vector<Object>> dataVector;
    private String title;
    private ArrayList<StoredOptionSet> usageOptionSetList;
    
    private int rowIndex = 0;
    private int maxCols = 0;
    private int offsetCol = 0;
    private XSSFSheet sheet;
    private WaitProgressBar waitBar;
    private XSSFWorkbook workbook;
    private CellStyle styleTitle;
    private CellStyle styleHead;
    private CellStyle styleHead1;
    private CellStyle styleNomal;
    private CellStyle styleNomal1;
    private CellStyle styleNomal2;
    private OSpec ospec;


    public EBOMWeightExcelExportOperation(  File selectedFile, Vector<Object> headerVector, Vector<Vector<Object>> dataVector, String title, ArrayList<StoredOptionSet> usageOptionSetList, OSpec ospec){
        this.exportToFile = selectedFile;
        this.columnHeaders = headerVector;
        this.dataVector = dataVector;
        this.title = title;
        this.usageOptionSetList = usageOptionSetList;
        this.ospec = ospec;
    }

    @Override
    public void executeOperation() throws Exception {
    	waitBar = new WaitProgressBar(AIFUtility.getActiveDesktop().getFrame());
    	waitBar.setWindowSize(400, 500);
		waitBar.start();
		waitBar.setStatus("Start... Excel Export");
		
    	workbook = new XSSFWorkbook();
    	sheet = workbook.createSheet("New Sheet");
    	sheet.setDisplayGridlines(false);
    	
    	printSheet(sheet);
    	FileOutputStream fos = new FileOutputStream(exportToFile);
    	workbook.write(fos);
    	fos.flush();
        fos.close();
        
        waitBar.setStatus("End... Excel Export");
        waitBar.close();
        
        openFile();
        
        
    }
    
    private void setStyle(){
    	//1.컬럼타이틀(font14, GREY_25_PERCENT) 셀 스타일 및 폰트 설정
    	styleTitle = workbook.createCellStyle();
    	//정렬
    	styleTitle.setAlignment(CellStyle.ALIGN_CENTER); //가운데 정렬
    	styleTitle.setVerticalAlignment(CellStyle.VERTICAL_CENTER); //높이 가운데 정렬
    	//배경색
    	styleTitle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
    	styleTitle.setFillPattern(CellStyle.SOLID_FOREGROUND);
    	//테두리 선 (우,좌,위,아래)
//    	styleTitle.setBorderRight(HSSFCellStyle.BORDER_THIN);
//    	styleTitle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
//    	styleTitle.setBorderTop(HSSFCellStyle.BORDER_THIN);
//    	styleTitle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
    	Font titleFont = this.sheet.getWorkbook().createFont();
    	titleFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
    	titleFont.setFontHeightInPoints((short)14);
    	styleTitle.setFont(titleFont);
    	styleTitle.setWrapText(true);
        
    	//1.컬럼헤드(font11, GREY_25_PERCENT) 셀 스타일 및 폰트 설정
    	styleHead = workbook.createCellStyle();
    	//정렬
    	styleHead.setAlignment(CellStyle.ALIGN_CENTER); //가운데 정렬
    	styleHead.setVerticalAlignment(CellStyle.VERTICAL_CENTER); //높이 가운데 정렬
    	//배경색
    	styleHead.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
    	styleHead.setFillPattern(CellStyle.SOLID_FOREGROUND);
    	//테두리 선 (우,좌,위,아래)
    	styleHead.setBorderRight(HSSFCellStyle.BORDER_THIN);
    	styleHead.setBorderLeft(HSSFCellStyle.BORDER_THIN);
    	styleHead.setBorderTop(HSSFCellStyle.BORDER_THIN);
    	styleHead.setBorderBottom(HSSFCellStyle.BORDER_THIN);
    	Font headerFont = this.sheet.getWorkbook().createFont();
        headerFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
        headerFont.setFontHeightInPoints((short)10);
        styleHead.setFont(headerFont);
        styleHead.setWrapText(true);

      //2.컬럼헤드(YELLOW) 셀 스타일 및 폰트 설정
    	styleHead1 = workbook.createCellStyle();
    	//정렬
    	styleHead1.setAlignment(CellStyle.ALIGN_CENTER); //가운데 정렬
    	styleHead1.setVerticalAlignment(CellStyle.VERTICAL_CENTER); //높이 가운데 정렬
    	//배경색
    	styleHead1.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
    	styleHead1.setFillPattern(CellStyle.SOLID_FOREGROUND);
    	//테두리 선 (우,좌,위,아래)
    	styleHead1.setBorderRight(HSSFCellStyle.BORDER_THIN);
    	styleHead1.setBorderLeft(HSSFCellStyle.BORDER_THIN);
    	styleHead1.setBorderTop(HSSFCellStyle.BORDER_THIN);
    	styleHead1.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        styleHead1.setFont(headerFont);
        styleHead1.setWrapText(true);
    	
    	//3.일반 셀(font9, WHITE) 스타일 및 폰트 설정
        styleNomal = workbook.createCellStyle();
    	//정렬
        styleNomal.setAlignment(CellStyle.ALIGN_LEFT); //좌측 정렬
        styleNomal.setVerticalAlignment(CellStyle.VERTICAL_CENTER); //높이 가운데 정렬
    	//배경색
    	styleNomal.setFillForegroundColor(IndexedColors.WHITE.getIndex());
    	styleNomal.setFillPattern(CellStyle.SOLID_FOREGROUND);
    	//테두리 선 (우,좌,위,아래)
    	styleNomal.setBorderRight(HSSFCellStyle.BORDER_THIN);
    	styleNomal.setBorderLeft(HSSFCellStyle.BORDER_THIN);
    	styleNomal.setBorderTop(HSSFCellStyle.BORDER_THIN);
    	styleNomal.setBorderBottom(HSSFCellStyle.BORDER_THIN);
    	Font nomalFont = this.sheet.getWorkbook().createFont();
    	nomalFont.setBoldweight(Font.BOLDWEIGHT_NORMAL);
    	nomalFont.setFontHeightInPoints((short)9);
        styleNomal.setFont(nomalFont);
        
      //4.수량합계 셀(GREY_40_PERCENT) 스타일 및 폰트 설정
        styleNomal1 = workbook.createCellStyle();
    	//정렬
        styleNomal1.setAlignment(CellStyle.ALIGN_CENTER); //가운데 정렬
        styleNomal1.setVerticalAlignment(CellStyle.VERTICAL_CENTER); //높이 가운데 정렬
    	//배경색
        styleNomal1.setFillForegroundColor(IndexedColors.GREY_40_PERCENT.getIndex());
        styleNomal1.setFillPattern(CellStyle.SOLID_FOREGROUND);
    	//테두리 선 (우,좌,위,아래)
        styleNomal1.setBorderRight(HSSFCellStyle.BORDER_THIN);
        styleNomal1.setBorderLeft(HSSFCellStyle.BORDER_THIN);
    	styleNomal1.setBorderTop(HSSFCellStyle.BORDER_THIN);
    	styleNomal1.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        styleNomal1.setFont(nomalFont);
        
      //5.일반 셀(font9, WHITE) 스타일 및 폰트 설정
        styleNomal2 = workbook.createCellStyle();
    	//정렬
        styleNomal2.setAlignment(CellStyle.ALIGN_RIGHT); //우측 정렬
        styleNomal2.setVerticalAlignment(CellStyle.VERTICAL_CENTER); //높이 가운데 정렬
    	//배경색
        styleNomal2.setFillForegroundColor(IndexedColors.WHITE.getIndex());
    	styleNomal2.setFillPattern(CellStyle.SOLID_FOREGROUND);
    	//테두리 선 (우,좌,위,아래)
    	styleNomal2.setBorderRight(HSSFCellStyle.BORDER_THIN);
    	styleNomal2.setBorderLeft(HSSFCellStyle.BORDER_THIN);
    	styleNomal2.setBorderTop(HSSFCellStyle.BORDER_THIN);
    	styleNomal2.setBorderBottom(HSSFCellStyle.BORDER_THIN);
    	styleNomal2.setFont(nomalFont);

    }
    
    private void printSheet(XSSFSheet sheet) {
    	setStyle();
    	waitBar.setStatus("    Insert Title");
    	setTitle();
    	waitBar.setStatus("    Set Column Head");
    	setColHead();
    	waitBar.setStatus("    Insert Contents");
    	setContents();
    }
    
    private void setColHead(){
    	String colHead = "";
    	int[] columnWidth = {1500,3500,2000,1800,2300,1700,1400,2400,8000,1700,2000,1700};
    	
    	if(ospec != null){
	    	for (int i = 0; i < 12; i++){
	    		colHead = columnHeaders.get(i).toString();
	    		addRow(colHead,3,i,styleHead);
	    		addRow("",4,i,styleHead);
	    		addRow("",5,i,styleHead);
	    		addRow("",6,i,styleHead);
	    		addRow("",7,i,styleHead);
	    		sheet.setColumnWidth(i, columnWidth[i]);
	    		sheet.addMergedRegion(new CellRangeAddress(3, 7, i, i));
	    	}
	    	
	    	sheet.getRow(3).getCell(11).setCellStyle(styleHead1);
	    	
	    	for (int i = 3; i < 8; i++) {
	            Row row = sheet.getRow(i);
	            if(row == null){
	            	row = sheet.createRow(i);
	            }
	            makeUsageColumn(sheet, row, i-3);
	        }
    	} else {
    		for (int i = 0; i < columnHeaders.size(); i++){
	    		colHead = columnHeaders.get(i).toString();
	    		addRow(colHead,3,i,styleHead);
    		}
    	}
    	
    }
    
    private void setTitle(){
    	addRow("Weight Master List(" + title + ")",1,0,styleTitle);
//    	for(int i=1; i<columnHeaders.size(); i++){
//    		addRow("",1,i,styleTitle);
//    	}
    	sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, columnHeaders.size()-1));
    }
    
    private void setContents(){
    	int[] intStyle = {0,5,6,11};
    	int startRow = 4;
    	if(ospec != null){
    		startRow = 8;
    	}
    	CellStyle cs = null;
        for (int i = 0; i < dataVector.size(); i++){
            Vector<?> row = dataVector.get(i);
            Object value = row.get(0);
            if (value == null){
                continue;
            }
            if(i == 0){
            	cs = styleNomal1;
            } 

            for (int j = 0; j < row.size(); j++){
                Object rowValue = row.get(j);
                if (j == 0) {
                	if(i != 0){
                		cs = styleNomal2;
                		addRow(i + "",startRow + i,j,cs);
                	} else {
                		addRow("",startRow + i,j,cs);
                	}
                } else {
                	if(i != 0){
	                	if(j== 5 || j== 6 || j >= 11){
	                		cs = styleNomal2;
	                	} else {
	                		cs = styleNomal;
	                	}
                	}
                	addRow(rowValue == null ? "" : rowValue.toString(),startRow + i,j,cs);
                }
            }
        }
    }
    
    private void makeUsageColumn(Sheet sheet, Row row, int lev) {
    	int START_COLUMN_INDEX = 12;
        String keyValue = "";
        String tempValue = "";
        int mergeLev = 0;
        String[] arrayKey = new String[5];
        
        for (int i = 0; i < usageOptionSetList.size(); i++) {
            arrayKey = usageOptionSetList.get(i).getName().split("_");
            String temp = arrayKey[0];
            arrayKey[0] = arrayKey[1];
            arrayKey[1] = arrayKey[2];
            arrayKey[2] = arrayKey[3];
            arrayKey[3] = arrayKey[4];
            arrayKey[4] = temp;
            tempValue = "";
            for (int j = 0; j < lev + 1; j++) {
                tempValue += arrayKey[j];
            }
            Cell cell = row.createCell(START_COLUMN_INDEX + i);
            cell.setCellStyle(styleHead);
            if (keyValue.equals("")) {
                cell.setCellValue(arrayKey[lev]);
                mergeLev++;
            } else if (keyValue.equals(tempValue)) {
                if (usageOptionSetList.size() == (i + 1)) {
                    sheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), cell.getColumnIndex() - mergeLev, cell.getColumnIndex()));
                }
                mergeLev++;
            } else if (!keyValue.equals(tempValue)) {
                if (mergeLev != 0) {
                    sheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), cell.getColumnIndex() - mergeLev, cell.getColumnIndex() - 1));
                }
                cell.setCellValue(arrayKey[lev]);
                mergeLev = 1;
            }
            keyValue = tempValue;
        }
    }
    
    public void addRow(String backgroundColor, short boldweight, List<String> cellStrings) {
        XSSFRow header = sheet.createRow(rowIndex++);
        int cellIndex = offsetCol;
        for (String value : cellStrings) {
          XSSFCell cell = header.createCell(cellIndex++, XSSFCell.CELL_TYPE_STRING);
          cell.setCellValue(value);
        }
        if (maxCols < cellIndex) {
          maxCols = cellIndex;
        }
      }
    
    public void addRow(String str, int rowInt, int cellInt, CellStyle style) {
        XSSFRow row = sheet.getRow(rowInt);
        if(row == null){
        	row = sheet.createRow(rowInt);
        }
        int cellIndex = offsetCol;
          XSSFCell cell = row.createCell(cellInt, XSSFCell.CELL_TYPE_STRING);
          cell.setCellValue(str);
          cell.setCellStyle(style);
        if (maxCols < cellIndex) {
          maxCols = cellIndex;
        }
      }
    
    public void openFile() {
        try {
            Desktop.getDesktop().open(exportToFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    
}
