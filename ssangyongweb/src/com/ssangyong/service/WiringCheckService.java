package com.ssangyong.service;

import java.util.ArrayList;
import java.util.HashMap;

import com.ssangyong.common.remote.DataSet;
import com.ssangyong.dao.WiringCheckDao;

public class WiringCheckService
{
	public ArrayList<HashMap<String, Object>> findUserInVNet(DataSet ds) throws Exception
	{
		WiringCheckDao dao = new WiringCheckDao();
		return dao.findUserInVNet(ds);
	}

	public ArrayList<HashMap<String, Object>> getWiringMailList(DataSet ds) throws Exception
	{
		WiringCheckDao dao = new WiringCheckDao();
		return dao.getWiringMailList(ds);
	}

	public void deleteWiringMailList() throws Exception
	{
		WiringCheckDao dao = new WiringCheckDao();
		dao.deleteWiringMailList();
	}

	public void insertWiringMailList(DataSet ds) throws Exception
	{
		WiringCheckDao dao = new WiringCheckDao();
		dao.insertWiringMailList(ds);
	}

	public ArrayList<HashMap<String, Object>> getWiringCategoryNo() throws Exception
	{
		WiringCheckDao dao = new WiringCheckDao();
		return dao.getWiringCategoryNo();
	}

	public void deleteWiringCategoryNo() throws Exception
	{
		WiringCheckDao dao = new WiringCheckDao();
		dao.deleteWiringCategoryNo();
	}

	public void insertWiringCategoryNo(DataSet ds) throws Exception
	{
		WiringCheckDao dao = new WiringCheckDao();
		dao.insertWiringCategoryNo(ds);
	}

}
