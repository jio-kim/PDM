package com.ssangyong.soa.service;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import com.ssangyong.common.remote.DataSet;
import com.ssangyong.soa.biz.Session;
import com.ssangyong.soa.biz.TcDatasetUtil;
import com.ssangyong.soa.biz.TcFileUtil;
import com.ssangyong.soa.biz.TcItemUtil;
import com.ssangyong.soa.common.constants.BomLineProp;
import com.ssangyong.soa.common.constants.ItemProp;
import com.ssangyong.soa.common.constants.PropertyConstant;
import com.ssangyong.soa.common.constants.RevProp;
import com.ssangyong.soa.common.constants.TcConstants;
import com.ssangyong.soa.common.util.LogUtil;
import com.ssangyong.soa.dao.MasterListDao;
import com.ssangyong.soa.ospec.OSpec;
import com.ssangyong.soa.ospec.OpTrim;
import com.ssangyong.soa.ospec.OpUtil;
import com.ssangyong.soa.ospec.OpValueName;
import com.ssangyong.soa.ospec.Option;
import com.ssangyong.soa.ospec.StoredOptionSet;
import com.ssangyong.soa.tcservice.TcQueryService;
import com.ssangyong.soa.tcservice.TcStructureManagementService;
import com.ssangyong.soa.tcservice.TcStructureService;
import com.teamcenter.services.strong.cad._2007_01.StructureManagement.CreateBOMWindowsResponse;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.Property;
import com.teamcenter.soa.client.model.strong.BOMLine;
import com.teamcenter.soa.client.model.strong.BOMWindow;
import com.teamcenter.soa.client.model.strong.Dataset;
import com.teamcenter.soa.client.model.strong.Group;
import com.teamcenter.soa.client.model.strong.ImanFile;
import com.teamcenter.soa.client.model.strong.Item;
import com.teamcenter.soa.client.model.strong.ItemRevision;
import com.teamcenter.soa.client.model.strong.User;

/**
 * [20160830][ymjang] BOM Load 처리 로직 서버 이관
 * [SR171227-049][LJG] N,M,C,D에서 M을 -> M1,M2로 세분화
 */
public class MasterListService {
	
    private TcStructureManagementService strManagementService;
    private TcStructureService strService;
    private TcQueryService queryService;   
    private TcItemUtil tcItemUtil;
	private Session tcSession;
    private TcDatasetUtil datasetUtil;
    private TcFileUtil fileUtil;
    private TcLOVService lovService ;
    private TcLoginService tcLoginService;
    private ScriptEngineManager manager = new ScriptEngineManager();
    private ScriptEngine engine = manager.getEngineByName("js");
    
    private HashMap<String, String> systemCodeMap = null;
    private HashMap<String, OpValueName> wtMap = null;
    private HashMap<String, OpValueName> tmMap = null;
    private StringBuffer log = new StringBuffer();
    private String funcId = null;
	private HashMap<String, HashMap<String, ArrayList<BOMLine>>> bomlineMap = null;
	private HashMap<String, ArrayList<String>> bomlineOccThreadMap = null;
	private HashMap<String, ArrayList<String>> bomlinePuidMap = null;
//    private HashMap<String, String> bomlineCntMap = null;
//    private HashMap<String, String> bomlinePuidCntMap = null;
    private String isIncludeBOMLine = null;
    private ArrayList<String> keyList = null;
    private HashMap<String, ArrayList<String>> childMap = null;
    
	public Object loadChildPropMap(DataSet ds) throws Exception {
		
		MasterListDao dao = new MasterListDao();
		
		Long from = (Long) System.currentTimeMillis ();
        
		/* ================================================================================
		 * Param 정보 Read
		 * ================================================================================ */
		funcId = ds.get("funcId").toString();
		String fmpId = ds.get("fmpId").toString();
		String fmpRevId = ds.get("fmpRevId").toString();
		String ospecId = ds.get("ospecId").toString();
		String ospecRevId = ds.get("ospecRevId").toString();
		keyList = new ArrayList<String>();
		//keyList = (ArrayList<String>) ds.get("keyList"); // system row key
		bomlineMap = new HashMap<String, HashMap<String, ArrayList<BOMLine>>>();
		bomlineOccThreadMap = new HashMap<String, ArrayList<String>>();
		bomlinePuidMap = new HashMap<String, ArrayList<String>>();
		//bomlineCntMap = (HashMap<String, String>) ds.get("bomlineCntMap"); // line cnt map
		//bomlinePuidCntMap = (HashMap<String, String>) ds.get("bomlinePuidCntMap"); // puid cnt map
		isIncludeBOMLine = ds.get("isIncludeBOMLine").toString(); // Working or Released
		String isWorking = ds.get("isWorking").toString();
		String userId = ds.get("userId").toString();
		String password = ds.get("password").toString();
		
		/* ================================================================================
		 * Service 초기화
		 * ================================================================================ */
        tcLoginService = new TcLoginService();
        tcSession = tcLoginService.getTcSession(userId, password);
        
        tcItemUtil = new TcItemUtil(tcSession);
        datasetUtil = new TcDatasetUtil(tcSession);
        fileUtil = new TcFileUtil(tcSession);
        strManagementService = new TcStructureManagementService(tcSession);
        strService = new TcStructureService(tcSession);
		lovService = new TcLOVService();
		queryService = new TcQueryService(tcSession);
		
		/* ================================================================================
		 * FMP Reivison Search
		 * ================================================================================ */
		// FMP Revision Search
		ModelObject[] fmpObjects  = queryService.searchTcObject("Item Revision...", new String[]{"Item ID", "Revision"}, 
                                                                                    new String[]{fmpId, fmpRevId}, 
																				    new String[]{PropertyConstant.ATTR_NAME_ITEMID, PropertyConstant.ATTR_NAME_ITEMREVID});

		if (fmpObjects == null || fmpObjects.length == 0) {
			System.out.println(fmpId + " - " + fmpRevId + " FMP Not Found.");
			return null;
		}
		
		ItemRevision fmpRevision = (ItemRevision) fmpObjects[0];
		
		/* ================================================================================
		 * OSpec Reivison Search
		 * ================================================================================ */
		ModelObject[] ospecObjects  = queryService.searchTcObject("Item Revision...", new String[]{"Item ID", "Revision"}, 
                                                                                      new String[]{ospecId, ospecRevId}, 
																					  new String[]{PropertyConstant.ATTR_NAME_ITEMID, PropertyConstant.ATTR_NAME_ITEMREVID, TcConstants.RELATION_REFERENCES});

		if (ospecObjects == null || ospecObjects.length == 0) {
			System.out.println(ospecId + " - " + ospecRevId + " OSpec Not Found.");
			return null;
		}
		
		ModelObject ospecRev = ospecObjects[0];
		
		/* ================================================================================
		 * OSpec Info Read
		 * ================================================================================ */
        OSpec oSpec = getOspec(ospecRev);
        HashMap<String, StoredOptionSet> storedOptionSetMap = getStoredOptionSets(oSpec);

        // Wheel Type Option 및 Transmission Option 정보를 분류한다.
        tmMap = new HashMap<String, OpValueName>();
        wtMap = new HashMap<String, OpValueName>();
        childMap = new HashMap<String, ArrayList<String>>();
        for (OpValueName opValueName : oSpec.getOpNameList())
        {
            // TransMission
            if (opValueName.getCategory().equals("E00"))
            {
                tmMap.put(opValueName.getOption(), opValueName);
            }
            // Wheel Type
            if (opValueName.getCategory().equals("E10"))
            {
                wtMap.put(opValueName.getOption(), opValueName);
            }
        }
        
		/* ================================================================================
		 * System Code Lov Read
		 * ================================================================================ */
        systemCodeMap = new HashMap<String, String>();
        List<HashMap<String, Object>> systemCodeLovList  = lovService.getLOVDescList("S7_SYSTEM_CODE");
        for (HashMap<String, Object> map : systemCodeLovList)
	      {
	          String key = map.get("VALUE") == null ? "" : map.get("VALUE").toString();
	          String keyValue = map.get("DESCRIPTION") == null ? "" : map.get("DESCRIPTION").toString();
	
	          systemCodeMap.put(key, keyValue);
	      }
        
		/* ================================================================================
		 * BOMLine Expand
		 * ================================================================================ */
        // BOMWindow Creation 
    	CreateBOMWindowsResponse res = null;
        if (isWorking.equals("Y")) {
        	res = strManagementService.createTopLineBOMWindow(fmpRevision, strManagementService.getRevisionRule(TcConstants.BOMVIEW_LATEST_WORKING), null);
        } else {
        	res = strManagementService.createTopLineBOMWindow(fmpRevision, strManagementService.getRevisionRule(TcConstants.BOMVIEW_LATEST_RELEASED), null);
        }
        
        HashMap<String, HashMap<String, Object>> propMap = new HashMap<String, HashMap<String, Object>> ();
        BOMWindow window = null;
        BOMLine fmpLine = null;
        try
        {
            window = res.output[0].bomWindow;
            fmpLine = res.output[0].bomLine;
            
            tcItemUtil.getProperties(new ModelObject[]{fmpLine}, new String[]{PropertyConstant.ATTR_NAME_ITEMID, 
            		                                                          PropertyConstant.ATTR_NAME_BL_ITEM_REVISION_ID, 
            		                                                          PropertyConstant.ATTR_NAME_BL_CHILD_LINES,
            		                                                          PropertyConstant.ATTR_NAME_BL_ITEM_REVISION,
            		                                                          PropertyConstant.ATTR_NAME_BL_PARENT,
            		                                                          PropertyConstant.ATTR_NAME_BL_CONDITION});
        	if (fmpLine.get_bl_revision() != null && fmpLine.get_bl_child_lines().length > 0)
        	{
        		// Bom Expand
        		expandBom(dao, window, fmpLine, propMap, oSpec, storedOptionSetMap);
        	}
        }
        catch (Exception ex) {
        	ex.printStackTrace();
        	LogUtil.error(ex.getMessage());
            throw ex;
        } finally {
        	
        	//LogUtil.fout(null, this.getClass().getName(), "loadChildPropMap", "", "Completed!");       	
            if (window != null)
            	strManagementService.closeBOMWindow(window);
            
            if (tcSession != null)
            	tcSession.logout();
            
        }
        
        HashMap<String, Object> rtnMap = new HashMap<String, Object> ();
        rtnMap.put("keyList", keyList);
        rtnMap.put("propMap", propMap);
        //rtnMap.put("bomlineMap", bomlineMap);
        rtnMap.put("bomlineOccThreadMap", bomlineOccThreadMap);
        rtnMap.put("bomlinePuidMap", bomlinePuidMap);
        
		Long to = (Long) System.currentTimeMillis ();
		System.out.println( ( to - from ) / 1000.0 );
		return rtnMap;
	}
	
