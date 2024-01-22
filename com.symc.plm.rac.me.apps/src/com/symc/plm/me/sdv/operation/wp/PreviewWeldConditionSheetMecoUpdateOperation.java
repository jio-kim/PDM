package com.symc.plm.me.sdv.operation.wp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.excel.common.PreviewWeldConditionSheetExcelHelper;
import com.symc.plm.me.sdv.excel.transformer.PreviewWeldConditionSheetExcelTransformer;
import com.symc.plm.me.sdv.operation.ps.ProcessSheetDataHelper;
import com.symc.plm.me.utils.CustomUtil;
import com.symc.plm.me.utils.SYMTcUtil;
import com.symc.plm.me.utils.TcDefinition;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCSession;


public class PreviewWeldConditionSheetMecoUpdateOperation {


    /**
     * MECO 완료 시점에 배치를 통해 용접공법이 Release 되기전에 MECO 정보를 Update 한다
     * 1. MECO ItemRevision 을 가져온다
     * 2. MECO ItemRevision 하위 Solution Folder 에서 WeldOperation 만 가져온다
     * 3. WeldOperation 에 연결되어 있는 상위 MECO 정보도 가져온다
     * 4. WeldOperation 에 있는 용접조건표 데이터셋 File 로컬폴더에 Export 한 후 Workbook 을 추출하여 수정
     * 5. 수정한 용접조건표 다시 Import 후 로컬 파일을 삭제한다
     *
     * @method setLastMecoInfo
     * @date 2013. 12. 16.
     * @param
     * @return boolean
     * @throws Exception
     * @exception
     * @throws
     * @see
     */
    public static void setLastMecoInfo(String mecoID) throws Exception
    {
        TCSession session = CustomUtil.getTCSession();

        // 넘어온 MECO ID 로 MECO ItemRevision 을 가져온다
        TCComponentItem mecoItem = SYMTcUtil.findItem(session, mecoID);
        TCComponentItemRevision mecoItemRevision = mecoItem.getLatestItemRevision();
        

        // MECO ItemRevision 하위
        List<TCComponentItemRevision> weldOPList = PreviewWeldConditionSheetDataHelper.getMecoTargetList(mecoItemRevision, SDVTypeConstant.MECO_SOLUTION_ITEM, SDVTypeConstant.BOP_PROCESS_BODY_WELD_OPERATION_ITEM_REV);

        // MECO List 를 가져와서 MECO 상세정보를 저장한다
        for (TCComponentItemRevision weldOP : weldOPList)
        {
            // MECO 데이터를 추출한다
            List<HashMap<String, Object>> mecoDataList = new ArrayList<HashMap<String, Object>>();
            List<String> mecoList = new ArrayList<String>();
            mecoList.add(mecoID);
            
            String weldConditionSheetType = ProcessSheetDataHelper.getWeldConditionSheetType(weldOP);
            
            mecoList = PreviewWeldConditionSheetDataHelper.getMecoList(weldOP, mecoList);
            mecoDataList = PreviewWeldConditionSheetDataHelper.getMecoInfoList(mecoList);
            // WeldOPRevision 하위 Dataset 에 용접조건표 데이터셋 에서 File 을 가져온다
            File file = getFile(weldOP, session);
            // 가져온 file 에서 Workbook을 추출하여 기존 Sheet 에 입력된 MECO 삭제한다
            //Workbook workbook = PreviewWeldConditionSheetExcelHelper.getWorkbook(file);
            Workbook workbook = PreviewWeldConditionSheetExcelTransformer.initWorkBook(file, weldConditionSheetType);
            workbook = deleteMecoInfo(workbook);
            // 정리된 Workbook 에 Meco 정보를 Update 한다
            workbook = setMecoData(workbook, mecoDataList);
            // MECO 최종 결재일을 시스템 Sheet 에 추가하고 페이지수를 입력 시킨다
            file = setPageNO(file, workbook);

            // 정리가 끝난 file을 Teamcenter Dataset 에 Import 한 후 삭제한다
            importDatasetFile(weldOP, file);
            PreviewWeldConditionSheetExcelHelper.deleteLocalFile(file);
        }
    }

    /**
     * 용접공법에서 File 을 가져온다
     *
     * @method getFile
     * @date 2013. 12. 17.
     * @param
     * @return File
     * @exception
     * @throws
     * @see
     */
    private static File getFile(TCComponentItemRevision weldOpRevision, TCSession session) throws Exception
    {
        Vector<TCComponentDataset> datasets = new Vector<TCComponentDataset>();
        datasets = CustomUtil.getDatasets(weldOpRevision, TcDefinition.TC_SPECIFICATION_RELATION, TcDefinition.DATASET_TYPE_EXCELX);
        File[] localfile = null;
        localfile = CustomUtil.exportDataset(datasets.get(0), session.toString());
        return localfile[0];
    }

