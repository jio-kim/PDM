package com.symc.plm.rac.prebom.ccn.excel.transformer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;

import com.symc.plm.rac.prebom.ccn.commands.dao.CustomCCNDao;
import com.symc.plm.rac.prebom.common.PropertyConstant;
import com.symc.plm.rac.prebom.common.util.SDVPreBOMUtilities;
import com.teamcenter.rac.kernel.TCException;

public class CCNDataExcelTransformer extends AbstractExcelTransformer {

    private final int ospecLev = 5;
    private int rowSeq = 1;
//    private int optionTypeSeq = 0;
    private int count = 0;
    private int columnSeq = 0;
//    private int columnSize = 46;
    //[CSH][20180508]proto tooling, ospec no 추가
    private int columnSize = 48;
    
//    private String optionType[] = {"STD", "OPT"};
    
    private HashMap<String, Integer> usageCP;
    
    private ArrayList<HashMap<String, Object>> oldMasterUsageList;
    private ArrayList<HashMap<String, Object>> newMasterUsageList;
    
    protected ArrayList<Integer> usageNoList;
    protected ArrayList<String> usageNoCountList;
    

    public CCNDataExcelTransformer() {
        super();
    }

    @Override
    public void print(int mode, String templatePreference, String defaultFileName, IDataSet dataSet) {
        // PreferenceName으로 템플릿 파일 가져오기
        templateFile = getTemplateFile(mode, templatePreference, defaultFileName);

        try {
            Workbook workbook = new XSSFWorkbook(new FileInputStream(templateFile));
            IDataMap dataMap = dataSet.getDataMap("mainInfo");

            if(dataMap != null) {
                Sheet sheet_A = workbook.getSheet("CCN_A");
                Sheet sheet_B = workbook.getSheet("CCN_B");
                
                // SHEET A 에 내용을 입력한다
                printSheetA(workbook, sheet_A, dataMap);
                
                // SHEET B 결재일, 출력일, CCN NO 를 넣는다
                prientSheetBHeader(workbook, sheet_B, dataMap);
                
                @SuppressWarnings("unchecked")
                List<HashMap<String, Object>> ospecDataList = (List<HashMap<String, Object>>) dataMap.getValue("ospecTrimList");
                
                // SHEET B 에 Header 를 그린다
                if (null != ospecDataList && ospecDataList.size() > 0) {
                    for (int i = 0; i < ospecLev; i++) {
                        prientSheetBHeaderMake(workbook, sheet_B, ospecDataList, i + 1);
                    }
                }
                
                // SHEET B 에 Data 를 넣는다
                boolean isMultiOption = false;
                List<HashMap<String, Object>> masterList = getTableData(dataSet, "masterList");
                for (int i = 0; i < masterList.size(); i++) {
                    oldMasterUsageList = new ArrayList<HashMap<String, Object>>();
                    newMasterUsageList = new ArrayList<HashMap<String, Object>>();
//                    if (null != masterList.get(i).get("OLD_LIST_ID")) {
//                        String oldListId = masterList.get(i).get("OLD_LIST_ID").toString();
//                        if (null != oldListId) {
//                            oldMasterUsageList = selectMasterUsageInfoList(oldListId, "OLD");
//                        }
//                    }
                    if (null != masterList.get(i).get("LIST_ID")) {
                        String listId = masterList.get(i).get("LIST_ID").toString();
                        if (null != listId) {
                            oldMasterUsageList = selectMasterUsageInfoList(listId, "OLD");
                            newMasterUsageList = selectMasterUsageInfoList(listId, "NEW");
                        }
                    }
//                    int count = Integer.parseInt(masterList.get(i).get("OPTION_TYPE_COUNT").toString());
//                    if (count > 1) {
//                        isMultiOption = true;
//                    }
                    
                    prientSheetB(workbook, sheet_B, true, masterList.get(i), isMultiOption, oldMasterUsageList);
                    prientSheetB(workbook, sheet_B, false, masterList.get(i), isMultiOption, newMasterUsageList);
//                    if (isMultiOption) {
////                        optionTypeSeq = 1;
//                        prientSheetB(workbook, sheet_B, true, masterList.get(i), isMultiOption, oldMasterUsageList);
//                        prientSheetB(workbook, sheet_B, false, masterList.get(i), isMultiOption, newMasterUsageList);
//                    }
//                    isMultiOption = false;
                    rowSeq++;
//                    optionTypeSeq = 0;
                }
                
                XSSFFormulaEvaluator.evaluateAllFormulaCells((XSSFWorkbook) workbook);
            }

            FileOutputStream fos = new FileOutputStream(templateFile);
            workbook.write(fos);
            fos.flush();
            fos.close();
            openFile();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TCException e) {
            e.printStackTrace();
        }
    }

