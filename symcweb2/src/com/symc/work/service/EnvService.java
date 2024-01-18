package com.symc.work.service;

import java.util.HashMap;
import java.util.List;

import com.symc.common.dao.TcCommonDao;

/**
 * 환경변수 Service
 *
 */
public class EnvService {

    private HashMap<String, String> envMap;

    public EnvService() {        
    }
    
    @SuppressWarnings("unchecked")
    public EnvService(TcCommonDao tcCommonDao) {
        envMap = new HashMap<String, String>();
        List<HashMap<String, String>> envList = (List<HashMap<String, String>>) tcCommonDao.selectList("com.symc.env.getTCWebEnvList");
        for (int i = 0; envList != null && i < envList.size(); i++) {
            envMap.put(envList.get(i).get("KEY"), envList.get(i).get("VALUE"));
        } 
    }

    public HashMap<String, String> getTCWebEnv() {
        return this.envMap;
    }
}
