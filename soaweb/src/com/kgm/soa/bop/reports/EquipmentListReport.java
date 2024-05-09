package com.kgm.soa.bop.reports;

import java.io.File;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import com.kgm.CommonConstants;
import com.kgm.common.remote.SendMailEAIUtil;
import com.kgm.soa.bop.reports.ToolListReport.BOMLineItemIdAscCompare;
import com.kgm.soa.bop.util.BasicSoaUtil;
import com.kgm.soa.bop.util.LogFileUtility;
import com.kgm.soa.bop.util.MPPTopLines;
import com.kgm.soa.bop.util.MppUtil;
import com.kgm.soa.service.TcLOVService;
import com.teamcenter.ets.soa.SoaHelper;
import com.teamcenter.soa.client.Connection;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.Property;
import com.teamcenter.soa.client.model.strong.AppearanceGroup;
import com.teamcenter.soa.client.model.strong.BOMLine;
import com.teamcenter.soa.client.model.strong.BOMWindow;
import com.teamcenter.soa.client.model.strong.Dataset;
import com.teamcenter.soa.client.model.strong.Item;
import com.teamcenter.soa.client.model.strong.ItemRevision;
import com.teamcenter.soa.client.model.strong.ListOfValues;
import com.teamcenter.soa.client.model.strong.MECollaborationContext;
import com.teamcenter.soa.client.model.strong.RevisionRule;
import com.teamcenter.soa.exceptions.NotLoadedException;

/**
 * 부자재 목록 Report
 * Structure를 Operatoin Level 까지 전개 한다.
 * Operation의 Child Node로 부자재들이 있다.
 * @author tj
 *
 */
public class EquipmentListReport {
	
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
	
	String currentShopCode = null;
	BOMLine currentLineBOMLine = null;
	String currentLineItemId = null;
	String currentLineCode = null;
	String currentLineItemRevId = null;
	
	BOMLine currentStationBOMLine = null;
	String currentStationItemId = null;
	String currentStationCode = null;
	String currentStationItemRevId = null;
	
	String operationType = null;
	
	int indexNo = 0;
	int limitIndexCount = 0;
	int templateDataStartRowIndex = 4;
	
	String reportDestinationFolderPath = CommonConstants.REPORT_FILE_PATH;
	File reportTemplateFile = null;
	String reportFilePath = null;
	
	ExcelWorkBookWriter excelWorkBookWriter;
	
	boolean isSuccess = false;
	
	boolean isAllList = false;
	
	public EquipmentListReport(Connection connection, String ccName){
		
		this.connection = connection;
		this.targetCCName = ccName;
		
		this.basicSoaUtil = new BasicSoaUtil(this.connection);
		String userId = this.basicSoaUtil.getLoginUserId();
		DateFormat fileNameDf = new SimpleDateFormat("yyyyMMdd_HHmmss");
		String timeStr = fileNameDf.format(new Date());
		this.logFileUtility = new LogFileUtility("EquipmentListReport"+ccName+"["+timeStr+"].txt");
		this.logFileUtility.setOutUseSystemOut(true);
		
		this.mppUtil = new MppUtil(connection);
		
		this.reportTargeItemType  = new Vector<String>();
		this.reportTargeItemType.add("M7_BOPBodyOp");
		this.reportTargeItemType.add("M7_BOPPaintOp");
		this.reportTargeItemType.add("M7_BOPAssyOp");
		this.reportTargeItemType.add("M7_BOPWeldOP");
		this.reportTargeItemType.add("M7_BOPStation");
		
	}
	
