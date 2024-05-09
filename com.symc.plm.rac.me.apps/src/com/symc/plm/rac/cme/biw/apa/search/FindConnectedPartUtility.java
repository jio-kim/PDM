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
	 * Product Window���� �˻� ����� BOM Line�� ���� �ϱ����� �ʿ��� BOMLine�� ã�Ƽ� Return �Ѵ�.
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
//	 * 2015/12/18 ���� Connected Part�� �˻������ 25���� ���� �ϰ� �ִµ� StructureSearch ����� 25������ �������
//	 * Display�� �� ���� ����� ���� Running Time�� �Ҹ����� �ʵ��� �ϱ����� ��ġ�� ���Ǵ� ���� ����
//	 * @param resultLimit
//	 */
//	public void setResultLimit(int resultLimit) {
//		this.resultLimit = resultLimit;
//	}
	
//	/**
//	 * Connected Part �˻��� ���� ���Ǵ� ������ �������� ���� �ϴ��� �����ϹǷ� �˻����ǿ� ���Ǵ� ��������
//	 * �ʱ�ȭ�Ѵ�.
//	 * @param weldPointBOMLines ����ڰ� ������ ���������� ���Ե� ������ �Ǵ� ������ 1�� ������ �������� List
//	 * @param revisionRule Structure Search�� ���� Revision Rule
//	 * @param proximityDistance �������� ������ Part�� ã���� ���Ǵ� ���� ���� �Ÿ� (������ m ��!)
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
//		// PERT�� ��ϵ� ������ �̿��� ���� Station Id List�� �����´�.
//	    this.preStationIdList = getPreStationIdList(representWeldPointBOMLine);
//	    this.preStationIdList.add(this.representTargetOperationId);
//	    
//	    for (int i = 0;this.preDecessorBOMLineLists!=null && i < this.preDecessorBOMLineLists.length; i++) {
//			System.out.println("@@@@ BOMLine["+i+"] = "+this.preDecessorBOMLineLists[i]);
//		}
//	    
//	}

