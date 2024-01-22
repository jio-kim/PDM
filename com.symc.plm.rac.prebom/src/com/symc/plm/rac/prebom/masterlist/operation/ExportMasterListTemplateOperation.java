package com.symc.plm.rac.prebom.masterlist.operation;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ssangyong.commands.ospec.op.OSpec;
import com.ssangyong.commands.ospec.op.OpTrim;
import com.symc.plm.rac.prebom.ccn.excel.common.ExcelTemplateHelper;
import com.teamcenter.rac.aif.AbstractAIFOperation;

/**
 * [20171215][LJG] Proto Tooling 컬럼 추가로 인한, Excel컬럼 index 하나씩 밀림 현상 수정
 * [20180424][CSH] OSPEC NO 컬럼 추가로 인한, Excel컬럼 index 하나씩 당겨짐 현상 수정
 * [20190318][CSH] Column Header 자동 / 필수 / 선택 항목으로 변경, 기술관리 송대영 책임 요청
 */
public class ExportMasterListTemplateOperation extends AbstractAIFOperation{

    private OSpec ospec;
    private List<OpTrim> ospecTrimList;
    private File templateFile;
    private CellStyle cellStyle;      // 선택된 스타일
    private CellStyle cellStyle2;     // 선택
    private CellStyle cellStyle3;     // 필수 (살색)
    private CellStyle cellStyle4;     // 자동1 (청록색)
    private CellStyle cellStyle5;     // 자동2 (연두색)
    private String templateName = "S7_TEM_DocItemID_MastList";
    
    private String fixColumn[] = {"S/MODE", "WEIGHT", /** 20201223 전성용 WEIGHT_MANAGEMENT Column 추가 */"Weight 관리\n(STD)", "MODULE", "ALTER\nPART", "DR",/** 20200923 seho EJS Column 추가 */"EJS", "Responsibility", "Change\nDescription", "MATERIAL COST", "DVP SAMPLE", 
            "CONCEPT DWG", "PRD. DWG", "Design Concept Doc.", "DESIGN CHARGE", "업체명", "예상투자비", "PRD INVENSTMENT COST", "PROCUMENT"};
    private String fixColumnSize[] = {"0/4", "1/0", "0/4"/** 20201223 전성용 WEIGHT_MANAGEMENT Column 추가 */, "0/4", "0/4", "0/4", /** 20200923 seho EJS Column 추가 */"0/4", "0/4", "0/4", "1/0", "2/0", 
            "3/0", "2/0", "2/0", "1/0", "0/4", "0/0", "3/0", "1/0"};
    private String fixColumn2[] = {"", "ESTIMATE,TARGET", /** 20201223 전성용 WEIGHT_MANAGEMENT Column 추가 */"", "", "", "", /** 20200923 seho EJS Column 추가 */"", "", "", "ESTIMATE,TARGET", "요구수량,용도,요청부서",
            "실적,계획,2D/3D,REL. DATE", "실적,계획,ECO/NO", "OSPEC NO,Doc. No.,Rel. Date", "TEAM,CHARGER", "", "PROTO TOOL'G", "TOOL'G,SVC. COST,SAMPLE,SUM", "TEAM,CHARGER"};
    
    private String autoColumns1[] = {"Design Concept Doc.", "OSPEC NO", "Doc. No.", "Rel. Date"};  // 자동1
    private String autoColumns2[] = {"예상투자비", "PRD INVENSTMENT COST", "PROCUMENT", "MATERIAL COST", "업체명", "PROTO TOOL'G", "TOOL'G", "SVC. COST", "SAMPLE" ,"SUM"};  // 자동2
    private String essentialColumns[] = {"S/MODE", "ESTIMATE", "Responsibility", "DESIGN CHARGE", "TEAM", "CHARGER"};                                      // 필수
    private List<String> autoColumnArray1;
    private List<String> autoColumnArray2;
    private List<String> essentialColumnArray;

    //[20171215][LJG]다운 받은 템플릿 엑셀 파일에 추가 되는 Trim컬럼들의 시작 인덱스 (18->19로 수정)
    private static final int START_COLUMN_INDEX = 19;
  //[20190318][CSH]Column Header 색상 표기로 인한 Start Row 변경
    private static final int START_ROW_INDEX = 2;
    
    public ExportMasterListTemplateOperation(OSpec targetOspec) {
        this.ospec = targetOspec;
    }
    