    /**
     * 엑셀 시트 을지에 데이터를 넣는다
     * 
     * @param workbook
     * @param currentSheet
     * @param isOld
     * @param masterList
     * @param isMultiOption
     * @param usageList
     * @throws TCException 
     */
    private void prientSheetB(Workbook workbook, Sheet currentSheet, boolean isOld, HashMap<String, Object> masterList, boolean isMultiOption, ArrayList<HashMap<String, Object>> usageList) throws TCException {
        CellStyle cellStyle = getCellStyle(workbook, isMultiOption, rowSeq);
        
        Row row = currentSheet.createRow(9 + count);
        String defaultString = "OLD_";
        if (!isOld) {
            defaultString = "NEW_";
        } 
        
        Cell cell = row.createCell(0);
        cell.setCellValue(Integer.toString(rowSeq));
        cell.setCellStyle(cellStyle);
        
        cell = row.createCell(1);
        cell.setCellValue((String) masterList.get(defaultString + "CHILD_UNIQUE_NO"));
        cell.setCellStyle(cellStyle);
        
        cell = row.createCell(2);
        cell.setCellValue((String) masterList.get(defaultString + "CONTENTS"));
        cell.setCellStyle(cellStyle);
        
        cell = row.createCell(3);
        cell.setCellValue((String) masterList.get(defaultString + "SYSTEM_CODE"));
        cell.setCellStyle(cellStyle);
        
        cell = row.createCell(4);
        cell.setCellValue((String) masterList.get(defaultString + "SYSTEM_NAME"));
        cell.setCellStyle(cellStyle);
        
        cell = row.createCell(5);
        cell.setCellValue((String) masterList.get(defaultString + "FUNCTION"));
        cell.setCellStyle(cellStyle);
        
        cell = row.createCell(6);
        cell.setCellValue(getStringValue(masterList.get(defaultString + "LEV")));
        cell.setCellStyle(cellStyle);
        
        cell = row.createCell(7);
        cell.setCellValue((String) masterList.get(defaultString + "SEQ"));
        cell.setCellStyle(cellStyle);
        
        cell = row.createCell(8);
        if (null != masterList.get(defaultString + "CHILD_NO") && !masterList.get(defaultString + "CHILD_NO").equals("")) {
            cell.setCellValue((String) masterList.get("PARENT_UNIQUE_NO"));
        }else{
            cell.setCellValue("");
        }
        cell.setCellStyle(cellStyle);
        
        cell = row.createCell(9);
        cell.setCellValue((String) masterList.get(defaultString + "OLD_PART_NO"));
        cell.setCellStyle(cellStyle);
        
        cell = row.createCell(10);
        cell.setCellValue((String) masterList.get(defaultString + "CHILD_NO"));
        cell.setCellStyle(cellStyle);
        
        cell = row.createCell(11);
        cell.setCellValue((String) masterList.get(defaultString + "CHILD_NAME"));
        cell.setCellStyle(cellStyle);
        
        cell = row.createCell(12);
        cell.setCellValue((String) masterList.get(defaultString + "REQ_OPT"));
        cell.setCellStyle(cellStyle);

        cell = row.createCell(13);
        cell.setCellValue((String) masterList.get(defaultString + "SPECIFICATION"));
//        cell.setCellValue((String) masterList.get(defaultString + "SPEC_DESC"));
        cell.setCellStyle(cellStyle);
        
        cell = row.createCell(14);
        cell.setCellValue((String) masterList.get(defaultString + "VC"));
        cell.setCellStyle(cellStyle);
        
        cell = row.createCell(15);
        cell.setCellValue((String) masterList.get(defaultString + "CHG_TYPE_ENGCONCEPT"));
        cell.setCellStyle(cellStyle);
        
        cell = row.createCell(16);
        cell.setCellValue((String) masterList.get(defaultString + "PROJECT"));
        cell.setCellStyle(cellStyle);
        
        cell = row.createCell(17);
        cell.setCellValue((String) masterList.get(defaultString + "PROTO_TOOLING"));
        cell.setCellStyle(cellStyle);
        
        cell = row.createCell(18);
        cell.setCellValue((String) masterList.get(defaultString + "SMODE"));
        cell.setCellStyle(cellStyle);
        
        cell = row.createCell(19);
        cell.setCellValue(getStringValue(masterList.get(defaultString + "EST_WEIGHT")));
        cell.setCellStyle(cellStyle);
        
        cell = row.createCell(20);
        cell.setCellValue(getStringValue(masterList.get(defaultString + "TGT_WEIGHT")));
        cell.setCellStyle(cellStyle);
        
        cell = row.createCell(21);
        cell.setCellValue((String) masterList.get(defaultString + "MODULE"));
        cell.setCellStyle(cellStyle);
        
        cell = row.createCell(22);
        cell.setCellValue((String) masterList.get(defaultString + "ALTER_PART"));
        cell.setCellStyle(cellStyle);
        
        cell = row.createCell(23);
        cell.setCellValue((String) masterList.get(defaultString + "REGULATION"));
        cell.setCellStyle(cellStyle);
        
        cell = row.createCell(24);
        cell.setCellValue((String) masterList.get(defaultString + "BOX"));
        cell.setCellStyle(cellStyle);
        
        cell = row.createCell(25);
        cell.setCellValue(getMasterNewValue(defaultString, masterList, "CHANGE_DESC"));
        cell.setCellStyle(cellStyle);
        
        cell = row.createCell(26);
        cell.setCellValue(getMasterNewValue(defaultString, masterList, "EST_COST_MATERIAL"));
        cell.setCellStyle(cellStyle);
        
        cell = row.createCell(27);
        cell.setCellValue(getMasterNewValue(defaultString, masterList, "TGT_COST_MATERIAL"));
        cell.setCellStyle(cellStyle);
        
        cell = row.createCell(28);
        cell.setCellValue(getStringValue(masterList.get(defaultString + "DVP_NEEDED_QTY")));
        cell.setCellStyle(cellStyle);
        
        cell = row.createCell(29);
        cell.setCellValue((String) masterList.get(defaultString + "DVP_USE"));
        cell.setCellStyle(cellStyle);
        
        cell = row.createCell(30);
        cell.setCellValue((String) masterList.get(defaultString + "DVP_REQ_DEPT"));
        cell.setCellStyle(cellStyle);
        
        cell = row.createCell(31);
        cell.setCellValue((String) masterList.get(defaultString + "CON_DWG_PERFORMANCE"));
        cell.setCellStyle(cellStyle);
        
        cell = row.createCell(32);
        cell.setCellValue((String) masterList.get(defaultString + "CON_DWG_PLAN"));
        cell.setCellStyle(cellStyle);
        
        cell = row.createCell(33);
        cell.setCellValue((String) masterList.get(defaultString + "CON_DWG_TYPE"));
        cell.setCellStyle(cellStyle);
        
        cell = row.createCell(34);
        cell.setCellValue(SDVPreBOMUtilities.getChangeCCNDBDate((String)masterList.get(defaultString + "DWG_DEPLOYABLE_DATE")));
        cell.setCellStyle(cellStyle);
        
        cell = row.createCell(35);
        cell.setCellValue((String) masterList.get(defaultString + "PRD_DWG_PERFORMANCE"));
        cell.setCellStyle(cellStyle);
        
        cell = row.createCell(36);
        cell.setCellValue((String) masterList.get(defaultString + "PRD_DWG_PLAN"));
        cell.setCellStyle(cellStyle);
        
        cell = row.createCell(37);
        cell.setCellValue((String) masterList.get(defaultString + "ECO"));
        cell.setCellStyle(cellStyle);
        
        cell = row.createCell(38);
        cell.setCellValue((String) masterList.get(defaultString + "OSPEC_NO"));
        cell.setCellStyle(cellStyle);
        
        cell = row.createCell(39);
        cell.setCellValue((String) masterList.get(defaultString + "DC_ID"));
        cell.setCellStyle(cellStyle);
        
        cell = row.createCell(40);
        cell.setCellValue(SDVPreBOMUtilities.getChangeCCNDBDate((String)masterList.get(defaultString + "RELEASED_DATE")));
        cell.setCellStyle(cellStyle);
        
        cell = row.createCell(41);
        cell.setCellValue((String) masterList.get(defaultString + "ENG_DEPT_NM"));
        cell.setCellStyle(cellStyle);
        
        cell = row.createCell(42);
        cell.setCellValue((String) masterList.get(defaultString + "ENG_RESPONSIBLITY"));
        cell.setCellStyle(cellStyle);
        
        cell = row.createCell(43);
        cell.setCellValue(getMasterNewValue(defaultString, masterList, "SELECTED_COMPANY"));
        cell.setCellStyle(cellStyle);
        
        cell = row.createCell(44);
        cell.setCellValue("");
        cell.setCellStyle(cellStyle);
        
        cell = row.createCell(45);
        cell.setCellValue(getMasterNewValue(defaultString, masterList, "PRD_TOOL_COST"));
        cell.setCellStyle(cellStyle);
        
        cell = row.createCell(46);
        cell.setCellValue(getMasterNewValue(defaultString, masterList, "PRD_SERVICE_COST"));
        cell.setCellStyle(cellStyle);
        
        cell = row.createCell(47);
        cell.setCellValue(getMasterNewValue(defaultString, masterList, "PRD_SAMPLE_COST"));
        cell.setCellStyle(cellStyle);
        
        // UsageList 
        if (null != usageList && usageList.size() > 0) {
            getUsagePosition(usageList);
            for (int i = 0; i < usageNoList.size(); i++) {
                cell = row.createCell(columnSize + usageNoList.get(i));
                cell.setCellValue((String)usageNoCountList.get(i));
            }
        }
        setCellStyle(row, columnSize, cellStyle);
        count++;
    }
    
