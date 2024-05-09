package com.kgm.soa.mapper;

import java.util.ArrayList;
import java.util.HashMap;

import com.kgm.common.remote.DataSet;

public interface MasterListMapper {   
    
    public ArrayList<HashMap<String, Object>> getDCSList(DataSet ds);
    
    public String getSysGuid();
    
}
