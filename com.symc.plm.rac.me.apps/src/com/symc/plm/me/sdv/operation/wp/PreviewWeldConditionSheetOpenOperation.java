package com.symc.plm.me.sdv.operation.wp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.sdv.excel.common.PreviewWeldConditionSheetExcelHelper;
import com.symc.plm.me.sdv.excel.transformer.PreviewWeldConditionSheetExcelTransformer;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.TCComponentBOPLine;


/**
 * [SR140611-027][20140611] jwlee 용접조건표 을지 추가
 * [SR150303-013][20150310]shcho, 용접조건표 갑지(2) 그림영역 삭제되는 오류 수정
 */
public class PreviewWeldConditionSheetOpenOperation {

    protected final static int DEFAULT_TOP_START_INDEX = 1;
    protected final static int DEFAULT_HEADER_START_INDEX = 3; // headerSrartIndex = 3;
    protected final static int DEFAULT_WELD_START_INDEX = 40; // weldStartIndex = 40;
    protected final static int DEFAULT_MECO_START_INDEX = 83; // mecoStartIndex = 83;

    protected final static int DEFAULT_WELD_LIST_SIZE = 20; // weldListSize = 20;
    protected final static int DEFAULT_MECO_LIST_SIZE = 5; // mecoListSize = 5;

    protected final static int[] DEFAULT_TOP_CELL = {32, 37};
    protected final static int[] DEFAULT_HEADER_CELL = {3, 11, 19, 27, 35}; // headerCell = {3, 11, 19, 27, 35};
    protected final static int[] DEFAULT_WELD_CELL = {0, 2, 8, 10, 33, 35, 37}; // weldCell = {0, 2, 7, 9, 33, 35, 37};
    protected final static int[] DEFAULT_MECO_CELL = {0, 2, 5, 9, 17, 19, 21, 23, 26, 30, 38, 40}; // mecoCell = {0, 2, 5, 9, 17, 19, 21, 23, 26, 30, 38, 40};

    protected final static String DEFAULT_BASE_SHEET = "1"; // baseSheet = "1";
    protected final static String DEFAULT_SECRET_SHEET = "copySheet"; // secretSheet = "copySheet";
    protected final static String DEFAULT_SYSTEM_SHEET = "systemSheet"; // 갑지 시트  [SR140611-027][20140611] jwlee 용접조건표 을지 추가
    protected final static String DEFAULT_SECRET_MECO_SHEET = "MECO_List";

    /**
     *  용접점 갯수를 체크한다 (Sheet 1개)
     *
     *
     * @method redefinition
     * @date 2013. 11. 26.
     * @param
     * @return void
     * @throws Exception
     * @exception
     * @throws
     * @see
     */
    public Workbook redefinition(TCComponentBOPLine selectedTarget, File file, String weldConditionSheetType) throws Exception
    {
        AIFComponentContext[] weldOPChilds = selectedTarget.getChildren();
        int weldCount = 0;
        for (AIFComponentContext weldOPChild : weldOPChilds)
        {
            if (weldOPChild.getComponent().getProperty(SDVPropertyConstant.BL_OBJECT_TYPE).equals("WeldPoint"))
                weldCount += 1;
        }
        // 용접점에 갯수가 20개 미만이면 시스템에서 추가한 시트를 삭제한다
        // 시트 삭제 로직 추가
        Workbook workbook = sheetCheck(weldCount, file, weldConditionSheetType);

        /*PreviewWeldConditionSheetExcelTransformer transFormer = new PreviewWeldConditionSheetExcelTransformer();
        transFormer.print(file, workbook, null);*/
        return workbook;
    }

    /**
     * 기존에 있는 Sheet수와 현재 WeldOP 에 할당된 용접점 갯수를 비교하여 Sheet 갯수를 비교한다
     * [SR150303-013][20150310]shcho, 용접조건표 갑지(2) 그림영역 삭제되는 오류 수정
     *
     *
     * @method sheetCheck
     * @date 2013. 11. 26.
     * @param
     * @return void
     * @throws IOException
     * @exception
     * @throws
     * @see
     */
    private Workbook sheetCheck(int count, File file, String weldConditionSheetType) throws IOException
    {
    	
        //Workbook workbook = PreviewWeldConditionSheetExcelHelper.getWorkbook(file);
    	Workbook workbook = PreviewWeldConditionSheetExcelTransformer.initWorkBook(file, weldConditionSheetType);

        boolean numberCheckflag = false;
        
        /* 필요한 sheet수 : (용접점 개수를 20개 단위로 나눌 경우 필요한 sheet수) */
        double weldSheetCount = Math.ceil(((double)count)/((double)20));

        for (int i = 0; i < workbook.getNumberOfSheets(); i++)
        {
            String sheetName = workbook.getSheetName(i);
            numberCheckflag = Pattern.matches("^[0-9]*$", sheetName.substring(0, 1));
            // sheet 이름이 숫자로 시작하면 삭제대상인지 판별하여 삭제 작업 진행
            if (numberCheckflag) {
                int sheetNum = Integer.parseInt(sheetName.substring(0, 1));
                if(weldSheetCount < sheetNum) {
                    workbook = sheetDelete(workbook, i);
                } else {
                    //sheet 삭제 대상이 아닌 경우 기존 입력되어 있는 내용을 삭제한다.
                    workbook = clearSheetData(workbook, i, true);
                }
            }
        }
        
        // DEFAULT_SECRET_SHEET의 기존 입력되어 있는 내용을 삭제한다 (UserSheet 용)
        String copySheet = DEFAULT_SECRET_SHEET;
        int copyNO = workbook.getSheetIndex(copySheet);
        workbook = clearSheetData(workbook, copyNO, false);

        return workbook;
    }

