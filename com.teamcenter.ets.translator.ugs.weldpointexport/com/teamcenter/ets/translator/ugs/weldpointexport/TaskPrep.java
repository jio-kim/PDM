/*==============================================================================
 Copyright 2009.
 Siemens Product Lifecycle Management Software Inc.
 All Rights Reserved.
================================================================================
File description: Custom TaskPrep class for cgrtojt translator. This sub class
                  prepares a cgrtojt translation task. This is a configuration
                  specified class based on provider name and translator name in
                  DispatcherClient property file which creates the cgr specific
                  translation request by preparing the data for translation and
                  creating the Translation request object.

        Filename:   TaskPrep.java
================================================================================*/

//==== Package  ================================================================
package com.teamcenter.ets.translator.ugs.weldpointexport;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import com.kgm.common.remote.DataSet;
import com.teamcenter.ets.extract.DefaultTaskPrep;
import com.teamcenter.ets.request.TranslationRequest;
import com.teamcenter.ets.soa.SoaHelper;
import com.teamcenter.soa.client.Connection;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.strong.Dataset;
import com.teamcenter.soa.client.model.strong.ImanFile;
import com.teamcenter.soa.client.model.strong.ItemRevision;
import com.teamcenter.translationservice.task.TranslationTask;
import com.teamcenter.tstk.util.log.ITaskLogger;

/**
 * [NON-SR][20150925][taeku.jeong] 용접점 Feature Name Update 기능 구현및 Test 과정에 발생된 기타 오류 수정
 * [20160404][taeku.jeong] Co2 용접점을 추가하기위해 Class 전반적으로 사용하지 않는 Method 정리
 */
public class TaskPrep extends DefaultTaskPrep {

	private Properties prop = null;
	private boolean isDebug = false;
	
	private StringBuffer buffer = new StringBuffer();
	private HashMap<String, String> paraMap = new HashMap<String, String>();
	private ItemRevision ecoRevision = null;
	private ArrayList<ModelObject>  targetToReleaseList = new ArrayList<ModelObject>();
	
	/**
	 * Dispatcher Progrma 진행 순서
	 * 	TaskPrep.init()
	 *  TaskPrep.prepareTask()   -> CSV 파일 생성을위한 Validation 및 CATIA Script 호출 (SuperClass)
	 *  DatabaseOperation.init(); -> FMP 및 WeldGroup 정보 초기화
	 *  DatabaseOperation.getResultFileList();
	 *  DatabaseOperation.load();	-> Read CSV Files & Save Pre revision Weld Group BOM Structure
	                                -> Weld Group Item Revision을 생성하고 Structuer를 생성하거나 변경함.
	 *  DatabaseOperation.getResultFileList();
	 *  DatabaseOperation.processTaskPost()
	 */

	
	@Override
	public void init(ModelObject request, String scSrcDir,
			ITaskLogger zTaskLogger) throws Exception {
		super.init(request, scSrcDir, zTaskLogger);
		
		// 제일 먼저 호출되는 Method
		prop = Util.getDefaultProperties("weldpointexport");
		try {
			isDebug = new Boolean(prop.getProperty("isDebug"));
		} catch (Exception e) {
			m_zTaskLogger.info("Could not find 'isDebug' property.");
		}

		addLog("## TaskPrep ##  init -------------------- ");
	}
	
