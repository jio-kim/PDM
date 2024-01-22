package com.teamcenter.ets.translator.ugs.weldpointexport;

import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import com.teamcenter.ets.soa.SoaHelper;
import com.teamcenter.services.strong.cad.StructureManagementService;
import com.teamcenter.soa.client.Connection;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.Property;
import com.teamcenter.soa.client.model.strong.BOMLine;
import com.teamcenter.soa.client.model.strong.BOMWindow;
import com.teamcenter.soa.client.model.strong.Item;
import com.teamcenter.soa.client.model.strong.ItemRevision;
import com.teamcenter.soa.client.model.strong.PSOccurrence;
import com.teamcenter.soa.client.model.strong.PSOccurrenceThread;
import com.teamcenter.soa.client.model.strong.RevisionRule;
import com.teamcenter.tstk.util.log.ITaskLogger;

/**
 * Weld Group Item Revision을 생성하는 과정에 이전 Revision Data를 DB에 저장하고 이를 이용해 새로 생성될
 * Data를 만드는데 사용하는데 이과정에 필요한 이전 Revision Data를 저장하기위해 기존의 BOM Structure를
 * 저장하기위해 필요한 기능을 구현 해놓은 Class
 * [NON-SR][20160503] Taeku.Jeong
 * @author Taeku
 *
 */
public class StructureExpander {
	
	ITaskLogger m_zTaskLogger;
	StringBuffer buffer;
	boolean isDebug = false;
	
	public StructureExpander(ITaskLogger m_zTaskLogger, StringBuffer buffer, boolean isDebug){
		this.m_zTaskLogger = m_zTaskLogger;
		this.buffer = buffer;
		this.isDebug = isDebug;
	}
	
	private void addLog(String msg){
		if( isDebug ){
			m_zTaskLogger.info(msg);
			buffer.append(msg);
			buffer.append("\r\n");
		}
	}
	
	/**
	 * Weld Group BOMWindow를 이용해 이전 Version인 Weld Group Item Revision의 Data를 전개하고 저장하는 기능을 수행한다. 
	 * @param connection
	 * @param servletUrlStr
	 * @param partItemId
	 * @param partItemRevId
	 * @param weldGroupItemRevision
	 * @throws Exception
	 */
	public void saveWeldGroupLatistRevisionData(Connection connection, String servletUrlStr, String partItemId, String partItemRevId, ItemRevision weldGroupItemRevision) throws Exception{
		
		if(weldGroupItemRevision==null){
			return;
		}
		
		SoaHelper.getProperties(weldGroupItemRevision, new String[]{"item_id", "item_revision_id"});
		String currentWeldGroupItemId = weldGroupItemRevision.get_item_id();
		String currentWeldGroupItemRevisionId = weldGroupItemRevision.get_item_revision_id();
		
		String wledGroupEcoId = null;
		SoaHelper.getProperties(weldGroupItemRevision, new String[]{"item_id","item_revision_id","s7_ECO_NO"});
		ItemRevision wledGroupEcoRevision = (ItemRevision) weldGroupItemRevision.getPropertyObject("s7_ECO_NO").getModelObjectValue();
		if(wledGroupEcoRevision!=null){
			SoaHelper.getProperties(wledGroupEcoRevision, new String[]{"item_id"});
			wledGroupEcoId = wledGroupEcoRevision.get_item_id();
		}

		RevisionRule rule = Util.getRevisionRule(connection, "Latest Released");
		Date date = new Date();
		
		BOMWindow window = Util.createTopLineBOMWindow(connection, weldGroupItemRevision, rule, date);
		if(window==null){
			return;
		}

		SoaHelper.getProperties(window, "top_line");
		BOMLine weldGroupBOMLine = (BOMLine)window.get_top_line();
				
		// WeldGroup BOMLine의 Child Node를 전개한다.
		SoaHelper.getProperties(weldGroupBOMLine, new String[]{"bl_child_lines"});
		ModelObject[] weldGroupChildObjects = weldGroupBOMLine.get_bl_child_lines();
				
		Vector<WeldInformation> weldInformationV = new Vector<WeldInformation>();
		for( ModelObject childNodeModelObject : weldGroupChildObjects){
			
			BOMLine weldPointLine = (BOMLine)childNodeModelObject;
			if(weldPointLine!=null){

				String occurrenceName = null;
				String occurrenceUid = null;
				String occurrenceThreadUid = null;
				
				SoaHelper.getProperties(weldPointLine, new String[]{"bl_real_occurrence"});
				ModelObject realOccurrenceModelObject = weldPointLine.get_bl_real_occurrence();
				if(realOccurrenceModelObject!=null){
							
					PSOccurrence realOccurrence = (PSOccurrence)realOccurrenceModelObject;
					SoaHelper.getProperties(realOccurrence, new String[]{"occurrence_name", "occ_thread"});
					occurrenceName = realOccurrence.get_occurrence_name();
					occurrenceUid = realOccurrence.getUid();
					PSOccurrenceThread occurrenceThread = realOccurrence.get_occ_thread();
					if(occurrenceThread!=null){
						occurrenceThreadUid = occurrenceThread.getUid();
					}

//					addLog("occurrenceName = "+occurrenceName);
//					addLog("occurrenceUid = "+occurrenceUid);
//					addLog("occurrenceThreadUid = "+occurrenceThreadUid);

				}
				
				WeldInformation aWeldInformation = getWeldInformationFromBOMLine(servletUrlStr, partItemId, partItemRevId, weldPointLine);
				if(aWeldInformation!=null){
					weldInformationV.add(aWeldInformation);
				}
			}
		}
		
		if(weldInformationV!=null && weldInformationV.size()>0){
			WeldPointDataManager aWeldPointDataManager = new WeldPointDataManager(m_zTaskLogger, buffer, isDebug);
			aWeldPointDataManager.savePreRevisionBOMStructureData(weldInformationV ,
					partItemId, partItemRevId, wledGroupEcoId,
					weldGroupItemRevision);
		}
		
		if( window != null){
			StructureManagementService.getService(connection).closeBOMWindows(new BOMWindow[]{window});
		}
	}
	
