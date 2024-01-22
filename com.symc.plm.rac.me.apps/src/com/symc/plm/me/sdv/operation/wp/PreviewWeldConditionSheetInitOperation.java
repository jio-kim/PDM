package com.symc.plm.me.sdv.operation.wp;

import java.io.File;
import java.util.Date;
import java.util.Vector;

import org.apache.poi.ss.usermodel.Workbook;
import org.sdv.core.common.data.DataSet;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.common.data.RawDataMap;
import org.sdv.core.ui.operation.AbstractSDVInitOperation;

import com.ssangyong.common.utils.SYMTcUtil;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVStringUtiles;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.excel.common.ExcelTemplateHelper;
import com.symc.plm.me.sdv.excel.common.PreviewWeldConditionSheetExcelHelper;
import com.symc.plm.me.sdv.excel.transformer.PreviewWeldConditionSheetExcelTransformer;
import com.symc.plm.me.sdv.operation.ps.ProcessSheetDataHelper;
import com.symc.plm.me.utils.CustomUtil;
import com.symc.plm.me.utils.TcDefinition;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;

public class PreviewWeldConditionSheetInitOperation extends AbstractSDVInitOperation {

    private TCComponentDataset wpDataset;
    private TCComponentBOPLine selectedTarget;

    private IDataSet weldCondData;

    //ProgressBar progressBar;


    @Override
    public void executeOperation() throws Exception
    {
        //progressBar = new ProgressBar(AIFUtility.getActiveDesktop().getShell());
        //progressBar.start();

        selectedTarget = (TCComponentBOPLine) getTarget();
        final TCSession session = CustomUtil.getTCSession();

        DataSet targetDataset = new DataSet();
        RawDataMap targetDataMap = new RawDataMap();

        // 선택한 BOP에 아이템에 Type 은 용접공법 인지 체크한다
        if (selectedTarget.getItem().getType().equals(SDVTypeConstant.BOP_PROCESS_BODY_WELD_OPERATION_ITEM))
        {
            TCComponentItemRevision weOPItemRevision = null;
            weOPItemRevision = selectedTarget.getItemRevision();
            // 선택한 용접공법 Revision 하위 dataset 중에 excel 파일(용접조건표)이 존재하는지 확인하고 없으면 만든다
            wpDataset = getWeldExcelPath(session, weOPItemRevision);
            
            if(wpDataset!=null){
                targetDataMap.put("exceltemplate", wpDataset, IData.OBJECT_FIELD);
                targetDataset.addDataMap("weldExcelView", targetDataMap);

                // 현재 까지 입력한 내용의 DataSet 을 TCComponentDataset 저장한다
                setData(targetDataset);

                //progressBar.close();
            	
            }else{
            	String messageString = "Check whether the allocation welding points.";
            	MessageBox.post(AIFUtility.getActiveDesktop().getShell(), 
            			messageString, 
            			"Error", 
            			MessageBox.ERROR);
            	throw new Exception(messageString);
            }

        }
    }

