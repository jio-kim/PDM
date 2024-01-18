package com.ssangyong.soa.mapper;

import java.util.HashMap;
import java.util.List;

import com.ssangyong.common.remote.DataSet;

public interface TcLovMapper {   
    
    public List<HashMap<String, Object>> getLOVVList(DataSet ds);
    
    public List<HashMap<String, Object>> getLOVDescList(DataSet ds);
    
}
