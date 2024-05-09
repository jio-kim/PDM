package com.kgm.service;

import java.util.ArrayList;
import java.util.HashMap;

import com.kgm.common.remote.DataSet;
import com.kgm.dao.ModuleBomValidationDao;

/**
 * ��� BOM�� ���� ����
 * [SR140722-022][20140522] swyoon ���� ����
 */
public class ModuleBomValidationService {

	@SuppressWarnings("unchecked")
	public ArrayList<HashMap<String, Object>> validateModule(DataSet ds) throws Exception{
		ModuleBomValidationDao dao = new ModuleBomValidationDao();
		return dao.validateModule(ds);
	}
	
	@SuppressWarnings("rawtypes")
	public ArrayList getModulePart(DataSet ds) throws Exception{
		ModuleBomValidationDao dao = new ModuleBomValidationDao();
		return dao.getModulePart(ds);
	}
	
	@SuppressWarnings("rawtypes")
	public ArrayList getModuleValidationResult(DataSet ds) throws Exception{
		ModuleBomValidationDao dao = new ModuleBomValidationDao();
		return dao.getModuleValidationResult(ds);
	}
}