	/**
	 * 실제 Dispacher Service의 변환 과정을 수행 하는 부분임.
	 */
	@Override
	public TranslationTask prepareTask() throws Exception {
		
			boolean transFailFlag = false;
			addLog("## TaskPrep ##  prepareTask -------------------- ");
			
			// 두번째로 호출 되는 Method
			
			addLog("========================= prepareTask Start ======================");
			
			TranslationTask zTransTask = new TranslationTask();
			
			// Request 객체를 읽어 Interface 대상들을 읽어 들인다.
			ModelObject[] primary_objs = this.request.getPropertyObject( "primaryObjects").getModelObjectArrayValue();
			ModelObject[] secondary_objs = this.request.getPropertyObject( "secondaryObjects").getModelObjectArrayValue();
			
			Dataset catPartDataset = null;
			String datasetName = null;
			for (int i = 0;primary_objs!=null && i < primary_objs.length; i++) {
				if(primary_objs[i] instanceof Dataset){
					String typeObjectName = primary_objs[i].getTypeObject().getName();
					if(typeObjectName!=null &&typeObjectName.trim().equalsIgnoreCase("CATPart")==true){
						catPartDataset = (Dataset)primary_objs[i];
						SoaHelper.getProperties(catPartDataset, new String[]{"object_name"});
						datasetName = catPartDataset.get_object_name();
					}
				}
			}
			
			ItemRevision tempItmeRevision = null;
			String tempItemId = null;
			String tempItemRevId = null;
			for (int i = 0;secondary_objs!=null && i < secondary_objs.length; i++) {
				if(secondary_objs[i]!=null && secondary_objs[i] instanceof ItemRevision){
					tempItmeRevision = (ItemRevision)secondary_objs[i];
					if(tempItmeRevision!=null){
						SoaHelper.getProperties(tempItmeRevision, new String[]{"item_id", "item_revision_id"});
						tempItemId = tempItmeRevision.get_item_id();
						tempItemRevId = tempItmeRevision.get_item_revision_id();
					}
				}
			}
			
			addLog("datasetName = "+datasetName);
			addLog("tempItemId/tempItemRevId = "+tempItemId+"/"+tempItemRevId);
			
			
			try {
				for (int i = 0; i < primary_objs.length; i++) {
					
					// primaryObject는 Dispatcher 구동을 위해 선택된 Dataset이 들어 있음.
					Dataset dataset = (Dataset) primary_objs[i];
		
					// 선택된 Dataset의 Type이 CATPart가 아닌경우 고려 대상이 아님으로 다음 대상을 검토하도록 한다.
					if (!dataset.getTypeObject().getName().equalsIgnoreCase("CATPart")) {
						addLog("CatPart가 없는 경우 = "+tempItemId+"/"+tempItemRevId);
						continue;
					}
		
					// 검토대상인 CATPart 의 Secondary Object를 읽어 온다.
					ItemRevision itemRev = (ItemRevision) secondary_objs[i];
					
					// Dataset의 NamedRef 중에 CATPart 파일을 찾는다.
					ImanFile zIFile = null;
					SoaHelper.getProperties(dataset, "ref_list");
					ModelObject[] contexts = dataset.get_ref_list();
					for (int j = 0;contexts!=null && j < contexts.length; j++) {
						
						// Named Ref로 저장된 File들중에 파일의 확장자가 CATPart 인것을 찾는다.
						if (contexts[j] instanceof ImanFile) {
							zIFile = (ImanFile) contexts[j];
							try {
								zIFile = (ImanFile) SoaHelper.getProperties(zIFile, "file_ext");
							} catch (Exception e) {
								transFailFlag = true;
								addLog("=========================== ImanFile 가져오는 부분에러 ==========================================");
								
								ByteArrayOutputStream out = new ByteArrayOutputStream();
								PrintStream pinrtStream = new PrintStream(out);
								e.printStackTrace(pinrtStream);
								addLog(out.toString());
								
								addLog("=========================== ImanFile 가져오는 부분에러 ==========================================");
							}
							String scFileExt = zIFile.get_file_ext();
							if (scFileExt.equalsIgnoreCase("CATPart")) {
								break;
							}
						}
						zIFile = null;
					}
		
					// 검색된 CATPart File이 없는경우 Exception 발생
					if (zIFile == null) {
						addLog("[ERROR]    No named reference found for " + dataset.get_object_string());
						throw new Exception("No named reference found for " + dataset.get_object_string());
					}
		
					// 파일을 Download 한다.
					String fileName = null;
					File zFile = TranslationRequest.getFileToStaging(zIFile, stagingLoc);
					if(zFile!=null){
						fileName = zFile.getName();
					}
					addLog("FileName = " + fileName);
					
					List<String> keyList = request.getPropertyDisplayableValues("argumentKeys");
					List<String> dataList = request.getPropertyDisplayableValues("argumentData");
					for( int j = 0; keyList != null && j < keyList.size(); j++){
						String key = keyList.get(j).trim().toUpperCase();
						String value = dataList.get(j);
						value = value == null ? null:value.trim().toUpperCase();
						addOptions(zTransTask, key, value);
						paraMap.put(key, value);
						addLog("Key[" + j + "] = " + key);
						addLog("Value[" + j + "] = " + value);
					}
					
					//FMP 체크 및 생성.
					createFmp(itemRev, zTransTask);
					
					// 용접점 생성을위해 필요한 Argument값들이 입력되었는지 확인 함
					if(isValidate()){
						// CSV 파일을 생성하고 DB에 필요한 작업도 수행 한다. 
						zTransTask = addRefIdToTask(
								prepTransTask(zTransTask, dataset, itemRev, fileName, true, true, ".csv", 0, null), i);
					}
						
				}
				
				} catch(Exception e) {
					transFailFlag = true;
					addLog("=========================== ERROR 발생 ==========================================");
					
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					PrintStream pinrtStream = new PrintStream(out);
					e.printStackTrace(pinrtStream);
					addLog(out.toString());
					
					addLog("=========================== ERROR 발생 ==========================================");
					
					sendMail();
					
				} finally {
					
					DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
					String tiem = df.format(new Date());
					
					if( transFailFlag ) {
						addLog("========================= prepareTask End ======================");
						Util.printLog("D:/IF_FOLDER/WELDPOINT_LOG", "ERROR_" + tempItemId+ "_" + tempItemRevId, buffer.toString());
						
					} else {
						addLog("========================= prepareTask End ======================");
						Util.printLog("D:/IF_FOLDER/WELDPOINT_LOG",  tempItemId+ "_" + tempItemRevId, buffer.toString());
					}
					
				}

		
		

		
		
		
		return zTransTask;
		
	}

