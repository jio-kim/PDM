package com.ssangyong.dao;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.ibatis.session.SqlSession;

import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.util.LogUtil;
import com.ssangyong.mapper.DCSFavoritesMapper;

/**
 * [20160928][ymjang] log4j에 의한 에러 로그 기록
 * [20161201][ymjang] commit/rollback 구문 위치 정리
 */
public class DCSFavoritesDao extends AbstractDao {

	public DCSFavoritesDao() {

	}

	public boolean insertDCSFavoritesTeam(DataSet dataSet) {
		SqlSession sqlSession = null;

		try {
			sqlSession = getSqlSession();
			DCSFavoritesMapper mapper = sqlSession.getMapper(DCSFavoritesMapper.class);
			mapper.insertDCSFavoritesTeam(dataSet);
			
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

	public boolean deleteDCSFavoritesTeam(DataSet dataSet) {
		SqlSession sqlSession = null;

		try {
			sqlSession = getSqlSession();
			DCSFavoritesMapper mapper = sqlSession.getMapper(DCSFavoritesMapper.class);
			mapper.deleteDCSFavoritesTeam(dataSet);
			
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

	public ArrayList<HashMap<String, Object>> selectDCSFavoritesTeamList(DataSet dataSet) {
		SqlSession sqlSession = null;
		ArrayList<HashMap<String, Object>> resultList = null;

		try {
			sqlSession = getSqlSession();
			DCSFavoritesMapper mapper = sqlSession.getMapper(DCSFavoritesMapper.class);
			resultList = mapper.selectDCSFavoritesTeamList(dataSet);
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
