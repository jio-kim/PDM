package com.kgm.soa.bop.intest;

import com.kgm.soa.bop.util.BasicSoaUtil;
import com.kgm.soa.bop.util.LogFileUtility;
import com.kgm.soa.bop.util.MPPTopLines;
import com.kgm.soa.bop.util.MppUtil;
import com.teamcenter.soa.client.Connection;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.strong.AppearanceGroup;
import com.teamcenter.soa.client.model.strong.BOMLine;
import com.teamcenter.soa.client.model.strong.BOMWindow;
import com.teamcenter.soa.client.model.strong.MECollaborationContext;
import com.teamcenter.soa.client.model.strong.Mfg0BvrProcess;
import com.teamcenter.soa.client.model.strong.Mfg0BvrWorkarea;

public class BOMFinder {
	
	Connection connection;
	BasicSoaUtil basicSoaUtil;
	MppUtil mppUtil;
	LogFileUtility logFileUtility; 
	//Vector<String> expandExcludeItemTypesV;
	int limitCount = 100;
	int currentBOMLineCount = 0;
	MPPTopLines mppTopLines;
	String targetCCName;

	public BOMFinder(Connection connection, LogFileUtility logFileUtility, String ccName){
		this.connection = connection;
		this.basicSoaUtil = new BasicSoaUtil(this.connection);
		this.mppUtil = new MppUtil(connection);
		this.logFileUtility = logFileUtility;
		this.logFileUtility.setOutUseSystemOut(true);
		this.targetCCName = ccName;

	}
	
	public void doTestFunction(){
	
		logFileUtility.setOutUseSystemOut(true);
		logFileUtility.setTimmerStarat();
		logFileUtility.writeReport("CC Find...");
		
		// CC를 찾는다
		MECollaborationContext aMECollaborationContext = mppUtil.findMECollaborationContext(targetCCName);
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
		
		
		logFileUtility.writeReport("Open BOP Window...");
    	// Product, Process, Plant의 Top BOMLine을 가져온다.
    	try {
			mppTopLines = mppUtil.openCollaborationContext(aMECollaborationContext);
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
    	if(mppTopLines!=null){
    		printMPPTopLines(mppTopLines);
    	}
    	
    	
//    	logFileUtility.writeReport("Find BOP Line...");
//    	bopLineFindTest();
    	
    	try {
			basicSoaUtil.readProperties(mppTopLines.processLine, new String[]{"bl_all_child_lines"});
			ModelObject[] chilBOMLineObjects = mppTopLines.processLine.get_bl_all_child_lines();
			
			for (int i = 0; chilBOMLineObjects!=null && i < chilBOMLineObjects.length; i++) {
				if( chilBOMLineObjects[i]!=null && chilBOMLineObjects[i] instanceof BOMLine){
					expandAllChildLine((BOMLine)chilBOMLineObjects[i]);
				}
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
    	
    	// Window를 닫는다.
    	try {
			mppUtil.closeCollaborationContext(mppTopLines);
		} catch (Exception e) {
			e.printStackTrace();
		}
        
    	logFileUtility.writeReport("End ["+logFileUtility.getElapsedTime()+"]");
    	
	}
	
	/**
	 * Child Node를 전개하는 Function
	 * @param bomLine
	 */
	private void expandAllChildLine(BOMLine bomLine){
		
		String[] properteis = new String[]{"bl_item_item_id", "bl_item_item_revision", "bl_item_object_type", "bl_item_object_name", "bl_window", "bl_occ_type", "bl_indented_title", "bl_line_object"};

		
		String itemId = null;
		String itemRevId = null;
		String itemType = null;
		String itemName = null;
		String occType = null;
		String indentedTitle = null;
		ModelObject  lineObject = null;
		try {
			bomLine = (BOMLine)basicSoaUtil.readProperties(bomLine, properteis);
			itemId = bomLine.get_bl_item_item_id();
			itemRevId = bomLine.get_bl_item_item_revision();
			itemType = bomLine.get_bl_item_object_type();
			itemName = bomLine.get_bl_item_object_name();
			occType = bomLine.get_bl_occ_type();
			indentedTitle = bomLine.get_bl_indented_title();
			lineObject = bomLine.get_bl_line_object();
			
			System.out.println("BOMLine = "+itemId+"/"+itemRevId+" "+itemName+" ["+itemType+"/"+occType+"/"+indentedTitle+"]");
			
		} catch (Exception e3) {
			e3.printStackTrace();
		}

		
		if(itemType!=null && itemType.trim().equalsIgnoreCase("M7_BOPWeldOP")){
			return;
		}
		
		if(lineObject != null && lineObject instanceof AppearanceGroup){
			//itemType="M7_MfgProduct"
			return;
		}else{
			// MEProcessRevision, MEOPRevision, ItemRevision, Mfg0MEResourceRevision, MEWorkareaRevision, Mfg0MEDiscreteOPRevision
		}
		
    	try {
			basicSoaUtil.readProperties(bomLine, new String[]{"bl_all_child_lines"});
			ModelObject[] chilBOMLineObjects = bomLine.get_bl_all_child_lines();
			
			for (int i = 0; chilBOMLineObjects!=null && i < chilBOMLineObjects.length; i++) {
				// Child Node를 전개하는 Function을 재귀호출 한다.
				if( chilBOMLineObjects[i]!=null && chilBOMLineObjects[i] instanceof BOMLine){
					expandAllChildLine((BOMLine)chilBOMLineObjects[i]);
				}
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
    	
	}
	
	private void bopLineFindTest(){
		
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
