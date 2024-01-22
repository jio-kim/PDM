package com.symc.plm.me.sdv.excel.transformer;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFShape;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.internal.util.BundleUtility;
import org.osgi.framework.Bundle;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;

import com.symc.plm.activator.Activator;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.excel.common.ExcelTemplateHelper;


public class KPCOPMasterListExcelTransformer extends AbstractExcelTransformer {

    private final int dataStartIndex = 8;
    private final int printDataStartIndex = 0;
    @SuppressWarnings("unused")
    private final int rowCnt = 20;
    private final int CellCnt = 11;
    private int rowSize;

    HSSFWorkbook workbook;
    HSSFCellStyle style = null;

    ArrayList<String> arrayList = new ArrayList<String>();

    public KPCOPMasterListExcelTransformer() {
        super();
    }

    @Override
    public void print(int mode, String templatePreference, String defaultFileName, IDataSet dataSet) {
        
        // PreferenceNameÀ¸·Î ÅÛÇÃ¸´ ÆÄÀÏ °¡Á®¿À±â
        templateFile = getTemplateFile(mode, templatePreference, defaultFileName);

        try {
            
            Workbook workbook = new XSSFWorkbook(new FileInputStream(templateFile));
            workbook.setForceFormulaRecalculation(true);
            Map<String, XSSFCellStyle> cellStyles = ExcelTemplateHelper.getCellStyles(workbook);
            List<HashMap<String, Object>> dataList = getTableData(dataSet);

            /***°ü¸®´ëÀå Sheet***/
            if(dataList != null) {
                Sheet currentSheet = workbook.getSheetAt(0);

                rowSize = dataList.size();

                for (int i = 0; i < rowSize; i++) {
                    printRow(currentSheet, i, dataList.get(i), cellStyles);
                }
                printBorder(currentSheet, cellStyles, workbook);
                
                IDataMap dataMap = dataSet.getDataMap("additionalInfo");
                prnitProductInfo(currentSheet, dataMap.getStringValue("productInfo"));
                prnitBOMStandardInfo(currentSheet, dataMap.getStringValue("bomStandardInfo"),
                        dataMap.getStringValue("variantRule"),dataMap.getStringValue("bomStandardInfo_date"));

                XSSFFormulaEvaluator.evaluateAllFormulaCells((XSSFWorkbook) workbook);
            }

            /***ÀÎ¼â¹° Sheet***/
            if(dataList != null) {
                Sheet currentSheet = workbook.getSheetAt(1);

                int rowSize = dataList.size();
                for(int i = 0; i < rowSize; i++) {
                	if(!((String)dataList.get(i).get("realControlPoint")).equals("") && (String)dataList.get(i).get("realControlPoint") != null) {
                		printSecSheet(currentSheet, i, dataList.get(i), cellStyles);
                	}
                }
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
     * ÇÑ Row¾¿ Ãâ·ÂÇÑ´Ù.
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

        Row row = currentSheet.createRow(dataStartIndex + num);
        row.setHeight((short) 0x240);

        // NO
        Cell cell = row.createCell(0);
        cell.setCellValue(num + 1);

        //LINE¸í
        cell = row.createCell(1);
        cell.setCellValue((String)dataMap.get(SDVPropertyConstant.LINE_REV_CODE));

        //LINE rev
        cell = row.createCell(2);
        cell.setCellValue((String)dataMap.get(SDVTypeConstant.BOP_PROCESS_LINE_ITEM + SDVPropertyConstant.BL_ITEM_REV_ID));
        
        //°øÁ¤
        cell = row.createCell(3);
        cell.setCellValue((String)dataMap.get(SDVPropertyConstant.OPERATION_REV_STATION_NO));

        //ÀÛ¾÷Ç¥ÁØ°ü¸®¹øÈ£
        cell = row.createCell(4);
        cell.setCellValue((String)dataMap.get(SDVPropertyConstant.BL_ITEM_ID));

        //ÀÛÇ¥ REV
        cell = row.createCell(5);
        cell.setCellValue((String)dataMap.get(SDVPropertyConstant.BL_ITEM_REV_ID));

         cell = row.createCell(6);
        cell.setCellValue((String)dataMap.get("controlPoint"));
        
        //°ü¸®±âÁØ
        cell = row.createCell(7);
        cell.setCellValue((String)dataMap.get("controlStd")); //x
        
        //°ü¸®¹æ¹ý(DEFAULT °ª)
        cell = row.createCell(8);
        cell.setCellValue((String)dataMap.get("controlSheet"));
        //¼³Á¤ÀÏ
        cell = row.createCell(9);
        cell.setCellValue((String)dataMap.get("afterDate"));

        //º¯°æÀÏ(°ø¶õ)
        cell = row.createCell(10);
        cell.setCellValue("");

        //ºñ°í(°ø¶õ)
        cell = row.createCell(11);
        cell.setCellValue((String)dataMap.get(SDVPropertyConstant.ACTIVITY_CONTROL_POINT));
    }

    /***ÀÎ¼â¹°*****************************************/
    private void printSecSheet(Sheet currentSheet, int num, HashMap<String, Object> dataMap, Map<String, XSSFCellStyle> cellStyles) {


        Row row = currentSheet.getRow(printDataStartIndex + num*3);
        if(row == null) {
        	row = currentSheet.createRow(printDataStartIndex + num*3);
        }
        
        /////////////////////////////////////////////////////////////////////////////////////////////
        /**
         * Á¶¸³ »ý»ê ±â¼úÆÀ Æ¯º°Æ¯¼º º¸¿Ï°ü·Ã ¼öÁ¤
         * KPC List ¼öÁ¤
         */
        ////////////////////////////////////////////////////////////////////////////////////////////
          Row deltaRow = currentSheet.getRow(1 + num*3);
          if(deltaRow == null ) {
        	  deltaRow =   currentSheet.createRow(1 + num*3);
          }
          String controlPoint = (String)dataMap.get("realControlPoint");
          Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);
	  	  URL fileURL = null;
//	  	  InputStream inputStream = null;
	  	  byte[] bytes = null;
	  	  CreationHelper helper = null;
	  	  Drawing drawing = null;
	  	  ClientAnchor anchor = null;
	  	  Picture pict = null;
	  	  
		  try {
		  	  if( controlPoint.startsWith("CT") || Pattern.matches("^[°¡-ÆR]*$", controlPoint)) {
		  		  fileURL = BundleUtility.find(bundle, "icons/delta_c_large.png");
		  		  InputStream inputStream = fileURL.openStream();
		  		  if( inputStream == null) {
		  			  System.out.println("CT ¿¡·¯=======================>");
		  		  }
		  		  bytes = IOUtils.toByteArray(inputStream);
		  		  int pictureIdx = currentSheet.getWorkbook().addPicture(bytes, Workbook.PICTURE_TYPE_PNG);
		  		  helper = currentSheet.getWorkbook().getCreationHelper();
		  		  drawing = currentSheet.createDrawingPatriarch();
		  		  anchor = helper.createClientAnchor();
		  		  anchor.setCol1(0);
		  		  anchor.setRow1(deltaRow.getRowNum());
//		  		  anchor.setRow2(deltaRow.getRowNum() + 1);
//		  		  Cell deltaCell = deltaRow.getCell(0);
//		  		  deltaCell.getCellStyle();
//		  		  anchor.setRow1(deltaRow.getRowNum());
		  		  pict = drawing.createPicture(anchor, pictureIdx);
		  		  ClientAnchor pictAnchor =  pict.getPreferredSize();
		  		  pict.resize(0.9);
		  		  inputStream.close();
		  		  
		  	  } else if (controlPoint.startsWith("RE")) {
		  		  fileURL = BundleUtility.find(bundle, "icons/delta_r_large.png");
		  		InputStream inputStream = fileURL.openStream();
		  		  bytes = IOUtils.toByteArray(inputStream);
		  		  int pictureIdx = currentSheet.getWorkbook().addPicture(bytes, Workbook.PICTURE_TYPE_PNG);
		  		if( inputStream == null) {
		  			  System.out.println("RE ¿¡·¯=======================>");
		  		  }
		  		  helper = currentSheet.getWorkbook().getCreationHelper();
		  		  drawing = currentSheet.createDrawingPatriarch();
		  		  anchor = helper.createClientAnchor();
		  		  anchor.setCol1(0);
		  		  anchor.setRow1(deltaRow.getRowNum());
//		  		  anchor.setCol2(1);
//		  		  anchor.setRow2(deltaRow.getRowNum() + 1);
		  		  pict = drawing.createPicture(anchor, pictureIdx);
		  		  pict.resize(0.9);
		  		inputStream.close();
		  		  
		  	  } 
		  	  
		  }catch(Exception e) {
	  		  e.printStackTrace();
	  	  } 
//		  finally {
//	  		  try {
//	  			  inputStream.close();
//	  		  } catch(Exception e) {
//	  			  e.printStackTrace();
//	  		  }
//	  	  }
  		  
        /////////////////////////////////////////////////////////////////////////////////////////////
        

        //Project Code
        Cell cell = row.getCell(1);
        if(cell == null) {
        	cell = row.createCell(1);
        }
        cell.setCellValue((String)dataMap.get("projectCode"));

        //°ü¸®Á¡
        cell = row.getCell(2);
        if(cell == null) {
        	cell = row.createCell(2);
        }
        cell.setCellValue((String)dataMap.get("controlPoint"));

        //ÀÛ¾÷³»¿ë
        Row rowNum = currentSheet.getRow(printDataStartIndex + (num*3)+1);
        if(rowNum == null) {
        	rowNum = currentSheet.createRow(printDataStartIndex + (num*3)+1);
        }
        Cell sndCell = rowNum.getCell(1);
        if(sndCell == null) {
        	sndCell = rowNum.createCell(1);
        }
        sndCell.setCellValue((String)dataMap.get(SDVPropertyConstant.BL_OBJECT_NAME));

        //°ü¸®±âÁØ
        Row rowNum2 = currentSheet.getRow(printDataStartIndex + (num*3)+2);
        if(rowNum2 == null) {
        	rowNum2 = currentSheet.createRow(printDataStartIndex + (num*3)+2);
        }
        Cell trdCell = rowNum2.getCell(1);
        if(trdCell == null) {
        	trdCell = rowNum2.createCell(1);
        }
        trdCell.setCellValue((String)dataMap.get("controlStd"));

    }

    /**
     * Product Code + Product ID
     * ¸®Æ÷Æ® 2¹øÂ° Çà µ¥ÀÌÅÍ
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
     * BOM Á¤º¸(Revision Rule, ±âÁØ ³¯Â¥, ¿É¼Ç, Ãâ·Â ³¯Â¥)
     * ¸®Æ÷Æ® 3¹øÂ° Çà µ¥ÀÌÅÍ
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

        cell = row.getCell(11);
        cell.setCellValue(bomStandardInfo_date);
    }

    // excel border(¿¢¼¿ º¸´õ ±×¸®±â)
    private void printBorder(Sheet currentSheet, Map<String, XSSFCellStyle> cellStyles, Workbook workbook) {
        
        int[] center_columnArray = new int[]{0, 1, 2, 3, 4, 5, 8, 9, 10};
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
                    row.getCell(j).setCellStyle(cellStyles.get("border_center"));
                }else{
                    row.getCell(j).setCellStyle(cellStyles.get("border"));
                }
            }
        }
    }
}
