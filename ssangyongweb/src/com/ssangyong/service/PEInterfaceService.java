package com.ssangyong.service;

import java.util.ArrayList;
import java.util.HashMap;

import com.ssangyong.common.remote.DataSet;
import com.ssangyong.dao.PEInterfaceDao;

public class PEInterfaceService {

	
	public  ArrayList<HashMap<String, Object>> getProductEndItemABSOccPuidList(DataSet ds) throws Exception{
		PEInterfaceDao dao = new PEInterfaceDao();
		return dao.getProductEndItemABSOccPuidList(ds);
	}
	
}
