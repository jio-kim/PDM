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

import com.kgm.commands.ospec.op.OSpec;
import com.kgm.commands.ospec.op.OpTrim;
import com.symc.plm.rac.prebom.ccn.excel.common.ExcelTemplateHelper;
import com.teamcenter.rac.aif.AbstractAIFOperation;

/**
 * [20171215][LJG] Proto Tooling �÷� �߰��� ����, Excel�÷� index �ϳ��� �и� ���� ����
 * [20180424][CSH] OSPEC NO �÷� �߰��� ����, Excel�÷� index �ϳ��� ����� ���� ����
 * [20190318][CSH] Column Header �ڵ� / �ʼ� / ���� �׸����� ����, ������� �۴뿵 å�� ��û
 */
public class ExportMasterListTemplateOperation extends AbstractAIFOperation{

    private OSpec ospec;
    private List<OpTrim> ospecTrimList;
    private File templateFile;
    private CellStyle cellStyle;      // ���õ� ��Ÿ��
    private CellStyle cellStyle2;     // ����
    private CellStyle cellStyle3;     // �ʼ� (���)
    private CellStyle cellStyle4;     // �ڵ�1 (û�ϻ�)
    private CellStyle cellStyle5;     // �ڵ�2 (���λ�)
    private String templateName = "S7_TEM_DocItemID_MastList";
    
    private String fixColumn[] = {"S/MODE", "WEIGHT", /** 20201223 ������ WEIGHT_MANAGEMENT Column �߰� */"Weight ����\n(STD)", "MODULE", "ALTER\nPART", "DR",/** 20200923 seho EJS Column �߰� */"EJS", "Responsibility", "Change\nDescription", "MATERIAL COST", "DVP SAMPLE", 
            "CONCEPT DWG", "PRD. DWG", "Design Concept Doc.", "DESIGN CHARGE", "��ü��", "�������ں�", "PRD INVENSTMENT COST", "PROCUMENT"};
    private String fixColumnSize[] = {"0/4", "1/0", "0/4"/** 20201223 ������ WEIGHT_MANAGEMENT Column �߰� */, "0/4", "0/4", "0/4", /** 20200923 seho EJS Column �߰� */"0/4", "0/4", "0/4", "1/0", "2/0", 
            "3/0", "2/0", "2/0", "1/0", "0/4", "0/0", "3/0", "1/0"};
    private String fixColumn2[] = {"", "ESTIMATE,TARGET", /** 20201223 ������ WEIGHT_MANAGEMENT Column �߰� */"", "", "", "", /** 20200923 seho EJS Column �߰� */"", "", "", "ESTIMATE,TARGET", "�䱸����,�뵵,��û�μ�",
            "����,��ȹ,2D/3D,REL. DATE", "����,��ȹ,ECO/NO", "OSPEC NO,Doc. No.,Rel. Date", "TEAM,CHARGER", "", "PROTO TOOL'G", "TOOL'G,SVC. COST,SAMPLE,SUM", "TEAM,CHARGER"};
    
    private String autoColumns1[] = {"Design Concept Doc.", "OSPEC NO", "Doc. No.", "Rel. Date"};  // �ڵ�1
    private String autoColumns2[] = {"�������ں�", "PRD INVENSTMENT COST", "PROCUMENT", "MATERIAL COST", "��ü��", "PROTO TOOL'G", "TOOL'G", "SVC. COST", "SAMPLE" ,"SUM"};  // �ڵ�2
    private String essentialColumns[] = {"S/MODE", "ESTIMATE", "Responsibility", "DESIGN CHARGE", "TEAM", "CHARGER"};                                      // �ʼ�
    private List<String> autoColumnArray1;
    private List<String> autoColumnArray2;
    private List<String> essentialColumnArray;

    //[20171215][LJG]�ٿ� ���� ���ø� ���� ���Ͽ� �߰� �Ǵ� Trim�÷����� ���� �ε��� (18->19�� ����)
    private static final int START_COLUMN_INDEX = 19;
  //[20190318][CSH]Column Header ���� ǥ��� ���� Start Row ����
    private static final int START_ROW_INDEX = 2;
    
    public ExportMasterListTemplateOperation(OSpec targetOspec) {
        this.ospec = targetOspec;
    }
    
    @Override
    public void executeOperation() throws Exception {
        try {
            // �ʼ�, ���� �÷� ����
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
     * �ʼ�, ������ �ʼ� �� �÷� �߰��� ���� ���� 
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
     * MasterList Sheet �� Usage Header ������ ���� �� ���� �÷� ���̿� �� �÷����� Remove ��Ų��
     * 
     * @param workbook
     */
    private void printSheet(Workbook workbook) {
        ospecTrimList = ospec.getTrimList();
        Sheet sheet = workbook.getSheetAt(0);
//        cellStyle = getCellStyle(workbook);
        cellStyle2 = sheet.getRow(0).getCell(2).getCellStyle();//����
        cellStyle3 = sheet.getRow(0).getCell(3).getCellStyle();//�ʼ�
        cellStyle4 = sheet.getRow(0).getCell(1).getCellStyle();//�ڵ�1
        cellStyle5 = sheet.getRow(0).getCell(4).getCellStyle();//�ڵ�2
        for (int i = 0; i < 5; i++) {
            Row row = sheet.getRow(i+START_ROW_INDEX);
            makeUsageColumn(sheet, row, i);
        }
        makeFixColumn(sheet, sheet.getRow(0+START_ROW_INDEX), 1+START_ROW_INDEX);
    }

    /**
     * MasterList Sheet �� Usage Header ������ ����� (Row �� Usage �÷��� �����)
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
     * Usage �÷� �ڿ� �����÷��� �����
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
     * �����÷� Merge �� SubColumn �� �����
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
                // �ʼ� ���ÿ� TEAM, CHARGER �� �÷��� �ߺ��̶� �ϵ��ڵ����� �����ص�
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
     * �����Ϳ� ����Ǿ� �ִ� MasterList ���ø��� �����´�
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
     * ������ �����Ѵ�
     */
    public void openFile() {
        try {
            Desktop.getDesktop().open(templateFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * CellStyle ����
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
        font.setFontName("���� ���");
        font.setFontHeightInPoints((short)9);
        
        style.setFont(font);
        
        return style;
        
    }
    
    /**
     *  ���η� Merge �� Cell �� CellStyle ���� 
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
     * �÷��� ��Ÿ�� ��������
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