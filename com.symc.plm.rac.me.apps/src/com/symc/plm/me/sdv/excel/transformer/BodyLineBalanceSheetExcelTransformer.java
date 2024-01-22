/**
 *
 */
package com.symc.plm.me.sdv.excel.transformer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.sdv.operation.report.LineBalanceSheet4BodyOperation;
import com.teamcenter.rac.util.Registry;

/**
 * Class Name : BodyLineBalanceSheetExcelTransformer
 * Class Description :
 * [SR140723-010][20140717] shcho, m7_JPH 속성의 타입을 정수에서 부동 소수점으로 변경. 소수점포함5자리까지 입력가능.
 * 
 * @date 2013. 10. 29.
 *
 */
public class BodyLineBalanceSheetExcelTransformer extends AbstractExcelTransformer {
    int printRowPerSheet = 34;
    int printStartRow = 4;
    Registry registry;

    public BodyLineBalanceSheetExcelTransformer() {
        super();
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

        // No.
        currentRow.getCell(0).setCellValue(dataNumber);
        // 공정코드
        currentRow.getCell(1).setCellValue((String) rowData.get(SDVPropertyConstant.BL_ITEM_ID));
        // 공법 명
        currentRow.getCell(2).setCellValue((String) rowData.get(SDVPropertyConstant.BL_OBJECT_NAME));
        // 작업자 수
        currentRow.getCell(4).setCellValue((String) rowData.get(registry.getString("BODYOperationWorkerCount.ATTR.NAME", "WorkerCount")));
        // 비고
        currentRow.getCell(15).setCellValue((String) rowData.get(registry.getString("BODYPrintETC.ATTR.NAME", "LineJPHOfETC")));
        // 작업자 정미시간
        currentRow.getCell(19).setCellValue((Double) rowData.get(registry.getString("BODYUserWorkTime.ATTR.NAME", "UserWorkTime")));
        // 불가피 대기시간
        currentRow.getCell(20).setCellValue((Double) rowData.get(registry.getString("BODYUserWaitTime.ATTR.NAME", "UserWaitTime")));
        // 라인 JPH
        // [SR140723-010][20140717] shcho, m7_JPH 속성의 타입을 정수에서 부동 소수점으로 변경. 소수점포함5자리까지 입력가능.
        currentRow.getCell(21).setCellValue((Double) rowData.get(SDVPropertyConstant.LINE_REV_JPH));
    }

    /**
     * 액셀 시트 복사 함수
     *
     * @method copySheet
     * @date 2013. 10. 29.
     * @param
     * @return Sheet
     * @exception
     * @throws
     * @see
     */
//    private Sheet copySheet(Workbook workbook) {
//        return null;
//    }

