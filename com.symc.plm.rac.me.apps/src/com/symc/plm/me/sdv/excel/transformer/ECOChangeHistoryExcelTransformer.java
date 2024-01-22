package com.symc.plm.me.sdv.excel.transformer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;

import com.teamcenter.rac.util.Registry;

public class ECOChangeHistoryExcelTransformer extends AbstractExcelTransformer {

    private final int HEADER_CELL_COUNT = 28;
    private final int DATA_START_ROW_INDEX = 5;

    private ArrayList<Row> changeBackgroundRow = new ArrayList<Row>();

    private Registry registry;

    public ECOChangeHistoryExcelTransformer() {
        this.registry = Registry.getRegistry(this);
    }

    @Override
    public void print(int mode, String templatePreference, String defaultFileName, IDataSet dataSet) throws Exception {
        // PreferenceName으로 템플릿 파일 가져오기
        templateFile = getTemplateFile(mode, templatePreference, defaultFileName);

        try {
            Workbook workbook = new XSSFWorkbook(new FileInputStream(templateFile));
            List<HashMap<String, Object>> dataList = getTableData(dataSet);

            if (dataList != null) {
                Sheet currentSheet = workbook.getSheetAt(0);

                IDataMap dataMap = dataSet.getDataMap("additionalInfo");
                printHeaderInfo(workbook, currentSheet, dataMap);
                int rowSize = dataList.size();
                for (int i = 0; i < rowSize; i++) {
                    printRow(currentSheet, i, dataList.get(i));
                }
                setCellStyleOfContents(workbook, currentSheet);
            }

            FileOutputStream fos = new FileOutputStream(templateFile);
            workbook.write(fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<HashMap<String, Object>> getTableData(IDataSet dataSet) {
        Collection<HashMap<String, Object>> data = null;

        IDataMap dataMap = dataSet.getDataMap("ECOChangeHistoryList");
        if (dataMap != null) {
            data = dataMap.getTableValue("ECOChangeHistoryList");
        }

        return (List<HashMap<String, Object>>) data;
    }

    /**
     *
     *
     * @method printRow
     * @date 2014. 3. 21.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void printRow(Sheet currentSheet, int num, HashMap<String, Object> dataMap) {
        Row row = currentSheet.createRow(this.DATA_START_ROW_INDEX + num);

        // NO
        Cell cell = row.createCell(0);
        cell.setCellValue((String) dataMap.get("no"));

        // Proj
        cell = row.createCell(1);
        cell.setCellValue((String) dataMap.get("project"));

        // Find No
        cell = row.createCell(2);
        cell.setCellValue((String) dataMap.get("find_no"));

        // C/T
        cell = row.createCell(3);
        cell.setCellValue((String) dataMap.get("ct"));

        // Parent No
        cell = row.createCell(4);
        cell.setCellValue((String) dataMap.get("parent_no"));

        // Parent Rev
        cell = row.createCell(5);
        cell.setCellValue((String) dataMap.get("parent_rev"));

        // Part Origin
        cell = row.createCell(6);
        cell.setCellValue((String) dataMap.get("part_origin"));

        // Part No
        cell = row.createCell(7);
        cell.setCellValue((String) dataMap.get("part_no"));

        // Part Rev
        cell = row.createCell(8);
        cell.setCellValue((String) dataMap.get("part_rev"));

        // Part Name
        cell = row.createCell(9);
        cell.setCellValue((String) dataMap.get("part_name"));

        // IC
        cell = row.createCell(10);
        cell.setCellValue((String) dataMap.get("ic"));

        // Supply Mode
        cell = row.createCell(11);
        cell.setCellValue((String) dataMap.get("supply_mode"));

        // QTY
        cell = row.createCell(12);
        cell.setCellValue((String) dataMap.get("qty"));

        // ALT
        cell = row.createCell(13);
        cell.setCellValue((String) dataMap.get("alt"));

        // SEL
        cell = row.createCell(14);
        cell.setCellValue((String) dataMap.get("sel"));

        // CAT
        cell = row.createCell(15);
        cell.setCellValue((String) dataMap.get("cat"));

        // Color
        cell = row.createCell(16);
        cell.setCellValue((String) dataMap.get("color"));

        // Color Section
        cell = row.createCell(17);
        cell.setCellValue((String) dataMap.get("color_section"));

        // Module Code
        cell = row.createCell(18);
        cell.setCellValue((String) dataMap.get("module_code"));

        // PLT Stk
        cell = row.createCell(19);
        cell.setCellValue((String) dataMap.get("plt_stk"));

        // A/S Stk
        cell = row.createCell(20);
        cell.setCellValue((String) dataMap.get("as_stk"));

        // Cost
        cell = row.createCell(21);
        cell.setCellValue((String) dataMap.get("cost"));

        // Tool
        cell = row.createCell(22);
        cell.setCellValue((String) dataMap.get("tool"));

        // Shown-On
        cell = row.createCell(23);
        cell.setCellValue((String) dataMap.get("shown_on"));

        // Options
        cell = row.createCell(24);
        cell.setCellValue((String) dataMap.get("options"));

        // Change Desc
        cell = row.createCell(25);
        cell.setCellValue((String) dataMap.get("change_desc"));

        // ECO No
        cell = row.createCell(26);
        cell.setCellValue((String) dataMap.get("eco_no"));

        // Date
        cell = row.createCell(27);
        cell.setCellValue((String) dataMap.get("release_date"));

        if (dataMap.get("isColorSetting") != null) {
            if (dataMap.get("isColorSetting").equals("1")) {
                changeBackgroundRow.add(row);
            }
        }
    }

    /**
     *
     *
     * @method printHeaderInfo
     * @date 2014. 3. 21.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings("unchecked")
    private void printHeaderInfo(Workbook workbook, Sheet currentSheet, IDataMap dataMap) {
        HashMap<String, Object> conditionMap = (HashMap<String, Object>) dataMap.getValue("conditionMap");

        Row row = null;
        Cell cell = null;

        // Product
        String product = (String) conditionMap.get("product");
        row = currentSheet.getRow(1);
        cell = row.getCell(0);
        cell.setCellValue("Product : " + product);

        // Date
        String fromDate = (String) conditionMap.get("fromDate");
        String toDate = (String) conditionMap.get("toDate");
        cell = row.getCell(3);
        cell.setCellValue("Date : " + fromDate + " - " + toDate);

        // BOM Instance
        String bomInstance = (String) conditionMap.get("bomInstance");
        row = currentSheet.getRow(2);
        cell = row.getCell(0);
        cell.setCellValue("BOM Instance : " + bomInstance);

        // Change Type
        String changeType = (String) conditionMap.get("changeType");
        cell = row.getCell(3);
        cell.setCellValue("Change Type : " + changeType);

        // 출력 일시
        Font font = workbook.createFont();
        font.setFontName("Arial");
        font.setBoldweight(Font.BOLDWEIGHT_BOLD);
        font.setFontHeightInPoints((short) 11);

        XSSFCellStyle style = (XSSFCellStyle) workbook.createCellStyle();
        style.setFont(font);
        style.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);
        style.setAlignment(XSSFCellStyle.ALIGN_RIGHT);
        style.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
        style.setFillForegroundColor(new XSSFColor(new byte[] { 0, (byte) 204, (byte) 255 }));

        row = currentSheet.getRow(2);
        cell = row.getCell(27);
        cell.setCellValue(String.format(registry.getString("report.ExcelExportDate", "출력 일시 : %s"), dataMap.getStringValue("excelExportDate")));
        cell.setCellStyle(style);
    }

    /**
     *
     *
     * @method setCellStyleOfContents
     * @date 2014. 3. 21.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void setCellStyleOfContents(Workbook workbook, Sheet currentSheet) {
        // 가운데 정렬 column
        int[] center_columnArray = new int[] { 0, 1, 2, 3, 5, 6, 7, 8, 10, 11, 12, 13, 14, 15, 16, 17, 18, 21, 22, 23, 26, 27 };
        ArrayList<Integer> center_columnList = new ArrayList<Integer>();
        for (int i = 0; i < center_columnArray.length; i++) {
            center_columnList.add(center_columnArray[i]);
        }

        int lastRowNum = currentSheet.getLastRowNum();
        for (int i = DATA_START_ROW_INDEX; i <= lastRowNum; i++) {
            // 스타일
            CellStyle style = workbook.createCellStyle();
            CellStyle style1 = workbook.createCellStyle();

            Font font = workbook.createFont();
            font.setFontName("Arial");
            font.setFontHeightInPoints((short) 10);

            style.setFont(font);
            style.setBorderTop(XSSFCellStyle.BORDER_THIN);
            style.setBorderBottom(XSSFCellStyle.BORDER_THIN);
            style.setBorderLeft(XSSFCellStyle.BORDER_THIN);
            style.setBorderRight(XSSFCellStyle.BORDER_THIN);

            style1.setFont(font);
            style1.setBorderTop(XSSFCellStyle.BORDER_THIN);
            style1.setBorderBottom(XSSFCellStyle.BORDER_THIN);
            style1.setBorderLeft(XSSFCellStyle.BORDER_THIN);
            style1.setBorderRight(XSSFCellStyle.BORDER_THIN);
            style1.setAlignment(XSSFCellStyle.ALIGN_CENTER);

            Row row = currentSheet.getRow(i);
            if (changeBackgroundRow.contains(row)) {
                style.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
                style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index);
                style1.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
                style1.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index);
            }

            for (int j = 0; j < HEADER_CELL_COUNT; j++) {
                if (center_columnList.contains(j)) {
                    row.getCell(j).setCellStyle(style1);
                } else {
                    row.getCell(j).setCellStyle(style);
                }
            }
        }

        changeBackgroundRow.clear();
    }

}