    /**
     * 기존 등록되어 있는 MECOSheet 삭제후 1번 sheet 에 저장된 MECO 정보를 저장한다
     *
     * @method deleteMecoInfo
     * @date 2013. 12. 17.
     * @param
     * @return Workbook
     * @exception
     * @throws
     * @see
     */
    private static Workbook deleteMecoInfo(Workbook workbook)
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
        String mecoSheet = "MECOList";

        for (String sheet : sheetGroup)
        {
            if (sheet.equals(mecoSheet))
            {
                sheetNO = workbook.getSheetIndex(sheet);
                workbook.removeSheetAt(sheetNO);
            }
        }

        //BaseSheet 에 있는 Meco 정보를 Clear 한다
        String baseSheet = PreviewWeldConditionSheetOpenOperation.DEFAULT_BASE_SHEET;
        int mecoListSize = PreviewWeldConditionSheetOpenOperation.DEFAULT_MECO_LIST_SIZE;
        int mecoStartIndex = PreviewWeldConditionSheetOpenOperation.DEFAULT_MECO_START_INDEX;
        int mecoCell[] = PreviewWeldConditionSheetOpenOperation.DEFAULT_MECO_CELL;

        int originalNO = workbook.getSheetIndex(baseSheet);
        Sheet currentSheet = workbook.getSheetAt(originalNO);

        // 기존 등록되어 있는 MECO 정보를 Clear 한다
        for(int i = 0; i < mecoListSize; i++)
        {
            PreviewWeldConditionSheetOpenOperation.clearRow(currentSheet, mecoStartIndex + i, mecoCell);
        }
        return workbook;
    }

    /**
     * MECO 정보를 입력하고 10개가 넘어갈때에는 Sheet 를 추가하여 모두 출력한다
     *
     * @method setMecoData
     * @date 2013. 12. 17.
     * @param
     * @return void
     * @throws IOException
     * @exception
     * @throws
     * @see
     */
    private static Workbook setMecoData(Workbook workbook, List<HashMap<String, Object>> mecoDataList)
    {
        int sheetTotalCount = workbook.getNumberOfSheets();

        // MECO 가 10개가 넘어갈경우 MECOList Sheet 추가 하고 List 를 출력한다
        if (mecoDataList.size() > 10)
        {
            int mecoSheetIndex = workbook.getSheetIndex("MECO_List");
            Sheet mecoSheet = workbook.cloneSheet(mecoSheetIndex);
            sheetTotalCount = workbook.getNumberOfSheets();
            workbook.setSheetName((sheetTotalCount - 1), "MECOList");
            for (int mecoListRow = 0; mecoListRow < mecoDataList.size(); mecoListRow++)
            {
                PreviewWeldConditionSheetExcelTransformer.mecoListPrintRow(mecoSheet, mecoListRow, mecoDataList.get(mecoListRow));
            }
            PreviewWeldConditionSheetExcelTransformer.setBorderAndAlign(workbook, mecoSheet);
        }

        // MECO List 를 출력한다
        int systemSheet = workbook.getSheetIndex("1");
        for (int mecoRow = 0; mecoRow < mecoDataList.size(); mecoRow++)
        {
            PreviewWeldConditionSheetExcelTransformer.mecoPrintRow(workbook.getSheetAt(systemSheet), mecoRow, mecoDataList.get(mecoRow));
            if (mecoRow == 9)
                break;
        }

        return workbook;
    }

    private static File setPageNO(File file, Workbook workbook) throws IOException
    {
        int totalSheet = workbook.getNumberOfSheets();
        int pageNO = 1;
        for (int i = 0; i < totalSheet; i++)
        {
            Sheet currentSheet = workbook.getSheetAt(i);
            if (!currentSheet.getSheetName().equals("MECO_List") && !currentSheet.getSheetName().equals("copySheet"))
            {
                PreviewWeldConditionSheetExcelTransformer.setPageNumber(currentSheet, (totalSheet - 2), pageNO);
                pageNO++;
            }
        }

        FileOutputStream fos = new FileOutputStream(file);
        workbook.write(fos);
        fos.flush();
        fos.close();

        return file;
    }

    /**
     * 타겟 WeldOP 에 용접조건표 Dataset 에 기존 파일을 삭제하고 수정한 파일을 Import 한다
     *
     * @method importDatasetFile
     * @date 2013. 12. 17.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private static void importDatasetFile(TCComponentItemRevision itemRevision, File file) throws Exception
    {
        Vector<TCComponentDataset> vData = new Vector<TCComponentDataset>();
        Vector<File> vFile = new Vector<File>();

        vData = CustomUtil.getDatasets(itemRevision, TcDefinition.TC_SPECIFICATION_RELATION, TcDefinition.DATASET_TYPE_EXCELX);
        vFile.add(file);
        CustomUtil.removeAllNamedReference(vData.get(0));
        SYMTcUtil.importFiles(vData.get(0), vFile);
    }

}