	public EquipmentListReport(Connection connection, String ccName, boolean isAllList){
		
		this.connection = connection;
		this.targetCCName = ccName;
		
		this.basicSoaUtil = new BasicSoaUtil(this.connection);
		String userId = this.basicSoaUtil.getLoginUserId();
		DateFormat fileNameDf = new SimpleDateFormat("yyyyMMdd_HHmmss");
		String timeStr = fileNameDf.format(new Date());
		this.logFileUtility = new LogFileUtility("EquipmentListReport"+ccName+"["+timeStr+"].txt");
		this.logFileUtility.setOutUseSystemOut(true);
		
		this.mppUtil = new MppUtil(connection);
		
		this.reportTargeItemType  = new Vector<String>();
		this.reportTargeItemType.add("M7_BOPBodyOp");
		this.reportTargeItemType.add("M7_BOPPaintOp");
		this.reportTargeItemType.add("M7_BOPAssyOp");
//		this.reportTargeItemType.add("M7_BOPWeldOP");
//		this.reportTargeItemType.add("M7_BOPStation");
		
		this.isAllList = isAllList;
		
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

    	unpack();
    	
    	initProductBasicInformation();
    	
    	String structureDataInitTime = logFileUtility.getElapsedTime();

		boolean isReadyOk = false;
		
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_ss");
		String newFileName = "EquipmentReport_"+this.productCode+"_"+this.plantCode+"["+simpleDateFormat.format(new Date())+"]";
		reportFilePath = reportDestinationFolderPath + File.separator + newFileName+".xlsx";

		this.reportTemplateFile = SDVBOPUtilities.getReportExcelTemplateFile(connection, reportDestinationFolderPath, "ME_DOCTEMP_04", reportFilePath);
		
		if(this.reportTemplateFile!=null && this.reportTemplateFile.exists()==true){
			
			//this.reportTemplateFile.renameTo(new File(reportFilePath));
			
			excelWorkBookWriter = new ExcelWorkBookWriter(reportFilePath);

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
		printTitleInformation();
		
		String replacedPartInitTime = logFileUtility.getElapsedTime();
    	
    	// BOP Process Structure를 전개해서 Report 생성을 시작 한다.
    	try {
//			basicSoaUtil.readProperties(mppTopLines.processLine, new String[]{"bl_all_child_lines"});
//			ModelObject[] chilBOMLineObjects = mppTopLines.processLine.get_bl_all_child_lines();
//			
//			
//			for (int i = 0; chilBOMLineObjects!=null && i < chilBOMLineObjects.length; i++) {
//				
//				if(limitIndexCount!=0 && this.indexNo>limitIndexCount){
//					break;
//				}
//				
//				if( chilBOMLineObjects[i]!=null && chilBOMLineObjects[i] instanceof BOMLine){
//					expandAllChildLine((BOMLine)chilBOMLineObjects[i]);
//				}
//			}
			expandAllChildLine(mppTopLines.processLine);
			
		} catch (Exception e) {
			this.logFileUtility.writeExceptionTrace(e);
		}
    	
    	// Excle 파일을 구성이 완료된 상태임.
    	// 여기서 Sub Total을 표현하기위한 처리를 추가한다.
    	int keyColumnIndex = 1;
    	int sortAreasStartRowIndex = this.templateDataStartRowIndex;
    	boolean isAscendingOrder = true;
    	excelWorkBookWriter.rowSortingForMakeSubTotalData("Sheet1", 1, sortAreasStartRowIndex, isAscendingOrder);

    	// Data의 Index번호를 다시 부여 한다.
    	excelWorkBookWriter.updateSortedDataIndex("Sheet1", 0, sortAreasStartRowIndex);
    	
    	try {
			excelWorkBookWriter.saveWorkBook();
		} catch (Exception e1) {
			e1.printStackTrace();
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
			excelWorkBookWriter.writeRow("Sheet1", 2, 19, reportDate);
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
					this.operationType = "M7_BOPBodyOp";
				} else if (processType!=null && processType.startsWith("P")) {
					this.operationType = "M7_BOPPaintOp";
				} else if (processType!=null && processType.startsWith("A")) {
					this.operationType = "M7_BOPAssyOp";
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
		
		String[] properteis = new String[]{ "bl_all_child_lines"};
		ModelObject[] childrenObject = null;
		try {
			bomLine = (BOMLine)basicSoaUtil.readProperties(bomLine, properteis);
			childrenObject = bomLine.get_bl_all_child_lines();
		} catch (Exception e) {
			this.logFileUtility.writeExceptionTrace(e);
		}
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		try {
				for( int i = 0 ; i < childrenObject.length; i ++ ) {
					
					basicSoaUtil.readProperties(childrenObject[i], new String[]{"bl_item_object_type", "bl_revision"});
					String childrenType = ((BOMLine)childrenObject[i]).get_bl_item_object_type();
					ItemRevision equipmentRevision = (ItemRevision)((BOMLine)childrenObject[i]).get_bl_revision();
					if(childrenObject[i] != null && childrenObject[i] instanceof AppearanceGroup){
						continue;
					}
					
					
					if(childrenType!=null && childrenType.trim().equalsIgnoreCase("M7_BOPLine")){
					
						  ItemRevision itemRevision = (ItemRevision)basicSoaUtil.readProperties(equipmentRevision, new String[]{"m7_SHOP","m7_LINE"});
							this.currentShopCode = itemRevision.getPropertyDisplayableValue("m7_SHOP");
							this.currentLineCode = itemRevision.getPropertyDisplayableValue("m7_LINE");
						
						// 조립의 경우 미할당 Line을 Report 출력에서 제외 한다.
						if(this.currentLineCode==null || (this.currentLineCode!=null && this.currentLineCode.trim().length()<1)){
							continue;
						}
					}
					
					if(childrenType.equals("M7_MfgProduct") || childrenType.equals("S7_Vehpart") || childrenType.equals("S7_Stdpart") || childrenType.equals("M7_BOPWeldOP")){
						continue;
					}
					
					if( (childrenType!=null && childrenType.trim().equalsIgnoreCase("M7_GeneralEquip")) || (childrenType!=null && childrenType.trim().equalsIgnoreCase("M7_JigFixture"))|| (childrenType!=null && childrenType.trim().equalsIgnoreCase("M7_Robot"))|| (childrenType!=null && childrenType.trim().equalsIgnoreCase("M7_Gun"))){
						
						HashMap<String, Object> columnDataHash = new HashMap<String, Object>();
						
						BOMLine shop = getParentBOPLine((BOMLine)childrenObject[i], "M7_BOPShop");
						
						if(shop != null) {
							String[] shopBOMLineProperties = new String[] {
																			  
																			 "m7_SHOP"
																		  };
							basicSoaUtil.readProperties(shop, new String[] {"bl_revision"});
							ItemRevision shopRevision = (ItemRevision)shop.get_bl_revision();
							basicSoaUtil.readProperties(shopRevision, shopBOMLineProperties);
							String shopCode = shopRevision.getPropertyDisplayableValue("m7_SHOP");
							columnDataHash.put("Col01", (Object) shopCode);  // SHOP Code
							
						}
						
						BOMLine line = getParentBOPLine((BOMLine)childrenObject[i], "M7_BOPLine");
						basicSoaUtil.readProperties(line, new String[] {"bl_revision", "bl_rev_item_revision_id"});
						ItemRevision lineRevision = (ItemRevision)line.get_bl_revision();
						if( line != null ) {
							
								BOMLine station = getParentBOPLine((BOMLine)childrenObject[i], "M7_BOPStation");
								String[] lineBOMLineProperties = new String [] {
																					 "m7_LINE"
																				   };
								basicSoaUtil.readProperties(lineRevision, lineBOMLineProperties);
								
								String lineCode = lineRevision.getPropertyDisplayableValue("m7_LINE");
								String lineRevisionId = line.get_bl_rev_item_revision_id();
								columnDataHash.put("Col02", (Object) lineCode);  // LineCode
								columnDataHash.put("Col03", (Object) lineRevisionId);  // Line Rev
							
						}
						
					  if (processType.startsWith("B") || processType.startsWith("P")) {
						  BOMLine station = getParentBOPLine((BOMLine)childrenObject[i], "M7_BOPStation");
						  if( station != null ) {
							  
							  basicSoaUtil.readProperties(station, new String[] {"bl_revision", "bl_rev_item_revision_id"});
							  ItemRevision stationItemRevision = (ItemRevision)station.get_bl_revision();
							  String[] stationBOMLineProperties = new String [] {
																				   "m7_LINE"
									  											 , "m7_STATION_CODE"
																			    };
							  basicSoaUtil.readProperties(stationItemRevision, stationBOMLineProperties);
							  String stationCode = stationItemRevision.getPropertyDisplayableValue("m7_LINE");
							  String stationCodeValue = stationItemRevision.getPropertyDisplayableValue("m7_STATION_CODE"); // stationCode + stationCodeValue
							  String stationRevisionId = station.get_bl_rev_item_revision_id();
							  columnDataHash.put("Col04", (Object) stationCode + "-" + stationCodeValue);   // 공정 
							  columnDataHash.put("Col05", (Object) stationRevisionId);
							  
						  }
					  }
						  
						  BOMLine operation = getParentBOPLine((BOMLine)childrenObject[i], operationType);
						  if( operation != null) {
							  String[] operationBOMLineProperteis = new String[]{ 
																				    "bl_item_item_id"
																				  , "bl_rev_item_revision_id"
																				  , "bl_rev_owning_user"
																				  , "bl_revision"
																				  };

							basicSoaUtil.readProperties(operation, operationBOMLineProperteis);
							ItemRevision operationRevision = (ItemRevision)operation.get_bl_revision();
							basicSoaUtil.readProperties(operationRevision, new String[] { "m7_STATION_NO" });
							String operationItemId = operation.get_bl_item_item_id();
							String operationItemRevId = operation.get_bl_rev_item_revision_id();
							String operationOwningUser = operation.get_bl_rev_owning_user();
							
							columnDataHash.put("Col06", (Object) operationItemId);
							columnDataHash.put("Col07", (Object) operationItemRevId);
							columnDataHash.put("Col21", (Object) operationOwningUser.substring(0, operationOwningUser.indexOf("(")));
							
							if( processType.startsWith("A")) {
								String operationStationCode =  operationRevision.getPropertyDisplayableValue("m7_STATION_NO");
								 columnDataHash.put("Col04", (Object) operationStationCode);
								 columnDataHash.put("Col05", (Object) "");
							}
						  } else {
							  	columnDataHash.put("Col06", (Object) "");
								columnDataHash.put("Col07", (Object) "");
								columnDataHash.put("Col21", (Object) "");
						  }
							//////////////////////Equipment 속성 추출/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
							
						  String[] equipmentBOMLineProperteis = new String[]{
																"bl_item_item_id"
															  , "bl_rev_item_revision_id"
															  //20201110 seho 아이템 이름 대신 리비전 이름을 사용.
															  , "bl_rev_object_name"
//															  , "bl_item_object_name"
															  , "bl_revision"
															  , "bl_quantity"
															  };
							
							String[] equipmentRevisionProperties = new String[] {
																  "m7_CAPACITY"
																	, "m7_ENG_NAME"
																, "m7_INSTALL_YEAR"
																, "m7_MAIN_CLASS"
																, "m7_MAKER"
																, "m7_NATION"
																, "m7_PURPOSE_ENG"
																, "m7_PURPOSE_KOR"
																, "m7_RESOURCE_CATEGORY"
																, "m7_SPEC_ENG"
																, "m7_SUB_CLASS"
																};
							
							String euqipmentItemId = null;
							String equipmentItemRevId = null;
							String equipmentItemName = null;
							ItemRevision equipmentItemRevision = null;
							String engName = null;
							String engSpec = null;
							String torque = null;
							String torqueValue = null;
							String quantity = null;
							String capacity = null;
							String purposeEng = null;
							String installYear = null;
							String maker = null;
							
							
							String resourceCategory = null;
							String mainClass = null;
							String lovName = null;
							
							basicSoaUtil.readProperties((BOMLine)childrenObject[i], equipmentBOMLineProperteis);
							euqipmentItemId = ((BOMLine)childrenObject[i]).get_bl_item_item_id();
							equipmentItemRevId = ((BOMLine)childrenObject[i]).get_bl_rev_item_revision_id();
							//20201110 seho 아이템 name을 revision name으로 변경
							equipmentItemName = ((BOMLine)childrenObject[i]).get_bl_rev_object_name();
//							equipmentItemName = ((BOMLine)childrenObject[i]).get_bl_item_object_name();
							equipmentItemRevision = (ItemRevision)((BOMLine)childrenObject[i]).get_bl_revision();
							equipmentItemRevision = (ItemRevision)basicSoaUtil.readProperties(equipmentItemRevision, equipmentRevisionProperties);
							
							engName = equipmentItemRevision.getPropertyDisplayableValue("m7_ENG_NAME");
							engSpec = equipmentItemRevision.getPropertyDisplayableValue("m7_SPEC_ENG");
							quantity = ((BOMLine)childrenObject[i]).get_bl_quantity();
							capacity = equipmentItemRevision.getPropertyDisplayableValue("m7_CAPACITY");
							purposeEng = equipmentItemRevision.getPropertyDisplayableValue("m7_PURPOSE_ENG");
							installYear = equipmentItemRevision.getPropertyDisplayableValue("m7_INSTALL_YEAR");
							maker = equipmentItemRevision.getPropertyDisplayableValue("m7_MAKER");
							
							columnDataHash.put("Col08", (Object) euqipmentItemId);
							
							// Type 속성 추출
							resourceCategory = equipmentItemRevision.getPropertyDisplayableValue("m7_RESOURCE_CATEGORY");
							mainClass = equipmentItemRevision.getPropertyDisplayableValue("m7_MAIN_CLASS");
							
							lovName = "M7_" +  processType.substring(0, 1) + "_EQUIP_" + resourceCategory;
							
							
							if (!"".equals(resourceCategory) && !"".equals(mainClass)) {
								TcLOVService lovService = new TcLOVService();
								List<HashMap<String, Object>> equipmentTypeList  = lovService.getLOVDescList(lovName);
								
								for (HashMap<String, Object> map : equipmentTypeList) {
									String key = map.get("VALUE") == null ? "" : map.get("VALUE").toString();
									
									if( key.equals(mainClass)) {
										String keyValue = map.get("DESCRIPTION") == null ? "" : map.get("DESCRIPTION").toString();
										columnDataHash.put("Col09", (Object) keyValue);// Type
										break;
									}
								
								}
							
							}
							columnDataHash.put("Col10", (Object) engName);
							getAdditionalProperty(equipmentItemRevision, columnDataHash);
							columnDataHash.put("Col15", (Object) engSpec);
							columnDataHash.put("Col16", (Object) quantity);
							columnDataHash.put("Col17", (Object) capacity);
							columnDataHash.put("Col18", (Object) purposeEng);
							columnDataHash.put("Col19", (Object) installYear);
							columnDataHash.put("Col20", (Object) maker);
							
							
							
							/////////////////////////////////엑셀 출력 ////////////////////////////////////////////////////////////////////////
							

							
							indexNo++;
							columnDataHash.put("Col00", (Object) (""+indexNo));
							
							String rowDataString = "";
							rowDataString = rowDataString+ columnDataHash.get("Col00");       // 순번 
							rowDataString = rowDataString + "\t"+columnDataHash.get("Col01"); // 공법 No. : bl_item_item_id
							rowDataString = rowDataString + "\t"+columnDataHash.get("Col02"); // 공법 Rev : bl_item_item_id
							rowDataString = rowDataString + "\t"+columnDataHash.get("Col03"); // 공구 Code   
							rowDataString = rowDataString + "\t"+columnDataHash.get("Col04"); // 공구 Name(English)  : m7_SPEC_ENG
							rowDataString = rowDataString + "\t"+columnDataHash.get("Col05"); // Cad
							rowDataString = rowDataString + "\t"+columnDataHash.get("Col06"); // JT
							rowDataString = rowDataString + "\t"+columnDataHash.get("Col07"); // CGR
							rowDataString = rowDataString + "\t"+columnDataHash.get("Col08"); // 기타
							rowDataString = rowDataString + "\t"+columnDataHash.get("Col09"); // Tech Spec
							rowDataString = rowDataString + "\t"+columnDataHash.get("Col10"); // Torque
							rowDataString = rowDataString + "\t"+columnDataHash.get("Col11"); // 수량
							rowDataString = rowDataString + "\t"+columnDataHash.get("Col12"); // Line
							rowDataString = rowDataString + "\t"+columnDataHash.get("Col13"); // Line Rev
							rowDataString = rowDataString + "\t"+columnDataHash.get("Col14"); // 공정
							rowDataString = rowDataString + "\t"+columnDataHash.get("Col15"); // 공정 Rev
							rowDataString = rowDataString + "\t"+columnDataHash.get("Col16"); // 작업자 Code
							rowDataString = rowDataString + "\t"+columnDataHash.get("Col17"); // 공법 명
							rowDataString = rowDataString + "\t"+columnDataHash.get("Col18"); // Find No.
							rowDataString = rowDataString + "\t"+columnDataHash.get("Col19"); // 담당자 
							rowDataString = rowDataString + "\t"+columnDataHash.get("Col20"); // 담당자 
							rowDataString = rowDataString + "\t"+columnDataHash.get("Col21"); // 담당자 
							
							
							Object[] rowData = new Object[22];
							
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
						
							
							
							  
							///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
						  
						
					}  else {
						expandAllChildLine((BOMLine)childrenObject[i]);
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
		


		HashMap<String, Object> columnDataHash = new HashMap<String, Object>();
		
		columnDataHash.put("Col01", (Object) this.currentShopCode);  // SHOP Code
		columnDataHash.put("Col02", (Object) this.currentLineCode);  // LineCode
		columnDataHash.put("Col03", (Object) this.currentLineItemRevId);  // Line Rev
		
		if (!processType.startsWith("A")) {
			columnDataHash.put("Col04", (Object) currentLineCode + "-" + currentStationCode);   // 공정 
			columnDataHash.put("Col05", (Object) currentStationItemRevId);
			
		} else {
			columnDataHash.put("Col04", "");   // 공정 
			columnDataHash.put("Col04", "");
		}
		
		////////////////////////////////////Operation 정보 추출/////////////////////////////////////
		String operationItemId = null;
		String operationItemRevId = null;
		String operationItemType = null;
		Item operationItem = null;
		ItemRevision operationItemRevision  = null;
		String operationReleasStatusDate = null;
		String operationObjectName = null;
//		String operationOwningUser = null;
		String shopCode = null;

		
		String[] operationBOMLineProperteis = new String[]{ 
				 										    "bl_item_item_id"
				 										  , "bl_rev_item_revision_id"
				 										  , "bl_rev_owning_user"
				 										  , "bl_item_object_type"
				 										  , "bl_all_child_lines"
				 										  , "m7_SHOP"
				 										  };
		
		
		
		try {
			operationBOMLine = (BOMLine)basicSoaUtil.readProperties(operationBOMLine, operationBOMLineProperteis);
			operationItemId = operationBOMLine.get_bl_item_item_id();
			operationItemType = operationBOMLine.get_bl_item_object_type();
			operationItemRevId = operationBOMLine.get_bl_rev_item_revision_id();
//			operationOwningUser = operationBOMLine.get_bl_rev_owning_user();
			shopCode = operationBOMLine.getPropertyDisplayableValue("m7_SHOP");
		} catch (Exception e) {
			this.logFileUtility.writeExceptionTrace(e);
		}
		
		
		columnDataHash.put("Col06", (Object) operationItemId);
		columnDataHash.put("Col07", (Object) operationItemRevId);
//		columnDataHash.put("Col21", (Object) operationOwningUser.substring(0, operationOwningUser.indexOf("(")));
		
		
		
		ModelObject[] chilBOMLineObjects = null;
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
						new String[]{"bl_pack_count","bl_packed_lines", "bl_is_packed", "bl_item_object_type","bl_line_object", "bl_item_item_id", "bl_rev_owning_user"});
				
				lineObject = currentOperationChildBOMLine.get_bl_line_object();
				// ------------------------------------------------
				// 전개 제외 조건 (Start)
				// ------------------------------------------------
				if(lineObject != null && lineObject instanceof AppearanceGroup){
					//itemType="M7_MfgProduct"
					return;
				}
				
				String itemType = currentOperationChildBOMLine.get_bl_item_object_type();
				if( (itemType!=null && itemType.trim().equalsIgnoreCase("M7_GeneralEquip")) || (itemType!=null && itemType.trim().equalsIgnoreCase("M7_JigFixture"))|| (itemType!=null && itemType.trim().equalsIgnoreCase("M7_Robot"))|| (itemType!=null && itemType.trim().equalsIgnoreCase("M7_Gun"))){
					
				if (currentOperationChildBOMLine.get_bl_is_packed()) {
					
					int packCount = currentOperationChildBOMLine.get_bl_pack_count();
					ModelObject[] packedLines = currentOperationChildBOMLine.get_bl_packed_lines();
					
					// Pack 된것의 Data를 가져오지 못하는 것 같아 관련된 소스를 아래와 같이 참고 함.
					packedLines = new ModelObject[currentOperationChildBOMLine.get_bl_packed_lines().length + 1];
					packedLines[0] = currentOperationChildBOMLine;
					System.arraycopy(currentOperationChildBOMLine.get_bl_packed_lines(), 0, packedLines, 1, currentOperationChildBOMLine.get_bl_packed_lines().length);
					
					for (int packIndex = 0; packedLines!=null && packIndex < packedLines.length; packIndex++) {
						BOMLine packedChildBOMLine = (BOMLine)packedLines[packIndex];
						childBOMLineVector.add(packedChildBOMLine);
					}
				}else{
					childBOMLineVector.add(currentOperationChildBOMLine);
				}
			  }
			} catch (Exception e) {
				this.logFileUtility.writeExceptionTrace(e);
			}
		}
		
		// BOMLie을 Item Id 순으로 Sort 한다.
		Collections.sort(childBOMLineVector, new BOMLineItemIdAscCompare());
		
		// M7_Tool Type의 Child Node만 검토대상으로 List 됨.
		for (int equipmentIndex = 0; childBOMLineVector!=null && equipmentIndex < childBOMLineVector.size(); equipmentIndex++) {
			
			BOMLine equipmentBOMLine = (BOMLine)childBOMLineVector.get(equipmentIndex);
			
			
			// ----------------------------------------------				
			// 부자재 BOMLine 특성값을 읽는다.
			// ----------------------------------------------
			String[] equipmentBOMLineProperteis = new String[]{
																"bl_item_item_id"
															  , "bl_rev_item_revision_id"
															  , "bl_item_object_name"
															  , "bl_revision"
															  , "bl_quantity"
															  };
			
			String[] equipmentRevisionProperties = new String[] {
																  "m7_CAPACITY"
					  											, "m7_ENG_NAME"
																, "m7_INSTALL_YEAR"
																, "m7_MAIN_CLASS"
																, "m7_MAKER"
																, "m7_NATION"
																, "m7_PURPOSE_ENG"
																, "m7_PURPOSE_KOR"
																, "m7_RESOURCE_CATEGORY"
																, "m7_SPEC_ENG"
																, "m7_SUB_CLASS"
																};
			
			String itemId = null;
			String itemRevId = null;
			String itemName = null;
			ItemRevision equipmentItemRevision = null;
			String engName = null;
			String engSpec = null;
			String torque = null;
			String torqueValue = null;
			String quantity = null;
			String capacity = null;
			String purposeEng = null;
			String installYear = null;
			String maker = null;
			
			
			String resourceCategory = null;
			String mainClass = null;
			String lovName = null;
			
			try {
				equipmentBOMLine = (BOMLine)basicSoaUtil.readProperties(equipmentBOMLine, equipmentBOMLineProperteis);
				itemId = equipmentBOMLine.get_bl_item_item_id();
				itemRevId = equipmentBOMLine.get_bl_rev_item_revision_id();
				itemName = equipmentBOMLine.get_bl_item_object_name();
				equipmentItemRevision = (ItemRevision)equipmentBOMLine.get_bl_revision();
				equipmentItemRevision = (ItemRevision)basicSoaUtil.readProperties(equipmentItemRevision, equipmentRevisionProperties);

				engName = equipmentItemRevision.getPropertyDisplayableValue("m7_ENG_NAME");
				engSpec = equipmentItemRevision.getPropertyDisplayableValue("m7_SPEC_ENG");
				quantity = equipmentBOMLine.get_bl_quantity();
				capacity = equipmentItemRevision.getPropertyDisplayableValue("m7_CAPACITY");
				purposeEng = equipmentItemRevision.getPropertyDisplayableValue("m7_PURPOSE_ENG");
				installYear = equipmentItemRevision.getPropertyDisplayableValue("m7_INSTALL_YEAR");
				maker = equipmentItemRevision.getPropertyDisplayableValue("m7_MAKER");
				
				columnDataHash.put("Col08", (Object) itemId);
				
				// Type 속성 추출
				resourceCategory = equipmentItemRevision.getPropertyDisplayableValue("m7_RESOURCE_CATEGORY");
				mainClass = equipmentItemRevision.getPropertyDisplayableValue("m7_MAIN_CLASS");
				
				lovName = "M7_" +  processType.substring(0, 1) + "_EQUIP_" + resourceCategory;
				
				
				if (!"".equals(resourceCategory) && !"".equals(mainClass)) {
				TcLOVService lovService = new TcLOVService();
				List<HashMap<String, Object>> equipmentTypeList  = lovService.getLOVDescList(lovName);
		        for (HashMap<String, Object> map : equipmentTypeList)
			      {
			          String key = map.get("VALUE") == null ? "" : map.get("VALUE").toString();
			          
			          if( key.equals(mainClass)) {
			        	  String keyValue = map.get("DESCRIPTION") == null ? "" : map.get("DESCRIPTION").toString();
			        	  columnDataHash.put("Col09", (Object) keyValue);// Type
			        	  break;
			          }
			        
			      }
		        
				}
				columnDataHash.put("Col10", (Object) itemName);
				columnDataHash.put("Col15", (Object) engSpec);
				columnDataHash.put("Col16", (Object) quantity);
				columnDataHash.put("Col17", (Object) capacity);
				columnDataHash.put("Col18", (Object) purposeEng);
				columnDataHash.put("Col19", (Object) installYear);
				columnDataHash.put("Col20", (Object) maker);
			} catch (Exception e) {
				this.logFileUtility.writeExceptionTrace(e);
			}
			
			
			if(equipmentItemRevision!=null){
				
				indexNo++;
				columnDataHash.put("Col00", (Object) (""+indexNo));
				
				
				getAdditionalProperty(equipmentItemRevision, columnDataHash);
				
				
				String rowDataString = "";
				rowDataString = rowDataString+ columnDataHash.get("Col00");       // 순번 
				rowDataString = rowDataString + "\t"+columnDataHash.get("Col01"); // 공법 No. : bl_item_item_id
				rowDataString = rowDataString + "\t"+columnDataHash.get("Col02"); // 공법 Rev : bl_item_item_id
				rowDataString = rowDataString + "\t"+columnDataHash.get("Col03"); // 공구 Code   
				rowDataString = rowDataString + "\t"+columnDataHash.get("Col04"); // 공구 Name(English)  : m7_SPEC_ENG
				rowDataString = rowDataString + "\t"+columnDataHash.get("Col05"); // Cad
				rowDataString = rowDataString + "\t"+columnDataHash.get("Col06"); // JT
				rowDataString = rowDataString + "\t"+columnDataHash.get("Col07"); // CGR
				rowDataString = rowDataString + "\t"+columnDataHash.get("Col08"); // 기타
				rowDataString = rowDataString + "\t"+columnDataHash.get("Col09"); // Tech Spec
				rowDataString = rowDataString + "\t"+columnDataHash.get("Col10"); // Torque
				rowDataString = rowDataString + "\t"+columnDataHash.get("Col11"); // 수량
				rowDataString = rowDataString + "\t"+columnDataHash.get("Col12"); // Line
				rowDataString = rowDataString + "\t"+columnDataHash.get("Col13"); // Line Rev
				rowDataString = rowDataString + "\t"+columnDataHash.get("Col14"); // 공정
				rowDataString = rowDataString + "\t"+columnDataHash.get("Col15"); // 공정 Rev
				rowDataString = rowDataString + "\t"+columnDataHash.get("Col16"); // 작업자 Code
				rowDataString = rowDataString + "\t"+columnDataHash.get("Col17"); // 공법 명
				rowDataString = rowDataString + "\t"+columnDataHash.get("Col18"); // Find No.
				rowDataString = rowDataString + "\t"+columnDataHash.get("Col19"); // 담당자 
				rowDataString = rowDataString + "\t"+columnDataHash.get("Col20"); // 담당자 
				rowDataString = rowDataString + "\t"+columnDataHash.get("Col21"); // 담당자 
				
				
				Object[] rowData = new Object[20];
				
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
		}
		
	}
	
	 /**
	  * BOM Lie의 Attribute 중 Item Id를 읽어 비교하는 Comparator 구현 Class
	  */
	static class BOMLineItemIdAscCompare implements Comparator<BOMLine> {

		/**
		 * 오름차순(ASC)
		 */
		@Override
		public int compare(BOMLine arg0, BOMLine arg1) {

			String arg0Str = null;
			try {
				arg0Str = arg0.get_bl_item_item_id();
			} catch (Exception e) {
			}
			String arg1Str = null;
			try {
				arg1Str = arg1.get_bl_item_item_id();
			} catch (Exception e) {
			}
			
			return arg0Str.compareTo(arg1Str);
		}

	}

	/**
	 * BOM Lie의 Attribute 중 Item Id를 읽어 비교하는 Comparator 구현 Class
	 *
	 */
	static class BOMLineItemIdDescCompare implements Comparator<BOMLine> {

		/**
		 * 내림차순(DESC)
		 */
		@Override
		public int compare(BOMLine arg0, BOMLine arg1) {

			String arg0Str = null;
			try {
				arg0Str = arg0.get_bl_item_item_id();
			} catch (Exception e) {
			}
			String arg1Str = null;
			try {
				arg1Str = arg1.get_bl_item_item_id();
			} catch (Exception e) {
			}
			
			return arg1Str.compareTo(arg0Str);
		}

	}
	
	
	 // CAD Data 속성
    private void getAdditionalProperty(ItemRevision eqipmentRevision, HashMap<String, Object> columnDataHash)  {

    	try {
    		
    		basicSoaUtil.readProperties(eqipmentRevision, new String[]{"IMAN_specification", "IMAN_Rendering", "IMAN_reference"});
    		ModelObject[] referenceModel = eqipmentRevision.get_IMAN_reference();
    		ModelObject[] specifiModel = eqipmentRevision.get_IMAN_specification();
    		ModelObject[] renderingModel = eqipmentRevision.get_IMAN_Rendering();
    		Vector<ModelObject> modelVector = new Vector<ModelObject>();
    		for (int i = 0;referenceModel!=null && i < referenceModel.length; i++) {
    			if (referenceModel[i] instanceof Dataset) {
    					modelVector.add(referenceModel[i]);
    				}
    				
    			}
    		
    		for (int i = 0;specifiModel!=null && i < specifiModel.length; i++) {
    			if (specifiModel[i] instanceof Dataset) {
    				modelVector.add(specifiModel[i]);
    			}
    			
    		}
    		
    		for (int i = 0;renderingModel!=null && i < renderingModel.length; i++) {
    			if (renderingModel[i] instanceof Dataset) {
    				modelVector.add(renderingModel[i]);
    			}
    			
    		}
    		
    		
    		for (int i = 0;modelVector!=null && i < modelVector.size(); i++) {
    			Dataset aDataset = (Dataset)modelVector.get(i);
    			basicSoaUtil.readProperties(aDataset, new String[]{"object_type"});
				String type = aDataset.get_object_type();
				
				if (type.equals("CATPart") || type.equals("CATDrawing")) {
					columnDataHash.put("Col11","●" );
					
	              } else if(type.equals("DirectModel"))  {
	            	  columnDataHash.put("Col12","●" );
	            	  
	              }  else if ( type.equals("CATCache")) {
	            	  columnDataHash.put("Col13","●" );
						
	              } else {
	            	  columnDataHash.put("Col14","●" );
	              }
    			
    		}
    		
    		
    	} catch ( Exception e ) {
    		e.getStackTrace();
    	}
    }
    
    
    private void getOperation(BOMLine bomline)  {
    	try {
    			if(!processType.startsWith("A")) {
    				
		    		basicSoaUtil.readProperties(bomline, new String[]{"bl_all_child_lines", "bl_item_object_type", "bl_parent"});
		    		ModelObject[] childrenBOMLine = bomline.get_bl_all_child_lines();
		    		String bomlineType = bomline.get_bl_item_object_type();
		    		ModelObject  parentBOMLine = bomline.get_bl_parent();  
		    		basicSoaUtil.readProperties(parentBOMLine, new String[]{"bl_item_object_type"});
		    		String parentType = ((BOMLine)parentBOMLine).get_bl_item_object_type();
    			}
    		
    	} catch(Exception e) {
    		e.getStackTrace();
    	}
    }
    
    private BOMLine getParentBOPLine(BOMLine bopLine, String itemType)  {
    		BOMLine parentBOPLine = null;
	    	try {
	    		basicSoaUtil.readProperties(bopLine, new String[]{"bl_parent"});
	    	
	        if (bopLine.get_bl_parent() != null) {
	            parentBOPLine = (BOMLine) bopLine.get_bl_parent();
	            basicSoaUtil.readProperties(parentBOPLine, new String[]{"bl_item_object_type"});
	            
	            if (!parentBOPLine.get_bl_item_object_type().equals(itemType)) {
	                return getParentBOPLine(parentBOPLine, itemType);
	            }
	        }
		}  catch( Exception e) {
		    e.getStackTrace();
		}

        return parentBOPLine;
    }

}
