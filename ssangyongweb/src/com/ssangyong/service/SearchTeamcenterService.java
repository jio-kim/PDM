package com.ssangyong.service;

import java.util.ArrayList;
import java.util.HashMap;

import com.ssangyong.common.remote.DataSet;
import com.ssangyong.dao.SearchTeamcenterDao;

public class SearchTeamcenterService
{
	public ArrayList<HashMap<String, Object>> searchItemRevision(DataSet ds) throws Exception
	{
		SearchTeamcenterDao dao = new SearchTeamcenterDao();
		return dao.searchItemRevision(ds);
	}

	public ArrayList<HashMap<String, Object>> executeSqlSelect(DataSet ds) throws Exception
	{
		SearchTeamcenterDao dao = new SearchTeamcenterDao();
		return dao.executeSqlSelect(ds);
	}

	public void executeSqlInsert(DataSet ds) throws Exception
	{
		SearchTeamcenterDao dao = new SearchTeamcenterDao();
		dao.executeSqlInsert(ds);
	}

	public void executeSqlUpdate(DataSet ds) throws Exception
	{
		SearchTeamcenterDao dao = new SearchTeamcenterDao();
		dao.executeSqlUpdate(ds);
	}

	public void executeSqlDelete(DataSet ds) throws Exception
	{
		SearchTeamcenterDao dao = new SearchTeamcenterDao();
		dao.executeSqlDelete(ds);
	}
}
