package com.symc.plm.rac.prebom.masterlist.service;

import java.util.HashMap;

import com.kgm.common.remote.DataSet;
import com.kgm.common.remote.SYMCSoaWebUtil;

/**
 * [20160830][ymjang] BOM Load ó�� ���� ���� �̰�
 */
public class MasterListSoaWebService {

	private SYMCSoaWebUtil soaWebUtil;
	
	private final String SERVICE_CLASS_NAME = "com.kgm.soa.service.MasterListService";

	public MasterListSoaWebService() {
		soaWebUtil = new SYMCSoaWebUtil();
	}

	public HashMap<String, Object> loadChildPropMap(DataSet dataSet) throws Exception {
		Object object = null;

		try {
			object = soaWebUtil.execute(SERVICE_CLASS_NAME, "loadChildPropMap", dataSet);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return (HashMap<String, Object>) object;
	}

}
