package com.symc.plm.me.sdv.command.meco.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.remote.SYMCRemoteUtil;
import com.ssangyong.dto.EndItemData;

/**
 * [SR150122-027][20150210] shcho, Find automatically replaced end item (공법 할당 E/Item의 설계 DPV에 의한 자동 변경 오류 해결) (10버전에서 개발 된 소스 9으로 이식함)
 * [NONE-SR][20151123] taeku.jeong 조립작업 표준서를 검색기능개선 (Query를 이용 속도 & 검색조건 누락수정)
 * [SR151207-041][20151215] taeku.jeong 용접점의 끊어지기전 Occurrence Name을 찾아서 표시해주는 메뉴추가에따른 검색기능 추가
 */
public class CustomBOPDao {

	public static final String BOP_SERVICE_CLASS = "com.ssangyong.service.SYMCBOPService";

    private SYMCRemoteUtil remoteQuery;
    private DataSet ds;
    public CustomBOPDao() {
//    	this.remoteQuery = new SYMCRemoteUtil("http://localhost:8080/ssangyongweb/HomeServlet");
        this.remoteQuery = new SYMCRemoteUtil();
    }

    @SuppressWarnings("unchecked")
	public ArrayList<EndItemData> findReplacedEndItems(String targetUid, String productId) throws Exception {
    	ArrayList<EndItemData> resultList = null;
        
    	ds = new DataSet();
        ds.put("targetUid", targetUid);
        ds.put("productId", productId);

        System.out.println("targetUid : " + targetUid);
        System.out.println("productId : "  + productId);

        resultList = (ArrayList<EndItemData>) remoteQuery.execute(BOP_SERVICE_CLASS, "findReplacedEndItems", ds);
		return resultList;
    }

