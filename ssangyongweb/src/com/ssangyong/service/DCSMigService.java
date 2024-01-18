package com.ssangyong.service;

import java.util.ArrayList;
import java.util.HashMap;

import com.ssangyong.common.remote.DataSet;
import com.ssangyong.dao.DCSMigDao;

public class DCSMigService {

	private DCSMigDao dao;

	public DCSMigService() {

	}

	public ArrayList<HashMap<String, Object>> selectWorkflowInfoList(DataSet dataSet) {
		dao = new DCSMigDao();

		return dao.selectWorkflowInfoList(dataSet);
	}

	public ArrayList<HashMap<String, Object>> selectMyWorkflowInfoList(DataSet dataSet) {
		dao = new DCSMigDao();

		return dao.selectMyWorkflowInfoList(dataSet);
	}

}
