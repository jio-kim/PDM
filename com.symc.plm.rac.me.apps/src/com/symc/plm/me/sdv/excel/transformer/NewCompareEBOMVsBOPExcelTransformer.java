package com.symc.plm.me.sdv.excel.transformer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
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

import com.symc.plm.me.sdv.excel.common.ExcelTemplateHelper;
import com.symc.plm.me.utils.BundleUtil;
import com.teamcenter.rac.util.Registry;

/**
 * [20140507] 컬럼 추가(공법 Rev, 공법 Status)
 *
 * @author bykim
 *
 */
public class NewCompareEBOMVsBOPExcelTransformer extends AbstractExcelTransformer {

    private final int HEADER_CELL_COUNT = 29;
    private final int DATA_START_ROW_INDEX = 5;

    private Registry registry;

    public NewCompareEBOMVsBOPExcelTransformer() {
        this.registry = Registry.getRegistry(this);
    }

	@Override
    public void print(int mode, String templatePreference, String defaultFileName, IDataSet dataSet) throws Exception {
        // PreferenceName으로 템플릿 파일 가져오기
        templateFile = getTemplateFile(mode, templatePreference, defaultFileName);

        try {
            Workbook workbook = new XSSFWorkbook(new FileInputStream(templateFile));
            Sheet currentSheet = workbook.getSheetAt(0);
            int index = 0;
            
            IDataMap dataMap = dataSet.getDataMap("EndItemList");
            if(dataMap != null) {
            	
            	List<HashMap<String, Object>> endItemList = dataMap.getTableValue("EndItemListMap");
            	List<HashMap<String, Object>> assignedEndItemList = dataMap.getTableValue("AssignedEndItemListMap");
            	
            	printHeaderInfo(workbook, currentSheet, dataMap);
            	
            	if(endItemList != null) {
            		for(HashMap<String, Object> endItem : endItemList) {
            			String occUid = (String) endItem.get("OCC_PUID");
            			for(HashMap<String, Object> assignedEndItem : assignedEndItemList) {
            				String endItemOccUid = (String) assignedEndItem.get("MPRODUCT_OCC_PUID");
            				if(occUid.equals(endItemOccUid)) {
            					endItem.putAll(assignedEndItem);
            					assignedEndItemList.remove(assignedEndItem);
            					
            					break;
            				}
            			}
            			
            			printRow(currentSheet, index++, endItem);
            		}
            	}
            	
            	if(assignedEndItemList.size() > 0) {
            		for(HashMap<String, Object> assignedEndItem : assignedEndItemList) {
            			printRow(currentSheet, index++, assignedEndItem);
            		}
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
        cell.setCellValue(num + 1);

        // Proj
        cell = row.createCell(1);
        cell.setCellValue(BundleUtil.nullObjToString(dataMap.get("PROJECT")));

        // Function
        cell = row.createCell(2);
        cell.setCellValue(BundleUtil.nullObjToString(dataMap.get("FUNCTION_NO")));

        // SEQ
        cell = row.createCell(3);
        cell.setCellValue(BundleUtil.nullObjToString(dataMap.get("SEQ")));

        // Lev
        cell = row.createCell(4);
        cell.setCellValue(BundleUtil.nullObjToString(dataMap.get("BOM_LEVEL")));

        // Part No
        cell = row.createCell(5);
        cell.setCellValue(BundleUtil.nullObjToString(dataMap.get("PART_NO")));

        // Ver
        cell = row.createCell(6);
        cell.setCellValue(BundleUtil.nullObjToString(dataMap.get("VER")));

        // S/Mode
        cell = row.createCell(7);
        cell.setCellValue(BundleUtil.nullObjToString(dataMap.get("SUPPMODE")));

        // Part Name
        cell = row.createCell(8);
        cell.setCellValue(BundleUtil.nullObjToString(dataMap.get("PART_NAME")));

        // Option
        cell = row.createCell(9);
        cell.setCellValue(BundleUtil.nullObjToString(dataMap.get("OPTIONS")));

        // Pos Description
        cell = row.createCell(10);
        cell.setCellValue(BundleUtil.nullObjToString(dataMap.get("POST_DESC")));

        // CAT
        cell = row.createCell(11);
        cell.setCellValue(BundleUtil.nullObjToString(dataMap.get("U_CATEGORY")));

        // In ECO
        cell = row.createCell(12);
        cell.setCellValue(BundleUtil.nullObjToString(dataMap.get("IN_ECO")));

        // In ECO Released Date
        cell = row.createCell(13);
        cell.setCellValue(BundleUtil.nullObjToString(dataMap.get("IN_ECO_DATE")));

        // BP Date
        cell = row.createCell(14);
        cell.setCellValue(BundleUtil.nullObjToString(dataMap.get("OUT_DATE")));

        // E-BOM QTY
        cell = row.createCell(15);
        cell.setCellValue(BundleUtil.nullObjToString(dataMap.get("QTY")));

        // Shop
        cell = row.createCell(16);
        cell.setCellValue(BundleUtil.nullObjToString(dataMap.get("shop_code")));

        // Line
        cell = row.createCell(17);
        cell.setCellValue(BundleUtil.nullObjToString(dataMap.get("line_code")));

        // Line Rev
        cell = row.createCell(18);
        cell.setCellValue(BundleUtil.nullObjToString(dataMap.get("line_rev")));

        // 공정
        cell = row.createCell(19);
        cell.setCellValue(BundleUtil.nullObjToString(dataMap.get("station_code")));

        // 공정 Rev
        cell = row.createCell(20);
        cell.setCellValue(BundleUtil.nullObjToString(dataMap.get("station_rev")));

        // 공법
        cell = row.createCell(21);
        cell.setCellValue(BundleUtil.nullObjToString(dataMap.get("operation_id")));

        // 공법 Rev.
        cell = row.createCell(22);
        cell.setCellValue(BundleUtil.nullObjToString(dataMap.get("operation_rev")));

        // 공법 Status
        cell = row.createCell(23);
        cell.setCellValue(BundleUtil.nullObjToString(dataMap.get("operation_status")));

        // 공법명
        cell = row.createCell(24);
        cell.setCellValue(BundleUtil.nullObjToString(dataMap.get("operation_name")));

        // 공법사양
        cell = row.createCell(25);
        cell.setCellValue(BundleUtil.nullObjToString(dataMap.get("operation_spec")));

        // BOP 수량
        cell = row.createCell(26);
        cell.setCellValue(BundleUtil.nullObjToString(dataMap.get("endItem_quantity")));

        // In MECO
        cell = row.createCell(27);
        cell.setCellValue(BundleUtil.nullObjToString(dataMap.get("in_meco")));

        // In MECO Released Date
        cell = row.createCell(28);
        cell.setCellValue(BundleUtil.nullObjToString(dataMap.get("in_meco_released_date")));
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
    private void printHeaderInfo(Workbook workbook, Sheet currentSheet, IDataMap dataMap) {
        Row row = null;
        Cell cell = null;

        // Function
        String function = dataMap.getStringValue("FunctionIds");
        row = currentSheet.getRow(1);
        cell = row.getCell(0);
        cell.setCellValue("Function : " + function);

        // MProduct Revision Rule
        String mProductRevRule = dataMap.getStringValue("MProductRevRule");
        cell = row.getCell(3);
        cell.setCellValue("Revision Rule : " + mProductRevRule);

        // Shop
        String shop = dataMap.getStringValue("BOPId");
        row = currentSheet.getRow(2);
        cell = row.getCell(0);
        cell.setCellValue("Shop : " + shop);

        // BOP Revision Rule
        String bopRevRule = dataMap.getStringValue("BOPRevRule");
        cell = row.getCell(3);
        cell.setCellValue("Revision Rule : " + bopRevRule);

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
        cell = row.getCell(HEADER_CELL_COUNT - 1);
        cell.setCellValue(String.format(registry.getString("report.ExcelExportDate", "출력 일시 : %s"), ExcelTemplateHelper.getToday("yyyyMMdd")));
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

        // 가운데 정렬 column
        int[] center_columnArray = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 26, 27, 28 };
        ArrayList<Integer> center_columnList = new ArrayList<Integer>();
        for (int i = 0; i < center_columnArray.length; i++) {
            center_columnList.add(center_columnArray[i]);
        }

        int lastRowNum = currentSheet.getLastRowNum();
        for (int i = DATA_START_ROW_INDEX; i <= lastRowNum; i++) {
            Row row = currentSheet.getRow(i);
            if(row != null) {
	            for (int j = 0; j < HEADER_CELL_COUNT; j++) {
	                if (center_columnList.contains(j)) {
	                	if(row.getCell(j) != null) {
	                		row.getCell(j).setCellStyle(style1);                		
	                	}
	                } else {
	                	if(row.getCell(j) != null) {
	                		row.getCell(j).setCellStyle(style);
	                	}
	                }
	            }
            }
        }
    }

}
