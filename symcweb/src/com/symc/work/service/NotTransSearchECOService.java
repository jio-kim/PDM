package com.symc.work.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import com.ssangyong.common.remote.DataSet;
import com.symc.common.dao.TcCommonDao;
import com.symc.common.soa.biz.Session;
import com.symc.common.soa.service.TcPreferenceManagementService;
import com.symc.common.soa.service.TcServiceManager;
import com.symc.common.soa.util.TcConstants;
import com.symc.common.util.IFConstants;
import com.symc.common.util.StringUtil;
import com.symc.work.model.EcoDetailVO;
import com.symc.work.model.ProductInfoVO;
import com.symc.work.model.ProjectVO;
import com.teamcenter.services.strong.administration._2012_09.PreferenceManagement.CompletePreference;
import com.teamcenter.services.strong.administration._2012_09.PreferenceManagement.GetPreferencesResponse;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.strong.ItemRevision;

/**
 * [20150709] [ymjang] __SYMC_S7_ProductRevision Invalid list of user entries 오류 수정 
 * [20150710] [ymjang] Product 일괄 재배포시 2 건 Select 오류 불생
 * [20161222][ymjang] S201 ECO 는 Legacy Interface 제외
 * [20161223][ymjang] S201 Project 는 Legacy Interface 제외
 * [20170117][ymjang] C300의 경우, Pre-Product 도 동일한 Project Code 를 가지므로 Pre-Product 는 제외함.
 */
public class NotTransSearchECOService {

	Session session = null;
	
	@SuppressWarnings("unchecked")
	public void updateProductStat() throws Exception {
    	TcCommonDao commonDao = TcCommonDao.getTcCommonDao();
    	DataSet ds = new DataSet();

    	//EAI_FLAG가 "S" 인 경우 전송 상태값을 SUCCESS로 변경
    	ds.put("EAI_FLAG", "S");
    	ds.put("STAT", "SUCCESS");
        commonDao.update("com.symc.interface.updateProductStat", ds);

        //EAI_FLAG가 "E" 인 경우 전송 상태값을 FAIL로 변경
        ds.put("EAI_FLAG", "E");
    	ds.put("STAT", "FAIL");
        commonDao.update("com.symc.interface.updateProductStat", ds);
    }

    public void createNotTransECO() throws Exception {
    	TcCommonDao commonDao = TcCommonDao.getTcCommonDao();
        Date date = (Date)commonDao.selectOne("com.symc.interface.getLastBatchExcutionTime", null);
        if( date == null){
        	//Daemon이 한번도 실행되지 않았으므로, 한번도 초도배포된 이력이 없는 경우임,
        	return;
        }else{

        	// 1. TC Session 생성
        	TcLoginService tcLoginService = new TcLoginService();
        	try{
	        	session = tcLoginService.getTcSession();

	        	//9시간 차이 적용.
	        	Calendar c = Calendar.getInstance();
	        	c.setTime(date);
	        	c.add(Calendar.HOUR_OF_DAY, -9);
	        	this.createNotTransECOInfo(this.getNotTransECOList(c.getTime(), session), session);

	        } catch (Exception e) {
	            throw e;
	        } finally {
	            if (session != null) {
	                session.logout();
	            }

	        }
        }
    }


    /**
     * 마지막 Daemon 실행시각이후에 Release된 ECO검색
     *
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
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

            	/* [20161222][ymjang] S201 ECO 는 Legacy Interface 제외 */
            	if (ecoRevision.get_item_id().startsWith("CM")) {
            		continue;
            	}
            	
            	EcoDetailVO ecoDetailVO = new EcoDetailVO();
            	ecoList.add(ecoDetailVO);
            	ecoDetailVO.setEcoId(ecoRevision.get_item_id());
            	ecoDetailVO.setPlant(ecoRevision.getPropertyObject("s7_PLANT_CODE").getDisplayableValue());
            	ecoDetailVO.setReleaseDate(ecoRevision.get_date_released().getTime());
            	ecoDetailVO.setChangeDesc(ecoRevision.get_object_desc());
            	//3. Affected Project 추출 및 Base Project들 추출.
                String affectedProjects = ecoRevision.getPropertyObject("s7_AFFECTED_PROJECT").getDisplayableValue();
                ecoDetailVO.setAffectedProject(affectedProjects);
                String changeReasonCode = ecoRevision.getPropertyObject("s7_CHANGE_REASON").getDisplayableValue();
                if( changeReasonCode != null){
                	TcCommonDao commonDao = TcCommonDao.getTcCommonDao();
                    DataSet ds = new DataSet();
                    ds.put("ECO_REASON_CODE", changeReasonCode.trim());
                    String ecoReason = (String)commonDao.selectOne("com.symc.tc.lov.getEcoReason", ds);
                    ecoDetailVO.setEcoReason(ecoReason);
                }
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

