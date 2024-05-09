package com.kgm.commands.workflow.changetoreplace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import com.kgm.commands.ec.dao.CustomECODao;
import com.kgm.common.remote.DataSet;
import com.kgm.common.remote.SYMCRemoteUtil;
import com.kgm.common.utils.CustomUtil;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.SYMCBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentBOMWindowType;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentRevisionRule;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;

/**
 * [20160930][ymjang] Pack 된 라인 IN-ECO Update 오류 수정 (Unpack 후 하도록 재수정)
 */
/**
 * @author slobbie_vm
 * Cut and Paste가 발생한 건에 대해 Occ_Thread와 Order_NO를 Cut하기 전의 원래 값으로 되돌려 주는 기능.
 */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class ChangeToReplace {
	
	private String ecoNo;
	
	public ChangeToReplace(String ecoNo){
		this.ecoNo = ecoNo;
	}
	
	public TCComponentRevisionRule getRevisionRule(String ruleName) throws TCException {
		
		TCComponentRevisionRule atccomponentrevisionrule[] = TCComponentRevisionRule.listAllRules(CustomUtil.getTCSession());
		for (int i = 0; i < atccomponentrevisionrule.length; i++) {
			if (atccomponentrevisionrule[i].getProperty("object_string").equals(ruleName))
				return atccomponentrevisionrule[i];
		}
		
		return null;
	}
	
    public void execute() throws Exception{
		
		//Latest Released, Latest Working
		TCComponentBOMWindow releasedWindow = null, workingWindow = null;
		TCSession session = CustomUtil.getTCSession();
		TCComponentBOMWindowType winType = (TCComponentBOMWindowType) session.getTypeComponent("BOMWindow");

		ArrayList<String> fixedOccThreads = new ArrayList();
		HashMap<String, String> fixedParents = new HashMap();
		
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		DataSet ds = new DataSet();
		ds.put("ECO_NO", ecoNo);
		ArrayList<HashMap<String, String>> list = (ArrayList<HashMap<String, String>>) remote
				.execute(CustomECODao.ECO_INFO_SERVICE_CLASS, "getChangedOcc", ds);
		
		if (list != null && !list.isEmpty()) {
			
			String parentNo = null, preParentNo = null, parentRev = null;
			TCComponentBOMLine foundDeletedLine = null, foundAddedLine = null, releaseTopLine = null, workingTopLine = null;
			TCComponentItemRevision deletedParentRev = null, addedParentRev = null;
			
			for (HashMap<String, String> map : list) {
				try {
					parentNo = map.get("ADDED_PARENT_NO");
					parentRev = map.get("ADDED_PARENT_REV");
					
					// 기존 윈도우 재사용.
					if( !parentNo.equals(preParentNo)){
						
						closeWindows(releasedWindow, workingWindow);					
						
						deletedParentRev = CustomUtil.findItemRevision("ItemRevision", map.get("DELETED_PARENT_NO"), map.get("DELETED_PARENT_REV"));
						releasedWindow = winType.create(getRevisionRule("Latest Released"));
						releaseTopLine = releasedWindow.setWindowTopLine(null, deletedParentRev, null, null);
						
						addedParentRev = CustomUtil.findItemRevision("ItemRevision", map.get("ADDED_PARENT_NO"), map.get("ADDED_PARENT_REV"));
						workingWindow = winType.create(getRevisionRule("Latest Working"));
						workingTopLine = workingWindow.setWindowTopLine(null, addedParentRev, null, null);
						
						preParentNo = parentNo;
					}
					
					boolean isFound = false;
					TCComponentBOMLine[] packedLines = null;
					String deletedOccurrence = map.get("DELETED_OCC_THREAD");
					AIFComponentContext[] contexts = releaseTopLine.getChildren();
					for( int i = 0; !isFound && contexts != null && i < contexts.length; i++){
						TCComponentBOMLine child = (TCComponentBOMLine)contexts[i].getComponent();

						// [20160930][ymjang] Pack 된 라인 IN-ECO Update 오류 수정 (Unpack 후 하도록 재수정)
						boolean isPacked = child.isPacked();
				        if (isPacked) {
				        	packedLines = getUnpackBOMLines(child);
				        } else {
				        	packedLines = new TCComponentBOMLine[] { child };
				        }
				        /*
						if( child.isPacked()){
							packedLines = new TCComponentBOMLine[child.getPackedLines().length + 1];
							packedLines[0] = child;
							System.arraycopy(child.getPackedLines(), 0, packedLines, 1, child.getPackedLines().length);
						}else{
							packedLines = new TCComponentBOMLine[]{child};
						}
						*/
				        
						for( int j = 0; j < packedLines.length; j++){
							if( packedLines[j].getProperty("bl_occurrence_uid").equals(deletedOccurrence)){
								foundDeletedLine = packedLines[j];
								isFound = true;
								break;
							}
						}	
						
						if (isPacked) {
							child.pack();
				        }
						
					}			
					
					/*
					ArrayList<TCComponentBOMLine> deletedBOMLines = search(releaseTopLine, map.get("DELETED_PART_NO"));
					for(int i = 0; deletedBOMLines != null && i < deletedBOMLines.size(); i++){
						if( deletedBOMLines.get(i).getProperty("bl_occurrence_uid").equals(deletedOccurrence)){
							foundDeletedLine = deletedBOMLines.get(i);
							break;
						}
					}
					*/
					
					isFound = false;
					String addedOccurrence = map.get("ADDED_OCC_THREAD");
					contexts = workingTopLine.getChildren();
					for( int i = 0; !isFound && contexts != null && i < contexts.length; i++){
						TCComponentBOMLine child = (TCComponentBOMLine)contexts[i].getComponent();
						
						// [20160930][ymjang] Pack 된 라인 IN-ECO Update 오류 수정 (Unpack 후 하도록 재수정)
						boolean isPacked = child.isPacked();
				        if (isPacked) {
				        	packedLines = getUnpackBOMLines(child);
				        } else {
				        	packedLines = new TCComponentBOMLine[] { child };
				        }
												
						/*
						if( child.isPacked()){
							packedLines = new TCComponentBOMLine[child.getPackedLines().length + 1];
							packedLines[0] = child;
							System.arraycopy(child.getPackedLines(), 0, packedLines, 1, child.getPackedLines().length);
						}else{
							packedLines = new TCComponentBOMLine[]{child};
						}
						*/
						
						for( int j = 0; j < packedLines.length; j++){
							if( packedLines[j].getProperty("bl_occurrence_uid").equals(addedOccurrence)){
								foundAddedLine = packedLines[j];
								isFound = true;
								break;
							}
						}
						
						if (isPacked) {
							child.pack();
				        }
					}
					/*
					ArrayList<TCComponentBOMLine> addedBOMLines = search(workingTopLine, map.get("ADDED_PART_NO"));
					for(int i = 0; addedBOMLines != null && i < addedBOMLines.size(); i++){
						if( addedBOMLines.get(i).getProperty("bl_occurrence_uid").equals(addedOccurrence)){
							foundAddedLine = addedBOMLines.get(i);
							break;
						}
					}
					*/
					
					if( foundDeletedLine != null && foundAddedLine != null){
						String orderNo = foundDeletedLine.getProperty("bl_occ_int_order_no");
						SYMCBOMLine symcBomline = (SYMCBOMLine)foundAddedLine;
						symcBomline.setProperty("bl_occ_int_order_no", orderNo, false);
						workingWindow.save();
						foundDeletedLine.changeToReplace(foundDeletedLine, foundAddedLine);
						
						//C지(ECO_BOM_LIST table)의 정보를 업데이트함.
						ds.clear();
						ds.put("ECO_NO", ecoNo);
						ds.put("ADDED_OCC_THREAD", addedOccurrence);
						ds.put("DELETED_OCC_THREAD", deletedOccurrence);
						remote.execute(CustomECODao.ECO_INFO_SERVICE_CLASS, "updateOccthread", ds);
						
						fixedParents.put(parentNo, parentRev);
						fixedOccThreads.add(deletedOccurrence);
						
					}else{
						throw new TCException("Could not find 'Cut and Paste' BOM Lines.");
					}
					
				} catch (Exception e) {
					closeWindows(releasedWindow, workingWindow);
					throw e;
				} 
			}
			
			closeWindows(releasedWindow, workingWindow);
			
			//중복된 Order No 체크
			fixDuplicatedOrderNo(fixedParents, fixedOccThreads);
			
		}

	}
	
	private void fixDuplicatedOrderNo(HashMap<String, String> fixedParents, ArrayList<String> fixedOccThreads) throws Exception{
		
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		DataSet ds = new DataSet();
		
		Set<String> set = fixedParents.keySet();
		Object[] objects = set.toArray();
		if( objects != null && objects.length > 0){
			
			TCSession session = CustomUtil.getTCSession();
			TCComponentBOMWindowType winType = (TCComponentBOMWindowType) session.getTypeComponent("BOMWindow");
			TCComponentBOMWindow workingWindow = null;
			TCComponentBOMLine workingTopLine = null;
			TCComponentBOMLine[] packedLines = null; 
			String parentNo = null, preParentNo = null, parentRev = null;
			int maxOrderNO = -1;
			
			for(Object obj:objects){
				
				parentNo =  obj.toString();
				parentRev = fixedParents.get(obj);
				//Parent No별로 하위에 중복된 OrderNo리스트를 가져온다.
				ds.put("PARENT_NO", parentNo);
				ds.put("PARENT_REV", parentRev);
				ArrayList<HashMap<String, String>> list = (ArrayList<HashMap<String, String>>) remote
						.execute(CustomECODao.ECO_INFO_SERVICE_CLASS, "getDuplicatedOrderNoList", ds);	
				if( list != null && !list.isEmpty()){
					for( HashMap<String, String> map : list){
						
						//Cut and paste에 의한 보정이었으면 정상이므로 Skip.
						if( fixedOccThreads.contains( map.get("OCC_THREAD"))){
							continue;
						}
						
						try{
							if( !parentNo.equals(preParentNo)){
								
								closeWindows(null, workingWindow);
								TCComponentItemRevision revision = CustomUtil.findItemRevision("ItemRevision", parentNo, parentRev);
								workingWindow = winType.create(getRevisionRule("Latest Working"));
								workingTopLine = workingWindow.setWindowTopLine(null, revision, null, null);
								preParentNo = parentNo;
								maxOrderNO = getMaxOrderNo(parentNo, parentRev);
							}
							
							boolean isFound = false;
							AIFComponentContext[] contexts = workingTopLine.getChildren();
							for( int i = 0; !isFound && contexts != null && i < contexts.length; i++){
								TCComponentBOMLine child = (TCComponentBOMLine)contexts[i].getComponent();

								// [20160930][ymjang] Pack 된 라인 IN-ECO Update 오류 수정 (Unpack 후 하도록 재수정)
								boolean isPacked = child.isPacked();
						        if (isPacked) {
						        	packedLines = getUnpackBOMLines(child);
						        } else {
						        	packedLines = new TCComponentBOMLine[] { child };
						        }
																
								/*
								if( child.isPacked()){
									packedLines = new TCComponentBOMLine[child.getPackedLines().length + 1];
									packedLines[0] = child;
									System.arraycopy(child.getPackedLines(), 0, packedLines, 1, child.getPackedLines().length);
								}else{
									packedLines = new TCComponentBOMLine[]{child};
								}
								*/
								
								for( int j = 0; j < packedLines.length; j++){
									if( packedLines[j].getProperty("bl_occurrence_uid").equals(map.get("OCC_THREAD"))){
										SYMCBOMLine symcBomline = (SYMCBOMLine)packedLines[j];
										maxOrderNO += 10;
										symcBomline.setProperty("bl_occ_int_order_no", "" + maxOrderNO, false);
										isFound = true;
										break;
									}
								}								
								
								if (isPacked) {
									child.pack();
						        }
								
							}								
						}catch(Exception e){
							closeWindows(null, workingWindow);
							throw e;
						}
						
					}
					
					closeWindows(null, workingWindow);
				}
			}
		}
	}
	
	private int getMaxOrderNo(String parentNo, String parentRev) throws Exception{
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		DataSet ds = new DataSet();
		ds.put("PARENT_NO", parentNo);
		ds.put("PARENT_REV", parentRev);
		int orderNo = -1;
		String maxOrderNo = (String)remote.execute(CustomECODao.ECO_INFO_SERVICE_CLASS, "getMaxOrderNo", ds);
		return Integer.parseInt(maxOrderNo);
	}
	
	private void closeWindows(TCComponentBOMWindow releasedWindow, TCComponentBOMWindow workingWindow) throws TCException{
		if (releasedWindow != null) {
			releasedWindow.close();
		}

		if (workingWindow != null) {
			workingWindow.save();
			workingWindow.fireComponentChangeEvent();
			workingWindow.close();
		}
	}
	
    // [20160930][ymjang] Pack 된 라인 IN-ECO Update 오류 수정 (Unpack 후 하도록 재수정)
    public TCComponentBOMLine[] getUnpackBOMLines(TCComponentBOMLine packBOMLine) throws Exception {
        if (packBOMLine == null) {
            return null;
        }
        TCComponentBOMLine[] packedLines = packBOMLine.getPackedLines();
        TCComponentBOMLine[] unpackLines = new TCComponentBOMLine[packedLines.length + 1];
        System.arraycopy(packedLines, 0, unpackLines, 0, packedLines.length);
        packBOMLine.unpack();
        packBOMLine.refresh();
        packBOMLine.parent().refresh();
        unpackLines[unpackLines.length - 1] = packBOMLine;
        return unpackLines;
    }    
	
/*	
    public  ArrayList<TCComponentBOMLine> search(TCComponent scope, String itemId) throws Exception { 
        //ISearchEngine searchEngine = null;        
        //searchEngine = new ManufacturingSearchEngine();
        //searchEngine.initializeSearch(null);
        TCSession tcSession = scope.getSession();
        MFGSearchOperationParameters localMFGSearchOperationParameters = this.getSearchParameters(scope, itemId);
        if (!(localMFGSearchOperationParameters.needToSearch())) {
            throw new Exception("No_search_criteria_has_been_specified");            
        }
        String[] arrayOfString = this.getBOMLinesPropertiesToCache(scope.getSession());
        return performSearch(tcSession, localMFGSearchOperationParameters, arrayOfString);
    }
    
    protected String[] getBOMLinesPropertiesToCache(TCSession paramTCSession) {
        String[] arrayOfString = null;
        if (paramTCSession != null) {
            TCPreferenceService localTCPreferenceService = paramTCSession.getPreferenceService();
            arrayOfString = localTCPreferenceService.getStringArray(0, "MEAdvancedSearchResultsViewDisplayNameColumnsShownPref");
        }
        return arrayOfString;
    }
    
    private MFGSearchOperationParameters getSearchParameters(TCComponent scope, String itemId) throws Exception {
        double d = -1.0D;
        double[] arrayOfDouble = null;
        TCComponentBOMLine[] arrayOfTCComponentBOMLine = null;
        boolean bool = false;
        
        TCComponentQueryType queryType = (TCComponentQueryType) scope.getSession().getTypeComponent("ImanQuery");
		TCComponentQuery localTCComponentQuery = (TCComponentQuery) queryType.find("Item...");
        
        String[] arrayOfString1 = new String[] {"Item ID"};
        String[] arrayOfString2 = new String[] {itemId};        
        String[] arrayOfString3 = null;
        String[] arrayOfString4 = null;
        String[] arrayOfString5 = null;
//        Map localMap = this.generalCriteria.getNoteTypeNamesInfo();
//        if (this.generalCriteria.occurrenceNotesDialog != null) {
//            arrayOfString3 = this.generalCriteria.occurrenceNotesDialog.getOccurrenceNotesNames();
//            arrayOfString4 = this.generalCriteria.occurrenceNotesDialog.getOccurrenceNotesValues();
//            arrayOfString5 = this.generalCriteria.occurrenceNotesDialog.getOccurrenceNotesOperators();
//        }
        String[] arrayOfString6 = null;
//        if ((arrayOfString3 != null) && (arrayOfString3.length > 0)) {
//            arrayOfString6 = new String[arrayOfString3.length];
//            for (int i = 0; i < arrayOfString3.length; ++i)
//                arrayOfString6[i] = ((String) localMap.get(arrayOfString3[i]));
//        }
        Object[] arrayOfObject = null;
//        if (this.generalCriteria.fascDialog != null)
//            arrayOfObject = this.generalCriteria.fascDialog.getPseFormAttributeSearchCriteria();
        
        TCComponent[] arrayOfTCComponent = new TCComponent[] {scope};
        MFGSearchOperationParameters.CustomSearchOperationParameters localCustomSearchOperationParameters = null;
//        AbstractSearchCriteriaComposite localAbstractSearchCriteriaComposite = this.queryDefinitions.getCurrentShownCustomCriteria();
//        if (localAbstractSearchCriteriaComposite != null)
//            localCustomSearchOperationParameters = localAbstractSearchCriteriaComposite.getSearchCriteria();
        TCComponentBOMWindow localTCComponentBOMWindow = ((TCComponentBOMLine)scope).window();
        PSESearchInClassElement[] arrayOfPSESearchInClassElement = null;
        ICSProperty[] arrayOfICSProperty = null;
//        if (this.generalCriteria.inClassAttributeDialog != null) {
//            arrayOfPSESearchInClassElement = this.generalCriteria.inClassAttributeDialog.getInClassViewNames();
//            arrayOfICSProperty = this.generalCriteria.inClassAttributeDialog.getInClassAttributes();
//        }        
        return new MFGSearchOperationParameters(d, arrayOfTCComponentBOMLine, localTCComponentQuery, arrayOfString1, arrayOfString2, arrayOfString6, arrayOfString4, arrayOfString5, arrayOfPSESearchInClassElement, arrayOfICSProperty, arrayOfDouble, arrayOfObject, bool, arrayOfTCComponent, localCustomSearchOperationParameters, localTCComponentBOMWindow);
    }
    
    public ArrayList<TCComponentBOMLine> performSearch(TCSession tcSession, MFGSearchOperationParameters paramMFGSearchOperationParameters, String[] paramArrayOfString) throws TCException {
        //this.searchEngine.bomlinePropertiesToCache = paramArrayOfString;
        StructureSearch.SearchExpressionSet localSearchExpressionSet = new StructureSearch.SearchExpressionSet();
        StructureSearch.AttributeExpression[] arrayOfAttributeExpression = new StructureSearch.AttributeExpression[0];
        StructureSearch.ProximityExpression[] arrayOfProximityExpression = paramMFGSearchOperationParameters.buildProximityExpressions();
        StructureSearch.BoxZoneExpression[] arrayOfBoxZoneExpression = paramMFGSearchOperationParameters.buildBoxZoneExpressions();
        StructureSearch.SavedQueryExpression[] arrayOfSavedQueryExpression = paramMFGSearchOperationParameters.buildSavedQueryExpressions();
        StructureSearch.OccurrenceNoteExpression[] arrayOfOccurrenceNoteExpression = paramMFGSearchOperationParameters.buildOccurrenceNoteExpressions();
        StructureSearch.FormAttributeExpression[] arrayOfFormAttributeExpression = paramMFGSearchOperationParameters.buildFormAttributeExpressions();
        StructureSearch.PlaneZoneExpression[] arrayOfPlaneZoneExpression = new StructureSearch.PlaneZoneExpression[0];
        StructureSearch.InClassExpression[] arrayOfInClassExpression = paramMFGSearchOperationParameters.buildInClassExpressions();
        localSearchExpressionSet.itemAndRevisionAttributeExpressions = arrayOfAttributeExpression;
        localSearchExpressionSet.occurrenceNoteExpressions = arrayOfOccurrenceNoteExpression;
        localSearchExpressionSet.formAttributeExpressions = arrayOfFormAttributeExpression;
        localSearchExpressionSet.proximitySearchExpressions = arrayOfProximityExpression;
        localSearchExpressionSet.boxZoneExpressions = arrayOfBoxZoneExpression;
        localSearchExpressionSet.planeZoneExpressions = arrayOfPlaneZoneExpression;
        localSearchExpressionSet.savedQueryExpressions = arrayOfSavedQueryExpression;
        localSearchExpressionSet.inClassQueryExpressions = arrayOfInClassExpression;
        localSearchExpressionSet.doTrushapeRefinement = paramMFGSearchOperationParameters.isTrushapeFilterEnabled();
        TCComponent[] arrayOfTCComponent = paramMFGSearchOperationParameters.getScopes();        
        StructureSearch.MFGSearchCriteria localMFGSearchCriteria = paramMFGSearchOperationParameters.buildCustomMFGSearchCriteria();
        StructureSearchService searchService = StructureSearchService.getService(tcSession);
        StructureSearchResultResponse response = null;
        ArrayList<TCComponentBOMLine> searchArrayList = new ArrayList<TCComponentBOMLine>();
        try {
            response = searchService.startSearch(arrayOfTCComponent, localSearchExpressionSet, localMFGSearchCriteria);
            extractBOMlinesFromSearchResponse(response, searchArrayList);            
        } catch (Exception e) {
            e.printStackTrace();
        }
        while ((response != null) && (!(response.finished)))
            try {
                response = searchService.nextSearch(response.searchCursor);
                extractBOMlinesFromSearchResponse(response, searchArrayList);                
            } catch (Exception e) {
                e.printStackTrace();
            }        
        return searchArrayList;
    }
    
    private void extractBOMlinesFromSearchResponse(StructureSearch.StructureSearchResultResponse paramStructureSearchResultResponse, List<TCComponentBOMLine> paramList) {
        for (int i = 0; i < paramStructureSearchResultResponse.objects.length; ++i)
            paramList.add((TCComponentBOMLine) paramStructureSearchResultResponse.objects[i]);
    }	
*/    
}