    /**
     * sheet 수 비교 결과를 받아서 삭제대상 sheet를 삭제한다. 
     * [SR150303-013][20150310]shcho, 용접조건표 갑지(2) 그림영역 삭제되는 오류 수정
     * 
     * @param workbook
     * @param sheetIndex
     * @return Workbook
     */
    private Workbook sheetDelete(Workbook workbook, int sheetIndex) {
        if(sheetIndex >= 0) {
            workbook.removeSheetAt(sheetIndex);
        }
        return workbook;
    }

    /**
     * sheet 수 비교 결과를 받아서 삭제 나머지 삭제 한다
     *
     * @method sheetDelete
     * @date 2013. 11. 26.
     * @param
     * @return Workbook
     * @exception
     * @throws
     * @see
     */
    /* [SR150303-013][20150310]shcho, 용접조건표 갑지(2) 그림영역 삭제되는 오류 수정건으로 더이상 사용 안함.
    private Workbook sheetDelete(Workbook workbook)
    {
        int sheetTotalCount = workbook.getNumberOfSheets();
        List<String> sheetGroup = new ArrayList<String>();

        // sheet 이름을 추출한다
        for (int i = 0; i < sheetTotalCount; i++)
        {
            sheetGroup.add(workbook.getSheetName(i));
        }

        int sheetNO;
        // 추출한 sheet 이름을 가지고 비교하여 삭제한다
        String baseSheet = DEFAULT_BASE_SHEET;
        String secretSheet = DEFAULT_SECRET_SHEET;
        String systemSheet = DEFAULT_SYSTEM_SHEET;  //[SR140611-027][20140611] jwlee 용접조건표 을지 추가
        String mecoSheet = DEFAULT_SECRET_MECO_SHEET;

        for (String sheet : sheetGroup)
        {
            if (!sheet.equals(baseSheet) && !sheet.equals(secretSheet) && !sheet.startsWith("UserSheet") && !sheet.equals(mecoSheet) && !sheet.equals(systemSheet)) //[SR140611-027][20140611] jwlee 용접조건표 을지 추가
            {
                sheetNO = workbook.getSheetIndex(sheet);
                workbook.removeSheetAt(sheetNO);
            }
        }
        return workbook;
    }
    */

    /**
     * 정리된 Sheet 에 1번 Sheet 에 있는 값을 Clear 한다
     * [SR150303-013][20150310]shcho, 용접조건표 갑지(2) 그림영역 삭제되는 오류 수정
     * 
     * @method clearSheetData
     * @date 2013. 11. 26.
     * @param
     * @return Workbook
     * @exception
     * @throws
     * @see
     */
    private Workbook clearSheetData(Workbook workbook, int sheetIndex, boolean additionalDeleteFlag)
    {
        int headerTopIndex = DEFAULT_TOP_START_INDEX;
        int headerSrartIndex = DEFAULT_HEADER_START_INDEX;
        int topCell[] = DEFAULT_TOP_CELL;
        int headerCell[] = DEFAULT_HEADER_CELL;
        int weldListSize = DEFAULT_WELD_LIST_SIZE;
        int weldStartIndex = DEFAULT_WELD_START_INDEX;
        int weldCell[] = DEFAULT_WELD_CELL;
        int mecoListSize = DEFAULT_MECO_LIST_SIZE;
        int mecoStartIndex = DEFAULT_MECO_START_INDEX;
        int mecoCell[] = DEFAULT_MECO_CELL;

        Sheet sheet = workbook.getSheetAt(sheetIndex);

        // 기존 입력되어 있는 일련번호 와 작성일자를 Clear 한다
        clearRow(sheet, headerTopIndex, topCell);
        // 기존 등록되어 있는 해더 정보를 Clear 한다
        clearRow(sheet, headerSrartIndex, headerCell);

        if(additionalDeleteFlag) {
            // 기존 등록되어 있는 용접점 정보를 Clear 한다
            for(int i = 0; i < weldListSize; i++)
            {
                clearRow(sheet, weldStartIndex + (i * 2), weldCell);
            }
            // 기존 등록되어 있는 MECO 정보를 Clear 한다
            for(int i = 0; i < mecoListSize; i++)
            {
                clearRow(sheet, mecoStartIndex + i, mecoCell);
            }
        }
        
        return workbook;
    }


    /**
     *  이미 입력된 Row 를 삭제한다
     *
     * @method clearRow
     * @date 2013. 12. 4.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public static void clearRow(Sheet currentSheet, int rowIndex, int[] cellIndex) {
        Row row = currentSheet.getRow(rowIndex);

        for (int i = 0; i < cellIndex.length; i++)
        {
            Cell cell = row.getCell(cellIndex[i]);
            if( cell != null ) {
            	cell.setCellValue("");
            	
            }
        }
    }

}
