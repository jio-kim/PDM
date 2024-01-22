package com.symc.plm.me.sdv.excel.transformer;

import java.io.FileInputStream;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;

import com.ssangyong.rac.kernel.SYMCBOPEditData;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.teamcenter.rac.kernel.TCComponentPerson;

/**
 * [SR140611-055][20140609] byKim
 * ① MECO Report에 작성자 승인요청일자 누락
 * ② MECO EPL에 내용 유무 상관없이 Old/New에 No. 모두 채워줄 것, No. 필드 다음에 Old/New 표시하는 필드 추가 필요
 * ③ MECO Report 글꼴 및 크기 변경 의뢰
 */
public class MECOExcelTransformer extends AbstractExcelTransformer {

    private final int HEADER_CELL_COUNT = 15;
    private final int DATA_START_ROW_INDEX = 5;
    private Map<String, XSSFCellStyle> styleMap = null;

    public MECOExcelTransformer() {

    }

    @Override
    public void print(int mode, String templatePreference, String defaultFileName, IDataSet dataSet) throws Exception {
        // PreferenceName으로 템플릿 파일 가져오기
        templateFile = getTemplateFile(mode, templatePreference, defaultFileName);

        try {
            Workbook workbook = new XSSFWorkbook(new FileInputStream(templateFile));
            Sheet currentSheet = null;

            // MECO_A
            currentSheet = workbook.getSheetAt(0);
            printMECOInfo(currentSheet, dataSet);

            // MECO_EPL
            currentSheet = workbook.getSheetAt(1);
            printMECOEPLList(currentSheet, dataSet);

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
     * MECO A MECO Info
     *
     * @method printMECOInfo
     * @date 2014. 2. 13.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void printMECOInfo(Sheet currentSheet, IDataSet dataSet) {
        IDataMap mecoInfoMap = dataSet.getDataMap("mecoInfo");
        IDataMap signatureInfoMap = dataSet.getDataMap("signatureInfo");
        IDataMap additionalInfoMap = dataSet.getDataMap("additionalInfo");

        Row row = null;
        Cell cell = null;

        // Print Date
        row = currentSheet.getRow(1);
        cell = row.getCell(7);
        cell.setCellValue(additionalInfoMap.getStringValue("print_date"));

        // MECO Date
        row = currentSheet.getRow(1);
        cell = row.getCell(6);
        cell.setCellValue(mecoInfoMap.getStringValue(SDVPropertyConstant.ITEM_DATE_RELEASED));

        // MECO Number
        row = currentSheet.getRow(3);
        cell = row.getCell(2);
        cell.setCellValue(mecoInfoMap.getStringValue(SDVPropertyConstant.ITEM_ITEM_ID));

        // MECO Dept
        row = currentSheet.getRow(3);
        cell = row.getCell(4);
        cell.setCellValue(mecoInfoMap.getStringValue(TCComponentPerson.PROP_PA6));

        // 차종
        row = currentSheet.getRow(5);
        cell = row.getCell(2);
        cell.setCellValue(mecoInfoMap.getStringValue(SDVPropertyConstant.MECO_PROJECT));

        // Status
        row = currentSheet.getRow(6);
        cell = row.getCell(2);
        cell.setCellValue(mecoInfoMap.getStringValue("m7_MECO_MATURITY"));

        // Eff.Date
        row = currentSheet.getRow(7);
        cell = row.getCell(2);
        cell.setCellValue(mecoInfoMap.getStringValue(SDVPropertyConstant.MECO_EFFECT_DATE));

        // Eff.Event
        row = currentSheet.getRow(7);
        cell = row.getCell(4);
        cell.setCellValue(mecoInfoMap.getStringValue(SDVPropertyConstant.MECO_EFFECT_EVENT));

        // Change Reason
        row = currentSheet.getRow(8);
        cell = row.getCell(2);
        cell.setCellValue(mecoInfoMap.getStringValue(SDVPropertyConstant.MECO_CHANGE_REASON));

        // Change Description
        row = currentSheet.getRow(9);
        cell = row.getCell(2);
        cell.setCellValue(mecoInfoMap.getStringValue(SDVPropertyConstant.ITEM_OBJECT_DESC));

        // Signature
        printSignatureInfo(currentSheet, signatureInfoMap);
    }

    /**
     * MECO A Signature Info
     * [SR140611-055][20140609] byKim ① MECO Report에 작성자 승인요청일자 누락
     *
     * @method printSignatureInfo
     * @date 2014. 2. 13.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void printSignatureInfo(Sheet currentSheet, IDataMap signatureInfoMap) {
        IDataMap creatorMap = (IDataMap) signatureInfoMap.getValue("creator");
        IDataMap subTeamLeaderMap = (IDataMap) signatureInfoMap.getValue("subTeamLeader");
        IDataMap teamLeaderMap = (IDataMap) signatureInfoMap.getValue("teamLeader");
        IDataMap bopAdminMap = (IDataMap) signatureInfoMap.getValue("bopAdmin");

        Row row = null;
        Cell cell = null;

        // Creator
        if (creatorMap != null) {
            row = currentSheet.getRow(11);
            cell = row.getCell(3);
            cell.setCellValue(creatorMap.getStringValue("user_name"));

            cell = row.getCell(4);
            cell.setCellValue(creatorMap.getStringValue(TCComponentPerson.PROP_PA10));

            cell = row.getCell(5);
            cell.setCellValue(creatorMap.getStringValue("decision_date"));

            cell = row.getCell(6);
            cell.setCellValue(creatorMap.getStringValue(TCComponentPerson.PROP_PA6));

            cell = row.getCell(7);
            cell.setCellValue(creatorMap.getStringValue("comments"));
        }

        // Sub Team Leader
        if (subTeamLeaderMap != null) {
            row = currentSheet.getRow(12);
            cell = row.getCell(3);
            cell.setCellValue(subTeamLeaderMap.getStringValue("user_name"));

            cell = row.getCell(4);
            cell.setCellValue(subTeamLeaderMap.getStringValue(TCComponentPerson.PROP_PA10));

            cell = row.getCell(5);
            cell.setCellValue(subTeamLeaderMap.getStringValue("decision_date"));

            cell = row.getCell(6);
            cell.setCellValue(subTeamLeaderMap.getStringValue(TCComponentPerson.PROP_PA6));

            cell = row.getCell(7);
            cell.setCellValue(subTeamLeaderMap.getStringValue("comments"));
        }

        // Team Leader
        if (teamLeaderMap != null) {
            row = currentSheet.getRow(13);
            cell = row.getCell(3);
            cell.setCellValue(teamLeaderMap.getStringValue("user_name"));

            cell = row.getCell(4);
            cell.setCellValue(teamLeaderMap.getStringValue(TCComponentPerson.PROP_PA10));

            cell = row.getCell(5);
            cell.setCellValue(teamLeaderMap.getStringValue("decision_date"));

            cell = row.getCell(6);
            cell.setCellValue(teamLeaderMap.getStringValue(TCComponentPerson.PROP_PA6));

            cell = row.getCell(7);
            cell.setCellValue(teamLeaderMap.getStringValue("comments"));
        }

        // BOP Admin
        if (bopAdminMap != null) {
            row = currentSheet.getRow(14);
            cell = row.getCell(3);
            cell.setCellValue(bopAdminMap.getStringValue("user_name"));

            cell = row.getCell(4);
            cell.setCellValue(bopAdminMap.getStringValue(TCComponentPerson.PROP_PA10));

            cell = row.getCell(5);
            cell.setCellValue(bopAdminMap.getStringValue("decision_date"));

            cell = row.getCell(6);
            cell.setCellValue(bopAdminMap.getStringValue(TCComponentPerson.PROP_PA6));

            cell = row.getCell(7);
            cell.setCellValue(bopAdminMap.getStringValue("comments"));
        }
    }

    /**
     * MECO EPL
     *
     * @method printMECOEPLList
     * @date 2014. 2. 13.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings("unchecked")
    private void printMECOEPLList(Sheet currentSheet, IDataSet dataSet) {
        IDataMap mecoInfoMap = dataSet.getDataMap("mecoInfo");
        IDataMap mecoEPLInfoMap = dataSet.getDataMap("mecoEPLInfo");
        IDataMap additionalInfoMap = dataSet.getDataMap("additionalInfo");

        Row row = null;
        Cell cell = null;

        // Print Date
        row = currentSheet.getRow(1);
        cell = row.getCell(14);
        cell.setCellValue(additionalInfoMap.getStringValue("print_date"));

        // MECO Date
        row = currentSheet.getRow(1);
        cell = row.getCell(13);
        cell.setCellValue(mecoInfoMap.getStringValue(SDVPropertyConstant.ITEM_DATE_RELEASED));

        // MECO No.
        row = currentSheet.getRow(3);
        cell = row.getCell(3);
        cell.setCellValue(mecoInfoMap.getStringValue(SDVPropertyConstant.ITEM_ITEM_ID));

        // MECO EPL
        ArrayList<SYMCBOPEditData> dataList = (ArrayList<SYMCBOPEditData>) mecoEPLInfoMap.getValue("mecoEPL");
        if (dataList != null && dataList.size() > 0) {
            ArrayList<String> addedEPLs = new ArrayList<String>();
            ArrayList<Row> changeBackgroundRow = new ArrayList<Row>();
            for (int i = 0; i < dataList.size(); i++) {
                SYMCBOPEditData data = dataList.get(i);
                String eplId = data.getEplId();
                if (!addedEPLs.contains(eplId)) {
                    Row oldDataRow = currentSheet.createRow(currentSheet.getLastRowNum() + 1);
                    Row newDataRow = currentSheet.createRow(currentSheet.getLastRowNum() + 1);
                    printOldEPLData(addedEPLs.size() + 1, oldDataRow, data);
                    printNewEPLData(addedEPLs.size() + 1, newDataRow, data);

                    if ((addedEPLs.size() + 1) % 2 == 0) {
                        changeBackgroundRow.add(oldDataRow);
                        changeBackgroundRow.add(newDataRow);
                    }

                    addedEPLs.add(eplId);
                }
            }

            setCellStyleOfContents(currentSheet, changeBackgroundRow);
        }
    }

    /**
     * [SR140611-055][20140609] byKim ② MECO EPL에 내용 유무 상관없이 Old/New에 No. 모두 채워줄 것, No. 필드 다음에 Old/New 표시하는 필드 추가 필요
     */
    private void printOldEPLData(int no, Row row, SYMCBOPEditData data) {
        for (int i = 0; i < HEADER_CELL_COUNT; i++) {
            row.createCell(i);
        }

        // No
        Cell cell = row.getCell(0);
        cell.setCellValue(no);

        // Old/New
        cell = row.getCell(1);
        cell.setCellValue("Old");

        if (data.getOld_child_no() == null || data.getOld_child_no().equals("")) {
            return;
        }

        // P_TYPE
        cell = row.getCell(2);
        cell.setCellValue(data.getParentType());

        // Parent Part No.
        cell = row.getCell(3);
        cell.setCellValue(data.getParentNo());

        // Rev
        cell = row.getCell(4);
        cell.setCellValue(data.getParentRev());

        // Parent Part Name
        cell = row.getCell(5);
        cell.setCellValue(data.getParentName());

        // SEQ
        cell = row.getCell(6);
        cell.setCellValue(data.getSeq());

        // C_TYPE
        cell = row.getCell(7);
        cell.setCellValue(data.getOld_child_type());

        // Child Part No.
        cell = row.getCell(8);
        cell.setCellValue(data.getOld_child_no());

        // Rev
        cell = row.getCell(9);
        cell.setCellValue(data.getOld_child_rev());

        // Child Part Name
        cell = row.getCell(10);
        cell.setCellValue(data.getOld_child_name());

        // QTY
        cell = row.getCell(11);
        cell.setCellValue(data.getOld_qty());

        // Shown_On
        cell = row.getCell(12);
        cell.setCellValue(data.getOld_shown_no_no());

        // ECO No
        cell = row.getCell(13);
        cell.setCellValue(data.getEcoNo());

        // Option
        cell = row.getCell(14);
        cell.setCellValue(data.getOld_vc());
    }

    /**
     * [SR140611-055][20140609] byKim ② MECO EPL에 내용 유무 상관없이 Old/New에 No. 모두 채워줄 것, No. 필드 다음에 Old/New 표시하는 필드 추가 필요
     */
    private void printNewEPLData(int no, Row row, SYMCBOPEditData data) {
        for (int i = 0; i < HEADER_CELL_COUNT; i++) {
            row.createCell(i);
        }

        // No
        Cell cell = row.getCell(0);
        cell.setCellValue(no);

        // Old/New
        cell = row.getCell(1);
        cell.setCellValue("New");

        if (data.getNew_child_no() == null || data.getNew_child_no().equals("")) {
            return;
        }

        // P_TYPE
        cell = row.getCell(2);
        cell.setCellValue(data.getParentType());

        // Parent Part No.
        cell = row.getCell(3);
        cell.setCellValue(data.getParentNo());

        // Rev
        cell = row.getCell(4);
        cell.setCellValue(data.getParentRev());

        // Parent Part Name
        cell = row.getCell(5);
        cell.setCellValue(data.getParentName());

        // SEQ
        cell = row.getCell(6);
        cell.setCellValue(data.getSeq());

        // C_TYPE
        cell = row.getCell(7);
        cell.setCellValue(data.getNew_child_type());

        // Child Part No.
        cell = row.getCell(8);
        cell.setCellValue(data.getNew_child_no());

        // Rev
        cell = row.getCell(9);
        cell.setCellValue(data.getNew_child_rev());

        // Child Part Name
        cell = row.getCell(10);
        cell.setCellValue(data.getNew_child_name());

        // QTY
        cell = row.getCell(11);
        cell.setCellValue(data.getNew_qty());

        // Shown_On
        cell = row.getCell(12);
        cell.setCellValue(data.getNew_shown_no_no());

        // ECO No
        cell = row.getCell(13);
        cell.setCellValue(data.getEcoNo());

        // Option
        cell = row.getCell(14);
        cell.setCellValue(data.getNew_vc());
    }

    /**
     * Contents Cell Style
     * [SR140611-055][20140609] byKim ③ MECO Report 글꼴 및 크기 변경 의뢰
     *
     * @method setCellStyleOfContents
     * @date 2014. 2. 13.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    /*
    private void setCellStyleOfContents(Sheet currentSheet, ArrayList<Row> changeBackgroundRow) {
        Workbook workbook = currentSheet.getWorkbook();

        Font font = workbook.createFont();
        font.setFontName("맑은 고딕");
        font.setFontHeightInPoints((short) 10);

        // 가운데 정렬 column
        int[] center_columnArray = new int[] { 0, 1, 4, 6, 9, 11 };
        ArrayList<Integer> center_columnList = new ArrayList<Integer>();
        for (int i = 0; i < center_columnArray.length; i++) {
            center_columnList.add(center_columnArray[i]);
        }

        int lastRowNum = currentSheet.getLastRowNum();
        for (int i = DATA_START_ROW_INDEX; i <= lastRowNum; i++) {
            Row row = currentSheet.getRow(i);
            for (int j = 0; j < HEADER_CELL_COUNT; j++) {
                Cell cell = row.getCell(j);

                XSSFCellStyle style = (XSSFCellStyle) workbook.createCellStyle();
                style.setFont(font);
                style.setBorderTop(XSSFCellStyle.BORDER_THIN);
                style.setBorderBottom(XSSFCellStyle.BORDER_THIN);
                style.setBorderLeft(XSSFCellStyle.BORDER_THIN);
                style.setBorderRight(XSSFCellStyle.BORDER_THIN);

                if (changeBackgroundRow.contains(row)) {
                    style.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
                    style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index);
                }

                if (center_columnList.contains(j)) {
                    style.setAlignment(CellStyle.ALIGN_CENTER);
                }

                cell.setCellStyle(style);
            }
        }
    }
	*/
        
    /**
     * Contents Cell Style
     * [SR140611-055][20140609] byKim ③ MECO Report 글꼴 및 크기 변경 의뢰
     * [SR141120-030][20141230] ymjang 진행 및 완료된 MECO의 상세 내용을 파악하기 위하여 MEOC를 Report로 Export하면 서식이 반영되지 않은 상태로 나옴
     * @method setCellStyleOfContents
     * @date 2014. 2. 13.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void setCellStyleOfContents(Sheet currentSheet, ArrayList<Row> changeBackgroundRow) {
        Workbook workbook = currentSheet.getWorkbook();

        // Content 셀 스타일 생성
        createCellStype(workbook);
        
        // 가운데 정렬 column
        int[] center_columnArray = new int[] { 0, 1, 4, 6, 9, 11 };
        ArrayList<Integer> center_columnList = new ArrayList<Integer>();
        for (int i = 0; i < center_columnArray.length; i++) {
            center_columnList.add(center_columnArray[i]);
        }
        
        XSSFCellStyle style = null;
        int lastRowNum = currentSheet.getLastRowNum();
        for (int i = DATA_START_ROW_INDEX; i <= lastRowNum; i++) {
            Row row = currentSheet.getRow(i);
            for (int j = 0; j < HEADER_CELL_COUNT; j++) {
                Cell cell = row.getCell(j);
                if (changeBackgroundRow.contains(row)) {
                	if (center_columnList.contains(j))
                		style = (XSSFCellStyle) this.styleMap.get("GrayCenter");
                	else
                    	style = (XSSFCellStyle) this.styleMap.get("Gray");
                } 
                else
                {
                	if (center_columnList.contains(j))
                		style = (XSSFCellStyle) this.styleMap.get("WhiteCenter");
                	else
                    	style = (XSSFCellStyle) this.styleMap.get("White");
                }
                
                cell.setCellStyle(style);
            }
        }
    }
    
    /**
     * 셀 스타일 생성
     * [SR141120-030][20141230] ymjang 진행 및 완료된 MECO의 상세 내용을 파악하기 위하여 MEOC를 Report로 Export하면 서식이 반영되지 않은 상태로 나옴
     * @method createCellStype
     * @date 2014.12.30.
     * @param workbook
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void createCellStype(Workbook workbook) {
    	
    	XSSFCellStyle style = null;
    	this.styleMap = new HashMap<String, XSSFCellStyle>();
    	
        Font font = workbook.createFont();
        font.setFontName("맑은 고딕");
        font.setFontHeightInPoints((short) 10);
        
        style = (XSSFCellStyle) workbook.createCellStyle();
        style.setFont(font);
        style.setBorderTop(XSSFCellStyle.BORDER_THIN);
        style.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        style.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        style.setBorderRight(XSSFCellStyle.BORDER_THIN);
        
        this.styleMap.put("White", style);
        
        style = (XSSFCellStyle) workbook.createCellStyle();
        style.setFont(font);
        style.setBorderTop(XSSFCellStyle.BORDER_THIN);
        style.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        style.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        style.setBorderRight(XSSFCellStyle.BORDER_THIN);
        style.setAlignment(CellStyle.ALIGN_CENTER);
        
        this.styleMap.put("WhiteCenter", style);
        
        style = (XSSFCellStyle) workbook.createCellStyle();
        style.setFont(font);
        style.setBorderTop(XSSFCellStyle.BORDER_THIN);
        style.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        style.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        style.setBorderRight(XSSFCellStyle.BORDER_THIN);
        style.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index);
        
        this.styleMap.put("Gray", style);
        
        style = (XSSFCellStyle) workbook.createCellStyle();
        style.setFont(font);
        style.setBorderTop(XSSFCellStyle.BORDER_THIN);
        style.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        style.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        style.setBorderRight(XSSFCellStyle.BORDER_THIN);
        style.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index);
        style.setAlignment(CellStyle.ALIGN_CENTER);
        
        this.styleMap.put("GrayCenter", style);
    }
}
