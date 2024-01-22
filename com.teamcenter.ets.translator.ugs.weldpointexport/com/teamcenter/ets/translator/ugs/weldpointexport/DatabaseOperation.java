/*==============================================================================
 Copyright 2009.
 Siemens Product Lifecycle Management Software Inc.
 All Rights Reserved.
================================================================================
File description:   This custom class is a cgrtojt specific sub class of the base
                    DatabaseOperation class which performs the loading operation
                    to Tc. This class stores results for translation requests.
                    This is a configuration specified class based on provider
                    name and translator name in DispatcherClient property file.

        Filename:   DatabaseOperation.java
=================================================================================*/

//==== Package  =================================================================
package com.teamcenter.ets.translator.ugs.weldpointexport;

//==== Imports  =================================================================
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import com.ssangyong.common.remote.DataSet;
import com.teamcenter.ets.load.DefaultDatabaseOperation;
import com.teamcenter.ets.soa.ConnectionInformation;
import com.teamcenter.ets.soa.SoaHelper;
import com.teamcenter.services.strong.core.DataManagementService;
import com.teamcenter.services.strong.core._2006_03.DataManagement.CreateItemsOutput;
import com.teamcenter.services.strong.core._2008_06.DataManagement.ReviseInfo;
import com.teamcenter.services.strong.core._2008_06.DataManagement.ReviseResponse2;
import com.teamcenter.soa.client.Connection;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.strong.Dataset;
import com.teamcenter.soa.client.model.strong.Item;
import com.teamcenter.soa.client.model.strong.ItemRevision;
import com.teamcenter.soa.exceptions.CanceledOperationException;
import com.teamcenter.translationservice.task.Option;
import com.teamcenter.translationservice.task.TranslationDBMapInfo;
import com.teamcenter.translationservice.task.TranslationTask;
import com.teamcenter.tstk.util.log.ITaskLogger;

/**
 * [SR150119-033][20150123] shcho, MProduct WeldGroup 생성 오류 수정
 *                                       1) Reference된 CATPart의 경우 Dispatcher Service에서 올바른 sourceItemRev을 가져오지 못하므로 Parameter로 Revision ID를 넘겨받아 처리 하도록 수정
 *                                       2) 로그메시지 보강 및 생성 위치 변경
 * [SR150119-034][20150205] shcho, MProduct WeldGroup Part Name 변경 (설계에서 정의한 Part Name를 WeldGroup Name에서도 동일하도록 수정)
 * [SR150605-009][20150605] shcho, DB에 이전 리비전의 하위 용접점 정보가 존재할때 용접점 그룹을 Revise 하던 것을, 000 Revision이 아닌경우에는 무조건 Revise 하도록 변경
 * [NON-SR] [20150624] ymjang, 새로운 리비전의 Weld Group 과 변경내역을 체크하기 위하여 이전 리비전의 Weld Group 정볼를 저장한다.
 * [용점접 2차 개선안] [20150907][ymjang] 용접점 처리시 각 대상 Part 별로 상태를 기록하기 위해 상태 테이블에 데이터를 생성한다.
 * [SR150714-022][20150907][ymjang] 용접점 정보(CATIA Feature Name) 추가적 추출 및 BOP 컬럼 생성 요청
 * [NON-SR][20150925][taeku.jeong] 용접점 Feature Name Update 기능 구현및 Test 과정에 발생된 기타 오류 수정 
 * [20151126][ymjang] 용점접 Itmem 명을 symcweb 의 Item 생성시와 동일하게 통일함. 
 * [20151215][ymjang] 오류 발생시 관리자 메일 발송 기능 추가
 * [20160121][ymjang] csv file 에 내용이 없더라도 Revise 를 한 후, Remove 해야 함.
 * [20160121][ymjang] csv file 을 찾지 못했을 경우, 오류 처리 (관리자 메일 발송)
 * [20160404][taeku.jeong] Co2 용접점을 추가하기위해 Class 전반적으로 사용하지 않는 Method 정리
 */
@SuppressWarnings({ "rawtypes", "unused", "unchecked" })
public class DatabaseOperation extends DefaultDatabaseOperation {
	
	// 본 Class는 Dispatcher가 수행되는 절차에 있어서 TaskPrep Class가 수행된 이후에 수행되는 Class이다.
	// Class의 이름에서 미루어 보듯 Dispatcher에서 Object 생성에 따른 POM관련 Data가 변경되거나 추가되는 내용을
	// 반영하는 Class로 판단됨.
	// Co2 용접점을 추가하면서 대부분의 내용이 변경됨. [NON-SR][20160503] Taeku.Jeong
	
	private StringBuffer buffer = new StringBuffer();
	private boolean isDebug = false;
	private Properties prop = null;
	
	private String servletUrlStr = null;
	private HashMap<String, String> optionMap = new HashMap<String, String>();
	private ArrayList<ModelObject> targetToReleaseList = new ArrayList<ModelObject>(); 
	private List<String> fileList = null;
	
	private Connection connection = null;									// Checked
	private WeldPointDataManager weldPointDataManager = null;	// Checked

	private String projectCode = null; 							// Checked
	private String changeType = null;							// Checked
	
	private String ecoItemId = null;								// Checked
	private ItemRevision ecoItemRevision = null;			// Checked
	
	private String partItemId = null;								// Checked
	private String partItemRevId = null;						// Checked
	private ItemRevision partItemRevision = null;			// Checked
	
