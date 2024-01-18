package com.ssangyong.soa.bop.util;

import java.util.ArrayList;

import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.services.strong.manufacturing._2010_09.Core;
import com.teamcenter.services.strong.manufacturing._2011_06.DataManagement.ContextGroup;
import com.teamcenter.services.strong.manufacturing._2011_06.DataManagement.OpenContextInfo;
import com.teamcenter.services.strong.manufacturing._2011_06.StructureManagement.ReferencedContexts;
import com.teamcenter.services.strong.manufacturing._2013_12.Model.AppearancePathResult;
import com.teamcenter.services.strong.manufacturing._2013_12.Model.ComputeAppearancePathResponse;
import com.teamcenter.services.strong.manufacturing._2013_12.Model.ComputeAppearancePathResult;
import com.teamcenter.services.strong.query.SavedQueryService;
import com.teamcenter.services.strong.query._2007_09.SavedQuery.QueryResults;
import com.teamcenter.services.strong.query._2007_09.SavedQuery.SavedQueriesResponse;
import com.teamcenter.services.strong.query._2008_06.SavedQuery.QueryInput;
import com.teamcenter.soa.client.Connection;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.ServiceData;
import com.teamcenter.soa.client.model.strong.BOMLine;
import com.teamcenter.soa.client.model.strong.BOMWindow;
import com.teamcenter.soa.client.model.strong.BOPWindow;
import com.teamcenter.soa.client.model.strong.ImanQuery;
import com.teamcenter.soa.client.model.strong.MECollaborationContext;
import com.teamcenter.soa.client.model.strong.MEPlantContext;
import com.teamcenter.soa.client.model.strong.MEProcessContext;
import com.teamcenter.soa.client.model.strong.MEProductContext;
import com.teamcenter.soa.client.model.strong.Mfg0BvrProcess;
import com.teamcenter.soa.client.model.strong.Mfg0BvrWorkarea;
import com.teamcenter.soa.client.model.strong.StructureContext;
import com.teamcenter.soa.exceptions.NotLoadedException;

/**
 * BOP 전개를 위해 BOP Window를 열고 활용하고 닫는데 필요한 Function들을 구현해놓은 Class임
 * [NON-SR][20160610] taeku.jeong 각종 BOP Report 개선을 위해 작성
 * @author Taeku
 *
 */
public class MppUtil {
	
	private Connection connection;
	
	public MppUtil(Connection connection){
		this.connection = connection;
	}
	
