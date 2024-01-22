package com.symc.plm.me.sdv.excel.transformer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;

import com.symc.plm.me.common.SDVLOVUtils;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

/**
 * [SR140723-010][20140717] shcho, m7_JPH 속성의 타입을 정수에서 부동 소수점으로 변경. 소수점포함5자리까지 입력가능. 
 * [SR150604-001][20150716] shcho, TCM 도장 라인 편성 Report 오류 - Sheet 추가시 발생하는 IndexOutOfBoundsException 오류 수정
 *
 */
public class PaintLineBalanceSheetExcelTransformer extends AbstractExcelTransformer {
    private final int printStartRow = 4;
    private final int printRowPerSheet = 51;

    private Registry registy;

    public PaintLineBalanceSheetExcelTransformer() {
        super();
        this.registy = Registry.getRegistry(this);
    }

    @Override
    public void print(int mode, String templatePreference, String defaultFileNameObject, IDataSet dataSet) throws TCException {
        // PreferenceName으로 템플릿 파일 가져오기
        
    	 templateFile = getTemplateFile(mode, templatePreference, defaultFileNameObject);
        try {
            List<HashMap<String, Object>> dataList = getTableData(dataSet);
            
//            if( dataList == null || dataList.size() < 1) {
//            	   MessageBox.post(AIFDesktop.getActiveDesktop().getShell(), "출력할 데이터가 없습니다.", "WARNING", MessageBox.WARNING);
//            	return;
//            }
           
            // 여기서 복사할 Sheet수를 정해서 복사호출
//            int totalSheetCount = dataList.size() / (printRowPerSheet + 1) + 1;
            int totalSheetCount = dataList.size() / printRowPerSheet + (dataList.size() % printRowPerSheet > 0 ? 1 : 0);
            if(totalSheetCount > 1) {
                Properties props = System.getProperties();
                String rootPath = props.getProperty("eclipse.launcher");
                rootPath = rootPath.substring(0, rootPath.lastIndexOf('\\') + 1);

                Runtime runTime = Runtime.getRuntime();
                try {
                    Process execProc = runTime.exec(rootPath + "ExcelUtil.exe  /copySheet file=" + templateFile.getAbsolutePath() + " count=" + (totalSheetCount - 1));
                    execProc.waitFor();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } 

            Workbook workbook = new XSSFWorkbook(new FileInputStream(templateFile));

            double totalWorkerCnt = 0.0;
            double totalWorkTime = 0.0;

            if(dataList != null) {
                Sheet currentSheet = workbook.getSheetAt(0);

                IDataMap dataMap = dataSet.getDataMap("AdditionalInfo");

                String productCode = dataMap.getStringValue(SDVPropertyConstant.SHOP_REV_PRODUCT_CODE);
                String shopCode = dataMap.getStringValue(SDVPropertyConstant.SHOP_REV_SHOP_CODE);

                // [SR140723-010][20140717] shcho, m7_JPH 속성의 타입을 정수에서 부동 소수점으로 변경. 소수점포함5자리까지 입력가능.
                //int shopJPH = 0;
                //if((Integer) dataMap.getIntValue(SDVPropertyConstant.SHOP_REV_JPH) != null) {
                //    shopJPH = dataMap.getIntValue(SDVPropertyConstant.SHOP_REV_JPH);
                //}
                double shopJPH = 0;
                if(dataMap.getValue(SDVPropertyConstant.SHOP_REV_JPH) != null) {
                    shopJPH = ((Double) dataMap.getValue(SDVPropertyConstant.SHOP_REV_JPH)).doubleValue();
                }

                int rowSize = dataList.size();
                
                for(int i = 0; i < totalSheetCount; i++) {
                    if(i > 0) {
                        currentSheet = workbook.getSheetAt(i);
                    }
                    
                    printHeader(currentSheet, productCode, shopCode);
                    printShopJPH(currentSheet, shopJPH);
                    printRevisionAndVariantRule(currentSheet, dataMap);
                    
                    // 액셀 리스트 부분 출력
                    int toPrintRow = (i + 1) == totalSheetCount ? (totalSheetCount == 1 ? rowSize : (rowSize % printRowPerSheet > 0 ? rowSize % printRowPerSheet : printRowPerSheet)) : printRowPerSheet;
                    for(int j = 0; j < toPrintRow; j++) {
                        HashMap<String, Object> data = dataList.get(j + (printRowPerSheet * i));
                        
                        printRow(currentSheet, j + printStartRow, j + (printRowPerSheet * i) + 1, data);

                        int workerCnt = (Integer) data.get(SDVPropertyConstant.PAINT_OPERATION_REV_WORKER_COUNT);
                        totalWorkerCnt += workerCnt;
                        //[SR140723-010][20140717] shcho, m7_JPH 속성의 타입을 정수에서 부동 소수점으로 변경. 소수점포함5자리까지 입력가능.
                        double lineJPH = (Double) data.get(SDVPropertyConstant.LINE_REV_JPH);
                        totalWorkTime += (Double) data.get(SDVPropertyConstant.ACTIVITY_WORK_TIME) * (lineJPH / shopJPH) * workerCnt;
                    }
                }

                workbook.getSheetAt(totalSheetCount - 1).getRow(63).getCell(9).setCellValue(totalWorkerCnt/shopJPH);
                workbook.getSheetAt(totalSheetCount - 1).getRow(60).getCell(8).setCellValue(totalWorkTime/(totalWorkerCnt * 3600 / shopJPH) * 100);
            }

            workbook.setForceFormulaRecalculation(true);

            FileOutputStream fos = new FileOutputStream(templateFile);
            workbook.write(fos);
            fos.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void printRevisionAndVariantRule(Sheet currentSheet, IDataMap dataMap) {
        Cell cell = currentSheet.getRow(58).getCell(1);
        cell.setCellValue(registy.getString("BOPRevisionRuleLabel.TEXT", "Revision Rule :") + dataMap.getStringValue("RevisionRule"));

        cell = currentSheet.getRow(59).getCell(1);
        String variant = dataMap.getStringValue("VariantRule");
        if(variant.isEmpty()) {
            variant = registy.getString("BOPVariantConditionNotConfigured", "Not Specified");
        }
        cell.setCellValue(registy.getString("BOPRevisionRuleLabel.TEXT", "Variant Condition :") + variant);
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
     * 액셀 파일에 리스트를 출력하는 함수
     *
     * @method printRow
     * @date 2013. 10. 29.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void printRow(Sheet sheet, int rowNum, int dataNumber, HashMap<String, Object> rowData) {
        Row currentRow = sheet.getRow(rowNum);

        // NO
        Cell cell = currentRow.getCell(0);
        cell.setCellValue(dataNumber);

        // 공정 ID
        cell = currentRow.getCell(1);
        cell.setCellValue((String)rowData.get(SDVPropertyConstant.BL_ITEM_ID));

        // 공정명
        cell = currentRow.getCell(2);
        cell.setCellValue((String)rowData.get(SDVPropertyConstant.BL_OBJECT_NAME));

        // 작업자
        cell = currentRow.getCell(3);
        cell.setCellValue((Integer)rowData.get(SDVPropertyConstant.PAINT_OPERATION_REV_WORKER_COUNT));

        // 작업시간
        cell = currentRow.getCell(5);
        cell.setCellValue((Double)rowData.get(SDVPropertyConstant.ACTIVITY_WORK_TIME));

        // JPH
        //[SR140723-010][20140717] shcho, m7_JPH 속성의 타입을 정수에서 부동 소수점으로 변경. 소수점포함5자리까지 입력가능.
        cell = currentRow.getCell(15);
        cell.setCellValue((Double)rowData.get(SDVPropertyConstant.LINE_REV_JPH));
    }

    //[SR140723-010][20140717] shcho, m7_JPH 속성의 타입을 정수에서 부동 소수점으로 변경. 소수점포함5자리까지 입력가능.
    private void printShopJPH(Sheet currentSheet, double jph) {
        Row row = currentSheet.getRow(63);
        Cell cell = row.getCell(2);
        cell.setCellValue(jph);
    }

    private void printHeader(Sheet currentSheet, String productCode, String shopCode) throws TCException {
        String header = productCode + " " + SDVLOVUtils.getLovValueDesciption(SDVTypeConstant.LOV_PAINT_SHOP_CODE, shopCode)
                + " " + registy.getString("PaintLineBalanceSheetTitle");
        Row row = currentSheet.getRow(1);
        Cell cell = row.createCell(1);
        cell.setCellStyle(row.getCell(0).getCellStyle());
        cell.setCellValue(header);
    }

}
