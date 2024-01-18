package com.ssangyong.service;

import java.util.ArrayList;
import java.util.HashMap;

import com.ssangyong.common.remote.DataSet;
import com.ssangyong.dao.DCSHitsDao;

public class DCSHitsService {

	private DCSHitsDao dao;

	public DCSHitsService() {

	}

	public boolean insertDCSHits(DataSet dataSet) {
		dao = new DCSHitsDao();

		return dao.insertDCSHits(dataSet);
	}

	public ArrayList<HashMap<String, Object>> selectDCSHits(DataSet dataSet) {
		dao = new DCSHitsDao();

		return dao.selectDCSHits(dataSet);
	}

	public ArrayList<HashMap<String, Object>> selectDetailDCSHits(DataSet dataSet) {
		dao = new DCSHitsDao();

		return dao.selectDetailDCSHits(dataSet);
	}

	public boolean saveDCSWorkflowHistory(DataSet dataSet) {
		dao = new DCSHitsDao();
		
		return dao.saveDCSWorkflowHistory(dataSet);	
	}
	
}
