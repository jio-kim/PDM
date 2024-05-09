package com.kgm.service;

import java.util.ArrayList;
import java.util.HashMap;

import com.kgm.common.remote.DataSet;
import com.kgm.dao.DCSHitsDao;

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
