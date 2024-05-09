package com.teamcenter.ets.translator.ugs.weldpointexport;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Clob;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.kgm.common.remote.DataSet;
import com.teamcenter.ets.soa.SoaHelper;
import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.services.internal.loose.core.ICTService;
import com.teamcenter.services.internal.loose.core._2011_06.ICT.Arg;
import com.teamcenter.services.internal.loose.core._2011_06.ICT.InvokeICTMethodResponse;
import com.teamcenter.services.internal.strong.structuremanagement.VariantManagementService;
import com.teamcenter.services.internal.strong.structuremanagement._2011_06.VariantManagement;
import com.teamcenter.services.internal.strong.structuremanagement._2011_06.VariantManagement.ModularOptionsForBomResponse;
import com.teamcenter.services.internal.strong.structuremanagement._2011_06.VariantManagement.ModularOptionsInput;
import com.teamcenter.services.loose.core._2007_01.DataManagement.WhereReferencedResponse;
import com.teamcenter.services.strong.cad.StructureManagementRestBindingStub;
import com.teamcenter.services.strong.cad.StructureManagementService;
import com.teamcenter.services.strong.cad._2007_01.StructureManagement.AttributesInfo;
import com.teamcenter.services.strong.cad._2007_01.StructureManagement.CreateBOMWindowsInfo;
import com.teamcenter.services.strong.cad._2007_01.StructureManagement.CreateBOMWindowsResponse;
import com.teamcenter.services.strong.cad._2007_01.StructureManagement.CreateOrUpdateRelativeStructureResponse;
import com.teamcenter.services.strong.cad._2007_01.StructureManagement.RelativeStructureChildInfo;
import com.teamcenter.services.strong.cad._2007_12.StructureManagement.CreateOrUpdateRelativeStructureInfo2;
import com.teamcenter.services.strong.cad._2007_12.StructureManagement.CreateOrUpdateRelativeStructurePref2;
import com.teamcenter.services.strong.cad._2007_12.StructureManagement.DeleteRelativeStructureInfo2;
import com.teamcenter.services.strong.cad._2007_12.StructureManagement.DeleteRelativeStructurePref2;
import com.teamcenter.services.strong.core.DataManagementRestBindingStub;
import com.teamcenter.services.strong.core.DataManagementService;
import com.teamcenter.services.strong.core.ReservationService;
import com.teamcenter.services.strong.core._2006_03.DataManagement.CreateItemsOutput;
import com.teamcenter.services.strong.core._2006_03.DataManagement.CreateItemsResponse;
import com.teamcenter.services.strong.core._2006_03.DataManagement.ItemProperties;
import com.teamcenter.services.strong.core._2008_06.DataManagement.ReviseInfo;
import com.teamcenter.services.strong.core._2008_06.DataManagement.ReviseResponse2;
import com.teamcenter.services.strong.core._2010_09.DataManagement.NameValueStruct1;
import com.teamcenter.services.strong.core._2010_09.DataManagement.PropInfo;
import com.teamcenter.services.strong.core._2010_09.DataManagement.SetPropertyResponse;
import com.teamcenter.services.strong.query.SavedQueryService;
import com.teamcenter.services.strong.query._2006_03.SavedQuery.GetSavedQueriesResponse;
import com.teamcenter.services.strong.query._2007_09.SavedQuery.QueryResults;
import com.teamcenter.services.strong.query._2007_09.SavedQuery.SavedQueriesResponse;
import com.teamcenter.services.strong.query._2008_06.SavedQuery.QueryInput;
import com.teamcenter.services.strong.workflow.WorkflowService;
import com.teamcenter.services.strong.workflow._2008_06.Workflow.ContextData;
import com.teamcenter.services.strong.workflow._2008_06.Workflow.InstanceInfo;
import com.teamcenter.soa.client.Connection;
import com.teamcenter.soa.client.model.ErrorStack;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.ServiceData;
import com.teamcenter.soa.client.model.Type;
import com.teamcenter.soa.client.model.strong.BOMLine;
import com.teamcenter.soa.client.model.strong.BOMWindow;
import com.teamcenter.soa.client.model.strong.EPMJob;
import com.teamcenter.soa.client.model.strong.EPMTask;
import com.teamcenter.soa.client.model.strong.Effectivity;
import com.teamcenter.soa.client.model.strong.ImanQuery;
import com.teamcenter.soa.client.model.strong.Item;
import com.teamcenter.soa.client.model.strong.ItemRevision;
import com.teamcenter.soa.client.model.strong.PSBOMViewRevision;
import com.teamcenter.soa.client.model.strong.ReleaseStatus;
import com.teamcenter.soa.client.model.strong.RevisionRule;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.teamcenter.soaictstubs.ICCTException;
import com.teamcenter.soaictstubs.TcUtility;

/**
 * [SR150119-033][20150123] shcho, MProduct WeldGroup 생성 오류 수정
 *                                       1) Reference된 CATPart의 경우 Dispatcher Service에서 올바른 sourceItemRev을 가져오지 못하므로 Parameter로 Revision ID를 넘겨받아 처리 하도록 수정
 *                                       2) 로그메시지 보강 및 생성 위치 변경
 * [SR150119-034][20150205] shcho, MProduct WeldGroup Part Name 변경 (설계에서 정의한 Part Name를 WeldGroup Name에서도 동일하도록 수정)
 *                                                 Revise시 WeldGroupRevision Name을 변경하도록 수정.
 * [SR150522-030][20150529] shcho, 용접그룹 (weldGroup) Revise 후 Release 안되는 오류 수정 (java.lang.ClassCastException 오류 수정)
 * [SR150714-022][20150907][ymjang] 용접점 정보(CATIA Feature Name) 추가적 추출 및 BOP 컬럼 생성 요청
 * [NON-SR][20150925][taeku.jeong] 용접점 Feature Name Update 기능 구현및 Test 과정에 발생된 기타 오류 수정  
 * [20151126][ymjang] 신규 용접 그룹일 경우, 000 리비전의 Release 시간과 001의 Release 시간이 동일하여 Structure Manager 조회시 001 이 아닌 000 리비전이 조회됨.                 
 */
public class Util {
	
	protected static String getNextRevisionID(String itemRevID){

		String revisionStr = "000";
		try{
			int revNumber = Integer.parseInt(itemRevID);
			revisionStr = String.format("%03d", revNumber + 1);
		}catch( NumberFormatException nfe){
			revisionStr = "000";
		}
		return revisionStr;
	}
	
	/**
	 * FMP로 부터 WelPointGroup를 제거 한다.
	 * 
	 * @param connection
	 * @param parentID
	 * @param parentRevID
	 * @param childID
	 * @param childRevID
	 * @throws Exception
	 */
	protected static void removeChildLineWithChildId(Connection connection, String parentID, String parentRevID
			, String childID, String childRevID) throws Exception {
		
		if( childID == null ){
			throw new Exception("Could not find the " + childID + "/" + childRevID);	
		}
		
		Properties prop = getDefaultProperties("weldpointexport");
		String servletUrlStr = prop.getProperty("servlet.url");
		
		DataSet ds = new DataSet();
		ds.put("parent_id", parentID);
		ds.put("parent_rev_id", parentRevID);
		ds.put("child_id", childID);
		ds.put("child_rev_id", childRevID);
		
		// DB에서 삭제 대상인 것의 목록을 읽어 온다.
		ArrayList<HashMap<String, String>> list = (ArrayList<HashMap<String, String>>)Util.execute(servletUrlStr,"com.kgm.service.WeldPointService", "getChildren",ds, true);
		if( list == null || list.isEmpty()){
			return;
		}
		
		ItemRevision fmpRevision = SoaHelper.getItemFromId(parentID, parentRevID);
		ModelObject strModel = SoaHelper.getProperties(fmpRevision, "structure_revisions");
		ReservationService rService = ReservationService.getService(connection);
		PSBOMViewRevision[] bomViewRevision = null;
		
		for( HashMap childInfo : list){
			
			String occThread = (String)childInfo.get("OCC_THREAD");
			
			if( strModel != null){
				bomViewRevision = fmpRevision.get_structure_revisions();
				if (bomViewRevision != null && bomViewRevision.length > 0) {
					rService.checkout(bomViewRevision, "", null);
				}
			}

			DeleteRelativeStructureInfo2 adeleterelativestructureinfo = new DeleteRelativeStructureInfo2();
			adeleterelativestructureinfo.parent = fmpRevision;
			adeleterelativestructureinfo.childInfo = new String[]{occThread};
			DeleteRelativeStructurePref2 pref = new DeleteRelativeStructurePref2();

			StructureManagementRestBindingStub smrms = new StructureManagementRestBindingStub(connection);

			try{
				smrms.deleteRelativeStructure(new DeleteRelativeStructureInfo2[]{adeleterelativestructureinfo}, "view", pref);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				if (bomViewRevision != null && bomViewRevision.length > 0) {
					rService.checkin(bomViewRevision);
				}
			}
		}
		
	}
	
