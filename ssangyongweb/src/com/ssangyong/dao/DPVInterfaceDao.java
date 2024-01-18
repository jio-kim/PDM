package com.ssangyong.dao;

import java.util.ArrayList;

import org.apache.ibatis.session.SqlSession;

import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.util.LogUtil;
import com.ssangyong.dto.DPVInterfaceData;
import com.ssangyong.mapper.DPVInterfaceMapper;

/**
 * [20160928][ymjang] log4j에 의한 에러 로그 기록
 */
public class DPVInterfaceDao extends AbstractDao {
	
	public ArrayList<DPVInterfaceData> searchTargetReports(DataSet ds) throws Exception {
		
		SqlSession sqlSession = null;
		ArrayList<DPVInterfaceData> queryResults = null;
		
		try{
			sqlSession = getSqlSession();
			DPVInterfaceMapper mapper = sqlSession.getMapper(DPVInterfaceMapper.class);
			queryResults = mapper.searchTargetReports(ds);
		}catch( Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		}finally{
			sqlSessionClose();
		}
		return queryResults;
	}
	
	public boolean updateInterfaceInfo(DataSet ds) throws Exception {
//		DPVInterfaceData objectMap = (DPVInterfaceData)object;
		
		SqlSession sqlSession = null;
		try{
			sqlSession = getSqlSession();
			DPVInterfaceMapper mapper = sqlSession.getMapper(DPVInterfaceMapper.class);
			
			mapper.updateInterfaceInfo(ds);
		}catch( Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		}finally{
			sqlSessionClose();
		}
		return true;
	}
	
	public String getItemRevisionPuid(DataSet ds) throws Exception {
		
		SqlSession sqlSession = null;
		String puid = null;
		
		try{
			sqlSession = getSqlSession();
			DPVInterfaceMapper mapper = sqlSession.getMapper(DPVInterfaceMapper.class);
			puid = mapper.getItemRevisionPuid(ds);
		}catch( Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		}finally{
			sqlSessionClose();
		}
		
		return puid;
	}
	
	public boolean updatePartProperty(DataSet ds) throws Exception {
		
		SqlSession sqlSession = null;
		
		try{
			sqlSession = getSqlSession();
			DPVInterfaceMapper mapper = sqlSession.getMapper(DPVInterfaceMapper.class);
			mapper.updatePartProperty(ds);
		}catch( Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		}finally{
			sqlSessionClose();
		}
		
		return true;
	}
}
