package com.ssangyong.soa.dao;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.ibatis.session.SqlSession;

import com.ssangyong.common.remote.DataSet;
import com.ssangyong.soa.mapper.MasterListMapper;


public class MasterListDao extends AbstractDao {
	
	public ArrayList<HashMap<String, Object>> getDCSList(DataSet ds){
		ArrayList<HashMap<String, Object>> resultList = null;
		SqlSession sqlSession = null;
		try {
			sqlSession = getSqlSession();
			MasterListMapper mapper = sqlSession.getMapper(MasterListMapper.class);
			resultList = mapper.getDCSList(ds);
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			sqlSessionClose();
		}
		
		return resultList;
	}

	public String getSysGuid(){
		String sysUid = null;
		SqlSession sqlSession = null;
		try {
			sqlSession = getSqlSession();
			MasterListMapper mapper = sqlSession.getMapper(MasterListMapper.class);
			sysUid = mapper.getSysGuid();
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			sqlSessionClose();
		}
		
		return sysUid;
	}
	
}
