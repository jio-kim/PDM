package com.symc.plm.me.sdv.excel.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.kgm.common.utils.SYMTcUtil;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.excel.transformer.PreviewWeldConditionSheetExcelTransformer;
import com.symc.plm.me.sdv.operation.ps.ProcessSheetDataHelper;
import com.symc.plm.me.utils.CustomUtil;
import com.symc.plm.me.utils.TcDefinition;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.kernel.TCException;

public class PreviewWeldConditionSheetExcelHelper {

    protected final static int DEFAULT_WELD_START_INDEX = 40; // weldStartIndex = 40;
    protected final static int DEFAULT_WELD_LIST_SIZE = 20; // weldListSize = 20;

//	/**
//     *  넘겨진 Excel File 에 workbook 을 가져온다
//     *
//     * @method getWorkbook
//     * @date 2013. 11. 26.
//     * @param
//     * @return Workbook
//     * @throws IOException
//     * @exception
//     * @throws
//     * @see
//     */
//    public static Workbook getWorkbook(File file) throws IOException
//    {
//        Workbook workbook = null;
//        InputStream is = new FileInputStream(file);
//        workbook = new XSSFWorkbook(is);
//        
//        
//
//        return workbook;
//    }

    /**
     * ---Teamcenter Update 버튼 클릭----
     * 1. 로컬파일을 Dataset 에 Update
     * 2. Excel파일에 입력된 용접점에 계열/가압력/비고 란을 읽어서 용접점 Note 에 저장 (View 에서 처리 변경)
     *
     * @method updateTeamcenter
     * @date 2013. 12. 5.
     * @param
     * @return void
     * @throws TCException
     * @exception
     * @throws
     * @see
     */
    public static void updateTeamcenter(File file, TCComponentDataset targetDataset) throws IOException, TCException
    {
        /**
         * sheet 내용 추출 및 용접점 Note 저장
         */
        //Workbook workbook = getWorkbook(file);
    	
    	Workbook workbook = null;
    	
    	String weldConditionSheetType = null;
        if(targetDataset!=null){

        	TCComponentItemRevision weldOpRevision = null;
        	String[] relationNames = new String[]{
        			SDVTypeConstant.WELD_CONDITION_SHEET_RELATION
				};
        	AIFComponentContext[] relations = targetDataset.whereReferencedByTypeRelation(null, relationNames);
        	for (int i = 0; relations!=null && i < relations.length; i++) {
        		if(relations[i].getComponent()!=null && relations[i].getComponent() instanceof TCComponentItemRevision){
        			String itemRevType = relations[i].getComponent().getType();
        			if(itemRevType!=null && itemRevType.trim().equalsIgnoreCase(SDVTypeConstant.BOP_PROCESS_BODY_WELD_OPERATION_ITEM_REV)==true){
        				weldOpRevision = (TCComponentItemRevision)relations[i].getComponent();
        			}
        		}
			}
        	
        	if(weldOpRevision!=null){
        		weldConditionSheetType = ProcessSheetDataHelper.getWeldConditionSheetType(weldOpRevision);
        	}
        }
        
        if(weldConditionSheetType!=null){
        	workbook = PreviewWeldConditionSheetExcelTransformer.initWorkBook(file, weldConditionSheetType);
        }else{
          InputStream is = new FileInputStream(file);
          workbook = new XSSFWorkbook(is);
        }

        // Sheet 에 입력한 용접점에 계열/가압력/비고 를 추출한다
        HashMap<String, ArrayList<Object>> weldData = new HashMap<String, ArrayList<Object>>();
        weldData = getWeldRowData(workbook);

        // weldData 의 데이터를 용접점 Note 속성의 저장한다
        if (weldData != null)
            setWeldNoteData(weldData);
    }

    /**
     * 용접조건표에 사용자가 입력한 계열/가압력/비교 값을 Map 에 저장한다
     *
     * @method getWeldRowData
     * @date 2013. 12. 6.
     * @param
     * @return HashMap<String,ArrayList<Object>>
     * @exception
     * @throws
     * @see
     */
    public static HashMap<String, ArrayList<Object>> getWeldRowData(Workbook workbook)
//    private static HashMap<String, ArrayList<Object>> getWeldRowData(Workbook workbook)
    {
        HashMap<String, ArrayList<Object>> weldDataList = new HashMap<String, ArrayList<Object>>();
        // System Sheet 를 체크한다
        int totalSystemSheet = systemSheetCheck(workbook);

        Row row;
        Cell cell;
        Object weldID;
        Object noteLine;
        Object notePressurization;
        Object noteEtc;
        // int cellType = 0;
        int weldListSize = DEFAULT_WELD_LIST_SIZE;
        int weldStartIndex = DEFAULT_WELD_START_INDEX;
        for (int i = 1; i < (totalSystemSheet + 1); i++)
        {
            Sheet systemSheet = workbook.getSheet(i + "");
            for (int j = 0; j < weldListSize; j++)
            {
                ArrayList<Object> dataList = new ArrayList<Object>();
                row = systemSheet.getRow(weldStartIndex + (j * 2));

                cell = row.getCell(2);
                weldID = getCellValue(cell);

                // 입력된 값이 없으면 중지한다
                if (weldID.equals(""))
                    break;

                cell = row.getCell(33);
                noteLine = getCellValue(cell);
                dataList.add((String)noteLine);

                cell = row.getCell(35);
                notePressurization = getCellValue(cell);
                dataList.add((String)notePressurization);

                cell = row.getCell(37);
                noteEtc = getCellValue(cell);
                dataList.add((String)noteEtc);

                weldDataList.put((String) weldID, dataList);
            }
        }
        return weldDataList;
    }

