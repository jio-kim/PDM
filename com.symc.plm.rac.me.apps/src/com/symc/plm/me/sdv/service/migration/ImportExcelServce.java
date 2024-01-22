/**
 * 
 */
package com.symc.plm.me.sdv.service.migration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.symc.plm.me.sdv.service.migration.util.PEExcelConstants;
import com.symc.plm.me.utils.BundleUtil;

/**
 * Class Name : ImportExcelServce
 * Class Description :
 * 
 * @date 2013. 11. 14.
 * 
 */
public class ImportExcelServce {

    /**
     * POI Excel Workboook을 가져온다.
     * 
     * @method getWorkBook
     * @date 2013. 11. 14.
     * @param
     * @return Workbook
     * @exception
     * @throws
     * @see
     */
    public static Workbook getWorkBook(String filePath) throws Exception {
        if (StringUtils.isEmpty(filePath)) {
            throw new Exception("Excel 파일 이름이 틀리거나 존재하지 않습니다.");
        }
        FileInputStream fis = new FileInputStream(new File(filePath));
        return WorkbookFactory.create(fis);
    }

    /**
     * Excel Cell Value Return
     * 
     * CELL_TYPE_NUMERIC인 경우 Integer로 Casting하여 반환함
     * Long 형태의 값을 원할경우 다르게 구현해야 함.
     * 
     * @param cell
     * @return
     */
    public static String getCellText(Cell cell) {
        String value = "";
        if (cell != null) {
            switch (cell.getCellType()) {
            case XSSFCell.CELL_TYPE_FORMULA:
                value = cell.getCellFormula();
                break;
            // Integer로 Casting하여 반환함
            case XSSFCell.CELL_TYPE_NUMERIC:
                value = ("" + getFormatedString(cell.getNumericCellValue())).trim();
                break;
            case XSSFCell.CELL_TYPE_STRING:
                value = ("" + cell.getStringCellValue()).trim();
                break;
            case XSSFCell.CELL_TYPE_BLANK:
                // value = "" + cell.getBooleanCellValue();
                value = "";
                break;
            case XSSFCell.CELL_TYPE_ERROR:
                value = ("" + cell.getErrorCellValue()).trim();
                break;
            case XSSFCell.CELL_TYPE_BOOLEAN:
                value = "" + cell.getBooleanCellValue();
                break;
            default:
            }
        }
        if (StringUtils.equalsIgnoreCase("NULL", value)) {
            value = "";
        }
        return BundleUtil.nullToString(value);
    }

    /**
     * 
     * 
     * @method getFormatedString
     * @date 2013. 11. 14.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    public static String getFormatedString(double value) {
        DecimalFormat df = new DecimalFormat("#####################.####");//
        return df.format(value);
    }

    /**
     * Export Wirrte Excel
     * 
     * @method writeExportExcel
     * @date 2013. 12. 24.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public static void writeExportExcel(String filePath, ArrayList<String> headerList, ArrayList<ArrayList<String>> rowList) throws Exception {
        checkFolder(filePath);
//        HSSFWorkbook workbook = new HSSFWorkbook();
//        HSSFSheet sheet = workbook.createSheet("export");
//        HSSFCellStyle style = workbook.createCellStyle();
//        style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
//        style.setBottomBorderColor(HSSFColor.BLACK.index);
//        style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
//        style.setLeftBorderColor(HSSFColor.GREEN.index);
//        style.setBorderRight(HSSFCellStyle.BORDER_THIN);
//        style.setRightBorderColor(HSSFColor.BLUE.index);
//        style.setBorderTop(HSSFCellStyle.BORDER_MEDIUM_DASHED);
//        style.setTopBorderColor(HSSFColor.BLACK.index);
//        style.setFillBackgroundColor(HSSFColor.LIGHT_BLUE.index);
        
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet();
        

        XSSFCellStyle style = wb.createCellStyle();
        style.setFillForegroundColor(HSSFColor.LIGHT_BLUE.index);
        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
        style.setBorderTop(XSSFCellStyle.BORDER_MEDIUM);
        style.setBorderBottom(XSSFCellStyle.BORDER_MEDIUM);
        style.setBorderLeft(XSSFCellStyle.BORDER_MEDIUM);
        style.setBorderRight(XSSFCellStyle.BORDER_MEDIUM);
        
        // Header
        XSSFRow headerRow = sheet.createRow(1);
        for (int i = 0; i < headerList.size(); i++) {
            XSSFCell cell = headerRow.createCell(i);
            cell.setCellStyle(style);
            cell.setCellValue(headerList.get(i));
        }
        // Row
        // START_ROW_INDEX
        int rowIndex = 0;
        for (int i = 0; i < rowList.size(); i++) {
            rowIndex = PEExcelConstants.START_ROW_INDEX + i;
            ArrayList<String> rowInfos = rowList.get(i);
            XSSFRow dataRow = sheet.createRow(rowIndex);
            for (int j = 0; j < rowInfos.size(); j++) {
                XSSFCell datacell = dataRow.createCell(j);
                // datacell.setCellStyle(style);
                datacell.setCellValue(rowInfos.get(j));
            }
        }
        // END Row에 "EOF" 문자 등록
        int eofRowIndex = 0;
        // Row Data가 사이즈가 0인 경우
        if (rowIndex == 0) {
            eofRowIndex = PEExcelConstants.START_ROW_INDEX;
        } else {
            eofRowIndex = rowIndex + 1;
        }
        XSSFRow eofRow = sheet.createRow(eofRowIndex);
        XSSFCell eofCell = eofRow.createCell(PEExcelConstants.END_CHAR_COLUMN_INDEX);
        eofCell.setCellValue(PEExcelConstants.END_CHAR_ROW_STRING);
        FileOutputStream fs = null;
        try {
            fs = new FileOutputStream(filePath);
            wb.write(fs);
        } catch (Exception e) {
            throw e;
        } finally {
            if (fs != null) {
                fs.close();
            }
        }
    }

    /**
     * File Path를 가지고 폴더가 생성되어있는지 확인하여 생성되어 있지않으면 생성한다.
     * 
     * @method checkFolder
     * @date 2013. 12. 26.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private static void checkFolder(String filePath) {
        File writableExcelFile = new File(filePath);
        File writableExcelFolder = new File(writableExcelFile.getParent());
        // Folder가 없으므로 생성한다.
        if (!writableExcelFolder.exists()) {
            writableExcelFolder.mkdirs();
        }
    }
}
