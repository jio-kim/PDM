package com.symc.plm.me.utils;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.remote.SYMCRemoteUtil;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.rac.cme.biw.apa.search.FindConnectedPartUtility;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;

public class BOPStructureDataUtility {
	
	private final String serviceClassName = "com.ssangyong.service.SYMCBOPStructureService";
	private DateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss SSS");
	
	private TCSession session;
	private String shopId;
	private String lineId;
	private String stationId;

	public BOPStructureDataUtility(){

	}
	
	public void initBOMLineData(TCSession session, String shopItemId){
		this.session = session;
		this.shopId = shopItemId;
	}
	
	public void initBOMLineData(TCComponentBOMLine bomLine){
		
		if(bomLine==null){
			return;
		}
		
		this.session = bomLine.getSession();
		
		String itemType = null;
		String itemId = null;
		TCComponentBOMLine topBOMLine = null;
		try {
			TCComponentBOMWindow window = bomLine.window();
			if(window!=null){
				topBOMLine = window.getTopBOMLine();
				if(topBOMLine!=null){
					TCComponentItem item = topBOMLine.getItem();
					if(item!=null){
						itemType = item.getType();
						itemId = item.getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
					}
				}
			}
		} catch (TCException e1) {
			e1.printStackTrace();
		}
		
		if(itemType==null || (itemType!=null && itemType.trim().equalsIgnoreCase(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM)==false)){
			return;
		}
		
		this.shopId = itemId;
		
		TCComponentBOMLine stationBOMLine = FindConnectedPartUtility.findTypedItemBOMLine(bomLine, SDVTypeConstant.BOP_PROCESS_STATION_ITEM);
		if(stationBOMLine!=null){
			try {
				this.stationId = stationBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
			} catch (TCException e) {
				e.printStackTrace();
			}
		}
		
		TCComponentBOMLine lineBOMLine = FindConnectedPartUtility.findTypedItemBOMLine(bomLine, SDVTypeConstant.BOP_PROCESS_LINE_ITEM);
		if(lineBOMLine!=null){
			try {
				this.lineId = lineBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
			} catch (TCException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("Shop : "+this.shopId);
		System.out.println("Line : " + this.lineId);
		System.out.println("Station : " + this.stationId);
		
	}
	
	public String getShopId() {
		return shopId;
	}
	
	public String getLineId() {
		return lineId;
	}
	
	public String getStationId() {
		return stationId;
	}
	
	public void deleteOldShopStructureData() throws Exception{
		
	    System.out.println("// --------------------------------\n"+
		        "// deleteOldShopStructureData\n"+
		        "// --------------------------------\n"+
		        "// find_Key=TEMP_KEY\n"+
		        "// Start "+df.format(new Date())+"\n"+
		        "// --------------------------------");
	
	    DataSet ds = new DataSet();
	    ds.setString("find_Key", "TEMP_KEY");
	
	     SYMCRemoteUtil remoteUtil = new SYMCRemoteUtil();
	    try {
			remoteUtil.execute(serviceClassName, "deleteOldShopStructureData", ds);
		} catch (Exception e) {
			System.out.println("   Exception -------");
			e.printStackTrace();
			throw e;
		}finally{
			System.out.println("// --------------------------------");
		}
	}

	/**
	 * 주어진 BOM Line 정보를 이용해 BOP Shop의 Structure Data를 저장하고 해당 Data를 찾을 수 있는 Find Key를 Return 한다.
	 * 
	 * @param bomLine
	 * @return
	 */
	public String makeNewBOPInformationData(String shopId, String isLatestReleased) throws Exception {
		
		String searchKey = null;
		
        String userId = null;
        try {
        	if(session!=null){
        		userId = session.getUser().getUserId();
        	}
		} catch (TCException e1) {
			e1.printStackTrace();
		}
        
        DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
		String timeStr = df.format(new Date());
        
		if(userId!=null && userId.trim().length()>0){
			searchKey  = (userId.trim().toUpperCase())+"_"+timeStr.trim();
		}
		
		if(searchKey==null){
			System.out.println("// --------------------------------\n"+
					"// Cancellation (Making Condition Error) : "+df.format(new Date())+"\n"+
					"// --------------------------------\n");
			return null;
		}
		
        System.out.println("// --------------------------------\n"+
    	        "// saveShopStructureData\n"+
    	        "// --------------------------------\n"+
    	        "// find_Key="+searchKey + "\n"+
    	        "// shop_item_Id="+shopId + "\n"+
		        "// Start "+df.format(new Date())+"\n"+
    	        "// --------------------------------");

        DataSet ds = new DataSet();
        ds.setString("find_Key", searchKey);
        ds.setString("shop_Item_Id", shopId);
        // 2020-09-02 seho bop 구조 만들때 리비전 룰을 적용해서 만들도록 수정.
        if(isLatestReleased != null)
        	ds.setString("isLatestReleased", isLatestReleased);

         SYMCRemoteUtil remoteUtil = new SYMCRemoteUtil();
        try {
			remoteUtil.execute(serviceClassName, "saveShopStructureData", ds);
		} catch (Exception e) {
			System.out.println("   Exception -------");
			e.printStackTrace();
			throw e;
		}finally{
			System.out.println("// --------------------------------\n"+
					"// End : "+df.format(new Date())+"\n"+
					"// --------------------------------\n");
		}
    	
        return searchKey;
	}
	
	public int getAllStationCount(String findKey){
		
		int resultCount = -1;

        System.out.println("// --------------------------------\n"+
    	        "// getAllStationCount\n"+
    	        "// --------------------------------\n"+
    	        "// find_Key="+findKey + "\n"+
    	        "// line_Item_Id="+lineId + "\n"+
		        "// Start "+df.format(new Date())+"\n"+
    	        "// --------------------------------");
        
        DataSet ds = new DataSet();
        ds.setString("find_Key", findKey);
        
        ArrayList<HashMap> resultList = null;
        SYMCRemoteUtil remoteUtil = new SYMCRemoteUtil();
    	try {
			resultList = (ArrayList<HashMap>) remoteUtil.execute(
					serviceClassName, 
					"getAllStationCount",
					ds);
			
	    	if(resultList!=null && resultList.size()>0){
	    		HashMap<String, Object> hash = resultList.get(0);
	    		Object aObject = hash.get("STATION_COUNT");
	    		if(aObject instanceof BigDecimal){
	    			resultCount = ((BigDecimal)aObject).intValue();
	    		}
	    	}

	    	System.out.println("   All Station Count = "+resultCount);
	    	
		} catch (Exception e) {
			System.out.println("   Exception -------");
			e.printStackTrace();
		}finally{
			System.out.println("// --------------------------------\n"+
					"// End : "+df.format(new Date())+"\n"+
					"// --------------------------------\n");
		}
    	
    	return resultCount;
	}	
	
	public List<HashMap> getPredecessorLines(String findKey, String lineId){

        System.out.println("// --------------------------------\n"+
    	        "// getPredecessorLines\n"+
    	        "// --------------------------------\n"+
    	        "// find_Key="+findKey + "\n"+
    	        "// line_Item_Id="+lineId + "\n"+
		        "// Start "+df.format(new Date())+"\n"+
    	        "// --------------------------------");
        
        DataSet ds = new DataSet();
        ds.setString("find_Key", findKey);
        ds.setString("line_Item_Id", lineId);
        
        ArrayList<HashMap> resultList = null;
        
        if(lineId==null || (lineId!=null && lineId.trim().length()<1)){
			System.out.println("// --------------------------------\n"+
					"// Cancellation : "+df.format(new Date())+"\n"+
					"// --------------------------------\n");
        	return resultList;
        }
        
        SYMCRemoteUtil remoteUtil = new SYMCRemoteUtil();
    	try {
			resultList = (ArrayList<HashMap>) remoteUtil.execute(
					serviceClassName, 
					"getPredecessorLines",
					ds);
			
	    	for (int j = 0; resultList!=null &&  j < resultList.size(); j++) {
	    		HashMap<String, Object> hash = resultList.get(j);
	    		String alineId = (String)  hash.get("LINE_ID");
	    		String lineAppNodePuid = (String) hash.get("APP_NODE_PUID");
	    		System.out.println("Line["+j+"] "+alineId +" ["+lineAppNodePuid+"]");
			}

		} catch (Exception e) {
			System.out.println("   Exception -------");
			e.printStackTrace();
		}finally{
			System.out.println("// --------------------------------\n"+
					"// End : "+df.format(new Date())+"\n"+
					"// --------------------------------\n");
		}
    	
    	return resultList;
	}	

	public List<HashMap> getPredecessorStationsAtLine(String findKey, String stationId){

        System.out.println("// --------------------------------\n"+
	        "// getPredecessorStationsAtLine\n"+
	        "// --------------------------------\n"+
	        "// find_Key="+findKey + "\n"+
	        "// station_Item_Id="+stationId + "\n"+
	        "// Start "+df.format(new Date())+"\n"+
	        "// --------------------------------");
    	
        DataSet ds = new DataSet();
        ds.setString("find_Key", findKey);
        ds.setString("station_Item_Id", stationId);
        
        ArrayList<HashMap> resultList = null;

        if(stationId==null || (stationId!=null && stationId.trim().length()<1)){
			System.out.println("// --------------------------------\n"+
					"// Cancellation : "+df.format(new Date())+"\n"+
					"// --------------------------------\n");
        	return resultList;
        }

        SYMCRemoteUtil remoteUtil = new SYMCRemoteUtil();
    	try {
			resultList = (ArrayList<HashMap>) remoteUtil.execute(
					serviceClassName, 
					"getPredecessorStationsAtLine",
					ds);
			
	    	for (int j = 0; resultList!=null &&  j < resultList.size(); j++) {
	    		HashMap<String, Object> hash = resultList.get(j);
	    		String aStationId = (String) hash.get("STATION_ID");
	    		String aAppNodePuid = (String) hash.get("APP_NODE_PUID");
	    		System.out.println("   Station ["+j+"] "+aStationId +" ["+aAppNodePuid+"]");
			}
	    	System.out.println("Total Predecessor Count at line = "+resultList.size());
		} catch (Exception e) {
			System.out.println("    Exception ----");
			e.printStackTrace();
		}finally{
			System.out.println("// --------------------------------\n"+
					"// End : "+df.format(new Date())+"\n"+
					"// --------------------------------\n");
		}
    	
    	return resultList;
	}	
	
	
	public List<HashMap> getPredecessorStationsAtAllLine(String findKey, String lineId, String stationId){

        System.out.println("// --------------------------------\n"+
	        "// getPredecessorStationsAtAllLine\n"+
	        "// --------------------------------\n"+
	        "// find_Key="+findKey + "\n"+
	        "// line_Item_Id="+lineId + "\n"+
	        "// station_Item_Id="+stationId + "\n"+
	        "// Start "+df.format(new Date())+"\n"+
	        "// --------------------------------");
    	
        DataSet ds = new DataSet();
        ds.setString("find_Key", findKey);
        ds.setString("line_Item_Id", lineId);
        ds.setString("station_Item_Id", stationId);
        
        ArrayList<HashMap> resultList = null;
        
        if(lineId==null || (lineId!=null && lineId.trim().length()<1)){
			System.out.println("// --------------------------------\n"+
					"// Cancellation : "+df.format(new Date())+"\n"+
					"// --------------------------------\n");
        	return resultList;
        }
        if(stationId==null || (stationId!=null && stationId.trim().length()<1)){
			System.out.println("// --------------------------------\n"+
					"// Cancellation : "+df.format(new Date())+"\n"+
					"// --------------------------------\n");
        	return resultList;
        }
        
        SYMCRemoteUtil remoteUtil = new SYMCRemoteUtil();
    	try {
			resultList = (ArrayList<HashMap>) remoteUtil.execute(
					serviceClassName, 
					"getPredecessorStationsAtAllLine",
					ds);
			
	    	for (int j = 0; resultList!=null &&  j < resultList.size(); j++) {
	    		HashMap<String, Object> hash = resultList.get(j);
	    		String aStationId = (String) hash.get("STATION_ID");
	    		String aAppNodePuid = (String) hash.get("APP_NODE_PUID");
	    		System.out.println("   Station["+j+"] "+aStationId +" ["+aAppNodePuid+"]");
			}
	    	System.out.println("Total Predecessor All Line Count = "+resultList.size());
	    	
		} catch (Exception e) {
			System.out.println("   Exception -------");
			e.printStackTrace();
		}finally{
			System.out.println("// --------------------------------\n"+
					"// End : "+df.format(new Date())+"\n"+
					"// --------------------------------\n");
		}
    	
    	return resultList;
	}	

	/**
	 * PERT 구성되지 않은 Sattion에 대한 정보를 찾아서 List 해준다.
	 * @param findKey
	 * @return
	 */
	public List<HashMap> getUnPertedStationList(String findKey){

        System.out.println("// --------------------------------\n"+
	        "// getUnPertedStationList\n"+
	        "// --------------------------------\n"+
	        "// find_Key="+findKey + "\n"+
	        "// Start "+df.format(new Date())+"\n"+
	        "// --------------------------------");
        
        DataSet ds = new DataSet();
        ds.setString("find_Key", findKey);
        
        List<HashMap> resultList = null;
        SYMCRemoteUtil remoteUtil = new SYMCRemoteUtil();
    	try {
			resultList = (List<HashMap>)remoteUtil.execute(
					serviceClassName, 
					"getUnPertedStationList",
					ds);
			
			for (int i = 0; resultList!=null && i < resultList.size(); i++) {
				HashMap aHashMap = resultList.get(i);
				String parentId = (String) aHashMap.get("PARENT_ID");
				String childId = (String) aHashMap.get("CHILD_ID");
				
				System.out.println("    PERT ERROR ["+i+"] : "+parentId+" <-> "+childId);
			}
			System.out.println("Total Un PERT Count = "+resultList.size());
		} catch (Exception e) {
			System.out.println("   Exception -------");
			e.printStackTrace();
		}finally{
			System.out.println("// --------------------------------\n"+
					"// End : "+df.format(new Date())+"\n"+
					"// --------------------------------\n");
		}
    	
    	return resultList;
	}
	
	public List<HashMap> getAllFindKeyList(){
		
		String searchKey = null;
		
        String userId = null;
        try {
        	if(session!=null){
        		userId = session.getUser().getUserId();
        	}
		} catch (TCException e1) {
			e1.printStackTrace();
		}
		
        System.out.println("// --------------------------------\n"+
    	        "// keyCodeListFind\n"+
    	        "// --------------------------------\n"+
    	        "// find_Key = TEMP_STR\n"+
		        "// Start : "+df.format(new Date())+"\n"+
		        "// userId = "+userId+"\n"+
    	        "// --------------------------------");
		
		List<HashMap> resultList = null;
    	
        DataSet ds = new DataSet();
        ds.setString("find_Key", "TEMP_STR");
        ds.setString("userId", userId);
        
        SYMCRemoteUtil remoteUtil = new SYMCRemoteUtil();
    	try {
			resultList = (List<HashMap>)remoteUtil.execute(
					serviceClassName, 
					"keyCodeListFind",
					ds);
			
			for (int i = 0; resultList!=null && i < resultList.size(); i++) {
				HashMap aHashMap = resultList.get(i);
				String shopItemId = (String) aHashMap.get("SHOP_ITEM_ID");
				String keyCode = (String) aHashMap.get("KEY_CODE");
				System.out.println("   FindKey ["+i+"] : "+shopItemId+" ["+keyCode+"]");
			}
			
			System.out.println("Total All Finded Key Count = "+resultList.size());
		} catch (Exception e) {
			System.out.println("   Exception -------");
			e.printStackTrace();
		}finally{
			System.out.println("// --------------------------------\n"+
					"// End : "+df.format(new Date())+"\n"+
					"// --------------------------------\n");
		}

    	return resultList;
	}
	
	public String getLatestKeyCodeForShop(String shopItemId){
		
		String searchKey = null;
		
        String userId = null;
        try {
        	if(session!=null){
        		userId = session.getUser().getUserId();
        	}
		} catch (TCException e1) {
			e1.printStackTrace();
		}
		
        System.out.println("// --------------------------------\n"+
    	        "// getLatestKeyCodeForShop\n"+
    	        "// --------------------------------\n"+
    	        "// shop_Item_Id = "+shopItemId+"\n"+
		        "// Start : "+df.format(new Date())+"\n"+
		        "// userId = "+userId+"\n"+
    	        "// --------------------------------");
		
        String latestKeyCode = null;
		List<HashMap> resultList = null;
    	
        DataSet ds = new DataSet();
        ds.setString("shop_Item_Id", shopItemId);
        ds.setString("userId", userId);
        
        SYMCRemoteUtil remoteUtil = new SYMCRemoteUtil();
    	try {
			resultList = (List<HashMap>)remoteUtil.execute(
					serviceClassName, 
					"getLatestKeyCodeForShop",
					ds);
			
			if(resultList!=null){
				System.out.println("resultList.size() = "+resultList.size());	
			}else{
				System.out.println("resultList.size() = null");
			}
			
			for (int i = 0; resultList!=null && i < resultList.size(); i++) {
				HashMap aHashMap = resultList.get(i);
				latestKeyCode = (String) aHashMap.get("KEY_CODE");
				System.out.println("   Key Code ["+i+"] : "+latestKeyCode);
			}

		} catch (Exception e) {
			System.out.println("   Exception -------");
			e.printStackTrace();
		}finally{
			System.out.println("// --------------------------------\n"+
					"// End : "+df.format(new Date())+"\n"+
					"// --------------------------------\n");
		}

    	return latestKeyCode;
	}
	
}
