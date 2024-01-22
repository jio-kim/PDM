package com.symc.plm.me.sdv.excel.transformer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.sdv.excel.common.ExcelTemplateHelper;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.kernel.TCPreferenceService.TCPreferenceLocation;
import com.teamcenter.rac.util.Registry;

import common.Logger;

public class SubsidiaryRequestFormListExcelTransformer extends AbstractExcelTransformer {

    private static Registry registry = Registry.getRegistry(SubsidiaryRequestFormListExcelTransformer.class);
    private static final Logger logger = Logger.getLogger(SubsidiaryRequestFormListExcelTransformer.class);
    public static final int DEFAULT_OPTION_DESC_ROW_NO = 5;
    public static final int DEFAULT_START_ROW_NO = 6;
    public static final int DEFAULT_COMMON_OPTION_COLUMN_NO = 8;
    public static final int DEFAULT_REMARK_COLUMN_NO = 17;
    public static final int DEFAULT_SUM_QUANTITY_ROW_NO = 18;

    public static final String DEFAULT_SUBSIDIARY_TYPE_FORMAT = "부자재 구분 : ( %s 직접  %s 간접  %s 소모성)";
    public static final String DEFAULT_SUBSIDIARY_TYPE_SELECT = "■";
    public static final Object[] DEFAULT_SUBSIDIARY_TYPE_NORMAL = { "□", "□", "□" };

    private static final String DEFAULT_COMMON_OPTION_DESCRIPTION = "";

    private int dataStartIndex = DEFAULT_START_ROW_NO;
    private int commonOptionColIndex = DEFAULT_COMMON_OPTION_COLUMN_NO;
    private String operationType = "";

    public SubsidiaryRequestFormListExcelTransformer() {
        super();
    }

    /**
     * 
     * @method print
     * @date 2013. 11. 26.
     * @param mode
     *            , templatePreference, defaultFileName, dataSet
     * @return void
     * @throws Exception
     * @see
     */

