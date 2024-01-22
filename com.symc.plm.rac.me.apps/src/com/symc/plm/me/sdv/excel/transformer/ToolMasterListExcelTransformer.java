package com.symc.plm.me.sdv.excel.transformer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.excel.common.ExcelTemplateHelper;


public class ToolMasterListExcelTransformer extends AbstractExcelTransformer {

    @SuppressWarnings("unused")
    private String operationType = "";
    private final int dataStartIndex = 5;
    private final int rowCnt = 0;
    private final int CellCnt = 19;
    private int rowSize;

    HSSFWorkbook workbook;
    HSSFCellStyle style = null;

    ArrayList<String> arrayList = new ArrayList<String>();

    public ToolMasterListExcelTransformer() {
        super();
    }

    @Override
    public void print(int mode, String templatePreference, String defaultFileName, IDataSet dataSet) {
        // PreferenceName으로 템플릿 파일 가져오기
        templateFile = getTemplateFile(mode, templatePreference, defaultFileName);

        try {
            Workbook workbook = new XSSFWorkbook(new FileInputStream(templateFile));
            Map<String, XSSFCellStyle> cellStyles = ExcelTemplateHelper.getCellStyles(workbook);
            List<HashMap<String, Object>> dataList = getTableData(dataSet);

            if(dataList != null) {
                Sheet currentSheet = workbook.getSheetAt(0);

                rowSize = dataList.size();

                for(int i = 0; i < rowSize; i++) {
                    printRow(currentSheet, i, dataList.get(i), cellStyles);
                    printBorder(currentSheet, cellStyles);
                }

                if (rowSize > rowCnt) {
                    printBorder(currentSheet, cellStyles);
                }

                IDataMap dataMap = dataSet.getDataMap("additionalInfo");
                prnitProductInfo(currentSheet, dataMap.getStringValue("productInfo"));
                prnitBOMStandardInfo(currentSheet, dataMap.getStringValue("bomStandardInfo"),
                                                    dataMap.getStringValue("variantRule"),dataMap.getStringValue("bomStandardInfo_date"));
                operationType = dataMap.getStringValue("operationType");

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
    private void printRow(Sheet currentSheet, int num, HashMap<String, Object> dataMap, Map<String, XSSFCellStyle> cellStyles) {
        Row row = currentSheet.createRow(this.dataStartIndex + num);
        // NO
        Cell cell = row.createCell(0);
        cell.setCellValue(num + 1);


        //공법(작업표준서)No.
        cell = row.createCell(1);
        cell.setCellValue((String)dataMap.get("operationID"));

        //공법 rev
        cell = row.createCell(2);
        cell.setCellValue((String)dataMap.get("operationRev"));

        //공구 Code
        cell = row.createCell(3);
        cell.setCellValue((String)dataMap.get(SDVPropertyConstant.BL_ITEM_ID));

        //공구 Name(English)
        cell = row.createCell(4);
        cell.setCellValue((String)dataMap.get(SDVPropertyConstant.BL_REV_OBJECT_NAME));

        //CAD
        cell = row.createCell(5);
        cell.setCellValue((String)dataMap.get("cad"));

        //JT
        cell = row.createCell(6);
        cell.setCellValue((String)dataMap.get("jt"));

        //CGR
        cell = row.createCell(7);
        cell.setCellValue((String)dataMap.get("cgr"));

        //기타
        cell = row.createCell(8);
        cell.setCellValue((String)dataMap.get("etc"));

        //Tech Spec
        cell = row.createCell(9);
        cell.setCellValue((String)dataMap.get("spec_code"));

        //TORQUE
        cell = row.createCell(10);
        cell.setCellValue((String)dataMap.get("torqueResult"));

        //수량
        cell = row.createCell(11);
        cell.setCellValue((String)dataMap.get("quantity"));

        //line
        cell = row.createCell(12);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.LINE_REV_CODE));

        //line rev
        cell = row.createCell(13);
        cell.setCellValue((String)dataMap.get(SDVTypeConstant.BOP_PROCESS_LINE_ITEM + SDVPropertyConstant.BL_ITEM_REV_ID));

        //공정
        cell = row.createCell(14);
        cell.setCellValue((String)dataMap.get(SDVPropertyConstant.STATION_REV_CODE));

        //공정 rev
        cell = row.createCell(15);
        cell.setCellValue((String)dataMap.get(SDVTypeConstant.BOP_PROCESS_STATION_ITEM + SDVPropertyConstant.BL_ITEM_REV_ID));

        //작업자code
        cell = row.createCell(16);
        cell.setCellValue((String)dataMap.get("worker_code")); //operation

        //공법명
        cell = row.createCell(17);
        cell.setCellValue((String)dataMap.get("operationName"));

        //Find No.
        cell = row.createCell(18);
        cell.setCellValue((Integer)dataMap.get("seq"));

        //담당자
        cell = row.createCell(19);
        cell.setCellValue((String)dataMap.get("name"));

    }

    /**
     * Product Code + Product ID
     * 리포트 2번째 행 데이터
     *
     * @method prnitProductInfo
     * @date 2013. 10. 31.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void prnitProductInfo(Sheet currentSheet, String productInfo) {
        Row row = currentSheet.getRow(1);
        Cell cell = row.getCell(0);
        cell.setCellValue(productInfo);
    }


    /**
     * BOM 정보(Revision Rule, 기준 날짜, 옵션, 출력 날짜)
     * 리포트 3번째 행 데이터
     *
     * @method prnitBOMStandardInfo
     * @date 2013. 10. 31.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void prnitBOMStandardInfo(Sheet currentSheet, String bomStandardInfo, String variantRule, String bomStandardInfo_date) {
        Row row = currentSheet.getRow(2);
        Cell cell = row.getCell(0);
        cell.setCellValue(bomStandardInfo);

        cell = row.getCell(2);
        cell.setCellValue(variantRule);

        cell = row.getCell(19);
        cell.setCellValue(bomStandardInfo_date);
    }

    // excel border(엑셀 보더 그리기)
    private void printBorder(Sheet currentSheet, Map<String, XSSFCellStyle> cellStyles) {
        int[] center_columnArray = new int[]{0, 1, 2, 5, 6, 7, 8, 11, 12, 13, 14, 15, 16, 18, 19};
        ArrayList<Integer> center_columnList = new ArrayList<Integer>();
        for(int i = 0; i < center_columnArray.length; i++) {
            center_columnList.add(center_columnArray[i]);
        }

        Row row;

        int lastRowNum = currentSheet.getLastRowNum();
        for (int i = dataStartIndex; i < lastRowNum+1; i++) {
            row = currentSheet.getRow(i);
            for (int j = 0; j <= CellCnt; j++) {
                if (center_columnList.contains(j)) {
//                    row.createCell(j);
                    row.getCell(j).setCellStyle(cellStyles.get("border_center"));
                }else{
                    row.getCell(j).setCellStyle(cellStyles.get("border"));
                }
            }
        }
    }
}
