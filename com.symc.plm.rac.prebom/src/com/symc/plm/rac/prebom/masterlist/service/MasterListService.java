package com.symc.plm.rac.prebom.masterlist.service;

import java.util.List;
import java.util.Map;

import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.remote.SYMCRemoteUtil;

/**
 * [20161012][ymjang] BOM Loading 贸府 肺流 辑滚 捞包
 */
public class MasterListService {

	private SYMCRemoteUtil remoteUtil;

	private final String SERVICE_CLASS_NAME = "com.ssangyong.service.MasterListService";

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
