/**
 * 
 */
package com.symc.plm.me.sdv.operation.resource;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;

import com.teamcenter.rac.aif.AIFDesktop;

/**
 * [20140430][SR140507-042] shcho, 부자재 검색 결과 Excel로 내보내기 기능 추가에 따른 Class 수정
 * 
 * @author shcho
 * 
 */
public class ExportExcelOperation {

    public static final String FILE_DEFAULT_NAME = "Resource_Search_Result_List";

//    private StringBuilder strData;              // [20140430][SR140507-042]
    private List<List<String>> tableValue;  // [20140430][SR140507-042]
    private String highlightKey;                // [20140430][SR140507-042]
    private int highlightPosition;              // [20140430][SR140507-042]

    // 20201118 seho table 데이터를 StringBuilder로 가져오던것을 리스트로 받아 처리.... 구분자를 사용하는건...xxxxx
    public ExportExcelOperation(List<List<String>> _allDataList) {
    	tableValue = _allDataList;
    }

    // [20140430][SR140507-042]
    public ExportExcelOperation(List<List<String>> rowValueList, String highlightKey, int highlightPosition) {
        this.tableValue = rowValueList;
        this.highlightKey = highlightKey;
        this.highlightPosition = highlightPosition;
    }

    @SuppressWarnings("unused")
    public void executeOperation() throws Exception {

        // 저장될 파일 경로
        String fileName = openFileDialog(FILE_DEFAULT_NAME, "xlsx");

        if (fileName != null) {
            File file = new File(fileName);

            if (file.exists()) {
                org.eclipse.swt.widgets.MessageBox confirmBox = new org.eclipse.swt.widgets.MessageBox(AIFDesktop.getActiveDesktop().getShell(), SWT.OK | SWT.CANCEL | SWT.ICON_INFORMATION);
                confirmBox.setMessage("A file named " + file.getName() + " already exists. Are you sure you want to overwrite it?");

                if (confirmBox.open() != SWT.OK) {
                    return;
                }
            }

            // 엑셀 내용 작성
            XSSFWorkbook workbook = createExcel();

            // 엑셀 파일 생성
            FileOutputStream fileOutput = new FileOutputStream(file);
            workbook.write(fileOutput);
            fileOutput.close();

            /* 엑셀 파일 열기 */
            Runtime runtime = Runtime.getRuntime();
            String strCmd = "cmd /c \"" + file.getAbsolutePath();
            Process p = runtime.exec(strCmd);
        }
    }

    /**
     * Excel 내용 작성 함수
     * 
     * @return
     */
    public XSSFWorkbook createExcel() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet();

        // Font 설정
        XSSFFont font = workbook.createFont();
        font.setFontName(HSSFFont.FONT_ARIAL);

        // 제목의 스타일 지정
        XSSFCellStyle titlestyle = workbook.createCellStyle();
        titlestyle.setFillForegroundColor(HSSFColor.SKY_BLUE.index);
        titlestyle.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
        titlestyle.setAlignment(XSSFCellStyle.ALIGN_CENTER);
        titlestyle.setBorderLeft(BorderStyle.THIN);
        titlestyle.setBorderRight(BorderStyle.THIN);
        titlestyle.setBorderTop(BorderStyle.THIN);
        titlestyle.setBorderBottom(BorderStyle.DOUBLE);
        titlestyle.setFont(font);


        // Original 소스 ([20140430][SR140507-042] shcho, 부자재 검색 결과 Excel로 내보내기 기능 추가에 따른 Class 수정 전)
        // int headColumnCount = 0;
        // String arrRowValue[] = strData.toString().split("\n");
        // for (int i = 0; i < arrRowValue.length; i++) {
        // XSSFRow row = sheet.createRow(i);
        //
        // String arrCellValue[] = arrRowValue[i].split("%%");
        // int columnCount = arrCellValue.length;
        // if (i == 0)
        // headColumnCount = columnCount;
        // for (int j = 0; j < headColumnCount; j++) {
        // XSSFCell cell = row.createCell(j);
        // cell.setCellValue((j >= columnCount) ? "" : arrCellValue[j]);
        //
        // // 스타일 적용
        // if (i == 0) {
        // cell.setCellStyle(titlestyle);
        // } else {
        // cell.setCellStyle(style);
        // }
        // }
        // }

