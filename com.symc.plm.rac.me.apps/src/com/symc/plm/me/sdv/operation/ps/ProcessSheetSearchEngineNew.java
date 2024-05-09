package com.symc.plm.me.sdv.operation.ps;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;

import com.sap.tc.logging.standard.Message;
import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVStringUtiles;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.command.meco.dao.CustomBOPDao;
import com.symc.plm.me.utils.BOPStructureDataUtility;
import com.symc.plm.me.utils.ProcessUtil;
import com.symc.plm.me.utils.SYMTcUtil;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentAppGroupBOPLine;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentPerson;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.psebase.common.AbstractViewableTreeTable;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

/**
 * [NONE-SR][20151123] taeku.jeong �����۾� ǥ�ؼ��� �˻���ɰ��� (Query�� �̿� �ӵ� & �˻����� ��������)
 * �����۾�ǥ�ؼ� �˻������ Query�� �̿��� ���� ������ Occurence���� �˻��ϰ� �� ����� �̿���
 * BOMWindow�� �ִ� Function�� ��� ���� BOM ���� Rule�� ����� Occurence���� ã��
 * �˻� ����� Ȱ���ϴ� ����� ����� ����
 * @author Taeku.Jeong
 *
 */
public class ProcessSheetSearchEngineNew {
	
	private String currentFindKeyStr  = null;
	private InstructionSheetSearchUtil instructionSheetSearchUtil;
	
	private final String serviceClassName = "com.kgm.service.OperationSearchQueryService";

	TCSession session;
	IDataSet dataset;
	boolean isAppGroup;
	 HashMap<String, String> structureSearchConditionMap;
     int configId;
     String processType;
     String noVariantString;
     String publishItemPrefix;
     Registry registry;
     String engPrefix;
     String korPrefix;

	
	public ProcessSheetSearchEngineNew(IDataSet dataset, TCSession session){
		this.session = session;
		this.dataset = dataset;
	}
	
