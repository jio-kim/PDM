package com.symc.plm.rac.prebom.masterlist.util;

import java.util.ArrayList;
import java.util.HashMap;

import com.kgm.common.remote.DataSet;
import com.kgm.common.remote.SYMCRemoteUtil;
import com.kgm.common.utils.CustomUtil;
import com.teamcenter.rac.kernel.TCSession;

public class WebUtil {
	public static HashMap<String, String> getPart(String itemId) throws Exception{
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		DataSet ds = new DataSet();
		
		ds.put("ITEM_ID", itemId);
		try {
			
			ArrayList<HashMap<String, String>> list = (ArrayList<HashMap<String, String>>)remote.execute("com.kgm.service.MasterListService", "getPart", ds);
			
			if( list != null && !list.isEmpty()){
				return list.get(0);
			}else{
				return null;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw e;
		}
	}
	
	public static ArrayList<HashMap> getWorkingCCN() throws Exception{
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		DataSet ds = new DataSet();
		try {
			TCSession session = CustomUtil.getTCSession();
			String user_ID = session.getUser().getUserId();
			ds.put("USER_ID", user_ID);
			
			return (ArrayList<HashMap>)remote.execute("com.kgm.service.MasterListService", "getWorkingCCN", ds);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw e;
		}
	}
	
	public static boolean isExistPartName(String partName) throws Exception{
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		DataSet ds = new DataSet();
		try {
			ds.put("ITEM_NAME", partName);
			
			Object obj = remote.execute("com.kgm.service.MasterListService", "getExistPart", ds);
			if( obj != null){
				return true;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw e;
		}
		
		return false;
	}
	
	public static boolean isExistPartNo(String partNo) throws Exception{
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		DataSet ds = new DataSet();
		try {
			ds.put("ITEM_ID", partNo);
			
			Object obj = remote.execute("com.kgm.service.MasterListService", "getExistPart", ds);
			if( obj != null){
				return true;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw e;
		}
		
		return false;
	}
	
}
