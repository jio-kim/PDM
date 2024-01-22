package com.symc.plm.me.sdv.excel.transformer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;

import com.symc.plm.me.common.SDVPropertyConstant;

public class ExportRobotSimulationResultExcelTransformer extends AbstractExcelTransformer {

    public ExportRobotSimulationResultExcelTransformer() {
        super();
    }

    @Override
    public void print(int mode, String templatePreference, String defaultFileName, IDataSet dataSet) throws Exception {
        // PreferenceName으로 템플릿 파일 가져오기
        templateFile = getTemplateFile(mode, templatePreference, defaultFileName);

        try {
            Workbook workbook = new XSSFWorkbook(new FileInputStream(templateFile));
            List<HashMap<String, Object>> dataList = getTableData(dataSet);

            if(dataList != null && dataList.size() > 0) {
                Sheet currentSheet = workbook.getSheetAt(0);
                // 첫 번째 제외한 모든 sheet 삭제
                removeSheet(workbook);
                printRow(workbook, currentSheet, dataList.get(0));
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
    @SuppressWarnings("unchecked")
    private void printRow(Workbook workbook, Sheet currentSheet, HashMap<String, Object> dataMap) {
        Row row = currentSheet.getRow(3);

        // 차종
        Cell cell = row.getCell(0);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.STATION_VEHICLE_CODE));

        // Line명
        cell = row.getCell(1);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.STATION_LINE));

        // 공정명
        cell = row.getCell(2);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.STATION_STATION_CODE));

        // Robot명
        cell = row.getCell(3);
        cell.setCellValue((String) dataMap.get("robotName"));

        // Gun Info
        List<String> gunList = (List<String>) dataMap.get("gunList");
        for(int i = 0; i < gunList.size(); i++) {
            // Gun 개수 =(같다) Sheet 개수
            if(i > 0) {
                Sheet sheet = workbook.cloneSheet(0);
                row = sheet.getRow(3);
            }

            // Gun No
            cell = row.getCell(4);
            cell.setCellValue(gunList.get(i));

            // Sheet Name
            workbook.setSheetName(i, dataMap.get("robotName") + "-" + gunList.get(i));
        }
    }

    /**
     * 첫 번째 제외한 모든 sheet 삭제
     *
     * @method removeSheet
     * @date 2014. 1. 14.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void removeSheet(Workbook workbook) {
        int numberOfSheets = workbook.getNumberOfSheets();
        for(int i = 1; i < numberOfSheets; i++) {
            workbook.removeSheetAt(1);
        }
    }

}
