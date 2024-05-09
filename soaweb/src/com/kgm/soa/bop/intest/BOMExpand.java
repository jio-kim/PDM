package com.kgm.soa.bop.intest;

import java.util.ArrayList;
import java.util.Vector;

import com.kgm.soa.bop.util.BasicSoaUtil;
import com.kgm.soa.bop.util.LogFileUtility;
import com.kgm.soa.bop.util.MPPTopLines;
import com.kgm.soa.bop.util.MppUtil;
import com.teamcenter.soa.client.Connection;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.strong.BOMLine;
import com.teamcenter.soa.client.model.strong.BOMWindow;
import com.teamcenter.soa.client.model.strong.MECollaborationContext;
import com.teamcenter.soa.client.model.strong.Mfg0BvrProcess;
import com.teamcenter.soa.client.model.strong.Mfg0BvrWorkarea;
import com.teamcenter.soa.exceptions.NotLoadedException;

public class BOMExpand {
	
	Connection connection;
	BasicSoaUtil basicSoaUtil;
	MppUtil mppUtil;
	LogFileUtility logFileUtility; 
	Vector<String> expandExcludeItemTypesV;
	int limitCount = 100;
	int currentBOMLineCount = 0;
	MPPTopLines mppTopLines;

	public BOMExpand(Connection connection){
		this.connection = connection;
		this.basicSoaUtil = new BasicSoaUtil(this.connection);
		this.mppUtil = new MppUtil(connection);
		logFileUtility = new LogFileUtility("BOMExpandTest4[BodyX100].txt");
		logFileUtility.setOutUseSystemOut(true);
		
		expandExcludeItemTypesV = new Vector<String>();
//		expandExcludeItemTypesV.add("M7_BOPShopRevision");
//		expandExcludeItemTypesV.add("M7_BOPLineRevision");
//		expandExcludeItemTypesV.add("M7_BOPStationRevision");
//		expandExcludeItemTypesV.add("M7_BOPBodyOpRevision");
		//expandExcludeItemTypesV.add("M7_BOPWeldOPRevision");
		//expandExcludeItemTypesV.add("M7_GunRevision");
		//expandExcludeItemTypesV.add("M7_JigFixtureRevision");
		//expandExcludeItemTypesV.add("M7_MfgProductRevision");		// Accumulated Parts
		//expandExcludeItemTypesV.add("M7_PlantOPAreaRevision");
		//expandExcludeItemTypesV.add("M7_RobotRevision");
		//expandExcludeItemTypesV.add("M7_SubsidiaryRevision");
		//expandExcludeItemTypesV.add("S7_FunctionRevision");				// Accumulated Part 2Level
		//expandExcludeItemTypesV.add("S7_FunctionMastRevision");		// Accumulated Part 3Level
//		expandExcludeItemTypesV.add("S7_StdpartRevision");
//		expandExcludeItemTypesV.add("S7_VehpartRevision");
		//expandExcludeItemTypesV.add("WeldPointRevision");
	}
	
