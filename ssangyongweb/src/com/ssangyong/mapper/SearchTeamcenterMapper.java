package com.ssangyong.mapper;

import java.util.ArrayList;
import java.util.HashMap;

import com.ssangyong.common.remote.DataSet;

public interface SearchTeamcenterMapper
{
	public ArrayList<HashMap<String, Object>> searchItemRevision(DataSet ds) throws Exception;
	public ArrayList<HashMap<String, Object>> executeSqlSelect(DataSet ds) throws Exception;
	public void executeSqlInsert(DataSet ds) throws Exception;
	public void executeSqlUpdate(DataSet ds) throws Exception;
	public void executeSqlDelete(DataSet ds) throws Exception;
}
