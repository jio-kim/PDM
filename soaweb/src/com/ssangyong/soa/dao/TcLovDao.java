package com.ssangyong.soa.dao;

import java.util.HashMap;
import java.util.List;

import org.apache.ibatis.session.SqlSession;

import com.ssangyong.common.remote.DataSet;
import com.ssangyong.soa.mapper.TcLovMapper;


public class TcLovDao extends AbstractDao {
	
	public List<HashMap<String, Object>> getLOVVList(DataSet ds){
		List<HashMap<String, Object>> resultList = null;
		SqlSession sqlSession = null;
		try {
			sqlSession = getSqlSession();
			TcLovMapper mapper = sqlSession.getMapper(TcLovMapper.class);
			resultList = mapper.getLOVVList(ds);
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			sqlSessionClose();
		}
		
		return resultList;
	}

	public List<HashMap<String, Object>> getLOVDescList(DataSet ds){
		List<HashMap<String, Object>> resultList = null;
		SqlSession sqlSession = null;
		try {
			sqlSession = getSqlSession();
			TcLovMapper mapper = sqlSession.getMapper(TcLovMapper.class);
			resultList = mapper.getLOVDescList(ds);
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			sqlSessionClose();
		}
		
		return resultList;
	}
	
}
