package com.kgm.soa.bop.reports;

import java.io.File;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

import com.kgm.CommonConstants;
import com.kgm.common.remote.SendMailEAIUtil;
import com.kgm.dto.EndItemData;
import com.kgm.soa.bop.util.BasicSoaUtil;
import com.kgm.soa.bop.util.LogFileUtility;
import com.kgm.soa.bop.util.MPPTopLines;
import com.kgm.soa.bop.util.MppUtil;
import com.teamcenter.services.strong.manufacturing.CoreService;
import com.teamcenter.services.strong.manufacturing._2010_09.Core;
import com.teamcenter.services.strong.manufacturing._2010_09.Core.FindNodeInContextResponse;
import com.teamcenter.services.strong.manufacturing._2010_09.Core.NodeInfo;
import com.teamcenter.services.strong.manufacturing._2013_05.Core.FindNodeInContextInputInfo;
import com.teamcenter.soa.client.Connection;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.Property;
import com.teamcenter.soa.client.model.strong.AppearanceGroup;
import com.teamcenter.soa.client.model.strong.BOMLine;
import com.teamcenter.soa.client.model.strong.BOMWindow;
import com.teamcenter.soa.client.model.strong.ImanItemBOPLine;
import com.teamcenter.soa.client.model.strong.Item;
import com.teamcenter.soa.client.model.strong.ItemRevision;
import com.teamcenter.soa.client.model.strong.MECollaborationContext;
import com.teamcenter.soa.client.model.strong.Mfg0BvrProcess;
import com.teamcenter.soa.client.model.strong.Mfg0BvrWorkarea;
import com.teamcenter.soa.client.model.strong.ReleaseStatus;
import com.teamcenter.soa.client.model.strong.RevisionRule;
import com.teamcenter.soa.client.model.strong.User;
import com.teamcenter.soa.exceptions.NotLoadedException;

/**
 * Discription 추가
 * @author tj
 *
 */
public class EndItemMasterListReport {
	
	Connection connection;
	String targetCCName;
	MECollaborationContext targetMECollaborationContext;

	BasicSoaUtil basicSoaUtil;
	MppUtil mppUtil;
	LogFileUtility logFileUtility; 
	MPPTopLines mppTopLines;

	DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	Vector<String> reportTargeItemType  = null;
	BOMLine productTopBOMLine = null;
	
	ArrayList<String> replacedEndItemOccPuidList;
	
	String plantCode = null;
	String korCarName = null;
	String engCarName = null;
	String vechileCode = null;
	String productCode = null;
	String mProductItemId = null;
	String mProductItemPUID = null;
	
	String processTopItemId = null;
	String processTopItemPuid = null;
	ImanItemBOPLine processTopBOMLine = null;
	String processType = null;
	
	

	//-----------------------------------
	
	Vector<String> occTypeV  = null;
	
	ImanItemBOPLine currentLineBOMLine = null;
	String currentLineItemId = null;
	String currentLineCode = null;
	String currentLineItemRevId = null;
	
	ImanItemBOPLine currentStationBOMLine = null;
	String currentStationItemId = null;
	String currentStationCode = null;
	String currentStationItemRevId = null;
	
	int indexNo = 0;
	int limitIndexCount = 0;
	
	String reportDestinationFolderPath = CommonConstants.REPORT_FILE_PATH;
	File reportTemplateFile = null;
	String reportFilePath = null;
	
	ExcelWorkBookWriter excelWorkBookWriter;
	
	boolean isSuccess = false;
	
	public EndItemMasterListReport(Connection connection, String ccName){
		
		this.connection = connection;
		this.targetCCName = ccName;
		
		this.basicSoaUtil = new BasicSoaUtil(this.connection);
		String userId = this.basicSoaUtil.getLoginUserId();
		DateFormat fileNameDf = new SimpleDateFormat("yyyyMMdd_HHmmss");
		String timeStr = fileNameDf.format(new Date());
		this.logFileUtility = new LogFileUtility("EIMLReport_"+ccName+"["+timeStr+"].txt");
		this.logFileUtility.setOutUseSystemOut(true);
		
		this.mppUtil = new MppUtil(connection);
		
		this.reportTargeItemType  = new Vector<String>();
		this.reportTargeItemType.add("M7_BOPBodyOp");
		this.reportTargeItemType.add("M7_BOPPaintOp");
		this.reportTargeItemType.add("M7_BOPAssyOp");
		
		this.occTypeV  = new Vector<String>();
		
	}
	
	public boolean makeReport(){
		
		this.logFileUtility.writeReport("Make Report");
	
		logFileUtility.setTimmerStarat();
		logFileUtility.writeReport("Find CC...");
		
		// CC를 찾는다
		targetMECollaborationContext = mppUtil.findMECollaborationContext(targetCCName);
		if(targetMECollaborationContext==null){
			logFileUtility.writeReport("Return ["+logFileUtility.getElapsedTime()+"] : CC is null!!");
			return isSuccess;
		}
		
		// MECollaborationContext의 Structure Context Object를 찾아 온다.
		try {
			String[] propertyNames = new String[]{"object_name","structure_contexts"};
			targetMECollaborationContext = (MECollaborationContext)basicSoaUtil.readProperties(targetMECollaborationContext, propertyNames);
			this.logFileUtility.writeReport("aMECollaborationContext.get_object_name() = "+targetMECollaborationContext.get_object_name());
		} catch (Exception e) {
			this.logFileUtility.writeExceptionTrace(e);
		}
		
		logFileUtility.writeReport("Open BOP Window...");
		
    	// Product, Process, Plant의 Top BOMLine을 가져온다.
		// 아래의 Function 실행을 하지 않으면 mppTopLines의 값들이 초기화 되지 않은
		// 상태로 남아 있음.
    	try {
			mppTopLines = mppUtil.openCollaborationContext(targetMECollaborationContext);
		} catch (Exception e) {
			this.logFileUtility.writeExceptionTrace(e);
		}
    	
    	if(mppTopLines==null){
    		logFileUtility.writeReport("Return ["+logFileUtility.getElapsedTime()+"] : mppTopLines is null!!");
    		return isSuccess;
    	}
    	
//    	processStructureExpandTest(mppTopLines);
//    	
//    	if(true){
//    		return false;
//    	}

    	
    	unpack();
    	initProductBasicInformation();
    	
    	String structureDataInitTime = logFileUtility.getElapsedTime();

		boolean isReadyOk = false;
		
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_ss");
		String newFileName = "EIMLReport_"+this.productCode+"_"+this.plantCode+"["+simpleDateFormat.format(new Date())+"]";
		reportFilePath = reportDestinationFolderPath + File.separator + newFileName+".xlsx";

		this.reportTemplateFile = SDVBOPUtilities.getReportExcelTemplateFile(connection, reportDestinationFolderPath, "ME_DOCTEMP_01", reportFilePath);
		
		if(this.reportTemplateFile!=null && this.reportTemplateFile.exists()==true){
			
			//this.reportTemplateFile.renameTo(new File(reportFilePath));
			
			excelWorkBookWriter = new ExcelWorkBookWriter(reportFilePath);

			int templateDataStartRowIndex = 4;
			int templateDataStartColumnIndex = 0;
			int templateReadyLastRowIndex = templateDataStartRowIndex+3;
			
			try {
				excelWorkBookWriter.readyFile(templateDataStartRowIndex, templateDataStartColumnIndex, templateReadyLastRowIndex);
				isReadyOk = true;
			} catch (Exception e) {
				this.logFileUtility.writeExceptionTrace(e);
			}
		}
		
		if(isReadyOk==false){
			this.logFileUtility.writeReport("It failed ready to output to a file.");
			
	    	// Window를 닫는다.
	    	try {
				mppUtil.closeCollaborationContext(mppTopLines);
			} catch (Exception e) {
				this.logFileUtility.writeExceptionTrace(e);
			}
	    	
			return isSuccess;
		}
		
		// 자동변경된 Item List를 전역변수에 저장한다.
		initReplacedEndItem();
		printTitleInformation();
		
		String replacedPartInitTime = logFileUtility.getElapsedTime();
    	
    	// BOP Process Structure를 전개해서 Report 생성을 시작 한다.
    	try {
			basicSoaUtil.readProperties(mppTopLines.processLine, new String[]{"bl_all_child_lines"});
			ModelObject[] chilBOMLineObjects = mppTopLines.processLine.get_bl_all_child_lines();
			
			for (int i = 0; chilBOMLineObjects!=null && i < chilBOMLineObjects.length; i++) {
				
				if(limitIndexCount!=0 && this.indexNo>limitIndexCount){
					break;
				}
				
				if( chilBOMLineObjects[i]!=null && chilBOMLineObjects[i] instanceof BOMLine){
					expandAllChildLine((ImanItemBOPLine)chilBOMLineObjects[i]);
				}
			}
		} catch (Exception e) {
			this.logFileUtility.writeExceptionTrace(e);
		}
    	
    	// Window를 닫는다.
    	try {
    		
			mppUtil.closeCollaborationContext(mppTopLines);
			excelWorkBookWriter.closeWorkBook();
		} catch (Exception e) {
			this.logFileUtility.writeExceptionTrace(e);
		}
    	
    	File tempFile = new File(this.reportFilePath);
    	if(this.reportFilePath!=null && tempFile.exists()==true && indexNo>0){
    		isSuccess = true;
    	}
    	isSuccess = true;
        
    	this.logFileUtility.writeReport("\n\n----------------------------------");
		this.logFileUtility.writeReport("korCarName = "+korCarName);
		this.logFileUtility.writeReport("engCarName = "+engCarName);
		this.logFileUtility.writeReport("vechileCode = "+vechileCode);
		this.logFileUtility.writeReport("productCode = "+productCode);
		this.logFileUtility.writeReport("mProductItemId = "+mProductItemId);
    	
		this.logFileUtility.writeReport("\n\n----------------------------------");
		
    	for (int i = 0; this.occTypeV!=null && i < this.occTypeV.size(); i++) {
    		String occType = this.occTypeV.get(i);
    		this.logFileUtility.writeReport("occType["+i+"] = "+occType);
		}
    	
    	this.logFileUtility.writeReport("\n\n----------------------------------");
    	
    	logFileUtility.writeReport("Structure Init Time :  ["+structureDataInitTime+"]");
    	logFileUtility.writeReport("Replaced Part Init Time :  ["+replacedPartInitTime+"]");
    	logFileUtility.writeReport("End ["+logFileUtility.getElapsedTime()+"]");
    	
    	return isSuccess;
	}
	