	/**
	 * Log File을 Update 한다.
	 * @param msg
	 */
	protected void addLog(String msg){
		
		if( isDebug ){
			m_zTaskLogger.info(msg);
			buffer.append(msg);
			buffer.append("\r\n");
		}
	}
	
	/**
	 * 용접점을 붙여넣을 FMP를 생성한다.
	 * @param itemRev
	 * @param zTransTask
	 * @throws Exception
	 */
    private void createFmp(ItemRevision itemRev, TranslationTask zTransTask) throws Exception{
		
		String ecoID = paraMap.get("ECO_NO");
		ecoRevision = SoaHelper.getItemFromId(ecoID, "000");
		String fmpItemID = paraMap.get("FMP_ID");
		String fmpItemRevID = "";
		String projectCode = paraMap.get("PROJECT_CODE");
//		String eplID = paraMap.get("EPL_ID");
		ItemRevision revision = itemRev;
		SoaHelper.getProperties(itemRev, new String[]{"item_id"});
		String itemID = revision.get_item_id();
//		String weldGroupID = itemID + "-WeldGroup";
		ItemRevision fmpRevision = null;
		DataSet ds = new DataSet();
		Connection connection = SoaHelper.getSoaConnection();
		
		Calendar ecoReleaseDate = null;
		if(ecoRevision!=null){
			SoaHelper.getProperties(ecoRevision, new String[]{"date_released"});
			ecoReleaseDate = ecoRevision.get_date_released();
		}else{
			addLog("[ERROR]  eco revision is null");
			throw new Exception("eco revision is null");
		}
		String servletUrlStr = prop.getProperty("servlet.url");
		
		if(fmpItemID==null || (fmpItemID!=null && fmpItemID.trim().length()<1)){
			addLog("[ERROR]  FMP id is null");
			throw new Exception("FMP id is null");
		}
		
		try{
			ItemRevision functionRevision = Util.getFunctionRevision(fmpItemID);
			if( functionRevision == null){
				addLog("[ERROR]  Could not find the Function(" + "F" + fmpItemID.substring(1, fmpItemID.length() - 1) + ")");
				throw new Exception("Could not find the Function(" + "F" + fmpItemID.substring(1, fmpItemID.length() - 1) + ")");
			}
			SoaHelper.getProperties(functionRevision, new String[]{"item_id"});
			
//			if( !isReleased(connection, functionRevision)){
//				baseFmpRelease(servletUrlStr, ecoReleaseDate, functionRevision);
//			}
			
			//M605****A/000 FMP Revision이 릴리즈 되어 있지 않다면 릴리즈함.
			ItemRevision baseFmpRevision = SoaHelper.getItemFromId(fmpItemID, "000");
			if( baseFmpRevision == null){
				addLog("[ERROR]  Could not find the FMP(" + fmpItemID + ").");
				throw new Exception("Could not find the FMP(" + fmpItemID + ").");
			}
			
			// 불필요한 Code로 보여 Remark 함
			// [NONE-SR][20160426] taeku.jeong
//			try{
//				Util.removeReleaseStatus(connection, new ModelObject[]{baseFmpRevision});
//        		System.out.println("FMP/000의 PSR Release 제거완료.");
//        	}catch(Exception e){
//        		System.out.println("FMP/000의 PSR Release 제거실패!");
//        		System.out.println("이건의 오류에 대해서는 무시하고 진행함.");
//        		e.printStackTrace();
//        	}			
			
			if( !Util.isReleased(connection, baseFmpRevision)){
				addLog("BaseFmpRevision release Start.");
				Util.baseFmpRelease(servletUrlStr, ecoReleaseDate, baseFmpRevision, projectCode);
				addLog("BaseFmpRevision release Complete!!!.");
			}else{
				addLog("BaseFmpRevision is Released.");
			}
			
			fmpRevision = getFmpRevision(connection, fmpItemID, ecoID);
			SoaHelper.getProperties(fmpRevision, new String[]{"item_id", "item_revision_id"});
			fmpItemRevID = fmpRevision.get_item_revision_id(); 	
			
			addOptions(zTransTask, "FMP_REV_ID", fmpItemRevID);
			addOptions(zTransTask, "FUNCTION_ID", functionRevision.get_item_id());
			paraMap.put("FMP_REV_ID", fmpItemRevID);
			paraMap.put("FUNCTION_ID", functionRevision.get_item_id());
			
		}catch(Exception e){
			throw e;
		}finally{
			Util.release(servletUrlStr, ecoRevision, targetToReleaseList, projectCode);
		}
	}
    
