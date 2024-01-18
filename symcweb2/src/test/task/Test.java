/**
 *
 */
package test.task;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import test.common.AbstractTcSoaTest;

import com.symc.common.soa.biz.Session;
import com.symc.common.soa.biz.TcItemUtil;
import com.symc.common.soa.util.TcConstants;
import com.symc.common.util.IFConstants;
import com.symc.common.util.StringUtil;
import com.symc.work.model.EcoDetailVO;
import com.symc.work.model.ProjectVO;
import com.symc.work.service.BOMLineService;
import com.symc.work.service.TcQueryService;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.strong.BOMLine;
import com.teamcenter.soa.client.model.strong.BOMWindow;
import com.teamcenter.soa.client.model.strong.ItemRevision;

/**
 * Class Name : Test
 * Class Description :
 * @date 2013. 9. 10.
 *
 */
public class Test extends AbstractTcSoaTest {

    @org.junit.Test
    public void test() {
//        TcItemUtil tcItemUtil = new TcItemUtil(session);
        try {
        	//TcSessionServiceManager sessionServiceManager = new TcSessionServiceManager(session);
//        	com.teamcenter.services.strong.core._2006_03.Session.PreferencesResponse response =  sessionServiceManager.getPreferences("site", new String[]{"PE_IF_ADMIN"});
//        	List<String> list = response.preferences.getPreference("PE_IF_ADMIN");

//            tcItemUtil.getProperties(new ModelObject[] { revision }, new String[] { "s7_ECO_NO" });
//            ItemRevision ecoRevision = (ItemRevision) revision.getPropertyObject("s7_ECO_NO").getModelObjectValue();
//            tcItemUtil.getProperties(new ModelObject[] {ecoRevision}, new String[] {"item_id"});
//            System.out.println(ecoRevision.get_item_id());
//            Assert.assertNotNull(ecoRevision.get_item_id());

        	getWindow();
//        	removeReleaseStatusTest();

//        	TcServiceManager serviceManager = new TcServiceManager(session);
//        	serviceManager.getDataService().setProperties(arg0, arg1)
//        	getDataService()
//        	DataManagement dm = new DataManagement();
//        	SetPropertiesInput input;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    private void removeRelated(Connection connection, ModelObject source, ModelObject target, String relatedContextName) throws ServiceException, ICCTException{
//
//		com.teamcenter.services.internal.loose.core.ICTService service
//			= com.teamcenter.services.internal.loose.core.ICTService.getService(session.getConnection());
//
//		Type type = source.getTypeObject();
//
//	    Arg[] args_ = new Arg[5];
//	    args_[0] = TcUtility.createArg(type.getName());
//	    args_[1] = TcUtility.createArg(type.getUid());
//	    args_[2] = TcUtility.createArgStringUnion(source.getUid());
//	    args_[3] = TcUtility.createArg(relatedContextName);
//	    args_[4] = TcUtility.createArg(new String[]{target.getUid()});
//	    InvokeICTMethodResponse response = service.invokeICTMethod("ICCT", "removeRelated", args_);
//	    if( response.serviceData.sizeOfPartialErrors() > 0)
//	    {
//	      throw new ICCTException( response.serviceData);
//	    }
//
//	    args_ = new Arg[4];
//	    args_[0] = TcUtility.createArg( type.getName() );
//	    args_[1] = TcUtility.createArg( type.getUid() );
//	    args_[2] = TcUtility.createArg( source.getUid() );
//	    args_[3] = TcUtility.createArg(0);
//	    response = service.invokeICTMethod("ICCT", "refresh", args_);
//		if( response.serviceData.sizeOfPartialErrors() > 0)
//	    {
//	      throw new ICCTException( response.serviceData);
//	    }
//    }

//    private ModelObject[] whereReferencedInfo(Connection connection, ReleaseStatus rs) throws ICCTException, ServiceException{
//
//    	ModelObject[] result = null;
//
//    	com.teamcenter.services.internal.loose.core.ICTService service
//				= com.teamcenter.services.internal.loose.core.ICTService.getService(session.getConnection());
//
//    	Type type = rs.getTypeObject();
//
//        Arg[] args_ = new Arg[4];
//        args_[0] = TcUtility.createArg(type.getName());
//        args_[1] = TcUtility.createArg(type.getUid());
//        args_[2] = TcUtility.createArg(rs.getUid());
//        args_[3] = TcUtility.createArg(true);
//        InvokeICTMethodResponse response = service.invokeICTMethod("ICCT", "whereReferencedInfo", args_);
//        if( response.serviceData.sizeOfPartialErrors() > 0)
//        {
//          throw new ICCTException( response.serviceData);
//        }
//
//        int size = response.serviceData.sizeOfPlainObjects();
//        if ( size > 0 ) {
//        	result = new ModelObject[size];
//        	for( int i = 0; i < size; i++){
//        		result[i] = response.serviceData.getPlainObject(i);
//        	}
//        }
//
//        return result;
//    }

//    private void removeReleaseStatusTest() throws Exception{
//
//    	TcQueryService tcQueryService = new TcQueryService(session);
//    	String queryName = TcConstants.SEARCH_ITEM_REVISION;
//        String[] entries = new String[]{ "Type", "Item ID", "Revision"};
//        String[] values = new String[]{ "ItemRevision", "TEST000002", "000"};
//        String[] properties = new String[]{TcConstants.PROP_ITEM_ID, "item_revision_id"};
//
//        DataManagementService dm = DataManagementService.getService(session.getConnection());
//		com.teamcenter.services.internal.loose.core.ICTService service
//					= com.teamcenter.services.internal.loose.core.ICTService.getService(session.getConnection());
//
//        ModelObject[] modelObject = tcQueryService.searchTcObject(queryName, entries, values, properties);
//        for( int i = 0; modelObject != null && i < modelObject.length; i++){
//
//        	WhereReferencedResponse whereResponse = dm.whereReferenced(modelObject, 1);
//        	ServiceData data = whereResponse.serviceData;
//        	for( int j = 0; j < data.sizeOfPlainObjects(); j++){
//        		if( data.getPlainObject(j) instanceof EPMTask){
//
//                	ItemRevision revision = (ItemRevision)modelObject[i];
//                	dm.getProperties(new ModelObject[]{revision}, new String[]{"release_status_list"});
//                	ReleaseStatus[] releaseStatus = revision.get_release_status_list();
//                	for(ReleaseStatus rs : releaseStatus){
//
//                		ModelObject[] rsReferenced = whereReferencedInfo(session.getConnection(), rs);
//                		if ( rsReferenced != null ){
//                			for(ModelObject ref : rsReferenced){
//                				if( ref instanceof EPMTask){
//                        			EPMTask task = (EPMTask)ref;
//                        			dm.getProperties(new ModelObject[]{task}, new String[]{"object_name"});
//                        			String objectName = task.get_object_name();
//                        			if( objectName.equalsIgnoreCase("PSR")){
//                        				removeRelated(session.getConnection(), modelObject[i], rs, "release_status_list");
//                        			}
//                        		}
//                			}
//                		}
//                	}
//
//        			EPMTask task = (EPMTask)data.getPlainObject(j);
//        			dm.getProperties(new ModelObject[]{task}, new String[]{"object_name"});
//        			String objectName = task.get_object_name();
//        			if( objectName.equalsIgnoreCase("PSR")){
//        				removeRelated(session.getConnection(), task, modelObject[i], "target_attachments");
//        			}
//
//        		}
//
//        	}
//        	System.out.println("adsfasdfasd");
//        	/*
//        	ItemRevision revision = (ItemRevision)modelObject[i];
//        	dm.getProperties(new ModelObject[]{revision}, new String[]{"release_status_attachments","release_status_list","release_statuses"});
//			ModelObject[] releaseStatusAtt = revision.get_re .get_release_status_attachments();
//			ReleaseStatus releaseStatus = (ReleaseStatus)releaseStatusAtt[0];
//			*/
//        }
//    }

    public BOMWindow getWindow() throws ParseException, Exception{

    	TcItemUtil tcItemUtil = new TcItemUtil(session);
    	TcQueryService tcQueryService = new TcQueryService(session);
        String queryName = TcConstants.SEARCH_ITEM_REVISION;
        String[] entries = { "Type", "Item ID", "Revision"};
//        String[] values = { "S7_FunctionMastRevision", "M620XA2015A", "004"};
        String[] values = { "S7_FunctionMastRevision", "M734XA2015A", "008"};
        String[] properties = {TcConstants.PROP_ITEM_ID, "item_revision_id", "date_released", "object_desc"};

        //2. Batch가 실행된 이후 Release된 ECO 검색함.
        ModelObject[] modelObject = tcQueryService.searchTcObject(queryName, entries, values, properties);
    	BOMLineService bomLineService = new BOMLineService(session);
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    	//sdf.format(c.getTime());
    	BOMWindow bomWindow = null;
    	int foundCount = 0;
    	try{
    		bomWindow = bomLineService.getCreateBOMWindow((ItemRevision)modelObject[0], IFConstants.BOMVIEW_LATEST_RELEASED, sdf.parse("2013-09-06 12:05"));
    		BOMLine topLine = (BOMLine)bomWindow.get_top_line();
    		properties = new String[]{"item_id", "bl_child_lines", "bl_parent", "bl_revision", "bl_occurrence_uid", "bl_occ_occ_thread", "bl_sequence_no", "bl_has_date_effectivity", "bl_occ_effectivity"};


    		tcItemUtil.getProperties(new ModelObject[]{topLine}, properties);
    		ModelObject[] child_lines = topLine.get_bl_child_lines();
    		for( int i = 0; child_lines != null && i < child_lines.length; i++){
    			tcItemUtil.getProperties(new ModelObject[]{child_lines[i]}, properties);
    			BOMLine child = (BOMLine)child_lines[i];
    			HashMap<String, String> map = new HashMap<String, String>();
    			map.put("S7_IN_ECO", "TEST000");
    			map.put("S7_OUT_ECO", "20131103 15:30:23 TO 20131110 15:30:23");
    			//tcItemUtil.setAttributes(child, map);

    			//structureSVC.saveBOMWindow(bomWindow);

    			ItemRevision revision = (ItemRevision)child.get_bl_revision();
    			tcItemUtil.getProperties(new ModelObject[]{revision}, new String[]{"item_id"});
    			String itemID = revision.get_item_id();
    			if( itemID.equals("7219105000")){
    				if( child.get_bl_is_packed()){
    					System.out.println("7219105000 is packed.");
    				}
    				foundCount++;
    			}
//    			String occThread = child.get_bl_occ_occ_thread();
//    			String occPuid = child.get_bl_occurrence_uid();
//    			String effectivityID = child.get_bl_has_date_effectivity();
//    			List<String> list = child.getPropertyDisplayableValues("bl_occ_effectivity");
//    			Property property = child.getPropertyObject("bl_occ_effectivity");

    			/*
    			ServiceData serviceData = dmService.getService().loadObjects(new String[]{"gzfJY2GE4Fo0UD"});
    			int cnt = serviceData.sizeOfPlainObjects();
    			if(!(serviceData.sizeOfPartialErrors() > 0) ) {
	    			CFM_date_info cfmInfo = (CFM_date_info)serviceData.getPlainObject(0);
	    			tcItemUtil.getProperties(new ModelObject[]{cfmInfo}, new String[]{"effectivity_id","eff_date"});
	    			Calendar[] cals = cfmInfo.get_eff_date();
	    			String effectivity_id = cfmInfo.get_effectivity_id();
    			}
    	        CFM_date_info ecoEffectivity = null;
    	        CreateIn ci = new CreateIn();
    	        EffectivitiesManagementService effectiviyiesService = EffectivitiesManagementService.getService(session.getConnection());
    	        */
//    	        effectiviyiesService.
//    	        ci.data.dateArrayProps.put(key, value)
//    	        TcServiceManager svcManager = new TcServiceManager(session);
//    	        com.symc.common.soa.service.TcQueryService qrySvc = svcManager.getQueryService();
//    	        com.teamcenter.services.strong.query._2008_06.SavedQuery.QueryInput[] qryInputs = new com.teamcenter.services.strong.query._2008_06.SavedQuery.QueryInput[1];
//    	        qryInputs[0] = new com.teamcenter.services.strong.query._2008_06.SavedQuery.QueryInput();
//    	        qryInputs[0].query = qrySvc.getQuery("__SYMC_occ_effectivity");
//    	        qryInputs[0].entries = new String[]{"effectivity_id"};
//    	        qryInputs[0].values = new String[]{"TEST007"};
//    	        SavedQueriesResponse response = qrySvc.executeSavedQueries(qryInputs);
//    	        SavedQuery.QueryResults[] queryResults = response.arrayOfResults;

//    			property.get
//    			dmService.setProperties(arg0, arg1)
//    			Arg[] arg = new Arg[3];
//    			arg[0] = new Arg();
//    			arg[0].val = "CFM_date_info";
//    			arg[1] = new Arg();
//    			arg[1].val = "id";
//				arg[2] = new Arg();
//    			arg[2].val = "35AD101";
//    			InvokeICTMethodResponse response = sv.invokeICTMethod("ICCTClassService", "findByClass", arg);
//    			GetPropertiesInput input = new GetPropertiesInput();
//    			List list = input.getAttributes();
//    			list.add(child.getUid());
//    			list.add(e)
//    			String m_uid = child.getUid();
//    			//bl_occ_effectivity
//    			ModelObject[] effectivities = child.get_bl_occ_effectivity();

			}
    		System.out.println("foundCount : " + foundCount);

    	}catch(Exception e){
    		e.printStackTrace();
    	}finally{
    		if ( bomWindow != null ){
    			bomLineService.closeBOMWindow(bomWindow);
    		}
    	}
    	return null;
    }

    public List<EcoDetailVO> getNotTransECOList(Date date, Session session) throws Exception {

    	TcQueryService tcQueryService = new TcQueryService(session);
        String queryName = TcConstants.SEARCH_GENERAL;
        String[] entries = { "Type", "Released After"};

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy HH:mm", Locale.US);
        String[] values = { "S7_ECORevision", sdf.format(date)};
        String[] properties = {TcConstants.PROP_ITEM_ID, "item_revision_id", "date_released", "s7_AFFECTED_PROJECT", "s7_PLANT_CODE", "object_desc"};

        //2. Batch가 실행된 이후 Release된 ECO 검색함.
        ModelObject[] modelObject = tcQueryService.searchTcObject(queryName, entries, values, properties);

        ArrayList<EcoDetailVO> ecoList = new ArrayList<EcoDetailVO>();
        if( modelObject != null){
        	ItemRevision[] ecoRevisions = new ItemRevision[modelObject.length];
            System.arraycopy(modelObject, 0, ecoRevisions, 0, modelObject.length);

            HashMap<String, ItemRevision> baseProjectMap = new HashMap<String, ItemRevision>();
            for( ItemRevision  ecoRevision : ecoRevisions){

            	EcoDetailVO ecoDetailVO = new EcoDetailVO();
            	ecoList.add(ecoDetailVO);
            	ecoDetailVO.setEcoId(ecoRevision.get_item_id());
            	ecoDetailVO.setPlant(ecoRevision.getPropertyObject("s7_PLANT_CODE").getDisplayableValue());
            	ecoDetailVO.setReleaseDate(ecoRevision.get_date_released().getTime());
            	ecoDetailVO.setChangeDesc(ecoRevision.get_object_desc());
            	//3. Affected Project 추출 및 Base Project들 추출.
                String affectedProjects = ecoRevision.getPropertyObject("s7_AFFECTED_PROJECT").getDisplayableValue();
                ecoDetailVO.setAffectedProject(affectedProjects);
//                String changeReasonCode = ecoRevision.getPropertyObject("s7_CHANGE_REASON").getDisplayableValue();
//                if( changeReasonCode != null){
//                	TcCommonDao commonDao = TcCommonDao.getTcCommonDao();
//                    DataSet ds = new DataSet();
//                    ds.put("ECO_REASON_CODE", changeReasonCode.trim());
//                    String ecoReason = (String)commonDao.selectOne("com.symc.tc.lov.getEcoReason", ds);
//                    ecoDetailVO.setEcoReason(ecoReason);
//                }
                HashMap<String, ProjectVO> projectMap = new HashMap<String, ProjectVO>();
                ecoDetailVO.setProjectMap(projectMap);
                String[] projects = affectedProjects.split(",");
                for( String projectStr : projects){

                	projectStr = StringUtil.nullToString( projectStr );
                	if( baseProjectMap.containsKey(projectStr) || projectStr.equals("")){
                		continue;
                	}
                	queryName = TcConstants.SEARCH_ITEM_REVISION;
                    entries = new String[]{ "Type", "Item ID"};
                    values = new String[]{ "S7_PROJECTRevision", projectStr};
                    properties = new String[]{TcConstants.PROP_ITEM_ID, "item_revision_id", "s7_BASE_PRJ"};
                    ItemRevision baseProjectRevision = getBaseProject(tcQueryService, queryName, entries, values, properties);

                    if( baseProjectRevision != null ){
	                    if( !projectMap.containsKey(baseProjectRevision.get_item_id())){
	                    	ProjectVO projectVO = new ProjectVO();
	                    	projectVO.setProjectId(baseProjectRevision.get_item_id());
	                    	projectVO.setProjectRevId(baseProjectRevision.get_item_revision_id());
	                    	projectMap.put(baseProjectRevision.get_item_id(), projectVO);
	                    }
                    }
                }
            }

        }

        return ecoList;

    }

    public ItemRevision getBaseProject(TcQueryService tcQueryService,String queryName, String[] entries, String[] values, String[] properties) throws Exception{
    	ModelObject[] modelObject = tcQueryService.searchTcObject(queryName, entries, values, properties);
        if( modelObject != null){
        	ItemRevision projectRevision = (ItemRevision)modelObject[0];
        	String basePrj = projectRevision.getPropertyDisplayableValue("s7_BASE_PRJ");
        	if( basePrj == null || basePrj.equals("")){
        		return projectRevision;
        	}else{
        		if( values[1].equals(basePrj)){
        			return projectRevision;
        		}
        		String[] newValues = new String[]{ "S7_PROJECTRevision", basePrj};
        		return getBaseProject(tcQueryService,queryName, entries, newValues, properties);
        	}
        }

        return null;
    }
}