//	/**
//	 * Structure Search�� ���� �־��� �������� ���� Connected Part�� �˻��ϰ� �� ����� Return�Ѵ�. 
//	 * @param targetWeldPointBOMLine �˻� ������ �Ǵ� ������
//	 * @return
//	 */
//    public ArrayList<TCComponentItemRevision> getStructureSearchResultRevisionList(
//    		TCComponentBOMLine targetWeldPointBOMLine){
//    	
//    	System.out.println("STR Search  "+targetWeldPointBOMLine+" ["+df.format(new Date())+" (Start)]");
//    	
//    	/*
//    	 * 1. Connected Part �˻��� Structure Search�� ����Ѵ�.
//    	 * 2. Structure Search�� �˻������� ������ ���� ���ѵǾ�� �Ѵ�.
//    	 *     a. �˻��� ����� �Ǵ� BOMLine ( ex : Root Node)
//    	 *     b. Revision Rule
//    	 *     c. �˻� ������ BOM Line
//    	 *     d. �˻� ����� ���Ե� Item Type���� Saved Query
//    	 *     e. �˻� ������ �Ǵ� �Ÿ� (Structure Search�� m ������ �Է� �޾Ƽ� �˻���)
//    	 *     f. Test�� ���ؼ��� ���� ������ ���ο� �ΰ� MPP���� Structure Search�� �غ��� �ȴ�.
//    	 * 3. �˻� ����߿� Assembly Item�� Connected Part ����� �ƴ�
//    	 *     -- ������ ���� ��� ���� ������ �߰��Ǳ⸦ ����ڴ� �����.
//    	 *     a. �����ϴٸ� Part�� �̸� �������� ������ �̸��� �ִ°�� Part No�� �������� ����� �����ϰ� �������� ���Խ�Ű�� �ʴ´�.
//    	 *     b. Part No�� �������� ���Ͻ� �� �� �ִ� Part�� �˻� ����� ���� ��Ű�� �ʴ´�.
//    	 * 4. �˻� ��� Part�� �˻� ������ �Ǿ��� �������� Target Operation�� �Ҵ�� Part�� �����Ѵ�.
//    	 * 5. �˻� ��� Part�� PERT ���� ���������� ���� �˻� ���� �������� ���Ե� Station�� ���� Statoin�� ���Ե� Part�� �����
//    	 * 6. �˻� ������� Operation�� �Ҵ�� Part�� ���� Part���� ���ԵǾ�� �Ѵ�. 
//    	 */
//    	
//    	// �˻� ������ �Ǵ� ������ BOMLine
//		TCComponentBOMLine[] targetBOMLines = new TCComponentBOMLine[]{
//				targetWeldPointBOMLine
//		};
//		
//		// �˻� ����� Item Type�� �����Ѵ�.
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
//		// �˻� ����� ���Ե� Part Type�� �����ϴ� Saved Query ��ü�� ���� �Ѵ�.
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
//		boolean isIncludeChildBomLines = true;         // �˻��� Part�� Child Node���� BOMLine�� �����Ѵ�.
//		
//		isReturnScopedSubTreesHit = false;
//		isIncludeChildBomLines = false;         // �˻��� Part�� Child Node���� BOMLine�� �����Ѵ�.
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
//    	// ���� Structure Search�� �����ϴ� Function ȣ��
//    	searchedItemRevision =  getFindedConnectedPartBOMLines(searchexpressionset, targetWeldPointBOMLine);
//
//    	// �˻� ����� ���Ե� Part Item Revision�� ������ �ֿܼ� ��´�.
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
//     * �־��� BOMLine�� ItemRevision�� Connected Part�� ������ Data���� Ȯ�� �ϰ� ������ ������ �Ǻ��Ǹ� Item Revision�� Return �Ѵ�.
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
//    	// �켱 �˻��� Part�� Parent�� OperaionBOMLine ���� Ȯ�� �ؾ� �Ѵ�.
//    	// ���õ� �������� ���Ե� ���������� Target Operation�� ã�� �̿� ���Ե� Child Node�� Part��
//    	// �˻��� Connected Part�� ���� ��� ���Եǵ��� �Ѵ�.
//    	try {
//			TCComponentBOMLine parentBOMLine = currentFindedPartBOMLine.parent();
//			String parentItemRevisionType = parentBOMLine.getItemRevision().getType();
//			if(parentItemRevisionType!=null && parentItemRevisionType.trim().equalsIgnoreCase("M7_BOPBodyOpRevision")==true){
//    			try {
//    				String operationId = parentBOMLine.getItem().getProperty("item_id");
//    				if(operationId!=null && operationId.trim().equalsIgnoreCase(this.representTargetOperationId)==true){
//    		    		// ���õ� WeldOperation�� Target Operation�̹Ƿ� ����� ���� ��Ų��.
//    		    		isConsiderationTarget = true;
//						System.out.println(df.format(new Date())+" : "+"���⼭ True ----1");
//    		    	}
//    			} catch (TCException e1) {
//    				e1.printStackTrace();
//    			}
//			}
//		} catch (TCException e2) {
//			e2.printStackTrace();
//		}
//    	
//    	// Target Operation�� Child Node�� �ƴϸ� PERT�� ���Ե� Part�� ���� ����� �ǵ��� �ؾ� �Ѵ�.
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
//    			// ���õ� WeldOperation�� Target Operation�̹Ƿ� ����� ���� ��Ų��.
//    			isConsiderationTarget = true;        	
//    		}
//    	}
//
//    	// Connected Part�� �߰��� ����� �ƴѰ�� null�� Return�Ѵ�.
//    	if(isConsiderationTarget==false){
//    		return (TCComponentItemRevision)null;
//    	}
//    	
//    	// Part Id�� Ȯ���ؼ� Connected Part�� ���� ��� Part ���� �����Ѵ�.
//   		// Assembly Part ���� ����...
//    	String partId = null;
//    	TCComponentItemRevision searchedRevision = null;
//    	try {
//			searchedRevision = currentFindedPartBOMLine.getItemRevision();
//			partId = currentFindedPartBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
//		} catch (TCException e) {
//			e.printStackTrace();
//		}
//    	
//        //[SR140611-032][20140611] jwlee �����ǳ� Dialog�� ǥ�õǴ� ������ �˻���󿡼� Assay Item�� ���� (Part ID�� �ټ���° �ڸ��� 0�̸� Assay Item�̴� �̰͵��� �˻���󿡼� ���� ó����)
//        if (partId.length() > 5) {
//            if (partId.charAt(4) == '0') {
//            	searchedRevision = null;
//            }
//        }
//        
//        // Connected Part �Ǻ��� �߰����� ������ ������ �Ʒ� �κп� �߰� ���� �Ѵ�.
//        
//        return searchedRevision;
//    }