	private WeldInformation getWeldInformationFromBOMLine(String servletUrlStr, String partItemId, String partItemRevId, BOMLine weldPointLine) throws Exception{

		
		WeldInformation aWeldInformation = new WeldInformation(
					m_zTaskLogger, buffer, isDebug, 
					servletUrlStr, partItemId, partItemRevId, weldPointLine );
		
		addLog(aWeldInformation.toString());
		
		return aWeldInformation;
	}

	/**
	 * FMP를 기준으로 주어진 Weld Group을 전개하는 Function
	 * @param connection
	 * @param functionRevision
	 * @param weldGroupRevision
	 * @param date
	 * @throws Exception
	 */
/*
	public void expandWeldGroupAtFunctrionBOMLine(Connection connection 
			, ItemRevision functionRevision
			, ItemRevision weldGroupRevision
			, Date date
			) throws Exception {
		

		BOMWindow window = null;
		
		SoaHelper.getProperties(functionRevision, new String[]{"item_id"});
		String functionId = functionRevision.get_item_id();
		
		SoaHelper.getProperties(weldGroupRevision, new String[]{"item_id"});
		String weldGroupID = weldGroupRevision.get_item_id();
		
		addLog("===============================================\n"
				+ "= Expand\n"
				+ "===============================================\n"
				+ "= FunctionId : "+functionId+", weldGroupID : "+weldGroupID+"\n"
				+ "===============================================\n");
		
		try {
			
			RevisionRule rule = Util.getRevisionRule(connection, "Latest Released");
			window = Util.createTopLineBOMWindow(connection, functionRevision, rule, date);
			
			SoaHelper.getProperties(window, "top_line");
			BOMLine functionLine = (BOMLine)window.get_top_line();
			
			BOMLine weldGroupLine = null;
			
			//weldGroupLine = findBOMLine(functionLine, "5060036000-WeldGroup");
			
			addLog("-------------------- Find 1[S]");
			structureSearch(connection, window, functionLine );
			addLog("-------------------- Find 1[E]");
			
//			addLog("-------------------- Find 2");
//			String findTargetItemType = "S7_Vehpart";
//			String findTargetItemId = "5015036000-WeldGroup"; //"5060036000-WeldGroup"
//
//			BOMLine weldGroupLine = null;
//			BOMLine[] findedBOMLines = structureSearch( connection, window,  functionLine, findTargetItemType, findTargetItemId );
//			if(findedBOMLines!=null){
//				addLog("findedBOMLines : "+findedBOMLines.length);
//			}else{
//				addLog("findedBOMLines : 0");
//			}
//
//			for (int i = 0; findedBOMLines!=null && i < findedBOMLines.length; i++) {
//				printBOMLine("Finded BOMLine ["+i+"] = ", findedBOMLines[i]);
//				if(findedBOMLines[i]!=null){
//					weldGroupLine = findedBOMLines[i];
//				}
//			}

			if(weldGroupLine!=null){
				
				printBOMLine("WeldGroup ", weldGroupLine);
				
				// WeldGroup Item의 Child Node를 전개한다.
				SoaHelper.getProperties(weldGroupLine, new String[]{"bl_child_lines"});
				ModelObject[] weldGroupChildObjects = weldGroupLine.get_bl_child_lines();
				
				int wpIndex = 0;
				for( ModelObject childObj : weldGroupChildObjects){
					
					BOMLine weldPointLine = (BOMLine)childObj;
					
					SoaHelper.getProperties(weldPointLine, new String[]{"bl_item"});
					Item weldPointItem = (Item)weldPointLine.get_bl_item();
	
					SoaHelper.getProperties(weldPointItem, new String[]{"item_id"});
					String weldPointItemId = weldPointItem.get_item_id();
					
					printBOMLine("Weld Points["+wpIndex+"] ", weldPointLine);
				}
				
			}else{
//				SoaHelper.getProperties(functionLine, new String[]{"bl_child_lines"});
//				ModelObject[] weldGroupChildObjects = functionLine.get_bl_child_lines();
//				for (int i = 0; i < weldGroupChildObjects.length; i++) {
//					BOMLine weldGroupBOMLine = (BOMLine)weldGroupChildObjects[i];
//					printBOMLine("Weld Points["+i+"] ", weldGroupBOMLine);
//				}
			}
			
			//Util.save(connection, window);

		}catch(Exception e){
			throw e;
		}finally{
			if( window != null){
				StructureManagementService.getService(connection).closeBOMWindows(new BOMWindow[]{window});
			}
		}
		
	}
*/	
	
