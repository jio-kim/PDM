package com.kgm.soa.mapper;

import java.util.HashMap;
import java.util.List;

import com.kgm.common.remote.DataSet;

public interface EnvMapper {   
    
    public List<HashMap<String, String>> getTCWebEnvList();
    
    public HashMap<String, String> getUserInfo(DataSet ds);
    
}
