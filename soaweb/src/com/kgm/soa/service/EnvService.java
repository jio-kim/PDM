package com.kgm.soa.service;

import java.util.HashMap;

import com.kgm.common.remote.DataSet;
import com.kgm.soa.dao.EnvDao;

public class EnvService {
	
    public HashMap<String, String> getTCWebEnv() {
        EnvDao dao = new EnvDao();
        return (HashMap<String, String>)dao.getTCWebEnv();
    }
    
    public HashMap<String, String> getUserInfo(String userId) {
        EnvDao dao = new EnvDao();
        
        DataSet ds = new DataSet();
        ds.put("PUSER_ID", userId);
        
        return (HashMap<String, String>)dao.getUserInfo(ds);
    }
    
}
