package com.symc.plm.rac.prebom.masterlist.service;

import java.util.List;
import java.util.Map;

import com.kgm.common.remote.DataSet;
import com.kgm.common.remote.SYMCRemoteUtil;

/**
 * [20161012][ymjang] BOM Loading ó�� ���� ���� �̰�
 */
public class MasterListService {

	private SYMCRemoteUtil remoteUtil;

	private final String SERVICE_CLASS_NAME = "com.kgm.service.MasterListService";

	public MasterListService() {
		remoteUtil = new SYMCRemoteUtil();
	}

	public List<Map<String, Object>> getMLMLoadProp(DataSet dataSet) throws Exception {
		Object object = null;

		try {
			object = remoteUtil.execute(SERVICE_CLASS_NAME, "getMLMLoadProp", dataSet);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return (List<Map<String, Object>>) object;
	}
}