	/**
	 * FMP를 기준으로 주어진 Child Node를 전개해서 Weld Group Item Revision을 찾기위해 만든 Function
	 * @param connection
	 * @param parentItemRevision
	 * @param weldGroupRevision
	 * @param date
	 * @throws Exception
	 */
	public static boolean haveSameChildNode(Connection connection 
			, ItemRevision parentItemRevision
			, String targetItemId ) throws Exception {

		boolean isExist = false;
		BOMWindow window = null;
		try {
			
			Date date = new Date();
			RevisionRule rule = Util.getRevisionRule(connection, "Latest Released");
			window = Util.createTopLineBOMWindow(connection, parentItemRevision, rule, date);
			
			SoaHelper.getProperties(window, "top_line");
			BOMLine topBOMLine = (BOMLine)window.get_top_line();
			
			if(topBOMLine!=null){

				// Child Node를 전개한다.
				SoaHelper.getProperties(topBOMLine, new String[]{"bl_child_lines"});
				ModelObject[] childNodeObjects = topBOMLine.get_bl_child_lines();
				
				for( ModelObject childObj : childNodeObjects){
					
					BOMLine childBOMLine = (BOMLine)childObj;
					
					SoaHelper.getProperties(childBOMLine, new String[]{"bl_item"});
					Item childItem = (Item)childBOMLine.get_bl_item();
					if(childItem!=null){
						SoaHelper.getProperties(childItem, new String[]{"item_id"});
						String childItemId = childItem.get_item_id();
						if( childItemId!=null && childItemId.trim().equalsIgnoreCase(targetItemId.trim())==true ){
							isExist = true;
							break;
						}
					}
					
				}
			}			
		}catch(Exception e){
			throw e;
		}finally{
			if( window != null){
				StructureManagementService.getService(connection).closeBOMWindows(new BOMWindow[]{window});
			}
		}
		
		return isExist;
	}
	