    @Override
    public void print(int mode, String templatePreference, String defaultFileName, IDataSet dataSet) throws Exception {

        // 템플리트에서 ROW가 시작할 행의 인덱스 위치를 서버의 환경설정에서 가져온다.
        // 만약 서버에서 가져오지 못하거나 오류나 날 경우 기본 설정인 DEFAULT_START_ROW_NO=6 을 사용한다.
        try {
            TCSession session = ExcelTemplateHelper.getTCSession();
            TCPreferenceService tcprefService = session.getPreferenceService();
//            int rowStartIndex = tcprefService.getInt(TCPreferenceService.TC_preference_site, "DEFAULT_START_ROW_NO", DEFAULT_START_ROW_NO);
            Integer rowStartIndex = tcprefService.getIntegerValueAtLocation("DEFAULT_START_ROW_NO", TCPreferenceLocation.OVERLAY_LOCATION);
            if(rowStartIndex == null) {
            	rowStartIndex = DEFAULT_START_ROW_NO;
            }
            dataStartIndex = rowStartIndex;

//            int commonColIndex = tcprefService.getInt(TCPreferenceService.TC_preference_site, "DEFAULT_COMMON_OPTION_COLUMN_NO", DEFAULT_COMMON_OPTION_COLUMN_NO);
            Integer commonColIndex = tcprefService.getIntegerValueAtLocation("DEFAULT_COMMON_OPTION_COLUMN_NO", TCPreferenceLocation.OVERLAY_LOCATION);
            if(commonColIndex == null) {
            	commonColIndex = DEFAULT_COMMON_OPTION_COLUMN_NO;
            }

            commonOptionColIndex = commonColIndex;

        } catch (Exception ex) {
            logger.equals("DEFAULT_START_ROW_NO preference is not defined !!!");
            logger.error(ex);
        }

        // PreferenceName으로 템플릿 파일 가져오기
        templateFile = getTemplateFile(mode, templatePreference, defaultFileName);

        try {
            Workbook workbook = new XSSFWorkbook(new FileInputStream(templateFile));
            Map<String, XSSFCellStyle> cellStyles = ExcelTemplateHelper.getCellStyles(workbook);
            IDataMap dataMap = dataSet.getDataMap("SubsidiaryListMap");
            IData data = dataMap.get("SubsidiaryListMap");
            //String targetName = dataMap.getStringValue("SubsidiaryListTargetName");
            operationType = dataMap.getStringValue("operationType");
            // Data Value에 대한 유형을 정확히 알고 있으므로 @SuppressWarnings 적용함
            @SuppressWarnings("unchecked")
            HashMap<String, ArrayList<HashMap<String, Object>>> subsidiaryListMap = (HashMap<String, ArrayList<HashMap<String, Object>>>) data.getValue();
            boolean isArrayOp = "M7_BOPAssyOp".equals(operationType);

            Sheet currentSheet = null;
            boolean foundSubsidiaryList = false;
            if (subsidiaryListMap.size() == 0) {
                throw new TCException(registry.getString("report.subsidiaryMaterialError"));
            }
            //부자재 품번No 순으로 subsidiaryListMap 정렬하여 treeMap 구성
            Map<String, ArrayList<HashMap<String, Object>>> treeMap = new TreeMap<String, ArrayList<HashMap<String, Object>>>(subsidiaryListMap);


            for (String key : treeMap.keySet()) {
                if (!foundSubsidiaryList)
                    foundSubsidiaryList = true;
                // if(workbook.getSheetIndex(currentSheet) >= 0)

                // 부자재가 속한 공법 목록을 가져온다.
                ArrayList<HashMap<String, Object>> subList = treeMap.get(key);
                if (subList == null)
                    continue;

                // 전체 부자재 할당된 공법의 수
                int listSize = subList.size();
                int page = 0;

                List<Sheet> sheetList = new ArrayList<Sheet>();
                List<OptionColumnInfo> optionInfos = new ArrayList<OptionColumnInfo>();

                // 부자재별 표시된 공법 (공법 12개마다 한페이지에 저장한다.)
                HashMap<String, SheetRowInfo> opForSubsidiraryListIgnoreOptionCode = new HashMap<String, SheetRowInfo>();
                int rowIndex = 0;

                for (int j = 0; j < listSize; j++) {
                    Sheet targetSheet = null;
                    String opID = (String) subList.get(j).get(operationType + SDVPropertyConstant.BL_ITEM_ID);
                    double subQuantity = 0.0;
                    try {
                        if ((Double) subList.get(j).get("quantity") == null) {
                            subQuantity = 0.0;
                        } else {
                            subQuantity = (Double) subList.get(j).get("quantity");
                        }
                    } catch (Exception ex) {
                        logger.error(ex);
                    }

                    String description = (String) subList.get(j).get("description");
                    String rowKey = opID;
                    // 조립일 경우 옵션 구분을 안하므로 옵션별 공법을 모두 구분하도록 키를 공법아이디와 옵션설명을 합쳐서 사용한다.
                    if (isArrayOp) {
                        rowKey = opID + ":" + description;
                    }

                    // 이미 처리한 공법의 경우 소요량만 표시
                    if (opForSubsidiraryListIgnoreOptionCode.containsKey(rowKey)) {
                        rowIndex = opForSubsidiraryListIgnoreOptionCode.get(rowKey).rowIndex;
                        targetSheet = opForSubsidiraryListIgnoreOptionCode.get(rowKey).sheet;
                    } else {
                        // 처음 등록하는 공법의 경우에는 열을 추가하여 공법을 등록
                        if (opForSubsidiraryListIgnoreOptionCode.size() % 12 == 0) {

                            // 첫번째 시트를 복사한다. 작업이 끝난후 첫번째 시트를 삭제해주게 된다.
                            targetSheet = workbook.cloneSheet(0);
                            currentSheet = targetSheet;
                            rowIndex = 0;
                            page++;
                            sheetList.add(targetSheet);
                            changeSheetName(targetSheet, sheetList.size(), subList.get(0));

                            if (!operationType.equals("M7_BOPAssyOp")) {
                                updateDescription(sheetList, optionInfos);
                            }
                        } else {
                            // 페이지가 새로 생성되지 않는다면 최근 생성한 시트를 사용한다.
                            targetSheet = currentSheet;
                        }
                        printRow(currentSheet, rowIndex++, subList.get(j), opID, cellStyles);
                        opForSubsidiraryListIgnoreOptionCode.put(rowKey, new SheetRowInfo(currentSheet, rowIndex));
                    }
                    // 옵션 description
                    setOptionNQuantity(optionInfos, isArrayOp, targetSheet, rowIndex, description, subQuantity, subQuantity);

                }
                // 조립을 제외하고 나머지 부문은 부자재별 각 시트의 옵션 정보를 정리하여 각 시트의 옵션 정보 세팅을 맞춘다.
                if (!isArrayOp) {
                    updateDescription(sheetList, optionInfos);
                }
                // 부자재별 공법입력후 시트 전체 정보 입력을 처리한다.
                updateSheetSummarise(sheetList, optionInfos, subList.get(0), page);
            }

            if (foundSubsidiaryList) {
                workbook.removeSheetAt(0);
                FileOutputStream fos = new FileOutputStream(templateFile);
                workbook.write(fos);
                fos.flush();
                fos.close();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 
     * @method setOptionNQuantity
     * @date 2013. 11. 26.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void setOptionNQuantity(List<OptionColumnInfo> optionInfos, boolean isArrayOp, Sheet targetSheet, int rowIndex, String description, double subQuantityStr, double subQuantity) {
        rowIndex = dataStartIndex + rowIndex - 1;
        // 공통이 먼저 나올수 있도록 옵션 정보가 처음 한개도 등록되지 않았을때 제일 첫열 정보로 공통옵션을 기본으로 넣어준다.
        if (optionInfos.size() == 0) {
            optionInfos.add(new OptionColumnInfo(DEFAULT_COMMON_OPTION_DESCRIPTION, 0, 0));
        }
        // 조립
        if (isArrayOp) {
            // 소요량을 공통 옵션이 속한 컬럼(첫번째 컬럼)에 수량을 표기한다. - 조립
            Row rowQuantity = targetSheet.getRow(rowIndex);
            Cell cellQuantity = rowQuantity.getCell(DEFAULT_COMMON_OPTION_COLUMN_NO);
            cellQuantity.setCellValue(subQuantity);

            // 공통 옵션 컬럼에 합산 처리
            optionInfos.get(0).sumQuntity(subQuantity);

            // description - 조립
            // 조립의 경우 모든 옵션을 공통으로 처리하여 공통 옵션 컬럼에 수량을 입력하나, 공통옵션이 아닌 경우에는 비고란에 옵션코드를 표기해준다.
            // 공통이 아닌 옵션 코드를 비교란에 입력한다
            if (!DEFAULT_COMMON_OPTION_DESCRIPTION.equals(description)) {
                Row rowDesc = targetSheet.getRow(rowIndex);
                Cell cellDesc = rowDesc.getCell(DEFAULT_REMARK_COLUMN_NO);
                cellDesc.setCellValue(description);
            }
            // 차체
        } else {
            int optionIndex = 0;
            // 옵션코드가 이전에 한번 나왔었는지를 확인하기 위해 옵션 코드 목록을 확인한다.
            // 이전에 등록된 옵션코드는 옵션인포 리스트에 등록되어 있으므로 수량만 합산하고 같은 컬럼위치에 각 Row별 수량을 표기한다.
            // 이미 공통 옵션처리가 되어 있어 공통옵션이 나오면 자동으로 제일 앞 컬럼에 들어간다.
            if (!containOptionInfo(optionInfos, description)) {
                // 새롭게 옵션코드를 등록하기 위해 이전에 등록된 옵션코드의 마지막 인덱스를 가져온다 (현재 리스트의 크기가 추가될 마지막 인덱스가 된다.)
                optionIndex = optionInfos.size();
                optionInfos.add(new OptionColumnInfo(description, optionIndex, subQuantity));
            } else {
                // 이전의 등록된 같은 옵션코드의 컬럼 인덱스를 찾아온다.
                optionIndex = indexOfOptionInfo(optionInfos, description);
                // 이전에 등록된 옵션이 나오면 합산처리한다.
                optionInfos.get(optionIndex).sumQuntity(subQuantity);
            }
            // 옵션 인덱스는 0 기준이므로 템플리트 파일에서 표기될 옵션코드의 시작위치를 더해준다. 기본으로 공통 컬럼이 제일 좌측에 표기되므로 공통컬럼 인덱스를 더해주면
            // 해당 옵션의 컬럼인덱스가 나온다.
            int optionColIndex = optionIndex + commonOptionColIndex;

            // 부자재 소요량을 해당 옵션컬럼의 공법 열에 입력해준다. - 차체/도장
            Row row1 = targetSheet.getRow(rowIndex);
            Cell cell = row1.getCell(optionColIndex);
            cell.setCellValue(subQuantity);
        }
    }

    /**
     * 
     * @method updateSheetSummarise
     * @date 2013. 11. 26.
     * @param sheetList
     *            , optionInfos, subDatamap, subDatamap
     * @return void
     * @exception Exception
     * @throws
     * @see
     */
    private void updateSheetSummarise(List<Sheet> sheetList, List<OptionColumnInfo> optionInfos, HashMap<String, Object> subDatamap, int page) throws Exception {
        // 부자재 기본 정보 세팅
        for (int l = 0; l < sheetList.size(); l++) {
            printQuantitySum(sheetList.get(l), optionInfos);
            printSubRow(sheetList.get(l), subDatamap, page, l + 1);
        }
    }

    /**
     * 
     * @method containOptionInfo
     * @date 2013. 11. 26.
     * @param optionInfos
     *            , description
     * @return boolean
     * @exception
     * @throws
     * @see
     */
    private boolean containOptionInfo(List<OptionColumnInfo> optionInfos, String description) {
        if (optionInfos != null) {
            for (OptionColumnInfo oi : optionInfos) {
                if (oi.optionDescription.equals(description))
                    return true;
            }
        }
        return false;
    }

    /**
     * 
     * @param rowIndex
     * @method indexOfOptionInfo
     * @date 2013. 11. 26.
     * @param optionInfos
     *            , description
     * @return int
     * @exception
     * @throws
     * @see
     */
    private int indexOfOptionInfo(List<OptionColumnInfo> optionInfos, String description) {
        if (optionInfos != null) {
            for (OptionColumnInfo oi : optionInfos) {
                if (oi.optionDescription.equals(description)) {
                    return oi.columnIndex;
                }
            }
        }
        return 0;
    }

    /**
     * 
     * @method changeSheetName
     * @date 2013. 11. 25.
     * @param targetSheet
     *            , size, subsidiraryDataMap
     * @return boolean
     * @exception
     * @throws
     * @see
     */
    protected boolean changeSheetName(Sheet targetSheet, int size, HashMap<String, Object> subsidiraryDataMap) {
        // Part No를 시트 이름으로 사용함
        String partNoForSheetname = (String) subsidiraryDataMap.get(SDVPropertyConstant.BL_ITEM_ID);
        // 주어진 시트의 인덱스를 찾는다.
        Workbook parentWB = targetSheet.getWorkbook();
        int currentSheetIndex = parentWB.getSheetIndex(targetSheet);

        // 처음 시트일 경우 partno만을 사용한다.
        if (size == 1) {
            parentWB.setSheetName(currentSheetIndex, partNoForSheetname);
        } else if (size == 2) {
            // 두번째 시트부터는 PartNo에 번호를 붙인다. (단 두번째에는 이전의 PartNo로 된 Sheet를 찾아서 이름을 변경해준다.
            int previousSheetIndex = parentWB.getSheetIndex(partNoForSheetname);
            if (previousSheetIndex >= 0) {
                // 혹시 partno로 된 시트 이름이 안나올 경우 오류를 없애기 위하여
                parentWB.setSheetName(previousSheetIndex, partNoForSheetname + "(1)");
                parentWB.setSheetName(currentSheetIndex, partNoForSheetname + "(2)");
            } else {
                parentWB.setSheetName(currentSheetIndex, partNoForSheetname + "(1)");
            }
        } else if (size > 2) {
            // 세번째 이후는 인덱스 번호로 시트 번호를 붙여 준다.
            parentWB.setSheetName(currentSheetIndex, partNoForSheetname + "(" + size + ")");
        } else {
            // 빈값일 경우
        }
        return true;
    }

    /**
     * 
     * @method updateDescription
     * @date 2013. 11. 25.
     * @param sheetList
     *            , optionInfos
     * @return List<Sheet>
     * @exception
     * @throws
     * @see
     */
    // 새롭게 시트가 추가되거나 옵션이 추가될때마다 이전에 생성된 시트 모두에 모아진 옵션 설명을 모두 세팅해준다.
    private List<Sheet> updateDescription(List<Sheet> sheetList, List<OptionColumnInfo> optionInfos) {
        for (Sheet sheet : sheetList) {
            Row row = sheet.getRow(DEFAULT_OPTION_DESC_ROW_NO);
            for (int i = 0; i < optionInfos.size(); i++) {
                Cell cell = row.getCell(this.commonOptionColIndex + i);
                if (optionInfos.get(i).optionDescription.equals("")) {
                    cell.setCellValue("공통" + optionInfos.get(i).getOptionDescription());
                } else {
                    cell.setCellValue(optionInfos.get(i).getOptionDescription());
                }
            }
        }
        return sheetList;
    }

    /**
     * 
     * @method printQuantitySum
     * @date 2013. 11. 25.
     * @param sheetList
     *            , optionInfos
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void printQuantitySum(Sheet sheet, List<OptionColumnInfo> optionInfos) {
        Row row = sheet.getRow(DEFAULT_SUM_QUANTITY_ROW_NO);
        for (int i = 0; i < optionInfos.size(); i++) {
            if (optionInfos.get(i).optionDescription.equals("")) {
                Cell cell = row.getCell(DEFAULT_COMMON_OPTION_COLUMN_NO + i);
                cell.setCellValue(optionInfos.get(i).quantitySum);
            } else {
                Cell cell = row.getCell(this.commonOptionColIndex + i);
                cell.setCellValue(optionInfos.get(i).quantitySum);
            }
        }
    }

    /**
     * 
     * @method printSubRow
     * @date 2013. 11. 12.
     * @param currentSheet
     *            , dataMap, mapSize, page
     * @return void
     * @throws TCException
     * @exception
     * @throws
     * @see
     */
    private void printSubRow(Sheet currentSheet, HashMap<String, Object> dataMap, int mapSize, int page) throws TCException {

        // 부자재 구분
        Row row = currentSheet.getRow(3);
        Cell cell = row.getCell(0);

        // public static final String DEFAULT_SUBSIDIARY_TYPE_SELECT = "■";

        Object[] subsidiaryTypes = DEFAULT_SUBSIDIARY_TYPE_NORMAL.clone();
        if (dataMap.containsKey(SDVPropertyConstant.SUBSIDIARY_SUBSIDIARY_GROUP)) {
            int typeIndex = ((Integer) dataMap.get(SDVPropertyConstant.SUBSIDIARY_SUBSIDIARY_GROUP)).intValue();
            subsidiaryTypes[typeIndex] = DEFAULT_SUBSIDIARY_TYPE_SELECT;
        }
        String subsidiaryTypeStr = String.format(DEFAULT_SUBSIDIARY_TYPE_FORMAT, subsidiaryTypes);
        cell.setCellValue(subsidiaryTypeStr);

        // 부자재명
        row = currentSheet.getRow(19);
        cell = row.getCell(7);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.BL_OBJECT_NAME));

        // 규격
        row = currentSheet.getRow(21);
        cell = row.getCell(7);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.SUBSIDIARY_SPEC_KOR));