	private BOMLine initItemRevisionBasicInformation(BOMLine bomLine, String messageStr){
		
		BOMLine newBOMLine = bomLine;
		
		String[] targetPropertyNames = new String[]{"bl_item_item_id", "bl_rev_item_revision_id",
					"bl_rev_object_name", "bl_rev_object_type", "bl_rev_object_desc",
					"bl_occ_occurrence_type", "bl_occ_occurrence_name",
					"bl_occ_type", "bl_abs_occ_id", "bl_item_object_type"
				};
	
		String itemId = null;
		String itemRevId = null;
		String itemRevisionName = null;
		String itemRevisionDesc = null;
		String itemRevisionType = null;
		String itemObjectType = null;
		String occurrenceType = null;
		String occurrenceName = null;
		String occType = null;
		String absOccId = null;
		
		BasicSoaUtil basicSoaUtil = new BasicSoaUtil(connection);
		
		try {
			newBOMLine = (BOMLine) basicSoaUtil.readProperties(bomLine, targetPropertyNames);
			
			itemId = newBOMLine.get_bl_item_item_id();
			itemRevId = newBOMLine.get_bl_rev_item_revision_id();
			itemRevisionName = newBOMLine.get_bl_rev_object_name();
			itemRevisionDesc = newBOMLine.get_bl_rev_object_desc();
			itemRevisionType = newBOMLine.get_bl_rev_object_type();
			occurrenceType = newBOMLine.get_bl_occ_occurrence_type();
			occurrenceName = newBOMLine.get_bl_occ_occurrence_name();
			occType = newBOMLine.get_bl_occ_type();
			absOccId = newBOMLine.get_bl_abs_occ_id();
			itemObjectType = newBOMLine.get_bl_item_object_type();
			
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		if(messageStr==null || (messageStr!=null && messageStr.trim().length()<1)){
			messageStr = "";
		}
		
		System.out.println(messageStr+" "+itemId+"/"+itemRevId+" "+itemRevisionName+" ["+itemRevisionType+", "+itemObjectType+"] "+occurrenceName+", "+absOccId+" ["+occurrenceType+", "+occType+"]");
		
		return newBOMLine;
	}

	/**
	 * MPP에서 CC를 Open할때처럼 CollaborationContext 객체의 이름을 이용해 
	 * @param ccName
	 * @return
	 */
	public MECollaborationContext findMECollaborationContext(String ccName){
		
		String findedCollaborationContextUid = null;
		
		// General... Saved Query를 이용해서 CC를 찾는다.
        String queryName = "General...";
        String[] entries = { "Name",  "Type" };
        String[] values = { ccName, "MECollaborationContext" };
        
        QueryInput[] queryInput = new QueryInput[1];
        queryInput[0] = new QueryInput();
        queryInput[0].clientId= "site";
        queryInput[0].resultsType=2; 
        queryInput[0].entries = entries;
        queryInput[0].values = values;
        
        BasicSoaUtil basicSoaUtil = new BasicSoaUtil(connection);
        try {
			queryInput[0].query = basicSoaUtil.getImanQuery(queryName);
		} catch (Exception e) {
			e.printStackTrace();
		}

        
        SavedQueriesResponse executeSavedQueries = 
        		SavedQueryService.getService(connection).executeSavedQueries(queryInput);
        
        if(executeSavedQueries.serviceData.sizeOfPartialErrors() < 1){
        	
        	QueryResults[] queryresults = executeSavedQueries.arrayOfResults ;
        	
        	for (int i = 0;queryresults!=null && i < queryresults.length; i++) {
        		String[] objectUid = queryresults[i].objectUIDS;
        		for (int j = 0; j < objectUid.length; j++) {
					findedCollaborationContextUid = objectUid[j];
					break;
				}
			}
        	
        }
        
        MECollaborationContext aMECollaborationContext = null;
        if(findedCollaborationContextUid!=null && findedCollaborationContextUid.trim().length()>0){
        	aMECollaborationContext = new MECollaborationContext(null, findedCollaborationContextUid);
        }

        return aMECollaborationContext;
	}
	
	/**
	 * MECollaborationContext를 이용해 Plant, Process, Product의 Top BOMLine을 찾고 관련된 객체들을 모아 
	 * MPPTopLines 객체를 생성해서 Return 한다.
	 * 
	 * @param aMECollaborationContext
	 * @return
	 * @throws Exception
	 */
	 public MPPTopLines openCollaborationContext(MECollaborationContext aMECollaborationContext) throws Exception{
		 
		 MPPTopLines aMPPTopLines = null;
		 MEProductContext aMEProductContext = null;
		 MEProcessContext aMEProcessContext = null;
		 MEPlantContext aMEPlantContext = null;
		 
		BOMLine productLine = null;
		Mfg0BvrProcess processLine = null;
		Mfg0BvrWorkarea plantLine = null;
	    	
		 ModelObject[]  tempStructureContexts = null;
		 try {
			 tempStructureContexts = aMECollaborationContext.get_structure_contexts();
		 } catch (NotLoadedException e) {
			 throw e;
		 }
	
		 for (int i = 0; i < tempStructureContexts.length; i++) {
			 if(tempStructureContexts[i] instanceof MEProductContext){
				 aMEProductContext = (MEProductContext)tempStructureContexts[i];
				 productLine = (BOMLine)getContextTopBOMLine((StructureContext)aMEProductContext, "Product");
			 }else if(tempStructureContexts[i] instanceof MEProcessContext){
				 aMEProcessContext = (MEProcessContext)tempStructureContexts[i];
				 processLine = (Mfg0BvrProcess)getContextTopBOMLine((StructureContext)aMEProcessContext, "Process");
			 }else if(tempStructureContexts[i] instanceof MEPlantContext){
				 aMEPlantContext = (MEPlantContext)tempStructureContexts[i];
				 plantLine = (Mfg0BvrWorkarea)getContextTopBOMLine((StructureContext)aMEPlantContext, "Plant");
			 }
		}
		 
		 processLine = updateUpdatedReferenceInforamtionProcessLine(productLine, processLine, plantLine);
		 
		 if(productLine!=null || processLine!=null || plantLine!=null){
			 aMPPTopLines = new MPPTopLines(connection, productLine, processLine, plantLine);
			 aMPPTopLines.setMeCollaborationContext(aMECollaborationContext);
			 aMPPTopLines.setPlantContext(aMEPlantContext);
			 aMPPTopLines.setProcessContext(aMEProcessContext);
			 aMPPTopLines.setProductContext(aMEProductContext);
		 }

		 return aMPPTopLines;
	 }
	 
	 /**
	  * [NON-SR][2016.12.12] taeku.jeong
	  * Process의 Top BOMLine이 Product Top BOMLine과 Referenced 된 상태라야 Product의 자동 변경된 내용이 Process에 반영되어 보이는데
	  * 그렇게 되기위해서 setReferenceContexts() 함수를 통해 Reference를 설정하고 설정된 Process BOPLine을 Process의 TopLine으로 되게 처리후
	  * 결과를 Return 한다.
	  * @param productLine
	  * @param processLine
	  * @param plantLine
	  * @return
	  */
	 private Mfg0BvrProcess updateUpdatedReferenceInforamtionProcessLine(BOMLine productLine, Mfg0BvrProcess processLine, Mfg0BvrWorkarea plantLine){

		 Mfg0BvrProcess resultProcessLine = processLine;
		 BOPWindow srcBOPWindow = null;
			
		 BasicSoaUtil basicSoaUtil = new BasicSoaUtil(connection);
		 ReferencedContexts aReferencedContexts = new ReferencedContexts();
		 aReferencedContexts.context  = processLine;
		
		 boolean isContinuAble = true;
		 if(productLine!=null && plantLine!=null){
			 aReferencedContexts.addRefContexts = new ModelObject[]{productLine, plantLine};
		 }else if(productLine!=null && plantLine==null){
			 aReferencedContexts.addRefContexts = new ModelObject[]{productLine};
		 }else if(productLine==null && plantLine!=null){
			 aReferencedContexts.addRefContexts = new ModelObject[]{plantLine};
		 }else if(productLine==null && plantLine==null){
			 isContinuAble = false;
		 }
			
		 if(isContinuAble == false){
			 return resultProcessLine;
		 }
		 
		 try {
			 resultProcessLine = (Mfg0BvrProcess) basicSoaUtil.readProperties(resultProcessLine, new String[]{"bl_window"});
			 srcBOPWindow = (BOPWindow)resultProcessLine.get_bl_window();
		 } catch (Exception e) {
			 e.printStackTrace();
		 }
			
		 String srcUid = srcBOPWindow.getUid();
		 
		 ReferencedContexts[] referencedContextsList = new ReferencedContexts[]{aReferencedContexts};
		 ServiceData returnData = com.teamcenter.services.strong.manufacturing.StructureManagementService.getService(connection).setReferenceContexts(referencedContextsList);
			
		 BOPWindow changedBOPWindow = null;
		 int updateSize = returnData.sizeOfUpdatedObjects();
		 if(updateSize>0){
			 for (int i = 0; i < updateSize; i++) {
				 ModelObject aModelObject = returnData.getUpdatedObject(i);
				 if(aModelObject instanceof BOPWindow){
	        			
					 String targetUid = ((BOPWindow)aModelObject).getUid();
					 if(srcUid!=null && targetUid!=null && targetUid.trim().equals(srcUid.trim())){
						 changedBOPWindow = (BOPWindow)aModelObject;
						 break;
					 }
				 }
			 }
		 }
		 
		 if(changedBOPWindow!=null){
			 srcBOPWindow =  changedBOPWindow;
			 try {
				srcBOPWindow = (BOPWindow) basicSoaUtil.readProperties(srcBOPWindow, new String[]{"bl_window"});
				resultProcessLine = (Mfg0BvrProcess)srcBOPWindow.get_top_line();
			} catch (Exception e) {
				e.printStackTrace();
			}
		 }
			
		 return resultProcessLine;
		 
	 }
	 
	 private BOMLine getContextTopBOMLine(StructureContext structureContext, String contextType){
	
		 BOMLine topBOMLine = null;
		 
		 com.teamcenter.services.strong.manufacturing._2011_06.DataManagement.OpenContextInput openContextInput = 
				 new com.teamcenter.services.strong.manufacturing._2011_06.DataManagement.OpenContextInput();
		 openContextInput.object = structureContext;
		 openContextInput.openAssociatedContexts = true;
		 openContextInput.openViews = true;
		 
		 com.teamcenter.services.strong.manufacturing._2011_06.DataManagement.OpenContextsResponse openContextsResponse = null;
		 
		 openContextsResponse = com.teamcenter.services.strong.manufacturing.DataManagementService.getService(connection).openContexts(
				 new com.teamcenter.services.strong.manufacturing._2011_06.DataManagement.OpenContextInput[]{openContextInput});
		 
		 ContextGroup[] aContextGroup = openContextsResponse.output;
		 for (int i = 0;aContextGroup!=null && i < aContextGroup.length; i++) {
			 OpenContextInfo[] openContextInfos = aContextGroup[i].contexts;
			 
			 for (int j = 0;openContextInfos!=null && j < openContextInfos.length; j++) {
				 ModelObject tempModel = openContextInfos[j].context;
				 
				 String className = tempModel.getClass().getName();
				 System.out.println(contextType+" Top BOMLine Class : " + className);
				 
				 if(tempModel!=null && tempModel instanceof BOMLine){
					 topBOMLine = (BOMLine)tempModel;
					 break;
				 }
			}
			if(topBOMLine!=null){
				break;
			}
		}
		 
		 return topBOMLine;
	 }

	/**
	  * MECollaborationContext의 context를 Close한다.
	  * @param aMECollaborationContext
	  * @throws Exception
	  */
	 public void closeCollaborationContext(MECollaborationContext aMECollaborationContext) throws Exception{
		 
		 ModelObject[]  tempStructureContexts = null;
		 //BasicSoaUtil basicSoaUtil = new BasicSoaUtil(connection);
		 try {
			 //aMECollaborationContext = (MECollaborationContext) basicSoaUtil.readProperties(aMECollaborationContext, new String[]{"structure_contexts"});
			 tempStructureContexts = aMECollaborationContext.get_structure_contexts();
		 } catch (NotLoadedException e) {
			 e.printStackTrace();
		 }
		 
		 ServiceData serviceData = com.teamcenter.services.strong.manufacturing.DataManagementService.getService(connection).closeContexts(tempStructureContexts);
		 if(serviceData.sizeOfPartialErrors()>0){
			 throw new Exception(serviceData.getPartialError(0).getMessages()[0]);
		 }
	 }
	 
	 /**
	  * MPPTopLines에서 MECollaborationContext를 찾아 context를 Close한다.
	  * 이때 Window도 같이 닫힌다.
	  * @param mppTopLines
	  * @throws Exception
	  */
	 public void closeCollaborationContext(MPPTopLines mppTopLines) throws Exception{
		 if(mppTopLines==null){
			 return;
		 }
		 
		 MECollaborationContext meCollaborationContext = mppTopLines.getMeCollaborationContext();
		 closeCollaborationContext(meCollaborationContext);
	 }
	 
	 /**
	 * CC를 통해 연 Product, Process, Plant Window를 닫는다. 
	 * @param srcMPPTopLines
	 */
	public void closeMPPTopLineWindows(MPPTopLines srcMPPTopLines){
		
		if(srcMPPTopLines==null){
			return;
		}
	
		com.teamcenter.services.strong.manufacturing.StructureManagementService.
			getService(connection).closeAttachmentWindow(new BOMLine[]{
				srcMPPTopLines.productLine, 
				srcMPPTopLines.plantLine, 
				srcMPPTopLines.processLine
				});
	
	}

	/**
     * 주어진 BOMLine을 주어진 
     * @param searchTargetTopLine 검색 대상이 되는 Window의 Top BOMLine
     * @param findTargeBOMLine   대상 Window에서 검색 하려고 하는 BOMLine (ex. Product에서 찾기위하 Process의 BOMLine)
     * @return 검색된 BOMLine Array (ex. Product에서 찾은 Process에 할당된 Product BOMLine)
     */
    public ArrayList<BOMLine> findBOMLineFromTopNode(ModelObject searchTargetTopLine, BOMLine findTargeBOMLine){
    	
    	ArrayList<BOMLine> foundBOMLineList = new ArrayList<BOMLine>();
    	
    	com.teamcenter.services.strong.manufacturing._2013_05.Core.FindNodeInContextInputInfo findNodeInContextInputInfo =
    			new com.teamcenter.services.strong.manufacturing._2013_05.Core.FindNodeInContextInputInfo();
    	findNodeInContextInputInfo.allContexts = true;		// 전체 Context에서 검색 False이고 context가 지정되지 않은경우 Current Context에서 검색됨
    	findNodeInContextInputInfo.byIdOnly = false;			// True인경우 정확한 APN이 없는경우 동일한 Id 인것을 검색
    	findNodeInContextInputInfo.context = searchTargetTopLine;				// 검색대상의 Top Line
    	findNodeInContextInputInfo.inContextLine = null;	// 추가적인 검색 범위?
    	findNodeInContextInputInfo.nodes = new ModelObject[]{findTargeBOMLine};	// 검색의 Key에 해당하는 BOMLine들
    	findNodeInContextInputInfo.relationDepth = -1;		// -1인 경우 전체, 다른 숫자가 주어지는 경우 검색 Depth가 됨.
    	findNodeInContextInputInfo.relationDirection = 0;	// Relatoin의 방향을 지정 1(primary), 2(secondary) and 0(primary and secondary)
    	findNodeInContextInputInfo.relationTypes = new String[]{};	// 검색 대상인 Relation Type
    	
    	com.teamcenter.services.strong.manufacturing._2010_09.Core.FindNodeInContextResponse findNodeInContextResponse =
    			com.teamcenter.services.strong.manufacturing.CoreService.getService(connection).findNodeInContext(
    					new com.teamcenter.services.strong.manufacturing._2013_05.Core.FindNodeInContextInputInfo[]{findNodeInContextInputInfo}
    					);

		if(findNodeInContextResponse!=null){
			Core.FoundNodesInfo[] resultInfo = findNodeInContextResponse.resultInfo;
			for (int i = 0;resultInfo!=null && i < resultInfo.length; i++) {
				Core.NodeInfo[] resultNodes = resultInfo[i].resultNodes;
				for (int j = 0;resultNodes!=null && j < resultNodes.length; j++) {
					ModelObject[]  foundNodes = resultNodes[j].foundNodes;
					for (int k = 0;foundNodes!=null && k < foundNodes.length; k++) {
						if(foundNodes[k]!=null && foundNodes[k] instanceof BOMLine){
							BOMLine tempBOMLine  = (BOMLine)foundNodes[k];
							if(foundBOMLineList.contains(tempBOMLine)==false){
								foundBOMLineList.add(tempBOMLine);
							}
						}
					}
				}
			}
		}
		
		if(foundBOMLineList!=null && foundBOMLineList.size()<1){
			foundBOMLineList = null;
		}
    	return foundBOMLineList;
    }
    
    /**
     * 처음으로 찾은 검색방식으로 좀더 구체적인 조건이 포함된 방식으로 개선된방식을 Public Method로 구현했음.
     * 이 검색 방식은 참고를 위해 Private Method로 둔다.
     * @param searchTargetTopLine
     * @param findTargeBOMLine
     * @return
     */
    private ArrayList<BOMLine> findBOMLineFromTopNodeOldWay(ModelObject searchTargetTopLine, BOMLine findTargeBOMLine){
    	
    	ArrayList<BOMLine> foundBOMLineList = new ArrayList<BOMLine>();
    	
		com.teamcenter.services.strong.manufacturing._2010_09.Core.FindNodeInContextInputInfo aFindNodeInContextInputInfo = 
				new com.teamcenter.services.strong.manufacturing._2010_09.Core.FindNodeInContextInputInfo();
		aFindNodeInContextInputInfo.context = searchTargetTopLine;
		aFindNodeInContextInputInfo.nodes = new ModelObject[]{(ModelObject)findTargeBOMLine};
		aFindNodeInContextInputInfo.allContexts = true;
		
		com.teamcenter.services.strong.manufacturing._2010_09.Core.FindNodeInContextInputInfo[] aInfos = 
				new com.teamcenter.services.strong.manufacturing._2010_09.Core.FindNodeInContextInputInfo[]{aFindNodeInContextInputInfo};
		
		com.teamcenter.services.strong.manufacturing._2010_09.Core.FindNodeInContextResponse aFindNodeInContextResponse = com.teamcenter.services.strong.manufacturing.CoreService.getService(connection).findNodeInContext(aInfos);
		if(aFindNodeInContextResponse!=null){
			Core.FoundNodesInfo[] resultInfo = aFindNodeInContextResponse.resultInfo;
			
			for (int i = 0;resultInfo!=null && i < resultInfo.length; i++) {
				Core.NodeInfo[] resultNodes = resultInfo[i].resultNodes;
				for (int j = 0;resultNodes!=null && j < resultNodes.length; j++) {
					ModelObject[]  foundNodes = resultNodes[j].foundNodes;
					for (int k = 0;foundNodes!=null && k < foundNodes.length; k++) {
						if(foundNodes[k]!=null && foundNodes[k] instanceof BOMLine){
							BOMLine tempBOMLine  = (BOMLine)foundNodes[k];
							if(foundBOMLineList.contains(tempBOMLine)==false){
								foundBOMLineList.add(tempBOMLine);
							}
						}
					}
				}
			}
			
		}
		
		if(foundBOMLineList!=null && foundBOMLineList.size()<1){
			foundBOMLineList = null;
		}
		
		return foundBOMLineList;
    }
    
    /**
     * Structure Search 기능을 이용해 Data를 찾는 기능을 수행 한다.
     * 현재 양식만 갖춰진 상태이며 Test결과가 나오지 않는 상황임.
     * @param itemId
     * @param targetBOMLine
     * @param window
     * @return
     */
    public BOMLine[] findBOMLineUseStructureSearch(String itemId, BOMLine targetBOMLine, BOMWindow window){
    	
    	
    	
    	com.teamcenter.services.strong.structuremanagement._2008_05.StructureSearch.SearchScope searchScope =
    			new com.teamcenter.services.strong.structuremanagement._2008_05.StructureSearch.SearchScope();
    	searchScope.ignoreOccurrenceTypes = new String[]{};
    	searchScope.scopeBomLines = new BOMLine[]{targetBOMLine};
    	searchScope.window = window;	// 검색 대상인 BOM Window
    	
    	com.teamcenter.services.strong.structuremanagement._2010_09.StructureSearch.SearchExpressionSet searchExpressionSet =
    			new com.teamcenter.services.strong.structuremanagement._2010_09.StructureSearch.SearchExpressionSet();
    	
    	// collection of spatial box zone expressions
    	//----------------------------------------------------------------
    	//com.teamcenter.services.strong.structuremanagement._2008_05.StructureSearch.SearchBox searchBox = 
    	//		new com.teamcenter.services.strong.structuremanagement._2008_05.StructureSearch.SearchBox();
    	//searchBox.transform = new double[]{};
    	//searchBox.xmax = 0.0;
    	//searchBox.xmin = 0.0;
    	//searchBox.ymax = 0.0;
    	//searchBox.ymin = 0.0;
    	//searchBox.zmax = 0.0;
    	//searchBox.zmin = 0.0;
    	com.teamcenter.services.strong.structuremanagement._2008_05.StructureSearch.BoxZoneExpression boxZoneExpression = 
    			new com.teamcenter.services.strong.structuremanagement._2008_05.StructureSearch.BoxZoneExpression();
    	//boxZoneExpression.boxOperator = null;   // 'OR'...
    	//boxZoneExpression.searchBoxes = new com.teamcenter.services.strong.structuremanagement._2008_05.StructureSearch.SearchBox[]{searchBox};
    	searchExpressionSet.boxZoneExpressions = new com.teamcenter.services.strong.structuremanagement._2008_05.StructureSearch.BoxZoneExpression[0];
    	
    	// collection of Form attribute search expressions
    	//----------------------------------------------------------------
    	com.teamcenter.services.strong.structuremanagement._2008_05.StructureSearch.FormAttributeExpression formAttributeExpression = 
    			new com.teamcenter.services.strong.structuremanagement._2008_05.StructureSearch.FormAttributeExpression();
    	//formAttributeExpression.attributeName = null; //String (The attribute of the above Form type to be searched)
    	//formAttributeExpression.attributeType = null;	// String
    	//formAttributeExpression.formType = null;		// ModelObject (Form type to be searched)
    	//formAttributeExpression.isItemForm = false;	// boolean Item or ItemRevision form
    	//formAttributeExpression.queryOperator = null; //String (operator to use for search value comparison)
    	//formAttributeExpression.relationType = null;	// ModelObject   (The Form relation type)
    	//com.teamcenter.services.strong.structuremanagement._2008_05.StructureSearch.AttributeValues attributeValues = new com.teamcenter.services.strong.structuremanagement._2008_05.StructureSearch.AttributeValues();
    	//attributeValues.boolValues = new boolean[]{};
    	//attributeValues.dateValues = Calendar.getInstance();
    	//attributeValues.doubleValues = new double[]{};
    	//attributeValues.intValues = new int[]{};
    	//attributeValues.stringValues = new String[]{};
    	//attributeValues.tagValues = new ModelObject[]{};
    	//formAttributeExpression.values = attributeValues;
    	// FormAttributeExpression이 복수 인경우 "Or" 조건으로 묶인다.
    	searchExpressionSet.formAttributeExpressions = new com.teamcenter.services.strong.structuremanagement._2008_05.StructureSearch.FormAttributeExpression[0];
    	
    	// collection of inclass attribute search expressions
    	//----------------------------------------------------------------
    	com.teamcenter.services.strong.structuremanagement._2008_05.StructureSearch.InClassExpression inClassExpression = 
    			new com.teamcenter.services.strong.structuremanagement._2008_05.StructureSearch.InClassExpression();
    	//inClassExpression.inClassAttributeIDs = new int[]{};	// inClassAttributeIDs
    	//inClassExpression.inClassAttributeValues = new String[]{};		// inClassAttributeValues
    	//inClassExpression.inClassClassNames = new String[]{};
    	searchExpressionSet.inClassQueryExpressions = new com.teamcenter.services.strong.structuremanagement._2008_05.StructureSearch.InClassExpression[0];
    	
    	// collection of item and item revision attribute search expressions
    	//----------------------------------------------------------------
    	com.teamcenter.services.strong.structuremanagement._2008_05.StructureSearch.AttributeValues attributeValues = 
    			new com.teamcenter.services.strong.structuremanagement._2008_05.StructureSearch.AttributeValues();
    	//attributeValues.boolValues = new boolean[]{};
    	//attributeValues.dateValues = Calendar.getInstance();
    	//attributeValues.doubleValues = new double[]{};
    	//attributeValues.intValues = new int[]{};
    	attributeValues.stringValues = new String[]{itemId};
    	//attributeValues.tagValues = new ModelObject[]{};
    	
    	com.teamcenter.services.strong.structuremanagement._2008_05.StructureSearch.AttributeExpression attributeExpression = 
    			new com.teamcenter.services.strong.structuremanagement._2008_05.StructureSearch.AttributeExpression();
    	attributeExpression.attributeName = "Item ID";			// String		(DB에 기록되는 Column 이름과 같은형태로 보임 ,)
    	attributeExpression.attributeType = "StringType";		// "BooleanType", "DateType", "DoubleType", "IntegerType", "StringType", "TagType"
    	attributeExpression.className = "Item";					// Type Name String
    	attributeExpression.queryOperator = "Equal";			// "Equal","GreaterThan", "GreaterThanOrEqual", "IsNotNull", "IsNull", "LessThan", "LessThanOrEqual", "Like", "NotEqual", "NotLike"
    	attributeExpression.values = attributeValues;
    	searchExpressionSet.itemAndRevisionAttributeExpressions = new com.teamcenter.services.strong.structuremanagement._2008_05.StructureSearch.AttributeExpression[0];
    	
    	// collection of Occurrence Note attribute search expressions
    	//----------------------------------------------------------------
    	com.teamcenter.services.strong.structuremanagement._2008_05.StructureSearch.OccurrenceNoteExpression occurrenceNoteExpression = 
    			new com.teamcenter.services.strong.structuremanagement._2008_05.StructureSearch.OccurrenceNoteExpression();
    	//occurrenceNoteExpression.noteType = null;	// String (Occurrence note type to search)
    	//occurrenceNoteExpression.queryOperator = null;	// String (operator to use for search value comparison, "OR"...)
    	//occurrenceNoteExpression.values = new String[]{};	// String Array (The list of values to search for)
    	searchExpressionSet.occurrenceNoteExpressions = new com.teamcenter.services.strong.structuremanagement._2008_05.StructureSearch.OccurrenceNoteExpression[0];
    	
    	// collection of spatial plane zone search expressions
    	//----------------------------------------------------------------
    	//com.teamcenter.services.strong.structuremanagement._2008_05.StructureSearch.PlaneZone planeZone =
    	//		new com.teamcenter.services.strong.structuremanagement._2008_05.StructureSearch.PlaneZone();
    	//planeZone.displacement = 0.0;	// double (지정된 Vector의 방향에대해 떨어진 평면의 거리? )
    	//planeZone.xValue = 0.0; 	// X 평면에서 벡터의 방향 값?
    	//planeZone.yValue = 0.0; 	// Y 평면에서 벡터의 방향 값?
    	//planeZone.zValue = 0.0; 	// Z 평면에서 벡터의 방향 값?
    	com.teamcenter.services.strong.structuremanagement._2010_04.StructureSearch.PlaneZoneExpression planeZoneExpression = 
    			new com.teamcenter.services.strong.structuremanagement._2010_04.StructureSearch.PlaneZoneExpression();
    	//planeZoneExpression.planeZone = planeZone; 
    	searchExpressionSet.planeZoneExpressions = new com.teamcenter.services.strong.structuremanagement._2010_04.StructureSearch.PlaneZoneExpression[0];
    	
    	// collection of spatial proximity search expressions
    	// ----------------------------------------------------------------
    	com.teamcenter.services.strong.structuremanagement._2008_05.StructureSearch.ProximityExpression proximityExpression = 
    			new com.teamcenter.services.strong.structuremanagement._2008_05.StructureSearch.ProximityExpression();
    	//proximityExpression.bomLines = new BOMLine[]{};	// BOM lines around which to search
    	//proximityExpression.distance = 0.0;		// double (Proximity distance in metres from the outer surface of the BomLine geometry)
    	//proximityExpression.includeChildBomLines = true;	// includeChildBomLines
    	searchExpressionSet.proximitySearchExpressions = new com.teamcenter.services.strong.structuremanagement._2008_05.StructureSearch.ProximityExpression[0];
    	
    	// collection of saved query search expressions
    	// ----------------------------------------------------------------
    	BasicSoaUtil aBasicSoaUtil = new BasicSoaUtil(connection);
    	ImanQuery itemQuery = aBasicSoaUtil.getImanQuery("Item...");
    	com.teamcenter.services.strong.structuremanagement._2008_05.StructureSearch.SavedQueryExpression savedQueryExpression = 
    			new com.teamcenter.services.strong.structuremanagement._2008_05.StructureSearch.SavedQueryExpression();
    	savedQueryExpression.savedQuery = itemQuery;	// ModelObject (Tag of an existing saved query)
    	savedQueryExpression.entries = new String[]{};	// Attribute entries that are to be searched for
    	savedQueryExpression.values = new String[]{};		// Values of the above entries to be searched for
    	searchExpressionSet.savedQueryExpressions = new com.teamcenter.services.strong.structuremanagement._2008_05.StructureSearch.SavedQueryExpression[0];
    	
    	// collection of spatial size search expressions
    	// ----------------------------------------------------------------
    	com.teamcenter.services.strong.structuremanagement._2008_05.StructureSearch.SearchBySizeExpression searchBySizeExpression = 
    		new com.teamcenter.services.strong.structuremanagement._2008_05.StructureSearch.SearchBySizeExpression();
    	//searchBySizeExpression.diagonalLength = 0.0;	// 가장큰 대각선 방향의 거리
    	//searchBySizeExpression.largerThan = false;		// true 인 경우 설정값보다 큰것을 false인경우 설정값보다 작은것을 검색
    	searchExpressionSet.sizeSearchExpression = searchBySizeExpression;
    	
    	// boolean -------------------------------------------------------
    	searchExpressionSet.doTrushapeRefinement = false; 	// spatial search를 통해 공간검색을 할것인지 정의한다.
    	searchExpressionSet.executeVOOFilter = false;				// 서버에서 이전결과의 VOO 필터 적용 여부
    	searchExpressionSet.returnScopedSubTreesHit = true;	// 검색 결과에 포함된 BOMLine의 하위 검색 결과를 포함 할것인지 여부
    	
    	// 원격검색 --------------------------------------------------------
    	searchExpressionSet.remoteSiteID = null;		// 원격 서버 검색에 사용될 원격서버의 Id (선택)
    	searchExpressionSet.executeRemoteSearch = false;		// 원격 Site 검색 결과를 포함 할것인지 여부를 정의
    	
    	// Structure Search를 수행
    	com.teamcenter.services.strong.structuremanagement._2008_05.StructureSearch.StructureSearchResultResponse structureSearchResultResponse = 
    			new com.teamcenter.services.strong.structuremanagement._2008_05.StructureSearch.StructureSearchResultResponse();
    	com.teamcenter.services.strong.structuremanagement.StructureSearchService structureSearchService = null;
    	
    	try {
    		structureSearchService = com.teamcenter.services.strong.structuremanagement.StructureSearchService.getService(connection);
			structureSearchResultResponse = structureSearchService.startSearch(searchScope, searchExpressionSet);
		} catch (ServiceException e) {
			e.printStackTrace();
		}
    	
    	// 검색이 완료 될때까지 기다린다.
    	while(structureSearchResultResponse.finished==false){
    		System.out.println("Wait....");
    		try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			try {
				structureSearchResultResponse = structureSearchService.nextSearch(structureSearchResultResponse.searchCursor);
			} catch (ServiceException e1) {
				e1.printStackTrace();
			}
    	}
    	
    	if(structureSearchResultResponse==null){
    		System.out.println("Not Fount .........................................");
    		System.out.println("Not Fount .........................................");
    		System.out.println("Not Fount .........................................");
    		System.out.println("Not Fount .........................................");
    		return null;
    	}
    	
    	System.out.println("");
    	System.out.println("");
    	BOMLine[] bomLines = structureSearchResultResponse.bomLines;
    	int leftLines = structureSearchResultResponse.estimatedLinesLeft;
    	boolean finished = structureSearchResultResponse.finished;

    	if(bomLines!=null){
    		System.out.println("bomLines.length  = "+bomLines.length);	
    	}else{
    		System.out.println("bomLines : null");
    	}
    	System.out.println("leftLines  = "+leftLines);
    	System.out.println("finished  = "+finished);
    	
    	System.out.println("");
    	System.out.println("");
    	
    	
    	for (int i = 0;structureSearchResultResponse.bomLines!=null && i < structureSearchResultResponse.bomLines.length; i++) {
			System.out.println("Fount Fount Fount :::::::: "+structureSearchResultResponse.bomLines[i].toString());
		}

    	return structureSearchResultResponse.bomLines;
    	
    }
    
    
    
    /**
	 * 이거 나중에 Test 하고 확인 해보자.
	 * 
	 * 
	 * 
	 * @param topBOMLine
	 * @param threadIDs
	 */
	public void findAppearancePathNode(BOMLine topBOMLine, String[] threadIDs){
		
		System.out.println("------------------------");
		
    	com.teamcenter.services.strong.manufacturing._2013_12.Model.NodePath nodePath = 
    			new com.teamcenter.services.strong.manufacturing._2013_12.Model.NodePath();
    	nodePath.threadIDs = threadIDs;
		
    	com.teamcenter.services.strong.manufacturing._2013_12.Model.AppearancePathInput appearancePathInput =
    			new com.teamcenter.services.strong.manufacturing._2013_12.Model.AppearancePathInput();
    	appearancePathInput.parentObject = topBOMLine;
    	appearancePathInput.childPaths = new com.teamcenter.services.strong.manufacturing._2013_12.Model.NodePath[]{nodePath};
    	
    	ComputeAppearancePathResponse computeAppearancePathResponse = com.teamcenter.services.strong.manufacturing.ModelService.getService(connection).computeAppearancePath(appearancePathInput);
    	com.teamcenter.services.strong.manufacturing._2013_12.Model.ComputeAppearancePathResult[] results = 
    			computeAppearancePathResponse.results;
    	for (int i = 0; i < results.length; i++) {
    		
    		System.out.println("###");
    		
    		ComputeAppearancePathResult aResult = results[i];
    		BOMLine parent = (BOMLine) aResult.parentObject;
    		AppearancePathResult[] appResults = aResult.childResults;
    		for (int j = 0; j < appResults.length; j++) {
    			AppearancePathResult appResult = appResults[j];
    			String apnUID = appResult.apnUID;
    			String absOccUID = appResult.absOccUID;
    			
    			System.out.println("apnUID = "+apnUID+", absOccUID="+absOccUID);
			}
		}
	}

}
