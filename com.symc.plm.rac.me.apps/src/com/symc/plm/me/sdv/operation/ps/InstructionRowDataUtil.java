package com.symc.plm.me.sdv.operation.ps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.psebase.common.AbstractViewableTreeTable;

public class InstructionRowDataUtil {

	private boolean isEnglish = false;
	private ArrayList<HashMap> queryResult;
	private TCComponentBOMWindow window;
	private String optionCondition;
	private HashMap<String, String> searchConditionMap;
	private String noVariantString;
	private InstructionSheetSearchUtil instructionSheetSearchUtil;
	
	public InstructionRowDataUtil(InstructionSheetSearchUtil instructionSheetSearchUtil){
		this.instructionSheetSearchUtil = instructionSheetSearchUtil;
	}
	
	public List<HashMap<String, Object>> getTableDisplayDataList(
			boolean isEnglish, 
			ArrayList<HashMap> queryResult, 
			HashMap<String, String> searchConditionMap,
			String noVariantString){
		
		List<HashMap<String, Object>> tableDisplayDataHash = null;
		
		this.isEnglish = isEnglish;
		this.queryResult = queryResult;
		this.searchConditionMap = searchConditionMap;
		this.noVariantString = noVariantString;
		
    	// TCComponentBOPLine을 Operation List에 담에서 Return 해야 한다.
    	MFGLegacyApplication mfgApp = (MFGLegacyApplication) AIFUtility.getCurrentApplication();
    	AbstractViewableTreeTable treetable = mfgApp.getAbstractViewableTreeTable();
    	try {
			this.window = treetable.getBOMWindow();
		} catch (TCException e) {
			e.printStackTrace();
		}
		
    	if(window==null){
    		return tableDisplayDataHash;
    	}
    	
    	tableDisplayDataHash = new ArrayList<HashMap<String, Object>>();
    			
    	int rowIdx = 0;
    	
    	for (int i = 0; queryResult!=null && i < queryResult.size(); i++) {
    		HashMap resultRowHash = queryResult.get(i);
    		
    		HashMap<String, Object> displayRowData = null;
    		if(this.isEnglish==true){
    			displayRowData = getEngSearchResultDataRead(resultRowHash);
    		}else{
    			displayRowData = getKorSearchResultDataRead(resultRowHash);
    		}
    		
    		if(displayRowData!=null){
    			displayRowData.put("rowIdx", String.valueOf(++rowIdx)); 
    	    	tableDisplayDataHash.add(displayRowData);
    		}
		}
		
		return tableDisplayDataHash;
	}
	
	private HashMap<String, Object> getKorSearchResultDataRead(HashMap resultRowHash){
		
		HashMap<String, Object> displayRowData = null;
		
		displayRowData = getCommonSearchResultDataRead(resultRowHash, displayRowData);
		
		// BOPLIne을 못찾은 경우 Null을 Return 하도록 한다.
		if(displayRowData==null){
			return displayRowData;
		}
		
		String rowOperationId = null;
		String rowKpubRev = null;
		String rowKpubStatus = null;
		String rowKpubDate = null;
		String rowKpubOpRev = null;
		String rowKpubUser = null;
		String rowKpubRevPuid = null;
		
		if(resultRowHash.get("OPERATION_ID")!=null) rowOperationId = resultRowHash.get("OPERATION_ID").toString();
		if(resultRowHash.get("KPUB_REV")!=null) rowKpubRev = resultRowHash.get("KPUB_REV").toString();
		if(resultRowHash.get("KPUB_STATUS")!=null) rowKpubStatus = resultRowHash.get("KPUB_STATUS").toString();
		if(resultRowHash.get("KPUB_DATE")!=null) rowKpubDate = resultRowHash.get("KPUB_DATE").toString();
		if(resultRowHash.get("KPUB_OP_REV")!=null) rowKpubOpRev = resultRowHash.get("KPUB_OP_REV").toString();
		if(resultRowHash.get("KPUB_USER")!=null) rowKpubUser = resultRowHash.get("KPUB_USER").toString();
		if(resultRowHash.get("KPUB_REV_PUID")!=null) rowKpubRevPuid = resultRowHash.get("KPUB_REV_PUID").toString();

		// Publish Rev List
		List<String> itemRevisionIdList = this.instructionSheetSearchUtil.getPublishedRevisionListData(rowOperationId);
		
		if(itemRevisionIdList!=null){
			displayRowData.put("selected_publish_rev", rowKpubRev);
			displayRowData.put("publish_rev", itemRevisionIdList.toArray());	// ComboBox
		}
		
		displayRowData.put("publish_status", rowKpubStatus);
		displayRowData.put("publsih_date", rowKpubDate);
		displayRowData.put("publish_user", rowKpubUser);
		
		return displayRowData;
	}
	
