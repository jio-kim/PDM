package com.symc.work.service;

import java.util.HashMap;
import java.util.List;

import com.symc.common.dao.TcCommonDao;
import com.symc.work.model.EcoBomVO;


public class TcEcoService {

    @SuppressWarnings("unchecked")
    public List<EcoBomVO> getEcoBomList(String ecoNo, String projectId) throws Exception {
	    HashMap<String, Object> param = new HashMap<String, Object>();
        param.put("ecoNo", ecoNo);
        param.put("projectId", projectId);
        return (List<EcoBomVO>) TcCommonDao.getTcCommonDao().selectList("com.symc.tc.eco.getEcoBomList", param);
	}
}
