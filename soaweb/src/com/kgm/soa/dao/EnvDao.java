package com.kgm.soa.dao;

import java.util.HashMap;
import java.util.List;

import org.apache.ibatis.session.SqlSession;

import com.kgm.common.remote.DataSet;
import com.kgm.soa.mapper.EnvMapper;

public class EnvDao extends AbstractDao {

	public HashMap<String, String> getTCWebEnv() {
		HashMap<String, String> envMap = new HashMap<String, String>();
		List<HashMap<String, String>> result = null;

		try {
			SqlSession session = getSqlSession();
			EnvMapper mapper = session.getMapper(EnvMapper.class);
			result = mapper.getTCWebEnvList();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			sqlSessionClose();
		}

		for (int i = 0; result != null && i < result.size(); i++) {
			envMap.put(result.get(i).get("KEY"), result.get(i).get("VALUE"));
		}
		return envMap;
	}
	
	public HashMap<String, String> getUserInfo(DataSet ds) {
		HashMap<String, String> resultMap = null;

		try {
			SqlSession session = getSqlSession();
			EnvMapper mapper = session.getMapper(EnvMapper.class);
			resultMap = mapper.getUserInfo(ds);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			sqlSessionClose();
		}

		return resultMap;
	}
	
}
