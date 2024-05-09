package com.kgm.dao;

import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.ibatis.session.SqlSession;

import com.kgm.common.remote.DataSet;
import com.kgm.common.util.LogUtil;
import com.kgm.mapper.DownDataSetMapper;

/**
 * [20160928][ymjang] log4j에 의한 에러 로그 기록
 * [20160930][ymjang] commit 문장 추가
 */
public class DownDataSetDao extends AbstractDao {
	
	/**
	 * DataSet Download Log Insert.
	 * @Copyright : S-PALM
	 * @author : 권상기
	 * @since  : 2013. 1. 8.
	 * @param data
	 * @throws Exception 
	 */
	public Integer downDataSetlogInsert(DataSet ds) throws Exception {
		
		SqlSession sqlSession = null;
		int ii = -1;
		try {
			sqlSession = getSqlSession();
			sqlSession.getConnection().setAutoCommit(false);
			DownDataSetMapper mapper = sqlSession.getMapper(DownDataSetMapper.class);
			ii = mapper.downDataSetlogInsert(ds);
			sqlSession.getConnection().commit();
		}catch (Exception e) {
			e.printStackTrace();
			
			try {
				sqlSession.getConnection().rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		}finally {
			sqlSessionClose();
		}
		
		return ii;
	}
	
	/**
	 * DataSet Download Log Select.
	 * @Copyright : S-PALM
	 * @author : 권상기
	 * @since  : 2013. 1. 8.
	 * @param uid
	 * @return
	 * @throws Exception 
	 */
	@SuppressWarnings("rawtypes")
	public ArrayList downDataSetlogSelect(DataSet ds) throws Exception {
		
		SqlSession sqlSession = null;
		ArrayList list = null;
		try{
			sqlSession = getSqlSession();
			DownDataSetMapper mapper = sqlSession.getMapper(DownDataSetMapper.class);
			
			list = mapper.downDataSetlogSelect(ds);
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		}finally {
			sqlSessionClose();
		}
		
		return list;
	}
	
	/**
	 * DataSet Down Log Date 별 Select.
	 * @Copyright : S-PALM
	 * @author : 권상기
	 * @since  : 2013. 1. 9.
	 * @param data
	 * @return
	 * @throws Exception 
	 */
	@SuppressWarnings("rawtypes")
	public ArrayList downDataSetLogDateSelect(DataSet ds) throws Exception {
		
		SqlSession sqlSession = null;
		ArrayList list = null;
		try{
			sqlSession = getSqlSession();
			DownDataSetMapper mapper = sqlSession.getMapper(DownDataSetMapper.class);
			
			list = mapper.downDataSetLogDateSelect(ds);
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		}finally {
			sqlSessionClose();
		}
		
		return list;
	}
}
