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
 * [NON-SR][20150925][taeku.jeong] ������ Feature Name Update ��� ������ Test ������ �߻��� ��Ÿ ���� ����
 * [20160404][taeku.jeong] Co2 �������� �߰��ϱ����� Class ���������� ������� �ʴ� Method ����
 */
public class TaskPrep extends DefaultTaskPrep {

	private Properties prop = null;
	private boolean isDebug = false;
	
	private StringBuffer buffer = new StringBuffer();
	private HashMap<String, String> paraMap = new HashMap<String, String>();
	private ItemRevision ecoRevision = null;
	private ArrayList<ModelObject>  targetToReleaseList = new ArrayList<ModelObject>();
	
	/**
	 * Dispatcher Progrma ���� ����
	 * 	TaskPrep.init()
	 *  TaskPrep.prepareTask()   -> CSV ���� ���������� Validation �� CATIA Script ȣ�� (SuperClass)
	 *  DatabaseOperation.init(); -> FMP �� WeldGroup ���� �ʱ�ȭ
	 *  DatabaseOperation.getResultFileList();
	 *  DatabaseOperation.load();	-> Read CSV Files & Save Pre revision Weld Group BOM Structure
	                                -> Weld Group Item Revision�� �����ϰ� Structuer�� �����ϰų� ������.
	 *  DatabaseOperation.getResultFileList();
	 *  DatabaseOperation.processTaskPost()
	 */

	
	@Override
	public void init(ModelObject request, String scSrcDir,
			ITaskLogger zTaskLogger) throws Exception {
		super.init(request, scSrcDir, zTaskLogger);
		
		// ���� ���� ȣ��Ǵ� Method
		prop = Util.getDefaultProperties("weldpointexport");
		try {
			isDebug = new Boolean(prop.getProperty("isDebug"));
		} catch (Exception e) {
			m_zTaskLogger.info("Could not find 'isDebug' property.");
		}

		addLog("## TaskPrep ##  init -------------------- ");
	}
	