	public ArrayList<HashMap> findTargetOccurenceList(Registry registry){
		
		this.registry = registry;

		ArrayList<HashMap> resultList = null;
		IDataMap conditionMap = null;
		
		if(dataset.containsMap("searchConditionView")) {
			conditionMap = dataset.getDataMap("searchConditionView");
			if(conditionMap!=null){
				processType = conditionMap.getStringValue("process_type");
				configId = conditionMap.getIntValue("configId");
				noVariantString = registry.getString("ProcessSheetCommonVariant." + configId);
				publishItemPrefix = registry.getString("ProcessSheetItemIDPrefix." + configId);
				engPrefix = registry.getString("ProcessSheetItemIDPrefix.0");
				korPrefix = registry.getString("ProcessSheetItemIDPrefix.0");
			}
		}

		TCComponentBOPLine shopBOPLine = null;

		AbstractAIFUIApplication application = AIFUtility.getActiveDesktop().getCurrentApplication();
		InterfaceAIFComponent[] targetComponents = application.getTargetComponents();
		for (int i = 0; targetComponents!=null && i < targetComponents.length; i++) {
			
			if(targetComponents[i] instanceof TCComponentBOMLine){
				try {
					shopBOPLine = (TCComponentBOPLine) ((TCComponentBOPLine)targetComponents[i]).window().getTopBOMLine();
				} catch (TCException e) {
					e.printStackTrace();
				}
				
				if(shopBOPLine!=null){
					continue;
				}
			}
		}

		boolean isEnglish = true;
		if(configId==0){
			isEnglish = false;
		}

		// ---------------------------------------
		// Search Condition Making
		// ---------------------------------------

		String shopItemId = null;
		String vehicleCode = null;

		if(shopBOPLine!=null){
			try {
				shopItemId = shopBOPLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
				vehicleCode = shopBOPLine.getItemRevision().getProperty(SDVPropertyConstant.SHOP_REV_VEHICLE_CODE);
			} catch (TCException e) {
				e.printStackTrace();
			}
		}
		
		this.structureSearchConditionMap = new HashMap<String, String>();

		String value = conditionMap.getStringValue("station_code");
		if(value != null && value.length() > 0){
			structureSearchConditionMap.put("station_code", value);
		}

		String lineCode = null;
        if(conditionMap.get("line") != null) {
        	TCComponentBOPLine lineBOPLine = (TCComponentBOPLine) conditionMap.getValue("line");
        	if(lineBOPLine!=null){
        		try {
        			lineCode = lineBOPLine.getItemRevision().getProperty(SDVPropertyConstant.LINE_REV_CODE);
				} catch (TCException e) {
					e.printStackTrace();
				}
        	}
        }
		if(lineCode != null && lineCode.length() > 0){
			structureSearchConditionMap.put("line_code", lineCode);
		}

		value = conditionMap.getStringValue(SDVPropertyConstant.ITEM_ITEM_ID);
		if(value != null && value.length() > 0){
			structureSearchConditionMap.put(SDVPropertyConstant.ITEM_ITEM_ID, value);
		}

		value = conditionMap.getStringValue(SDVPropertyConstant.OPERATION_REV_KOR_NAME);
		if(value != null && value.length() > 0){
			structureSearchConditionMap.put(SDVPropertyConstant.OPERATION_REV_KOR_NAME, value);
		}

		value = conditionMap.getStringValue(SDVPropertyConstant.BL_OCC_MVL_CONDITION);
		if(value != null && value.length() > 0){
			structureSearchConditionMap.put(SDVPropertyConstant.BL_OCC_MVL_CONDITION, value);
		}

		value = conditionMap.getStringValue(SDVPropertyConstant.ITEM_OWNING_USER);
		if(value != null && value.length() > 0){
			structureSearchConditionMap.put(SDVPropertyConstant.ITEM_OWNING_USER, value);
		}

		value = conditionMap.getStringValue(SDVPropertyConstant.OPERATION_REV_MECO_NO);
		if(value != null && value.length() > 0){
			structureSearchConditionMap.put(SDVPropertyConstant.OPERATION_REV_MECO_NO, value);
		}

		value = conditionMap.getStringValue("publish_user");
		if(value != null && value.length() > 0){
			structureSearchConditionMap.put("publish_user", value);
		}

		boolean isEmpty = false;
		value = String.valueOf(conditionMap.getValue("empty_operation"));
		if(value != null && "true".equals(value)){
			structureSearchConditionMap.put("empty_operation", value);
			isEmpty = true;
		}

		boolean isDifferent = false;
		value = String.valueOf(conditionMap.getValue("different_operation"));
		if(value != null && "true".equals(value)){
			structureSearchConditionMap.put("different_operation", value);
			isDifferent = true;
		}

		boolean isNoReleased = false;
		value = String.valueOf(conditionMap.getValue("norelease_operation"));
		if(value != null && "true".equals(value)==true){
			structureSearchConditionMap.put("norelease_operation", value);
			isNoReleased = true;
		}

		value = String.valueOf(conditionMap.getValue("release_operation"));
		if(value != null && "true".equals(value)==true){
			structureSearchConditionMap.put("release_operation", value);
		}

		structureSearchConditionMap.put("vehicleCode", vehicleCode);
		
		
		System.out.println("structureSearchConditionMap.vehicleCode = "+structureSearchConditionMap.get("vehicleCode"));
		System.out.println("structureSearchConditionMap.line_code = "+structureSearchConditionMap.get("line_code"));
		
		
		
		
		
		if(isEmpty==true && isDifferent==true && isNoReleased==true){
			//MessageBox.post(new Throwable("������ ���� �˻� ���� �Դϴ�."));
			System.out.println("������ ���� �˻� ���� �Դϴ�.");
		}else{
			this.instructionSheetSearchUtil = new InstructionSheetSearchUtil();
			resultList = this.instructionSheetSearchUtil.getSearchResultData(shopBOPLine, structureSearchConditionMap , isEnglish);
		}
		
		return resultList;
	}
	
	/**
	 * [SR160912-012][20160929] �����۾�ǥ�ؼ� �˻� Logic �������� ���� ������ Class�� ������.
	 * @param resultList 
	 * @return
	 */
	public List<HashMap<String, Object>> getOperationHashMapList(ArrayList<HashMap> resultList){

    	boolean isEnglish = false;
    	if(configId==1){
    		isEnglish = true;
    	}
		
		// �˻���� Operation Map ã�Ƽ� Return
		List<HashMap<String, Object>> operationList = null;
    	
    	InstructionRowDataUtil instructionRowDataUtil = new InstructionRowDataUtil(this.instructionSheetSearchUtil);
    	operationList = instructionRowDataUtil.getTableDisplayDataList(isEnglish, resultList, structureSearchConditionMap, noVariantString);
		
		return operationList;
	}

}