	// FMP의 경우 DatabaseOperation보다 먼저 수행되는 TaskPrep 에서 이미 생성된 상태임
	private String fmpItemId = null;								// Checked
	private String oldFmpItemRevId = null;					// Checked
	private String newFmpItemRevId = null;					// Checked
	private ItemRevision oldFmpItemRevision = null;		// Checked
	private ItemRevision newFmpItemRevision = null;	// Checked

	private String weldGroupItemId = null;							// Checked
	private String oldWeldGroupItemRevId = null;					// Checked
	private String newWeldGroupItemRevId = null;				// Checked
	private String weldGroupItemName = null;						// Checked
	private ItemRevision oldWeldGroupItemRevision = null;	// Checked
	private ItemRevision newWeldGroupItemRevision = null;	// Checked
	
	/**
	 * Dispatcher Progrma 진행 순서
	 * ----------------------------------------------------------------------------
	 * 	TaskPrep.init()
	 *  TaskPrep.prepareTask()   -> CSV 파일 생성을위한 Validation 및 CATIA Script 호출 (SuperClass)
	 *  DatabaseOperation.init(); -> FMP 및 WeldGroup 정보 초기화
	 *  DatabaseOperation.getResultFileList();
	 *  DatabaseOperation.load();	-> Read CSV Files & Save Pre revision Weld Group BOM Structure
	                                -> Weld Group Item Revision을 생성하고 Structuer를 생성하거나 변경함.
	 *  DatabaseOperation.getResultFileList();
	 *  DatabaseOperation.processTaskPost()
	 */
	
	/**
	 * DB Query 관련 주요 변경 사항
	 * ----------------------------------------------------------------------------
	 *  com.ssangyong.service.WeldPointService 여전히 사용중인 Query
	 *  ---------------------------------------------------------------------------
	 * 	  updateWeldPointTransLog <Util.updateWeldPointTransLog()>
	 * 	  getChildren <Util.removeChildLineWithChildId(), Util.getChildren()>
	 * 	  getEcoEplInfo <Util.setMvlCondition, Util.getChildren()>
	 * 	  updateDateReleasedWithEco <Util.release(), Util.baseFmpRelease()>
	 * -----------------------------------------------------------------------------
	 * 
	 * position Matrix 계산을 위해 j3dcore.jar, j3dutils.jar, vecmath.jar 파일을 Lib에 추가해 줘야 한다.
	 * 
	 * 나머지 Query는 com.ssangyong.service.WeldPoint2ndService에
	 * 신규 등록된 Query들을 사용함.
	 */

	/**
	 * 제일 먼저 실행 되는 부분임.
	 * 초기화를 수행 하는 부분
	 */
	@Override
	public void init(ModelObject zRequest, TranslationTask zTransTask,
			String scResultDir, ITaskLogger zTaskLogger) throws Exception {
		
		// 제일 먼저 호출되는 Method -----
		
		prop = Util.getDefaultProperties("weldpointexport");
		try {
			isDebug = new Boolean(prop.getProperty("isDebug"));
		} catch (Exception e) {
			m_zTaskLogger.info("Could not find 'isDebug' property.");
		}
		
		super.init(zRequest, zTransTask, scResultDir, zTaskLogger);
		
		initFmpAndWeldGroup();
	}

	/**
	 * Load Meathod가 수행되면서 호출 되는 Method
	 * CATIA P/G이 수행되어 CAPPart File에서 읽어서 용접점 정보를 저장한 CSV 파일의 목록을 읽어 오는 부분임.
	 * 여기에 구현된 내용은 실제 Co2와 관련된 CSV 파일은 읽어 오지 않는 형태로 구현되어 있음.
	 * DefaultDatabaseOperation에 구현되어 있는 getResultFileList의 구현내용이 그렇게 구현되어 있는것으로 보임.
	 * 실제 필요하다면 DefaultDatabaseOperation 부분도 상황에 맞게 구현 해야 되는 것으로 보임.
	 * 따라서 초기에 구현 된 내용을 벗어 나지 않았음.
	 * [NONE-SR][2016.04.18] taeku.jeong
	 */
	@Override
	public List<String> getResultFileList(TranslationDBMapInfo zDbMapInfo,
			String scResultFileType) {
		addLog("============ getResultFileList  실행 ============");
		int resultCount = zDbMapInfo.getTranslationDBMapInfoItemCount();
		addLog("============ resultCount = " + resultCount + " ============");
		return super.getResultFileList(zDbMapInfo, scResultFileType);
	}
	