	public void expandBom(MasterListDao dao, BOMWindow window, BOMLine parentLine, 
			              HashMap<String, HashMap<String, Object>> propMap, OSpec oSpec, 
			              HashMap<String, StoredOptionSet> storedOptionSetMap) throws Exception {
		
		ItemRevision parentRevision = (ItemRevision) parentLine.get_bl_revision();
        tcItemUtil.getProperties(new ModelObject[]{parentRevision}, new String[] {PropertyConstant.ATTR_NAME_ITEMID, 
																				  PropertyConstant.ATTR_NAME_DISPLAYPARTNO, 
																				  PropertyConstant.ATTR_NAME_ITEMTYPE, 
																				  PropertyConstant.ATTR_NAME_ITEMREVID, 
																				  PropertyConstant.ATTR_NAME_ITEMNAME});
        // PreFuncMasterRevision 과 PreVehPartRevision 만 전개함.
		String parentItemType = parentRevision.getPropertyObject(PropertyConstant.ATTR_NAME_ITEMTYPE).getStringValue();
		if( !parentItemType.equals(TcConstants.S7_PREVEHICLEPARTREVISIONTYPE) &&
			!parentItemType.equals(TcConstants.S7_PREFUNCMASTERREVISIONTYPE) ) {
			return;
		}
		
        // Pack 을 수행함.
        //BOMLine[] bomLines = Arrays.copyOf(childlines, childlines.length, BOMLine[].class);
        strService.getService().packOrUnpack(new BOMLine[]{parentLine}, 2); // 0:pack the lines 1:unpack the lines 2:pack all lines 3:unpack all lines

        ModelObject[] childlines = parentLine.get_bl_child_lines();
        tcItemUtil.getProperties(childlines, BomLineProp.getPropNames());
        
		// SEQ No 순으로 정렬
    	ArrayList<BOMLine> childLinelist = new ArrayList<BOMLine>();
    	for (ModelObject childline : childlines) {
    		childLinelist.add((BOMLine)childline);
		}
		Collections.sort(childLinelist, new Comparator<BOMLine>() {
			@Override
			public int compare(BOMLine childLine1, BOMLine childLine2)
			{
				try {
					String seqNo1 = childLine1.getPropertyObject(PropertyConstant.ATTR_NAME_BL_SEQUENCE_NO).getStringValue();
					String seqNo2 = childLine2.getPropertyObject(PropertyConstant.ATTR_NAME_BL_SEQUENCE_NO).getStringValue();
					return seqNo1.compareTo(seqNo2);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return 0;
			}
		});
        
    	BOMLine childBomline = null;
    	ModelObject[] packedLines = null;
    	HashMap<String, Object> paramMap = new HashMap<String, Object>();
    	boolean isNewLineInStructure = false;
    	for (int i = 0; i < childLinelist.size(); i++) {
    		
    		childBomline = childLinelist.get(i);

            strService.getService().packOrUnpack(new BOMLine[] {childBomline}, 0); // 0:pack the lines 1:unpack the lines 2:pack all lines 3:unpack all lines
        	
			Item childItem = (Item) childBomline.get_bl_item();
            tcItemUtil.getProperties(new ModelObject[]{childItem}, ItemProp.getPropNames());
            
            ItemRevision childRev = (ItemRevision) childBomline.get_bl_revision();
            tcItemUtil.getProperties(new ModelObject[]{childRev}, RevProp.getPropNames());
        	
    		/* **************************************
    		 * BOMLine Property
    		 * ************************************** */
    		// System Row Key
            String systemRowKey = childBomline.getPropertyObject(PropertyConstant.ATTR_NAME_BL_SYSTEM_ROW_KEY).getStringValue();
    		HashMap<String, Object> map = propMap.get(systemRowKey);
    		if( map == null){
    			map = new HashMap<String, Object>();
    			propMap.put(systemRowKey, map);
    		}
            
			// Structure Manager에서 추가한 BOMLine은 시스템 키가 존재하지 않으므로 생성함.
            isNewLineInStructure = false; 
    		if( systemRowKey == null || systemRowKey.equals("")){
//    			systemRowKey = dao.getSysGuid();
//    			paramMap.clear();
//				paramMap.put(PropertyConstant.ATTR_NAME_BL_SYSTEM_ROW_KEY, systemRowKey);
//                tcItemUtil.setAttributes(childBomline, paramMap);
//                strManagementService.saveBOMWindow(window);
    			isNewLineInStructure = true;
    		} 
            
    		// OccThread
    		String occThread = childBomline.getPropertyObject(PropertyConstant.ATTR_NAME_BL_OCC_THREAD).getStringValue();
    		String occUid = childBomline.getPropertyObject(PropertyConstant.ATTR_NAME_BL_OCC_UID).getStringValue();
    		
    		// Qty    		
			if( keyList.contains(systemRowKey)) {
    			
    			// Latest Working 일 경우,
    			// Working BOM Line 추가
    			// Working BOM은 변경이 되며, BOM Line 객체를 가지고 있다.
    			if (isIncludeBOMLine.equals("Y")) {
    				HashMap<String, ArrayList<BOMLine>> tmpMap = bomlineMap.get(systemRowKey);
    				ArrayList<String> tmpOccThreadList = bomlineOccThreadMap.get(systemRowKey);
    				// Parent Move에 의해 하위 BOM Line이 변경 될 수 있으므로,
    				// 기존에 있던 정보를 Update.
					if (childBomline.get_bl_is_packed()) {
						packedLines = childBomline.get_bl_packed_lines();
						packedLines = new ModelObject[childBomline.get_bl_packed_lines().length + 1];
						packedLines[0] = childBomline;
						System.arraycopy(childBomline.get_bl_packed_lines(), 0, packedLines, 1, childBomline.get_bl_packed_lines().length);
					} else {
						packedLines = new ModelObject[]{childBomline};
					}
    				    				
					tcItemUtil.getProperties(packedLines, BomLineProp.getPropNames());
					for ( ModelObject packedLine : packedLines ) {
						BOMLine packLine = (BOMLine) packedLine;
						occThread = packLine.getPropertyObject(PropertyConstant.ATTR_NAME_BL_OCC_THREAD).getStringValue();
						occUid = packLine.getPropertyObject(PropertyConstant.ATTR_NAME_BL_OCC_UID).getStringValue();
						ArrayList<BOMLine> lines = tmpMap.get(occThread);
						if ( lines == null) {
							lines = new ArrayList<BOMLine>();
							lines.add(packLine);
							tmpMap.put(occThread, lines);
						} else {
							if( !lines.contains(packLine)){
								lines.add(packLine);
								tmpMap.put(occThread, lines);
							}
						}
						if( !tmpOccThreadList.contains(occThread)){
							tmpOccThreadList.add(occThread);
						}
					}
    				
	    			map.put("REPRESENTATIVE_QUANTITY", tmpMap.size() + "");
	    			
    			// Release BOM은 변경이 없으며, 수량 체크를 위해 BOM Line Uid만 가지고 있다.
    			} else {
    				
    				ArrayList<String> list = bomlinePuidMap.get(systemRowKey);
    				ArrayList<String> tmpOccThreadList = bomlineOccThreadMap.get(systemRowKey);
    				
					if (childBomline.get_bl_is_packed()) {
						packedLines = childBomline.get_bl_packed_lines();
						packedLines = new ModelObject[childBomline.get_bl_packed_lines().length + 1];
						packedLines[0] = childBomline;
						System.arraycopy(childBomline.get_bl_packed_lines(), 0, packedLines, 1, childBomline.get_bl_packed_lines().length);
					} else {
						packedLines = new ModelObject[]{childBomline};
					}
    				
					tcItemUtil.getProperties(packedLines, BomLineProp.getPropNames());
					for ( ModelObject packedLine : packedLines ) {
    					BOMLine packLine = (BOMLine) packedLine;
						occThread = packLine.getPropertyObject(PropertyConstant.ATTR_NAME_BL_OCC_THREAD).getStringValue();
						occUid = packLine.getPropertyObject(PropertyConstant.ATTR_NAME_BL_OCC_UID).getStringValue();
						if( !list.contains(occUid)){
							list.add(occUid);
						}
						if( !tmpOccThreadList.contains(occThread)){
							tmpOccThreadList.add(occThread);
						}
					}

	    			map.put("REPRESENTATIVE_QUANTITY", list.size() + "");
    			}
    		} else {
    			
    			if (isIncludeBOMLine.equals("Y")) {
    				
    				ArrayList<BOMLine> lines = new ArrayList<BOMLine>();
    				ArrayList<String> tmpOccThreadList = new ArrayList<String>();
    				HashMap<String, ArrayList<BOMLine>> tmpMap = new HashMap<String, ArrayList<BOMLine>>();

					if (childBomline.get_bl_is_packed()) {
						packedLines = childBomline.get_bl_packed_lines();
						packedLines = new ModelObject[childBomline.get_bl_packed_lines().length + 1];
						packedLines[0] = childBomline;
						System.arraycopy(childBomline.get_bl_packed_lines(), 0, packedLines, 1, childBomline.get_bl_packed_lines().length);
					} else {
						packedLines = new ModelObject[]{childBomline};
					}
    				
					tcItemUtil.getProperties(packedLines, BomLineProp.getPropNames());
					for ( ModelObject packedLine : packedLines ) {
    					BOMLine packLine = (BOMLine) packedLine;
						occThread = packLine.getPropertyObject(PropertyConstant.ATTR_NAME_BL_OCC_THREAD).getStringValue();
						occUid = packLine.getPropertyObject(PropertyConstant.ATTR_NAME_BL_OCC_UID).getStringValue();
						lines = tmpMap.get(occThread);
						if ( lines == null){
							lines = new ArrayList<BOMLine>();
							lines.add(packLine);
							tmpMap.put(occThread, lines);
						} else {
							if( !lines.contains(packLine)){
								lines.add(packLine);
								tmpMap.put(occThread, lines);
							}
						}
						if( !tmpOccThreadList.contains(occThread)){
							tmpOccThreadList.add(occThread);
						}
					}
    				    				
    				bomlineMap.put(systemRowKey, tmpMap);
    				bomlineOccThreadMap.put(systemRowKey, tmpOccThreadList);
    			} else {
    				
    				ArrayList<String> list = new ArrayList<String>();
    				ArrayList<String> tmpOccThreadList = new ArrayList<String>();

					if (childBomline.get_bl_is_packed()) {
						packedLines = childBomline.get_bl_packed_lines();
						packedLines = new ModelObject[childBomline.get_bl_packed_lines().length + 1];
						packedLines[0] = childBomline;
						System.arraycopy(childBomline.get_bl_packed_lines(), 0, packedLines, 1, childBomline.get_bl_packed_lines().length);
					} else {
						packedLines = new ModelObject[]{childBomline};
					}
    				
					tcItemUtil.getProperties(packedLines, BomLineProp.getPropNames());
					for ( ModelObject packedLine : packedLines ) {
    					BOMLine packLine = (BOMLine) packedLine;
						occThread = packLine.getPropertyObject(PropertyConstant.ATTR_NAME_BL_OCC_THREAD).getStringValue();
						occUid = packLine.getPropertyObject(PropertyConstant.ATTR_NAME_BL_OCC_UID).getStringValue();
						if( !list.contains(occUid)){
							list.add(occUid);
						}
						if( !tmpOccThreadList.contains(occThread)){
							tmpOccThreadList.add(occThread);
						}
					}

    				bomlinePuidMap.put(systemRowKey, list);
    				bomlineOccThreadMap.put(systemRowKey, tmpOccThreadList);
    			}
    			
        		keyList.add(systemRowKey);
    		}
    		
    		HashMap<String, ArrayList<BOMLine>> bomlines = bomlineMap.get(systemRowKey);
    		ArrayList<String> bomLinePuids = bomlinePuidMap.get(systemRowKey);
    		
    		/* **************************************
    		 * BOMLine Property
    		 * ************************************** */
    		// Change Type CD
    		map.put(PropertyConstant.ATTR_NAME_BL_SYSTEM_ROW_KEY, systemRowKey);

    		// Change Type CD
            String chgCd = childBomline.getPropertyObject(PropertyConstant.ATTR_NAME_BL_CHG_CD).getStringValue();
    		map.put(PropertyConstant.ATTR_NAME_BL_CHG_CD, chgCd);
    		
    		// Level
    		String lvlM = childBomline.getPropertyObject(PropertyConstant.ATTR_NAME_BL_LEV_M).getStringValue();
    		map.put(PropertyConstant.ATTR_NAME_BL_LEV_M, lvlM);

    		// Seq No
    		String seqNo = childBomline.getPropertyObject(PropertyConstant.ATTR_NAME_BL_SEQUENCE_NO).getStringValue();
    		map.put(PropertyConstant.ATTR_NAME_BL_SEQUENCE_NO, seqNo);
    		
    		// Parent No
    		String parentId = parentRevision.getPropertyObject(PropertyConstant.ATTR_NAME_ITEMID).getStringValue();
    		map.put("PARENT_NO", parentId);
    		
    		// Qty
    		if (isIncludeBOMLine.equals("Y")) {
    			map.put("REPRESENTATIVE_QUANTITY", bomlines.size() + "");
    		}else{
    			map.put("REPRESENTATIVE_QUANTITY", bomLinePuids.size() + "");
    		}
    		
    		// EA는 Double형의 Quantity가 올 수 없다.
    		boolean isIntegerQty = false;
    		String qty = childBomline.getPropertyObject(PropertyConstant.ATTR_NAME_BL_QUANTITY).getStringValue(); 
    		try{
    			double dNum = Double.parseDouble(qty);
    			int iNum = (int)dNum;
    			if( dNum == iNum){
    				qty = "" + iNum;
    				isIntegerQty = true;
    			}else{
    				isIntegerQty = false;
    			}
    		}catch(NumberFormatException nfe){
    			isIntegerQty = false;
    			map.put("REPRESENTATIVE_QUANTITY", qty);
    		}
    		
    		// Spec Description
    		String specDesc = childBomline.getPropertyObject(PropertyConstant.ATTR_NAME_BL_SPEC_DESC).getStringValue(); 
    		map.put(PropertyConstant.ATTR_NAME_BL_SPEC_DESC, specDesc);
    		
    		// Opion 
    		ArrayList<OpTrim> trimList = oSpec.getTrimList();
    		// FMP 하위 1 Level Parent Parent 의 Condition 조합을 구한다. 
    		String parentCondition = getConditionSet(window, parentLine, parentRevision, null);
    		String conditionStr = childBomline.getPropertyObject(PropertyConstant.ATTR_NAME_BL_CONDITION).getStringValue(); 
    		if( (parentCondition != null && !parentCondition.equals("")) || (conditionStr != null && !conditionStr.equals(""))){
    			String simpleCondition = "";
    			
    			if(conditionStr != null && !conditionStr.equals("")){
    				simpleCondition = convertToSimpleCondition(conditionStr);
    				String tCondition = removeTrimOptionValue(simpleCondition);
    				map.put("SPEC_DISP", tCondition);
    			}else{
    				map.put("SPEC_DISP", "");
    			}
    			
    			if( simpleCondition != null && !simpleCondition.equals("")){
    				map.put("SPEC", simpleCondition);
    			}else{
    				map.put("SPEC", "");
    			}
    			
    			if( parentCondition != null && !parentCondition.equals("")){
    				simpleCondition = parentCondition + (simpleCondition.equals("") ? "" : " and (" + simpleCondition + ")");
    			}
    			map.put("COMPLEX_SPEC", simpleCondition);
    			
				int totUsage = 0;
				if( storedOptionSetMap != null){
					
					for( OpTrim trim : trimList){
						String sosStdName = trim.getTrim() + "_STD";
						String sosOptName = trim.getTrim() + "_OPT";
						StoredOptionSet sosStd = storedOptionSetMap.get(sosStdName);
						StoredOptionSet sosOpt = storedOptionSetMap.get(sosOptName);
						
						if( sosStd == null || sosOpt == null){
							map.put(trim.getTrim(), "");
							continue;
						}
						
						if( sosStd.isInclude(engine, simpleCondition)){
							
							if( isIntegerQty){
								if(isIncludeBOMLine.equals("Y")) {
									map.put(trim.getTrim(), bomlines.size());
									System.out.println("systemRowKey : " + systemRowKey);
									totUsage += bomlines.size() ;
								}else{
				        			map.put(trim.getTrim(), bomLinePuids.size());
									totUsage += bomLinePuids.size() ;
								}
							} else {
								map.put(trim.getTrim(), qty);
							}
							
						}else if( sosOpt.isInclude(engine, simpleCondition)){
							if( isIntegerQty){
								if(isIncludeBOMLine.equals("Y")) {
									map.put(trim.getTrim(), bomlines.size());
									totUsage += bomlines.size() ;
								}else{
				        			map.put(trim.getTrim(), bomLinePuids.size());
									totUsage += bomLinePuids.size() ;
								}
							}else{
								map.put(trim.getTrim(), qty);
							}
						}else{
							map.put(trim.getTrim(), "");
						}
					}
					map.put("TOT_USAGE", "" + totUsage);
				}
    			
    		}else{
    			map.put("SPEC", "");
    			map.put("SPEC_DISP", "");
    			map.put("COMPLEX_SPEC", "");
    			for( OpTrim trim : trimList){
    				map.put(trim.getTrim(), map.get("REPRESENTATIVE_QUANTITY")); 
    			}
    		}
    		
    		// Module
    		String moduleCode = childBomline.getPropertyObject(PropertyConstant.ATTR_NAME_BL_MODULE_CODE).getStringValue();
    		map.put(PropertyConstant.ATTR_NAME_BL_MODULE_CODE, moduleCode);
    		
    		// Supply Mode
    		String supplyMode = childBomline.getPropertyObject(PropertyConstant.ATTR_NAME_BL_SUPPLY_MODE).getStringValue();
    		map.put(PropertyConstant.ATTR_NAME_BL_SUPPLY_MODE, supplyMode);
    		
    		// Alter Part
    		String alterPart = childBomline.getPropertyObject(PropertyConstant.ATTR_NAME_BL_ALTER_PART).getStringValue();
    		map.put(PropertyConstant.ATTR_NAME_BL_ALTER_PART, alterPart);
    		
    		// Level(by System)
    		map.put("LEV_A", "" + getLevel(childBomline, 1));
    		
    		// Req Opt.
    		String reqDept = childBomline.getPropertyObject(PropertyConstant.ATTR_NAME_BL_REQ_OPT).getStringValue();
    		map.put(PropertyConstant.ATTR_NAME_BL_REQ_OPT, reqDept);
    		
			// PRT-TEST
			String neededQty = childBomline.getPropertyObject(PropertyConstant.ATTR_NAME_BL_DVP_NEEDED_QTY).getStringValue();
			map.put(PropertyConstant.ATTR_NAME_BL_DVP_NEEDED_QTY, neededQty);
			
			// Dpv Sample Use
			String dpvUse = childBomline.getPropertyObject(PropertyConstant.ATTR_NAME_BL_DVP_USE).getStringValue();
			map.put(PropertyConstant.ATTR_NAME_BL_DVP_USE, dpvUse);

			// Dpv Sample Req Dept
			String dpvReqDept = childBomline.getPropertyObject(PropertyConstant.ATTR_NAME_BL_DVP_REQ_DEPT).getStringValue();
			map.put(PropertyConstant.ATTR_NAME_BL_DVP_REQ_DEPT, dpvReqDept);
			
			// Engineering Dept 
    		String engDept = childBomline.getPropertyObject(PropertyConstant.ATTR_NAME_BL_ENG_DEPT_NM).getStringValue();
    		map.put(PropertyConstant.ATTR_NAME_BL_ENG_DEPT_NM, engDept);
			
			// Engineer
    		String engineer = childBomline.getPropertyObject(PropertyConstant.ATTR_NAME_BL_ENG_RESPONSIBLITY).getStringValue();
    		map.put(PropertyConstant.ATTR_NAME_BL_ENG_RESPONSIBLITY, engineer);
    		
    		/* **************************************
    		 * Item Property
    		 * ************************************** */
    		// Item Id
    		String childItemId = childItem.getPropertyObject(PropertyConstant.ATTR_NAME_ITEMID).getStringValue();
    		map.put(PropertyConstant.ATTR_NAME_ITEMID, childItemId);
    		
    		// UOM
    		String uomTags = childItem.getPropertyObject(PropertyConstant.ATTR_NAME_UOMTAG).getDisplayableValue();
    		map.put(PropertyConstant.ATTR_NAME_UOMTAG, uomTags);
    		
//        		if (childItemId.equals("9285406000")) {
//        			System.out.println("aaaaaaaaaaaaaaaaaaaa");
//        		}
    		
    		/* **************************************
    		 * Item Revision Property
    		 * ************************************** */
    		String childRevType = childRev.getPropertyObject(PropertyConstant.ATTR_NAME_ITEMTYPE).getStringValue();        		
    		if( childRevType.equals(TcConstants.S7_PREVEHICLEPARTREVISIONTYPE)) {
    			setPreVehiclePartProp(dao, childRev, map, systemRowKey, chgCd);
    		} else if ( childRevType.equals(TcConstants.S7_VEH_PART_REVISION)) {
    			setVehiclePartProp(dao, childRev, map, systemRowKey, chgCd);
    		} else {
    			setStdPartProp(dao, childRev, map, systemRowKey, chgCd);
    		}
    		
            // 하위 전개 (재귀호출)
            if (childBomline.get_bl_revision() != null && childBomline.get_bl_child_lines().length > 0) {
            	expandBom(dao, window, childBomline, propMap, oSpec, storedOptionSetMap);
            }               
        }
	}
	
	/**
	 * PreVehicle Part Property Set
	 * @param dao
	 * @param childRev
	 * @param map
	 * @param systemRowKey
	 * @param chgCd
	 * @throws Exception
	 */
	private void setPreVehiclePartProp(MasterListDao dao, ItemRevision childRev, HashMap<String, Object> map, String systemRowKey, String chgCd) throws Exception {
	 
		// Item Revision Type
		String childRevType = childRev.getPropertyObject(PropertyConstant.ATTR_NAME_ITEMTYPE).getStringValue();
		
		// Change Type
        String chgType = childRev.getPropertyObject(PropertyConstant.ATTR_NAME_CHG_TYPE_NM).getStringValue();
        map.put(PropertyConstant.ATTR_NAME_CHG_TYPE_NM, chgType);
        
		// Project Code
		String projCode = "";
		if( chgType.contains("M")){
			projCode = childRev.getPropertyObject(PropertyConstant.ATTR_NAME_PRD_PROJ_CODE).getStringValue();
		}else{
			projCode = childRev.getPropertyObject(PropertyConstant.ATTR_NAME_PROJCODE).getStringValue();
		}
		map.put(PropertyConstant.ATTR_NAME_PROJCODE, projCode);
		
		// System Code
		String systemCode = childRev.getPropertyObject(PropertyConstant.ATTR_NAME_BUDGETCODE).getStringValue();
		map.put(PropertyConstant.ATTR_NAME_BUDGETCODE, systemCode);
		
		// DCS
		if( projCode != null && !projCode.equals("") && systemCode != null && !systemCode.equals("")){
			
			DataSet ds = new DataSet();
			ds.put("PS7_PROJECT_CODE", projCode);
			ds.put("PS7_SYSTEM_CODE", systemCode);
			
			List<HashMap<String, Object>> dcList = dao.getDCSList(ds);
			if (dcList != null && dcList.size() > 0) {
				HashMap<String, Object> rtnMap = (HashMap<String, Object>) dcList.get(0);
				map.put("DCS_NO", rtnMap.get("DOC_NO") == null ? "" : rtnMap.get("DOC_NO").toString());
				map.put("DCS_DATE", rtnMap.get("RELEASE_DATE") == null ? "" : rtnMap.get("RELEASE_DATE").toString());
			} else {
				map.put("DCS_NO", "");
				map.put("DCS_DATE", "");
			}
		}
		
		// System
		map.put("SYSTEM_NAME", getSystemName(systemCode));
		map.put("FUNC", funcId.substring(0, 4));
		
		// Old Part No
		String oldPartNo = childRev.getPropertyObject(PropertyConstant.ATTR_NAME_OLD_PART_NO).getStringValue();
		map.put(PropertyConstant.ATTR_NAME_OLD_PART_NO, oldPartNo);
		
		// Display Part No
		String displayPartNo = childRev.getPropertyObject(PropertyConstant.ATTR_NAME_DISPLAYPARTNO).getStringValue();
		map.put(PropertyConstant.ATTR_NAME_DISPLAYPARTNO, displayPartNo);
		
		// Part Name
		String partName = childRev.getPropertyObject(PropertyConstant.ATTR_NAME_ITEMNAME).getStringValue();
		map.put(PropertyConstant.ATTR_NAME_ITEMNAME, partName);
		
		// Contents
		String contents = childRev.getPropertyObject(PropertyConstant.ATTR_NAME_CONTENTS).getStringValue();
		map.put(PropertyConstant.ATTR_NAME_CONTENTS, contents);
		
		// Parent 하위의 Child List 생성 
		if( childRevType.equals(TcConstants.S7_PREVEHICLEPARTREVISIONTYPE)){
			ArrayList<String> childList = childMap.get(map.get("PARENT_NO"));
			if( childList == null){
				childList = new ArrayList<String>();
				childList.add(map.get(PropertyConstant.ATTR_NAME_ITEMID).toString());
				childMap.put(map.get("PARENT_NO").toString(), childList);
			}else{
				if( childList.contains(map.get(PropertyConstant.ATTR_NAME_ITEMID).toString())){
					//throw new Exception("Parent Part can not have more than two identical Child Part.");
				}else{
					childList.add(map.get(PropertyConstant.ATTR_NAME_ITEMID).toString());
				}
			}
		}
		
		// Change Desc
		String changeDesc = childRev.getPropertyObject(PropertyConstant.ATTR_NAME_CHANGE_DESCRIPTION).getStringValue();
		map.put(PropertyConstant.ATTR_NAME_CHANGE_DESCRIPTION, changeDesc);
		
		// Act/Est Weight
		String actWgt = childRev.getPropertyObject(PropertyConstant.ATTR_NAME_ACTWEIGHT).getDisplayableValue();
		String estWgt = childRev.getPropertyObject(PropertyConstant.ATTR_NAME_ESTWEIGHT).getDisplayableValue();
		if ("C".equals(chgCd)) {
			map.put(PropertyConstant.ATTR_NAME_ESTWEIGHT, actWgt);
		} else {
			map.put(PropertyConstant.ATTR_NAME_ESTWEIGHT, estWgt);
		}
		
		// Target Weight
		String tgtWgt = childRev.getPropertyObject(PropertyConstant.ATTR_NAME_TARGET_WEIGHT).getDisplayableValue();
		map.put(PropertyConstant.ATTR_NAME_TARGET_WEIGHT, tgtWgt);
		
		// DR
		String dr = childRev.getPropertyObject(PropertyConstant.ATTR_NAME_DR).getStringValue();
		map.put(PropertyConstant.ATTR_NAME_DR, dr);
		
		// Responsibility
		String box = childRev.getPropertyObject(PropertyConstant.ATTR_NAME_BOX).getStringValue();
		map.put(PropertyConstant.ATTR_NAME_BOX, box);
		
		ModelObject relObjs = tcItemUtil.getRelatedFromModelObjectValue(childRev.getUid(), PropertyConstant.ATTR_NAME_PRE_VEH_TYPE_REF);
		if (relObjs != null) {
			
			tcItemUtil.getProperties(new ModelObject[]{relObjs}, new String[] {PropertyConstant.ATTR_NAME_EST_COST_MATERIAL, 
													        				   PropertyConstant.ATTR_NAME_TARGET_COST_MATERIAL, 
													        				   PropertyConstant.ATTR_NAME_PRT_TOOLG_INVESTMENT, 
													        				   PropertyConstant.ATTR_NAME_PRD_TOOL_COST, 
													        				   PropertyConstant.ATTR_NAME_PRD_SERVICE_COST,
													        				   PropertyConstant.ATTR_NAME_PRD_SAMPLE_COST,
													        				   PropertyConstant.ATTR_NAME_TOTAL});
			
			// Est Material Cost
			String estCostMaterial = relObjs.getPropertyObject(PropertyConstant.ATTR_NAME_EST_COST_MATERIAL).getDisplayableValue();
			map.put(PropertyConstant.ATTR_NAME_EST_COST_MATERIAL, estCostMaterial);
			
			// Tgt Material Cost
			String tgtCostMaterial = relObjs.getPropertyObject(PropertyConstant.ATTR_NAME_TARGET_COST_MATERIAL).getDisplayableValue();
			map.put(PropertyConstant.ATTR_NAME_TARGET_COST_MATERIAL, tgtCostMaterial);
			
			// Prod Tool cost
			String prdToolCost = relObjs.getPropertyObject(PropertyConstant.ATTR_NAME_PRD_TOOL_COST).getDisplayableValue();
			map.put(PropertyConstant.ATTR_NAME_PRD_TOOL_COST, prdToolCost);
			
			// Prod Service Cost
			String serviceCost = relObjs.getPropertyObject(PropertyConstant.ATTR_NAME_PRD_SERVICE_COST).getDisplayableValue();
			map.put(PropertyConstant.ATTR_NAME_PRD_SERVICE_COST, serviceCost);
			
			// Prod Sampe Cost
			String sampleCost = relObjs.getPropertyObject(PropertyConstant.ATTR_NAME_PRD_SAMPLE_COST).getDisplayableValue();
			map.put(PropertyConstant.ATTR_NAME_PRD_SAMPLE_COST, sampleCost);
			
		} else {
			// Est Material Cost
			map.put(PropertyConstant.ATTR_NAME_EST_COST_MATERIAL, "");
			// Tgt Material Cost
			map.put(PropertyConstant.ATTR_NAME_TARGET_COST_MATERIAL, "");
			// Tool Investment
			map.put(PropertyConstant.ATTR_NAME_PRT_TOOLG_INVESTMENT, "");
			// Prod Tool cost
			map.put(PropertyConstant.ATTR_NAME_PRD_TOOL_COST, "");
			// Prod Service Cost
			map.put(PropertyConstant.ATTR_NAME_PRD_SERVICE_COST, "");
			// Prod Sampe Cost
			map.put(PropertyConstant.ATTR_NAME_PRD_SAMPLE_COST, "");
		}

		// Total Cost (미사용 컬럼)
		map.put(PropertyConstant.ATTR_NAME_TOTAL, "");
		
		// Tool Investment
		String toolInvestment = childRev.getPropertyObject(PropertyConstant.ATTR_NAME_PRT_TOOLG_INVESTMENT).getDisplayableValue();
		map.put(PropertyConstant.ATTR_NAME_PRT_TOOLG_INVESTMENT, toolInvestment);
		
		// Total Cost (미사용 컬럼)
//		String totCost = childRev.getPropertyObject(PropertyConstant.ATTR_NAME_TOTAL).getDisplayableValue();
//		map.put(PropertyConstant.ATTR_NAME_TOTAL, totCost);
		
		// Supplier
		String selectdCompany = childRev.getPropertyObject(PropertyConstant.ATTR_NAME_SELECTED_COMPANY).getStringValue();
		map.put(PropertyConstant.ATTR_NAME_SELECTED_COMPANY, selectdCompany);
		
		// Concept DWG Plan
		String conDwgPlan = childRev.getPropertyObject(PropertyConstant.ATTR_NAME_CON_DWG_PLAN).getStringValue();
		map.put(PropertyConstant.ATTR_NAME_CON_DWG_PLAN, conDwgPlan);
		
		// Concept DWG Performance
		String conDwgPerformance = childRev.getPropertyObject(PropertyConstant.ATTR_NAME_CON_DWG_PERFORMANCE).getStringValue();
		map.put(PropertyConstant.ATTR_NAME_CON_DWG_PERFORMANCE, conDwgPerformance);
		
		// Concept DWG Type
		String conDwgType = childRev.getPropertyObject(PropertyConstant.ATTR_NAME_CON_DWG_TYPE).getStringValue();
		map.put(PropertyConstant.ATTR_NAME_CON_DWG_TYPE, conDwgPerformance);
		
		// Deployable Date
		String dwgDeployableDate = childRev.getPropertyObject(PropertyConstant.ATTR_NAME_DWG_DEPLOYABLE_DATE).getDisplayableValue();
		if (dwgDeployableDate != null && !dwgDeployableDate.equals("")) {
			Date tmpDate = (new SimpleDateFormat("dd-MMM-yyyy HH:mm")).parse(dwgDeployableDate);
			//System.out.println("dwgDeployableDate : " + (new SimpleDateFormat("yyyy-MM-dd")).format(tmpDate));
			map.put(PropertyConstant.ATTR_NAME_DWG_DEPLOYABLE_DATE, (new SimpleDateFormat("yyyy-MM-dd")).format(tmpDate));
		} else {
			map.put(PropertyConstant.ATTR_NAME_DWG_DEPLOYABLE_DATE, "");
		}
		
		// Prod Dwg Plan
		String prodDwgPlan = childRev.getPropertyObject(PropertyConstant.ATTR_NAME_PRD_DWG_PLAN).getStringValue();
		map.put(PropertyConstant.ATTR_NAME_PRD_DWG_PLAN, prodDwgPlan);
		
		// Prod Dwg Performance
		String prodDwgPermfornce = childRev.getPropertyObject(PropertyConstant.ATTR_NAME_PRD_DWG_PERFORMANCE).getStringValue();
		map.put(PropertyConstant.ATTR_NAME_PRD_DWG_PERFORMANCE, prodDwgPermfornce);
		
		// Eco No
		String ecoNo = childRev.getPropertyObject(PropertyConstant.ATTR_NAME_ECO_NO).getStringValue();
		map.put(PropertyConstant.ATTR_NAME_ECO_NO, ecoNo);
		
		// Owning User
		ModelObject owningUsers = childRev.getPropertyObject(PropertyConstant.ATTR_NAME_OWNINGUSER).getModelObjectValue();
		tcItemUtil.getProperties(new ModelObject[]{owningUsers}, new String[] {PropertyConstant.ATTR_NAME_USERID});
		String owningUserId = ((User)owningUsers).get_user_id();
		map.put(PropertyConstant.ATTR_NAME_OWNINGUSER, owningUserId);
		
		// Owning Group
		ModelObject owningGroups = childRev.getPropertyObject(PropertyConstant.ATTR_NAME_OWNINGGROUP).getModelObjectValue();
		tcItemUtil.getProperties(new ModelObject[]{owningGroups}, new String[] {PropertyConstant.ATTR_NAME_NAME});
		String owningGroupName = ((Group)owningGroups).get_name();
		map.put(PropertyConstant.ATTR_NAME_OWNINGGROUP, owningGroupName);
		   			
		// Purchase Team
		String putTeam = childRev.getPropertyObject(PropertyConstant.ATTR_NAME_PUR_DEPT_NM).getStringValue();
		map.put(PropertyConstant.ATTR_NAME_PUR_DEPT_NM, putTeam);
		
		// Purchase Responsibility
		String purResponsibility = childRev.getPropertyObject(PropertyConstant.ATTR_NAME_PUR_RESPONSIBILITY).getStringValue();
		map.put(PropertyConstant.ATTR_NAME_PUR_RESPONSIBILITY, purResponsibility);
		
		// Employee No
		//String employeeNo = childRev.getPropertyObject(PropertyConstant.ATTR_NAME_EMPLOYEE_NO).getStringValue();
		map.put(PropertyConstant.ATTR_NAME_EMPLOYEE_NO, "");
		
	}
    
	/**
	 * Standard Part Property Set
	 * @param dao
	 * @param childRev
	 * @param map
	 * @param systemRowKey
	 * @param chgCd
	 * @throws Exception
	 */
	private void setStdPartProp(MasterListDao dao, ItemRevision childRev, HashMap<String, Object> map, String systemRowKey, String chgCd) throws Exception {
		 
		// Change Type
        map.put(PropertyConstant.ATTR_NAME_CHG_TYPE_NM, "");
        
		// Project Code
		String projCode = childRev.getPropertyObject(PropertyConstant.ATTR_NAME_PROJCODE).getStringValue();
		map.put(PropertyConstant.ATTR_NAME_PROJCODE, projCode);
		
		// System
		map.put(PropertyConstant.ATTR_NAME_BUDGETCODE, "X00");
		map.put("SYSTEM_NAME", "STANDARD HARD-WARES");
		map.put("FUNC", funcId.substring(0, 4));
		
		// Old Part No
		map.put(PropertyConstant.ATTR_NAME_OLD_PART_NO, "");
		
		// Display Part No
		String displayPartNo = childRev.getPropertyObject(PropertyConstant.ATTR_NAME_DISPLAYPARTNO).getStringValue();
		map.put(PropertyConstant.ATTR_NAME_DISPLAYPARTNO, displayPartNo);
		
		// Part Name
		String partName = childRev.getPropertyObject(PropertyConstant.ATTR_NAME_ITEMNAME).getStringValue();
		map.put(PropertyConstant.ATTR_NAME_ITEMNAME, partName);
		
		// Contents
		map.put(PropertyConstant.ATTR_NAME_CONTENTS, "");
		
		// Change Desc
		String changeDesc = childRev.getPropertyObject(PropertyConstant.ATTR_NAME_CHANGE_DESCRIPTION).getStringValue();
		map.put(PropertyConstant.ATTR_NAME_CHANGE_DESCRIPTION, changeDesc);
		
		// Act/Est Weight
		String actWgt = childRev.getPropertyObject(PropertyConstant.ATTR_NAME_ACTWEIGHT).getDisplayableValue();
		if ("C".equals(chgCd)) {
			map.put(PropertyConstant.ATTR_NAME_ESTWEIGHT, actWgt);
		} else {
			map.put(PropertyConstant.ATTR_NAME_ESTWEIGHT, "");
		}
		
		// Target Weight
		map.put(PropertyConstant.ATTR_NAME_TARGET_WEIGHT, "");
		
		// DR
		map.put(PropertyConstant.ATTR_NAME_DR, "");
		
		// Responsibility
		map.put(PropertyConstant.ATTR_NAME_BOX, "");
		
		// Est Material Cost
		map.put(PropertyConstant.ATTR_NAME_EST_COST_MATERIAL, "");
		// Tgt Material Cost
		map.put(PropertyConstant.ATTR_NAME_TARGET_COST_MATERIAL, "");
		// Tool Investment
		map.put(PropertyConstant.ATTR_NAME_PRT_TOOLG_INVESTMENT, "");
		// Prod Tool cost
		map.put(PropertyConstant.ATTR_NAME_PRD_TOOL_COST, "");
		// Prod Service Cost
		map.put(PropertyConstant.ATTR_NAME_PRD_SERVICE_COST, "");
		// Prod Sampe Cost
		map.put(PropertyConstant.ATTR_NAME_PRD_SAMPLE_COST, "");

		// Total Cost (미사용 컬럼)
		map.put(PropertyConstant.ATTR_NAME_TOTAL, "");
		
		// Tool Investment
		map.put(PropertyConstant.ATTR_NAME_PRT_TOOLG_INVESTMENT, "");
		
		// Supplier
		map.put(PropertyConstant.ATTR_NAME_SELECTED_COMPANY, "");
		
		// Concept DWG Plan
		map.put(PropertyConstant.ATTR_NAME_CON_DWG_PLAN, "");
		
		// Concept DWG Performance
		map.put(PropertyConstant.ATTR_NAME_CON_DWG_PERFORMANCE, "");
		
		// Concept DWG Type
		map.put(PropertyConstant.ATTR_NAME_CON_DWG_TYPE, "");
		
		// Deployable Date
		map.put(PropertyConstant.ATTR_NAME_DWG_DEPLOYABLE_DATE, "");
		
		// Prod Dwg Plan
		map.put(PropertyConstant.ATTR_NAME_PRD_DWG_PLAN, "");
		
		// Prod Dwg Performance
		map.put(PropertyConstant.ATTR_NAME_PRD_DWG_PERFORMANCE, "");
		
		// Eco No
		map.put(PropertyConstant.ATTR_NAME_ECO_NO, "");
		
		// Owning User
		ModelObject owningUsers = childRev.getPropertyObject(PropertyConstant.ATTR_NAME_OWNINGUSER).getModelObjectValue();
		tcItemUtil.getProperties(new ModelObject[]{owningUsers}, new String[] {PropertyConstant.ATTR_NAME_USERID});
		String owningUserId = ((User)owningUsers).get_user_id();
		map.put(PropertyConstant.ATTR_NAME_OWNINGUSER, owningUserId);
		
		// Owning Group
		ModelObject owningGroups = childRev.getPropertyObject(PropertyConstant.ATTR_NAME_OWNINGGROUP).getModelObjectValue();
		tcItemUtil.getProperties(new ModelObject[]{owningGroups}, new String[] {PropertyConstant.ATTR_NAME_NAME});
		String owningGroupName = ((Group)owningGroups).get_name();
		map.put(PropertyConstant.ATTR_NAME_OWNINGGROUP, owningGroupName);
		   			
		// Purchase Team
		map.put(PropertyConstant.ATTR_NAME_PUR_DEPT_NM, "");
		
		// Purchase Responsibility
		map.put(PropertyConstant.ATTR_NAME_PUR_RESPONSIBILITY, "");
		
		// Employee No
		map.put(PropertyConstant.ATTR_NAME_EMPLOYEE_NO, "");
		
	}
	
	/**
	 * Vehicle Part Property Set
	 * @param dao
	 * @param childRev
	 * @param map
	 * @param systemRowKey
	 * @param chgCd
	 * @throws Exception
	 */
	private void setVehiclePartProp(MasterListDao dao, ItemRevision childRev, HashMap<String, Object> map, String systemRowKey, String chgCd) throws Exception {
		 
		// Item Revision Type
		String childRevType = childRev.getPropertyObject(PropertyConstant.ATTR_NAME_ITEMTYPE).getStringValue();
		
		// Change Type
        map.put(PropertyConstant.ATTR_NAME_CHG_TYPE_NM, "");
        
		// Project Code
		String projCode = childRev.getPropertyObject(PropertyConstant.ATTR_NAME_PROJCODE).getStringValue();
		map.put(PropertyConstant.ATTR_NAME_PROJCODE, projCode);
		
		// System Code
		String systemCode = childRev.getPropertyObject(PropertyConstant.ATTR_NAME_BUDGETCODE).getStringValue();
		map.put(PropertyConstant.ATTR_NAME_BUDGETCODE, systemCode);
		
		// DCS
		if( projCode != null && !projCode.equals("") && systemCode != null && !systemCode.equals("")){
			
			DataSet ds = new DataSet();
			ds.put("PS7_PROJECT_CODE", projCode);
			ds.put("PS7_SYSTEM_CODE", systemCode);
			
			List<HashMap<String, Object>> dcList = dao.getDCSList(ds);
			if (dcList != null && dcList.size() > 0) {
				HashMap<String, Object> rtnMap = (HashMap<String, Object>) dcList.get(0);
				map.put("DCS_NO", rtnMap.get("DOC_NO") == null ? "" : rtnMap.get("DOC_NO").toString());
				map.put("DCS_DATE", rtnMap.get("RELEASE_DATE") == null ? "" : rtnMap.get("RELEASE_DATE").toString());
			} else {
				map.put("DCS_NO", "");
				map.put("DCS_DATE", "");
			}
		}
		
		// System
		map.put("SYSTEM_NAME", getSystemName(systemCode));
		map.put("FUNC", funcId.substring(0, 4));
		
		// Old Part No
		map.put(PropertyConstant.ATTR_NAME_OLD_PART_NO, "");
		
		// Display Part No
		String displayPartNo = childRev.getPropertyObject(PropertyConstant.ATTR_NAME_DISPLAYPARTNO).getStringValue();
		map.put(PropertyConstant.ATTR_NAME_DISPLAYPARTNO, displayPartNo);
		
		// Part Name
		String partName = childRev.getPropertyObject(PropertyConstant.ATTR_NAME_ITEMNAME).getStringValue();
		map.put(PropertyConstant.ATTR_NAME_ITEMNAME, partName);
		
		// Contents
		map.put(PropertyConstant.ATTR_NAME_CONTENTS, "");
		
		// Change Desc
		String changeDesc = childRev.getPropertyObject(PropertyConstant.ATTR_NAME_CHANGE_DESCRIPTION).getStringValue();
		map.put(PropertyConstant.ATTR_NAME_CHANGE_DESCRIPTION, changeDesc);
		
		// Act/Est Weight
		String actWgt = "";
		ModelObject relObjs = tcItemUtil.getRelatedFromModelObjectValue(childRev.getUid(), PropertyConstant.ATTR_NAME_VEH_TYPE_REF);
		if (relObjs != null) {
			
			tcItemUtil.getProperties(new ModelObject[]{relObjs}, new String[] {PropertyConstant.ATTR_NAME_ACTWEIGHT});
			
			// Act Material Cost
			actWgt = childRev.getPropertyObject(PropertyConstant.ATTR_NAME_ACTWEIGHT).getDisplayableValue();
		}
		
		String estWgt = childRev.getPropertyObject(PropertyConstant.ATTR_NAME_ESTWEIGHT).getDisplayableValue();
		if ("C".equals(chgCd)) {
			map.put(PropertyConstant.ATTR_NAME_ESTWEIGHT, actWgt);
		} else {
			map.put(PropertyConstant.ATTR_NAME_ESTWEIGHT, estWgt);
		}
		
		// Target Weight
		map.put(PropertyConstant.ATTR_NAME_TARGET_WEIGHT, "");
		
		// DR
		String dr = childRev.getPropertyObject(PropertyConstant.ATTR_NAME_DR).getStringValue();
		map.put(PropertyConstant.ATTR_NAME_DR, dr);
		
		// Responsibility
		String box = childRev.getPropertyObject(PropertyConstant.ATTR_NAME_BOX).getStringValue();
		map.put(PropertyConstant.ATTR_NAME_BOX, box);
		
		// Est Material Cost
		map.put(PropertyConstant.ATTR_NAME_EST_COST_MATERIAL, "");
		// Tgt Material Cost
		map.put(PropertyConstant.ATTR_NAME_TARGET_COST_MATERIAL, "");
		// Tool Investment
		map.put(PropertyConstant.ATTR_NAME_PRT_TOOLG_INVESTMENT, "");
		// Prod Tool cost
		map.put(PropertyConstant.ATTR_NAME_PRD_TOOL_COST, "");
		// Prod Service Cost
		map.put(PropertyConstant.ATTR_NAME_PRD_SERVICE_COST, "");
		// Prod Sampe Cost
		map.put(PropertyConstant.ATTR_NAME_PRD_SAMPLE_COST, "");

		// Total Cost (미사용 컬럼)
		map.put(PropertyConstant.ATTR_NAME_TOTAL, "");
		
		// Tool Investment
		map.put(PropertyConstant.ATTR_NAME_PRT_TOOLG_INVESTMENT, "");
		
		// Supplier
		map.put(PropertyConstant.ATTR_NAME_SELECTED_COMPANY, "");
		
		// Concept DWG Plan
		map.put(PropertyConstant.ATTR_NAME_CON_DWG_PLAN, "");
		
		// Concept DWG Performance
		map.put(PropertyConstant.ATTR_NAME_CON_DWG_PERFORMANCE, "");
		
		// Concept DWG Type
		map.put(PropertyConstant.ATTR_NAME_CON_DWG_TYPE, "");
		
		// Deployable Date
		map.put(PropertyConstant.ATTR_NAME_DWG_DEPLOYABLE_DATE, "");
		
		// Prod Dwg Plan
		map.put(PropertyConstant.ATTR_NAME_PRD_DWG_PLAN, "");
		
		// Prod Dwg Performance
		map.put(PropertyConstant.ATTR_NAME_PRD_DWG_PERFORMANCE, "");
		
		// Eco No
		map.put(PropertyConstant.ATTR_NAME_ECO_NO, "");
		
		// Owning User
		ModelObject owningUsers = childRev.getPropertyObject(PropertyConstant.ATTR_NAME_OWNINGUSER).getModelObjectValue();
		tcItemUtil.getProperties(new ModelObject[]{owningUsers}, new String[] {PropertyConstant.ATTR_NAME_USERID});
		String owningUserId = ((User)owningUsers).get_user_id();
		map.put(PropertyConstant.ATTR_NAME_OWNINGUSER, owningUserId);
		
		// Owning Group
		ModelObject owningGroups = childRev.getPropertyObject(PropertyConstant.ATTR_NAME_OWNINGGROUP).getModelObjectValue();
		tcItemUtil.getProperties(new ModelObject[]{owningGroups}, new String[] {PropertyConstant.ATTR_NAME_NAME});
		String owningGroupName = ((Group)owningGroups).get_name();
		map.put(PropertyConstant.ATTR_NAME_OWNINGGROUP, owningGroupName);
		   			
		// Purchase Team
		map.put(PropertyConstant.ATTR_NAME_PUR_DEPT_NM, "");
		
		// Purchase Responsibility
		map.put(PropertyConstant.ATTR_NAME_PUR_RESPONSIBILITY, "");
		
		// Employee No
		map.put(PropertyConstant.ATTR_NAME_EMPLOYEE_NO, "");
		
	}
	
    private OSpec getOspec(ModelObject ospecRev) throws Exception {
        try
        {
            OSpec ospec = null;
            String ospecStr = ((ItemRevision) ospecRev).get_item_id() + "-" + ((ItemRevision) ospecRev).get_item_revision_id();
            ModelObject[] datasets = ospecRev.getPropertyObject(TcConstants.RELATION_REFERENCES).getModelObjectArrayValue();

            tcItemUtil.getProperties(datasets, new String[]{PropertyConstant.ATTR_NAME_ITEMNAME, PropertyConstant.ATTR_NAME_REF_NAMES});
            for( int i = 0; datasets != null && i < datasets.length; i++){
                ModelObject ds = datasets[i];

                if( ospecStr.equals(((Dataset) ds).get_object_name())){
                    Property refProperty = ds.getPropertyObject(PropertyConstant.ATTR_NAME_REF_NAMES);
                    ImanFile[] imanFiles = datasetUtil.getReferencedFileFromDataset(ds.getUid(), refProperty.getStringArrayValue());
                    File[] files = fileUtil.getFiles(imanFiles);
                    ospec = OpUtil.getOSpec(files[0]);
                    break;
                };
            }
            
            return ospec;
        }
        catch (Exception ex)
        {
            throw ex;
        }
    }
	
    private HashMap<String, StoredOptionSet> getStoredOptionSets(OSpec ospec) throws Exception
    {
        try
        {
            HashMap<String, StoredOptionSet> optionSetMap = new HashMap<String, StoredOptionSet>();;

            ArrayList<OpTrim> trimList = ospec.getTrimList();
            HashMap<String, ArrayList<Option>> trimOptionMap = ospec.getOptions();
            
            for (OpTrim opTrim : trimList)
            {
                ArrayList<Option> options = trimOptionMap.get(opTrim.getTrim());
                String stdName = opTrim.getTrim() + "_STD";
                String optName = opTrim.getTrim() + "_OPT";
                StoredOptionSet stdSos = new StoredOptionSet(stdName);
                stdSos.add("TRIM", stdName);
                StoredOptionSet optSos = new StoredOptionSet(optName);
                optSos.add("TRIM", optName);
                
                for( Option option : options){
                    if( option.getValue().equalsIgnoreCase("S")){
                        stdSos.add(option.getOp(), option.getOpValue());
                        optSos.add(option.getOp(), option.getOpValue());
                    }else if( !option.getValue().equalsIgnoreCase("-") ){
                        optSos.add(option.getOp(), option.getOpValue());
                    }
                }
                
                optionSetMap.put(stdName, stdSos);
                optionSetMap.put(optName, optSos);
            }

            return optionSetMap;
        }
        catch (Exception ex)
        {
            throw ex;
        }
    }
    
	private String getSystemName(String systemCode){
		if( systemCode == null || systemCode.equals("")){
			return "";
		}
		
		Iterator<String> iterator = systemCodeMap.keySet().iterator();
	    while (iterator.hasNext()) {
	        String key = (String) iterator.next();
	    	if( key.equals(systemCode)){
				return systemCodeMap.get(key);
			}
	    }
		
		return "";
	}
    
	public String getConditionSet(BOMWindow window, BOMLine parentLine, ItemRevision parentRevision, String conditionStr) {
		
		try {
			String parentype = parentRevision.getPropertyObject(PropertyConstant.ATTR_NAME_ITEMTYPE).getStringValue();
			BOMLine topLine = (BOMLine)window.get_top_line();
			if( topLine.equals(parentLine) || parentype.equals(TcConstants.S7_PREFUNCMASTERREVISIONTYPE)) {
				return conditionStr;
			}
			
			String resultCondition = "";
			String str = parentLine.getPropertyObject(PropertyConstant.ATTR_NAME_BL_CONDITION).getStringValue();
			str = convertToSimpleCondition(str);
			if( conditionStr == null || conditionStr.equals("")){
				if(str != null && !str.equals("")){
					resultCondition = "(" + str +")";	
				}
			}else{
				if( str != null && !str.equals("")){
					resultCondition = "(" + str +")" + " and " + conditionStr;
				}else{
					resultCondition = conditionStr;
				}
			}
			
			BOMLine pparentLine = (BOMLine) parentLine.get_bl_parent();
            tcItemUtil.getProperties(new ModelObject[]{pparentLine}, new String[]{PropertyConstant.ATTR_NAME_BL_ITEM_REVISION});
			ItemRevision ppRevision = (ItemRevision) pparentLine.get_bl_revision();
			tcItemUtil.getProperties(new ModelObject[]{ppRevision}, new String[] {PropertyConstant.ATTR_NAME_ITEMTYPE});
			
			String pparnetType = ppRevision.getPropertyObject(PropertyConstant.ATTR_NAME_ITEMTYPE).getStringValue();
			if( !pparnetType.equals(TcConstants.S7_PREFUNCMASTERREVISIONTYPE)){
				resultCondition = getConditionSet(window, pparentLine, ppRevision, resultCondition);
			}
			
			return resultCondition;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return conditionStr; 
	}

    private String convertToSimpleCondition(String condition){
        ArrayList<String> foundOpValueList = new ArrayList<String>();

        if (condition == null || condition.equals("")) {
        	return "" ;
        }
        
        Pattern p = Pattern.compile(" or | and |\"[a-zA-Z0-9]{4}\"|\"[a-zA-Z0-9]{5}_STD\"|\"[a-zA-Z0-9]{5}_OPT\"");
        Matcher m = p.matcher(condition);
        while (m.find()) {
//          System.out.println(m.start() + " " + m.group());
            foundOpValueList.add(m.group().trim());
        }
        
        String conditionResult = null;
        for( String opValue : foundOpValueList){
            String con = opValue.replaceAll("\"", "");
            if( conditionResult == null){
                conditionResult = con;
            }else{
                conditionResult += " " + con;
            }
        }
        
        return conditionResult;
    }
	
    public String removeTrimOptionValue(String condition){
    	
        String resultStr = "";
        
        if (condition == null || condition.equals("")) {
        	return "" ;
        }
        
        String[] tmpArray = condition.split(" or ");
        for( int i = 0; tmpArray != null && i < tmpArray.length; i++){
            String str = tmpArray[i].replaceAll("[a-zA-Z0-9]{5}_STD and |[a-zA-Z0-9]{5}_OPT and ", "");
            str = str.replaceAll(" and [a-zA-Z0-9]{5}_STD| and [a-zA-Z0-9]{5}_OPT", "");
            str = str.replaceAll("[a-zA-Z0-9]{5}_STD|[a-zA-Z0-9]{5}_OPT", "").trim(); 
            if( resultStr.equals("")){
                resultStr = str;
            }else{
                resultStr += " or " + str;
            }
        }
        
        return resultStr;
    }
    
	public int getLevel(BOMLine childLine, int baseLevel) throws Exception{

		BOMLine parentLine = (BOMLine) childLine.get_bl_parent();
		if( parentLine == null){
			return 0;
		}
		
        tcItemUtil.getProperties(new ModelObject[]{parentLine}, new String[]{PropertyConstant.ATTR_NAME_BL_ITEM_REVISION});
		ItemRevision parentRevision = (ItemRevision) parentLine.get_bl_revision();
		tcItemUtil.getProperties(new ModelObject[]{parentRevision}, new String[] {PropertyConstant.ATTR_NAME_ITEMTYPE});
		
		String parnetType = parentRevision.getPropertyObject(PropertyConstant.ATTR_NAME_ITEMTYPE).getStringValue();
		if( parnetType.equals(TcConstants.S7_PREFUNCMASTERREVISIONTYPE)){
			return baseLevel;
		} else {
			return getLevel(parentLine, baseLevel + 1);
		}
	}
    
    public static String convertToString(Object obj){
        if( obj == null ){
            return "";
        }else{
            return obj.toString();
        }
    }
	
}
