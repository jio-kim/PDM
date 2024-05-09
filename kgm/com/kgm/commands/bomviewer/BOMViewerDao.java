package com.kgm.commands.bomviewer;

import java.util.ArrayList;
import java.util.HashMap;

import com.kgm.common.remote.DataSet;
import com.kgm.common.remote.SYMCRemoteUtil;

@SuppressWarnings("unchecked")
public class BOMViewerDao {
	private SYMCRemoteUtil remoteQuery;
    public static final String BOM_VIEWER_SERVICE_CLASS = "com.kgm.service.BOMViewerService";
    
	public BOMViewerDao() {
		this.remoteQuery = new SYMCRemoteUtil();
	}
	
	/**
	 * 
	 * @param sParentNo
	 * @param sParentRev
	 * @return
	 * @throws Exception
	 */
	public ArrayList<HashMap<String, String>> selectBOMViewer(String sPUID) throws Exception {
		DataSet ds = new DataSet();
		ds.setString(BOMViewerConstants.PROP_PARENT_UID, sPUID);
		
		return (ArrayList<HashMap<String, String>>)remoteQuery.execute(BOM_VIEWER_SERVICE_CLASS, "selectBOMViewer", ds);
	}
}
