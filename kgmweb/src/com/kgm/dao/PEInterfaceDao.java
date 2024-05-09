package com.kgm.dao;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.ibatis.session.SqlSession;

import com.kgm.common.remote.DataSet;
import com.kgm.common.util.LogUtil;
import com.kgm.mapper.PEInterfaceMapper;

public class PEInterfaceDao extends AbstractDao {
	
	public  ArrayList<HashMap<String, Object>> getProductEndItemABSOccPuidList(DataSet ds) throws Exception{
		ArrayList<HashMap<String, Object>> list = null;
	    try {
	        SqlSession session = getSqlSession();
	        PEInterfaceMapper mapper = session.getMapper(PEInterfaceMapper.class);
	        list = mapper.getProductEndItemABSOccPuidList(ds);
	    } catch (Exception e) {
	        e.printStackTrace();
	        
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
	        
	    } finally {
	        sqlSessionClose();
	    }
	
	    return list;
	}
	
}