        // 용기 단위
        row = currentSheet.getRow(20);
        cell = row.getCell(11);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.SUBSIDIARY_BUY_UNIT));

        // 품번
        row = currentSheet.getRow(25);
        cell = row.getCell(11);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.BL_ITEM_ID));

        // 작성부서
        row = currentSheet.getRow(22);
        cell = row.getCell(16);
        cell.setCellValue((String) dataMap.get("group"));

        // 작성인
        row = currentSheet.getRow(23);
        cell = row.getCell(16);
        cell.setCellValue((String) dataMap.get("name"));

        // shopCode
        row = currentSheet.getRow(25);
        cell = row.getCell(16);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.SHOP_REV_SHOP_CODE));

        // productCode
        row = currentSheet.getRow(24);
        cell = row.getCell(16);
        cell.setCellValue((String) dataMap.get("productCode"));

        // 매수
        row = currentSheet.getRow(21);
        cell = row.getCell(16);
        cell.setCellValue(page + " / " + mapSize);

    }

    /**
     * 한 Row씩 출력한다.
     * 
     * @method printRow
     * @date 2013. 10. 28.
     * @param currentSheet
     * @param num
     * @param dataMap
     * @param opID
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void printRow(Sheet currentSheet, int num, HashMap<String, Object> dataMap, String opID, Map<String, XSSFCellStyle> cellStyles) {

        int rowIndex = dataStartIndex + num;

        Row row = currentSheet.getRow(rowIndex);

        // Line
        Cell cell = row.getCell(2);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.LINE_REV_CODE));

        // 공정 NO
        cell = row.getCell(3);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.STATION_STATION_CODE));

        // 공법(작업표준서) NO
        cell = row.getCell(4);
        cell.setCellValue(opID);

        // 공법명
        cell = row.getCell(5);
        cell.setCellValue((String) dataMap.get(operationType + SDVPropertyConstant.BL_OBJECT_NAME));

    }

    protected class SheetRowInfo {
        Sheet sheet;
        int rowIndex;

        SheetRowInfo(Sheet sheet, int rowIndex) {
            this.sheet = sheet;
            this.rowIndex = rowIndex;
        }
    }

    protected class OptionColumnInfo {
        String optionDescription;
        int columnIndex;
        double quantitySum;

        public OptionColumnInfo(String optionDescription, int columnIndex, double quantitySum) {
            this.optionDescription = optionDescription;
            this.columnIndex = columnIndex;
            this.quantitySum = quantitySum;
        }

        public void sumQuntity(double quantity) {
            this.quantitySum += quantity;
        }

        public String getOptionDescription() {
            return optionDescription;
        }

        public int getColIndex() {
            return this.columnIndex;
        }

        public int compareOption(String anotherOptionDesc) {
            return this.optionDescription.compareTo(anotherOptionDesc);
        }

    }

}
