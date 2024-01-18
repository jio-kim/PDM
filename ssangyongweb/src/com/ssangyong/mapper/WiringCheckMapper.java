package com.ssangyong.mapper;

import java.util.ArrayList;
import java.util.HashMap;

import com.ssangyong.common.remote.DataSet;

public interface WiringCheckMapper
{
	public ArrayList<HashMap<String, Object>> findUserInVNet(DataSet ds) throws Exception;

	public ArrayList<HashMap<String, Object>> getWiringMailList(DataSet ds) throws Exception;

	public void deleteWiringMailList() throws Exception;

	public void insertWiringMailList(DataSet ds) throws Exception;

	public ArrayList<HashMap<String, Object>> getWiringCategoryNo() throws Exception;

	public void deleteWiringCategoryNo() throws Exception;

	public void insertWiringCategoryNo(DataSet ds) throws Exception;
}
