package com.symc.plm.rac.prebom.dcs.service;

import java.util.Map;

import com.ssangyong.common.remote.SYMCRemoteUtil;

public class ENVService {

	private SYMCRemoteUtil remoteUtil;

	private final String SERVICE_CLASS_NAME = "com.ssangyong.service.EnvService";

	public ENVService() {
		remoteUtil = new SYMCRemoteUtil();
	}

    @SuppressWarnings("unchecked")
	public Map<String, String> getTCWebEnv() {
    	
		Object object = null;

		try {
			object = remoteUtil.execute(SERVICE_CLASS_NAME, "getTCWebEnv", null);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return (Map<String, String>) object;
    }
	
}
