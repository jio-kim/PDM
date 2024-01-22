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
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.teamcenter.rac.util.Registry;

public class EquipmemntMasterListExcelTransformer extends AbstractExcelTransformer {

    private final int HEADER_CELL_COUNT = 22;
    private final int DATA_START_ROW_INDEX = 5;

    private Registry registry;

    public EquipmemntMasterListExcelTransformer() {
        super();
        this.registry = Registry.getRegistry(this);
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
                int rowSize = dataList.size();
                for(int i = 0; i < rowSize; i++) {
                    printRow(currentSheet, i, dataList.get(i), dataMap.getStringValue("operationType"));
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
    private void printRow(Sheet currentSheet, int num, HashMap<String, Object> dataMap, String operationType) {
        Row row = currentSheet.createRow(this.DATA_START_ROW_INDEX + num);

        // NO
        Cell cell = row.createCell(0);
        cell.setCellValue(num + 1);

        // Shop Code
        cell = row.createCell(1);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.SHOP_REV_SHOP_CODE));

        // Line Code
        cell = row.createCell(2);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.LINE_REV_CODE));

        // Line Revision
        cell = row.createCell(3);
        cell.setCellValue((String) dataMap.get("line_revision"));

        // 공정 Code
        cell = row.createCell(4);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.STATION_STATION_CODE));

        // 공정 Revision
        cell = row.createCell(5);
        cell.setCellValue((String) dataMap.get("station_revision"));

        // 공법(작업표준서) NO
        cell = row.createCell(6);
        cell.setCellValue((String) dataMap.get("operation_id"));

        // 공법 Revision
        cell = row.createCell(7);
        cell.setCellValue((String) dataMap.get("operation_revision"));

        // 설비 Code
        cell = row.createCell(8);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.BL_ITEM_ID));

        // Type
        cell = row.createCell(9);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.EQUIP_MAIN_CLASS));

        // 설비명
        cell = row.createCell(10);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.BL_OBJECT_NAME));

        // CAD
        cell = row.createCell(11);
        cell.setCellValue((String) dataMap.get("cad"));

        // JT
        cell = row.createCell(12);
        cell.setCellValue((String) dataMap.get("jt"));

        // CGR
        cell = row.createCell(13);
        cell.setCellValue((String) dataMap.get("cgr"));

        // 기타
        cell = row.createCell(14);
        cell.setCellValue((String) dataMap.get("etc"));

        // 주요사양(규격)
        cell = row.createCell(15);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.EQUIP_SPEC_ENG));

        // 수량
        cell = row.createCell(16);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.BL_QUANTITY));

        // 생산능력
        cell = row.createCell(17);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.EQUIP_CAPACITY));

        // 용도
        cell = row.createCell(18);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.EQUIP_PURPOSE_ENG));

        // 설치년도
        cell = row.createCell(19);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.EQUIP_INSTALL_YEAR));

        // MAKER
        cell = row.createCell(20);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.EQUIP_MAKER));

        // 담당자
        cell = row.createCell(21);
        cell.setCellValue((String) dataMap.get("operation_owning_user"));
    }

    /**
     * Header 정보
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
        // product code, shop or line or station - id
        Row row = currentSheet.getRow(1);
        Cell cell = row.getCell(0);
        cell.setCellValue(dataMap.getStringValue("productCode") + " - " +  dataMap.getStringValue("compID"));

        // revision rule, revision rule 기준일
        row = currentSheet.getRow(2);
        cell = row.getCell(0);
        cell.setCellValue(dataMap.getStringValue("revisionRule") + "(" + dataMap.getStringValue("revRuleStandardDate") + ")");

        // 옵션
        cell = row.getCell(2);
        if(!dataMap.getStringValue("variantRule").isEmpty()) {
            cell.setCellValue(dataMap.getStringValue("variantRule"));
        }

        // 출력 일시
        Font font = workbook.createFont();
        font.setFontName("Arial");
        font.setBoldweight(Font.BOLDWEIGHT_BOLD);
        font.setFontHeightInPoints((short)11);

        XSSFCellStyle style = (XSSFCellStyle) workbook.createCellStyle();
        style.setFont(font);
        style.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);
        style.setAlignment(XSSFCellStyle.ALIGN_RIGHT);
        style.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
        style.setFillForegroundColor(new XSSFColor(new byte[]{0, (byte) 204, (byte) 255}));

        cell = row.getCell(21);
        cell.setCellValue(String.format(registry.getString("report.ExcelExportDate", "출력 일시 : %s"), dataMap.getStringValue("excelExportDate")));
        cell.setCellStyle(style);
    }

    /**
     * Contents Cell Style 설정
     *
     * @method setCellStyle
     * @date 2013. 11. 20.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void setCellStyleOfContents(Workbook workbook, Sheet currentSheet) {
        // 스타일
        CellStyle style = workbook.createCellStyle();
        CellStyle style1 = workbook.createCellStyle();

        Font font = workbook.createFont();
        font.setFontName("Arial");
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
        int[] center_columnArray = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 11, 12, 13, 14, 16, 17, 19, 21};
        ArrayList<Integer> center_columnList = new ArrayList<Integer>();
        for(int i = 0; i < center_columnArray.length; i++) {
            center_columnList.add(center_columnArray[i]);
        }

        int lastRowNum = currentSheet.getLastRowNum();
        for(int i = DATA_START_ROW_INDEX; i <= lastRowNum; i++) {
            Row row = currentSheet.getRow(i);
            for(int j = 0; j < HEADER_CELL_COUNT; j++) {
                if(center_columnList.contains(j)) {
                    row.getCell(j).setCellStyle(style1);
                } else {
                    row.getCell(j).setCellStyle(style);
                }
            }
        }
    }

}
