package com.ssangyong.soa.bop.reports;

import java.io.File;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import com.ssangyong.CommonConstants;
import com.ssangyong.common.remote.SendMailEAIUtil;
import com.ssangyong.soa.bop.util.BasicSoaUtil;
import com.ssangyong.soa.bop.util.LogFileUtility;
import com.ssangyong.soa.bop.util.MPPTopLines;
import com.ssangyong.soa.bop.util.MppUtil;
import com.teamcenter.soa.client.Connection;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.Property;
import com.teamcenter.soa.client.model.strong.AppearanceGroup;
import com.teamcenter.soa.client.model.strong.BOMLine;
import com.teamcenter.soa.client.model.strong.BOMWindow;
import com.teamcenter.soa.client.model.strong.CfgActivityLine;
import com.teamcenter.soa.client.model.strong.Item;
import com.teamcenter.soa.client.model.strong.ItemRevision;
import com.teamcenter.soa.client.model.strong.MEActivity;
import com.teamcenter.soa.client.model.strong.MECollaborationContext;
import com.teamcenter.soa.client.model.strong.Mfg0BvrOperation;
import com.teamcenter.soa.client.model.strong.NoteType;
import com.teamcenter.soa.client.model.strong.RevisionRule;
import com.teamcenter.soa.client.model.strong.User;
import com.teamcenter.soa.client.model.strong.WorkspaceObject;
import com.teamcenter.soa.exceptions.NotLoadedException;

/**
 * 부자재 목록 Report
 * Structure를 Operatoin Level 까지 전개 한다.
 * Operation의 Child Node로 부자재들이 있다.
 * @author tj
 *
 */
public class OperationMasterListReport {
	
	Connection connection;
	String targetCCName;

	BasicSoaUtil basicSoaUtil;
	MppUtil mppUtil;
	LogFileUtility logFileUtility; 
	MPPTopLines mppTopLines;

	DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	Vector<String> reportTargeItemType  = null;
	BOMLine productTopBOMLine = null;
	
	String plantCode = null;
	String korCarName = null;
	String engCarName = null;
	String vechileCode = null;
	String productCode = null;
	String mProductItemId = null;
	String mProductItemPUID = null;
	
	String processTopItemId = null;
	String processTopItemPuid = null;
	BOMLine processTopBOMLine = null;
	String processType = null;
	
	//-----------------------------------
	
	BOMLine currentLineBOMLine = null;
	String currentLineItemId = null;
	String currentLineCode = null;
	String currentLineItemRevId = null;
	
	BOMLine currentStationBOMLine = null;
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
	
	HashMap<String, Object> columnDataHash = null;
	
	//////////////////////////////////////////////////////////////////////////////////////
	/*
	 * 수정점 : 20200330
	 * 수정내용 : MPP -> Assembly BOP -> Reports ->  Process Master List(On Server)
	 * 			  "작업정보"를 추가 하기위해 writeRow 메서드 추가 및 setCellStyleOfHeaderInfo 메서드 추가 
	 */
	private String selectedValue = "";
	//////////////////////////////////////////////////////////////////////////////////////
	
	public OperationMasterListReport(Connection connection, String ccName){
		
		this.connection = connection;
		this.targetCCName = ccName;
		
		this.basicSoaUtil = new BasicSoaUtil(this.connection);
		String userId = this.basicSoaUtil.getLoginUserId();
		DateFormat fileNameDf = new SimpleDateFormat("yyyyMMdd_HHmmss");
		String timeStr = fileNameDf.format(new Date());
		this.logFileUtility = new LogFileUtility("OMLReport_"+ccName+"["+timeStr+"].txt");
		this.logFileUtility.setOutUseSystemOut(true);
		
		this.mppUtil = new MppUtil(connection);
		
		this.reportTargeItemType  = new Vector<String>();
		this.reportTargeItemType.add("M7_BOPBodyOp");
		this.reportTargeItemType.add("M7_BOPPaintOp");
		this.reportTargeItemType.add("M7_BOPAssyOp");
		
	}
	
	public boolean makeReport(){
		
		this.logFileUtility.writeReport("Make Report");
	
		logFileUtility.setTimmerStarat();
		logFileUtility.writeReport("Find CC...");
		
		// CC를 찾는다
		MECollaborationContext aMECollaborationContext = mppUtil.findMECollaborationContext(targetCCName);
		if(aMECollaborationContext==null){
			logFileUtility.writeReport("Return ["+logFileUtility.getElapsedTime()+"] : CC is null!!");
			return isSuccess;
		}

		// MECollaborationContext의 Structure Context Object를 찾아 온다.
		try {
			String[] propertyNames = new String[]{"object_name","structure_contexts"};
			aMECollaborationContext = (MECollaborationContext)basicSoaUtil.readProperties(aMECollaborationContext, propertyNames);
			this.logFileUtility.writeReport("aMECollaborationContext.get_object_name() = "+aMECollaborationContext.get_object_name());
		} catch (Exception e) {
			this.logFileUtility.writeExceptionTrace(e);
		}
		
		logFileUtility.writeReport("Open BOP Window...");
		
    	// Product, Process, Plant의 Top BOMLine을 가져온다.
		// 아래의 Function 실행을 하지 않으면 mppTopLines의 값들이 초기화 되지 않은
		// 상태로 남아 있음.
    	try {
			mppTopLines = mppUtil.openCollaborationContext(aMECollaborationContext);
		} catch (Exception e) {
			this.logFileUtility.writeExceptionTrace(e);
		}
    	
    	if(mppTopLines==null){
    		logFileUtility.writeReport("Return ["+logFileUtility.getElapsedTime()+"] : mppTopLines is null!!");
    		return isSuccess;
    	}
    	
    	initProductBasicInformation();
    	
    	unpack();
    	
    	String structureDataInitTime = logFileUtility.getElapsedTime();

		boolean isReadyOk = false;
		
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_ss");
		String newFileName = "OMLReport_"+this.productCode+"["+simpleDateFormat.format(new Date())+"]";
		reportFilePath = reportDestinationFolderPath + File.separator + newFileName+".xlsx";

		this.reportTemplateFile = SDVBOPUtilities.getReportExcelTemplateFile(connection, reportDestinationFolderPath, "ME_DOCTEMP_07", reportFilePath);
		
		if(this.reportTemplateFile!=null && this.reportTemplateFile.exists()==true){
			
			//this.reportTemplateFile.renameTo(new File(reportFilePath));
			
			excelWorkBookWriter = new ExcelWorkBookWriter(reportFilePath);

			int templateDataStartRowIndex = 5;
			int templateDataStartColumnIndex = 0;
			int templateReadyLastRowIndex = templateDataStartRowIndex+3;
			int[] wrapTextColumns = new int[]{25,26,27,28,29,30,31,32,33};
			excelWorkBookWriter.setWrapTextColumns(wrapTextColumns);
			
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
					expandAllChildLine((BOMLine)chilBOMLineObjects[i]);
				}
			}
			////////////////////////////테스트//////////////////////////////////////////////////////////////////
//			for (int i = 3; chilBOMLineObjects!=null && i < 5; i++) {
//				
//				if(limitIndexCount!=0 && this.indexNo>limitIndexCount){
//					break;
//				}
//				
//				if( chilBOMLineObjects[i]!=null && chilBOMLineObjects[i] instanceof BOMLine){
//					expandAllChildLine((BOMLine)chilBOMLineObjects[i]);
//				}
//			}
			//////////////////////////////////////////////////////////////////////////////////////////////
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
    	
    	logFileUtility.writeReport("Structure Init Time :  ["+structureDataInitTime+"]");
    	logFileUtility.writeReport("Replaced Part Init Time :  ["+replacedPartInitTime+"]");
    	logFileUtility.writeReport("End ["+logFileUtility.getElapsedTime()+"]");
    	
