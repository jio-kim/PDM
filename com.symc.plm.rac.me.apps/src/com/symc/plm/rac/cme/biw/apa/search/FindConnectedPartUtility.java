package com.symc.plm.rac.cme.biw.apa.search;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.swing.plaf.basic.BasicOptionPaneUI;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.utils.BOPStructureDataUtility;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.designcontext.util.RDVUtilities;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentQuery;
import com.teamcenter.rac.kernel.TCComponentQueryType;
import com.teamcenter.rac.kernel.TCComponentRevisionRule;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.kernel.ics.ICSProperty;
import com.teamcenter.rac.pse.search.PSEFormAttributeSearchCriteria;
import com.teamcenter.rac.pse.search.PSESearchInClassElement;
import com.teamcenter.rac.pse.search.PSESearchOperationParameters;
import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.services.rac.structuremanagement.StructureSearchService;

public class FindConnectedPartUtility {
	
	private TCSession session;
	private TCComponentRevisionRule revisionRule;
	private double proximityDistance;
	private String representTargetOperationId;
	private ArrayList<String> preStationIdList;
	private int resultLimit = 0;
	private DateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss SSS");
	private TCComponentBOMLine[] preDecessorBOMLineLists;
	
	public FindConnectedPartUtility(){
		
	}
	
	public static Vector<String> getConnectedPartTargetFunctionNoStrList(){
		
		Vector<String> targetFunctionNoStrV = new Vector<String>();
		
		targetFunctionNoStrV.add("F006");
		targetFunctionNoStrV.add("F610");
		targetFunctionNoStrV.add("F620");
		targetFunctionNoStrV.add("F630");
		targetFunctionNoStrV.add("F640");
		targetFunctionNoStrV.add("F650");
		targetFunctionNoStrV.add("F660");
		targetFunctionNoStrV.add("F720");
		targetFunctionNoStrV.add("F730");
		targetFunctionNoStrV.add("F740");
		targetFunctionNoStrV.add("F750");

		return targetFunctionNoStrV;
	}
	