	public void test(){
	
		logFileUtility.setTimmerStarat();
		logFileUtility.writeReport("Start");
		
		// CC를 찾는다
		String ccName = "";
		ccName = "X100 Body CC";
		//ccName = "X100 Assembly CC";
		
		MECollaborationContext aMECollaborationContext = mppUtil.findMECollaborationContext(ccName);
		if(aMECollaborationContext==null){
			return;
		}
		String[] propertyNames = new String[]{"object_name","structure_contexts"};

		try {
			aMECollaborationContext = (MECollaborationContext)basicSoaUtil.readProperties(aMECollaborationContext, propertyNames);
			System.out.println("aMECollaborationContext.get_object_name() = "+aMECollaborationContext.get_object_name());
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
    	// Product, Process, Plant의 Top BOMLine을 가져온다.
    	try {
			mppTopLines = mppUtil.openCollaborationContext(aMECollaborationContext);
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
    	if(mppTopLines!=null){
    		printMPPTopLines(mppTopLines);
    	}
    	
    	
    	bomLineFindTest();

    	// BOM을 전개 한다.
    	//expandProcessLines();
    	
    	// Window를 닫는다.
    	try {
			mppUtil.closeCollaborationContext(mppTopLines);
		} catch (Exception e) {
			e.printStackTrace();
		}
        
    	logFileUtility.writeReport("End ["+logFileUtility.getElapsedTime()+"]");
    	
	}
	
	private void bomLineFindTest(){
		
		if(mppTopLines==null){
			return;
		}
		
    	// BOM 검색 Test
    	String itemId = "35-B1-131N-00";
    	BOMWindow targetWindow = null;
    	try {
			mppTopLines.productLine = (BOMLine)basicSoaUtil.readProperties(mppTopLines.processLine, new String[]{"bl_window"});
			targetWindow = (BOMWindow) mppTopLines.processLine.get_bl_window();
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
    	BOMLine[] finded = mppUtil.findBOMLineUseStructureSearch(itemId, mppTopLines.processLine, targetWindow);
    	for (int i = 0;finded!=null && i < finded.length; i++) {
    		if(finded[i] != null){
    			finded[i] = initItemRevisionBasicInformation(finded[i], "Searched BOMLine :");
    		}
		}
    	
	}
	
	private void expandProcessLines(){
		
		Mfg0BvrProcess processTopLine = mppTopLines.processLine;
		expand((BOMLine)processTopLine);
	}
	
	private void expand(BOMLine processLine){
		
		if(currentBOMLineCount>=this.limitCount){
			return;
		}
		
		processLine = initItemRevisionBasicInformation(processLine, "Process BOMLine :");
		
		currentBOMLineCount++;
		
		String occType = null;
		try {
			occType = processLine.get_bl_occ_type();
		} catch (NotLoadedException e2) {
			e2.printStackTrace();
		}
		
		// MEWorkpiece, MEResource, MEWeldPoint, MEWorkArea, MEConsumed
		if(occType!=null && occType.trim().equalsIgnoreCase("MEConsumed")==true){
			
			BOMLine[] assignedProductBOMLines = findAssignedProductBOMLines(processLine);
			for (int i = 0; i < assignedProductBOMLines.length; i++) {
				assignedProductBOMLines[i] = initItemRevisionBasicInformation(assignedProductBOMLines[i], "@@ Assigned Product BOMLine :");
			}
			
			return;
		}
		
		String itemRevisionType;
		try {
			itemRevisionType = processLine.get_bl_rev_object_type();
			if(expandExcludeItemTypesV.contains(itemRevisionType)==true){
				return;
			}
		} catch (NotLoadedException e1) {
			e1.printStackTrace();
		}
		
		String[] targetPropertyNames = new String[]{"bl_child_lines"};
		ModelObject[] childLineModels = null;
		try {
			processLine = (BOMLine)basicSoaUtil.readProperties(processLine, targetPropertyNames);
			childLineModels = processLine.get_bl_child_lines();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		for (int i = 0;childLineModels!=null && i < childLineModels.length; i++) {
			BOMLine tempBOMLine = (BOMLine)childLineModels[i];
			if(tempBOMLine!=null){
				expand(tempBOMLine);
			}
		}
		
	}
	
	private BOMLine[] findAssignedProductBOMLines(BOMLine findTargetBOMLine){
		
		try {
			String occType = findTargetBOMLine.get_bl_occ_type();
		} catch (NotLoadedException e) {
			e.printStackTrace();
		}

		BOMLine[] foundBOMLines = null;
		// 할당된 원본을 Product에서 찾는다.
		ArrayList<BOMLine> foundBOMLineList = mppUtil.findBOMLineFromTopNode(this.mppTopLines.productLine, findTargetBOMLine);
		if(foundBOMLineList!=null && foundBOMLineList.size()>0){
			foundBOMLines = new BOMLine[foundBOMLineList.size()];
		}
		for (int i = 0;foundBOMLineList!=null && i < foundBOMLineList.size(); i++) {
			BOMLine tempBOMLine = (BOMLine)foundBOMLineList.get(i);
			foundBOMLines[i] = tempBOMLine;
		}
		
		return foundBOMLines;
		
	}
	
	private BOMLine initItemRevisionBasicInformation(BOMLine bomLine, String messageStr){
		
		BOMLine newBOMLine = bomLine;
		
		String[] targetPropertyNames = new String[]{"bl_item_item_id", "bl_rev_item_revision_id",
					"bl_rev_object_name", "bl_rev_object_type", "bl_rev_object_desc",
					"bl_occ_occurrence_type", "bl_occ_occurrence_name",
					"bl_occ_type", "bl_abs_occ_id", "bl_item_object_type"
				};

		String itemId = null;
		String itemRevId = null;
		String itemRevisionName = null;
		String itemRevisionDesc = null;
		String itemRevisionType = null;
		String itemObjectType = null;
		String occurrenceType = null;
		String occurrenceName = null;
		String occType = null;
		String absOccId = null;
		
		try {
			newBOMLine = (BOMLine) basicSoaUtil.readProperties(bomLine, targetPropertyNames);
			
			itemId = newBOMLine.get_bl_item_item_id();
			itemRevId = newBOMLine.get_bl_rev_item_revision_id();
			itemRevisionName = newBOMLine.get_bl_rev_object_name();
			itemRevisionDesc = newBOMLine.get_bl_rev_object_desc();
			itemRevisionType = newBOMLine.get_bl_rev_object_type();
			occurrenceType = newBOMLine.get_bl_occ_occurrence_type();
			occurrenceName = newBOMLine.get_bl_occ_occurrence_name();
			occType = newBOMLine.get_bl_occ_type();
			absOccId = newBOMLine.get_bl_abs_occ_id();
			itemObjectType = newBOMLine.get_bl_item_object_type();
			
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		if(messageStr==null || (messageStr!=null && messageStr.trim().length()<1)){
			messageStr = "";
		}
		
		logFileUtility.writeReport(messageStr+" "+itemId+"/"+itemRevId+" "+itemRevisionName+" ["+itemRevisionType+", "+itemObjectType+"] "+occurrenceName+", "+absOccId+" ["+occurrenceType+", "+occType+"]");
		
		return newBOMLine;
	}
	
	private void printMPPTopLines(MPPTopLines mppTopLines){
		
		String[] properteis = new String[]{"bl_item_item_id", "bl_item_item_revision", "bl_item_object_type", "bl_item_object_name", "bl_window"};
		BOMLine tempBOMLine  = mppTopLines.productLine;
		try {
			tempBOMLine = (BOMLine)basicSoaUtil.readProperties(tempBOMLine, properteis);
			
			String itemId = tempBOMLine.get_bl_item_item_id();
			String itemRevId = tempBOMLine.get_bl_item_item_revision();
			String itemType = tempBOMLine.get_bl_item_object_type();
			String itemName = tempBOMLine.get_bl_item_object_name();
			System.out.println("BOMLine = "+itemId+"/"+itemRevId+" "+itemName+" ["+itemType+"]");
			
			//ModelObject windowModel = tempOBMLine.get_bl_window();
			//System.out.println("windowModel.getClass().getName() = "+windowModel.getClass().getName());

		} catch (Exception e3) {
			e3.printStackTrace();
		}

		Mfg0BvrProcess tempProcessLine  = mppTopLines.processLine;

		try {
			tempProcessLine = (Mfg0BvrProcess) basicSoaUtil.readProperties(tempProcessLine, properteis);
			String itemId = tempProcessLine.get_bl_item_item_id();
			String itemRevId = tempProcessLine.get_bl_item_item_revision();
			String itemType = tempProcessLine.get_bl_item_object_type();
			String itemName = tempProcessLine.get_bl_item_object_name();
			System.out.println("ProcessLine = "+itemId+"/"+itemRevId+" "+itemName+" ["+itemType+"]");
			
			//ModelObject windowModel = tempProcessLine.get_bl_window();
			//System.out.println("windowModel.getClass().getName() = "+windowModel.getClass().getName());

		} catch (Exception e2) {
			e2.printStackTrace();
		}
		
		Mfg0BvrWorkarea tempPlantLine  = mppTopLines.plantLine;
		try {
			tempPlantLine = (Mfg0BvrWorkarea) basicSoaUtil.readProperties(tempPlantLine, properteis);
			
			String itemId = tempPlantLine.get_bl_item_item_id();
			String itemRevId = tempPlantLine.get_bl_item_item_revision();
			String itemType = tempPlantLine.get_bl_item_object_type();
			String itemName = tempPlantLine.get_bl_item_object_name();
			tempPlantLine.get_bl_occ_type();
			System.out.println("PlantLine = "+itemId+"/"+itemRevId+" "+itemName+" ["+itemType+"]");
			
			//ModelObject windowModel = tempPlantLine.get_bl_window();
			//System.out.println("windowModel.getClass().getName() = "+windowModel.getClass().getName());

		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
	}
	
	
	
}