	/**
	 * Load Method가 수행되면서 호출되는 DefaultDatabaseOperation의 Implementation Method
	 * 실제 CATPart 파일을 읽어 저장하는 과정중 File의 변환이 발생된 이후에 실제 CSV 파일을 읽어
	 * 용접점을 WeldGroup에 붙여넣는 기능을 수행하는 함수임
	 */
	@Override
	protected void load(TranslationDBMapInfo zDbMapInfo, List<String> zFileList)
			throws Exception {
		
		// 용접점 개선 
		// 용접점 생성중 에러 발생시 로그파일에 표시 Flag 추가
		
		boolean weldPointFlagFail = false;
		
		addLog("================== load Start ====================");
		
		// 실제 CATIA Program이 실행되어 생성된 CSV 파일의 수를 Count 한다.
		int csvFileCount = csvFileCount();
		
		addLog("m_scResultDir : " + m_scResultDir);
		addLog("CSV File count : " + csvFileCount);
		
		// [20160121][ymjang] csv file 을 찾지 못했을 경우, 오류 처리 (관리자 메일 발송)
		if (csvFileCount <= 0)
		{
			addLog("csv file not found!");
			throw new Exception("csv file not found!");
		}
		
		fileList = zFileList;
		
		addLog("================== load End ====================");
		
		try{
			
			// [용점접 2차 개선안] [20150907][ymjang] 용접점 처리시 각 대상 Part 별로 상태를 기록하기 위해 상태 테이블에 데이터를 생성한다.
			updateWeldPointTransLog("P", ""); // 진행중 

			// -------------------------------------------
			// - CSV 파일에서 읽은 내용을 저장하고
			// - 이전 Weld Group Revision의 Structure Data와
			// - 비교 해서 새로 만들어질 Data를 생성 저장 한다.
			// -------------------------------------------			
			readCSVFileAndSaveWeldPointData();
			weldPointDataManager.makeInBoundData(partItemId, partItemRevId);
			// 오차범위내 Data와 End Point가 변경된 Data를 저장한다.
			weldPointDataManager.makeSaveDataForInBound(partItemId, partItemRevId);
			
			// 여기까지 진행 되면 새로 생성될 Weld Group의 Structure Data를 대부분 만든 상태임.

			// -------------------------------------------
			// - 용접 그룹 및 용접점 생성
			// -------------------------------------------
			syncronizeWeldGroup();
			
			// [용점접 2차 개선안] [20150907][ymjang] 용접점 처리시 각 대상 Part 별로 상태를 기록하기 위해 상태 테이블에 데이터를 생성한다.
			updateWeldPointTransLog("S", "Success"); // 성공
			
		} catch(Exception e) {
			weldPointFlagFail = true;
			
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			PrintStream pinrtStream = new PrintStream(out);
			e.printStackTrace(pinrtStream);
			addLog(out.toString());
			
			// [용점접 2차 개선안] [20150907][ymjang] 용접점 처리시 각 대상 Part 별로 상태를 기록하기 위해 상태 테이블에 데이터를 생성한다.
			updateWeldPointTransLog("E", e.getMessage()); // 실패
			
			// [20151215][ymjang] 오류 발생시 관리자 메일 발송 기능 추가
			sendMail();
			
		} finally {
			//로그 파일 남기기
			
			Util.printLog(m_scResultDir, buffer.toString());
			if( weldPointFlagFail ) {
				Util.printLog("D:/IF_FOLDER/WELDPOINT_LOG", "ERROR_" +  sourceItemRev.get_item_id().toString() + "_" + optionMap.get("PART_REVISION_ID"), buffer.toString());
			} else {
				Util.printLog("D:/IF_FOLDER/WELDPOINT_LOG", sourceItemRev.get_item_id().toString() + "_" + optionMap.get("PART_REVISION_ID"), buffer.toString());
			}
		}
	}
	
