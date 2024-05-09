/**
 * 
 */
package com.symc.plm.me.sdv.operation.body;

import java.io.File;
import java.util.ArrayList;

import org.apache.poi.ss.usermodel.Workbook;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.ui.operation.AbstractSDVActionOperation;

import com.kgm.commands.ec.SYMCECConstant;
import com.kgm.common.utils.ProcessUtil;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.excel.common.PreviewWeldConditionSheetExcelHelper;
import com.symc.plm.me.sdv.excel.transformer.PreviewWeldConditionSheetExcelTransformer;
import com.symc.plm.me.sdv.operation.ps.ProcessSheetDataHelper;
import com.symc.plm.me.sdv.operation.wp.PreviewWeldConditionSheetDataHelper;
import com.symc.plm.me.sdv.operation.wp.PreviewWeldConditionSheetOpenOperation;
import com.symc.plm.me.utils.CustomUtil;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentChangeItemRevision;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.Registry;

/**
 * Class Name : UpdateWeldCondSheetActionOperation
 * Class Description :
 * [NON-SR][20160217] taeku.jeong, 용접조건표를 개정만 하는경우 개정이력이 기록되지 않는 경우가 있어 이를 해결하기위해 추가된 Operation
 * 
 * @date 2016. 02. 17.
 * 
 */
public class UpdateWeldCondSheetActionOperation extends AbstractSDVActionOperation {
	
    private Registry registry;
    private TCComponentBOMWindow bopWindow;
    private TCComponentBOMLine bopTopBOMLine;