    /**
     * Base Project를 찾는 메서드.
     *
     * @param tcQueryService
     * @param queryName
     * @param entries
     * @param values
     * @param properties
     * @return
     * @throws Exception
     */
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

    @SuppressWarnings({ "unchecked", "rawtypes" })
	public void createNotTransECOInfo(List<EcoDetailVO> ecoList, Session session) throws Exception {

    	ArrayList<ProductInfoVO> productList = new ArrayList<ProductInfoVO>();

        //4. Product 추출.
        if( !ecoList.isEmpty()){

        	TcQueryService tcQueryService = new TcQueryService(session);
        	for( EcoDetailVO eco : ecoList){

        		if( eco.getEcoId() == null){
            		continue;
            	}

            	//이미 전송할 리스트에 포한되어 있는 지 확인.
            	TcCommonDao commonDao = TcCommonDao.getTcCommonDao();
            	DataSet ds = new DataSet();
            	ds.put("ECO_ID", eco.getEcoId());
            	ds.put("TRANS_TYPE", "E");
            	List result = commonDao.selectList("com.symc.interface.getProductList", ds);
            	if( result != null && !result.isEmpty()){
            		continue;
            	}

            	//ECO Detail Insert
            	ds.clear();
            	ds.put("ECO_ID", eco.getEcoId());
            	ds.put("RELEASE_DATE", eco.getReleaseDate());
            	ds.put("PLANT", eco.getPlant());
            	ds.put("ECO_REASON", eco.getEcoReason());
            	ds.put("CHANGE_DESC", eco.getChangeDesc());
            	ds.put("AFFECTED_PROJECT", eco.getAffectedProject());
            	commonDao.insert("com.symc.interface.insertEcoDetail", ds);

        		HashMap<String, ProjectVO> projectMap = eco.getProjectMap();
        		Collection<String> collection = projectMap.keySet();
        		Iterator<String> its = collection.iterator();
        		while(its.hasNext()){
            		String projectID = its.next();
            		
            		String queryName = "__SYMC_S7_ProductRevision";
            		// [20150709] [ymjang] __SYMC_S7_ProductRevision Invalid list of user entries 오류 수정 
            		String[] entries = new String[]{ "Project Code"};
            		String[] values = new String[]{ projectID};
            		String[] properties = new String[]{TcConstants.PROP_ITEM_ID, TcConstants.PROP_ITEM_TYPE, "item_revision_id", "s7_PROJECT_CODE" };
                    ModelObject[] modelObject = tcQueryService.searchTcObject(queryName, entries, values, properties);

                    //Base Project와 Product는 1:1대응.
                    if( modelObject != null && modelObject.length > 0){
                    	
                    	// [20170117][ymjang] C300의 경우, Pre-Product 도 동일한 Project Code 를 가지므로 Pre-Product 는 제외함.
                    	// C300 를 Project Code로 가진 Product가 2개이상 존재함. --> 오류 발생
                    	int modelObjectCnt = 0;
                    	for (int i = 0; i < modelObject.length; i++) {
                        	ItemRevision productRev = (ItemRevision)modelObject[i];
                        	
                        	if (productRev.get_object_type().equals("S7_ProductRevision")) {
                            	ProductInfoVO productVO = new ProductInfoVO();
                            	productVO.setEcoId(eco.getEcoId());
                            	productVO.setProductId(productRev.get_item_id());
                            	productVO.setProductRevId(productRev.get_item_revision_id());
                            	productVO.setProjectId(projectID);
                            	productVO.setTransType("E");
                            	productVO.setIfDate(eco.getReleaseDate());
                            	
                            	productList.add(productVO);
                            	
                            	modelObjectCnt++;
                        	}
						}
                    	
                    	if( modelObjectCnt > 1){
                    		throw new Exception(projectID + "를 Project Code로 가진 Product가 2개이상 존재함.");
                    	}
                    	
                    }
        		}
        	}
        }

    	ArrayList<DataSet> list = new ArrayList<DataSet>();
    	if( productList != null && !productList.isEmpty()){

    		TcCommonDao commonDao = TcCommonDao.getTcCommonDao();

        	for( ProductInfoVO product : productList){

        		DataSet ds = new DataSet();
        		ds.put("PRODUCT_ID", product.getProductId());

        		// [20161223][ymjang] S201 Project 는 Legacy Interface 제외 (S201 데이터 추가)
        		//비전송 프로젝트로 설정되어 있다면 Skip.
        		List noTransInfo = commonDao.selectList("com.symc.interface.getNoTransInfo", ds);
        		if( noTransInfo != null && !noTransInfo.isEmpty()){
        			continue;
        		}

    			ds.put("PRODUCT_REV_ID", product.getProductRevId());
    			ds.put("PROJECT_ID", product.getProjectId());
    			ds.put("TRANS_TYPE", "P");
    			ds.put("STAT", "SUCCESS");

    			// [20150710] [ymjang] Product 일괄 재배포시 2 건 Select 오류 발생
    			//초도 정상 배포 이력 유무 확인.
    			Object obj = commonDao.selectOne("com.symc.interface.chkFirstTransYN", ds);
    			
    			//초도배포 이력이 있으므로, 해당 EC Product를 등록함.
    			if( (int) obj > 0 ){
    				ds.put("ECO_ID", product.getEcoId());
    				ds.put("TRANS_TYPE", "E");
        			ds.put("STAT", "CREATION");
        			ds.put("IF_DATE", product.getIfDate());
        			list.add(ds);
    			}

        	}

             commonDao.insertList("com.symc.interface.insertProduct", list);

        }
    }