    /**
     * DB 에 값은있지만 OLD 이면 빈칸을 채워야 해서 만든 함수
     * @param type
     * @param masterList
     * @param column
     * @return
     */
    private String getMasterNewValue(String type, HashMap<String, Object> masterList, String column ){
        if (type.equals("OLD_")) {
            return "";
        }else{
            return (String)masterList.get(column);
        }
    }
    
    /**
     * 을지에 넣을 usage 정보에 위치값을 저장한다
     * @param usageList
     * @param optionTypeStr
     */
    @SuppressWarnings("unused")
    private void getUsagePosition(ArrayList<HashMap<String, Object>> usageList, String optionTypeStr) {
        usageNoList = new ArrayList<Integer>();
        usageNoCountList = new ArrayList<String>();
        for (HashMap<String, Object> resultMap : usageList) {
            if (resultMap.get("OPTION_TYPE").equals(optionTypeStr)) {
                int seq = usageCP.get(resultMap.get("LV5_KEY"));
                usageNoList.add(seq);
                usageNoCountList.add(resultMap.get("USAGE_QTY").toString());
            }
        }
    }
    
    /**
     * 을지에 넣을 usage 정보에 위치값을 저장한다
     * @param usageList
     * @param optionTypeStr
     */
    private void getUsagePosition(ArrayList<HashMap<String, Object>> usageList) {
        usageNoList = new ArrayList<Integer>();
        usageNoCountList = new ArrayList<String>();
        for (HashMap<String, Object> resultMap : usageList) {
            if (null != usageCP.get(resultMap.get("LV5_KEY"))) {
                int seq = usageCP.get(resultMap.get("LV5_KEY"));
                usageNoList.add(seq);
                if (resultMap.get("OPTION_TYPE").toString().equals("OPT")) {
                    usageNoCountList.add("(" + resultMap.get("USAGE_QTY").toString() + ")");
                }else{
                    usageNoCountList.add(resultMap.get("USAGE_QTY").toString());
                }
            }
        }
    }
    
