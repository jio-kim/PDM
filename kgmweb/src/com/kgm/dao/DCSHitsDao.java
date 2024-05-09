package com.kgm.dao;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.ibatis.session.SqlSession;

import com.kgm.common.remote.DataSet;
import com.kgm.common.util.LogUtil;
import com.kgm.mapper.DCSHitsMapper;

/**
 * [20160928][ymjang] log4j에 의한 에러 로그 기록
 * [20161201][ymjang] commit/rollback 구문 위치 정리
 * 
 */
public class DCSHitsDao extends AbstractDao {

	public DCSHitsDao() {

	}

	public boolean insertDCSHits(DataSet dataSet) {
		SqlSession sqlSession = null;

		try {
			sqlSession = getSqlSession();
			DCSHitsMapper mapper = sqlSession.getMapper(DCSHitsMapper.class);
			mapper.insertDCSHits(dataSet);
			
			sqlSession.commit();
			
		} catch (Exception e) {
			e.printStackTrace();
			
			sqlSession.rollback();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), dataSet);
			
		} finally {
			sqlSessionClose();
		}

		return true;
	}

	public ArrayList<HashMap<String, Object>> selectDCSHits(DataSet dataSet) {
		SqlSession sqlSession = null;
		ArrayList<HashMap<String, Object>> resultList = null;

		try {
			sqlSession = getSqlSession();
			DCSHitsMapper mapper = sqlSession.getMapper(DCSHitsMapper.class);
			resultList = mapper.selectDCSHits(dataSet);
		} catch (Exception e) {
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), dataSet);
			
		} finally {
			sqlSessionClose();
		}

		return resultList;
	}

	public ArrayList<HashMap<String, Object>> selectDetailDCSHits(DataSet dataSet) {
		SqlSession sqlSession = null;
		ArrayList<HashMap<String, Object>> resultList = null;

		try {
			sqlSession = getSqlSession();
			DCSHitsMapper mapper = sqlSession.getMapper(DCSHitsMapper.class);
			resultList = mapper.selectDetailDCSHits(dataSet);
		} catch (Exception e) {
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), dataSet);
			
		} finally {
			sqlSessionClose();
		}

		return resultList;
	}
	
	public int isExistsDCSWorkflowHistory(DataSet dataSet) {
		SqlSession sqlSession = null;
		int rtn = 0;

		try {
			sqlSession = getSqlSession();
			DCSHitsMapper mapper = sqlSession.getMapper(DCSHitsMapper.class);
			rtn = mapper.isExistsDCSWorkflowHistory(dataSet);
		} catch (Exception e) {
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), dataSet);
			
		} finally {
			sqlSessionClose();
		}

		return rtn;
	}

	public boolean saveDCSWorkflowHistory(DataSet dataSet) {
		SqlSession sqlSession = null;
		try {
			sqlSession = getSqlSession();
			DCSHitsMapper mapper = sqlSession.getMapper(DCSHitsMapper.class);
			
			// 기 등록여부 체크
			int rtn = mapper.isExistsDCSWorkflowHistory(dataSet);		
			if (rtn == 0) {
				// Seq ++
				int seq = mapper.getDCSWorkflowHistoryMaxSeq();
				dataSet.put("SEQ", seq);
				
				mapper.insertDCSWorkflowHistory(dataSet);
			}
			else {
				mapper.updDCSWorkflowHistory(dataSet);
			}
			
			sqlSession.commit();
			
		} catch (Exception e) {
			e.printStackTrace();
			
			sqlSession.rollback();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), dataSet);
			
		} finally {
			sqlSessionClose();
		}

		return true;
	}
	
	
	public boolean insertDCSWorkflowHistory(DataSet dataSet) {
		SqlSession sqlSession = null;
		try {
			sqlSession = getSqlSession();
			DCSHitsMapper mapper = sqlSession.getMapper(DCSHitsMapper.class);
			
			// Seq ++
			int seq = mapper.getDCSWorkflowHistoryMaxSeq();
			dataSet.put("SEQ", seq);
			
			mapper.insertDCSWorkflowHistory(dataSet);
			
			sqlSession.commit();
			
		} catch (Exception e) {
			e.printStackTrace();
			
			sqlSession.rollback();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), dataSet);
			
		} finally {
			sqlSessionClose();
		}

		return true;
	}
	
	public boolean updDCSWorkflowHistory(DataSet dataSet) {
		SqlSession sqlSession = null;
		try {
			sqlSession = getSqlSession();
			DCSHitsMapper mapper = sqlSession.getMapper(DCSHitsMapper.class);
			
			mapper.updDCSWorkflowHistory(dataSet);
			
			sqlSession.commit();
			
		} catch (Exception e) {
			e.printStackTrace();
			
			sqlSession.rollback();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), dataSet);
			
		} finally {
			sqlSessionClose();
		}

		return true;
	}
	
}