        // [20140430][SR140507-042] shcho, 부자재 검색 결과 Excel로 내보내기 기능 추가에 따른 소스 변경
        // 20201118 seho 이부분이 필요없음.
//        if (tableValue == null) {
//            tableValue = setDataToArrayList(allDataList);
//        }

        int rowCount = tableValue.size();
        int headColumnCount = 0;
        for (int i = 0; i < rowCount; i++) {
            XSSFRow row = sheet.createRow(i);

            List<String> rowValue = tableValue.get(i);
            int columnCount = rowValue.size();
            if (i == 0)
                headColumnCount = columnCount;

            for (int j = 0; j < headColumnCount; j++) {
                XSSFCell cell = row.createCell(j);
                String columnValue = (j >= columnCount) ? "" : rowValue.get(j);
                cell.setCellValue(columnValue);

                // 스타일 적용
                if (i == 0) {
                    cell.setCellStyle(titlestyle);
                } else {
                    // 추가 시작//
                    // [20140430][SR140507-042] shcho, TC에 기등록된 부자재 회색으로 강조 표시
                    if (ishighlight(rowValue)) {
                        cell.setCellStyle(setCellStyle(workbook, font, HSSFColor.GREY_25_PERCENT.index));
                    } else {
                        cell.setCellStyle(setCellStyle(workbook, font, HSSFColor.WHITE.index));
                    }
                    // 추가 종료//
                }
            }
        }

        // 셀 크기 자동 지정
        for (int i = 0; i <= headColumnCount; i++) {
            sheet.autoSizeColumn(i);
        }

        return workbook;
    }

    /**
     * Cell에 Style 지정 함수
     * 
     * [20140430][SR140507-042] shcho, 함수신규생성
     * 
     * @param workbook
     * @param font
     * @param colorIndex
     * @return XSSFCellStyle
     */
    public XSSFCellStyle setCellStyle(XSSFWorkbook workbook, XSSFFont font, short colorIndex) {
        // 내용 스타일 지정
        XSSFCellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(colorIndex);
        style.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND); // [20140430][SR140507-042] shcho, TC에 기등록된 부자재 회색으로 강조 표시
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setFont(font);
        return style;
    }

    /**
     * Excel Row에 강조표시(배경색) 할때 기준이 되는 키 값 검증 함수 (highlightPosition셀의 값이 highlightKey와 일치하는지 비교)
     * 
     * [20140430][SR140507-042] shcho, 함수신규생성
     * 
     * @param arrCellValue
     * @return boolean
     */
    public boolean ishighlight(List<String> arrCellValue) {
        if (highlightKey != null && highlightKey.length() > 0) {

            // System.out.println(highlightPosition + " >> " + arrCellValue.length + "  >> " + arrCellValue);
            if (highlightKey.equals(arrCellValue.get(highlightPosition))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 저장할 파일 위치 지정 함수
     * 
     * @return String
     */
    public String openFileDialog(final String defaultFileName, final String extention) {
        FileDialog fileDialog = new FileDialog(AIFDesktop.getActiveDesktop().getShell(), SWT.SAVE);
        fileDialog.setFileName(defaultFileName);
        fileDialog.setFilterExtensions(new String[] { "*." + extention });
        String selectedFile = fileDialog.open();

        return selectedFile;
    }

    // 20201118 seho 구분자를 사용하는건 좋지 못함.  xx xxxxxx
//    /**
//     * StringBuilder타입으로 존재하는 Data를 ArrayList<ArrayList<String>>타입으로 변환
//     * 
//     * [20140430][SR140507-042] shcho, 함수신규생성
//     * 
//     * @param strData
//     * @return ArrayList<ArrayList<String>>
//     */
//    public List<List<String>> setDataToArrayList(StringBuilder strData) {
//        List<List<String>> arrListRowValues = new ArrayList<List<String>>();
//        List<String> arrListColumnValues = new ArrayList<String>();
//
//        String arrRowValue[] = strData.toString().split("\n");
//        for (int i = 0; i < arrRowValue.length; i++) {
//            String arrCellValue[] = arrRowValue[i].split("%%");
//            arrListColumnValues = Arrays.asList(arrCellValue);
//            arrListRowValues.add(arrListColumnValues);
//        }
//        return arrListRowValues;
//    }

}
