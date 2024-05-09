package com.kgm.service;

import com.kgm.common.remote.DataSet;
import com.kgm.dao.PartMappingDao;

public class PartMappingService {

	public void insertTrim(DataSet ds) throws Exception{
		PartMappingDao dao = new PartMappingDao();
		dao.insertTrim(ds);		
	}
}