	private void printTitleInformation(){
		
		String productInfoString = this.productCode + "_" + this.processTopItemId;
		
		try {
			processTopBOMLine = (ImanItemBOPLine) basicSoaUtil.readProperties(processTopBOMLine, new String[]{"bl_window"});
			
			BOMWindow window = (BOMWindow)processTopBOMLine.get_bl_window();
			
			window = (BOMWindow) basicSoaUtil.readProperties(window, new String[]{"revision_rule"});
			RevisionRule revisionRule = (RevisionRule)window.get_revision_rule();
			revisionRule = (RevisionRule) basicSoaUtil.readProperties(revisionRule, new String[]{"object_name", "rule_date"});
			String revisionRuleName = revisionRule.get_object_name();
			// Revision Rule 기준일
			String revRuleStandardDate = null;
			Calendar ruleDateCalendar  = revisionRule.get_rule_date();
			Date rule_date = null;
			if(ruleDateCalendar!=null){
				rule_date = ruleDateCalendar.getTime();
			}
			if (rule_date != null) {
				revRuleStandardDate = new SimpleDateFormat("yyyy-MM-dd").format(rule_date);
			} else {
				revRuleStandardDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
			}
			String revisionRuleInfo = revisionRuleName+"("+revRuleStandardDate+")";
			String reportDate = "출력 일시 : "+new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
			
			this.logFileUtility.writeReport("productInfoString = "+productInfoString);
			this.logFileUtility.writeReport("revisionRuleInfo = "+revisionRuleInfo);
			this.logFileUtility.writeReport("reportDate = "+reportDate);
			
			excelWorkBookWriter.writeRow("Sheet1", 1, 0, productInfoString);
			excelWorkBookWriter.writeRow("Sheet1", 2, 0, revisionRuleInfo);
			//excelWorkBookWriter.writeRow("Sheet1", 2, 3, variantRule);
			/*
			 * 수정점 : 20200303 
			 * 수정 내용 : 컬럼 추가 (공법명 Cols )으로 인해 헤더의 길이가 길어져
			 * 			   출력 시간을 찍는곳을 그 다음 칸으로 수정
			 */
			excelWorkBookWriter.writeRow("Sheet1", 2, 44, reportDate);
		} catch (Exception e) {
			this.logFileUtility.writeExceptionTrace(e);
		}

	}
	
	public void sendResultMail(String typeOfReport, String userId){
		
		SendMailEAIUtil asendfile = new SendMailEAIUtil();
    	asendfile.sendMailEAI(typeOfReport, processTopItemId+" ["+korCarName+"/"+engCarName+"]", logFileUtility, reportFilePath, isSuccess, userId);
	}
	
