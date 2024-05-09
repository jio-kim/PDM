package com.kgm.dao;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.ibatis.session.SqlSession;

import com.kgm.common.remote.DataSet;
import com.kgm.common.util.LogUtil;
import com.kgm.mapper.DCSNoticeMapper;

/**
 * [20161201][ymjang] commit/rollback 구문 위치 정리
 */
public class DCSNoticeDao extends AbstractDao {

	public DCSNoticeDao() {

	}

	public boolean insertDCSNotice(DataSet dataSet) {
		SqlSession sqlSession = null;

		try {
			sqlSession = getSqlSession();
			DCSNoticeMapper mapper = sqlSession.getMapper(DCSNoticeMapper.class);
			mapper.insertDCSNotice(dataSet);
			
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

	public boolean insertDCSNoticeContents(DataSet dataSet) {
		SqlSession sqlSession = null;

		try {
			sqlSession = getSqlSession();
			DCSNoticeMapper mapper = sqlSession.getMapper(DCSNoticeMapper.class);
			mapper.insertDCSNoticeContents(dataSet);
			
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

	public boolean updateDCSNotice(DataSet dataSet) {
		SqlSession sqlSession = null;

		try {
			sqlSession = getSqlSession();
			DCSNoticeMapper mapper = sqlSession.getMapper(DCSNoticeMapper.class);
			mapper.updateDCSNotice(dataSet);
			
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

	public boolean updateDCSNoticeContents(DataSet dataSet) {
		SqlSession sqlSession = null;

		try {
			sqlSession = getSqlSession();
			DCSNoticeMapper mapper = sqlSession.getMapper(DCSNoticeMapper.class);
			mapper.updateDCSNoticeContents(dataSet);
			
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

	public boolean deleteDCSNotice(DataSet dataSet) {
		SqlSession sqlSession = null;

		try {
			sqlSession = getSqlSession();
			DCSNoticeMapper mapper = sqlSession.getMapper(DCSNoticeMapper.class);
			mapper.deleteDCSNotice(dataSet);
			
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

	public boolean deleteDCSNoticeContents(DataSet dataSet) {
		SqlSession sqlSession = null;

		try {
			sqlSession = getSqlSession();
			DCSNoticeMapper mapper = sqlSession.getMapper(DCSNoticeMapper.class);
			mapper.deleteDCSNoticeContents(dataSet);
			
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

	public ArrayList<HashMap<String, Object>> selectDCSNoticeList() {
		SqlSession sqlSession = null;
		ArrayList<HashMap<String, Object>> resultList = null;

		try {
			sqlSession = getSqlSession();
			DCSNoticeMapper mapper = sqlSession.getMapper(DCSNoticeMapper.class);
			resultList = mapper.selectDCSNoticeList();
		} catch (Exception e) {
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage());
			
		} finally {
			sqlSessionClose();
		}

		return resultList;
	}

	public ArrayList<HashMap<String, Object>> selectDCSNoticeContentsList(DataSet dataSet) {
		SqlSession sqlSession = null;
		ArrayList<HashMap<String, Object>> resultList = null;

		try {
			sqlSession = getSqlSession();
			DCSNoticeMapper mapper = sqlSession.getMapper(DCSNoticeMapper.class);
			resultList = mapper.selectDCSNoticeContentsList(dataSet);
		} catch (Exception e) {
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), dataSet);
			
		} finally {
			sqlSessionClose();
		}

		return resultList;
	}

	public HashMap<String, Object> selectNextOUID() {
		SqlSession sqlSession = null;
		HashMap<String, Object> ouidMap = null;

		try {
			sqlSession = getSqlSession();
			DCSNoticeMapper mapper = sqlSession.getMapper(DCSNoticeMapper.class);
			ouidMap = mapper.selectNextOUID();
		} catch (Exception e) {
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage());
			
		} finally {
			sqlSessionClose();
		}

		return ouidMap;
	}

}
