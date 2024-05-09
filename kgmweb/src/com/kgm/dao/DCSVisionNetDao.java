package com.kgm.dao;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.ibatis.session.SqlSession;

import com.kgm.common.remote.DataSet;
import com.kgm.common.util.LogUtil;
import com.kgm.mapper.DCSVisionNetMapper;

/**
 * [20160923][ymjang] log4j�� ���� ���� �α� ���
 * [20161201][ymjang] commit/rollback ���� ��ġ ����
 */
public class DCSVisionNetDao extends AbstractDao {

	public DCSVisionNetDao() {

	}

	public ArrayList<HashMap<String, Object>> selectVNetTeamList(DataSet dataSet) {
		SqlSession sqlSession = null;
		ArrayList<HashMap<String, Object>> resultList = null;

		try {
			sqlSession = getSqlSession();
			DCSVisionNetMapper mapper = sqlSession.getMapper(DCSVisionNetMapper.class);
			resultList = mapper.selectVNetTeamList(dataSet);
			
			sqlSession.commit();
			
		} catch (Exception e) {
			e.printStackTrace();
			
			sqlSession.rollback();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), dataSet);
	    	
		} finally {
			sqlSessionClose();
		}

		return resultList;
	}

	public ArrayList<HashMap<String, Object>> selectVNetTeamHistList(DataSet dataSet) {
		SqlSession sqlSession = null;
		ArrayList<HashMap<String, Object>> resultList = null;

		try {
			sqlSession = getSqlSession();
			DCSVisionNetMapper mapper = sqlSession.getMapper(DCSVisionNetMapper.class);
			resultList = mapper.selectVNetTeamHistList(dataSet);
		} catch (Exception e) {
			e.printStackTrace();
			sqlSession.rollback();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), dataSet);
			
		} finally {
			sqlSessionClose();
		}

		return resultList;
	}

	public ArrayList<HashMap<String, Object>> selectVNetUserList(DataSet dataSet) {
		SqlSession sqlSession = null;
		ArrayList<HashMap<String, Object>> resultList = null;

		try {
			sqlSession = getSqlSession();
			DCSVisionNetMapper mapper = sqlSession.getMapper(DCSVisionNetMapper.class);
			resultList = mapper.selectVNetUserList(dataSet);
			
			sqlSession.commit();
			
		} catch (Exception e) {
			e.printStackTrace();
			
			sqlSession.rollback();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), dataSet);
			
		} finally {
			sqlSessionClose();
		}

		return resultList;
	}

	
	public ArrayList<HashMap<String, Object>> getVnetAndTcLiveSameTeamCode(DataSet dataSet) {
		SqlSession sqlSession = null;
		ArrayList<HashMap<String, Object>> resultList = null;

		try {
			sqlSession = getSqlSession();
			DCSVisionNetMapper mapper = sqlSession.getMapper(DCSVisionNetMapper.class);
			resultList = mapper.getVnetAndTcLiveSameTeamCode(dataSet);
			
			sqlSession.commit();
			
		} catch (Exception e) {
			e.printStackTrace();
			
			sqlSession.rollback();
			
			LogUtil.error(e.getMessage(), dataSet);
			
		} finally {
			sqlSessionClose();
		}

		return resultList;
	}
	
	public ArrayList<HashMap<String, Object>> getVnetTeamName(DataSet dataSet) {
		SqlSession sqlSession = null;
		ArrayList<HashMap<String, Object>> resultList = null;

		try {
			sqlSession = getSqlSession();
			DCSVisionNetMapper mapper = sqlSession.getMapper(DCSVisionNetMapper.class);
			resultList = mapper.getVnetTeamName(dataSet);
			
			sqlSession.commit();
			
		} catch (Exception e) {
			e.printStackTrace();
			
			sqlSession.rollback();
			
			LogUtil.error(e.getMessage(), dataSet);
			
		} finally {
			sqlSessionClose();
		}

		return resultList;
	}
}