	/**
	 * Product Window에서 검색 대상인 BOM Line을 제한 하기위해 필요한 BOMLine만 찾아서 Return 한다.
	 * @param window
	 * @return
	 */
	public static TCComponentBOMLine[] geConnectedPartSearchTargetBOMLine(TCComponentBOMWindow window){
		
		TCComponentBOMLine[] connectedPartSearchTargetBOMLine = null;
		
		if(window==null){
			return connectedPartSearchTargetBOMLine;
		}
		
		Vector<String> targetFunctionNoStrV = FindConnectedPartUtility.getConnectedPartTargetFunctionNoStrList();
		Vector<TCComponentBOMLine> targetFunctionBOMLineV = new Vector<TCComponentBOMLine>();
		try {
			TCComponentBOMLine topBOMLine = window.getTopBOMLine();
			
			String topNodeItemRevisionType = topBOMLine.getItemRevision().getType();
			if(topNodeItemRevisionType.equalsIgnoreCase(SDVTypeConstant.BOP_MPRODUCT_REVISION)==false){
				return connectedPartSearchTargetBOMLine;
			}
			
			AIFComponentContext[] childLines = topBOMLine.getChildren();
			for (int i = 0;childLines!=null &&  i < childLines.length; i++) {
				TCComponentBOMLine tmpBOMLine = (TCComponentBOMLine)childLines[i].getComponent();
				String itemId = tmpBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
				if(itemId!=null && itemId.trim().length()>4){
					String functionKeyCode = itemId.substring(0, 4);
					boolean isTargetCode = targetFunctionNoStrV.contains(functionKeyCode);
					if(isTargetCode){
						if(targetFunctionBOMLineV.contains(tmpBOMLine)==false){
							targetFunctionBOMLineV.add(tmpBOMLine);
						}
					}
				}
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
		
		if(targetFunctionBOMLineV!=null && targetFunctionBOMLineV.size()>0){
			connectedPartSearchTargetBOMLine = new TCComponentBOMLine[targetFunctionBOMLineV.size()];
		}
		for (int i = 0;targetFunctionBOMLineV!=null && i < targetFunctionBOMLineV.size(); i++) {
			TCComponentBOMLine bomLinea = targetFunctionBOMLineV.get(i);
			connectedPartSearchTargetBOMLine[i] = bomLinea;
		}
		
		return connectedPartSearchTargetBOMLine;
	}
	
//	/**
//	 * 2015/12/18 현재 Connected Part의 검색결과를 25개로 제한 하고 있는데 StructureSearch 결과가 25개보다 많은경우
//	 * Display할 수 없는 결과에 대해 Running Time을 소모하지 않도록 하기위한 조치에 사용되는 제한 수량
//	 * @param resultLimit
//	 */
//	public void setResultLimit(int resultLimit) {
//		this.resultLimit = resultLimit;
//	}
	
//	/**
//	 * Connected Part 검색을 위해 사용되는 조건은 복수개를 선택 하더라도 동일하므로 검색조건에 사용되는 변수들을
//	 * 초기화한다.
//	 * @param weldPointBOMLines 사용자가 선택한 용접공법에 포함된 용접점 또는 별도로 1개 선택한 용접점의 List
//	 * @param revisionRule Structure Search에 사용될 Revision Rule
//	 * @param proximityDistance 용접점과 근접한 Part를 찾을때 사용되는 근접 기준 거리 (단위는 m 임!)
//	 */
//	public void initSearchConditionFactors(TCComponent[] weldPointBOMLines,
//			TCComponentRevisionRule revisionRule, double proximityDistance){
//		
//    	TCComponentBOMLine representWeldPointBOMLine = null;
//    	ArrayList<TCComponentBOMLine> userSelectTargetBOMLineList = new ArrayList<TCComponentBOMLine>();
//	
//		for (int i = 0;weldPointBOMLines!=null && i < weldPointBOMLines.length; i++) {
//			
//			if(weldPointBOMLines[i]!=null && weldPointBOMLines[i] instanceof TCComponentBOMLine){
//				representWeldPointBOMLine = (TCComponentBOMLine)weldPointBOMLines[i];
//				if(userSelectTargetBOMLineList.contains(representWeldPointBOMLine)==false){
//					userSelectTargetBOMLineList.add(representWeldPointBOMLine);
//				}
//			}
//		}
//		
//    	boolean isSpatialDataAvailable = false;
//    	try {
//    		isSpatialDataAvailable = RDVUtilities.isSpatialDataAvailable(representWeldPointBOMLine.window());
//		} catch (TCException e) {
//				System.out.println(df.format(new Date())+" : "+e.getMessage());
//		}
//    	System.out.println(df.format(new Date())+" : "+"isSpatialDataAvailable = "+isSpatialDataAvailable);
//    	if(isSpatialDataAvailable==false){
//    		System.out.println(df.format(new Date())+" : "+"can't going on structure search..");
//    	}
//		
//		this.revisionRule = revisionRule;
//		this.session = representWeldPointBOMLine.getSession();
//		this.proximityDistance = proximityDistance;
//		
//		try {
//			TCComponentBOMLine weldOperationBOMLine = findTypedBOMLine(representWeldPointBOMLine, "M7_BOPWeldOPRevision");
//			TCComponentItemRevision targetOperationItemRevision = (TCComponentItemRevision)weldOperationBOMLine.getItemRevision().getReferenceProperty("m7_TARGET_OP");
//			this.representTargetOperationId = targetOperationItemRevision.getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
//		} catch (TCException e) {
//			e.printStackTrace();
//		}
//		
//		// PERT에 기록된 정보를 이용해 선행 Station Id List를 가져온다.
//	    this.preStationIdList = getPreStationIdList(representWeldPointBOMLine);
//	    this.preStationIdList.add(this.representTargetOperationId);
//	    
//	    for (int i = 0;this.preDecessorBOMLineLists!=null && i < this.preDecessorBOMLineLists.length; i++) {
//			System.out.println("@@@@ BOMLine["+i+"] = "+this.preDecessorBOMLineLists[i]);
//		}
//	    
//	}

//	/**
//	 * Structure Search를 통해 주어진 용접점에 대한 Connected Part를 검색하고 그 결과를 Return한다. 
//	 * @param targetWeldPointBOMLine 검색 기준이 되는 용접점
//	 * @return
//	 */
//    public ArrayList<TCComponentItemRevision> getStructureSearchResultRevisionList(
//    		TCComponentBOMLine targetWeldPointBOMLine){
//    	
//    	System.out.println("STR Search  "+targetWeldPointBOMLine+" ["+df.format(new Date())+" (Start)]");
//    	
//    	/*
//    	 * 1. Connected Part 검색은 Structure Search를 사용한다.
//    	 * 2. Structure Search의 검색조건은 다음의 것이 포한되어야 한다.
//    	 *     a. 검색의 대상이 되는 BOMLine ( ex : Root Node)
//    	 *     b. Revision Rule
//    	 *     c. 검색 기준인 BOM Line
//    	 *     d. 검색 결과에 포함될 Item Type조건 Saved Query
//    	 *     e. 검색 조건이 되는 거리 (Structure Search는 m 단위로 입력 받아서 검색함)
//    	 *     f. Test를 위해서는 위의 조건을 염두에 두고 MPP에서 Structure Search를 해보면 된다.
//    	 * 3. 검색 결과중에 Assembly Item은 Connected Part 대상이 아님
//    	 *     -- 다음과 같은 결과 구분 조건이 추가되기를 사용자는 희망함.
//    	 *     a. 가능하다면 Part의 이름 기준으로 동일한 이름이 있는경우 Part No가 빠른것을 결과에 포함하고 나머지는 포함시키지 않는다.
//    	 *     b. Part No를 기준으로 동일시 될 수 있는 Part는 검색 결과에 포함 시키지 않는다.
//    	 * 4. 검색 결과 Part는 검색 기준이 되었던 용접점의 Target Operation에 할당된 Part를 포함한다.
//    	 * 5. 검색 결과 Part는 PERT 구성 순서상으로 볼때 검색 기준 용접점이 포함된 Station의 이전 Statoin에 포함된 Part만 대상임
//    	 * 6. 검색 결과에는 Operation에 할당된 Part의 하위 Part들이 포함되어야 한다. 
//    	 */
//    	
//    	// 검색 기준이 되는 용접점 BOMLine
//		TCComponentBOMLine[] targetBOMLines = new TCComponentBOMLine[]{
//				targetWeldPointBOMLine
//		};
//		
//		// 검색 대상인 Item Type을 정의한다.
//        String searchType = ((TCSession)this.session).getPreferenceService().getStringValue("MEAPCSearchType"); //S7_Vehpart
//        String type= null;
//        String keyItemId = null;
//        String keyOwningGroup = null;
//		try {
//			type = ((TCSession)this.session).getTextService().getTextValue("Type");
//			keyItemId = ((TCSession)this.session).getTextService().getTextValue("ItemID");
//			keyOwningGroup = ((TCSession)this.session).getTextService().getTextValue("OwningGroup");
//		} catch (TCException e) {
//			e.printStackTrace();
//		}
//        
//		// 검색 결과에 포함될 Part Type을 정의하는 Saved Query 객체를 생성 한다.
//        TCComponentQuery savedQuery = null;
//        String itemAttributesNames[] = {type, keyItemId, keyOwningGroup};
//        String itemAttributesValues[] = {searchType, "5*;6*", "BODY DESIGN1.SYMC;BODY DESIGN2.SYMC"};
//
//		try {
//			TCComponentQueryType  tccomponentquerytype = (TCComponentQueryType)this.session.getTypeComponent("ImanQuery");
//			savedQuery = (TCComponentQuery)tccomponentquerytype.find("Item...");
//		} catch (TCException e) {
//			e.printStackTrace();
//		}
//		
//		TCComponent[] m_itemAttributesTags = null;
//		String[] mappedAttributesNames = null;
//		String[] mappedAttributesValues = null;
//		String[] mappedAttributesOperators = null;
//		String remoteSearchSite = null;
//		PSESearchInClassElement[] inClassViewNames = null;
//		ICSProperty[] inClassProperties = null;
//		List boxzones = null;
//		List boxOperators = null;
//		List planezone = null;
//		List planeOperators = null;
//		PSEFormAttributeSearchCriteria[] pseFormAttributeSearchCriteria = null;
//		boolean enableTrushapeFiltering = true;
//		
//		boolean isReturnScopedSubTreesHit = false;
//		boolean isIncludeChildBomLines = true;         // 검색된 Part의 Child Node들의 BOMLine을 포함한다.
//		
//		isReturnScopedSubTreesHit = false;
//		isIncludeChildBomLines = false;         // 검색된 Part의 Child Node들의 BOMLine을 포함한다.
//		
//		boolean executeVOOFilter = false;
//		boolean performRemoteSearch = false;
//		TCComponentBOMLine[] scopeLines = null;
//		
//    	PSESearchOperationParameters searchexpressionset = new PSESearchOperationParameters(
//    			this.revisionRule,
//    			this.proximityDistance, 
//    			targetBOMLines,
//    			savedQuery,
//    			itemAttributesNames,
//    			itemAttributesValues,  
//    			m_itemAttributesTags,
//    			mappedAttributesNames,
//    			mappedAttributesValues,
//    			mappedAttributesOperators,
//    			remoteSearchSite,
//    			inClassViewNames,
//    			inClassProperties,
//    			boxzones,
//    			boxOperators,
//    			planezone,
//    			planeOperators,
//    			pseFormAttributeSearchCriteria,
//    			enableTrushapeFiltering,
//    			isReturnScopedSubTreesHit,
//    			isIncludeChildBomLines,
//    			executeVOOFilter,
//    			performRemoteSearch,
//    			scopeLines);
//    	
//    	ArrayList<TCComponentItemRevision> searchedItemRevision = new ArrayList<TCComponentItemRevision>();
//    	// 실제 Structure Search를 수행하는 Function 호출
//    	searchedItemRevision =  getFindedConnectedPartBOMLines(searchexpressionset, targetWeldPointBOMLine);
//
//    	// 검색 결과에 포함된 Part Item Revision의 갯수를 콘솔에 찍는다.
//    	int filteredCount = 0;
//        if(searchedItemRevision!=null){
//        	filteredCount = searchedItemRevision.size();
//        }
//
//    	System.out.println("STR Search  "+targetWeldPointBOMLine + " : Found "+filteredCount+" parts"+" ["+df.format(new Date())+" (End)]");
//    	
//    	return searchedItemRevision;
//    }
    
//    /**
//     * 주어진 BOMLine의 ItemRevision이 Connected Part로 적합한 Data인지 확인 하고 적합한 것으로 판별되면 Item Revision을 Return 한다.
//     * @param currentFindedPartBOMLine
//     * @return
//     */
//    private TCComponentItemRevision getTargetAblePart(TCComponentBOMLine currentFindedPartBOMLine){
//    	
//    	boolean isConsiderationTarget = false;
//    	
//    	try {
//			String itemId = currentFindedPartBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
//		} catch (TCException e3) {
//			e3.printStackTrace();
//		}
//    	
//    	// 우선 검색된 Part의 Parent가 OperaionBOMLine 인지 확인 해야 한다.
//    	// 선택된 용접점이 포함된 용접공법의 Target Operation을 찾고 이에 포함된 Child Node인 Part는
//    	// 검색된 Connected Part가 검토 대상에 포함되도록 한다.
//    	try {
//			TCComponentBOMLine parentBOMLine = currentFindedPartBOMLine.parent();
//			String parentItemRevisionType = parentBOMLine.getItemRevision().getType();
//			if(parentItemRevisionType!=null && parentItemRevisionType.trim().equalsIgnoreCase("M7_BOPBodyOpRevision")==true){
//    			try {
//    				String operationId = parentBOMLine.getItem().getProperty("item_id");
//    				if(operationId!=null && operationId.trim().equalsIgnoreCase(this.representTargetOperationId)==true){
//    		    		// 선택된 WeldOperation의 Target Operation이므로 결과에 포함 시킨다.
//    		    		isConsiderationTarget = true;
//						System.out.println(df.format(new Date())+" : "+"여기서 True ----1");
//    		    	}
//    			} catch (TCException e1) {
//    				e1.printStackTrace();
//    			}
//			}
//		} catch (TCException e2) {
//			e2.printStackTrace();
//		}
//    	
//    	// Target Operation의 Child Node가 아니면 PERT에 포함된 Part만 검토 대상이 되도록 해야 한다.
//    	if(isConsiderationTarget==false){
//    		
//    		TCComponentBOMLine stationBOMLine = findTypedBOMLine(currentFindedPartBOMLine, "M7_BOPStationRevision");
//    		String currentStationId = null;
//    		if(stationBOMLine!=null){
//    			try {
//    				currentStationId = stationBOMLine.getItem().getProperty("item_id");
//    			} catch (TCException e) {
//    				e.printStackTrace();
//    			}
//    		}
//    		
//    		if(currentStationId!=null && this.preStationIdList!=null && this.preStationIdList.contains(currentStationId)==true){
//    			// 선택된 WeldOperation의 Target Operation이므로 결과에 포함 시킨다.
//    			isConsiderationTarget = true;        	
//    		}
//    	}
//
//    	// Connected Part로 추가할 대상이 아닌경우 null을 Return한다.
//    	if(isConsiderationTarget==false){
//    		return (TCComponentItemRevision)null;
//    	}
//    	
//    	// Part Id를 확인해서 Connected Part에 포함 대상 Part 인지 검토한다.
//   		// Assembly Part 제외 위해...
//    	String partId = null;
//    	TCComponentItemRevision searchedRevision = null;
//    	try {
//			searchedRevision = currentFindedPartBOMLine.getItemRevision();
//			partId = currentFindedPartBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
//		} catch (TCException e) {
//			e.printStackTrace();
//		}
//    	
//        //[SR140611-032][20140611] jwlee 결합판넬 Dialog에 표시되는 개수를 검색대상에서 Assay Item은 제외 (Part ID의 다섯번째 자리가 0이면 Assay Item이니 이것들은 검색대상에서 제외 처리함)
//        if (partId.length() > 5) {
//            if (partId.charAt(4) == '0') {
//            	searchedRevision = null;
//            }
//        }
//        
//        // Connected Part 판벽의 추가적인 조건이 있으면 아래 부분에 추가 구현 한다.
//        
//        return searchedRevision;
//    }


//    /**
//     * Structure Search를 통해서 근접거리에 있는 Part들을 찾는다.
//     * @param psesearchoperationparameters
//     * @param srcWeldPointBOMLine
//     * @return
//     */
//    @SuppressWarnings({ "rawtypes", "unused" })
//    private ArrayList<TCComponentItemRevision> getFindedConnectedPartBOMLines(
//    		PSESearchOperationParameters psesearchoperationparameters,
//    		TCComponentBOMLine srcWeldPointBOMLine){
//    	
//        com.teamcenter.services.rac.structuremanagement._2010_09.StructureSearch.SearchExpressionSet searchexpressionset = new com.teamcenter.services.rac.structuremanagement._2010_09.StructureSearch.SearchExpressionSet();
//        com.teamcenter.services.rac.structuremanagement._2008_05.StructureSearch.AttributeExpression aattributeexpression[] = new com.teamcenter.services.rac.structuremanagement._2008_05.StructureSearch.AttributeExpression[0];
//        com.teamcenter.services.rac.structuremanagement._2008_05.StructureSearch.AttributeExpression aattributeexpression1[] = psesearchoperationparameters.buildAttributeExpression();
//        // 근접조건 설정
//        com.teamcenter.services.rac.structuremanagement._2008_05.StructureSearch.ProximityExpression aproximityexpression[] = psesearchoperationparameters.buildProximityExpressions();
//        com.teamcenter.services.rac.structuremanagement._2008_05.StructureSearch.BoxZoneExpression aboxzoneexpression[] = psesearchoperationparameters.buildBoxZoneExpressions();
//        com.teamcenter.services.rac.structuremanagement._2008_05.StructureSearch.SavedQueryExpression asavedqueryexpression[] = psesearchoperationparameters.buildSavedQueryExpressions();
//        com.teamcenter.services.rac.structuremanagement._2008_05.StructureSearch.OccurrenceNoteExpression aoccurrencenoteexpression[] = psesearchoperationparameters.buildOccurrenceNoteExpressions();
//        com.teamcenter.services.rac.structuremanagement._2008_05.StructureSearch.FormAttributeExpression aformattributeexpression[] = psesearchoperationparameters.buildFormAttributeExpressions();
//        com.teamcenter.services.rac.structuremanagement._2010_04.StructureSearch.PlaneZoneExpression aplanezoneexpression[] = new com.teamcenter.services.rac.structuremanagement._2010_04.StructureSearch.PlaneZoneExpression[0];
//        com.teamcenter.services.rac.structuremanagement._2008_05.StructureSearch.InClassExpression ainclassexpression[] = new com.teamcenter.services.rac.structuremanagement._2008_05.StructureSearch.InClassExpression[0];
//        
//        // -----------------------------------------------------------------------
//    	// 검색범위를 전제 Process로 정의한다.
//        // -----------------------------------------------------------------------
//    	TCComponentBOMWindow bomWindow = null;
//    	TCComponentBOMLine bomWindowTopBOMLine = null;
//    	try {
//    		bomWindow = srcWeldPointBOMLine.window();
//    		bomWindowTopBOMLine = bomWindow.getTopBOMLine();
//		} catch (TCException e1) {
//			e1.printStackTrace();
//		}
//        com.teamcenter.services.rac.structuremanagement._2008_05.StructureSearch.SearchScope searchscope = new com.teamcenter.services.rac.structuremanagement._2008_05.StructureSearch.SearchScope();
//        searchscope.window = bomWindow;
//        TCComponentBOMLine atccomponentbomline[] = new TCComponentBOMLine[]{bomWindowTopBOMLine};
//        
////		if(this.preDecessorBOMLineLists!=null && this.preDecessorBOMLineLists.length>0){
////			// Search Scop 축소
////			searchscope.scopeBomLines = this.preDecessorBOMLineLists; 
////		}else{
//			searchscope.scopeBomLines = atccomponentbomline;
////		}
//        
//        System.out.println(df.format(new Date())+" : "+"bomWindowTopBOMLine = "+bomWindowTopBOMLine);
//        
//        // -----------------------------------------------------------------------
//    	// 검색범위 정의 완료
//        // -----------------------------------------------------------------------
//        
//        searchexpressionset.itemAndRevisionAttributeExpressions = aattributeexpression1;
//        searchexpressionset.itemAndRevisionAttributeExpressions = aattributeexpression;
//        searchexpressionset.occurrenceNoteExpressions = aoccurrencenoteexpression;
//        searchexpressionset.formAttributeExpressions = aformattributeexpression;
//        searchexpressionset.proximitySearchExpressions = aproximityexpression; 
//        searchexpressionset.boxZoneExpressions = aboxzoneexpression;
//        searchexpressionset.planeZoneExpressions = aplanezoneexpression;
//        searchexpressionset.savedQueryExpressions = asavedqueryexpression;
//        searchexpressionset.inClassQueryExpressions = ainclassexpression;
//        searchexpressionset.doTrushapeRefinement = psesearchoperationparameters.isTrushapeFilterEnabled();
//        
//        StructureSearchService structuresearchservice = StructureSearchService.getService(this.session);
//        
//        // 검색 결과를 저장할 변수 정의
//        ArrayList<TCComponentItemRevision> searchedBOMLines = new ArrayList<TCComponentItemRevision>();
//        com.teamcenter.services.rac.structuremanagement._2008_05.StructureSearch.StructureSearchResultResponse structuresearchresultresponse = null;
//
//        // --------------------------------------------------        
//        // Structure Search 를 수행 한다.
//        // --------------------------------------------------
//        ArrayList<TCComponentBOMLine> list = new ArrayList<TCComponentBOMLine>();
//        try {
//            structuresearchresultresponse = structuresearchservice.startSearch(searchscope, searchexpressionset);
//            
//            // 검색결과 근접 Part의 BOMLine 정보를 확인하기위한 While Loop의 조건을 초기화 한다.
//            boolean isInCondition = true;
//            int countI = 0;
//            while (isInCondition == true) {
//            	
//            	structuresearchresultresponse = structuresearchservice.nextSearch(structuresearchresultresponse.searchCursor);
//            	
//            	// 검색 결과가 더이상 없거나 검색결과응답이 종료되는 경우 While Loop를 종료한다.
//            	if(structuresearchresultresponse == null || structuresearchresultresponse.finished){
//            		isInCondition = false;
//            		break;
//            	}
//            	 
//            	// 검색결과에 포함된 근접 Part BOMLine을 배열에 담는다.
//                 for (int i = 0;structuresearchresultresponse!=null && i < structuresearchresultresponse.bomLines.length; i++) {
//                	 
//                 	// 검색 결과가 더이상 없거나 검색결과응답이 종료되는 경우 While Loop를 종료한다.
//                 	if(structuresearchresultresponse.finished){
//                 		System.out.println("@@@@ "+df.format(new Date())+" : BREAK !!!!");
//                 		isInCondition = false;
//                 		break;
//                 	}
//                 	
//                 	if(list.size()<25){
//                        TCComponentBOMLine searchedBOMLine = structuresearchresultresponse.bomLines[i];
//                        System.out.println("@@@@ "+df.format(new Date())+" : "+"searchedBOMLine["+countI+"] = "+searchedBOMLine);
//                        countI++;
//                        boolean dupe = isDuplicate(searchedBOMLine, list);
//                        if(dupe==false){
//                       	 list.add(searchedBOMLine);
//                        }
//                 	}else{
//                 		isInCondition = false;
//                 		break;                 		
//                 	}
//                	 
// 				}
//
//			}
//
//        } catch(ServiceException serviceexception){
//            serviceexception.printStackTrace();
//        } catch(Exception exception){
//            exception.printStackTrace();
//        }finally{
//        	try {
//				structuresearchservice.stopSearch(structuresearchresultresponse.searchCursor);
//			} catch (ServiceException e) {
//				e.printStackTrace();
//			}
//        }
//        
//        
//        for (int j = 0; j < list.size(); j++) {
//        	
//        	TCComponentBOMLine searchedBOMLine = list.get(j);
//        	System.out.println(df.format(new Date())+" : "+"searchedBOMLine["+j+"] = "+searchedBOMLine);
//        	if(searchedBOMLine!=null){
//        		// 검색결과가 Connected Part로 유효한 것들만 결과 ItemRevision List에 담는다.
//        		TCComponentItemRevision itemRevision = getTargetAblePart(searchedBOMLine);
//        		System.out.println(df.format(new Date())+" : "+"## Item Revision = "+itemRevision);
//        		
//        		// 중복 없이 배열에 담는다.
//        		if(itemRevision!=null && searchedBOMLines.contains(itemRevision)==false){
//        			searchedBOMLines.add(itemRevision);
//        		}
//        		
////              	 // 검색결과를 25개로 제한 해 놓았으므로 25개 이상 검색결과를 전개하지 않는다.
////              	 if(resultLimit>0 && searchedBOMLines!=null && searchedBOMLines.size()>=resultLimit){
////              		 isInCondition = false;
////              		 break;
////              	 }
//        		
//        		if(searchedBOMLines.size()>=resultLimit){
//        			break;
//        		}
//        		
//        	}
//        	
//        }
//        	
//        // 검색된 근접 Part에 해당하는 BOMLine을 List에 담은것을 Return 한다.
//
//        return searchedBOMLines;
//    }
    
//    private boolean isDuplicate(TCComponentBOMLine tcComponentBOMLine, List<TCComponentBOMLine> list) {
//        int size = list.size();
//        String uid = tcComponentBOMLine.getUid();
//        
//        for (int i = 0; i < size; ++i) {
//            if (uid.equals(list.get(i).getUid())) {
//                return true;
//            }
//        }
//        return false;
//    }
    
//    /**
//     * Pert Chart를 통해 구성한 Pert의 순서를 기준으로 현재 선택된 Station 보다 선행하는 공정의 Station 목록을
//     * 가져온다.
//     * @param aTargetWeldPointBOMLine
//     * @return
//     */
//    private ArrayList<String> getPreStationIdList(TCComponentBOMLine aTargetWeldPointBOMLine){
//    	
//    	//------------------------------------------------------------
//    	// PERT 연결을 통한 이전 Station List 를 찾는다.
//    	//------------------------------------------------------------
//    	String shopId = null;
//    	String targetStationId = null;
//    	TCComponentBOPLine processTopBOMLine = null;
//		try {
//			processTopBOMLine = (TCComponentBOPLine)aTargetWeldPointBOMLine.window().getTopBOMLine();
//			shopId = processTopBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
//
//			TCComponentBOMLine stationBOMLine = findTypedBOMLine(aTargetWeldPointBOMLine, "M7_BOPStationRevision");
//			targetStationId = stationBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
//
//		} catch (TCException e) {
//			e.printStackTrace();
//		}
//        
//		ArrayList<String> preStationIdList = null;
//		
//		try {
//			preStationIdList = (ArrayList<String>) getDecessorsList(aTargetWeldPointBOMLine, shopId, targetStationId);
//			this.preDecessorBOMLineLists = getDecessorsBOMLineList(aTargetWeldPointBOMLine, shopId, targetStationId);
//		} catch (TCException e) {
//			e.printStackTrace();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		
//		return preStationIdList;
//    }
    
//    /**
//     * 현재 선택된 용접점이 속한 공정의 상위 공정 리스트를 가져온다
//     *
//     * @method getDecessorsList
//     * @date 2014. 3. 21.
//     * @param
//     * @return List<String>
//     * @exception
//     * @throws
//     * @see
//     */
//    @SuppressWarnings({ "unchecked", "rawtypes" })
//    private List<String> getDecessorsList(TCComponentBOMLine bomLine, String shopID, String stationID) throws Exception{
//    	
//    	// [NON-SR][20160128] Taeku.jeong PERT 연결정보를 이용해서 decessorsList 찾는 부분의 Query 수행 속도가
//    	// 너무 느려져서 검색 방법을 개선하는 일환으로 변경함.
//    	// 함수의 TCComponentBOMLine bomLine Argument 추가 했음.
//    	// 새로운 Code 추가 Start ------------ [20160128] 
//        BOPStructureDataUtility aBOPStructureDataUtility = new BOPStructureDataUtility();
//        aBOPStructureDataUtility.initBOMLineData(bomLine);
//        String lineId = aBOPStructureDataUtility.getLineId();
//        String findKeyCode = aBOPStructureDataUtility.getLatestKeyCodeForShop(shopID);
//        
//        System.out.println("Finded Key Code = "+findKeyCode);
//        
//        List<String> decessorsList = new ArrayList<String>();
//        List<HashMap> decessorsStationDataList  = aBOPStructureDataUtility.getPredecessorStationsAtAllLine(findKeyCode, lineId, stationID);
//        for (int i = 0; decessorsStationDataList!=null && i < decessorsStationDataList.size(); i++) {
//			//STATION_ID, APP_NODE_PUID
//        	HashMap rowData = decessorsStationDataList.get(i);
//        	String tempStatoinId = (String)rowData.get("STATION_ID");
//        	String tempStatoinIdAppNodePuid = (String)rowData.get("APP_NODE_PUID");
//        	decessorsList.add(tempStatoinId);
//		}
//        // 새로운 Code 추가 End ------------ [20160128]
//
//        return decessorsList;
//        
///*
//        SYMCRemoteUtil remoteUtil = new SYMCRemoteUtil();
//        List<String> decessorsList = new ArrayList<String>();
//
//        DataSet ds = new DataSet();
//        ds.put("SHOP_ID", shopID);
//        ds.put("STATION_ID", stationID);
//
//        ArrayList<HashMap<String, Object>> results;
//
//        String serviceClassName = "com.kgm.service.BopPertService";
//        results = (ArrayList<HashMap<String, Object>>) remoteUtil.execute(serviceClassName, "selectStationDecessorsList", ds);
//        for(HashMap result : results){
//            decessorsList.add((String) result.get("ID"));
//        }
//*/
//    }
    
//    private TCComponentBOMLine[] getDecessorsBOMLineList(TCComponentBOMLine bomLine, String shopID, String stationID) throws Exception{
//
//		BOPStructureDataUtility aBOPStructureDataUtility = new BOPStructureDataUtility();
//        aBOPStructureDataUtility.initBOMLineData(bomLine);
//        String lineId = aBOPStructureDataUtility.getLineId();
//        String findKeyCode = aBOPStructureDataUtility.getLatestKeyCodeForShop(shopID);
//        
//        Vector<String> findTargetAppNodePuidV = new Vector<String>();
//        List<HashMap> decessorsLineDataList = aBOPStructureDataUtility.getPredecessorLines(findKeyCode, lineId);
//        for (int i = 0; decessorsLineDataList!=null && i < decessorsLineDataList.size(); i++) {
//        	HashMap rowData = decessorsLineDataList.get(i);
//        	
//        	String tempLineId = (String)rowData.get("LINE_ID");
//        	String tempLineAppNodePuid = (String)rowData.get("APP_NODE_PUID");
//        	
//        	if(findTargetAppNodePuidV.contains(tempLineAppNodePuid)==false){
//        		findTargetAppNodePuidV.add(tempLineAppNodePuid);
//        	}
//		}
//        
//        List<HashMap> decessorsStationDataList  = aBOPStructureDataUtility.getPredecessorStationsAtAllLine(findKeyCode, lineId, stationID);
//        for (int i = 0; decessorsStationDataList!=null && i < decessorsStationDataList.size(); i++) {
//			//STATION_ID, APP_NODE_PUID
//        	HashMap rowData = decessorsStationDataList.get(i);
//        	String tempStatoinId = (String)rowData.get("STATION_ID");
//        	String tempStatoinIdAppNodePuid = (String)rowData.get("APP_NODE_PUID");
//        	
//        	if(findTargetAppNodePuidV.contains(tempStatoinIdAppNodePuid)==false){
//        		findTargetAppNodePuidV.add(tempStatoinIdAppNodePuid);
//        	}
//		}
//        
//        TCComponentBOMLine[] predecessorBOMLines = null;
//        Vector<TCComponentBOMLine> predecessorLineV = new Vector<TCComponentBOMLine>(); 
//        TCComponentBOMWindow window = bomLine.window();
//        
//        if(window!=null && findTargetAppNodePuidV!=null && findTargetAppNodePuidV.size()>0){
//        	
//        	for (int i = 0; i < findTargetAppNodePuidV.size(); i++) {
//        		String appNodePuid = findTargetAppNodePuidV.get(i);
//        		try {
//        			TCComponentBOMLine[] finded = window.findAppearance(appNodePuid);
//        			for (int j = 0; j < finded.length; j++) {
//						if(finded[j]!=null && predecessorLineV.contains(finded[j])==false){
//							predecessorLineV.add(finded[j]);
//						}
//					}
//        		} catch (TCException e) {
//        			e.printStackTrace();
//        		}
//			}
//        	
//    		int counts = 0;
//    		if(predecessorLineV!=null && predecessorLineV.size()>0){
//    			counts = predecessorLineV.size();
//    		}
//        	
//        	TCComponentBOMLine[] findedOperationBOMLines = null;
//    		TCComponentBOMLine topBOMLie = null;
//    		try {
//    			topBOMLie = window.getTopBOMLine();
//    			findedOperationBOMLines = window.findConfigedBOMLinesForAbsOccID(this.representTargetOperationId, true, topBOMLie);
//    			if(findedOperationBOMLines!=null){
//    				counts = findedOperationBOMLines.length+counts;
//    			}
//    		} catch (TCException e) {
//    			e.printStackTrace();
//    		}
//    		
//    		predecessorBOMLines = new TCComponentBOMLine[counts];
//        	int countIndex = 0;
//        	if(predecessorLineV!=null && predecessorLineV.size()>0){
//        		for (int i = 0; i < predecessorLineV.size(); i++) {
//        			predecessorBOMLines[countIndex] = predecessorLineV.get(i);
//        			countIndex++;
//				}
//        	}
//        	
//			for (int i = 0; i < findedOperationBOMLines.length; i++) {
//				predecessorBOMLines[countIndex] = findedOperationBOMLines[i];
//				countIndex++;
//			}
//        	
//        }
//        
//        return predecessorBOMLines;
//
//    }
    
//    /**
//     * 주어진 BOMLine을 상향 전개하면서 주어진 Type의 ItemRevision을 만나면 해당 ItemRevision의 BOMLine을 Return 한다.
//     * 이 함수의 주된 사용목적은 Structure Search를 통해 검색된 Part가 속한 Station을 찾을 목적으로 만들었다.
//     * 이 이외의 용도로도 유용한 함수임. 이미 유사한 Function이 있을듯 한데 찾지 못해서 private 함수로 정의함. 
//     * @param currentBOMLine
//     * @param ItemRevisionTypeName
//     * @return
//     */
//    private TCComponentBOMLine findTypedBOMLine(TCComponentBOMLine currentBOMLine, String ItemRevisionTypeName){
//    	
//    	if(currentBOMLine==null){
//    		return (TCComponentBOMLine)null;
//    	}
//    	
//    	String currentItemRevisionType = null;
//		try {
//			currentItemRevisionType = currentBOMLine.getItemRevision().getType();
//		} catch (TCException e) {
//			e.printStackTrace();
//		}
//		
//    	if(currentItemRevisionType!=null && currentItemRevisionType.trim().equalsIgnoreCase(ItemRevisionTypeName.trim())==true){
//    		return currentBOMLine;
//    	}else{
//
//    		TCComponentBOMLine parentBOMLine = null;
//			try {
//				parentBOMLine = currentBOMLine.parent();
//			} catch (TCException e) {
//				e.printStackTrace();
//			}
//
//			if(parentBOMLine!=null){
////				String objectType = null;
////				try {
////					objectType = parentBOMLine.getProperty(SDVPropertyConstant.BL_OBJECT_TYPE);
////				} catch (TCException e) {
////					e.printStackTrace();
////				}
////    			if(objectType!=null && objectType.trim().equalsIgnoreCase("OccurrenceGroup")==true){
////    				return (TCComponentBOMLine)null;	
////    			}else{
//    				return findTypedBOMLine(parentBOMLine, ItemRevisionTypeName);
////    			}
//    		}else{
//    			return (TCComponentBOMLine)null;
//    		}
//    	}
//
//    }
    
    static public TCComponentBOMLine findTypedItemBOMLine(TCComponentBOMLine currentBOMLine, String targetItemTypeName){
    	
    	String currentItemType = null;
		try {
			currentItemType = currentBOMLine.getItem().getType();
		} catch (TCException e) {
			e.printStackTrace();
		}
		
		//System.out.println(df.format(new Date())+" : "+"currentItemType = "+currentItemType+", targetItemTypeName = "+targetItemTypeName);
		
    	if(currentItemType.trim().equalsIgnoreCase(targetItemTypeName.trim())==true){
    		return currentBOMLine;
    	}else{

    		TCComponentBOMLine parentBOMLine = null;
			try {
				parentBOMLine = currentBOMLine.parent();
			} catch (TCException e) {
				e.printStackTrace();
			}

			if(parentBOMLine!=null){
    			return findTypedItemBOMLine(parentBOMLine, targetItemTypeName);
    		}else{
    			return (TCComponentBOMLine)null;
    		}
    	}

    }
    
}
