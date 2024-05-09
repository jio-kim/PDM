package com.kgm.service;

import java.util.HashMap;
import java.util.List;

import com.kgm.common.remote.DataSet;
import com.kgm.dao.GetParentDao;

public class GetParentService {
	public List<HashMap<String, String>> searchUpperBOM(DataSet ds) throws Exception {
		GetParentDao dao = new GetParentDao();
        return dao.searchUpperBOM(ds);
    }
	
	public List<HashMap<String, String>> searchAll(DataSet ds) throws Exception {
		GetParentDao dao = new GetParentDao();
        return dao.searchAll(ds);
    }
	
	public List<HashMap<String, String>> searchLatestReleased(DataSet ds) throws Exception {
		GetParentDao dao = new GetParentDao();
		return dao.searchLatestReleased(ds);
	}
	
	public List<HashMap<String, String>> searchLatestWorking(DataSet ds) throws Exception {
		GetParentDao dao = new GetParentDao();
		return dao.searchLatestWorking(ds);
	}
	
	public List<HashMap<String, String>> isConnectedFunction(DataSet ds) throws Exception {
		GetParentDao dao = new GetParentDao();
		return dao.isConnectedFunction(ds);
	}

    public List<HashMap<String, Object>> whereUsedStructure(DataSet ds) throws Exception {
        GetParentDao dao = new GetParentDao();
        return dao.whereUsedStructure(ds);
    }
    
    public List<HashMap<String, Object>> whereUsedPreBOMStructure(DataSet ds) throws Exception {
        GetParentDao dao = new GetParentDao();
        return dao.whereUsedPreBOMStructure(ds);
    }
}