    /**
     * 용접공법에 용접조건표에 유/무 를 확인하고 있으면 기존에 정보를 읽어오고
     * 없으면 Dataset 을 생성 한다
     *
     * @method cteateWeldExcel
     * @date 2013. 11. 12.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    private TCComponentDataset getWeldExcelPath(TCSession session, TCComponentItemRevision selectedItemRevision) throws Exception
    {
        Vector<TCComponentDataset> datasets = new Vector<TCComponentDataset>();
        datasets = CustomUtil.getDatasets(selectedItemRevision, SDVTypeConstant.WELD_CONDITION_SHEET_RELATION, TcDefinition.DATASET_TYPE_EXCELX);
        File[] localfile = null;

        // [NON-SR][20150520] taeku.jeong Co2 용접 추가로 인해 용접 조건표 Type을 구분한다.
        // SPOT_TYPE, CO2_TYPE, null 등으로 구분된다.
        // 용접조건표 Type이 null인 경우 WeldOperation에 용접점 할당이 잘못 구분되어 할당된 것임.
        String weldConditionSheetType = ProcessSheetDataHelper.getWeldConditionSheetType(selectedItemRevision);
        
        if(weldConditionSheetType==null || (weldConditionSheetType!=null && weldConditionSheetType.trim().length()<1)){
        	return (TCComponentDataset)null;
        }
        
        System.out.println("weldConditionSheetType = "+weldConditionSheetType);
        if(datasets!=null){
        	System.out.println("datasets.size() = "+datasets.size());	
        }else{
        	System.out.println("datasets = null");
        }

        PreviewWeldConditionSheetDataHelper wpExcelData = new PreviewWeldConditionSheetDataHelper(weldConditionSheetType);
        PreviewWeldConditionSheetExcelTransformer transFormer = new PreviewWeldConditionSheetExcelTransformer();

        // 기존 용접조건표 File 을 읽어 온다
        if (datasets.size() > 0)
        {
            PreviewWeldConditionSheetOpenOperation wpExcelOpen = new PreviewWeldConditionSheetOpenOperation();
            // 기존 용접 조건표(Excel Dataset)의 최종 수정일과 용접공법에 BOMView 아이템 최종 수정일을 비교한다
            if (checkWeldSheetData(selectedItemRevision))
            {
                System.out.println("=================WeldConditionSheet Update=================");
                for (TCComponentDataset tcComponentDataset : datasets)
                {
                    wpDataset = tcComponentDataset;
                    localfile = CustomUtil.exportDataset(tcComponentDataset, session.toString());
                }
                wpDataset = datasets.get(0);
                
                if(selectedTarget==null){
                	System.out.println("selectedTarget = null");
                }else{
                	System.out.println("selectedTarget = "+selectedTarget.toString());
                }
                
                if(localfile==null){
                	System.out.println("localfile = null");
                }else{
                	System.out.println("localfile = "+localfile.length);
                	System.out.println("localfile[0].getPath() = "+localfile[0].getPath());
                }

                // 1.기존 용접 조건표에 데이타를 지우는 로직 필요 (할당된 용접점에 갯수를 읽어와서 시스템에서 추가된 Sheet를 비교한다)
                Workbook workbook = wpExcelOpen.redefinition(selectedTarget, localfile[0], weldConditionSheetType);
                // 2. 용접조건표에 채워 넣을 Data를 추출한다
                weldCondData = wpExcelData.getDataSet();
                // 3. 데이타를 엑셀에 출력한다
                transFormer.print(localfile[0], workbook, weldCondData, weldConditionSheetType);
                // 4. Update된 엑셀 파일을 팀센터에 Import 한다
                PreviewWeldConditionSheetExcelHelper.importExcel(localfile[0]);
                // 5. Import 한 로컬파일은 삭제한다
                PreviewWeldConditionSheetExcelHelper.deleteLocalFile(localfile[0]);

                return wpDataset;
            }
            else
            {
                System.out.println("=================WeldConditionSheet No Update=================");
                wpDataset = datasets.get(0);

                return wpDataset;
            }
        }
        // 기존 용접조건표 가 없으므로 Template 에서 dataset 정보를 가져와서 용접 공법 하위 dataset 을 생성한다
        else
        {
            System.out.println("=================WeldConditionSheet Create=================");
            
            
            TCComponentItem documentItem = CustomUtil.findItem("Document", "ME_DOCTEMP_11");
            TCComponentItemRevision documentItemRevision = SYMTcUtil.getLatestReleasedRevision(documentItem);
            
            System.out.println("documentItemRevision = "+documentItemRevision.toString());
            datasets = CustomUtil.getDatasets(documentItemRevision, "TC_Attaches", TcDefinition.DATASET_TYPE_EXCELX);

            if(datasets==null){
            	System.out.println("datasets = null");
            }else{
            	System.out.println("datasets.size() = "+datasets.size());
            }
            
            
            if(selectedTarget==null){
            	System.out.println("selectedTarget(B) = null");
            }else{
            	System.out.println("selectedTarget(B) = "+selectedTarget.toString());
            }
            
            //for (TCComponentDataset tcComponentDataset : datasets)
            for (int i = 0;datasets!=null && i < datasets.size(); i++) {
            	
            	TCComponentDataset tcComponentDataset = datasets.get(i);
 
            	// 생성시 필요한 파일에 이름을 정의한다
                String targetName = selectedTarget.getProperty(SDVPropertyConstant.BL_ITEM_ID);
                String fileName = targetName + ExcelTemplateHelper.getToday("yyyy-MM-dd");
                
				System.out.println("tcComponentDataset = "+tcComponentDataset);
				System.out.println("fileName = "+fileName);

                localfile = CustomUtil.exportDataset(tcComponentDataset, session.toString());
                
				if(localfile == null){
					System.out.println("localfile = null");
				}else{
					System.out.println("localfile = "+localfile.length);
				}

                File loFile = CustomUtil.renameFile(localfile[0], fileName);
                Vector<File> vFile = new Vector<File>();
                vFile.add(loFile);

				if(loFile==null){
					System.out.println("loFile = null");
				}else{
					System.out.println("loFile = "+loFile.getPath());
				}
                
                wpDataset = CustomUtil.createPasteDataset(selectedItemRevision, targetName, "", TcDefinition.DATASET_TYPE_EXCELX, SDVTypeConstant.WELD_CONDITION_SHEET_RELATION);

            	System.out.println("wpDataset = "+wpDataset);
                
                // 1.생성시 필요한 데이터를 가져온다
                weldCondData = wpExcelData.getDataSet();
                // 2.새롭게 추가한 데이타를 엑셀에 출력한다
                transFormer.print(loFile, null, weldCondData, weldConditionSheetType);
                // 3. 입력된 내용에 엑셀 파일로 팀센터에 Import 한다
                PreviewWeldConditionSheetExcelHelper.importExcel(loFile);
                // 4. Import 한 로컬파일은 삭제한다
                PreviewWeldConditionSheetExcelHelper.deleteLocalFile(loFile);

                return wpDataset;
            }
        }
        return wpDataset = null;
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
    public TCComponentBOMLine getTarget()
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
     * BOMView 와 용접조건표 데이터셋에 최종 수정일을 비교하여 BOMView 수정일과 같거나 더크면
     * true 를 반환 한다
     * [SR140704-002][20140703] shcho, 용접조건표 Update 여부 체크시 시간오차 수정 (BOMView수정일이 5초이내 범위만큼 늦게 저장되는것 허용)
     * 1. 결재 여부를 체크
     * 2. 쓰기권한 체크
     * 3. 데이터셋에 최종 수정일과 BOMView 의 수정일을 비교하여 BOMView 리비전이 5초 이상 더 늦게 수정된 상태라면 용접조건표를 업데이트 한다.
     *
     * @method checkWeldSheet
     * @date 2013. 11. 25.
     * @param
     * @return boolean
     * @throws Exception
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings("unused")
    private boolean checkWeldSheetData(TCComponentItemRevision selectedTarget) throws Exception
    {
        TCComponent[] releaseStatusList = selectedTarget.getReferenceListProperty(SDVPropertyConstant.ITEM_REV_RELEASE_STATUS_LIST);
        if (releaseStatusList.length == 0)
        {
            Date weldLastModDate = selectedTarget.getDateProperty(SDVPropertyConstant.WELDOP_REV_LAST_MOD_DATE);
            // 선택한 WeldOP 에 BOMView Revision 타입을 가져온다
            TCComponent[] bomViewTypes = selectedTarget.getReferenceListProperty(SDVTypeConstant.BOMLINE_RELATION);
            // 선택한 WeldOP 에 용접공법 Dataset 을 가져온다
            Vector<TCComponentDataset> datasets = new Vector<TCComponentDataset>();
            datasets = CustomUtil.getDatasets(selectedTarget, SDVTypeConstant.WELD_CONDITION_SHEET_RELATION, TcDefinition.DATASET_TYPE_EXCELX);
            if (!CustomUtil.isWritable(datasets.get(0))) return false;
            // [NON-SR][20160125] taeku.jeong 용접 Operatoin을 개정한 이후 처음 보여지는 경우 용접조건표를 한번 update 되도록 한다. 
            boolean isFirstUpdateTarget = isDatasetFirstUpdateTarget(selectedTarget);
            if(isFirstUpdateTarget==true){
            	return true;
            }
            for (TCComponent bomViewType : bomViewTypes)
            {
                if (bomViewType.getType().equals(SDVTypeConstant.BOMLINE_ITEM_REVISION))
                {
                    Date bomViewLastDate = bomViewType.getDateProperty(SDVPropertyConstant.ITEM_LAST_MODIFY_DATE);
//                    File dataSetLastDates = datasets.get(0).getFile(null, "35-B4-305N-00-WEOP-02_WeldCondition_2014-01-23.6.xlsx");
//                    dataSetLastDates.lastModified();
//                    Date dataSetLastDate = new Date(dataSetLastDates.lastModified());
                    Date dataSetLastDate = datasets.get(0).getDateProperty(SDVPropertyConstant.ITEM_LAST_MODIFY_DATE);
                    Long compare = (long)bomViewLastDate.compareTo(dataSetLastDate);

                    String bomViewStringDate = SDVStringUtiles.dateToString(bomViewLastDate, "yyyyMMddHHmmss");
                    String dataSetStringDate = SDVStringUtiles.dateToString(dataSetLastDate, "yyyyMMddHHmmss");
                    Long bomViewLongDate = Long.parseLong(bomViewStringDate);
                    Long dataSetLongDate = Long.parseLong(dataSetStringDate);
                    // 추가한 m7_WELD_NAME_LAST_MOD_DATE 날짜가 앞선다면 뒤에 검증은 하지 않아도 됨
                    if (weldLastModDate != null)
                    {
                        String weldOPStringDate = SDVStringUtiles.dateToString(weldLastModDate, "yyyyMMddHHmmss");
                        Long weldOPLongDate = Long.parseLong(weldOPStringDate);
                        Long compareWeldOpLongResult = dataSetLongDate - weldOPLongDate;
                        if (compareWeldOpLongResult < Long.parseLong("-5"))
                        {
                            return true;
                        }
                    }

                    Long compareLongResult = dataSetLongDate - bomViewLongDate;
                    if (compareLongResult < Long.parseLong("-5"))
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    /**
     * 용접공법 개정후 한번도 Dataset Update가 없었는지 확인 하는 Function
     * @param selectedTarget
     * @return
     * @throws Exception
     */
    private boolean isDatasetFirstUpdateTarget(TCComponentItemRevision selectedTarget) throws Exception {
    	
    	boolean isDatasetFirstUpdateTarget = false;
    	
    	TCComponent[] releaseStatusList = selectedTarget.getReferenceListProperty(SDVPropertyConstant.ITEM_REV_RELEASE_STATUS_LIST);
    	
    	// Dataset의 Createion Date와 Last Modify Date의 차가 큰것을 List 한다.
        Vector<TCComponentDataset> datasets = new Vector<TCComponentDataset>();
        datasets = CustomUtil.getDatasets(selectedTarget, SDVTypeConstant.WELD_CONDITION_SHEET_RELATION, TcDefinition.DATASET_TYPE_EXCELX);
        long maxGapTime = 0;
        for (int i = 0; datasets!=null && i < datasets.size(); i++) {
        	Date currentDataSetDate = datasets.get(i).getDateProperty(SDVPropertyConstant.ITEM_LAST_MODIFY_DATE);
        	
            Date creationDate = datasets.get(i).getDateProperty(SDVPropertyConstant.ITEM_CREATION_DATE);
            Date modifyDate = datasets.get(i).getDateProperty(SDVPropertyConstant.ITEM_LAST_MODIFY_DATE);
            
            long dateGap = modifyDate.getTime() - creationDate.getTime();
            if(maxGapTime < dateGap){
            	maxGapTime=dateGap;
            }
		}
        
        // Dataset의 Time Gap은 1/1000초 이므로 이를 기준으로 Gap 시간을 계산해서 Updaet 가 안된 경우이면 Update가 한번 일어 나도록 한다.
        // Dataset의 Named Reference가 변경 되는 경우에 Dataset의 마지막 수정시간이 변경 되는지 확인 해야 한다.
        if(maxGapTime / 1000 < 10){
        	// Dataset 생성일과 수정일의 차이가 10초 보다 작으면 Update 대상임.
        	isDatasetFirstUpdateTarget = true;
        }
        
        return isDatasetFirstUpdateTarget;
    }

}
