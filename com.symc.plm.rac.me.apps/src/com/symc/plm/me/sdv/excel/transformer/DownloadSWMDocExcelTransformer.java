/**
 * 
 */
package com.symc.plm.me.sdv.excel.transformer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.util.Registry;

/**
 * Class Name : DownloadSWMDocExcelTransformer
 * Class Description : 검색 결과를 표준작업요령서 관리대장 템플릿으로 Local영역에 다운
 * 
 * @date 2013. 12. 6.
 * 
 */
public class DownloadSWMDocExcelTransformer extends AbstractExcelTransformer {
    private Registry registry;
    private final int dataStartIndex = 7;
    private final int headerCellCount = 10;
    private int rowSize;
    private Row row;

    ArrayList<String> arrayList = new ArrayList<String>();

    public DownloadSWMDocExcelTransformer() {
        super();
        registry = Registry.getRegistry(this);
    }

    @Override
    public void print(int mode, String templatePreference, String defaultFileNameObject, IDataSet dataSet) {
        // PreferenceName으로 템플릿 파일 가져오기
        File templateFile = getTemplateFile(mode, templatePreference, defaultFileNameObject);
        try {
            Workbook workbook = new XSSFWorkbook(new FileInputStream(templateFile));
            List<HashMap<String, Object>> dataList = getTableData(dataSet);

            if (dataList != null) {
                Sheet currentSheet = workbook.getSheetAt(0);
                rowSize = dataList.size();

                for (int i = 0; i < rowSize; i++) {
                    printRow(currentSheet, i, dataList.get(i));
                }
                setBorderAndAlign(workbook, currentSheet);

                XSSFFormulaEvaluator.evaluateAllFormulaCells((XSSFWorkbook) workbook);
            }
            FileOutputStream fos = new FileOutputStream(templateFile);
            workbook.write(fos);
            fos.flush();
            fos.close();

            Display.getDefault().syncExec(new Runnable() {
                @Override
                public void run() {
                    MessageDialog.openInformation(AIFUtility.getActiveDesktop().getShell(), "Information", registry.getString("List working complete."));
                }
            });

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<HashMap<String, Object>> getTableData(IDataSet dataSet) {
        Collection<HashMap<String, Object>> data = null;

        IDataMap dataMap = dataSet.getDataMap("downloadList");
        if (dataMap != null) {
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

        // 관리번호
        Cell cell = row.createCell(0);
        cell.setCellValue((String) dataMap.get("item_id"));
        row.setHeight((short) 497);

        // 리비전
        cell = row.createCell(1);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.ITEM_REVISION_ID));

        // 작업명
        cell = row.createCell(2);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.ITEM_OBJECT_NAME));

        // 작성일
        cell = row.createCell(3);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.ITEM_CREATION_DATE));

        // 관련근거
        cell = row.createCell(4);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.ITEM_M7_REFERENCE_INFO));

        // 게시일
        cell = row.createCell(5);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.ITEM_DATE_RELEASED));

        // 폐기일
        cell = row.createCell(6);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.ITEM_M7_DISCARD_DATE));

        // 작성자
        cell = row.createCell(7);
        cell.setCellValue(((String) dataMap.get(SDVPropertyConstant.ITEM_OWNING_USER)).split(" ")[0]);

        // 구분
        cell = row.createCell(8);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.SWM_CATEGORY));

        // 승인
        cell = row.createCell(9);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.WORKFLOW_SIGNOFF));

        // 직
        cell = row.createCell(10);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.SWM_GROUP));

    }

    private void setBorderAndAlign(Workbook workbook, Sheet currentSheet) {

        Font font = workbook.createFont();
        font.setFontName("돋움");
        font.setFontHeightInPoints((short) 10);

        // 가운데 정렬
        XSSFCellStyle cellStyle1 = (XSSFCellStyle) workbook.createCellStyle();
        cellStyle1.setBorderTop(XSSFCellStyle.BORDER_THIN);
        cellStyle1.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        cellStyle1.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        cellStyle1.setBorderRight(XSSFCellStyle.BORDER_THIN);
        cellStyle1.setAlignment(XSSFCellStyle.ALIGN_CENTER);
        cellStyle1.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);
        cellStyle1.setFont(font);

        // 왼쪽 정렬
        XSSFCellStyle cellStyle2 = (XSSFCellStyle) workbook.createCellStyle();
        cellStyle2.setBorderTop(XSSFCellStyle.BORDER_THIN);
        cellStyle2.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        cellStyle2.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        cellStyle2.setBorderRight(XSSFCellStyle.BORDER_THIN);
        cellStyle2.setAlignment(XSSFCellStyle.ALIGN_LEFT);
        cellStyle2.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);
        cellStyle2.setWrapText(true);
        cellStyle2.setFont(font);

        for (int i = dataStartIndex; i < (dataStartIndex + rowSize); i++) {
            row = currentSheet.getRow(i);

            for (int j = 0; j <= headerCellCount; j++) {
                if (j != 2) {
                    row.getCell(j).setCellStyle(cellStyle1);
                } else {
                    row.getCell(j).setCellStyle(cellStyle2);
                }
            }
        }
    }

}
