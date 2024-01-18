package com.ssangyong.service;

import java.util.ArrayList;
import java.util.HashMap;

import com.ssangyong.common.remote.DataSet;
import com.ssangyong.dao.PreOSpecDao;

public class PreOSpecService {
	
	public void insertTrim(DataSet ds) throws Exception{
		PreOSpecDao dao = new PreOSpecDao();
		dao.insertTrim(ds);		
	}
	
	public ArrayList<HashMap<String, String>> selectPreOSpecMandatory(DataSet ds) throws Exception {
		PreOSpecDao dao = new PreOSpecDao();
		return dao.selectPreOSpecMandatory(ds);
	}
	
	public ArrayList<HashMap<String, String>> selectPreOSpecMandatoryInfo(DataSet ds) throws Exception {
		PreOSpecDao dao = new PreOSpecDao();
		return dao.selectPreOSpecMandatoryInfo(ds);
	}
	
	public ArrayList<HashMap<String, String>> selectPreOSpecMandatoryTrim(DataSet ds) throws Exception {
		PreOSpecDao dao = new PreOSpecDao();
		return dao.selectPreOSpecMandatoryTrim(ds);
	}
	
	public void insertPreOSpecMandatoryInfo(DataSet ds) throws Exception {
		PreOSpecDao dao = new PreOSpecDao();
		dao.insertPreOSpecMandatoryInfo(ds);
	}
	
	public void insertPreOSpecMandatoryTrim(DataSet ds) throws Exception {
		PreOSpecDao dao = new PreOSpecDao();
		dao.insertPreOSpecMandatoryTrim(ds);
	}
	
	public void deletePreOSpecMandatoryInfo(DataSet ds) throws Exception {
		PreOSpecDao dao = new PreOSpecDao();
		dao.deletePreOSpecMandatoryInfo(ds);
	}
	
	public void deletePreOSpecMandatoryTrim(DataSet ds) throws Exception {
		PreOSpecDao dao = new PreOSpecDao();
		dao.deletePreOSpecMandatoryTrim(ds);
	}
}