    // [20161223][ymjang] Preference Array Value 값 가저오기
    public ArrayList<String> getPreferenceList(String key){
    	
    	TcServiceManager manager = new TcServiceManager(session);
    	TcPreferenceManagementService prefManager = null;
    	CompletePreference retPrefValue = null;
		try {
			prefManager = manager.getPreferenceService();
			GetPreferencesResponse ret = prefManager.getPreferences(new String[]{key}, true);
			if (ret != null && ret.data.sizeOfPartialErrors() == 0)
			{
				for (CompletePreference pref : ret.response)
					if (pref.definition.protectionScope.toUpperCase().equals("site".toUpperCase()))
						retPrefValue = pref;
			}
			
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		if (retPrefValue.values.values == null || retPrefValue.values.values.length <= 0) {
			return null;
		}
		
    	ArrayList<String> list = new ArrayList<String>();
    	for (int i = 0; i < retPrefValue.values.values.length; i++) {
    		if (retPrefValue.values.values[i] == null || retPrefValue.values.values[i].equals("")) 
    			continue;
    		
    		list.add(retPrefValue.values.values[i]);
		}
    	
		return list;

    }
    
    public void testCGR() throws Exception{
    	TcLoginService tcLoginService = new TcLoginService();
    	Session session = null;
    	try{
        	session = tcLoginService.getTcSession();
        	TcQueryService tcQueryService = new TcQueryService(session);
        	TcFileService tcFileService = new TcFileService(session);
        	String queryName = TcConstants.SEARCH_ITEM_REVISION;
            String[] entries = new String[]{ "Type", "Item ID", "Revision"};
            String[] values = new String[]{ IFConstants.CLASS_TYPE_S7_VEH_PART_REVISION, "TEST000001", "007"};
            String[] properties = new String[]{TcConstants.PROP_ITEM_ID, "item_revision_id"};
            ModelObject[] modelObject = tcQueryService.searchTcObject(queryName, entries, values, properties);
            ItemRevision revision = (ItemRevision)modelObject[0];
            tcFileService.createCGR(revision);
    	} catch (Exception e) {
            throw e;
        }
    }
}