	/**
	 * ECO ID가 일치하는 FMP를 가져온다.
	 * ECO ID와 일치하는 FMP가 존재하지 않을 경우, 최신 리비전을 가져온 후, Revis함.
	 * 
	 * @param connection
	 * @param fmpItemID
	 * @param weldGroupID
	 * @param ecoID
	 * @param eplID
	 * @return
	 * @throws Exception
	 */
	private ItemRevision getFmpRevision(Connection connection, String fmpItemID, String ecoID) throws Exception{
		//ECO ID가 일치하는 FMP를 가져온다.
		ItemRevision fmpRevision = Util.getFmpRevisionWithEco(connection, fmpItemID, ecoID);
		
		//ECO ID와 일치하는 FMP가 존재하지 않을 경우, 최신 리비전을 가져온 후, Revis함.
		if( fmpRevision == null){
			fmpRevision = Util.getLatestRevision(connection, fmpItemID);
			SoaHelper.getProperties(fmpRevision, new String[]{"item_id", "item_revision_id"});

			fmpRevision = Util.revise(fmpRevision, connection);
			
			if(fmpRevision!=null){
				targetToReleaseList.add(fmpRevision);
				//ECO No 저장
				Util.setReferenceProperties(connection, fmpRevision, ecoRevision);
			}else{
				addLog("[ERROR]  BaseFmpRevision release Complete!!!.");
				throw new Exception("Fail to revise FMP ...\nRef : com.teamcenter.ets.translator.ugs.weldpointexport.TaskPrep.getFmpRevision()");
			}
			
		}else{
			if( !Util.isReleased(connection, fmpRevision)){
				targetToReleaseList.add(fmpRevision);
			}
		}
		return fmpRevision;
	}
	