    	return isSuccess;
	}
	
	private void printTitleInformation(){
		
		String productInfoString = this.productCode + "_" + this.processTopItemId;
		
		try {
			processTopBOMLine = (BOMLine) basicSoaUtil.readProperties(processTopBOMLine, new String[]{"bl_window"});
			
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
			
			/* 수정점 - 202001006
			 * 리포트 작성 날짜를 입력 하기 위해 현재 날짜를 구해 입력 하는데 
			 * 맨 끝 컬럼 위에 입력 하기 위해 컬럼 값을 수정 
			 * 2개 컬럼 추가 로  기존 35 -> 39로 수정 
			 */
			/////////////////////////////////////////////////////////////////////////////////////
			/*
			 * 수정점 : 20200330
			 * 수정내용 : MPP -> Assembly BOP -> Reports ->  Process Master List(On Server)
			 * 			  "작업정보" 추가를 위해 수정 
			 * 			  작업정보를 추가 하면(selectedValue 값이 1일경우) Process Master List 에 작업코드, 작업시간, 작업이름 컬럼 추가 
			 */
			
			int reportDateIndex = 40;
			if( selectedValue.equals("1") ) {
				
				reportDateIndex = excelWorkBookWriter.writeRow("Sheet1");
				reportDateIndex = reportDateIndex - 1;
			}
			
			/////////////////////////////////////////////////////////////////////////////////////
			excelWorkBookWriter.writeRow("Sheet1", 2, reportDateIndex, reportDate);
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
			processTopBOMLine = (BOMLine)this.mppTopLines.processLine;
			processTopBOMLine = (BOMLine) basicSoaUtil.readProperties(processTopBOMLine, new String[]{"bl_item", "bl_revision"});
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
				plantCode = processItemRevision.getPropertyDisplayableValue("m7_PROCESS_TYPE");
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
	 * @param bomLine
	 */
	private void expandAllChildLine(BOMLine bomLine){
		
		if(limitIndexCount!=0 && this.indexNo>limitIndexCount){
			return;
		}
		
		String[] properteis = new String[]{"bl_item_item_id", "bl_rev_item_revision_id", "bl_item_object_type", "bl_line_object", "bl_revision"};
		
		String itemId = null;
		String itemRevId = null;
		String itemType = null;
		ModelObject  lineObject = null;
		ItemRevision itemRevision = null;
		try {
			bomLine = (BOMLine)basicSoaUtil.readProperties(bomLine, properteis);
			itemId = bomLine.get_bl_item_item_id();
			itemRevId = bomLine.get_bl_rev_item_revision_id();
			itemType = bomLine.get_bl_item_object_type();
			lineObject = bomLine.get_bl_line_object();
			itemRevision = (ItemRevision)bomLine.get_bl_revision();
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
			if(this.currentLineBOMLine==null || (this.currentLineBOMLine!=null && this.currentLineBOMLine.equals(bomLine)==false)){
				this.currentLineBOMLine = bomLine;
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
//				else if(this.currentLineCode!=null && this.currentLineCode.trim().equalsIgnoreCase("I1")==false){
//					return;
//				}
				
				// operation type(차체(B), 도장(P), 조립(A))
				if (processType!=null && processType.startsWith("B")) {
				} else if (processType!=null && processType.startsWith("P")) {
					 // 도장 - 편성버전이 00인것만 출력한다
	                String paintLineCode = this.currentLineItemId.substring(this.currentLineItemId.length() - 2);
	                if (paintLineCode.equals("00")==false) {
	                    return;
	                }
				} else if (processType!=null && processType.startsWith("A")) {
				}
				
			}
		}

		if(itemType!=null && itemType.trim().equalsIgnoreCase("M7_BOPStation")){
			if(this.currentStationBOMLine==null || (this.currentStationBOMLine!=null && this.currentStationBOMLine.equals(bomLine)==false)){
				this.currentStationBOMLine = bomLine;
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
			makeOperatonReportData(bomLine);
			return;
		}
		
		// ----------------------------------------------------------------------------------
		// 제귀 호출을 이용해 Child Node를 계속 전개 한다.
		// ----------------------------------------------------------------------------------
    	try {
			basicSoaUtil.readProperties(bomLine, new String[]{"bl_all_child_lines"});
			ModelObject[] chilBOMLineObjects = bomLine.get_bl_all_child_lines();
			
			for (int i = 0; chilBOMLineObjects!=null && i < chilBOMLineObjects.length; i++) {
				// Child Node를 전개하는 Function을 재귀호출 한다.
				if( chilBOMLineObjects[i]!=null && chilBOMLineObjects[i] instanceof BOMLine){
					expandAllChildLine((BOMLine)chilBOMLineObjects[i]);
				}
			}
		} catch (Exception e) {
			this.logFileUtility.writeExceptionTrace(e);
		}
    	
	}
	
	/**
	 * Operation의 Child Node 정보를 Report에 List 할 형태로 만든다.
	 * @param operationBOMLine
	 */
	private void makeOperatonReportData(BOMLine operationBOMLine){
		
		if(limitIndexCount!=0 && this.indexNo>limitIndexCount){
			return;
		}

		this.columnDataHash = new HashMap<String, Object>();
		
		// Activity List를 Vector에 담는다.
		Vector<MEActivity> activityVector = getMEActivitys(operationBOMLine);
		
		// -------------------------------------------------
		// 여기서부터 공정정보
		// -------------------------------------------------
		initOperationInformation(operationBOMLine, activityVector);

		// -------------------------------------------------
		// 여기서부터 시간정보
		// -------------------------------------------------
		Hashtable<String, String> returnValuHash = getTimeInfo(operationBOMLine, activityVector);
		
//		columnDataHash.put("Col10", returnValuHash.get("autoMaticTime") );				// 자동시간
//		columnDataHash.put("Col11", returnValuHash.get("workerNetTime") );				// 작업자정미시간
//		columnDataHash.put("Col12", returnValuHash.get("cycleTime") );					// 정미합
//		columnDataHash.put("Col13", returnValuHash.get("allowance") );					// 부대계수
//		columnDataHash.put("Col14", returnValuHash.get("standardTime") );				// 표준시간
//		columnDataHash.put("Col15", returnValuHash.get("assistantTime") );				// 보조시간
//		columnDataHash.put("Col16", returnValuHash.get("workingTime") );				// 작업시간
//		columnDataHash.put("Col17", returnValuHash.get("maXWorkingTime") );			// 편성시간(최대)
//		columnDataHash.put("Col18", returnValuHash.get("representWorkingTime") );	// 편성시간 (대표차종)
		// 공법 마스터 리스트에 KPC 내용 컬럼 추가
		columnDataHash.put("Col11", returnValuHash.get("autoMaticTime") );				// 자동시간
		columnDataHash.put("Col12", returnValuHash.get("workerNetTime") );				// 작업자정미시간
		columnDataHash.put("Col13", returnValuHash.get("cycleTime") );					// 정미합
		columnDataHash.put("Col14", returnValuHash.get("allowance") );					// 부대계수
		columnDataHash.put("Col15", returnValuHash.get("standardTime") );				// 표준시간
		columnDataHash.put("Col16", returnValuHash.get("assistantTime") );				// 보조시간
		columnDataHash.put("Col17", returnValuHash.get("workingTime") );				// 작업시간
		columnDataHash.put("Col18", returnValuHash.get("maXWorkingTime") );			// 편성시간(최대)
		columnDataHash.put("Col19", returnValuHash.get("representWorkingTime") );	// 편성시간 (대표차종)
		
		// -------------------------------------------------
		// Child Node 전개
		// -------------------------------------------------
		String[] operationBOMLineProperteis = new String[]{"bl_all_child_lines", "bl_revision"};
		ModelObject[] chilBOMLineObjects = null;
		try {
			operationBOMLine = (BOMLine)basicSoaUtil.readProperties(operationBOMLine, operationBOMLineProperteis);
		} catch (Exception e) {
			this.logFileUtility.writeExceptionTrace(e);
		}
		try {
			chilBOMLineObjects = operationBOMLine.get_bl_all_child_lines();
		} catch (NotLoadedException e) {
			this.logFileUtility.writeExceptionTrace(e);
		}
		
		// Pack 된 BOMLine이 있으면 UnPack 한다.
		// 전개되는 자동으로 Pack 되도록 설정 되어 있으므로 Unpack 하도록 한다.
		// Rich Client에 구동된 Report 모듈과 동일한 방식으로 구현 한다.
		Vector<BOMLine> childBOMLineVector = new Vector<BOMLine>();
		for (int i = 0; chilBOMLineObjects!=null && i < chilBOMLineObjects.length; i++) {

			if(chilBOMLineObjects[i]==null || (chilBOMLineObjects[i]!=null && (chilBOMLineObjects[i] instanceof BOMLine)==false)){
				continue;
			}
			
			ModelObject  lineObject = null;
			BOMLine currentOperationChildBOMLine = (BOMLine)chilBOMLineObjects[i];
			
			try {
				currentOperationChildBOMLine = (BOMLine)basicSoaUtil.readProperties(currentOperationChildBOMLine, 
						new String[]{"bl_pack_count","bl_packed_lines", "bl_is_packed", "bl_item_object_type","bl_line_object", "bl_item_item_id"});
			} catch (Exception e) {
				this.logFileUtility.writeExceptionTrace(e);
			}
			try {
				lineObject = currentOperationChildBOMLine.get_bl_line_object();
				// ------------------------------------------------
				// 전개 제외 조건 (Start)
				// ------------------------------------------------
				if(lineObject != null && lineObject instanceof AppearanceGroup){
					//itemType="M7_MfgProduct"
					return;
				}
			} catch (Exception e) {
				this.logFileUtility.writeExceptionTrace(e);
			}
			
			boolean isPacked = false;
			try {
				isPacked = currentOperationChildBOMLine.get_bl_is_packed();
			} catch (Exception e) {
				this.logFileUtility.writeExceptionTrace(e);
			}
			
			if (isPacked==true) {
				try {
					int packCount = currentOperationChildBOMLine.get_bl_pack_count();
					
					// Pack 된것의 Data를 가져오지 못하는 것 같아 관련된 소스를 아래와 같이 참고 함.
					ModelObject[] packedLines = new ModelObject[currentOperationChildBOMLine.get_bl_packed_lines().length + 1];
					packedLines[0] = currentOperationChildBOMLine;
					System.arraycopy(currentOperationChildBOMLine.get_bl_packed_lines(), 0, packedLines, 1, currentOperationChildBOMLine.get_bl_packed_lines().length);
					packedLines = currentOperationChildBOMLine.get_bl_packed_lines();
					
					for (int packIndex = 0; packedLines!=null && packIndex < packedLines.length; packIndex++) {
						BOMLine packedChildBOMLine = (BOMLine)packedLines[packIndex];
						
						try {
							packedChildBOMLine = (BOMLine)basicSoaUtil.readProperties(packedChildBOMLine, 
									new String[]{"bl_pack_count","bl_packed_lines", "bl_is_packed", "bl_item_object_type","bl_line_object", "bl_item_item_id"});
						} catch (Exception e) {
							this.logFileUtility.writeExceptionTrace(e);
						}
						
						childBOMLineVector.add(packedChildBOMLine);
					}

				} catch (NotLoadedException e) {
					this.logFileUtility.writeExceptionTrace(e);
				}
			}else{
				childBOMLineVector.add(currentOperationChildBOMLine);
			}
			
		}
		
		ItemRevision operationItemRevision = null;
		try {
			operationItemRevision = (ItemRevision) operationBOMLine.get_bl_revision();
		} catch (NotLoadedException e) {
			e.printStackTrace();
		}
		
		System.out.println("childBOMLineVector : "+ childBOMLineVector);
		
		String drValue = null;
		if(childBOMLineVector!=null && operationItemRevision!=null){
			drValue = getDRValue(childBOMLineVector, operationItemRevision);
		}
		columnDataHash.put("Col07", drValue);
		
		//--------------------------------
		// 여기서부터 공구정보
		//--------------------------------
    	
    	String madeToolId = "";
    	String madeToolName = "";
    	String madeToolSpec = "";
    	String madeToolTorque = "";
    	String madeToolQty = "";
    	
		List<HashMap<String, Object>> toolList = getToolInformation(childBOMLineVector);
		for (int i = 0; toolList!=null && i < toolList.size(); i++) {
			HashMap<String, Object> toolMap = toolList.get(i);
			
			String toolId = (String)toolMap.get("bl_item_item_id");
	    	if(toolId==null || (toolId!=null && toolId.trim().length()<1)){
	    		toolId = " ";
	    	}
			String toolName = (String)toolMap.get("m7_ENG_NAME");
	    	if(toolName==null || (toolName!=null && toolName.trim().length()<1)){
	    		toolName = " ";
	    	}
			String toolSpec = (String)toolMap.get("m7_SPEC_ENG");
	    	if(toolSpec==null || (toolSpec!=null && toolSpec.trim().length()<1)){
	    		toolSpec = "-";
	    	}

	    	String toolTorque = (String)toolMap.get("M7_TORQUE");
	    	if(toolTorque==null || (toolTorque!=null && toolTorque.trim().length()<1)){
	    		toolTorque = " ";
	    	}	    	
	    	String toolTorqueValue = (String)toolMap.get("M7_TORQUE_VALUE");
	    	if(toolTorqueValue==null || (toolTorqueValue!=null && toolTorqueValue.trim().length()<1)){
	    		toolTorqueValue = " ";
	    	}
	    	String toolTorqueStr = null;
	    	if( (toolTorque!=null && toolTorque.trim().length()>0) && (toolTorqueValue!=null && toolTorqueValue.trim().length()>0) ){
	    		toolTorqueStr = toolTorque+" "+toolTorqueValue;
	    	}else{
	    		toolTorqueStr = " ";
	    	}
	    	
			String toolQty = (String)toolMap.get("bl_quantity");
	    	if(toolQty==null || (toolQty!=null && toolQty.length()<1)){
	    		toolQty = "";
	    	}
	    	
	    	if(madeToolId==null || (madeToolId!=null && madeToolId.length()<1) ){
	    		madeToolId = toolId;
	    	}else{
	    		madeToolId = madeToolId+"\n"+toolId;
	    	}
	    	if(madeToolName==null || (madeToolName!=null && madeToolName.length()<1) ){
	    		madeToolName = toolName;
	    	}else{
	    		madeToolName = madeToolName+"\n"+toolName;
	    	}
	    	if(madeToolSpec==null || (madeToolSpec!=null && madeToolSpec.length()<1) ){
	    		madeToolSpec = toolSpec;
	    	}else{
	    		madeToolSpec = madeToolSpec+"\n"+toolSpec;
	    	}
	    	if(madeToolTorque==null || (madeToolTorque!=null && madeToolTorque.length()<1) ){
	    		madeToolTorque = toolTorqueStr;
	    	}else{
	    		madeToolTorque = madeToolTorque+"\n"+toolTorqueStr;
	    	}
	    	if(madeToolQty==null || (madeToolQty!=null && madeToolQty.length()<1) ){
	    		madeToolQty = toolQty;
	    	}else{
	    		madeToolQty = madeToolQty+"\n"+toolQty;
	    	}
		}
		
//		columnDataHash.put("Col26", madeToolId);
//		columnDataHash.put("Col27", madeToolName);
//		columnDataHash.put("Col28", madeToolSpec);
//		columnDataHash.put("Col29", madeToolTorque);
//		columnDataHash.put("Col30", madeToolQty);
		
		// 공법 마스터 리스트에 KPC 내용 컬럼 추가
		columnDataHash.put("Col27", madeToolId);
		columnDataHash.put("Col28", madeToolName);
		columnDataHash.put("Col29", madeToolSpec);
		columnDataHash.put("Col30", madeToolTorque);
		columnDataHash.put("Col31", madeToolQty);
		
		//--------------------------------
		// 여기서부터 설비정보
		//--------------------------------
		List<HashMap<String, Object>> equipmentList = getFacilityInformation(childBOMLineVector);
		
    	String madeFacilityId = "";
    	String madeFacilityName = "";
    	String madeFacilitySpec = "";
    	String madeFacilityQuantity = "";
		
		for (int i = 0; equipmentList!=null && i < equipmentList.size(); i++) {
			
			HashMap<String, Object> equipmentMap = equipmentList.get(i);
			
	    	String facilityId =(String) equipmentMap.get("bl_item_item_id");
	    	if(facilityId==null || (facilityId!=null && facilityId.trim().length()<1)){
	    		facilityId = "";
	    	}
	    	String facilityName =(String) equipmentMap.get("bl_item_object_name");
	    	if(facilityName==null || (facilityName!=null && facilityName.trim().length()<1)){
	    		facilityName = "";
	    	}
	    	String facilitySpec =(String)equipmentMap.get("m7_SPEC_ENG");
	    	if(facilitySpec==null || (facilitySpec!=null && facilitySpec.trim().length()<1)){
	    		facilitySpec = "-";
	    	}
	    	String facilityPurpose =(String)equipmentMap.get("m7_PURPOSE_ENG");
	    	if(facilityPurpose==null || (facilityPurpose!=null && facilityPurpose.trim().length()<1)){
	    		facilityPurpose = "-";
	    	}
	    	
	    	String specValueStr = null;
	    	
	    	if(facilitySpec!=null && facilitySpec.trim().equalsIgnoreCase("-") && facilityPurpose!=null && facilityPurpose.trim().equalsIgnoreCase("-") ){
	    		specValueStr = "";
	    	}else if(facilitySpec!=null && facilitySpec.trim().length()>0 && facilityPurpose!=null && facilityPurpose.trim().length()>0 ){
	    		specValueStr = facilitySpec.trim()+"/"+facilityPurpose.trim();
	    	}
	    	
	    	String quantity = (String)equipmentMap.get("bl_quantity");
	    	if(quantity==null || (quantity!=null && quantity.length()<1)){
	    		quantity = "";
	    	}
	    	
	    	if(madeFacilityId==null || (madeFacilityId!=null && madeFacilityId.length()<1) ){
	    		madeFacilityId = facilityId;
	    	}else{
	    		madeFacilityId = madeFacilityId+"\n"+facilityId;
	    	}

	    	if(madeFacilityName==null || (madeFacilityName!=null && madeFacilityName.length()<1) ){
	    		madeFacilityName = facilityName;
	    	}else{
	    		madeFacilityName = madeFacilityName+"\n"+facilityName;
	    	}

	    	if(madeFacilitySpec==null || (madeFacilitySpec!=null && madeFacilitySpec.length()<1) ){
	    		madeFacilitySpec = specValueStr;
	    	}else{
	    		madeFacilitySpec = madeFacilitySpec+"\n"+specValueStr;
	    	}

	    	if(madeFacilityQuantity==null || (madeFacilityQuantity!=null && madeFacilityQuantity.length()<1) ){
	    		madeFacilityQuantity = quantity;
	    	}else{
	    		madeFacilityQuantity = madeFacilityQuantity+"\n"+quantity;
	    	}
		}
		
//    	columnDataHash.put("Col31", madeFacilityId);
//    	columnDataHash.put("Col32", madeFacilityName);
//    	columnDataHash.put("Col33", madeFacilitySpec);
//    	columnDataHash.put("Col34", madeFacilityQuantity);
    	
		// 공법 마스터 리스트에 KPC 내용 컬럼 추가
    	columnDataHash.put("Col32", madeFacilityId);
    	columnDataHash.put("Col33", madeFacilityName);
    	columnDataHash.put("Col34", madeFacilitySpec);
    	columnDataHash.put("Col35", madeFacilityQuantity);
		

		
		this.indexNo++;
		
		columnDataHash.put("Col00", (""+ this.indexNo));				// Row Index
		
		
		
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
		// 공법 마스터 리스트에 KPC 내용 컬럼 추가
		rowDataString = rowDataString + "\t"+columnDataHash.get("Col38");
		
		/* 수정점 - 20200106
		 *  P-FMEA No., CP No. (m7_P_FMEA_NO, m7_P_FMEA_NO)컬럼 추가로 인해 
		 *   columnDataHash  변수에 입력
		 */
		rowDataString = rowDataString + "\t"+columnDataHash.get("Col39");
		rowDataString = rowDataString + "\t"+columnDataHash.get("Col40");
		
		/**
		 * 특별 특성 속성 추가로 인한 배열 개수 변경 37 -> 38
		 * 공법 마스터 리스트에 KPC 내용 컬럼 추가 38 -> 39
		 * P-FMEA No., CP No. (m7_P_FMEA_NO, m7_P_FMEA_NO)컬럼 추가 39 -> 41
		 */
		Object[] rowData = new Object[41];
		
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
		
		excelWorkBookWriter.writeRow("Sheet1", indexNo, rowData, this.selectedValue, operationBOMLine, basicSoaUtil, this.logFileUtility);
		
		
	}
	
	private void initOperationInformation(BOMLine operationBOMLine, Vector<MEActivity> activityVector){
		
//		columnDataHash.put("Col19", (Object) this.currentLineCode);
//		columnDataHash.put("Col20", (Object) currentLineItemRevId);
//		
//		columnDataHash.put("Col21", (Object) currentStationCode);
//		columnDataHash.put("Col22", (Object) currentStationItemRevId);
		
		// 공법 마스터 리스트에 KPC 내용 컬럼 추가
		columnDataHash.put("Col20", (Object) this.currentLineCode);
		columnDataHash.put("Col21", (Object) currentLineItemRevId);
		
		columnDataHash.put("Col22", (Object) currentStationCode);
		columnDataHash.put("Col23", (Object) currentStationItemRevId);
		
		String operationItemId = null;
		String operationItemRevId = null;
		String operationRevName = null;
		String operationItemType = null;
		Item operationItem = null;
		ItemRevision operationItemRevision  = null;
		String operationVlCondition = null;
		String operationMVlCondition = null;
		String operationReleasStatusDate = null;

		String specialCharicter = null;
		
		// "bl_item_object_name", "bl_variant_condition",
		String[] operationBOMLineProperteis = new String[]{"bl_item",  "bl_item_item_id", "bl_item_object_type", 
				"bl_revision", "bl_rev_item_revision_id", "bl_rev_object_name", 
				"bl_all_child_lines", "bl_occ_mvl_condition"
				};
		
		try {
			operationBOMLine = (BOMLine)basicSoaUtil.readProperties(operationBOMLine, operationBOMLineProperteis);
			
			operationItemId = operationBOMLine.get_bl_item_item_id();
			operationItemRevId = operationBOMLine.get_bl_rev_item_revision_id();
			operationItemType = operationBOMLine.get_bl_item_object_type();
			operationRevName = operationBOMLine.get_bl_rev_object_name();
			//operationVlCondition = operationBOMLine.get_bl_variant_condition();
			operationMVlCondition = operationBOMLine.get_bl_occ_mvl_condition();
			operationItem = (Item)operationBOMLine.get_bl_item();
			operationItemRevision  = (ItemRevision)operationBOMLine.get_bl_revision();
			if(operationItemRevision!=null){
				operationItemRevision = (ItemRevision)basicSoaUtil.readProperties(operationItemRevision, 
				///////////////////////////////////////////////////////////////////////////////////		
						/**
						 *  Operation Master List Report 특별 특성 속성 추가
						 */
						new String[]{"m7_DR", "m7_SPECIAL_CHARICTER"});
				
				specialCharicter = operationItemRevision.getPropertyDisplayableValue("m7_SPECIAL_CHARICTER");
				columnDataHash.put("Col08", specialCharicter == null ? "" : specialCharicter );
				///////////////////////////////////////////////////////////////////////////////////
			}

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
		
		columnDataHash.put("Col01", (Object) operationItemId);
		columnDataHash.put("Col02", (Object) operationItemRevId);
		columnDataHash.put("Col03", (Object) operationRevName);
		columnDataHash.put("Col05", (Object) operationOptionCode);
		columnDataHash.put("Col06", (Object) operationOptionDesc);
		
		String operationMECONo = null;
		String operationEffectiveDate = null;
		String operationEngName = null;
		String assemblySystem = null;
		String owningUserName = null;
		String installDrawingNo = null;
		try {
			operationItemRevision = (ItemRevision)basicSoaUtil.readProperties(operationItemRevision, new String[]{"m7_ENG_NAME", "m7_ASSY_SYSTEM", "owning_user", "m7_INST_DWG_NO"});
		} catch (Exception e) {
			this.logFileUtility.writeExceptionTrace(e);
		}
		User owningUser = null;
		try {
			operationEngName = operationItemRevision.getPropertyDisplayableValue("m7_ENG_NAME");
			if (processType!=null && processType.startsWith("A")) {
				assemblySystem = operationItemRevision.getPropertyDisplayableValue("m7_ASSY_SYSTEM");
			}
			installDrawingNo = operationItemRevision.getPropertyDisplayableValue("m7_INST_DWG_NO");
			owningUser = (User)operationItemRevision.get_owning_user();
		} catch (NotLoadedException e) {
			this.logFileUtility.writeExceptionTrace(e);
		}
		
		if(owningUser!=null){
			try {
				owningUser = (User)basicSoaUtil.readProperties(owningUser, new String[]{"user_name"});
				owningUserName = owningUser.get_user_name();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		columnDataHash.put("Col04", (Object) operationEngName);
//		columnDataHash.put("Col35", (Object) assemblySystem);
//		columnDataHash.put("Col36", (Object) owningUserName);
//		columnDataHash.put("Col37", (Object) installDrawingNo);
		// 공법 마스터 리스트에 KPC 내용 컬럼 추가
		columnDataHash.put("Col36", (Object) assemblySystem);
		columnDataHash.put("Col37", (Object) owningUserName);
		columnDataHash.put("Col38", (Object) installDrawingNo);
		
		
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
			columnDataHash.put("Col20", (Object) stationNo);
			columnDataHash.put("Col21", (Object) staionRevId);
			
		}else if(operationItemType!=null && operationItemType.trim().equalsIgnoreCase("M7_BOPPaintOp")==true){
			
		}else if(operationItemType!=null && operationItemType.trim().equalsIgnoreCase("M7_BOPBodyOp")==true){
			
		}
		
		
		
		String kpcValue = null;
		if (processType!=null && processType.startsWith("A")) {
			kpcValue = getKPCValue(activityVector);
			
			if(kpcValue!=null && kpcValue.trim().length()>0){
				columnDataHash.put("Col09", "Y");
				// 공법 마스터 리스트에 KPC 내용 컬럼 추가
				columnDataHash.put("Col10", kpcValue);
			}
		}else{
			boolean isKpcValue = false;
			try {
				operationItemRevision = (ItemRevision)basicSoaUtil.readProperties(operationItemRevision, new String[]{"m7_KPC"});
				Property  tempProperty  = operationItemRevision.getPropertyObject("m7_KPC");
				if(tempProperty!=null){
					isKpcValue = tempProperty.getBoolValue();
				}
			} catch (Exception e) {
				this.logFileUtility.writeExceptionTrace(e);
			}
			if(isKpcValue==true){
				columnDataHash.put("Col09", "Y");
			}
		}
		
		String workPerson = "";
		String workProcessSequence = "";
		String workArea = "";
		
		/* 수정점 - 20200106
		 *  P-FMEA No., CP No. (m7_P_FMEA_NO, m7_P_FMEA_NO)컬럼 추가로 인해 
		 *   columnDataHash  변수에 입력
		 */
		String pFMEANo = "";
		String cPno = "";
		
		if(operationItem!=null){
			try {
				/* 수정점 - 20200106
				 *  P-FMEA No., CP No. (m7_P_FMEA_NO, m7_P_FMEA_NO)컬럼 추가로 인해 
				 *  속성값 추출  위 속성은 Item 의 속성임   
				 */
				operationItem = (Item)basicSoaUtil.readProperties(operationItem, 
						new String[]{"m7_WORKER_CODE", "m7_PROCESS_SEQ", "m7_WORKAREA"});
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		try {
			if( processType.startsWith("A")) {
				workPerson = operationItem.getPropertyDisplayableValue("m7_WORKER_CODE");
				workProcessSequence = operationItem.getPropertyDisplayableValue("m7_PROCESS_SEQ");
				workArea = operationItem.getPropertyDisplayableValue("m7_WORKAREA");
				/* 수정점 - 20200106
				 *  P-FMEA No., CP No. (m7_P_FMEA_NO, m7_P_FMEA_NO)컬럼 추가로 인해 
				 *  속성값 추출  위 속성은 Item 의 속성임   
				 */
				try {
					basicSoaUtil.readProperties(operationItem, new String[]{"m7_P_FMEA_NO", "m7_CP_NO"});
					
				} catch(Exception e) {
					e.printStackTrace();
				}
				pFMEANo = operationItem.getPropertyDisplayableValue("m7_P_FMEA_NO");
				cPno = operationItem.getPropertyDisplayableValue("m7_CP_NO");
				
			} 
		} catch (NotLoadedException e) {
			e.printStackTrace();
		}

//		columnDataHash.put("Col23", workPerson);
//		columnDataHash.put("Col24", workProcessSequence);
//		columnDataHash.put("Col25", workArea);
		
		// 공법 마스터 리스트에 KPC 내용 컬럼 추가
		columnDataHash.put("Col24", workPerson);
		columnDataHash.put("Col25", workProcessSequence);
		columnDataHash.put("Col26", workArea);
		
		/* 수정점 - 20200106
		 *  P-FMEA No., CP No. (m7_P_FMEA_NO, m7_CP_NO)컬럼 추가로 인해 
		 *  두 변수의 값이  각각 "Y" 값을 가질 경우 
		 *  PF(디폴트 값)-C300(차종)-A1(공장명)-F480("F" & 관리번호 FUNC No.)-XX(디폴트 값)
		 *  CP(디폴트 값)-C300(차종)-A1(공장명)-F480("F" & 관리번호 FUNC No.)-XX(디폴트 값)  
		 *  으로 값 변형
		 *  
		 */
		try {
			if( pFMEANo.equals("Y") || cPno.equals("Y")) {
				String projectCode = "";
				String functionCode = "";
				String shopCode = "";
				processTopBOMLine = (BOMLine)this.mppTopLines.processLine;
				processTopBOMLine = (BOMLine) basicSoaUtil.readProperties(processTopBOMLine, new String[]{"bl_revision"});
				ItemRevision processTopRevision = (ItemRevision)processTopBOMLine.get_bl_revision();
				basicSoaUtil.readProperties(processTopRevision, new String[]{"m7_SHOP"});
				shopCode =  processTopRevision.getPropertyDisplayableValue("m7_SHOP");
				basicSoaUtil.readProperties(operationItemRevision, new String[]{"m7_MECO_NO", "m7_FUNCTION_CODE"});
				functionCode = operationItemRevision.getPropertyDisplayableValue("m7_FUNCTION_CODE");
				Property  tempProperty  = (Property)operationItemRevision.getPropertyObject("m7_MECO_NO");
				if(tempProperty!=null){
					ItemRevision changeRevision = (ItemRevision) tempProperty.getModelObjectValue();
					if(changeRevision!=null){
						changeRevision = (ItemRevision)basicSoaUtil.readProperties(changeRevision, new String[]{"m7_PROJECT"});
						projectCode = changeRevision.getPropertyDisplayableValue("m7_PROJECT");
						// 프로젝트 코드 Default 값으로 변경
						projectCode = reNameProjectCode(projectCode);
					}
				}
				
				  if(((String)pFMEANo).equals("Y")) {
					  columnDataHash.put("Col39", (Object) "PF" + "-" + projectCode + "-" + shopCode + "-" + "F" + functionCode + "-" + "XX");
				  } else {
					  columnDataHash.put("Col39", (Object)"");
				  } 
				  
				  if(((String)cPno).equals("Y")) {
					  columnDataHash.put("Col40", (Object) "CP" + "-" + projectCode + "-" + shopCode + "-" + "F" + functionCode + "-" + "XX");
				  } else {
					  columnDataHash.put("Col40", (Object)"");
				  }
			} else {
				columnDataHash.put("Col39", (Object)"");
				columnDataHash.put("Col40", (Object)"");
			}
			
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private String getDRValue(Vector<BOMLine> childBOMLineVector, ItemRevision operationRevision){
		String drValueStr = null;
		
		if(childBOMLineVector==null || (childBOMLineVector!=null && childBOMLineVector.size()<1)){
			return drValueStr;
		}
		
		
		for (int i = 0; childBOMLineVector!=null && i < childBOMLineVector.size(); i++) {
			
			
			BOMLine childBOMLine = childBOMLineVector.get(i);
			
			try {
				childBOMLine = (BOMLine)basicSoaUtil.readProperties(childBOMLine, 
						new String[]{"bl_item_object_type", "bl_revision"});
			} catch (Exception e) {
				this.logFileUtility.writeExceptionTrace(e);
			}
			
			// S7_Vehpart, S7_Stdpart Type이 아니면 검토 대상이 아님.
			String itemType = null;
			try {
				itemType = childBOMLine.get_bl_item_object_type();
			} catch (NotLoadedException e) {
				this.logFileUtility.writeExceptionTrace(e);
			}
			
			// part가 아니면 검토 대상이 아님.
			if (itemType==null || 
					( itemType!=null && ( itemType.trim().equalsIgnoreCase("S7_Vehpart") 
							//|| itemType.trim().equalsIgnoreCase("S7_Stdpart") 
							)==false )
			){
				continue;
			}
			
			ItemRevision childItemRevision = null;
			try {
				childItemRevision = (ItemRevision)childBOMLine.get_bl_revision();
			} catch (NotLoadedException e) {
				this.logFileUtility.writeExceptionTrace(e);
			}
			
			try {
				childItemRevision = (ItemRevision)basicSoaUtil.readProperties(
						childItemRevision, 
						new String[]{"s7_REGULATION"});
			} catch (Exception e) {
				this.logFileUtility.writeExceptionTrace(e);
			}
			
			String dr = null;
			try {
				dr = childItemRevision.getPropertyDisplayableValue("s7_REGULATION");
			} catch (NotLoadedException e) {
				this.logFileUtility.writeExceptionTrace(e);
			}

            if (!dr.equals(".") && !dr.equals("")) {
                if (drValueStr==null || (drValueStr!=null && drValueStr.trim().length()<1)) {
                	drValueStr = dr;
                } else {
                    // End Item DR 속성 우선( DR1 > DR2 > DR3 )
                    if (dr.compareToIgnoreCase((String) drValueStr) < 0) {
                    	drValueStr = dr;
                    }
                }
            }
		}

		// End Item DR 속성이 없으면 공법 DR 속성
        if (drValueStr==null || (drValueStr!=null && drValueStr.trim().length()<1)) {
			try {
				operationRevision = (ItemRevision)basicSoaUtil.readProperties(
						operationRevision, 
						new String[]{"m7_DR"});
				drValueStr = operationRevision.getPropertyDisplayableValue("m7_DR");
			} catch (NotLoadedException e) {
				this.logFileUtility.writeExceptionTrace(e);
			} catch (Exception e) {
				this.logFileUtility.writeExceptionTrace(e);
			}
        }
		
		return drValueStr;
	}
	
	private Vector<MEActivity> getMEActivitys(BOMLine operationBOMLine){
		
		Mfg0BvrOperation mfg0BvrOperation = null;
		if(operationBOMLine!=null && operationBOMLine instanceof Mfg0BvrOperation){
			mfg0BvrOperation = (Mfg0BvrOperation)operationBOMLine;	
		}
	
		// Root Activity 를 찾아온다.
		ModelObject activityLineModelObject = null;
		try {
			mfg0BvrOperation = (Mfg0BvrOperation)basicSoaUtil.readProperties(
					mfg0BvrOperation, 
					new String[]{"bl_me_activity_lines"});
			activityLineModelObject = mfg0BvrOperation.get_bl_me_activity_lines();
		} catch (Exception e) {
			this.logFileUtility.writeExceptionTrace(e);
		}
		
		MEActivity rootMEActivity = null;
		if(activityLineModelObject!=null && activityLineModelObject instanceof CfgActivityLine){
			CfgActivityLine root = (CfgActivityLine)activityLineModelObject;
			try {
				root = (CfgActivityLine)basicSoaUtil.readProperties( root, new String[]{ "al_object" });
				//"me_cl_source", "me_cl_wso" Properties도 동일한 MEActivity Object를 Return 한다.
				rootMEActivity = (MEActivity)root.get_al_object();
			} catch (Exception e) {
				this.logFileUtility.writeExceptionTrace(e);
			}
		}
		
		Vector<MEActivity> activityVector = new Vector<MEActivity>();
		if(rootMEActivity!=null){
			WorkspaceObject[] contentsObjects = null;
			try {
				rootMEActivity = (MEActivity)basicSoaUtil.readProperties(rootMEActivity, new String[]{ "contents" });
				contentsObjects = rootMEActivity.get_contents();
			} catch (Exception e) {
				this.logFileUtility.writeExceptionTrace(e);
			}
			
			for (int i = 0; contentsObjects!=null && i < contentsObjects.length; i++) {
				if(contentsObjects[i]!=null && contentsObjects[i] instanceof MEActivity){
					
					MEActivity tempActivity = (MEActivity)contentsObjects[i];
					// Collections Sort에 필요한 Attribute를 미리 Load 한다.
					try {
						tempActivity = (MEActivity)basicSoaUtil.readProperties( tempActivity, new String[]{ "seq_no", "time_system_frequency" });
					} catch (Exception e) {
						this.logFileUtility.writeExceptionTrace(e);
					}
					if(activityVector.contains(tempActivity)==false ){
						activityVector.add(tempActivity);
					}
				}
			}
		}
		
		return activityVector;
	}

	private String getKPCValue(Vector<MEActivity> activityVector){
		String kpcValue = null;
		
		for (int i = 0; activityVector!=null && i < activityVector.size(); i++) {
			MEActivity tempActivity = activityVector.get(i);
			String activityControlPoint = null;
			// 공법 마스터 리스트에 KPC 내용 컬럼 추가
			String activityControlBasis = null;
			String seqNo = null;
			double tsFrequency = 0;
			try {
				// 공법 마스터 리스트에 KPC 내용 컬럼 추가
				tempActivity = (MEActivity)basicSoaUtil.readProperties( tempActivity, new String[]{ "m7_CONTROL_POINT", "m7_CONTROL_BASIS" });
				activityControlPoint = tempActivity.getPropertyDisplayableValue("m7_CONTROL_POINT");
				activityControlBasis = tempActivity.getPropertyDisplayableValue("m7_CONTROL_BASIS");
				seqNo = tempActivity.get_seq_no();
				tsFrequency = tempActivity.get_time_system_frequency();
			} catch (Exception e) {
				this.logFileUtility.writeExceptionTrace(e);
			}
		 //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
			 /*
             * "KPC내용" 출력 내용 변경
             *  AL_CONTROL_POINT 값이 있고 "TR추적" 이 아니면 출력
             */
//          if (activityControlPoint!=null && !activityControlPoint.equals("")  && activityControlBasis != null && !activityControlBasis.equals("")) {
//        	  // 공법 마스터 리스트에 KPC 내용 컬럼 추가
////        	  kpcValue = activityControlPoint;
//        	  kpcValue = activityControlPoint + ":" + activityControlBasis; 
//        	  break;
//          }
          if (activityControlPoint!=null && !activityControlPoint.equals("")  && !activityControlPoint.equals("TR추적") ) {
        	  // 공법 마스터 리스트에 KPC 내용 컬럼 추가
//        	  kpcValue = activityControlPoint;
        	  if(activityControlPoint.length() > 2) {
        		  activityControlPoint = activityControlPoint.substring(2);
        	  }
        		  
        	  kpcValue = activityControlPoint + ":" + activityControlBasis; 
        	  break;
          }
        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		}
        
		return kpcValue;
	}
	
	private Hashtable<String, String> getTimeInfo(BOMLine operationBOMLine, Vector<MEActivity> activityVector){
		
	    Double autoMaticTime = 0.0;		// 자동시간
	    Double workerNetTime = 0.0;		// 작업자정미시간
	    Double assistantTime = 0.0;			// 보조시간
	    Double standardTime = 0.0;			// 표준시간
	    Double workingTime = 0.0;			// 작업시간
	    Double cycleTime = 0.0;				// 정미합
	    Double allowance = 0.0;				// 부대계수
	    String maXWorkingTime = "";			// 편성시간(최대)
	    String representWorkingTime = "";		// 편성시간 (대표차종)
		
		if(this.processTopBOMLine!=null){
			try {
				processTopBOMLine = (BOMLine)basicSoaUtil.readProperties( processTopBOMLine, new String[]{ "m7_ALLOWANCE" });
				String shop_allowance = processTopBOMLine.getPropertyDisplayableValue("m7_ALLOWANCE");
	            if (!shop_allowance.trim().equals("")) {
	                allowance = Double.valueOf(shop_allowance);
	            }
			} catch (Exception e) {
				this.logFileUtility.writeExceptionTrace(e);
			}
		}
		
		if(this.currentLineBOMLine!=null){
			try {
				currentLineBOMLine = (BOMLine)basicSoaUtil.readProperties( currentLineBOMLine, new String[]{ "m7_ALLOWANCE" });
				String line_allowance = currentLineBOMLine.getPropertyDisplayableValue("m7_ALLOWANCE");
	            if (!line_allowance.trim().equals("")) {
	                allowance = Double.valueOf(line_allowance);
	            }
			} catch (Exception e) {
				this.logFileUtility.writeExceptionTrace(e);
			}
		}
	
		// 순서 정의 하는 부분...
		// 실제 순서가 정돈 되도록 추가 구현 해야 한다.
		Collections.sort(activityVector, new MEActivitySeqNoAscCompare());
		
		for (int i = 0; activityVector!=null && i < activityVector.size(); i++) {
			MEActivity tempMEActivity = activityVector.get(i);

			String activity_system_category = null;
			double unitTime = 0.0;
			double frequencyTime = 0.0;
			try {
				tempMEActivity = (MEActivity)basicSoaUtil.readProperties( tempMEActivity, new String[]{ "time_system_category", "time_system_unit_time", "time_system_frequency" });
				activity_system_category = tempMEActivity.get_time_system_category();
				unitTime = tempMEActivity.get_time_system_unit_time();
				frequencyTime = tempMEActivity.get_time_system_frequency();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			if (activity_system_category.equals("01")) {
				autoMaticTime += (unitTime * frequencyTime);
			}else if (activity_system_category.equals("02")) {
				workerNetTime += (unitTime * frequencyTime);
			}else if (activity_system_category.equals("03")) {
				assistantTime += (unitTime * frequencyTime);
			}
		}
	
		// 표준시간(작업자 정미 시간 * 부대 계수)
	    standardTime = autoMaticTime * allowance;
	
	    // 작업 시간(표준 시간 + 보조 시간)
	    workingTime = assistantTime + standardTime;
	
	    // cycle time(자동 시간 + 작업자 정미 시간)
	    cycleTime = autoMaticTime + workerNetTime;

	    boolean maxWorkTimeCheck = false;
	    boolean refVhicleCheck = false;
	    
	    Item operationItem = null;
		try {
			operationItem = (Item)operationBOMLine.get_bl_item();
		} catch (NotLoadedException e) {
			e.printStackTrace();
		}
	    if(operationItem!=null){

	    	try {
	    		
	    		if (processType.startsWith("A")) {
	    			operationItem = (Item)basicSoaUtil.readProperties( operationItem, new String[]{ 
	    					"m7_MAX_WORK_TIME_CHECK", "m7_REP_VHICLE_CHECK"});
	    			Property maxWorkTimeProperty = operationItem.getPropertyObject("m7_MAX_WORK_TIME_CHECK");
	    			if(maxWorkTimeProperty!=null){
	    				maxWorkTimeCheck = maxWorkTimeProperty.getBoolValue();
	    			}
	    			Property refVhicleCheckProperty = operationItem.getPropertyObject("m7_REP_VHICLE_CHECK");
	    			if(refVhicleCheckProperty!=null){
	    				refVhicleCheck = refVhicleCheckProperty.getBoolValue();
	    			}
	    		}
			} catch (Exception e) {
				e.printStackTrace();
			}
	    	
	    }
	    
        // 편성 시간(최대)
        if (maxWorkTimeCheck==true) {
        	maXWorkingTime = ((Double) (Math.ceil(workingTime * 10.0) / 10.0)).toString();
        } else {
        	maXWorkingTime = "";
        }

        // 편성 시간(대표차종)
        if (refVhicleCheck==true) {
        	representWorkingTime = ((Double) (Math.ceil(workingTime * 10.0) / 10.0)).toString();
        } else {
        	representWorkingTime = "";
        }

        DecimalFormat formatter = new DecimalFormat("0.00");
        Hashtable<String, String> returnValuHash = new Hashtable<String, String>();
        returnValuHash.put("autoMaticTime", formatter.format( Math.ceil(autoMaticTime * 10.0) / 10.0) );
        returnValuHash.put("workerNetTime", formatter.format( Math.ceil(workerNetTime * 10.0) / 10.0) );
        returnValuHash.put("assistantTime", formatter.format( Math.ceil(assistantTime * 10.0) / 10.0) );
        returnValuHash.put("standardTime", formatter.format( Math.ceil(standardTime * 10.0) / 10.0) );
        returnValuHash.put("workingTime", formatter.format( Math.ceil(workingTime * 10.0) / 10.0) );
        returnValuHash.put("cycleTime", formatter.format( Math.ceil(cycleTime * 10.0) / 10.0) );
        returnValuHash.put("allowance", formatter.format( allowance) );
        returnValuHash.put("maXWorkingTime", maXWorkingTime );
        returnValuHash.put("representWorkingTime", representWorkingTime );
        
	    return returnValuHash;
	}

	private List<HashMap<String, Object>> getToolInformation(Vector<BOMLine> childBOMLineVector){
 
		List<HashMap<String, Object>> toolList = null;
		
		Vector<BOMLine> toolLineVector = new Vector<BOMLine>();
		
		for (int i = 0; childBOMLineVector!=null &&  i < childBOMLineVector.size(); i++) {
			// 
			BOMLine tempBOMLine = childBOMLineVector.get(i);
			
	    	try {
	    		tempBOMLine = (BOMLine)basicSoaUtil.readProperties( tempBOMLine, new String[]{ 
						"bl_item_object_type"});
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			String itemType = null;
	    	try {
	    		itemType = tempBOMLine.get_bl_item_object_type();
			} catch (Exception e) {
				e.printStackTrace();
			}
	    	
	    	if(itemType==null || ( itemType!=null && itemType.trim().equalsIgnoreCase("M7_Tool")==false)){
	    		continue;
	    	}
	    	
	    	toolLineVector.add(tempBOMLine);
		}
		
		if(toolLineVector!=null && toolLineVector.size()>0){
			toolList = new ArrayList<HashMap<String, Object>>();
		}
		
		for (int i = 0; toolLineVector!=null && i < toolLineVector.size(); i++) {
			BOMLine toolBOMLine = toolLineVector.get(i);
			
			try {
				toolBOMLine = (BOMLine)basicSoaUtil.readProperties( toolBOMLine, 
						new String[]{"bl_item_item_id", "bl_quantity", "bl_revision", "M7_TORQUE", "M7_TORQUE_VALUE"});
			} catch (Exception e) {
				e.printStackTrace();
			}
			String toolItemId = null;
			String quantity = null;
			ItemRevision toolRevision = null;
			try {
				toolItemId = toolBOMLine.get_bl_item_item_id();
				quantity = toolBOMLine.get_bl_quantity();
	    		if(quantity!=null && quantity.trim().length()>0){
	    			quantity = quantity.split("\\.")[0];
	    		}
	    		toolRevision = (ItemRevision)toolBOMLine.get_bl_revision();
			} catch (NotLoadedException e) {
				e.printStackTrace();
			}

			String torque = null;
			try {
				Property torqueNoteTypeProperty = (Property)toolBOMLine.getPropertyObject("M7_TORQUE");
				torque = torqueNoteTypeProperty.getDisplayableValue();
			} catch (NotLoadedException e1) {
				e1.printStackTrace();
			}

			String torqueValue = null;
			try {
				Property torqueValueNoteTypeProperty = (Property)toolBOMLine.getPropertyObject("M7_TORQUE_VALUE");
				torqueValue = torqueValueNoteTypeProperty.getDisplayableValue();
			} catch (NotLoadedException e1) {
				e1.printStackTrace();
			}
			
			String engName = null;
			String specValue = null;
			if(toolRevision!=null){
				try {
					toolRevision = (ItemRevision)basicSoaUtil.readProperties( toolRevision, 
							new String[]{"m7_ENG_NAME", "m7_SPEC_ENG"});
					engName = toolRevision.getPropertyDisplayableValue("m7_ENG_NAME");
					specValue = toolRevision.getPropertyDisplayableValue("m7_SPEC_ENG");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			HashMap<String, Object> toolMap = new HashMap<String, Object>();
			toolMap.put("bl_item_item_id", toolItemId);
			toolMap.put("bl_quantity", quantity);
			toolMap.put("m7_ENG_NAME", engName);
			toolMap.put("m7_SPEC_ENG", specValue);
			toolMap.put("M7_TORQUE", torque);
			toolMap.put("M7_TORQUE_VALUE", torqueValue);
			toolList.add(toolMap);
		}
		
		return toolList;
	}

	private List<HashMap<String, Object>> getFacilityInformation(Vector<BOMLine> childBOMLineVector){
		
		Vector<String> targetTypeNameVector = new Vector<String>();
		targetTypeNameVector.add("M7_GeneralEquip");
		targetTypeNameVector.add("M7_JigFixture");
		targetTypeNameVector.add("M7_Robot");
		targetTypeNameVector.add("M7_Gun");
		
		List<HashMap<String, Object>> equipmentList = null;
		
		Vector<BOMLine> targetChildBOMLineVector = new Vector<BOMLine>();
		for (int i = 0; childBOMLineVector!=null && i < childBOMLineVector.size(); i++) {
			BOMLine operationChildNodeBOMLine = childBOMLineVector.get(i);
			
			try {
				operationChildNodeBOMLine = (BOMLine)basicSoaUtil.readProperties( operationChildNodeBOMLine, 
						new String[]{"bl_item_object_type"});
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			String itemType = null;
	    	try {
	    		itemType = operationChildNodeBOMLine.get_bl_item_object_type();
			} catch (Exception e) {
				e.printStackTrace();
			}
	    	
	    	System.out.println("itemType = "+itemType);
	    	
	    	if(itemType!=null && targetTypeNameVector.contains(itemType.trim())==true){
	    		targetChildBOMLineVector.add(operationChildNodeBOMLine);
	    	}
		}

		if(targetChildBOMLineVector!=null && targetChildBOMLineVector.size()>0){
			
			System.out.println("targetChildBOMLineVector(1) = "+targetChildBOMLineVector);
			
			equipmentList = new ArrayList<HashMap<String, Object>>();
		}else{
			
			System.out.println("targetChildBOMLineVector(2) = "+targetChildBOMLineVector);
			
			return equipmentList;
		}
		
		for (int i = 0; targetChildBOMLineVector!=null && i < targetChildBOMLineVector.size(); i++) {
			
			BOMLine operationChildNodeBOMLine = targetChildBOMLineVector.get(i);
			
			ItemRevision childNodeRevision = null;
			String childNodeItemId = null;		 			// 설비 ID
			String childNodeItemRevName = null;		 	// 설비명
			String blQuantity = null;							// 설비 수량 - 정수로 표시

	    	try {
	    		
	    		operationChildNodeBOMLine = (BOMLine)basicSoaUtil.readProperties( 
	    				operationChildNodeBOMLine, 
	    				new String[]{"bl_revision", "bl_item_item_id", "bl_rev_object_name", "bl_quantity"});

	    		childNodeRevision = (ItemRevision)operationChildNodeBOMLine.get_bl_revision();
	    		childNodeItemId = operationChildNodeBOMLine.get_bl_item_item_id();
	    		childNodeItemRevName = operationChildNodeBOMLine.get_bl_rev_object_name();
	    		blQuantity = operationChildNodeBOMLine.get_bl_quantity();
	    		if(blQuantity!=null && blQuantity.trim().length()>0){
	    			blQuantity = blQuantity.split("\\.")[0];
	    		}
			} catch (Exception e) {
				e.printStackTrace();
			}
	    	
	    	if(childNodeRevision==null){
	    		continue;
	    	}
	    	
	    	String specEng = null;
	    	String purposeEng = null;
	    	
	    	try {
	    		childNodeRevision = (ItemRevision)basicSoaUtil.readProperties( 
	    				childNodeRevision, 
	    				new String[]{ 
	    						"m7_SPEC_ENG", "m7_PURPOSE_ENG"});

	    		 // 설비 스펙/목적
	    		specEng = childNodeRevision.getPropertyDisplayableValue("m7_SPEC_ENG");
	    		purposeEng = childNodeRevision.getPropertyDisplayableValue("m7_PURPOSE_ENG");
	    		
			} catch (Exception e) {
				e.printStackTrace();
			}
	    	
	    	HashMap<String, Object> equipmentMap = new HashMap<String, Object>();
	    	equipmentMap.put("bl_item_item_id", childNodeItemId);
	    	equipmentMap.put("bl_item_object_name", childNodeItemRevName);
	    	equipmentMap.put("m7_SPEC_ENG", specEng);
	    	equipmentMap.put("m7_PURPOSE_ENG", purposeEng);
	    	equipmentMap.put("bl_quantity", blQuantity);
	    	
	    	equipmentList.add(equipmentMap);
		}
		
		return equipmentList;
	}

	 /**
	  * BOM Lie의 Attribute 중 Item Id를 읽어 비교하는 Comparator 구현 Class
	  */
	static class MEActivitySeqNoAscCompare implements Comparator<MEActivity> {

		
		/**
		 * 오름차순(ASC)
		 */
		@Override
		public int compare(MEActivity arg0, MEActivity arg1) {
			
			String arg0Str = null;
			try {
				arg0Str = arg0.get_seq_no();
			} catch (Exception e) {
			}
			String arg1Str = null;
			try {
				arg1Str = arg1.get_seq_no();
			} catch (Exception e) {
			}
			
			return arg0Str.compareTo(arg1Str);
		}

	}

	/**
	 * BOM Lie의 Attribute 중 Item Id를 읽어 비교하는 Comparator 구현 Class
	 *
	 */
	static class MEActivitySeqNoDescCompare implements Comparator<MEActivity> {

		/**
		 * 내림차순(DESC)
		 */
		@Override
		public int compare(MEActivity arg0, MEActivity arg1) {

			String arg0Str = null;
			try {
				arg0Str = arg0.get_seq_no();
			} catch (Exception e) {
			}
			String arg1Str = null;
			try {
				arg1Str = arg1.get_seq_no();
			} catch (Exception e) {
			}
			
			return arg1Str.compareTo(arg0Str);
		}

	}
	
	/**
	 * 수정점 : [CF-196]20200114
	 * ProjectCode Name 변경 로직 
	 * Ex) X100, X150, X151 -> X150
	 * 	   C300, C301 		-> C300  으로 변경 추가로 변경 항목 가능
	 * @param projectCode
	 * @return
	 */
	private String reNameProjectCode(String projectCode) {
		String projectCodeRename = "";
		if( projectCode.startsWith("X1")) {
			projectCodeRename = "X150";
		}
		
		if( projectCode.startsWith("C3")) {
			projectCodeRename = "C300";
		}
		
		return projectCodeRename;
	}
	
	////////////////////////////////////////////////////////////////////////////////////
	/*
	 * 수정점 : 20200330
	 * 수정내용 : MPP -> Assembly BOP -> Reports ->  Process Master List(On Server)
	 * 			  실행시 "작업정보" 추가로 인해   "selectedValue"  변수 추가로 인한 setSelectedValue 메서드 추가
	 */
	public void setSelectedValue(String selectedValue) {
		this.selectedValue = selectedValue;
	}
	
	public String getSelectedValue() {
		return this.selectedValue;
	}
	////////////////////////////////////////////////////////////////////////////////////

}
