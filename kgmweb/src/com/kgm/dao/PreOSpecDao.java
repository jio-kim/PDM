package com.kgm.dao;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.ibatis.session.SqlSession;

import com.kgm.common.remote.DataSet;
import com.kgm.common.util.LogUtil;
import com.kgm.mapper.PreOSpecMapper;

/**
 * [20160928][ymjang] log4j에 의한 에러 로그 기록
 */
public class PreOSpecDao extends AbstractDao {
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void insertTrim(DataSet ds) throws Exception{
		
		SqlSession session = null;
        try {
            session = getSqlSession();
            session.getConnection().setAutoCommit(false);
            
            String ospecID = (String)ds.get("OSPEC_NO");
            ArrayList<HashMap> trims = (ArrayList<HashMap>)ds.get("DATA");
            
            session.delete("com.kgm.mapper.PreOSpecMapper.deleteTrim", ds);
            for( HashMap map : trims){
            	map.put("OSPEC_NO", ospecID);
            	session.insert("com.kgm.mapper.PreOSpecMapper.insertTrim", map);
            }
            session.commit();
        } catch (Exception e) {
        	session.rollback();
            e.printStackTrace();
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
            
            throw e;
        } finally {
            sqlSessionClose();
        }		
	}
	
	public ArrayList<HashMap<String, String>> selectPreOSpecMandatory(DataSet ds) throws Exception {
		ArrayList<HashMap<String, String>> resultList = null;
		try{
			SqlSession sqlSession = getSqlSession();
			PreOSpecMapper mapper = sqlSession.getMapper(PreOSpecMapper.class);	
			resultList = mapper.selectPreOSpecMandatory(ds);
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSessionClose();
		}
		return resultList;
	}
	
	public ArrayList<HashMap<String, String>> selectPreOSpecMandatoryInfo(DataSet ds) throws Exception {
		ArrayList<HashMap<String, String>> resultList = null;
		try{
			SqlSession sqlSession = getSqlSession();
			PreOSpecMapper mapper = sqlSession.getMapper(PreOSpecMapper.class);	
			resultList = mapper.selectPreOSpecMandatoryInfo(ds);
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSessionClose();
		}
		return resultList;
	}
	
	public ArrayList<HashMap<String, String>> selectPreOSpecMandatoryTrim(DataSet ds) throws Exception {
		ArrayList<HashMap<String, String>> resultList = null;
		try{
			SqlSession sqlSession = getSqlSession();
			PreOSpecMapper mapper = sqlSession.getMapper(PreOSpecMapper.class);	
			resultList = mapper.selectPreOSpecMandatoryTrim(ds);
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSessionClose();
		}
		return resultList;
	}
	
	public void insertPreOSpecMandatoryInfo(DataSet ds) throws Exception {
		try{
			SqlSession sqlSession = getSqlSession();
			PreOSpecMapper mapper = sqlSession.getMapper(PreOSpecMapper.class);	
			mapper.insertPreOSpecMandatoryInfo(ds);
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSessionClose();
		}
	}
	
	public void insertPreOSpecMandatoryTrim(DataSet ds) throws Exception {
		try{
			SqlSession sqlSession = getSqlSession();
			PreOSpecMapper mapper = sqlSession.getMapper(PreOSpecMapper.class);	
			mapper.insertPreOSpecMandatoryTrim(ds);
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSessionClose();
		}
	}
	
	public void deletePreOSpecMandatoryInfo(DataSet ds) throws Exception {
		try{
			SqlSession sqlSession = getSqlSession();
			PreOSpecMapper mapper = sqlSession.getMapper(PreOSpecMapper.class);	
			mapper.deletePreOSpecMandatoryInfo(ds);
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSessionClose();
		}
	}
	
	public void deletePreOSpecMandatoryTrim(DataSet ds) throws Exception {
		try{
			SqlSession sqlSession = getSqlSession();
			PreOSpecMapper mapper = sqlSession.getMapper(PreOSpecMapper.class);	
			mapper.deletePreOSpecMandatoryTrim(ds);
		}catch(Exception e){
			e.printStackTrace();

			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSessionClose();
		}
	}
}