    /* (non-Javadoc)
     * @see org.sdv.core.common.IExcelTransformer#print(int, java.lang.String, java.lang.String, java.lang.Object)
     */
    @Override
    public void print(int mode, String templatePreference, String defaultFileName, IDataSet dataSet) throws Exception {
        registry = Registry.getRegistry(LineBalanceSheet4BodyOperation.class);

        // PreferenceName으로 템플릿 파일 가져오기
        templateFile = getTemplateFile(mode, templatePreference, defaultFileName);
        try {
            // 액셀에 출력할 리스트를 파라메타에서 가져온다.
            List<HashMap<String, Object>> dataList = getTableData(dataSet);
        	// 여기서 복사할 Sheet수를 정해서 복사호출
        	int totalSheetCount = dataList.size() / printRowPerSheet + (dataList.size() % printRowPerSheet > 0 ? 1 : 0);
        	if (totalSheetCount > 1)
        	{
        		Properties props = System.getProperties();
        		String rootPath = props.getProperty("eclipse.launcher");
        		rootPath = rootPath.substring(0, rootPath.lastIndexOf('\\') + 1);

        		Runtime runTime = Runtime.getRuntime();
        		try
        		{
        			Process execProc = runTime.exec(rootPath + "ExcelUtil.exe  /copySheet file=" + templateFile.getAbsolutePath() + " count=" + (totalSheetCount - 1));
        			execProc.waitFor();
        		}
        		catch (Exception ex)
        		{
        			
        		}
        	}

        	Workbook workbook = new XSSFWorkbook(new FileInputStream(templateFile));
            if(dataList != null) {
                Sheet currentSheet = workbook.getSheetAt(0);

                // 액셀 시트의 타이틀 부분 출력
                IDataMap sheetHeadMap = dataSet.getDataMap(registry.getString("BODYIDataHeadName"));
                printHeadInfo(currentSheet, sheetHeadMap, workbook, 0);

                int rowSize = dataList.size();
                for(int i = 0; i < totalSheetCount; i++) {
                    if(i > 0) {
                        currentSheet = workbook.getSheetAt(i);//copySheet(workbook);
                        printHeadInfo(currentSheet, sheetHeadMap, workbook, i);
                    }

                    // 액셀 리스트 부분 출력
                    int toPrintRow = (i + 1) == totalSheetCount ? (totalSheetCount == 1 ? rowSize : (rowSize % printRowPerSheet > 0 ? rowSize % printRowPerSheet : printRowPerSheet)) : printRowPerSheet;
                    for(int j = 0; j < toPrintRow; j++) {
                        printRow(currentSheet, j + printStartRow, j + (printRowPerSheet * i) + 1, dataList.get(j + (printRowPerSheet * i)));

                        printTailer(workbook, currentSheet, totalSheetCount);
                    }
                }
            }

            // 계산식이 있는 내용 전부 재 계산 명령
            workbook.setForceFormulaRecalculation(true);

            // 액셀 파일 저장
            FileOutputStream fos = new FileOutputStream(templateFile);
            workbook.write(fos);
            fos.flush();
            fos.close();

            // 액셀 파일 열기
//            String command = "cmd /C " + templateFile.getPath() + " \"" + templateFile.getAbsolutePath() + "\"";
//            Runtime.getRuntime().exec(command);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw e;
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 각 액셀의 총합에 대해 지정하는 부분
     * 
     * @param workbook
     * @param currentSheet
     * @param totalSheetCount
     */
    private void printTailer(Workbook workbook, Sheet currentSheet, int totalSheetCount) {
		
	}

	/**
     * IDataSet(파라메타)에서 내용에 해당하는 부분을 리턴하는 함수
     *
     * @method getTableData
     * @date 2013. 10. 30.
     * @param
     * @return List<HashMap<String,Object>>
     * @exception
     * @throws
     * @see
     */
    private List<HashMap<String, Object>> getTableData(IDataSet dataSet) {
        Collection<HashMap<String, Object>> data = null;

        IDataMap dataMap = dataSet.getDataMap(registry.getString("BODYIDataBodyName"));
        if(dataMap != null) {
            data = dataMap.getTableValue(registry.getString("BODYIDataBodyName"));
        }

        return (List<HashMap<String, Object>>) data;
    }

    /**
     * 액셀의 헤드부분에 해당하는 내용 출력 함수
     *
     * @method printHeadInfo
     * @date 2013. 10. 30.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void printHeadInfo(Sheet currentSheet, IDataMap dataMap, Workbook workbook, int sheetIndex) throws Exception {
        // SHOP의 JPH
        //[SR140723-010][20140717] shcho, m7_JPH 속성의 타입을 정수에서 부동 소수점으로 변경. 소수점포함5자리까지 입력가능.
        // int shop_jph = ((Integer) dataMap.getValue(SDVPropertyConstant.SHOP_REV_JPH)).intValue();
        double shop_jph = ((Double) dataMap.getValue(SDVPropertyConstant.SHOP_REV_JPH)).doubleValue();
        // SHOP의 Product 코드
        String product_code = dataMap.getStringValue(SDVPropertyConstant.SHOP_REV_PRODUCT_CODE);

        // Shop의 JPH값 설정
        currentSheet.getRow(48).getCell(2).setCellValue(shop_jph);

        // 제목의 Product Code 변경
        String title_name = currentSheet.getRow(0).getCell(0).getStringCellValue();
        int prod_code_start_index = title_name.indexOf(' ') + 1;
        int prod_code_end_index = title_name.indexOf(' ', prod_code_start_index + 1);
        String title_product_code = title_name.substring(prod_code_start_index, prod_code_end_index);
        title_name = title_name.replace(title_product_code, product_code);

        // 제목의 Shop JPH값 변경
        int jph_end_index = title_name.lastIndexOf("JPH");
        int jph_start_index = title_name.lastIndexOf('(') + 1;
        String title_name_JPH = title_name.substring(jph_start_index, jph_end_index);
        title_name = title_name.replace(title_name_JPH, String.valueOf(shop_jph));

        currentSheet.getRow(0).getCell(0).setCellValue(title_name);

        // 출력일 설정
        DateFormat sd_format = new SimpleDateFormat("yy.MM.dd");
        Date now_date = new Date();
        String date_string = sd_format.format(now_date);
        currentSheet.getRow(1).getCell(15).setCellValue(date_string);

        // Sheet page 설정
        currentSheet.getRow(50).getCell(15).setCellValue((sheetIndex + 1) + "/" + workbook.getNumberOfSheets() + " Page");

        // Total 값 설정
        if (sheetIndex + 1 == workbook.getNumberOfSheets())
        {
        	String totalCalcString = "";
        	for (int i = 0; i < sheetIndex; i++)
        	{
    			totalCalcString += "'" + workbook.getSheetName(i) + "'!" + "#PLACE# + ";
        	}
        	totalCalcString += "#PLACE#";

        	// 작업자 총합
        	currentSheet.getRow(38).getCell(4).setCellFormula(totalCalcString.replaceAll("#PLACE#", "S39"));
        	// 작업자 정미시간 총합
        	currentSheet.getRow(38).getCell(5).setCellFormula(totalCalcString.replaceAll("#PLACE#", "T39"));
        	// 작업자 작업시간 총합
        	currentSheet.getRow(38).getCell(7).setCellFormula(totalCalcString.replaceAll("#PLACE#", "V39"));
        	// 총 작업시간 총합
        	currentSheet.getRow(38).getCell(8).setCellFormula(totalCalcString.replaceAll("#PLACE#", "W39"));
        }
    }

}