    /**
     * @param actionId
     * @param ownerId
     * @param dataset
     */
    public UpdateWeldCondSheetActionOperation(int actionId, String ownerId, IDataSet dataset) {
        super(actionId, ownerId, dataset);

        registry = Registry.getRegistry(UpdateWeldCondSheetActionOperation.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.common.ISDVOperation#startOperation(java.lang.String)
     */
    @Override
    public void startOperation(String commandId) {

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.common.ISDVOperation#endOperation()
     */
    @Override
    public void endOperation() {

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.teamcenter.rac.aif.AbstractAIFOperation#executeOperation()
     */
    @Override
    public void executeOperation() throws Exception {
        IDataSet dataSet = getDataSet();

        TCComponentItem mecoItem = null;
        TCComponentChangeItemRevision mecoItemRevision =  null;
        TCSession session = CustomUtil.getTCSession();
        
        InterfaceAIFComponent targetComponentInterface = AIFUtility.getCurrentApplication().getTargetComponent();
        TCComponentBOPLine aBOPLine = null;
        if(targetComponentInterface!=null && targetComponentInterface instanceof TCComponentBOPLine){
        	aBOPLine = (TCComponentBOPLine)targetComponentInterface;
        	if(aBOPLine!=null){
        		this.bopWindow = aBOPLine.window();
        		if(this.bopWindow!=null){
        			this.bopTopBOMLine = this.bopWindow.getTopBOMLine();
        		}
        	}
        }
        
        Object meco_no = dataSet.getValue("mecoView", SDVPropertyConstant.SHOP_REV_MECO_NO);
        System.out.println("meco_no = "+meco_no);
        System.out.println("meco_no.getClass().getName() = "+meco_no.getClass().getName());
        if(meco_no!=null && meco_no instanceof String){
        	mecoItem = CustomUtil.findItem(SDVTypeConstant.MECO_ITEM, meco_no.toString());
        	if(mecoItem!=null){
        		mecoItemRevision = (TCComponentChangeItemRevision)mecoItem.getLatestItemRevision();
        	}
        }else if(meco_no!=null && meco_no instanceof TCComponentChangeItemRevision){
        	mecoItemRevision = (TCComponentChangeItemRevision)meco_no;
        }
        
        TCComponent[] solutionList = mecoItemRevision.getRelatedComponents(SYMCECConstant.SOLUTION_REL);
		for(TCComponent solutionItemComponent : solutionList){
			
			TCComponentItemRevision solutionItemrevision = (TCComponentItemRevision) solutionItemComponent;
			String solutionItemrevisionType = solutionItemrevision.getType();
			
			if(solutionItemrevisionType!=null && 
					solutionItemrevisionType.trim().equalsIgnoreCase(SDVTypeConstant.BOP_PROCESS_BODY_WELD_OPERATION_ITEM_REV)==true){

				ArrayList<TCComponent> weldConditionSheetDatasetList = ProcessUtil.getDatasets(solutionItemrevision, SDVTypeConstant.WELD_CONDITION_SHEET_RELATION);
				for(TCComponent dataset : weldConditionSheetDatasetList){
					boolean isExcel = false;
					boolean isReleased = false;
					
					if(dataset==null){
						continue;
					}
					
					if(dataset.getType().equals("MSExcelX")==true){
						isExcel = true;
					}
					if(isExcel==false){
						continue;
					}
					
					if(ProcessUtil.isWorkingStatus(dataset)==false){
						isReleased = true;
					}
					// Released 상태라도 Bypass 상태이면 계속 할 수 있도록 한다.
					if(isReleased==true){
						if(session.hasBypass()==false){
							continue;
						}
					}
					
					System.out.println("solutionItemrevision = "+solutionItemrevision);
					System.out.println("dataset = "+dataset);

					// Update 가능한 용접조건표 Dataset을 가진 Item Revision임
					updateWeldConditionSheet(solutionItemrevision, (TCComponentDataset)dataset);
				}
			}
			
		}
		
    }
    
    /**
     * WeldOperation의 경우 WeldOperation의 Item Id를 abs_occ_id로 설정 했으므로 이것을 이용해
     * BOPLine 객체를 찾아 올 수 있으므로 BOMWindow에서 해당 BOMLine을 찾아 Return하는 함수
     * @param weldOperationRevision
     * @return
     */
    private TCComponentBOPLine getTargetBOPLine(TCComponentItemRevision weldOperationRevision){
    	TCComponentBOPLine targetBOMLine = null;
    	
    	if(this.bopTopBOMLine==null){
    		return targetBOMLine;
    	}
    	
		boolean searchAllContext = true;
		TCComponentBOMLine[] assignedPartBOMLines = null;
		try {
			// Item Id를 bl_abs_occ_id에서 찾으면 된다.
			String blAbsOccId = weldOperationRevision.getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
			assignedPartBOMLines = this.bopWindow.findConfigedBOMLinesForAbsOccID(blAbsOccId, searchAllContext, this.bopTopBOMLine);
			for (int i = 0; i < assignedPartBOMLines.length; i++) {
				if(assignedPartBOMLines[i]!=null){
					targetBOMLine = (TCComponentBOPLine)assignedPartBOMLines[i];
				}
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
    	
    	return targetBOMLine;
    }
    
    /**
     * Weld Operation의 ItemRevision
     * @param weldOperationRevision
     * @param dataset
     */
    private void updateWeldConditionSheet(TCComponentItemRevision weldOperationRevision, TCComponentDataset dataset){
    	
    	TCComponentBOPLine targetBOMLine = getTargetBOPLine(weldOperationRevision);
    	
    	if(targetBOMLine==null){
    		return;
    	}
    	
    	System.out.println("weldOperationRevision = "+weldOperationRevision.toString());
    	
    	// [NON-SR][20150520] taeku.jeong Co2 용접 추가로 인해 용접 조건표 Type을 구분한다.
    	// SPOT_TYPE, CO2_TYPE, null 등으로 구분된다.
    	// 용접조건표 Type이 null인 경우 WeldOperation에 용접점 할당이 잘못 구분되어 할당된 것임.
    	String weldConditionSheetType = ProcessSheetDataHelper.getWeldConditionSheetType(weldOperationRevision);
    	
    	// Data Set의 엑셀 파일을 Export 한다.
    	// Session 이름을이용해 특정 폴더 경로를 생성한다.
    	File[] localfile = null;
		try {
			localfile = CustomUtil.exportDataset(dataset, weldOperationRevision.getSession().toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("weldConditionSheetType = "+weldConditionSheetType);
		if(localfile!=null && localfile[0]!=null){
			System.out.println("localfile[0].getPath() = "+localfile[0].getPath());
		}else{
			System.out.println("localfile is null");
		}
    	
		// Work Book 객체를 생성한다.
    	Workbook workbook = null;
		try {
			PreviewWeldConditionSheetOpenOperation wpExcelOpen = new PreviewWeldConditionSheetOpenOperation();
			workbook = wpExcelOpen.redefinition(targetBOMLine, localfile[0], weldConditionSheetType);
		} catch (Exception e) {
			e.printStackTrace();
		}

    	// 용접조건표 Update 에 필요한 Data를 기록한 IDataSet 객체를 생성한다.
    	PreviewWeldConditionSheetDataHelper wpExcelData = new PreviewWeldConditionSheetDataHelper(weldConditionSheetType);
    	wpExcelData.setUserDefineTarget(targetBOMLine);
    	IDataSet weldCondData = null;
		try {
			weldCondData = wpExcelData.getDataSet();
		} catch (Exception e) {
			e.printStackTrace();
		}

    	PreviewWeldConditionSheetExcelTransformer transFormer = new PreviewWeldConditionSheetExcelTransformer();
    	// 현재 Data를 기준으로 용접 조건표를 Update 한다. 
    	transFormer.print(localfile[0], workbook, weldCondData, weldConditionSheetType);
    	
    	System.out.println("localfile[0].getPath() = "+localfile[0].getPath());
    	
        // 내용 Update 된 엑셀 파일을 팀센터에 Import 한다
    	PreviewWeldConditionSheetExcelHelper.importExcel(weldOperationRevision, localfile[0]);
		// 5. Import 한 로컬파일은 삭제한다
		PreviewWeldConditionSheetExcelHelper.deleteLocalFile(localfile[0]);
    }

}
