package com.ssangyong.service;

import java.util.ArrayList;
import java.util.HashMap;

import com.ssangyong.common.remote.DataSet;
import com.ssangyong.dao.DCSVisionNetDao;

public class DCSVisionNetService {

	private DCSVisionNetDao dao;

	public DCSVisionNetService() {

	}

	public ArrayList<HashMap<String, Object>> selectVNetTeamList(DataSet dataSet) {
		dao = new DCSVisionNetDao();

		return dao.selectVNetTeamList(dataSet);
	}

	public ArrayList<HashMap<String, Object>> selectVNetTeamHistList(DataSet dataSet) {
		dao = new DCSVisionNetDao();

		return dao.selectVNetTeamHistList(dataSet);
	}

	public ArrayList<HashMap<String, Object>> selectVNetUserList(DataSet dataSet) {
		dao = new DCSVisionNetDao();

		return dao.selectVNetUserList(dataSet);
	}
	
	public ArrayList<HashMap<String, Object>> getVnetAndTcLiveSameTeamCode(DataSet dataSet) {
		dao = new DCSVisionNetDao();

		return dao.getVnetAndTcLiveSameTeamCode(dataSet);
	}
	
	public ArrayList<HashMap<String, Object>> getVnetTeamName(DataSet dataSet) {
		dao = new DCSVisionNetDao();

		return dao.getVnetTeamName(dataSet);
	}
	
}
