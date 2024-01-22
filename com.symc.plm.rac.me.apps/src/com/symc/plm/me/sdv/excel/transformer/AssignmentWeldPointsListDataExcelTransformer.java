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
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;

public class AssignmentWeldPointsListDataExcelTransformer extends AbstractExcelTransformer {

    private final int dataStartIndex = 5;
    private final int CellCnt = 13;

    public AssignmentWeldPointsListDataExcelTransformer() {
        super();
    }

    @Override
    public void print(int mode, String templatePreference, String defaultFileName, IDataSet dataSet) {
        // PreferenceName으로 템플릿 파일 가져오기
        templateFile = getTemplateFile(mode, templatePreference, defaultFileName);

        try {
            Workbook workbook = new XSSFWorkbook(new FileInputStream(templateFile));
            List<HashMap<String, Object>> dataList = getTableData(dataSet);

            if(dataList != null) {
                Sheet currentSheet = workbook.getSheetAt(0);

                IDataMap dataMap = dataSet.getDataMap("additionalInfo");
                printHeaderInfo(workbook, currentSheet, dataMap);
                //operationType = dataMap.getStringValue("operationType");

                int rowSize = dataList.size();
                for(int i = 0; i < rowSize; i++) {
                    printRow(currentSheet, i, dataList.get(i));
                }
                setBorderAndAlign(workbook, currentSheet);

                XSSFFormulaEvaluator.evaluateAllFormulaCells((XSSFWorkbook) workbook);
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

        IDataMap dataMap = dataSet.getDataMap("operationList");
        if(dataMap != null) {
            data = dataMap.getTableValue("operationList");
        }

        return (List<HashMap<String, Object>>) data;
    }

    /**
     * 한 Row씩 출력한다.
     *
     * @method printRow
     * @date 2013. 10. 28.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void printRow(Sheet currentSheet, int num, HashMap<String, Object> dataMap) {
        Row row = currentSheet.createRow(this.dataStartIndex + num);

        // 순번
        Cell cell = row.createCell(0);
        cell.setCellValue(num + 1);

        // Line CODE
        cell = row.createCell(1);
        cell.setCellValue((String) dataMap.get(SDVTypeConstant.BOP_PROCESS_LINE_ITEM + SDVPropertyConstant.LINE_REV_CODE));

        // Line Revision
        cell = row.createCell(2);
        cell.setCellValue((String) dataMap.get(SDVTypeConstant.BOP_PROCESS_LINE_ITEM + SDVPropertyConstant.BL_ITEM_REV_ID));

        // 공정 CODE
        cell = row.createCell(3);
        cell.setCellValue((String) dataMap.get(SDVTypeConstant.BOP_PROCESS_LINE_ITEM + SDVPropertyConstant.LINE_REV_CODE) + "-" + dataMap.get(SDVTypeConstant.BOP_PROCESS_STATION_ITEM + SDVPropertyConstant.STATION_STATION_CODE));

        // 공정 Revision
        cell = row.createCell(4);
        cell.setCellValue((String) dataMap.get(SDVTypeConstant.BOP_PROCESS_STATION_ITEM + SDVPropertyConstant.BL_ITEM_REV_ID));

        // 공법 ID
        cell = row.createCell(5);
        cell.setCellValue((String) dataMap.get(SDVTypeConstant.BOP_PROCESS_BODY_OPERATION_ITEM + SDVPropertyConstant.BL_ITEM_ID));

        // 공법명 (KR)
        cell = row.createCell(6);
        cell.setCellValue((String) dataMap.get(SDVTypeConstant.BOP_PROCESS_BODY_OPERATION_ITEM + SDVPropertyConstant.OPERATION_REV_KOR_NAME));

        // 공법 REV
        cell = row.createCell(7);
        cell.setCellValue((String) dataMap.get(SDVTypeConstant.BOP_PROCESS_BODY_OPERATION_ITEM + SDVPropertyConstant.BL_ITEM_REV_ID));

        // 로봇 ID
        cell = row.createCell(8);
        cell.setCellValue((String) dataMap.get(SDVTypeConstant.PLANT_OPAREA_ITEM + SDVPropertyConstant.BL_ITEM_ID));

        // 건 ID
        cell = row.createCell(9);
        cell.setCellValue((String) dataMap.get(SDVTypeConstant.BOP_PROCESS_GUN_ITEM + SDVPropertyConstant.BL_ITEM_ID));

        // 용접점 ID (bl_occurence_name)
        cell = row.createCell(10);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.BL_OCCURRENCE_NAME));

        // 용접점 ID (ID 겸 겹수)
        cell = row.createCell(11);
        //cell.setCellValue((String) dataMap.get(SDVPropertyConstant.BL_ITEM_ID));
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.WELD_NUMBER_OF_SHEETS));

        // Option Code
        cell = row.createCell(12);
        cell.setCellValue((String) dataMap.get("optionDescription"));
    }

    /**
     * Excel Header 정보 출력
     * product code, shop or line - id, revision rule, revision rule 기준일, 옵션, Excel 출력 일시
     *
     * @method printHeaderInfo
     * @date 2013. 11. 18.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void printHeaderInfo(Workbook workbook, Sheet currentSheet, IDataMap dataMap) {
        // 2행
        Row row = currentSheet.getRow(1);
        Cell cell = row.getCell(0);
        cell.setCellValue(dataMap.getStringValue("productCode") + " - " +  dataMap.getStringValue("compID"));

        // 3행
        row = currentSheet.getRow(2);
        cell = row.getCell(0);
        cell.setCellValue(dataMap.getStringValue("revisionRule") + "(" + dataMap.getStringValue("revRuleStandardDate") + ")");

        cell = row.getCell(2);
        if(!dataMap.getStringValue("variantRule").equals("")) {
            cell.setCellValue(dataMap.getStringValue("variantRule"));
        }

        // 출력 일시
        Font font = workbook.createFont();
        font.setFontName("돋움");
        font.setBoldweight(Font.BOLDWEIGHT_BOLD);
        font.setFontHeightInPoints((short)11);

        XSSFCellStyle style = (XSSFCellStyle) workbook.createCellStyle();
        style.setFont(font);
        style.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);
        style.setAlignment(XSSFCellStyle.ALIGN_RIGHT);
        style.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
        style.setFillForegroundColor(new XSSFColor(new byte[]{0, (byte) 204, (byte) 255}));

        cell.setCellStyle(style);
    }

    /**
     * Border, Align 설정
     *
     * @method setBorderAndAlign
     * @date 2013. 11. 20.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void setBorderAndAlign(Workbook workbook, Sheet currentSheet) {
        // 스타일
        CellStyle style = workbook.createCellStyle();
        CellStyle style1 = workbook.createCellStyle();

        Font font = workbook.createFont();
        font.setFontName("돋움");
        font.setFontHeightInPoints((short)10);

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

        // 가운데 정렬 column
        int[] center_columnArray = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
        ArrayList<Integer> center_columnList = new ArrayList<Integer>();
        for(int i = 0; i < center_columnArray.length; i++) {
            center_columnList.add(center_columnArray[i]);
        }

        Row row;
        int lastRowNum = currentSheet.getLastRowNum();
        for(int i = dataStartIndex; i <= lastRowNum; i++) {
            row = currentSheet.getRow(i);
            for(int j = 0; j < CellCnt; j++) {
                if(center_columnList.contains(j)) {
                    row.getCell(j).setCellStyle(style1);
                } else {
                    row.getCell(j).setCellStyle(style);
                }
            }
        }
    }

}