	private HashMap<String, Object> getEngSearchResultDataRead(HashMap resultRowHash){
		
		HashMap<String, Object> displayRowData = null;
		displayRowData = getCommonSearchResultDataRead(resultRowHash, displayRowData);
		
		// BOPLIne을 못찾은 경우 Null을 Return 하도록 한다.
		if(displayRowData==null){
			return displayRowData;
		}
		
		String rowOperationId = null;
		String rowKpubRev = null;
		String rowKpubStatus = null;
		String rowKpubDate = null;
		String rowKpubOpRev = null;
		String rowKpubUser = null;
		String rowKpubRevPuid = null;
		
		String rowEpubRev = null;
		String rowEpubStatus = null;
		String rowEpubDate = null;
		String rowEpubOpRev = null;
		String rowEpubUser = null;
		String rowEpubRevPuid = null;
		
		if(resultRowHash.get("OPERATION_ID")!=null) rowOperationId = resultRowHash.get("OPERATION_ID").toString();
		if(resultRowHash.get("KPUB_REV")!=null) rowKpubRev = resultRowHash.get("KPUB_REV").toString();
		if(resultRowHash.get("KPUB_STATUS")!=null) rowKpubStatus = resultRowHash.get("KPUB_STATUS").toString();
		if(resultRowHash.get("KPUB_DATE")!=null) rowKpubDate = resultRowHash.get("KPUB_DATE").toString();
		if(resultRowHash.get("KPUB_OP_REV")!=null) rowKpubOpRev = resultRowHash.get("KPUB_OP_REV").toString();
		if(resultRowHash.get("KPUB_USER")!=null) rowKpubUser = resultRowHash.get("KPUB_USER").toString();
		if(resultRowHash.get("KPUB_REV_PUID")!=null) rowKpubRevPuid = resultRowHash.get("KPUB_REV_PUID").toString();
		
		if(resultRowHash.get("EPUB_REV")!=null) rowEpubRev = resultRowHash.get("EPUB_REV").toString();
		if(resultRowHash.get("EPUB_STATUS")!=null) rowEpubStatus = resultRowHash.get("EPUB_STATUS").toString();
		if(resultRowHash.get("EPUB_DATE")!=null) rowEpubDate = resultRowHash.get("EPUB_DATE").toString();
		if(resultRowHash.get("EPUB_OP_REV")!=null) rowEpubOpRev = resultRowHash.get("EPUB_OP_REV").toString();
		if(resultRowHash.get("EPUB_USER")!=null) rowEpubUser = resultRowHash.get("EPUB_USER").toString();
		if(resultRowHash.get("EPUB_REV_PUID")!=null) rowEpubRevPuid = resultRowHash.get("EPUB_REV_PUID").toString();

		displayRowData.put("kor_publish_rev", rowKpubRev);
		displayRowData.put("kor_publish_status", rowKpubStatus);
		displayRowData.put("kor_publsih_date", rowKpubDate);
		displayRowData.put("kor_publish_user", rowKpubUser);

		// Publish Rev List
		List<String> itemRevisionIdList = this.instructionSheetSearchUtil.getPublishedRevisionListData(rowOperationId);
		
		if(itemRevisionIdList!=null){
			displayRowData.put("selected_publish_rev", rowEpubRev);
			displayRowData.put("publish_rev", itemRevisionIdList.toArray());	// ComboBox
		}

		displayRowData.put("publish_status", rowEpubStatus);
		displayRowData.put("publsih_date", rowEpubDate);
		displayRowData.put("publish_user", rowEpubUser);
		
		return displayRowData;
	}
	
