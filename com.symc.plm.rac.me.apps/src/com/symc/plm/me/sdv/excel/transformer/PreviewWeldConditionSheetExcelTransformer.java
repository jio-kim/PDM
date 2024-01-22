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
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;

import com.symc.plm.me.common.SDVPropertyConstant;

/**
 * [SR140611-027][20140611] jwlee 용접조건표 을지 추가
 * [SR140702-044][20140702] shcho 용접공법 ID체계 변경에 따른 용접조건표 시트 수정
 * [SR140902-070][201408011] shcho, 용접조건표 시트에 추가된 옵션 셀의 값을, SYSTEM에서 자동으로 입력 하도록 기능 추가
 * [SR150303-013][20150310] shcho, 용접조건표 갑지(2) 그림영역 삭제되는 오류 수정
 * [SR150708-014][20150709] shcho, 용접조건표의 Header에 옵션 기입란이 추가되었기에 기존 비고란에 시스템에 의한 용접공법의 옵션 기입 불필요.
 */
public class PreviewWeldConditionSheetExcelTransformer {

    protected final static int DEFAULT_WELD_START_INDEX = 40;
    protected final static int DEFAULT_MECO_START_INDEX = 87;
    protected final static int DEFAULT_MECO_LIST_START_INDEX = 2;
    protected final static int DEFAULT_CELL_CNT = 6;
    protected final static int DEFAULT_MECO_LIST_SIZE = 5;

    protected final static int[] DEFAULT_MECO_CELL = {0, 2, 5, 9, 17, 19, 21, 23, 26, 30, 38, 40};
    
    private String weldConditionSheetType;
    