	/**
	 * parent Line 에서 occThread에 해당하는 Child Line을 제거.
	 * 
	 * @param connection
	 * @param parentID
	 * @param parentRevID
	 * @param occThreads
	 * @throws Exception
	 */
	protected static void removeChildLineWithOccThreads(Connection connection, String parentID, String parentRevID
			, String[] occThreads) throws Exception {
		
		ItemRevision fmpRevision = SoaHelper.getItemFromId(parentID, parentRevID);
		ModelObject strModel = SoaHelper.getProperties(fmpRevision, "structure_revisions");
		ReservationService rService = ReservationService.getService(connection);
		PSBOMViewRevision[] bomViewRevision = null;
		
		if( strModel != null){
			bomViewRevision = fmpRevision.get_structure_revisions();
			if (bomViewRevision != null && bomViewRevision.length > 0) {
				rService.checkout(bomViewRevision, "", null);
			}
		}

		DeleteRelativeStructureInfo2 adeleterelativestructureinfo = new DeleteRelativeStructureInfo2();
		adeleterelativestructureinfo.parent = fmpRevision;
		adeleterelativestructureinfo.childInfo = occThreads;
		DeleteRelativeStructurePref2 pref = new DeleteRelativeStructurePref2();

		StructureManagementRestBindingStub smrms = new StructureManagementRestBindingStub(connection);

		try{
			smrms.deleteRelativeStructure(new DeleteRelativeStructureInfo2[]{adeleterelativestructureinfo}, "view", pref);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if (bomViewRevision != null && bomViewRevision.length > 0) {
				rService.checkin(bomViewRevision);
			}
		}
		
	}	
	