    /**
     * cell 에 Type 에 따라 형변환을 하여 저장한다 (String / Numeric)
     *
     * @method getCellValue
     * @date 2013. 12. 10.
     * @param
     * @return Object
     * @exception
     * @throws
     * @see
     */
    private static Object getCellValue(Cell cell)
    {
        Object resultObject;
        int cellType = cell.getCellType();

        if (cellType == 0)
            resultObject = Double.toString(cell.getNumericCellValue());
        else
            resultObject = cell.getStringCellValue();

        return resultObject;
    }

    /**
     * 용접점 아이템 BOMLine note 값에 엑셀 추출 값을 저장한다
     *
     * @method setWeldNoteData
     * @date 2013. 12. 6.
     * @param
     * @return void
     * @throws TCException
     * @exception
     * @throws
     * @see
     */
    private static void setWeldNoteData(HashMap<String, ArrayList<Object>> weldDataList) throws TCException
    {
        TCComponentBOPLine weldOP = (TCComponentBOPLine)getTarget();
        ArrayList<Object> dataList = new ArrayList<Object>();

        AIFComponentContext[] weldOPChilds = weldOP.getChildren();
        for (AIFComponentContext weldOPChild : weldOPChilds)
        {
            TCComponentBOPLine weldItem = (TCComponentBOPLine)weldOPChild.getComponent();
            if (weldItem.getItem().getType().equals(SDVTypeConstant.BOP_BODY_WELD_POINT_ITEM))
            {
                String weldOccName = weldItem.getProperty(SDVPropertyConstant.BL_OCCURRENCE_NAME);
                dataList = weldDataList.get(weldOccName);
                
                if(dataList!=null){
                	weldItem.setProperty(SDVPropertyConstant.BL_WELD_NOTE_LINE, (String) dataList.get(0));
                	weldItem.setProperty(SDVPropertyConstant.BL_WELD_NOTE_PRESSURIZATION, (String) dataList.get(1));
                	weldItem.setProperty(SDVPropertyConstant.BL_WELD_NOTE_ETC, (String) dataList.get(2));
                	weldItem.save();
                }
                
            }
        }
    }



    /**
     * 숫자로 시작하는 System이 생성한 Sheet 를 체크한다
     * (숫자로 시작 예) 1, 2, 3.......)
     *
     * @method systemSheetCheck
     * @date 2013. 12. 6.
     * @param
     * @return int
     * @exception
     * @throws
     * @see
     */
    public static int systemSheetCheck(Workbook workbook)
    {
        boolean numberCheckflag = false;
        int sheetNum = 0;

        for (int i = 0; i < workbook.getNumberOfSheets(); i++)
        {
            numberCheckflag = Pattern.matches("^[0-9]*$", workbook.getSheetName(i).substring(0, 1));
            // sheet 이름이 숫자로 시작하면 갯수를 체크한다
            if (numberCheckflag)
                sheetNum += 1;
        }
        return sheetNum;
    }

    /**
     *  SAVE 버튼을 눌렀을때 팀센터로 Import 시킨다
     *
     * @method copyFile
     * @date 2013. 11. 13.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    public static void importExcel(File file)
    {
        TCComponentBOPLine target = (TCComponentBOPLine) getTarget();
        Vector<TCComponentDataset> vData = new Vector<TCComponentDataset>();
        Vector<File> vFile = new Vector<File>();
        
        try {
           	vData = CustomUtil.getDatasets(target.getItemRevision(), SDVTypeConstant.WELD_CONDITION_SHEET_RELATION, TcDefinition.DATASET_TYPE_EXCELX);
            vFile.add(file);
            CustomUtil.removeAllNamedReference(vData.get(0));
            SYMTcUtil.importFiles(vData.get(0), vFile);
            vData.get(0).refresh();
        } catch (TCException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     *  SAVE 버튼을 눌렀을때 팀센터로 Import 시킨다
     *
     * @method copyFile
     * @date 2013. 11. 13.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    public static void importExcel(TCComponentItemRevision itemRevision, File file)
    {
        Vector<TCComponentDataset> vData = new Vector<TCComponentDataset>();
        Vector<File> vFile = new Vector<File>();
        
        try {
           	vData = CustomUtil.getDatasets(itemRevision, SDVTypeConstant.WELD_CONDITION_SHEET_RELATION, TcDefinition.DATASET_TYPE_EXCELX);	
            vFile.add(file);
            CustomUtil.removeAllNamedReference(vData.get(0));
            SYMTcUtil.importFiles(vData.get(0), vFile);
            vData.get(0).refresh();
        } catch (TCException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *  다이얼로그를 닫을때 로컬 파일을 삭제 한다
     *
     * @method deleteLocalFile
     * @date 2013. 11. 15.
     * @param
     * @return boolean
     * @exception
     * @throws
     * @see
     */
    public static boolean deleteLocalFile(File file)
    {
        boolean result = false;
        result = file.delete();
        return result;
    }

