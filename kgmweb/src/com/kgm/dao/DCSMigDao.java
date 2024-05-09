package com.kgm.dao;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.ibatis.session.SqlSession;

import com.kgm.common.remote.DataSet;
import com.kgm.common.util.LogUtil;
import com.kgm.mapper.DCSMigMapper;

public class DCSMigDao extends AbstractDao {

	public DCSMigDao() {

	}

	public ArrayList<HashMap<String, Object>> selectWorkflowInfoList(DataSet dataSet) {
		SqlSession sqlSession = null;
		ArrayList<HashMap<String, Object>> resultList = null;

		try {
			sqlSession = getSqlSession();
			DCSMigMapper mapper = sqlSession.getMapper(DCSMigMapper.class);
			resultList = mapper.selectWorkflowInfoList(dataSet);
		} catch (Exception e) {
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), dataSet);
			
		} finally {
			sqlSessionClose();
		}

		return resultList;
	}

	public ArrayList<HashMap<String, Object>> selectMyWorkflowInfoList(DataSet dataSet) {
		SqlSession sqlSession = null;
		ArrayList<HashMap<String, Object>> resultList = null;

		try {
			sqlSession = getSqlSession();
			DCSMigMapper mapper = sqlSession.getMapper(DCSMigMapper.class);
			resultList = mapper.selectMyWorkflowInfoList(dataSet);
		} catch (Exception e) {
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), dataSet);
			
		} finally {
			sqlSessionClose();
		}

		return resultList;
	}

}