	/**
	 * ���� Dispacher Service�� ��ȯ ������ ���� �ϴ� �κ���.
	 */
	@Override
	public TranslationTask prepareTask() throws Exception {
		
			boolean transFailFlag = false;
			addLog("## TaskPrep ##  prepareTask -------------------- ");
			
			// �ι�°�� ȣ�� �Ǵ� Method
			
			addLog("========================= prepareTask Start ======================");
			
			TranslationTask zTransTask = new TranslationTask();
			
			// Request ��ü�� �о� Interface ������ �о� ���δ�.
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
					
					// primaryObject�� Dispatcher ������ ���� ���õ� Dataset�� ��� ����.
					Dataset dataset = (Dataset) primary_objs[i];
		
					// ���õ� Dataset�� Type�� CATPart�� �ƴѰ�� ��� ����� �ƴ����� ���� ����� �����ϵ��� �Ѵ�.
					if (!dataset.getTypeObject().getName().equalsIgnoreCase("CATPart")) {
						addLog("CatPart�� ���� ��� = "+tempItemId+"/"+tempItemRevId);
						continue;
					}
		
					// �������� CATPart �� Secondary Object�� �о� �´�.
					ItemRevision itemRev = (ItemRevision) secondary_objs[i];
					
					// Dataset�� NamedRef �߿� CATPart ������ ã�´�.
					ImanFile zIFile = null;
					SoaHelper.getProperties(dataset, "ref_list");
					ModelObject[] contexts = dataset.get_ref_list();
					for (int j = 0;contexts!=null && j < contexts.length; j++) {
						
						// Named Ref�� ����� File���߿� ������ Ȯ���ڰ� CATPart �ΰ��� ã�´�.
						if (contexts[j] instanceof ImanFile) {
							zIFile = (ImanFile) contexts[j];
							try {
								zIFile = (ImanFile) SoaHelper.getProperties(zIFile, "file_ext");
							} catch (Exception e) {
								transFailFlag = true;
								addLog("=========================== ImanFile �������� �κп��� ==========================================");
								
								ByteArrayOutputStream out = new ByteArrayOutputStream();
								PrintStream pinrtStream = new PrintStream(out);
								e.printStackTrace(pinrtStream);
								addLog(out.toString());
								
								addLog("=========================== ImanFile �������� �κп��� ==========================================");
							}
							String scFileExt = zIFile.get_file_ext();
							if (scFileExt.equalsIgnoreCase("CATPart")) {
								break;
							}
						}
						zIFile = null;
					}
		
					// �˻��� CATPart File�� ���°�� Exception �߻�
					if (zIFile == null) {
						addLog("[ERROR]    No named reference found for " + dataset.get_object_string());
						throw new Exception("No named reference found for " + dataset.get_object_string());
					}
		
					// ������ Download �Ѵ�.
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
					
					//FMP üũ �� ����.
					createFmp(itemRev, zTransTask);
					
					// ������ ���������� �ʿ��� Argument������ �ԷµǾ����� Ȯ�� ��
					if(isValidate()){
						// CSV ������ �����ϰ� DB�� �ʿ��� �۾��� ���� �Ѵ�. 
						zTransTask = addRefIdToTask(
								prepTransTask(zTransTask, dataset, itemRev, fileName, true, true, ".csv", 0, null), i);
					}
						
				}
				
				} catch(Exception e) {
					transFailFlag = true;
					addLog("=========================== ERROR �߻� ==========================================");
					
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					PrintStream pinrtStream = new PrintStream(out);
					e.printStackTrace(pinrtStream);
					addLog(out.toString());
					
					addLog("=========================== ERROR �߻� ==========================================");
					
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
	 * Log File�� Update �Ѵ�.
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
	 * �������� �ٿ����� FMP�� �����Ѵ�.
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
			
			//M605****A/000 FMP Revision�� ������ �Ǿ� ���� �ʴٸ� ��������.
			ItemRevision baseFmpRevision = SoaHelper.getItemFromId(fmpItemID, "000");
			if( baseFmpRevision == null){
				addLog("[ERROR]  Could not find the FMP(" + fmpItemID + ").");
				throw new Exception("Could not find the FMP(" + fmpItemID + ").");
			}
			
			// ���ʿ��� Code�� ���� Remark ��
			// [NONE-SR][20160426] taeku.jeong
//			try{
//				Util.removeReleaseStatus(connection, new ModelObject[]{baseFmpRevision});
//        		System.out.println("FMP/000�� PSR Release ���ſϷ�.");
//        	}catch(Exception e){
//        		System.out.println("FMP/000�� PSR Release ���Ž���!");
//        		System.out.println("�̰��� ������ ���ؼ��� �����ϰ� ������.");
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
	 * ECO ID�� ��ġ�ϴ� FMP�� �����´�.
	 * ECO ID�� ��ġ�ϴ� FMP�� �������� ���� ���, �ֽ� �������� ������ ��, Revis��.
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
		//ECO ID�� ��ġ�ϴ� FMP�� �����´�.
		ItemRevision fmpRevision = Util.getFmpRevisionWithEco(connection, fmpItemID, ecoID);
		
		//ECO ID�� ��ġ�ϴ� FMP�� �������� ���� ���, �ֽ� �������� ������ ��, Revis��.
		if( fmpRevision == null){
			fmpRevision = Util.getLatestRevision(connection, fmpItemID);
			SoaHelper.getProperties(fmpRevision, new String[]{"item_id", "item_revision_id"});

			fmpRevision = Util.revise(fmpRevision, connection);
			
			if(fmpRevision!=null){
				targetToReleaseList.add(fmpRevision);
				//ECO No ����
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
		
		// Exception�� �ִ°�� �ش� �޽����� ��� ��Ƽ� Throw �Ѵ�.
		if(haveException==true){
			addLog("[ERROR]  " + exceptionMessage);
			throw new Exception(exceptionMessage);
		}
		
		return true;
	}
	
	private void sendMail(){

		String servletUrlStr = prop.getProperty("servlet.url");
		String tsabun = prop.getProperty("Admin.User");// Admin.User=208748
		
    	String title = "New PLM : Dispatcher Server ������ ���� ���� Error �˸�";
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