	public boolean isValidate() throws Exception{
		
		boolean haveException = false;
		String exceptionMessage = "";
		
		if( !paraMap.containsKey("ECO_NO")){
			String temMessage = "Could not find the ECO_NO.";
			if(exceptionMessage==null || (exceptionMessage!=null && exceptionMessage.trim().length()<1)){
				exceptionMessage = temMessage;
			}else{
				exceptionMessage = exceptionMessage + temMessage;
			}
			haveException = true;
		}
		if( !paraMap.containsKey("FUNCTION_ID")){
			String temMessage = "Could not find the FUNCTION_ID.";
			if(exceptionMessage==null || (exceptionMessage!=null && exceptionMessage.trim().length()<1)){
				exceptionMessage = temMessage;
			}else{
				exceptionMessage = exceptionMessage + temMessage;
			}
			haveException = true;
		}
		if( !paraMap.containsKey("FMP_ID")){
			String temMessage = "Could not find the FMP_ID.";
			if(exceptionMessage==null || (exceptionMessage!=null && exceptionMessage.trim().length()<1)){
				exceptionMessage = temMessage;
			}else{
				exceptionMessage = exceptionMessage + temMessage;
			}
			haveException = true;
		}
		if( !paraMap.containsKey("FMP_REV_ID")){
			String temMessage = "Could not find the FMP_REV_ID.";
			if(exceptionMessage==null || (exceptionMessage!=null && exceptionMessage.trim().length()<1)){
				exceptionMessage = temMessage;
			}else{
				exceptionMessage = exceptionMessage + temMessage;
			}
			haveException = true;
		}
		if( !paraMap.containsKey("CHANGE_TYPE")){
			String temMessage = "Could not find the CHANGE_TYPE. EX)R0,F1,D...";
			if(exceptionMessage==null || (exceptionMessage!=null && exceptionMessage.trim().length()<1)){
				exceptionMessage = temMessage;
			}else{
				exceptionMessage = exceptionMessage + temMessage;
			}
			haveException = true;
		}
		if( !paraMap.containsKey("EPL_ID")){
			String temMessage = "Could not find the EPL_ID. See the ECO_BOM_LIST table.";
			if(exceptionMessage==null || (exceptionMessage!=null && exceptionMessage.trim().length()<1)){
				exceptionMessage = temMessage;
			}else{
				exceptionMessage = exceptionMessage + temMessage;
			}
			haveException = true;
		}
		if( !paraMap.containsKey("PROJECT_CODE")){
			String temMessage = "Could not find the PROJECT_CODE.";
			if(exceptionMessage==null || (exceptionMessage!=null && exceptionMessage.trim().length()<1)){
				exceptionMessage = temMessage;
			}else{
				exceptionMessage = exceptionMessage + temMessage;
			}
			haveException = true;
		}
		
		// Exception이 있는경우 해당 메시지를 모두 모아서 Throw 한다.
		if(haveException==true){
			addLog("[ERROR]  " + exceptionMessage);
			throw new Exception(exceptionMessage);
		}
		
		return true;
	}
	
	private void sendMail(){

		String servletUrlStr = prop.getProperty("servlet.url");
		String tsabun = prop.getProperty("Admin.User");// Admin.User=208748
		
    	String title = "New PLM : Dispatcher Server 용접점 생성 오류 Error 알림";
    	String body = "<PRE>";
    	body += buffer.toString();
    	body += "</PRE>";

    	DataSet ds = new DataSet();
		ds.put("the_sysid", "NPLM");
		ds.put("the_sabun", "NPLM");

		ds.put("the_title", title);
		ds.put("the_remark", body);
		ds.put("the_tsabun", tsabun);
    		
		try {
    		Util.execute(servletUrlStr,"com.kgm.service.ECOService", "sendMail", ds, false);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
    }
	
	
}