    /**
    *
    *
    * @method getTarget
    * @date 2013. 11. 15.
    * @param
    * @return TCComponentBOMLine
    * @exception
    * @throws
    * @see
    */
   public static TCComponentBOMLine getTarget()
   {
       TCComponentBOMLine target = null;
       AbstractAIFUIApplication abstractaifuiapplication = AIFDesktop.getActiveDesktop().getCurrentApplication();

       AIFComponentContext aaifcomponentcontext[] = abstractaifuiapplication.getTargetContexts();

       if(aaifcomponentcontext != null && aaifcomponentcontext.length == 1 && (aaifcomponentcontext[0].getComponent() instanceof TCComponentBOMLine))
       {
           target = (TCComponentBOMLine)aaifcomponentcontext[0].getComponent();
           return target;
       }
       return null;
   }

   /**
    * 용접공법 타입인지 체크한다.
    *
    * @method isOperation
    * @date 2013. 11. 22.
    * @param
    * @return boolean
    * @exception
    * @throws
    * @see
    */
   public static boolean isWeldOperation(TCComponentBOPLine bopLine) throws TCException {
       String type = bopLine.getItem().getType();
       if (SDVTypeConstant.BOP_PROCESS_BODY_WELD_OPERATION_ITEM.equals(type) || SDVTypeConstant.BOP_PROCESS_PAINT_OPERATION_ITEM.equals(type) || SDVTypeConstant.BOP_PROCESS_ASSY_OPERATION_ITEM.equals(type) || SDVTypeConstant.BOP_PROCESS_OPERATION_ITEM.equals(type)) {
           return true;
       }

       return false;
   }

   /**
    * 공정 타입인지 체크한다.
    *
    * @method isStation
    * @date 2013. 11. 22.
    * @param
    * @return boolean
    * @exception
    * @throws
    * @see
    */
   public static boolean isStation(TCComponentBOPLine bopLine) throws TCException {
       String type = bopLine.getItem().getType();
       if (SDVTypeConstant.BOP_PROCESS_STATION_ITEM.equals(type)) {
           return true;
       }

       return false;
   }

   /**
    * 공법 타입인지 체크한다.
    *
    * @method isOperation
    * @date 2013. 11. 22.
    * @param
    * @return boolean
    * @exception
    * @throws
    * @see
    */
   public static boolean isOperation(TCComponentBOPLine bopLine) throws TCException {
       String type = bopLine.getItem().getType();
       if (SDVTypeConstant.BOP_PROCESS_BODY_OPERATION_ITEM.equals(type) || SDVTypeConstant.BOP_PROCESS_PAINT_OPERATION_ITEM.equals(type) || SDVTypeConstant.BOP_PROCESS_ASSY_OPERATION_ITEM.equals(type) || SDVTypeConstant.BOP_PROCESS_OPERATION_ITEM.equals(type)) {
           return true;
       }

       return false;
   }

   /**
    * Shop 타입인지 체크한다.
    *
    * @method isShop
    * @date 2013. 11. 22.
    * @param
    * @return boolean
    * @exception
    * @throws
    * @see
    */
   public static boolean isShop(TCComponentBOPLine bopLine) throws TCException {
       String type = bopLine.getItem().getType();
       if (SDVTypeConstant.BOP_PROCESS_SHOP_ITEM.equals(type)) {
           return true;
       }

       return false;
   }

   /**
    * Line 타입인지 체크한다.
    *
    * @method isLine
    * @date 2013. 11. 22.
    * @param
    * @return boolean
    * @exception
    * @throws
    * @see
    */
   public static boolean isLine(TCComponentBOPLine bopLine) throws TCException {
       String type = bopLine.getItem().getType();
       if (SDVTypeConstant.BOP_PROCESS_LINE_ITEM.equals(type)) {
           return true;
       }

       return false;
   }

   /**
    * 용접조건표 엑셀 파일 로컬 다운로드
    *
    * @method downloadProcessSheet
    * @date 2013. 12. 19.
    * @param
    * @return void
    * @exception
    * @throws
    * @see
    */
   public static void downloadProcessSheet(TCComponentItemRevision revision, String filePath, String fileName) throws TCException {
       TCComponent comp = revision.getRelatedComponent(SDVTypeConstant.WELD_CONDITION_SHEET_RELATION);
       if(comp != null && comp instanceof TCComponentDataset) {
           TCComponentDataset dataSet = (TCComponentDataset) comp;
           TCComponentTcFile tcFile = dataSet.getTcFiles()[0];
           if(tcFile != null) {
               tcFile.getFile(filePath, fileName);
           }
       }
   }

}