	private HashMap<String, Object> getCommonSearchResultDataRead(HashMap resultRowHash, HashMap<String, Object> displayRowData){
		
		String rowKeyCode = null;
		String rowVehicleCode = null;
		String rowShopCode = null;
		String rowLineCode = null;
		String rowStationCode = null;
		String rowOperationCode = null;
		String rowOperationId = null;
		String rowOperationRev = null;
		String rowOperationName = null;
		String rowOptionCond = null;
		String rowMecoId = null;
		String rowOpReleaseDate = null;
		String rowMecoOwnerName = null;
		String rowEnglishName = null;
		String rowAppNodePathPuid = null;

		if(resultRowHash.get("KEY_CODE")!=null) rowKeyCode = resultRowHash.get("KEY_CODE").toString();
		if(resultRowHash.get("VEHICLE_CODE")!=null) rowVehicleCode = resultRowHash.get("VEHICLE_CODE").toString();
		if(resultRowHash.get("SHOP_CODE")!=null) rowShopCode = resultRowHash.get("SHOP_CODE").toString();
		if(resultRowHash.get("LINE_CODE")!=null) rowLineCode = resultRowHash.get("LINE_CODE").toString();
		if(resultRowHash.get("STATION_CODE")!=null) rowStationCode = resultRowHash.get("STATION_CODE").toString();
		if(resultRowHash.get("OPERATION_CODE")!=null) rowOperationCode = resultRowHash.get("OPERATION_CODE").toString();
		if(resultRowHash.get("OPERATION_ID")!=null) rowOperationId = resultRowHash.get("OPERATION_ID").toString();
		if(resultRowHash.get("OPERATION_REV")!=null) rowOperationRev = resultRowHash.get("OPERATION_REV").toString();
		if(resultRowHash.get("OPERATION_NAME")!=null) rowOperationName = resultRowHash.get("OPERATION_NAME").toString();
		if(resultRowHash.get("OPTION_COND")!=null) rowOptionCond = resultRowHash.get("OPTION_COND").toString();
		if(resultRowHash.get("MECO_ID")!=null) rowMecoId = resultRowHash.get("MECO_ID").toString();
		if(resultRowHash.get("OP_RELEASE_DATE")!=null) rowOpReleaseDate = resultRowHash.get("OP_RELEASE_DATE").toString();
		if(resultRowHash.get("MECO_OWNER_NAME")!=null) rowMecoOwnerName = resultRowHash.get("MECO_OWNER_NAME").toString();
		if(resultRowHash.get("ENGLISH_NAME")!=null) rowEnglishName = resultRowHash.get("ENGLISH_NAME").toString();
		if(resultRowHash.get("APP_NODE_PATH_PUID")!=null) rowAppNodePathPuid = resultRowHash.get("APP_NODE_PATH_PUID").toString();
		
		String variant = null;
		if(rowOptionCond!=null && rowOptionCond.trim().length()>0){
			variant = (String) SDVBOPUtilities.getVariant(rowOptionCond).get("printDescriptions");
		}else{
			variant = this.noVariantString;
		}
		
		boolean isInOptionCondition = false;
		String optionSearchKey = this.searchConditionMap.get("bl_occ_mvl_condition");
		if(optionSearchKey!=null && optionSearchKey.trim().length()>0){
			int index = variant.indexOf(optionSearchKey.trim());
			if(index>0){
				isInOptionCondition = true;
			}
			
			if(isInOptionCondition==false){
				return displayRowData;
			}
		}
		
		TCComponentBOMLine operationBOMLine = null;
		if(window!=null){
			TCComponentBOMLine[] finded = null;
			try {
				finded = (TCComponentBOMLine[])window.findAppearance(rowAppNodePathPuid.trim());

				if(finded!=null){
					for (int j = 0; j < finded.length; j++) {
						if(finded[j]!=null){
							operationBOMLine = finded[j];
							if(operationBOMLine!=null){
								break;
							}
						}
					}
				}
			} catch (TCException e) {
				e.printStackTrace();
			}
		}
		
		if(operationBOMLine==null){
			return displayRowData;
		}
		
		if(displayRowData==null){
			displayRowData = new HashMap<String, Object>();
		}
		
		displayRowData.put("line_code", rowLineCode);
		//displayRowData.put("line_rev", );
		displayRowData.put("station_code", rowStationCode);
		//displayRowData.put("station_rev", );
		displayRowData.put("item_id", rowOperationId);
		displayRowData.put("item_revision_id", rowOperationRev);
		displayRowData.put("m7_KOR_NAME", rowOperationName);
		displayRowData.put("bl_occ_mvl_condition", variant);
		displayRowData.put("m7_MECO_NO", rowMecoId);
		displayRowData.put("date_released", rowOpReleaseDate);
		displayRowData.put("owning_user", rowMecoOwnerName);
		displayRowData.put("m7_ENG_NAME", rowEnglishName);

		String uid = null;
		try {
			if(operationBOMLine.getItemRevision()!=null){
				uid = operationBOMLine.getItemRevision().getUid();
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
		displayRowData.put("UID", uid);
		displayRowData.put("OPERATION_BOPLINE", operationBOMLine);
		
		//System.out.println("operationBOMLine = "+operationBOMLine);
		
		return displayRowData;
	}
	

	
	
}