	@Override
	public void processTaskPost() throws Exception {
		
		addLog("@@ processTaskPost =================");
		
		m_zTaskLogger.info("=============== processTaskPost  실행 ============");
		if( noResultFileList == null || noResultFileList.isEmpty()){
			
		}
		try{
			super.processTaskPost();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	protected void addLog(String msg){
		
		if( isDebug ){
			m_zTaskLogger.info(msg);
			buffer.append(msg);
			buffer.append("\r\n");
		}
	}
	
	private void initFmpAndWeldGroup(){
		
		//ItemRevision revision = sourceItemRev;
		
		this.servletUrlStr = prop.getProperty("servlet.url");
		this.connection = SoaHelper.getSoaConnection();
		
		this.weldPointDataManager = new WeldPointDataManager(m_zTaskLogger, buffer, isDebug);
		
		Option[] option = transTask.getTranslatorOptions().getOption().clone();
		for( int i = 0; option!=null && i < option.length; i++){
			addLog("option name : " + option[i].getName() + ", option value : " + option[i].getValue());
			optionMap.put(option[i].getName(), option[i].getValue());
		}
		
		this.projectCode = optionMap.get("PROJECT_CODE");
		this.changeType = optionMap.get("CHANGE_TYPE");
		
		this.ecoItemId = optionMap.get("ECO_NO");
		if(this.ecoItemId!=null && this.ecoItemId.trim().length()>0){
			try {
				ecoItemRevision = SoaHelper.getItemFromId(this.ecoItemId, "000");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		// [차체 Function 용접점 관리 방안] [20150907][ymjang]
		// X100 이외의 차종은 별도의 용접점 관리 Part를 생성하여 관리하고, 
		// X100 의 경우만, 기존의 용접점 대상 추출 로직을 그대로 적용한다.
		// X100 을 제외한 다른 차종의 경우는 부모 Part ID 가지고 용접그룹을 생성한다.
		if (projectCode.startsWith("X"))
		{
			this.partItemId = optionMap.get("PART_NO");	
			this.partItemRevId = optionMap.get("PART_REVISION_ID");		
		} else
		{
			this.partItemId = optionMap.get("PARENT_NO");	
			this.partItemRevId = optionMap.get("PARENT_REVISION_ID");		
		}
		
		// 이미저장되어 있는 CSV파일에서 읽은 정보가 있으면 지우도록한다.
		try {
			weldPointDataManager.clearWeldPointDataRow(partItemId, partItemRevId);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		try {
			ItemRevision tempItemRevision = SoaHelper.getItemFromId(partItemId, partItemRevId);
			if(tempItemRevision!=null){
				this.partItemRevision = tempItemRevision;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		this.fmpItemId = optionMap.get("FMP_ID");
		this.newFmpItemRevId = optionMap.get("FMP_REV_ID");
		try {
			this.newFmpItemRevision = SoaHelper.getItemFromId(fmpItemId, newFmpItemRevId);
			if(newFmpItemRevision==null){
				try {
					String tempFmpItemRevId = weldPointDataManager.getECOMatchedFMPRevisionId(fmpItemId, ecoItemId);
					if(tempFmpItemRevId!=null){
						this.newFmpItemRevision = SoaHelper.getItemFromId(fmpItemId, newFmpItemRevId);
						// 여기서도 FMP Revision을 찾을 수 없다면 새로 만들어야 한다.
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if(newFmpItemRevision!=null){
				String basedOnRevisionId = Util.getBasedOnRevisionId(newFmpItemRevision);
				ItemRevision basedOnFmpItemRevision = (ItemRevision) SoaHelper.getItemFromId(fmpItemId, basedOnRevisionId);
				if(basedOnFmpItemRevision!=null){
					this.oldFmpItemRevision = basedOnFmpItemRevision; 
					if(oldFmpItemRevision!=null){
						SoaHelper.getProperties(oldFmpItemRevision, new String[]{"item_revision_id"});
						this.oldFmpItemRevId = oldFmpItemRevision.get_item_revision_id();
					}
				}
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		// Weld Group 관련 정보 초기화
		this.weldGroupItemId = partItemId.trim() + "-WeldGroup";
		if(partItemRevision!=null){
			try {
				SoaHelper.getProperties(partItemRevision, new String[]{"object_name"});
				String tempItemName = partItemRevision.get_object_name();
				if(tempItemName!=null && tempItemName.trim().length()>0){
					this.weldGroupItemName = "WELD "+tempItemName.trim();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		this.newWeldGroupItemRevId = partItemRevId;
		
		// 이전 Revision인 WeldGroupRevision의 정보를 검색 한다.
		// WeldGroup Revision은 Part Item Revision과 1:1 Mapping 되도록 정의함 (2016.04.22 윤순식 차장님 협의후 결정된 내용임)
		// 이전 Weld Group의 이전 Revision이 없는경우 Skip하고 Match 되는 Revision 부터 생성한다.
		HashMap<String, String> weldGroupDataHashMap = null;
		try {
			
			Item weldGroupItem = Util.getItem(connection, weldGroupItemId);
			if(weldGroupItem!=null){
				
				String latestRevId = null;
				ItemRevision latestWeldGroupItemRevision = Util.getLatestRevision(connection, weldGroupItemId);
				if(latestWeldGroupItemRevision!=null){
					SoaHelper.getProperties(latestWeldGroupItemRevision, new String[]{"item_revision_id"});
					latestRevId = latestWeldGroupItemRevision.get_item_revision_id();
				}
				
				if(newWeldGroupItemRevId.trim().compareToIgnoreCase(latestRevId.trim()) < 0){
					// 발견된 Revision 이후에 다른 Revision이 있는 상태임.
					// 이것은 이미 다른 설변이 진행 된 것으로 판단해야 함.
					throw new Exception("이미 다른 설계변경으로 생성된 Weld Group Revision이 있습니다.");// 로그를 남겨야 할것 같음
				}	
				
				weldGroupDataHashMap = weldPointDataManager.findHaveSameEcoWeldGroupRevisionData(partItemId, ecoItemId);
				// 실제 존재하는 WeldGroup Revision이 있는지 찾아 온다.
				if(weldGroupDataHashMap!=null && weldGroupDataHashMap.size()>0){
					
					String tempTargetweldGroupItemRevId = weldGroupDataHashMap.get("item_rev_id");
					
					if(tempTargetweldGroupItemRevId.trim().equalsIgnoreCase(newWeldGroupItemRevId.trim()) == true){
						
						// Interface가 다시 진행 되는 상태임
						// New Weld Group의 이전 Revision을 찾아야 한다.
						this.newWeldGroupItemRevision = SoaHelper.getItemFromId(weldGroupItemId, newWeldGroupItemRevId);
						if(this.newWeldGroupItemRevision!=null){
							this.oldWeldGroupItemRevision = 	Util.getBasedOnRevision(newWeldGroupItemRevision);
						}
						
					}else if(tempTargetweldGroupItemRevId.trim().compareToIgnoreCase(newWeldGroupItemRevId.trim()) < 0){
						
						this.oldWeldGroupItemRevision = weldPointDataManager.getWeldGroupPreRevision(connection, weldGroupDataHashMap);
					}
					
					if(oldWeldGroupItemRevision!=null){
						SoaHelper.getProperties(oldWeldGroupItemRevision, new String[]{"item_revision_id"});
						oldWeldGroupItemRevId = oldWeldGroupItemRevision.get_item_revision_id();
					}else{
						// old Weld Grouop Revision이 없는 경우 임 (아마도 첫번째 Revision이 아닐까?)
						oldWeldGroupItemRevId = null;
						oldWeldGroupItemRevision = null;
					}
				}else if(latestWeldGroupItemRevision!=null){
				
					oldWeldGroupItemRevision = latestWeldGroupItemRevision;
					oldWeldGroupItemRevId = latestRevId;
				}
				
			}else{
				// Weld Group이 아직 존재하지 않는 경우임.
				// P/G 진행 과정의 적절한 시점에 생성되어야 함.
			}
				
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// WeldGroup의 이전에 Revision이 있으면 해당 Data를 DB에 저장한다.
		if(oldWeldGroupItemRevision!=null){
			
			StructureExpander aStructureExpander = new StructureExpander(m_zTaskLogger, buffer, isDebug);
			try {
				aStructureExpander.saveWeldGroupLatistRevisionData(connection, servletUrlStr, partItemId, partItemRevId, oldWeldGroupItemRevision);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	}
	
	/**
	 * [용점접 2차 개선안] [20150907][ymjang] 용접점 처리시 각 대상 Part 별로 상태를 기록하기 위해 상태 테이블에 데이터를 생성한다.
	 * @throws Exception
	 */
	public void updateWeldPointTransLog(String transFlag, String transMsg) throws Exception{
		
		try{			

			addLog("======== updateWeldPointTransLog 실행 ========");
			addLog("transFlag =" + transFlag );
			
			String ecoNo = optionMap.get("ECO_NO");
			String fmpId = optionMap.get("FMP_ID");
			String projectCode = optionMap.get("PROJECT_CODE");
			String changeType = optionMap.get("CHANGE_TYPE");
			String eplId = optionMap.get("EPL_ID");
			String parentNo = optionMap.get("PARENT_NO");
			String parentRev = optionMap.get("PARENT_REVISION_ID");
			String partNo = optionMap.get("PART_NO");
			//String partNo = sourceItemRev.get_item_id();
			String partRev = optionMap.get("PART_REVISION_ID");

			DataSet ds = new DataSet();
			ds.put("ECO_NO", ecoNo);
			ds.put("FMP_ID", fmpId);
			ds.put("PROJECT_CODE", projectCode);
			ds.put("CHANGE_TYPE", changeType);
			ds.put("EPL_ID", eplId);
			ds.put("PARENT_NO", parentNo);
			ds.put("PARENT_REVISION_ID", parentRev);
			ds.put("PART_NO", partNo);
			ds.put("PART_REVISION_ID", partRev);
			ds.put("TRANS_FLAG", transFlag);
			ds.put("TRANS_MSG", transMsg);
			
			String servletUrlStr = prop.getProperty("servlet.url");
			Util.execute(servletUrlStr,"com.ssangyong.service.WeldPointService", "updateWeldPointTransLog", ds, false);
		
		}catch(Exception ex){
			ByteArrayOutputStream out = new ByteArrayOutputStream();
	        PrintStream pinrtStream = new PrintStream(out);
	        ex.printStackTrace(pinrtStream);
			addLog(out.toString());
			throw ex;
		}finally{
		}
		
	}
	
	private int csvFileCount(){
		File dir = new File(m_scResultDir);
		File[] files = dir.listFiles();
		
		int csvFileCount = 0;
		for( int i = 0; files != null && i < files.length; i++){
			
			if(files[i]==null || (files[i]!=null && files[i].isFile()==false)){
				continue;
			}
			// CSV 파일에 한에서 Data를 기록하도록 한다.
			if(files[i].getPath()!=null && files[i].getPath().trim().toUpperCase().endsWith("CSV")==false){
				continue;
			}
			csvFileCount++;
		}
		
		return csvFileCount;
	}
	
    /**
     * Co2 용접 추가를 위해 DB에 Data를 저장하는 부분부터 수정을 시작한다.
     * 2016.04.15 Taeku.jeong
     * 
     * @throws Exception
     */
    public void readCSVFileAndSaveWeldPointData( ) throws Exception{
		
		addLog("======== Read CSV Weld Data (시작) ========");
		addLog("= Source Item  : " + partItemId + "/" + partItemRevId);
        addLog("==================================");
		
		WeldPointDataManager aWeldPointDataManager = new WeldPointDataManager(m_zTaskLogger, buffer, isDebug);
		
		int insertCount = 0;
		try{
			
			File dir = new File(m_scResultDir);
			File[] files = dir.listFiles();
			
			// 혹시 저장되어 있을지 모를 기존의 Data를 지운다.
			// initFmpAndWeldGroup 함수에서 먼저 실행됨
			//aWeldPointDataManager.clearWeldPointDataRow(partItemId, partItemRevId);
			
			for( int i = 0; files != null && i < files.length; i++){
				
				if(files[i]==null || (files[i]!=null && files[i].isFile()==false)){
					continue;
				}
				// CSV 파일에 한에서 Data를 기록하도록 한다.
				if(files[i].getPath()!=null && files[i].getPath().trim().toUpperCase().endsWith("CSV")==false){
					continue;
				}
				
				addLog("=> CSV File Path["+i+"] = "+files[i].getPath());

				// CSV 파일에서 용접점 생성 정보를 읽어 배열에 담는다.
				WeldInformation[] weldInformations = null;
				weldInformations = getWeldingDataFromCSVFile(servletUrlStr, partItemId, partItemRevId, files[i]);

				for (int j = 0; weldInformations!=null && j < weldInformations.length; j++) {
					
					WeldInformation tempWeldInformation = weldInformations[j];
					
					// [NON-SR][20160405][taeku.jeong] 용접점정보 저장 함수 변경
					aWeldPointDataManager.saveReadCSVFileWeldDataRow(tempWeldInformation, ecoItemId);
					addLog(tempWeldInformation.toString());
					insertCount++;
				}
				
			}

			aWeldPointDataManager.translateWeldPointDataRaw2(partItemId, partItemRevId);
			double scale = (1.0d/1000.0d);
			aWeldPointDataManager.updateArrangedStartPointDataScaling(partItemId, partItemRevId, scale);
			
			addLog("======== Read CSV Weld Data (종료) ========");
			addLog("= Insert Count : " + (insertCount));
	        addLog("==================================");
		}catch(Exception ex){
			ByteArrayOutputStream out = new ByteArrayOutputStream();
	        PrintStream pinrtStream = new PrintStream(out);
	        ex.printStackTrace(pinrtStream);
			addLog(out.toString());
			throw ex;
		}finally{

		}		
    }

	/**
	 * 용접점 생성에 관련된 정보를 주어진 CSV 파일에서 읽어 WeldInformation Object에 담아서 Return 한다.
	 * 이 함수는 용접접 생성정보를 읽는 부분을 유연화 하기위해 추가한 함수임.
	 * [NON-SR][20160405] taeku.jeong
	 * @param serverURLStr 호출할 WebServer URL
	 * @param itemId target item id
	 * @param itemRevId target item revision id
	 * @param csvFile Catia script에서 CATPart 파일의 용접점 생성 정보를 읽어 저장한 CSV 파일 (점용접, Co2용접 두가지가 생성됨)
	 * @return CSV 파일에서 읽은 용접점 생성정보를 담은 WeldInformation 객체를 배열에 담아 Return 한다.
	 */
	private WeldInformation[] getWeldingDataFromCSVFile(String serverURLStr, String itemId, String itemRevId, File csvFile) throws Exception{
		
		WeldInformation[] weldInformations = null;
		Vector<WeldInformation> dataVector = new Vector<WeldInformation>();
		/*
		 * 수정점 : 20200218
		 * 좌표 중복점 체크 Vector 추가
		 */
		Vector<String> duplicateVector = new Vector<String>();
		
		// CSV 파일이 아니면 Return
		String fileName = csvFile.getName();
		if( fileName.length() < 3 ) {
			return weldInformations;
		}
		String extName = fileName.substring(fileName.length() - 3);
		if( extName==null || (extName!=null && extName.equalsIgnoreCase("CSV")==false)){
			return weldInformations;
		}
		
	    InputStream tempInputStream = null;
		try {
			tempInputStream = new FileInputStream( csvFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
	    BufferedReader aBufferedReader = new BufferedReader(new InputStreamReader(tempInputStream));
	    
		int dataCount = 0;
		String fileLineString = null;
		try {
			while ((fileLineString = aBufferedReader.readLine()) != null) {
				if( dataCount != 0){

					if(fileLineString==null || (fileLineString!=null && fileLineString.trim().length()<1)){
						continue;
					}
					String[] weldInfo = fileLineString.split(",");
					if(weldInfo==null || (weldInfo!=null && weldInfo.length<5)){
						continue;
					}

					WeldInformation aWeldInformation = new WeldInformation(
							m_zTaskLogger, buffer, isDebug, 
							serverURLStr, itemId, itemRevId, fileLineString);
					////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				    /*
				     * 수정점:20200218 
				     * 좌표 중복을 걸러내기 위해 수정
				     */
					
					String startX = Double.toString(aWeldInformation.getStartPointX());
					String startY = Double.toString(aWeldInformation.getStartPointY());
					String startZ = Double.toString(aWeldInformation.getStartPointZ());
					String endX = Double.toString(aWeldInformation.getEndPointX());
					String endY = Double.toString(aWeldInformation.getEndPointY());
					String endZ = Double.toString(aWeldInformation.getEndPointZ());
					String sheet = Integer.toString(aWeldInformation.getSheets());
					String weldType = aWeldInformation.weldType;
					String duplicateString = startX + "/" + startY + "/" + startZ + "/" + endX + "/" + endY + "/" + endZ + "/" + sheet + "/" + weldType;
					if(duplicateVector.size() == 0) {
						duplicateVector.add(duplicateString);
						dataVector.add(aWeldInformation);
					} else if( !duplicateVector.contains(duplicateString)) {
						duplicateVector.add(duplicateString);
						dataVector.add(aWeldInformation);
					} else {
						
						addLog("중복 좌표값 : " + aWeldInformation.featureName + "/ " + duplicateString);
					}
					////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				}
				dataCount++;
			}
		} catch (IOException e) {
			throw e;
		}finally{
			if( aBufferedReader != null ){
				try {
					aBufferedReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				aBufferedReader = null;
				tempInputStream = null;	
			}
		}
		
		if(dataVector!=null && dataVector.size()>0){
			weldInformations = new WeldInformation[dataVector.size()];
			for (int i = 0; i < weldInformations.length; i++) {
				weldInformations[i] = dataVector.get(i);
			}
		}
		
		return weldInformations;
	}
	
    /**
     * Weld Group에 용접점을 추가하고 CSV 파일을 Named Ref 파일로 첨부한다.
     * @throws Exception
     */
	private void syncronizeWeldGroup() throws Exception{
		
		addLog("*****************************************************************");
		addLog("* syncronizeWeldGroup (시작)");
		addLog("*****************************************************************");
		addLog("ChangeType = "+changeType);

		
		try{
			
			if( changeType.equals("D")){
				
				//FMP Revision에서 WeldGroup를 삭제한다.
			    addLog("changeType==D : FMP Revision에서 WeldGroup를 삭제 ");
				try{
					Util.removeChildLineWithChildId(connection, fmpItemId, newFmpItemRevId, weldGroupItemId, null);
				}catch(Exception e){
					e.printStackTrace();
				}
			}else{
				
				// 변경 내용에 따라 추가 삭제된 내용을 반영 한다.
				doNormalChange();
			}
			
		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			
			addLog("*****************************************************************");
			addLog("* syncronizeWeldGroup (종료)");
			addLog("*****************************************************************");
			
			if(this.newFmpItemRevision!=null){
				if(targetToReleaseList.contains(this.newFmpItemRevision)==false){
					targetToReleaseList.add(this.newFmpItemRevision);
				}
			}
			if(this.newWeldGroupItemRevision!=null){
				if(targetToReleaseList.contains(this.newWeldGroupItemRevision)==false){
					targetToReleaseList.add(this.newWeldGroupItemRevision);
				}
			}
			
			//Release 후 Release날짜와  Effectivity 날짜를 ECO 릴리즈 날짜로 수정.
		    addLog("Release 후 Release날짜와  Effectivity 날짜를 ECO 릴리즈 날짜로 수정.");
            SoaHelper.getProperties(ecoItemRevision, new String[]{"item_id"});
		    addLog("servletUrlStr=" + servletUrlStr + ", eco Item ID=" + ecoItemRevision.get_item_id());
		    addLog("Release 대상 수=" + targetToReleaseList.size());
			Util.release(servletUrlStr, ecoItemRevision, targetToReleaseList, projectCode);

		}

	}

	/**
	 * Weld Group을 삭제하는것을 제외한 일반적인 설변에 따라 Weld Group을 추가 하거나 변경하는 작업을 수행 한다.
	 * 
	 * @throws Exception
	 */
	private void doNormalChange( ) throws Exception {
		
		boolean isRevised = false;
		
		// 이전 Revision이 존재하고 새로 생성될 Revision이 없는 경우 Weld Group Revision을 개정 한다.
																				// Change Type 이 R0 일 경우 간헐 적으로 대상 파트를 Revise 하지 않는 경우도 있음
		if(oldWeldGroupItemRevision!=null && newWeldGroupItemRevision == null  && !oldWeldGroupItemRevId.equals(newWeldGroupItemRevId)){ 
			newWeldGroupItemRevision = revise(connection, oldWeldGroupItemRevision, newWeldGroupItemRevId, weldGroupItemName);
			isRevised = true;
		// Old도 업고 신규도 없는 경우 Weld Group을 만들어야 한다.
		}else if(oldWeldGroupItemRevision==null && newWeldGroupItemRevision == null){
			
			Item weldGroupItem = Util.getItem(connection, weldGroupItemId);
			if(weldGroupItem==null){
				
				// Weld Group Item이 없었으므로 새로 생성한다.
				newWeldGroupItemRevision = createWeldGroup();
				oldWeldGroupItemRevId = null;
				oldWeldGroupItemRevision = null;

			}else{
				// initFmpWeldGroup() 에서 Old Weld Group Revision이 정의 되었으므로 여기서는 검토하지 않는다.
				// 혹시라도 여기에 들어오는 Case가 있다면 Exception 처리되었어야 하는 것이 아닐까.
			}
		}
		
		if(isRevised == true){
			//ECO No 저장
			Util.setReferenceProperties(connection, newWeldGroupItemRevision, ecoItemRevision);
			addLog("FMP에 ECO No 설정 : " + fmpItemId+"/"+newFmpItemRevId+" -> "+weldGroupItemId+"/"+newWeldGroupItemRevId);
			
			//FMP에 WeldGroup 추가.
			boolean isExist = StructureExpander.haveSameChildNode(connection 
					, newFmpItemRevision, weldGroupItemId );
			if(isExist==false){
				Util.addWeldPointGroup(connection, newFmpItemRevision, newWeldGroupItemRevision);
				addLog("FMP에 WeldGroupItem 추가됨 : " + weldGroupItemId+"/"+newWeldGroupItemRevId);
			}
		}
		
		// =========================
		// 실제 Structure를 변경 하는 Operation을 수행 한다.
		// =========================
		WeldGroupStructureBuilder aWeldGroupStructureBuilder = new  WeldGroupStructureBuilder( m_zTaskLogger,  buffer,  isDebug,  servletUrlStr);
		aWeldGroupStructureBuilder.makeOrChangeNewWeldGroupRevisionStructure( partItemId,  partItemRevId,  newWeldGroupItemRevision);
		
		// 용접점 CSV 파일을 새로 생성된 Weld Group Item Revision에 Dataset으로 추가한다.
		if(newWeldGroupItemRevision!=null){
			updateWeldPointDataset(connection, newWeldGroupItemRevision);
		}

	}
	
	private ItemRevision revise(Connection connection, ItemRevision itemRev, String newRevisionId, String revisionName) throws Exception {
	    
	    SoaHelper.getProperties(itemRev, new String[]{"object_name", "item_revision_id"});
	    
	    ReviseInfo[] revInfo = new ReviseInfo[1];
	    revInfo[0] = new ReviseInfo();
	    revInfo[0].clientId = "";
	    revInfo[0].baseItemRevision = itemRev;
	    revInfo[0].name = revisionName;
	    revInfo[0].newRevId = newRevisionId;
	    
	    ReviseResponse2 response = DataManagementService.getService(connection).revise2(revInfo);
	    if(response.serviceData.sizeOfPartialErrors() > 0){
	        throw new Exception("Revise Fail!");
	    }
	    
        ItemRevision revisedItemRevision = null;
        if(response.serviceData!=null){
        	for (int i = 0; i < response.serviceData.sizeOfCreatedObjects(); i++) {
        		ModelObject  tmpModelObject = response.serviceData.getCreatedObject(i);
        		if(tmpModelObject!=null && tmpModelObject instanceof ItemRevision){
                	revisedItemRevision = (ItemRevision)tmpModelObject;
                	break;
                }
			}
        }
        
        //Util.removeReleaseStatus(Connection connection, ModelObject[] modelObject)
        //Util.release(servletUrlStr, ecoRevision, targetToReleaseList, projectCode);
        
        return revisedItemRevision;
	}
	
	private void updateWeldPointDataset(Connection connection, ItemRevision weldGroupItemRevision) throws Exception {
		
		if(weldGroupItemRevision==null){
			return;
		}
		
		// 이전 Data와 이후 Data를 비교 하는 Data를 생성한다.
		SoaHelper.getProperties(weldGroupItemRevision, new String[]{"IMAN_reference"});
		ModelObject[] models = weldGroupItemRevision.get_IMAN_reference();
		Vector<Dataset> csvDataset = new Vector<Dataset>();
		for (int i = 0;models!=null && i < models.length; i++) {
			if (models[i] instanceof Dataset) {
				
				Dataset aDataset = (Dataset)models[i];
				SoaHelper.getProperties(aDataset, new String[]{"object_type", "object_name"});
				String datasetTypeName = aDataset.get_object_type();
				String datasetName = aDataset.get_object_name();
				
				if(datasetTypeName!=null && datasetTypeName.trim().equalsIgnoreCase("M7_WELDPTSET")==true){
					csvDataset.add(aDataset);
				}
				
			}
		}
		
		if(csvDataset!=null){
			ModelObject[] dataseteModels = new ModelObject[csvDataset.size()];
			for (int i = 0; i < dataseteModels.length; i++) {
				dataseteModels[i] = csvDataset.get(i);
			}
			DataManagementService dmService = DataManagementService.getService(connection);
			dmService.deleteObjects(dataseteModels);
		}

		addWeldPointDataSet(weldGroupItemRevision);
	}
	
	/**
	 * WeldGroupID에 해당하는 용접점 그룹이 존재하지 않으면 생성 후, F605 Function에 BOMLine ADD.
	 * 존재하면, 리비전을 가져온 후, F605 Function에 BOMLine ADD.
	 * 
	 * [SR150119-034][20150205] shcho, MProduct WeldGroup Part Name 변경 (설계에서 정의한 Part Name를 WeldGroup Name에서도 동일하도록 수정)
	 * 
	 * @param connection
	 * @param fmpRevision
	 * @param weldGroupID
	 * @param weldGroupRevID
	 * @return
	 * @throws Exception
	 */
	private ItemRevision createWeldGroup() throws Exception{
		
		addLog("=======================");
		addLog("= Create Weld Group (S)");
		addLog("=======================");
		
		CreateItemsOutput[] output = Util.createItems(weldGroupItemId,
				weldGroupItemName, newWeldGroupItemRevId, 
				null, "S7_Vehpart", null, null, connection);
		
		addLog("=======================");
		addLog("= Create Weld Group (E)");
		addLog("=======================");
		
		for (int i = 0;output!=null && i < output.length; i++) {
			newWeldGroupItemRevision = output[i].itemRev;
			break;
		}
		
		if(newWeldGroupItemRevision!=null){
			//ECO No 저장
			Util.setReferenceProperties(connection, newWeldGroupItemRevision, ecoItemRevision);
			addLog("= FMP에 ECO No 설정 : " + weldGroupItemId+"/"+newWeldGroupItemRevId);
			
			//FMP에 WeldGroup 추가.
			Util.addWeldPointGroup(connection, newFmpItemRevision, newWeldGroupItemRevision);
			addLog("= FMP에 WeldGroupItem 추가됨 : " + weldGroupItemId+"/"+newWeldGroupItemRevId);
		}
		
		return newWeldGroupItemRevision;
	}
	
	/**
	 * CSV 파일을 WeldGroup에 NamedRef로 추가하는 Function
	 * @param targetItemRev
	 */
	private void addWeldPointDataSet(ItemRevision targetItemRev){
		try {
			
			addLog("==============================");
			addLog("= Dataset(CSV) 추가 (E)");
			addLog("==============================");
			
			// 기존에는 Spot Type 하나만 있었으므로 CSV 파일이 복수개 등록되는 부분이 고려되어 있지 않아서
			// 전반적인 수정을 실시했음.
			File dir = new File(m_scResultDir);
			File[] files = dir.listFiles();
			// CSV 파일이 생성된 폴더 내의 CSV 파일 수만큼 Dataset을 추가한다.
			// 추가되는 Datset의 이름은 기존읠 형태에 이름에 _CO2를 더한것 까지
			// 최대 2개의 Dataset이 추가되게 된다.
			for (int i = 0;files!=null && i < files.length; i++) {
				String tempPath  = files[i].getPath();
				if(tempPath!=null){
					if(tempPath.trim().toUpperCase().endsWith("CSV")==true){
						String pathStr = files[i].getPath();
						int lastIndex = pathStr.lastIndexOf("\\");
						String temp2 = pathStr.substring(lastIndex+1);
						
						if(temp2!=null){
							ArrayList<String> csvFileList = new ArrayList<String>();
							csvFileList.clear();
							csvFileList.add(temp2);
							boolean isSpotWeldCsv = true;
							if(temp2.trim().toUpperCase().indexOf("_CO2")>-1){
								isSpotWeldCsv = false;
							}
							
							if(csvFileList!=null && csvFileList.size()>0){

								WpDataSetHelper datasetSetHelper = new WpDataSetHelper(this.m_zTaskLogger, buffer,  isDebug, isSpotWeldCsv);
								datasetSetHelper.createInsertDataset(targetItemRev, sourceDataset,
										"M7_WELDPTSET", "IMAN_reference", "M7_WELDPT_CSV", m_scResultDir,
										csvFileList, false);
							}
						}
					}
				}
			}


		} catch (Exception e) {
			m_zTaskLogger.info("DataSet 등록 실패");
			e.printStackTrace();
		}		
	}
	
    /**
     * [20151215][ymjang] 오류 발생시 관리자 메일 발송 기능 추가
     */
	private void sendMail(){

		String servletUrlStr = prop.getProperty("servlet.url");
		String tsabun = prop.getProperty("Admin.User");// Admin.User=148757,158808
		
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
    		Util.execute(servletUrlStr,"com.ssangyong.service.ECOService", "sendMail", ds, false);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
    }
	
}
