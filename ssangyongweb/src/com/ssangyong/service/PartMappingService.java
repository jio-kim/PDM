package com.ssangyong.service;

import com.ssangyong.common.remote.DataSet;
import com.ssangyong.dao.PartMappingDao;

public class PartMappingService {

	public void insertTrim(DataSet ds) throws Exception{
		PartMappingDao dao = new PartMappingDao();
		dao.insertTrim(ds);		
	}
}