	/**
	 * FMP에 용접점 그룹을 추가
	 * 
	 * @param connection
	 * @param fmpRevision
	 * @param weldGroupRevision
	 * @return
	 * @throws Exception
	 */
	protected static Object addWeldPointGroup(Connection connection, ItemRevision fmpRevision
			, ItemRevision weldGroupRevision) throws Exception {
		
		ModelObject strModel = SoaHelper.getProperties(fmpRevision, "structure_revisions");
		
		ReservationService rService = ReservationService.getService(connection);
//		ReservationRestBindingStub rServiceStub = (ReservationRestBindingStub) rService;
		PSBOMViewRevision[] bomViewRevision = null;
		if( strModel != null){
			bomViewRevision = fmpRevision.get_structure_revisions();
			if (bomViewRevision != null && bomViewRevision.length > 0) {
				rService.checkout(bomViewRevision, "", null);
			}
		}
		CreateOrUpdateRelativeStructureInfo2 createOrUpdateRelativeStructureInfo2 = new CreateOrUpdateRelativeStructureInfo2();
		createOrUpdateRelativeStructureInfo2.parent = fmpRevision;
		createOrUpdateRelativeStructureInfo2.precise = false;
		createOrUpdateRelativeStructureInfo2.childInfo = new RelativeStructureChildInfo[1];
		createOrUpdateRelativeStructureInfo2.childInfo[0] = new RelativeStructureChildInfo();
		createOrUpdateRelativeStructureInfo2.childInfo[0].child = weldGroupRevision;
		CreateOrUpdateRelativeStructurePref2 pref2 = new CreateOrUpdateRelativeStructurePref2();

		StructureManagementRestBindingStub smrms = new StructureManagementRestBindingStub(connection);
		try{
			CreateOrUpdateRelativeStructureResponse response = smrms
					.createOrUpdateRelativeStructure(
							new CreateOrUpdateRelativeStructureInfo2[] { createOrUpdateRelativeStructureInfo2 },
							"view", false, pref2);

			if( response.serviceData.sizeOfCreatedObjects() > 0){
				String occPuid = response.serviceData.getCreatedObject(0).getUid();
				return occPuid;
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if (bomViewRevision != null && bomViewRevision.length > 0) {
				rService.checkin(bomViewRevision);
			}
		}

		return null;
	}
	
	/**
	 * ECO_BOM_LIST에서 eplID에 해당하는 Variant Condition을 가져온 후,
	 * weldGroupRevision에 컨디션을 설정한다. 
	 * 
	 * @param connection
	 * @param fmpRevision
	 * @param weldGroupRevision
	 * @param eplId
	 * @throws Exception
	 */
	protected static void setMvlCondition(Connection connection, Date date, ItemRevision functionRevision
			,ItemRevision weldGroupRevision, String eplId) throws Exception {
		
		// 1. ECO_BOM_LIST에서 Condition 가져오기
		Properties prop = getDefaultProperties("weldpointexport");
		String servletUrlStr = prop.getProperty("servlet.url");
		
		DataSet ds = new DataSet();
		ds.put("epl_id", eplId);
		HashMap<String, Object> map = (HashMap<String, Object>)Util.execute(servletUrlStr, "com.kgm.service.WeldPointService", "getEcoEplInfo", ds, true);
		if ( map == null ) return;
		
		String conditionStr = (String)map.get("NEW_VC");
		if( conditionStr != null){
			conditionStr = conditionStr.trim();
		}
		System.out.println("Condition : " + conditionStr);
		
		SoaHelper.getProperties(weldGroupRevision, new String[]{"item_id"});
		String weldGroupID = weldGroupRevision.get_item_id();
		
		if( conditionStr != null && !conditionStr.equals("")){
			SoaHelper.getProperties(functionRevision, new String[]{"item_id"});
			String functionId = functionRevision.get_item_id();
			
			//2.Function을 최상위로 하여 BOM Window생성.
			boolean bFound = false;
			BOMWindow window = null;
			try{
				RevisionRule rule = getRevisionRule(connection, "Latest Released");
				window = createTopLineBOMWindow(connection, functionRevision, rule, date);
				SoaHelper.getProperties(window, "top_line");
				BOMLine functionLine = (BOMLine)window.get_top_line();
				SoaHelper.getProperties(functionLine, new String[]{"bl_child_lines"});
				ModelObject[] fmpObjs = functionLine.get_bl_child_lines();
				
				for( ModelObject fmpObj : fmpObjs){
					
					BOMLine fmpLine = (BOMLine)fmpObj;
					SoaHelper.getProperties(fmpLine, new String[]{"bl_child_lines"});
					ModelObject[] weldGroupObjs = fmpLine.get_bl_child_lines();
					
					for( ModelObject weldGroupObj : weldGroupObjs){
						BOMLine weldGroupLine = (BOMLine)weldGroupObj;
						weldGroupLine.get_bl_item();
						SoaHelper.getProperties(weldGroupLine, new String[]{"bl_item"});
						Item tmpItem = (Item)weldGroupLine.get_bl_item();
						SoaHelper.getProperties(tmpItem, new String[]{"item_id"});
						String tmpItemID = tmpItem.get_item_id();
						if( weldGroupID.equals(tmpItemID)){
							SoaHelper.getProperties(fmpLine, new String[]{"bl_revision"});
							ItemRevision fmpRevision = (ItemRevision)fmpLine.get_bl_revision();
							SoaHelper.getProperties(new ModelObject[]{fmpRevision}, new String[]{"structure_revisions"});
							
							PSBOMViewRevision[] bomViewRevisions = fmpRevision.get_structure_revisions();
							ReservationService rService = ReservationService.getService(connection);
							try{
								rService.checkout(bomViewRevisions, "", null);
								setMvlCondition(connection, functionId, conditionStr, window, weldGroupLine);
							}catch(Exception e){
								throw e;
							}finally{
								rService.checkin(bomViewRevisions);
								for( PSBOMViewRevision bomView : bomViewRevisions){
									save(connection, bomView);
								}
							}
							
							bFound = true;
							break;
						}
					}
					
					if( bFound ){
						break;
					}
				}
				
				save(connection, window);
				
			}catch(Exception e){
				throw e;
			}finally{
				if( window != null){
					StructureManagementService.getService(connection).closeBOMWindows(new BOMWindow[]{window});
				}
			}
			
		}
		
	}
	
	protected static void save(Connection connection, ModelObject model) throws ServiceException{

		ICTService service = ICTService.getService(connection);
		com.teamcenter.soa.client.model.Type type = model.getTypeObject();
		Arg[] args = new Arg[3];
		args[0] = TcUtility.createArg(type.getName());
		args[1] = TcUtility.createArg(type.getUid());
		args[2] = TcUtility.createArg(model.getUid());
		service.invokeICTMethod("ICCT", "save", args);
	}
	
	/**
	 * targer Line에 Condition을 설정한다.
	 * 만약 상위 Function이 옵션을 가지고 있지 않다면, 
	 * Function에 옵션을 정의 후에 Conditiond을 설정함.
	 * 
	 * @param connection
	 * @param condition
	 * @param window
	 * @param targetLine
	 * @throws Exception
	 */
	private static void setMvlCondition(Connection connection,
			String functionId, String condition, BOMWindow window,
			BOMLine targetLine) throws Exception {

		HashMap<String, String> corpOptionMap = null;
		HashMap<String, String> optionDescMap = new HashMap<String, String>();

		Type windowType = window.getTypeObject();
		BOMLine topLine = (BOMLine) window.get_top_line();
		ICTService service = ICTService.getService(connection);
		String result = "";
		Pattern p = Pattern.compile("F\\w*:");
		Matcher m = p.matcher(condition);
		while (m.find()) {
			System.out.println(m.start() + " " + m.group());
			result = m.replaceAll(functionId + ":");
			System.out.println("result : " + result);
		}

		p = Pattern.compile(":\\w{3}\\s");
		m = p.matcher(condition);
		while (m.find()) {
			String optionName = m.group().substring(1).trim();
			System.out.println(m.start() + " " + optionName);
			if (!optionDescMap.containsKey(optionName)) {
				// Function Option 정의
				if (corpOptionMap == null) {
					corpOptionMap = getCorpOption(connection);
				}

				String uid = topLine.getUid();
				String desc = corpOptionMap.get(optionName);

				// Function에 옵션 정의
				Arg[] args = new Arg[2];
				args[0] = TcUtility.createArg(uid);
				args[1] = TcUtility.createArg("public " + optionName
						+ " uses  \"" + desc + "\" 'CorporateOption-001':"
						+ optionName);
				service.invokeICTMethod("ICCTVariantService",
						"lineDefineOption", args);
				System.out.println("Option Definition complete");
				// Window Save
				args = new Arg[3];
				args[0] = TcUtility.createArg( windowType.getName() );
				args[1] = TcUtility.createArg( windowType.getUid() );
				args[2] = TcUtility.createArg( window.getUid() );
				service.invokeICTMethod("ICCT", "save", args);
				System.out.println("Option Save");
				// Window Refresh
				args = new Arg[4];
				args[0] = TcUtility.createArg( windowType.getName() );
				args[1] = TcUtility.createArg( windowType.getUid() );
				args[2] = TcUtility.createArg( window.getUid() );
				args[3] = TcUtility.createArg(0);
				service.invokeICTMethod("ICCT", "refresh", args);
				System.out.println("BOMLine Refresh");
				optionDescMap.put(optionName, corpOptionMap.get(optionName));
			}else{
				System.out.println("optionDescMap.containsKey(" + optionName + ") : " + optionDescMap.containsKey(optionName));
			}
		}

		// Condition 셋팅.
		String uid = targetLine.getUid();
		Arg[] args = new Arg[2];
		args[0] = TcUtility.createArg(uid);
		args[1] = TcUtility.createArg(result);
		service.invokeICTMethod("ICCTVariantService", "setLineMvlCondition",
				args);
		
		System.out.println("setLineMvlCondition ");
	}
	
	private static HashMap<String, String> getCorpOption(Connection connection) throws Exception{

		BOMWindow window = null;
		HashMap<String, String> optionDescMap = new HashMap();
		try{
			ItemRevision corpOptionRevision = SoaHelper.getItemFromId("CorporateOption-001", "000");
			window = createTopLineBOMWindow(connection, corpOptionRevision, null, null);
			SoaHelper.getProperties(window, new String[]{"top_line"});
			 BOMLine topLine = (BOMLine)window.get_top_line();

			 VariantManagementService vm = VariantManagementService.getService(connection);
			 ModularOptionsInput input = new ModularOptionsInput();
			 input.bomWindow = window;
			 input.bomLines = new BOMLine[]{topLine};
			 ModularOptionsForBomResponse response = vm.getModularOptionsForBom(new ModularOptionsInput[]{input});
			 if( response.serviceData.sizeOfPartialErrors() < 1){

				 VariantManagement.ModularOptionsInfo[] optionInfos =  response.optionsOutput[0].optionsInfo;
				 for( VariantManagement.ModularOptionsInfo optionInfo : optionInfos){
					 for( VariantManagement.ModularOption option : optionInfo.options.options){
						 if( !optionDescMap.containsKey(option.optionName)){
							 optionDescMap.put(option.optionName, option.optionDescription);
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
		System.out.println("getCorpOption : " + optionDescMap.size());
		return optionDescMap;
	}	
	
	protected static BOMWindow createTopLineBOMWindow(Connection connection, ItemRevision parentItemRev, RevisionRule revisionRule, Date date) throws Exception {
        CreateBOMWindowsInfo[] createBOMWindowsInfo = populateBOMWindowInfo(parentItemRev, revisionRule, date);
        CreateBOMWindowsResponse createBOMWindowsResponse = StructureManagementService.getService(connection).createBOMWindows(createBOMWindowsInfo);
        if(createBOMWindowsResponse.serviceData.sizeOfPartialErrors() < 1)
        {
        	if (createBOMWindowsResponse.output != null && createBOMWindowsResponse.output.length > 0) {
                return createBOMWindowsResponse.output[0].bomWindow;
            } else {
                return null;
            }
        }
        return null;
    }
	
    protected static CreateBOMWindowsInfo[] populateBOMWindowInfo(ItemRevision itemRev, RevisionRule revisionRule, Date date) {
        CreateBOMWindowsInfo[] bomInfo = new CreateBOMWindowsInfo[1];
        bomInfo[0] = new CreateBOMWindowsInfo();
        bomInfo[0].itemRev = itemRev;
        if (revisionRule != null) {
            com.teamcenter.services.strong.cad._2007_01.StructureManagement.RevisionRuleConfigInfo revisionRuleConfigInfo = new com.teamcenter.services.strong.cad._2007_01.StructureManagement.RevisionRuleConfigInfo();
			revisionRuleConfigInfo.revRule = revisionRule;
			if( date != null){
				Calendar cal = Calendar.getInstance();
				cal.setTime(date);
	            revisionRuleConfigInfo.props.date = cal;
			}
            bomInfo[0].revRuleConfigInfo = revisionRuleConfigInfo;
        }
        return bomInfo;
    }
	
    protected static ImanQuery getQueryObject(Connection connection, String queryName) throws Exception {

        try {
            GetSavedQueriesResponse savedQueries = SavedQueryService.getService(connection).getSavedQueries();
            for (int i = 0; i < savedQueries.queries.length; i++) {

                if (savedQueries.queries[i].name.equals(queryName)){
                    return savedQueries.queries[i].query;
                }
            }  
        } catch (ServiceException e) {
            e.printStackTrace();                  
        }
        return null;          
    }
    
    protected static ItemRevision[] getItemRevisionList(Connection connection, String itemID) throws Exception {
    	Item item = getItem(connection, itemID);
    	if( item == null) return null;
    	
    	SoaHelper.getProperties(item, new String[]{"revision_list"});
    	ModelObject[] list = item.get_revision_list();
    	ItemRevision[] revisionList = new ItemRevision[list.length];
    	System.arraycopy(list, 0, revisionList, 0, list.length);
    	
    	return revisionList;
    }
    
    protected static Item getItem(Connection connection, String itemID) throws Exception {
        String queryName = "Item...";
        String[] entries = { "Item ID" };
        String[] values = { itemID };
        QueryInput[] queryInput = new QueryInput[1];
        queryInput[0] = new QueryInput();
        queryInput[0].clientId= "site";
        queryInput[0].query = getQueryObject(connection, queryName);
        queryInput[0].resultsType=2; 
        queryInput[0].entries = entries;
        queryInput[0].values = values;
        
        SavedQueriesResponse executeSavedQueries = SavedQueryService.getService(connection).executeSavedQueries(queryInput);
        if(executeSavedQueries.serviceData.sizeOfPartialErrors() < 1)
        {
        	QueryResults[] queryresults = executeSavedQueries.arrayOfResults ;
        	String[] uids = queryresults[0].objectUIDS;
        	if (uids == null || uids.length == 0) {
        		return null;
        	}
        	return (Item)SoaHelper.getModelObject(uids)[0];
        }
        
        return null;
    }
	
    protected static RevisionRule getRevisionRule(Connection connection, String revisionRuleName) throws Exception {
        String queryName = "General...";
        String[] entries = { "Type", "Name" };
        String[] values = { "RevisionRule", revisionRuleName };
        QueryInput[] queryInput = new QueryInput[1];
        queryInput[0] = new QueryInput();
        queryInput[0].clientId= "user";
        queryInput[0].query = getQueryObject(connection, queryName);
        queryInput[0].resultsType=2; 
        queryInput[0].entries = entries;
        queryInput[0].values = values;
        
        SavedQueriesResponse executeSavedQueries = SavedQueryService.getService(connection).executeSavedQueries(queryInput);
        if(executeSavedQueries.serviceData.sizeOfPartialErrors() < 1)
        {
        	QueryResults[] queryresults = executeSavedQueries.arrayOfResults ;
        	String[] uids = queryresults[0].objectUIDS;
        	if (uids == null || uids.length == 0) {
        		throw new Exception("Could not find RevisionRule[" + revisionRuleName + "]");
        	}
        	return (RevisionRule)SoaHelper.getModelObject(uids)[0];
        }
        
        return null;
    }
	
	protected static Object addWeldPointGroup(Connection connection, String fmpID, String fmpRevID
			, String weldGroupID, String weldGroupRevID) throws Exception {

		ItemRevision fmpRevision = SoaHelper.getItemFromId(fmpID, fmpRevID);
		ItemRevision weldGroupRevision = SoaHelper.getItemFromId(weldGroupID, weldGroupRevID);
		
		return addWeldPointGroup(connection, fmpRevision, weldGroupRevision);
	}	
	
	/**
	 * [SR150714-022][20150907][ymjang] 용접점 정보(CATIA Feature Name) 추가적 추출 및 BOP 컬럼 생성 요청
	 * 용접그룹에 용접점 추가 
	 * @param connection
	 * @param weldGroupID
	 * @param weldGroupRevID
	 * @param weldPointSheetType
	 * @param matrix
	 * @return
	 * @throws Exception
	 */
	protected static Object addWeldPoint(Connection connection, String weldGroupID, String weldGroupRevID
			,String weldType, BigDecimal sheets, BigDecimal[] matrix, String occName, String feature_name) throws Exception {

		ItemRevision weldGroupRevision = SoaHelper.getItemFromId(weldGroupID, weldGroupRevID);
		SoaHelper.getProperties(weldGroupRevision, "structure_revisions");
		String weldPointID = "";
		Properties prop = getDefaultProperties("weldpointexport");
		if(weldType.equals("CO")){
			if( sheets.intValue() == 2 ){
				weldPointID = prop.getProperty("weld.2");
			}else if( sheets.intValue() == 3){
				weldPointID = prop.getProperty("weld.3");
			}else if( sheets.intValue() == 4){
				weldPointID = prop.getProperty("weld.4");
			}else{
				throw new Exception("Invalid Weld Type.");
			}
		}else if(weldType.equals("DR")){	
			if( sheets.intValue() == 2 ){
				weldPointID = prop.getProperty("weld.2s");
			}else if( sheets.intValue() == 3){
				weldPointID = prop.getProperty("weld.3s");
			}else if( sheets.intValue() == 4){
				weldPointID = prop.getProperty("weld.4s");
			}else{
				throw new Exception("Invalid Weld Type.");
			}
		}else{
			throw new Exception("Invalid Weld Type.");
		}
		
		ItemRevision weldPointRevision = SoaHelper.getItemFromId(weldPointID, "000");
		ReservationService rService = ReservationService.getService(connection);
		PSBOMViewRevision[] bomViewRevision = weldGroupRevision.get_structure_revisions();
		if (bomViewRevision != null && bomViewRevision.length > 0) {
			rService.checkout(bomViewRevision, "", null);
		}

		CreateOrUpdateRelativeStructureInfo2 createOrUpdateRelativeStructureInfo2 = new CreateOrUpdateRelativeStructureInfo2();
		createOrUpdateRelativeStructureInfo2.parent = weldGroupRevision;
		createOrUpdateRelativeStructureInfo2.precise = false;

		createOrUpdateRelativeStructureInfo2.childInfo = new RelativeStructureChildInfo[1];
		createOrUpdateRelativeStructureInfo2.childInfo[0] = new RelativeStructureChildInfo();
		createOrUpdateRelativeStructureInfo2.childInfo[0].child = weldPointRevision;
		CreateOrUpdateRelativeStructurePref2 pref2 = new CreateOrUpdateRelativeStructurePref2();
		ArrayList<AttributesInfo> attrList = new ArrayList();
		AttributesInfo attr = new AttributesInfo();
		attr.name = "bl_plmxml_occ_xform";
		attr.value = "1 0 0 0 0 1 0 0 0 0 1 0 " + matrix[0] + " " + matrix[1] + " " + matrix[2] + " 1";
		attrList.add(attr);
		attr = new AttributesInfo();
		attr.name = "bl_occurrence_name";
		attr.value = occName;
		attrList.add(attr);
		attr = new AttributesInfo();
		attr.name = "M7_FEATURE_NAME";
		attr.value = feature_name;
		attrList.add(attr);
		createOrUpdateRelativeStructureInfo2.childInfo[0].occInfo.attrsToSet = attrList.toArray(new AttributesInfo[attrList.size()]);

		StructureManagementRestBindingStub smrms = new StructureManagementRestBindingStub(connection);
		try{
			CreateOrUpdateRelativeStructureResponse response = smrms
					.createOrUpdateRelativeStructure(
							new CreateOrUpdateRelativeStructureInfo2[] { createOrUpdateRelativeStructureInfo2 },
							"view", false, pref2);

			if( response.serviceData.sizeOfCreatedObjects() > 0){
				String occPuid = response.serviceData.getCreatedObject(0).getUid();
				return occPuid;
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if (bomViewRevision != null && bomViewRevision.length > 0) {
				rService.checkin(bomViewRevision);
				for( PSBOMViewRevision view : bomViewRevision){
					Util.save(connection, view);
				}
			}
		}

		return null;
	}	
	
	protected static CreateItemsOutput[] createItems(String id, String name, String revNo, String unit, String itemType, String sParentObjUid, HashMap<String, String> revMasterProp, Connection connection) throws Exception {
        // Get the service stub
        ItemProperties itemProperty = new ItemProperties();
        itemProperty.clientId = "1";
        itemProperty.itemId = id;
        itemProperty.revId = revNo;
        itemProperty.name = name;
        itemProperty.type = itemType;
        itemProperty.description = name;
        itemProperty.uom = unit;

        // 붙여야할 parent object
        ModelObject parentComp = null;
//        if (sParentObjUid != null && sParentObjUid.length() > 0) {
//            parentComp = getModelObjectFromUid(sParentObjUid, tcSession);
//        }
        // *****************************
        // Execute the service operation
        // *****************************
        DataManagementRestBindingStub stub = new DataManagementRestBindingStub(connection);
        
        CreateItemsResponse response = stub.createItems(new ItemProperties[] { itemProperty }, parentComp, "");
        if(response.serviceData.sizeOfCreatedObjects()>0){
            return response.output;
        } else {
            throw new Exception("Item Creation Fail.");
        }
        
    }
	
	protected static void delete(ItemRevision itemRev, Connection connection) throws Exception {
		DataManagementRestBindingStub stub = new DataManagementRestBindingStub(connection);
		stub.deleteObjects(new ModelObject[]{itemRev});
    }	
	
	protected static boolean ServiceDataError(final ServiceData serviceData) {
        if(serviceData.sizeOfPartialErrors() > 0)
        {
            for(int i = 0; i < serviceData.sizeOfPartialErrors(); i++)
            {
                for(String msg : serviceData.getPartialError(i).getMessages())
                    System.out.println(msg);
            }

            return true;
        }

        return false;
    }	
	
	protected static ItemRevision revise(ItemRevision itemRev, Connection connection) throws Exception {

		SoaHelper.getProperties(itemRev, new String[]{"item_id", "item_revision_id", "object_name"});
		
        ReviseInfo[] revInfo = new ReviseInfo[1];
        revInfo[0] = new ReviseInfo();
        revInfo[0].clientId = "";
        revInfo[0].baseItemRevision = itemRev;
        revInfo[0].name = itemRev.get_object_name();
        revInfo[0].newRevId = getNextRevisionID(itemRev.get_item_revision_id());
        
//        System.out.println("------------------------------------------------------");
//        System.out.println("Item Revision Revise Information");
//        System.out.println("------------------------------------------------------");
//        System.out.println("Baseon item revision = "+itemRev.get_item_id()+"/"+itemRev.get_item_revision_id());
//        System.out.println("Revised item revision = "+itemRev.get_item_id()+"/"+revInfo[0].newRevId);
        
        ReviseResponse2 response = DataManagementService.getService(connection).revise2(revInfo);
        if(response.serviceData.sizeOfPartialErrors() > 0){
        	System.out.println("------------- Start of Error Message (revise)----------------");
        	for (int i = 0; i < response.serviceData.sizeOfPartialErrors(); i++) {
        		ErrorStack errorStack = response.serviceData.getPartialError(i);
        		System.out.println(errorStack.toString());
			}
        	System.out.println("------------- End of Error Message (revise)----------------");
        	throw new Exception("Revise Fail!");
        }
        
        ItemRevision revisedItemRevision = null;
        
        //ModelObject modleObject = SoaHelper.getModelObject(response.serviceData.getCreatedObject(0).getUid());
        // 예전에 위와 같은 형태로 사용했으나 Revie되고 나면 esponse.serviceData.getCreatedObject[]에 Revise 되면서 생성된
        // Object들이 저장되어 있는데 여기에는 Item Revision과 ItemRevision Master등이 들어 있는것을 확인했음.
        // 여기서 Type Cast가 안되는 오류가 발생되어 확인해 보니
        // 원인은 response.serviceData.getCreatedObject(0)가 Revision Object가 아니라 ItemRevision Master였음.
        // 따라서 아래와 같이 코드를 수정함.
        // 2016-09-25 Taeku.Jeong
        if(response.serviceData!=null){
        	for (int i = 0; i < response.serviceData.sizeOfCreatedObjects(); i++) {
        		ModelObject  tmpModelObject = response.serviceData.getCreatedObject(i);
        		
//        		if(tmpModelObject!=null){
//        			System.out.println("@@@ Object Class Name : "+tmpModelObject.getClass().getName());
//        		}
        		
        		if(tmpModelObject!=null && tmpModelObject instanceof ItemRevision){
                	revisedItemRevision = (ItemRevision)tmpModelObject;
                	break;
                }
			}
        }
        
//        if(revisedItemRevision!=null){
//        	System.out.println("------------------------------------------------------");
//        	System.out.println("Revise Success!!");
//        	System.out.println("------------------------------------------------------");
//        }else{
//        	System.out.println("------------------------------------------------------");
//        	System.out.println("Revise Fail!!");
//        	System.out.println("------------------------------------------------------");
//        }

        return revisedItemRevision;
    }	
	
	/**
	 * [SR150119-034][20150205] shcho, MProduct WeldGroup Part Name 변경 (설계에서 정의한 Part Name를 WeldGroup Name에서도 동일하도록 수정)
	 *                                                 Revise시 WeldGroupRevision Name을 변경하도록 수정.
	 */
	protected static ItemRevision revise(ItemRevision itemRev, String revisionName, Connection connection) throws Exception {
	    
	    SoaHelper.getProperties(itemRev, new String[]{"object_name", "item_revision_id"});
	    
	    ReviseInfo[] revInfo = new ReviseInfo[1];
	    revInfo[0] = new ReviseInfo();
	    revInfo[0].clientId = "";
	    revInfo[0].baseItemRevision = itemRev;
	    revInfo[0].name = revisionName;
	    revInfo[0].newRevId = getNextRevisionID(itemRev.get_item_revision_id());
	    
	    ReviseResponse2 response = DataManagementService.getService(connection).revise2(revInfo);
	    if(response.serviceData.sizeOfPartialErrors() > 0){
	        throw new Exception("Revise Fail!");
	    }
	    
	    //[SR150522-030][20150529] shcho, 용접그룹 (weldGroup) Revise 후 Release 안되는 오류 수정 (java.lang.ClassCastException 오류 수정) 
	    //(TC10 부터 createdObject가 2개 이상이며 첫번째는 Master 객체이고, 두번째에 itemRevision이 담기는 경우가 발생함.
	    //기존 소스는 무조건 getCreatedObject(0) 으로 가져오던 것을, ItemRevision에 해당하는 객체를 찾아서 가져오도록 변경 함.
	    String scUID = "";
	    int cretedCount = response.serviceData.sizeOfCreatedObjects();
        for(int i=0; i <cretedCount; i++) {
            ModelObject createdObject = response.serviceData.getCreatedObject(i);
            if(createdObject instanceof ItemRevision) {
                scUID = createdObject.getUid();
            }
        }
	    
	    return (ItemRevision)SoaHelper.getModelObject(scUID);
	}	
	
	protected static Object execute(String servletUrlStr, String class_name, String method, DataSet paramData, boolean blnReturn) throws Exception {
		Object obj = null;
		ObjectOutputStream output = null;
		ObjectInputStream input = null;

		try {
			if (paramData == null) {
				paramData = new DataSet();
			}

			paramData.setString("class_name", class_name);
			paramData.setString("method", method);
			paramData.setBoolean("blnReturn", blnReturn);

			String strParameter = "";

			URL url = new URL(servletUrlStr);
			URLConnection urlConn = url.openConnection();

			urlConn.setDoOutput(true);
			urlConn.setDoInput(true);
			urlConn.setUseCaches(false);

			urlConn.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
			urlConn.setRequestProperty("Content-length", String.valueOf(strParameter.length()));

			output = new ObjectOutputStream(urlConn.getOutputStream());
			output.writeObject(paramData);

			input = new ObjectInputStream(new BufferedInputStream(urlConn.getInputStream()));

			obj = input.readObject();

			if (obj instanceof Exception) {
				throw (Exception) obj;
			}
		} finally {
			if (output != null)
				output.close();
			if (input != null)
				input.close();
		}

		return obj;
	}
	
	protected static void setReferenceProperties(Connection connection, ItemRevision targetRevision, ItemRevision ecoRevision) throws Exception {
		DataManagementService dmService = DataManagementService.getService(connection);
		
		PropInfo prop[] = new PropInfo[1];
		prop[0] = new PropInfo();
		prop[0].object = targetRevision;
		prop[0].timestamp = Calendar.getInstance();
		NameValueStruct1 nvs = new NameValueStruct1();
		nvs.name = "s7_ECO_NO";
		nvs.values = new String[]{ecoRevision.getUid()};
		prop[0].vecNameVal = new NameValueStruct1[]{nvs};
		SetPropertyResponse response = dmService.setProperties(prop, new String[]{"ENABLE_PSE_BULLETIN_BOARD"});
		if(response.data.sizeOfPartialErrors() > 0){
			throw new Exception("setProperties Fail!");
        }
	}
	
	protected static Properties getDefaultProperties(String serviceName) throws FileNotFoundException, IOException{
	      String classPath = Util.class.getProtectionDomain().getCodeSource().getLocation().getPath();
	      StringBuffer sb = new StringBuffer(classPath);
	      Integer idx = sb.indexOf("DispatcherClient");
	      String dpRoot = classPath.substring(1, idx);
	      String transPath = dpRoot + "Module" + File.separator + "Translators";
	      String path = transPath + File.separator + serviceName + File.separator + serviceName + ".properties";
	      Properties prop = new Properties();
	      prop.load(new FileInputStream(path));
	      
	      return prop;
	}
	
	protected static ArrayList<HashMap<String, Object>> getChildren(String parentID, String parentRevID, String childID, String childRevID, String eplID) throws Exception{
		
		Properties prop = getDefaultProperties("weldpointexport");
		String servletUrlStr = prop.getProperty("servlet.url");
		String condition = null;
		
		DataSet ds = new DataSet();
		if( eplID != null){
			ds.put("epl_id", eplID);
			HashMap<String, Object> map = (HashMap<String, Object>)Util.execute(servletUrlStr, "com.kgm.service.WeldPointService", "getEcoEplInfo", ds, true);
			condition = (String)map.get("NEW_VC");
		}
		ds.clear();
		ds.put("parent_id", parentID);
		ds.put("parent_rev_id", parentRevID);
		ds.put("child_id", childID);
		ds.put("child_rev_id", childRevID);
		ds.put("condition", (condition == null ? null:condition.trim()));
		ArrayList<HashMap<String, Object>> list = (ArrayList<HashMap<String, Object>>)Util.execute(servletUrlStr,"com.kgm.service.WeldPointService", "getChildren",ds, true);
		
		return list;
	}
	
	public static String clobToString(Clob clob) throws IOException, SQLException {
		if (clob == null) {
			return "";
		}
		StringBuffer strOut = new StringBuffer();
		String str = "";
		BufferedReader br = new BufferedReader(clob.getCharacterStream());
		while ((str = br.readLine()) != null) {
			strOut.append(str);
		}
		return strOut.toString();
	}
	
	protected static void changeEffectivityDate(ReleaseStatus releaseStatus, Connection connection, Calendar ecoReleaseDate) throws Exception{

		SoaHelper.getProperties(new ModelObject[]{releaseStatus}, new String[]{"effectivities"});
		ModelObject[] effectivities = releaseStatus.get_effectivities();
		for(ModelObject effectivityObj : effectivities){
			Effectivity effectivity = (Effectivity)effectivityObj;
			SoaHelper.getProperties(new ModelObject[]{effectivity}, new String[]{"date_range_text"});
			com.teamcenter.soa.client.model.Type type = effectivity.getTypeObject();
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy HH:mm", Locale.US);
			// [20151126][ymjang] 신규 용접 그룹일 경우, 000 리비전의 Release 시간과 001의 Release 시간이 동일하여
			// Structure Manager 조회시 001 이 아닌 000 리비전이 조회됨.
			//String ecoDateStr = sdf.format(new Date(ecoReleaseDate.getTime().getTime() - (long)5*60*1000));
			String ecoDateStr = sdf.format(ecoReleaseDate.getTime().getTime());
			ICTService service = ICTService.getService(connection);
		    Arg[] args_ = new Arg[5];
		    args_[0] = TcUtility.createArg(type.getName());
		    args_[1] = TcUtility.createArg(type.getUid());
		    args_[2] = TcUtility.createArg(releaseStatus.getUid());
		    args_[3] = TcUtility.createArg(effectivity.getUid());
		    args_[4] = TcUtility.createArgStringUnion(ecoDateStr + " to UP");
		    InvokeICTMethodResponse response = service.invokeICTMethod("ICCTEffectivity", "setDateRange", args_);
		    if( response.serviceData.sizeOfPartialErrors() > 0)
		    {
		      throw new ICCTException( response.serviceData);
		    }

			Arg[] args = new Arg[3];
			args[0] = TcUtility.createArg(type.getName());
			args[1] = TcUtility.createArg(type.getUid());
			args[2] = TcUtility.createArg(effectivity.getUid());
			service.invokeICTMethod("ICCT", "save", args);

		}
	}
	
	protected static void changeReleaseDate(ModelObject modelObject, Connection connection, Calendar ecoReleaseDate) throws Exception{
		ICTService service = ICTService.getService(connection);
		com.teamcenter.soa.client.model.Type type = modelObject.getTypeObject();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		// [20151126][ymjang] 신규 용접 그룹일 경우, 000 리비전의 Release 시간과 001의 Release 시간이 동일하여
		// Structure Manager 조회시 001 이 아닌 000 리비전이 조회됨.
		//String ecoDateStr = sdf.format(new Date(ecoReleaseDate.getTime().getTime() - (long)5*60*1000));
		String ecoDateStr = sdf.format(ecoReleaseDate.getTime());
		StringBuffer sb = new StringBuffer(ecoDateStr);
		sb.insert(22, ':');
		com.teamcenter.services.loose.core._2010_09.DataManagement.PropInfo propInfo
			= new com.teamcenter.services.loose.core._2010_09.DataManagement.PropInfo();
		propInfo.timestamp = Calendar.getInstance();
		propInfo.object = modelObject;
		com.teamcenter.services.loose.core._2010_09.DataManagement.NameValueStruct1 nameValueStruct = new  com.teamcenter.services.loose.core._2010_09.DataManagement.NameValueStruct1();
		nameValueStruct.name = "date_released";
		nameValueStruct.values = new String[]{sb.toString()};//"2013-05-10T15:40:28+09:00"
		propInfo.vecNameVal = new com.teamcenter.services.loose.core._2010_09.DataManagement.NameValueStruct1[]{nameValueStruct};
		com.teamcenter.services.loose.core.DataManagementService.getService(connection).setProperties(new com.teamcenter.services.loose.core._2010_09.DataManagement.PropInfo[]{propInfo},new String[]{"ENABLE_PSE_BULLETIN_BOARD"});

		Arg[] args = new Arg[3];
		args[0] = TcUtility.createArg(type.getName());
		args[1] = TcUtility.createArg(type.getUid());
		args[2] = TcUtility.createArg(modelObject.getUid());
		service.invokeICTMethod("ICCT", "save", args);
	}

    @SuppressWarnings("unused")
    protected static InstanceInfo createNewProcess (Connection connection, ModelObject[] revModels, String processTitle, String wfprocessName) throws NotLoadedException, Exception {

        ContextData contextData = new ContextData();
        String observerKey = "";
        String name = processTitle; //"My SOA Do Task";
        String subject = "";
        String description = "";
        EPMJob job = null;

        //==============================================
        int[] attType = new int[revModels.length];
        for(int i=0; i < revModels.length; i++) {
            attType[i] = 1;
        }
        //=============================================
        String[] revUid = new String[revModels.length];
        for(int i=0; i<revModels.length; i++) {
            revUid[i] = revModels[i].getUid();
        }
        contextData.processTemplate = wfprocessName; //"SOA Do Task";
        contextData.subscribeToEvents = false;
        contextData.subscriptionEventCount = 0;
        contextData.attachmentCount = revModels.length;
        //==================================
        contextData.attachments = revUid;
        contextData.attachmentTypes = attType;
        //==================================

        InstanceInfo instanceInfo =  WorkflowService.getService(connection).createInstance(true, observerKey, name, subject, description, contextData);
                //wfService.createInstance(false, observerKey,name, subject, description, contextData);
        if( instanceInfo.serviceData.sizeOfPartialErrors() > 0){
        	throw new Exception("Could not create process.");
        }else{
        	return instanceInfo;
        }
    }	
    
	protected static void release(String servletUrlStr, ItemRevision ecoRevision,ArrayList<ModelObject> targetToReleaseList, String projectCode) throws Exception{
		
		Connection connection = SoaHelper.getSoaConnection();
		//ReleaseStatus의 Release날짜와  Effectivity 날짜를 ECO 릴리즈 날짜로 수정.
		InstanceInfo instanceInfo = Util.createNewProcess(connection, targetToReleaseList.toArray(new ModelObject[targetToReleaseList.size()]), "Weld Point Auto Release", "WSR");
		if( instanceInfo.serviceData.sizeOfUpdatedObjects() > 0){
			
			SoaHelper.getProperties(new ModelObject[]{ecoRevision}, new String[]{"date_released"});
			Calendar ecoReleaseDate = ecoRevision.get_date_released();
			for( int i = 0; i < instanceInfo.serviceData.sizeOfUpdatedObjects(); i++ ){
				ModelObject updatedObject = instanceInfo.serviceData.getUpdatedObject(i);
				if( updatedObject instanceof EPMTask){
					EPMTask task = (EPMTask)updatedObject;
					SoaHelper.getProperties(new ModelObject[]{task}, new String[]{"release_status_attachments","release_status_list","release_statuses"});
					ModelObject[] releaseStatusAtt = task.get_release_status_attachments();
					ReleaseStatus releaseStatus = (ReleaseStatus)releaseStatusAtt[0];
					Util.changeEffectivityDate(releaseStatus, connection, ecoReleaseDate);
					Util.changeReleaseDate(releaseStatus, connection, ecoReleaseDate);
					
					DataSet ds = new DataSet();
					ds.put("release_status_puid", releaseStatus.getUid());
					ds.put("eco_rev_puid", ecoRevision.getUid());
					try {
						Util.execute(servletUrlStr, "com.kgm.service.WeldPointService", "updateDateReleasedWithEco", ds, false);
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					break;
				}
			}
		}	
		
		com.teamcenter.services.loose.core.DataManagementService dmService = com.teamcenter.services.loose.core.DataManagementService.getService(connection);
		for( int i = 0; i < targetToReleaseList.size(); i++){
			ItemRevision revision = (ItemRevision)targetToReleaseList.get(i);
			com.teamcenter.services.loose.core._2010_09.DataManagement.PropInfo propInfo 
					= new com.teamcenter.services.loose.core._2010_09.DataManagement.PropInfo();
			propInfo.timestamp = Calendar.getInstance();
			propInfo.object = revision;
			com.teamcenter.services.loose.core._2010_09.DataManagement.NameValueStruct1[] nameValueStruct 
					= new com.teamcenter.services.loose.core._2010_09.DataManagement.NameValueStruct1[2];
			nameValueStruct[0] = new  com.teamcenter.services.loose.core._2010_09.DataManagement.NameValueStruct1();
			nameValueStruct[0].name = "s7_MATURITY";
			nameValueStruct[0].values = new String[]{"Released"}; //"2013-05-10T15:40:28+09:00"
			nameValueStruct[1] = new  com.teamcenter.services.loose.core._2010_09.DataManagement.NameValueStruct1();
			nameValueStruct[1].name = "s7_PROJECT_CODE";
			nameValueStruct[1].values = new String[]{projectCode}; 
			propInfo.vecNameVal = nameValueStruct;						
			dmService.setProperties(new com.teamcenter.services.loose.core._2010_09.DataManagement.PropInfo[]{propInfo}, new String[]{"ENABLE_PSE_BULLETIN_BOARD"});
		}
		
	}
	
	/**
	 * WeldPoint Key 생성.
	 * 
	 * @param weldPoint
	 * @return
	 */
	protected static String getWeldPointKey(HashMap<String, Object> weldPoint){
		String weldType = (String)weldPoint.get("WELD_TYPE");
		String sheets = ((BigDecimal)weldPoint.get("SHEETS")) + "";
		String[] matrix = new String[3];
		matrix[0]= (BigDecimal)weldPoint.get("TRANSFORM_TRA0") + "";
		matrix[1] = (BigDecimal)weldPoint.get("TRANSFORM_TRA1") + "";
		matrix[2] = (BigDecimal)weldPoint.get("TRANSFORM_TRA2") + "";
		
		String key = weldType + "_" + sheets + "_" + matrix[0] + "_" + matrix[1] + "_" + matrix[2];
		
		return key;
	}
	
	public static ItemRevision getBasedOnRevision(ItemRevision itemRevision) throws Exception{
		
		ItemRevision basedOnRevision = null;
		
		SoaHelper.getProperties(itemRevision, new String[]{"IMAN_based_on"});
		ModelObject[] resultModels = itemRevision.get_IMAN_based_on();
		for (int i = 0;resultModels!=null && i < resultModels.length; i++) {
			if(resultModels[i] !=null && resultModels[i] instanceof ItemRevision){
				basedOnRevision = (ItemRevision)resultModels[i];
			}
		}
		
		return basedOnRevision;
	}
	
	public static String getBasedOnRevisionId(ItemRevision itemRevision) throws Exception{
		
		String itemRevisionId = null;
		ItemRevision basedOnRevision = Util.getBasedOnRevision(itemRevision);
		if(basedOnRevision!=null){
			SoaHelper.getProperties(basedOnRevision, new String[]{"item_revision_id"});
			itemRevisionId = basedOnRevision.get_item_revision_id();
		}
		
		return itemRevisionId;
	}
    
	/**
	 * fmpItemID와 ecoID가 매치되는 FMP Revision을 리턴.
	 * FMP가 생성되지 않았으면 Throw Exception
	 * 
	 * 
	 * @param connection
	 * @param fmpItemID
	 * @param fmpItemRevID
	 * @return
	 * @throws Exception
	 */
	protected static ItemRevision getFmpRevisionWithEco(Connection connection, String fmpItemID, String ecoID) throws Exception{
		
		ItemRevision resultFmpRevision = null;
		ArrayList<ItemRevision> fmpList = new ArrayList<ItemRevision>();
		ItemRevision[] fmpRevisionList = Util.getItemRevisionList(connection, fmpItemID);
		for(ItemRevision fmpRevision : fmpRevisionList){
			SoaHelper.getProperties(fmpRevision, new String[]{"item_id","item_revision_id","s7_ECO_NO"});
			
			//Revision ID 가 000은 비교에서 제외.
			if( fmpRevision.get_item_revision_id().equals("000")){
				continue;
			}
			ItemRevision fmpEcoRevision = (ItemRevision) fmpRevision.getPropertyObject("s7_ECO_NO").getModelObjectValue();
			SoaHelper.getProperties(fmpEcoRevision, new String[]{"item_id"});
			String foundEcoId = fmpEcoRevision.get_item_id();
			if( ecoID.equals(foundEcoId)){
				fmpList.add(fmpRevision); 
			}
		}
		
		if( fmpList.isEmpty()){
			return null;
		}
		
		//혹시 동일한 ECO ID를 가진 FMP가 다수 존재 할 경우, 가장 최신의 리비전을 리턴. 
		resultFmpRevision = fmpList.get(fmpList.size() - 1);
		return resultFmpRevision;
	}
    
	protected static ItemRevision getFunctionRevision(String fmpItemID) throws Exception{
		String fmpIdStr = fmpItemID;
		String functionId = "F" + fmpIdStr.substring(1, fmpIdStr.length() - 1);
		ItemRevision functionRev = SoaHelper.getItemFromId(functionId, "000");		
		
		return functionRev;
	}	
	
	/**
	 * baseFmpRevision이 릴리즈 되었는지 확인.
	 * 
	 * @param connection
	 * @param baseFmpRevision
	 * @return
	 * @throws Exception
	 */
	protected static boolean isReleased(Connection connection, ItemRevision revision) throws Exception{
		SoaHelper.refresh(revision);
		SoaHelper.getProperties(revision, new String[]{"release_status_list"});
		ReleaseStatus[] releaseStatus = revision.get_release_status_list();
		if( releaseStatus != null && releaseStatus.length > 0){
			return true;
		}
		
		return false;
	}
	
	/**
	 * F605*******A/000 리비전이 릴리즈 되어 있지 않으면, 현재의 ECO Release날짜보다 하루빠르게 릴리즈 날짜를 입력함.
	 * 
	 * @param servletUrlStr
	 * @param ecoReleaseDate
	 * @param baseFmpRevision
	 * @throws Exception
	 */
	protected static void baseFmpRelease(String servletUrlStr, Calendar ecoReleaseDate, ItemRevision baseFmpRevision, String projectCode) throws Exception{
		Connection connection = SoaHelper.getSoaConnection();
		Calendar newReleaseDate = Calendar.getInstance();
		newReleaseDate.setTime(ecoReleaseDate.getTime());
		newReleaseDate.add(Calendar.DATE, -1);
		
		//ReleaseStatus의 Release날짜와  Effectivity 날짜를 ECO 릴리즈 날짜로 수정.
		InstanceInfo instanceInfo = Util.createNewProcess(connection, new ModelObject[]{baseFmpRevision}, "Weld Point Auto Release", "WSR");
		if( instanceInfo.serviceData.sizeOfUpdatedObjects() > 0){
			for( int i = 0; i < instanceInfo.serviceData.sizeOfUpdatedObjects(); i++ ){
				ModelObject updatedObject = instanceInfo.serviceData.getUpdatedObject(i);
				if( updatedObject instanceof EPMTask){
					EPMTask task = (EPMTask)updatedObject;
					SoaHelper.getProperties(new ModelObject[]{task}, new String[]{"release_status_attachments","release_status_list","release_statuses"});
					ModelObject[] releaseStatusAtt = task.get_release_status_attachments();
					ReleaseStatus releaseStatus = (ReleaseStatus)releaseStatusAtt[0];
					Util.changeEffectivityDate(releaseStatus, connection, newReleaseDate);
					Util.changeReleaseDate(releaseStatus, connection, newReleaseDate);
					
					DataSet ds = new DataSet();
					ds.put("release_status_puid", releaseStatus.getUid());
					ds.put("eco_rev_puid", "UPDATE_REF_COMS");
					try {
						Util.execute(servletUrlStr, "com.kgm.service.WeldPointService", "updateDateReleasedWithEco", ds, false);
					} catch (Exception e) {
						e.printStackTrace();
					}
					break;
				}
			}
		}
		
		DataManagementService dmService = DataManagementService.getService(connection);
		PropInfo propInfo = new PropInfo();
		propInfo.timestamp = Calendar.getInstance();
		propInfo.object = baseFmpRevision;
		NameValueStruct1[] nameValueStruct = new NameValueStruct1[2];
		nameValueStruct[0] = new  NameValueStruct1();
		nameValueStruct[0].name = "s7_MATURITY";
		nameValueStruct[0].values = new String[]{"Released"}; //"2013-05-10T15:40:28+09:00"
		nameValueStruct[1] = new  NameValueStruct1();
		nameValueStruct[1].name = "s7_PROJECT_CODE";
		nameValueStruct[1].values = new String[]{projectCode}; 
		propInfo.vecNameVal = nameValueStruct;			
		dmService.setProperties(new PropInfo[]{propInfo}, new String[]{"ENABLE_PSE_BULLETIN_BOARD"});		
	}
	
	protected static ItemRevision getLatestRevision(Connection connection, String itemID) throws Exception{
		ItemRevision revision = null;
		ItemRevision[] revisionList = Util.getItemRevisionList(connection, itemID);
		if( revisionList == null){
			throw new Exception("fmpLatestRevision == null");
		}else{
			revision = revisionList[revisionList.length-1];
		}
		
		return revision;		
	}
	
	protected static void printLog(String dirPath, String s) throws IOException {
		printLog(dirPath, "ModuleLog", s);
	}     
	
	protected static void printLog(String dirPath, String fileName, String logContents) throws IOException {

	    File zLogFile = new File( dirPath + "\\"+ fileName +"_"+ getCurrentTime() +".txt" );			
	    BufferedWriter out = new BufferedWriter(new FileWriter(zLogFile));                                    
	    out.write(logContents);	      
	    out.close();		
	}     
	
	protected static void removeRelated(Connection connection, ModelObject source, ModelObject target, String relatedContextName) throws ServiceException, ICCTException{

		com.teamcenter.services.internal.loose.core.ICTService service
			= com.teamcenter.services.internal.loose.core.ICTService.getService(connection);

		Type type = source.getTypeObject();

	    Arg[] args_ = new Arg[5];
	    args_[0] = TcUtility.createArg(type.getName());
	    args_[1] = TcUtility.createArg(type.getUid());
	    args_[2] = TcUtility.createArgStringUnion(source.getUid());
	    args_[3] = TcUtility.createArg(relatedContextName);
	    args_[4] = TcUtility.createArg(new String[]{target.getUid()});
	    InvokeICTMethodResponse response = service.invokeICTMethod("ICCT", "removeRelated", args_);
	    if( response.serviceData.sizeOfPartialErrors() > 0)
	    {
	      throw new ICCTException( response.serviceData);
	    }

	    args_ = new Arg[4];
	    args_[0] = TcUtility.createArg( type.getName() );
	    args_[1] = TcUtility.createArg( type.getUid() );
	    args_[2] = TcUtility.createArg( source.getUid() );
	    args_[3] = TcUtility.createArg(0);
	    response = service.invokeICTMethod("ICCT", "refresh", args_);
		if( response.serviceData.sizeOfPartialErrors() > 0)
	    {
	      throw new ICCTException( response.serviceData);
	    }
    }

	protected static ModelObject[] whereReferencedInfo(Connection connection, ReleaseStatus rs) throws ICCTException, ServiceException{

    	ModelObject[] result = null;

    	com.teamcenter.services.internal.loose.core.ICTService service
				= com.teamcenter.services.internal.loose.core.ICTService.getService(connection);

    	Type type = rs.getTypeObject();

        Arg[] args_ = new Arg[4];
        args_[0] = TcUtility.createArg(type.getName());
        args_[1] = TcUtility.createArg(type.getUid());
        args_[2] = TcUtility.createArg(rs.getUid());
        args_[3] = TcUtility.createArg(true);
        InvokeICTMethodResponse response = service.invokeICTMethod("ICCT", "whereReferencedInfo", args_);
        if( response.serviceData.sizeOfPartialErrors() > 0)
        {
          throw new ICCTException( response.serviceData);
        }

        int size = response.serviceData.sizeOfPlainObjects();
        if ( size > 0 ) {
        	result = new ModelObject[size];
        	for( int i = 0; i < size; i++){
        		result[i] = response.serviceData.getPlainObject(i);
        	}
        }

        return result;
    }

	protected static void removeReleaseStatus(Connection connection, ModelObject[] modelObject) throws Exception{

    	com.teamcenter.services.loose.core.DataManagementService dm = com.teamcenter.services.loose.core.DataManagementService.getService(connection);
        for( int i = 0; modelObject != null && i < modelObject.length; i++){

        	WhereReferencedResponse whereResponse = dm.whereReferenced(modelObject, 1);
        	ServiceData data = whereResponse.serviceData;
        	for( int j = 0; j < data.sizeOfPlainObjects(); j++){
        		if( data.getPlainObject(j) instanceof EPMTask){

                	ItemRevision revision = (ItemRevision)modelObject[i];
                	dm.getProperties(new ModelObject[]{revision}, new String[]{"release_status_list"});
                	ReleaseStatus[] releaseStatus = revision.get_release_status_list();
                	for(ReleaseStatus rs : releaseStatus){

                		ModelObject[] rsReferenced = whereReferencedInfo(connection, rs);
                		if ( rsReferenced != null ){
                			for(ModelObject ref : rsReferenced){
                				if( ref instanceof EPMTask){
                        			EPMTask task = (EPMTask)ref;
                        			dm.getProperties(new ModelObject[]{task}, new String[]{"object_name"});
                        			String objectName = task.get_object_name();
                        			if( objectName.equalsIgnoreCase("PSR")){
                        				removeRelated(connection, modelObject[i], rs, "release_status_list");
                        			}
                        		}
                			}
                		}
                	}

        			EPMTask task = (EPMTask)data.getPlainObject(j);
        			dm.getProperties(new ModelObject[]{task}, new String[]{"object_name"});
        			String objectName = task.get_object_name();
        			if( objectName.equalsIgnoreCase("PSR")){
        				removeRelated(connection, task, modelObject[i], "target_attachments");
        			}

        		}

        	}
        }
    }	
	
	/**
     * 현재 년월일시분초 값을 가져오는 함수
     */
    public static String getCurrentTime() {
        SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat ( "yyyyMMddHHmmss", Locale.KOREA );
        Date currentTime = new Date(); 
        String mTime = mSimpleDateFormat.format(currentTime);
        return mTime;
    }
	
}
