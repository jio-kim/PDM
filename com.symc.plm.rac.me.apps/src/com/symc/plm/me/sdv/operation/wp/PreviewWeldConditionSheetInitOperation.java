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

import com.kgm.common.utils.SYMTcUtil;
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

        // ������ BOP�� �����ۿ� Type �� �������� ���� üũ�Ѵ�
        if (selectedTarget.getItem().getType().equals(SDVTypeConstant.BOP_PROCESS_BODY_WELD_OPERATION_ITEM))
        {
            TCComponentItemRevision weOPItemRevision = null;
            weOPItemRevision = selectedTarget.getItemRevision();
            // ������ �������� Revision ���� dataset �߿� excel ����(��������ǥ)�� �����ϴ��� Ȯ���ϰ� ������ �����
            wpDataset = getWeldExcelPath(session, weOPItemRevision);
            
            if(wpDataset!=null){
                targetDataMap.put("exceltemplate", wpDataset, IData.OBJECT_FIELD);
                targetDataset.addDataMap("weldExcelView", targetDataMap);

                // ���� ���� �Է��� ������ DataSet �� TCComponentDataset �����Ѵ�
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
     * ���������� ��������ǥ�� ��/�� �� Ȯ���ϰ� ������ ������ ������ �о����
     * ������ Dataset �� ���� �Ѵ�
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

        // [NON-SR][20150520] taeku.jeong Co2 ���� �߰��� ���� ���� ����ǥ Type�� �����Ѵ�.
        // SPOT_TYPE, CO2_TYPE, null ������ ���еȴ�.
        // ��������ǥ Type�� null�� ��� WeldOperation�� ������ �Ҵ��� �߸� ���еǾ� �Ҵ�� ����.
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

        // ���� ��������ǥ File �� �о� �´�
        if (datasets.size() > 0)
        {
            PreviewWeldConditionSheetOpenOperation wpExcelOpen = new PreviewWeldConditionSheetOpenOperation();
            // ���� ���� ����ǥ(Excel Dataset)�� ���� �����ϰ� ���������� BOMView ������ ���� �������� ���Ѵ�
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

                // 1.���� ���� ����ǥ�� ����Ÿ�� ����� ���� �ʿ� (�Ҵ�� �������� ������ �о�ͼ� �ý��ۿ��� �߰��� Sheet�� ���Ѵ�)
                Workbook workbook = wpExcelOpen.redefinition(selectedTarget, localfile[0], weldConditionSheetType);
                // 2. ��������ǥ�� ä�� ���� Data�� �����Ѵ�
                weldCondData = wpExcelData.getDataSet();
                // 3. ����Ÿ�� ������ ����Ѵ�
                transFormer.print(localfile[0], workbook, weldCondData, weldConditionSheetType);
                // 4. Update�� ���� ������ �����Ϳ� Import �Ѵ�
                PreviewWeldConditionSheetExcelHelper.importExcel(localfile[0]);
                // 5. Import �� ���������� �����Ѵ�
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
        // ���� ��������ǥ �� �����Ƿ� Template ���� dataset ������ �����ͼ� ���� ���� ���� dataset �� �����Ѵ�
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
 
            	// ������ �ʿ��� ���Ͽ� �̸��� �����Ѵ�
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
                
                // 1.������ �ʿ��� �����͸� �����´�
                weldCondData = wpExcelData.getDataSet();
                // 2.���Ӱ� �߰��� ����Ÿ�� ������ ����Ѵ�
                transFormer.print(loFile, null, weldCondData, weldConditionSheetType);
                // 3. �Էµ� ���뿡 ���� ���Ϸ� �����Ϳ� Import �Ѵ�
                PreviewWeldConditionSheetExcelHelper.importExcel(loFile);
                // 4. Import �� ���������� �����Ѵ�
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
     * BOMView �� ��������ǥ �����ͼ¿� ���� �������� ���Ͽ� BOMView �����ϰ� ���ų� ��ũ��
     * true �� ��ȯ �Ѵ�
     * [SR140704-002][20140703] shcho, ��������ǥ Update ���� üũ�� �ð����� ���� (BOMView�������� 5���̳� ������ŭ �ʰ� ����Ǵ°� ���)
     * 1. ���� ���θ� üũ
     * 2. ������� üũ
     * 3. �����ͼ¿� ���� �����ϰ� BOMView �� �������� ���Ͽ� BOMView �������� 5�� �̻� �� �ʰ� ������ ���¶�� ��������ǥ�� ������Ʈ �Ѵ�.
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
            // ������ WeldOP �� BOMView Revision Ÿ���� �����´�
            TCComponent[] bomViewTypes = selectedTarget.getReferenceListProperty(SDVTypeConstant.BOMLINE_RELATION);
            // ������ WeldOP �� �������� Dataset �� �����´�
            Vector<TCComponentDataset> datasets = new Vector<TCComponentDataset>();
            datasets = CustomUtil.getDatasets(selectedTarget, SDVTypeConstant.WELD_CONDITION_SHEET_RELATION, TcDefinition.DATASET_TYPE_EXCELX);
            if (!CustomUtil.isWritable(datasets.get(0))) return false;
            // [NON-SR][20160125] taeku.jeong ���� Operatoin�� ������ ���� ó�� �������� ��� ��������ǥ�� �ѹ� update �ǵ��� �Ѵ�. 
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
                    // �߰��� m7_WELD_NAME_LAST_MOD_DATE ��¥�� �ռ��ٸ� �ڿ� ������ ���� �ʾƵ� ��
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
     * �������� ������ �ѹ��� Dataset Update�� �������� Ȯ�� �ϴ� Function
     * @param selectedTarget
     * @return
     * @throws Exception
     */
    private boolean isDatasetFirstUpdateTarget(TCComponentItemRevision selectedTarget) throws Exception {
    	
    	boolean isDatasetFirstUpdateTarget = false;
    	
    	TCComponent[] releaseStatusList = selectedTarget.getReferenceListProperty(SDVPropertyConstant.ITEM_REV_RELEASE_STATUS_LIST);
    	
    	// Dataset�� Createion Date�� Last Modify Date�� ���� ū���� List �Ѵ�.
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
        
        // Dataset�� Time Gap�� 1/1000�� �̹Ƿ� �̸� �������� Gap �ð��� ����ؼ� Updaet �� �ȵ� ����̸� Update�� �ѹ� �Ͼ� ������ �Ѵ�.
        // Dataset�� Named Reference�� ���� �Ǵ� ��쿡 Dataset�� ������ �����ð��� ���� �Ǵ��� Ȯ�� �ؾ� �Ѵ�.
        if(maxGapTime / 1000 < 10){
        	// Dataset �����ϰ� �������� ���̰� 10�� ���� ������ Update �����.
        	isDatasetFirstUpdateTarget = true;
        }
        
        return isDatasetFirstUpdateTarget;
    }

}
