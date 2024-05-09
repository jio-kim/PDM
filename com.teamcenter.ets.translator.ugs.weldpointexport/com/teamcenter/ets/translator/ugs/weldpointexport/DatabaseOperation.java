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

import com.kgm.common.remote.DataSet;
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
 * [SR150119-033][20150123] shcho, MProduct WeldGroup ���� ���� ����
 *                                       1) Reference�� CATPart�� ��� Dispatcher Service���� �ùٸ� sourceItemRev�� �������� ���ϹǷ� Parameter�� Revision ID�� �Ѱܹ޾� ó�� �ϵ��� ����
 *                                       2) �α׸޽��� ���� �� ���� ��ġ ����
 * [SR150119-034][20150205] shcho, MProduct WeldGroup Part Name ���� (���迡�� ������ Part Name�� WeldGroup Name������ �����ϵ��� ����)
 * [SR150605-009][20150605] shcho, DB�� ���� �������� ���� ������ ������ �����Ҷ� ������ �׷��� Revise �ϴ� ����, 000 Revision�� �ƴѰ�쿡�� ������ Revise �ϵ��� ����
 * [NON-SR] [20150624] ymjang, ���ο� �������� Weld Group �� ���泻���� üũ�ϱ� ���Ͽ� ���� �������� Weld Group ������ �����Ѵ�.
 * [������ 2�� ������] [20150907][ymjang] ������ ó���� �� ��� Part ���� ���¸� ����ϱ� ���� ���� ���̺� �����͸� �����Ѵ�.
 * [SR150714-022][20150907][ymjang] ������ ����(CATIA Feature Name) �߰��� ���� �� BOP �÷� ���� ��û
 * [NON-SR][20150925][taeku.jeong] ������ Feature Name Update ��� ������ Test ������ �߻��� ��Ÿ ���� ���� 
 * [20151126][ymjang] ������ Itmem ���� symcweb �� Item �����ÿ� �����ϰ� ������. 
 * [20151215][ymjang] ���� �߻��� ������ ���� �߼� ��� �߰�
 * [20160121][ymjang] csv file �� ������ ������ Revise �� �� ��, Remove �ؾ� ��.
 * [20160121][ymjang] csv file �� ã�� ������ ���, ���� ó�� (������ ���� �߼�)
 * [20160404][taeku.jeong] Co2 �������� �߰��ϱ����� Class ���������� ������� �ʴ� Method ����
 */
@SuppressWarnings({ "rawtypes", "unused", "unchecked" })
public class DatabaseOperation extends DefaultDatabaseOperation {
	
	// �� Class�� Dispatcher�� ����Ǵ� ������ �־ TaskPrep Class�� ����� ���Ŀ� ����Ǵ� Class�̴�.
	// Class�� �̸����� �̷�� ���� Dispatcher���� Object ������ ���� POM���� Data�� ����ǰų� �߰��Ǵ� ������
	// �ݿ��ϴ� Class�� �Ǵܵ�.
	// Co2 �������� �߰��ϸ鼭 ��κ��� ������ �����. [NON-SR][20160503] Taeku.Jeong
	
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
	
	// FMP�� ��� DatabaseOperation���� ���� ����Ǵ� TaskPrep ���� �̹� ������ ������
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
	 * Dispatcher Progrma ���� ����
	 * ----------------------------------------------------------------------------
	 * 	TaskPrep.init()
	 *  TaskPrep.prepareTask()   -> CSV ���� ���������� Validation �� CATIA Script ȣ�� (SuperClass)
	 *  DatabaseOperation.init(); -> FMP �� WeldGroup ���� �ʱ�ȭ
	 *  DatabaseOperation.getResultFileList();
	 *  DatabaseOperation.load();	-> Read CSV Files & Save Pre revision Weld Group BOM Structure
	                                -> Weld Group Item Revision�� �����ϰ� Structuer�� �����ϰų� ������.
	 *  DatabaseOperation.getResultFileList();
	 *  DatabaseOperation.processTaskPost()
	 */
	
