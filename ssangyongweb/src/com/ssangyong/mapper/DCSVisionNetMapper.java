package com.ssangyong.mapper;

import java.util.ArrayList;
import java.util.HashMap;

import com.ssangyong.common.remote.DataSet;

public interface DCSVisionNetMapper {

	public ArrayList<HashMap<String, Object>> selectVNetTeamList(DataSet dataSet);

	public ArrayList<HashMap<String, Object>> selectVNetTeamHistList(DataSet dataSet);
	
	public ArrayList<HashMap<String, Object>> selectVNetUserList(DataSet dataSet);
	
	public ArrayList<HashMap<String, Object>> getVnetAndTcLiveSameTeamCode(DataSet dataSet);
	
	public ArrayList<HashMap<String, Object>> getVnetTeamName(DataSet dataSet);

}