	private void initProductBasicInformation(){
		
    	// productTopBOMLine 을 초기화 한다.
    	productTopBOMLine = mppTopLines.productLine;
		
		try {
			processTopBOMLine = (ImanItemBOPLine)this.mppTopLines.processLine;
			processTopBOMLine = (ImanItemBOPLine)basicSoaUtil.readProperties(processTopBOMLine, new String[]{"bl_item", "bl_revision"});
			
			Item processItem = (Item) processTopBOMLine.get_bl_item();
			if(processItem!=null){
				processItem = (Item) basicSoaUtil.readProperties(processItem, new String[]{"m7_VEHICLE_KOR_NAME", "m7_VEHICLE_ENG_NAME", "item_id"});
				korCarName = processItem.getPropertyDisplayableValue("m7_VEHICLE_KOR_NAME");
				engCarName = processItem.getPropertyDisplayableValue("m7_VEHICLE_ENG_NAME");
				
				engCarName = engCarName.replaceAll("/", "_");
				engCarName = engCarName.replaceAll("\\\\", "_");
				
				this.logFileUtility.writeReport("korCarName = "+korCarName);
				this.logFileUtility.writeReport("engCarName = "+engCarName);
				
				this.processTopItemId = processItem.get_item_id();
				this.processTopItemPuid = processItem.getUid();
				
			}

			ItemRevision processItemRevision = (ItemRevision) processTopBOMLine.get_bl_revision();
			if(processItemRevision!=null){
				
				this.logFileUtility.writeReport("Process Item Revision is not null");
				
				processItemRevision = (ItemRevision) basicSoaUtil.readProperties(processItemRevision, new String[]{"m7_VEHICLE_CODE", "m7_PRODUCT_CODE", "m7_SHOP", "m7_PROCESS_TYPE"});
				vechileCode = processItemRevision.getPropertyDisplayableValue("m7_VEHICLE_CODE");
				productCode = processItemRevision.getPropertyDisplayableValue("m7_PRODUCT_CODE");
				plantCode = processItemRevision.getPropertyDisplayableValue("m7_SHOP");
				// process type
				processType = processItemRevision.getPropertyDisplayableValue("m7_PROCESS_TYPE");
				// operation type(차체(B), 도장(P), 조립(A))
				if (processType!=null && processType.startsWith("B")) {
				} else if (processType!=null && processType.startsWith("P")) {
				} else if (processType!=null && processType.startsWith("A")) {
				}

				this.logFileUtility.writeReport("vechileCode = "+vechileCode);
				this.logFileUtility.writeReport("productCode = "+productCode);
				this.logFileUtility.writeReport("plantCode = "+plantCode);
				
			}else{
				this.logFileUtility.writeReport("Process Item Revision is null");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			productTopBOMLine = (BOMLine) basicSoaUtil.readProperties(productTopBOMLine, new String[]{"bl_item_item_id", "bl_item"});
			mProductItemId = productTopBOMLine.get_bl_item_item_id();
			mProductItemPUID = productTopBOMLine.get_bl_item().getUid();
			this.logFileUtility.writeReport("mProductItemId = "+mProductItemId);
		} catch (Exception e) {
			this.logFileUtility.writeExceptionTrace(e);
		}

	}
	
	private void unpack(){
		
		int flag = 3;    // 0:pack the lines 
		// 1:unpack the lines 
		// 2:pack all lines 
		// 3:unpack all lines

	BOMLine[] srcBOMLines = new BOMLine[]{this.mppTopLines.processLine};
	com.teamcenter.soa.client.model.ServiceData serviceData = 
	com.teamcenter.services.strong.structuremanagement.StructureService.getService(connection).packOrUnpack(srcBOMLines, flag);
	
	this.logFileUtility.writeReport("++++++++++++");
	this.logFileUtility.writeReport("Packed");
	this.logFileUtility.writeReport("++++++++++++");
	int plainObjectsize = serviceData.sizeOfPlainObjects();
	int createObjectsize = serviceData.sizeOfCreatedObjects();
	int updateObjectsize = serviceData.sizeOfUpdatedObjects();

	this.logFileUtility.writeReport("plainObjectsize = "+plainObjectsize);
	this.logFileUtility.writeReport("createObjectsize = "+createObjectsize);
	this.logFileUtility.writeReport("updateObjectsize = "+updateObjectsize);
		
	}

	/**
	 * Child Node를 전개하는 Function
	 * @param bopLine
	 */
	private void expandAllChildLine(ImanItemBOPLine bopLine){
		
		if(limitIndexCount!=0 && this.indexNo>limitIndexCount){
			return;
		}
		
		String[] properteis = new String[]{"bl_item_item_id", "bl_item_item_revision", "bl_rev_item_revision_id", "bl_item_object_type", "bl_item_object_name", "bl_window", "bl_occ_type", "bl_indented_title", "bl_line_object", "bl_revision"};
		
		String itemId = null;
		String itemRevId = null;
		String itemType = null;
		String itemName = null;
		String occType = null;
		String indentedTitle = null;
		ModelObject  lineObject = null;
		ItemRevision itemRevision = null;
		try {
			bopLine = (ImanItemBOPLine)basicSoaUtil.readProperties(bopLine, properteis);
			itemId = bopLine.get_bl_item_item_id();
			itemRevId = bopLine.get_bl_rev_item_revision_id();
			itemType = bopLine.get_bl_item_object_type();
			itemName = bopLine.get_bl_item_object_name();
			occType = bopLine.get_bl_occ_type();
			indentedTitle = bopLine.get_bl_indented_title();
			lineObject = bopLine.get_bl_line_object();
			itemRevision = (ItemRevision)bopLine.get_bl_revision();
			
		} catch (Exception e) {
			this.logFileUtility.writeExceptionTrace(e);
		}

		// ------------------------------------------------
		// 전개 제외 조건 (Start)
		// ------------------------------------------------
		if(lineObject != null && lineObject instanceof AppearanceGroup){
			//itemType="M7_MfgProduct"
			return;
		}
		
		if(itemType!=null && itemType.trim().equalsIgnoreCase("M7_BOPLine")){
			if(this.currentLineBOMLine==null || (this.currentLineBOMLine!=null && this.currentLineBOMLine.equals(bopLine)==false)){
				this.currentLineBOMLine = bopLine;
				this.currentLineItemId = itemId;
				this.currentLineItemRevId = itemRevId;
				
				try {
					itemRevision = (ItemRevision)basicSoaUtil.readProperties(itemRevision, new String[]{"m7_LINE"});
					this.currentLineCode = itemRevision.getPropertyDisplayableValue("m7_LINE");
				} catch (Exception e) {
					this.logFileUtility.writeExceptionTrace(e);
				}
				
				// 조립의 경우 미할당 Line을 Report 출력에서 제외 한다.
				if(this.currentLineCode==null || (this.currentLineCode!=null && this.currentLineCode.trim().length()<1)){
					return;
				}
				
			}
		}

		if(itemType!=null && itemType.trim().equalsIgnoreCase("M7_BOPStation")){
			if(this.currentStationBOMLine==null || (this.currentStationBOMLine!=null && this.currentStationBOMLine.equals(bopLine)==false)){
				this.currentStationBOMLine = bopLine;
				this.currentStationItemId = itemId;
				
				try {
					itemRevision = (ItemRevision)basicSoaUtil.readProperties(itemRevision, new String[]{"m7_STATION_CODE"});
					this.currentStationCode = itemRevision.getPropertyDisplayableValue("m7_STATION_CODE");
				} catch (Exception e) {
					this.logFileUtility.writeExceptionTrace(e);
				}
				
				this.currentStationItemRevId = itemRevId;
			}
		}
		
		if(itemType!=null && itemType.trim().equalsIgnoreCase("M7_BOPWeldOP")){
			return;
		}
		
		// ------------------------------------------------
		// 전개 제외 조건 (End)
		// ------------------------------------------------
		
		// MEProcessRevision, MEOPRevision, ItemRevision, Mfg0MEResourceRevision, MEWorkareaRevision, Mfg0MEDiscreteOPRevision
		
		// ----------------------------------------------------------------------------------
		// Operation에 대한 End Item을 전개한 결과를 Report 해야 하므로
		// Operation의 경우 Report를 만들기 위한 Data를 생성 하도록 한다.
		// ----------------------------------------------------------------------------------
		if(itemType!=null && this.reportTargeItemType.contains(itemType)==true ){
			
			//this.logFileUtility.writeReport("BOMLine = "+itemId+"/"+itemRevId+" "+itemName+" ["+itemType+"/"+occType+"/"+indentedTitle+"]");
			
			makeOperatonReportData(bopLine);
			return;
		}
		
		// ----------------------------------------------------------------------------------
		// 제귀 호출을 이용해 Child Node를 계속 전개 한다.
		// ----------------------------------------------------------------------------------
    	try {
			basicSoaUtil.readProperties(bopLine, new String[]{"bl_all_child_lines"});
			ModelObject[] chilBOMLineObjects = bopLine.get_bl_all_child_lines();
			
			for (int i = 0; chilBOMLineObjects!=null && i < chilBOMLineObjects.length; i++) {
			/////////////////////////////////////////////////////////////////////
				/*
				 *    수정점 : 20200303
				 * 수정 내용 : 마스터 리스트 수정 후 테스트를 위해 테스트 항목을 줄이기 위한
				 * 			   수정
				 *    유의점 : 배포시 주석 처리 할것
				 */
//			for (int i = 0; chilBOMLineObjects!=null && i < 4; i++) {
			/////////////////////////////////////////////////////////////////////
				// Child Node를 전개하는 Function을 재귀호출 한다.
				if( chilBOMLineObjects[i]!=null && chilBOMLineObjects[i] instanceof BOMLine){
					expandAllChildLine((ImanItemBOPLine)chilBOMLineObjects[i]);
				}
			}
		} catch (Exception e) {
			this.logFileUtility.writeExceptionTrace(e);
		}
    	
	}
	
	/**
	 * Operation의 Child Node 정보를 Report에 List 할 형태로 만든다.
	 * @param operationBOPLine
	 */
	private void makeOperatonReportData(ImanItemBOPLine operationBOPLine){
		
		ModelObject[] chilBOMLineObjects = null;
		try {
			operationBOPLine = (ImanItemBOPLine)basicSoaUtil.readProperties(operationBOPLine, new String[] {"bl_all_child_lines"});
			chilBOMLineObjects = operationBOPLine.get_bl_all_child_lines();
		} catch (Exception e) {
			this.logFileUtility.writeExceptionTrace(e);
		}
		
		if( chilBOMLineObjects.length == 0 ) {
			return;
		}
		
		HashMap<String, Object> columnDataHash = new HashMap<String, Object>();
		
		columnDataHash.put("Col01", (Object) productCode);

		columnDataHash.put("Col31", (Object) this.currentLineCode);
		columnDataHash.put("Col32", (Object) currentLineItemRevId);
		
		columnDataHash.put("Col33", (Object) currentStationCode);
		columnDataHash.put("Col34", (Object) currentStationItemRevId);
		
		String operationItemId = null;
		String operationItemRevId = null;
		String operationItemType = null;
		
		/*
		 * 수정점 : 20200228
		 * 수정내용 : End Item List Report 에서 공법 명 컬럼 추가
		 */
		String operationItemName = null;
		
		Item operationItem = null;
		ItemRevision operationItemRevision  = null;
		
		String location = null;
 
		String operationVlCondition = null;
		String operationMVlCondition = null;
		
		// "bl_item_object_name", "bl_variant_condition",
		String[] operationBOMLineProperteis = new String[]{"bl_item_item_id", "bl_item_item_revision", "bl_rev_item_revision_id", "bl_item_object_type" 
				, "bl_item", "bl_revision",  "bl_occ_mvl_condition", "m7_WORK_UBODY",
				//20201109 seho EndItem List export 시 이름을 아이템 이름에서 리비전 이름으로 변경함.
				"bl_rev_object_name" /** "bl_item_object_name" */
			};
		
		try {
			operationBOPLine = (ImanItemBOPLine)basicSoaUtil.readProperties(operationBOPLine, operationBOMLineProperteis);
			
			operationItemId = operationBOPLine.get_bl_item_item_id();
			if(operationItemId.equals("35-1D-600-060R-00"))
			{
				System.out.println();
			}
			/*
			 * 수정점 : 20200228
			 * 수정내용 : End Item List Report 에서 공법 명 컬럼 추가
			 */
			//20201109 seho EndItem List export 시 이름을 아이템 이름에서 리비전 이름으로 변경함.
			operationItemName = operationBOPLine.get_bl_rev_object_name();
//			operationItemName = operationBOPLine.get_bl_item_object_name();
					
			operationItemRevId = operationBOPLine.get_bl_rev_item_revision_id();
			operationItemType = operationBOPLine.get_bl_item_object_type();
			//operationVlCondition = operationBOMLine.get_bl_variant_condition();
			operationMVlCondition = operationBOPLine.get_bl_occ_mvl_condition();
			
			operationItem = (Item)operationBOPLine.get_bl_item();
			operationItemRevision  = (ItemRevision)operationBOPLine.get_bl_revision();
			
		} catch (Exception e) {
			this.logFileUtility.writeExceptionTrace(e);
		}
		
		String operationOptionCode = null;
		String operationOptionDesc = null;
		
		if(operationMVlCondition!=null){
			// 옵션값을 설정한다.
			HashMap<String, Object> mapData = SDVBOPUtilities.getVariant(operationMVlCondition);
			operationOptionCode = (String)mapData.get("printValues");
			operationOptionDesc = (String)mapData.get("printDescriptions");
		}else{
			operationOptionCode = "";
			operationOptionDesc = "";
		}
		
		columnDataHash.put("Col35", (Object) operationItemId);
		/*
		 * 수정점 : 20200228
		 * 수정내용 : End Item List Report 에서 공법 명 컬럼 추가
		 * 			  기존의 36번 컬럼자리(공법리비전 번호)에 공법명을 추가 하여 나머지 컬럼의 번호가 하나씩 밀림
		 */
		columnDataHash.put("Col36", (Object) operationItemName);
		// 기존의 컬럼 36번에서 하나씩 밀림
		columnDataHash.put("Col37", (Object) operationItemRevId);
		columnDataHash.put("Col38", (Object) operationOptionCode);
		columnDataHash.put("Col39", (Object) operationOptionDesc);
		
		String[] operationItemRevProperties = new String[]{"m7_MECO_NO", "owning_user"};
		if (processType!=null && processType.startsWith("A")) {
			operationItemRevProperties = new String[]{"m7_MECO_NO", "m7_ASSY_SYSTEM", "owning_user"};
		}
		String assySystem = null;
		String operationMECONo = null;
		String operationOwnerName = null;
		String operationOwnerId = null;
		
		try {
			operationItemRevision = (ItemRevision)basicSoaUtil.readProperties(operationItemRevision, operationItemRevProperties);
		} catch (Exception e) {
			this.logFileUtility.writeExceptionTrace(e);
		}
		
		if (processType!=null && processType.startsWith("A")) {
			try {
				assySystem = operationItemRevision.getPropertyDisplayableValue("m7_ASSY_SYSTEM");
			} catch (NotLoadedException e) {
				e.printStackTrace();
			}
		}

		User owningUser = null;
		try {
			owningUser = (User)operationItemRevision.get_owning_user();
		} catch (Exception e) {
			this.logFileUtility.writeExceptionTrace(e);
		}

		try {
			operationMECONo = operationItemRevision.getPropertyDisplayableValue("m7_MECO_NO");
			
			Property  tempProperty  = (Property)operationItemRevision.getPropertyObject("m7_MECO_NO");
			if(tempProperty!=null){
				ItemRevision changeRevision = (ItemRevision) tempProperty.getModelObjectValue();
				if(changeRevision!=null){
					changeRevision = (ItemRevision)basicSoaUtil.readProperties(changeRevision, new String[]{"item_id"});
					operationMECONo = changeRevision.get_item_id();
				}
			}
			
		} catch (Exception e) {
			this.logFileUtility.writeExceptionTrace(e);
		}
			
		try {
			if(owningUser!=null){
				owningUser = (User)basicSoaUtil.readProperties(owningUser, new String[]{"user_name", "userid"});
				operationOwnerName = owningUser.get_user_name();
				operationOwnerId = owningUser.get_userid();
				
			}
		} catch (Exception e) {
			this.logFileUtility.writeExceptionTrace(e);
		}
		/*
		 * 수정점 : 20200228
		 * 수정내용 : End Item List Report 에서 공법 명 컬럼 추가
		 * 			  기존의 36번 컬럼자리(공법리비전 번호)에 공법명을 추가 하여 나머지 컬럼의 번호가 하나씩 밀림
		 */
		columnDataHash.put("Col42", (Object) operationMECONo);
		columnDataHash.put("Col43", (Object) "");		// E/Item 여부 -> 공란.
		columnDataHash.put("Col44", (Object) assySystem);
		columnDataHash.put("Col45", (Object) operationOwnerName);

		String locationLR = "";
		String locationUL = "";
		
		try {
			locationUL = operationBOPLine.getPropertyDisplayableValue("m7_WORK_UBODY");
		} catch (Exception e) {
			this.logFileUtility.writeExceptionTrace(e);
		}
		
		if(operationItemType!=null && operationItemType.trim().equalsIgnoreCase("M7_BOPAssyOp")==true){
			
			String stationNo = null;
			String staionRevId = null;
			
			String[] assyOpItemRevProperties = new String[]{"m7_STATION_NO"};
			try {
				operationItemRevision = (ItemRevision)basicSoaUtil.readProperties(operationItemRevision, assyOpItemRevProperties);
				stationNo = operationItemRevision.getPropertyDisplayableValue("m7_STATION_NO");
				staionRevId = "";
			} catch (Exception e) {
				this.logFileUtility.writeExceptionTrace(e);
			}
			// 조립의 경우 Station BOMLine이 없음.
			columnDataHash.put("Col33", (Object) stationNo);
			columnDataHash.put("Col34", (Object) staionRevId);
			
			String[] tempStrs = operationItemId.split("-");
			if(tempStrs!=null && tempStrs.length>4){
				String targetStr = tempStrs[3];
				if(targetStr!=null && targetStr.trim().endsWith("L")==true){
					locationLR = "L";
				}else if(targetStr!=null && targetStr.trim().endsWith("R")==true){
					locationLR = "R";
				}
			}
			
		}else if(operationItemType!=null && operationItemType.trim().equalsIgnoreCase("M7_BOPPaintOp")==true){
			
		}else if(operationItemType!=null && operationItemType.trim().equalsIgnoreCase("M7_BOPBodyOp")==true){
			
		}else{
			return;
		}
		
		/*
		 * 수정점 : 20200228
		 * 수정내용 : End Item List Report 에서 공법 명 컬럼 추가
		 * 			  기존의 36번 컬럼자리(공법리비전 번호)에 공법명을 추가 하여 나머지 컬럼의 번호가 하나씩 밀림
		 */
		columnDataHash.put("Col40", (Object) locationLR);
		columnDataHash.put("Col41", (Object) locationUL);
		
		String[] partBOMLineProperteis = new String[]{"bl_item_item_id", "bl_item_item_revision", "bl_rev_item_revision_id", "bl_item_object_type", "bl_rev_object_name"
				, "bl_occ_type", "bl_indented_title", "bl_line_object", "bl_occ_assigned", "bl_variant_condition", "bl_occ_mvl_condition", "bl_quantity", "bl_sequence_no",
				"M7_BP_ID", "M7_BP_DATE", "bl_property_overrides", "S7_MODULE_CODE", "S7_POSITION_DESC", "S7_SUPPLY_MODE",
				"bl_occ_assigned", "bl_occ_fnd0objectId"};

			// Pack 된 BOMLine이 있으면 UnPack 한다.
			// 전개되는 자동으로 Pack 되도록 설정 되어 있으므로 Unpack 하도록 한다.
			// Rich Client에 구동된 Report 모듈과 동일한 방식으로 구현 한다.
			Vector<ImanItemBOPLine> childBOMLineVector = new Vector<ImanItemBOPLine>();
			for (int i = 0; chilBOMLineObjects!=null && i < chilBOMLineObjects.length; i++) {
				
				if(chilBOMLineObjects[i]==null || (chilBOMLineObjects[i]!=null && (chilBOMLineObjects[i] instanceof BOMLine)==false)){
					continue;
				}
				
				ImanItemBOPLine currentOperationChildBOMLine = (ImanItemBOPLine)chilBOMLineObjects[i];
				try {
					currentOperationChildBOMLine = (ImanItemBOPLine)basicSoaUtil.readProperties(currentOperationChildBOMLine, 
							new String[]{"bl_pack_count","bl_packed_lines", "bl_is_packed", "bl_pack_master", "bl_real_occurrence"});
					
					if (currentOperationChildBOMLine.get_bl_is_packed()) {
						
						
						System.out.println("----/"+operationItemId+"/---------->");
						
						int packCount = currentOperationChildBOMLine.get_bl_pack_count();
						
						System.out.println("packCount = "+packCount);
						
						ModelObject[] packedLines = currentOperationChildBOMLine.get_bl_packed_lines();
						
						
						int flag = 1;    // 0:pack the lines 
						// 1:unpack the lines 
						// 2:pack all lines 
						// 3:unpack all lines

					ImanItemBOPLine[] srcBOMLines = new ImanItemBOPLine[]{currentOperationChildBOMLine};
					com.teamcenter.soa.client.model.ServiceData serviceData = 
					com.teamcenter.services.strong.structuremanagement.StructureService.getService(connection).packOrUnpack(srcBOMLines, flag);
					ImanItemBOPLine aTempBOPLine = (ImanItemBOPLine)serviceData.getUpdatedObject(0);
					//BOMLine b = (BOMLine)serviceData.getPlainObject(0);
						
						childBOMLineVector.add(aTempBOPLine);
						System.out.println("packIndex[M] = "+aTempBOPLine);
						
						for (int packIndex = 0; packedLines!=null && packIndex < packedLines.length; packIndex++) {
							ImanItemBOPLine unPackedChildBOMLine = (ImanItemBOPLine)packedLines[packIndex];
							
							System.out.println("packIndex["+packIndex+"] = "+unPackedChildBOMLine);
							childBOMLineVector.add(unPackedChildBOMLine);
						}

						System.out.println("---------------<");

//						// Pack 된것의 Data를 가져오지 못하는 것 같아 관련된 소스를 아래와 같이 참고 함.
//						ModelObject[] unPackedLines = new ModelObject[(packedLines.length + 1)];
//						unPackedLines[0] = currentOperationChildBOMLine.get_bl_pack_master();
//						
//						System.arraycopy(packedLines, 0, unPackedLines, 1, packedLines.length);
						
					}else{
						childBOMLineVector.add(currentOperationChildBOMLine);
					}
				} catch (Exception e) {
					this.logFileUtility.writeExceptionTrace(e);
				}
			}
			
			for (int findedPartIndex = 0; childBOMLineVector!=null && findedPartIndex < childBOMLineVector.size(); findedPartIndex++) {
				
				ModelObject currentChildBOMLineModelObject = childBOMLineVector.get(findedPartIndex);
				ImanItemBOPLine currentOperationChildBOPLine = (ImanItemBOPLine)currentChildBOMLineModelObject;
				
				String itemId = null;
				String itemRevId = null;
				String itemType = null;
				String itemName = null;
				String occType = null;
				ModelObject  lineObject = null;
				//String occAssigned = null;
				//String partVlConditionStr = null;
				//String parMvlConditionStr = null;
				//Item childBOMLineItem = null;
				
				String quantity = null;
				String seqNo = null;
				String bpId = null;
				String bpDate = null;
				String moduleCode = null;
				String positionDesc = null;
				String supplyMode = null;
				
				String occFndObjId = null;

				
				ItemRevision childBOMLineItemRevision = null;
				try {
					currentOperationChildBOPLine = (ImanItemBOPLine)basicSoaUtil.readProperties(currentOperationChildBOPLine, partBOMLineProperteis);
					itemId = currentOperationChildBOPLine.get_bl_item_item_id();
					itemRevId = currentOperationChildBOPLine.get_bl_rev_item_revision_id();
					itemType = currentOperationChildBOPLine.get_bl_item_object_type();
					itemName = currentOperationChildBOPLine.get_bl_rev_object_name();
					occType = currentOperationChildBOPLine.get_bl_occ_type();
					lineObject = currentOperationChildBOPLine.get_bl_line_object();
					//occAssigned = currentOperationChildBOMLine.get_bl_occ_assigned();
					//partVlConditionStr = currentOperationChildBOMLine.get_bl_variant_condition();
					//parMvlConditionStr = currentOperationChildBOMLine.get_bl_occ_mvl_condition();
					//childBOMLineItem = (Item)currentOperationChildBOMLine.get_bl_item();
					childBOMLineItemRevision =(ItemRevision)currentOperationChildBOPLine.get_bl_revision();
					quantity = currentOperationChildBOPLine.get_bl_quantity();
					
					seqNo = currentOperationChildBOPLine.get_bl_sequence_no();
					bpId = currentOperationChildBOPLine.getPropertyDisplayableValue("M7_BP_ID");
					bpDate = currentOperationChildBOPLine.getPropertyDisplayableValue("M7_BP_DATE");
				
					moduleCode = currentOperationChildBOPLine.getPropertyObject("S7_MODULE_CODE").getStringValue();
					positionDesc = currentOperationChildBOPLine.getPropertyObject("S7_POSITION_DESC").getStringValue();
					supplyMode = currentOperationChildBOPLine.getPropertyObject("S7_SUPPLY_MODE").getStringValue();
					
					occFndObjId = currentOperationChildBOPLine.get_bl_occ_fnd0objectId();

				} catch (Exception e) {
					this.logFileUtility.writeExceptionTrace(e);
				}
				
				columnDataHash.put("Col03", (Object) seqNo);
				columnDataHash.put("Col05", (Object) itemId);
				columnDataHash.put("Col07", (Object) itemRevId);

				columnDataHash.put("Col08", (Object) itemName);
	
				columnDataHash.put("Col11", (Object) moduleCode);
				
				columnDataHash.put("Col12", (Object) positionDesc);
				columnDataHash.put("Col14", (Object) supplyMode);
				
				columnDataHash.put("Col25", (Object) bpId);
				columnDataHash.put("Col26", (Object) bpDate);
				columnDataHash.put("Col30", (Object) quantity);
				
				// M7_BP_ID(35-TE039_M754XA2015A_20), M7_BP_DATE (2014-06-17 00:00:00 TO 2014-06-16 23:59:59)
				
				// ------------------------------------------------
				// 전개 제외 조건 (Start)
				// ------------------------------------------------
				if(lineObject != null && lineObject instanceof AppearanceGroup){
					//itemType="M7_MfgProduct"
					return;
				}
				if(itemType!=null && itemType.trim().equalsIgnoreCase("M7_BOPWeldOP")){
					return;
				}
				
				if(this.occTypeV!=null && this.occTypeV.contains(occType)== false){
					this.occTypeV.add(occType);
				}
				//this.logFileUtility.writeReport("OP Child ---- occType = "+occType);
				
				if(itemType!=null && (itemType.trim().equalsIgnoreCase("S7_Stdpart") || itemType.trim().equalsIgnoreCase("S7_Vehpart") )){
				//if( occType!=null && occType.trim().equalsIgnoreCase("MEConsumed")==true ){
					
					// Process에 할당된 Part BOMLine을 Product BOMLine중에 찾아서 Return 한다. 
					BOMLine[] findesBOMLineList = findInBOMLine(currentOperationChildBOPLine, productTopBOMLine);
					BOMLine delegateBOMLine = null;
					
					boolean isUnLinked = false;
					if(findesBOMLineList!=null && findesBOMLineList.length>0){
						delegateBOMLine = findesBOMLineList[0];
					}else{
						isUnLinked = true;
					}
					
					// Col45에 추가정보를 설정 해야 한다.
					boolean replaced = false;
					if(occFndObjId!=null && occFndObjId.trim().length()>0){
						// occFndObjId 와 동일한 Data가 있는지 확인한다.
						
						if(replacedEndItemOccPuidList!=null){
							if(replacedEndItemOccPuidList.contains(occFndObjId)==true){
								replaced = true;
							}
						}
					}
					
					if(replaced==true){
						if(isUnLinked==true){
							columnDataHash.put("Col46", "Unlinked + Replaced");
						}else{
							columnDataHash.put("Col46", "Replaced");
						}
					}else{
						if(isUnLinked==true){
							columnDataHash.put("Col46", "Unlinked");
						}
					}
					
					String productPartVLCond = null;
					String productPartMVLCond = null;
					String productPartSeqNo = null;
					String functionItemId = null;
					
					String partOptionCode = null;
					String partOptionDesc = null;
					
					BOMLine functionBOMLine = null;
					if(delegateBOMLine!=null){
						String[] dgBOMLineProps = new String[]{"bl_variant_condition", "bl_occ_mvl_condition", "bl_sequence_no", "bl_parent"};
						
						try {
							delegateBOMLine = (BOMLine)basicSoaUtil.readProperties(delegateBOMLine, dgBOMLineProps);
							productPartVLCond = delegateBOMLine.get_bl_variant_condition();
							productPartMVLCond = delegateBOMLine.get_bl_occ_mvl_condition();
							productPartSeqNo = delegateBOMLine.get_bl_sequence_no();
						} catch (Exception e) {
							this.logFileUtility.writeExceptionTrace(e);
						}
						
						if(productPartMVLCond!=null && productPartMVLCond.trim().length()>0){
							// 옵션값을 설정한다.
							// 수정 : bc.kim  
							// 수정 내용 : EBOM 에서 설계 변경이 일어나 Part 가 Replace 되어 BOP 와 Link 가 끊긴 경우
							//             Option 값을 공백으로 입력 해야 하는데 그러한 로직이 빠져 있음
							HashMap<String, Object> mapData = SDVBOPUtilities.getVariant(productPartMVLCond);
							partOptionCode = (String)mapData.get("printValues");
							partOptionDesc = (String)mapData.get("printDescriptions");
						}else{
							partOptionCode = "";
							partOptionDesc = "";
						}
						
						functionBOMLine = 
								findFunctionBOMLine(delegateBOMLine);
					} 
					
					columnDataHash.put("Col09", (Object) partOptionCode == null ? "" : partOptionCode);
					columnDataHash.put("Col10", (Object) partOptionDesc == null ? "" : partOptionDesc);
					
					columnDataHash.put("Col04", (Object) productPartSeqNo);
					
					if(functionBOMLine!=null){
						String[] functionBOMLineProps = new String[]{"bl_item_item_id"};
						try {
							functionBOMLine = (BOMLine)basicSoaUtil.readProperties(functionBOMLine, functionBOMLineProps);
							functionItemId = functionBOMLine.get_bl_item_item_id(); 
						} catch (Exception e) {
							this.logFileUtility.writeExceptionTrace(e);
						}
					}
					
					columnDataHash.put("Col02", (Object) functionItemId);
					
					//  
					String[] partItemRevProperties = new String[]{"owning_user", "date_released", "release_statuses", 
							"s7_PART_TYPE", "s7_DISPLAY_PART_NO", 
							"s7_ACT_WEIGHT", "s7_CAL_WEIGHT", 
							"s7_COLOR", "s7_COLOR_ID", "s7_ECO_NO", 
							"s7_EST_WEIGHT", "s7_MATERIAL", 
							"s7_REFERENCE", "s7_REGULATION", 
							"s7_SHOWN_PART_NO", "s7_THICKNESS" };
					
					String displayPartNo = null;
					String partOwningUserName = null;
					String partReleasStatusDate = null;
					String partReleasStatusName = null;
					String referenceValue = null;
					String ecoNo = null;
					String shownNo = null;
					String estWeight = null;
					String calWeight = null;
					String actWeight = null;
					String thickness = null;
					String material = null;
					String color = null;
					String colorId = null;
					String regulation = null;
					try {
						childBOMLineItemRevision = (ItemRevision)basicSoaUtil.readProperties(childBOMLineItemRevision, partItemRevProperties);
						User partOwningUser = (User)childBOMLineItemRevision.get_owning_user();
						if(partOwningUser!=null){
							partOwningUser = (User)basicSoaUtil.readProperties(partOwningUser, new String[]{"user_name"});
							partOwningUserName = partOwningUser.get_user_name();
						}
						Calendar releaseDateCal = (Calendar)childBOMLineItemRevision.get_date_released();
						if(releaseDateCal!=null){
							partReleasStatusDate = df.format(releaseDateCal.getTime());
						}
						ModelObject[] releaseStatusModels = childBOMLineItemRevision.get_release_statuses();
						for (int j = 0; j < releaseStatusModels.length; j++) {
							ReleaseStatus status = (ReleaseStatus)releaseStatusModels[j];
							status = (ReleaseStatus)basicSoaUtil.readProperties(status, new String[]{"name"});
							partReleasStatusName = status.get_name();
						}
						
						String partType = childBOMLineItemRevision.getPropertyDisplayableValue("s7_PART_TYPE");
						String dspPartNo = childBOMLineItemRevision.getPropertyDisplayableValue("s7_DISPLAY_PART_NO");
						if(partType!=null && dspPartNo!=null){
							displayPartNo = partType +" "+dspPartNo;
						}else if(partType==null && dspPartNo!=null){
							displayPartNo = dspPartNo;
						}
						
						referenceValue = childBOMLineItemRevision.getPropertyDisplayableValue("s7_REFERENCE");
						ecoNo = childBOMLineItemRevision.getPropertyDisplayableValue("s7_ECO_NO");
						
						Property  tempProperty  = (Property)childBOMLineItemRevision.getPropertyObject("s7_ECO_NO");
						if(tempProperty!=null){
							ItemRevision changeRevision = (ItemRevision) tempProperty.getModelObjectValue();
							if(changeRevision!=null){
								changeRevision = (ItemRevision)basicSoaUtil.readProperties(changeRevision, new String[]{"item_id"});
								ecoNo = changeRevision.get_item_id();
							}
						}
						
						
						if(itemType!=null && itemType.trim().equalsIgnoreCase("S7_Vehpart")){
							shownNo = childBOMLineItemRevision.getPropertyDisplayableValue("s7_SHOWN_PART_NO");
							estWeight = childBOMLineItemRevision.getPropertyDisplayableValue("s7_EST_WEIGHT");
							calWeight = childBOMLineItemRevision.getPropertyDisplayableValue("s7_CAL_WEIGHT");
							thickness = childBOMLineItemRevision.getPropertyDisplayableValue("s7_THICKNESS");
							color = childBOMLineItemRevision.getPropertyDisplayableValue("s7_COLOR");
							colorId = childBOMLineItemRevision.getPropertyDisplayableValue("s7_COLOR_ID");
							regulation = childBOMLineItemRevision.getPropertyDisplayableValue("s7_REGULATION");
						}
						actWeight = childBOMLineItemRevision.getPropertyDisplayableValue("s7_ACT_WEIGHT");
						material = childBOMLineItemRevision.getPropertyDisplayableValue("s7_MATERIAL");
						
					} catch (Exception e) {
						this.logFileUtility.writeExceptionTrace(e);
					}
						
					columnDataHash.put("Col06", (Object) displayPartNo);
					columnDataHash.put("Col13", (Object) referenceValue);
					
					columnDataHash.put("Col15", (Object) ecoNo);
					columnDataHash.put("Col16", (Object) partOwningUserName);
					
					columnDataHash.put("Col17", (Object) shownNo);
					columnDataHash.put("Col18", (Object) estWeight);
					columnDataHash.put("Col19", (Object) calWeight);
					columnDataHash.put("Col20", (Object) actWeight);
					columnDataHash.put("Col21", (Object) thickness);
					columnDataHash.put("Col22", (Object) material);
					columnDataHash.put("Col23", (Object) partReleasStatusName);
					columnDataHash.put("Col24", (Object) partReleasStatusDate);

					columnDataHash.put("Col27", (Object) color);
					columnDataHash.put("Col28", (Object) colorId);
					columnDataHash.put("Col29", (Object) regulation);
					
					indexNo++;
					
					columnDataHash.put("Col00", (Object) (""+indexNo));
					
					String rowDataString = "";
					rowDataString = rowDataString+ columnDataHash.get("Col00");
					rowDataString = rowDataString + "\t"+columnDataHash.get("Col01");
					rowDataString = rowDataString + "\t"+columnDataHash.get("Col02");
					rowDataString = rowDataString + "\t"+columnDataHash.get("Col03");
					rowDataString = rowDataString + "\t"+columnDataHash.get("Col04");
					rowDataString = rowDataString + "\t"+columnDataHash.get("Col05");
					rowDataString = rowDataString + "\t"+columnDataHash.get("Col06");
					rowDataString = rowDataString + "\t"+columnDataHash.get("Col07");
					rowDataString = rowDataString + "\t"+columnDataHash.get("Col08");
					rowDataString = rowDataString + "\t"+columnDataHash.get("Col09");
					rowDataString = rowDataString + "\t"+columnDataHash.get("Col10");
					rowDataString = rowDataString + "\t"+columnDataHash.get("Col11");
					rowDataString = rowDataString + "\t"+columnDataHash.get("Col12");
					rowDataString = rowDataString + "\t"+columnDataHash.get("Col13");
					rowDataString = rowDataString + "\t"+columnDataHash.get("Col14");
					rowDataString = rowDataString + "\t"+columnDataHash.get("Col15");
					rowDataString = rowDataString + "\t"+columnDataHash.get("Col16");
					rowDataString = rowDataString + "\t"+columnDataHash.get("Col17");
					rowDataString = rowDataString + "\t"+columnDataHash.get("Col18");
					rowDataString = rowDataString + "\t"+columnDataHash.get("Col19");
					rowDataString = rowDataString + "\t"+columnDataHash.get("Col20");
					rowDataString = rowDataString + "\t"+columnDataHash.get("Col21");
					rowDataString = rowDataString + "\t"+columnDataHash.get("Col22");
					rowDataString = rowDataString + "\t"+columnDataHash.get("Col23");
					rowDataString = rowDataString + "\t"+columnDataHash.get("Col24");
					rowDataString = rowDataString + "\t"+columnDataHash.get("Col25");
					rowDataString = rowDataString + "\t"+columnDataHash.get("Col26");
					rowDataString = rowDataString + "\t"+columnDataHash.get("Col27");
					rowDataString = rowDataString + "\t"+columnDataHash.get("Col28");
					rowDataString = rowDataString + "\t"+columnDataHash.get("Col29");
					rowDataString = rowDataString + "\t"+columnDataHash.get("Col30");
					rowDataString = rowDataString + "\t"+columnDataHash.get("Col31");
					rowDataString = rowDataString + "\t"+columnDataHash.get("Col32");
					rowDataString = rowDataString + "\t"+columnDataHash.get("Col33");
					rowDataString = rowDataString + "\t"+columnDataHash.get("Col34");
					rowDataString = rowDataString + "\t"+columnDataHash.get("Col35");
					rowDataString = rowDataString + "\t"+columnDataHash.get("Col36");
					rowDataString = rowDataString + "\t"+columnDataHash.get("Col37");
					rowDataString = rowDataString + "\t"+columnDataHash.get("Col38");
					rowDataString = rowDataString + "\t"+columnDataHash.get("Col39");
					rowDataString = rowDataString + "\t"+columnDataHash.get("Col40");
					rowDataString = rowDataString + "\t"+columnDataHash.get("Col41");
					rowDataString = rowDataString + "\t"+columnDataHash.get("Col42");
					rowDataString = rowDataString + "\t"+columnDataHash.get("Col43");
					rowDataString = rowDataString + "\t"+columnDataHash.get("Col44");
					rowDataString = rowDataString + "\t"+columnDataHash.get("Col45");
					rowDataString = rowDataString + "\t"+columnDataHash.get("Col46");
					
					Object[] rowData = new Object[47];
					
					for (int j = 0; j < rowData.length; j++) {
						
						DecimalFormat formatter = new DecimalFormat("00");
						String indexStr = "Col"+formatter.format(j);
						Object valueObject = columnDataHash.get(indexStr);
						if(valueObject==null){
							valueObject = new String("");
						}
						this.logFileUtility.writeReport("indexStr = "+indexStr+", valueObject = "+valueObject);
						
						rowData[j] = valueObject;
					}
					
					excelWorkBookWriter.writeRow("Sheet1", indexNo, rowData);

				}
				// METool, MEResource, MESubsidiary
			}
			
			//--------------------------------
    	
	}
	
	private BOMLine findFunctionBOMLine(BOMLine productPartBOMLine){

		if(productPartBOMLine==null){
			return null;
		}
		
		String[] properteis = new String[]{"bl_item_item_id", "bl_item_item_revision", "bl_rev_item_revision_id", "bl_item_object_type", "bl_item_object_name", "bl_parent"};
		
		String itemId = null;
		String itemRevId = null;
		String itemType = null;
		String itemName = null;
		try {
			productPartBOMLine = (BOMLine)basicSoaUtil.readProperties(productPartBOMLine, properteis);
			itemId = productPartBOMLine.get_bl_item_item_id();
			itemRevId = productPartBOMLine.get_bl_rev_item_revision_id();
			itemType = productPartBOMLine.get_bl_item_object_type();
			itemName = productPartBOMLine.get_bl_item_object_name();
		} catch (Exception e) {
			this.logFileUtility.writeExceptionTrace(e);
		}
		
		this.logFileUtility.writeReport("IS Function : "+itemType);

		if(itemType!=null && itemType.trim().equalsIgnoreCase("S7_Function")==true){
			return productPartBOMLine;
		}else{
			BOMLine parentBOMLine = null;
			try {
				parentBOMLine = (BOMLine)productPartBOMLine.get_bl_parent();
			} catch (NotLoadedException e) {
				this.logFileUtility.writeExceptionTrace(e);
			}
			if(parentBOMLine!=null){
				return findFunctionBOMLine(parentBOMLine);
			}
		}
		return null;
	}
	
	private BOMLine[] findInBOMLine(ImanItemBOPLine whatToFindBOPLine, BOMLine whereFindFromBOMLine){
		
		FindNodeInContextInputInfo findContextInfo = new FindNodeInContextInputInfo();
		
		findContextInfo.context = whereFindFromBOMLine;
		findContextInfo.byIdOnly = false;
		findContextInfo.allContexts = true;
		findContextInfo.nodes = new ModelObject[]{whatToFindBOPLine};
		
		Vector<BOMLine> findedBOMLineVector = new Vector<BOMLine>(); 
		FindNodeInContextResponse resp = CoreService.getService(connection).findNodeInContext(new FindNodeInContextInputInfo[]{findContextInfo});
		Core.FoundNodesInfo[] reNodes = resp.resultInfo;
		for (int i = 0; reNodes!=null && i < reNodes.length; i++) {
			NodeInfo[] nodeInfos = reNodes[i].resultNodes;
			for (int j = 0; j < nodeInfos.length; j++) {
				ModelObject[] nodes = nodeInfos[j].foundNodes;
				ModelObject orignNode = nodeInfos[j].originalNode;
				
				for (int k = 0; nodes!=null && k < nodes.length; k++) {
					
					if(nodes[k]!=null && nodes[k] instanceof BOMLine){
						
						this.logFileUtility.writeReport("nodes["+k+"] = "+((BOMLine)nodes[k]).toString());
						
						findedBOMLineVector.add((BOMLine)nodes[k]);
					}
				}
			}
		}
		
		BOMLine[] findedBOMLineList = null;
		if(findedBOMLineVector!=null && findedBOMLineVector.size()>0){
			findedBOMLineList = new BOMLine[findedBOMLineVector.size()];
			
			for (int i = 0; i < findedBOMLineVector.size(); i++) {
				findedBOMLineList[i] = findedBOMLineVector.get(i);
			}
		}
		
		return findedBOMLineList;
	}
	
//	private BOMLine initItemRevisionBasicInformation(BOMLine bomLine, String messageStr){
//		
//		BOMLine newBOMLine = bomLine;
//		
//		String[] targetPropertyNames = new String[]{"bl_item_item_id", "bl_rev_item_revision_id",
//					"bl_rev_object_name", "bl_rev_object_type", "bl_rev_object_desc",
//					"bl_occ_occurrence_type", "bl_occ_occurrence_name",
//					"bl_occ_type", "bl_abs_occ_id", "bl_item_object_type"
//				};
//
//		String itemId = null;
//		String itemRevId = null;
//		String itemRevisionName = null;
//		String itemRevisionDesc = null;
//		String itemRevisionType = null;
//		String itemObjectType = null;
//		String occurrenceType = null;
//		String occurrenceName = null;
//		String occType = null;
//		String absOccId = null;
//		
//		try {
//			newBOMLine = (BOMLine) basicSoaUtil.readProperties(bomLine, targetPropertyNames);
//
//			itemId = newBOMLine.get_bl_item_item_id();
//			itemRevId = newBOMLine.get_bl_rev_item_revision_id();
//			itemRevisionName = newBOMLine.get_bl_rev_object_name();
//			itemRevisionDesc = newBOMLine.get_bl_rev_object_desc();
//			itemRevisionType = newBOMLine.get_bl_rev_object_type();
//			occurrenceType = newBOMLine.get_bl_occ_occurrence_type();
//			occurrenceName = newBOMLine.get_bl_occ_occurrence_name();
//			occType = newBOMLine.get_bl_occ_type();
//			absOccId = newBOMLine.get_bl_abs_occ_id();
//			itemObjectType = newBOMLine.get_bl_item_object_type();
//			
//		} catch (Exception e) {
//			this.logFileUtility.writeExceptionTrace(e);
//		}
//		
//		if(messageStr==null || (messageStr!=null && messageStr.trim().length()<1)){
//			messageStr = "";
//		}
//		
//		logFileUtility.writeReport(messageStr+" "+itemId+"/"+itemRevId+" "+itemRevisionName+" ["+itemRevisionType+", "+itemObjectType+"] "+occurrenceName+", "+absOccId+" ["+occurrenceType+", "+occType+"]");
//		
//		return newBOMLine;
//	}
	

	
//	/**
//	 * 이건 다음에 Test 하자.
//	 */
//	private void assign(){
//		//DataManagementService.getService(connection).connectObjects(paramArrayOfConnectObjectsInputData)
//		//DataManagementService.getService(connection).disconnectObjects(paramArrayOfDisconnectInput)
//		//DataManagementService.getService(connection).addOrRemoveAssociatedContexts(paramArrayOfAddOrRemoveContextsInfo)
//	}
	
//	/**
//	 * BOM을 전개해서 해당 BOMLine을 찾는 Service를 Test 하는 Function
//	 * (해당 BOMLIne을 찾아 오기는 하는데 시간이 매우 많이 걸림.)
//	 */
//	private void bomExpandAndFind(){
//		BOMWindow productWindow = null;
//		BOMLine productTopBOMLine = mppTopLines.productLine;
//		
//		Date sD = new Date();
//		try {
//			
//			BOMLine[] findTargetBOMLine = new BOMLine[]{productTopBOMLine};
//			StructureFilterWithExpandService kkk = StructureFilterWithExpandService.getService(connection);
//			SearchCondition condition = new SearchCondition();
//			condition.logicalOperator = "OR"; // "AND", "OR"
//			condition.inputValue="xHSJPNCCoNUN7C";
//			condition.relationalOperator = "="; // "=", ">", ">=","<", "<="
//			condition.propertyName="bl_abs_occ_id";
//			ExpandAndSearchResponse  resp = kkk.expandAndSearch(findTargetBOMLine, new SearchCondition[]{condition});
//			if(resp!=null){
//				StructureFilterWithExpand.ExpandAndSearchOutput[] outputLines = resp.outputLines;
//				if(outputLines==null){
//					logFileUtility.writeReport("$$$$$$$$$$--- EEEE");
//				}else{
//					logFileUtility.writeReport("$$$$$$$$$$--- "+outputLines.length);
//					for (int i = 0; i < outputLines.length; i++) {
//						logFileUtility.writeReport("############## Out PUt = "+outputLines[i].resultLine);
//					}
//				}
//			}
//			
//		} catch (Exception e) {
//			this.logFileUtility.writeExceptionTrace(e);
//		}finally{
//			Date eD = new Date();
//			logFileUtility.writeReport("S : "+df.format(sD)+" -> E : "+df.format(eD));
//		}
//	}
	
	/**
	 * Product / Process / Plant Window의 정보를 출력하는 Function
	 * @param mppTopLines
	 */
	private void printMPPTopLines(MPPTopLines mppTopLines){
		
		String[] properteis = new String[]{"bl_item_item_id", "bl_item_item_revision", "bl_rev_item_revision_id", "bl_item_object_type", "bl_item_object_name", "bl_window"};
		BOMLine tempBOMLine  = mppTopLines.productLine;
		try {
			tempBOMLine = (BOMLine)basicSoaUtil.readProperties(tempBOMLine, properteis);
			
			String itemId = tempBOMLine.get_bl_item_item_id();
			String itemRevId = tempBOMLine.get_bl_rev_item_revision_id();
			String itemType = tempBOMLine.get_bl_item_object_type();
			String itemName = tempBOMLine.get_bl_item_object_name();
			this.logFileUtility.writeReport("BOMLine = "+itemId+"/"+itemRevId+" "+itemName+" ["+itemType+"]");
			
			//ModelObject windowModel = tempOBMLine.get_bl_window();
			//this.logFileUtility.writeReport("windowModel.getClass().getName() = "+windowModel.getClass().getName());

		} catch (Exception e) {
			this.logFileUtility.writeExceptionTrace(e);
		}

		Mfg0BvrProcess tempProcessLine  = mppTopLines.processLine;

		try {
			// ImanItemBOPLine
			tempProcessLine = (Mfg0BvrProcess) basicSoaUtil.readProperties(tempProcessLine, properteis);
			String itemId = tempProcessLine.get_bl_item_item_id();
			String itemRevId = tempProcessLine.get_bl_rev_item_revision_id();
			String itemType = tempProcessLine.get_bl_item_object_type();
			String itemName = tempProcessLine.get_bl_item_object_name();
			this.logFileUtility.writeReport("ProcessLine = "+itemId+"/"+itemRevId+" "+itemName+" ["+itemType+"]");
			
			//ModelObject windowModel = tempProcessLine.get_bl_window();
			//this.logFileUtility.writeReport("windowModel.getClass().getName() = "+windowModel.getClass().getName());

		} catch (Exception e) {
			this.logFileUtility.writeExceptionTrace(e);
		}
		
		Mfg0BvrWorkarea tempPlantLine  = mppTopLines.plantLine;
		try {
			tempPlantLine = (Mfg0BvrWorkarea) basicSoaUtil.readProperties(tempPlantLine, properteis);
			
			String itemId = tempPlantLine.get_bl_item_item_id();
			String itemRevId = tempPlantLine.get_bl_rev_item_revision_id();
			String itemType = tempPlantLine.get_bl_item_object_type();
			String itemName = tempPlantLine.get_bl_item_object_name();
			tempPlantLine.get_bl_occ_type();
			this.logFileUtility.writeReport("PlantLine = "+itemId+"/"+itemRevId+" "+itemName+" ["+itemType+"]");
			
			//ModelObject windowModel = tempPlantLine.get_bl_window();
			//this.logFileUtility.writeReport("windowModel.getClass().getName() = "+windowModel.getClass().getName());

		} catch (Exception e) {
			this.logFileUtility.writeExceptionTrace(e);
		}
		
	}
	
	/**
	 * Product / Process / Plant Window의 정보를 출력하는 Function
	 * @param mppTopLines
	 */
	private void processStructureExpandTest(MPPTopLines mppTopLines){
		
		ImanItemBOPLine processLine  = (ImanItemBOPLine)mppTopLines.processLine;
		
		ImanItemBOPLine t1Line = getTargetChildBOPLine(processLine, "PTP-A1-T1-PVXA2015-00");
		if(t1Line==null){
			System.out.println("T1 Line not found...");
			return;
		}

		ImanItemBOPLine targetOperation = getTargetChildBOPLine(t1Line, "35-1D-240-0700-00");
		if(targetOperation==null){
			System.out.println("35-1D-240-070-00 operation not found...");
			return;
		}		
		
		System.out.println("/////////////////////////////////\n// Find\n/////////////////////////////////");
		
		printChildBOPLine(targetOperation);
		
	}
	
	private ImanItemBOPLine getTargetChildBOPLine(ImanItemBOPLine bopLine, String targetItemId){
		
		ImanItemBOPLine findesBOPLine = null;
		if(bopLine==null){
			return findesBOPLine;
		}
		
		if(targetItemId==null || (targetItemId!=null && targetItemId.trim().length()<1)){
			return findesBOPLine;
		}
		
    	try {
			basicSoaUtil.readProperties(bopLine, new String[]{"bl_all_child_lines"});
			ModelObject[] chilBOMLineObjects = bopLine.get_bl_all_child_lines();
			for (int i = 0; chilBOMLineObjects!=null && i < chilBOMLineObjects.length; i++) {
				
				String[] properteis = new String[]{"bl_item_item_id", "bl_item_item_revision", "bl_rev_item_revision_id", "bl_item_object_type", "bl_item_object_name", "bl_window"};
				
				ImanItemBOPLine currentBOPLine  = (ImanItemBOPLine)chilBOMLineObjects[i];
				try {
					// ImanItemBOPLine
					currentBOPLine = (ImanItemBOPLine) basicSoaUtil.readProperties(currentBOPLine, properteis);
					
					String itemId = currentBOPLine.get_bl_item_item_id();
					String itemRevId = currentBOPLine.get_bl_rev_item_revision_id();
					String itemType = currentBOPLine.get_bl_item_object_type();
					String itemName = currentBOPLine.get_bl_item_object_name();
					
					if(itemId!=null && itemId.trim().equalsIgnoreCase(targetItemId.trim())==true){
						findesBOPLine = currentBOPLine;
					}
					
					//this.logFileUtility.writeReport("ProcessLine = "+itemId+"/"+itemRevId+" "+itemName+" ["+itemType+"]");

				} catch (Exception e) {
					this.logFileUtility.writeExceptionTrace(e);
				}
				
			}
		} catch (Exception e) {
			this.logFileUtility.writeExceptionTrace(e);
		}
	
		return findesBOPLine;
	}
	
	private void printChildBOPLine(ImanItemBOPLine bopLine){
		
		if(bopLine==null){
			return;
		}
		
    	try {
			basicSoaUtil.readProperties(bopLine, new String[]{"bl_all_child_lines"});
			ModelObject[] chilBOMLineObjects = bopLine.get_bl_all_child_lines();
			
			for (int i = 0; chilBOMLineObjects!=null && i < chilBOMLineObjects.length; i++) {
				
				if(chilBOMLineObjects[i] instanceof ImanItemBOPLine){
					String[] properteis = new String[]{"bl_item_item_id", "bl_rev_item_revision_id"};
					
					ImanItemBOPLine tempImanItemBOPLine = (ImanItemBOPLine)chilBOMLineObjects[i]; 
					tempImanItemBOPLine = (ImanItemBOPLine)basicSoaUtil.readProperties(tempImanItemBOPLine, properteis);
					String itemId = tempImanItemBOPLine.get_bl_item_item_id();
					String itemRevId = tempImanItemBOPLine.get_bl_rev_item_revision_id();

					System.out.println("itemId/itemRevId = "+itemId+"/"+itemRevId);
				}
			}
		} catch (Exception e) {
			this.logFileUtility.writeExceptionTrace(e);
		}
	
	}
	
//	private void expandA(ImanItemBOPLine bopLine){
//		
//    	try {
//			basicSoaUtil.readProperties(bopLine, new String[]{"bl_all_child_lines"});
//			ModelObject[] chilBOMLineObjects = bopLine.get_bl_all_child_lines();
//			
//			for (int i = 0; chilBOMLineObjects!=null && i < chilBOMLineObjects.length; i++) {
//				// Child Node를 전개하는 Function을 재귀호출 한다.
//				if( chilBOMLineObjects[i]!=null && chilBOMLineObjects[i] instanceof BOMLine){
//					expandAllChildLine((ImanItemBOPLine)chilBOMLineObjects[i]);
//				}
//			}
//		} catch (Exception e) {
//			this.logFileUtility.writeExceptionTrace(e);
//		}
//
//	}

	private void initReplacedEndItem(){
		
		ArrayList<EndItemData> replacedItem  = null;
		
		// Process Top BOMLine Item Puid, Product Item Id 를 이용해 자동변경된 Data List Up
		// 수행 시간 많이 걸림 (80초)
		this.logFileUtility.writeReport("this.processTopItemPuid = "+this.processTopItemPuid);
		this.logFileUtility.writeReport("this.mProductItemId = "+this.mProductItemId);
		
    	try {
//			replacedItem = SDVBOPUtilities.findReplacedEndItems(this.processTopItemPuid, this.mProductItemId);
    		// 기존의 Replaced Items 기능의 쿼리를 수정 하여 그 시간을 단축한 쿼리  
    		// 이 메서드는 End Item List Report 기능에서만 사용됨
			replacedItem = SDVBOPUtilities.findReplacedRootEndItems(this.processTopItemPuid, this.mProductItemId);
		} catch (Exception e) {
			this.logFileUtility.writeExceptionTrace(e);
		}

    	if(replacedEndItemOccPuidList==null){
    		replacedEndItemOccPuidList = new ArrayList<String>();
    	}else{
    		replacedEndItemOccPuidList.clear();
    	}
    	
    	for (int i = 0; replacedItem!=null && i < replacedItem.size(); i++) {
    		EndItemData replacedEndItem = replacedItem.get(i);
    		String tempPUID = replacedEndItem.getOcc_puid();
    		
			if (tempPUID!=null && replacedEndItemOccPuidList.contains(tempPUID)==false) {
				replacedEndItemOccPuidList.add(tempPUID);
				//this.logFileUtility.writeReport("Replaced BOMLine["+i+"] = "+tempPUID);
			}
		}
	}
}