    /**
     * 을지에 데이터에 스타일을 지정한다
     * @param workbook
     * @param isMultiOption
     * @param rowSeq
     * @return
     */
    private CellStyle getCellStyle(Workbook workbook, boolean isMultiOption, int rowSeq){
        CellStyle style = workbook.createCellStyle();
        
        Font font = workbook.createFont();
        font.setFontName("돋움");
        font.setFontHeightInPoints((short)10);

        style.setFont(font);
        style.setBorderTop(XSSFCellStyle.BORDER_THIN);
        style.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        style.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        style.setBorderRight(XSSFCellStyle.BORDER_THIN);
        style.setAlignment(XSSFCellStyle.ALIGN_CENTER);
        if ((rowSeq % 2) == 0) {
            if (isMultiOption) {
                style.setFillForegroundColor(HSSFColor.YELLOW.index);
                style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
            } else {
                style.setFillForegroundColor(HSSFColor.SKY_BLUE.index);
                style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
            }
        }
        
        return style;
    }

    /**
     * 을지에 Header 를 그려준다 
     * 
     * @param workbook
     * @param currentSheet
     * @param ospecDataList
     * @param depth
     */
    private void prientSheetBHeaderMake(Workbook workbook, Sheet currentSheet, List<HashMap<String, Object>> ospecDataList, int depth) {
      ArrayList<String> compareList = new ArrayList<String>();
      String mapKey = "LV" + depth + "_KEY";
      String mapCountKey = "LV" + depth + "_COUNT";
      String mapAttrKey = "USAGE_LV" + depth;
      columnSeq = 0;
      Row row = currentSheet.getRow(3 + depth);
      Cell cell = null;
      usageCP = new HashMap<String, Integer>();
      CellStyle cellStyle = row.getCell(1).getCellStyle();
      cellStyle = getHeaderStyle(cellStyle);
      
      for (HashMap<String, Object> mapData : ospecDataList) {
          if (depth != 5) {
              if (!compareList.contains(mapData.get(mapKey))) {
                  cell = row.createCell(columnSize + columnSeq);
                  int count = Integer.parseInt(mapData.get(mapCountKey).toString());
                  currentSheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), cell.getColumnIndex(), cell.getColumnIndex() + count - 1));
                  cell.setCellValue((String)mapData.get(mapAttrKey));
                  compareList.add(mapData.get(mapKey).toString());
                  columnSeq += count;
              }
          } else {
              cell = row.createCell(columnSize + columnSeq);
              cell.setCellValue((String)mapData.get(mapAttrKey));
              usageCP.put(mapData.get(mapKey).toString(), columnSeq);
              columnSeq++;
          }
      }
      setCellStyle(row, columnSize, cellStyle);
    }
    
    /**
     * 받아온 cell 에 스타일을 넣어준다 
     * 
     * @param row
     * @param startCellNo
     * @param cellStyle
     */
    private void setCellStyle(Row row, int startCellNo, CellStyle cellStyle) {
        Cell cell = null;
        for (int i = 0; i < columnSeq; i++) {
            cell = row.getCell(startCellNo + i);
            if (null == cell) {
                cell = row.createCell(startCellNo + i);
            }
            cell.setCellStyle(cellStyle);
        }
    }

    /**
     * Header 에 그려진 컬럼에 테두리 및 가운데 정렬을 지정한다
     * 
     * @param cellStyle
     * @return
     */
    private CellStyle getHeaderStyle(CellStyle cellStyle){
        
        cellStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);
        cellStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        cellStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        cellStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
        cellStyle.setAlignment(XSSFCellStyle.ALIGN_CENTER);
        
        return cellStyle;
    }

    /**
     * 을지에 상단 기본정보를 넣어준다
     * @param workbook
     * @param currentSheet
     * @param dataMap
     */
    private void prientSheetBHeader(Workbook workbook, Sheet currentSheet, IDataMap dataMap) {
        // Released Date, 출력 일시
        Row row = currentSheet.getRow(1);
        Cell cell = row.getCell(12);
        cell.setCellValue(dataMap.getStringValue(PropertyConstant.ATTR_NAME_DATERELEASED));
        cell = row.getCell(13);
        cell.setCellValue(dataMap.getStringValue("excelExportDate"));

        // CCN NO, O-Spec NO
        row = currentSheet.getRow(3);
        cell = row.getCell(3);
        cell.setCellValue(dataMap.getStringValue(PropertyConstant.ATTR_NAME_ITEMID));
    }

    /**
     * CCN 갑지 데이터를 뿌려준다
     * 
     * @param workbook
     * @param currentSheet
     * @param dataMap
     */
    private void printSheetA(Workbook workbook, Sheet currentSheet, IDataMap dataMap) {
        // Released Date, 출력 일시
        Row row = currentSheet.getRow(1);
        Cell cell = row.getCell(6);
        cell.setCellValue(dataMap.getStringValue(PropertyConstant.ATTR_NAME_DATERELEASED));
        cell = row.getCell(7);
        cell.setCellValue(dataMap.getStringValue("excelExportDate"));

        // CCN NO, Project Type
        row = currentSheet.getRow(3);
        cell = row.getCell(2);
        cell.setCellValue(dataMap.getStringValue(PropertyConstant.ATTR_NAME_ITEMID));
        cell = row.getCell(6);
        cell.setCellValue(dataMap.getStringValue(PropertyConstant.ATTR_NAME_PROJECTTYPE));
        
        // Project Code, System Code
        row = currentSheet.getRow(4);
        cell = row.getCell(2);
        cell.setCellValue(dataMap.getStringValue(PropertyConstant.ATTR_NAME_PROJCODE));
        cell = row.getCell(6);
        cell.setCellValue(dataMap.getStringValue(PropertyConstant.ATTR_NAME_SYSTEMCODE));
        
        // O-Spec NO, Gate Number
        row = currentSheet.getRow(5);
        cell = row.getCell(2);
        cell.setCellValue(dataMap.getStringValue(PropertyConstant.ATTR_NAME_OSPECNO));
        cell = row.getCell(6);
        cell.setCellValue(dataMap.getStringValue(PropertyConstant.ATTR_NAME_GATENO));
        
        // Affected System Code
        row = currentSheet.getRow(6);
        cell = row.getCell(2);
        cell.setCellValue(dataMap.getStringValue(PropertyConstant.ATTR_NAME_AFFECTEDSYSCODE));       

        // Change Description
        row = currentSheet.getRow(7);
        cell = row.getCell(2);
        cell.setCellValue(dataMap.getStringValue(PropertyConstant.ATTR_NAME_ITEMDESC));
        
        String checkPoint = "";
        if (dataMap.getStringValue(PropertyConstant.ATTR_NAME_REGULATION).equals("true")) {
            checkPoint += "(Regulation) ";
        }
        if (dataMap.getStringValue(PropertyConstant.ATTR_NAME_COSTDOWN).equals("true")) {
            checkPoint += "(Cost Down) ";
        }
        if (dataMap.getStringValue(PropertyConstant.ATTR_NAME_ORDERINGSPEC).equals("true")) {
            checkPoint += "(Ordering Spec) ";
        }
        if (dataMap.getStringValue(PropertyConstant.ATTR_NAME_QUALITYIMPROVEMENT).equals("true")) {
            checkPoint += "(Quality Improvement) ";
        }
        if (dataMap.getStringValue(PropertyConstant.ATTR_NAME_CORRECTIONOFEPL).equals("true")) {
            checkPoint += "(Correction of EPL) ";
        }
        if (dataMap.getStringValue(PropertyConstant.ATTR_NAME_STYLINGUPDATE).equals("true")) {
            checkPoint += "(Styling up-date) ";
        }
        if (dataMap.getStringValue(PropertyConstant.ATTR_NAME_WEIGHTCHANGE).equals("true")) {
            checkPoint += "(Weight Change) ";
        }
        if (dataMap.getStringValue(PropertyConstant.ATTR_NAME_MATERIALCOSTCHANGE).equals("true")) {
            checkPoint += "(Material Cost Change) ";
        }
        if (dataMap.getStringValue(PropertyConstant.ATTR_NAME_THEOTHERS).equals("true")) {
            checkPoint += "(The others)";
        }
        
        // Check Point
        row = currentSheet.getRow(14);
        cell = row.getCell(2);
        cell.setCellValue(checkPoint);
        
        // Creator
        row = currentSheet.getRow(15);
        cell = row.getCell(2);
        cell.setCellValue(dataMap.getStringValue("creator"));
        
        // Dept Name
        row = currentSheet.getRow(15);
        cell = row.getCell(6);
        cell.setCellValue(dataMap.getStringValue("deptName"));
    }

    /**
     * @param dataSet
     * @param property
     * @return
     */
    private List<HashMap<String, Object>> getTableData(IDataSet dataSet, String property) {
        Collection<HashMap<String, Object>> data = null;

        IDataMap dataMap = dataSet.getDataMap(property);
        if(dataMap != null) {
            data = dataMap.getTableValue(property);
        }

        return (List<HashMap<String, Object>>) data;
    }
    

    /**
     * DB 의 USAGE 데이터를 읽어 온다
     * @param listId
     * @return
     */
    private ArrayList<HashMap< String, Object>> selectMasterUsageInfoList(String listId, String historyType) {                                                
        CustomCCNDao dao = null;
        ArrayList<HashMap< String, Object>> resultList = new ArrayList<HashMap< String, Object>>();
        try {
            dao = new CustomCCNDao();
            resultList = dao.selectMasterUsageInfoList(listId, historyType);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultList;
    }
    
    /**
     * DB 에서 가져온 값을 String 변환해서 리턴한다
     * @param value
     * @return
     */
    @SuppressWarnings("static-access")
    private String getStringValue(Object value){
        if (!toString().valueOf(value).equals("null")) {
            return toString().valueOf(value);
        }else{
            return "";
        }
    }

}
