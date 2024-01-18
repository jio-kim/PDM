package com.ssangyong.soa.service;

import java.util.HashMap;

import com.ssangyong.common.remote.DataSet;
import com.ssangyong.soa.dao.EnvDao;

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