    @Override
    public void executeOperation() throws Exception {
        try {
            // 필수, 선택 컬럼 셋팅
            settingColumnStyle();
            
            String defaultFileName = "MasterList" + "_" + ExcelTemplateHelper.getToday("yyyyMMdd");
            templateFile = getTemplateFile(0, templateName, defaultFileName);
            Workbook workbook = new XSSFWorkbook(new FileInputStream(templateFile));
            
            printSheet(workbook);
            
            XSSFFormulaEvaluator.evaluateAllFormulaCells((XSSFWorkbook) workbook);

            FileOutputStream fos = new FileOutputStream(templateFile);
            workbook.write(fos);
            fos.flush();
            fos.close();
            openFile();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 필수, 선택적 필수 의 컬럼 추가로 인한 셋팅 
     */
    private void settingColumnStyle() {
        autoColumnArray1 = new ArrayList<String>();
        autoColumnArray2 = new ArrayList<String>();
        essentialColumnArray = new ArrayList<String>();
        for (int i = 0; i < autoColumns1.length; i++) {
        	autoColumnArray1.add(autoColumns1[i]);
        }
        for (int i = 0; i < autoColumns2.length; i++) {
        	autoColumnArray2.add(autoColumns2[i]);
        }
        for (int j = 0; j < essentialColumns.length; j++) {
            essentialColumnArray.add(essentialColumns[j]);
        }
    }

    /**
     * MasterList Sheet 에 Usage Header 정보를 만든 후 다음 컬럼 사이에 빈 컬럼들은 Remove 시킨다
     * 
     * @param workbook
     */
    private void printSheet(Workbook workbook) {
        ospecTrimList = ospec.getTrimList();
        Sheet sheet = workbook.getSheetAt(0);
//        cellStyle = getCellStyle(workbook);
        cellStyle2 = sheet.getRow(0).getCell(2).getCellStyle();//선택
        cellStyle3 = sheet.getRow(0).getCell(3).getCellStyle();//필수
        cellStyle4 = sheet.getRow(0).getCell(1).getCellStyle();//자동1
        cellStyle5 = sheet.getRow(0).getCell(4).getCellStyle();//자동2
        for (int i = 0; i < 5; i++) {
            Row row = sheet.getRow(i+START_ROW_INDEX);
            makeUsageColumn(sheet, row, i);
        }
        makeFixColumn(sheet, sheet.getRow(0+START_ROW_INDEX), 1+START_ROW_INDEX);
    }

    /**
     * MasterList Sheet 에 Usage Header 정보를 만든다 (Row 의 Usage 컬럼을 만든다)
     * 
     * @param sheet
     * @param row
     * @param lev
     */
    private void makeUsageColumn(Sheet sheet, Row row, int lev) {
        String keyValue = "";
        String tempValue = "";
        int mergeLev = 0;
        String[] arrayKey = new String[5];
        
        for (int i = 0; i < ospecTrimList.size(); i++) {
            arrayKey = ospecTrimList.get(i).toString().split("_");
            tempValue = "";
            for (int j = 0; j < lev + 1; j++) {
                tempValue += arrayKey[j];
            }
            Cell cell = row.createCell(START_COLUMN_INDEX + i);
            cell.setCellStyle(cellStyle3);
            if (keyValue.equals("")) {
                cell.setCellValue(arrayKey[lev]);
                mergeLev++;
            } else if (keyValue.equals(tempValue)) {
                if (ospecTrimList.size() == (i + 1)) {
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
    
    /**
     * Usage 컬럼 뒤에 고정컬럼을 만든다
     * 
     * @param sheet
     * @param row
     * @param rowSeq
     */
    private void makeFixColumn(Sheet sheet, Row row, int rowSeq) {
        int seq = 0;
        for (int j = 0; j < fixColumn.length; j++) {
            Cell cell = row.createCell(START_COLUMN_INDEX + ospecTrimList.size() + j + seq);
            cell.setCellValue(fixColumn[j]);
            seq += mergeColumn(sheet, row, cell, rowSeq, j, fixColumn[j]);
        }
    }

    /**
     * 고정컬럼 Merge 및 SubColumn 을 만든다
     * 
     * @param sheet
     * @param row
     * @param cell
     * @param rowSeq
     * @param j
     * @param fixColumn 
     * @return
     */
    private int mergeColumn(Sheet sheet, Row row, Cell cell, int rowSeq, int j, String fixColumnName) {
        String columnSize[] = fixColumnSize[j].split("/");

        int rowStart = rowSeq - 1;
        int rowEnd = rowStart + Integer.parseInt(columnSize[1]);
        int cellStart = cell.getColumnIndex();
        int cellEnd = cell.getColumnIndex() + Integer.parseInt(columnSize[0]);
        
        sheet.addMergedRegion(new CellRangeAddress(rowStart, rowEnd, cellStart, cellEnd));
        settingCellStyle(fixColumnName);
        cleanBeforeMergeOnValidCells(sheet, new CellRangeAddress(rowStart, rowEnd, cellStart, cellEnd), cellStyle);
        if (fixColumn2[j].length() > 0) {
            String subColumn[] = fixColumn2[j].split(",");
            Row subRow = sheet.getRow(rowStart + 1);
            rowStart = rowStart + 1;
            rowEnd = rowStart + 3;
            for (int i = 0; i < subColumn.length; i++) {
                Cell subCell = subRow.createCell(cellStart + i);
                subCell.setCellValue(subColumn[i]);
                sheet.addMergedRegion(new CellRangeAddress(rowStart, rowEnd, cellStart + i, cellStart + i));
                // 필수 선택에 TEAM, CHARGER 에 컬럼이 중복이라서 하드코딩으로 지정해둠
                if (fixColumnName.equals("PROCUMENT")) {
                    cellStyle = cellStyle5;
                } else if (fixColumnName.equals("MATERIAL COST")) {
                        cellStyle = cellStyle5;
                } else {
                    settingCellStyle(subColumn[i]);
                }
                cleanBeforeMergeOnValidCells(sheet, new CellRangeAddress(rowStart, rowEnd, cellStart + i, cellStart + i), cellStyle);
            }            
        }
        return Integer.parseInt(columnSize[0]);
    }
    
    /**
     * 팀센터에 저장되어 있는 MasterList 템플릿을 가져온다
     * 
     * @param mode
     * @param preferenceName
     * @param defaultFileName
     * @return
     */
    public File getTemplateFile(int mode, String preferenceName, String defaultFileName) {
        return ExcelTemplateHelper.getTemplateFile(mode, preferenceName, defaultFileName);
    }
    
    /**
     * 엑셀을 오픈한다
     */
    public void openFile() {
        try {
            Desktop.getDesktop().open(templateFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * CellStyle 지정
     * @param workbook
     * @return
     */
    @SuppressWarnings("unused")
    private CellStyle getCellStyle(Workbook workbook){
        
        CellStyle style = (XSSFCellStyle) workbook.createCellStyle();
        style.setBorderTop(XSSFCellStyle.BORDER_THIN);
        style.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        style.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        style.setBorderRight(XSSFCellStyle.BORDER_THIN);
        
        style.setTopBorderColor(HSSFColor.BLACK.index);
        style.setBottomBorderColor(HSSFColor.BLACK.index);
        style.setLeftBorderColor(HSSFColor.BLACK.index);
        style.setRightBorderColor(HSSFColor.BLACK.index);
        
        style.setAlignment(XSSFCellStyle.ALIGN_CENTER);
        style.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);
        style.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
        style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        
        Font font = workbook.createFont();
        font.setFontName("맑은 고딕");
        font.setFontHeightInPoints((short)9);
        
        style.setFont(font);
        
        return style;
        
    }
    
    /**
     *  세로로 Merge 된 Cell 에 CellStyle 적용 
     *  
     * @param sheet
     * @param region
     * @param cellStyle
     */
    private void cleanBeforeMergeOnValidCells(Sheet sheet,CellRangeAddress region, CellStyle cellStyle ) {
        for(int rowNum =region.getFirstRow();rowNum<=region.getLastRow();rowNum++){
            Row row= sheet.getRow(rowNum);
            if(row==null){
                sheet.createRow(rowNum);
            }
            for(int colNum=region.getFirstColumn();colNum<=region.getLastColumn();colNum++){
                Cell currentCell = row.getCell(colNum); 
                if(currentCell==null){
                    currentCell = row.createCell(colNum);
                }    
                currentCell.setCellStyle(cellStyle);
            }
        }
    }
    
    /**
     * 컬럼별 스타일 가져오기
     * @param columnName
     */
    private void settingCellStyle(String columnName){
        if (autoColumnArray1.contains(columnName)) {
            cellStyle = cellStyle4;
        }else if(autoColumnArray2.contains(columnName)){
            cellStyle = cellStyle5;
        }else if(essentialColumnArray.contains(columnName)){
            cellStyle = cellStyle3;
        }else{
            cellStyle = cellStyle2;
        }
    }
}