	/**
	 * DB Query ���� �ֿ� ���� ����
	 * ----------------------------------------------------------------------------
	 *  com.kgm.service.WeldPointService ������ ������� Query
	 *  ---------------------------------------------------------------------------
	 * 	  updateWeldPointTransLog <Util.updateWeldPointTransLog()>
	 * 	  getChildren <Util.removeChildLineWithChildId(), Util.getChildren()>
	 * 	  getEcoEplInfo <Util.setMvlCondition, Util.getChildren()>
	 * 	  updateDateReleasedWithEco <Util.release(), Util.baseFmpRelease()>
	 * -----------------------------------------------------------------------------
	 * 
	 * position Matrix ����� ���� j3dcore.jar, j3dutils.jar, vecmath.jar ������ Lib�� �߰��� ��� �Ѵ�.
	 * 
	 * ������ Query�� com.kgm.service.WeldPoint2ndService��
	 * �ű� ��ϵ� Query���� �����.
	 */

	/**
	 * ���� ���� ���� �Ǵ� �κ���.
	 * �ʱ�ȭ�� ���� �ϴ� �κ�
	 */
	@Override
	public void init(ModelObject zRequest, TranslationTask zTransTask,
			String scResultDir, ITaskLogger zTaskLogger) throws Exception {
		
		// ���� ���� ȣ��Ǵ� Method -----
		
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
	 * Load Meathod�� ����Ǹ鼭 ȣ�� �Ǵ� Method
	 * CATIA P/G�� ����Ǿ� CAPPart File���� �о ������ ������ ������ CSV ������ ����� �о� ���� �κ���.
	 * ���⿡ ������ ������ ���� Co2�� ���õ� CSV ������ �о� ���� �ʴ� ���·� �����Ǿ� ����.
	 * DefaultDatabaseOperation�� �����Ǿ� �ִ� getResultFileList�� ���������� �׷��� �����Ǿ� �ִ°����� ����.
	 * ���� �ʿ��ϴٸ� DefaultDatabaseOperation �κе� ��Ȳ�� �°� ���� �ؾ� �Ǵ� ������ ����.
	 * ���� �ʱ⿡ ���� �� ������ ���� ���� �ʾ���.
	 * [NONE-SR][2016.04.18] taeku.jeong
	 */
	@Override
	public List<String> getResultFileList(TranslationDBMapInfo zDbMapInfo,
			String scResultFileType) {
		addLog("============ getResultFileList  ���� ============");
		int resultCount = zDbMapInfo.getTranslationDBMapInfoItemCount();
		addLog("============ resultCount = " + resultCount + " ============");
		return super.getResultFileList(zDbMapInfo, scResultFileType);
	}
	
	/**
	 * Load Method�� ����Ǹ鼭 ȣ��Ǵ� DefaultDatabaseOperation�� Implementation Method
	 * ���� CATPart ������ �о� �����ϴ� ������ File�� ��ȯ�� �߻��� ���Ŀ� ���� CSV ������ �о�
	 * �������� WeldGroup�� �ٿ��ִ� ����� �����ϴ� �Լ���
	 */
	@Override
	protected void load(TranslationDBMapInfo zDbMapInfo, List<String> zFileList)
			throws Exception {
		
		// ������ ���� 
		// ������ ������ ���� �߻��� �α����Ͽ� ǥ�� Flag �߰�
		
		boolean weldPointFlagFail = false;
		
		addLog("================== load Start ====================");
		
		// ���� CATIA Program�� ����Ǿ� ������ CSV ������ ���� Count �Ѵ�.
		int csvFileCount = csvFileCount();
		
		addLog("m_scResultDir : " + m_scResultDir);
		addLog("CSV File count : " + csvFileCount);
		
		// [20160121][ymjang] csv file �� ã�� ������ ���, ���� ó�� (������ ���� �߼�)
		if (csvFileCount <= 0)
		{
			addLog("csv file not found!");
			throw new Exception("csv file not found!");
		}
		
		fileList = zFileList;
		
		addLog("================== load End ====================");
		
		try{
			
			// [������ 2�� ������] [20150907][ymjang] ������ ó���� �� ��� Part ���� ���¸� ����ϱ� ���� ���� ���̺� �����͸� �����Ѵ�.
			updateWeldPointTransLog("P", ""); // ������ 

			// -------------------------------------------
			// - CSV ���Ͽ��� ���� ������ �����ϰ�
			// - ���� Weld Group Revision�� Structure Data��
			// - �� �ؼ� ���� ������� Data�� ���� ���� �Ѵ�.
			// -------------------------------------------			
			readCSVFileAndSaveWeldPointData();
			weldPointDataManager.makeInBoundData(partItemId, partItemRevId);
			// ���������� Data�� End Point�� ����� Data�� �����Ѵ�.
			weldPointDataManager.makeSaveDataForInBound(partItemId, partItemRevId);
			
			// ������� ���� �Ǹ� ���� ������ Weld Group�� Structure Data�� ��κ� ���� ������.

			// -------------------------------------------
			// - ���� �׷� �� ������ ����
			// -------------------------------------------
			syncronizeWeldGroup();
			
			// [������ 2�� ������] [20150907][ymjang] ������ ó���� �� ��� Part ���� ���¸� ����ϱ� ���� ���� ���̺� �����͸� �����Ѵ�.
			updateWeldPointTransLog("S", "Success"); // ����
			
		} catch(Exception e) {
			weldPointFlagFail = true;
			
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			PrintStream pinrtStream = new PrintStream(out);
			e.printStackTrace(pinrtStream);
			addLog(out.toString());
			
			// [������ 2�� ������] [20150907][ymjang] ������ ó���� �� ��� Part ���� ���¸� ����ϱ� ���� ���� ���̺� �����͸� �����Ѵ�.
			updateWeldPointTransLog("E", e.getMessage()); // ����
			
			// [20151215][ymjang] ���� �߻��� ������ ���� �߼� ��� �߰�
			sendMail();
			
		} finally {
			//�α� ���� �����
			
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
		
		m_zTaskLogger.info("=============== processTaskPost  ���� ============");
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
		
		// [��ü Function ������ ���� ���] [20150907][ymjang]
		// X100 �̿��� ������ ������ ������ ���� Part�� �����Ͽ� �����ϰ�, 
		// X100 �� ��츸, ������ ������ ��� ���� ������ �״�� �����Ѵ�.
		// X100 �� ������ �ٸ� ������ ���� �θ� Part ID ������ �����׷��� �����Ѵ�.
		if (projectCode.startsWith("X"))
		{
			this.partItemId = optionMap.get("PART_NO");	
			this.partItemRevId = optionMap.get("PART_REVISION_ID");		
		} else
		{
			this.partItemId = optionMap.get("PARENT_NO");	
			this.partItemRevId = optionMap.get("PARENT_REVISION_ID");		
		}
		
		// �̹�����Ǿ� �ִ� CSV���Ͽ��� ���� ������ ������ ���쵵���Ѵ�.
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
						// ���⼭�� FMP Revision�� ã�� �� ���ٸ� ���� ������ �Ѵ�.
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
		
		// Weld Group ���� ���� �ʱ�ȭ
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
		
		// ���� Revision�� WeldGroupRevision�� ������ �˻� �Ѵ�.
		// WeldGroup Revision�� Part Item Revision�� 1:1 Mapping �ǵ��� ������ (2016.04.22 ������ ����� ������ ������ ������)
		// ���� Weld Group�� ���� Revision�� ���°�� Skip�ϰ� Match �Ǵ� Revision ���� �����Ѵ�.
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
					// �߰ߵ� Revision ���Ŀ� �ٸ� Revision�� �ִ� ������.
					// �̰��� �̹� �ٸ� ������ ���� �� ������ �Ǵ��ؾ� ��.
					throw new Exception("�̹� �ٸ� ���躯������ ������ Weld Group Revision�� �ֽ��ϴ�.");// �α׸� ���ܾ� �Ұ� ����
				}	
				
				weldGroupDataHashMap = weldPointDataManager.findHaveSameEcoWeldGroupRevisionData(partItemId, ecoItemId);
				// ���� �����ϴ� WeldGroup Revision�� �ִ��� ã�� �´�.
				if(weldGroupDataHashMap!=null && weldGroupDataHashMap.size()>0){
					
					String tempTargetweldGroupItemRevId = weldGroupDataHashMap.get("item_rev_id");
					
					if(tempTargetweldGroupItemRevId.trim().equalsIgnoreCase(newWeldGroupItemRevId.trim()) == true){
						
						// Interface�� �ٽ� ���� �Ǵ� ������
						// New Weld Group�� ���� Revision�� ã�ƾ� �Ѵ�.
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
						// old Weld Grouop Revision�� ���� ��� �� (�Ƹ��� ù��° Revision�� �ƴұ�?)
						oldWeldGroupItemRevId = null;
						oldWeldGroupItemRevision = null;
					}
				}else if(latestWeldGroupItemRevision!=null){
				
					oldWeldGroupItemRevision = latestWeldGroupItemRevision;
					oldWeldGroupItemRevId = latestRevId;
				}
				
			}else{
				// Weld Group�� ���� �������� �ʴ� �����.
				// P/G ���� ������ ������ ������ �����Ǿ�� ��.
			}
				
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// WeldGroup�� ������ Revision�� ������ �ش� Data�� DB�� �����Ѵ�.
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
	 * [������ 2�� ������] [20150907][ymjang] ������ ó���� �� ��� Part ���� ���¸� ����ϱ� ���� ���� ���̺� �����͸� �����Ѵ�.
	 * @throws Exception
	 */
	public void updateWeldPointTransLog(String transFlag, String transMsg) throws Exception{
		
		try{			

			addLog("======== updateWeldPointTransLog ���� ========");
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
			Util.execute(servletUrlStr,"com.kgm.service.WeldPointService", "updateWeldPointTransLog", ds, false);
		
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
			// CSV ���Ͽ� �ѿ��� Data�� ����ϵ��� �Ѵ�.
			if(files[i].getPath()!=null && files[i].getPath().trim().toUpperCase().endsWith("CSV")==false){
				continue;
			}
			csvFileCount++;
		}
		
		return csvFileCount;
	}
	
    /**
     * Co2 ���� �߰��� ���� DB�� Data�� �����ϴ� �κк��� ������ �����Ѵ�.
     * 2016.04.15 Taeku.jeong
     * 
     * @throws Exception
     */
    public void readCSVFileAndSaveWeldPointData( ) throws Exception{
		
		addLog("======== Read CSV Weld Data (����) ========");
		addLog("= Source Item  : " + partItemId + "/" + partItemRevId);
        addLog("==================================");
		
		WeldPointDataManager aWeldPointDataManager = new WeldPointDataManager(m_zTaskLogger, buffer, isDebug);
		
		int insertCount = 0;
		try{
			
			File dir = new File(m_scResultDir);
			File[] files = dir.listFiles();
			
			// Ȥ�� ����Ǿ� ������ �� ������ Data�� �����.
			// initFmpAndWeldGroup �Լ����� ���� �����
			//aWeldPointDataManager.clearWeldPointDataRow(partItemId, partItemRevId);
			
			for( int i = 0; files != null && i < files.length; i++){
				
				if(files[i]==null || (files[i]!=null && files[i].isFile()==false)){
					continue;
				}
				// CSV ���Ͽ� �ѿ��� Data�� ����ϵ��� �Ѵ�.
				if(files[i].getPath()!=null && files[i].getPath().trim().toUpperCase().endsWith("CSV")==false){
					continue;
				}
				
				addLog("=> CSV File Path["+i+"] = "+files[i].getPath());

				// CSV ���Ͽ��� ������ ���� ������ �о� �迭�� ��´�.
				WeldInformation[] weldInformations = null;
				weldInformations = getWeldingDataFromCSVFile(servletUrlStr, partItemId, partItemRevId, files[i]);

				for (int j = 0; weldInformations!=null && j < weldInformations.length; j++) {
					
					WeldInformation tempWeldInformation = weldInformations[j];
					
					// [NON-SR][20160405][taeku.jeong] ���������� ���� �Լ� ����
					aWeldPointDataManager.saveReadCSVFileWeldDataRow(tempWeldInformation, ecoItemId);
					addLog(tempWeldInformation.toString());
					insertCount++;
				}
				
			}

			aWeldPointDataManager.translateWeldPointDataRaw2(partItemId, partItemRevId);
			double scale = (1.0d/1000.0d);
			aWeldPointDataManager.updateArrangedStartPointDataScaling(partItemId, partItemRevId, scale);
			
			addLog("======== Read CSV Weld Data (����) ========");
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
	 * ������ ������ ���õ� ������ �־��� CSV ���Ͽ��� �о� WeldInformation Object�� ��Ƽ� Return �Ѵ�.
	 * �� �Լ��� ������ ���������� �д� �κ��� ����ȭ �ϱ����� �߰��� �Լ���.
	 * [NON-SR][20160405] taeku.jeong
	 * @param serverURLStr ȣ���� WebServer URL
	 * @param itemId target item id
	 * @param itemRevId target item revision id
	 * @param csvFile Catia script���� CATPart ������ ������ ���� ������ �о� ������ CSV ���� (������, Co2���� �ΰ����� ������)
	 * @return CSV ���Ͽ��� ���� ������ ���������� ���� WeldInformation ��ü�� �迭�� ��� Return �Ѵ�.
	 */
	private WeldInformation[] getWeldingDataFromCSVFile(String serverURLStr, String itemId, String itemRevId, File csvFile) throws Exception{
		
		WeldInformation[] weldInformations = null;
		Vector<WeldInformation> dataVector = new Vector<WeldInformation>();
		/*
		 * ������ : 20200218
		 * ��ǥ �ߺ��� üũ Vector �߰�
		 */
		Vector<String> duplicateVector = new Vector<String>();
		
		// CSV ������ �ƴϸ� Return
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
				     * ������:20200218 
				     * ��ǥ �ߺ��� �ɷ����� ���� ����
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
						
						addLog("�ߺ� ��ǥ�� : " + aWeldInformation.featureName + "/ " + duplicateString);
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
     * Weld Group�� �������� �߰��ϰ� CSV ������ Named Ref ���Ϸ� ÷���Ѵ�.
     * @throws Exception
     */
	private void syncronizeWeldGroup() throws Exception{
		
		addLog("*****************************************************************");
		addLog("* syncronizeWeldGroup (����)");
		addLog("*****************************************************************");
		addLog("ChangeType = "+changeType);

		
		try{
			
			if( changeType.equals("D")){
				
				//FMP Revision���� WeldGroup�� �����Ѵ�.
			    addLog("changeType==D : FMP Revision���� WeldGroup�� ���� ");
				try{
					Util.removeChildLineWithChildId(connection, fmpItemId, newFmpItemRevId, weldGroupItemId, null);
				}catch(Exception e){
					e.printStackTrace();
				}
			}else{
				
				// ���� ���뿡 ���� �߰� ������ ������ �ݿ� �Ѵ�.
				doNormalChange();
			}
			
		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			
			addLog("*****************************************************************");
			addLog("* syncronizeWeldGroup (����)");
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
			
			//Release �� Release��¥��  Effectivity ��¥�� ECO ������ ��¥�� ����.
		    addLog("Release �� Release��¥��  Effectivity ��¥�� ECO ������ ��¥�� ����.");
            SoaHelper.getProperties(ecoItemRevision, new String[]{"item_id"});
		    addLog("servletUrlStr=" + servletUrlStr + ", eco Item ID=" + ecoItemRevision.get_item_id());
		    addLog("Release ��� ��=" + targetToReleaseList.size());
			Util.release(servletUrlStr, ecoItemRevision, targetToReleaseList, projectCode);

		}

	}

	/**
	 * Weld Group�� �����ϴ°��� ������ �Ϲ����� ������ ���� Weld Group�� �߰� �ϰų� �����ϴ� �۾��� ���� �Ѵ�.
	 * 
	 * @throws Exception
	 */
	private void doNormalChange( ) throws Exception {
		
		boolean isRevised = false;
		
		// ���� Revision�� �����ϰ� ���� ������ Revision�� ���� ��� Weld Group Revision�� ���� �Ѵ�.
																				// Change Type �� R0 �� ��� ���� ������ ��� ��Ʈ�� Revise ���� �ʴ� ��쵵 ����
		if(oldWeldGroupItemRevision!=null && newWeldGroupItemRevision == null  && !oldWeldGroupItemRevId.equals(newWeldGroupItemRevId)){ 
			newWeldGroupItemRevision = revise(connection, oldWeldGroupItemRevision, newWeldGroupItemRevId, weldGroupItemName);
			isRevised = true;
		// Old�� ���� �űԵ� ���� ��� Weld Group�� ������ �Ѵ�.
		}else if(oldWeldGroupItemRevision==null && newWeldGroupItemRevision == null){
			
			Item weldGroupItem = Util.getItem(connection, weldGroupItemId);
			if(weldGroupItem==null){
				
				// Weld Group Item�� �������Ƿ� ���� �����Ѵ�.
				newWeldGroupItemRevision = createWeldGroup();
				oldWeldGroupItemRevId = null;
				oldWeldGroupItemRevision = null;

			}else{
				// initFmpWeldGroup() ���� Old Weld Group Revision�� ���� �Ǿ����Ƿ� ���⼭�� �������� �ʴ´�.
				// Ȥ�ö� ���⿡ ������ Case�� �ִٸ� Exception ó���Ǿ���� �ϴ� ���� �ƴұ�.
			}
		}
		
		if(isRevised == true){
			//ECO No ����
			Util.setReferenceProperties(connection, newWeldGroupItemRevision, ecoItemRevision);
			addLog("FMP�� ECO No ���� : " + fmpItemId+"/"+newFmpItemRevId+" -> "+weldGroupItemId+"/"+newWeldGroupItemRevId);
			
			//FMP�� WeldGroup �߰�.
			boolean isExist = StructureExpander.haveSameChildNode(connection 
					, newFmpItemRevision, weldGroupItemId );
			if(isExist==false){
				Util.addWeldPointGroup(connection, newFmpItemRevision, newWeldGroupItemRevision);
				addLog("FMP�� WeldGroupItem �߰��� : " + weldGroupItemId+"/"+newWeldGroupItemRevId);
			}
		}
		
		// =========================
		// ���� Structure�� ���� �ϴ� Operation�� ���� �Ѵ�.
		// =========================
		WeldGroupStructureBuilder aWeldGroupStructureBuilder = new  WeldGroupStructureBuilder( m_zTaskLogger,  buffer,  isDebug,  servletUrlStr);
		aWeldGroupStructureBuilder.makeOrChangeNewWeldGroupRevisionStructure( partItemId,  partItemRevId,  newWeldGroupItemRevision);
		
		// ������ CSV ������ ���� ������ Weld Group Item Revision�� Dataset���� �߰��Ѵ�.
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
		
		// ���� Data�� ���� Data�� �� �ϴ� Data�� �����Ѵ�.
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
	 * WeldGroupID�� �ش��ϴ� ������ �׷��� �������� ������ ���� ��, F605 Function�� BOMLine ADD.
	 * �����ϸ�, �������� ������ ��, F605 Function�� BOMLine ADD.
	 * 
	 * [SR150119-034][20150205] shcho, MProduct WeldGroup Part Name ���� (���迡�� ������ Part Name�� WeldGroup Name������ �����ϵ��� ����)
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
			//ECO No ����
			Util.setReferenceProperties(connection, newWeldGroupItemRevision, ecoItemRevision);
			addLog("= FMP�� ECO No ���� : " + weldGroupItemId+"/"+newWeldGroupItemRevId);
			
			//FMP�� WeldGroup �߰�.
			Util.addWeldPointGroup(connection, newFmpItemRevision, newWeldGroupItemRevision);
			addLog("= FMP�� WeldGroupItem �߰��� : " + weldGroupItemId+"/"+newWeldGroupItemRevId);
		}
		
		return newWeldGroupItemRevision;
	}
	
	/**
	 * CSV ������ WeldGroup�� NamedRef�� �߰��ϴ� Function
	 * @param targetItemRev
	 */
	private void addWeldPointDataSet(ItemRevision targetItemRev){
		try {
			
			addLog("==============================");
			addLog("= Dataset(CSV) �߰� (E)");
			addLog("==============================");
			
			// �������� Spot Type �ϳ��� �־����Ƿ� CSV ������ ������ ��ϵǴ� �κ��� ����Ǿ� ���� �ʾƼ�
			// �������� ������ �ǽ�����.
			File dir = new File(m_scResultDir);
			File[] files = dir.listFiles();
			// CSV ������ ������ ���� ���� CSV ���� ����ŭ Dataset�� �߰��Ѵ�.
			// �߰��Ǵ� Datset�� �̸��� ������ ���¿� �̸��� _CO2�� ���Ѱ� ����
			// �ִ� 2���� Dataset�� �߰��ǰ� �ȴ�.
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
			m_zTaskLogger.info("DataSet ��� ����");
			e.printStackTrace();
		}		
	}
	
    /**
     * [20151215][ymjang] ���� �߻��� ������ ���� �߼� ��� �߰�
     */
	private void sendMail(){

		String servletUrlStr = prop.getProperty("servlet.url");
		String tsabun = prop.getProperty("Admin.User");// Admin.User=148757,158808
		
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