    /**
     * [NONE_SR][20151123] taeku.jeong Operation검색 속도 개선을위해 API를 이용한 전개 방식이 아닌 Query를 이용하는 방식으로 개선
     * 개발과정에Query사용방법을 Test할 목적으로 만들고 Test를 진행할 예정임 
     * 필요한 곳에서 바로 호출해도 상관 없으나 필요한경우 한번더 Controll 할 수 있으므로 코딩 Rule을 따름
     * @param searchCondition
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
	public ArrayList<HashMap> getFindedOperationOccurenceDataList(HashMap<String, String> searchCondition, boolean isNewWay) throws Exception{
    	
    	ArrayList<HashMap> resultList = null;
    	
    	System.out.println("searchCondition = "+searchCondition);
    	
    	String searchTargetType = null;
    	String vehicleCode = null;
    	String shop_code = null;
    	
    	ds = new DataSet();
    	if(searchCondition.get("findKey")!=null){
    		ds.put("findKey", searchCondition.get("findKey"));
    	}
    	if(searchCondition.get("shop")!=null && 
    			searchCondition.get("shop").equalsIgnoreCase("null")==false &&
    			searchCondition.get("shop").trim().length()>0){
    		//ds.put("shop", searchCondition.get("shop"));
    		ds.put("shop_item_Id", searchCondition.get("shop"));
    		shop_code = searchCondition.get("shop").trim().substring(4, 6);
    		ds.put("shop_code", shop_code);
    	}
    	
    	if(searchCondition.get("line")!=null && 
    			searchCondition.get("line").equalsIgnoreCase("null")==false &&
    			searchCondition.get("line").trim().length()>0){
    		ds.put("line", searchCondition.get("line"));
    		ds.put("line_code", searchCondition.get("line").trim().substring(7, 9));
    	}  
    	
    	if(searchCondition.get("station_code")!=null && 
    			searchCondition.get("station_code").equalsIgnoreCase("null")==false &&
    			searchCondition.get("station_code").trim().length()>0){
    		ds.put("station_code", searchCondition.get("station_code"));
    	}
    	if(searchCondition.get("item_id")!=null && 
    			searchCondition.get("item_id").equalsIgnoreCase("null")==false &&
    			searchCondition.get("item_id").trim().length()>0){
    		ds.put("item_id", searchCondition.get("item_id"));
    	}    
    	if(searchCondition.get("m7_KOR_NAME")!=null && 
    			searchCondition.get("m7_KOR_NAME").equalsIgnoreCase("null")==false &&
    			searchCondition.get("m7_KOR_NAME").trim().length()>0){
    		String tempStr = searchCondition.get("m7_KOR_NAME");
    		ds.put("m7_KOR_NAME", tempStr.trim().toUpperCase());
    	} 

    	if(searchCondition.get("bl_occ_mvl_condition")!=null){
    		ds.put("bl_occ_mvl_condition", searchCondition.get("bl_occ_mvl_condition"));
    	} 
    	if(searchCondition.get("owning_user")!=null){
    		ds.put("owning_user", searchCondition.get("owning_user"));
    	}
    	if(searchCondition.get("m7_MECO_NO")!=null){
    		ds.put("m7_MECO_NO", searchCondition.get("m7_MECO_NO"));
    	}   
    	if(searchCondition.get("publish_user")!=null){
    		ds.put("publish_user", searchCondition.get("publish_user"));
    	}

    	if(searchCondition.get("norelease_operation")!=null){
    		ds.put("norelease_operation", searchCondition.get("norelease_operation"));
    	}
    	if(searchCondition.get("empty_operation")!=null){
    		ds.put("empty_operation", searchCondition.get("empty_operation"));
    	} 
    	if(searchCondition.get("different_operation")!=null){
    		ds.put("different_operation", searchCondition.get("different_operation"));
    	}    	
    	if(searchCondition.get("release_operation")!=null){
    		ds.put("release_operation", searchCondition.get("release_operation"));
    	}
    	
    	if(searchCondition.get("configId")!=null && searchCondition.get("configId").trim().equalsIgnoreCase("1")){
    		// 영문 검색
    		searchTargetType = "EPS";
    		ds.put("searchTargetType", searchTargetType);
    		
    	}else{
    		searchTargetType = "KPS";
    		ds.put("searchTargetType", searchTargetType);
    	}
    	
    	if(searchCondition.get("vehicleCode")!=null && searchCondition.get("vehicleCode").trim().length()>0){
    		// VehicleCode
    		vehicleCode = searchCondition.get("vehicleCode");
    		ds.put("vehicleCode", vehicleCode);
    	}
    	
    	String basicFindKey1 = null;
    	String basicFindKey2 = null;
    	if(vehicleCode!=null && vehicleCode.trim().length()>0 &&
    			shop_code!=null && shop_code.trim().length()>0){
    		basicFindKey1 =  vehicleCode+"-"+shop_code;
    		ds.put("basicFindKey1", basicFindKey1);
        	if(searchTargetType!=null && searchTargetType.trim().length()>0){
        		basicFindKey2 =  searchTargetType+"-"+vehicleCode+"-"+shop_code;
        		ds.put("basicFindKey2", basicFindKey2);
        	}
    	}
    	
    	System.out.println("findKey = "+ds.getString("findKey"));
    	System.out.println("shop_item_Id = "+ds.getString("shop_item_Id"));
    	System.out.println("shop_code = "+ds.getString("shop_code"));
    	System.out.println("line_code = "+ds.getString("line_code"));
    	System.out.println("station_code = "+ds.getString("station_code"));
    	
    	System.out.println("item_id = "+ds.getString("item_id"));
    	System.out.println("m7_KOR_NAME = "+ds.getString("m7_KOR_NAME"));
    	
    	System.out.println("vehicleCode = "+ds.getString("vehicleCode"));
    	System.out.println("searchTargetType = "+ds.getString("searchTargetType"));
    	System.out.println("basicFindKey1 = "+ds.getString("basicFindKey1"));
    	System.out.println("basicFindKey2 = "+ds.getString("basicFindKey2"));
    	
    	// 하루전 조립작업 표준서 검색 Data를 삭제 한다
//    	remoteQuery.execute(
//    			BOP_SERVICE_CLASS, 
//    			"deleteOperationOccurenceForInstructionSheets2",
//    			ds);
    	
    	remoteQuery.execute(
    			BOP_SERVICE_CLASS, 
    			"insertOperationOccurenceForInstructionSheets",
    			ds);
    	
    	if(isNewWay==true){
        	resultList = (ArrayList<HashMap>) remoteQuery.execute(
        			BOP_SERVICE_CLASS, 
        			"findOperationOccurenceForInstructionSheetsNew",
        			ds);    		
    	}else{
    		resultList = (ArrayList<HashMap>) remoteQuery.execute(
    				BOP_SERVICE_CLASS, 
    				"findOperationOccurenceForInstructionSheets",
    				ds);
    	}
    	
    	return resultList;
    }
    
    /**
     * [20151211] taeku.jeong 용접점 Occurrence를 찾는  기능 구현을 위한 Query 수행 
     * @param mProductItemId
     * @param findValues
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
	public ArrayList<HashMap> getFindedProductAbsOccurenceId(String mProductItemId,  List<String> findValues) throws Exception{
    	
    	ArrayList<HashMap> resultList = null;
    	
    	DataSet ds = new DataSet();
        ds.put("PRODUCT_ITEM_ID", mProductItemId);
        ds.put("USER_INPUT_VALUES", findValues);
        ds.put("KEY1", findValues.get(0));
        
    	resultList = (ArrayList<HashMap>) remoteQuery.execute(
    			BOP_SERVICE_CLASS, 
    			"findPWProductAbsOccurenceId",
    			ds);
    	
    	return resultList;
    }
    
    /**
     * [SR151207-041][20151215] taeku.jeong 연결이 끊어진 용접접의 ABS_OCC_ID를 이용해 예전의 Occurrence Name을 찾아오는 Function 
     * @param mProductItemId
     * @param findValues
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
	public ArrayList<HashMap> getFindWPProductOccurenceName(String mProductItemId,  List<String> findValues) throws Exception{
    	
    	ArrayList<HashMap> resultList = null;
    	
    	DataSet ds = new DataSet();
        ds.put("PRODUCT_ITEM_ID", mProductItemId);
        ds.put("ABS_OCC_ID_LIST", findValues);
        
    	resultList = (ArrayList<HashMap>) remoteQuery.execute(
    			BOP_SERVICE_CLASS, 
    			"findWPProductOccurenceName",
    			ds);
    	
    	return resultList;
    }
}