//    /**
//     * Structure Search�� ���ؼ� �����Ÿ��� �ִ� Part���� ã�´�.
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
//        // �������� ����
//        com.teamcenter.services.rac.structuremanagement._2008_05.StructureSearch.ProximityExpression aproximityexpression[] = psesearchoperationparameters.buildProximityExpressions();
//        com.teamcenter.services.rac.structuremanagement._2008_05.StructureSearch.BoxZoneExpression aboxzoneexpression[] = psesearchoperationparameters.buildBoxZoneExpressions();
//        com.teamcenter.services.rac.structuremanagement._2008_05.StructureSearch.SavedQueryExpression asavedqueryexpression[] = psesearchoperationparameters.buildSavedQueryExpressions();
//        com.teamcenter.services.rac.structuremanagement._2008_05.StructureSearch.OccurrenceNoteExpression aoccurrencenoteexpression[] = psesearchoperationparameters.buildOccurrenceNoteExpressions();
//        com.teamcenter.services.rac.structuremanagement._2008_05.StructureSearch.FormAttributeExpression aformattributeexpression[] = psesearchoperationparameters.buildFormAttributeExpressions();
//        com.teamcenter.services.rac.structuremanagement._2010_04.StructureSearch.PlaneZoneExpression aplanezoneexpression[] = new com.teamcenter.services.rac.structuremanagement._2010_04.StructureSearch.PlaneZoneExpression[0];
//        com.teamcenter.services.rac.structuremanagement._2008_05.StructureSearch.InClassExpression ainclassexpression[] = new com.teamcenter.services.rac.structuremanagement._2008_05.StructureSearch.InClassExpression[0];
//        
//        // -----------------------------------------------------------------------
//    	// �˻������� ���� Process�� �����Ѵ�.
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
////			// Search Scop ���
////			searchscope.scopeBomLines = this.preDecessorBOMLineLists; 
////		}else{
//			searchscope.scopeBomLines = atccomponentbomline;
////		}
//        
//        System.out.println(df.format(new Date())+" : "+"bomWindowTopBOMLine = "+bomWindowTopBOMLine);
//        
//        // -----------------------------------------------------------------------
//    	// �˻����� ���� �Ϸ�
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
//        // �˻� ����� ������ ���� ����
//        ArrayList<TCComponentItemRevision> searchedBOMLines = new ArrayList<TCComponentItemRevision>();
//        com.teamcenter.services.rac.structuremanagement._2008_05.StructureSearch.StructureSearchResultResponse structuresearchresultresponse = null;
//
//        // --------------------------------------------------        
//        // Structure Search �� ���� �Ѵ�.
//        // --------------------------------------------------
//        ArrayList<TCComponentBOMLine> list = new ArrayList<TCComponentBOMLine>();
//        try {
//            structuresearchresultresponse = structuresearchservice.startSearch(searchscope, searchexpressionset);
//            
//            // �˻���� ���� Part�� BOMLine ������ Ȯ���ϱ����� While Loop�� ������ �ʱ�ȭ �Ѵ�.
//            boolean isInCondition = true;
//            int countI = 0;
//            while (isInCondition == true) {
//            	
//            	structuresearchresultresponse = structuresearchservice.nextSearch(structuresearchresultresponse.searchCursor);
//            	
//            	// �˻� ����� ���̻� ���ų� �˻���������� ����Ǵ� ��� While Loop�� �����Ѵ�.
//            	if(structuresearchresultresponse == null || structuresearchresultresponse.finished){
//            		isInCondition = false;
//            		break;
//            	}
//            	 
//            	// �˻������ ���Ե� ���� Part BOMLine�� �迭�� ��´�.
//                 for (int i = 0;structuresearchresultresponse!=null && i < structuresearchresultresponse.bomLines.length; i++) {
//                	 
//                 	// �˻� ����� ���̻� ���ų� �˻���������� ����Ǵ� ��� While Loop�� �����Ѵ�.
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
//        		// �˻������ Connected Part�� ��ȿ�� �͵鸸 ��� ItemRevision List�� ��´�.
//        		TCComponentItemRevision itemRevision = getTargetAblePart(searchedBOMLine);
//        		System.out.println(df.format(new Date())+" : "+"## Item Revision = "+itemRevision);
//        		
//        		// �ߺ� ���� �迭�� ��´�.
//        		if(itemRevision!=null && searchedBOMLines.contains(itemRevision)==false){
//        			searchedBOMLines.add(itemRevision);
//        		}
//        		
////              	 // �˻������ 25���� ���� �� �������Ƿ� 25�� �̻� �˻������ �������� �ʴ´�.
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
//        // �˻��� ���� Part�� �ش��ϴ� BOMLine�� List�� �������� Return �Ѵ�.
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
//     * Pert Chart�� ���� ������ Pert�� ������ �������� ���� ���õ� Station ���� �����ϴ� ������ Station �����
//     * �����´�.
//     * @param aTargetWeldPointBOMLine
//     * @return
//     */
//    private ArrayList<String> getPreStationIdList(TCComponentBOMLine aTargetWeldPointBOMLine){
//    	
//    	//------------------------------------------------------------
//    	// PERT ������ ���� ���� Station List �� ã�´�.
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
//     * ���� ���õ� �������� ���� ������ ���� ���� ����Ʈ�� �����´�
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
//    	// [NON-SR][20160128] Taeku.jeong PERT ���������� �̿��ؼ� decessorsList ã�� �κ��� Query ���� �ӵ���
//    	// �ʹ� �������� �˻� ����� �����ϴ� ��ȯ���� ������.
//    	// �Լ��� TCComponentBOMLine bomLine Argument �߰� ����.
//    	// ���ο� Code �߰� Start ------------ [20160128] 
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
//        // ���ο� Code �߰� End ------------ [20160128]
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
//     * �־��� BOMLine�� ���� �����ϸ鼭 �־��� Type�� ItemRevision�� ������ �ش� ItemRevision�� BOMLine�� Return �Ѵ�.
//     * �� �Լ��� �ֵ� �������� Structure Search�� ���� �˻��� Part�� ���� Station�� ã�� �������� �������.
//     * �� �̿��� �뵵�ε� ������ �Լ���. �̹� ������ Function�� ������ �ѵ� ã�� ���ؼ� private �Լ��� ������. 
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
