package com.ssangyong.soa.service;

import java.util.HashMap;
import java.util.List;

import com.ssangyong.common.remote.DataSet;
import com.ssangyong.soa.dao.TcLovDao;

public class TcLOVService {

    /**
     * KEY_LOV ID를 가지고 VALUE를 조회한다.
     * @param id
     * @return
     */
    public List<HashMap<String, Object>> getLOVVList(String id) {
    	TcLovDao dao = new TcLovDao();
    	
    	DataSet ds = new DataSet();
        ds.put("id", id);
    	
        return dao.getLOVVList(ds);
    }
	
    /**
     * KEY_LOV ID를 가지고 VALUE, DESCRIPTION를 조회한다.
     * @param id
     * @return
     */
    public List<HashMap<String, Object>> getLOVDescList(String id) {
    	TcLovDao dao = new TcLovDao();
    	
    	DataSet ds = new DataSet();
        ds.put("id", id);
    	
        return dao.getLOVDescList(ds);
    }

}