    public static Workbook initWorkBook(File file, String weldConditionSheetType){
    	
    	Workbook workbook = null;
    	if(file!=null && file.exists()){
    		try {
				workbook = new XSSFWorkbook(new FileInputStream(file));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}else{
    		System.out.println("PreviewWeldConditionSheetExcelTransformer.initWorkBook : FIle 없음.");
    	}
    	
    	// Sheet Number 확인
    	int sheetNumber = workbook.getNumberOfSheets();

		int sportTemplateSheetIndex = -1;
		int sportCopySheetTemplateSheetIndex = -1;
		int sportSystemTemplateSheetIndex = -1;
		
		int co2TemplateSheetIndex = -1;
		int co2CopySheetTemplateSheetIndex = -1;
		int co2SystemTemplateSheetIndex = -1;

		
		for (int i = 0; i < sheetNumber; i++) {
			
			String currentSheetName = workbook.getSheetName(i);
			
			if(currentSheetName!=null && currentSheetName.trim().equalsIgnoreCase("SPOT")){
				sportTemplateSheetIndex = i;
			}
			
			if(currentSheetName!=null && currentSheetName.trim().equalsIgnoreCase("COPYSHEET")){
				sportCopySheetTemplateSheetIndex = i;
			}
			
			if(currentSheetName!=null && currentSheetName.trim().equalsIgnoreCase("SYSTEMSHEET")){
				sportSystemTemplateSheetIndex = i;
			}
			
			if(currentSheetName!=null && currentSheetName.trim().equalsIgnoreCase("CO2")){
				co2TemplateSheetIndex = i;
			}
			
			if(currentSheetName!=null && currentSheetName.trim().equalsIgnoreCase("COPYSHEET_CO2")){
				co2CopySheetTemplateSheetIndex = i;
			}
			
			if(currentSheetName!=null && currentSheetName.trim().equalsIgnoreCase("SYSTEMSHEET_CO2")){
				co2SystemTemplateSheetIndex = i;
			}
		}
		
		boolean isFirst = false;
		if(sheetNumber==7 &&
			sportTemplateSheetIndex > -1 && sportCopySheetTemplateSheetIndex >-1 && sportSystemTemplateSheetIndex > -1 &&
			co2TemplateSheetIndex > -1 && co2CopySheetTemplateSheetIndex > -1 && co2SystemTemplateSheetIndex > -1 ){
			isFirst = true;
		}
		
		System.out.println("weldConditionSheetType = "+weldConditionSheetType);
		System.out.println("isFirst = "+isFirst);
		
		if(isFirst==true){
			
	    	if(weldConditionSheetType!=null && weldConditionSheetType.trim().equalsIgnoreCase("SPOT_TYPE")==true){
	    		
	    		System.out.println("Case SPOT_TYPE");
	    		
	    		workbook = deleteNamedSheet(workbook, "Co2");
	    		workbook = deleteNamedSheet(workbook, "copySheet_Co2");
	    		workbook = deleteNamedSheet(workbook, "systemSheet_Co2");
	    		
	    		workbook = reNamedSheet(workbook, "Spot", "1");
	    	}else if(weldConditionSheetType!=null && weldConditionSheetType.trim().equalsIgnoreCase("CO2_TYPE")==true){
	    		
	    		System.out.println("Case CO2_TYPE");
	    		
	    		workbook = deleteNamedSheet(workbook, "Spot");
	    		workbook = deleteNamedSheet(workbook, "copySheet");
	    		workbook = deleteNamedSheet(workbook, "systemSheet");
	    		
	    		workbook = reNamedSheet(workbook, "Co2", "1");
	    		workbook = reNamedSheet(workbook, "copySheet_Co2", "copySheet");
	    		workbook = reNamedSheet(workbook, "systemSheet_Co2", "systemSheet");
	    	}
	    	
	    	// 아직은 이 함수가 제대로 동작하지 않는다
	    	// 참 이상하네... 원인을 찾는데 시간이 많이 걸릴듯...
	    	// [NON-SR][20160621] taeku.jeong
	    	workbook = setHiddenSheets(workbook);
	    	
    		FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(file);
				workbook.write(fos);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}finally{
				try {
					fos.flush();
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
    		try {
				workbook = new XSSFWorkbook(new FileInputStream(file));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
	    	
		}
    	
    	return workbook;
    }
    
    
    private static Workbook deleteNamedSheet(Workbook workbook, String SheetName){
    	
    	int sheetNumber = workbook.getNumberOfSheets();
    	for (int i = 0; i < sheetNumber; i++) {
    		String currentSheetName = workbook.getSheetName(i);
    		if(currentSheetName!=null && currentSheetName.trim().equalsIgnoreCase(SheetName.trim())==true){
    			workbook.removeSheetAt(i);
    			break;
    		}
		}
    	return workbook;
    }
    
    private static Workbook reNamedSheet(Workbook workbook, String oldSheetName, String newSheetName){
    	
    	int sheetNumber = workbook.getNumberOfSheets();
    	for (int i = 0; i < sheetNumber; i++) {
    		String currentSheetName = workbook.getSheetName(i);
    		if(currentSheetName!=null && currentSheetName.trim().equalsIgnoreCase(oldSheetName.trim())==true){
    			workbook.setSheetName(i, newSheetName);
    			break;
    		}
		}
    	return workbook;
    }
    

    /**
     * 해당 데이타셋을 출력한다
     *
     * @method print
     * @date 2013. 12. 4.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void print(File file, Workbook workbook, IDataSet dataSet, String weldConditionSheetType) {
    	
    	if(file==null){
    		System.out.println("print.file  = null");
    	}else{
    		System.out.println("print.file  = "+file.getPath());
    	}
    	System.out.println("print.weldConditionSheetType = "+weldConditionSheetType);
    	if(dataSet==null){
    		System.out.println("print.dataSet  = null");
    	}else{
    		System.out.println("print.dataSet.getMapCount  = "+dataSet.getMapCount());
    	}
    	
    	this.weldConditionSheetType = weldConditionSheetType;
    	
        try {
            // 신규 생성한 용접조건표에 WorkBook 을 가져온다
            if (workbook == null){
            	// [NON-SR][20160524] taeku.joeng 
            	//workbook = new XSSFWorkbook(new FileInputStream(file));
            	workbook = PreviewWeldConditionSheetExcelTransformer.initWorkBook(file, weldConditionSheetType);
            }

            List<Sheet> currentSheet = new ArrayList<Sheet>();
            List<HashMap<String, Object>> weldDataList = getTableData(dataSet, "weldList");
            List<HashMap<String, Object>> mecoDataList = getTableData(dataSet, "mecoList");
            
            IDataMap dataMap = dataSet.getDataMap("weldCondSheetInfo");
            // 용접공법에 적용된 욥션을 가져온다
            String option = dataMap.getStringValue("optionDescription");

            int rowSize = weldDataList.size();
            // 용접점 갯수가 20개 이상이면 20개 마다 sheet 를 추가로 생성한다
            int sheetQuantity = (int) Math.ceil((double)rowSize/20);
            //int copySheetIndex = workbook.getSheetIndex("systemSheet"); //[SR140611-027][20140611] jwlee 용접조건표 을지 추가
            if (sheetQuantity > 1)
            {
                for (int i = 0; i < sheetQuantity; i++) {
                    String sheetName = String.valueOf(i+1);
                    int sheetIndex = workbook.getSheetIndex(sheetName);
                    if(sheetIndex < 0) {
                        Sheet sheet = workbook.cloneSheet(workbook.getSheetIndex("systemSheet"));
                        workbook.setSheetName(workbook.getNumberOfSheets()-1, sheetName);
                        // setSheetOrder  ==> Sheet 의 순서를 지정하는 메서드 이 메서드 때문에 용접 조건표의 순서가 1,2 에서 2,1 순서로 나옴
                        // 윤순식 부장님 요청으로 순서를 2,1 에서 1,2 로 변경 하기 위해 메서드 사용 X 근데 왜 저렇게 나오는지는 나도 모르겠음
//                        workbook.setSheetOrder(sheetName, i);
                        currentSheet.add(sheet);
                    } else {
                        currentSheet.add(workbook.getSheet(sheetName));
                    }
                }
                /* [SR150303-013][20150310]shcho, 용접조건표 갑지(2) 그림영역 삭제되는 오류 수정으로 인하여 주석처리
                int oneSheetOrder = workbook.getSheetIndex("1");    //[SR140611-027][20140611] jwlee 용접조건표 을지 추가
                int mainSheet = 1;                                               //[SR140611-027][20140611] jwlee 용접조건표 을지 추가
                for (int i = 1; i < sheetQuantity; i++)
                {
                    copySheetIndex = workbook.getSheetIndex("systemSheet");
                    Sheet sheet = workbook.cloneSheet(copySheetIndex);
                    workbook.setSheetName((sheetTotalCount - 1) + i, Integer.toString(i + 1));
                    workbook.setSheetOrder(Integer.toString(mainSheet + 1), oneSheetOrder + 1); //[SR140611-027][20140611] jwlee 용접조건표 을지 추가
                    currentSheet.add(sheet);
                    mainSheet++;
                    oneSheetOrder = workbook.getSheetIndex("" + mainSheet);
                }
                */
            } else {
                //[NON-SR][20150327] shcho, 용접조건표 Open시 java.lang.NullPointerException 나는 오류 수정
                currentSheet.add(workbook.getSheet("1"));
            }
            // MECO 가 10개가 넘어갈경우 MECOList Sheet 추가 하고 List 를 출력한다
            if (mecoDataList.size() > 10)
            {
                int mecoSheetIndex = workbook.getSheetIndex("MECO_List");
                Sheet mecoSheet = workbook.cloneSheet(mecoSheetIndex);
                workbook.setSheetName((workbook.getNumberOfSheets() - 1), "MECOList");
                for (int mecoListRow = 0; mecoListRow < mecoDataList.size(); mecoListRow++)
                {
                    mecoListPrintRow(mecoSheet, mecoListRow, mecoDataList.get(mecoListRow));
                }
                setBorderAndAlign(workbook, mecoSheet);
            }

            // 새로생성한 sheet 갯수에 맞게 용접점을 출력한다 (20개 마다 출력)
            int rowSize2 = 0;
            for (int k = 0; k < currentSheet.size(); k++)
            {
                printHeaderInfo(workbook, currentSheet.get(k), dataMap);
                for(int j = 0; j < 20; j++)
                {
                    if (rowSize == rowSize2)
                        break;
                    printRow(currentSheet.get(k), rowSize2, j, weldDataList.get(rowSize2), option);
                    rowSize2++;
                }
            }

            // MECO List 를 출력한다
            for (int mecoRow = 0; mecoRow < mecoDataList.size(); mecoRow++)
            {
                if (mecoDataList.size() < 11)
                {
                    mecoPrintRow(currentSheet.get(0), mecoRow, mecoDataList.get(mecoRow));
                }
                else
                {
                    if (mecoRow == 0)
                        mecoPrintRow(currentSheet.get(0), mecoRow, mecoDataList.get(mecoRow));
                    else
                        mecoPrintRow(currentSheet.get(0), mecoRow, mecoDataList.get(mecoDataList.size() - (10 - mecoRow)));
                }

                if (mecoRow == 9)
                    break;
            }

            // copySheet 의 해더정보를 입력해 둔다
            printHeaderInfo(workbook, workbook.getSheet("copySheet"), dataMap);
            
	    	// 아직은 이 함수가 제대로 동작하지 않는다
	    	// 참 이상하네... 원인을 찾는데 시간이 많이 걸릴듯...
	    	// [NON-SR][20160621] taeku.jeong
            workbook = setHiddenSheets(workbook);

            FileOutputStream fos = new FileOutputStream(file);
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
     * 아직은 이 함수가 제대로 동작하지 않는다
	 * 참 이상하네... 원인을 찾는데 시간이 많이 걸릴듯...
	 * Commit은 하지 않았음.
	 * [NON-SR][20160621] taeku.jeong
     */
    private static Workbook setHiddenSheets(Workbook workbook){
    	
    	int sheetCount = workbook.getNumberOfSheets();
    	for (int i = 0; i < sheetCount; i++) {
    		
    		String sheetName = workbook.getSheetName(i);
    		
    		if(sheetName!=null && sheetName.trim().length()>0){
    			if( sheetName.trim().equalsIgnoreCase("copySheet")
					|| sheetName.trim().equalsIgnoreCase("systemSheet")
					|| sheetName.trim().equalsIgnoreCase("copySheet_Co2")
					|| sheetName.trim().equalsIgnoreCase("systemSheet_Co2")
					){
    				workbook.setSheetHidden(i, true);
    				System.out.println("Hidden SheetName --> " + sheetName);
    			}
    		}
    		
		}
    	
    	return workbook;
    }

    /**
     * 용접점 List 를 가져온다
     *
     * @method getTableData
     * @date 2013. 12. 5.
     * @param
     * @return List<HashMap<String,Object>>
     * @exception
     * @throws
     * @see
     */
    private List<HashMap<String, Object>> getTableData(IDataSet dataSet, String listName)
    {
        Collection<HashMap<String, Object>> data = null;

        IDataMap dataMap = null;
        
        if(dataSet!=null){
        	dataMap = dataSet.getDataMap(listName);
        }
        if(dataMap != null) {
            data = dataMap.getTableValue(listName);
        }
        return (List<HashMap<String, Object>>) data;
    }


    /**
     * 용접점 아이템을 한개씩 출력한다.
     *
     * @method printRow
     * @date 2013. 10. 28.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void printRow(Sheet currentSheet, int num, int rowNum, HashMap<String, Object> dataMap, String option)
    {
        int weldStartIndex = DEFAULT_WELD_START_INDEX;
        Row row = currentSheet.getRow(weldStartIndex + (rowNum * 2));

        // 순번
        Cell cell = row.getCell(0);
        cell.setCellValue((num + 1));

        // 용접점 ID
        cell = row.getCell(2);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.BL_OCCURRENCE_NAME));

        // 겹수
        cell = row.getCell(8);
        //cell.setCellValue((Integer) dataMap.get("MfgNumber"));
//        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.WELD_NUMBER_OF_SHEETS));
        // 실제겹수와 마스터정보를 비교하여 다르면 둘다 표시
        String connectedLength = (dataMap.get("MfgNumber")).toString().trim();
        String numberSheet = (dataMap.get(SDVPropertyConstant.WELD_NUMBER_OF_SHEETS)).toString().trim();
        if (connectedLength.equals(numberSheet)) {
            cell.setCellValue(numberSheet);
        }else{
            cell.setCellValue(connectedLength + "(" + numberSheet + ")");
        }

        // 재질 및 두께
        cell = row.getCell(10);
        cell.setCellValue((String) dataMap.get("MaterialThickness"));

        // 계열
        cell = row.getCell(33);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.BL_WELD_NOTE_LINE));

        // 가압력
        cell = row.getCell(35);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.BL_WELD_NOTE_PRESSURIZATION));

        // 비교 (BOMLine 저장된 값이 있으면 BOMLine 을 보여주고 없으면 용접공법에 할당된 Option 을 보여준다)
        //[SR150708-014][20150709] shcho, 용접조건표의 Header에 옵션 기입란이 추가되었기에 기존 비고란에 시스템에 의한 용접공법의 옵션 기입 불필요.
        cell = row.getCell(37);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.BL_WELD_NOTE_ETC));

    }

    /**
     * MECOList (10개 미만)
     *
     * @method printRow
     * @date 2013. 10. 28.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public static void mecoPrintRow(Sheet currentSheet, int rowNum, HashMap<String, Object> dataMap)
    {
        int mecoStartIndex = DEFAULT_MECO_START_INDEX;
        int mecoCell[] = DEFAULT_MECO_CELL;
        int rightNum = 0;
        int rightCell = 0;

        // 5개가 넘어가면 오른쪽 Cell 로 다시 셋팅한다
        if (rowNum > 4)
        {
            rightNum = 5;
            rightCell = 6;
        }

        Row row = currentSheet.getRow(mecoStartIndex - (rowNum - rightNum));

        // 변경
        Cell cell = row.getCell(mecoCell[(0 + rightCell)]);
        cell.setCellValue((String) dataMap.get("changeNo"));

        // 일자
        cell = row.getCell(mecoCell[(1 + rightCell)]);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.ITEM_DATE_RELEASED));

        // MECO ID
        cell = row.getCell(mecoCell[(2 + rightCell)]);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.ITEM_ITEM_ID));

        // 변경내용
        cell = row.getCell(mecoCell[(3 + rightCell)]);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.ITEM_OBJECT_DESC));

        // 담당
        cell = row.getCell(mecoCell[(4 + rightCell)]);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.ITEM_OWNING_USER));

        // 팀장
        cell = row.getCell(mecoCell[(5 + rightCell)]);
        cell.setCellValue((String) dataMap.get("APPR"));

    }

    /**
     * MECOList (10개 이상) (추가한 MECO List 에 추가)
     *
     * @method printRow
     * @date 2013. 10. 28.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public static void mecoListPrintRow(Sheet currentSheet, int rowNum, HashMap<String, Object> dataMap)
    {
        Row row = currentSheet.createRow(2 + rowNum);

        // 변경
        Cell cell = row.createCell(0);
        cell.setCellValue((String) dataMap.get("changeNo"));

        // 일자
        cell = row.createCell(1);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.ITEM_DATE_RELEASED));

        // MECO ID
        cell = row.createCell(2);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.ITEM_ITEM_ID));

        // 변경내용
        cell = row.createCell(3);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.ITEM_OBJECT_DESC));

        // 담당
        cell = row.createCell(4);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.ITEM_OWNING_USER));

        // 팀장
        cell = row.createCell(5);
        cell.setCellValue((String) dataMap.get("APPR"));
    }

    /**
     * Excel Header 정보 출력
     *
     * [SR140702-044][20140702] shcho 용접공법 ID체계 변경에 따른 용접조건표 시트 수정
     * [SR140902-070][201408011] shcho, 용접조건표 시트에 추가된 옵션 셀의 값을, SYSTEM에서 자동으로 입력 하도록 기능 추가
     *
     * @method printHeaderInfo
     * @date 2013. 11. 18.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void printHeaderInfo(Workbook workbook, Sheet currentSheet, IDataMap dataMap)
    {
        // 일련번호
        Row row = currentSheet.getRow(1);
        Cell cell = row.getCell(32);
        cell.setCellValue(dataMap.getStringValue("compID") + "\r\n" + dataMap.getStringValue("revID"));

        // Product Code
        row = currentSheet.getRow(3);
        cell = row.getCell(3);
        cell.setCellValue(dataMap.getStringValue("productCode"));

        // Line Code
        cell = row.getCell(11);
        cell.setCellValue(dataMap.getStringValue("lineCode"));

        // Station Code
        cell = row.getCell(19);
        cell.setCellValue(dataMap.getStringValue("stationCode"));

        // Robot
        cell = row.getCell(27);
        cell.setCellValue(dataMap.getStringValue("robotWorkArea"));

        // Gun
        cell = row.getCell(35);
        cell.setCellValue(dataMap.getStringValue("gunNO"));
        
        // 옵션 (Weld Operation Option Code Description)
        // [SR140902-070][201408011] shcho, 용접조건표 시트에 추가된 옵션 셀의 값을, SYSTEM에서 자동으로 입력 하도록 기능 추가      
        row = currentSheet.getRow(5);
        cell = row.getCell(3);
        cell.setCellValue(dataMap.getStringValue("weldOptionCodeDescription"));
    }

    /**
     * MECOList 에 Style 을 준다
     *
     * @method setBorderAndAlign
     * @date 2013. 12. 11.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public static void setBorderAndAlign(Workbook workbook, Sheet currentSheet)
    {
        // 스타일
        CellStyle style = workbook.createCellStyle();
        CellStyle style1 = workbook.createCellStyle();
        CellStyle style2 = workbook.createCellStyle();
        CellStyle style3 = workbook.createCellStyle();

        Font koFont = workbook.createFont();
        koFont.setFontName("돋움");
        koFont.setFontHeightInPoints((short)10);

        style.setFont(koFont);
        style.setBorderTop(XSSFCellStyle.BORDER_THIN);
        style.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        style.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        style.setBorderRight(XSSFCellStyle.BORDER_THIN);

        style1.setFont(koFont);
        style1.setBorderTop(XSSFCellStyle.BORDER_THIN);
        style1.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        style1.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        style1.setBorderRight(XSSFCellStyle.BORDER_THIN);
        style1.setAlignment(XSSFCellStyle.ALIGN_CENTER);

        Font enFont = workbook.createFont();
        enFont.setFontName("Arial");
        enFont.setFontHeightInPoints((short)10);

        style2.setFont(enFont);
        style2.setBorderTop(XSSFCellStyle.BORDER_THIN);
        style2.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        style2.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        style2.setBorderRight(XSSFCellStyle.BORDER_THIN);

        style3.setFont(enFont);
        style3.setBorderTop(XSSFCellStyle.BORDER_THIN);
        style3.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        style3.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        style3.setBorderRight(XSSFCellStyle.BORDER_THIN);
        style3.setAlignment(XSSFCellStyle.ALIGN_CENTER);

        // 가운데 정렬 column
        int[] center_columnArray = new int[]{0, 1, 2, 3, 4, 5};
        ArrayList<Integer> center_columnList = new ArrayList<Integer>();
        for(int i = 0; i < center_columnArray.length; i++)
        {
            center_columnList.add(center_columnArray[i]);
        }

        Row row;
        int lastRowNum = currentSheet.getLastRowNum();
        int mecoListStartIndex = DEFAULT_MECO_LIST_START_INDEX;
        int CellCnt = DEFAULT_CELL_CNT;
        for(int i = mecoListStartIndex; i <= lastRowNum; i++)
        {
            row = currentSheet.getRow(i);
            for(int j = 0; j < CellCnt; j++)
            {
                if(center_columnList.contains(j))
                {
                    if (j == 2)
                        row.getCell(j).setCellStyle(style3);
                    else
                        row.getCell(j).setCellStyle(style1);
                }
                else
                {
                    if (j == 2)
                        row.getCell(j).setCellStyle(style2);
                    else
                        row.getCell(j).setCellStyle(style);
                }
            }
        }
    }

    /**
     * MECO 결재 진행시 Page 처리를 한다
     *
     * @method setPageNumber
     * @date 2013. 12. 20.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public static void setPageNumber(Sheet currentSheet, int totalNO, int pageNO )
    {
        Row row = currentSheet.getRow(89);
        Cell cell = row.getCell(1);
        cell.setCellValue(Integer.toString(pageNO) + " / " + Integer.toString(totalNO));
    }

}
