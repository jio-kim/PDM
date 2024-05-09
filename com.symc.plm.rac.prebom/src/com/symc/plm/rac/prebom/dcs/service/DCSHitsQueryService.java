package com.symc.plm.rac.prebom.dcs.service;

import java.util.ArrayList;
import java.util.HashMap;

import com.kgm.common.remote.DataSet;
import com.kgm.common.remote.SYMCRemoteUtil;

public class DCSHitsQueryService {

	private SYMCRemoteUtil remoteUtil;

	private final String SERVICE_CLASS_NAME = "com.kgm.service.DCSHitsService";

	public DCSHitsQueryService() {
		remoteUtil = new SYMCRemoteUtil();
	}

	public Boolean insertDCSHits(DataSet dataSet) {
		Object object = null;

		try {
			object = remoteUtil.execute(SERVICE_CLASS_NAME, "insertDCSHits", dataSet);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return (Boolean) object;
	}

	@SuppressWarnings("unchecked")
	public ArrayList<HashMap<String, Object>> selectDCSHits(DataSet dataSet) {
		ArrayList<HashMap<String, Object>> resultList = null;

		try {
			resultList = (ArrayList<HashMap<String, Object>>) remoteUtil.execute(SERVICE_CLASS_NAME, "selectDCSHits", dataSet);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return resultList;
	}

	@SuppressWarnings("unchecked")
	public ArrayList<HashMap<String, Object>> selectDetailDCSHits(DataSet dataSet) {
		ArrayList<HashMap<String, Object>> resultList = null;

		try {
			resultList = (ArrayList<HashMap<String, Object>>) remoteUtil.execute(SERVICE_CLASS_NAME, "selectDetailDCSHits", dataSet);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return resultList;
	}

	public Boolean saveDCSWorkflowHistory(DataSet dataSet) {
		Object object = null;

		try {
			object = remoteUtil.execute(SERVICE_CLASS_NAME, "saveDCSWorkflowHistory", dataSet);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return (Boolean) object;
	}
}