	/**
	 * BOMLine 정보를 확인하기위해 Log와 Console에 기록하는 함수.
	 * @param message
	 * @param bomLine
	 */
	private void printBOMLine(String message, BOMLine bomLine){
		
		/*
		bl_item_object_type
		bl_item_item_id
		bl_item_item_revision
		bl_rev_item_revision
		bl_rev_item_revision_id
		bl_occurrence_name
		bl_plmxml_occ_xform
		bl_plmxml_abs_xform
		bl_rev_object_name
		bl_sequence_no
		M7_FEATURE_NAME
		*/
		
		try {
			
			SoaHelper.getProperties(bomLine, new String[]{
					"bl_item_item_id", "bl_rev_item_revision_id",
					"bl_rev_object_type", "bl_rev_object_name",
					"bl_sequence_no", "bl_occurrence_name",
					"bl_item","bl_revision",
					"bl_occ_xform", "bl_occ_xform_matrix", 
					"bl_abs_xform_matrix", "bl_absocc_uid_in_topline_context",
					"bl_appearance_path_node", "bl_real_occurrence"
				});
			
			Hashtable<String, Property> propertyHasy = bomLine.copyProperties();
			
			//PSOccurrenceThread
			//Appearance
			
			if(propertyHasy==null){
				addLog(message+" BOMLine : ----- null");
				return;
			}
			
			String itemId = null;
			Property itemIdProperty = propertyHasy.get("bl_item_item_id");
			if(itemIdProperty!=null){
				itemId = itemIdProperty.getDisplayableValue();
			}

			String itemRevId = null;
			Property itemRevIdProperty = propertyHasy.get("bl_rev_item_revision_id");
			if(itemRevIdProperty!=null){
				itemRevId = itemRevIdProperty.getDisplayableValue();
			}

			String itemRevType = null;
			Property itemRevTypeProperty = propertyHasy.get("bl_rev_object_type");
			if(itemRevTypeProperty!=null){
				itemRevType = itemRevTypeProperty.getDisplayableValue();
			}
			
			String itemRevName = null;
			Property itemRevNameProperty = propertyHasy.get("bl_rev_object_name");
			if(itemRevNameProperty!=null){
				itemRevName = itemRevNameProperty.getDisplayableValue();
			}
			
			String sequenceNoStr = null;
			Property sequenceNoProperty = propertyHasy.get("bl_sequence_no");
			if(sequenceNoProperty!=null){
				sequenceNoStr = sequenceNoProperty.getDisplayableValue();
			}
			
			Item item = null;
			Property itemProperty = propertyHasy.get("bl_item");
			if(itemProperty!=null){
				item = (Item)itemProperty.getModelObjectValue();
			}
			
			ItemRevision itemRevision = null;
			Property itemRevisionProperty = propertyHasy.get("bl_revision");
			if(itemRevisionProperty!=null){
				itemRevision = (ItemRevision)itemRevisionProperty.getModelObjectValue();
			}
			
			String occurrenceName = null;
			String occurrenceUid = null;
			String occurrenceThreadUid = null;

			ModelObject realOccurrenceModelObject = bomLine.get_bl_real_occurrence();
			if(realOccurrenceModelObject!=null){
						
				PSOccurrence realOccurrence = (PSOccurrence)realOccurrenceModelObject;
				SoaHelper.getProperties(realOccurrence, new String[]{"occurrence_name", "occ_thread"});
				occurrenceName = realOccurrence.get_occurrence_name();
				occurrenceUid = realOccurrence.getUid();
				PSOccurrenceThread occurrenceThread = realOccurrence.get_occ_thread();
				if(occurrenceThread!=null){
					occurrenceThreadUid = occurrenceThread.getUid();
				}

				addLog("occurrenceName = "+occurrenceName);
				addLog("occurrenceUid = "+occurrenceUid);
				addLog("occurrenceThreadUid = "+occurrenceThreadUid);
			}
			
//			Enumeration<String> keys = propertyHasy.keys();
//			while (keys.hasMoreElements()) {
//				String keyString = (String) keys.nextElement();
//				Property aProperty = propertyHasy.get(keyString);
//				
//				String valueString = aProperty.getDisplayableValue();
//
//				addLog("## "+keyString+" = "+valueString);
//			}
			
			String occXForm = bomLine.get_bl_occ_xform();
			String occXFormMatrix = bomLine.get_bl_occ_xform_matrix();
			String absOccXFormMatrix = bomLine.get_bl_abs_xform_matrix();
			String absOccUidInTopLineContext = bomLine.get_bl_absocc_uid_in_topline_context();
			
			addLog(message+" BOMLine : "+itemId+"/"+itemRevId+"["+itemRevType+"] "+itemRevName+" ("+occurrenceName+") "+occXForm+" | "+occXFormMatrix+" | "+absOccXFormMatrix+" | "+absOccUidInTopLineContext);
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
//	public static BOMWindow getWindow(Connection connection, ItemRevision itemRevision, Date date){
//			
//			BOMWindow window = null;
//			try {
//				RevisionRule rule = Util.getRevisionRule(connection, "Latest Released");
//				window = Util.createTopLineBOMWindow(connection, itemRevision, rule, date);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//				
//			return window;
//	}
	
//	public static BOMLine findBOMLine(BOMLine functionLine, String targetItemId ){
//		
//		BOMLine findedBOMLine = null;
//		
//		try {
//			SoaHelper.getProperties(functionLine, new String[]{"bl_child_lines"});
//			ModelObject[] weldGroupBOMLineObjects = functionLine.get_bl_child_lines();
//			for (int i = 0; i < weldGroupBOMLineObjects.length; i++) {
//				
//				BOMLine tempBOMLine = (BOMLine)weldGroupBOMLineObjects[i];
//				
//				SoaHelper.getProperties(tempBOMLine, new String[]{"bl_item"});
//				Item tempItem = (Item)tempBOMLine.get_bl_item();
//
//				SoaHelper.getProperties(tempItem, new String[]{"item_id"});
//				String tempItemId = tempItem.get_item_id();
//				
//				if(tempItemId!=null && tempItemId.trim().length()>0){
//					if(tempItemId.trim().equalsIgnoreCase(targetItemId)==true){
//						findedBOMLine = tempBOMLine;
//						break;
//					}
//				}
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		
//		return findedBOMLine;
//	}
	
/*
	private ModelObject getSavedQuery(Connection connection) throws ServiceException{
		
        ModelObject query = null;

        // Get the service stub
        SavedQueryService queryService = SavedQueryService.getService(connection);
        GetSavedQueriesResponse aGetSavedQueriesResponse = queryService.getSavedQueries();
        
        for (int i = 0;aGetSavedQueriesResponse!=null && aGetSavedQueriesResponse.queries!=null && i < aGetSavedQueriesResponse.queries.length; i++) {
        	if (aGetSavedQueriesResponse.queries[i].name.equals("Item...")==true){
        		query = aGetSavedQueriesResponse.queries[i].query;
        		break;
        	}
		}
        
//        try{
//        	// Search for all Items, returning a maximum of 25 objects
//        	SavedQuery.SavedQueryInput[] savedQueryInput = new SavedQuery.SavedQueryInput[1];
//        	savedQueryInput[0] = new SavedQuery.SavedQueryInput();
//        	savedQueryInput[0].query = query;
//        	savedQueryInput[0].maxNumToReturn = 25;
//        	savedQueryInput[0].limitListCount = 0;
//        	savedQueryInput[0].limitList = new ModelObject[0];
//        	savedQueryInput[0].entries = new String[]{"Item Name" };
//        	savedQueryInput[0].values = new String[1];
//        	savedQueryInput[0].values[0] = "*";
//        	savedQueryInput[0].maxNumToInflate = 25;
//
//        	//*****************************
//        	//Execute the service operation
//        	//*****************************
//        	com.teamcenter.services.loose.query._2007_06.SavedQuery.ExecuteSavedQueriesResponse savedQueryResult = queryService.executeSavedQueries(savedQueryInput);
//        	com.teamcenter.services.loose.query._2007_06.SavedQuery.SavedQueryResults found = savedQueryResult.arrayOfResults[0];
//
//        	System.out.println("");
//        	System.out.println("Found Items:");
        	
//        } catch (Exception e) {
//        	
//        	System.out.println("ExecuteSavedQuery service request failed.");
//        	System.out.println(e.getMessage());
//        	return;
//        }

        return query;

	}
	*/

	/**
	 * Structure에서 원하는 BOMLine을 바로 찾아오는 기능을 수행 하고자 Test 했으나 Argument를 잘못 입력한 것인지 결과가 원하는데로
	 * 검색되어 나오지 않음. 추후에 다시한번 확인 해 볼 필요가 있겠다.
	 * 2016-05-03 Taeku.jeong
	 * @param connection
	 * @param window
	 * @param functionLine
	 */
/*
		private void structureSearch(Connection connection, BOMWindow window, BOMLine functionLine ){
		
			StructureSearchRestBindingStub a = new StructureSearchRestBindingStub(connection);
			
			//StructureSearch.SearchExpressionSet
			SearchScope  aSearchScope  = new StructureSearch.SearchScope();
			aSearchScope.window = window;
			aSearchScope.scopeBomLines = new BOMLine[]{functionLine};
			
			ImanQuery itemFindQuery = null;
			try {
				itemFindQuery = Util.getQueryObject(connection, "Item...");
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			
			ModelObject query = null;
			try {
				query = getSavedQuery(connection);
			} catch (ServiceException e1) {
				e1.printStackTrace();
			}
			
//			if(query==null){
//				addLog("SavedQuery is null");
//			}else{
//				addLog("SavedQuery is Not null");
//			}
			
			SavedQueryExpression qSavedQueryExpression = new SavedQueryExpression();
			//qSavedQueryExpression.savedQuery = query;
			qSavedQueryExpression.savedQuery = itemFindQuery;
			
//			qSavedQueryExpression.entries= new String[]{"object_type", "item_id"};
//			qSavedQueryExpression.values= new String[]{"S7_Vehpart", "5015036000-WeldGroup"};
			
//			qSavedQueryExpression.entries= new String[]{"ItemID"};	// item_id -> Item ID or ItemID, object_type -> Type
//			qSavedQueryExpression.values= new String[]{"5015036000-WeldGroup"};
			
//			qSavedQueryExpression.entries= new String[]{"object_type"};
//			qSavedQueryExpression.values= new String[]{"S7_Vehpart"};
			
			SavedQueryExpression[] queryExpressions = new SavedQueryExpression[1];
			queryExpressions[0] = qSavedQueryExpression;
			
			StructureSearch.AttributeValues values = new StructureSearch.AttributeValues();
			values.stringValues = new String[]{"501*"};
			
			StructureSearch.AttributeExpression tempExpression = new StructureSearch.AttributeExpression();
			tempExpression.className = "Item";
			tempExpression.attributeType="StringType";
			tempExpression.queryOperator="Equal";
			tempExpression.attributeName = "Item ID";
			tempExpression.values =  values;
			
			StructureSearch.AttributeExpression[] aAttributeExpression  = new StructureSearch.AttributeExpression[1];
			aAttributeExpression[0] = tempExpression;
			
			com.teamcenter.services.strong.structuremanagement._2008_05.StructureSearch.SearchExpressionSet aSearchExpressionSet = new com.teamcenter.services.strong.structuremanagement._2008_05.StructureSearch.SearchExpressionSet();
			aSearchExpressionSet.doTrushapeRefinement = false;
			aSearchExpressionSet.returnScopedSubTreesHit = true;
			aSearchExpressionSet.savedQueryExpressions = queryExpressions;
			aSearchExpressionSet.itemAndRevisionAttributeExpressions = aAttributeExpression;
			
			com.teamcenter.services.strong.structuremanagement._2008_05.StructureSearch.StructureSearchResultResponse aR = null;
			try {
				aR = a.startSearch(aSearchScope, aSearchExpressionSet);
			} catch (ServiceException e) {
				addLog("@ Search Error : "+e.toString());
				e.printStackTrace();
			}
			if(aR!=null){
				addLog("@@@@@@@@@@@@@@@@");
				addLog("@ BOMLine Search @");
				addLog("@@@@@@@@@@@@@@@@");
				
				BOMLine[] bomlines = null;
				if(aR.bomLines!=null){
					bomlines = aR.bomLines;
				}
				
				if(bomlines!=null){
					addLog("@ Search Result Count : "+bomlines.length);
					
					for (int i = 0;bomlines!=null && i < bomlines.length; i++) {
						printBOMLine("@ Finded BOMLine ["+i+"] = ", bomlines[i]);
					}
				}else{
					addLog("@ Result BOMLines are not found!!");
				}
			}

	}
*/

	/**
  	 * Structure에서 원하는 BOMLine을 바로 찾아오는 기능을 수행 하고자 Test 했으나 Argument를 잘못 입력한 것인지 결과가 원하는데로
	 * 검색되어 나오지 않음. 추후에 다시한번 확인 해 볼 필요가 있겠다.
	 * 2016-05-03 Taeku.jeong
	 */
/*
	private BOMLine[] structureSearch(Connection connection, BOMWindow window, BOMLine functionLine, String targetItemType, String targetItemId ){
		
		addLog("Find targetItemType = "+targetItemType);
		addLog("Find targetItemId = "+targetItemId);
		
		BOMLine[] bomlines = null;

		// Structure Search Scop 정의
		SearchScope  searchScope  = new StructureSearch.SearchScope();
		searchScope.window = window;
		searchScope.scopeBomLines = new BOMLine[]{functionLine};
		
		// Structure Search에 사용될 Query 정의
		SavedQueryExpression qSavedQueryExpression = new SavedQueryExpression();
		ImanQuery itemFindQuery = null;
		try {
			itemFindQuery = Util.getQueryObject(connection, "Item...");
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		qSavedQueryExpression.savedQuery = itemFindQuery;
//		qSavedQueryExpression.entries= new String[]{"object_type", "item_id"};
//		qSavedQueryExpression.values= new String[]{targetItemType, targetItemId};
		qSavedQueryExpression.entries= new String[]{"item_id"};
		qSavedQueryExpression.values= new String[]{targetItemId};
		SavedQueryExpression[] queryExpressions = new SavedQueryExpression[]{qSavedQueryExpression};
		
		// Structure Search를 수행한다.
		com.teamcenter.services.strong.structuremanagement._2008_05.StructureSearch.SearchExpressionSet searchExpressionSet = 
				new com.teamcenter.services.strong.structuremanagement._2008_05.StructureSearch.SearchExpressionSet();
		searchExpressionSet.returnScopedSubTreesHit = true;
		searchExpressionSet.savedQueryExpressions = queryExpressions;
		
		StructureSearchRestBindingStub structureSearchRestBindingStub = new StructureSearchRestBindingStub(connection);
		com.teamcenter.services.strong.structuremanagement._2008_05.StructureSearch.StructureSearchResultResponse aR = null;
		try {
			aR = structureSearchRestBindingStub.startSearch(searchScope, searchExpressionSet);
			bomlines = aR.bomLines;
			
			for (int i = 0;bomlines!=null && i < bomlines.length; i++) {
				printBOMLine("@@@@@@Finded : ", bomlines[i]);
			}
		} catch (ServiceException e) {
			e.printStackTrace();
		}
		

		
		return bomlines;
	}
*/
}
